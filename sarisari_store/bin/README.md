# Sari-Sari Store Inventory and Sales Management System

A polished, desktop-based inventory and sales management system designed for small sari-sari stores. Built with Java Swing and MySQL/MariaDB, featuring a modern design system and comprehensive stock/capital tracking.

## Features

### Core Modules

- **User Authentication** - Secure login with role-based access (Admin/Cashier).
- **Point of Sale (POS)** - Intuitive sales interface with automatic stock deduction and profit calculation.
- **Product Management** - CRUD operations for products, category tracking, and image uploads.
- **Dashboard** - Real-time insights: today's revenue, profit, capital spent, and low stock alerts.
- **Sales History** - Comprehensive transaction logs with detailed line-item views and date filtering.
- **Restock History** - **(New)** Dedicated module to track capital expenditure and audit stock additions.
- **User Management** - Admin tools for managing staff accounts and access levels.

### Key Capabilities

- **Capital Tracking** - Automatically logs Every restock event to monitor total investment.
- **Loss Prevention** - SRP validation ensures no product is sold below purchase price.
- **Visual Stock Alerts** - Color-coded indicators for products reaching critical stock levels.
- **Automated Profit Reporting** - Calculations based on historical purchase prices for accurate accounting.
- **Premium Design System** - Consistent modern aesthetics (Segoe UI, custom color palette) across all panels.

## Technology Stack

- **Language:** Java (JDK 8+)
- **GUI Framework:** Java Swing
- **Database:** MySQL 5.7+ / MariaDB 10.3+
- **JDBC Driver:** MySQL Connector/J

## Project Structure

```
sarisari_store/
├── database_schema.sql      # Database structure and seed data
├── bin/                    # Compiled classes
├── model/                  # Data models (Product, Sale, User, RestockLog)
├── dao/                    # Database access layer (DBConnection, ProductDAO, etc.)
└── gui/                    # UI Components
    ├── ThemeManager.java   # Centralized UI design and consistency engine
    ├── MainFrame.java      # Primary application window
    ├── LoginFrame.java     # Start screen
    ├── DashboardPanel.java # Analytics overview
    ├── ProductPanel.java   # Inventory management
    ├── SalesPanel.java     # POS terminal
    ├── SalesHistoryPanel.java
    ├── RestockHistoryPanel.java # Capital expenditure tracking
    └── UserManagementPanel.java
```

## Setup Instructions

### 1. Database Initialization

1. Ensure MySQL/MariaDB is running.
2. Run `database_schema.sql` to initialize the `sarisari_db` database, tables, and default data.
   ```sql
   SOURCE /path/to/database_schema.sql;
   ```

### 2. Configuration

Edit `dao/DBConnection.java` with your local database credentials:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/sarisari_db";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "your_password";
```

### 3. Compilation & Launch

Include the JDBC driver in your classpath and run `gui.LoginFrame`.

## Default Access

| Username | Password   | Role    |
| -------- | ---------- | ------- |
| admin    | admin123   | Admin   |
| cashier1 | cashier123 | Cashier |

## Role Privileges

- **Admin:** Full access to inventory, user management, and capital expenditure tracking.
- **Cashier:** Access to POS, read-only inventory, and viewing sales history.

## Premium UI & Design

The application utilizes a custom `ThemeManager` to ensure a premium look and feel.

- **Typography:** Uses _Segoe UI_ for a modern, native Windows feel.
- **Color Palette:** Curated harmonious colors for primary actions, success/danger states, and clean backgrounds.
- **Consistency:** Unified button styles, table themes, and spacing across the entire system.

## Important Notes

- **Images:** Product images are stored in `images/products/` folder, only the path is saved in database
- **Profit Calculation:** (SRP - Purchase Price) × Quantity.
- **Capital Spent:** Tracked separately in the `restock_log` table for accurate investment audits.
- **Validation:** System prevents sales of out-of-stock items and ensures SRP >= Purchase Price.

---

_Created for educational purposes - focusing on clean architecture and professional UI implementation._
