/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;
import java.sql.Timestamp;

/**
 *
 * @author HP
 */
public class SeatType {
    private int id;
    private String type;
    private String deskripsi;
    private int price;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Constructors
    public SeatType() {}
    
    public SeatType(String type, String deskripsi, int price) {
        this.type = type;
        this.deskripsi = deskripsi;
        this.price = price;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public String toString() {
        return type + " (Rp " + String.format("%,d", price) + ")";
    }
}
