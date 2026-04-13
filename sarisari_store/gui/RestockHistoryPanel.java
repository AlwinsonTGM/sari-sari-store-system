package gui;

import dao.ProductDAO;
import model.RestockLog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class RestockHistoryPanel extends JPanel {

    private ProductDAO productDAO;
    private JTable restockTable;
    private DefaultTableModel tableModel;
    private JLabel lblTotalCapital;

    public RestockHistoryPanel() {
        this.productDAO = new ProductDAO();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(ThemeManager.bg());

        // Top Panel for Title and Total Capital Spent
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(" Restock History (Capital Spent)");
        lblTitle.setFont(ThemeManager.fontSection());
        lblTitle.setForeground(ThemeManager.primary());
        topPanel.add(lblTitle, BorderLayout.WEST);

        lblTotalCapital = new JLabel("Total Capital Spent: ₱0.00");
        lblTotalCapital.setFont(ThemeManager.fontBold());
        lblTotalCapital.setForeground(ThemeManager.danger()); // Red color to indicate cost
        topPanel.add(lblTotalCapital, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Center Panel for Table
        String[] columns = {"Date/Time", "Product", "Quantity Added", "Purchase Price", "Total Cost", "Restocked By"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        restockTable = new JTable(tableModel);
        restockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        restockTable.setRowHeight(25);

        // Customize columns
        restockTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        restockTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        restockTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        restockTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        restockTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        restockTable.getColumnModel().getColumn(5).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(restockTable);
        ThemeManager.applyTableTheme(restockTable, scrollPane);

        add(scrollPane, BorderLayout.CENTER);

        // Bottom Button Panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);

        JButton btnRefresh = new JButton("Refresh");
        ThemeManager.styleButton(btnRefresh, "primary");
        btnRefresh.addActionListener(e -> refreshData());
        bottomPanel.add(btnRefresh);

        add(bottomPanel, BorderLayout.SOUTH);

        // Load initial data
        refreshData();
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        List<RestockLog> logs = productDAO.getRestockLogs();
        double totalCapital = 0.0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (RestockLog log : logs) {
            String dateStr = log.getRestockDate() != null ? sdf.format(log.getRestockDate()) : "Unknown";
            tableModel.addRow(new Object[]{
                    dateStr,
                    log.getProductName(),
                    log.getQuantityAdded(),
                    String.format("₱%.2f", log.getPurchasePrice()),
                    String.format("₱%.2f", log.getTotalCost()),
                    log.getCashierName()
            });
            totalCapital += log.getTotalCost();
        }

        lblTotalCapital.setText("Total Capital Spent: ₱" + String.format("%.2f", totalCapital));
    }
}
