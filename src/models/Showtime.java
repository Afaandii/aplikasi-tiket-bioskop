package models;

import java.sql.Timestamp;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDateTime;

/**
 * Model untuk entitas Showtime
 */
public class Showtime {
    private int id;
    private int movieId;
    private int studioId;
    private Date date;
    private Time startTime;
    private Time endTime;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private byte[] posterImage; // <-- pastikan ini ada

    // Field tambahan untuk menampilkan nama di GUI
    private String movieTitle;
    private String studioName;

    public Showtime() {}

    public Showtime(int movieId, int studioId, Date date, Time startTime, Time endTime) {
        this.movieId = movieId;
        this.studioId = studioId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }

    public int getStudioId() { return studioId; }
    public void setStudioId(int studioId) { this.studioId = studioId; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public Time getStartTime() { return startTime; }
    public void setStartTime(Time startTime) { this.startTime = startTime; }

    public Time getEndTime() { return endTime; }
    public void setEndTime(Time endTime) { this.endTime = endTime; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    public String getStudioName() { return studioName; }
    public void setStudioName(String studioName) { this.studioName = studioName; }

    /**
     * Kembalikan LocalDateTime gabungan dari date + start_time.
     */
    public LocalDateTime getStartDatetime() {
        if (this.date == null || this.startTime == null) {
            return null;
        }
        return LocalDateTime.of(this.date.toLocalDate(), this.startTime.toLocalTime());
    }

    /**
     * Kembalikan LocalDateTime gabungan dari date + end_time.
     */
    public LocalDateTime getEndDatetime() {
        if (this.date == null || this.endTime == null) {
            return null;
        }
        return LocalDateTime.of(this.date.toLocalDate(), this.endTime.toLocalTime());
    }
    
    public void setPosterImage(byte[] posterImage) {
        this.posterImage = posterImage;
    }

    @Override
    public String toString() {
        return "Showtime{" +
                "id=" + id +
                ", movieId=" + movieId +
                ", studioId=" + studioId +
                ", date=" + date +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}