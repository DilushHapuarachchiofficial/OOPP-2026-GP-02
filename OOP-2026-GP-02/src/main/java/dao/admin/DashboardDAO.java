package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DashboardDAO {

    public static Map<String, Integer> getDashboardStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalUsers", getCount("users"));
        stats.put("totalCourses", getCount("courses"));
        stats.put("totalNotices", getCount("notices"));
        stats.put("totalTimetables", getCount("timetables"));
        stats.put("totalUndergraduates", getCount("students"));
        stats.put("totalLecturers", getCount("lecturers"));
        stats.put("totalTechnicalOfficers", getCount("technical_officers"));
        return stats;
    }

    private static int getCount(String tableName) {
        int count = 0;
        String query = "SELECT COUNT(*) FROM " + tableName; // Safe string concat because table name is hardcoded above
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
}
