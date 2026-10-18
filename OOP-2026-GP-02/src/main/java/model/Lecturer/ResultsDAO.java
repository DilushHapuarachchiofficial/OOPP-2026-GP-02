package main.java.model.Lecturer;

import main.java.dao.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ResultsDAO.java
 * TecFAMS – Faculty of Technology Academic Management System
 * Faculty of Technology, University of Ruhuna
 *
 * Dedicated Data Access Object for final course results and publication management.
 * Retrieves calculated student scores, CA eligibility, UGC letter grades, and grade points
 * directly from the MySQL view `view_course_final_results`.
 *
 * Workflow Rules:
 * - Read-only retrieval of calculated results from the database view.
 * - Publication status management ('Draft' -> 'Reviewed' -> 'Published').
 * - Restricts publication and status updates to assigned lecturers only.
 */
public class ResultsDAO {

    /**
     * Data transfer object representing a student's final result record from the SQL view.
     */
    public static class CourseFinalResultItem {
        private final int studentId;
        private final String studentRegNo;
        private final String fullName;
        private final int courseId;
        private final String courseCode;
        private final String courseName;
        private final int academicYear;
        private final int semester;
        private final String registrationStatus;
        private final Double caPercentage;
        private final String caEligibility;
        private final Double endExamPercentage;
        private final Double weightedCaScore;
        private final Double weightedFinalScore;
        private final Double finalCalculatedMarks;
        private final String letterGrade;
        private final Double gradePoint;
        private final String resultStatus;
        private final String publicationStatus;

        public CourseFinalResultItem(int studentId, String studentRegNo, String fullName,
                                     int courseId, String courseCode, String courseName,
                                     int academicYear, int semester, String registrationStatus,
                                     Double caPercentage, String caEligibility, Double endExamPercentage,
                                     Double weightedCaScore, Double weightedFinalScore,
                                     Double finalCalculatedMarks, String letterGrade,
                                     Double gradePoint, String resultStatus, String publicationStatus) {
            this.studentId = studentId;
            this.studentRegNo = studentRegNo != null ? studentRegNo : "";
            this.fullName = fullName != null ? fullName : "";
            this.courseId = courseId;
            this.courseCode = courseCode != null ? courseCode : "";
            this.courseName = courseName != null ? courseName : "";
            this.academicYear = academicYear;
            this.semester = semester;
            this.registrationStatus = registrationStatus != null ? registrationStatus : "Registered";
            this.caPercentage = caPercentage;
            this.caEligibility = caEligibility != null ? caEligibility : "PENDING";
            this.endExamPercentage = endExamPercentage;
            this.weightedCaScore = weightedCaScore;
            this.weightedFinalScore = weightedFinalScore;
            this.finalCalculatedMarks = finalCalculatedMarks;
            this.letterGrade = letterGrade != null ? letterGrade : "I";
            this.gradePoint = gradePoint != null ? gradePoint : 0.00;
            this.resultStatus = resultStatus != null ? resultStatus : "Incomplete";
            this.publicationStatus = publicationStatus != null ? publicationStatus : "Draft";
        }

        public int getStudentId() { return studentId; }
        public String getStudentRegNo() { return studentRegNo; }
        public String getFullName() { return fullName; }
        public int getCourseId() { return courseId; }
        public String getCourseCode() { return courseCode; }
        public String getCourseName() { return courseName; }
        public int getAcademicYear() { return academicYear; }
        public int getSemester() { return semester; }
        public String getRegistrationStatus() { return registrationStatus; }
        public Double getCaPercentage() { return caPercentage; }
        public String getCaEligibility() { return caEligibility; }
        public Double getEndExamPercentage() { return endExamPercentage; }
        public Double getWeightedCaScore() { return weightedCaScore; }
        public Double getWeightedFinalScore() { return weightedFinalScore; }
        public Double getFinalCalculatedMarks() { return finalCalculatedMarks; }
        public String getLetterGrade() { return letterGrade; }
        public Double getGradePoint() { return gradePoint; }
        public String getResultStatus() { return resultStatus; }
        public String getPublicationStatus() { return publicationStatus; }

        public boolean isCaEligible() { return "ELIGIBLE".equalsIgnoreCase(caEligibility); }
        public boolean isPassed() { return "Pass".equalsIgnoreCase(resultStatus); }
        public boolean isIncomplete() { return "Incomplete".equalsIgnoreCase(resultStatus) || "I".equalsIgnoreCase(letterGrade); }
    }

    /**
     * Summary statistics for the final results dashboard cards.
     */
    public static class ResultsSummary {
        private final int totalRegistered;
        private final int marksCompleted;
        private final int marksPending;
        private final int caEligibleCount;
        private final int caIneligibleCount;
        private final int resultsCalculated;
        private final String publicationStatus;

        public ResultsSummary(int totalRegistered, int marksCompleted, int marksPending,
                              int caEligibleCount, int caIneligibleCount, int resultsCalculated,
                              String publicationStatus) {
            this.totalRegistered = totalRegistered;
            this.marksCompleted = marksCompleted;
            this.marksPending = marksPending;
            this.caEligibleCount = caEligibleCount;
            this.caIneligibleCount = caIneligibleCount;
            this.resultsCalculated = resultsCalculated;
            this.publicationStatus = publicationStatus != null ? publicationStatus : "Draft";
        }

        public int getTotalRegistered() { return totalRegistered; }
        public int getMarksCompleted() { return marksCompleted; }
        public int getMarksPending() { return marksPending; }
        public int getCaEligibleCount() { return caEligibleCount; }
        public int getCaIneligibleCount() { return caIneligibleCount; }
        public int getResultsCalculated() { return resultsCalculated; }
        public String getPublicationStatus() { return publicationStatus; }
    }

    /**
     * Retrieves all calculated final results for a course from the MySQL view `view_course_final_results`.
     */
    public static List<CourseFinalResultItem> getCourseFinalResults(int courseId) throws SQLException {
        List<CourseFinalResultItem> list = new ArrayList<>();

        String sql = "SELECT student_id, student_reg_no, full_name, course_id, course_code, course_name, " +
                     "       academic_year, semester, registration_status, ca_percentage, ca_eligibility, " +
                     "       end_exam_percentage, weighted_ca_score, weighted_final_score, " +
                     "       final_calculated_marks, letter_grade, grade_point, result_status, publication_status " +
                     "FROM view_course_final_results " +
                     "WHERE course_id = ? " +
                     "ORDER BY student_reg_no ASC";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database.");
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, courseId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Double caPct = rs.getObject("ca_percentage") != null ? rs.getDouble("ca_percentage") : null;
                        Double endExamPct = rs.getObject("end_exam_percentage") != null ? rs.getDouble("end_exam_percentage") : null;
                        Double weightedCa = rs.getObject("weighted_ca_score") != null ? rs.getDouble("weighted_ca_score") : null;
                        Double weightedFinal = rs.getObject("weighted_final_score") != null ? rs.getDouble("weighted_final_score") : null;
                        Double finalMark = rs.getObject("final_calculated_marks") != null ? rs.getDouble("final_calculated_marks") : null;
                        Double gradePt = rs.getObject("grade_point") != null ? rs.getDouble("grade_point") : null;

                        list.add(new CourseFinalResultItem(
                                rs.getInt("student_id"),
                                rs.getString("student_reg_no"),
                                rs.getString("full_name"),
                                rs.getInt("course_id"),
                                rs.getString("course_code"),
                                rs.getString("course_name"),
                                rs.getInt("academic_year"),
                                rs.getInt("semester"),
                                rs.getString("registration_status"),
                                caPct,
                                rs.getString("ca_eligibility"),
                                endExamPct,
                                weightedCa,
                                weightedFinal,
                                finalMark,
                                rs.getString("letter_grade"),
                                gradePt,
                                rs.getString("result_status"),
                                rs.getString("publication_status")
                        ));
                    }
                }
            }
        }
        return list;
    }

    /**
     * Calculates the summary counts for the course results summary cards.
     */
    public static ResultsSummary getResultsSummary(int courseId) throws SQLException {
        List<CourseFinalResultItem> results = getCourseFinalResults(courseId);

        int total = results.size();
        int completed = 0;
        int pending = 0;
        int caEligible = 0;
        int caIneligible = 0;
        int calculated = 0;
        String pubStatus = "Draft";

        for (CourseFinalResultItem item : results) {
            pubStatus = item.getPublicationStatus();
            if (item.isIncomplete()) {
                pending++;
            } else {
                completed++;
                calculated++;
            }

            if ("ELIGIBLE".equalsIgnoreCase(item.getCaEligibility())) {
                caEligible++;
            } else if ("NOT_ELIGIBLE".equalsIgnoreCase(item.getCaEligibility())) {
                caIneligible++;
            }
        }

        return new ResultsSummary(total, completed, pending, caEligible, caIneligible, calculated, pubStatus);
    }

    /**
     * Updates the course results publication status ('Draft', 'Reviewed', 'Published').
     * Enforces security validation that the authenticated lecturer is assigned to this course.
     */
    public static boolean updateCoursePublicationStatus(int courseId, String status, int lecturerId) throws SQLException {
        if (!AssessmentDAO.isLecturerAssignedToCourse(lecturerId, courseId)) {
            throw new SecurityException("Security Violation: You are not authorized to update result publication status for this course.");
        }

        String sqlStatus = "INSERT INTO course_result_status (course_id, status, published_by, published_at) " +
                           "VALUES (?, ?, ?, NOW()) " +
                           "ON DUPLICATE KEY UPDATE " +
                           "  status = VALUES(status), " +
                           "  published_by = VALUES(published_by), " +
                           "  published_at = NOW()";

        String sqlComp = "UPDATE evaluation_components SET status = ? WHERE course_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to connect to MySQL database.");
            }
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sqlStatus);
                 PreparedStatement pstmtComp = conn.prepareStatement(sqlComp)) {
                
                pstmt.setInt(1, courseId);
                pstmt.setString(2, status);
                pstmt.setInt(3, lecturerId);
                int updated = pstmt.executeUpdate();

                // Sync component status: "Published" -> "Published", otherwise "Open"
                String compStatus = "Published".equalsIgnoreCase(status) ? "Published" : "Open";
                pstmtComp.setString(1, compStatus);
                pstmtComp.setInt(2, courseId);
                pstmtComp.executeUpdate();

                conn.commit();
                return updated > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Gets the current publication status for a course.
     */
    public static String getCoursePublicationStatus(int courseId) throws SQLException {
        String sql = "SELECT status FROM course_result_status WHERE course_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return "Draft";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, courseId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("status");
                    }
                }
            }
        }
        return "Draft";
    }
}
