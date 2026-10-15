package main.java.model.Lecturer;

import main.java.model.User;
import main.java.model.UserSession;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Lecturer.java
 * TecFAMS – Faculty of Technology Academic Management System
 * University of Ruhuna
 *
 * Domain Entity & Logic Model for Lecturer.
 * Extends {@link User} as specified in the TecFAMS Class Diagram.
 *
 * Implements strict Object-Oriented Programming (OOP) encapsulation:
 * - All attributes are declared private.
 * - Comprehensive getters and setters provided for all fields.
 * - Constructor chaining via super() to inherit User identity and profile attributes.
 * - Fully implements all operations declared in the class diagram:
 *     + updateProfile() : void
 *     + addCourseMaterial(course : Course, material : CourseMaterial) : void
 *     + modifyCourseMaterial(material : CourseMaterial) : void
 *     + uploadMarks(mark : Mark) : void
 *     + viewStudentDetails(student : Undergraduate) : void
 *     + viewEligibility(student : Undergraduate) : Eligibility
 *     + viewMarks(student : Undergraduate) : void
 *     + viewAttendance(student : Undergraduate) : void
 *     + viewMedicalRecords(student : Undergraduate) : void
 *     + viewNotices() : List
 */
public class Lecturer extends User {

    // =========================================================================
    // ENCAPSULATED PRIVATE ATTRIBUTES (Class Diagram Specifications)
    // =========================================================================
    private String lecturerId;
    private String designation;
    private String department;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================

    /**
     * Default no-argument constructor.
     */
    public Lecturer() {
        super();
    }

    /**
     * Constructor for Lecturer-specific fields with String lecturerId.
     *
     * @param lecturerId  Lecturer ID identifier (e.g., "LEC001" or numeric string "2")
     * @param designation Academic designation (e.g., "Senior Lecturer", "Head of Dept")
     * @param department  Academic department name
     */
    public Lecturer(String lecturerId, String designation, String department) {
        super();
        this.lecturerId = lecturerId;
        this.designation = designation;
        this.department = department;
    }

    /**
     * Overloaded constructor for Lecturer-specific fields with integer lecturerId.
     * Maps the integer ID into both User's userId and Lecturer's lecturerId.
     *
     * @param lecturerId  Integer lecturer primary key
     * @param designation Academic designation
     * @param department  Academic department name
     */
    public Lecturer(int lecturerId, String designation, String department) {
        this(String.valueOf(lecturerId), designation, department);
        setUserId(lecturerId);
    }

    /**
     * Full parameterized constructor initializing all inherited User attributes
     * and Lecturer-specific attributes.
     */
    public Lecturer(int userId, String username, String password, String name,
                    String email, String phone, String profilePicture,
                    String lecturerId, String designation, String department) {
        super(userId, username, password, name, email, phone, profilePicture);
        this.lecturerId = lecturerId;
        this.designation = designation;
        this.department = department;
    }

    /**
     * Convenience factory method to construct a Lecturer from the active UserSession.
     *
     * @return Fully populated Lecturer entity or null if unauthenticated.
     */
    public static Lecturer fromCurrentSession() {
        UserSession session = UserSession.getCurrentSession();
        if (session == null) {
            return null;
        }
        Lecturer lec = new Lecturer();
        lec.setUserId(session.getUserId());
        lec.setLecturerId(String.valueOf(session.getUserId()));
        lec.setUsername(session.getUsername());
        lec.setName(session.getFullName());
        lec.setEmail(session.getEmail());
        lec.setDesignation(session.getDesignation());
        lec.setDepartment(session.getDepartmentName());
        return lec;
    }

    // =========================================================================
    // ENCAPSULATION: GETTERS AND SETTERS
    // =========================================================================

    public String getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(String lecturerId) {
        this.lecturerId = lecturerId != null ? lecturerId.trim() : null;
    }

    /**
     * Convenience setter accepting integer ID.
     */
    public void setLecturerId(int lecturerId) {
        this.lecturerId = String.valueOf(lecturerId);
        setUserId(lecturerId);
    }

    /**
     * Helper to safely retrieve lecturerId as an integer.
     */
    public int getLecturerIdAsInt() {
        if (lecturerId != null) {
            try {
                return Integer.parseInt(lecturerId.trim());
            } catch (NumberFormatException ignored) {}
        }
        return getUserId();
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation != null ? designation.trim() : null;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department != null ? department.trim() : null;
    }

    // =========================================================================
    // METHODS SPECIFIED IN CLASS DIAGRAM
    // =========================================================================

    /**
     * + updateProfile() : void
     * Updates lecturer profile details in the database via LecturerProfileLogic.
     */
    @Override
    public void updateProfile() {
        int id = getLecturerIdAsInt();
        if (id > 0) {
            LecturerProfileLogic.updateProfile(id, getName(), getEmail(), getPhone());
        }
    }

    /**
     * + addCourseMaterial(course : Course, material : CourseMaterial) : void
     * Uploads course material for the specified course.
     */
    public void addCourseMaterial(Course course, CourseMaterial material) {
        if (course == null || material == null) return;
        int id = getLecturerIdAsInt();
        if (id <= 0) return;

        File f = material.getFilePath() != null ? new File(material.getFilePath()) : null;
        if (f != null && f.exists()) {
            try {
                CourseMaterialsLogic.uploadMaterial(id, course.getCourseId(), material.getTitle(), material.getDescription(), f);
            } catch (Exception e) {
                System.err.println("Error adding course material: " + e.getMessage());
            }
        }
    }

    /**
     * Overloaded helper: add course material directly with File.
     */
    public int addCourseMaterial(int courseId, String title, String description, File file) throws Exception {
        return CourseMaterialsLogic.uploadMaterial(getLecturerIdAsInt(), courseId, title, description, file);
    }

    /**
     * + modifyCourseMaterial(material : CourseMaterial) : void
     * Modifies the title, description, or course of existing course material.
     */
    public void modifyCourseMaterial(CourseMaterial material) {
        if (material == null) return;
        int id = getLecturerIdAsInt();
        if (id <= 0) return;

        try {
            CourseMaterialsLogic.updateMaterial(id, material.getMaterialId(), material.getCourseId(), material.getTitle(), material.getDescription());
        } catch (Exception e) {
            System.err.println("Error modifying course material: " + e.getMessage());
        }
    }

    /**
     * Overloaded helper: modify course material directly.
     */
    public void modifyCourseMaterial(int materialId, int courseId, String title, String description) throws Exception {
        CourseMaterialsLogic.updateMaterial(getLecturerIdAsInt(), materialId, courseId, title, description);
    }

    /**
     * + uploadMarks(mark : Mark) : void
     * Saves or updates assessment marks in the database.
     */
    public void uploadMarks(Mark mark) {
        if (mark == null) return;
        String sql = "INSERT INTO marks (component_id, student_id, marks_obtained, entered_by, recorded_at) " +
                     "VALUES (?, ?, ?, ?, NOW()) ON DUPLICATE KEY UPDATE marks_obtained = VALUES(marks_obtained)";
        try (Connection conn = main.java.dao.DBConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, mark.getMarkId());
                    pstmt.setInt(2, mark.getStudentId());
                    pstmt.setDouble(3, mark.getMarks());
                    pstmt.setInt(4, getLecturerIdAsInt());
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error uploading marks: " + e.getMessage());
        }
    }

    /**
     * + viewStudentDetails(student : Undergraduate) : void
     * Displays or logs student details.
     */
    public void viewStudentDetails(Undergraduate student) {
        if (student != null) {
            System.out.println("Student Details -> ID: " + student.getStudentId() +
                    ", RegNo: " + student.getRegistrationNo() +
                    ", Batch: " + student.getBatch() +
                    ", Department: " + student.getDepartment());
        }
    }

    /**
     * + viewEligibility(student : Undergraduate) : Eligibility
     * Calculates examination eligibility for the specified undergraduate.
     */
    public Eligibility viewEligibility(Undergraduate student) {
        if (student == null) return new Eligibility();
        String sql = "SELECT attendance_percentage, attendance_eligibility, final_exam_eligibility " +
                     "FROM view_student_eligibility WHERE student_reg_no = ? LIMIT 1";
        try (Connection conn = main.java.dao.DBConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, student.getRegistrationNo());
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            double pct = rs.getDouble("attendance_percentage");
                            String status = rs.getString("final_exam_eligibility");
                            boolean eligible = "ELIGIBLE".equalsIgnoreCase(status);
                            return new Eligibility(student.getRegistrationNo(), "", pct, eligible, status);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching eligibility: " + e.getMessage());
        }
        return new Eligibility(student.getRegistrationNo(), "", 0.0, false, "NOT_ELIGIBLE");
    }

    /**
     * + viewMarks(student : Undergraduate) : void
     * Prints or inspects marks for the undergraduate student.
     */
    public void viewMarks(Undergraduate student) {
        if (student == null) return;
        String sql = "SELECT c.course_code, ec.component_name, m.marks_obtained, ec.maximum_marks " +
                     "FROM marks m " +
                     "JOIN evaluation_components ec ON m.component_id = ec.component_id " +
                     "JOIN courses c ON ec.course_id = c.course_id " +
                     "JOIN students s ON m.student_id = s.student_id " +
                     "WHERE s.student_reg_no = ?";
        try (Connection conn = main.java.dao.DBConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, student.getRegistrationNo());
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            System.out.println("Course: " + rs.getString("course_code") +
                                    ", Component: " + rs.getString("component_name") +
                                    ", Mark: " + rs.getDouble("marks_obtained") +
                                    "/" + rs.getDouble("maximum_marks"));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error viewing marks: " + e.getMessage());
        }
    }

    /**
     * + viewAttendance(student : Undergraduate) : void
     * Prints or inspects attendance summary for the undergraduate student.
     */
    public void viewAttendance(Undergraduate student) {
        if (student == null) return;
        String sql = "SELECT course_code, attendance_percentage, attendance_eligibility " +
                     "FROM view_student_eligibility WHERE student_reg_no = ?";
        try (Connection conn = main.java.dao.DBConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, student.getRegistrationNo());
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            System.out.println("Course: " + rs.getString("course_code") +
                                    ", Attendance: " + rs.getDouble("attendance_percentage") + "%" +
                                    ", Eligibility: " + rs.getString("attendance_eligibility"));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error viewing attendance: " + e.getMessage());
        }
    }

    /**
     * + viewMedicalRecords(student : Undergraduate) : void
     * Prints or inspects medical submissions for the undergraduate student.
     */
    public void viewMedicalRecords(Undergraduate student) {
        if (student == null) return;
        String sql = "SELECT m.medical_id, m.start_date, m.end_date, m.reason, m.approval_status " +
                     "FROM medicals m " +
                     "JOIN students s ON m.student_id = s.student_id " +
                     "WHERE s.student_reg_no = ?";
        try (Connection conn = main.java.dao.DBConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, student.getRegistrationNo());
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            System.out.println("Medical ID: " + rs.getInt("medical_id") +
                                    ", Period: " + rs.getString("start_date") + " to " + rs.getString("end_date") +
                                    ", Reason: " + rs.getString("reason") +
                                    ", Status: " + rs.getString("approval_status"));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error viewing medical records: " + e.getMessage());
        }
    }

    /**
     * + viewNotices() : List
     * Loads the latest academic notices for the lecturer.
     */
    public List<LecturerDashboardModel.NoticeItem> viewNotices() {
        try {
            int id = getLecturerIdAsInt();
            if (id > 0) {
                LecturerDashboardModel model = LecturerDashboardDAO.loadDashboardData(id);
                return model.getNotices();
            }
        } catch (Exception e) {
            System.err.println("Error loading notices: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    // =========================================================================
    // SUPPORTING INNER DOMAIN MODELS (Fully Encapsulated according to Class Diagram)
    // =========================================================================

    /**
     * Course entity class from class diagram.
     */
    public static class Course {
        private int courseId;
        private String courseCode;
        private String courseName;
        private int credits;
        private String department;
        private String description;

        public Course() {}

        public Course(int courseId, String courseCode, String courseName, int credits, String department, String description) {
            this.courseId = courseId;
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.credits = credits;
            this.department = department;
            this.description = description;
        }

        public int getCourseId() { return courseId; }
        public void setCourseId(int courseId) { this.courseId = courseId; }
        public String getCourseCode() { return courseCode; }
        public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        public int getCredits() { return credits; }
        public void setCredits(int credits) { this.credits = credits; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    /**
     * CourseMaterial entity class from class diagram.
     */
    public static class CourseMaterial {
        private int materialId;
        private int courseId;
        private String title;
        private String description;
        private String filePath;
        private Date uploadedDate;

        public CourseMaterial() {}

        public CourseMaterial(int materialId, String title, String description, String filePath, Date uploadedDate) {
            this.materialId = materialId;
            this.courseId = 0;
            this.title = title;
            this.description = description;
            this.filePath = filePath;
            this.uploadedDate = uploadedDate;
        }

        public CourseMaterial(int materialId, int courseId, String title, String description, String filePath, Date uploadedDate) {
            this.materialId = materialId;
            this.courseId = courseId;
            this.title = title;
            this.description = description;
            this.filePath = filePath;
            this.uploadedDate = uploadedDate;
        }

        public int getMaterialId() { return materialId; }
        public void setMaterialId(int materialId) { this.materialId = materialId; }
        public int getCourseId() { return courseId; }
        public void setCourseId(int courseId) { this.courseId = courseId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        public Date getUploadedDate() { return uploadedDate; }
        public void setUploadedDate(Date uploadedDate) { this.uploadedDate = uploadedDate; }
    }

    /**
     * Mark entity class from class diagram.
     */
    public static class Mark {
        private int markId;
        private int studentId;
        private double marks;
        private String assessmentType;
        private String assessmentName;
        private Date date;

        public Mark() {}

        public Mark(int markId, double marks, String assessmentType, String assessmentName, Date date) {
            this.markId = markId;
            this.marks = marks;
            this.assessmentType = assessmentType;
            this.assessmentName = assessmentName;
            this.date = date;
        }

        public int getMarkId() { return markId; }
        public void setMarkId(int markId) { this.markId = markId; }
        public int getStudentId() { return studentId; }
        public void setStudentId(int studentId) { this.studentId = studentId; }
        public double getMarks() { return marks; }
        public void setMarks(double marks) { this.marks = marks; }
        public String getAssessmentType() { return assessmentType; }
        public void setAssessmentType(String assessmentType) { this.assessmentType = assessmentType; }
        public String getAssessmentName() { return assessmentName; }
        public void setAssessmentName(String assessmentName) { this.assessmentName = assessmentName; }
        public Date getDate() { return date; }
        public void setDate(Date date) { this.date = date; }
    }

    /**
     * Undergraduate entity class from class diagram.
     */
    public static class Undergraduate {
        private String studentId;
        private String registrationNo;
        private String batch;
        private String department;
        private int enrollmentYear;

        public Undergraduate() {}

        public Undergraduate(String studentId, String registrationNo, String batch, String department, int enrollmentYear) {
            this.studentId = studentId;
            this.registrationNo = registrationNo;
            this.batch = batch;
            this.department = department;
            this.enrollmentYear = enrollmentYear;
        }

        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        public String getRegistrationNo() { return registrationNo; }
        public void setRegistrationNo(String registrationNo) { this.registrationNo = registrationNo; }
        public String getBatch() { return batch; }
        public void setBatch(String batch) { this.batch = batch; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public int getEnrollmentYear() { return enrollmentYear; }
        public void setEnrollmentYear(int enrollmentYear) { this.enrollmentYear = enrollmentYear; }
    }

    /**
     * Eligibility data representation returned by viewEligibility().
     */
    public static class Eligibility {
        private String studentRegNo;
        private String courseCode;
        private double attendancePercentage;
        private boolean isEligible;
        private String eligibilityStatus;

        public Eligibility() {}

        public Eligibility(String studentRegNo, String courseCode, double attendancePercentage, boolean isEligible, String eligibilityStatus) {
            this.studentRegNo = studentRegNo;
            this.courseCode = courseCode;
            this.attendancePercentage = attendancePercentage;
            this.isEligible = isEligible;
            this.eligibilityStatus = eligibilityStatus;
        }

        public String getStudentRegNo() { return studentRegNo; }
        public void setStudentRegNo(String studentRegNo) { this.studentRegNo = studentRegNo; }
        public String getCourseCode() { return courseCode; }
        public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
        public double getAttendancePercentage() { return attendancePercentage; }
        public void setAttendancePercentage(double attendancePercentage) { this.attendancePercentage = attendancePercentage; }
        public boolean isEligible() { return isEligible; }
        public void setEligible(boolean eligible) { isEligible = eligible; }
        public String getEligibilityStatus() { return eligibilityStatus; }
        public void setEligibilityStatus(String eligibilityStatus) { this.eligibilityStatus = eligibilityStatus; }
    }

    @Override
    public String toString() {
        return "Lecturer{" +
                "lecturerId='" + lecturerId + '\'' +
                ", designation='" + designation + '\'' +
                ", department='" + department + '\'' +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", phone='" + getPhone() + '\'' +
                '}';
    }
}
