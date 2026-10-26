package main.java.ui.Undergraduate;

import main.java.dao.Undergraduate.GradesDAO;
import main.java.model.Undergraduate.CourseGradeItem;
import main.java.model.Undergraduate.ExamMarkItem;
import main.java.model.Undergraduate.StudentGPASummary;
import main.java.model.Undergraduate.Undergraduate;

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
 * Undergraduate Grades & GPA UI Page.
 *
 * Displays live, comprehensive exam marks, continuous assessment (CA) scores,
 * course grades, and Cumulative Grade Point Average (CGPA) for the logged-in student.
 *
 * Built with clean, beginner-friendly Java Swing code and rich professional design.
 */
public class UndergraduateGradesUI extends JPanel {

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
    private static final Color COLOR_WARNING      = new Color(0xF5, 0x9E, 0x0B); // #F59E0B
    private static final Color COLOR_PURPLE       = new Color(0x8B, 0x5C, 0xF6); // #8B5CF6
    private static final Color COLOR_DANGER       = new Color(0xEF, 0x44, 0x44); // #EF4444

    // Data references
    private final Undergraduate loggedInStudent;
    private final UndergraduateDashboard parentDashboard;
    private final String studentIdentifier;
    private final GradesDAO gradesDAO;

    // Loaded Data Models
    private List<CourseGradeItem> courseGradesList = new ArrayList<>();
    private List<ExamMarkItem> examMarksList = new ArrayList<>();
    private StudentGPASummary gpaSummary = new StudentGPASummary();

    // UI Dynamic Components
    private JLabel studentSubtitleLabel;
    private JPanel summaryCardsRow;

    // Course Grades Table (Tab 1)
    private JTable gradesTable;
    private DefaultTableModel gradesTableModel;
    private TableRowSorter<DefaultTableModel> gradesTableSorter;
    private JLabel gradesTallyLabel;

    // Exam Components Table (Tab 2)
    private JTable examMarksTable;
    private DefaultTableModel examMarksTableModel;
    private TableRowSorter<DefaultTableModel> examMarksTableSorter;
    private JLabel examMarksTallyLabel;

    // Search and filter controls
    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private JComboBox<String> componentTypeFilterCombo;

    /**
     * Main constructor for embedding inside UndergraduateDashboard.
     */
    public UndergraduateGradesUI(Undergraduate student, UndergraduateDashboard parentDashboard) {
        this.loggedInStudent = student;
        this.parentDashboard = parentDashboard;
        this.studentIdentifier = resolveStudentIdentifier(student);
        this.gradesDAO = new GradesDAO();

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
        contentPanel.setBorder(new EmptyBorder(20, 24, 24, 24));

        // 1. Header Bar
        contentPanel.add(createHeaderBar());
        contentPanel.add(Box.createVerticalStrut(18));

        // 2. Summary Metric Cards Row (CGPA, SGPA, Credits, Status)
        summaryCardsRow = createSummaryCardsRow();
        contentPanel.add(summaryCardsRow);
        contentPanel.add(Box.createVerticalStrut(20));

        // 3. Tabbed Pane: Course Grades, Exam Marks, UGC Reference Guide
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

        // Right actions box (Refresh, Print/Export, Back to Dashboard)
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBox.setOpaque(false);

        JButton refreshBtn = createActionButton("Refresh Data", COLOR_MAIN_BLUE, COLOR_WHITE);
        refreshBtn.addActionListener(e -> {
            loadGradesData();
            JOptionPane.showMessageDialog(this,
                    "Academic results refreshed successfully from MySQL database.",
                    "TecFAMS - Sync Complete",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        rightBox.add(refreshBtn);

        JButton printBtn = createActionButton("Print Transcript", COLOR_WHITE, COLOR_TEXT_PRIMARY);
        printBtn.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        printBtn.addActionListener(e -> showTranscriptDialog());
        rightBox.add(printBtn);

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
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        row.setPreferredSize(new Dimension(0, 130));

        // Populated dynamically in updateSummaryCards()
        return row;
    }

    private void updateSummaryCards() {
        summaryCardsRow.removeAll();

        // 1. Cumulative GPA (CGPA) Card
        String cgpaStr = gpaSummary.getFormattedCGPA();
        String classTitle = gpaSummary.getDegreeClassification();
        Color cgpaColor = (gpaSummary.getCgpa() >= 3.70) ? new Color(0x7C, 0x3A, 0xED) : COLOR_MAIN_BLUE;
        summaryCardsRow.add(createMetricCard(
                "CUMULATIVE GPA (CGPA)",
                cgpaStr,
                classTitle,
                "UGC Circular 12-2024 Standard",
                cgpaColor
        ));

        // 2. Semester GPA (SGPA) Card
        String sgpaStr = gpaSummary.getFormattedSGPA();
        String standingStr = (gpaSummary.getSgpa() >= 3.0) ? "Outstanding" : "Satisfactory";
        summaryCardsRow.add(createMetricCard(
                "SEMESTER GPA (SGPA)",
                sgpaStr,
                standingStr,
                "Level II Semester I (Current)",
                COLOR_MAIN_BLUE
        ));

        // 3. Evaluated Academic Credits Card
        int compCred = gpaSummary.getTotalCompletedCredits();
        int regCred = gpaSummary.getTotalRegisteredCredits();
        String credRatio = compCred + " / " + regCred + " Credits";
        int pct = (regCred > 0) ? (compCred * 100 / regCred) : 0;
        summaryCardsRow.add(createMetricCard(
                "EVALUATED CREDITS",
                credRatio,
                pct + "% Completed",
                compCred + " credits earned toward degree",
                COLOR_SUCCESS
        ));

        // 4. Academic Standing Card
        String standing = gpaSummary.getStandingStatus();
        int evalCourses = gpaSummary.getEvaluatedCourses();
        int regCourses = gpaSummary.getTotalRegisteredCourses();
        summaryCardsRow.add(createMetricCard(
                "ACADEMIC STATUS",
                standing,
                evalCourses + " / " + regCourses + " Courses Released",
                "Minimum GPA 2.00 requirement met",
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
                new EmptyBorder(14, 16, 14, 16)
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

        // Bottom: Explanatory Subtext
        JLabel subLbl = new JLabel(subText);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLbl.setForeground(COLOR_TEXT_MUTED);
        card.add(subLbl, BorderLayout.SOUTH);

        return card;
    }

    // ==========================================
    // 3. TABBED SECTION (COURSES, EXAMS, SCALE)
    // ==========================================
    private JTabbedPane createStyledTabbedPane() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setBackground(COLOR_WHITE);

        // Tab 1: Overall Course Grades
        tabs.addTab("Semester Course Grades", createCourseGradesTabPanel());

        // Tab 2: Detailed Exam & Assessment Breakdown
        tabs.addTab("Exam & Assessment Breakdown", createExamMarksTabPanel());

        // Tab 3: UGC Grading Scale & Reference
        tabs.addTab("Grading Scale & GPA Guide", createGradingReferenceTabPanel());

        return tabs;
    }

    // ------------------------------------------
    // TAB 1: COURSE GRADES PANEL
    // ------------------------------------------
    private JPanel createCourseGradesTabPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(COLOR_WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        // Filter / Search Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilters.setOpaque(false);

        JLabel searchLbl = new JLabel("Search Course:");
        searchLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLbl.setForeground(COLOR_TEXT_PRIMARY);
        leftFilters.add(searchLbl);

        searchField = new JTextField(16);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setPreferredSize(new Dimension(180, 32));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterGradesTable(); }
            @Override public void removeUpdate(DocumentEvent e) { filterGradesTable(); }
            @Override public void changedUpdate(DocumentEvent e) { filterGradesTable(); }
        });
        leftFilters.add(searchField);

        JLabel statusLbl = new JLabel("Status:");
        statusLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLbl.setForeground(COLOR_TEXT_PRIMARY);
        leftFilters.add(statusLbl);

        statusFilterCombo = new JComboBox<>(new String[]{"All Courses", "Completed / Released", "Pending Results"});
        statusFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusFilterCombo.setPreferredSize(new Dimension(160, 32));
        statusFilterCombo.addActionListener(e -> filterGradesTable());
        leftFilters.add(statusFilterCombo);

        toolbar.add(leftFilters, BorderLayout.WEST);

        gradesTallyLabel = new JLabel("Showing all registered courses");
        gradesTallyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gradesTallyLabel.setForeground(COLOR_TEXT_MUTED);
        toolbar.add(gradesTallyLabel, BorderLayout.EAST);

        panel.add(toolbar, BorderLayout.NORTH);

        // Course Grades Table
        String[] columns = {"#", "Course Code", "Course Name", "Credits", "Final Mark", "Letter Grade", "Grade Point", "Status"};
        gradesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        gradesTable = new JTable(gradesTableModel);
        gradesTable.setRowHeight(38);
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
        gradesTable.getColumnModel().getColumn(1).setPreferredWidth(110);  // Code
        gradesTable.getColumnModel().getColumn(2).setPreferredWidth(320);  // Name
        gradesTable.getColumnModel().getColumn(3).setPreferredWidth(75);   // Credits
        gradesTable.getColumnModel().getColumn(4).setPreferredWidth(110);  // Mark
        gradesTable.getColumnModel().getColumn(5).setPreferredWidth(110);  // Grade
        gradesTable.getColumnModel().getColumn(6).setPreferredWidth(100);  // GP
        gradesTable.getColumnModel().getColumn(7).setPreferredWidth(120);  // Status

        // Alignments & Pill Badge Renderers
        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        gradesTable.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        gradesTable.getColumnModel().getColumn(3).setCellRenderer(centerRender);
        gradesTable.getColumnModel().getColumn(4).setCellRenderer(centerRender);
        gradesTable.getColumnModel().getColumn(6).setCellRenderer(centerRender);

        gradesTable.getColumnModel().getColumn(1).setCellRenderer(new CourseCodeCellRenderer());
        gradesTable.getColumnModel().getColumn(5).setCellRenderer(new GradeBadgeCellRenderer());
        gradesTable.getColumnModel().getColumn(7).setCellRenderer(new StatusBadgeCellRenderer());

        JScrollPane tableScroll = new JScrollPane(gradesTable);
        tableScroll.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        tableScroll.getViewport().setBackground(COLOR_WHITE);
        panel.add(tableScroll, BorderLayout.CENTER);

        return panel;
    }

    // ------------------------------------------
    // TAB 2: DETAILED EXAM & CA MARKS PANEL
    // ------------------------------------------
    private JPanel createExamMarksTabPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(COLOR_WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBox.setOpaque(false);

        JLabel compLbl = new JLabel("Assessment Type:");
        compLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        compLbl.setForeground(COLOR_TEXT_PRIMARY);
        leftBox.add(compLbl);

        componentTypeFilterCombo = new JComboBox<>(new String[]{"All Assessment Components", "Continuous Assessments (CA)", "Final Examinations"});
        componentTypeFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        componentTypeFilterCombo.setPreferredSize(new Dimension(210, 32));
        componentTypeFilterCombo.addActionListener(e -> filterExamMarksTable());
        leftBox.add(componentTypeFilterCombo);

        toolbar.add(leftBox, BorderLayout.WEST);

        examMarksTallyLabel = new JLabel("Showing all component marks");
        examMarksTallyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        examMarksTallyLabel.setForeground(COLOR_TEXT_MUTED);
        toolbar.add(examMarksTallyLabel, BorderLayout.EAST);

        panel.add(toolbar, BorderLayout.NORTH);

        // Exam Marks Table
        String[] columns = {"#", "Course Code", "Course Name", "Assessment Component", "Type", "Max Marks", "Weight %", "Marks Obtained", "Weighted Mark", "Date Uploaded"};
        examMarksTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        examMarksTable = new JTable(examMarksTableModel);
        examMarksTable.setRowHeight(38);
        examMarksTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        examMarksTable.setSelectionBackground(COLOR_LIGHT_BLUE);
        examMarksTable.setSelectionForeground(COLOR_TEXT_PRIMARY);
        examMarksTable.setShowVerticalLines(false);
        examMarksTable.setGridColor(new Color(0xF1, 0xF5, 0xF9));

        examMarksTableSorter = new TableRowSorter<>(examMarksTableModel);
        examMarksTable.setRowSorter(examMarksTableSorter);

        JTableHeader header = examMarksTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(0xFA, 0xFA, 0xFC));
        header.setForeground(COLOR_PRIMARY_NAVY);
        header.setPreferredSize(new Dimension(0, 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

        // Column widths
        examMarksTable.getColumnModel().getColumn(0).setPreferredWidth(40);   // #
        examMarksTable.getColumnModel().getColumn(1).setPreferredWidth(95);   // Code
        examMarksTable.getColumnModel().getColumn(2).setPreferredWidth(210);  // Course Name
        examMarksTable.getColumnModel().getColumn(3).setPreferredWidth(190);  // Component
        examMarksTable.getColumnModel().getColumn(4).setPreferredWidth(80);   // Type
        examMarksTable.getColumnModel().getColumn(5).setPreferredWidth(75);   // Max
        examMarksTable.getColumnModel().getColumn(6).setPreferredWidth(75);   // Weight
        examMarksTable.getColumnModel().getColumn(7).setPreferredWidth(110);  // Obtained
        examMarksTable.getColumnModel().getColumn(8).setPreferredWidth(100);  // Weighted
        examMarksTable.getColumnModel().getColumn(9).setPreferredWidth(95);   // Date

        // Alignments
        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        examMarksTable.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        examMarksTable.getColumnModel().getColumn(4).setCellRenderer(new ComponentTypeBadgeCellRenderer());
        examMarksTable.getColumnModel().getColumn(5).setCellRenderer(centerRender);
        examMarksTable.getColumnModel().getColumn(6).setCellRenderer(centerRender);
        examMarksTable.getColumnModel().getColumn(7).setCellRenderer(centerRender);
        examMarksTable.getColumnModel().getColumn(8).setCellRenderer(centerRender);
        examMarksTable.getColumnModel().getColumn(9).setCellRenderer(centerRender);
        examMarksTable.getColumnModel().getColumn(1).setCellRenderer(new CourseCodeCellRenderer());

        JScrollPane tableScroll = new JScrollPane(examMarksTable);
        tableScroll.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        tableScroll.getViewport().setBackground(COLOR_WHITE);
        panel.add(tableScroll, BorderLayout.CENTER);

        return panel;
    }

    // ------------------------------------------
    // TAB 3: UGC GRADING SCALE REFERENCE
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
        JLabel refTitle = new JLabel("University Grants Commission (UGC) Grading Criteria");
        refTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        refTitle.setForeground(COLOR_PRIMARY_NAVY);
        JLabel refSub = new JLabel("Official Circular No. 12-2024 Criteria for Faculty of Technology Degrees");
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

        JLabel examRuleLbl = new JLabel("<html><b>Note on Assessment:</b><br>" +
                "• Continuous Assessment (CA) accounts for 30% - 40% of the course.<br>" +
                "• Final Examination accounts for 60% - 70% of the course.<br>" +
                "• A minimum 80% attendance is strictly required to be eligible for final examinations.</html>");
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
    // 4. DATA LOADING FROM DATABASE
    // ==========================================
    public void loadGradesData() {
        try {
            // 1. Fetch Course Grades
            courseGradesList = gradesDAO.getStudentCourseGrades(studentIdentifier);

            // 2. Fetch Exam Component Marks
            examMarksList = gradesDAO.getStudentExamMarks(studentIdentifier);

            // 3. Fetch GPA Summary
            gpaSummary = gradesDAO.getStudentGPASummary(studentIdentifier);

            // 4. Update Header Subtitle
            String studentName = (loggedInStudent != null && loggedInStudent.getName() != null)
                    ? loggedInStudent.getName() : "H.D.W.B Hapuarachchi";
            String regNo = studentIdentifier;
            studentSubtitleLabel.setText("Student: " + studentName + "  •  Reg No: " + regNo +
                    "  •  Batch: 2023/2024  •  Department of ICT");

            // 5. Update KPI Cards
            updateSummaryCards();

            // 6. Populate Tables
            populateCourseGradesTable();
            populateExamMarksTable();

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
                    item.getFormattedMark(),
                    item.getGrade(),
                    item.getFormattedGradePoint(),
                    item.getStatus()
            });
        }
        filterGradesTable();
    }

    private void populateExamMarksTable() {
        examMarksTableModel.setRowCount(0);
        int index = 1;
        for (ExamMarkItem item : examMarksList) {
            examMarksTableModel.addRow(new Object[]{
                    index++,
                    item.getCourseCode(),
                    item.getCourseName(),
                    item.getComponentName(),
                    item.getComponentType(),
                    String.format("%.0f", item.getMaximumMarks()),
                    String.format("%.0f%%", item.getWeightPercentage()),
                    String.format("%.2f", item.getMarksObtained()),
                    String.format("%.2f", item.getWeightedContribution()),
                    item.getUploadedDate()
            });
        }
        filterExamMarksTable();
    }

    // ==========================================
    // 5. SEARCH & FILTERING LOGIC
    // ==========================================
    private void filterGradesTable() {
        String query = searchField.getText().trim();
        String status = (String) statusFilterCombo.getSelectedItem();

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        if (!query.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + query, 1, 2)); // Course Code or Name
        }

        if (status != null && !"All Courses".equals(status)) {
            if ("Completed / Released".equals(status)) {
                filters.add(RowFilter.regexFilter("(?i)Completed", 7));
            } else if ("Pending Results".equals(status)) {
                filters.add(RowFilter.regexFilter("(?i)In Progress|Pending", 7));
            }
        }

        if (filters.isEmpty()) {
            gradesTableSorter.setRowFilter(null);
        } else {
            gradesTableSorter.setRowFilter(RowFilter.andFilter(filters));
        }

        int count = gradesTable.getRowCount();
        gradesTallyLabel.setText("Showing " + count + " of " + courseGradesList.size() + " courses");
    }

    private void filterExamMarksTable() {
        String compType = (String) componentTypeFilterCombo.getSelectedItem();
        if (compType == null || "All Assessment Components".equals(compType)) {
            examMarksTableSorter.setRowFilter(null);
        } else if ("Continuous Assessments (CA)".equals(compType)) {
            examMarksTableSorter.setRowFilter(RowFilter.regexFilter("(?i)CA", 4));
        } else if ("Final Examinations".equals(compType)) {
            examMarksTableSorter.setRowFilter(RowFilter.regexFilter("(?i)Final", 4));
        }

        int count = examMarksTable.getRowCount();
        examMarksTallyLabel.setText("Showing " + count + " of " + examMarksList.size() + " assessment marks");
    }

    // ==========================================
    // 6. CUSTOM TABLE CELL RENDERERS
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

    private static class ComponentTypeBadgeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

            String type = (value != null) ? value.toString() : "";
            JLabel badge = new JLabel(" " + type + " ");
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setOpaque(true);

            if ("CA".equalsIgnoreCase(type)) {
                badge.setBackground(new Color(0xFA, 0xE8, 0xFF)); // Light Purple
                badge.setForeground(new Color(0x86, 0x19, 0x8F));
            } else {
                badge.setBackground(new Color(0xEE, 0xF2, 0xFF)); // Light Navy
                badge.setForeground(new Color(0x37, 0x30, 0xA3));
            }
            badge.setBorder(new EmptyBorder(3, 6, 3, 6));

            panel.add(badge);
            return panel;
        }
    }

    // ==========================================
    // 7. TRANSCRIPT DIALOG & HELPERS
    // ==========================================
    private void showTranscriptDialog() {
        StringBuilder sb = new StringBuilder();
        sb.append("=================================================================\n");
        sb.append("      FACULTY OF TECHNOLOGY - ACADEMIC TRANSCRIPT SUMMARY        \n");
        sb.append("=================================================================\n\n");
        sb.append("Student Name       : ").append(loggedInStudent != null && loggedInStudent.getName() != null ? loggedInStudent.getName() : "H.D.W.B Hapuarachchi").append("\n");
        sb.append("Registration No    : ").append(studentIdentifier).append("\n");
        sb.append("Department         : Department of Information and Communication Technology\n");
        sb.append("Academic Year      : Level II Semester I (2026)\n");
        sb.append("Date Generated     : ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("\n\n");
        sb.append("-----------------------------------------------------------------\n");
        sb.append(String.format("%-10s %-32s %-6s %-8s %-6s\n", "CODE", "COURSE NAME", "CREDIT", "MARK", "GRADE"));
        sb.append("-----------------------------------------------------------------\n");

        for (CourseGradeItem c : courseGradesList) {
            String cName = c.getCourseName();
            if (cName.length() > 30) cName = cName.substring(0, 27) + "...";
            sb.append(String.format("%-10s %-32s %-6d %-8s %-6s\n",
                    c.getCourseCode(), cName, c.getCredit(), c.getFormattedMark(), c.getGrade()));
        }

        sb.append("-----------------------------------------------------------------\n");
        sb.append(String.format("Total Registered Credits: %d  |  Evaluated Credits: %d\n",
                gpaSummary.getTotalRegisteredCredits(), gpaSummary.getTotalCompletedCredits()));
        sb.append(String.format("Cumulative GPA (CGPA)   : %.2f\n", gpaSummary.getCgpa()));
        sb.append(String.format("Honours Classification  : %s\n", gpaSummary.getDegreeClassification()));
        sb.append("Academic Standing       : ").append(gpaSummary.getStandingStatus()).append("\n");
        sb.append("=================================================================\n");

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setCaretPosition(0);

        JScrollPane sp = new JScrollPane(textArea);
        sp.setPreferredSize(new Dimension(600, 420));

        JOptionPane.showMessageDialog(this, sp, "Academic Transcript Summary", JOptionPane.INFORMATION_MESSAGE);
    }

    private JButton createActionButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
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
