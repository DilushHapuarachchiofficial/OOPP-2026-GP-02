package main.java.model;

public class User {
    private int id;
    private String name;
    private String username;
    private String role;
    private String email;
    private String status;
    private String profilePicture;

    public User() {}

    public User(int id, String name, String username, String role, String email, String status) {
        this(id, name, username, role, email, status, null);
    }

    public User(int id, String name, String username, String role, String email, String status, String profilePicture) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.role = role;
        this.email = email;
        this.status = status;
        this.profilePicture = profilePicture;
    }

    public int getUserId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}
