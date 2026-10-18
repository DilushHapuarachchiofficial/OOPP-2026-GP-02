package main.java.model.Lecturer;

import main.java.dao.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class AssessmentDAO {

    /**
     * Data transfer object representing an evaluation component / assessment.
     */
    public static class AssessmentComponent {
        private int componentId;
        private int courseId;
        private String componentName;
        private String componentType; // "CA" or "Final"
        private String assessmentType; // "Quiz", "Assignment", "Mini-Project", "Midterm", "End Exam", "Other"
        private double maximumMarks;
        private double weightPercentage;
        private String assessmentDate; // yyyy-MM-dd or null
        private String status; // "Draft", "Open", "Closed", "Published"
        private int enteredMarksCount; // Count of students with entered marks

        public AssessmentComponent() {
            this.maximumMarks = 100.00;
            this.componentType = "CA";
            this.assessmentType = "Quiz";
            this.status = "Open";
        }

        public AssessmentComponent(int componentId, int courseId, String componentName, String componentType,
                                   String assessmentType, double maximumMarks, double weightPercentage,
                                   String assessmentDate, String status, int enteredMarksCount) {
            this.componentId = componentId;
            this.courseId = courseId;
            this.componentName = componentName != null ? componentName.trim() : "";
            this.componentType = componentType != null ? componentType.trim() : "CA";
            this.assessmentType = assessmentType != null ? assessmentType.trim() : "General";
            this.maximumMarks = maximumMarks > 0 ? maximumMarks : 100.00;
            this.weightPercentage = weightPercentage;
            this.assessmentDate = assessmentDate;
            this.status = status != null ? status.trim() : "Open";
            this.enteredMarksCount = enteredMarksCount;
        }

        public int getComponentId() { return componentId; }
        public void setComponentId(int componentId) { this.componentId = componentId; }

        public int getCourseId() { return courseId; }
        public void setCourseId(int courseId) { this.courseId = courseId; }

        public String getComponentName() { return componentName; }
        public void setComponentName(String componentName) { this.componentName = componentName; }

        public String getComponentType() { return componentType; }
        public void setComponentType(String componentType) { this.componentType = componentType; }

        public String getAssessmentType() { return assessmentType; }
        public void setAssessmentType(String assessmentType) { this.assessmentType = assessmentType; }

        public double getMaximumMarks() { return maximumMarks; }
        public void setMaximumMarks(double maximumMarks) { this.maximumMarks = maximumMarks; }

        public double getWeightPercentage() { return weightPercentage; }
        public void setWeightPercentage(double weightPercentage) { this.weightPercentage = weightPercentage; }

        public String getAssessmentDate() { return assessmentDate; }
        public void setAssessmentDate(String assessmentDate) { this.assessmentDate = assessmentDate; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public int getEnteredMarksCount() { return enteredMarksCount; }
        public void setEnteredMarksCount(int enteredMarksCount) { this.enteredMarksCount = enteredMarksCount; }

        public boolean isCA() { return "CA".equalsIgnoreCase(componentType); }
        public boolean isFinal() { return "Final".equalsIgnoreCase(componentType); }
        public boolean isPublished() { return "Published".equalsIgnoreCase(status); }

        @Override
        public String toString() {
            return componentName + " (" + componentType + " - " + weightPercentage + "%)";
        }
    }

    /**
     * Encapsulates course assessment weights validation summary.
     */
    public static class WeightValidationReport {
        private final double totalCaWeight;
        private final double totalFinalWeight;
        private final double totalWeight;
        private final int caComponentsCount;
        private final int finalComponentsCount;
        private final boolean valid;
        private final String message;

        public WeightValidationReport(double totalCaWeight, double totalFinalWeight,
                                      int caComponentsCount, int finalComponentsCount) {
            this.totalCaWeight = MarksCalculationService.round2(totalCaWeight);
            this.totalFinalWeight = MarksCalculationService.round2(totalFinalWeight);
            this.totalWeight = MarksCalculationService.round2(totalCaWeight + totalFinalWeight);
            this.caComponentsCount = caComponentsCount;
            this.finalComponentsCount = finalComponentsCount;

            if (totalWeight == 100.00 && caComponentsCount > 0) {
                this.valid = true;
                this.message = String.format("Approved Scheme Complete: CA %.1f%% + Final %.1f%% = 100.0%%",
                        this.totalCaWeight, this.totalFinalWeight);
            } else if (totalWeight < 100.00) {
                this.valid = false;
                this.message = String.format("Incomplete Scheme: Total weight is %.1f%% (Remaining: %.1f%%)",
                        this.totalWeight, 100.00 - this.totalWeight);
            } else {
                this.valid = false;
                this.message = String.format("Excessive Weight: Total weight is %.1f%% (Exceeds by: %.1f%%)",
                        this.totalWeight, this.totalWeight - 100.00);
            }
        }

        public double getTotalCaWeight() { return totalCaWeight; }
        public double getTotalFinalWeight() { return totalFinalWeight; }
        public double getTotalWeight() { return totalWeight; }
        public int getCaComponentsCount() { return caComponentsCount; }
        public int getFinalComponentsCount() { return finalComponentsCount; }
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }

    /**
     * Retrieves all configured assessments for a specific course, ordered by CA first, then Final.
     */
    public static List<AssessmentComponent> getAssessmentsForCourse(int courseId) throws SQLException {
        List<AssessmentComponent> list = new ArrayList<>();
        String sql = "SELECT ec.component_id, ec.course_id, ec.component_name, ec.component_type, " +
                     "       COALESCE(ec.assessment_type, 'General') AS assessment_type, " +
                     "       ec.maximum_marks, ec.weight_percentage, ec.assessment_date, " +
                     "       COALESCE(ec.status, 'Open') AS status, " +
                     "       COUNT(m.mark_id) AS entered_count " +
                     "FROM evaluation_components ec " +
                     "LEFT JOIN marks m ON ec.component_id = m.component_id " +
                     "WHERE ec.course_id = ? " +
                     "GROUP BY ec.component_id, ec.course_id, ec.component_name, ec.component_type, " +
                     "         ec.assessment_type, ec.maximum_marks, ec.weight_percentage, ec.assessment_date, ec.status " +
                     "ORDER BY CASE WHEN ec.component_type = 'CA' THEN 1 ELSE 2 END, ec.component_id ASC";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database.");
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, courseId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(new AssessmentComponent(
                                rs.getInt("component_id"),
                                rs.getInt("course_id"),
                                rs.getString("component_name"),
                                rs.getString("component_type"),
                                rs.getString("assessment_type"),
                                rs.getDouble("maximum_marks"),
                                rs.getDouble("weight_percentage"),
                                rs.getString("assessment_date"),
                                rs.getString("status"),
                                rs.getInt("entered_count")
                        ));
                    }
                }
            }
        }
        return list;
    }

    /**
     * Retrieves only open or active examinations/assessments for a specific course.
     */
    public static List<AssessmentComponent> getOpenAssessmentsForCourse(int courseId) throws SQLException {
        List<AssessmentComponent> all = getAssessmentsForCourse(courseId);
        List<AssessmentComponent> openList = new ArrayList<>();
        for (AssessmentComponent ac : all) {
            if ("Open".equalsIgnoreCase(ac.getStatus()) || "Published".equalsIgnoreCase(ac.getStatus())) {
                openList.add(ac);
            }
        }
        return openList;
    }

    /**
     * Helper factory to build a default Quiz component.
     */
    public static AssessmentComponent buildDefaultQuiz(int courseId, String name, double weight) {
        AssessmentComponent c = new AssessmentComponent();
        c.setCourseId(courseId);
        c.setComponentName(name != null && !name.trim().isEmpty() ? name.trim() : "Quiz");
        c.setComponentType("CA");
        c.setAssessmentType("Quiz");
        c.setMaximumMarks(100.0);
        c.setWeightPercentage(weight > 0 ? weight : 10.0);
        c.setStatus("Open");
        return c;
    }

    /**
     * Helper factory to build a default Assignment component.
     */
    public static AssessmentComponent buildDefaultAssignment(int courseId, String name, double weight) {
        AssessmentComponent c = new AssessmentComponent();
        c.setCourseId(courseId);
        c.setComponentName(name != null && !name.trim().isEmpty() ? name.trim() : "Assignment");
        c.setComponentType("CA");
        c.setAssessmentType("Assignment");
        c.setMaximumMarks(100.0);
        c.setWeightPercentage(weight > 0 ? weight : 10.0);
        c.setStatus("Open");
        return c;
    }

    /**
     * Helper factory to build a default Midterm Exam component.
     */
    public static AssessmentComponent buildDefaultMidExam(int courseId, String name, double weight) {
        AssessmentComponent c = new AssessmentComponent();
        c.setCourseId(courseId);
        c.setComponentName(name != null && !name.trim().isEmpty() ? name.trim() : "Midterm Examination");
        c.setComponentType("CA");
        c.setAssessmentType("Midterm Exam");
        c.setMaximumMarks(100.0);
        c.setWeightPercentage(weight > 0 ? weight : 20.0);
        c.setStatus("Open");
        return c;
    }

    /**
     * Helper factory to build a default End Exam (ESA) component.
     */
    public static AssessmentComponent buildDefaultEndExam(int courseId, String name, double weight) {
        AssessmentComponent c = new AssessmentComponent();
        c.setCourseId(courseId);
        c.setComponentName(name != null && !name.trim().isEmpty() ? name.trim() : "End Semester Examination");
        c.setComponentType("Final");
        c.setAssessmentType("End Exam");
        c.setMaximumMarks(100.0);
        c.setWeightPercentage(weight > 0 ? weight : 60.0);
        c.setStatus("Open");
        return c;
    }

    /**
     * Validates whether a lecturer is officially assigned to this course.
     */
    public static boolean isLecturerAssignedToCourse(int lecturerId, int courseId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM course_lecturers WHERE lecturer_id = ? AND course_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, lecturerId);
                pstmt.setInt(2, courseId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Calculates the weight validation report for a course.
     */
    public static WeightValidationReport getCourseWeightReport(int courseId) throws SQLException {
        String sql = "SELECT component_type, SUM(weight_percentage) AS sum_weight, COUNT(component_id) AS comp_count " +
                     "FROM evaluation_components " +
                     "WHERE course_id = ? " +
                     "GROUP BY component_type";

        double caWeight = 0.0;
        double finalWeight = 0.0;
        int caCount = 0;
        int finalCount = 0;

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database.");
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, courseId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String type = rs.getString("component_type");
                        double w = rs.getDouble("sum_weight");
                        int count = rs.getInt("comp_count");
                        if ("CA".equalsIgnoreCase(type)) {
                            caWeight = w;
                            caCount = count;
                        } else if ("Final".equalsIgnoreCase(type)) {
                            finalWeight = w;
                            finalCount = count;
                        }
                    }
                }
            }
        }
        return new WeightValidationReport(caWeight, finalWeight, caCount, finalCount);
    }

    /**
     * Adds a new assessment component for a course.
     * Enforces security validation that the authenticated lecturer is assigned to this course.
     */
    public static boolean addAssessment(AssessmentComponent comp, int lecturerId) throws SQLException {
        if (!isLecturerAssignedToCourse(lecturerId, comp.getCourseId())) {
            throw new SecurityException("Security Violation: You are not authorized to configure assessments for this course.");
        }
        if (comp.getComponentName() == null || comp.getComponentName().trim().isEmpty()) {
            throw new IllegalArgumentException("Assessment name cannot be empty.");
        }
        if (comp.getMaximumMarks() <= 0.0) {
            throw new IllegalArgumentException("Maximum marks must be greater than zero.");
        }
        if (comp.getWeightPercentage() <= 0.0 || comp.getWeightPercentage() > 100.0) {
            throw new IllegalArgumentException("Weight percentage must be between 0.1% and 100%.");
        }

        String sql = "INSERT INTO evaluation_components (course_id, component_name, component_type, " +
                     "       assessment_type, maximum_marks, weight_percentage, assessment_date, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database.");
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, comp.getCourseId());
                pstmt.setString(2, comp.getComponentName().trim());
                pstmt.setString(3, comp.getComponentType());
                pstmt.setString(4, comp.getAssessmentType());
                pstmt.setDouble(5, comp.getMaximumMarks());
                pstmt.setDouble(6, comp.getWeightPercentage());
                if (comp.getAssessmentDate() != null && !comp.getAssessmentDate().trim().isEmpty()) {
                    pstmt.setDate(7, java.sql.Date.valueOf(comp.getAssessmentDate().trim()));
                } else {
                    pstmt.setNull(7, Types.DATE);
                }
                pstmt.setString(8, comp.getStatus());

                int affected = pstmt.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            comp.setComponentId(rs.getInt(1));
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Updates an existing assessment component.
     */
    public static boolean updateAssessment(AssessmentComponent comp, int lecturerId) throws SQLException {
        if (!isLecturerAssignedToCourse(lecturerId, comp.getCourseId())) {
            throw new SecurityException("Security Violation: You are not authorized to configure assessments for this course.");
        }
        if (comp.getComponentName() == null || comp.getComponentName().trim().isEmpty()) {
            throw new IllegalArgumentException("Assessment name cannot be empty.");
        }
        if (comp.getMaximumMarks() <= 0.0) {
            throw new IllegalArgumentException("Maximum marks must be greater than zero.");
        }
        if (comp.getWeightPercentage() <= 0.0 || comp.getWeightPercentage() > 100.0) {
            throw new IllegalArgumentException("Weight percentage must be between 0.1% and 100%.");
        }

        String sql = "UPDATE evaluation_components SET " +
                     "  component_name = ?, component_type = ?, assessment_type = ?, " +
                     "  maximum_marks = ?, weight_percentage = ?, assessment_date = ?, status = ? " +
                     "WHERE component_id = ? AND course_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database.");
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, comp.getComponentName().trim());
                pstmt.setString(2, comp.getComponentType());
                pstmt.setString(3, comp.getAssessmentType());
                pstmt.setDouble(4, comp.getMaximumMarks());
                pstmt.setDouble(5, comp.getWeightPercentage());
                if (comp.getAssessmentDate() != null && !comp.getAssessmentDate().trim().isEmpty()) {
                    pstmt.setDate(6, java.sql.Date.valueOf(comp.getAssessmentDate().trim()));
                } else {
                    pstmt.setNull(6, Types.DATE);
                }
                pstmt.setString(7, comp.getStatus());
                pstmt.setInt(8, comp.getComponentId());
                pstmt.setInt(9, comp.getCourseId());

                return pstmt.executeUpdate() > 0;
            }
        }
    }

    /**
     * Deletes an assessment component with safety check: if marks exist, throws informative exception.
     */
    public static boolean deleteAssessment(int componentId, int courseId, int lecturerId, boolean forceDeleteMarks) throws SQLException {
        if (!isLecturerAssignedToCourse(lecturerId, courseId)) {
            throw new SecurityException("Security Violation: You are not authorized to delete assessments for this course.");
        }

        // Check for existing student marks
        String countSql = "SELECT COUNT(*) FROM marks WHERE component_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database.");
            }
            try (PreparedStatement cStmt = conn.prepareStatement(countSql)) {
                cStmt.setInt(1, componentId);
                try (ResultSet rs = cStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        int count = rs.getInt(1);
                        if (!forceDeleteMarks) {
                            throw new IllegalStateException("Cannot delete assessment: " + count + " student mark record(s) exist. " +
                                    "Please confirm deletion to proceed.");
                        }
                    }
                }
            }

            String deleteSql = "DELETE FROM evaluation_components WHERE component_id = ? AND course_id = ?";
            try (PreparedStatement delStmt = conn.prepareStatement(deleteSql)) {
                delStmt.setInt(1, componentId);
                delStmt.setInt(2, courseId);
                return delStmt.executeUpdate() > 0;
            }
        }
    }
}
