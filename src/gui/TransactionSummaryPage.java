package gui;

import models.Transaction;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Locale;

public class TransactionSummaryPage extends JPanel {
    private final CinemaApp app;
    private final Transaction transaction;

    public TransactionSummaryPage(CinemaApp app, Transaction transaction) {
        this.app = app;
        this.transaction = transaction;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));

        JLabel titleLabel = new JLabel("TicketID", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Terima Kasih Atas Kepercayaan Anda.", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        return headerPanel;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        contentPanel.setBackground(Color.WHITE);

        // Bioskop
        contentPanel.add(createLabel("Bioskop:"));
        contentPanel.add(createLabel("TicketID Cinema"));

        // Studio
        contentPanel.add(createLabel("Theatre:"));
        contentPanel.add(createValue(transaction.getShowtime().getStudioName()));

        // Show
        contentPanel.add(createLabel("Show:"));
        contentPanel.add(createValue(String.valueOf(transaction.getShowtime().getId())));

        // Film
        contentPanel.add(createLabel("Film:"));
        contentPanel.add(createValue(transaction.getMovieTitle()));

        // Tanggal Tayang
        contentPanel.add(createLabel("Tanggal Tayang:"));
        contentPanel.add(createValue(transaction.getShowtime().getDate().toString()));

        // Jam Tayang
        contentPanel.add(createLabel("Jam Tayang:"));
        contentPanel.add(createValue(transaction.getShowtime().getStartTime().toString()));

        // Jumlah Tiket
        contentPanel.add(createLabel("Jumlah Tiket:"));
        int jumlahTiket = transaction.getAmountTicket() > 0 ? transaction.getAmountTicket() : transaction.getSeats().size();
        contentPanel.add(createValue(String.valueOf(jumlahTiket)));

        // Harga Tiket
        contentPanel.add(createLabel("Harga Tiket:"));
        double hargaTiket = (double) transaction.getTotalPrice() / transaction.getSeats().size();
        contentPanel.add(createValue("Rp " + String.format("%,.0f", hargaTiket)));
        // Jenis Pembayaran
        contentPanel.add(createLabel("Jenis Pembayaran:"));
        contentPanel.add(createValue(transaction.getPaymentMethod()));

        // Kursi
        contentPanel.add(createLabel("Kursi:"));
        StringBuilder seatsText = new StringBuilder();
        for (int i = 0; i < transaction.getSeats().size(); i++) {
            seatsText.append(transaction.getSeats().get(i).getSeatsCode());
            if (i < transaction.getSeats().size() - 1) {
                seatsText.append(", ");
            }
        }
        contentPanel.add(createValue(seatsText.toString()));

        // Total
        contentPanel.add(createLabel("Total:"));
        contentPanel.add(createValue("Rp " + String.format("%,d", transaction.getTotalPrice())));
        
        // Kode Transaksi
        contentPanel.add(createLabel("Kode Transaksi:"));
        contentPanel.add(createValue(transaction.getTransactionCode()));

        return contentPanel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(51, 51, 51));
        return label;
    }

    private JLabel createValue(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(102, 102, 102));
        return label;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(Color.WHITE);

        JButton printButton = new JButton("Cetak Tiket") {
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
        printButton.setPreferredSize(new Dimension(150, 45));
        printButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        printButton.setBackground(new Color(72, 201, 176)); // Hijau
        printButton.setForeground(Color.WHITE);
        printButton.setFocusPainted(false);
        printButton.setBorderPainted(false);
        printButton.setContentAreaFilled(false);
        printButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        printButton.addActionListener(e -> {
            // Cetak tiket dalam format PDF
            generatePDF();
        });

        JButton backButton = new JButton("Kembali ke Menu") {
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
        backButton.setPreferredSize(new Dimension(150, 45));
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backButton.setBackground(new Color(100, 149, 237)); // Biru
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        backButton.addActionListener(e -> {
            app.showPage(new MovieSelectionPage(app));
        });

        buttonPanel.add(printButton);
        buttonPanel.add(backButton);

        return buttonPanel;
    }

    private void generatePDF() {
        // Implementasi cetak PDF akan ditambahkan nanti
        JOptionPane.showMessageDialog(this,
                "Fitur cetak PDF akan diimplementasikan segera!",
                "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }
}