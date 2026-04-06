# Sari-Sari Store Inventory and Sales Management System

A complete desktop-based inventory and sales management system designed for small sari-sari stores (convenience stores). Built with Java Swing and MySQL/MariaDB.

## Features

### Core Features
- **User Authentication** - Login system with role-based access (Admin/Cashier)
- **Product Management** - Add, edit, deactivate products with stock tracking
- **Point of Sale (POS)** - Record sales with automatic stock deduction
- **Profit Calculation** - Automatic profit computation per sale
- **Dashboard** - Today's sales, transaction count, profit summary, low stock alerts
- **Sales History** - View past transactions with date filtering
- **User Management** - Admin can add/edit users and reset passwords

### Key Capabilities
- Prevents negative stock (validates before sale)
- SRP must be >= Purchase Price (prevents loss-making sales)
- Low stock alerts on dashboard
- Image upload for products
- Transaction-based sales (all-or-nothing commit)

## Technology Stack

- **Language:** Java (JDK 8 or higher)
- **GUI Framework:** Java Swing
- **Database:** MySQL 5.7+ or MariaDB 10.3+
- **JDBC Driver:** mysql-connector-java

## Project Structure

```
sarisari_store/
├── database_schema.sql      # MySQL/MariaDB DDL
├── DBConnection.java        # Database connection manager
├── README.md               # This file
├── model/                  # Model classes
│   ├── User.java
│   ├── Product.java
│   ├── Sale.java
│   └── SaleItem.java
├── dao/                    # Data Access Objects
│   ├── UserDAO.java
│   ├── ProductDAO.java
│   └── SaleDAO.java
└── gui/                    # GUI classes
    ├── LoginFrame.java
    ├── MainFrame.java
    ├── DashboardPanel.java
    ├── ProductPanel.java
    ├── SalesPanel.java
    ├── SalesHistoryPanel.java
    └── UserManagementPanel.java
```

## Setup Instructions

### 1. Database Setup

1. Install MySQL or MariaDB on your computer
2. Open MySQL Workbench, phpMyAdmin, or command line
3. Run the `database_schema.sql` file:
   ```sql
   SOURCE /path/to/database_schema.sql;
   ```
   Or copy-paste the SQL contents and execute.

This will:
- Create the `sarisari_db` database
- Create all required tables
- Insert default users (admin/cashier)
- Insert sample products

### 2. Configure Database Connection

Edit `DBConnection.java` and update the credentials:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/sarisari_db";
private static final String DB_USER = "root";      // Your MySQL username
private static final String DB_PASSWORD = "";      // Your MySQL password
```

### 3. Add MySQL JDBC Driver

Download `mysql-connector-java-8.0.xx.jar` from:
https://dev.mysql.com/downloads/connector/j/

Add it to your project's classpath:
- **NetBeans:** Right-click Project → Properties → Libraries → Add JAR
- **Eclipse:** Right-click Project → Build Path → Add External JARs
- **IntelliJ:** File → Project Structure → Libraries → + → Java

### 4. Compile and Run

Compile all Java files:
```bash
javac -cp .:mysql-connector-java-8.0.xx.jar *.java model/*.java dao/*.java gui/*.java
```

Run the application:
```bash
java -cp .:mysql-connector-java-8.0.xx.jar gui.LoginFrame
```

On Windows, use `;` instead of `:` as classpath separator.

## Default Login Credentials

| Username   | Password    | Role    |
|------------|-------------|---------|
| admin      | admin123    | Admin   |
| cashier1   | cashier123  | Cashier |

## User Roles

### Admin
- Full access to all modules
- Can add/edit/deactivate products
- Can restock products
- Can manage users (add, edit, reset passwords)
- Can view all reports

### Cashier
- Can record sales (POS)
- Can view products (read-only)
- Can view dashboard
- Can view sales history
- **Cannot** edit products or manage users

## Usage Guide

### Recording a Sale
1. Login as Cashier or Admin
2. Click "Sales / POS" tab
3. Type product name/code in search box and click Search
4. Select product from results and click "Add to Cart"
5. Enter quantity when prompted
6. Repeat for more items
7. Enter discount amount (optional) and click Apply
8. Click "Complete Sale" to finalize

### Adding a Product (Admin only)
1. Login as Admin
2. Click "Products" tab
3. Click "Add Product" button
4. Fill in product details
5. Click Browse to select product image (optional)
6. Click OK to save

### Restocking (Admin only)
1. Select product from the table
2. Click "Restock" button
3. Enter quantity to add
4. Click OK

### Viewing Sales Reports
1. Click "Sales History" tab
2. Use date filters or click "Today's Sales"
3. Select a sale and click "View Details" to see line items

## Database Schema

### Tables

**users** - System users
- user_id (PK), username, password_hash, full_name, role, is_active

**products** - Product catalog
- product_id (PK), product_code, product_name, category, unit, purchase_price, srp, current_stock, min_stock_level, expiry_date, image_path, is_active

**sales** - Transaction headers
- sale_id (PK), user_id (FK), sale_datetime, total_amount, discount_amount, final_amount, notes

**sale_items** - Transaction line items
- sale_item_id (PK), sale_id (FK), product_id (FK), quantity, unit_price, purchase_price, line_total

**stock_history** - Audit trail for stock changes (optional)

## Important Notes

1. **Images:** Product images are stored in `images/products/` folder, only the path is saved in database
2. **Stock:** System prevents sales if stock is insufficient
3. **Profit:** Calculated as (SRP - Purchase Price) × Quantity
4. **Transactions:** Sales use database transactions - if any part fails, entire sale is rolled back
5. **Security:** Passwords are stored in plain text for simplicity (first-year level). In production, use proper hashing.

## Troubleshooting

### "Cannot connect to database" error
- Check if MySQL/MariaDB is running
- Verify DB_URL, DB_USER, DB_PASSWORD in DBConnection.java
- Ensure database `sarisari_db` exists

### "MySQL JDBC Driver not found" error
- Ensure mysql-connector-java JAR is in classpath
- Check JAR version compatibility with your Java version

### Images not showing
- Check if `images/products/` folder exists
- Verify image paths are correct in database

## Future Enhancements

For a more advanced project, consider:
- Barcode scanner integration
- Receipt printing
- Backup/restore database
- Supplier management
- Purchase order tracking
- Advanced reports (monthly, yearly)
- Multi-currency support
- VAT/tax calculation

## License

This project is created for educational purposes (first-year college project).

## Credits

Developed as a capstone project for Database Management and Programming courses.
