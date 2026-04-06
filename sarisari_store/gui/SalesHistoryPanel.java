package gui;

import dao.SaleDAO;
import model.Sale;
import model.SaleItem;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.Date;
import java.util.List;

/**
 * SalesHistoryPanel - Sales History and Reports Screen
 * 
 * Features:
 * - View all sales transactions
 * - Filter by date range
 * - View sale details (line items)
 * - Shows transaction summary
 */
public class SalesHistoryPanel extends JPanel {
    
    private SaleDAO saleDAO;
    
    private JTable salesTable;
    private DefaultTableModel salesTableModel;
    private JTextField txtFromDate;
    private JTextField txtToDate;
    private List<Sale> currentSales;
    
    public SalesHistoryPanel() {
        saleDAO = new SaleDAO();
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(ThemeManager.bg());
        
        // Title
        JLabel lblTitle = new JLabel(" Sales History");
        lblTitle.setFont(ThemeManager.fontSection());
        lblTitle.setForeground(ThemeManager.primary());
        add(lblTitle, BorderLayout.NORTH);
        
        // Filter panel
        JPanel filterPanel = createFilterPanel();
        add(filterPanel, BorderLayout.NORTH);
        
        // Sales table
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // Load initial data
        refreshData();
    }
    
    /**
     * Create the filter panel
     */
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        panel.add(new JLabel("From (YYYY-MM-DD):"));
        txtFromDate = new JTextField(10);
        txtFromDate.setFont(new Font("Arial", Font.PLAIN, 12));
        txtFromDate.addActionListener(e -> filterByDate()); // Enter key support
        panel.add(txtFromDate);
        
        panel.add(new JLabel("To (YYYY-MM-DD):"));
        txtToDate = new JTextField(10);
        txtToDate.setFont(new Font("Arial", Font.PLAIN, 12));
        txtToDate.addActionListener(e -> filterByDate()); // Enter key support
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
    
    /**
     * Create the sales table panel
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.surface());
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ThemeManager.border()),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Table
        String[] columns = {"Sale ID", "Date/Time", "Cashier", "Items", "Total", "Discount", "Final"};
        salesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        salesTable = new JTable(salesTableModel);
        salesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Column widths
        TableColumnModel columnModel = salesTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(60);
        columnModel.getColumn(1).setPreferredWidth(150);
        columnModel.getColumn(2).setPreferredWidth(120);
        columnModel.getColumn(3).setPreferredWidth(50);
        columnModel.getColumn(4).setPreferredWidth(80);
        columnModel.getColumn(5).setPreferredWidth(80);
        columnModel.getColumn(6).setPreferredWidth(80);
        
        JScrollPane scrollPane = new JScrollPane(salesTable);
        ThemeManager.applyTableTheme(salesTable, scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);
        
        JButton btnViewDetails = new JButton(" View Details");
        ThemeManager.styleButton(btnViewDetails, "primary");
        btnViewDetails.addActionListener(e -> viewSaleDetails());
        buttonPanel.add(btnViewDetails);
        
        JButton btnRefresh = new JButton("↺ Refresh");
        ThemeManager.styleButton(btnRefresh, "neutral");
        btnRefresh.addActionListener(e -> refreshData());
        buttonPanel.add(btnRefresh);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Load all sales
     */
    public void refreshData() {
        currentSales = saleDAO.getAll();
        refreshTable();
        txtFromDate.setText("");
        txtToDate.setText("");
    }
    
    /**
     * Load today's sales
     */
    private void loadTodaySales() {
        currentSales = saleDAO.getTodaySales();
        refreshTable();
        
        // Set date fields to today
        java.util.Date today = new java.util.Date();
        txtFromDate.setText(new Date(today.getTime()).toString());
        txtToDate.setText(new Date(today.getTime()).toString());
    }
    
    /**
     * Filter sales by date range
     */
    private void filterByDate() {
        String fromStr = txtFromDate.getText().trim();
        String toStr = txtToDate.getText().trim();
        
        if (fromStr.isEmpty() || toStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both from and to dates.", "Missing Dates", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            Date fromDate = Date.valueOf(fromStr);
            Date toDate = Date.valueOf(toStr);
            
            currentSales = saleDAO.getByDateRange(fromDate, toDate);
            refreshTable();
            
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Invalid Date", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Refresh the sales table
     */
    private void refreshTable() {
        salesTableModel.setRowCount(0);
        
        double totalSales = 0;
        int totalTransactions = 0;
        
        for (Sale sale : currentSales) {
            salesTableModel.addRow(new Object[]{
                sale.getSaleId(),
                sale.getSaleDatetime().toString(),
                sale.getCashierName(),
                sale.getItemCount(),
                String.format("%.2f", sale.getTotalAmount()),
                String.format("%.2f", sale.getDiscountAmount()),
                String.format("%.2f", sale.getFinalAmount())
            });
            
            totalSales += sale.getFinalAmount();
            totalTransactions++;
        }
        
        // Show summary
        if (totalTransactions > 0) {
            JLabel lblSummary = new JLabel(String.format("   Showing %d transactions | Total: P%.2f", 
                totalTransactions, totalSales));
            lblSummary.setFont(new Font("Arial", Font.BOLD, 12));
            lblSummary.setForeground(ThemeManager.primary());
            
            // Remove old summary if exists
            Component[] components = this.getComponents();
            for (Component c : components) {
                if (c instanceof JPanel && ((JPanel) c).getComponentCount() > 0) {
                    // Check if it's the summary panel
                }
            }
        }
    }
    
    /**
     * View details of selected sale
     */
    private void viewSaleDetails() {
        int row = salesTable.getSelectedRow();
        if (row < 0 || row >= currentSales.size()) {
            JOptionPane.showMessageDialog(this, "Please select a sale to view details.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Sale sale = currentSales.get(row);
        
        // Fetch full sale with items
        Sale fullSale = saleDAO.getById(sale.getSaleId());
        if (fullSale == null) {
            JOptionPane.showMessageDialog(this, "Could not load sale details.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create details dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Sale Details - ID: " + sale.getSaleId(), true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(ThemeManager.surface());
        
        // Header info
        JPanel headerPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createTitledBorder("Transaction Info"));
        
        headerPanel.add(new JLabel("Sale ID:"));
        headerPanel.add(new JLabel(String.valueOf(fullSale.getSaleId())));
        
        headerPanel.add(new JLabel("Date/Time:"));
        headerPanel.add(new JLabel(fullSale.getSaleDatetime().toString()));
        
        headerPanel.add(new JLabel("Cashier:"));
        headerPanel.add(new JLabel(fullSale.getCashierName()));
        
        headerPanel.add(new JLabel("Total Items:"));
        headerPanel.add(new JLabel(String.valueOf(fullSale.getItemCount())));
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Items table
        String[] itemColumns = {"Product", "Qty", "Unit Price", "Total"};
        DefaultTableModel itemModel = new DefaultTableModel(itemColumns, 0);
        
        for (SaleItem item : fullSale.getItems()) {
            itemModel.addRow(new Object[]{
                item.getProductName(),
                item.getQuantity(),
                String.format("%.2f", item.getUnitPrice()),
                String.format("%.2f", item.getLineTotal())
            });
        }
        
        JTable itemTable = new JTable(itemModel);
        itemTable.setEnabled(false);
        
        JScrollPane scrollPane = new JScrollPane(itemTable);
        ThemeManager.applyTableTheme(itemTable, scrollPane);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Items"));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Totals
        JPanel totalsPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        totalsPanel.setOpaque(false);
        totalsPanel.setBorder(BorderFactory.createTitledBorder("Totals"));
        
        totalsPanel.add(new JLabel("Subtotal:"));
        totalsPanel.add(new JLabel("P" + String.format("%.2f", fullSale.getTotalAmount()), JLabel.RIGHT));
        
        totalsPanel.add(new JLabel("Discount:"));
        totalsPanel.add(new JLabel("P" + String.format("%.2f", fullSale.getDiscountAmount()), JLabel.RIGHT));
        
        JLabel lblFinal = new JLabel("FINAL TOTAL:");
        lblFinal.setFont(new Font("Arial", Font.BOLD, 14));
        totalsPanel.add(lblFinal);
        
        JLabel lblFinalValue = new JLabel("₱" + String.format("%.2f", fullSale.getFinalAmount()), JLabel.RIGHT);
        lblFinalValue.setFont(ThemeManager.fontSection());
        lblFinalValue.setForeground(ThemeManager.success());
        totalsPanel.add(lblFinalValue);
        
        panel.add(totalsPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
}
