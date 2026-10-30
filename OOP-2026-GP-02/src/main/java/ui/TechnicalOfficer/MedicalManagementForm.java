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
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
 * - Medical Records table with DB columns, status badges, and details trigger
 * - Medical Details panel (view-only student info, dates, clinical reason, certificate View, approval status)
 * - Actions: Approve Medical, Reject Medical, Save Status, Clear, Refresh, View Document
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

    // Details Controls
    private JLabel lblDetailsBadge, lblSessionDisplay, lblVerifiedBy;
    private JComboBox<StudentItem> cmbStudentSelect;
    private JComboBox<SessionItem> cmbSessionSelect;
    private JComboBox<String> cmbApprovalStatus;
    private JTextField txtStudentRegNo, txtStudentName, txtStudentLevel, txtStudentContact;
    private JTextField txtStartDate, txtEndDate, txtSubmittedDate, txtDocumentPath;
    private JTextArea txtReason;
    private JButton btnViewDoc, btnApprove, btnReject, btnUpdate, btnClear, btnRefresh;

    // State
    private int currentMedicalId = -1;
    private final List<StudentItem> allStudents = new ArrayList<>();
    private final List<SessionItem> allSessions = new ArrayList<>();

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
        centerContainer.add(createMainSplitSection(), BorderLayout.CENTER);
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

        row.add(createMetricCard("Total Medical Records", lblTotalCount, "All registered submissions", "📋", PRIMARY_BLUE));
        row.add(createMetricCard("Pending Approval", lblPendingCount, "Awaiting officer review", "⏳", WARNING_AMBER));
        row.add(createMetricCard("Approved Records", lblApprovedCount, "Absence verified & granted", "✅", SUCCESS_GREEN));
        row.add(createMetricCard("Rejected Records", lblRejectedCount, "Declined / invalid submissions", "❌", Color.decode("#EF4444")));
        return row;
    }

    private JPanel createMetricCard(String title, JLabel countLabel, String subtext, String icon, Color accent) {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_SMALL);
        titleLabel.setForeground(TEXT_SECONDARY);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        top.add(titleLabel, BorderLayout.WEST);
        top.add(iconLabel, BorderLayout.EAST);

        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        countLabel.setForeground(TEXT_MAIN);
        countLabel.setBorder(new EmptyBorder(8, 0, 4, 0));

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

    private JComponent createMainSplitSection() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.62);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        splitPane.setBackground(BG_COLOR);
        splitPane.setLeftComponent(createTableCard());
        splitPane.setRightComponent(createDetailsCard());
        return splitPane;
    }

    private JPanel createTableCard() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Submitted Medical Records");
        title.setFont(FONT_HEADER_MEDIUM);
        title.setForeground(PRIMARY_NAVY);

        lblTableInfo = new JLabel("Loading database records...");
        lblTableInfo.setFont(FONT_SMALL);
        lblTableInfo.setForeground(TEXT_SECONDARY);
        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(lblTableInfo, BorderLayout.EAST);
        card.add(headerPanel, BorderLayout.NORTH);

        String[] columns = {"#", "Medical ID", "Registration No", "Student Name", "Start Date", "End Date", "Reason", "Submitted", "Approval Status", "Action"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tblMedicals = new JTable(tableModel);
        tblMedicals.setFont(FONT_REGULAR);
        tblMedicals.setRowHeight(36);
        tblMedicals.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblMedicals.setSelectionBackground(LIGHT_BLUE);
        tblMedicals.setSelectionForeground(TEXT_MAIN);
        tblMedicals.setGridColor(BORDER_COLOR);
        tblMedicals.setShowHorizontalLines(true);
        tblMedicals.setShowVerticalLines(true);

        tblMedicals.getColumnModel().getColumn(1).setMinWidth(0);
        tblMedicals.getColumnModel().getColumn(1).setMaxWidth(0);
        tblMedicals.getColumnModel().getColumn(1).setPreferredWidth(0);

        int[] widths = {36, 0, 105, 140, 85, 85, 150, 85, 100, 95};
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
        for (int col : new int[]{0, 2, 4, 5, 7}) tblMedicals.getColumnModel().getColumn(col).setCellRenderer(centerRenderer);

        tblMedicals.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
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

        tblMedicals.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, val, isSelected, hasFocus, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(FONT_MEDIUM);
                l.setForeground(PRIMARY_BLUE);
                l.setText("View Details ➔");
                return l;
            }
        });

        tblMedicals.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblMedicals.getSelectedRow();
                if (row >= 0) loadMedicalDetails((Integer) tableModel.getValueAt(row, 1));
            }
        });

        JScrollPane scroll = new JScrollPane(tblMedicals);
        scroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        scroll.getViewport().setBackground(WHITE);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel createDetailsCard() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Medical Record Details");
        title.setFont(FONT_HEADER_MEDIUM);
        title.setForeground(PRIMARY_NAVY);

        lblDetailsBadge = new JLabel("Mode: Review & Verification");
        lblDetailsBadge.setFont(FONT_SMALL);
        lblDetailsBadge.setForeground(PRIMARY_BLUE);
        lblDetailsBadge.setBackground(LIGHT_BLUE);
        lblDetailsBadge.setOpaque(true);
        lblDetailsBadge.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#BFDBFE"), 1, true),
                new EmptyBorder(4, 10, 4, 10)));

        header.add(title, BorderLayout.WEST);
        header.add(lblDetailsBadge, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        JPanel formBody = new JPanel();
        formBody.setLayout(new BoxLayout(formBody, BoxLayout.Y_AXIS));
        formBody.setBackground(WHITE);
        formBody.setBorder(new EmptyBorder(4, 4, 4, 4));

        // 1. Student Information
        cmbStudentSelect = new JComboBox<>();
        cmbStudentSelect.setFont(FONT_REGULAR);
        cmbStudentSelect.setEnabled(false);

        JPanel studentGrid = new JPanel(new GridLayout(2, 2, 10, 8));
        studentGrid.setOpaque(false);
        txtStudentRegNo = createReadOnlyField();
        txtStudentName = createReadOnlyField();
        txtStudentLevel = createReadOnlyField();
        txtStudentContact = createReadOnlyField();
        studentGrid.add(createFieldWrapper("Registration No (Student):", txtStudentRegNo));
        studentGrid.add(createFieldWrapper("Full Name:", txtStudentName));
        studentGrid.add(createFieldWrapper("Academic Level:", txtStudentLevel));
        studentGrid.add(createFieldWrapper("Official Contact / Email:", txtStudentContact));
        addSection(formBody, "1. Student Information (Read-Only)", studentGrid);

        // 2. Absence Period & Reason
        JPanel datesGrid = new JPanel(new GridLayout(1, 3, 10, 0));
        datesGrid.setOpaque(false);
        txtStartDate = createReadOnlyField();
        txtEndDate = createReadOnlyField();
        txtSubmittedDate = createReadOnlyField();
        datesGrid.add(createFieldWrapper("Start Date (Submitted):", txtStartDate));
        datesGrid.add(createFieldWrapper("End Date (Submitted):", txtEndDate));
        datesGrid.add(createFieldWrapper("Submitted Date:", txtSubmittedDate));

        txtReason = new JTextArea(3, 20);
        txtReason.setFont(FONT_REGULAR);
        txtReason.setLineWrap(true);
        txtReason.setWrapStyleWord(true);
        txtReason.setEditable(false);
        txtReason.setFocusable(false);
        txtReason.setCursor(Cursor.getDefaultCursor());
        txtReason.setForeground(TEXT_MAIN);
        txtReason.setBackground(Color.decode("#F1F5F9"));
        JScrollPane reasonScroll = new JScrollPane(txtReason);
        reasonScroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        addSection(formBody, "2. Absence Period & Reason (Submitted by Student)", datesGrid, createFieldWrapper("Reason / Medical Condition:", reasonScroll));

        // 3. Associated Academic Session
        cmbSessionSelect = new JComboBox<>();
        cmbSessionSelect.setFont(FONT_REGULAR);
        cmbSessionSelect.setEnabled(false);

        lblSessionDisplay = new JLabel("No specific session associated (General Medical Absence)");
        lblSessionDisplay.setFont(FONT_REGULAR);
        lblSessionDisplay.setForeground(TEXT_MAIN);
        lblSessionDisplay.setBackground(Color.decode("#F1F5F9"));
        lblSessionDisplay.setOpaque(true);
        lblSessionDisplay.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        addSection(formBody, "3. Associated Academic Session (Submitted by Student)", lblSessionDisplay);

        // 4. Medical Certificate Document
        JPanel docRow = new JPanel(new BorderLayout(8, 0));
        docRow.setOpaque(false);
        txtDocumentPath = createReadOnlyField();

        btnViewDoc = createPrimaryButton("👁 View Medical Certificate");
        btnViewDoc.addActionListener(e -> viewDocument());

        docRow.add(txtDocumentPath, BorderLayout.CENTER);
        docRow.add(btnViewDoc, BorderLayout.EAST);
        addSection(formBody, "4. Medical Certificate Document", createFieldWrapper("Certificate File Reference:", docRow));

        // 5. Verification & Approval Status
        JPanel statusGrid = new JPanel(new GridLayout(1, 2, 10, 0));
        statusGrid.setOpaque(false);
        cmbApprovalStatus = new JComboBox<>(new String[]{"Pending", "Approved", "Rejected"});
        cmbApprovalStatus.setFont(FONT_MEDIUM);
        cmbApprovalStatus.setBackground(WHITE);
        lblVerifiedBy = new JLabel("Review Status: Unreviewed");
        lblVerifiedBy.setFont(FONT_SMALL);
        lblVerifiedBy.setForeground(TEXT_SECONDARY);
        statusGrid.add(createFieldWrapper("Approval Decision (Editable by Technical Officer):", cmbApprovalStatus));
        statusGrid.add(createFieldWrapper("Verified By:", lblVerifiedBy));
        addSection(formBody, "5. Verification & Approval Status", statusGrid);

        // 6. Action Buttons Bar
        formBody.add(createActionButtonsSection());

        JScrollPane formScroll = new JScrollPane(formBody);
        formScroll.setBorder(null);
        formScroll.getViewport().setBackground(WHITE);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        card.add(formScroll, BorderLayout.CENTER);
        return card;
    }

    private void addSection(JPanel parent, String title, JComponent... comps) {
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_HEADER_SMALL);
        titleLabel.setForeground(PRIMARY_NAVY);
        parent.add(titleLabel);
        parent.add(Box.createVerticalStrut(6));
        for (JComponent c : comps) {
            parent.add(c);
            parent.add(Box.createVerticalStrut(6));
        }
        parent.add(Box.createVerticalStrut(6));
    }

    private JPanel createActionButtonsSection() {
        JPanel container = new JPanel(new GridLayout(2, 1, 0, 8));
        container.setOpaque(false);

        // Row 1: Fast Approval Decisions
        JPanel decisionRow = new JPanel(new GridLayout(1, 2, 8, 0));
        decisionRow.setOpaque(false);

        btnApprove = createColorButton("✅ Approve Medical", Color.decode("#10B981"), Color.decode("#059669"));
        btnApprove.addActionListener(e -> updateApprovalDecision("Approved"));

        btnReject = createColorButton("❌ Reject Medical", Color.decode("#EF4444"), Color.decode("#DC2626"));
        btnReject.addActionListener(e -> updateApprovalDecision("Rejected"));

        decisionRow.add(btnApprove);
        decisionRow.add(btnReject);

        // Row 2: Status Save, Clear and Refresh Buttons
        JPanel subRow = new JPanel(new GridLayout(1, 3, 8, 0));
        subRow.setOpaque(false);

        btnUpdate = createPrimaryButton("💾 Save Status");
        btnUpdate.addActionListener(e -> saveApprovalStatus());

        btnClear = createSecondaryButton("🧹 Clear");
        btnClear.addActionListener(e -> clearDetailsPanel());

        btnRefresh = createSecondaryButton("↺ Refresh");
        btnRefresh.addActionListener(e -> refreshAll());

        subRow.add(btnUpdate);
        subRow.add(btnClear);
        subRow.add(btnRefresh);

        container.add(decisionRow);
        container.add(subRow);
        return container;
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

    private JTextField createReadOnlyField() {
        JTextField tf = new JTextField();
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

    // =========================================================================
    // DATABASE OPERATIONS & DATA MANAGEMENT
    // =========================================================================

    private void loadInitialData() {
        loadStudentsList();
        loadSessionsList();
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

    private void loadStudentsList() {
        allStudents.clear();
        cmbStudentSelect.removeAllItems();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        String sql = "SELECT student_id, student_reg_no, full_name, email, phone, academic_year FROM students ORDER BY student_reg_no ASC";
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                StudentItem item = new StudentItem(
                        rs.getInt("student_id"), rs.getString("student_reg_no"),
                        rs.getString("full_name"), rs.getInt("academic_year"),
                        rs.getString("email"), rs.getString("phone"));
                allStudents.add(item);
                cmbStudentSelect.addItem(item);
            }
        } catch (SQLException e) {
            System.err.println("MedicalManagementForm: Error loading students: " + e.getMessage());
        }
    }

    private void loadSessionsList() {
        allSessions.clear();
        cmbSessionSelect.removeAllItems();
        SessionItem noneItem = new SessionItem(null, 0, "", null, "None (General Medical Absence)");
        allSessions.add(noneItem);
        cmbSessionSelect.addItem(noneItem);

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        String sql = "SELECT s.session_id, s.session_number, s.session_type, s.session_date, c.course_code " +
                "FROM attendance_sessions s JOIN courses c ON s.course_id = c.course_id ORDER BY s.session_date DESC, s.session_number ASC";
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                int sId = rs.getInt("session_id");
                int sNum = rs.getInt("session_number");
                String sType = rs.getString("session_type");
                Date sDate = rs.getDate("session_date");
                String label = String.format("Session #%d (%s) - %s (%s)", sNum, sType, rs.getString("course_code"), formatDate(sDate));
                SessionItem item = new SessionItem(sId, sNum, sType, sDate, label);
                allSessions.add(item);
                cmbSessionSelect.addItem(item);
            }
        } catch (SQLException e) {
            System.err.println("MedicalManagementForm: Error loading sessions: " + e.getMessage());
        }
    }

    public void loadMedicalRecords(String keyword, String status, String dateFrom, String dateTo) {
        tableModel.setRowCount(0);
        StringBuilder sql = new StringBuilder(
                "SELECT m.medical_id, m.student_id, s.student_reg_no, s.full_name, " +
                "m.start_date, m.end_date, m.reason, m.submitted_date, m.approval_status " +
                "FROM medicals m LEFT JOIN students s ON m.student_id = s.student_id WHERE 1=1 ");

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
                    String regNo = rs.getString("student_reg_no");
                    String name = rs.getString("full_name");
                    String reason = rs.getString("reason");
                    String appStatus = rs.getString("approval_status");
                    tableModel.addRow(new Object[]{
                            rowNum++, rs.getInt("medical_id"),
                            regNo != null ? regNo : "N/A", name != null ? name : "N/A",
                            formatDate(rs.getDate("start_date")), formatDate(rs.getDate("end_date")),
                            reason != null ? reason : "", formatDate(rs.getDate("submitted_date")),
                            appStatus != null ? appStatus : "Pending", "View Details"
                    });
                }
                lblTableInfo.setText("Displaying " + tableModel.getRowCount() + " records");
                if (tableModel.getRowCount() > 0 && currentMedicalId <= 0) {
                    tblMedicals.setRowSelectionInterval(0, 0);
                }
            }
        } catch (SQLException e) {
            lblTableInfo.setText("SQL Error: " + e.getMessage());
            System.err.println("MedicalManagementForm: Error loading medicals: " + e.getMessage());
        }
    }

    private void loadMedicalDetails(int medicalId) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        String sql = "SELECT m.medical_id, m.student_id, st.student_reg_no, st.full_name, st.academic_year, st.email, st.phone, " +
                "m.session_id, sess.session_number, sess.session_type, sess.session_date, " +
                "c.course_code, m.start_date, m.end_date, m.reason, m.document_path, m.submitted_date, m.approval_status, " +
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
                if (rs.next()) {
                    currentMedicalId = medicalId;
                    lblDetailsBadge.setText("Record ID: #" + medicalId);

                    String regNo = rs.getString("student_reg_no");
                    String fullName = rs.getString("full_name");
                    int acYear = rs.getInt("academic_year");
                    String email = rs.getString("email");
                    String phone = rs.getString("phone");

                    txtStudentRegNo.setText(regNo != null ? regNo : "N/A");
                    txtStudentName.setText(fullName != null ? fullName : "N/A");
                    txtStudentLevel.setText(acYear > 0 ? "Level " + acYear + " Undergraduate" : "Undergraduate");
                    txtStudentContact.setText((email != null ? email : "") + ((phone != null && !phone.isEmpty()) ? " • " + phone : ""));

                    txtStartDate.setText(formatDate(rs.getDate("start_date")));
                    txtEndDate.setText(formatDate(rs.getDate("end_date")));
                    txtSubmittedDate.setText(formatDate(rs.getDate("submitted_date")));
                    txtReason.setText(rs.getString("reason"));
                    txtDocumentPath.setText(rs.getString("document_path"));

                    int sessId = rs.getInt("session_id");
                    if (!rs.wasNull() && sessId > 0) {
                        for (int i = 0; i < cmbSessionSelect.getItemCount(); i++) {
                            SessionItem sItem = cmbSessionSelect.getItemAt(i);
                            if (sItem.getSessionId() != null && sItem.getSessionId() == sessId) {
                                cmbSessionSelect.setSelectedIndex(i);
                                break;
                            }
                        }
                        String cCode = rs.getString("course_code");
                        lblSessionDisplay.setText(String.format("Session #%d (%s) - %s [%s]",
                                rs.getInt("session_number"), rs.getString("session_type"),
                                cCode != null ? cCode : "", formatDate(rs.getDate("session_date"))));
                    } else {
                        cmbSessionSelect.setSelectedIndex(0);
                        lblSessionDisplay.setText("No specific session associated (General Medical Absence)");
                    }

                    String status = rs.getString("approval_status");
                    cmbApprovalStatus.setSelectedItem(status != null ? status : "Pending");

                    String reviewerName = rs.getString("verified_by_name");
                    int reviewerId = rs.getInt("verified_by");
                    if (reviewerName != null && !reviewerName.isEmpty()) {
                        lblVerifiedBy.setText("Verified by: " + reviewerName + " (ID #" + reviewerId + ")");
                    } else if (reviewerId > 0) {
                        lblVerifiedBy.setText("Verified by: Officer #" + reviewerId);
                    } else {
                        lblVerifiedBy.setText("Review Status: Unreviewed");
                    }

                    for (int r = 0; r < tableModel.getRowCount(); r++) {
                        Object val = tableModel.getValueAt(r, 1);
                        if (val instanceof Integer && ((Integer) val) == medicalId) {
                            if (tblMedicals.getSelectedRow() != r) {
                                tblMedicals.setRowSelectionInterval(r, r);
                            }
                            break;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("MedicalManagementForm: Error fetching record #" + medicalId + ": " + e.getMessage());
        }
    }

    private void updateStudentDisplayFields(StudentItem item) {
        txtStudentRegNo.setText(item != null ? item.getRegNo() : "");
        txtStudentName.setText(item != null ? item.getFullName() : "");
        txtStudentLevel.setText(item != null ? "Level " + item.getAcademicYear() + " Undergraduate" : "");
        txtStudentContact.setText(item != null ? item.getEmail() + " • " + (item.getPhone() != null ? item.getPhone() : "") : "");
    }

    private void bindMedicalParameters(PreparedStatement ps, int offset, int studentId, Integer sessionId,
                                       LocalDate startDate, LocalDate endDate, String reason, String docPath,
                                       LocalDate submittedDate, String status, int officerId) throws SQLException {
        ps.setInt(offset, studentId);
        if (sessionId != null) ps.setInt(offset + 1, sessionId);
        else ps.setNull(offset + 1, Types.INTEGER);
        ps.setDate(offset + 2, Date.valueOf(startDate));
        ps.setDate(offset + 3, Date.valueOf(endDate));
        ps.setString(offset + 4, reason);
        ps.setString(offset + 5, docPath);
        ps.setDate(offset + 6, Date.valueOf(submittedDate));
        ps.setString(offset + 7, status);

        if (("Approved".equalsIgnoreCase(status) || "Rejected".equalsIgnoreCase(status)) && officerId > 0) {
            ps.setInt(offset + 8, officerId);
        } else {
            ps.setNull(offset + 8, Types.INTEGER);
        }
    }

    /**
     * Fast action to update approval status directly to "Approved" or "Rejected".
     * Saves the decision to the database and logs the Technical Officer who verified it.
     */
    private void updateApprovalDecision(String newStatus) {
        if (currentMedicalId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a medical record from the table first.", "No Record Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("Rejected".equalsIgnoreCase(newStatus)) {
            int conf = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to mark Medical Record #" + currentMedicalId + " as REJECTED?",
                    "Confirm Rejection",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (conf != JOptionPane.YES_OPTION) return;
        }

        cmbApprovalStatus.setSelectedItem(newStatus);
        saveApprovalStatus();
    }

    /**
     * Saves or updates the Approval Status for the selected Medical Record.
     * Technical Officers can only verify and approve/reject; student medical submission details are preserved.
     */
    private void saveApprovalStatus() {
        if (currentMedicalId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a medical record from the table first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String status = (String) cmbApprovalStatus.getSelectedItem();
        if (status == null || status.trim().isEmpty()) {
            status = "Pending";
        }

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Could not establish database connection.", "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
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
                    ps.setInt(3, currentMedicalId);
                } else {
                    ps.setInt(2, currentMedicalId);
                }

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Medical Record #" + currentMedicalId + " status updated to '" + status + "' successfully!",
                            "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                    refreshViews(currentMedicalId, false);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to update record. No record found for ID #" + currentMedicalId,
                            "Update Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database SQL Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void refreshViews(int medId, boolean resetFilter) {
        loadSummaryCounts();
        if (resetFilter) {
            loadMedicalRecords(null, null, null, null);
        } else {
            loadMedicalRecords(txtSearch.getText().trim(), (String) cmbStatusFilter.getSelectedItem(),
                    txtDateFrom.getText().trim(), txtDateTo.getText().trim());
        }
        loadMedicalDetails(medId);
    }

    /**
     * Views the submitted Medical Certificate.
     * If the physical file exists, attempts to open it via the system PDF/image viewer.
     * If not found on disk or if opening fails, launches the in-app Medical Certificate Preview Dialog.
     */
    private void viewDocument() {
        if (currentMedicalId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a medical record from the table first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String docPath = txtDocumentPath.getText().trim();
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

        showMedicalCertificatePreviewDialog(docPath, targetFile);
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
    private void showMedicalCertificatePreviewDialog(String docPath, File targetFile) {
        JDialog dlg = new JDialog(this, "Medical Certificate Document Preview - Record #" + currentMedicalId, true);
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

        JLabel badgeRec = new JLabel("Record #" + currentMedicalId, SwingConstants.CENTER);
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

        infoGrid.add(createPreviewField("Student Reg. Number:", txtStudentRegNo.getText()));
        infoGrid.add(createPreviewField("Student Full Name:", txtStudentName.getText()));
        infoGrid.add(createPreviewField("Academic Level:", txtStudentLevel.getText()));
        infoGrid.add(createPreviewField("Submitted Date:", txtSubmittedDate.getText()));
        infoGrid.add(createPreviewField("Absence Start Date:", txtStartDate.getText()));
        infoGrid.add(createPreviewField("Absence End Date:", txtEndDate.getText()));
        infoGrid.add(createPreviewField("Document Reference:", (docPath != null && !docPath.isEmpty()) ? docPath : "None attached"));
        infoGrid.add(createPreviewField("Current Status:", (String) cmbApprovalStatus.getSelectedItem()));

        paper.add(infoGrid);
        paper.add(Box.createVerticalStrut(14));

        // Session
        JPanel sessPanel = new JPanel(new BorderLayout(0, 4));
        sessPanel.setOpaque(false);
        JLabel sessLbl = new JLabel("Linked Academic Session:");
        sessLbl.setFont(FONT_SMALL);
        sessLbl.setForeground(TEXT_SECONDARY);
        JLabel sessVal = new JLabel(lblSessionDisplay.getText());
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
        JTextArea rArea = new JTextArea(txtReason.getText());
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
        JLabel vText = new JLabel(lblVerifiedBy.getText());
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
            dlg.dispose();
            updateApprovalDecision("Approved");
        });

        JButton dlgReject = createColorButton("❌ Reject", Color.decode("#EF4444"), Color.decode("#DC2626"));
        dlgReject.addActionListener(e -> {
            dlg.dispose();
            updateApprovalDecision("Rejected");
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

    private void clearDetailsPanel() {
        currentMedicalId = -1;
        lblDetailsBadge.setText("Mode: Review & Verification");
        if (cmbStudentSelect.getItemCount() > 0) cmbStudentSelect.setSelectedIndex(0);
        txtStudentRegNo.setText("");
        txtStudentName.setText("");
        txtStudentLevel.setText("");
        txtStudentContact.setText("");
        txtStartDate.setText("");
        txtEndDate.setText("");
        txtSubmittedDate.setText("");
        txtReason.setText("");
        txtDocumentPath.setText("");
        if (cmbSessionSelect.getItemCount() > 0) cmbSessionSelect.setSelectedIndex(0);
        lblSessionDisplay.setText("No specific session associated (General Medical Absence)");
        cmbApprovalStatus.setSelectedItem("Pending");
        lblVerifiedBy.setText("Review Status: Unreviewed");
        tblMedicals.clearSelection();
    }

    private void refreshAll() {
        resetFilters();
        loadSummaryCounts();
        clearDetailsPanel();
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

    // =========================================================================
    // HELPER DATA OBJECTS
    // =========================================================================

    public static class StudentItem {
        private final int studentId;
        private final String regNo, fullName, email, phone;
        private final int academicYear;

        public StudentItem(int id, String reg, String name, int year, String email, String phone) {
            this.studentId = id; this.regNo = reg; this.fullName = name;
            this.academicYear = year; this.email = email; this.phone = phone;
        }

        public int getStudentId() { return studentId; }
        public String getRegNo() { return regNo; }
        public String getFullName() { return fullName; }
        public int getAcademicYear() { return academicYear; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }

        @Override public String toString() { return regNo + " - " + fullName; }
    }

    public static class SessionItem {
        private final Integer sessionId;
        private final int sessionNumber;
        private final String sessionType;
        private final Date sessionDate;
        private final String displayText;

        public SessionItem(Integer id, int number, String type, Date date, String display) {
            this.sessionId = id; this.sessionNumber = number; this.sessionType = type;
            this.sessionDate = date; this.displayText = display;
        }

        public Integer getSessionId() { return sessionId; }
        public int getSessionNumber() { return sessionNumber; }
        public String getSessionType() { return sessionType; }
        public Date getSessionDate() { return sessionDate; }
        public String getDetailsDescription() { return displayText; }

        @Override public String toString() { return displayText; }
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

