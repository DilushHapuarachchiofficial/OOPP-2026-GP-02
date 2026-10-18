package main.java.model;

import java.time.LocalDate;

/**
 * Model class representing an Undergraduate Medical Certificate submission in TecFAMS.
 * Tracks medical absence verification and approval statuses.
 */
public class Medical {
    private String medicalId;
    private String studentId;
    private String courseCode;
    private LocalDate submissionDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String certificatePath;
    private String approvalStatus; // "Pending", "Approved", "Rejected"
    private String reviewedBy;     // Technical Officer or Authority
    private String remarks;

    public Medical() {
        this.submissionDate = LocalDate.now();
        this.approvalStatus = "Pending";
    }

    public Medical(String medicalId, String studentId, String courseCode,
                   LocalDate submissionDate, LocalDate startDate, LocalDate endDate,
                   String reason, String certificatePath, String approvalStatus) {
        this.medicalId = medicalId;
        this.studentId = studentId;
        this.courseCode = courseCode;
        this.submissionDate = submissionDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.certificatePath = certificatePath;
        this.approvalStatus = approvalStatus != null ? approvalStatus : "Pending";
    }

    public boolean isPending() {
        return "Pending".equalsIgnoreCase(approvalStatus);
    }

    public boolean isApproved() {
        return "Approved".equalsIgnoreCase(approvalStatus);
    }

    // Getters and Setters
    public String getMedicalId() {
        return medicalId;
    }

    public void setMedicalId(String medicalId) {
        this.medicalId = medicalId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public LocalDate getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDate submissionDate) {
        this.submissionDate = submissionDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCertificatePath() {
        return certificatePath;
    }

    public void setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return "Medical{" +
                "id='" + medicalId + '\'' +
                ", student='" + studentId + '\'' +
                ", course='" + courseCode + '\'' +
                ", status='" + approvalStatus + '\'' +
                ", submitted=" + submissionDate +
                '}';
    }
}
