package models;

import java.sql.Timestamp;

public class Studio {
    private int id;
    private String nameStudio;
    private int capacity;
    private String description;
    private Timestamp createdAt;

    public Studio() {}

    public Studio(String nameStudio, int capacity, String description) {
        this.nameStudio = nameStudio;
        this.capacity = capacity;
        this.description = description;
    }

    @Override
    public String toString() {
        return nameStudio + " - Capacity: " + capacity;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getNameStudio() { return nameStudio; }
    public void setNameStudio(String nameStudio) { this.nameStudio = nameStudio; }
    
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}