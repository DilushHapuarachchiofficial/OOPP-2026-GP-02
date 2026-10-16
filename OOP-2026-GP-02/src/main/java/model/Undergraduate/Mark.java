package main.java.model.Undergraduate;

/**
 * Represents academic marks, continuous assessments, and grades for a course.
 * Complies with the TecFAMS UML class diagram and UGC circular grading standards.
 */
public class Mark {
    private int markId;
    private String courseCode;
    private String courseName;
    private double quiz;
    private double midExam;
    private double finalTheory;
    private double finalPractical;
    private String grade;
    private double gradePoint;

    public Mark() {
    }

    public Mark(int markId, String courseCode, String courseName, double quiz, double midExam,
                double finalTheory, double finalPractical) {
        this.markId = markId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.quiz = quiz;
        this.midExam = midExam;
        this.finalTheory = finalTheory;
        this.finalPractical = finalPractical;
        this.grade = calculateGrade();
    }

    /**
     * Calculates the overall weighted or total continuous assessment & exam score.
     */
    public double calculateTotal() {
        // Simple aggregate or weighted representation
        return (quiz * 0.1) + (midExam * 0.2) + (finalTheory * 0.4) + (finalPractical * 0.3);
    }

    /**
     * Calculates letter grade and grade point based on UGC guidelines.
     */
    public String calculateGrade() {
        double total = calculateTotal();
        if (total >= 85) {
            this.gradePoint = 4.0;
            this.grade = "A+";
        } else if (total >= 70) {
            this.gradePoint = 4.0;
            this.grade = "A";
        } else if (total >= 65) {
            this.gradePoint = 3.7;
            this.grade = "A-";
        } else if (total >= 60) {
            this.gradePoint = 3.3;
            this.grade = "B+";
        } else if (total >= 55) {
            this.gradePoint = 3.0;
            this.grade = "B";
        } else if (total >= 50) {
            this.gradePoint = 2.7;
            this.grade = "B-";
        } else if (total >= 45) {
            this.gradePoint = 2.3;
            this.grade = "C+";
        } else if (total >= 40) {
            this.gradePoint = 2.0;
            this.grade = "C";
        } else if (total >= 35) {
            this.gradePoint = 1.7;
            this.grade = "C-";
        } else {
            this.gradePoint = 0.0;
            this.grade = "E";
        }
        return this.grade;
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

    public double getQuiz() {
        return quiz;
    }

    public void setQuiz(double quiz) {
        this.quiz = quiz;
    }

    public double getMidExam() {
        return midExam;
    }

    public void setMidExam(double midExam) {
        this.midExam = midExam;
    }

    public double getFinalTheory() {
        return finalTheory;
    }

    public void setFinalTheory(double finalTheory) {
        this.finalTheory = finalTheory;
    }

    public double getFinalPractical() {
        return finalPractical;
    }

    public void setFinalPractical(double finalPractical) {
        this.finalPractical = finalPractical;
    }

    public String getGrade() {
        if (grade == null) {
            calculateGrade();
        }
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public double getGradePoint() {
        return gradePoint;
    }

    public void setGradePoint(double gradePoint) {
        this.gradePoint = gradePoint;
    }
}
