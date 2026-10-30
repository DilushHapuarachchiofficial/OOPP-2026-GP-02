package main.java.dao.Undergraduate;

import main.java.model.Undergraduate.*;
import main.resources.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Undergraduate Attendance.
 * Retrieves real-time session logs, course-wise breakdowns, and overall eligibility metrics
 * from the TecFAMS MySQL database with fallback data handling.
 */
public class AttendanceDAO {

    /**
     * Retrieves all individual attendance session records for the specified student.
     *
     * @param studentIdOrRegNo Student registration number or student ID.
     * @return List of Attendance objects ordered by course and session number.
     */
    public List<Attendance> getAttendanceRecords(String studentIdOrRegNo) {
        List<Attendance> list = new ArrayList<>();
        if (studentIdOrRegNo == null || studentIdOrRegNo.trim().isEmpty()) {
            return list;
        }

        String sql = "SELECT " +
                "a.attendance_id, " +
                "c.course_code, " +
                "c.course_name, " +
                "s.session_number, " +
                "DATE_FORMAT(s.session_date, '%Y-%m-%d') AS formatted_date, " +
                "s.session_type, " +
                "a.attendance_status " +
                "FROM attendances a " +
                "INNER JOIN attendance_sessions s ON a.session_id = s.session_id " +
                "INNER JOIN courses c ON s.course_id = c.course_id " +
                "INNER JOIN students stu ON a.student_id = stu.student_id " +
                "WHERE (stu.student_reg_no = ? OR CAST(stu.student_id AS CHAR) = ?) " +
                "AND c.academic_year = 2 AND c.semester = 1 " +
                "ORDER BY c.course_code ASC, s.session_number ASC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null) {
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, studentIdOrRegNo.trim());
                stmt.setString(2, studentIdOrRegNo.trim());
                rs = stmt.executeQuery();

                while (rs.next()) {
                    AttendanceStatus status = parseStatus(rs.getString("attendance_status"));
                    SessionType sessionType = parseSessionType(rs.getString("session_type"));

                    Attendance att = new Attendance(
                            rs.getInt("attendance_id"),
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            rs.getString("formatted_date"),
                            status,
                            sessionType,
                            rs.getInt("session_number")
                    );
                    list.add(att);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database Notice: Could not retrieve attendance records: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }

        return list;
    }

    /**
     * Retrieves course-wise attendance breakdown and eligibility metrics.
     *
     * @param studentIdOrRegNo Student registration number or student ID.
     * @return List of CourseAttendanceSummary objects.
     */
    public List<CourseAttendanceSummary> getCourseAttendanceSummaries(String studentIdOrRegNo) {
        List<CourseAttendanceSummary> summaries = new ArrayList<>();
        if (studentIdOrRegNo == null || studentIdOrRegNo.trim().isEmpty()) {
            return summaries;
        }

        String sql = "SELECT " +
                "c.course_id, " +
                "c.course_code, " +
                "c.course_name, " +
                "COUNT(a.attendance_id) AS total_sessions, " +
                "SUM(CASE WHEN a.attendance_status = 'Present' THEN 1 ELSE 0 END) AS attended_sessions, " +
                "SUM(CASE WHEN a.attendance_status = 'Absent' THEN 1 ELSE 0 END) AS absent_sessions, " +
                "SUM(CASE WHEN a.attendance_status = 'Medical' THEN 1 ELSE 0 END) AS medical_sessions " +
                "FROM course_registrations cr " +
                "INNER JOIN students stu ON cr.student_id = stu.student_id " +
                "INNER JOIN courses c ON cr.course_id = c.course_id " +
                "LEFT JOIN attendance_sessions s ON s.course_id = c.course_id " +
                "LEFT JOIN attendances a ON a.session_id = s.session_id AND a.student_id = stu.student_id " +
                "WHERE (stu.student_reg_no = ? OR CAST(stu.student_id AS CHAR) = ?) " +
                "AND c.academic_year = 2 AND c.semester = 1 " +
                "GROUP BY c.course_id, c.course_code, c.course_name " +
                "ORDER BY c.course_code ASC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null) {
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, studentIdOrRegNo.trim());
                stmt.setString(2, studentIdOrRegNo.trim());
                rs = stmt.executeQuery();

                while (rs.next()) {
                    int courseId = rs.getInt("course_id");
                    String courseCode = rs.getString("course_code");
                    String courseName = rs.getString("course_name");
                    int total = rs.getInt("total_sessions");
                    int attended = rs.getInt("attended_sessions");
                    int absent = rs.getInt("absent_sessions");
                    int medical = rs.getInt("medical_sessions");

                    // Check approved medicals count if not directly marked in attendances table
                    int approvedMedicals = getApprovedMedicalsForCourse(conn, studentIdOrRegNo, courseId);
                    if (approvedMedicals > medical) {
                        medical = approvedMedicals;
                        if (absent >= approvedMedicals) {
                            absent -= approvedMedicals;
                        }
                    }

                    CourseAttendanceSummary cas = new CourseAttendanceSummary(
                            courseId, courseCode, courseName, total, attended, absent, medical
                    );
                    summaries.add(cas);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database Notice: Could not retrieve course attendance summaries: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }

        return summaries;
    }

    /**
     * Retrieves overall attendance summary (total sessions, attended, approved medicals, percentage).
     */
    public AttendanceSummary getOverallAttendanceSummary(String studentIdOrRegNo) {
        List<CourseAttendanceSummary> courseList = getCourseAttendanceSummaries(studentIdOrRegNo);
        int total = 0;
        int attended = 0;
        int medical = 0;

        for (CourseAttendanceSummary c : courseList) {
            total += c.getTotalSessions();
            attended += c.getAttendedSessions();
            medical += c.getMedicalSessions();
        }

        if (total > 0) {
            return new AttendanceSummary(total, attended, medical);
        }

        return new AttendanceSummary(0, 0, 0);
    }

    private int getApprovedMedicalsForCourse(Connection conn, String studentIdOrRegNo, int courseId) {
        String sql = "SELECT COUNT(*) FROM medicals m " +
                "INNER JOIN students s ON m.student_id = s.student_id " +
                "INNER JOIN attendance_sessions atts ON (m.session_id = atts.session_id OR (m.session_id IS NULL AND atts.session_date >= m.start_date AND atts.session_date <= m.end_date)) " +
                "WHERE (s.student_reg_no = ? OR CAST(s.student_id AS CHAR) = ?) " +
                "AND atts.course_id = ? AND m.approval_status = 'Approved'";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentIdOrRegNo.trim());
            stmt.setString(2, studentIdOrRegNo.trim());
            stmt.setInt(3, courseId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ignored) {
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
        }
        return 0;
    }

    private AttendanceStatus parseStatus(String statusStr) {
        if (statusStr == null) return AttendanceStatus.PRESENT;
        String s = statusStr.trim().toUpperCase();
        if (s.contains("PRESENT")) return AttendanceStatus.PRESENT;
        if (s.contains("ABSENT")) return AttendanceStatus.ABSENT;
        if (s.contains("MED")) return AttendanceStatus.MEDICAL;
        return AttendanceStatus.PRESENT;
    }

    private SessionType parseSessionType(String typeStr) {
        if (typeStr == null) return SessionType.THEORY;
        String s = typeStr.trim().toUpperCase();
        if (s.contains("PRAC")) return SessionType.PRACTICAL;
        return SessionType.THEORY;
    }

    private void closeQuietly(ResultSet rs, PreparedStatement stmt, Connection conn) {
        try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
        try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
        DatabaseConnection.closeConnection(conn);
    }
}
