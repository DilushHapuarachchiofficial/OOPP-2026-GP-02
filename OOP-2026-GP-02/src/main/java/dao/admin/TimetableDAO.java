package main.java.dao.admin;

import main.java.model.admin.Timetable;
import main.java.model.admin.TimetableEntry;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TimetableDAO {

    public static List<Timetable> getAllTimetables() {
        List<Timetable> timetables = new ArrayList<>();
        String query = "SELECT t.*, d.department_name FROM timetables t " +
                       "LEFT JOIN departments d ON t.department_id = d.department_id " +
                       "ORDER BY t.timetable_id DESC";
        
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
                t.setDepartmentName(rs.getString("department_name"));
                timetables.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return timetables;
    }

    public static Timetable getTimetable(int departmentId, int academicYear, int semester) {
        String query = "SELECT t.*, d.department_name FROM timetables t " +
                       "LEFT JOIN departments d ON t.department_id = d.department_id " +
                       "WHERE t.department_id = ? AND t.academic_year = ? AND t.semester = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, departmentId);
            stmt.setInt(2, academicYear);
            stmt.setInt(3, semester);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timetable t = new Timetable(
                        rs.getInt("timetable_id"),
                        rs.getInt("department_id"),
                        rs.getInt("academic_year"),
                        rs.getInt("semester"),
                        rs.getDate("published_date")
                    );
                    t.setDepartmentName(rs.getString("department_name"));
                    return t;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getOrCreateTimetable(int departmentId, int academicYear, int semester) {
        Timetable existing = getTimetable(departmentId, academicYear, semester);
        if (existing != null) {
            return existing.getTimetableId();
        }
        return addTimetableAndGetId(departmentId, academicYear, semester);
    }

    public static int addTimetableAndGetId(int departmentId, int year, int semester) {
        String query = "INSERT INTO timetables (department_id, academic_year, semester, published_date) VALUES (?, ?, ?, CURDATE())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, departmentId);
            stmt.setInt(2, year);
            stmt.setInt(3, semester);
            
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean addTimetable(int departmentId, int year, int semester) {
        return addTimetableAndGetId(departmentId, year, semester) > 0;
    }

    public static boolean deleteTimetable(int timetableId) {
        String deleteEntries = "DELETE FROM timetable_entries WHERE timetable_id = ?";
        String deleteTimetable = "DELETE FROM timetables WHERE timetable_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt1 = conn.prepareStatement(deleteEntries)) {
                    stmt1.setInt(1, timetableId);
                    stmt1.executeUpdate();
                }
                boolean success;
                try (PreparedStatement stmt2 = conn.prepareStatement(deleteTimetable)) {
                    stmt2.setInt(1, timetableId);
                    success = stmt2.executeUpdate() > 0;
                }
                conn.commit();
                return success;
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

    public static List<TimetableEntry> getEntriesByTimetable(int timetableId) {
        List<TimetableEntry> entries = new ArrayList<>();
        String query = "SELECT te.*, c.course_code, c.course_name " +
                       "FROM timetable_entries te " +
                       "LEFT JOIN courses c ON te.course_id = c.course_id " +
                       "WHERE te.timetable_id = ? " +
                       "ORDER BY te.day_of_week, te.start_time";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, timetableId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TimetableEntry entry = new TimetableEntry(
                        rs.getInt("entry_id"),
                        rs.getInt("timetable_id"),
                        rs.getInt("course_id"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getString("day_of_week"),
                        rs.getString("start_time"),
                        rs.getString("end_time"),
                        rs.getString("hall"),
                        rs.getString("session_type")
                    );
                    entries.add(entry);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

    public static List<TimetableEntry> getEntries(int departmentId, int academicYear, int semester) {
        if (departmentId > 0 && semester > 0 && academicYear > 0) {
            Timetable t = getTimetable(departmentId, academicYear, semester);
            if (t == null) {
                return new ArrayList<>();
            }
            return getEntriesByTimetable(t.getTimetableId());
        }

        // Support 'All Departments' (departmentId == 0) and/or 'All Semesters' (semester == 0)
        List<TimetableEntry> entries = new ArrayList<>();
        StringBuilder query = new StringBuilder(
            "SELECT te.*, c.course_code, c.course_name " +
            "FROM timetable_entries te " +
            "JOIN timetables t ON te.timetable_id = t.timetable_id " +
            "LEFT JOIN courses c ON te.course_id = c.course_id " +
            "WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (academicYear > 0) {
            query.append(" AND t.academic_year = ?");
            params.add(academicYear);
        }
        if (departmentId > 0) {
            query.append(" AND t.department_id = ?");
            params.add(departmentId);
        }
        if (semester > 0) {
            query.append(" AND t.semester = ?");
            params.add(semester);
        }
        query.append(" ORDER BY te.day_of_week, te.start_time");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TimetableEntry entry = new TimetableEntry(
                        rs.getInt("entry_id"),
                        rs.getInt("timetable_id"),
                        rs.getInt("course_id"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getString("day_of_week"),
                        rs.getString("start_time"),
                        rs.getString("end_time"),
                        rs.getString("hall"),
                        rs.getString("session_type")
                    );
                    entries.add(entry);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

    public static boolean addEntry(int timetableId, int courseId, String dayOfWeek, 
                                   String startTime, String endTime, String hall, String sessionType) {
        String query = "INSERT INTO timetable_entries (timetable_id, course_id, day_of_week, start_time, end_time, hall, session_type) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, timetableId);
            stmt.setInt(2, courseId);
            stmt.setString(3, dayOfWeek);
            stmt.setString(4, startTime);
            stmt.setString(5, endTime);
            stmt.setString(6, hall);
            stmt.setString(7, sessionType);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateEntry(int entryId, int courseId, String dayOfWeek, 
                                      String startTime, String endTime, String hall, String sessionType) {
        String query = "UPDATE timetable_entries SET course_id = ?, day_of_week = ?, start_time = ?, end_time = ?, hall = ?, session_type = ? " +
                       "WHERE entry_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, courseId);
            stmt.setString(2, dayOfWeek);
            stmt.setString(3, startTime);
            stmt.setString(4, endTime);
            stmt.setString(5, hall);
            stmt.setString(6, sessionType);
            stmt.setInt(7, entryId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteEntry(int entryId) {
        String query = "DELETE FROM timetable_entries WHERE entry_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, entryId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
