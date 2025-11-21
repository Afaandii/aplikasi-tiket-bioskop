package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginPage extends JPanel {
    private final Color primaryColor = new Color(74, 144, 226);
    private final Color accentColor = new Color(255, 87, 87);
    private final Color backgroundColor = new Color(15, 23, 42);
    private final Color textColor = new Color(226, 232, 240);
    private final Color placeholderColor = new Color(148, 163, 184);

    public LoginPage(CinemaApp app) {
        setLayout(new BorderLayout());
        setBackground(backgroundColor);

        // Background
        JPanel background = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp1 = new GradientPaint(
                        0, 0, backgroundColor,
                        getWidth(), getHeight(), new Color(30, 41, 59)
                );
                g2d.setPaint(gp1);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.setColor(new Color(255, 255, 255, 5));
                for (int i = 0; i < getWidth(); i += 60) {
                    for (int j = 0; j < getHeight(); j += 60) {
                        g2d.fillOval(i, j, 2, 2);
                    }
                }

                g2d.dispose();
            }
        };
        background.setLayout(new GridBagLayout());
        add(background, BorderLayout.CENTER);

        // Card
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(255, 255, 255, 10));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2d.setColor(new Color(255, 255, 255, 30));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                g2d.dispose();
            }
        };
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(450, 450));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        // Logo Icon
        ImageIcon logoIcon = new ImageIcon(getClass().getResource("/resource/TiketID.png"));

        Image img = logoIcon.getImage();
        Image scaledImg = img.getScaledInstance(200, 120, Image.SCALE_SMOOTH);
        logoIcon = new ImageIcon(scaledImg);

        JLabel logoLabel = new JLabel(logoIcon, JLabel.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 5, 0);
        card.add(logoLabel, gbc);

        JLabel title = new JLabel("Cinema Sign", JLabel.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(textColor);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(title, gbc);

        // username field 
        JTextField usernameField = createModernTextField("Enter your username");
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(usernameField, gbc);

        // Password field
        JPasswordField passField = createModernPasswordField("Enter your password");
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 30, 0);
        card.add(passField, gbc);

        // Login button
        JButton loginBtn = createModernButton("Sign In", 120);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        card.add(loginBtn, gbc);

        // Action login check ke database (ubah logic untuk username)
        loginBtn.addActionListener(e -> {
        String username = usernameField.getText().trim(); 
        String password = new String(passField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Username dan password tidak boleh kosong!",
                    "Login Gagal", JOptionPane.ERROR_MESSAGE);
            return;
        }

//        if (!isValidUsername(username)) {
//            JOptionPane.showMessageDialog(this,
//                    "Format username tidak valid!",
//                    "Login Gagal", JOptionPane.ERROR_MESSAGE);
//            return;
//        }

        // Di dalam ActionListener tombol login
try {
    dao.UserDAO userDAO = new dao.UserDAO();
    models.User user = userDAO.loginUser(username, password);

    if (user != null) {
        // Dapatkan nama role dari roleId
        String roleName = userDAO.getRoleNameByRoleId(user.getRoleId());

        JOptionPane.showMessageDialog(this,
                "Login sukses! Selamat datang, " + user.getUsername(),
                "Berhasil", JOptionPane.INFORMATION_MESSAGE);

        // simpan user yang login
        app.setLoggedInUser(user);

        // cek role berdasarkan nama role
        if ("admin".equalsIgnoreCase(roleName)) {
            // arahkan ke dashboard admin
            app.showPage(new AdminDashboard(app, user));
        } else if ("cashier".equalsIgnoreCase(roleName)) {
            // arahkan ke halaman pemilihan film
            app.showPage(new MovieSelectionPage(app));
        } else {
            // Role lain, bisa ditangani sesuai kebutuhan
            JOptionPane.showMessageDialog(this,
                    "Role tidak dikenali: " + roleName,
                    "Login Gagal", JOptionPane.ERROR_MESSAGE);
        }

    } else {
        JOptionPane.showMessageDialog(this,
                "Username atau password salah!", 
                "Login Gagal", JOptionPane.ERROR_MESSAGE);
    }
} catch (Exception ex) {
    ex.printStackTrace();
    JOptionPane.showMessageDialog(this,
            "Terjadi kesalahan koneksi database.",
            "Error", JOptionPane.ERROR_MESSAGE);
}
    });

        // Enter key support
        passField.addActionListener(e -> loginBtn.doClick());
        usernameField.addActionListener(e -> passField.requestFocus()); 

        background.add(card, new GridBagConstraints());
    }

    // Method untuk validasi usename sederhana
    private boolean isValidUsername(String username) {
        return username.contains("@") && username.contains(".");
    }

    private JTextField createModernTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(51, 65, 85));
                g2d.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);

                if (hasFocus()) {
                    g2d.setColor(primaryColor);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                }

                g2d.dispose();
                super.paintComponent(g);
            }
        };

        field.setPreferredSize(new Dimension(350, 50));
        field.setFont(new Font("SansSerif", Font.PLAIN, 16));
        field.setForeground(textColor);
        field.setCaretColor(primaryColor);
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Placeholder untuk username
        addPlaceholder(field, placeholder);

        return field;
    }

    private JPasswordField createModernPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(51, 65, 85));
                g2d.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);

                if (hasFocus()) {
                    g2d.setColor(primaryColor);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                }

                g2d.dispose();
                super.paintComponent(g);
            }
        };

        field.setPreferredSize(new Dimension(350, 50));
        field.setFont(new Font("SansSerif", Font.PLAIN, 16));
        field.setForeground(textColor);
        field.setCaretColor(primaryColor);
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        field.setEchoChar((char) 0); // Non-echo karakter untuk placeholder

        // Tambahkan placeholder untuk password field
        addPasswordPlaceholder(field, placeholder);

        return field;
    }

    private void addPlaceholder(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(placeholderColor);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(textColor);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(placeholderColor);
                }
            }
        });
    }
    
    private void addPasswordPlaceholder(JPasswordField field, String placeholder) {
        field.setEchoChar((char) 0); // Non-echo karakter untuk placeholder
        field.setText(placeholder);
        field.setForeground(placeholderColor);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (new String(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setEchoChar('●'); // Kembali ke echo karakter normal
                    field.setForeground(textColor);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getPassword().length == 0) {
                    field.setEchoChar((char) 0); // Non-echo karakter untuk placeholder
                    field.setText(placeholder);
                    field.setForeground(placeholderColor);
                }
            }
        });
    }

    private JButton createModernButton(String text, int height) {
        return new JButton(text) {
            private boolean isHovered = false;
            private boolean isPressed = false;

            {
                setPreferredSize(new Dimension(350, height));
                setFont(new Font("SansSerif", Font.BOLD, 16));
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        isHovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        isHovered = false;
                        repaint();
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        isPressed = true;
                        repaint();
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        isPressed = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bgColor;
                if (isPressed) {
                    bgColor = new Color(59, 130, 246);
                } else if (isHovered) {
                    bgColor = new Color(96, 165, 250);
                } else {
                    bgColor = primaryColor;
                }

                int r = Math.max(bgColor.getRed() - 20, 0);
                int gVal = Math.max(bgColor.getGreen() - 20, 0);
                int b = Math.max(bgColor.getBlue() - 20, 0);

                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.fillRoundRect(2, 4, getWidth() - 2, getHeight() - 2, 12, 12);

                GradientPaint gp = new GradientPaint(
                        0, 0, bgColor,
                        0, getHeight(), new Color(r, gVal, b)
                );
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 4, 12, 12);

                g2d.setColor(Color.WHITE);
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(text)) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2 - 2;
                g2d.drawString(text, textX, textY);

                g2d.dispose();
            }
        };
    }
//     public static void main(String[] args) {
//    SwingUtilities.invokeLater(() -> {
//        // Dummy app biar tidak null
//        CinemaApp app = new CinemaApp();
//
//        // Jalankan loading screen
//        LoadingScreen loadingScreen = new LoadingScreen(() -> {
//            // Callback setelah loading selesai
//            JFrame frame = new JFrame("Login Page - TiketID");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setContentPane(new LoginPage(app));
//            frame.pack();
//            frame.setLocationRelativeTo(null);
//            frame.setVisible(true);
//        });
//
//        loadingScreen.startLoading();
//    });
//}

}