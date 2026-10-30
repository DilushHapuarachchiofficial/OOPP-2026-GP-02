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
    private static final String DB_USER = "root";

    private static final String[] DB_URLS = {
        "jdbc:mysql://127.0.0.1:3306/tecfams_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
        "jdbc:mysql://localhost:3306/tecfams_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
        "jdbc:mysql://127.0.0.1:3306/tecFams?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
        "jdbc:mysql://localhost:3306/tecFams?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
    };

    // Candidate passwords for team members
    private static final String[] PASSWORDS = {
        "DVBH123@mysql",
        "1234",
        "#Hirunika1",
        "",
        "root"
    };

    // Cached active credentials for instant subsequent connections
    private static volatile String activeWorkingUrl = null;
    private static volatile String activeWorkingPwd = null;

    // Cache for user details loaded from tecfams_db.sql file
    private static Map<String, UserRecord> sqlDumpUsers = null;

    public static class UserRecord {
        private final int userId;
        private final String username;
        private final String passwordHash;
        private final String role;

        public UserRecord(int userId, String username, String passwordHash, String role) {
            this.userId = userId;
            this.username = username;
            this.passwordHash = passwordHash;
            this.role = role;
        }

        public UserRecord(String username, String passwordHash, String role) {
            this(0, username, passwordHash, role);
        }

        public int getUserId() {
            return userId;
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
     * Attempts MySQL connection with candidate passwords ("DVBH123@mysql", "1234", "#Hirunika1", and fallback empty/root).
     * Automatically discovers which credentials are valid for the active team member environment.
     * Returns null if MySQL server is offline or unreachable.
     */
    public static Connection getConnection() {
        try {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Class.forName("com.mysql.jdbc.Driver");
            }

            // 1. Fast path: try cached working credentials first
            if (activeWorkingUrl != null && activeWorkingPwd != null) {
                try {
                    Connection conn = DriverManager.getConnection(activeWorkingUrl, DB_USER, activeWorkingPwd);
                    if (conn != null && !conn.isClosed()) {
                        return conn;
                    }
                } catch (Exception ignored) {
                    activeWorkingUrl = null;
                    activeWorkingPwd = null;
                }
            }

            // 2. Iterate through configured team passwords across candidate URLs
            for (String url : DB_URLS) {
                for (String pwd : PASSWORDS) {
                    try {
                        Connection conn = DriverManager.getConnection(url, DB_USER, pwd);
                        if (conn != null && !conn.isClosed()) {
                            activeWorkingUrl = url;
                            activeWorkingPwd = pwd;
                            return conn;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            return null;
        } catch (Exception e) {
            return null;
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
            // Matches tuples: (12, 'username', 'password', 'Role', ...)
            Pattern pattern = Pattern.compile("\\(\\s*(\\d+)\\s*,\\s*'([^']+)'\\s*,\\s*'([^']+)'\\s*,\\s*'([^']+)'");

            while ((line = br.readLine()) != null) {
                if (line.contains("INSERT INTO `users`") || line.contains("INSERT INTO users")) {
                    inUsersTable = true;
                }

                if (inUsersTable) {
                    Matcher matcher = pattern.matcher(line);
                    while (matcher.find()) {
                        int uid = 0;
                        try {
                            uid = Integer.parseInt(matcher.group(1));
                        } catch (Exception ignored) {}
                        String username = matcher.group(2);
                        String passwordHash = matcher.group(3);
                        String role = matcher.group(4);
                        sqlDumpUsers.put(username, new UserRecord(uid, username, passwordHash, role));
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
