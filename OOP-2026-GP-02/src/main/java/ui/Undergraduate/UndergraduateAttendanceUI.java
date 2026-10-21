package main.java.ui.Undergraduate;

import main.java.dao.Undergraduate.AttendanceDAO;
import main.java.model.Undergraduate.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * TecFAMS - Faculty of Technology Academic Management System
 * Undergraduate Attendance UI Page.
 *
 * Displays live, comprehensive attendance records for the logged-in student,
 * including overall attendance %, examination eligibility under the faculty 80% rule,
 * course-wise breakdown cards, interactive filtering (course, status, session type),
 * and an individual session-by-session log table with custom status pill badges.
 */
public class UndergraduateAttendanceUI extends JPanel {

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
    private static final Color COLOR_DANGER       = new Color(0xEF, 0x44, 0x44); // #EF4444
    private static final Color COLOR_WARNING      = new Color(0xF5, 0x9E, 0x0B); // #F59E0B
    private static final Color COLOR_PURPLE       = new Color(0x8B, 0x5C, 0xF6); // #8B5CF6

    // Data references
    private final Undergraduate loggedInStudent;
    private final UndergraduateDashboard parentDashboard;
    private final String studentIdentifier;
    private final AttendanceDAO attendanceDAO;

    // Data Models
    private List<Attendance> allAttendanceRecords = new ArrayList<>();
    private List<CourseAttendanceSummary> courseSummaries = new ArrayList<>();
    private AttendanceSummary overallSummary = new AttendanceSummary();

    // UI Panels and controls to update dynamically
    private JLabel statusSubtitleLabel;
    private JPanel summaryCardsRow;
    private JPanel courseCardsContainer;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private JComboBox<String> courseFilterCombo;
    private JComboBox<String> statusFilterCombo;
    private JComboBox<String> typeFilterCombo;
    private JTextField searchField;
    private JLabel recordCountLabel;

    /**
     * Main constructor for embedding inside UndergraduateDashboard.
     */
    public UndergraduateAttendanceUI(Undergraduate student, UndergraduateDashboard parentDashboard) {
        this.loggedInStudent = student;
        this.parentDashboard = parentDashboard;
        this.studentIdentifier = resolveStudentIdentifier(student);
        this.attendanceDAO = new AttendanceDAO();

        initializeUI();
        loadAttendanceData();
    }

    /**
     * Constructor for standalone or student-only view.
     */
    public UndergraduateAttendanceUI(Undergraduate student) {
        this(student, null);
    }

    /**
     * Constructor by Student Registration Number.
     */
    public UndergraduateAttendanceUI(String studentIdOrRegNo) {
        this.loggedInStudent = null;
        this.parentDashboard = null;
        this.studentIdentifier = (studentIdOrRegNo != null && !studentIdOrRegNo.trim().isEmpty())
                ? studentIdOrRegNo.trim()
                : "TG/2024/2085";
        this.attendanceDAO = new AttendanceDAO();

        initializeUI();
        loadAttendanceData();
    }

    /**
     * Default constructor for testing.
     */
    public UndergraduateAttendanceUI() {
        this("TG/2024/2085");
    }

    private String resolveStudentIdentifier(Undergraduate student) {
        if (student == null) {
            return "TG/2024/2085";
        }
        if (student.getRegistrationNo() != null && !student.getRegistrationNo().trim().isEmpty()) {
            return student.getRegistrationNo().trim();
        }
        if (student.getStudentId() != null && !student.getStudentId().trim().isEmpty()) {
            return student.getStudentId().trim();
        }
        return "TG/2024/2085";
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);

        // Scrollable content area
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(COLOR_BG);
        scrollContent.setBorder(new EmptyBorder(16, 24, 24, 24));

        // 1. Header Bar
        scrollContent.add(createHeaderPanel());
        scrollContent.add(Box.createVerticalStrut(16));

        // 2. Student Info Ribbon
        scrollContent.add(createStudentInfoRibbon());
        scrollContent.add(Box.createVerticalStrut(16));

        // 3. Top 4 Metric Overview Cards
        summaryCardsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        summaryCardsRow.setOpaque(false);
        summaryCardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));
        scrollContent.add(summaryCardsRow);
        scrollContent.add(Box.createVerticalStrut(20));

        // 4. Course-Wise Breakdown Cards
        scrollContent.add(createSectionTitle("Course-Wise Attendance Breakdown", "Course components & minimum 80% requirement"));
        scrollContent.add(Box.createVerticalStrut(10));
        courseCardsContainer = new JPanel();
        courseCardsContainer.setLayout(new BoxLayout(courseCardsContainer, BoxLayout.Y_AXIS));
        courseCardsContainer.setOpaque(false);
        scrollContent.add(courseCardsContainer);
        scrollContent.add(Box.createVerticalStrut(24));

        // 5. Attendance Session Log Section (Filter & Table)
        scrollContent.add(createSectionTitle("Attendance Session History Log", "Complete history of lectures and practicals held"));
        scrollContent.add(Box.createVerticalStrut(10));
        scrollContent.add(createFilterAndSearchPanel());
        scrollContent.add(Box.createVerticalStrut(10));
        scrollContent.add(createTableCard());
        scrollContent.add(Box.createVerticalStrut(20));

        // 6. Regulatory Notes Footer
        scrollContent.add(createPolicyNoticeCard());

        JScrollPane mainScrollPane = new JScrollPane(scrollContent);
        mainScrollPane.setBorder(null);
        mainScrollPane.setBackground(COLOR_BG);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScrollPane, BorderLayout.CENTER);
    }

    // ==========================================
    // 1. TOP HEADER PANEL
    // ==========================================
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel leftBox = new JPanel(new GridLayout(2, 1, 0, 4));
        leftBox.setOpaque(false);

        JLabel titleLbl = new JLabel("My Attendance & Exam Eligibility");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(COLOR_PRIMARY_NAVY);

        statusSubtitleLabel = new JLabel("Loading live attendance records from database...");
        statusSubtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusSubtitleLabel.setForeground(COLOR_TEXT_MUTED);

        leftBox.add(titleLbl);
        leftBox.add(statusSubtitleLabel);
        header.add(leftBox, BorderLayout.WEST);

        JButton backBtn = new JButton("← Back to Dashboard");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        backBtn.setForeground(COLOR_MAIN_BLUE);
        backBtn.setBackground(COLOR_WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        backBtn.addActionListener(e -> {
            if (parentDashboard != null) {
                parentDashboard.showDashboardView();
            } else {
                Window win = SwingUtilities.getWindowAncestor(UndergraduateAttendanceUI.this);
                if (win != null) {
                    win.dispose();
                }
            }
        });
        header.add(backBtn, BorderLayout.EAST);
        return header;
    }

    // ==========================================
    // 2. STUDENT INFO RIBBON
    // ==========================================
    private JPanel createStudentInfoRibbon() {
        JPanel ribbon = new JPanel(new BorderLayout());
        ribbon.setBackground(COLOR_WHITE);
        ribbon.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(12, 16, 12, 16)));
        ribbon.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        leftBox.setOpaque(false);

        String studentName = (loggedInStudent != null && loggedInStudent.getName() != null)
                ? loggedInStudent.getName() : "H.D.W.B. Hapuarachchi";
        String regNo = studentIdentifier;
        String dept = (loggedInStudent != null && loggedInStudent.getDepartment() != null)
                ? loggedInStudent.getDepartment() : "Department of Information and Communication Technology";
        String batch = (loggedInStudent != null && loggedInStudent.getBatch() != null)
                ? loggedInStudent.getBatch() : "Batch 2023/2024";

        JLabel avatar = createAvatarIcon(38, studentName);
        leftBox.add(avatar);

        JPanel details = new JPanel(new GridLayout(2, 1, 0, 2));
        details.setOpaque(false);
        JLabel nameLbl = new JLabel(studentName + "  (" + regNo + ")");
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLbl.setForeground(COLOR_TEXT_PRIMARY);

        JLabel metaLbl = new JLabel(dept + "  •  " + batch + "  •  Level II Semester I");
        metaLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        metaLbl.setForeground(COLOR_TEXT_MUTED);

        details.add(nameLbl);
        details.add(metaLbl);
        leftBox.add(details);

        ribbon.add(leftBox, BorderLayout.WEST);

        // Date pill badge on right
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"));
        JLabel dateBadge = new JLabel("Academic Session 2026  |  " + today);
        dateBadge.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateBadge.setForeground(COLOR_PRIMARY_NAVY);
        dateBadge.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(6, 12, 6, 12)));
        ribbon.add(dateBadge, BorderLayout.EAST);

        return ribbon;
    }

    // ==========================================
    // 3. METRIC OVERVIEW CARDS
    // ==========================================
    private void renderSummaryCards() {
        summaryCardsRow.removeAll();

        double pct = overallSummary.calculatePercentage();
        boolean eligible = overallSummary.isEligible();
        int total = overallSummary.getTotalSessions();
        int attended = overallSummary.getAttendedSessions();
        int medical = overallSummary.getMedicalSessions();
        int absent = Math.max(0, total - (attended + medical));

        // Card 1: Attendance Percentage
        Color pctColor = eligible ? COLOR_SUCCESS : (pct >= 70.0 ? COLOR_WARNING : COLOR_DANGER);
        JPanel card1 = createMetricCard(
                "OVERALL ATTENDANCE",
                String.format("%.1f%%", pct),
                pctColor,
                total > 0 ? (attended + medical) + " of " + total + " sessions" : "No sessions recorded",
                "Faculty Threshold: 80.0%"
        );

        // Card 2: Exam Eligibility
        String eligText = eligible ? "ELIGIBLE" : "NOT ELIGIBLE";
        Color eligColor = eligible ? COLOR_SUCCESS : COLOR_DANGER;
        String eligSub = eligible ? "Qualified for Semester Exams" : "Attendance below 80% requirement";
        JPanel card2 = createMetricCard(
                "EXAM ELIGIBILITY",
                eligText,
                eligColor,
                eligSub,
                eligible ? "Meets faculty criteria" : "Contact Academic Dept / TO"
        );

        // Card 3: Sessions Breakdown
        JPanel card3 = createMetricCard(
                "SESSION BREAKDOWN",
                attended + " Present",
                COLOR_PRIMARY_NAVY,
                absent + " Absent  •  " + medical + " Medical",
                total + " Total sessions scheduled"
        );

        // Card 4: Approved Medicals
        JPanel card4 = createMetricCard(
                "APPROVED MEDICALS",
                medical + " Sessions",
                COLOR_WARNING,
                medical > 0 ? "Approved by University Medical Officer" : "No medical records logged",
                "Counts toward 80% requirement"
        );

        summaryCardsRow.add(card1);
        summaryCardsRow.add(card2);
        summaryCardsRow.add(card3);
        summaryCardsRow.add(card4);

        summaryCardsRow.revalidate();
        summaryCardsRow.repaint();
    }

    private JPanel createMetricCard(String title, String mainValue, Color valColor, String sub1, String sub2) {
        JPanel card = new JPanel(new GridLayout(4, 1, 0, 3));
        card.setBackground(COLOR_WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(12, 16, 12, 16)));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 10));
        t.setForeground(COLOR_TEXT_MUTED);

        JLabel v = new JLabel(mainValue);
        v.setFont(new Font("Segoe UI", Font.BOLD, 18));
        v.setForeground(valColor);

        JLabel s1 = new JLabel(sub1);
        s1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        s1.setForeground(COLOR_TEXT_PRIMARY);

        JLabel s2 = new JLabel(sub2);
        s2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        s2.setForeground(COLOR_TEXT_MUTED);

        card.add(t);
        card.add(v);
        card.add(s1);
        card.add(s2);
        return card;
    }

    // ==========================================
    // 4. COURSE-WISE BREAKDOWN CARDS
    // ==========================================
    private void renderCourseCards() {
        courseCardsContainer.removeAll();

        if (courseSummaries.isEmpty()) {
            JPanel emptyCard = new JPanel(new BorderLayout());
            emptyCard.setBackground(COLOR_WHITE);
            emptyCard.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(20, 20, 20, 20)));
            JLabel emptyLbl = new JLabel("No registered course attendance records found for this student.", SwingConstants.CENTER);
            emptyLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            emptyLbl.setForeground(COLOR_TEXT_MUTED);
            emptyCard.add(emptyLbl, BorderLayout.CENTER);
            courseCardsContainer.add(emptyCard);
            courseCardsContainer.revalidate();
            courseCardsContainer.repaint();
            return;
        }

        // Layout in a grid: 2 cards per row
        int numCourses = courseSummaries.size();
        int rows = (int) Math.ceil(numCourses / 2.0);
        JPanel grid = new JPanel(new GridLayout(rows, 2, 14, 14));
        grid.setOpaque(false);

        for (CourseAttendanceSummary cs : courseSummaries) {
            grid.add(createCourseCard(cs));
        }

        // If odd number, add filler
        if (numCourses % 2 != 0) {
            JPanel filler = new JPanel();
            filler.setOpaque(false);
            grid.add(filler);
        }

        courseCardsContainer.add(grid);
        courseCardsContainer.revalidate();
        courseCardsContainer.repaint();
    }

    private JPanel createCourseCard(CourseAttendanceSummary cs) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(COLOR_WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(14, 18, 14, 18)));

        // Top Row: Course Code & Name + Eligibility Badge
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);
        JLabel codeLbl = new JLabel(cs.getCourseCode());
        codeLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        codeLbl.setForeground(COLOR_PRIMARY_NAVY);

        JLabel nameLbl = new JLabel(cs.getCourseName());
        nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nameLbl.setForeground(COLOR_TEXT_MUTED);

        titleBox.add(codeLbl);
        titleBox.add(nameLbl);
        topRow.add(titleBox, BorderLayout.WEST);

        // Badge Pill
        boolean eligible = cs.isEligible();
        double pct = cs.getPercentage();
        Color badgeBg = (cs.getTotalSessions() == 0) ? COLOR_LIGHT_BLUE : (eligible ? new Color(0xD1, 0xFA, 0xE5) : new Color(0xFE, 0xE2, 0xE2));
        Color badgeFg = (cs.getTotalSessions() == 0) ? COLOR_MAIN_BLUE : (eligible ? new Color(0x06, 0x5F, 0x46) : new Color(0x99, 0x1B, 0x1B));
        String badgeText = (cs.getTotalSessions() == 0) ? "No Sessions" : (eligible ? "Eligible" : "At Risk (<80%)");

        JLabel badge = new JLabel("  " + badgeText + "  ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setBackground(badgeBg);
        badge.setForeground(badgeFg);
        badge.setOpaque(true);
        badge.setBorder(new LineBorder(badgeBg, 4, true));
        topRow.add(badge, BorderLayout.EAST);

        card.add(topRow, BorderLayout.NORTH);

        // Middle Row: Custom Progress Bar & Percentage
        JPanel midRow = new JPanel(new BorderLayout(12, 0));
        midRow.setOpaque(false);

        Color barColor = eligible ? COLOR_SUCCESS : (pct >= 70.0 ? COLOR_WARNING : COLOR_DANGER);
        JPanel progressBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                // Background track
                g2.setColor(new Color(0xEE, 0xF2, 0xF6));
                g2.fillRoundRect(0, (h - 8) / 2, w, 8, 8, 8);
                // Fill
                int fillW = (int) Math.round((Math.min(pct, 100.0) / 100.0) * w);
                if (fillW > 0) {
                    g2.setColor(barColor);
                    g2.fillRoundRect(0, (h - 8) / 2, fillW, 8, 8, 8);
                }
                g2.dispose();
            }
        };
        progressBar.setPreferredSize(new Dimension(150, 16));
        progressBar.setOpaque(false);
        midRow.add(progressBar, BorderLayout.CENTER);

        JLabel pctLbl = new JLabel(String.format("%.1f%%", pct));
        pctLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pctLbl.setForeground(barColor);
        midRow.add(pctLbl, BorderLayout.EAST);

        card.add(midRow, BorderLayout.CENTER);

        // Bottom Row: Detailed counts (Total, Present, Absent, Medical)
        JPanel statRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        statRow.setOpaque(false);
        statRow.add(createMiniStat("Total", String.valueOf(cs.getTotalSessions()), COLOR_TEXT_PRIMARY));
        statRow.add(createMiniStat("Present", String.valueOf(cs.getAttendedSessions()), COLOR_SUCCESS));
        statRow.add(createMiniStat("Absent", String.valueOf(cs.getAbsentSessions()), COLOR_DANGER));
        statRow.add(createMiniStat("Medical", String.valueOf(cs.getMedicalSessions()), COLOR_WARNING));

        card.add(statRow, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createMiniStat(String label, String value, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(label + ":");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(COLOR_TEXT_MUTED);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 11));
        v.setForeground(color);

        p.add(l);
        p.add(v);
        return p;
    }

    // ==========================================
    // 5. FILTER & SEARCH CONTROLS
    // ==========================================
    private JPanel createFilterAndSearchPanel() {
        JPanel filterCard = new JPanel(new BorderLayout());
        filterCard.setBackground(COLOR_WHITE);
        filterCard.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(10, 16, 10, 16)));
        filterCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftFilters.setOpaque(false);

        // Course filter
        leftFilters.add(new JLabel("Course:"));
        courseFilterCombo = new JComboBox<>();
        courseFilterCombo.addItem("All Courses");
        courseFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        courseFilterCombo.setPreferredSize(new Dimension(170, 32));
        courseFilterCombo.addActionListener(e -> applyTableFilters());
        leftFilters.add(courseFilterCombo);

        // Status filter
        leftFilters.add(new JLabel("Status:"));
        statusFilterCombo = new JComboBox<>(new String[]{"All Statuses", "Present Only", "Absent Only", "Medical Only"});
        statusFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusFilterCombo.setPreferredSize(new Dimension(130, 32));
        statusFilterCombo.addActionListener(e -> applyTableFilters());
        leftFilters.add(statusFilterCombo);

        // Type filter
        leftFilters.add(new JLabel("Type:"));
        typeFilterCombo = new JComboBox<>(new String[]{"All Types", "Theory", "Practical"});
        typeFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        typeFilterCombo.setPreferredSize(new Dimension(110, 32));
        typeFilterCombo.addActionListener(e -> applyTableFilters());
        leftFilters.add(typeFilterCombo);

        filterCard.add(leftFilters, BorderLayout.WEST);

        // Search Field on Right
        JPanel rightSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightSearch.setOpaque(false);

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setPreferredSize(new Dimension(180, 32));
        searchField.putClientProperty("JTextField.placeholderText", "Search date or session...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyTableFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { applyTableFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyTableFilters(); }
        });
        rightSearch.add(new JLabel("Search:"));
        rightSearch.add(searchField);

        JButton resetBtn = new JButton("Reset");
        resetBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        resetBtn.setBackground(COLOR_WHITE);
        resetBtn.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(6, 12, 6, 12)));
        resetBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetBtn.addActionListener(e -> {
            courseFilterCombo.setSelectedIndex(0);
            statusFilterCombo.setSelectedIndex(0);
            typeFilterCombo.setSelectedIndex(0);
            searchField.setText("");
            applyTableFilters();
        });
        rightSearch.add(resetBtn);

        filterCard.add(rightSearch, BorderLayout.EAST);
        return filterCard;
    }

    // ==========================================
    // 6. DETAILED SESSION LOG TABLE
    // ==========================================
    private JPanel createTableCard() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(COLOR_WHITE);
        tablePanel.setBorder(new LineBorder(COLOR_BORDER, 1, true));

        // Subheader inside table card
        JPanel tableSubHeader = new JPanel(new BorderLayout());
        tableSubHeader.setBackground(COLOR_WHITE);
        tableSubHeader.setBorder(new EmptyBorder(12, 16, 10, 16));

        JLabel title = new JLabel("Official Attendance Registry");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(COLOR_PRIMARY_NAVY);

        recordCountLabel = new JLabel("0 records loaded");
        recordCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        recordCountLabel.setForeground(COLOR_TEXT_MUTED);

        tableSubHeader.add(title, BorderLayout.WEST);
        tableSubHeader.add(recordCountLabel, BorderLayout.EAST);
        tablePanel.add(tableSubHeader, BorderLayout.NORTH);

        // Columns: Session #, Course Code, Course Name, Date, Session Type, Status, Remarks
        String[] columnNames = {"Session #", "Course Code", "Course Name", "Date", "Session Type", "Status", "Remarks / Eligibility"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only table
            }
        };

        attendanceTable = new JTable(tableModel);
        attendanceTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        attendanceTable.setRowHeight(40);
        attendanceTable.setGridColor(COLOR_BORDER);
        attendanceTable.setShowVerticalLines(false);
        attendanceTable.setSelectionBackground(COLOR_LIGHT_BLUE);
        attendanceTable.setSelectionForeground(COLOR_TEXT_PRIMARY);

        // Header Styling
        JTableHeader th = attendanceTable.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(new Color(0xFB, 0xFC, 0xFE));
        th.setForeground(COLOR_PRIMARY_NAVY);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        th.setPreferredSize(new Dimension(0, 36));

        // Column widths
        attendanceTable.getColumnModel().getColumn(0).setPreferredWidth(85);  // Session #
        attendanceTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Course Code
        attendanceTable.getColumnModel().getColumn(2).setPreferredWidth(260); // Course Name
        attendanceTable.getColumnModel().getColumn(3).setPreferredWidth(110); // Date
        attendanceTable.getColumnModel().getColumn(4).setPreferredWidth(110); // Session Type
        attendanceTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Status
        attendanceTable.getColumnModel().getColumn(6).setPreferredWidth(180); // Remarks

        // Custom Cell Renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        attendanceTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        attendanceTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        attendanceTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        // Session Type Pill Renderer
        attendanceTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String type = (value != null) ? value.toString() : "THEORY";
                if (type.equalsIgnoreCase("PRACTICAL")) {
                    lbl.setForeground(COLOR_PURPLE);
                } else {
                    lbl.setForeground(COLOR_MAIN_BLUE);
                }
                return lbl;
            }
        });

        // Status Pill Renderer (Present / Absent / Medical)
        attendanceTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
                panel.setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? COLOR_WHITE : new Color(0xF9, 0xFB, 0xFD)));

                String status = (value != null) ? value.toString() : "PRESENT";
                Color bg;
                Color fg;
                String dot = "● ";

                if (status.equalsIgnoreCase("PRESENT")) {
                    bg = new Color(0xD1, 0xFA, 0xE5);
                    fg = new Color(0x06, 0x5F, 0x46);
                } else if (status.equalsIgnoreCase("ABSENT")) {
                    bg = new Color(0xFE, 0xE2, 0xE2);
                    fg = new Color(0x99, 0x1B, 0x1B);
                } else {
                    bg = new Color(0xFE, 0xF3, 0xC7);
                    fg = new Color(0x92, 0x40, 0x0E);
                }

                JLabel pill = new JLabel("  " + dot + status + "  ");
                pill.setFont(new Font("Segoe UI", Font.BOLD, 11));
                pill.setOpaque(true);
                pill.setBackground(bg);
                pill.setForeground(fg);
                pill.setBorder(new LineBorder(bg, 4, true));
                panel.add(pill);
                return panel;
            }
        });

        // Alternating row background for general cells
        attendanceTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? COLOR_WHITE : new Color(0xF9, 0xFB, 0xFD));
                    c.setForeground(COLOR_TEXT_PRIMARY);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        });

        tableSorter = new TableRowSorter<>(tableModel);
        attendanceTable.setRowSorter(tableSorter);

        JScrollPane tableScroll = new JScrollPane(attendanceTable);
        tableScroll.setBorder(null);
        tableScroll.getViewport().setBackground(COLOR_WHITE);
        tableScroll.setPreferredSize(new Dimension(0, 360));

        tablePanel.add(tableScroll, BorderLayout.CENTER);
        return tablePanel;
    }

    // ==========================================
    // 7. POLICY NOTICE CARD
    // ==========================================
    private JPanel createPolicyNoticeCard() {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(new Color(0xEE, 0xF4, 0xFF));
        card.setBorder(new CompoundBorder(new LineBorder(new Color(0xC7, 0xDC, 0xFD), 1, true), new EmptyBorder(14, 18, 14, 18)));

        JLabel iconLbl = new JLabel("ℹ");
        iconLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        iconLbl.setForeground(COLOR_MAIN_BLUE);
        card.add(iconLbl, BorderLayout.WEST);

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 3));
        text.setOpaque(false);

        JLabel h = new JLabel("Faculty of Technology Attendance Regulation (80% Rule)");
        h.setFont(new Font("Segoe UI", Font.BOLD, 12));
        h.setForeground(COLOR_PRIMARY_NAVY);

        JLabel b = new JLabel("<html>As per Faculty regulations, students are required to maintain a minimum of <b>80% attendance</b> in both Theory and Practical sessions to sit for semester examinations. Valid medical certificates approved by the University Medical Officer (UMO) are counted towards the attendance percentage.</html>");
        b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        b.setForeground(COLOR_TEXT_PRIMARY);

        text.add(h);
        text.add(b);
        card.add(text, BorderLayout.CENTER);

        return card;
    }

    private JPanel createSectionTitle(String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 15));
        t.setForeground(COLOR_PRIMARY_NAVY);

        JLabel s = new JLabel(subtitle);
        s.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        s.setForeground(COLOR_TEXT_MUTED);

        p.add(t, BorderLayout.WEST);
        p.add(s, BorderLayout.EAST);
        return p;
    }

    // ==========================================
    // 8. DATA LOADING & FILTERING
    // ==========================================
    public void loadAttendanceData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            List<Attendance> recs;
            List<CourseAttendanceSummary> sums;
            AttendanceSummary overall;

            @Override
            protected Void doInBackground() {
                recs = attendanceDAO.getAttendanceRecords(studentIdentifier);
                sums = attendanceDAO.getCourseAttendanceSummaries(studentIdentifier);
                overall = attendanceDAO.getOverallAttendanceSummary(studentIdentifier);
                return null;
            }

            @Override
            protected void done() {
                try {
                    allAttendanceRecords = (recs != null) ? recs : new ArrayList<>();
                    courseSummaries = (sums != null) ? sums : new ArrayList<>();
                    overallSummary = (overall != null) ? overall : new AttendanceSummary(15, 15, 0);

                    // Update Course Filter dropdown items
                    if (courseFilterCombo != null) {
                        courseFilterCombo.removeAllItems();
                        courseFilterCombo.addItem("All Courses");
                        for (CourseAttendanceSummary c : courseSummaries) {
                            courseFilterCombo.addItem(c.getCourseCode());
                        }
                    }

                    // Render Overview cards
                    renderSummaryCards();

                    // Render Course breakdown cards
                    renderCourseCards();

                    // Populate table
                    populateTable(allAttendanceRecords);

                    // Update subtitle status
                    if (statusSubtitleLabel != null) {
                        statusSubtitleLabel.setText("Showing attendance for " + studentIdentifier + "  •  " +
                                allAttendanceRecords.size() + " total sessions logged");
                    }
                } catch (Exception e) {
                    System.err.println("Notice: Could not finish attendance UI update: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void populateTable(List<Attendance> records) {
        if (tableModel == null) return;
        tableModel.setRowCount(0);

        for (Attendance a : records) {
            String remarks;
            if (a.getStatus() == AttendanceStatus.PRESENT) {
                remarks = "Counted towards 80% requirement";
            } else if (a.getStatus() == AttendanceStatus.MEDICAL) {
                remarks = "Approved Medical (Excused)";
            } else {
                remarks = "Unexcused absence";
            }

            tableModel.addRow(new Object[]{
                    "Session " + String.format("%02d", a.getSessionCount()),
                    a.getCourseCode(),
                    a.getCourseName(),
                    a.getDate(),
                    a.getSessionType().name(),
                    a.getStatus().name(),
                    remarks
            });
        }

        if (recordCountLabel != null) {
            recordCountLabel.setText("Showing " + records.size() + " of " + allAttendanceRecords.size() + " sessions");
        }
    }

    private void applyTableFilters() {
        if (tableSorter == null) return;

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // 1. Course Filter
        if (courseFilterCombo != null && courseFilterCombo.getSelectedIndex() > 0) {
            String selectedCourse = (String) courseFilterCombo.getSelectedItem();
            if (selectedCourse != null) {
                filters.add(RowFilter.regexFilter("(?i)" + selectedCourse, 1));
            }
        }

        // 2. Status Filter
        if (statusFilterCombo != null && statusFilterCombo.getSelectedIndex() > 0) {
            String status = (String) statusFilterCombo.getSelectedItem();
            if ("Present Only".equals(status)) {
                filters.add(RowFilter.regexFilter("(?i)PRESENT", 5));
            } else if ("Absent Only".equals(status)) {
                filters.add(RowFilter.regexFilter("(?i)ABSENT", 5));
            } else if ("Medical Only".equals(status)) {
                filters.add(RowFilter.regexFilter("(?i)MEDICAL", 5));
            }
        }

        // 3. Type Filter
        if (typeFilterCombo != null && typeFilterCombo.getSelectedIndex() > 0) {
            String type = (String) typeFilterCombo.getSelectedItem();
            filters.add(RowFilter.regexFilter("(?i)" + type, 4));
        }

        // 4. Search Filter
        if (searchField != null) {
            String text = searchField.getText().trim();
            if (!text.isEmpty()) {
                filters.add(RowFilter.regexFilter("(?i)" + text));
            }
        }

        if (filters.isEmpty()) {
            tableSorter.setRowFilter(null);
        } else {
            tableSorter.setRowFilter(RowFilter.andFilter(filters));
        }

        if (recordCountLabel != null && attendanceTable != null) {
            recordCountLabel.setText("Showing " + attendanceTable.getRowCount() + " of " + allAttendanceRecords.size() + " sessions");
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

    // ==========================================
    // STANDALONE LAUNCHER / PREVIEW
    // ==========================================
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            JFrame frame = new JFrame("TecFAMS - Undergraduate Attendance");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1240, 800);
            frame.setMinimumSize(new Dimension(1000, 680));
            frame.setLocationRelativeTo(null);

            Undergraduate sampleStudent = new Undergraduate(
                    "USR-1085", "tg2085", "", "H.D.W.B. Hapuarachchi",
                    "tg2085@fot.ruh.ac.lk", "+94 77 123 4567", "",
                    "STU-2085", "TG/2024/2085", "2023/2024",
                    "Department of Information and Communication Technology", 2024
            );

            UndergraduateAttendanceUI page = new UndergraduateAttendanceUI(sampleStudent, null);
            frame.setContentPane(page);
            frame.setVisible(true);
        });
    }
}
