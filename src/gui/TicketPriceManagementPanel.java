package gui;

import dao.TicketPriceDAO;
import models.TicketPrice;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TicketPriceManagementPanel extends JPanel {
    private final Color primaryColor = new Color(41, 128, 185);
    private final Color backgroundColor = new Color(236, 240, 241);
    private final Color whiteColor = Color.WHITE;
    private final Color textColor = new Color(44, 62, 80);

    private JTable ticketPriceTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> showtimeCombo;
    private JTextField daysField, priceField;
    private JSpinner dateSpinner;

    private TicketPriceDAO ticketPriceDAO = new TicketPriceDAO();

    public TicketPriceManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(backgroundColor);
        initializeComponents();
        loadTicketPriceData();
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

        JLabel titleLabel = new JLabel("Ticket Price Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(textColor);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(whiteColor);

        JButton refreshBtn = createActionButton("Refresh", new Color(52, 152, 219));
        refreshBtn.addActionListener(e -> loadTicketPriceData());
        buttonPanel.add(refreshBtn);

        headerPanel.add(buttonPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(backgroundColor);
        panel.setBorder(new EmptyBorder(0, 20, 20, 10));

        String[] columns = {"ID", "Showtime", "Days", "Price", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        ticketPriceTable = new JTable(tableModel);
        ticketPriceTable.setBackground(whiteColor);
        ticketPriceTable.setSelectionBackground(new Color(52, 152, 219, 50));
        ticketPriceTable.setRowHeight(35);
        ticketPriceTable.getTableHeader().setBackground(new Color(149, 165, 166));
        ticketPriceTable.getTableHeader().setForeground(whiteColor);
        ticketPriceTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        // Column widths
        ticketPriceTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        ticketPriceTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        ticketPriceTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        ticketPriceTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        ticketPriceTable.getColumnModel().getColumn(4).setPreferredWidth(100);

        // Selection listener
        ticketPriceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedTicketPrice();
            }
        });

        JScrollPane scrollPane = new JScrollPane(ticketPriceTable);
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

        JLabel formTitle = new JLabel("Ticket Price Form");
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

        // Showtime Combo
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        JLabel showtimeLabel = new JLabel("Showtime:");
        showtimeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        showtimeLabel.setForeground(textColor);
        formFields.add(showtimeLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        showtimeCombo = createShowtimeCombo();
        formFields.add(showtimeCombo, gbc);

        // Days Field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        JLabel daysLabel = new JLabel("Days (e.g., Weekday, Weekend):");
        daysLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        daysLabel.setForeground(textColor);
        formFields.add(daysLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        daysField = createTextField();
        formFields.add(daysField, gbc);

        // Price Field
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        JLabel priceLabel = new JLabel("Price (Rp):");
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        priceLabel.setForeground(textColor);
        formFields.add(priceLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        priceField = createTextField();
        formFields.add(priceField, gbc);

        // Date Field
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0.0;
        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        dateLabel.setForeground(textColor);
        formFields.add(dateLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.weightx = 1.0;
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setFont(new Font("Arial", Font.PLAIN, 14));
        dateSpinner.setPreferredSize(new Dimension(0, 35));
        // Set nilai default ke hari ini
        dateSpinner.setValue(new java.util.Date());
        formFields.add(dateSpinner, gbc);

        panel.add(formFields, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(whiteColor);

        JButton addBtn = createActionButton("Add", new Color(46, 204, 113));
        addBtn.addActionListener(e -> addTicketPrice());
        buttonPanel.add(addBtn);

        JButton updateBtn = createActionButton("Update", new Color(241, 196, 15));
        updateBtn.addActionListener(e -> updateTicketPrice());
        buttonPanel.add(updateBtn);

        JButton deleteBtn = createActionButton("Delete", new Color(231, 76, 60));
        deleteBtn.addActionListener(e -> deleteTicketPrice());
        buttonPanel.add(deleteBtn);

        JButton clearBtn = createActionButton("Clear", new Color(149, 165, 166));
        clearBtn.addActionListener(e -> clearForm());
        buttonPanel.add(clearBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JComboBox<String> createShowtimeCombo() {
        JComboBox<String> combo = new JComboBox<>();
        combo.addItem("-- Select Showtime --");
        List<String> showtimes = ticketPriceDAO.getShowtimeList();
        for (String showtime : showtimes) {
            combo.addItem(showtime);
        }
        combo.setFont(new Font("Arial", Font.PLAIN, 14));
        combo.setPreferredSize(new Dimension(0, 35));
        return combo;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(0, 35));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(whiteColor);
        button.setFont(new Font("Arial", Font.BOLD, 13)); // lebih besar sedikit
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Lebar tombol menyesuaikan teks
        int textWidth = button.getFontMetrics(button.getFont()).stringWidth(text);
        int padding = 40; // jarak kiri-kanan biar lega
        int buttonWidth = textWidth + padding;
        button.setPreferredSize(new Dimension(buttonWidth, 40)); // tinggi 40 biar proporsional

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

    private void loadTicketPriceData() {
        tableModel.setRowCount(0);
        List<TicketPrice> ticketPrices = ticketPriceDAO.getAllTicketPrices();

        for (TicketPrice tp : ticketPrices) {
            tableModel.addRow(new Object[]{
                    tp.getId(),
                    tp.getShowtimeInfo(),
                    tp.getDays(),
                    tp.getPrice(),
                    tp.getDate(),
            });
        }
    }

    private void loadSelectedTicketPrice() {
        int selectedRow = ticketPriceTable.getSelectedRow();
        if (selectedRow >= 0) {
            int showtimeId = (int) tableModel.getValueAt(selectedRow, 0); // ID bukan showtimeInfo

            // Cari string combo yang cocok berdasarkan ID
            String showtimeItem = findShowtimeItemById(showtimeId);
            if (showtimeItem != null) {
                showtimeCombo.setSelectedItem(showtimeItem);
            }

            daysField.setText((String) tableModel.getValueAt(selectedRow, 2));
            priceField.setText(String.valueOf(tableModel.getValueAt(selectedRow, 3)));

            // Date
            Date date = (Date) tableModel.getValueAt(selectedRow, 4);
            dateSpinner.setValue(date);
        }
    }

    private String findShowtimeItemById(int showtimeId) {
        for (int i = 0; i < showtimeCombo.getItemCount(); i++) {
            String item = (String) showtimeCombo.getItemAt(i);
            if (item.startsWith(String.valueOf(showtimeId) + " - ")) {
                return item;
            }
        }
        return null;
    }

    private void addTicketPrice() {
        if (validateForm()) {
            try {
                int showtimeId = TicketPriceDAO.extractIdFromCombo((String) showtimeCombo.getSelectedItem());

                String days = daysField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());

                // Konversi java.util.Date dari JSpinner ke java.sql.Date
                java.util.Date utilDate = (java.util.Date) dateSpinner.getValue();
                Date sqlDate = new Date(utilDate.getTime());

                TicketPrice ticketPrice = new TicketPrice(showtimeId, days, price, sqlDate);

                int newId = ticketPriceDAO.addTicketPrice(ticketPrice);
                if (newId > 0) {
                    JOptionPane.showMessageDialog(this, "Ticket price berhasil ditambahkan!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadTicketPriceData();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menambahkan ticket price!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Price harus berupa angka!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateTicketPrice() {
        int selectedRow = ticketPriceTable.getSelectedRow();
        if (selectedRow >= 0 && validateForm()) {
            try {
                int ticketPriceId = (int) tableModel.getValueAt(selectedRow, 0);
                int showtimeId = TicketPriceDAO.extractIdFromCombo((String) showtimeCombo.getSelectedItem());

                String days = daysField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());

                java.util.Date utilDate = (java.util.Date) dateSpinner.getValue();
                Date sqlDate = new Date(utilDate.getTime());

                TicketPrice ticketPrice = new TicketPrice(showtimeId, days, price, sqlDate);
                ticketPrice.setId(ticketPriceId);

                if (ticketPriceDAO.updateTicketPrice(ticketPrice)) {
                    JOptionPane.showMessageDialog(this, "Ticket price berhasil diupdate!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadTicketPriceData();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal update ticket price!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Price harus berupa angka!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih ticket price yang akan diupdate!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteTicketPrice() {
        int selectedRow = ticketPriceTable.getSelectedRow();
        if (selectedRow >= 0) {
            int ticketPriceId = (int) tableModel.getValueAt(selectedRow, 0);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Apakah Anda yakin ingin menghapus ticket price ini?",
                    "Konfirmasi Delete",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (ticketPriceDAO.deleteTicketPrice(ticketPriceId)) {
                    JOptionPane.showMessageDialog(this, "Ticket price berhasil dihapus!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadTicketPriceData();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menghapus ticket price!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih ticket price yang akan dihapus!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void clearForm() {
        showtimeCombo.setSelectedIndex(0);
        daysField.setText("");
        priceField.setText("");
        dateSpinner.setValue(new java.util.Date());
        ticketPriceTable.clearSelection();
    }

    private boolean validateForm() {
        if (showtimeCombo.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Pilih showtime terlebih dahulu!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (daysField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Days tidak boleh kosong!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (priceField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Price tidak boleh kosong!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            Double.parseDouble(priceField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Price harus berupa angka!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (dateSpinner.getValue() == null) {
            JOptionPane.showMessageDialog(this, "Pilih tanggal ticket price!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}