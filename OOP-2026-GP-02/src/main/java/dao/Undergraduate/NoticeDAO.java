package main.java.dao.Undergraduate;

import main.java.model.Undergraduate.Notice;
import main.resources.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Faculty Notices and Announcements.
 * Queries notices published for undergraduates directly from TecFAMS MySQL database.
 */
public class NoticeDAO {

    /**
     * Retrieves all active notices for undergraduates from database,
     * sorted with the newest notices at the top.
     *
     * @return List of Notice objects ordered by published_date DESC, notice_id DESC.
     */
    public List<Notice> getAllNotices() {
        List<Notice> list = new ArrayList<>();

        String query = "SELECT n.notice_id, n.title, n.description, " +
                "DATE_FORMAT(n.published_date, '%Y-%m-%d %h:%i %p') AS pub_date_str, " +
                "n.target_audience, " +
                "COALESCE(a.name, u.username, 'Faculty Administration') AS publisher_name " +
                "FROM notices n " +
                "LEFT JOIN admins a ON n.published_by = a.admin_id " +
                "LEFT JOIN users u ON n.published_by = u.user_id " +
                "WHERE n.target_audience IN ('All', 'Students') " +
                "ORDER BY n.published_date DESC, n.notice_id DESC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.err.println("Database Error: Could not connect to MySQL database.");
                return list;
            }

            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Notice notice = new Notice(
                        rs.getInt("notice_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("pub_date_str"),
                        null,
                        rs.getString("target_audience"),
                        rs.getString("publisher_name")
                );
                list.add(notice);
            }
        } catch (SQLException e) {
            System.err.println("Database Error: Error retrieving notices: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }

        return list;
    }

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
