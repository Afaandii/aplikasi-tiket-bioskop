package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import dao.SeatDAO;
import dao.StudioDAO;
import models.Seat;
import models.Studio;

/**
 * SeatManagementPanel - panel manajemen kursi untuk aplikasi bioskop.
 * Versi ini bebas dependency eksternal dan sudah diperbaiki agar tidak
 * menimbulkan error/warning umum di IDE.
 */
public class SeatManagementPanel extends JPanel {
    private final Color primaryColor = new Color(41, 128, 185);
    private final Color backgroundColor = new Color(236, 240, 241);
    private final Color whiteColor = Color.WHITE;
    private final Color textColor = new Color(44, 62, 80);

    private JTable seatTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> studioCombo, statusCombo;
    private JTextField seatRowField, seatNumberField, seatCodeField;
    private int selectedSeatId = -1; // Untuk tracking ID kursi yang dipilih

    private SeatDAO seatDAO = new SeatDAO();
    private StudioDAO studioDAO = new StudioDAO();

    // Dropdown filter
    private JComboBox<String> filterStudioCombo;
    private List<Studio> allStudios; // Untuk menyimpan data studio dari DB

    public SeatManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(backgroundColor);

        initializeComponents();
        loadSeatData(); // Muat data awal
        loadStudioData();   // Muat data studio ke dropdown
    }

    private void initializeComponents() {
        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main split
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.7);
        splitPane.setBorder(null);

        splitPane.setLeftComponent(createTablePanel());
        splitPane.setRightComponent(createFormPanel());

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(whiteColor);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Seat Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(textColor);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(whiteColor);

        JButton refreshBtn = createActionButton("Refresh", new Color(52, 152, 219));
        refreshBtn.addActionListener(e -> loadSeatData());
        buttonPanel.add(refreshBtn);

        JButton generateBtn = createActionButton("Generate...", new Color(46, 204, 113));
        generateBtn.addActionListener(e -> showGenerateSeatsDialog());
        buttonPanel.add(generateBtn);

        headerPanel.add(buttonPanel, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(backgroundColor);
        panel.setBorder(new EmptyBorder(0, 20, 20, 10));

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(backgroundColor);
        JLabel filterLabel = new JLabel("Filter by Studio:");
        filterLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        filterPanel.add(filterLabel);

        // Dropdown untuk filter
        filterStudioCombo = new JComboBox<>();
        filterStudioCombo.addItem("All Studios");

        // Isi dropdown filter dengan data studio dari DB
        allStudios = studioDAO.getAllStudios(); // Simpan data studio
        for (Studio studio : allStudios) {
            String studioDisplay = studio.getNameStudio();
            filterStudioCombo.addItem(studioDisplay);
        }

        filterStudioCombo.setPreferredSize(new Dimension(200, 30));
        filterStudioCombo.addActionListener(e -> {
            String selected = (String) filterStudioCombo.getSelectedItem();
            if ("All Studios".equals(selected)) {
                loadSeatData();
            } else {
                // Cari studio_id berdasarkan nama studio
                int studioId = findStudioIdByName(selected);
                if (studioId > 0) {
                    loadSeatDataByStudio(studioId);
                }
            }
        });
        filterPanel.add(filterStudioCombo);
        panel.add(filterPanel, BorderLayout.NORTH);

        // Table model & table
        String[] columns = {"ID", "Studio", "Row", "Number", "Code", "Status", "Created At"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // tidak editable langsung
            }
        };

        seatTable = new JTable(tableModel);
        seatTable.setBackground(whiteColor);
        seatTable.setSelectionBackground(new Color(52, 152, 219, 50));
        seatTable.setRowHeight(30);
        seatTable.getTableHeader().setBackground(new Color(149, 165, 166));
        seatTable.getTableHeader().setForeground(whiteColor);
        seatTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        // Lebar kolom
        seatTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        seatTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        seatTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        seatTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        seatTable.getColumnModel().getColumn(4).setPreferredWidth(70);
        seatTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        seatTable.getColumnModel().getColumn(6).setPreferredWidth(150);

        // Pasang renderer status (kolom index 5)
        seatTable.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

        // Selection listener
        seatTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedSeat();
            }
        });

        panel.add(new JScrollPane(seatTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(whiteColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));
        panel.setPreferredSize(new Dimension(320, 0));

        JLabel formTitle = new JLabel("Seat Form");
        formTitle.setFont(new Font("Arial", Font.BOLD, 18));
        formTitle.setForeground(textColor);
        panel.add(formTitle, BorderLayout.NORTH);

        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setBackground(whiteColor);
        fields.setBorder(new EmptyBorder(20, 0, 20, 0));

        // components (gunakan ukuran prefered non-zero)
        fields.add(createLabeledComponent("Studio:", studioCombo = createStudioCombo()));
        fields.add(Box.createRigidArea(new Dimension(0, 12)));
        fields.add(createLabeledComponent("Seat Row (A-Z):", seatRowField = createTextField()));
        fields.add(Box.createRigidArea(new Dimension(0, 12)));
        fields.add(createLabeledComponent("Seat Number:", seatNumberField = createNumberField()));
        fields.add(Box.createRigidArea(new Dimension(0, 12)));
        fields.add(createLabeledComponent("Seat Code (e.g., A1):", seatCodeField = createTextField()));
        fields.add(Box.createRigidArea(new Dimension(0, 12)));
        fields.add(createLabeledComponent("Status:", statusCombo = createStatusCombo()));

        panel.add(fields, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(whiteColor);

        JButton addBtn = createActionButton("Add", new Color(46, 204, 113));
        addBtn.addActionListener(e -> addSeat());
        buttonPanel.add(addBtn);

        JButton updateBtn = createActionButton("Update", new Color(241, 196, 15));
        updateBtn.addActionListener(e -> updateSeat());
        buttonPanel.add(updateBtn);

        JButton deleteBtn = createActionButton("Delete", new Color(231, 76, 60));
        deleteBtn.addActionListener(e -> deleteSeat());
        buttonPanel.add(deleteBtn);

        JButton clearBtn = createActionButton("Clear", new Color(149, 165, 166));
        clearBtn.addActionListener(e -> clearForm());
        buttonPanel.add(clearBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createLabeledComponent(String labelText, JComponent comp) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(whiteColor);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setForeground(textColor);
        label.setHorizontalAlignment(SwingConstants.LEFT); // Rata kiri
        p.add(label);
        p.add(Box.createRigidArea(new Dimension(0, 6)));

        comp.setPreferredSize(new Dimension(200, 30));
        // wrap in a panel so BoxLayout respects preferred size
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); // Rata kiri
        wrapper.setBackground(whiteColor);
        wrapper.add(comp);
        p.add(wrapper);

        return p;
    }

    private JComboBox<String> createStudioCombo() {
        JComboBox<String> combo = new JComboBox<>();
        combo.addItem("-- Select Studio --");

        List<Studio> studios = studioDAO.getAllStudios();
        for (Studio studio : studios) {
            String studioDisplay = studio.getId() + " - " + studio.getNameStudio();
            combo.addItem(studioDisplay);
        }

        combo.setFont(new Font("Arial", Font.PLAIN, 14));
        combo.setPreferredSize(new Dimension(200, 30));
        return combo;
    }

    private JComboBox<String> createStatusCombo() {
        String[] statuses = {"Available", "Unavailable", "Booked"}; // Sesuai permintaan
        JComboBox<String> combo = new JComboBox<>(statuses);
        combo.setFont(new Font("Arial", Font.PLAIN, 14));
        combo.setPreferredSize(new Dimension(200, 30));
        return combo;
    }

    private JTextField createTextField() {
        JTextField f = new JTextField();
        f.setFont(new Font("Arial", Font.PLAIN, 14));
        f.setPreferredSize(new Dimension(200, 30));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return f;
    }

    private JFormattedTextField createNumberField() {
        JFormattedTextField f = new JFormattedTextField(new java.text.DecimalFormat("#"));
        f.setFont(new Font("Arial", Font.PLAIN, 14));
        f.setPreferredSize(new Dimension(200, 30));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return f;
    }

    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(whiteColor);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setPreferredSize(new Dimension(95, 34));
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

    /**
     * Cari studio_id berdasarkan nama studio (dari dropdown filter)
     */
    private int findStudioIdByName(String studioName) {
        if (allStudios == null) return 0;
        for (Studio studio : allStudios) {
            if (studio.getNameStudio().equals(studioName)) {
                return studio.getId();
            }
        }
        return 0;
    }

    private void loadSeatData() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);

        // Ambil semua data dari DAO
        List<Seat> seats = seatDAO.getAllSeats();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (Seat seat : seats) {
            Object[] row = {
                    seat.getId(),
                    seat.getStudioName(),   // Nama studio yang sudah di-join
                    seat.getSeatsRow(),
                    seat.getSeatsNumber(),
                    seat.getSeatsCode(),
                    seat.getStatus(),
                    seat.getCreatedAt() != null ? sdf.format(seat.getCreatedAt()) : ""
            };
            tableModel.addRow(row);
        }
    }

    private void loadSeatDataByStudio(int studioId) {
        if (tableModel == null) return;
        tableModel.setRowCount(0);

        // Ambil data berdasarkan studio_id
        List<Seat> seats = seatDAO.getSeatsByStudio(studioId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (Seat seat : seats) {
            Object[] row = {
                    seat.getId(),
                    seat.getStudioName(),   // Nama studio yang sudah di-join
                    seat.getSeatsRow(),
                    seat.getSeatsNumber(),
                    seat.getSeatsCode(),
                    seat.getStatus(),
                    seat.getCreatedAt() != null ? sdf.format(seat.getCreatedAt()) : ""
            };
            tableModel.addRow(row);
        }
    }

    private void loadStudioData() {
        // Isi dropdown studio
        studioCombo.removeAllItems();
        studioCombo.addItem("-- Select Studio --");

        List<Studio> studios = studioDAO.getAllStudios();
        for (Studio studio : studios) {
            String studioDisplay = studio.getId() + " - " + studio.getNameStudio();
            studioCombo.addItem(studioDisplay);
        }
    }

    private void loadSelectedSeat() {
        int sel = seatTable.getSelectedRow();
        if (sel < 0) {
            clearForm();
            selectedSeatId = -1;
            return;
        }

        // Ambil ID dari kolom pertama
        selectedSeatId = (int) tableModel.getValueAt(sel, 0);

        // Load data ke form
        Object studioNameObj = tableModel.getValueAt(sel, 1);
        if (studioNameObj != null) {
            String studioNameStr = studioNameObj.toString();
            for (int i = 0; i < studioCombo.getItemCount(); i++) {
                if (studioCombo.getItemAt(i).equals(studioNameStr)) {
                    studioCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        Object r = tableModel.getValueAt(sel, 2);
        seatRowField.setText(r == null ? "" : r.toString());

        Object num = tableModel.getValueAt(sel, 3);
        seatNumberField.setText(num == null ? "" : num.toString());

        Object code = tableModel.getValueAt(sel, 4);
        seatCodeField.setText(code == null ? "" : code.toString());

        Object status = tableModel.getValueAt(sel, 5);
        if (status != null) statusCombo.setSelectedItem(status.toString());
    }

    private void addSeat() {
        if (!validateForm()) return;

        try {
            // Parse Studio ID dari string dropdown
            String selectedStudio = (String) studioCombo.getSelectedItem();
            if (selectedStudio == null || selectedStudio.equals("-- Select Studio --")) {
                JOptionPane.showMessageDialog(this, "Pilih studio terlebih dahulu!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int studioId = Integer.parseInt(selectedStudio.split(" - ")[0]);

            char seatsRow = seatRowField.getText().trim().isEmpty() ? ' ' : seatRowField.getText().charAt(0);
            int seatsNumber = Integer.parseInt(seatNumberField.getText().trim());
            String seatsCode = seatCodeField.getText().trim();
            String status = (String) statusCombo.getSelectedItem();

            Seat newSeat = new Seat(studioId, seatsRow, seatsNumber, seatsCode, status);

            if (seatDAO.addSeat(newSeat)) {
                JOptionPane.showMessageDialog(this, "Seat berhasil ditambahkan!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadSeatData();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menambahkan seat.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Studio ID atau Seat Number harus angka!", "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSeat() {
        if (selectedSeatId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih seat yang akan diupdate!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateForm()) return;

        try {
            // Parse Studio ID dari string dropdown
            String selectedStudio = (String) studioCombo.getSelectedItem();
            if (selectedStudio == null || selectedStudio.equals("-- Select Studio --")) {
                JOptionPane.showMessageDialog(this, "Pilih studio terlebih dahulu!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int studioId = Integer.parseInt(selectedStudio.split(" - ")[0]);

            char seatsRow = seatRowField.getText().trim().isEmpty() ? ' ' : seatRowField.getText().charAt(0);
            int seatsNumber = Integer.parseInt(seatNumberField.getText().trim());
            String seatsCode = seatCodeField.getText().trim();
            String status = (String) statusCombo.getSelectedItem();

            Seat seatToUpdate = new Seat(studioId, seatsRow, seatsNumber, seatsCode, status);
            seatToUpdate.setId(selectedSeatId);

            if (seatDAO.updateSeat(seatToUpdate)) {
                JOptionPane.showMessageDialog(this, "Seat berhasil diupdate!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadSeatData();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal mengupdate seat.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Studio ID atau Seat Number harus angka!", "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSeat() {
        if (selectedSeatId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih seat yang akan dihapus!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus seat ini?",
                "Konfirmasi Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (seatDAO.deleteSeat(selectedSeatId)) {
                    JOptionPane.showMessageDialog(this, "Seat berhasil dihapus!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadSeatData();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menghapus seat.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Terjadi kesalahan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        studioCombo.setSelectedIndex(0);
        seatRowField.setText("");
        seatNumberField.setText("");
        seatCodeField.setText("");
        statusCombo.setSelectedIndex(0);
        selectedSeatId = -1;
        seatTable.clearSelection();
    }

    private void showGenerateSeatsDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Generate Seats", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(420, 320);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(16, 16, 16, 16));
        content.setBackground(whiteColor);

        JPanel studioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        studioPanel.setBackground(whiteColor);
        studioPanel.add(new JLabel("Studio:"));
        JComboBox<String> dialogStudioCombo = createStudioCombo();
        studioPanel.add(dialogStudioCombo);
        content.add(studioPanel);

        dialog.add(content, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.setBackground(whiteColor);
        JButton gen = createActionButton("Generate", new Color(46, 204, 113));
        gen.addActionListener(e -> {
            if (dialogStudioCombo.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(dialog, "Pilih studio!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                String selectedStudio = (String) dialogStudioCombo.getSelectedItem();
                int studioId = Integer.parseInt(selectedStudio.split(" - ")[0]);

                seatDAO.generateSeatsForStudio(studioId);

                JOptionPane.showMessageDialog(dialog, "Kursi berhasil digenerate!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadSeatData();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Studio ID harus angka!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Terjadi kesalahan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnPanel.add(gen);

        JButton cancel = createActionButton("Cancel", new Color(149, 165, 166));
        cancel.addActionListener(e -> dialog.dispose());
        btnPanel.add(cancel);

        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private boolean validateForm() {
        if (studioCombo.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Pilih studio terlebih dahulu!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (seatRowField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seat row tidak boleh kosong!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (seatNumberField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seat number tidak boleh kosong!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (seatCodeField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seat code tidak boleh kosong!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    // Custom renderer untuk kolom Status
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(CENTER);
            label.setOpaque(true);

            if (isSelected) {
                // gunakan warna seleksi tabel agar konsisten
                label.setBackground(table.getSelectionBackground());
                label.setForeground(table.getSelectionForeground());
            } else {
                String status = value == null ? "" : value.toString();
                switch (status) {
                    case "Available":
                        label.setBackground(new Color(46, 204, 113, 90));
                        label.setForeground(new Color(39, 174, 96));
                        break;
                    case "Booked":
                        label.setBackground(new Color(241, 196, 15, 90));
                        label.setForeground(new Color(212, 172, 13));
                        break;
                    case "Unavailable":
                        label.setBackground(new Color(231, 76, 60, 90));
                        label.setForeground(new Color(192, 57, 43));
                        break;
                    default:
                        label.setBackground(whiteColor);
                        label.setForeground(textColor);
                }
            }
            return label;
        }
    }
}