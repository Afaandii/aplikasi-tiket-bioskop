package gui;

import dao.TransactionDAO;
import models.Transaction;
import models.Ticket;
import models.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.border.EmptyBorder;
import com.toedter.calendar.JDateChooser;
import javax.swing.border.TitledBorder;

public class FinancialReportKasir extends JPanel {
    private final Color primaryColor = new Color(41, 128, 185);
    private final Color secondaryColor = new Color(52, 152, 219);
    private final Color backgroundColor = new Color(236, 240, 241);
    private final Color textColor = new Color(44, 62, 80);
    private final Color whiteColor = Color.WHITE;

    private JTable reportTable;
    private DefaultTableModel tableModel;
    private TransactionDAO transactionDAO;
    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;
    private JComboBox<String> paymentMethodComboBox;
    private CinemaApp app;
    private User loggedInUser;

    public FinancialReportKasir(CinemaApp app) {
        setLayout(new BorderLayout());
        setBackground(backgroundColor);
        this.app = app;
        this.loggedInUser = app.getLoggedInUser();
        if (loggedInUser == null) {
            JOptionPane.showMessageDialog(this, "User not logged in!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        transactionDAO = new TransactionDAO();
        initializeComponents();
    }

    private void initializeComponents() {
        // =====================================================================
        // TOP PANEL: gabungkan header + filter dalam satu panel di NORTH
        // =====================================================================
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(whiteColor);

        // --- Header Bar ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(whiteColor);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(189, 195, 199)),
                new EmptyBorder(10, 20, 10, 20)
        ));
        headerPanel.setPreferredSize(new Dimension(0, 60));

        JLabel titleLabel = new JLabel("Laporan Keuangan - Kasir");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(textColor);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton backBtn = new JButton("← Kembali");
        backBtn.setBackground(primaryColor);
        backBtn.setForeground(whiteColor);
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
        backBtn.setFont(new Font("Arial", Font.BOLD, 12));
        backBtn.setPreferredSize(new Dimension(110, 35));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> app.showPage(new MovieSelectionPage(app)));
        headerPanel.add(backBtn, BorderLayout.EAST);

        topPanel.add(headerPanel, BorderLayout.NORTH);

        // --- Filter Bar ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterPanel.setBackground(new Color(245, 247, 250));
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(189, 195, 199)),
                new EmptyBorder(4, 10, 4, 10)
        ));

        JLabel startDateLabel = new JLabel("Dari Tanggal:");
        startDateLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        startDateChooser = new JDateChooser();
        startDateChooser.setDateFormatString("yyyy-MM-dd");
        startDateChooser.setPreferredSize(new Dimension(130, 28));
        ((JTextField) startDateChooser.getDateEditor().getUiComponent()).setEditable(false);

        JLabel endDateLabel = new JLabel("Sampai Tanggal:");
        endDateLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        endDateChooser = new JDateChooser();
        endDateChooser.setDateFormatString("yyyy-MM-dd");
        endDateChooser.setPreferredSize(new Dimension(130, 28));
        ((JTextField) endDateChooser.getDateEditor().getUiComponent()).setEditable(false);

        JButton generateBtn = new JButton("Filter");
        generateBtn.setBackground(primaryColor);
        generateBtn.setForeground(whiteColor);
        generateBtn.setFocusPainted(false);
        generateBtn.setBorderPainted(false);
        generateBtn.setFont(new Font("Arial", Font.BOLD, 12));
        generateBtn.setPreferredSize(new Dimension(80, 28));
        generateBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel paymentMethodLabel = new JLabel("Metode Pembayaran:");
        paymentMethodLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        paymentMethodComboBox = new JComboBox<>();
        paymentMethodComboBox.addItem("Semua Metode");
        paymentMethodComboBox.addItem("Cash");
        paymentMethodComboBox.addItem("Qris");
        paymentMethodComboBox.setPreferredSize(new Dimension(140, 28));

        JButton exportBtn = new JButton("Export to Excel");
        exportBtn.setBackground(secondaryColor);
        exportBtn.setForeground(whiteColor);
        exportBtn.setFocusPainted(false);
        exportBtn.setBorderPainted(false);
        exportBtn.setFont(new Font("Arial", Font.BOLD, 12));
        exportBtn.setPreferredSize(new Dimension(140, 28));
        exportBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        filterPanel.add(startDateLabel);
        filterPanel.add(startDateChooser);
        filterPanel.add(endDateLabel);
        filterPanel.add(endDateChooser);
        filterPanel.add(generateBtn);
        filterPanel.add(paymentMethodLabel);
        filterPanel.add(paymentMethodComboBox);
        filterPanel.add(exportBtn);

        topPanel.add(filterPanel, BorderLayout.SOUTH);

        // Tambahkan topPanel ke NORTH (hanya satu kali!)
        add(topPanel, BorderLayout.NORTH);

        // =====================================================================
        // CENTER PANEL: Daftar Transaksi
        // =====================================================================
        JPanel transactionPanel = new JPanel(new BorderLayout());
        transactionPanel.setBackground(backgroundColor);
        transactionPanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(12, 16, 12, 16),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(189, 195, 199)),
                        "Daftar Transaksi",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 14),
                        textColor
                )
        ));

        String[] columnNames = {
                "No", "Transaction Code", "Tanggal", "Movie Title",
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
        reportTable.setFont(new Font("Arial", Font.PLAIN, 13));
        reportTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        reportTable.getTableHeader().setBackground(new Color(41, 128, 185));
        reportTable.getTableHeader().setForeground(whiteColor);
        reportTable.setSelectionBackground(primaryColor);
        reportTable.setSelectionForeground(whiteColor);
        reportTable.setGridColor(new Color(220, 220, 220));
        reportTable.setShowGrid(true);
        reportTable.setIntercellSpacing(new Dimension(1, 1));

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

        add(transactionPanel, BorderLayout.CENTER);

        // =====================================================================
        // Load data & listeners
        // =====================================================================
        loadAllData();

        paymentMethodComboBox.addActionListener(e -> applyFilters());

        generateBtn.addActionListener(e -> {
            java.util.Date startUtil = startDateChooser.getDate();
            java.util.Date endUtil = endDateChooser.getDate();
            if (startUtil == null || endUtil == null) {
                JOptionPane.showMessageDialog(FinancialReportKasir.this,
                        "Please select both start and end dates.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            Date startDate = new Date(startUtil.getTime());
            Date endDate = new Date(endUtil.getTime());
            if (startDate.after(endDate)) {
                JOptionPane.showMessageDialog(FinancialReportKasir.this,
                        "Start date cannot be after end date.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            loadFilteredReportData(startDate, endDate);
        });

        exportBtn.addActionListener(e -> exportToExcel());
    }

    private void applyFilters() {
        loadFilteredReportData(null, null);
    }

    private void loadFilteredReportData(Date startDate, Date endDate) {
        tableModel.setRowCount(0);

        String selectedPaymentMethod = (String) paymentMethodComboBox.getSelectedItem();

        List<Transaction> transactions;
        if (startDate == null || endDate == null) {
            transactions = transactionDAO.getAllFinancialReportDataByCashier(loggedInUser.getId());
        } else {
            transactions = transactionDAO.getTransactionsByDateRangeByCashier(startDate, endDate, loggedInUser.getId());
        }

        if (!"Semua Metode".equals(selectedPaymentMethod)) {
            transactions.removeIf(tx -> !selectedPaymentMethod.equalsIgnoreCase(tx.getPaymentMethod()));
        }

        int rowNumber = 1;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (Transaction tx : transactions) {
            tableModel.addRow(new Object[]{
                    rowNumber++,
                    tx.getTransactionCode(),
                    sdf.format(tx.getCreatedAt()),
                    tx.getMovieTitle(),
                    formatCurrency(tx.getTotalPrice()),
                    tx.getPaymentMethod(),
                    tx.getStatus()
            });
        }
    }

    private void loadAllData() {
        tableModel.setRowCount(0);

        List<Transaction> transactions = transactionDAO.getAllFinancialReportDataByCashier(loggedInUser.getId());

        int rowNumber = 1;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (Transaction tx : transactions) {
            tableModel.addRow(new Object[]{
                    rowNumber++,
                    tx.getTransactionCode(),
                    sdf.format(tx.getCreatedAt()),
                    tx.getMovieTitle(),
                    formatCurrency(tx.getTotalPrice()),
                    tx.getPaymentMethod(),
                    tx.getStatus()
            });
        }
    }

    private void showTransactionDetail(String transactionCode) {
        Transaction tx = transactionDAO.getTransactionByCode(transactionCode);
        if (tx == null) {
            JOptionPane.showMessageDialog(this, "Transaction not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<Ticket> tickets = transactionDAO.getTicketsByTransactionId(tx.getId());

        JDialog detailDialog = new JDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Detail Transaksi",
                true
        );
        detailDialog.setLayout(new BorderLayout());
        detailDialog.setSize(800, 550);
        detailDialog.setLocationRelativeTo(this);

        // Header dialog
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(whiteColor);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(189, 195, 199)),
                new EmptyBorder(12, 16, 12, 16)
        ));
        JLabel headerLabel = new JLabel("Detail Transaksi: " + transactionCode);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(textColor);
        header.add(headerLabel, BorderLayout.WEST);
        detailDialog.add(header, BorderLayout.NORTH);

        // Info panel
        JPanel infoPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        infoPanel.setBackground(backgroundColor);
        infoPanel.setBorder(new EmptyBorder(20, 20, 10, 20));

        Font labelFont = new Font("Arial", Font.BOLD, 13);
        Font valueFont = new Font("Arial", Font.PLAIN, 13);

        JLabel kasirLbl = new JLabel("Kasir:"); kasirLbl.setFont(labelFont);
        JLabel kasirVal = new JLabel(tx.getUsername()); kasirVal.setFont(valueFont);
        JLabel filmLbl = new JLabel("Film:"); filmLbl.setFont(labelFont);
        JLabel filmVal = new JLabel(tx.getMovieTitle()); filmVal.setFont(valueFont);

        JLabel tglLbl = new JLabel("Tanggal Tayang:"); tglLbl.setFont(labelFont);
        JLabel tglVal = new JLabel(tx.getShowDate() != null ? tx.getShowDate().toString() : "-"); tglVal.setFont(valueFont);
        JLabel jamLbl = new JLabel("Jam Tayang:"); jamLbl.setFont(labelFont);
        JLabel jamVal = new JLabel(tx.getShowTime() != null ? tx.getShowTime().toString() : "-"); jamVal.setFont(valueFont);

        JLabel totalLbl = new JLabel("Total Harga:"); totalLbl.setFont(labelFont);
        JLabel totalVal = new JLabel(formatCurrency(tx.getTotalPrice())); totalVal.setFont(valueFont);
        JLabel metodeLbl = new JLabel("Metode Pembayaran:"); metodeLbl.setFont(labelFont);
        JLabel metodeVal = new JLabel(tx.getPaymentMethod()); metodeVal.setFont(valueFont);

        infoPanel.add(kasirLbl);  infoPanel.add(kasirVal);
        infoPanel.add(filmLbl);   infoPanel.add(filmVal);
        infoPanel.add(tglLbl);    infoPanel.add(tglVal);
        infoPanel.add(jamLbl);    infoPanel.add(jamVal);
        infoPanel.add(totalLbl);  infoPanel.add(totalVal);
        infoPanel.add(metodeLbl); infoPanel.add(metodeVal);

        detailDialog.add(infoPanel, BorderLayout.CENTER);

        // Ticket table
        JPanel ticketPanel = new JPanel(new BorderLayout());
        ticketPanel.setBackground(backgroundColor);
        ticketPanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 16, 16, 16),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(189, 195, 199)),
                        "Daftar Tiket",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 13),
                        textColor
                )
        ));

        String[] ticketColumns = {"Seat Label", "Harga"};
        DefaultTableModel ticketModel = new DefaultTableModel(ticketColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        for (Ticket ticket : tickets) {
            ticketModel.addRow(new Object[]{ticket.getSeatLabel(), formatCurrency(ticket.getPrice())});
        }

        JTable ticketTable = new JTable(ticketModel);
        ticketTable.setRowHeight(25);
        ticketTable.setFont(new Font("Arial", Font.PLAIN, 13));
        ticketTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
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
        fileChooser.setSelectedFile(new java.io.File("financial_report_kasir.xlsx"));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
                org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.createSheet("Report");
                org.apache.poi.xssf.usermodel.XSSFRow headerRow = sheet.createRow(0);
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    headerRow.createCell(col).setCellValue(tableModel.getColumnName(col));
                }
                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    org.apache.poi.xssf.usermodel.XSSFRow excelRow = sheet.createRow(row + 1);
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        Object value = tableModel.getValueAt(row, col);
                        excelRow.createCell(col).setCellValue(value != null ? value.toString() : "");
                    }
                }
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    sheet.autoSizeColumn(col);
                }
                try (java.io.FileOutputStream fileOut = new java.io.FileOutputStream(fileToSave)) {
                    workbook.write(fileOut);
                }
                JOptionPane.showMessageDialog(this,
                        "Export berhasil! File disimpan di: " + fileToSave.getAbsolutePath(),
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