package main.java.model.Undergraduate;

/**
 * Model representing an absent/missed lecture session for a student.
 * Used in the Medical Details submission dropdown.
 */
public class MissedSession {
    private final int sessionId;
    private final String courseCode;
    private final String courseName;
    private final int sessionNumber;
    private final String sessionDate;
    private final String sessionType;

    public MissedSession(int sessionId, String courseCode, String courseName, int sessionNumber, String sessionDate, String sessionType) {
        this.sessionId = sessionId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.sessionNumber = sessionNumber;
        this.sessionDate = sessionDate;
        this.sessionType = sessionType;
    }

    public int getSessionId() {
        return sessionId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public int getSessionNumber() {
        return sessionNumber;
    }

    public String getSessionDate() {
        return sessionDate;
    }

    public String getSessionType() {
        return sessionType;
    }

    @Override
    public String toString() {
        return courseCode + " - " + courseName + " | Session #" + String.format("%02d", sessionNumber) + " (" + sessionDate + " - " + sessionType + ")";
    }
}
