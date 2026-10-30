package main.java.model.Undergraduate;

/**
 * Model class representing the overall course grade and CA breakdown for a student.
 * Complies with UGC Circular 12-2024 grading criteria.
 * Holds marks for individual continuous assessment components:
 * - Quiz marks
 * - Assignment / Project marks
 * - Mid-Semester Exam marks
 * - Total Continuous Assessment (CA) score
 * - Final Semester Exam marks
 * - Overall Final Weighted Mark and UGC Letter Grade
 */
public class CourseGradeItem {
    private int courseId;
    private String courseCode;
    private String courseName;
    private int credit;
    private int academicYear;      // 1 = Level I, 2 = Level II
    private int semester;          // 1 = Semester I, 2 = Semester II

    // CA and Exam Mark Breakdown
    private Double quizMark;          // Score achieved in Quizzes (e.g. 88.00)
    private Double assignmentMark;    // Score achieved in Assignments / Projects (e.g. 90.00)
    private Double midExamMark;       // Score achieved in Mid-Semester Exam (e.g. 84.00)
    private Double caTotalMark;       // Total weighted CA score (e.g. 34.90 out of 40%)
    private Double finalExamMark;     // Score achieved in Final Examination (e.g. 86.00 out of 100%)

    // Overall Results
    private Double finalMark;         // Overall weighted percentage mark (e.g. 86.50), null if not released
    private String grade;             // UGC letter grade (A+, A, B, etc. or "Pending")
    private Double gradePoint;        // Grade point value (4.0, 3.7, etc. or null if pending)
    private String status;            // "Completed", "In Progress", "Repeat"

    // Default Constructor
    public CourseGradeItem() {
    }

    // Full Parameterized Constructor
    public CourseGradeItem(int courseId, String courseCode, String courseName, int credit,
                           int academicYear, int semester,
                           Double quizMark, Double assignmentMark, Double midExamMark,
                           Double caTotalMark, Double finalExamMark,
                           Double finalMark, String grade, Double gradePoint, String status) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credit = credit;
        this.academicYear = academicYear;
        this.semester = semester;
        this.quizMark = quizMark;
        this.assignmentMark = assignmentMark;
        this.midExamMark = midExamMark;
        this.caTotalMark = caTotalMark;
        this.finalExamMark = finalExamMark;
        this.finalMark = finalMark;
        this.grade = grade;
        this.gradePoint = gradePoint;
        this.status = status;
    }

    // Backward-compatible Constructor
    public CourseGradeItem(int courseId, String courseCode, String courseName, int credit,
                           Double finalMark, String grade, Double gradePoint, String status) {
        this(courseId, courseCode, courseName, credit, 2, 1, null, null, null, null, null,
                finalMark, grade, gradePoint, status);
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

    public String getFormattedQuiz() {
        return (quizMark != null) ? String.format("%.1f", quizMark) : "-";
    }

    public String getFormattedAssignment() {
        return (assignmentMark != null) ? String.format("%.1f", assignmentMark) : "-";
    }

    public String getFormattedMidExam() {
        return (midExamMark != null) ? String.format("%.1f", midExamMark) : "-";
    }

    public String getFormattedCATotal() {
        return (caTotalMark != null) ? String.format("%.2f", caTotalMark) : "-";
    }

    public String getFormattedFinalExam() {
        return (finalExamMark != null) ? String.format("%.1f", finalExamMark) : "-";
    }

    public String getYearSemesterLabel() {
        return "Y" + academicYear + " S" + semester;
    }

    public String getSemesterName() {
        if (academicYear == 1 && semester == 1) return "Level I - Semester I";
        if (academicYear == 1 && semester == 2) return "Level I - Semester II";
        if (academicYear == 2 && semester == 1) return "Level II - Semester I";
        return "Year " + academicYear + " Semester " + semester;
    }

    public double getQuizContribution() {
        return (quizMark != null) ? (quizMark * 0.10) : 0.0;
    }

    public double getAssignmentContribution() {
        return (assignmentMark != null) ? (assignmentMark * 0.15) : 0.0;
    }

    public double getMidExamContribution() {
        return (midExamMark != null) ? (midExamMark * 0.15) : 0.0;
    }

    public double getFinalExamContribution() {
        return (finalExamMark != null) ? (finalExamMark * 0.60) : 0.0;
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

    public int getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public Double getQuizMark() {
        return quizMark;
    }

    public void setQuizMark(Double quizMark) {
        this.quizMark = quizMark;
    }

    public Double getAssignmentMark() {
        return assignmentMark;
    }

    public void setAssignmentMark(Double assignmentMark) {
        this.assignmentMark = assignmentMark;
    }

    public Double getMidExamMark() {
        return midExamMark;
    }

    public void setMidExamMark(Double midExamMark) {
        this.midExamMark = midExamMark;
    }

    public Double getCaTotalMark() {
        return caTotalMark;
    }

    public void setCaTotalMark(Double caTotalMark) {
        this.caTotalMark = caTotalMark;
    }

    public Double getFinalExamMark() {
        return finalExamMark;
    }

    public void setFinalExamMark(Double finalExamMark) {
        this.finalExamMark = finalExamMark;
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
