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
    private List<CourseMaterial> materials;

    public Course() {
        this.materials = new ArrayList<>();
    }

    public Course(int courseId, String courseCode, String courseName, int credits, String department, String description) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.department = department;
        this.description = description;
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
}
