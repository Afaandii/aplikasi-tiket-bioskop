package gui;

import dao.TransactionDAO;
import models.Transaction;
import models.Ticket;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

// Import JDateChooser dan util.Date
import com.toedter.calendar.JDateChooser;

public class FinancialReportPanel extends JPanel {
    private final Color primaryColor = new Color(41, 128, 185);
    private final Color secondaryColor = new Color(52, 152, 219);
    private final Color backgroundColor = new Color(236, 240, 241);
    private final Color textColor = new Color(44, 62, 80);
    private final Color whiteColor = Color.WHITE;

    private JTable reportTable;
    private DefaultTableModel tableModel;
    private TransactionDAO transactionDAO;

    // Panel untuk ringkasan metode pembayaran
    private JLabel cashTotalLabel;
    private JLabel qrisTotalLabel;

    // Date choosers untuk filter
    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;

    public FinancialReportPanel() {
        setLayout(new BorderLayout());
        setBackground(backgroundColor);
        transactionDAO = new TransactionDAO();

        initializeComponents();
    }

    private void initializeComponents() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(whiteColor);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(189, 195, 199)));
        headerPanel.setPreferredSize(new Dimension(0, 70));

        JLabel titleLabel = new JLabel("Financial Report");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(textColor);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(backgroundColor);
        filterPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel startDateLabel = new JLabel("Dari Tanggal:");
        startDateChooser = new JDateChooser();
        startDateChooser.setDateFormatString("yyyy-MM-dd");
        startDateChooser.setPreferredSize(new Dimension(120, 25));
        ((JTextField) startDateChooser.getDateEditor().getUiComponent()).setEditable(false);

        JLabel endDateLabel = new JLabel("Sampai Tanggal:");
        endDateChooser = new JDateChooser();
        endDateChooser.setDateFormatString("yyyy-MM-dd");
        endDateChooser.setPreferredSize(new Dimension(120, 25));
        ((JTextField) endDateChooser.getDateEditor().getUiComponent()).setEditable(false);

        JButton generateBtn = new JButton("Generate Report");
        generateBtn.setBackground(primaryColor);
        generateBtn.setForeground(whiteColor);
        generateBtn.setFocusPainted(false);
        generateBtn.setBorderPainted(false);

        JButton exportBtn = new JButton("Export to Excel");
        exportBtn.setBackground(new Color(52, 152, 219));
        exportBtn.setForeground(whiteColor);
        exportBtn.setFocusPainted(false);
        exportBtn.setBorderPainted(false);

        filterPanel.add(startDateLabel);
        filterPanel.add(startDateChooser);
        filterPanel.add(endDateLabel);
        filterPanel.add(endDateChooser);
        filterPanel.add(generateBtn);
        filterPanel.add(exportBtn);

        add(filterPanel, BorderLayout.NORTH);

        // Ringkasan Transaksi Panel
        JPanel summaryPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        summaryPanel.setBackground(backgroundColor);
        summaryPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            "Ringkasan Transaksi",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14),
            textColor
        ));

        JPanel totalPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        totalPanel.setBackground(backgroundColor);
        JLabel totalTransLabel = new JLabel("Total Transaksi: 0");
        totalTransLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalTransLabel.setForeground(textColor);
        JLabel totalRevenueLabel = new JLabel("Total Pendapatan: Rp 0");
        totalRevenueLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalRevenueLabel.setForeground(textColor);
        totalPanel.add(totalTransLabel);
        totalPanel.add(totalRevenueLabel);

        JPanel paymentBreakdownPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        paymentBreakdownPanel.setBackground(backgroundColor);
        paymentBreakdownPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            "Breakdown Metode Pembayaran",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12),
            textColor
        ));

        cashTotalLabel = new JLabel("CASH: Rp 0");
        cashTotalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        cashTotalLabel.setForeground(Color.BLUE);
        qrisTotalLabel = new JLabel("QRIS: Rp 0");
        qrisTotalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        qrisTotalLabel.setForeground(Color.MAGENTA);

        paymentBreakdownPanel.add(cashTotalLabel);
        paymentBreakdownPanel.add(qrisTotalLabel);

        summaryPanel.add(totalPanel);
        summaryPanel.add(paymentBreakdownPanel);

        add(summaryPanel, BorderLayout.CENTER);

        // Daftar Transaksi Panel
        JPanel transactionPanel = new JPanel(new BorderLayout());
        transactionPanel.setBackground(backgroundColor);
        transactionPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            "Daftar Transaksi",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14),
            textColor
        ));

        String[] columnNames = {
            "No", "Transaction Code", "Tanggal", "Kasir", "Movie Title", 
            "Total Price", "Payment Method", "Status"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reportTable = new JTable(tableModel);
        reportTable.setRowHeight(30);
        reportTable.setFont(new Font("Arial", Font.PLAIN, 14));
        reportTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        reportTable.setSelectionBackground(primaryColor);
        reportTable.setSelectionForeground(whiteColor);

        reportTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = reportTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        String transactionCode = (String) tableModel.getValueAt(selectedRow, 1);
                        showTransactionDetail(transactionCode);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        transactionPanel.add(scrollPane, BorderLayout.CENTER);
        add(transactionPanel, BorderLayout.SOUTH);

        // Action Listener for Generate Button
        generateBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                java.util.Date startUtil = startDateChooser.getDate();
                java.util.Date endUtil = endDateChooser.getDate();

                if (startUtil == null || endUtil == null) {
                    JOptionPane.showMessageDialog(FinancialReportPanel.this,
                        "Please select both start and end dates.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Date startDate = new Date(startUtil.getTime());
                Date endDate = new Date(endUtil.getTime());

                if (startDate.after(endDate)) {
                    JOptionPane.showMessageDialog(FinancialReportPanel.this,
                        "Start date cannot be after end date.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                loadDetailedReportData(startDate, endDate, totalTransLabel, totalRevenueLabel, cashTotalLabel, qrisTotalLabel);
            }
        });

        // Export to Excel (placeholder)
        exportBtn.addActionListener(e -> exportToExcel());

        // Load semua data saat panel dibuka
        loadAllData(totalTransLabel, totalRevenueLabel, cashTotalLabel, qrisTotalLabel);
    }

    private void loadAllData(JLabel totalTransLabel, JLabel totalRevenueLabel, JLabel cashTotalLabel, JLabel qrisTotalLabel) {
        tableModel.setRowCount(0);

        List<Transaction> transactions = transactionDAO.getAllFinancialReportData();

        long totalRevenue = 0;
        int totalTransactions = 0;
        long cashTotal = 0;
        long qrisTotal = 0;

        int rowNumber = 1;
        for (Transaction tx : transactions) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String formattedDate = sdf.format(tx.getCreatedAt());

            tableModel.addRow(new Object[]{
                rowNumber++,
                tx.getTransactionCode(),
                formattedDate,
                tx.getUsername(),
                tx.getMovieTitle(),
                formatCurrency(tx.getTotalPrice()),
                tx.getPaymentMethod(),
                tx.getStatus()
            });

            totalRevenue += tx.getTotalPrice();
            totalTransactions++;

            if ("Cash".equalsIgnoreCase(tx.getPaymentMethod())) {
                cashTotal += tx.getTotalPrice();
            } else if ("Qris".equalsIgnoreCase(tx.getPaymentMethod())) {
                qrisTotal += tx.getTotalPrice();
            }
        }

        totalTransLabel.setText("Total Transaksi: " + totalTransactions);
        totalRevenueLabel.setText("Total Pendapatan: " + formatCurrency(totalRevenue));
        cashTotalLabel.setText("CASH: " + formatCurrency(cashTotal));
        qrisTotalLabel.setText("QRIS: " + formatCurrency(qrisTotal));
    }

    private void loadDetailedReportData(Date startDate, Date endDate, JLabel totalTransLabel, JLabel totalRevenueLabel, JLabel cashTotalLabel, JLabel qrisTotalLabel) {
        tableModel.setRowCount(0);

        List<Transaction> transactions = transactionDAO.getTransactionsByDateRange(startDate, endDate);

        long totalRevenue = 0;
        int totalTransactions = 0;
        long cashTotal = 0;
        long qrisTotal = 0;

        int rowNumber = 1;
        for (Transaction tx : transactions) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String formattedDate = sdf.format(tx.getCreatedAt());

            tableModel.addRow(new Object[]{
                rowNumber++,
                tx.getTransactionCode(),
                formattedDate,
                tx.getUsername(),
                tx.getMovieTitle(),
                formatCurrency(tx.getTotalPrice()),
                tx.getPaymentMethod(),
                tx.getStatus()
            });

            totalRevenue += tx.getTotalPrice();
            totalTransactions++;

            if ("Cash".equalsIgnoreCase(tx.getPaymentMethod())) {
                cashTotal += tx.getTotalPrice();
            } else if ("Qris".equalsIgnoreCase(tx.getPaymentMethod())) {
                qrisTotal += tx.getTotalPrice();
            }
        }

        totalTransLabel.setText("Total Transaksi: " + totalTransactions);
        totalRevenueLabel.setText("Total Pendapatan: " + formatCurrency(totalRevenue));
        cashTotalLabel.setText("CASH: " + formatCurrency(cashTotal));
        qrisTotalLabel.setText("QRIS: " + formatCurrency(qrisTotal));
    }

    private void showTransactionDetail(String transactionCode) {
        Transaction tx = transactionDAO.getTransactionByCode(transactionCode);
        if (tx == null) {
            JOptionPane.showMessageDialog(this, "Transaction not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Ticket> tickets = transactionDAO.getTicketsByTransactionId(tx.getId());

        JDialog detailDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Detail Transaksi", true);
        detailDialog.setLayout(new BorderLayout());
        detailDialog.setSize(800, 600);
        detailDialog.setLocationRelativeTo(this);

        // Header Detail
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(whiteColor);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(189, 195, 199)));
        JLabel headerLabel = new JLabel("Detail Transaksi: " + transactionCode);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(textColor);
        header.add(headerLabel, BorderLayout.WEST);
        detailDialog.add(header, BorderLayout.NORTH);

        // Panel Info Utama
        JPanel infoPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        infoPanel.setBackground(backgroundColor);
        infoPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        infoPanel.add(new JLabel("Kasir:"));
        infoPanel.add(new JLabel(tx.getUsername()));
        infoPanel.add(new JLabel("Film:"));
        infoPanel.add(new JLabel(tx.getMovieTitle()));
        infoPanel.add(new JLabel("Tanggal Tayang:"));
        infoPanel.add(new JLabel(tx.getShowDate() != null ? tx.getShowDate().toString() : "-"));
        infoPanel.add(new JLabel("Jam Tayang:"));
        infoPanel.add(new JLabel(tx.getShowTime() != null ? tx.getShowTime().toString() : "-"));
        infoPanel.add(new JLabel("Total Harga:"));
        infoPanel.add(new JLabel(formatCurrency(tx.getTotalPrice())));
        infoPanel.add(new JLabel("Metode Pembayaran:"));
        infoPanel.add(new JLabel(tx.getPaymentMethod()));

        detailDialog.add(infoPanel, BorderLayout.CENTER);

        // Panel Tiket
        JPanel ticketPanel = new JPanel(new BorderLayout());
        ticketPanel.setBackground(backgroundColor);
        ticketPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            "Daftar Tiket",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12),
            textColor
        ));

        String[] ticketColumns = {"Seat Label", "Harga"};
        DefaultTableModel ticketModel = new DefaultTableModel(ticketColumns, 0);
        for (Ticket ticket : tickets) {
            ticketModel.addRow(new Object[]{ticket.getSeatLabel(), formatCurrency(ticket.getPrice())});
        }

        JTable ticketTable = new JTable(ticketModel);
        ticketTable.setRowHeight(25);
        ticketTable.setFont(new Font("Arial", Font.PLAIN, 12));
        JScrollPane ticketScrollPane = new JScrollPane(ticketTable);
        ticketPanel.add(ticketScrollPane, BorderLayout.CENTER);

        detailDialog.add(ticketPanel, BorderLayout.SOUTH);

        detailDialog.setVisible(true);
    }

    private String formatCurrency(long amount) {
        return "Rp " + String.format("%,d", amount);
    }
    
    private void exportToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Excel File");
        fileChooser.setSelectedFile(new java.io.File("financial_report.xlsx"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();

            try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

                org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.createSheet("Report");

                // Header Excel
                org.apache.poi.xssf.usermodel.XSSFRow headerRow = sheet.createRow(0);
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    headerRow.createCell(col).setCellValue(tableModel.getColumnName(col));
                }

                // Isi data Excel dari JTable
                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    org.apache.poi.xssf.usermodel.XSSFRow excelRow = sheet.createRow(row + 1);

                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        Object value = tableModel.getValueAt(row, col);
                        excelRow.createCell(col).setCellValue(value != null ? value.toString() : "");
                    }
                }

                // Autosize column
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    sheet.autoSizeColumn(col);
                }

                // Save file
                try (java.io.FileOutputStream fileOut = new java.io.FileOutputStream(fileToSave)) {
                    workbook.write(fileOut);
                }

                JOptionPane.showMessageDialog(this,
                    "Export berhasil! File disimpan di:\n" + fileToSave.getAbsolutePath(),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Gagal export: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

}