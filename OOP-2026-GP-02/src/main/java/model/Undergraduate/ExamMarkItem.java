package main.java.model.Undergraduate;

/**
 * Model class representing individual exam and continuous assessment (CA) marks.
 * Holds component-level scores like Quiz, Mid-Exam, Assignments, and Final Exam.
 * Designed with clean and easy-to-understand Java methods.
 */
public class ExamMarkItem {
    private int markId;
    private String courseCode;
    private String courseName;
    private String componentName;       // e.g. "Quiz 01", "Mid-Semester Exam", "Final Theory Exam"
    private String componentType;       // "CA" or "Final"
    private double maximumMarks;        // e.g. 100.00
    private double weightPercentage;    // e.g. 10%, 20%, 60%
    private double marksObtained;       // Score achieved by the student (e.g. 90.00)
    private double weightedContribution;// Weighted score toward final course total (e.g. 9.00)
    private String uploadedDate;        // Date lecturer entered marks

    // Default Constructor
    public ExamMarkItem() {
    }

    // Full Parameterized Constructor
    public ExamMarkItem(int markId, String courseCode, String courseName, String componentName,
                        String componentType, double maximumMarks, double weightPercentage,
                        double marksObtained, double weightedContribution, String uploadedDate) {
        this.markId = markId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.componentName = componentName;
        this.componentType = componentType;
        this.maximumMarks = maximumMarks;
        this.weightPercentage = weightPercentage;
        this.marksObtained = marksObtained;
        this.weightedContribution = weightedContribution;
        this.uploadedDate = uploadedDate;
    }

    // Formatted score percentage (e.g. "90.0%")
    public String getScorePercentage() {
        if (maximumMarks <= 0) return "0.0%";
        double pct = (marksObtained / maximumMarks) * 100.0;
        return String.format("%.1f%%", pct);
    }

    // Formatted obtained marks display (e.g. "90.00 / 100")
    public String getFormattedObtained() {
        return String.format("%.2f / %.0f", marksObtained, maximumMarks);
    }

    // Formatted weighted contribution (e.g. "9.00 / 10.0%")
    public String getFormattedWeighted() {
        return String.format("%.2f / %.0f%%", weightedContribution, weightPercentage);
    }

    // Check if this is a final semester examination
    public boolean isFinalExam() {
        return "Final".equalsIgnoreCase(componentType);
    }

    // Check if this is continuous assessment
    public boolean isCA() {
        return "CA".equalsIgnoreCase(componentType);
    }

    // Getters and Setters
    public int getMarkId() {
        return markId;
    }

    public void setMarkId(int markId) {
        this.markId = markId;
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

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public double getMaximumMarks() {
        return maximumMarks;
    }

    public void setMaximumMarks(double maximumMarks) {
        this.maximumMarks = maximumMarks;
    }

    public double getWeightPercentage() {
        return weightPercentage;
    }

    public void setWeightPercentage(double weightPercentage) {
        this.weightPercentage = weightPercentage;
    }

    public double getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(double marksObtained) {
        this.marksObtained = marksObtained;
    }

    public double getWeightedContribution() {
        return weightedContribution;
    }

    public void setWeightedContribution(double weightedContribution) {
        this.weightedContribution = weightedContribution;
    }

    public String getUploadedDate() {
        return uploadedDate;
    }

    public void setUploadedDate(String uploadedDate) {
        this.uploadedDate = uploadedDate;
    }
}
