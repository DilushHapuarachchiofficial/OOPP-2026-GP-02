package main.java.model;

import main.java.dao.DBConnection;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


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
                                } else if ("Admin".equalsIgnoreCase(mappedRole)) {
                                    String admSql = "SELECT name, email, phone FROM admins WHERE admin_id = ?";
                                    try (PreparedStatement admStmt = conn.prepareStatement(admSql)) {
                                        admStmt.setInt(1, userId);
                                        try (ResultSet admRs = admStmt.executeQuery()) {
                                            if (admRs.next()) {
                                                UserSession.setCurrentSession(new UserSession(
                                                        userId, cleanUsername, mappedRole,
                                                        admRs.getString("name"),
                                                        "Administrator",
                                                        "Faculty of Technology",
                                                        admRs.getString("email")
                                                ));
                                            } else {
                                                UserSession.setCurrentSession(new UserSession(
                                                        userId, cleanUsername, mappedRole,
                                                        cleanUsername, "Administrator", "Faculty of Technology", ""
                                                ));
                                            }
                                        }
                                    }
                                } else if ("Undergraduate".equalsIgnoreCase(mappedRole) || "Student".equalsIgnoreCase(mappedRole)) {
                                    String stuSql = "SELECT s.full_name, s.email, d.department_name " +
                                                    "FROM students s " +
                                                    "LEFT JOIN departments d ON s.department_id = d.department_id " +
                                                    "WHERE s.student_id = ? OR s.student_reg_no = ?";
                                    try (PreparedStatement stuStmt = conn.prepareStatement(stuSql)) {
                                        stuStmt.setInt(1, userId);
                                        stuStmt.setString(2, cleanUsername);
                                        try (ResultSet stuRs = stuStmt.executeQuery()) {
                                            if (stuRs.next()) {
                                                UserSession.setCurrentSession(new UserSession(
                                                        userId, cleanUsername, mappedRole,
                                                        stuRs.getString("full_name"),
                                                        "Undergraduate Student",
                                                        stuRs.getString("department_name") != null ? stuRs.getString("department_name") : "Faculty of Technology",
                                                        stuRs.getString("email")
                                                ));
                                            } else {
                                                UserSession.setCurrentSession(new UserSession(
                                                        userId, cleanUsername, mappedRole,
                                                        cleanUsername, "Undergraduate Student", "Faculty of Technology", ""
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
                int inferredId = 1;
                String displayName = cleanUsername;
                if ("Lecturer".equalsIgnoreCase(mappedRole)) {
                    inferredId = 4;
                    displayName = "Dr. Chinthaka Premachandra";
                }
                UserSession.setCurrentSession(new UserSession(
                        inferredId, cleanUsername, mappedRole,
                        displayName, mappedRole, "Faculty of Technology", ""
                ));
                return new AuthResult(true, "Authentication successful!", mappedRole);
            } else {
                return new AuthResult(false, "Invalid username or password.", null);
            }
        }

        // 5. Fallback Mock Test Credentials
        if (ADMIN_USERNAME.equals(cleanUsername) && ADMIN_PASSWORD.equals(cleanPassword)) {
            UserSession.setCurrentSession(new UserSession(
                    1, cleanUsername, "Admin",
                    "System Administrator", "Admin", "Faculty of Technology", "admin@fot.ruh.ac.lk"
            ));
            return new AuthResult(true, "Admin authentication successful!", "Admin");
        }

        if (LECTURER_USERNAME.equals(cleanUsername) && LECTURER_PASSWORD.equals(cleanPassword)) {
            UserSession.setCurrentSession(new UserSession(
                    4, cleanUsername, "Lecturer",
                    "Dr. Chinthaka Premachandra", "Senior Lecturer Gr. I",
                    "Department of Information and Communication Technology", "chinthaka@fot.ruh.ac.lk"
            ));
            return new AuthResult(true, "Lecturer authentication successful!", "Lecturer");
        }

        if (OFFICER_USERNAME.equals(cleanUsername) && OFFICER_PASSWORD.equals(cleanPassword)) {
            UserSession.setCurrentSession(new UserSession(
                    12, cleanUsername, "Technical Officer",
                    "Mr. Kamal Perera", "Technical Officer", "Faculty of Technology", "kamal@fot.ruh.ac.lk"
            ));
            return new AuthResult(true, "Technical Officer authentication successful!", "Technical Officer");
        }

        if (STUDENT_USERNAME.equals(cleanUsername) && STUDENT_PASSWORD.equals(cleanPassword)) {
            UserSession.setCurrentSession(new UserSession(
                    18, cleanUsername, "Undergraduate",
                    "Kavishka Perera", "Undergraduate",
                    "Department of Information and Communication Technology", "tg2062@fot.ruh.ac.lk"
            ));
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
        String normalizedRole = DBConnection.normalizeRole(role);

        switch (normalizedRole) {
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
                    "Unknown role: " + role + ". Unable to open dashboard.", 
                    "System Error", 
                    JOptionPane.ERROR_MESSAGE);
                break;
        }
    }

    // =========================================================================
    // DASHBOARD LAUNCHER & DYNAMIC LINKING METHODS
    // Future classes created by group members: AdminDashboard, TechnicalOfficerDashboard, UndergraduateDashboard
    // =========================================================================

    public static void openAdminDashboard() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            UserSession session = UserSession.getCurrentSession();
            int adminId = (session != null) ? session.getUserId() : -1;

            if (adminId > 0) {
                try {
                    Class<?> clazz = Class.forName("main.java.ui.admin.AdminDashboard");
                    java.lang.reflect.Constructor<?> ctor = clazz.getConstructor(int.class);
                    JFrame frame = (JFrame) ctor.newInstance(adminId);
                    frame.setVisible(true);
                    return;
                } catch (Exception ignored) {}
            }

            boolean opened = tryOpenDashboard(
                "main.java.ui.admin.AdminDashboard",
                "main.java.ui.Admin.AdminDashboard",
                "ui.admin.AdminDashboard",
                "ui.Admin.AdminDashboard",
                "main.java.ui.AdminDashboard",
                "ui.AdminDashboard",
                "main.java.ui.admin.AdminUI",
                "main.java.ui.Admin.AdminUI",
                "ui.admin.AdminUI",
                "ui.AdminUI"
            );
            if (!opened) {
                JOptionPane.showMessageDialog(null,
                        "✓ Login Successful!\n\nRole: Admin\nWelcome to TecFAMS Admin Portal.\n\n[AdminDashboard UI class will open here once created by your team member]",
                        "TecFAMS - Admin Portal",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public static void openLecturerDashboard() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TecFAMS");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 720);
            frame.setLayout(new java.awt.BorderLayout());

            UserSession session = UserSession.getCurrentSession();
            String name = (session != null) ? session.getFullName() : "Dr. Chinthaka Premachandra";
            String initials = getInitialsFromName(name);

            main.java.ui.Lecturer.Navbar navbar = new main.java.ui.Lecturer.Navbar(name, initials, main.java.ui.Lecturer.Navbar.PAGE_DASHBOARD);

            // Central CardLayout content panel
            java.awt.CardLayout contentCards = new java.awt.CardLayout();
            javax.swing.JPanel mainContent = new javax.swing.JPanel(contentCards);

            // 1. Live Database Lecturer Dashboard
            main.java.ui.Lecturer.LecturerDashboard dashboard = new main.java.ui.Lecturer.LecturerDashboard();
            mainContent.add(dashboard, main.java.ui.Lecturer.Navbar.PAGE_DASHBOARD);

            // 2. Course Materials Module
            main.java.ui.Lecturer.CourseMaterials materials = new main.java.ui.Lecturer.CourseMaterials(navbar, contentCards, mainContent);
            mainContent.add(materials, main.java.ui.Lecturer.Navbar.PAGE_MATERIALS);

            // 3. Student Details & Eligibility Module
            main.java.ui.Lecturer.StudentDetailsEligibility eligibility = new main.java.ui.Lecturer.StudentDetailsEligibility(navbar, contentCards, mainContent);
            mainContent.add(eligibility, main.java.ui.Lecturer.Navbar.PAGE_ELIGIBILITY);

            // 4. Live Marks Management Module
            main.java.ui.Lecturer.LecturerMarksManagement marksManagement = new main.java.ui.Lecturer.LecturerMarksManagement(navbar, contentCards, mainContent);
            mainContent.add(marksManagement, main.java.ui.Lecturer.Navbar.PAGE_MARKS);

            main.java.ui.Lecturer.LecturerProfile profile = new main.java.ui.Lecturer.LecturerProfile(navbar, contentCards, mainContent);
            mainContent.add(profile, main.java.ui.Lecturer.Navbar.PAGE_PROFILE);

            // Wire Navigation Listener: Smooth switching in same window without duplicate frames
            navbar.addNavigationListener(pageName -> {
                contentCards.show(mainContent, pageName);
                if (main.java.ui.Lecturer.Navbar.PAGE_DASHBOARD.equals(pageName)) {
                    dashboard.refreshDashboard();
                } else if (main.java.ui.Lecturer.Navbar.PAGE_MATERIALS.equals(pageName)) {
                    materials.refreshMaterials();
                } else if (main.java.ui.Lecturer.Navbar.PAGE_ELIGIBILITY.equals(pageName)) {
                    eligibility.refreshData();
                } else if (main.java.ui.Lecturer.Navbar.PAGE_MARKS.equals(pageName)) {
                    marksManagement.refreshAllData();
                } else if (main.java.ui.Lecturer.Navbar.PAGE_PROFILE.equals(pageName)) {
                    profile.loadLecturerProfile();
                }
            });

            // Wire Logout Listener
            navbar.addLogoutListener(UserSession::clearSession);

            frame.add(navbar, java.awt.BorderLayout.NORTH);
            frame.add(mainContent, java.awt.BorderLayout.CENTER);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static javax.swing.JPanel createLecturerModulePlaceholder(
            String title, String description,
            main.java.ui.Lecturer.Navbar navbar,
            java.awt.CardLayout cardLayout,
            javax.swing.JPanel container) {

        javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.GridBagLayout());
        panel.setBackground(new java.awt.Color(244, 247, 251)); // #F4F7FB

        javax.swing.JPanel card = new javax.swing.JPanel();
        card.setLayout(new javax.swing.BoxLayout(card, javax.swing.BoxLayout.Y_AXIS));
        card.setBackground(java.awt.Color.WHITE);
        card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(220, 228, 238), 1),
                javax.swing.BorderFactory.createEmptyBorder(36, 44, 36, 44)
        ));
        card.setPreferredSize(new java.awt.Dimension(580, 260));

        javax.swing.JLabel titleLbl = new javax.swing.JLabel(title);
        titleLbl.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20));
        titleLbl.setForeground(new java.awt.Color(8, 47, 90));
        titleLbl.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        javax.swing.JLabel descLbl = new javax.swing.JLabel("<html><center style='width:450px;'>" + description + "</center></html>");
        descLbl.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        descLbl.setForeground(new java.awt.Color(100, 116, 139));
        descLbl.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        javax.swing.JLabel statusBadge = new javax.swing.JLabel("Module Under Active Development");
        statusBadge.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 11));
        statusBadge.setForeground(new java.awt.Color(11, 79, 156));
        statusBadge.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        javax.swing.JButton returnBtn = new javax.swing.JButton("← Return to Dashboard");
        returnBtn.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 13));
        returnBtn.setForeground(java.awt.Color.WHITE);
        returnBtn.setBackground(new java.awt.Color(11, 79, 156));
        returnBtn.setFocusPainted(false);
        returnBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 10, 20));
        returnBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        returnBtn.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        returnBtn.addActionListener(e -> {
            navbar.setActivePage(main.java.ui.Lecturer.Navbar.PAGE_DASHBOARD);
            cardLayout.show(container, main.java.ui.Lecturer.Navbar.PAGE_DASHBOARD);
        });

        card.add(titleLbl);
        card.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(0, 10)));
        card.add(descLbl);
        card.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(0, 14)));
        card.add(statusBadge);
        card.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(0, 22)));
        card.add(returnBtn);

        panel.add(card);
        return panel;
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
        javax.swing.SwingUtilities.invokeLater(() -> {
            boolean opened = tryOpenDashboard(
                "main.java.ui.TechnicalOfficer.TechnicalOfficerDashboard",
                "main.java.ui.technicalofficer.TechnicalOfficerDashboard",
                "ui.TechnicalOfficer.TechnicalOfficerDashboard",
                "ui.technicalofficer.TechnicalOfficerDashboard",
                "main.java.ui.TechnicalOfficerDashboard",
                "ui.TechnicalOfficerDashboard",
                "main.java.ui.TechnicalOfficer.TechnicalOfficerUI",
                "main.java.ui.technicalofficer.TechnicalOfficerUI",
                "ui.TechnicalOfficer.TechnicalOfficerUI",
                "ui.technicalofficer.TechnicalOfficerUI",
                "main.java.ui.to.TODashboard",
                "ui.to.TODashboard"
            );
            if (!opened) {
                JOptionPane.showMessageDialog(null,
                        "✓ Login Successful!\n\nRole: Technical Officer\nWelcome to TecFAMS Resource Portal.\n\n[TechnicalOfficerDashboard UI class will open here once created by your team member]",
                        "TecFAMS - Technical Officer Portal",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public static void openUndergraduateDashboard() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            UserSession session = UserSession.getCurrentSession();
            main.java.model.Undergraduate.Undergraduate student = null;
            main.java.dao.Undergraduate.UndergraduateDAO dao = new main.java.dao.Undergraduate.UndergraduateDAO();

            if (session != null) {
                if (session.getUsername() != null && !session.getUsername().trim().isEmpty()) {
                    student = dao.getUndergraduateByStudentId(session.getUsername().trim());
                }
                if (student == null && session.getUserId() > 0) {
                    student = dao.getUndergraduateByStudentId(String.valueOf(session.getUserId()));
                }
                if (student == null) {
                    String regNo = session.getUsername() != null ? session.getUsername().trim() : "TG/2024/2083";
                    String fullName = session.getFullName() != null && !session.getFullName().isEmpty()
                            ? session.getFullName() : session.getUsername();
                    String email = session.getEmail() != null ? session.getEmail() : "";
                    String dept = session.getDepartmentName() != null && !session.getDepartmentName().isEmpty()
                            ? session.getDepartmentName() : "Department of Information and Communication Technology";
                    student = new main.java.model.Undergraduate.Undergraduate(
                            session.getUserId() > 0 ? session.getUserId() : 19,
                            session.getUsername() != null ? session.getUsername() : "student",
                            "",
                            fullName,
                            email,
                            "0710001000",
                            "default_avatar.png",
                            String.valueOf(session.getUserId() > 0 ? session.getUserId() : 19),
                            regNo,
                            "Batch 2023/2024",
                            dept,
                            2024
                    );
                }
            }

            if (student != null) {
                try {
                    main.java.ui.Undergraduate.UndergraduateDashboard dashboard =
                            new main.java.ui.Undergraduate.UndergraduateDashboard(student);
                    dashboard.setVisible(true);
                    return;
                } catch (Exception e) {
                    System.err.println("Error opening UndergraduateDashboard with student instance: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            boolean opened = tryOpenDashboard(
                "main.java.ui.Undergraduate.UndergraduateDashboard",
                "ui.Undergraduate.UndergraduateDashboard"
            );
            if (!opened) {
                JOptionPane.showMessageDialog(null,
                        "✓ Login Successful!\n\nRole: Undergraduate\nWelcome to TecFAMS Student Portal.",
                        "TecFAMS - Student Portal",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    /**
     * Attempts to dynamically discover and instantiate a Dashboard Window/Frame by checking class names.
     * Keeps modules decoupled so the project compiles and runs smoothly even before group members merge their code.
     *
     * @param candidateClassNames Possible fully qualified class names for the dashboard
     * @return true if a dashboard class was found, instantiated, and made visible; false otherwise.
     */
    private static boolean tryOpenDashboard(String... candidateClassNames) {
        for (String className : candidateClassNames) {
            try {
                Class<?> clazz = Class.forName(className);
                Object instance = null;

                // 1. Try default no-arg constructor
                try {
                    java.lang.reflect.Constructor<?> defaultCtor = clazz.getDeclaredConstructor();
                    defaultCtor.setAccessible(true);
                    instance = defaultCtor.newInstance();
                } catch (NoSuchMethodException e) {
                    // 3. Try looking for a single-argument constructor (e.g. passing null or student object)
                    for (java.lang.reflect.Constructor<?> ctor : clazz.getDeclaredConstructors()) {
                        if (ctor.getParameterCount() == 1) {
                            try {
                                ctor.setAccessible(true);
                                instance = ctor.newInstance(new Object[]{null});
                                break;
                            } catch (Exception ignored) {}
                        }
                    }
                }

                if (instance != null) {
                    if (instance instanceof JFrame) {
                        ((JFrame) instance).setVisible(true);
                        return true;
                    } else if (instance instanceof java.awt.Window) {
                        ((java.awt.Window) instance).setVisible(true);
                        return true;
                    } else if (instance instanceof javax.swing.JComponent) {
                        // If their dashboard is a JPanel or JComponent, wrap in a JFrame
                        JFrame frame = new JFrame("TecFAMS");
                        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        frame.setSize(1100, 720);
                        frame.setLocationRelativeTo(null);
                        frame.add((javax.swing.JComponent) instance);
                        frame.setVisible(true);
                        return true;
                    } else {
                        // In case it's a helper with a setVisible or display method
                        try {
                            java.lang.reflect.Method setVis = clazz.getMethod("setVisible", boolean.class);
                            setVis.invoke(instance, true);
                            return true;
                        } catch (Exception ignored) {}
                    }
                }
            } catch (ClassNotFoundException ignored) {
                // Class not in current branch, try next candidate
            } catch (Exception e) {
                System.err.println("Error instantiating dashboard class " + className + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }
}
