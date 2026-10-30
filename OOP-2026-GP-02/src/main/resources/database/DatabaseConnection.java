package main.resources.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Dedicated database connection manager for TecFAMS MySQL Database.
 * Connects to local MySQL Server (tecfams_db) with automatic fallback for credentials and hosts.
 */
public class DatabaseConnection {

    private static final String[] URLS = {
        "jdbc:mysql://127.0.0.1:3306/tecfams_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
        "jdbc:mysql://localhost:3306/tecfams_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
        "jdbc:mysql://127.0.0.1:3306/tecFams?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
        "jdbc:mysql://localhost:3306/tecFams?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
    };

    private static final String USER = "root";

    private static final String[] PASSWORDS = {
        "DVBH123@mysql",
        "1234",
        "",
        "root"
    };

    public DatabaseConnection() {
    }

    /**
     * Connects to the MySQL database (tecfams_db) trying configured credentials.
     *
     * @return Valid Connection object, or null if MySQL Server cannot be reached.
     */
    public static Connection getConnection() {
        try {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Class.forName("com.mysql.jdbc.Driver");
            }

            for (String url : URLS) {
                for (String pwd : PASSWORDS) {
                    try {
                        Connection conn = DriverManager.getConnection(url, USER, pwd);
                        if (conn != null && !conn.isClosed()) {
                            return conn;
                        }
                    } catch (SQLException ignored) {
                    }
                }
            }

            System.err.println("[MySQL Server Error] Unable to connect to MySQL database with any configured credentials.");
            return null;
        } catch (Exception e) {
            System.err.println("[MySQL Server Error] Driver error: " + e.getMessage());
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
