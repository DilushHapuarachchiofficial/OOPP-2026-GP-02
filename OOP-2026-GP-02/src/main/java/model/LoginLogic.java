package main.java.model;

import main.java.dao.DBConnection;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * LoginLogic.java
 * TecFAMS – Faculty of Technology Academic Management System
 * University of Ruhuna
 *
 * Role: Business & Authentication Logic (Controller / Service layer)
 * 
 * Responsibilities:
 * - Admin, Lecturer, Technical Officer, and Undergraduate credential validation using database details
 * - User input validation (empty fields, role selection)
 * - MySQL + JDBC database authentication & SQL file fallback database support
 * - Dashboard navigation dispatching
 */
public class LoginLogic {

    // =========================================================================
    // TEMPORARY / MOCK TEST CREDENTIALS (FALLBACK)
    // =========================================================================
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    private static final String LECTURER_USERNAME = "lecturer1";
    private static final String LECTURER_PASSWORD = "lecturer123";

    private static final String OFFICER_USERNAME = "officer1";
    private static final String OFFICER_PASSWORD = "officer123";

    private static final String STUDENT_USERNAME = "student1";
    private static final String STUDENT_PASSWORD = "student123";

    /**
     * Inner class representing the outcome of an authentication attempt.
     * Contains success status, user-friendly message, and authenticated role.
     */
    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final String role;

        public AuthResult(boolean success, String message, String role) {
            this.success = success;
            this.message = message;
            this.role = role;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getRole() {
            return role;
        }
    }

    /**
     * Authenticates credentials by checking against user details stored in the database.
     * First attempts MySQL live database connection via JDBC; if unavailable, checks user records from tecfams_db.sql file.
     *
     * @param username Entered username string
     * @param password Entered password string
     * @return AuthResult object indicating success/failure, message, and detected role
     */
    public static AuthResult authenticate(String username, String password) {

        // 1. Username Validation
        if (username == null || username.trim().isEmpty()) {
            return new AuthResult(false, "Please enter your username.", null);
        }

        // 2. Password Validation
        if (password == null || password.trim().isEmpty()) {
            return new AuthResult(false, "Please enter your password.", null);
        }

        String cleanUsername = username.trim();
        String cleanPassword = password.trim();

        // 3. Database Authentication (Attempt 1: Live MySQL Database via JDBC)
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                String sql = "SELECT user_id, password_hash, role FROM users WHERE username = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, cleanUsername);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            int userId = rs.getInt("user_id");
                            String dbPassword = rs.getString("password_hash");
                            String dbRole = rs.getString("role");
                            if (cleanPassword.equals(dbPassword)) {
                                String mappedRole = DBConnection.normalizeRole(dbRole);

                                // Initialize authenticated UserSession
                                if ("Lecturer".equalsIgnoreCase(mappedRole)) {
                                    String lecSql = "SELECT l.lecturer_name, l.designation, l.email, d.department_name " +
                                                    "FROM lecturers l " +
                                                    "LEFT JOIN departments d ON l.department_id = d.department_id " +
                                                    "WHERE l.lecturer_id = ?";
                                    try (PreparedStatement lecStmt = conn.prepareStatement(lecSql)) {
                                        lecStmt.setInt(1, userId);
                                        try (ResultSet lecRs = lecStmt.executeQuery()) {
                                            if (lecRs.next()) {
                                                UserSession.setCurrentSession(new UserSession(
                                                        userId, cleanUsername, mappedRole,
                                                        lecRs.getString("lecturer_name"),
                                                        lecRs.getString("designation"),
                                                        lecRs.getString("department_name"),
                                                        lecRs.getString("email")
                                                ));
                                            } else {
                                                UserSession.setCurrentSession(new UserSession(
                                                        userId, cleanUsername, mappedRole,
                                                        cleanUsername, "", "Faculty of Technology", ""
                                                ));
                                            }
                                        }
                                    }
                                } else {
                                    UserSession.setCurrentSession(new UserSession(
                                            userId, cleanUsername, mappedRole,
                                            cleanUsername, "", "Faculty of Technology", ""
                                    ));
                                }

                                return new AuthResult(true, "Authentication successful!", mappedRole);
                            } else {
                                return new AuthResult(false, "Invalid username or password.", null);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Database JDBC authentication check error: " + e.getMessage());
        }

        // 4. Database Authentication (Attempt 2: Check user details in tecfams_db.sql database file)
        DBConnection.UserRecord sqlUser = DBConnection.getSqlDumpUser(cleanUsername);
        if (sqlUser != null) {
            if (cleanPassword.equals(sqlUser.getPasswordHash())) {
                String mappedRole = DBConnection.normalizeRole(sqlUser.getRole());
                return new AuthResult(true, "Authentication successful!", mappedRole);
            } else {
                return new AuthResult(false, "Invalid username or password.", null);
            }
        }

        // 5. Fallback Mock Test Credentials
        if (ADMIN_USERNAME.equals(cleanUsername) && ADMIN_PASSWORD.equals(cleanPassword)) {
            return new AuthResult(true, "Admin authentication successful!", "Admin");
        }

        if (LECTURER_USERNAME.equals(cleanUsername) && LECTURER_PASSWORD.equals(cleanPassword)) {
            return new AuthResult(true, "Lecturer authentication successful!", "Lecturer");
        }

        if (OFFICER_USERNAME.equals(cleanUsername) && OFFICER_PASSWORD.equals(cleanPassword)) {
            return new AuthResult(true, "Technical Officer authentication successful!", "Technical Officer");
        }

        if (STUDENT_USERNAME.equals(cleanUsername) && STUDENT_PASSWORD.equals(cleanPassword)) {
            return new AuthResult(true, "Undergraduate authentication successful!", "Undergraduate");
        }

        // Credentials did not match any registered user in database
        return new AuthResult(false, "Invalid username or password.", null);
    }

    /**
     * Overloaded authentication entry point for backwards compatibility.
     */
    public static AuthResult authenticate(String username, String password, String selectedRole) {
        return authenticate(username, password);
    }

    /**
     * Handles navigation after successful login based on user role.
     * Closes the login UI frame and invokes the relevant dashboard launcher.
     *
     * @param role       Authenticated role
     * @param loginFrame Reference to current LoginUI frame
     */
    public static void navigateToDashboard(String role, JFrame loginFrame) {
        if (loginFrame != null) {
            loginFrame.dispose(); // Close the login window
        }

        if (role == null) return;

        switch (role) {
            case "Admin":
                openAdminDashboard();
                break;
            case "Lecturer":
                openLecturerDashboard();
                break;
            case "Technical Officer":
                openTechnicalOfficerDashboard();
                break;
            case "Undergraduate":
                openUndergraduateDashboard();
                break;
            default:
                JOptionPane.showMessageDialog(null, 
                    "Unknown role. Unable to open dashboard.", 
                    "System Error", 
                    JOptionPane.ERROR_MESSAGE);
                break;
        }
    }

    // =========================================================================
    // DASHBOARD PLACEHOLDER METHODS
    // Future classes: AdminDashboard, LecturerDashboard, TechnicalOfficerDashboard, UndergraduateDashboard
    // =========================================================================

    public static void openAdminDashboard() {
        // Placeholder until AdminDashboard.java is created
        JOptionPane.showMessageDialog(null,
                "✓ Login Successful!\n\nRole: Admin\nWelcome to TecFAMS Admin Portal.\n\n[AdminDashboard will be connected here]",
                "TecFAMS - Admin Portal",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void openLecturerDashboard() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TecFAMS - Faculty of Technology Academic Management System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 720);
            frame.setLayout(new java.awt.BorderLayout());

            UserSession session = UserSession.getCurrentSession();
            String name = (session != null) ? session.getFullName() : "Dr. Chinthaka Premachandra";
            String initials = getInitialsFromName(name);

            main.java.ui.Lecturer.Navbar navbar = new main.java.ui.Lecturer.Navbar(name, initials, main.java.ui.Lecturer.Navbar.PAGE_DASHBOARD);
            main.java.ui.Lecturer.LecturerDashboard dashboard = new main.java.ui.Lecturer.LecturerDashboard();

            frame.add(navbar, java.awt.BorderLayout.NORTH);
            frame.add(dashboard, java.awt.BorderLayout.CENTER);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static String getInitialsFromName(String name) {
        if (name == null || name.trim().isEmpty()) return "CP";
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty() && !p.endsWith(".")) {
                sb.append(Character.toUpperCase(p.charAt(0)));
            }
        }
        return sb.length() >= 2 ? sb.substring(0, 2) : (sb.length() == 1 ? sb.toString() : "CP");
    }

    public static void openTechnicalOfficerDashboard() {
        // Placeholder until TechnicalOfficerDashboard.java is created
        JOptionPane.showMessageDialog(null,
                "✓ Login Successful!\n\nRole: Technical Officer\nWelcome to TecFAMS Resource Portal.\n\n[TechnicalOfficerDashboard will be connected here]",
                "TecFAMS - Technical Officer Portal",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void openUndergraduateDashboard() {
        // Placeholder until UndergraduateDashboard.java is created
        JOptionPane.showMessageDialog(null,
                "✓ Login Successful!\n\nRole: Undergraduate\nWelcome to TecFAMS Student Portal.\n\n[UndergraduateDashboard will be connected here]",
                "TecFAMS - Student Portal",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
