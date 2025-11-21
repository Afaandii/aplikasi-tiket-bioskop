/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.List;

/**
 *
 * @author HP
 */
public class Transaction {
    private int id;
    private int userId;
    private int showtimeId;
    private String transactionCode;
    private int totalPrice;
    private String status;
    private Timestamp createdAt;
    private int amountTicket;
    
    // Additional fields for join (dari SQL join)
    private String username;
    private String movieTitle;
    private Date showDate;
    private Time showTime;
    private String cinemaName; 
    private double ticketPrice;
    
    // --- Relasi tambahan ---
    private Showtime showtime;       // relasi ke showtime
    private List<Seat> seats;        // relasi ke seat (bisa banyak)
    private String paymentMethod;    // relasi ke metode pembayaran
    
    // Constructors
    public Transaction() {}
    
    public Transaction(int userId, int showtimeId, String transactionCode, int totalPrice) {
        this.userId = userId;
        this.showtimeId = showtimeId;
        this.transactionCode = transactionCode;
        this.totalPrice = totalPrice;
        this.status = "pending";
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public int getShowtimeId() { return showtimeId; }
    public void setShowtimeId(int showtimeId) { this.showtimeId = showtimeId; }
    
    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(String transactionCode) { this.transactionCode = transactionCode; }
    
    public int getTotalPrice() { return totalPrice; }
    public void setTotalPrice(int totalPrice) { this.totalPrice = totalPrice; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    
    public Date getShowDate() { return showDate; }
    public void setShowDate(Date showDate) { this.showDate = showDate; }
    
    public Time getShowTime() { return showTime; }
    public void setShowTime(Time showTime) { this.showTime = showTime; }
    
    // --- Getter Setter Relasi ---
    public Showtime getShowtime() { return showtime; }
    public void setShowtime(Showtime showtime) { this.showtime = showtime; }

    public List<Seat> getSeats() { return seats; }
    public void setSeats(List<Seat> seats) { this.seats = seats; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getCinemaName() { return cinemaName; }
    public void setCinemaName(String cinemaName) { this.cinemaName = cinemaName;}
    
    // Getter & Setter
    public int getAmountTicket() {
        return amountTicket;
    }

    public void setAmountTicket(int amountTicket) {
        this.amountTicket = amountTicket;
    }
    
    public double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }
}
