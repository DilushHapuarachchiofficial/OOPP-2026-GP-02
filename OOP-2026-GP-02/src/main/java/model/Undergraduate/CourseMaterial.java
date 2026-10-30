package main.java.model.Undergraduate;

/**
 * Represents course materials uploaded by lecturers for undergraduate students.
 * Complies with the TecFAMS UML class diagram and the course_materials database schema.
 */
public class CourseMaterial {
    private int materialId;
    private String courseCode;
    private String title;
    private String description;
    private String fileName;
    private long fileSize;
    private String filePath;
    private String uploadedDate;

    public CourseMaterial() {
    }

    public CourseMaterial(int materialId, String courseCode, String title,
                          String description, String filePath, String uploadedDate) {
        this.materialId = materialId;
        this.courseCode = courseCode;
        this.title = title;
        this.description = description;
        this.filePath = filePath;
        this.fileName = filePath;
        this.uploadedDate = uploadedDate;
    }

    public CourseMaterial(int materialId, String courseCode, String title,
                          String description, String fileName, long fileSize, String uploadedDate) {
        this.materialId = materialId;
        this.courseCode = courseCode;
        this.title = title;
        this.description = description;
        this.fileName = fileName;
        this.filePath = fileName;
        this.fileSize = fileSize;
        this.uploadedDate = uploadedDate;
    }

    // UML Methods
    public void upload() {
        // Lecturer action - handled in database/DAO
    }

    public void update() {
        // Lecturer action
    }

    public void delete() {
        // Lecturer action
    }

    // Getters and Setters
    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
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
