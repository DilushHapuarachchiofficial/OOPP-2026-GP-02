package main.java.ui.Undergraduate;

import main.java.dao.Undergraduate.GradesDAO;
import main.java.model.Undergraduate.CourseGradeItem;
import main.java.model.Undergraduate.StudentGPASummary;
import main.java.model.Undergraduate.Undergraduate;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * TecFAMS - Faculty of Technology Academic Management System
 * Undergraduate Grades & GPA UI Page.
 *
 * Displays student course units, letter grades (A+, A, etc.), and grade points (GP).
 * Includes an interactive "View Marks ➔" button for every subject that opens a clean modal popup
 * showing the full evaluation breakdown: Quizzes, Assignments, Mid Exam, CA Total (40%), and End Exam (60%).
 *
 * Defaults to Level I - Semester II (L1.S2) with a simple dropdown to switch between semesters.
 * Built with clean, beginner-friendly Java Swing code.
 */
public class UndergraduateGradesUI extends JPanel {

    // TecFAMS Primary Theme Palette
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

    // Data references
    private final Undergraduate loggedInStudent;
    private final UndergraduateDashboard parentDashboard;
    private final String studentIdentifier;
    private final GradesDAO gradesDAO;

    // Filter state (Defaults to Level II - Semester I: Year 2, Sem 1)
    private Integer selectedYear = 2;
    private Integer selectedSem = 1;

    // Loaded Data Models
    private List<CourseGradeItem> courseGradesList = new ArrayList<>();
    private StudentGPASummary cumulativeSummary = new StudentGPASummary(); // Overall CGPA
    private StudentGPASummary semesterSummary = new StudentGPASummary();   // Selected Year/Semester SGPA

    // UI Dynamic Components
    private JLabel studentSubtitleLabel;
    private JPanel summaryCardsRow;

    // Semester GPA Banner Components (Displayed directly above Course Units)
    private JLabel semesterGpaTitleLabel;
    private JLabel semesterGpaValueLabel;
    private JLabel semesterGpaBadgeLabel;
    private JLabel semesterGpaDetailsLabel;

    // Table controls
    private JComboBox<String> periodFilterCombo;
    private JTable gradesTable;
    private DefaultTableModel gradesTableModel;
    private TableRowSorter<DefaultTableModel> gradesTableSorter;
    private JLabel tableTallyLabel;

    /**
     * Main constructor for embedding inside UndergraduateDashboard.
     */
    public UndergraduateGradesUI(Undergraduate student, UndergraduateDashboard parentDashboard) {
        this.loggedInStudent = student;
        this.parentDashboard = parentDashboard;
        this.studentIdentifier = resolveStudentIdentifier(student);
        this.gradesDAO = new GradesDAO();

        // Default to Level 2, Semester 1 (L2.S1)
        this.selectedYear = 2;
        this.selectedSem = 1;

        initializeUI();
        loadGradesData();
    }

    /**
     * Constructor for standalone or student-only view.
     */
    public UndergraduateGradesUI(Undergraduate student) {
        this(student, null);
    }

    /**
     * Constructor by Student Registration Number.
     */
    public UndergraduateGradesUI(String studentIdOrRegNo) {
        this.loggedInStudent = null;
        this.parentDashboard = null;
        this.studentIdentifier = (studentIdOrRegNo != null && !studentIdOrRegNo.trim().isEmpty())
                ? studentIdOrRegNo.trim()
                : "TG/2024/2085";
        this.gradesDAO = new GradesDAO();

        // Default to Level 2, Semester 1 (L2.S1)
        this.selectedYear = 2;
        this.selectedSem = 1;

        initializeUI();
        loadGradesData();
    }

    /**
     * Default constructor for testing.
     */
    public UndergraduateGradesUI() {
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

    /**
     * Initializes the entire UI structure and layout.
     */
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);

        // Scrollable content wrapper
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(COLOR_BG);
        contentPanel.setBorder(new EmptyBorder(18, 24, 24, 24));

        // 1. Top Header Bar (With Back to Dashboard only, Refresh & Transcript removed)
        contentPanel.add(createHeaderBar());
        contentPanel.add(Box.createVerticalStrut(16));

        // 2. Summary Metric Cards Row (GPA, Credits, Courses, Status)
        summaryCardsRow = createSummaryCardsRow();
        contentPanel.add(summaryCardsRow);
        contentPanel.add(Box.createVerticalStrut(18));

        // 3. Tabbed Pane: Course Grades and UGC Reference Guide
        JTabbedPane tabbedPane = createStyledTabbedPane();
        contentPanel.add(tabbedPane);

        // Put everything inside a smooth scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(COLOR_BG);
        add(scrollPane, BorderLayout.CENTER);
    }

    // ==========================================
    // 1. TOP HEADER SECTION
    // ==========================================
    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        // Left title box
        JPanel leftBox = new JPanel(new GridLayout(2, 1, 0, 4));
        leftBox.setOpaque(false);

        JLabel titleLbl = new JLabel("Grades & Academic Performance");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(COLOR_PRIMARY_NAVY);

        studentSubtitleLabel = new JLabel("Loading student examination results and GPA...");
        studentSubtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        studentSubtitleLabel.setForeground(COLOR_TEXT_MUTED);

        leftBox.add(titleLbl);
        leftBox.add(studentSubtitleLabel);
        header.add(leftBox, BorderLayout.WEST);

        // Right actions box (Back to Dashboard button only)
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBox.setOpaque(false);

        if (parentDashboard != null) {
            JButton backBtn = createActionButton("← Back to Dashboard", COLOR_LIGHT_BLUE, COLOR_MAIN_BLUE);
            backBtn.addActionListener(e -> parentDashboard.showDashboardView());
            rightBox.add(backBtn);
        }

        header.add(rightBox, BorderLayout.EAST);
        return header;
    }

    // ==========================================
    // 2. SUMMARY METRIC CARDS ROW (GPA & CREDITS)
    // ==========================================
    private JPanel createSummaryCardsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));
        row.setPreferredSize(new Dimension(0, 125));

        return row;
    }

    private void updateSummaryCards() {
        summaryCardsRow.removeAll();

        // 1. Overall Cumulative GPA Card (CGPA) - Always shows the student's total CGPA
        String cgpaValue = cumulativeSummary.getFormattedCGPA();
        String classTitle = cumulativeSummary.getDegreeClassification();
        Color cgpaColor = (cumulativeSummary.getCgpa() >= 3.70) ? new Color(0x7C, 0x3A, 0xED) : COLOR_MAIN_BLUE;

        summaryCardsRow.add(createMetricCard(
                "CUMULATIVE GPA (CGPA)",
                cgpaValue,
                classTitle,
                "Overall Academic Performance",
                cgpaColor
        ));

        // 2. Evaluated Academic Credits Card (Total degree progress)
        int compCred = cumulativeSummary.getTotalCompletedCredits();
        int regCred = cumulativeSummary.getTotalRegisteredCredits();
        String credRatio = compCred + " / " + regCred + " Credits";
        int pct = (regCred > 0) ? (compCred * 100 / regCred) : 0;
        summaryCardsRow.add(createMetricCard(
                "COMPLETED CREDITS",
                credRatio,
                pct + "% Completed",
                compCred + " total credits earned",
                COLOR_SUCCESS
        ));

        // 3. Evaluated Courses Card (Total degree courses)
        int evalCourses = cumulativeSummary.getEvaluatedCourses();
        int regCourses = cumulativeSummary.getTotalRegisteredCourses();
        summaryCardsRow.add(createMetricCard(
                "EVALUATED COURSES",
                evalCourses + " / " + regCourses + " Courses",
                "Results Published",
                "Overall course units calculated",
                COLOR_MAIN_BLUE
        ));

        // 4. Academic Standing Card (Overall academic standing)
        String standing = cumulativeSummary.getStandingStatus();
        summaryCardsRow.add(createMetricCard(
                "ACADEMIC STATUS",
                standing,
                "Good Standing",
                "Minimum GPA requirement satisfied",
                COLOR_PRIMARY_NAVY
        ));

        summaryCardsRow.revalidate();
        summaryCardsRow.repaint();
    }

    private JPanel createMetricCard(String title, String mainValue, String badgeText, String subText, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth(), 4, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        // Top Row: Title + Badge
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLbl.setForeground(COLOR_TEXT_MUTED);
        topRow.add(titleLbl, BorderLayout.WEST);

        JLabel badgeLbl = new JLabel(" " + badgeText + " ");
        badgeLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badgeLbl.setForeground(accentColor);
        badgeLbl.setOpaque(true);
        badgeLbl.setBackground(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 28));
        badgeLbl.setBorder(new EmptyBorder(2, 6, 2, 6));
        topRow.add(badgeLbl, BorderLayout.EAST);
        card.add(topRow, BorderLayout.NORTH);

        // Center: Big Main Value
        JLabel valLbl = new JLabel(mainValue);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLbl.setForeground(COLOR_TEXT_PRIMARY);
        card.add(valLbl, BorderLayout.CENTER);

        // Bottom: Subtext
        JLabel subLbl = new JLabel(subText);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLbl.setForeground(COLOR_TEXT_MUTED);
        card.add(subLbl, BorderLayout.SOUTH);

        return card;
    }

    // ==========================================
    // 3. TABBED SECTION (COURSES & SCALE)
    // ==========================================
    private JTabbedPane createStyledTabbedPane() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setBackground(COLOR_WHITE);

        // Tab 1: Clean Course Grades Table
        tabs.addTab("Course Unit Results & Grades", createCourseGradesTabPanel());

        // Tab 2: UGC Grading Scale Reference
        tabs.addTab("UGC Grading Scale Guide", createGradingReferenceTabPanel());

        return tabs;
    }

    // ------------------------------------------
    // TAB 1: CLEAN COURSE GRADES PANEL
    // ------------------------------------------
    private JPanel createCourseGradesTabPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(COLOR_WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        // Top Area: Holds Semester Selector toolbar AND SGPA Banner above Course Units Table
        JPanel topArea = new JPanel();
        topArea.setLayout(new BoxLayout(topArea, BoxLayout.Y_AXIS));
        topArea.setOpaque(false);

        // 1. Semester Selector Bar (Search and Status filters removed as requested)
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilters.setOpaque(false);

        JLabel periodLbl = new JLabel("Select Semester:");
        periodLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        periodLbl.setForeground(COLOR_PRIMARY_NAVY);
        leftFilters.add(periodLbl);

        String[] periods = {
                "Level II - Semester I (L2.S1)",
                "Level I - Semester II (L1.S2)",
                "Level I - Semester I (L1.S1)",
                "All Semesters (Cumulative CGPA)"
        };
        periodFilterCombo = new JComboBox<>(periods);
        periodFilterCombo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        periodFilterCombo.setPreferredSize(new Dimension(240, 32));
        periodFilterCombo.setSelectedIndex(0); // Level II - Semester I (L2.S1) by default
        periodFilterCombo.addActionListener(e -> onPeriodFilterChanged());
        leftFilters.add(periodFilterCombo);

        toolbar.add(leftFilters, BorderLayout.WEST);

        tableTallyLabel = new JLabel("Showing courses");
        tableTallyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableTallyLabel.setForeground(COLOR_TEXT_MUTED);
        toolbar.add(tableTallyLabel, BorderLayout.EAST);

        topArea.add(toolbar);
        topArea.add(Box.createVerticalStrut(12));

        // 2. SGPA Banner: Displays selected semester GPA (SGPA) directly above the course units table
        topArea.add(createSemesterGpaBanner());

        panel.add(topArea, BorderLayout.NORTH);

        // Clean, Uncluttered Course Grades Table (Only 8 Essential Columns)
        String[] columns = {
                "#", "Course Unit", "Course Title", "Credits", "Grade", "GP", "Status", "Action"
        };
        gradesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        gradesTable = new JTable(gradesTableModel);
        gradesTable.setRowHeight(40);
        gradesTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gradesTable.setSelectionBackground(COLOR_LIGHT_BLUE);
        gradesTable.setSelectionForeground(COLOR_TEXT_PRIMARY);
        gradesTable.setShowVerticalLines(false);
        gradesTable.setGridColor(new Color(0xF1, 0xF5, 0xF9));

        gradesTableSorter = new TableRowSorter<>(gradesTableModel);
        gradesTable.setRowSorter(gradesTableSorter);

        JTableHeader header = gradesTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(0xFA, 0xFA, 0xFC));
        header.setForeground(COLOR_PRIMARY_NAVY);
        header.setPreferredSize(new Dimension(0, 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

        // Column widths
        gradesTable.getColumnModel().getColumn(0).setPreferredWidth(45);   // #
        gradesTable.getColumnModel().getColumn(1).setPreferredWidth(120);  // Code
        gradesTable.getColumnModel().getColumn(2).setPreferredWidth(380);  // Title
        gradesTable.getColumnModel().getColumn(3).setPreferredWidth(70);   // Credits
        gradesTable.getColumnModel().getColumn(4).setPreferredWidth(95);   // Grade
        gradesTable.getColumnModel().getColumn(5).setPreferredWidth(80);   // GP
        gradesTable.getColumnModel().getColumn(6).setPreferredWidth(120);  // Status
        gradesTable.getColumnModel().getColumn(7).setPreferredWidth(140);  // Action Button

        // Cell Renderers
        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);

        gradesTable.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        gradesTable.getColumnModel().getColumn(1).setCellRenderer(new CourseCodeCellRenderer());
        gradesTable.getColumnModel().getColumn(3).setCellRenderer(centerRender);
        gradesTable.getColumnModel().getColumn(4).setCellRenderer(new GradeBadgeCellRenderer());
        gradesTable.getColumnModel().getColumn(5).setCellRenderer(centerRender);
        gradesTable.getColumnModel().getColumn(6).setCellRenderer(new StatusBadgeCellRenderer());
        gradesTable.getColumnModel().getColumn(7).setCellRenderer(new ActionButtonRenderer());

        // Mouse motion listener to show hand cursor on hover over the action button
        gradesTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = gradesTable.columnAtPoint(e.getPoint());
                if (col == 7) {
                    gradesTable.setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    gradesTable.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        // Mouse click listener to open popup modal on button click or row double-click
        gradesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int viewRow = gradesTable.rowAtPoint(e.getPoint());
                if (viewRow < 0) return;
                int viewCol = gradesTable.columnAtPoint(e.getPoint());

                // Click on Action button (col 7) or double click any row
                if (viewCol == 7 || e.getClickCount() == 2) {
                    triggerCourseBreakdown(viewRow);
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(gradesTable);
        tableScroll.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        tableScroll.getViewport().setBackground(COLOR_WHITE);
        panel.add(tableScroll, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates a clean, modern summary banner showing the selected year/semester GPA (SGPA)
     * right above the Course Units table.
     */
    private JPanel createSemesterGpaBanner() {
        JPanel banner = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xF8, 0xFA, 0xFC));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(COLOR_MAIN_BLUE);
                g2.fillRoundRect(0, 0, 5, getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        banner.setOpaque(false);
        banner.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));

        // Left Box: Title + SGPA Value + Degree Honours Badge
        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftBox.setOpaque(false);

        semesterGpaTitleLabel = new JLabel("Semester GPA (SGPA):");
        semesterGpaTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        semesterGpaTitleLabel.setForeground(COLOR_TEXT_PRIMARY);

        semesterGpaValueLabel = new JLabel("4.00");
        semesterGpaValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        semesterGpaValueLabel.setForeground(COLOR_MAIN_BLUE);

        semesterGpaBadgeLabel = new JLabel(" First Class Honours ");
        semesterGpaBadgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        semesterGpaBadgeLabel.setOpaque(true);
        semesterGpaBadgeLabel.setBackground(new Color(0xDC, 0xFC, 0xE7));
        semesterGpaBadgeLabel.setForeground(new Color(0x16, 0x65, 0x34));
        semesterGpaBadgeLabel.setBorder(new EmptyBorder(3, 8, 3, 8));

        leftBox.add(semesterGpaTitleLabel);
        leftBox.add(semesterGpaValueLabel);
        leftBox.add(semesterGpaBadgeLabel);

        // Right Box: Credits & Evaluated Courses for this selected semester
        semesterGpaDetailsLabel = new JLabel("Year 2 Semester 1  •  Credits: 11 / 18  •  Evaluated: 5 / 9 Courses");
        semesterGpaDetailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        semesterGpaDetailsLabel.setForeground(COLOR_TEXT_MUTED);

        banner.add(leftBox, BorderLayout.WEST);
        banner.add(semesterGpaDetailsLabel, BorderLayout.EAST);

        return banner;
    }

    private void updateSemesterGpaBanner() {
        boolean isAll = (selectedYear == null && selectedSem == null);
        if (isAll) {
            semesterGpaTitleLabel.setText("Cumulative GPA (CGPA):");
            semesterGpaValueLabel.setText(semesterSummary.getFormattedCGPA());
            semesterGpaValueLabel.setForeground(new Color(0x7C, 0x3A, 0xED));
        } else {
            semesterGpaTitleLabel.setText("Semester GPA (SGPA):");
            semesterGpaValueLabel.setText(semesterSummary.getFormattedSGPA());
            semesterGpaValueLabel.setForeground(COLOR_MAIN_BLUE);
        }

        double val = isAll ? semesterSummary.getCgpa() : semesterSummary.getSgpa();
        String honours = StudentGPASummary.determineClassification(val);
        semesterGpaBadgeLabel.setText(" " + honours + " ");

        if (val >= 3.70) {
            semesterGpaBadgeLabel.setBackground(new Color(0xDC, 0xFC, 0xE7));
            semesterGpaBadgeLabel.setForeground(new Color(0x16, 0x65, 0x34));
        } else if (val >= 3.30) {
            semesterGpaBadgeLabel.setBackground(new Color(0xDB, 0xEA, 0xFE));
            semesterGpaBadgeLabel.setForeground(new Color(0x1E, 0x40, 0xAF));
        } else if (val >= 3.00) {
            semesterGpaBadgeLabel.setBackground(new Color(0xFE, 0xF3, 0xC7));
            semesterGpaBadgeLabel.setForeground(new Color(0x92, 0x40, 0x0E));
        } else {
            semesterGpaBadgeLabel.setBackground(new Color(0xF1, 0xF5, 0xF9));
            semesterGpaBadgeLabel.setForeground(COLOR_TEXT_PRIMARY);
        }

        int compCred = semesterSummary.getTotalCompletedCredits();
        int regCred = semesterSummary.getTotalRegisteredCredits();
        int evalCourses = semesterSummary.getEvaluatedCourses();
        int regCourses = semesterSummary.getTotalRegisteredCourses();

        semesterGpaDetailsLabel.setText(
                semesterSummary.getPeriodLabel() + "  •  Completed Credits: " + compCred + " / " + regCred +
                "  •  Evaluated: " + evalCourses + " / " + regCourses + " Courses"
        );
    }

    private void onPeriodFilterChanged() {
        int idx = periodFilterCombo.getSelectedIndex();
        switch (idx) {
            case 0: // Level II - Semester I (L2.S1)
                selectedYear = 2;
                selectedSem = 1;
                break;
            case 1: // Level I - Semester II (L1.S2)
                selectedYear = 1;
                selectedSem = 2;
                break;
            case 2: // Level I - Semester I (L1.S1)
                selectedYear = 1;
                selectedSem = 1;
                break;
            case 3: // All Semesters (Cumulative CGPA)
            default:
                selectedYear = null;
                selectedSem = null;
                break;
        }
        loadGradesData();
    }

    private void triggerCourseBreakdown(int viewRow) {
        if (viewRow < 0 || viewRow >= gradesTable.getRowCount()) return;
        String courseCode = (String) gradesTable.getValueAt(viewRow, 1);
        CourseGradeItem targetItem = null;
        for (CourseGradeItem c : courseGradesList) {
            if (c.getCourseCode() != null && c.getCourseCode().equalsIgnoreCase(courseCode)) {
                targetItem = c;
                break;
            }
        }
        if (targetItem != null) {
            showCourseBreakdownDialog(targetItem);
        }
    }

    // ==========================================
    // 4. POPUP MODAL: COURSE MARKS BREAKDOWN
    // ==========================================
    /**
     * Opens a dedicated modal panel showing the complete evaluation breakdown
     * for the selected course unit: Quizzes, Assignments, Mid Exam, CA Total (40%),
     * End Semester Exam (60%), Overall Mark, and Final Grade.
     * Includes a permanently visible, custom-painted Close button at the bottom.
     */
    private void showCourseBreakdownDialog(CourseGradeItem item) {
        if (item == null) return;

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Evaluation Breakdown: " + item.getCourseCode(), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(600, 620);
        dialog.setMinimumSize(new Dimension(540, 520));
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // CENTER: Scrollable content with all the evaluation cards
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(COLOR_BG);
        content.setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. Course Header Card
        JPanel courseHeader = new JPanel(new BorderLayout(10, 4));
        courseHeader.setBackground(COLOR_WHITE);
        courseHeader.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel leftHeader = new JPanel(new GridLayout(2, 1, 0, 2));
        leftHeader.setOpaque(false);
        JLabel codeLbl = new JLabel(item.getCourseCode() + " - " + item.getCourseName());
        codeLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        codeLbl.setForeground(COLOR_PRIMARY_NAVY);
        JLabel semLbl = new JLabel(item.getSemesterName() + "  •  " + item.getCredit() + " Academic Credits");
        semLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        semLbl.setForeground(COLOR_TEXT_MUTED);
        leftHeader.add(codeLbl);
        leftHeader.add(semLbl);
        courseHeader.add(leftHeader, BorderLayout.CENTER);

        // Grade badge on top right
        JLabel gradeBadge = new JLabel(" Grade: " + item.getGrade() + " ");
        gradeBadge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gradeBadge.setOpaque(true);
        gradeBadge.setBackground(new Color(0xDC, 0xFC, 0xE7));
        gradeBadge.setForeground(new Color(0x16, 0x65, 0x34));
        gradeBadge.setBorder(new EmptyBorder(4, 10, 4, 10));
        courseHeader.add(gradeBadge, BorderLayout.EAST);

        content.add(courseHeader);
        content.add(Box.createVerticalStrut(14));

        // 2. Continuous Assessment (CA) Breakdown Box (40%)
        JPanel caBox = new JPanel();
        caBox.setLayout(new BoxLayout(caBox, BoxLayout.Y_AXIS));
        caBox.setBackground(COLOR_WHITE);
        caBox.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel caTitle = new JLabel("Continuous Assessment (CA) - 40% Weightage");
        caTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        caTitle.setForeground(COLOR_PRIMARY_NAVY);
        caBox.add(caTitle);
        caBox.add(Box.createVerticalStrut(10));

        caBox.add(createDetailRow("1. Quizzes (10% Weight):",
                item.getFormattedQuiz() + " / 100",
                String.format("%.2f / 10.0%%", item.getQuizContribution())));
        caBox.add(Box.createVerticalStrut(6));

        caBox.add(createDetailRow("2. Assignments / Mini Project (15% Weight):",
                item.getFormattedAssignment() + " / 100",
                String.format("%.2f / 15.0%%", item.getAssignmentContribution())));
        caBox.add(Box.createVerticalStrut(6));

        caBox.add(createDetailRow("3. Mid-Semester Examination (15% Weight):",
                item.getFormattedMidExam() + " / 100",
                String.format("%.2f / 15.0%%", item.getMidExamContribution())));
        caBox.add(Box.createVerticalStrut(8));

        // CA Total row with highlight
        JPanel caTotalRow = new JPanel(new BorderLayout());
        caTotalRow.setOpaque(true);
        caTotalRow.setBackground(new Color(0xFA, 0xE8, 0xFF));
        caTotalRow.setBorder(new EmptyBorder(6, 10, 6, 10));
        JLabel caTotLbl = new JLabel("Total Continuous Assessment (CA) Score:");
        caTotLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        caTotLbl.setForeground(new Color(0x86, 0x19, 0x8F));
        JLabel caTotVal = new JLabel(item.getFormattedCATotal() + " / 40.00");
        caTotVal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        caTotVal.setForeground(new Color(0x86, 0x19, 0x8F));
        caTotalRow.add(caTotLbl, BorderLayout.WEST);
        caTotalRow.add(caTotVal, BorderLayout.EAST);
        caBox.add(caTotalRow);

        content.add(caBox);
        content.add(Box.createVerticalStrut(12));

        // 3. Final / End Semester Exam Box (60%)
        JPanel endExamBox = new JPanel();
        endExamBox.setLayout(new BoxLayout(endExamBox, BoxLayout.Y_AXIS));
        endExamBox.setBackground(COLOR_WHITE);
        endExamBox.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel endTitle = new JLabel("End-Semester Final Examination - 60% Weightage");
        endTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        endTitle.setForeground(COLOR_PRIMARY_NAVY);
        endExamBox.add(endTitle);
        endExamBox.add(Box.createVerticalStrut(10));

        endExamBox.add(createDetailRow("Final Theory & Practical Examination:",
                item.getFormattedFinalExam() + " / 100",
                String.format("%.2f / 60.0%%", item.getFinalExamContribution())));
        endExamBox.add(Box.createVerticalStrut(8));

        // End exam total highlight
        JPanel endTotalRow = new JPanel(new BorderLayout());
        endTotalRow.setOpaque(true);
        endTotalRow.setBackground(new Color(0xEE, 0xF2, 0xFF));
        endTotalRow.setBorder(new EmptyBorder(6, 10, 6, 10));
        JLabel endTotLbl = new JLabel("Final Exam Weighted Contribution:");
        endTotLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        endTotLbl.setForeground(COLOR_PRIMARY_NAVY);
        JLabel endTotVal = new JLabel(String.format("%.2f / 60.00", item.getFinalExamContribution()));
        endTotVal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        endTotVal.setForeground(COLOR_PRIMARY_NAVY);
        endTotalRow.add(endTotLbl, BorderLayout.WEST);
        endTotalRow.add(endTotVal, BorderLayout.EAST);
        endExamBox.add(endTotalRow);

        content.add(endExamBox);
        content.add(Box.createVerticalStrut(12));

        // 4. Overall Final Mark & Result Summary Card
        JPanel finalCard = new JPanel(new GridLayout(1, 3, 10, 0));
        finalCard.setOpaque(false);

        finalCard.add(createMiniSummaryBox("Overall Final Mark", item.getFormattedMark(), COLOR_PRIMARY_NAVY));
        finalCard.add(createMiniSummaryBox("UGC Letter Grade", item.getGrade() + " (GP: " + item.getFormattedGradePoint() + ")", COLOR_MAIN_BLUE));
        finalCard.add(createMiniSummaryBox("Status", item.getStatus(), COLOR_SUCCESS));

        content.add(finalCard);
        content.add(Box.createVerticalStrut(10));

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        dialog.add(scrollPane, BorderLayout.CENTER);

        // SOUTH: Dedicated, Permanently Visible Bottom Bar with Custom-Painted Close Button
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 12));
        bottomBar.setBackground(COLOR_WHITE);
        bottomBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));

        JButton closeBtn = new JButton("Close Details") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(COLOR_PRIMARY_NAVY.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(COLOR_MAIN_BLUE);
                } else {
                    g2.setColor(COLOR_PRIMARY_NAVY);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        closeBtn.setForeground(COLOR_WHITE);
        closeBtn.setPreferredSize(new Dimension(150, 38));
        closeBtn.setContentAreaFilled(false);
        closeBtn.setOpaque(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dialog.dispose());
        bottomBar.add(closeBtn);

        dialog.add(bottomBar, BorderLayout.SOUTH);

        // Allow ESC key to close dialog
        dialog.getRootPane().registerKeyboardAction(
                e -> dialog.dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        dialog.setVisible(true);
    }

    private JPanel createDetailRow(String label, String score, String contribution) {
        JPanel r = new JPanel(new BorderLayout());
        r.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(COLOR_TEXT_PRIMARY);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JLabel s = new JLabel("Score: " + score);
        s.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        s.setForeground(COLOR_TEXT_MUTED);

        JLabel c = new JLabel("Contrib: " + contribution);
        c.setFont(new Font("Segoe UI", Font.BOLD, 12));
        c.setForeground(COLOR_MAIN_BLUE);

        right.add(s);
        right.add(c);

        r.add(l, BorderLayout.WEST);
        r.add(right, BorderLayout.EAST);
        return r;
    }

    private JPanel createMiniSummaryBox(String title, String val, Color color) {
        JPanel b = new JPanel(new GridLayout(2, 1, 0, 2));
        b.setBackground(COLOR_WHITE);
        b.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        t.setForeground(COLOR_TEXT_MUTED);
        JLabel v = new JLabel(val);
        v.setFont(new Font("Segoe UI", Font.BOLD, 13));
        v.setForeground(color);
        b.add(t);
        b.add(v);
        return b;
    }

    // ------------------------------------------
    // TAB 2: UGC GRADING SCALE REFERENCE
    // ------------------------------------------
    private JPanel createGradingReferenceTabPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(COLOR_WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(20, 24, 20, 24)
        ));

        // Top info header
        JPanel infoHeader = new JPanel(new GridLayout(2, 1, 0, 4));
        infoHeader.setOpaque(false);
        JLabel refTitle = new JLabel("University Grants Commission (UGC) Grading Criteria & Assessment Guide");
        refTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        refTitle.setForeground(COLOR_PRIMARY_NAVY);
        JLabel refSub = new JLabel("Official Circular No. 12-2024 Criteria for Faculty of Technology Degrees (BICT / BST / ET)");
        refSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refSub.setForeground(COLOR_TEXT_MUTED);
        infoHeader.add(refTitle);
        infoHeader.add(refSub);
        panel.add(infoHeader, BorderLayout.NORTH);

        // Center split: Left Table (Grades), Right Info (Formula & Degree Class)
        JPanel splitGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        splitGrid.setOpaque(false);

        // Left: Grading Scheme Table
        String[] refCols = {"Marks Range (%)", "Letter Grade", "Grade Point", "Performance Level"};
        Object[][] refData = {
                {"85 - 100", "A+", "4.00", "Outstanding"},
                {"80 - 84",  "A",  "4.00", "Excellent"},
                {"75 - 79",  "A-", "3.70", "Very Good"},
                {"70 - 74",  "B+", "3.30", "Good"},
                {"65 - 69",  "B",  "3.00", "Satisfactory"},
                {"60 - 64",  "B-", "2.70", "Above Average"},
                {"55 - 59",  "C+", "2.30", "Average"},
                {"50 - 54",  "C",  "2.00", "Pass / Minimum Requirement"},
                {"45 - 49",  "C-", "1.70", "Conditional Pass / Repeat"},
                {"40 - 44",  "D+", "1.30", "Poor / Repeat"},
                {"35 - 39",  "D",  "1.00", "Very Poor / Repeat"},
                {"00 - 34",  "E",  "0.00", "Fail / Mandatory Repeat"}
        };
        JTable refTable = new JTable(refData, refCols);
        refTable.setRowHeight(28);
        refTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refTable.setShowVerticalLines(false);
        refTable.setGridColor(new Color(0xF1, 0xF5, 0xF9));

        JTableHeader refH = refTable.getTableHeader();
        refH.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refH.setBackground(new Color(0xFA, 0xFA, 0xFC));
        refH.setForeground(COLOR_PRIMARY_NAVY);
        refH.setPreferredSize(new Dimension(0, 32));

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        refTable.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        refTable.getColumnModel().getColumn(1).setCellRenderer(new GradeBadgeCellRenderer());
        refTable.getColumnModel().getColumn(2).setCellRenderer(centerRender);

        JScrollPane refScroll = new JScrollPane(refTable);
        refScroll.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        splitGrid.add(refScroll);

        // Right: GPA Calculation Formula & Degree Honours
        JPanel formulaPanel = new JPanel();
        formulaPanel.setLayout(new BoxLayout(formulaPanel, BoxLayout.Y_AXIS));
        formulaPanel.setOpaque(false);
        formulaPanel.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel fTitle = new JLabel("GPA Calculation Formula:");
        fTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        fTitle.setForeground(COLOR_PRIMARY_NAVY);
        formulaPanel.add(fTitle);
        formulaPanel.add(Box.createVerticalStrut(6));

        JLabel fFormula = new JLabel("<html><b>GPA = &Sigma; (Credit &times; Grade Point) / &Sigma; Credit</b></html>");
        fFormula.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fFormula.setForeground(COLOR_MAIN_BLUE);
        formulaPanel.add(fFormula);
        formulaPanel.add(Box.createVerticalStrut(14));

        JLabel cTitle = new JLabel("Degree Honours Classification:");
        cTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cTitle.setForeground(COLOR_PRIMARY_NAVY);
        formulaPanel.add(cTitle);
        formulaPanel.add(Box.createVerticalStrut(8));

        formulaPanel.add(createHonourRow("First Class Honours:", "CGPA ≥ 3.70", new Color(0x7C, 0x3A, 0xED)));
        formulaPanel.add(Box.createVerticalStrut(6));
        formulaPanel.add(createHonourRow("Second Class (Upper Division):", "CGPA ≥ 3.30", COLOR_MAIN_BLUE));
        formulaPanel.add(Box.createVerticalStrut(6));
        formulaPanel.add(createHonourRow("Second Class (Lower Division):", "CGPA ≥ 3.00", COLOR_SUCCESS));
        formulaPanel.add(Box.createVerticalStrut(6));
        formulaPanel.add(createHonourRow("General Degree Pass:", "CGPA ≥ 2.00", COLOR_TEXT_PRIMARY));
        formulaPanel.add(Box.createVerticalStrut(14));

        JLabel examRuleLbl = new JLabel("<html><b>Continuous Assessment (CA) Structure:</b><br>" +
                "• <b>Quizzes:</b> 10% weightage.<br>" +
                "• <b>Assignments / Projects:</b> 15% weightage.<br>" +
                "• <b>Mid-Semester Exam:</b> 15% weightage.<br>" +
                "• <b>End-Semester Examination:</b> 60% weightage.<br>" +
                "• <b>Minimum 80% attendance</b> is required for final examination eligibility.</html>");
        examRuleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        examRuleLbl.setForeground(COLOR_TEXT_MUTED);
        formulaPanel.add(examRuleLbl);

        splitGrid.add(formulaPanel);
        panel.add(splitGrid, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createHonourRow(String label, String value, Color color) {
        JPanel r = new JPanel(new BorderLayout());
        r.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(COLOR_TEXT_PRIMARY);
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 12));
        v.setForeground(color);
        r.add(l, BorderLayout.WEST);
        r.add(v, BorderLayout.EAST);
        return r;
    }

    // ==========================================
    // 5. DATA LOADING FROM DATABASE
    // ==========================================
    public void loadGradesData() {
        try {
            // 1. Fetch Course Grades for selected period
            courseGradesList = gradesDAO.getStudentCourseGrades(studentIdentifier, selectedYear, selectedSem);

            // 2. Fetch Semester GPA Summary for selected period (SGPA)
            semesterSummary = gradesDAO.getStudentGPASummary(studentIdentifier, selectedYear, selectedSem);

            // 3. Fetch Overall Cumulative GPA Summary (CGPA)
            cumulativeSummary = gradesDAO.getStudentGPASummary(studentIdentifier, null, null);

            // 4. Update Header Subtitle
            String studentName = (loggedInStudent != null && loggedInStudent.getName() != null)
                    ? loggedInStudent.getName() : "H.D.W.B Hapuarachchi";
            String regNo = studentIdentifier;
            studentSubtitleLabel.setText("Student: " + studentName + "  •  Reg No: " + regNo +
                    "  •  Batch: 2023/2024  •  Department of ICT  •  " + semesterSummary.getPeriodLabel());

            // 5. Update KPI Cards (Top 4 cards always show Cumulative GPA & degree progress)
            updateSummaryCards();

            // 6. Update Semester GPA Banner above Course Units Table
            updateSemesterGpaBanner();

            // 7. Populate Course Table
            populateCourseGradesTable();

        } catch (Exception e) {
            System.err.println("Database Notice: Error loading grades data: " + e.getMessage());
        }
    }

    private void populateCourseGradesTable() {
        gradesTableModel.setRowCount(0);
        int index = 1;
        for (CourseGradeItem item : courseGradesList) {
            gradesTableModel.addRow(new Object[]{
                    index++,
                    item.getCourseCode(),
                    item.getCourseName(),
                    item.getCredit(),
                    item.getGrade(),
                    item.getFormattedGradePoint(),
                    item.getStatus(),
                    "View Marks >"
            });
        }
        tableTallyLabel.setText("Showing " + courseGradesList.size() + " of " + courseGradesList.size() + " courses (" + semesterSummary.getPeriodLabel() + ")");
    }

    // ==========================================
    // 7. CUSTOM TABLE CELL RENDERERS
    // ==========================================
    private static class CourseCodeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(COLOR_MAIN_BLUE);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            return lbl;
        }
    }

    private static class GradeBadgeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

            String grade = (value != null) ? value.toString() : "-";
            JLabel badge = new JLabel(" " + grade + " ");
            badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
            badge.setOpaque(true);

            if (grade.startsWith("A")) {
                badge.setBackground(new Color(0xDC, 0xFC, 0xE7)); // Soft Emerald
                badge.setForeground(new Color(0x16, 0x65, 0x34));
            } else if (grade.startsWith("B")) {
                badge.setBackground(new Color(0xDB, 0xEA, 0xFE)); // Soft Blue
                badge.setForeground(new Color(0x1E, 0x40, 0xAF));
            } else if (grade.startsWith("C")) {
                badge.setBackground(new Color(0xFE, 0xF3, 0xC7)); // Soft Amber
                badge.setForeground(new Color(0x92, 0x40, 0x0E));
            } else if (grade.startsWith("D") || grade.startsWith("E")) {
                badge.setBackground(new Color(0xFE, 0xE2, 0xE2)); // Soft Red
                badge.setForeground(new Color(0x99, 0x1B, 0x1B));
            } else {
                badge.setBackground(new Color(0xF1, 0xF5, 0xF9)); // Neutral Gray
                badge.setForeground(new Color(0x47, 0x55, 0x69));
            }
            badge.setBorder(new EmptyBorder(3, 8, 3, 8));

            panel.add(badge);
            return panel;
        }
    }

    private static class StatusBadgeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

            String status = (value != null) ? value.toString() : "";
            JLabel badge = new JLabel(" " + status + " ");
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setOpaque(true);

            if ("Completed".equalsIgnoreCase(status) || "Passed".equalsIgnoreCase(status)) {
                badge.setBackground(new Color(0xDC, 0xFC, 0xE7));
                badge.setForeground(new Color(0x16, 0x65, 0x34));
            } else if ("In Progress".equalsIgnoreCase(status) || "Pending".equalsIgnoreCase(status)) {
                badge.setBackground(new Color(0xEA, 0xF2, 0xFF));
                badge.setForeground(COLOR_MAIN_BLUE);
            } else {
                badge.setBackground(new Color(0xFE, 0xE2, 0xE2));
                badge.setForeground(COLOR_DANGER);
            }
            badge.setBorder(new EmptyBorder(3, 8, 3, 8));

            panel.add(badge);
            return panel;
        }
    }

    /**
     * Custom TableCellRenderer that renders a crisp, high-visibility rounded Action button.
     */
    private static class ActionButtonRenderer implements TableCellRenderer {
        private final JPanel panel = new JPanel(new GridBagLayout());
        private final JLabel btnLabel = new JLabel("View Marks >") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_MAIN_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        public ActionButtonRenderer() {
            panel.setOpaque(true);
            btnLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btnLabel.setForeground(COLOR_WHITE);
            btnLabel.setHorizontalAlignment(SwingConstants.CENTER);
            btnLabel.setBorder(new EmptyBorder(6, 14, 6, 14));
            panel.add(btnLabel);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return panel;
        }
    }

    private JButton createActionButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.darker());
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ==========================================
    // 8. STANDALONE MAIN METHOD
    // ==========================================
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            JFrame frame = new JFrame("TecFAMS - Grades & GPA Academic Record");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 780);
            frame.setLocationRelativeTo(null);

            UndergraduateGradesUI gradesUI = new UndergraduateGradesUI("TG/2024/2085");
            frame.setContentPane(gradesUI);
            frame.setVisible(true);
        });
    }
}
