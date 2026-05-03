package gui;

import dao.ProductDAO;
import dao.TransactionDAO;
import model.Product;
import model.Transaction;
import model.TransactionItem;

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
 */
public class SalesPanel extends JPanel {

    private final boolean isOwner;
    private ProductDAO productDAO;
    private TransactionDAO transactionDAO;

    // Cart data
    private List<TransactionItem> cartItems;

    // UI Components
    private JTextField    txtSearch;
    private JList<Product> searchResultsList;
    private DefaultListModel<Product> searchResultsModel;
    private JTable        cartTable;
    private DefaultTableModel cartTableModel;
    private JLabel        lblSubtotal;
    private JLabel        lblDiscount;
    private JLabel        lblTotal;
    private JTextField    txtDiscount;

    public SalesPanel(boolean isOwner) {
        this.isOwner        = isOwner;
        this.productDAO     = new ProductDAO();
        this.transactionDAO = new TransactionDAO();
        this.cartItems      = new ArrayList<>();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(ThemeManager.bg());

        // Title
        JLabel lblTitle = new JLabel(" Point of Sale (POS)");
        lblTitle.setFont(ThemeManager.fontSection());
        lblTitle.setForeground(ThemeManager.primary());
        add(lblTitle, BorderLayout.NORTH);

        // Center: product search + cart
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        centerPanel.setOpaque(false);

        JTabbedPane leftPanel = new JTabbedPane();
        leftPanel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ThemeManager.applyTabbedPaneTheme(leftPanel);

        leftPanel.addTab(" All Products", createQuickAddPanel());
        leftPanel.addTab(" Global Search",    createSearchPanel());
        centerPanel.add(leftPanel);
        centerPanel.add(createCartPanel());

        add(centerPanel, BorderLayout.CENTER);

        // Bottom buttons
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

        searchProducts();
    }

    // ── Quick-add panel (category tabs) ─────────────────────────────────────

    private JComponent createQuickAddPanel() {
        List<Product> allProducts = productDAO.getAllActive();
        
        if (allProducts.isEmpty()) {
            JPanel empty = new JPanel();
            empty.setBackground(ThemeManager.bg());
            empty.add(new JLabel("No products available"));
            return empty;
        }

        int rows = Math.max(4, (int) Math.ceil(allProducts.size() / 3.0));
        JPanel gridPanel = new JPanel(new GridLayout(rows, 3, 10, 10));
        gridPanel.setBackground(ThemeManager.bg());
        gridPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        for (Product p : allProducts) {
            JButton btn = new JButton("<html><center><b>" + p.getProductName() + "</b><br>₱" +
                    String.format("%.2f", p.getSellPrice()) + "</center></html>");
            btn.setFont(ThemeManager.fontBody());
            ThemeManager.styleButton(btn, "success");
            btn.addActionListener(e -> quickAddToCart(p));
            gridPanel.add(btn);
        }

        // Fill empty slots
        int emptySlots = (rows * 3) - allProducts.size();
        for (int i = 0; i < emptySlots; i++) {
            JButton empty = new JButton("-");
            empty.setEnabled(false);
            gridPanel.add(empty);
        }

        JScrollPane sp = new JScrollPane(gridPanel);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.setBorder(null);

        return sp;
    }

    private void quickAddToCart(Product p) {
        if (p.getCurrentStock() <= 0) {
            JOptionPane.showMessageDialog(this, "This product is out of stock!", "Out of Stock", JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (TransactionItem item : cartItems) {
            if (item.getProductId() == p.getProductId()) {
                int newQty = item.getQuantity() + 1;
                if (newQty > p.getCurrentStock()) {
                    JOptionPane.showMessageDialog(this, "Cannot add more — total would exceed available stock!", "Stock Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                item.setQuantity(newQty);
                refreshCartTable();
                return;
            }
        }
        cartItems.add(new TransactionItem(p.getProductId(), p.getProductName(), 1, p.getSellPrice(), p.getCostPerUnit()));
        refreshCartTable();
    }

    // ── Search panel ─────────────────────────────────────────────────────────

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(ThemeManager.surface());
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ThemeManager.border()),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblTitle = new JLabel(" Search Products");
        lblTitle.setFont(ThemeManager.fontSection());
        lblTitle.setForeground(ThemeManager.text());
        panel.add(lblTitle, BorderLayout.NORTH);

        JPanel searchFieldPanel = new JPanel(new BorderLayout(5, 0));
        searchFieldPanel.setOpaque(false);
        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 14));
        txtSearch.addActionListener(e -> searchProducts());
        JButton btnSearch = new JButton("Search");
        btnSearch.setFont(new Font("Arial", Font.BOLD, 12));
        btnSearch.addActionListener(e -> searchProducts());
        searchFieldPanel.add(txtSearch, BorderLayout.CENTER);
        searchFieldPanel.add(btnSearch, BorderLayout.EAST);
        panel.add(searchFieldPanel, BorderLayout.NORTH);

        searchResultsModel = new DefaultListModel<>();
        searchResultsList  = new JList<>(searchResultsModel);
        searchResultsList.setFont(ThemeManager.fontBody());
        searchResultsList.setBackground(ThemeManager.surface());
        searchResultsList.setForeground(ThemeManager.text());
        searchResultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchResultsList.setCellRenderer(new ProductListRenderer());

        JScrollPane sp = new JScrollPane(searchResultsList);
        sp.setBorder(BorderFactory.createTitledBorder("Search Results (Click to add to cart)"));
        panel.add(sp, BorderLayout.CENTER);

        JButton btnAdd = new JButton("Add to Cart");
        ThemeManager.styleButton(btnAdd, "primary");
        btnAdd.addActionListener(e -> addSelectedToCart());
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addPanel.setOpaque(false);
        addPanel.add(btnAdd);
        panel.add(addPanel, BorderLayout.SOUTH);

        searchResultsList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) addSelectedToCart();
            }
        });

        return panel;
    }

    // ── Cart panel ───────────────────────────────────────────────────────────

    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(ThemeManager.surface());
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ThemeManager.border()),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblTitle = new JLabel(" Shopping Cart");
        lblTitle.setFont(ThemeManager.fontSection());
        lblTitle.setForeground(ThemeManager.text());
        panel.add(lblTitle, BorderLayout.NORTH);

        String[] columns = {"Product", "Qty", "Price", "Total"};
        cartTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        cartTable = new JTable(cartTableModel);
        JScrollPane sp = new JScrollPane(cartTable);
        ThemeManager.applyTableTheme(cartTable, sp);
        panel.add(sp, BorderLayout.CENTER);

        // Totals + remove button (south)
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);

        JPanel removePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        removePanel.setOpaque(false);
        JButton btnRemove = new JButton("Remove Selected");
        ThemeManager.styleButton(btnRemove, "warning");
        btnRemove.addActionListener(e -> removeSelectedItem());
        removePanel.add(btnRemove);

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

        JPanel discountPanel = new JPanel(new BorderLayout(5, 0));
        discountPanel.setOpaque(false);
        txtDiscount = new JTextField("0");
        txtDiscount.setHorizontalAlignment(JTextField.RIGHT);
        txtDiscount.setFont(ThemeManager.fontBody());
        txtDiscount.addActionListener(e -> applyDiscount());
        JButton btnApply = new JButton("Apply");
        ThemeManager.styleButton(btnApply, "primary");
        btnApply.addActionListener(e -> applyDiscount());
        discountPanel.add(txtDiscount, BorderLayout.CENTER);
        discountPanel.add(btnApply, BorderLayout.EAST);
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

        southPanel.add(removePanel,  BorderLayout.NORTH);
        southPanel.add(totalsPanel,  BorderLayout.SOUTH);
        panel.add(southPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    private void searchProducts() {
        String term = txtSearch.getText().trim();
        List<Product> results = term.isEmpty() ? productDAO.getAllActive() : productDAO.search(term);
        searchResultsModel.clear();
        for (Product p : results) searchResultsModel.addElement(p);
        if (results.isEmpty() && !term.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No products found for: " + term, "Search", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void addSelectedToCart() {
        Product selected = searchResultsList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a product from the list.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selected.getCurrentStock() <= 0) {
            JOptionPane.showMessageDialog(this, "This product is out of stock!", "Out of Stock", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String input = JOptionPane.showInputDialog(this,
            "Product: " + selected.getProductName() + "\n" +
            "Available Stock: " + selected.getCurrentStock() + "\n" +
            "Price: ₱" + String.format("%.2f", selected.getSellPrice()) + "\n\nEnter quantity:", "1");

        if (input == null || input.isEmpty()) return;

        try {
            int qty = Integer.parseInt(input);
            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be positive.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (qty > selected.getCurrentStock()) {
                JOptionPane.showMessageDialog(this, "Insufficient stock!\nAvailable: " + selected.getCurrentStock(), "Stock Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Check if already in cart
            for (TransactionItem item : cartItems) {
                if (item.getProductId() == selected.getProductId()) {
                    int newQty = item.getQuantity() + qty;
                    if (newQty > selected.getCurrentStock()) {
                        JOptionPane.showMessageDialog(this, "Cannot add more — total would exceed available stock!\nAvailable: " + selected.getCurrentStock(), "Stock Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    item.setQuantity(newQty);
                    refreshCartTable();
                    return;
                }
            }
            cartItems.add(new TransactionItem(selected.getProductId(), selected.getProductName(), qty, selected.getSellPrice(), selected.getCostPerUnit()));
            refreshCartTable();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void removeSelectedItem() {
        int row = cartTable.getSelectedRow();
        if (row < 0 || row >= cartItems.size()) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        cartItems.remove(row);
        refreshCartTable();
    }

    private void applyDiscount() {
        try {
            double discount = Double.parseDouble(txtDiscount.getText().trim());
            if (discount < 0) discount = 0;
            double subtotal = calculateSubtotal();
            if (discount > subtotal) {
                JOptionPane.showMessageDialog(this, "Discount cannot exceed subtotal.", "Invalid Discount", JOptionPane.WARNING_MESSAGE);
                return;
            }
            lblDiscount.setText("₱" + String.format("%.2f", discount));
            updateTotals();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid discount amount.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }

    private double calculateSubtotal() {
        double total = 0;
        for (TransactionItem item : cartItems) total += item.getItemTotal();
        return total;
    }

    private void refreshCartTable() {
        cartTableModel.setRowCount(0);
        for (TransactionItem item : cartItems) {
            cartTableModel.addRow(new Object[]{
                item.getProductName(),
                item.getQuantity(),
                String.format("%.2f", item.getSoldPrice()),
                String.format("%.2f", item.getItemTotal())
            });
        }
        updateTotals();
    }

    private void updateTotals() {
        double subtotal = calculateSubtotal();
        double discount = 0;
        try {
            discount = Double.parseDouble(txtDiscount.getText().trim());
            if (discount < 0) discount = 0;
            if (discount > subtotal) discount = subtotal;
        } catch (NumberFormatException ignored) {}
        double total = subtotal - discount;
        lblSubtotal.setText("₱" + String.format("%.2f", subtotal));
        lblDiscount.setText("₱" + String.format("%.2f", discount));
        lblTotal.setText("₱"    + String.format("%.2f", total));
    }

    private void completeSale() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty! Add products before completing sale.", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double subtotal = calculateSubtotal();
        double discount = 0;
        try { discount = Double.parseDouble(txtDiscount.getText().trim()); } catch (NumberFormatException ignored) {}
        double total = subtotal - discount;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Confirm Sale:\n\nItems: " + cartItems.size() + "\n" +
            "Subtotal: ₱" + String.format("%.2f", subtotal) + "\n" +
            "Discount: ₱" + String.format("%.2f", discount) + "\n" +
            "TOTAL: ₱"    + String.format("%.2f", total)    + "\n\nProceed with sale?",
            "Confirm Sale", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        Transaction transaction = new Transaction(discount);
        transaction.setItems(new ArrayList<>(cartItems));
        transaction.recalculateTotals();

        if (transactionDAO.createTransaction(transaction)) {
            JOptionPane.showMessageDialog(this,
                "Sale completed successfully!\n\n" +
                "Transaction ID: " + transaction.getTransactionId() + "\n" +
                "Total: ₱" + String.format("%.2f", total) + "\n" +
                "Thank you for your purchase!",
                "Sale Complete", JOptionPane.INFORMATION_MESSAGE);
            clearCart();
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to complete sale.\nPlease check stock availability and try again.",
                "Sale Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearCart() {
        cartItems.clear();
        txtDiscount.setText("0");
        refreshCartTable();
        searchResultsModel.clear();
        if (txtSearch != null) txtSearch.setText("");
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
            setText(p.getProductName() +
                    "  (₱" + String.format("%.2f", p.getSellPrice()) + ")  [Stock: " + p.getCurrentStock() + "]");
        }
        if (!isSelected) {
            setBackground(gui.ThemeManager.surface());
            setForeground(gui.ThemeManager.text());
        }
        return this;
    }
}
