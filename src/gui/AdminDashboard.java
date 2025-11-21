package gui;

import dao.MovieDAO;
import dao.SeatDAO;
import dao.ShowtimeDAO;
import dao.StudioDAO;
import dao.TransactionDAO;
import dao.UserDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;

public class AdminDashboard extends JPanel {
    private final Color primaryColor = new Color(41, 128, 185);
    private final Color secondaryColor = new Color(52, 152, 219);
    private final Color backgroundColor = new Color(236, 240, 241);
    private final Color sidebarColor = new Color(44, 62, 80);
    private final Color textColor = new Color(44, 62, 80);
    private final Color whiteColor = Color.WHITE;
    
    private CinemaApp app;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private models.User currentUser;
    private TransactionDAO transactionDAO;
    
    public AdminDashboard(CinemaApp app, models.User user) {
        this.app = app;
        this.currentUser = user;
        this.transactionDAO = new TransactionDAO();
        setLayout(new BorderLayout());
        setBackground(backgroundColor);
        
        initializeComponents();
    }
    
    private void initializeComponents() {
        // Sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);
        
        // Header
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);
        
        // Content Panel dengan CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(backgroundColor);
        
        // Tambahkan semua panel content
        contentPanel.add(createDashboardOverview(), "OVERVIEW");
        contentPanel.add(new StudioManagementPanel(), "STUDIO"); // Ganti Cinema jadi Studio
        contentPanel.add(new MovieManagementPanel(), "MOVIE");
        // Seat Type Management Panel dihapus
        contentPanel.add(new ShowtimeManagementPanel(), "SHOWTIME");
        contentPanel.add(new SeatManagementPanel(), "SEAT");
        contentPanel.add(new TicketPriceManagementPanel(), "TICKET_PRICE"); 
        contentPanel.add(new UserManagementPanel(), "USER"); // Tambahkan User Management
        contentPanel.add(new FinancialReportPanel(), "FINANCIAL_REPORT");
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Show overview by default
        cardLayout.show(contentPanel, "OVERVIEW");
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(sidebarColor);
        sidebar.setPreferredSize(new Dimension(250, 600));
        sidebar.setLayout(new BorderLayout());
        
        // Logo Panel
        JPanel logoPanel = new JPanel(new FlowLayout());
        logoPanel.setBackground(sidebarColor);
        logoPanel.setBorder(new EmptyBorder(20, 10, 20, 10));
        
        JLabel logoLabel = new JLabel("TICKET ID ADMIN");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        logoLabel.setForeground(Color.WHITE);
        logoPanel.add(logoLabel);
        
        sidebar.add(logoPanel, BorderLayout.NORTH);
        
        // Menu Panel
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(sidebarColor);
        menuPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        // Menu Items - Diperbarui
        String[] menuItems = {"Dashboard", "Studio", "Movies", "Showtimes", "Seats", "Ticket Price", "Users", "Financial Report"}; // Ganti Cinema -> Studio, Hilangkan Seat Types, Tambah Users
        String[] menuCommands = {"OVERVIEW", "STUDIO", "MOVIE", "SHOWTIME", "SEAT", "TICKET_PRICE", "USER", "FINANCIAL_REPORT"}; // Sesuaikan command
        
        for (int i = 0; i < menuItems.length; i++) {
            JButton menuBtn = createMenuButton(menuItems[i], menuCommands[i]);
            menuPanel.add(menuBtn);
            menuPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        sidebar.add(menuPanel, BorderLayout.CENTER);
        
        // Logout Panel
        JPanel logoutPanel = new JPanel(new FlowLayout());
        logoutPanel.setBackground(sidebarColor);
        logoutPanel.setBorder(new EmptyBorder(20, 10, 20, 10));
        
        JButton logoutBtn = createMenuButton("Logout", "LOGOUT");
        logoutBtn.setBackground(new Color(231, 76, 60));
        logoutPanel.add(logoutBtn);
        
        sidebar.add(logoutPanel, BorderLayout.SOUTH);
        
        return sidebar;
    }
    
    private JButton createMenuButton(String text, String command) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(230, 40));
        button.setMaximumSize(new Dimension(230, 40));
        button.setBackground(new Color(52, 73, 94));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ("LOGOUT".equals(command)) {
                    // Handle logout
                    int confirm = JOptionPane.showConfirmDialog(
                        AdminDashboard.this,
                        "Apakah Anda yakin ingin logout?",
                        "Konfirmasi Logout",
                        JOptionPane.YES_NO_OPTION
                    );
                    if (confirm == JOptionPane.YES_OPTION) {
                        app.setLoggedInUser(null);
                        app.showPage(new LoginPage(app));
                    }
                } else {
                    cardLayout.show(contentPanel, command);
                }
            }
        });
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(primaryColor);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!"LOGOUT".equals(command)) {
                    button.setBackground(new Color(52, 73, 94));
                } else {
                    button.setBackground(new Color(231, 76, 60));
                }
            }
        });
        
        return button;
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(whiteColor);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(189, 195, 199)),
            new EmptyBorder(15, 20, 15, 20)
        ));
        header.setPreferredSize(new Dimension(0, 70));
        
        // Title
        JLabel titleLabel = new JLabel("Admin Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(textColor);
        header.add(titleLabel, BorderLayout.WEST);
        
        // User info
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setBackground(whiteColor);
        
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername());
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        welcomeLabel.setForeground(textColor);
        userPanel.add(welcomeLabel);
        
        header.add(userPanel, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createDashboardOverview() {
        JPanel overview = new JPanel(new BorderLayout());
        overview.setBackground(backgroundColor);
        overview.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Dashboard Overview");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(textColor);
        overview.add(titleLabel, BorderLayout.NORTH);
        
        // Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        statsPanel.setBackground(backgroundColor);
        statsPanel.setBorder(new EmptyBorder(30, 0, 0, 0));
        
        // Create stat cards - Diperbarui
        StudioDAO studioDAO = new StudioDAO();
        int totalStudios = studioDAO.getTotalStudios();
        statsPanel.add(createStatCard("Total Studios", String.valueOf(totalStudios), new Color(52, 152, 219)));
        MovieDAO movieDAO = new MovieDAO();
        int totalMovies = movieDAO.getTotalMovies();
        statsPanel.add(createStatCard("Total Movies", String.valueOf(totalMovies), new Color(46, 204, 113)));
        ShowtimeDAO showtimeDAO = new ShowtimeDAO();
        int totalShowtimes = showtimeDAO.getTotalShowtimes();
        statsPanel.add(createStatCard("Total Showtimes", String.valueOf(totalShowtimes), new Color(155, 89, 182)));
        SeatDAO seatDAO = new SeatDAO();
        int totalSeats = seatDAO.getTotalSeats();
        statsPanel.add(createStatCard("Total Seats", String.valueOf(totalSeats), new Color(231, 76, 60)));
        UserDAO userDAO = new UserDAO();
        int totalUsers = userDAO.getTotalUsers();
        statsPanel.add(createStatCard("Total Users", String.valueOf(totalUsers), new Color(155, 89, 182)));
        // Di dalam createDashboardOverview(), setelah stat card Total Users
        long totalRevenue = transactionDAO.getTotalRevenue(
            new Date(System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L), // 30 hari terakhir
            new Date(System.currentTimeMillis())
        );
        statsPanel.add(createStatCard("Total Revenue", "Rp " + String.format("%,d", totalRevenue), new Color(241, 196, 15)));
        
        overview.add(statsPanel, BorderLayout.CENTER);
        
        return overview;
    }
    
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(whiteColor);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        card.setPreferredSize(new Dimension(200, 120));
        
        // Color indicator
        JPanel colorBar = new JPanel();
        colorBar.setBackground(color);
        colorBar.setPreferredSize(new Dimension(0, 4));
        card.add(colorBar, BorderLayout.NORTH);
        
        // Content
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(whiteColor);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(127, 140, 141));
        content.add(titleLabel, BorderLayout.NORTH);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 36));
        valueLabel.setForeground(textColor);
        content.add(valueLabel, BorderLayout.CENTER);
        
        card.add(content, BorderLayout.CENTER);
        
        return card;
    }
}