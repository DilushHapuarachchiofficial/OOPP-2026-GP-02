package main.java.dao.admin;

import main.java.model.admin.Course;
import main.java.model.admin.CourseLecturerAssignment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    public static class LecturerOption {
        private final int lecturerId;
        private final String lecturerName;
        private final String designation;
        private final String departmentName;

        public LecturerOption(int lecturerId, String lecturerName, String designation, String departmentName) {
            this.lecturerId = lecturerId;
            this.lecturerName = lecturerName;
            this.designation = designation;
            this.departmentName = departmentName;
        }

        public int getLecturerId() { return lecturerId; }
        public String getLecturerName() { return lecturerName; }
        public String getDesignation() { return designation; }
        public String getDepartmentName() { return departmentName; }

        @Override
        public String toString() {
            String des = (designation != null && !designation.trim().isEmpty()) ? " (" + designation.trim() + ")" : "";
            return lecturerName + des;
        }
    }

    public static List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String query = "SELECT * FROM courses ORDER BY course_id ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
             
            while (rs.next()) {
                Course course = new Course(
                    rs.getInt("course_id"),
                    rs.getInt("department_id"),
                    rs.getString("course_code"),
                    rs.getString("course_name"),
                    rs.getInt("credit"),
                    rs.getString("course_type"),
                    rs.getInt("semester"),
                    rs.getInt("academic_year")
                );
                courses.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    public static List<Course> getCoursesByFilter(Integer departmentId, Integer academicYear, Integer semester) {
        List<Course> courses = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT * FROM courses WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (academicYear != null && academicYear > 0) {
            query.append(" AND academic_year = ?");
            params.add(academicYear);
        }
        if (semester != null && semester > 0) {
            query.append(" AND semester = ?");
            params.add(semester);
        }
        if (departmentId != null && departmentId > 0) {
            // Also allow department 4 (multidisciplinary) courses if applicable
            query.append(" AND (department_id = ? OR department_id = 4)");
            params.add(departmentId);
        }
        query.append(" ORDER BY course_code ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {
             
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Course course = new Course(
                        rs.getInt("course_id"),
                        rs.getInt("department_id"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getInt("credit"),
                        rs.getString("course_type"),
                        rs.getInt("semester"),
                        rs.getInt("academic_year")
                    );
                    courses.add(course);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    public static boolean addCourse(int departmentId, String courseCode, String courseName, int credit, String courseType, int semester, int academicYear, String materialPath) {
        String nextCourseIdQuery = "SELECT COALESCE(MAX(course_id), 0) + 1 FROM courses";
        String nextMatIdQuery = "SELECT COALESCE(MAX(material_id), 0) + 1 FROM course_materials";
        String insertCourseQuery = "INSERT INTO courses (course_id, department_id, course_code, course_name, credit, course_type, semester, academic_year) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String insertMatQuery = "INSERT INTO course_materials (material_id, course_id, lecturer_id, material_title, file_path) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                int nextCourseId = 1;
                try (PreparedStatement stmt = conn.prepareStatement(nextCourseIdQuery);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) nextCourseId = rs.getInt(1);
                }
                
                try (PreparedStatement stmt = conn.prepareStatement(insertCourseQuery)) {
                    stmt.setInt(1, nextCourseId);
                    stmt.setInt(2, departmentId);
                    stmt.setString(3, courseCode);
                    stmt.setString(4, courseName);
                    stmt.setInt(5, credit);
                    stmt.setString(6, courseType);
                    stmt.setInt(7, semester);
                    stmt.setInt(8, academicYear);
                    stmt.executeUpdate();
                }
                
                if (materialPath != null && !materialPath.trim().isEmpty()) {
                    int nextMatId = 1;
                    try (PreparedStatement stmt = conn.prepareStatement(nextMatIdQuery);
                          ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) nextMatId = rs.getInt(1);
                    }
                    
                    try (PreparedStatement stmt = conn.prepareStatement(insertMatQuery)) {
                        stmt.setInt(1, nextMatId);
                        stmt.setInt(2, nextCourseId);
                        stmt.setInt(3, 4); // Default lecturer_id for uploaded material
                        stmt.setString(4, courseName + " Material");
                        stmt.setString(5, materialPath);
                        stmt.executeUpdate();
                    }
                }
                
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteCourse(int courseId) {
        String query = "DELETE FROM courses WHERE course_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, courseId);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================================
    // COURSE-LECTURER ASSIGNMENT OPERATIONS
    // =========================================================================

    /**
     * Retrieves all registered academic lecturers for assignment dropdown.
     */
    public static List<LecturerOption> getAllLecturers() {
        List<LecturerOption> list = new ArrayList<>();
        String query = "SELECT l.lecturer_id, l.lecturer_name, l.designation, " +
                       "       COALESCE(d.department_name, 'Faculty of Technology') AS department_name " +
                       "FROM lecturers l " +
                       "LEFT JOIN departments d ON l.department_id = d.department_id " +
                       "ORDER BY l.lecturer_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new LecturerOption(
                    rs.getInt("lecturer_id"),
                    rs.getString("lecturer_name"),
                    rs.getString("designation"),
                    rs.getString("department_name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Retrieves all current course lecturer assignments from the database.
     */
    public static List<CourseLecturerAssignment> getAllCourseLecturerAssignments() {
        List<CourseLecturerAssignment> list = new ArrayList<>();
        String query = "SELECT cl.assignment_id, c.course_id, c.course_code, c.course_name, " +
                       "       l.lecturer_id, l.lecturer_name, l.designation, " +
                       "       COALESCE(d.department_name, 'Faculty of Technology') AS department_name " +
                       "FROM course_lecturers cl " +
                       "JOIN courses c ON cl.course_id = c.course_id " +
                       "JOIN lecturers l ON cl.lecturer_id = l.lecturer_id " +
                       "LEFT JOIN departments d ON l.department_id = d.department_id " +
                       "ORDER BY c.course_code ASC, l.lecturer_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new CourseLecturerAssignment(
                    rs.getInt("assignment_id"),
                    rs.getInt("course_id"),
                    rs.getString("course_code"),
                    rs.getString("course_name"),
                    rs.getInt("lecturer_id"),
                    rs.getString("lecturer_name"),
                    rs.getString("designation"),
                    rs.getString("department_name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Checks if a specific lecturer is already assigned to a course.
     */
    public static boolean isLecturerAssignedToCourse(int courseId, int lecturerId) {
        String query = "SELECT COUNT(*) FROM course_lecturers WHERE course_id = ? AND lecturer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, courseId);
            stmt.setInt(2, lecturerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Assigns a registered lecturer to a registered course in the database.
     */
    public static boolean assignLecturerToCourse(int courseId, int lecturerId) {
        if (isLecturerAssignedToCourse(courseId, lecturerId)) {
            return false;
        }

        String insertQuery = "INSERT INTO course_lecturers (course_id, lecturer_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setInt(1, courseId);
            stmt.setInt(2, lecturerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // Fallback if assignment_id column requires explicit value
            try (Connection conn = DatabaseConnection.getConnection()) {
                int nextId = 1;
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COALESCE(MAX(assignment_id), 0) + 1 FROM course_lecturers")) {
                    if (rs.next()) nextId = rs.getInt(1);
                }
                try (PreparedStatement stmt2 = conn.prepareStatement("INSERT INTO course_lecturers (assignment_id, course_id, lecturer_id) VALUES (?, ?, ?)")) {
                    stmt2.setInt(1, nextId);
                    stmt2.setInt(2, courseId);
                    stmt2.setInt(3, lecturerId);
                    return stmt2.executeUpdate() > 0;
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                return false;
            }
        }
    }

    /**
     * Removes an assignment from course_lecturers table.
     */
    public static boolean removeLecturerAssignment(int assignmentId) {
        String query = "DELETE FROM course_lecturers WHERE assignment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, assignmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
