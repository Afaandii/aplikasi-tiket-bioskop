package dao;

import models.User;
import utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk User
 * Mengelola operasi CRUD untuk tabel User
 */
public class UserDAO {
    
    /**
     * Registrasi user baru
     * @param user objek User yang akan disimpan
     * @return true jika berhasil, false jika gagal
     */
    public boolean registerUser(User user) {
        // Gunakan role_id, bukan role
        String query = "INSERT INTO User (username, email, password, role_id) VALUES (?, ?, ?, ?)";
        try {
            int result = DatabaseConnection.executeUpdate(query, 
                user.getUsername(), 
                user.getEmail(), 
                user.getPassword(), 
                user.getRoleId() // Gunakan getRoleId()
            );
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Login user dengan email dan password
     * @param email email user
     * @param password password user
     * @return User object jika berhasil, null jika gagal
     */
    public User loginUserByEmail(String email, String password) {
        // Gunakan JOIN untuk mendapatkan nama role
        String query = "SELECT u.*, r.role_name FROM User u JOIN roles r ON u.role_id = r.id WHERE u.email = ? AND u.password = ?";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query, email, password);
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setRoleId(rs.getInt("role_id")); // Simpan role_id
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setUpdatedAt(rs.getTimestamp("updated_at"));
                rs.close();
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Login user (method lama - tetap dipertahankan untuk compatibility)
     * @param username username atau email
     * @param password password
     * @return User object jika berhasil, null jika gagal
     */
    public User loginUser(String username, String password) {
        // Gunakan JOIN untuk mendapatkan nama role
        String query = "SELECT u.*, r.role_name FROM User u JOIN roles r ON u.role_id = r.id WHERE (u.username = ? OR u.email = ?) AND u.password = ?";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query, username, username, password);
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setRoleId(rs.getInt("role_id")); // Simpan role_id
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setUpdatedAt(rs.getTimestamp("updated_at"));
                rs.close();
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Update profil user
     * @param user objek User dengan data baru
     * @return true jika berhasil
     */
    public boolean updateProfile(User user) {
        String query = "UPDATE User SET username = ?, email = ? WHERE id = ?";
        try {
            int result = DatabaseConnection.executeUpdate(query, 
                user.getUsername(), 
                user.getEmail(), 
                user.getId()
            );
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error updating profile: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update password user
     * @param userId ID user
     * @param newPassword password baru
     * @return true jika berhasil
     */
    public boolean updatePassword(int userId, String newPassword) {
        String query = "UPDATE User SET password = ? WHERE id = ?";
        try {
            int result = DatabaseConnection.executeUpdate(query, newPassword, userId);
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Hapus akun user
     * @param userId ID user yang akan dihapus
     * @return true jika berhasil
     */
    public boolean deleteAccount(int userId) {
        String query = "DELETE FROM User WHERE id = ?";
        try {
            int result = DatabaseConnection.executeUpdate(query, userId);
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting account: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Cek apakah username sudah ada
     * @param username username yang akan dicek
     * @return true jika sudah ada
     */
    public boolean isUsernameExists(String username) {
        String query = "SELECT COUNT(*) FROM User WHERE username = ?";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query, username);
            if (rs.next()) {
                boolean exists = rs.getInt(1) > 0;
                rs.close();
                return exists;
            }
        } catch (SQLException e) {
            System.err.println("Error checking username: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Cek apakah email sudah ada
     * @param email email yang akan dicek
     * @return true jika sudah ada
     */
    public boolean isEmailExists(String email) {
        String query = "SELECT COUNT(*) FROM User WHERE email = ?";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query, email);
            if (rs.next()) {
                boolean exists = rs.getInt(1) > 0;
                rs.close();
                return exists;
            }
        } catch (SQLException e) {
            System.err.println("Error checking email: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Ambil semua user (untuk admin)
     * @return List of User
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        // Gunakan JOIN untuk mendapatkan nama role
        String query = "SELECT u.*, r.role_name FROM User u JOIN roles r ON u.role_id = r.id ORDER BY u.created_at DESC";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query);
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setRoleId(rs.getInt("role_id")); // Simpan role_id
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setUpdatedAt(rs.getTimestamp("updated_at"));
                users.add(user);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
        }
        return users;
    }
    
    /**
     * Ambil user berdasarkan ID
     * @param userId ID user
     * @return User object atau null jika tidak ditemukan
     */
    public User getUserById(int userId) {
        // Gunakan JOIN untuk mendapatkan nama role
        String query = "SELECT u.*, r.role_name FROM User u JOIN roles r ON u.role_id = r.id WHERE u.id = ?";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query, userId);
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setRoleId(rs.getInt("role_id")); // Simpan role_id
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setUpdatedAt(rs.getTimestamp("updated_at"));
                rs.close();
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
        }
        return null;
    }
    
    // Tambahkan di UserDAO.java
public String getRoleNameByRoleId(int roleId) {
    String query = "SELECT role_name FROM roles WHERE id = ?";
    try {
        ResultSet rs = DatabaseConnection.executeQuery(query, roleId);
        if (rs.next()) {
            String roleName = rs.getString("role_name");
            rs.close();
            return roleName;
        }
    } catch (SQLException e) {
        System.err.println("Error getting role name: " + e.getMessage());
    }
    return "unknown"; // Default jika tidak ditemukan
}

    public int getTotalUsers() {
        String query = "SELECT COUNT(*) AS total FROM user";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query);
            if (rs.next()) {
                int total = rs.getInt("total");
                rs.close();
                return total;
            }
        } catch (SQLException e) {
            System.err.println("Error counting users: " + e.getMessage());
        }
        return 0;
    }
}