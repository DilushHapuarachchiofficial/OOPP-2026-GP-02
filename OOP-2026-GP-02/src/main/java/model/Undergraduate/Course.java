package main.java.model.Undergraduate;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an academic course/module in the faculty.
 * Complies with the TecFAMS UML class diagram.
 */
public class Course {
    private int courseId;
    private String courseCode;
    private String courseName;
    private int credits;
    private String department;
    private String description;
    private String courseType;
    private int semester;
    private int academicYear;
    private String status;
    private List<CourseMaterial> materials;

    public Course() {
        this.materials = new ArrayList<>();
        this.courseType = "Theory_and_Practical";
        this.semester = 1;
        this.academicYear = 2;
        this.status = "Registered";
    }

    public Course(int courseId, String courseCode, String courseName, int credits, String department, String description) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.department = department;
        this.description = description;
        this.courseType = (description != null && !description.isEmpty()) ? description : "Theory_and_Practical";
        this.semester = 1;
        this.academicYear = 2;
        this.status = "Registered";
        this.materials = new ArrayList<>();
    }

    public Course(int courseId, String courseCode, String courseName, int credits, String department,
                  String description, String courseType, int semester, int academicYear, String status) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.department = department;
        this.description = description;
        this.courseType = (courseType != null && !courseType.isEmpty()) ? courseType : "Theory_and_Practical";
        this.semester = semester > 0 ? semester : 1;
        this.academicYear = academicYear > 0 ? academicYear : 2;
        this.status = (status != null && !status.isEmpty()) ? status : "Registered";
        this.materials = new ArrayList<>();
    }

    public void addMaterial(CourseMaterial material) {
        if (material != null) {
            materials.add(material);
        }
    }

    public void removeMaterial(int materialId) {
        materials.removeIf(m -> m.getMaterialId() == materialId);
    }

    public List<CourseMaterial> getMaterials() {
        return materials;
    }

    public void setMaterials(List<CourseMaterial> materials) {
        this.materials = (materials != null) ? materials : new ArrayList<>();
    }

    // Getters and Setters
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

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCourseType() {
        return courseType != null ? courseType : "Theory_and_Practical";
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public int getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public String getStatus() {
        return status != null ? status : "Registered";
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
