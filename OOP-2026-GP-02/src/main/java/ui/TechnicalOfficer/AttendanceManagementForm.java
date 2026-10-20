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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static main.java.ui.TechnicalOfficer.TechnicalOfficerDashboard.*;

/**
 * AttendanceManagementForm - Technical Officer Module for TecFAMS.
 * University of Ruhuna - Faculty of Technology.
 * 
 * Features:
 * - Academic Year filter (populated from database)
 * - Course selector (populated from 'courses' table)
 * - Session Type ('Theory', 'Practical' - enum values from database)
 * - Session Number (1 to 15 sessions per module)
 * - Session Date (auto-retrieved from 'attendance_sessions' table)
 * - Student Table with Reg No, Full Name, Academic Year, and Present/Absent status
 * - Dropdown editor and colored badges for attendance status
 * - Save and Update Attendance with PreparedStatement and duplicate prevention
 * - Offline/Demo fallback mode if MySQL server is not currently running
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

        // 1. Top Header Bar
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Main Content Area (Form Controls + Table)
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

        // Right Actions (Officer pill + Back button)
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

        // 1. Academic Year
        JPanel pYear = createFieldWrapper("Academic Year / Level:", cmbAcademicYear = new JComboBox<>());
        cmbAcademicYear.setFont(FONT_REGULAR);
        cmbAcademicYear.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                filterCoursesByYear();
            }
        });

        // 2. Course
        JPanel pCourse = createFieldWrapper("Course / Module:", cmbCourse = new JComboBox<>());
        cmbCourse.setFont(FONT_REGULAR);
        cmbCourse.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                checkAndUpdateSessionDetails();
            }
        });

        // 3. Session Type (Theory / Practical as per DB schema)
        JPanel pType = createFieldWrapper("Session Type:", cmbSessionType = new JComboBox<>(new String[]{"Theory", "Practical"}));
        cmbSessionType.setFont(FONT_REGULAR);
        cmbSessionType.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                checkAndUpdateSessionDetails();
            }
        });

        // 4. Session Number (1 to 15)
        Integer[] sessionNums = new Integer[15];
        for (int i = 0; i < 15; i++) sessionNums[i] = i + 1;
        JPanel pNum = createFieldWrapper("Session Number (1-15):", cmbSessionNumber = new JComboBox<>(sessionNums));
        cmbSessionNumber.setFont(FONT_REGULAR);
        cmbSessionNumber.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                checkAndUpdateSessionDetails();
            }
        });

        // 5. Session Date
        txtSessionDate = new JTextField(LocalDate.now().toString());
        txtSessionDate.setFont(FONT_REGULAR);
        txtSessionDate.setToolTipText("Format: YYYY-MM-DD");
        JPanel pDate = createFieldWrapper("Session Date (YYYY-MM-DD):", txtSessionDate);

        // 6. Load Button
        JPanel pBtn = new JPanel(new BorderLayout());
        pBtn.setOpaque(false);
        JLabel emptyLabel = new JLabel(" ");
        emptyLabel.setFont(FONT_SMALL);
        pBtn.add(emptyLabel, BorderLayout.NORTH);

        btnLoadStudents = createPrimaryButton("Load Students");
        btnLoadStudents.setPreferredSize(new Dimension(0, 36));
        btnLoadStudents.addActionListener(e -> loadStudents());
        pBtn.add(btnLoadStudents, BorderLayout.CENTER);

        controls.add(pYear);
        controls.add(pCourse);
        controls.add(pType);
        controls.add(pNum);
        controls.add(pDate);
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

        // Table Header Label with summary counter
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

        // Table Model Setup
        String[] columnNames = {"#", "Database ID", "Registration No", "Student Full Name", "Level", "Attendance Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only the Attendance Status column (index 5) is editable
                return column == 5;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };

        tblStudents = new JTable(tableModel);
        tblStudents.setFont(FONT_REGULAR);
        tblStudents.setRowHeight(36);
        tblStudents.setSelectionBackground(LIGHT_BLUE);
        tblStudents.setSelectionForeground(TEXT_MAIN);
        tblStudents.setGridColor(BORDER_COLOR);
        tblStudents.setShowVerticalLines(true);
        tblStudents.setShowHorizontalLines(true);

        // Hide DB student_id column (index 1) from view while preserving in model
        tblStudents.getColumnModel().getColumn(1).setMinWidth(0);
        tblStudents.getColumnModel().getColumn(1).setMaxWidth(0);
        tblStudents.getColumnModel().getColumn(1).setPreferredWidth(0);

        // Column sizing
        tblStudents.getColumnModel().getColumn(0).setPreferredWidth(45);  // #
        tblStudents.getColumnModel().getColumn(2).setPreferredWidth(140); // Reg No
        tblStudents.getColumnModel().getColumn(3).setPreferredWidth(320); // Full Name
        tblStudents.getColumnModel().getColumn(4).setPreferredWidth(90);  // Level
        tblStudents.getColumnModel().getColumn(5).setPreferredWidth(160); // Status Dropdown

        // Table Header Styling
        JTableHeader th = tblStudents.getTableHeader();
        th.setFont(FONT_MEDIUM);
        th.setBackground(Color.decode("#F1F5F9"));
        th.setForeground(PRIMARY_NAVY);
        th.setPreferredSize(new Dimension(0, 36));

        // Attendance Status Cell Editor (JComboBox Dropdown)
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Present", "Absent"});
        statusCombo.setFont(FONT_MEDIUM);
        tblStudents.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(statusCombo));

        // Custom Cell Renderer for Attendance Status (Pill badges)
        tblStudents.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String val = value != null ? value.toString() : "Present";
                if ("Present".equalsIgnoreCase(val)) {
                    l.setForeground(Color.decode("#047857")); // dark emerald green
                    l.setText("●  Present");
                } else {
                    l.setForeground(Color.decode("#B91C1C")); // dark red
                    l.setText("○  Absent");
                }
                return l;
            }
        });

        // Center align row number and level
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tblStudents.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblStudents.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

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

        // Quick Batch Marking Tools
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JButton btnMarkAllPresent = createSecondaryButton("Mark All Present");
        btnMarkAllPresent.addActionListener(e -> setAllStatus("Present"));

        JButton btnMarkAllAbsent = createSecondaryButton("Mark All Absent");
        btnMarkAllAbsent.addActionListener(e -> setAllStatus("Absent"));

        left.add(new JLabel("Batch Actions:"));
        left.add(btnMarkAllPresent);
        left.add(btnMarkAllAbsent);

        // Main Action Buttons
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

    // =========================================================================
    // DATABASE OPERATIONS & DATA LOADING
    // =========================================================================

    private final List<CourseItem> allCourses = new ArrayList<>();

    private void loadInitialData() {
        loadAcademicYears();
        loadCourses();
        checkAndUpdateSessionDetails();
    }

    /**
     * Populates Academic Year dropdown from database.
     */
    private void loadAcademicYears() {
        cmbAcademicYear.removeAllItems();
        cmbAcademicYear.addItem("All Years");

        Connection conn = DatabaseConnection.getConnection();
        boolean loadedFromDb = false;

        if (conn != null) {
            try {
                String sql = "SELECT DISTINCT academic_year FROM courses ORDER BY academic_year ASC";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        int year = rs.getInt("academic_year");
                        cmbAcademicYear.addItem("Year " + year);
                        loadedFromDb = true;
                    }
                }
            } catch (SQLException e) {
                System.err.println("Notice: Could not load years via query: " + e.getMessage());
            }
        }

        // Standard fallback if database is empty or offline
        if (!loadedFromDb) {
            cmbAcademicYear.addItem("Year 1");
            cmbAcademicYear.addItem("Year 2");
            cmbAcademicYear.addItem("Year 3");
            cmbAcademicYear.addItem("Year 4");
        }
    }

    /**
     * Populates Courses dropdown from 'courses' table.
     */
    private void loadCourses() {
        allCourses.clear();
        cmbCourse.removeAllItems();

        Connection conn = DatabaseConnection.getConnection();
        boolean loadedFromDb = false;

        if (conn != null) {
            try {
                String sql = "SELECT course_id, course_code, course_name, course_type, academic_year FROM courses ORDER BY course_code ASC";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        CourseItem item = new CourseItem(
                                rs.getInt("course_id"),
                                rs.getString("course_code"),
                                rs.getString("course_name"),
                                rs.getString("course_type"),
                                rs.getInt("academic_year")
                        );
                        allCourses.add(item);
                        cmbCourse.addItem(item);
                        loadedFromDb = true;
                    }
                }
            } catch (SQLException e) {
                System.err.println("Notice: Could not query courses: " + e.getMessage());
            }
        }

        // Fallback demo courses matching actual TecFAMS schema
        if (!loadedFromDb) {
            allCourses.add(new CourseItem(1, "ICT2132", "Object Oriented Programming Practicum", "Theory_and_Practical", 2));
            allCourses.add(new CourseItem(2, "ICT2113", "Data Structures and Algorithms", "Theory_and_Practical", 2));
            allCourses.add(new CourseItem(3, "ICT2122", "Database Management Systems", "Theory_and_Practical", 2));
            allCourses.add(new CourseItem(4, "ENG2112", "Communication Skills II", "Theory_Only", 2));
            for (CourseItem c : allCourses) {
                cmbCourse.addItem(c);
            }
        }
    }

    private void filterCoursesByYear() {
        String selectedYear = (String) cmbAcademicYear.getSelectedItem();
        cmbCourse.removeAllItems();

        if (selectedYear == null || "All Years".equalsIgnoreCase(selectedYear)) {
            for (CourseItem c : allCourses) {
                cmbCourse.addItem(c);
            }
        } else {
            int year = parseYearNumber(selectedYear);
            for (CourseItem c : allCourses) {
                if (c.getAcademicYear() == year) {
                    cmbCourse.addItem(c);
                }
            }
        }
    }

    /**
     * Retrieves session date and session_id from 'attendance_sessions' if already scheduled.
     */
    private void checkAndUpdateSessionDetails() {
        CourseItem course = (CourseItem) cmbCourse.getSelectedItem();
        if (course == null) return;

        Integer sessionNum = (Integer) cmbSessionNumber.getSelectedItem();
        String sessionType = (String) cmbSessionType.getSelectedItem();
        if (sessionNum == null || sessionType == null) return;

        currentSessionId = -1;
        Connection conn = DatabaseConnection.getConnection();

        if (conn != null) {
            try {
                String sql = "SELECT session_id, session_date FROM attendance_sessions " +
                             "WHERE course_id = ? AND session_number = ? AND session_type = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, course.getCourseId());
                    stmt.setInt(2, sessionNum);
                    stmt.setString(3, sessionType);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            currentSessionId = rs.getInt("session_id");
                            Date d = rs.getDate("session_date");
                            if (d != null) {
                                txtSessionDate.setText(d.toString());
                            }
                        }
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
        boolean loaded = false;

        // Existing saved attendance map (student_id -> "Present" / "Absent")
        Map<Integer, String> existingAttendance = new HashMap<>();

        if (conn != null && currentSessionId > 0) {
            try {
                String attSql = "SELECT student_id, attendance_status FROM attendances WHERE session_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(attSql)) {
                    stmt.setInt(1, currentSessionId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            existingAttendance.put(rs.getInt("student_id"), rs.getString("attendance_status"));
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Notice: Could not query existing attendance: " + e.getMessage());
            }
        }

        // Query students enrolled in the course via course_registrations
        if (conn != null) {
            try {
                String studentSql = "SELECT s.student_id, s.student_reg_no, s.full_name, s.academic_year " +
                                    "FROM students s " +
                                    "JOIN course_registrations cr ON s.student_id = cr.student_id " +
                                    "WHERE cr.course_id = ? " +
                                    "ORDER BY s.student_reg_no ASC";

                try (PreparedStatement stmt = conn.prepareStatement(studentSql)) {
                    stmt.setInt(1, course.getCourseId());
                    try (ResultSet rs = stmt.executeQuery()) {
                        int rowIdx = 1;
                        while (rs.next()) {
                            int sid = rs.getInt("student_id");
                            String regNo = rs.getString("student_reg_no");
                            String name = rs.getString("full_name");
                            int year = rs.getInt("academic_year");

                            // Saved status or default to "Present"
                            String status = existingAttendance.getOrDefault(sid, "Present");

                            tableModel.addRow(new Object[]{
                                    rowIdx++,
                                    String.valueOf(sid),
                                    regNo,
                                    name,
                                    "Year " + year,
                                    status
                            });
                            loaded = true;
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Notice: Could not query students via registrations: " + e.getMessage());
            }
        }

        // Fallback sample data if database is empty or offline
        if (!loaded) {
            loadMockStudents(course, existingAttendance);
        }

        updateSummaryLabel(course, sessionNum, sessionType);
    }

    private void loadMockStudents(CourseItem course, Map<Integer, String> existingAttendance) {
        Object[][] mockData = {
                {18, "TG/2024/2062", "K.K.A.H.W Kumarasinghe", 2},
                {19, "TG/2024/2083", "K.A.B.V Smarathunga", 2},
                {20, "TG/2024/2084", "H.M.K.S Bandara", 2},
                {21, "TG/2024/2085", "W.M.P.L Wickramasinghe", 2},
                {22, "TG/2024/2086", "A.M.D.N Gunathilaka", 2},
                {23, "TG/2024/2087", "P.K.S.D Perera", 2},
                {24, "TG/2024/2088", "R.M.K.L Rathnayake", 2},
                {25, "TG/2024/2089", "S.A.N.C Senanayake", 2}
        };

        int rowIdx = 1;
        for (Object[] row : mockData) {
            int sid = (int) row[0];
            String status = existingAttendance.getOrDefault(sid, "Present");
            tableModel.addRow(new Object[]{
                    rowIdx++,
                    String.valueOf(sid),
                    row[1],
                    row[2],
                    "Year " + row[3],
                    status
            });
        }
    }

    /**
     * Saves or updates attendance records using PreparedStatement.
     * Prevents duplicates by adhering to UNIQUE constraint on (session_id, student_id).
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

        // Validate Date format YYYY-MM-DD
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

                // 2. Resolve recorded_by Technical Officer ID (FK constraint to technical_officers)
                int officerId = resolveTechnicalOfficerId(conn);

                // 3. Insert or Update records adhering to UNIQUE constraint (session_id, student_id)
                String upsertSql = "INSERT INTO attendances (session_id, student_id, attendance_status, recorded_by, recorded_date) " +
                                   "VALUES (?, ?, ?, ?, NOW()) " +
                                   "ON DUPLICATE KEY UPDATE attendance_status = VALUES(attendance_status), " +
                                   "recorded_by = VALUES(recorded_by), recorded_date = NOW()";

                try (PreparedStatement stmt = conn.prepareStatement(upsertSql)) {
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        int studentId = Integer.parseInt(tableModel.getValueAt(i, 1).toString());
                        String status = tableModel.getValueAt(i, 5).toString();

                        stmt.setInt(1, sessionId);
                        stmt.setInt(2, studentId);
                        stmt.setString(3, status);
                        stmt.setInt(4, officerId);
                        stmt.addBatch();
                        savedCount++;
                    }
                    stmt.executeBatch();
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
        // Check existing
        String findSql = "SELECT session_id FROM attendance_sessions WHERE course_id = ? AND session_number = ? AND session_type = ?";
        try (PreparedStatement stmt = conn.prepareStatement(findSql)) {
            stmt.setInt(1, courseId);
            stmt.setInt(2, sessionNum);
            stmt.setString(3, sessionType);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int sid = rs.getInt("session_id");
                    // Update session_date if changed
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

        // Insert new session record
        String insertSql = "INSERT INTO attendance_sessions (course_id, session_number, session_date, session_type, duration_hours) " +
                           "VALUES (?, ?, ?, ?, 2.0)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, courseId);
            stmt.setInt(2, sessionNum);
            stmt.setDate(3, Date.valueOf(date));
            stmt.setString(4, sessionType);
            stmt.executeUpdate();

            try (ResultSet gk = stmt.getGeneratedKeys()) {
                if (gk.next()) {
                    return gk.getInt(1);
                }
            }
        }
        return -1;
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
            // Fallback: pick existing technical officer id from database
            String sql = "SELECT officer_id FROM technical_officers LIMIT 1";
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery(sql)) {
                if (rs.next()) {
                    return rs.getInt("officer_id");
                }
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
