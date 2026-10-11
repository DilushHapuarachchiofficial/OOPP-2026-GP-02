package main.java.model.Lecturer;

import main.java.dao.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * LecturerDashboardDAO.java
 * TecFAMS – Faculty of Technology Academic Management System
 *
 * Dedicated data-access class for the Lecturer Dashboard.
 * Executes read-only parameterized JDBC queries against the TecFAMS MySQL database.
 *
 * Architecture Rules:
 * - Uses PreparedStatement for all parameterized queries.
 * - Uses try-with-resources for automatic JDBC resource management.
 * - Reuses existing DBConnection.getConnection() configuration.
 * - Zero fallback to fabricated or mock data upon connection failures.
 * - Differentiates legitimate zero counts from database query errors.
 */
public class LecturerDashboardDAO {

    /**
     * Loads the complete dashboard state for a specific authenticated lecturer ID.
     *
     * @param lecturerId The authenticated lecturer's primary key (user_id in users, lecturer_id in lecturers).
     * @return Fully populated LecturerDashboardModel.
     * @throws SQLException If database connection fails or queries cannot execute.
     */
    public static LecturerDashboardModel loadDashboardData(int lecturerId) throws SQLException {
        if (lecturerId <= 0) {
            throw new IllegalArgumentException("Invalid lecturer ID: " + lecturerId);
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database 'tecfams_db'. " +
                        "Please verify that MySQL server is running on localhost:3306.");
            }

            LecturerDashboardModel model = new LecturerDashboardModel();
            model.setLecturerId(lecturerId);

            // 1. Fetch Lecturer Profile
            fetchLecturerProfile(conn, lecturerId, model);

            // 2. Fetch Assigned Courses Count
            int coursesCount = fetchAssignedCoursesCount(conn, lecturerId);
            model.setAssignedCoursesCount(coursesCount);

            // 3. Fetch Uploaded Course Materials Count
            int materialsCount = fetchUploadedMaterialsCount(conn, lecturerId);
            model.setUploadedMaterialsCount(materialsCount);

            // 4. Fetch Recent Notices
            List<LecturerDashboardModel.NoticeItem> notices = fetchRecentNotices(conn);
            model.setNotices(notices);

            model.setState(LecturerDashboardModel.State.SUCCESS);
            return model;
        }
    }

    /**
     * Resolves lecturer ID by username, then loads the dashboard data.
     */
    public static LecturerDashboardModel loadDashboardDataByUsername(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database 'tecfams_db'. " +
                        "Please verify that MySQL server is running on localhost:3306.");
            }

            String sql = "SELECT user_id FROM users WHERE username = ? AND role = 'Lecturer'";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username.trim());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int lecturerId = rs.getInt("user_id");
                        return loadDashboardData(lecturerId);
                    } else {
                        throw new SQLException("No lecturer account found for username: " + username);
                    }
                }
            }
        }
    }

    // =========================================================================
    // PRIVATE HELPER QUERIES (Parameterized JDBC)
    // =========================================================================

    /**
     * Queries the authenticated lecturer's profile, including department and designation.
     */
    private static void fetchLecturerProfile(Connection conn, int lecturerId, LecturerDashboardModel model) throws SQLException {
        String sql = "SELECT l.lecturer_id, u.username, l.lecturer_name, l.email, l.phone, l.designation, " +
                     "       l.department_id, d.department_code, d.department_name " +
                     "FROM lecturers l " +
                     "JOIN users u ON l.lecturer_id = u.user_id " +
                     "LEFT JOIN departments d ON l.department_id = d.department_id " +
                     "WHERE l.lecturer_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, lecturerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    model.setUsername(rs.getString("username"));
                    model.setLecturerName(rs.getString("lecturer_name"));
                    model.setEmail(rs.getString("email"));
                    model.setDesignation(rs.getString("designation"));
                    model.setDepartmentName(rs.getString("department_name"));
                } else {
                    throw new SQLException("No lecturer record found in database for lecturer ID: " + lecturerId);
                }
            }
        }
    }

    /**
     * Counts distinct courses assigned to the authenticated lecturer.
     * Establishes course assignment through associated course materials and marks evaluation components.
     */
    private static int fetchAssignedCoursesCount(Connection conn, int lecturerId) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT course_id) AS course_count FROM (" +
                     "    SELECT course_id FROM course_materials WHERE lecturer_id = ? " +
                     "    UNION " +
                     "    SELECT ec.course_id " +
                     "    FROM marks m " +
                     "    JOIN evaluation_components ec ON m.component_id = ec.component_id " +
                     "    WHERE m.uploaded_by = ?" +
                     ") AS assigned_courses";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, lecturerId);
            pstmt.setInt(2, lecturerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("course_count");
                }
            }
        }
        return 0;
    }

    /**
     * Counts course materials uploaded by the authenticated lecturer.
     * Follows course_materials.lecturer_id foreign key.
     */
    private static int fetchUploadedMaterialsCount(Connection conn, int lecturerId) throws SQLException {
        String sql = "SELECT COUNT(*) AS material_count FROM course_materials WHERE lecturer_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, lecturerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("material_count");
                }
            }
        }
        return 0;
    }

    /**
     * Retrieves recent university notices applicable to lecturers.
     * Filters by target_audience IN ('All', 'Lecturers') and sorts latest first.
     */
    private static List<LecturerDashboardModel.NoticeItem> fetchRecentNotices(Connection conn) throws SQLException {
        List<LecturerDashboardModel.NoticeItem> list = new ArrayList<>();
        String sql = "SELECT notice_id, title, description, published_date, target_audience " +
                     "FROM notices " +
                     "WHERE target_audience IN ('All', 'Lecturers') " +
                     "ORDER BY published_date DESC, notice_id DESC " +
                     "LIMIT 5";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new LecturerDashboardModel.NoticeItem(
                            rs.getInt("notice_id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("published_date"),
                            rs.getString("target_audience")
                    ));
                }
            }
        }
        return list;
    }
}
