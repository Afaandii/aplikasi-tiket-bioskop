package dao;

import utils.DatabaseConnection;
import models.Seat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO untuk tabel Seats
 */
public class SeatDAO {

    /**
     * Ambil semua kursi berdasarkan studio_id
     */
    public List<Seat> getSeatsByStudio(int studioId) {
        List<Seat> seats = new ArrayList<>();
        try {
            String query = "SELECT s.*, st.name_studio AS studio_name " +
                           "FROM Seats s " +
                           "JOIN studios st ON s.studio_id = st.id " +
                           "WHERE s.studio_id = ?";
            ResultSet rs = DatabaseConnection.executeQuery(query, studioId);

            while (rs.next()) {
                Seat seat = new Seat();
                seat.setId(rs.getInt("id"));
                seat.setStudioId(rs.getInt("studio_id"));
                seat.setSeatsRow(rs.getString("seats_row").charAt(0));
                seat.setSeatsNumber(rs.getInt("seats_number"));
                seat.setSeatsCode(rs.getString("seats_code"));
                seat.setStatus(rs.getString("status"));
                seat.setCreatedAt(rs.getTimestamp("created_at"));
                seat.setUpdatedAt(rs.getTimestamp("updated_at"));
                seat.setStudioName(rs.getString("studio_name"));
                seats.add(seat);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seats;
    }

    /**
     * Ambil semua kursi (untuk ditampilkan di tabel admin)
     */
    public List<Seat> getAllSeats() {
        List<Seat> seats = new ArrayList<>();
        try {
            String query = "SELECT s.*, st.name_studio AS studio_name " +
                           "FROM Seats s " +
                           "JOIN studios st ON s.studio_id = st.id";
            ResultSet rs = DatabaseConnection.executeQuery(query);

            while (rs.next()) {
                Seat seat = new Seat();
                seat.setId(rs.getInt("id"));
                seat.setStudioId(rs.getInt("studio_id"));
                seat.setSeatsRow(rs.getString("seats_row").charAt(0));
                seat.setSeatsNumber(rs.getInt("seats_number"));
                seat.setSeatsCode(rs.getString("seats_code"));
                seat.setStatus(rs.getString("status"));
                seat.setCreatedAt(rs.getTimestamp("created_at"));
                seat.setUpdatedAt(rs.getTimestamp("updated_at"));
                seat.setStudioName(rs.getString("studio_name"));
                seats.add(seat);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seats;
    }

    /**
     * Ambil satu kursi berdasarkan ID
     */
    public Seat getSeatById(int id) {
        Seat seat = null;
        try {
            String query = "SELECT s.*, st.name_studio AS studio_name " +
                           "FROM Seats s " +
                           "JOIN studios st ON s.studio_id = st.id " +
                           "WHERE s.id = ?";
            ResultSet rs = DatabaseConnection.executeQuery(query, id);

            if (rs.next()) {
                seat = new Seat();
                seat.setId(rs.getInt("id"));
                seat.setStudioId(rs.getInt("studio_id"));
                seat.setSeatsRow(rs.getString("seats_row").charAt(0));
                seat.setSeatsNumber(rs.getInt("seats_number"));
                seat.setSeatsCode(rs.getString("seats_code"));
                seat.setStatus(rs.getString("status"));
                seat.setCreatedAt(rs.getTimestamp("created_at"));
                seat.setUpdatedAt(rs.getTimestamp("updated_at"));
                seat.setStudioName(rs.getString("studio_name"));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seat;
    }

    /**
     * Tambahkan kursi baru
     */
    public boolean addSeat(Seat seat) {
        try {
            String query = "INSERT INTO Seats (studio_id, seats_row, seats_number, seats_code, status) VALUES (?, ?, ?, ?, ?)";
            int rowsAffected = DatabaseConnection.executeUpdate(query,
                    seat.getStudioId(),
                    String.valueOf(seat.getSeatsRow()),
                    seat.getSeatsNumber(),
                    seat.getSeatsCode(),
                    seat.getStatus());
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update kursi
     */
    public boolean updateSeat(Seat seat) {
        try {
            String query = "UPDATE Seats SET studio_id = ?, seats_row = ?, seats_number = ?, seats_code = ?, status = ?, updated_at = NOW() WHERE id = ?";
            int rowsAffected = DatabaseConnection.executeUpdate(query,
                    seat.getStudioId(),
                    String.valueOf(seat.getSeatsRow()),
                    seat.getSeatsNumber(),
                    seat.getSeatsCode(),
                    seat.getStatus(),
                    seat.getId());
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Hapus kursi
     */
    public boolean deleteSeat(int seatId) {
        try {
            String query = "DELETE FROM Seats WHERE id = ?";
            int rowsAffected = DatabaseConnection.executeUpdate(query, seatId);
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Generate kursi default untuk studio tertentu berdasarkan kapasitas studio
     */
    public void generateSeatsForStudio(int studioId) {
        try {
            String studioQuery = "SELECT capacity FROM studios WHERE id = ?";
            ResultSet rs = DatabaseConnection.executeQuery(studioQuery, studioId);

            if (rs.next()) {
                int totalSeats = rs.getInt("capacity");
                int rows = 10;
                int seatsPerRow = (int) Math.ceil((double) totalSeats / rows);

                for (int i = 0; i < rows; i++) {
                    char row = (char) ('A' + i);
                    for (int j = 1; j <= seatsPerRow; j++) {
                        String seatCode = row + String.valueOf(j);
                        String insertQuery = "INSERT INTO Seats (studio_id, seats_row, seats_number, seats_code, status) VALUES (?, ?, ?, ?, 'available')";
                        DatabaseConnection.executeUpdate(insertQuery, studioId, String.valueOf(row), j, seatCode);
                    }
                }
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ambil semua kursi berdasarkan showtime_id dengan status PER SHOWTIME.
     *
     * Status kursi ditentukan berdasarkan apakah seat_id sudah ada di tabel
     * `tickets` yang terhubung ke showtime ini (bukan dari kolom status global
     * di tabel seats).
     *
     * Dengan ini, kursi booked di Showtime A (Studio 1) TIDAK akan ikut
     * tampil booked di Showtime B (Studio 2).
     */
    public List<Seat> getSeatsByShowtime(int showtimeId) {
        List<Seat> seats = new ArrayList<>();
        try {
            /*
             * Penjelasan query:
             * - Ambil semua kursi milik studio yang terkait dengan showtime ini
             * - LEFT JOIN ke tabel `tickets` untuk cek apakah kursi sudah dibooking
             *   KHUSUS untuk showtime_id ini (bukan showtime lain)
             * - CASE WHEN: jika ada record di tickets dengan seat_id + showtime_id
             *   yang cocok → 'booked', jika tidak → 'available'
             */
            String query = """
                SELECT
                    s.id,
                    s.studio_id,
                    s.seats_row,
                    s.seats_number,
                    s.seats_code,
                    st.name_studio AS studio_name,
                    s.created_at,
                    s.updated_at,
                    CASE
                        WHEN tk.seat_id IS NOT NULL THEN 'booked'
                        ELSE 'available'
                    END AS status
                FROM seats s
                JOIN studios st ON s.studio_id = st.id
                JOIN showtimes sh ON st.id = sh.studio_id
                LEFT JOIN tickets tk ON tk.seat_id = s.id
                    AND tk.showtime_id = sh.id
                WHERE sh.id = ?
                GROUP BY
                    s.id, s.studio_id, s.seats_row, s.seats_number,
                    s.seats_code, st.name_studio, s.created_at, s.updated_at, tk.seat_id
                ORDER BY s.seats_row ASC, s.seats_number ASC
                """;

            ResultSet rs = DatabaseConnection.executeQuery(query, showtimeId);

            while (rs.next()) {
                Seat seat = new Seat();
                seat.setId(rs.getInt("id"));
                seat.setStudioId(rs.getInt("studio_id"));
                seat.setSeatsRow(rs.getString("seats_row").charAt(0));
                seat.setSeatsNumber(rs.getInt("seats_number"));
                seat.setSeatsCode(rs.getString("seats_code"));
                seat.setStatus(rs.getString("status")); // status per-showtime, bukan global
                seat.setCreatedAt(rs.getTimestamp("created_at"));
                seat.setUpdatedAt(rs.getTimestamp("updated_at"));
                seat.setStudioName(rs.getString("studio_name"));
                seats.add(seat);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seats;
    }

    public int getTotalSeats() {
        String query = "SELECT COUNT(*) AS total FROM seats";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query);
            if (rs.next()) {
                int total = rs.getInt("total");
                rs.close();
                return total;
            }
        } catch (SQLException e) {
            System.err.println("Error counting seats: " + e.getMessage());
        }
        return 0;
    }
}