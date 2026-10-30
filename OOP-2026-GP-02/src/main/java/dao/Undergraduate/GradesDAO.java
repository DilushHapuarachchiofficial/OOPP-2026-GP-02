package main.java.dao.Undergraduate;

import main.java.model.Undergraduate.CourseGradeItem;
import main.java.model.Undergraduate.ExamMarkItem;
import main.java.model.Undergraduate.StudentGPASummary;
import main.resources.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Academic Grades, Component Marks & GPA computations.
 * Interfaces with TecFAMS MySQL database views and tables (marks, evaluation_components,
 * view_student_course_grades, view_student_gpa).
 */
public class GradesDAO {

    /**
     * Retrieves course unit results and component mark breakdowns for the student,
     * with optional academic year and semester filtering.
     *
     * @param studentIdOrRegNo Student registration number (e.g. TG/2024/2085) or ID.
     * @param yearFilter       Academic year (1, 2) or null for all.
     * @param semFilter        Semester (1, 2) or null for all.
     * @return List of CourseGradeItem objects.
     */
    public List<CourseGradeItem> getStudentCourseGrades(String studentIdOrRegNo, Integer yearFilter, Integer semFilter) {
        List<CourseGradeItem> grades = new ArrayList<>();

        if (studentIdOrRegNo == null || studentIdOrRegNo.trim().isEmpty()) {
            studentIdOrRegNo = "TG/2024/2085";
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
           .append("c.course_id, c.course_code, c.course_name, c.credit, c.academic_year, c.semester, ")
           .append("MAX(CASE WHEN ec.component_name LIKE '%Quiz%' THEN m.marks_obtained END) AS quiz_mark, ")
           .append("MAX(CASE WHEN ec.component_name LIKE '%Assign%' THEN m.marks_obtained END) AS assignment_mark, ")
           .append("MAX(CASE WHEN ec.component_name LIKE '%Mid%' THEN m.marks_obtained END) AS mid_exam_mark, ")
           .append("SUM(CASE WHEN ec.component_type = 'CA' THEN (m.marks_obtained / ec.maximum_marks) * ec.weight_percentage END) AS ca_total, ")
           .append("MAX(CASE WHEN ec.component_type = 'Final' THEN m.marks_obtained END) AS final_exam_mark, ")
           .append("v.final_mark, v.grade, v.grade_point ")
           .append("FROM course_registrations cr ")
           .append("JOIN students s ON cr.student_id = s.student_id ")
           .append("JOIN courses c ON cr.course_id = c.course_id ")
           .append("LEFT JOIN evaluation_components ec ON c.course_id = ec.course_id ")
           .append("LEFT JOIN marks m ON ec.component_id = m.component_id AND s.student_id = m.student_id ")
           .append("LEFT JOIN view_student_course_grades v ON c.course_id = v.course_id AND s.student_id = v.student_id ")
           .append("WHERE (s.student_reg_no = ? OR CAST(s.student_id AS CHAR) = ?) ");

        if (yearFilter != null) {
            sql.append("AND c.academic_year = ? ");
        }
        if (semFilter != null) {
            sql.append("AND c.semester = ? ");
        }

        sql.append("GROUP BY c.course_id, c.course_code, c.course_name, c.credit, c.academic_year, c.semester, ")
           .append("v.final_mark, v.grade, v.grade_point ")
           .append("ORDER BY c.academic_year ASC, c.semester ASC, c.course_code ASC");

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.err.println("Database Error: Could not connect to MySQL database.");
                return grades;
            }

            stmt = conn.prepareStatement(sql.toString());
            stmt.setString(1, studentIdOrRegNo.trim());
            stmt.setString(2, studentIdOrRegNo.trim());

            int paramIndex = 3;
            if (yearFilter != null) {
                stmt.setInt(paramIndex++, yearFilter);
            }
            if (semFilter != null) {
                stmt.setInt(paramIndex++, semFilter);
            }

            rs = stmt.executeQuery();

            while (rs.next()) {
                int courseId = rs.getInt("course_id");
                String code = rs.getString("course_code");
                String name = rs.getString("course_name");
                int credit = rs.getInt("credit");
                int acYear = rs.getInt("academic_year");
                int sem = rs.getInt("semester");

                Double quiz = getNullableDouble(rs, "quiz_mark");
                Double assignment = getNullableDouble(rs, "assignment_mark");
                Double midExam = getNullableDouble(rs, "mid_exam_mark");
                Double caTotal = getNullableDouble(rs, "ca_total");
                Double finalExam = getNullableDouble(rs, "final_exam_mark");

                Double finalMark = getNullableDouble(rs, "final_mark");
                String grade = rs.getString("grade");
                Double gradePoint = getNullableDouble(rs, "grade_point");

                // Determine student course status
                String status;
                if (finalMark == null) {
                    grade = "Pending";
                    gradePoint = null;
                    status = "In Progress";
                } else {
                    status = (gradePoint != null && gradePoint >= 2.0) ? "Completed" : "Referred";
                }

                CourseGradeItem item = new CourseGradeItem(courseId, code, name, credit, acYear, sem,
                        quiz, assignment, midExam, caTotal, finalExam,
                        finalMark, grade, gradePoint, status);
                grades.add(item);
            }

        } catch (SQLException e) {
            System.err.println("Database Error: Could not retrieve course grades: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }

        return grades;
    }

    public List<CourseGradeItem> getStudentCourseGrades(String studentIdOrRegNo) {
        return getStudentCourseGrades(studentIdOrRegNo, null, null);
    }

    /**
     * Retrieves individual assessment and exam marks (Quiz, Mid, Practical, Final) for the student,
     * with optional academic year and semester filtering.
     *
     * @param studentIdOrRegNo Student registration number or student ID.
     * @param yearFilter       Academic year filter (null for all).
     * @param semFilter        Semester filter (null for all).
     * @return List of ExamMarkItem records from database.
     */
    public List<ExamMarkItem> getStudentExamMarks(String studentIdOrRegNo, Integer yearFilter, Integer semFilter) {
        List<ExamMarkItem> marksList = new ArrayList<>();

        if (studentIdOrRegNo == null || studentIdOrRegNo.trim().isEmpty()) {
            studentIdOrRegNo = "TG/2024/2085";
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
           .append("m.mark_id, c.course_code, c.course_name, c.academic_year, c.semester, ")
           .append("ec.component_name, ec.component_type, ec.maximum_marks, ec.weight_percentage, ")
           .append("m.marks_obtained, ")
           .append("ROUND((m.marks_obtained / ec.maximum_marks) * ec.weight_percentage, 2) AS weighted_contribution, ")
           .append("DATE_FORMAT(m.created_at, '%Y-%m-%d') AS uploaded_date ")
           .append("FROM marks m ")
           .append("JOIN evaluation_components ec ON m.component_id = ec.component_id ")
           .append("JOIN courses c ON ec.course_id = c.course_id ")
           .append("JOIN students s ON m.student_id = s.student_id ")
           .append("WHERE (s.student_reg_no = ? OR CAST(s.student_id AS CHAR) = ?) ");

        if (yearFilter != null) {
            sql.append("AND c.academic_year = ? ");
        }
        if (semFilter != null) {
            sql.append("AND c.semester = ? ");
        }

        sql.append("ORDER BY c.academic_year ASC, c.semester ASC, c.course_code ASC, ec.component_type ASC, ec.component_name ASC");

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.err.println("Database Error: Could not connect to MySQL database.");
                return marksList;
            }

            stmt = conn.prepareStatement(sql.toString());
            stmt.setString(1, studentIdOrRegNo.trim());
            stmt.setString(2, studentIdOrRegNo.trim());

            int paramIndex = 3;
            if (yearFilter != null) {
                stmt.setInt(paramIndex++, yearFilter);
            }
            if (semFilter != null) {
                stmt.setInt(paramIndex++, semFilter);
            }

            rs = stmt.executeQuery();

            while (rs.next()) {
                int markId = rs.getInt("mark_id");
                String courseCode = rs.getString("course_code");
                String courseName = rs.getString("course_name");
                int acYear = rs.getInt("academic_year");
                int sem = rs.getInt("semester");
                String compName = rs.getString("component_name");
                String compType = rs.getString("component_type");
                double maxMarks = rs.getDouble("maximum_marks");
                double weightPct = rs.getDouble("weight_percentage");
                double obtained = rs.getDouble("marks_obtained");
                double weighted = rs.getDouble("weighted_contribution");
                String upDate = rs.getString("uploaded_date");

                ExamMarkItem item = new ExamMarkItem(markId, courseCode, courseName, acYear, sem, compName, compType,
                        maxMarks, weightPct, obtained, weighted, upDate != null ? upDate : "2026-10-07");
                marksList.add(item);
            }

        } catch (SQLException e) {
            System.err.println("Database Error: Could not retrieve exam marks: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }

        return marksList;
    }

    public List<ExamMarkItem> getStudentExamMarks(String studentIdOrRegNo) {
        return getStudentExamMarks(studentIdOrRegNo, null, null);
    }

    /**
     * Calculates and retrieves the GPA summary for the student for the given year/semester period.
     *
     * @param studentIdOrRegNo Student registration number or student ID.
     * @param yearFilter       Academic year filter (null for all).
     * @param semFilter        Semester filter (null for all).
     * @return Configured StudentGPASummary object.
     */
    public StudentGPASummary getStudentGPASummary(String studentIdOrRegNo, Integer yearFilter, Integer semFilter) {
        if (studentIdOrRegNo == null || studentIdOrRegNo.trim().isEmpty()) {
            studentIdOrRegNo = "TG/2024/2085";
        }

        Connection conn = null;
        PreparedStatement stmtGpa = null;
        PreparedStatement stmtReg = null;
        ResultSet rsGpa = null;
        ResultSet rsReg = null;

        double gpa = 0.0;
        int evaluatedCredits = 0;
        int evaluatedCourses = 0;
        int totalRegCredits = 0;
        int totalRegCourses = 0;

        String periodLabel;
        if (yearFilter == null) {
            periodLabel = "All Academic Years (Cumulative)";
        } else if (semFilter == null) {
            periodLabel = "Year " + yearFilter + " (Level " + (yearFilter == 1 ? "I" : "II") + ") All Semesters";
        } else {
            periodLabel = "Year " + yearFilter + " Semester " + semFilter;
        }

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.err.println("Database Error: Could not connect to MySQL database.");
                return new StudentGPASummary(0.0, 0.0, 0, 0, 0, 0, periodLabel);
            }

            // 1. Calculate GPA from evaluated courses for this period
            StringBuilder sqlGpa = new StringBuilder();
            sqlGpa.append("SELECT ")
                  .append("ROUND(SUM(v.grade_point * c.credit) / SUM(c.credit), 2) AS calculated_gpa, ")
                  .append("COALESCE(SUM(c.credit), 0) AS total_evaluated_credits, ")
                  .append("COUNT(v.course_id) AS total_evaluated_courses ")
                  .append("FROM view_student_course_grades v ")
                  .append("JOIN courses c ON v.course_id = c.course_id ")
                  .append("WHERE (v.student_reg_no = ? OR CAST(v.student_id AS CHAR) = ?) AND v.final_mark IS NOT NULL ");

            if (yearFilter != null) sqlGpa.append("AND c.academic_year = ? ");
            if (semFilter != null) sqlGpa.append("AND c.semester = ? ");

            stmtGpa = conn.prepareStatement(sqlGpa.toString());
            stmtGpa.setString(1, studentIdOrRegNo.trim());
            stmtGpa.setString(2, studentIdOrRegNo.trim());

            int idx = 3;
            if (yearFilter != null) stmtGpa.setInt(idx++, yearFilter);
            if (semFilter != null) stmtGpa.setInt(idx++, semFilter);

            rsGpa = stmtGpa.executeQuery();
            if (rsGpa.next()) {
                double val = rsGpa.getDouble("calculated_gpa");
                if (!rsGpa.wasNull()) {
                    gpa = val;
                }
                evaluatedCredits = rsGpa.getInt("total_evaluated_credits");
                evaluatedCourses = rsGpa.getInt("total_evaluated_courses");
            }

            // 2. Fetch total registered credits and courses for this period
            StringBuilder sqlReg = new StringBuilder();
            sqlReg.append("SELECT COALESCE(SUM(c.credit), 0) AS total_credits, COUNT(c.course_id) AS total_courses ")
                  .append("FROM course_registrations cr ")
                  .append("JOIN students s ON cr.student_id = s.student_id ")
                  .append("JOIN courses c ON cr.course_id = c.course_id ")
                  .append("WHERE (s.student_reg_no = ? OR CAST(s.student_id AS CHAR) = ?) ");

            if (yearFilter != null) sqlReg.append("AND c.academic_year = ? ");
            if (semFilter != null) sqlReg.append("AND c.semester = ? ");

            stmtReg = conn.prepareStatement(sqlReg.toString());
            stmtReg.setString(1, studentIdOrRegNo.trim());
            stmtReg.setString(2, studentIdOrRegNo.trim());

            idx = 3;
            if (yearFilter != null) stmtReg.setInt(idx++, yearFilter);
            if (semFilter != null) stmtReg.setInt(idx++, semFilter);

            rsReg = stmtReg.executeQuery();
            if (rsReg.next()) {
                totalRegCredits = rsReg.getInt("total_credits");
                totalRegCourses = rsReg.getInt("total_courses");
            }

            return new StudentGPASummary(gpa, gpa, totalRegCredits, evaluatedCredits, totalRegCourses, evaluatedCourses, periodLabel);

        } catch (SQLException e) {
            System.err.println("Database Error: Could not compute student GPA: " + e.getMessage());
            return new StudentGPASummary(0.0, 0.0, 0, 0, 0, 0, periodLabel);
        } finally {
            closeQuietly(rsGpa, stmtGpa, null);
            closeQuietly(rsReg, stmtReg, conn);
        }
    }

    public StudentGPASummary getStudentGPASummary(String studentIdOrRegNo) {
        return getStudentGPASummary(studentIdOrRegNo, null, null);
    }

    private Double getNullableDouble(ResultSet rs, String colName) throws SQLException {
        double val = rs.getDouble(colName);
        return rs.wasNull() ? null : val;
    }

    /**
     * Safely closes JDBC database resources.
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
