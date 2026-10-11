package main.java.model.Lecturer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * LecturerDashboardModel.java
 * TecFAMS – Faculty of Technology Academic Management System
 *
 * Data model representing the state of the Lecturer Dashboard.
 * Serves as the clean data contract between the UI and the database DAO.
 */
public class LecturerDashboardModel {

    public enum State {
        LOADING,
        SUCCESS,
        ERROR,
        UNAUTHENTICATED
    }

    private State state = State.LOADING;
    private String errorMessage = null;

    private int lecturerId = -1;
    private String username;
    private String lecturerName;
    private String designation;
    private String departmentName;
    private String email;

    private Integer assignedCoursesCount = null;
    private Integer uploadedMaterialsCount = null;
    private final List<NoticeItem> notices = new ArrayList<>();

    public LecturerDashboardModel() {
        this.state = State.LOADING;
        this.lecturerName = "—";
        this.departmentName = "—";
        this.designation = "";
    }

    public LecturerDashboardModel(String lecturerName, String departmentName,
                                  Integer assignedCoursesCount, Integer uploadedMaterialsCount) {
        this.state = State.SUCCESS;
        this.lecturerName = (lecturerName != null && !lecturerName.trim().isEmpty()) ? lecturerName : "—";
        this.departmentName = (departmentName != null && !departmentName.trim().isEmpty()) ? departmentName : "—";
        this.assignedCoursesCount = assignedCoursesCount;
        this.uploadedMaterialsCount = uploadedMaterialsCount;
    }

    // State & Error Management
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    // Lecturer Profile
    public int getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(int lecturerId) {
        this.lecturerId = lecturerId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLecturerName() {
        return lecturerName;
    }

    public void setLecturerName(String lecturerName) {
        this.lecturerName = (lecturerName != null && !lecturerName.trim().isEmpty()) ? lecturerName : "—";
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = (designation != null) ? designation.trim() : "";
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = (departmentName != null && !departmentName.trim().isEmpty()) ? departmentName : "—";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns a formatted department + designation badge text.
     */
    public String getDepartmentBadgeText() {
        if (designation != null && !designation.isEmpty() && departmentName != null && !departmentName.equals("—")) {
            return departmentName + "  •  " + designation;
        } else if (departmentName != null && !departmentName.equals("—")) {
            return departmentName;
        } else if (designation != null && !designation.isEmpty()) {
            return designation;
        }
        return "Faculty of Technology";
    }

    // Counts
    public Integer getAssignedCoursesCount() {
        return assignedCoursesCount;
    }

    public void setAssignedCoursesCount(Integer assignedCoursesCount) {
        this.assignedCoursesCount = assignedCoursesCount;
    }

    public Integer getUploadedMaterialsCount() {
        return uploadedMaterialsCount;
    }

    public void setUploadedMaterialsCount(Integer uploadedMaterialsCount) {
        this.uploadedMaterialsCount = uploadedMaterialsCount;
    }

    public String getCoursesCountDisplay() {
        return (assignedCoursesCount == null || assignedCoursesCount < 0) ? "—" : String.valueOf(assignedCoursesCount);
    }

    public String getMaterialsCountDisplay() {
        return (uploadedMaterialsCount == null || uploadedMaterialsCount < 0) ? "—" : String.valueOf(uploadedMaterialsCount);
    }

    // Notices
    public List<NoticeItem> getNotices() {
        return Collections.unmodifiableList(new ArrayList<>(notices));
    }

    public void setNotices(List<NoticeItem> newNotices) {
        this.notices.clear();
        if (newNotices != null) {
            this.notices.addAll(newNotices);
        }
    }

    public void addNotice(NoticeItem item) {
        if (item != null) {
            this.notices.add(item);
        }
    }

    public boolean hasNotices() {
        return !notices.isEmpty();
    }

    /**
     * Represents a single notice record according to tecfams_db schema and class diagram.
     */
    public static class NoticeItem {
        private final int noticeId;
        private final String title;
        private final String description;
        private final String publishedDate;
        private final String targetAudience;

        public NoticeItem(int noticeId, String title, String description, String publishedDate, String targetAudience) {
            this.noticeId = noticeId;
            this.title = title;
            this.description = description;
            this.publishedDate = publishedDate;
            this.targetAudience = targetAudience;
        }

        public int getNoticeId() { return noticeId; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getPublishedDate() { return publishedDate; }
        public String getTargetAudience() { return targetAudience; }
    }
}
