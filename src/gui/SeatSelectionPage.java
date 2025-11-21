package gui;

import dao.SeatDAO;
import dao.TicketPriceDAO;
import models.Seat;
import models.Showtime;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeatSelectionPage extends JPanel {
    private final CinemaApp app;
    private final Showtime selectedShowtime; // Gunakan objek Showtime, bukan hanya ID
    private final List<Seat> selectedSeats = new ArrayList<>();

    // Custom colors
    private static final Color AVAILABLE_COLOR = new Color(72, 201, 176);   // Hijau
    private static final Color SELECTED_COLOR = new Color(255, 193, 7);     // Kuning
    private static final Color OCCUPIED_COLOR = new Color(231, 76, 60);     // Merah
    private static final Color BACKGROUND_COLOR = new Color(26, 32, 46);
    private static final Color CARD_COLOR = new Color(40, 44, 52);
    private static final Color TEXT_COLOR = new Color(248, 249, 250);
    private static final Color ACCENT_COLOR = new Color(116, 185, 255);
    private final TicketPriceDAO ticketPriceDAO = new TicketPriceDAO();

    private double getTicketPrice() {
        return ticketPriceDAO.getPriceByShowtimeId(selectedShowtime.getId());
    }

    public SeatSelectionPage(CinemaApp app, Showtime showtime) {
        this.app = app;
        this.selectedShowtime = showtime;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainContentPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Pilih Kursi Favorit Anda", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_COLOR);

        JLabel subtitle = new JLabel("Silakan pilih kursi yang tersedia", JLabel.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(134, 142, 150));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BACKGROUND_COLOR);
        titlePanel.add(title, BorderLayout.CENTER);
        titlePanel.add(subtitle, BorderLayout.SOUTH);

        headerPanel.add(titlePanel, BorderLayout.CENTER);

        return headerPanel;
    }

    private JPanel createMainContentPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Hanya tampilkan layar bioskop (tanpa info film/studio)
        JPanel screenPanel = createScreenPanel();
        mainPanel.add(screenPanel, BorderLayout.NORTH);

        // Bagian kursi + legend yang dapat discroll
        JPanel scrollableContent = new JPanel(new BorderLayout(10, 10));
        scrollableContent.setBackground(BACKGROUND_COLOR);

        JPanel seatsPanelWrapper = new JPanel(new BorderLayout());
        seatsPanelWrapper.setBackground(CARD_COLOR);
        seatsPanelWrapper.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(52, 58, 64), 1, true),
                new EmptyBorder(30, 30, 30, 30)
        ));
        seatsPanelWrapper.add(createSeatsPanel(), BorderLayout.CENTER);

        scrollableContent.add(seatsPanelWrapper, BorderLayout.CENTER);
        scrollableContent.add(createLegendPanel(), BorderLayout.SOUTH);

        // ScrollPane untuk kursi dan legend saja
        JScrollPane scrollPane = new JScrollPane(scrollableContent);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }


    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        infoPanel.setBackground(BACKGROUND_COLOR);

        // Film Title
        JLabel movieLabel = new JLabel("Film: " + selectedShowtime.getMovieTitle());
        movieLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        movieLabel.setForeground(TEXT_COLOR);

        // Studio
        JLabel studioLabel = new JLabel("Studio: " + selectedShowtime.getStudioName());
        studioLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        studioLabel.setForeground(new Color(134, 142, 150));

        // Date & Time
        JLabel timeLabel = new JLabel("Tanggal: " + selectedShowtime.getDate() + " | Jam: " + selectedShowtime.getStartTime());
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        timeLabel.setForeground(new Color(134, 142, 150));

        infoPanel.add(movieLabel);
        infoPanel.add(studioLabel);
        infoPanel.add(timeLabel);

        return infoPanel;
    }

    private JPanel createScreenPanel() {
        JPanel screenContainer = new JPanel(new BorderLayout());
        screenContainer.setBackground(BACKGROUND_COLOR);
        screenContainer.setBorder(new EmptyBorder(0, 0, 30, 0));

        JPanel screenPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();

                GradientPaint gradient = new GradientPaint(0, 0, ACCENT_COLOR, 0, height,
                        new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(), ACCENT_COLOR.getBlue(), 100));
                g2d.setPaint(gradient);
                g2d.fillRoundRect(width / 4, 10, width / 2, height - 20, 20, 20);

                g2d.setColor(ACCENT_COLOR);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(width / 4, 10, width / 2, height - 20, 20, 20);

                g2d.dispose();
            }
        };
        screenPanel.setBackground(BACKGROUND_COLOR);
        screenPanel.setPreferredSize(new Dimension(400, 60));

        JLabel screenLabel = new JLabel("LAYAR BIOSKOP", JLabel.CENTER);
        screenLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        screenLabel.setForeground(ACCENT_COLOR);

        screenContainer.add(screenPanel, BorderLayout.CENTER);
        screenContainer.add(screenLabel, BorderLayout.SOUTH);

        return screenContainer;
    }

    private JPanel createSeatsPanel() {
        SeatDAO seatDAO = new SeatDAO();
        List<Seat> seats = seatDAO.getSeatsByShowtime(selectedShowtime.getId());

        if (seats.isEmpty()) {
            JLabel noSeatsLabel = new JLabel("Belum ada kursi untuk studio ini.", JLabel.CENTER);
            noSeatsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            noSeatsLabel.setForeground(new Color(134, 142, 150));

            JPanel noSeatsPanel = new JPanel(new BorderLayout());
            noSeatsPanel.setBackground(CARD_COLOR);
            noSeatsPanel.add(noSeatsLabel, BorderLayout.CENTER);

            return noSeatsPanel;
        }

        // Kelompokkan kursi berdasarkan baris
        Map<Character, List<Seat>> seatsByRow = new HashMap<>();
        for (Seat seat : seats) {
            seatsByRow.computeIfAbsent(seat.getSeatsRow(), k -> new ArrayList<>()).add(seat);
        }

        // Urutkan baris A, B, C...
        List<Character> sortedRows = new ArrayList<>(seatsByRow.keySet());
        sortedRows.sort(Character::compareTo);

        // Buat panel untuk tampilan kursi
        JPanel seatsPanel = new JPanel(new GridBagLayout());
        seatsPanel.setBackground(CARD_COLOR);

        // Tampilkan baris dari bawah ke atas (seperti bioskop)
        for (int i = sortedRows.size() - 1; i >= 0; i--) {
            char row = sortedRows.get(i);
            List<Seat> rowSeats = seatsByRow.get(row);

            // Label baris (misal: A, B, C...)
            JLabel rowLabel = new JLabel(String.valueOf(row), JLabel.CENTER);
            rowLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            rowLabel.setForeground(TEXT_COLOR);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.insets = new Insets(5, 5, 5, 5);
            seatsPanel.add(rowLabel, gbc);

            // Tampilkan kursi dalam baris
            for (int j = 0; j < rowSeats.size(); j++) {
                Seat seat = rowSeats.get(j);
                JButton seatBtn = createSeatButton(seat);
                gbc.gridx = j + 1;
                gbc.gridy = i;
                seatsPanel.add(seatBtn, gbc);
            }
        }

        // Hitung jumlah kolom maksimum per baris
        int maxColumns = 0;
        for (List<Seat> rowSeats : seatsByRow.values()) {
            maxColumns = Math.max(maxColumns, rowSeats.size());
        }

        // Tambahkan label nomor kolom di bawah (dinamis)
        for (int j = 0; j < maxColumns; j++) {
            JLabel colLabel = new JLabel(String.valueOf(j + 1), JLabel.CENTER);
            colLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            colLabel.setForeground(TEXT_COLOR);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = j + 1;
            gbc.gridy = sortedRows.size();
            gbc.insets = new Insets(5, 5, 5, 5);
            seatsPanel.add(colLabel, gbc);
        }

        return seatsPanel;
    }

    private JButton createSeatButton(Seat seat) {
        JButton seatBtn = new JButton(seat.getSeatsCode()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Border
                if (isEnabled()) {
                    g2d.setColor(getBackground().darker());
                    g2d.setStroke(new BasicStroke(1));
                    g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }

                g2d.dispose();

                // Biarkan Swing menggambar teks setelah background selesai
                super.paintComponent(g);
            }
        };

        seatBtn.setPreferredSize(new Dimension(55, 55)); // sedikit diperlebar
        seatBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        seatBtn.setForeground(Color.WHITE);
        seatBtn.setFocusPainted(false);
        seatBtn.setBorderPainted(false);
        seatBtn.setContentAreaFilled(false);
        seatBtn.setHorizontalTextPosition(SwingConstants.CENTER);
        seatBtn.setVerticalTextPosition(SwingConstants.CENTER);
        seatBtn.setMargin(new Insets(0, 0, 0, 0));
        seatBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Tampilkan teks penuh (hilangkan elipsis)
        seatBtn.setHorizontalAlignment(SwingConstants.CENTER);
        seatBtn.setVerticalAlignment(SwingConstants.CENTER);
        seatBtn.setText(seat.getSeatsCode()); // pastikan tidak terpotong

        // Set warna awal
        if ("available".equalsIgnoreCase(seat.getStatus())) {
            seatBtn.setBackground(AVAILABLE_COLOR);
        } else {
            seatBtn.setBackground(OCCUPIED_COLOR);
            seatBtn.setEnabled(false);
            seatBtn.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

        // Hover effect
        if ("available".equalsIgnoreCase(seat.getStatus())) {
            seatBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (seatBtn.isEnabled() && !selectedSeats.contains(seat)) {
                        seatBtn.setBackground(AVAILABLE_COLOR.brighter());
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (seatBtn.isEnabled() && !selectedSeats.contains(seat)) {
                        seatBtn.setBackground(AVAILABLE_COLOR);
                    }
                }
            });
        }

        // Klik: pilih / batal
        seatBtn.addActionListener(e -> {
            if (!"available".equalsIgnoreCase(seat.getStatus())) return;

            if (selectedSeats.contains(seat)) {
                selectedSeats.remove(seat);
                seatBtn.setBackground(AVAILABLE_COLOR);
            } else {
                selectedSeats.add(seat);
                seatBtn.setBackground(SELECTED_COLOR);
            }
        });

        return seatBtn;
    }


    private JPanel createLegendPanel() {
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        legendPanel.setBackground(BACKGROUND_COLOR);
        legendPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        legendPanel.add(createLegendItem("Tersedia", AVAILABLE_COLOR));
        legendPanel.add(createLegendItem("Terpilih", SELECTED_COLOR));
        legendPanel.add(createLegendItem("Terisi", OCCUPIED_COLOR));

        return legendPanel;
    }

    private JPanel createLegendItem(String text, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        item.setBackground(BACKGROUND_COLOR);

        JPanel colorBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2d.dispose();
            }
        };
        colorBox.setPreferredSize(new Dimension(20, 20));
        colorBox.setBackground(BACKGROUND_COLOR);

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(TEXT_COLOR);

        item.add(colorBox);
        item.add(label);

        return item;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(BACKGROUND_COLOR);
        footerPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        // Panel utama dengan GridBagLayout untuk kontrol posisi presisi
        JPanel mainFooterPanel = new JPanel(new GridBagLayout());
        mainFooterPanel.setBackground(BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); // Padding dalam setiap komponen
        gbc.fill = GridBagConstraints.BOTH;

        // Kolom 1: Nama Film (Kiri)
        JPanel filmPanel = new JPanel(new BorderLayout());
        filmPanel.setBackground(BACKGROUND_COLOR);
        JLabel filmLabel = new JLabel("Film: " + selectedShowtime.getMovieTitle());
        filmLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        filmLabel.setForeground(TEXT_COLOR);
        filmPanel.add(filmLabel, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3; // Lebar kolom 30%
        mainFooterPanel.add(filmPanel, gbc);

        // Kolom 2: Info Ringkasan (Tengah)
        JPanel summaryPanel = new JPanel(new GridLayout(0, 1, 3, 2));
        summaryPanel.setBackground(BACKGROUND_COLOR);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel hargaLabel = new JLabel("Harga Tiket: Rp " + String.format("%,.0f", getTicketPrice()));
        hargaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hargaLabel.setForeground(new Color(134, 142, 150));

        JLabel jumlahLabel = new JLabel("Jumlah Tiket: 0");
        jumlahLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        jumlahLabel.setForeground(new Color(134, 142, 150));

        JLabel totalLabel = new JLabel("Total: Rp 0");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalLabel.setForeground(TEXT_COLOR);

        JLabel seatsLabel = new JLabel("Seats: ");
        seatsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        seatsLabel.setForeground(new Color(134, 142, 150));

        summaryPanel.add(hargaLabel);
        summaryPanel.add(jumlahLabel);
        summaryPanel.add(totalLabel);
        summaryPanel.add(seatsLabel);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.4; // Lebar kolom 40% — agar lebih lebar dari kolom kiri dan kanan
        mainFooterPanel.add(summaryPanel, gbc);

        // Kolom 3: Info Show & Tombol (Kanan)
        JPanel rightPanel = new JPanel(new BorderLayout(10, 0));
        rightPanel.setBackground(BACKGROUND_COLOR);

        // Panel info show, studio, tanggal, jam
        JPanel showInfoPanel = new JPanel(new GridLayout(0, 1, 3, 2));
        showInfoPanel.setBackground(BACKGROUND_COLOR);
        showInfoPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel showLabel = new JLabel("Show: " + selectedShowtime.getId());
        showLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showLabel.setForeground(new Color(134, 142, 150));

        JLabel studioLabel = new JLabel("Studio: " + selectedShowtime.getStudioName());
        studioLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        studioLabel.setForeground(new Color(134, 142, 150));

        JLabel dateLabel = new JLabel("Tanggal: " + selectedShowtime.getDate());
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(134, 142, 150));

        JLabel timeLabel = new JLabel("Jam: " + selectedShowtime.getStartTime());
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timeLabel.setForeground(new Color(134, 142, 150));

        showInfoPanel.add(showLabel);
        showInfoPanel.add(studioLabel);
        showInfoPanel.add(dateLabel);
        showInfoPanel.add(timeLabel);

        // Panel tombol
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        // Tombol Kembali
        JButton backBtn = new JButton("Kembali") {
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
        backBtn.setPreferredSize(new Dimension(150, 45));
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backBtn.setBackground(ACCENT_COLOR);
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        backBtn.addActionListener(e -> {
            app.showPage(new MovieSelectionPage(app)); // Kembali ke halaman film
        });

        // Tombol Beli
        JButton buyBtn = new JButton("Beli") {
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

        buyBtn.setPreferredSize(new Dimension(150, 45));
        buyBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        buyBtn.setBackground(ACCENT_COLOR);
        buyBtn.setForeground(Color.WHITE);
        buyBtn.setFocusPainted(false);
        buyBtn.setBorderPainted(false);
        buyBtn.setContentAreaFilled(false);
        buyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        buyBtn.addActionListener(e -> {
            if (selectedSeats.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Silakan pilih minimal 1 kursi!",
                        "Peringatan",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            app.setSelectedSeats(new ArrayList<>(selectedSeats));
            PaymentPopup popup = new PaymentPopup(app, selectedShowtime, selectedSeats);
            popup.setVisible(true);
        });

        buttonPanel.add(backBtn);
        buttonPanel.add(buyBtn);

        rightPanel.add(showInfoPanel, BorderLayout.CENTER);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.3; // Lebar kolom 30%
        mainFooterPanel.add(rightPanel, gbc);

        // Update selected seats counter AND summary info
        Timer timer = new Timer(100, e -> {
            int count = selectedSeats.size();
            ((JLabel)summaryPanel.getComponent(1)).setText("Jumlah Tiket: " + count);

            double hargaTiket = getTicketPrice();
            double totalPrice = hargaTiket * count;
            ((JLabel)summaryPanel.getComponent(0)).setText("Harga Tiket: Rp " + String.format("%,.0f", hargaTiket));
            ((JLabel)summaryPanel.getComponent(2)).setText("Total: Rp " + String.format("%,.0f", totalPrice));

            // Update Seats
            StringBuilder seatsText = new StringBuilder("Seats: ");
            for (int i = 0; i < Math.min(count, 3); i++) { // tampilkan max 3 kursi
                seatsText.append(selectedSeats.get(i).getSeatsCode()).append(", ");
            }
            if (count > 3) {
                seatsText.append("...");
            } else {
                seatsText.setLength(Math.max(0, seatsText.length() - 2)); // hapus koma terakhir
            }
            ((JLabel)summaryPanel.getComponent(3)).setText(seatsText.toString());
        });
        timer.start();

        footerPanel.add(mainFooterPanel, BorderLayout.CENTER);

        return footerPanel;
    }
}