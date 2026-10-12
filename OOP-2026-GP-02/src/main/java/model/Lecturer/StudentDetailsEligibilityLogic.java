package main.java.model.Lecturer;

import main.java.dao.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StudentDetailsEligibilityLogic.java
 * TecFAMS - Faculty of Technology Academic Management System
 *
 * Dedicated business logic and database access layer for Student Details and
 * Exam Attendance Eligibility Management.
 *
 * Responsibilities:
 * - Parameterized JDBC queries against MySQL tecfams_db.
 * - Retrieve only courses assigned to the authenticated lecturer.
 * - Calculate Theory-only, Practical-only, and Combined attendance percentages.
 * - Enforce university 80% threshold policy (>= 80.00% is Eligible).
 * - Precision-safe handling of exactly 80.00% without floating point errors.
 * - Categorize all 5 required attendance & medical scenarios.
 * - Associate medical records with students and courses without row duplication.
 */
public class StudentDetailsEligibilityLogic {

    // Threshold constant: 80% attendance required for examination admission
    public static final double ELIGIBILITY_THRESHOLD = 80.00;

    // Decimal formatter for 2 decimal places
    private static final DecimalFormat PCT_FORMAT = new DecimalFormat("0.00");

    // =========================================================================
    // ENUMS FOR FILTERING & VIEWS
    // =========================================================================

    public enum AttendanceView {
        THEORY_ONLY("Theory Only"),
        PRACTICAL_ONLY("Practical Only"),
        COMBINED("Combined Theory + Practical");

        private final String displayName;
        AttendanceView(String name) { this.displayName = name; }
        public String getDisplayName() { return displayName; }
        @Override public String toString() { return displayName; }
    }

    public enum Scenario {
        ALL("All Scenarios"),
        SCENARIO_1_ABOVE_80_NO_MED("Scenario 1: Above 80% (No Medical)"),
        SCENARIO_2_EXACTLY_80("Scenario 2: Exactly 80% Attendance"),
        SCENARIO_3_BELOW_80_NO_MED("Scenario 3: Below 80% (No Medical)"),
        SCENARIO_4_ABOVE_80_WITH_MED("Scenario 4: Above 80% (With Medical)"),
        SCENARIO_5_BELOW_80_WITH_MED("Scenario 5: Below 80% (With Medical)");

        private final String label;
        Scenario(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    public enum EligibilityFilter {
        ALL("All Students"),
        MEETS_REQUIREMENT("Meets Requirement (≥ 80%)"),
        BELOW_REQUIREMENT("Below Requirement (< 80%)");

        private final String label;
        EligibilityFilter(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    public enum MedicalFilter {
        ALL("All Medical Statuses"),
        HAS_MEDICAL("Medical Records Available"),
        NO_MEDICAL("No Medical Records");

        private final String label;
        MedicalFilter(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    public enum ThresholdClassification {
        ABOVE_80("Above 80%"),
        EXACTLY_80("Exactly 80%"),
        BELOW_80("Below 80%"),
        UNAVAILABLE("Data Unavailable");

        private final String label;
        ThresholdClassification(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    public enum EligibilityDecision {
        ELIGIBLE("Eligible"),
        NOT_ELIGIBLE("Not Eligible"),
        UNAVAILABLE("Unavailable");

        private final String label;
        EligibilityDecision(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    // =========================================================================
    // INNER DATA MODELS
    // =========================================================================

    /**
     * Course entity for the course dropdown.
     */
    public static class CourseInfo {
        private final int courseId;
        private final String courseCode;
        private final String courseName;
        private final String courseType; // "Theory_Only", "Practical_Only", "Theory_and_Practical"
        private final int semester;
        private final int academicYear;

        public CourseInfo(int courseId, String courseCode, String courseName, String courseType, int semester, int academicYear) {
            this.courseId = courseId;
            this.courseCode = courseCode != null ? courseCode : "";
            this.courseName = courseName != null ? courseName : "";
            this.courseType = courseType != null ? courseType : "Theory_and_Practical";
            this.semester = semester;
            this.academicYear = academicYear;
        }

        public int getCourseId() { return courseId; }
        public String getCourseCode() { return courseCode; }
        public String getCourseName() { return courseName; }
        public String getCourseType() { return courseType; }
        public int getSemester() { return semester; }
        public int getAcademicYear() { return academicYear; }

        public boolean hasPracticalComponent() {
            return !"Theory_Only".equalsIgnoreCase(courseType);
        }

        public boolean hasTheoryComponent() {
            return !"Practical_Only".equalsIgnoreCase(courseType);
        }

        @Override
        public String toString() {
            return courseCode + " - " + courseName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CourseInfo that = (CourseInfo) o;
            return courseId == that.courseId;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(courseId);
        }
    }

    /**
     * Medical submission record.
     */
    public static class MedicalRecordItem {
        private final int medicalId;
        private final int studentId;
        private final Integer sessionId;
        private final String startDate;
        private final String endDate;
        private final String reason;
        private final String documentPath;
        private final String submittedDate;
        private final String approvalStatus; // "Approved", "Pending", "Rejected"
        private final Integer verifiedBy;

        public MedicalRecordItem(int medicalId, int studentId, Integer sessionId, String startDate, String endDate,
                                 String reason, String documentPath, String submittedDate,
                                 String approvalStatus, Integer verifiedBy) {
            this.medicalId = medicalId;
            this.studentId = studentId;
            this.sessionId = sessionId;
            this.startDate = startDate != null ? startDate : "";
            this.endDate = endDate != null ? endDate : "";
            this.reason = reason != null ? reason : "No reason provided";
            this.documentPath = documentPath != null ? documentPath : "";
            this.submittedDate = submittedDate != null ? submittedDate : "";
            this.approvalStatus = approvalStatus != null ? approvalStatus : "Pending";
            this.verifiedBy = verifiedBy;
        }

        public int getMedicalId() { return medicalId; }
        public int getStudentId() { return studentId; }
        public Integer getSessionId() { return sessionId; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
        public String getReason() { return reason; }
        public String getDocumentPath() { return documentPath; }
        public String getSubmittedDate() { return submittedDate; }
        public String getApprovalStatus() { return approvalStatus; }
        public Integer getVerifiedBy() { return verifiedBy; }

        public boolean isApproved() {
            return "Approved".equalsIgnoreCase(approvalStatus);
        }
    }

    /**
     * Session-by-session attendance history for individual student details modal.
     */
    public static class AttendanceDetailItem {
        private final int sessionId;
        private final int sessionNumber;
        private final String sessionDate;
        private final String sessionType; // "Theory" or "Practical"
        private final double durationHours;
        private final String attendanceStatus; // "Present", "Absent", "Not Recorded"
        private final boolean medicalCovered;

        public AttendanceDetailItem(int sessionId, int sessionNumber, String sessionDate, String sessionType,
                                    double durationHours, String attendanceStatus, boolean medicalCovered) {
            this.sessionId = sessionId;
            this.sessionNumber = sessionNumber;
            this.sessionDate = sessionDate != null ? sessionDate : "";
            this.sessionType = sessionType != null ? sessionType : "Theory";
            this.durationHours = durationHours;
            this.attendanceStatus = attendanceStatus != null ? attendanceStatus : "Not Recorded";
            this.medicalCovered = medicalCovered;
        }

        public int getSessionId() { return sessionId; }
        public int getSessionNumber() { return sessionNumber; }
        public String getSessionDate() { return sessionDate; }
        public String getSessionType() { return sessionType; }
        public double getDurationHours() { return durationHours; }
        public String getAttendanceStatus() { return attendanceStatus; }
        public boolean isMedicalCovered() { return medicalCovered; }
        public boolean isPresent() { return "Present".equalsIgnoreCase(attendanceStatus); }
    }

    /**
     * Main student row item containing undergraduate data, calculated attendance percentages,
     * medical summary, and eligibility decision.
     */
    public static class StudentEligibilityItem {
        private final int studentId;
        private final String studentRegNo;
        private final String fullName;
        private final String gender;
        private final String dob;
        private final String email;
        private final String phone;
        private final String address;
        private final String studentStatus; // "Regular", "Repeat", "Batch_Missed"
        private final int academicYear;

        // Course details
        private final int courseId;
        private final String courseCode;
        private final String courseName;
        private final String courseType;

        // Theory attendance counts
        private final int theoryHeld;
        private final int theoryAttended;
        private final Double theoryPercentage;

        // Practical attendance counts
        private final int practicalHeld;
        private final int practicalAttended;
        private final Double practicalPercentage;

        // Combined attendance counts
        private final int totalHeld;
        private final int totalAttended;
        private final Double combinedPercentage;

        // Associated medical records
        private final List<MedicalRecordItem> medicalRecords;

        public StudentEligibilityItem(int studentId, String studentRegNo, String fullName, String gender,
                                      String dob, String email, String phone, String address,
                                      String studentStatus, int academicYear,
                                      int courseId, String courseCode, String courseName, String courseType,
                                      int theoryHeld, int theoryAttended,
                                      int practicalHeld, int practicalAttended,
                                      List<MedicalRecordItem> medicalRecords) {
            this.studentId = studentId;
            this.studentRegNo = studentRegNo != null ? studentRegNo : "N/A";
            this.fullName = fullName != null ? fullName : "Unknown";
            this.gender = gender != null ? gender : "";
            this.dob = dob != null ? dob : "";
            this.email = email != null ? email : "";
            this.phone = phone != null ? phone : "";
            this.address = address != null ? address : "";
            this.studentStatus = studentStatus != null ? studentStatus : "Regular";
            this.academicYear = academicYear;

            this.courseId = courseId;
            this.courseCode = courseCode != null ? courseCode : "";
            this.courseName = courseName != null ? courseName : "";
            this.courseType = courseType != null ? courseType : "Theory_and_Practical";

            this.theoryHeld = theoryHeld;
            this.theoryAttended = theoryAttended;
            this.theoryPercentage = (theoryHeld > 0) ? ((theoryAttended / (double) theoryHeld) * 100.0) : null;

            this.practicalHeld = practicalHeld;
            this.practicalAttended = practicalAttended;
            this.practicalPercentage = (practicalHeld > 0) ? ((practicalAttended / (double) practicalHeld) * 100.0) : null;

            this.totalHeld = theoryHeld + practicalHeld;
            this.totalAttended = theoryAttended + practicalAttended;
            this.combinedPercentage = (this.totalHeld > 0) ? ((this.totalAttended / (double) this.totalHeld) * 100.0) : null;

            this.medicalRecords = (medicalRecords != null) ? new ArrayList<>(medicalRecords) : new ArrayList<>();
        }

        // Getters
        public int getStudentId() { return studentId; }
        public String getStudentRegNo() { return studentRegNo; }
        public String getFullName() { return fullName; }
        public String getGender() { return gender; }
        public String getDob() { return dob; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getAddress() { return address; }
        public String getStudentStatus() { return studentStatus; }
        public int getAcademicYear() { return academicYear; }

        public int getCourseId() { return courseId; }
        public String getCourseCode() { return courseCode; }
        public String getCourseName() { return courseName; }
        public String getCourseType() { return courseType; }

        public int getTheoryHeld() { return theoryHeld; }
        public int getTheoryAttended() { return theoryAttended; }
        public Double getTheoryPercentage() { return theoryPercentage; }

        public int getPracticalHeld() { return practicalHeld; }
        public int getPracticalAttended() { return practicalAttended; }
        public Double getPracticalPercentage() { return practicalPercentage; }

        public int getTotalHeld() { return totalHeld; }
        public int getTotalAttended() { return totalAttended; }
        public Double getCombinedPercentage() { return combinedPercentage; }

        public List<MedicalRecordItem> getMedicalRecords() { return medicalRecords; }
        public int getMedicalCount() { return medicalRecords.size(); }
        public boolean hasMedicalRecords() { return !medicalRecords.isEmpty(); }

        public int getApprovedMedicalCount() {
            int count = 0;
            for (MedicalRecordItem m : medicalRecords) {
                if (m.isApproved()) count++;
            }
            return count;
        }

        // Applicable percentage and counts based on selected view mode
        public int getApplicableHeld(AttendanceView view) {
            if (view == AttendanceView.THEORY_ONLY) return theoryHeld;
            if (view == AttendanceView.PRACTICAL_ONLY) return practicalHeld;
            return totalHeld;
        }

        public int getApplicableAttended(AttendanceView view) {
            if (view == AttendanceView.THEORY_ONLY) return theoryAttended;
            if (view == AttendanceView.PRACTICAL_ONLY) return practicalAttended;
            return totalAttended;
        }

        public Double getApplicablePercentage(AttendanceView view) {
            if (view == AttendanceView.THEORY_ONLY) return theoryPercentage;
            if (view == AttendanceView.PRACTICAL_ONLY) return practicalPercentage;
            return combinedPercentage;
        }

        public String getApplicablePercentageFormatted(AttendanceView view) {
            Double val = getApplicablePercentage(view);
            if (val == null) {
                if (view == AttendanceView.PRACTICAL_ONLY && "Theory_Only".equalsIgnoreCase(courseType)) {
                    return "N/A (Theory Only Course)";
                }
                return "N/A (0 Sessions)";
            }
            return PCT_FORMAT.format(val) + "%";
        }

        /**
         * Checks if the applicable percentage is exactly 80.00% without floating point errors.
         * For integer counts, 80% means: attended * 5 == held * 4 (e.g. 12/15 -> 60 == 60).
         */
        public boolean isExactly80Percent(AttendanceView view) {
            int held = getApplicableHeld(view);
            int attended = getApplicableAttended(view);
            if (held <= 0) return false;
            return (attended * 5) == (held * 4);
        }

        /**
         * Classifies student against the 80% threshold.
         */
        public ThresholdClassification getThresholdStatus(AttendanceView view) {
            Double pct = getApplicablePercentage(view);
            if (pct == null) {
                return ThresholdClassification.UNAVAILABLE;
            }
            if (isExactly80Percent(view)) {
                return ThresholdClassification.EXACTLY_80;
            }
            if (pct > ELIGIBILITY_THRESHOLD) {
                return ThresholdClassification.ABOVE_80;
            }
            return ThresholdClassification.BELOW_80;
        }

        /**
         * Exam eligibility decision: >= 80.00% is Eligible.
         */
        public EligibilityDecision getEligibilityDecision(AttendanceView view) {
            Double pct = getApplicablePercentage(view);
            if (pct == null) {
                return EligibilityDecision.UNAVAILABLE;
            }
            // >= 80% is eligible
            if (isExactly80Percent(view) || pct >= ELIGIBILITY_THRESHOLD) {
                return EligibilityDecision.ELIGIBLE;
            }
            return EligibilityDecision.NOT_ELIGIBLE;
        }

        /**
         * Concise explanation for the eligibility result.
         */
        public String getEligibilityReason(AttendanceView view) {
            Double pct = getApplicablePercentage(view);
            int held = getApplicableHeld(view);
            int attended = getApplicableAttended(view);

            if (pct == null || held <= 0) {
                return "No " + view.getDisplayName().toLowerCase() + " attendance sessions held yet.";
            }

            String viewName = view.getDisplayName();
            String ratio = attended + "/" + held + " sessions attended";
            String pctStr = PCT_FORMAT.format(pct) + "%";

            if (isExactly80Percent(view)) {
                return viewName + " is exactly 80.00% (" + ratio + "), meets the university 80% attendance requirement.";
            } else if (pct > ELIGIBILITY_THRESHOLD) {
                return viewName + " is " + pctStr + " (" + ratio + "), meets the university 80% requirement.";
            } else {
                return viewName + " is " + pctStr + " (" + ratio + "), below the university 80% requirement.";
            }
        }

        /**
         * Matches against the 5 attendance & medical scenarios.
         */
        public boolean matchesScenario(Scenario scenario, AttendanceView view) {
            if (scenario == Scenario.ALL) return true;

            Double pct = getApplicablePercentage(view);
            if (pct == null) return false; // Incomplete/no sessions cannot match specific percentage scenarios

            boolean hasMed = hasMedicalRecords();
            boolean exact80 = isExactly80Percent(view);

            switch (scenario) {
                case SCENARIO_1_ABOVE_80_NO_MED:
                    return !exact80 && pct > ELIGIBILITY_THRESHOLD && !hasMed;
                case SCENARIO_2_EXACTLY_80:
                    return exact80;
                case SCENARIO_3_BELOW_80_NO_MED:
                    return pct < ELIGIBILITY_THRESHOLD && !hasMed;
                case SCENARIO_4_ABOVE_80_WITH_MED:
                    return !exact80 && pct > ELIGIBILITY_THRESHOLD && hasMed;
                case SCENARIO_5_BELOW_80_WITH_MED:
                    return pct < ELIGIBILITY_THRESHOLD && hasMed;
                default:
                    return true;
            }
        }

        public boolean matchesEligibilityFilter(EligibilityFilter filter, AttendanceView view) {
            if (filter == EligibilityFilter.ALL) return true;
            EligibilityDecision dec = getEligibilityDecision(view);
            if (filter == EligibilityFilter.MEETS_REQUIREMENT) {
                return dec == EligibilityDecision.ELIGIBLE;
            } else if (filter == EligibilityFilter.BELOW_REQUIREMENT) {
                return dec == EligibilityDecision.NOT_ELIGIBLE;
            }
            return true;
        }

        public boolean matchesMedicalFilter(MedicalFilter filter) {
            if (filter == MedicalFilter.ALL) return true;
            if (filter == MedicalFilter.HAS_MEDICAL) {
                return hasMedicalRecords();
            } else if (filter == MedicalFilter.NO_MEDICAL) {
                return !hasMedicalRecords();
            }
            return true;
        }

        public boolean matchesSearch(String query) {
            if (query == null || query.trim().isEmpty()) return true;
            String q = query.trim().toLowerCase();
            return studentRegNo.toLowerCase().contains(q) ||
                   fullName.toLowerCase().contains(q) ||
                   String.valueOf(studentId).equals(q) ||
                   email.toLowerCase().contains(q);
        }
    }

    /**
     * Statistical counts for summary cards.
     */
    public static class SummaryStatistics {
        private final int totalStudents;
        private final int meetsRequirementCount;
        private final int belowRequirementCount;
        private final int medicalCount;
        private final int unavailableCount;

        public SummaryStatistics(int totalStudents, int meetsRequirementCount, int belowRequirementCount, int medicalCount, int unavailableCount) {
            this.totalStudents = totalStudents;
            this.meetsRequirementCount = meetsRequirementCount;
            this.belowRequirementCount = belowRequirementCount;
            this.medicalCount = medicalCount;
            this.unavailableCount = unavailableCount;
        }

        public int getTotalStudents() { return totalStudents; }
        public int getMeetsRequirementCount() { return meetsRequirementCount; }
        public int getBelowRequirementCount() { return belowRequirementCount; }
        public int getMedicalCount() { return medicalCount; }
        public int getUnavailableCount() { return unavailableCount; }
    }

    // =========================================================================
    // DATABASE RETRIEVAL METHODS
    // =========================================================================

    /**
     * Retrieves assigned courses for the authenticated lecturer from the database.
     */
    public static List<CourseInfo> getAssignedCourses(int lecturerId) throws SQLException {
        if (lecturerId <= 0) {
            throw new IllegalArgumentException("Invalid lecturer ID: " + lecturerId);
        }

        List<CourseInfo> list = new ArrayList<>();
        String sql = "SELECT DISTINCT c.course_id, c.course_code, c.course_name, c.course_type, c.semester, c.academic_year " +
                     "FROM courses c " +
                     "JOIN course_lecturers cl ON c.course_id = cl.course_id " +
                     "WHERE cl.lecturer_id = ? " +
                     "ORDER BY c.course_code ASC";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database 'tecfams_db'. Please check that MySQL server is running.");
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, lecturerId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(new CourseInfo(
                                rs.getInt("course_id"),
                                rs.getString("course_code"),
                                rs.getString("course_name"),
                                rs.getString("course_type"),
                                rs.getInt("semester"),
                                rs.getInt("academic_year")
                        ));
                    }
                }
            }

            // Fallback if course_lecturers is empty: check evaluation components marks
            if (list.isEmpty()) {
                String fallbackSql = "SELECT DISTINCT c.course_id, c.course_code, c.course_name, c.course_type, c.semester, c.academic_year " +
                                     "FROM courses c " +
                                     "JOIN evaluation_components ec ON c.course_id = ec.course_id " +
                                     "JOIN marks m ON ec.component_id = m.component_id " +
                                     "WHERE m.uploaded_by = ? " +
                                     "ORDER BY c.course_code ASC";
                try (PreparedStatement fbStmt = conn.prepareStatement(fallbackSql)) {
                    fbStmt.setInt(1, lecturerId);
                    try (ResultSet rs = fbStmt.executeQuery()) {
                        while (rs.next()) {
                            CourseInfo item = new CourseInfo(
                                    rs.getInt("course_id"),
                                    rs.getString("course_code"),
                                    rs.getString("course_name"),
                                    rs.getString("course_type"),
                                    rs.getInt("semester"),
                                    rs.getInt("academic_year")
                            );
                            if (!list.contains(item)) {
                                list.add(item);
                            }
                        }
                    }
                }
            }
        }
        return list;
    }

    /**
     * Retrieves all student eligibility items for a selected assigned course.
     * Extracts students, sessions, attendances, and medical records using parameterized SQL.
     */
    public static List<StudentEligibilityItem> getStudentEligibilityList(int lecturerId, int courseId) throws SQLException {
        if (lecturerId <= 0 || courseId <= 0) {
            return new ArrayList<>();
        }

        List<StudentEligibilityItem> result = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database 'tecfams_db'.");
            }

            // 1. Fetch Course details
            CourseInfo course = null;
            String courseSql = "SELECT course_id, course_code, course_name, course_type, semester, academic_year " +
                               "FROM courses WHERE course_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(courseSql)) {
                pstmt.setInt(1, courseId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        course = new CourseInfo(
                                rs.getInt("course_id"),
                                rs.getString("course_code"),
                                rs.getString("course_name"),
                                rs.getString("course_type"),
                                rs.getInt("semester"),
                                rs.getInt("academic_year")
                        );
                    }
                }
            }
            if (course == null) return result;

            // 2. Count distinct attendance sessions held for this course by type
            int theoryHeld = 0;
            int practicalHeld = 0;
            String sessCountSql = "SELECT session_type, COUNT(session_id) AS session_count " +
                                  "FROM attendance_sessions " +
                                  "WHERE course_id = ? " +
                                  "GROUP BY session_type";
            try (PreparedStatement pstmt = conn.prepareStatement(sessCountSql)) {
                pstmt.setInt(1, courseId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String type = rs.getString("session_type");
                        int count = rs.getInt("session_count");
                        if ("Theory".equalsIgnoreCase(type)) {
                            theoryHeld = count;
                        } else if ("Practical".equalsIgnoreCase(type)) {
                            practicalHeld = count;
                        }
                    }
                }
            }

            // 3. Query all attendance counts per student for this course
            Map<Integer, Integer> theoryAttendedMap = new HashMap<>();
            Map<Integer, Integer> practicalAttendedMap = new HashMap<>();

            String attSql = "SELECT a.student_id, sess.session_type, " +
                            "       SUM(CASE WHEN a.attendance_status = 'Present' THEN 1 ELSE 0 END) AS attended_count " +
                            "FROM attendances a " +
                            "JOIN attendance_sessions sess ON a.session_id = sess.session_id " +
                            "WHERE sess.course_id = ? " +
                            "GROUP BY a.student_id, sess.session_type";
            try (PreparedStatement pstmt = conn.prepareStatement(attSql)) {
                pstmt.setInt(1, courseId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int stuId = rs.getInt("student_id");
                        String type = rs.getString("session_type");
                        int attended = rs.getInt("attended_count");
                        if ("Theory".equalsIgnoreCase(type)) {
                            theoryAttendedMap.put(stuId, attended);
                        } else if ("Practical".equalsIgnoreCase(type)) {
                            practicalAttendedMap.put(stuId, attended);
                        }
                    }
                }
            }

            // 4. Query all medical records relevant to this course
            Map<Integer, List<MedicalRecordItem>> medicalMap = new HashMap<>();
            String medSql = "SELECT m.medical_id, m.student_id, m.session_id, m.start_date, m.end_date, " +
                            "       m.reason, m.document_path, m.submitted_date, m.approval_status, m.verified_by " +
                            "FROM medicals m " +
                            "WHERE m.session_id IN (SELECT session_id FROM attendance_sessions WHERE course_id = ?) " +
                            "   OR EXISTS ( " +
                            "       SELECT 1 FROM attendance_sessions sess " +
                            "       WHERE sess.course_id = ? " +
                            "         AND sess.session_date BETWEEN m.start_date AND m.end_date " +
                            "   ) " +
                            "ORDER BY m.submitted_date DESC";
            try (PreparedStatement pstmt = conn.prepareStatement(medSql)) {
                pstmt.setInt(1, courseId);
                pstmt.setInt(2, courseId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int stuId = rs.getInt("student_id");
                        int medId = rs.getInt("medical_id");
                        Integer sessId = (Integer) rs.getObject("session_id");
                        String sDate = rs.getString("start_date");
                        String eDate = rs.getString("end_date");
                        String reason = rs.getString("reason");
                        String doc = rs.getString("document_path");
                        String subDate = rs.getString("submitted_date");
                        String status = rs.getString("approval_status");
                        Integer verified = (Integer) rs.getObject("verified_by");

                        MedicalRecordItem medItem = new MedicalRecordItem(
                                medId, stuId, sessId, sDate, eDate, reason, doc, subDate, status, verified
                        );

                        medicalMap.computeIfAbsent(stuId, k -> new ArrayList<>()).add(medItem);
                    }
                }
            }

            // 5. Query all students registered for this course
            String studentSql = "SELECT s.student_id, s.student_reg_no, s.full_name, s.gender, s.dob, " +
                                "       s.email, s.phone, s.address, s.student_status, s.academic_year " +
                                "FROM students s " +
                                "JOIN course_registrations cr ON s.student_id = cr.student_id " +
                                "WHERE cr.course_id = ? " +
                                "ORDER BY s.student_reg_no ASC";
            try (PreparedStatement pstmt = conn.prepareStatement(studentSql)) {
                pstmt.setInt(1, courseId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int stuId = rs.getInt("student_id");
                        String regNo = rs.getString("student_reg_no");
                        String name = rs.getString("full_name");
                        String gender = rs.getString("gender");
                        String dob = rs.getString("dob");
                        String email = rs.getString("email");
                        String phone = rs.getString("phone");
                        String address = rs.getString("address");
                        String status = rs.getString("student_status");
                        int yr = rs.getInt("academic_year");

                        int tAtt = theoryAttendedMap.getOrDefault(stuId, 0);
                        int pAtt = practicalAttendedMap.getOrDefault(stuId, 0);
                        List<MedicalRecordItem> medList = medicalMap.getOrDefault(stuId, new ArrayList<>());

                        result.add(new StudentEligibilityItem(
                                stuId, regNo, name, gender, dob, email, phone, address, status, yr,
                                course.getCourseId(), course.getCourseCode(), course.getCourseName(), course.getCourseType(),
                                theoryHeld, tAtt,
                                practicalHeld, pAtt,
                                medList
                        ));
                    }
                }
            }
        }

        return result;
    }

    /**
     * Retrieves session-by-session detailed attendance records for a specific student and course.
     */
    public static List<AttendanceDetailItem> getStudentAttendanceDetails(int studentId, int courseId) throws SQLException {
        List<AttendanceDetailItem> list = new ArrayList<>();

        String sql = "SELECT sess.session_id, sess.session_number, sess.session_date, sess.session_type, sess.duration_hours, " +
                     "       a.attendance_status, " +
                     "       (CASE WHEN a.attendance_status = 'Absent' AND EXISTS ( " +
                     "           SELECT 1 FROM medicals m " +
                     "           WHERE m.student_id = ? AND m.approval_status = 'Approved' " +
                     "             AND (m.session_id = sess.session_id OR sess.session_date BETWEEN m.start_date AND m.end_date) " +
                     "       ) THEN 1 ELSE 0 END) AS is_covered " +
                     "FROM attendance_sessions sess " +
                     "LEFT JOIN attendances a ON a.session_id = sess.session_id AND a.student_id = ? " +
                     "WHERE sess.course_id = ? " +
                     "ORDER BY sess.session_number ASC, sess.session_date ASC";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database.");
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, studentId);
                pstmt.setInt(2, studentId);
                pstmt.setInt(3, courseId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(new AttendanceDetailItem(
                                rs.getInt("session_id"),
                                rs.getInt("session_number"),
                                rs.getString("session_date"),
                                rs.getString("session_type"),
                                rs.getDouble("duration_hours"),
                                rs.getString("attendance_status"),
                                rs.getInt("is_covered") == 1
                        ));
                    }
                }
            }
        }
        return list;
    }

    /**
     * Filters student list in memory for instantaneous UI responsiveness.
     */
    public static List<StudentEligibilityItem> filterStudents(List<StudentEligibilityItem> fullList,
                                                              AttendanceView view,
                                                              String searchQuery,
                                                              Scenario scenario,
                                                              EligibilityFilter eligibilityFilter,
                                                              MedicalFilter medicalFilter) {
        if (fullList == null) return new ArrayList<>();

        List<StudentEligibilityItem> filtered = new ArrayList<>();
        for (StudentEligibilityItem item : fullList) {
            if (!item.matchesSearch(searchQuery)) {
                continue;
            }
            if (!item.matchesScenario(scenario, view)) {
                continue;
            }
            if (!item.matchesEligibilityFilter(eligibilityFilter, view)) {
                continue;
            }
            if (!item.matchesMedicalFilter(medicalFilter)) {
                continue;
            }
            filtered.add(item);
        }
        return filtered;
    }

    /**
     * Calculates real-time statistics from the filtered student list and selected attendance view.
     */
    public static SummaryStatistics calculateSummary(List<StudentEligibilityItem> students, AttendanceView view) {
        if (students == null || students.isEmpty()) {
            return new SummaryStatistics(0, 0, 0, 0, 0);
        }

        int total = students.size();
        int meets = 0;
        int below = 0;
        int withMed = 0;
        int unavail = 0;

        for (StudentEligibilityItem item : students) {
            EligibilityDecision decision = item.getEligibilityDecision(view);
            if (decision == EligibilityDecision.ELIGIBLE) {
                meets++;
            } else if (decision == EligibilityDecision.NOT_ELIGIBLE) {
                below++;
            } else {
                unavail++;
            }

            if (item.hasMedicalRecords()) {
                withMed++;
            }
        }

        return new SummaryStatistics(total, meets, below, withMed, unavail);
    }
}
