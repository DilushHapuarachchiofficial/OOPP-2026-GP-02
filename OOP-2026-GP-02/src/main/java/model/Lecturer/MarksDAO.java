package main.java.model.Lecturer;

import main.java.dao.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class MarksDAO {

    /**
     * Data transfer object representing a student's marks row in the UI table,
     * including real-time automatic calculation of the student's overall CA marks and CA eligibility.
     */
    public static class StudentMarkItem {
        private final int studentId;
        private final String studentRegNo;
        private final String studentName;
        private final int componentId;
        private Integer markId;
        private Double rawMark; // Null if unentered, 0.00 if legitimate zero
        private double maximumMarks;
        private double weightPercentage;
        private String componentType; // "CA" or "Final"
        private Double normalizedPercentage;
        private double weightedScore;

        // Overall Continuous Assessment (CA) automated calculation fields
        private double otherCaWeightedScore;
        private double totalCourseCaWeight;
        private double overallCaWeightedScore;
        private Double overallCaPercentage;
        private String overallCaEligibility; // "ELIGIBLE", "NOT_ELIGIBLE", "PENDING"

        private boolean modified; // Track unsaved edits in UI
        private String uploadedDate;
        private String uploadedByName;

        public StudentMarkItem(int studentId, String studentRegNo, String studentName,
                               int componentId, Integer markId, Double rawMark,
                               double maximumMarks, double weightPercentage, String componentType,
                               double otherCaWeightedScore, double totalCourseCaWeight,
                               String uploadedDate, String uploadedByName) {
            this.studentId = studentId;
            this.studentRegNo = studentRegNo != null ? studentRegNo : "";
            this.studentName = studentName != null ? studentName : "";
            this.componentId = componentId;
            this.markId = markId;
            this.rawMark = rawMark;
            this.maximumMarks = maximumMarks > 0 ? maximumMarks : 100.00;
            this.weightPercentage = weightPercentage;
            this.componentType = componentType != null ? componentType : "CA";
            this.otherCaWeightedScore = otherCaWeightedScore;
            this.totalCourseCaWeight = totalCourseCaWeight;
            this.uploadedDate = uploadedDate;
            this.uploadedByName = uploadedByName;
            this.modified = false;
            recalculateMetrics();
        }

        public void setRawMark(Double rawMark) {
            this.rawMark = rawMark;
            this.modified = true;
            recalculateMetrics();
        }

        /**
         * Real-time automated calculation of:
         * 1. Normalized Percentage for current assessment
         * 2. Weighted Score contribution for current assessment
         * 3. Overall CA Weighted Score for the student in this subject
         * 4. Overall CA Percentage out of 100%
         * 5. CA Eligibility determination (Handbook >= 40.00%)
         */
        public void recalculateMetrics() {
            if (this.rawMark != null && this.maximumMarks > 0) {
                this.normalizedPercentage = MarksCalculationService.calculatePercentage(this.rawMark, this.maximumMarks);
                this.weightedScore = MarksCalculationService.calculateWeightedContribution(this.rawMark, this.maximumMarks, this.weightPercentage);
            } else {
                this.normalizedPercentage = null;
                this.weightedScore = 0.00;
            }

            // Overall CA calculation:
            if ("CA".equalsIgnoreCase(this.componentType)) {
                this.overallCaWeightedScore = MarksCalculationService.round2(this.otherCaWeightedScore + this.weightedScore);
            } else {
                // If current component is End Exam, overall CA consists solely of the CA components
                this.overallCaWeightedScore = MarksCalculationService.round2(this.otherCaWeightedScore);
            }

            if (this.totalCourseCaWeight > 0) {
                // If student has entered marks in at least one CA component
                if (this.rawMark != null || this.otherCaWeightedScore > 0) {
                    this.overallCaPercentage = MarksCalculationService.round2((this.overallCaWeightedScore / this.totalCourseCaWeight) * 100.0);
                } else {
                    this.overallCaPercentage = null;
                }
            } else {
                this.overallCaPercentage = null;
            }

            this.overallCaEligibility = MarksCalculationService.determineCaEligibility(this.overallCaPercentage);
        }

        public int getStudentId() { return studentId; }
        public String getStudentRegNo() { return studentRegNo; }
        public String getStudentName() { return studentName; }
        public int getComponentId() { return componentId; }
        public Integer getMarkId() { return markId; }
        public void setMarkId(Integer markId) { this.markId = markId; }
        public Double getRawMark() { return rawMark; }
        public double getMaximumMarks() { return maximumMarks; }
        public void setMaximumMarks(double maximumMarks) {
            this.maximumMarks = maximumMarks;
            recalculateMetrics();
        }
        public double getWeightPercentage() { return weightPercentage; }
        public void setWeightPercentage(double weightPercentage) {
            this.weightPercentage = weightPercentage;
            recalculateMetrics();
        }
        public String getComponentType() { return componentType; }
        public Double getNormalizedPercentage() { return normalizedPercentage; }
        public double getWeightedScore() { return weightedScore; }
        public double getOtherCaWeightedScore() { return otherCaWeightedScore; }
        public double getTotalCourseCaWeight() { return totalCourseCaWeight; }
        public double getOverallCaWeightedScore() { return overallCaWeightedScore; }
        public Double getOverallCaPercentage() { return overallCaPercentage; }
        public String getOverallCaEligibility() { return overallCaEligibility; }
        public boolean isCaEligible() { return "ELIGIBLE".equalsIgnoreCase(overallCaEligibility); }
        public boolean isModified() { return modified; }
        public void setModified(boolean modified) { this.modified = modified; }
        public boolean isEntered() { return rawMark != null; }
        public String getUploadedDate() { return uploadedDate; }
        public String getUploadedByName() { return uploadedByName; }
    }

    /**
     * DTO for displaying complete breakdown of all assessments for a single student.
     */
    public static class StudentAssessmentDetail {
        private final int componentId;
        private final String componentName;
        private final String assessmentType;
        private final String componentType;
        private final double maximumMarks;
        private final double weightPercentage;
        private final Double marksObtained;
        private final Double weightedScore;
        private final String status;
        private final String assessmentDate;

        public StudentAssessmentDetail(int componentId, String componentName, String assessmentType,
                                       String componentType, double maximumMarks, double weightPercentage,
                                       Double marksObtained, Double weightedScore, String status, String assessmentDate) {
            this.componentId = componentId;
            this.componentName = componentName;
            this.assessmentType = assessmentType;
            this.componentType = componentType;
            this.maximumMarks = maximumMarks;
            this.weightPercentage = weightPercentage;
            this.marksObtained = marksObtained;
            this.weightedScore = weightedScore;
            this.status = status;
            this.assessmentDate = assessmentDate;
        }

        public int getComponentId() { return componentId; }
        public String getComponentName() { return componentName; }
        public String getAssessmentType() { return assessmentType; }
        public String getComponentType() { return componentType; }
        public double getMaximumMarks() { return maximumMarks; }
        public double getWeightPercentage() { return weightPercentage; }
        public Double getMarksObtained() { return marksObtained; }
        public Double getWeightedScore() { return weightedScore; }
        public String getStatus() { return status; }
        public String getAssessmentDate() { return assessmentDate; }
    }

    /**
     * Loads officially registered students for a course and their existing marks for a specific assessment,
     * simultaneously calculating other CA weighted scores so the overall CA mark is instantly computed.
     */
    public static List<StudentMarkItem> getStudentMarksForAssessment(int courseId, int componentId,
                                                                    double maximumMarks, double weightPercentage,
                                                                    String componentType) throws SQLException {
        List<StudentMarkItem> list = new ArrayList<>();

        String sql = "SELECT s.student_id, s.student_reg_no, s.full_name, " +
                     "       m.mark_id, m.marks_obtained, m.uploaded_date, " +
                     "       l.lecturer_name AS uploaded_by_name, " +
                     "       COALESCE(( " +
                     "           SELECT SUM((m2.marks_obtained / ec2.maximum_marks) * ec2.weight_percentage) " +
                     "           FROM marks m2 " +
                     "           JOIN evaluation_components ec2 ON m2.component_id = ec2.component_id " +
                     "           WHERE m2.student_id = s.student_id " +
                     "             AND ec2.course_id = ? " +
                     "             AND ec2.component_type = 'CA' " +
                     "             AND ec2.component_id != ? " +
                     "       ), 0.0) AS other_ca_weighted_score, " +
                     "       COALESCE(( " +
                     "           SELECT SUM(ec3.weight_percentage) " +
                     "           FROM evaluation_components ec3 " +
                     "           WHERE ec3.course_id = ? AND ec3.component_type = 'CA' " +
                     "       ), 0.0) AS total_course_ca_weight " +
                     "FROM students s " +
                     "JOIN course_registrations cr ON s.student_id = cr.student_id " +
                     "LEFT JOIN marks m ON m.student_id = s.student_id AND m.component_id = ? " +
                     "LEFT JOIN lecturers l ON m.uploaded_by = l.lecturer_id " +
                     "WHERE cr.course_id = ? AND cr.registration_status IN ('Registered', 'Repeat') " +
                     "ORDER BY s.student_reg_no ASC";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database.");
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, courseId);
                pstmt.setInt(2, componentId);
                pstmt.setInt(3, courseId);
                pstmt.setInt(4, componentId);
                pstmt.setInt(5, courseId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int studentId = rs.getInt("student_id");
                        String regNo = rs.getString("student_reg_no");
                        String name = rs.getString("full_name");
                        int mId = rs.getInt("mark_id");
                        Integer markId = rs.wasNull() ? null : mId;

                        double markVal = rs.getDouble("marks_obtained");
                        Double rawMark = rs.wasNull() ? null : markVal;

                        double otherCa = rs.getDouble("other_ca_weighted_score");
                        double caWeight = rs.getDouble("total_course_ca_weight");

                        String date = rs.getString("uploaded_date");
                        String lecturer = rs.getString("uploaded_by_name");

                        list.add(new StudentMarkItem(studentId, regNo, name, componentId, markId, rawMark,
                                maximumMarks, weightPercentage, componentType, otherCa, caWeight, date, lecturer));
                    }
                }
            }
        }
        return list;
    }

    /**
     * Backward-compatible overload for existing calls.
     */
    public static List<StudentMarkItem> getStudentMarksForAssessment(int courseId, int componentId, double maximumMarks) throws SQLException {
        return getStudentMarksForAssessment(courseId, componentId, maximumMarks, 10.0, "CA");
    }

    /**
     * Retrieves the complete breakdown of all assessments and marks for a specific student in a course.
     */
    public static List<StudentAssessmentDetail> getStudentAssessmentBreakdown(int courseId, int studentId) throws SQLException {
        List<StudentAssessmentDetail> list = new ArrayList<>();
        String sql = "SELECT ec.component_id, ec.component_name, " +
                     "       COALESCE(ec.assessment_type, 'General') AS assessment_type, " +
                     "       ec.component_type, ec.maximum_marks, ec.weight_percentage, " +
                     "       m.marks_obtained, " +
                     "       COALESCE(ec.status, 'Open') AS status, ec.assessment_date " +
                     "FROM evaluation_components ec " +
                     "LEFT JOIN marks m ON ec.component_id = m.component_id AND m.student_id = ? " +
                     "WHERE ec.course_id = ? " +
                     "ORDER BY CASE WHEN ec.component_type = 'CA' THEN 1 ELSE 2 END, ec.component_id ASC";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return list;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, studentId);
                pstmt.setInt(2, courseId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int compId = rs.getInt("component_id");
                        String name = rs.getString("component_name");
                        String aType = rs.getString("assessment_type");
                        String cType = rs.getString("component_type");
                        double maxM = rs.getDouble("maximum_marks");
                        double weight = rs.getDouble("weight_percentage");

                        double markVal = rs.getDouble("marks_obtained");
                        Double raw = rs.wasNull() ? null : markVal;
                        Double weighted = raw != null && maxM > 0 ? MarksCalculationService.round2((raw / maxM) * weight) : null;

                        String st = rs.getString("status");
                        String date = rs.getString("assessment_date");

                        list.add(new StudentAssessmentDetail(compId, name, aType, cType, maxM, weight, raw, weighted, st, date));
                    }
                }
            }
        }
        return list;
    }

    /**
     * Saves or updates a list of student marks in a single atomic database transaction.
     * If any record fails or validation is violated, the transaction is automatically rolled back.
     *
     * @param courseId The course being evaluated.
     * @param componentId The assessment component.
     * @param lecturerId The authenticated lecturer performing the save.
     * @param marksList List of StudentMarkItem records from the UI.
     * @return Number of records successfully inserted/updated.
     * @throws SQLException If database error occurs or transaction fails.
     * @throws SecurityException If authenticated lecturer is not assigned to course.
     */
    public static int saveMarksBatch(int courseId, int componentId, int lecturerId, List<StudentMarkItem> marksList) throws SQLException {
        if (!AssessmentDAO.isLecturerAssignedToCourse(lecturerId, courseId)) {
            throw new SecurityException("Security Violation: You are not authorized to record marks for this course.");
        }

        String upsertSql = "INSERT INTO marks (component_id, student_id, marks_obtained, uploaded_by, uploaded_date) " +
                           "VALUES (?, ?, ?, ?, NOW()) " +
                           "ON DUPLICATE KEY UPDATE " +
                           "    marks_obtained = VALUES(marks_obtained), " +
                           "    uploaded_by = VALUES(uploaded_by), " +
                           "    uploaded_date = NOW()";

        String deleteSql = "DELETE FROM marks WHERE component_id = ? AND student_id = ?";

        Connection conn = null;
        int savedCount = 0;

        try {
            conn = DBConnection.getConnection();
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database.");
            }

            // Begin atomic transaction
            conn.setAutoCommit(false);

            try (PreparedStatement upsertStmt = conn.prepareStatement(upsertSql);
                 PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {

                for (StudentMarkItem item : marksList) {
                    if (item.getRawMark() != null) {
                        // Validate mark bounds
                        if (item.getRawMark() < 0.0 || item.getRawMark() > item.getMaximumMarks()) {
                            throw new IllegalArgumentException(String.format(
                                    "Invalid mark for %s (%s): %.2f exceeds allowed bounds [0.00 - %.2f].",
                                    item.getStudentRegNo(), item.getStudentName(), item.getRawMark(), item.getMaximumMarks()));
                        }

                        upsertStmt.setInt(1, componentId);
                        upsertStmt.setInt(2, item.getStudentId());
                        upsertStmt.setDouble(3, MarksCalculationService.round2(item.getRawMark()));
                        upsertStmt.setInt(4, lecturerId);
                        upsertStmt.addBatch();
                        savedCount++;
                    } else if (item.getMarkId() != null) {
                        // Mark was previously entered, but user cleared it -> delete record to mark as unentered
                        deleteStmt.setInt(1, componentId);
                        deleteStmt.setInt(2, item.getStudentId());
                        deleteStmt.addBatch();
                    }
                }

                upsertStmt.executeBatch();
                deleteStmt.executeBatch();

                // Commit transaction
                conn.commit();

                // Update items state
                for (StudentMarkItem item : marksList) {
                    item.setModified(false);
                }

                return savedCount;

            } catch (Exception e) {
                // Rollback on any failure
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        System.err.println("Transaction rollback failed: " + rollbackEx.getMessage());
                    }
                }
                throw new SQLException("Failed to save marks. All changes rolled back: " + e.getMessage(), e);
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                    } catch (SQLException ignored) {}
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Counts the total number of registered students for a course.
     */
    public static int getRegisteredStudentsCount(int courseId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM course_registrations " +
                     "WHERE course_id = ? AND registration_status IN ('Registered', 'Repeat')";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return 0;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, courseId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }
        return 0;
    }
}
