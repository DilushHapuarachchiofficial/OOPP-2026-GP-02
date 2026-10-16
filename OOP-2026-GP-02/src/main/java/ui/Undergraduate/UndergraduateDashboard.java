package main.java.ui.Undergraduate;

import main.java.dao.Undergraduate.UndergraduateDAO;
import main.java.model.Undergraduate.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
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

        // Header, Left Navigation Sidebar & Center Dashboard Overview
        rootPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        
        rootPanel.add(createDashboardOverviewPanel(), BorderLayout.CENTER);

        setContentPane(rootPanel);
    }

    private void loadDatabaseData() {
        if (currentStudent == null || currentStudent.getStudentId() == null) return;
        try {
            List<Notice> dbNotices = dao.getRecentNotices(6);
            if (dbNotices != null && !dbNotices.isEmpty()) currentStudent.setNotices(dbNotices);

            List<Schedule> dbLectures = dao.getTodayLectures(
                    currentStudent.getStudentId(), currentStudent.getDepartment(), currentStudent.getBatch());
            if (dbLectures != null && !dbLectures.isEmpty()) {
                Timetable tt = currentStudent.viewTimetable();
                if (tt == null) tt = new Timetable(1, "Semester I", "2026");
                tt.setSchedules(dbLectures);
                currentStudent.setTimetable(tt);
            }

            GPA dbGpa = dao.getStudentGPA(currentStudent.getStudentId());
            if (dbGpa != null) currentStudent.setGpa(dbGpa);

            AttendanceSummary dbAtt = dao.getAttendanceSummary(currentStudent.getStudentId());
            if (dbAtt != null) currentStudent.setAttendanceSummary(dbAtt);

            List<CourseMaterial> dbMat = dao.getCourseMaterialsForStudent(currentStudent.getStudentId());
            if (dbMat != null && !dbMat.isEmpty() && currentStudent.viewCourses() != null && !currentStudent.viewCourses().isEmpty()) {
                currentStudent.viewCourses().get(0).setMaterials(dbMat);
            }
        } catch (Exception ignored) {}
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

        JPanel nameBox = new JPanel(new GridLayout(2, 1, 0, 2));
        nameBox.setOpaque(false);
        JLabel nameLbl = new JLabel(currentStudent.getName() != null ? currentStudent.getName() : "Student");
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLbl.setForeground(COLOR_TEXT_PRIMARY);
        JLabel roleLbl = new JLabel("Undergraduate");
        roleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        roleLbl.setForeground(COLOR_MAIN_BLUE);
        nameBox.add(nameLbl);
        nameBox.add(roleLbl);

        rightProfile.add(dateBadge);
        rightProfile.add(nameBox);
        rightProfile.add(createAvatarIcon(36, currentStudent.getName()));

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

        // 4 Summary Metric Cards
        JPanel cardsRow = createSummaryCardsRow();
        cardsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(cardsRow);
        content.add(Box.createVerticalStrut(20));

        // Quick Access Shortcuts
        JPanel quickAccess = createQuickAccessRow();
        quickAccess.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(quickAccess);
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
