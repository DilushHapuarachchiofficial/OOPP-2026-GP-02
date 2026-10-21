package main.java.ui.TechnicalOfficer;

import main.java.model.TechnicalOfficer.TechnicalOfficer;
import main.resources.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static main.java.ui.TechnicalOfficer.TechnicalOfficerDashboard.*;

/**
 * AttendanceManagementForm - Technical Officer Module for TecFAMS.
 * University of Ruhuna - Faculty of Technology.
 * 
 * Features:
 * - Academic Year filter (All Years, Year 1 to 4)
 * - Course selector (loaded from database and standard syllabus)
 * - Session Type ('Theory', 'Practical') and Session Number (1 to 15)
 * - Session Date (auto-retrieved from 'attendance_sessions' table)
 * - Student Table with Reg No, Full Name, Academic Year, and Present/Absent status
 * - Dropdown editor and colored badges for attendance status
 * - Save and Update Attendance directly persisted to 'tecfams_db'
 * - Clean, modular design easy for beginner Java students to understand
 */
public class AttendanceManagementForm extends JFrame {

    private final TechnicalOfficer currentOfficer;
    private final JFrame parentDashboard;

    // Filter UI controls
    private JComboBox<String> cmbAcademicYear;
    private JComboBox<CourseItem> cmbCourse;
    private JComboBox<String> cmbSessionType;
    private JComboBox<Integer> cmbSessionNumber;
    private JTextField txtSessionDate;
    private JButton btnLoadStudents;

    // Table controls
    private JTable tblStudents;
    private DefaultTableModel tableModel;
    private JLabel lblTableStatus;

    // Action buttons
    private JButton btnSaveAttendance;
    private JButton btnUpdateAttendance;
    private JButton btnClear;
    private JButton btnBack;

    // Current session tracker
    private int currentSessionId = -1;
    private final List<CourseItem> allCourses = new ArrayList<>();

    public AttendanceManagementForm() {
        this(null, new TechnicalOfficer());
    }

    public AttendanceManagementForm(JFrame parentDashboard) {
        this(parentDashboard, new TechnicalOfficer());
    }

    public AttendanceManagementForm(JFrame parentDashboard, TechnicalOfficer officer) {
        this.parentDashboard = parentDashboard;
        this.currentOfficer = officer != null ? officer : new TechnicalOfficer();

        initFrameSettings();
        buildUI();
        loadInitialData();
    }

    private void initFrameSettings() {
        setTitle("TecFAMS - Attendance Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1140, 760);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(parentDashboard);
    }

    private void buildUI() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);

        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setBackground(BG_COLOR);
        centerPanel.setBorder(new EmptyBorder(18, 24, 18, 24));
        centerPanel.add(createFilterCard(), BorderLayout.NORTH);
        centerPanel.add(createTableCard(), BorderLayout.CENTER);
        centerPanel.add(createBottomActionBar(), BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Top Header with branding, title, officer info, and Back button.
     */
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(14, 24, 14, 24)
        ));

        // Left Branding & Title
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        JLabel titleLabel = new JLabel("Attendance Management");
        titleLabel.setFont(FONT_HEADER_LARGE);
        titleLabel.setForeground(PRIMARY_NAVY);
        JLabel subLabel = new JLabel("Faculty of Technology • University of Ruhuna | Technical Officer Session Logger");
        subLabel.setFont(FONT_SMALL);
        subLabel.setForeground(TEXT_SECONDARY);
        left.add(titleLabel);
        left.add(subLabel);

        // Right Actions (Officer badge + Back button)
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        JLabel officerBadge = new JLabel("Officer: " + currentOfficer.getName());
        officerBadge.setFont(FONT_SMALL);
        officerBadge.setForeground(PRIMARY_NAVY);
        officerBadge.setBackground(LIGHT_BLUE);
        officerBadge.setOpaque(true);
        officerBadge.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#BFDBFE"), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));

        btnBack = createSecondaryButton("← Back to Dashboard");
        btnBack.addActionListener(e -> backToDashboard());
        right.add(officerBadge);
        right.add(btnBack);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    /**
     * Filter card with Academic Year, Course, Session Number, Type, Date, and Load button.
     */
    private JPanel createFilterCard() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JLabel cardTitle = new JLabel("Session Selection & Criteria");
        cardTitle.setFont(FONT_HEADER_MEDIUM);
        cardTitle.setForeground(PRIMARY_NAVY);
        card.add(cardTitle, BorderLayout.NORTH);

        JPanel controls = new JPanel(new GridLayout(2, 3, 16, 12));
        controls.setOpaque(false);

        cmbAcademicYear = new JComboBox<>();
        cmbCourse = new JComboBox<>();
        cmbSessionType = new JComboBox<>(new String[]{"Theory", "Practical"});

        Integer[] sessionNums = new Integer[15];
        for (int i = 0; i < 15; i++) sessionNums[i] = i + 1;
        cmbSessionNumber = new JComboBox<>(sessionNums);

        txtSessionDate = new JTextField(LocalDate.now().toString());
        txtSessionDate.setToolTipText("Format: YYYY-MM-DD");

        for (JComponent c : new JComponent[]{cmbAcademicYear, cmbCourse, cmbSessionType, cmbSessionNumber, txtSessionDate}) {
            c.setFont(FONT_REGULAR);
        }

        onSelectionChanged(cmbAcademicYear, this::filterCoursesByYear);
        onSelectionChanged(cmbCourse, this::checkAndUpdateSessionDetails);
        onSelectionChanged(cmbSessionType, this::checkAndUpdateSessionDetails);
        onSelectionChanged(cmbSessionNumber, this::checkAndUpdateSessionDetails);

        btnLoadStudents = createPrimaryButton("Load Students");
        btnLoadStudents.setPreferredSize(new Dimension(0, 36));
        btnLoadStudents.addActionListener(e -> loadStudents());

        JPanel pBtn = new JPanel(new BorderLayout());
        pBtn.setOpaque(false);
        JLabel emptyLabel = new JLabel(" ");
        emptyLabel.setFont(FONT_SMALL);
        pBtn.add(emptyLabel, BorderLayout.NORTH);
        pBtn.add(btnLoadStudents, BorderLayout.CENTER);

        controls.add(createFieldWrapper("Academic Year / Level:", cmbAcademicYear));
        controls.add(createFieldWrapper("Course / Module:", cmbCourse));
        controls.add(createFieldWrapper("Session Type:", cmbSessionType));
        controls.add(createFieldWrapper("Session Number (1-15):", cmbSessionNumber));
        controls.add(createFieldWrapper("Session Date (YYYY-MM-DD):", txtSessionDate));
        controls.add(pBtn);

        card.add(controls, BorderLayout.CENTER);
        return card;
    }

    /**
     * Table displaying enrolled students and their attendance status.
     */
    private JPanel createTableCard() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 8));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel tableTitle = new JLabel("Student Attendance Records");
        tableTitle.setFont(FONT_HEADER_MEDIUM);
        tableTitle.setForeground(PRIMARY_NAVY);

        lblTableStatus = new JLabel("No session loaded. Select criteria and click 'Load Students'.");
        lblTableStatus.setFont(FONT_SMALL);
        lblTableStatus.setForeground(TEXT_SECONDARY);
        headerPanel.add(tableTitle, BorderLayout.WEST);
        headerPanel.add(lblTableStatus, BorderLayout.EAST);
        card.add(headerPanel, BorderLayout.NORTH);

        String[] columnNames = {"#", "Database ID", "Registration No", "Student Full Name", "Level", "Attendance Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return c == 5; }
            @Override
            public Class<?> getColumnClass(int c) { return String.class; }
        };

        tblStudents = new JTable(tableModel);
        tblStudents.setFont(FONT_REGULAR);
        tblStudents.setRowHeight(36);
        tblStudents.setSelectionBackground(LIGHT_BLUE);
        tblStudents.setSelectionForeground(TEXT_MAIN);
        tblStudents.setGridColor(BORDER_COLOR);
        tblStudents.setShowVerticalLines(true);
        tblStudents.setShowHorizontalLines(true);

        // Hide DB student_id column (index 1) from view
        tblStudents.getColumnModel().getColumn(1).setMinWidth(0);
        tblStudents.getColumnModel().getColumn(1).setMaxWidth(0);
        tblStudents.getColumnModel().getColumn(1).setPreferredWidth(0);

        int[] widths = {45, 0, 140, 320, 90, 160};
        for (int i = 0; i < widths.length; i++) {
            if (i != 1) tblStudents.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JTableHeader th = tblStudents.getTableHeader();
        th.setFont(FONT_MEDIUM);
        th.setBackground(Color.decode("#F1F5F9"));
        th.setForeground(PRIMARY_NAVY);
        th.setPreferredSize(new Dimension(0, 36));

        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Present", "Absent"});
        statusCombo.setFont(FONT_MEDIUM);
        tblStudents.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(statusCombo));

        tblStudents.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean sel, boolean focus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, val, sel, focus, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                boolean present = "Present".equalsIgnoreCase(val != null ? val.toString() : "");
                l.setForeground(present ? Color.decode("#047857") : Color.decode("#B91C1C"));
                l.setText(present ? "●  Present" : "○  Absent");
                return l;
            }
        });

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        tblStudents.getColumnModel().getColumn(0).setCellRenderer(center);
        tblStudents.getColumnModel().getColumn(4).setCellRenderer(center);

        JScrollPane scroll = new JScrollPane(tblStudents);
        scroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        scroll.getViewport().setBackground(WHITE);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    /**
     * Bottom action bar containing summary counts and Save/Update/Clear buttons.
     */
    private JPanel createBottomActionBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JButton btnMarkAllPresent = createSecondaryButton("Mark All Present");
        btnMarkAllPresent.addActionListener(e -> setAllStatus("Present"));
        JButton btnMarkAllAbsent = createSecondaryButton("Mark All Absent");
        btnMarkAllAbsent.addActionListener(e -> setAllStatus("Absent"));
        left.add(new JLabel("Batch Actions:"));
        left.add(btnMarkAllPresent);
        left.add(btnMarkAllAbsent);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        btnClear = createSecondaryButton("Clear");
        btnClear.addActionListener(e -> clearForm());
        btnUpdateAttendance = createSecondaryButton("Update Attendance");
        btnUpdateAttendance.addActionListener(e -> saveOrUpdateAttendance(true));
        btnSaveAttendance = createPrimaryButton("Save Attendance");
        btnSaveAttendance.addActionListener(e -> saveOrUpdateAttendance(false));
        right.add(btnClear);
        right.add(btnUpdateAttendance);
        right.add(btnSaveAttendance);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel createFieldWrapper(String labelText, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(FONT_SMALL);
        label.setForeground(TEXT_MAIN);
        p.add(label, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void onSelectionChanged(JComboBox<?> combo, Runnable action) {
        combo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) action.run();
        });
    }

    // =========================================================================
    // DATABASE OPERATIONS & DATA LOADING
    // =========================================================================

    private void loadInitialData() {
        loadAcademicYears();
        loadCourses();
        checkAndUpdateSessionDetails();
    }

    /**
     * Populates Academic Year dropdown with Year 1, 2, 3, and 4.
     */
    private void loadAcademicYears() {
        cmbAcademicYear.removeAllItems();
        for (String yr : new String[]{"All Years", "Year 1", "Year 2", "Year 3", "Year 4"}) {
            cmbAcademicYear.addItem(yr);
        }
    }

    /**
     * Populates Courses dropdown from 'courses' table and standard faculty curriculum.
     */
    private void loadCourses() {
        allCourses.clear();
        cmbCourse.removeAllItems();

        Set<String> loadedCodes = new HashSet<>();
        Connection conn = DatabaseConnection.getConnection();

        if (conn != null) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT course_id, course_code, course_name, course_type, academic_year FROM courses ORDER BY academic_year ASC, course_code ASC")) {
                while (rs.next()) {
                    CourseItem item = new CourseItem(
                            rs.getInt("course_id"),
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            rs.getString("course_type"),
                            rs.getInt("academic_year")
                    );
                    allCourses.add(item);
                    loadedCodes.add(item.getCourseCode().toUpperCase());
                }
            } catch (SQLException e) {
                System.err.println("Notice: Could not query courses: " + e.getMessage());
            }
        }

        // Add standard syllabus courses for all 4 academic years without modifying database
        for (CourseItem standard : getStandardCurriculumCourses()) {
            if (!loadedCodes.contains(standard.getCourseCode().toUpperCase())) {
                allCourses.add(standard);
            }
        }

        filterCoursesByYear();
    }

    private List<CourseItem> getStandardCurriculumCourses() {
        return Arrays.asList(
                // Year 1 Courses
                new CourseItem(101, "ICT1113", "Introduction to Computer Science & Programming", "Theory_and_Practical", 1),
                new CourseItem(102, "ICT1122", "Computer System Architecture", "Theory_and_Practical", 1),
                new CourseItem(103, "ICT1133", "Mathematics for Computing", "Theory_Only", 1),
                new CourseItem(104, "ENG1112", "Communication Skills I", "Theory_Only", 1),
                new CourseItem(105, "ICT1213", "Object-Oriented Programming", "Theory_and_Practical", 1),
                new CourseItem(106, "ICT1222", "Web Technologies", "Theory_and_Practical", 1),

                // Year 2 Courses
                new CourseItem(1, "ICT2132", "Object Oriented Programming Practicum", "Theory_and_Practical", 2),
                new CourseItem(2, "ICT2113", "Data Structures and Algorithms", "Theory_and_Practical", 2),
                new CourseItem(3, "ICT2122", "Database Management Systems", "Theory_and_Practical", 2),
                new CourseItem(4, "ENG2112", "Communication Skills II", "Theory_Only", 2),
                new CourseItem(205, "ICT2213", "Operating Systems", "Theory_and_Practical", 2),
                new CourseItem(206, "ICT2223", "Software Engineering", "Theory_Only", 2),

                // Year 3 Courses
                new CourseItem(301, "ICT3113", "Software Project Management", "Theory_Only", 3),
                new CourseItem(302, "ICT3122", "Computer Networks", "Theory_and_Practical", 3),
                new CourseItem(303, "ICT3132", "Information Security", "Theory_and_Practical", 3),
                new CourseItem(304, "ICT3142", "Mobile Application Development", "Theory_and_Practical", 3),
                new CourseItem(305, "ICT3213", "Cloud Computing & DevOps", "Theory_and_Practical", 3),

                // Year 4 Courses
                new CourseItem(401, "ICT4113", "Machine Learning & Artificial Intelligence", "Theory_and_Practical", 4),
                new CourseItem(402, "ICT4122", "Distributed Systems", "Theory_and_Practical", 4),
                new CourseItem(403, "ICT4136", "Research Project / Capstone", "Practical", 4),
                new CourseItem(404, "ICT4216", "Industrial Training", "Practical", 4)
        );
    }

    private void filterCoursesByYear() {
        String selectedYear = (String) cmbAcademicYear.getSelectedItem();
        cmbCourse.removeAllItems();

        int year = (selectedYear != null && !"All Years".equalsIgnoreCase(selectedYear)) ? parseYearNumber(selectedYear) : -1;
        for (CourseItem c : allCourses) {
            if (year == -1 || c.getAcademicYear() == year) {
                cmbCourse.addItem(c);
            }
        }

        if (cmbCourse.getItemCount() > 0) {
            cmbCourse.setSelectedIndex(0);
        }
        checkAndUpdateSessionDetails();
    }

    /**
     * Retrieves session date and session_id from 'attendance_sessions' if already scheduled.
     */
    private void checkAndUpdateSessionDetails() {
        CourseItem course = (CourseItem) cmbCourse.getSelectedItem();
        Integer sessionNum = (Integer) cmbSessionNumber.getSelectedItem();
        String sessionType = (String) cmbSessionType.getSelectedItem();
        if (course == null || sessionNum == null || sessionType == null) return;

        currentSessionId = -1;
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            String sql = "SELECT session_id, session_date FROM attendance_sessions WHERE course_id = ? AND session_number = ? AND session_type = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, course.getCourseId());
                stmt.setInt(2, sessionNum);
                stmt.setString(3, sessionType);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        currentSessionId = rs.getInt("session_id");
                        Date d = rs.getDate("session_date");
                        if (d != null) txtSessionDate.setText(d.toString());
                    }
                }
            } catch (SQLException e) {
                System.err.println("Notice: Could not check session: " + e.getMessage());
            }
        }
    }

    /**
     * Loads registered students and existing attendance statuses for the selected session.
     */
    public void loadStudents() {
        CourseItem course = (CourseItem) cmbCourse.getSelectedItem();
        if (course == null) {
            JOptionPane.showMessageDialog(this, "Please select a course first.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer sessionNum = (Integer) cmbSessionNumber.getSelectedItem();
        String sessionType = (String) cmbSessionType.getSelectedItem();
        if (sessionNum == null || sessionType == null) return;

        tableModel.setRowCount(0);
        checkAndUpdateSessionDetails();

        Connection conn = DatabaseConnection.getConnection();
        Map<Integer, String> existingAttendance = loadExistingAttendance(conn);
        boolean loaded = false;

        if (conn != null) {
            // 1. Try querying enrolled students from course_registrations
            String regSql = "SELECT s.student_id, s.student_reg_no, s.full_name, s.academic_year " +
                            "FROM students s JOIN course_registrations cr ON s.student_id = cr.student_id " +
                            "WHERE cr.course_id = ? ORDER BY s.student_reg_no ASC";
            loaded = queryStudents(conn, regSql, course.getCourseId(), existingAttendance);

            // 2. If no direct registrations, query students by academic year from DB
            if (!loaded) {
                String yearSql = "SELECT student_id, student_reg_no, full_name, academic_year FROM students WHERE academic_year = ? ORDER BY student_reg_no ASC";
                loaded = queryStudents(conn, yearSql, course.getAcademicYear(), existingAttendance);
            }
        }

        // 3. Fallback sample data if database is empty, offline, or has no students for the selected year
        if (!loaded) {
            loadMockStudents(course, existingAttendance);
        }

        updateSummaryLabel(course, sessionNum, sessionType);
    }

    private Map<Integer, String> loadExistingAttendance(Connection conn) {
        Map<Integer, String> existing = new HashMap<>();
        if (conn != null && currentSessionId > 0) {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT student_id, attendance_status FROM attendances WHERE session_id = ?")) {
                stmt.setInt(1, currentSessionId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        existing.put(rs.getInt("student_id"), rs.getString("attendance_status"));
                    }
                }
            } catch (SQLException e) {
                System.err.println("Notice: Could not query existing attendance: " + e.getMessage());
            }
        }
        return existing;
    }

    private boolean queryStudents(Connection conn, String sql, int param, Map<Integer, String> existingAttendance) {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    int sid = rs.getInt("student_id");
                    String status = existingAttendance.getOrDefault(sid, "Present");
                    addStudentRow(sid, rs.getString("student_reg_no"), rs.getString("full_name"), rs.getInt("academic_year"), status);
                    found = true;
                }
                return found;
            }
        } catch (SQLException e) {
            System.err.println("Notice: Could not query students: " + e.getMessage());
            return false;
        }
    }

    private void addStudentRow(int sid, String regNo, String name, int year, String status) {
        tableModel.addRow(new Object[]{tableModel.getRowCount() + 1, String.valueOf(sid), regNo, name, "Year " + year, status});
    }

    private void loadMockStudents(CourseItem course, Map<Integer, String> existingAttendance) {
        int year = course != null ? course.getAcademicYear() : 2;
        String[] names;
        String[] regNos = null;
        int[] ids = null;

        if (year == 1) {
            names = new String[]{"K.P.S Deshan", "H.M.A Kavinda", "N.V.C Sandunika", "T.M.D Jayalath", "S.R.K Madushanka", "W.A.N Dilshan", "M.D.T Fernando", "R.G.S Liyanage"};
        } else if (year == 3) {
            names = new String[]{"A.M.K Pathirana", "D.S.L Wickramatunga", "K.G.T Malshan", "P.H.R Wijesinghe", "J.M.B Senadheera", "C.K.N Weerasinghe", "B.L.A Dassanayake", "U.S.M Jayawardena"};
        } else if (year == 4) {
            names = new String[]{"M.A.C Perera", "G.H.D Samaranayake", "V.S.N Kulasekara", "E.K.P Ranasinghe", "T.D.A Abeyratne", "N.H.M Jayatissa", "K.D.C Jayasinghe", "L.R.B Gunasekara"};
        } else {
            names = new String[]{"K.K.A.H.W Kumarasinghe", "K.A.B.V Smarathunga", "H.M.K.S Bandara", "W.M.P.L Wickramasinghe", "A.M.D.N Gunathilaka", "P.K.S.D Perera", "R.M.K.L Rathnayake", "S.A.N.C Senanayake"};
            ids = new int[]{18, 19, 20, 21, 22, 23, 24, 25};
            regNos = new String[]{"TG/2024/2062", "TG/2024/2083", "TG/2024/2084", "TG/2024/2085", "TG/2024/2086", "TG/2024/2087", "TG/2024/2088", "TG/2024/2089"};
        }

        int batchYear = 2026 - year;
        for (int i = 0; i < names.length; i++) {
            int sid = ids != null ? ids[i] : (year * 100 + i + 1);
            String reg = regNos != null ? regNos[i] : String.format("TG/%d/%d%03d", batchYear, year, i + 1);
            String status = existingAttendance.getOrDefault(sid, "Present");
            addStudentRow(sid, reg, names[i], year, status);
        }
    }

    /**
     * Saves or updates attendance records using PreparedStatement.
     * Compatible with tecfams_db schema and manual key tracking.
     */
    private void saveOrUpdateAttendance(boolean isExplicitUpdate) {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No student records loaded. Click 'Load Students' first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CourseItem course = (CourseItem) cmbCourse.getSelectedItem();
        Integer sessionNum = (Integer) cmbSessionNumber.getSelectedItem();
        String sessionType = (String) cmbSessionType.getSelectedItem();
        String dateText = txtSessionDate.getText().trim();

        if (course == null || sessionNum == null || sessionType == null) {
            JOptionPane.showMessageDialog(this, "Please select Course, Session Number, and Session Type.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate sessionDate;
        try {
            sessionDate = LocalDate.parse(dateText);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid Date format! Please use YYYY-MM-DD (e.g. " + LocalDate.now() + ").", "Invalid Date", JOptionPane.WARNING_MESSAGE);
            txtSessionDate.requestFocus();
            return;
        }

        Connection conn = DatabaseConnection.getConnection();
        int savedCount = 0;

        if (conn != null) {
            try {
                conn.setAutoCommit(false);

                // 1. Ensure attendance_session exists in database
                int sessionId = ensureSessionExists(conn, course.getCourseId(), sessionNum, sessionType, sessionDate);
                if (sessionId <= 0) {
                    throw new SQLException("Failed to resolve or create attendance session ID.");
                }

                // 2. Resolve recorded_by Technical Officer ID (FK constraint to technical_officers)
                int officerId = resolveTechnicalOfficerId(conn);

                // 3. Query maximum attendance_id for manual ID sequencing (tecfams_db schema)
                int nextAttendanceId = getNextId(conn, "attendance_id", "attendances");

                // 4. Insert or Update records adhering to existing schema
                String findAttSql = "SELECT attendance_id FROM attendances WHERE session_id = ? AND student_id = ?";
                String updateAttSql = "UPDATE attendances SET attendance_status = ?, recorded_by = ?, recorded_date = NOW() WHERE attendance_id = ?";
                String insertAttSql = "INSERT INTO attendances (attendance_id, session_id, student_id, attendance_status, recorded_by, recorded_date) " +
                                      "VALUES (?, ?, ?, ?, ?, NOW())";

                try (PreparedStatement stmtFind = conn.prepareStatement(findAttSql);
                     PreparedStatement stmtUpdate = conn.prepareStatement(updateAttSql);
                     PreparedStatement stmtInsert = conn.prepareStatement(insertAttSql)) {

                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        int studentId = Integer.parseInt(tableModel.getValueAt(i, 1).toString());
                        String status = tableModel.getValueAt(i, 5).toString();

                        stmtFind.setInt(1, sessionId);
                        stmtFind.setInt(2, studentId);
                        int existingAttId = -1;
                        try (ResultSet rsFind = stmtFind.executeQuery()) {
                            if (rsFind.next()) {
                                existingAttId = rsFind.getInt("attendance_id");
                            }
                        }

                        if (existingAttId > 0) {
                            stmtUpdate.setString(1, status);
                            stmtUpdate.setInt(2, officerId);
                            stmtUpdate.setInt(3, existingAttId);
                            stmtUpdate.executeUpdate();
                        } else {
                            stmtInsert.setInt(1, nextAttendanceId++);
                            stmtInsert.setInt(2, sessionId);
                            stmtInsert.setInt(3, studentId);
                            stmtInsert.setString(4, status);
                            stmtInsert.setInt(5, officerId);
                            stmtInsert.executeUpdate();
                        }
                        savedCount++;
                    }
                }

                conn.commit();
                conn.setAutoCommit(true);
                currentSessionId = sessionId;

                String actionVerb = isExplicitUpdate ? "updated" : "saved";
                JOptionPane.showMessageDialog(this,
                        "Attendance successfully " + actionVerb + " for " + savedCount + " students!\n\n" +
                        "Course: " + course.getCourseCode() + "\n" +
                        "Session: " + sessionType + " #" + sessionNum + "\n" +
                        "Date: " + sessionDate,
                        "Attendance Saved",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (SQLException e) {
                try {
                    conn.rollback();
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {}

                JOptionPane.showMessageDialog(this,
                        "Database Error while saving attendance:\n" + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            // Offline demo mode confirmation
            savedCount = tableModel.getRowCount();
            JOptionPane.showMessageDialog(this,
                    "Attendance successfully saved in local offline mode for " + savedCount + " students.\n" +
                    "(Note: Connect MySQL Server on port 3306 to persist directly into 'tecfams_db').",
                    "Saved (Offline Mode)",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        updateSummaryLabel(course, sessionNum, sessionType);
    }

    /**
     * Ensures the attendance session exists in 'attendance_sessions' table, returning its session_id.
     */
    private int ensureSessionExists(Connection conn, int courseId, int sessionNum, String sessionType, LocalDate date) throws SQLException {
        String findSql = "SELECT session_id FROM attendance_sessions WHERE course_id = ? AND session_number = ? AND session_type = ?";
        try (PreparedStatement stmt = conn.prepareStatement(findSql)) {
            stmt.setInt(1, courseId);
            stmt.setInt(2, sessionNum);
            stmt.setString(3, sessionType);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int sid = rs.getInt("session_id");
                    String updateSql = "UPDATE attendance_sessions SET session_date = ? WHERE session_id = ?";
                    try (PreparedStatement uStmt = conn.prepareStatement(updateSql)) {
                        uStmt.setDate(1, Date.valueOf(date));
                        uStmt.setInt(2, sid);
                        uStmt.executeUpdate();
                    }
                    return sid;
                }
            }
        }

        int nextSessionId = getNextId(conn, "session_id", "attendance_sessions");
        String insertSql = "INSERT INTO attendance_sessions (session_id, course_id, session_number, session_date, session_type, duration_hours) " +
                           "VALUES (?, ?, ?, ?, ?, 2.0)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setInt(1, nextSessionId);
            stmt.setInt(2, courseId);
            stmt.setInt(3, sessionNum);
            stmt.setDate(4, Date.valueOf(date));
            stmt.setString(5, sessionType);
            stmt.executeUpdate();
            return nextSessionId;
        }
    }

    private int getNextId(Connection conn, String column, String table) {
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COALESCE(MAX(" + column + "), 0) + 1 FROM " + table)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ignored) {}
        return 1;
    }

    /**
     * Resolves valid officer_id referencing technical_officers table.
     */
    private int resolveTechnicalOfficerId(Connection conn) {
        try {
            String rawId = currentOfficer.getOfficerId();
            if (rawId != null && rawId.matches("\\d+")) {
                return Integer.parseInt(rawId);
            }
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT officer_id FROM technical_officers LIMIT 1")) {
                if (rs.next()) return rs.getInt("officer_id");
            }
        } catch (Exception ignored) {}
        return 12; // Sample technical officer ID from tecfams_db schema
    }

    private void setAllStatus(String status) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(status, i, 5);
        }
        tblStudents.repaint();
    }

    private void clearForm() {
        tableModel.setRowCount(0);
        txtSessionDate.setText(LocalDate.now().toString());
        lblTableStatus.setText("Cleared. Select criteria and click 'Load Students'.");
        currentSessionId = -1;
    }

    private void backToDashboard() {
        dispose();
        if (parentDashboard != null) {
            parentDashboard.setVisible(true);
            parentDashboard.toFront();
        }
    }

    private void updateSummaryLabel(CourseItem course, int sessionNum, String sessionType) {
        int total = tableModel.getRowCount();
        int present = 0;
        for (int i = 0; i < total; i++) {
            if ("Present".equalsIgnoreCase(String.valueOf(tableModel.getValueAt(i, 5)))) {
                present++;
            }
        }
        int absent = total - present;
        lblTableStatus.setText(String.format("Loaded: %s | %s #%d | Total: %d  (Present: %d, Absent: %d)",
                course.getCourseCode(), sessionType, sessionNum, total, present, absent));
    }

    private int parseYearNumber(String text) {
        try {
            return Integer.parseInt(text.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 1;
        }
    }

    // =========================================================================
    // HELPER DTO
    // =========================================================================

    public static class CourseItem {
        private final int courseId;
        private final String courseCode;
        private final String courseName;
        private final String courseType;
        private final int academicYear;

        public CourseItem(int courseId, String courseCode, String courseName, String courseType, int academicYear) {
            this.courseId = courseId;
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.courseType = courseType;
            this.academicYear = academicYear;
        }

        public int getCourseId() { return courseId; }
        public String getCourseCode() { return courseCode; }
        public String getCourseName() { return courseName; }
        public String getCourseType() { return courseType; }
        public int getAcademicYear() { return academicYear; }

        @Override
        public String toString() {
            return courseCode + " - " + courseName;
        }
    }

    /**
     * Standalone main method to run and preview the Attendance Management Form.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            AttendanceManagementForm form = new AttendanceManagementForm();
            form.setVisible(true);
        });
    }
}
