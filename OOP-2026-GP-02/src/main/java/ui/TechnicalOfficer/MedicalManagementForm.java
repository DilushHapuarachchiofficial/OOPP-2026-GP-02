package main.java.ui.TechnicalOfficer;

import main.java.model.TechnicalOfficer.TechnicalOfficer;
import main.resources.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static main.java.ui.TechnicalOfficer.TechnicalOfficerDashboard.*;

/**
 * MedicalManagementForm - Technical Officer Module for TecFAMS.
 * Faculty of Technology - University of Ruhuna.
 * 
 * Features:
 * - Summary metric cards (Total, Pending, Approved, Rejected)
 * - Filter & Search bar (Reg No/Name, Status, Date range, Search & Reset)
 * - Full-width Medical Records table in the center displaying submitted student records
 * - Separate Review & Verification modal dialog (JDialog) for reviewing full submission details,
 *   inspecting medical certificate (PDF/Image), and approving or rejecting the submission.
 * - Live database persistence of verification status and officer audit trail.
 */
public class MedicalManagementForm extends JFrame {

    private final JFrame parentDashboard;
    private final TechnicalOfficer currentOfficer;

    // Summary Card Labels
    private JLabel lblTotalCount, lblPendingCount, lblApprovedCount, lblRejectedCount;

    // Search and Filter Controls
    private JTextField txtSearch, txtDateFrom, txtDateTo;
    private JComboBox<String> cmbStatusFilter;
    private JButton btnSearch, btnResetFilter;

    // Table Controls
    private JTable tblMedicals;
    private DefaultTableModel tableModel;
    private JLabel lblTableInfo;
    private JButton btnReviewSelected, btnRefreshTable;

    public MedicalManagementForm() {
        this(null, new TechnicalOfficer());
    }

    public MedicalManagementForm(JFrame parentDashboard) {
        this(parentDashboard, new TechnicalOfficer());
    }

    public MedicalManagementForm(JFrame parentDashboard, TechnicalOfficer officer) {
        this.parentDashboard = parentDashboard;
        this.currentOfficer = officer != null ? officer : new TechnicalOfficer();
        initFrameSettings();
        buildUI();
        loadInitialData();
    }

    private void initFrameSettings() {
        setTitle("TecFAMS - Medical Records Management (Technical Officer)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1320, 840);
        setMinimumSize(new Dimension(1080, 700));
        setLocationRelativeTo(parentDashboard);
    }

    private void buildUI() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);
        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel centerContainer = new JPanel(new BorderLayout(0, 14));
        centerContainer.setBackground(BG_COLOR);
        centerContainer.setBorder(new EmptyBorder(16, 22, 18, 22));

        JPanel topStrip = new JPanel();
        topStrip.setLayout(new BoxLayout(topStrip, BoxLayout.Y_AXIS));
        topStrip.setOpaque(false);
        topStrip.add(createSummaryCardsRow());
        topStrip.add(Box.createVerticalStrut(14));
        topStrip.add(createFilterCard());

        centerContainer.add(topStrip, BorderLayout.NORTH);
        centerContainer.add(createTableCard(), BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(12, 24, 12, 24)));

        JPanel leftBrand = new JPanel(new GridLayout(2, 1, 0, 2));
        leftBrand.setOpaque(false);
        JLabel brandTitle = new JLabel("TecFAMS  •  Medical Records Management");
        brandTitle.setFont(FONT_HEADER_LARGE);
        brandTitle.setForeground(PRIMARY_NAVY);

        JLabel brandSubtitle = new JLabel("Faculty of Technology • University of Ruhuna | Technical Officer Absence Verification");
        brandSubtitle.setFont(FONT_SMALL);
        brandSubtitle.setForeground(TEXT_SECONDARY);
        leftBrand.add(brandTitle);
        leftBrand.add(brandSubtitle);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightActions.setOpaque(false);

        JLabel officerBadge = new JLabel("👤 Officer: " + currentOfficer.getName());
        officerBadge.setFont(FONT_SMALL);
        officerBadge.setForeground(PRIMARY_NAVY);
        officerBadge.setBackground(LIGHT_BLUE);
        officerBadge.setOpaque(true);
        officerBadge.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#BFDBFE"), 1, true),
                new EmptyBorder(6, 14, 6, 14)));

        JButton btnBack = createSecondaryButton("← Back to Dashboard");
        btnBack.addActionListener(e -> backToDashboard());
        rightActions.add(officerBadge);
        rightActions.add(btnBack);

        header.add(leftBrand, BorderLayout.WEST);
        header.add(rightActions, BorderLayout.EAST);
        return header;
    }

    private JPanel createSummaryCardsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        lblTotalCount = new JLabel("0");
        lblPendingCount = new JLabel("0");
        lblApprovedCount = new JLabel("0");
        lblRejectedCount = new JLabel("0");

        row.add(createMetricCard("Total Medical Records", lblTotalCount, "All registered submissions", PRIMARY_BLUE, "🗎"));
        row.add(createMetricCard("Pending Approval", lblPendingCount, "Awaiting officer review", WARNING_AMBER, "⌛"));
        row.add(createMetricCard("Approved Records", lblApprovedCount, "Absence verified & granted", SUCCESS_GREEN, "☑"));
        row.add(createMetricCard("Rejected Records", lblRejectedCount, "Declined / invalid submissions", Color.decode("#EF4444"), "✖"));
        return row;
    }

    private JPanel createMetricCard(String title, JLabel countLabel, String subtext, Color accent, String symbol) {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 4));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_SMALL);
        titleLabel.setForeground(TEXT_SECONDARY);

        JLabel symLabel = new JLabel(symbol);
        symLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        symLabel.setForeground(accent);
        top.add(titleLabel, BorderLayout.WEST);
        top.add(symLabel, BorderLayout.EAST);

        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        countLabel.setForeground(TEXT_MAIN);

        JLabel subLabel = new JLabel(subtext);
        subLabel.setFont(FONT_SMALL);
        subLabel.setForeground(accent);

        card.add(top, BorderLayout.NORTH);
        card.add(countLabel, BorderLayout.CENTER);
        card.add(subLabel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createFilterCard() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JLabel cardTitle = new JLabel("Filter & Search Medical Submissions");
        cardTitle.setFont(FONT_HEADER_SMALL);
        cardTitle.setForeground(PRIMARY_NAVY);
        card.add(cardTitle, BorderLayout.NORTH);

        JPanel filtersRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 4));
        filtersRow.setOpaque(false);

        txtSearch = new JTextField(16);
        txtSearch.setFont(FONT_REGULAR);

        cmbStatusFilter = new JComboBox<>(new String[]{"All Statuses", "Pending", "Approved", "Rejected"});
        cmbStatusFilter.setFont(FONT_REGULAR);
        cmbStatusFilter.setPreferredSize(new Dimension(135, 32));

        txtDateFrom = new JTextField(9);
        txtDateFrom.setFont(FONT_REGULAR);
        txtDateTo = new JTextField(9);
        txtDateTo.setFont(FONT_REGULAR);

        btnSearch = createPrimaryButton("🔍 Search");
        btnSearch.addActionListener(e -> applyFilters());
        btnResetFilter = createSecondaryButton("↺ Reset");
        btnResetFilter.addActionListener(e -> resetFilters());

        filtersRow.add(createInlineField("Search Reg No / Name:", txtSearch));
        filtersRow.add(createInlineField("Status:", cmbStatusFilter));
        filtersRow.add(createInlineField("From Date:", txtDateFrom));
        filtersRow.add(createInlineField("To Date:", txtDateTo));
        filtersRow.add(btnSearch);
        filtersRow.add(btnResetFilter);

        card.add(filtersRow, BorderLayout.CENTER);
        return card;
    }

    private JPanel createInlineField(String labelText, JComponent comp) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(labelText);
        l.setFont(FONT_SMALL);
        l.setForeground(TEXT_MAIN);
        p.add(l);
        p.add(comp);
        return p;
    }

    /**
     * Creates the full-width medical records table card placed in the center of the UI.
     */
    private JPanel createTableCard() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Submitted Student Medical Records");
        title.setFont(FONT_HEADER_MEDIUM);
        title.setForeground(PRIMARY_NAVY);

        lblTableInfo = new JLabel("Loading database records...");
        lblTableInfo.setFont(FONT_SMALL);
        lblTableInfo.setForeground(TEXT_SECONDARY);
        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(lblTableInfo, BorderLayout.EAST);
        card.add(headerPanel, BorderLayout.NORTH);

        String[] columns = {
                "#", "Medical ID", "Registration No", "Student Name", "Linked Session",
                "Start Date", "End Date", "Reason / Medical Condition", "Submitted Date", "Approval Status", "Action"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tblMedicals = new JTable(tableModel);
        tblMedicals.setFont(FONT_REGULAR);
        tblMedicals.setRowHeight(38);
        tblMedicals.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblMedicals.setSelectionBackground(LIGHT_BLUE);
        tblMedicals.setSelectionForeground(TEXT_MAIN);
        tblMedicals.setGridColor(BORDER_COLOR);
        tblMedicals.setShowHorizontalLines(true);
        tblMedicals.setShowVerticalLines(true);

        // Hide Medical ID column (column 1)
        tblMedicals.getColumnModel().getColumn(1).setMinWidth(0);
        tblMedicals.getColumnModel().getColumn(1).setMaxWidth(0);
        tblMedicals.getColumnModel().getColumn(1).setPreferredWidth(0);

        int[] widths = {45, 0, 120, 160, 190, 95, 95, 230, 100, 125, 135};
        for (int i = 0; i < widths.length; i++) {
            if (i != 1) tblMedicals.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JTableHeader th = tblMedicals.getTableHeader();
        th.setFont(FONT_MEDIUM);
        th.setBackground(Color.decode("#F1F5F9"));
        th.setForeground(PRIMARY_NAVY);
        th.setPreferredSize(new Dimension(0, 36));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int col : new int[]{0, 2, 5, 6, 8}) {
            tblMedicals.getColumnModel().getColumn(col).setCellRenderer(centerRenderer);
        }

        tblMedicals.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, val, isSelected, hasFocus, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(FONT_MEDIUM);
                String status = val != null ? val.toString() : "Pending";
                if ("Approved".equalsIgnoreCase(status)) {
                    l.setForeground(Color.decode("#065F46")); l.setText("●  Approved");
                } else if ("Rejected".equalsIgnoreCase(status)) {
                    l.setForeground(Color.decode("#991B1B")); l.setText("✖  Rejected");
                } else {
                    l.setForeground(Color.decode("#92400E")); l.setText("⏳  Pending");
                }
                return l;
            }
        });

        tblMedicals.getColumnModel().getColumn(10).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, val, isSelected, hasFocus, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(FONT_MEDIUM);
                l.setForeground(PRIMARY_BLUE);
                l.setText("👁 View Details ➔");
                return l;
            }
        });

        // Click listeners: Single click on Action column or double click on any row opens review dialog
        tblMedicals.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblMedicals.rowAtPoint(e.getPoint());
                int col = tblMedicals.columnAtPoint(e.getPoint());
                if (row >= 0) {
                    if (col == 10 || e.getClickCount() == 2) {
                        int medId = (Integer) tableModel.getValueAt(row, 1);
                        openMedicalDetailsDialog(medId);
                    }
                }
            }
        });

        tblMedicals.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = tblMedicals.columnAtPoint(e.getPoint());
                if (col == 10) {
                    tblMedicals.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    tblMedicals.setCursor(Cursor.getDefaultCursor());
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblMedicals);
        scroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        scroll.getViewport().setBackground(WHITE);
        card.add(scroll, BorderLayout.CENTER);

        // Bottom Table Action Row
        JPanel tableFooter = new JPanel(new BorderLayout(10, 0));
        tableFooter.setOpaque(false);
        tableFooter.setBorder(new EmptyBorder(8, 4, 4, 4));

        JLabel hint = new JLabel("💡 Click 'View Details ➔' on any row or double-click to view medical certificate and approve/reject.");
        hint.setFont(FONT_SMALL);
        hint.setForeground(TEXT_SECONDARY);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        btnReviewSelected = createPrimaryButton("👁 View & Review Selected");
        btnReviewSelected.addActionListener(e -> {
            int row = tblMedicals.getSelectedRow();
            if (row >= 0) {
                int medId = (Integer) tableModel.getValueAt(row, 1);
                openMedicalDetailsDialog(medId);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a medical record from the table first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnRefreshTable = createSecondaryButton("↺ Refresh List");
        btnRefreshTable.addActionListener(e -> {
            loadSummaryCounts();
            applyFilters();
        });

        btnRow.add(btnReviewSelected);
        btnRow.add(btnRefreshTable);

        tableFooter.add(hint, BorderLayout.WEST);
        tableFooter.add(btnRow, BorderLayout.EAST);
        card.add(tableFooter, BorderLayout.SOUTH);

        return card;
    }

    private static JButton createColorButton(String text, Color bg, Color hoverBg) {
        JButton button = new JButton(text);
        button.setFont(FONT_MEDIUM);
        button.setForeground(WHITE);
        button.setBackground(bg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bg);
            }
        });
        return button;
    }

    private JTextField createFieldWrapperField(String value) {
        JTextField tf = new JTextField(value != null ? value : "");
        tf.setEditable(false);
        tf.setFocusable(false);
        tf.setCursor(Cursor.getDefaultCursor());
        tf.setFont(FONT_REGULAR);
        tf.setForeground(TEXT_MAIN);
        tf.setBackground(Color.decode("#F1F5F9"));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(6, 8, 6, 8)));
        return tf;
    }

    private JPanel createFieldWrapper(String labelText, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel l = new JLabel(labelText);
        l.setFont(FONT_SMALL);
        l.setForeground(TEXT_MAIN);
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JPanel createModalCard(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_HEADER_SMALL);
        lblTitle.setForeground(PRIMARY_NAVY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(10));
        return card;
    }

    // =========================================================================
    // SEPARATE REVIEW & VERIFICATION MODAL DIALOG
    // =========================================================================

    /**
     * Opens a dedicated modal review & verification dialog for the selected medical record.
     * Technical Officers can view all student submission details, inspect the medical certificate,
     * and update approval decisions directly to the database.
     */
    public void openMedicalDetailsDialog(int medicalId) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Could not establish database connection.", "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "SELECT m.medical_id, m.student_id, st.student_reg_no, st.full_name, st.academic_year, st.email, st.phone, " +
                "m.session_id, sess.session_number, sess.session_type, sess.session_date, " +
                "c.course_code, c.course_name, m.start_date, m.end_date, m.reason, m.document_path, m.submitted_date, m.approval_status, " +
                "m.verified_by, to_off.officer_name as verified_by_name " +
                "FROM medicals m " +
                "LEFT JOIN students st ON m.student_id = st.student_id " +
                "LEFT JOIN attendance_sessions sess ON m.session_id = sess.session_id " +
                "LEFT JOIN courses c ON sess.course_id = c.course_id " +
                "LEFT JOIN technical_officers to_off ON m.verified_by = to_off.officer_id " +
                "WHERE m.medical_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "No record found for Medical ID #" + medicalId, "Notice", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String regNo = rs.getString("student_reg_no");
                String fullName = rs.getString("full_name");
                int academicYear = rs.getInt("academic_year");
                String email = rs.getString("email");
                String phone = rs.getString("phone");

                Date startD = rs.getDate("start_date");
                Date endD = rs.getDate("end_date");
                Date submittedD = rs.getDate("submitted_date");
                String reason = rs.getString("reason");
                String docPath = rs.getString("document_path");
                String approvalStatus = rs.getString("approval_status");
                if (approvalStatus == null || approvalStatus.trim().isEmpty()) approvalStatus = "Pending";

                int sessId = rs.getInt("session_id");
                String sessionDesc;
                if (!rs.wasNull() && sessId > 0) {
                    sessionDesc = String.format("Session #%d (%s) - %s: %s [%s]",
                            rs.getInt("session_number"), rs.getString("session_type"),
                            rs.getString("course_code"), rs.getString("course_name") != null ? rs.getString("course_name") : "",
                            formatDate(rs.getDate("session_date")));
                } else {
                    sessionDesc = "No specific session associated (General Medical Absence)";
                }

                String reviewerName = rs.getString("verified_by_name");
                int reviewerId = rs.getInt("verified_by");
                String reviewStatusText;
                if (reviewerName != null && !reviewerName.isEmpty()) {
                    reviewStatusText = "Verified by: " + reviewerName + " (ID #" + reviewerId + ")";
                } else if (reviewerId > 0) {
                    reviewStatusText = "Verified by: Officer #" + reviewerId;
                } else {
                    reviewStatusText = "Review Status: Unreviewed (Awaiting Decision)";
                }

                // Calculate duration in days
                String durationText = "-";
                if (startD != null && endD != null) {
                    try {
                        long days = ChronoUnit.DAYS.between(startD.toLocalDate(), endD.toLocalDate()) + 1;
                        durationText = days + (days == 1 ? " Day" : " Days");
                    } catch (Exception ignored) {}
                }

                showVerificationModal(medicalId, regNo, fullName, academicYear, email, phone,
                        startD, endD, durationText, submittedD, reason, sessionDesc, docPath,
                        approvalStatus, reviewStatusText);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading medical record #" + medicalId + ": " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void showVerificationModal(int medicalId, String regNo, String fullName, int academicYear,
                                       String email, String phone, Date startD, Date endD, String durationText,
                                       Date submittedD, String reason, String sessionDesc, String docPath,
                                       String currentStatus, String reviewStatusText) {
        JDialog dlg = new JDialog(this, "Medical Record Review & Approval - Record #" + medicalId, true);
        dlg.setSize(760, 720);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(BG_COLOR);

        // 1. Header
        JPanel dlgHeader = new JPanel(new BorderLayout());
        dlgHeader.setBackground(PRIMARY_NAVY);
        dlgHeader.setBorder(new EmptyBorder(14, 22, 14, 22));

        JLabel hTitle = new JLabel("Faculty of Technology • University of Ruhuna");
        hTitle.setFont(FONT_HEADER_MEDIUM);
        hTitle.setForeground(WHITE);

        JLabel hSub = new JLabel("Student Medical Absence Verification & Document Review");
        hSub.setFont(FONT_SMALL);
        hSub.setForeground(Color.decode("#BFDBFE"));

        JPanel hText = new JPanel(new GridLayout(2, 1, 0, 3));
        hText.setOpaque(false);
        hText.add(hTitle);
        hText.add(hSub);
        dlgHeader.add(hText, BorderLayout.WEST);

        JLabel badgeRec = new JLabel("Record ID: #" + medicalId, SwingConstants.CENTER);
        badgeRec.setFont(FONT_MEDIUM);
        badgeRec.setForeground(PRIMARY_NAVY);
        badgeRec.setBackground(WHITE);
        badgeRec.setOpaque(true);
        badgeRec.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#E2E8F0"), 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        dlgHeader.add(badgeRec, BorderLayout.EAST);
        dlg.getContentPane().add(dlgHeader, BorderLayout.NORTH);

        // 2. Scrollable Body
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(BG_COLOR);
        body.setBorder(new EmptyBorder(16, 20, 16, 20));

        // --- Card 1: Student Information ---
        JPanel studentCard = createModalCard("1. Student Information (Submitted by Student)");
        JPanel studentGrid = new JPanel(new GridLayout(2, 2, 12, 8));
        studentGrid.setOpaque(false);
        studentGrid.add(createFieldWrapper("Registration No:", createFieldWrapperField(regNo != null ? regNo : "N/A")));
        studentGrid.add(createFieldWrapper("Full Name:", createFieldWrapperField(fullName != null ? fullName : "N/A")));
        studentGrid.add(createFieldWrapper("Academic Level:", createFieldWrapperField(academicYear > 0 ? "Level " + academicYear + " Undergraduate" : "Undergraduate")));
        studentGrid.add(createFieldWrapper("Official Contact / Email:", createFieldWrapperField((email != null ? email : "") + ((phone != null && !phone.isEmpty()) ? " • " + phone : ""))));
        studentCard.add(studentGrid);
        body.add(studentCard);
        body.add(Box.createVerticalStrut(12));

        // --- Card 2: Absence Period & Clinical Condition ---
        JPanel absenceCard = createModalCard("2. Absence Period & Reason (Submitted by Student)");
        JPanel datesGrid = new JPanel(new GridLayout(1, 4, 10, 0));
        datesGrid.setOpaque(false);
        datesGrid.add(createFieldWrapper("Start Date:", createFieldWrapperField(formatDate(startD))));
        datesGrid.add(createFieldWrapper("End Date:", createFieldWrapperField(formatDate(endD))));
        datesGrid.add(createFieldWrapper("Duration:", createFieldWrapperField(durationText)));
        datesGrid.add(createFieldWrapper("Submitted Date:", createFieldWrapperField(formatDate(submittedD))));

        JTextArea txtDiag = new JTextArea(reason != null ? reason : "");
        txtDiag.setFont(FONT_REGULAR);
        txtDiag.setLineWrap(true);
        txtDiag.setWrapStyleWord(true);
        txtDiag.setEditable(false);
        txtDiag.setFocusable(false);
        txtDiag.setCursor(Cursor.getDefaultCursor());
        txtDiag.setForeground(TEXT_MAIN);
        txtDiag.setBackground(Color.decode("#F1F5F9"));
        JScrollPane diagScroll = new JScrollPane(txtDiag);
        diagScroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        diagScroll.setPreferredSize(new Dimension(0, 64));

        absenceCard.add(datesGrid);
        absenceCard.add(Box.createVerticalStrut(10));
        absenceCard.add(createFieldWrapper("Reason / Clinical Diagnosis:", diagScroll));
        body.add(absenceCard);
        body.add(Box.createVerticalStrut(12));

        // --- Card 3: Associated Academic Session ---
        JPanel sessionCard = createModalCard("3. Associated Academic Session");
        JLabel lblSess = new JLabel(sessionDesc);
        lblSess.setFont(FONT_REGULAR);
        lblSess.setForeground(TEXT_MAIN);
        lblSess.setBackground(Color.decode("#F1F5F9"));
        lblSess.setOpaque(true);
        lblSess.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        sessionCard.add(lblSess);
        body.add(sessionCard);
        body.add(Box.createVerticalStrut(12));

        // --- Card 4: Medical Certificate Document ---
        JPanel docCard = createModalCard("4. Medical Certificate Document");
        JPanel docRow = new JPanel(new BorderLayout(10, 0));
        docRow.setOpaque(false);
        JTextField txtDoc = createFieldWrapperField((docPath != null && !docPath.isEmpty()) ? docPath : "No document attached");
        JButton btnOpenCert = createPrimaryButton("👁 View / Open Medical Certificate");
        btnOpenCert.addActionListener(e -> viewDocument(docPath, medicalId, regNo, fullName, academicYear, startD, endD, submittedD, reason, sessionDesc, currentStatus, reviewStatusText));

        docRow.add(txtDoc, BorderLayout.CENTER);
        docRow.add(btnOpenCert, BorderLayout.EAST);
        docCard.add(createFieldWrapper("Certificate File Reference:", docRow));
        body.add(docCard);
        body.add(Box.createVerticalStrut(12));

        // --- Card 5: Verification & Approval Decision ---
        JPanel decisionCard = createModalCard("5. Verification & Approval Decision");
        JPanel decisionGrid = new JPanel(new GridLayout(1, 2, 14, 0));
        decisionGrid.setOpaque(false);

        JComboBox<String> cmbDecision = new JComboBox<>(new String[]{"Pending", "Approved", "Rejected"});
        cmbDecision.setFont(FONT_MEDIUM);
        cmbDecision.setBackground(WHITE);
        cmbDecision.setSelectedItem(currentStatus);

        JLabel lblRevStatus = new JLabel(reviewStatusText);
        lblRevStatus.setFont(FONT_SMALL);
        lblRevStatus.setForeground(TEXT_SECONDARY);

        decisionGrid.add(createFieldWrapper("Approval Decision (Editable by Technical Officer):", cmbDecision));
        decisionGrid.add(createFieldWrapper("Current Verification Record:", lblRevStatus));

        decisionCard.add(decisionGrid);
        body.add(decisionCard);

        JScrollPane scrollPane = new JScrollPane(body);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        dlg.getContentPane().add(scrollPane, BorderLayout.CENTER);

        // 3. Footer Action Bar
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(12, 20, 12, 20)
        ));

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftActions.setOpaque(false);
        JButton btnViewDocFooter = createSecondaryButton("👁 View Document");
        btnViewDocFooter.addActionListener(e -> viewDocument(docPath, medicalId, regNo, fullName, academicYear, startD, endD, submittedD, reason, sessionDesc, (String) cmbDecision.getSelectedItem(), reviewStatusText));
        leftActions.add(btnViewDocFooter);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton btnApproveDlg = createColorButton("✅ Approve Medical", Color.decode("#10B981"), Color.decode("#059669"));
        btnApproveDlg.addActionListener(e -> {
            boolean ok = updateMedicalStatusInDB(medicalId, "Approved");
            if (ok) {
                JOptionPane.showMessageDialog(dlg, "Medical Record #" + medicalId + " for " + fullName + " (" + regNo + ") has been APPROVED.", "Approval Successful", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
            }
        });

        JButton btnRejectDlg = createColorButton("❌ Reject Medical", Color.decode("#EF4444"), Color.decode("#DC2626"));
        btnRejectDlg.addActionListener(e -> {
            int conf = JOptionPane.showConfirmDialog(dlg, "Are you sure you want to REJECT Medical Record #" + medicalId + "?", "Confirm Rejection", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (conf == JOptionPane.YES_OPTION) {
                boolean ok = updateMedicalStatusInDB(medicalId, "Rejected");
                if (ok) {
                    JOptionPane.showMessageDialog(dlg, "Medical Record #" + medicalId + " for " + fullName + " has been REJECTED.", "Rejection Confirmed", JOptionPane.INFORMATION_MESSAGE);
                    dlg.dispose();
                }
            }
        });

        JButton btnSaveDlg = createPrimaryButton("💾 Save Status");
        btnSaveDlg.addActionListener(e -> {
            String selectedStatus = (String) cmbDecision.getSelectedItem();
            if ("Rejected".equalsIgnoreCase(selectedStatus)) {
                int conf = JOptionPane.showConfirmDialog(dlg, "Are you sure you want to mark Medical Record #" + medicalId + " as REJECTED?", "Confirm Rejection", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (conf != JOptionPane.YES_OPTION) return;
            }
            boolean ok = updateMedicalStatusInDB(medicalId, selectedStatus);
            if (ok) {
                JOptionPane.showMessageDialog(dlg, "Medical Record #" + medicalId + " status updated to '" + selectedStatus + "' successfully!", "Status Saved", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
            }
        });

        JButton btnCloseDlg = createSecondaryButton("Close");
        btnCloseDlg.addActionListener(e -> dlg.dispose());

        rightActions.add(btnApproveDlg);
        rightActions.add(btnRejectDlg);
        rightActions.add(btnSaveDlg);
        rightActions.add(btnCloseDlg);

        footer.add(leftActions, BorderLayout.WEST);
        footer.add(rightActions, BorderLayout.EAST);
        dlg.getContentPane().add(footer, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    /**
     * Persists status updates to MySQL and refreshes the table and metric counters.
     */
    public boolean updateMedicalStatusInDB(int medicalId, String status) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Could not establish database connection.", "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            int officerId = resolveTechnicalOfficerId(conn);
            String sql;
            if (("Approved".equalsIgnoreCase(status) || "Rejected".equalsIgnoreCase(status)) && officerId > 0) {
                sql = "UPDATE medicals SET approval_status = ?, verified_by = ? WHERE medical_id = ?";
            } else {
                sql = "UPDATE medicals SET approval_status = ?, verified_by = NULL WHERE medical_id = ?";
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status);
                if (("Approved".equalsIgnoreCase(status) || "Rejected".equalsIgnoreCase(status)) && officerId > 0) {
                    ps.setInt(2, officerId);
                    ps.setInt(3, medicalId);
                } else {
                    ps.setInt(2, medicalId);
                }

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    loadSummaryCounts();
                    loadMedicalRecords(txtSearch.getText().trim(), (String) cmbStatusFilter.getSelectedItem(),
                            txtDateFrom.getText().trim(), txtDateTo.getText().trim());
                    return true;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database SQL Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Opens medical document via Desktop viewer if present on local disk, or launches the in-app
     * official Certificate Preview dialog.
     */
    private void viewDocument(String docPath, int medicalId, String regNo, String fullName, int academicYear,
                              Date startD, Date endD, Date submittedD, String reason, String sessionDesc,
                              String status, String verifiedBy) {
        if (docPath == null || docPath.trim().isEmpty()) {
            docPath = "medicals/medical_sample.pdf";
        }

        File targetFile = resolveDocumentFile(docPath);
        if (targetFile != null && targetFile.exists()) {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(targetFile);
                    return;
                }
            } catch (Exception ex) {
                System.err.println("MedicalManagementForm: Desktop open failed: " + ex.getMessage());
            }
        }

        showMedicalCertificatePreviewDialog(docPath, targetFile, medicalId, regNo, fullName,
                academicYear, startD, endD, submittedD, reason, sessionDesc, status, verifiedBy);
    }

    private File resolveDocumentFile(String path) {
        if (path == null || path.trim().isEmpty()) return null;
        File direct = new File(path);
        if (direct.exists()) return direct;
        String userDir = System.getProperty("user.dir");
        File rel = new File(userDir, path);
        if (rel.exists()) return rel;
        File medDir = new File(userDir, "medicals" + File.separator + new File(path).getName());
        return medDir.exists() ? medDir : null;
    }

    /**
     * Displays a formatted Medical Certificate Review & Verification dialog with official faculty layout.
     */
    private void showMedicalCertificatePreviewDialog(String docPath, File targetFile, int medicalId,
                                                     String regNo, String fullName, int academicYear,
                                                     Date startD, Date endD, Date submittedD, String reason,
                                                     String sessionDesc, String status, String verifiedBy) {
        JDialog dlg = new JDialog(this, "Medical Certificate Document Preview - Record #" + medicalId, true);
        dlg.setSize(680, 620);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(BG_COLOR);

        // Header Panel
        JPanel dlgHeader = new JPanel(new BorderLayout());
        dlgHeader.setBackground(PRIMARY_NAVY);
        dlgHeader.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel hTitle = new JLabel("Faculty of Technology • University of Ruhuna");
        hTitle.setFont(FONT_HEADER_MEDIUM);
        hTitle.setForeground(WHITE);

        JLabel hSub = new JLabel("Official Student Medical Certificate & Absence Verification Record");
        hSub.setFont(FONT_SMALL);
        hSub.setForeground(Color.decode("#BFDBFE"));

        JPanel hText = new JPanel(new GridLayout(2, 1, 0, 4));
        hText.setOpaque(false);
        hText.add(hTitle);
        hText.add(hSub);
        dlgHeader.add(hText, BorderLayout.WEST);

        JLabel badgeRec = new JLabel("Record #" + medicalId, SwingConstants.CENTER);
        badgeRec.setFont(FONT_MEDIUM);
        badgeRec.setForeground(PRIMARY_NAVY);
        badgeRec.setBackground(WHITE);
        badgeRec.setOpaque(true);
        badgeRec.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#E2E8F0"), 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        dlgHeader.add(badgeRec, BorderLayout.EAST);
        dlg.getContentPane().add(dlgHeader, BorderLayout.NORTH);

        // Body Panel
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(BG_COLOR);
        body.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Certificate Card
        JPanel paper = new JPanel();
        paper.setLayout(new BoxLayout(paper, BoxLayout.Y_AXIS));
        paper.setBackground(WHITE);
        paper.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JLabel certHeading = new JLabel("✚  MEDICAL ABSENCE CERTIFICATION DOCUMENT");
        certHeading.setFont(FONT_HEADER_SMALL);
        certHeading.setForeground(PRIMARY_NAVY);
        paper.add(certHeading);
        paper.add(Box.createVerticalStrut(12));

        // Details Grid
        JPanel infoGrid = new JPanel(new GridLayout(4, 2, 12, 10));
        infoGrid.setOpaque(false);

        infoGrid.add(createPreviewField("Student Reg. Number:", regNo != null ? regNo : "N/A"));
        infoGrid.add(createPreviewField("Student Full Name:", fullName != null ? fullName : "N/A"));
        infoGrid.add(createPreviewField("Academic Level:", academicYear > 0 ? "Level " + academicYear + " Undergraduate" : "Undergraduate"));
        infoGrid.add(createPreviewField("Submitted Date:", formatDate(submittedD)));
        infoGrid.add(createPreviewField("Absence Start Date:", formatDate(startD)));
        infoGrid.add(createPreviewField("Absence End Date:", formatDate(endD)));
        infoGrid.add(createPreviewField("Document Reference:", (docPath != null && !docPath.isEmpty()) ? docPath : "None attached"));
        infoGrid.add(createPreviewField("Current Status:", status != null ? status : "Pending"));

        paper.add(infoGrid);
        paper.add(Box.createVerticalStrut(14));

        // Session
        JPanel sessPanel = new JPanel(new BorderLayout(0, 4));
        sessPanel.setOpaque(false);
        JLabel sessLbl = new JLabel("Linked Academic Session:");
        sessLbl.setFont(FONT_SMALL);
        sessLbl.setForeground(TEXT_SECONDARY);
        JLabel sessVal = new JLabel(sessionDesc != null ? sessionDesc : "General Medical Absence");
        sessVal.setFont(FONT_REGULAR);
        sessVal.setForeground(TEXT_MAIN);
        sessPanel.add(sessLbl, BorderLayout.NORTH);
        sessPanel.add(sessVal, BorderLayout.CENTER);
        paper.add(sessPanel);
        paper.add(Box.createVerticalStrut(12));

        // Reason / Medical Diagnosis
        JPanel reasonPanel = new JPanel(new BorderLayout(0, 4));
        reasonPanel.setOpaque(false);
        JLabel rLbl = new JLabel("Diagnosis / Medical Reason provided by Student / Registered Physician:");
        rLbl.setFont(FONT_SMALL);
        rLbl.setForeground(TEXT_SECONDARY);
        JTextArea rArea = new JTextArea(reason != null ? reason : "");
        rArea.setFont(FONT_REGULAR);
        rArea.setEditable(false);
        rArea.setLineWrap(true);
        rArea.setWrapStyleWord(true);
        rArea.setBackground(Color.decode("#F8FAFC"));
        rArea.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        reasonPanel.add(rLbl, BorderLayout.NORTH);
        reasonPanel.add(rArea, BorderLayout.CENTER);
        paper.add(reasonPanel);
        paper.add(Box.createVerticalStrut(14));

        // Verification banner
        JPanel vBanner = new JPanel(new BorderLayout());
        vBanner.setBackground(Color.decode("#F1F5F9"));
        vBanner.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        JLabel vText = new JLabel(verifiedBy != null ? verifiedBy : "Review Status: Unreviewed");
        vText.setFont(FONT_SMALL);
        vText.setForeground(TEXT_MAIN);
        vBanner.add(vText, BorderLayout.WEST);
        paper.add(vBanner);

        body.add(paper);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_COLOR);
        dlg.getContentPane().add(scroll, BorderLayout.CENTER);

        // Footer Actions Panel
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(12, 20, 12, 20)
        ));

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftActions.setOpaque(false);
        if (targetFile != null && targetFile.exists()) {
            JButton btnOpenExt = createSecondaryButton("📂 Open Original File");
            btnOpenExt.addActionListener(e -> {
                try {
                    Desktop.getDesktop().open(targetFile);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, "Error opening external file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            leftActions.add(btnOpenExt);
        }

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton dlgApprove = createColorButton("✅ Approve", Color.decode("#10B981"), Color.decode("#059669"));
        dlgApprove.addActionListener(e -> {
            boolean ok = updateMedicalStatusInDB(medicalId, "Approved");
            if (ok) {
                JOptionPane.showMessageDialog(dlg, "Medical Record #" + medicalId + " has been APPROVED.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
            }
        });

        JButton dlgReject = createColorButton("❌ Reject", Color.decode("#EF4444"), Color.decode("#DC2626"));
        dlgReject.addActionListener(e -> {
            int conf = JOptionPane.showConfirmDialog(dlg, "Are you sure you want to REJECT Medical Record #" + medicalId + "?", "Confirm Rejection", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (conf == JOptionPane.YES_OPTION) {
                boolean ok = updateMedicalStatusInDB(medicalId, "Rejected");
                if (ok) {
                    JOptionPane.showMessageDialog(dlg, "Medical Record #" + medicalId + " has been REJECTED.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dlg.dispose();
                }
            }
        });

        JButton dlgClose = createSecondaryButton("Close");
        dlgClose.addActionListener(e -> dlg.dispose());

        rightActions.add(dlgApprove);
        rightActions.add(dlgReject);
        rightActions.add(dlgClose);

        footer.add(leftActions, BorderLayout.WEST);
        footer.add(rightActions, BorderLayout.EAST);
        dlg.getContentPane().add(footer, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private JPanel createPreviewField(String label, String value) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(FONT_SMALL);
        l.setForeground(TEXT_SECONDARY);
        JLabel v = new JLabel((value != null && !value.isEmpty()) ? value : "-");
        v.setFont(FONT_MEDIUM);
        v.setForeground(TEXT_MAIN);
        p.add(l);
        p.add(v);
        return p;
    }

    // =========================================================================
    // DATABASE OPERATIONS & DATA MANAGEMENT
    // =========================================================================

    private void loadInitialData() {
        loadSummaryCounts();
        loadMedicalRecords(null, null, null, null);
    }

    public void loadSummaryCounts() {
        String sql = "SELECT COUNT(*) as total, " +
                "COALESCE(SUM(approval_status = 'Pending'), 0) as pending, " +
                "COALESCE(SUM(approval_status = 'Approved'), 0) as approved, " +
                "COALESCE(SUM(approval_status = 'Rejected'), 0) as rejected FROM medicals";

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            lblTableInfo.setText("Database Connection Error. Please verify MySQL service.");
            return;
        }

        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            if (rs.next()) {
                lblTotalCount.setText(String.valueOf(rs.getInt("total")));
                lblPendingCount.setText(String.valueOf(rs.getInt("pending")));
                lblApprovedCount.setText(String.valueOf(rs.getInt("approved")));
                lblRejectedCount.setText(String.valueOf(rs.getInt("rejected")));
            }
        } catch (SQLException e) {
            System.err.println("MedicalManagementForm: Error loading summary counts: " + e.getMessage());
        }
    }

    public void loadMedicalRecords(String keyword, String status, String dateFrom, String dateTo) {
        tableModel.setRowCount(0);
        StringBuilder sql = new StringBuilder(
                "SELECT m.medical_id, m.student_id, s.student_reg_no, s.full_name, " +
                "m.start_date, m.end_date, m.reason, m.submitted_date, m.approval_status, " +
                "m.session_id, sess.session_number, sess.session_type, c.course_code " +
                "FROM medicals m " +
                "LEFT JOIN students s ON m.student_id = s.student_id " +
                "LEFT JOIN attendance_sessions sess ON m.session_id = sess.session_id " +
                "LEFT JOIN courses c ON sess.course_id = c.course_id WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            sql.append("AND (LOWER(s.student_reg_no) LIKE ? OR LOWER(s.full_name) LIKE ?) ");
            params.add(kw); params.add(kw);
        }
        if (status != null && !status.equalsIgnoreCase("All Statuses") && !status.trim().isEmpty()) {
            sql.append("AND m.approval_status = ? ");
            params.add(status.trim());
        }
        LocalDate fromDate = parseLocalDate(dateFrom);
        if (fromDate != null) {
            sql.append("AND m.start_date >= ? ");
            params.add(Date.valueOf(fromDate));
        }
        LocalDate toDate = parseLocalDate(dateTo);
        if (toDate != null) {
            sql.append("AND m.end_date <= ? ");
            params.add(Date.valueOf(toDate));
        }
        sql.append("ORDER BY m.medical_id DESC");

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            lblTableInfo.setText("Database Connection Error.");
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                int rowNum = 1;
                while (rs.next()) {
                    int medId = rs.getInt("medical_id");
                    String regNo = rs.getString("student_reg_no");
                    String name = rs.getString("full_name");
                    String reason = rs.getString("reason");
                    String appStatus = rs.getString("approval_status");

                    int sessId = rs.getInt("session_id");
                    String sessionLabel;
                    if (!rs.wasNull() && sessId > 0) {
                        String cCode = rs.getString("course_code");
                        sessionLabel = String.format("Session #%d (%s) - %s",
                                rs.getInt("session_number"), rs.getString("session_type"),
                                cCode != null ? cCode : "");
                    } else {
                        sessionLabel = "General Absence";
                    }

                    tableModel.addRow(new Object[]{
                            rowNum++,
                            medId,
                            regNo != null ? regNo : "N/A",
                            name != null ? name : "N/A",
                            sessionLabel,
                            formatDate(rs.getDate("start_date")),
                            formatDate(rs.getDate("end_date")),
                            reason != null ? reason : "",
                            formatDate(rs.getDate("submitted_date")),
                            appStatus != null ? appStatus : "Pending",
                            "👁 View Details ➔"
                    });
                }
                lblTableInfo.setText("Displaying " + tableModel.getRowCount() + " records");
            }
        } catch (SQLException e) {
            lblTableInfo.setText("SQL Error: " + e.getMessage());
            System.err.println("MedicalManagementForm: Error loading medicals: " + e.getMessage());
        }
    }

    private int resolveTechnicalOfficerId(Connection conn) {
        try {
            String rawId = currentOfficer.getOfficerId();
            if (rawId != null && rawId.matches("\\d+")) return Integer.parseInt(rawId);

            String[][] lookupParams = {
                {"SELECT officer_id FROM technical_officers WHERE email = ? LIMIT 1", currentOfficer.getEmail()},
                {"SELECT officer_id FROM technical_officers WHERE officer_name = ? LIMIT 1", currentOfficer.getName()}
            };

            for (String[] lp : lookupParams) {
                if (lp[1] != null && !lp[1].trim().isEmpty()) {
                    try (PreparedStatement ps = conn.prepareStatement(lp[0])) {
                        ps.setString(1, lp[1].trim());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) return rs.getInt("officer_id");
                        }
                    }
                }
            }

            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT officer_id FROM technical_officers LIMIT 1")) {
                if (rs.next()) return rs.getInt("officer_id");
            }
        } catch (Exception ignored) {
        }
        return -1;
    }

    private void applyFilters() {
        loadMedicalRecords(txtSearch.getText().trim(), (String) cmbStatusFilter.getSelectedItem(),
                txtDateFrom.getText().trim(), txtDateTo.getText().trim());
    }

    private void resetFilters() {
        txtSearch.setText("");
        cmbStatusFilter.setSelectedIndex(0);
        txtDateFrom.setText("");
        txtDateTo.setText("");
        loadMedicalRecords(null, null, null, null);
    }

    private void backToDashboard() {
        if (parentDashboard instanceof TechnicalOfficerDashboard) {
            ((TechnicalOfficerDashboard) parentDashboard).showDashboardView();
        } else {
            dispose();
            if (parentDashboard != null) {
                parentDashboard.setVisible(true);
                parentDashboard.toFront();
            }
        }
    }

    private static String formatDate(Date d) {
        return d != null ? d.toString() : "";
    }

    private static LocalDate parseLocalDate(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    /**
     * Standalone main method to preview and run the Medical Management Form independently.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            MedicalManagementForm form = new MedicalManagementForm();
            form.setVisible(true);
        });
    }
}
