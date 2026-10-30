package main.java.model.Undergraduate;

/**
 * Summarizes attendance metrics for a specific enrolled course.
 * Complies with the TecFAMS UML class model and Faculty of Technology 80% rule.
 */
public class CourseAttendanceSummary {
    private int courseId;
    private String courseCode;
    private String courseName;
    private int totalSessions;
    private int attendedSessions;
    private int absentSessions;
    private int medicalSessions;
    private double percentage;

    public CourseAttendanceSummary() {
    }

    public CourseAttendanceSummary(int courseId, String courseCode, String courseName,
                                   int totalSessions, int attendedSessions, int absentSessions, int medicalSessions) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.totalSessions = totalSessions;
        this.attendedSessions = attendedSessions;
        this.absentSessions = absentSessions;
        this.medicalSessions = medicalSessions;
        this.percentage = calculatePercentage();
    }

    /**
     * Calculates attendance percentage including approved medical sessions.
     * Formula: ((Attended + Approved Medicals) / Total Sessions) * 100
     */
    public double calculatePercentage() {
        if (totalSessions <= 0) {
            this.percentage = 100.0; // If no sessions held yet, mark as full / pending
            return this.percentage;
        }
        int effective = attendedSessions + medicalSessions;
        this.percentage = Math.round(((double) effective / totalSessions * 100.0) * 10.0) / 10.0;
        return this.percentage;
    }

    /**
     * Checks if the student meets the minimum 80% attendance requirement for this course.
     */
    public boolean isEligible() {
        if (totalSessions <= 0) return true;
        return calculatePercentage() >= 80.0;
    }

    public String getEligibilityStatus() {
        if (totalSessions <= 0) return "No Sessions Yet";
        return isEligible() ? "Eligible (>=80%)" : "At Risk (<80%)";
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

    public int getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(int totalSessions) {
        this.totalSessions = totalSessions;
        calculatePercentage();
    }

    public int getAttendedSessions() {
        return attendedSessions;
    }

    public void setAttendedSessions(int attendedSessions) {
        this.attendedSessions = attendedSessions;
        calculatePercentage();
    }

    public int getAbsentSessions() {
        return absentSessions;
    }

    public void setAbsentSessions(int absentSessions) {
        this.absentSessions = absentSessions;
        calculatePercentage();
    }

    public int getMedicalSessions() {
        return medicalSessions;
    }

    public void setMedicalSessions(int medicalSessions) {
        this.medicalSessions = medicalSessions;
        calculatePercentage();
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
