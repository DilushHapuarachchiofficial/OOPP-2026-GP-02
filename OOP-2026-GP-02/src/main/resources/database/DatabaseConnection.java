package main.resources.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Database credentials and URL for TecFAMS
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/tecfams_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Singleton instance of Connection
    private static Connection connection = null;

    public DatabaseConnection() {
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connection established successfully.");
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
            }
        }
    }
}
