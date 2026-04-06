package gui;

import dao.ProductDAO;
import model.Product;
import model.User;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.io.File;
import java.nio.file.*;
import java.sql.Date;
import java.util.List;

/**
 * ProductPanel - Product Management Screen
 * 
 * Features:
 * - View all products in a table
 * - Search products by name or code
 * - Add new products (Admin only)
 * - Edit products (Admin only)
 * - Restock products (Admin only)
 * - Deactivate products (Admin only)
 * - View product image
 * 
 * Cashiers can only view products (read-only access)
 */
public class ProductPanel extends JPanel {
    
    private User currentUser;
    private ProductDAO productDAO;
    
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JLabel lblImagePreview;
    
    // View tabs
    private JTabbedPane viewTabs;
    private JPanel cardViewContainer;
    private JComboBox<String> cmbSort;
    
    // Form fields for add/edit
    private JTextField txtProductCode;
    private JTextField txtProductName;
    private JTextField txtCategory;
    private JTextField txtUnit;
    private JTextField txtPurchasePrice;
    private JTextField txtSRP;
    private JTextField txtStock;
    private JTextField txtMinStock;
    private JTextField txtExpiryDate;
    private JTextField txtImagePath;
    
    private List<Product> currentProducts;
    
    public ProductPanel(User user) {
        this.currentUser = user;
        this.productDAO = new ProductDAO();
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(ThemeManager.bg());
        
        // Title and search panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel(" Product Management");
        lblTitle.setFont(ThemeManager.fontSection());
        lblTitle.setForeground(ThemeManager.primary());
        topPanel.add(lblTitle, BorderLayout.WEST);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        
        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 12));
        txtSearch.addActionListener(e -> searchProducts()); // Enter key support
        
        JButton btnSearch = new JButton("Search");
        btnSearch.setFont(new Font("Arial", Font.BOLD, 12));
        btnSearch.addActionListener(e -> searchProducts());
        
        JButton btnClear = new JButton("Clear");
        btnClear.setFont(new Font("Arial", Font.BOLD, 12));
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            loadAllProducts();
        });
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnClear);
        
        // Sort combobox
        cmbSort = new JComboBox<>(new String[]{
            "Default", "Name (A-Z)", "Name (Z-A)", 
            "Category (A-Z)", "Category (Z-A)", 
            "Expiry (Soonest)", "Expiry (Latest)", 
            "Price (Low-High)", "Price (High-Low)"
        });
        cmbSort.setFont(new Font("Arial", Font.PLAIN, 12));
        cmbSort.addActionListener(e -> applySort());
        
        searchPanel.add(new JLabel("  Sort by:"));
        searchPanel.add(cmbSort);
        
        topPanel.add(searchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
        
        // Center: Tabs for Table View and Card View
        viewTabs = new JTabbedPane();
        viewTabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ThemeManager.applyTabbedPaneTheme(viewTabs);
        
        // --- TABLE VIEW PANEL ---
        JPanel tableViewPanel = new JPanel(new BorderLayout(10, 10));
        tableViewPanel.setOpaque(false);
        
        // Product table
        String[] columns = {"Code", "Name", "Category", "Unit", "Purchase", "SRP", "Stock", "Min"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Read-only table
            }
        };
        
        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        
        // Set column widths
        TableColumnModel columnModel = productTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(80);
        columnModel.getColumn(1).setPreferredWidth(150);
        columnModel.getColumn(2).setPreferredWidth(100);
        columnModel.getColumn(3).setPreferredWidth(60);
        columnModel.getColumn(4).setPreferredWidth(70);
        columnModel.getColumn(5).setPreferredWidth(70);
        columnModel.getColumn(6).setPreferredWidth(50);
        columnModel.getColumn(7).setPreferredWidth(50);
        
        // Selection listener to show image
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedProductImage();
            }
        });
        
        JScrollPane tableScroll = new JScrollPane(productTable);
        ThemeManager.applyTableTheme(productTable, tableScroll);
        tableViewPanel.add(tableScroll, BorderLayout.CENTER);
        
        // Image preview panel
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(ThemeManager.surface());
        imagePanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ThemeManager.border()),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        imagePanel.setPreferredSize(new Dimension(250, 300));
        
        JLabel lblImageTitle = new JLabel("Product Image", JLabel.CENTER);
        lblImageTitle.setFont(ThemeManager.fontBold());
        lblImageTitle.setForeground(ThemeManager.text());
        imagePanel.add(lblImageTitle, BorderLayout.NORTH);
        
        lblImagePreview = new JLabel("No Image", JLabel.CENTER);
        lblImagePreview.setFont(ThemeManager.fontSmall());
        lblImagePreview.setForeground(ThemeManager.text2());
        imagePanel.add(lblImagePreview, BorderLayout.CENTER);
        
        tableViewPanel.add(imagePanel, BorderLayout.EAST);
        
        // --- CARD VIEW PANEL ---
        cardViewContainer = new JPanel(new BorderLayout());
        cardViewContainer.setOpaque(false);
        // It will be populated in refreshData()
        
        viewTabs.addTab(" Table View", tableViewPanel);
        viewTabs.addTab("️ Card View", cardViewContainer);
        
        add(viewTabs, BorderLayout.CENTER);
        
        // Button panel (Admin only)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);
        
        if (currentUser.isAdmin()) {
            JButton btnAdd = new JButton(" Add Product");
            ThemeManager.styleButton(btnAdd, "success");
            btnAdd.addActionListener(e -> showAddProductDialog());
            
            JButton btnEdit = new JButton(" Edit");
            ThemeManager.styleButton(btnEdit, "primary");
            btnEdit.addActionListener(e -> showEditProductDialog());
            
            JButton btnRestock = new JButton(" Restock");
            ThemeManager.styleButton(btnRestock, "warning");
            btnRestock.addActionListener(e -> showRestockDialog());
            
            JButton btnDeactivate = new JButton(" Deactivate");
            ThemeManager.styleButton(btnDeactivate, "danger");
            btnDeactivate.addActionListener(e -> deactivateProduct());
            
            buttonPanel.add(btnAdd);
            buttonPanel.add(btnEdit);
            buttonPanel.add(btnRestock);
            buttonPanel.add(btnDeactivate);
        } else {
            JLabel lblReadOnly = new JLabel("Read-only access (Cashier)");
            lblReadOnly.setFont(new Font("Arial", Font.ITALIC, 12));
            lblReadOnly.setForeground(Color.GRAY);
            buttonPanel.add(lblReadOnly);
        }
        
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Arial", Font.BOLD, 12));
        btnRefresh.addActionListener(e -> refreshData());
        buttonPanel.add(btnRefresh);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Load initial data
        loadAllProducts();
    }
    
    /**
     * Load all products into the table
     */
    private void loadAllProducts() {
        currentProducts = productDAO.getAllActive();
        applySort();
    }
    
    /**
     * Search products
     */
    private void searchProducts() {
        String searchTerm = txtSearch.getText().trim();
        if (searchTerm.isEmpty()) {
            loadAllProducts();
            return;
        }
        
        currentProducts = productDAO.search(searchTerm);
        applySort();
    }
    
    /**
     * Apply the selected sorting logic to the current products
     */
    private void applySort() {
        if (currentProducts == null || currentProducts.isEmpty() || cmbSort == null) {
            refreshTable();
            return;
        }
        
        String sortCriteria = (String) cmbSort.getSelectedItem();
        if ("Default".equals(sortCriteria)) {
            // Usually ID based or what comes from DB
            currentProducts.sort((p1, p2) -> Integer.compare(p1.getProductId(), p2.getProductId()));
        } else {
            currentProducts.sort((p1, p2) -> {
                if ("Name (A-Z)".equals(sortCriteria)) {
                    return p1.getProductName().compareToIgnoreCase(p2.getProductName());
                } else if ("Name (Z-A)".equals(sortCriteria)) {
                    return p2.getProductName().compareToIgnoreCase(p1.getProductName());
                } else if ("Category (A-Z)".equals(sortCriteria)) {
                    String c1 = p1.getCategory() == null ? "" : p1.getCategory();
                    String c2 = p2.getCategory() == null ? "" : p2.getCategory();
                    return c1.compareToIgnoreCase(c2);
                } else if ("Category (Z-A)".equals(sortCriteria)) {
                    String c1 = p1.getCategory() == null ? "" : p1.getCategory();
                    String c2 = p2.getCategory() == null ? "" : p2.getCategory();
                    return c2.compareToIgnoreCase(c1);
                } else if ("Expiry (Soonest)".equals(sortCriteria)) {
                    java.util.Date d1 = p1.getExpiryDate();
                    java.util.Date d2 = p2.getExpiryDate();
                    if (d1 == null && d2 == null) return 0;
                    if (d1 == null) return 1; // nulls at bottom
                    if (d2 == null) return -1;
                    return d1.compareTo(d2);
                } else if ("Expiry (Latest)".equals(sortCriteria)) {
                    java.util.Date d1 = p1.getExpiryDate();
                    java.util.Date d2 = p2.getExpiryDate();
                    if (d1 == null && d2 == null) return 0;
                    if (d1 == null) return 1; // nulls at bottom
                    if (d2 == null) return -1;
                    return d2.compareTo(d1);
                } else if ("Price (Low-High)".equals(sortCriteria)) {
                    return Double.compare(p1.getSrp(), p2.getSrp());
                } else if ("Price (High-Low)".equals(sortCriteria)) {
                    return Double.compare(p2.getSrp(), p1.getSrp());
                }
                return 0;
            });
        }
        
        refreshTable();
    }
    
    /**
     * Refresh the table with current products list
     */
    private void refreshTable() {
        tableModel.setRowCount(0);
        
        for (Product p : currentProducts) {
            tableModel.addRow(new Object[]{
                p.getProductCode(),
                p.getProductName(),
                p.getCategory(),
                p.getUnit(),
                String.format("%.2f", p.getPurchasePrice()),
                String.format("%.2f", p.getSrp()),
                p.getCurrentStock(),
                p.getMinStockLevel()
            });
        }
        
        // Refresh Card View
        updateCardView();
    }
    
    /**
     * Rebuild the card view container
     */
    private void updateCardView() {
        cardViewContainer.removeAll();
        
        if (currentProducts == null || currentProducts.isEmpty()) {
            cardViewContainer.add(new JLabel("No products found.", JLabel.CENTER), BorderLayout.CENTER);
            cardViewContainer.revalidate();
            cardViewContainer.repaint();
            return;
        }
        
        int cols = 4;
        int rows = (int) Math.ceil((double) currentProducts.size() / cols);
        JPanel grid = new JPanel(new GridLayout(rows, cols, 15, 15));
        grid.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        grid.setBackground(ThemeManager.bg());
        
        for (Product p : currentProducts) {
            JPanel card = new JPanel(new BorderLayout(5, 5));
            card.setBackground(ThemeManager.surface());
            card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ThemeManager.border(), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            
            // Image
            JLabel imgLabel = new JLabel("No Image", JLabel.CENTER);
            imgLabel.setPreferredSize(new Dimension(100, 100));
            if (p.getImagePath() != null && !p.getImagePath().isEmpty()) {
                File imgFile = new File(p.getImagePath());
                if (imgFile.exists()) {
                    ImageIcon icon = new ImageIcon(p.getImagePath());
                    Image baseImg = icon.getImage();
                    
                    int w = icon.getIconWidth();
                    int h = icon.getIconHeight();
                    if (w > 0 && h > 0) {
                        double ratio = Math.min(100.0 / w, 100.0 / h);
                        Image scaled = baseImg.getScaledInstance((int)(w * ratio), (int)(h * ratio), Image.SCALE_SMOOTH);
                        imgLabel.setIcon(new ImageIcon(scaled));
                        imgLabel.setText("");
                    }
                }
            }
            card.add(imgLabel, BorderLayout.NORTH);
            
            // Details
            JPanel details = new JPanel(new GridLayout(6, 1));
            details.setBackground(ThemeManager.surface());
            
            JLabel nameLbl = new JLabel(p.getProductName(), JLabel.CENTER);
            nameLbl.setFont(ThemeManager.fontBold());
            nameLbl.setForeground(ThemeManager.text());
            
            JLabel categoryLbl = new JLabel((p.getCategory() == null || p.getCategory().isEmpty()) ? "Uncategorized" : p.getCategory(), JLabel.CENTER);
            categoryLbl.setFont(ThemeManager.fontSmall());
            categoryLbl.setForeground(ThemeManager.text2());
            
            JLabel priceLbl = new JLabel("SRP: ₱" + String.format("%.2f", p.getSrp()), JLabel.CENTER);
            priceLbl.setFont(ThemeManager.fontBody());
            priceLbl.setForeground(ThemeManager.primary());
            
            JLabel expiryLbl = new JLabel("Expiry: " + (p.getExpiryDate() != null ? p.getExpiryDate().toString() : "N/A"), JLabel.CENTER);
            expiryLbl.setFont(ThemeManager.fontSmall());
            if (p.getExpiryDate() != null) {
                java.util.Date today = new java.util.Date();
                if (p.getExpiryDate().before(today)) {
                    expiryLbl.setForeground(ThemeManager.danger());
                    expiryLbl.setText("EXPIRED: " + p.getExpiryDate().toString());
                    expiryLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                } else {
                    expiryLbl.setForeground(ThemeManager.text2());
                }
            } else {
                expiryLbl.setForeground(ThemeManager.text2());
            }
            
            JLabel stockLbl = new JLabel("Stock: " + p.getCurrentStock(), JLabel.CENTER);
            stockLbl.setFont(ThemeManager.fontBody());
            if (p.getCurrentStock() <= p.getMinStockLevel()) {
                stockLbl.setForeground(ThemeManager.danger());
            } else {
                stockLbl.setForeground(ThemeManager.text());
            }
            
            details.add(nameLbl);
            details.add(categoryLbl);
            details.add(priceLbl);
            details.add(stockLbl);
            details.add(expiryLbl);
            
            // Edit Button (if admin)
            if (currentUser.isAdmin()) {
                JButton btnEditCard = new JButton(" Edit");
                ThemeManager.styleButton(btnEditCard, "primary");
                btnEditCard.setFont(ThemeManager.fontSmall());
                btnEditCard.addActionListener(e -> showEditProductDialogFor(p));
                JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                btnPanel.setBackground(ThemeManager.surface());
                btnPanel.add(btnEditCard);
                details.add(btnPanel);
            }
            
            card.add(details, BorderLayout.CENTER);
            grid.add(card);
        }
        
        // Fill empty slots
        int emptySlots = (rows * cols) - currentProducts.size();
        for (int i = 0; i < emptySlots; i++) {
            JPanel empty = new JPanel();
            empty.setOpaque(false);
            grid.add(empty);
        }
        
        JScrollPane scrollPane = new JScrollPane(grid);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        
        cardViewContainer.add(scrollPane, BorderLayout.CENTER);
        cardViewContainer.revalidate();
        cardViewContainer.repaint();
    }

    
    /**
     * Show selected product image
     */
    private void showSelectedProductImage() {
        int row = productTable.getSelectedRow();
        if (row >= 0 && row < currentProducts.size()) {
            Product product = currentProducts.get(row);
            String imagePath = product.getImagePath();
            
            if (imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    ImageIcon icon = new ImageIcon(imagePath);
                    Image originalImage = icon.getImage();
                    
                    // Preserve aspect ratio
                    int originalWidth = icon.getIconWidth();
                    int originalHeight = icon.getIconHeight();
                    int targetWidth = 200;
                    int targetHeight = 200;
                    
                    if (originalWidth > 0 && originalHeight > 0) {
                        double ratio = Math.min((double) targetWidth / originalWidth, (double) targetHeight / originalHeight);
                        int newWidth = (int) (originalWidth * ratio);
                        int newHeight = (int) (originalHeight * ratio);
                        
                        Image img = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                        lblImagePreview.setIcon(new ImageIcon(img));
                        lblImagePreview.setText("");
                    } else {
                        lblImagePreview.setIcon(null);
                        lblImagePreview.setText("Invalid Image");
                    }
                } else {
                    lblImagePreview.setIcon(null);
                    lblImagePreview.setText("Image not found");
                }
            } else {
                lblImagePreview.setIcon(null);
                lblImagePreview.setText("No Image");
            }
        }
    }
    
    /**
     * Show dialog to add a new product
     */
    private void showAddProductDialog() {
        JPanel panel = createProductFormPanel(null);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Product", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            if (validateForm()) {
                Product product = getProductFromForm();
                if (productDAO.add(product)) {
                    JOptionPane.showMessageDialog(this, "Product added successfully!");
                    loadAllProducts();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add product.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    /**
     * Show dialog to edit a product (from Table View)
     */
    private void showEditProductDialog() {
        int row = productTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Product product = currentProducts.get(row);
        showEditProductDialogFor(product);
    }
    
    /**
     * Show dialog to edit a specific product (Reusable for Card View)
     */
    private void showEditProductDialogFor(Product product) {
        JPanel panel = createProductFormPanel(product);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Product", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            if (validateForm()) {
                Product updatedProduct = getProductFromForm();
                updatedProduct.setProductId(product.getProductId());
                
                if (productDAO.update(updatedProduct)) {
                    JOptionPane.showMessageDialog(this, "Product updated successfully!");
                    loadAllProducts();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update product.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    /**
     * Show dialog to restock a product - shows cost preview
     */
    private void showRestockDialog() {
        int row = productTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to restock from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        showRestockDialogFor(currentProducts.get(row));
    }
    
    /**
     * Reusable restock dialog with live cost calculator
     */
    private void showRestockDialogFor(Product product) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Restock — " + product.getProductName(), true);
        dialog.setSize(420, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        panel.setBackground(ThemeManager.surface());
        
        // Header
        JLabel header = new JLabel("\uD83D\uDCE6 " + product.getProductName());
        header.setFont(ThemeManager.fontSection());
        header.setForeground(ThemeManager.primary());
        panel.add(header, BorderLayout.NORTH);
        
        // Form
        JPanel form = new JPanel(new GridLayout(4, 2, 10, 12));
        form.setBackground(ThemeManager.surface());
        
        JLabel lblCurrent = new JLabel("Current Stock:");
        lblCurrent.setFont(ThemeManager.fontBody());
        lblCurrent.setForeground(ThemeManager.text());
        JLabel valCurrent = new JLabel(String.valueOf(product.getCurrentStock()));
        valCurrent.setFont(ThemeManager.fontBold());
        valCurrent.setForeground(ThemeManager.text());
        
        JLabel lblPrice = new JLabel("Purchase Price:");
        lblPrice.setFont(ThemeManager.fontBody());
        lblPrice.setForeground(ThemeManager.text());
        JLabel valPrice = new JLabel("\u20b1" + String.format("%.2f", product.getPurchasePrice()) + " / unit");
        valPrice.setFont(ThemeManager.fontBold());
        valPrice.setForeground(ThemeManager.text());
        
        JLabel lblQty = new JLabel("Quantity to Add:*");
        lblQty.setFont(ThemeManager.fontBody());
        lblQty.setForeground(ThemeManager.text());
        JTextField txtQty = new JTextField("1");
        txtQty.setFont(ThemeManager.fontBold());
        
        JLabel lblCost = new JLabel("Total Capital Cost:");
        lblCost.setFont(ThemeManager.fontBody());
        lblCost.setForeground(ThemeManager.text());
        JLabel valCost = new JLabel("\u20b1" + String.format("%.2f", product.getPurchasePrice()));
        valCost.setFont(ThemeManager.fontBold());
        valCost.setForeground(ThemeManager.danger());
        
        // Live cost update on qty change
        txtQty.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void updateCost() {
                try {
                    int qty = Integer.parseInt(txtQty.getText().trim());
                    double cost = qty * product.getPurchasePrice();
                    valCost.setText("\u20b1" + String.format("%.2f", cost) + "  \u2192 will be deducted from profit");
                } catch (NumberFormatException ex) {
                    valCost.setText("—");
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateCost(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateCost(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateCost(); }
        });
        
        form.add(lblCurrent); form.add(valCurrent);
        form.add(lblPrice);   form.add(valPrice);
        form.add(lblQty);     form.add(txtQty);
        form.add(lblCost);    form.add(valCost);
        panel.add(form, BorderLayout.CENTER);
        
        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(ThemeManager.surface());
        JButton btnConfirm = new JButton("Confirm Restock");
        ThemeManager.styleButton(btnConfirm, "success");
        JButton btnCancel = new JButton("Cancel");
        ThemeManager.styleButton(btnCancel, "neutral");
        btnRow.add(btnCancel);
        btnRow.add(btnConfirm);
        panel.add(btnRow, BorderLayout.SOUTH);
        
        // Enter key on qty field also confirms
        txtQty.addActionListener(e -> btnConfirm.doClick());
        
        btnCancel.addActionListener(e -> dialog.dispose());
        btnConfirm.addActionListener(e -> {
            try {
                int qty = Integer.parseInt(txtQty.getText().trim());
                if (qty <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Quantity must be a positive number.", "Invalid", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                double cost = qty * product.getPurchasePrice();
                int confirm = JOptionPane.showConfirmDialog(dialog,
                    String.format("Confirm restocking %d units of '%s'?\nCapital cost: \u20b1%.2f will be logged.",
                        qty, product.getProductName(), cost),
                    "Confirm Restock", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (productDAO.restock(product.getProductId(), qty, product.getPurchasePrice(), currentUser.getUserId())) {
                        JOptionPane.showMessageDialog(dialog, "Restocked successfully!\nCapital logged: \u20b1" + String.format("%.2f", cost));
                        dialog.dispose();
                        loadAllProducts();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Failed to restock.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid number.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    /**
     * Deactivate (soft delete) a product
     */
    private void deactivateProduct() {
        int row = productTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to deactivate.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Product product = currentProducts.get(row);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to deactivate '\"" + product.getProductName() + "\"?\n\n" +
            "This will hide the product from the list but preserve sales history.", 
            "Confirm Deactivation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (productDAO.deactivate(product.getProductId())) {
                JOptionPane.showMessageDialog(this, "Product deactivated successfully!");
                loadAllProducts();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to deactivate product.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Create the product form panel for add/edit dialogs
     */
    private JPanel createProductFormPanel(Product product) {
        JPanel panel = new JPanel(new GridLayout(10, 2, 10, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Initialize fields
        txtProductCode = new JTextField(20);
        txtProductName = new JTextField(20);
        txtCategory = new JTextField(20);
        txtUnit = new JTextField(20);
        txtPurchasePrice = new JTextField(20);
        txtSRP = new JTextField(20);
        txtStock = new JTextField(20);
        txtMinStock = new JTextField(20);
        txtExpiryDate = new JTextField(20);
        txtImagePath = new JTextField(20);
        
        // Populate if editing
        if (product != null) {
            txtProductCode.setText(product.getProductCode());
            txtProductName.setText(product.getProductName());
            txtCategory.setText(product.getCategory());
            txtUnit.setText(product.getUnit());
            txtPurchasePrice.setText(String.valueOf(product.getPurchasePrice()));
            txtSRP.setText(String.valueOf(product.getSrp()));
            txtStock.setText(String.valueOf(product.getCurrentStock()));
            txtMinStock.setText(String.valueOf(product.getMinStockLevel()));
            if (product.getExpiryDate() != null) {
                txtExpiryDate.setText(product.getExpiryDate().toString());
            }
            txtImagePath.setText(product.getImagePath());
            txtStock.setEditable(false);  // Stock managed separately via restock
        } else {
            txtStock.setText("0");
            txtMinStock.setText("5");
            txtUnit.setText("piece");
        }
        
        // Add fields to panel
        panel.add(new JLabel("Product Code:*"));
        panel.add(txtProductCode);
        
        panel.add(new JLabel("Product Name:*"));
        panel.add(txtProductName);
        
        panel.add(new JLabel("Category:"));
        panel.add(txtCategory);
        
        panel.add(new JLabel("Unit:"));
        panel.add(txtUnit);
        
        panel.add(new JLabel("Purchase Price:*"));
        panel.add(txtPurchasePrice);
        
        panel.add(new JLabel("SRP (Selling Price):*"));
        panel.add(txtSRP);
        
        panel.add(new JLabel("Initial Stock:"));
        panel.add(txtStock);
        
        panel.add(new JLabel("Min Stock Level:*"));
        panel.add(txtMinStock);
        
        panel.add(new JLabel("Expiry Date (YYYY-MM-DD):"));
        panel.add(txtExpiryDate);
        
        panel.add(new JLabel("Image Path:"));
        
        // Image path with browse button
        JPanel imagePanel = new JPanel(new BorderLayout(5, 0));
        imagePanel.add(txtImagePath, BorderLayout.CENTER);
        JButton btnBrowse = new JButton("Browse");
        btnBrowse.addActionListener(e -> browseImage());
        imagePanel.add(btnBrowse, BorderLayout.EAST);
        panel.add(imagePanel);
        
        return panel;
    }
    
    /**
     * Browse for image file
     */
    private void browseImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Copy to images folder
            try {
                File destDir = new File("images/products");
                if (!destDir.exists()) {
                    destDir.mkdirs();
                }
                
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                File destFile = new File(destDir, fileName);
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                txtImagePath.setText(destFile.getPath());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error copying image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Validate form input
     */
    private boolean validateForm() {
        if (txtProductCode.getText().trim().isEmpty() ||
            txtProductName.getText().trim().isEmpty() ||
            txtPurchasePrice.getText().trim().isEmpty() ||
            txtSRP.getText().trim().isEmpty() ||
            txtMinStock.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields (*).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        try {
            double purchasePrice = Double.parseDouble(txtPurchasePrice.getText().trim());
            double srp = Double.parseDouble(txtSRP.getText().trim());
            
            if (srp < purchasePrice) {
                JOptionPane.showMessageDialog(this, "SRP must be greater than or equal to Purchase Price.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for prices.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    /**
     * Get Product object from form fields
     */
    private Product getProductFromForm() {
        Product product = new Product();
        product.setProductCode(txtProductCode.getText().trim());
        product.setProductName(txtProductName.getText().trim());
        product.setCategory(txtCategory.getText().trim());
        product.setUnit(txtUnit.getText().trim());
        product.setPurchasePrice(Double.parseDouble(txtPurchasePrice.getText().trim()));
        product.setSrp(Double.parseDouble(txtSRP.getText().trim()));
        product.setCurrentStock(Integer.parseInt(txtStock.getText().trim()));
        product.setMinStockLevel(Integer.parseInt(txtMinStock.getText().trim()));
        
        String expiryStr = txtExpiryDate.getText().trim();
        if (!expiryStr.isEmpty()) {
            try {
                product.setExpiryDate(Date.valueOf(expiryStr));
            } catch (IllegalArgumentException e) {
                // Invalid date format, ignore
            }
        }
        
        product.setImagePath(txtImagePath.getText().trim());
        
        return product;
    }
    
    /**
     * Refresh data from database
     */
    public void refreshData() {
        loadAllProducts();
        lblImagePreview.setIcon(null);
        lblImagePreview.setText("No Image");
    }
}
