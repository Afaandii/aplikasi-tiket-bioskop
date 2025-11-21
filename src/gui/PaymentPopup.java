// gui/PaymentPopup.java
package gui;

import dao.SeatDAO;
import dao.TicketPriceDAO;
import dao.TransactionDAO;
import models.Showtime;
import models.Seat;
import models.Transaction;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;

public class PaymentPopup extends JDialog {
    private final CinemaApp app;
    private final Showtime selectedShowtime;
    private final List<Seat> selectedSeats;
    private final double ticketPrice;
    private final double totalPrice;
    private String paymentMethod = "Cash";
    
    // 🔑 Field yang disimpan
    private JComboBox<String> paymentComboBox;
    private JTextField amountField;
    private JTextField cardField;

    public PaymentPopup(CinemaApp app, Showtime showtime, List<Seat> seats) {
        this.app = app;
        this.selectedShowtime = showtime;
        this.selectedSeats = new ArrayList<>(seats);
        this.ticketPrice = app.getTicketPriceDAO().getPriceByShowtimeId(showtime.getId());
        this.totalPrice = ticketPrice * seats.size();
        this.paymentMethod = "Cash";

        initializeUI();
    }

    private void initializeUI() {
        setTitle("Pembayaran");
        setModal(true);
        setLayout(new BorderLayout(15, 15));
        setSize(400, 600);
        setLocationRelativeTo(null);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Konfirmasi Pembayaran", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel, BorderLayout.CENTER);

        return headerPanel;
    }

    private JPanel createContentPanel() {
    JPanel contentPanel = new JPanel(new GridLayout(0, 1, 10, 10));
    contentPanel.setBackground(Color.WHITE);
    contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    // Info Film & Show
    contentPanel.add(createInfoLabel("Film: " + selectedShowtime.getMovieTitle()));
    contentPanel.add(createInfoLabel("Studio: " + selectedShowtime.getStudioName()));
    contentPanel.add(createInfoLabel("Show: " + selectedShowtime.getId()));
    contentPanel.add(createInfoLabel("Tanggal: " + selectedShowtime.getDate()));
    contentPanel.add(createInfoLabel("Jam: " + selectedShowtime.getStartTime()));

    // Kursi yang dipilih
    StringBuilder seatsText = new StringBuilder("Seats: ");
    for (int i = 0; i < Math.min(selectedSeats.size(), 3); i++) {
        seatsText.append(selectedSeats.get(i).getSeatsCode()).append(", ");
    }
    if (selectedSeats.size() > 3) {
        seatsText.append("...");
    } else if (seatsText.length() > 7) {
        seatsText.setLength(seatsText.length() - 2);
    }
    contentPanel.add(createInfoLabel(seatsText.toString()));

    // Harga & Total
    contentPanel.add(createInfoLabel("Harga Tiket: Rp " + String.format("%,.0f", ticketPrice)));
    contentPanel.add(createInfoLabel("Jumlah Tiket: " + selectedSeats.size()));
    contentPanel.add(createInfoLabel("Total: Rp " + String.format("%,.0f", totalPrice)));

    // Metode Pembayaran (hanya dropdown)
    JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    paymentPanel.setBackground(Color.WHITE);

    JLabel methodLabel = new JLabel("Metode Pembayaran:");
    methodLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    methodLabel.setForeground(new Color(51, 51, 51));

    this.paymentComboBox = new JComboBox<>(new String[]{"Cash", "Qris"});
    this.paymentComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    this.paymentComboBox.setSelectedItem(this.paymentMethod);

    paymentPanel.add(methodLabel);
    paymentPanel.add(this.paymentComboBox);

    contentPanel.add(paymentPanel);

    return contentPanel;
}

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(51, 51, 51));
        return label;
    }

    private void updatePaymentFields(String method) {
        if ("Cash".equals(method)) {
            amountField.setVisible(true);
            cardField.setVisible(false);
        } else {
            amountField.setVisible(false);
            cardField.setVisible(true);
        }
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(Color.WHITE);

        JButton okButton = new JButton("OK") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(getBackground().darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(getBackground().brighter());
                } else {
                    g2d.setColor(getBackground());
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        okButton.setPreferredSize(new Dimension(100, 35));
        okButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        okButton.setBackground(new Color(72, 201, 176)); // Hijau
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        okButton.setBorderPainted(false);
        okButton.setContentAreaFilled(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        okButton.addActionListener(e -> {
            processPayment();
        });

        JButton cancelButton = new JButton("BATAL") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(getBackground().darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(getBackground().brighter());
                } else {
                    g2d.setColor(getBackground());
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelButton.setBackground(new Color(231, 76, 60)); // Merah
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setContentAreaFilled(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        cancelButton.addActionListener(e -> {
            dispose(); // Tutup popup
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        return buttonPanel;
    }

    private void processPayment() {
    String method = (String) paymentComboBox.getSelectedItem();

    // Simpan transaksi ke database
    Transaction transaction = saveTransaction(method, 0.0, 0.0);

    if (transaction != null) {
        // Tampilkan halaman ringkasan transaksi
        app.showPage(new TransactionSummaryPage(app, transaction));
        dispose(); // Tutup popup
    }
}

    private Transaction saveTransaction(String paymentMethod, double amount, double change) {
    TransactionDAO transactionDAO = new TransactionDAO();

    // Buat list seat_id dari selectedSeats
    List<Seat> seats = new ArrayList<>();
    for (Seat seat : selectedSeats) {
        seats.add(seat);
    }

    // Buat transaksi baru
    int userId = app.getLoggedInUser().getId();
    TicketPriceDAO ticketPriceDAO = new TicketPriceDAO();
    int ticketPriceId = ticketPriceDAO.getTicketPriceIdByShowtimeId(selectedShowtime.getId());
    Transaction transaction = transactionDAO.createTransaction(userId, ticketPriceId, selectedShowtime.getId(), seats, paymentMethod, (int) totalPrice);

    if (transaction == null) {
        JOptionPane.showMessageDialog(this,
                "Gagal menyimpan transaksi!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        return null;
    }

    // Update status kursi menjadi "booked"
    for (Seat seat : selectedSeats) {
        seat.setStatus("booked");
        SeatDAO seatDAO = new SeatDAO();
        seatDAO.updateSeat(seat);
    }

    return transaction;
}
}