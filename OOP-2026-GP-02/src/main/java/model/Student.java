package main.java.model;

/**
 * Model class representing an Undergraduate Student in TecFAMS.
 * Extends the common User class.
 */
public class Student extends User {
    private String studentId;
    private String batch;
    private int currentSemester;

    public Student() {
        super();
        setRole("Student");
    }

    public Student(String studentId, String username, String name, String email,
                   String phone, String department, String batch, int currentSemester) {
        super(studentId, username, "", name, email, phone, "Student", department);
        this.studentId = studentId;
        this.batch = batch;
        this.currentSemester = currentSemester;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
        setUserId(studentId);
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public int getCurrentSemester() {
        return currentSemester;
    }

    public void setCurrentSemester(int currentSemester) {
        this.currentSemester = currentSemester;
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + studentId + '\'' +
                ", name='" + getName() + '\'' +
                ", department='" + getDepartment() + '\'' +
                ", batch='" + batch + '\'' +
                '}';
    }
}
