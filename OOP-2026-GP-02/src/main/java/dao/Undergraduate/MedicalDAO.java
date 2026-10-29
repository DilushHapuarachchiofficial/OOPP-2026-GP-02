package main.java.dao.Undergraduate;

import main.java.model.Undergraduate.MedicalRecord;
import main.java.model.Undergraduate.MedicalStatus;
import main.java.model.Undergraduate.MissedSession;
import main.resources.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Student Medical Certificates.
 * Handles database operations for medical submissions and records.
 * Uses MySQL Workbench tecfams_db database schema without modifying table structures.
 */
public class MedicalDAO {

    /**
     * Resolves the numeric student_id from students table using student_reg_no or student_id.
     */
    public int getStudentNumericId(String studentIdOrRegNo) {
        if (studentIdOrRegNo == null || studentIdOrRegNo.trim().isEmpty()) {
            return 21; // Default fallback to student 21 (H.D.W.B. Hapuarachchi)
        }

        String sql = "SELECT student_id FROM students WHERE student_reg_no = ? OR CAST(student_id AS CHAR) = ?";
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
                if (rs.next()) {
                    return rs.getInt("student_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Database Notice: Could not resolve student_id: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }
        return 21;
    }

    /**
     * Retrieves all missed / absent lecture sessions for the logged-in student.
     */
    public List<MissedSession> getAbsentSessions(String studentIdOrRegNo) {
        List<MissedSession> list = new ArrayList<>();
        if (studentIdOrRegNo == null || studentIdOrRegNo.trim().isEmpty()) {
            return list;
        }

        String sql = "SELECT a.session_id, s.session_number, " +
                "DATE_FORMAT(s.session_date, '%Y-%m-%d') AS formatted_date, " +
                "s.session_type, c.course_code, c.course_name " +
                "FROM attendances a " +
                "INNER JOIN attendance_sessions s ON a.session_id = s.session_id " +
                "INNER JOIN courses c ON s.course_id = c.course_id " +
                "INNER JOIN students stu ON a.student_id = stu.student_id " +
                "WHERE (stu.student_reg_no = ? OR CAST(stu.student_id AS CHAR) = ?) " +
                "AND c.academic_year = 2 AND c.semester = 1 " +
                "AND a.attendance_status = 'Absent' " +
                "ORDER BY s.session_date DESC, c.course_code ASC";

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
                    MissedSession item = new MissedSession(
                            rs.getInt("session_id"),
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            rs.getInt("session_number"),
                            rs.getString("formatted_date"),
                            rs.getString("session_type")
                    );
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database Notice: Could not retrieve absent sessions: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }
        return list;
    }

    /**
     * Retrieves all missed / absent lecture sessions for the student between the specified dates.
     */
    public List<MissedSession> getAbsentSessionsBetweenDates(String studentIdOrRegNo, String startDate, String endDate) {
        List<MissedSession> list = new ArrayList<>();
        if (studentIdOrRegNo == null || studentIdOrRegNo.trim().isEmpty() || startDate == null || endDate == null) {
            return list;
        }

        String sql = "SELECT a.session_id, s.session_number, " +
                "DATE_FORMAT(s.session_date, '%Y-%m-%d') AS formatted_date, " +
                "s.session_type, c.course_code, c.course_name " +
                "FROM attendances a " +
                "INNER JOIN attendance_sessions s ON a.session_id = s.session_id " +
                "INNER JOIN courses c ON s.course_id = c.course_id " +
                "INNER JOIN students stu ON a.student_id = stu.student_id " +
                "WHERE (stu.student_reg_no = ? OR CAST(stu.student_id AS CHAR) = ?) " +
                "AND c.academic_year = 2 AND c.semester = 1 " +
                "AND a.attendance_status = 'Absent' " +
                "AND s.session_date >= ? AND s.session_date <= ? " +
                "ORDER BY s.session_date ASC, c.course_code ASC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null) {
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, studentIdOrRegNo.trim());
                stmt.setString(2, studentIdOrRegNo.trim());
                stmt.setString(3, startDate.trim());
                stmt.setString(4, endDate.trim());
                rs = stmt.executeQuery();

                while (rs.next()) {
                    MissedSession item = new MissedSession(
                            rs.getInt("session_id"),
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            rs.getInt("session_number"),
                            rs.getString("formatted_date"),
                            rs.getString("session_type")
                    );
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database Notice: Could not retrieve absent sessions for date range: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }
        return list;
    }

    /**
     * Submits a medical certificate covering the student's medical leave period.
     * Inserts a single record into the medicals table with session_id = NULL,
     * representing one official medical certificate for the entire date range.
     *
     * @return The medical_id used for the Reference Number, or -1 if failed.
     */
    public int submitMedicalForSessions(int studentId, List<Integer> sessionIds, String startDate, String endDate, String reason, String documentPath) {
        Connection conn = null;
        Statement maxStmt = null;
        PreparedStatement insertStmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                return -1;
            }

            // Find current MAX(medical_id)
            int currentMax = 0;
            maxStmt = conn.createStatement();
            rs = maxStmt.executeQuery("SELECT COALESCE(MAX(medical_id), 0) FROM medicals");
            if (rs.next()) {
                currentMax = rs.getInt(1);
            }

            int newMedicalId = currentMax + 1;
            Integer primarySessionId = (sessionIds != null && !sessionIds.isEmpty()) ? sessionIds.get(0) : null;

            String insertSql = "INSERT INTO medicals (medical_id, student_id, session_id, start_date, end_date, " +
                    "reason, document_path, submitted_date, approval_status, verified_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, CURDATE(), 'Pending', NULL)";

            insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setInt(1, newMedicalId);
            insertStmt.setInt(2, studentId);
            if (primarySessionId != null) {
                insertStmt.setInt(3, primarySessionId);
            } else {
                insertStmt.setNull(3, java.sql.Types.INTEGER);
            }
            insertStmt.setString(4, startDate);
            insertStmt.setString(5, endDate);
            insertStmt.setString(6, reason);
            insertStmt.setString(7, documentPath);
            insertStmt.executeUpdate();

            return newMedicalId;
        } catch (SQLException e) {
            System.err.println("Database Notice: Error inserting medical record: " + e.getMessage());
        } finally {
            closeQuietly(rs, maxStmt, null);
            closeQuietly(null, insertStmt, conn);
        }
        return -1;
    }

    /**
     * Submits a single medical record (legacy helper).
     */
    public int submitMedical(int studentId, Integer sessionId, String startDate, String endDate, String reason, String documentPath) {
        return submitMedicalForSessions(studentId, null, startDate, endDate, reason, documentPath);
    }

    /**
     * Retrieves all medical certificate submissions made by the logged-in student.
     */
    public List<MedicalRecord> getMedicalRecords(String studentIdOrRegNo) {
        List<MedicalRecord> list = new ArrayList<>();
        if (studentIdOrRegNo == null || studentIdOrRegNo.trim().isEmpty()) {
            return list;
        }

        String sql = "SELECT m.medical_id, m.student_id, m.session_id, " +
                "DATE_FORMAT(m.start_date, '%Y-%m-%d') AS start_date_str, " +
                "DATE_FORMAT(m.end_date, '%Y-%m-%d') AS end_date_str, " +
                "DATE_FORMAT(m.submitted_date, '%Y-%m-%d') AS submitted_date_str, " +
                "m.reason, m.document_path, m.approval_status, " +
                "c.course_code, c.course_name, s.session_number, s.session_type " +
                "FROM medicals m " +
                "INNER JOIN students stu ON m.student_id = stu.student_id " +
                "LEFT JOIN attendance_sessions s ON m.session_id = s.session_id " +
                "LEFT JOIN courses c ON s.course_id = c.course_id " +
                "WHERE (stu.student_reg_no = ? OR CAST(stu.student_id AS CHAR) = ?) " +
                "AND ((c.academic_year = 2 AND c.semester = 1) " +
                "     OR (m.session_id IS NULL AND EXISTS (" +
                "         SELECT 1 FROM attendance_sessions s2 " +
                "         INNER JOIN courses c2 ON s2.course_id = c2.course_id " +
                "         WHERE c2.academic_year = 2 AND c2.semester = 1 " +
                "         AND (s2.session_date BETWEEN m.start_date AND m.end_date)" +
                "     ))) " +
                "ORDER BY m.medical_id DESC";

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
                    int medId = rs.getInt("medical_id");
                    String statusStr = rs.getString("approval_status");
                    MedicalStatus status = parseStatus(statusStr);

                    MedicalRecord rec = new MedicalRecord(
                            medId,
                            String.valueOf(rs.getInt("student_id")),
                            rs.getString("start_date_str"),
                            rs.getString("reason"),
                            rs.getString("document_path"),
                            status
                    );

                    rec.setReferenceNo("MED-REF-" + String.format("%04d", medId));
                    rec.setSessionId(rs.getInt("session_id"));
                    rec.setStartDate(rs.getString("start_date_str"));
                    rec.setEndDate(rs.getString("end_date_str"));
                    rec.setSubmittedDate(rs.getString("submitted_date_str"));
                    rec.setCourseCode(rs.getString("course_code"));
                    rec.setCourseName(rs.getString("course_name"));
                    rec.setSessionNumber(rs.getInt("session_number"));
                    rec.setSessionType(rs.getString("session_type"));

                    list.add(rec);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database Notice: Could not retrieve medical history: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }
        return list;
    }

    private MedicalStatus parseStatus(String statusStr) {
        if (statusStr == null) return MedicalStatus.PENDING;
        String s = statusStr.trim().toUpperCase();
        if (s.contains("APPROV")) return MedicalStatus.APPROVED;
        if (s.contains("REJECT")) return MedicalStatus.REJECTED;
        return MedicalStatus.PENDING;
    }

    private void closeQuietly(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException ignored) {}
        }
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException ignored) {}
        }
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }
}
