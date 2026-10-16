package main.java.model.Undergraduate;

/**
 * Represents a scheduled lecture session slot in the timetable.
 * Complies with the TecFAMS UML class diagram.
 */
public class Schedule {
    private int scheduleId;
    private String day;
    private String startTime;
    private String endTime;
    private String location;
    private SessionType sessionType;
    private String courseCode;
    private String courseName;
    private String lecturerName;

    public Schedule() {
    }

    public Schedule(int scheduleId, String day, String startTime, String endTime,
                    String location, SessionType sessionType,
                    String courseCode, String courseName, String lecturerName) {
        this.scheduleId = scheduleId;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.sessionType = sessionType;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.lecturerName = lecturerName;
    }

    /**
     * Calculates session duration in hours based on start and end time.
     * Default per SRS: 2.0 hours for 1 credit theory or practical.
     */
    public double getDuration() {
        return 2.0;
    }

    // Getters and Setters
    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
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

    public String getLecturerName() {
        return lecturerName;
    }

    public void setLecturerName(String lecturerName) {
        this.lecturerName = lecturerName;
    }
}
