# 🏪 Sari-Sari Store System

Welcome to the **Sari-Sari Store System** repository! This is a Java-based desktop application designed to manage inventory, sales, and restocking for a local retail store.

## 🚀 How to Run the System

Follow these steps to get the project running on your local machine:

### 1. Prerequisites & Environment Setup
Before opening the code, ensure you have the following installed:
* **Java Development Kit (JDK):** Version 17 or higher is recommended.
* **MySQL Server:** You will need a local database instance (like XAMPP or MySQL Workbench).
* **VS Code Extensions:** To make Java work properly, you **MUST** install the **"Extension Pack for Java"** by Microsoft from the VS Code Marketplace.

### 2. Database Configuration
1. Open your MySQL admin tool (e.g., phpMyAdmin or MySQL Workbench).
2. Create a new database named `sarisari_store`.
3. Import the `database_schema.sql` file located in the `sarisari_store/` folder to create the necessary tables.
4. **Warning:** Open `src/dao/DBConnection.java` and update the `URL`, `USER`, and `PASSWORD` strings to match your local MySQL credentials. 

### 3. Setting Up the Project in VS Code
1. Clone this repository and open the `sarisari_store` folder in VS Code.
2. **Add the Library:** The system requires a database connector. Go to the **Java Projects** view in the sidebar, find **Referenced Libraries**, and add the `lib/mysql-connector-j-9.6.0.jar` file.
3. **Compile and Run:** Locate `src/gui/LoginFrame.java`, right-click, and select **Run Java**. 

---

## ⚠️ Important Warnings & Tips

* **Database Connection:** The system will fail to launch if it cannot connect to your MySQL server. Double-check your username and password in `DBConnection.java`. 
* **Images:** If product images do not appear, ensure the `images/products/` directory exists and contains the images referenced in your database.
* **Vibe Coding Note:** Since parts of this were "vibe coded," please use **Git Branches** when testing new UI changes to avoid breaking the main working version of the `MainFrame` or `Dashboard`.
* **Clean & Build:** If you encounter weird errors after pulling updates, try running `Clean Java Language Server Workspace` from the VS Code Command Palette (`Ctrl+Shift+P`).

---

## 👥 Project Team
* **Lead/Architect:** Alwinson Abejuela Bustamante
* **Design & Documentation:** tba

Happy coding! 💻
