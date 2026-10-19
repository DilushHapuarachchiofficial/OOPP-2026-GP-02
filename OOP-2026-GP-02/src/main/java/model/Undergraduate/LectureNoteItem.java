package main.java.model.Undergraduate;

/**
 * Model class representing an undergraduate lecture note / course material.
 * Contains course and lecturer metadata for clean display in the Lecture Notes UI.
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
    private String filePath;
    private String uploadedDate;

    // Default constructor
    public LectureNoteItem() {
    }

    // Parameterized constructor
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
        this.uploadedDate = uploadedDate;
    }

    /**
     * Determines a human-readable file type from the file extension.
     */
    public String getFileType() {
        if (filePath == null || filePath.trim().isEmpty()) {
            return "Unknown";
        }
        String lowerPath = filePath.toLowerCase();
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
        return "File";
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUploadedDate() {
        return uploadedDate;
    }

    public void setUploadedDate(String uploadedDate) {
        this.uploadedDate = uploadedDate;
    }
}
