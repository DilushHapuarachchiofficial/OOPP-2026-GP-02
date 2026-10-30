package main.java.model.Undergraduate;

/**
 * Model class representing overall student GPA and academic performance summary.
 * Follows UGC Circular No. 12-2024 university classification standards.
 * Supports Cumulative GPA (CGPA) and Semester GPA (SGPA) for specific academic periods.
 */
public class StudentGPASummary {
    private double cgpa;                     // Cumulative Grade Point Average
    private double sgpa;                     // Semester Grade Point Average
    private int totalRegisteredCredits;      // Total credits registered this academic period
    private int totalCompletedCredits;       // Credits evaluated and passed with final marks
    private int totalRegisteredCourses;      // Number of courses registered
    private int evaluatedCourses;            // Number of courses with final marks released
    private String standingStatus;           // "Good Standing", "Active", "Probation"
    private String degreeClassification;     // e.g. "First Class Honours"
    private String periodLabel;              // e.g. "All Academic Years", "Year 1 Sem 1"

    // Default Constructor
    public StudentGPASummary() {
        this.cgpa = 0.0;
        this.sgpa = 0.0;
        this.standingStatus = "Active";
        this.degreeClassification = "General Pass";
        this.periodLabel = "All Academic Years (Cumulative)";
    }

    // Full Parameterized Constructor
    public StudentGPASummary(double cgpa, double sgpa, int totalRegisteredCredits,
                             int totalCompletedCredits, int totalRegisteredCourses,
                             int evaluatedCourses, String periodLabel) {
        this.cgpa = cgpa;
        this.sgpa = sgpa;
        this.totalRegisteredCredits = totalRegisteredCredits;
        this.totalCompletedCredits = totalCompletedCredits;
        this.totalRegisteredCourses = totalRegisteredCourses;
        this.evaluatedCourses = evaluatedCourses;
        this.periodLabel = (periodLabel != null) ? periodLabel : "All Academic Years (Cumulative)";
        this.degreeClassification = determineClassification(cgpa);
        this.standingStatus = (cgpa >= 2.00) ? "Good Standing" : "Academic Probation";
    }

    // Backward-compatible Constructor
    public StudentGPASummary(double cgpa, double sgpa, int totalRegisteredCredits,
                             int totalCompletedCredits, int totalRegisteredCourses,
                             int evaluatedCourses) {
        this(cgpa, sgpa, totalRegisteredCredits, totalCompletedCredits, totalRegisteredCourses,
                evaluatedCourses, "All Academic Years (Cumulative)");
    }

    /**
     * Determines degree honours classification based on UGC circular standards.
     */
    public static String determineClassification(double gpa) {
        if (gpa >= 3.70) {
            return "First Class Honours";
        } else if (gpa >= 3.30) {
            return "Second Class (Upper Division)";
        } else if (gpa >= 3.00) {
            return "Second Class (Lower Division)";
        } else if (gpa >= 2.00) {
            return "General Pass";
        } else if (gpa > 0.0) {
            return "Referred / Probation";
        } else {
            return "Pending Evaluation";
        }
    }

    public String getFormattedCGPA() {
        return String.format("%.2f", cgpa);
    }

    public String getFormattedSGPA() {
        return String.format("%.2f", sgpa);
    }

    // Getters and Setters
    public double getCgpa() {
        return cgpa;
    }

    public void setCgpa(double cgpa) {
        this.cgpa = cgpa;
        this.degreeClassification = determineClassification(cgpa);
    }

    public double getSgpa() {
        return sgpa;
    }

    public void setSgpa(double sgpa) {
        this.sgpa = sgpa;
    }

    public int getTotalRegisteredCredits() {
        return totalRegisteredCredits;
    }

    public void setTotalRegisteredCredits(int totalRegisteredCredits) {
        this.totalRegisteredCredits = totalRegisteredCredits;
    }

    public int getTotalCompletedCredits() {
        return totalCompletedCredits;
    }

    public void setTotalCompletedCredits(int totalCompletedCredits) {
        this.totalCompletedCredits = totalCompletedCredits;
    }

    public int getTotalRegisteredCourses() {
        return totalRegisteredCourses;
    }

    public void setTotalRegisteredCourses(int totalRegisteredCourses) {
        this.totalRegisteredCourses = totalRegisteredCourses;
    }

    public int getEvaluatedCourses() {
        return evaluatedCourses;
    }

    public void setEvaluatedCourses(int evaluatedCourses) {
        this.evaluatedCourses = evaluatedCourses;
    }

    public String getStandingStatus() {
        return standingStatus;
    }

    public void setStandingStatus(String standingStatus) {
        this.standingStatus = standingStatus;
    }

    public String getDegreeClassification() {
        return degreeClassification;
    }

    public void setDegreeClassification(String degreeClassification) {
        this.degreeClassification = degreeClassification;
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
    }
}
