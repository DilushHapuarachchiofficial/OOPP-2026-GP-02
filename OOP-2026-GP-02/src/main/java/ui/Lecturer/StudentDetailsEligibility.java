package main.java.ui.Lecturer;

import main.java.model.Lecturer.StudentDetailsEligibilityLogic;
import main.java.model.Lecturer.StudentDetailsEligibilityLogic.*;
import main.java.model.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;


public class StudentDetailsEligibility extends JPanel {

    // =========================================================================
    // COLOR PALETTE (TecFAMS Academic Theme)
    // =========================================================================
    public static final Color COLOR_DEEP_NAVY      = new Color(8, 47, 90);    // #082F5A - Brand headers
    public static final Color COLOR_PRO_BLUE       = new Color(11, 79, 156);  // #0B4F9C - Primary action buttons
    public static final Color COLOR_TECH_BLUE      = new Color(40, 120, 208); // #2878D0 - Active accents
    public static final Color COLOR_PAGE_BG        = new Color(244, 247, 251);// #F4F7FB - Canvas background
    public static final Color COLOR_WHITE          = Color.WHITE;             // Pure white card background
    public static final Color COLOR_BORDER         = new Color(220, 228, 238);// #DCE4EE - Clean card border
    public static final Color COLOR_TEXT_PRIMARY   = new Color(15, 23, 42);   // Slate-900 primary text
    public static final Color COLOR_TEXT_SECONDARY = new Color(71, 85, 105);  // Slate-600 secondary text
    public static final Color COLOR_TEXT_MUTED     = new Color(148, 163, 184);// Slate-400 placeholder text
    public static final Color COLOR_SUCCESS        = new Color(16, 185, 129); // #10B981 - Tech emerald for Eligible
    public static final Color COLOR_DANGER         = new Color(220, 38, 38);  // #DC2626 - Warning red for Below 80%
    public static final Color COLOR_WARNING        = new Color(217, 119, 6);  // #D97706 - Amber for Exactly 80%
    public static final Color COLOR_ROW_ALT        = new Color(248, 250, 252);// Alternating row tint
    public static final Color COLOR_ROW_SELECTED   = new Color(224, 237, 254);// Soft selected row blue

    // =========================================================================
    // TYPOGRAPHY (Segoe UI Hierarchy)
    // =========================================================================
    public static final Font FONT_PAGE_TITLE       = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_PAGE_SUBTITLE    = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_CARD_LABEL       = new Font("Segoe UI Semibold", Font.BOLD, 12);
    public static final Font FONT_CARD_COUNT       = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SECTION_TITLE    = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_INPUT            = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_TABLE_HEADER     = new Font("Segoe UI Semibold", Font.BOLD, 12);
    public static final Font FONT_TABLE_CELL       = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BADGE            = new Font("Segoe UI", Font.BOLD, 11);

    // View State Constants
    private static final String VIEW_LOADING         = "LOADING";
    private static final String VIEW_SUCCESS         = "SUCCESS";
    private static final String VIEW_ERROR           = "ERROR";
    private static final String VIEW_UNAUTHENTICATED = "UNAUTHENTICATED";

    // =========================================================================
    // INSTANCE VARIABLES & STATE
    // =========================================================================
    private int lecturerId = -1;

    // View States Container
    private CardLayout cardLayout;
    private JPanel stateContainer;

    // Filter Controls
    private ModernComboBox<CourseInfo> courseSelector;
    private ModernSearchField searchField;
    private ModernComboBox<Scenario> scenarioSelector;
    private ModernComboBox<EligibilityFilter> eligibilityFilterCombo;
    private ModernComboBox<MedicalFilter> medicalFilterCombo;

    // Attendance View Selector (Segmented Pill Tab Bar)
    private ModernSegmentedViewSelector segmentedViewSelector;
    private AttendanceView currentAttendanceView = AttendanceView.THEORY_ONLY;

    // Summary Card Labels & Container
    private JPanel summaryCardsContainer;
    private JLabel totalStudentsLabel;
    private JLabel meetsRequirementLabel;
    private JLabel belowRequirementLabel;
    private JLabel withMedicalsLabel;

    // Table & Model
    private JTable studentsTable;
    private DefaultTableModel tableModel;
    private JLabel tableStatusLabel;
    private JLabel quickEligibleBadge;
    private JLabel quickIneligibleBadge;
    private JPanel emptyStatePanel;
    private JScrollPane tableScrollPane;

    // Action Buttons
    private ModernButton viewDetailsBtn;
    private ModernButton refreshBtn;
    private ModernButton resetFiltersBtn;
    private ModernButton exportBtn;

    // Error View Components
    private JLabel errorTitleLabel;
    private JTextArea errorDetailsArea;

    // Cached Data
    private List<CourseInfo> assignedCourses = new ArrayList<>();
    private List<StudentEligibilityItem> rawStudentList = new ArrayList<>();
    private List<StudentEligibilityItem> displayedStudentList = new ArrayList<>();

    // Optional Embedded Navigation Wiring
    private Navbar parentNavbar;
    private CardLayout parentCardLayout;
    private JPanel parentContainer;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================

    public StudentDetailsEligibility() {
        resolveLecturerSession();
        initializeUI();
        loadCoursesAndData();
    }

    public StudentDetailsEligibility(int lecturerId) {
        this.lecturerId = lecturerId;
        initializeUI();
        loadCoursesAndData();
    }

    public StudentDetailsEligibility(Navbar navbar, CardLayout layout, JPanel container) {
        this.parentNavbar = navbar;
        this.parentCardLayout = layout;
        this.parentContainer = container;
        resolveLecturerSession();
        initializeUI();
        loadCoursesAndData();
    }

    private void resolveLecturerSession() {
        UserSession session = UserSession.getCurrentSession();
        if (session != null && "Lecturer".equalsIgnoreCase(session.getRole())) {
            this.lecturerId = session.getUserId();
        }
    }

    // =========================================================================
    // UI INITIALIZATION
    // =========================================================================

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(COLOR_PAGE_BG);

        cardLayout = new CardLayout();
        stateContainer = new JPanel(cardLayout);
        stateContainer.setOpaque(false);

        // 1. Loading View
        stateContainer.add(createLoadingView(), VIEW_LOADING);

        // 2. Success View
        stateContainer.add(createSuccessView(), VIEW_SUCCESS);

        // 3. Error View
        stateContainer.add(createErrorView(), VIEW_ERROR);

        // 4. Unauthenticated View
        stateContainer.add(createUnauthenticatedView(), VIEW_UNAUTHENTICATED);

        add(stateContainer, BorderLayout.CENTER);
    }

    // =========================================================================
    // SUCCESS VIEW ASSEMBLY (Laptop Responsive)
    // =========================================================================

    private JPanel createSuccessView() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(COLOR_PAGE_BG);
        mainPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. Header Banner
        mainPanel.add(createHeaderSection());
        mainPanel.add(Box.createVerticalStrut(14));

        // 2. Interactive Adaptive Summary Statistics Cards
        mainPanel.add(createSummaryCardsSection());
        mainPanel.add(Box.createVerticalStrut(14));

        // 3. Filter Controls & Attendance View Selector
        mainPanel.add(createFilterPanel());
        mainPanel.add(Box.createVerticalStrut(14));

        // 4. Main Students Table Card
        mainPanel.add(createTableSection());

        JScrollPane outerScroll = new JScrollPane(mainPanel);
        outerScroll.setBorder(null);
        outerScroll.getVerticalScrollBar().setUnitIncrement(18);
        outerScroll.getHorizontalScrollBar().setUnitIncrement(18);
        outerScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        outerScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        outerScroll.setOpaque(false);
        outerScroll.getViewport().setOpaque(false);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(outerScroll, BorderLayout.CENTER);
        return wrapper;
    }

    // Header section (Wraps gracefully on small laptop screens)
    private JPanel createHeaderSection() {
        JPanel panel = new JPanel(new WrapLayout(FlowLayout.LEFT, 16, 6));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel title = new JLabel("Student Details & Eligibility");
        title.setFont(FONT_PAGE_TITLE);
        title.setForeground(COLOR_DEEP_NAVY);

        JLabel subtitle = new JLabel("Review undergraduate information, attendance records, medical submissions, and examination eligibility.");
        subtitle.setFont(FONT_PAGE_SUBTITLE);
        subtitle.setForeground(COLOR_TEXT_SECONDARY);

        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(subtitle);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        ModernButton topRefreshBtn = new ModernButton(
                "Refresh",
                new EligibilityVectorIcon(EligibilityVectorIcon.Type.REFRESH, 12, 12),
                ModernButton.Style.SECONDARY
        );
        topRefreshBtn.setPreferredSize(new Dimension(100, 34));
        topRefreshBtn.setToolTipText("Reload latest attendance records from database");
        topRefreshBtn.addActionListener(e -> {
            CourseInfo sel = (CourseInfo) courseSelector.getSelectedItem();
            if (sel != null) loadStudentEligibilityData(sel.getCourseId());
        });

        ModernButton topExportBtn = new ModernButton(
                "Export CSV",
                new EligibilityVectorIcon(EligibilityVectorIcon.Type.EXPORT, 12, 12),
                ModernButton.Style.SUCCESS
        );
        topExportBtn.setPreferredSize(new Dimension(120, 34));
        topExportBtn.setToolTipText("Export currently displayed eligibility list to CSV");
        topExportBtn.addActionListener(e -> exportEligibilityCSV());

        rightActions.add(topRefreshBtn);
        rightActions.add(topExportBtn);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(left, BorderLayout.WEST);
        wrapper.add(rightActions, BorderLayout.EAST);

        return wrapper;
    }

    // =========================================================================
    // SUMMARY CARDS SECTION (Adaptive 1x4 or 2x2 for Laptops)
    // =========================================================================

    private JPanel createSummaryCardsSection() {
        summaryCardsContainer = new JPanel() {
            @Override
            public void doLayout() {
                int w = getWidth();
                if (w > 0 && w < 960) {
                    if (!(getLayout() instanceof GridLayout) || ((GridLayout) getLayout()).getRows() != 2) {
                        setLayout(new GridLayout(2, 2, 12, 10));
                    }
                } else {
                    if (!(getLayout() instanceof GridLayout) || ((GridLayout) getLayout()).getRows() != 1) {
                        setLayout(new GridLayout(1, 4, 14, 0));
                    }
                }
                super.doLayout();
            }
        };
        summaryCardsContainer.setLayout(new GridLayout(1, 4, 14, 0));
        summaryCardsContainer.setOpaque(false);
        summaryCardsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        totalStudentsLabel = new JLabel("—");
        meetsRequirementLabel = new JLabel("—");
        belowRequirementLabel = new JLabel("—");
        withMedicalsLabel = new JLabel("—");

        summaryCardsContainer.add(createMetricCard(
                "Total Enrolled",
                totalStudentsLabel,
                "All course candidates",
                COLOR_PRO_BLUE,
                "Show All",
                this::resetAllFilters
        ));

        summaryCardsContainer.add(createMetricCard(
                "Meets Requirement (≥ 80%)",
                meetsRequirementLabel,
                "Exam admission eligible",
                COLOR_SUCCESS,
                "Filter ≥80%",
                () -> {
                    if (eligibilityFilterCombo != null) {
                        eligibilityFilterCombo.setSelectedItem(EligibilityFilter.MEETS_REQUIREMENT);
                    }
                }
        ));

        summaryCardsContainer.add(createMetricCard(
                "Below Requirement (< 80%)",
                belowRequirementLabel,
                "Requires review / medicals",
                COLOR_DANGER,
                "Filter <80%",
                () -> {
                    if (eligibilityFilterCombo != null) {
                        eligibilityFilterCombo.setSelectedItem(EligibilityFilter.BELOW_REQUIREMENT);
                    }
                }
        ));

        summaryCardsContainer.add(createMetricCard(
                "With Medical Records",
                withMedicalsLabel,
                "Official leave records",
                COLOR_WARNING,
                "Filter Meds",
                () -> {
                    if (medicalFilterCombo != null) {
                        medicalFilterCombo.setSelectedItem(MedicalFilter.HAS_MEDICAL);
                    }
                }
        ));

        return summaryCardsContainer;
    }

    private JPanel createMetricCard(String labelText, JLabel countLabel, String subtitleText, Color accentColor, String actionHint, Runnable clickAction) {
        InteractiveMetricCard card = new InteractiveMetricCard(accentColor, clickAction);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(new EmptyBorder(12, 14, 12, 14));

        // Left accent stripe
        JPanel stripe = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                g2.fill(new RoundRectangle2D.Float(0, 2, 4, getHeight() - 4, 4, 4));
                g2.dispose();
            }
        };
        stripe.setPreferredSize(new Dimension(5, 36));
        stripe.setOpaque(false);

        // Content
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_CARD_LABEL);
        lbl.setForeground(COLOR_TEXT_SECONDARY);

        JLabel tag = new JLabel(actionHint + " →");
        tag.setFont(new Font("Segoe UI Semibold", Font.BOLD, 10));
        tag.setForeground(accentColor);

        topHeader.add(lbl, BorderLayout.WEST);
        topHeader.add(tag, BorderLayout.EAST);

        countLabel.setFont(FONT_CARD_COUNT);
        countLabel.setForeground(COLOR_DEEP_NAVY);

        JLabel sub = new JLabel(subtitleText);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(COLOR_TEXT_MUTED);

        textPanel.add(topHeader);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(countLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(sub);

        card.add(stripe, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // FILTER CONTROLS & ATTENDANCE VIEW SELECTOR (Fluid WrapLayout)
    // =========================================================================

    private JPanel createFilterPanel() {
        JPanel card = createWhiteCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Section Title Bar
        JPanel sectionHeader = new JPanel(new BorderLayout());
        sectionHeader.setOpaque(false);

        JLabel secTitle = new JLabel("FILTER & ATTENDANCE CRITERIA");
        secTitle.setIcon(new EligibilityVectorIcon(EligibilityVectorIcon.Type.SEARCH, 13, 13, COLOR_PRO_BLUE));
        secTitle.setIconTextGap(6);
        secTitle.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        secTitle.setForeground(COLOR_PRO_BLUE);

        JLabel hint = new JLabel("Toggle between theory, practical, or combined views and filter criteria");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(COLOR_TEXT_MUTED);

        sectionHeader.add(secTitle, BorderLayout.WEST);
        sectionHeader.add(hint, BorderLayout.EAST);

        card.add(sectionHeader);
        card.add(Box.createVerticalStrut(10));

        // Row 1: Course Selector and Segmented View Selector (Wraps cleanly on laptop)
        JPanel row1 = new JPanel(new WrapLayout(FlowLayout.LEFT, 12, 6));
        row1.setOpaque(false);

        JPanel courseGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        courseGroup.setOpaque(false);
        JLabel courseLbl = new JLabel("Assigned Course:");
        courseLbl.setFont(FONT_CARD_LABEL);
        courseLbl.setForeground(COLOR_DEEP_NAVY);

        courseSelector = new ModernComboBox<>();
        courseSelector.setPreferredSize(new Dimension(340, 36));
        courseSelector.addActionListener(e -> {
            CourseInfo sel = (CourseInfo) courseSelector.getSelectedItem();
            if (sel != null) {
                updateViewSelectorAvailability(sel);
                loadStudentEligibilityData(sel.getCourseId());
            }
        });
        courseGroup.add(courseLbl);
        courseGroup.add(courseSelector);

        JPanel viewGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        viewGroup.setOpaque(false);
        JLabel viewLbl = new JLabel("Attendance View:");
        viewLbl.setFont(FONT_CARD_LABEL);
        viewLbl.setForeground(COLOR_DEEP_NAVY);

        segmentedViewSelector = new ModernSegmentedViewSelector(this::setAttendanceView);
        viewGroup.add(viewLbl);
        viewGroup.add(segmentedViewSelector);

        row1.add(courseGroup);
        row1.add(viewGroup);

        card.add(row1);
        card.add(Box.createVerticalStrut(8));

        // Row 2: Search, Scenario, Status, Medical, Reset (Fluid wrapping prevents offscreen clipping)
        JPanel row2 = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 6));
        row2.setOpaque(false);

        searchField = new ModernSearchField();
        searchField.setPreferredSize(new Dimension(190, 34));
        searchField.setPlaceholder("Search student or reg no...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        scenarioSelector = new ModernComboBox<>(Scenario.values());
        scenarioSelector.setPreferredSize(new Dimension(230, 34));
        scenarioSelector.setToolTipText("Filter by attendance threshold and medical records");
        scenarioSelector.addActionListener(e -> applyFilters());

        eligibilityFilterCombo = new ModernComboBox<>(EligibilityFilter.values());
        eligibilityFilterCombo.setPreferredSize(new Dimension(180, 34));
        eligibilityFilterCombo.setToolTipText("Filter by examination eligibility threshold (80%)");
        eligibilityFilterCombo.addActionListener(e -> applyFilters());

        medicalFilterCombo = new ModernComboBox<>(MedicalFilter.values());
        medicalFilterCombo.setPreferredSize(new Dimension(170, 34));
        medicalFilterCombo.setToolTipText("Filter by submitted medical certificate status");
        medicalFilterCombo.addActionListener(e -> applyFilters());

        resetFiltersBtn = new ModernButton(
                "Reset",
                new EligibilityVectorIcon(EligibilityVectorIcon.Type.RESET, 12, 12),
                ModernButton.Style.OUTLINE
        );
        resetFiltersBtn.setPreferredSize(new Dimension(85, 34));
        resetFiltersBtn.setToolTipText("Clear search and reset all filters to default");
        resetFiltersBtn.addActionListener(e -> resetAllFilters());

        row2.add(new JLabel("Search:"));
        row2.add(searchField);
        row2.add(new JLabel("Attendance:"));
        row2.add(scenarioSelector);
        row2.add(new JLabel("Status:"));
        row2.add(eligibilityFilterCombo);
        row2.add(new JLabel("Medical:"));
        row2.add(medicalFilterCombo);
        row2.add(resetFiltersBtn);

        card.add(row2);
        return card;
    }

    private void updateViewSelectorAvailability(CourseInfo course) {
        if (course == null || segmentedViewSelector == null) return;
        boolean hasPractical = course.hasPracticalComponent();

        segmentedViewSelector.setPracticalEnabled(hasPractical);
        if (!hasPractical && currentAttendanceView == AttendanceView.PRACTICAL_ONLY) {
            setAttendanceView(AttendanceView.THEORY_ONLY);
        }
    }

    private void setAttendanceView(AttendanceView view) {
        this.currentAttendanceView = view;
        if (segmentedViewSelector != null) {
            segmentedViewSelector.setSelectedView(view);
        }
        applyFilters();
    }

    private void resetAllFilters() {
        if (searchField != null) searchField.setText("");
        if (scenarioSelector != null) scenarioSelector.setSelectedIndex(0);
        if (eligibilityFilterCombo != null) eligibilityFilterCombo.setSelectedIndex(0);
        if (medicalFilterCombo != null) medicalFilterCombo.setSelectedIndex(0);
        applyFilters();
    }

    // =========================================================================
    // STUDENT DETAILS TABLE SECTION
    // =========================================================================

    private JPanel createTableSection() {
        JPanel card = createWhiteCardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Header Bar of Card
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel heading = new JLabel("Undergraduate Attendance & Examination Eligibility");
        heading.setFont(FONT_SECTION_TITLE);
        heading.setForeground(COLOR_DEEP_NAVY);

        tableStatusLabel = new JLabel("Loading students...");
        tableStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableStatusLabel.setForeground(COLOR_TEXT_SECONDARY);

        topBar.add(heading, BorderLayout.WEST);
        topBar.add(tableStatusLabel, BorderLayout.EAST);

        // Table Model Setup
        String[] columnNames = {
                "#", "Student Reg No", "Student Name",
                "Theory Attendance", "Practical Attendance", "Selected View Attendance",
                "Medical Records", "Eligibility Status", "Actions"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8; // Only interactive action button is editable
            }
        };

        studentsTable = new JTable(tableModel);
        studentsTable.setFont(FONT_TABLE_CELL);
        studentsTable.setRowHeight(38);
        studentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentsTable.setShowVerticalLines(false);
        studentsTable.setShowHorizontalLines(true);
        studentsTable.setGridColor(new Color(241, 245, 249));
        studentsTable.setSelectionBackground(COLOR_ROW_SELECTED);
        studentsTable.setSelectionForeground(COLOR_DEEP_NAVY);

        // Turn OFF auto-resize to guarantee clean columns on laptops with horizontal scrollbar
        studentsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Explicit Column Preferred Widths
        studentsTable.getColumnModel().getColumn(0).setPreferredWidth(45);  // #
        studentsTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Reg No
        studentsTable.getColumnModel().getColumn(2).setPreferredWidth(190); // Name
        studentsTable.getColumnModel().getColumn(3).setPreferredWidth(125); // Theory
        studentsTable.getColumnModel().getColumn(4).setPreferredWidth(125); // Practical
        studentsTable.getColumnModel().getColumn(5).setPreferredWidth(150); // Selected View
        studentsTable.getColumnModel().getColumn(6).setPreferredWidth(125); // Medical Records
        studentsTable.getColumnModel().getColumn(7).setPreferredWidth(125); // Eligibility
        studentsTable.getColumnModel().getColumn(8).setPreferredWidth(125); // Actions

        // Header Styling
        JTableHeader header = studentsTable.getTableHeader();
        header.setFont(FONT_TABLE_HEADER);
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(COLOR_DEEP_NAVY);
        header.setPreferredSize(new Dimension(0, 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        header.setReorderingAllowed(false);

        // Custom Renderers for Badges & Status
        setupTableRenderers();

        // Double-click row opens Details Dialog
        studentsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && studentsTable.getSelectedRow() != -1) {
                    showStudentDetailsDialog(studentsTable.getSelectedRow());
                }
            }
        });

        tableScrollPane = new JScrollPane(studentsTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        tableScrollPane.getViewport().setBackground(COLOR_WHITE);
        tableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.setPreferredSize(new Dimension(800, 360));

        // Empty State Panel
        emptyStatePanel = createEmptyStatePanel();

        // Bottom Action Bar: Responsive WrapLayout prevents buttons from colliding
        JPanel bottomBar = new JPanel(new WrapLayout(FlowLayout.RIGHT, 10, 6));
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(10, 0, 0, 0));

        quickEligibleBadge = new JLabel("● 0 Meets Requirement");
        quickEligibleBadge.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        quickEligibleBadge.setForeground(COLOR_SUCCESS);

        quickIneligibleBadge = new JLabel("● 0 Below Requirement");
        quickIneligibleBadge.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        quickIneligibleBadge.setForeground(COLOR_DANGER);

        refreshBtn = new ModernButton(
                "Refresh Data",
                new EligibilityVectorIcon(EligibilityVectorIcon.Type.REFRESH, 12, 12),
                ModernButton.Style.SECONDARY
        );
        refreshBtn.addActionListener(e -> {
            CourseInfo sel = (CourseInfo) courseSelector.getSelectedItem();
            if (sel != null) loadStudentEligibilityData(sel.getCourseId());
        });

        exportBtn = new ModernButton(
                "Export CSV",
                new EligibilityVectorIcon(EligibilityVectorIcon.Type.EXPORT, 12, 12),
                ModernButton.Style.SUCCESS
        );
        exportBtn.addActionListener(e -> exportEligibilityCSV());

        viewDetailsBtn = new ModernButton(
                "View Full Profile",
                new EligibilityVectorIcon(EligibilityVectorIcon.Type.EYE, 12, 12),
                ModernButton.Style.PRIMARY
        );
        viewDetailsBtn.addActionListener(e -> showStudentDetailsDialog());

        bottomBar.add(quickEligibleBadge);
        bottomBar.add(Box.createHorizontalStrut(8));
        bottomBar.add(quickIneligibleBadge);
        bottomBar.add(Box.createHorizontalStrut(14));
        bottomBar.add(refreshBtn);
        bottomBar.add(exportBtn);
        bottomBar.add(viewDetailsBtn);

        card.add(topBar, BorderLayout.NORTH);
        card.add(Box.createVerticalStrut(10), BorderLayout.CENTER);
        card.add(tableScrollPane, BorderLayout.CENTER);
        card.add(bottomBar, BorderLayout.SOUTH);

        return card;
    }

    private void setupTableRenderers() {
        // Center alignment for index
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        studentsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        // Selected View Attendance (Column 5) - High contrast & bold
        studentsTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));

                if (row >= 0 && row < displayedStudentList.size()) {
                    StudentEligibilityItem item = displayedStudentList.get(row);
                    ThresholdClassification tc = item.getThresholdStatus(currentAttendanceView);
                    if (tc == ThresholdClassification.ABOVE_80) {
                        lbl.setForeground(new Color(4, 120, 87));
                    } else if (tc == ThresholdClassification.EXACTLY_80) {
                        lbl.setForeground(COLOR_WARNING);
                    } else if (tc == ThresholdClassification.BELOW_80) {
                        lbl.setForeground(COLOR_DANGER);
                    } else {
                        lbl.setForeground(COLOR_TEXT_MUTED);
                    }
                }
                return lbl;
            }
        });

        // Medical Records Badge (Column 6)
        studentsTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(FONT_BADGE);

                String text = value != null ? value.toString() : "None";
                lbl.setText(text);

                if ("None".equals(text) || text.contains("0 Records")) {
                    lbl.setForeground(COLOR_TEXT_MUTED);
                } else if (text.contains("Approved")) {
                    lbl.setForeground(new Color(16, 149, 193));
                } else {
                    lbl.setForeground(COLOR_WARNING);
                }
                return lbl;
            }
        });

        // Eligibility Status Badge (Column 7)
        studentsTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(FONT_BADGE);

                String status = value != null ? value.toString() : "";
                if ("Eligible".equalsIgnoreCase(status)) {
                    lbl.setForeground(COLOR_SUCCESS);
                    lbl.setText("  ELIGIBLE  ");
                } else if ("Not Eligible".equalsIgnoreCase(status)) {
                    lbl.setForeground(COLOR_DANGER);
                    lbl.setText("  NOT ELIGIBLE  ");
                } else {
                    lbl.setForeground(COLOR_TEXT_MUTED);
                    lbl.setText("  " + status + "  ");
                }
                return lbl;
            }
        });

        // Action column: Interactive Modern Pill Button with Vector Eye Icon
        studentsTable.getColumnModel().getColumn(8).setCellRenderer(new ModernTableActionRenderer());
        studentsTable.getColumnModel().getColumn(8).setCellEditor(new ModernTableActionEditor(this::showStudentDetailsDialog));
    }

    private JPanel createEmptyStatePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(800, 200));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel iconLabel = new JLabel(new EligibilityVectorIcon(EligibilityVectorIcon.Type.DOCUMENT, 36, 36, COLOR_TEXT_MUTED));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msgLabel = new JLabel("No Students Found For Current Criteria");
        msgLabel.setFont(FONT_SECTION_TITLE);
        msgLabel.setForeground(COLOR_DEEP_NAVY);
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hintLabel = new JLabel("Adjust or reset your filters above to view enrolled students.");
        hintLabel.setFont(FONT_PAGE_SUBTITLE);
        hintLabel.setForeground(COLOR_TEXT_SECONDARY);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        ModernButton resetBtn = new ModernButton(
                "Reset All Filters",
                new EligibilityVectorIcon(EligibilityVectorIcon.Type.RESET, 12, 12),
                ModernButton.Style.PRIMARY
        );
        resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetBtn.addActionListener(e -> resetAllFilters());

        inner.add(iconLabel);
        inner.add(Box.createVerticalStrut(8));
        inner.add(msgLabel);
        inner.add(Box.createVerticalStrut(4));
        inner.add(hintLabel);
        inner.add(Box.createVerticalStrut(12));
        inner.add(resetBtn);

        panel.add(inner);
        return panel;
    }

    // =========================================================================
    // ASYNCHRONOUS DATA LOADING (SwingWorker)
    // =========================================================================

    public void refreshData() {
        loadCoursesAndData();
    }

    private void loadCoursesAndData() {
        resolveLecturerSession();

        if (lecturerId <= 0) {
            cardLayout.show(stateContainer, VIEW_UNAUTHENTICATED);
            return;
        }

        cardLayout.show(stateContainer, VIEW_LOADING);

        SwingWorker<List<CourseInfo>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<CourseInfo> doInBackground() throws Exception {
                return StudentDetailsEligibilityLogic.getAssignedCourses(lecturerId);
            }

            @Override
            protected void done() {
                try {
                    assignedCourses = get();
                    courseSelector.removeAllItems();
                    for (CourseInfo c : assignedCourses) {
                        courseSelector.addItem(c);
                    }

                    if (!assignedCourses.isEmpty()) {
                        CourseInfo first = assignedCourses.get(0);
                        updateViewSelectorAvailability(first);
                        loadStudentEligibilityData(first.getCourseId());
                    } else {
                        tableStatusLabel.setText("No courses assigned to your account.");
                        cardLayout.show(stateContainer, VIEW_SUCCESS);
                    }
                } catch (Exception ex) {
                    showError("Could not retrieve assigned courses: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void loadStudentEligibilityData(int courseId) {
        tableStatusLabel.setText("Loading attendance records...");

        SwingWorker<List<StudentEligibilityItem>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<StudentEligibilityItem> doInBackground() throws Exception {
                return StudentDetailsEligibilityLogic.getStudentEligibilityList(lecturerId, courseId);
            }

            @Override
            protected void done() {
                try {
                    rawStudentList = get();
                    applyFilters();
                    cardLayout.show(stateContainer, VIEW_SUCCESS);
                } catch (Exception ex) {
                    showError("Failed to load student eligibility records: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void applyFilters() {
        String search = (searchField != null) ? searchField.getText() : "";
        Scenario scenario = (scenarioSelector != null) ? (Scenario) scenarioSelector.getSelectedItem() : Scenario.ALL;
        EligibilityFilter elFilter = (eligibilityFilterCombo != null) ? (EligibilityFilter) eligibilityFilterCombo.getSelectedItem() : EligibilityFilter.ALL;
        MedicalFilter medFilter = (medicalFilterCombo != null) ? (MedicalFilter) medicalFilterCombo.getSelectedItem() : MedicalFilter.ALL;

        if (scenario == null) scenario = Scenario.ALL;
        if (elFilter == null) elFilter = EligibilityFilter.ALL;
        if (medFilter == null) medFilter = MedicalFilter.ALL;

        displayedStudentList = StudentDetailsEligibilityLogic.filterStudents(
                rawStudentList, currentAttendanceView, search, scenario, elFilter, medFilter
        );

        // Update Summary Cards based on the filtered course records
        SummaryStatistics stats = StudentDetailsEligibilityLogic.calculateSummary(displayedStudentList, currentAttendanceView);
        totalStudentsLabel.setText(String.valueOf(stats.getTotalStudents()));
        meetsRequirementLabel.setText(String.valueOf(stats.getMeetsRequirementCount()));
        belowRequirementLabel.setText(String.valueOf(stats.getBelowRequirementCount()));
        withMedicalsLabel.setText(String.valueOf(stats.getMedicalCount()));

        // Quick badges update
        if (stats.getTotalStudents() > 0) {
            double meetPct = (stats.getMeetsRequirementCount() * 100.0) / stats.getTotalStudents();
            double belowPct = (stats.getBelowRequirementCount() * 100.0) / stats.getTotalStudents();
            quickEligibleBadge.setText("● " + stats.getMeetsRequirementCount() + " Meets Requirement (" + formatPct(meetPct) + ")");
            quickIneligibleBadge.setText("● " + stats.getBelowRequirementCount() + " Below Requirement (" + formatPct(belowPct) + ")");
        } else {
            quickEligibleBadge.setText("● 0 Meets Requirement");
            quickIneligibleBadge.setText("● 0 Below Requirement");
        }

        // Populate Table Rows
        tableModel.setRowCount(0);
        int index = 1;
        for (StudentEligibilityItem item : displayedStudentList) {
            String theoryStr = (item.getTheoryHeld() > 0)
                    ? item.getTheoryAttended() + "/" + item.getTheoryHeld() + " (" + formatPct(item.getTheoryPercentage()) + ")"
                    : "N/A (0 Held)";

            String practicalStr;
            if ("Theory_Only".equalsIgnoreCase(item.getCourseType())) {
                practicalStr = "N/A (Theory Only)";
            } else if (item.getPracticalHeld() > 0) {
                practicalStr = item.getPracticalAttended() + "/" + item.getPracticalHeld() + " (" + formatPct(item.getPracticalPercentage()) + ")";
            } else {
                practicalStr = "N/A (0 Held)";
            }

            String applicableStr = item.getApplicableAttended(currentAttendanceView) + "/" +
                                   item.getApplicableHeld(currentAttendanceView) + " (" +
                                   item.getApplicablePercentageFormatted(currentAttendanceView) + ")";

            String medStr = "None";
            if (item.hasMedicalRecords()) {
                int totalMed = item.getMedicalCount();
                int approved = item.getApprovedMedicalCount();
                if (approved > 0) {
                    medStr = totalMed + " Rec (" + approved + " Appr)";
                } else {
                    medStr = totalMed + " Records";
                }
            }

            String eligibilityStr = item.getEligibilityDecision(currentAttendanceView).getLabel();

            tableModel.addRow(new Object[]{
                    index++,
                    item.getStudentRegNo(),
                    item.getFullName(),
                    theoryStr,
                    practicalStr,
                    applicableStr,
                    medStr,
                    eligibilityStr,
                    "View"
            });
        }

        // Table status counter
        tableStatusLabel.setText("Showing " + displayedStudentList.size() + " of " + rawStudentList.size() + " students");

        // Empty state handling
        if (displayedStudentList.isEmpty()) {
            tableScrollPane.setViewportView(emptyStatePanel);
        } else {
            tableScrollPane.setViewportView(studentsTable);
        }
    }

    private static String formatPct(Double pct) {
        if (pct == null) return "N/A";
        return new DecimalFormat("0.0").format(pct) + "%";
    }

    // =========================================================================
    // EXPORT TO CSV FEATURE
    // =========================================================================

    private void exportEligibilityCSV() {
        if (displayedStudentList == null || displayedStudentList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No student records available to export for current criteria.", "Export Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        CourseInfo sel = (CourseInfo) courseSelector.getSelectedItem();
        String courseCode = (sel != null) ? sel.getCourseCode() : "Course";

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Examination Attendance Eligibility Report");
        chooser.setSelectedFile(new File("TecFAMS_Eligibility_" + courseCode.replaceAll("[^a-zA-Z0-9.-]", "_") + "_" + currentAttendanceView.name() + ".csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int ret = chooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("# TecFAMS - Faculty of Technology Academic Management System");
                writer.println("# Examination Attendance Eligibility Official Report");
                writer.println("# Course: " + (sel != null ? sel.toString() : "N/A"));
                writer.println("# Attendance Mode: " + currentAttendanceView.getDisplayName());
                writer.println("# Generated On: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                writer.println("# Total Students: " + displayedStudentList.size());
                writer.println();

                writer.println("Index,Student Reg No,Student Name,Theory Attended,Theory Held,Theory Pct,Practical Attended,Practical Held,Practical Pct,Selected View Attendance,Medical Records Count,Approved Medicals,Eligibility Status,Decision Reason");

                int idx = 1;
                for (StudentEligibilityItem item : displayedStudentList) {
                    String theoryPct = item.getTheoryPercentage() != null ? formatPct(item.getTheoryPercentage()) : "N/A";
                    String pracPct = item.getPracticalPercentage() != null ? formatPct(item.getPracticalPercentage()) : "N/A";
                    String appStr = item.getApplicableAttended(currentAttendanceView) + "/" + item.getApplicableHeld(currentAttendanceView) + " (" + item.getApplicablePercentageFormatted(currentAttendanceView) + ")";

                    writer.printf("\"%d\",\"%s\",\"%s\",\"%d\",\"%d\",\"%s\",\"%d\",\"%d\",\"%s\",\"%s\",\"%d\",\"%d\",\"%s\",\"%s\"%n",
                            idx++,
                            escapeCsv(item.getStudentRegNo()),
                            escapeCsv(item.getFullName()),
                            item.getTheoryAttended(),
                            item.getTheoryHeld(),
                            escapeCsv(theoryPct),
                            item.getPracticalAttended(),
                            item.getPracticalHeld(),
                            escapeCsv(pracPct),
                            escapeCsv(appStr),
                            item.getMedicalCount(),
                            item.getApprovedMedicalCount(),
                            escapeCsv(item.getEligibilityDecision(currentAttendanceView).getLabel()),
                            escapeCsv(item.getEligibilityReason(currentAttendanceView).replaceAll("<[^>]*>", ""))
                    );
                }

                int choice = JOptionPane.showConfirmDialog(
                        this,
                        "Successfully exported " + displayedStudentList.size() + " records to:\n" + file.getAbsolutePath() + "\n\nWould you like to open this file now?",
                        "Export Complete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE
                );

                if (choice == JOptionPane.YES_OPTION) {
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Could not launch spreadsheet application: " + ex.getMessage(), "Open File", JOptionPane.WARNING_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to write CSV file: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static String escapeCsv(String str) {
        if (str == null) return "";
        return str.replace("\"", "\"\"");
    }

    // =========================================================================
    // STUDENT DETAILS & MEDICAL RECORDS MODAL DIALOG
    // =========================================================================

    private void showStudentDetailsDialog() {
        int selectedRow = studentsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        showStudentDetailsDialog(selectedRow);
    }

    private void showStudentDetailsDialog(int selectedRow) {
        if (selectedRow < 0 || selectedRow >= displayedStudentList.size()) {
            return;
        }

        StudentEligibilityItem student = displayedStudentList.get(selectedRow);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Student Details & Examination Eligibility", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(640, 540);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(COLOR_WHITE);
        root.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Header Card with Student Reg No & Full Name
        JPanel studentHeader = new JPanel(new BorderLayout(14, 0));
        studentHeader.setOpaque(false);
        studentHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel nameLbl = new JLabel(student.getFullName());
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLbl.setForeground(COLOR_DEEP_NAVY);

        JLabel subMeta = new JLabel(student.getStudentRegNo() + " • " + student.getStudentStatus() +
                                    " • Academic Year " + student.getAcademicYear() + " • Course: " + student.getCourseCode());
        subMeta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subMeta.setForeground(COLOR_TEXT_SECONDARY);

        JPanel nameCol = new JPanel();
        nameCol.setLayout(new BoxLayout(nameCol, BoxLayout.Y_AXIS));
        nameCol.setOpaque(false);
        nameCol.add(nameLbl);
        nameCol.add(Box.createVerticalStrut(2));
        nameCol.add(subMeta);

        studentHeader.add(nameCol, BorderLayout.CENTER);
        root.add(studentHeader);
        root.add(Box.createVerticalStrut(12));

        // Tabbed Panel: Attendance Breakdown, Medical Records, Detailed Log
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));

        tabs.addTab("Attendance & Eligibility", new EligibilityVectorIcon(EligibilityVectorIcon.Type.LAYERS, 13, 13, COLOR_PRO_BLUE), createAttendanceTab(student));
        tabs.addTab("Medical Records (" + student.getMedicalCount() + ")", new EligibilityVectorIcon(EligibilityVectorIcon.Type.DOCUMENT, 13, 13, COLOR_TECH_BLUE), createMedicalTab(student));
        tabs.addTab("Session History", new EligibilityVectorIcon(EligibilityVectorIcon.Type.REFRESH, 13, 13, COLOR_DEEP_NAVY), createSessionHistoryTab(student));

        root.add(tabs);

        // Bottom Actions: Copy Reg No & Close
        JPanel bottomActions = new JPanel(new BorderLayout());
        bottomActions.setBackground(new Color(248, 250, 252));
        bottomActions.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));
        bottomActions.setBorder(new EmptyBorder(10, 16, 10, 16));

        ModernButton copyRegBtn = new ModernButton(
                "Copy Reg No",
                new EligibilityVectorIcon(EligibilityVectorIcon.Type.COPY, 12, 12),
                ModernButton.Style.SECONDARY
        );
        copyRegBtn.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(student.getStudentRegNo()), null);
            copyRegBtn.setText("Copied!");
            Timer t = new Timer(1500, evt -> copyRegBtn.setText("Copy Reg No"));
            t.setRepeats(false);
            t.start();
        });

        ModernButton closeBtn = new ModernButton("Close", ModernButton.Style.PRIMARY);
        closeBtn.addActionListener(e -> dialog.dispose());

        bottomActions.add(copyRegBtn, BorderLayout.WEST);
        bottomActions.add(closeBtn, BorderLayout.EAST);

        dialog.add(root, BorderLayout.CENTER);
        dialog.add(bottomActions, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel createAttendanceTab(StudentEligibilityItem student) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(COLOR_WHITE);
        p.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Attendance Metric Tiles
        JPanel tiles = new JPanel(new GridLayout(1, 3, 10, 0));
        tiles.setOpaque(false);
        tiles.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        tiles.add(createMiniStatTile("Theory Attendance",
                student.getTheoryAttended() + "/" + student.getTheoryHeld(),
                formatPct(student.getTheoryPercentage())));

        String pracRatio = "Theory_Only".equalsIgnoreCase(student.getCourseType()) ? "N/A" : student.getPracticalAttended() + "/" + student.getPracticalHeld();
        tiles.add(createMiniStatTile("Practical Attendance",
                pracRatio,
                student.getPracticalPercentage() != null ? formatPct(student.getPracticalPercentage()) : "N/A"));

        tiles.add(createMiniStatTile("Combined Attendance",
                student.getTotalAttended() + "/" + student.getTotalHeld(),
                student.getCombinedPercentage() != null ? formatPct(student.getCombinedPercentage()) : "N/A"));

        p.add(tiles);
        p.add(Box.createVerticalStrut(14));

        // Eligibility Decision Card
        JPanel decisionCard = new JPanel(new BorderLayout(14, 0));
        decisionCard.setOpaque(false);
        decisionCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(12, 14, 12, 14)
        ));

        EligibilityDecision decision = student.getEligibilityDecision(currentAttendanceView);
        Color decColor = (decision == EligibilityDecision.ELIGIBLE) ? COLOR_SUCCESS : COLOR_DANGER;

        JLabel decTitle = new JLabel("Official Decision: " + decision.getLabel().toUpperCase());
        decTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        decTitle.setForeground(decColor);

        JLabel reasonLbl = new JLabel("<html>" + student.getEligibilityReason(currentAttendanceView) + "</html>");
        reasonLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        reasonLbl.setForeground(COLOR_TEXT_PRIMARY);

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);
        textCol.add(decTitle);
        textCol.add(Box.createVerticalStrut(4));
        textCol.add(reasonLbl);

        decisionCard.add(textCol, BorderLayout.CENTER);
        p.add(decisionCard);
        p.add(Box.createVerticalStrut(12));

        // Contact & Bio Details
        JPanel bioCard = new JPanel(new GridLayout(2, 2, 8, 8));
        bioCard.setOpaque(false);
        bioCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(10, 14, 10, 14)
        ));

        bioCard.add(new JLabel("Email: " + (student.getEmail().isEmpty() ? "N/A" : student.getEmail())));
        bioCard.add(new JLabel("Phone: " + (student.getPhone().isEmpty() ? "N/A" : student.getPhone())));
        bioCard.add(new JLabel("Gender: " + student.getGender()));
        bioCard.add(new JLabel("Address: " + (student.getAddress().isEmpty() ? "N/A" : student.getAddress())));

        p.add(bioCard);
        return p;
    }

    private JPanel createMiniStatTile(String title, String ratio, String pct) {
        JPanel tile = new JPanel();
        tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
        tile.setBackground(COLOR_ROW_ALT);
        tile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        t.setForeground(COLOR_TEXT_SECONDARY);

        JLabel r = new JLabel(ratio + "  (" + pct + ")");
        r.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        r.setForeground(COLOR_DEEP_NAVY);

        tile.add(t);
        tile.add(Box.createVerticalStrut(3));
        tile.add(r);
        return tile;
    }

    private JPanel createMedicalTab(StudentEligibilityItem student) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(COLOR_WHITE);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        if (!student.hasMedicalRecords()) {
            JLabel empty = new JLabel("No medical records submitted by this undergraduate for this course.");
            empty.setFont(FONT_PAGE_SUBTITLE);
            empty.setForeground(COLOR_TEXT_SECONDARY);
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            p.add(empty, BorderLayout.CENTER);
            return p;
        }

        String[] cols = {"ID", "Leave Period", "Approval Status", "Reason", "Document", "Submitted Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        for (MedicalRecordItem m : student.getMedicalRecords()) {
            String period = m.getStartDate().equals(m.getEndDate()) ? m.getStartDate() : m.getStartDate() + " to " + m.getEndDate();
            model.addRow(new Object[]{
                    "#" + m.getMedicalId(),
                    period,
                    m.getApprovalStatus(),
                    m.getReason(),
                    m.getDocumentPath(),
                    m.getSubmittedDate()
            });
        }

        JTable table = new JTable(model);
        table.setFont(FONT_TABLE_CELL);
        table.setRowHeight(32);
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);
        table.getColumnModel().getColumn(5).setPreferredWidth(95);

        // Status badge renderer for medical approvals
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(FONT_BADGE);
                String st = value != null ? value.toString() : "";
                if ("Approved".equalsIgnoreCase(st)) {
                    lbl.setForeground(COLOR_SUCCESS);
                } else if ("Rejected".equalsIgnoreCase(st)) {
                    lbl.setForeground(COLOR_DANGER);
                } else {
                    lbl.setForeground(COLOR_WARNING);
                }
                return lbl;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private JPanel createSessionHistoryTab(StudentEligibilityItem student) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(COLOR_WHITE);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel loadingLbl = new JLabel("Loading session attendance history...");
        loadingLbl.setFont(FONT_PAGE_SUBTITLE);
        loadingLbl.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(loadingLbl, BorderLayout.CENTER);

        SwingWorker<List<AttendanceDetailItem>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<AttendanceDetailItem> doInBackground() throws Exception {
                return StudentDetailsEligibilityLogic.getStudentAttendanceDetails(student.getStudentId(), student.getCourseId());
            }

            @Override
            protected void done() {
                try {
                    List<AttendanceDetailItem> details = get();
                    p.removeAll();

                    String[] cols = {"Session #", "Date", "Type", "Duration", "Attendance", "Medical Exemption"};
                    DefaultTableModel model = new DefaultTableModel(cols, 0);

                    for (AttendanceDetailItem item : details) {
                        String covered = item.isMedicalCovered() ? "Yes (Approved Medical)" : "No";
                        model.addRow(new Object[]{
                                "Session " + item.getSessionNumber(),
                                item.getSessionDate(),
                                item.getSessionType(),
                                item.getDurationHours() + " hrs",
                                item.getAttendanceStatus(),
                                covered
                        });
                    }

                    JTable table = new JTable(model);
                    table.setFont(FONT_TABLE_CELL);
                    table.setRowHeight(30);

                    table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                            lbl.setHorizontalAlignment(SwingConstants.CENTER);
                            lbl.setFont(FONT_BADGE);
                            String st = value != null ? value.toString() : "";
                            if ("Present".equalsIgnoreCase(st)) {
                                lbl.setForeground(COLOR_SUCCESS);
                            } else if ("Absent".equalsIgnoreCase(st)) {
                                lbl.setForeground(COLOR_DANGER);
                            } else {
                                lbl.setForeground(COLOR_TEXT_MUTED);
                            }
                            return lbl;
                        }
                    });

                    JScrollPane sc = new JScrollPane(table);
                    sc.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
                    p.add(sc, BorderLayout.CENTER);
                    p.revalidate();
                    p.repaint();
                } catch (Exception ex) {
                    p.removeAll();
                    p.add(new JLabel("Could not load session history: " + ex.getMessage()), BorderLayout.CENTER);
                    p.revalidate();
                    p.repaint();
                }
            }
        };
        worker.execute();

        return p;
    }

    // =========================================================================
    // STATE VIEWS (Loading, Error, Unauthenticated)
    // =========================================================================

    private JPanel createLoadingView() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_PAGE_BG);

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);

        JProgressBar pbar = new JProgressBar();
        pbar.setIndeterminate(true);
        pbar.setPreferredSize(new Dimension(240, 8));
        pbar.setForeground(COLOR_PRO_BLUE);
        pbar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel text = new JLabel("Loading Undergraduate Attendance & Eligibility...");
        text.setFont(FONT_PAGE_SUBTITLE);
        text.setForeground(COLOR_DEEP_NAVY);
        text.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(text);
        box.add(Box.createVerticalStrut(14));
        box.add(pbar);

        panel.add(box);
        return panel;
    }

    private JPanel createErrorView() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_PAGE_BG);

        JPanel card = createWhiteCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(32, 40, 32, 40));
        card.setPreferredSize(new Dimension(520, 260));

        errorTitleLabel = new JLabel("Database Connection Error");
        errorTitleLabel.setFont(FONT_SECTION_TITLE);
        errorTitleLabel.setForeground(COLOR_DANGER);
        errorTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        errorDetailsArea = new JTextArea("Could not connect to MySQL server at localhost:3306.");
        errorDetailsArea.setWrapStyleWord(true);
        errorDetailsArea.setLineWrap(true);
        errorDetailsArea.setEditable(false);
        errorDetailsArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorDetailsArea.setForeground(COLOR_TEXT_SECONDARY);
        errorDetailsArea.setBackground(COLOR_PAGE_BG);
        errorDetailsArea.setAlignmentX(Component.CENTER_ALIGNMENT);

        ModernButton retryBtn = new ModernButton(
                "Retry Connection",
                new EligibilityVectorIcon(EligibilityVectorIcon.Type.REFRESH, 12, 12),
                ModernButton.Style.PRIMARY
        );
        retryBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        retryBtn.addActionListener(e -> loadCoursesAndData());

        card.add(errorTitleLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(errorDetailsArea);
        card.add(Box.createVerticalStrut(20));
        card.add(retryBtn);

        panel.add(card);
        return panel;
    }

    private JPanel createUnauthenticatedView() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_PAGE_BG);

        JPanel card = createWhiteCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(32, 40, 32, 40));
        card.setPreferredSize(new Dimension(500, 220));

        JLabel title = new JLabel("Authentication Required");
        title.setFont(FONT_SECTION_TITLE);
        title.setForeground(COLOR_DEEP_NAVY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("Please log in with an authenticated Lecturer account to view student eligibility.");
        desc.setFont(FONT_PAGE_SUBTITLE);
        desc.setForeground(COLOR_TEXT_SECONDARY);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        ModernButton loginBtn = new ModernButton("Go to Login", ModernButton.Style.PRIMARY);
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> {
            Window win = SwingUtilities.getWindowAncestor(this);
            if (win != null) win.dispose();
            try {
                Class<?> loginCls = Class.forName("main.java.ui.LoginUI");
                Object inst = loginCls.getDeclaredConstructor().newInstance();
                if (inst instanceof JFrame) ((JFrame) inst).setVisible(true);
            } catch (Exception ignored) {}
        });

        card.add(title);
        card.add(Box.createVerticalStrut(10));
        card.add(desc);
        card.add(Box.createVerticalStrut(22));
        card.add(loginBtn);

        panel.add(card);
        return panel;
    }

    private void showError(String msg) {
        if (errorDetailsArea != null) {
            errorDetailsArea.setText(msg);
        }
        cardLayout.show(stateContainer, VIEW_ERROR);
    }

    // =========================================================================
    // UI HELPERS & COMPONENT FACTORIES
    // =========================================================================

    private JPanel createWhiteCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.setColor(COLOR_BORDER);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    // Backward-compatible button creator
    private JButton createStyledButton(String text, Color bg, Color fg) {
        ModernButton.Style style = ModernButton.Style.PRIMARY;
        if (bg.equals(COLOR_WHITE) || (bg.getRed() > 230 && bg.getGreen() > 230 && bg.getBlue() > 230)) {
            style = ModernButton.Style.SECONDARY;
        }
        return new ModernButton(text, style);
    }

    // =========================================================================
    // CUSTOM MODERN VECTOR ICONS (Zero OS Emoji Dependency)
    // =========================================================================

    /**
     * EligibilityVectorIcon: High-DPI, vector graphics icon implementing javax.swing.Icon.
     * Guaranteed to render flawlessly across all Windows versions and Java runtimes.
     */
    public static class EligibilityVectorIcon implements Icon {
        public enum Type {
            REFRESH,
            RESET,
            EXPORT,
            EYE,
            BOOK,
            FLASK,
            LAYERS,
            SEARCH,
            CHECK,
            CROSS,
            MINUS,
            DOCUMENT,
            COPY,
            DOT_SUCCESS,
            DOT_WARNING,
            DOT_DANGER,
            DOT_BLUE,
            DOT_ORANGE,
            DOT_NAVY
        }

        private final Type type;
        private final int width;
        private final int height;
        private Color color;

        public EligibilityVectorIcon(Type type, int width, int height) {
            this(type, width, height, null);
        }

        public EligibilityVectorIcon(Type type, int width, int height, Color color) {
            this.type = type;
            this.width = width;
            this.height = height;
            this.color = color;
        }

        @Override public int getIconWidth() { return width; }
        @Override public int getIconHeight() { return height; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            Color paintColor = (this.color != null) ? this.color : (c != null ? c.getForeground() : COLOR_DEEP_NAVY);
            g2.setColor(paintColor);

            int cx = x + width / 2;
            int cy = y + height / 2;

            switch (type) {
                case REFRESH:
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int r = Math.min(width, height) - 4;
                    g2.drawArc(x + 2, y + 2, r, r, 45, 270);
                    int tipX = x + 2 + r / 2;
                    int tipY = y + 2;
                    Path2D arrow = new Path2D.Float();
                    arrow.moveTo(tipX + 3, tipY - 2);
                    arrow.lineTo(tipX + 3, tipY + 4);
                    arrow.lineTo(tipX - 2, tipY + 1);
                    arrow.closePath();
                    g2.fill(arrow);
                    break;

                case RESET:
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int rr = Math.min(width, height) - 4;
                    g2.drawArc(x + 2, y + 2, rr, rr, 135, -270);
                    int rTipX = x + 2 + rr / 2;
                    int rTipY = y + 2;
                    Path2D rArrow = new Path2D.Float();
                    rArrow.moveTo(rTipX - 3, rTipY - 2);
                    rArrow.lineTo(rTipX - 3, rTipY + 4);
                    rArrow.lineTo(rTipX + 2, rTipY + 1);
                    rArrow.closePath();
                    g2.fill(rArrow);
                    break;

                case EXPORT:
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(x + 1, y + height - 2, x + width - 2, y + height - 2);
                    g2.drawLine(x + 1, y + height - 5, x + 1, y + height - 2);
                    g2.drawLine(x + width - 2, y + height - 5, x + width - 2, y + height - 2);
                    g2.drawLine(cx, y + 1, cx, y + height - 5);
                    g2.drawLine(cx - 3, y + height - 8, cx, y + height - 5);
                    g2.drawLine(cx + 3, y + height - 8, cx, y + height - 5);
                    break;

                case EYE:
                    g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Path2D eye = new Path2D.Float();
                    eye.moveTo(x + 1, cy);
                    eye.curveTo(x + 3, cy - 4, x + width - 4, cy - 4, x + width - 1, cy);
                    eye.curveTo(x + width - 4, cy + 4, x + 3, cy + 4, x + 1, cy);
                    eye.closePath();
                    g2.draw(eye);
                    g2.fillOval(cx - 2, cy - 2, 4, 4);
                    break;

                case BOOK:
                    g2.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Path2D leftPage = new Path2D.Float();
                    leftPage.moveTo(cx, y + 2);
                    leftPage.curveTo(cx - 2, y + 1, x + 3, y + 1, x + 1, y + 2);
                    leftPage.lineTo(x + 1, y + height - 3);
                    leftPage.curveTo(x + 3, y + height - 4, cx - 2, y + height - 4, cx, y + height - 2);
                    leftPage.closePath();
                    g2.draw(leftPage);

                    Path2D rightPage = new Path2D.Float();
                    rightPage.moveTo(cx, y + 2);
                    rightPage.curveTo(cx + 2, y + 1, x + width - 4, y + 1, x + width - 2, y + 2);
                    rightPage.lineTo(x + width - 2, y + height - 3);
                    rightPage.curveTo(x + width - 4, y + height - 4, cx + 2, y + height - 4, cx, y + height - 2);
                    rightPage.closePath();
                    g2.draw(rightPage);
                    break;

                case FLASK:
                    g2.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Path2D flask = new Path2D.Float();
                    flask.moveTo(cx - 2, y + 1);
                    flask.lineTo(cx + 2, y + 1);
                    flask.moveTo(cx - 2, y + 2);
                    flask.lineTo(cx - 2, y + 4);
                    flask.lineTo(x + 2, y + height - 2);
                    flask.lineTo(x + width - 3, y + height - 2);
                    flask.lineTo(cx + 2, y + 4);
                    flask.lineTo(cx + 2, y + 2);
                    g2.draw(flask);
                    g2.drawLine(x + 4, y + height - 5, x + width - 5, y + height - 5);
                    break;

                case LAYERS:
                    g2.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawRoundRect(x + 1, y + 1, width - 5, height - 6, 2, 2);
                    g2.drawRoundRect(x + 4, y + 5, width - 5, height - 6, 2, 2);
                    break;

                case SEARCH:
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawOval(x + 1, y + 1, width - 6, height - 6);
                    g2.drawLine(x + width - 5, y + height - 5, x + width - 1, y + height - 1);
                    break;

                case CHECK:
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(x + 1, cy + 1, cx - 1, y + height - 3);
                    g2.drawLine(cx - 1, y + height - 3, x + width - 2, y + 2);
                    break;

                case CROSS:
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(x + 2, y + 2, x + width - 3, y + height - 3);
                    g2.drawLine(x + width - 3, y + 2, x + 2, y + height - 3);
                    break;

                case MINUS:
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(x + 2, cy, x + width - 3, cy);
                    break;

                case DOCUMENT:
                    g2.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawRoundRect(x + 2, y + 1, width - 5, height - 3, 2, 2);
                    g2.drawLine(x + 5, y + 5, x + width - 6, y + 5);
                    g2.drawLine(x + 5, y + 8, x + width - 6, y + 8);
                    break;

                case COPY:
                    g2.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawRoundRect(x + 4, y + 1, width - 5, height - 5, 2, 2);
                    g2.drawRoundRect(x + 1, y + 4, width - 5, height - 5, 2, 2);
                    break;

                case DOT_SUCCESS:
                    g2.setColor(COLOR_SUCCESS);
                    g2.fillOval(cx - 4, cy - 4, 8, 8);
                    break;

                case DOT_WARNING:
                    g2.setColor(COLOR_WARNING);
                    g2.fillOval(cx - 4, cy - 4, 8, 8);
                    break;

                case DOT_DANGER:
                    g2.setColor(COLOR_DANGER);
                    g2.fillOval(cx - 4, cy - 4, 8, 8);
                    break;

                case DOT_BLUE:
                    g2.setColor(COLOR_TECH_BLUE);
                    g2.fillOval(cx - 4, cy - 4, 8, 8);
                    break;

                case DOT_ORANGE:
                    g2.setColor(new Color(234, 88, 12));
                    g2.fillOval(cx - 4, cy - 4, 8, 8);
                    break;

                case DOT_NAVY:
                    g2.setColor(COLOR_DEEP_NAVY);
                    g2.fillOval(cx - 4, cy - 4, 8, 8);
                    break;
            }

            g2.dispose();
        }
    }

    // =========================================================================
    // RESPONSIVE WRAP LAYOUT (Prevents Overflow & Clipping on Laptops)
    // =========================================================================

    public static class WrapLayout extends FlowLayout {
        public WrapLayout() { super(FlowLayout.LEFT, 10, 8); }
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override public Dimension preferredLayoutSize(Container target) { return layoutSize(target, true); }
        @Override public Dimension minimumLayoutSize(Container target) { return layoutSize(target, false); }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getWidth();
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;

                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
                int maxWidth = targetWidth - horizontalInsetsAndGap;

                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0;
                int rowHeight = 0;

                int nmembers = target.getComponentCount();
                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowWidth + d.width > maxWidth) {
                            addRow(dim, rowWidth, rowHeight);
                            rowWidth = 0;
                            rowHeight = 0;
                        }
                        if (rowWidth != 0) rowWidth += hgap;
                        rowWidth += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }
                addRow(dim, rowWidth, rowHeight);

                dim.width += horizontalInsetsAndGap;
                dim.height += insets.top + insets.bottom + vgap * 2;
                return dim;
            }
        }

        private void addRow(Dimension dim, int rowWidth, int rowHeight) {
            dim.width = Math.max(dim.width, rowWidth);
            if (dim.height > 0) dim.height += getVgap();
            dim.height += rowHeight;
        }
    }

    // =========================================================================
    // CUSTOM MODERN UI COMPONENTS
    // =========================================================================

    /**
     * ModernButton: Professional Swing button with smooth hover, click feedback,
     * gradient rendering, and custom vector icons.
     */
    public static class ModernButton extends JButton {
        public enum Style { PRIMARY, SECONDARY, OUTLINE, SUCCESS, DANGER_OUTLINE }

        private final Style style;
        private boolean isHovered = false;
        private boolean isPressed = false;
        private int cornerRadius = 8;

        public ModernButton(String text, Style style) {
            this(text, null, style);
        }

        public ModernButton(String text, Icon icon, Style style) {
            super(text);
            this.style = style;
            if (icon != null) {
                setIcon(icon);
                setIconTextGap(7);
            }
            setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(7, 14, 7, 14));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { isHovered = false; isPressed = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { isPressed = true; repaint(); }
                @Override public void mouseReleased(MouseEvent e) { isPressed = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (!isEnabled()) {
                g2.setColor(new Color(241, 245, 249));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));
                g2.setColor(new Color(226, 232, 240));
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, cornerRadius, cornerRadius));
                setForeground(COLOR_TEXT_MUTED);
            } else {
                switch (style) {
                    case PRIMARY:
                        if (isPressed) {
                            g2.setColor(new Color(6, 36, 71));
                            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));
                        } else if (isHovered) {
                            GradientPaint gp = new GradientPaint(0, 0, new Color(20, 98, 190), 0, h, COLOR_PRO_BLUE);
                            g2.setPaint(gp);
                            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));
                        } else {
                            GradientPaint gp = new GradientPaint(0, 0, COLOR_PRO_BLUE, 0, h, COLOR_DEEP_NAVY);
                            g2.setPaint(gp);
                            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));
                        }
                        setForeground(COLOR_WHITE);
                        break;

                    case SECONDARY:
                        Color bg = isPressed ? new Color(224, 237, 254) : (isHovered ? new Color(240, 246, 255) : COLOR_WHITE);
                        g2.setColor(bg);
                        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));
                        g2.setColor(isHovered ? COLOR_TECH_BLUE : COLOR_BORDER);
                        g2.setStroke(new BasicStroke(1.2f));
                        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, cornerRadius, cornerRadius));
                        setForeground(isHovered ? COLOR_DEEP_NAVY : COLOR_PRO_BLUE);
                        break;

                    case OUTLINE:
                        Color obg = isPressed ? new Color(226, 232, 240) : (isHovered ? new Color(241, 245, 249) : COLOR_WHITE);
                        g2.setColor(obg);
                        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));
                        g2.setColor(isHovered ? new Color(148, 163, 184) : COLOR_BORDER);
                        g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, cornerRadius, cornerRadius));
                        setForeground(isHovered ? COLOR_TEXT_PRIMARY : COLOR_TEXT_SECONDARY);
                        break;

                    case SUCCESS:
                        if (isPressed) {
                            g2.setColor(new Color(4, 120, 87));
                            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));
                        } else if (isHovered) {
                            GradientPaint gp = new GradientPaint(0, 0, new Color(52, 211, 153), 0, h, COLOR_SUCCESS);
                            g2.setPaint(gp);
                            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));
                        } else {
                            GradientPaint gp = new GradientPaint(0, 0, COLOR_SUCCESS, 0, h, new Color(5, 150, 105));
                            g2.setPaint(gp);
                            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));
                        }
                        setForeground(COLOR_WHITE);
                        break;

                    case DANGER_OUTLINE:
                        Color dbg = isPressed ? new Color(254, 226, 226) : (isHovered ? new Color(254, 242, 242) : COLOR_WHITE);
                        g2.setColor(dbg);
                        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));
                        g2.setColor(isHovered ? new Color(248, 113, 113) : new Color(254, 202, 202));
                        g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, cornerRadius, cornerRadius));
                        setForeground(COLOR_DANGER);
                        break;
                }
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * ModernSegmentedViewSelector:
     * Capsule pill tab bar with vector icons for Theory Only, Practical Only, and Combined.
     */
    public class ModernSegmentedViewSelector extends JPanel {
        private AttendanceView selectedView = AttendanceView.THEORY_ONLY;
        private final SegmentPillButton theoryBtn;
        private final SegmentPillButton practicalBtn;
        private final SegmentPillButton combinedBtn;
        private final Consumer<AttendanceView> listener;

        public ModernSegmentedViewSelector(Consumer<AttendanceView> listener) {
            this.listener = listener;
            setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
            setOpaque(false);

            theoryBtn = new SegmentPillButton("Theory Only", new EligibilityVectorIcon(EligibilityVectorIcon.Type.BOOK, 13, 13), AttendanceView.THEORY_ONLY);
            practicalBtn = new SegmentPillButton("Practical Only", new EligibilityVectorIcon(EligibilityVectorIcon.Type.FLASK, 13, 13), AttendanceView.PRACTICAL_ONLY);
            combinedBtn = new SegmentPillButton("Combined", new EligibilityVectorIcon(EligibilityVectorIcon.Type.LAYERS, 13, 13), AttendanceView.COMBINED);

            add(theoryBtn);
            add(practicalBtn);
            add(combinedBtn);
            updateSelection();
        }

        public void setSelectedView(AttendanceView view) {
            this.selectedView = view;
            updateSelection();
        }

        public void setPracticalEnabled(boolean enabled) {
            practicalBtn.setEnabled(enabled);
            if (!enabled) {
                practicalBtn.setToolTipText("Course is Theory Only (no practical component)");
            } else {
                practicalBtn.setToolTipText(null);
            }
            updateSelection();
        }

        private void updateSelection() {
            theoryBtn.setSelected(selectedView == AttendanceView.THEORY_ONLY);
            practicalBtn.setSelected(selectedView == AttendanceView.PRACTICAL_ONLY);
            combinedBtn.setSelected(selectedView == AttendanceView.COMBINED);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            g2.setColor(new Color(238, 242, 248));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 20, 20));

            g2.setColor(new Color(222, 230, 240));
            g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 20, 20));

            g2.dispose();
            super.paintComponent(g);
        }

        private class SegmentPillButton extends JButton {
            private boolean isSelected = false;
            private boolean isHovered = false;

            public SegmentPillButton(String text, Icon icon, AttendanceView view) {
                super(text);
                if (icon != null) {
                    setIcon(icon);
                    setIconTextGap(6);
                }
                setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
                setFocusPainted(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setBorder(new EmptyBorder(5, 12, 5, 12));

                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
                });

                addActionListener(e -> {
                    if (isEnabled()) {
                        setSelectedView(view);
                        if (listener != null) listener.accept(view);
                    }
                });
            }

            public void setSelected(boolean sel) {
                this.isSelected = sel;
                repaint();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                if (!isEnabled()) {
                    setForeground(COLOR_TEXT_MUTED);
                } else if (isSelected) {
                    GradientPaint gp = new GradientPaint(0, 0, COLOR_PRO_BLUE, 0, h, COLOR_DEEP_NAVY);
                    g2.setPaint(gp);
                    g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 16, 16));
                    setForeground(COLOR_WHITE);
                } else {
                    if (isHovered) {
                        g2.setColor(new Color(226, 232, 240));
                        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 16, 16));
                        setForeground(COLOR_DEEP_NAVY);
                    } else {
                        setForeground(COLOR_TEXT_SECONDARY);
                    }
                }

                g2.dispose();
                super.paintComponent(g);
            }
        }
    }

    /**
     * ModernComboBox: Custom dropdown with vector chevron, focus glow, and vector-rendered items.
     */
    public static class ModernComboBox<T> extends JComboBox<T> {
        public ModernComboBox() {
            super();
            initStyle();
        }

        public ModernComboBox(T[] items) {
            super(items);
            initStyle();
        }

        public ModernComboBox(ComboBoxModel<T> model) {
            super(model);
            initStyle();
        }

        private void initStyle() {
            setFont(FONT_INPUT);
            setOpaque(false);
            setBackground(COLOR_WHITE);
            setUI(new ModernComboBoxUI(COLOR_BORDER, COLOR_TECH_BLUE, COLOR_PRO_BLUE));
            setRenderer(new ModernComboBoxRenderer());
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    public static class ModernComboBoxUI extends BasicComboBoxUI {
        private final Color normalBorderColor;
        private final Color hoverBorderColor;
        private final Color focusBorderColor;
        private boolean isHovered = false;

        public ModernComboBoxUI(Color normal, Color hover, Color focus) {
            this.normalBorderColor = normal;
            this.hoverBorderColor = hover;
            this.focusBorderColor = focus;
        }

        @Override
        protected JButton createArrowButton() {
            JButton btn = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = getWidth();
                    int h = getHeight();

                    if (getModel().isRollover() || isHovered) {
                        g2.setColor(new Color(235, 243, 254));
                        g2.fillOval(w / 2 - 10, h / 2 - 10, 20, 20);
                    }

                    g2.setColor(isEnabled() ? COLOR_PRO_BLUE : COLOR_TEXT_MUTED);
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                    int cx = w / 2;
                    int cy = h / 2 - 1;
                    Path2D chevron = new Path2D.Float();
                    chevron.moveTo(cx - 4, cy - 2);
                    chevron.lineTo(cx, cy + 2);
                    chevron.lineTo(cx + 4, cy - 2);
                    g2.draw(chevron);

                    g2.dispose();
                }
            };
            btn.setBorder(BorderFactory.createEmptyBorder());
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setOpaque(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return btn;
        }

        @Override
        protected void installListeners() {
            super.installListeners();
            comboBox.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    comboBox.repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    comboBox.repaint();
                }
            });
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = c.getWidth();
            int h = c.getHeight();

            g2.setColor(comboBox.isEnabled() ? COLOR_WHITE : new Color(245, 247, 250));
            g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 8, 8));

            if (comboBox.isPopupVisible() || comboBox.hasFocus()) {
                g2.setColor(new Color(40, 120, 208, 60));
                g2.setStroke(new BasicStroke(2.5f));
                g2.draw(new RoundRectangle2D.Float(1.5f, 1.5f, w - 3, h - 3, 8, 8));

                g2.setColor(focusBorderColor);
                g2.setStroke(new BasicStroke(1.3f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 8, 8));
            } else if (isHovered) {
                g2.setColor(hoverBorderColor);
                g2.setStroke(new BasicStroke(1.3f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 8, 8));
            } else {
                g2.setColor(normalBorderColor);
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 8, 8));
            }

            g2.dispose();

            hasFocus = comboBox.hasFocus();
            Rectangle r = rectangleForCurrentValue();
            paintCurrentValue(g, r, hasFocus);
        }

        @Override
        protected ComboPopup createPopup() {
            BasicComboPopup popup = new BasicComboPopup(comboBox) {
                @Override
                protected JScrollPane createScroller() {
                    JScrollPane scroller = super.createScroller();
                    scroller.setBorder(BorderFactory.createEmptyBorder());
                    scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                    scroller.getVerticalScrollBar().setUnitIncrement(14);
                    return scroller;
                }

                @Override
                public void paintBorder(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(normalBorderColor);
                    g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                    g2.dispose();
                }
            };
            popup.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                    BorderFactory.createEmptyBorder(4, 0, 4, 0)
            ));
            return popup;
        }
    }

    public static class ModernComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            lbl.setOpaque(true);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbl.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            lbl.setIconTextGap(8);

            if (isSelected) {
                lbl.setBackground(COLOR_ROW_SELECTED);
                lbl.setForeground(COLOR_DEEP_NAVY);
                lbl.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
            } else {
                lbl.setBackground(COLOR_WHITE);
                lbl.setForeground(COLOR_TEXT_PRIMARY);
            }

            // High-DPI Vector Icons per item type
            if (value instanceof CourseInfo) {
                CourseInfo c = (CourseInfo) value;
                lbl.setIcon(new EligibilityVectorIcon(EligibilityVectorIcon.Type.BOOK, 13, 13, COLOR_PRO_BLUE));
                lbl.setText(c.getCourseCode() + " - " + c.getCourseName());
            } else if (value instanceof Scenario) {
                Scenario s = (Scenario) value;
                switch (s) {
                    case ALL:
                        lbl.setIcon(new EligibilityVectorIcon(EligibilityVectorIcon.Type.LAYERS, 13, 13, COLOR_DEEP_NAVY));
                        lbl.setText("All Attendance Levels");
                        break;
                    case SCENARIO_1_ABOVE_80_NO_MED:
                        lbl.setIcon(new EligibilityVectorIcon(EligibilityVectorIcon.Type.DOT_SUCCESS, 12, 12));
                        lbl.setText("Above 80% (No Medical)");
                        break;
                    case SCENARIO_2_EXACTLY_80:
                        lbl.setIcon(new EligibilityVectorIcon(EligibilityVectorIcon.Type.DOT_WARNING, 12, 12));
                        lbl.setText("Exactly 80% Attendance");
                        break;
                    case SCENARIO_3_BELOW_80_NO_MED:
                        lbl.setIcon(new EligibilityVectorIcon(EligibilityVectorIcon.Type.DOT_DANGER, 12, 12));
                        lbl.setText("Below 80% (No Medical)");
                        break;
                    case SCENARIO_4_ABOVE_80_WITH_MED:
                        lbl.setIcon(new EligibilityVectorIcon(EligibilityVectorIcon.Type.DOT_BLUE, 12, 12));
                        lbl.setText("Above 80% (With Medical)");
                        break;
                    case SCENARIO_5_BELOW_80_WITH_MED:
                        lbl.setIcon(new EligibilityVectorIcon(EligibilityVectorIcon.Type.DOT_ORANGE, 12, 12));
                        lbl.setText("Below 80% (With Medical)");
                        break;
                    default:
                        lbl.setIcon(null);
                        lbl.setText(s.getLabel());
                }
            } else if (value instanceof EligibilityFilter) {
                EligibilityFilter ef = (EligibilityFilter) value;
                switch (ef) {
                    case ALL:
                        lbl.setIcon(new EligibilityVectorIcon(EligibilityVectorIcon.Type.LAYERS, 13, 13, COLOR_TEXT_SECONDARY));
                        lbl.setText(ef.getLabel());
                        break;
                    case MEETS_REQUIREMENT:
                        lbl.setIcon(new EligibilityVectorIcon(EligibilityVectorIcon.Type.CHECK, 13, 13, COLOR_SUCCESS));
                        lbl.setText(ef.getLabel());
                        break;
                    case BELOW_REQUIREMENT:
                        lbl.setIcon(new EligibilityVectorIcon(EligibilityVectorIcon.Type.CROSS, 13, 13, COLOR_DANGER));
                        lbl.setText(ef.getLabel());
                        break;
                }
            } else if (value instanceof MedicalFilter) {
                MedicalFilter mf = (MedicalFilter) value;
                switch (mf) {
                    case ALL:
                        lbl.setIcon(new EligibilityVectorIcon(EligibilityVectorIcon.Type.DOCUMENT, 13, 13, COLOR_TEXT_SECONDARY));
                        lbl.setText(mf.getLabel());
                        break;
                    case HAS_MEDICAL:
                        lbl.setIcon(new EligibilityVectorIcon(EligibilityVectorIcon.Type.DOCUMENT, 13, 13, COLOR_TECH_BLUE));
                        lbl.setText(mf.getLabel());
                        break;
                    case NO_MEDICAL:
                        lbl.setIcon(new EligibilityVectorIcon(EligibilityVectorIcon.Type.MINUS, 13, 13, COLOR_TEXT_MUTED));
                        lbl.setText(mf.getLabel());
                        break;
                }
            }

            return lbl;
        }
    }

    /**
     * ModernSearchField: Rounded search textfield with vector magnifying glass,
     * focus glow, and clear (✕) button.
     */
    public static class ModernSearchField extends JTextField {
        private String placeholder = "Search student name or reg no...";
        private final JButton clearBtn;

        public ModernSearchField() {
            setFont(FONT_INPUT);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(5, 28, 5, 24));

            clearBtn = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isRollover()) {
                        g2.setColor(new Color(226, 232, 240));
                        g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
                    }
                    g2.setColor(COLOR_TEXT_MUTED);
                    g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;
                    g2.drawLine(cx - 3, cy - 3, cx + 3, cy + 3);
                    g2.drawLine(cx + 3, cy - 3, cx - 3, cy + 3);
                    g2.dispose();
                }
            };
            clearBtn.setContentAreaFilled(false);
            clearBtn.setBorderPainted(false);
            clearBtn.setFocusPainted(false);
            clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            clearBtn.setVisible(false);
            clearBtn.addActionListener(e -> {
                setText("");
                requestFocusInWindow();
            });

            setLayout(new BorderLayout());
            JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 3));
            rightBox.setOpaque(false);
            rightBox.add(clearBtn);
            add(rightBox, BorderLayout.EAST);

            getDocument().addDocumentListener(new DocumentListener() {
                private void update() { clearBtn.setVisible(!getText().isEmpty()); }
                public void insertUpdate(DocumentEvent e) { update(); }
                public void removeUpdate(DocumentEvent e) { update(); }
                public void changedUpdate(DocumentEvent e) { update(); }
            });
        }

        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(COLOR_WHITE);
            g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 8, 8));

            if (hasFocus()) {
                g2.setColor(new Color(40, 120, 208, 60));
                g2.setStroke(new BasicStroke(2.5f));
                g2.draw(new RoundRectangle2D.Float(1.5f, 1.5f, w - 3, h - 3, 8, 8));

                g2.setColor(COLOR_TECH_BLUE);
                g2.setStroke(new BasicStroke(1.3f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 8, 8));
            } else {
                g2.setColor(COLOR_BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 8, 8));
            }

            // Draw Vector Magnifying Glass
            int cy = h / 2;
            g2.setColor(COLOR_TEXT_MUTED);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(8, cy - 5, 8, 8);
            g2.drawLine(15, cy + 2, 19, cy + 6);

            // Placeholder Text
            if (getText().isEmpty() && !hasFocus()) {
                g2.setColor(COLOR_TEXT_MUTED);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(placeholder, 28, (h + fm.getAscent() - fm.getDescent()) / 2);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * InteractiveMetricCard: Card component with subtle hover lift and click-to-filter support.
     */
    private static class InteractiveMetricCard extends JPanel {
        private final Color accentColor;
        private final Runnable clickAction;
        private boolean isHovered = false;

        public InteractiveMetricCard(Color accentColor, Runnable clickAction) {
            this.accentColor = accentColor;
            this.clickAction = clickAction;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (clickAction != null) {
                        clickAction.run();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(COLOR_WHITE);
            g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 10, 10));

            if (isHovered) {
                g2.setColor(accentColor);
                g2.setStroke(new BasicStroke(1.4f));
            } else {
                g2.setColor(COLOR_BORDER);
                g2.setStroke(new BasicStroke(1.0f));
            }
            g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 10, 10));

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * ModernTableActionRenderer: Renders an eye icon and pill in the action column.
     */
    private static class ModernTableActionRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel container = new JPanel(new GridBagLayout());
            container.setOpaque(true);
            container.setBackground(isSelected ? COLOR_ROW_SELECTED : (row % 2 == 1 ? COLOR_ROW_ALT : COLOR_WHITE));

            JPanel pill = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth();
                    int h = getHeight();

                    g2.setColor(new Color(235, 243, 254));
                    g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));
                    g2.setColor(new Color(191, 219, 254));
                    g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 14, 14));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            pill.setOpaque(false);
            pill.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 2));

            JLabel lbl = new JLabel("View Profile →");
            lbl.setIcon(new EligibilityVectorIcon(EligibilityVectorIcon.Type.EYE, 12, 12, COLOR_PRO_BLUE));
            lbl.setIconTextGap(5);
            lbl.setFont(new Font("Segoe UI Semibold", Font.BOLD, 11));
            lbl.setForeground(COLOR_PRO_BLUE);
            pill.add(lbl);

            container.add(pill);
            return container;
        }
    }

    /**
     * ModernTableActionEditor: Interactive cell editor allowing single-click opening of student profiles.
     */
    private static class ModernTableActionEditor extends DefaultCellEditor {
        private final JButton editBtn;
        private int currentRow = -1;

        public ModernTableActionEditor(Consumer<Integer> onAction) {
            super(new JCheckBox());
            editBtn = new JButton();
            editBtn.setOpaque(false);
            editBtn.setContentAreaFilled(false);
            editBtn.setBorderPainted(false);
            editBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            editBtn.addActionListener(e -> {
                int rowToOpen = currentRow;
                fireEditingStopped();
                if (rowToOpen >= 0 && onAction != null) {
                    SwingUtilities.invokeLater(() -> onAction.accept(rowToOpen));
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.currentRow = row;
            JPanel container = new JPanel(new GridBagLayout());
            container.setOpaque(true);
            container.setBackground(COLOR_ROW_SELECTED);

            JPanel pill = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth();
                    int h = getHeight();
                    GradientPaint gp = new GradientPaint(0, 0, COLOR_PRO_BLUE, 0, h, COLOR_DEEP_NAVY);
                    g2.setPaint(gp);
                    g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            pill.setOpaque(false);
            pill.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 2));

            JLabel lbl = new JLabel("View Profile →");
            lbl.setIcon(new EligibilityVectorIcon(EligibilityVectorIcon.Type.EYE, 12, 12, Color.WHITE));
            lbl.setIconTextGap(5);
            lbl.setFont(new Font("Segoe UI Semibold", Font.BOLD, 11));
            lbl.setForeground(Color.WHITE);
            pill.add(lbl);

            editBtn.removeAll();
            editBtn.setLayout(new BorderLayout());
            editBtn.add(pill, BorderLayout.CENTER);

            container.add(editBtn);
            return container;
        }

        @Override
        public Object getCellEditorValue() {
            return "View";
        }
    }

    // Standalone Runner for Development and Verification
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            JFrame frame = new JFrame("TecFAMS - Student Details & Eligibility");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // Default typical laptop resolution test (1200 x 720)
            frame.setSize(1200, 720);
            frame.setMinimumSize(new Dimension(860, 600));
            frame.setLocationRelativeTo(null);

            if (UserSession.getCurrentSession() == null) {
                UserSession.setCurrentSession(new UserSession(
                        4, "lec_chinthaka", "Lecturer",
                        "Dr. Chinthaka Premachandra", "Senior Lecturer Gr. I",
                        "Department of Information and Communication Technology",
                        "chinthaka@fot.ruh.ac.lk"
                ));
            }

            StudentDetailsEligibility view = new StudentDetailsEligibility();
            frame.add(view);
            frame.setVisible(true);
        });
    }
}
