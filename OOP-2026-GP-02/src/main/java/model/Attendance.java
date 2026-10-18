package main.java.model;

import java.time.LocalDate;

/**
 * Model class representing an Attendance record.
 * Supports Theory and Practical session tracking up to 15 sessions per module.
 */
public class Attendance {
    private String attendanceId;
    private String studentId;
    private String courseCode;
    private int sessionNumber; // 1 to 15
    private String sessionType; // "Theory" or "Practical"
    private String status;      // "Present" or "Absent"
    private LocalDate date;
    private String markedBy;    // Technical Officer ID

    public Attendance() {
        this.date = LocalDate.now();
        this.status = "Present";
    }

    public Attendance(String attendanceId, String studentId, String courseCode,
                      int sessionNumber, String sessionType, String status,
                      LocalDate date, String markedBy) {
        this.attendanceId = attendanceId;
        this.studentId = studentId;
        this.courseCode = courseCode;
        this.sessionNumber = sessionNumber;
        this.sessionType = sessionType;
        this.status = status;
        this.date = date;
        this.markedBy = markedBy;
    }

    public boolean isPresent() {
        return "Present".equalsIgnoreCase(status);
    }

    // Getters and Setters
    public String getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(String attendanceId) {
        this.attendanceId = attendanceId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public int getSessionNumber() {
        return sessionNumber;
    }

    public void setSessionNumber(int sessionNumber) {
        this.sessionNumber = sessionNumber;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getMarkedBy() {
        return markedBy;
    }

    public void setMarkedBy(String markedBy) {
        this.markedBy = markedBy;
    }

    @Override
    public String toString() {
        return "Attendance{" +
                "id='" + attendanceId + '\'' +
                ", student='" + studentId + '\'' +
                ", course='" + courseCode + '\'' +
                ", session=" + sessionNumber + " (" + sessionType + ")" +
                ", status='" + status + '\'' +
                ", date=" + date +
                '}';
    }
}
