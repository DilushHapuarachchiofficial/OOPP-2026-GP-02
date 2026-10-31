package main.java.model.admin;

public class CourseLecturerAssignment {
    private int assignmentId;
    private int courseId;
    private String courseCode;
    private String courseName;
    private int lecturerId;
    private String lecturerName;
    private String designation;
    private String departmentName;

    public CourseLecturerAssignment(int assignmentId, int courseId, String courseCode, String courseName,
                                    int lecturerId, String lecturerName, String designation, String departmentName) {
        this.assignmentId = assignmentId;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.lecturerId = lecturerId;
        this.lecturerName = lecturerName;
        this.designation = designation;
        this.departmentName = departmentName;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public int getCourseId() {
        return courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public int getLecturerId() {
        return lecturerId;
    }

    public String getLecturerName() {
        return lecturerName;
    }

    public String getDesignation() {
        return designation;
    }

    public String getDepartmentName() {
        return departmentName;
    }
}

