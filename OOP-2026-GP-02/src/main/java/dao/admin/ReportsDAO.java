package main.java.dao.admin;

import main.java.model.admin.CourseEnrollmentReportItem;
import main.java.model.admin.DepartmentStaffReportItem;
import main.java.model.admin.StaffDetailReportItem;
import main.java.model.admin.StudentEligibilityReportItem;

import java.sql.*;
import java.util.*;

public class ReportsDAO {

    public static List<StudentEligibilityReportItem> getStudentEligibilityReport(Integer courseId, Integer departmentId, String statusFilter) {
        List<StudentEligibilityReportItem> items = new ArrayList<>();
        StringBuilder query = new StringBuilder(
            "SELECT " +
            "    s.student_id, " +
            "    s.student_reg_no, " +
            "    s.full_name, " +
            "    d.department_name, " +
            "    c.course_id, " +
            "    c.course_code, " +
            "    c.course_name, " +
            "    (SELECT COUNT(*) FROM attendance_sessions ses WHERE ses.course_id = c.course_id) as total_course_sessions, " +
            "    COUNT(DISTINCT CASE WHEN a.attendance_status = 'Present' THEN a.session_id END) as present_count, " +
            "    COUNT(DISTINCT CASE WHEN a.attendance_status = 'Absent' THEN a.session_id END) as absent_count, " +
            "    COUNT(DISTINCT CASE WHEN m.approval_status = 'Approved' THEN m.medical_id END) as approved_medicals, " +
            "    (SELECT SUM(mk.marks_obtained * (ec.weight_percentage / 100.0)) " +
            "     FROM marks mk " +
            "     JOIN evaluation_components ec ON mk.component_id = ec.component_id " +
            "     WHERE ec.course_id = c.course_id AND ec.component_type = 'CA' AND mk.student_id = s.student_id) as total_ca_marks " +
            "FROM students s " +
            "JOIN departments d ON s.department_id = d.department_id " +
            "JOIN course_registrations cr ON s.student_id = cr.student_id " +
            "JOIN courses c ON cr.course_id = c.course_id " +
            "LEFT JOIN attendance_sessions ses ON c.course_id = ses.course_id " +
            "LEFT JOIN attendances a ON s.student_id = a.student_id AND ses.session_id = a.session_id " +
            "LEFT JOIN medicals m ON s.student_id = m.student_id AND ses.session_id = m.session_id " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        if (courseId != null && courseId > 0) {
            query.append(" AND c.course_id = ? ");
            params.add(courseId);
        }
        if (departmentId != null && departmentId > 0) {
            query.append(" AND s.department_id = ? ");
            params.add(departmentId);
        }

        query.append("GROUP BY s.student_id, s.student_reg_no, s.full_name, d.department_name, c.course_id, c.course_code, c.course_name ");
        query.append("ORDER BY c.course_code ASC, s.student_reg_no ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int studentId = rs.getInt("student_id");
                    String regNo = rs.getString("student_reg_no");
                    String name = rs.getString("full_name");
                    String deptName = rs.getString("department_name");
                    int cId = rs.getInt("course_id");
                    String code = rs.getString("course_code");
                    String cName = rs.getString("course_name");

                    int totalSessions = rs.getInt("total_course_sessions");
                    int present = rs.getInt("present_count");
                    int absent = rs.getInt("absent_count");
                    int meds = rs.getInt("approved_medicals");

                    int effectivePresent = Math.min(totalSessions, present + meds);
                    double pct = totalSessions > 0 ? ((double) effectivePresent / totalSessions) * 100.0 : 0.0;

                    Double caMarks = rs.getObject("total_ca_marks") != null ? rs.getDouble("total_ca_marks") : null;

                    String status;
                    if (totalSessions == 0) {
                        status = "Pending";
                    } else if (pct >= 80.0) {
                        if (meds > 0 && ((double) present / totalSessions) * 100.0 < 80.0) {
                            status = "Eligible (Medicals)";
                        } else {
                            status = "Eligible";
                        }
                    } else {
                        status = "Not Eligible";
                    }

                    // Apply status filter if specified
                    if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equalsIgnoreCase("All")) {
                        if (statusFilter.equalsIgnoreCase("Eligible") && !status.startsWith("Eligible")) {
                            continue;
                        } else if (statusFilter.equalsIgnoreCase("Not Eligible") && !status.equals("Not Eligible")) {
                            continue;
                        } else if (statusFilter.equalsIgnoreCase("Pending") && !status.equals("Pending")) {
                            continue;
                        }
                    }

                    items.add(new StudentEligibilityReportItem(
                        studentId, regNo, name, deptName, cId, code, cName,
                        totalSessions, present, absent, meds, pct, caMarks, status
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public static List<CourseEnrollmentReportItem> getCourseEnrollmentReport(Integer departmentId, Integer academicYear, Integer semester) {
        List<CourseEnrollmentReportItem> items = new ArrayList<>();
        StringBuilder query = new StringBuilder(
            "SELECT " +
            "    c.course_id, " +
            "    c.course_code, " +
            "    c.course_name, " +
            "    COALESCE(d.department_name, 'General / Multi') as department_name, " +
            "    c.academic_year, " +
            "    c.semester, " +
            "    c.credit, " +
            "    c.course_type, " +
            "    COUNT(DISTINCT cr.student_id) as enrolled_students, " +
            "    (SELECT COUNT(*) FROM attendance_sessions ses WHERE ses.course_id = c.course_id) as total_sessions, " +
            "    (SELECT COUNT(*) FROM evaluation_components ec WHERE ec.course_id = c.course_id) as total_eval_components, " +
            "    COALESCE((SELECT SUM(weight_percentage) FROM evaluation_components ec WHERE ec.course_id = c.course_id AND ec.component_type = 'CA'), 0) as ca_weight, " +
            "    COALESCE((SELECT SUM(weight_percentage) FROM evaluation_components ec WHERE ec.course_id = c.course_id AND ec.component_type = 'Final'), 0) as final_weight " +
            "FROM courses c " +
            "LEFT JOIN departments d ON c.department_id = d.department_id " +
            "LEFT JOIN course_registrations cr ON c.course_id = cr.course_id " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        if (departmentId != null && departmentId > 0) {
            query.append(" AND c.department_id = ? ");
            params.add(departmentId);
        }
        if (academicYear != null && academicYear > 0) {
            query.append(" AND c.academic_year = ? ");
            params.add(academicYear);
        }
        if (semester != null && semester > 0) {
            query.append(" AND c.semester = ? ");
            params.add(semester);
        }

        query.append("GROUP BY c.course_id, c.course_code, c.course_name, d.department_name, c.academic_year, c.semester, c.credit, c.course_type ");
        query.append("ORDER BY c.academic_year ASC, c.semester ASC, c.course_code ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new CourseEnrollmentReportItem(
                        rs.getInt("course_id"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getString("department_name"),
                        rs.getInt("academic_year"),
                        rs.getInt("semester"),
                        rs.getInt("credit"),
                        rs.getString("course_type").replace("_", " "),
                        rs.getInt("enrolled_students"),
                        rs.getInt("total_sessions"),
                        rs.getInt("total_eval_components"),
                        rs.getDouble("ca_weight"),
                        rs.getDouble("final_weight")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public static List<DepartmentStaffReportItem> getDepartmentStaffSummaryReport() {
        List<DepartmentStaffReportItem> items = new ArrayList<>();
        String query = 
            "SELECT " +
            "    d.department_id, " +
            "    d.department_code, " +
            "    d.department_name, " +
            "    (SELECT COUNT(*) FROM lecturers l WHERE l.department_id = d.department_id) as lecturer_count, " +
            "    (SELECT COUNT(*) FROM technical_officers t WHERE t.department_id = d.department_id) as to_count, " +
            "    (SELECT COUNT(*) FROM students s WHERE s.department_id = d.department_id) as student_count, " +
            "    (SELECT COUNT(*) FROM courses c WHERE c.department_id = d.department_id) as course_count " +
            "FROM departments d " +
            "ORDER BY d.department_id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                items.add(new DepartmentStaffReportItem(
                    rs.getInt("department_id"),
                    rs.getString("department_code"),
                    rs.getString("department_name"),
                    rs.getInt("lecturer_count"),
                    rs.getInt("to_count"),
                    rs.getInt("student_count"),
                    rs.getInt("course_count")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public static List<StaffDetailReportItem> getStaffDetailsReport(Integer departmentId, String roleFilter) {
        List<StaffDetailReportItem> items = new ArrayList<>();
        StringBuilder query = new StringBuilder(
            "SELECT " +
            "    u.user_id, " +
            "    COALESCE(l.lecturer_name, t.officer_name, 'Unknown') as staff_name, " +
            "    u.role, " +
            "    COALESCE(l.designation, 'Technical Officer') as designation, " +
            "    COALESCE(d.department_name, 'Not Assigned') as department_name, " +
            "    COALESCE(l.email, t.email, u.username) as email, " +
            "    COALESCE(l.phone, t.phone, 'N/A') as phone, " +
            "    u.status " +
            "FROM users u " +
            "LEFT JOIN lecturers l ON u.user_id = l.lecturer_id AND u.role = 'Lecturer' " +
            "LEFT JOIN technical_officers t ON u.user_id = t.officer_id AND u.role = 'Technical_Officer' " +
            "LEFT JOIN departments d ON d.department_id = COALESCE(l.department_id, t.department_id) " +
            "WHERE u.role IN ('Lecturer', 'Technical_Officer') "
        );

        List<Object> params = new ArrayList<>();
        if (departmentId != null && departmentId > 0) {
            query.append(" AND (l.department_id = ? OR t.department_id = ?) ");
            params.add(departmentId);
            params.add(departmentId);
        }
        if (roleFilter != null && !roleFilter.isEmpty() && !roleFilter.equalsIgnoreCase("All")) {
            String dbRole = roleFilter.replace(" ", "_");
            query.append(" AND u.role = ? ");
            params.add(dbRole);
        }

        query.append("ORDER BY d.department_name ASC, u.role ASC, staff_name ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String role = rs.getString("role");
                    if ("Technical_Officer".equals(role)) role = "Technical Officer";
                    items.add(new StaffDetailReportItem(
                        rs.getInt("user_id"),
                        rs.getString("staff_name"),
                        role,
                        rs.getString("designation"),
                        rs.getString("department_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public static Map<Integer, String> getAllDepartments() {
        Map<Integer, String> depts = new LinkedHashMap<>();
        String query = "SELECT department_id, department_name FROM departments ORDER BY department_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                depts.put(rs.getInt("department_id"), rs.getString("department_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return depts;
    }

    public static Map<Integer, String> getAllCourses() {
        Map<Integer, String> courses = new LinkedHashMap<>();
        String query = "SELECT course_id, course_code, course_name FROM courses ORDER BY course_code ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                courses.put(rs.getInt("course_id"), rs.getString("course_code") + " - " + rs.getString("course_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }
}
