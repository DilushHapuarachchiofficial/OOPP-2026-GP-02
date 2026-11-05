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
    private Date fromDate;
    private Date toDate;

    public Notice(int noticeId, String title, String description, Date publishedDate, String targetAudience, int publishedBy, String attachmentPath) {
        this(noticeId, title, description, publishedDate, targetAudience, publishedBy, attachmentPath, publishedDate, null);
    }

    public Notice(int noticeId, String title, String description, Date publishedDate, String targetAudience, int publishedBy, String attachmentPath, Date fromDate, Date toDate) {
        this.noticeId = noticeId;
        this.title = title;
        this.description = description;
        this.publishedDate = publishedDate;
        this.targetAudience = targetAudience;
        this.publishedBy = publishedBy;
        this.attachmentPath = attachmentPath;
        this.fromDate = (fromDate != null) ? fromDate : publishedDate;
        this.toDate = toDate;
    }

    public int getNoticeId() { return noticeId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Date getPublishedDate() { return publishedDate; }
    public String getTargetAudience() { return targetAudience; }
    public int getPublishedBy() { return publishedBy; }
    public String getAttachmentPath() { return attachmentPath; }
    public Date getFromDate() { return fromDate; }
    public void setFromDate(Date fromDate) { this.fromDate = fromDate; }
    public Date getToDate() { return toDate; }
    public void setToDate(Date toDate) { this.toDate = toDate; }

    public String getStatus() {
        Date now = new Date();
        if (fromDate != null && now.before(fromDate)) {
            return "Scheduled";
        }
        if (toDate != null && now.after(toDate)) {
            return "Expired";
        }
        return "Active";
    }
}
