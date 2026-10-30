package main.java.dao.Undergraduate;

import main.java.model.Undergraduate.Schedule;
import main.java.model.Undergraduate.SessionType;
import main.resources.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Academic Timetable & Lecture Schedules.
 * Handles database queries for weekly lecture schedules, session time slots,
 * assigned lecture halls/labs, course details, and lecturers directly from TecFAMS MySQL database.
 */
public class TimetableDAO {

    /**
     * Retrieves the complete weekly lecture schedule for the specified academic year and semester.
     * Defaults to Level II - Semester I (Academic Year 2, Semester 1).
     *
     * @param academicYear Academic year level (e.g., 2 for Level II).
     * @param semester     Semester number (e.g., 1 for Semester I).
     * @return List of Schedule objects ordered chronologically by day and start time.
     */
    public List<Schedule> getWeeklyTimetable(int academicYear, int semester) {
        List<Schedule> list = new ArrayList<>();

        String query = "SELECT te.entry_id, te.day_of_week, " +
                "TIME_FORMAT(te.start_time, '%h:%i %p') AS start_time_str, " +
                "TIME_FORMAT(te.end_time, '%h:%i %p') AS end_time_str, " +
                "te.hall, te.session_type, c.course_code, c.course_name, " +
                "COALESCE((SELECT l.lecturer_name FROM lecturers l WHERE l.department_id = c.department_id LIMIT 1), 'Faculty Lecturer') AS lecturer_name " +
                "FROM timetable_entries te " +
                "INNER JOIN courses c ON te.course_id = c.course_id " +
                "INNER JOIN timetables t ON te.timetable_id = t.timetable_id " +
                "WHERE t.academic_year = ? AND t.semester = ? " +
                "ORDER BY FIELD(te.day_of_week, 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'), te.start_time ASC";

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
            stmt.setInt(1, academicYear);
            stmt.setInt(2, semester);
            rs = stmt.executeQuery();

            while (rs.next()) {
                SessionType type = SessionType.THEORY;
                String typeStr = rs.getString("session_type");
                if (typeStr != null && typeStr.equalsIgnoreCase("PRACTICAL")) {
                    type = SessionType.PRACTICAL;
                }

                Schedule schedule = new Schedule(
                        rs.getInt("entry_id"),
                        rs.getString("day_of_week"),
                        rs.getString("start_time_str"),
                        rs.getString("end_time_str"),
                        rs.getString("hall"),
                        type,
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getString("lecturer_name")
                );
                list.add(schedule);
            }

            if (list.isEmpty()) {
                // Fetch active database entries if any exist
                return fetchAllDatabaseEntries(conn);
            }

        } catch (SQLException e) {
            System.err.println("Database Error: Error retrieving timetable: " + e.getMessage());
        } finally {
            closeQuietly(rs, stmt, conn);
        }

        return list;
    }

    /**
     * Default method to fetch Level II - Semester I timetable.
     */
    public List<Schedule> getWeeklyTimetable() {
        return getWeeklyTimetable(2, 1);
    }

    /**
     * Retrieves lectures scheduled for a specific day of the week.
     */
    public List<Schedule> getTimetableForDay(String day, int academicYear, int semester) {
        List<Schedule> all = getWeeklyTimetable(academicYear, semester);
        if (day == null || day.trim().isEmpty() || day.equalsIgnoreCase("All") || day.equalsIgnoreCase("All Days")) {
            return all;
        }

        List<Schedule> filtered = new ArrayList<>();
        for (Schedule s : all) {
            if (s.getDay() != null && s.getDay().equalsIgnoreCase(day.trim())) {
                filtered.add(s);
            }
        }
        return filtered;
    }

    private List<Schedule> fetchAllDatabaseEntries(Connection conn) {
        List<Schedule> list = new ArrayList<>();
        if (conn == null) return list;

        String query = "SELECT te.entry_id, te.day_of_week, " +
                "TIME_FORMAT(te.start_time, '%h:%i %p') AS start_time_str, " +
                "TIME_FORMAT(te.end_time, '%h:%i %p') AS end_time_str, " +
                "te.hall, te.session_type, c.course_code, c.course_name, " +
                "COALESCE((SELECT l.lecturer_name FROM lecturers l WHERE l.department_id = c.department_id LIMIT 1), 'Faculty Lecturer') AS lecturer_name " +
                "FROM timetable_entries te " +
                "INNER JOIN courses c ON te.course_id = c.course_id " +
                "ORDER BY FIELD(te.day_of_week, 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'), te.start_time ASC";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                SessionType type = SessionType.THEORY;
                String typeStr = rs.getString("session_type");
                if (typeStr != null && typeStr.equalsIgnoreCase("PRACTICAL")) {
                    type = SessionType.PRACTICAL;
                }

                Schedule schedule = new Schedule(
                        rs.getInt("entry_id"),
                        rs.getString("day_of_week"),
                        rs.getString("start_time_str"),
                        rs.getString("end_time_str"),
                        rs.getString("hall"),
                        type,
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getString("lecturer_name")
                );
                list.add(schedule);
            }
        } catch (SQLException ignored) {}
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
