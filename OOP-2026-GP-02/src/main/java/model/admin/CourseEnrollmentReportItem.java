package main.java.model.admin;

public class CourseEnrollmentReportItem {
    private int courseId;
    private String courseCode;
    private String courseName;
    private String departmentName;
    private int academicYear;
    private int semester;
    private int credit;
    private String courseType;
    private int enrolledStudents;
    private int totalSessions;
    private int totalEvalComponents;
    private double caWeight;
    private double finalWeight;

    public CourseEnrollmentReportItem(int courseId, String courseCode, String courseName,
                                      String departmentName, int academicYear, int semester,
                                      int credit, String courseType, int enrolledStudents,
                                      int totalSessions, int totalEvalComponents,
                                      double caWeight, double finalWeight) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.departmentName = departmentName;
        this.academicYear = academicYear;
        this.semester = semester;
        this.credit = credit;
        this.courseType = courseType;
        this.enrolledStudents = enrolledStudents;
        this.totalSessions = totalSessions;
        this.totalEvalComponents = totalEvalComponents;
        this.caWeight = caWeight;
        this.finalWeight = finalWeight;
    }

    public int getCourseId() { return courseId; }
    public String getCourseCode() { return courseCode; }
    public String getCourseName() { return courseName; }
    public String getDepartmentName() { return departmentName; }
    public int getAcademicYear() { return academicYear; }
    public int getSemester() { return semester; }
    public int getCredit() { return credit; }
    public String getCourseType() { return courseType; }
    public int getEnrolledStudents() { return enrolledStudents; }
    public int getTotalSessions() { return totalSessions; }
    public int getTotalEvalComponents() { return totalEvalComponents; }
    public double getCaWeight() { return caWeight; }
    public double getFinalWeight() { return finalWeight; }
}
