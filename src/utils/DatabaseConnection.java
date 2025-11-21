package utils;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database Connection Helper Class
 */
public class DatabaseConnection {
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/db_aplikasi_penjualan_tiket_bioskop";
    private static final String DATABASE_USERNAME = "root";
    private static final String DATABASE_PASSWORD = ""; 
    private static final String DATABASE_DRIVER = "com.mysql.cj.jdbc.Driver";
    
    private static Connection connection;
    
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName(DATABASE_DRIVER);
                connection = DriverManager.getConnection(
                    DATABASE_URL, 
                    DATABASE_USERNAME, 
                    DATABASE_PASSWORD
                );
                System.out.println("Database connected successfully!");
            }
        } catch (ClassNotFoundException e) {
            Logger.getLogger(DatabaseConnection.class.getName())
                  .log(Level.SEVERE, "MySQL Driver not found", e);
        } catch (SQLException e) {
            Logger.getLogger(DatabaseConnection.class.getName())
                  .log(Level.SEVERE, "Failed to connect to database", e);
        }
        return connection;
    }
    
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            Logger.getLogger(DatabaseConnection.class.getName())
                  .log(Level.SEVERE, "Failed to close database connection", e);
        }
    }
    
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    public static ResultSet executeQuery(String query, Object... params) throws SQLException {
        PreparedStatement pstmt = getConnection().prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        return pstmt.executeQuery(); // stmt harus ditutup oleh pemanggil
    }
    
    public static int executeUpdate(String query, Object... params) throws SQLException {
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            return pstmt.executeUpdate();
        }
    }
    
    public static int executeUpdateWithGeneratedKey(String query, Object... params) throws SQLException {
        try (PreparedStatement pstmt = getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0; 
    }

}
