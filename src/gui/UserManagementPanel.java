package gui;

import dao.UserDAO;
import models.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.util.List;
import utils.DatabaseConnection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserManagementPanel extends JPanel {
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JTextField usernameField, emailField;           // ✅ Hanya untuk username & email
    private JPasswordField passwordField;                   // ✅ Tipe benar untuk password
    private JComboBox<String> roleComboBox;
    private JButton addButton, updateButton, deleteButton, clearButton, refreshButton;
    private UserDAO userDAO;
    private int selectedUserId = -1;

    public UserManagementPanel() {
        userDAO = new UserDAO();
        initializeComponents();
        loadUsers();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Title Panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("User Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        // Button Panel (Refresh)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadUsers());
        buttonPanel.add(refreshButton);
        titlePanel.add(buttonPanel, BorderLayout.EAST);
        add(titlePanel, BorderLayout.NORTH);

        // Main Content Panel (Split: Table & Form)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(500);
        splitPane.setContinuousLayout(true);

        // Tabel User
        String[] columnNames = {"ID", "Username", "Email", "Role", "Created At", "Updated At"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = userTable.getSelectedRow();
                if (selectedRow != -1) {
                    selectedUserId = (int) tableModel.getValueAt(selectedRow, 0);
                    loadUserToForm(selectedUserId);
                }
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(userTable);
        splitPane.setLeftComponent(tableScrollPane);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Role
        JLabel roleLabel = new JLabel("Role:");
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(roleLabel, gbc);
        roleComboBox = new JComboBox<>();
        populateRoleComboBox();
        gbc.gridx = 1;
        formPanel.add(roleComboBox, gbc);
        row++;

        // Username
        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(usernameLabel, gbc);
        usernameField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);
        row++;

        // Email
        JLabel emailLabel = new JLabel("Email:");
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(emailLabel, gbc);
        emailField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);
        row++;

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(passwordLabel, gbc);
        passwordField = new JPasswordField(20); // ✅ Benar
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);
        row++;

        // Tombol CRUD
        JPanel buttonCRUDPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        buttonCRUDPanel.setBackground(Color.WHITE);
        addButton = createModernButton("Add", new Color(149, 165, 166));
        updateButton = createModernButton("Update", new Color(241, 196, 15));
        deleteButton = createModernButton("Delete", new Color(231, 76, 60));
        clearButton = createModernButton("Clear", new Color(52, 152, 219));
        buttonCRUDPanel.add(addButton);
        buttonCRUDPanel.add(updateButton);
        buttonCRUDPanel.add(deleteButton);
        buttonCRUDPanel.add(clearButton);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        formPanel.add(buttonCRUDPanel, gbc);

        splitPane.setRightComponent(formPanel);
        add(splitPane, BorderLayout.CENTER);

        // Action Listeners
        addButton.addActionListener(e -> addUser());
        updateButton.addActionListener(e -> updateUser());
        deleteButton.addActionListener(e -> deleteUser());
        clearButton.addActionListener(e -> clearForm());
    }

    private void populateRoleComboBox() {
        roleComboBox.removeAllItems();
        try {
            String query = "SELECT id, role_name FROM roles ORDER BY id";
            ResultSet rs = DatabaseConnection.executeQuery(query);
            while (rs.next()) {
                int roleId = rs.getInt("id");
                String roleName = rs.getString("role_name");
                roleComboBox.addItem(roleName + " (ID: " + roleId + ")");
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error loading roles: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Gagal memuat daftar role.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getSelectedRoleId() {
        String selectedItem = (String) roleComboBox.getSelectedItem();
        if (selectedItem != null) {
            int startIndex = selectedItem.indexOf("(ID: ") + 5;
            int endIndex = selectedItem.indexOf(")");
            if (startIndex > 0 && endIndex > startIndex) {
                try {
                    return Integer.parseInt(selectedItem.substring(startIndex, endIndex));
                } catch (NumberFormatException ex) {
                    System.err.println("Error parsing role ID: " + ex.getMessage());
                }
            }
        }
        return 0;
    }

    private void loadUsers() {
        tableModel.setRowCount(0);
        List<User> users = userDAO.getAllUsers();
        for (User user : users) {
            try {
                String roleName = userDAO.getRoleNameByRoleId(user.getRoleId());
                Object[] rowData = {
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    roleName,
                    user.getCreatedAt(),
                    user.getUpdatedAt()
                };
                tableModel.addRow(rowData);
            } catch (Exception e) {
                System.err.println("Error loading user  " + e.getMessage());
            }
        }
    }

    private void loadUserToForm(int userId) {
        User user = userDAO.getUserById(userId);
        if (user != null) {
            usernameField.setText(user.getUsername());
            emailField.setText(user.getEmail());
            passwordField.setText(""); // Kosongkan password field

            try {
                String roleName = userDAO.getRoleNameByRoleId(user.getRoleId());
                for (int i = 0; i < roleComboBox.getItemCount(); i++) {
                    String item = (String) roleComboBox.getItemAt(i);
                    if (item.startsWith(roleName)) {
                        roleComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error setting role in combo box: " + e.getMessage());
            }
        }
    }

    private void addUser() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        int roleId = getSelectedRoleId();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || roleId == 0) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (userDAO.isUsernameExists(username)) {
            JOptionPane.showMessageDialog(this, "Username sudah digunakan!", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (userDAO.isEmailExists(email)) {
            JOptionPane.showMessageDialog(this, "Email sudah digunakan!", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User newUser = new User(username, email, password);
        newUser.setRoleId(roleId);

        if (userDAO.registerUser(newUser)) {
            JOptionPane.showMessageDialog(this, "User berhasil ditambahkan!");
            clearForm();
            loadUsers();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menambahkan user.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUser() {
        if (selectedUserId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih user yang ingin diupdate dari tabel!", "Perhatian", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        int roleId = getSelectedRoleId();

        if (username.isEmpty() || email.isEmpty() || roleId == 0) {
            JOptionPane.showMessageDialog(this, "Username, Email, dan Role harus diisi!", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (userDAO.isUsernameExists(username) && !isUserSameUsername(selectedUserId, username)) {
            JOptionPane.showMessageDialog(this, "Username sudah digunakan!", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (userDAO.isEmailExists(email) && !isUserSameEmail(selectedUserId, email)) {
            JOptionPane.showMessageDialog(this, "Email sudah digunakan!", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User updatedUser = new User();
        updatedUser.setId(selectedUserId);
        updatedUser.setUsername(username);
        updatedUser.setEmail(email);
        updatedUser.setRoleId(roleId);

        if (!password.isEmpty()) {
            updatedUser.setPassword(password);
        }

        if (userDAO.updateProfile(updatedUser)) {
            if (!password.isEmpty()) {
                userDAO.updatePassword(selectedUserId, password);
            }
            JOptionPane.showMessageDialog(this, "User berhasil diperbarui!");
            clearForm();
            selectedUserId = -1;
            loadUsers();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal memperbarui user.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isUserSameUsername(int userId, String username) {
        User user = userDAO.getUserById(userId);
        return user != null && user.getUsername().equals(username);
    }

    private boolean isUserSameEmail(int userId, String email) {
        User user = userDAO.getUserById(userId);
        return user != null && user.getEmail().equals(email);
    }

    private void deleteUser() {
        if (selectedUserId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih user yang ingin dihapus dari tabel!", "Perhatian", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Apakah Anda yakin ingin menghapus user ini?",
            "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (userDAO.deleteAccount(selectedUserId)) {
                JOptionPane.showMessageDialog(this, "User berhasil dihapus!");
                clearForm();
                selectedUserId = -1;
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        usernameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        roleComboBox.setSelectedIndex(0);
        selectedUserId = -1;
    }

    private JButton createModernButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(100, 30));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Custom render warna & hover
        button = new JButton(text) {
            private Color baseColor = bgColor;
            private Color hoverColor = bgColor.brighter();
            private boolean hover = false;

            {
                setForeground(Color.WHITE);
                setFont(new Font("Arial", Font.BOLD, 12));
                setFocusPainted(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        hover = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hover = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? hoverColor : baseColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        return button;
    }
}