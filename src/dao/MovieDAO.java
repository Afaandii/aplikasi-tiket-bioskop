package dao;

import models.Movie;
import utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk Movie
 * Mengelola operasi CRUD untuk tabel Movies
 */
public class MovieDAO {
    
    /**
     * Tambah film baru
     * @param movie objek Movie yang akan disimpan
     * @return ID film yang baru dibuat, 0 jika gagal
     */
    public int addMovie(Movie movie) {
        String query = "INSERT INTO Movies (title, sinopsis, genre, duration, director, " +
                      "released_date, rating_usia, language, poster_image) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            return DatabaseConnection.executeUpdateWithGeneratedKey(query,
                movie.getTitle(),
                movie.getSinopsis(),
                movie.getGenre(),
                movie.getDuration(),
                movie.getDirector(),
                movie.getReleasedDate(),
                movie.getRatingUsia(),
                movie.getLanguage(),
                movie.getPosterImage()
            );
        } catch (SQLException e) {
            System.err.println("Error adding movie: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Update film
     * @param movie objek Movie dengan data baru
     * @return true jika berhasil
     */
    public boolean updateMovie(Movie movie) {
        String query = "UPDATE Movies SET title = ?, sinopsis = ?, genre = ?, duration = ?, " +
                      "director = ?, released_date = ?, rating_usia = ?, language = ?, " +
                      "poster_image = ? WHERE id = ?";
        try {
            int result = DatabaseConnection.executeUpdate(query,
                movie.getTitle(),
                movie.getSinopsis(),
                movie.getGenre(),
                movie.getDuration(),
                movie.getDirector(),
                movie.getReleasedDate(),
                movie.getRatingUsia(),
                movie.getLanguage(),
                movie.getPosterImage(),
                movie.getId()
            );
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error updating movie: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Hapus film
     * @param movieId ID film yang akan dihapus
     * @return true jika berhasil
     */
    public boolean deleteMovie(int movieId) {
        String query = "DELETE FROM Movies WHERE id = ?";
        try {
            int result = DatabaseConnection.executeUpdate(query, movieId);
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting movie: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Ambil semua film
     * @return List of Movie
     */
    public List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();
        String query = "SELECT * FROM Movies ORDER BY created_at DESC";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query);
            while (rs.next()) {
                Movie movie = mapResultSetToMovie(rs);
                movies.add(movie);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error getting all movies: " + e.getMessage());
        }
        return movies;
    }
    
    /**
     * Ambil film berdasarkan ID
     * @param movieId ID film
     * @return Movie object atau null jika tidak ditemukan
     */
    public Movie getMovieById(int movieId) {
        String query = "SELECT * FROM Movies WHERE id = ?";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query, movieId);
            if (rs.next()) {
                Movie movie = mapResultSetToMovie(rs);
                rs.close();
                return movie;
            }
        } catch (SQLException e) {
            System.err.println("Error getting movie by ID: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Cari film berdasarkan judul
     * @param title judul film (bisa sebagian)
     * @return List of Movie
     */
    public List<Movie> searchMoviesByTitle(String title) {
        List<Movie> movies = new ArrayList<>();
        String query = "SELECT * FROM Movies WHERE title LIKE ? ORDER BY title";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query, "%" + title + "%");
            while (rs.next()) {
                Movie movie = mapResultSetToMovie(rs);
                movies.add(movie);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error searching movies: " + e.getMessage());
        }
        return movies;
    }
    
    /**
     * Ambil film berdasarkan genre
     * @param genre genre film
     * @return List of Movie
     */
    public List<Movie> getMoviesByGenre(String genre) {
        List<Movie> movies = new ArrayList<>();
        String query = "SELECT * FROM Movies WHERE genre LIKE ? ORDER BY title";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query, "%" + genre + "%");
            while (rs.next()) {
                Movie movie = mapResultSetToMovie(rs);
                movies.add(movie);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error getting movies by genre: " + e.getMessage());
        }
        return movies;
    }
    
    /**
     * Cek apakah film sedang digunakan dalam showtime
     * @param movieId ID film
     * @return true jika sedang digunakan
     */
    public boolean isMovieInUse(int movieId) {
        String query = "SELECT COUNT(*) FROM Showtimes WHERE movie_id = ?";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query, movieId);
            if (rs.next()) {
                boolean inUse = rs.getInt(1) > 0;
                rs.close();
                return inUse;
            }
        } catch (SQLException e) {
            System.err.println("Error checking movie usage: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Helper method untuk mapping ResultSet ke Movie object
     */
    private Movie mapResultSetToMovie(ResultSet rs) throws SQLException {
        Movie movie = new Movie();
        movie.setId(rs.getInt("id"));
        movie.setTitle(rs.getString("title"));
        movie.setSinopsis(rs.getString("sinopsis"));
        movie.setGenre(rs.getString("genre"));
        movie.setDuration(rs.getTime("duration"));
        movie.setDirector(rs.getString("director"));
        movie.setReleasedDate(rs.getDate("released_date"));
        movie.setRatingUsia(rs.getString("rating_usia"));
        movie.setLanguage(rs.getString("language"));
        movie.setPosterImage(rs.getBytes("poster_image"));
        movie.setCreatedAt(rs.getTimestamp("created_at"));
        movie.setUpdatedAt(rs.getTimestamp("updated_at"));
        return movie;
    }
    
    /**
     * Ambil semua genre yang ada
     * @return List of unique genres
     */
    public List<String> getAllGenres() {
        List<String> genres = new ArrayList<>();
        String query = "SELECT DISTINCT genre FROM Movies WHERE genre IS NOT NULL ORDER BY genre";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query);
            while (rs.next()) {
                String genre = rs.getString("genre");
                if (genre != null && !genre.trim().isEmpty()) {
                    // Split genre jika ada multiple genre (separated by comma)
                    String[] genreArray = genre.split(",");
                    for (String g : genreArray) {
                        String trimmedGenre = g.trim();
                        if (!genres.contains(trimmedGenre)) {
                            genres.add(trimmedGenre);
                        }
                    }
                }
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error getting all genres: " + e.getMessage());
        }
        return genres;
    }
    
    /**
     * Ambil film yang sedang tayang (ada showtime hari ini atau ke depan)
     * @return List of Movie
     */
    public List<Movie> getCurrentlyShowingMovies() {
        List<Movie> movies = new ArrayList<>();
        String query = "SELECT DISTINCT m.* FROM Movies m " +
                      "INNER JOIN Showtimes s ON m.id = s.movie_id " +
                      "WHERE s.date >= CURDATE() " +
                      "ORDER BY m.title";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query);
            while (rs.next()) {
                Movie movie = mapResultSetToMovie(rs);
                movies.add(movie);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error getting currently showing movies: " + e.getMessage());
        }
        return movies;
    }
    
    public int getTotalMovies() {
        String query = "SELECT COUNT(*) AS total FROM movies";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query);
            if (rs.next()) {
                int total = rs.getInt("total");
                rs.close();
                return total;
            }
        } catch (SQLException e) {
            System.err.println("Error counting movies: " + e.getMessage());
        }
        return 0;
    }
    
}