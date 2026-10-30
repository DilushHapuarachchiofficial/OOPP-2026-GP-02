package main.java.dao.admin;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    // Database credentials and URLs
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
        "#Hirunika1",
        "",
        "root"
    };

    private static volatile String activeWorkingUrl = null;
    private static volatile String activeWorkingPwd = null;

    // Singleton instance of Connection
    private static Connection connection = null;
    private static boolean schemaChecked = false;

    private DatabaseConnection() {
        // Private constructor to prevent instantiation
    }

    public static Connection getConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }

            // Ensure the driver class is loaded
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Class.forName("com.mysql.jdbc.Driver");
            }

            // 1. Try active working credentials first
            if (activeWorkingUrl != null && activeWorkingPwd != null) {
                try {
                    Connection conn = DriverManager.getConnection(activeWorkingUrl, USER, activeWorkingPwd);
                    if (conn != null && !conn.isClosed()) {
                        connection = conn;
                        ensureSchemaCompatibility(connection);
                        return connection;
                    }
                } catch (SQLException ignored) {
                    activeWorkingUrl = null;
                    activeWorkingPwd = null;
                }
            }

            // 2. Iterate through all team passwords and URLs
            for (String url : URLS) {
                for (String pwd : PASSWORDS) {
                    try {
                        Connection conn = DriverManager.getConnection(url, USER, pwd);
                        if (conn != null && !conn.isClosed()) {
                            activeWorkingUrl = url;
                            activeWorkingPwd = pwd;
                            connection = conn;
                            System.out.println("Admin Database connection established successfully with url: " + url);
                            ensureSchemaCompatibility(connection);
                            return connection;
                        }
                    } catch (SQLException ignored) {
                    }
                }
            }

            System.err.println("Failed to connect to the database with any team credentials. Ensure MySQL server is running.");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Please add the mysql-connector-j jar to your build path.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database. Ensure MySQL server is running and the 'tecfams_db' exists.");
            e.printStackTrace();
        }
        return connection;
    }

    private static void ensureSchemaCompatibility(Connection conn) {
        if (schemaChecked || conn == null) return;
        schemaChecked = true;
        try (Statement stmt = conn.createStatement()) {
            // Ensure admins has address column
            if (!columnExists(conn, "admins", "address")) {
                stmt.executeUpdate("ALTER TABLE admins ADD COLUMN address TEXT DEFAULT NULL");
            }
            // Ensure users has status column
            if (!columnExists(conn, "users", "status")) {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'Active'");
            }
            // Ensure notices has attachment_path column
            if (!columnExists(conn, "notices", "attachment_path")) {
                stmt.executeUpdate("ALTER TABLE notices ADD COLUMN attachment_path VARCHAR(255) DEFAULT NULL");
            }
            // Ensure course_materials has file_path column and nullable binary fields
            if (!columnExists(conn, "course_materials", "file_path")) {
                stmt.executeUpdate("ALTER TABLE course_materials ADD COLUMN file_path VARCHAR(255) DEFAULT NULL");
                stmt.executeUpdate("ALTER TABLE course_materials MODIFY COLUMN file_name VARCHAR(255) NULL");
                stmt.executeUpdate("ALTER TABLE course_materials MODIFY COLUMN file_data LONGBLOB NULL");
            }
        } catch (Exception ex) {
            System.err.println("Schema compatibility check: " + ex.getMessage());
        }
    }

    private static boolean columnExists(Connection conn, String tableName, String columnName) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, tableName, columnName)) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
}
