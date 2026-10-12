package main.java.ui.Lecturer;

import main.java.model.Lecturer.StudentDetailsEligibilityLogic;
import main.java.model.Lecturer.StudentDetailsEligibilityLogic.*;
import main.java.model.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * StudentDetailsEligibility.java
 * TecFAMS - Faculty of Technology Academic Management System
 *
 * Professional Java Swing UI component for Lecturer Portal:
 * Student Details & Examination Attendance Eligibility Management.
 *
 * Designed to provide university lecturers with a clear, reliable,
 * and high-contrast interface to review student information, inspect
 * attendance records across Theory/Practical/Combined modes, review
 * submitted medicals, and verify exam admission criteria.
 */
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
    public static final Font FONT_PAGE_TITLE       = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_PAGE_SUBTITLE    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_CARD_LABEL       = new Font("Segoe UI Semibold", Font.BOLD, 12);
    public static final Font FONT_CARD_COUNT       = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_SECTION_TITLE    = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_INPUT            = new Font("Segoe UI", Font.PLAIN, 13);
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
    private JComboBox<CourseInfo> courseSelector;
    private JTextField searchField;
    private JComboBox<Scenario> scenarioSelector;
    private JComboBox<EligibilityFilter> eligibilityFilterCombo;
    private JComboBox<MedicalFilter> medicalFilterCombo;

    // Attendance View Selector Buttons
    private JButton theoryViewBtn;
    private JButton practicalViewBtn;
    private JButton combinedViewBtn;
    private AttendanceView currentAttendanceView = AttendanceView.THEORY_ONLY;

    // Summary Card Labels
    private JLabel totalStudentsLabel;
    private JLabel meetsRequirementLabel;
    private JLabel belowRequirementLabel;
    private JLabel withMedicalsLabel;

    // Table & Model
    private JTable studentsTable;
    private DefaultTableModel tableModel;
    private JLabel tableStatusLabel;
    private JPanel emptyStatePanel;
    private JScrollPane tableScrollPane;

    // Action Buttons
    private JButton viewDetailsBtn;
    private JButton refreshBtn;
    private JButton resetFiltersBtn;

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
    // SUCCESS VIEW ASSEMBLY
    // =========================================================================

    private JPanel createSuccessView() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(COLOR_PAGE_BG);
        mainPanel.setBorder(new EmptyBorder(22, 28, 24, 28));

        // 1. Header Banner
        mainPanel.add(createHeaderSection());
        mainPanel.add(Box.createVerticalStrut(18));

        // 2. Summary Statistics Cards
        mainPanel.add(createSummaryCardsSection());
        mainPanel.add(Box.createVerticalStrut(18));

        // 3. Filter Controls & Attendance View Selector
        mainPanel.add(createFilterPanel());
        mainPanel.add(Box.createVerticalStrut(18));

        // 4. Main Students Table Card
        mainPanel.add(createTableSection());

        JScrollPane outerScroll = new JScrollPane(mainPanel);
        outerScroll.setBorder(null);
        outerScroll.getVerticalScrollBar().setUnitIncrement(16);
        outerScroll.setOpaque(false);
        outerScroll.getViewport().setOpaque(false);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(outerScroll, BorderLayout.CENTER);
        return wrapper;
    }

    // Header section
    private JPanel createHeaderSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
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
        left.add(Box.createVerticalStrut(3));
        left.add(subtitle);

        panel.add(left, BorderLayout.WEST);
        return panel;
    }

    // =========================================================================
    // SUMMARY CARDS SECTION
    // =========================================================================

    private JPanel createSummaryCardsSection() {
        JPanel container = new JPanel(new GridLayout(1, 4, 16, 0));
        container.setOpaque(false);
        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        container.setAlignmentX(Component.LEFT_ALIGNMENT);

        totalStudentsLabel = new JLabel("—");
        meetsRequirementLabel = new JLabel("—");
        belowRequirementLabel = new JLabel("—");
        withMedicalsLabel = new JLabel("—");

        container.add(createMetricCard("Total Enrolled Students", totalStudentsLabel, "Active course candidates", COLOR_PRO_BLUE));
        container.add(createMetricCard("Meets Requirement (≥ 80%)", meetsRequirementLabel, "Exam admission eligible", COLOR_SUCCESS));
        container.add(createMetricCard("Below Requirement (< 80%)", belowRequirementLabel, "Requires review or medicals", COLOR_DANGER));
        container.add(createMetricCard("With Medical Records", withMedicalsLabel, "Official leave certificates", COLOR_WARNING));

        return container;
    }

    private JPanel createMetricCard(String labelText, JLabel countLabel, String subtitleText, Color accentColor) {
        JPanel card = createWhiteCardPanel();
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        // Left accent stripe
        JPanel stripe = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                g2.fill(new RoundRectangle2D.Float(0, 4, 4, getHeight() - 8, 4, 4));
                g2.dispose();
            }
        };
        stripe.setPreferredSize(new Dimension(6, 40));
        stripe.setOpaque(false);

        // Content
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_CARD_LABEL);
        lbl.setForeground(COLOR_TEXT_SECONDARY);

        countLabel.setFont(FONT_CARD_COUNT);
        countLabel.setForeground(COLOR_DEEP_NAVY);

        JLabel sub = new JLabel(subtitleText);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(COLOR_TEXT_MUTED);

        textPanel.add(lbl);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(countLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(sub);

        card.add(stripe, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // FILTER CONTROLS & ATTENDANCE VIEW SELECTOR
    // =========================================================================

    private JPanel createFilterPanel() {
        JPanel card = createWhiteCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Row 1: Course Selector and Attendance View Toggle
        JPanel row1 = new JPanel(new BorderLayout(16, 0));
        row1.setOpaque(false);

        // Left: Course Selector
        JPanel coursePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        coursePanel.setOpaque(false);
        JLabel courseLbl = new JLabel("Assigned Course:");
        courseLbl.setFont(FONT_CARD_LABEL);
        courseLbl.setForeground(COLOR_DEEP_NAVY);

        courseSelector = new JComboBox<>();
        courseSelector.setFont(FONT_INPUT);
        courseSelector.setPreferredSize(new Dimension(380, 36));
        courseSelector.addActionListener(e -> {
            CourseInfo sel = (CourseInfo) courseSelector.getSelectedItem();
            if (sel != null) {
                updateViewSelectorAvailability(sel);
                loadStudentEligibilityData(sel.getCourseId());
            }
        });
        coursePanel.add(courseLbl);
        coursePanel.add(courseSelector);

        // Right: Attendance View Selector Buttons (Theory, Practical, Combined)
        JPanel viewTogglePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        viewTogglePanel.setOpaque(false);

        JLabel viewLbl = new JLabel("Attendance View:");
        viewLbl.setFont(FONT_CARD_LABEL);
        viewLbl.setForeground(COLOR_DEEP_NAVY);
        viewTogglePanel.add(viewLbl);

        theoryViewBtn = createSegmentedButton("Theory Only", true);
        practicalViewBtn = createSegmentedButton("Practical Only", false);
        combinedViewBtn = createSegmentedButton("Combined (Theory + Practical)", false);

        theoryViewBtn.addActionListener(e -> setAttendanceView(AttendanceView.THEORY_ONLY));
        practicalViewBtn.addActionListener(e -> setAttendanceView(AttendanceView.PRACTICAL_ONLY));
        combinedViewBtn.addActionListener(e -> setAttendanceView(AttendanceView.COMBINED));

        viewTogglePanel.add(theoryViewBtn);
        viewTogglePanel.add(practicalViewBtn);
        viewTogglePanel.add(combinedViewBtn);

        row1.add(coursePanel, BorderLayout.WEST);
        row1.add(viewTogglePanel, BorderLayout.EAST);

        // Row 2: Search, Scenario Filter, Eligibility Filter, Medical Filter, Reset
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        row2.setOpaque(false);

        // Search Field
        searchField = new JTextField();
        searchField.setFont(FONT_INPUT);
        searchField.setPreferredSize(new Dimension(200, 34));
        searchField.putClientProperty("JTextField.placeholderText", "Search student name or reg no...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        // Scenario Selector (All 5 Required Scenarios)
        scenarioSelector = new JComboBox<>(Scenario.values());
        scenarioSelector.setFont(FONT_INPUT);
        scenarioSelector.setPreferredSize(new Dimension(250, 34));
        scenarioSelector.addActionListener(e -> applyFilters());

        // Eligibility Filter
        eligibilityFilterCombo = new JComboBox<>(EligibilityFilter.values());
        eligibilityFilterCombo.setFont(FONT_INPUT);
        eligibilityFilterCombo.setPreferredSize(new Dimension(190, 34));
        eligibilityFilterCombo.addActionListener(e -> applyFilters());

        // Medical Filter
        medicalFilterCombo = new JComboBox<>(MedicalFilter.values());
        medicalFilterCombo.setFont(FONT_INPUT);
        medicalFilterCombo.setPreferredSize(new Dimension(180, 34));
        medicalFilterCombo.addActionListener(e -> applyFilters());

        // Reset Filters Button
        resetFiltersBtn = createStyledButton("Reset Filters", new Color(241, 245, 249), COLOR_TEXT_PRIMARY);
        resetFiltersBtn.setPreferredSize(new Dimension(110, 34));
        resetFiltersBtn.addActionListener(e -> resetAllFilters());

        row2.add(new JLabel("Search:"));
        row2.add(searchField);
        row2.add(scenarioSelector);
        row2.add(eligibilityFilterCombo);
        row2.add(medicalFilterCombo);
        row2.add(resetFiltersBtn);

        card.add(row1);
        card.add(Box.createVerticalStrut(14));
        card.add(row2);
        return card;
    }

    private void updateViewSelectorAvailability(CourseInfo course) {
        if (course == null) return;
        boolean hasPractical = course.hasPracticalComponent();

        practicalViewBtn.setEnabled(hasPractical);
        if (!hasPractical) {
            practicalViewBtn.setToolTipText("This course is Theory Only (no practical component).");
            if (currentAttendanceView == AttendanceView.PRACTICAL_ONLY) {
                setAttendanceView(AttendanceView.THEORY_ONLY);
            }
        } else {
            practicalViewBtn.setToolTipText(null);
        }
    }

    private void setAttendanceView(AttendanceView view) {
        this.currentAttendanceView = view;
        updateSegmentedButtonStyles();
        applyFilters();
    }

    private void updateSegmentedButtonStyles() {
        styleSegmentedButton(theoryViewBtn, currentAttendanceView == AttendanceView.THEORY_ONLY);
        styleSegmentedButton(practicalViewBtn, currentAttendanceView == AttendanceView.PRACTICAL_ONLY);
        styleSegmentedButton(combinedViewBtn, currentAttendanceView == AttendanceView.COMBINED);
    }

    private JButton createSegmentedButton(String text, boolean selected) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        styleSegmentedButton(btn, selected);
        return btn;
    }

    private void styleSegmentedButton(JButton btn, boolean selected) {
        if (!btn.isEnabled()) {
            btn.setBackground(new Color(241, 245, 249));
            btn.setForeground(COLOR_TEXT_MUTED);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_BORDER, 1),
                    new EmptyBorder(6, 12, 6, 12)
            ));
            return;
        }

        if (selected) {
            btn.setBackground(COLOR_PRO_BLUE);
            btn.setForeground(COLOR_WHITE);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_PRO_BLUE, 1),
                    new EmptyBorder(6, 12, 6, 12)
            ));
        } else {
            btn.setBackground(COLOR_WHITE);
            btn.setForeground(COLOR_TEXT_PRIMARY);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_BORDER, 1),
                    new EmptyBorder(6, 12, 6, 12)
            ));
        }
    }

    private void resetAllFilters() {
        searchField.setText("");
        scenarioSelector.setSelectedIndex(0);
        eligibilityFilterCombo.setSelectedIndex(0);
        medicalFilterCombo.setSelectedIndex(0);
        applyFilters();
    }

    // =========================================================================
    // STUDENT DETAILS TABLE SECTION
    // =========================================================================

    private JPanel createTableSection() {
        JPanel card = createWhiteCardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 22, 18, 22));
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
                return false;
            }
        };

        studentsTable = new JTable(tableModel);
        studentsTable.setFont(FONT_TABLE_CELL);
        studentsTable.setRowHeight(40);
        studentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentsTable.setShowVerticalLines(false);
        studentsTable.setShowHorizontalLines(true);
        studentsTable.setGridColor(new Color(241, 245, 249));
        studentsTable.setSelectionBackground(COLOR_ROW_SELECTED);
        studentsTable.setSelectionForeground(COLOR_DEEP_NAVY);

        // Column Preferred Widths
        studentsTable.getColumnModel().getColumn(0).setPreferredWidth(45);  // #
        studentsTable.getColumnModel().getColumn(1).setPreferredWidth(125); // Reg No
        studentsTable.getColumnModel().getColumn(2).setPreferredWidth(210); // Name
        studentsTable.getColumnModel().getColumn(3).setPreferredWidth(130); // Theory
        studentsTable.getColumnModel().getColumn(4).setPreferredWidth(130); // Practical
        studentsTable.getColumnModel().getColumn(5).setPreferredWidth(160); // Selected View
        studentsTable.getColumnModel().getColumn(6).setPreferredWidth(135); // Medical Records
        studentsTable.getColumnModel().getColumn(7).setPreferredWidth(125); // Eligibility
        studentsTable.getColumnModel().getColumn(8).setPreferredWidth(110); // Actions

        // Header Styling
        JTableHeader header = studentsTable.getTableHeader();
        header.setFont(FONT_TABLE_HEADER);
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(COLOR_DEEP_NAVY);
        header.setPreferredSize(new Dimension(0, 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

        // Custom Renderers for Badges & Status
        setupTableRenderers();

        // Double-click row opens Details Dialog
        studentsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && studentsTable.getSelectedRow() != -1) {
                    showStudentDetailsDialog();
                }
            }
        });

        tableScrollPane = new JScrollPane(studentsTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        tableScrollPane.getViewport().setBackground(COLOR_WHITE);

        // Empty State Panel
        emptyStatePanel = createEmptyStatePanel();

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setOpaque(false);

        refreshBtn = createStyledButton("Refresh", new Color(241, 245, 249), COLOR_TEXT_PRIMARY);
        refreshBtn.addActionListener(e -> {
            CourseInfo sel = (CourseInfo) courseSelector.getSelectedItem();
            if (sel != null) loadStudentEligibilityData(sel.getCourseId());
        });

        viewDetailsBtn = createStyledButton("View Details", COLOR_PRO_BLUE, COLOR_WHITE);
        viewDetailsBtn.addActionListener(e -> showStudentDetailsDialog());

        bottomBar.add(refreshBtn);
        bottomBar.add(viewDetailsBtn);

        card.add(topBar, BorderLayout.NORTH);
        card.add(Box.createVerticalStrut(10), BorderLayout.CENTER);
        card.add(tableScrollPane, BorderLayout.CENTER);
        card.add(bottomBar, BorderLayout.SOUTH);

        return card;
    }

    private void setupTableRenderers() {
        // Center alignment for index and badges
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
                        lbl.setForeground(new Color(4, 120, 87)); // Dark green
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
                    lbl.setForeground(new Color(16, 149, 193)); // Cyan / medical blue
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
                    lbl.setText("  ✔ ELIGIBLE  ");
                } else if ("Not Eligible".equalsIgnoreCase(status)) {
                    lbl.setForeground(COLOR_DANGER);
                    lbl.setText("  ✖ NOT ELIGIBLE  ");
                } else {
                    lbl.setForeground(COLOR_TEXT_MUTED);
                    lbl.setText("  " + status + "  ");
                }
                return lbl;
            }
        });

        // Action column: View Details hint
        studentsTable.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                lbl.setForeground(COLOR_TECH_BLUE);
                lbl.setText("View Details ➜");
                return lbl;
            }
        });
    }

    private JPanel createEmptyStatePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(800, 200));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel iconLabel = new JLabel("📋");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msgLabel = new JLabel("No Students Found For Current Criteria");
        msgLabel.setFont(FONT_SECTION_TITLE);
        msgLabel.setForeground(COLOR_DEEP_NAVY);
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hintLabel = new JLabel("Adjust or reset your filters above to view enrolled students.");
        hintLabel.setFont(FONT_PAGE_SUBTITLE);
        hintLabel.setForeground(COLOR_TEXT_SECONDARY);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(iconLabel);
        inner.add(Box.createVerticalStrut(8));
        inner.add(msgLabel);
        inner.add(Box.createVerticalStrut(4));
        inner.add(hintLabel);

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
                        // Lecturer has no assigned courses
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
    // STUDENT DETAILS & MEDICAL RECORDS MODAL DIALOG
    // =========================================================================

    private void showStudentDetailsDialog() {
        int selectedRow = studentsTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= displayedStudentList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StudentEligibilityItem student = displayedStudentList.get(selectedRow);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Student Details & Examination Eligibility", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(640, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(COLOR_WHITE);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header Card with Student Reg No & Full Name
        JPanel studentHeader = new JPanel(new BorderLayout(14, 0));
        studentHeader.setOpaque(false);
        studentHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel nameLbl = new JLabel(student.getFullName());
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        nameLbl.setForeground(COLOR_DEEP_NAVY);

        JLabel subMeta = new JLabel(student.getStudentRegNo() + " • " + student.getStudentStatus() +
                                    " • Academic Year " + student.getAcademicYear() + " • Course: " + student.getCourseCode());
        subMeta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subMeta.setForeground(COLOR_TEXT_SECONDARY);

        JPanel nameCol = new JPanel();
        nameCol.setLayout(new BoxLayout(nameCol, BoxLayout.Y_AXIS));
        nameCol.setOpaque(false);
        nameCol.add(nameLbl);
        nameCol.add(Box.createVerticalStrut(3));
        nameCol.add(subMeta);

        studentHeader.add(nameCol, BorderLayout.CENTER);
        root.add(studentHeader);
        root.add(Box.createVerticalStrut(14));

        // Tabbed Panel: Attendance Breakdown, Medical Records, Detailed Log
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));

        // Tab 1: Attendance & Eligibility Breakdown
        tabs.addTab("Attendance & Eligibility", createAttendanceTab(student));

        // Tab 2: Medical Records
        tabs.addTab("Medical Records (" + student.getMedicalCount() + ")", createMedicalTab(student));

        // Tab 3: Session Attendance History
        tabs.addTab("Session History", createSessionHistoryTab(student));

        root.add(tabs);

        // Close action
        JPanel bottomActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomActions.setBackground(new Color(248, 250, 252));
        bottomActions.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));

        JButton closeBtn = createStyledButton("Close", COLOR_PRO_BLUE, COLOR_WHITE);
        closeBtn.addActionListener(e -> dialog.dispose());
        bottomActions.add(closeBtn);

        dialog.add(root, BorderLayout.CENTER);
        dialog.add(bottomActions, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel createAttendanceTab(StudentEligibilityItem student) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(COLOR_WHITE);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Attendance Metric Tiles
        JPanel tiles = new JPanel(new GridLayout(1, 3, 12, 0));
        tiles.setOpaque(false);
        tiles.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

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
        p.add(Box.createVerticalStrut(16));

        // Eligibility Decision Card
        JPanel decisionCard = new JPanel(new BorderLayout(14, 0));
        decisionCard.setOpaque(false);
        decisionCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(14, 16, 14, 16)
        ));

        EligibilityDecision decision = student.getEligibilityDecision(currentAttendanceView);
        Color decColor = (decision == EligibilityDecision.ELIGIBLE) ? COLOR_SUCCESS : COLOR_DANGER;

        JLabel decTitle = new JLabel("Official Decision: " + decision.getLabel().toUpperCase());
        decTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
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
        p.add(Box.createVerticalStrut(14));

        // Contact & Bio Details
        JPanel bioCard = new JPanel(new GridLayout(2, 2, 8, 8));
        bioCard.setOpaque(false);
        bioCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(12, 16, 12, 16)
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
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        t.setForeground(COLOR_TEXT_SECONDARY);

        JLabel r = new JLabel(ratio + "  (" + pct + ")");
        r.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        r.setForeground(COLOR_DEEP_NAVY);

        tile.add(t);
        tile.add(Box.createVerticalStrut(4));
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

                    // Attendance status color renderer
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

        JButton retryBtn = createStyledButton("Retry Connection", COLOR_PRO_BLUE, COLOR_WHITE);
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

        JButton loginBtn = createStyledButton("Go to Login", COLOR_PRO_BLUE, COLOR_WHITE);
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

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(7, 14, 7, 14)
        ));
        return btn;
    }

    // Standalone Runner for Development and Verification
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            JFrame frame = new JFrame("TecFAMS - Student Details & Eligibility");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1180, 760);
            frame.setLocationRelativeTo(null);

            // Mock session if none active
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
