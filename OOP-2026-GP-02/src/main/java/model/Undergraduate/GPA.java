package main.java.model.Undergraduate;

import java.util.List;

/**
 * Represents the GPA (SGPA and CGPA) of an undergraduate student.
 * Complies with the TecFAMS UML class diagram.
 */
public class GPA {
    private double sgpa;
    private double cgpa;

    public GPA() {
        this.sgpa = 0.0;
        this.cgpa = 0.0;
    }

    public GPA(double sgpa, double cgpa) {
        this.sgpa = sgpa;
        this.cgpa = cgpa;
    }

    /**
     * Calculates SGPA from a list of marks.
     */
    public double calculateSGPA(List<Mark> marks) {
        if (marks == null || marks.isEmpty()) {
            return 0.0;
        }
        double totalPoints = 0.0;
        for (Mark m : marks) {
            totalPoints += m.getGradePoint();
        }
        this.sgpa = Math.round((totalPoints / marks.size()) * 100.0) / 100.0;
        return this.sgpa;
    }

    /**
     * Calculates CGPA from all completed semester marks.
     */
    public double calculateCGPA(List<Mark> marks) {
        if (marks == null || marks.isEmpty()) {
            return 0.0;
        }
        double totalPoints = 0.0;
        for (Mark m : marks) {
            totalPoints += m.getGradePoint();
        }
        this.cgpa = Math.round((totalPoints / marks.size()) * 100.0) / 100.0;
        return this.cgpa;
    }

    // Getters and Setters
    public double getSGPA() {
        return sgpa;
    }

    public void setSGPA(double sgpa) {
        this.sgpa = sgpa;
    }

    public double getCGPA() {
        return cgpa;
    }

    public void setCGPA(double cgpa) {
        this.cgpa = cgpa;
    }
}
