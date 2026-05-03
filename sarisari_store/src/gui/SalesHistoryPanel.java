package gui;

import dao.TransactionDAO;
import model.Transaction;
import model.TransactionItem;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.Date;
import java.util.List;

/**
 * SalesHistoryPanel - Transaction History and Reports Screen
 *
 * Features:
 * - View all transactions
 * - Filter by date range
 * - View transaction details (line items)
 */
public class SalesHistoryPanel extends JPanel {

    private TransactionDAO transactionDAO;

    private JTable salesTable;
    private DefaultTableModel salesTableModel;
    private JTextField txtFromDate;
    private JTextField txtToDate;
    private List<Transaction> currentSales;

    public SalesHistoryPanel() {
        transactionDAO = new TransactionDAO();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(ThemeManager.bg());

        JLabel lblTitle = new JLabel(" Sales History");
        lblTitle.setFont(ThemeManager.fontSection());
        lblTitle.setForeground(ThemeManager.primary());
        add(lblTitle, BorderLayout.NORTH);

        add(createFilterPanel(), BorderLayout.NORTH);
        add(createTablePanel(),  BorderLayout.CENTER);

        refreshData();
    }

    // ── Filter panel ─────────────────────────────────────────────────────────

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        panel.add(new JLabel("From (YYYY-MM-DD):"));
        txtFromDate = new JTextField(10);
        txtFromDate.setFont(new Font("Arial", Font.PLAIN, 12));
        txtFromDate.addActionListener(e -> filterByDate());
        panel.add(txtFromDate);

        panel.add(new JLabel("To (YYYY-MM-DD):"));
        txtToDate = new JTextField(10);
        txtToDate.setFont(new Font("Arial", Font.PLAIN, 12));
        txtToDate.addActionListener(e -> filterByDate());
        panel.add(txtToDate);

        JButton btnFilter = new JButton("Filter");
        btnFilter.setFont(new Font("Arial", Font.BOLD, 12));
        btnFilter.addActionListener(e -> filterByDate());
        panel.add(btnFilter);

        JButton btnToday = new JButton("Today's Sales");
        btnToday.setFont(new Font("Arial", Font.BOLD, 12));
        btnToday.addActionListener(e -> loadTodaySales());
        panel.add(btnToday);

        JButton btnAll = new JButton("All Sales");
        btnAll.setFont(new Font("Arial", Font.BOLD, 12));
        btnAll.addActionListener(e -> refreshData());
        panel.add(btnAll);

        return panel;
    }

    // ── Table panel ──────────────────────────────────────────────────────────

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.surface());
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ThemeManager.border()),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Removed "Cashier" column — no user management in simplified system
        String[] columns = {"Transaction ID", "Date/Time", "Items", "Total", "Discount", "Final"};
        salesTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        salesTable = new JTable(salesTableModel);
        salesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableColumnModel cm = salesTable.getColumnModel();
        cm.getColumn(0).setPreferredWidth(100);
        cm.getColumn(1).setPreferredWidth(160);
        cm.getColumn(2).setPreferredWidth(50);
        cm.getColumn(3).setPreferredWidth(90);
        cm.getColumn(4).setPreferredWidth(90);
        cm.getColumn(5).setPreferredWidth(90);

        JScrollPane sp = new JScrollPane(salesTable);
        ThemeManager.applyTableTheme(salesTable, sp);
        panel.add(sp, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnPanel.setOpaque(false);

        JButton btnDetails = new JButton(" View Details");
        ThemeManager.styleButton(btnDetails, "primary");
        btnDetails.addActionListener(e -> viewSaleDetails());
        btnPanel.add(btnDetails);

        JButton btnRefresh = new JButton("Refresh");
        ThemeManager.styleButton(btnRefresh, "neutral");
        btnRefresh.addActionListener(e -> refreshData());
        btnPanel.add(btnRefresh);

        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ── Data methods ─────────────────────────────────────────────────────────

    public void refreshData() {
        currentSales = transactionDAO.getAll();
        refreshTable();
        txtFromDate.setText("");
        txtToDate.setText("");
    }

    private void loadTodaySales() {
        currentSales = transactionDAO.getTodayTransactions();
        refreshTable();
        java.util.Date today = new java.util.Date();
        txtFromDate.setText(new Date(today.getTime()).toString());
        txtToDate.setText(new Date(today.getTime()).toString());
    }

    private void filterByDate() {
        String fromStr = txtFromDate.getText().trim();
        String toStr   = txtToDate.getText().trim();
        if (fromStr.isEmpty() || toStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both from and to dates.", "Missing Dates", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            currentSales = transactionDAO.getByDateRange(Date.valueOf(fromStr), Date.valueOf(toStr));
            refreshTable();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Invalid Date", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTable() {
        salesTableModel.setRowCount(0);
        for (Transaction t : currentSales) {
            salesTableModel.addRow(new Object[]{
                t.getTransactionId(),
                t.getTransactionDatetime().toString(),
                t.getItemCount(),
                String.format("%.2f", t.getTotalAmount()),
                String.format("%.2f", t.getDiscountAmount()),
                String.format("%.2f", t.getFinalAmount())
            });
        }
    }

    // ── View details dialog ───────────────────────────────────────────────────

    private void viewSaleDetails() {
        int row = salesTable.getSelectedRow();
        if (row < 0 || row >= currentSales.size()) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to view details.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Transaction selected = currentSales.get(row);
        Transaction full = transactionDAO.getById(selected.getTransactionId());
        if (full == null) {
            JOptionPane.showMessageDialog(this, "Could not load transaction details.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Transaction Details - ID: " + full.getTransactionId(), true);
        dialog.setSize(500, 420);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(ThemeManager.surface());

        // Header info
        JPanel headerPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createTitledBorder("Transaction Info"));

        headerPanel.add(new JLabel("Transaction ID:"));
        headerPanel.add(new JLabel(String.valueOf(full.getTransactionId())));
        headerPanel.add(new JLabel("Date/Time:"));
        headerPanel.add(new JLabel(full.getTransactionDatetime().toString()));
        headerPanel.add(new JLabel("Total Items:"));
        headerPanel.add(new JLabel(String.valueOf(full.getItemCount())));

        panel.add(headerPanel, BorderLayout.NORTH);

        // Items table
        String[] itemCols = {"Product", "Qty", "Unit Price", "Total"};
        DefaultTableModel itemModel = new DefaultTableModel(itemCols, 0);
        for (TransactionItem item : full.getItems()) {
            itemModel.addRow(new Object[]{
                item.getProductName(),
                item.getQuantity(),
                String.format("%.2f", item.getSoldPrice()),
                String.format("%.2f", item.getItemTotal())
            });
        }
        JTable itemTable = new JTable(itemModel);
        itemTable.setEnabled(false);
        JScrollPane sp = new JScrollPane(itemTable);
        ThemeManager.applyTableTheme(itemTable, sp);
        sp.setBorder(BorderFactory.createTitledBorder("Items"));
        panel.add(sp, BorderLayout.CENTER);

        // Totals
        JPanel totalsPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        totalsPanel.setOpaque(false);
        totalsPanel.setBorder(BorderFactory.createTitledBorder("Totals"));

        totalsPanel.add(new JLabel("Subtotal:"));
        totalsPanel.add(new JLabel("₱" + String.format("%.2f", full.getTotalAmount()), JLabel.RIGHT));
        totalsPanel.add(new JLabel("Discount:"));
        totalsPanel.add(new JLabel("₱" + String.format("%.2f", full.getDiscountAmount()), JLabel.RIGHT));

        JLabel lblFinalKey = new JLabel("FINAL TOTAL:");
        lblFinalKey.setFont(new Font("Arial", Font.BOLD, 14));
        totalsPanel.add(lblFinalKey);
        JLabel lblFinalVal = new JLabel("₱" + String.format("%.2f", full.getFinalAmount()), JLabel.RIGHT);
        lblFinalVal.setFont(ThemeManager.fontSection());
        lblFinalVal.setForeground(ThemeManager.success());
        totalsPanel.add(lblFinalVal);

        panel.add(totalsPanel, BorderLayout.SOUTH);
        dialog.add(panel);
        dialog.setVisible(true);
    }
}
