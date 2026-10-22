package main.java.ui.Undergraduate;

import main.java.dao.Undergraduate.MedicalDAO;
import main.java.model.Undergraduate.MedicalRecord;
import main.java.model.Undergraduate.MedicalStatus;
import main.java.model.Undergraduate.MissedSession;
import main.java.model.Undergraduate.Undergraduate;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * TecFAMS - Faculty of Technology Academic Management System
 * Undergraduate Medical Details UI Page.
 *
 * Allows students to submit a medical certificate covering a date range
 * (dawas gana), automatically detects all lectures/sessions missed during
 * that period, allows attaching a medical photo/document, generates a
 * unique reference number, and saves directly to the existing database schema.
 */
public class UndergraduateMedicalUI extends JPanel {

    // Theme Color Palette
    private static final Color COLOR_PRIMARY_NAVY = new Color(0x12, 0x3B, 0x70);
    private static final Color COLOR_MAIN_BLUE    = new Color(0x25, 0x63, 0xEB);
    private static final Color COLOR_LIGHT_BLUE   = new Color(0xEA, 0xF2, 0xFF);
    private static final Color COLOR_WHITE        = new Color(0xFF, 0xFF, 0xFF);
    private static final Color COLOR_BG           = new Color(0xF5, 0xF8, 0xFD);
    private static final Color COLOR_TEXT_PRIMARY = new Color(0x17, 0x2B, 0x4D);
    private static final Color COLOR_TEXT_MUTED   = new Color(0x71, 0x80, 0x96);
    private static final Color COLOR_BORDER       = new Color(0xE2, 0xE8, 0xF0);
    private static final Color COLOR_SUCCESS      = new Color(0x10, 0xB9, 0x81);
    private static final Color COLOR_DANGER       = new Color(0xEF, 0x44, 0x44);
    private static final Color COLOR_WARNING      = new Color(0xF5, 0x9E, 0x0B);

    // References
    private final Undergraduate loggedInStudent;
    private final UndergraduateDashboard parentDashboard;
    private final String studentIdentifier;
    private final MedicalDAO medicalDAO;

    // Form inputs
    private JTextField startDateField;
    private JTextField endDateField;
    private JPanel absentQuickPickRow;
    private JPanel sessionsChecklistContainer;
    private JLabel sessionsFoundCountLabel;
    private JTextField reasonField;
    private JLabel selectedFileLabel;
    private JLabel photoPreviewLabel;
    private File chosenMedicalFile = null;

    // Checkbox list for missed sessions found in range
    private final List<JCheckBox> sessionCheckBoxes = new ArrayList<>();
    private final List<MissedSession> currentRangeSessions = new ArrayList<>();

    // Metric Labels
    private JLabel totalSubmissionsLabel;
    private JLabel approvedCountLabel;
    private JLabel pendingCountLabel;
    private JLabel missedSessionsCountLabel;

    // History Table
    private DefaultTableModel historyTableModel;
    private JTable historyTable;
    private JLabel tableCountLabel;

    // Data Cache
    private List<MissedSession> allAbsentSessions = new ArrayList<>();
    private List<MedicalRecord> submittedMedicals = new ArrayList<>();

    public UndergraduateMedicalUI(Undergraduate student, UndergraduateDashboard parentDashboard) {
        this.loggedInStudent = student;
        this.parentDashboard = parentDashboard;
        this.studentIdentifier = resolveStudentIdentifier(student);
        this.medicalDAO = new MedicalDAO();

        initializeUI();
        loadMedicalData();
    }

    public UndergraduateMedicalUI(Undergraduate student) {
        this(student, null);
    }

    public UndergraduateMedicalUI() {
        this(null, null);
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

        // 2. Student Info Ribbon
        scrollContent.add(createStudentInfoRibbon());
        scrollContent.add(Box.createVerticalStrut(16));

        // 3. Metric Summary Cards
        scrollContent.add(createMetricCardsRow());
        scrollContent.add(Box.createVerticalStrut(20));

        // 4. Submission Form Card (Multi-session date range)
        scrollContent.add(createSubmissionFormCard());
        scrollContent.add(Box.createVerticalStrut(20));

        // 5. History Table Card
        scrollContent.add(createHistoryTableCard());
        scrollContent.add(Box.createVerticalStrut(20));

        // 6. Faculty Policy Notice Card
        scrollContent.add(createPolicyNoticeCard());

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.setBackground(COLOR_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }

    // ==========================================
    // 1. TOP HEADER PANEL
    // ==========================================
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel leftBox = new JPanel(new GridLayout(2, 1, 0, 4));
        leftBox.setOpaque(false);

        JLabel titleLbl = new JLabel("Medical Certificates & Details");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(COLOR_PRIMARY_NAVY);

        JLabel subtitleLbl = new JLabel("Submit a medical certificate covering your absent days and all missed lectures.");
        subtitleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLbl.setForeground(COLOR_TEXT_MUTED);

        leftBox.add(titleLbl);
        leftBox.add(subtitleLbl);
        header.add(leftBox, BorderLayout.WEST);

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
            }
        });
        header.add(backBtn, BorderLayout.EAST);
        return header;
    }

    // ==========================================
    // 2. STUDENT INFO RIBBON
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

        leftBox.add(createAvatarIcon(38, studentName));

        JPanel details = new JPanel(new GridLayout(2, 1, 0, 2));
        details.setOpaque(false);
        JLabel nameLbl = new JLabel(studentName + "  (" + regNo + ")");
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLbl.setForeground(COLOR_TEXT_PRIMARY);

        JLabel metaLbl = new JLabel(dept + "  •  " + batch + "  •  Undergraduate Portal");
        metaLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        metaLbl.setForeground(COLOR_TEXT_MUTED);

        details.add(nameLbl);
        details.add(metaLbl);
        leftBox.add(details);

        ribbon.add(leftBox, BorderLayout.WEST);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"));
        JLabel dateBadge = new JLabel("Academic Session 2026  |  " + today);
        dateBadge.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateBadge.setForeground(COLOR_PRIMARY_NAVY);
        dateBadge.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(6, 12, 6, 12)));
        ribbon.add(dateBadge, BorderLayout.EAST);

        return ribbon;
    }

    // ==========================================
    // 3. METRIC CARDS ROW
    // ==========================================
    private JPanel createMetricCardsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        totalSubmissionsLabel = new JLabel("0");
        approvedCountLabel = new JLabel("0");
        pendingCountLabel = new JLabel("0");
        missedSessionsCountLabel = new JLabel("0");

        row.add(createMetricCard("TOTAL SUBMISSIONS", totalSubmissionsLabel, COLOR_PRIMARY_NAVY, "Certificates logged", "Official Faculty Registry"));
        row.add(createMetricCard("APPROVED MEDICALS", approvedCountLabel, COLOR_SUCCESS, "Certified by UMO", "Counted toward 80% Rule"));
        row.add(createMetricCard("PENDING VERIFICATION", pendingCountLabel, COLOR_WARNING, "Awaiting UMO Review", "Submitted to Medical Centre"));
        row.add(createMetricCard("MISSED SESSIONS", missedSessionsCountLabel, COLOR_DANGER, "Absent without medical", "Eligible for submission"));

        return row;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color valColor, String sub1, String sub2) {
        JPanel card = new JPanel(new GridLayout(4, 1, 0, 3));
        card.setBackground(COLOR_WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(12, 16, 12, 16)));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 10));
        t.setForeground(COLOR_TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(valColor);

        JLabel s1 = new JLabel(sub1);
        s1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        s1.setForeground(COLOR_TEXT_PRIMARY);

        JLabel s2 = new JLabel(sub2);
        s2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        s2.setForeground(COLOR_TEXT_MUTED);

        card.add(t);
        card.add(valueLabel);
        card.add(s1);
        card.add(s2);
        return card;
    }

    // ==========================================
    // 4. SUBMISSION FORM CARD (MULTI-SESSION)
    // ==========================================
    private JPanel createSubmissionFormCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(COLOR_WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        // Form Title & Subtitle
        JPanel formHeader = new JPanel(new BorderLayout());
        formHeader.setOpaque(false);
        JLabel title = new JLabel("Submit Medical Certificate for Absent Days & Missed Lectures");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(COLOR_PRIMARY_NAVY);

        JLabel hint = new JLabel("Select your medical leave date range (start & end dates). The system will detect all lectures missed during that period.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(COLOR_TEXT_MUTED);

        formHeader.add(title, BorderLayout.NORTH);
        formHeader.add(hint, BorderLayout.SOUTH);
        card.add(formHeader, BorderLayout.NORTH);

        // Form Fields Grid
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setOpaque(false);

        // 1. Quick Absent Dates Strip (helper to easily set dates)
        absentQuickPickRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        absentQuickPickRow.setOpaque(false);
        fieldsPanel.add(absentQuickPickRow);
        fieldsPanel.add(Box.createVerticalStrut(8));

        // 2. Date Range Input Row
        JPanel datesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        datesRow.setOpaque(false);

        JLabel startLbl = new JLabel("From Date:*");
        startLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        startLbl.setForeground(COLOR_TEXT_PRIMARY);
        datesRow.add(startLbl);

        startDateField = new JTextField("2026-10-13", 10);
        startDateField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        startDateField.setPreferredSize(new Dimension(110, 32));
        startDateField.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1), new EmptyBorder(4, 8, 4, 8)));
        datesRow.add(startDateField);

        JLabel endLbl = new JLabel("To Date:*");
        endLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        endLbl.setForeground(COLOR_TEXT_PRIMARY);
        datesRow.add(endLbl);

        endDateField = new JTextField("2026-10-15", 10);
        endDateField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        endDateField.setPreferredSize(new Dimension(110, 32));
        endDateField.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1), new EmptyBorder(4, 8, 4, 8)));
        datesRow.add(endDateField);

        JButton checkSessionsBtn = new JButton("🔍 Find Missed Lectures in Period");
        checkSessionsBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        checkSessionsBtn.setForeground(COLOR_MAIN_BLUE);
        checkSessionsBtn.setBackground(COLOR_LIGHT_BLUE);
        checkSessionsBtn.setFocusPainted(false);
        checkSessionsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        checkSessionsBtn.setBorder(new CompoundBorder(new LineBorder(COLOR_MAIN_BLUE, 1, true), new EmptyBorder(6, 12, 6, 12)));
        checkSessionsBtn.addActionListener(e -> findMissedLecturesForRange());
        datesRow.add(checkSessionsBtn);

        fieldsPanel.add(datesRow);
        fieldsPanel.add(Box.createVerticalStrut(12));

        // 3. Detected Missed Lectures Checklist Container
        JPanel detectedBox = new JPanel(new BorderLayout(0, 6));
        detectedBox.setBackground(new Color(0xF8, 0xFA, 0xFC));
        detectedBox.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(10, 14, 10, 14)));

        sessionsFoundCountLabel = new JLabel("Missed Lectures Detected during this period (Select all covered by your certificate):");
        sessionsFoundCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sessionsFoundCountLabel.setForeground(COLOR_PRIMARY_NAVY);
        detectedBox.add(sessionsFoundCountLabel, BorderLayout.NORTH);

        sessionsChecklistContainer = new JPanel();
        sessionsChecklistContainer.setLayout(new BoxLayout(sessionsChecklistContainer, BoxLayout.Y_AXIS));
        sessionsChecklistContainer.setOpaque(false);
        detectedBox.add(sessionsChecklistContainer, BorderLayout.CENTER);

        fieldsPanel.add(detectedBox);
        fieldsPanel.add(Box.createVerticalStrut(14));

        // 4. Illness Reason Input
        JPanel reasonRow = new JPanel(new BorderLayout(10, 4));
        reasonRow.setOpaque(false);

        JLabel reasonLbl = new JLabel("Illness Reason / Diagnosis:*");
        reasonLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        reasonLbl.setForeground(COLOR_TEXT_PRIMARY);

        reasonField = new JTextField();
        reasonField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        reasonField.setPreferredSize(new Dimension(0, 32));
        reasonField.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1), new EmptyBorder(4, 8, 4, 8)));

        reasonRow.add(reasonLbl, BorderLayout.WEST);
        reasonRow.add(reasonField, BorderLayout.CENTER);
        fieldsPanel.add(reasonRow);
        fieldsPanel.add(Box.createVerticalStrut(14));

        // 5. File Upload Row & Photo Preview
        JPanel uploadSection = new JPanel(new BorderLayout(14, 0));
        uploadSection.setOpaque(false);

        JPanel uploadLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        uploadLeft.setOpaque(false);

        JLabel uploadLbl = new JLabel("Medical Certificate Photo:*");
        uploadLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        uploadLbl.setForeground(COLOR_TEXT_PRIMARY);
        uploadLeft.add(uploadLbl);

        JButton chooseFileBtn = new JButton("📁 Choose Photo / Certificate (PNG, JPG, PDF)...");
        chooseFileBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        chooseFileBtn.setForeground(COLOR_MAIN_BLUE);
        chooseFileBtn.setBackground(COLOR_LIGHT_BLUE);
        chooseFileBtn.setFocusPainted(false);
        chooseFileBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chooseFileBtn.setBorder(new CompoundBorder(new LineBorder(COLOR_MAIN_BLUE, 1, true), new EmptyBorder(6, 12, 6, 12)));
        chooseFileBtn.addActionListener(e -> chooseMedicalFile());
        uploadLeft.add(chooseFileBtn);

        selectedFileLabel = new JLabel("No file chosen yet");
        selectedFileLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        selectedFileLabel.setForeground(COLOR_TEXT_MUTED);
        uploadLeft.add(selectedFileLabel);
        uploadSection.add(uploadLeft, BorderLayout.CENTER);

        photoPreviewLabel = new JLabel("No photo preview");
        photoPreviewLabel.setPreferredSize(new Dimension(130, 80));
        photoPreviewLabel.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        photoPreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        photoPreviewLabel.setForeground(COLOR_TEXT_MUTED);
        photoPreviewLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        uploadSection.add(photoPreviewLabel, BorderLayout.EAST);

        fieldsPanel.add(uploadSection);
        fieldsPanel.add(Box.createVerticalStrut(16));

        // 6. Action Buttons (Submit & Clear)
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionRow.setOpaque(false);

        JButton submitBtn = new JButton("Submit Medical Certificate");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        submitBtn.setForeground(COLOR_WHITE);
        submitBtn.setBackground(COLOR_MAIN_BLUE);
        submitBtn.setFocusPainted(false);
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.setBorder(new CompoundBorder(new LineBorder(COLOR_MAIN_BLUE, 1, true), new EmptyBorder(8, 20, 8, 20)));
        submitBtn.addActionListener(e -> submitMedicalForm());
        actionRow.add(submitBtn);

        JButton clearBtn = new JButton("Clear Form");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearBtn.setForeground(COLOR_TEXT_PRIMARY);
        clearBtn.setBackground(COLOR_WHITE);
        clearBtn.setFocusPainted(false);
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(8, 14, 8, 14)));
        clearBtn.addActionListener(e -> resetForm());
        actionRow.add(clearBtn);

        fieldsPanel.add(actionRow);

        card.add(fieldsPanel, BorderLayout.CENTER);
        return card;
    }

    private void chooseMedicalFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Medical Certificate Photo or Document");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Medical Certificates (Images & PDF)", "jpg", "jpeg", "png", "gif", "pdf"));

        int res = fileChooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            chosenMedicalFile = fileChooser.getSelectedFile();
            selectedFileLabel.setText(chosenMedicalFile.getName() + " (" + (chosenMedicalFile.length() / 1024) + " KB)");
            selectedFileLabel.setForeground(COLOR_PRIMARY_NAVY);

            String name = chosenMedicalFile.getName().toLowerCase();
            if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif")) {
                try {
                    BufferedImage img = ImageIO.read(chosenMedicalFile);
                    if (img != null) {
                        Image scaled = img.getScaledInstance(120, 75, Image.SCALE_SMOOTH);
                        photoPreviewLabel.setIcon(new ImageIcon(scaled));
                        photoPreviewLabel.setText("");
                    }
                } catch (Exception ex) {
                    photoPreviewLabel.setIcon(null);
                    photoPreviewLabel.setText("Preview error");
                }
            } else {
                photoPreviewLabel.setIcon(null);
                photoPreviewLabel.setText("PDF Document attached");
            }
        }
    }

    /**
     * Finds all lectures/sessions where the student was marked absent between the chosen dates.
     */
    private void findMissedLecturesForRange() {
        String startDate = startDateField.getText().trim();
        String endDate = endDateField.getText().trim();

        if (startDate.isEmpty() || endDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both Start Date and End Date.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        sessionsChecklistContainer.removeAll();
        sessionCheckBoxes.clear();
        currentRangeSessions.clear();

        List<MissedSession> found = medicalDAO.getAbsentSessionsBetweenDates(studentIdentifier, startDate, endDate);
        currentRangeSessions.addAll(found);

        if (found.isEmpty()) {
            sessionsFoundCountLabel.setText("Missed Lectures Detected: 0 sessions found in this date range");
            JLabel emptyLbl = new JLabel("<html><i>No absent lectures recorded between " + startDate + " and " + endDate +
                    ". If you were absent, Technical Officer may not have uploaded attendance yet, but you can still submit.</i></html>");
            emptyLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            emptyLbl.setForeground(COLOR_TEXT_MUTED);
            sessionsChecklistContainer.add(emptyLbl);
        } else {
            sessionsFoundCountLabel.setText("Missed Lectures Detected: " + found.size() + " session(s) in this period (Select covered lectures):");
            for (MissedSession ms : found) {
                String text = "<html><b>" + ms.getCourseCode() + " - " + ms.getCourseName() + "</b>  |  Session #" +
                        ms.getSessionNumber() + "  |  Date: <font color='#2563EB'><b>" + ms.getSessionDate() +
                        "</b></font> (" + ms.getSessionType() + ")</html>";
                JCheckBox cb = new JCheckBox(text, true); // checked by default
                cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                cb.setOpaque(false);
                cb.setCursor(new Cursor(Cursor.HAND_CURSOR));
                sessionCheckBoxes.add(cb);
                sessionsChecklistContainer.add(cb);
                sessionsChecklistContainer.add(Box.createVerticalStrut(4));
            }
        }

        sessionsChecklistContainer.revalidate();
        sessionsChecklistContainer.repaint();
    }

    private void submitMedicalForm() {
        String startDate = startDateField.getText().trim();
        String endDate = endDateField.getText().trim();
        String reason = reasonField.getText().trim();

        if (startDate.isEmpty() || endDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the medical period (Start Date and End Date).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (reason.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please specify the illness reason / diagnosis for the medical certificate.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (chosenMedicalFile == null || !chosenMedicalFile.exists()) {
            JOptionPane.showMessageDialog(this, "Please attach a photo or document of your medical certificate before submitting.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Collect checked sessions
        List<Integer> selectedSessionIds = new ArrayList<>();
        List<String> coveredCourseNames = new ArrayList<>();

        for (int i = 0; i < sessionCheckBoxes.size(); i++) {
            if (sessionCheckBoxes.get(i).isSelected()) {
                MissedSession ms = currentRangeSessions.get(i);
                selectedSessionIds.add(ms.getSessionId());
                coveredCourseNames.add(ms.getCourseCode() + " (Sess #" + ms.getSessionNumber() + " on " + ms.getSessionDate() + ")");
            }
        }

        int studentNumericId = medicalDAO.getStudentNumericId(studentIdentifier);
        String savedDocPath = saveMedicalFile(chosenMedicalFile, studentNumericId);

        // Submit to database covering all selected sessions!
        int primaryMedicalId = medicalDAO.submitMedicalForSessions(studentNumericId, selectedSessionIds, startDate, endDate, reason, savedDocPath);

        if (primaryMedicalId > 0) {
            String referenceNumber = "MED-REF-" + String.format("%04d", primaryMedicalId);

            StringBuilder coursesHtml = new StringBuilder();
            if (coveredCourseNames.isEmpty()) {
                coursesHtml.append("<li>General Medical Leave (No specific lectures selected)</li>");
            } else {
                for (String c : coveredCourseNames) {
                    coursesHtml.append("<li><b>").append(c).append("</b></li>");
                }
            }

            String msg = "<html><body style='width: 360px; font-family: Segoe UI;'>" +
                    "<h3 style='color: #10B981; margin-bottom: 6px;'>✓ Medical Certificate Submitted Successfully!</h3>" +
                    "<p>Your medical certificate has been registered in the TecFAMS system.</p>" +
                    "<div style='background-color: #EAF2FF; padding: 10px; border: 1px solid #2563EB; border-radius: 4px; margin: 10px 0;'>" +
                    "<b>Reference Number: </b><span style='color: #2563EB; font-size: 14px; font-weight: bold;'>" + referenceNumber + "</span><br>" +
                    "<b>Medical Period: </b>" + startDate + " to " + endDate + "<br>" +
                    "<b>Covered Lectures (" + (selectedSessionIds.isEmpty() ? 1 : selectedSessionIds.size()) + "):</b>" +
                    "<ul style='margin-top: 4px; margin-bottom: 4px; padding-left: 18px;'>" + coursesHtml + "</ul>" +
                    "<b>Status: </b><span style='color: #D97706; font-weight: bold;'>Pending Verification</span>" +
                    "</div>" +
                    "<p style='font-size: 11px; color: #718096;'>Please keep this Reference Number for official inquiries with the Medical Officer or Technical Officer.</p>" +
                    "</body></html>";

            JOptionPane.showMessageDialog(this, msg, "Medical Submission Confirmed", JOptionPane.INFORMATION_MESSAGE);

            resetForm();
            loadMedicalData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to submit medical certificate into the database. Please verify your connection.", "Submission Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String saveMedicalFile(File sourceFile, int studentId) {
        File dir = new File("medicals");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String origName = sourceFile.getName();
        String ext = "";
        int dot = origName.lastIndexOf('.');
        if (dot >= 0) {
            ext = origName.substring(dot);
        }

        String newFileName = "med_" + studentId + "_" + System.currentTimeMillis() + ext;
        File destFile = new File(dir, newFileName);

        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(sourceFile);
            out = new FileOutputStream(destFile);
            byte[] buf = new byte[4096];
            int read;
            while ((read = in.read(buf)) != -1) {
                out.write(buf, 0, read);
            }
            return "medicals/" + newFileName;
        } catch (IOException e) {
            System.err.println("Notice: Could not copy file to medicals directory: " + e.getMessage());
            return sourceFile.getAbsolutePath();
        } finally {
            if (in != null) try { in.close(); } catch (IOException ignored) {}
            if (out != null) try { out.close(); } catch (IOException ignored) {}
        }
    }

    private void resetForm() {
        reasonField.setText("");
        chosenMedicalFile = null;
        selectedFileLabel.setText("No file chosen yet");
        selectedFileLabel.setForeground(COLOR_TEXT_MUTED);
        photoPreviewLabel.setIcon(null);
        photoPreviewLabel.setText("No photo preview");
        findMissedLecturesForRange();
    }

    // ==========================================
    // 5. MEDICAL HISTORY TABLE CARD
    // ==========================================
    private JPanel createHistoryTableCard() {
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(COLOR_WHITE);
        tableCard.setBorder(new LineBorder(COLOR_BORDER, 1, true));

        // Subheader
        JPanel tableSubHeader = new JPanel(new BorderLayout());
        tableSubHeader.setBackground(COLOR_WHITE);
        tableSubHeader.setBorder(new EmptyBorder(12, 16, 10, 16));

        JLabel title = new JLabel("Submitted Medical Certificates Registry");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(COLOR_PRIMARY_NAVY);

        tableCountLabel = new JLabel("0 records loaded");
        tableCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableCountLabel.setForeground(COLOR_TEXT_MUTED);

        tableSubHeader.add(title, BorderLayout.WEST);
        tableSubHeader.add(tableCountLabel, BorderLayout.EAST);
        tableCard.add(tableSubHeader, BorderLayout.NORTH);

        // Columns
        String[] columns = {"Reference #", "Covered Lecture / Course", "Medical Period", "Illness Reason", "Certificate File", "Submitted On", "Status"};
        historyTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        historyTable = new JTable(historyTableModel);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        historyTable.setRowHeight(38);
        historyTable.setGridColor(COLOR_BORDER);
        historyTable.setShowVerticalLines(false);
        historyTable.setSelectionBackground(COLOR_LIGHT_BLUE);
        historyTable.setSelectionForeground(COLOR_TEXT_PRIMARY);

        JTableHeader th = historyTable.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(new Color(0xFB, 0xFC, 0xFE));
        th.setForeground(COLOR_PRIMARY_NAVY);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        th.setPreferredSize(new Dimension(0, 36));

        // Column widths
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(120); // Reference #
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(230); // Course & Session
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(160); // Period
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(190); // Reason
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(140); // Document
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Submitted On
        historyTable.getColumnModel().getColumn(6).setPreferredWidth(110); // Status

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        historyTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        historyTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        // Status badge pill renderer
        historyTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
                panel.setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? COLOR_WHITE : new Color(0xF9, 0xFB, 0xFD)));

                String status = (value != null) ? value.toString() : "Pending";
                Color bg;
                Color fg;
                String dot = "● ";

                if (status.equalsIgnoreCase("Approved")) {
                    bg = new Color(0xD1, 0xFA, 0xE5);
                    fg = new Color(0x06, 0x5F, 0x46);
                } else if (status.equalsIgnoreCase("Rejected")) {
                    bg = new Color(0xFE, 0xE2, 0xE2);
                    fg = new Color(0x99, 0x1B, 0x1B);
                } else {
                    bg = new Color(0xFE, 0xF3, 0xC7);
                    fg = new Color(0x92, 0x40, 0x0E);
                }

                JLabel pill = new JLabel("  " + dot + status + "  ");
                pill.setFont(new Font("Segoe UI", Font.BOLD, 11));
                pill.setOpaque(true);
                pill.setBackground(bg);
                pill.setForeground(fg);
                pill.setBorder(new LineBorder(bg, 4, true));
                panel.add(pill);
                return panel;
            }
        });

        // Double-click to view photo/file
        historyTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = historyTable.getSelectedRow();
                    if (row >= 0 && row < submittedMedicals.size()) {
                        openDocumentFile(submittedMedicals.get(row).getDocumentPath());
                    }
                }
            }
        });

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(historyTableModel);
        historyTable.setRowSorter(sorter);

        JScrollPane tableScroll = new JScrollPane(historyTable);
        tableScroll.setBorder(null);
        tableScroll.getViewport().setBackground(COLOR_WHITE);
        tableScroll.setPreferredSize(new Dimension(0, 260));
        tableCard.add(tableScroll, BorderLayout.CENTER);

        return tableCard;
    }

    private void openDocumentFile(String path) {
        if (path == null || path.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No document path is recorded for this submission.", "Document Not Available", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        File file = new File(path);
        if (!file.exists()) {
            file = new File(System.getProperty("user.dir"), path);
        }

        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "The certificate document file was not found on local storage:\n" + path, "File Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(file);
            } else {
                JOptionPane.showMessageDialog(this, "File Location: " + file.getAbsolutePath(), "Document Path", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not open document: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // 6. POLICY NOTICE CARD
    // ==========================================
    private JPanel createPolicyNoticeCard() {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(new Color(0xEE, 0xF4, 0xFF));
        card.setBorder(new CompoundBorder(new LineBorder(new Color(0xC7, 0xDC, 0xFD), 1, true), new EmptyBorder(14, 18, 14, 18)));

        JLabel iconLbl = new JLabel("ℹ");
        iconLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        iconLbl.setForeground(COLOR_MAIN_BLUE);
        card.add(iconLbl, BorderLayout.WEST);

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 3));
        text.setOpaque(false);

        JLabel h = new JLabel("Faculty Medical Regulations & 14-Day Submission Rule");
        h.setFont(new Font("Segoe UI", Font.BOLD, 12));
        h.setForeground(COLOR_PRIMARY_NAVY);

        JLabel b = new JLabel("<html>Under University regulations, medical certificates must be submitted within <b>14 days</b> of the absent lecture session. A single medical certificate can cover multiple missed lectures across its valid date range. Only certificates certified by the <b>University Medical Officer (UMO)</b> will be credited towards the 80% attendance rule.</html>");
        b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        b.setForeground(COLOR_TEXT_PRIMARY);

        text.add(h);
        text.add(b);
        card.add(text, BorderLayout.CENTER);

        return card;
    }

    // ==========================================
    // 7. DATA LOADING & ASYNC WORKER
    // ==========================================
    public void loadMedicalData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            List<MissedSession> absent;
            List<MedicalRecord> records;

            @Override
            protected Void doInBackground() {
                absent = medicalDAO.getAbsentSessions(studentIdentifier);
                records = medicalDAO.getMedicalRecords(studentIdentifier);
                return null;
            }

            @Override
            protected void done() {
                try {
                    allAbsentSessions = (absent != null) ? absent : new ArrayList<>();
                    submittedMedicals = (records != null) ? records : new ArrayList<>();

                    // 1. Build Quick-Pick Absent Dates Strip
                    buildAbsentQuickPickStrip();

                    // 2. Load Missed Lectures for current date range
                    findMissedLecturesForRange();

                    // 3. Populate History Table
                    if (historyTableModel != null) {
                        historyTableModel.setRowCount(0);
                        int approved = 0;
                        int pending = 0;

                        for (MedicalRecord rec : submittedMedicals) {
                            if (rec.getStatus() == MedicalStatus.APPROVED) approved++;
                            else if (rec.getStatus() == MedicalStatus.PENDING) pending++;

                            String courseSession = (rec.getCourseCode() != null && !rec.getCourseCode().isEmpty())
                                    ? rec.getCourseCode() + " - " + (rec.getCourseName() != null ? rec.getCourseName() : "") +
                                      (rec.getSessionNumber() > 0 ? " (Sess #" + rec.getSessionNumber() + ")" : "")
                                    : "General Medical Absence";

                            String period = (rec.getStartDate() != null ? rec.getStartDate() : "") +
                                    (rec.getEndDate() != null && !rec.getEndDate().equals(rec.getStartDate()) ? " to " + rec.getEndDate() : "");

                            String docName = (rec.getDocumentPath() != null) ? new File(rec.getDocumentPath()).getName() : "None";

                            historyTableModel.addRow(new Object[]{
                                    rec.getReferenceNo(),
                                    courseSession,
                                    period,
                                    rec.getReason() != null ? rec.getReason() : "Medical illness",
                                    docName,
                                    rec.getSubmittedDate() != null ? rec.getSubmittedDate() : rec.getDate(),
                                    rec.getStatus().name()
                            });
                        }

                        // 4. Update Metric Counts
                        if (totalSubmissionsLabel != null) totalSubmissionsLabel.setText(String.valueOf(submittedMedicals.size()));
                        if (approvedCountLabel != null) approvedCountLabel.setText(String.valueOf(approved));
                        if (pendingCountLabel != null) pendingCountLabel.setText(String.valueOf(pending));
                        if (missedSessionsCountLabel != null) missedSessionsCountLabel.setText(String.valueOf(allAbsentSessions.size()));
                        if (tableCountLabel != null) tableCountLabel.setText("Showing " + submittedMedicals.size() + " submitted certificate record(s)");
                    }
                } catch (Exception e) {
                    System.err.println("Notice: Could not finish medical data update: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Builds interactive chips for absent dates, allowing one-click date range filling.
     */
    private void buildAbsentQuickPickStrip() {
        if (absentQuickPickRow == null) return;
        absentQuickPickRow.removeAll();

        JLabel stripLbl = new JLabel("Your Recorded Absent Dates (Click to auto-set dates): ");
        stripLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        stripLbl.setForeground(COLOR_TEXT_PRIMARY);
        absentQuickPickRow.add(stripLbl);

        if (allAbsentSessions.isEmpty()) {
            JLabel noAbsent = new JLabel("None recorded (All lectures attended or up to date)");
            noAbsent.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            noAbsent.setForeground(COLOR_SUCCESS);
            absentQuickPickRow.add(noAbsent);
        } else {
            // Group unique dates
            Map<String, List<MissedSession>> dateMap = new LinkedHashMap<>();
            for (MissedSession ms : allAbsentSessions) {
                String d = ms.getSessionDate();
                dateMap.computeIfAbsent(d, k -> new ArrayList<>()).add(ms);
            }

            for (Map.Entry<String, List<MissedSession>> entry : dateMap.entrySet()) {
                String d = entry.getKey();
                int sessCount = entry.getValue().size();

                JButton chip = new JButton(d + " (" + sessCount + " lecture" + (sessCount > 1 ? "s" : "") + ")");
                chip.setFont(new Font("Segoe UI", Font.BOLD, 10));
                chip.setForeground(COLOR_PRIMARY_NAVY);
                chip.setBackground(new Color(0xFE, 0xE2, 0xE2));
                chip.setFocusPainted(false);
                chip.setCursor(new Cursor(Cursor.HAND_CURSOR));
                chip.setBorder(new CompoundBorder(
                        new LineBorder(new Color(0xFC, 0xA5, 0xA5), 1, true),
                        new EmptyBorder(3, 8, 3, 8)
                ));
                chip.addActionListener(e -> {
                    startDateField.setText(d);
                    endDateField.setText(d);
                    findMissedLecturesForRange();
                });
                absentQuickPickRow.add(chip);
            }

            // If multiple dates exist, add a "Select All Absent Range" button
            if (dateMap.size() > 1) {
                List<String> dates = new ArrayList<>(dateMap.keySet());
                String earliest = dates.get(dates.size() - 1);
                String latest = dates.get(0);
                if (earliest.compareTo(latest) > 0) {
                    String tmp = earliest; earliest = latest; latest = tmp;
                }
                final String fEarly = earliest;
                final String fLate = latest;

                JButton allRangeBtn = new JButton("Cover Full Range (" + fEarly + " to " + fLate + ")");
                allRangeBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
                allRangeBtn.setForeground(COLOR_MAIN_BLUE);
                allRangeBtn.setBackground(COLOR_LIGHT_BLUE);
                allRangeBtn.setFocusPainted(false);
                allRangeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                allRangeBtn.setBorder(new CompoundBorder(
                        new LineBorder(new Color(0xBF, 0xDB, 0xFE), 1, true),
                        new EmptyBorder(3, 8, 3, 8)
                ));
                allRangeBtn.addActionListener(e -> {
                    startDateField.setText(fEarly);
                    endDateField.setText(fLate);
                    findMissedLecturesForRange();
                });
                absentQuickPickRow.add(allRangeBtn);
            }
        }

        absentQuickPickRow.revalidate();
        absentQuickPickRow.repaint();
    }

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
}
