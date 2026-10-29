package main.java.model.Undergraduate;

/**
 * Model class representing an undergraduate lecture note / course material.
 * Contains course and lecturer metadata for clean display in the Lecture Notes UI.
 * Supports file metadata (file_name, file_size) reflecting the course_materials database schema.
 * Designed for beginner-friendly Java OOP understanding.
 */
public class LectureNoteItem {
    private int materialId;
    private int courseId;
    private String courseCode;
    private String courseName;
    private String lecturerName;
    private String title;
    private String description;
    private String fileName;
    private long fileSize;
    private String filePath;
    private String uploadedDate;

    // Default constructor
    public LectureNoteItem() {
    }

    // Parameterized constructor (Legacy / FilePath based)
    public LectureNoteItem(int materialId, int courseId, String courseCode, String courseName,
                           String lecturerName, String title, String description,
                           String filePath, String uploadedDate) {
        this.materialId = materialId;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.lecturerName = lecturerName;
        this.title = title;
        this.description = description;
        this.filePath = filePath;
        this.fileName = filePath;
        this.uploadedDate = uploadedDate;
    }

    // New Parameterized constructor (Database Schema with file_name and file_size)
    public LectureNoteItem(int materialId, int courseId, String courseCode, String courseName,
                           String lecturerName, String title, String description,
                           String fileName, long fileSize, String uploadedDate) {
        this.materialId = materialId;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.lecturerName = lecturerName;
        this.title = title;
        this.description = description;
        this.fileName = fileName;
        this.filePath = fileName;
        this.fileSize = fileSize;
        this.uploadedDate = uploadedDate;
    }

    /**
     * Determines a human-readable file type from the file extension.
     */
    public String getFileType() {
        String name = (fileName != null && !fileName.trim().isEmpty()) ? fileName : filePath;
        if (name == null || name.trim().isEmpty()) {
            return "Document";
        }
        String lowerPath = name.toLowerCase();
        if (lowerPath.endsWith(".pdf")) {
            return "PDF Document";
        } else if (lowerPath.endsWith(".doc") || lowerPath.endsWith(".docx")) {
            return "Word Document";
        } else if (lowerPath.endsWith(".ppt") || lowerPath.endsWith(".pptx")) {
            return "PowerPoint";
        } else if (lowerPath.endsWith(".txt")) {
            return "Text File";
        } else if (lowerPath.endsWith(".zip") || lowerPath.endsWith(".rar")) {
            return "Archive";
        }
        return "Document";
    }

    /**
     * Returns a human-friendly formatted file size (e.g. 100 KB, 1.5 MB).
     */
    public String getFormattedFileSize() {
        if (fileSize <= 0) {
            return "";
        }
        if (fileSize >= 1024 * 1024) {
            return String.format("%.1f MB", (double) fileSize / (1024.0 * 1024.0));
        } else if (fileSize >= 1024) {
            return String.format("%d KB", fileSize / 1024);
        } else {
            return fileSize + " B";
        }
    }

    // Getters and Setters
    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getLecturerName() {
        return lecturerName;
    }

    public void setLecturerName(String lecturerName) {
        this.lecturerName = lecturerName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileName() {
        return (fileName != null && !fileName.trim().isEmpty()) ? fileName : filePath;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        if (this.filePath == null) {
            this.filePath = fileName;
        }
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFilePath() {
        return (filePath != null && !filePath.trim().isEmpty()) ? filePath : fileName;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
        if (this.fileName == null) {
            this.fileName = filePath;
        }
    }

    public String getUploadedDate() {
        return uploadedDate;
    }

    public void setUploadedDate(String uploadedDate) {
        this.uploadedDate = uploadedDate;
    }
}
