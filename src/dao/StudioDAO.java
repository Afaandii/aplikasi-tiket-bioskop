package dao;

import utils.DatabaseConnection;
import models.Studio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudioDAO {

    // CREATE - Tambah studio baru
    public boolean addStudio(Studio studio) {
        String query = "INSERT INTO studios (name_studio, capacity, description, created_at) " +
                       "VALUES (?, ?, ?, NOW())";
        try {
            int rows = DatabaseConnection.executeUpdate(query,
                    studio.getNameStudio(),
                    studio.getCapacity(),
                    studio.getDescription()
            );
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error adding studio: " + e.getMessage());
            return false;
        }
    }

    // READ - Ambil semua studio
    public List<Studio> getAllStudios() {
        List<Studio> studios = new ArrayList<>();
        String query = "SELECT * FROM studios";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query);
            while (rs.next()) {
                Studio studio = mapResultSetToStudio(rs);
                studios.add(studio);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error getting studios: " + e.getMessage());
        }
        return studios;
    }

    // READ - Ambil studio by ID
    public Studio getStudioById(int id) {
        String query = "SELECT * FROM studios WHERE id = ?";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query, id);
            if (rs.next()) {
                Studio studio = mapResultSetToStudio(rs);
                rs.close();
                return studio;
            }
        } catch (SQLException e) {
            System.err.println("Error getting studio by ID: " + e.getMessage());
        }
        return null;
    }

    // UPDATE - Update studio
    public boolean updateStudio(Studio studio) {
        String query = "UPDATE studios SET name_studio=?, capacity=?, description=? WHERE id=?";
        try {
            int rows = DatabaseConnection.executeUpdate(query,
                    studio.getNameStudio(),
                    studio.getCapacity(),
                    studio.getDescription(),
                    studio.getId()
            );
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating studio: " + e.getMessage());
            return false;
        }
    }

    // DELETE - Hapus studio
    public boolean deleteStudio(int id) {
        String query = "DELETE FROM studios WHERE id=?";
        try {
            int rows = DatabaseConnection.executeUpdate(query, id);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting studio: " + e.getMessage());
            return false;
        }
    }

    // Helper untuk mapping ResultSet ke object
    private Studio mapResultSetToStudio(ResultSet rs) throws SQLException {
        Studio studio = new Studio();
        studio.setId(rs.getInt("id"));
        studio.setNameStudio(rs.getString("name_studio"));
        studio.setCapacity(rs.getInt("capacity"));
        studio.setDescription(rs.getString("description"));
        studio.setCreatedAt(rs.getTimestamp("created_at"));
        return studio;
    }
    
    /**
    * Menghitung jumlah total studio di database
    * @return jumlah studio
    */
   public int getTotalStudios() {
       String query = "SELECT COUNT(*) AS total FROM studios";
       try {
           ResultSet rs = DatabaseConnection.executeQuery(query);
           if (rs.next()) {
               int total = rs.getInt("total");
               rs.close();
               return total;
           }
       } catch (SQLException e) {
           System.err.println("Error counting studios: " + e.getMessage());
       }
       return 0; // default jika error
   }
}