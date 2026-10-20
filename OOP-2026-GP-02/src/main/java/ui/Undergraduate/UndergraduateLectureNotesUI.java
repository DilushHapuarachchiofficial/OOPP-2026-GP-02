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
 * Implemented as a clean, embeddable JPanel component that renders
 * directly within the main Undergraduate Dashboard window.
 * Displays only the lecture notes belonging to courses in which
 * the currently logged-in student is enrolled.
 */
public class UndergraduateLectureNotesUI extends JPanel {

    // TecFAMS Blue-and-White Theme Colors
    private static final Color COLOR_PRIMARY_NAVY = new Color(0x12, 0x3B, 0x70); // #123B70
    private static final Color COLOR_MAIN_BLUE    = new Color(0x25, 0x63, 0xEB); // #2563EB
    private static final Color COLOR_LIGHT_BLUE   = new Color(0xEA, 0xF2, 0xFF); // #EAF2FF
    private static final Color COLOR_WHITE        = new Color(0xFF, 0xFF, 0xFF); // #FFFFFF
    private static final Color COLOR_BG           = new Color(0xF5, 0xF8, 0xFD); // #F5F8FD
    private static final Color COLOR_TEXT_PRIMARY = new Color(0x17, 0x2B, 0x4D); // #172B4D
    private static final Color COLOR_TEXT_MUTED   = new Color(0x71, 0x80, 0x96); // #718096
    private static final Color COLOR_BORDER       = new Color(0xE2, 0xE8, 0xF0); // #E2E8F0

    // Student identity, parent window, and DAO references
    private final Undergraduate loggedInStudent;
    private final UndergraduateDashboard parentDashboard;
    private final String studentIdentifier;
    private final LectureNotesDAO lectureNotesDAO;

    // UI Components
    private JPanel mainContentPanel;
    private JTable notesTable;
    private DefaultTableModel tableModel;
    private JLabel statusCountLabel;
    private List<LectureNoteItem> displayedNotes;

    /**
     * Primary constructor: Used by UndergraduateDashboard to embed the lecture notes page.
     *
     * @param student         The logged-in undergraduate student object.
     * @param parentDashboard Reference to the parent dashboard window.
     */
    public UndergraduateLectureNotesUI(Undergraduate student, UndergraduateDashboard parentDashboard) {
        this.loggedInStudent = student;
        this.parentDashboard = parentDashboard;
        this.studentIdentifier = resolveStudentIdentifier(student);
        this.lectureNotesDAO = new LectureNotesDAO();
        this.displayedNotes = new ArrayList<>();

        initializePanel();
        loadLectureNotesData();
    }

    /**
     * Overloaded constructor without dashboard reference.
     */
    public UndergraduateLectureNotesUI(Undergraduate student) {
        this(student, null);
    }

    /**
     * Overloaded constructor accepting student ID or registration number directly.
     */
    public UndergraduateLectureNotesUI(String studentIdOrRegNo) {
        this.loggedInStudent = null;
        this.parentDashboard = null;
        this.studentIdentifier = (studentIdOrRegNo != null && !studentIdOrRegNo.trim().isEmpty())
                ? studentIdOrRegNo.trim()
                : "TG/2024/2085";
        this.lectureNotesDAO = new LectureNotesDAO();
        this.displayedNotes = new ArrayList<>();

        initializePanel();
        loadLectureNotesData();
    }

    /**
     * Resolves the student ID or registration number from the Undergraduate object.
     */
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

    /**
     * Sets up the panel layout, heading bar, content card, and action bar.
     */
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);

        // Top Section: Heading "My Course Lecture Notes" and status count
        add(createHeadingPanel(), BorderLayout.NORTH);

        // Center Section: Dynamic card area (Table or Empty state message)
        mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBackground(COLOR_BG);
        mainContentPanel.setBorder(new EmptyBorder(12, 24, 16, 24));
        add(mainContentPanel, BorderLayout.CENTER);

        // Bottom Section: Actions (Back to Dashboard, Refresh, Download, Open)
        add(createBottomActionPanel(), BorderLayout.SOUTH);
    }

    /**
     * Creates the heading banner with title, enrolled course status, and quick return shortcut.
     */
    private JPanel createHeadingPanel() {
        JPanel headingBar = new JPanel(new BorderLayout());
        headingBar.setBackground(COLOR_BG);
        headingBar.setBorder(new EmptyBorder(18, 24, 4, 24));

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

        // Quick back to dashboard shortcut on top-right
        if (parentDashboard != null) {
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
            backBtn.addActionListener(e -> parentDashboard.showDashboardView());
            headingBar.add(backBtn, BorderLayout.EAST);
        }

        return headingBar;
    }

    /**
     * Loads enrolled courses and their associated lecture notes from the database.
     * Enforces course-based restriction and displays proper empty state messages.
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
                    "Please contact your Department Coordinator or Dean's Office to complete course registration."
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
     * Creates an empty state panel when no courses or notes are available.
     */
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

    /**
     * Builds the JTable displaying lecture notes.
     */
    private JPanel createTablePanel(List<LectureNoteItem> notes) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(COLOR_WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

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
        notesTable.setRowHeight(34);
        notesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        notesTable.setGridColor(new Color(0xED, 0xF2, 0xF7));
        notesTable.setShowVerticalLines(false);
        notesTable.setShowHorizontalLines(true);
        notesTable.setSelectionBackground(COLOR_LIGHT_BLUE);
        notesTable.setSelectionForeground(COLOR_PRIMARY_NAVY);

        JTableHeader header = notesTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(0xF1, 0xF5, 0xF9));
        header.setForeground(COLOR_PRIMARY_NAVY);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setReorderingAllowed(false);

        notesTable.getColumnModel().getColumn(0).setPreferredWidth(35);   // #
        notesTable.getColumnModel().getColumn(1).setPreferredWidth(270);  // Title
        notesTable.getColumnModel().getColumn(2).setPreferredWidth(95);   // Code
        notesTable.getColumnModel().getColumn(3).setPreferredWidth(220);  // Name
        notesTable.getColumnModel().getColumn(4).setPreferredWidth(170);  // Lecturer
        notesTable.getColumnModel().getColumn(5).setPreferredWidth(95);   // Type
        notesTable.getColumnModel().getColumn(6).setPreferredWidth(95);   // Date

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        notesTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        notesTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        notesTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        notesTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        notesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    openSelectedNote();
                }
            }
        });

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
     * Bottom action bar containing Back to Dashboard, Refresh, Download, and Open buttons.
     */
    private JPanel createBottomActionPanel() {
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(COLOR_WHITE);
        bottomBar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER),
                new EmptyBorder(12, 24, 12, 24)
        ));

        JLabel hintLabel = new JLabel("Tip: Double-click any note to open it, or use the buttons on the right.");
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

        // 3. Download Note Button
        JButton downloadBtn = createStyledButton("Download Note", COLOR_LIGHT_BLUE, COLOR_MAIN_BLUE, COLOR_MAIN_BLUE);
        downloadBtn.addActionListener(e -> downloadSelectedNote());
        buttonBox.add(downloadBtn);

        // 4. Open Note Button
        JButton openBtn = createStyledButton("Open Note", COLOR_MAIN_BLUE, COLOR_WHITE, COLOR_MAIN_BLUE);
        openBtn.addActionListener(e -> openSelectedNote());
        buttonBox.add(openBtn);

        bottomBar.add(buttonBox, BorderLayout.EAST);
        return bottomBar;
    }

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

        if (!targetFile.exists()) {
            JOptionPane.showMessageDialog(
                    this,
                    "The lecture note file was not found on the server storage.\n\n" +
                            "Note Title: " + selectedNote.getTitle() + "\n" +
                            "Course: " + selectedNote.getCourseCode() + " - " + selectedNote.getCourseName() + "\n" +
                            "File Path: " + targetFile.getAbsolutePath() + "\n\n" +
                            "Please contact the lecturer (" + selectedNote.getLecturerName() + ") or Technical Officer.",
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

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(sourceFile.getName()));
        fileChooser.setDialogTitle("Save Lecture Note As...");

        int choice = fileChooser.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File destinationFile = fileChooser.getSelectedFile();

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
