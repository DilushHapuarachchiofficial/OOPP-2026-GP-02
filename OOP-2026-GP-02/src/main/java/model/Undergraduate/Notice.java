package main.java.model.Undergraduate;

import java.time.LocalDate;

/**
 * Represents faculty announcements and notices.
 * Complies with the TecFAMS UML class diagram.
 */
public class Notice {
    private int noticeId;
    private String title;
    private String description;
    private String publishedDate;
    private String expiryDate;

    public Notice() {
    }

    public Notice(int noticeId, String title, String description, String publishedDate, String expiryDate) {
        this.noticeId = noticeId;
        this.title = title;
        this.description = description;
        this.publishedDate = publishedDate;
        this.expiryDate = expiryDate;
    }

    // UML Methods
    public void publish() {
        // Handled by Admin / DAO
    }

    public void update() {
        // Handled by Admin / DAO
    }

    public void delete() {
        // Handled by Admin / DAO
    }

    /**
     * Checks if the notice is still active based on today's date and expiryDate.
     */
    public boolean isActive() {
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            return true;
        }
        try {
            LocalDate exp = LocalDate.parse(expiryDate);
            return !LocalDate.now().isAfter(exp);
        } catch (Exception e) {
            return true;
        }
    }

    // Getters and Setters
    public int getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(int noticeId) {
        this.noticeId = noticeId;
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

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
}
