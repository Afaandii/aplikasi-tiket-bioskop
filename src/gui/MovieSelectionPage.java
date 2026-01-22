package gui;

import dao.MovieDAO;
import dao.ShowtimeDAO;
import dao.TicketPriceDAO;
import models.Movie;
import models.Showtime;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.table.TableCellEditor;

/**
 * MovieSelectionPage - versi baru menyerupai monitor kasir XXI.
 * Menampilkan tabel jadwal tayang per film dengan tombol jam tayang.
 */
public class MovieSelectionPage extends JPanel {
    private static final long serialVersionUID = 1L;

    // Colors
    private static final Color HEADER_COLOR = new Color(70, 130, 180);
    private static final Color ROW_COLOR_1 = new Color(200, 255, 255); // cyan
    private static final Color ROW_COLOR_2 = new Color(255, 255, 150); // yellow
    private static final Color ROW_COLOR_3 = new Color(150, 255, 150); // green
    private static final Color ROW_COLOR_4 = new Color(255, 200, 200); // pink
    private static final Color ROW_COLOR_5 = new Color(200, 200, 255); // light blue
    private static final Color ROW_COLOR_6 = new Color(255, 200, 150); // orange

    private static final Color X_COLOR = new Color(255, 50, 50); // merah untuk X
    private static final Color TEXT_COLOR = new Color(51, 51, 51);

    private JTable table;
    private DefaultTableModel tableModel;
    private final MovieDAO movieDAO = new MovieDAO();
    private final ShowtimeDAO showtimeDAO = new ShowtimeDAO();
    private final TicketPriceDAO ticketPriceDAO = new TicketPriceDAO();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd-MM-yyyy");
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

    public MovieSelectionPage(CinemaApp app) {
        initializeComponents(app);
    }

    private void initializeComponents(CinemaApp app) {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(app), BorderLayout.CENTER);
        add(createBottomPanel(app), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_COLOR);
        header.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel("TicketID Cinema", JLabel.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        JLabel dateLabel = new JLabel(dateFormat.format(new Date(System.currentTimeMillis())), JLabel.CENTER);
        dateLabel.setForeground(Color.WHITE);
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        header.add(titleLabel, BorderLayout.CENTER);
        header.add(dateLabel, BorderLayout.SOUTH);

        return header;
    }

    private JScrollPane createMainContent(CinemaApp app) {
        // Kolom tabel: No, Movie, HTM, Show 1, Show 2, Show 3, Show 4, Show 5
        String[] columnNames = {"No", "Movie", "HTM", "Show 1", "Show 2", "Show 3", "Show 4", "Show 5"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Hanya kolom Show 1-5 (index 3-7) yang bisa diedit
                return column >= 3 && column <= 7;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(60);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.getTableHeader().setBackground(HEADER_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);

        // Custom renderer untuk kolom Movie (poster + judul)
        table.getColumnModel().getColumn(1).setCellRenderer(new MovieCellRenderer());

        // Custom renderer untuk kolom Show (button jam tayang atau X)
        for (int i = 3; i <= 7; i++) { // Show 1 sampai Show 5
            table.getColumnModel().getColumn(i).setCellRenderer(new ShowtimeCellRenderer());
            table.getColumnModel().getColumn(i).setCellEditor(new ShowtimeCellEditor(app));
        }

        // Warna baris bergantian
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    int colorIndex = row % 6; // 6 warna
                    Color bgColor = switch (colorIndex) {
                        case 0 -> ROW_COLOR_1;
                        case 1 -> ROW_COLOR_2;
                        case 2 -> ROW_COLOR_3;
                        case 3 -> ROW_COLOR_4;
                        case 4 -> ROW_COLOR_5;
                        case 5 -> ROW_COLOR_6;
                        default -> Color.WHITE;
                    };
                    c.setBackground(bgColor);
                }
                c.setForeground(TEXT_COLOR);
                return c;
            }
        });

        // Muat data
        loadData(app);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        return scrollPane;
    }

    private void loadData(CinemaApp app) {
        tableModel.setRowCount(0); // kosongkan dulu

        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        List<Showtime> allShowtimes = showtimeDAO.getShowtimesByDate(today);

        // Kelompokkan showtime berdasarkan movie_id
        Map<Integer, List<Showtime>> showtimesByMovie = new HashMap<>();
        for (Showtime s : allShowtimes) {
            showtimesByMovie.computeIfAbsent(s.getMovieId(), k -> new ArrayList<>()).add(s);
        }

        // Ambil daftar film yang sedang tayang
        List<Movie> movies = movieDAO.getCurrentlyShowingMovies();

        int no = 1;
        for (Movie movie : movies) {
            if (!showtimesByMovie.containsKey(movie.getId())) continue;

            List<Showtime> movieShowtimes = showtimesByMovie.get(movie.getId());
            double htm = ticketPriceDAO.getPriceByShowtimeId(movieShowtimes.get(0).getId()); // ambil harga dari showtime pertama

            // Siapkan array untuk kolom Show 1-5
            Object[] showButtons = new Object[5];
            Arrays.fill(showButtons, ""); // default kosong

            // Isi jam tayang ke kolom Show 1-5
            for (int i = 0; i < Math.min(movieShowtimes.size(), 5); i++) {
                Showtime s = movieShowtimes.get(i);
                showButtons[i] = s; // simpan objek Showtime untuk nanti diproses di renderer/editor
            }

            // Tambahkan baris ke tabel
            Object[] row = {
                no++,
                movie, // akan di-render oleh MovieCellRenderer
                "Rp " + String.format("%,.0f", htm),
                showButtons[0], showButtons[1], showButtons[2], showButtons[3], showButtons[4]
            };
            tableModel.addRow(row);
        }
    }

        private JPanel createBottomPanel(CinemaApp app) {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottomPanel.setBackground(Color.WHITE);

        // Tombol Logout (di sebelah kiri)
        JButton logoutButton = createButton("Logout", new Color(231, 76, 60)); // Merah

        // Tombol Laporan Keuangan (di tengah)
        JButton reportButton = createButton("📊 Laporan Keuangan", new Color(70, 130, 180)); // Biru

        // Tombol Refresh (di sebelah kanan)
        JButton refreshButton = createButton("🔄 Segarkan", new Color(70, 130, 180));

        // Action Listener untuk Logout
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                MovieSelectionPage.this,
                "Apakah Anda yakin ingin logout?",
                "Konfirmasi Logout",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                app.setLoggedInUser(null); // Hapus sesi pengguna
                app.showPage(new LoginPage(app)); // Kembali ke halaman login
            }
        });

        // Action Listener untuk Report
        reportButton.addActionListener(e -> {
            app.showPage(new FinancialReportKasir(app)); // Alihkan ke laporan keuangan
        });

        // Action Listener untuk Refresh
        refreshButton.addActionListener(e -> {
            loadData(app);
            table.repaint();
        });

        // Tambahkan tombol dalam urutan yang diinginkan
        bottomPanel.add(logoutButton);
        bottomPanel.add(reportButton);
        bottomPanel.add(refreshButton);

        return bottomPanel;
    }

    private JButton createButton(String text, Color bg) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bg);
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bg.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bg);
            }
        });
        return button;
    }

    // Renderer untuk kolom Movie (poster + judul)
    private class MovieCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new BorderLayout(5, 0));
            panel.setOpaque(true);
            panel.setBackground(table.getSelectionBackground());

            if (value instanceof Movie movie) {
                // Poster
                JLabel posterLabel = createPosterLabelFromBytes(movie.getPosterImage());
                panel.add(posterLabel, BorderLayout.WEST);

                // Judul film
                JLabel titleLabel = new JLabel(movie.getTitle());
                titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
                titleLabel.setForeground(TEXT_COLOR);
                titleLabel.setHorizontalAlignment(JLabel.LEFT);
                panel.add(titleLabel, BorderLayout.CENTER);
            }

            return panel;
        }
    }

    // Renderer untuk kolom Showtime (tombol jam atau X)
    private class ShowtimeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setOpaque(true);
            panel.setBackground(table.getSelectionBackground());

            if (value instanceof Showtime showtime) {
                LocalTime now = LocalTime.now();
                LocalTime showTime = showtime.getStartTime().toLocalTime();

                if (showTime.isBefore(now)) {
                    // Tampilkan JAM + ❌, bukan hanya ❌
                    JLabel label = new JLabel(showTime.format(timeFormat) + " ❌");
                    label.setFont(new Font("SansSerif", Font.BOLD, 14));
                    label.setForeground(X_COLOR); // merah
                    label.setHorizontalAlignment(JLabel.CENTER);
                    panel.add(label, BorderLayout.CENTER);
                } else {
                    JButton btn = new JButton(showTime.format(timeFormat));
                    btn.setFont(new Font("SansSerif", Font.BOLD, 14));
                    btn.setForeground(TEXT_COLOR);
                    btn.setBackground(Color.WHITE);
                    btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                    btn.setFocusPainted(false);
                    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    panel.add(btn, BorderLayout.CENTER);
                }
            }

            return panel;
        }
    }

        // Editor untuk kolom Showtime (agar bisa diklik)
        private class ShowtimeCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final CinemaApp app;
        private JPanel panel;
        private JButton button;
        private JLabel xLabel;
        private Showtime currentShowtime;

        public ShowtimeCellEditor(CinemaApp app) {
            this.app = app;
            panel = new JPanel(new BorderLayout());
            button = new JButton();
            xLabel = new JLabel();

            button.addActionListener(e -> {
                if (currentShowtime != null) {
                    app.setSelectedShowtime(currentShowtime); // simpan objek lengkap
                    app.showPage(new SeatSelectionPage(app, currentShowtime)); // 🔥 PINDAH HALAMAN DI SINI
                    stopCellEditing();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            panel.removeAll();
            if (value instanceof Showtime showtime) {
                currentShowtime = showtime;
                LocalTime now = LocalTime.now();
                LocalTime showTime = showtime.getStartTime().toLocalTime();
                String timeStr = showTime.format(timeFormat);

                if (showTime.isBefore(now)) {
                    xLabel.setText(timeStr + " ❌");
                    xLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
                    xLabel.setForeground(X_COLOR);
                    xLabel.setHorizontalAlignment(JLabel.CENTER);
                    panel.add(xLabel, BorderLayout.CENTER);
                } else {
                    button.setText(timeStr);
                    button.setFont(new Font("SansSerif", Font.BOLD, 14));
                    button.setForeground(TEXT_COLOR);
                    button.setBackground(Color.WHITE);
                    button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                    button.setFocusPainted(false);
                    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    panel.add(button, BorderLayout.CENTER);
                }
            } else {
                panel.add(new JLabel(""), BorderLayout.CENTER);
            }
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentShowtime;
        }

        @Override
        public boolean isCellEditable(EventObject event) {
            if (event instanceof MouseEvent me) {
                int row = table.rowAtPoint(me.getPoint());
                int col = table.columnAtPoint(me.getPoint());
                Object value = tableModel.getValueAt(row, col);
                if (value instanceof Showtime s) {
                    LocalTime now = LocalTime.now();
                    LocalTime showTime = s.getStartTime().toLocalTime();
                    return !showTime.isBefore(now);
                }
            }
            return false;
        }
    }

    // Helper: Buat label poster dari byte[]
    private JLabel createPosterLabelFromBytes(byte[] posterImage) {
        int w = 40, h = 60;
        JLabel posterLabel = new JLabel("🎬", JLabel.CENTER);
        posterLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        posterLabel.setOpaque(true);
        posterLabel.setPreferredSize(new Dimension(w, h));
        posterLabel.setMinimumSize(new Dimension(w, h));
        posterLabel.setBackground(new Color(100, 149, 237));
        posterLabel.setForeground(Color.WHITE);
        posterLabel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));

        if (posterImage != null && posterImage.length > 10) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(posterImage)) {
                BufferedImage buf = ImageIO.read(bais);
                if (buf != null) {
                    Image scaled = buf.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                    posterLabel.setIcon(new ImageIcon(scaled));
                    posterLabel.setText(null);
                    posterLabel.setBackground(Color.WHITE);
                    posterLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
                }
            } catch (Exception ex) {
                // tetap pakai fallback
            }
        }

        return posterLabel;
    }
}