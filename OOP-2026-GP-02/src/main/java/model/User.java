
package main.java.model;



/**
 * Base User class for the TecFAMS system.
 * Follows OOP principles (Inheritance, Encapsulation) for all university users:
 * TechnicalOfficer, Admin, Student, Lecturer.
 */
public class User {

    // =========================================================================
    // ENCAPSULATED PRIVATE ATTRIBUTES
    // =========================================================================
    private int userId;
    private String username;
    private String password;
    private String name;
    private String role;
    private String email;
    private String phone;
    private String status;
    private String profilePicture;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================

    /**
     * Default no-argument constructor.
     */
    public User() {
    }

    /**
     * Admin Dashboard constructor (6 parameters).
     */
    public User(int id, String name, String username, String role, String email, String status) {
        this.userId = id;
        this.name = name;
        this.username = username;
        this.role = role;
        this.email = email;
        this.status = status;
    }

    /**
     * Full parameterized constructor from class diagram specifications (7 parameters).
     */
    public User(int userId, String username, String password, String name,
                String email, String phone, String profilePicture) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.profilePicture = profilePicture;
    }

    /**
     * Parameterized constructor supporting String userId for compatibility with subclasses and UML inputs.
     */
    public User(String userId, String username, String password, String name,
                String email, String phone, String profilePicture) {
        int parsedId = 0;
        if (userId != null && !userId.trim().isEmpty()) {
            try {
                parsedId = Integer.parseInt(userId.replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {
            }
        }
        this.userId = parsedId;
        this.username = username != null ? username.trim() : null;
        this.password = password != null ? password.trim() : null;
        this.name = name != null ? name.trim() : null;
        this.email = email != null ? email.trim() : null;
        this.phone = phone != null ? phone.trim() : null;
        this.profilePicture = profilePicture != null ? profilePicture.trim() : null;
    }

    /**
     * Convenience constructor without password and profile picture (5 parameters).
     */
    public User(int userId, String username, String name, String email, String phone) {
        this(userId, username, null, name, email, phone, null);
    }

    // =========================================================================
    // GETTERS AND SETTERS
    // =========================================================================

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUserId(String userId) {
        if (userId != null && !userId.trim().isEmpty()) {
            try {
                this.userId = Integer.parseInt(userId.replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {
                this.userId = 0;
            }
        } else {
            this.userId = 0;
        }
    }

    // Alias for getId() for backward compatibility
    public int getId() {
        return userId;
    }

    public void setId(int id) {
        this.userId = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username != null ? username.trim() : null;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password != null ? password.trim() : null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
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
        this.email = email != null ? email.trim() : null;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone.trim() : null;
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
        this.profilePicture = profilePicture != null ? profilePicture.trim() : null;
    }

    // =========================================================================
    // METHODS SPECIFIED IN CLASS DIAGRAM
    // =========================================================================

    /**
     * + login() : boolean
     * Authenticates the user credentials against the system authentication service.
     */
    public boolean login() {
        if (this.username != null && this.password != null) {
            LoginLogic.AuthResult result = LoginLogic.authenticate(this.username, this.password);
            return result != null && result.isSuccess();
        }
        return false;
    }

    /**
     * + logout() : void
     * Invalidates the active authenticated user session.
     */
    public void logout() {
        UserSession.clearSession();
    }

    /**
     * + updateProfile() : void
     * Base profile update hook; can be overridden by specialized subclasses.
     */
    public void updateProfile() {
        // Base implementation hook
    }


    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", status='" + status + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                '}';
    }



