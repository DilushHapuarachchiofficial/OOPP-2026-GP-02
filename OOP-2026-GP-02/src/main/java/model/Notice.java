package main.java.model;

import java.time.LocalDate;

/**
 * Model class representing a Faculty Notice / Circular in TecFAMS.
 * Notices are managed by Administration with view access for Technical Officers and Students.
 */
public class Notice {
    private String noticeId;
    private String title;
    private String content;
    private LocalDate publishedDate;
    private String publishedBy; // e.g. "Dean's Office" or "Administration"
    private String targetAudience; // e.g. "All", "Students", "Staff"

    public Notice() {
        this.publishedDate = LocalDate.now();
        this.publishedBy = "Administration";
    }

    public Notice(String noticeId, String title, String content,
                  LocalDate publishedDate, String publishedBy, String targetAudience) {
        this.noticeId = noticeId;
        this.title = title;
        this.content = content;
        this.publishedDate = publishedDate != null ? publishedDate : LocalDate.now();
        this.publishedBy = publishedBy;
        this.targetAudience = targetAudience;
    }

    // Getters and Setters
    public String getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(String noticeId) {
        this.noticeId = noticeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDate getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getPublishedBy() {
        return publishedBy;
    }

    public void setPublishedBy(String publishedBy) {
        this.publishedBy = publishedBy;
    }

    public String getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(String targetAudience) {
        this.targetAudience = targetAudience;
    }

    @Override
    public String toString() {
        return "[" + publishedDate + "] " + title + " (By: " + publishedBy + ")";
    }
}
