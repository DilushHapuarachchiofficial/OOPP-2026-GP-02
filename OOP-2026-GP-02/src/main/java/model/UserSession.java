package main.java.model;

/**
 * UserSession.java
 * TecFAMS – Faculty of Technology Academic Management System
 *
 * Thread-safe global session manager holding the identity of the currently
 * authenticated user. Populated upon successful login and invalidated upon logout.
 */
public class UserSession {

    private static volatile UserSession currentSession = null;

    private final int userId;
    private final String username;
    private final String role;
    private final String fullName;
    private final String designation;
    private final String departmentName;
    private final String email;

    public UserSession(int userId, String username, String role, String fullName,
                       String designation, String departmentName, String email) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.fullName = fullName;
        this.designation = designation;
        this.departmentName = departmentName;
        this.email = email;
    }

    /**
     * Sets the active authenticated user session.
     */
    public static synchronized void setCurrentSession(UserSession session) {
        currentSession = session;
    }

    /**
     * Gets the currently active user session, or null if no user is authenticated.
     */
    public static synchronized UserSession getCurrentSession() {
        return currentSession;
    }

    /**
     * Invalidates the active session (used on logout).
     */
    public static synchronized void clearSession() {
        currentSession = null;
    }

    /**
     * Checks if there is an active authenticated session.
     */
    public static synchronized boolean isLoggedIn() {
        return currentSession != null;
    }

    // Getters
    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDesignation() {
        return designation;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getEmail() {
        return email;
    }
}
