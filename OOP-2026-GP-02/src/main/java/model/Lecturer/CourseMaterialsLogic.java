package main.java.model.Lecturer;

import main.java.dao.DBConnection;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * CourseMaterialsLogic.java
 * TecFAMS - Faculty of Technology Academic Management System
 *
 * Dedicated business logic and data access class for Course Materials.
 * Separated from the UI layer to maintain clean OOP separation of concerns.
 *
 * Responsibilities:
 * - Parameterized JDBC queries for MySQL tecfams_db (course_materials, courses, lecturers)
 * - File validation (extensions, magic bytes signatures, file size limit)
 * - Safe filesystem storage in materials/ directory
 * - Atomicity & rollback cleanup if database insert fails
 * - Lecturer ownership verification before update/delete operations
 */
public class CourseMaterialsLogic {

    // Maximum allowed file size: 50 MB
    public static final long MAX_FILE_SIZE_BYTES = 50L * 1024L * 1024L;

    // Allowed file extensions
    public static final String[] ALLOWED_EXTENSIONS = {"pdf", "doc", "docx", "ppt", "pptx"};

    // Default materials storage directory relative to project root
    private static final String STORAGE_DIR_NAME = "materials";

    // =========================================================================
    // INNER DATA STRUCTURES (Clean Models)
    // =========================================================================

    /**
     * Represents a single course material entity matching the MySQL schema.
     */
    public static class CourseMaterialItem {
        private final int materialId;
        private final int courseId;
        private final String courseCode;
        private final String courseName;
        private final int lecturerId;
        private final String title;
        private final String description;
        private final String filePath;
        private final String fileName;
        private final String fileType; // "PDF", "Word", "PowerPoint", "Other"
        private final String uploadedDate;
        private final long fileSize;
        private final String fileSizeFormatted;

        public CourseMaterialItem(int materialId, int courseId, String courseCode, String courseName,
                                  int lecturerId, String title, String description,
                                  String filePath, String uploadedDate) {
            this.materialId = materialId;
            this.courseId = courseId;
            this.courseCode = courseCode != null ? courseCode : "N/A";
            this.courseName = courseName != null ? courseName : "Unknown Course";
            this.lecturerId = lecturerId;
            this.title = title != null ? title : "Untitled";
            this.description = description != null ? description : "";
            this.filePath = filePath != null ? filePath : "";
            this.uploadedDate = uploadedDate != null ? uploadedDate : "";

            // Derive file name from path
            File diskFile = resolveStorageFile(this.filePath);
            this.fileName = diskFile.getName();

            // Derive file type from extension
            this.fileType = detectFileType(this.fileName);

            // Derive file size from disk if available
            if (diskFile.exists() && diskFile.isFile()) {
                this.fileSize = diskFile.length();
                this.fileSizeFormatted = formatFileSize(this.fileSize);
            } else {
                this.fileSize = 0;
                this.fileSizeFormatted = "Not on disk";
            }
        }

        public int getMaterialId() { return materialId; }
        public int getCourseId() { return courseId; }
        public String getCourseCode() { return courseCode; }
        public String getCourseName() { return courseName; }
        public int getLecturerId() { return lecturerId; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getFilePath() { return filePath; }
        public String getFileName() { return fileName; }
        public String getFileType() { return fileType; }
        public String getUploadedDate() { return uploadedDate; }
        public long getFileSize() { return fileSize; }
        public String getFileSizeFormatted() { return fileSizeFormatted; }

        public boolean existsOnDisk() {
            File f = resolveStorageFile(this.filePath);
            return f.exists() && f.isFile();
        }
    }

    /**
     * Represents an assigned course item for dropdown selectors.
     */
    public static class CourseItem {
        private final int courseId;
        private final String courseCode;
        private final String courseName;

        public CourseItem(int courseId, String courseCode, String courseName) {
            this.courseId = courseId;
            this.courseCode = courseCode;
            this.courseName = courseName;
        }

        public int getCourseId() { return courseId; }
        public String getCourseCode() { return courseCode; }
        public String getCourseName() { return courseName; }

        @Override
        public String toString() {
            return courseCode + " - " + courseName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CourseItem that = (CourseItem) o;
            return courseId == that.courseId;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(courseId);
        }
    }

    /**
     * Summary statistics container for the 4 summary cards.
     */
    public static class MaterialsSummary {
        private final int totalCount;
        private final int pdfCount;
        private final int wordCount;
        private final int pptCount;

        public MaterialsSummary(int totalCount, int pdfCount, int wordCount, int pptCount) {
            this.totalCount = totalCount;
            this.pdfCount = pdfCount;
            this.wordCount = wordCount;
            this.pptCount = pptCount;
        }

        public int getTotalCount() { return totalCount; }
        public int getPdfCount() { return pdfCount; }
        public int getWordCount() { return wordCount; }
        public int getPptCount() { return pptCount; }
    }

    /**
     * File validation result.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult ok() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult fail(String msg) {
            return new ValidationResult(false, msg);
        }

        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
    }

    // =========================================================================
    // DATABASE OPERATIONS: RETRIEVAL & FILTERING
    // =========================================================================

    /**
     * Retrieves assigned courses for an authenticated lecturer.
     * Uses lecturer's department from the MySQL database, plus any courses where materials already exist.
     */
    public static List<CourseItem> getAssignedCourses(int lecturerId) throws SQLException {
        if (lecturerId <= 0) {
            throw new IllegalArgumentException("Invalid lecturer ID: " + lecturerId);
        }

        List<CourseItem> list = new ArrayList<>();
        String sql = "SELECT DISTINCT c.course_id, c.course_code, c.course_name " +
                     "FROM courses c " +
                     "WHERE c.department_id = (SELECT department_id FROM lecturers WHERE lecturer_id = ?) " +
                     "   OR c.course_id IN (SELECT course_id FROM course_materials WHERE lecturer_id = ?) " +
                     "ORDER BY c.course_code ASC";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database 'tecfams_db'. Please check that MySQL server is running.");
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, lecturerId);
                pstmt.setInt(2, lecturerId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(new CourseItem(
                                rs.getInt("course_id"),
                                rs.getString("course_code"),
                                rs.getString("course_name")
                        ));
                    }
                }
            }
        }
        return list;
    }

    /**
     * Calculates genuine summary counts for the authenticated lecturer from MySQL database.
     */
    public static MaterialsSummary getSummary(int lecturerId) throws SQLException {
        if (lecturerId <= 0) {
            return new MaterialsSummary(0, 0, 0, 0);
        }

        String sql = "SELECT " +
                     "    COUNT(*) AS total_count, " +
                     "    SUM(CASE WHEN LOWER(file_path) LIKE '%.pdf' THEN 1 ELSE 0 END) AS pdf_count, " +
                     "    SUM(CASE WHEN LOWER(file_path) LIKE '%.doc' OR LOWER(file_path) LIKE '%.docx' THEN 1 ELSE 0 END) AS word_count, " +
                     "    SUM(CASE WHEN LOWER(file_path) LIKE '%.ppt' OR LOWER(file_path) LIKE '%.pptx' THEN 1 ELSE 0 END) AS ppt_count " +
                     "FROM course_materials " +
                     "WHERE lecturer_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database 'tecfams_db'.");
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, lecturerId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int total = rs.getInt("total_count");
                        int pdf = rs.getInt("pdf_count");
                        int word = rs.getInt("word_count");
                        int ppt = rs.getInt("ppt_count");
                        return new MaterialsSummary(total, pdf, word, ppt);
                    }
                }
            }
        }
        return new MaterialsSummary(0, 0, 0, 0);
    }

    /**
     * Retrieves course materials uploaded by the lecturer with optional course, file type, and text filters.
     */
    public static List<CourseMaterialItem> getMaterials(int lecturerId, Integer courseIdFilter,
                                                        String fileTypeFilter, String searchQuery) throws SQLException {
        if (lecturerId <= 0) {
            return new ArrayList<>();
        }

        List<CourseMaterialItem> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT cm.material_id, cm.course_id, c.course_code, c.course_name, cm.lecturer_id, ")
           .append("       cm.material_title, cm.material_description, cm.file_path, cm.uploaded_date ")
           .append("FROM course_materials cm ")
           .append("JOIN courses c ON cm.course_id = c.course_id ")
           .append("WHERE cm.lecturer_id = ? ");

        List<Object> params = new ArrayList<>();
        params.add(lecturerId);

        // Course Filter
        if (courseIdFilter != null && courseIdFilter > 0) {
            sql.append("AND cm.course_id = ? ");
            params.add(courseIdFilter);
        }

        // File Type Filter
        if (fileTypeFilter != null && !fileTypeFilter.equalsIgnoreCase("All") && !fileTypeFilter.equalsIgnoreCase("All Types")) {
            if (fileTypeFilter.equalsIgnoreCase("PDF")) {
                sql.append("AND LOWER(cm.file_path) LIKE '%.pdf' ");
            } else if (fileTypeFilter.equalsIgnoreCase("Word")) {
                sql.append("AND (LOWER(cm.file_path) LIKE '%.doc' OR LOWER(cm.file_path) LIKE '%.docx') ");
            } else if (fileTypeFilter.equalsIgnoreCase("PowerPoint")) {
                sql.append("AND (LOWER(cm.file_path) LIKE '%.ppt' OR LOWER(cm.file_path) LIKE '%.pptx') ");
            }
        }

        // Text Search Query (Title or File Name)
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql.append("AND (LOWER(cm.material_title) LIKE ? OR LOWER(cm.file_path) LIKE ? OR LOWER(c.course_code) LIKE ?) ");
            String wild = "%" + searchQuery.trim().toLowerCase() + "%";
            params.add(wild);
            params.add(wild);
            params.add(wild);
        }

        sql.append("ORDER BY cm.uploaded_date DESC, cm.material_id DESC");

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database 'tecfams_db'.");
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    pstmt.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(new CourseMaterialItem(
                                rs.getInt("material_id"),
                                rs.getInt("course_id"),
                                rs.getString("course_code"),
                                rs.getString("course_name"),
                                rs.getInt("lecturer_id"),
                                rs.getString("material_title"),
                                rs.getString("material_description"),
                                rs.getString("file_path"),
                                rs.getString("uploaded_date")
                        ));
                    }
                }
            }
        }
        return list;
    }

    // =========================================================================
    // FILE VALIDATION (Security, Size, Extensions & Magic Bytes)
    // =========================================================================

    /**
     * Validates a selected document file before uploading.
     * Enforces existence, readability, size limit, extension allowlist, and file header signatures.
     */
    public static ValidationResult validateFile(File file) {
        if (file == null) {
            return ValidationResult.fail("Please choose a file to upload.");
        }
        if (!file.exists()) {
            return ValidationResult.fail("The selected file does not exist on your computer.");
        }
        if (!file.isFile()) {
            return ValidationResult.fail("The selected path is not a regular file.");
        }
        if (!file.canRead()) {
            return ValidationResult.fail("The selected file cannot be read. Check operating system permissions.");
        }
        if (file.length() == 0) {
            return ValidationResult.fail("The selected file is empty (0 bytes).");
        }
        if (file.length() > MAX_FILE_SIZE_BYTES) {
            return ValidationResult.fail("File size (" + formatFileSize(file.length()) +
                    ") exceeds the allowed limit of 50 MB.");
        }

        String name = file.getName();
        String ext = getFileExtension(name).toLowerCase();
        if (!isAllowedExtension(ext)) {
            return ValidationResult.fail("Unsupported file format: ." + ext +
                    ". Only PDF (.pdf), Word (.doc, .docx), and PowerPoint (.ppt, .pptx) files are permitted.");
        }

        // Verify Magic Bytes / File Header Signatures
        ValidationResult sigResult = verifyFileSignature(file, ext);
        if (!sigResult.isValid()) {
            return sigResult;
        }

        return ValidationResult.ok();
    }

    /**
     * Inspects initial file signature bytes to reject renamed executables or incompatible files.
     */
    private static ValidationResult verifyFileSignature(File file, String ext) {
        byte[] header = new byte[8];
        try (FileInputStream fis = new FileInputStream(file)) {
            int bytesRead = fis.read(header);
            if (bytesRead < 4) {
                return ValidationResult.fail("File is too small to verify document signature.");
            }

            if ("pdf".equals(ext)) {
                // PDF files start with %PDF- (0x25 0x50 0x44 0x46)
                if (header[0] != 0x25 || header[1] != 0x50 || header[2] != 0x44 || header[3] != 0x46) {
                    return ValidationResult.fail("File content does not match standard PDF document structure (%PDF header missing).");
                }
            } else if ("docx".equals(ext) || "pptx".equals(ext)) {
                // DOCX and PPTX are OpenXML ZIP archives starting with PK (0x50 0x4B 0x03 0x04)
                if (header[0] != 0x50 || header[1] != 0x4B || header[2] != 0x03 || header[3] != 0x04) {
                    return ValidationResult.fail("File content does not match modern Office document structure (ZIP container missing).");
                }
            } else if ("doc".equals(ext) || "ppt".equals(ext)) {
                // Legacy Office files start with OLE CFB header: 0xD0 0xCF 0x11 0xE0
                if ((header[0] & 0xFF) != 0xD0 || (header[1] & 0xFF) != 0xCF ||
                    (header[2] & 0xFF) != 0x11 || (header[3] & 0xFF) != 0xE0) {
                    return ValidationResult.fail("File content does not match binary Microsoft Office document structure.");
                }
            }
        } catch (IOException e) {
            return ValidationResult.fail("Could not read file signature: " + e.getMessage());
        }
        return ValidationResult.ok();
    }

    // =========================================================================
    // UPLOAD, UPDATE & DELETE OPERATIONS
    // =========================================================================

    /**
     * Uploads a new course material.
     * 1. Validates inputs and course assignment.
     * 2. Copies file to the materials/ directory with safe sanitized name.
     * 3. Inserts metadata record into course_materials table.
     * 4. Cleans up orphaned file if database insert fails.
     */
    public static int uploadMaterial(int lecturerId, int courseId, String title,
                                     String description, File sourceFile) throws Exception {
        if (lecturerId <= 0) {
            throw new IllegalArgumentException("Invalid lecturer ID.");
        }
        if (courseId <= 0) {
            throw new IllegalArgumentException("Please select a valid course.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Material title cannot be empty.");
        }
        String cleanTitle = title.trim();
        if (cleanTitle.length() > 150) {
            cleanTitle = cleanTitle.substring(0, 150);
        }

        // Validate selected file
        ValidationResult valRes = validateFile(sourceFile);
        if (!valRes.isValid()) {
            throw new IllegalArgumentException(valRes.getErrorMessage());
        }

        // Verify that the course is assigned to the lecturer
        if (!isCourseAssignedToLecturer(lecturerId, courseId)) {
            throw new IllegalAccessException("Unauthorized: You are not assigned to course ID " + courseId + ".");
        }

        // Prepare storage directory
        File storageDir = getStorageDirectory();

        // Generate safe unique filename: <timestamp>_<cleanOriginalName>
        String ext = getFileExtension(sourceFile.getName());
        String base = getBaseName(sourceFile.getName()).replaceAll("[^a-zA-Z0-9_-]", "_");
        if (base.isEmpty()) base = "material";
        String uniqueFileName = System.currentTimeMillis() + "_" + base + "." + ext;

        File targetFile = new File(storageDir, uniqueFileName);

        // Copy file safely
        try {
            Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Failed to save material to storage: " + e.getMessage(), e);
        }

        // Database Relative Path convention: materials/<filename>
        String dbFilePath = STORAGE_DIR_NAME + "/" + uniqueFileName;

        // Perform Database Insert
        String sql = "INSERT INTO course_materials (course_id, lecturer_id, material_title, material_description, file_path, uploaded_date) " +
                     "VALUES (?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                // Rollback copied file
                targetFile.delete();
                throw new SQLException("Unable to connect to MySQL database 'tecfams_db'. Upload rolled back.");
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, courseId);
                pstmt.setInt(2, lecturerId);
                pstmt.setString(3, cleanTitle);
                pstmt.setString(4, description != null ? description.trim() : null);
                pstmt.setString(5, dbFilePath);

                int affected = pstmt.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            return rs.getInt(1);
                        }
                    }
                }
            } catch (SQLException ex) {
                // Rollback copied file on database failure to avoid orphan files
                targetFile.delete();
                throw new SQLException("Failed to save course material metadata: " + ex.getMessage(), ex);
            }
        }

        return -1;
    }

    /**
     * Updates metadata (title, description, course) of an existing material.
     * Enforces that the material belongs to the authenticated lecturer.
     */
    public static void updateMaterial(int lecturerId, int materialId, int courseId,
                                      String title, String description) throws Exception {
        if (lecturerId <= 0 || materialId <= 0) {
            throw new IllegalArgumentException("Invalid lecturer ID or material ID.");
        }
        if (courseId <= 0) {
            throw new IllegalArgumentException("Please select a valid course.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Material title cannot be empty.");
        }
        String cleanTitle = title.trim();
        if (cleanTitle.length() > 150) {
            cleanTitle = cleanTitle.substring(0, 150);
        }

        // Verify that destination course is assigned to the lecturer
        if (!isCourseAssignedToLecturer(lecturerId, courseId)) {
            throw new IllegalAccessException("Unauthorized: Course ID " + courseId + " is not assigned to you.");
        }

        String sql = "UPDATE course_materials " +
                     "SET course_id = ?, material_title = ?, material_description = ? " +
                     "WHERE material_id = ? AND lecturer_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database 'tecfams_db'.");
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, courseId);
                pstmt.setString(2, cleanTitle);
                pstmt.setString(3, description != null ? description.trim() : null);
                pstmt.setInt(4, materialId);
                pstmt.setInt(5, lecturerId);

                int rows = pstmt.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("Update failed. You may not be authorized to modify this material.");
                }
            }
        }
    }

    /**
     * Deletes a course material from database and its stored file from filesystem.
     * Enforces ownership check and checks for shared file references before deletion.
     */
    public static void deleteMaterial(int lecturerId, int materialId) throws Exception {
        if (lecturerId <= 0 || materialId <= 0) {
            throw new IllegalArgumentException("Invalid lecturer ID or material ID.");
        }

        String selectSql = "SELECT file_path FROM course_materials WHERE material_id = ? AND lecturer_id = ?";
        String deleteSql = "DELETE FROM course_materials WHERE material_id = ? AND lecturer_id = ?";
        String countSql = "SELECT COUNT(*) FROM course_materials WHERE file_path = ?";

        String storedPath = null;

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database 'tecfams_db'.");
            }

            // 1. Verify ownership and get file_path
            try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
                pstmt.setInt(1, materialId);
                pstmt.setInt(2, lecturerId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        storedPath = rs.getString("file_path");
                    } else {
                        throw new IllegalAccessException("Material not found or you are not authorized to delete it.");
                    }
                }
            }

            // 2. Delete database record
            try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                pstmt.setInt(1, materialId);
                pstmt.setInt(2, lecturerId);
                int deleted = pstmt.executeUpdate();
                if (deleted == 0) {
                    throw new SQLException("Could not delete record from database.");
                }
            }

            // 3. Safely delete file if no other material references it
            if (storedPath != null && !storedPath.trim().isEmpty()) {
                boolean isShared = false;
                try (PreparedStatement pstmt = conn.prepareStatement(countSql)) {
                    pstmt.setString(1, storedPath);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            isShared = true; // Still referenced by another record
                        }
                    }
                }

                if (!isShared) {
                    File target = resolveStorageFile(storedPath);
                    // Ensure the file is inside the configured materials directory to prevent accidental arbitrary deletion
                    if (target.exists() && isInsideStorageDir(target)) {
                        target.delete();
                    }
                }
            }
        }
    }

    // =========================================================================
    // OPEN & DOWNLOAD OPERATIONS
    // =========================================================================

    /**
     * Opens the stored document file using the system default application via Desktop.open().
     */
    public static void openFile(String filePath) throws Exception {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new FileNotFoundException("File path is empty.");
        }

        File file = resolveStorageFile(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException("The file '" + file.getName() +
                    "' was not found in the materials storage directory.\nPath: " + file.getAbsolutePath());
        }

        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            throw new UnsupportedOperationException("System desktop integration is not supported on this platform.");
        }

        Desktop.getDesktop().open(file);
    }

    /**
     * Downloads/saves a copy of the stored file to a destination file chosen by the user.
     */
    public static void downloadFile(String filePath, File destination) throws Exception {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new FileNotFoundException("File path is empty.");
        }
        if (destination == null) {
            throw new IllegalArgumentException("Destination file cannot be null.");
        }

        File source = resolveStorageFile(filePath);
        if (!source.exists() || !source.isFile()) {
            throw new FileNotFoundException("The source material file was not found in storage: " + source.getName());
        }

        File parent = destination.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    // =========================================================================
    // HELPER UTILITIES
    // =========================================================================

    /**
     * Checks if a course is assigned to the lecturer.
     */
    public static boolean isCourseAssignedToLecturer(int lecturerId, int courseId) throws SQLException {
        List<CourseItem> courses = getAssignedCourses(lecturerId);
        for (CourseItem c : courses) {
            if (c.getCourseId() == courseId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolves the storage file relative to working directory or project root.
     */
    public static File resolveStorageFile(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return new File("");
        }

        // Check as-is (e.g., "materials/file.pdf")
        File f1 = new File(relativePath);
        if (f1.exists()) return f1;

        // Check in current materials directory
        File f2 = new File(STORAGE_DIR_NAME, new File(relativePath).getName());
        if (f2.exists()) return f2;

        // Check resources directory
        File f3 = new File("src/main/resources/materials", new File(relativePath).getName());
        if (f3.exists()) return f3;

        // Default to storage directory target
        return f2;
    }

    /**
     * Ensures materials/ storage directory exists and returns it.
     */
    public static File getStorageDirectory() {
        File dir = new File(STORAGE_DIR_NAME);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * Security check: Ensures target file is strictly inside the materials/ storage directory.
     */
    private static boolean isInsideStorageDir(File file) {
        try {
            File storageDir = getStorageDirectory().getCanonicalFile();
            File canonicalFile = file.getCanonicalFile();
            return canonicalFile.getPath().startsWith(storageDir.getPath());
        } catch (IOException e) {
            return false;
        }
    }

    public static String getFileExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return (dot >= 0 && dot < filename.length() - 1) ? filename.substring(dot + 1) : "";
    }

    public static String getBaseName(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(0, dot) : filename;
    }

    public static boolean isAllowedExtension(String ext) {
        if (ext == null) return false;
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equalsIgnoreCase(ext)) return true;
        }
        return false;
    }

    public static String detectFileType(String filename) {
        String ext = getFileExtension(filename).toLowerCase();
        if ("pdf".equals(ext)) return "PDF";
        if ("doc".equals(ext) || "docx".equals(ext)) return "Word";
        if ("ppt".equals(ext) || "pptx".equals(ext)) return "PowerPoint";
        return "Other";
    }

    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        if (digitGroups >= units.length) digitGroups = units.length - 1;
        return new DecimalFormat("#,##0.#").format(bytes / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
