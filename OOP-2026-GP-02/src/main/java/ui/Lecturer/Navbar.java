package main.java.ui.Lecturer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Premium TecFAMS Lecturer Horizontal Navigation Bar in Java Swing.
 *
 * Designed specifically for the Faculty of Technology Academic Management System.
 * Fully self-contained component:
 * - Pure typography academic branding (no external logo image)
 * - 4 core horizontal navigation items (Dashboard, Course Materials, Student Eligibility, Marks Management)
 * - Profile dropdown menu with "My Profile" and "Logout"
 * - Genuine database & session notifications loaded from tecfams_db
 */
public class Navbar extends JPanel {

    // =========================================================================
    // COLOR PALETTE (TecFAMS Academic Theme)
    // =========================================================================
    public static final Color COLOR_DEEP_NAVY      = new Color(8, 47, 90);    // #082F5A - Brand & primary headings
    public static final Color COLOR_PRO_BLUE       = new Color(11, 79, 156);  // #0B4F9C - Navigation active text & primary accents
    public static final Color COLOR_TECH_BLUE      = new Color(40, 120, 208); // #2878D0 - Active indicator underline & highlight
    public static final Color COLOR_WHITE          = new Color(255, 255, 255);// #FFFFFF - Background
    public static final Color COLOR_LIGHT_BG       = new Color(244, 247, 251);// #F4F7FB - Page background
    public static final Color COLOR_SUBTLE_BORDER  = new Color(220, 228, 238);// #DCE4EE - Divider & bottom border
    public static final Color COLOR_NOTIF_ACCENT   = new Color(255, 107, 0);  // #FF6B00 - Notification badge
    public static final Color COLOR_TEXT_INACTIVE  = new Color(71, 85, 105);  // #475569 - Slate text for inactive links
    public static final Color COLOR_HOVER_BG       = new Color(241, 245, 251);// Subtle soft hover
    public static final Color COLOR_ACTIVE_BG      = new Color(235, 243, 254);// Soft blue tint for active pill
    public static final Color COLOR_DANGER         = new Color(220, 38, 38);  // #DC2626 - Logout / Warning red

    // =========================================================================
    // TYPOGRAPHY (Segoe UI Hierarchy)
    // =========================================================================
    public static final Font FONT_BRAND_TITLE    = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_BRAND_SUBTITLE = new Font("Segoe UI Semibold", Font.BOLD, 9);
    public static final Font FONT_NAV_ITEM       = new Font("Segoe UI Semibold", Font.PLAIN, 13);
    public static final Font FONT_NOTIF_BADGE    = new Font("Segoe UI", Font.BOLD, 10);
    public static final Font FONT_USER_NAME      = new Font("Segoe UI Semibold", Font.BOLD, 12);
    public static final Font FONT_USER_ROLE      = new Font("Segoe UI", Font.PLAIN, 10);
    public static final Font FONT_POPUP_HEADER   = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_POPUP_ITEM     = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_POPUP_TIME     = new Font("Segoe UI", Font.PLAIN, 10);
    public static final Font FONT_MENU_ITEM       = new Font("Segoe UI Semibold", Font.PLAIN, 12);

    // Preferred Navbar Height
    public static final int NAVBAR_HEIGHT = 80;

    // Navigation Pages Constants
    public static final String PAGE_DASHBOARD     = "Dashboard";
    public static final String PAGE_MATERIALS     = "Course Materials";
    public static final String PAGE_ELIGIBILITY   = "Student Eligibility";
    public static final String PAGE_MARKS         = "Marks Management";
    public static final String PAGE_PROFILE       = "My Profile";

    // Callbacks
    public interface NavigationListener {
        void onNavigate(String pageName);
    }

    public interface LogoutListener {
        void onLogout();
    }

    private final List<NavigationListener> navListeners = new ArrayList<>();
    private final List<LogoutListener> logoutListeners = new ArrayList<>();
    private final Map<String, NavButton> navButtons = new LinkedHashMap<>();

    // State Variables
    private String activePage = PAGE_DASHBOARD;
    private String lecturerName = "Dr. Chinthaka Premachandra";
    private String lecturerInitials = "CP";

    // Database & Session Notifications List
    private final List<NotificationItem> notifications = new ArrayList<>();

    // UI Elements
    private JPanel brandSection;
    private JPanel navSection;
    private JPanel actionsSection;
    private JButton bellButton;
    private NotificationBadgeOverlay badgeOverlay;
    private JPopupMenu notificationPopup;
    private JPopupMenu profileDropdown;
    private JPanel profilePill;
    private JLabel avatarLabel;
    private JLabel profileNameLabel;

    /**
     * Constructs a Navbar with default lecturer and Dashboard active.
     */
    public Navbar() {
        this(PAGE_DASHBOARD);
    }

    /**
     * Constructs a Navbar with a designated initial active page.
     */
    public Navbar(String initialActivePage) {
        this("Dr. Chinthaka Premachandra", "CP", initialActivePage);
    }

    /**
     * Constructs a Navbar with custom lecturer name, initials, and active page.
     */
    public Navbar(String lecturerName, String lecturerInitials, String initialActivePage) {
        this.lecturerName = (lecturerName != null && !lecturerName.trim().isEmpty()) ? lecturerName : "Dr. Chinthaka Premachandra";
        this.lecturerInitials = (lecturerInitials != null && !lecturerInitials.trim().isEmpty()) ? lecturerInitials : "CP";
        this.activePage = (initialActivePage != null) ? initialActivePage : PAGE_DASHBOARD;

        loadDatabaseAndSessionNotifications();
        initializeNavbar();
    }

    // =========================================================================
    // DATABASE & SESSION NOTIFICATIONS LOADER
    // =========================================================================
    /**
     * Loads genuine notifications:
     * 1. Lecturer login event notification (timestamped).
     * 2. Official faculty notices loaded from tecfams_db database or tecfams_db.sql dump.
     */
    private void loadDatabaseAndSessionNotifications() {
        notifications.clear();

        // 1. Session Login Notification
        String currentTime = new SimpleDateFormat("hh:mm a").format(new Date());
        notifications.add(new NotificationItem(
                "LOGIN-001",
                "You logged in to TecFAMS",
                "Session active for " + lecturerName + " (Role: Lecturer). Welcome to the Academic Portal.",
                "LOGIN",
                "Today at " + currentTime,
                false
        ));

        // 2. Load Real Notices from Database (or SQL dump if MySQL is offline)
        List<NotificationItem> dbNotices = fetchNoticesFromDatabaseOrDump();
        notifications.addAll(dbNotices);
    }

    /**
     * Attempts MySQL query first; falls back cleanly to parsing src/main/resources/database/tecfams_db.sql
     */
    private List<NotificationItem> fetchNoticesFromDatabaseOrDump() {
        List<NotificationItem> list = new ArrayList<>();

        // Method A: Direct JDBC Query (if MySQL server is running)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/tecfams_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    "root", ""
            );
            if (conn != null) {
                String sql = "SELECT notice_id, title, description, published_date FROM notices " +
                             "WHERE target_audience IN ('All', 'Lecturers') ORDER BY notice_id DESC";
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    list.add(new NotificationItem(
                            "NOTICE-" + rs.getInt("notice_id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            "NOTICE",
                            rs.getString("published_date"),
                            false
                    ));
                }
                conn.close();
                return list;
            }
        } catch (Exception ignored) {
            // MySQL offline or driver not configured; proceed to fallback
        }

        // Method B: Parse resources/database/tecfams_db.sql dump
        String[] possibleSqlPaths = {
                "src/main/resources/database/tecfams_db.sql",
                "src/main/resources/database/tecFams.sql",
                "src/main/resources/database/tecfams_db",
                "resources/database/tecfams_db.sql"
        };

        BufferedReader reader = null;
        for (String p : possibleSqlPaths) {
            File f = new File(p);
            if (f.exists() && f.isFile()) {
                try {
                    reader = new BufferedReader(new FileReader(f));
                    break;
                } catch (Exception ignored) {}
            }
        }

        if (reader == null) {
            InputStream is = getClass().getResourceAsStream("/database/tecfams_db.sql");
            if (is != null) {
                reader = new BufferedReader(new InputStreamReader(is));
            }
        }

        if (reader != null) {
            try (BufferedReader br = reader) {
                String line;
                boolean inNotices = false;
                Pattern p = Pattern.compile("\\(\\s*(\\d+)\\s*,\\s*'([^']+)'\\s*,\\s*'([^']+)'\\s*,\\s*'([^']+)'\\s*,\\s*'([^']+)'");

                while ((line = br.readLine()) != null) {
                    if (line.contains("INSERT INTO `notices`") || line.contains("INSERT INTO notices")) {
                        inNotices = true;
                    }
                    if (inNotices) {
                        Matcher m = p.matcher(line);
                        while (m.find()) {
                            String id = m.group(1);
                            String title = m.group(2);
                            String desc = m.group(3);
                            String date = m.group(4);
                            String audience = m.group(5);

                            if ("All".equalsIgnoreCase(audience) || "Lecturers".equalsIgnoreCase(audience)) {
                                list.add(0, new NotificationItem(
                                        "NOTICE-" + id,
                                        title,
                                        desc,
                                        "NOTICE",
                                        date,
                                        false
                                ));
                            }
                        }
                        if (line.trim().endsWith(";")) {
                            inNotices = false;
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        return list;
    }

    // =========================================================================
    // INITIALIZATION & LAYOUT
    // =========================================================================
    private void initializeNavbar() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1100, NAVBAR_HEIGHT));
        setMinimumSize(new Dimension(860, NAVBAR_HEIGHT));
        setBackground(COLOR_WHITE);

        // Section A: Left Brand (Pure clean typography branding)
        brandSection = createBrandSection();
        add(brandSection, BorderLayout.WEST);

        // Section B: Center Navigation Menu (Dashboard, Materials, Eligibility, Marks)
        navSection = createNavigationSection();
        add(navSection, BorderLayout.CENTER);

        // Section C: Right Actions (Notifications Bell + Profile Pill with Dropdown)
        actionsSection = createActionsSection();
        add(actionsSection, BorderLayout.EAST);

        // Set initial active button
        setActivePage(activePage);
    }

    /**
     * Paints subtle bottom separator border and clean shadow depth.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1px bottom border
        g2.setColor(COLOR_SUBTLE_BORDER);
        g2.drawLine(0, h - 1, w, h - 1);

        // Soft 2px bottom shadow line
        g2.setColor(new Color(230, 235, 245, 120));
        g2.drawLine(0, h - 2, w, h - 2);

        g2.dispose();
    }

    // =========================================================================
    // SECTION A: BRANDING (Pure Clean Typography, No External Logo)
    // =========================================================================
    private JPanel createBrandSection() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 24, 16, 14));

        // Brand Typography Container
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("TecFAMS");
        titleLabel.setFont(FONT_BRAND_TITLE);
        titleLabel.setForeground(COLOR_DEEP_NAVY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("FACULTY OF TECHNOLOGY");
        subtitleLabel.setFont(FONT_BRAND_SUBTITLE);
        subtitleLabel.setForeground(COLOR_PRO_BLUE);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(Box.createVerticalGlue());
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(subtitleLabel);
        textPanel.add(Box.createVerticalGlue());

        // Vertical divider separating branding from navigation
        JSeparator divider = new JSeparator(SwingConstants.VERTICAL);
        divider.setPreferredSize(new Dimension(1, 32));
        divider.setForeground(COLOR_SUBTLE_BORDER);

        panel.add(textPanel);
        panel.add(Box.createHorizontalStrut(8));
        panel.add(divider);

        return panel;
    }

    // =========================================================================
    // SECTION B: NAVIGATION MENU (Dashboard, Materials, Eligibility, Marks)
    // =========================================================================
    private JPanel createNavigationSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(18, 4, 18, 4));

        panel.add(Box.createHorizontalGlue());
        addNavItem(panel, PAGE_DASHBOARD, "Dashboard", NavVectorIcon.create(NavVectorIcon.Type.DASHBOARD));
        panel.add(Box.createHorizontalStrut(6));
        addNavItem(panel, PAGE_MATERIALS, "Course Materials", NavVectorIcon.create(NavVectorIcon.Type.MATERIALS));
        panel.add(Box.createHorizontalStrut(6));
        addNavItem(panel, PAGE_ELIGIBILITY, "Student Eligibility", NavVectorIcon.create(NavVectorIcon.Type.ELIGIBILITY));
        panel.add(Box.createHorizontalStrut(6));
        addNavItem(panel, PAGE_MARKS, "Marks Management", NavVectorIcon.create(NavVectorIcon.Type.MARKS));
        panel.add(Box.createHorizontalGlue());

        return panel;
    }

    private void addNavItem(JPanel container, String pageId, String label, NavVectorIcon icon) {
        NavButton button = new NavButton(pageId, label, icon);
        button.addActionListener(e -> {
            setActivePage(pageId);
            notifyNavigationListeners(pageId);
        });
        navButtons.put(pageId, button);
        container.add(button);
    }

    // =========================================================================
    // SECTION C: ACTIONS SECTION (Notification Bell + Profile Dropdown)
    // =========================================================================
    private JPanel createActionsSection() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 18));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 4, 0, 18));

        // 1. Notification Bell Button Container with Badge
        JLayeredPane notifContainer = new JLayeredPane();
        notifContainer.setPreferredSize(new Dimension(42, 42));

        bellButton = new JButton();
        bellButton.setIcon(NavVectorIcon.create(NavVectorIcon.Type.BELL));
        bellButton.setToolTipText("Notifications");
        bellButton.setFocusPainted(false);
        bellButton.setBorderPainted(false);
        bellButton.setContentAreaFilled(false);
        bellButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bellButton.setBounds(1, 1, 40, 40);

        // Bell hover circle styling
        bellButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                bellButton.setOpaque(true);
                bellButton.setBackground(COLOR_HOVER_BG);
                bellButton.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                bellButton.setOpaque(false);
                bellButton.setBackground(COLOR_WHITE);
                bellButton.repaint();
            }
        });

        // Click bell to show dropdown popup directly beneath it
        bellButton.addActionListener(e -> toggleNotificationPopup());

        // Unread Badge Overlay
        badgeOverlay = new NotificationBadgeOverlay(getUnreadNotificationCount());
        badgeOverlay.setBounds(22, 0, 20, 20);

        notifContainer.add(bellButton, Integer.valueOf(1));
        notifContainer.add(badgeOverlay, Integer.valueOf(2));

        // 2. Subtle Divider
        JSeparator divider = new JSeparator(SwingConstants.VERTICAL);
        divider.setPreferredSize(new Dimension(1, 28));
        divider.setForeground(COLOR_SUBTLE_BORDER);

        // 3. Lecturer Profile Pill (Click to scroll down / popup menu with My Profile & Logout)
        profilePill = createProfilePill();

        panel.add(notifContainer);
        panel.add(divider);
        panel.add(profilePill);

        return panel;
    }

    /**
     * Creates the lecturer profile badge that opens the dropdown menu on click.
     */
    private JPanel createProfilePill() {
        JPanel pill = new JPanel(new BorderLayout(8, 0)) {
            private boolean hovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        toggleProfileDropdown();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hovered) {
                    g2.setColor(COLOR_HOVER_BG);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 36, 36));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        pill.setOpaque(false);
        pill.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pill.setBorder(new EmptyBorder(3, 6, 3, 10));
        pill.setToolTipText("Click to view profile options & logout");

        // Circular Avatar with Initials
        avatarLabel = new JLabel(lecturerInitials, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_DEEP_NAVY);
                g2.fill(new Ellipse2D.Float(0, 0, getWidth(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatarLabel.setPreferredSize(new Dimension(32, 32));
        avatarLabel.setForeground(COLOR_WHITE);
        avatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));

        // Name and Designation Column
        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        profileNameLabel = new JLabel(lecturerName);
        profileNameLabel.setFont(FONT_USER_NAME);
        profileNameLabel.setForeground(COLOR_DEEP_NAVY);

        JLabel roleLabel = new JLabel("Lecturer");
        roleLabel.setFont(FONT_USER_ROLE);
        roleLabel.setForeground(COLOR_TEXT_INACTIVE);

        textCol.add(Box.createVerticalGlue());
        textCol.add(profileNameLabel);
        textCol.add(roleLabel);
        textCol.add(Box.createVerticalGlue());

        // Vector Chevron Down Indicator
        JLabel chevronLabel = new JLabel(new ChevronDownIcon(8, 5));

        pill.add(avatarLabel, BorderLayout.WEST);
        pill.add(textCol, BorderLayout.CENTER);
        pill.add(chevronLabel, BorderLayout.EAST);

        return pill;
    }

    // =========================================================================
    // PROFILE DROPDOWN MENU (My Profile & Logout)
    // =========================================================================
    private void toggleProfileDropdown() {
        if (profileDropdown != null && profileDropdown.isVisible()) {
            profileDropdown.setVisible(false);
            return;
        }

        if (profilePill == null || !profilePill.isShowing()) {
            return;
        }

        profileDropdown = createProfileDropdown();
        int popupWidth = 220;
        int xOffset = profilePill.getWidth() - popupWidth;
        profileDropdown.show(profilePill, xOffset, profilePill.getHeight() + 6);
    }

    private JPopupMenu createProfileDropdown() {
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(COLOR_WHITE);
        popup.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_SUBTLE_BORDER, 1),
                new EmptyBorder(6, 0, 6, 0)
        ));

        // Header info
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(6, 16, 8, 16));

        JLabel nameHeader = new JLabel(lecturerName);
        nameHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameHeader.setForeground(COLOR_DEEP_NAVY);

        JLabel roleHeader = new JLabel("Lecturer • Faculty of Technology");
        roleHeader.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        roleHeader.setForeground(COLOR_TEXT_INACTIVE);

        headerPanel.add(nameHeader);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(roleHeader);

        popup.add(headerPanel);

        JSeparator sep1 = new JSeparator();
        sep1.setForeground(COLOR_SUBTLE_BORDER);
        popup.add(sep1);

        // 1. My Profile Menu Item
        JMenuItem profileItem = new JMenuItem("  My Profile");
        profileItem.setFont(FONT_MENU_ITEM);
        profileItem.setForeground(COLOR_DEEP_NAVY);
        profileItem.setBackground(COLOR_WHITE);
        profileItem.setBorder(new EmptyBorder(8, 16, 8, 16));
        profileItem.setIcon(NavVectorIcon.create(NavVectorIcon.Type.PROFILE));
        profileItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profileItem.addActionListener(e -> {
            // Deselect horizontal nav tabs so active styling is clean
            setActivePage(PAGE_PROFILE);
            notifyNavigationListeners(PAGE_PROFILE);
        });

        // 2. Logout Menu Item
        JMenuItem logoutItem = new JMenuItem("  Logout");
        logoutItem.setFont(FONT_MENU_ITEM);
        logoutItem.setForeground(COLOR_DANGER);
        logoutItem.setBackground(COLOR_WHITE);
        logoutItem.setBorder(new EmptyBorder(8, 16, 8, 16));
        logoutItem.setIcon(new LogoutIcon(16, 16));
        logoutItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutItem.addActionListener(e -> handleLogoutAction());

        popup.add(profileItem);

        JSeparator sep2 = new JSeparator();
        sep2.setForeground(new Color(241, 245, 249));
        popup.add(sep2);

        popup.add(logoutItem);

        return popup;
    }

    /**
     * Handles logout confirmation and redirects back to LoginUI.
     */
    private void handleLogoutAction() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out of TecFAMS?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            // Alert registered logout listeners
            for (LogoutListener listener : logoutListeners) {
                listener.onLogout();
            }

            // Close current window
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
            }

            // Return to LoginUI
            try {
                Class<?> loginClass = Class.forName("main.java.ui.LoginUI");
                Object loginInstance = loginClass.getDeclaredConstructor().newInstance();
                if (loginInstance instanceof JFrame) {
                    ((JFrame) loginInstance).setVisible(true);
                }
            } catch (Exception ex) {
                // Standalone fallback message
                JOptionPane.showMessageDialog(null, "You have been logged out successfully.", "Logged Out", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    // =========================================================================
    // NOTIFICATION DROPDOWN POPUP (Real Database & Session Data)
    // =========================================================================
    public int getUnreadNotificationCount() {
        int count = 0;
        for (NotificationItem n : notifications) {
            if (!n.isRead()) {
                count++;
            }
        }
        return count;
    }

    public void markNotificationAsRead(String id) {
        if (id == null) return;
        for (NotificationItem n : notifications) {
            if (id.equals(n.getId())) {
                n.setRead(true);
                break;
            }
        }
        updateNotificationBadge(getUnreadNotificationCount());
    }

    public void markAllNotificationsAsRead() {
        for (NotificationItem n : notifications) {
            n.setRead(true);
        }
        updateNotificationBadge(0);
    }

    public void updateNotificationBadge(int count) {
        if (badgeOverlay != null) {
            badgeOverlay.setCount(count);
        }
    }

    private void toggleNotificationPopup() {
        if (notificationPopup != null && notificationPopup.isVisible()) {
            notificationPopup.setVisible(false);
            return;
        }

        if (bellButton == null || !bellButton.isShowing()) {
            return;
        }

        notificationPopup = createNotificationPopup();
        int popupWidth = 360;
        int xOffset = bellButton.getWidth() - popupWidth;
        notificationPopup.show(bellButton, xOffset, bellButton.getHeight() + 8);
    }

    private JPopupMenu createNotificationPopup() {
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(COLOR_WHITE);
        popup.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_SUBTLE_BORDER, 1),
                new EmptyBorder(8, 0, 8, 0)
        ));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(COLOR_WHITE);
        content.setPreferredSize(new Dimension(360, 340));

        // 1. Popup Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(8, 16, 10, 16));

        int unread = getUnreadNotificationCount();
        JLabel titleLbl = new JLabel("Notifications (" + unread + ")");
        titleLbl.setFont(FONT_POPUP_HEADER);
        titleLbl.setForeground(COLOR_DEEP_NAVY);

        JButton markAllBtn = new JButton("Mark all as read");
        markAllBtn.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 11));
        markAllBtn.setForeground(COLOR_TECH_BLUE);
        markAllBtn.setBorderPainted(false);
        markAllBtn.setContentAreaFilled(false);
        markAllBtn.setFocusPainted(false);
        markAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        markAllBtn.addActionListener(e -> {
            markAllNotificationsAsRead();
            popup.setVisible(false);
            toggleNotificationPopup();
        });

        header.add(titleLbl, BorderLayout.WEST);
        if (unread > 0) {
            header.add(markAllBtn, BorderLayout.EAST);
        }

        content.add(header);

        JSeparator sep = new JSeparator();
        sep.setForeground(COLOR_SUBTLE_BORDER);
        content.add(sep);

        // 2. Notification Items List
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(COLOR_WHITE);

        if (notifications.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setBackground(COLOR_WHITE);
            emptyPanel.setPreferredSize(new Dimension(340, 180));
            JLabel emptyLbl = new JLabel("No notifications at this time");
            emptyLbl.setFont(FONT_POPUP_ITEM);
            emptyLbl.setForeground(COLOR_TEXT_INACTIVE);
            emptyPanel.add(emptyLbl);
            listPanel.add(emptyPanel);
        } else {
            for (NotificationItem notif : notifications) {
                JPanel itemRow = createNotificationItemRow(notif, popup);
                listPanel.add(itemRow);
                JSeparator itemSep = new JSeparator();
                itemSep.setForeground(new Color(241, 245, 249));
                listPanel.add(itemSep);
            }
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        content.add(scroll);

        popup.add(content);
        return popup;
    }

    private JPanel createNotificationItemRow(NotificationItem notif, JPopupMenu popup) {
        JPanel row = new JPanel(new BorderLayout(12, 4)) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        markNotificationAsRead(notif.getId());
                        popup.setVisible(false);
                        toggleNotificationPopup();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                if (hovered) {
                    g2.setColor(COLOR_HOVER_BG);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                } else if (!notif.isRead()) {
                    g2.setColor(new Color(248, 250, 252));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        row.setOpaque(false);
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));
        row.setBorder(new EmptyBorder(10, 16, 10, 16));

        // Category Tag Badge
        JLabel catIcon = new JLabel(getCategoryBadgeText(notif.getCategory()), SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getCategoryColor(notif.getCategory()));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        catIcon.setPreferredSize(new Dimension(36, 26));
        catIcon.setFont(new Font("Segoe UI", Font.BOLD, 9));
        catIcon.setForeground(COLOR_WHITE);

        // Text details
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLbl = new JLabel(notif.getTitle());
        titleLbl.setFont(new Font("Segoe UI", notif.isRead() ? Font.PLAIN : Font.BOLD, 12));
        titleLbl.setForeground(notif.isRead() ? COLOR_TEXT_INACTIVE : COLOR_DEEP_NAVY);

        JLabel descLbl = new JLabel("<html><body style='width: 220px;'>" + notif.getDescription() + "</body></html>");
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLbl.setForeground(new Color(100, 116, 139));

        JLabel timeLbl = new JLabel(notif.getTimestamp());
        timeLbl.setFont(FONT_POPUP_TIME);
        timeLbl.setForeground(new Color(148, 163, 184));

        textPanel.add(titleLbl);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(descLbl);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(timeLbl);

        // Unread status dot
        if (!notif.isRead()) {
            JLabel unreadDot = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(COLOR_NOTIF_ACCENT);
                    g2.fillOval(2, getHeight() / 2 - 4, 8, 8);
                    g2.dispose();
                }
            };
            unreadDot.setPreferredSize(new Dimension(14, 14));
            row.add(unreadDot, BorderLayout.EAST);
        }

        row.add(catIcon, BorderLayout.WEST);
        row.add(textPanel, BorderLayout.CENTER);

        return row;
    }

    private String getCategoryBadgeText(String category) {
        if ("LOGIN".equalsIgnoreCase(category)) return "LOG";
        if ("NOTICE".equalsIgnoreCase(category)) return "FOT";
        return "INFO";
    }

    private Color getCategoryColor(String category) {
        if ("LOGIN".equalsIgnoreCase(category)) return new Color(16, 185, 129); // Green for login session
        if ("NOTICE".equalsIgnoreCase(category)) return COLOR_PRO_BLUE;        // Blue for faculty notice
        return COLOR_TECH_BLUE;
    }

    // =========================================================================
    // PUBLIC API & NAVIGATION MANAGEMENT
    // =========================================================================
    public void setActivePage(String pageName) {
        if (pageName == null) return;
        this.activePage = pageName;

        for (Map.Entry<String, NavButton> entry : navButtons.entrySet()) {
            boolean isActive = entry.getKey().equalsIgnoreCase(pageName);
            entry.getValue().setActive(isActive);
        }
        repaint();
    }

    public String getActivePage() {
        return activePage;
    }

    public void setLecturer(String name, String initials) {
        if (name != null && !name.trim().isEmpty()) {
            this.lecturerName = name;
            if (profileNameLabel != null) {
                profileNameLabel.setText(this.lecturerName);
            }
        }
        if (initials != null && !initials.trim().isEmpty()) {
            this.lecturerInitials = initials;
            if (avatarLabel != null) {
                avatarLabel.setText(this.lecturerInitials);
            }
        }
        repaint();
    }

    public String getLecturerName() {
        return lecturerName;
    }

    public String getLecturerInitials() {
        return lecturerInitials;
    }

    public List<NotificationItem> getNotifications() {
        return new ArrayList<>(notifications);
    }

    public void addNotification(NotificationItem item) {
        if (item != null) {
            notifications.add(0, item);
            updateNotificationBadge(getUnreadNotificationCount());
        }
    }

    public void addNavigationListener(NavigationListener listener) {
        if (listener != null && !navListeners.contains(listener)) {
            navListeners.add(listener);
        }
    }

    public void removeNavigationListener(NavigationListener listener) {
        navListeners.remove(listener);
    }

    private void notifyNavigationListeners(String pageName) {
        for (NavigationListener listener : navListeners) {
            listener.onNavigate(pageName);
        }
    }

    public void addLogoutListener(LogoutListener listener) {
        if (listener != null && !logoutListeners.contains(listener)) {
            logoutListeners.add(listener);
        }
    }

    public void removeLogoutListener(LogoutListener listener) {
        logoutListeners.remove(listener);
    }

    // =========================================================================
    // INNER CLASSES: NOTIFICATION ENTITY, BUTTON, BADGE, & VECTOR ICONS
    // =========================================================================
    public static class NotificationItem {
        private final String id;
        private final String title;
        private final String description;
        private final String category;
        private final String timestamp;
        private boolean read;

        public NotificationItem(String id, String title, String description, String category, String timestamp, boolean read) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.category = category;
            this.timestamp = timestamp;
            this.read = read;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
        public String getTimestamp() { return timestamp; }
        public boolean isRead() { return read; }
        public void setRead(boolean read) { this.read = read; }
    }

    private static class NavButton extends JButton {
        private final String pageId;
        private final NavVectorIcon vectorIcon;
        private boolean isActive = false;
        private boolean isHovered = false;

        public NavButton(String pageId, String text, NavVectorIcon vectorIcon) {
            super(text);
            this.pageId = pageId;
            this.vectorIcon = vectorIcon;

            setFont(FONT_NAV_ITEM);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setIcon(vectorIcon);
            setIconTextGap(6);
            setBorder(new EmptyBorder(6, 12, 7, 12));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        public void setActive(boolean active) {
            this.isActive = active;
            setForeground(active ? COLOR_PRO_BLUE : COLOR_TEXT_INACTIVE);
            if (vectorIcon != null) {
                vectorIcon.setActive(active);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (isActive) {
                g2.setColor(COLOR_ACTIVE_BG);
                g2.fill(new RoundRectangle2D.Float(2, 4, w - 4, h - 8, 16, 16));

                g2.setColor(COLOR_TECH_BLUE);
                g2.fill(new RoundRectangle2D.Float(w / 4f, h - 3, w / 2f, 3, 3, 3));
            } else if (isHovered) {
                g2.setColor(COLOR_HOVER_BG);
                g2.fill(new RoundRectangle2D.Float(2, 4, w - 4, h - 8, 16, 16));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class NotificationBadgeOverlay extends JComponent {
        private int count;

        public NotificationBadgeOverlay(int count) {
            this.count = count;
            setOpaque(false);
        }

        public void setCount(int count) {
            this.count = count;
            setVisible(count > 0);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (count <= 0) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int size = 18;
            int x = getWidth() - size - 1;
            int y = 0;

            g2.setColor(COLOR_NOTIF_ACCENT);
            g2.fill(new Ellipse2D.Float(x, y, size, size));

            g2.setColor(COLOR_WHITE);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new Ellipse2D.Float(x, y, size, size));

            g2.setFont(FONT_NOTIF_BADGE);
            String text = count > 9 ? "9+" : String.valueOf(count);
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (size - fm.stringWidth(text)) / 2;
            int ty = y + ((size - fm.getHeight()) / 2) + fm.getAscent();

            g2.setColor(COLOR_WHITE);
            g2.drawString(text, tx, ty);

            g2.dispose();
        }
    }

    public static class NavVectorIcon implements Icon {
        public enum Type { DASHBOARD, MATERIALS, ELIGIBILITY, MARKS, PROFILE, BELL }

        private final Type type;
        private final int width;
        private final int height;
        private boolean active = false;

        public NavVectorIcon(Type type, int width, int height) {
            this.type = type;
            this.width = width;
            this.height = height;
        }

        public static NavVectorIcon create(Type type) {
            int size = (type == Type.BELL) ? 20 : 16;
            return new NavVectorIcon(type, size, size);
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        @Override public int getIconWidth() { return width; }
        @Override public int getIconHeight() { return height; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            Color strokeColor = active ? COLOR_PRO_BLUE : COLOR_TEXT_INACTIVE;
            g2.setColor(strokeColor);

            switch (type) {
                case DASHBOARD:
                    int s = 5;
                    g2.fillRoundRect(x + 1, y + 1, s, s, 2, 2);
                    g2.fillRoundRect(x + 9, y + 1, s, s, 2, 2);
                    g2.fillRoundRect(x + 1, y + 9, s, s, 2, 2);
                    g2.fillRoundRect(x + 9, y + 9, s, s, 2, 2);
                    break;

                case MATERIALS:
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawRoundRect(x + 1, y + 2, 13, 12, 3, 3);
                    g2.drawLine(x + 4, y + 5, x + 11, y + 5);
                    g2.drawLine(x + 4, y + 8, x + 9, y + 8);
                    break;

                case ELIGIBILITY:
                    g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Polygon cap = new Polygon();
                    cap.addPoint(x + 8, y + 2);
                    cap.addPoint(x + 15, y + 6);
                    cap.addPoint(x + 8, y + 10);
                    cap.addPoint(x + 1, y + 6);
                    g2.draw(cap);
                    g2.drawArc(x + 4, y + 8, 8, 5, 0, -180);
                    g2.drawLine(x + 15, y + 6, x + 15, y + 11);
                    break;

                case MARKS:
                    g2.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(x + 1, y + 13, x + 14, y + 13);
                    g2.fillRect(x + 2, y + 8, 3, 5);
                    g2.fillRect(x + 6, y + 5, 3, 8);
                    g2.fillRect(x + 10, y + 2, 3, 11);
                    break;

                case PROFILE:
                    g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawOval(x + 4, y + 1, 7, 7);
                    g2.drawArc(x + 1, y + 8, 13, 9, 0, 180);
                    break;

                case BELL:
                    g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawOval(x + 8, y + 1, 3, 3);
                    g2.drawArc(x + 4, y + 3, 11, 10, 0, 180);
                    g2.drawLine(x + 4, y + 8, x + 2, y + 13);
                    g2.drawLine(x + 15, y + 8, x + 17, y + 13);
                    g2.drawLine(x + 2, y + 13, x + 17, y + 13);
                    g2.fillOval(x + 8, y + 14, 3, 3);
                    break;
            }

            g2.dispose();
        }
    }

    private static class ChevronDownIcon implements Icon {
        private final int width;
        private final int height;

        public ChevronDownIcon(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override public int getIconWidth() { return width; }
        @Override public int getIconHeight() { return height; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COLOR_TEXT_INACTIVE);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Path2D path = new Path2D.Float();
            path.moveTo(x + 1, y + 1);
            path.lineTo(x + width / 2.0f, y + height - 1);
            path.lineTo(x + width - 1, y + 1);
            g2.draw(path);
            g2.dispose();
        }
    }

    private static class LogoutIcon implements Icon {
        private final int width;
        private final int height;

        public LogoutIcon(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override public int getIconWidth() { return width; }
        @Override public int getIconHeight() { return height; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COLOR_DANGER);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Door frame
            g2.drawRect(x + 1, y + 1, 7, 13);

            // Exit arrow pointing right
            g2.drawLine(x + 5, y + 7, x + 14, y + 7);
            g2.drawLine(x + 11, y + 4, x + 14, y + 7);
            g2.drawLine(x + 11, y + 10, x + 14, y + 7);

            g2.dispose();
        }
    }

    // =========================================================================
    // STANDALONE DEMO TEST RUNNER
    // =========================================================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TecFAMS - Lecturer Navbar Preview");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 700);
            frame.setLocationRelativeTo(null);

            Navbar navbar = new Navbar();

            JPanel placeholderContent = new JPanel(new BorderLayout());
            placeholderContent.setBackground(COLOR_LIGHT_BG);

            JLabel statusLabel = new JLabel("Current Selected Page: " + navbar.getActivePage(), SwingConstants.CENTER);
            statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
            statusLabel.setForeground(COLOR_DEEP_NAVY);
            placeholderContent.add(statusLabel, BorderLayout.CENTER);

            // React to navbar clicks
            navbar.addNavigationListener(page -> {
                statusLabel.setText("Current Selected Page: " + page);
            });

            frame.setLayout(new BorderLayout());
            frame.add(navbar, BorderLayout.NORTH);
            frame.add(placeholderContent, BorderLayout.CENTER);
            frame.setVisible(true);
        });
    }
}
