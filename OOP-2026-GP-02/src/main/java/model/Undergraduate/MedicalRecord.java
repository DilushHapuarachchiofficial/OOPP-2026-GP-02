package main.java.model.Undergraduate;

/**
 * Represents a medical record or certificate submitted by an undergraduate.
 * Complies with the TecFAMS UML class diagram.
 */
public class MedicalRecord {
    private int medicalId;
    private String studentId;
    private String date;
    private String reason;
    private String documentPath;
    private MedicalStatus status;

    // Additional display and submission details
    private String referenceNo;
    private int sessionId;
    private String startDate;
    private String endDate;
    private String submittedDate;
    private String courseCode;
    private String courseName;
    private int sessionNumber;
    private String sessionType;

    public MedicalRecord() {
        this.status = MedicalStatus.PENDING;
    }

    public MedicalRecord(int medicalId, String studentId, String date, String reason,
                         String documentPath, MedicalStatus status) {
        this.medicalId = medicalId;
        this.studentId = studentId;
        this.date = date;
        this.reason = reason;
        this.documentPath = documentPath;
        this.status = (status != null) ? status : MedicalStatus.PENDING;
        this.referenceNo = "MED-REF-" + String.format("%04d", medicalId);
    }

    // UML Methods
    public void submit() {
        this.status = MedicalStatus.PENDING;
    }

    public void approve() {
        this.status = MedicalStatus.APPROVED;
    }

    public void reject() {
        this.status = MedicalStatus.REJECTED;
    }

    // Getters and Setters
    public int getMedicalId() {
        return medicalId;
    }

    public void setMedicalId(int medicalId) {
        this.medicalId = medicalId;
        if (this.referenceNo == null || this.referenceNo.isEmpty()) {
            this.referenceNo = "MED-REF-" + String.format("%04d", medicalId);
        }
    }

    public String getReferenceNo() {
        if (referenceNo != null && !referenceNo.isEmpty()) {
            return referenceNo;
        }
        return "MED-REF-" + String.format("%04d", medicalId);
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    public MedicalStatus getStatus() {
        return status;
    }

    public void setStatus(MedicalStatus status) {
        this.status = status;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(String submittedDate) {
        this.submittedDate = submittedDate;
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

    public int getSessionNumber() {
        return sessionNumber;
    }

    public void setSessionNumber(int sessionNumber) {
        this.sessionNumber = sessionNumber;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }
}
