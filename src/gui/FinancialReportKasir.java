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
    private final Color primaryColor   = new Color(41, 128, 185);
    private final Color secondaryColor = new Color(52, 152, 219);
    private final Color backgroundColor = new Color(236, 240, 241);
    private final Color textColor      = new Color(44, 62, 80);
    private final Color whiteColor     = Color.WHITE;

    // Warna kartu ringkasan
    private final Color cardAllColor  = new Color(41, 128, 185);   // biru – semua transaksi
    private final Color cardCashColor = new Color(39, 174, 96);    // hijau – cash
    private final Color cardQrisColor = new Color(142, 68, 173);   // ungu – qris

    private JTable reportTable;
    private DefaultTableModel tableModel;
    private TransactionDAO transactionDAO;
    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;
    private JComboBox<String> paymentMethodComboBox;
    private CinemaApp app;
    private User loggedInUser;

    // Label ringkasan – diperbarui setiap kali data dimuat
    private JLabel lblTotalTx;
    private JLabel lblTotalPendapatan;
    private JLabel lblCashTx;
    private JLabel lblCashPendapatan;
    private JLabel lblQrisTx;
    private JLabel lblQrisPendapatan;

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

    // =========================================================================
    // BUILD UI
    // =========================================================================

    private void initializeComponents() {

        // --- TOP PANEL: header + filter ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(whiteColor);

        // Header bar
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

        JButton backBtn = createStyledButton("← Kembali", primaryColor, 110, 35);
        backBtn.addActionListener(e -> app.showPage(new MovieSelectionPage(app)));
        headerPanel.add(backBtn, BorderLayout.EAST);
        topPanel.add(headerPanel, BorderLayout.NORTH);

        // Filter bar
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

        JButton generateBtn = createStyledButton("Filter", primaryColor, 80, 28);

        JLabel paymentMethodLabel = new JLabel("Metode Pembayaran:");
        paymentMethodLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        paymentMethodComboBox = new JComboBox<>();
        paymentMethodComboBox.addItem("Semua Metode");
        paymentMethodComboBox.addItem("Cash");
        paymentMethodComboBox.addItem("Qris");
        paymentMethodComboBox.setPreferredSize(new Dimension(140, 28));

        JButton exportBtn = createStyledButton("Export to Excel", secondaryColor, 140, 28);

        filterPanel.add(startDateLabel);
        filterPanel.add(startDateChooser);
        filterPanel.add(endDateLabel);
        filterPanel.add(endDateChooser);
        filterPanel.add(generateBtn);
        filterPanel.add(paymentMethodLabel);
        filterPanel.add(paymentMethodComboBox);
        filterPanel.add(exportBtn);

        topPanel.add(filterPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // --- SUMMARY PANEL ---
        add(createSummaryPanel(), BorderLayout.CENTER == BorderLayout.CENTER
                ? BorderLayout.CENTER : BorderLayout.CENTER);

        // Bungkus summary + tabel dalam satu CENTER panel
        JPanel centerWrapper = new JPanel(new BorderLayout(0, 0));
        centerWrapper.setBackground(backgroundColor);
        centerWrapper.add(createSummaryPanel(), BorderLayout.NORTH);
        centerWrapper.add(createTransactionTablePanel(), BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);

        // --- Listeners ---
        paymentMethodComboBox.addActionListener(e -> applyFilters());

        generateBtn.addActionListener(e -> {
            java.util.Date startUtil = startDateChooser.getDate();
            java.util.Date endUtil   = endDateChooser.getDate();
            if (startUtil == null || endUtil == null) {
                JOptionPane.showMessageDialog(this,
                        "Please select both start and end dates.",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Date startDate = new Date(startUtil.getTime());
            Date endDate   = new Date(endUtil.getTime());
            if (startDate.after(endDate)) {
                JOptionPane.showMessageDialog(this,
                        "Start date cannot be after end date.",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            loadFilteredReportData(startDate, endDate);
        });

        exportBtn.addActionListener(e -> exportToExcel());

        // Load data awal
        loadAllData();
    }

    // =========================================================================
    // SUMMARY PANEL
    // =========================================================================

    private JPanel createSummaryPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(backgroundColor);
        wrapper.setBorder(new EmptyBorder(14, 16, 4, 16));

        JLabel sectionTitle = new JLabel("Ringkasan Transaksi");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 15));
        sectionTitle.setForeground(textColor);
        sectionTitle.setBorder(new EmptyBorder(0, 2, 8, 0));
        wrapper.add(sectionTitle, BorderLayout.NORTH);

        // 3 kartu: Semua | Cash | QRIS
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 14, 0));
        cardsPanel.setBackground(backgroundColor);

        // -- Kartu Semua Transaksi --
        JPanel cardAll = createSummaryCard(cardAllColor, "Semua Transaksi", "💳");
        lblTotalTx         = getCardValueLabel(cardAll, 0);
        lblTotalPendapatan = getCardValueLabel(cardAll, 1);
        cardsPanel.add(cardAll);

        // -- Kartu Cash --
        JPanel cardCash = createSummaryCard(cardCashColor, "Cash", "💵");
        lblCashTx         = getCardValueLabel(cardCash, 0);
        lblCashPendapatan = getCardValueLabel(cardCash, 1);
        cardsPanel.add(cardCash);

        // -- Kartu QRIS --
        JPanel cardQris = createSummaryCard(cardQrisColor, "QRIS", "📱");
        lblQrisTx         = getCardValueLabel(cardQris, 0);
        lblQrisPendapatan = getCardValueLabel(cardQris, 1);
        cardsPanel.add(cardQris);

        wrapper.add(cardsPanel, BorderLayout.CENTER);
        return wrapper;
    }

    /**
     * Buat satu kartu ringkasan dengan judul dan ikon.
     * Kartu memiliki 2 baris nilai: jumlah transaksi dan total pendapatan.
     */
    private JPanel createSummaryCard(Color color, String title, String icon) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(color);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        // Header baris: ikon + judul
        JPanel headerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        headerRow.setBackground(color);

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Arial", Font.BOLD, 14));
        titleLbl.setForeground(new Color(255, 255, 255, 210));

        headerRow.add(iconLbl);
        headerRow.add(titleLbl);
        card.add(headerRow, BorderLayout.NORTH);

        // Nilai: jumlah transaksi
        JLabel txLbl = new JLabel("0 Transaksi");
        txLbl.setFont(new Font("Arial", Font.BOLD, 22));
        txLbl.setForeground(Color.WHITE);
        card.add(txLbl, BorderLayout.CENTER);

        // Nilai: total pendapatan
        JLabel pendapatanLbl = new JLabel("Rp 0");
        pendapatanLbl.setFont(new Font("Arial", Font.PLAIN, 13));
        pendapatanLbl.setForeground(new Color(255, 255, 255, 190));
        card.add(pendapatanLbl, BorderLayout.SOUTH);

        // Simpan reference label di client property agar bisa diambil nanti
        card.putClientProperty("txLabel", txLbl);
        card.putClientProperty("pendapatanLabel", pendapatanLbl);

        return card;
    }

    /** Ambil JLabel nilai ke-index dari kartu (0 = tx count, 1 = pendapatan) */
    private JLabel getCardValueLabel(JPanel card, int index) {
        if (index == 0) return (JLabel) card.getClientProperty("txLabel");
        return (JLabel) card.getClientProperty("pendapatanLabel");
    }

    /** Perbarui nilai semua kartu ringkasan dari list transaksi yang sedang ditampilkan */
    private void updateSummary(List<Transaction> transactions) {
        long totalAll = 0, totalCash = 0, totalQris = 0;
        int  cntAll   = 0, cntCash   = 0, cntQris   = 0;

        for (Transaction tx : transactions) {
            totalAll += tx.getTotalPrice();
            cntAll++;
            String pm = tx.getPaymentMethod();
            if (pm != null) {
                if ("cash".equalsIgnoreCase(pm)) {
                    totalCash += tx.getTotalPrice();
                    cntCash++;
                } else if ("qris".equalsIgnoreCase(pm)) {
                    totalQris += tx.getTotalPrice();
                    cntQris++;
                }
            }
        }

        lblTotalTx.setText(cntAll + " Transaksi");
        lblTotalPendapatan.setText(formatCurrency(totalAll));

        lblCashTx.setText(cntCash + " Transaksi");
        lblCashPendapatan.setText(formatCurrency(totalCash));

        lblQrisTx.setText(cntQris + " Transaksi");
        lblQrisPendapatan.setText(formatCurrency(totalQris));
    }

    // =========================================================================
    // TRANSACTION TABLE PANEL
    // =========================================================================

    private JPanel createTransactionTablePanel() {
        JPanel transactionPanel = new JPanel(new BorderLayout());
        transactionPanel.setBackground(backgroundColor);
        transactionPanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(4, 16, 12, 16),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(189, 195, 199)),
                        "Daftar Transaksi",
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 14), textColor
                )
        ));

        String[] columnNames = {
            "No", "Transaction Code", "Tanggal", "Movie Title",
            "Total Price", "Payment Method", "Status"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        reportTable = new JTable(tableModel);
        reportTable.setRowHeight(30);
        reportTable.setFont(new Font("Arial", Font.PLAIN, 13));
        reportTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        reportTable.getTableHeader().setBackground(primaryColor);
        reportTable.getTableHeader().setForeground(whiteColor);
        reportTable.setSelectionBackground(primaryColor);
        reportTable.setSelectionForeground(whiteColor);
        reportTable.setGridColor(new Color(220, 220, 220));
        reportTable.setShowGrid(true);
        reportTable.setIntercellSpacing(new Dimension(1, 1));

        reportTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = reportTable.getSelectedRow();
                    if (row >= 0) {
                        showTransactionDetail((String) tableModel.getValueAt(row, 1));
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        transactionPanel.add(scrollPane, BorderLayout.CENTER);
        return transactionPanel;
    }

    // =========================================================================
    // DATA LOADING
    // =========================================================================

    private void loadAllData() {
        tableModel.setRowCount(0);
        List<Transaction> transactions =
                transactionDAO.getAllFinancialReportDataByCashier(loggedInUser.getId());
        populateTable(transactions);
        updateSummary(transactions);
    }

    private void applyFilters() {
        loadFilteredReportData(null, null);
    }

    private void loadFilteredReportData(Date startDate, Date endDate) {
        tableModel.setRowCount(0);

        String selectedMethod = (String) paymentMethodComboBox.getSelectedItem();

        List<Transaction> transactions;
        if (startDate == null || endDate == null) {
            transactions = transactionDAO.getAllFinancialReportDataByCashier(loggedInUser.getId());
        } else {
            transactions = transactionDAO.getTransactionsByDateRangeByCashier(
                    startDate, endDate, loggedInUser.getId());
        }

        if (!"Semua Metode".equals(selectedMethod)) {
            transactions.removeIf(tx -> !selectedMethod.equalsIgnoreCase(tx.getPaymentMethod()));
        }

        populateTable(transactions);
        updateSummary(transactions);
    }

    private void populateTable(List<Transaction> transactions) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        int rowNum = 1;
        for (Transaction tx : transactions) {
            tableModel.addRow(new Object[]{
                rowNum++,
                tx.getTransactionCode(),
                sdf.format(tx.getCreatedAt()),
                tx.getMovieTitle(),
                formatCurrency(tx.getTotalPrice()),
                tx.getPaymentMethod(),
                tx.getStatus()
            });
        }
    }

    // =========================================================================
    // DETAIL DIALOG
    // =========================================================================

    private void showTransactionDetail(String transactionCode) {
        Transaction tx = transactionDAO.getTransactionByCode(transactionCode);
        if (tx == null) {
            JOptionPane.showMessageDialog(this, "Transaction not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<Ticket> tickets = transactionDAO.getTicketsByTransactionId(tx.getId());

        JDialog detailDialog = new JDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "Detail Transaksi", true
        );
        detailDialog.setLayout(new BorderLayout());
        detailDialog.setSize(800, 550);
        detailDialog.setLocationRelativeTo(this);

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

        JPanel infoPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        infoPanel.setBackground(backgroundColor);
        infoPanel.setBorder(new EmptyBorder(20, 20, 10, 20));

        Font lf = new Font("Arial", Font.BOLD, 13);
        Font vf = new Font("Arial", Font.PLAIN, 13);

        addInfoRow(infoPanel, "Kasir:", tx.getUsername(), lf, vf);
        addInfoRow(infoPanel, "Film:", tx.getMovieTitle(), lf, vf);
        addInfoRow(infoPanel, "Tanggal Tayang:", tx.getShowDate() != null ? tx.getShowDate().toString() : "-", lf, vf);
        addInfoRow(infoPanel, "Jam Tayang:", tx.getShowTime() != null ? tx.getShowTime().toString() : "-", lf, vf);
        addInfoRow(infoPanel, "Total Harga:", formatCurrency(tx.getTotalPrice()), lf, vf);
        addInfoRow(infoPanel, "Metode Pembayaran:", tx.getPaymentMethod(), lf, vf);

        detailDialog.add(infoPanel, BorderLayout.CENTER);

        JPanel ticketPanel = new JPanel(new BorderLayout());
        ticketPanel.setBackground(backgroundColor);
        ticketPanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 16, 16, 16),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(189, 195, 199)),
                        "Daftar Tiket", TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 13), textColor
                )
        ));

        String[] ticketColumns = {"Seat Label", "Harga"};
        DefaultTableModel ticketModel = new DefaultTableModel(ticketColumns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Ticket ticket : tickets) {
            ticketModel.addRow(new Object[]{ticket.getSeatLabel(), formatCurrency(ticket.getPrice())});
        }

        JTable ticketTable = new JTable(ticketModel);
        ticketTable.setRowHeight(25);
        ticketTable.setFont(new Font("Arial", Font.PLAIN, 13));
        ticketTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        ticketPanel.add(new JScrollPane(ticketTable), BorderLayout.CENTER);

        detailDialog.add(ticketPanel, BorderLayout.SOUTH);
        detailDialog.setVisible(true);
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private void addInfoRow(JPanel panel, String label, String value, Font lf, Font vf) {
        JLabel lbl = new JLabel(label); lbl.setFont(lf);
        JLabel val = new JLabel(value); val.setFont(vf);
        panel.add(lbl);
        panel.add(val);
    }

    private JButton createStyledButton(String text, Color bg, int w, int h) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(whiteColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(w, h));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private String formatCurrency(long amount) {
        return "Rp " + String.format("%,d", amount);
    }

    // =========================================================================
    // EXPORT
    // =========================================================================

    private void exportToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Excel File");
        fileChooser.setSelectedFile(new java.io.File("financial_report_kasir.xlsx"));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook =
                         new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
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
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Gagal export: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}