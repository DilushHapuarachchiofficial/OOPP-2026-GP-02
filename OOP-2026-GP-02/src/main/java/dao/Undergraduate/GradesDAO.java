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
 * Data Access Object (DAO) for Undergraduate Grades, Marks, and GPA.
 * Handles database queries for student examination results, continuous assessment (CA) marks,
 * final course grades, and Cumulative Grade Point Average (CGPA) calculations.
 *
 * Implements parameterized JDBC queries and safe connection closure.
 * Also includes robust offline fallback support to ensure UI continuity.
 */
public class GradesDAO {

    /**
     * Retrieves overall course grades and final marks for the student.
     * Queries MySQL view `view_student_course_grades`.
     *
     * @param studentIdOrRegNo Student registration number or student ID.
     * @return List of CourseGradeItem objects.
     */
    public List<CourseGradeItem> getStudentCourseGrades(String studentIdOrRegNo) {
        List<CourseGradeItem> grades = new ArrayList<>();

        if (studentIdOrRegNo == null || studentIdOrRegNo.trim().isEmpty()) {
            studentIdOrRegNo = "TG/2024/2085";
        }

        String sql = "SELECT course_id, course_code, course_name, credit, final_mark, grade, grade_point " +
                "FROM view_student_course_grades " +
                "WHERE student_id = ? OR student_reg_no = ? " +
                "ORDER BY course_code ASC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.err.println("Database Notice: Could not connect to MySQL. Using academic preview grades.");
                return getFallbackCourseGrades();
            }

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentIdOrRegNo.trim());
            stmt.setString(2, studentIdOrRegNo.trim());
            rs = stmt.executeQuery();

            while (rs.next()) {
                int courseId = rs.getInt("course_id");
                String code = rs.getString("course_code");
                String name = rs.getString("course_name");
                int credit = rs.getInt("credit");

                Double finalMark = null;
                double markVal = rs.getDouble("final_mark");
                if (!rs.wasNull()) {
                    finalMark = markVal;
                }

                String grade = rs.getString("grade");
                Double gradePoint = null;
                double gpVal = rs.getDouble("grade_point");
                if (!rs.wasNull()) {
                    gradePoint = gpVal;
                }

                // If final marks are not yet released for this course:
                String status;
                if (finalMark == null) {
                    grade = "Pending";
                    gradePoint = null;
                    status = "In Progress";
                } else {
                    status = (gradePoint != null && gradePoint >= 2.0) ? "Completed" : "Referred";
                }

                CourseGradeItem item = new CourseGradeItem(courseId, code, name, credit, finalMark, grade, gradePoint, status);
                grades.add(item);
            }

            if (grades.isEmpty()) {
                return getFallbackCourseGrades();
            }

        } catch (SQLException e) {
            System.err.println("Database Error: Could not retrieve course grades: " + e.getMessage());
            return getFallbackCourseGrades();
        } finally {
            closeQuietly(rs, stmt, conn);
        }

        return grades;
    }

    /**
     * Retrieves individual assessment and exam marks (Quiz, Mid, Practical, Final) for the student.
     * Joins `marks`, `evaluation_components`, `courses`, and `students` tables.
     *
     * @param studentIdOrRegNo Student registration number or student ID.
     * @return List of ExamMarkItem objects.
     */
    public List<ExamMarkItem> getStudentExamMarks(String studentIdOrRegNo) {
        List<ExamMarkItem> marksList = new ArrayList<>();

        if (studentIdOrRegNo == null || studentIdOrRegNo.trim().isEmpty()) {
            studentIdOrRegNo = "TG/2024/2085";
        }

        String sql = "SELECT m.mark_id, c.course_code, c.course_name, ec.component_name, ec.component_type, " +
                "ec.maximum_marks, ec.weight_percentage, m.marks_obtained, " +
                "ROUND((m.marks_obtained / ec.maximum_marks) * ec.weight_percentage, 2) AS weighted_contribution, " +
                "DATE_FORMAT(m.uploaded_date, '%Y-%m-%d') AS uploaded_date " +
                "FROM marks m " +
                "JOIN evaluation_components ec ON m.component_id = ec.component_id " +
                "JOIN courses c ON ec.course_id = c.course_id " +
                "JOIN students s ON m.student_id = s.student_id " +
                "WHERE s.student_id = ? OR s.student_reg_no = ? " +
                "ORDER BY c.course_code ASC, ec.component_type ASC, ec.component_name ASC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.err.println("Database Notice: Could not connect to MySQL. Using academic preview exam marks.");
                return getFallbackExamMarks();
            }

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentIdOrRegNo.trim());
            stmt.setString(2, studentIdOrRegNo.trim());
            rs = stmt.executeQuery();

            while (rs.next()) {
                int markId = rs.getInt("mark_id");
                String courseCode = rs.getString("course_code");
                String courseName = rs.getString("course_name");
                String compName = rs.getString("component_name");
                String compType = rs.getString("component_type");
                double maxMarks = rs.getDouble("maximum_marks");
                double weightPct = rs.getDouble("weight_percentage");
                double obtained = rs.getDouble("marks_obtained");
                double weighted = rs.getDouble("weighted_contribution");
                String upDate = rs.getString("uploaded_date");

                ExamMarkItem item = new ExamMarkItem(markId, courseCode, courseName, compName, compType,
                        maxMarks, weightPct, obtained, weighted, upDate != null ? upDate : "2026-10-07");
                marksList.add(item);
            }

            if (marksList.isEmpty()) {
                return getFallbackExamMarks();
            }

        } catch (SQLException e) {
            System.err.println("Database Error: Could not retrieve exam marks: " + e.getMessage());
            return getFallbackExamMarks();
        } finally {
            closeQuietly(rs, stmt, conn);
        }

        return marksList;
    }

    /**
     * Calculates and retrieves the cumulative GPA summary for the student.
     * Computes weighted GPA from evaluated courses and tallies registered vs completed credits.
     *
     * @param studentIdOrRegNo Student registration number or student ID.
     * @return Configured StudentGPASummary object.
     */
    public StudentGPASummary getStudentGPASummary(String studentIdOrRegNo) {
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
        int totalRegCredits = 16;
        int totalRegCourses = 8;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                return getFallbackGPASummary();
            }

            // 1. Calculate GPA and evaluated credits from courses with released marks
            String sqlGpa = "SELECT " +
                    "ROUND(SUM(grade_point * credit) / SUM(credit), 2) AS calculated_gpa, " +
                    "COALESCE(SUM(credit), 0) AS total_evaluated_credits, " +
                    "COUNT(course_id) AS total_evaluated_courses " +
                    "FROM view_student_course_grades " +
                    "WHERE (student_id = ? OR student_reg_no = ?) AND final_mark IS NOT NULL";

            stmtGpa = conn.prepareStatement(sqlGpa);
            stmtGpa.setString(1, studentIdOrRegNo.trim());
            stmtGpa.setString(2, studentIdOrRegNo.trim());
            rsGpa = stmtGpa.executeQuery();

            if (rsGpa.next()) {
                double val = rsGpa.getDouble("calculated_gpa");
                if (!rsGpa.wasNull()) {
                    gpa = val;
                }
                evaluatedCredits = rsGpa.getInt("total_evaluated_credits");
                evaluatedCourses = rsGpa.getInt("total_evaluated_courses");
            }

            // 2. Fetch total registered credits and courses
            String sqlReg = "SELECT COALESCE(SUM(c.credit), 0) AS total_credits, COUNT(c.course_id) AS total_courses " +
                    "FROM course_registrations cr " +
                    "JOIN students s ON cr.student_id = s.student_id " +
                    "JOIN courses c ON cr.course_id = c.course_id " +
                    "WHERE s.student_id = ? OR s.student_reg_no = ?";

            stmtReg = conn.prepareStatement(sqlReg);
            stmtReg.setString(1, studentIdOrRegNo.trim());
            stmtReg.setString(2, studentIdOrRegNo.trim());
            rsReg = stmtReg.executeQuery();

            if (rsReg.next()) {
                int tc = rsReg.getInt("total_credits");
                int tCourses = rsReg.getInt("total_courses");
                if (tc > 0) totalRegCredits = tc;
                if (tCourses > 0) totalRegCourses = tCourses;
            }

            if (evaluatedCredits == 0 && gpa == 0.0) {
                return getFallbackGPASummary();
            }

            return new StudentGPASummary(gpa, gpa, totalRegCredits, evaluatedCredits, totalRegCourses, evaluatedCourses);

        } catch (SQLException e) {
            System.err.println("Database Error: Could not compute student GPA: " + e.getMessage());
            return getFallbackGPASummary();
        } finally {
            closeQuietly(rsGpa, stmtGpa, null);
            closeQuietly(rsReg, stmtReg, conn);
        }
    }

    /**
     * Fallback Course Grades for offline testing or demo.
     */
    public List<CourseGradeItem> getFallbackCourseGrades() {
        List<CourseGradeItem> list = new ArrayList<>();
        list.add(new CourseGradeItem(1, "ICT2132", "Object Oriented Programming Practicum", 2, 91.30, "A+", 4.00, "Completed"));
        list.add(new CourseGradeItem(3, "ICT2122", "Object Oriented Programming", 2, 83.00, "A", 4.00, "Completed"));
        list.add(new CourseGradeItem(5, "ICT2142", "E-Business Systems", 2, null, "Pending", null, "In Progress"));
        list.add(new CourseGradeItem(6, "ICT2152", "Object Oriented Analysis and Design", 2, null, "Pending", null, "In Progress"));
        list.add(new CourseGradeItem(7, "ICT2162", "Management Information Systems", 2, null, "Pending", null, "In Progress"));
        list.add(new CourseGradeItem(8, "TCS2112", "Business Economics", 2, null, "Pending", null, "In Progress"));
        list.add(new CourseGradeItem(9, "TCS2121", "Soft Skills", 1, null, "Pending", null, "In Progress"));
        list.add(new CourseGradeItem(10, "TCS1212", "Fundamentals of Management", 2, null, "Pending", null, "In Progress"));
        list.add(new CourseGradeItem(11, "TMS1233", "Discrete Mathematics", 3, null, "Pending", null, "In Progress"));
        return list;
    }

    /**
     * Fallback Exam Marks for offline testing or demo.
     */
    public List<ExamMarkItem> getFallbackExamMarks() {
        List<ExamMarkItem> list = new ArrayList<>();
        list.add(new ExamMarkItem(4, "ICT2132", "Object Oriented Programming Practicum", "Quiz 01", "CA", 100.0, 10.0, 90.0, 9.0, "2026-10-07"));
        list.add(new ExamMarkItem(35, "ICT2132", "Object Oriented Programming Practicum", "Mid-Semester Exam", "CA", 100.0, 20.0, 88.0, 17.6, "2026-10-07"));
        list.add(new ExamMarkItem(66, "ICT2132", "Object Oriented Programming Practicum", "Mini Project Assignment", "CA", 100.0, 10.0, 95.0, 9.5, "2026-10-07"));
        list.add(new ExamMarkItem(97, "ICT2132", "Object Oriented Programming Practicum", "Final Practical & Theory Exam", "Final", 100.0, 60.0, 92.0, 55.2, "2026-10-07"));
        list.add(new ExamMarkItem(128, "ICT2122", "Object Oriented Programming", "CA Continuous Assessment", "CA", 100.0, 40.0, 80.0, 32.0, "2026-10-07"));
        list.add(new ExamMarkItem(159, "ICT2122", "Object Oriented Programming", "Final Examination", "Final", 100.0, 60.0, 85.0, 51.0, "2026-10-07"));
        return list;
    }

    /**
     * Fallback GPA summary for offline testing or demo.
     */
    public StudentGPASummary getFallbackGPASummary() {
        return new StudentGPASummary(4.00, 4.00, 18, 4, 9, 2);
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
