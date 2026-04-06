package gui;

import dao.UserDAO;
import model.User;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * UserManagementPanel - User Management Screen (Admin Only)
 * 
 * Features:
 * - View all users
 * - Add new users
 * - Edit user details
 * - Reset passwords
 * - Activate/Deactivate users
 */
public class UserManagementPanel extends JPanel {
    
    private UserDAO userDAO;
    
    private JTable userTable;
    private DefaultTableModel tableModel;
    private List<User> currentUsers;
    
    public UserManagementPanel() {
        userDAO = new UserDAO();
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(ThemeManager.bg());
        
        // Title
        JLabel lblTitle = new JLabel(" User Management");
        lblTitle.setFont(ThemeManager.fontSection());
        lblTitle.setForeground(ThemeManager.primary());
        add(lblTitle, BorderLayout.NORTH);
        
        // Table panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Load data
        refreshData();
    }
    
    /**
     * Create the users table panel
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.surface());
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ThemeManager.border()),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Table
        String[] columns = {"ID", "Username", "Full Name", "Role", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        TableColumnModel columnModel = userTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(200);
        columnModel.getColumn(3).setPreferredWidth(80);
        columnModel.getColumn(4).setPreferredWidth(80);
        
        JScrollPane scrollPane = new JScrollPane(userTable);
        ThemeManager.applyTableTheme(userTable, scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create the button panel
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setOpaque(false);
        
        JButton btnAdd = new JButton(" Add User");
        ThemeManager.styleButton(btnAdd, "success");
        btnAdd.addActionListener(e -> showAddUserDialog());
        panel.add(btnAdd);
        
        JButton btnEdit = new JButton(" Edit User");
        ThemeManager.styleButton(btnEdit, "primary");
        btnEdit.addActionListener(e -> showEditUserDialog());
        panel.add(btnEdit);
        
        JButton btnResetPassword = new JButton(" Reset Password");
        ThemeManager.styleButton(btnResetPassword, "warning");
        btnResetPassword.addActionListener(e -> resetPassword());
        panel.add(btnResetPassword);
        
        JButton btnToggleStatus = new JButton(" Activate/Deactivate");
        ThemeManager.styleButton(btnToggleStatus, "danger");
        btnToggleStatus.addActionListener(e -> toggleUserStatus());
        panel.add(btnToggleStatus);
        
        JButton btnRefresh = new JButton("↺ Refresh");
        ThemeManager.styleButton(btnRefresh, "neutral");
        btnRefresh.addActionListener(e -> refreshData());
        panel.add(btnRefresh);
        
        return panel;
    }
    
    /**
     * Refresh user data
     */
    public void refreshData() {
        currentUsers = userDAO.getAll();
        refreshTable();
    }
    
    /**
     * Refresh the table
     */
    private void refreshTable() {
        tableModel.setRowCount(0);
        
        for (User user : currentUsers) {
            tableModel.addRow(new Object[]{
                user.getUserId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole().toUpperCase(),
                user.isActive() ? "ACTIVE" : "INACTIVE"
            });
        }
    }
    
    /**
     * Show dialog to add a new user
     */
    private void showAddUserDialog() {
        JTextField txtUsername = new JTextField(20);
        JTextField txtFullName = new JTextField(20);
        JPasswordField txtPassword = new JPasswordField(20);
        String[] roles = {"cashier", "admin"};
        JComboBox<String> cmbRole = new JComboBox<>(roles);
        
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panel.add(new JLabel("Username:*"));
        panel.add(txtUsername);
        panel.add(new JLabel("Full Name:*"));
        panel.add(txtFullName);
        panel.add(new JLabel("Password:*"));
        panel.add(txtPassword);
        panel.add(new JLabel("Role:*"));
        panel.add(cmbRole);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Add New User", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String username = txtUsername.getText().trim();
            String fullName = txtFullName.getText().trim();
            String password = new String(txtPassword.getPassword());
            String role = (String) cmbRole.getSelectedItem();
            
            if (username.isEmpty() || fullName.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (userDAO.usernameExists(username)) {
                JOptionPane.showMessageDialog(this, "Username already exists.", "Duplicate Username", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            User newUser = new User(username, password, fullName, role, true);
            
            if (userDAO.add(newUser)) {
                JOptionPane.showMessageDialog(this, "User added successfully!");
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Show dialog to edit a user
     */
    private void showEditUserDialog() {
        int row = userTable.getSelectedRow();
        if (row < 0 || row >= currentUsers.size()) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        User user = currentUsers.get(row);
        
        JTextField txtUsername = new JTextField(user.getUsername(), 20);
        JTextField txtFullName = new JTextField(user.getFullName(), 20);
        String[] roles = {"cashier", "admin"};
        JComboBox<String> cmbRole = new JComboBox<>(roles);
        cmbRole.setSelectedItem(user.getRole());
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panel.add(new JLabel("Username:*"));
        panel.add(txtUsername);
        panel.add(new JLabel("Full Name:*"));
        panel.add(txtFullName);
        panel.add(new JLabel("Role:*"));
        panel.add(cmbRole);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Edit User", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String username = txtUsername.getText().trim();
            String fullName = txtFullName.getText().trim();
            String role = (String) cmbRole.getSelectedItem();
            
            if (username.isEmpty() || fullName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Check if username changed and new username exists
            if (!username.equals(user.getUsername()) && userDAO.usernameExists(username)) {
                JOptionPane.showMessageDialog(this, "Username already exists.", "Duplicate Username", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            user.setUsername(username);
            user.setFullName(fullName);
            user.setRole(role);
            
            if (userDAO.update(user)) {
                JOptionPane.showMessageDialog(this, "User updated successfully!");
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Reset user password
     */
    private void resetPassword() {
        int row = userTable.getSelectedRow();
        if (row < 0 || row >= currentUsers.size()) {
            JOptionPane.showMessageDialog(this, "Please select a user.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        User user = currentUsers.get(row);
        
        JPasswordField txtNewPassword = new JPasswordField(20);
        JPasswordField txtConfirmPassword = new JPasswordField(20);
        
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panel.add(new JLabel("New Password:*"));
        panel.add(txtNewPassword);
        panel.add(new JLabel("Confirm Password:*"));
        panel.add(txtConfirmPassword);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Reset Password for " + user.getFullName(), 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String newPassword = new String(txtNewPassword.getPassword());
            String confirmPassword = new String(txtConfirmPassword.getPassword());
            
            if (newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (userDAO.updatePassword(user.getUserId(), newPassword)) {
                JOptionPane.showMessageDialog(this, "Password reset successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to reset password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Toggle user active/inactive status
     */
    private void toggleUserStatus() {
        int row = userTable.getSelectedRow();
        if (row < 0 || row >= currentUsers.size()) {
            JOptionPane.showMessageDialog(this, "Please select a user.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        User user = currentUsers.get(row);
        
        String action = user.isActive() ? "deactivate" : "activate";
        String status = user.isActive() ? "INACTIVE" : "ACTIVE";
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to " + action + " user '\"" + user.getFullName() + "\"?\n\n" +
            "Current status: " + (user.isActive() ? "ACTIVE" : "INACTIVE") + "\n" +
            "New status: " + status,
            "Confirm " + action.toUpperCase(), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            user.setActive(!user.isActive());
            
            if (userDAO.update(user)) {
                JOptionPane.showMessageDialog(this, "User " + action + "d successfully!");
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to " + action + " user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
