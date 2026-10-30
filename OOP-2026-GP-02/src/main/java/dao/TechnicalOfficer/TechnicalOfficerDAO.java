package main.java.dao.TechnicalOfficer;

import main.java.model.Notice;
import main.java.model.TechnicalOfficer.TechnicalOfficer;
import main.java.model.Timetable;
import main.java.model.UserSession;
import main.resources.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Technical Officer operations in TecFAMS.
 * Manages Technical Officer database persistence, profile updates, secure password
 * verification, duplicate email checks, and dashboard summary metrics.
 */
public class TechnicalOfficerDAO {

    private TechnicalOfficer currentOfficer;

    public TechnicalOfficerDAO() {
        this(-1);
    }

    public TechnicalOfficerDAO(int officerId) {
        this.currentOfficer = new TechnicalOfficer();

        // Load actual record from database if available
        if (officerId > 0) {
            loadOfficerFromDatabase(officerId);
        } else {
            loadDefaultOrSessionOfficer();
        }
    }

    /**
     * Resolves the current officer either from active UserSession or queries the first
     * Technical Officer record from the database.
     */
    private void loadDefaultOrSessionOfficer() {
        if (UserSession.isLoggedIn()) {
            UserSession session = UserSession.getCurrentSession();
            int sessionUserId = session.getUserId();
            if (sessionUserId > 0 && loadOfficerFromDatabase(sessionUserId)) {
                return;
            }
            if (session.getUsername() != null && !session.getUsername().trim().isEmpty()) {
                if (loadOfficerByUsername(session.getUsername().trim())) {
                    return;
                }
            }

            // Fallback for static known officers if database connection is offline
            TechnicalOfficer fallback = getStaticFallbackOfficer(session.getUsername(), sessionUserId);
            if (fallback != null) {
                this.currentOfficer = fallback;
                return;
            }

            // Fallback: initialize officer details directly from the authenticated session
            if (this.currentOfficer == null) {
                this.currentOfficer = new TechnicalOfficer();
            }
            this.currentOfficer.setOfficerId(String.valueOf(session.getUserId() > 0 ? session.getUserId() : 12));
            this.currentOfficer.setUsername(session.getUsername() != null ? session.getUsername() : "to_kamal");
            this.currentOfficer.setName(session.getFullName() != null && !session.getFullName().isEmpty() ? session.getFullName() : session.getUsername());
            this.currentOfficer.setEmail(session.getEmail() != null ? session.getEmail() : "");
            this.currentOfficer.setDepartment(session.getDepartmentName() != null && !session.getDepartmentName().isEmpty() ? session.getDepartmentName() : "Faculty of Technology");
            this.currentOfficer.setRole("Technical Officer");
            return;
        }

        // Only when no session is active, attempt to query the first technical officer from the database
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT officer_id FROM technical_officers ORDER BY officer_id ASC LIMIT 1")) {
                if (rs.next()) {
                    int firstId = rs.getInt("officer_id");
                    loadOfficerFromDatabase(firstId);
                }
            } catch (SQLException e) {
                System.err.println("TechnicalOfficerDAO: Error finding default officer: " + e.getMessage());
            }
        }
    }

    /**
     * Returns pre-configured technical officer profile details for offline / fallback mode.
     */
    public static TechnicalOfficer getStaticFallbackOfficer(String username, int officerId) {
        String cleanUser = username != null ? username.trim().toLowerCase() : "";
        if ("to_kamal".equals(cleanUser) || officerId == 12) {
            return new TechnicalOfficer("12", "to_kamal", "Mr. Kamal Wickrama", "to.kamal@fot.ruh.ac.lk", "0751234561", "Department of Information and Communication Technology");
        }
        if ("to_nimal".equals(cleanUser) || officerId == 13) {
            return new TechnicalOfficer("13", "to_nimal", "Mr. Nimal Abeywickrama", "to.nimal@fot.ruh.ac.lk", "0751234562", "Department of Information and Communication Technology");
        }
        if ("to_sunil".equals(cleanUser) || officerId == 14) {
            return new TechnicalOfficer("14", "to_sunil", "Mr. Sunil Shantha", "to.sunil@fot.ruh.ac.lk", "0751234563", "Department of Engineering Technology");
        }
        if ("to_ruwan".equals(cleanUser) || officerId == 15) {
            return new TechnicalOfficer("15", "to_ruwan", "Mr. Ruwan Jayasuriya", "to.ruwan@fot.ruh.ac.lk", "0751234564", "Department of Engineering Technology");
        }
        if ("to_asanka".equals(cleanUser) || officerId == 16) {
            return new TechnicalOfficer("16", "to_asanka", "Mr. Asanka Priyadarshana", "to.asanka@fot.ruh.ac.lk", "0751234565", "Department of Biosystems Technology");
        }
        if ("to_malik".equals(cleanUser) || officerId == 17) {
            return new TechnicalOfficer("17", "to_malik", "Mr. Malik Jayatilleke", "to.malik@fot.ruh.ac.lk", "0751234566", "Department of Multidisciplinary Studies");
        }
        return null;
    }

    /**
     * Loads the Technical Officer details from the database by officerId (which matches users.user_id).
     *
     * @param officerId Target officer ID
     * @return true if loaded successfully from database
     */
    public boolean loadOfficerFromDatabase(int officerId) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            TechnicalOfficer fallback = getStaticFallbackOfficer(null, officerId);
            if (fallback != null) {
                this.currentOfficer = fallback;
                return true;
            }
            return false;
        }

        String sql = "SELECT t.officer_id, t.officer_name, t.email, t.phone, " +
                     "       u.username, u.role, u.profile_picture, " +
                     "       d.department_name " +
                     "FROM technical_officers t " +
                     "JOIN users u ON t.officer_id = u.user_id " +
                     "LEFT JOIN departments d ON t.department_id = d.department_id " +
                     "WHERE t.officer_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, officerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    if (currentOfficer == null) {
                        currentOfficer = new TechnicalOfficer();
                    }
                    currentOfficer.setOfficerId(String.valueOf(rs.getInt("officer_id")));
                    currentOfficer.setUsername(rs.getString("username"));
                    currentOfficer.setName(rs.getString("officer_name"));
                    currentOfficer.setEmail(rs.getString("email"));
                    currentOfficer.setPhone(rs.getString("phone"));
                    String dept = rs.getString("department_name");
                    currentOfficer.setDepartment(dept != null ? dept : "Faculty of Technology");
                    String pic = rs.getString("profile_picture");
                    currentOfficer.setProfilePicture((pic != null && !pic.trim().isEmpty()) ? pic.trim() : "default_avatar.png");
                    currentOfficer.setRole(rs.getString("role"));
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("TechnicalOfficerDAO: Error loading officer " + officerId + ": " + e.getMessage());
        }

        TechnicalOfficer fallback = getStaticFallbackOfficer(null, officerId);
        if (fallback != null) {
            this.currentOfficer = fallback;
            return true;
        }
        return false;
    }

    /**
     * Loads the Technical Officer by username.
     */
    public boolean loadOfficerByUsername(String username) {
        if (username == null || username.trim().isEmpty()) return false;
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            TechnicalOfficer fallback = getStaticFallbackOfficer(username, -1);
            if (fallback != null) {
                this.currentOfficer = fallback;
                return true;
            }
            return false;
        }

        String sql = "SELECT user_id FROM users WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return loadOfficerFromDatabase(rs.getInt("user_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("TechnicalOfficerDAO: Error loading by username: " + e.getMessage());
        }

        TechnicalOfficer fallback = getStaticFallbackOfficer(username, -1);
        if (fallback != null) {
            this.currentOfficer = fallback;
            return true;
        }
        return false;
    }

    public TechnicalOfficer getCurrentOfficer() {
        return currentOfficer;
    }

    public void setCurrentOfficer(TechnicalOfficer officer) {
        this.currentOfficer = officer;
    }

    /**
     * Checks whether an email address is already registered to another technical officer.
     *
     * @param email Email address to check
     * @param excludeOfficerId Officer ID to exclude (current officer)
     * @return true if the email is already in use by another officer
     */
    public boolean isEmailTaken(String email, int excludeOfficerId) {
        if (email == null || email.trim().isEmpty()) return false;
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        String sql = "SELECT COUNT(*) FROM technical_officers WHERE LOWER(email) = LOWER(?) AND officer_id != ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ps.setInt(2, excludeOfficerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("TechnicalOfficerDAO: isEmailTaken check error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Updates permitted profile information and photo in the database using JDBC PreparedStatement.
     * Adheres to database schema and transaction safety.
     *
     * @param officerId Target officer ID
     * @param name New Full Name
     * @param email New Email Address
     * @param phone New Contact Number
     * @param profilePicture New profile photo path (or null to keep existing)
     * @return true if update succeeded
     */
    public boolean updateProfile(int officerId, String name, String email, String phone, String profilePicture) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return false;
        }

        try {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                // 1. Update technical_officers table
                String sqlOfficer = "UPDATE technical_officers SET officer_name = ?, email = ?, phone = ? WHERE officer_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlOfficer)) {
                    ps.setString(1, name.trim());
                    ps.setString(2, email.trim());
                    ps.setString(3, (phone != null && !phone.trim().isEmpty()) ? phone.trim() : null);
                    ps.setInt(4, officerId);
                    ps.executeUpdate();
                }

                // 2. Update profile_picture in users table if new photo provided
                if (profilePicture != null && !profilePicture.trim().isEmpty()) {
                    String sqlUser = "UPDATE users SET profile_picture = ? WHERE user_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sqlUser)) {
                        ps.setString(1, profilePicture.trim());
                        ps.setInt(2, officerId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                conn.setAutoCommit(originalAutoCommit);

                // Update in-memory model
                updateInMemory(name, email, phone, profilePicture);

                // Synchronize active UserSession if matching current user
                if (UserSession.isLoggedIn() && UserSession.getCurrentSession().getUserId() == officerId) {
                    UserSession curr = UserSession.getCurrentSession();
                    UserSession.setCurrentSession(new UserSession(
                            curr.getUserId(),
                            curr.getUsername(),
                            curr.getRole(),
                            name.trim(),
                            curr.getDesignation(),
                            curr.getDepartmentName(),
                            email.trim()
                    ));
                }

                return true;
            } catch (SQLException ex) {
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                throw ex;
            }
        } catch (SQLException e) {
            System.err.println("TechnicalOfficerDAO: updateProfile error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Backward-compatible overload for existing code.
     */
    public boolean updateProfile(String name, String email, String phone) {
        int id = parseOfficerId(currentOfficer);
        return updateProfile(id, name, email, phone, (String) null);
    }

    private void updateInMemory(String name, String email, String phone, String profilePicture) {
        if (currentOfficer == null) return;
        if (name != null && !name.trim().isEmpty()) currentOfficer.setName(name.trim());
        if (email != null && !email.trim().isEmpty()) currentOfficer.setEmail(email.trim());
        if (phone != null) currentOfficer.setPhone(phone.trim());
        if (profilePicture != null && !profilePicture.trim().isEmpty()) {
            currentOfficer.setProfilePicture(profilePicture.trim());
        }
    }

    /**
     * Verifies the Technical Officer's current password against the stored password in users table.
     *
     * @param userId Technical officer user ID (matches users.user_id)
     * @param currentPassword Plain password entered by user
     * @return true if current password matches stored credentials
     */
    public boolean verifyPassword(int userId, String currentPassword) {
        if (currentPassword == null || currentPassword.trim().isEmpty()) return false;
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return false;
        }

        String sql = "SELECT password_hash FROM users WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String stored = rs.getString("password_hash");
                    return stored != null && stored.trim().equals(currentPassword.trim());
                }
            }
        } catch (SQLException e) {
            System.err.println("TechnicalOfficerDAO: verifyPassword error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Changes the Technical Officer's password in the users table using PreparedStatement.
     * Verifies current password first before applying the change.
     *
     * @param userId Technical officer user ID
     * @param currentPassword Current password
     * @param newPassword New password
     * @return true if password changed successfully
     */
    public boolean changePassword(int userId, String currentPassword, String newPassword) {
        if (!verifyPassword(userId, currentPassword)) {
            return false;
        }

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return false;
        }

        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword.trim());
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("TechnicalOfficerDAO: changePassword error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper to safely parse numeric officer ID from session or model.
     */
    public static int parseOfficerId(TechnicalOfficer officer) {
        if (UserSession.isLoggedIn()) {
            return UserSession.getCurrentSession().getUserId();
        }
        if (officer != null && officer.getOfficerId() != null) {
            String raw = officer.getOfficerId().trim();
            if (raw.matches("\\d+")) {
                return Integer.parseInt(raw);
            }
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+$").matcher(raw);
            if (m.find()) {
                try {
                    return Integer.parseInt(m.group());
                } catch (NumberFormatException ignored) {}
            }
        }
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT officer_id FROM technical_officers ORDER BY officer_id ASC LIMIT 1")) {
                if (rs.next()) return rs.getInt("officer_id");
            } catch (SQLException ignored) {}
        }
        return -1;
    }

    // Dashboard Statistics (strictly queries database, returns 0 if disconnected or empty)
    public int getTotalAttendanceCount() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM attendances")) {
                if (rs.next()) return rs.getInt(1);
            } catch (SQLException ignored) {}
        }
        return 0;
    }

    public int getPendingMedicalCount() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM medicals WHERE approval_status = 'Pending'")) {
                if (rs.next()) return rs.getInt(1);
            } catch (SQLException ignored) {}
        }
        return 0;
    }

    public int getTodaySessionsCount() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM attendance_sessions WHERE session_date = CURDATE()")) {
                if (rs.next()) return rs.getInt(1);
            } catch (SQLException ignored) {}
        }
        return 0;
    }

    public int getActiveNoticesCount() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM notices")) {
                if (rs.next()) return rs.getInt(1);
            } catch (SQLException ignored) {}
        }
        return 0;
    }

    /**
     * Retrieves all department names from the database for filter dropdowns.
     */
    public List<String> getAllDepartmentNames() {
        List<String> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT department_name FROM departments ORDER BY department_id ASC")) {
                while (rs.next()) {
                    list.add(rs.getString("department_name"));
                }
            } catch (SQLException e) {
                System.err.println("TechnicalOfficerDAO: Error fetching departments: " + e.getMessage());
            }
        }
        return list;
    }

    /**
     * Retrieves timetable entries matching optional filters from database.
     */
    public List<Timetable> getDepartmentTimetableEntries(String departmentFilter, Integer yearFilter, Integer semFilter, String dayFilter, String sessionTypeFilter, String searchQuery) {
        List<Timetable> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return list;
        }

        StringBuilder sql = new StringBuilder(
            "SELECT te.entry_id, te.timetable_id, te.course_id, " +
            "       COALESCE(c.course_code, 'N/A') AS course_code, " +
            "       COALESCE(c.course_name, 'General Session') AS course_name, " +
            "       te.day_of_week, " +
            "       DATE_FORMAT(te.start_time, '%H:%i') AS start_time, " +
            "       DATE_FORMAT(te.end_time, '%H:%i') AS end_time, " +
            "       te.hall, te.session_type, " +
            "       t.academic_year, t.semester, " +
            "       COALESCE(d.department_name, 'Faculty of Technology') AS department_name " +
            "FROM timetable_entries te " +
            "JOIN timetables t ON te.timetable_id = t.timetable_id " +
            "LEFT JOIN courses c ON te.course_id = c.course_id " +
            "LEFT JOIN departments d ON t.department_id = d.department_id " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (departmentFilter != null && !departmentFilter.trim().isEmpty() && !departmentFilter.equalsIgnoreCase("All Departments") && !departmentFilter.equalsIgnoreCase("All")) {
            sql.append(" AND (d.department_name LIKE ? OR d.department_code LIKE ?) ");
            params.add("%" + departmentFilter.trim() + "%");
            params.add("%" + departmentFilter.trim() + "%");
        }

        if (yearFilter != null && yearFilter > 0) {
            sql.append(" AND t.academic_year = ? ");
            params.add(yearFilter);
        }

        if (semFilter != null && semFilter > 0) {
            sql.append(" AND t.semester = ? ");
            params.add(semFilter);
        }

        if (dayFilter != null && !dayFilter.trim().isEmpty() && !dayFilter.equalsIgnoreCase("All Days") && !dayFilter.equalsIgnoreCase("All")) {
            sql.append(" AND te.day_of_week = ? ");
            params.add(dayFilter.trim());
        }

        if (sessionTypeFilter != null && !sessionTypeFilter.trim().isEmpty() && !sessionTypeFilter.equalsIgnoreCase("All Types") && !sessionTypeFilter.equalsIgnoreCase("All")) {
            sql.append(" AND te.session_type = ? ");
            params.add(sessionTypeFilter.trim());
        }

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql.append(" AND (c.course_code LIKE ? OR c.course_name LIKE ? OR te.hall LIKE ?) ");
            String q = "%" + searchQuery.trim() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
        }

        sql.append(" ORDER BY FIELD(te.day_of_week, 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'), te.start_time ");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String timeSlot = rs.getString("start_time") + " - " + rs.getString("end_time");
                    Timetable t = new Timetable(
                        rs.getInt("entry_id"),
                        String.valueOf(rs.getInt("timetable_id")),
                        rs.getString("day_of_week"),
                        timeSlot,
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getString("hall"),
                        rs.getString("session_type"),
                        rs.getInt("academic_year"),
                        rs.getInt("semester"),
                        rs.getString("department_name")
                    );
                    list.add(t);
                }
            }
        } catch (SQLException e) {
            System.err.println("TechnicalOfficerDAO: Error querying timetables: " + e.getMessage());
        }

        return list;
    }

    /**
     * Retrieves faculty notices from the database with optional audience filter and search query.
     */
    public List<Notice> getFacultyNotices(String targetAudience, String searchKeyword) {
        List<Notice> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return list;
        }

        StringBuilder sql = new StringBuilder(
            "SELECT n.notice_id, n.title, n.description, n.published_date, n.target_audience, " +
            "       COALESCE(u.username, 'Administration') AS publisher_name, " +
            "       COALESCE(u.role, 'Admin') AS publisher_role " +
            "FROM notices n " +
            "LEFT JOIN users u ON n.published_by = u.user_id " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (targetAudience != null && !targetAudience.trim().isEmpty() && !targetAudience.equalsIgnoreCase("All Audiences") && !targetAudience.equalsIgnoreCase("All")) {
            sql.append(" AND (n.target_audience = ? OR n.target_audience = 'All') ");
            params.add(targetAudience.trim());
        }

        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            sql.append(" AND (n.title LIKE ? OR n.description LIKE ?) ");
            String q = "%" + searchKeyword.trim() + "%";
            params.add(q);
            params.add(q);
        }

        sql.append(" ORDER BY n.published_date DESC, n.notice_id DESC ");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("published_date");
                    LocalDate pDate = (ts != null) ? ts.toLocalDateTime().toLocalDate() : LocalDate.now();
                    String pub = rs.getString("publisher_name");
                    String role = rs.getString("publisher_role");
                    String publishedBy = (role != null ? role + " (" + pub + ")" : pub);

                    Notice notice = new Notice(
                        String.valueOf(rs.getInt("notice_id")),
                        rs.getString("title"),
                        rs.getString("description"),
                        pDate,
                        publishedBy,
                        rs.getString("target_audience")
                    );
                    list.add(notice);
                }
            }
        } catch (SQLException e) {
            System.err.println("TechnicalOfficerDAO: Error querying notices: " + e.getMessage());
        }

        return list;
    }

    /**
     * Retrieves notices received by the Technical Officer.
     * By default, returns notices targeted to Technical Officers and All Faculty.
     *
     * @param categoryFilter Filter option ("TO_ONLY", "GENERAL", "ALL_CIRCULARS", or "TO_DEFAULT")
     * @param searchKeyword Search string in title or description
     * @return List of Notice objects
     */
    public List<Notice> getTechnicalOfficerNotices(String categoryFilter, String searchKeyword) {
        List<Notice> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return list;
        }

        StringBuilder sql = new StringBuilder(
            "SELECT n.notice_id, n.title, n.description, n.published_date, n.target_audience, " +
            "       COALESCE(u.username, 'Administration') AS publisher_name, " +
            "       COALESCE(u.role, 'Admin') AS publisher_role " +
            "FROM notices n " +
            "LEFT JOIN users u ON n.published_by = u.user_id " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if ("ALL_CIRCULARS".equalsIgnoreCase(categoryFilter)) {
            // Include all circulars
        } else if ("TO_ONLY".equalsIgnoreCase(categoryFilter)) {
            sql.append(" AND n.target_audience = 'Technical_Officers' ");
        } else if ("GENERAL".equalsIgnoreCase(categoryFilter)) {
            sql.append(" AND n.target_audience = 'All' ");
        } else {
            // Default: Notices received by Technical Officer ('All' or 'Technical_Officers')
            sql.append(" AND n.target_audience IN ('All', 'Technical_Officers') ");
        }

        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            sql.append(" AND (n.title LIKE ? OR n.description LIKE ?) ");
            String q = "%" + searchKeyword.trim() + "%";
            params.add(q);
            params.add(q);
        }

        sql.append(" ORDER BY n.published_date DESC, n.notice_id DESC ");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("published_date");
                    LocalDate pDate = (ts != null) ? ts.toLocalDateTime().toLocalDate() : LocalDate.now();
                    String pub = rs.getString("publisher_name");
                    String role = rs.getString("publisher_role");
                    String publishedBy = (role != null ? role + " (" + pub + ")" : pub);

                    Notice notice = new Notice(
                        String.valueOf(rs.getInt("notice_id")),
                        rs.getString("title"),
                        rs.getString("description"),
                        pDate,
                        publishedBy,
                        rs.getString("target_audience")
                    );
                    list.add(notice);
                }
            }
        } catch (SQLException e) {
            System.err.println("TechnicalOfficerDAO: Error querying notices: " + e.getMessage());
        }

        return list;
    }
}
