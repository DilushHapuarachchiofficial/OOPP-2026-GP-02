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
    private JPanel scrollContent;
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

        scrollContent = new ScrollablePanel();
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
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        totalSubmissionsLabel = new JLabel("0");
        approvedCountLabel = new JLabel("0");
        pendingCountLabel = new JLabel("0");
        missedSessionsCountLabel = new JLabel("0");

        row.add(createMetricCard("TOTAL SUBMISSIONS", totalSubmissionsLabel, COLOR_PRIMARY_NAVY, "Faculty Registry"));
        row.add(createMetricCard("APPROVED MEDICALS", approvedCountLabel, COLOR_SUCCESS, "Counted towards 80%"));
        row.add(createMetricCard("PENDING VERIFICATION", pendingCountLabel, COLOR_WARNING, "Under UMO Review"));
        row.add(createMetricCard("MISSED SESSIONS", missedSessionsCountLabel, COLOR_DANGER, "Absent Lectures"));

        return row;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color valColor, String subtitle) {
        JPanel card = new JPanel(new BorderLayout(8, 2));
        card.setBackground(COLOR_WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(8, 12, 8, 12)));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 10));
        t.setForeground(COLOR_TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(valColor);

        JLabel sub = new JLabel(subtitle);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(COLOR_TEXT_MUTED);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(t);
        left.add(sub);

        card.add(left, BorderLayout.CENTER);
        card.add(valueLabel, BorderLayout.EAST);
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

        JLabel hint = new JLabel("Enter your medical leave date range and click 'Find Missed Lectures' to select your absent sessions.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(COLOR_TEXT_MUTED);

        formHeader.add(title, BorderLayout.NORTH);
        formHeader.add(hint, BorderLayout.SOUTH);
        card.add(formHeader, BorderLayout.NORTH);

        // Form Fields Grid
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setOpaque(false);

        // 1. Date Range Input Row
        JPanel datesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        datesRow.setOpaque(false);
        datesRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel startLbl = new JLabel("From Date:*");
        startLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        startLbl.setForeground(COLOR_TEXT_PRIMARY);
        datesRow.add(startLbl);

        startDateField = new JTextField("", 10);
        startDateField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        startDateField.setPreferredSize(new Dimension(110, 34));
        startDateField.setToolTipText("e.g. 2026-10-13");
        startDateField.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1), new EmptyBorder(4, 8, 4, 8)));
        datesRow.add(startDateField);

        JLabel endLbl = new JLabel("To Date:*");
        endLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        endLbl.setForeground(COLOR_TEXT_PRIMARY);
        datesRow.add(endLbl);

        endDateField = new JTextField("", 10);
        endDateField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        endDateField.setPreferredSize(new Dimension(110, 34));
        endDateField.setToolTipText("e.g. 2026-10-15");
        endDateField.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1), new EmptyBorder(4, 8, 4, 8)));
        datesRow.add(endDateField);

        JButton checkSessionsBtn = createColoredButton("🔍 Find Missed Lectures in Period",
                new Color(0x25, 0x63, 0xEB), new Color(0x1D, 0x4E, 0xD8), Color.WHITE, new Dimension(245, 34));
        checkSessionsBtn.addActionListener(e -> findMissedLecturesForRange());
        datesRow.add(checkSessionsBtn);

        JLabel hintDatesLbl = new JLabel("(Format: YYYY-MM-DD)");
        hintDatesLbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hintDatesLbl.setForeground(COLOR_TEXT_MUTED);
        datesRow.add(hintDatesLbl);

        fieldsPanel.add(datesRow);
        fieldsPanel.add(Box.createVerticalStrut(10));

        // 2. Detected Missed Lectures Checklist Container
        JPanel detectedBox = new JPanel(new BorderLayout(0, 6));
        detectedBox.setBackground(new Color(0xF8, 0xFA, 0xFC));
        detectedBox.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(10, 14, 10, 14)));
        detectedBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel detectedHeader = new JPanel(new BorderLayout());
        detectedHeader.setOpaque(false);

        sessionsFoundCountLabel = new JLabel("Missed Lectures in Period (Enter dates above and search):");
        sessionsFoundCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sessionsFoundCountLabel.setForeground(COLOR_PRIMARY_NAVY);
        detectedHeader.add(sessionsFoundCountLabel, BorderLayout.WEST);

        JPanel selectButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        selectButtons.setOpaque(false);

        JButton selectAllBtn = new JButton("Select All");
        selectAllBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        selectAllBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        selectAllBtn.setForeground(COLOR_MAIN_BLUE);
        selectAllBtn.setBackground(COLOR_LIGHT_BLUE);
        selectAllBtn.setFocusPainted(false);
        selectAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        selectAllBtn.setBorder(new CompoundBorder(new LineBorder(new Color(0xBF, 0xDB, 0xFE), 1, true), new EmptyBorder(3, 8, 3, 8)));
        selectAllBtn.addActionListener(e -> {
            for (JCheckBox cb : sessionCheckBoxes) cb.setSelected(true);
        });

        JButton deselectAllBtn = new JButton("Deselect All");
        deselectAllBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        deselectAllBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        deselectAllBtn.setForeground(COLOR_TEXT_MUTED);
        deselectAllBtn.setBackground(COLOR_WHITE);
        deselectAllBtn.setFocusPainted(false);
        deselectAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deselectAllBtn.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(3, 8, 3, 8)));
        deselectAllBtn.addActionListener(e -> {
            for (JCheckBox cb : sessionCheckBoxes) cb.setSelected(false);
        });

        selectButtons.add(selectAllBtn);
        selectButtons.add(deselectAllBtn);
        detectedHeader.add(selectButtons, BorderLayout.EAST);

        detectedBox.add(detectedHeader, BorderLayout.NORTH);

        sessionsChecklistContainer = new JPanel();
        sessionsChecklistContainer.setLayout(new BoxLayout(sessionsChecklistContainer, BoxLayout.Y_AXIS));
        sessionsChecklistContainer.setOpaque(false);

        // Put sessions directly into detectedBox without nested scrollpane
        detectedBox.add(sessionsChecklistContainer, BorderLayout.CENTER);

        // Show initial placeholder prompt
        showInitialSearchPrompt();

        fieldsPanel.add(detectedBox);
        fieldsPanel.add(Box.createVerticalStrut(12));

        // 3. Illness Reason Input
        JPanel reasonRow = new JPanel(new BorderLayout(12, 0));
        reasonRow.setOpaque(false);
        reasonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        reasonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel reasonLbl = new JLabel("Illness Reason / Diagnosis:*");
        reasonLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        reasonLbl.setForeground(COLOR_TEXT_PRIMARY);
        reasonLbl.setPreferredSize(new Dimension(170, 32));

        reasonField = new JTextField();
        reasonField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        reasonField.setPreferredSize(new Dimension(300, 32));
        reasonField.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1), new EmptyBorder(4, 8, 4, 8)));

        reasonRow.add(reasonLbl, BorderLayout.WEST);
        reasonRow.add(reasonField, BorderLayout.CENTER);
        fieldsPanel.add(reasonRow);
        fieldsPanel.add(Box.createVerticalStrut(12));

        // 4. File Upload Row & Photo Preview
        JPanel uploadSection = new JPanel(new BorderLayout(14, 0));
        uploadSection.setOpaque(false);
        uploadSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        uploadSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JPanel uploadLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        uploadLeft.setOpaque(false);

        JLabel uploadLbl = new JLabel("Medical Certificate Photo:*");
        uploadLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        uploadLbl.setForeground(COLOR_TEXT_PRIMARY);
        uploadLbl.setPreferredSize(new Dimension(170, 32));
        uploadLeft.add(uploadLbl);

        JButton chooseFileBtn = new JButton("📁 Choose Photo / Slip (JPG, PNG, PDF)...");
        chooseFileBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
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

        photoPreviewLabel = new JLabel("No preview");
        photoPreviewLabel.setPreferredSize(new Dimension(100, 42));
        photoPreviewLabel.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        photoPreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        photoPreviewLabel.setForeground(COLOR_TEXT_MUTED);
        photoPreviewLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        uploadSection.add(photoPreviewLabel, BorderLayout.EAST);

        fieldsPanel.add(uploadSection);
        fieldsPanel.add(Box.createVerticalStrut(14));

        // 5. Action Buttons (Submit & Clear)
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        actionRow.setBackground(new Color(0xF1, 0xF5, 0xF9));
        actionRow.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(6, 10, 6, 10)));
        actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        JButton submitBtn = createColoredButton("✓  Submit Medical Certificate Application",
                new Color(0x10, 0xB9, 0x81), new Color(0x05, 0x96, 0x69), Color.WHITE, new Dimension(330, 42));
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        submitBtn.addActionListener(e -> submitMedicalForm());
        actionRow.add(submitBtn);

        JButton clearBtn = new JButton("Clear Form");
        clearBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearBtn.setForeground(COLOR_TEXT_PRIMARY);
        clearBtn.setBackground(COLOR_WHITE);
        clearBtn.setFocusPainted(false);
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.setPreferredSize(new Dimension(110, 42));
        clearBtn.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(8, 14, 8, 14)));
        clearBtn.addActionListener(e -> resetForm());
        actionRow.add(clearBtn);

        fieldsPanel.add(actionRow);

        card.add(fieldsPanel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Helper to create reliable, modern colored buttons that render their background
     * and text across all Look and Feels (including Windows Look & Feel).
     */
    private JButton createColoredButton(String text, Color normalBg, Color hoverBg, Color fg, Dimension size) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(hoverBg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(hoverBg);
                } else {
                    g2.setColor(normalBg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (size != null) {
            btn.setPreferredSize(size);
        }
        btn.setBorder(new EmptyBorder(6, 16, 6, 16));
        return btn;
    }

    /**
     * Displays a clean initial prompt guiding the student to enter dates and search.
     */
    private void showInitialSearchPrompt() {
        sessionsChecklistContainer.removeAll();
        sessionCheckBoxes.clear();
        currentRangeSessions.clear();

        sessionsFoundCountLabel.setText("Missed Lectures in Period (Enter dates above and search):");

        JPanel promptCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        promptCard.setBackground(new Color(0xF8, 0xFA, 0xFC));
        promptCard.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(10, 14, 10, 14)));
        promptCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        promptCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel promptLbl = new JLabel("<html><font color='#2563EB'><b>📅 Enter your medical leave dates (From Date & To Date) above</b></font> and click <b>'🔍 Find Missed Lectures in Period'</b> to check your absent sessions.</html>");
        promptLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        promptLbl.setForeground(COLOR_TEXT_PRIMARY);
        promptCard.add(promptLbl);

        sessionsChecklistContainer.add(promptCard);
        sessionsChecklistContainer.revalidate();
        sessionsChecklistContainer.repaint();
        if (scrollContent != null) {
            scrollContent.revalidate();
            scrollContent.repaint();
        }
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

            JPanel emptyCard = new JPanel(new BorderLayout(10, 4));
            emptyCard.setBackground(new Color(0xFF, 0xFB, 0xEB));
            emptyCard.setBorder(new CompoundBorder(new LineBorder(new Color(0xFC, 0xD3, 0x4D), 1, true), new EmptyBorder(10, 14, 10, 14)));
            emptyCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            emptyCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

            JLabel iconLbl = new JLabel("ℹ");
            iconLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
            iconLbl.setForeground(new Color(0xD9, 0x77, 0x06));
            emptyCard.add(iconLbl, BorderLayout.WEST);

            JLabel msgLbl = new JLabel("<html><b>No absent lectures recorded between " + startDate + " and " + endDate + ".</b><br>" +
                    "<span style='color: #4B5563;'>Recorded absent dates for TG/2024/2085 are: <b>2026-10-13, 2026-10-14, 2026-10-15</b>.<br>" +
                    "Enter these dates above to link your medical to absent sessions, or submit as a general medical certificate.</span></html>");
            msgLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            msgLbl.setForeground(COLOR_TEXT_PRIMARY);
            emptyCard.add(msgLbl, BorderLayout.CENTER);

            sessionsChecklistContainer.add(emptyCard);
        } else {
            sessionsFoundCountLabel.setText("Missed Lectures Detected: " + found.size() + " session(s) in this period (Select covered lectures):");
            for (MissedSession ms : found) {
                JPanel sessRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
                sessRow.setBackground(Color.WHITE);
                sessRow.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(4, 8, 4, 8)));
                sessRow.setAlignmentX(Component.LEFT_ALIGNMENT);
                sessRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

                JCheckBox cb = new JCheckBox();
                cb.setSelected(true);
                cb.setOpaque(false);
                cb.setCursor(new Cursor(Cursor.HAND_CURSOR));
                sessionCheckBoxes.add(cb);
                sessRow.add(cb);

                JLabel codeBadge = new JLabel(ms.getCourseCode());
                codeBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
                codeBadge.setForeground(new Color(0x1D, 0x4E, 0xD8));
                codeBadge.setBackground(new Color(0xDB, 0xEA, 0xFE));
                codeBadge.setOpaque(true);
                codeBadge.setBorder(new EmptyBorder(2, 6, 2, 6));
                sessRow.add(codeBadge);

                JLabel nameLbl = new JLabel(ms.getCourseName());
                nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                nameLbl.setForeground(COLOR_TEXT_PRIMARY);
                sessRow.add(nameLbl);

                JLabel sessTypeLbl = new JLabel("• Session #" + ms.getSessionNumber() + " (" + ms.getSessionType() + ")");
                sessTypeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                sessTypeLbl.setForeground(COLOR_TEXT_MUTED);
                sessRow.add(sessTypeLbl);

                JLabel dateBadge = new JLabel("📅 " + ms.getSessionDate());
                dateBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
                dateBadge.setForeground(new Color(0xB9, 0x1C, 0x1C));
                dateBadge.setBackground(new Color(0xFE, 0xE2, 0xE2));
                dateBadge.setOpaque(true);
                dateBadge.setBorder(new EmptyBorder(2, 6, 2, 6));
                sessRow.add(dateBadge);

                sessionsChecklistContainer.add(sessRow);
                sessionsChecklistContainer.add(Box.createVerticalStrut(4));
            }
        }

        sessionsChecklistContainer.revalidate();
        sessionsChecklistContainer.repaint();
        if (scrollContent != null) {
            scrollContent.revalidate();
            scrollContent.repaint();
        }
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
            String msg = "<html><body style='width: 360px; font-family: Segoe UI;'>" +
                    "<h3 style='color: #10B981; margin-bottom: 6px;'>✓ Medical Certificate Submitted Successfully!</h3>" +
                    "<p>Your medical certificate has been registered in the TecFAMS system.</p>" +
                    "<div style='background-color: #EAF2FF; padding: 10px; border: 1px solid #2563EB; border-radius: 4px; margin: 10px 0;'>" +
                    "<b>Reference Number: </b><span style='color: #2563EB; font-size: 14px; font-weight: bold;'>" + referenceNumber + "</span><br>" +
                    "<b>Medical Period: </b>" + startDate + " to " + endDate + "<br>" +
                    "<b>Covered Absent Sessions: </b>" + (selectedSessionIds.isEmpty() ? "General medical leave" : selectedSessionIds.size() + " session(s) in period") + "<br>" +
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
        startDateField.setText("");
        endDateField.setText("");
        reasonField.setText("");
        chosenMedicalFile = null;
        selectedFileLabel.setText("No file chosen yet");
        selectedFileLabel.setForeground(COLOR_TEXT_MUTED);
        photoPreviewLabel.setIcon(null);
        photoPreviewLabel.setText("No preview");
        showInitialSearchPrompt();
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

        // Columns (One row per medical certificate submission)
        String[] columns = {"Reference #", "Medical Period", "Illness Reason", "Certificate File", "Submitted On", "Status"};
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
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(130); // Reference #
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(180); // Medical Period
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(260); // Illness Reason
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(210); // Certificate File
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Submitted On
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Status

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        historyTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        historyTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        historyTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // Status badge pill renderer
        historyTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
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
                    if (row >= 0) {
                        int modelRow = historyTable.convertRowIndexToModel(row);
                        if (modelRow >= 0 && modelRow < submittedMedicals.size()) {
                            openDocumentFile(submittedMedicals.get(modelRow).getDocumentPath());
                        }
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

                    // Populate History Table
                    if (historyTableModel != null) {
                        historyTableModel.setRowCount(0);
                        int approved = 0;
                        int pending = 0;

                        for (MedicalRecord rec : submittedMedicals) {
                            if (rec.getStatus() == MedicalStatus.APPROVED) approved++;
                            else if (rec.getStatus() == MedicalStatus.PENDING) pending++;

                            String period = (rec.getStartDate() != null ? rec.getStartDate() : "") +
                                    (rec.getEndDate() != null && !rec.getEndDate().equals(rec.getStartDate()) ? " to " + rec.getEndDate() : "");

                            String docName = (rec.getDocumentPath() != null) ? new File(rec.getDocumentPath()).getName() : "None";

                            historyTableModel.addRow(new Object[]{
                                    rec.getReferenceNo(),
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



    private JLabel createAvatarIcon(int size, String name) {
        String pic = (loggedInStudent != null) ? loggedInStudent.getProfilePicture() : null;
        if (pic != null && !pic.trim().isEmpty() && !pic.equalsIgnoreCase("default_avatar.png")) {
            File f = new File(pic.trim());
            if (f.exists() && f.isFile()) {
                try {
                    ImageIcon icon = new ImageIcon(f.getAbsolutePath());
                    Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                    JLabel av = new JLabel(new ImageIcon(img));
                    av.setPreferredSize(new Dimension(size, size));
                    return av;
                } catch (Exception ignored) {}
            }
        }
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
