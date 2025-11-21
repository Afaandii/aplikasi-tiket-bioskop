package dao;

import utils.DatabaseConnection;
import models.Ticket;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TicketDAO {

    public boolean createTicket(Ticket ticket) {
        String query = "INSERT INTO tickets (seat_id, transaction_id, price, manual_code, created_at) " +
               "VALUES (?, ?, ?, ?, NOW())";
        try {
            int result = DatabaseConnection.executeUpdate(query,
                    ticket.getSeatId(),
                    ticket.getTransactionId(),
                    ticket.getPrice(),
                    ticket.getTicketCode()
            );
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Ticket> getTicketsByTransaction(int transactionId) {
        List<Ticket> tickets = new ArrayList<>();
        String query = """
            SELECT 
                t.id AS ticket_id,
                t.transaction_id,
                t.seat_id,
                t.price,
                t.created_at,

                tr.transaction_code,
                m.title AS movie_title,
                c.name_cinema,
                s.date AS show_date,
                s.broadcast AS show_time,
                se.seats_code AS seat_label
            FROM tickets t
            INNER JOIN transaction tr ON t.transaction_id = tr.id
            INNER JOIN showtimes s ON tr.showtime_id = s.id
            INNER JOIN movies m ON s.movie_id = m.id
            INNER JOIN studios st ON s.studio_id = st.id
            INNER JOIN cinema c ON st.cinema_id = c.id
            INNER JOIN seats se ON t.seat_id = se.id
            WHERE t.transaction_id = ?
        """;

        try {
            ResultSet rs = DatabaseConnection.executeQuery(query, transactionId);
            while (rs.next()) {
                Ticket ticket = new Ticket();
                ticket.setId(rs.getInt("ticket_id"));
                ticket.setTransactionId(rs.getInt("transaction_id"));
                ticket.setSeatId(rs.getInt("seat_id"));
                ticket.setPrice(rs.getInt("price"));
                ticket.setCreatedAt(rs.getTimestamp("created_at"));

                // Ambil dari tabel transactions
                ticket.setTicketCode(rs.getString("transaction_code"));

                // Tambahan biar tidak null
                ticket.setMovieTitle(rs.getString("movie_title"));
                ticket.setCinemaName(rs.getString("name_cinema"));
                ticket.setShowDate(rs.getDate("show_date"));
                ticket.setShowTime(rs.getTime("show_time"));
                ticket.setSeatLabel(rs.getString("seat_label"));

                tickets.add(ticket);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }
}
