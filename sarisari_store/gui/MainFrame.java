package gui;

import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * MainFrame — Main Application Window
 *
 * Builds the main application window with a top header bar,
 * tabbed content area, status bar, and menu bar.
 */
public class MainFrame extends JFrame {

    private User currentUser;

    // Panels (kept as fields so tab-change listener can reference them)
    private DashboardPanel dashboardPanel;
    private ProductPanel productPanel;
    private SalesPanel salesPanel;
    private SalesHistoryPanel salesHistoryPanel;
    private RestockHistoryPanel restockHistoryPanel;
    private UserManagementPanel userManagementPanel;
    private JTabbedPane tabbedPane;

    // Clock timer — kept as a field so we can stop it before rebuilding
    private Timer clockTimer;

    public MainFrame(User user) {
        this.currentUser = user;
        buildUI();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Build / Rebuild (called on construction and on theme toggle)
    // ─────────────────────────────────────────────────────────────────────────
    private void buildUI() {
        // Stop old clock if rebuilding
        if (clockTimer != null) clockTimer.stop();

        getContentPane().removeAll();
        setLayout(new BorderLayout());

        setTitle("Sari-Sari Store System — " + currentUser.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);

        // ── Top Header Bar ──────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(ThemeManager.primary());
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblLogo = new JLabel("  Sari-Sari Store System");
        lblLogo.setFont(ThemeManager.fontTitle());
        lblLogo.setForeground(Color.WHITE);
        header.add(lblLogo, BorderLayout.WEST);

        // Right side of header — GridBagLayout centers items vertically
        JPanel headerRight = new JPanel(new GridBagLayout());
        headerRight.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 12, 0, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel lblUser = new JLabel("  " + currentUser.getFullName()
            + "  |  " + currentUser.getRole().toUpperCase());
        lblUser.setFont(ThemeManager.fontBold());
        lblUser.setForeground(Color.WHITE);
        headerRight.add(lblUser, gbc);

        // Clock label
        JLabel lblClock = new JLabel();
        lblClock.setFont(ThemeManager.fontBold());
        lblClock.setForeground(Color.WHITE);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd  \u2022  hh:mm:ss a  ");
        lblClock.setText(sdf.format(new Date()));
        headerRight.add(lblClock, gbc);

        clockTimer = new Timer(1000, e -> lblClock.setText(sdf.format(new Date())));
        clockTimer.start();

        header.add(headerRight, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Tabbed Content Area ────────────────────────────────────────────
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(ThemeManager.fontBold());
        ThemeManager.applyTabbedPaneTheme(tabbedPane);

        dashboardPanel    = new DashboardPanel();
        salesPanel        = new SalesPanel(currentUser);
        productPanel      = new ProductPanel(currentUser);
        salesHistoryPanel = new SalesHistoryPanel();
        if (currentUser.isAdmin()) {
            restockHistoryPanel = new RestockHistoryPanel();
        }

        tabbedPane.addTab("   Dashboard  ",   dashboardPanel);
        tabbedPane.addTab("   Point of Sale  ", salesPanel);
        tabbedPane.addTab("   Products  ",     productPanel);
        tabbedPane.addTab("   Sales History  ", salesHistoryPanel);
        if (currentUser.isAdmin()) {
            tabbedPane.addTab("   Restock History  ", restockHistoryPanel);
        }

        if (currentUser.isAdmin()) {
            userManagementPanel = new UserManagementPanel();
            tabbedPane.addTab("   Users  ", userManagementPanel);
        }

        // Refresh panels on tab switch
        tabbedPane.addChangeListener(e -> {
            Component sel = tabbedPane.getSelectedComponent();
            if (sel == dashboardPanel)    dashboardPanel.refreshData();
            else if (sel == productPanel) productPanel.refreshData();
            else if (sel == salesHistoryPanel) salesHistoryPanel.refreshData();
            else if (sel == restockHistoryPanel && restockHistoryPanel != null) restockHistoryPanel.refreshData();
        });

        add(tabbedPane, BorderLayout.CENTER);

        // ── Status Bar ─────────────────────────────────────────────────────
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeManager.border()));
        statusBar.setBackground(new Color(226, 232, 240));
        statusBar.setPreferredSize(new Dimension(0, 28));

        JLabel lblStatus = new JLabel("   Logged in as: " + currentUser.getFullName()
            + "   |   Role: " + currentUser.getRole().toUpperCase());
        lblStatus.setFont(ThemeManager.fontSmall());
        lblStatus.setForeground(ThemeManager.text2());
        statusBar.add(lblStatus, BorderLayout.WEST);

        JLabel lblVersion = new JLabel("Sari-Sari Store System v1.0   ");
        lblVersion.setFont(ThemeManager.fontSmall());
        lblVersion.setForeground(ThemeManager.text2());
        statusBar.add(lblVersion, BorderLayout.EAST);

        add(statusBar, BorderLayout.SOUTH);

        // ── Menu Bar ────────────────────────────────────────────────────────
        buildMenuBar();

        revalidate();
        repaint();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Menu bar
    // ─────────────────────────────────────────────────────────────────────────
    private void buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem refreshItem = new JMenuItem("Refresh");
        refreshItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        refreshItem.addActionListener(e -> refreshCurrentTab());

        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> doLogout());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        fileMenu.add(logoutItem);
        fileMenu.add(exitItem);



        JMenu navMenu = new JMenu("Navigate");
        navMenu.setMnemonic(KeyEvent.VK_N);
        addNavItem(navMenu, "Dashboard",    KeyEvent.VK_D, () -> tabbedPane.setSelectedComponent(dashboardPanel));
        addNavItem(navMenu, "Point of Sale", KeyEvent.VK_S, () -> tabbedPane.setSelectedComponent(salesPanel));
        addNavItem(navMenu, "Products",     KeyEvent.VK_P, () -> tabbedPane.setSelectedComponent(productPanel));
        addNavItem(navMenu, "Sales History", KeyEvent.VK_H, () -> tabbedPane.setSelectedComponent(salesHistoryPanel));
        if (currentUser.isAdmin() && restockHistoryPanel != null) {
            addNavItem(navMenu, "Restock History", KeyEvent.VK_R, () -> tabbedPane.setSelectedComponent(restockHistoryPanel));
        }
        if (currentUser.isAdmin() && userManagementPanel != null) {
            addNavItem(navMenu, "User Management", KeyEvent.VK_U, () -> tabbedPane.setSelectedComponent(userManagementPanel));
        }

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "Sari-Sari Store Inventory & Sales System\n\nVersion: 1.0\nBuilt with Java Swing + MySQL\n\n" +
            "Features: POS · Product Management · Stock Tracking\n" +
            "Capital Expense Tracking"));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(navMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void addNavItem(JMenu menu, String label, int mnemonic, Runnable action) {
        JMenuItem item = new JMenuItem(label, mnemonic);
        item.addActionListener(e -> action.run());
        menu.add(item);
    }

    private void refreshCurrentTab() {
        Component sel = tabbedPane.getSelectedComponent();
        if (sel == dashboardPanel)         dashboardPanel.refreshData();
        else if (sel == productPanel)      productPanel.refreshData();
        else if (sel == salesHistoryPanel) salesHistoryPanel.refreshData();
        else if (sel == restockHistoryPanel && restockHistoryPanel != null) restockHistoryPanel.refreshData();
    }

    private void doLogout() {
        if (clockTimer != null) clockTimer.stop();
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new LoginFrame().setVisible(true);
            dispose();
        }
    }
}
