package main.java.model.Lecturer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data model representing the state of the Lecturer Dashboard.
 * Serves as the clean data contract between the UI (Part 1) and
 * the database integration / DAO queries (Part 2).
 *
 * Defaults to neutral placeholder values to prevent fabricated business data.
 */
public class LecturerDashboardModel {

    private String lecturerName;
    private String departmentName;
    private Integer assignedCoursesCount;
    private Integer uploadedMaterialsCount;
    private final List<NoticeItem> notices = new ArrayList<>();

    public LecturerDashboardModel() {
        this("[Lecturer Name]", "[Department]", null, null);
    }

    public LecturerDashboardModel(String lecturerName, String departmentName,
                                  Integer assignedCoursesCount, Integer uploadedMaterialsCount) {
        this.lecturerName = (lecturerName != null && !lecturerName.trim().isEmpty()) ? lecturerName : "[Lecturer Name]";
        this.departmentName = (departmentName != null && !departmentName.trim().isEmpty()) ? departmentName : "[Department]";
        this.assignedCoursesCount = assignedCoursesCount;
        this.uploadedMaterialsCount = uploadedMaterialsCount;
    }

    public String getLecturerName() {
        return lecturerName;
    }

    public void setLecturerName(String lecturerName) {
        this.lecturerName = (lecturerName != null && !lecturerName.trim().isEmpty()) ? lecturerName : "[Lecturer Name]";
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = (departmentName != null && !departmentName.trim().isEmpty()) ? departmentName : "[Department]";
    }

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

    /**
     * Returns count as string, or "—" if not yet loaded from database.
     */
    public String getCoursesCountDisplay() {
        return (assignedCoursesCount == null || assignedCoursesCount < 0) ? "—" : String.valueOf(assignedCoursesCount);
    }

    /**
     * Returns count as string, or "—" if not yet loaded from database.
     */
    public String getMaterialsCountDisplay() {
        return (uploadedMaterialsCount == null || uploadedMaterialsCount < 0) ? "—" : String.valueOf(uploadedMaterialsCount);
    }

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
