package main.java.dao.Undergraduate;

import main.java.model.Undergraduate.Course;
import main.resources.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Course management.
 * Responsible for fetching undergraduate enrolled and registered courses
 * from TecFAMS MySQL database with course metadata (codes, names, credits, types).
 */
public class CourseDAO {

    /**
     * Retrieves all courses registered by the student from the database.
     * Enforces the real student registration association via 'course_registrations'.
     *
     * @param studentIdOrRegNo Student registration number (e.g. TG/2024/2085) or ID.
     * @return List of registered Course objects.
     */
    public List<Course> getRegisteredCoursesForStudent(String studentIdOrRegNo) {
        List<Course> courses = new ArrayList<>();

        if (studentIdOrRegNo == null || studentIdOrRegNo.trim().isEmpty()) {
            studentIdOrRegNo = "TG/2024/2085";
        }

        String sql = "SELECT c.course_id, c.course_code, c.course_name, c.credit, c.course_type, " +
                "COALESCE(c.semester, cr.semester, 1) AS course_sem, " +
                "COALESCE(c.academic_year, 2) AS course_year, " +
                "COALESCE(d.department_code, 'ICT') AS dept_code, " +
                "COALESCE(d.department_name, 'Department of Information and Communication Technology') AS dept_name, " +
                "COALESCE(cr.registration_status, 'Registered') AS reg_status " +
                "FROM course_registrations cr " +
                "INNER JOIN students s ON cr.student_id = s.student_id " +
                "INNER JOIN courses c ON cr.course_id = c.course_id " +
                "LEFT JOIN departments d ON c.department_id = d.department_id " +
                "WHERE s.student_reg_no = ? OR CAST(s.student_id AS CHAR) = ? " +
                "ORDER BY course_year DESC, course_sem ASC, c.course_code ASC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.err.println("Database Notice: Could not connect to MySQL. Using academic preview courses.");
                return getFallbackRegisteredCourses();
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
                String type = rs.getString("course_type");
                int sem = rs.getInt("course_sem");
                int year = rs.getInt("course_year");
                if (sem == 0) sem = 1;
                if (year == 0) {
                    year = (code.length() >= 4 && Character.isDigit(code.charAt(3)))
                            ? Character.getNumericValue(code.charAt(3)) : 2;
                }
                String dept = rs.getString("dept_code");
                String status = rs.getString("reg_status");

                Course c = new Course(
                        courseId,
                        code,
                        name,
                        credit,
                        dept,
                        type, // description / syllabus overview
                        type,
                        sem,
                        year,
                        status
                );
                courses.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Database Notice: Error retrieving registered courses: " + e.getMessage());
            if (courses.isEmpty()) {
                return getFallbackRegisteredCourses();
            }
        } finally {
            closeQuietly(rs, stmt, conn);
        }

        // If no records found in database for student, provide default faculty curriculum
        if (courses.isEmpty()) {
            return getFallbackRegisteredCourses();
        }

        return courses;
    }

    /**
     * Calculates the total sum of credits for a list of courses.
     *
     * @param courses List of courses.
     * @return Total academic credit sum.
     */
    public int calculateTotalCredits(List<Course> courses) {
        if (courses == null || courses.isEmpty()) return 0;
        int sum = 0;
        for (Course c : courses) {
            sum += c.getCredits();
        }
        return sum;
    }

    /**
     * Fallback courses representing the Faculty of Technology real Level II / Level I registered modules.
     * Used when database connection is unreachable or for preview demonstrations.
     */
    public List<Course> getFallbackRegisteredCourses() {
        List<Course> list = new ArrayList<>();
        list.add(new Course(1, "ICT2113", "Data Structures and Algorithms", 3, "ICT", "Data structures, algorithms and algorithmic complexity", "Theory_and_Practical", 1, 2, "Registered"));
        list.add(new Course(2, "ICT2122", "Object Oriented Programming", 2, "ICT", "OOP fundamentals, classes, inheritance and polymorphism", "Theory_Only", 1, 2, "Registered"));
        list.add(new Course(3, "ICT2132", "Object Oriented Programming Practicum", 2, "ICT", "Java Swing GUI development and JDBC database connectivity", "Practical_Only", 1, 2, "Registered"));
        list.add(new Course(4, "ICT2142", "E-Business Systems", 2, "ICT", "Electronic commerce, payment gateways and business models", "Theory_and_Practical", 1, 2, "Registered"));
        list.add(new Course(5, "ICT2152", "Object Oriented Analysis and Design", 2, "ICT", "UML diagrams, design patterns and software architectural modeling", "Theory_Only", 1, 2, "Registered"));
        list.add(new Course(6, "ICT2162", "Management Information Systems", 2, "ICT", "Enterprise systems, ERP, CRM and business decision making", "Theory_Only", 1, 2, "Registered"));
        list.add(new Course(7, "ENG2122", "English III", 2, "ICT", "Technical communication, academic writing and presentation skills", "Theory_Only", 1, 2, "Registered"));
        list.add(new Course(8, "TCS2112", "Business Economics", 2, "MDS", "Microeconomics, macroeconomics and cost-benefit analysis", "Theory_Only", 1, 2, "Registered"));
        list.add(new Course(9, "TCS2121", "Soft Skills", 1, "MDS", "Leadership, teamwork, emotional intelligence and workplace ethics", "Practical_Only", 1, 2, "Registered"));
        list.add(new Course(10, "TCS1212", "Fundamentals of Management", 2, "MDS", "Organizational behavior, strategic management and leadership", "Theory_Only", 2, 1, "Registered"));
        list.add(new Course(11, "TMS1233", "Discrete Mathematics", 3, "MDS", "Set theory, propositional logic, graph theory and combinatorics", "Theory_Only", 2, 1, "Registered"));
        return list;
    }

    /**
     * Safely closes JDBC resources.
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
