package main.java.model.admin;

import java.util.Date;

public class Timetable {
    private int timetableId;
    private int departmentId;
    private int academicYear;
    private int semester;
    private Date publishedDate;

    private String departmentName;

    public Timetable(int timetableId, int departmentId, int academicYear, int semester, Date publishedDate) {
        this.timetableId = timetableId;
        this.departmentId = departmentId;
        this.academicYear = academicYear;
        this.semester = semester;
        this.publishedDate = publishedDate;
    }

    public int getTimetableId() { return timetableId; }
    public void setTimetableId(int timetableId) { this.timetableId = timetableId; }

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

    public int getAcademicYear() { return academicYear; }
    public void setAcademicYear(int academicYear) { this.academicYear = academicYear; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public Date getPublishedDate() { return publishedDate; }
    public void setPublishedDate(Date publishedDate) { this.publishedDate = publishedDate; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
}
