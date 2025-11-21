package dao;

import models.Showtime;
import utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk Showtime
 */
public class ShowtimeDAO {

    /**
     * Tambah showtime baru
     */
    public int addShowtime(Showtime showtime) {
        String query = "INSERT INTO showtimes (movie_id, studio_id, date, start_time, end_start) " +
                       "VALUES (?, ?, ?, ?, ?)";
        try {
            return DatabaseConnection.executeUpdateWithGeneratedKey(query,
                    showtime.getMovieId(),
                    showtime.getStudioId(),
                    showtime.getDate(),
                    showtime.getStartTime(),
                    showtime.getEndTime()
            );
        } catch (SQLException e) {
            System.err.println("Error adding showtime: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Update showtime
     */
    public boolean updateShowtime(Showtime showtime) {
        String query = "UPDATE showtimes SET movie_id = ?, studio_id = ?, date = ?, start_time = ?, end_start = ? WHERE id = ?";
        try {
            int result = DatabaseConnection.executeUpdate(query,
                    showtime.getMovieId(),
                    showtime.getStudioId(),
                    showtime.getDate(),
                    showtime.getStartTime(),
                    showtime.getEndTime(),
                    showtime.getId()
            );
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error updating showtime: " + e.getMessage());
            return false;
        }
    }

    /**
     * Hapus showtime
     */
    public boolean deleteShowtime(int showtimeId) {
        String query = "DELETE FROM showtimes WHERE id = ?";
        try {
            int result = DatabaseConnection.executeUpdate(query, showtimeId);
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting showtime: " + e.getMessage());
            return false;
        }
    }

    /**
     * Ambil semua showtime dengan informasi film dan studio
     */
    public List<Showtime> getAllShowtimes() {
        List<Showtime> showtimes = new ArrayList<>();
        String query = "SELECT s.*, m.title as movie_title, st.name_studio as studio_name " +
                       "FROM showtimes s " +
                       "INNER JOIN movies m ON s.movie_id = m.id " +
                       "INNER JOIN studios st ON s.studio_id = st.id " +
                       "ORDER BY s.date DESC, s.start_time DESC";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query);
            while (rs.next()) {
                Showtime showtime = mapResultSetToShowtime(rs);
                showtimes.add(showtime);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error getting all showtimes: " + e.getMessage());
        }
        return showtimes;
    }

    /**
     * Ambil showtime berdasarkan ID
     */
    public Showtime getShowtimeById(int showtimeId) {
        String query = "SELECT s.*, m.title as movie_title, st.name_studio as studio_name " +
                       "FROM showtimes s " +
                       "INNER JOIN movies m ON s.movie_id = m.id " +
                       "INNER JOIN studios st ON s.studio_id = st.id " +
                       "WHERE s.id = ?";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query, showtimeId);
            if (rs.next()) {
                Showtime showtime = mapResultSetToShowtime(rs);
                rs.close();
                return showtime;
            }
        } catch (SQLException e) {
            System.err.println("Error getting showtime by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Helper method untuk mapping ResultSet ke Showtime
     */
    private Showtime mapResultSetToShowtime(ResultSet rs) throws SQLException {
        Showtime showtime = new Showtime();
        showtime.setId(rs.getInt("id"));
        showtime.setMovieId(rs.getInt("movie_id"));
        showtime.setStudioId(rs.getInt("studio_id"));
        showtime.setDate(rs.getDate("date"));
        showtime.setStartTime(rs.getTime("start_time"));
        showtime.setEndTime(rs.getTime("end_start")); // <-- Kolom di DB bernama 'end_start'
        showtime.setCreatedAt(rs.getTimestamp("created_at"));
        showtime.setUpdatedAt(rs.getTimestamp("updated_at"));
        showtime.setMovieTitle(rs.getString("movie_title"));
        showtime.setStudioName(rs.getString("studio_name"));
        return showtime;
    }

    /**
     * Ambil daftar movie untuk dropdown
     */
    public List<String> getMovieList() {
        List<String> movies = new ArrayList<>();
        String query = "SELECT id, title FROM movies ORDER BY title ASC";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query);
            while (rs.next()) {
                movies.add(rs.getInt("id") + " - " + rs.getString("title"));
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error getting movie list: " + e.getMessage());
        }
        return movies;
    }

    /**
     * Ambil daftar studio untuk dropdown
     */
    public List<String> getStudioList() {
        List<String> studios = new ArrayList<>();
        String query = "SELECT id, name_studio FROM studios ORDER BY name_studio ASC";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query);
            while (rs.next()) {
                studios.add(rs.getInt("id") + " - " + rs.getString("name_studio"));
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error getting studio list: " + e.getMessage());
        }
        return studios;
    }
    
    /**
 * Ambil semua showtime berdasarkan tanggal
 */
    public List<Showtime> getShowtimesByDate(java.sql.Date date) {
        List<Showtime> showtimes = new ArrayList<>();
        String query = """
            SELECT s.*, m.title as movie_title, m.poster_image, st.name_studio as studio_name
            FROM Showtimes s
            JOIN Movies m ON s.movie_id = m.id
            JOIN Studios st ON s.studio_id = st.id
            WHERE s.date = ?
            ORDER BY s.start_time ASC
            """;

        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(query)) {
            ps.setDate(1, date);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Showtime showtime = new Showtime();
                showtime.setId(rs.getInt("id"));
                showtime.setMovieId(rs.getInt("movie_id"));
                showtime.setStudioId(rs.getInt("studio_id"));
                showtime.setDate(rs.getDate("date"));
                showtime.setStartTime(rs.getTime("start_time"));
                showtime.setEndTime(rs.getTime("end_start")); // sesuai nama kolom di DB Anda
                showtime.setCreatedAt(rs.getTimestamp("created_at"));
                showtime.setUpdatedAt(rs.getTimestamp("updated_at"));
                showtime.setMovieTitle(rs.getString("movie_title"));
                showtime.setPosterImage(rs.getBytes("poster_image"));
                showtime.setStudioName(rs.getString("studio_name"));
                showtimes.add(showtime);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching showtimes by date: " + e.getMessage());
        }
        return showtimes;
    }

    /**
     * Ekstrak ID dari string combo (format: "ID - Title")
     */
    public static int extractIdFromCombo(String selected) {
        if (selected != null && selected.contains(" - ")) {
            return Integer.parseInt(selected.split(" - ")[0]);
        }
        return 0;
    }
    
    public int getTotalShowtimes() {
        String query = "SELECT COUNT(*) AS total FROM showtimes";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query);
            if (rs.next()) {
                int total = rs.getInt("total");
                rs.close();
                return total;
            }
        } catch (SQLException e) {
            System.err.println("Error counting showtimes: " + e.getMessage());
        }
        return 0;
    }
}