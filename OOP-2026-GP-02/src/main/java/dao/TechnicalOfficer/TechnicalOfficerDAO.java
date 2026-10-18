package main.java.dao.TechnicalOfficer;

import main.java.model.TechnicalOfficer.TechnicalOfficer;

/**
 * Data Access Object (DAO) for Technical Officer operations.
 * Manages Technical Officer profile information and dashboard summary metrics.
 */
public class TechnicalOfficerDAO {

    private TechnicalOfficer currentOfficer;

    public TechnicalOfficerDAO() {
        this.currentOfficer = new TechnicalOfficer(
                "TO/2026/001",
                "to_officer",
                "Mr. Nimal Jayawardena",
                "nimal.to@tech.ruh.ac.lk",
                "071-2345678",
                "Information & Communication Technology"
        );
    }

    public TechnicalOfficer getCurrentOfficer() {
        return currentOfficer;
    }

    /**
     * Updates permitted profile information as per SRS FR-04:
     * "The system shall allow technical officers to update their profiles except username and password."
     */
    public boolean updateProfile(String name, String email, String phone) {
        if (currentOfficer == null) return false;
        if (name != null && !name.trim().isEmpty()) {
            currentOfficer.setName(name.trim());
        }
        if (email != null && !email.trim().isEmpty()) {
            currentOfficer.setEmail(email.trim());
        }
        if (phone != null && !phone.trim().isEmpty()) {
            currentOfficer.setPhone(phone.trim());
        }
        return true;
    }

    // Dashboard Statistics
    public int getTotalAttendanceCount() {
        return 124;
    }

    public int getPendingMedicalCount() {
        return 4;
    }

    public int getTodaySessionsCount() {
        return 3;
    }

    public int getActiveNoticesCount() {
        return 6;
    }
}
