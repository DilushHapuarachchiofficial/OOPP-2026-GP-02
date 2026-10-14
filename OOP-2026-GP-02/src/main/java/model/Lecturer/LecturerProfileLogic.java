package main.java.model.Lecturer;

import main.java.dao.DBConnection;
import main.java.model.UserSession;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 * LecturerProfileLogic.java
 * TecFAMS – Faculty of Technology Academic Management System
 *
 * Dedicated business logic and data access class for the Lecturer Profile page.
 * Keeps business validation, JDBC database access, and session synchronization
 * cleanly separated from the Swing UI layer.
 *
 * Responsibilities:
 * - Parameterized JDBC queries against MySQL tables: `lecturers`, `users`, and `departments`.
 * - Profile information retrieval for the authenticated lecturer.
 * - Validation of personal information (name, email format, email uniqueness, phone format).
 * - Safe updating of editable profile fields in the database.
 * - Password change validation (current password check, length, complexity, match verification).
 * - Safe updating of user passwords in the `users` table.
 * - Synchronization of active `UserSession` upon profile updates.
 */
public class LecturerProfileLogic {

    // Regex for basic email format validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // Regex for sensible phone number format (allows digits, spaces, plus sign, hyphens, parentheses; 7-20 chars)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+0-9\\s\\-()]{7,20}$"
    );

    // =========================================================================
    // DATA MODEL: LecturerProfileData
    // =========================================================================

    /**
     * Data Transfer Object representing the lecturer's profile details
     * retrieved from `lecturers`, `users`, and `departments` tables.
     */
    public static class LecturerProfileData {
        private final int lecturerId;
        private final String username;
        private final String lecturerName;
        private final String email;
        private final String phone;
        private final String designation;
        private final int departmentId;
        private final String departmentCode;
        private final String departmentName;
        private final String profilePicture;
        private final String createdAt;

        public LecturerProfileData(int lecturerId, String username, String lecturerName,
                                   String email, String phone, String designation,
                                   int departmentId, String departmentCode, String departmentName,
                                   String profilePicture, String createdAt) {
            this.lecturerId = lecturerId;
            this.username = username != null ? username : "";
            this.lecturerName = lecturerName != null ? lecturerName : "";
            this.email = email != null ? email : "";
            this.phone = phone != null ? phone : "";
            this.designation = designation != null ? designation : "Lecturer";
            this.departmentId = departmentId;
            this.departmentCode = departmentCode != null ? departmentCode : "";
            this.departmentName = departmentName != null ? departmentName : "Faculty of Technology";
            this.profilePicture = profilePicture != null ? profilePicture : "default_avatar.png";
            this.createdAt = createdAt != null ? createdAt : "";
        }

        public int getLecturerId() { return lecturerId; }
        public String getUsername() { return username; }
        public String getLecturerName() { return lecturerName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getDesignation() { return designation; }
        public int getDepartmentId() { return departmentId; }
        public String getDepartmentCode() { return departmentCode; }
        public String getDepartmentName() { return departmentName; }
        public String getProfilePicture() { return profilePicture; }
        public String getCreatedAt() { return createdAt; }

        /**
         * Computes clean 2-letter initials for circular avatar display.
         */
        public String getInitials() {
            if (lecturerName == null || lecturerName.trim().isEmpty()) {
                return "LP";
            }
            String[] parts = lecturerName.trim().split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (String p : parts) {
                if (!p.isEmpty() && !p.endsWith(".")) {
                    sb.append(Character.toUpperCase(p.charAt(0)));
                }
            }
            if (sb.length() >= 2) {
                return sb.substring(0, 2);
            } else if (sb.length() == 1) {
                return sb.toString();
            }
            return "LP";
        }
    }

    // =========================================================================
    // RESULT CONTAINERS
    // =========================================================================

    /**
     * Encapsulates the outcome of form validation.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final String fieldName;

        public ValidationResult(boolean valid, String message, String fieldName) {
            this.valid = valid;
            this.message = message;
            this.fieldName = fieldName;
        }

        public static ValidationResult ok() {
            return new ValidationResult(true, "", "");
        }

        public static ValidationResult fail(String message, String fieldName) {
            return new ValidationResult(false, message, fieldName);
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public String getFieldName() { return fieldName; }
    }

    /**
     * Encapsulates the result of a database update operation.
     */
    public static class OperationResult {
        private final boolean success;
        private final String message;

        public OperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

    // =========================================================================
    // PROFILE RETRIEVAL METHODS
    // =========================================================================

    /**
     * Loads the profile details for a given lecturer ID by joining
     * `lecturers`, `users`, and `departments` tables.
     *
     * @param lecturerId Primary key ID of the lecturer (matches users.user_id)
     * @return Fully populated LecturerProfileData
     * @throws SQLException If database query fails or no record is found
     */
    public static LecturerProfileData loadLecturerProfile(int lecturerId) throws SQLException {
        if (lecturerId <= 0) {
            throw new IllegalArgumentException("Invalid lecturer ID: " + lecturerId);
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database 'tecfams_db'. Please check that your MySQL server is running.");
            }

            String sql = "SELECT l.lecturer_id, u.username, u.profile_picture, u.created_at, " +
                         "       l.lecturer_name, l.email, l.phone, l.designation, " +
                         "       l.department_id, d.department_code, d.department_name " +
                         "FROM lecturers l " +
                         "JOIN users u ON l.lecturer_id = u.user_id " +
                         "LEFT JOIN departments d ON l.department_id = d.department_id " +
                         "WHERE l.lecturer_id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, lecturerId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return new LecturerProfileData(
                                rs.getInt("lecturer_id"),
                                rs.getString("username"),
                                rs.getString("lecturer_name"),
                                rs.getString("email"),
                                rs.getString("phone"),
                                rs.getString("designation"),
                                rs.getInt("department_id"),
                                rs.getString("department_code"),
                                rs.getString("department_name"),
                                rs.getString("profile_picture"),
                                rs.getString("created_at")
                        );
                    } else {
                        throw new SQLException("No profile record found in database for lecturer ID: " + lecturerId);
                    }
                }
            }
        }
    }

    /**
     * Resolves lecturer ID by username from the `users` table and retrieves the profile.
     */
    public static LecturerProfileData loadLecturerProfileByUsername(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database 'tecfams_db'. Please check that your MySQL server is running.");
            }

            String sql = "SELECT user_id FROM users WHERE username = ? AND role = 'Lecturer'";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username.trim());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int resolvedId = rs.getInt("user_id");
                        return loadLecturerProfile(resolvedId);
                    } else {
                        throw new SQLException("No lecturer account found for username: " + username);
                    }
                }
            }
        }
    }

    // =========================================================================
    // PROFILE VALIDATION & UPDATE METHODS
    // =========================================================================

    /**
     * Validates personal information form inputs before database execution.
     *
     * @param lecturerId Authenticated lecturer's ID (to exclude self from duplicate email check)
     * @param name       Entered full name
     * @param email      Entered email address
     * @param phone      Entered contact phone number
     * @return ValidationResult indicating whether the data is valid
     */
    public static ValidationResult validateProfileForm(int lecturerId, String name, String email, String phone) {
        // 1. Full Name Validation
        if (name == null || name.trim().isEmpty()) {
            return ValidationResult.fail("Lecturer Name cannot be empty.", "name");
        }
        String cleanName = name.trim();
        if (cleanName.length() > 100) {
            return ValidationResult.fail("Lecturer Name cannot exceed 100 characters.", "name");
        }

        // 2. Email Validation
        if (email == null || email.trim().isEmpty()) {
            return ValidationResult.fail("Email address is required.", "email");
        }
        String cleanEmail = email.trim();
        if (cleanEmail.length() > 100) {
            return ValidationResult.fail("Email address cannot exceed 100 characters.", "email");
        }
        if (!EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            return ValidationResult.fail("Please enter a valid email address (e.g., lecturer@fot.ruh.ac.lk).", "email");
        }

        // 3. Email Uniqueness Check (Check against other lecturers in MySQL)
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                String sql = "SELECT COUNT(*) AS cnt FROM lecturers WHERE email = ? AND lecturer_id != ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, cleanEmail);
                    pstmt.setInt(2, lecturerId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next() && rs.getInt("cnt") > 0) {
                            return ValidationResult.fail("This email address is already registered to another account.", "email");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Warning: Unable to check email uniqueness due to DB connection issue: " + e.getMessage());
        }

        // 4. Phone Validation (Optional, but if entered must be sensible)
        if (phone != null && !phone.trim().isEmpty()) {
            String cleanPhone = phone.trim();
            if (cleanPhone.length() > 20) {
                return ValidationResult.fail("Contact number cannot exceed 20 characters.", "phone");
            }
            if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
                return ValidationResult.fail("Please enter a valid phone number (digits, spaces, or international '+' prefix).", "phone");
            }
        }

        return ValidationResult.ok();
    }

    /**
     * Updates editable personal details in the `lecturers` table using PreparedStatement.
     * Synchronizes active UserSession and returns an OperationResult.
     *
     * @param lecturerId Authenticated lecturer's ID
     * @param name       New full name
     * @param email      New email address
     * @param phone      New phone number
     * @return OperationResult
     */
    public static OperationResult updateProfile(int lecturerId, String name, String email, String phone) {
        if (lecturerId <= 0) {
            return new OperationResult(false, "Invalid session: Lecturer ID not found.");
        }

        // 1. Run input validation
        ValidationResult val = validateProfileForm(lecturerId, name, email, phone);
        if (!val.isValid()) {
            return new OperationResult(false, val.getMessage());
        }

        String cleanName = name.trim();
        String cleanEmail = email.trim();
        String cleanPhone = (phone != null && !phone.trim().isEmpty()) ? phone.trim() : null;

        // 2. Perform parameterized SQL update
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return new OperationResult(false, "Database connection failed. Please ensure MySQL server is online.");
            }

            String sql = "UPDATE lecturers SET lecturer_name = ?, email = ?, phone = ? WHERE lecturer_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, cleanName);
                pstmt.setString(2, cleanEmail);
                pstmt.setString(3, cleanPhone);
                pstmt.setInt(4, lecturerId);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    // Synchronize active UserSession if matching current session
                    UserSession current = UserSession.getCurrentSession();
                    if (current != null && current.getUserId() == lecturerId) {
                        UserSession.setCurrentSession(new UserSession(
                                current.getUserId(),
                                current.getUsername(),
                                current.getRole(),
                                cleanName,
                                current.getDesignation(),
                                current.getDepartmentName(),
                                cleanEmail
                        ));
                    }
                    return new OperationResult(true, "Profile updated successfully!");
                } else {
                    return new OperationResult(false, "No record found for lecturer ID: " + lecturerId);
                }
            }
        } catch (SQLException e) {
            // Check for duplicate key error from MySQL constraint
            if (e.getErrorCode() == 1062 || (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate"))) {
                return new OperationResult(false, "This email address is already registered to another account.");
            }
            return new OperationResult(false, "Database update error: " + e.getMessage());
        }
    }

    // =========================================================================
    // PASSWORD VALIDATION & CHANGE METHODS
    // =========================================================================

    /**
     * Validates password fields and enforces security policy.
     *
     * @param currentPass Current password entered by user
     * @param newPass     New password entered by user
     * @param confirmPass Confirmation of new password
     * @return ValidationResult
     */
    public static ValidationResult validatePasswordForm(String currentPass, String newPass, String confirmPass) {
        // 1. Current password cannot be empty
        if (currentPass == null || currentPass.trim().isEmpty()) {
            return ValidationResult.fail("Please enter your current password.", "currentPassword");
        }

        // 2. New password cannot be empty
        if (newPass == null || newPass.trim().isEmpty()) {
            return ValidationResult.fail("Please enter a new password.", "newPassword");
        }

        // 3. Confirm password cannot be empty
        if (confirmPass == null || confirmPass.trim().isEmpty()) {
            return ValidationResult.fail("Please confirm your new password.", "confirmPassword");
        }

        String cleanNew = newPass.trim();
        String cleanConfirm = confirmPass.trim();
        String cleanCurrent = currentPass.trim();

        // 4. New and Confirm passwords must match
        if (!cleanNew.equals(cleanConfirm)) {
            return ValidationResult.fail("The new password and confirmation password do not match.", "confirmPassword");
        }

        // 5. New password cannot be the same as current password
        if (cleanNew.equals(cleanCurrent)) {
            return ValidationResult.fail("The new password cannot be the same as your current password.", "newPassword");
        }

        // 6. Enforce password complexity policy:
        // - At least 8 characters
        if (cleanNew.length() < 8) {
            return ValidationResult.fail("Password must be at least 8 characters long.", "newPassword");
        }

        // - Must contain at least one letter
        if (!cleanNew.matches(".*[a-zA-Z].*")) {
            return ValidationResult.fail("Password must contain at least one letter.", "newPassword");
        }

        // - Must contain at least one number
        if (!cleanNew.matches(".*[0-9].*")) {
            return ValidationResult.fail("Password must contain at least one number (0-9).", "newPassword");
        }

        return ValidationResult.ok();
    }

    /**
     * Verifies the current password against the `users` table and safely updates
     * the password for the authenticated lecturer only.
     *
     * Security notes:
     * - Passwords and hashes are never printed or logged.
     * - Parameterized SQL statements are used exclusively.
     *
     * @param lecturerId  Authenticated lecturer's user ID
     * @param currentPass Entered current password
     * @param newPass     Entered new password
     * @param confirmPass Entered password confirmation
     * @return OperationResult
     */
    public static OperationResult changePassword(int lecturerId, String currentPass, String newPass, String confirmPass) {
        if (lecturerId <= 0) {
            return new OperationResult(false, "Invalid session: Lecturer ID not found.");
        }

        // 1. Validate inputs against password policy
        ValidationResult val = validatePasswordForm(currentPass, newPass, confirmPass);
        if (!val.isValid()) {
            return new OperationResult(false, val.getMessage());
        }

        String cleanCurrent = currentPass.trim();
        String cleanNew = newPass.trim();

        // 2. Query and verify the current password stored in database
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return new OperationResult(false, "Database connection failed. Please ensure MySQL server is online.");
            }

            // Retrieve current password_hash from users table
            String verifySql = "SELECT password_hash FROM users WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(verifySql)) {
                pstmt.setInt(1, lecturerId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String storedPassword = rs.getString("password_hash");

                        // Verify current password match
                        if (storedPassword == null || !cleanCurrent.equals(storedPassword.trim())) {
                            return new OperationResult(false, "The current password you entered is incorrect.");
                        }
                    } else {
                        return new OperationResult(false, "User account record not found in database.");
                    }
                }
            }

            // 3. Update password in users table
            String updateSql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, cleanNew);
                updateStmt.setInt(2, lecturerId);

                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected > 0) {
                    return new OperationResult(true, "Password changed successfully! Please use your new password next time you log in.");
                } else {
                    return new OperationResult(false, "Failed to update password. Please try again.");
                }
            }
        } catch (SQLException e) {
            return new OperationResult(false, "Database error while updating password: " + e.getMessage());
        }
    }

    // =========================================================================
    // PROFILE PICTURE MANAGEMENT METHODS
    // =========================================================================

    // Directory path for profile uploads relative to workspace
    public static final String UPLOAD_DIR_PATH = "uploads/profiles";

    // 5 MB maximum image file size
    public static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024L * 1024L;

    // Allowed image formats
    public static final String[] ALLOWED_IMAGE_EXTENSIONS = {"png", "jpg", "jpeg", "webp", "gif", "bmp"};

    /**
     * Validates an image file before upload.
     * Enforces existence, file readability, size limit, extension, and ImageIO decoding.
     *
     * @param file The file selected by the lecturer
     * @return ValidationResult
     */
    public static ValidationResult validateProfilePicture(File file) {
        if (file == null) {
            return ValidationResult.fail("Please choose an image file to upload.", "profilePicture");
        }
        if (!file.exists() || !file.isFile()) {
            return ValidationResult.fail("The selected file does not exist or is not a regular file.", "profilePicture");
        }
        if (!file.canRead()) {
            return ValidationResult.fail("The selected file cannot be read. Check file permissions.", "profilePicture");
        }
        if (file.length() == 0) {
            return ValidationResult.fail("The selected file is empty (0 bytes).", "profilePicture");
        }
        if (file.length() > MAX_IMAGE_SIZE_BYTES) {
            return ValidationResult.fail("Image size exceeds the maximum allowed limit of 5 MB.", "profilePicture");
        }

        // Check file extension
        String name = file.getName().toLowerCase();
        int dot = name.lastIndexOf('.');
        if (dot < 0) {
            return ValidationResult.fail("The file must have an image extension (.png, .jpg, .jpeg, etc.).", "profilePicture");
        }
        String ext = name.substring(dot + 1);
        boolean allowed = false;
        for (String a : ALLOWED_IMAGE_EXTENSIONS) {
            if (a.equalsIgnoreCase(ext)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            return ValidationResult.fail("Unsupported image format. Allowed formats: PNG, JPG, JPEG, WEBP, GIF, BMP.", "profilePicture");
        }

        // Verify valid image header / decoding
        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) {
                return ValidationResult.fail("The selected file is not a valid or readable image.", "profilePicture");
            }
        } catch (IOException e) {
            return ValidationResult.fail("Unable to read image file: " + e.getMessage(), "profilePicture");
        }

        return ValidationResult.ok();
    }

    /**
     * Updates the lecturer's profile photo in the `users` table `profile_picture` column.
     * Copies the image file securely to the uploads directory and records the path.
     *
     * @param userId    Lecturer's user ID (users.user_id)
     * @param imageFile Selected image file
     * @return OperationResult
     */
    public static OperationResult updateProfilePicture(int userId, File imageFile) {
        if (userId <= 0) {
            return new OperationResult(false, "Invalid user session: User ID not found.");
        }

        // 1. Validate image file
        ValidationResult val = validateProfilePicture(imageFile);
        if (!val.isValid()) {
            return new OperationResult(false, val.getMessage());
        }

        try {
            // 2. Ensure target upload directory exists
            File dir = new File(UPLOAD_DIR_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 3. Generate unique filename: user_<userId>_<timestamp>.<ext>
            String originalName = imageFile.getName();
            int dot = originalName.lastIndexOf('.');
            String ext = (dot >= 0) ? originalName.substring(dot).toLowerCase() : ".png";
            String newFileName = "user_" + userId + "_" + System.currentTimeMillis() + ext;
            File destFile = new File(dir, newFileName);

            // 4. Copy image file to uploads folder
            Files.copy(imageFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // 5. Normalized path stored in MySQL users table
            String storedPath = UPLOAD_DIR_PATH + "/" + newFileName;

            // 6. Execute parameterized update on users table
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    return new OperationResult(false, "Database connection failed. Please ensure MySQL server is online.");
                }

                String sql = "UPDATE users SET profile_picture = ? WHERE user_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, storedPath);
                    pstmt.setInt(2, userId);

                    int rows = pstmt.executeUpdate();
                    if (rows > 0) {
                        return new OperationResult(true, "Profile photo updated successfully!");
                    } else {
                        return new OperationResult(false, "No user record found in database for ID: " + userId);
                    }
                }
            }
        } catch (IOException e) {
            return new OperationResult(false, "Failed to save image file: " + e.getMessage());
        } catch (SQLException e) {
            return new OperationResult(false, "Database error while updating profile picture: " + e.getMessage());
        }
    }

    /**
     * Resets the user's profile photo back to the default avatar ('default_avatar.png')
     * in the `users` table.
     *
     * @param userId Lecturer's user ID
     * @return OperationResult
     */
    public static OperationResult resetProfilePicture(int userId) {
        if (userId <= 0) {
            return new OperationResult(false, "Invalid user session: User ID not found.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                return new OperationResult(false, "Database connection failed. Please ensure MySQL server is online.");
            }

            String sql = "UPDATE users SET profile_picture = 'default_avatar.png' WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    return new OperationResult(true, "Profile photo reset to default avatar.");
                } else {
                    return new OperationResult(false, "No user record found for ID: " + userId);
                }
            }
        } catch (SQLException e) {
            return new OperationResult(false, "Database error: " + e.getMessage());
        }
    }

    /**
     * Attempts to resolve and decode a BufferedImage from the stored profile_picture path.
     * Checks relative file path, uploads directory, resources, and classpath.
     *
     * @param pathStr The profile_picture string from database users table
     * @return Decoded BufferedImage, or null if default or not found
     */
    public static BufferedImage resolveProfileImage(String pathStr) {
        if (pathStr == null || pathStr.trim().isEmpty() || "default_avatar.png".equalsIgnoreCase(pathStr.trim())) {
            return null;
        }

        String clean = pathStr.trim();

        // 1. Direct file path check
        File direct = new File(clean);
        if (direct.exists() && direct.isFile()) {
            try {
                BufferedImage img = ImageIO.read(direct);
                if (img != null) return img;
            } catch (Exception ignored) {}
        }

        // 2. Relative to UPLOAD_DIR_PATH
        File inUpload = new File(UPLOAD_DIR_PATH, new File(clean).getName());
        if (inUpload.exists() && inUpload.isFile()) {
            try {
                BufferedImage img = ImageIO.read(inUpload);
                if (img != null) return img;
            } catch (Exception ignored) {}
        }

        // 3. In src/main/resources/images
        File inRes = new File("src/main/resources/images", new File(clean).getName());
        if (inRes.exists() && inRes.isFile()) {
            try {
                BufferedImage img = ImageIO.read(inRes);
                if (img != null) return img;
            } catch (Exception ignored) {}
        }

        // 4. Classpath resource
        try {
            java.io.InputStream is = LecturerProfileLogic.class.getResourceAsStream("/images/" + new File(clean).getName());
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                if (img != null) return img;
            }
        } catch (Exception ignored) {}

        return null;
    }
}
