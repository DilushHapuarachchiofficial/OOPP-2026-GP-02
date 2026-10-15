package main.java.model.admin;

public class StudentEligibilityReportItem {
    private int studentId;
    private String studentRegNo;
    private String studentName;
    private String departmentName;
    private int courseId;
    private String courseCode;
    private String courseName;
    private int totalSessions;
    private int presentCount;
    private int absentCount;
    private int approvedMedicals;
    private double attendancePercentage;
    private Double caMarks; // out of 40 or percentage
    private String eligibilityStatus; // "Eligible", "Eligible (Medicals)", "Not Eligible", "Pending"

    public StudentEligibilityReportItem(int studentId, String studentRegNo, String studentName,
                                      String departmentName, int courseId, String courseCode,
                                      String courseName, int totalSessions, int presentCount,
                                      int absentCount, int approvedMedicals,
                                      double attendancePercentage, Double caMarks,
                                      String eligibilityStatus) {
        this.studentId = studentId;
        this.studentRegNo = studentRegNo;
        this.studentName = studentName;
        this.departmentName = departmentName;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.totalSessions = totalSessions;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.approvedMedicals = approvedMedicals;
        this.attendancePercentage = attendancePercentage;
        this.caMarks = caMarks;
        this.eligibilityStatus = eligibilityStatus;
    }

    public int getStudentId() { return studentId; }
    public String getStudentRegNo() { return studentRegNo; }
    public String getStudentName() { return studentName; }
    public String getDepartmentName() { return departmentName; }
    public int getCourseId() { return courseId; }
    public String getCourseCode() { return courseCode; }
    public String getCourseName() { return courseName; }
    public int getTotalSessions() { return totalSessions; }
    public int getPresentCount() { return presentCount; }
    public int getAbsentCount() { return absentCount; }
    public int getApprovedMedicals() { return approvedMedicals; }
    public double getAttendancePercentage() { return attendancePercentage; }
    public Double getCaMarks() { return caMarks; }
    public String getEligibilityStatus() { return eligibilityStatus; }
}
