package dao;

import utils.DatabaseConnection;
import models.SeatType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SeatTypeDAO {

    // Ambil semua seat types
    public List<SeatType> getAllSeatTypes() {
        List<SeatType> seatTypes = new ArrayList<>();
        String query = "SELECT * FROM Seats_type";

        try {
            ResultSet rs = DatabaseConnection.executeQuery(query);
            while (rs.next()) {
                SeatType seatType = new SeatType();
                seatType.setId(rs.getInt("id"));
                seatType.setType(rs.getString("type"));
                seatType.setDeskripsi(rs.getString("deskripsi"));
                seatType.setPrice(rs.getInt("price"));
                seatType.setCreatedAt(rs.getTimestamp("created_at"));
                seatType.setUpdatedAt(rs.getTimestamp("updated_at"));
                seatTypes.add(seatType);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error getting seat types: " + e.getMessage());
        }
        return seatTypes;
    }

    // Ambil seat type by ID
    public SeatType getSeatTypeById(int id) {
        String query = "SELECT * FROM Seats_type WHERE id = ?";
        try {
            ResultSet rs = DatabaseConnection.executeQuery(query, id);
            if (rs.next()) {
                SeatType seatType = new SeatType();
                seatType.setId(rs.getInt("id"));
                seatType.setType(rs.getString("type"));
                seatType.setDeskripsi(rs.getString("deskripsi"));
                seatType.setPrice(rs.getInt("price"));
                seatType.setCreatedAt(rs.getTimestamp("created_at"));
                seatType.setUpdatedAt(rs.getTimestamp("updated_at"));
                rs.close();
                return seatType;
            }
        } catch (SQLException e) {
            System.err.println("Error getting seat type by ID: " + e.getMessage());
        }
        return null;
    }
}
