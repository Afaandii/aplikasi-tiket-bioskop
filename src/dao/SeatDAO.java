package dao;

import utils.DatabaseConnection;
import models.Seat;

import java.sql.Connection;
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
                seat.setStudioId(rs.getInt("studio_id")); // Ganti showtimeId menjadi studioId
                seat.setSeatsRow(rs.getString("seats_row").charAt(0));
                seat.setSeatsNumber(rs.getInt("seats_number"));
                seat.setSeatsCode(rs.getString("seats_code"));
                seat.setStatus(rs.getString("status"));
                seat.setCreatedAt(rs.getTimestamp("created_at"));
                seat.setUpdatedAt(rs.getTimestamp("updated_at"));

                // Ambil nama studio dari hasil JOIN
                String studioName = rs.getString("studio_name");
                seat.setStudioName(studioName);

                seats.add(seat);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seats;
    }

    /**
     * Ambil semua kursi (untuk ditampilkan di tabel)
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
                seat.setStudioId(rs.getInt("studio_id")); // Ganti showtimeId menjadi studioId
                seat.setSeatsRow(rs.getString("seats_row").charAt(0));
                seat.setSeatsNumber(rs.getInt("seats_number"));
                seat.setSeatsCode(rs.getString("seats_code"));
                seat.setStatus(rs.getString("status"));
                seat.setCreatedAt(rs.getTimestamp("created_at"));
                seat.setUpdatedAt(rs.getTimestamp("updated_at"));

                // Ambil nama studio dari hasil JOIN
                String studioName = rs.getString("studio_name");
                seat.setStudioName(studioName);

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
                seat.setStudioId(rs.getInt("studio_id")); // Ganti showtimeId menjadi studioId
                seat.setSeatsRow(rs.getString("seats_row").charAt(0));
                seat.setSeatsNumber(rs.getInt("seats_number"));
                seat.setSeatsCode(rs.getString("seats_code"));
                seat.setStatus(rs.getString("status"));
                seat.setCreatedAt(rs.getTimestamp("created_at"));
                seat.setUpdatedAt(rs.getTimestamp("updated_at"));

                // Ambil nama studio dari hasil JOIN
                String studioName = rs.getString("studio_name");
                seat.setStudioName(studioName);
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
                    seat.getStudioId(), // Ganti showtimeId menjadi studioId
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
                    seat.getStudioId(), // Ganti showtimeId menjadi studioId
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
            // Ambil kapasitas studio
            String studioQuery = "SELECT capacity FROM studios WHERE id = ?";
            ResultSet rs = DatabaseConnection.executeQuery(studioQuery, studioId);

            if (rs.next()) {
                int totalSeats = rs.getInt("capacity");
                // Asumsikan 10 baris, hitung jumlah kursi per baris
                int rows = 10; // Bisa diatur atau dihitung dinamis
                int seatsPerRow = (int) Math.ceil((double) totalSeats / rows);

                for (int i = 0; i < rows; i++) {
                    char row = (char) ('A' + i); // Baris A, B, C, ...
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
    * Ambil semua kursi berdasarkan showtime_id (melalui studio_id)
    */
   public List<Seat> getSeatsByShowtime(int showtimeId) {
       List<Seat> seats = new ArrayList<>();
       try {
           String query = """
               SELECT s.*, st.name_studio AS studio_name
               FROM Seats s
               JOIN studios st ON s.studio_id = st.id
               JOIN showtimes sh ON st.id = sh.studio_id
               WHERE sh.id = ?
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