package dao;

import models.TicketPrice;
import utils.DatabaseConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk TicketPrice
 */
public class TicketPriceDAO {

    /**
     * Tambah ticket price baru
     */
        /**
     * Tambah ticket price baru
     */
    public int addTicketPrice(TicketPrice ticketPrice) {
        // 🔑 Jangan sebutkan created_at dan updated_at
        String query = "INSERT INTO ticket_price (showtime_id, days, price, date) " +
                       "VALUES (?, ?, ?, ?)";
        try {
            return DatabaseConnection.executeUpdateWithGeneratedKey(query,
                    ticketPrice.getShowtimeId(),
                    ticketPrice.getDays(),
                    ticketPrice.getPrice(),
                    ticketPrice.getDate()
            );
        } catch (SQLException e) {
            System.err.println("Error adding ticket price: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Update ticket price
     */
        /**
     * Update ticket price
     */
    public boolean updateTicketPrice(TicketPrice ticketPrice) {
        // 🔑 Jangan sebutkan updated_at (dan created_at)
        String query = "UPDATE ticket_price SET showtime_id = ?, days = ?, price = ?, date = ? WHERE id = ?";
        try {
            int result = DatabaseConnection.executeUpdate(query,
                    ticketPrice.getShowtimeId(),
                    ticketPrice.getDays(),
                    ticketPrice.getPrice(),
                    ticketPrice.getDate(),
                    ticketPrice.getId()
            );
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error updating ticket price: " + e.getMessage());
            return false;
        }
    }

    /**
     * Hapus ticket price
     */
    public boolean deleteTicketPrice(int ticketPriceId) {
        String query = "DELETE FROM ticket_price WHERE id = ?";
        try {
            int result = DatabaseConnection.executeUpdate(query, ticketPriceId);
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting ticket price: " + e.getMessage());
            return false;
        }
    }

    /**
     * Ambil semua ticket price dengan informasi showtime
     */
    public List<TicketPrice> getAllTicketPrices() {
        List<TicketPrice> ticketPrices = new ArrayList<>();
        String query = "SELECT tp.*, s.movie_id, s.studio_id, s.date as showtime_date, s.start_time, s.end_start, " +
                       "m.title as movie_title, st.name_studio as studio_name " +
                       "FROM ticket_price tp " +
                       "INNER JOIN showtimes s ON tp.showtime_id = s.id " +
                       "INNER JOIN movies m ON s.movie_id = m.id " +
                       "INNER JOIN studios st ON s.studio_id = st.id " +
                       "ORDER BY tp.date DESC";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query);
            while (rs.next()) {
                TicketPrice ticketPrice = mapResultSetToTicketPrice(rs);
                ticketPrices.add(ticketPrice);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error getting all ticket prices: " + e.getMessage());
        }
        return ticketPrices;
    }

    /**
     * Ambil ticket price berdasarkan ID
     */
    public TicketPrice getTicketPriceById(int ticketPriceId) {
        String query = "SELECT tp.*, s.movie_id, s.studio_id, s.date as showtime_date, s.start_time, s.end_start, " +
                       "m.title as movie_title, st.name_studio as studio_name " +
                       "FROM ticket_price tp " +
                       "INNER JOIN showtimes s ON tp.showtime_id = s.id " +
                       "INNER JOIN movies m ON s.movie_id = m.id " +
                       "INNER JOIN studios st ON s.studio_id = st.id " +
                       "WHERE tp.id = ?";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query, ticketPriceId);
            if (rs.next()) {
                TicketPrice ticketPrice = mapResultSetToTicketPrice(rs);
                rs.close();
                return ticketPrice;
            }
        } catch (SQLException e) {
            System.err.println("Error getting ticket price by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Ambil daftar showtime untuk dropdown
     */
    public List<String> getShowtimeList() {
        List<String> showtimes = new ArrayList<>();
        String query = "SELECT s.id, m.title, st.name_studio, s.date, s.start_time " +
                       "FROM showtimes s " +
                       "INNER JOIN movies m ON s.movie_id = m.id " +
                       "INNER JOIN studios st ON s.studio_id = st.id " +
                       "ORDER BY s.date DESC, s.start_time DESC";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query);
            while (rs.next()) {
                int id = rs.getInt("id");
                String movieTitle = rs.getString("title");
                String studioName = rs.getString("name_studio");
                java.sql.Date date = rs.getDate("date");
                java.sql.Time startTime = rs.getTime("start_time");
                String info = id + " - " + movieTitle + " @ " + studioName + " (" + date + " " + startTime + ")";
                showtimes.add(info);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error getting showtime list: " + e.getMessage());
        }
        return showtimes;
    }

    /**
     * Helper method untuk mapping ResultSet ke TicketPrice
     */
    private TicketPrice mapResultSetToTicketPrice(ResultSet rs) throws SQLException {
        TicketPrice ticketPrice = new TicketPrice();
        ticketPrice.setId(rs.getInt("id"));
        ticketPrice.setShowtimeId(rs.getInt("showtime_id"));
        ticketPrice.setDays(rs.getString("days"));
        ticketPrice.setPrice(rs.getDouble("price"));
        ticketPrice.setDate(rs.getDate("date"));
        ticketPrice.setCreatedAt(rs.getTimestamp("created_at"));
        ticketPrice.setUpdatedAt(rs.getTimestamp("updated_at"));

        // Buat string deskripsi showtime untuk ditampilkan di tabel
        String movieTitle = rs.getString("movie_title");
        String studioName = rs.getString("studio_name");
        java.sql.Date showtimeDate = rs.getDate("showtime_date");
        java.sql.Time startTime = rs.getTime("start_time");
        ticketPrice.setShowtimeInfo(movieTitle + " @ " + studioName + " (" + showtimeDate + " " + startTime + ")");

        return ticketPrice;
    }
    
    /**
    * Ambil harga tiket berdasarkan showtime_id
    */
    public double getPriceByShowtimeId(int showtimeId) {
        String query = "SELECT price FROM ticket_price WHERE showtime_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(query)) {
            ps.setInt(1, showtimeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("price");
            }
        } catch (SQLException e) {
            System.err.println("Error getting price by showtime ID: " + e.getMessage());
        }
        return 0.0; // default jika tidak ditemukan
    }
    
    /**
    * Ambil ID ticket_price berdasarkan showtime_id
    */
   public int getTicketPriceIdByShowtimeId(int showtimeId) {
       String query = "SELECT id FROM ticket_price WHERE showtime_id = ?";
       try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(query)) {
           ps.setInt(1, showtimeId);
           ResultSet rs = ps.executeQuery();
           if (rs.next()) {
               return rs.getInt("id");
           }
       } catch (SQLException e) {
           System.err.println("Error getting ticket_price ID: " + e.getMessage());
       }
       return 0; // default jika tidak ditemukan
   }

    /**
     * Ekstrak ID dari string combo (format: "ID - ...")
     */
    public static int extractIdFromCombo(String selected) {
        if (selected != null && selected.contains(" - ")) {
            return Integer.parseInt(selected.split(" - ")[0]);
        }
        return 0;
    }
}