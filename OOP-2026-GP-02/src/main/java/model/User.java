package main.java.model;


 
public class User {

    // =========================================================================
    // ENCAPSULATED PRIVATE ATTRIBUTES (Class Diagram Specifications)
    // =========================================================================
    private int userId;
    private String username;
    private String password;
    private String name;
    private String email;
    private String phone;
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
     * Full parameterized constructor initializing all 7 attributes.
     *
     * @param userId         Unique system identifier
     * @param username       System login username
     * @param password       User password (plaintext or hash)
     * @param name           Full user display name
     * @param email          Contact email address
     * @param phone          Contact phone number
     * @param profilePicture Relative or absolute path to avatar/profile image
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
     * Convenience constructor without password and profile picture.
     */
    public User(int userId, String username, String name, String email, String phone) {
        this(userId, username, null, name, email, phone, null);
    }

    // =========================================================================
    // ENCAPSULATION: GETTERS AND SETTERS
    // =========================================================================

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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
     *
     * @return true if credentials match a valid user account, false otherwise
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

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                '}';
    }
}
