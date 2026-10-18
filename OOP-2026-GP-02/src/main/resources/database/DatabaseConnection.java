package main.resources.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Database credentials and URL for TecFAMS (MySQL Server 8.0 / MySQL Workbench)
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/tecfams_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    // Singleton instance of Connection
    private static Connection connection = null;

    public DatabaseConnection() {
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try {
                    // 1. Try connecting with MySQL Workbench password
                    connection = DriverManager.getConnection(URL, USER, PASSWORD);
                } catch (SQLException ex) {
                    // 2. Fallback to empty password (e.g. XAMPP default) or 'root'
                    try {
                        connection = DriverManager.getConnection(URL, USER, "");
                    } catch (SQLException ex2) {
                        connection = DriverManager.getConnection(URL, USER, "root");
                    }
                }
                System.out.println("Database connection established successfully with MySQL Workbench (tecfams_db).");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Falling back to offline/mock data mode.");
        } catch (SQLException e) {
            System.err.println("Failed to connect to MySQL database (" + e.getMessage() + "). Running with local/mock data.");
        }
        return connection;
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
            } finally {
                connection = null;
            }
        }
    }
}
