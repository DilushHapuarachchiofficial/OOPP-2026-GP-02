package main.java.model;

/**
 * Model class representing an Academic Course / Module in TecFAMS.
 * Follows Faculty of Technology syllabus structure with 15 sessions per component.
 */
public class Course {
    private String courseCode;
    private String courseName;
    private int credits;
    private String department;
    private int semester;
    private boolean hasTheory;
    private boolean hasPractical;
    private int totalTheorySessions = 15;
    private int totalPracticalSessions = 15;

    public Course() {
        this.hasTheory = true;
        this.hasPractical = true;
    }

    public Course(String courseCode, String courseName, int credits, String department, int semester) {
        this(courseCode, courseName, credits, department, semester, true, true);
    }

    public Course(String courseCode, String courseName, int credits, String department,
                  int semester, boolean hasTheory, boolean hasPractical) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.department = department;
        this.semester = semester;
        this.hasTheory = hasTheory;
        this.hasPractical = hasPractical;
    }

    /**
     * Checks if a student is eligible for examination based on 80% attendance rule.
     */
    public boolean isEligibleForExam(int attendedTheory, int attendedPractical) {
        boolean theoryOk = !hasTheory || ((double) attendedTheory / totalTheorySessions) >= 0.80;
        boolean practicalOk = !hasPractical || ((double) attendedPractical / totalPracticalSessions) >= 0.80;
        return theoryOk && practicalOk;
    }

    // Getters and Setters
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

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public boolean isHasTheory() {
        return hasTheory;
    }

    public void setHasTheory(boolean hasTheory) {
        this.hasTheory = hasTheory;
    }

    public boolean isHasPractical() {
        return hasPractical;
    }

    public void setHasPractical(boolean hasPractical) {
        this.hasPractical = hasPractical;
    }

    public int getTotalTheorySessions() {
        return totalTheorySessions;
    }

    public void setTotalTheorySessions(int totalTheorySessions) {
        this.totalTheorySessions = totalTheorySessions;
    }

    public int getTotalPracticalSessions() {
        return totalPracticalSessions;
    }

    public void setTotalPracticalSessions(int totalPracticalSessions) {
        this.totalPracticalSessions = totalPracticalSessions;
    }

    @Override
    public String toString() {
        return courseCode + " - " + courseName + " (" + credits + " Credits)";
    }
}
