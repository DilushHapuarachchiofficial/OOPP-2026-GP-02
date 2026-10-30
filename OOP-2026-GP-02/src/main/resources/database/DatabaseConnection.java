package main.resources.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection provides a singleton connection to the TecFAMS MySQL database.
 * Reusable across all modules adhering to the project's database schema.
 */
public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/tecfams_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String DEFAULT_PASSWORD = "#Hirunika1";

    private static Connection connection = null;

    public DatabaseConnection() {
    }

    /**
     * Obtains an active connection to the TecFAMS MySQL database.
     * Tries configured password (1234) and falls back to blank password (e.g. XAMPP default).
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                } catch (ClassNotFoundException ignored) {
                    // Modern MySQL JDBC drivers auto-register via ServiceLoader
                }

                // Try common local MySQL passwords: blank (default), 1234, root
                String[] candidatePasswords = {"", DEFAULT_PASSWORD, "root"};
                for (String pwd : candidatePasswords) {
                    try {
                        connection = DriverManager.getConnection(URL, USER, pwd);
                        if (connection != null && !connection.isClosed()) {
                            break;
                        }
                    } catch (SQLException ignored) {
                    }
                }

                if (connection == null || connection.isClosed()) {
                    System.err.println("DatabaseConnection: Could not connect to MySQL server (port 3306) with root user.");
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("DatabaseConnection: SQL error: " + e.getMessage());
            return null;
        }
        return connection;
    }

    /**
     * Safely closes an open connection.
     */
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

