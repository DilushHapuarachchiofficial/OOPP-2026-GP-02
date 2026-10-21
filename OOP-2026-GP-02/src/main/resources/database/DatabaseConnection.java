package main.resources.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Dedicated database connection manager for MySQL Server 8.0 (MySQL Workbench).
 * Strictly connects to the local MySQL Server service on port 3306 (tecfams_db).
 * All XAMPP fallback code has been permanently removed.
 */
public class DatabaseConnection {
    // MySQL Server 8.0 credentials & connection URL
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/tecfams_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    public DatabaseConnection() {
    }

    /**
     * Connects directly and strictly to the MySQL Server 8.0 database (tecfams_db).
     *
     * @return Valid Connection object, or null if MySQL Server cannot be reached.
     */
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("[MySQL Server Error] Driver not found: " + e.getMessage());
            return null;
        } catch (SQLException e) {
            System.err.println("[MySQL Server Error] Connection to MySQL Server (tecfams_db) failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Safely closes a database connection.
     *
     * @param conn Connection to close.
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
