package gui;

import dao.ShowtimeDAO;
import dao.TicketPriceDAO;
import javax.swing.*;
import models.Showtime;

public class CinemaApp extends JFrame {

    public CinemaApp() {
        setTitle("Aplikasi Penjualan Tiket Bioskop");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
  
    // User yang login
    private models.User loggedInUser;
    public void setLoggedInUser(models.User user) {
        this.loggedInUser = user;
    }
    public models.User getLoggedInUser() {
        return loggedInUser;
    }

    // Film yang dipilih
    private Integer selectedMovieId;
    public void setSelectedMovieId(Integer movieId) {
        this.selectedMovieId = movieId;
    }
    public Integer getSelectedMovieId() {
        return selectedMovieId;
    }

    // Showtime yang dipilih
    private models.Showtime selectedShowtime;
    public void setSelectedShowtime(models.Showtime showtime) {
        this.selectedShowtime = showtime;
    }
    public models.Showtime getSelectedShowtime() {
        return selectedShowtime;
    }
    
    // Kursi yang dipilih
    private java.util.List<models.Seat> selectedSeats;
    public void setSelectedSeats(java.util.List<models.Seat> seats) {
        this.selectedSeats = seats;
    }
    public java.util.List<models.Seat> getSelectedSeats() {
        return selectedSeats;
    }
    
    private models.Transaction lastTransaction;
    public void setLastTransaction(models.Transaction t){ this.lastTransaction = t; }
    public models.Transaction getLastTransaction(){ return lastTransaction; }


    public void showPage(JPanel page) {
        setContentPane(page);
        revalidate();
        repaint();
    }
    
    public void showApplication() {
        setVisible(true);
        showPage(new LoginPage(this));
    }
    
    // Tambahkan di bawah setSelectedShowtime(Showtime)
    public void setSelectedShowtimeId(int showtimeId) {
        ShowtimeDAO showtimeDAO = new ShowtimeDAO();
        Showtime showtime = showtimeDAO.getShowtimeById(showtimeId);
        if (showtime != null) {
            this.selectedShowtime = showtime;
        } else {
            System.err.println("Showtime dengan ID " + showtimeId + " tidak ditemukan.");
        }
    }
    
    private TicketPriceDAO ticketPriceDAO;
    public TicketPriceDAO getTicketPriceDAO() {
        if (this.ticketPriceDAO == null) {
            this.ticketPriceDAO = new TicketPriceDAO();
        }
        return this.ticketPriceDAO;
    }
}
