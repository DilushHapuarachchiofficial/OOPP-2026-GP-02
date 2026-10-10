package main.java.dao.admin;

import main.java.model.admin.Timetable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TimetableDAO {

    public static List<Timetable> getAllTimetables() {
        List<Timetable> timetables = new ArrayList<>();
        String query = "SELECT * FROM timetables ORDER BY timetable_id DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
             
            while (rs.next()) {
                Timetable t = new Timetable(
                    rs.getInt("timetable_id"),
                    rs.getInt("department_id"),
                    rs.getInt("academic_year"),
                    rs.getInt("semester"),
                    rs.getDate("published_date")
                );
                timetables.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return timetables;
    }

    public static boolean addTimetable(int departmentId, int year, int semester) {
        String query = "INSERT INTO timetables (department_id, academic_year, semester, published_date) VALUES (?, ?, ?, CURDATE())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, departmentId);
            stmt.setInt(2, year);
            stmt.setInt(3, semester);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteTimetable(int timetableId) {
        String query = "DELETE FROM timetables WHERE timetable_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, timetableId);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
