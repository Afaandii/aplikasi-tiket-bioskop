package gui;

import dao.StudioDAO;
import models.Studio;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class StudioManagementPanel extends JPanel {
    private final Color primaryColor = new Color(41, 128, 185);
    private final Color backgroundColor = new Color(236, 240, 241);
    private final Color whiteColor = Color.WHITE;
    private final Color textColor = new Color(44, 62, 80);

    private JTable studioTable;
    private DefaultTableModel tableModel;
    private JTextField nameField, capacityField;
    private JTextArea descriptionArea;

    private StudioDAO studioDAO = new StudioDAO();

    public StudioManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(backgroundColor);
        initializeComponents();
        loadStudioData();
    }

    private void initializeComponents() {
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main Content
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.7);
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

        JLabel titleLabel = new JLabel("Studio Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(textColor);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(whiteColor);

        JButton refreshBtn = createActionButton("Refresh", new Color(52, 152, 219));
        refreshBtn.addActionListener(e -> loadStudioData());
        buttonPanel.add(refreshBtn);

        headerPanel.add(buttonPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(backgroundColor);
        panel.setBorder(new EmptyBorder(0, 20, 20, 10));

        // Table
        String[] columns = {"ID", "Name", "Capacity", "Description", "Created At"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        studioTable = new JTable(tableModel);
        studioTable.setBackground(whiteColor);
        studioTable.setSelectionBackground(new Color(52, 152, 219, 50));
        studioTable.setRowHeight(30);
        studioTable.getTableHeader().setBackground(new Color(149, 165, 166));
        studioTable.getTableHeader().setForeground(whiteColor);
        studioTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        // Selection listener
        studioTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedStudio();
            }
        });

        JScrollPane scrollPane = new JScrollPane(studioTable);
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

        // Title
        JLabel formTitle = new JLabel("Studio Form");
        formTitle.setFont(new Font("Arial", Font.BOLD, 18));
        formTitle.setForeground(textColor);
        panel.add(formTitle, BorderLayout.NORTH);

        // Form Fields - Gunakan GridBagLayout untuk kontrol lebih baik
        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setBackground(whiteColor);
        formFields.setBorder(new EmptyBorder(20, 0, 20, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 10, 0); // Atur jarak antar komponen
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST; // Anchor ke kiri

        // Studio Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        JLabel nameLabel = new JLabel("Studio Name:");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        nameLabel.setForeground(textColor);
        formFields.add(nameLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        nameField = createTextField();
        formFields.add(nameField, gbc);

        // Capacity
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        JLabel capacityLabel = new JLabel("Capacity:");
        capacityLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        capacityLabel.setForeground(textColor);
        formFields.add(capacityLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        capacityField = createTextField();
        formFields.add(capacityField, gbc);

        // Description Label (dibuat left-aligned)
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        descLabel.setForeground(textColor);
        descLabel.setHorizontalAlignment(SwingConstants.LEFT); // Pastikan left-aligned
        formFields.add(descLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setFont(new Font("Arial", Font.PLAIN, 14));
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                new EmptyBorder(8, 8, 8, 8)
        ));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        formFields.add(descScroll, gbc);

        panel.add(formFields, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(whiteColor);

        JButton addBtn = createActionButton("Add", new Color(46, 204, 113));
        addBtn.addActionListener(e -> addStudio());
        buttonPanel.add(addBtn);

        JButton updateBtn = createActionButton("Update", new Color(241, 196, 15));
        updateBtn.addActionListener(e -> updateStudio());
        buttonPanel.add(updateBtn);

        JButton deleteBtn = createActionButton("Delete", new Color(231, 76, 60));
        deleteBtn.addActionListener(e -> deleteStudio());
        buttonPanel.add(deleteBtn);

        JButton clearBtn = createActionButton("Clear", new Color(149, 165, 166));
        clearBtn.addActionListener(e -> clearForm());
        buttonPanel.add(clearBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(200, 35)); // fix width
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
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(80, 35));
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

    private void loadStudioData() {
        tableModel.setRowCount(0);
        List<Studio> studios = studioDAO.getAllStudios();

        for (Studio s : studios) {
            tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getNameStudio(),
                    s.getCapacity(),
                    s.getDescription(),
                    s.getCreatedAt()
            });
        }
    }

    private void loadSelectedStudio() {
        int selectedRow = studioTable.getSelectedRow();
        if (selectedRow >= 0) {
            nameField.setText((String) tableModel.getValueAt(selectedRow, 1));
            capacityField.setText(String.valueOf(tableModel.getValueAt(selectedRow, 2)));
            descriptionArea.setText((String) tableModel.getValueAt(selectedRow, 3));
        }
    }

    private void addStudio() {
        if (validateForm()) {
            try {
                int capacity = Integer.parseInt(capacityField.getText().trim());

                Studio studio = new Studio(
                        nameField.getText().trim(),
                        capacity,
                        descriptionArea.getText().trim()
                );

                if (studioDAO.addStudio(studio)) {
                    JOptionPane.showMessageDialog(this, "Studio berhasil ditambahkan!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadStudioData();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menambahkan studio!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Capacity harus berupa angka!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateStudio() {
        int selectedRow = studioTable.getSelectedRow();
        if (selectedRow >= 0 && validateForm()) {
            try {
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                int capacity = Integer.parseInt(capacityField.getText().trim());

                Studio studio = new Studio(
                        nameField.getText().trim(),
                        capacity,
                        descriptionArea.getText().trim()
                );
                studio.setId(id);

                if (studioDAO.updateStudio(studio)) {
                    JOptionPane.showMessageDialog(this, "Studio berhasil diupdate!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadStudioData();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal update studio!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Capacity harus berupa angka!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih studio yang akan diupdate!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteStudio() {
        int selectedRow = studioTable.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Apakah Anda yakin ingin menghapus studio ini?",
                    "Konfirmasi Delete",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (studioDAO.deleteStudio(id)) {
                    JOptionPane.showMessageDialog(this, "Studio berhasil dihapus!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadStudioData();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menghapus studio!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih studio yang akan dihapus!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void clearForm() {
        nameField.setText("");
        capacityField.setText("");
        descriptionArea.setText("");
        studioTable.clearSelection();
    }

    private boolean validateForm() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Studio name tidak boleh kosong!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (capacityField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Capacity tidak boleh kosong!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (descriptionArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Description tidak boleh kosong!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}