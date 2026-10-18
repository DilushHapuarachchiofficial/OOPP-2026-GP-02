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
     * Retrieves student profile details by student ID, registration number, or username.
     */
    public Undergraduate getUndergraduateByStudentId(String studentId) {
        String query = "SELECT u.user_id, u.username, s.full_name, s.email, s.phone, u.profile_picture, " +
                "s.student_id, s.student_reg_no, s.academic_year, d.department_name, YEAR(s.enrollment_date) AS enrollment_year " +
                "FROM users u " +
                "INNER JOIN students s ON u.user_id = s.student_id " +
                "LEFT JOIN departments d ON s.department_id = d.department_id " +
                "WHERE s.student_id = ? OR s.student_reg_no = ? OR u.username = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return null;
            stmt = conn.prepareStatement(query);
            stmt.setString(1, studentId);
            stmt.setString(2, studentId);
            stmt.setString(3, studentId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int enYear = rs.getInt("enrollment_year");
                String batch = (enYear > 0) ? "Batch " + (enYear - 1) + "/" + enYear : "2023/2024";
                String deptName = rs.getString("department_name");
                if (deptName == null || deptName.trim().isEmpty()) {
                    deptName = "Department of Information and Communication Technology";
                }

                Undergraduate student = new Undergraduate(
                        String.valueOf(rs.getInt("user_id")),
                        rs.getString("username"),
                        "", // Do not expose password
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("profile_picture"),
                        String.valueOf(rs.getInt("student_id")),
                        rs.getString("student_reg_no"),
                        batch,
                        deptName,
                        enYear > 0 ? enYear : 2024
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
        String query = "UPDATE students SET phone = ?, email = ? WHERE student_id = ? OR student_reg_no = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;
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
        String query = "UPDATE users u INNER JOIN students s ON u.user_id = s.student_id " +
                "SET u.profile_picture = ? WHERE s.student_id = ? OR s.student_reg_no = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;
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
     * Retrieves student's GPA record calculated from course grades or view.
     */
    public GPA getStudentGPA(String studentId) {
        String query = "SELECT ROUND(SUM(grade_point * credit) / SUM(credit), 2) AS calculated_gpa " +
                "FROM view_student_course_grades " +
                "WHERE (student_id = ? OR student_reg_no = ?) AND final_mark IS NOT NULL";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return null;
            stmt = conn.prepareStatement(query);
            stmt.setString(1, studentId);
            stmt.setString(2, studentId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                double gpaVal = rs.getDouble("calculated_gpa");
                if (!rs.wasNull()) {
                    return new GPA(gpaVal, gpaVal);
                }
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
                "COUNT(a.attendance_id) AS total_sessions, " +
                "SUM(CASE WHEN a.attendance_status = 'Present' THEN 1 ELSE 0 END) AS attended_sessions, " +
                "(SELECT COUNT(*) FROM medicals m JOIN students s2 ON m.student_id = s2.student_id " +
                " WHERE (s2.student_id = ? OR s2.student_reg_no = ?) AND m.approval_status = 'Approved') AS medical_sessions " +
                "FROM students s " +
                "LEFT JOIN attendances a ON s.student_id = a.student_id " +
                "WHERE s.student_id = ? OR s.student_reg_no = ? " +
                "GROUP BY s.student_id";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return null;
            stmt = conn.prepareStatement(query);
            stmt.setString(1, studentId);
            stmt.setString(2, studentId);
            stmt.setString(3, studentId);
            stmt.setString(4, studentId);
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
     * If no lectures are scheduled for today (e.g. weekend), fetches the weekly entries.
     */
    public List<Schedule> getTodayLectures(String studentId, String department, String batch) {
        String today = LocalDate.now().getDayOfWeek().toString(); // e.g. MONDAY
        String formattedToday = today.substring(0, 1).toUpperCase() + today.substring(1).toLowerCase();

        String query = "SELECT te.entry_id, te.day_of_week, " +
                "TIME_FORMAT(te.start_time, '%h:%i %p') AS start_time_str, " +
                "TIME_FORMAT(te.end_time, '%h:%i %p') AS end_time_str, " +
                "te.hall, te.session_type, c.course_code, c.course_name, " +
                "COALESCE((SELECT l.lecturer_name FROM lecturers l WHERE l.department_id = c.department_id LIMIT 1), 'Assigned Lecturer') AS lecturer_name " +
                "FROM timetable_entries te " +
                "INNER JOIN courses c ON te.course_id = c.course_id " +
                "INNER JOIN timetables t ON te.timetable_id = t.timetable_id " +
                "WHERE LOWER(te.day_of_week) = LOWER(?) " +
                "ORDER BY te.start_time ASC";

        List<Schedule> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return list;
            stmt = conn.prepareStatement(query);
            stmt.setString(1, formattedToday);
            rs = stmt.executeQuery();

            while (rs.next()) {
                SessionType type = SessionType.THEORY;
                String typeStr = rs.getString("session_type");
                if (typeStr != null && typeStr.equalsIgnoreCase("PRACTICAL")) {
                    type = SessionType.PRACTICAL;
                }

                Schedule schedule = new Schedule(
                        rs.getInt("entry_id"),
                        rs.getString("day_of_week"),
                        rs.getString("start_time_str"),
                        rs.getString("end_time_str"),
                        rs.getString("hall"),
                        type,
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getString("lecturer_name")
                );
                list.add(schedule);
            }

            // Fallback for weekends / empty days: return regular active timetable schedule
            if (list.isEmpty()) {
                String fallbackQuery = "SELECT te.entry_id, te.day_of_week, " +
                        "TIME_FORMAT(te.start_time, '%h:%i %p') AS start_time_str, " +
                        "TIME_FORMAT(te.end_time, '%h:%i %p') AS end_time_str, " +
                        "te.hall, te.session_type, c.course_code, c.course_name, " +
                        "COALESCE((SELECT l.lecturer_name FROM lecturers l WHERE l.department_id = c.department_id LIMIT 1), 'Assigned Lecturer') AS lecturer_name " +
                        "FROM timetable_entries te " +
                        "INNER JOIN courses c ON te.course_id = c.course_id " +
                        "INNER JOIN timetables t ON te.timetable_id = t.timetable_id " +
                        "ORDER BY FIELD(te.day_of_week, 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'), te.start_time ASC " +
                        "LIMIT 5";
                try (PreparedStatement fbStmt = conn.prepareStatement(fallbackQuery);
                     ResultSet fbRs = fbStmt.executeQuery()) {
                    while (fbRs.next()) {
                        SessionType type = SessionType.THEORY;
                        String typeStr = fbRs.getString("session_type");
                        if (typeStr != null && typeStr.equalsIgnoreCase("PRACTICAL")) {
                            type = SessionType.PRACTICAL;
                        }

                        Schedule schedule = new Schedule(
                                fbRs.getInt("entry_id"),
                                fbRs.getString("day_of_week"),
                                fbRs.getString("start_time_str"),
                                fbRs.getString("end_time_str"),
                                fbRs.getString("hall"),
                                type,
                                fbRs.getString("course_code"),
                                fbRs.getString("course_name"),
                                fbRs.getString("lecturer_name")
                        );
                        list.add(schedule);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Database Notice: Could not retrieve today's lectures: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }
        return list;
    }

    /**
     * Retrieves recent active notices for undergraduates from the database.
     */
    public List<Notice> getRecentNotices(int limit) {
        String query = "SELECT notice_id, title, description, DATE_FORMAT(published_date, '%Y-%m-%d') AS pub_date " +
                "FROM notices " +
                "WHERE target_audience IN ('All', 'Students') " +
                "ORDER BY published_date DESC LIMIT ?";

        List<Notice> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return list;
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, limit);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Notice n = new Notice(
                        rs.getInt("notice_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("pub_date"),
                        null
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
     * Retrieves lecturer course materials from the database.
     */
    public List<CourseMaterial> getCourseMaterialsForStudent(String studentId) {
        String query = "SELECT m.material_id, c.course_code, m.material_title AS title, " +
                "m.material_description AS description, m.file_path, " +
                "DATE_FORMAT(m.uploaded_date, '%Y-%m-%d') AS uploaded_date " +
                "FROM course_materials m " +
                "INNER JOIN courses c ON m.course_id = c.course_id " +
                "ORDER BY m.uploaded_date DESC";

        List<CourseMaterial> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return list;
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
     * Retrieves enrolled/registered courses for the given student from the database.
     */
    public List<Course> getCoursesForStudent(String studentId) {
        String query = "SELECT c.course_id, c.course_code, c.course_name, c.credit, " +
                "COALESCE(d.department_code, 'ICT') AS dept, c.course_type " +
                "FROM course_registrations cr " +
                "INNER JOIN students s ON cr.student_id = s.student_id " +
                "INNER JOIN courses c ON cr.course_id = c.course_id " +
                "LEFT JOIN departments d ON c.department_id = d.department_id " +
                "WHERE s.student_id = ? OR s.student_reg_no = ? " +
                "ORDER BY c.course_code ASC";

        List<Course> courses = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return courses;
            stmt = conn.prepareStatement(query);
            stmt.setString(1, studentId);
            stmt.setString(2, studentId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Course course = new Course(
                        rs.getInt("course_id"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getInt("credit"),
                        rs.getString("dept"),
                        rs.getString("course_type")
                );
                courses.add(course);
            }
        } catch (SQLException e) {
            System.err.println("Database Notice: Could not retrieve courses: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }
        return courses;
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
