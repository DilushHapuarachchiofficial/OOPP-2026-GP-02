package main.java.model.admin;

import java.util.Date;

public class Notice {
    private int noticeId;
    private String title;
    private String description;
    private Date publishedDate;
    private String targetAudience;
    private int publishedBy;
    private String attachmentPath;

    public Notice(int noticeId, String title, String description, Date publishedDate, String targetAudience, int publishedBy, String attachmentPath) {
        this.noticeId = noticeId;
        this.title = title;
        this.description = description;
        this.publishedDate = publishedDate;
        this.targetAudience = targetAudience;
        this.publishedBy = publishedBy;
        this.attachmentPath = attachmentPath;
    }

    public int getNoticeId() { return noticeId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Date getPublishedDate() { return publishedDate; }
    public String getTargetAudience() { return targetAudience; }
    public int getPublishedBy() { return publishedBy; }
    public String getAttachmentPath() { return attachmentPath; }
}
