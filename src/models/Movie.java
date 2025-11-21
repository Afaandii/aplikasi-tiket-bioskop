package models;

import java.sql.Time;
import java.sql.Date;
import java.sql.Timestamp;

public class Movie {
    private int id;
    private String title;
    private String sinopsis;
    private String genre;
    private Time duration;
    private String director;
    private Date releasedDate;
    private String ratingUsia;
    private String language;
    private byte[] posterImage;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Movie() {}

    public Movie(String title, String sinopsis, String genre, Time duration,
                 String director, Date releasedDate, String ratingUsia, String language) {
        this.title = title;
        this.sinopsis = sinopsis;
        this.genre = genre;
        this.duration = duration;
        this.director = director;
        this.releasedDate = releasedDate;
        this.ratingUsia = ratingUsia;
        this.language = language;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getSinopsis() { return sinopsis; }
    public void setSinopsis(String sinopsis) { this.sinopsis = sinopsis; }
    
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    
    public Time getDuration() { return duration; }
    public void setDuration(Time duration) { this.duration = duration; }
    
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    
    public Date getReleasedDate() { return releasedDate; }
    public void setReleasedDate(Date releasedDate) { this.releasedDate = releasedDate; }
    
    public String getRatingUsia() { return ratingUsia; }
    public void setRatingUsia(String ratingUsia) { this.ratingUsia = ratingUsia; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public byte[] getPosterImage() { return posterImage; }
    public void setPosterImage(byte[] posterImage) { this.posterImage = posterImage; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    

    @Override
    public String toString() {
        return title; // tampil di combobox
    }
}
