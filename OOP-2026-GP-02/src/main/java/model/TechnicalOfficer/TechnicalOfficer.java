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
     * Default constructor with sample university data
     */
    public TechnicalOfficer() {
        super();
        this.officerId = "TO/2026/001";
        this.username = "to_officer";
        this.name = "Mr. Nimal Jayawardena";
        this.email = "nimal.to@tech.ruh.ac.lk";
        this.phone = "071-2345678";
        this.department = "Information & Communication Technology";
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

    
}
