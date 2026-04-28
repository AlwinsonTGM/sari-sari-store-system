# Sari-Sari Store Inventory and Sales Management System

A desktop application for managing inventory, sales, and profits for Filipino convenience stores (Sari-Sari Stores). This project uses Java Swing and MySQL.

---

## 🛠️ Setup Instructions for Groupmates

To get this project working on your local machine, follow these steps exactly:

### 1. Prerequisites
Ensure you have the following installed:
- **JDK (Java Development Kit)**: Version 17 or 21 (LTS) is recommended. [Download here](https://www.oracle.com/java/technologies/downloads/).
- **MySQL Server**: You need a running MySQL or MariaDB instance (e.g., via XAMPP or a standalone installation).

### 2. Necessary VS Code Extensions
Open VS Code and install these extensions:
- **Extension Pack for Java** (Microsoft) - *This is the most important one.*
- **MySQL** (optional, for viewing the database within VS Code)

### 3. How to Import the Project
1. **Clone/Download** the repository to your computer.
2. Open VS Code.
3. Go to `File` > `Open Folder...` and select the `sarisari_store` folder.
4. **Wait** for the Java extension to initialize (look for the "Java" icon/status in the bottom bar).
5. If VS Code asks to import the project as a "Java Project", click **Yes/Import**.

> [!NOTE]
> The project is already configured to find the database driver in the `lib/` folder automatically. You don't need to manually add the JAR file.

### 4. Database Setup
1. Open your MySQL client (e.g., phpMyAdmin, MySQL Workbench, or Command Line).
2. Create a database named `sarisari_db`.
3. Import the `database_schema.sql` file located in the project root.
4. **Update Credentials**: Open `src/dao/DBConnection.java` and change the `USER` and `PASSWORD` to match your local MySQL settings:
   ```java
   private static final String USER = "root";       // default is usually root
   private static final String PASSWORD = "your_password"; 
   ```

---

## 🚀 Running the Application

### Option 1: VS Code Run Button (Easiest)
1. Open `src/gui/LoginFrame.java`.
2. Click the **Run** button (play icon) at the top right of the file.
3. Alternatively, press **F5** or go to the **Run and Debug** tab and select "SariSari Store".

### Option 2: PowerShell Script
If you are on Windows, you can just run the included script:
```powershell
./run.ps1
```

---

## 🔑 Default Credentials
Since user management is simplified, use these hardcoded accounts to log in:

| Role    | Username | Password   |
| ------- | -------- | ---------- |
| Owner   | owner    | owner123   |
| Cashier | cashier  | cashier123 |

---

## 📁 Project Structure
- `src/`: Java source code (MVC Architecture)
  - `model/`: Data entities (Product, Transaction, etc.)
  - `dao/`: Database logic (Data Access Objects)
  - `gui/`: User interface components
- `lib/`: External dependencies (MySQL JDBC Driver)
- `bin/`: Compiled files (automatically generated, ignored by Git)
- `database_schema.sql`: SQL script to create the database

---

## ⚠️ Recent Changes
- **Product Code removed**: We now use `product_id` as the primary identifier.
- **User Management**: Authentication is hardcoded in `LoginFrame` for simplicity.
- **Simplified Database**: Categories are now stored as strings within the product table.

---

## 📜 Features
- **Inventory**: CRUD operations, stock tracking, and low-stock alerts.
- **POS**: Shopping cart system with automatic stock deduction and profit tracking.
- **Reports**: Transaction history and capital/profit analytics.
- **Images**: Support for uploading and displaying product images.
