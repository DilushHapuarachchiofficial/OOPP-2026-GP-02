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
 * - Medical Details panel (student info, absence dates, session info, certificate Browse/View, approval status)
 * - Actions: Save New, Update (persists Approval Status), Clear, Refresh
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
    private JButton btnBrowseDoc, btnViewDoc, btnSaveNew, btnUpdate, btnClear, btnRefresh;

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

        lblDetailsBadge = new JLabel("Mode: New Record");
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
        cmbStudentSelect.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateStudentDisplayFields((StudentItem) cmbStudentSelect.getSelectedItem());
            }
        });

        JPanel studentGrid = new JPanel(new GridLayout(2, 2, 10, 8));
        studentGrid.setOpaque(false);
        txtStudentRegNo = createReadOnlyField();
        txtStudentName = createReadOnlyField();
        txtStudentLevel = createReadOnlyField();
        txtStudentContact = createReadOnlyField();
        studentGrid.add(createFieldWrapper("Registration No:", txtStudentRegNo));
        studentGrid.add(createFieldWrapper("Full Name:", txtStudentName));
        studentGrid.add(createFieldWrapper("Academic Level:", txtStudentLevel));
        studentGrid.add(createFieldWrapper("Official Contact / Email:", txtStudentContact));
        addSection(formBody, "1. Student Information", createFieldWrapper("Select Student:", cmbStudentSelect), studentGrid);

        // 2. Absence Period & Reason
        JPanel datesGrid = new JPanel(new GridLayout(1, 3, 10, 0));
        datesGrid.setOpaque(false);
        String today = LocalDate.now().toString();
        txtStartDate = new JTextField(today);
        txtStartDate.setFont(FONT_REGULAR);
        txtEndDate = new JTextField(today);
        txtEndDate.setFont(FONT_REGULAR);
        txtSubmittedDate = new JTextField(today);
        txtSubmittedDate.setFont(FONT_REGULAR);
        datesGrid.add(createFieldWrapper("Start Date (YYYY-MM-DD):", txtStartDate));
        datesGrid.add(createFieldWrapper("End Date (YYYY-MM-DD):", txtEndDate));
        datesGrid.add(createFieldWrapper("Submitted Date:", txtSubmittedDate));

        txtReason = new JTextArea(3, 20);
        txtReason.setFont(FONT_REGULAR);
        txtReason.setLineWrap(true);
        txtReason.setWrapStyleWord(true);
        JScrollPane reasonScroll = new JScrollPane(txtReason);
        reasonScroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        addSection(formBody, "2. Absence Period & Reason", datesGrid, createFieldWrapper("Reason / Medical Condition:", reasonScroll));

        // 3. Associated Academic Session
        cmbSessionSelect = new JComboBox<>();
        cmbSessionSelect.setFont(FONT_REGULAR);
        cmbSessionSelect.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                SessionItem sItem = (SessionItem) cmbSessionSelect.getSelectedItem();
                lblSessionDisplay.setText(sItem != null ? sItem.getDetailsDescription() : "None");
            }
        });
        lblSessionDisplay = new JLabel("No specific session associated (General Medical Absence)");
        lblSessionDisplay.setFont(FONT_SMALL);
        lblSessionDisplay.setForeground(TEXT_SECONDARY);
        addSection(formBody, "3. Associated Academic Session (Optional)", createFieldWrapper("Link to Scheduled Session:", cmbSessionSelect), lblSessionDisplay);

        // 4. Medical Certificate Document
        JPanel docRow = new JPanel(new BorderLayout(8, 0));
        docRow.setOpaque(false);
        txtDocumentPath = new JTextField("medicals/certificate_sample.pdf");
        txtDocumentPath.setFont(FONT_REGULAR);

        JPanel docBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        docBtnRow.setOpaque(false);
        btnBrowseDoc = createPrimaryButton("📁 Browse...");
        btnBrowseDoc.addActionListener(e -> browseDocument());
        btnViewDoc = createPrimaryButton("👁 View Document");
        btnViewDoc.addActionListener(e -> viewDocument());
        docBtnRow.add(btnBrowseDoc);
        docBtnRow.add(btnViewDoc);

        docRow.add(txtDocumentPath, BorderLayout.CENTER);
        docRow.add(docBtnRow, BorderLayout.EAST);
        addSection(formBody, "4. Medical Certificate Document", createFieldWrapper("Certificate File Path (PDF / Image):", docRow));

        // 5. Verification & Approval Status
        JPanel statusGrid = new JPanel(new GridLayout(1, 2, 10, 0));
        statusGrid.setOpaque(false);
        cmbApprovalStatus = new JComboBox<>(new String[]{"Pending", "Approved", "Rejected"});
        cmbApprovalStatus.setFont(FONT_MEDIUM);
        lblVerifiedBy = new JLabel("Review Status: Unreviewed");
        lblVerifiedBy.setFont(FONT_SMALL);
        lblVerifiedBy.setForeground(TEXT_SECONDARY);
        statusGrid.add(createFieldWrapper("Approval Status:", cmbApprovalStatus));
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
        JPanel mgmtRow = new JPanel(new GridLayout(1, 4, 8, 0));
        mgmtRow.setOpaque(false);

        btnSaveNew = createPrimaryButton("💾 Save New");
        btnSaveNew.addActionListener(e -> saveMedicalRecord(false));
        btnUpdate = createPrimaryButton("🔄 Update");
        btnUpdate.addActionListener(e -> saveMedicalRecord(true));
        btnClear = createSecondaryButton("🧹 Clear");
        btnClear.addActionListener(e -> clearDetailsPanel());
        btnRefresh = createSecondaryButton("↺ Refresh");
        btnRefresh.addActionListener(e -> refreshAll());

        mgmtRow.add(btnSaveNew);
        mgmtRow.add(btnUpdate);
        mgmtRow.add(btnClear);
        mgmtRow.add(btnRefresh);
        return mgmtRow;
    }

    private JTextField createReadOnlyField() {
        JTextField tf = new JTextField();
        tf.setEditable(false);
        tf.setFont(FONT_REGULAR);
        tf.setBackground(Color.decode("#F8FAFC"));
        tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_COLOR, 1), new EmptyBorder(4, 6, 4, 6)));
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
            }
        } catch (SQLException e) {
            lblTableInfo.setText("SQL Error: " + e.getMessage());
            System.err.println("MedicalManagementForm: Error loading medicals: " + e.getMessage());
        }
    }

    private void loadMedicalDetails(int medicalId) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        String sql = "SELECT m.medical_id, m.student_id, m.session_id, sess.session_number, sess.session_type, sess.session_date, " +
                "c.course_code, m.start_date, m.end_date, m.reason, m.document_path, m.submitted_date, m.approval_status, " +
                "m.verified_by, to_off.officer_name as verified_by_name " +
                "FROM medicals m " +
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

                    int studentId = rs.getInt("student_id");
                    for (int i = 0; i < cmbStudentSelect.getItemCount(); i++) {
                        StudentItem item = cmbStudentSelect.getItemAt(i);
                        if (item.getStudentId() == studentId) {
                            cmbStudentSelect.setSelectedIndex(i);
                            updateStudentDisplayFields(item);
                            break;
                        }
                    }

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

    private boolean showValidationWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
        return false;
    }

    /**
     * Save (New) or Update an existing Medical Record.
     * When updating, the selected Approval Status from cmbApprovalStatus is persisted to the database.
     */
    private void saveMedicalRecord(boolean isUpdate) {
        StudentItem student = (StudentItem) cmbStudentSelect.getSelectedItem();
        if (student == null) {
            showValidationWarning("Please select an undergraduate student.");
            return;
        }

        String startStr = txtStartDate.getText().trim();
        String endStr = txtEndDate.getText().trim();
        String submittedStr = txtSubmittedDate.getText().trim();
        String reason = txtReason.getText().trim();
        String docPath = txtDocumentPath.getText().trim();
        String status = (String) cmbApprovalStatus.getSelectedItem();

        if (startStr.isEmpty() || endStr.isEmpty()) {
            showValidationWarning("Medical start date and end date are required.");
            return;
        }

        LocalDate startDate, endDate, submittedDate;
        try {
            startDate = LocalDate.parse(startStr);
            endDate = LocalDate.parse(endStr);
            submittedDate = submittedStr.isEmpty() ? LocalDate.now() : LocalDate.parse(submittedStr);
        } catch (DateTimeParseException e) {
            showValidationWarning("Invalid date format. Please use YYYY-MM-DD (e.g. 2024-04-15).");
            return;
        }

        if (endDate.isBefore(startDate)) {
            showValidationWarning("Medical End Date cannot be earlier than Start Date.");
            return;
        }

        if (reason.isEmpty()) {
            showValidationWarning("Please enter the medical reason or clinical condition.");
            return;
        }

        if (isUpdate && "Rejected".equalsIgnoreCase(status)) {
            int conf = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to mark Medical Record #" + currentMedicalId + " as REJECTED?",
                    "Confirm Rejection",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (conf != JOptionPane.YES_OPTION) return;
        }

        if (docPath.isEmpty()) docPath = "medicals/default_certificate.pdf";

        SessionItem sessionItem = (SessionItem) cmbSessionSelect.getSelectedItem();
        Integer sessionId = (sessionItem != null && sessionItem.getSessionId() != null && sessionItem.getSessionId() > 0)
                ? sessionItem.getSessionId() : null;

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Could not establish database connection.", "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int officerId = resolveTechnicalOfficerId(conn);
            if (("Approved".equalsIgnoreCase(status) || "Rejected".equalsIgnoreCase(status)) && officerId <= 0) {
                JOptionPane.showMessageDialog(this, "Could not identify Technical Officer account in database for verification.", "Authentication Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String sql;
            if (isUpdate) {
                if (currentMedicalId <= 0) {
                    JOptionPane.showMessageDialog(this, "No existing medical record selected to update. Select a record from the table first.", "Notice", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                sql = "UPDATE medicals SET student_id = ?, session_id = ?, start_date = ?, end_date = ?, " +
                      "reason = ?, document_path = ?, submitted_date = ?, approval_status = ?, verified_by = ? WHERE medical_id = ?";
            } else {
                sql = "INSERT INTO medicals (medical_id, student_id, session_id, start_date, end_date, " +
                      "reason, document_path, submitted_date, approval_status, verified_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int targetId = isUpdate ? currentMedicalId : getNextMedicalId(conn);
                if (isUpdate) {
                    bindMedicalParameters(ps, 1, student.getStudentId(), sessionId, startDate, endDate, reason, docPath, submittedDate, status, officerId);
                    ps.setInt(10, targetId);
                } else {
                    ps.setInt(1, targetId);
                    bindMedicalParameters(ps, 2, student.getStudentId(), sessionId, startDate, endDate, reason, docPath, submittedDate, status, officerId);
                }

                if (ps.executeUpdate() > 0) {
                    String msg = isUpdate ? "Medical Record #" + targetId + " updated successfully!" : "Medical Record #" + targetId + " successfully registered!";
                    JOptionPane.showMessageDialog(this, msg, isUpdate ? "Update Successful" : "Record Saved", JOptionPane.INFORMATION_MESSAGE);
                    currentMedicalId = targetId;
                    refreshViews(targetId, !isUpdate);
                } else {
                    JOptionPane.showMessageDialog(this, isUpdate ? "Failed to update record. No matching record found." : "Could not save medical record.", isUpdate ? "Update Failed" : "Save Failed", JOptionPane.ERROR_MESSAGE);
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

    private void viewDocument() {
        String docPath = txtDocumentPath.getText().trim();
        if (docPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No certificate document path specified for this record.", "Document Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
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
            showDocumentInfoDialog(targetFile);
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Certificate document was not found on local disk at:\n" + docPath +
                    "\n\nYou may use the '📁 Browse...' button to attach a valid local PDF or image.",
                    "Document File Not Found",
                    JOptionPane.WARNING_MESSAGE);
        }
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

    private void showDocumentInfoDialog(File file) {
        JDialog dlg = new JDialog(this, "Document Attached - " + file.getName(), true);
        dlg.setSize(440, 240);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel p = new JPanel(new GridLayout(4, 1, 6, 6));
        p.setBorder(new EmptyBorder(20, 24, 20, 24));
        p.setBackground(WHITE);
        p.add(new JLabel("Document File: " + file.getName()));
        p.add(new JLabel("Disk Location: " + file.getAbsolutePath()));
        p.add(new JLabel("File Size: " + (file.length() / 1024) + " KB"));
        p.add(new JLabel("File Status: Document available locally."));

        JButton btnClose = createPrimaryButton("Close");
        btnClose.addActionListener(e -> dlg.dispose());
        JPanel b = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        b.add(btnClose);

        dlg.add(p, BorderLayout.CENTER);
        dlg.add(b, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void browseDocument() {
        JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir"), "medicals"));
        chooser.setDialogTitle("Select Medical Certificate (PDF, PNG, JPG)");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            String userDir = System.getProperty("user.dir");
            String abs = selected.getAbsolutePath();

            if (abs.startsWith(userDir)) {
                String rel = abs.substring(userDir.length());
                if (rel.startsWith(File.separator)) rel = rel.substring(1);
                txtDocumentPath.setText(rel.replace('\\', '/'));
            } else {
                txtDocumentPath.setText(abs);
            }
        }
    }

    private int getNextMedicalId(Connection conn) {
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COALESCE(MAX(medical_id), 0) + 1 FROM medicals")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ignored) {
        }
        return 1;
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
        lblDetailsBadge.setText("Mode: New Record");
        if (cmbStudentSelect.getItemCount() > 0) cmbStudentSelect.setSelectedIndex(0);
        String today = LocalDate.now().toString();
        txtStartDate.setText(today);
        txtEndDate.setText(today);
        txtSubmittedDate.setText(today);
        txtReason.setText("");
        txtDocumentPath.setText("medicals/sample_certificate.pdf");
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
        dispose();
        if (parentDashboard != null) {
            parentDashboard.setVisible(true);
            parentDashboard.toFront();
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

