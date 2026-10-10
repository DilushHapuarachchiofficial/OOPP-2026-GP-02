package main.java.model.admin;

public class Course {
    private int courseId;
    private int departmentId;
    private String courseCode;
    private String courseName;
    private int credit;
    private String courseType;
    private int semester;
    private int academicYear;

    public Course(int courseId, int departmentId, String courseCode, String courseName, int credit, String courseType, int semester, int academicYear) {
        this.courseId = courseId;
        this.departmentId = departmentId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credit = credit;
        this.courseType = courseType;
        this.semester = semester;
        this.academicYear = academicYear;
    }

    public int getCourseId() { return courseId; }
    public int getDepartmentId() { return departmentId; }
    public String getCourseCode() { return courseCode; }
    public String getCourseName() { return courseName; }
    public int getCredit() { return credit; }
    public String getCourseType() { return courseType; }
    public int getSemester() { return semester; }
    public int getAcademicYear() { return academicYear; }
}
