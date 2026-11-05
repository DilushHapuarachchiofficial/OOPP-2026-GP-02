package main.java.model.Lecturer;

import main.java.dao.DBConnection;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CourseMaterialsLogic {

    // Maximum allowed file size: 50 MB
    public static final long MAX_FILE_SIZE_BYTES = 50L * 1024L * 1024L;

    // Allowed file extensions
    public static final String[] ALLOWED_EXTENSIONS = {"pdf", "doc", "docx", "ppt", "pptx"};

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
        private final String fileName;
        private final String filePath;
        private final String fileType; // "PDF", "Word", "PowerPoint", "Other"
        private final String uploadedDate;
        private final long fileSize;
        private final String fileSizeFormatted;
        private final String fromDate;
        private final String dueDate;

        public CourseMaterialItem(int materialId, int courseId, String courseCode, String courseName,
                                  int lecturerId, String title, String description,
                                  String fileName, long fileSize, String uploadedDate,
                                  String fromDate, String dueDate) {
            this.materialId = materialId;
            this.courseId = courseId;
            this.courseCode = courseCode != null ? courseCode : "N/A";
            this.courseName = courseName != null ? courseName : "Unknown Course";
            this.lecturerId = lecturerId;
            this.title = title != null ? title : "Untitled";
            this.description = description != null ? description : "";
            this.fileName = fileName != null && !fileName.trim().isEmpty() ? fileName : "material";
            this.filePath = this.fileName; // Backwards compatibility
            this.uploadedDate = uploadedDate != null ? uploadedDate : "";
            this.fileSize = fileSize > 0 ? fileSize : 0;
            this.fileSizeFormatted = formatFileSize(this.fileSize);
            this.fileType = detectFileType(this.fileName);
            this.fromDate = (fromDate != null && !fromDate.trim().isEmpty()) ? fromDate.trim() : null;
            this.dueDate = (dueDate != null && !dueDate.trim().isEmpty()) ? dueDate.trim() : null;
        }

        public CourseMaterialItem(int materialId, int courseId, String courseCode, String courseName,
                                  int lecturerId, String title, String description,
                                  String filePath, String uploadedDate) {
            this(materialId, courseId, courseCode, courseName, lecturerId, title, description,
                 filePath != null ? new File(filePath).getName() : "material", 0, uploadedDate, null, null);
        }

        public CourseMaterialItem(int materialId, int courseId, String courseCode, String courseName,
                                  int lecturerId, String title, String description,
                                  String fileName, long fileSize, String uploadedDate) {
            this(materialId, courseId, courseCode, courseName, lecturerId, title, description,
                 fileName, fileSize, uploadedDate, null, null);
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
        public String getFromDate() { return fromDate; }
        public String getDueDate() { return dueDate; }

        /**
         * Checks whether this material is currently visible to students based on from_date and due_date.
         * If neither date is set, it is always visible.
         */
        public boolean isCurrentlyVisibleToStudents() {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate from = parseLocalDate(fromDate);
            java.time.LocalDate due = parseLocalDate(dueDate);

            if (from == null && due == null) {
                return true;
            }
            if (from != null && due != null) {
                return !today.isBefore(from) && !today.isAfter(due);
            }
            if (from == null && due != null) {
                return !today.isAfter(due);
            }
            if (from != null && due == null) {
                return !today.isBefore(from);
            }
            return true;
        }

        /**
         * Returns a status descriptor for the material's availability window.
         * ("Always Visible", "Active", "Scheduled", "Expired")
         */
        public String getVisibilityStatus() {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate from = parseLocalDate(fromDate);
            java.time.LocalDate due = parseLocalDate(dueDate);

            if (from == null && due == null) {
                return "Always Visible";
            }
            if (due != null && today.isAfter(due)) {
                return "Expired";
            }
            if (from != null && today.isBefore(from)) {
                return "Scheduled";
            }
            return "Active";
        }

        /**
         * Returns a readable summary of the date range restriction.
         */
        public String getAvailabilityDescription() {
            if (fromDate == null && dueDate == null) {
                return "Always Visible";
            }
            if (fromDate != null && dueDate != null) {
                return fromDate + " to " + dueDate;
            }
            if (dueDate != null) {
                return "Until " + dueDate;
            }
            return "From " + fromDate + " onwards";
        }

        private static java.time.LocalDate parseLocalDate(String str) {
            if (str == null || str.trim().isEmpty()) return null;
            try {
                String clean = str.trim().replace('/', '-').replace('.', '-');
                String[] p = clean.split("-");
                if (p.length == 3) {
                    return java.time.LocalDate.of(
                            Integer.parseInt(p[0].trim()),
                            Integer.parseInt(p[1].trim()),
                            Integer.parseInt(p[2].trim())
                    );
                }
            } catch (Exception ignored) {}
            return null;
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
        private final int totalMaterials;
        private final int pdfCount;
        private final int wordCount;
        private final int pptCount;

        public MaterialsSummary(int totalMaterials, int pdfCount, int wordCount, int pptCount) {
            this.totalMaterials = totalMaterials;
            this.pdfCount = pdfCount;
            this.wordCount = wordCount;
            this.pptCount = pptCount;
        }

        public int getTotalMaterials() { return totalMaterials; }
        public int getTotalCount() { return totalMaterials; }
        public int getPdfCount() { return pdfCount; }
        public int getWordCount() { return wordCount; }
        public int getPptCount() { return pptCount; }
    }


    /**
     * Result of file validation checks.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
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
    // SCHEMA INITIALIZATION & MIGRATION
    // =========================================================================

    /**
     * Ensures that the database schema contains the required tables and columns
     * for direct database file storage and course assignments.
     */
    public static void ensureDatabaseSchema(Connection conn) {
        if (conn == null) return;
        try (Statement stmt = conn.createStatement()) {
            // 1. Ensure course_lecturers assignment table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS course_lecturers (" +
                "  assignment_id INT(11) NOT NULL AUTO_INCREMENT," +
                "  course_id INT(11) NOT NULL," +
                "  lecturer_id INT(11) NOT NULL," +
                "  PRIMARY KEY (assignment_id)," +
                "  UNIQUE KEY uq_course_lecturer (course_id, lecturer_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
            );

            // Populate default course assignments if table is empty
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM course_lecturers")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.executeUpdate(
                        "INSERT IGNORE INTO course_lecturers (assignment_id, course_id, lecturer_id) VALUES " +
                        "(1, 1, 4), (2, 2, 5), (3, 3, 6), (4, 4, 11)"
                    );
                }
            }

            // 2. Ensure course_materials table columns for DB storage
            try {
                stmt.executeUpdate("ALTER TABLE course_materials ADD COLUMN file_name VARCHAR(255) NULL AFTER material_description");
            } catch (SQLException ignored) {}
            try {
                stmt.executeUpdate("ALTER TABLE course_materials ADD COLUMN file_data LONGBLOB NULL AFTER file_name");
            } catch (SQLException ignored) {}
            try {
                stmt.executeUpdate("ALTER TABLE course_materials ADD COLUMN file_size BIGINT DEFAULT 0 AFTER file_data");
            } catch (SQLException ignored) {}
            try {
                stmt.executeUpdate("ALTER TABLE course_materials ADD COLUMN file_path VARCHAR(255) NULL DEFAULT NULL AFTER file_name");
            } catch (SQLException ignored) {}
            try {
                stmt.executeUpdate("ALTER TABLE course_materials ADD COLUMN from_date DATE NULL DEFAULT NULL");
            } catch (SQLException ignored) {}
            try {
                stmt.executeUpdate("ALTER TABLE course_materials ADD COLUMN due_date DATE NULL DEFAULT NULL");
            } catch (SQLException ignored) {}
        } catch (Exception ignored) {
            // Non-critical if user does not have DDL privileges
        }
    }

    // =========================================================================
    // DATABASE OPERATIONS: RETRIEVAL & FILTERING
    // =========================================================================

    /**
     * Retrieves assigned courses for an authenticated lecturer.
     * Enforces that the lecturer can ONLY view and select courses specifically assigned to him/her.
     */
    public static List<CourseItem> getAssignedCourses(int lecturerId) throws SQLException {
        if (lecturerId <= 0) {
            throw new IllegalArgumentException("Invalid lecturer ID: " + lecturerId);
        }

        List<CourseItem> list = new ArrayList<>();
        String sql = "SELECT DISTINCT c.course_id, c.course_code, c.course_name " +
                     "FROM courses c " +
                     "JOIN course_lecturers cl ON c.course_id = cl.course_id " +
                     "WHERE cl.lecturer_id = ? " +
                     "ORDER BY c.course_code ASC";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database 'tecfams_db'. Please check that MySQL server is running.");
            }
            ensureDatabaseSchema(conn);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, lecturerId);
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

            // Fallback: If no explicit course_lecturers assignment exists, check marks uploaded_by
            if (list.isEmpty()) {
                String fallbackSql = "SELECT DISTINCT c.course_id, c.course_code, c.course_name " +
                                     "FROM courses c " +
                                     "JOIN evaluation_components ec ON c.course_id = ec.course_id " +
                                     "JOIN marks m ON ec.component_id = m.component_id " +
                                     "WHERE m.uploaded_by = ? " +
                                     "ORDER BY c.course_code ASC";
                try (PreparedStatement fallbackStmt = conn.prepareStatement(fallbackSql)) {
                    fallbackStmt.setInt(1, lecturerId);
                    try (ResultSet rs = fallbackStmt.executeQuery()) {
                        while (rs.next()) {
                            CourseItem item = new CourseItem(
                                    rs.getInt("course_id"),
                                    rs.getString("course_code"),
                                    rs.getString("course_name")
                            );
                            if (!list.contains(item)) {
                                list.add(item);
                            }
                        }
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
                     "    SUM(CASE WHEN LOWER(COALESCE(file_name, file_path, '')) LIKE '%.pdf' THEN 1 ELSE 0 END) AS pdf_count, " +
                     "    SUM(CASE WHEN LOWER(COALESCE(file_name, file_path, '')) LIKE '%.doc' OR LOWER(COALESCE(file_name, file_path, '')) LIKE '%.docx' THEN 1 ELSE 0 END) AS word_count, " +
                     "    SUM(CASE WHEN LOWER(COALESCE(file_name, file_path, '')) LIKE '%.ppt' OR LOWER(COALESCE(file_name, file_path, '')) LIKE '%.pptx' THEN 1 ELSE 0 END) AS ppt_count " +
                     "FROM course_materials " +
                     "WHERE lecturer_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database 'tecfams_db'.");
            }
            ensureDatabaseSchema(conn);
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
           .append("       cm.material_title, cm.material_description, ")
           .append("       COALESCE(cm.file_name, cm.file_path, 'material') AS file_name, ")
           .append("       COALESCE(cm.file_size, LENGTH(cm.file_data), 0) AS file_size, ")
           .append("       cm.uploaded_date, cm.from_date, cm.due_date ")
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
                sql.append("AND LOWER(COALESCE(cm.file_name, cm.file_path, '')) LIKE '%.pdf' ");
            } else if (fileTypeFilter.equalsIgnoreCase("Word")) {
                sql.append("AND (LOWER(COALESCE(cm.file_name, cm.file_path, '')) LIKE '%.doc' OR LOWER(COALESCE(cm.file_name, cm.file_path, '')) LIKE '%.docx') ");
            } else if (fileTypeFilter.equalsIgnoreCase("PowerPoint")) {
                sql.append("AND (LOWER(COALESCE(cm.file_name, cm.file_path, '')) LIKE '%.ppt' OR LOWER(COALESCE(cm.file_name, cm.file_path, '')) LIKE '%.pptx') ");
            }
        }

        // Text Search Query (Title or File Name)
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql.append("AND (LOWER(cm.material_title) LIKE ? OR LOWER(COALESCE(cm.file_name, cm.file_path, '')) LIKE ? OR LOWER(c.course_code) LIKE ?) ");
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
            ensureDatabaseSchema(conn);
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
                                rs.getString("file_name"),
                                rs.getLong("file_size"),
                                rs.getString("uploaded_date"),
                                rs.getString("from_date"),
                                rs.getString("due_date")
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
    // UPLOAD, UPDATE & DELETE OPERATIONS (Direct MySQL Database BLOB)
    // =========================================================================

    /**
     * Uploads a new course material directly into the MySQL database.
     * 1. Validates inputs and that the course is assigned to the lecturer.
     * 2. Inserts material metadata and raw file bytes (LONGBLOB) directly into course_materials table.
     * 3. No local filesystem storage directory used.
     */
    public static int uploadMaterial(int lecturerId, int courseId, String title,
                                     String description, File sourceFile,
                                     String fromDateStr, String dueDateStr) throws Exception {
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

        // Date Parsing & Validation
        java.sql.Date sqlFromDate = parseSqlDate(fromDateStr);
        java.sql.Date sqlDueDate = parseSqlDate(dueDateStr);

        // Rule: If lecturer only fills due date and not from date, default from date to today (upload date)
        if (sqlDueDate != null && sqlFromDate == null) {
            sqlFromDate = java.sql.Date.valueOf(java.time.LocalDate.now());
        }

        // Validation: due date must not be before from date
        if (sqlFromDate != null && sqlDueDate != null && sqlDueDate.before(sqlFromDate)) {
            throw new IllegalArgumentException("Due Date (" + sqlDueDate + ") cannot be earlier than From Date (" + sqlFromDate + ").");
        }

        String fileName = sourceFile.getName();
        long fileSize = sourceFile.length();

        // Perform Database Insert directly into course_materials table
        String sql = "INSERT INTO course_materials (course_id, lecturer_id, material_title, material_description, file_name, file_data, file_size, uploaded_date, from_date, due_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database 'tecfams_db'. Please ensure MySQL is running.");
            }
            ensureDatabaseSchema(conn);

            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                 FileInputStream fis = new FileInputStream(sourceFile)) {
                pstmt.setInt(1, courseId);
                pstmt.setInt(2, lecturerId);
                pstmt.setString(3, cleanTitle);
                pstmt.setString(4, description != null ? description.trim() : null);
                pstmt.setString(5, fileName);
                pstmt.setBinaryStream(6, fis, (int) fileSize);
                pstmt.setLong(7, fileSize);
                if (sqlFromDate != null) {
                    pstmt.setDate(8, sqlFromDate);
                } else {
                    pstmt.setNull(8, java.sql.Types.DATE);
                }
                if (sqlDueDate != null) {
                    pstmt.setDate(9, sqlDueDate);
                } else {
                    pstmt.setNull(9, java.sql.Types.DATE);
                }

                int affected = pstmt.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            return rs.getInt(1);
                        }
                    }
                }
            } catch (SQLException ex) {
                throw new SQLException("Failed to save course material to database: " + ex.getMessage(), ex);
            }
        }

        return -1;
    }

    /**
     * Backwards compatible uploadMaterial overload.
     */
    public static int uploadMaterial(int lecturerId, int courseId, String title,
                                     String description, File sourceFile) throws Exception {
        return uploadMaterial(lecturerId, courseId, title, description, sourceFile, null, null);
    }

    /**
     * Updates metadata (title, description, course) of an existing material.
     * Enforces that destination course is assigned to the lecturer and ownership.
     */
    public static void updateMaterial(int lecturerId, int materialId, int courseId,
                                      String title, String description,
                                      String fromDateStr, String dueDateStr) throws Exception {
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

        // Date Parsing & Validation
        java.sql.Date sqlFromDate = parseSqlDate(fromDateStr);
        java.sql.Date sqlDueDate = parseSqlDate(dueDateStr);

        if (sqlDueDate != null && sqlFromDate == null) {
            sqlFromDate = java.sql.Date.valueOf(java.time.LocalDate.now());
        }

        if (sqlFromDate != null && sqlDueDate != null && sqlDueDate.before(sqlFromDate)) {
            throw new IllegalArgumentException("Due Date (" + sqlDueDate + ") cannot be earlier than From Date (" + sqlFromDate + ").");
        }

        String sql = "UPDATE course_materials " +
                     "SET course_id = ?, material_title = ?, material_description = ?, from_date = ?, due_date = ? " +
                     "WHERE material_id = ? AND lecturer_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database 'tecfams_db'.");
            }
            ensureDatabaseSchema(conn);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, courseId);
                pstmt.setString(2, cleanTitle);
                pstmt.setString(3, description != null ? description.trim() : null);
                if (sqlFromDate != null) {
                    pstmt.setDate(4, sqlFromDate);
                } else {
                    pstmt.setNull(4, java.sql.Types.DATE);
                }
                if (sqlDueDate != null) {
                    pstmt.setDate(5, sqlDueDate);
                } else {
                    pstmt.setNull(5, java.sql.Types.DATE);
                }
                pstmt.setInt(6, materialId);
                pstmt.setInt(7, lecturerId);

                int rows = pstmt.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("Update failed. You may not be authorized to modify this material.");
                }
            }
        }
    }

    /**
     * Backwards compatible updateMaterial overload.
     */
    public static void updateMaterial(int lecturerId, int materialId, int courseId,
                                      String title, String description) throws Exception {
        updateMaterial(lecturerId, materialId, courseId, title, description, null, null);
    }

    /**
     * Deletes a course material from the MySQL database.
     * Enforces lecturer ownership check.
     */
    public static void deleteMaterial(int lecturerId, int materialId) throws Exception {
        if (lecturerId <= 0 || materialId <= 0) {
            throw new IllegalArgumentException("Invalid lecturer ID or material ID.");
        }

        String deleteSql = "DELETE FROM course_materials WHERE material_id = ? AND lecturer_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database 'tecfams_db'.");
            }
            ensureDatabaseSchema(conn);
            try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                pstmt.setInt(1, materialId);
                pstmt.setInt(2, lecturerId);
                int deleted = pstmt.executeUpdate();
                if (deleted == 0) {
                    throw new IllegalAccessException("Material not found or you are not authorized to delete it.");
                }
            }
        }
    }

    // =========================================================================
    // OPEN & DOWNLOAD OPERATIONS (Loaded from Database BLOB)
    // =========================================================================

    /**
     * Opens the stored document by material ID directly from MySQL database BLOB.
     * Extracts bytes to a temporary runtime file and triggers system default viewer via Desktop.open().
     */
    public static void openMaterial(int materialId) throws Exception {
        if (materialId <= 0) {
            throw new IllegalArgumentException("Invalid material ID: " + materialId);
        }

        String sql = "SELECT file_name, file_data FROM course_materials WHERE material_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database 'tecfams_db'.");
            }
            ensureDatabaseSchema(conn);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, materialId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String fileName = rs.getString("file_name");
                        if (fileName == null || fileName.trim().isEmpty()) {
                            fileName = "material.pdf";
                        }

                        InputStream is = rs.getBinaryStream("file_data");
                        if (is == null) {
                            throw new FileNotFoundException("No file binary data found in the database for material ID " + materialId);
                        }

                        // Create safe temp file to open with default system application
                        String ext = getFileExtension(fileName);
                        String prefix = getBaseName(fileName).replaceAll("[^a-zA-Z0-9_-]", "_");
                        if (prefix.length() < 3) prefix = "tecfams_doc";
                        File tempFile = File.createTempFile(prefix + "_", "." + (ext.isEmpty() ? "dat" : ext));
                        tempFile.deleteOnExit();

                        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                            byte[] buffer = new byte[8192];
                            int read;
                            while ((read = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, read);
                            }
                        }

                        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                            throw new UnsupportedOperationException("System desktop integration is not supported on this platform.");
                        }

                        Desktop.getDesktop().open(tempFile);
                    } else {
                        throw new FileNotFoundException("Material ID " + materialId + " not found in database.");
                    }
                }
            }
        }
    }

    /**
     * Legacy openFile overload using material file path / lookup.
     */
    public static void openFile(String fileNameOrPath) throws Exception {
        if (fileNameOrPath == null || fileNameOrPath.trim().isEmpty()) {
            throw new FileNotFoundException("File name is empty.");
        }

        String sql = "SELECT material_id FROM course_materials WHERE file_name = ? OR file_path = ? ORDER BY material_id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, new File(fileNameOrPath).getName());
                    pstmt.setString(2, fileNameOrPath);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            openMaterial(rs.getInt("material_id"));
                            return;
                        }
                    }
                }
            }
        }
        throw new FileNotFoundException("Material '" + fileNameOrPath + "' not found in database.");
    }

    /**
     * Downloads/saves a copy of the stored file from the database to a user-specified destination.
     */
    public static void downloadMaterial(int materialId, File destination) throws Exception {
        if (materialId <= 0) {
            throw new IllegalArgumentException("Invalid material ID.");
        }
        if (destination == null) {
            throw new IllegalArgumentException("Destination file cannot be null.");
        }

        String sql = "SELECT file_data FROM course_materials WHERE material_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database 'tecfams_db'.");
            }
            ensureDatabaseSchema(conn);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, materialId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        InputStream is = rs.getBinaryStream("file_data");
                        if (is == null) {
                            throw new FileNotFoundException("No binary file data found in database for material ID " + materialId);
                        }

                        File parent = destination.getParentFile();
                        if (parent != null && !parent.exists()) {
                            parent.mkdirs();
                        }

                        try (FileOutputStream fos = new FileOutputStream(destination)) {
                            byte[] buffer = new byte[8192];
                            int read;
                            while ((read = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, read);
                            }
                        }
                    } else {
                        throw new FileNotFoundException("Material ID " + materialId + " not found in database.");
                    }
                }
            }
        }
    }

    /**
     * Legacy downloadFile overload using file path.
     */
    public static void downloadFile(String fileNameOrPath, File destination) throws Exception {
        if (fileNameOrPath == null || fileNameOrPath.trim().isEmpty()) {
            throw new FileNotFoundException("File name is empty.");
        }

        String sql = "SELECT material_id FROM course_materials WHERE file_name = ? OR file_path = ? ORDER BY material_id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, new File(fileNameOrPath).getName());
                    pstmt.setString(2, fileNameOrPath);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            downloadMaterial(rs.getInt("material_id"), destination);
                            return;
                        }
                    }
                }
            }
        }
        throw new FileNotFoundException("Material '" + fileNameOrPath + "' not found in database.");
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

    /**
     * Parses a user-entered date string in formats like YYYY-MM-DD, YYYY/MM/DD, YYYY/M/D.
     * Returns null if input is empty or null.
     * Throws IllegalArgumentException on invalid format or date values.
     */
    public static java.sql.Date parseSqlDate(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String clean = text.trim().replace('/', '-').replace('.', '-');
        String[] parts = clean.split("-");
        if (parts.length == 3) {
            try {
                int y = Integer.parseInt(parts[0].trim());
                int m = Integer.parseInt(parts[1].trim());
                int d = Integer.parseInt(parts[2].trim());
                java.time.LocalDate ld = java.time.LocalDate.of(y, m, d);
                return java.sql.Date.valueOf(ld);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid date: '" + text.trim() + "'. Please enter date in YYYY-MM-DD or YYYY/MM/DD format (e.g. 2026-08-01).");
            }
        }
        throw new IllegalArgumentException("Invalid date format: '" + text.trim() + "'. Please enter date in YYYY-MM-DD or YYYY/MM/DD format (e.g. 2026-08-01).");
    }
}
