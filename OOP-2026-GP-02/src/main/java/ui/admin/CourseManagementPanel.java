package main.java.ui.admin;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import main.java.model.admin.Course;
import main.java.model.admin.CourseLecturerAssignment;
import main.java.dao.admin.CourseDAO;

public class CourseManagementPanel extends JPanel {
    private CardLayout cardLayout;
    private JLabel showingLabel;
    private JTable table;
    private JScrollPane courseTableScroll;
    
    private JPanel listPanel;
    private JPanel formPanel;
    
    // Course Form fields
    private JTextField codeField;
    private JTextField nameField;
    private JTextField creditField;
    private JComboBox<String> typeCombo;
    private JComboBox<String> semCombo;
    private JComboBox<String> yearCombo;

    // Assignment Section components
    private JComboBox<CourseComboItem> assignCourseCombo;
    private JComboBox<CourseDAO.LecturerOption> assignLecturerCombo;
    private JTable assignmentTable;
    private JScrollPane aScroll;
    private JLabel showingAssignmentsLabel;

    public static class CourseComboItem {
        private final int courseId;
        private final String courseCode;
        private final String courseName;

        public CourseComboItem(int courseId, String courseCode, String courseName) {
            this.courseId = courseId;
            this.courseCode = courseCode;
            this.courseName = courseName;
        }

        public int getCourseId() { return courseId; }
        public String getCourseCode() { return courseCode; }
        public String getCourseName() { return courseName; }

        @Override
        public String toString() {
            return courseCode + " - " + courseName;
        }
    }

    /**
     * Custom panel that tracks viewport width to prevent horizontal overflow in JScrollPane.
     */
    private static class ScrollablePanel extends JPanel implements Scrollable {
        public ScrollablePanel() {
            super();
        }

        public ScrollablePanel(LayoutManager layout) {
            super(layout);
        }

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

    public CourseManagementPanel() {
        cardLayout = new CardLayout();
        setLayout(cardLayout);
        setBackground(new Color(245, 247, 250));
        
        buildListPanel();
        buildFormPanel();
        
        add(listPanel, "List");
        add(formPanel, "Form");
        
        cardLayout.show(this, "List");
    }
    
    private void buildListPanel() {
        listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(new Color(245, 247, 250));
        
        // --- Header Section ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(25, 25, 20, 25));
        
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Course Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(new Color(30, 40, 50));
        
        JLabel subtitleLabel = new JLabel("View and manage academic courses");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(120, 130, 140));
        
        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(5));
        titleBlock.add(subtitleLabel);
        
        JButton addCourseBtn = new JButton("  + Add Course  ");
        addCourseBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addCourseBtn.setForeground(Color.WHITE);
        addCourseBtn.setBackground(new Color(21, 101, 192));
        addCourseBtn.setFocusPainted(false);
        addCourseBtn.setBorderPainted(false);
        addCourseBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addCourseBtn.setPreferredSize(new Dimension(135, 36));
        addCourseBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { addCourseBtn.setBackground(new Color(13, 71, 161)); }
            public void mouseExited(java.awt.event.MouseEvent e) { addCourseBtn.setBackground(new Color(21, 101, 192)); }
        });
        addCourseBtn.addActionListener(e -> {
            clearForm();
            cardLayout.show(this, "Form");
        });
        
        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(addCourseBtn, BorderLayout.EAST);
        
        // --- Scrollable Main Content Section ---
        ScrollablePanel scrollContent = new ScrollablePanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setOpaque(false);
        scrollContent.setBorder(new EmptyBorder(0, 25, 25, 25));
        
        // 1. Courses Table Card
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            new EmptyBorder(0, 0, 0, 0)
        ));
        
        String[] columns = {"ID", "Course Code", "Course Name", "Credits", "Type", "Year/Sem", "Actions"};
        DefaultTableModel model = new DefaultTableModel(null, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 6; }
        };
        
        table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setRowHeight(48);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(new Color(60, 70, 80));
        table.setSelectionBackground(new Color(240, 245, 250));
        table.setSelectionForeground(new Color(30, 40, 50));
        
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setBackground(Color.WHITE);
        tableHeader.setForeground(new Color(100, 110, 120));
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        tableHeader.setPreferredSize(new Dimension(100, 38));
        
        table.getColumnModel().getColumn(4).setCellRenderer(new TypeCellRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new ActionsCellRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionsCellEditor(table));
        
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(250);
        table.getColumnModel().getColumn(3).setPreferredWidth(60);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(65);
        
        int tableHeight = 240;
        courseTableScroll = new JScrollPane(table);
        courseTableScroll.setBorder(BorderFactory.createEmptyBorder());
        courseTableScroll.getViewport().setBackground(Color.WHITE);
        courseTableScroll.setPreferredSize(new Dimension(100, tableHeight));
        courseTableScroll.setMinimumSize(new Dimension(100, tableHeight));
        courseTableScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, tableHeight));
        courseTableScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        courseTableScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        tableWrapper.setPreferredSize(new Dimension(100, tableHeight));
        tableWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, tableHeight));
        tableWrapper.add(courseTableScroll, BorderLayout.CENTER);
        
        // Table Footer
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(10, 4, 15, 4));
        
        showingLabel = new JLabel("Showing courses");
        showingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        showingLabel.setForeground(new Color(120, 130, 140));
        footerPanel.add(showingLabel, BorderLayout.WEST);

        int totalCardHeight = tableHeight + 45;
        JPanel tableCardContainer = new JPanel(new BorderLayout());
        tableCardContainer.setOpaque(false);
        tableCardContainer.setPreferredSize(new Dimension(100, totalCardHeight));
        tableCardContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, totalCardHeight));
        tableCardContainer.add(tableWrapper, BorderLayout.CENTER);
        tableCardContainer.add(footerPanel, BorderLayout.SOUTH);
        tableCardContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        scrollContent.add(tableCardContainer);
        scrollContent.add(Box.createVerticalStrut(10));

        // 2. Assign Lecturer to Course Card
        JPanel assignCard = buildAssignLecturerCard();
        assignCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollContent.add(assignCard);
        scrollContent.add(Box.createVerticalGlue());

        JScrollPane mainScrollPane = new JScrollPane(scrollContent);
        mainScrollPane.setBorder(null);
        mainScrollPane.setOpaque(false);
        mainScrollPane.getViewport().setOpaque(false);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        setupSmoothWheelScrolling(courseTableScroll, mainScrollPane);
        setupSmoothWheelScrolling(aScroll, mainScrollPane);

        listPanel.add(headerPanel, BorderLayout.NORTH);
        listPanel.add(mainScrollPane, BorderLayout.CENTER);
        
        refreshTableData();
    }

    /**
     * Forwards mouse wheel events from an inner scroll pane to the parent scroll pane
     * when the inner scroll pane reaches its top/bottom boundaries or does not need scrolling.
     */
    private void setupSmoothWheelScrolling(JScrollPane innerScroll, JScrollPane parentScroll) {
        if (innerScroll == null || parentScroll == null) return;
        innerScroll.getVerticalScrollBar().setUnitIncrement(16);
        innerScroll.getVerticalScrollBar().setBlockIncrement(48);
        innerScroll.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            @Override
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
                JScrollBar bar = innerScroll.getVerticalScrollBar();
                if (bar == null || !bar.isVisible() || !bar.isEnabled()) {
                    Point pt = SwingUtilities.convertPoint(innerScroll, e.getPoint(), parentScroll);
                    java.awt.event.MouseWheelEvent parentEvent = new java.awt.event.MouseWheelEvent(
                        parentScroll, e.getID(), e.getWhen(), e.getModifiersEx(),
                        pt.x, pt.y, e.getClickCount(), e.isPopupTrigger(),
                        e.getScrollType(), e.getScrollAmount(), e.getWheelRotation()
                    );
                    parentScroll.dispatchEvent(parentEvent);
                    return;
                }
                int rotation = e.getWheelRotation();
                boolean atTop = bar.getValue() <= bar.getMinimum();
                boolean atBottom = bar.getValue() + bar.getModel().getExtent() >= bar.getMaximum();
                if ((rotation < 0 && atTop) || (rotation > 0 && atBottom)) {
                    Point pt = SwingUtilities.convertPoint(innerScroll, e.getPoint(), parentScroll);
                    java.awt.event.MouseWheelEvent parentEvent = new java.awt.event.MouseWheelEvent(
                        parentScroll, e.getID(), e.getWhen(), e.getModifiersEx(),
                        pt.x, pt.y, e.getClickCount(), e.isPopupTrigger(),
                        e.getScrollType(), e.getScrollAmount(), e.getWheelRotation()
                    );
                    parentScroll.dispatchEvent(parentEvent);
                }
            }
        });
    }

    /**
     * Builds the modern Card panel for assigning registered lecturers to courses.
     */
    private JPanel buildAssignLecturerCard() {
        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            new EmptyBorder(22, 24, 22, 24)
        ));

        // Top: Card Header
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLbl = new JLabel("Assign Lecturer to Course");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLbl.setForeground(new Color(30, 40, 50));

        JLabel subtitleLbl = new JLabel("Select a registered course module and assign an academic lecturer");
        subtitleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLbl.setForeground(new Color(120, 130, 140));

        titleBlock.add(titleLbl);
        titleBlock.add(Box.createVerticalStrut(3));
        titleBlock.add(subtitleLbl);

        showingAssignmentsLabel = new JLabel("Active Assignments: 0");
        showingAssignmentsLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        showingAssignmentsLabel.setForeground(new Color(21, 101, 192));

        cardHeader.add(titleBlock, BorderLayout.CENTER);
        cardHeader.add(showingAssignmentsLabel, BorderLayout.EAST);

        // Center: Selection Form & Active Assignments Table
        JPanel bodyPanel = new JPanel(new BorderLayout(0, 12));
        bodyPanel.setOpaque(false);

        // Selection Controls Row
        JPanel controlsRow = new JPanel(new GridBagLayout());
        controlsRow.setOpaque(false);
        controlsRow.setBorder(new EmptyBorder(0, 0, 12, 0));
        controlsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;

        // Course Selector
        JPanel courseBox = new JPanel(new BorderLayout(0, 6));
        courseBox.setOpaque(false);
        JLabel cLbl = new JLabel("Select Course *");
        cLbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        cLbl.setForeground(new Color(70, 80, 90));
        assignCourseCombo = new JComboBox<>();
        assignCourseCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        assignCourseCombo.setBackground(Color.WHITE);
        assignCourseCombo.setPreferredSize(new Dimension(100, 36));
        courseBox.add(cLbl, BorderLayout.NORTH);
        courseBox.add(assignCourseCombo, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.weightx = 0.44;
        gbc.insets = new Insets(0, 0, 0, 12);
        controlsRow.add(courseBox, gbc);

        // Lecturer Selector
        JPanel lecturerBox = new JPanel(new BorderLayout(0, 6));
        lecturerBox.setOpaque(false);
        JLabel lLbl = new JLabel("Select Lecturer *");
        lLbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        lLbl.setForeground(new Color(70, 80, 90));
        assignLecturerCombo = new JComboBox<>();
        assignLecturerCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        assignLecturerCombo.setBackground(Color.WHITE);
        assignLecturerCombo.setPreferredSize(new Dimension(100, 36));
        lecturerBox.add(lLbl, BorderLayout.NORTH);
        lecturerBox.add(assignLecturerCombo, BorderLayout.CENTER);

        gbc.gridx = 1;
        gbc.weightx = 0.44;
        gbc.insets = new Insets(0, 0, 0, 12);
        controlsRow.add(lecturerBox, gbc);

        // Assign Button
        JPanel btnBox = new JPanel(new BorderLayout(0, 6));
        btnBox.setOpaque(false);
        JLabel spaceLbl = new JLabel(" ");
        spaceLbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        JButton assignBtn = new JButton("Assign Lecturer");
        assignBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        assignBtn.setForeground(Color.WHITE);
        assignBtn.setBackground(new Color(21, 101, 192));
        assignBtn.setFocusPainted(false);
        assignBtn.setBorderPainted(false);
        assignBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        assignBtn.setPreferredSize(new Dimension(140, 36));
        assignBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { assignBtn.setBackground(new Color(13, 71, 161)); }
            public void mouseExited(java.awt.event.MouseEvent e) { assignBtn.setBackground(new Color(21, 101, 192)); }
        });
        assignBtn.addActionListener(e -> performAssignLecturer());
        btnBox.add(spaceLbl, BorderLayout.NORTH);
        btnBox.add(assignBtn, BorderLayout.CENTER);

        gbc.gridx = 2;
        gbc.weightx = 0.12;
        gbc.insets = new Insets(0, 0, 0, 0);
        controlsRow.add(btnBox, gbc);

        // Divider
        JPanel divider = new JPanel();
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setPreferredSize(new Dimension(100, 1));
        divider.setBackground(new Color(230, 235, 240));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Sub-title for Assignments Table
        JLabel subTableTitle = new JLabel("Current Course Assignments");
        subTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        subTableTitle.setForeground(new Color(40, 50, 60));
        subTableTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);
        topSection.add(controlsRow);
        topSection.add(divider);
        topSection.add(Box.createVerticalStrut(14));
        topSection.add(subTableTitle);

        bodyPanel.add(topSection, BorderLayout.NORTH);

        // Assignments Table
        String[] assignCols = {"ID", "Course Code", "Course Name", "Assigned Lecturer", "Designation", "Department", "Actions"};
        DefaultTableModel assignModel = new DefaultTableModel(null, assignCols) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 6; }
        };

        assignmentTable = new JTable(assignModel);
        assignmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        assignmentTable.setRowHeight(42);
        assignmentTable.setShowGrid(false);
        assignmentTable.setIntercellSpacing(new Dimension(0, 0));
        assignmentTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        assignmentTable.setForeground(new Color(60, 70, 80));
        assignmentTable.setSelectionBackground(new Color(240, 245, 250));
        assignmentTable.setSelectionForeground(new Color(30, 40, 50));

        JTableHeader aHeader = assignmentTable.getTableHeader();
        aHeader.setBackground(Color.WHITE);
        aHeader.setForeground(new Color(100, 110, 120));
        aHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        aHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        aHeader.setPreferredSize(new Dimension(100, 36));

        assignmentTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        assignmentTable.getColumnModel().getColumn(1).setPreferredWidth(85);
        assignmentTable.getColumnModel().getColumn(2).setPreferredWidth(210);
        assignmentTable.getColumnModel().getColumn(3).setPreferredWidth(180);
        assignmentTable.getColumnModel().getColumn(4).setPreferredWidth(125);
        assignmentTable.getColumnModel().getColumn(5).setPreferredWidth(135);
        assignmentTable.getColumnModel().getColumn(6).setPreferredWidth(60);

        assignmentTable.getColumnModel().getColumn(6).setCellRenderer(new AssignmentActionsCellRenderer());
        assignmentTable.getColumnModel().getColumn(6).setCellEditor(new AssignmentActionsCellEditor(assignmentTable));

        int assignTableHeight = 200;
        aScroll = new JScrollPane(assignmentTable);
        aScroll.setBorder(BorderFactory.createLineBorder(new Color(235, 238, 242), 1, true));
        aScroll.getViewport().setBackground(Color.WHITE);
        aScroll.setPreferredSize(new Dimension(100, assignTableHeight));
        aScroll.setMinimumSize(new Dimension(100, assignTableHeight));
        aScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, assignTableHeight));
        aScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        aScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        bodyPanel.add(aScroll, BorderLayout.CENTER);

        card.add(cardHeader, BorderLayout.NORTH);
        card.add(bodyPanel, BorderLayout.CENTER);
        return card;
    }

    private void performAssignLecturer() {
        CourseComboItem selCourse = (CourseComboItem) assignCourseCombo.getSelectedItem();
        CourseDAO.LecturerOption selLecturer = (CourseDAO.LecturerOption) assignLecturerCombo.getSelectedItem();

        if (selCourse == null || selLecturer == null) {
            ModernDialog.showMessageDialog(this, "Please select both a course and a lecturer.", "Selection Required", ModernDialog.WARNING_MESSAGE);
            return;
        }

        if (CourseDAO.isLecturerAssignedToCourse(selCourse.getCourseId(), selLecturer.getLecturerId())) {
            ModernDialog.showMessageDialog(this,
                selLecturer.getLecturerName() + " is already assigned to " + selCourse.getCourseCode() + " (" + selCourse.getCourseName() + ").",
                "Already Assigned", ModernDialog.WARNING_MESSAGE);
            return;
        }

        boolean success = CourseDAO.assignLecturerToCourse(selCourse.getCourseId(), selLecturer.getLecturerId());
        if (success) {
            ModernDialog.showMessageDialog(this,
                "Lecturer " + selLecturer.getLecturerName() + " successfully assigned to " + selCourse.getCourseCode() + "!",
                "Assignment Success", ModernDialog.INFORMATION_MESSAGE);
            refreshAssignmentsTable();
        } else {
            ModernDialog.showMessageDialog(this, "Failed to save assignment to database.", "Error", ModernDialog.ERROR_MESSAGE);
        }
    }
    
    private void buildFormPanel() {
        formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(new Color(245, 247, 250));
        
        // Form Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Add New Course");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(new Color(30, 40, 50));
        
        JLabel subtitleLabel = new JLabel("Enter course details below");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(120, 130, 140));
        
        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(5));
        titleBlock.add(subtitleLabel);
        
        JButton backBtn = new JButton("  Back to Courses  ");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backBtn.setForeground(new Color(100, 110, 120));
        backBtn.setBackground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> cardLayout.show(this, "List"));
        
        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(backBtn, BorderLayout.EAST);
        
        // Form Container
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setOpaque(false);
        
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 230), 1, true),
            new EmptyBorder(40, 50, 40, 50)
        ));
        
        JPanel grid = new JPanel(new GridLayout(6, 2, 20, 25));
        grid.setOpaque(false);
        
        Font labelFont = new Font("Segoe UI Semibold", Font.PLAIN, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 15);
        Color labelColor = new Color(70, 80, 90);
        
        javax.swing.border.Border fieldBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 215, 220), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
        
        codeField = new JTextField(); 
        codeField.setFont(fieldFont);
        codeField.setBorder(fieldBorder);
        
        nameField = new JTextField(); 
        nameField.setFont(fieldFont);
        nameField.setBorder(fieldBorder);
        
        creditField = new JTextField(); 
        creditField.setFont(fieldFont);
        creditField.setBorder(fieldBorder);
        
        String[] types = {"Theory_Only", "Practical_Only", "Theory_and_Practical"};
        typeCombo = new JComboBox<>(types); 
        typeCombo.setFont(fieldFont);
        typeCombo.setBackground(Color.WHITE);
        
        String[] sems = {"1", "2"};
        semCombo = new JComboBox<>(sems); 
        semCombo.setFont(fieldFont);
        semCombo.setBackground(Color.WHITE);
        
        String[] years = {"1", "2", "3", "4"};
        yearCombo = new JComboBox<>(years); 
        yearCombo.setFont(fieldFont);
        yearCombo.setBackground(Color.WHITE);
        
        JLabel l1 = new JLabel("Course Code"); l1.setFont(labelFont); l1.setForeground(labelColor);
        JLabel l2 = new JLabel("Course Name"); l2.setFont(labelFont); l2.setForeground(labelColor);
        JLabel l3 = new JLabel("Credits"); l3.setFont(labelFont); l3.setForeground(labelColor);
        JLabel l4 = new JLabel("Course Type"); l4.setFont(labelFont); l4.setForeground(labelColor);
        JLabel l5 = new JLabel("Semester"); l5.setFont(labelFont); l5.setForeground(labelColor);
        JLabel l6 = new JLabel("Academic Year"); l6.setFont(labelFont); l6.setForeground(labelColor);
        
        grid.add(l1); grid.add(codeField);
        grid.add(l2); grid.add(nameField);
        grid.add(l3); grid.add(creditField);
        grid.add(l4); grid.add(typeCombo);
        grid.add(l5); grid.add(semCombo);
        grid.add(l6); grid.add(yearCombo);
        
        box.add(grid);
        box.add(Box.createVerticalStrut(35));
        
        JButton saveBtn = new JButton("Save Course");
        saveBtn.setBackground(new Color(21, 101, 192));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        saveBtn.setFocusPainted(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setPreferredSize(new Dimension(150, 40));
        saveBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        saveBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { saveBtn.setBackground(new Color(13, 71, 161)); }
            public void mouseExited(java.awt.event.MouseEvent e) { saveBtn.setBackground(new Color(21, 101, 192)); }
        });
        
        saveBtn.addActionListener(e -> saveCourse());
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnPanel.add(saveBtn);
        box.add(btnPanel);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(box, BorderLayout.NORTH);
        
        formContainer.add(wrapper, gbc);
        
        JScrollPane sp = new JScrollPane(formContainer);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        
        formPanel.add(headerPanel, BorderLayout.NORTH);
        formPanel.add(sp, BorderLayout.CENTER);
    }
    
    private void clearForm() {
        codeField.setText("");
        nameField.setText("");
        creditField.setText("");
        typeCombo.setSelectedIndex(0);
        semCombo.setSelectedIndex(0);
        yearCombo.setSelectedIndex(0);
    }
    
    private void saveCourse() {
        try {
            String code = codeField.getText();
            String name = nameField.getText();
            int credit = Integer.parseInt(creditField.getText());
            String type = (String) typeCombo.getSelectedItem();
            int sem = Integer.parseInt((String) semCombo.getSelectedItem());
            int year = Integer.parseInt((String) yearCombo.getSelectedItem());
            
            if (code.isEmpty() || name.isEmpty()) {
                ModernDialog.showMessageDialog(this, "Please fill all required fields.", "Validation Error", ModernDialog.WARNING_MESSAGE);
                return;
            }
            
            if (CourseDAO.addCourse(1, code, name, credit, type, sem, year, null)) {
                ModernDialog.showMessageDialog(this, "Course added successfully!", "Success", ModernDialog.INFORMATION_MESSAGE);
                refreshTableData();
                cardLayout.show(this, "List");
            } else {
                ModernDialog.showMessageDialog(this, "Failed to add course.", "Error", ModernDialog.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            ModernDialog.showMessageDialog(this, "Invalid input. Check credits format.", "Input Error", ModernDialog.ERROR_MESSAGE);
        }
    }
    
    public void refreshTableData() {
        if (table == null) return;
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        List<Course> list = CourseDAO.getAllCourses();
        for (Course c : list) {
            String idStr = String.format("C%03d", c.getCourseId());
            String yearSem = "Y" + c.getAcademicYear() + " S" + c.getSemester();
            model.addRow(new Object[]{ idStr, c.getCourseCode(), c.getCourseName(), c.getCredit(), c.getCourseType(), yearSem, "" });
        }
        if (showingLabel != null) showingLabel.setText("Showing 1 - " + list.size() + " of " + list.size() + " courses");

        // Refresh Assignment Dropdowns
        refreshAssignDropdowns(list);

        // Refresh Assignments Table
        refreshAssignmentsTable();
    }

    private void refreshAssignDropdowns(List<Course> courseList) {
        if (assignCourseCombo != null) {
            assignCourseCombo.removeAllItems();
            if (courseList == null) courseList = CourseDAO.getAllCourses();
            for (Course c : courseList) {
                assignCourseCombo.addItem(new CourseComboItem(c.getCourseId(), c.getCourseCode(), c.getCourseName()));
            }
        }

        if (assignLecturerCombo != null) {
            assignLecturerCombo.removeAllItems();
            List<CourseDAO.LecturerOption> lecturers = CourseDAO.getAllLecturers();
            for (CourseDAO.LecturerOption l : lecturers) {
                assignLecturerCombo.addItem(l);
            }
        }
    }

    private void refreshAssignmentsTable() {
        if (assignmentTable == null) return;
        DefaultTableModel model = (DefaultTableModel) assignmentTable.getModel();
        model.setRowCount(0);
        List<CourseLecturerAssignment> list = CourseDAO.getAllCourseLecturerAssignments();
        for (CourseLecturerAssignment a : list) {
            String idStr = String.format("A%03d", a.getAssignmentId());
            model.addRow(new Object[]{
                idStr,
                a.getCourseCode(),
                a.getCourseName(),
                a.getLecturerName(),
                a.getDesignation() != null ? a.getDesignation() : "-",
                a.getDepartmentName() != null ? a.getDepartmentName() : "Faculty of Technology",
                ""
            });
        }
        if (showingAssignmentsLabel != null) {
            showingAssignmentsLabel.setText("Active Assignments: " + list.size());
        }
    }
    
    class TypeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            
            String valStr = value != null ? value.toString() : "";
            Color bgColor = new Color(240, 240, 240);
            Color fgColor = new Color(100, 100, 100);
            
            if (valStr.equals("Theory_Only")) { bgColor = new Color(230, 244, 255); fgColor = new Color(0, 102, 204); }
            else if (valStr.equals("Practical_Only")) { bgColor = new Color(255, 235, 238); fgColor = new Color(204, 0, 51); }
            else if (valStr.equals("Theory_and_Practical")) { bgColor = new Color(230, 248, 234); fgColor = new Color(34, 139, 34); }
            
            final Color fBgColor = bgColor;
            JLabel label = new JLabel(valStr.replace("_", " ")) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(fBgColor);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setForeground(fgColor);
            label.setBorder(new EmptyBorder(4, 12, 4, 12));
            panel.add(label);
            return panel;
        }
    }
    
    class ActionsCellRenderer extends DefaultTableCellRenderer {
        private ImageIcon deleteIconImg;
        public ActionsCellRenderer() {
            try {
                ImageIcon icon = new ImageIcon("src/main/resources/images/admin-icons/action_delete.png");
                deleteIconImg = new ImageIcon(icon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
            } catch (Exception e) {}
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            if (deleteIconImg != null) {
                JLabel del = new JLabel(deleteIconImg);
                del.setCursor(new Cursor(Cursor.HAND_CURSOR));
                panel.add(del);
            }
            return panel;
        }
    }
    
    class ActionsCellEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private JPanel panel;
        private int currentRow;
        public ActionsCellEditor(JTable table) {
            panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
            panel.setBackground(table.getSelectionBackground());
            try {
                ImageIcon icon = new ImageIcon("src/main/resources/images/admin-icons/action_delete.png");
                ImageIcon delIcon = new ImageIcon(icon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
                JLabel del = new JLabel(delIcon);
                del.setCursor(new Cursor(Cursor.HAND_CURSOR));
                del.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        fireEditingStopped();
                        String idStr = (String) table.getValueAt(currentRow, 0);
                        int id = Integer.parseInt(idStr.replaceAll("[^0-9]", ""));
                        int confirm = ModernDialog.showConfirmDialog(panel, "Delete course " + idStr + "?", "Confirm Delete", ModernDialog.YES_NO_OPTION);
                        if (confirm == ModernDialog.YES_OPTION) {
                            if (CourseDAO.deleteCourse(id)) {
                                refreshTableData();
                            } else {
                                ModernDialog.showMessageDialog(panel, "Failed to delete course.", "Error", ModernDialog.ERROR_MESSAGE);
                            }
                        }
                    }
                });
                panel.add(del);
            } catch(Exception e){}
        }
        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean isSelected, int row, int column) {
            this.currentRow = row;
            return panel;
        }
        @Override
        public Object getCellEditorValue() { return ""; }
    }

    class AssignmentActionsCellRenderer extends DefaultTableCellRenderer {
        private ImageIcon deleteIconImg;
        public AssignmentActionsCellRenderer() {
            try {
                ImageIcon icon = new ImageIcon("src/main/resources/images/admin-icons/action_delete.png");
                deleteIconImg = new ImageIcon(icon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
            } catch (Exception e) {}
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            if (deleteIconImg != null) {
                JLabel del = new JLabel(deleteIconImg);
                del.setCursor(new Cursor(Cursor.HAND_CURSOR));
                del.setToolTipText("Remove Assignment");
                panel.add(del);
            }
            return panel;
        }
    }

    class AssignmentActionsCellEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private JPanel panel;
        private int currentRow;
        public AssignmentActionsCellEditor(JTable table) {
            panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
            panel.setBackground(table.getSelectionBackground());
            try {
                ImageIcon icon = new ImageIcon("src/main/resources/images/admin-icons/action_delete.png");
                ImageIcon delIcon = new ImageIcon(icon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
                JLabel del = new JLabel(delIcon);
                del.setCursor(new Cursor(Cursor.HAND_CURSOR));
                del.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        fireEditingStopped();
                        String idStr = (String) table.getValueAt(currentRow, 0);
                        String courseCode = (String) table.getValueAt(currentRow, 1);
                        String lecturer = (String) table.getValueAt(currentRow, 3);
                        int assignmentId = Integer.parseInt(idStr.replaceAll("[^0-9]", ""));

                        int confirm = ModernDialog.showConfirmDialog(panel,
                            "Remove assignment for " + lecturer + " from " + courseCode + "?",
                            "Confirm Unassign", ModernDialog.YES_NO_OPTION);
                        if (confirm == ModernDialog.YES_OPTION) {
                            if (CourseDAO.removeLecturerAssignment(assignmentId)) {
                                refreshAssignmentsTable();
                            } else {
                                ModernDialog.showMessageDialog(panel, "Failed to remove course assignment.", "Error", ModernDialog.ERROR_MESSAGE);
                            }
                        }
                    }
                });
                panel.add(del);
            } catch(Exception e){}
        }
        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean isSelected, int row, int column) {
            this.currentRow = row;
            return panel;
        }
        @Override
        public Object getCellEditorValue() { return ""; }
    }
}
