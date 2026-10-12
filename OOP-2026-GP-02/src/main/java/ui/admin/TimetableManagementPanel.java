package main.java.ui.admin;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;

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
    private JComboBox<LevelItem> levelCombo;
    private JComboBox<SemesterItem> semCombo;
    private JPanel gridContainer;
    private JPanel tablePanel;

    // 5 Days (Monday - Friday)
    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

    // Hourly ordered time slots from 8:00 to 19:00
    private static final String[] TIME_SLOTS = {
        "8:00 - 9:00",
        "9:00 - 10:00",
        "10:00 - 11:00",
        "11:00 - 12:00",
        "12:00 - 13:00",
        "13:00 - 14:00",
        "14:00 - 15:00",
        "15:00 - 16:00",
        "16:00 - 17:00",
        "17:00 - 18:00",
        "18:00 - 19:00"
    };

    // Current state
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
    // 1. TIMETABLE VIEW (HOURLY 8-19 GRID WITH VERTICAL SCROLLBAR)
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

        // Header Right: Filters + Button
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
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
        stylePillComboBox(deptCombo, 155);
        deptCombo.addActionListener(e -> loadTimetableForSelectedFilters());

        // 2. Level / Academic Year dropdown
        LevelItem[] levels = {
            new LevelItem(0, "All Levels"),
            new LevelItem(1, "Level 1"),
            new LevelItem(2, "Level 2"),
            new LevelItem(3, "Level 3"),
            new LevelItem(4, "Level 4")
        };
        levelCombo = new JComboBox<>(levels);
        stylePillComboBox(levelCombo, 120);
        levelCombo.addActionListener(e -> loadTimetableForSelectedFilters());

        // 3. Semester dropdown
        SemesterItem[] sems = {
            new SemesterItem(0, "All Semesters"),
            new SemesterItem(1, "Semester 1"),
            new SemesterItem(2, "Semester 2")
        };
        semCombo = new JComboBox<>(sems);
        stylePillComboBox(semCombo, 140);
        semCombo.addActionListener(e -> loadTimetableForSelectedFilters());

        // 4. "+ Create Timetable" button
        JButton createBtn = new JButton("+ Create Timetable");
        createBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        createBtn.setForeground(Color.WHITE);
        createBtn.setBackground(new Color(24, 119, 242));
        createBtn.setFocusPainted(false);
        createBtn.setBorderPainted(false);
        createBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createBtn.setPreferredSize(new Dimension(165, 38));
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
        controlsPanel.add(levelCombo);
        controlsPanel.add(semCombo);
        controlsPanel.add(Box.createHorizontalStrut(2));
        controlsPanel.add(createBtn);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(controlsPanel, BorderLayout.EAST);

        timetablePanel.add(headerPanel, BorderLayout.NORTH);
    }

    private void buildContentSection() {
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(new EmptyBorder(0, 30, 24, 30));

        // Rounded white card container
        gridContainer = new JPanel(new BorderLayout(0, 16));
        gridContainer.setBackground(Color.WHITE);
        gridContainer.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(228, 232, 239), 18, 1),
            new EmptyBorder(18, 20, 18, 20)
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

                int totalRows = TIME_SLOTS.length + 1; // 1 header row + 11 hourly slot rows
                int rowH = h / totalRows;
                for (int r = 1; r < totalRows; r++) {
                    int y = r * rowH;
                    g2.drawLine(0, y, w, y);
                }

                // Column lines (6 cols: time label + 5 days)
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
        tablePanel.setLayout(new GridLayout(TIME_SLOTS.length + 1, 6, 0, 0));

        int totalHeight = (TIME_SLOTS.length + 1) * 82;
        tablePanel.setPreferredSize(new Dimension(860, totalHeight));

        // Smooth vertical scroll pane for the grid
        JScrollPane gridScrollPane = new JScrollPane(tablePanel);
        gridScrollPane.setBorder(BorderFactory.createEmptyBorder());
        gridScrollPane.setOpaque(false);
        gridScrollPane.getViewport().setOpaque(false);
        gridScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        gridScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        gridScrollPane.getVerticalScrollBar().setUnitIncrement(24);

        JScrollBar vBar = gridScrollPane.getVerticalScrollBar();
        vBar.setPreferredSize(new Dimension(8, Integer.MAX_VALUE));
        vBar.setUI(new ModernScrollBarUI());

        gridContainer.add(gridScrollPane, BorderLayout.CENTER);

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
        legend.add(createLegendItem("Practical", new Color(16, 185, 129))); // Mint Green
        legend.add(createLegendItem("Lab", new Color(16, 185, 129)));       // Mint Green
        legend.add(createLegendItem("Tutorial", new Color(245, 158, 11)));  // Orange / Peach
        legend.add(createLegendItem("Other", new Color(192, 132, 252)));    // Pink / Purple

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
        LevelItem selLevel = (LevelItem) levelCombo.getSelectedItem();
        SemesterItem selSem = (SemesterItem) semCombo.getSelectedItem();

        int deptId = selDept != null ? selDept.id : 0;
        int level = selLevel != null ? selLevel.level : 0;
        int sem = selSem != null ? selSem.semester : 0;

        currentEntries = TimetableDAO.getEntries(deptId, level, sem);

        renderScheduleGrid();
    }

    /**
     * Renders the schedule grid in orderly hourly rows from 8:00 to 19:00.
     */
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
            dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            dayLabel.setForeground(new Color(110, 125, 145));
            dayCell.add(dayLabel);
            tablePanel.add(dayCell);
        }

        // Hourly Rows: 8:00 - 9:00, 9:00 - 10:00, ... 18:00 - 19:00
        for (String slot : TIME_SLOTS) {
            JPanel timeCell = new JPanel(new GridBagLayout());
            timeCell.setOpaque(false);
            JLabel timeLabel = new JLabel(slot);
            timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            timeLabel.setForeground(new Color(110, 125, 145));
            timeCell.add(timeLabel);
            tablePanel.add(timeCell);

            // Day cells for this hourly slot
            for (String day : DAYS) {
                List<TimetableEntry> matches = findEntriesForSlot(day, slot);
                if (matches.isEmpty()) {
                    tablePanel.add(createEmptySlotCell(day, slot));
                } else {
                    tablePanel.add(createMultiCardSlotCell(day, slot, matches));
                }
            }
        }

        tablePanel.revalidate();
        tablePanel.repaint();
    }

    /**
     * Finds ALL entries that start at or overlap with this hourly slot.
     */
    private List<TimetableEntry> findEntriesForSlot(String day, String slotTime) {
        List<TimetableEntry> matches = new ArrayList<>();
        String[] parts = slotTime.split(" - ");
        if (parts.length < 2) return matches;
        String startSlot = parts[0].trim();
        String endSlot = parts[1].trim();

        int slotStartMin = parseTimeToMinutes(startSlot);
        int slotEndMin = parseTimeToMinutes(endSlot);

        for (TimetableEntry entry : currentEntries) {
            if (entry.getDayOfWeek() != null && entry.getDayOfWeek().equalsIgnoreCase(day)) {
                String entStart = cleanTime(entry.getStartTime());
                String entEnd = cleanTime(entry.getEndTime());

                int eStart = parseTimeToMinutes(entStart);
                int eEnd = parseTimeToMinutes(entEnd);

                if (eStart == slotStartMin || (eStart >= slotStartMin && eStart < slotEndMin)) {
                    matches.add(entry);
                } else if (eStart < slotStartMin && eEnd > slotStartMin) {
                    matches.add(entry);
                }
            }
        }
        return matches;
    }

    private int parseTimeToMinutes(String t) {
        if (t == null || t.trim().isEmpty()) return 0;
        try {
            t = t.trim();
            String[] parts = t.split(":");
            int h = Integer.parseInt(parts[0].trim());
            int m = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
            return h * 60 + m;
        } catch (Exception ex) {
            return 0;
        }
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

    private JPanel createMultiCardSlotCell(String day, String slotTime, List<TimetableEntry> entries) {
        JPanel cellWrapper = new JPanel(new BorderLayout());
        cellWrapper.setOpaque(false);

        JPanel cardsRow = new JPanel();
        cardsRow.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 4));
        cardsRow.setOpaque(false);

        for (TimetableEntry entry : entries) {
            cardsRow.add(createCardComponent(entry));
        }

        JButton addMoreBtn = createMiniAddButton(day, slotTime);
        cardsRow.add(addMoreBtn);

        JScrollPane scrollPane = new JScrollPane(cardsRow);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JScrollBar hBar = scrollPane.getHorizontalScrollBar();
        hBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 4));
        hBar.setUI(new ModernScrollBarUI());
        hBar.setUnitIncrement(16);

        scrollPane.addMouseWheelListener(e -> {
            if (hBar.isVisible()) {
                int scrollAmount = e.getUnitsToScroll() * 15;
                hBar.setValue(hBar.getValue() + scrollAmount);
                e.consume();
            }
        });

        cellWrapper.add(scrollPane, BorderLayout.CENTER);
        return cellWrapper;
    }

    private JPanel createCardComponent(TimetableEntry entry) {
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
        card.setBorder(new EmptyBorder(8, 12, 8, 12));
        card.setPreferredSize(new Dimension(145, 68));
        card.setMinimumSize(new Dimension(130, 64));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        String tip = (entry.getCourseCode() != null ? entry.getCourseCode() : "") +
                     (entry.getCourseName() != null ? " - " + entry.getCourseName() : "") +
                     " (" + (entry.getSessionType() != null ? entry.getSessionType() : "Class") + ")" +
                     " [" + entry.getTimeSlotFormatted() + "]" +
                     " @ " + (entry.getHall() != null ? entry.getHall() : "TBA");
        card.setToolTipText(tip);

        JLabel codeLabel = new JLabel(entry.getCourseCode() != null ? entry.getCourseCode() : "N/A");
        codeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        codeLabel.setForeground(theme.text);
        codeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hallLabel = new JLabel(entry.getHall() != null ? entry.getHall() : "");
        hallLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hallLabel.setForeground(theme.text);
        hallLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(codeLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(hallLabel);
        card.add(Box.createVerticalGlue());

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openEditForm(entry);
            }
        });

        return card;
    }

    private JButton createMiniAddButton(String day, String slotTime) {
        JButton btn = new JButton("+");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(new Color(160, 174, 192));
        btn.setBackground(new Color(248, 250, 252));
        btn.setPreferredSize(new Dimension(28, 56));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Add another class to " + day + " (" + slotTime + ")");

        btn.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(226, 232, 240), 10, 1),
            new EmptyBorder(4, 4, 4, 4)
        ));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(237, 242, 247));
                btn.setForeground(new Color(24, 119, 242));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(248, 250, 252));
                btn.setForeground(new Color(160, 174, 192));
            }
        });

        btn.addActionListener(e -> openCreateForm(day, slotTime));
        return btn;
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
            case "practical":
            case "practicle":
            case "lab":
                return new SlotColorTheme(
                    new Color(213, 245, 227), // Soft pastel green
                    new Color(187, 240, 209),
                    new Color(30, 130, 76)     // Deep green text
                );
            case "tutorial":
                return new SlotColorTheme(
                    new Color(254, 237, 214), // Soft pastel peach
                    new Color(250, 221, 180),
                    new Color(194, 98, 7)      // Deep amber text
                );
            case "other":
                return new SlotColorTheme(
                    new Color(252, 228, 236), // Soft pastel pink
                    new Color(248, 205, 220),
                    new Color(173, 20, 87)     // Deep magenta text
                );
            case "lecture":
            case "theory":
            default:
                return new SlotColorTheme(
                    new Color(212, 228, 252), // Soft pastel blue
                    new Color(187, 212, 250),
                    new Color(30, 80, 180)     // Deep blue text
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
    // 2. ULTRA-MODERN IN-PLACE FORM VIEW (CLEAN DESIGN SYSTEM)
    // =========================================================================

    private void buildFormPanel() {
        formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(new Color(248, 250, 252));

        // Form Header
        JPanel headerPanel = new JPanel(new BorderLayout(20, 10));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(26, 36, 18, 36));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        formTitleLabel = new JLabel("Create Timetable Entry");
        formTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        formTitleLabel.setForeground(new Color(15, 23, 42)); // Slate 900

        formSubtitleLabel = new JLabel("Configure schedule parameters, assign venue, and allocate course timing");
        formSubtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formSubtitleLabel.setForeground(new Color(100, 116, 139)); // Slate 500

        titleBlock.add(formTitleLabel);
        titleBlock.add(Box.createVerticalStrut(6));
        titleBlock.add(formSubtitleLabel);

        // Header Right: Sleek "← Back to Timetable" Button
        JButton backBtn = new JButton("← Back to Timetable");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backBtn.setForeground(new Color(51, 65, 85));
        backBtn.setBackground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setPreferredSize(new Dimension(175, 40));
        backBtn.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(226, 232, 240), 10, 1),
            new EmptyBorder(8, 16, 8, 16)
        ));
        backBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                backBtn.setBackground(new Color(241, 245, 249));
                backBtn.setForeground(new Color(15, 23, 42));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                backBtn.setBackground(Color.WHITE);
                backBtn.setForeground(new Color(51, 65, 85));
            }
        });
        backBtn.addActionListener(e -> returnToTimetableGrid());

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(backBtn, BorderLayout.EAST);
        formPanel.add(headerPanel, BorderLayout.NORTH);

        // Main Form Container inside a clean ScrollPane
        JPanel scrollContentWrapper = new JPanel(new BorderLayout());
        scrollContentWrapper.setOpaque(false);
        scrollContentWrapper.setBorder(new EmptyBorder(4, 36, 32, 36));

        // Modern Card Container with soft elevation
        JPanel cardBox = new JPanel(new BorderLayout(0, 24)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Soft card background
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));

                // Subtle border
                g2.setColor(new Color(226, 232, 240));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 20, 20));

                g2.dispose();
            }
        };
        cardBox.setOpaque(false);
        cardBox.setBorder(new EmptyBorder(32, 38, 32, 38));

        // Card Top Badge & Section Heading
        JPanel cardTopPanel = new JPanel(new BorderLayout());
        cardTopPanel.setOpaque(false);

        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeWrapper.setOpaque(false);

        JLabel badgeLabel = new JLabel("  SCHEDULE CONFIGURATION  ");
        badgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badgeLabel.setForeground(new Color(24, 119, 242));
        badgeLabel.setBackground(new Color(239, 246, 255)); // Blue 50
        badgeLabel.setOpaque(true);
        badgeLabel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(191, 219, 254), 8, 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
        badgeWrapper.add(badgeLabel);

        cardTopPanel.add(badgeWrapper, BorderLayout.WEST);
        cardBox.add(cardTopPanel, BorderLayout.NORTH);

        // 2-Column Inputs Grid with generous row spacing (24px)
        JPanel fieldsGrid = new JPanel(new GridLayout(4, 2, 32, 24));
        fieldsGrid.setOpaque(false);

        // 1. Department
        DepartmentItem[] depts = {
            new DepartmentItem(1, "Department of ICT"),
            new DepartmentItem(2, "Department of ET"),
            new DepartmentItem(3, "Department of BST"),
            new DepartmentItem(4, "Department of MDS")
        };
        formDeptCombo = new JComboBox<>(depts);
        styleModernComboBox(formDeptCombo);
        fieldsGrid.add(createModernFormItem("Faculty Department", formDeptCombo));

        // 2. Academic Year / Level
        Integer[] years = {1, 2, 3, 4};
        formYearCombo = new JComboBox<>(years);
        formYearCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText("Level " + value + " (Year " + value + ")");
                setBorder(new EmptyBorder(6, 12, 6, 12));
                return this;
            }
        });
        styleModernComboBox(formYearCombo);
        fieldsGrid.add(createModernFormItem("Academic Year / Level", formYearCombo));

        // 3. Semester
        Integer[] sems = {1, 2};
        formSemCombo = new JComboBox<>(sems);
        formSemCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText("Semester " + value);
                setBorder(new EmptyBorder(6, 12, 6, 12));
                return this;
            }
        });
        styleModernComboBox(formSemCombo);
        fieldsGrid.add(createModernFormItem("Semester", formSemCombo));

        // 4. Day of the Week
        formDayCombo = new JComboBox<>(DAYS);
        styleModernComboBox(formDayCombo);
        fieldsGrid.add(createModernFormItem("Day of the Week", formDayCombo));

        // 5. Time Slot - Hourly & multi-hour slots
        String[] allAvailableSlots = {
            "8:00 - 9:00",
            "9:00 - 10:00",
            "10:00 - 11:00",
            "11:00 - 12:00",
            "12:00 - 13:00",
            "13:00 - 14:00",
            "14:00 - 15:00",
            "15:00 - 16:00",
            "16:00 - 17:00",
            "17:00 - 18:00",
            "18:00 - 19:00",
            "8:00 - 10:00",
            "10:00 - 12:00",
            "12:00 - 14:00",
            "14:00 - 16:00",
            "16:00 - 18:00",
            "8:00 - 11:00",
            "9:00 - 12:00",
            "13:00 - 16:00",
            "14:00 - 17:00",
            "16:00 - 19:00",
            "8:00 - 12:00",
            "13:00 - 17:00",
            "14:00 - 18:00",
            "15:00 - 19:00"
        };
        formTimeCombo = new JComboBox<>(allAvailableSlots);
        formTimeCombo.setEditable(true);
        styleModernComboBox(formTimeCombo);
        fieldsGrid.add(createModernFormItem("Class Time Slot (8:00 - 19:00)", formTimeCombo));

        // 6. Subject / Course (loaded from CourseDAO)
        formCourseCombo = new JComboBox<>();
        styleModernComboBox(formCourseCombo);
        refreshCoursesInForm();
        fieldsGrid.add(createModernFormItem("Subject / Course Code", formCourseCombo));

        // 7. Session Type (Lecture, Practical, Lab, Tutorial, Other)
        String[] types = {"Lecture", "Practical", "Lab", "Tutorial", "Other"};
        formTypeCombo = new JComboBox<>(types);
        styleModernComboBox(formTypeCombo);
        fieldsGrid.add(createModernFormItem("Session Type", formTypeCombo));

        // 8. Hall / Venue
        String[] halls = {"LT 1", "LT 2", "LT 3", "Lab 1", "Lab 2", "Drawing Office", "Main Auditorium"};
        formHallCombo = new JComboBox<>(halls);
        formHallCombo.setEditable(true);
        styleModernComboBox(formHallCombo);
        fieldsGrid.add(createModernFormItem("Lecture Hall / Venue", formHallCombo));

        cardBox.add(fieldsGrid, BorderLayout.CENTER);

        // Action Buttons Bar at Bottom of Card
        JPanel actionBar = new JPanel(new BorderLayout());
        actionBar.setOpaque(false);
        actionBar.setBorder(new EmptyBorder(18, 0, 0, 0));

        // Delete button on the left (only when editing)
        formDeleteBtn = new JButton("Delete Class Slot");
        formDeleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formDeleteBtn.setForeground(new Color(220, 38, 38));
        formDeleteBtn.setBackground(new Color(254, 242, 242));
        formDeleteBtn.setFocusPainted(false);
        formDeleteBtn.setBorderPainted(false);
        formDeleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        formDeleteBtn.setPreferredSize(new Dimension(155, 42));
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
        formCancelBtn.setForeground(new Color(71, 85, 105));
        formCancelBtn.setBackground(Color.WHITE);
        formCancelBtn.setFocusPainted(false);
        formCancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        formCancelBtn.setPreferredSize(new Dimension(110, 42));
        formCancelBtn.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(203, 213, 225), 10, 1),
            new EmptyBorder(8, 18, 8, 18)
        ));
        formCancelBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                formCancelBtn.setBackground(new Color(241, 245, 249));
                formCancelBtn.setForeground(new Color(15, 23, 42));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                formCancelBtn.setBackground(Color.WHITE);
                formCancelBtn.setForeground(new Color(71, 85, 105));
            }
        });
        formCancelBtn.addActionListener(e -> returnToTimetableGrid());

        formSaveBtn = new JButton("Save Timetable Entry");
        formSaveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formSaveBtn.setForeground(Color.WHITE);
        formSaveBtn.setBackground(new Color(24, 119, 242));
        formSaveBtn.setFocusPainted(false);
        formSaveBtn.setBorderPainted(false);
        formSaveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        formSaveBtn.setPreferredSize(new Dimension(195, 42));
        formSaveBtn.setBorder(new EmptyBorder(10, 22, 10, 22));
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

    /**
     * Modern Form Field Container with typography & clear spacing
     */
    private JPanel createModernFormItem(String labelText, JComponent field) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(30, 41, 59)); // Slate 800
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        wrapper.add(label);
        wrapper.add(Box.createVerticalStrut(8));
        wrapper.add(field);

        return wrapper;
    }

    /**
     * Custom Modern ComboBox Styling:
     * - Flat white rounded pill
     * - Subtle slate border
     * - Minimalist arrow
     * - Clean padding
     */
    private void styleModernComboBox(JComboBox<?> box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        box.setBackground(Color.WHITE);
        box.setForeground(new Color(15, 23, 42)); // Slate 900
        box.setPreferredSize(new Dimension(320, 44));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        box.setFocusable(true);

        // Modern flat UI
        box.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        // Draw clean modern chevron arrow
                        int w = getWidth();
                        int h = getHeight();
                        int size = 5;
                        int x = (w - size * 2) / 2;
                        int y = (h - size) / 2;

                        g2.setColor(new Color(100, 116, 139)); // Slate 500
                        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawLine(x, y, x + size, y + size);
                        g2.drawLine(x + size, y + size, x + size * 2, y);

                        g2.dispose();
                    }
                };
                button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
                button.setContentAreaFilled(false);
                button.setFocusPainted(false);
                button.setOpaque(false);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
                return button;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                // Keep clean flat white
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.WHITE);
                g2.fill(bounds);
                g2.dispose();
            }
        });

        // Set rounded border with soft gray outline
        box.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(214, 222, 232), 10, 1),
            new EmptyBorder(4, 12, 4, 6)
        ));

        // Style the popup menu list
        Object comp = box.getUI().getAccessibleChild(box, 0);
        if (comp instanceof JPopupMenu) {
            JPopupMenu popup = (JPopupMenu) comp;
            popup.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(226, 232, 240), 10, 1),
                new EmptyBorder(4, 4, 4, 4)
            ));
            popup.setBackground(Color.WHITE);
        }

        // Focus outline transition
        box.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                box.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(new Color(24, 119, 242), 10, 1),
                    new EmptyBorder(4, 12, 4, 6)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                box.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(new Color(214, 222, 232), 10, 1),
                    new EmptyBorder(4, 12, 4, 6)
                ));
            }
        });
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
        formSubtitleLabel.setText("Configure schedule parameters, assign venue, and allocate course timing");
        formSaveBtn.setText("Save Timetable Entry");
        formDeleteBtn.setVisible(false);

        refreshCoursesInForm();

        // Sync with currently selected header department filter if specific
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

        // Sync with currently selected level filter if specific
        LevelItem selLevel = (LevelItem) levelCombo.getSelectedItem();
        if (selLevel != null && selLevel.level > 0) {
            formYearCombo.setSelectedItem(selLevel.level);
        } else {
            formYearCombo.setSelectedItem(1);
        }

        // Sync with currently selected semester filter if specific
        SemesterItem selSem = (SemesterItem) semCombo.getSelectedItem();
        if (selSem != null && selSem.semester > 0) {
            formSemCombo.setSelectedItem(selSem.semester);
        } else {
            formSemCombo.setSelectedItem(1);
        }

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

        // Session Type (handles Practical, Lab, Lecture, Tutorial, Other)
        if (entry.getSessionType() != null) {
            String stype = entry.getSessionType().trim();
            boolean found = false;
            for (int i = 0; i < formTypeCombo.getItemCount(); i++) {
                if (formTypeCombo.getItemAt(i).equalsIgnoreCase(stype)) {
                    formTypeCombo.setSelectedIndex(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                formTypeCombo.addItem(stype);
                formTypeCombo.setSelectedItem(stype);
            }
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
        if (slotStr == null || !slotStr.contains("-")) {
            JOptionPane.showMessageDialog(this, "Please specify a valid time slot like '8:00 - 9:00' or '8:00 - 10:00'.", "Invalid Time", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String type = (String) formTypeCombo.getSelectedItem();
        String hall = (String) formHallCombo.getSelectedItem();
        if (hall != null) hall = hall.trim();
        if (hall == null || hall.isEmpty()) hall = "LT 1";

        String[] times = slotStr.split("-");
        String startSql = formatSqlTime(times[0].trim());
        String endSql = formatSqlTime(times[1].trim());

        if (editingEntryId == null) {
            // Create New Entry in Database
            int targetTtId = TimetableDAO.getOrCreateTimetable(deptId, year, sem);
            boolean success = TimetableDAO.addEntry(targetTtId, selCourse.id, day, startSql, endSql, hall, type);
            if (success) {
                JOptionPane.showMessageDialog(this, "Timetable entry created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                // Ensure newly added entry is visible by matching filter if needed
                LevelItem currentLevel = (LevelItem) levelCombo.getSelectedItem();
                if (currentLevel != null && currentLevel.level > 0 && currentLevel.level != year) {
                    levelCombo.setSelectedIndex(0); // All Levels
                }

                loadTimetableForSelectedFilters();
                returnToTimetableGrid();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create timetable entry. Please verify inputs.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // Update Existing Entry in Database
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
        if (t == null) return "08:00:00";
        t = t.trim();
        if (!t.contains(":")) return String.format("%02d:00:00", Integer.parseInt(t));
        String[] parts = t.split(":");
        int h = Integer.parseInt(parts[0].trim());
        int m = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
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

    private static class LevelItem {
        int level;
        String title;
        LevelItem(int level, String title) { this.level = level; this.title = title; }
        @Override public String toString() { return title; }
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

    /**
     * Sleek, modern scrollbar for cards and grid scrolling
     */
    private static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(203, 213, 225);
            this.trackColor = new Color(245, 247, 250);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            btn.setMinimumSize(new Dimension(0, 0));
            btn.setMaximumSize(new Dimension(0, 0));
            return btn;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(185, 198, 214));
            g2.fill(new RoundRectangle2D.Float(thumbBounds.x + 1, thumbBounds.y + 1, thumbBounds.width - 2, thumbBounds.height - 2, 4, 4));
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(241, 245, 249));
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g2.dispose();
        }
    }
}
