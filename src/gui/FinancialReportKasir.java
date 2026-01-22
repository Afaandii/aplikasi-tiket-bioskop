package gui;

import dao.TransactionDAO;
import models.Transaction;
import models.Ticket;
import models.User;
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
    private JComboBox<String> paymentMethodComboBox; // Dropdown untuk Metode Pembayaran
    private CinemaApp app;
    private User loggedInUser; // Simpan user yang login

    // --- Konstruktor ---
    public FinancialReportKasir(CinemaApp app) {
        setLayout(new BorderLayout());
        setBackground(backgroundColor);
        this.app = app;
        this.loggedInUser = app.getLoggedInUser(); // Ambil user yang login
        if (loggedInUser == null) {
            JOptionPane.showMessageDialog(this, "User not logged in!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        transactionDAO = new TransactionDAO();
        initializeComponents();
    }

    private void initializeComponents() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(whiteColor);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(189, 195, 199)));
        headerPanel.setPreferredSize(new Dimension(0, 70));

        JLabel titleLabel = new JLabel("Laporan Keuangan - Kasir");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(textColor);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

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

        // Inisialisasi ComboBox Metode Pembayaran
        JLabel paymentMethodLabel = new JLabel("Metode Pembayaran:");
        paymentMethodComboBox = new JComboBox<>();
        paymentMethodComboBox.addItem("Semua Metode");
        paymentMethodComboBox.addItem("Cash");
        paymentMethodComboBox.addItem("Qris");

        JButton generateBtn = new JButton("Filter");
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
        filterPanel.add(paymentMethodLabel);
        filterPanel.add(paymentMethodComboBox);
        filterPanel.add(exportBtn);

        add(filterPanel, BorderLayout.NORTH);

        // HAPUS PANEL RINGKASAN DAN BREAKDOWN DI SINI
        // Kita langsung ke panel Daftar Transaksi

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

        // Tambahkan panel Daftar Transaksi ke BorderLayout.CENTER
        add(transactionPanel, BorderLayout.CENTER);

        // Load data awal (semua transaksi kasir)
        loadAllData();

        // Tambahkan listener untuk dropdown Metode Pembayaran
        paymentMethodComboBox.addActionListener(e -> {
            // Filter otomatis ketika pilihan metode pembayaran berubah
            applyFilters();
        });

        // Listener untuk tombol Filter
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
            // Gunakan method baru yang mendukung filter metode pembayaran
            loadFilteredReportData(startDate, endDate);
        });

        // Listener untuk tombol Export
        exportBtn.addActionListener(e -> exportToExcel());
    }

    /**
     * Method helper untuk menerapkan filter berdasarkan kombinasi metode pembayaran.
     * Metode ini akan memuat data untuk semua tanggal (tanpa batas waktu) berdasarkan pilihan metode pembayaran.
     */
    private void applyFilters() {
        // Ambil nilai dari dropdown
        String selectedPaymentMethod = (String) paymentMethodComboBox.getSelectedItem();

        // Panggil method yang bisa menangani semua filter, dengan null untuk tanggal
        loadFilteredReportData(null, null);
    }

    /**
     * Method baru untuk memuat data laporan dengan filter lengkap (tanggal dan metode pembayaran)
     * Jika startDate dan endDate null, maka akan memuat semua data tanpa batas waktu.
     */
    private void loadFilteredReportData(Date startDate, Date endDate) {
        tableModel.setRowCount(0);

        // Ambil nilai dari dropdown
        String selectedPaymentMethod = (String) paymentMethodComboBox.getSelectedItem();

        List<Transaction> transactions;

        // Untuk kasir, kita hanya ingin melihat transaksinya sendiri, jadi abaikan filter dropdown kasir
        if (startDate == null || endDate == null) {
            // Jika tidak ada filter tanggal, ambil semua transaksi kasir
            transactions = transactionDAO.getAllFinancialReportDataByCashier(loggedInUser.getId());
        } else {
            // Jika ada filter tanggal, ambil transaksi kasir dalam rentang tanggal
            transactions = transactionDAO.getTransactionsByDateRangeByCashier(startDate, endDate, loggedInUser.getId());
        }

        // Filter berdasarkan metode pembayaran jika bukan "Semua Metode"
        if (!"Semua Metode".equals(selectedPaymentMethod)) {
            transactions.removeIf(tx -> !selectedPaymentMethod.equalsIgnoreCase(tx.getPaymentMethod()));
        }

        int rowNumber = 1;

        for (Transaction tx : transactions) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String formattedDate = sdf.format(tx.getCreatedAt());
            tableModel.addRow(new Object[]{
                    rowNumber++,
                    tx.getTransactionCode(),
                    formattedDate,
                    tx.getMovieTitle(),
                    formatCurrency(tx.getTotalPrice()),
                    tx.getPaymentMethod(),
                    tx.getStatus()
            });
        }
    }

    /**
     * Method lama untuk memuat semua data tanpa filter (untuk inisialisasi)
     */
    private void loadAllData() {
        tableModel.setRowCount(0);

        List<Transaction> transactions = transactionDAO.getAllFinancialReportDataByCashier(loggedInUser.getId());

        int rowNumber = 1;

        for (Transaction tx : transactions) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String formattedDate = sdf.format(tx.getCreatedAt());
            tableModel.addRow(new Object[]{
                    rowNumber++,
                    tx.getTransactionCode(),
                    formattedDate,
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

        JDialog detailDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Detail Transaksi", true);
        detailDialog.setLayout(new BorderLayout());
        detailDialog.setSize(800, 600);
        detailDialog.setLocationRelativeTo(this);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(whiteColor);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(189, 195, 199)));
        JLabel headerLabel = new JLabel("Detail Transaksi: " + transactionCode);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(textColor);
        header.add(headerLabel, BorderLayout.WEST);
        detailDialog.add(header, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        infoPanel.setBackground(backgroundColor);
        infoPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        infoPanel.add(new JLabel("Kasir:")); infoPanel.add(new JLabel(tx.getUsername()));
        infoPanel.add(new JLabel("Film:")); infoPanel.add(new JLabel(tx.getMovieTitle()));
        infoPanel.add(new JLabel("Tanggal Tayang:")); infoPanel.add(new JLabel(tx.getShowDate() != null ? tx.getShowDate().toString() : "-"));
        infoPanel.add(new JLabel("Jam Tayang:")); infoPanel.add(new JLabel(tx.getShowTime() != null ? tx.getShowTime().toString() : "-"));
        infoPanel.add(new JLabel("Total Harga:")); infoPanel.add(new JLabel(formatCurrency(tx.getTotalPrice())));
        infoPanel.add(new JLabel("Metode Pembayaran:")); infoPanel.add(new JLabel(tx.getPaymentMethod()));
        detailDialog.add(infoPanel, BorderLayout.CENTER);

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
                        "Export berhasil! File disimpan di:" + fileToSave.getAbsolutePath(),
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