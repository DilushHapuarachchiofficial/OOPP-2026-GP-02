package main.java.ui.TechnicalOfficer;

import main.java.dao.TechnicalOfficer.TechnicalOfficerDAO;
import main.java.model.Notice;
import main.java.model.TechnicalOfficer.TechnicalOfficer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static main.java.ui.TechnicalOfficer.TechnicalOfficerDashboard.*;

/**
 * FacultyNoticesForm - Technical Officer Notice Viewer for TecFAMS.
 * Faculty of Technology - University of Ruhuna.
 *
 * NOTE: Technical Officers have VIEW-ONLY access to notices.
 * Notices are published and managed by Administration.
 * This page displays notices and circulars received by the Technical Officer.
 * Built with simple, clean Java Swing for easy understanding in OOP projects.
 */
public class FacultyNoticesForm extends JFrame {

    private final JFrame parentDashboard;
    private final TechnicalOfficer currentOfficer;
    private final TechnicalOfficerDAO dao;

    // Filter controls
    private JComboBox<String> cmbNoticeFilter;
    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnReset;
    private JButton btnRefresh;
    private JButton btnBack;

    // Table controls
    private JTable tblNotices;
    private DefaultTableModel tableModel;
    private JLabel lblTableStatus;

    // Metric Summary Labels
    private JLabel lblTotalCount;
    private JLabel lblOfficerCount;
    private JLabel lblLatestDate;

    // Reading Pane Components (View-Only)
    private JLabel lblNoticeTitle;
    private JLabel lblNoticeDate;
    private JLabel lblNoticePublisher;
    private JLabel lblNoticeAudience;
    private JTextArea txtNoticeBody;

    // Active loaded notices list
    private List<Notice> noticesList;

    public FacultyNoticesForm() {
        this(null, new TechnicalOfficer());
    }

    public FacultyNoticesForm(JFrame parentDashboard) {
        this(parentDashboard, new TechnicalOfficer());
    }

    public FacultyNoticesForm(JFrame parentDashboard, TechnicalOfficer officer) {
        this.parentDashboard = parentDashboard;
        this.currentOfficer = officer != null ? officer : new TechnicalOfficer();
        this.dao = new TechnicalOfficerDAO();

        initFrameSettings();
        buildUI();
        loadNotices();
    }

    /**
     * Initializes basic window settings.
     */
    private void initFrameSettings() {
        setTitle("TecFAMS - Faculty Notices (View Only)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(980, 620));
        setLocationRelativeTo(parentDashboard);

        // Return focus to dashboard when window is closed
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (parentDashboard != null) {
                    parentDashboard.setVisible(true);
                    parentDashboard.toFront();
                }
            }
        });
    }

    /**
     * Builds the main UI layout using standard BorderLayout and FlowLayout.
     */
    private void buildUI() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);

        // 1. Top Header Bar
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Content Area
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setBackground(BG_COLOR);
        centerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top section: Stats + Search/Filter
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);
        topSection.add(createSummaryStatsRow());
        topSection.add(Box.createVerticalStrut(12));
        topSection.add(createFilterPanel());

        centerPanel.add(topSection, BorderLayout.NORTH);
        centerPanel.add(createNoticeViewerSplit(), BorderLayout.CENTER);
        centerPanel.add(createBottomBar(), BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Top header showing branding, technical officer details, and back button.
     */
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(12, 24, 12, 24)
        ));

        // Left Branding & Subtitle
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        JLabel titleLabel = new JLabel("TecFAMS  •  Faculty Notices & Circulars");
        titleLabel.setFont(FONT_HEADER_LARGE);
        titleLabel.setForeground(PRIMARY_NAVY);

        JLabel subLabel = new JLabel("University of Ruhuna • Faculty of Technology | Received Notices (View-Only Access)");
        subLabel.setFont(FONT_SMALL);
        subLabel.setForeground(TEXT_SECONDARY);
        left.add(titleLabel);
        left.add(subLabel);

        // Right Officer Info & Back Button
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        JLabel officerBadge = new JLabel("Officer: " + currentOfficer.getName());
        officerBadge.setFont(FONT_SMALL);
        officerBadge.setForeground(PRIMARY_NAVY);
        officerBadge.setBackground(LIGHT_BLUE);
        officerBadge.setOpaque(true);
        officerBadge.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#BFDBFE"), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));

        btnBack = createSecondaryButton("← Back to Dashboard");
        btnBack.addActionListener(e -> backToDashboard());

        right.add(officerBadge);
        right.add(btnBack);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    /**
     * Three summary cards showing total notices received by TO.
     */
    private JPanel createSummaryStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
        row.setOpaque(false);

        lblTotalCount = new JLabel("0");
        lblOfficerCount = new JLabel("0");
        lblLatestDate = new JLabel("-");

        row.add(createStatCard("Notices For Technical Officer", lblTotalCount, "Received circulars", "📢", PRIMARY_NAVY));
        row.add(createStatCard("Specific to Tech Officers", lblOfficerCount, "Special duties & circulars", "🛡️", SUCCESS_GREEN));
        row.add(createStatCard("Latest Received Date", lblLatestDate, "Most recent publication", "📅", MAIN_BLUE));

        return row;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, String subtitle, String icon, Color accent) {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_SMALL);
        titleLbl.setForeground(TEXT_SECONDARY);

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        top.add(titleLbl, BorderLayout.WEST);
        top.add(iconLbl, BorderLayout.EAST);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(TEXT_MAIN);
        valueLabel.setBorder(new EmptyBorder(6, 0, 2, 0));

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(FONT_SMALL);
        subLbl.setForeground(accent);

        card.add(top, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Filter card allows the officer to filter received notices and search keywords.
     */
    private JPanel createFilterPanel() {
        JPanel card = createCardPanel();
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 6));

        JLabel filterLabel = new JLabel("Show Notices:");
        filterLabel.setFont(FONT_MEDIUM);
        filterLabel.setForeground(PRIMARY_NAVY);
        card.add(filterLabel);

        // Filter options tailored specifically for Technical Officer
        cmbNoticeFilter = new JComboBox<>(new String[]{
                "Notices for Technical Officer (Default)",
                "Technical Officer Specific Only",
                "General Faculty Announcements",
                "All Published Circulars"
        });
        cmbNoticeFilter.setFont(FONT_REGULAR);
        cmbNoticeFilter.setPreferredSize(new Dimension(270, 32));
        cmbNoticeFilter.addActionListener(e -> loadNotices());
        card.add(cmbNoticeFilter);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(FONT_MEDIUM);
        searchLabel.setForeground(TEXT_MAIN);
        card.add(searchLabel);

        txtSearch = new JTextField();
        txtSearch.setFont(FONT_REGULAR);
        txtSearch.setPreferredSize(new Dimension(200, 32));
        card.add(txtSearch);

        btnSearch = createPrimaryButton("Search");
        btnSearch.setPreferredSize(new Dimension(85, 32));
        btnSearch.addActionListener(e -> loadNotices());
        card.add(btnSearch);

        btnReset = createSecondaryButton("Reset");
        btnReset.setPreferredSize(new Dimension(75, 32));
        btnReset.addActionListener(e -> {
            cmbNoticeFilter.setSelectedIndex(0);
            txtSearch.setText("");
            loadNotices();
        });
        card.add(btnReset);

        btnRefresh = createSecondaryButton("🔄 Refresh");
        btnRefresh.setPreferredSize(new Dimension(100, 32));
        btnRefresh.addActionListener(e -> loadNotices());
        card.add(btnRefresh);

        return card;
    }

    /**
     * Split view: Left = Table of notices, Right = Reading pane with full notice content.
     */
    private JPanel createNoticeViewerSplit() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.52);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        splitPane.setBackground(BG_COLOR);

        splitPane.setLeftComponent(createTableCard());
        splitPane.setRightComponent(createReadingPane());

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Left side table listing notices.
     */
    private JPanel createTableCard() {
        JPanel tableCard = createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 8));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);

        JLabel tableTitle = new JLabel("Received Notices List");
        tableTitle.setFont(FONT_HEADER_MEDIUM);
        tableTitle.setForeground(PRIMARY_NAVY);

        lblTableStatus = new JLabel("Loading notices...");
        lblTableStatus.setFont(FONT_SMALL);
        lblTableStatus.setForeground(TEXT_SECONDARY);

        tableHeader.add(tableTitle, BorderLayout.WEST);
        tableHeader.add(lblTableStatus, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        String[] columns = {"#", "Notice Title", "Audience", "Date Published", "Published By"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // View-only, cells cannot be edited
            }
        };

        tblNotices = new JTable(tableModel);
        tblNotices.setFont(FONT_REGULAR);
        tblNotices.setRowHeight(36);
        tblNotices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblNotices.setSelectionBackground(LIGHT_BLUE);
        tblNotices.setSelectionForeground(TEXT_MAIN);
        tblNotices.setGridColor(BORDER_COLOR);
        tblNotices.setShowHorizontalLines(true);
        tblNotices.setShowVerticalLines(true);

        int[] widths = {36, 240, 110, 100, 120};
        for (int i = 0; i < widths.length && i < tblNotices.getColumnCount(); i++) {
            tblNotices.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JTableHeader th = tblNotices.getTableHeader();
        th.setFont(FONT_MEDIUM);
        th.setBackground(Color.decode("#F1F5F9"));
        th.setForeground(PRIMARY_NAVY);
        th.setPreferredSize(new Dimension(0, 36));

        // Center alignments
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        tblNotices.getColumnModel().getColumn(0).setCellRenderer(center);
        tblNotices.getColumnModel().getColumn(3).setCellRenderer(center);

        // Audience badge renderer
        tblNotices.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, val, isSelected, hasFocus, row, col);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(FONT_MEDIUM);
                String aud = val != null ? val.toString() : "All";
                if ("Technical_Officers".equalsIgnoreCase(aud)) {
                    label.setForeground(Color.decode("#047857"));
                    label.setText("🛡️ Tech Officers");
                } else if ("All".equalsIgnoreCase(aud)) {
                    label.setForeground(Color.decode("#1D4ED8"));
                    label.setText("🌐 All Faculty");
                } else if ("Students".equalsIgnoreCase(aud)) {
                    label.setForeground(Color.decode("#B45309"));
                    label.setText("🎓 Students");
                } else if ("Lecturers".equalsIgnoreCase(aud)) {
                    label.setForeground(Color.decode("#6D28D9"));
                    label.setText("👨‍🏫 Lecturers");
                } else {
                    label.setText(aud);
                }
                return label;
            }
        });

        // When user selects a row, load it into the reading pane
        tblNotices.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblNotices.getSelectedRow();
                if (row >= 0) {
                    displaySelectedNotice(row);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblNotices);
        scroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        scroll.getViewport().setBackground(WHITE);
        tableCard.add(scroll, BorderLayout.CENTER);

        return tableCard;
    }

    /**
     * Right side view-only reading pane.
     */
    private JPanel createReadingPane() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        // Header of reading pane
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        JLabel sectionTitle = new JLabel("Notice Reader (View-Only)");
        sectionTitle.setFont(FONT_HEADER_SMALL);
        sectionTitle.setForeground(TEXT_SECONDARY);

        lblNoticeTitle = new JLabel("Select a notice to read");
        lblNoticeTitle.setFont(FONT_HEADER_MEDIUM);
        lblNoticeTitle.setForeground(PRIMARY_NAVY);
        lblNoticeTitle.setBorder(new EmptyBorder(4, 0, 8, 0));

        // Meta tags strip
        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        meta.setOpaque(false);

        lblNoticeDate = createBadge("📅 -", Color.decode("#64748B"), Color.decode("#F1F5F9"));
        lblNoticePublisher = createBadge("👤 By: -", Color.decode("#1E293B"), Color.decode("#E2E8F0"));
        lblNoticeAudience = createBadge("Audience: -", Color.decode("#1D4ED8"), LIGHT_BLUE);

        meta.add(lblNoticeDate);
        meta.add(lblNoticePublisher);
        meta.add(lblNoticeAudience);

        top.add(sectionTitle);
        top.add(lblNoticeTitle);
        top.add(meta);
        top.add(Box.createVerticalStrut(8));
        top.add(new JSeparator(SwingConstants.HORIZONTAL));

        card.add(top, BorderLayout.NORTH);

        // Body text area: read-only, line wrap
        txtNoticeBody = new JTextArea();
        txtNoticeBody.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNoticeBody.setForeground(TEXT_MAIN);
        txtNoticeBody.setLineWrap(true);
        txtNoticeBody.setWrapStyleWord(true);
        txtNoticeBody.setEditable(false);
        txtNoticeBody.setBackground(Color.decode("#F8FAFC"));
        txtNoticeBody.setBorder(new EmptyBorder(16, 16, 16, 16));

        JScrollPane scroll = new JScrollPane(txtNoticeBody);
        scroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        scroll.getViewport().setBackground(Color.decode("#F8FAFC"));
        card.add(scroll, BorderLayout.CENTER);

        JLabel note = new JLabel("🔒 Official circular issued by University Administration. Technical Officers have view-only access.");
        note.setFont(FONT_SMALL);
        note.setForeground(TEXT_SECONDARY);
        card.add(note, BorderLayout.SOUTH);

        return card;
    }

    private JLabel createBadge(String text, Color fg, Color bg) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SMALL);
        l.setForeground(fg);
        l.setBackground(bg);
        l.setOpaque(true);
        l.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#CBD5E1"), 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
        return l;
    }

    /**
     * Loads notices from database based on user's selected filter and search keyword.
     */
    private void loadNotices() {
        int filterIndex = cmbNoticeFilter.getSelectedIndex();
        String search = txtSearch.getText().trim();

        String audienceParam;
        if (filterIndex == 1) {
            audienceParam = "TO_ONLY";
        } else if (filterIndex == 2) {
            audienceParam = "GENERAL";
        } else if (filterIndex == 3) {
            audienceParam = "ALL_CIRCULARS";
        } else {
            audienceParam = "TO_DEFAULT"; // Default: relevant to Technical Officer (TO + All)
        }

        tableModel.setRowCount(0);
        noticesList = dao.getTechnicalOfficerNotices(audienceParam, search);

        int officerSpecific = 0;
        String latest = "-";

        int count = 1;
        for (Notice n : noticesList) {
            String dateStr = n.getPublishedDate() != null ? n.getPublishedDate().toString() : "-";

            tableModel.addRow(new Object[]{
                    count++,
                    n.getTitle(),
                    n.getTargetAudience(),
                    dateStr,
                    n.getPublishedBy()
            });

            if ("Technical_Officers".equalsIgnoreCase(n.getTargetAudience())) {
                officerSpecific++;
            }
            if (latest.equals("-") && n.getPublishedDate() != null) {
                latest = n.getPublishedDate().toString();
            }
        }

        lblTotalCount.setText(String.valueOf(noticesList.size()));
        lblOfficerCount.setText(String.valueOf(officerSpecific));
        lblLatestDate.setText(latest);
        lblTableStatus.setText(String.format("Showing %d notice(s)", noticesList.size()));

        if (noticesList.isEmpty()) {
            lblNoticeTitle.setText("No notices found.");
            lblNoticeDate.setText("📅 -");
            lblNoticePublisher.setText("👤 By: -");
            lblNoticeAudience.setText("Audience: -");
            txtNoticeBody.setText("There are no circulars matching your current filter criteria.");
        } else {
            tblNotices.setRowSelectionInterval(0, 0);
            displaySelectedNotice(0);
        }
    }

    /**
     * Displays the selected notice's details in the right-side reading pane.
     */
    private void displaySelectedNotice(int row) {
        if (noticesList == null || row < 0 || row >= noticesList.size()) return;
        Notice n = noticesList.get(row);

        lblNoticeTitle.setText(n.getTitle());

        String dateStr = n.getPublishedDate() != null
                ? n.getPublishedDate().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
                : "Unknown";
        lblNoticeDate.setText("📅 " + dateStr);

        String publisher = n.getPublishedBy() != null ? n.getPublishedBy() : "Administration";
        lblNoticePublisher.setText("👤 Published By: " + publisher);

        String aud = n.getTargetAudience() != null ? n.getTargetAudience() : "All";
        lblNoticeAudience.setText("Audience: " + aud.replace("_", " "));

        txtNoticeBody.setText(n.getContent() != null ? n.getContent() : "No notice text available.");
        txtNoticeBody.setCaretPosition(0);
    }

    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);

        JLabel info = new JLabel("💡 Notices are published by Administration. Technical Officers have view-only access.");
        info.setFont(FONT_SMALL);
        info.setForeground(TEXT_SECONDARY);

        JButton backBtn = createPrimaryButton("← Back to Dashboard");
        backBtn.addActionListener(e -> backToDashboard());

        bar.add(info, BorderLayout.WEST);
        bar.add(backBtn, BorderLayout.EAST);
        return bar;
    }

    private void backToDashboard() {
        dispose();
        if (parentDashboard != null) {
            parentDashboard.setVisible(true);
            parentDashboard.toFront();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            FacultyNoticesForm form = new FacultyNoticesForm();
            form.setVisible(true);
        });
    }
}
