package gui;

import dao.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * LoginFrame - User Login Screen (Simplified, hardcoded authentication)
 *
 * Credentials are hardcoded — no database user table required.
 * Default accounts:
 *   owner   / owner123
 *   cashier / cashier123
 */
public class LoginFrame extends JFrame {

    private JTextField     txtUsername;
    private JPasswordField txtPassword;
    private JButton        btnLogin;
    private JButton        btnExit;
    private JCheckBox      chkShowPassword;

    // Hardcoded credentials  [username, password, displayName, role]
    private static final String[][] CREDENTIALS = {
        { "owner",   "owner123",   "Store Owner", "owner"   },
        { "cashier", "cashier123", "Cashier",     "cashier" }
    };

    public LoginFrame() {
        setTitle("Sari-Sari Store Inventory System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 420);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        mainPanel.setBackground(ThemeManager.bg());

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("  Sari-Sari Store System");
        lblTitle.setFont(ThemeManager.fontTitle());
        lblTitle.setForeground(ThemeManager.primary());
        headerPanel.add(lblTitle);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(ThemeManager.surface());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.border()),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(ThemeManager.fontBold());
        lblUsername.setForeground(ThemeManager.text());
        formPanel.add(lblUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 1.0;
        txtUsername = new JTextField(20);
        txtUsername.setFont(ThemeManager.fontBody());
        txtUsername.setForeground(ThemeManager.text());
        txtUsername.setBackground(ThemeManager.surface());
        txtUsername.setPreferredSize(new Dimension(200, 32));
        formPanel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.insets = new Insets(10, 5, 5, 5);
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(ThemeManager.fontBold());
        lblPassword.setForeground(ThemeManager.text());
        formPanel.add(lblPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.insets = new Insets(5, 5, 5, 5);
        txtPassword = new JPasswordField(20);
        txtPassword.setFont(ThemeManager.fontBody());
        txtPassword.setForeground(ThemeManager.text());
        txtPassword.setBackground(ThemeManager.surface());
        txtPassword.setPreferredSize(new Dimension(200, 32));
        formPanel.add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.insets = new Insets(2, 5, 5, 5);
        chkShowPassword = new JCheckBox("Show Password");
        chkShowPassword.setFont(ThemeManager.fontSmall());
        chkShowPassword.setForeground(ThemeManager.text2());
        chkShowPassword.setBackground(ThemeManager.surface());
        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('\u2022');
            }
        });
        formPanel.add(chkShowPassword, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);

        btnLogin = new JButton("Login");
        ThemeManager.styleButton(btnLogin, "primary");
        btnLogin.setPreferredSize(new Dimension(130, 40));

        btnExit = new JButton("Exit");
        ThemeManager.styleButton(btnExit, "neutral");
        btnExit.setPreferredSize(new Dimension(100, 40));

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnExit);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Listeners
        btnLogin.addActionListener(e -> doLogin());
        btnExit.addActionListener(e -> System.exit(0));

        txtPassword.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
            }
        });
        txtUsername.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) txtPassword.requestFocus();
            }
        });
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter both username and password.",
                "Login Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Hardcoded credential check
        for (String[] cred : CREDENTIALS) {
            if (cred[0].equalsIgnoreCase(username) && cred[1].equals(password)) {
                String displayName = cred[2];
                String role        = cred[3];

                JOptionPane.showMessageDialog(this,
                    "Welcome, " + displayName + "!\nRole: " + role.toUpperCase(),
                    "Login Successful", JOptionPane.INFORMATION_MESSAGE);

                MainFrame mainFrame = new MainFrame(displayName, role);
                mainFrame.setVisible(true);
                this.dispose();
                return;
            }
        }

        JOptionPane.showMessageDialog(this,
            "Invalid username or password.\nPlease try again.",
            "Login Failed", JOptionPane.ERROR_MESSAGE);
        txtPassword.setText("");
        txtPassword.requestFocus();
    }

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        if (!DBConnection.testConnection()) {
            JOptionPane.showMessageDialog(null,
                "Cannot connect to database.\nPlease ensure MySQL is running and database 'sarisari_db' exists.",
                "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
