package main.java.model.Undergraduate;

/**
 * Represents course materials uploaded by lecturers for undergraduate students.
 * Complies with the TecFAMS UML class diagram.
 */
public class CourseMaterial {
    private int materialId;
    private String courseCode;
    private String title;
    private String description;
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
