package main.java.model.Undergraduate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the academic timetable for an undergraduate semester.
 * Complies with the TecFAMS UML class diagram.
 */
public class Timetable {
    private int timetableId;
    private String semester;
    private String academicYear;
    private List<Schedule> schedules;

    public Timetable() {
        this.schedules = new ArrayList<>();
    }

    public Timetable(int timetableId, String semester, String academicYear) {
        this.timetableId = timetableId;
        this.semester = semester;
        this.academicYear = academicYear;
        this.schedules = new ArrayList<>();
    }

    public void addSchedule(Schedule schedule) {
        if (schedule != null) {
            this.schedules.add(schedule);
        }
    }

    public void updateSchedule(Schedule updated) {
        if (updated == null) return;
        for (int i = 0; i < schedules.size(); i++) {
            if (schedules.get(i).getScheduleId() == updated.getScheduleId()) {
                schedules.set(i, updated);
                return;
            }
        }
    }

    public void removeSchedule(int scheduleId) {
        schedules.removeIf(s -> s.getScheduleId() == scheduleId);
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = (schedules != null) ? schedules : new ArrayList<>();
    }

    /**
     * Filters and returns schedules for today's day of week.
     */
    public List<Schedule> getTodaySchedules() {
        String today = LocalDate.now().getDayOfWeek().toString(); // e.g., MONDAY
        List<Schedule> todayList = new ArrayList<>();
        for (Schedule s : schedules) {
            if (s.getDay() != null && s.getDay().equalsIgnoreCase(today)) {
                todayList.add(s);
            }
        }
        return todayList;
    }

    // Getters and Setters
    public int getTimetableId() {
        return timetableId;
    }

    public void setTimetableId(int timetableId) {
        this.timetableId = timetableId;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }
}
