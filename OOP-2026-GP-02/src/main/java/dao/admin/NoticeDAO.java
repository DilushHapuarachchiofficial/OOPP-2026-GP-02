package dao;

import model.Notice;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoticeDAO {

    public static List<Notice> getAllNotices() {
        List<Notice> notices = new ArrayList<>();
        String query = "SELECT * FROM notices ORDER BY notice_id DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
             
            while (rs.next()) {
                Notice notice = new Notice(
                    rs.getInt("notice_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getTimestamp("published_date"),
                    rs.getString("target_audience"),
                    rs.getInt("published_by"),
                    rs.getString("attachment_path")
                );
                notices.add(notice);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notices;
    }

    public static boolean addNotice(String title, String description, String targetAudience, int publishedBy, String attachmentPath) {
        String maxIdQuery = "SELECT COALESCE(MAX(notice_id), 0) + 1 FROM notices";
        String query = "INSERT INTO notices (notice_id, title, description, target_audience, published_by, attachment_path) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            int nextId = 1;
            try (PreparedStatement stmt = conn.prepareStatement(maxIdQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) nextId = rs.getInt(1);
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, nextId);
                stmt.setString(2, title);
                stmt.setString(3, description);
                stmt.setString(4, targetAudience);
                stmt.setInt(5, publishedBy);
                stmt.setString(6, attachmentPath);
                
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean deleteNotice(int noticeId) {
        String query = "DELETE FROM notices WHERE notice_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, noticeId);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
