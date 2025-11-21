package models;

import java.sql.Timestamp;

/**
 * Model untuk tabel Seats
 */
public class Seat {
    private int id;
    private int studioId; // Ganti showtimeId menjadi studioId
    private char seatsRow;
    private int seatsNumber;
    private String seatsCode;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Additional fields for display (from JOIN)
    private String studioName;   // e.g., "CGV Senayan"

    // Constructors
    public Seat() {}

    public Seat(int studioId, char seatsRow, int seatsNumber, String seatsCode, String status) {
        this.studioId = studioId;
        this.seatsRow = seatsRow;
        this.seatsNumber = seatsNumber;
        this.seatsCode = seatsCode;
        this.status = status;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudioId() { return studioId; } // Ganti nama method
    public void setStudioId(int studioId) { this.studioId = studioId; } // Ganti nama method

    public char getSeatsRow() { return seatsRow; }
    public void setSeatsRow(char seatsRow) { this.seatsRow = seatsRow; }

    public int getSeatsNumber() { return seatsNumber; }
    public void setSeatsNumber(int seatsNumber) { this.seatsNumber = seatsNumber; }

    public String getSeatsCode() { return seatsCode; }
    public void setSeatsCode(String seatsCode) { this.seatsCode = seatsCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getStudioName() { return studioName; }
    public void setStudioName(String studioName) { this.studioName = studioName; }

    // Helper method untuk mendapatkan label kursi
    public String getSeatLabel() {
        return seatsRow + String.valueOf(seatsNumber);
    }
}