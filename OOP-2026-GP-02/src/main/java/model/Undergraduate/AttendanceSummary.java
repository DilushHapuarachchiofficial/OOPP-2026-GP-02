package main.java.model.Undergraduate;

/**
 * Summarizes an undergraduate student's attendance statistics and exam eligibility.
 * Complies with the TecFAMS UML class diagram and 80% attendance rule.
 */
public class AttendanceSummary {
    private int totalSessions;
    private int attendedSessions;
    private int medicalSessions;
    private double percentage;

    public AttendanceSummary() {
    }

    public AttendanceSummary(int totalSessions, int attendedSessions, int medicalSessions) {
        this.totalSessions = totalSessions;
        this.attendedSessions = attendedSessions;
        this.medicalSessions = medicalSessions;
        this.percentage = calculatePercentage();
    }

    /**
     * Calculates the attendance percentage including approved medical sessions.
     * As per SRS Section 5.2, both theory and practical components have 15 sessions each.
     */
    public double calculatePercentage() {
        if (totalSessions <= 0) {
            this.percentage = 0.0;
            return 0.0;
        }
        int effectiveAttended = attendedSessions + medicalSessions;
        this.percentage = Math.round(((double) effectiveAttended / totalSessions * 100.0) * 10.0) / 10.0;
        return this.percentage;
    }

    /**
     * Checks if the student meets the minimum 80% attendance requirement to sit for exams.
     */
    public boolean isEligible() {
        return calculatePercentage() >= 80.0;
    }

    // Getters and Setters
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
