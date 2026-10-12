package main.java.ui.admin;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import main.java.dao.admin.CourseDAO;
import main.java.dao.admin.TimetableDAO;
import main.java.model.admin.Course;
import main.java.model.admin.TimetableEntry;

public class TimetableManagementPanel extends JPanel {

    private static final String VIEW_TIMETABLE = "TIMETABLE_VIEW";
    private static final String VIEW_FORM = "FORM_VIEW";

    private CardLayout cardLayout;

    // --- TIMETABLE VIEW COMPONENTS ---
    private JPanel timetablePanel;
    private JComboBox<DepartmentItem> deptCombo;
    private JComboBox<SemesterItem> semCombo;
    private JPanel gridContainer;
    private JPanel tablePanel;

    // Exactly 5 days and 4 slots
    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    private static final String[] TIME_SLOTS = {
        "8:00 - 10:00",
        "10:00 - 12:00",
        "12:00 - 14:00",
        "14:00 - 16:00"
    };

    // State for grid view
    private List<TimetableEntry> currentEntries = new ArrayList<>();

    // --- FORM VIEW COMPONENTS ---
    private JPanel formPanel;
    private JLabel formTitleLabel;
    private JLabel formSubtitleLabel;
    private JComboBox<DepartmentItem> formDeptCombo;
    private JComboBox<Integer> formYearCombo;
    private JComboBox<Integer> formSemCombo;
    private JComboBox<String> formDayCombo;
    private JComboBox<String> formTimeCombo;
    private JComboBox<CourseItem> formCourseCombo;
    private JComboBox<String> formTypeCombo;
    private JComboBox<String> formHallCombo;

    private JButton formSaveBtn;
    private JButton formDeleteBtn;
    private JButton formCancelBtn;

    // null if creating, ID if editing
    private Integer editingEntryId = null;

    public TimetableManagementPanel() {
        cardLayout = new CardLayout();
        setLayout(cardLayout);
        setBackground(new Color(248, 250, 252));

        buildTimetablePanel();
        buildFormPanel();

        add(timetablePanel, VIEW_TIMETABLE);
        add(formPanel, VIEW_FORM);

        cardLayout.show(this, VIEW_TIMETABLE);
        loadTimetableForSelectedFilters();
    }

    // =========================================================================
    // 1. TIMETABLE VIEW (GRID & FILTERS)
    // =========================================================================

    private void buildTimetablePanel() {
        timetablePanel = new JPanel(new BorderLayout());
        timetablePanel.setBackground(new Color(248, 250, 252));

        buildHeaderSection();
        buildContentSection();
    }

    private void buildHeaderSection() {
        JPanel headerPanel = new JPanel(new BorderLayout(20, 10));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(25, 30, 20, 30));

        // Title and Subtitle
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("Timetable Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(new Color(30, 41, 59));

        JLabel subtitleLabel = new JLabel("View, schedule and manage faculty academic timetables");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(120, 134, 150));

        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(6));
        titleBlock.add(subtitleLabel);

        // Header Right: "All Departments", "All Semesters", "+ Create Timetable"
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        controlsPanel.setOpaque(false);

        // 1. Department dropdown
        DepartmentItem[] depts = {
            new DepartmentItem(0, "All Departments"),
            new DepartmentItem(1, "Department of ICT"),
            new DepartmentItem(2, "Department of ET"),
            new DepartmentItem(3, "Department of BST"),
            new DepartmentItem(4, "Department of MDS")
        };
        deptCombo = new JComboBox<>(depts);
        stylePillComboBox(deptCombo, 160);
        deptCombo.addActionListener(e -> loadTimetableForSelectedFilters());

        // 2. Semester dropdown
        SemesterItem[] sems = {
            new SemesterItem(0, "All Semesters"),
            new SemesterItem(1, "Semester 1"),
            new SemesterItem(2, "Semester 2")
        };
        semCombo = new JComboBox<>(sems);
        stylePillComboBox(semCombo, 150);
        semCombo.addActionListener(e -> loadTimetableForSelectedFilters());

        // 3. "+ Create Timetable" button
        JButton createBtn = new JButton("+ Create Timetable");
        createBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        createBtn.setForeground(Color.WHITE);
        createBtn.setBackground(new Color(24, 119, 242)); // Modern vibrant blue
        createBtn.setFocusPainted(false);
        createBtn.setBorderPainted(false);
        createBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createBtn.setPreferredSize(new Dimension(170, 38));
        createBtn.setBorder(new EmptyBorder(8, 16, 8, 16));
        createBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                createBtn.setBackground(new Color(13, 98, 220));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                createBtn.setBackground(new Color(24, 119, 242));
            }
        });
        createBtn.addActionListener(e -> openCreateForm(null, null));

        controlsPanel.add(deptCombo);
        controlsPanel.add(semCombo);
        controlsPanel.add(Box.createHorizontalStrut(4));
        controlsPanel.add(createBtn);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(controlsPanel, BorderLayout.EAST);

        timetablePanel.add(headerPanel, BorderLayout.NORTH);
    }

    private void buildContentSection() {
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(new EmptyBorder(0, 30, 24, 30));

        // Rounded white card containing grid + bottom legend
        gridContainer = new JPanel(new BorderLayout(0, 16));
        gridContainer.setBackground(Color.WHITE);
        gridContainer.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(228, 232, 239), 18, 1),
            new EmptyBorder(22, 24, 18, 24)
        ));

        // Inner table grid with continuous grid lines
        tablePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(238, 242, 246));
                g2.setStroke(new BasicStroke(1f));

                int w = getWidth();
                int h = getHeight();

                // Row height = h / 5
                int rowH = h / 5;
                for (int r = 1; r < 5; r++) {
                    int y = r * rowH;
                    g2.drawLine(0, y, w, y);
                }

                // Column width = w / 6
                int colW = w / 6;
                for (int c = 1; c < 6; c++) {
                    int x = c * colW;
                    g2.drawLine(x, 0, x, h);
                }

                g2.dispose();
            }
        };
        tablePanel.setOpaque(true);
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setLayout(new GridLayout(5, 6, 0, 0));

        gridContainer.add(tablePanel, BorderLayout.CENTER);

        // Bottom Legend Section
        JPanel legendPanel = createLegendPanel();
        gridContainer.add(legendPanel, BorderLayout.SOUTH);

        centerWrapper.add(gridContainer, BorderLayout.CENTER);
        timetablePanel.add(centerWrapper, BorderLayout.CENTER);
    }

    private JPanel createLegendPanel() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 26, 6));
        legend.setOpaque(false);
        legend.setBorder(new EmptyBorder(8, 4, 4, 4));

        legend.add(createLegendItem("Lecture", new Color(37, 99, 235)));   // Blue
        legend.add(createLegendItem("Lab", new Color(16, 185, 129)));      // Green
        legend.add(createLegendItem("Tutorial", new Color(245, 158, 11))); // Orange
        legend.add(createLegendItem("Other", new Color(192, 132, 252)));   // Pink/Purple

        return legend;
    }

    private JPanel createLegendItem(String label, Color dotColor) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        item.setOpaque(false);

        JLabel dot = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(dotColor);
                g2.fillOval(0, 3, 11, 11);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(13, 18));

        JLabel text = new JLabel(label);
        text.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        text.setForeground(new Color(90, 105, 120));

        item.add(dot);
        item.add(text);
        return item;
    }

    private void loadTimetableForSelectedFilters() {
        DepartmentItem selDept = (DepartmentItem) deptCombo.getSelectedItem();
        SemesterItem selSem = (SemesterItem) semCombo.getSelectedItem();

        int deptId = selDept != null ? selDept.id : 0;
        int sem = selSem != null ? selSem.semester : 0;

        // Query entries matching department and semester
        currentEntries = TimetableDAO.getEntries(deptId, 1, sem);
        if (currentEntries.isEmpty() && (deptId == 0 || sem == 0)) {
            currentEntries = TimetableDAO.getEntries(deptId, 0, sem);
        }

        renderScheduleGrid();
    }

    private void renderScheduleGrid() {
        tablePanel.removeAll();

        // Row 0: Headers
        JPanel corner = new JPanel();
        corner.setOpaque(false);
        tablePanel.add(corner);

        // Columns 1-5: Days
        for (String day : DAYS) {
            JPanel dayCell = new JPanel(new GridBagLayout());
            dayCell.setOpaque(false);
            JLabel dayLabel = new JLabel(day);
            dayLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            dayLabel.setForeground(new Color(110, 125, 145));
            dayCell.add(dayLabel);
            tablePanel.add(dayCell);
        }

        // Rows 1-4: Time Slots
        for (String slot : TIME_SLOTS) {
            JPanel timeCell = new JPanel(new GridBagLayout());
            timeCell.setOpaque(false);
            JLabel timeLabel = new JLabel(slot);
            timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            timeLabel.setForeground(new Color(110, 125, 145));
            timeCell.add(timeLabel);
            tablePanel.add(timeCell);

            for (String day : DAYS) {
                TimetableEntry match = findEntryForSlot(day, slot);
                if (match != null) {
                    tablePanel.add(createCardCell(match));
                } else {
                    tablePanel.add(createEmptySlotCell(day, slot));
                }
            }
        }

        tablePanel.revalidate();
        tablePanel.repaint();
    }

    private TimetableEntry findEntryForSlot(String day, String slotTime) {
        String[] parts = slotTime.split(" - ");
        if (parts.length < 2) return null;
        String startSlot = parts[0].trim();
        String endSlot = parts[1].trim();

        for (TimetableEntry entry : currentEntries) {
            if (entry.getDayOfWeek() != null && entry.getDayOfWeek().equalsIgnoreCase(day)) {
                String entStart = cleanTime(entry.getStartTime());
                String entEnd = cleanTime(entry.getEndTime());
                if (entStart.equals(startSlot) && entEnd.equals(endSlot)) {
                    return entry;
                }
            }
        }
        return null;
    }

    private String cleanTime(String t) {
        if (t == null) return "";
        t = t.trim();
        if (t.length() >= 5) {
            String s = t.substring(0, 5);
            if (s.startsWith("0")) s = s.substring(1);
            return s;
        }
        return t;
    }

    private JPanel createCardCell(TimetableEntry entry) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(8, 8, 8, 8));

        SlotColorTheme theme = getThemeForSessionType(entry.getSessionType());

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(theme.background);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));

                g2.setColor(theme.border);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 16, 16));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(10, 16, 10, 16));
        card.setPreferredSize(new Dimension(135, 62));
        card.setMinimumSize(new Dimension(110, 54));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel codeLabel = new JLabel(entry.getCourseCode() != null ? entry.getCourseCode() : "N/A");
        codeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        codeLabel.setForeground(theme.text);
        codeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hallLabel = new JLabel(entry.getHall() != null ? entry.getHall() : "");
        hallLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hallLabel.setForeground(theme.text);
        hallLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(codeLabel);
        card.add(Box.createVerticalStrut(3));
        card.add(hallLabel);
        card.add(Box.createVerticalGlue());

        // In-place edit on click - no separate window!
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openEditForm(entry);
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(4, 6, 4, 6);
        wrapper.add(card, gbc);

        return wrapper;
    }

    private JPanel createEmptySlotCell(String day, String slotTime) {
        JPanel emptyCell = new JPanel();
        emptyCell.setOpaque(false);
        emptyCell.setCursor(new Cursor(Cursor.HAND_CURSOR));
        emptyCell.setToolTipText("Click to create a timetable slot for " + day + " (" + slotTime + ")");

        emptyCell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                emptyCell.setBackground(new Color(245, 248, 252));
                emptyCell.setOpaque(true);
                emptyCell.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                emptyCell.setOpaque(false);
                emptyCell.repaint();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                openCreateForm(day, slotTime);
            }
        });

        return emptyCell;
    }

    private SlotColorTheme getThemeForSessionType(String type) {
        if (type == null) type = "Lecture";
        switch (type.trim().toLowerCase()) {
            case "lab":
            case "practical":
                return new SlotColorTheme(
                    new Color(213, 245, 227), // Soft pastel green
                    new Color(187, 240, 209),
                    new Color(30, 130, 76)
                );
            case "tutorial":
                return new SlotColorTheme(
                    new Color(254, 237, 214), // Soft pastel peach
                    new Color(250, 221, 180),
                    new Color(194, 98, 7)
                );
            case "other":
                return new SlotColorTheme(
                    new Color(252, 228, 236), // Soft pastel pink
                    new Color(248, 205, 220),
                    new Color(173, 20, 87)
                );
            case "lecture":
            case "theory":
            default:
                return new SlotColorTheme(
                    new Color(212, 228, 252), // Soft pastel blue
                    new Color(187, 212, 250),
                    new Color(30, 80, 180)
                );
        }
    }

    private static class SlotColorTheme {
        Color background;
        Color border;
        Color text;
        SlotColorTheme(Color bg, Color br, Color tx) {
            this.background = bg;
            this.border = br;
            this.text = tx;
        }
    }

    // =========================================================================
    // 2. IN-PLACE MODERN FORM VIEW (NO MODAL/POPUP WINDOW!)
    // =========================================================================

    private void buildFormPanel() {
        formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(new Color(248, 250, 252));

        // Form Header
        JPanel headerPanel = new JPanel(new BorderLayout(20, 10));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(25, 30, 20, 30));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        formTitleLabel = new JLabel("Create Timetable Entry");
        formTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        formTitleLabel.setForeground(new Color(30, 41, 59));

        formSubtitleLabel = new JLabel("Schedule a class slot and assign course, hall, and timing details");
        formSubtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formSubtitleLabel.setForeground(new Color(120, 134, 150));

        titleBlock.add(formTitleLabel);
        titleBlock.add(Box.createVerticalStrut(6));
        titleBlock.add(formSubtitleLabel);

        // Header Right: "← Back to Timetable" Button
        JButton backBtn = new JButton("← Back to Timetable");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backBtn.setForeground(new Color(71, 85, 105));
        backBtn.setBackground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setPreferredSize(new Dimension(175, 38));
        backBtn.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(203, 213, 225), 10, 1),
            new EmptyBorder(8, 16, 8, 16)
        ));
        backBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                backBtn.setBackground(new Color(241, 245, 249));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                backBtn.setBackground(Color.WHITE);
            }
        });
        backBtn.addActionListener(e -> returnToTimetableGrid());

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(backBtn, BorderLayout.EAST);
        formPanel.add(headerPanel, BorderLayout.NORTH);

        // Main Form Container inside a clean ScrollPane
        JPanel scrollContentWrapper = new JPanel(new BorderLayout());
        scrollContentWrapper.setOpaque(false);
        scrollContentWrapper.setBorder(new EmptyBorder(0, 30, 25, 30));

        // Modern Card Container
        JPanel cardBox = new JPanel(new BorderLayout(0, 24));
        cardBox.setBackground(Color.WHITE);
        cardBox.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(228, 232, 239), 18, 1),
            new EmptyBorder(32, 36, 32, 36)
        ));

        // Card Top Badge
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgePanel.setOpaque(false);
        JLabel badgeLabel = new JLabel("  ACADEMIC SCHEDULE DETAILS  ");
        badgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badgeLabel.setForeground(new Color(24, 119, 242));
        badgeLabel.setBackground(new Color(235, 244, 255));
        badgeLabel.setOpaque(true);
        badgeLabel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(195, 220, 255), 8, 1),
            new EmptyBorder(4, 8, 4, 8)
        ));
        badgePanel.add(badgeLabel);
        cardBox.add(badgePanel, BorderLayout.NORTH);

        // 2-Column Inputs Grid
        JPanel fieldsGrid = new JPanel(new GridLayout(4, 2, 30, 20));
        fieldsGrid.setOpaque(false);

        // 1. Department
        DepartmentItem[] depts = {
            new DepartmentItem(1, "Department of ICT"),
            new DepartmentItem(2, "Department of ET"),
            new DepartmentItem(3, "Department of BST"),
            new DepartmentItem(4, "Department of MDS")
        };
        formDeptCombo = new JComboBox<>(depts);
        styleFormFieldCombo(formDeptCombo);
        fieldsGrid.add(createFormItem("Faculty Department", formDeptCombo));

        // 2. Academic Year / Level
        Integer[] years = {1, 2, 3, 4};
        formYearCombo = new JComboBox<>(years);
        formYearCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText("Year " + value + " (Level " + value + ")");
                return this;
            }
        });
        styleFormFieldCombo(formYearCombo);
        fieldsGrid.add(createFormItem("Academic Year / Level", formYearCombo));

        // 3. Semester
        Integer[] sems = {1, 2};
        formSemCombo = new JComboBox<>(sems);
        formSemCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText("Semester " + value);
                return this;
            }
        });
        styleFormFieldCombo(formSemCombo);
        fieldsGrid.add(createFormItem("Semester", formSemCombo));

        // 4. Day of the Week
        formDayCombo = new JComboBox<>(DAYS);
        styleFormFieldCombo(formDayCombo);
        fieldsGrid.add(createFormItem("Day of the Week", formDayCombo));

        // 5. Time Slot
        formTimeCombo = new JComboBox<>(TIME_SLOTS);
        styleFormFieldCombo(formTimeCombo);
        fieldsGrid.add(createFormItem("Class Time Slot", formTimeCombo));

        // 6. Subject / Course (loaded from CourseDAO)
        formCourseCombo = new JComboBox<>();
        styleFormFieldCombo(formCourseCombo);
        refreshCoursesInForm();
        fieldsGrid.add(createFormItem("Subject / Course Code", formCourseCombo));

        // 7. Session Type (Lecture, Lab, Tutorial, Other)
        String[] types = {"Lecture", "Lab", "Tutorial", "Other"};
        formTypeCombo = new JComboBox<>(types);
        styleFormFieldCombo(formTypeCombo);
        fieldsGrid.add(createFormItem("Session Type", formTypeCombo));

        // 8. Hall / Venue
        String[] halls = {"LT 1", "LT 2", "LT 3", "Lab 1", "Lab 2", "Drawing Office", "Main Auditorium"};
        formHallCombo = new JComboBox<>(halls);
        formHallCombo.setEditable(true);
        styleFormFieldCombo(formHallCombo);
        fieldsGrid.add(createFormItem("Lecture Hall / Venue", formHallCombo));

        cardBox.add(fieldsGrid, BorderLayout.CENTER);

        // Action Buttons Bar at Bottom of Card
        JPanel actionBar = new JPanel(new BorderLayout());
        actionBar.setOpaque(false);
        actionBar.setBorder(new EmptyBorder(16, 0, 0, 0));

        // Delete button on the left (only when editing)
        formDeleteBtn = new JButton("Delete Class Slot");
        formDeleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formDeleteBtn.setForeground(new Color(220, 38, 38));
        formDeleteBtn.setBackground(new Color(254, 242, 242));
        formDeleteBtn.setFocusPainted(false);
        formDeleteBtn.setBorderPainted(false);
        formDeleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        formDeleteBtn.setPreferredSize(new Dimension(150, 42));
        formDeleteBtn.setBorder(new EmptyBorder(10, 18, 10, 18));
        formDeleteBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                formDeleteBtn.setBackground(new Color(254, 226, 226));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                formDeleteBtn.setBackground(new Color(254, 242, 242));
            }
        });
        formDeleteBtn.addActionListener(e -> handleDeleteAction());

        // Right side: Cancel & Save
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightButtons.setOpaque(false);

        formCancelBtn = new JButton("Cancel");
        formCancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formCancelBtn.setForeground(new Color(100, 116, 139));
        formCancelBtn.setBackground(Color.WHITE);
        formCancelBtn.setFocusPainted(false);
        formCancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        formCancelBtn.setPreferredSize(new Dimension(110, 42));
        formCancelBtn.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(203, 213, 225), 8, 1),
            new EmptyBorder(8, 16, 8, 16)
        ));
        formCancelBtn.addActionListener(e -> returnToTimetableGrid());

        formSaveBtn = new JButton("Save Timetable Entry");
        formSaveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formSaveBtn.setForeground(Color.WHITE);
        formSaveBtn.setBackground(new Color(24, 119, 242));
        formSaveBtn.setFocusPainted(false);
        formSaveBtn.setBorderPainted(false);
        formSaveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        formSaveBtn.setPreferredSize(new Dimension(190, 42));
        formSaveBtn.setBorder(new EmptyBorder(10, 20, 10, 20));
        formSaveBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                formSaveBtn.setBackground(new Color(13, 98, 220));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                formSaveBtn.setBackground(new Color(24, 119, 242));
            }
        });
        formSaveBtn.addActionListener(e -> handleSaveAction());

        rightButtons.add(formCancelBtn);
        rightButtons.add(formSaveBtn);

        actionBar.add(formDeleteBtn, BorderLayout.WEST);
        actionBar.add(rightButtons, BorderLayout.EAST);

        cardBox.add(actionBar, BorderLayout.SOUTH);

        scrollContentWrapper.add(cardBox, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(scrollContentWrapper);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        formPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createFormItem(String labelText, JComponent field) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(51, 65, 85));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        wrapper.add(label);
        wrapper.add(Box.createVerticalStrut(7));
        wrapper.add(field);

        return wrapper;
    }

    private void styleFormFieldCombo(JComboBox<?> box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        box.setBackground(Color.WHITE);
        box.setForeground(new Color(30, 41, 59));
        box.setPreferredSize(new Dimension(300, 42));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        box.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(214, 221, 230), 8, 1),
            new EmptyBorder(4, 10, 4, 10)
        ));
    }

    private void refreshCoursesInForm() {
        formCourseCombo.removeAllItems();
        try {
            List<Course> allCourses = CourseDAO.getAllCourses();
            for (Course c : allCourses) {
                formCourseCombo.addItem(new CourseItem(c.getCourseId(), c.getCourseCode(), c.getCourseName()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // =========================================================================
    // 3. TRANSITION & ACTION LOGIC (IN-PLACE SWITCHING)
    // =========================================================================

    /**
     * Open form in "Create" mode in-place (no popups)
     */
    private void openCreateForm(String defaultDay, String defaultSlot) {
        editingEntryId = null;
        formTitleLabel.setText("Create Timetable Entry");
        formSubtitleLabel.setText("Schedule a class slot and assign course, hall, and timing details");
        formSaveBtn.setText("Save Timetable Entry");
        formDeleteBtn.setVisible(false);

        refreshCoursesInForm();

        // Sync with currently selected header filters if valid
        DepartmentItem selDept = (DepartmentItem) deptCombo.getSelectedItem();
        if (selDept != null && selDept.id > 0) {
            for (int i = 0; i < formDeptCombo.getItemCount(); i++) {
                if (formDeptCombo.getItemAt(i).id == selDept.id) {
                    formDeptCombo.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            formDeptCombo.setSelectedIndex(0);
        }

        SemesterItem selSem = (SemesterItem) semCombo.getSelectedItem();
        if (selSem != null && selSem.semester > 0) {
            formSemCombo.setSelectedItem(selSem.semester);
        } else {
            formSemCombo.setSelectedItem(1);
        }

        formYearCombo.setSelectedItem(1);

        if (defaultDay != null) {
            formDayCombo.setSelectedItem(defaultDay);
        } else {
            formDayCombo.setSelectedIndex(0);
        }

        if (defaultSlot != null) {
            formTimeCombo.setSelectedItem(defaultSlot);
        } else {
            formTimeCombo.setSelectedIndex(0);
        }

        formTypeCombo.setSelectedItem("Lecture");
        formHallCombo.setSelectedItem("LT 1");

        // In-place switch
        cardLayout.show(this, VIEW_FORM);
    }

    /**
     * Open form in "Edit" mode in-place (no popups)
     */
    private void openEditForm(TimetableEntry entry) {
        editingEntryId = entry.getEntryId();
        formTitleLabel.setText("Edit Timetable Class Slot");
        formSubtitleLabel.setText("Modify schedule for " + (entry.getCourseCode() != null ? entry.getCourseCode() : "Class") + " on " + entry.getDayOfWeek());
        formSaveBtn.setText("Update Class Slot");
        formDeleteBtn.setVisible(true);

        refreshCoursesInForm();

        // Department
        DepartmentItem selDept = (DepartmentItem) deptCombo.getSelectedItem();
        if (selDept != null && selDept.id > 0) {
            for (int i = 0; i < formDeptCombo.getItemCount(); i++) {
                if (formDeptCombo.getItemAt(i).id == selDept.id) {
                    formDeptCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Semester
        SemesterItem selSem = (SemesterItem) semCombo.getSelectedItem();
        if (selSem != null && selSem.semester > 0) {
            formSemCombo.setSelectedItem(selSem.semester);
        }

        // Day
        if (entry.getDayOfWeek() != null) {
            formDayCombo.setSelectedItem(entry.getDayOfWeek());
        }

        // Slot
        String slotFormatted = entry.getTimeSlotFormatted();
        if (slotFormatted != null && !slotFormatted.isEmpty()) {
            formTimeCombo.setSelectedItem(slotFormatted);
        }

        // Course
        for (int i = 0; i < formCourseCombo.getItemCount(); i++) {
            CourseItem ci = formCourseCombo.getItemAt(i);
            if (ci.id == entry.getCourseId()) {
                formCourseCombo.setSelectedIndex(i);
                break;
            }
        }

        // Session Type
        if (entry.getSessionType() != null) {
            formTypeCombo.setSelectedItem(entry.getSessionType());
        }

        // Hall
        if (entry.getHall() != null) {
            formHallCombo.setSelectedItem(entry.getHall());
        }

        // In-place switch
        cardLayout.show(this, VIEW_FORM);
    }

    private void returnToTimetableGrid() {
        cardLayout.show(this, VIEW_TIMETABLE);
    }

    private void handleSaveAction() {
        CourseItem selCourse = (CourseItem) formCourseCombo.getSelectedItem();
        if (selCourse == null) {
            JOptionPane.showMessageDialog(this, "Please select a valid course.", "Missing Field", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DepartmentItem selDept = (DepartmentItem) formDeptCombo.getSelectedItem();
        int deptId = selDept != null ? selDept.id : 1;
        int year = (Integer) formYearCombo.getSelectedItem();
        int sem = (Integer) formSemCombo.getSelectedItem();

        String day = (String) formDayCombo.getSelectedItem();
        String slotStr = (String) formTimeCombo.getSelectedItem();
        String type = (String) formTypeCombo.getSelectedItem();
        String hall = (String) formHallCombo.getSelectedItem();
        if (hall != null) hall = hall.trim();
        if (hall == null || hall.isEmpty()) hall = "LT 1";

        String[] times = slotStr.split(" - ");
        String startSql = formatSqlTime(times[0].trim());
        String endSql = formatSqlTime(times[1].trim());

        if (editingEntryId == null) {
            // Create New Entry
            int targetTtId = TimetableDAO.getOrCreateTimetable(deptId, year, sem);
            boolean success = TimetableDAO.addEntry(targetTtId, selCourse.id, day, startSql, endSql, hall, type);
            if (success) {
                JOptionPane.showMessageDialog(this, "Timetable entry created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadTimetableForSelectedFilters();
                returnToTimetableGrid();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create timetable entry. Please verify inputs.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // Update Existing Entry
            boolean success = TimetableDAO.updateEntry(editingEntryId, selCourse.id, day, startSql, endSql, hall, type);
            if (success) {
                JOptionPane.showMessageDialog(this, "Timetable entry updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadTimetableForSelectedFilters();
                returnToTimetableGrid();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update timetable entry.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteAction() {
        if (editingEntryId == null) return;

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this timetable class slot?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (TimetableDAO.deleteEntry(editingEntryId)) {
                JOptionPane.showMessageDialog(this, "Class slot deleted successfully!", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                loadTimetableForSelectedFilters();
                returnToTimetableGrid();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete slot.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String formatSqlTime(String t) {
        if (!t.contains(":")) return t + ":00:00";
        String[] parts = t.split(":");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        return String.format("%02d:%02d:00", h, m);
    }

    private void stylePillComboBox(JComboBox<?> box, int width) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        box.setBackground(Color.WHITE);
        box.setForeground(new Color(60, 75, 95));
        box.setPreferredSize(new Dimension(width, 38));
        box.setFocusable(false);
        box.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(220, 226, 235), 10, 1),
            new EmptyBorder(3, 10, 3, 10)
        ));
    }

    // Helper Dropdown Items
    private static class DepartmentItem {
        int id;
        String name;
        DepartmentItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    private static class SemesterItem {
        int semester;
        String title;
        SemesterItem(int semester, String title) { this.semester = semester; this.title = title; }
        @Override public String toString() { return title; }
    }

    private static class CourseItem {
        int id;
        String code;
        String name;
        CourseItem(int id, String code, String name) { this.id = id; this.code = code; this.name = name; }
        @Override public String toString() { return code + " - " + name; }
    }

    // Custom Rounded Border
    private static class RoundedBorder extends LineBorder {
        private int radius;
        RoundedBorder(Color color, int radius, int thickness) {
            super(color, thickness, true);
            this.radius = radius;
        }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(lineColor);
            g2.draw(new RoundRectangle2D.Float(x, y, width - 1, height - 1, radius, radius));
            g2.dispose();
        }
    }
}
