package model;

import java.util.Date;

public class Timetable {
    private int timetableId;
    private int departmentId;
    private int academicYear;
    private int semester;
    private Date publishedDate;

    public Timetable(int timetableId, int departmentId, int academicYear, int semester, Date publishedDate) {
        this.timetableId = timetableId;
        this.departmentId = departmentId;
        this.academicYear = academicYear;
        this.semester = semester;
        this.publishedDate = publishedDate;
    }

    public int getTimetableId() { return timetableId; }
    public int getDepartmentId() { return departmentId; }
    public int getAcademicYear() { return academicYear; }
    public int getSemester() { return semester; }
    public Date getPublishedDate() { return publishedDate; }
}
