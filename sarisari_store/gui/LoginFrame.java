package gui;

import dao.UserDAO;
import dao.DBConnection;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * LoginFrame - User Login Screen
 * 
 * This is the entry point of the application.
 * Users must enter valid credentials to access the system.
 * 
 * Default accounts:
 * - Admin: username="admin", password="admin123"
 * - Cashier: username="cashier1", password="cashier123"
 */
public class LoginFrame extends JFrame {
    
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnExit;
    private JCheckBox chkShowPassword;
    private UserDAO userDAO;
    
    public LoginFrame() {
        userDAO = new UserDAO();
        
        // Frame setup
        setTitle("Sari-Sari Store Inventory System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 420);  // Increased height to ensure button visibility
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        mainPanel.setBackground(ThemeManager.bg());
        
        // Header Panel (Logo/Title) - Using a bit more space
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("  Sari-Sari Store System");
        lblTitle.setFont(ThemeManager.fontTitle());
        lblTitle.setForeground(ThemeManager.primary());
        headerPanel.add(lblTitle);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Form panel using GridBagLayout
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(ThemeManager.surface());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.border()),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(ThemeManager.fontBold());
        lblUsername.setForeground(ThemeManager.text());
        formPanel.add(lblUsername, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1.0;
        txtUsername = new JTextField(20);
        txtUsername.setFont(ThemeManager.fontBody());
        txtUsername.setForeground(ThemeManager.text());
        txtUsername.setBackground(ThemeManager.surface());
        txtUsername.setPreferredSize(new Dimension(200, 32));
        formPanel.add(txtUsername, gbc);
        
        // Password
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
        
        // Show Password Checkbox
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
        
        // Button panel - Added vertical gap for visibility
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
        
        // Event listeners
        btnLogin.addActionListener(e -> doLogin());
        btnExit.addActionListener(e -> System.exit(0));
        
        // Key listeners for Enter
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
            }
        });
        
        txtUsername.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) txtPassword.requestFocus();
            }
        });
    }
    
    /**
     * Perform login authentication
     */
    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter both username and password.", 
                "Login Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Authenticate
            User user = userDAO.authenticate(username, password);
            
            if (user != null) {
                // Login successful
                JOptionPane.showMessageDialog(this, 
                    "Welcome, " + user.getFullName() + "!\nRole: " + user.getRole().toUpperCase(), 
                    "Login Successful", JOptionPane.INFORMATION_MESSAGE);
                
                MainFrame mainFrame = new MainFrame(user);
                mainFrame.setVisible(true);
                this.dispose();
            } else {
                // Login failed - invalid credentials
                JOptionPane.showMessageDialog(this, 
                    "Invalid username or password.\nPlease try again.", 
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
                txtPassword.requestFocus();
            }
        } catch (Exception e) {
            // System/Database Error
            JOptionPane.showMessageDialog(this, 
                "A system error occurred during login:\n" + e.getMessage() +
                "\n\nPlease ensure the database is running and reachable.", 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Main entry point
     */
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Test database connection first
        if (!DBConnection.testConnection()) {
            JOptionPane.showMessageDialog(null, 
                "Cannot connect to database.\nPlease ensure MySQL is running and database 'sarisari_db' exists.", 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        // Show login frame
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
