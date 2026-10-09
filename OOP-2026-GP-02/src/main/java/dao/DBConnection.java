package main.java.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DBConnection {

    // MySQL Database Configuration
    private static final String DB_NAME = "tecfams_db";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/" + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    // Secondary URL fallback if database is named tecFams
    private static final String ALT_DB_URL = "jdbc:mysql://localhost:3306/tecFams?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    // Cache for user details loaded from tecfams_db.sql file
    private static Map<String, UserRecord> sqlDumpUsers = null;

    public static class UserRecord {
        private final String username;
        private final String passwordHash;
        private final String role;

        public UserRecord(String username, String passwordHash, String role) {
            this.username = username;
            this.passwordHash = passwordHash;
            this.role = role;
        }

        public String getUsername() {
            return username;
        }

        public String getPasswordHash() {
            return passwordHash;
        }

        public String getRole() {
            return role;
        }
    }

    /**

     * Returns null if MySQL server is offline or JDBC driver is not present.
     */
    public static Connection getConnection() {
        try {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Class.forName("com.mysql.jdbc.Driver");
            }
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (Exception e1) {
            try {
                return DriverManager.getConnection(ALT_DB_URL, DB_USER, DB_PASSWORD);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    /**
     * Normalizes database role strings into standard TecFAMS UI roles.
     * 'Admin' -> 'Admin'
     * 'Lecturer' -> 'Lecturer'
     * 'Technical_Officer' -> 'Technical Officer'
     * 'Student' -> 'Undergraduate'
     */
    public static String normalizeRole(String dbRole) {
        if (dbRole == null) return null;
        String r = dbRole.trim();
        if (r.equalsIgnoreCase("Admin")) {
            return "Admin";
        }
        if (r.equalsIgnoreCase("Lecturer")) {
            return "Lecturer";
        }
        if (r.equalsIgnoreCase("Technical_Officer") || r.equalsIgnoreCase("Technical Officer") || r.equalsIgnoreCase("Officer")) {
            return "Technical Officer";
        }
        if (r.equalsIgnoreCase("Student") || r.equalsIgnoreCase("Undergraduate")) {
            return "Undergraduate";
        }
        return r;
    }

    /**
     * Gets user record parsed directly from tecfams_db.sql file.
     */
    public static synchronized UserRecord getSqlDumpUser(String username) {
        if (sqlDumpUsers == null) {
            loadSqlDumpUsers();
        }
        return sqlDumpUsers.get(username);
    }

    /**
     * Reads tecfams_db.sql database file and extracts user records from INSERT INTO users statements.
     */
    private static void loadSqlDumpUsers() {
        sqlDumpUsers = new HashMap<>();
        String[] possibleFileNames = {
            "tecfams_db.sql",
            "src/tecfams_db.sql",
            "src/main/resources/database/tecfams_db.sql",
            "src/main/resources/database/tecFams.sql",
            "src/main/resources/database/tecFams",
            "src/main/resources/database/tecfams_db",
            "C:/Users/kavis/OneDrive/Desktop/TecFAMS/OOPP-2026-GP-02/OOP-2026-GP-02/src/main/resources/database/tecFams",
            "C:/Users/kavis/OneDrive/Desktop/TecFAMS/OOPP-2026-GP-02/OOP-2026-GP-02/src/main/resources/database/tecfams_db.sql",
            "c:/Users/kavis/OneDrive/Desktop/TecFAMS/Local Project/src/tecfams_db.sql"
        };

        BufferedReader reader = null;

        for (String fileName : possibleFileNames) {
            File f = new File(fileName);
            if (f.exists()) {
                try {
                    reader = new BufferedReader(new FileReader(f));
                    break;
                } catch (Exception ignored) {
                }
            }
        }

        if (reader == null) {
            String[] resourcePaths = {
                "/database/tecfams_db.sql",
                "/database/tecFams.sql",
                "/database/tecFams",
                "/tecfams_db.sql"
            };
            for (String rPath : resourcePaths) {
                InputStream is = DBConnection.class.getResourceAsStream(rPath);
                if (is != null) {
                    reader = new BufferedReader(new InputStreamReader(is));
                    break;
                }
            }
        }

        if (reader == null) {
            return;
        }

        try (BufferedReader br = reader) {
            String line;
            boolean inUsersTable = false;
            // Matches tuples: (1, 'username', 'password', 'Role', ...)
            Pattern pattern = Pattern.compile("\\(\\s*\\d+\\s*,\\s*'([^']+)'\\s*,\\s*'([^']+)'\\s*,\\s*'([^']+)'");

            while ((line = br.readLine()) != null) {
                if (line.contains("INSERT INTO `users`") || line.contains("INSERT INTO users")) {
                    inUsersTable = true;
                }

                if (inUsersTable) {
                    Matcher matcher = pattern.matcher(line);
                    while (matcher.find()) {
                        String username = matcher.group(1);
                        String passwordHash = matcher.group(2);
                        String role = matcher.group(3);
                        sqlDumpUsers.put(username, new UserRecord(username, passwordHash, role));
                    }

                    if (line.trim().endsWith(";")) {
                        inUsersTable = false;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading tecfams_db.sql file: " + e.getMessage());
        }
    }
}
