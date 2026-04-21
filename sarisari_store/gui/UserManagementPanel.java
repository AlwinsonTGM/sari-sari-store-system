package gui;

import javax.swing.*;
import java.awt.*;

/**
 * UserManagementPanel — REMOVED.
 * User management has been removed from the simplified system.
 * This stub exists so the file still compiles without errors if referenced elsewhere.
 */
public class UserManagementPanel extends JPanel {

    public UserManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        JLabel lbl = new JLabel("User management has been removed from this system.", JLabel.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        add(lbl, BorderLayout.CENTER);
    }

    public void refreshData() { /* no-op */ }
}
