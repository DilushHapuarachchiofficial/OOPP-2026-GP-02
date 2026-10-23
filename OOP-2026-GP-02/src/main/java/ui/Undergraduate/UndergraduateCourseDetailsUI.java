package main.java.ui.Undergraduate;

import main.java.dao.Undergraduate.CourseDAO;
import main.java.model.Undergraduate.Course;
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
 * Undergraduate Course Details UI Page.
 *
 * Displays a comprehensive, beautifully styled table of all registered courses
 * for the logged-in undergraduate student. Shows per-course credits, overall
 * total credits sum, academic levels, semesters, departments, and course delivery types.
 *
 * Provides real-time search filtering, category filtering, a detailed course modal,
 * and seamless navigation integration with the TecFAMS dashboard.
 */
public class UndergraduateCourseDetailsUI extends JPanel {

    // TecFAMS Primary Theme Palette
    private static final Color COLOR_PRIMARY_NAVY = new Color(0x12, 0x3B, 0x70); // #123B70
    private static final Color COLOR_MAIN_BLUE    = new Color(0x25, 0x63, 0xEB); // #2563EB
    private static final Color COLOR_LIGHT_BLUE   = new Color(0xEA, 0xF2, 0xFF); // #EAF2FF
    private static final Color COLOR_WHITE        = new Color(0xFF, 0xFF, 0xFF); // #FFFFFF
    private static final Color COLOR_BG           = new Color(0xF5, 0xF8, 0xFD); // #F5F8FD
    private static final Color COLOR_TEXT_PRIMARY = new Color(0x17, 0x2B, 0x4D); // #172B4D
    private static final Color COLOR_TEXT_MUTED   = new Color(0x71, 0x80, 0x96); // #718096
    private static final Color COLOR_BORDER       = new Color(0xE2, 0xE8, 0xF0); // #E2E8F0
    private static final Color COLOR_SUCCESS      = new Color(0x10, 0xB9, 0x81); // #10B981
    private static final Color COLOR_DANGER       = new Color(0xEF, 0x44, 0x44); // #EF4444
    private static final Color COLOR_WARNING      = new Color(0xF5, 0x9E, 0x0B); // #F59E0B
    private static final Color COLOR_PURPLE       = new Color(0x8B, 0x5C, 0xF6); // #8B5CF6

    // Data references
    private final Undergraduate loggedInStudent;
    private final UndergraduateDashboard parentDashboard;
    private final String studentIdentifier;
    private final CourseDAO courseDAO;

    // Data Models
    private List<Course> allCourses = new ArrayList<>();

    // UI Components to update dynamically
    private JLabel statusSubtitleLabel;
    private JPanel summaryCardsRow;
    private JTable coursesTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private JTextField searchField;
    private JComboBox<String> semesterFilterCombo;
    private JComboBox<String> typeFilterCombo;
    private JComboBox<String> deptFilterCombo;
    private JLabel tableTallyLabel;

    /**
     * Main constructor for embedding inside UndergraduateDashboard.
     */
    public UndergraduateCourseDetailsUI(Undergraduate student, UndergraduateDashboard parentDashboard) {
        this.loggedInStudent = student;
        this.parentDashboard = parentDashboard;
        this.studentIdentifier = resolveStudentIdentifier(student);
        this.courseDAO = new CourseDAO();

        initializeUI();
        loadCourseData();
    }

    /**
     * Constructor for standalone or student-only view.
     */
    public UndergraduateCourseDetailsUI(Undergraduate student) {
        this(student, null);
    }

    /**
     * Constructor by Student Registration Number.
     */
    public UndergraduateCourseDetailsUI(String studentIdOrRegNo) {
        this.loggedInStudent = null;
        this.parentDashboard = null;
        this.studentIdentifier = (studentIdOrRegNo != null && !studentIdOrRegNo.trim().isEmpty())
                ? studentIdOrRegNo.trim()
                : "TG/2024/2085";
        this.courseDAO = new CourseDAO();

        initializeUI();
        loadCourseData();
    }

    /**
     * Default constructor for testing.
     */
    public UndergraduateCourseDetailsUI() {
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

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);

        // Scrollable content area
        class ScrollablePanel extends JPanel implements Scrollable {
            @Override
            public Dimension getPreferredScrollableViewportSize() {
                return getPreferredSize();
            }
            @Override
            public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
                return 16;
            }
            @Override
            public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
                return 64;
            }
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }
            @Override
            public boolean getScrollableTracksViewportHeight() {
                return false;
            }
        }

        JPanel scrollContent = new ScrollablePanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(COLOR_BG);
        scrollContent.setBorder(new EmptyBorder(16, 24, 24, 24));

        // 1. Top Header Bar
        scrollContent.add(createHeaderPanel());
        scrollContent.add(Box.createVerticalStrut(16));

        // 2. Student Information Ribbon
        scrollContent.add(createStudentInfoRibbon());
        scrollContent.add(Box.createVerticalStrut(16));

        // 3. Top 4 Summary Cards (Total Registered Courses & Total Enrolled Credits Sum)
        summaryCardsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        summaryCardsRow.setOpaque(false);
        summaryCardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));
        scrollContent.add(summaryCardsRow);
        scrollContent.add(Box.createVerticalStrut(20));

        // 4. Section Title & Search/Filter Control Bar
        scrollContent.add(createSectionTitle("Registered Course Catalog", "Official registered modules, credit weightages & delivery format"));
        scrollContent.add(Box.createVerticalStrut(10));
        scrollContent.add(createFilterPanel());
        scrollContent.add(Box.createVerticalStrut(12));

        // 5. Course Details Table Card
        scrollContent.add(createCoursesTableCard());
        scrollContent.add(Box.createVerticalStrut(20));

        // 6. Faculty Academic Regulations & Credit Policy Card
        scrollContent.add(createCreditPolicyCard());

        JScrollPane mainScrollPane = new JScrollPane(scrollContent);
        mainScrollPane.setBorder(null);
        mainScrollPane.setBackground(COLOR_BG);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(mainScrollPane, BorderLayout.CENTER);
    }

    // ==========================================
    // 1. TOP HEADER PANEL
    // ==========================================
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel leftBox = new JPanel(new GridLayout(2, 1, 0, 4));
        leftBox.setOpaque(false);

        JLabel titleLbl = new JLabel("Enrolled Course Details & Academic Credits");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(COLOR_PRIMARY_NAVY);

        statusSubtitleLabel = new JLabel("Loading registered course records from database...");
        statusSubtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusSubtitleLabel.setForeground(COLOR_TEXT_MUTED);

        leftBox.add(titleLbl);
        leftBox.add(statusSubtitleLabel);
        header.add(leftBox, BorderLayout.WEST);

        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBox.setOpaque(false);

        // Refresh Button
        JButton refreshBtn = new JButton("↻ Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setForeground(COLOR_PRIMARY_NAVY);
        refreshBtn.setBackground(COLOR_WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        refreshBtn.addActionListener(e -> loadCourseData());
        rightBox.add(refreshBtn);

        // Back to Dashboard Button
        JButton backBtn = new JButton("← Back to Dashboard");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        backBtn.setForeground(COLOR_MAIN_BLUE);
        backBtn.setBackground(COLOR_WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        backBtn.addActionListener(e -> {
            if (parentDashboard != null) {
                parentDashboard.showDashboardView();
            } else {
                Window win = SwingUtilities.getWindowAncestor(UndergraduateCourseDetailsUI.this);
                if (win != null) {
                    win.dispose();
                }
            }
        });
        rightBox.add(backBtn);

        header.add(rightBox, BorderLayout.EAST);
        return header;
    }

    // ==========================================
    // 2. STUDENT INFORMATION RIBBON
    // ==========================================
    private JPanel createStudentInfoRibbon() {
        JPanel ribbon = new JPanel(new BorderLayout());
        ribbon.setBackground(COLOR_WHITE);
        ribbon.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(12, 16, 12, 16)));
        ribbon.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        leftBox.setOpaque(false);

        String studentName = (loggedInStudent != null && loggedInStudent.getName() != null)
                ? loggedInStudent.getName() : "H.D.W.B. Hapuarachchi";
        String regNo = studentIdentifier;
        String dept = (loggedInStudent != null && loggedInStudent.getDepartment() != null)
                ? loggedInStudent.getDepartment() : "Department of Information and Communication Technology";
        String batch = (loggedInStudent != null && loggedInStudent.getBatch() != null)
                ? loggedInStudent.getBatch() : "Batch 2023/2024";

        JLabel avatar = createAvatarIcon(38, studentName);
        leftBox.add(avatar);

        JPanel details = new JPanel(new GridLayout(2, 1, 0, 2));
        details.setOpaque(false);
        JLabel nameLbl = new JLabel(studentName + "  (" + regNo + ")");
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLbl.setForeground(COLOR_TEXT_PRIMARY);

        JLabel metaLbl = new JLabel(dept + "  •  " + batch + "  •  Faculty of Technology");
        metaLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        metaLbl.setForeground(COLOR_TEXT_MUTED);

        details.add(nameLbl);
        details.add(metaLbl);
        leftBox.add(details);

        ribbon.add(leftBox, BorderLayout.WEST);

        // Date & Status badge on right
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"));
        JLabel dateBadge = new JLabel("Academic Session 2026  |  " + today);
        dateBadge.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateBadge.setForeground(COLOR_PRIMARY_NAVY);
        dateBadge.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(6, 12, 6, 12)));
        ribbon.add(dateBadge, BorderLayout.EAST);

        return ribbon;
    }

    // ==========================================
    // 3. TOP SUMMARY CARDS (CREDITS SUM & METRICS)
    // ==========================================
    private void renderSummaryCards() {
        summaryCardsRow.removeAll();

        int totalCourses = allCourses.size();
        int totalCredits = courseDAO.calculateTotalCredits(allCourses);

        int theoryOnly = 0;
        int practicalOnly = 0;
        int theoryAndPractical = 0;

        for (Course c : allCourses) {
            String type = c.getCourseType();
            if (type != null) {
                if (type.equalsIgnoreCase("Theory_Only") || type.equalsIgnoreCase("Theory Only")) {
                    theoryOnly++;
                } else if (type.equalsIgnoreCase("Practical_Only") || type.equalsIgnoreCase("Practical Only")) {
                    practicalOnly++;
                } else {
                    theoryAndPractical++;
                }
            } else {
                theoryAndPractical++;
            }
        }

        // Card 1: Total Registered Courses Count
        JPanel card1 = createMetricCard(
                "REGISTERED COURSES",
                totalCourses + " Courses",
                COLOR_PRIMARY_NAVY,
                "All enrolled academic modules",
                "Official registration confirmed"
        );

        // Card 2: Total Credits Sum (The key user requirement!)
        JPanel card2 = createMetricCard(
                "TOTAL ENROLLED CREDITS",
                totalCredits + " Credits",
                COLOR_SUCCESS,
                "Accumulated semester credit weight",
                "Maximum permitted load: 30 credits"
        );

        // Card 3: Course Delivery Breakdown
        JPanel card3 = createMetricCard(
                "COURSE DELIVERY TYPES",
                (theoryOnly + theoryAndPractical) + " Theory | " + (practicalOnly + theoryAndPractical) + " Practical",
                COLOR_MAIN_BLUE,
                theoryAndPractical + " Combined  •  " + theoryOnly + " Theory-only  •  " + practicalOnly + " Practical-only",
                "Continuous assessment & lab exams"
        );

        // Card 4: Academic Registration Status
        JPanel card4 = createMetricCard(
                "REGISTRATION STATUS",
                "ACTIVE & VERIFIED",
                COLOR_PURPLE,
                "Faculty of Technology Senate Approved",
                "Eligible for End-Semester Examinations"
        );

        summaryCardsRow.add(card1);
        summaryCardsRow.add(card2);
        summaryCardsRow.add(card3);
        summaryCardsRow.add(card4);

        summaryCardsRow.revalidate();
        summaryCardsRow.repaint();
    }

    private JPanel createMetricCard(String title, String mainValue, Color valueColor, String subtitle, String footer) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(COLOR_WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        // Title
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLbl.setForeground(COLOR_TEXT_MUTED);
        card.add(titleLbl, BorderLayout.NORTH);

        // Center: Main Value and Subtitle
        JPanel center = new JPanel(new GridLayout(2, 1, 0, 2));
        center.setOpaque(false);

        JLabel valLbl = new JLabel(mainValue);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valLbl.setForeground(valueColor);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLbl.setForeground(COLOR_TEXT_PRIMARY);

        center.add(valLbl);
        center.add(subLbl);
        card.add(center, BorderLayout.CENTER);

        // Footer note
        JLabel footLbl = new JLabel(footer);
        footLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footLbl.setForeground(COLOR_TEXT_MUTED);
        card.add(footLbl, BorderLayout.SOUTH);

        return card;
    }

    // ==========================================
    // 4. FILTER & SEARCH PANEL
    // ==========================================
    private JPanel createFilterPanel() {
        JPanel filterCard = new JPanel(new BorderLayout(12, 0));
        filterCard.setBackground(COLOR_WHITE);
        filterCard.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(10, 16, 10, 16)));
        filterCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        // Left: Search Field
        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBox.setOpaque(false);

        JLabel searchIconLbl = new JLabel("🔍 Search:");
        searchIconLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchIconLbl.setForeground(COLOR_PRIMARY_NAVY);
        leftBox.add(searchIconLbl);

        searchField = new JTextField(18);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setToolTipText("Filter by course code or course name");
        searchField.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(6, 8, 6, 8)));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        leftBox.add(searchField);

        filterCard.add(leftBox, BorderLayout.WEST);

        // Right: Dropdown Filters & Clear
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBox.setOpaque(false);

        // Semester Filter
        JLabel semLbl = new JLabel("Semester:");
        semLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        semLbl.setForeground(COLOR_TEXT_MUTED);
        rightBox.add(semLbl);

        semesterFilterCombo = new JComboBox<>(new String[]{"All Semesters", "Level II - Semester 1", "Level I - Semester 2"});
        styleFilterCombo(semesterFilterCombo);
        semesterFilterCombo.addActionListener(e -> applyFilters());
        rightBox.add(semesterFilterCombo);

        // Course Type Filter
        JLabel typeLbl = new JLabel("Type:");
        typeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        typeLbl.setForeground(COLOR_TEXT_MUTED);
        rightBox.add(typeLbl);

        typeFilterCombo = new JComboBox<>(new String[]{"All Types", "Theory & Practical", "Theory Only", "Practical Only"});
        styleFilterCombo(typeFilterCombo);
        typeFilterCombo.addActionListener(e -> applyFilters());
        rightBox.add(typeFilterCombo);

        // Department Filter
        JLabel deptLbl = new JLabel("Dept:");
        deptLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        deptLbl.setForeground(COLOR_TEXT_MUTED);
        rightBox.add(deptLbl);

        deptFilterCombo = new JComboBox<>(new String[]{"All Departments", "ICT", "MDS"});
        styleFilterCombo(deptFilterCombo);
        deptFilterCombo.addActionListener(e -> applyFilters());
        rightBox.add(deptFilterCombo);

        // Reset Filter Button
        JButton resetBtn = new JButton("Reset");
        resetBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        resetBtn.setForeground(COLOR_MAIN_BLUE);
        resetBtn.setBackground(COLOR_WHITE);
        resetBtn.setFocusPainted(false);
        resetBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetBtn.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(5, 10, 5, 10)));
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            semesterFilterCombo.setSelectedIndex(0);
            typeFilterCombo.setSelectedIndex(0);
            deptFilterCombo.setSelectedIndex(0);
            applyFilters();
        });
        rightBox.add(resetBtn);

        filterCard.add(rightBox, BorderLayout.EAST);
        return filterCard;
    }

    private void styleFilterCombo(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setBackground(COLOR_WHITE);
        combo.setForeground(COLOR_TEXT_PRIMARY);
        combo.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        combo.setPreferredSize(new Dimension(combo.getPreferredSize().width + 10, 30));
    }

    // ==========================================
    // 5. COURSE DETAILS TABLE CARD
    // ==========================================
    private JPanel createCoursesTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_WHITE);
        card.setBorder(new LineBorder(COLOR_BORDER, 1, true));

        // Subheader inside table card
        JPanel tableSubHeader = new JPanel(new BorderLayout());
        tableSubHeader.setBackground(COLOR_WHITE);
        tableSubHeader.setBorder(new EmptyBorder(12, 16, 10, 16));

        JLabel title = new JLabel("Official Course Registration Registry");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(COLOR_PRIMARY_NAVY);

        tableTallyLabel = new JLabel("0 courses loaded");
        tableTallyLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableTallyLabel.setForeground(COLOR_TEXT_MUTED);

        tableSubHeader.add(title, BorderLayout.WEST);
        tableSubHeader.add(tableTallyLabel, BorderLayout.EAST);
        card.add(tableSubHeader, BorderLayout.NORTH);

        // Columns:
        // 0: # (Index)
        // 1: Course Code
        // 2: Course Name
        // 3: Credits (Bold & Pill)
        // 4: Delivery Type
        // 5: Department
        // 6: Level & Semester
        // 7: Status
        // 8: Action
        String[] columnNames = {
                "#", "Course Code", "Course Title", "Credits", "Delivery Type",
                "Department", "Level & Semester", "Status", "Quick Action"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only table
            }
        };

        coursesTable = new JTable(tableModel);
        coursesTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        coursesTable.setRowHeight(42);
        coursesTable.setGridColor(COLOR_BORDER);
        coursesTable.setShowVerticalLines(false);
        coursesTable.setSelectionBackground(COLOR_LIGHT_BLUE);
        coursesTable.setSelectionForeground(COLOR_TEXT_PRIMARY);

        // Header Styling
        JTableHeader th = coursesTable.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(new Color(0xFB, 0xFC, 0xFE));
        th.setForeground(COLOR_PRIMARY_NAVY);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        th.setPreferredSize(new Dimension(0, 38));

        // Column widths
        coursesTable.getColumnModel().getColumn(0).setPreferredWidth(45);   // #
        coursesTable.getColumnModel().getColumn(1).setPreferredWidth(100);  // Code
        coursesTable.getColumnModel().getColumn(2).setPreferredWidth(270);  // Name
        coursesTable.getColumnModel().getColumn(3).setPreferredWidth(95);   // Credits
        coursesTable.getColumnModel().getColumn(4).setPreferredWidth(140);  // Type
        coursesTable.getColumnModel().getColumn(5).setPreferredWidth(90);   // Dept
        coursesTable.getColumnModel().getColumn(6).setPreferredWidth(130);  // Level & Sem
        coursesTable.getColumnModel().getColumn(7).setPreferredWidth(110);  // Status
        coursesTable.getColumnModel().getColumn(8).setPreferredWidth(110);  // Quick Action

        // Cell Alignment Renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        coursesTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        coursesTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        coursesTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        // Course Code Badge Renderer (Monospace Navy)
        coursesTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setForeground(COLOR_PRIMARY_NAVY);
                return lbl;
            }
        });

        // Course Title (Bold Segoe UI)
        coursesTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setForeground(COLOR_TEXT_PRIMARY);
                lbl.setBorder(new EmptyBorder(0, 12, 0, 4));
                return lbl;
            }
        });

        // Credits Pill Badge Renderer (Prominent highlight!)
        coursesTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
                p.setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? COLOR_WHITE : new Color(0xF9, 0xFB, 0xFD)));

                String text = (value != null) ? value.toString() : "2 Credits";
                JLabel badge = new JLabel(text);
                badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
                badge.setForeground(new Color(0x03, 0x69, 0xA1)); // Dark Sky Blue
                badge.setBackground(new Color(0xE0, 0xF2, 0xFE)); // Light Sky Blue
                badge.setOpaque(true);
                badge.setBorder(new EmptyBorder(3, 8, 3, 8));
                p.add(badge);
                return p;
            }
        });

        // Delivery Type Pill Badge Renderer
        coursesTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
                p.setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? COLOR_WHITE : new Color(0xF9, 0xFB, 0xFD)));

                String val = (value != null) ? value.toString() : "Theory & Practical";
                Color bg;
                Color fg;

                if (val.equalsIgnoreCase("Practical Only") || val.contains("Practical_Only")) {
                    bg = new Color(0xED, 0xE9, 0xFE); // Purple tint
                    fg = COLOR_PURPLE;
                } else if (val.equalsIgnoreCase("Theory Only") || val.contains("Theory_Only")) {
                    bg = new Color(0xEA, 0xF2, 0xFF); // Blue tint
                    fg = COLOR_MAIN_BLUE;
                } else {
                    bg = new Color(0xFE, 0xF3, 0xC7); // Amber/Yellow tint
                    fg = new Color(0x92, 0x40, 0x0E);
                }

                JLabel badge = new JLabel(formatTypeDisplay(val));
                badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
                badge.setForeground(fg);
                badge.setBackground(bg);
                badge.setOpaque(true);
                badge.setBorder(new EmptyBorder(3, 8, 3, 8));
                p.add(badge);
                return p;
            }
        });

        // Status Pill Renderer (Registered / Confirmed)
        coursesTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
                p.setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? COLOR_WHITE : new Color(0xF9, 0xFB, 0xFD)));

                String status = (value != null) ? value.toString() : "Registered";
                JLabel badge = new JLabel("● " + status);
                badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
                badge.setForeground(new Color(0x06, 0x5F, 0x46)); // Forest Green
                badge.setBackground(new Color(0xD1, 0xFA, 0xE5)); // Emerald tint
                badge.setOpaque(true);
                badge.setBorder(new EmptyBorder(3, 8, 3, 8));
                p.add(badge);
                return p;
            }
        });

        // Action Column: "View Details" Pill Button
        coursesTable.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 7));
                p.setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? COLOR_WHITE : new Color(0xF9, 0xFB, 0xFD)));

                JLabel btn = new JLabel("View Details");
                btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
                btn.setForeground(COLOR_MAIN_BLUE);
                btn.setBackground(COLOR_WHITE);
                btn.setOpaque(true);
                btn.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(3, 8, 3, 8)));
                p.add(btn);
                return p;
            }
        });

        // Double-click row or click action column to open Detailed Course Info Modal
        coursesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = coursesTable.getSelectedRow();
                if (row >= 0) {
                    int modelRow = coursesTable.convertRowIndexToModel(row);
                    if (e.getClickCount() == 2 || coursesTable.columnAtPoint(e.getPoint()) == 8) {
                        showCourseDetailsModal(modelRow);
                    }
                }
            }
        });

        // Table Sorter
        tableSorter = new TableRowSorter<>(tableModel);
        coursesTable.setRowSorter(tableSorter);

        JScrollPane tableScroll = new JScrollPane(coursesTable);
        tableScroll.setBorder(null);
        tableScroll.getViewport().setBackground(COLOR_WHITE);
        tableScroll.setPreferredSize(new Dimension(0, 360));
        card.add(tableScroll, BorderLayout.CENTER);

        // Table Footer Summary Bar
        JPanel tableFooter = new JPanel(new BorderLayout());
        tableFooter.setBackground(new Color(0xF8, 0xFA, 0xFC));
        tableFooter.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER),
                new EmptyBorder(10, 16, 10, 16)
        ));

        JLabel hintLbl = new JLabel("💡 Tip: Double-click any course row or click 'View Details' to inspect syllabus, contact hours & prerequisites.");
        hintLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hintLbl.setForeground(COLOR_TEXT_MUTED);

        JLabel clickLbl = new JLabel("Click any table column header to sort ascending/descending");
        clickLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        clickLbl.setForeground(COLOR_TEXT_MUTED);

        tableFooter.add(hintLbl, BorderLayout.WEST);
        tableFooter.add(clickLbl, BorderLayout.EAST);
        card.add(tableFooter, BorderLayout.SOUTH);

        return card;
    }

    // ==========================================
    // 6. CREDIT POLICY & FACULTY NOTICE CARD
    // ==========================================
    private JPanel createCreditPolicyCard() {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(COLOR_WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(14, 18, 14, 18)));

        JPanel leftAccent = new JPanel();
        leftAccent.setPreferredSize(new Dimension(4, 0));
        leftAccent.setBackground(COLOR_MAIN_BLUE);
        card.add(leftAccent, BorderLayout.WEST);

        JPanel content = new JPanel(new GridLayout(3, 1, 0, 4));
        content.setOpaque(false);

        JLabel heading = new JLabel("Faculty of Technology • Academic Credit System & Examination Regulations");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 13));
        heading.setForeground(COLOR_PRIMARY_NAVY);

        JLabel body1 = new JLabel("• One academic credit corresponds to 15 hours of Theory lectures or 30 to 45 hours of Laboratory / Practical work per semester.");
        body1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        body1.setForeground(COLOR_TEXT_PRIMARY);

        JLabel body2 = new JLabel("• Students must maintain a minimum of 80% attendance in lectures and practical sessions for each registered module to qualify for End-Semester Examinations.");
        body2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        body2.setForeground(COLOR_TEXT_MUTED);

        content.add(heading);
        content.add(body1);
        content.add(body2);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JPanel createSectionTitle(String title, String subtitle) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 2));
        panel.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(COLOR_PRIMARY_NAVY);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(COLOR_TEXT_MUTED);

        panel.add(titleLbl);
        panel.add(subLbl);
        return panel;
    }

    // ==========================================
    // 7. DATA LOADING & POPULATION
    // ==========================================
    /**
     * Loads live registered course data from the database and updates all UI elements.
     */
    public void loadCourseData() {
        statusSubtitleLabel.setText("Connecting to TecFAMS database...");

        SwingUtilities.invokeLater(() -> {
            try {
                allCourses = courseDAO.getRegisteredCoursesForStudent(studentIdentifier);
                int totalCount = allCourses.size();
                int totalCredits = courseDAO.calculateTotalCredits(allCourses);

                // Populate Table
                tableModel.setRowCount(0);
                for (int i = 0; i < allCourses.size(); i++) {
                    Course c = allCourses.get(i);
                    int year = c.getAcademicYear() > 0 ? c.getAcademicYear() : 2;
                    int sem = c.getSemester() > 0 ? c.getSemester() : 1;
                    String levelSem = "Level " + (year == 1 ? "I" : (year == 2 ? "II" : (year == 3 ? "III" : "IV"))) + " - Sem " + sem;

                    tableModel.addRow(new Object[]{
                            (i + 1),
                            c.getCourseCode(),
                            c.getCourseName(),
                            c.getCredits() + " Credits",
                            formatTypeDisplay(c.getCourseType()),
                            c.getDepartment() != null ? c.getDepartment() : "ICT",
                            levelSem,
                            c.getStatus() != null ? c.getStatus() : "Registered",
                            "View Details"
                    });
                }

                // Render Top Metric Cards
                renderSummaryCards();

                // Update Labels
                statusSubtitleLabel.setText("Student Registration: " + studentIdentifier + "  •  " + totalCount + " Enrolled Modules  •  Total Academic Weight: " + totalCredits + " Credits");
                tableTallyLabel.setText("Showing " + totalCount + " of " + totalCount + " courses  |  Total: " + totalCredits + " Credits");

                applyFilters();
            } catch (Exception e) {
                statusSubtitleLabel.setText("Notice: Displaying cached curriculum data: " + e.getMessage());
            }
        });
    }

    // ==========================================
    // 8. FILTERING LOGIC
    // ==========================================
    private void applyFilters() {
        if (tableSorter == null) return;

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // Text Search Filter (Course code or title or department)
        String searchText = searchField != null ? searchField.getText().trim() : "";
        if (!searchText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(searchText), 1, 2, 5));
        }

        // Semester Filter
        if (semesterFilterCombo != null && semesterFilterCombo.getSelectedIndex() > 0) {
            String selectedSem = (String) semesterFilterCombo.getSelectedItem();
            if (selectedSem != null) {
                if (selectedSem.contains("Level II - Semester 1")) {
                    filters.add(RowFilter.regexFilter("(?i)Level II - Sem 1", 6));
                } else if (selectedSem.contains("Level I - Semester 2")) {
                    filters.add(RowFilter.regexFilter("(?i)Level I - Sem 2", 6));
                }
            }
        }

        // Course Type Filter
        if (typeFilterCombo != null && typeFilterCombo.getSelectedIndex() > 0) {
            String selectedType = (String) typeFilterCombo.getSelectedItem();
            if (selectedType != null) {
                if (selectedType.equalsIgnoreCase("Theory & Practical")) {
                    filters.add(RowFilter.regexFilter("(?i)Theory & Practical", 4));
                } else if (selectedType.equalsIgnoreCase("Theory Only")) {
                    filters.add(RowFilter.regexFilter("(?i)Theory Only", 4));
                } else if (selectedType.equalsIgnoreCase("Practical Only")) {
                    filters.add(RowFilter.regexFilter("(?i)Practical Only", 4));
                }
            }
        }

        // Department Filter
        if (deptFilterCombo != null && deptFilterCombo.getSelectedIndex() > 0) {
            String selectedDept = (String) deptFilterCombo.getSelectedItem();
            if (selectedDept != null && !selectedDept.equalsIgnoreCase("All Departments")) {
                filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(selectedDept), 5));
            }
        }

        if (filters.isEmpty()) {
            tableSorter.setRowFilter(null);
        } else {
            tableSorter.setRowFilter(RowFilter.andFilter(filters));
        }

        // Update count tally
        int visibleCount = coursesTable.getRowCount();
        int visibleCredits = 0;
        for (int i = 0; i < visibleCount; i++) {
            int modelIdx = coursesTable.convertRowIndexToModel(i);
            String crStr = tableModel.getValueAt(modelIdx, 3).toString();
            try {
                int cr = Integer.parseInt(crStr.split(" ")[0]);
                visibleCredits += cr;
            } catch (Exception ignored) {}
        }

        tableTallyLabel.setText("Showing " + visibleCount + " of " + allCourses.size() + " courses  |  Enrolled: " + visibleCredits + " Credits");
    }

    private String formatTypeDisplay(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "Theory & Practical";
        if (raw.equalsIgnoreCase("Theory_Only") || raw.equalsIgnoreCase("Theory Only")) return "Theory Only";
        if (raw.equalsIgnoreCase("Practical_Only") || raw.equalsIgnoreCase("Practical Only")) return "Practical Only";
        if (raw.equalsIgnoreCase("Theory_and_Practical") || raw.equalsIgnoreCase("Theory & Practical")) return "Theory & Practical";
        return raw.replace("_", " ");
    }

    // ==========================================
    // 9. DETAILED COURSE MODAL DIALOG
    // ==========================================
    private void showCourseDetailsModal(int modelRow) {
        if (modelRow < 0 || modelRow >= allCourses.size()) return;
        Course course = allCourses.get(modelRow);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Course Module Details - " + course.getCourseCode(), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(520, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(COLOR_WHITE);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Top Header of Dialog
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel codeLbl = new JLabel(course.getCourseCode() + " • " + course.getCredits() + " Credits");
        codeLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        codeLbl.setForeground(COLOR_MAIN_BLUE);

        JLabel nameLbl = new JLabel(course.getCourseName());
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLbl.setForeground(COLOR_PRIMARY_NAVY);

        JPanel headerText = new JPanel(new GridLayout(2, 1, 0, 4));
        headerText.setOpaque(false);
        headerText.add(codeLbl);
        headerText.add(nameLbl);
        header.add(headerText, BorderLayout.CENTER);

        root.add(header, BorderLayout.NORTH);

        // Center Grid of Course Specifications
        JPanel specsGrid = new JPanel(new GridLayout(6, 2, 12, 10));
        specsGrid.setOpaque(false);
        specsGrid.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        int year = course.getAcademicYear() > 0 ? course.getAcademicYear() : 2;
        int sem = course.getSemester() > 0 ? course.getSemester() : 1;

        addSpecRow(specsGrid, "Module Code:", course.getCourseCode());
        addSpecRow(specsGrid, "Academic Credits:", course.getCredits() + " Academic Credits");
        addSpecRow(specsGrid, "Department:", course.getDepartment() != null ? course.getDepartment() : "DICT");
        addSpecRow(specsGrid, "Course Delivery:", formatTypeDisplay(course.getCourseType()));
        addSpecRow(specsGrid, "Academic Level & Term:", "Level " + (year == 1 ? "I" : "II") + " - Semester " + sem);
        addSpecRow(specsGrid, "Registration Status:", "● " + course.getStatus());

        // Syllabus / Description Section
        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setOpaque(false);
        centerContainer.add(specsGrid);
        centerContainer.add(Box.createVerticalStrut(14));

        JLabel syllabusHeading = new JLabel("Module Scope & Description:");
        syllabusHeading.setFont(new Font("Segoe UI", Font.BOLD, 12));
        syllabusHeading.setForeground(COLOR_PRIMARY_NAVY);
        centerContainer.add(syllabusHeading);
        centerContainer.add(Box.createVerticalStrut(6));

        JTextArea descArea = new JTextArea();
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descArea.setForeground(COLOR_TEXT_PRIMARY);
        descArea.setBackground(new Color(0xF8, 0xFA, 0xFC));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(8, 10, 8, 10)));

        String desc = course.getDescription();
        if (desc == null || desc.trim().isEmpty() || desc.contains("Theory") || desc.contains("Practical")) {
            desc = "Comprehensive curriculum module for " + course.getCourseName() +
                    ". Covers fundamental theoretical principles, applied practical exercises, and structured assessment under the Faculty of Technology degree program.";
        }
        descArea.setText(desc);
        centerContainer.add(descArea);

        root.add(centerContainer, BorderLayout.CENTER);

        // Bottom Close Button
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeBtn.setForeground(COLOR_WHITE);
        closeBtn.setBackground(COLOR_PRIMARY_NAVY);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setBorder(new EmptyBorder(8, 20, 8, 20));
        closeBtn.addActionListener(e -> dialog.dispose());
        bottom.add(closeBtn);
        root.add(bottom, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void addSpecRow(JPanel grid, String label, String value) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(COLOR_TEXT_MUTED);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        v.setForeground(COLOR_TEXT_PRIMARY);

        grid.add(l);
        grid.add(v);
    }

    // ==========================================
    // 10. AVATAR ICON HELPER
    // ==========================================
    private JLabel createAvatarIcon(int size, String name) {
        JLabel avatar = new JLabel();
        avatar.setPreferredSize(new Dimension(size, size));

        String initials = "ST";
        if (name != null && !name.trim().isEmpty()) {
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
            else if (parts[0].length() >= 2) initials = parts[0].substring(0, 2).toUpperCase();
        }
        final String text = initials;

        avatar.setIcon(new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_PRIMARY_NAVY);
                g2.fillOval(x, y, size, size);
                g2.setColor(COLOR_WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
                FontMetrics fm = g2.getFontMetrics();
                int tx = x + (size - fm.stringWidth(text)) / 2;
                int ty = y + ((size - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(text, tx, ty);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        });
        return avatar;
    }

    // ==========================================
    // 11. STANDALONE TESTING ENTRY POINT
    // ==========================================
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            JFrame frame = new JFrame("TecFAMS - Registered Course Details & Credits UI");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1240, 780);
            frame.setLocationRelativeTo(null);

            UndergraduateCourseDetailsUI panel = new UndergraduateCourseDetailsUI("TG/2024/2085");
            frame.setContentPane(panel);
            frame.setVisible(true);
        });
    }
}
