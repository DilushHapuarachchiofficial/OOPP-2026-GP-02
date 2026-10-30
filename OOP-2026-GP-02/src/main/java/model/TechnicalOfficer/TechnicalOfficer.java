package main.java.model.TechnicalOfficer;

import main.java.model.Attendance;
import main.java.model.Medical;
import main.java.model.User;

/**
 * Model class representing a Technical Officer in the TecFAMS system.
 * Inherits from the common User class following OOP principles and UML specifications.
 */
public class TechnicalOfficer extends User {
    private String officerId;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String department;
    private String profilePicture;

    /**
     * Default constructor initializing empty technical officer fields.
     */
    public TechnicalOfficer() {
        super();
        this.officerId = "";
        this.username = "";
        this.name = "";
        this.email = "";
        this.phone = "";
        this.department = "";
        this.profilePicture = "default_avatar.png";
    }

    /**
     * Parameterized constructor
     */
    public TechnicalOfficer(String officerId, String username, String name, 
                            String email, String phone, String department) {
        super();
        this.officerId = officerId;
        this.username = username;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.profilePicture = "default_avatar.png";
    }

    // UML Methods
    public void createProfile() {
        // Profile setup
    }

    public void viewTimetable() {
        new main.java.ui.TechnicalOfficer.DepartmentTimetableForm(null, this).setVisible(true);
    }

    public void addAttendance(String studentId, String courseCode, String sessionType, String status) {
        // Add attendance logic
    }

    public void addAttendance(Attendance attendance) {
        // Add attendance model object logic
    }

    public void updateAttendance(String recordId, String status) {
        // Update attendance logic
    }

    public void addMedical(String studentId, String courseCode, String reason) {
        // Medical submission logic
    }

    public void addMedical(Medical medical) {
        // Medical submission model object logic
    }

    public void updateMedicalRecord(String recordId, String status) {
        // Medical record status update logic
    }

    public void viewNotices() {
        new main.java.ui.TechnicalOfficer.FacultyNoticesForm(null, this).setVisible(true);
    }

    // Getters and Setters
    public String getOfficerId() {
        return officerId;
    }

    public void setOfficerId(String officerId) {
        this.officerId = officerId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    @Override
    public String toString() {
        return "TechnicalOfficer{" +
                "officerId='" + officerId + '\'' +
                ", name='" + name + '\'' +
                ", department='" + department + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
