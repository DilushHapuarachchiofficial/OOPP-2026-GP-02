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
import main.java.model.admin.Timetable;
import main.java.model.admin.TimetableEntry;

public class TimetableManagementPanel extends JPanel {

    // Filter controls
    private JComboBox<DepartmentItem> deptCombo;
    private JComboBox<SemesterItem> semCombo;

    // Grid components
    private JPanel gridContainer;
    private JPanel tablePanel;

    // Exactly 5 days and 4 slots as in the image
    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    private static final String[] TIME_SLOTS = {
        "8:00 - 10:00",
        "10:00 - 12:00",
        "12:00 - 14:00",
        "14:00 - 16:00"
    };

    // Current state
    private int currentTimetableId = -1;
    private List<TimetableEntry> currentEntries = new ArrayList<>();

    public TimetableManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));

        buildHeaderSection();
        buildContentSection();

        loadTimetableForSelectedFilters();
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

        JLabel subtitleLabel = new JLabel("View and manage faculty timetables");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(120, 134, 150));

        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(6));
        titleBlock.add(subtitleLabel);

        // Header Right: "All Departments", "All Semesters", "+ Add Timetable"
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

        // 3. "+ Add Timetable" button
        JButton addBtn = new JButton("+ Add Timetable");
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addBtn.setForeground(Color.WHITE);
        addBtn.setBackground(new Color(24, 119, 242)); // Modern vibrant blue
        addBtn.setFocusPainted(false);
        addBtn.setBorderPainted(false);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.setPreferredSize(new Dimension(156, 38));
        addBtn.setBorder(new EmptyBorder(8, 16, 8, 16));
        addBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                addBtn.setBackground(new Color(13, 98, 220));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                addBtn.setBackground(new Color(24, 119, 242));
            }
        });
        addBtn.addActionListener(e -> showAddEntryDialog(null, null));

        controlsPanel.add(deptCombo);
        controlsPanel.add(semCombo);
        controlsPanel.add(Box.createHorizontalStrut(4));
        controlsPanel.add(addBtn);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(controlsPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
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
                // Draw delicate full-span inner grid lines
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
        add(centerWrapper, BorderLayout.CENTER);
    }

    private JPanel createLegendPanel() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 26, 6));
        legend.setOpaque(false);
        legend.setBorder(new EmptyBorder(8, 4, 4, 4));

        legend.add(createLegendItem("Lecture", new Color(37, 99, 235)));   // Blue
        legend.add(createLegendItem("Lab", new Color(16, 185, 129)));      // Green
        legend.add(createLegendItem("Tutorial", new Color(245, 158, 11))); // Orange
        legend.add(createLegendItem("Other", new Color(192, 132, 252)));   // Light Purple / Pink

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

        // Query entries matching department and semester (or all)
        currentEntries = TimetableDAO.getEntries(deptId, 1, sem);
        if (currentEntries.isEmpty() && (deptId == 0 || sem == 0)) {
            // Also try any level/semester entries if none found
            currentEntries = TimetableDAO.getEntries(deptId, 0, sem);
        }

        renderScheduleGrid();
    }

    private void renderScheduleGrid() {
        tablePanel.removeAll();

        // Row 0: Headers
        // Column 0: Empty
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
            // Time label cell
            JPanel timeCell = new JPanel(new GridBagLayout());
            timeCell.setOpaque(false);
            JLabel timeLabel = new JLabel(slot);
            timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            timeLabel.setForeground(new Color(110, 125, 145));
            timeCell.add(timeLabel);
            tablePanel.add(timeCell);

            // Day cells for this slot
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

    /**
     * Card Cell: Center-padded with modern rounded pill card matching screenshot.
     */
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

                // Background
                g2.setColor(theme.background);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));

                // Optional subtle border
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

        // Subject / Course Code (Bold)
        JLabel codeLabel = new JLabel(entry.getCourseCode() != null ? entry.getCourseCode() : "N/A");
        codeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        codeLabel.setForeground(theme.text);
        codeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Hall / Location
        JLabel hallLabel = new JLabel(entry.getHall() != null ? entry.getHall() : "");
        hallLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hallLabel.setForeground(theme.text);
        hallLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(codeLabel);
        card.add(Box.createVerticalStrut(3));
        card.add(hallLabel);
        card.add(Box.createVerticalGlue());

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showSlotDetailsOrEditDialog(entry);
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

    /**
     * Empty slot cell with hover cue and click-to-add action.
     */
    private JPanel createEmptySlotCell(String day, String slotTime) {
        JPanel emptyCell = new JPanel();
        emptyCell.setOpaque(false);
        emptyCell.setCursor(new Cursor(Cursor.HAND_CURSOR));
        emptyCell.setToolTipText("Click to schedule a class for " + day + " (" + slotTime + ")");

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
                showAddEntryDialog(day, slotTime);
            }
        });

        return emptyCell;
    }

    /**
     * Soft pastel colors precisely matching the user's image:
     * - Lecture: Soft blue (#D4E4FC)
     * - Lab: Soft mint green (#D5F5E3)
     * - Tutorial: Soft orange/yellow (#FDEBD0)
     * - Other: Soft pink/lavender (#FCE4EC or #F3E5F5)
     */
    private SlotColorTheme getThemeForSessionType(String type) {
        if (type == null) type = "Lecture";
        switch (type.trim().toLowerCase()) {
            case "lab":
            case "practical":
                return new SlotColorTheme(
                    new Color(213, 245, 227), // Soft pastel green
                    new Color(187, 240, 209),
                    new Color(30, 130, 76)     // Deep green text
                );
            case "tutorial":
                return new SlotColorTheme(
                    new Color(254, 237, 214), // Soft pastel peach/amber
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

    /**
     * Add Class / Timetable Slot Modal Dialog
     */
    private void showAddEntryDialog(String defaultDay, String defaultSlot) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Timetable Entry", true);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(25, 30, 20, 30));

        JLabel infoLabel = new JLabel("Schedule a Class Slot for Timetable");
        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        infoLabel.setForeground(new Color(24, 119, 242));
        formPanel.add(infoLabel);
        formPanel.add(Box.createVerticalStrut(15));

        // 1. Day of Week
        JComboBox<String> dayBox = new JComboBox<>(DAYS);
        if (defaultDay != null) dayBox.setSelectedItem(defaultDay);
        addFormField(formPanel, "Day of the Week", dayBox);

        // 2. Time Slot
        JComboBox<String> timeBox = new JComboBox<>(TIME_SLOTS);
        if (defaultSlot != null) timeBox.setSelectedItem(defaultSlot);
        addFormField(formPanel, "Time Slot", timeBox);

        // 3. Courses
        List<Course> allCourses = CourseDAO.getAllCourses();
        JComboBox<CourseItem> courseBox = new JComboBox<>();
        for (Course c : allCourses) {
            courseBox.addItem(new CourseItem(c.getCourseId(), c.getCourseCode(), c.getCourseName()));
        }
        addFormField(formPanel, "Subject / Course", courseBox);

        // 4. Session Type (Lecture, Lab, Tutorial, Other)
        String[] types = {"Lecture", "Lab", "Tutorial", "Other"};
        JComboBox<String> typeBox = new JComboBox<>(types);
        addFormField(formPanel, "Session Type", typeBox);

        // 5. Hall / Venue
        String[] halls = {"LT 1", "LT 2", "LT 3", "Lab 1", "Lab 2", "Drawing Office", "Main Auditorium"};
        JComboBox<String> hallBox = new JComboBox<>(halls);
        hallBox.setEditable(true);
        addFormField(formPanel, "Hall / Venue", hallBox);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 15));
        btnPanel.setBackground(new Color(248, 250, 252));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton cancelBtn = new JButton("Cancel");
        styleSecondaryButton(cancelBtn);
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = new JButton("Save Class");
        stylePrimaryButton(saveBtn);
        saveBtn.addActionListener(e -> {
            CourseItem selCourse = (CourseItem) courseBox.getSelectedItem();
            if (selCourse == null) {
                JOptionPane.showMessageDialog(dialog, "Please select a course.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String day = (String) dayBox.getSelectedItem();
            String slotStr = (String) timeBox.getSelectedItem();
            String type = (String) typeBox.getSelectedItem();
            String hall = (String) hallBox.getSelectedItem();
            if (hall != null) hall = hall.trim();
            if (hall == null || hall.isEmpty()) hall = "LT 1";

            String[] times = slotStr.split(" - ");
            String startSql = formatSqlTime(times[0].trim());
            String endSql = formatSqlTime(times[1].trim());

            // Get target timetable_id (or create if needed)
            int targetTtId = 2; // Default to Level 1 Sem 2 timetable
            DepartmentItem deptItem = (DepartmentItem) deptCombo.getSelectedItem();
            if (deptItem != null && deptItem.id > 0) {
                targetTtId = TimetableDAO.getOrCreateTimetable(deptItem.id, 1, 2);
            }

            boolean success = TimetableDAO.addEntry(targetTtId, selCourse.id, day, startSql, endSql, hall, type);
            if (success) {
                JOptionPane.showMessageDialog(dialog, "Timetable entry saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadTimetableForSelectedFilters();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to save timetable entry.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Details & Edit/Delete dialog for existing slot
     */
    private void showSlotDetailsOrEditDialog(TimetableEntry entry) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Timetable Slot Details", true);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(25, 30, 20, 30));

        JLabel infoLabel = new JLabel("Class Slot #" + entry.getEntryId() + " • " + entry.getDayOfWeek());
        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        infoLabel.setForeground(new Color(24, 119, 242));
        formPanel.add(infoLabel);
        formPanel.add(Box.createVerticalStrut(15));

        // 1. Day of Week
        JComboBox<String> dayBox = new JComboBox<>(DAYS);
        dayBox.setSelectedItem(entry.getDayOfWeek());
        addFormField(formPanel, "Day of the Week", dayBox);

        // 2. Time Slot
        JComboBox<String> timeBox = new JComboBox<>(TIME_SLOTS);
        String currentSlotFormatted = entry.getTimeSlotFormatted();
        timeBox.setSelectedItem(currentSlotFormatted);
        addFormField(formPanel, "Time Slot", timeBox);

        // 3. Courses
        List<Course> allCourses = CourseDAO.getAllCourses();
        JComboBox<CourseItem> courseBox = new JComboBox<>();
        CourseItem selCourseItem = null;
        for (Course c : allCourses) {
            CourseItem item = new CourseItem(c.getCourseId(), c.getCourseCode(), c.getCourseName());
            courseBox.addItem(item);
            if (c.getCourseId() == entry.getCourseId()) {
                selCourseItem = item;
            }
        }
        if (selCourseItem != null) {
            courseBox.setSelectedItem(selCourseItem);
        } else {
            CourseItem custom = new CourseItem(entry.getCourseId(), entry.getCourseCode(), entry.getCourseName());
            courseBox.addItem(custom);
            courseBox.setSelectedItem(custom);
        }
        addFormField(formPanel, "Subject / Course", courseBox);

        // 4. Session Type
        String[] types = {"Lecture", "Lab", "Tutorial", "Other"};
        JComboBox<String> typeBox = new JComboBox<>(types);
        typeBox.setSelectedItem(entry.getSessionType());
        addFormField(formPanel, "Session Type", typeBox);

        // 5. Hall / Venue
        String[] halls = {"LT 1", "LT 2", "LT 3", "Lab 1", "Lab 2", "Drawing Office", "Main Auditorium"};
        JComboBox<String> hallBox = new JComboBox<>(halls);
        hallBox.setEditable(true);
        hallBox.setSelectedItem(entry.getHall());
        addFormField(formPanel, "Hall / Venue", hallBox);

        // Action Buttons: Delete on Left, Cancel & Update on Right
        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.setBackground(new Color(248, 250, 252));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton deleteBtn = new JButton("Delete Slot");
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deleteBtn.setForeground(new Color(220, 38, 38));
        deleteBtn.setBackground(new Color(254, 226, 226));
        deleteBtn.setFocusPainted(false);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.setBorder(new EmptyBorder(8, 16, 8, 16));
        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                dialog,
                "Delete slot for " + entry.getCourseCode() + " on " + entry.getDayOfWeek() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                if (TimetableDAO.deleteEntry(entry.getEntryId())) {
                    JOptionPane.showMessageDialog(dialog, "Slot deleted successfully!");
                    dialog.dispose();
                    loadTimetableForSelectedFilters();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to delete slot.");
                }
            }
        });

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        rightActions.setOpaque(false);

        JButton cancelBtn = new JButton("Cancel");
        styleSecondaryButton(cancelBtn);
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton updateBtn = new JButton("Update Class");
        stylePrimaryButton(updateBtn);
        updateBtn.addActionListener(e -> {
            CourseItem selCourse = (CourseItem) courseBox.getSelectedItem();
            if (selCourse == null) return;

            String day = (String) dayBox.getSelectedItem();
            String slotStr = (String) timeBox.getSelectedItem();
            String type = (String) typeBox.getSelectedItem();
            String hall = (String) hallBox.getSelectedItem();

            String[] times = slotStr.split(" - ");
            String startSql = formatSqlTime(times[0].trim());
            String endSql = formatSqlTime(times[1].trim());

            boolean success = TimetableDAO.updateEntry(entry.getEntryId(), selCourse.id, day, startSql, endSql, hall, type);
            if (success) {
                JOptionPane.showMessageDialog(dialog, "Slot updated successfully!");
                dialog.dispose();
                loadTimetableForSelectedFilters();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to update slot.");
            }
        });

        JPanel leftWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        leftWrapper.setOpaque(false);
        leftWrapper.add(deleteBtn);

        rightActions.add(cancelBtn);
        rightActions.add(updateBtn);

        btnPanel.add(leftWrapper, BorderLayout.WEST);
        btnPanel.add(rightActions, BorderLayout.EAST);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addFormField(JPanel parent, String labelText, JComponent component) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(51, 65, 85));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        component.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        component.setPreferredSize(new Dimension(400, 38));
        component.setAlignmentX(Component.LEFT_ALIGNMENT);

        parent.add(label);
        parent.add(Box.createVerticalStrut(6));
        parent.add(component);
        parent.add(Box.createVerticalStrut(14));
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

    private void stylePrimaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(24, 119, 242));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
    }

    private void styleSecondaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(100, 116, 139));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(203, 213, 225), 6, 1),
            new EmptyBorder(7, 16, 7, 16)
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
