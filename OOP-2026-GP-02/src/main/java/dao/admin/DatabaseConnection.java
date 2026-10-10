package main.java.dao.admin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Database credentials and URL
    private static final String URL = "jdbc:mysql://localhost:3306/tecfams_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Singleton instance of Connection
    private static Connection connection = null;

    private DatabaseConnection() {
        // Private constructor to prevent instantiation
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Ensure the driver class is loaded (optional in newer JDBC, but good practice)
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // Establish the connection
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connection established successfully.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Please add the mysql-connector-j jar to your build path.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database. Ensure MySQL server is running and the 'tecfams_db' exists.");
            e.printStackTrace();
        }
        return connection;
    }
}
