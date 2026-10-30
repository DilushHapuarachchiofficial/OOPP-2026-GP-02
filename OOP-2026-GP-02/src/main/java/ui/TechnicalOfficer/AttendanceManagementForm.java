package main.java.ui.TechnicalOfficer;

import main.java.dao.TechnicalOfficer.TechnicalOfficerDAO;
import main.java.model.TechnicalOfficer.TechnicalOfficer;
import main.java.model.Timetable;
import main.java.model.UserSession;
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
    private final TechnicalOfficerDAO dao;

    // Filter UI controls
    private JComboBox<String> cmbAcademicYear;
    private JComboBox<CourseItem> cmbCourse;
    private JComboBox<String> cmbSessionType;
    private JComboBox<PastSessionItem> cmbPastSessions;
    private JComboBox<Integer> cmbSessionNumber;
    private JTextField txtSessionDate;
    private JComboBox<String> cmbDurationHours;
    private JLabel lblSessionModeBadge;

    private JButton btnFromTimetable;
    private JButton btnPastSessions;
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
    private boolean isUpdatingDropdown = false;
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
        this.dao = new TechnicalOfficerDAO();

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
        String offName = (currentOfficer != null && currentOfficer.getName() != null && !currentOfficer.getName().trim().isEmpty())
                ? currentOfficer.getName().trim() : "Technical Officer";
        JLabel officerBadge = new JLabel("Officer: " + offName);
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
     * Filter card with Academic Year, Course, Session Number, Type, Date, Duration, and Timetable/History actions.
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
        cmbPastSessions = new JComboBox<>();

        Integer[] sessionNums = new Integer[15];
        for (int i = 0; i < 15; i++) sessionNums[i] = i + 1;
        cmbSessionNumber = new JComboBox<>(sessionNums);

        txtSessionDate = new JTextField(LocalDate.now().toString());
        txtSessionDate.setToolTipText("Format: YYYY-MM-DD");

        cmbDurationHours = new JComboBox<>(new String[]{"1.0", "1.5", "2.0", "2.5", "3.0", "4.0"});
        cmbDurationHours.setEditable(true);
        cmbDurationHours.setSelectedItem("2.0");
        cmbDurationHours.setToolTipText("Session duration in hours (e.g. 1.0, 2.0, 3.0)");

        for (JComponent c : new JComponent[]{cmbAcademicYear, cmbCourse, cmbSessionType, cmbPastSessions, cmbSessionNumber, txtSessionDate, cmbDurationHours}) {
            c.setFont(FONT_REGULAR);
        }

        onSelectionChanged(cmbAcademicYear, this::filterCoursesByYear);
        onSelectionChanged(cmbCourse, this::onCourseOrTypeChanged);
        onSelectionChanged(cmbSessionType, this::onCourseOrTypeChanged);
        onSelectionChanged(cmbPastSessions, this::onPastSessionSelected);
        onSelectionChanged(cmbSessionNumber, this::checkAndUpdateSessionDetails);

        JPanel pNumDur = new JPanel(new GridLayout(1, 2, 8, 0));
        pNumDur.setOpaque(false);
        pNumDur.add(createFieldWrapper("Session # (1-15):", cmbSessionNumber));
        pNumDur.add(createFieldWrapper("Duration (Hours):", cmbDurationHours));

        controls.add(createFieldWrapper("Academic Year / Level:", cmbAcademicYear));
        controls.add(createFieldWrapper("Course / Module:", cmbCourse));
        controls.add(createFieldWrapper("Session Type:", cmbSessionType));
        controls.add(createFieldWrapper("Past / Existing Session:", cmbPastSessions));
        controls.add(pNumDur);
        controls.add(createFieldWrapper("Session Date (YYYY-MM-DD):", txtSessionDate));

        JPanel bottomBar = new JPanel(new BorderLayout(10, 0));
        bottomBar.setOpaque(false);

        lblSessionModeBadge = new JLabel("● Mode: New Session");
        lblSessionModeBadge.setFont(FONT_MEDIUM);
        lblSessionModeBadge.setForeground(PRIMARY_BLUE);

        JPanel btnRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRight.setOpaque(false);

        btnFromTimetable = createSecondaryButton("📅 Load from Timetable");
        btnFromTimetable.setToolTipText("Pick scheduled lecture/lab from timetable to start session");
        btnFromTimetable.setPreferredSize(new Dimension(190, 36));
        btnFromTimetable.addActionListener(e -> showTimetableSelectionDialog());

        btnPastSessions = createSecondaryButton("📋 Past Sessions History");
        btnPastSessions.setToolTipText("Browse all previous sessions and edit attendance");
        btnPastSessions.setPreferredSize(new Dimension(190, 36));
        btnPastSessions.addActionListener(e -> showPastSessionsDialog());

        btnLoadStudents = createPrimaryButton("🔍 Load Students");
        btnLoadStudents.setPreferredSize(new Dimension(135, 36));
        btnLoadStudents.addActionListener(e -> loadStudents());

        btnRight.add(btnFromTimetable);
        btnRight.add(btnPastSessions);
        btnRight.add(btnLoadStudents);

        bottomBar.add(lblSessionModeBadge, BorderLayout.WEST);
        bottomBar.add(btnRight, BorderLayout.EAST);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setOpaque(false);
        contentPanel.add(controls, BorderLayout.CENTER);
        contentPanel.add(bottomBar, BorderLayout.SOUTH);

        card.add(contentPanel, BorderLayout.CENTER);
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
        btnUpdateAttendance = createPrimaryButton("Update Attendance");
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
                }
            } catch (SQLException e) {
                System.err.println("Notice: Could not query courses: " + e.getMessage());
            }
        }

        filterCoursesByYear();
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
        onCourseOrTypeChanged();
    }

    private void onCourseOrTypeChanged() {
        refreshPastSessionsDropdown();
        checkAndUpdateSessionDetails();
    }

    private void refreshPastSessionsDropdown() {
        if (cmbPastSessions == null) return;
        isUpdatingDropdown = true;
        cmbPastSessions.removeAllItems();
        cmbPastSessions.addItem(new PastSessionItem(-1, -1, LocalDate.now(), "", 2.0, 0, 0, 0));

        CourseItem course = (CourseItem) cmbCourse.getSelectedItem();
        if (course != null) {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                String sql = "SELECT s.session_id, s.session_number, s.session_date, s.session_type, s.duration_hours, " +
                             "       COUNT(a.attendance_id) AS total_marked, " +
                             "       SUM(CASE WHEN a.attendance_status = 'Present' THEN 1 ELSE 0 END) AS present_count, " +
                             "       SUM(CASE WHEN a.attendance_status = 'Absent' THEN 1 ELSE 0 END) AS absent_count " +
                             "FROM attendance_sessions s " +
                             "LEFT JOIN attendances a ON s.session_id = a.session_id " +
                             "WHERE s.course_id = ? " +
                             "GROUP BY s.session_id, s.session_number, s.session_date, s.session_type, s.duration_hours " +
                             "ORDER BY s.session_number ASC";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, course.getCourseId());
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            Date d = rs.getDate("session_date");
                            LocalDate lDate = d != null ? d.toLocalDate() : LocalDate.now();
                            cmbPastSessions.addItem(new PastSessionItem(
                                    rs.getInt("session_id"),
                                    rs.getInt("session_number"),
                                    lDate,
                                    rs.getString("session_type"),
                                    rs.getDouble("duration_hours"),
                                    rs.getInt("total_marked"),
                                    rs.getInt("present_count"),
                                    rs.getInt("absent_count")
                            ));
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Notice: Could not query past sessions: " + e.getMessage());
                }
            }
        }
        isUpdatingDropdown = false;
    }

    private void onPastSessionSelected() {
        if (isUpdatingDropdown || cmbPastSessions == null) return;
        PastSessionItem item = (PastSessionItem) cmbPastSessions.getSelectedItem();
        if (item == null) return;

        if (item.getSessionId() <= 0) {
            // New Session Mode
            currentSessionId = -1;
            txtSessionDate.setText(LocalDate.now().toString());
            cmbDurationHours.setSelectedItem("2.0");
            if (lblSessionModeBadge != null) {
                lblSessionModeBadge.setText("● Mode: New Session");
                lblSessionModeBadge.setForeground(PRIMARY_BLUE);
            }
            suggestNextSessionNumber();
            loadStudents();
        } else {
            // Viewing Past Session Mode
            currentSessionId = item.getSessionId();
            cmbSessionNumber.setSelectedItem(item.getSessionNumber());
            if ("Practical".equalsIgnoreCase(item.getSessionType())) {
                cmbSessionType.setSelectedItem("Practical");
            } else {
                cmbSessionType.setSelectedItem("Theory");
            }
            if (item.getSessionDate() != null) {
                txtSessionDate.setText(item.getSessionDate().toString());
            }
            double dur = item.getDurationHours();
            String durStr = (dur == (long) dur) ? String.format("%.1f", dur) : String.valueOf(dur);
            cmbDurationHours.setSelectedItem(durStr);
            if (lblSessionModeBadge != null) {
                lblSessionModeBadge.setText(String.format("● Mode: Viewing Past Session #%d (%s)",
                        item.getSessionNumber(), item.getSessionDate()));
                lblSessionModeBadge.setForeground(Color.decode("#047857"));
            }
            loadStudents();
        }
    }

    private void suggestNextSessionNumber() {
        CourseItem course = (CourseItem) cmbCourse.getSelectedItem();
        String sessionType = (String) cmbSessionType.getSelectedItem();
        if (course == null || sessionType == null) return;

        int nextNum = 1;
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            String sql = "SELECT COALESCE(MAX(session_number), 0) + 1 FROM attendance_sessions WHERE course_id = ? AND session_type = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, course.getCourseId());
                stmt.setString(2, sessionType);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        nextNum = rs.getInt(1);
                    }
                }
            } catch (SQLException ignored) {}
        }
        if (nextNum > 15) nextNum = 15;
        cmbSessionNumber.setSelectedItem(nextNum);
    }

    /**
     * Retrieves session date, duration, and session_id from 'attendance_sessions' if already scheduled.
     */
    private void checkAndUpdateSessionDetails() {
        CourseItem course = (CourseItem) cmbCourse.getSelectedItem();
        Integer sessionNum = (Integer) cmbSessionNumber.getSelectedItem();
        String sessionType = (String) cmbSessionType.getSelectedItem();
        if (course == null || sessionNum == null || sessionType == null) return;

        currentSessionId = -1;
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            String sql = "SELECT session_id, session_date, duration_hours FROM attendance_sessions WHERE course_id = ? AND session_number = ? AND session_type = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, course.getCourseId());
                stmt.setInt(2, sessionNum);
                stmt.setString(3, sessionType);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        currentSessionId = rs.getInt("session_id");
                        Date d = rs.getDate("session_date");
                        if (d != null) txtSessionDate.setText(d.toString());
                        double dur = rs.getDouble("duration_hours");
                        if (dur > 0 && cmbDurationHours != null) {
                            String durStr = (dur == (long) dur) ? String.format("%.1f", dur) : String.valueOf(dur);
                            cmbDurationHours.setSelectedItem(durStr);
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Notice: Could not check session: " + e.getMessage());
            }
        }
    }

    private double parseDurationHours() {
        if (cmbDurationHours == null) return 2.0;
        try {
            Object item = cmbDurationHours.getSelectedItem();
            if (item != null) {
                double val = Double.parseDouble(item.toString().trim());
                if (val > 0) return val;
            }
        } catch (Exception ignored) {}
        return 2.0;
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
            // A. If viewing an existing recorded session, load students directly from attendances table
            if (currentSessionId > 0) {
                String recordedSql = "SELECT s.student_id, s.student_reg_no, s.full_name, s.academic_year, a.attendance_status " +
                                     "FROM attendances a JOIN students s ON a.student_id = s.student_id " +
                                     "WHERE a.session_id = ? ORDER BY s.student_reg_no ASC";
                try (PreparedStatement stmt = conn.prepareStatement(recordedSql)) {
                    stmt.setInt(1, currentSessionId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            addStudentRow(rs.getInt("student_id"), rs.getString("student_reg_no"),
                                          rs.getString("full_name"), rs.getInt("academic_year"),
                                          rs.getString("attendance_status"));
                            loaded = true;
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Notice: Could not query recorded session students: " + e.getMessage());
                }
            }

            // B. If not loaded from existing session, try querying enrolled students from course_registrations
            if (!loaded) {
                String regSql = "SELECT s.student_id, s.student_reg_no, s.full_name, s.academic_year " +
                                "FROM students s JOIN course_registrations cr ON s.student_id = cr.student_id " +
                                "WHERE cr.course_id = ? ORDER BY s.student_reg_no ASC";
                loaded = queryStudents(conn, regSql, course.getCourseId(), existingAttendance);
            }

            // C. If no direct registrations, query students by academic year from DB
            if (!loaded) {
                String yearSql = "SELECT student_id, student_reg_no, full_name, academic_year FROM students WHERE academic_year = ? ORDER BY student_reg_no ASC";
                loaded = queryStudents(conn, yearSql, course.getAcademicYear(), existingAttendance);
            }
        }

        if (!loaded) {
            if (conn == null) {
                lblTableStatus.setText("Database Connection Error. Please verify MySQL service is running.");
            } else {
                lblTableStatus.setText("No registered students found in database for this course.");
            }
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

        double durationHours = parseDurationHours();
        if (durationHours <= 0 || durationHours > 12) {
            JOptionPane.showMessageDialog(this, "Please enter a valid session duration between 0.5 and 12 hours.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            cmbDurationHours.requestFocus();
            return;
        }

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this,
                    "Database is not connected! Please ensure MySQL server is running.",
                    "Database Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int savedCount = 0;

        try {
            conn.setAutoCommit(false);

            // 1. Ensure attendance_session exists in database
            int sessionId = ensureSessionExists(conn, course.getCourseId(), sessionNum, sessionType, sessionDate, durationHours);
            if (sessionId <= 0) {
                throw new SQLException("Failed to resolve or create attendance session ID.");
            }

            // 2. Resolve recorded_by Technical Officer ID (FK constraint to technical_officers)
            int officerId = resolveTechnicalOfficerId(conn);
            if (officerId <= 0) {
                JOptionPane.showMessageDialog(this, "Cannot save attendance: No Technical Officer found in database. Please log in first.", "Officer Error", JOptionPane.ERROR_MESSAGE);
                conn.rollback();
                conn.setAutoCommit(true);
                return;
            }

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
            refreshPastSessionsDropdown();

            if (lblSessionModeBadge != null) {
                lblSessionModeBadge.setText(String.format("● Mode: Viewing Past Session #%d (%s)", sessionNum, sessionDate));
                lblSessionModeBadge.setForeground(Color.decode("#047857"));
            }

            String actionVerb = isExplicitUpdate ? "updated" : "saved";
            JOptionPane.showMessageDialog(this,
                    "Attendance successfully " + actionVerb + " for " + savedCount + " students!\n\n" +
                    "Course: " + course.getCourseCode() + "\n" +
                    "Session: " + sessionType + " #" + sessionNum + "\n" +
                    "Duration: " + durationHours + " hrs\n" +
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

        updateSummaryLabel(course, sessionNum, sessionType);
    }

    /**
     * Ensures the attendance session exists in 'attendance_sessions' table, returning its session_id.
     */
    private int ensureSessionExists(Connection conn, int courseId, int sessionNum, String sessionType, LocalDate date, double durationHours) throws SQLException {
        String findSql = "SELECT session_id FROM attendance_sessions WHERE course_id = ? AND session_number = ? AND session_type = ?";
        try (PreparedStatement stmt = conn.prepareStatement(findSql)) {
            stmt.setInt(1, courseId);
            stmt.setInt(2, sessionNum);
            stmt.setString(3, sessionType);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int sid = rs.getInt("session_id");
                    String updateSql = "UPDATE attendance_sessions SET session_date = ?, duration_hours = ? WHERE session_id = ?";
                    try (PreparedStatement uStmt = conn.prepareStatement(updateSql)) {
                        uStmt.setDate(1, Date.valueOf(date));
                        uStmt.setDouble(2, durationHours);
                        uStmt.setInt(3, sid);
                        uStmt.executeUpdate();
                    }
                    return sid;
                }
            }
        }

        int nextSessionId = getNextId(conn, "session_id", "attendance_sessions");
        String insertSql = "INSERT INTO attendance_sessions (session_id, course_id, session_number, session_date, session_type, duration_hours) " +
                           "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setInt(1, nextSessionId);
            stmt.setInt(2, courseId);
            stmt.setInt(3, sessionNum);
            stmt.setDate(4, Date.valueOf(date));
            stmt.setString(5, sessionType);
            stmt.setDouble(6, durationHours);
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
        if (UserSession.isLoggedIn()) {
            return UserSession.getCurrentSession().getUserId();
        }
        if (currentOfficer != null && currentOfficer.getOfficerId() != null) {
            String rawId = currentOfficer.getOfficerId().trim();
            if (rawId.matches("\\d+")) {
                return Integer.parseInt(rawId);
            }
        }
        if (conn != null) {
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT officer_id FROM technical_officers ORDER BY officer_id ASC LIMIT 1")) {
                if (rs.next()) return rs.getInt("officer_id");
            } catch (SQLException ignored) {}
        }
        return -1;
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
        if (cmbDurationHours != null) cmbDurationHours.setSelectedItem("2.0");
        lblTableStatus.setText("Cleared. Select criteria and click 'Load Students'.");
        currentSessionId = -1;
        if (lblSessionModeBadge != null) {
            lblSessionModeBadge.setText("● Mode: New Session");
            lblSessionModeBadge.setForeground(PRIMARY_BLUE);
        }
        if (cmbPastSessions != null && cmbPastSessions.getItemCount() > 0) {
            cmbPastSessions.setSelectedIndex(0);
        }
    }

    private void backToDashboard() {
        if (parentDashboard instanceof TechnicalOfficerDashboard) {
            ((TechnicalOfficerDashboard) parentDashboard).showDashboardView();
        } else {
            dispose();
            if (parentDashboard != null) {
                parentDashboard.setVisible(true);
                parentDashboard.toFront();
            }
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
        double dur = parseDurationHours();
        String mode = (currentSessionId > 0) ? "Viewing/Editing Past Session" : "New Session";
        lblTableStatus.setText(String.format("[%s] %s | %s #%d (%.1f hrs) | Total: %d  (Present: %d, Absent: %d)",
                mode, course.getCourseCode(), sessionType, sessionNum, dur, total, present, absent));
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SMALL);
        label.setForeground(TEXT_MAIN);
        return label;
    }

    // =========================================================================
    // TIMETABLE & PAST SESSION DIALOGS
    // =========================================================================

    private void showTimetableSelectionDialog() {
        JDialog dlg = new JDialog(this, "Select Timetable Slot for New Attendance Session", true);
        dlg.setLayout(new BorderLayout(0, 12));
        dlg.setSize(1020, 560);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(BG_COLOR);

        // Header
        JPanel pTop = new JPanel(new BorderLayout());
        pTop.setBackground(WHITE);
        pTop.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(14, 20, 14, 20)
        ));
        JLabel title = new JLabel("Department Timetable Schedule");
        title.setFont(FONT_HEADER_MEDIUM);
        title.setForeground(PRIMARY_NAVY);
        JLabel sub = new JLabel("Choose a scheduled class slot to automatically initialize and log an attendance session");
        sub.setFont(FONT_SMALL);
        sub.setForeground(TEXT_SECONDARY);
        JPanel pText = new JPanel(new GridLayout(2, 1, 0, 2));
        pText.setOpaque(false);
        pText.add(title);
        pText.add(sub);
        pTop.add(pText, BorderLayout.WEST);

        // Filter Strip
        JPanel pFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        pFilter.setOpaque(false);
        JComboBox<String> cmbDay = new JComboBox<>(new String[]{"All Days", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"});
        JComboBox<String> cmbLvl = new JComboBox<>(new String[]{"All Levels", "Level 1", "Level 2", "Level 3", "Level 4"});
        JComboBox<String> cmbTyp = new JComboBox<>(new String[]{"All Types", "Theory", "Practical"});
        JTextField txtSearch = new JTextField(12);

        pFilter.add(createFieldLabel("Day:"));
        pFilter.add(cmbDay);
        pFilter.add(createFieldLabel("Level:"));
        pFilter.add(cmbLvl);
        pFilter.add(createFieldLabel("Type:"));
        pFilter.add(cmbTyp);
        pFilter.add(createFieldLabel("Search:"));
        pFilter.add(txtSearch);

        // Timetable Table
        String[] cols = {"#", "Day", "Time Slot", "Course Code", "Course Name", "Session Type", "Venue / Hall", "Level", "Est. Duration"};
        DefaultTableModel tm = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(tm);
        tbl.setFont(FONT_REGULAR);
        tbl.setRowHeight(32);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl.setSelectionBackground(LIGHT_BLUE);
        tbl.setSelectionForeground(TEXT_MAIN);
        tbl.setGridColor(BORDER_COLOR);
        tbl.getTableHeader().setFont(FONT_MEDIUM);
        tbl.getTableHeader().setBackground(Color.decode("#F1F5F9"));
        tbl.getTableHeader().setForeground(PRIMARY_NAVY);

        int[] widths = {40, 95, 120, 95, 270, 95, 120, 75, 90};
        for (int i = 0; i < widths.length && i < tbl.getColumnCount(); i++) {
            tbl.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        List<Timetable> allSlots = new ArrayList<>();
        Runnable loadTable = () -> {
            tm.setRowCount(0);
            allSlots.clear();
            String selDay = (String) cmbDay.getSelectedItem();
            String selLvl = (String) cmbLvl.getSelectedItem();
            String selTyp = (String) cmbTyp.getSelectedItem();
            String q = txtSearch.getText().trim().toLowerCase();

            Integer yr = null;
            if (selLvl != null && selLvl.contains("1")) yr = 1;
            else if (selLvl != null && selLvl.contains("2")) yr = 2;
            else if (selLvl != null && selLvl.contains("3")) yr = 3;
            else if (selLvl != null && selLvl.contains("4")) yr = 4;

            List<Timetable> entries = dao.getDepartmentTimetableEntries(null, yr, null,
                    "All Days".equalsIgnoreCase(selDay) ? null : selDay,
                    "All Types".equalsIgnoreCase(selTyp) ? null : selTyp,
                    q.isEmpty() ? null : q);

            int idx = 1;
            for (Timetable t : entries) {
                if (selDay != null && !"All Days".equalsIgnoreCase(selDay) && !selDay.equalsIgnoreCase(t.getDayOfWeek())) continue;
                if (selTyp != null && !"All Types".equalsIgnoreCase(selTyp) && !selTyp.equalsIgnoreCase(t.getSessionType())) continue;
                if (yr != null && t.getAcademicYear() > 0 && t.getAcademicYear() != yr) continue;
                if (!q.isEmpty()) {
                    boolean mCode = t.getCourseCode() != null && t.getCourseCode().toLowerCase().contains(q);
                    boolean mName = t.getCourseName() != null && t.getCourseName().toLowerCase().contains(q);
                    boolean mHall = t.getLocation() != null && t.getLocation().toLowerCase().contains(q);
                    if (!mCode && !mName && !mHall) continue;
                }

                allSlots.add(t);
                double dur = parseDurationFromTimeSlot(t.getTimeSlot());
                tm.addRow(new Object[]{
                        idx++,
                        t.getDayOfWeek(),
                        t.getTimeSlot(),
                        t.getCourseCode(),
                        t.getCourseName(),
                        t.getSessionType(),
                        t.getLocation(),
                        "Level " + t.getAcademicYear(),
                        String.format("%.1f hrs", dur)
                });
            }
        };

        cmbDay.addItemListener(e -> { if (e.getStateChange() == ItemEvent.SELECTED) loadTable.run(); });
        cmbLvl.addItemListener(e -> { if (e.getStateChange() == ItemEvent.SELECTED) loadTable.run(); });
        cmbTyp.addItemListener(e -> { if (e.getStateChange() == ItemEvent.SELECTED) loadTable.run(); });
        txtSearch.addActionListener(e -> loadTable.run());
        loadTable.run();

        // Bottom action bar
        JPanel pBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        pBottom.setBackground(WHITE);
        pBottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JButton btnCancel = createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSelect = createPrimaryButton("Start Attendance Session →");
        btnSelect.setPreferredSize(new Dimension(220, 36));

        Runnable doSelect = () -> {
            int sel = tbl.getSelectedRow();
            if (sel < 0 || sel >= allSlots.size()) {
                JOptionPane.showMessageDialog(dlg, "Please select a timetable slot from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Timetable slot = allSlots.get(sel);
            applyTimetableSlot(slot);
            dlg.dispose();
        };

        btnSelect.addActionListener(e -> doSelect.run());
        tbl.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) doSelect.run();
            }
        });

        pBottom.add(btnCancel);
        pBottom.add(btnSelect);

        JPanel pCenter = new JPanel(new BorderLayout(0, 8));
        pCenter.setOpaque(false);
        pCenter.setBorder(new EmptyBorder(0, 16, 0, 16));
        pCenter.add(pFilter, BorderLayout.NORTH);
        pCenter.add(new JScrollPane(tbl), BorderLayout.CENTER);

        dlg.add(pTop, BorderLayout.NORTH);
        dlg.add(pCenter, BorderLayout.CENTER);
        dlg.add(pBottom, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    public void applyTimetableSlot(Timetable slot) {
        if (slot == null) return;

        // 1. Set Academic Year
        if (slot.getAcademicYear() > 0) {
            cmbAcademicYear.setSelectedItem("Year " + slot.getAcademicYear());
        }

        // 2. Match Course
        CourseItem matchCourse = null;
        for (int i = 0; i < cmbCourse.getItemCount(); i++) {
            CourseItem ci = cmbCourse.getItemAt(i);
            if (ci != null && ci.getCourseCode().equalsIgnoreCase(slot.getCourseCode())) {
                matchCourse = ci;
                break;
            }
        }
        if (matchCourse != null) {
            cmbCourse.setSelectedItem(matchCourse);
        } else {
            for (CourseItem ci : allCourses) {
                if (ci.getCourseCode().equalsIgnoreCase(slot.getCourseCode())) {
                    cmbCourse.addItem(ci);
                    cmbCourse.setSelectedItem(ci);
                    matchCourse = ci;
                    break;
                }
            }
        }

        // 3. Set Session Type
        if ("Practical".equalsIgnoreCase(slot.getSessionType())) {
            cmbSessionType.setSelectedItem("Practical");
        } else {
            cmbSessionType.setSelectedItem("Theory");
        }

        // 4. Set Duration
        double dur = parseDurationFromTimeSlot(slot.getTimeSlot());
        String durStr = (dur == (long) dur) ? String.format("%.1f", dur) : String.valueOf(dur);
        cmbDurationHours.setSelectedItem(durStr);

        // 5. Set Date to Today
        txtSessionDate.setText(LocalDate.now().toString());

        // 6. Suggest next session number
        suggestNextSessionNumber();

        // 7. Reset to New Session mode
        currentSessionId = -1;
        if (lblSessionModeBadge != null) {
            lblSessionModeBadge.setText(String.format("● Mode: New Session (Timetable: %s - %s)", slot.getCourseCode(), slot.getTimeSlot()));
            lblSessionModeBadge.setForeground(PRIMARY_BLUE);
        }

        // 8. Load enrolled students
        loadStudents();

        JOptionPane.showMessageDialog(this,
                "Attendance Session initialized from Department Timetable!\n\n" +
                "Course: " + slot.getCourseCode() + " - " + slot.getCourseName() + "\n" +
                "Session: " + cmbSessionType.getSelectedItem() + " #" + cmbSessionNumber.getSelectedItem() + " (" + durStr + " hrs)\n" +
                "Schedule: " + slot.getDayOfWeek() + " (" + slot.getTimeSlot() + ") at " + slot.getLocation() + "\n\n" +
                "Student list loaded. Please adjust any absent students and click 'Save Attendance'.",
                "Timetable Session Loaded",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showPastSessionsDialog() {
        JDialog dlg = new JDialog(this, "Past Attendance Sessions History", true);
        dlg.setLayout(new BorderLayout(0, 12));
        dlg.setSize(1040, 580);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(BG_COLOR);

        // Top Header
        JPanel pTop = new JPanel(new BorderLayout());
        pTop.setBackground(WHITE);
        pTop.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(14, 20, 14, 20)
        ));
        JLabel title = new JLabel("Past Attendance Sessions History");
        title.setFont(FONT_HEADER_MEDIUM);
        title.setForeground(PRIMARY_NAVY);
        JLabel sub = new JLabel("Browse previously recorded sessions, view attendance counts, and open any session to edit Present/Absent statuses");
        sub.setFont(FONT_SMALL);
        sub.setForeground(TEXT_SECONDARY);
        JPanel pText = new JPanel(new GridLayout(2, 1, 0, 2));
        pText.setOpaque(false);
        pText.add(title);
        pText.add(sub);
        pTop.add(pText, BorderLayout.WEST);

        // Filter Strip
        JPanel pFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        pFilter.setOpaque(false);
        JComboBox<String> cmbYearFilter = new JComboBox<>(new String[]{"All Years", "Year 1", "Year 2", "Year 3", "Year 4"});
        JComboBox<String> cmbTypeFilter = new JComboBox<>(new String[]{"All Types", "Theory", "Practical"});
        JTextField txtSearch = new JTextField(14);
        txtSearch.setToolTipText("Search course code or name");

        pFilter.add(createFieldLabel("Year:"));
        pFilter.add(cmbYearFilter);
        pFilter.add(createFieldLabel("Session Type:"));
        pFilter.add(cmbTypeFilter);
        pFilter.add(createFieldLabel("Search:"));
        pFilter.add(txtSearch);

        // Sessions Table
        String[] cols = {"Session ID", "Date", "Course Code", "Course Name", "Type", "Session #", "Duration", "Present", "Absent", "Total Students"};
        DefaultTableModel tm = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(tm);
        tbl.setFont(FONT_REGULAR);
        tbl.setRowHeight(32);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl.setSelectionBackground(LIGHT_BLUE);
        tbl.setSelectionForeground(TEXT_MAIN);
        tbl.setGridColor(BORDER_COLOR);
        tbl.getTableHeader().setFont(FONT_MEDIUM);
        tbl.getTableHeader().setBackground(Color.decode("#F1F5F9"));
        tbl.getTableHeader().setForeground(PRIMARY_NAVY);

        int[] widths = {70, 95, 95, 260, 85, 75, 75, 65, 65, 90};
        for (int i = 0; i < widths.length && i < tbl.getColumnCount(); i++) {
            tbl.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        List<PastSessionRecord> sessionRecords = new ArrayList<>();
        Runnable loadSessions = () -> {
            tm.setRowCount(0);
            sessionRecords.clear();
            String selYr = (String) cmbYearFilter.getSelectedItem();
            String selTyp = (String) cmbTypeFilter.getSelectedItem();
            String q = txtSearch.getText().trim().toLowerCase();

            int yr = (selYr != null && !"All Years".equalsIgnoreCase(selYr)) ? parseYearNumber(selYr) : -1;

            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                StringBuilder sql = new StringBuilder(
                        "SELECT s.session_id, s.course_id, c.course_code, c.course_name, c.academic_year, " +
                        "       s.session_number, s.session_date, s.session_type, s.duration_hours, " +
                        "       COUNT(a.attendance_id) AS total_count, " +
                        "       SUM(CASE WHEN a.attendance_status = 'Present' THEN 1 ELSE 0 END) AS present_count, " +
                        "       SUM(CASE WHEN a.attendance_status = 'Absent' THEN 1 ELSE 0 END) AS absent_count " +
                        "FROM attendance_sessions s " +
                        "JOIN courses c ON s.course_id = c.course_id " +
                        "LEFT JOIN attendances a ON s.session_id = a.session_id " +
                        "WHERE 1=1 "
                );
                List<Object> params = new ArrayList<>();
                if (yr > 0) {
                    sql.append(" AND c.academic_year = ? ");
                    params.add(yr);
                }
                if (selTyp != null && !"All Types".equalsIgnoreCase(selTyp)) {
                    sql.append(" AND s.session_type = ? ");
                    params.add(selTyp);
                }
                if (!q.isEmpty()) {
                    sql.append(" AND (c.course_code LIKE ? OR c.course_name LIKE ?) ");
                    params.add("%" + q + "%");
                    params.add("%" + q + "%");
                }
                sql.append("GROUP BY s.session_id, s.course_id, c.course_code, c.course_name, c.academic_year, " +
                           "         s.session_number, s.session_date, s.session_type, s.duration_hours " +
                           "ORDER BY s.session_date DESC, s.session_number DESC");

                try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                    for (int i = 0; i < params.size(); i++) {
                        stmt.setObject(i + 1, params.get(i));
                    }
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            PastSessionRecord r = new PastSessionRecord();
                            r.sessionId = rs.getInt("session_id");
                            r.courseId = rs.getInt("course_id");
                            r.courseCode = rs.getString("course_code");
                            r.courseName = rs.getString("course_name");
                            r.academicYear = rs.getInt("academic_year");
                            r.sessionNumber = rs.getInt("session_number");
                            Date d = rs.getDate("session_date");
                            r.sessionDate = d != null ? d.toLocalDate() : LocalDate.now();
                            r.sessionType = rs.getString("session_type");
                            r.durationHours = rs.getDouble("duration_hours");
                            r.totalMarked = rs.getInt("total_count");
                            r.presentCount = rs.getInt("present_count");
                            r.absentCount = rs.getInt("absent_count");

                            sessionRecords.add(r);
                            tm.addRow(new Object[]{
                                    r.sessionId,
                                    r.sessionDate.toString(),
                                    r.courseCode,
                                    r.courseName,
                                    r.sessionType,
                                    "#" + r.sessionNumber,
                                    String.format("%.1f hrs", r.durationHours),
                                    r.presentCount,
                                    r.absentCount,
                                    r.totalMarked
                            });
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Notice: Could not query past sessions: " + e.getMessage());
                }
            }
        };

        cmbYearFilter.addItemListener(e -> { if (e.getStateChange() == ItemEvent.SELECTED) loadSessions.run(); });
        cmbTypeFilter.addItemListener(e -> { if (e.getStateChange() == ItemEvent.SELECTED) loadSessions.run(); });
        txtSearch.addActionListener(e -> loadSessions.run());
        loadSessions.run();

        // Bottom action bar
        JPanel pBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        pBottom.setBackground(WHITE);
        pBottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JButton btnCancel = createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnOpen = createPrimaryButton("Open & Edit Attendance →");
        btnOpen.setPreferredSize(new Dimension(210, 36));

        Runnable doOpen = () -> {
            int sel = tbl.getSelectedRow();
            if (sel < 0 || sel >= sessionRecords.size()) {
                JOptionPane.showMessageDialog(dlg, "Please select a past session from the list first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            PastSessionRecord r = sessionRecords.get(sel);
            applyPastSession(r);
            dlg.dispose();
        };

        btnOpen.addActionListener(e -> doOpen.run());
        tbl.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) doOpen.run();
            }
        });

        pBottom.add(btnCancel);
        pBottom.add(btnOpen);

        JPanel pCenter = new JPanel(new BorderLayout(0, 8));
        pCenter.setOpaque(false);
        pCenter.setBorder(new EmptyBorder(0, 16, 0, 16));
        pCenter.add(pFilter, BorderLayout.NORTH);
        pCenter.add(new JScrollPane(tbl), BorderLayout.CENTER);

        dlg.add(pTop, BorderLayout.NORTH);
        dlg.add(pCenter, BorderLayout.CENTER);
        dlg.add(pBottom, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void applyPastSession(PastSessionRecord r) {
        if (r == null) return;

        // 1. Set Academic Year
        if (r.academicYear > 0) {
            cmbAcademicYear.setSelectedItem("Year " + r.academicYear);
        }

        // 2. Select Course
        CourseItem matchCourse = null;
        for (int i = 0; i < cmbCourse.getItemCount(); i++) {
            CourseItem ci = cmbCourse.getItemAt(i);
            if (ci != null && (ci.getCourseId() == r.courseId || ci.getCourseCode().equalsIgnoreCase(r.courseCode))) {
                matchCourse = ci;
                break;
            }
        }
        if (matchCourse != null) {
            cmbCourse.setSelectedItem(matchCourse);
        }

        // 3. Set Session Type
        if ("Practical".equalsIgnoreCase(r.sessionType)) {
            cmbSessionType.setSelectedItem("Practical");
        } else {
            cmbSessionType.setSelectedItem("Theory");
        }

        // 4. Set Session Number
        cmbSessionNumber.setSelectedItem(r.sessionNumber);

        // 5. Set Date
        if (r.sessionDate != null) {
            txtSessionDate.setText(r.sessionDate.toString());
        }

        // 6. Set Duration
        double dur = r.durationHours;
        String durStr = (dur == (long) dur) ? String.format("%.1f", dur) : String.valueOf(dur);
        cmbDurationHours.setSelectedItem(durStr);

        // 7. Track current session ID
        currentSessionId = r.sessionId;

        // 8. Update badge
        if (lblSessionModeBadge != null) {
            lblSessionModeBadge.setText(String.format("● Mode: Viewing Past Session #%d (%s - %s)",
                    r.sessionNumber, r.courseCode, r.sessionDate));
            lblSessionModeBadge.setForeground(Color.decode("#047857"));
        }

        // 9. Load students and their recorded statuses
        loadStudents();

        // 10. Update past sessions dropdown selection
        if (cmbPastSessions != null) {
            for (int i = 0; i < cmbPastSessions.getItemCount(); i++) {
                PastSessionItem item = cmbPastSessions.getItemAt(i);
                if (item != null && item.getSessionId() == r.sessionId) {
                    cmbPastSessions.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private double parseDurationFromTimeSlot(String timeSlot) {
        if (timeSlot == null || !timeSlot.contains("-")) return 2.0;
        try {
            String[] parts = timeSlot.split("-");
            String[] start = parts[0].trim().split(":");
            String[] end = parts[1].trim().split(":");
            double sHours = Double.parseDouble(start[0]) + Double.parseDouble(start[1]) / 60.0;
            double eHours = Double.parseDouble(end[0]) + Double.parseDouble(end[1]) / 60.0;
            double diff = eHours - sHours;
            return diff > 0 ? Math.round(diff * 10.0) / 10.0 : 2.0;
        } catch (Exception e) {
            return 2.0;
        }
    }

    private int parseYearNumber(String text) {
        try {
            return Integer.parseInt(text.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 1;
        }
    }

    // =========================================================================
    // HELPER DTOS
    // =========================================================================

    public static class PastSessionItem {
        private final int sessionId;
        private final int sessionNumber;
        private final LocalDate sessionDate;
        private final String sessionType;
        private final double durationHours;
        private final int totalRecords;
        private final int presentCount;
        private final int absentCount;

        public PastSessionItem(int sessionId, int sessionNumber, LocalDate sessionDate, String sessionType, double durationHours, int totalRecords, int presentCount, int absentCount) {
            this.sessionId = sessionId;
            this.sessionNumber = sessionNumber;
            this.sessionDate = sessionDate;
            this.sessionType = sessionType;
            this.durationHours = durationHours;
            this.totalRecords = totalRecords;
            this.presentCount = presentCount;
            this.absentCount = absentCount;
        }

        public int getSessionId() { return sessionId; }
        public int getSessionNumber() { return sessionNumber; }
        public LocalDate getSessionDate() { return sessionDate; }
        public String getSessionType() { return sessionType; }
        public double getDurationHours() { return durationHours; }
        public int getTotalRecords() { return totalRecords; }
        public int getPresentCount() { return presentCount; }
        public int getAbsentCount() { return absentCount; }

        @Override
        public String toString() {
            if (sessionId <= 0) {
                return "+ Start New Session";
            }
            return String.format("Session #%d - %s (%s, %.1f hrs) [%d students]",
                    sessionNumber, sessionDate != null ? sessionDate.toString() : "", sessionType, durationHours, totalRecords);
        }
    }

    public static class PastSessionRecord {
        public int sessionId;
        public int courseId;
        public String courseCode;
        public String courseName;
        public int academicYear;
        public int sessionNumber;
        public LocalDate sessionDate;
        public String sessionType;
        public double durationHours;
        public int totalMarked;
        public int presentCount;
        public int absentCount;
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
