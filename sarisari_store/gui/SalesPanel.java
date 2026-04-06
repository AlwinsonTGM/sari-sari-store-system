package gui;

import dao.ProductDAO;
import dao.SaleDAO;
import model.Product;
import model.Sale;
import model.SaleItem;
import model.User;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SalesPanel - Point of Sale (POS) Screen
 * 
 * Features:
 * - Search and add products to cart
 * - Display cart with quantities and totals
 * - Apply discount
 * - Complete sale (deducts stock automatically)
 * - Clear cart
 * 
 * Stock is automatically deducted when sale is completed.
 * If stock is insufficient, sale is prevented.
 */
public class SalesPanel extends JPanel {
    
    private User currentUser;
    private ProductDAO productDAO;
    private SaleDAO saleDAO;
    
    // Cart data
    private List<SaleItem> cartItems;
    private Sale currentSale;
    
    // UI Components
    private JTextField txtSearch;
    private JList<Product> searchResultsList;
    private DefaultListModel<Product> searchResultsModel;
    private JTable cartTable;
    private DefaultTableModel cartTableModel;
    private JLabel lblSubtotal;
    private JLabel lblDiscount;
    private JLabel lblTotal;
    private JTextField txtDiscount;
    
    public SalesPanel(User user) {
        this.currentUser = user;
        this.productDAO = new ProductDAO();
        this.saleDAO = new SaleDAO();
        this.cartItems = new ArrayList<>();
        this.currentSale = new Sale();
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(ThemeManager.bg());
        
        // Title
        JLabel lblTitle = new JLabel(" Point of Sale (POS)");
        lblTitle.setFont(ThemeManager.fontSection());
        lblTitle.setForeground(ThemeManager.primary());
        add(lblTitle, BorderLayout.NORTH);
        
        // Center panel: Product search and cart
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        centerPanel.setOpaque(false);
        
        // Left: Product left panel (Tabs for Quick Add and Search)
        JTabbedPane leftPanel = new JTabbedPane();
        leftPanel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ThemeManager.applyTabbedPaneTheme(leftPanel);
        
        JComponent quickAddPanel = createQuickAddPanel();
        JPanel searchPanel = createSearchPanel();
        
        leftPanel.addTab(" Quick Categories", quickAddPanel);
        leftPanel.addTab(" Global Search", searchPanel);
        
        centerPanel.add(leftPanel);
        
        // Right: Cart panel
        JPanel cartPanel = createCartPanel();
        centerPanel.add(cartPanel);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom: Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        
        JButton btnClear = new JButton("Clear Cart");
        ThemeManager.styleButton(btnClear, "danger");
        btnClear.setPreferredSize(new Dimension(150, 40));
        btnClear.addActionListener(e -> clearCart());
        
        JButton btnComplete = new JButton(" Complete Sale");
        ThemeManager.styleButton(btnComplete, "success");
        btnComplete.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnComplete.setPreferredSize(new Dimension(200, 50));
        btnComplete.addActionListener(e -> completeSale());
        
        buttonPanel.add(btnClear);
        buttonPanel.add(btnComplete);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Initial load of all products
        searchProducts();
    }
    
    /**
     * Create the quick add panel grouped by categories
     */
    private JComponent createQuickAddPanel() {
        JTabbedPane categoryTabs = new JTabbedPane();
        categoryTabs.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ThemeManager.applyTabbedPaneTheme(categoryTabs);
        
        List<Product> allProducts = productDAO.getAllActive();
        
        // Group products by category
        java.util.Map<String, List<Product>> categoryMap = new java.util.HashMap<>();
        for (Product p : allProducts) {
            String cat = p.getCategory();
            if (cat == null || cat.trim().isEmpty()) {
                cat = "Uncategorized";
            }
            categoryMap.computeIfAbsent(cat, k -> new ArrayList<>()).add(p);
        }
        
        // Create a tab for each category
        for (String category : categoryMap.keySet()) {
            List<Product> catProducts = categoryMap.get(category);
            
            // Calculate grid rows based on items (min 4 rows, 3 cols)
            int rows = Math.max(4, (int) Math.ceil(catProducts.size() / 3.0));
            JPanel gridPanel = new JPanel(new GridLayout(rows, 3, 10, 10));
            gridPanel.setBackground(ThemeManager.bg());
            gridPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            
            for (Product p : catProducts) {
                JButton btn = new JButton("<html><center><b>" + p.getProductName() + "</b><br>₱" + String.format("%.2f", p.getSrp()) + "</center></html>");
                btn.setFont(ThemeManager.fontBody());
                ThemeManager.styleButton(btn, "success");
                
                btn.addActionListener(e -> quickAddToCart(p));
                gridPanel.add(btn);
            }
            
            // Fill empty slots
            int emptySlots = (rows * 3) - catProducts.size();
            for (int i = 0; i < emptySlots; i++) {
                JButton emptyBtn = new JButton("-");
                emptyBtn.setEnabled(false);
                gridPanel.add(emptyBtn);
            }
            
            JScrollPane scrollPane = new JScrollPane(gridPanel);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            scrollPane.setBorder(null);
            
            categoryTabs.addTab(category, scrollPane);
        }
        
        // If no products, just add an empty tab
        if (categoryMap.isEmpty()) {
            JPanel empty = new JPanel();
            empty.setBackground(ThemeManager.bg());
            empty.add(new JLabel("No products available"));
            categoryTabs.addTab("Empty", empty);
        }
        
        return categoryTabs;
    }
    
    /**
     * 1-Click add to cart (defaults to qty 1, NO popups)
     */
    private void quickAddToCart(Product selected) {
        if (selected.getCurrentStock() <= 0) {
            JOptionPane.showMessageDialog(this, "This product is out of stock!", "Out of Stock", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Check if already in cart
        for (SaleItem item : cartItems) {
            if (item.getProductId() == selected.getProductId()) {
                int newQty = item.getQuantity() + 1;
                if (newQty > selected.getCurrentStock()) {
                    JOptionPane.showMessageDialog(this, "Cannot add more. Total would exceed available stock!", "Stock Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                item.setQuantity(newQty);
                refreshCartTable();
                return;
            }
        }
        
        // Add new item to cart (Qty = 1)
        SaleItem newItem = new SaleItem(
            selected.getProductId(),
            selected.getProductName(),
            selected.getProductCode(),
            1,
            selected.getSrp(),
            selected.getPurchasePrice()
        );
        cartItems.add(newItem);
        refreshCartTable();
    }
    
    /**
     * Create the product search panel
     */
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(ThemeManager.surface());
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ThemeManager.border()),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel lblSearchTitle = new JLabel(" Search Products");
        lblSearchTitle.setFont(ThemeManager.fontSection());
        lblSearchTitle.setForeground(ThemeManager.text());
        panel.add(lblSearchTitle, BorderLayout.NORTH);
        
        // Search field
        JPanel searchFieldPanel = new JPanel(new BorderLayout(5, 0));
        searchFieldPanel.setOpaque(false);
        
        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 14));
        txtSearch.addActionListener(e -> searchProducts()); // Enter key support
        
        JButton btnSearch = new JButton("Search");
        btnSearch.setFont(new Font("Arial", Font.BOLD, 12));
        btnSearch.addActionListener(e -> searchProducts());
        
        searchFieldPanel.add(txtSearch, BorderLayout.CENTER);
        searchFieldPanel.add(btnSearch, BorderLayout.EAST);
        
        panel.add(searchFieldPanel, BorderLayout.NORTH);
        
        // Search results list
        searchResultsModel = new DefaultListModel<>();
        searchResultsList = new JList<>(searchResultsModel);
        searchResultsList.setFont(ThemeManager.fontBody());
        searchResultsList.setBackground(ThemeManager.surface());
        searchResultsList.setForeground(ThemeManager.text());
        searchResultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchResultsList.setCellRenderer(new ProductListRenderer());
        
        JScrollPane scrollPane = new JScrollPane(searchResultsList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Search Results (Click to add to cart)"));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add to cart button
        JButton btnAddToCart = new JButton("Add to Cart");
        ThemeManager.styleButton(btnAddToCart, "primary");
        btnAddToCart.addActionListener(e -> addSelectedToCart());
        
        JPanel addButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addButtonPanel.setOpaque(false);
        addButtonPanel.add(btnAddToCart);
        panel.add(addButtonPanel, BorderLayout.SOUTH);
        
        // Double-click to add
        searchResultsList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    addSelectedToCart();
                }
            }
        });
        
        return panel;
    }
    
    /**
     * Create the cart panel
     */
    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(ThemeManager.surface());
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ThemeManager.border()),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel lblCartTitle = new JLabel(" Shopping Cart");
        lblCartTitle.setFont(ThemeManager.fontSection());
        lblCartTitle.setForeground(ThemeManager.text());
        panel.add(lblCartTitle, BorderLayout.NORTH);
        
        // Cart table
        String[] columns = {"Product", "Qty", "Price", "Total"};
        cartTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        cartTable = new JTable(cartTableModel);
        
        JScrollPane scrollPane = new JScrollPane(cartTable);
        ThemeManager.applyTableTheme(cartTable, scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Totals panel
        JPanel totalsPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        totalsPanel.setOpaque(false);
        totalsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JLabel lblSubtotalKey = new JLabel("Subtotal:", JLabel.LEFT);
        lblSubtotalKey.setForeground(ThemeManager.text());
        lblSubtotal = new JLabel("₱0.00", JLabel.RIGHT);
        lblSubtotal.setFont(ThemeManager.fontBold());
        lblSubtotal.setForeground(ThemeManager.text());
        totalsPanel.add(lblSubtotalKey);
        totalsPanel.add(lblSubtotal);
        
        // Discount input
        JPanel discountPanel = new JPanel(new BorderLayout(5, 0));
        discountPanel.setOpaque(false);
        txtDiscount = new JTextField("0");
        txtDiscount.setHorizontalAlignment(JTextField.RIGHT);
        txtDiscount.setFont(ThemeManager.fontBody());
        txtDiscount.addActionListener(e -> applyDiscount()); // Enter key support
        JButton btnApplyDiscount = new JButton("Apply");
        ThemeManager.styleButton(btnApplyDiscount, "primary");
        btnApplyDiscount.addActionListener(e -> applyDiscount());
        discountPanel.add(txtDiscount, BorderLayout.CENTER);
        discountPanel.add(btnApplyDiscount, BorderLayout.EAST);
        
        JLabel lblDiscountInputKey = new JLabel("Discount (₱):", JLabel.LEFT);
        lblDiscountInputKey.setForeground(ThemeManager.text());
        totalsPanel.add(lblDiscountInputKey);
        totalsPanel.add(discountPanel);
        
        lblDiscount = new JLabel("₱0.00", JLabel.RIGHT);
        lblDiscount.setFont(ThemeManager.fontBig());
        lblDiscount.setForeground(ThemeManager.danger());
        JLabel lblDiscountKey = new JLabel("Discount Applied:", JLabel.LEFT);
        lblDiscountKey.setForeground(ThemeManager.text());
        totalsPanel.add(lblDiscountKey);
        totalsPanel.add(lblDiscount);
        
        lblTotal = new JLabel("₱0.00", JLabel.RIGHT);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTotal.setForeground(ThemeManager.success());
        JLabel lblTotalKey = new JLabel("TOTAL:", JLabel.LEFT);
        lblTotalKey.setFont(ThemeManager.fontBold());
        lblTotalKey.setForeground(ThemeManager.text());
        totalsPanel.add(lblTotalKey);
        totalsPanel.add(lblTotal);
        
        panel.add(totalsPanel, BorderLayout.SOUTH);
        
        // Remove item button
        JButton btnRemove = new JButton("Remove Selected");
        ThemeManager.styleButton(btnRemove, "warning");
        btnRemove.addActionListener(e -> removeSelectedItem());
        
        JPanel removePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        removePanel.setOpaque(false);
        removePanel.add(btnRemove);
        panel.add(removePanel, BorderLayout.SOUTH);
        
        // Re-add totals panel properly
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.add(removePanel, BorderLayout.NORTH);
        southPanel.add(totalsPanel, BorderLayout.SOUTH);
        panel.add(southPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Search products
     */
    private void searchProducts() {
        String searchTerm = txtSearch.getText().trim();
        List<Product> results;
        
        if (searchTerm.isEmpty()) {
            // Load all products if search is empty
            results = productDAO.getAllActive();
        } else {
            results = productDAO.search(searchTerm);
        }
        
        searchResultsModel.clear();
        
        for (Product p : results) {
            searchResultsModel.addElement(p);
        }
        
        if (results.isEmpty() && !searchTerm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No products found for: " + searchTerm, "Search", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Add selected product to cart
     */
    private void addSelectedToCart() {
        Product selected = searchResultsList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a product from the list.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Check stock
        if (selected.getCurrentStock() <= 0) {
            JOptionPane.showMessageDialog(this, "This product is out of stock!", "Out of Stock", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Ask for quantity
        String input = JOptionPane.showInputDialog(this, 
            "Product: " + selected.getProductName() + "\n" +
            "Available Stock: " + selected.getCurrentStock() + "\n" +
            "Price: P" + String.format("%.2f", selected.getSrp()) + "\n\n" +
            "Enter quantity:", "1");
        
        if (input == null || input.isEmpty()) {
            return;
        }
        
        try {
            int quantity = Integer.parseInt(input);
            
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be positive.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (quantity > selected.getCurrentStock()) {
                JOptionPane.showMessageDialog(this, 
                    "Insufficient stock!\nAvailable: " + selected.getCurrentStock(), 
                    "Stock Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if already in cart
            for (SaleItem item : cartItems) {
                if (item.getProductId() == selected.getProductId()) {
                    int newQty = item.getQuantity() + quantity;
                    if (newQty > selected.getCurrentStock()) {
                        JOptionPane.showMessageDialog(this, 
                            "Cannot add more. Total would exceed available stock!\nAvailable: " + selected.getCurrentStock(), 
                            "Stock Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    item.setQuantity(newQty);
                    refreshCartTable();
                    return;
                }
            }
            
            // Add new item to cart
            SaleItem newItem = new SaleItem(
                selected.getProductId(),
                selected.getProductName(),
                selected.getProductCode(),
                quantity,
                selected.getSrp(),
                selected.getPurchasePrice()
            );
            cartItems.add(newItem);
            refreshCartTable();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Remove selected item from cart
     */
    private void removeSelectedItem() {
        int row = cartTable.getSelectedRow();
        if (row < 0 || row >= cartItems.size()) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        cartItems.remove(row);
        refreshCartTable();
    }
    
    /**
     * Apply discount
     */
    private void applyDiscount() {
        try {
            double discount = Double.parseDouble(txtDiscount.getText().trim());
            if (discount < 0) {
                discount = 0;
            }
            
            double subtotal = calculateSubtotal();
            if (discount > subtotal) {
                JOptionPane.showMessageDialog(this, "Discount cannot exceed subtotal.", "Invalid Discount", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            lblDiscount.setText("P" + String.format("%.2f", discount));
            updateTotals();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid discount amount.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Calculate subtotal from cart items
     */
    private double calculateSubtotal() {
        double subtotal = 0;
        for (SaleItem item : cartItems) {
            subtotal += item.getLineTotal();
        }
        return subtotal;
    }
    
    /**
     * Refresh the cart table
     */
    private void refreshCartTable() {
        cartTableModel.setRowCount(0);
        
        for (SaleItem item : cartItems) {
            cartTableModel.addRow(new Object[]{
                item.getProductName(),
                item.getQuantity(),
                String.format("%.2f", item.getUnitPrice()),
                String.format("%.2f", item.getLineTotal())
            });
        }
        
        updateTotals();
    }
    
    /**
     * Update total labels
     */
    private void updateTotals() {
        double subtotal = calculateSubtotal();
        double discount = 0;
        
        try {
            discount = Double.parseDouble(txtDiscount.getText().trim());
            if (discount < 0) discount = 0;
            if (discount > subtotal) discount = subtotal;
        } catch (NumberFormatException e) {
            discount = 0;
        }
        
        double total = subtotal - discount;
        
        lblSubtotal.setText("P" + String.format("%.2f", subtotal));
        lblDiscount.setText("P" + String.format("%.2f", discount));
        lblTotal.setText("P" + String.format("%.2f", total));
    }
    
    /**
     * Complete the sale
     */
    private void completeSale() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty! Add products before completing sale.", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        double subtotal = calculateSubtotal();
        double discount = 0;
        try {
            discount = Double.parseDouble(txtDiscount.getText().trim());
        } catch (NumberFormatException e) {
            discount = 0;
        }
        double total = subtotal - discount;
        
        // Confirm sale
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Confirm Sale:\n\n" +
            "Items: " + cartItems.size() + "\n" +
            "Subtotal: P" + String.format("%.2f", subtotal) + "\n" +
            "Discount: P" + String.format("%.2f", discount) + "\n" +
            "TOTAL: P" + String.format("%.2f", total) + "\n\n" +
            "Proceed with sale?",
            "Confirm Sale", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Create sale object
        Sale sale = new Sale();
        sale.setUserId(currentUser.getUserId());
        sale.setTotalAmount(subtotal);
        sale.setDiscountAmount(discount);
        sale.setFinalAmount(total);
        sale.setItems(new ArrayList<>(cartItems));
        
        // Save sale to database (transaction)
        if (saleDAO.createSale(sale)) {
            JOptionPane.showMessageDialog(this, 
                "Sale completed successfully!\n\n" +
                "Sale ID: " + sale.getSaleId() + "\n" +
                "Total: P" + String.format("%.2f", total) + "\n" +
                "Thank you for your purchase!",
                "Sale Complete", JOptionPane.INFORMATION_MESSAGE);
            
            // Clear cart
            clearCart();
            
        } else {
            JOptionPane.showMessageDialog(this, 
                "Failed to complete sale.\nPlease check stock availability and try again.", 
                "Sale Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Clear the cart
     */
    private void clearCart() {
        cartItems.clear();
        txtDiscount.setText("0");
        refreshCartTable();
        searchResultsModel.clear();
        txtSearch.setText("");
    }
}

/**
 * Custom renderer for product list — theme-aware
 */
class ProductListRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        if (value instanceof Product) {
            Product p = (Product) value;
            setText(p.getProductCode() + " — " + p.getProductName() +
                    "  (₱" + String.format("%.2f", p.getSrp()) + ")  [Stock: " + p.getCurrentStock() + "]");
        }
        
        if (!isSelected) {
            setBackground(gui.ThemeManager.surface());
            setForeground(gui.ThemeManager.text());
        }
        
        return this;
    }
}
