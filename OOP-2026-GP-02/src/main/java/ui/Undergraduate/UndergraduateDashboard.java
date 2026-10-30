package main.java.ui.Undergraduate;

import main.java.dao.Undergraduate.UndergraduateDAO;
import main.java.model.Undergraduate.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
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
    private UndergraduateProfileUI profilePanel;
    private JLabel headerNameLbl;
    private JLabel headerAvatar;

    public UndergraduateDashboard(Undergraduate student) {
        this.dao = new UndergraduateDAO();
        if (student == null) {
            main.java.model.UserSession session = main.java.model.UserSession.getCurrentSession();
            if (session != null) {
                if (session.getUsername() != null && !session.getUsername().trim().isEmpty()) {
                    student = dao.getUndergraduateByStudentId(session.getUsername().trim());
                }
                if (student == null && session.getUserId() > 0) {
                    student = dao.getUndergraduateByStudentId(String.valueOf(session.getUserId()));
                }
            }
        }
        if (student == null) {
            student = dao.getUndergraduateByStudentId("TG/2024/2083");
        }
        if (student == null) {
            student = dao.getUndergraduateByStudentId("TG/2024/2085");
        }
        if (student == null) {
            main.java.model.UserSession session = main.java.model.UserSession.getCurrentSession();
            int uid = (session != null && session.getUserId() > 0) ? session.getUserId() : 19;
            String uname = (session != null && session.getUsername() != null) ? session.getUsername() : "TG/2024/2083";
            String fname = (session != null && session.getFullName() != null) ? session.getFullName() : "Student";
            String email = (session != null && session.getEmail() != null) ? session.getEmail() : "student@ruh.ac.lk";
            String dept = (session != null && session.getDepartmentName() != null) ? session.getDepartmentName() : "Department of Information and Communication Technology";
            student = new Undergraduate(
                uid, uname, "", fname, email, "0710001000", "default_avatar.png",
                String.valueOf(uid), uname, "Batch 2023/2024", dept, 2024
            );
        }
        this.currentStudent = student;
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
        profilePanel = new UndergraduateProfileUI(currentStudent, this);

        mainContentArea.add(dashboardOverviewPanel, "DASHBOARD");
        mainContentArea.add(lectureNotesPanel, "LECTURE_NOTES");
        mainContentArea.add(attendancePanel, "ATTENDANCE");
        mainContentArea.add(medicalPanel, "MEDICAL");
        mainContentArea.add(courseDetailsPanel, "COURSES");
        mainContentArea.add(gradesPanel, "GRADES");
        mainContentArea.add(timetablePanel, "TIMETABLE");
        mainContentArea.add(noticesPanel, "NOTICES");
        mainContentArea.add(profilePanel, "PROFILE");

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
                lookupId = (currentStudent.getUsername() != null && !currentStudent.getUsername().trim().isEmpty()) ? currentStudent.getUsername() : "TG/2024/2083";
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
                if (dbProfile.getProfilePicture() != null) currentStudent.setProfilePicture(dbProfile.getProfilePicture());
                if (dbProfile.getAddress() != null) currentStudent.setAddress(dbProfile.getAddress());
                if (dbProfile.getNic() != null) currentStudent.setNic(dbProfile.getNic());
                if (dbProfile.getDob() != null) currentStudent.setDob(dbProfile.getDob());
                if (dbProfile.getUserId() > 0) currentStudent.setUserId(dbProfile.getUserId());
                if (dbProfile.getUsername() != null) currentStudent.setUsername(dbProfile.getUsername());
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

            // 6. Fetch live registered courses
            List<Course> dbCourses = dao.getCoursesForStudent(lookupId);
            if (dbCourses != null && !dbCourses.isEmpty()) {
                currentStudent.setCourses(dbCourses);
            }
        } catch (Exception e) {
            System.err.println("Database Notice: Could not load data from database: " + e.getMessage());
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
        headerNameLbl = new JLabel(currentStudent.getName() != null ? currentStudent.getName() : "Student");
        headerNameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        headerNameLbl.setForeground(COLOR_TEXT_PRIMARY);
        headerNameLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        headerNameLbl.setToolTipText("Click to view My Profile");
        headerNameLbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openProfile();
            }
        });

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
                main.java.model.UserSession.clearSession();
                this.dispose();
                SwingUtilities.invokeLater(() -> {
                    main.java.ui.WelcomeScreen welcomeScreen = new main.java.ui.WelcomeScreen();
                    welcomeScreen.setVisible(true);
                });
            }
        });

        nameBox.add(headerNameLbl);
        nameBox.add(roleLbl);
        nameBox.add(logoutBtn);

        headerAvatar = new JLabel();
        headerAvatar.setPreferredSize(new Dimension(40, 40));
        headerAvatar.setIcon(renderAvatarIcon(40, currentStudent.getProfilePicture(), currentStudent.getName()));
        headerAvatar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        headerAvatar.setToolTipText("Click to view My Profile");
        headerAvatar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openProfile();
            }
        });

        rightProfile.add(dateBadge);
        rightProfile.add(nameBox);
        rightProfile.add(headerAvatar);

        header.add(leftBrand, BorderLayout.WEST);
        header.add(rightProfile, BorderLayout.EAST);
        return header;
    }

    // ==========================================
    // 2. DASHBOARD OVERVIEW MAIN PANEL
    // ==========================================
    private JScrollPane createDashboardOverviewPanel() {
        JPanel content = new ScrollableTrackWidthPanel();
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

        JPanel welcomeBox = new JPanel(new BorderLayout(0, 4));
        welcomeBox.setOpaque(false);
        welcomeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        welcomeBox.add(wTitle, BorderLayout.NORTH);
        welcomeBox.add(wSub, BorderLayout.SOUTH);
        content.add(welcomeBox);
        content.add(Box.createVerticalStrut(18));

        // 3 Summary Metric Cards
        JPanel cardsRow = createSummaryCardsRow();
        cardsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(cardsRow);
        content.add(Box.createVerticalStrut(20));

        // 2-Column Split: Today's Lectures (50%) & Recent Notices (50%)
        JPanel gridSplit = new JPanel(new GridBagLayout());
        gridSplit.setOpaque(false);
        gridSplit.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left Column (54%): Today's Lectures
        gbc.gridx = 0;
        gbc.weightx = 0.54;
        gbc.insets = new Insets(0, 0, 0, 12);
        gridSplit.add(createTodayLecturesCard(), gbc);

        // Right Column (46%): Recent Notices
        gbc.gridx = 1;
        gbc.weightx = 0.46;
        gbc.insets = new Insets(0, 12, 0, 0);
        gridSplit.add(createRecentNoticesCard(), gbc);

        content.add(gridSplit);
        content.add(Box.createVerticalStrut(18));

        // Academic Support Banner (Full Width)
        JPanel supportCard = createAcademicSupportCard();
        supportCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(supportCard);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
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
            JPanel emptyRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
            emptyRow.setOpaque(false);
            JLabel empty = new JLabel("No lectures scheduled for today.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            empty.setForeground(COLOR_TEXT_MUTED);
            emptyRow.add(empty);
            list.add(emptyRow);
        } else {
            Collections.sort(lectures, Comparator.comparing(Schedule::getStartTime, Comparator.nullsLast(String::compareTo)));
            for (int i = 0; i < lectures.size(); i++) {
                list.add(createLectureRowItem(lectures.get(i), i == 0));
                if (i < lectures.size() - 1) list.add(Box.createVerticalStrut(8));
            }
        }
        list.add(Box.createVerticalGlue());

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setOpaque(false);

        JLabel badge = new JLabel("Day: " + LocalDate.now().getDayOfWeek());
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(COLOR_MAIN_BLUE);

        JButton viewTimetableBtn = createLinkButton("View Full Timetable →", e -> openTimetable());

        headerRight.add(badge);
        headerRight.add(viewTimetableBtn);

        return createSectionCard("Today's Lectures", headerRight, list);
    }

    private JPanel createLectureRowItem(Schedule s, boolean isUpcoming) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(isUpcoming ? new Color(0xF4, 0xF8, 0xFF) : COLOR_WHITE);
        row.setBorder(new CompoundBorder(
                new LineBorder(isUpcoming ? COLOR_MAIN_BLUE : COLOR_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));

        JPanel timeBox = new JPanel(new GridLayout(2, 1, 0, 2));
        timeBox.setOpaque(false);
        timeBox.setPreferredSize(new Dimension(122, 36));

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

        String lect = (s.getLecturerName() != null ? s.getLecturerName() : "Lecturer");
        String room = (s.getLocation() != null ? s.getLocation() : "TBA");
        JLabel desc = new JLabel(lect + "  •  " + room);
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
    // 5. RECENT NOTICES CARD
    // ==========================================
    private JPanel createRecentNoticesCard() {
        JButton viewAll = createLinkButton("View All Notices →", e -> openNotices());

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);

        List<Notice> notices = currentStudent.viewNotices();
        if (notices == null || notices.isEmpty()) {
            JPanel emptyRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
            emptyRow.setOpaque(false);
            JLabel empty = new JLabel("No notices published currently.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            empty.setForeground(COLOR_TEXT_MUTED);
            emptyRow.add(empty);
            list.add(emptyRow);
        } else {
            int count = Math.min(3, notices.size());
            for (int i = 0; i < count; i++) {
                list.add(createNoticeRowItem(notices.get(i)));
                if (i < count - 1) list.add(Box.createVerticalStrut(8));
            }
        }
        list.add(Box.createVerticalGlue());

        return createSectionCard("Recent Notices", viewAll, list);
    }

    private JPanel createNoticeRowItem(Notice n) {
        JPanel box = new JPanel(new BorderLayout(0, 4));
        box.setBackground(COLOR_WHITE);
        box.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(8, 12, 8, 12)));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));

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

        JLabel desc = new JLabel("<html><body style='width: 500px;'>" + n.getDescription() + "</body></html>");
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
    // 6. ACADEMIC ASSISTANCE CARD
    // ==========================================
    private JPanel createAcademicSupportCard() {
        JPanel card = new JPanel(new BorderLayout(20, 0));
        card.setBackground(COLOR_LIGHT_BLUE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xBF, 0xDB, 0xFE), 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JPanel left = new JPanel(new BorderLayout(0, 4));
        left.setOpaque(false);

        JLabel title = new JLabel("Academic Assistance & Student Helpdesk");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(COLOR_PRIMARY_NAVY);

        JLabel desc = new JLabel("For course registrations, timetable clashes, or official inquiries, contact the Dean's Office.");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        desc.setForeground(COLOR_TEXT_PRIMARY);

        left.add(title, BorderLayout.NORTH);
        left.add(desc, BorderLayout.SOUTH);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);

        JLabel email = new JLabel("Email: tech-dean@fot.ruh.ac.lk");
        email.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        email.setForeground(COLOR_TEXT_PRIMARY);

        JLabel desk = new JLabel("Office: Dean's Office, Admin Block B, Level 1");
        desk.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        desk.setForeground(COLOR_TEXT_PRIMARY);

        right.add(email);
        right.add(Box.createVerticalStrut(4));
        right.add(desk);

        card.add(left, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
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

    public void openProfile() {
        if (navigationPanel != null) {
            navigationPanel.highlightButton("PROFILE");
        }
        if (profilePanel != null) {
            profilePanel.showViewProfilePanel();
            profilePanel.loadProfileData();
        }
        if (contentCardLayout != null && mainContentArea != null) {
            contentCardLayout.show(mainContentArea, "PROFILE");
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
        if ("My Profile".equalsIgnoreCase(moduleName) || "Profile".equalsIgnoreCase(moduleName) || "PROFILE".equalsIgnoreCase(moduleName)) {
            openProfile();
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

    /**
     * Called when student profile is updated in My Profile.
     * Updates top header, overview dashboard, and sub-module panels.
     */
    public void notifyProfileUpdated() {
        // 1. Update Top-Right Header (Name and Avatar photo)
        if (headerNameLbl != null && currentStudent != null) {
            headerNameLbl.setText(currentStudent.getName() != null ? currentStudent.getName() : "Student");
        }
        if (headerAvatar != null && currentStudent != null) {
            headerAvatar.setIcon(renderAvatarIcon(40, currentStudent.getProfilePicture(), currentStudent.getName()));
        }

        // 2. Refresh Dashboard Overview panel
        if (mainContentArea != null && dashboardOverviewPanel != null) {
            mainContentArea.remove(dashboardOverviewPanel);
            dashboardOverviewPanel = createDashboardOverviewPanel();
            mainContentArea.add(dashboardOverviewPanel, "DASHBOARD");
        }

        // 3. Re-instantiate sub-panels with the updated student model
        refreshSubPanels();

        // 4. Ensure PROFILE remains the active view
        if (contentCardLayout != null && mainContentArea != null) {
            contentCardLayout.show(mainContentArea, "PROFILE");
        }

        revalidate();
        repaint();
    }

    public void refreshSubPanels() {
        if (mainContentArea == null) return;

        if (attendancePanel != null) {
            mainContentArea.remove(attendancePanel);
            attendancePanel = new UndergraduateAttendanceUI(currentStudent, this);
            mainContentArea.add(attendancePanel, "ATTENDANCE");
        }
        if (medicalPanel != null) {
            mainContentArea.remove(medicalPanel);
            medicalPanel = new UndergraduateMedicalUI(currentStudent, this);
            mainContentArea.add(medicalPanel, "MEDICAL");
        }
        if (courseDetailsPanel != null) {
            mainContentArea.remove(courseDetailsPanel);
            courseDetailsPanel = new UndergraduateCourseDetailsUI(currentStudent, this);
            mainContentArea.add(courseDetailsPanel, "COURSES");
        }
        if (lectureNotesPanel != null) {
            mainContentArea.remove(lectureNotesPanel);
            lectureNotesPanel = new UndergraduateLectureNotesUI(currentStudent, this);
            mainContentArea.add(lectureNotesPanel, "MATERIALS");
        }
        if (gradesPanel != null) {
            mainContentArea.remove(gradesPanel);
            gradesPanel = new UndergraduateGradesUI(currentStudent, this);
            mainContentArea.add(gradesPanel, "GRADES");
        }
        if (timetablePanel != null) {
            mainContentArea.remove(timetablePanel);
            timetablePanel = new UndergraduateTimetableUI(currentStudent, this);
            mainContentArea.add(timetablePanel, "TIMETABLE");
        }
        if (noticesPanel != null) {
            mainContentArea.remove(noticesPanel);
            noticesPanel = new UndergraduateNoticesUI(currentStudent, this);
            mainContentArea.add(noticesPanel, "NOTICES");
        }
    }

    private Icon renderAvatarIcon(int size, String imagePath, String name) {
        if (imagePath != null && !imagePath.trim().isEmpty() && !imagePath.equalsIgnoreCase("default_avatar.png")) {
            File f = new File(imagePath.trim());
            if (!f.isAbsolute()) {
                File rel = new File(System.getProperty("user.dir"), imagePath.trim());
                if (rel.exists() && rel.isFile()) {
                    f = rel;
                }
            }
            if (f.exists() && f.isFile()) {
                try {
                    ImageIcon icon = new ImageIcon(f.getAbsolutePath());
                    Image img = icon.getImage();
                    return createCircularAvatarIcon(img, size);
                } catch (Exception ignored) {}
            }
        }
        return createVectorInitialsIcon(size, name);
    }

    private Icon createCircularAvatarIcon(Image rawImg, int size) {
        BufferedImage circular = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = circular.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Circular clipping mask
        g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, size, size));
        g2.drawImage(rawImg, 0, 0, size, size, null);

        // Smooth circular border outline
        g2.setClip(null);
        g2.setColor(COLOR_BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(0, 0, size - 1, size - 1);
        g2.dispose();

        return new ImageIcon(circular);
    }

    private Icon createVectorInitialsIcon(int size, String name) {
        String initials = "ST";
        if (name != null && !name.trim().isEmpty()) {
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
            else if (parts[0].length() >= 2) initials = parts[0].substring(0, 2).toUpperCase();
        }
        final String text = initials;

        return new Icon() {
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
        };
    }

    private JLabel createAvatarIcon(int size, String name) {
        JLabel avatar = new JLabel();
        avatar.setPreferredSize(new Dimension(size, size));
        avatar.setIcon(renderAvatarIcon(size, currentStudent != null ? currentStudent.getProfilePicture() : null, name));
        return avatar;
    }

    private JLabel createAvatarIcon_old(int size, String name) {
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
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            UndergraduateDAO dao = new UndergraduateDAO();
            Undergraduate student = dao.getUndergraduateByStudentId("TG/2024/2083");
            if (student == null) {
                student = dao.getUndergraduateByStudentId("TG/2024/2085");
            }
            if (student == null) {
                student = new Undergraduate(
                    19, "TG/2024/2083", "", "K.A.B.V Smarathunga", "smarathunga@student.ruh.ac.lk", "0710001002",
                    "default_avatar.png", "19", "TG/2024/2083", "Batch 2023/2024",
                    "Department of Information and Communication Technology", 2024
                );
            }
            UndergraduateDashboard dashboard = new UndergraduateDashboard(student);
            dashboard.setVisible(true);
        });
    }

    // ==========================================
    // HELPER: Track Viewport Width Scrollable Panel
    // ==========================================
    private static class ScrollableTrackWidthPanel extends JPanel implements Scrollable {
        public ScrollableTrackWidthPanel() {
            super();
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 64;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
