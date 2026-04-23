package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection - Database Connection Manager
 * 
 * This class provides a centralized way to get database connections.
 * It uses JDBC to connect to MySQL/MariaDB.
 * 
 * NOTE: Update the credentials below to match your database setup.
 */
public class DBConnection {
    
    // Database configuration - MODIFY THESE TO MATCH YOUR SETUP
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sarisari_db";
    private static final String DB_USER = "root";  // Change to your MySQL username
    private static final String DB_PASSWORD = "@Alwinson100";  // Change to your MySQL password
    
    // JDBC Driver class name
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    
    // Static block to load the driver class
    static {
        try {
            Class.forName(DRIVER_CLASS);
            System.out.println("MySQL JDBC Driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: MySQL JDBC Driver not found!");
            System.err.println("Please add mysql-connector-java to your classpath.");
            e.printStackTrace();
        }
    }
    
    /**
     * Get a connection to the database
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    /**
     * Test the database connection
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("Database connection successful!");
            return true;
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Close a connection safely
     * @param conn the connection to close
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
