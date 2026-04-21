# Sari-Sari Store - Entity Relationship Documentation

## Simplified Database Schema (No User Table)

This document describes the entity relationships in the simplified Sari-Sari Store Inventory and Sales Management System.

---

## 📊 Entity Relationship Diagram

```
┌─────────────────┐
│    products     │
│─────────────────│
│ PK product_id   │◄──────┐
│    product_name │       │
│    category     │       │
│    unit         │       │
│    cost_per_unit│       │
│    sell_price   │       │
│    current_stock│       │
│    min_stock_level      │
│    ...          │       │
└────────┬────────┘       │
         │                │
         │ 1:N            │ 1:N
         ▼                │
┌─────────────────┐       │
│ transaction_items│      │
│─────────────────│       │
│ PK transaction_item_id  │
│ FK transaction_id │     │
│ FK product_id    │──────┘
│    quantity      │
│    sold_price    │
│    cost_at_sale  │
│    item_total    │
└────────┬─────────┘
         │ N:1
         ▼
┌─────────────────┐
│  transactions   │
│─────────────────│
│ PK transaction_id       │
│    transaction_datetime │
│    total_amount         │
│    discount_amount      │
│    final_amount         │
│    notes                │
└─────────────────┘

┌─────────────────┐
│   restock_log   │
│─────────────────│
│ PK restock_id   │
│ FK product_id   │◄────── products
│    quantity_added       │
│    cost_per_unit        │
│    total_cost           │
│    restock_date         │
│    notes                │
└─────────────────┘
```

---

## 📋 Table Descriptions

### 1. **products** 
Stores all product information including inventory levels and pricing.

| Column | Type | Description |
|--------|------|-------------|
| `product_id` | INT (PK) | Auto-increment unique identifier |
| `product_name` | VARCHAR(150) | Name of the product |
| `category` | VARCHAR(50) | Product category (e.g., Beverages, Snacks) - **embedded field, not separate entity** |
| `unit` | VARCHAR(20) | Unit of measurement (piece, pack, bottle) |
| `cost_per_unit` | DECIMAL(10,2) | **How much you pay to buy 1 item from supplier** |
| `sell_price` | DECIMAL(10,2) | **How much you charge customers for 1 item** |
| `current_stock` | INT | Current quantity in inventory |
| `min_stock_level` | INT | Minimum stock before low-stock alert |
| `expiry_date` | DATE | Product expiration date (nullable) |
| `image_path` | VARCHAR(255) | Path to product image |
| `is_active` | BOOLEAN | Whether product is active |
| `created_at` | TIMESTAMP | Record creation time |
| `updated_at` | TIMESTAMP | Last update time |

---

### 2. **transactions** (formerly `sales`)
Stores each sales transaction header information.

| Column | Type | Description |
|--------|------|-------------|
| `transaction_id` | INT (PK) | Auto-increment unique identifier |
| `transaction_datetime` | TIMESTAMP | When the sale occurred |
| `total_amount` | DECIMAL(10,2) | Sum of all items before discount |
| `discount_amount` | DECIMAL(10,2) | Discount applied |
| `final_amount` | DECIMAL(10,2) | Total after discount |
| `notes` | VARCHAR(255) | Optional transaction notes |

**Note:** No user reference - simplified system has hardcoded authentication only.

---

### 3. **transaction_items** (formerly `sale_items`)
Stores individual line items for each transaction.

| Column | Type | Description |
|--------|------|-------------|
| `transaction_item_id` | INT (PK) | Auto-increment unique identifier |
| `transaction_id` | INT (FK) | Reference to transactions table |
| `product_id` | INT (FK) | Reference to products table |
| `quantity` | INT | Number of units sold |
| `sold_price` | DECIMAL(10,2) | **Actual price customer paid per unit at time of sale** |
| `cost_at_sale` | DECIMAL(10,2) | **Historical cost per unit when this item was sold** (for accurate profit calculation) |
| `item_total` | DECIMAL(10,2) | **Subtotal: sold_price × quantity** |

#### ❓ Why Both `cost_per_unit` and `cost_at_sale`?

**Problem:** Product costs change over time. If you bought Coke for ₱10 last month and now buy it for ₱12, which cost should be used to calculate last month's profit?

**Solution:** 
- `products.cost_per_unit` = Current cost (what you pay NOW)
- `transaction_items.cost_at_sale` = Historical cost (what you paid WHEN sold)

This ensures accurate profit calculations even when supplier prices change.

---

### 4. **restock_log**
Tracks all stock additions and their capital costs.

| Column | Type | Description |
|--------|------|-------------|
| `restock_id` | INT (PK) | Auto-increment unique identifier |
| `product_id` | INT (FK) | Reference to products table |
| `quantity_added` | INT | Number of units added |
| `cost_per_unit` | DECIMAL(10,2) | **Cost per unit at time of restock** |
| `total_cost` | DECIMAL(10,2) | **Total: cost_per_unit × quantity_added** |
| `restock_date` | TIMESTAMP | When restock occurred |
| `notes` | VARCHAR(255) | Optional notes |

**Note:** No user reference - simplified system doesn't track who did the restock.

---

## 🔗 Relationships Summary

| From | To | Relationship | Description |
|------|-----|--------------|-------------|
| `products` | `transaction_items` | 1:N | One product can appear in many transaction lines |
| `transactions` | `transaction_items` | 1:N | One transaction has many line items |
| `products` | `restock_log` | 1:N | One product can have many restock records |

---

## 🚫 Removed Entities

### Users Table (REMOVED)
- **Reason:** Teacher requested simplification
- **Replacement:** Hardcoded authentication in code
- **Impact:** No cashier tracking per transaction or restock

### Categories Table (NEVER EXISTED)
- **Reason:** Category is embedded as a simple field in products table
- **Format:** VARCHAR(50) column in products

### Product Code (REMOVED)
- **Reason:** Redundant with product_id
- **Replacement:** Use product_id as sole identifier

### Stock History Table (REMOVED)
- **Reason:** Simplification; restock_log provides sufficient audit trail

---

## 💰 Price Field Clarification

| Field | Location | Meaning |
|-------|----------|---------|
| `cost_per_unit` | products | Current supplier cost |
| `sell_price` | products | Current selling price (SRP) |
| `sold_price` | transaction_items | Actual selling price at time of sale |
| `cost_at_sale` | transaction_items | Historical cost at time of sale |
| `item_total` | transaction_items | Line subtotal (sold_price × quantity) |
| `total_amount` | transactions | Sum of all item_totals |
| `final_amount` | transactions | Amount after discount |

---

## 📝 Notes

- All monetary values use DECIMAL(10,2) for precision
- Foreign keys enforce referential integrity
- Soft deletes via `is_active` flag on products
- Timestamps provide audit trail for all transactions
