package main.java.ui.Undergraduate;

import main.java.dao.Undergraduate.TimetableDAO;
import main.java.model.Undergraduate.Schedule;
import main.java.model.Undergraduate.SessionType;
import main.java.model.Undergraduate.Undergraduate;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * TecFAMS - Faculty of Technology Academic Management System
 * Undergraduate Timetable & Lecture Schedule UI.
 *
 * Displays the complete weekly lecture timetable for Level II - Semester I (L2.S1),
 * including lecture time slots, assigned lecture halls/labs, session types, and lecturers.
 *
 * Designed with simple, beginner-friendly standard Java Swing code.
 */
public class UndergraduateTimetableUI extends JPanel {

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
    private static final Color COLOR_PURPLE       = new Color(0x7C, 0x3A, 0xED); // #7C3AED

    // Data references
    private final Undergraduate loggedInStudent;
    private final UndergraduateDashboard parentDashboard;
    private final TimetableDAO timetableDAO;

    // State: Current Academic Period (Level II - Semester I)
    private final int currentYear = 2;
    private final int currentSem = 1;
    private String selectedDayFilter = "All Days";

    // Loaded data
    private List<Schedule> weeklySchedules = new ArrayList<>();

    // UI Components
    private JPanel summaryCardsRow;
    private JComboBox<String> dayFilterCombo;
    private JLabel tableTallyLabel;
    private JTable timetableTable;
    private DefaultTableModel tableModel;

    public UndergraduateTimetableUI(Undergraduate student, UndergraduateDashboard parentDashboard) {
        this.loggedInStudent = student;
        this.parentDashboard = parentDashboard;
        this.timetableDAO = new TimetableDAO();

        initializeUI();
        loadTimetableData();
    }

    public UndergraduateTimetableUI(Undergraduate student) {
        this(student, null);
    }

    public UndergraduateTimetableUI() {
        this(null, null);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);

        // Scrollable content wrapper
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(COLOR_BG);
        contentPanel.setBorder(new EmptyBorder(18, 24, 24, 24));

        // 1. Top Header Bar
        contentPanel.add(createHeaderBar());
        contentPanel.add(Box.createVerticalStrut(16));

        // 2. Summary Metric Cards Row
        summaryCardsRow = createSummaryCardsRow();
        contentPanel.add(summaryCardsRow);
        contentPanel.add(Box.createVerticalStrut(18));

        // 3. Main Timetable Panel (Filter + Table)
        contentPanel.add(createTimetableMainPanel());

        // Put in smooth scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(COLOR_BG);
        add(scrollPane, BorderLayout.CENTER);
    }

    // ==========================================
    // 1. HEADER SECTION
    // ==========================================
    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel leftBox = new JPanel(new GridLayout(2, 1, 0, 4));
        leftBox.setOpaque(false);

        JLabel titleLbl = new JLabel("Academic Timetable & Lecture Schedule");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(COLOR_PRIMARY_NAVY);

        String studentName = (loggedInStudent != null && loggedInStudent.getName() != null)
                ? loggedInStudent.getName() : "H.D.W.B Hapuarachchi";
        String regNo = (loggedInStudent != null && loggedInStudent.getRegistrationNo() != null)
                ? loggedInStudent.getRegistrationNo() : "TG/2024/2085";

        JLabel subtitleLbl = new JLabel("Student: " + studentName + "  •  Reg No: " + regNo +
                "  •  Batch: 2023/2024  •  Department of ICT  •  Level II - Semester I");
        subtitleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLbl.setForeground(COLOR_TEXT_MUTED);

        leftBox.add(titleLbl);
        leftBox.add(subtitleLbl);
        header.add(leftBox, BorderLayout.WEST);

        // Right Box: Back to Dashboard
        if (parentDashboard != null) {
            JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            rightBox.setOpaque(false);

            JButton backBtn = createActionButton("← Back to Dashboard", COLOR_LIGHT_BLUE, COLOR_MAIN_BLUE);
            backBtn.addActionListener(e -> parentDashboard.showDashboardView());
            rightBox.add(backBtn);
            header.add(rightBox, BorderLayout.EAST);
        }

        return header;
    }

    // ==========================================
    // 2. METRIC SUMMARY CARDS ROW
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

        // 1. Academic Period Card
        summaryCardsRow.add(createMetricCard(
                "CURRENT SEMESTER",
                "Level II - Sem I",
                "Active Period",
                "Academic Year 2026 • Dept of ICT",
                COLOR_MAIN_BLUE
        ));

        // 2. Scheduled Weekly Sessions Card
        int totalSessions = weeklySchedules.size();
        summaryCardsRow.add(createMetricCard(
                "WEEKLY SESSIONS",
                totalSessions + " Lectures",
                "Monday - Friday",
                totalSessions * 2 + " Total Lecture Hours / Week",
                COLOR_PURPLE
        ));

        // 3. Theory vs Practical Ratio Card
        int theoryCount = 0;
        int practicalCount = 0;
        for (Schedule s : weeklySchedules) {
            if (s.getSessionType() == SessionType.PRACTICAL) {
                practicalCount++;
            } else {
                theoryCount++;
            }
        }
        summaryCardsRow.add(createMetricCard(
                "SESSION BREAKDOWN",
                theoryCount + " Theory  •  " + practicalCount + " Lab",
                "Full Practical Coverage",
                "Computer Labs & Interactive Halls",
                COLOR_SUCCESS
        ));

        // 4. Today's Lectures Card
        String today = LocalDate.now().getDayOfWeek().toString(); // e.g. MONDAY
        String formattedToday = today.substring(0, 1).toUpperCase() + today.substring(1).toLowerCase();
        int todayCount = 0;
        for (Schedule s : weeklySchedules) {
            if (s.getDay() != null && s.getDay().equalsIgnoreCase(formattedToday)) {
                todayCount++;
            }
        }
        String todaySubtext = (todayCount > 0)
                ? todayCount + " lecture sessions today"
                : "No lectures today (Self-study / Weekend)";

        summaryCardsRow.add(createMetricCard(
                "TODAY'S SCHEDULE",
                formattedToday,
                todayCount + " Lectures",
                todaySubtext,
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

        JLabel valLbl = new JLabel(mainValue);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valLbl.setForeground(COLOR_TEXT_PRIMARY);
        card.add(valLbl, BorderLayout.CENTER);

        JLabel subLbl = new JLabel(subText);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLbl.setForeground(COLOR_TEXT_MUTED);
        card.add(subLbl, BorderLayout.SOUTH);

        return card;
    }

    // ==========================================
    // 3. MAIN TIMETABLE PANEL & TABLE
    // ==========================================
    private JPanel createTimetableMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(COLOR_WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        // Top Area: Filter toolbar and quick banner
        JPanel topArea = new JPanel();
        topArea.setLayout(new BoxLayout(topArea, BoxLayout.Y_AXIS));
        topArea.setOpaque(false);

        // Toolbar: Day Selector & Table Tally
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBox.setOpaque(false);

        JLabel filterLbl = new JLabel("Filter by Day:");
        filterLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterLbl.setForeground(COLOR_PRIMARY_NAVY);
        leftBox.add(filterLbl);

        String[] days = {"All Days (Weekly Overview)", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        dayFilterCombo = new JComboBox<>(days);
        dayFilterCombo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dayFilterCombo.setPreferredSize(new Dimension(240, 32));
        dayFilterCombo.addActionListener(e -> onDayFilterChanged());
        leftBox.add(dayFilterCombo);

        toolbar.add(leftBox, BorderLayout.WEST);

        tableTallyLabel = new JLabel("Showing all 12 lecture sessions (Level II - Semester I)");
        tableTallyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableTallyLabel.setForeground(COLOR_TEXT_MUTED);
        toolbar.add(tableTallyLabel, BorderLayout.EAST);

        topArea.add(toolbar);
        topArea.add(Box.createVerticalStrut(12));

        // Info Banner: Explains current semester & highlights Lecture Halls
        JPanel infoBanner = createInfoBanner();
        topArea.add(infoBanner);

        panel.add(topArea, BorderLayout.NORTH);

        // Timetable JTable
        String[] columns = {
                "#", "Day", "Lecture Time", "Course Code", "Course Title", "Lecture Hall / Venue", "Type", "Lecturer"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        timetableTable = new JTable(tableModel);
        timetableTable.setRowHeight(42);
        timetableTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        timetableTable.setSelectionBackground(COLOR_LIGHT_BLUE);
        timetableTable.setSelectionForeground(COLOR_TEXT_PRIMARY);
        timetableTable.setShowVerticalLines(false);
        timetableTable.setGridColor(new Color(0xF1, 0xF5, 0xF9));

        JTableHeader tableHeader = timetableTable.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableHeader.setBackground(new Color(0xFA, 0xFA, 0xFC));
        tableHeader.setForeground(COLOR_PRIMARY_NAVY);
        tableHeader.setPreferredSize(new Dimension(0, 38));
        tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

        // Column widths
        timetableTable.getColumnModel().getColumn(0).setPreferredWidth(45);   // #
        timetableTable.getColumnModel().getColumn(1).setPreferredWidth(100);  // Day
        timetableTable.getColumnModel().getColumn(2).setPreferredWidth(170);  // Time
        timetableTable.getColumnModel().getColumn(3).setPreferredWidth(110);  // Code
        timetableTable.getColumnModel().getColumn(4).setPreferredWidth(320);  // Title
        timetableTable.getColumnModel().getColumn(5).setPreferredWidth(160);  // Hall / Venue
        timetableTable.getColumnModel().getColumn(6).setPreferredWidth(110);  // Type
        timetableTable.getColumnModel().getColumn(7).setPreferredWidth(210);  // Lecturer

        // Custom Cell Renderers
        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);

        timetableTable.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        timetableTable.getColumnModel().getColumn(1).setCellRenderer(new DayBadgeCellRenderer());
        timetableTable.getColumnModel().getColumn(2).setCellRenderer(new TimeCellRenderer());
        timetableTable.getColumnModel().getColumn(3).setCellRenderer(new CourseCodeCellRenderer());
        timetableTable.getColumnModel().getColumn(5).setCellRenderer(new HallBadgeCellRenderer());
        timetableTable.getColumnModel().getColumn(6).setCellRenderer(new SessionTypeCellRenderer());

        JScrollPane tableScroll = new JScrollPane(timetableTable);
        tableScroll.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        tableScroll.getViewport().setBackground(COLOR_WHITE);
        panel.add(tableScroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createInfoBanner() {
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

        JLabel leftText = new JLabel("Level II - Semester I Weekly Academic Timetable (BICT Honours Degree)");
        leftText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        leftText.setForeground(COLOR_TEXT_PRIMARY);

        JLabel rightText = new JLabel("Official Faculty Timetable  •  Attendance minimum 80% mandatory");
        rightText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rightText.setForeground(COLOR_TEXT_MUTED);

        banner.add(leftText, BorderLayout.WEST);
        banner.add(rightText, BorderLayout.EAST);
        return banner;
    }

    // ==========================================
    // 4. DATA LOADING & FILTERING
    // ==========================================
    public void loadTimetableData() {
        try {
            // Fetch live timetable from database for current year (2) and semester (1)
            weeklySchedules = timetableDAO.getWeeklyTimetable(currentYear, currentSem);

            // Update Metric Cards
            updateSummaryCards();

            // Populate Table
            populateTable();

        } catch (Exception e) {
            System.err.println("Database Notice: Error loading timetable: " + e.getMessage());
        }
    }

    private void onDayFilterChanged() {
        int idx = dayFilterCombo.getSelectedIndex();
        switch (idx) {
            case 1: selectedDayFilter = "Monday"; break;
            case 2: selectedDayFilter = "Tuesday"; break;
            case 3: selectedDayFilter = "Wednesday"; break;
            case 4: selectedDayFilter = "Thursday"; break;
            case 5: selectedDayFilter = "Friday"; break;
            case 0:
            default:
                selectedDayFilter = "All Days";
                break;
        }
        populateTable();
    }

    private void populateTable() {
        tableModel.setRowCount(0);

        List<Schedule> displayedList = new ArrayList<>();
        if ("All Days".equalsIgnoreCase(selectedDayFilter)) {
            displayedList.addAll(weeklySchedules);
        } else {
            for (Schedule s : weeklySchedules) {
                if (s.getDay() != null && s.getDay().equalsIgnoreCase(selectedDayFilter)) {
                    displayedList.add(s);
                }
            }
        }

        int index = 1;
        for (Schedule s : displayedList) {
            String timeStr = s.getStartTime() + " - " + s.getEndTime();
            tableModel.addRow(new Object[]{
                    index++,
                    s.getDay(),
                    timeStr,
                    s.getCourseCode(),
                    s.getCourseName(),
                    s.getLocation(), // Hall / Lab
                    (s.getSessionType() != null) ? s.getSessionType().toString() : "THEORY",
                    s.getLecturerName()
            });
        }

        String filterDesc = "All Days".equalsIgnoreCase(selectedDayFilter)
                ? "all " + displayedList.size() + " lecture sessions"
                : displayedList.size() + " lectures for " + selectedDayFilter;
        tableTallyLabel.setText("Showing " + filterDesc + " (Level II - Semester I)");
    }

    // ==========================================
    // 5. CUSTOM TABLE CELL RENDERERS
    // ==========================================

    /**
     * Renders Day name in a clean colored badge.
     */
    private static class DayBadgeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

            String day = (value != null) ? value.toString() : "-";
            JLabel badge = new JLabel(" " + day + " ");
            badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
            badge.setOpaque(true);
            badge.setBackground(new Color(0xEA, 0xF2, 0xFF));
            badge.setForeground(COLOR_PRIMARY_NAVY);
            badge.setBorder(new EmptyBorder(3, 8, 3, 8));

            panel.add(badge);
            return panel;
        }
    }

    /**
     * Renders lecture time with clear emphasis.
     */
    private static class TimeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(new Color(0x33, 0x41, 0x55));
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            return lbl;
        }
    }

    /**
     * Renders Course Code in bold TecFAMS blue.
     */
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

    /**
     * Renders Lecture Hall / Venue in an eye-catching soft indigo badge so students
     * can immediately see where their class is located!
     */
    private static class HallBadgeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

            String hall = (value != null) ? value.toString() : "-";
            JLabel badge = new JLabel(" " + hall + " ");
            badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
            badge.setOpaque(true);

            // Distinctive soft violet/indigo badge for halls/labs
            badge.setBackground(new Color(0xEE, 0xF2, 0xFF));
            badge.setForeground(new Color(0x43, 0x38, 0xCA));
            badge.setBorder(new EmptyBorder(3, 10, 3, 10));

            panel.add(badge);
            return panel;
        }
    }

    /**
     * Renders Session Type badge: Theory (Blue) vs Practical (Green).
     */
    private static class SessionTypeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

            String type = (value != null) ? value.toString() : "THEORY";
            JLabel badge = new JLabel(" " + type + " ");
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setOpaque(true);

            if ("PRACTICAL".equalsIgnoreCase(type)) {
                badge.setBackground(new Color(0xDC, 0xFC, 0xE7)); // Soft Emerald
                badge.setForeground(new Color(0x16, 0x65, 0x34));
            } else {
                badge.setBackground(new Color(0xDB, 0xEA, 0xFE)); // Soft Blue
                badge.setForeground(new Color(0x1E, 0x40, 0xAF));
            }
            badge.setBorder(new EmptyBorder(3, 8, 3, 8));

            panel.add(badge);
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
    // 6. STANDALONE MAIN METHOD FOR TESTING
    // ==========================================
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            JFrame frame = new JFrame("TecFAMS - Academic Timetable & Lecture Schedule");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 780);
            frame.setLocationRelativeTo(null);

            UndergraduateTimetableUI timetableUI = new UndergraduateTimetableUI();
            frame.setContentPane(timetableUI);
            frame.setVisible(true);
        });
    }
}
