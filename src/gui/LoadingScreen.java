package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

public class LoadingScreen extends JWindow {
    private static final long serialVersionUID = 1L;

    // Tema warna cinema yang elegan dengan gradient
    private static final Color PRIMARY_COLOR = new Color(255, 87, 87);       // Coral red
    private static final Color SECONDARY_COLOR = new Color(126, 87, 194);    // Purple
    private static final Color ACCENT_COLOR = new Color(255, 206, 84);       // Gold
    private static final Color DARK_BG = new Color(26, 26, 46);              // Dark purple-blue
    private static final Color LIGHT_BG = new Color(44, 44, 84);             // Medium purple-blue
    private static final Color TEXT_COLOR = new Color(255, 255, 255);        // Pure white
    private static final Color PROGRESS_TRACK = new Color(45, 45, 65);       // Dark track

    private JProgressBar progressBar;
    private Timer timer;
    private int progress = 0;
    private CinemaApp app;
    
    private Runnable onFinish;

    public LoadingScreen(Runnable onFinish) {
        this.onFinish = onFinish;
        initializeComponents();
        setupAnimation();
    }

    private void initializeComponents() {
        setSize(500, 380);  // Tinggi diperbesar dari 320 ke 380
        setLocationRelativeTo(null);

        // Panel utama dengan background gradient cinema theme
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Multi-gradient background yang lebih menarik
                // Layer 1: Base gradient
                GradientPaint baseGradient = new GradientPaint(
                        0, 0, DARK_BG,
                        0, getHeight(), LIGHT_BG
                );
                g2d.setPaint(baseGradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Layer 2: Diagonal accent gradient
                GradientPaint accentGradient = new GradientPaint(
                        0, 0, new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), 
                                      PRIMARY_COLOR.getBlue(), 30),
                        getWidth(), getHeight(), new Color(SECONDARY_COLOR.getRed(), 
                                                         SECONDARY_COLOR.getGreen(), 
                                                         SECONDARY_COLOR.getBlue(), 20)
                );
                g2d.setPaint(accentGradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Outer glow border
                g2d.setColor(new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(), 
                           ACCENT_COLOR.getBlue(), 120));
                g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.draw(new RoundRectangle2D.Float(3, 3, getWidth() - 6, getHeight() - 6, 25, 25));

                // Inner subtle border
                g2d.setColor(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), 
                           PRIMARY_COLOR.getBlue(), 60));
                g2d.setStroke(new BasicStroke(1));
                g2d.draw(new RoundRectangle2D.Float(6, 6, getWidth() - 12, getHeight() - 12, 20, 20));

                g2d.dispose();
            }
        };
        mainPanel.setLayout(new BorderLayout(0, 35));  // Spacing diperbesar
        mainPanel.setBorder(BorderFactory.createEmptyBorder(45, 50, 45, 50));  // Padding diperbesar

        // Header dengan logo dan judul
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center dengan progress bar
        JPanel centerPanel = createCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Membuat window rounded
        setShape(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 25, 25));
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        // Logo
        ImageIcon icon = new ImageIcon(getClass().getResource("/resource/TiketID.png"));
        Image scaled = icon.getImage().getScaledInstance(180, 85, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaled));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Judul aplikasi dengan efek shadow
        JLabel titleLabel = new JLabel("TiketID") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                                   RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                // Shadow effect
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2;
                g2d.drawString(getText(), x + 2, y + 2);
                
                // Main text with beautiful gradient
                GradientPaint textGradient = new GradientPaint(
                    x, y - fm.getAscent(), ACCENT_COLOR,
                    x, y, new Color(255, 255, 255, 240)
                );
                g2d.setPaint(textGradient);
                g2d.drawString(getText(), x, y);
                
                g2d.dispose();
            }
        };
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setPreferredSize(new Dimension(200, 40));

        headerPanel.add(logoLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(titleLabel);

        return headerPanel;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Custom progress bar dengan tinggi yang lebih proporsional
        progressBar = new JProgressBar(0, 100) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();

                // Background track dengan shadow yang lebih dalam
                g2d.setColor(new Color(0, 0, 0, 120));
                g2d.fill(new RoundRectangle2D.Float(3, 3, width - 3, height - 3, 18, 18));
                
                g2d.setColor(PROGRESS_TRACK);
                g2d.fill(new RoundRectangle2D.Float(0, 0, width, height, 18, 18));

                // Progress fill dengan gradient yang cantik
                if (getValue() > 0) {
                    double progressPercent = getValue() / 100.0;
                    int progressWidth = (int) (width * progressPercent);
                    
                    if (progressWidth > 10) {
                        // Multi-layer gradient untuk efek yang lebih rich
                        GradientPaint progressGradient = new GradientPaint(
                            0, 0, PRIMARY_COLOR,
                            progressWidth, height, SECONDARY_COLOR
                        );
                        g2d.setPaint(progressGradient);
                        g2d.fill(new RoundRectangle2D.Float(4, 4, progressWidth - 8, height - 8, 14, 14));
                        
                        // Top highlight untuk efek glossy
                        GradientPaint highlightGradient = new GradientPaint(
                            0, 4, new Color(255, 255, 255, 150),
                            0, height/2, new Color(255, 255, 255, 30)
                        );
                        g2d.setPaint(highlightGradient);
                        g2d.fill(new RoundRectangle2D.Float(4, 4, progressWidth - 8, (height - 8)/2, 14, 14));
                        
                        // Accent border pada progress
                        g2d.setColor(new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(), 
                                             ACCENT_COLOR.getBlue(), 100));
                        g2d.setStroke(new BasicStroke(1.5f));
                        g2d.draw(new RoundRectangle2D.Float(4, 4, progressWidth - 8, height - 8, 14, 14));
                    }
                }

                // Outer border yang elegan
                g2d.setColor(new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(), 
                           ACCENT_COLOR.getBlue(), 180));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.draw(new RoundRectangle2D.Float(0.5f, 0.5f, width - 1, height - 1, 18, 18));

                // Text dengan shadow yang lebih halus
                if (isStringPainted() && getString() != null) {
                    g2d.setFont(getFont());
                    FontMetrics fm = g2d.getFontMetrics();
                    String text = getString();
                    int textWidth = fm.stringWidth(text);
                    int x = (width - textWidth) / 2;
                    int y = (height - fm.getHeight()) / 2 + fm.getAscent();
                    
                    // Multiple shadow layers untuk depth
                    g2d.setColor(new Color(0, 0, 0, 180));
                    g2d.drawString(text, x + 2, y + 2);
                    g2d.setColor(new Color(0, 0, 0, 100));
                    g2d.drawString(text, x + 1, y + 1);
                    
                    // Main text dengan slight glow
                    g2d.setColor(TEXT_COLOR);
                    g2d.drawString(text, x, y);
                    
                    // Subtle glow effect
                    g2d.setColor(new Color(TEXT_COLOR.getRed(), TEXT_COLOR.getGreen(), 
                                         TEXT_COLOR.getBlue(), 50));
                    g2d.drawString(text, x - 1, y);
                    g2d.drawString(text, x + 1, y);
                }

                g2d.dispose();
            }
        };
        
        progressBar.setStringPainted(true);
        progressBar.setString("0%");
        progressBar.setFont(new Font("SansSerif", Font.BOLD, 14));
        progressBar.setOpaque(false);
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(400, 35));  // Tinggi diperbesar ke 35
        progressBar.setMaximumSize(new Dimension(400, 35));

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(progressBar);
        centerPanel.add(Box.createVerticalGlue());

        return centerPanel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel();
        footerPanel.setOpaque(false);
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));

        JLabel versionLabel = new JLabel("Version 1.0.0");
        versionLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        versionLabel.setForeground(new Color(TEXT_COLOR.getRed(), TEXT_COLOR.getGreen(), 
                                           TEXT_COLOR.getBlue(), 180));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel copyrightLabel = new JLabel("© 2025 TiketID");
        copyrightLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        copyrightLabel.setForeground(new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(), 
                                             ACCENT_COLOR.getBlue(), 180));
        copyrightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        footerPanel.add(versionLabel);
        footerPanel.add(Box.createVerticalStrut(3));
        footerPanel.add(copyrightLabel);

        return footerPanel;
    }

    private void setupAnimation() {
        timer = new Timer(80, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                progress += 2;
                progressBar.setValue(progress);
                progressBar.setString(progress + "%");

                if (progress >= 100) {
                    timer.stop();
                    // Delay sebentar sebelum menutup loading screen
                    Timer closeTimer = new Timer(800, evt -> {
                        dispose();
                        if (onFinish != null) {
                            onFinish.run(); // jalankan callback
                        }
                    });
                    closeTimer.setRepeats(false);
                    closeTimer.start();
                }

                repaint();
            }
        });
    }

    public void startLoading() {
        setVisible(true);
        timer.start();
    }

    private void showMainApplication() {
        SwingUtilities.invokeLater(() -> {
            app.setVisible(true);
            app.showPage(new MovieSelectionPage(app));
        });
    }
}