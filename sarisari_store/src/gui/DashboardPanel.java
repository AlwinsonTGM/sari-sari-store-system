package gui;

import dao.ProductDAO;
import dao.TransactionDAO;
import model.Product;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

/**
 * DashboardPanel — Main Overview Screen
 *
 * Shows:
 *  - Today's snapshots: Sales, Profit, Capital Spent, Net Profit
 *  - All-Time totals: Revenue, Profit, Capital, Net
 *  - Low-stock alerts
 *  - Top-selling products (today)
 */
public class DashboardPanel extends JPanel {

    private TransactionDAO transactionDAO;
    private ProductDAO productDAO;

    // Today cards
    private JLabel lblTodaySales;
    private JLabel lblTodayProfit;
    private JLabel lblCapitalToday;
    private JLabel lblNetToday;

    // All-time cards
    private JLabel lblAllRevenue;
    private JLabel lblAllProfit;
    private JLabel lblAllCapital;
    private JLabel lblAllNet;

    // Bottom panels
    private JTable lowStockTable;
    private DefaultListModel<String> topProductsModel;

    public DashboardPanel() {
        transactionDAO = new TransactionDAO();
        productDAO = new ProductDAO();

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(ThemeManager.bg());

        // ── Title row ──────────────────────────────────────────────────────
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel(" Dashboard");
        lblTitle.setFont(ThemeManager.fontTitle());
        lblTitle.setForeground(ThemeManager.primary());
        titleRow.add(lblTitle, BorderLayout.WEST);

        JButton btnRefresh = new JButton("⟳ Refresh");
        ThemeManager.styleButton(btnRefresh, "primary");
        btnRefresh.addActionListener(e -> refreshData());
        JPanel titleRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        titleRight.setOpaque(false);
        titleRight.add(btnRefresh);
        titleRow.add(titleRight, BorderLayout.EAST);

        add(titleRow, BorderLayout.NORTH);

        // ── Center content ─────────────────────────────────────────────────
        JPanel center = new JPanel(new BorderLayout(15, 15));
        center.setOpaque(false);

        // ── TODAY'S row (4 cards) ───────────────────────────────────────────
        JPanel todaySection = new JPanel(new BorderLayout(0, 6));
        todaySection.setOpaque(false);

        JLabel lblTodayTitle = new JLabel("  Today");
        lblTodayTitle.setFont(ThemeManager.fontBold());
        lblTodayTitle.setForeground(ThemeManager.text2());
        todaySection.add(lblTodayTitle, BorderLayout.NORTH);

        JPanel todayCards = new JPanel(new GridLayout(1, 4, 12, 0));
        todayCards.setOpaque(false);

        lblTodaySales  = new JLabel("₱0.00", JLabel.CENTER);
        lblTodayProfit = new JLabel("₱0.00", JLabel.CENTER);
        lblCapitalToday = new JLabel("₱0.00", JLabel.CENTER);
        lblNetToday    = new JLabel("₱0.00", JLabel.CENTER);

        todayCards.add(buildCard(" Sales",         lblTodaySales,  ThemeManager.success()));
        todayCards.add(buildCard(" Gross Profit",  lblTodayProfit, ThemeManager.primary()));
        todayCards.add(buildCard(" Capital Spent", lblCapitalToday, ThemeManager.danger()));
        todayCards.add(buildCard(" Net Profit",   lblNetToday,    ThemeManager.warning()));

        todaySection.add(todayCards, BorderLayout.CENTER);
        center.add(todaySection, BorderLayout.NORTH);

        // ── ALL-TIME row (4 cards) ──────────────────────────────────────────
        JPanel allTimeSection = new JPanel(new BorderLayout(0, 6));
        allTimeSection.setOpaque(false);

        JLabel lblAllTitle = new JLabel("  All-Time");
        lblAllTitle.setFont(ThemeManager.fontBold());
        lblAllTitle.setForeground(ThemeManager.text2());
        allTimeSection.add(lblAllTitle, BorderLayout.NORTH);

        JPanel allTimeCards = new JPanel(new GridLayout(1, 4, 12, 0));
        allTimeCards.setOpaque(false);

        lblAllRevenue = new JLabel("₱0.00", JLabel.CENTER);
        lblAllProfit  = new JLabel("₱0.00", JLabel.CENTER);
        lblAllCapital = new JLabel("₱0.00", JLabel.CENTER);
        lblAllNet     = new JLabel("₱0.00", JLabel.CENTER);

        allTimeCards.add(buildCard(" Revenue",       lblAllRevenue, ThemeManager.success()));
        allTimeCards.add(buildCard(" Gross Profit",  lblAllProfit,  ThemeManager.primary()));
        allTimeCards.add(buildCard(" Capital Spent", lblAllCapital, ThemeManager.danger()));
        allTimeCards.add(buildCard(" Net Profit",    lblAllNet,     ThemeManager.purple()));

        allTimeSection.add(allTimeCards, BorderLayout.CENTER);

        // Stack today + alltime vertically
        JPanel statsStack = new JPanel(new GridLayout(2, 1, 0, 14));
        statsStack.setOpaque(false);
        statsStack.add(todaySection);
        statsStack.add(allTimeSection);
        center.add(statsStack, BorderLayout.NORTH);

        // ── Bottom: Low Stock + Top Products ─────────────────────────────────
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.add(buildLowStockPanel());
        bottomPanel.add(buildTopProductsPanel());
        center.add(bottomPanel, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        refreshData();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Card factory — colored left accent bar
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(accentColor);
                g.fillRoundRect(0, 0, 6, getHeight(), 6, 6);
            }
        };
        card.setOpaque(true);
        card.setBackground(ThemeManager.surface());
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ThemeManager.border(), 1, true),
            BorderFactory.createEmptyBorder(14, 18, 14, 14)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(ThemeManager.fontSmall());
        titleLbl.setForeground(ThemeManager.text2());

        valueLabel.setFont(ThemeManager.fontBig());
        valueLabel.setForeground(accentColor);
        valueLabel.setHorizontalAlignment(JLabel.LEFT);

        card.add(titleLbl,   BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Low Stock panel
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildLowStockPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(ThemeManager.surface());
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ThemeManager.danger(), 1, true),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JLabel title = new JLabel("  Low Stock Alerts");
        title.setFont(ThemeManager.fontSection());
        title.setForeground(ThemeManager.danger());
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"Product", "Stock", "Min"};
        lowStockTable = new JTable(new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        
        lowStockTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        lowStockTable.getColumnModel().getColumn(1).setPreferredWidth(55);
        lowStockTable.getColumnModel().getColumn(2).setPreferredWidth(55);

        JScrollPane sp = new JScrollPane(lowStockTable);
        ThemeManager.applyTableTheme(lowStockTable, sp);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Top Products panel
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildTopProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(ThemeManager.surface());
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ThemeManager.success(), 1, true),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JLabel title = new JLabel(" Top Selling Today");
        title.setFont(ThemeManager.fontSection());
        title.setForeground(ThemeManager.success());
        panel.add(title, BorderLayout.NORTH);

        topProductsModel = new DefaultListModel<>();
        JList<String> list = new JList<>(topProductsModel);
        list.setFont(ThemeManager.fontBody());
        list.setBackground(ThemeManager.surface());
        list.setForeground(ThemeManager.text());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane sp = new JScrollPane(list);
        sp.setBackground(ThemeManager.surface());
        sp.getViewport().setBackground(ThemeManager.surface());
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Data refresh
    // ─────────────────────────────────────────────────────────────────────────
    public void refreshData() {
        // ── Today ──────────────────────────────────────────────────────────
        double todaySales   = transactionDAO.getTodayTotalSales();
        double todayProfit  = transactionDAO.getTodayProfit();
        double capitalToday = productDAO.getTodayRestockCost();
        double netToday     = todayProfit - capitalToday;

        lblTodaySales.setText("₱" + String.format("%,.2f", todaySales));
        lblTodayProfit.setText("₱" + String.format("%,.2f", todayProfit));
        lblCapitalToday.setText("₱" + String.format("%,.2f", capitalToday));

        // Net turns green when positive, red when in deficit
        lblNetToday.setText((netToday < 0 ? "\u2212\u20b1" : "\u20b1") + String.format("%,.2f", Math.abs(netToday)));
        lblNetToday.setForeground(netToday >= 0 ? ThemeManager.success() : ThemeManager.danger());

        // \u2500\u2500 All-Time \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500
        double allRevenue = transactionDAO.getAllTimeTotalSales();
        double allProfit  = transactionDAO.getAllTimeProfit();
        double allCapital = productDAO.getAllTimeRestockCost();
        double allNet     = allProfit - allCapital;

        lblAllRevenue.setText("₱" + String.format("%,.2f", allRevenue));
        lblAllProfit.setText("₱" + String.format("%,.2f", allProfit));
        lblAllCapital.setText("₱" + String.format("%,.2f", allCapital));
        lblAllNet.setText((allNet < 0 ? "−₱" : "₱") + String.format("%,.2f", Math.abs(allNet)));
        lblAllNet.setForeground(allNet >= 0 ? ThemeManager.purple() : ThemeManager.danger());

        // ── Low Stock ──────────────────────────────────────────────────────
        javax.swing.table.DefaultTableModel model =
            (javax.swing.table.DefaultTableModel) lowStockTable.getModel();
        model.setRowCount(0);
        for (Product p : productDAO.getLowStock()) {
            model.addRow(new Object[]{p.getProductName(), p.getCurrentStock(), p.getMinStockLevel()});
        }

        // ── Top Products ───────────────────────────────────────────────────
        topProductsModel.clear();
        List<Object[]> top = transactionDAO.getTopSellingProductsToday(5);
        if (top.isEmpty()) {
            topProductsModel.addElement("  No sales recorded today");
        } else {
            int rank = 1;
            for (Object[] row : top) {
                topProductsModel.addElement("  " + rank++ + ".  " + row[0] + "   (" + row[1] + " sold)");
            }
        }
    }
}
