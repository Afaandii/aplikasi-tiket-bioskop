package models;

import java.sql.Timestamp;
import java.sql.Date;

/**
 * Model untuk entitas TicketPrice
 */
public class TicketPrice {
    private int id;
    private int showtimeId;
    private String days;
    private double price;
    private Date date;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Field tambahan untuk menampilkan nama showtime di GUI
    private String showtimeInfo; // Misal: "Movie - Studio - Date Time"

    public TicketPrice() {}

    public TicketPrice(int showtimeId, String days, double price, Date date) {
        this.showtimeId = showtimeId;
        this.days = days;
        this.price = price;
        this.date = date;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getShowtimeId() { return showtimeId; }
    public void setShowtimeId(int showtimeId) { this.showtimeId = showtimeId; }

    public String getDays() { return days; }
    public void setDays(String days) { this.days = days; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getShowtimeInfo() { return showtimeInfo; }
    public void setShowtimeInfo(String showtimeInfo) { this.showtimeInfo = showtimeInfo; }

    @Override
    public String toString() {
        return "TicketPrice{" +
                "id=" + id +
                ", showtimeId=" + showtimeId +
                ", days='" + days + '\'' +
                ", price=" + price +
                ", date=" + date +
                '}';
    }
}