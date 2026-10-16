package main.java.dao.Undergraduate;

import main.java.model.Undergraduate.*;
import main.resources.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Undergraduate operations.
 * Handles database communication for the TecFAMS Undergraduate Dashboard.
 * Follows parameterized JDBC queries and safe resource management.
 */
public class UndergraduateDAO {

    /**
     * Retrieves student profile details by student ID.
     */
    public Undergraduate getUndergraduateByStudentId(String studentId) {
        String query = "SELECT u.user_id, u.username, u.name, u.email, u.phone, u.profile_picture, " +
                "s.student_id, s.reg_no, s.batch, s.department, s.enrollment_year " +
                "FROM users u INNER JOIN undergraduates s ON u.user_id = s.user_id " +
                "WHERE s.student_id = ? OR s.reg_no = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, studentId);
            stmt.setString(2, studentId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                Undergraduate student = new Undergraduate(
                        rs.getString("user_id"),
                        rs.getString("username"),
                        "", // Do not expose password
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("profile_picture"),
                        rs.getString("student_id"),
                        rs.getString("reg_no"),
                        rs.getString("batch"),
                        rs.getString("department"),
                        rs.getInt("enrollment_year")
                );
                return student;
            }
        } catch (SQLException e) {
            System.err.println("Database Notice: Could not retrieve undergraduate from database: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }
        return null;
    }

    /**
     * Updates permitted contact details (phone and email) in the database.
     * Complies with SRS Undergraduate profile update restrictions.
     */
    public boolean updateContactDetails(String studentId, String phone, String email) {
        String query = "UPDATE users u INNER JOIN undergraduates s ON u.user_id = s.user_id " +
                "SET u.phone = ?, u.email = ? WHERE s.student_id = ? OR s.reg_no = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, phone);
            stmt.setString(2, email);
            stmt.setString(3, studentId);
            stmt.setString(4, studentId);

            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("Database Notice: Could not update contact details in database: " + e.getMessage());
            return false;
        } finally {
            closeQuietly(null, stmt, conn);
        }
    }

    /**
     * Updates permitted profile picture path in the database.
     */
    public boolean updateProfilePicture(String studentId, String imagePath) {
        String query = "UPDATE users u INNER JOIN undergraduates s ON u.user_id = s.user_id " +
                "SET u.profile_picture = ? WHERE s.student_id = ? OR s.reg_no = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, imagePath);
            stmt.setString(2, studentId);
            stmt.setString(3, studentId);

            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("Database Notice: Could not update profile picture in database: " + e.getMessage());
            return false;
        } finally {
            closeQuietly(null, stmt, conn);
        }
    }

    /**
     * Retrieves student's GPA record from the database.
     */
    public GPA getStudentGPA(String studentId) {
        String query = "SELECT sgpa, cgpa FROM gpa WHERE student_id = ? ORDER BY id DESC LIMIT 1";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return new GPA(rs.getDouble("sgpa"), rs.getDouble("cgpa"));
            }
        } catch (SQLException e) {
            System.err.println("Database Notice: Could not retrieve GPA: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }
        return null;
    }

    /**
     * Retrieves attendance summary (total sessions, attended, medical) from the database.
     */
    public AttendanceSummary getAttendanceSummary(String studentId) {
        String query = "SELECT " +
                "COUNT(*) AS total_sessions, " +
                "SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) AS attended_sessions, " +
                "SUM(CASE WHEN status = 'MEDICAL' THEN 1 ELSE 0 END) AS medical_sessions " +
                "FROM attendance WHERE student_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int total = rs.getInt("total_sessions");
                if (total > 0) {
                    return new AttendanceSummary(
                            total,
                            rs.getInt("attended_sessions"),
                            rs.getInt("medical_sessions")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Database Notice: Could not retrieve attendance summary: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }
        return null;
    }

    /**
     * Retrieves today's lecture schedule for the student based on current day of week.
     */
    public List<Schedule> getTodayLectures(String studentId, String department, String batch) {
        String today = LocalDate.now().getDayOfWeek().toString(); // e.g. MONDAY
        String query = "SELECT s.schedule_id, s.day_of_week, s.start_time, s.end_time, s.location, " +
                "s.session_type, c.course_code, c.course_name, u.name AS lecturer_name " +
                "FROM schedules s " +
                "INNER JOIN courses c ON s.course_id = c.course_id " +
                "LEFT JOIN users u ON s.lecturer_id = u.user_id " +
                "WHERE (s.department = ? OR ? IS NULL) " +
                "AND (s.batch = ? OR ? IS NULL) " +
                "AND UPPER(s.day_of_week) = ? " +
                "ORDER BY s.start_time ASC";

        List<Schedule> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, department);
            stmt.setString(2, department);
            stmt.setString(3, batch);
            stmt.setString(4, batch);
            stmt.setString(5, today);
            rs = stmt.executeQuery();

            while (rs.next()) {
                SessionType type = SessionType.THEORY;
                String typeStr = rs.getString("session_type");
                if (typeStr != null && typeStr.equalsIgnoreCase("PRACTICAL")) {
                    type = SessionType.PRACTICAL;
                }

                Schedule schedule = new Schedule(
                        rs.getInt("schedule_id"),
                        rs.getString("day_of_week"),
                        rs.getString("start_time"),
                        rs.getString("end_time"),
                        rs.getString("location"),
                        type,
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getString("lecturer_name") != null ? rs.getString("lecturer_name") : "Assigned Lecturer"
                );
                list.add(schedule);
            }
        } catch (SQLException e) {
            System.err.println("Database Notice: Could not retrieve today's lectures: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }
        return list;
    }

    /**
     * Retrieves recent active notices for undergraduates.
     */
    public List<Notice> getRecentNotices(int limit) {
        String query = "SELECT notice_id, title, description, published_date, expiry_date " +
                "FROM notices " +
                "WHERE expiry_date IS NULL OR expiry_date >= CURRENT_DATE " +
                "ORDER BY published_date DESC LIMIT ?";

        List<Notice> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, limit);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Notice n = new Notice(
                        rs.getInt("notice_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("published_date"),
                        rs.getString("expiry_date")
                );
                list.add(n);
            }
        } catch (SQLException e) {
            System.err.println("Database Notice: Could not retrieve notices: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }
        return list;
    }

    /**
     * Retrieves lecturer course materials relevant to the student's department or courses.
     */
    public List<CourseMaterial> getCourseMaterialsForStudent(String studentId) {
        String query = "SELECT m.material_id, c.course_code, m.title, m.description, m.file_path, m.uploaded_date " +
                "FROM course_materials m " +
                "INNER JOIN courses c ON m.course_id = c.course_id " +
                "ORDER BY m.uploaded_date DESC";

        List<CourseMaterial> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                CourseMaterial cm = new CourseMaterial(
                        rs.getInt("material_id"),
                        rs.getString("course_code"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("file_path"),
                        rs.getString("uploaded_date")
                );
                list.add(cm);
            }
        } catch (SQLException e) {
            System.err.println("Database Notice: Could not retrieve course materials: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }
        return list;
    }

    /**
     * Helper to close JDBC resources safely.
     */
    private void closeQuietly(ResultSet rs, PreparedStatement stmt, Connection conn) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException ignored) {}
        }
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException ignored) {}
        }
        if (conn != null) {
            DatabaseConnection.closeConnection(conn);
        }
    }
}
