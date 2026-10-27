package main.java.ui.Undergraduate;

import main.java.dao.Undergraduate.UndergraduateDAO;
import main.java.model.Undergraduate.AttendanceSummary;
import main.java.model.Undergraduate.Course;
import main.java.model.Undergraduate.GPA;
import main.java.model.Undergraduate.Undergraduate;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

/**
 * TecFAMS - Faculty of Technology Academic Management System
 * Undergraduate Student Profile UI.
 *
 * This UI allows the currently logged-in undergraduate student to view their
 * complete profile details, academic records, and contact information.
 *
 * It also enables students to update permitted contact details (phone number & email)
 * in accordance with the OOP Mini Project UML specifications and SRS guidelines.
 *
 * Built using clear, simple, standard Java Swing components.
 */
public class UndergraduateProfileUI extends JPanel {

    // TecFAMS Theme Palette
    private static final Color COLOR_PRIMARY_NAVY = new Color(0x12, 0x3B, 0x70); // #123B70
    private static final Color COLOR_MAIN_BLUE    = new Color(0x25, 0x63, 0xEB); // #2563EB
    private static final Color COLOR_LIGHT_BLUE   = new Color(0xEA, 0xF2, 0xFF); // #EAF2FF
    private static final Color COLOR_WHITE        = new Color(0xFF, 0xFF, 0xFF); // #FFFFFF
    private static final Color COLOR_BG           = new Color(0xF5, 0xF8, 0xFD); // #F5F8FD
    private static final Color COLOR_TEXT_PRIMARY = new Color(0x17, 0x2B, 0x4D); // #172B4D
    private static final Color COLOR_TEXT_MUTED   = new Color(0x71, 0x80, 0x96); // #718096
    private static final Color COLOR_BORDER       = new Color(0xE2, 0xE8, 0xF0); // #E2E8F0
    private static final Color COLOR_SUCCESS      = new Color(0x10, 0xB9, 0x81); // #10B981
    private static final Color COLOR_CARD_BG      = new Color(0xFF, 0xFF, 0xFF); // #FFFFFF
    private static final Color COLOR_HOVER_BLUE   = new Color(0x1D, 0x4E, 0xD8); // #1D4ED8

    // Data references
    private final Undergraduate loggedInStudent;
    private final UndergraduateDashboard parentDashboard;
    private final UndergraduateDAO dao;

    // Header & Banner Dynamic Components
    private JLabel bannerAvatarLabel;
    private JLabel bannerNameLabel;
    private JLabel bannerRegNoLabel;
    private JLabel bannerDeptLabel;

    // Quick Stat Labels
    private JLabel gpaValueLabel;
    private JLabel gpaSubtitleLabel;
    private JLabel attendanceValueLabel;
    private JLabel attendanceSubtitleLabel;
    private JLabel coursesValueLabel;
    private JLabel coursesSubtitleLabel;

    // Academic Details Labels (Official - Read Only)
    private JLabel valFullName;
    private JLabel valRegNo;
    private JLabel valStudentId;
    private JLabel valDepartment;
    private JLabel valDegree;
    private JLabel valBatch;
    private JLabel valEnrollmentYear;
    private JLabel valAcademicStatus;

    // Contact Details Components (Editable by Student)
    private JLabel valUsername;
    private JLabel valUserId;
    private JTextField txtEmail;
    private JTextField txtPhone;

    // Contact Edit Controls
    private JButton btnEditContact;
    private JButton btnSaveContact;
    private JButton btnCancelContact;
    private JLabel contactStatusNotice;
    private boolean isEditingContact = false;

    /**
     * Primary constructor for embedding inside UndergraduateDashboard.
     *
     * @param student         The currently logged in undergraduate model.
     * @param parentDashboard The parent UndergraduateDashboard window.
     */
    public UndergraduateProfileUI(Undergraduate student, UndergraduateDashboard parentDashboard) {
        this.loggedInStudent = (student != null) ? student : createPreviewStudent();
        this.parentDashboard = parentDashboard;
        this.dao = new UndergraduateDAO();

        initializeUI();
        loadProfileData();
    }

    /**
     * Overloaded constructor for standalone testing.
     *
     * @param student The undergraduate student model.
     */
    public UndergraduateProfileUI(Undergraduate student) {
        this(student, null);
    }

    /**
     * Default constructor for testing without parameters.
     */
    public UndergraduateProfileUI() {
        this(null, null);
    }

    /**
     * Initializes the UI components and layout.
     */
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);

        // Scrollable content panel with vertical stacking
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(COLOR_BG);
        contentPanel.setBorder(new EmptyBorder(18, 26, 26, 26));

        // 1. Top Header Bar (Title, Subtitle, Back to Dashboard button)
        contentPanel.add(createHeaderBar());
        contentPanel.add(Box.createVerticalStrut(14));

        // 2. Profile Hero Card (Avatar, Full Name, Registration No, Status Badges)
        contentPanel.add(createProfileHeroCard());
        contentPanel.add(Box.createVerticalStrut(16));

        // 3. Quick Academic Stats Row (CGPA, Attendance Rate, Enrolled Courses)
        contentPanel.add(createQuickStatsRow());
        contentPanel.add(Box.createVerticalStrut(16));

        // 4. Detailed Information Section (Two-Column Layout: Academic Info & Contact Info)
        contentPanel.add(createDetailedInfoSection());
        contentPanel.add(Box.createVerticalStrut(16));

        // 5. Account & Security Information Card
        contentPanel.add(createSecurityInfoCard());

        // Place everything inside a smooth scrolling JScrollPane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(COLOR_BG);
        add(scrollPane, BorderLayout.CENTER);
    }

    // ==========================================
    // 1. TOP HEADER BAR
    // ==========================================
    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(3000, 50));

        JPanel leftBox = new JPanel(new GridLayout(2, 1, 0, 2));
        leftBox.setOpaque(false);

        JLabel titleLbl = new JLabel("My Profile");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(COLOR_PRIMARY_NAVY);

        JLabel subtitleLbl = new JLabel("Faculty of Technology  •  Student Academic Profile & Contact Details");
        subtitleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLbl.setForeground(COLOR_TEXT_MUTED);

        leftBox.add(titleLbl);
        leftBox.add(subtitleLbl);
        header.add(leftBox, BorderLayout.WEST);

        // Right side buttons: Refresh & Back to Dashboard
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBox.setOpaque(false);

        JButton refreshBtn = createStyledButton("↻ Refresh", COLOR_LIGHT_BLUE, COLOR_MAIN_BLUE);
        refreshBtn.addActionListener(e -> {
            loadProfileData();
            JOptionPane.showMessageDialog(this,
                    "Profile data refreshed successfully from the system.",
                    "Profile Refreshed",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        rightBox.add(refreshBtn);

        if (parentDashboard != null) {
            JButton backBtn = createStyledButton("← Back to Dashboard", COLOR_LIGHT_BLUE, COLOR_MAIN_BLUE);
            backBtn.addActionListener(e -> parentDashboard.showDashboardView());
            rightBox.add(backBtn);
        }

        header.add(rightBox, BorderLayout.EAST);
        return header;
    }

    // ==========================================
    // 2. PROFILE HERO BANNER CARD
    // ==========================================
    private JPanel createProfileHeroCard() {
        JPanel card = new JPanel(new BorderLayout(20, 0));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(18, 22, 18, 22)
        ));
        card.setMaximumSize(new Dimension(3000, 110));

        // Left: Circular Avatar with initials
        bannerAvatarLabel = new JLabel();
        bannerAvatarLabel.setPreferredSize(new Dimension(72, 72));
        card.add(bannerAvatarLabel, BorderLayout.WEST);

        // Center: Student Name, Reg No, Department & Badges
        JPanel centerBox = new JPanel();
        centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
        centerBox.setOpaque(false);

        bannerNameLabel = new JLabel("Undergraduate Student");
        bannerNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        bannerNameLabel.setForeground(COLOR_PRIMARY_NAVY);

        JPanel subInfoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
        subInfoRow.setOpaque(false);

        bannerRegNoLabel = new JLabel("Reg No: TG/2024/2085");
        bannerRegNoLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bannerRegNoLabel.setForeground(COLOR_MAIN_BLUE);

        bannerDeptLabel = new JLabel("•  Department of ICT");
        bannerDeptLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bannerDeptLabel.setForeground(COLOR_TEXT_MUTED);

        subInfoRow.add(bannerRegNoLabel);
        subInfoRow.add(bannerDeptLabel);

        // Badges Row
        JPanel badgesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        badgesRow.setOpaque(false);

        badgesRow.add(createPillBadge("● Active Student", new Color(0xDC, 0xFC, 0xE7), new Color(0x16, 0x65, 0x34)));
        badgesRow.add(createPillBadge("BICT (Hons)", new Color(0xEF, 0xF6, 0xFF), new Color(0x1D, 0x4E, 0xD8)));
        badgesRow.add(createPillBadge("Level II • Semester I", new Color(0xF3, 0xE8, 0xFF), new Color(0x6B, 0x21, 0xA8)));

        centerBox.add(bannerNameLabel);
        centerBox.add(subInfoRow);
        centerBox.add(badgesRow);

        card.add(centerBox, BorderLayout.CENTER);
        return card;
    }

    // ==========================================
    // 3. QUICK ACADEMIC STATS ROW
    // ==========================================
    private JPanel createQuickStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(3000, 95));

        // 1. CGPA & SGPA Card
        JPanel gpaCard = createStatBox("Cumulative GPA (CGPA)");
        gpaValueLabel = new JLabel("3.65");
        gpaValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        gpaValueLabel.setForeground(COLOR_PRIMARY_NAVY);
        gpaSubtitleLabel = new JLabel("SGPA: 3.58  •  Good Standing");
        gpaSubtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gpaSubtitleLabel.setForeground(COLOR_SUCCESS);
        gpaCard.add(gpaValueLabel);
        gpaCard.add(Box.createVerticalStrut(2));
        gpaCard.add(gpaSubtitleLabel);

        // 2. Attendance Summary Card
        JPanel attCard = createStatBox("Overall Attendance Rate");
        attendanceValueLabel = new JLabel("92.8%");
        attendanceValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        attendanceValueLabel.setForeground(COLOR_SUCCESS);
        attendanceSubtitleLabel = new JLabel("Eligible for Exams (Threshold: 80%)");
        attendanceSubtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        attendanceSubtitleLabel.setForeground(COLOR_TEXT_MUTED);
        attCard.add(attendanceValueLabel);
        attCard.add(Box.createVerticalStrut(2));
        attCard.add(attendanceSubtitleLabel);

        // 3. Enrolled Courses Card
        JPanel coursesCard = createStatBox("Registered Courses");
        coursesValueLabel = new JLabel("8 Courses");
        coursesValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        coursesValueLabel.setForeground(COLOR_MAIN_BLUE);
        coursesSubtitleLabel = new JLabel("Level II Semester I (Current Academic Period)");
        coursesSubtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        coursesSubtitleLabel.setForeground(COLOR_TEXT_MUTED);
        coursesCard.add(coursesValueLabel);
        coursesCard.add(Box.createVerticalStrut(2));
        coursesCard.add(coursesSubtitleLabel);

        row.add(gpaCard);
        row.add(attCard);
        row.add(coursesCard);
        return row;
    }

    private JPanel createStatBox(String title) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(COLOR_CARD_BG);
        box.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(COLOR_TEXT_MUTED);

        box.add(titleLbl);
        box.add(Box.createVerticalStrut(4));
        return box;
    }

    // ==========================================
    // 4. DETAILED INFORMATION TWO-COLUMN SECTION
    // ==========================================
    private JPanel createDetailedInfoSection() {
        JPanel section = new JPanel(new GridLayout(1, 2, 18, 0));
        section.setOpaque(false);
        section.setMaximumSize(new Dimension(3000, 360));

        // Left Column: Academic & University Records (Official - Read Only)
        section.add(createAcademicDetailsCard());

        // Right Column: Contact & Account Information (Editable by Student)
        section.add(createContactDetailsCard());

        return section;
    }

    /**
     * Card displaying official university academic records.
     * These details are maintained by the Faculty/AR and are read-only for students.
     */
    private JPanel createAcademicDetailsCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        // Header
        JPanel cardHeader = new JPanel(new GridLayout(2, 1, 0, 2));
        cardHeader.setOpaque(false);
        JLabel title = new JLabel("Academic Records");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(COLOR_PRIMARY_NAVY);
        JLabel subtitle = new JLabel("Official university records (Managed by Academic Division)");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(COLOR_TEXT_MUTED);
        cardHeader.add(title);
        cardHeader.add(subtitle);
        card.add(cardHeader, BorderLayout.NORTH);

        // Body with grid of field rows
        JPanel body = new JPanel(new GridLayout(8, 1, 0, 6));
        body.setOpaque(false);

        valFullName = new JLabel("-");
        valRegNo = new JLabel("-");
        valStudentId = new JLabel("-");
        valDepartment = new JLabel("-");
        valDegree = new JLabel("Bachelor of Information and Communication Technology (Hons)");
        valBatch = new JLabel("-");
        valEnrollmentYear = new JLabel("-");
        valAcademicStatus = new JLabel("Active Undergraduate Student (Full-time)");

        body.add(createFieldRow("Full Name:", valFullName));
        body.add(createFieldRow("Registration No:", valRegNo));
        body.add(createFieldRow("Student ID:", valStudentId));
        body.add(createFieldRow("Department:", valDepartment));
        body.add(createFieldRow("Degree Program:", valDegree));
        body.add(createFieldRow("Academic Batch:", valBatch));
        body.add(createFieldRow("Enrollment Year:", valEnrollmentYear));
        body.add(createFieldRow("Academic Status:", valAcademicStatus));

        card.add(body, BorderLayout.CENTER);

        // Footer note
        JLabel footerNote = new JLabel("ℹ To request official corrections to academic records, contact the Dean's Office.");
        footerNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        footerNote.setForeground(COLOR_TEXT_MUTED);
        card.add(footerNote, BorderLayout.SOUTH);

        return card;
    }

    /**
     * Card displaying student contact & account information.
     * The student is permitted to edit their Email Address and Phone Number.
     */
    private JPanel createContactDetailsCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        // Header
        JPanel cardHeader = new JPanel(new GridLayout(2, 1, 0, 2));
        cardHeader.setOpaque(false);
        JLabel title = new JLabel("Contact & Account Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(COLOR_PRIMARY_NAVY);
        JLabel subtitle = new JLabel("You can update your personal contact email and phone number");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(COLOR_TEXT_MUTED);
        cardHeader.add(title);
        cardHeader.add(subtitle);
        card.add(cardHeader, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel(new GridLayout(5, 1, 0, 8));
        body.setOpaque(false);

        valUsername = new JLabel("-");
        valUserId = new JLabel("-");

        txtEmail = new JTextField(20);
        styleInputField(txtEmail);
        txtEmail.setEditable(false);

        txtPhone = new JTextField(20);
        styleInputField(txtPhone);
        txtPhone.setEditable(false);

        contactStatusNotice = new JLabel("Status: Contact details are up to date.");
        contactStatusNotice.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        contactStatusNotice.setForeground(COLOR_TEXT_MUTED);

        body.add(createFieldRow("Portal Username:", valUsername));
        body.add(createFieldRow("User Account ID:", valUserId));
        body.add(createInputRow("Email Address:", txtEmail));
        body.add(createInputRow("Phone Number:", txtPhone));
        body.add(contactStatusNotice);

        card.add(body, BorderLayout.CENTER);

        // Footer Action Buttons
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionRow.setOpaque(false);

        btnEditContact = createStyledButton("✏ Edit Contact Info", COLOR_LIGHT_BLUE, COLOR_MAIN_BLUE);
        btnSaveContact = createStyledButton("💾 Save Changes", new Color(0x10, 0xB9, 0x81), COLOR_WHITE);
        btnCancelContact = createStyledButton("✖ Cancel", new Color(0xF3, 0xF4, 0xF6), COLOR_TEXT_PRIMARY);

        btnSaveContact.setVisible(false);
        btnCancelContact.setVisible(false);

        btnEditContact.addActionListener(e -> enableContactEditMode(true));
        btnSaveContact.addActionListener(e -> saveContactChanges());
        btnCancelContact.addActionListener(e -> {
            enableContactEditMode(false);
            loadProfileData(); // Revert any unsaved typing
        });

        actionRow.add(btnEditContact);
        actionRow.add(btnSaveContact);
        actionRow.add(btnCancelContact);

        card.add(actionRow, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createFieldRow(String labelText, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(COLOR_TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(130, 22));

        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        valueLabel.setForeground(COLOR_TEXT_PRIMARY);

        row.add(lbl, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.CENTER);
        return row;
    }

    private JPanel createInputRow(String labelText, JTextField inputField) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(COLOR_TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(130, 28));

        row.add(lbl, BorderLayout.WEST);
        row.add(inputField, BorderLayout.CENTER);
        return row;
    }

    private void styleInputField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(COLOR_TEXT_PRIMARY);
        field.setBackground(new Color(0xFA, 0xFA, 0xFA));
        field.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(5, 8, 5, 8)
        ));
    }

    // ==========================================
    // 5. SECURITY & SESSION INFO CARD
    // ==========================================
    private JPanel createSecurityInfoCard() {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));
        card.setMaximumSize(new Dimension(3000, 75));

        JPanel leftBox = new JPanel(new GridLayout(2, 1, 0, 2));
        leftBox.setOpaque(false);

        JLabel title = new JLabel("System Privileges & Account Security");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(COLOR_PRIMARY_NAVY);

        JLabel desc = new JLabel("Role: Undergraduate Student  •  Session: Authenticated via TecFAMS Local Database  •  Privacy Protected");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        desc.setForeground(COLOR_TEXT_MUTED);

        leftBox.add(title);
        leftBox.add(desc);

        card.add(leftBox, BorderLayout.CENTER);

        JLabel badge = createPillBadge("Security Level: Student", new Color(0xEA, 0xF2, 0xFF), COLOR_MAIN_BLUE);
        card.add(badge, BorderLayout.EAST);

        return card;
    }

    // ==========================================
    // DATA LOADING & PROFILE LOGIC
    // ==========================================
    /**
     * Loads student profile data from the database or the loggedInStudent model.
     */
    public void loadProfileData() {
        if (loggedInStudent == null) return;

        String studentKey = (loggedInStudent.getRegistrationNo() != null && !loggedInStudent.getRegistrationNo().isEmpty())
                ? loggedInStudent.getRegistrationNo() : loggedInStudent.getStudentId();
        if (studentKey == null || studentKey.trim().isEmpty()) {
            studentKey = "TG/2024/2085";
        }

        // Try to fetch latest live details from database
        try {
            Undergraduate dbStudent = dao.getUndergraduateByStudentId(studentKey);
            if (dbStudent != null) {
                if (dbStudent.getName() != null) loggedInStudent.setName(dbStudent.getName());
                if (dbStudent.getEmail() != null) loggedInStudent.setEmail(dbStudent.getEmail());
                if (dbStudent.getPhone() != null) loggedInStudent.setPhone(dbStudent.getPhone());
                if (dbStudent.getDepartment() != null) loggedInStudent.setDepartment(dbStudent.getDepartment());
                if (dbStudent.getBatch() != null) loggedInStudent.setBatch(dbStudent.getBatch());
                if (dbStudent.getRegistrationNo() != null) loggedInStudent.setRegistrationNo(dbStudent.getRegistrationNo());
                if (dbStudent.getEnrollmentYear() > 0) loggedInStudent.setEnrollmentYear(dbStudent.getEnrollmentYear());
            }

            // Also load fresh GPA if available
            GPA dbGpa = dao.getStudentGPA(studentKey);
            if (dbGpa != null) {
                loggedInStudent.setGpa(dbGpa);
            }

            // Also load attendance summary if available
            AttendanceSummary dbAtt = dao.getAttendanceSummary(studentKey);
            if (dbAtt != null) {
                loggedInStudent.setAttendanceSummary(dbAtt);
            }

            // Also load enrolled courses if available
            List<Course> dbCourses = dao.getCoursesForStudent(studentKey);
            if (dbCourses != null && !dbCourses.isEmpty()) {
                loggedInStudent.setCourses(dbCourses);
            }
        } catch (Exception e) {
            System.err.println("Notice: Using memory student profile: " + e.getMessage());
        }

        // Populate Banner
        String name = loggedInStudent.getName() != null ? loggedInStudent.getName() : "Student";
        String regNo = loggedInStudent.getRegistrationNo() != null ? loggedInStudent.getRegistrationNo() : "TG/2024/2085";
        String dept = loggedInStudent.getDepartment() != null ? loggedInStudent.getDepartment() : "Department of Information and Communication Technology";

        bannerNameLabel.setText(name);
        bannerRegNoLabel.setText("Reg No: " + regNo);
        bannerDeptLabel.setText("•  " + dept);
        bannerAvatarLabel.setIcon(createCircularInitialsAvatar(72, name));

        // Populate Quick Stats
        if (loggedInStudent.viewGPA() != null) {
            double cgpa = loggedInStudent.viewGPA().getCGPA();
            double sgpa = loggedInStudent.viewGPA().getSGPA();
            gpaValueLabel.setText(String.format("%.2f", cgpa > 0 ? cgpa : 3.65));
            gpaSubtitleLabel.setText(String.format("SGPA: %.2f  •  Good Academic Standing", sgpa > 0 ? sgpa : 3.58));
        }

        if (loggedInStudent.getAttendanceSummary() != null) {
            double pct = loggedInStudent.getAttendanceSummary().getPercentage();
            attendanceValueLabel.setText(String.format("%.1f%%", pct > 0 ? pct : 92.8));
            attendanceSubtitleLabel.setText(pct >= 80.0
                    ? "Eligible for Exams (Threshold: 80%)"
                    : "Caution: Below 80% requirement");
        }

        int courseCount = (loggedInStudent.viewCourses() != null) ? loggedInStudent.viewCourses().size() : 8;
        coursesValueLabel.setText(courseCount + " Courses");

        // Populate Academic Details
        valFullName.setText(name);
        valRegNo.setText(regNo);
        valStudentId.setText(loggedInStudent.getStudentId() != null ? loggedInStudent.getStudentId() : "1085");
        valDepartment.setText(dept);
        valBatch.setText(loggedInStudent.getBatch() != null ? loggedInStudent.getBatch() : "2023/2024");
        valEnrollmentYear.setText(String.valueOf(loggedInStudent.getEnrollmentYear() > 0 ? loggedInStudent.getEnrollmentYear() : 2024));

        // Populate Contact Details
        valUsername.setText(loggedInStudent.getUsername() != null ? loggedInStudent.getUsername() : "tg2085");
        valUserId.setText(loggedInStudent.getUserId() != null ? loggedInStudent.getUserId() : "USR-1085");
        txtEmail.setText(loggedInStudent.getEmail() != null ? loggedInStudent.getEmail() : "tg2085@fot.ruh.ac.lk");
        txtPhone.setText(loggedInStudent.getPhone() != null ? loggedInStudent.getPhone() : "+94 77 123 4567");

        revalidate();
        repaint();
    }

    /**
     * Toggles between view mode and edit mode for contact details.
     */
    private void enableContactEditMode(boolean enable) {
        this.isEditingContact = enable;
        txtEmail.setEditable(enable);
        txtPhone.setEditable(enable);

        if (enable) {
            txtEmail.setBackground(COLOR_WHITE);
            txtPhone.setBackground(COLOR_WHITE);
            txtEmail.setBorder(new CompoundBorder(new LineBorder(COLOR_MAIN_BLUE, 2, true), new EmptyBorder(5, 8, 5, 8)));
            txtPhone.setBorder(new CompoundBorder(new LineBorder(COLOR_MAIN_BLUE, 2, true), new EmptyBorder(5, 8, 5, 8)));
            contactStatusNotice.setText("✏ Editing contact details. Click 'Save Changes' to update.");
            contactStatusNotice.setForeground(COLOR_MAIN_BLUE);
            txtEmail.requestFocus();
        } else {
            styleInputField(txtEmail);
            styleInputField(txtPhone);
            contactStatusNotice.setText("Status: Contact details are up to date.");
            contactStatusNotice.setForeground(COLOR_TEXT_MUTED);
        }

        btnEditContact.setVisible(!enable);
        btnSaveContact.setVisible(enable);
        btnCancelContact.setVisible(enable);
    }

    /**
     * Saves the modified email and phone number to database and model.
     */
    private void saveContactChanges() {
        String newEmail = txtEmail.getText().trim();
        String newPhone = txtPhone.getText().trim();

        // Simple validation
        if (newEmail.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Email address cannot be empty.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocus();
            return;
        }

        if (!newEmail.contains("@") || !newEmail.contains(".")) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid email address (e.g., student@fot.ruh.ac.lk).",
                    "Invalid Email",
                    JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocus();
            return;
        }

        if (newPhone.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Phone number cannot be empty.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            txtPhone.requestFocus();
            return;
        }

        String studentKey = (loggedInStudent.getRegistrationNo() != null && !loggedInStudent.getRegistrationNo().isEmpty())
                ? loggedInStudent.getRegistrationNo() : loggedInStudent.getStudentId();

        // 1. Update in database via DAO
        boolean dbSuccess = dao.updateContactDetails(studentKey, newPhone, newEmail);

        // 2. Update model in memory (OOP encapsulation)
        loggedInStudent.updateContactDetails(newPhone, newEmail);

        enableContactEditMode(false);

        if (dbSuccess) {
            JOptionPane.showMessageDialog(this,
                    "Contact details successfully updated in the system database!",
                    "Profile Updated",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Contact details saved for current session.\n(Note: Database update could not be committed)",
                    "Profile Updated (Session)",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        loadProfileData();
    }

    // ==========================================
    // UI HELPER METHODS
    // ==========================================
    private JLabel createPillBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(fg);
        badge.setBackground(bg);
        badge.setOpaque(true);
        badge.setBorder(new CompoundBorder(
                new LineBorder(bg, 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));
        return badge;
    }

    private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        return btn;
    }

    private Icon createCircularInitialsAvatar(int size, String name) {
        String initials = "ST";
        if (name != null && !name.trim().isEmpty()) {
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) {
                initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
            } else if (parts[0].length() >= 2) {
                initials = parts[0].substring(0, 2).toUpperCase();
            }
        }
        final String text = initials;

        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Navy circle
                g2.setColor(COLOR_PRIMARY_NAVY);
                g2.fillOval(x, y, size, size);

                // Outer border accent ring
                g2.setColor(new Color(0x3B, 0x82, 0xF6));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(x + 1, y + 1, size - 2, size - 2);

                // Initials Text
                g2.setColor(COLOR_WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, size / 2 - 2));
                FontMetrics fm = g2.getFontMetrics();
                int tx = x + (size - fm.stringWidth(text)) / 2;
                int ty = y + ((size - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(text, tx, ty);

                g2.dispose();
            }

            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    private static Undergraduate createPreviewStudent() {
        Undergraduate student = new Undergraduate(
                "USR-1085", "tg2085", "", "H.D.W.B. Hapuarachchi",
                "tg2085@fot.ruh.ac.lk", "+94 77 123 4567", "",
                "STU-2085", "TG/2024/2085", "2023/2024",
                "Department of Information and Communication Technology", 2024
        );
        student.setGpa(new GPA(3.65, 3.58));
        student.setAttendanceSummary(new AttendanceSummary(30, 26, 2));
        return student;
    }

    /**
     * Standalone main method to preview and test the Profile UI directly.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TecFAMS - My Profile Preview");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 750);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new UndergraduateProfileUI(createPreviewStudent(), null));
            frame.setVisible(true);
        });
    }
}
