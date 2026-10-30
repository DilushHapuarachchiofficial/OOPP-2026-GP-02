package main.java.ui.TechnicalOfficer;

import main.java.dao.TechnicalOfficer.TechnicalOfficerDAO;
import main.java.model.TechnicalOfficer.TechnicalOfficer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
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

    // TecFAMS Specified Color Palette - Primary button standardized to #1565C0
    public static final Color PRIMARY_NAVY   = Color.decode("#123B70");
    public static final Color PRIMARY_BLUE   = Color.decode("#1565C0");
    public static final Color PRIMARY_BLUE_HOVER = Color.decode("#0D47A1");
    public static final Color MAIN_BLUE      = PRIMARY_BLUE;
    public static final Color LIGHT_BLUE     = Color.decode("#EAF2FF");
    public static final Color WHITE          = Color.decode("#FFFFFF");
    public static final Color BG_COLOR       = Color.decode("#F5F8FD");
    public static final Color TEXT_MAIN      = Color.decode("#1E293B");
    public static final Color TEXT_SECONDARY = Color.decode("#64748B");
    public static final Color BORDER_COLOR   = Color.decode("#E2E8F0");
    public static final Color SIDEBAR_BG     = Color.decode("#0F294D");
    public static final Color SIDEBAR_HOVER  = Color.decode("#1A3E70");
    public static final Color SIDEBAR_ACTIVE = PRIMARY_BLUE;
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
    private JLabel deptLabel;
    private JLabel avatarBadge;
    private BufferedImage cachedAvatarPhoto = null;
    private TechnicalOfficerNavigationBar navigationBar;

    // Single-Window Multi-View Navigation (CardLayout)
    private CardLayout cardLayout;
    private JPanel centerCardPanel;

    private AttendanceManagementForm attendanceForm;
    private MedicalManagementForm medicalForm;
    private DepartmentTimetableForm timetableForm;
    private FacultyNoticesForm noticesForm;
    private TechnicalOfficerProfileForm profileForm;

    public TechnicalOfficerDashboard() {
        this((TechnicalOfficer) null);
    }

    public TechnicalOfficerDashboard(TechnicalOfficerDAO dao) {
        this.dao = dao != null ? dao : new TechnicalOfficerDAO();
        this.currentOfficer = this.dao.getCurrentOfficer();

        initFrameSettings();
        buildUI();
    }

    public TechnicalOfficerDashboard(TechnicalOfficer officer) {
        if (officer != null) {
            this.currentOfficer = officer;
            int officerId = -1;
            try {
                if (officer.getOfficerId() != null && !officer.getOfficerId().trim().isEmpty()) {
                    officerId = Integer.parseInt(officer.getOfficerId().trim());
                }
            } catch (Exception ignored) {}
            this.dao = new TechnicalOfficerDAO(officerId);
            this.dao.setCurrentOfficer(this.currentOfficer);
        } else {
            this.dao = new TechnicalOfficerDAO();
            this.currentOfficer = this.dao.getCurrentOfficer();
        }

        initFrameSettings();
        buildUI();
    }

    private void initFrameSettings() {
        setTitle("TecFAMS - Technical Officer Dashboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(1080, 700));
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);

        // Top Container: Combines the branding header & horizontal navigation bar
        JPanel topContainer = new JPanel(new BorderLayout());

        // 1. Top Header Bar
        JPanel topHeader = createHeader();
        topContainer.add(topHeader, BorderLayout.NORTH);

        // 2. Top Navigation Bar (Horizontal)
        navigationBar = new TechnicalOfficerNavigationBar(this);
        topContainer.add(navigationBar, BorderLayout.SOUTH);

        getContentPane().add(topContainer, BorderLayout.NORTH);

        // 3. Central Card Panel for Single-Window Multi-Page Navigation
        cardLayout = new CardLayout();
        centerCardPanel = new JPanel(cardLayout);
        centerCardPanel.setBackground(BG_COLOR);

        // Primary Card: Home Dashboard
        JScrollPane centerScroll = new JScrollPane(createDashboardContent());
        centerScroll.setBorder(null);
        centerScroll.setBackground(BG_COLOR);
        centerScroll.getVerticalScrollBar().setUnitIncrement(16);
        centerCardPanel.add(centerScroll, "DASHBOARD");

        getContentPane().add(centerCardPanel, BorderLayout.CENTER);
    }

    /**
     * Top Header adhering to specification:
     * - Top-left: "TecFAMS" in large, bold text
     * - "Technical Officer Dashboard" directly underneath in smaller text
     * - Faculty Portal / University of Ruhuna branding tag
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

        // Left Branding Block with University Tag
        JPanel leftBrand = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        leftBrand.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);

        JLabel brandTitle = new JLabel("TecFAMS");
        brandTitle.setFont(FONT_HEADER_LARGE);
        brandTitle.setForeground(PRIMARY_NAVY);

        JLabel brandSubtitle = new JLabel("Technical Officer Dashboard");
        brandSubtitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        brandSubtitle.setForeground(MAIN_BLUE);

        titleBlock.add(brandTitle);
        titleBlock.add(brandSubtitle);

        // University Tag Badge
        JPanel uniTag = new JPanel(new GridLayout(2, 1, 0, 1));
        uniTag.setOpaque(false);
        uniTag.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER_COLOR),
                new EmptyBorder(2, 14, 2, 0)
        ));

        JLabel facultyLabel = new JLabel("FACULTY PORTAL");
        facultyLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        facultyLabel.setForeground(Color.decode("#64748B"));

        JLabel uniLabel = new JLabel("University of Ruhuna");
        uniLabel.setFont(FONT_SMALL);
        uniLabel.setForeground(TEXT_SECONDARY);

        uniTag.add(facultyLabel);
        uniTag.add(uniLabel);

        leftBrand.add(titleBlock);
        leftBrand.add(uniTag);

        // Right User Information Block
        JPanel rightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightInfo.setOpaque(false);

        JPanel textBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        textBlock.setOpaque(false);

        String officerName = (currentOfficer != null && currentOfficer.getName() != null && !currentOfficer.getName().trim().isEmpty())
                ? currentOfficer.getName().trim() : "Technical Officer";
        headerWelcomeLabel = new JLabel("Welcome back, " + officerName + "!");
        headerWelcomeLabel.setFont(FONT_MEDIUM);
        headerWelcomeLabel.setForeground(TEXT_MAIN);

        String deptName = (currentOfficer != null && currentOfficer.getDepartment() != null && !currentOfficer.getDepartment().trim().isEmpty())
                ? currentOfficer.getDepartment().trim() : "Faculty of Technology";
        deptLabel = new JLabel(deptName + " • Staff");
        deptLabel.setFont(FONT_SMALL);
        deptLabel.setForeground(TEXT_SECONDARY);

        textBlock.add(headerWelcomeLabel);
        textBlock.add(deptLabel);

        cachedAvatarPhoto = (currentOfficer != null) ? currentOfficer.getProfileImage() : null;

        // Avatar circle badge - renders database profile picture if available, else "TO"
        avatarBadge = new JLabel("", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                BufferedImage photo = cachedAvatarPhoto;
                if (photo == null && currentOfficer != null) {
                    cachedAvatarPhoto = currentOfficer.getProfileImage();
                    photo = cachedAvatarPhoto;
                }

                int w = getWidth();
                int h = getHeight();

                if (photo != null) {
                    Shape oldClip = g2.getClip();
                    Ellipse2D.Float circle = new Ellipse2D.Float(0, 0, w, h);
                    g2.clip(circle);

                    int imgW = photo.getWidth();
                    int imgH = photo.getHeight();
                    double scale = Math.max((double) w / imgW, (double) h / imgH);
                    int drawW = (int) Math.round(imgW * scale);
                    int drawH = (int) Math.round(imgH * scale);
                    int drawX = (w - drawW) / 2;
                    int drawY = (h - drawH) / 2;

                    g2.drawImage(photo, drawX, drawY, drawW, drawH, null);
                    g2.setClip(oldClip);

                    g2.setColor(Color.decode("#BFDBFE"));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawOval(0, 0, w - 1, h - 1);
                } else {
                    g2.setColor(LIGHT_BLUE);
                    g2.fillOval(0, 0, w, h);
                    g2.setColor(MAIN_BLUE);
                    g2.setFont(FONT_MEDIUM);
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (w - fm.stringWidth("TO")) / 2;
                    int y = ((h - fm.getHeight()) / 2) + fm.getAscent();
                    g2.drawString("TO", x, y);

                    g2.setColor(Color.decode("#BFDBFE"));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawOval(0, 0, w - 1, h - 1);
                }
                g2.dispose();
            }
        };
        avatarBadge.setPreferredSize(new Dimension(40, 40));
        avatarBadge.setMinimumSize(new Dimension(40, 40));
        avatarBadge.setMaximumSize(new Dimension(40, 40));
        avatarBadge.setCursor(new Cursor(Cursor.HAND_CURSOR));
        avatarBadge.setToolTipText("View / Edit Profile");
        avatarBadge.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showProfileDialog();
            }
        });

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

        String officerName = (currentOfficer != null && currentOfficer.getName() != null && !currentOfficer.getName().trim().isEmpty())
                ? currentOfficer.getName().trim() : "Technical Officer";
        JLabel welcomeLabel = new JLabel("Welcome back, " + officerName + "!");
        welcomeLabel.setFont(FONT_HEADER_LARGE);
        welcomeLabel.setForeground(PRIMARY_NAVY);

        String deptName = (currentOfficer != null && currentOfficer.getDepartment() != null && !currentOfficer.getDepartment().trim().isEmpty())
                ? currentOfficer.getDepartment().trim() : "Technology";
        JLabel subLabel = new JLabel("Faculty of Technology • " + (deptName.startsWith("Department") ? deptName : ("Department of " + deptName)));
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
                this::openAttendanceManagement
        ));

        // Card 2: Medical Records
        grid.add(createActionCard(
                "Medical Records Management",
                "Process undergraduate medical certificate submissions, track approval statuses, and verify legitimate absence documentation.",
                "🏥",
                "Open Medical Records",
                this::openMedicalManagement
        ));

        // Card 3: Department Timetable
        grid.add(createActionCard(
                "Department Timetable",
                "View weekly department lecture timetables, computer laboratory allocations, and scheduled timeslots for academic staff and students.",
                "📅",
                "View Timetable",
                this::openDepartmentTimetable
        ));

        // Card 4: Notices
        grid.add(createActionCard(
                "Faculty Notices",
                "Read official faculty circulars, examination scheduling announcements, and administrative memorandums published by Administration.",
                "📢",
                "Browse Notices",
                this::openFacultyNotices
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


    private JPanel createSectionHeading(String title, String subtitle) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setOpaque(false);

        JLabel t = new JLabel(title);
        t.setFont(FONT_HEADER_MEDIUM);
        t.setForeground(PRIMARY_NAVY);

        JLabel s = new JLabel(subtitle);
        s.setFont(FONT_SMALL);
        s.setForeground(TEXT_SECONDARY);

        p.add(t);
        p.add(s);
        return p;
    }

    /**
     * Switches the center view to the main Dashboard card.
     */
    public void showDashboardView() {
        if (cardLayout != null && centerCardPanel != null) {
            cardLayout.show(centerCardPanel, "DASHBOARD");
        }
        if (navigationBar != null) {
            navigationBar.selectNav("DASHBOARD");
        }
        refreshHeaderProfile();
    }

    /**
     * Switches the center view to Attendance Management inside the same window.
     */
    public void openAttendanceManagement() {
        try {
            if (attendanceForm == null) {
                attendanceForm = new AttendanceManagementForm(this, currentOfficer);
                centerCardPanel.add(attendanceForm.getContentPane(), "ATTENDANCE");
            }
            cardLayout.show(centerCardPanel, "ATTENDANCE");
            if (navigationBar != null) {
                navigationBar.selectNav("ATTENDANCE");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open Attendance Management: " + ex.getMessage(),
                    "Navigation Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Switches the center view to Medical Records Management inside the same window.
     */
    public void openMedicalManagement() {
        try {
            if (medicalForm == null) {
                medicalForm = new MedicalManagementForm(this, currentOfficer);
                centerCardPanel.add(medicalForm.getContentPane(), "MEDICAL");
            }
            cardLayout.show(centerCardPanel, "MEDICAL");
            if (navigationBar != null) {
                navigationBar.selectNav("MEDICAL");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open Medical Records Management: " + ex.getMessage(),
                    "Navigation Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Switches the center view to Department Timetable inside the same window.
     */
    public void openDepartmentTimetable() {
        try {
            if (timetableForm == null) {
                timetableForm = new DepartmentTimetableForm(this, currentOfficer);
                centerCardPanel.add(timetableForm.getContentPane(), "TIMETABLE");
            }
            cardLayout.show(centerCardPanel, "TIMETABLE");
            if (navigationBar != null) {
                navigationBar.selectNav("TIMETABLE");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open Department Timetable: " + ex.getMessage(),
                    "Navigation Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Switches the center view to Faculty Notices inside the same window.
     */
    public void openFacultyNotices() {
        try {
            if (noticesForm == null) {
                noticesForm = new FacultyNoticesForm(this, currentOfficer);
                centerCardPanel.add(noticesForm.getContentPane(), "NOTICES");
            }
            cardLayout.show(centerCardPanel, "NOTICES");
            if (navigationBar != null) {
                navigationBar.selectNav("NOTICES");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open Faculty Notices: " + ex.getMessage(),
                    "Navigation Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void openSectionAction(String title, String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                title + " - TecFAMS",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Switches the center view to the Technical Officer Profile Form inside the same window.
     */
    public void showProfileDialog() {
        try {
            if (profileForm == null) {
                profileForm = new TechnicalOfficerProfileForm(this, currentOfficer, dao);
                profileForm.setOnProfileSavedCallback(() -> {
                    refreshHeaderProfile();
                });
                centerCardPanel.add(profileForm.getContentPane(), "PROFILE");
            }
            cardLayout.show(centerCardPanel, "PROFILE");
            if (navigationBar != null) {
                navigationBar.selectNav("PROFILE");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open Profile Form: " + ex.getMessage(),
                    "Navigation Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Refreshes the header officer profile data and circular avatar image from database.
     */
    public void refreshHeaderProfile() {
        if (currentOfficer != null) {
            int id = TechnicalOfficerDAO.parseOfficerId(currentOfficer);
            dao.loadOfficerFromDatabase(id);
            cachedAvatarPhoto = currentOfficer.getProfileImage();

            if (headerWelcomeLabel != null) {
                String officerName = (currentOfficer.getName() != null && !currentOfficer.getName().trim().isEmpty())
                        ? currentOfficer.getName().trim() : "Technical Officer";
                headerWelcomeLabel.setText("Welcome back, " + officerName + "!");
            }
            if (deptLabel != null) {
                String deptName = (currentOfficer.getDepartment() != null && !currentOfficer.getDepartment().trim().isEmpty())
                        ? currentOfficer.getDepartment().trim() : "Faculty of Technology";
                deptLabel.setText(deptName + " • Staff");
            }
            if (avatarBadge != null) {
                avatarBadge.repaint();
            }
        }
    }

    public void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out of the Technical Officer Dashboard?",
                "Confirm Logout - TecFAMS",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            if (currentOfficer != null) {
                currentOfficer.logout();
            }
            main.java.model.UserSession.clearSession();
            dispose();
            SwingUtilities.invokeLater(() -> {
                main.java.ui.LoginUI loginUI = new main.java.ui.LoginUI();
                loginUI.setVisible(true);
            });
        }
    }

    // --- Styling Utilities ---

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_MEDIUM);
        button.setForeground(WHITE);
        button.setBackground(PRIMARY_BLUE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(PRIMARY_BLUE_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY_BLUE);
            }
        });
        return button;
    }

    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_MEDIUM);
        button.setForeground(TEXT_MAIN);
        button.setBackground(WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(7, 15, 7, 15)
        ));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(LIGHT_BLUE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(WHITE);
            }
        });
        return button;
    }


    /**
     * Standalone main method to run and preview the Technical Officer Dashboard.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            TechnicalOfficerDashboard dashboard = new TechnicalOfficerDashboard();
            dashboard.setVisible(true);
        });
    }
}
