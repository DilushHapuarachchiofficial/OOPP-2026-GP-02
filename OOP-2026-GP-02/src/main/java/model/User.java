package main.java.model;

public class User {
    private int id;
    private String name;
    private String username;
    private String role;
    private String email;
    private String status;

    public User() {}

    public User(int id, String name, String username, String role, String email, String status) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.role = role;
        this.email = email;
        this.status = status;
    }

    public int getUserId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }
}
