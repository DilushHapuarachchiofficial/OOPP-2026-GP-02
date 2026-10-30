package main.java.model;

/**
 * Model class representing a Department Timetable Schedule Slot in TecFAMS.
 * Manages Lecture Halls, Computer Laboratories, and Scheduled Timeslots.
 */
public class Timetable {
    private String timetableId;
    private String dayOfWeek;   // "Monday", "Tuesday", etc.
    private String timeSlot;    // "08:30 - 10:30"
    private String courseCode;  // "ICT2132"
    private String location;    // "Hardware Lab 1" or "LH 02"
    private String sessionType; // "Practical" or "Theory"
    private String department;

    private int entryId;
    private String courseName;
    private int academicYear;
    private int semester;

    public Timetable() {
    }

    public Timetable(String timetableId, String dayOfWeek, String timeSlot,
                     String courseCode, String location, String sessionType, String department) {
        this.timetableId = timetableId;
        this.dayOfWeek = dayOfWeek;
        this.timeSlot = timeSlot;
        this.courseCode = courseCode;
        this.location = location;
        this.sessionType = sessionType;
        this.department = department;
    }

    public Timetable(int entryId, String timetableId, String dayOfWeek, String timeSlot,
                     String courseCode, String courseName, String location, String sessionType,
                     int academicYear, int semester, String department) {
        this.entryId = entryId;
        this.timetableId = timetableId;
        this.dayOfWeek = dayOfWeek;
        this.timeSlot = timeSlot;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.location = location;
        this.sessionType = sessionType;
        this.academicYear = academicYear;
        this.semester = semester;
        this.department = department;
    }

    // Getters and Setters
    public int getEntryId() {
        return entryId;
    }

    public void setEntryId(int entryId) {
        this.entryId = entryId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public String getTimetableId() {
        return timetableId;
    }

    public void setTimetableId(String timetableId) {
        this.timetableId = timetableId;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return dayOfWeek + " " + timeSlot + " | " + courseCode + " (" + location + ", " + sessionType + ")";
    }
}
