package main.java.ui.admin;

import main.java.dao.admin.ReportsDAO;
import main.java.model.admin.CourseEnrollmentReportItem;
import main.java.model.admin.DepartmentStaffReportItem;
import main.java.model.admin.StaffDetailReportItem;
import main.java.model.admin.StudentEligibilityReportItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class ReportsPanel extends JPanel {

    // Main navigation cards
    private CardLayout reportCardLayout;
    private JPanel reportContainer;

    // Tab buttons
    private JButton tabEligibilityBtn;
    private JButton tabEnrollmentBtn;
    private JButton tabStaffBtn;
    private String currentReportTab = "ELIGIBILITY";

    // Departments and Courses for dropdowns
    private Map<Integer, String> departmentMap = new LinkedHashMap<>();
    private Map<Integer, String> courseMap = new LinkedHashMap<>();

    // --- Report 1 Components (Eligibility) ---
    private JTable eligibilityTable;
    private DefaultTableModel eligibilityModel;
    private TableRowSorter<DefaultTableModel> eligibilitySorter;
    private JTextField eligibilitySearch;
    private JComboBox<String> eligibilityCourseCombo;
    private JComboBox<String> eligibilityDeptCombo;
    private JComboBox<String> eligibilityStatusCombo;
    private JLabel eligibilityTotalKpi;
    private JLabel eligibilityEligibleKpi;
    private JLabel eligibilityIneligibleKpi;
    private JLabel eligibilityRateKpi;
    private JLabel eligibilityFooterLabel;

    // --- Report 2 Components (Course Enrollment) ---
    private JTable enrollmentTable;
    private DefaultTableModel enrollmentModel;
    private TableRowSorter<DefaultTableModel> enrollmentSorter;
    private JTextField enrollmentSearch;
    private JComboBox<String> enrollmentDeptCombo;
    private JComboBox<String> enrollmentYearCombo;
    private JComboBox<String> enrollmentSemCombo;
    private JLabel enrollmentCoursesKpi;
    private JLabel enrollmentStudentsKpi;
    private JLabel enrollmentCreditsKpi;
    private JLabel enrollmentAvgKpi;
    private JLabel enrollmentFooterLabel;

    // --- Report 3 Components (Faculty Staff) ---
    private JTable deptStaffTable;
    private DefaultTableModel deptStaffModel;
    private JTable staffDetailTable;
    private DefaultTableModel staffDetailModel;
    private TableRowSorter<DefaultTableModel> staffDetailSorter;
    private JTextField staffSearch;
    private JComboBox<String> staffDeptCombo;
    private JComboBox<String> staffRoleCombo;
    private JLabel staffLecturersKpi;
    private JLabel staffOfficersKpi;
    private JLabel staffStudentsKpi;
    private JLabel staffRatioKpi;
    private JLabel staffFooterLabel;

    public ReportsPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        loadReferenceData();
        initUI();
        refreshAllReports();
    }

    private void loadReferenceData() {
        departmentMap = ReportsDAO.getAllDepartments();
        courseMap = ReportsDAO.getAllCourses();
    }

    private void initUI() {
        // --- Top Bar (Title + Tab Navigation + Actions) ---
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.setBorder(new EmptyBorder(25, 25, 15, 25));

        // 1. Header Title & Main Action Buttons
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("Academic & Institutional Reports");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(23, 32, 48));

        JLabel subtitleLabel = new JLabel("Live administrative intelligence, student examination eligibility, and faculty analytics");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(110, 120, 135));

        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitleLabel);

        // Header Actions (Refresh & Export)
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsPanel.setOpaque(false);

        JButton refreshBtn = createStyledButton(" Refresh Data", new Color(240, 243, 248), new Color(40, 50, 70), true);
        refreshBtn.setPreferredSize(new Dimension(135, 36));
        refreshBtn.addActionListener(e -> {
            loadReferenceData();
            refreshAllReports();
        });

        JButton exportBtn = createStyledButton(" Export CSV", new Color(16, 124, 65), Color.WHITE, false);
        exportBtn.setPreferredSize(new Dimension(135, 36));
        exportBtn.addActionListener(e -> exportActiveReportToCSV());

        actionsPanel.add(refreshBtn);
        actionsPanel.add(exportBtn);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(actionsPanel, BorderLayout.EAST);

        // 2. Tab Navigation Bar
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        navBar.setOpaque(false);
        navBar.setBorder(new EmptyBorder(18, 0, 0, 0));

        tabEligibilityBtn = createTabButton("Exam Eligibility & Attendance", true);
        tabEnrollmentBtn = createTabButton("Course Enrollment & Curriculum", false);
        tabStaffBtn = createTabButton("Faculty Staff & Resources", false);

        tabEligibilityBtn.addActionListener(e -> switchReportTab("ELIGIBILITY"));
        tabEnrollmentBtn.addActionListener(e -> switchReportTab("ENROLLMENT"));
        tabStaffBtn.addActionListener(e -> switchReportTab("STAFF"));

        navBar.add(tabEligibilityBtn);
        navBar.add(tabEnrollmentBtn);
        navBar.add(tabStaffBtn);

        topContainer.add(headerPanel);
        topContainer.add(navBar);
        add(topContainer, BorderLayout.NORTH);

        // --- Card Container for the 3 Reports ---
        reportCardLayout = new CardLayout();
        reportContainer = new JPanel(reportCardLayout);
        reportContainer.setOpaque(false);

        reportContainer.add(createEligibilityReportView(), "ELIGIBILITY");
        reportContainer.add(createEnrollmentReportView(), "ENROLLMENT");
        reportContainer.add(createStaffReportView(), "STAFF");

        add(reportContainer, BorderLayout.CENTER);
    }

    private void switchReportTab(String tabKey) {
        currentReportTab = tabKey;
        reportCardLayout.show(reportContainer, tabKey);

        updateTabButtonStyles(tabEligibilityBtn, "ELIGIBILITY".equals(tabKey));
        updateTabButtonStyles(tabEnrollmentBtn, "ENROLLMENT".equals(tabKey));
        updateTabButtonStyles(tabStaffBtn, "STAFF".equals(tabKey));
    }

    private JButton createTabButton(String text, boolean active) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getBackground() != null) {
                    g2.setColor(getBackground());
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                }
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        updateTabButtonStyles(btn, active);
        return btn;
    }

    private void updateTabButtonStyles(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(new Color(23, 32, 48)); // Deep brand navy
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(90, 100, 115));
        }
        btn.repaint();
    }

    // =========================================================================
    // VIEW 1: STUDENT EXAM ELIGIBILITY & ATTENDANCE REPORT
    // =========================================================================
    private JPanel createEligibilityReportView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(5, 25, 20, 25));

        // 1. KPI Metric Cards
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        eligibilityTotalKpi = new JLabel("0");
        eligibilityEligibleKpi = new JLabel("0");
        eligibilityIneligibleKpi = new JLabel("0");
        eligibilityRateKpi = new JLabel("0.0%");

        kpiPanel.add(createKpiCard("Students Evaluated", eligibilityTotalKpi, "Registered in course", new Color(225, 238, 255), new Color(30, 100, 220)));
        kpiPanel.add(createKpiCard("Exam Eligible", eligibilityEligibleKpi, ">= 80% (incl. medicals)", new Color(230, 248, 234), new Color(22, 163, 74)));
        kpiPanel.add(createKpiCard("Not Eligible", eligibilityIneligibleKpi, "Below minimum criteria", new Color(254, 235, 235), new Color(220, 38, 38)));
        kpiPanel.add(createKpiCard("Eligibility Rate", eligibilityRateKpi, "Target: 100%", new Color(246, 234, 255), new Color(130, 60, 220)));

        // 2. Filter Bar
        JPanel filterCard = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 8));
        filterCard.setBackground(Color.WHITE);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 232, 238), 1, true),
            new EmptyBorder(8, 14, 8, 14)
        ));

        JLabel searchIcon = new JLabel("Search:");
        searchIcon.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchIcon.setForeground(new Color(100, 110, 125));

        eligibilitySearch = new JTextField(12);
        eligibilitySearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        eligibilitySearch.putClientProperty("JTextField.placeholderText", "Reg No or Name...");
        eligibilitySearch.setPreferredSize(new Dimension(145, 32));

        JLabel courseLbl = new JLabel("Course:");
        courseLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        courseLbl.setForeground(new Color(100, 110, 125));

        eligibilityCourseCombo = new JComboBox<>();
        populateCourseCombo(eligibilityCourseCombo);
        eligibilityCourseCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        eligibilityCourseCombo.setPreferredSize(new Dimension(175, 32));

        JLabel deptLbl = new JLabel("Dept:");
        deptLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deptLbl.setForeground(new Color(100, 110, 125));

        eligibilityDeptCombo = new JComboBox<>();
        populateDepartmentCombo(eligibilityDeptCombo);
        eligibilityDeptCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        eligibilityDeptCombo.setPreferredSize(new Dimension(165, 32));

        JLabel statusLbl = new JLabel("Status:");
        statusLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLbl.setForeground(new Color(100, 110, 125));

        eligibilityStatusCombo = new JComboBox<>(new String[]{"All Statuses", "Eligible", "Not Eligible", "Pending"});
        eligibilityStatusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        eligibilityStatusCombo.setPreferredSize(new Dimension(115, 32));

        JButton resetBtn = createStyledButton("Reset", new Color(240, 243, 248), new Color(70, 80, 95), true);
        resetBtn.setPreferredSize(new Dimension(68, 32));
        resetBtn.addActionListener(e -> {
            eligibilitySearch.setText("");
            if (eligibilityCourseCombo.getItemCount() > 0) eligibilityCourseCombo.setSelectedIndex(0);
            if (eligibilityDeptCombo.getItemCount() > 0) eligibilityDeptCombo.setSelectedIndex(0);
            eligibilityStatusCombo.setSelectedIndex(0);
            loadEligibilityReportData();
        });

        filterCard.add(searchIcon);
        filterCard.add(eligibilitySearch);
        filterCard.add(courseLbl);
        filterCard.add(eligibilityCourseCombo);
        filterCard.add(deptLbl);
        filterCard.add(eligibilityDeptCombo);
        filterCard.add(statusLbl);
        filterCard.add(eligibilityStatusCombo);
        filterCard.add(resetBtn);

        // Filter event listeners
        eligibilityCourseCombo.addActionListener(e -> loadEligibilityReportData());
        eligibilityDeptCombo.addActionListener(e -> loadEligibilityReportData());
        eligibilityStatusCombo.addActionListener(e -> loadEligibilityReportData());

        eligibilitySearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterEligibilityTable(); }
            public void removeUpdate(DocumentEvent e) { filterEligibilityTable(); }
            public void changedUpdate(DocumentEvent e) { filterEligibilityTable(); }
        });

        // 3. Table Setup
        String[] cols = {
            "Reg No", "Student Name", "Course", "Sessions", "Present", "Absent", "Medicals", "Attendance %", "CA Marks (40%)", "Exam Eligibility"
        };
        eligibilityModel = new DefaultTableModel(null, cols) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        eligibilityTable = new JTable(eligibilityModel);
        setupTableStyle(eligibilityTable);

        // Custom Renderers
        eligibilityTable.getColumnModel().getColumn(7).setCellRenderer(new AttendancePercentageRenderer());
        eligibilityTable.getColumnModel().getColumn(8).setCellRenderer(new CAMarksRenderer());
        eligibilityTable.getColumnModel().getColumn(9).setCellRenderer(new EligibilityBadgeRenderer());

        eligibilityTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        eligibilityTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        eligibilityTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        eligibilityTable.getColumnModel().getColumn(3).setPreferredWidth(65);
        eligibilityTable.getColumnModel().getColumn(4).setPreferredWidth(65);
        eligibilityTable.getColumnModel().getColumn(5).setPreferredWidth(65);
        eligibilityTable.getColumnModel().getColumn(6).setPreferredWidth(70);
        eligibilityTable.getColumnModel().getColumn(7).setPreferredWidth(105);
        eligibilityTable.getColumnModel().getColumn(8).setPreferredWidth(110);
        eligibilityTable.getColumnModel().getColumn(9).setPreferredWidth(150);

        eligibilitySorter = new TableRowSorter<>(eligibilityModel);
        eligibilityTable.setRowSorter(eligibilitySorter);

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.setBorder(BorderFactory.createLineBorder(new Color(230, 232, 238), 1, true));

        JScrollPane scrollPane = new JScrollPane(eligibilityTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableWrapper.add(scrollPane, BorderLayout.CENTER);

        // 4. Footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 5, 0, 5));

        eligibilityFooterLabel = new JLabel("Showing 0 records");
        eligibilityFooterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        eligibilityFooterLabel.setForeground(new Color(110, 120, 135));

        JLabel ruleNotice = new JLabel("ⓘ University Criteria: Minimum 80.0% attendance (including approved medicals) is required to sit for examinations.");
        ruleNotice.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        ruleNotice.setForeground(new Color(110, 120, 135));

        footer.add(eligibilityFooterLabel, BorderLayout.WEST);
        footer.add(ruleNotice, BorderLayout.EAST);

        // Assemble View 1
        JPanel topWrapper = new JPanel();
        topWrapper.setLayout(new BoxLayout(topWrapper, BoxLayout.Y_AXIS));
        topWrapper.setOpaque(false);
        topWrapper.add(kpiPanel);
        topWrapper.add(filterCard);
        topWrapper.add(Box.createVerticalStrut(18)); // Bottom margin to separate filter bar from table cleanly

        panel.add(topWrapper, BorderLayout.NORTH);
        panel.add(tableWrapper, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private void loadEligibilityReportData() {
        Integer selectedCourseId = getSelectedIdFromCombo(eligibilityCourseCombo, courseMap);
        Integer selectedDeptId = getSelectedIdFromCombo(eligibilityDeptCombo, departmentMap);
        String status = (String) eligibilityStatusCombo.getSelectedItem();
        if ("All Statuses".equals(status)) status = null;

        List<StudentEligibilityReportItem> list = ReportsDAO.getStudentEligibilityReport(selectedCourseId, selectedDeptId, status);

        eligibilityModel.setRowCount(0);
        int eligibleCount = 0;
        int ineligibleCount = 0;

        for (StudentEligibilityReportItem item : list) {
            String caStr = item.getCaMarks() != null ? String.format("%.2f", item.getCaMarks()) : "N/A";
            eligibilityModel.addRow(new Object[]{
                item.getStudentRegNo(),
                item.getStudentName(),
                item.getCourseCode(),
                item.getTotalSessions(),
                item.getPresentCount(),
                item.getAbsentCount(),
                item.getApprovedMedicals(),
                String.format("%.1f%%", item.getAttendancePercentage()),
                caStr,
                item.getEligibilityStatus()
            });

            if (item.getEligibilityStatus().startsWith("Eligible")) {
                eligibleCount++;
            } else if ("Not Eligible".equals(item.getEligibilityStatus())) {
                ineligibleCount++;
            }
        }

        int total = list.size();
        eligibilityTotalKpi.setText(String.valueOf(total));
        eligibilityEligibleKpi.setText(String.valueOf(eligibleCount));
        eligibilityIneligibleKpi.setText(String.valueOf(ineligibleCount));

        double rate = total > 0 ? ((double) eligibleCount / total) * 100.0 : 0.0;
        eligibilityRateKpi.setText(String.format("%.1f%%", rate));

        eligibilityFooterLabel.setText(String.format("Showing %d students (Eligible: %d | Ineligible: %d)", total, eligibleCount, ineligibleCount));
        filterEligibilityTable();
    }

    private void filterEligibilityTable() {
        if (eligibilitySorter == null) return;
        String query = eligibilitySearch.getText().trim();
        if (query.isEmpty()) {
            eligibilitySorter.setRowFilter(null);
        } else {
            eligibilitySorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 0, 1, 2));
        }
        int filteredCount = eligibilityTable.getRowCount();
        eligibilityFooterLabel.setText(String.format("Showing %d records (filtered)", filteredCount));
    }

    // =========================================================================
    // VIEW 2: COURSE ENROLLMENT & ACADEMIC CURRICULUM REPORT
    // =========================================================================
    private JPanel createEnrollmentReportView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(5, 25, 20, 25));

        // 1. KPI Metric Cards
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        enrollmentCoursesKpi = new JLabel("0");
        enrollmentStudentsKpi = new JLabel("0");
        enrollmentCreditsKpi = new JLabel("0");
        enrollmentAvgKpi = new JLabel("0");

        kpiPanel.add(createKpiCard("Active Courses", enrollmentCoursesKpi, "Current semester curriculum", new Color(225, 238, 255), new Color(30, 100, 220)));
        kpiPanel.add(createKpiCard("Total Enrollments", enrollmentStudentsKpi, "Student course registrations", new Color(230, 248, 234), new Color(22, 163, 74)));
        kpiPanel.add(createKpiCard("Total Credits", enrollmentCreditsKpi, "Cumulative academic credits", new Color(254, 243, 225), new Color(217, 119, 6)));
        kpiPanel.add(createKpiCard("Avg Students / Course", enrollmentAvgKpi, "Classroom capacity ratio", new Color(246, 234, 255), new Color(130, 60, 220)));

        // 2. Filter Bar
        JPanel filterCard = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 8));
        filterCard.setBackground(Color.WHITE);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 232, 238), 1, true),
            new EmptyBorder(8, 14, 8, 14)
        ));

        JLabel searchIcon = new JLabel("Search:");
        searchIcon.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchIcon.setForeground(new Color(100, 110, 125));

        enrollmentSearch = new JTextField(12);
        enrollmentSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        enrollmentSearch.putClientProperty("JTextField.placeholderText", "Course code/title...");
        enrollmentSearch.setPreferredSize(new Dimension(160, 32));

        JLabel deptLbl = new JLabel("Dept:");
        deptLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deptLbl.setForeground(new Color(100, 110, 125));

        enrollmentDeptCombo = new JComboBox<>();
        populateDepartmentCombo(enrollmentDeptCombo);
        enrollmentDeptCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        enrollmentDeptCombo.setPreferredSize(new Dimension(175, 32));

        JLabel yearLbl = new JLabel("Year:");
        yearLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        yearLbl.setForeground(new Color(100, 110, 125));

        enrollmentYearCombo = new JComboBox<>(new String[]{"All Years", "Year 1", "Year 2", "Year 3", "Year 4"});
        enrollmentYearCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        enrollmentYearCombo.setPreferredSize(new Dimension(95, 32));

        JLabel semLbl = new JLabel("Semester:");
        semLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        semLbl.setForeground(new Color(100, 110, 125));

        enrollmentSemCombo = new JComboBox<>(new String[]{"All Semesters", "Semester 1", "Semester 2"});
        enrollmentSemCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        enrollmentSemCombo.setPreferredSize(new Dimension(115, 32));

        JButton resetBtn = createStyledButton("Reset", new Color(240, 243, 248), new Color(70, 80, 95), true);
        resetBtn.setPreferredSize(new Dimension(68, 32));
        resetBtn.addActionListener(e -> {
            enrollmentSearch.setText("");
            if (enrollmentDeptCombo.getItemCount() > 0) enrollmentDeptCombo.setSelectedIndex(0);
            enrollmentYearCombo.setSelectedIndex(0);
            enrollmentSemCombo.setSelectedIndex(0);
            loadEnrollmentReportData();
        });

        filterCard.add(searchIcon);
        filterCard.add(enrollmentSearch);
        filterCard.add(deptLbl);
        filterCard.add(enrollmentDeptCombo);
        filterCard.add(yearLbl);
        filterCard.add(enrollmentYearCombo);
        filterCard.add(semLbl);
        filterCard.add(enrollmentSemCombo);
        filterCard.add(resetBtn);

        enrollmentDeptCombo.addActionListener(e -> loadEnrollmentReportData());
        enrollmentYearCombo.addActionListener(e -> loadEnrollmentReportData());
        enrollmentSemCombo.addActionListener(e -> loadEnrollmentReportData());

        enrollmentSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterEnrollmentTable(); }
            public void removeUpdate(DocumentEvent e) { filterEnrollmentTable(); }
            public void changedUpdate(DocumentEvent e) { filterEnrollmentTable(); }
        });

        // 3. Table Setup
        String[] cols = {
            "Course Code", "Course Title", "Department", "Year / Sem", "Credits", "Course Type", "Enrolled", "Sessions", "Components", "CA Weight", "Final Weight"
        };
        enrollmentModel = new DefaultTableModel(null, cols) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        enrollmentTable = new JTable(enrollmentModel);
        setupTableStyle(enrollmentTable);

        enrollmentTable.getColumnModel().getColumn(5).setCellRenderer(new CourseTypeBadgeRenderer());
        enrollmentTable.getColumnModel().getColumn(6).setCellRenderer(new BoldNumberRenderer());

        enrollmentTable.getColumnModel().getColumn(0).setPreferredWidth(95);
        enrollmentTable.getColumnModel().getColumn(1).setPreferredWidth(210);
        enrollmentTable.getColumnModel().getColumn(2).setPreferredWidth(170);
        enrollmentTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        enrollmentTable.getColumnModel().getColumn(4).setPreferredWidth(60);
        enrollmentTable.getColumnModel().getColumn(5).setPreferredWidth(130);
        enrollmentTable.getColumnModel().getColumn(6).setPreferredWidth(75);
        enrollmentTable.getColumnModel().getColumn(7).setPreferredWidth(70);
        enrollmentTable.getColumnModel().getColumn(8).setPreferredWidth(85);
        enrollmentTable.getColumnModel().getColumn(9).setPreferredWidth(80);
        enrollmentTable.getColumnModel().getColumn(10).setPreferredWidth(85);

        enrollmentSorter = new TableRowSorter<>(enrollmentModel);
        enrollmentTable.setRowSorter(enrollmentSorter);

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.setBorder(BorderFactory.createLineBorder(new Color(230, 232, 238), 1, true));

        JScrollPane scrollPane = new JScrollPane(enrollmentTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableWrapper.add(scrollPane, BorderLayout.CENTER);

        // 4. Footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 5, 0, 5));

        enrollmentFooterLabel = new JLabel("Showing 0 courses");
        enrollmentFooterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        enrollmentFooterLabel.setForeground(new Color(110, 120, 135));

        JLabel infoLabel = new JLabel("ⓘ Curriculum reflects courses active in tecfams_db across faculty departments.");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        infoLabel.setForeground(new Color(110, 120, 135));

        footer.add(enrollmentFooterLabel, BorderLayout.WEST);
        footer.add(infoLabel, BorderLayout.EAST);

        // Assemble View 2
        JPanel topWrapper = new JPanel();
        topWrapper.setLayout(new BoxLayout(topWrapper, BoxLayout.Y_AXIS));
        topWrapper.setOpaque(false);
        topWrapper.add(kpiPanel);
        topWrapper.add(filterCard);
        topWrapper.add(Box.createVerticalStrut(18)); // Bottom margin to separate filter bar from table cleanly

        panel.add(topWrapper, BorderLayout.NORTH);
        panel.add(tableWrapper, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private void loadEnrollmentReportData() {
        Integer selectedDeptId = getSelectedIdFromCombo(enrollmentDeptCombo, departmentMap);
        Integer selectedYear = getYearFromCombo(enrollmentYearCombo);
        Integer selectedSem = getSemFromCombo(enrollmentSemCombo);

        List<CourseEnrollmentReportItem> list = ReportsDAO.getCourseEnrollmentReport(selectedDeptId, selectedYear, selectedSem);

        enrollmentModel.setRowCount(0);
        int totalEnrollments = 0;
        int totalCredits = 0;

        for (CourseEnrollmentReportItem item : list) {
            totalEnrollments += item.getEnrolledStudents();
            totalCredits += item.getCredit();

            enrollmentModel.addRow(new Object[]{
                item.getCourseCode(),
                item.getCourseName(),
                item.getDepartmentName(),
                "Y" + item.getAcademicYear() + " S" + item.getSemester(),
                item.getCredit(),
                item.getCourseType(),
                item.getEnrolledStudents(),
                item.getTotalSessions(),
                item.getTotalEvalComponents(),
                String.format("%.0f%%", item.getCaWeight()),
                String.format("%.0f%%", item.getFinalWeight())
            });
        }

        int totalCourses = list.size();
        enrollmentCoursesKpi.setText(String.valueOf(totalCourses));
        enrollmentStudentsKpi.setText(String.valueOf(totalEnrollments));
        enrollmentCreditsKpi.setText(String.valueOf(totalCredits));

        double avg = totalCourses > 0 ? ((double) totalEnrollments / totalCourses) : 0.0;
        enrollmentAvgKpi.setText(String.format("%.1f", avg));

        enrollmentFooterLabel.setText(String.format("Showing %d courses (Total Enrollments: %d | Cumulative Credits: %d)", totalCourses, totalEnrollments, totalCredits));
        filterEnrollmentTable();
    }

    private void filterEnrollmentTable() {
        if (enrollmentSorter == null) return;
        String query = enrollmentSearch.getText().trim();
        if (query.isEmpty()) {
            enrollmentSorter.setRowFilter(null);
        } else {
            enrollmentSorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 0, 1, 2));
        }
        int filteredCount = enrollmentTable.getRowCount();
        enrollmentFooterLabel.setText(String.format("Showing %d courses (filtered)", filteredCount));
    }

    // =========================================================================
    // VIEW 3: FACULTY STAFF & RESOURCE ALLOCATION REPORT
    // =========================================================================
    private JPanel createStaffReportView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(5, 25, 20, 25));

        // 1. KPI Metric Cards
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        staffLecturersKpi = new JLabel("0");
        staffOfficersKpi = new JLabel("0");
        staffStudentsKpi = new JLabel("0");
        staffRatioKpi = new JLabel("0.0:1");

        kpiPanel.add(createKpiCard("Academic Staff", staffLecturersKpi, "Lecturers & Professors", new Color(225, 238, 255), new Color(30, 100, 220)));
        kpiPanel.add(createKpiCard("Technical Officers", staffOfficersKpi, "Laboratory staff", new Color(246, 234, 255), new Color(130, 60, 220)));
        kpiPanel.add(createKpiCard("Undergraduates", staffStudentsKpi, "Active enrolled students", new Color(230, 248, 234), new Color(22, 163, 74)));
        kpiPanel.add(createKpiCard("Student-Lecturer Ratio", staffRatioKpi, "Faculty-wide ratio", new Color(254, 239, 227), new Color(234, 88, 12)));

        // 2. Department Summary Table (Capacity Overview)
        JPanel deptSummaryCard = new JPanel(new BorderLayout());
        deptSummaryCard.setBackground(Color.WHITE);
        deptSummaryCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 232, 238), 1, true),
            new EmptyBorder(10, 15, 12, 15)
        ));

        JLabel deptSummaryTitle = new JLabel("Departmental Capacity & Student-Staff Ratio");
        deptSummaryTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deptSummaryTitle.setForeground(new Color(23, 32, 48));
        deptSummaryTitle.setBorder(new EmptyBorder(0, 0, 8, 0));
        deptSummaryCard.add(deptSummaryTitle, BorderLayout.NORTH);

        String[] deptCols = {
            "Dept Code", "Department Name", "Lecturers", "Technical Officers", "Students", "Courses Offered", "Student-Lecturer Ratio"
        };
        deptStaffModel = new DefaultTableModel(null, deptCols) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        deptStaffTable = new JTable(deptStaffModel);
        setupTableStyle(deptStaffTable);
        deptStaffTable.setRowHeight(36);

        deptStaffTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        deptStaffTable.getColumnModel().getColumn(1).setPreferredWidth(320);
        deptStaffTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        deptStaffTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        deptStaffTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        deptStaffTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        deptStaffTable.getColumnModel().getColumn(6).setPreferredWidth(140);

        JScrollPane deptScroll = new JScrollPane(deptStaffTable);
        deptScroll.setBorder(BorderFactory.createEmptyBorder());
        deptScroll.getViewport().setBackground(Color.WHITE);
        deptScroll.setPreferredSize(new Dimension(800, 120));
        deptSummaryCard.add(deptScroll, BorderLayout.CENTER);

        // 3. Detailed Staff Member Table with Search and Filters
        JPanel staffDetailSection = new JPanel(new BorderLayout());
        staffDetailSection.setOpaque(false);
        staffDetailSection.setBorder(new EmptyBorder(15, 0, 0, 0));

        JPanel detailHeader = new JPanel(new BorderLayout());
        detailHeader.setOpaque(false);
        detailHeader.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel staffDirectoryTitle = new JLabel("Faculty Staff Directory & Allocation");
        staffDirectoryTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        staffDirectoryTitle.setForeground(new Color(23, 32, 48));

        // Staff Filters
        JPanel staffFilterBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        staffFilterBar.setOpaque(false);

        staffSearch = new JTextField(12);
        staffSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        staffSearch.putClientProperty("JTextField.placeholderText", "Search name or email...");
        staffSearch.setPreferredSize(new Dimension(170, 30));

        staffDeptCombo = new JComboBox<>();
        populateDepartmentCombo(staffDeptCombo);
        staffDeptCombo.setPreferredSize(new Dimension(180, 30));

        staffRoleCombo = new JComboBox<>(new String[]{"All Staff Roles", "Lecturer", "Technical Officer"});
        staffRoleCombo.setPreferredSize(new Dimension(140, 30));

        JButton staffResetBtn = createStyledButton("Reset", new Color(240, 243, 248), new Color(70, 80, 95), true);
        staffResetBtn.setPreferredSize(new Dimension(65, 30));
        staffResetBtn.addActionListener(e -> {
            staffSearch.setText("");
            if (staffDeptCombo.getItemCount() > 0) staffDeptCombo.setSelectedIndex(0);
            staffRoleCombo.setSelectedIndex(0);
            loadStaffReportData();
        });

        staffFilterBar.add(new JLabel("Search:"));
        staffFilterBar.add(staffSearch);
        staffFilterBar.add(staffDeptCombo);
        staffFilterBar.add(staffRoleCombo);
        staffFilterBar.add(staffResetBtn);

        detailHeader.add(staffDirectoryTitle, BorderLayout.WEST);
        detailHeader.add(staffFilterBar, BorderLayout.EAST);

        staffDeptCombo.addActionListener(e -> loadStaffReportData());
        staffRoleCombo.addActionListener(e -> loadStaffReportData());
        staffSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterStaffTable(); }
            public void removeUpdate(DocumentEvent e) { filterStaffTable(); }
            public void changedUpdate(DocumentEvent e) { filterStaffTable(); }
        });

        String[] staffCols = {
            "ID", "Staff Name", "Role", "Designation", "Department", "Email", "Phone", "Status"
        };
        staffDetailModel = new DefaultTableModel(null, staffCols) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        staffDetailTable = new JTable(staffDetailModel);
        setupTableStyle(staffDetailTable);

        staffDetailTable.getColumnModel().getColumn(2).setCellRenderer(new RoleBadgeRenderer());
        staffDetailTable.getColumnModel().getColumn(7).setCellRenderer(new StatusBadgeRenderer());

        staffDetailTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        staffDetailTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        staffDetailTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        staffDetailTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        staffDetailTable.getColumnModel().getColumn(4).setPreferredWidth(200);
        staffDetailTable.getColumnModel().getColumn(5).setPreferredWidth(170);
        staffDetailTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        staffDetailTable.getColumnModel().getColumn(7).setPreferredWidth(80);

        staffDetailSorter = new TableRowSorter<>(staffDetailModel);
        staffDetailTable.setRowSorter(staffDetailSorter);

        JPanel detailTableCard = new JPanel(new BorderLayout());
        detailTableCard.setBackground(Color.WHITE);
        detailTableCard.setBorder(BorderFactory.createLineBorder(new Color(230, 232, 238), 1, true));

        JScrollPane staffScroll = new JScrollPane(staffDetailTable);
        staffScroll.setBorder(BorderFactory.createEmptyBorder());
        staffScroll.getViewport().setBackground(Color.WHITE);
        detailTableCard.add(staffScroll, BorderLayout.CENTER);

        staffDetailSection.add(detailHeader, BorderLayout.NORTH);
        staffDetailSection.add(detailTableCard, BorderLayout.CENTER);

        // 4. Footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 5, 0, 5));

        staffFooterLabel = new JLabel("Showing staff members");
        staffFooterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        staffFooterLabel.setForeground(new Color(110, 120, 135));

        JLabel staffNotice = new JLabel("ⓘ Faculty staff distribution helps ensure UGC compliance and departmental course coverage.");
        staffNotice.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        staffNotice.setForeground(new Color(110, 120, 135));

        footer.add(staffFooterLabel, BorderLayout.WEST);
        footer.add(staffNotice, BorderLayout.EAST);

        // Assemble View 3
        JPanel topWrapper = new JPanel();
        topWrapper.setLayout(new BoxLayout(topWrapper, BoxLayout.Y_AXIS));
        topWrapper.setOpaque(false);
        topWrapper.add(kpiPanel);
        topWrapper.add(deptSummaryCard);

        panel.add(topWrapper, BorderLayout.NORTH);
        panel.add(staffDetailSection, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private void loadStaffReportData() {
        // 1. Load Department Summaries
        List<DepartmentStaffReportItem> deptList = ReportsDAO.getDepartmentStaffSummaryReport();
        deptStaffModel.setRowCount(0);

        int totalLecturers = 0;
        int totalOfficers = 0;
        int totalStudents = 0;

        for (DepartmentStaffReportItem d : deptList) {
            totalLecturers += d.getLecturerCount();
            totalOfficers += d.getTechnicalOfficerCount();
            totalStudents += d.getStudentCount();

            String ratioStr = d.getLecturerCount() > 0 ? String.format("%.1f : 1", d.getStudentLecturerRatio()) : "N/A";

            deptStaffModel.addRow(new Object[]{
                d.getDepartmentCode(),
                d.getDepartmentName(),
                d.getLecturerCount(),
                d.getTechnicalOfficerCount(),
                d.getStudentCount(),
                d.getCourseCount(),
                ratioStr
            });
        }

        staffLecturersKpi.setText(String.valueOf(totalLecturers));
        staffOfficersKpi.setText(String.valueOf(totalOfficers));
        staffStudentsKpi.setText(String.valueOf(totalStudents));

        double overallRatio = totalLecturers > 0 ? ((double) totalStudents / totalLecturers) : 0.0;
        staffRatioKpi.setText(String.format("%.1f : 1", overallRatio));

        // 2. Load Detailed Staff Members
        Integer selectedDeptId = getSelectedIdFromCombo(staffDeptCombo, departmentMap);
        String selectedRole = (String) staffRoleCombo.getSelectedItem();
        if ("All Staff Roles".equals(selectedRole)) selectedRole = null;

        List<StaffDetailReportItem> staffList = ReportsDAO.getStaffDetailsReport(selectedDeptId, selectedRole);
        staffDetailModel.setRowCount(0);

        for (StaffDetailReportItem s : staffList) {
            staffDetailModel.addRow(new Object[]{
                s.getUserId(),
                s.getName(),
                s.getRole(),
                s.getDesignation(),
                s.getDepartmentName(),
                s.getEmail(),
                s.getPhone(),
                s.getStatus()
            });
        }

        staffFooterLabel.setText(String.format("Showing %d staff members across %d departments", staffList.size(), deptList.size()));
        filterStaffTable();
    }

    private void filterStaffTable() {
        if (staffDetailSorter == null) return;
        String query = staffSearch.getText().trim();
        if (query.isEmpty()) {
            staffDetailSorter.setRowFilter(null);
        } else {
            staffDetailSorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 1, 3, 4, 5));
        }
        int filteredCount = staffDetailTable.getRowCount();
        staffFooterLabel.setText(String.format("Showing %d staff members (filtered)", filteredCount));
    }

    // =========================================================================
    // EXPORT TO CSV FUNCTIONALITY
    // =========================================================================
    private void exportActiveReportToCSV() {
        JTable targetTable;
        String reportPrefix;

        if ("ELIGIBILITY".equals(currentReportTab)) {
            targetTable = eligibilityTable;
            reportPrefix = "Exam_Eligibility_Report";
        } else if ("ENROLLMENT".equals(currentReportTab)) {
            targetTable = enrollmentTable;
            reportPrefix = "Course_Enrollment_Report";
        } else {
            targetTable = staffDetailTable;
            reportPrefix = "Faculty_Staff_Report";
        }

        if (targetTable.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data available in the current report to export.", "Export Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String defaultFileName = reportPrefix + "_" + timeStamp + ".csv";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report as CSV");
        fileChooser.setSelectedFile(new File(defaultFileName));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }

            try (PrintWriter pw = new PrintWriter(new FileWriter(fileToSave))) {
                // Write Header
                TableModel model = targetTable.getModel();
                int colCount = targetTable.getColumnCount();

                for (int c = 0; c < colCount; c++) {
                    pw.print(escapeCSV(targetTable.getColumnName(c)));
                    if (c < colCount - 1) pw.print(",");
                }
                pw.println();

                // Write Rows (Using view order to respect current user sorting/filtering)
                int rowCount = targetTable.getRowCount();
                for (int r = 0; r < rowCount; r++) {
                    for (int c = 0; c < colCount; c++) {
                        Object val = targetTable.getValueAt(r, c);
                        pw.print(escapeCSV(val != null ? val.toString() : ""));
                        if (c < colCount - 1) pw.print(",");
                    }
                    pw.println();
                }

                JOptionPane.showMessageDialog(
                    this,
                    "Report exported successfully to:\n" + fileToSave.getAbsolutePath() + "\n(" + rowCount + " records written)",
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to export report: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "\"\"";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return "\"" + value + "\"";
    }

    // =========================================================================
    // HELPER UI COMPONENT CREATORS & STYLING
    // =========================================================================
    private JPanel createKpiCard(String title, JLabel valueLabel, String subtitle, Color bgColor, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                // Left accent border bar
                g2.setColor(accentColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, 5, getHeight(), 12, 12));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel textBlock = new JPanel();
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
        textBlock.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(new Color(110, 120, 135));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(new Color(23, 32, 48));

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLbl.setForeground(new Color(140, 150, 165));

        textBlock.add(titleLbl);
        textBlock.add(Box.createVerticalStrut(4));
        textBlock.add(valueLabel);
        textBlock.add(Box.createVerticalStrut(2));
        textBlock.add(subLbl);

        card.add(textBlock, BorderLayout.CENTER);
        return card;
    }

    private JButton createStyledButton(String text, Color bg, Color fg, boolean border) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                if (border) {
                    g2.setColor(new Color(215, 220, 230));
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                }
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void setupTableStyle(JTable table) {
        table.setRowHeight(42);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(new Color(35, 45, 55));
        table.setSelectionBackground(new Color(236, 242, 252));
        table.setSelectionForeground(new Color(23, 32, 48));

        JTableHeader th = table.getTableHeader();
        th.setBackground(Color.WHITE);
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setForeground(new Color(100, 110, 125));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 232, 238)));
        th.setPreferredSize(new Dimension(100, 38));
        th.setReorderingAllowed(false);
    }

    private void populateDepartmentCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        combo.addItem("All Departments");
        for (String name : departmentMap.values()) {
            combo.addItem(name);
        }
    }

    private void populateCourseCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        combo.addItem("All Courses");
        for (String cStr : courseMap.values()) {
            combo.addItem(cStr);
        }
    }

    private Integer getSelectedIdFromCombo(JComboBox<String> combo, Map<Integer, String> map) {
        int idx = combo.getSelectedIndex();
        if (idx <= 0) return null; // "All..."
        String selectedVal = (String) combo.getSelectedItem();
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            if (entry.getValue().equals(selectedVal)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private Integer getYearFromCombo(JComboBox<String> combo) {
        int idx = combo.getSelectedIndex();
        if (idx <= 0) return null;
        return idx; // Year 1 -> 1, Year 2 -> 2, etc.
    }

    private Integer getSemFromCombo(JComboBox<String> combo) {
        int idx = combo.getSelectedIndex();
        if (idx <= 0) return null;
        return idx; // Semester 1 -> 1, Semester 2 -> 2
    }

    public void refreshAllReports() {
        loadEligibilityReportData();
        loadEnrollmentReportData();
        loadStaffReportData();
    }

    // =========================================================================
    // CUSTOM CELL RENDERERS
    // =========================================================================
    private static class EligibilityBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            String status = value != null ? value.toString() : "";
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 8));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            JLabel badge = new JLabel(" " + status + " ") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    super.paintComponent(g);
                    g2.dispose();
                }
            };
            badge.setOpaque(false);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setBorder(new EmptyBorder(3, 8, 3, 8));

            if (status.equals("Eligible")) {
                badge.setBackground(new Color(230, 248, 234));
                badge.setForeground(new Color(22, 163, 74));
            } else if (status.startsWith("Eligible (Medicals)")) {
                badge.setBackground(new Color(225, 238, 255));
                badge.setForeground(new Color(30, 100, 220));
            } else if (status.equals("Not Eligible")) {
                badge.setBackground(new Color(254, 235, 235));
                badge.setForeground(new Color(220, 38, 38));
            } else {
                badge.setBackground(new Color(241, 243, 244));
                badge.setForeground(new Color(95, 99, 104));
            }

            panel.add(badge);
            return panel;
        }
    }

    private static class AttendancePercentageRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setHorizontalAlignment(SwingConstants.CENTER);

            String str = value != null ? value.toString() : "0.0%";
            double pct = 0.0;
            try {
                pct = Double.parseDouble(str.replace("%", "").trim());
            } catch (Exception ignored) {}

            if (pct >= 80.0) {
                setForeground(new Color(22, 163, 74)); // Green
            } else {
                setForeground(new Color(220, 38, 38)); // Red
            }
            return this;
        }
    }

    private static class CAMarksRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setHorizontalAlignment(SwingConstants.CENTER);
            setForeground(new Color(60, 70, 85));
            return this;
        }
    }

    private static class CourseTypeBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            String type = value != null ? value.toString() : "";
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 8));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            JLabel badge = new JLabel(type) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                    super.paintComponent(g);
                    g2.dispose();
                }
            };
            badge.setOpaque(false);
            badge.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            badge.setBorder(new EmptyBorder(2, 6, 2, 6));

            if (type.toLowerCase().contains("practical")) {
                badge.setBackground(new Color(238, 242, 255));
                badge.setForeground(new Color(79, 70, 229));
            } else {
                badge.setBackground(new Color(243, 244, 246));
                badge.setForeground(new Color(75, 85, 99));
            }

            panel.add(badge);
            return panel;
        }
    }

    private static class BoldNumberRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setHorizontalAlignment(SwingConstants.CENTER);
            setForeground(new Color(30, 40, 50));
            return this;
        }
    }

    private static class RoleBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            String role = value != null ? value.toString() : "";
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 8));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            JLabel badge = new JLabel(role) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                    super.paintComponent(g);
                    g2.dispose();
                }
            };
            badge.setOpaque(false);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setBorder(new EmptyBorder(2, 8, 2, 8));

            if ("Lecturer".equals(role)) {
                badge.setBackground(new Color(254, 239, 227));
                badge.setForeground(new Color(234, 88, 12));
            } else {
                badge.setBackground(new Color(246, 234, 255));
                badge.setForeground(new Color(130, 60, 220));
            }

            panel.add(badge);
            return panel;
        }
    }

    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            String status = value != null ? value.toString() : "";
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 8));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            JLabel badge = new JLabel(status) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                    super.paintComponent(g);
                    g2.dispose();
                }
            };
            badge.setOpaque(false);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setBorder(new EmptyBorder(2, 8, 2, 8));

            if ("Active".equalsIgnoreCase(status)) {
                badge.setBackground(new Color(230, 248, 234));
                badge.setForeground(new Color(22, 163, 74));
            } else {
                badge.setBackground(new Color(241, 243, 244));
                badge.setForeground(new Color(95, 99, 104));
            }

            panel.add(badge);
            return panel;
        }
    }

    // =========================================================================
    // WRAP LAYOUT (Dynamically recalculates height when components wrap)
    // =========================================================================
    public static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension minimum = layoutSize(target, false);
            minimum.width -= (getHgap() + 1);
            return minimum;
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                Container c = target;
                while (targetWidth == 0 && c.getParent() != null) {
                    c = c.getParent();
                    targetWidth = c.getSize().width;
                }
                if (targetWidth == 0) {
                    targetWidth = 1000;
                }

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
                        if ((rowWidth + d.width) > maxWidth && rowWidth > 0) {
                            addRow(dim, rowWidth, rowHeight);
                            rowWidth = 0;
                            rowHeight = 0;
                        }
                        if (rowWidth != 0) {
                            rowWidth += hgap;
                        }
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
            if (dim.height > 0) {
                dim.height += getVgap();
            }
            dim.height += rowHeight;
        }
    }
}
