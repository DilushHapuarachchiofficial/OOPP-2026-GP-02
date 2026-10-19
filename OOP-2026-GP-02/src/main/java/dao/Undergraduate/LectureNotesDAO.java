package main.java.dao.Undergraduate;

import main.java.model.Undergraduate.Course;
import main.java.model.Undergraduate.LectureNoteItem;
import main.resources.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Lecture Notes operations.
 * Handles database queries strictly for retrieving enrolled courses
 * and the lecture notes uploaded for those enrolled courses.
 * Follows parameterized PreparedStatement queries and safe JDBC practices.
 */
public class LectureNotesDAO {

    /**
     * Retrieves the courses currently enrolled by the student.
     * Uses table 'course_registrations' joined with 'courses' and 'students'.
     *
     * @param studentIdOrRegNo The student ID or registration number (e.g. "TG/2024/2085").
     * @return List of Course objects the student is enrolled in.
     */
    public List<Course> getEnrolledCoursesForStudent(String studentIdOrRegNo) {
        List<Course> enrolledCourses = new ArrayList<>();

        if (studentIdOrRegNo == null || studentIdOrRegNo.trim().isEmpty()) {
            return enrolledCourses;
        }

        String sql = "SELECT c.course_id, c.course_code, c.course_name, c.credit, " +
                "COALESCE(d.department_code, 'ICT') AS dept, c.course_type " +
                "FROM course_registrations cr " +
                "INNER JOIN students s ON cr.student_id = s.student_id " +
                "INNER JOIN courses c ON cr.course_id = c.course_id " +
                "LEFT JOIN departments d ON c.department_id = d.department_id " +
                "WHERE s.student_id = ? OR s.student_reg_no = ? " +
                "ORDER BY c.course_code ASC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.err.println("Database Notice: Unable to connect to MySQL database.");
                return enrolledCourses;
            }

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentIdOrRegNo.trim());
            stmt.setString(2, studentIdOrRegNo.trim());
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
                enrolledCourses.add(course);
            }
        } catch (SQLException e) {
            System.err.println("Database Notice: Error retrieving enrolled courses: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }

        return enrolledCourses;
    }

    /**
     * Retrieves only the lecture notes belonging to the courses
     * in which the logged-in student is currently enrolled.
     *
     * Logical path enforced in SQL:
     * Logged-in Student -> Enrolled Courses (course_registrations) -> Lecture Notes (course_materials)
     *
     * @param studentIdOrRegNo The student ID or registration number.
     * @return List of LectureNoteItem objects for the student's enrolled courses.
     */
    public List<LectureNoteItem> getLectureNotesForStudent(String studentIdOrRegNo) {
        List<LectureNoteItem> lectureNotes = new ArrayList<>();

        if (studentIdOrRegNo == null || studentIdOrRegNo.trim().isEmpty()) {
            return lectureNotes;
        }

        // Enforce the course-based restriction in the SQL query:
        // Join course_materials with courses, then join with course_registrations and students.
        String sql = "SELECT m.material_id, " +
                "c.course_id, " +
                "c.course_code, " +
                "c.course_name, " +
                "COALESCE(l.lecturer_name, 'Assigned Lecturer') AS lecturer_name, " +
                "m.material_title, " +
                "m.material_description, " +
                "m.file_path, " +
                "DATE_FORMAT(m.uploaded_date, '%Y-%m-%d') AS uploaded_date " +
                "FROM course_materials m " +
                "INNER JOIN courses c ON m.course_id = c.course_id " +
                "INNER JOIN course_registrations cr ON c.course_id = cr.course_id " +
                "INNER JOIN students s ON cr.student_id = s.student_id " +
                "LEFT JOIN lecturers l ON m.lecturer_id = l.lecturer_id " +
                "WHERE s.student_id = ? OR s.student_reg_no = ? " +
                "ORDER BY c.course_code ASC, m.uploaded_date DESC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.err.println("Database Notice: Unable to connect to MySQL database.");
                return lectureNotes;
            }

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentIdOrRegNo.trim());
            stmt.setString(2, studentIdOrRegNo.trim());
            rs = stmt.executeQuery();

            while (rs.next()) {
                LectureNoteItem note = new LectureNoteItem(
                        rs.getInt("material_id"),
                        rs.getInt("course_id"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getString("lecturer_name"),
                        rs.getString("material_title"),
                        rs.getString("material_description"),
                        rs.getString("file_path"),
                        rs.getString("uploaded_date")
                );
                lectureNotes.add(note);
            }
        } catch (SQLException e) {
            System.err.println("Database Notice: Error retrieving lecture notes: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }

        return lectureNotes;
    }

    /**
     * Safely closes JDBC resources to avoid connection leaks.
     */
    private void closeQuietly(ResultSet rs, PreparedStatement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignored) {
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignored) {
            }
        }
        if (conn != null) {
            DatabaseConnection.closeConnection(conn);
        }
    }
}
