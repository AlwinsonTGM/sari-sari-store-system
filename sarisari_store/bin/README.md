# Sari-Sari Store Inventory and Sales Management System

A simplified desktop application for managing inventory, sales, and profits for Filipino convenience stores (Sari-Sari Stores).

## ⚠️ Recent Changes (Simplified Version)

Per academic requirements, the system has been simplified:

### Removed Features:
- **User Management** - No more user table; authentication is now hardcoded (Owner/Cashier only)
- **Categories Entity** - Now a simple field in products table
- **Product Code** - Removed; using `product_id` as sole identifier
- **Stock History** - Simplified audit trail

### What Still Works:
✅ Product management (CRUD)  
✅ Point-of-sale transactions  
✅ Inventory tracking with low-stock alerts  
✅ Profit calculations  
✅ Restock logging with capital tracking  
✅ Sales history and reports  

### Database Schema Changes:
- `users` table → **REMOVED** (hardcoded authentication)
- `sales` table → **RENAMED** to `transactions`
- `sale_items` table → **RENAMED** to `transaction_items`
- `products.product_code` → **REMOVED**
- `stock_history` table → **REMOVED**
- `restock_log.user_id` → **REMOVED**

See [ENTITY_RELATIONSHIPS.md](ENTITY_RELATIONSHIPS.md) for complete documentation.

---

## Overview

This system provides essential retail management features:

- **Inventory Management**: Track products, stock levels, and expiry dates
- **Point of Sale**: Process sales transactions with multiple items
- **Profit Tracking**: Calculate profits based on purchase price vs SRP
- **Low Stock Alerts**: Get notified when products need restocking
- **Restock Logging**: Track capital spent on inventory replenishment
- **Sales Reports**: View transaction history and analytics

## Technology Stack

- **Language**: Java (JDK 8+)
- **GUI**: Java Swing
- **Database**: MySQL/MariaDB with JDBC
- **Architecture**: MVC Pattern (Model-View-Controller)

## Project Structure

```
sarisari_store/
├── model/              # Data models (entities)
│   ├── Product.java    # Product entity
│   ├── Sale.java       # Transaction entity
│   ├── SaleItem.java   # Transaction line item
│   └── RestockLog.java # Restock log entity
│
├── dao/                # Data Access Objects (database operations)
│   ├── DBConnection.java
│   ├── ProductDAO.java
│   └── SaleDAO.java
│
├── gui/                # Graphical User Interface
│   ├── LoginFrame.java
│   ├── MainFrame.java
│   ├── DashboardPanel.java
│   ├── ProductPanel.java
│   ├── SalesPanel.java
│   ├── SalesHistoryPanel.java
│   ├── RestockHistoryPanel.java
│   └── ThemeManager.java
│
├── database_schema.sql # MySQL schema definition
├── ENTITY_RELATIONSHIPS.md # Detailed ER documentation
└── README.md           # This file
```

## Database Setup

1. Install MySQL or MariaDB
2. Create the database:
```bash
mysql -u root -p < database_schema.sql
```

3. Update database credentials in `dao/DBConnection.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/sarisari_db";
private static final String USER = "root";
private static final String PASSWORD = "your_password";
```

## Running the Application

1. Compile all Java files:
```bash
javac -d bin src/**/*.java
```

2. Run the application:
```bash
java -cp bin gui.LoginFrame
```

## Default Credentials (Hardcoded)

Since user management was removed, credentials are now hardcoded:

| Role | Username | Password |
|------|----------|----------|
| Owner | owner | owner123 |
| Cashier | cashier | cashier123 |

**Note**: To change passwords, you must modify the source code and recompile.

## Key Concepts

### Pricing Fields Explained:

| Field Location | Field Name | Meaning | Purpose |
|----------------|------------|---------|---------|
| `products.purchase_price` | Unit Cost | What the owner paid to supplier | Current inventory valuation |
| `products.srp` | Suggested Retail Price | What customers pay | Current selling price |
| `transaction_items.unit_price` | Selling Price | SRP at time of sale | Historical sale price |
| `transaction_items.purchase_price` | Cost Snapshot | Cost at time of sale | Historical cost for profit calculation |
| `restock_log.purchase_price` | Restock Cost | Cost when restocking | Capital tracking |

### Why is `purchase_price` duplicated in `transaction_items`?

This is intentional! It creates a historical snapshot so that:
- Past transactions maintain accurate profit calculations
- Changing product costs today doesn't affect historical reports
- You can track profit trends over time accurately

### Profit Calculation:

```
Profit per unit = srp - purchase_price
Profit per line = (unit_price - purchase_price) × quantity
Transaction Profit = Sum of all line profits - discount
```

## Entity Relationships

See [ENTITY_RELATIONSHIPS.md](ENTITY_RELATIONSHIPS.md) for detailed documentation.

**Simplified ER Diagram:**

```
┌─────────────────┐
│    products     │
│  ─────────────  │
│  product_id (PK)│
│  product_name   │
│  category       │
│  purchase_price │◄── Unit Cost
│  srp            │◄── Retail Price
│  current_stock  │
└────────┬────────┘
         │ 1:N
         ├──────────────────────┐
         │                      │
         ▼                      ▼
┌─────────────────┐    ┌─────────────────┐
│transaction_items│    │   restock_log   │
│─────────────────│    │─────────────────│
│transaction_id(FK)│   │product_id (FK)  │
│product_id (FK)  │    │quantity_added   │
│quantity         │    │purchase_price   │
│unit_price       │    │total_cost       │
│purchase_price   │    └─────────────────┘
│line_total       │
└────────┬────────┘
         │ N:1
         ▼
┌─────────────────┐
│  transactions   │
│─────────────────│
│transaction_id   │
│datetime         │
│total_amount     │
│discount_amount  │
│final_amount     │
└─────────────────┘
```

**Relationship Summary:**

| From | To | Relationship | Description |
|------|-----|--------------|-------------|
| `products` | `transaction_items` | 1:N | One product can appear in many transaction line items |
| `transactions` | `transaction_items` | 1:N | One transaction contains many line items |
| `products` | `restock_log` | 1:N | One product can have many restock events |

## Features

### Product Management
- Add, edit, delete products
- Track current stock levels
- Set minimum stock thresholds
- Manage expiry dates
- Upload product images

### Point of Sale (POS)
- Scan/search products
- Add multiple items to cart
- Apply discounts
- Generate receipts
- Automatic stock deduction

### Inventory Tracking
- Real-time stock updates
- Low stock alerts
- Restock logging
- Capital expenditure tracking

### Reports & Analytics
- Daily sales summary
- Transaction history
- Top selling products
- Profit calculations
- Restock history

## License

This project is created for educational purposes.

## Contributors

Created as a first-year college project for learning Java Swing and database management.
