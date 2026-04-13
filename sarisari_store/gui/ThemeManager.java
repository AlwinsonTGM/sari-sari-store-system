package gui;

import java.awt.*;

/**
 * ThemeManager - Centralized color and font system
 *
 * Provides the application-wide light color palette.
 * All panels read colors from here for visual consistency.
 */
public class ThemeManager {

    // ─────────────────────────────────────────────────────────────────────────
    // COLOR PALETTE
    // ─────────────────────────────────────────────────────────────────────────
    public static final Color BG        = new Color(245, 247, 250);
    public static final Color SURFACE   = new Color(255, 255, 255);
    public static final Color PRIMARY   = new Color(0,   102, 153);
    public static final Color SUCCESS   = new Color(0,   153, 76);
    public static final Color DANGER    = new Color(200, 0,   0);
    public static final Color WARNING   = new Color(220, 110, 0);
    public static final Color PURPLE    = new Color(100, 45,  180);
    public static final Color TEXT      = new Color(33,  37,  41);
    public static final Color TEXT2     = new Color(100, 110, 125);
    public static final Color BORDER    = new Color(210, 215, 222);

    public static final Color BTN_PRIMARY = new Color(224, 238, 255);
    public static final Color BTN_SUCCESS = new Color(225, 245, 230);
    public static final Color BTN_DANGER  = new Color(255, 226, 226);
    public static final Color BTN_WARNING = new Color(255, 241, 215);
    public static final Color BTN_NEUTRAL = new Color(240, 242, 245);

    static {
        updateUIManager();
    }

    public static void updateUIManager() {
        javax.swing.UIManager.put("Label.foreground", TEXT);
        javax.swing.UIManager.put("CheckBox.foreground", TEXT);
        javax.swing.UIManager.put("CheckBox.background", BG);
        javax.swing.UIManager.put("TextField.background", SURFACE);
        javax.swing.UIManager.put("TextField.foreground", TEXT);
        javax.swing.UIManager.put("TextField.caretForeground", TEXT);
        javax.swing.UIManager.put("FormattedTextField.background", SURFACE);
        javax.swing.UIManager.put("FormattedTextField.foreground", TEXT);
        javax.swing.UIManager.put("FormattedTextField.caretForeground", TEXT);
        javax.swing.UIManager.put("ComboBox.background", SURFACE);
        javax.swing.UIManager.put("ComboBox.foreground", TEXT);
        javax.swing.UIManager.put("ComboBox.selectionBackground", PRIMARY);
        javax.swing.UIManager.put("ComboBox.selectionForeground", java.awt.Color.WHITE);
        javax.swing.UIManager.put("Panel.background", BG);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SEMANTIC GETTERS
    // ─────────────────────────────────────────────────────────────────────────
    public static Color bg()       { return BG;        }
    public static Color surface()  { return SURFACE;   }
    public static Color primary()  { return PRIMARY;   }
    public static Color success()  { return SUCCESS;   }
    public static Color danger()   { return DANGER;    }
    public static Color warning()  { return WARNING;   }
    public static Color purple()   { return PURPLE;    }
    public static Color text()     { return TEXT;      }
    public static Color text2()    { return TEXT2;     }
    public static Color border()   { return BORDER;    }

    public static Color btnPrimary() { return BTN_PRIMARY; }
    public static Color btnSuccess() { return BTN_SUCCESS; }
    public static Color btnDanger()  { return BTN_DANGER;  }
    public static Color btnWarning() { return BTN_WARNING; }
    public static Color btnNeutral() { return BTN_NEUTRAL; }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER: Style a standard JButton with a theme type
    // ─────────────────────────────────────────────────────────────────────────
    public static void styleButton(javax.swing.JButton btn, String type) {
        Color bg;
        switch (type) {
            case "primary": bg = btnPrimary(); break;
            case "success": bg = btnSuccess(); break;
            case "danger":  bg = btnDanger();  break;
            case "warning": bg = btnWarning(); break;
            default:        bg = btnNeutral(); break;
        }
        // Bypass Windows Look and Feel ignoring backgrounds
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setBackground(bg);
        btn.setForeground(text());
        btn.setFont(fontBold());
        btn.setFocusPainted(false);
        // Important for BasicButtonUI to paint the background
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(border(), 1, true),
            javax.swing.BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FONTS — Consistent Segoe UI hierarchy
    // ─────────────────────────────────────────────────────────────────────────
    public static Font fontTitle()   { return new Font("Segoe UI", Font.BOLD,  22); }
    public static Font fontSection() { return new Font("Segoe UI", Font.BOLD,  16); }
    public static Font fontBody()    { return new Font("Segoe UI", Font.PLAIN, 13); }
    public static Font fontBold()    { return new Font("Segoe UI", Font.BOLD,  13); }
    public static Font fontSmall()   { return new Font("Segoe UI", Font.PLAIN, 11); }
    public static Font fontBig()     { return new Font("Segoe UI", Font.BOLD,  30); }
    public static Font fontMega()    { return new Font("Segoe UI", Font.BOLD,  38); }

    // ─────────────────────────────────────────────────────────────────────────
    // COMPONENT THEMING (Tables, ScrollPanes)
    // ─────────────────────────────────────────────────────────────────────────
    public static void applyTableTheme(javax.swing.JTable table, javax.swing.JScrollPane scrollPane) {
        table.setFont(fontBody());
        table.setRowHeight(26);
        table.setBackground(surface());
        table.setForeground(text());
        table.setGridColor(border());

        // Force basic UI on table header to prevent Windows native theming from overriding colors
        table.getTableHeader().setUI(new javax.swing.plaf.basic.BasicTableHeaderUI());
        table.getTableHeader().setFont(fontBold());
        table.getTableHeader().setBackground(bg());
        table.getTableHeader().setForeground(text());
        table.getTableHeader().setOpaque(true);

        // Custom renderer for column headers
        javax.swing.table.DefaultTableCellRenderer headerRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(javax.swing.JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                c.setBackground(bg());
                c.setForeground(text());
                c.setFont(fontBold());
                ((javax.swing.JComponent)c).setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 1, border()),
                    javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4)
                ));
                return c;
            }
        };
        table.getTableHeader().setDefaultRenderer(headerRenderer);

        // Custom renderer to force correct colors for all cells
        class ThemedCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(javax.swing.JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(surface());
                    c.setForeground(text());
                } else {
                    c.setBackground(primary());
                    c.setForeground(Color.WHITE);
                }
                // Ensure opaque to hide default white background
                ((javax.swing.JComponent)c).setOpaque(true);
                return c;
            }
        }

        ThemedCellRenderer renderer = new ThemedCellRenderer();
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(String.class, renderer);
        table.setDefaultRenderer(Number.class, renderer);
        table.setDefaultRenderer(Integer.class, renderer);
        table.setDefaultRenderer(Double.class, renderer);

        if (scrollPane != null) {
            scrollPane.setBackground(surface());
            scrollPane.getViewport().setBackground(surface());
            scrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(border()));

            // Fix upper right corner being default white/gray
            javax.swing.JPanel corner = new javax.swing.JPanel();
            corner.setBackground(bg());
            scrollPane.setCorner(javax.swing.JScrollPane.UPPER_RIGHT_CORNER, corner);
        }
    }

    public static void applyTabbedPaneTheme(javax.swing.JTabbedPane tabbedPane) {
        tabbedPane.setBackground(bg());
        tabbedPane.setForeground(text());
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                lightHighlight = border();
                highlight = border();
                shadow = border();
                darkShadow = border();
                focus = surface();
            }
        });
    }
}
