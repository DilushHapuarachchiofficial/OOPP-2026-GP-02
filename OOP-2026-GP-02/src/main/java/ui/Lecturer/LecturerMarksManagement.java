package main.java.ui.Lecturer;

import main.java.model.Lecturer.AssessmentDAO;
import main.java.model.Lecturer.AssessmentDAO.AssessmentComponent;
import main.java.model.Lecturer.AssessmentDAO.WeightValidationReport;
import main.java.model.Lecturer.Lecturer;
import main.java.model.Lecturer.MarksCalculationService;
import main.java.model.Lecturer.MarksDAO;
import main.java.model.Lecturer.MarksDAO.StudentAssessmentDetail;
import main.java.model.Lecturer.MarksDAO.StudentMarkItem;
import main.java.model.Lecturer.ResultsDAO;
import main.java.model.Lecturer.ResultsDAO.CourseFinalResultItem;
import main.java.model.Lecturer.ResultsDAO.ResultsSummary;
import main.java.model.Lecturer.StudentDetailsEligibilityLogic;
import main.java.model.Lecturer.StudentDetailsEligibilityLogic.CourseInfo;
import main.java.model.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class LecturerMarksManagement extends JPanel {

    // =========================================================================
    // COLOR PALETTE (Faculty of Technology Brand Theme)
    // =========================================================================
    public static final Color COLOR_DEEP_NAVY      = new Color(8, 47, 90);     // #082F5A - Brand headers & primary dark
    public static final Color COLOR_PRO_BLUE       = new Color(11, 79, 156);   // #0B4F9C - Faculty primary royal blue
    public static final Color COLOR_TECH_BLUE      = new Color(40, 120, 208);  // #2878D0 - Active accents & pill buttons
    public static final Color COLOR_PAGE_BG        = new Color(244, 247, 251); // #F4F7FB - Soft canvas background
    public static final Color COLOR_WHITE          = Color.WHITE;              // Pure white cards & surfaces
    public static final Color COLOR_BORDER         = new Color(220, 228, 238); // #DCE4EE - Clean card borders
    public static final Color COLOR_TEXT_PRIMARY   = new Color(15, 23, 42);    // #0F172A - Slate-900 primary text
    public static final Color COLOR_TEXT_SECONDARY = new Color(71, 85, 105);   // #475569 - Slate-600 secondary text
    public static final Color COLOR_TEXT_MUTED     = new Color(148, 163, 184); // #94A3B8 - Slate-400 placeholder text
    public static final Color COLOR_SUCCESS        = new Color(16, 185, 129);  // #10B981 - Tech emerald for Eligible/Pass
    public static final Color COLOR_SUCCESS_BG     = new Color(236, 253, 245); // #ECFDF5 - Emerald pill background
    public static final Color COLOR_SUCCESS_BORDER = new Color(167, 243, 208); // #A7F3D0
    public static final Color COLOR_DANGER         = new Color(220, 38, 38);   // #DC2626 - Crimson red for Ineligible/Fail
    public static final Color COLOR_DANGER_BG      = new Color(254, 242, 242); // #FEF2F2 - Red pill background
    public static final Color COLOR_DANGER_BORDER  = new Color(254, 202, 202); // #FECACA
    public static final Color COLOR_WARNING        = new Color(217, 119, 6);   // #D97706 - Amber for Incomplete/Pending
    public static final Color COLOR_WARNING_BG    = new Color(255, 251, 235); // #FFFBEB - Amber pill background
    public static final Color COLOR_WARNING_BORDER = new Color(253, 230, 138); // #FDE68A
    public static final Color COLOR_ROW_ALT        = new Color(248, 250, 252); // Soft alternating row tint
    public static final Color COLOR_ROW_SELECTED   = new Color(224, 237, 254); // Selected row blue

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

    // =========================================================================
    // STATE & INSTANCE VARIABLES
    // =========================================================================
    private int lecturerId = -1;
    private String lecturerName = "";
    private Lecturer lecturerEntity; // Class diagram model representation

    // Navigation Wiring
    private Navbar parentNavbar;
    private CardLayout parentCardLayout;
    private JPanel parentContainer;

    // Data Caches
    private List<CourseInfo> assignedCourses = new ArrayList<>();
    private CourseInfo selectedCourse = null;
    private List<AssessmentComponent> currentAssessments = new ArrayList<>();
    private AssessmentComponent selectedAssessment = null;
    private List<StudentMarkItem> currentMarksList = new ArrayList<>();
    private List<StudentMarkItem> filteredMarksList = new ArrayList<>();
    private List<CourseFinalResultItem> currentFinalResults = new ArrayList<>();
    private List<CourseFinalResultItem> filteredFinalResults = new ArrayList<>();

    // Selected Student Inspection State
    private StudentMarkItem selectedStudentItem = null;

    // Course Selection Header Components
    private JComboBox<CourseInfo> courseComboBox;
    private JLabel courseCodeBadge;
    private JLabel courseTypeBadge;
    private JLabel studentCountBadge;
    private JLabel schemeProgressBadge;
    private JLabel pubStatusBadge;

    // Segmented Navigation Tab State
    private static final String TAB_MARKS_ENTRY    = "MARKS_ENTRY";
    private static final String TAB_CONFIGURATION  = "CONFIGURATION";
    private static final String TAB_FINAL_RESULTS  = "FINAL_RESULTS";
    private CardLayout tabCardLayout;
    private JPanel tabContainer;
    private ModernButton tabBtnMarks;
    private ModernButton tabBtnConfig;
    private ModernButton tabBtnResults;

    // --- TAB 1: MARKS ENTRY COMPONENTS ---
    private JComboBox<AssessmentComponent> assessmentComboBox;
    private JCheckBox openOnlyCheckBox;
    private ModernButton prevAssessmentBtn;
    private ModernButton nextAssessmentBtn;
    private JLabel assessmentStatusBadge;
    private JLabel assessmentTypeBadge;
    private JLabel assessmentMaxBadge;
    private JLabel assessmentWeightBadge;
    private JLabel assessmentDateBadge;
    private JLabel assessmentCountBadge;
    private JTextField marksSearchField;
    private JTable marksTable;
    private DefaultTableModel marksTableModel;
    private JLabel unsavedChangesLabel;
    private ModernButton saveMarksBtn;
    private ModernButton clearUnsavedBtn;

    // Student Marks Add Section Components
    private JPanel studentInspectorPanel;
    private JLabel inspectorStudentName;
    private JLabel inspectorStudentReg;
    private JLabel inspectorAssessmentName;
    private JTextField inspectorMarkInput;
    private ModernButton inspectorApplyMarkBtn;
    private JScrollPane pageScrollPane;

    // --- TAB 2: CONFIGURATION COMPONENTS ---
    private JTable configTable;
    private DefaultTableModel configTableModel;
    private JLabel schemeSummaryLabel;
    private ModernButton addAssessmentBtn;
    private ModernButton editAssessmentBtn;
    private ModernButton deleteAssessmentBtn;

    // --- TAB 3: FINAL RESULTS COMPONENTS ---
    private JLabel kpiTotalStudents;
    private JLabel kpiMarksCompleted;
    private JLabel kpiMarksPending;
    private JLabel kpiCaEligible;
    private JLabel kpiCaIneligible;
    private JLabel kpiResultsCalculated;
    private JTextField resultsSearchField;
    private JComboBox<String> resultsEligibilityFilter;
    private JComboBox<String> resultsStatusFilter;
    private JTable resultsTable;
    private DefaultTableModel resultsTableModel;
    private ModernButton markReviewedBtn;
    private ModernButton publishResultsBtn;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================

    public LecturerMarksManagement() {
        resolveLecturerSession();
        initializeUI();
        loadCourses();
    }

    public LecturerMarksManagement(Navbar navbar, CardLayout cardLayout, JPanel contentContainer) {
        this.parentNavbar = navbar;
        this.parentCardLayout = cardLayout;
        this.parentContainer = contentContainer;
        resolveLecturerSession();
        initializeUI();
        loadCourses();
    }

    public LecturerMarksManagement(int lecturerId) {
        this.lecturerId = lecturerId;
        resolveLecturerSession();
        initializeUI();
        loadCourses();
    }

    private void resolveLecturerSession() {
        UserSession session = UserSession.getCurrentSession();
        if (session != null && "Lecturer".equalsIgnoreCase(session.getRole())) {
            this.lecturerId = session.getUserId();
            this.lecturerName = session.getFullName();
        } else if (this.lecturerId <= 0) {
            // Default fallback for development and IDE standalone launching
            this.lecturerId = 4;
            this.lecturerName = "Dr. Chinthaka Premachandra";
        }
        this.lecturerEntity = new Lecturer(this.lecturerId, "Senior Lecturer Gr. I", "Department of Information and Communication Technology");
        this.lecturerEntity.setName(this.lecturerName);
    }

    // =========================================================================
    // UI INITIALIZATION
    // =========================================================================

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(COLOR_PAGE_BG);

        JPanel mainWrapper = new JPanel(new BorderLayout(0, 12));
        mainWrapper.setOpaque(false);
        mainWrapper.setBorder(new EmptyBorder(14, 20, 14, 20));

        // 1. Header & Course Selection Section
        mainWrapper.add(buildTopHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Tabs Content
        tabCardLayout = new CardLayout();
        tabContainer = new JPanel(tabCardLayout);
        tabContainer.setOpaque(false);

        tabContainer.add(buildMarksEntryTab(), TAB_MARKS_ENTRY);
        tabContainer.add(buildConfigurationTab(), TAB_CONFIGURATION);
        tabContainer.add(buildFinalResultsTab(), TAB_FINAL_RESULTS);

        mainWrapper.add(tabContainer, BorderLayout.CENTER);

        pageScrollPane = new JScrollPane(mainWrapper);
        pageScrollPane.setBorder(null);
        pageScrollPane.setOpaque(false);
        pageScrollPane.getViewport().setOpaque(false);
        pageScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pageScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        pageScrollPane.getVerticalScrollBar().setUnitIncrement(18);

        add(pageScrollPane, BorderLayout.CENTER);
    }

    public JScrollPane getPageScrollPane() {
        return pageScrollPane;
    }

    /**
     * Builds the top course selection and navigation header.
     */
    private JPanel buildTopHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        // Title Row
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JPanel titleTextPanel = new JPanel();
        titleTextPanel.setLayout(new BoxLayout(titleTextPanel, BoxLayout.Y_AXIS));
        titleTextPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Lecturer Marks Management");
        titleLabel.setFont(FONT_PAGE_TITLE);
        titleLabel.setForeground(COLOR_DEEP_NAVY);

        JLabel subtitleLabel = new JLabel("Continuous assessment configuration, student marks recording, automated CA calculation (UGC Rules & Handbook), and official grading.");
        subtitleLabel.setFont(FONT_PAGE_SUBTITLE);
        subtitleLabel.setForeground(COLOR_TEXT_SECONDARY);

        titleTextPanel.add(titleLabel);
        titleTextPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        titleTextPanel.add(subtitleLabel);

        // Action Buttons on Right
        JPanel titleActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        titleActions.setOpaque(false);

        ModernButton refreshBtn = new ModernButton("Refresh Data", MarksVectorIcon.create(MarksVectorIcon.Type.REFRESH, 14, 14, COLOR_TEXT_PRIMARY),
                COLOR_WHITE, COLOR_TEXT_PRIMARY, COLOR_BORDER, 8);
        refreshBtn.addActionListener(e -> refreshAllData());
        titleActions.add(refreshBtn);

        titleRow.add(titleTextPanel, BorderLayout.WEST);
        titleRow.add(titleActions, BorderLayout.EAST);

        headerPanel.add(titleRow);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Course Selection Card
        JPanel courseCard = createRoundedCard();
        courseCard.setLayout(new BorderLayout(14, 0));
        courseCard.setBorder(new EmptyBorder(10, 16, 10, 16));

        JPanel courseSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        courseSelectPanel.setOpaque(false);

        JLabel selectLabel = new JLabel("Assigned Course:");
        selectLabel.setFont(FONT_CARD_LABEL);
        selectLabel.setForeground(COLOR_DEEP_NAVY);

        courseComboBox = new JComboBox<>();
        courseComboBox.setFont(FONT_INPUT);
        courseComboBox.setPreferredSize(new Dimension(320, 34));
        courseComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof CourseInfo) {
                    CourseInfo c = (CourseInfo) value;
                    setText(c.getCourseCode() + " - " + c.getCourseName() + " (Sem " + c.getSemester() + ", " + c.getAcademicYear() + ")");
                } else if (value == null) {
                    setText("-- Select an Assigned Course --");
                }
                return this;
            }
        });
        courseComboBox.addActionListener(e -> onCourseSelected());

        courseSelectPanel.add(selectLabel);
        courseSelectPanel.add(courseComboBox);

        // Course Badges Panel - Clean, responsive flow layout
        JPanel badgesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        badgesPanel.setOpaque(false);

        courseCodeBadge = createBadge("COURSE", COLOR_PRO_BLUE, COLOR_WHITE);
        courseTypeBadge = createBadge("Type", new Color(241, 245, 249), COLOR_TEXT_PRIMARY);
        studentCountBadge = createBadge("0 Students", COLOR_SUCCESS_BG, COLOR_SUCCESS);
        schemeProgressBadge = createBadge("Scheme: Checking...", COLOR_WARNING_BG, COLOR_WARNING);
        pubStatusBadge = createBadge("Draft", new Color(241, 245, 249), COLOR_TEXT_SECONDARY);

        badgesPanel.add(courseCodeBadge);
        badgesPanel.add(courseTypeBadge);
        badgesPanel.add(studentCountBadge);
        badgesPanel.add(schemeProgressBadge);
        badgesPanel.add(pubStatusBadge);

        courseCard.add(courseSelectPanel, BorderLayout.WEST);
        courseCard.add(badgesPanel, BorderLayout.EAST);

        headerPanel.add(courseCard);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Segmented Tab Switcher Bar - Generous button sizes so words never clip
        headerPanel.add(buildTabBar());

        return headerPanel;
    }

    /**
     * Builds the segmented tab navigation bar.
     */
    private JPanel buildTabBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);

        tabBtnMarks = new ModernButton("1. Marks Entry & Overall CA",
                MarksVectorIcon.create(MarksVectorIcon.Type.EDIT, 14, 14, COLOR_WHITE),
                COLOR_PRO_BLUE, COLOR_WHITE, COLOR_PRO_BLUE, 8);
        tabBtnMarks.setPreferredSize(new Dimension(220, 36));

        tabBtnConfig = new ModernButton("2. Assessment Scheme",
                MarksVectorIcon.create(MarksVectorIcon.Type.ASSIGNMENT, 14, 14, COLOR_TEXT_SECONDARY),
                COLOR_WHITE, COLOR_TEXT_SECONDARY, COLOR_BORDER, 8);
        tabBtnConfig.setPreferredSize(new Dimension(180, 36));

        tabBtnResults = new ModernButton("3. Final Results & Grading",
                MarksVectorIcon.create(MarksVectorIcon.Type.END_EXAM, 14, 14, COLOR_TEXT_SECONDARY),
                COLOR_WHITE, COLOR_TEXT_SECONDARY, COLOR_BORDER, 8);
        tabBtnResults.setPreferredSize(new Dimension(210, 36));

        tabBtnMarks.addActionListener(e -> switchTab(TAB_MARKS_ENTRY));
        tabBtnConfig.addActionListener(e -> switchTab(TAB_CONFIGURATION));
        tabBtnResults.addActionListener(e -> switchTab(TAB_FINAL_RESULTS));

        bar.add(tabBtnMarks);
        bar.add(tabBtnConfig);
        bar.add(tabBtnResults);

        return bar;
    }

    private void switchTab(String tabName) {
        tabCardLayout.show(tabContainer, tabName);

        boolean isMarks = TAB_MARKS_ENTRY.equals(tabName);
        boolean isConfig = TAB_CONFIGURATION.equals(tabName);
        boolean isResults = TAB_FINAL_RESULTS.equals(tabName);

        updateTabButtonVisual(tabBtnMarks, isMarks, MarksVectorIcon.Type.EDIT);
        updateTabButtonVisual(tabBtnConfig, isConfig, MarksVectorIcon.Type.ASSIGNMENT);
        updateTabButtonVisual(tabBtnResults, isResults, MarksVectorIcon.Type.END_EXAM);

        if (isResults) {
            loadFinalResults();
        } else if (isConfig) {
            loadAssessmentsForConfig();
        } else if (isMarks) {
            loadMarksForSelectedAssessment();
        }
    }

    private void updateTabButtonVisual(ModernButton btn, boolean active, MarksVectorIcon.Type iconType) {
        if (active) {
            btn.setCustomColors(COLOR_PRO_BLUE, COLOR_WHITE, COLOR_PRO_BLUE);
            btn.setIcon(MarksVectorIcon.create(iconType, 14, 14, COLOR_WHITE));
        } else {
            btn.setCustomColors(COLOR_WHITE, COLOR_TEXT_SECONDARY, COLOR_BORDER);
            btn.setIcon(MarksVectorIcon.create(iconType, 14, 14, COLOR_TEXT_SECONDARY));
        }
    }

    // =========================================================================
    // TAB 1: MARKS ENTRY VIEW WITH QUICK ASSESSMENT ADD & OVERALL CA SIDEBAR
    // =========================================================================

    private JPanel buildMarksEntryTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        // 1. Top Section: Unified Assessment Selector & Quick Add Action Card (Sleek, Compact, Zero Clipping)
        JPanel assessmentCard = createRoundedCard();
        assessmentCard.setLayout(new BoxLayout(assessmentCard, BoxLayout.Y_AXIS));
        assessmentCard.setBorder(new EmptyBorder(8, 14, 8, 14));

        // Row 1: Assessment Switcher (Left) + Status Badge (Right)
        JPanel row1 = new JPanel(new BorderLayout(8, 0));
        row1.setOpaque(false);

        JPanel row1Left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row1Left.setOpaque(false);

        JLabel selectAssLabel = new JLabel("Assessment / Exam:");
        selectAssLabel.setFont(FONT_CARD_LABEL);
        selectAssLabel.setForeground(COLOR_DEEP_NAVY);

        assessmentComboBox = new JComboBox<>();
        assessmentComboBox.setFont(FONT_INPUT);
        assessmentComboBox.setPreferredSize(new Dimension(240, 32));
        assessmentComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof AssessmentComponent) {
                    AssessmentComponent ac = (AssessmentComponent) value;
                    setText("[" + ac.getStatus().toUpperCase() + "] " + ac.getComponentName() + " (" + ac.getComponentType() + " - " + ac.getWeightPercentage() + "%)");
                } else if (value == null) {
                    setText("-- No Assessments Available --");
                }
                return this;
            }
        });
        assessmentComboBox.addActionListener(e -> onAssessmentSelected());

        prevAssessmentBtn = new ModernButton("Prev", MarksVectorIcon.create(MarksVectorIcon.Type.CHEVRON_LEFT, 11, 11, COLOR_TEXT_PRIMARY),
                COLOR_WHITE, COLOR_TEXT_PRIMARY, COLOR_BORDER, 6);
        prevAssessmentBtn.setPreferredSize(new Dimension(68, 32));
        prevAssessmentBtn.addActionListener(e -> navigateAssessment(-1));

        nextAssessmentBtn = new ModernButton("Next", MarksVectorIcon.create(MarksVectorIcon.Type.CHEVRON_RIGHT, 11, 11, COLOR_TEXT_PRIMARY),
                COLOR_WHITE, COLOR_TEXT_PRIMARY, COLOR_BORDER, 6);
        nextAssessmentBtn.setPreferredSize(new Dimension(68, 32));
        nextAssessmentBtn.addActionListener(e -> navigateAssessment(1));

        openOnlyCheckBox = new JCheckBox("Open Only");
        openOnlyCheckBox.setFont(FONT_INPUT);
        openOnlyCheckBox.setOpaque(false);
        openOnlyCheckBox.setForeground(COLOR_TEXT_SECONDARY);
        openOnlyCheckBox.addActionListener(e -> filterAssessmentsDropdown());

        row1Left.add(selectAssLabel);
        row1Left.add(assessmentComboBox);
        row1Left.add(prevAssessmentBtn);
        row1Left.add(nextAssessmentBtn);
        row1Left.add(openOnlyCheckBox);

        JPanel row1Right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        row1Right.setOpaque(false);

        JPopupMenu addMenu = new JPopupMenu();
        addMenu.setFont(FONT_INPUT);

        JMenuItem mQuiz = new JMenuItem("+ Add Quiz (CA - 10%)", MarksVectorIcon.create(MarksVectorIcon.Type.QUIZ, 13, 13, COLOR_PRO_BLUE));
        mQuiz.setFont(FONT_INPUT);
        mQuiz.addActionListener(e -> showQuickAddAssessmentDialog("Quiz", "CA", 10.0));

        JMenuItem mAssignment = new JMenuItem("+ Add Assignment (CA - 10%)", MarksVectorIcon.create(MarksVectorIcon.Type.ASSIGNMENT, 13, 13, COLOR_PRO_BLUE));
        mAssignment.setFont(FONT_INPUT);
        mAssignment.addActionListener(e -> showQuickAddAssessmentDialog("Assignment", "CA", 10.0));

        JMenuItem mMid = new JMenuItem("+ Add Midterm Exam (CA - 20%)", MarksVectorIcon.create(MarksVectorIcon.Type.MIDTERM, 13, 13, new Color(180, 83, 9)));
        mMid.setFont(FONT_INPUT);
        mMid.addActionListener(e -> showQuickAddAssessmentDialog("Midterm Exam", "CA", 20.0));

        JMenuItem mEnd = new JMenuItem("+ Add End Semester Exam (Final - 60%)", MarksVectorIcon.create(MarksVectorIcon.Type.END_EXAM, 13, 13, new Color(15, 118, 110)));
        mEnd.setFont(FONT_INPUT);
        mEnd.addActionListener(e -> showQuickAddAssessmentDialog("End Exam", "Final", 60.0));

        JMenuItem mCustom = new JMenuItem("+ Custom Assessment Component...", MarksVectorIcon.create(MarksVectorIcon.Type.ADD, 13, 13, COLOR_TEXT_PRIMARY));
        mCustom.setFont(FONT_INPUT);
        mCustom.addActionListener(e -> showAddAssessmentDialog());

        addMenu.add(mQuiz);
        addMenu.add(mAssignment);
        addMenu.add(mMid);
        addMenu.add(mEnd);
        addMenu.addSeparator();
        addMenu.add(mCustom);

        ModernButton addAssessmentDropdownBtn = new ModernButton("+ Add Assessment  ▼", MarksVectorIcon.create(MarksVectorIcon.Type.ADD, 12, 12, COLOR_WHITE),
                COLOR_PRO_BLUE, COLOR_WHITE, COLOR_PRO_BLUE, 6);
        addAssessmentDropdownBtn.setPreferredSize(new Dimension(160, 32));
        addAssessmentDropdownBtn.addActionListener(e -> addMenu.show(addAssessmentDropdownBtn, 0, addAssessmentDropdownBtn.getHeight()));

        assessmentStatusBadge = createBadge("OPEN", COLOR_SUCCESS_BG, COLOR_SUCCESS);

        row1Right.add(addAssessmentDropdownBtn);
        row1Right.add(assessmentStatusBadge);

        row1.add(row1Left, BorderLayout.WEST);
        row1.add(row1Right, BorderLayout.EAST);

        // Row 2: Dedicated Assessment Details Chips
        JPanel row2 = new JPanel(new BorderLayout(8, 0));
        row2.setOpaque(false);
        row2.setBorder(new EmptyBorder(6, 0, 0, 0));

        JPanel row2Left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row2Left.setOpaque(false);

        assessmentTypeBadge = createBadge("Type: CA", new Color(241, 245, 249), COLOR_TEXT_PRIMARY);
        assessmentMaxBadge = createBadge("Max: 100.0", new Color(241, 245, 249), COLOR_TEXT_PRIMARY);
        assessmentWeightBadge = createBadge("Weight: 10.0%", new Color(239, 246, 255), COLOR_PRO_BLUE);
        assessmentDateBadge = createBadge("Date: —", new Color(241, 245, 249), COLOR_TEXT_SECONDARY);
        assessmentCountBadge = createBadge("0 Students", COLOR_SUCCESS_BG, COLOR_SUCCESS);

        row2Left.add(assessmentTypeBadge);
        row2Left.add(assessmentMaxBadge);
        row2Left.add(assessmentWeightBadge);
        row2Left.add(assessmentDateBadge);
        row2Left.add(assessmentCountBadge);

        JPanel row2Right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        row2Right.setOpaque(false);
        JLabel hintLbl = new JLabel("Click student row or edit mark cell directly");
        hintLbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hintLbl.setForeground(COLOR_TEXT_SECONDARY);
        row2Right.add(hintLbl);

        row2.add(row2Left, BorderLayout.WEST);
        row2.add(row2Right, BorderLayout.EAST);

        assessmentCard.add(row1);
        assessmentCard.add(row2);

        panel.add(assessmentCard, BorderLayout.NORTH);

        // 2. Center: Dedicated Marks Add & Entry Strip + Table & Inspector Split Pane
        JPanel contentCard = createRoundedCard();
        contentCard.setLayout(new BorderLayout(0, 8));
        contentCard.setBorder(new EmptyBorder(10, 14, 10, 14));

        // Table Top Header Controls: Search Student & Status Indicator
        JPanel tableTop = new JPanel(new BorderLayout(8, 0));
        tableTop.setOpaque(false);
        tableTop.setBorder(new EmptyBorder(0, 0, 6, 0));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setOpaque(false);
        JLabel searchIcon = new JLabel(MarksVectorIcon.create(MarksVectorIcon.Type.SEARCH, 13, 13, COLOR_TEXT_SECONDARY));
        JLabel searchLbl = new JLabel("Search Student:");
        searchLbl.setFont(FONT_CARD_LABEL);
        searchLbl.setForeground(COLOR_TEXT_SECONDARY);

        marksSearchField = new JTextField();
        marksSearchField.setFont(FONT_INPUT);
        marksSearchField.setPreferredSize(new Dimension(240, 28));
        marksSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterMarksTable(); }
            @Override public void removeUpdate(DocumentEvent e) { filterMarksTable(); }
            @Override public void changedUpdate(DocumentEvent e) { filterMarksTable(); }
        });

        searchPanel.add(searchIcon);
        searchPanel.add(searchLbl);
        searchPanel.add(marksSearchField);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        statusPanel.setOpaque(false);

        unsavedChangesLabel = new JLabel("All marks saved to database");
        unsavedChangesLabel.setFont(FONT_CARD_LABEL);
        unsavedChangesLabel.setForeground(COLOR_SUCCESS);
        statusPanel.add(unsavedChangesLabel);

        tableTop.add(searchPanel, BorderLayout.WEST);
        tableTop.add(statusPanel, BorderLayout.EAST);

        contentCard.add(tableTop, BorderLayout.NORTH);

        // Build JTable with calibrated column headers and widths
        String[] columns = {
                "Reg. No", "Student Name", "Mark (Edit)", "Max",
                "Norm. %", "Weight %", "Overall CA", "CA %",
                "CA Eligibility", "Status"
        };

        marksTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Column 2 ("Mark (Edit)") is editable unless assessment is published
                return column == 2 && (selectedAssessment == null || !selectedAssessment.isPublished());
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };

        marksTable = new JTable(marksTableModel);
        marksTable.setFont(FONT_TABLE_CELL);
        marksTable.setRowHeight(34);
        marksTable.setSelectionBackground(COLOR_ROW_SELECTED);
        marksTable.setSelectionForeground(COLOR_TEXT_PRIMARY);
        marksTable.setShowGrid(false);
        marksTable.setIntercellSpacing(new Dimension(0, 0));
        marksTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JTableHeader header = marksTable.getTableHeader();
        header.setFont(FONT_TABLE_HEADER);
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(COLOR_DEEP_NAVY);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

        // Renderers & Editors
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        marksTable.getColumnModel().getColumn(0).setCellRenderer(new RegNoRenderer());
        marksTable.getColumnModel().getColumn(2).setCellRenderer(new RawMarkCellRenderer());
        marksTable.getColumnModel().getColumn(3).setCellRenderer(center);
        marksTable.getColumnModel().getColumn(4).setCellRenderer(new PercentageCellRenderer());
        marksTable.getColumnModel().getColumn(5).setCellRenderer(center);
        marksTable.getColumnModel().getColumn(6).setCellRenderer(center);
        marksTable.getColumnModel().getColumn(7).setCellRenderer(new OverallCaPercentageCellRenderer());
        marksTable.getColumnModel().getColumn(8).setCellRenderer(new CaEligibilityRenderer());
        marksTable.getColumnModel().getColumn(9).setCellRenderer(new StatusBadgeRenderer());

        marksTable.getColumnModel().getColumn(2).setCellEditor(new RawMarkCellEditor());

        // Set explicit preferred column widths so headers are NEVER clipped!
        int[] colWidths = {110, 160, 115, 65, 75, 75, 95, 75, 120, 80};
        for (int i = 0; i < colWidths.length; i++) {
            marksTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
            marksTable.getColumnModel().getColumn(i).setMinWidth(Math.max(50, colWidths[i] - 15));
        }

        // Row Selection Listener -> Updates both Top Quick Bar and Right Student CA Inspector!
        marksTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = marksTable.getSelectedRow();
                if (selectedRow >= 0 && selectedRow < filteredMarksList.size()) {
                    updateStudentInspector(filteredMarksList.get(selectedRow));
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(marksTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        tableScroll.getViewport().setBackground(COLOR_WHITE);
        tableScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScroll.setMinimumSize(new Dimension(460, 180));

        // Forward mouse wheel events to pageScrollPane when table reaches bounds or doesn't have vertical scrollbar
        MouseWheelListener scrollForwarder = new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                JScrollBar vBar = tableScroll.getVerticalScrollBar();
                boolean forwardToPage = false;
                if (vBar == null || !vBar.isVisible()) {
                    forwardToPage = true;
                } else {
                    int value = vBar.getValue();
                    int extent = vBar.getModel().getExtent();
                    int max = vBar.getMaximum();
                    int min = vBar.getMinimum();
                    int rot = e.getWheelRotation();
                    if (rot > 0 && (value + extent >= max)) {
                        forwardToPage = true;
                    } else if (rot < 0 && (value <= min)) {
                        forwardToPage = true;
                    }
                }

                if (forwardToPage && pageScrollPane != null) {
                    pageScrollPane.dispatchEvent(SwingUtilities.convertMouseEvent(tableScroll, e, pageScrollPane));
                }
            }
        };
        tableScroll.addMouseWheelListener(scrollForwarder);
        marksTable.addMouseWheelListener(scrollForwarder);

        // Build the Right-side Student Marks Add Panel (strictly providing add marks controls)
        studentInspectorPanel = buildStudentMarksAddPanel();
        JPanel rightWrapper = new JPanel(new BorderLayout());
        rightWrapper.setOpaque(false);
        rightWrapper.add(studentInspectorPanel, BorderLayout.NORTH);
        rightWrapper.setMinimumSize(new Dimension(280, 180));

        MouseWheelListener rightWheelForwarder = e -> {
            if (pageScrollPane != null) {
                pageScrollPane.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, pageScrollPane));
            }
        };
        studentInspectorPanel.addMouseWheelListener(rightWheelForwarder);
        rightWrapper.addMouseWheelListener(rightWheelForwarder);

        // Split Pane (Left: Table, Right: Student Marks Add Section)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, rightWrapper);
        splitPane.setResizeWeight(0.72);
        splitPane.setDividerSize(6);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        SwingUtilities.invokeLater(() -> {
            int w = splitPane.getWidth();
            if (w > 0) {
                splitPane.setDividerLocation(Math.max(w - 340, (int) (w * 0.70)));
            } else {
                splitPane.setDividerLocation(780);
            }
        });

        contentCard.add(splitPane, BorderLayout.CENTER);

        // Bottom Action Bar: Regulatory Reference Note & Save/Clear Action Buttons
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);

        JPanel leftNote = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        leftNote.setOpaque(false);
        JLabel ruleNote = new JLabel("UGC / By-Law No. 92-2025 • CA Minimum Pass Threshold: 40.00% (Grade C-) • ESA Pass Threshold: 35.00% (Grade D)");
        ruleNote.setFont(FONT_PAGE_SUBTITLE);
        ruleNote.setForeground(COLOR_TEXT_SECONDARY);
        leftNote.add(ruleNote);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        clearUnsavedBtn = new ModernButton("Clear Unsaved", MarksVectorIcon.create(MarksVectorIcon.Type.ALERT, 13, 13, COLOR_TEXT_PRIMARY),
                COLOR_WHITE, COLOR_TEXT_PRIMARY, COLOR_BORDER, 6);
        clearUnsavedBtn.addActionListener(e -> clearUnsavedMarks());

        saveMarksBtn = new ModernButton("Save All Marks", MarksVectorIcon.create(MarksVectorIcon.Type.SAVE, 14, 14, COLOR_WHITE),
                COLOR_PRO_BLUE, COLOR_WHITE, COLOR_PRO_BLUE, 6);
        saveMarksBtn.setPreferredSize(new Dimension(160, 34));
        saveMarksBtn.addActionListener(e -> saveCurrentMarks());

        rightActions.add(clearUnsavedBtn);
        rightActions.add(saveMarksBtn);

        bottomBar.add(leftNote, BorderLayout.WEST);
        bottomBar.add(rightActions, BorderLayout.EAST);

        contentCard.add(bottomBar, BorderLayout.SOUTH);

        panel.add(contentCard, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Builds the dedicated Student Marks Add Section.
     * Contains strictly the student identity, assessment target, mark input, and apply action.
     * All other information (CA summary cards, eligibility badges, breakdown tables) is removed per requirements.
     */
    private JPanel buildStudentMarksAddPanel() {
        JPanel card = createRoundedCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setPreferredSize(new Dimension(320, 230));

        // 1. Header Title
        JPanel headerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        headerRow.setOpaque(false);
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel iconLabel = new JLabel(MarksVectorIcon.create(MarksVectorIcon.Type.EDIT, 15, 15, COLOR_PRO_BLUE));
        JLabel titleLbl = new JLabel("Add Student Mark");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(COLOR_DEEP_NAVY);

        headerRow.add(iconLabel);
        headerRow.add(titleLbl);

        card.add(headerRow);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        // 2. Selected Student Box
        JPanel studentBox = new JPanel();
        studentBox.setLayout(new BoxLayout(studentBox, BoxLayout.Y_AXIS));
        studentBox.setBackground(new Color(248, 250, 252));
        studentBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        studentBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        studentBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        inspectorStudentName = new JLabel("Click any student in table");
        inspectorStudentName.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        inspectorStudentName.setForeground(COLOR_TEXT_SECONDARY);

        inspectorStudentReg = new JLabel("Reg No: —");
        inspectorStudentReg.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        inspectorStudentReg.setForeground(COLOR_TEXT_SECONDARY);

        studentBox.add(inspectorStudentName);
        studentBox.add(Box.createRigidArea(new Dimension(0, 3)));
        studentBox.add(inspectorStudentReg);

        card.add(studentBox);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        // 3. Current Assessment Target
        inspectorAssessmentName = new JLabel("Assessment: —");
        inspectorAssessmentName.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        inspectorAssessmentName.setForeground(COLOR_PRO_BLUE);
        inspectorAssessmentName.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(inspectorAssessmentName);
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        // 4. Mark Input & Apply Button
        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setOpaque(false);
        inputRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        inspectorMarkInput = new JTextField();
        inspectorMarkInput.setFont(new Font("Segoe UI", Font.BOLD, 14));
        inspectorMarkInput.setHorizontalAlignment(SwingConstants.CENTER);
        inspectorMarkInput.setPreferredSize(new Dimension(100, 34));
        inspectorMarkInput.setEnabled(false);

        inspectorApplyMarkBtn = new ModernButton("Apply Mark", MarksVectorIcon.create(MarksVectorIcon.Type.CHECK, 13, 13, COLOR_WHITE),
                COLOR_PRO_BLUE, COLOR_WHITE, COLOR_PRO_BLUE, 6);
        inspectorApplyMarkBtn.setPreferredSize(new Dimension(110, 34));
        inspectorApplyMarkBtn.setEnabled(false);
        inspectorApplyMarkBtn.addActionListener(e -> applyInspectorMark());
        inspectorMarkInput.addActionListener(e -> applyInspectorMark());

        inputRow.add(inspectorMarkInput, BorderLayout.CENTER);
        inputRow.add(inspectorApplyMarkBtn, BorderLayout.EAST);

        card.add(inputRow);
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        // 5. Instruction text
        JLabel hintLabel = new JLabel("Enter mark (0 - max). Press Enter to apply.");
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hintLabel.setForeground(COLOR_TEXT_SECONDARY);
        hintLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(hintLabel);

        return card;
    }

    /**
     * Updates the student marks add section with details for the selected student.
     * Contains only student identification, assessment max mark, and mark entry field.
     */
    private void updateStudentInspector(StudentMarkItem item) {
        this.selectedStudentItem = item;
        boolean editable = selectedAssessment != null && !selectedAssessment.isPublished();

        if (item == null) {
            if (inspectorStudentName != null) {
                inspectorStudentName.setText("Click any student in table");
                inspectorStudentName.setForeground(COLOR_TEXT_SECONDARY);
            }
            if (inspectorStudentReg != null) {
                inspectorStudentReg.setText("Reg No: —");
            }
            if (inspectorAssessmentName != null) {
                if (selectedAssessment != null) {
                    inspectorAssessmentName.setText("Assessment: " + selectedAssessment.getComponentName() + " (Max: " + selectedAssessment.getMaximumMarks() + ")");
                } else {
                    inspectorAssessmentName.setText("Assessment: —");
                }
            }
            if (inspectorMarkInput != null) {
                inspectorMarkInput.setText("");
                inspectorMarkInput.setEnabled(false);
            }
            if (inspectorApplyMarkBtn != null) {
                inspectorApplyMarkBtn.setEnabled(false);
            }
            return;
        }

        String currentMarkStr = item.getRawMark() != null ? String.format("%.2f", item.getRawMark()) : "";

        if (inspectorStudentName != null) {
            inspectorStudentName.setText(item.getStudentName());
            inspectorStudentName.setForeground(COLOR_DEEP_NAVY);
        }
        if (inspectorStudentReg != null) {
            inspectorStudentReg.setText("Reg No: " + item.getStudentRegNo());
        }
        if (inspectorAssessmentName != null) {
            if (selectedAssessment != null) {
                inspectorAssessmentName.setText("Assessment: " + selectedAssessment.getComponentName() + " (Max: " + selectedAssessment.getMaximumMarks() + ")");
            } else {
                inspectorAssessmentName.setText("Assessment: —");
            }
        }
        if (inspectorMarkInput != null) {
            inspectorMarkInput.setText(currentMarkStr);
            inspectorMarkInput.setEnabled(editable);
            inspectorMarkInput.requestFocusInWindow();
            inspectorMarkInput.selectAll();
        }
        if (inspectorApplyMarkBtn != null) {
            inspectorApplyMarkBtn.setEnabled(editable);
        }
    }

    /**
     * Applies mark entered in the inspector text field to the selected student.
     * Advances to the next student row automatically for rapid grading.
     */
    private void applyInspectorMark() {
        if (selectedStudentItem == null || selectedAssessment == null) {
            JOptionPane.showMessageDialog(this, "Please select a student and an assessment first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String markText = inspectorMarkInput.getText().trim();
        saveMarkForCurrentStudent(markText, true);
    }

    /**
     * Common core method to validate raw mark, update memory models, recalculate overall CA,
     * refresh table rows, and keep the Student Marks Add Section perfectly in sync.
     */
    private void saveMarkForCurrentStudent(String markText, boolean advanceToNext) {
        double maxAllowed = selectedAssessment.getMaximumMarks();

        MarksCalculationService.ValidationResult res = MarksCalculationService.validateRawMark(markText, maxAllowed);
        if (!res.isValid()) {
            JOptionPane.showMessageDialog(this, res.getErrorMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
            if (inspectorMarkInput != null) {
                inspectorMarkInput.requestFocus();
                inspectorMarkInput.selectAll();
            }
            return;
        }

        // Apply mark and trigger automated real-time CA recalculation
        selectedStudentItem.setRawMark(res.getParsedValue());
        updateUnsavedStatus();

        // Refresh table row displays
        filterMarksTable();

        // Find index of current student in filtered list
        int curIdx = -1;
        for (int i = 0; i < filteredMarksList.size(); i++) {
            if (filteredMarksList.get(i).getStudentId() == selectedStudentItem.getStudentId()) {
                curIdx = i;
                break;
            }
        }

        if (advanceToNext && curIdx >= 0 && curIdx + 1 < filteredMarksList.size()) {
            int nextIdx = curIdx + 1;
            marksTable.setRowSelectionInterval(nextIdx, nextIdx);
            marksTable.scrollRectToVisible(marksTable.getCellRect(nextIdx, 0, true));
            updateStudentInspector(filteredMarksList.get(nextIdx));
        } else if (curIdx >= 0) {
            marksTable.setRowSelectionInterval(curIdx, curIdx);
            updateStudentInspector(filteredMarksList.get(curIdx));
        }
    }

    // =========================================================================
    // TAB 2: ASSESSMENT CONFIGURATION VIEW
    // =========================================================================

    private JPanel buildConfigurationTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // Scheme Summary Banner
        JPanel bannerCard = createRoundedCard();
        bannerCard.setLayout(new BorderLayout(12, 0));
        bannerCard.setBorder(new EmptyBorder(12, 16, 12, 16));

        schemeSummaryLabel = new JLabel("Assessment Scheme Configuration: Checking weights...");
        schemeSummaryLabel.setFont(FONT_CARD_LABEL);
        schemeSummaryLabel.setForeground(COLOR_DEEP_NAVY);

        JPanel bannerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bannerActions.setOpaque(false);

        addAssessmentBtn = new ModernButton("+ Add Assessment Component", MarksVectorIcon.create(MarksVectorIcon.Type.ADD, 14, 14, COLOR_WHITE),
                COLOR_PRO_BLUE, COLOR_WHITE, COLOR_PRO_BLUE, 6);
        addAssessmentBtn.addActionListener(e -> showAddAssessmentDialog());

        bannerActions.add(addAssessmentBtn);

        bannerCard.add(schemeSummaryLabel, BorderLayout.WEST);
        bannerCard.add(bannerActions, BorderLayout.EAST);

        panel.add(bannerCard, BorderLayout.NORTH);

        // Configuration Table Card
        JPanel tableCard = createRoundedCard();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.setBorder(new EmptyBorder(12, 16, 12, 16));

        String[] cols = {"ID", "Assessment Name", "Category", "Type", "Max Raw Mark", "Weight %", "Scheduled Date", "Status", "Entered Marks"};
        configTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        configTable = new JTable(configTableModel);
        configTable.setFont(FONT_TABLE_CELL);
        configTable.setRowHeight(36);
        configTable.setSelectionBackground(COLOR_ROW_SELECTED);
        configTable.setSelectionForeground(COLOR_TEXT_PRIMARY);
        configTable.setShowGrid(false);

        JTableHeader header = configTable.getTableHeader();
        header.setFont(FONT_TABLE_HEADER);
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(COLOR_DEEP_NAVY);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < configTable.getColumnCount(); i++) {
            if (i != 1) configTable.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        JScrollPane scrollPane = new JScrollPane(configTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(COLOR_WHITE);

        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Action Buttons Row
        JPanel actionsRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsRow.setOpaque(false);

        editAssessmentBtn = new ModernButton("Edit Selected", MarksVectorIcon.create(MarksVectorIcon.Type.EDIT, 13, 13, COLOR_TEXT_PRIMARY),
                COLOR_WHITE, COLOR_TEXT_PRIMARY, COLOR_BORDER, 6);
        editAssessmentBtn.addActionListener(e -> showEditAssessmentDialog());

        deleteAssessmentBtn = new ModernButton("Delete Assessment", MarksVectorIcon.create(MarksVectorIcon.Type.TRASH, 13, 13, COLOR_DANGER),
                COLOR_DANGER_BG, COLOR_DANGER, COLOR_DANGER_BORDER, 6);
        deleteAssessmentBtn.addActionListener(e -> deleteSelectedAssessment());

        actionsRow.add(editAssessmentBtn);
        actionsRow.add(deleteAssessmentBtn);

        tableCard.add(actionsRow, BorderLayout.SOUTH);

        panel.add(tableCard, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // TAB 3: FINAL RESULTS & UGC GRADING VIEW
    // =========================================================================

    private JPanel buildFinalResultsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        // 6 KPI Summary Cards Row
        JPanel kpiGrid = new JPanel(new GridLayout(1, 6, 10, 0));
        kpiGrid.setOpaque(false);

        kpiTotalStudents = new JLabel("0");
        kpiMarksCompleted = new JLabel("0");
        kpiMarksPending = new JLabel("0");
        kpiCaEligible = new JLabel("0");
        kpiCaIneligible = new JLabel("0");
        kpiResultsCalculated = new JLabel("0");

        kpiGrid.add(createKpiCard("Registered", kpiTotalStudents, COLOR_PRO_BLUE));
        kpiGrid.add(createKpiCard("Completed", kpiMarksCompleted, COLOR_SUCCESS));
        kpiGrid.add(createKpiCard("Pending", kpiMarksPending, COLOR_WARNING));
        kpiGrid.add(createKpiCard("CA Eligible (≥40%)", kpiCaEligible, COLOR_SUCCESS));
        kpiGrid.add(createKpiCard("CA Ineligible", kpiCaIneligible, COLOR_DANGER));
        kpiGrid.add(createKpiCard("Calculated", kpiResultsCalculated, COLOR_TECH_BLUE));

        panel.add(kpiGrid, BorderLayout.NORTH);

        // Results Table Card
        JPanel tableCard = createRoundedCard();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.setBorder(new EmptyBorder(12, 16, 12, 16));

        // Filter Bar
        JPanel filterBar = new JPanel(new BorderLayout());
        filterBar.setOpaque(false);

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilters.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(FONT_CARD_LABEL);
        resultsSearchField = new JTextField();
        resultsSearchField.setFont(FONT_INPUT);
        resultsSearchField.setPreferredSize(new Dimension(200, 32));
        resultsSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterFinalResultsTable(); }
            @Override public void removeUpdate(DocumentEvent e) { filterFinalResultsTable(); }
            @Override public void changedUpdate(DocumentEvent e) { filterFinalResultsTable(); }
        });

        JLabel eligLbl = new JLabel("CA Eligibility:");
        eligLbl.setFont(FONT_CARD_LABEL);
        resultsEligibilityFilter = new JComboBox<>(new String[]{"All Eligibility", "ELIGIBLE", "NOT_ELIGIBLE", "PENDING"});
        resultsEligibilityFilter.setFont(FONT_INPUT);
        resultsEligibilityFilter.setPreferredSize(new Dimension(130, 32));
        resultsEligibilityFilter.addActionListener(e -> filterFinalResultsTable());

        JLabel statusLbl = new JLabel("Result Status:");
        statusLbl.setFont(FONT_CARD_LABEL);
        resultsStatusFilter = new JComboBox<>(new String[]{"All Statuses", "Pass", "Pass (D/C-)", "EE", "EC", "E", "Incomplete"});
        resultsStatusFilter.setFont(FONT_INPUT);
        resultsStatusFilter.setPreferredSize(new Dimension(130, 32));
        resultsStatusFilter.addActionListener(e -> filterFinalResultsTable());

        leftFilters.add(searchLbl);
        leftFilters.add(resultsSearchField);
        leftFilters.add(eligLbl);
        leftFilters.add(resultsEligibilityFilter);
        leftFilters.add(statusLbl);
        leftFilters.add(resultsStatusFilter);

        filterBar.add(leftFilters, BorderLayout.WEST);

        // Publication Status and Actions
        JPanel rightPublish = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPublish.setOpaque(false);

        markReviewedBtn = new ModernButton("Mark Reviewed", MarksVectorIcon.create(MarksVectorIcon.Type.CHECK, 13, 13, COLOR_TEXT_PRIMARY),
                COLOR_WHITE, COLOR_TEXT_PRIMARY, COLOR_BORDER, 6);
        markReviewedBtn.addActionListener(e -> updatePublicationStatus("Reviewed"));

        publishResultsBtn = new ModernButton("Publish Results", MarksVectorIcon.create(MarksVectorIcon.Type.END_EXAM, 13, 13, COLOR_WHITE),
                COLOR_SUCCESS, COLOR_WHITE, COLOR_SUCCESS, 6);
        publishResultsBtn.addActionListener(e -> confirmAndPublishResults());

        rightPublish.add(markReviewedBtn);
        rightPublish.add(publishResultsBtn);

        filterBar.add(rightPublish, BorderLayout.EAST);

        tableCard.add(filterBar, BorderLayout.NORTH);

        // Results JTable
        String[] resultCols = {
                "Reg. Number", "Student Name", "CA Mark (100%)", "CA Eligibility",
                "End Exam %", "CA Contrib.", "Final Contrib.", "Total Mark", "Grade", "GPV", "Handbook Status"
        };
        resultsTableModel = new DefaultTableModel(resultCols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        resultsTable = new JTable(resultsTableModel);
        resultsTable.setFont(FONT_TABLE_CELL);
        resultsTable.setRowHeight(36);
        resultsTable.setSelectionBackground(COLOR_ROW_SELECTED);
        resultsTable.setShowGrid(false);

        JTableHeader resHeader = resultsTable.getTableHeader();
        resHeader.setFont(FONT_TABLE_HEADER);
        resHeader.setBackground(new Color(248, 250, 252));
        resHeader.setForeground(COLOR_DEEP_NAVY);
        resHeader.setPreferredSize(new Dimension(resHeader.getWidth(), 38));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        resultsTable.getColumnModel().getColumn(0).setCellRenderer(new RegNoRenderer());
        resultsTable.getColumnModel().getColumn(2).setCellRenderer(center);
        resultsTable.getColumnModel().getColumn(3).setCellRenderer(new CaEligibilityRenderer());
        resultsTable.getColumnModel().getColumn(4).setCellRenderer(center);
        resultsTable.getColumnModel().getColumn(5).setCellRenderer(center);
        resultsTable.getColumnModel().getColumn(6).setCellRenderer(center);
        resultsTable.getColumnModel().getColumn(7).setCellRenderer(new TotalMarkRenderer());
        resultsTable.getColumnModel().getColumn(8).setCellRenderer(new GradeBadgeRenderer());
        resultsTable.getColumnModel().getColumn(9).setCellRenderer(center);
        resultsTable.getColumnModel().getColumn(10).setCellRenderer(new ResultStatusRenderer());

        JScrollPane resScroll = new JScrollPane(resultsTable);
        resScroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        resScroll.getViewport().setBackground(COLOR_WHITE);

        tableCard.add(resScroll, BorderLayout.CENTER);

        panel.add(tableCard, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // DATA LOADING & EVENT HANDLERS
    // =========================================================================

    /**
     * Loads assigned courses for the authenticated lecturer into the course dropdown.
     */
    public void loadCourses() {
        try {
            assignedCourses = StudentDetailsEligibilityLogic.getAssignedCourses(this.lecturerId);
            courseComboBox.removeAllItems();

            if (assignedCourses.isEmpty()) {
                courseComboBox.addItem(null);
                showErrorMessage("No Assigned Courses", "No courses are assigned to your lecturer account in the system.");
                return;
            }

            for (CourseInfo c : assignedCourses) {
                courseComboBox.addItem(c);
            }

            courseComboBox.setSelectedIndex(0);

        } catch (SQLException e) {
            showErrorMessage("Database Error", "Failed to load assigned courses: " + e.getMessage());
        }
    }

    /**
     * Triggered when the lecturer selects a course from the dropdown.
     */
    private void onCourseSelected() {
        selectedCourse = (CourseInfo) courseComboBox.getSelectedItem();
        if (selectedCourse == null) return;

        // Update Header Badges with clean, concise text
        courseCodeBadge.setText(selectedCourse.getCourseCode());
        String typeStr = selectedCourse.getCourseType().replace('_', ' ');
        if ("Theory and Practical".equalsIgnoreCase(typeStr)) {
            typeStr = "Theory + Practical";
        }
        courseTypeBadge.setText(typeStr);

        try {
            int studentCount = MarksDAO.getRegisteredStudentsCount(selectedCourse.getCourseId());
            studentCountBadge.setText(studentCount + " Students");
            studentCountBadge.setToolTipText(studentCount + " Registered Students");

            String pubStatus = ResultsDAO.getCoursePublicationStatus(selectedCourse.getCourseId());
            pubStatusBadge.setText(pubStatus);
            pubStatusBadge.setToolTipText("Publication status: " + pubStatus);
            if ("Published".equalsIgnoreCase(pubStatus)) {
                pubStatusBadge.setBackground(COLOR_SUCCESS_BG);
                pubStatusBadge.setForeground(COLOR_SUCCESS);
            } else {
                pubStatusBadge.setBackground(new Color(241, 245, 249));
                pubStatusBadge.setForeground(COLOR_TEXT_SECONDARY);
            }

            // Load Assessments for selected course
            loadAssessmentsForCourse();

        } catch (SQLException e) {
            showErrorMessage("Data Load Error", "Error retrieving course details: " + e.getMessage());
        }
    }

    /**
     * Loads all configured assessments for the selected course.
     */
    private void loadAssessmentsForCourse() {
        if (selectedCourse == null) return;

        try {
            currentAssessments = AssessmentDAO.getAssessmentsForCourse(selectedCourse.getCourseId());
            WeightValidationReport report = AssessmentDAO.getCourseWeightReport(selectedCourse.getCourseId());

            // Update Progress Badge with concise text that never overflows
            if (report.isValid()) {
                schemeProgressBadge.setText(String.format("Scheme: %.0f%% (Complete)", report.getTotalWeight()));
                schemeProgressBadge.setToolTipText(report.getMessage());
                schemeProgressBadge.setBackground(COLOR_SUCCESS_BG);
                schemeProgressBadge.setForeground(COLOR_SUCCESS);
            } else {
                schemeProgressBadge.setText(String.format("Scheme: %.0f%% (Incomplete)", report.getTotalWeight()));
                schemeProgressBadge.setToolTipText(report.getMessage());
                schemeProgressBadge.setBackground(COLOR_WARNING_BG);
                schemeProgressBadge.setForeground(COLOR_WARNING);
            }

            filterAssessmentsDropdown();

            // Refresh Config Tab table
            loadAssessmentsForConfig();

        } catch (SQLException e) {
            showErrorMessage("Assessments Error", "Failed to load course assessments: " + e.getMessage());
        }
    }

    private void filterAssessmentsDropdown() {
        boolean openOnly = openOnlyCheckBox != null && openOnlyCheckBox.isSelected();
        assessmentComboBox.removeAllItems();

        List<AssessmentComponent> targetList = new ArrayList<>();
        for (AssessmentComponent ac : currentAssessments) {
            if (!openOnly || "Open".equalsIgnoreCase(ac.getStatus()) || "Published".equalsIgnoreCase(ac.getStatus())) {
                targetList.add(ac);
            }
        }

        if (targetList.isEmpty()) {
            assessmentComboBox.addItem(null);
            selectedAssessment = null;
            marksTableModel.setRowCount(0);
            assessmentStatusBadge.setText("NONE");
            assessmentStatusBadge.setBackground(new Color(241, 245, 249));
            assessmentStatusBadge.setForeground(COLOR_TEXT_SECONDARY);
            assessmentTypeBadge.setText("Type: —");
            assessmentMaxBadge.setText("Max: —");
            assessmentWeightBadge.setText("Weight: —");
            assessmentDateBadge.setText("Date: —");
            assessmentCountBadge.setText("0 Students");
            updateStudentInspector(null);
        } else {
            for (AssessmentComponent ac : targetList) {
                assessmentComboBox.addItem(ac);
            }
            assessmentComboBox.setSelectedIndex(0);
        }
    }

    /**
     * Triggered when lecturer selects an assessment from the switcher.
     */
    private void onAssessmentSelected() {
        selectedAssessment = (AssessmentComponent) assessmentComboBox.getSelectedItem();
        if (selectedAssessment == null) return;

        assessmentTypeBadge.setText("Type: " + selectedAssessment.getComponentType() + " (" + selectedAssessment.getAssessmentType() + ")");
        assessmentMaxBadge.setText(String.format("Max Mark: %.1f", selectedAssessment.getMaximumMarks()));
        assessmentWeightBadge.setText(String.format("Weight: %.1f%%", selectedAssessment.getWeightPercentage()));
        String dateStr = (selectedAssessment.getAssessmentDate() != null && !selectedAssessment.getAssessmentDate().trim().isEmpty())
                ? selectedAssessment.getAssessmentDate() : "Not scheduled";
        assessmentDateBadge.setText("Date: " + dateStr);

        String st = selectedAssessment.getStatus();
        assessmentStatusBadge.setText(st.toUpperCase());
        if ("Open".equalsIgnoreCase(st)) {
            assessmentStatusBadge.setBackground(COLOR_SUCCESS_BG);
            assessmentStatusBadge.setForeground(COLOR_SUCCESS);
        } else if ("Published".equalsIgnoreCase(st)) {
            assessmentStatusBadge.setBackground(new Color(239, 246, 255));
            assessmentStatusBadge.setForeground(COLOR_PRO_BLUE);
        } else {
            assessmentStatusBadge.setBackground(COLOR_WARNING_BG);
            assessmentStatusBadge.setForeground(COLOR_WARNING);
        }

        loadMarksForSelectedAssessment();
    }

    /**
     * Navigates to previous or next assessment.
     */
    private void navigateAssessment(int direction) {
        int count = assessmentComboBox.getItemCount();
        if (count <= 1) return;
        int current = assessmentComboBox.getSelectedIndex();
        int next = current + direction;
        if (next >= 0 && next < count) {
            assessmentComboBox.setSelectedIndex(next);
        }
    }

    /**
     * Loads registered students and their marks for the selected assessment.
     * Computes real-time overall CA marks and CA eligibility for each student.
     */
    private void loadMarksForSelectedAssessment() {
        if (selectedCourse == null || selectedAssessment == null) {
            marksTableModel.setRowCount(0);
            updateStudentInspector(null);
            return;
        }

        try {
            currentMarksList = MarksDAO.getStudentMarksForAssessment(
                    selectedCourse.getCourseId(),
                    selectedAssessment.getComponentId(),
                    selectedAssessment.getMaximumMarks(),
                    selectedAssessment.getWeightPercentage(),
                    selectedAssessment.getComponentType()
            );

            assessmentCountBadge.setText(currentMarksList.size() + " Students Registered");
            filterMarksTable();
            updateUnsavedStatus();

            // Select first student by default
            if (!filteredMarksList.isEmpty()) {
                marksTable.setRowSelectionInterval(0, 0);
                updateStudentInspector(filteredMarksList.get(0));
            } else {
                updateStudentInspector(null);
            }

        } catch (SQLException e) {
            showErrorMessage("Marks Load Error", "Failed to retrieve student marks: " + e.getMessage());
        }
    }

    /**
     * Filters the student marks table based on search input.
     */
    private void filterMarksTable() {
        String query = marksSearchField.getText().trim().toLowerCase();
        marksTableModel.setRowCount(0);
        filteredMarksList.clear();

        for (StudentMarkItem item : currentMarksList) {
            if (query.isEmpty() ||
                    item.getStudentRegNo().toLowerCase().contains(query) ||
                    item.getStudentName().toLowerCase().contains(query)) {

                filteredMarksList.add(item);

                String rawStr = item.getRawMark() != null ? String.format("%.2f", item.getRawMark()) : "";
                String maxStr = String.format("%.2f", item.getMaximumMarks());
                String pctStr = item.getNormalizedPercentage() != null ? String.format("%.2f%%", item.getNormalizedPercentage()) : "—";
                String weightedStr = String.format("%.2f%%", item.getWeightedScore());
                String overallScoreStr = String.format("%.2f / %.1f", item.getOverallCaWeightedScore(), item.getTotalCourseCaWeight());
                String overallPctStr = item.getOverallCaPercentage() != null ? String.format("%.2f%%", item.getOverallCaPercentage()) : "—";
                String eligStr = item.getOverallCaEligibility();
                String statusStr = item.isModified() ? "Modified (Unsaved)" : (item.isEntered() ? "Saved" : "Unentered");

                marksTableModel.addRow(new Object[]{
                        item.getStudentRegNo(),
                        item.getStudentName(),
                        rawStr,
                        maxStr,
                        pctStr,
                        weightedStr,
                        overallScoreStr,
                        overallPctStr,
                        eligStr,
                        statusStr
                });
            }
        }
    }

    /**
     * Saves entered and modified marks in a single atomic database transaction.
     */
    private void saveCurrentMarks() {
        if (selectedCourse == null || selectedAssessment == null) {
            return;
        }

        if (marksTable.isEditing()) {
            marksTable.getCellEditor().stopCellEditing();
        }

        try {
            int saved = MarksDAO.saveMarksBatch(
                    selectedCourse.getCourseId(),
                    selectedAssessment.getComponentId(),
                    this.lecturerId,
                    currentMarksList
            );

            JOptionPane.showMessageDialog(this,
                    String.format("Successfully saved %d marks record(s) for %s.\nOverall student CA marks updated.",
                            saved, selectedAssessment.getComponentName()),
                    "Marks Saved Successfully", JOptionPane.INFORMATION_MESSAGE);

            loadMarksForSelectedAssessment();

        } catch (Exception e) {
            showErrorMessage("Save Error", "Failed to save marks: " + e.getMessage());
        }
    }

    /**
     * Clears unsaved changes by reloading the last saved marks from the database.
     */
    private void clearUnsavedMarks() {
        loadMarksForSelectedAssessment();
    }

    private void updateUnsavedStatus() {
        boolean hasModified = false;
        for (StudentMarkItem item : currentMarksList) {
            if (item.isModified()) {
                hasModified = true;
                break;
            }
        }

        if (hasModified) {
            unsavedChangesLabel.setText("● Unsaved changes present in table");
            unsavedChangesLabel.setForeground(COLOR_WARNING);
        } else {
            unsavedChangesLabel.setText("All marks saved to database");
            unsavedChangesLabel.setForeground(COLOR_SUCCESS);
        }
    }

    // =========================================================================
    // QUICK ASSESSMENT CREATION DIALOGS
    // =========================================================================

    private void showQuickAddAssessmentDialog(String defaultType, String defaultCategory, double defaultWeight) {
        if (selectedCourse == null) {
            JOptionPane.showMessageDialog(this, "Please select an assigned course first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AssessmentComponent preset = new AssessmentComponent();
        preset.setCourseId(selectedCourse.getCourseId());
        preset.setAssessmentType(defaultType);
        preset.setComponentType(defaultCategory);
        preset.setMaximumMarks(100.0);
        preset.setWeightPercentage(defaultWeight);
        preset.setStatus("Open");

        // Count existing assessments of this type to suggest a natural name (e.g. "Quiz 02")
        int count = 1;
        for (AssessmentComponent ac : currentAssessments) {
            if (defaultType.equalsIgnoreCase(ac.getAssessmentType())) {
                count++;
            }
        }
        if ("Quiz".equalsIgnoreCase(defaultType)) {
            preset.setComponentName("Quiz " + String.format("%02d", count));
        } else if ("Assignment".equalsIgnoreCase(defaultType)) {
            preset.setComponentName("Assignment " + String.format("%02d", count));
        } else if ("Midterm Exam".equalsIgnoreCase(defaultType)) {
            preset.setComponentName("Midterm Examination");
        } else if ("End Exam".equalsIgnoreCase(defaultType)) {
            preset.setComponentName("End Semester Examination");
        }

        AssessmentDialog dialog = new AssessmentDialog(SwingUtilities.getWindowAncestor(this), "Add New " + defaultType, preset);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            AssessmentComponent comp = dialog.getAssessment();
            comp.setCourseId(selectedCourse.getCourseId());
            try {
                AssessmentDAO.addAssessment(comp, this.lecturerId);
                JOptionPane.showMessageDialog(this, comp.getComponentName() + " added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAssessmentsForCourse();
            } catch (Exception e) {
                showErrorMessage("Add Assessment Error", e.getMessage());
            }
        }
    }

    private void showAddAssessmentDialog() {
        if (selectedCourse == null) {
            JOptionPane.showMessageDialog(this, "Please select a course first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AssessmentDialog dialog = new AssessmentDialog(SwingUtilities.getWindowAncestor(this), "Add Assessment Component", null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            AssessmentComponent comp = dialog.getAssessment();
            comp.setCourseId(selectedCourse.getCourseId());
            try {
                AssessmentDAO.addAssessment(comp, this.lecturerId);
                JOptionPane.showMessageDialog(this, "Assessment added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAssessmentsForCourse();
            } catch (Exception e) {
                showErrorMessage("Add Assessment Error", e.getMessage());
            }
        }
    }

    private void showEditAssessmentDialog() {
        int selectedRow = configTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an assessment from the table to edit.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int compId = (Integer) configTable.getValueAt(selectedRow, 0);
        AssessmentComponent target = null;
        for (AssessmentComponent ac : currentAssessments) {
            if (ac.getComponentId() == compId) {
                target = ac;
                break;
            }
        }

        if (target == null) return;

        AssessmentDialog dialog = new AssessmentDialog(SwingUtilities.getWindowAncestor(this), "Edit Assessment Component", target);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            AssessmentComponent updated = dialog.getAssessment();
            try {
                AssessmentDAO.updateAssessment(updated, this.lecturerId);
                JOptionPane.showMessageDialog(this, "Assessment updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAssessmentsForCourse();
            } catch (Exception e) {
                showErrorMessage("Update Assessment Error", e.getMessage());
            }
        }
    }

    private void deleteSelectedAssessment() {
        int selectedRow = configTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an assessment to delete.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int compId = (Integer) configTable.getValueAt(selectedRow, 0);
        String name = (String) configTable.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete assessment '" + name + "'?\n" +
                "Any recorded marks for this assessment will also be removed.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                AssessmentDAO.deleteAssessment(compId, selectedCourse.getCourseId(), this.lecturerId, true);
                JOptionPane.showMessageDialog(this, "Assessment deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                loadAssessmentsForCourse();
            } catch (Exception e) {
                showErrorMessage("Delete Error", e.getMessage());
            }
        }
    }

    private void loadAssessmentsForConfig() {
        if (selectedCourse == null) return;

        try {
            currentAssessments = AssessmentDAO.getAssessmentsForCourse(selectedCourse.getCourseId());
            WeightValidationReport report = AssessmentDAO.getCourseWeightReport(selectedCourse.getCourseId());

            schemeSummaryLabel.setText(String.format("Assessment Scheme: CA %.1f%% + Final %.1f%% = Total %.1f%% | %s",
                    report.getTotalCaWeight(), report.getTotalFinalWeight(), report.getTotalWeight(), report.getMessage()));

            configTableModel.setRowCount(0);
            for (AssessmentComponent ac : currentAssessments) {
                configTableModel.addRow(new Object[]{
                        ac.getComponentId(),
                        ac.getComponentName(),
                        ac.getComponentType(),
                        ac.getAssessmentType(),
                        String.format("%.2f", ac.getMaximumMarks()),
                        String.format("%.2f%%", ac.getWeightPercentage()),
                        ac.getAssessmentDate() != null ? ac.getAssessmentDate() : "—",
                        ac.getStatus(),
                        ac.getEnteredMarksCount() + " students"
                });
            }

        } catch (SQLException e) {
            showErrorMessage("Scheme Error", "Failed to load assessment scheme: " + e.getMessage());
        }
    }

    // =========================================================================
    // FINAL RESULTS TAB METHODS
    // =========================================================================

    private void loadFinalResults() {
        if (selectedCourse == null) return;

        try {
            currentFinalResults = ResultsDAO.getCourseFinalResults(selectedCourse.getCourseId());
            ResultsSummary summary = ResultsDAO.getResultsSummary(selectedCourse.getCourseId());

            // Update KPI Cards
            kpiTotalStudents.setText(String.valueOf(summary.getTotalRegistered()));
            kpiMarksCompleted.setText(String.valueOf(summary.getMarksCompleted()));
            kpiMarksPending.setText(String.valueOf(summary.getMarksPending()));
            kpiCaEligible.setText(String.valueOf(summary.getCaEligibleCount()));
            kpiCaIneligible.setText(String.valueOf(summary.getCaIneligibleCount()));
            kpiResultsCalculated.setText(String.valueOf(summary.getResultsCalculated()));

            filterFinalResultsTable();

        } catch (SQLException e) {
            showErrorMessage("Results Load Error", "Failed to retrieve results: " + e.getMessage());
        }
    }

    private void filterFinalResultsTable() {
        String query = resultsSearchField.getText().trim().toLowerCase();
        String eligFilter = (String) resultsEligibilityFilter.getSelectedItem();
        String statusFilter = (String) resultsStatusFilter.getSelectedItem();

        resultsTableModel.setRowCount(0);
        filteredFinalResults.clear();

        for (CourseFinalResultItem item : currentFinalResults) {
            boolean matchesSearch = query.isEmpty() ||
                    item.getStudentRegNo().toLowerCase().contains(query) ||
                    item.getFullName().toLowerCase().contains(query);

            boolean matchesElig = eligFilter == null || "All Eligibility".equals(eligFilter) ||
                    item.getCaEligibility().equalsIgnoreCase(eligFilter);

            boolean matchesStatus = statusFilter == null || "All Statuses".equals(statusFilter) ||
                    item.getResultStatus().equalsIgnoreCase(statusFilter);

            if (matchesSearch && matchesElig && matchesStatus) {
                filteredFinalResults.add(item);

                String caPctStr = item.getCaPercentage() != null ? String.format("%.2f%%", item.getCaPercentage()) : "—";
                String endPctStr = item.getEndExamPercentage() != null ? String.format("%.2f%%", item.getEndExamPercentage()) : "—";
                String caScoreStr = item.getWeightedCaScore() != null ? String.format("%.2f", item.getWeightedCaScore()) : "0.00";
                String finalScoreStr = item.getWeightedFinalScore() != null ? String.format("%.2f", item.getWeightedFinalScore()) : "0.00";
                String totalMarkStr = item.getFinalCalculatedMarks() != null ? String.format("%.2f", item.getFinalCalculatedMarks()) : "0.00";
                String gpStr = item.getGradePoint() != null ? String.format("%.2f", item.getGradePoint()) : "0.00";

                resultsTableModel.addRow(new Object[]{
                        item.getStudentRegNo(),
                        item.getFullName(),
                        caPctStr,
                        item.getCaEligibility(),
                        endPctStr,
                        caScoreStr,
                        finalScoreStr,
                        totalMarkStr,
                        item.getLetterGrade(),
                        gpStr,
                        item.getResultStatus()
                });
            }
        }
    }

    private void updatePublicationStatus(String newStatus) {
        if (selectedCourse == null) return;
        try {
            ResultsDAO.updateCoursePublicationStatus(selectedCourse.getCourseId(), newStatus, this.lecturerId);
            pubStatusBadge.setText("Status: " + newStatus);
            JOptionPane.showMessageDialog(this, "Course result status updated to '" + newStatus + "'.", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
            loadFinalResults();
        } catch (Exception e) {
            showErrorMessage("Status Update Error", e.getMessage());
        }
    }

    private void confirmAndPublishResults() {
        if (selectedCourse == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to officially PUBLISH the final results for " + selectedCourse.getCourseCode() + "?\n" +
                "Once published, marks will be protected and officially visible across the faculty.",
                "Confirm Result Publication", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            updatePublicationStatus("Published");
        }
    }

    public void refreshAllData() {
        onCourseSelected();
    }

    // =========================================================================
    // UI HELPERS & CUSTOM MODERN COMPONENTS
    // =========================================================================

    private JPanel createRoundedCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
                g2.setColor(COLOR_BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        return card;
    }

    private JPanel createKpiCard(String labelText, JLabel countLabel, Color accentColor) {
        JPanel card = createRoundedCard();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel title = new JLabel(labelText);
        title.setFont(FONT_CARD_LABEL);
        title.setForeground(COLOR_TEXT_SECONDARY);

        countLabel.setFont(FONT_CARD_COUNT);
        countLabel.setForeground(accentColor);

        card.add(title, BorderLayout.NORTH);
        card.add(countLabel, BorderLayout.CENTER);

        return card;
    }

    private JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(d.width + 16, Math.max(d.height + 4, 26));
            }
        };
        badge.setFont(FONT_BADGE);
        badge.setBackground(bg);
        badge.setForeground(fg);
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        return badge;
    }

    private void showErrorMessage(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    // =========================================================================
    // MODERN BUTTON COMPONENT (Overrides OS LookAndFeel Rendering)
    // =========================================================================
    public static class ModernButton extends JButton {
        private Color normalBg;
        private Color hoverBg;
        private Color pressedBg;
        private Color textColor;
        private Color borderColor;
        private int cornerRadius;
        private boolean hovered = false;
        private boolean pressed = false;

        public ModernButton(String text, Icon icon, Color bg, Color fg, Color border, int radius) {
            super(text, icon);
            this.normalBg = bg;
            this.hoverBg = (bg.getRed() > 220 && bg.getGreen() > 220 && bg.getBlue() > 220) ? bg.darker() : blend(bg, Color.BLACK, 0.12f);
            this.pressedBg = blend(bg, Color.BLACK, 0.22f);
            this.textColor = fg;
            this.borderColor = border;
            this.cornerRadius = radius;

            setFont(FONT_CARD_LABEL);
            setForeground(fg);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setIconTextGap(6);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(6, 12, 6, 12));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; pressed = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
                @Override public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
            });
        }

        public void setCustomColors(Color bg, Color fg, Color border) {
            this.normalBg = bg;
            this.hoverBg = (bg.getRed() > 220 && bg.getGreen() > 220 && bg.getBlue() > 220) ? bg.darker() : blend(bg, Color.BLACK, 0.12f);
            this.pressedBg = blend(bg, Color.BLACK, 0.22f);
            this.textColor = fg;
            this.borderColor = border;
            setForeground(fg);
            repaint();
        }

        private static Color blend(Color c1, Color c2, float ratio) {
            float r = (float) c1.getRed() * (1f - ratio) + (float) c2.getRed() * ratio;
            float g = (float) c1.getGreen() * (1f - ratio) + (float) c2.getGreen() * ratio;
            float b = (float) c1.getBlue() * (1f - ratio) + (float) c2.getBlue() * ratio;
            return new Color((int) r, (int) g, (int) b);
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            int textW = fm != null ? fm.stringWidth(getText() != null ? getText() : "") : 0;
            int iconW = getIcon() != null ? getIcon().getIconWidth() + getIconTextGap() : 0;
            int minW = textW + iconW + 28;

            if (isPreferredSizeSet()) {
                Dimension pref = super.getPreferredSize();
                return new Dimension(Math.max(pref.width, minW), Math.max(pref.height, 32));
            }
            Dimension d = super.getPreferredSize();
            return new Dimension(Math.max(d.width + 14, minW), Math.max(d.height, 32));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Color fill = !isEnabled() ? new Color(226, 232, 240) : (pressed ? pressedBg : (hovered ? hoverBg : normalBg));
            g2.setColor(fill);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));

            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, cornerRadius, cornerRadius));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // =========================================================================
    // CUSTOM VECTOR ICONS (Zero Missing Emojis / Native Graphics2D Rendering)
    // =========================================================================
    public static class MarksVectorIcon implements Icon {
        public enum Type {
            ADD, SAVE, REFRESH, QUIZ, ASSIGNMENT, MIDTERM, END_EXAM,
            SEARCH, CHECK, ALERT, CHEVRON_LEFT, CHEVRON_RIGHT, EDIT, TRASH
        }

        private final Type type;
        private final int width;
        private final int height;
        private final Color color;

        public MarksVectorIcon(Type type, int width, int height, Color color) {
            this.type = type;
            this.width = width;
            this.height = height;
            this.color = color;
        }

        public static MarksVectorIcon create(Type type, int w, int h, Color color) {
            return new MarksVectorIcon(type, w, h, color);
        }

        @Override public int getIconWidth() { return width; }
        @Override public int getIconHeight() { return height; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.setColor(color != null ? color : c.getForeground());

            switch (type) {
                case ADD:
                    g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(x + width / 2, y + 2, x + width / 2, y + height - 2);
                    g2.drawLine(x + 2, y + height / 2, x + width - 2, y + height / 2);
                    break;

                case SAVE:
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, 2, 2);
                    g2.fillRect(x + 4, y + 2, width - 8, 4);
                    g2.drawRect(x + 3, y + height - 6, width - 7, 4);
                    break;

                case REFRESH:
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawArc(x + 2, y + 2, width - 5, height - 5, 45, 270);
                    Polygon arrow = new Polygon();
                    arrow.addPoint(x + width - 4, y + 1);
                    arrow.addPoint(x + width, y + 5);
                    arrow.addPoint(x + width - 6, y + 6);
                    g2.fill(arrow);
                    break;

                case QUIZ:
                    g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawRoundRect(x + 2, y + 2, width - 4, height - 4, 3, 3);
                    // Draw clean checklist marks
                    g2.drawLine(x + 4, y + 5, x + 6, y + 7);
                    g2.drawLine(x + 6, y + 7, x + 9, y + 4);
                    g2.drawLine(x + 4, y + 9, x + 6, y + 11);
                    g2.drawLine(x + 6, y + 11, x + 9, y + 8);
                    break;

                case ASSIGNMENT:
                    g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Path2D doc = new Path2D.Float();
                    doc.moveTo(x + 2, y + 1);
                    doc.lineTo(x + width - 5, y + 1);
                    doc.lineTo(x + width - 2, y + 4);
                    doc.lineTo(x + width - 2, y + height - 2);
                    doc.lineTo(x + 2, y + height - 2);
                    doc.closePath();
                    g2.draw(doc);
                    g2.drawLine(x + 5, y + 6, x + width - 5, y + 6);
                    g2.drawLine(x + 5, y + 9, x + width - 5, y + 9);
                    break;

                case MIDTERM:
                    g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawOval(x + 2, y + 2, width - 5, height - 5);
                    g2.drawLine(x + width / 2, y + 4, x + width / 2, y + height / 2);
                    g2.drawLine(x + width / 2, y + height / 2, x + width / 2 + 3, y + height / 2);
                    break;

                case END_EXAM:
                    g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Polygon cap = new Polygon();
                    cap.addPoint(x + width / 2, y + 2);
                    cap.addPoint(x + width - 2, y + 6);
                    cap.addPoint(x + width / 2, y + 10);
                    cap.addPoint(x + 2, y + 6);
                    g2.draw(cap);
                    g2.drawArc(x + 4, y + 7, width - 8, 4, 0, -180);
                    g2.drawLine(x + width - 2, y + 6, x + width - 2, y + 11);
                    break;

                case SEARCH:
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int rad = width - 6;
                    g2.drawOval(x + 1, y + 1, rad, rad);
                    g2.drawLine(x + rad - 1, y + rad - 1, x + width - 2, y + height - 2);
                    break;

                case CHECK:
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Path2D p = new Path2D.Float();
                    p.moveTo(x + 2, y + height / 2);
                    p.lineTo(x + width / 2 - 1, y + height - 3);
                    p.lineTo(x + width - 2, y + 3);
                    g2.draw(p);
                    break;

                case ALERT:
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Polygon tri = new Polygon();
                    tri.addPoint(x + width / 2, y + 1);
                    tri.addPoint(x + width - 1, y + height - 2);
                    tri.addPoint(x + 1, y + height - 2);
                    g2.draw(tri);
                    g2.drawLine(x + width / 2, y + 5, x + width / 2, y + height - 6);
                    g2.fillRect(x + width / 2 - 1, y + height - 4, 2, 2);
                    break;

                case CHEVRON_LEFT:
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(x + width - 4, y + 2, x + 3, y + height / 2);
                    g2.drawLine(x + 3, y + height / 2, x + width - 4, y + height - 2);
                    break;

                case CHEVRON_RIGHT:
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(x + 4, y + 2, x + width - 3, y + height / 2);
                    g2.drawLine(x + width - 3, y + height / 2, x + 4, y + height - 2);
                    break;

                case EDIT:
                    g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(x + 2, y + height - 2, x + 5, y + height - 2);
                    g2.drawLine(x + 2, y + height - 2, x + 2, y + height - 5);
                    g2.drawLine(x + 3, y + height - 5, x + width - 3, y + 2);
                    g2.drawLine(x + 5, y + height - 3, x + width - 1, y + 4);
                    g2.drawLine(x + width - 3, y + 2, x + width - 1, y + 4);
                    break;

                case TRASH:
                    g2.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(x + 2, y + 3, x + width - 2, y + 3);
                    g2.drawRoundRect(x + 3, y + 3, width - 7, height - 5, 2, 2);
                    g2.drawLine(x + 6, y + 5, x + 6, y + height - 4);
                    g2.drawLine(x + width - 6, y + 5, x + width - 6, y + height - 4);
                    break;
            }

            g2.dispose();
        }
    }

    // =========================================================================
    // CUSTOM TABLE CELL RENDERERS & EDITORS
    // =========================================================================

    private static class RegNoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
            setForeground(COLOR_DEEP_NAVY);
            setBorder(new EmptyBorder(0, 10, 0, 0));
            return c;
        }
    }

    private static class RawMarkCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            String val = value != null ? value.toString().trim() : "";

            if (!isSelected) {
                setBackground(new Color(241, 248, 255)); // Soft blue highlight for editable column
            }

            if (val.isEmpty()) {
                setText("Click to enter");
                setForeground(COLOR_TEXT_MUTED);
                setFont(new Font("Segoe UI", Font.ITALIC, 11));
            } else {
                setText(val);
                setForeground(COLOR_PRO_BLUE);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
            }
            return this;
        }
    }

    private static class PercentageCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            String val = value != null ? value.toString() : "";
            if (val.contains("%")) {
                try {
                    double num = Double.parseDouble(val.replace("%", "").trim());
                    if (num >= 40.0) {
                        setForeground(COLOR_SUCCESS);
                    } else {
                        setForeground(COLOR_DANGER);
                    }
                } catch (Exception ignored) {
                    setForeground(COLOR_TEXT_PRIMARY);
                }
            } else {
                setForeground(COLOR_TEXT_MUTED);
            }
            return this;
        }
    }

    private static class OverallCaPercentageCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            String val = value != null ? value.toString() : "";
            if (val.contains("%")) {
                try {
                    double num = Double.parseDouble(val.replace("%", "").trim());
                    if (num >= 40.0) {
                        setForeground(COLOR_SUCCESS);
                    } else {
                        setForeground(COLOR_DANGER);
                    }
                } catch (Exception ignored) {
                    setForeground(COLOR_TEXT_PRIMARY);
                }
            } else {
                setForeground(COLOR_TEXT_MUTED);
            }
            return this;
        }
    }

    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            String val = value != null ? value.toString() : "";
            if (val.contains("Modified")) {
                setForeground(COLOR_WARNING);
                setFont(new Font("Segoe UI Semibold", Font.BOLD, 11));
            } else if ("Saved".equalsIgnoreCase(val) || val.contains("Entered")) {
                setForeground(COLOR_SUCCESS);
            } else {
                setForeground(COLOR_TEXT_MUTED);
            }
            return this;
        }
    }

    private static class CaEligibilityRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            String val = value != null ? value.toString() : "";
            if ("ELIGIBLE".equalsIgnoreCase(val)) {
                setForeground(COLOR_SUCCESS);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
            } else if ("NOT_ELIGIBLE".equalsIgnoreCase(val)) {
                setForeground(COLOR_DANGER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
            } else {
                setForeground(COLOR_WARNING);
            }
            return this;
        }
    }

    private static class GradeBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            String g = value != null ? value.toString().trim() : "";
            if (g.startsWith("A")) {
                setForeground(new Color(16, 185, 129));
            } else if (g.startsWith("B")) {
                setForeground(new Color(14, 165, 233));
            } else if (g.startsWith("C")) {
                setForeground(new Color(234, 179, 8));
            } else if (g.startsWith("D")) {
                setForeground(new Color(249, 115, 22));
            } else {
                setForeground(COLOR_DANGER);
            }
            return this;
        }
    }

    private static class TotalMarkRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
            setForeground(COLOR_DEEP_NAVY);
            return this;
        }
    }

    private static class ResultStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            String s = value != null ? value.toString().trim() : "";
            if (s.startsWith("Pass")) {
                setForeground(COLOR_SUCCESS);
            } else if (s.contains("EE") || s.contains("EC")) {
                setForeground(COLOR_WARNING);
            } else {
                setForeground(COLOR_DANGER);
            }
            return this;
        }
    }

    /**
     * Custom TableCellEditor for inline marks editing with numeric and boundary validation.
     * Automatically triggers real-time overall CA marks recalculation upon cell edit.
     */
    private class RawMarkCellEditor extends DefaultCellEditor {
        private final JTextField textField;
        private int editingRow;

        public RawMarkCellEditor() {
            super(new JTextField());
            this.textField = (JTextField) getComponent();
            this.textField.setFont(FONT_TABLE_CELL);
            this.textField.setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.editingRow = row;
            textField.setText(value != null ? value.toString() : "");
            textField.selectAll();
            return textField;
        }

        @Override
        public boolean stopCellEditing() {
            String text = textField.getText().trim();
            double maxAllowed = selectedAssessment != null ? selectedAssessment.getMaximumMarks() : 100.00;

            MarksCalculationService.ValidationResult res = MarksCalculationService.validateRawMark(text, maxAllowed);
            if (!res.isValid()) {
                JOptionPane.showMessageDialog(marksTable, res.getErrorMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            // Valid -> Update underlying model item and recalculate overall CA marks immediately!
            if (editingRow >= 0 && editingRow < filteredMarksList.size()) {
                StudentMarkItem item = filteredMarksList.get(editingRow);
                item.setRawMark(res.getParsedValue());
                updateUnsavedStatus();

                // If currently inspected student is this student, refresh inspector displays
                if (selectedStudentItem != null && selectedStudentItem.getStudentId() == item.getStudentId()) {
                    updateStudentInspector(item);
                }
            }

            return super.stopCellEditing();
        }

        @Override
        public Object getCellEditorValue() {
            String text = textField.getText().trim();
            if (text.isEmpty()) return "";
            try {
                return String.format("%.2f", Double.parseDouble(text));
            } catch (Exception e) {
                return text;
            }
        }
    }

    // =========================================================================
    // MODAL DIALOG: ADD / EDIT ASSESSMENT COMPONENT
    // =========================================================================

    private static class AssessmentDialog extends JDialog {
        private final JTextField nameField = new JTextField();
        private final JComboBox<String> categoryCombo = new JComboBox<>(new String[]{"CA", "Final"});
        private final JComboBox<String> typeCombo = new JComboBox<>(new String[]{
                "Quiz", "Assignment", "Mini-Project", "Midterm Exam", "End Exam", "Continuous Assessment", "Other"
        });
        private final JTextField maxMarksField = new JTextField("100.00");
        private final JTextField weightField = new JTextField("10.00");
        private final JTextField dateField = new JTextField(); // yyyy-MM-dd
        private final JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Open", "Draft", "Closed", "Published"});

        private boolean confirmed = false;
        private AssessmentComponent assessment;

        public AssessmentDialog(Window owner, String title, AssessmentComponent existing) {
            super(owner, title, ModalityType.APPLICATION_MODAL);
            setSize(460, 440);
            setLocationRelativeTo(owner);
            setResizable(false);

            if (existing != null) {
                this.assessment = existing;
                nameField.setText(existing.getComponentName());
                categoryCombo.setSelectedItem(existing.getComponentType());
                typeCombo.setSelectedItem(existing.getAssessmentType());
                maxMarksField.setText(String.format("%.2f", existing.getMaximumMarks()));
                weightField.setText(String.format("%.2f", existing.getWeightPercentage()));
                dateField.setText(existing.getAssessmentDate() != null ? existing.getAssessmentDate() : "");
                statusCombo.setSelectedItem(existing.getStatus());
            } else {
                this.assessment = new AssessmentComponent();
            }

            // Sync category with type selection
            typeCombo.addActionListener(e -> {
                String selectedType = (String) typeCombo.getSelectedItem();
                if ("End Exam".equalsIgnoreCase(selectedType)) {
                    categoryCombo.setSelectedItem("Final");
                } else if (!"Other".equalsIgnoreCase(selectedType)) {
                    categoryCombo.setSelectedItem("CA");
                }
            });

            JPanel content = new JPanel(new BorderLayout(0, 14));
            content.setBackground(COLOR_WHITE);
            content.setBorder(new EmptyBorder(18, 22, 18, 22));

            JPanel form = new JPanel(new GridLayout(7, 2, 10, 12));
            form.setOpaque(false);

            form.add(new JLabel("Assessment Name:"));
            form.add(nameField);

            form.add(new JLabel("Assessment Type:"));
            form.add(typeCombo);

            form.add(new JLabel("Category (CA / Final):"));
            form.add(categoryCombo);

            form.add(new JLabel("Maximum Raw Mark:"));
            form.add(maxMarksField);

            form.add(new JLabel("Course Weight (%):"));
            form.add(weightField);

            form.add(new JLabel("Scheduled Date (YYYY-MM-DD):"));
            form.add(dateField);

            form.add(new JLabel("Status:"));
            form.add(statusCombo);

            content.add(form, BorderLayout.CENTER);

            // Modern Buttons
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            btnPanel.setOpaque(false);

            ModernButton cancelBtn = new ModernButton("Cancel", null, COLOR_WHITE, COLOR_TEXT_PRIMARY, COLOR_BORDER, 6);
            cancelBtn.addActionListener(e -> dispose());

            ModernButton saveBtn = new ModernButton("Save Assessment", MarksVectorIcon.create(MarksVectorIcon.Type.CHECK, 13, 13, COLOR_WHITE),
                    COLOR_PRO_BLUE, COLOR_WHITE, COLOR_PRO_BLUE, 6);
            saveBtn.addActionListener(e -> onSave());

            btnPanel.add(cancelBtn);
            btnPanel.add(saveBtn);

            content.add(btnPanel, BorderLayout.SOUTH);
            setContentPane(content);
        }

        private void onSave() {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter an assessment name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double maxM;
            double weight;
            try {
                maxM = Double.parseDouble(maxMarksField.getText().trim());
                if (maxM <= 0) throw new NumberFormatException();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Maximum mark must be a positive number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                weight = Double.parseDouble(weightField.getText().trim());
                if (weight <= 0 || weight > 100.0) throw new NumberFormatException();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Weight must be between 0.1% and 100%.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            assessment.setComponentName(name);
            assessment.setComponentType((String) categoryCombo.getSelectedItem());
            assessment.setAssessmentType((String) typeCombo.getSelectedItem());
            assessment.setMaximumMarks(maxM);
            assessment.setWeightPercentage(weight);
            String date = dateField.getText().trim();
            assessment.setAssessmentDate(date.isEmpty() ? null : date);
            assessment.setStatus((String) statusCombo.getSelectedItem());

            this.confirmed = true;
            dispose();
        }

        public boolean isConfirmed() { return confirmed; }
        public AssessmentComponent getAssessment() { return assessment; }
    }

}

