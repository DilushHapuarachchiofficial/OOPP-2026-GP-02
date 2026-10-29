package main.java.model.admin;

public class TimetableEntry {
    private int entryId;
    private int timetableId;
    private int courseId;
    private String courseCode;
    private String courseName;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String hall;
    private String sessionType;

    public TimetableEntry() {}

    public TimetableEntry(int entryId, int timetableId, int courseId, String courseCode, 
                          String courseName, String dayOfWeek, String startTime, 
                          String endTime, String hall, String sessionType) {
        this.entryId = entryId;
        this.timetableId = timetableId;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.hall = hall;
        this.sessionType = sessionType;
    }

    public int getEntryId() { return entryId; }
    public void setEntryId(int entryId) { this.entryId = entryId; }

    public int getTimetableId() { return timetableId; }
    public void setTimetableId(int timetableId) { this.timetableId = timetableId; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getHall() { return hall; }
    public void setHall(String hall) { this.hall = hall; }

    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }

    public String getTimeSlotFormatted() {
        String start = formatTimeSimple(startTime);
        String end = formatTimeSimple(endTime);
        return start + " - " + end;
    }

    private String formatTimeSimple(String t) {
        if (t == null) return "";
        t = t.trim();
        if (t.length() >= 5) {
            String sub = t.substring(0, 5);
            // remove leading zero if needed, e.g. 08:00 -> 8:00
            if (sub.startsWith("0")) {
                return sub.substring(1);
            }
            return sub;
        }
        return t;
    }
}
