package main.java.model.Undergraduate;

import main.java.model.User;
import java.util.ArrayList;
import java.util.List;

/**
 * Undergraduate student model for the TecFAMS system.
 * Extends the User base class and implements student-specific attributes and operations
 * as defined in the OOP mini-project UML class diagram.
 */
public class Undergraduate extends User {
    private String studentId;
    private String registrationNo;
    private String batch;
    private String department;
    private int enrollmentYear;
    private String nic;
    private String dob;
    private String address;

    // Student academic data references
    private GPA gpa;
    private AttendanceSummary attendanceSummary;
    private Timetable timetable;
    private List<Attendance> attendanceList;
    private List<MedicalRecord> medicalRecords;
    private List<Course> courses;
    private List<Mark> grades;
    private List<Notice> notices;

    // Default constructor
    public Undergraduate() {
        super();
        this.attendanceList = new ArrayList<>();
        this.medicalRecords = new ArrayList<>();
        this.courses = new ArrayList<>();
        this.grades = new ArrayList<>();
        this.notices = new ArrayList<>();
    }

    // Parameterized constructor with User base properties and Undergraduate-specific fields
    // Parameterized constructor accepting int userId
    public Undergraduate(int userId, String username, String password, String name,
                         String email, String phone, String profilePicture,
                         String studentId, String registrationNo, String batch,
                         String department, int enrollmentYear) {
        super(userId, username, password, name, email, phone, profilePicture);
        this.studentId = studentId;
        this.registrationNo = registrationNo;
        this.batch = batch;
        this.department = department;
        this.enrollmentYear = enrollmentYear;
        this.attendanceList = new ArrayList<>();
        this.medicalRecords = new ArrayList<>();
        this.courses = new ArrayList<>();
        this.grades = new ArrayList<>();
        this.notices = new ArrayList<>();
    }

    // Parameterized constructor with User base properties and Undergraduate-specific fields
    public Undergraduate(String userId, String username, String password, String name,
                         String email, String phone, String profilePicture,
                         String studentId, String registrationNo, String batch,
                         String department, int enrollmentYear) {
        super(userId, username, password, name, email, phone, profilePicture);
        this.studentId = studentId;
        this.registrationNo = registrationNo;
        this.batch = batch;
        this.department = department;
        this.enrollmentYear = enrollmentYear;
        this.attendanceList = new ArrayList<>();
        this.medicalRecords = new ArrayList<>();
        this.courses = new ArrayList<>();
        this.grades = new ArrayList<>();
        this.notices = new ArrayList<>();
    }

    // UML Specified Methods

    /**
     * Updates permitted contact details (phone and email).
     * Follows the SRS profile restriction rule for Undergraduates.
     */
    /**
     * Updates full student profile details.
     */
    public void updateFullProfile(String name, String email, String phone, String address, String nic, String dob, String profilePicture) {
        setName(name);
        setEmail(email);
        setPhone(phone);
        this.address = address;
        this.nic = nic;
        this.dob = dob;
        if (profilePicture != null && !profilePicture.isEmpty()) {
            setProfilePicture(profilePicture);
        }
    }

    public void updateContactDetails(String phone, String email) {
        setPhone(phone);
        setEmail(email);
    }

    /**
     * Updates permitted profile picture path.
     */
    public void updateProfilePicture(String path) {
        setProfilePicture(path);
    }

    /**
     * Returns student's attendance records.
     */
    public List<Attendance> viewAttendance() {
        return (attendanceList != null) ? attendanceList : new ArrayList<>();
    }

    /**
     * Returns student's submitted medical records.
     */
    public List<MedicalRecord> viewMedicalDetails() {
        return (medicalRecords != null) ? medicalRecords : new ArrayList<>();
    }

    /**
     * Returns student's enrolled courses.
     */
    public List<Course> viewCourses() {
        return (courses != null) ? courses : new ArrayList<>();
    }

    /**
     * Returns student's course grades and marks.
     */
    public List<Mark> viewGrades() {
        return (grades != null) ? grades : new ArrayList<>();
    }

    /**
     * Returns student's GPA object containing SGPA and CGPA.
     */
    public GPA viewGPA() {
        return gpa;
    }

    /**
     * Returns student's timetable.
     */
    public Timetable viewTimetable() {
        return timetable;
    }

    /**
     * Returns notices available to undergraduates.
     */
    public List<Notice> viewNotices() {
        return (notices != null) ? notices : new ArrayList<>();
    }

    // Getters and Setters for Undergraduate specific fields

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getEnrollmentYear() {
        return enrollmentYear;
    }

    public void setEnrollmentYear(int enrollmentYear) {
        this.enrollmentYear = enrollmentYear;
    }

    public AttendanceSummary getAttendanceSummary() {
        return attendanceSummary;
    }

    public void setAttendanceSummary(AttendanceSummary attendanceSummary) {
        this.attendanceSummary = attendanceSummary;
    }

    public void setGpa(GPA gpa) {
        this.gpa = gpa;
    }

    public void setTimetable(Timetable timetable) {
        this.timetable = timetable;
    }

    public void setAttendanceList(List<Attendance> attendanceList) {
        this.attendanceList = attendanceList;
    }

    public void setMedicalRecords(List<MedicalRecord> medicalRecords) {
        this.medicalRecords = medicalRecords;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public void setGrades(List<Mark> grades) {
        this.grades = grades;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setNotices(List<Notice> notices) {
        this.notices = notices;
    }
}
