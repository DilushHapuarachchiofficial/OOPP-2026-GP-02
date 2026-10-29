package main.java.ui.TechnicalOfficer;

import main.java.dao.TechnicalOfficer.TechnicalOfficerDAO;
import main.java.model.TechnicalOfficer.TechnicalOfficer;
import main.java.model.Timetable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import static main.java.ui.TechnicalOfficer.TechnicalOfficerDashboard.*;

/**
 * DepartmentTimetableForm - Technical Officer Module for TecFAMS.
 * Faculty of Technology - University of Ruhuna.
 *
 * Allows Technical Officers to view weekly lecture timetables, computer laboratory
 * allocations, and scheduled timeslots for academic staff and students.
 */
public class DepartmentTimetableForm extends JFrame {

    private final JFrame parentDashboard;
    private final TechnicalOfficer currentOfficer;
    private final TechnicalOfficerDAO dao;

    // Filter controls
    private JComboBox<String> cmbDepartment;
    private JComboBox<String> cmbAcademicYear;
    private JComboBox<String> cmbSemester;
    private JComboBox<String> cmbDayOfWeek;
    private JComboBox<String> cmbSessionType;
    private JTextField txtSearch;
    private JButton btnFilter;
    private JButton btnReset;
    private JButton btnRefresh;
    private JButton btnBack;

    // Table controls
    private JTable tblTimetable;
    private DefaultTableModel tableModel;
    private JLabel lblTableStatus;

    // Summary Metric Labels
    private JLabel lblTotalSessions;
    private JLabel lblPracticalSessions;
    private JLabel lblTheorySessions;
    private JLabel lblTotalVenues;

    // Selected Slot Details Panel
    private JLabel lblDetailCourse;
    private JLabel lblDetailDayTime;
    private JLabel lblDetailHall;
    private JLabel lblDetailType;
    private JLabel lblDetailDept;

    public DepartmentTimetableForm() {
        this(null, new TechnicalOfficer());
    }

    public DepartmentTimetableForm(JFrame parentDashboard) {
        this(parentDashboard, new TechnicalOfficer());
    }

    public DepartmentTimetableForm(JFrame parentDashboard, TechnicalOfficer officer) {
        this.parentDashboard = parentDashboard;
        this.currentOfficer = officer != null ? officer : new TechnicalOfficer();
        this.dao = new TechnicalOfficerDAO();

        initFrameSettings();
        buildUI();
        loadInitialData();
    }

    private void initFrameSettings() {
        setTitle("TecFAMS - Department Timetable");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1240, 780);
        setMinimumSize(new Dimension(1020, 660));
        setLocationRelativeTo(parentDashboard);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (parentDashboard != null) {
                    parentDashboard.setVisible(true);
                    parentDashboard.toFront();
                }
            }
        });
    }

    private void buildUI() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);

        // 1. Header Bar
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Content
        JPanel centerContainer = new JPanel(new BorderLayout(0, 14));
        centerContainer.setBackground(BG_COLOR);
        centerContainer.setBorder(new EmptyBorder(16, 24, 16, 24));

        // Top Section: Metric cards & Filter card
        JPanel topStrip = new JPanel();
        topStrip.setLayout(new BoxLayout(topStrip, BoxLayout.Y_AXIS));
        topStrip.setOpaque(false);
        topStrip.add(createSummaryStatsRow());
        topStrip.add(Box.createVerticalStrut(14));
        topStrip.add(createFilterCard());

        centerContainer.add(topStrip, BorderLayout.NORTH);
        centerContainer.add(createMainScheduleSection(), BorderLayout.CENTER);
        centerContainer.add(createBottomBar(), BorderLayout.SOUTH);

        add(centerContainer, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(12, 24, 12, 24)
        ));

        // Left Branding & Title
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        JLabel titleLabel = new JLabel("TecFAMS  •  Department Timetable");
        titleLabel.setFont(FONT_HEADER_LARGE);
        titleLabel.setForeground(PRIMARY_NAVY);

        JLabel subLabel = new JLabel("Faculty of Technology • University of Ruhuna | Technical Officer Schedule & Lab Allocation");
        subLabel.setFont(FONT_SMALL);
        subLabel.setForeground(TEXT_SECONDARY);
        left.add(titleLabel);
        left.add(subLabel);

        // Right Officer Info & Back
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

    private JPanel createSummaryStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);

        lblTotalSessions = new JLabel("0");
        lblPracticalSessions = new JLabel("0");
        lblTheorySessions = new JLabel("0");
        lblTotalVenues = new JLabel("0");

        row.add(createMetricCard("Total Scheduled Sessions", lblTotalSessions, "Active academic slots", "📅", MAIN_BLUE));
        row.add(createMetricCard("Practical Lab Sessions", lblPracticalSessions, "Laboratory allocations", "🧪", SUCCESS_GREEN));
        row.add(createMetricCard("Theory Lectures", lblTheorySessions, "Lecture theatre classes", "📚", WARNING_AMBER));
        row.add(createMetricCard("Allocated Venues / Labs", lblTotalVenues, "Distinct halls & labs", "🏛️", PRIMARY_NAVY));

        return row;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, String subtitle, String icon, Color accent) {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_SMALL);
        titleLbl.setForeground(TEXT_SECONDARY);

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        top.add(titleLbl, BorderLayout.WEST);
        top.add(iconLbl, BorderLayout.EAST);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(TEXT_MAIN);
        valueLabel.setBorder(new EmptyBorder(6, 0, 2, 0));

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(FONT_SMALL);
        subLbl.setForeground(accent);

        card.add(top, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createFilterCard() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JLabel filterHeading = new JLabel("Filter Timetable Schedule");
        filterHeading.setFont(FONT_HEADER_SMALL);
        filterHeading.setForeground(PRIMARY_NAVY);
        card.add(filterHeading, BorderLayout.NORTH);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        controls.setOpaque(false);

        // 1. Department combo
        controls.add(createFieldLabel("Department:"));
        cmbDepartment = new JComboBox<>();
        cmbDepartment.setFont(FONT_REGULAR);
        cmbDepartment.setPreferredSize(new Dimension(220, 32));
        cmbDepartment.addItem("All Departments");
        List<String> depts = dao.getAllDepartmentNames();
        for (String d : depts) {
            cmbDepartment.addItem(d);
        }
        selectOfficerDepartment();
        controls.add(cmbDepartment);

        // 2. Academic Year / Level
        controls.add(createFieldLabel("Level:"));
        cmbAcademicYear = new JComboBox<>(new String[]{"All Levels", "Level 1", "Level 2", "Level 3", "Level 4"});
        cmbAcademicYear.setFont(FONT_REGULAR);
        cmbAcademicYear.setPreferredSize(new Dimension(100, 32));
        controls.add(cmbAcademicYear);

        // 3. Semester
        controls.add(createFieldLabel("Semester:"));
        cmbSemester = new JComboBox<>(new String[]{"All Semesters", "Semester 1", "Semester 2"});
        cmbSemester.setFont(FONT_REGULAR);
        cmbSemester.setPreferredSize(new Dimension(115, 32));
        controls.add(cmbSemester);

        // 4. Day of Week
        controls.add(createFieldLabel("Day:"));
        cmbDayOfWeek = new JComboBox<>(new String[]{"All Days", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"});
        cmbDayOfWeek.setFont(FONT_REGULAR);
        cmbDayOfWeek.setPreferredSize(new Dimension(105, 32));
        controls.add(cmbDayOfWeek);

        // 5. Session Type
        controls.add(createFieldLabel("Type:"));
        cmbSessionType = new JComboBox<>(new String[]{"All Types", "Practical", "Theory"});
        cmbSessionType.setFont(FONT_REGULAR);
        cmbSessionType.setPreferredSize(new Dimension(100, 32));
        controls.add(cmbSessionType);

        // 6. Search
        controls.add(createFieldLabel("Search:"));
        txtSearch = new JTextField();
        txtSearch.setFont(FONT_REGULAR);
        txtSearch.setPreferredSize(new Dimension(130, 32));
        txtSearch.putClientProperty("JTextField.placeholderText", "Course or hall...");
        controls.add(txtSearch);

        // Action buttons
        btnFilter = createPrimaryButton("Apply Filter");
        btnFilter.setPreferredSize(new Dimension(110, 32));
        btnFilter.addActionListener(e -> applyFilters());
        controls.add(btnFilter);

        btnReset = createSecondaryButton("Reset");
        btnReset.setPreferredSize(new Dimension(75, 32));
        btnReset.addActionListener(e -> resetFilters());
        controls.add(btnReset);

        card.add(controls, BorderLayout.CENTER);
        return card;
    }

    private void selectOfficerDepartment() {
        if (currentOfficer == null || currentOfficer.getDepartment() == null) return;
        String officerDept = currentOfficer.getDepartment().trim();
        for (int i = 0; i < cmbDepartment.getItemCount(); i++) {
            String item = cmbDepartment.getItemAt(i);
            if (item.equalsIgnoreCase(officerDept) || item.toLowerCase().contains(officerDept.toLowerCase()) || officerDept.toLowerCase().contains(item.toLowerCase())) {
                cmbDepartment.setSelectedIndex(i);
                return;
            }
        }
    }

    private JPanel createMainScheduleSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // Table Card
        JPanel tableCard = createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 8));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);

        JLabel tableTitle = new JLabel("Weekly Timetable Schedule Slots");
        tableTitle.setFont(FONT_HEADER_MEDIUM);
        tableTitle.setForeground(PRIMARY_NAVY);

        lblTableStatus = new JLabel("Loading schedule slots from database...");
        lblTableStatus.setFont(FONT_SMALL);
        lblTableStatus.setForeground(TEXT_SECONDARY);

        tableHeader.add(tableTitle, BorderLayout.WEST);
        tableHeader.add(lblTableStatus, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        String[] columns = {"#", "Day", "Time Slot", "Course Code", "Course Name", "Session Type", "Venue / Hall", "Level", "Semester", "Department"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tblTimetable = new JTable(tableModel);
        tblTimetable.setFont(FONT_REGULAR);
        tblTimetable.setRowHeight(34);
        tblTimetable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTimetable.setSelectionBackground(LIGHT_BLUE);
        tblTimetable.setSelectionForeground(TEXT_MAIN);
        tblTimetable.setGridColor(BORDER_COLOR);
        tblTimetable.setShowHorizontalLines(true);
        tblTimetable.setShowVerticalLines(true);

        // Column widths
        int[] widths = {36, 85, 95, 90, 200, 95, 130, 60, 65, 170};
        for (int i = 0; i < widths.length && i < tblTimetable.getColumnCount(); i++) {
            tblTimetable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JTableHeader th = tblTimetable.getTableHeader();
        th.setFont(FONT_MEDIUM);
        th.setBackground(Color.decode("#F1F5F9"));
        th.setForeground(PRIMARY_NAVY);
        th.setPreferredSize(new Dimension(0, 36));

        // Center alignments
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int col : new int[]{0, 1, 2, 3, 7, 8}) {
            tblTimetable.getColumnModel().getColumn(col).setCellRenderer(centerRenderer);
        }

        // Session Type badge renderer
        tblTimetable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, val, isSelected, hasFocus, row, col);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(FONT_MEDIUM);
                String type = val != null ? val.toString() : "";
                if ("Practical".equalsIgnoreCase(type)) {
                    label.setForeground(Color.decode("#047857"));
                    label.setText("🧪  Practical");
                } else if ("Theory".equalsIgnoreCase(type)) {
                    label.setForeground(Color.decode("#1D4ED8"));
                    label.setText("📚  Theory");
                }
                return label;
            }
        });

        tblTimetable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblTimetable.getSelectedRow();
                if (row >= 0) {
                    showRowDetails(row);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblTimetable);
        scroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        scroll.getViewport().setBackground(WHITE);
        tableCard.add(scroll, BorderLayout.CENTER);

        // Detail strip at bottom of table
        tableCard.add(createSlotDetailStrip(), BorderLayout.SOUTH);

        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSlotDetailStrip() {
        JPanel strip = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 6));
        strip.setBackground(Color.decode("#F8FAFC"));
        strip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(6, 12, 6, 12)
        ));

        lblDetailCourse = new JLabel("Select a session slot above to view details");
        lblDetailCourse.setFont(FONT_MEDIUM);
        lblDetailCourse.setForeground(PRIMARY_NAVY);

        lblDetailDayTime = new JLabel("");
        lblDetailDayTime.setFont(FONT_REGULAR);
        lblDetailDayTime.setForeground(TEXT_SECONDARY);

        lblDetailHall = new JLabel("");
        lblDetailHall.setFont(FONT_REGULAR);
        lblDetailHall.setForeground(TEXT_MAIN);

        lblDetailType = new JLabel("");
        lblDetailType.setFont(FONT_MEDIUM);

        lblDetailDept = new JLabel("");
        lblDetailDept.setFont(FONT_SMALL);
        lblDetailDept.setForeground(TEXT_SECONDARY);

        strip.add(lblDetailCourse);
        strip.add(lblDetailDayTime);
        strip.add(lblDetailHall);
        strip.add(lblDetailType);
        strip.add(lblDetailDept);

        return strip;
    }

    private void showRowDetails(int row) {
        String day = String.valueOf(tableModel.getValueAt(row, 1));
        String time = String.valueOf(tableModel.getValueAt(row, 2));
        String code = String.valueOf(tableModel.getValueAt(row, 3));
        String name = String.valueOf(tableModel.getValueAt(row, 4));
        String type = String.valueOf(tableModel.getValueAt(row, 5));
        String hall = String.valueOf(tableModel.getValueAt(row, 6));
        String dept = String.valueOf(tableModel.getValueAt(row, 9));

        lblDetailCourse.setText("Selected: " + code + " - " + name);
        lblDetailDayTime.setText("⏰ " + day + " (" + time + ")");
        lblDetailHall.setText("🏛️ " + hall);
        lblDetailType.setText(type.equalsIgnoreCase("Practical") ? "🧪 Practical Lab" : "📚 Theory Hall");
        lblDetailType.setForeground(type.equalsIgnoreCase("Practical") ? Color.decode("#047857") : Color.decode("#1D4ED8"));
        lblDetailDept.setText("• " + dept);
    }

    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);

        btnRefresh = createSecondaryButton("🔄 Refresh Timetable");
        btnRefresh.addActionListener(e -> loadInitialData());

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBtns.setOpaque(false);

        JButton backBottom = createPrimaryButton("← Back to Dashboard");
        backBottom.addActionListener(e -> backToDashboard());

        rightBtns.add(btnRefresh);
        rightBtns.add(backBottom);

        JLabel hint = new JLabel("💡 Data retrieved live from TecFAMS database ('timetables' & 'timetable_entries' tables)");
        hint.setFont(FONT_SMALL);
        hint.setForeground(TEXT_SECONDARY);

        bar.add(hint, BorderLayout.WEST);
        bar.add(rightBtns, BorderLayout.EAST);
        return bar;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_MEDIUM);
        label.setForeground(TEXT_MAIN);
        return label;
    }

    private void loadInitialData() {
        applyFilters();
    }

    private void applyFilters() {
        String dept = (String) cmbDepartment.getSelectedItem();
        String yearStr = (String) cmbAcademicYear.getSelectedItem();
        Integer year = null;
        if (yearStr != null && yearStr.contains("1")) year = 1;
        else if (yearStr != null && yearStr.contains("2")) year = 2;
        else if (yearStr != null && yearStr.contains("3")) year = 3;
        else if (yearStr != null && yearStr.contains("4")) year = 4;

        String semStr = (String) cmbSemester.getSelectedItem();
        Integer sem = null;
        if (semStr != null && semStr.contains("1")) sem = 1;
        else if (semStr != null && semStr.contains("2")) sem = 2;

        String day = (String) cmbDayOfWeek.getSelectedItem();
        String type = (String) cmbSessionType.getSelectedItem();
        String query = txtSearch.getText().trim();

        tableModel.setRowCount(0);
        List<Timetable> list = dao.getDepartmentTimetableEntries(dept, year, sem, day, type, query);

        int practicalCount = 0;
        int theoryCount = 0;
        java.util.Set<String> uniqueVenues = new java.util.HashSet<>();

        int idx = 1;
        for (Timetable t : list) {
            tableModel.addRow(new Object[]{
                    idx++,
                    t.getDayOfWeek(),
                    t.getTimeSlot(),
                    t.getCourseCode(),
                    t.getCourseName(),
                    t.getSessionType(),
                    t.getLocation(),
                    t.getAcademicYear() > 0 ? "Level " + t.getAcademicYear() : "-",
                    t.getSemester() > 0 ? "Sem " + t.getSemester() : "-",
                    t.getDepartment()
            });

            if ("Practical".equalsIgnoreCase(t.getSessionType())) practicalCount++;
            else if ("Theory".equalsIgnoreCase(t.getSessionType())) theoryCount++;

            if (t.getLocation() != null && !t.getLocation().trim().isEmpty()) {
                uniqueVenues.add(t.getLocation().trim());
            }
        }

        lblTotalSessions.setText(String.valueOf(list.size()));
        lblPracticalSessions.setText(String.valueOf(practicalCount));
        lblTheorySessions.setText(String.valueOf(theoryCount));
        lblTotalVenues.setText(String.valueOf(uniqueVenues.size()));

        lblTableStatus.setText(String.format("Showing %d timetable slots (%d Practical, %d Theory)",
                list.size(), practicalCount, theoryCount));

        if (list.isEmpty()) {
            lblDetailCourse.setText("No timetable records found matching selected filter criteria.");
            lblDetailDayTime.setText("");
            lblDetailHall.setText("");
            lblDetailType.setText("");
            lblDetailDept.setText("");
        } else {
            tblTimetable.setRowSelectionInterval(0, 0);
            showRowDetails(0);
        }
    }

    private void resetFilters() {
        selectOfficerDepartment();
        cmbAcademicYear.setSelectedIndex(0);
        cmbSemester.setSelectedIndex(0);
        cmbDayOfWeek.setSelectedIndex(0);
        cmbSessionType.setSelectedIndex(0);
        txtSearch.setText("");
        applyFilters();
    }

    private void backToDashboard() {
        dispose();
        if (parentDashboard != null) {
            parentDashboard.setVisible(true);
            parentDashboard.toFront();
        }
    }

    /**
     * Standalone main method for quick preview and verification.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            DepartmentTimetableForm form = new DepartmentTimetableForm();
            form.setVisible(true);
        });
    }
}
