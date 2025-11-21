package gui;
import dao.TicketDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import models.Ticket;

public class TicketPage extends JPanel {
    public TicketPage(CinemaApp app, int transactionId) {
        setLayout(new BorderLayout());
        setBackground(new Color(15, 15, 35)); // Dark navy background
        
        // Enhanced title with gradient-like effect
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BorderLayout());
        titlePanel.setBackground(new Color(15, 15, 35));
        titlePanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        JLabel title = new JLabel("🎫 TIKET ANDA 🎫", JLabel.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(new Color(255, 215, 0)); // Gold color
        
        // Create subtitle
        JLabel subtitle = new JLabel("Cinema Experience Ticket", JLabel.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.ITALIC, 14));
        subtitle.setForeground(new Color(180, 180, 200));
        
        titlePanel.add(title, BorderLayout.CENTER);
        titlePanel.add(subtitle, BorderLayout.SOUTH);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // Create main content panel with card design and scroll
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(new Color(15, 15, 35));
        mainPanel.setBorder(new EmptyBorder(20, 50, 20, 50));
        
        // Ambil data tiket dari DB
        TicketDAO ticketDAO = new TicketDAO();
        List<Ticket> ticketList = ticketDAO.getTicketsByTransaction(transactionId);
        
        JPanel ticketCard = createTicketCard(ticketList);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        mainPanel.add(ticketCard, gbc);
        
        // Add scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBackground(new Color(15, 15, 35));
        scrollPane.getViewport().setBackground(new Color(15, 15, 35));
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Style the scrollbar
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(100, 149, 237, 150);
                this.trackColor = new Color(15, 15, 35);
            }
            
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Enhanced bottom panel with better button
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(15, 15, 35));
        bottomPanel.setBorder(new EmptyBorder(20, 50, 30, 50));
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        
        JButton backBtn = createStyledButton("🏠 Kembali ke Movie Page", new Color(220, 20, 60));
        bottomPanel.add(backBtn);
        
        add(bottomPanel, BorderLayout.SOUTH);
        
        backBtn.addActionListener(e -> app.showPage(new MovieSelectionPage(app)));
    }
    
    private JPanel createTicketCard(List<Ticket> ticketList) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(255, 255, 255, 250),
                    0, getHeight(), new Color(240, 248, 255, 250)
                );
                g2.setPaint(gradient);
                
                // Draw rounded rectangle
                RoundRectangle2D roundRect = new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.fill(roundRect);
                
                // Add border
                g2.setColor(new Color(100, 149, 237, 100));
                g2.setStroke(new BasicStroke(2));
                g2.draw(roundRect);
                
                // Add subtle shadow effect
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fill(new RoundRectangle2D.Float(3, 3, getWidth()-1, getHeight()-1, 20, 20));
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(30, 40, 30, 40));
        card.setPreferredSize(new Dimension(600, 500)); // Increased height for better content display
        
        String content = "";
        if (ticketList != null && !ticketList.isEmpty()) {
            Ticket ticketData = ticketList.get(0); // Ambil tiket pertama
            content = String.format(
                "<html><div style='text-align: center; font-family: SansSerif;'>" +
                "<div style='color: #228B22; font-size: 18px; font-weight: bold; margin-bottom: 20px;'>" +
                "✅ TIKET BERHASIL DIPESAN! ✅</div>" +
                "<div style='font-size: 16px; line-height: 1.8; color: #333;'>" +
                "<div style='background: #f0f8ff; padding: 8px; margin: 5px 0; border-radius: 5px;'>" +
                "🎬 <b>Film:</b> %s</div>" +
                "<div style='background: #fff8dc; padding: 8px; margin: 5px 0; border-radius: 5px;'>" +
                "🏢 <b>Bioskop:</b> %s</div>" +
                "<div style='background: #f0fff0; padding: 8px; margin: 5px 0; border-radius: 5px;'>" +
                "📅 <b>Tanggal:</b> %s</div>" +
                "<div style='background: #fff0f5; padding: 8px; margin: 5px 0; border-radius: 5px;'>" +
                "⏰ <b>Waktu:</b> %s</div>" +
                "<div style='background: #f5f5dc; padding: 8px; margin: 5px 0; border-radius: 5px;'>" +
                "💺 <b>Seat:</b> %s</div>" +
                "<div style='background: #e6ffe6; padding: 8px; margin: 5px 0; border-radius: 5px;'>" +
                "💰 <b>Harga:</b> Rp %,d</div>" +
                "<div style='background: #ffe4e1; padding: 8px; margin: 5px 0; border-radius: 5px;'>" +
                "🎟️ <b>Kode Transaksi:</b> %s</div>" +
                "</div>" +
                "<div style='color: #FF4500; font-size: 18px; font-weight: bold; margin-top: 25px;'>" +
                "🍿 SELAMAT MENONTON! 🎭</div>" +
                "</div></html>",
                ticketData.getMovieTitle(),
                ticketData.getCinemaName(),
                ticketData.getShowDate(),
                ticketData.getShowTime(),
                ticketData.getSeatLabel(),
                ticketData.getPrice(),
                ticketData.getTicketCode()
            );
        } else {
            content = "<html><div style='text-align: center; font-family: SansSerif;'>" +
                     "<div style='color: #DC143C; font-size: 18px; font-weight: bold;'>" +
                     "❌ DATA TIKET TIDAK DITEMUKAN</div>" +
                     "<div style='color: #666; font-size: 14px; margin-top: 10px;'>" +
                     "Silakan hubungi customer service untuk bantuan</div>" +
                     "</div></html>";
        }
        
        JLabel ticket = new JLabel(content, JLabel.CENTER);
        ticket.setFont(new Font("SansSerif", Font.PLAIN, 14));
        ticket.setVerticalAlignment(JLabel.CENTER); // Changed to CENTER for better display
        
        // Wrap the ticket label in a panel for better scrolling
        JPanel ticketWrapper = new JPanel(new BorderLayout());
        ticketWrapper.setOpaque(false);
        ticketWrapper.add(ticket, BorderLayout.CENTER);
        
        // Add decorative elements
        JPanel decorativeTop = new JPanel();
        decorativeTop.setOpaque(false);
        decorativeTop.setPreferredSize(new Dimension(0, 10));
        
        JPanel decorativeBottom = new JPanel();
        decorativeBottom.setOpaque(false);
        decorativeBottom.setPreferredSize(new Dimension(0, 10));
        
        card.add(decorativeTop, BorderLayout.NORTH);
        card.add(ticketWrapper, BorderLayout.CENTER);
        card.add(decorativeBottom, BorderLayout.SOUTH);
        
        return card;
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(280, 50));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.repaint();
            }
        });
        
        return button;
    }
}