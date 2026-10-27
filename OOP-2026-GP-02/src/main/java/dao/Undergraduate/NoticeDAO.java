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
 * Queries notices published for undergraduates, ordered chronologically with the
 * newest notices always appearing at the top.
 *
 * Implements parameterized JDBC queries and safe connection closure,
 * with full offline fallback support to ensure UI continuity.
 */
public class NoticeDAO {

    /**
     * Retrieves all active notices for undergraduates, sorted with the newest notices at the top.
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
                System.err.println("Database Notice: Could not connect to MySQL. Using academic preview notices.");
                return getFallbackNotices();
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

            if (list.isEmpty()) {
                return getFallbackNotices();
            }

        } catch (SQLException e) {
            System.err.println("Database Notice: Error retrieving notices: " + e.getMessage());
            return getFallbackNotices();
        } finally {
            closeQuietly(rs, stmt, conn);
        }

        return list;
    }

    /**
     * Fallback notices displayed if database is temporarily offline,
     * ordered with the newest notices at the top.
     */
    public List<Notice> getFallbackNotices() {
        List<Notice> list = new ArrayList<>();

        list.add(new Notice(
                5,
                "Mid-Semester Examination Schedule & Regulations Released",
                "The examination timetable and hall arrangements for Level II Semester I mid-semester examinations have been published. All students are advised to check their respective examination slots and bring their Student ID card.",
                "2026-10-18 09:30 AM",
                "2026-10-31",
                "Students",
                "Senior Assistant Registrar / Examination Division"
        ));

        list.add(new Notice(
                4,
                "Submission of Medical Certificates for Missed Sessions",
                "Undergraduates who missed lecture or practical sessions due to illness must submit official medical certificates certified by the University Medical Officer (UMO) to the Department Technical Officer within 14 days.",
                "2026-10-12 11:15 AM",
                "2026-10-26",
                "Students",
                "Dean's Office / Faculty of Technology"
        ));

        list.add(new Notice(
                3,
                "Final Continuous Assessment (CA) Marks Verification Notice",
                "Continuous Assessment (CA) marks for ICT2113, ICT2122, and ENG2122 have been uploaded. Undergraduates are requested to verify their component scores and submit any inquiries before the final examination eligibility cutoff.",
                "2026-10-09 02:00 PM",
                "2026-10-23",
                "Students",
                "Department of ICT / Course Coordinators"
        ));

        list.add(new Notice(
                2,
                "Commencement of Level II Semester I Academic Activities",
                "All Level II undergraduates are hereby notified that the academic timetable has been published and lectures will commence as scheduled. Minimum 80% attendance is strictly required for final examination eligibility.",
                "2026-10-07 08:00 AM",
                "2026-10-21",
                "All",
                "Faculty Administration"
        ));

        list.add(new Notice(
                1,
                "Library Digital Resources & IEEE Xplore Access Credentials",
                "Students can now access university library online databases and IEEE Xplore digital library using their institutional student email credentials. For assistance, contact the Faculty Computer Center.",
                "2026-10-01 10:00 AM",
                "2026-10-30",
                "All",
                "Faculty Librarian"
        ));

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
