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
    private String targetAudience;
    private String publisherName;

    public Notice() {
    }

    public Notice(int noticeId, String title, String description, String publishedDate, String expiryDate) {
        this(noticeId, title, description, publishedDate, expiryDate, "Students", "Faculty Administration");
    }

    public Notice(int noticeId, String title, String description, String publishedDate, String expiryDate,
                  String targetAudience, String publisherName) {
        this.noticeId = noticeId;
        this.title = title;
        this.description = description;
        this.publishedDate = publishedDate;
        this.expiryDate = expiryDate;
        this.targetAudience = (targetAudience != null) ? targetAudience : "Students";
        this.publisherName = (publisherName != null) ? publisherName : "Faculty Administration";
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

    public String getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(String targetAudience) {
        this.targetAudience = targetAudience;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }
}
