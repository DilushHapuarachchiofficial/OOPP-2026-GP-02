package main.java.model;

/**
 * Base User class for the TecFAMS system.
 * Defines common user attributes and behaviors according to the UML class diagram.
 */
public class User {
    private String userId;
    private String username;
    private String password;
    private String name;
    private String email;
    private String phone;
    private String profilePicture;

    // Default constructor
    public User() {
    }

    // Parameterized constructor
    public User(String userId, String username, String password, String name, String email, String phone, String profilePicture) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.profilePicture = profilePicture;
    }

    // UML Methods
    public boolean login() {
        return username != null && password != null;
    }

    public void logout() {
        // Clear session or notify system
    }

    public void updateProfile() {
        // Base profile update
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}
