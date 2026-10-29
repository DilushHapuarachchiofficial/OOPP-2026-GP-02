package main.java.model.admin;

public class DepartmentStaffReportItem {
    private int departmentId;
    private String departmentCode;
    private String departmentName;
    private int lecturerCount;
    private int technicalOfficerCount;
    private int studentCount;
    private int courseCount;
    private double studentLecturerRatio;

    public DepartmentStaffReportItem(int departmentId, String departmentCode, String departmentName,
                                   int lecturerCount, int technicalOfficerCount, int studentCount,
                                   int courseCount) {
        this.departmentId = departmentId;
        this.departmentCode = departmentCode;
        this.departmentName = departmentName;
        this.lecturerCount = lecturerCount;
        this.technicalOfficerCount = technicalOfficerCount;
        this.studentCount = studentCount;
        this.courseCount = courseCount;
        this.studentLecturerRatio = lecturerCount > 0 ? (double) studentCount / lecturerCount : 0.0;
    }

    public int getDepartmentId() { return departmentId; }
    public String getDepartmentCode() { return departmentCode; }
    public String getDepartmentName() { return departmentName; }
    public int getLecturerCount() { return lecturerCount; }
    public int getTechnicalOfficerCount() { return technicalOfficerCount; }
    public int getStudentCount() { return studentCount; }
    public int getCourseCount() { return courseCount; }
    public double getStudentLecturerRatio() { return studentLecturerRatio; }
}
