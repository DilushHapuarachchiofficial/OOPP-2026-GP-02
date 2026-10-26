package main.java.model.Undergraduate;

/**
 * Model class representing the overall course grade and final mark for a student.
 * Complies with UGC Circular 12-2024 grading criteria.
 * Designed with simple, clear Java for beginner comprehension.
 */
public class CourseGradeItem {
    private int courseId;
    private String courseCode;
    private String courseName;
    private int credit;
    private Double finalMark;      // Overall weighted percentage mark (e.g. 83.00), null if not released
    private String grade;          // UGC letter grade (A+, A, B, etc. or "Pending")
    private Double gradePoint;     // Grade point value (4.0, 3.7, etc. or null if pending)
    private String status;         // "Completed", "In Progress", "Repeat"

    // Default Constructor
    public CourseGradeItem() {
    }

    // Full Parameterized Constructor
    public CourseGradeItem(int courseId, String courseCode, String courseName, int credit,
                           Double finalMark, String grade, Double gradePoint, String status) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credit = credit;
        this.finalMark = finalMark;
        this.grade = grade;
        this.gradePoint = gradePoint;
        this.status = status;
    }

    // Helper method to check if the grade is pending release
    public boolean isPending() {
        return finalMark == null || "Pending".equalsIgnoreCase(grade) || "In Progress".equalsIgnoreCase(status);
    }

    // Helper method to check if passed (Grade C or above, GPA >= 2.0)
    public boolean isPassed() {
        if (isPending()) return false;
        return gradePoint != null && gradePoint >= 2.0;
    }

    // Formatted mark string for display
    public String getFormattedMark() {
        if (finalMark == null) {
            return "Pending";
        }
        return String.format("%.2f%%", finalMark);
    }

    // Formatted grade point string
    public String getFormattedGradePoint() {
        if (gradePoint == null) {
            return "-";
        }
        return String.format("%.2f", gradePoint);
    }

    // Getters and Setters
    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
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

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public Double getFinalMark() {
        return finalMark;
    }

    public void setFinalMark(Double finalMark) {
        this.finalMark = finalMark;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Double getGradePoint() {
        return gradePoint;
    }

    public void setGradePoint(Double gradePoint) {
        this.gradePoint = gradePoint;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
