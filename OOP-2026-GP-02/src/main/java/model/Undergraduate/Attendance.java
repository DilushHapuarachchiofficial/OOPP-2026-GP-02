package main.java.model.Undergraduate;

/**
 * Represents an undergraduate attendance record for a theory or practical session.
 * Complies with the TecFAMS UML class diagram.
 */
public class Attendance {
    private int attendanceId;
    private String courseCode;
    private String courseName;
    private String date;
    private AttendanceStatus status;
    private SessionType sessionType;
    private int sessionCount;

    public Attendance() {
    }

    public Attendance(int attendanceId, String courseCode, String courseName, String date,
                      AttendanceStatus status, SessionType sessionType, int sessionCount) {
        this.attendanceId = attendanceId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.date = date;
        this.status = status;
        this.sessionType = sessionType;
        this.sessionCount = sessionCount;
    }

    // Getters and Setters
    public int getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public int getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(int sessionCount) {
        this.sessionCount = sessionCount;
    }
}
