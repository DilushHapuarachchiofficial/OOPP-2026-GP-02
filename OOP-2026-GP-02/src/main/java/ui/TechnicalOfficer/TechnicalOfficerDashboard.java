package main.java.ui.TechnicalOfficer;

import main.java.dao.TechnicalOfficer.TechnicalOfficerDAO;
import main.java.model.TechnicalOfficer.TechnicalOfficer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Main Technical Officer Dashboard for TecFAMS (Faculty Management System).
 * Designed for University of Ruhuna - Faculty of Technology.
 * 
 * Features:
 * - Professional blue-and-white theme (#123B70, #2563EB, #EAF2FF, #FFFFFF, #F5F8FD)
 * - Header with TecFAMS branding, role subtitle, officer welcome message, and avatar
 * - Clean sidebar navigation (Dashboard, Attendance, Medicals, Timetable, Notices, Profile, Logout)
 * - Four main interactive dashboard cards with icons, descriptions, and action buttons
 * - Key metric summary statistics strip
 * - Integrated Profile update dialog adhering to SRS FR-04 (locked username)
 * - Logout confirmation dialog
 */
public class TechnicalOfficerDashboard extends JFrame {

    // TecFAMS Specified Color Palette
    public static final Color PRIMARY_NAVY   = Color.decode("#123B70");
    public static final Color MAIN_BLUE      = Color.decode("#2563EB");
    public static final Color LIGHT_BLUE     = Color.decode("#EAF2FF");
    public static final Color WHITE          = Color.decode("#FFFFFF");
    public static final Color BG_COLOR       = Color.decode("#F5F8FD");
    public static final Color TEXT_MAIN      = Color.decode("#1E293B");
    public static final Color TEXT_SECONDARY = Color.decode("#64748B");
    public static final Color BORDER_COLOR   = Color.decode("#E2E8F0");
    public static final Color SIDEBAR_BG     = Color.decode("#0F294D");
    public static final Color SIDEBAR_HOVER  = Color.decode("#1A3E70");
    public static final Color SIDEBAR_ACTIVE = Color.decode("#2563EB");
    public static final Color SUCCESS_GREEN  = Color.decode("#10B981");
    public static final Color WARNING_AMBER  = Color.decode("#F59E0B");

    // Typography
    public static final Font FONT_HEADER_LARGE  = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER_MEDIUM = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_HEADER_SMALL  = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR       = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_MEDIUM        = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL         = new Font("Segoe UI", Font.PLAIN, 12);

    private final TechnicalOfficerDAO dao;
    private final TechnicalOfficer currentOfficer;

    private JLabel headerWelcomeLabel;
    private TechnicalOfficerNavigationBar navigationBar;

    public TechnicalOfficerDashboard() {
        this(new TechnicalOfficerDAO());
    }

    public TechnicalOfficerDashboard(TechnicalOfficerDAO dao) {
        this.dao = dao != null ? dao : new TechnicalOfficerDAO();
        this.currentOfficer = this.dao.getCurrentOfficer();

        initFrameSettings();
        buildUI();
    }

    private void initFrameSettings() {
        setTitle("TecFAMS - Technical Officer Dashboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1240, 780);
        setMinimumSize(new Dimension(1020, 680));
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);

        // 1. Top Header Bar
        JPanel topHeader = createHeader();
        getContentPane().add(topHeader, BorderLayout.NORTH);

        // 2. Left Sidebar Navigation
        navigationBar = new TechnicalOfficerNavigationBar(this);
        getContentPane().add(navigationBar, BorderLayout.WEST);

        // 3. Central Dashboard Content
        JScrollPane centerScroll = new JScrollPane(createDashboardContent());
        centerScroll.setBorder(null);
        centerScroll.setBackground(BG_COLOR);
        centerScroll.getVerticalScrollBar().setUnitIncrement(16);
        getContentPane().add(centerScroll, BorderLayout.CENTER);
    }

    /**
     * Top Header adhering to prompt specification:
     * - Top-left: "TecFAMS" in large, bold text
     * - "Technical Officer Dashboard" directly underneath in smaller text
     * - Welcome message: "Welcome back, [Technical Officer Name]!"
     * - User information and avatar area
     */
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(12, 24, 12, 24)
        ));

        // Left Branding Block
        JPanel leftBrand = new JPanel(new GridLayout(2, 1, 0, 2));
        leftBrand.setOpaque(false);

        JLabel brandTitle = new JLabel("TecFAMS");
        brandTitle.setFont(FONT_HEADER_LARGE);
        brandTitle.setForeground(PRIMARY_NAVY);

        JLabel brandSubtitle = new JLabel("Technical Officer Dashboard");
        brandSubtitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        brandSubtitle.setForeground(MAIN_BLUE);

        leftBrand.add(brandTitle);
        leftBrand.add(brandSubtitle);

        // Right User Information Block
        JPanel rightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightInfo.setOpaque(false);

        JPanel textBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        textBlock.setOpaque(false);

        headerWelcomeLabel = new JLabel("Welcome back, " + currentOfficer.getName() + "!");
        headerWelcomeLabel.setFont(FONT_MEDIUM);
        headerWelcomeLabel.setForeground(TEXT_MAIN);

        JLabel deptLabel = new JLabel(currentOfficer.getDepartment() + " • Staff");
        deptLabel.setFont(FONT_SMALL);
        deptLabel.setForeground(TEXT_SECONDARY);

        textBlock.add(headerWelcomeLabel);
        textBlock.add(deptLabel);

        // Avatar circle badge
        JLabel avatarBadge = new JLabel("TO", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(LIGHT_BLUE);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(MAIN_BLUE);
                g2.setFont(FONT_MEDIUM);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        avatarBadge.setPreferredSize(new Dimension(40, 40));

        rightInfo.add(textBlock);
        rightInfo.add(avatarBadge);

        header.add(leftBrand, BorderLayout.WEST);
        header.add(rightInfo, BorderLayout.EAST);
        return header;
    }

    public TechnicalOfficerNavigationBar getNavigationBar() {
        return navigationBar;
    }

    /**
     * Dashboard main content area containing:
     * - Welcome banner
     * - Metric stats row
     * - The 4 core dashboard cards
     */
    private JPanel createDashboardContent() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(24, 28, 24, 28));

        // 1. Welcome Banner
        mainPanel.add(createWelcomeBanner());
        mainPanel.add(Box.createVerticalStrut(20));

        // 2. Metric Counters Row
        mainPanel.add(createStatsRow());
        mainPanel.add(Box.createVerticalStrut(22));

        // 3. Four Dashboard Cards Heading
        mainPanel.add(createSectionHeading("Core Administrative Services", "Direct access to Technical Officer responsibilities"));
        mainPanel.add(Box.createVerticalStrut(12));

        // 4. Four Main Dashboard Cards Grid
        mainPanel.add(createFourCardsGrid());

        return mainPanel;
    }

    private JPanel createWelcomeBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(WHITE);
        banner.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(18, 22, 18, 22)
        ));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 4));
        left.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome back, " + currentOfficer.getName() + "!");
        welcomeLabel.setFont(FONT_HEADER_LARGE);
        welcomeLabel.setForeground(PRIMARY_NAVY);

        JLabel subLabel = new JLabel("Faculty of Technology • Department of " + currentOfficer.getDepartment());
        subLabel.setFont(FONT_REGULAR);
        subLabel.setForeground(TEXT_SECONDARY);

        left.add(welcomeLabel);
        left.add(subLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        right.setOpaque(false);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        JLabel dateLabel = new JLabel("📅  " + today);
        dateLabel.setFont(FONT_MEDIUM);
        dateLabel.setForeground(MAIN_BLUE);
        dateLabel.setBackground(LIGHT_BLUE);
        dateLabel.setOpaque(true);
        dateLabel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#BFDBFE"), 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));

        right.add(dateLabel);

        banner.add(left, BorderLayout.WEST);
        banner.add(right, BorderLayout.EAST);
        return banner;
    }

    private JPanel createStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);

        row.add(createStatCard("Total Attendance Records", String.valueOf(dao.getTotalAttendanceCount()), "Sessions logged", "📝", MAIN_BLUE));
        row.add(createStatCard("Pending Medical Records", String.valueOf(dao.getPendingMedicalCount()), "Requires review", "🏥", WARNING_AMBER));
        row.add(createStatCard("Today's Timetable Sessions", String.valueOf(dao.getTodaySessionsCount()), "Active practical labs", "📅", SUCCESS_GREEN));
        row.add(createStatCard("Faculty Notices", String.valueOf(dao.getActiveNoticesCount()), "Latest circulars", "📢", PRIMARY_NAVY));

        return row;
    }

    private JPanel createStatCard(String title, String count, String subtext, String icon, Color accent) {
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

        JLabel countLabel = new JLabel(count);
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        countLabel.setForeground(TEXT_MAIN);
        countLabel.setBorder(new EmptyBorder(8, 0, 4, 0));

        JLabel sub = new JLabel(subtext);
        sub.setFont(FONT_SMALL);
        sub.setForeground(accent);

        card.add(top, BorderLayout.NORTH);
        card.add(countLabel, BorderLayout.CENTER);
        card.add(sub, BorderLayout.SOUTH);
        return card;
    }

    /**
     * The 4 core dashboard cards specified in the prompt:
     * 1. Attendance Management
     * 2. Medical Records
     * 3. Department Timetable
     * 4. Notices
     */
    private JPanel createFourCardsGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
        grid.setOpaque(false);

        // Card 1: Attendance Management
        grid.add(createActionCard(
                "Attendance Management",
                "Record student attendance for theory and practical sessions. Maintain session logs (15 sessions per component) and verify 80% examination eligibility.",
                "📝",
                "Open Attendance System",
                () -> openSectionAction("Attendance Management", "Undergraduate Attendance Management section.\n(Theory and Practical Attendance, 15 Sessions Per Module)")
        ));

        // Card 2: Medical Records
        grid.add(createActionCard(
                "Medical Records Management",
                "Process undergraduate medical certificate submissions, track approval statuses, and verify legitimate absence documentation.",
                "🏥",
                "Open Medical Records",
                () -> openSectionAction("Medical Records Management", "Undergraduate Medical Records section.\n(Medical Certificate Submissions and Approval Status)")
        ));

        // Card 3: Department Timetable
        grid.add(createActionCard(
                "Department Timetable",
                "View weekly department lecture timetables, computer laboratory allocations, and scheduled timeslots for academic staff and students.",
                "📅",
                "View Timetable",
                () -> openSectionAction("Department Timetable", "Department Academic Timetable section.\n(Lecture Halls, Computer Laboratories, and Scheduled Timeslots)")
        ));

        // Card 4: Notices
        grid.add(createActionCard(
                "Faculty Notices",
                "Read official faculty circulars, examination scheduling announcements, and administrative memorandums published by Administration.",
                "📢",
                "Browse Notices",
                () -> openSectionAction("Faculty Notices", "Faculty Notices and Circulars section.\n(View-Only Access - Managed by Administration)")
        ));

        return grid;
    }

    private JPanel createActionCard(String title, String description, String icon, String actionText, Runnable clickAction) {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(0, 12));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        header.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_HEADER_MEDIUM);
        titleLabel.setForeground(PRIMARY_NAVY);

        header.add(iconLabel);
        header.add(titleLabel);

        JTextArea descArea = new JTextArea(description);
        descArea.setFont(FONT_REGULAR);
        descArea.setForeground(TEXT_SECONDARY);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setOpaque(false);
        descArea.setBorder(new EmptyBorder(4, 34, 4, 10));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);

        JButton actionBtn = createPrimaryButton(actionText + "  ➔");
        actionBtn.addActionListener(e -> clickAction.run());
        footer.add(actionBtn);

        card.add(header, BorderLayout.NORTH);
        card.add(descArea, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                clickAction.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(MAIN_BLUE, 1, true),
                        new EmptyBorder(16, 16, 16, 16)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER_COLOR, 1, true),
                        new EmptyBorder(16, 16, 16, 16)
                ));
            }
        });

        return card;
    }

}