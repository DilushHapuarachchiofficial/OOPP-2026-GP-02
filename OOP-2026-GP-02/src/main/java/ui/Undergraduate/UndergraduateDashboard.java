package main.java.ui.Undergraduate;

import main.java.dao.Undergraduate.UndergraduateDAO;
import main.java.model.Undergraduate.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * TecFAMS - Faculty of Technology Academic Management System
 * Undergraduate Dashboard Window.
 *
 * Dedicated strictly to the primary Dashboard home view and metrics overview.
 * Complies with OOP Single Responsibility Principle (SRP).
 * Sub-modules (Profile, Attendance, Medical, etc.) are built as separate standalone UIs.
 */
public class UndergraduateDashboard extends JFrame {

    // Palette Colors
    private static final Color COLOR_PRIMARY_NAVY = new Color(0x12, 0x3B, 0x70);
    private static final Color COLOR_MAIN_BLUE    = new Color(0x25, 0x63, 0xEB);
    private static final Color COLOR_LIGHT_BLUE   = new Color(0xEA, 0xF2, 0xFF);
    private static final Color COLOR_WHITE        = new Color(0xFF, 0xFF, 0xFF);
    private static final Color COLOR_BG           = new Color(0xF5, 0xF8, 0xFD);
    private static final Color COLOR_TEXT_PRIMARY = new Color(0x17, 0x2B, 0x4D);
    private static final Color COLOR_TEXT_MUTED   = new Color(0x71, 0x80, 0x96);
    private static final Color COLOR_BORDER       = new Color(0xE2, 0xE8, 0xF0);
    private static final Color COLOR_SUCCESS      = new Color(0x10, 0xB9, 0x81);
    private static final Color COLOR_DANGER       = new Color(0xEF, 0x44, 0x44);

    private final Undergraduate currentStudent;
    private final UndergraduateDAO dao;
    private UndergraduateNavigation navigationPanel;
    private CardLayout contentCardLayout;
    private JPanel mainContentArea;
    private JScrollPane dashboardOverviewPanel;
    private UndergraduateLectureNotesUI lectureNotesPanel;
    private UndergraduateAttendanceUI attendancePanel;
    private UndergraduateMedicalUI medicalPanel;
    private UndergraduateCourseDetailsUI courseDetailsPanel;
    private UndergraduateGradesUI gradesPanel;
    private UndergraduateTimetableUI timetablePanel;
    private UndergraduateNoticesUI noticesPanel;

    public UndergraduateDashboard(Undergraduate student) {
        this.dao = new UndergraduateDAO();
        this.currentStudent = (student != null) ? student : createDefaultTestStudent();
        loadDatabaseData();
        initializeUI();
    }

    public UndergraduateDashboard() {
        this(null);
    }

    private void initializeUI() {
        setTitle("TecFAMS - Faculty of Technology | Undergraduate Dashboard");
        setSize(1280, 820);
        setMinimumSize(new Dimension(1080, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(COLOR_BG);

        // Header & Horizontal Academic Menu Bar directly below TecFAMS header
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.add(createHeaderPanel());

        navigationPanel = new UndergraduateNavigation(this, currentStudent);
        topContainer.add(navigationPanel);

        // Center: CardLayout container to seamlessly switch between Dashboard and Module Views
        contentCardLayout = new CardLayout();
        mainContentArea = new JPanel(contentCardLayout);
        mainContentArea.setBackground(COLOR_BG);

        dashboardOverviewPanel = createDashboardOverviewPanel();
        lectureNotesPanel = new UndergraduateLectureNotesUI(currentStudent, this);
        attendancePanel = new UndergraduateAttendanceUI(currentStudent, this);
        medicalPanel = new UndergraduateMedicalUI(currentStudent, this);
        courseDetailsPanel = new UndergraduateCourseDetailsUI(currentStudent, this);
        gradesPanel = new UndergraduateGradesUI(currentStudent, this);
        timetablePanel = new UndergraduateTimetableUI(currentStudent, this);
        noticesPanel = new UndergraduateNoticesUI(currentStudent, this);

        mainContentArea.add(dashboardOverviewPanel, "DASHBOARD");
        mainContentArea.add(lectureNotesPanel, "LECTURE_NOTES");
        mainContentArea.add(attendancePanel, "ATTENDANCE");
        mainContentArea.add(medicalPanel, "MEDICAL");
        mainContentArea.add(courseDetailsPanel, "COURSES");
        mainContentArea.add(gradesPanel, "GRADES");
        mainContentArea.add(timetablePanel, "TIMETABLE");
        mainContentArea.add(noticesPanel, "NOTICES");

        rootPanel.add(topContainer, BorderLayout.NORTH);
        rootPanel.add(mainContentArea, BorderLayout.CENTER);

        setContentPane(rootPanel);
    }

    private void loadDatabaseData() {
        if (currentStudent == null) return;
        try {
            String lookupId = (currentStudent.getRegistrationNo() != null && !currentStudent.getRegistrationNo().isEmpty())
                    ? currentStudent.getRegistrationNo() : currentStudent.getStudentId();
            if (lookupId == null || lookupId.trim().isEmpty()) {
                lookupId = "TG/2024/2085";
            }

            // 1. Fetch live profile from MySQL Workbench database
            Undergraduate dbProfile = dao.getUndergraduateByStudentId(lookupId);
            if (dbProfile != null) {
                currentStudent.setName(dbProfile.getName());
                currentStudent.setEmail(dbProfile.getEmail());
                currentStudent.setPhone(dbProfile.getPhone());
                currentStudent.setDepartment(dbProfile.getDepartment());
                currentStudent.setBatch(dbProfile.getBatch());
                currentStudent.setEnrollmentYear(dbProfile.getEnrollmentYear());
                if (dbProfile.getStudentId() != null) currentStudent.setStudentId(dbProfile.getStudentId());
                if (dbProfile.getRegistrationNo() != null) currentStudent.setRegistrationNo(dbProfile.getRegistrationNo());
            }

            // 2. Fetch live notices
            List<Notice> dbNotices = dao.getRecentNotices(6);
            if (dbNotices != null && !dbNotices.isEmpty()) {
                currentStudent.setNotices(dbNotices);
            }

            // 3. Fetch live timetable
            List<Schedule> dbLectures = dao.getTodayLectures(
                    lookupId, currentStudent.getDepartment(), currentStudent.getBatch());
            if (dbLectures != null && !dbLectures.isEmpty()) {
                Timetable tt = currentStudent.viewTimetable();
                if (tt == null) tt = new Timetable(1, "Semester I", "2026");
                tt.setSchedules(dbLectures);
                currentStudent.setTimetable(tt);
            }

            // 4. Fetch live GPA
            GPA dbGpa = dao.getStudentGPA(lookupId);
            if (dbGpa != null) {
                currentStudent.setGpa(dbGpa);
            }

            // 5. Fetch live attendance summary
            AttendanceSummary dbAtt = dao.getAttendanceSummary(lookupId);
            if (dbAtt != null) {
                currentStudent.setAttendanceSummary(dbAtt);
            }

            // 6. Fetch live registered courses and lecturer materials
            List<Course> dbCourses = dao.getCoursesForStudent(lookupId);
            List<CourseMaterial> dbMat = dao.getCourseMaterialsForStudent(lookupId);
            if (dbCourses != null && !dbCourses.isEmpty()) {
                if (dbMat != null && !dbMat.isEmpty()) {
                    for (Course c : dbCourses) {
                        for (CourseMaterial m : dbMat) {
                            if (m.getCourseCode() != null && m.getCourseCode().equalsIgnoreCase(c.getCourseCode())) {
                                c.addMaterial(m);
                            }
                        }
                    }
                }
                currentStudent.setCourses(dbCourses);
            } else if (dbMat != null && !dbMat.isEmpty() && currentStudent.viewCourses() != null && !currentStudent.viewCourses().isEmpty()) {
                currentStudent.viewCourses().get(0).setMaterials(dbMat);
            }
        } catch (Exception e) {
            System.err.println("Notice: Running with fallback preview state: " + e.getMessage());
        }
    }

    // ==========================================
    // 1. TOP HEADER SECTION
    // ==========================================
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_WHITE);
        header.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
                new EmptyBorder(12, 24, 12, 24)
        ));

        JPanel leftBrand = new JPanel(new GridLayout(2, 1, 0, 2));
        leftBrand.setOpaque(false);
        JLabel brand = new JLabel("TecFAMS");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brand.setForeground(COLOR_PRIMARY_NAVY);
        JLabel sub = new JLabel("Undergraduate Dashboard");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(COLOR_TEXT_MUTED);
        leftBrand.add(brand);
        leftBrand.add(sub);

        JPanel rightProfile = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightProfile.setOpaque(false);

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"));
        JLabel dateBadge = new JLabel("Date: " + dateStr);
        dateBadge.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateBadge.setForeground(COLOR_TEXT_MUTED);
        dateBadge.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(5, 10, 5, 10)));

        JPanel nameBox = new JPanel(new GridLayout(3, 1, 0, 1));
        nameBox.setOpaque(false);
        JLabel nameLbl = new JLabel(currentStudent.getName() != null ? currentStudent.getName() : "Student");
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLbl.setForeground(COLOR_TEXT_PRIMARY);

        JLabel roleLbl = new JLabel("Undergraduate");
        roleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        roleLbl.setForeground(COLOR_MAIN_BLUE);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        logoutBtn.setForeground(new Color(0xDC, 0x26, 0x26));
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setHorizontalAlignment(SwingConstants.LEFT);
        logoutBtn.setBorder(new EmptyBorder(0, 0, 0, 0));
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setOpaque(false);
        logoutBtn.setIcon(createSmallLogoutIcon(new Color(0xDC, 0x26, 0x26)));
        logoutBtn.setIconTextGap(4);

        logoutBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                logoutBtn.setForeground(new Color(0x99, 0x1B, 0x1B));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoutBtn.setForeground(new Color(0xDC, 0x26, 0x26));
            }
        });

        logoutBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to log out of TecFAMS?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (choice == JOptionPane.YES_OPTION) {
                if (currentStudent != null) {
                    currentStudent.logout();
                }
                this.dispose();
            }
        });

        nameBox.add(nameLbl);
        nameBox.add(roleLbl);
        nameBox.add(logoutBtn);

        rightProfile.add(dateBadge);
        rightProfile.add(nameBox);
        rightProfile.add(createAvatarIcon(40, currentStudent.getName()));

        header.add(leftBrand, BorderLayout.WEST);
        header.add(rightProfile, BorderLayout.EAST);
        return header;
    }

    // ==========================================
    // 2. DASHBOARD OVERVIEW MAIN PANEL
    // ==========================================
    private JScrollPane createDashboardOverviewPanel() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(COLOR_BG);
        content.setBorder(new EmptyBorder(22, 26, 26, 26));

        // Welcome Box
        String fname = currentStudent.getName();
        if (fname != null && fname.contains(" ")) fname = fname.split(" ")[0];

        JLabel wTitle = new JLabel("Welcome back, " + (fname != null ? fname : "Student") + "!");
        wTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        wTitle.setForeground(COLOR_TEXT_PRIMARY);

        JLabel wSub = new JLabel("Here is your academic overview for today. Stay updated with your lectures and announcements.");
        wSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        wSub.setForeground(COLOR_TEXT_MUTED);

        JPanel welcomeBox = new JPanel();
        welcomeBox.setLayout(new BoxLayout(welcomeBox, BoxLayout.Y_AXIS));
        welcomeBox.setOpaque(false);
        welcomeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        welcomeBox.add(wTitle);
        welcomeBox.add(Box.createVerticalStrut(4));
        welcomeBox.add(wSub);
        content.add(welcomeBox);
        content.add(Box.createVerticalStrut(18));

        // 3 Summary Metric Cards
        JPanel cardsRow = createSummaryCardsRow();
        cardsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(cardsRow);
        content.add(Box.createVerticalStrut(20));

        // 2-Column Split: Lectures/Notes (Left) & Notices/Assistance (Right)
        JPanel gridSplit = new JPanel(new GridBagLayout());
        gridSplit.setOpaque(false);
        gridSplit.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left Column (62%)
        gbc.gridx = 0;
        gbc.weightx = 0.62;
        gbc.insets = new Insets(0, 0, 0, 16);
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);
        leftCol.add(createTodayLecturesCard());
        leftCol.add(Box.createVerticalStrut(18));
        leftCol.add(createLecturerNotesCard());
        gridSplit.add(leftCol, gbc);

        // Right Column (38%)
        gbc.gridx = 1;
        gbc.weightx = 0.38;
        gbc.insets = new Insets(0, 0, 0, 0);
        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setOpaque(false);
        rightCol.add(createRecentNoticesCard());
        rightCol.add(Box.createVerticalStrut(18));
        rightCol.add(createAcademicSupportCard());
        gridSplit.add(rightCol, gbc);

        content.add(gridSplit);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(COLOR_BG);
        return scroll;
    }

    // ==========================================
    // 3. 3 SUMMARY METRIC CARDS
    // ==========================================
    private JPanel createSummaryCardsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        row.setPreferredSize(new Dimension(0, 120));

        // Card 1: Student Profile (Integrated with Enrollment Year & Academic Details)
        int yr = currentStudent.getEnrollmentYear();
        String enrolledStr = yr > 0 ? "Enrolled in " + yr : "Enrolled in 2024";
        String regStr = "Reg: " + (currentStudent.getRegistrationNo() != null ? currentStudent.getRegistrationNo() : "N/A");
        String batchStr = "Batch: " + (currentStudent.getBatch() != null ? currentStudent.getBatch() : "N/A");

        JPanel profileCard = createMetricCard(
                "STUDENT PROFILE",
                currentStudent.getName() != null ? currentStudent.getName() : "Student",
                regStr + "  •  " + batchStr,
                enrolledStr + "  •  Level II (Semester I)",
                COLOR_MAIN_BLUE
        );
        if (currentStudent.getDepartment() != null && !currentStudent.getDepartment().isEmpty()) {
            profileCard.setToolTipText(currentStudent.getDepartment());
        }
        row.add(profileCard);

        // Card 2: Current GPA (Clickable to open Grades & GPA UI)
        GPA gpa = currentStudent.viewGPA();
        String gpaStr = (gpa != null && (gpa.getSGPA() > 0 || gpa.getCGPA() > 0))
                ? String.format("SGPA: %.2f | CGPA: %.2f", gpa.getSGPA(), gpa.getCGPA())
                : "Not available";
        JPanel gpaCard = createMetricCard(
                "CURRENT GPA",
                gpaStr,
                "Grading: UGC Circular 12-2024",
                gpa != null && gpa.getCGPA() >= 3.0 ? "Standing: Good (Click to view)" : "Status: Active (Click to view)",
                new Color(0x7C, 0x3A, 0xED)
        );
        gpaCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gpaCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openGrades();
            }
        });
        row.add(gpaCard);

        // Card 3: Attendance Overview
        AttendanceSummary att = currentStudent.getAttendanceSummary();
        boolean eligible = (att != null && att.isEligible());
        String attStr = (att != null && att.getTotalSessions() > 0)
                ? String.format("%.1f%%", att.calculatePercentage())
                : "Not available";
        String attSub = (att != null && att.getTotalSessions() > 0)
                ? att.getAttendedSessions() + " / " + att.getTotalSessions() + " sessions"
                : "Requires TO Entry";
        JPanel attCard = createMetricCard(
                "ATTENDANCE OVERVIEW",
                attStr,
                attSub,
                eligible ? "Eligible for Exam (>=80%)" : (att != null ? "Below 80% Requirement" : "Requirement: 80%"),
                eligible ? COLOR_SUCCESS : (att != null ? COLOR_DANGER : COLOR_TEXT_MUTED)
        );
        attCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
        attCard.setToolTipText("Click to view full attendance registry and breakdown");
        attCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openAttendance();
            }
        });
        row.add(attCard);

        return row;
    }

    private JPanel createMetricCard(String title, String val, String sub1, String sub2, Color accent) {
        JPanel card = new JPanel(new GridLayout(4, 1, 0, 3));
        card.setBackground(COLOR_WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(12, 14, 12, 14)));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 11));
        t.setForeground(accent);

        JLabel v = new JLabel(val);
        v.setFont(new Font("Segoe UI", Font.BOLD, val.length() > 20 ? 12 : 14));
        v.setForeground(COLOR_TEXT_PRIMARY);

        JLabel s1 = new JLabel(sub1);
        s1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        s1.setForeground(COLOR_TEXT_MUTED);

        JLabel s2 = new JLabel(sub2);
        s2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        s2.setForeground(accent);

        card.add(t);
        card.add(v);
        card.add(s1);
        card.add(s2);
        return card;
    }

    // ==========================================
    // 4. TODAY'S LECTURES CARD
    // ==========================================
    private JPanel createTodayLecturesCard() {
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);

        Timetable tt = currentStudent.viewTimetable();
        List<Schedule> lectures = (tt != null) ? tt.getTodaySchedules() : new ArrayList<>();
        if (lectures.isEmpty() && tt != null && !tt.getSchedules().isEmpty()) lectures = tt.getSchedules();

        if (lectures.isEmpty()) {
            JLabel empty = new JLabel("No lectures scheduled for today.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            empty.setForeground(COLOR_TEXT_MUTED);
            list.add(empty);
        } else {
            Collections.sort(lectures, Comparator.comparing(Schedule::getStartTime, Comparator.nullsLast(String::compareTo)));
            for (int i = 0; i < lectures.size(); i++) {
                list.add(createLectureRowItem(lectures.get(i), i == 0));
                if (i < lectures.size() - 1) list.add(Box.createVerticalStrut(8));
            }
        }

        JLabel badge = new JLabel("Day: " + LocalDate.now().getDayOfWeek());
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(COLOR_MAIN_BLUE);
        return createSectionCard("Today's Lectures", badge, list);
    }

    private JPanel createLectureRowItem(Schedule s, boolean isUpcoming) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(isUpcoming ? new Color(0xF4, 0xF8, 0xFF) : COLOR_WHITE);
        row.setBorder(new CompoundBorder(
                new LineBorder(isUpcoming ? COLOR_MAIN_BLUE : COLOR_BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JPanel timeBox = new JPanel(new GridLayout(2, 1, 0, 2));
        timeBox.setOpaque(false);
        timeBox.setPreferredSize(new Dimension(130, 36));

        JLabel timeLbl = new JLabel(s.getStartTime() + " - " + s.getEndTime());
        timeLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        timeLbl.setForeground(COLOR_PRIMARY_NAVY);

        JLabel typeBadge = new JLabel(s.getSessionType() != null ? s.getSessionType().name() : "THEORY");
        typeBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        typeBadge.setForeground(s.getSessionType() == SessionType.PRACTICAL ? new Color(0x7C, 0x3A, 0xED) : COLOR_MAIN_BLUE);

        timeBox.add(timeLbl);
        timeBox.add(typeBadge);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);

        JLabel courseLbl = new JLabel(s.getCourseCode() + ": " + s.getCourseName());
        courseLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        courseLbl.setForeground(COLOR_TEXT_PRIMARY);

        JLabel desc = new JLabel("Lecturer: " + (s.getLecturerName() != null ? s.getLecturerName() : "Lecturer") +
                " | Room: " + (s.getLocation() != null ? s.getLocation() : "TBA"));
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        desc.setForeground(COLOR_TEXT_MUTED);

        info.add(courseLbl);
        info.add(desc);

        row.add(timeBox, BorderLayout.WEST);
        row.add(info, BorderLayout.CENTER);

        if (isUpcoming) {
            JLabel badge = new JLabel("Upcoming");
            badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
            badge.setForeground(COLOR_MAIN_BLUE);
            row.add(badge, BorderLayout.EAST);
        }

        return row;
    }

    // ==========================================
    // 5. LECTURER NOTES CARD
    // ==========================================
    private JPanel createLecturerNotesCard() {
        JButton viewAll = createLinkButton("View All Materials →", e -> openLectureNotes());

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);

        List<CourseMaterial> materials = extractMaterials();
        if (materials.isEmpty()) {
            JLabel empty = new JLabel("No course materials currently available.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            empty.setForeground(COLOR_TEXT_MUTED);
            list.add(empty);
        } else {
            int count = Math.min(3, materials.size());
            for (int i = 0; i < count; i++) {
                list.add(createMaterialRowItem(materials.get(i)));
                if (i < count - 1) list.add(Box.createVerticalStrut(8));
            }
        }

        return createSectionCard("Lecturer Notes & Materials", viewAll, list);
    }

    private JPanel createMaterialRowItem(CourseMaterial cm) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(COLOR_WHITE);
        row.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(8, 12, 8, 12)));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));
        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openLectureNotes();
            }
        });

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);

        JLabel tLbl = new JLabel(cm.getTitle());
        tLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tLbl.setForeground(COLOR_TEXT_PRIMARY);

        JLabel dLbl = new JLabel(cm.getCourseCode() + " | " + cm.getDescription() + " | " + cm.getUploadedDate());
        dLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dLbl.setForeground(COLOR_TEXT_MUTED);

        info.add(tLbl);
        info.add(dLbl);
        row.add(info, BorderLayout.CENTER);

        JButton openBtn = new JButton("View Notes →");
        openBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        openBtn.setForeground(COLOR_MAIN_BLUE);
        openBtn.setBackground(COLOR_LIGHT_BLUE);
        openBtn.setFocusPainted(false);
        openBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        openBtn.setBorder(new CompoundBorder(new LineBorder(COLOR_MAIN_BLUE, 1, true), new EmptyBorder(5, 12, 5, 12)));
        openBtn.addActionListener(e -> openLectureNotes());

        row.add(openBtn, BorderLayout.EAST);
        return row;
    }

    private List<CourseMaterial> extractMaterials() {
        List<CourseMaterial> all = new ArrayList<>();
        if (currentStudent.viewCourses() != null) {
            for (Course c : currentStudent.viewCourses()) {
                if (c.getMaterials() != null) all.addAll(c.getMaterials());
            }
        }
        return all;
    }

    // ==========================================
    // 6. RECENT NOTICES CARD
    // ==========================================
    private JPanel createRecentNoticesCard() {
        JButton viewAll = createLinkButton("View All Notices →", e -> openNotices());

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);

        List<Notice> notices = currentStudent.viewNotices();
        if (notices == null || notices.isEmpty()) {
            JLabel empty = new JLabel("No notices published currently.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            empty.setForeground(COLOR_TEXT_MUTED);
            list.add(empty);
        } else {
            int count = Math.min(3, notices.size());
            for (int i = 0; i < count; i++) {
                list.add(createNoticeRowItem(notices.get(i)));
                if (i < count - 1) list.add(Box.createVerticalStrut(8));
            }
        }

        return createSectionCard("Recent Notices", viewAll, list);
    }

    private JPanel createNoticeRowItem(Notice n) {
        JPanel box = new JPanel(new BorderLayout(0, 4));
        box.setBackground(COLOR_WHITE);
        box.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(8, 12, 8, 12)));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel t = new JLabel(n.getTitle());
        t.setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.setForeground(COLOR_TEXT_PRIMARY);

        JLabel tag = new JLabel("Active");
        tag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        tag.setForeground(COLOR_SUCCESS);

        top.add(t, BorderLayout.WEST);
        top.add(tag, BorderLayout.EAST);

        JLabel desc = new JLabel("<html><body style='width: 240px;'>" + n.getDescription() + "</body></html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        desc.setForeground(COLOR_TEXT_MUTED);

        JLabel date = new JLabel("Published: " + n.getPublishedDate());
        date.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        date.setForeground(new Color(0x94, 0xA3, 0xB8));

        box.add(top, BorderLayout.NORTH);
        box.add(desc, BorderLayout.CENTER);
        box.add(date, BorderLayout.SOUTH);
        return box;
    }

    // ==========================================
    // 7. ACADEMIC ASSISTANCE CARD
    // ==========================================
    private JPanel createAcademicSupportCard() {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(COLOR_LIGHT_BLUE);
        card.setBorder(new CompoundBorder(new LineBorder(COLOR_MAIN_BLUE, 1, true), new EmptyBorder(12, 16, 12, 16)));

        JLabel title = new JLabel("Academic Assistance");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(COLOR_PRIMARY_NAVY);

        JLabel desc = new JLabel("<html>For inquiries, contact your Technical Officer or Dean's Office.<br><b>Email:</b> tech-dean@fot.ruh.ac.lk<br><b>Desk:</b> Admin Block B, Level 1</html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        desc.setForeground(COLOR_TEXT_PRIMARY);

        card.add(title, BorderLayout.NORTH);
        card.add(desc, BorderLayout.CENTER);
        return card;
    }

    private JPanel createSectionCard(String titleText, JComponent headerRight, JComponent content) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(COLOR_WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(14, 18, 14, 18)));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(COLOR_PRIMARY_NAVY);

        head.add(title, BorderLayout.WEST);
        if (headerRight != null) head.add(headerRight, BorderLayout.EAST);

        card.add(head, BorderLayout.NORTH);
        if (content != null) card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JButton createLinkButton(String text, ActionListener al) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(COLOR_MAIN_BLUE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(al);
        return btn;
    }

    // ==========================================
    // 8. MODULE ACTIONS & HELPERS
    // ==========================================
    public void openLectureNotes() {
        if (navigationPanel != null) {
            navigationPanel.highlightButton("MATERIALS");
        }
        if (lectureNotesPanel != null) {
            lectureNotesPanel.loadLectureNotesData();
        }
        if (contentCardLayout != null && mainContentArea != null) {
            contentCardLayout.show(mainContentArea, "LECTURE_NOTES");
        }
    }

    public void openAttendance() {
        if (navigationPanel != null) {
            navigationPanel.highlightButton("ATTENDANCE");
        }
        if (attendancePanel != null) {
            attendancePanel.loadAttendanceData();
        }
        if (contentCardLayout != null && mainContentArea != null) {
            contentCardLayout.show(mainContentArea, "ATTENDANCE");
        }
    }

    public void openMedical() {
        if (navigationPanel != null) {
            navigationPanel.highlightButton("MEDICAL");
        }
        if (medicalPanel != null) {
            medicalPanel.loadMedicalData();
        }
        if (contentCardLayout != null && mainContentArea != null) {
            contentCardLayout.show(mainContentArea, "MEDICAL");
        }
    }

    public void openCourseDetails() {
        if (navigationPanel != null) {
            navigationPanel.highlightButton("COURSES");
        }
        if (courseDetailsPanel != null) {
            courseDetailsPanel.loadCourseData();
        }
        if (contentCardLayout != null && mainContentArea != null) {
            contentCardLayout.show(mainContentArea, "COURSES");
        }
    }

    public void openGrades() {
        if (navigationPanel != null) {
            navigationPanel.highlightButton("GRADES");
        }
        if (gradesPanel != null) {
            gradesPanel.loadGradesData();
        }
        if (contentCardLayout != null && mainContentArea != null) {
            contentCardLayout.show(mainContentArea, "GRADES");
        }
    }

    public void openTimetable() {
        if (navigationPanel != null) {
            navigationPanel.highlightButton("TIMETABLE");
        }
        if (timetablePanel != null) {
            timetablePanel.loadTimetableData();
        }
        if (contentCardLayout != null && mainContentArea != null) {
            contentCardLayout.show(mainContentArea, "TIMETABLE");
        }
    }

    public void openNotices() {
        if (navigationPanel != null) {
            navigationPanel.highlightButton("NOTICES");
        }
        if (noticesPanel != null) {
            noticesPanel.loadNoticesData();
        }
        if (contentCardLayout != null && mainContentArea != null) {
            contentCardLayout.show(mainContentArea, "NOTICES");
        }
    }

    public void showModulePlaceholder(String moduleName) {
        if ("Lecturer Notes".equalsIgnoreCase(moduleName) || "Course Materials".equalsIgnoreCase(moduleName) || "MATERIALS".equalsIgnoreCase(moduleName)) {
            openLectureNotes();
            return;
        }
        if ("Attendance".equalsIgnoreCase(moduleName) || "ATTENDANCE".equalsIgnoreCase(moduleName)) {
            openAttendance();
            return;
        }
        if ("Medical Details".equalsIgnoreCase(moduleName) || "Medical".equalsIgnoreCase(moduleName) || "MEDICAL".equalsIgnoreCase(moduleName)) {
            openMedical();
            return;
        }
        if ("Course Details".equalsIgnoreCase(moduleName) || "Courses".equalsIgnoreCase(moduleName) || "COURSES".equalsIgnoreCase(moduleName)) {
            openCourseDetails();
            return;
        }
        if ("Grades & GPA".equalsIgnoreCase(moduleName) || "Grades".equalsIgnoreCase(moduleName) || "GPA".equalsIgnoreCase(moduleName) || "GRADES".equalsIgnoreCase(moduleName)) {
            openGrades();
            return;
        }
        if ("Timetable".equalsIgnoreCase(moduleName) || "TIMETABLE".equalsIgnoreCase(moduleName)) {
            openTimetable();
            return;
        }
        if ("Notices".equalsIgnoreCase(moduleName) || "NOTICES".equalsIgnoreCase(moduleName) || "Notices & Announcements".equalsIgnoreCase(moduleName)) {
            openNotices();
            return;
        }
        JOptionPane.showMessageDialog(
                this,
                moduleName + " UI will open in a separate window.\n(Currently under development by group members)",
                "TecFAMS - " + moduleName,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void showDashboardView() {
        if (navigationPanel != null) {
            navigationPanel.highlightButton("DASHBOARD");
        }
        if (contentCardLayout != null && mainContentArea != null) {
            contentCardLayout.show(mainContentArea, "DASHBOARD");
        }
    }

    private JLabel createAvatarIcon(int size, String name) {
        JLabel avatar = new JLabel();
        avatar.setPreferredSize(new Dimension(size, size));

        String initials = "ST";
        if (name != null && !name.trim().isEmpty()) {
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
            else if (parts[0].length() >= 2) initials = parts[0].substring(0, 2).toUpperCase();
        }
        final String text = initials;

        avatar.setIcon(new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_PRIMARY_NAVY);
                g2.fillOval(x, y, size, size);
                g2.setColor(COLOR_WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
                FontMetrics fm = g2.getFontMetrics();
                int tx = x + (size - fm.stringWidth(text)) / 2;
                int ty = y + ((size - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(text, tx, ty);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        });
        return avatar;
    }

    private Icon createSmallLogoutIcon(Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                // Door outline
                g2.drawRect(x + 1, y + 1, 5, 9);
                // Exit arrow
                g2.drawLine(x + 4, y + 5, x + 9, y + 5);
                g2.drawLine(x + 7, y + 3, x + 9, y + 5);
                g2.drawLine(x + 7, y + 7, x + 9, y + 5);
                g2.dispose();
            }

            @Override public int getIconWidth() { return 11; }
            @Override public int getIconHeight() { return 11; }
        };
    }

    public Undergraduate getCurrentStudent() { return currentStudent; }
    public UndergraduateNavigation getNavigationPanel() { return navigationPanel; }

    // ==========================================
    // 9. PREVIEW DATA & ENTRY POINT
    // ==========================================
    private static Undergraduate createDefaultTestStudent() {
        Undergraduate student = new Undergraduate(
                "USR-1085", "tg2085", "", "H.D.W.B. Hapuarachchi",
                "tg2085@fot.ruh.ac.lk", "+94 77 123 4567", "",
                "STU-2085", "TG/2024/2085", "2023/2024",
                "Department of Information and Communication Technology", 2024
        );

        student.setGpa(new GPA(3.65, 3.58));
        student.setAttendanceSummary(new AttendanceSummary(30, 26, 2));

        Timetable tt = new Timetable(1, "Semester I", "2026");
        String today = LocalDate.now().getDayOfWeek().toString();
        tt.addSchedule(new Schedule(1, today, "08:30 AM", "10:30 AM", "Lab 02, Tech Building", SessionType.PRACTICAL, "ICT2132", "OOP Practicum", "Dr. K. A. Perera"));
        tt.addSchedule(new Schedule(2, today, "10:45 AM", "12:45 PM", "Lecture Hall 01", SessionType.THEORY, "ICT2113", "Data Structures", "Prof. S. R. Fernando"));
        tt.addSchedule(new Schedule(3, today, "01:30 PM", "03:30 PM", "Lab 04, Tech Complex", SessionType.PRACTICAL, "ICT2122", "Database Systems", "Dr. M. Silva"));
        student.setTimetable(tt);

        Course c1 = new Course(1, "ICT2132", "OOP Practicum", 2, "DICT", "Java OOP principles and GUI.");
        c1.addMaterial(new CourseMaterial(1, "ICT2132", "Lecture 05 - Java Swing GUI", "Swing GUI layout design.", "materials/swing.pdf", "2026-10-12"));
        c1.addMaterial(new CourseMaterial(2, "ICT2132", "Lab Sheet 06 - JDBC & DAO", "PreparedStatement connectivity.", "materials/jdbc.pdf", "2026-10-14"));
        List<Course> cList = new ArrayList<>();
        cList.add(c1);
        student.setCourses(cList);

        List<Notice> notices = new ArrayList<>();
        notices.add(new Notice(1, "Mid-Semester Examination Schedule Released", "Level II Semester I exam timetable published.", "2026-10-14", "2026-10-30"));
        notices.add(new Notice(2, "Submission of Medical Certificates for Level II", "All medical certificates must be certified by UMO.", "2026-10-12", "2026-10-25"));
        student.setNotices(notices);

        return student;
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            Undergraduate sampleStudent = createDefaultTestStudent();
            UndergraduateDashboard dashboard = new UndergraduateDashboard(sampleStudent);
            dashboard.setVisible(true);
        });
    }
}
