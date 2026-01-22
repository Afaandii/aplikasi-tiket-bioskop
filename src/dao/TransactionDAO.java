package dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import models.*;
import utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Random;

/**
 * Data Access Object untuk Transaction dan Ticket
 * Mengelola operasi CRUD untuk transaksi dan tiket
 */
public class TransactionDAO {
    
    /**
     * Generate transaction code dengan format TIX-yyyyMMdd-###
     * @return transaction code yang unik
     */
    public String generateTransactionCode() {
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String query = "SELECT COUNT(*) FROM Transaction WHERE DATE(created_at) = CURDATE()";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query);
            int count = 1;
            if (rs.next()) {
                count = rs.getInt(1) + 1;
            }
            rs.close();
            return String.format("TIX-%s-%03d", dateStr, count);
        } catch (SQLException e) {
            Random random = new Random();
            int randomNum = random.nextInt(999) + 1;
            return String.format("TIX-%s-%03d", dateStr, randomNum);
        }
    }
    
    /**
     * Generate manual code untuk tiket
     * @return 6 digit random code
     */
    public String generateManualCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
    
    /**
     * Buat transaksi baru dan tiket-tiketnya (tanpa user_id)
     * @param showtimeId ID showtime
     * @param selectedSeats List seat yang dipilih
     * @return Transaction object jika berhasil, null jika gagal
     */
    public Transaction createTransaction(int userId, int ticketPriceId, int showtimeId, List<Seat> seats, String paymentMethod, int totalPrice) {
        String insertSql = "INSERT INTO transaction (user_id, ticket_price_id, showtime_id, transaction_code, total_price, status, payment_method, amount_ticket, created_at) " +
                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        String insertTicketSql = "INSERT INTO tickets (showtime_id, transaction_id, seat_id, price, ticket_code, created_at) " +
                                 "VALUES (?, ?, ?, ?, ?, NOW())";

        String showSql = "SELECT s.id, s.date, s.start_time, m.title AS movie_title, st.name_studio AS studio_name " +
                        "FROM showtimes s " +
                        "JOIN movies m ON s.movie_id = m.id " +
                        "JOIN studios st ON s.studio_id = st.id " +  // ✅ BENAR
                        "WHERE s.id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int txId = 0;
            String kode = generateTransactionCode(); // contoh: "TX-" + System.currentTimeMillis()

            // 1) Insert transaction
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setInt(2, ticketPriceId);
                ps.setInt(3, showtimeId);
                ps.setString(4, kode);
                ps.setBigDecimal(5, new BigDecimal(totalPrice).setScale(2, RoundingMode.HALF_UP));
                ps.setString(6, "completed"); // langsung completed (offline mode)
                ps.setString(7, paymentMethod);
                ps.setInt(8, seats.size());
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    txId = rs.getInt(1);
                }
                rs.close();
            }

            if (txId == 0) {
                conn.rollback();
                return null;
            }

            // 2) Insert tickets untuk tiap kursi
            try (PreparedStatement psTicket = conn.prepareStatement(insertTicketSql)) {
                // Ambil harga dari ticket_price berdasarkan showtime_id
                TicketPriceDAO ticketPriceDAO = new TicketPriceDAO();
                double ticketPrice = ticketPriceDAO.getPriceByShowtimeId(showtimeId);

                for (Seat seat : seats) {
                    String ticketCode = "TK-" + txId + "-" + seat.getSeatsCode();

                    psTicket.setInt(1, showtimeId);
                    psTicket.setInt(2, txId);
                    psTicket.setInt(3, seat.getId());
                    psTicket.setDouble(4, ticketPrice); // Gunakan harga dari ticket_price
                    psTicket.setString(5, ticketCode);

                    psTicket.addBatch();
                }
                psTicket.executeBatch();
            }

            // 3) Ambil detail showtime + movie + cinema
            Transaction tx = new Transaction();
            try (PreparedStatement ps2 = conn.prepareStatement(showSql)) {
                ps2.setInt(1, showtimeId);
                ResultSet rs2 = ps2.executeQuery();

                tx.setId(txId);
                tx.setShowtimeId(showtimeId);
                tx.setTransactionCode(kode);
                tx.setTotalPrice(totalPrice);
                tx.setStatus("completed");
                tx.setPaymentMethod(paymentMethod);
                tx.setSeats(seats);

                if (rs2.next()) {
                    tx.setMovieTitle(rs2.getString("movie_title"));
                    tx.setShowDate(rs2.getDate("date"));
                    tx.setShowTime(rs2.getTime("start_time"));
                    tx.setCinemaName(rs2.getString("studio_name"));
                    
                    Showtime showtime = new Showtime();
                    showtime.setId(rs2.getInt("id"));
                    showtime.setStudioName(rs2.getString("studio_name"));
                    showtime.setDate(rs2.getDate("date"));
                    showtime.setStartTime(rs2.getTime("start_time"));
                    showtime.setMovieTitle(rs2.getString("movie_title"));
                    tx.setShowtime(showtime);
                }
                rs2.close();
            }

            conn.commit();
            conn.setAutoCommit(true);
            return tx;

        } catch (SQLException ex) {
            try {
                if (conn != null) {
                    conn.rollback();
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ignored) {}
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Update status transaksi
     */
    public boolean updateTransactionStatus(int transactionId, String status) {
        Connection conn = DatabaseConnection.getConnection();
        try {
            conn.setAutoCommit(false);

            String transQuery = "UPDATE Transaction SET status = ? WHERE id = ?";
            int result = DatabaseConnection.executeUpdate(transQuery, status, transactionId);

            if (result > 0) {
                if ("completed".equals(status)) {
                    // kursi dikunci permanen
                    String seatQuery = "UPDATE Seats s INNER JOIN Tickets t ON s.id = t.seat_id " +
                                       "SET s.status = 'booked' WHERE t.transaction_id = ?";
                    DatabaseConnection.executeUpdate(seatQuery, transactionId);

                    String ticketQuery = "UPDATE Tickets SET status = 'active' WHERE transaction_id = ?";
                    DatabaseConnection.executeUpdate(ticketQuery, transactionId);
                }
                else if ("cancelled".equals(status)) {
                    // kursi dikembalikan ke available
                    String seatQuery = "UPDATE Seats s INNER JOIN Tickets t ON s.id = t.seat_id " +
                                       "SET s.status = 'available' WHERE t.transaction_id = ?";
                    DatabaseConnection.executeUpdate(seatQuery, transactionId);

                    String ticketQuery = "UPDATE Tickets SET status = 'cancelled' WHERE transaction_id = ?";
                    DatabaseConnection.executeUpdate(ticketQuery, transactionId);
                }

                conn.commit();
                conn.setAutoCommit(true);
                return true;
            }

            conn.rollback();
            conn.setAutoCommit(true);
            return false;

        } catch (SQLException e) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            System.err.println("Error updating transaction status: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Ambil transaksi berdasarkan ID
     */
    public Transaction getTransactionById(int transactionId) {
        String query = "SELECT t.*, m.title as movie_title, s.date as show_date, s.broadcast as show_time " +
                      "FROM Transaction t " +
                      "INNER JOIN Showtimes s ON t.showtime_id = s.id " +
                      "INNER JOIN Movies m ON s.movie_id = m.id " +
                      "WHERE t.id = ?";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query, transactionId);
            if (rs.next()) {
                Transaction transaction = mapResultSetToTransaction(rs);
                rs.close();
                return transaction;
            }
        } catch (SQLException e) {
            System.err.println("Error getting transaction by ID: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Ambil semua transaksi (untuk admin)
     */
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT t.*, m.title as movie_title, s.date as show_date, s.broadcast as show_time " +
                      "FROM Transaction t " +
                      "INNER JOIN Showtimes s ON t.showtime_id = s.id " +
                      "INNER JOIN Movies m ON s.movie_id = m.id " +
                      "ORDER BY t.created_at DESC";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query);
            while (rs.next()) {
                Transaction transaction = mapResultSetToTransaction(rs);
                transactions.add(transaction);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error getting all transactions: " + e.getMessage());
        }
        return transactions;
    }
    
    /**
     * Ambil tiket berdasarkan transaksi ID
     */
    public List<Ticket> getTicketsByTransactionId(int transactionId) {
        List<Ticket> tickets = new ArrayList<>();
        String query = "SELECT tk.*, CONCAT(s.seats_row, s.seats_number) as seat_label, " +
                      "m.title as movie_title, st.date as show_date, st.broadcast as show_time " +
                      "FROM Tickets tk " +
                      "INNER JOIN Seats s ON tk.seat_id = s.id " +
                      "INNER JOIN Showtimes st ON s.showtime_id = st.id " +
                      "INNER JOIN Movies m ON st.movie_id = m.id " +
                      "WHERE tk.transaction_id = ?";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query, transactionId);
            while (rs.next()) {
                Ticket ticket = mapResultSetToTicket(rs);
                tickets.add(ticket);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error getting tickets by transaction ID: " + e.getMessage());
        }
        return tickets;
    }
    
    /**
     * Cari transaksi berdasarkan kode
     */
    public Transaction getTransactionByCode(String transactionCode) {
            String query = "SELECT t.*, m.title as movie_title, s.date as show_date, s.broadcast as show_time " +
                          "FROM Transaction t " +
                          "INNER JOIN Showtimes s ON t.showtime_id = s.id " +
                          "INNER JOIN Movies m ON s.movie_id = m.id " +
                          "WHERE t.transaction_code = ?";
            try {
                ResultSet rs = DatabaseConnection.executeQuery(query, transactionCode);
                if (rs.next()) {
                    Transaction transaction = mapResultSetToTransaction(rs);
                    rs.close();
                    return transaction;
                }
            } catch (SQLException e) {
                System.err.println("Error getting transaction by code: " + e.getMessage());
            }
            return null;
        }

        public Ticket getTicketDetailByTransaction(int transactionId) {
        String query = """
            SELECT 
                t.id AS ticket_id,
                t.ticket_code,
                t.price,
                s.seat_label,
                m.title AS movie_title,
                c.name_cinema AS cinema_name,
                sh.show_date,
                sh.show_time,
                tr.id AS transaction_id
            FROM tickets t
            JOIN seats s ON t.seat_id = s.id
            JOIN seat_type st ON s.seat_type_id = st.id
            JOIN showtimes sh ON t.showtime_id = sh.id
            JOIN movies m ON sh.movie_id = m.id
            JOIN cinema c ON sh.cinema_id = c.id
            JOIN transaction tr ON t.transaction_id = tr.id
            WHERE tr.id = ?
            LIMIT 1
        """;

        try {
            ResultSet rs = DatabaseConnection.executeQuery(query, transactionId);
            if (rs.next()) {
                Ticket ticket = new Ticket();
                ticket.setId(rs.getInt("ticket_id"));
                ticket.setTransactionId(rs.getInt("transaction_id"));
                ticket.setTicketCode(rs.getString("ticket_code")); // kode tiket unik
                ticket.setPrice(rs.getInt("price"));
                ticket.setSeatLabel(rs.getString("seat_label"));
                ticket.setMovieTitle(rs.getString("movie_title"));
                ticket.setCinemaName(rs.getString("cinema_name")); // tambahin field baru di model
                ticket.setShowDate(rs.getDate("show_date"));
                ticket.setShowTime(rs.getTime("show_time"));
                rs.close();
                return ticket;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    
    /**
     * Helper method untuk mapping ResultSet ke Transaction
     */
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getInt("id"));
        transaction.setShowtimeId(rs.getInt("showtime_id"));
        transaction.setTransactionCode(rs.getString("transaction_code"));
        transaction.setTotalPrice(rs.getInt("total_price"));
        transaction.setStatus(rs.getString("status"));
        transaction.setAmountTicket(rs.getInt("amount_ticket"));
        transaction.setPaymentMethod(rs.getString("payment_method"));
        transaction.setCreatedAt(rs.getTimestamp("created_at"));

        // Buat Showtime
        Showtime st = new Showtime();
        st.setId(rs.getInt("showtime_id"));
        st.setDate(rs.getDate("date"));
        st.setStartTime(rs.getTime("start_time"));
        st.setMovieTitle(rs.getString("movie_title"));

        transaction.setShowtime(st);

        return transaction;
    }
 
    private Transaction mapResultSetToTransactionForReport(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getInt("id"));
        transaction.setUserId(rs.getInt("user_id"));
        transaction.setShowtimeId(rs.getInt("showtime_id"));
        transaction.setTransactionCode(rs.getString("transaction_code"));
        transaction.setTotalPrice(rs.getInt("total_price"));
        transaction.setStatus(rs.getString("status"));
        transaction.setAmountTicket(rs.getInt("amount_ticket"));
        transaction.setPaymentMethod(rs.getString("payment_method"));
        transaction.setCreatedAt(rs.getTimestamp("created_at"));
        
        return transaction;
    }

    
    /**
     * Helper method untuk mapping ResultSet ke Ticket
     */
    private Ticket mapResultSetToTicket(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setId(rs.getInt("id"));
        ticket.setSeatId(rs.getInt("seat_id"));
        ticket.setTransactionId(rs.getInt("transaction_id"));
        ticket.setPrice(rs.getInt("price"));
        ticket.setQrCode(rs.getString("qr_code"));
        ticket.setTicketCode(rs.getString("ticket_code")); // Ganti di sini
        ticket.setCreatedAt(rs.getTimestamp("created_at"));
        ticket.setSeatLabel(rs.getString("seat_label"));
        ticket.setMovieTitle(rs.getString("movie_title"));
        ticket.setShowDate(rs.getDate("show_date"));
        ticket.setShowTime(rs.getTime("show_time"));
        return ticket;
    }
    
    /**
     * Ambil laporan penjualan per film
     */
    public List<Object[]> getSalesReportByMovie(Date startDate, Date endDate) {
        List<Object[]> report = new ArrayList<>();
        String query = "SELECT m.title, COUNT(t.id) as total_transactions, " +
                      "SUM(t.total_price) as total_revenue, " +
                      "COUNT(tk.id) as total_tickets " +
                      "FROM Transaction t " +
                      "INNER JOIN Showtimes s ON t.showtime_id = s.id " +
                      "INNER JOIN Movies m ON s.movie_id = m.id " +
                      "INNER JOIN Tickets tk ON t.id = tk.transaction_id " +
                      "WHERE t.status = 'completed' AND DATE(t.created_at) BETWEEN ? AND ? " +
                      "GROUP BY m.id, m.title " +
                      "ORDER BY total_revenue DESC";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query, startDate, endDate);
            while (rs.next()) {
                Object[] row = {
                    rs.getString("title"),
                    rs.getInt("total_transactions"),
                    rs.getLong("total_revenue"),
                    rs.getInt("total_tickets")
                };
                report.add(row);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error getting sales report: " + e.getMessage());
        }
        return report;
    }
    
    /**
     * Ambil total pendapatan dalam periode tertentu
     */
    public long getTotalRevenue(Date startDate, Date endDate) {
        String query = "SELECT SUM(total_price) as total FROM Transaction " +
                      "WHERE status = 'completed' AND DATE(created_at) BETWEEN ? AND ?";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query, startDate, endDate);
            if (rs.next()) {
                long total = rs.getLong("total");
                rs.close();
                return total;
            }
        } catch (SQLException e) {
            System.err.println("Error getting total revenue: " + e.getMessage());
        }
        return 0;
    }
    
    /**
    * Ambil semua transaksi dalam rentang tanggal
    */
   public List<Transaction> getTransactionsByDateRange(Date startDate, Date endDate) {
       List<Transaction> transactions = new ArrayList<>();
       String query = """
           SELECT 
               t.id, t.user_id, t.showtime_id, t.transaction_code, t.total_price, t.status, t.payment_method, t.amount_ticket, t.created_at,
               m.title AS movie_title, u.username AS cashier_name
           FROM transaction t
           INNER JOIN showtimes s ON t.showtime_id = s.id
           INNER JOIN movies m ON s.movie_id = m.id
           INNER JOIN user u ON t.user_id = u.id
           WHERE t.status = 'completed'
             AND t.created_at >= ? AND t.created_at < ?
           ORDER BY t.created_at DESC
           """;

       try {
           // Konversi Date ke Timestamp
           Timestamp startTs = new Timestamp(startDate.getTime());
           Timestamp endTs = new Timestamp(endDate.getTime());

           // Tambahkan 1 hari ke endTs agar mencakup seluruh hari
           Calendar cal = Calendar.getInstance();
           cal.setTime(endTs);
           cal.add(Calendar.DAY_OF_MONTH, 1);
           endTs = new Timestamp(cal.getTimeInMillis());

           ResultSet rs = DatabaseConnection.executeQuery(query, startTs, endTs);
           while (rs.next()) {
               Transaction transaction = mapResultSetToTransactionForReport(rs);
               transaction.setMovieTitle(rs.getString("movie_title"));
               transaction.setUsername(rs.getString("cashier_name")); // Set username/kasir
               transactions.add(transaction);
           }
           rs.close();
       } catch (SQLException e) {
           System.err.println("Error getting transactions by date range: " + e.getMessage());
       }
       return transactions;
   }
   
   /**
    * Ambil SEMUA data laporan keuangan (tanpa filter tanggal)
    */
   public List<Transaction> getAllFinancialReportData() {
       List<Transaction> transactions = new ArrayList<>();
       String query = """
           SELECT 
               t.id,
               t.user_id,
               t.showtime_id,
               t.transaction_code,
               t.total_price,
               t.status,
               t.payment_method,
               t.amount_ticket,
               t.created_at,
               u.username AS cashier_name,
               m.title AS movie_title,
               s.date AS show_date,
               s.start_time AS show_start_time
           FROM transaction t
           INNER JOIN user u ON t.user_id = u.id
           INNER JOIN showtimes s ON t.showtime_id = s.id
           INNER JOIN movies m ON s.movie_id = m.id
           WHERE t.status = 'completed'
           ORDER BY t.created_at DESC
           """;

       try {
           ResultSet rs = DatabaseConnection.executeQuery(query);
           while (rs.next()) {
               Transaction tx = new Transaction();
               tx.setId(rs.getInt("id"));
               tx.setUserId(rs.getInt("user_id"));
               tx.setShowtimeId(rs.getInt("showtime_id"));
               tx.setTransactionCode(rs.getString("transaction_code"));
               tx.setTotalPrice(rs.getInt("total_price"));
               tx.setStatus(rs.getString("status"));
               tx.setPaymentMethod(rs.getString("payment_method"));
               tx.setAmountTicket(rs.getInt("amount_ticket"));
               tx.setCreatedAt(rs.getTimestamp("created_at"));
               tx.setUsername(rs.getString("cashier_name"));
               tx.setMovieTitle(rs.getString("movie_title"));
               tx.setShowDate(rs.getDate("show_date"));
               tx.setShowTime(rs.getTime("show_start_time"));

               transactions.add(tx);
           }
           rs.close();
       } catch (SQLException e) {
           System.err.println("Error getting all financial report data: " + e.getMessage());
       }
       return transactions;
   }
   
   /**
     * Ambil SEMUA data laporan keuangan untuk kasir tertentu.
     * @param cashierId ID pengguna (kasir)
     * @return List<Transaction> berisi transaksi milik kasir tersebut.
     */
    public List<Transaction> getAllFinancialReportDataByCashier(int cashierId) {
        String sql = """
            SELECT 
                t.id,
                t.user_id,
                t.showtime_id,
                t.transaction_code,
                t.total_price,
                t.status,
                t.payment_method,
                t.amount_ticket,
                t.created_at,
                u.username AS username,
                m.title AS movie_title,
                s.date AS show_date,
                s.start_time AS show_start_time
            FROM transaction t
            INNER JOIN user u ON t.user_id = u.id
            INNER JOIN showtimes s ON t.showtime_id = s.id
            INNER JOIN movies m ON s.movie_id = m.id
            WHERE u.id = ? AND t.status = 'completed'
            ORDER BY t.created_at DESC
            """;

        List<Transaction> transactions = new ArrayList<>();
        try {
            // Gunakan DatabaseConnection.executeQuery() seperti di metode lainnya
            ResultSet rs = DatabaseConnection.executeQuery(sql, cashierId);
            while (rs.next()) {
                Transaction tx = new Transaction();
                tx.setId(rs.getInt("id"));
                tx.setUserId(rs.getInt("user_id"));
                tx.setShowtimeId(rs.getInt("showtime_id"));
                tx.setTransactionCode(rs.getString("transaction_code"));
                tx.setTotalPrice(rs.getInt("total_price")); // Pastikan kolom total_price di DB bertipe INT/BIGINT
                tx.setAmountTicket(rs.getInt("amount_ticket"));
                tx.setStatus(rs.getString("status"));
                tx.setPaymentMethod(rs.getString("payment_method"));
                tx.setCreatedAt(rs.getTimestamp("created_at"));
                tx.setUsername(rs.getString("username"));
                tx.setMovieTitle(rs.getString("movie_title"));
                tx.setShowDate(rs.getDate("show_date"));
                tx.setShowTime(rs.getTime("show_start_time"));
                transactions.add(tx);
            }
            rs.close(); // Penting: tutup ResultSet
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error in getAllFinancialReportDataByCashier: " + e.getMessage());
        }
        return transactions;
    }

    /**
     * Ambil data laporan keuangan untuk kasir tertentu dalam rentang tanggal.
     * @param startDate Tanggal awal
     * @param endDate Tanggal akhir
     * @param cashierId ID pengguna (kasir)
     * @return List<Transaction> berisi transaksi milik kasir tersebut dalam rentang tanggal.
     */
    public List<Transaction> getTransactionsByDateRangeByCashier(Date startDate, Date endDate, int cashierId) {
        String sql = """
            SELECT 
                t.id,
                t.user_id,
                t.showtime_id,
                t.transaction_code,
                t.total_price,
                t.status,
                t.payment_method,
                t.amount_ticket,
                t.created_at,
                u.username AS username,
                m.title AS movie_title,
                s.date AS show_date,
                s.start_time AS show_start_time
            FROM transaction t
            INNER JOIN user u ON t.user_id = u.id
            INNER JOIN showtimes s ON t.showtime_id = s.id
            INNER JOIN movies m ON s.movie_id = m.id
            WHERE u.id = ? AND t.status = 'completed'
              AND DATE(t.created_at) BETWEEN ? AND ?
            ORDER BY t.created_at DESC
            """;

        List<Transaction> transactions = new ArrayList<>();
        try {
            // Gunakan DatabaseConnection.executeQuery() dengan parameter
            ResultSet rs = DatabaseConnection.executeQuery(sql, cashierId, startDate, endDate);
            while (rs.next()) {
                Transaction tx = new Transaction();
                tx.setId(rs.getInt("id"));
                tx.setUserId(rs.getInt("user_id"));
                tx.setShowtimeId(rs.getInt("showtime_id"));
                tx.setTransactionCode(rs.getString("transaction_code"));
                tx.setTotalPrice(rs.getInt("total_price"));
                tx.setAmountTicket(rs.getInt("amount_ticket"));
                tx.setStatus(rs.getString("status"));
                tx.setPaymentMethod(rs.getString("payment_method"));
                tx.setCreatedAt(rs.getTimestamp("created_at"));
                tx.setUsername(rs.getString("username"));
                tx.setMovieTitle(rs.getString("movie_title"));
                tx.setShowDate(rs.getDate("show_date"));
                tx.setShowTime(rs.getTime("show_start_time"));
                transactions.add(tx);
            }
            rs.close(); // Penting: tutup ResultSet
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error in getTransactionsByDateRangeByCashier: " + e.getMessage());
        }
        return transactions;
    }
    
    /**
    * Ambil semua pengguna dengan peran 'kasir'
    */
    public List<User> getAllCashiers() {
        List<User> cashiers = new ArrayList<>();
        String query = "SELECT id, username FROM user WHERE role_id = 2 ORDER BY username";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query);
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                cashiers.add(user);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error getting cashiers: " + e.getMessage());
        }
        return cashiers;
    }

    /**
    * Ambil transaksi berdasarkan rentang tanggal, nama kasir, dan metode pembayaran.
    * @param startDate Tanggal awal (bisa null)
    * @param endDate Tanggal akhir (bisa null)
    * @param cashierUsername Nama kasir (jika "Semua Kasir", abaikan filter)
    * @param paymentMethod Metode pembayaran (jika "Semua Metode", abaikan filter)
    * @return List<Transaction>
    */
    public List<Transaction> getTransactionsByDateRangeWithFilters(Date startDate, Date endDate, String cashierUsername, String paymentMethod) {
        List<Transaction> transactions = new ArrayList<>();

        // Buat query dasar
        StringBuilder sql = new StringBuilder("""
            SELECT
            t.id,
            t.user_id,
            t.showtime_id,
            t.transaction_code,
            t.total_price,
            t.status,
            t.payment_method,
            t.amount_ticket,
            t.created_at,
            u.username AS username,
            m.title AS movie_title,
            s.date AS show_date,
            s.start_time AS show_start_time
            FROM transaction t
            INNER JOIN user u ON t.user_id = u.id
            INNER JOIN showtimes s ON t.showtime_id = s.id
            INNER JOIN movies m ON s.movie_id = m.id
            WHERE t.status = 'completed'
            """);

        // Tambahkan filter tanggal jika startDate dan endDate tidak null
        if (startDate != null && endDate != null) {
            sql.append(" AND t.created_at >= ? AND t.created_at < ?");
        }

        // Tambahkan filter kasir jika bukan "Semua Kasir"
        if (!"Semua Kasir".equals(cashierUsername)) {
            sql.append(" AND u.username = ?");
        }

        // Tambahkan filter metode pembayaran jika bukan "Semua Metode"
        if (!"Semua Metode".equals(paymentMethod)) {
            sql.append(" AND t.payment_method = ?");
        }

        sql.append(" ORDER BY t.created_at DESC");

        try {
            List<Object> params = new ArrayList<>();

            // Tambahkan parameter untuk filter tanggal jika ada
            if (startDate != null && endDate != null) {
                Timestamp startTs = new Timestamp(startDate.getTime());
                // Tambahkan 1 hari ke endTs agar mencakup seluruh hari
                Calendar cal = Calendar.getInstance();
                cal.setTime(endDate);
                cal.add(Calendar.DAY_OF_MONTH, 1);
                Timestamp endTs = new Timestamp(cal.getTimeInMillis());

                params.add(startTs);
                params.add(endTs);
            }

            // Tambahkan parameter untuk filter kasir
            if (!"Semua Kasir".equals(cashierUsername)) {
                params.add(cashierUsername);
            }

            // Tambahkan parameter untuk filter metode pembayaran
            if (!"Semua Metode".equals(paymentMethod)) {
                params.add(paymentMethod);
            }

            ResultSet rs = DatabaseConnection.executeQuery(sql.toString(), params.toArray());
            while (rs.next()) {
                Transaction tx = new Transaction();
                tx.setId(rs.getInt("id"));
                tx.setUserId(rs.getInt("user_id"));
                tx.setShowtimeId(rs.getInt("showtime_id"));
                tx.setTransactionCode(rs.getString("transaction_code"));
                tx.setTotalPrice(rs.getInt("total_price"));
                tx.setAmountTicket(rs.getInt("amount_ticket"));
                tx.setStatus(rs.getString("status"));
                tx.setPaymentMethod(rs.getString("payment_method"));
                tx.setCreatedAt(rs.getTimestamp("created_at"));
                tx.setUsername(rs.getString("username"));
                tx.setMovieTitle(rs.getString("movie_title"));
                tx.setShowDate(rs.getDate("show_date"));
                tx.setShowTime(rs.getTime("show_start_time"));
                transactions.add(tx);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
}
