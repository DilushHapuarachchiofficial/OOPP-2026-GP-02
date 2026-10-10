package dao;

import model.Course;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

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
}
