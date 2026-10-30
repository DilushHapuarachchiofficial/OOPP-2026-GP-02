package main.java.ui.Undergraduate;

import main.java.dao.Undergraduate.LectureNotesDAO;
import main.java.model.Undergraduate.Course;
import main.java.model.Undergraduate.LectureNoteItem;
import main.java.model.Undergraduate.Undergraduate;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TecFAMS - Faculty of Technology Academic Management System
 * Undergraduate Lecture Notes UI Page.
 *
 * Displays lecture notes grouped course-by-course for each course
 * the logged-in student is enrolled in.
 * Supports loading and downloading files directly from the MySQL database
 * (course_materials.file_data LONGBLOB) without needing local disk files.
 */
public class UndergraduateLectureNotesUI extends JPanel {

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

    // Data references
    private final Undergraduate loggedInStudent;
    private final UndergraduateDashboard parentDashboard;
    private final String studentIdentifier;
    private final LectureNotesDAO lectureNotesDAO;

    // UI Components
    private JPanel mainContentPanel;
    private JLabel statusCountLabel;
    private JComboBox<String> semesterFilterCombo;
    private List<Course> allEnrolledCourses;
    private List<LectureNoteItem> allStudentNotes;

    public UndergraduateLectureNotesUI(Undergraduate student, UndergraduateDashboard parentDashboard) {
        this.loggedInStudent = student;
        this.parentDashboard = parentDashboard;
        this.studentIdentifier = resolveStudentIdentifier(student);
        this.lectureNotesDAO = new LectureNotesDAO();

        initializePanel();
        loadLectureNotesData();
    }

    public UndergraduateLectureNotesUI(Undergraduate student) {
        this(student, null);
    }

    public UndergraduateLectureNotesUI(String studentIdOrRegNo) {
        this.loggedInStudent = null;
        this.parentDashboard = null;
        this.studentIdentifier = (studentIdOrRegNo != null && !studentIdOrRegNo.trim().isEmpty())
                ? studentIdOrRegNo.trim()
                : "TG/2024/2085";
        this.lectureNotesDAO = new LectureNotesDAO();

        initializePanel();
        loadLectureNotesData();
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

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);

        // 1. Top Heading Section
        add(createHeadingPanel(), BorderLayout.NORTH);

        // 2. Center Dynamic Content Section
        mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBackground(COLOR_BG);
        mainContentPanel.setBorder(new EmptyBorder(8, 24, 12, 24));
        add(mainContentPanel, BorderLayout.CENTER);

        // 3. Bottom Action Bar
        add(createBottomActionPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeadingPanel() {
        JPanel headingBar = new JPanel(new BorderLayout(16, 0));
        headingBar.setBackground(COLOR_BG);
        headingBar.setBorder(new EmptyBorder(16, 24, 8, 24));

        JPanel leftBox = new JPanel(new GridLayout(2, 1, 0, 4));
        leftBox.setOpaque(false);

        JLabel pageHeading = new JLabel("My Course Lecture Notes");
        pageHeading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        pageHeading.setForeground(COLOR_PRIMARY_NAVY);

        statusCountLabel = new JLabel("Loading enrolled courses and lecture notes...");
        statusCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusCountLabel.setForeground(COLOR_TEXT_MUTED);

        leftBox.add(pageHeading);
        leftBox.add(statusCountLabel);
        headingBar.add(leftBox, BorderLayout.WEST);

        // Right Action Toolbar: Semester Dropdown Filter & Back Button
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        JLabel filterLabel = new JLabel("Select Semester:");
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterLabel.setForeground(COLOR_PRIMARY_NAVY);

        String[] semesterOptions = {
                "Level II - Semester I (Current)",
                "Level I - Semester I",
                "Level I - Semester II",
                "Level II - Semester II",
                "All Semesters"
        };
        semesterFilterCombo = new JComboBox<>(semesterOptions);
        semesterFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        semesterFilterCombo.setBackground(COLOR_WHITE);
        semesterFilterCombo.setPreferredSize(new Dimension(215, 32));
        semesterFilterCombo.setSelectedIndex(0); // Level II - Semester I (Current) by default
        semesterFilterCombo.addActionListener(e -> applySemesterFilter());

        rightActions.add(filterLabel);
        rightActions.add(semesterFilterCombo);

        if (parentDashboard != null) {
            JButton backBtn = new JButton("<- Back to Dashboard");
            backBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            backBtn.setForeground(COLOR_MAIN_BLUE);
            backBtn.setBackground(COLOR_WHITE);
            backBtn.setFocusPainted(false);
            backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            backBtn.setBorder(new CompoundBorder(
                    new LineBorder(COLOR_BORDER, 1, true),
                    new EmptyBorder(6, 12, 6, 12)
            ));
            backBtn.addActionListener(e -> parentDashboard.showDashboardView());
            rightActions.add(backBtn);
        }

        headingBar.add(rightActions, BorderLayout.EAST);
        return headingBar;
    }

    /**
     * Loads enrolled courses and groups lecture notes by each course.
     */
    public void loadLectureNotesData() {
        // 1. Fetch courses enrolled by this student from database
        allEnrolledCourses = lectureNotesDAO.getEnrolledCoursesForStudent(studentIdentifier);

        // 2. Fetch all lecture notes belonging to those enrolled courses
        allStudentNotes = lectureNotesDAO.getLectureNotesForStudent(studentIdentifier);

        // 3. Apply the semester filter (defaults to Level II - Semester I)
        applySemesterFilter();
    }

    /**
     * Filters courses and notes based on the semester selected in the dropdown.
     */
    public void applySemesterFilter() {
        if (mainContentPanel == null) return;
        mainContentPanel.removeAll();

        // Empty State: Student is not currently enrolled in any courses
        if (allEnrolledCourses == null || allEnrolledCourses.isEmpty()) {
            if (statusCountLabel != null) {
                statusCountLabel.setText("Enrollment Status: No registered courses found");
            }
            mainContentPanel.add(createEmptyStatePanel(
                    "You are not currently enrolled in any courses.",
                    "Please contact your Department Coordinator or Dean's Office to complete course registration."
            ), BorderLayout.CENTER);
            mainContentPanel.revalidate();
            mainContentPanel.repaint();
            return;
        }

        String selectedPeriod = (semesterFilterCombo != null && semesterFilterCombo.getSelectedItem() != null)
                ? (String) semesterFilterCombo.getSelectedItem()
                : "Level II - Semester I (Current)";

        int targetLevel = 0;
        int targetSemester = 0;

        if (selectedPeriod.startsWith("Level II - Semester I")) {
            targetLevel = 2;
            targetSemester = 1;
        } else if (selectedPeriod.startsWith("Level I - Semester I")) {
            targetLevel = 1;
            targetSemester = 1;
        } else if (selectedPeriod.startsWith("Level I - Semester II")) {
            targetLevel = 1;
            targetSemester = 2;
        } else if (selectedPeriod.startsWith("Level II - Semester II")) {
            targetLevel = 2;
            targetSemester = 2;
        }

        List<Course> filteredCourses = new ArrayList<>();
        for (Course c : allEnrolledCourses) {
            int cLvl = extractCourseLevel(c);
            int cSem = extractCourseSemester(c);
            if (targetLevel == 0 || (cLvl == targetLevel && cSem == targetSemester)) {
                filteredCourses.add(c);
            }
        }

        // Count notes available in the filtered courses
        int filteredNotesCount = 0;
        if (allStudentNotes != null) {
            for (Course c : filteredCourses) {
                for (LectureNoteItem note : allStudentNotes) {
                    if (note.getCourseCode() != null && note.getCourseCode().equalsIgnoreCase(c.getCourseCode())) {
                        filteredNotesCount++;
                    }
                }
            }
        }

        if (statusCountLabel != null) {
            statusCountLabel.setText("Showing: " + selectedPeriod +
                    "  |  Enrolled Courses: " + filteredCourses.size() +
                    "  |  Available Notes: " + filteredNotesCount + " document(s)");
        }

        if (filteredCourses.isEmpty()) {
            mainContentPanel.add(createEmptyStatePanel(
                    "No registered courses found for " + selectedPeriod + ".",
                    "You do not have any enrolled course units for the selected academic period."
            ), BorderLayout.CENTER);
        } else {
            JPanel courseCardsList = createCourseCardsListPanel(filteredCourses, allStudentNotes != null ? allStudentNotes : new ArrayList<>());
            JScrollPane scrollPane = new JScrollPane(courseCardsList);
            scrollPane.setBorder(null);
            scrollPane.setBackground(COLOR_BG);
            scrollPane.getViewport().setBackground(COLOR_BG);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            mainContentPanel.add(scrollPane, BorderLayout.CENTER);
        }

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    private int extractCourseLevel(Course c) {
        if (c.getAcademicYear() > 0) return c.getAcademicYear();
        String code = c.getCourseCode();
        if (code != null && code.length() >= 5) {
            char ch = code.charAt(3);
            if (Character.isDigit(ch)) return Character.getNumericValue(ch);
        }
        return 2;
    }

    private int extractCourseSemester(Course c) {
        if (c.getSemester() > 0) return c.getSemester();
        String code = c.getCourseCode();
        if (code != null && code.length() >= 5) {
            char ch = code.charAt(4);
            if (Character.isDigit(ch)) return Character.getNumericValue(ch);
        }
        return 1;
    }

    /**
     * Builds the vertical list of course cards/boxes.
     */
    private JPanel createCourseCardsListPanel(List<Course> courses, List<LectureNoteItem> allNotes) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        for (int i = 0; i < courses.size(); i++) {
            Course course = courses.get(i);

            // Filter lecture notes belonging specifically to this course
            List<LectureNoteItem> courseNotes = new ArrayList<>();
            for (LectureNoteItem note : allNotes) {
                if (note.getCourseCode() != null && note.getCourseCode().equalsIgnoreCase(course.getCourseCode())) {
                    courseNotes.add(note);
                }
            }

            // Create individual course card (box)
            JPanel courseCard = createIndividualCourseCard(course, courseNotes);
            courseCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            container.add(courseCard);

            if (i < courses.size() - 1) {
                container.add(Box.createVerticalStrut(16));
            }
        }

        return container;
    }

    /**
     * Creates a single course card (box) containing its course details
     * and the list of lecture notes with adjacent action buttons.
     */
    private JPanel createIndividualCourseCard(Course course, List<LectureNoteItem> notes) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(COLOR_WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        // 1. Course Box Header
        JPanel cardHeader = new JPanel(new BorderLayout(12, 0));
        cardHeader.setOpaque(false);
        cardHeader.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xED, 0xF2, 0xF7)),
                new EmptyBorder(0, 0, 10, 0)
        ));

        // Left Header: Course Code Badge + Course Name
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftHeader.setOpaque(false);

        JLabel codeBadge = new JLabel(" " + course.getCourseCode() + " ");
        codeBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        codeBadge.setForeground(COLOR_MAIN_BLUE);
        codeBadge.setBackground(COLOR_LIGHT_BLUE);
        codeBadge.setOpaque(true);
        codeBadge.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xBF, 0xDB, 0xFE), 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));

        JLabel nameLabel = new JLabel(course.getCourseName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLabel.setForeground(COLOR_PRIMARY_NAVY);

        int cLevel = extractCourseLevel(course);
        int cSem = extractCourseSemester(course);
        String semTag = "Level " + (cLevel == 1 ? "I" : "II") + " Sem " + (cSem == 1 ? "I" : "II");
        String info = (course.getCredits() > 0 ? course.getCredits() + " Credits" : "") +
                (course.getDepartment() != null ? " • " + course.getDepartment() : "") +
                " • " + semTag;
        JLabel deptLabel = new JLabel(info);
        deptLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        deptLabel.setForeground(COLOR_TEXT_MUTED);

        leftHeader.add(codeBadge);
        leftHeader.add(nameLabel);
        leftHeader.add(deptLabel);
        cardHeader.add(leftHeader, BorderLayout.WEST);

        // Right Header: Notes count badge
        String countText = notes.size() + (notes.size() == 1 ? " Lecture Note" : " Lecture Notes");
        JLabel countBadge = new JLabel(countText);
        countBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        countBadge.setForeground(notes.isEmpty() ? COLOR_TEXT_MUTED : COLOR_SUCCESS);
        cardHeader.add(countBadge, BorderLayout.EAST);

        card.add(cardHeader, BorderLayout.NORTH);

        // 2. Course Notes List
        JPanel notesListPanel = new JPanel();
        notesListPanel.setLayout(new BoxLayout(notesListPanel, BoxLayout.Y_AXIS));
        notesListPanel.setOpaque(false);

        if (notes.isEmpty()) {
            JPanel emptyNoteBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 6));
            emptyNoteBox.setOpaque(false);
            JLabel emptyMsg = new JLabel("No lecture notes have been uploaded for this course yet.");
            emptyMsg.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            emptyMsg.setForeground(COLOR_TEXT_MUTED);
            emptyNoteBox.add(emptyMsg);
            notesListPanel.add(emptyNoteBox);
        } else {
            for (int j = 0; j < notes.size(); j++) {
                LectureNoteItem note = notes.get(j);
                JPanel noteRow = createNoteRowItem(note, j + 1);
                noteRow.setAlignmentX(Component.LEFT_ALIGNMENT);
                notesListPanel.add(noteRow);

                if (j < notes.size() - 1) {
                    notesListPanel.add(Box.createVerticalStrut(8));
                }
            }
        }

        card.add(notesListPanel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Creates an individual lecture note row with title, metadata,
     * file size, and immediate Open/Download action buttons on the right.
     */
    private JPanel createNoteRowItem(LectureNoteItem note, int index) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setBackground(new Color(0xFA, 0xFC, 0xFF));
        row.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));

        // Left Content: Note Index Badge, Title, and Metadata
        JPanel leftPanel = new JPanel(new BorderLayout(12, 0));
        leftPanel.setOpaque(false);

        JLabel numLabel = new JLabel(String.valueOf(index));
        numLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        numLabel.setForeground(COLOR_TEXT_MUTED);
        numLabel.setPreferredSize(new Dimension(20, 20));
        leftPanel.add(numLabel, BorderLayout.WEST);

        JPanel detailsBox = new JPanel(new GridLayout(2, 1, 0, 2));
        detailsBox.setOpaque(false);

        JLabel titleLabel = new JLabel(note.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(COLOR_TEXT_PRIMARY);

        String sizeInfo = (note.getFormattedFileSize() != null && !note.getFormattedFileSize().isEmpty())
                ? "  |  Size: " + note.getFormattedFileSize() : "";
        String desc = (note.getDescription() != null && !note.getDescription().isEmpty())
                ? " • " + note.getDescription() : "";
        String metaText = "Lecturer: " + (note.getLecturerName() != null ? note.getLecturerName() : "Course Lecturer") +
                "  |  Type: " + note.getFileType() + sizeInfo +
                "  |  Uploaded: " + (note.getUploadedDate() != null ? note.getUploadedDate() : "N/A") + desc;
        JLabel metaLabel = new JLabel(metaText);
        metaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        metaLabel.setForeground(COLOR_TEXT_MUTED);

        detailsBox.add(titleLabel);
        detailsBox.add(metaLabel);
        leftPanel.add(detailsBox, BorderLayout.CENTER);

        row.add(leftPanel, BorderLayout.CENTER);

        // Right Content: Immediate Open Note & Download buttons beside the note
        JPanel actionBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBox.setOpaque(false);

        // 1. Download Note Button
        JButton downloadBtn = new JButton("Download") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(0xBF, 0xDB, 0xFE));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(0xDB, 0xEA, 0xFE));
                } else {
                    g2.setColor(COLOR_LIGHT_BLUE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(COLOR_MAIN_BLUE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        downloadBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        downloadBtn.setForeground(COLOR_MAIN_BLUE);
        downloadBtn.setContentAreaFilled(false);
        downloadBtn.setOpaque(false);
        downloadBtn.setBorderPainted(false);
        downloadBtn.setFocusPainted(false);
        downloadBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        downloadBtn.setBorder(new EmptyBorder(6, 14, 6, 14));
        downloadBtn.addActionListener(e -> downloadNote(note));
        actionBox.add(downloadBtn);

        // 2. Open Note Button (Solid Royal Blue with Crisp White Text)
        JButton openBtn = new JButton("Open Note") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(0x1E, 0x40, 0xAF)); // Dark blue on press
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(0x1D, 0x4E, 0xD8)); // Hover blue
                } else {
                    g2.setColor(COLOR_MAIN_BLUE); // #2563EB solid primary blue
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        openBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        openBtn.setForeground(Color.WHITE);
        openBtn.setContentAreaFilled(false);
        openBtn.setOpaque(false);
        openBtn.setBorderPainted(false);
        openBtn.setFocusPainted(false);
        openBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        openBtn.setBorder(new EmptyBorder(6, 14, 6, 14));
        openBtn.addActionListener(e -> openNote(note));
        actionBox.add(openBtn);

        row.add(actionBox, BorderLayout.EAST);
        return row;
    }

    private JPanel createEmptyStatePanel(String primaryMessage, String secondaryMessage) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(COLOR_WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(40, 40, 40, 40)
        ));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel msgLabel = new JLabel(primaryMessage);
        msgLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        msgLabel.setForeground(COLOR_TEXT_PRIMARY);
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel(secondaryMessage);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(COLOR_TEXT_MUTED);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton refreshBtn = new JButton("Check for Updates");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setForeground(COLOR_MAIN_BLUE);
        refreshBtn.setBackground(COLOR_LIGHT_BLUE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.setBorder(new CompoundBorder(
                new LineBorder(COLOR_MAIN_BLUE, 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        refreshBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        refreshBtn.addActionListener(e -> loadLectureNotesData());

        content.add(msgLabel);
        content.add(Box.createVerticalStrut(8));
        content.add(subLabel);
        content.add(Box.createVerticalStrut(18));
        content.add(refreshBtn);

        card.add(content);
        return card;
    }

    private JPanel createBottomActionPanel() {
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(COLOR_WHITE);
        bottomBar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER),
                new EmptyBorder(12, 24, 12, 24)
        ));

        JLabel hintLabel = new JLabel("Tip: Click 'Open Note' beside any lecture note to view it, or 'Download' to save a copy.");
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hintLabel.setForeground(COLOR_TEXT_MUTED);
        bottomBar.add(hintLabel, BorderLayout.WEST);

        JPanel buttonBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonBox.setOpaque(false);

        // 1. Back to Dashboard button
        if (parentDashboard != null) {
            JButton backBtn = createStyledButton("Back to Dashboard", COLOR_WHITE, COLOR_TEXT_PRIMARY, COLOR_BORDER);
            backBtn.addActionListener(e -> parentDashboard.showDashboardView());
            buttonBox.add(backBtn);
        }

        // 2. Refresh Button
        JButton refreshBtn = createStyledButton("Refresh", COLOR_WHITE, COLOR_TEXT_PRIMARY, COLOR_BORDER);
        refreshBtn.addActionListener(e -> loadLectureNotesData());
        buttonBox.add(refreshBtn);

        bottomBar.add(buttonBox, BorderLayout.EAST);
        return bottomBar;
    }

    private JButton createStyledButton(String text, Color bg, Color fg, Color border) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(Math.max(bg.getRed() - 20, 0), Math.max(bg.getGreen() - 20, 0), Math.max(bg.getBlue() - 20, 0)));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(Math.max(bg.getRed() - 8, 0), Math.max(bg.getGreen() - 8, 0), Math.max(bg.getBlue() - 8, 0)));
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
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
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(7, 16, 7, 16));
        return btn;
    }

    /**
     * Directly opens the specified lecture note file.
     * Fetches the file blob directly from the database (course_materials.file_data).
     * If found, writes to a temporary file and opens with default system application (e.g., PDF viewer, browser).
     */
    private void openNote(LectureNoteItem note) {
        if (note == null) return;

        File targetFile = null;

        // 1. First attempt to load binary data directly from MySQL database
        byte[] fileBytes = lectureNotesDAO.getMaterialFileData(note.getMaterialId());
        if (fileBytes != null && fileBytes.length > 0) {
            try {
                String safeName = (note.getFileName() != null && !note.getFileName().trim().isEmpty())
                        ? note.getFileName().trim() : "lecture_note_" + note.getMaterialId() + ".pdf";

                String prefix = "tecfams_";
                String suffix = ".pdf";
                int dotIdx = safeName.lastIndexOf('.');
                if (dotIdx > 0) {
                    prefix = safeName.substring(0, dotIdx);
                    suffix = safeName.substring(dotIdx);
                }
                if (prefix.length() < 3) {
                    prefix = "note_" + prefix;
                }

                targetFile = File.createTempFile(prefix + "_", suffix);
                targetFile.deleteOnExit();

                try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                    fos.write(fileBytes);
                    fos.flush();
                }
            } catch (IOException ex) {
                System.err.println("Could not create temp file for lecture note: " + ex.getMessage());
            }
        }

        // 2. Fallback to disk storage if not found in database
        if (targetFile == null || !targetFile.exists()) {
            String filePath = note.getFilePath();
            if (filePath != null && !filePath.trim().isEmpty()) {
                targetFile = resolveFile(filePath);
            }
        }

        if (targetFile == null || !targetFile.exists()) {
            JOptionPane.showMessageDialog(
                    this,
                    "The lecture note file could not be retrieved from the database.\n\n" +
                            "Note Title: " + note.getTitle() + "\n" +
                            "Course: " + note.getCourseCode() + " - " + note.getCourseName() + "\n" +
                            "File Name: " + note.getFileName() + "\n\n" +
                            "Please contact the lecturer (" + note.getLecturerName() + ") or Technical Officer.",
                    "File Not Found",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(targetFile);
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Your operating system does not support opening files directly.\n" +
                                "Saved to temporary location: " + targetFile.getAbsolutePath(),
                        "Action Unsupported",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error occurred while opening the file: " + ex.getMessage(),
                    "Open Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Directly downloads the specified lecture note file.
     * Fetches file bytes directly from MySQL database and saves it to user's selected location.
     */
    private void downloadNote(LectureNoteItem note) {
        if (note == null) return;

        String suggestedName = (note.getFileName() != null && !note.getFileName().trim().isEmpty())
                ? note.getFileName().trim() : (note.getTitle() + ".pdf");

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(suggestedName));
        fileChooser.setDialogTitle("Save Lecture Note As...");

        int choice = fileChooser.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File destinationFile = fileChooser.getSelectedFile();

            // 1. Fetch file bytes from database LONGBLOB
            byte[] fileBytes = lectureNotesDAO.getMaterialFileData(note.getMaterialId());
            boolean success = false;
            String errorMsg = null;

            if (fileBytes != null && fileBytes.length > 0) {
                try (FileOutputStream fos = new FileOutputStream(destinationFile)) {
                    fos.write(fileBytes);
                    fos.flush();
                    success = true;
                } catch (IOException ex) {
                    errorMsg = ex.getMessage();
                }
            } else {
                // 2. Fallback to disk file if available
                String filePath = note.getFilePath();
                File sourceFile = (filePath != null) ? resolveFile(filePath) : null;
                if (sourceFile != null && sourceFile.exists()) {
                    try (FileInputStream fis = new FileInputStream(sourceFile);
                         FileOutputStream fos = new FileOutputStream(destinationFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                        fos.flush();
                        success = true;
                    } catch (IOException ex) {
                        errorMsg = ex.getMessage();
                    }
                } else {
                    errorMsg = "Lecture note file data was not found in the database.";
                }
            }

            if (success) {
                JOptionPane.showMessageDialog(
                        this,
                        "Lecture note successfully downloaded to:\n" + destinationFile.getAbsolutePath(),
                        "Download Complete",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "An error occurred while saving the file: " + (errorMsg != null ? errorMsg : "Unknown error"),
                        "Download Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private File resolveFile(String filePath) {
        File file = new File(filePath);
        if (file.isAbsolute() && file.exists()) {
            return file;
        }
        File localFile = new File(System.getProperty("user.dir"), filePath);
        if (localFile.exists()) {
            return localFile;
        }
        return file;
    }
}
