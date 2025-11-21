package gui;

import dao.ShowtimeDAO;
import models.Showtime;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.ArrayList;

public class ShowtimeManagementPanel extends JPanel {
    private final Color primaryColor = new Color(41, 128, 185);
    private final Color backgroundColor = new Color(236, 240, 241);
    private final Color whiteColor = Color.WHITE;
    private final Color textColor = new Color(44, 62, 80);

    private JTable showtimeTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> movieCombo, studioCombo;
    private JSpinner dateSpinner, startTimeSpinner, endTimeSpinner;

    private ShowtimeDAO showtimeDAO = new ShowtimeDAO();

    public ShowtimeManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(backgroundColor);
        initializeComponents();
        loadShowtimeData();
    }

    private void initializeComponents() {
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main Content
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.65);
        splitPane.setBorder(null);

        // Left Panel - Table
        JPanel tablePanel = createTablePanel();
        splitPane.setLeftComponent(tablePanel);

        // Right Panel - Form
        JPanel formPanel = createFormPanel();
        splitPane.setRightComponent(formPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(whiteColor);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Showtime Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(textColor);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Panel tombol kanan
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(whiteColor);

        JButton refreshBtn = createActionButton("Refresh", new Color(52, 152, 219));
        refreshBtn.setPreferredSize(new Dimension(100, 35)); // biar tulisannya gak terpotong
        refreshBtn.addActionListener(e -> {
            // Refresh data showtime, movie, dan studio
            movieCombo.removeAllItems();
            studioCombo.removeAllItems();
            movieCombo.addItem("-- Select Movie --");
            studioCombo.addItem("-- Select Studio --");
            for (String movie : showtimeDAO.getMovieList()) movieCombo.addItem(movie);
            for (String studio : showtimeDAO.getStudioList()) studioCombo.addItem(studio);

            loadShowtimeData();
            clearForm();
            JOptionPane.showMessageDialog(this, "Halaman berhasil direfresh!", "Info", JOptionPane.INFORMATION_MESSAGE);
        });
        buttonPanel.add(refreshBtn);

        headerPanel.add(buttonPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(backgroundColor);
        panel.setBorder(new EmptyBorder(0, 20, 20, 10));

        String[] columns = {"ID", "Movie", "Studio", "Date", "Start Time", "End Time", "Created At"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        showtimeTable = new JTable(tableModel);
        showtimeTable.setBackground(whiteColor);
        showtimeTable.setSelectionBackground(new Color(52, 152, 219, 50));
        showtimeTable.setRowHeight(35);
        showtimeTable.getTableHeader().setBackground(new Color(149, 165, 166));
        showtimeTable.getTableHeader().setForeground(whiteColor);
        showtimeTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        // Column widths
        showtimeTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        showtimeTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        showtimeTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        showtimeTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        showtimeTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        showtimeTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        showtimeTable.getColumnModel().getColumn(6).setPreferredWidth(120);

        // Selection listener
        showtimeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedShowtime();
            }
        });

        JScrollPane scrollPane = new JScrollPane(showtimeTable);
        scrollPane.setBackground(whiteColor);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(whiteColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));
        panel.setPreferredSize(new Dimension(340, 0));

        JLabel formTitle = new JLabel("Showtime Form");
        formTitle.setFont(new Font("Arial", Font.BOLD, 18));
        formTitle.setForeground(textColor);
        panel.add(formTitle, BorderLayout.NORTH);

        // Gunakan GridBagLayout untuk kontrol lebih baik
        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setBackground(whiteColor);
        formFields.setBorder(new EmptyBorder(20, 0, 20, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Movie Combo
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        JLabel movieLabel = new JLabel("Movie:");
        movieLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        movieLabel.setForeground(textColor);
        formFields.add(movieLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        movieCombo = createMovieCombo();
        formFields.add(movieCombo, gbc);

        // Studio Combo
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        JLabel studioLabel = new JLabel("Studio:");
        studioLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        studioLabel.setForeground(textColor);
        formFields.add(studioLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        studioCombo = createStudioCombo();
        formFields.add(studioCombo, gbc);

        // Date Field
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        dateLabel.setForeground(textColor);
        formFields.add(dateLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setFont(new Font("Arial", Font.PLAIN, 14));
        dateSpinner.setPreferredSize(new Dimension(0, 35));
        formFields.add(dateSpinner, gbc);

        // Start Time Field
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0.0;
        JLabel startTimeLabel = new JLabel("Start Time:");
        startTimeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        startTimeLabel.setForeground(textColor);
        formFields.add(startTimeLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.weightx = 1.0;
        // Gunakan model yang spesifik untuk time
        SpinnerDateModel startTimeModel = new SpinnerDateModel();
        startTimeSpinner = new JSpinner(startTimeModel);
        JSpinner.DateEditor startTimeEditor = new JSpinner.DateEditor(startTimeSpinner, "HH:mm");
        startTimeSpinner.setEditor(startTimeEditor);
        startTimeSpinner.setFont(new Font("Arial", Font.PLAIN, 14));
        startTimeSpinner.setPreferredSize(new Dimension(0, 35));
        // Set nilai default ke waktu sekarang
        startTimeSpinner.setValue(new java.util.Date()); // <-- Ini penting!
        formFields.add(startTimeSpinner, gbc);

        // End Time Field
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.weightx = 0.0;
        JLabel endTimeLabel = new JLabel("End Time:");
        endTimeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        endTimeLabel.setForeground(textColor);
        formFields.add(endTimeLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.weightx = 1.0;
        SpinnerDateModel endTimeModel = new SpinnerDateModel();
        endTimeSpinner = new JSpinner(endTimeModel);
        JSpinner.DateEditor endTimeEditor = new JSpinner.DateEditor(endTimeSpinner, "HH:mm");
        endTimeSpinner.setEditor(endTimeEditor);
        endTimeSpinner.setFont(new Font("Arial", Font.PLAIN, 14));
        endTimeSpinner.setPreferredSize(new Dimension(0, 35));
        // Set nilai default ke waktu sekarang
        endTimeSpinner.setValue(new java.util.Date()); // <-- Ini penting!
        formFields.add(endTimeSpinner, gbc);

        panel.add(formFields, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(whiteColor);

        JButton addBtn = createActionButton("Add", new Color(46, 204, 113));
        addBtn.addActionListener(e -> addShowtime());
        buttonPanel.add(addBtn);

        JButton updateBtn = createActionButton("Update", new Color(241, 196, 15));
        updateBtn.addActionListener(e -> updateShowtime());
        buttonPanel.add(updateBtn);

        JButton deleteBtn = createActionButton("Delete", new Color(231, 76, 60));
        deleteBtn.addActionListener(e -> deleteShowtime());
        buttonPanel.add(deleteBtn);

        JButton clearBtn = createActionButton("Clear", new Color(149, 165, 166));
        clearBtn.addActionListener(e -> {
            clearForm();
            JOptionPane.showMessageDialog(this, "Form telah dibersihkan!", "Info", JOptionPane.INFORMATION_MESSAGE);
        });
        buttonPanel.add(clearBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JComboBox<String> createMovieCombo() {
        JComboBox<String> combo = new JComboBox<>();
        combo.addItem("-- Select Movie --");
        List<String> movies = showtimeDAO.getMovieList();
        for (String movie : movies) {
            combo.addItem(movie);
        }
        combo.setFont(new Font("Arial", Font.PLAIN, 14));
        combo.setPreferredSize(new Dimension(0, 35));
        return combo;
    }

    private JComboBox<String> createStudioCombo() {
        JComboBox<String> combo = new JComboBox<>();
        combo.addItem("-- Select Studio --");
        List<String> studios = showtimeDAO.getStudioList();
        for (String studio : studios) {
            combo.addItem(studio);
        }
        combo.setFont(new Font("Arial", Font.PLAIN, 14));
        combo.setPreferredSize(new Dimension(0, 35));
        return combo;
    }

    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(whiteColor);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(70, 35));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private void loadShowtimeData() {
        tableModel.setRowCount(0);
        List<Showtime> showtimes = showtimeDAO.getAllShowtimes();

        for (Showtime s : showtimes) {
            tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getMovieTitle(),
                    s.getStudioName(),
                    s.getDate(),
                    s.getStartTime(),
                    s.getEndTime(),
                    s.getCreatedAt()
            });
        }
    }

    private void loadSelectedShowtime() {
        int selectedRow = showtimeTable.getSelectedRow();
        if (selectedRow >= 0) {
            int movieId = (int) tableModel.getValueAt(selectedRow, 0); // ID bukan movieTitle
            int studioId = (int) tableModel.getValueAt(selectedRow, 0); // ID bukan studioName

            // Cari string combo yang cocok berdasarkan ID
            String movieItem = findMovieItemById(movieId);
            if (movieItem != null) {
                movieCombo.setSelectedItem(movieItem);
            }

            String studioItem = findStudioItemById(studioId);
            if (studioItem != null) {
                studioCombo.setSelectedItem(studioItem);
            }

            // Date
            Date date = (Date) tableModel.getValueAt(selectedRow, 3);
            dateSpinner.setValue(date);

            // Start Time
            Time startTime = (Time) tableModel.getValueAt(selectedRow, 4);
            startTimeSpinner.setValue(startTime);

            // End Time
            Time endTime = (Time) tableModel.getValueAt(selectedRow, 5);
            endTimeSpinner.setValue(endTime);
        }
    }

    private String findMovieItemById(int movieId) {
        for (int i = 0; i < movieCombo.getItemCount(); i++) {
            String item = (String) movieCombo.getItemAt(i);
            if (item.startsWith(String.valueOf(movieId) + " - ")) {
                return item;
            }
        }
        return null;
    }

    private String findStudioItemById(int studioId) {
        for (int i = 0; i < studioCombo.getItemCount(); i++) {
            String item = (String) studioCombo.getItemAt(i);
            if (item.startsWith(String.valueOf(studioId) + " - ")) {
                return item;
            }
        }
        return null;
    }

        private void addShowtime() {
        if (validateForm()) {
            try {
                int movieId = ShowtimeDAO.extractIdFromCombo((String) movieCombo.getSelectedItem());
                int studioId = ShowtimeDAO.extractIdFromCombo((String) studioCombo.getSelectedItem());

                // Konversi java.util.Date dari JSpinner ke java.sql.Date
                java.util.Date utilDate = (java.util.Date) dateSpinner.getValue();
                Date sqlDate = new Date(utilDate.getTime()); // <-- Konversi ke java.sql.Date

                // Konversi java.util.Date dari JSpinner ke java.sql.Time
                java.util.Date utilStartTime = (java.util.Date) startTimeSpinner.getValue();
                Time sqlStartTime = new Time(utilStartTime.getTime()); // <-- Konversi ke java.sql.Time

                java.util.Date utilEndTime = (java.util.Date) endTimeSpinner.getValue();
                Time sqlEndTime = new Time(utilEndTime.getTime()); // <-- Konversi ke java.sql.Time

                Showtime showtime = new Showtime(movieId, studioId, sqlDate, sqlStartTime, sqlEndTime);

                int newId = showtimeDAO.addShowtime(showtime);
                if (newId > 0) {
                    JOptionPane.showMessageDialog(this, "Showtime berhasil ditambahkan!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadShowtimeData();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menambahkan showtime!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

        private void updateShowtime() {
        int selectedRow = showtimeTable.getSelectedRow();
        if (selectedRow >= 0 && validateForm()) {
            try {
                int showtimeId = (int) tableModel.getValueAt(selectedRow, 0);
                int movieId = ShowtimeDAO.extractIdFromCombo((String) movieCombo.getSelectedItem());
                int studioId = ShowtimeDAO.extractIdFromCombo((String) studioCombo.getSelectedItem());

                // Konversi java.util.Date dari JSpinner ke java.sql.Date
                java.util.Date utilDate = (java.util.Date) dateSpinner.getValue();
                Date sqlDate = new Date(utilDate.getTime());

                java.util.Date utilStartTime = (java.util.Date) startTimeSpinner.getValue();
                Time sqlStartTime = new Time(utilStartTime.getTime());

                java.util.Date utilEndTime = (java.util.Date) endTimeSpinner.getValue();
                Time sqlEndTime = new Time(utilEndTime.getTime());

                Showtime showtime = new Showtime(movieId, studioId, sqlDate, sqlStartTime, sqlEndTime);
                showtime.setId(showtimeId);

                if (showtimeDAO.updateShowtime(showtime)) {
                    JOptionPane.showMessageDialog(this, "Showtime berhasil diupdate!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadShowtimeData();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal update showtime!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih showtime yang akan diupdate!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteShowtime() {
        int selectedRow = showtimeTable.getSelectedRow();
        if (selectedRow >= 0) {
            int showtimeId = (int) tableModel.getValueAt(selectedRow, 0);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Apakah Anda yakin ingin menghapus showtime ini?",
                    "Konfirmasi Delete",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (showtimeDAO.deleteShowtime(showtimeId)) {
                    JOptionPane.showMessageDialog(this, "Showtime berhasil dihapus!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadShowtimeData();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menghapus showtime!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih showtime yang akan dihapus!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

        private void clearForm() {
        movieCombo.setSelectedIndex(0);
        studioCombo.setSelectedIndex(0);
        
        // Set date ke hari ini
        dateSpinner.setValue(new Date(System.currentTimeMillis()));
        
        // Set start time & end time ke waktu sekarang
        java.util.Date now = new java.util.Date();
        startTimeSpinner.setValue(now);
        endTimeSpinner.setValue(now);
        
        showtimeTable.clearSelection();
    }

    private boolean validateForm() {
        if (movieCombo.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Pilih movie terlebih dahulu!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (studioCombo.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Pilih studio terlebih dahulu!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (dateSpinner.getValue() == null) {
            JOptionPane.showMessageDialog(this, "Pilih tanggal showtime!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (startTimeSpinner.getValue() == null) {
            JOptionPane.showMessageDialog(this, "Pilih start time!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (endTimeSpinner.getValue() == null) {
            JOptionPane.showMessageDialog(this, "Pilih end time!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}