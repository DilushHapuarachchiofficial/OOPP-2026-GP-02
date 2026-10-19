package main.java.ui.Undergraduate;

import main.java.dao.Undergraduate.LectureNotesDAO;
import main.java.model.Undergraduate.Course;
import main.java.model.Undergraduate.LectureNoteItem;
import main.java.model.Undergraduate.Undergraduate;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
 * This window displays lecture notes strictly for courses in which
 * the currently logged-in student is enrolled.
 *
 * Written in basic, clean, beginner-friendly Java Swing and OOP principles.
 */
public class UndergraduateLectureNotesUI extends JFrame {

    // TecFAMS Blue-and-White Theme Colors
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

    // Student identity and DAO
    private final Undergraduate loggedInStudent;
    private final String studentIdentifier;
    private final LectureNotesDAO lectureNotesDAO;

    // UI Components
    private JPanel mainContentPanel;
    private JTable notesTable;
    private DefaultTableModel tableModel;
    private JLabel statusCountLabel;
    private List<LectureNoteItem> displayedNotes;

    /**
     * Primary constructor: Receives the currently logged-in Undergraduate student model.
     *
     * @param student The logged-in undergraduate student object.
     */
    public UndergraduateLectureNotesUI(Undergraduate student) {
        this.loggedInStudent = student;
        this.studentIdentifier = resolveStudentIdentifier(student);
        this.lectureNotesDAO = new LectureNotesDAO();
        this.displayedNotes = new ArrayList<>();

        initializeWindow();
        loadLectureNotesData();
    }

    /**
     * Overloaded constructor: Allows passing a student ID or registration number directly.
     *
     * @param studentIdOrRegNo The student's ID or registration number (e.g., "TG/2024/2085").
     */
    public UndergraduateLectureNotesUI(String studentIdOrRegNo) {
        this.loggedInStudent = null;
        this.studentIdentifier = (studentIdOrRegNo != null && !studentIdOrRegNo.trim().isEmpty())
                ? studentIdOrRegNo.trim()
                : "TG/2024/2085";
        this.lectureNotesDAO = new LectureNotesDAO();
        this.displayedNotes = new ArrayList<>();

        initializeWindow();
        loadLectureNotesData();
    }

    /**
     * Default constructor for testing standalone in IntelliJ IDEA.
     * Uses default preview student if no active session is supplied.
     */
    public UndergraduateLectureNotesUI() {
        this((Undergraduate) null);
    }

    /**
     * Helper to get student ID or registration number from Undergraduate object.
     */
    private String resolveStudentIdentifier(Undergraduate student) {
        if (student == null) {
            return "TG/2024/2085"; // Default fallback test student
        }
        if (student.getRegistrationNo() != null && !student.getRegistrationNo().trim().isEmpty()) {
            return student.getRegistrationNo().trim();
        }
        if (student.getStudentId() != null && !student.getStudentId().trim().isEmpty()) {
            return student.getStudentId().trim();
        }
        return "TG/2024/2085";
    }

    /**
     * Sets up the JFrame properties and main layout structure.
     */
    private void initializeWindow() {
        setTitle("TecFAMS - Faculty of Technology | My Course Lecture Notes");
        setSize(1100, 680);
        setMinimumSize(new Dimension(850, 520));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(COLOR_BG);

        // Top Section: TecFAMS Header and Student Info
        rootPanel.add(createTopHeaderPanel(), BorderLayout.NORTH);

        // Center Section: Holds either the Notes Table or Empty State Message
        mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBackground(COLOR_BG);
        mainContentPanel.setBorder(new EmptyBorder(16, 24, 16, 24));
        rootPanel.add(mainContentPanel, BorderLayout.CENTER);

        // Bottom Section: Action buttons (Open, Download, Refresh, Close)
        rootPanel.add(createBottomActionPanel(), BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    /**
     * Creates the top header panel displaying "TecFAMS", "Lecture Notes",
     * student identity details, and heading "My Course Lecture Notes".
     */
    private JPanel createTopHeaderPanel() {
        JPanel headerContainer = new JPanel();
        headerContainer.setLayout(new BoxLayout(headerContainer, BoxLayout.Y_AXIS));
        headerContainer.setBackground(COLOR_WHITE);

        // 1. Top Brand Bar
        JPanel brandBar = new JPanel(new BorderLayout());
        brandBar.setBackground(COLOR_WHITE);
        brandBar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
                new EmptyBorder(14, 24, 14, 24)
        ));

        // Left Brand: TecFAMS & Lecture Notes
        JPanel leftBrand = new JPanel(new GridLayout(2, 1, 0, 2));
        leftBrand.setOpaque(false);

        JLabel brandTitle = new JLabel("TecFAMS");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brandTitle.setForeground(COLOR_PRIMARY_NAVY);

        JLabel brandSubtitle = new JLabel("Lecture Notes");
        brandSubtitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        brandSubtitle.setForeground(COLOR_MAIN_BLUE);

        leftBrand.add(brandTitle);
        leftBrand.add(brandSubtitle);
        brandBar.add(leftBrand, BorderLayout.WEST);

        // Right Student Info Badge
        JPanel rightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightInfo.setOpaque(false);

        String studentName = (loggedInStudent != null && loggedInStudent.getName() != null)
                ? loggedInStudent.getName()
                : "Undergraduate Student";
        String regNumber = studentIdentifier;

        JPanel studentBox = new JPanel(new GridLayout(2, 1, 0, 2));
        studentBox.setOpaque(false);

        JLabel nameLabel = new JLabel(studentName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLabel.setForeground(COLOR_TEXT_PRIMARY);

        JLabel regLabel = new JLabel("Reg No: " + regNumber + "  •  Undergraduate");
        regLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        regLabel.setForeground(COLOR_TEXT_MUTED);

        studentBox.add(nameLabel);
        studentBox.add(regLabel);

        rightInfo.add(studentBox);
        brandBar.add(rightInfo, BorderLayout.EAST);
        headerContainer.add(brandBar);

        // 2. Heading Section: "My Course Lecture Notes"
        JPanel headingBar = new JPanel(new BorderLayout());
        headingBar.setBackground(COLOR_BG);
        headingBar.setBorder(new EmptyBorder(16, 24, 6, 24));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 3));
        titleBox.setOpaque(false);

        JLabel pageHeading = new JLabel("My Course Lecture Notes");
        pageHeading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pageHeading.setForeground(COLOR_PRIMARY_NAVY);

        statusCountLabel = new JLabel("Loading enrolled courses and lecture notes...");
        statusCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusCountLabel.setForeground(COLOR_TEXT_MUTED);

        titleBox.add(pageHeading);
        titleBox.add(statusCountLabel);
        headingBar.add(titleBox, BorderLayout.WEST);

        headerContainer.add(headingBar);

        return headerContainer;
    }

    /**
     * Loads enrolled courses and their associated lecture notes from the database.
     * Enforces the business rules and displays proper empty state messages.
     */
    public void loadLectureNotesData() {
        mainContentPanel.removeAll();

        // 1. Retrieve enrolled courses for the logged-in student
        List<Course> enrolledCourses = lectureNotesDAO.getEnrolledCoursesForStudent(studentIdentifier);

        // Empty State 1: Student is not enrolled in any courses
        if (enrolledCourses == null || enrolledCourses.isEmpty()) {
            statusCountLabel.setText("Enrollment Status: No registered courses found");
            mainContentPanel.add(createEmptyStatePanel(
                    "You are not currently enrolled in any courses.",
                    "Please contact your Department Coordinator or Dean's Office to complete your course registration."
            ), BorderLayout.CENTER);
            mainContentPanel.revalidate();
            mainContentPanel.repaint();
            return;
        }

        // 2. Retrieve lecture notes strictly belonging to those enrolled courses
        displayedNotes = lectureNotesDAO.getLectureNotesForStudent(studentIdentifier);

        // Empty State 2: Student is enrolled in courses, but no notes uploaded yet
        if (displayedNotes == null || displayedNotes.isEmpty()) {
            statusCountLabel.setText("Enrolled Courses: " + enrolledCourses.size() + "  |  Available Notes: 0");
            mainContentPanel.add(createEmptyStatePanel(
                    "No lecture notes are available for your enrolled courses.",
                    "Your lecturers have not uploaded any notes or lab sheets for your registered courses yet."
            ), BorderLayout.CENTER);
            mainContentPanel.revalidate();
            mainContentPanel.repaint();
            return;
        }

        // 3. Notes are available: Display in clean JTable
        statusCountLabel.setText("Enrolled Courses: " + enrolledCourses.size() +
                "  |  Available Notes: " + displayedNotes.size() + " document(s)");

        JPanel tableCard = createTablePanel(displayedNotes);
        mainContentPanel.add(tableCard, BorderLayout.CENTER);

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    /**
     * Creates an empty state message panel.
     *
     * @param primaryMessage   The main requirement message.
     * @param secondaryMessage Additional helpful guidance for the student.
     * @return Styled JPanel.
     */
    private JPanel createEmptyStatePanel(String primaryMessage, String secondaryMessage) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(COLOR_WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(30, 30, 30, 30)
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

    /**
     * Builds the JTable inside a JScrollPane displaying the lecture notes.
     */
    private JPanel createTablePanel(List<LectureNoteItem> notes) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(COLOR_WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        // Column names matching required information:
        // Lecture note title, Course code, Course name, Lecturer name, File type, Uploaded date
        String[] columnNames = {
                "#",
                "Lecture Note Title",
                "Course Code",
                "Course Name",
                "Lecturer",
                "File Type",
                "Uploaded Date"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // View-only table
            }
        };

        // Populate table rows using simple for loop
        int rowNumber = 1;
        for (LectureNoteItem item : notes) {
            Object[] rowData = new Object[]{
                    rowNumber++,
                    item.getTitle(),
                    item.getCourseCode(),
                    item.getCourseName(),
                    item.getLecturerName(),
                    item.getFileType(),
                    item.getUploadedDate()
            };
            tableModel.addRow(rowData);
        }

        notesTable = new JTable(tableModel);
        notesTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        notesTable.setRowHeight(32);
        notesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        notesTable.setGridColor(new Color(0xED, 0xF2, 0xF7));
        notesTable.setShowVerticalLines(false);
        notesTable.setShowHorizontalLines(true);
        notesTable.setSelectionBackground(COLOR_LIGHT_BLUE);
        notesTable.setSelectionForeground(COLOR_PRIMARY_NAVY);

        // Header Styling
        JTableHeader header = notesTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(0xF1, 0xF5, 0xF9));
        header.setForeground(COLOR_PRIMARY_NAVY);
        header.setPreferredSize(new Dimension(header.getWidth(), 34));
        header.setReorderingAllowed(false);

        // Set column widths
        notesTable.getColumnModel().getColumn(0).setPreferredWidth(35);   // #
        notesTable.getColumnModel().getColumn(1).setPreferredWidth(260);  // Title
        notesTable.getColumnModel().getColumn(2).setPreferredWidth(90);   // Code
        notesTable.getColumnModel().getColumn(3).setPreferredWidth(210);  // Name
        notesTable.getColumnModel().getColumn(4).setPreferredWidth(160);  // Lecturer
        notesTable.getColumnModel().getColumn(5).setPreferredWidth(95);   // Type
        notesTable.getColumnModel().getColumn(6).setPreferredWidth(95);   // Date

        // Center align specific columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        notesTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        notesTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        notesTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        notesTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        // Double click on row to open note
        notesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    openSelectedNote();
                }
            }
        });

        // Default select the first row
        if (notesTable.getRowCount() > 0) {
            notesTable.setRowSelectionInterval(0, 0);
        }

        JScrollPane scrollPane = new JScrollPane(notesTable);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, COLOR_BORDER));
        scrollPane.getViewport().setBackground(COLOR_WHITE);

        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    /**
     * Bottom action bar containing Open Note, Download Note, Refresh, and Close buttons.
     */
    private JPanel createBottomActionPanel() {
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(COLOR_WHITE);
        bottomBar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER),
                new EmptyBorder(12, 24, 12, 24)
        ));

        // Left Hint
        JLabel hintLabel = new JLabel("Tip: Select a lecture note and click Open or Download (or double-click to open).");
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hintLabel.setForeground(COLOR_TEXT_MUTED);
        bottomBar.add(hintLabel, BorderLayout.WEST);

        // Right Action Buttons
        JPanel buttonBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonBox.setOpaque(false);

        // 1. Refresh Button
        JButton refreshBtn = createStyledButton("Refresh", COLOR_WHITE, COLOR_TEXT_PRIMARY, COLOR_BORDER);
        refreshBtn.addActionListener(e -> loadLectureNotesData());
        buttonBox.add(refreshBtn);

        // 2. Download Note Button
        JButton downloadBtn = createStyledButton("Download Note", COLOR_LIGHT_BLUE, COLOR_MAIN_BLUE, COLOR_MAIN_BLUE);
        downloadBtn.addActionListener(e -> downloadSelectedNote());
        buttonBox.add(downloadBtn);

        // 3. Open Note Button
        JButton openBtn = createStyledButton("Open Note", COLOR_MAIN_BLUE, COLOR_WHITE, COLOR_MAIN_BLUE);
        openBtn.addActionListener(e -> openSelectedNote());
        buttonBox.add(openBtn);

        // 4. Close Button
        JButton closeBtn = createStyledButton("Close", COLOR_WHITE, COLOR_TEXT_MUTED, COLOR_BORDER);
        closeBtn.addActionListener(e -> dispose());
        buttonBox.add(closeBtn);

        bottomBar.add(buttonBox, BorderLayout.EAST);
        return bottomBar;
    }

    /**
     * Helper to create uniformly styled buttons.
     */
    private JButton createStyledButton(String text, Color bg, Color fg, Color border) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new LineBorder(border, 1, true),
                new EmptyBorder(7, 16, 7, 16)
        ));
        return btn;
    }

    /**
     * Retrieves the selected LectureNoteItem from the table.
     */
    private LectureNoteItem getSelectedLectureNote() {
        if (notesTable == null || displayedNotes == null || displayedNotes.isEmpty()) {
            return null;
        }
        int selectedRow = notesTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= displayedNotes.size()) {
            return null;
        }
        return displayedNotes.get(selectedRow);
    }

    /**
     * Opens the selected lecture note file using the default system application.
     * Complies with requirement: Do not display a success message when a file cannot be opened.
     */
    private void openSelectedNote() {
        LectureNoteItem selectedNote = getSelectedLectureNote();
        if (selectedNote == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a lecture note from the table first.",
                    "No Note Selected",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        String filePath = selectedNote.getFilePath();
        if (filePath == null || filePath.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No file path is registered for this lecture note in the database.",
                    "File Unavailable",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        File targetFile = resolveFile(filePath);

        // Check if file exists on disk
        if (!targetFile.exists()) {
            JOptionPane.showMessageDialog(
                    this,
                    "The lecture note file could not be opened because it was not found on the system.\n\n" +
                            "Note Title: " + selectedNote.getTitle() + "\n" +
                            "Course: " + selectedNote.getCourseCode() + " - " + selectedNote.getCourseName() + "\n" +
                            "File Path: " + targetFile.getAbsolutePath() + "\n\n" +
                            "Please contact the lecturer (" + selectedNote.getLecturerName() + ") or Technical Officer.",
                    "File Not Found",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Open file using Desktop API
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(targetFile);
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Your operating system does not support opening files directly from Java.\n" +
                                "File Location: " + targetFile.getAbsolutePath(),
                        "Action Unsupported",
                        JOptionPane.WARNING_MESSAGE
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
     * Downloads (saves a copy of) the selected lecture note file to a chosen location.
     */
    private void downloadSelectedNote() {
        LectureNoteItem selectedNote = getSelectedLectureNote();
        if (selectedNote == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a lecture note from the table first.",
                    "No Note Selected",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        String filePath = selectedNote.getFilePath();
        if (filePath == null || filePath.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No file path is registered for this lecture note in the database.",
                    "Download Unavailable",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        File sourceFile = resolveFile(filePath);

        // Enforce check: Do not claim success if file cannot be retrieved
        if (!sourceFile.exists()) {
            JOptionPane.showMessageDialog(
                    this,
                    "The lecture note file could not be retrieved from the server storage.\n\n" +
                            "File Path: " + sourceFile.getAbsolutePath() + "\n\n" +
                            "Please contact the course lecturer (" + selectedNote.getLecturerName() + ").",
                    "File Retrieval Failed",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Prompt student to choose destination folder and filename
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(sourceFile.getName()));
        fileChooser.setDialogTitle("Save Lecture Note As...");

        int choice = fileChooser.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File destinationFile = fileChooser.getSelectedFile();

            // Perform simple file copy using basic Java streams
            FileInputStream inStream = null;
            FileOutputStream outStream = null;
            try {
                inStream = new FileInputStream(sourceFile);
                outStream = new FileOutputStream(destinationFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }

                JOptionPane.showMessageDialog(
                        this,
                        "Lecture note successfully downloaded to:\n" + destinationFile.getAbsolutePath(),
                        "Download Complete",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "An error occurred while saving the file: " + ex.getMessage(),
                        "Download Error",
                        JOptionPane.ERROR_MESSAGE
                );
            } finally {
                if (inStream != null) {
                    try { inStream.close(); } catch (IOException ignored) {}
                }
                if (outStream != null) {
                    try { outStream.close(); } catch (IOException ignored) {}
                }
            }
        }
    }

    /**
     * Resolves a file path against the project working directory or absolute path.
     */
    private File resolveFile(String filePath) {
        File file = new File(filePath);
        if (file.isAbsolute() && file.exists()) {
            return file;
        }
        // Relative to project workspace root
        File localFile = new File(System.getProperty("user.dir"), filePath);
        if (localFile.exists()) {
            return localFile;
        }
        return file;
    }

    // =========================================================================
    // Entry Point for Testing in IntelliJ IDEA
    // =========================================================================
    public static void main(String[] args) {
        // Enable clean font antialiasing on Windows
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            // Test student TG/2024/2085 (enrolled in ICT2122 and ICT2132)
            Undergraduate testStudent = new Undergraduate(
                    "21", "tg2085", "", "H.D.W.B. Hapuarachchi",
                    "tg2085@fot.ruh.ac.lk", "+94 77 123 4567", "",
                    "21", "TG/2024/2085", "2023/2024",
                    "Department of Information and Communication Technology", 2024
            );

            UndergraduateLectureNotesUI window = new UndergraduateLectureNotesUI(testStudent);
            window.setVisible(true);
        });
    }
}
