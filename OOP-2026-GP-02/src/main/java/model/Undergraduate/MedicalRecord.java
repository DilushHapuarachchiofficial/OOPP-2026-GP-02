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
}
