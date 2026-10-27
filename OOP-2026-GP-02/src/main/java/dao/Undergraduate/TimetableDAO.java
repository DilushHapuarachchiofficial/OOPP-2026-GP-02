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
 * assigned lecture halls/labs, course details, and lecturers.
 *
 * Implements parameterized JDBC queries and safe connection closure,
 * with complete offline fallback support to ensure UI continuity.
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
                System.err.println("Database Notice: Could not connect to MySQL. Using academic preview timetable.");
                return getFallbackWeeklyTimetable();
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
                // If query returns empty for specific parameters, fallback to all entries in database
                return fetchAllDatabaseEntries(conn);
            }

        } catch (SQLException e) {
            System.err.println("Database Notice: Error retrieving timetable: " + e.getMessage());
            return getFallbackWeeklyTimetable();
        } finally {
            closeQuietly(rs, stmt, conn);
        }

        return list.isEmpty() ? getFallbackWeeklyTimetable() : list;
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
        return list.isEmpty() ? getFallbackWeeklyTimetable() : list;
    }

    /**
     * Fallback weekly timetable for Level II - Semester I (matching faculty database records).
     */
    public List<Schedule> getFallbackWeeklyTimetable() {
        List<Schedule> list = new ArrayList<>();

        // Monday
        list.add(new Schedule(1, "Monday", "09:00 AM", "11:00 AM", "ICT Lab 11", SessionType.THEORY,
                "ICT2113", "Data Structures and Algorithms", "Dr. Kasun Fernando"));
        list.add(new Schedule(2, "Monday", "11:00 AM", "01:00 PM", "ICT Lab 11", SessionType.PRACTICAL,
                "ICT2113", "Data Structures and Algorithms", "Dr. Kasun Fernando"));

        // Tuesday
        list.add(new Schedule(3, "Tuesday", "09:00 AM", "12:00 PM", "ICT New Lab", SessionType.THEORY,
                "ICT2142", "E-Business Systems", "Dr. Priyantha Dissanayake"));
        list.add(new Schedule(4, "Tuesday", "01:00 PM", "03:00 PM", "ICT Lab 12", SessionType.THEORY,
                "ICT2152", "Object Oriented Analysis and Design", "Prof. Nadeeka Jayawardena"));

        // Wednesday
        list.add(new Schedule(5, "Wednesday", "08:00 AM", "09:00 AM", "ICT ILT", SessionType.THEORY,
                "TCS2112", "Business Economics", "Ms. Kumari Senanayake"));
        list.add(new Schedule(6, "Wednesday", "10:00 AM", "12:00 PM", "ICT Hardware Lab", SessionType.PRACTICAL,
                "TCS2121", "Soft Skills", "Mr. Saman Rathnayake"));

        // Thursday
        list.add(new Schedule(7, "Thursday", "09:00 AM", "11:00 AM", "ICT Lab 11", SessionType.THEORY,
                "ICT2122", "Object Oriented Programming", "Dr. Kasun Fernando"));
        list.add(new Schedule(8, "Thursday", "11:00 AM", "01:00 PM", "ICT Lab 11", SessionType.PRACTICAL,
                "ICT2132", "Object Oriented Programming Practicum", "Dr. Kasun Fernando"));
        list.add(new Schedule(9, "Thursday", "02:00 PM", "04:00 PM", "ICT Lab 11", SessionType.PRACTICAL,
                "ICT2132", "Object Oriented Programming Practicum", "Dr. Kasun Fernando"));

        // Friday
        list.add(new Schedule(10, "Friday", "08:00 AM", "10:00 AM", "ICT Hardware Lab", SessionType.THEORY,
                "TCS2112", "Business Economics", "Ms. Kumari Senanayake"));
        list.add(new Schedule(11, "Friday", "10:00 AM", "12:00 PM", "ICT New Lab", SessionType.THEORY,
                "ICT2162", "Management Information Systems", "Dr. Anura Bandara"));
        list.add(new Schedule(12, "Friday", "01:00 PM", "03:00 PM", "ICT Hardware Lab", SessionType.THEORY,
                "ENG2122", "English III", "Dr. Dilhani Gunasekara"));

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
