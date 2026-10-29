package main.java.dao.admin;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class SettingsDAO {

    private static final Map<String, String[]> DEFAULTS = new LinkedHashMap<>();

    static {
        // Academic
        DEFAULTS.put("academic.year", new String[]{"2024/2025", "Academic", "Current active academic year"});
        DEFAULTS.put("academic.semester", new String[]{"Semester 1", "Academic", "Current active academic semester"});
        DEFAULTS.put("academic.attendance_threshold", new String[]{"80", "Academic", "Minimum attendance % required for exam eligibility"});
        DEFAULTS.put("academic.registration_status", new String[]{"Open", "Academic", "Course enrollment status (Open / Closed)"});
        DEFAULTS.put("academic.medical_grace_days", new String[]{"14", "Academic", "Grace period in days for medical certificate submission"});

        // Institution
        DEFAULTS.put("faculty.institution_name", new String[]{"University of Ruhuna", "Institution", "University or Higher Education Institute Name"});
        DEFAULTS.put("faculty.name", new String[]{"Faculty of Technology", "Institution", "Faculty / Campus Department Name"});
        DEFAULTS.put("faculty.system_name", new String[]{"FAMS - Faculty Academic Management System", "Institution", "System name / Portal title"});
        DEFAULTS.put("faculty.email", new String[]{"admin@fot.ruh.ac.lk", "Institution", "Official Faculty contact email"});
        DEFAULTS.put("faculty.phone", new String[]{"+94 41 2245765", "Institution", "Official Faculty telephone number"});
        DEFAULTS.put("faculty.address", new String[]{"Karagoda Uyangoda, Kamburupitiya, Sri Lanka", "Institution", "Faculty campus physical address"});

        // Security
        DEFAULTS.put("security.default_password", new String[]{"default123", "Security", "Initial temporary password for newly created accounts"});
        DEFAULTS.put("security.min_password_length", new String[]{"8", "Security", "Minimum required password character length"});
        DEFAULTS.put("security.max_login_attempts", new String[]{"5", "Security", "Maximum failed login attempts before account lock"});
        DEFAULTS.put("security.session_timeout", new String[]{"30", "Security", "User session inactivity timeout in minutes"});
        DEFAULTS.put("security.force_password_change", new String[]{"true", "Security", "Require user to change default password on first login"});

        // Maintenance
        DEFAULTS.put("system.maintenance_mode", new String[]{"false", "Maintenance", "Enable maintenance mode (locks student & staff access)"});
        DEFAULTS.put("system.notice_expiry_days", new String[]{"30", "Maintenance", "Days after which published notices are auto-archived"});
        DEFAULTS.put("system.email_notifications", new String[]{"true", "Maintenance", "Enable system-wide automated email alerts"});
        DEFAULTS.put("system.auto_backup", new String[]{"true", "Maintenance", "Enable scheduled automatic database backups"});
    }

    public static void ensureSettingsTableExists() {
        String createSql = 
            "CREATE TABLE IF NOT EXISTS system_settings (" +
            "    setting_key VARCHAR(100) PRIMARY KEY, " +
            "    setting_value TEXT NOT NULL, " +
            "    category VARCHAR(50) NOT NULL, " +
            "    description VARCHAR(255), " +
            "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
            ")";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createSql);

            String insertSql = "INSERT IGNORE INTO system_settings (setting_key, setting_value, category, description) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                for (Map.Entry<String, String[]> entry : DEFAULTS.entrySet()) {
                    insertStmt.setString(1, entry.getKey());
                    insertStmt.setString(2, entry.getValue()[0]);
                    insertStmt.setString(3, entry.getValue()[1]);
                    insertStmt.setString(4, entry.getValue()[2]);
                    insertStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> getAllSettings() {
        ensureSettingsTableExists();
        Map<String, String> settings = new LinkedHashMap<>();

        // Initialize with hardcoded defaults first to ensure full coverage
        for (Map.Entry<String, String[]> entry : DEFAULTS.entrySet()) {
            settings.put(entry.getKey(), entry.getValue()[0]);
        }

        String query = "SELECT setting_key, setting_value FROM system_settings";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                settings.put(rs.getString("setting_key"), rs.getString("setting_value"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return settings;
    }

    public static String getSetting(String key, String defaultValue) {
        ensureSettingsTableExists();
        String query = "SELECT setting_value FROM system_settings WHERE setting_key = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("setting_value");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public static boolean updateSettings(Map<String, String> newSettings) {
        ensureSettingsTableExists();
        String sql = "UPDATE system_settings SET setting_value = ? WHERE setting_key = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (Map.Entry<String, String> entry : newSettings.entrySet()) {
                stmt.setString(1, entry.getValue());
                stmt.setString(2, entry.getKey());
                stmt.addBatch();
            }
            stmt.executeBatch();
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean resetCategoryToDefaults(String category) {
        ensureSettingsTableExists();
        Map<String, String> categoryDefaults = new HashMap<>();
        for (Map.Entry<String, String[]> entry : DEFAULTS.entrySet()) {
            if (entry.getValue()[1].equalsIgnoreCase(category)) {
                categoryDefaults.put(entry.getKey(), entry.getValue()[0]);
            }
        }
        return updateSettings(categoryDefaults);
    }

    public static boolean resetAllToDefaults() {
        ensureSettingsTableExists();
        Map<String, String> allDefaults = new HashMap<>();
        for (Map.Entry<String, String[]> entry : DEFAULTS.entrySet()) {
            allDefaults.put(entry.getKey(), entry.getValue()[0]);
        }
        return updateSettings(allDefaults);
    }

    public static boolean exportDatabaseBackup(File targetFile) {
        String[] tables = {
            "departments", "users", "admins", "lecturers", "technical_officers",
            "students", "courses", "course_registrations", "attendance_sessions",
            "attendances", "evaluation_components", "marks", "medicals",
            "notices", "timetables", "timetable_entries", "system_settings"
        };

        try (Connection conn = DatabaseConnection.getConnection();
             PrintWriter pw = new PrintWriter(new FileWriter(targetFile))) {

            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            pw.println("-- ========================================================");
            pw.println("-- TECFAMS (Faculty Academic Management System) Backup");
            pw.println("-- Database: tecfams_db");
            pw.println("-- Generated on: " + timeStamp);
            pw.println("-- Host: 127.0.0.1:3306");
            pw.println("-- ========================================================");
            pw.println("SET FOREIGN_KEY_CHECKS=0;");
            pw.println();

            for (String tableName : tables) {
                pw.println("-- --------------------------------------------------------");
                pw.println("-- Table data for `" + tableName + "`");
                pw.println("-- --------------------------------------------------------");

                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM `" + tableName + "`")) {
                    ResultSetMetaData md = rs.getMetaData();
                    int colCount = md.getColumnCount();

                    while (rs.next()) {
                        StringBuilder sb = new StringBuilder("INSERT INTO `" + tableName + "` VALUES (");
                        for (int i = 1; i <= colCount; i++) {
                            Object val = rs.getObject(i);
                            if (val == null) {
                                sb.append("NULL");
                            } else if (val instanceof Number) {
                                sb.append(val);
                            } else {
                                sb.append("'").append(val.toString().replace("'", "''")).append("'");
                            }
                            if (i < colCount) sb.append(", ");
                        }
                        sb.append(");");
                        pw.println(sb.toString());
                    }
                } catch (SQLException ex) {
                    pw.println("-- Error exporting table " + tableName + ": " + ex.getMessage());
                }
                pw.println();
            }

            pw.println("SET FOREIGN_KEY_CHECKS=1;");
            pw.println("-- Backup completed successfully.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
