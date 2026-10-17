package main.java.ui.Undergraduate;

import main.java.model.Undergraduate.Undergraduate;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TecFAMS Undergraduate Navigation Component.
 * Dedicated sidebar navigation panel for the Undergraduate interface in
 * the Faculty of Technology Academic Management System.
 * Built with custom vector Graphics2D icons to guarantee crisp,
 * modern rendering on all operating systems without missing-glyph boxes.
 */
public class UndergraduateNavigation extends JPanel {

    // Theme Color Palette
    private static final Color COLOR_PRIMARY_NAVY = new Color(0x12, 0x3B, 0x70); // #123B70
    private static final Color COLOR_MAIN_BLUE    = new Color(0x25, 0x63, 0xEB); // #2563EB
    private static final Color COLOR_WHITE        = new Color(0xFF, 0xFF, 0xFF); // #FFFFFF
    private static final Color COLOR_TEXT_MUTED   = new Color(0xD0, 0xDE, 0xF2); // #D0DEF2
    private static final Color COLOR_HOVER_NAVY   = new Color(0x1B, 0x48, 0x85); // #1B4885
    private static final Color COLOR_TITLE_MUTED  = new Color(0x8E, 0xA8, 0xCF); // #8EA8CF
    private static final Color COLOR_FOOTER_MUTED = new Color(0x7F, 0xA3, 0xD1); // #7FA3D1
    private static final Color COLOR_LOGOUT_RED   = new Color(0xFF, 0xAA, 0xAA); // #FFAAAA

    private final UndergraduateDashboard parentDashboard;
    private final Undergraduate currentStudent;
    private final Map<String, JButton> navButtons;
    private JButton dashboardBtn;

    /**
     * Constructs the sidebar navigation panel.
     *
     * @param parentDashboard The parent UndergraduateDashboard window.
     * @param student         The currently logged in undergraduate model.
     */
    public UndergraduateNavigation(UndergraduateDashboard parentDashboard, Undergraduate student) {
        this.parentDashboard = parentDashboard;
        this.currentStudent = student;
        this.navButtons = new LinkedHashMap<>();

        initializeNavigation();
    }

    /**
     * Overloaded constructor for standalone usage.
     *
     * @param parentDashboard The parent UndergraduateDashboard window.
     */
    public UndergraduateNavigation(UndergraduateDashboard parentDashboard) {
        this(parentDashboard, null);
    }

    /**
     * Initializes the sidebar navigation layout, buttons, and footer.
     */
    private void initializeNavigation() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(240, 0));
        setBackground(COLOR_PRIMARY_NAVY);

        JPanel navList = new JPanel();
        navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));
        navList.setOpaque(false);
        navList.setBorder(new EmptyBorder(16, 12, 16, 12));

        // Section Title
        JLabel menuTitle = new JLabel("ACADEMIC MENU");
        menuTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        menuTitle.setForeground(COLOR_TITLE_MUTED);
        menuTitle.setBorder(new EmptyBorder(8, 12, 10, 12));
        navList.add(menuTitle);

        // 1. Dashboard (Active main view)
        dashboardBtn = createSidebarItem("Dashboard", "DASHBOARD", true);
        navList.add(dashboardBtn);
        navList.add(Box.createVerticalStrut(4));

        // 2-9. Other Academic Modules (Open separate UI / show message)
        navList.add(createSidebarItem("My Profile", "PROFILE", false));
        navList.add(Box.createVerticalStrut(4));

        navList.add(createSidebarItem("Attendance", "ATTENDANCE", false));
        navList.add(Box.createVerticalStrut(4));

        navList.add(createSidebarItem("Medical Details", "MEDICAL", false));
        navList.add(Box.createVerticalStrut(4));

        navList.add(createSidebarItem("Course Details", "COURSES", false));
        navList.add(Box.createVerticalStrut(4));

        navList.add(createSidebarItem("Grades & GPA", "GRADES", false));
        navList.add(Box.createVerticalStrut(4));

        navList.add(createSidebarItem("Timetable", "TIMETABLE", false));
        navList.add(Box.createVerticalStrut(4));

        navList.add(createSidebarItem("Lecturer Notes", "MATERIALS", false));
        navList.add(Box.createVerticalStrut(4));

        navList.add(createSidebarItem("Notices", "NOTICES", false));
        navList.add(Box.createVerticalGlue());

        // Logout Button at bottom
        JButton logoutBtn = createLogoutButton();
        navList.add(logoutBtn);

        add(navList, BorderLayout.CENTER);

        // Footer Info
        JLabel facultyFooter = new JLabel("Faculty of Technology");
        facultyFooter.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        facultyFooter.setForeground(COLOR_FOOTER_MUTED);
        facultyFooter.setHorizontalAlignment(SwingConstants.CENTER);
        facultyFooter.setBorder(new EmptyBorder(0, 0, 14, 0));
        add(facultyFooter, BorderLayout.SOUTH);

        // Initial highlight on Dashboard
        highlightButton("DASHBOARD");
    }

    /**
     * Creates a styled sidebar navigation button with custom vector icon.
     *
     * @param title       Display title text.
     * @param moduleKey   Identifier for icon and button mapping.
     * @param isDashboard True if this button controls the Dashboard overview.
     * @return Configured JButton.
     */
    private JButton createSidebarItem(String title, String moduleKey, boolean isDashboard) {
        JButton btn = new JButton(title);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(COLOR_TEXT_MUTED);
        btn.setBackground(COLOR_PRIMARY_NAVY);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIcon(createNavIcon(moduleKey, COLOR_TEXT_MUTED));
        btn.setIconTextGap(12);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(216, 40));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.getBackground() != COLOR_MAIN_BLUE) {
                    btn.setBackground(COLOR_HOVER_NAVY);
                    btn.setForeground(COLOR_WHITE);
                    btn.setIcon(createNavIcon(moduleKey, COLOR_WHITE));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.getBackground() != COLOR_MAIN_BLUE) {
                    btn.setBackground(COLOR_PRIMARY_NAVY);
                    btn.setForeground(COLOR_TEXT_MUTED);
                    btn.setIcon(createNavIcon(moduleKey, COLOR_TEXT_MUTED));
                }
            }
        });

        btn.addActionListener(e -> {
            if (isDashboard) {
                highlightButton("DASHBOARD");
                if (parentDashboard != null) {
                    parentDashboard.showDashboardView();
                }
            } else {
                // As requested, clicking separate modules shows an informative message dialog
                if (parentDashboard != null) {
                    parentDashboard.showModulePlaceholder(title);
                }
            }
        });

        navButtons.put(moduleKey, btn);
        return btn;
    }

    /**
     * Creates the styled logout button with confirmation dialog.
     *
     * @return Configured logout JButton.
     */
    private JButton createLogoutButton() {
        JButton btn = new JButton("Logout");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(COLOR_LOGOUT_RED);
        btn.setBackground(COLOR_PRIMARY_NAVY);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIcon(createNavIcon("LOGOUT", COLOR_LOGOUT_RED));
        btn.setIconTextGap(12);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(216, 40));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to log out of TecFAMS?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (choice == JOptionPane.YES_OPTION) {
                if (currentStudent != null) {
                    currentStudent.logout();
                }
                if (parentDashboard != null) {
                    parentDashboard.dispose();
                }
            }
        });

        return btn;
    }

    /**
     * Highlights the sidebar button corresponding to the specified key,
     * resetting all other buttons to standard styling.
     *
     * @param moduleKey The module identifier (e.g. "DASHBOARD").
     */
    public void highlightButton(String moduleKey) {
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton b = entry.getValue();
            b.setBackground(COLOR_PRIMARY_NAVY);
            b.setForeground(COLOR_TEXT_MUTED);
            b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            b.setIcon(createNavIcon(entry.getKey(), COLOR_TEXT_MUTED));
        }

        JButton selected = navButtons.get(moduleKey);
        if (selected != null) {
            selected.setBackground(COLOR_MAIN_BLUE);
            selected.setForeground(COLOR_WHITE);
            selected.setFont(new Font("Segoe UI", Font.BOLD, 13));
            selected.setIcon(createNavIcon(moduleKey, COLOR_WHITE));
        }
    }

    public JButton getDashboardButton() {
        return dashboardBtn;
    }

    public JButton getNavButton(String moduleKey) {
        return navButtons.get(moduleKey);
    }

    /**
     * Factory for custom Graphics2D vector icons.
     * Prevents missing-glyph boxes ("□") across all operating systems.
     *
     * @param iconType Name of icon to draw.
     * @param color    Rendering color.
     * @return Custom painted Icon.
     */
    private static Icon createNavIcon(String iconType, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);

                switch (iconType) {
                    case "DASHBOARD":
                        // 2x2 grid tile squares
                        g2.fillRoundRect(x + 1, y + 1, 6, 6, 2, 2);
                        g2.fillRoundRect(x + 9, y + 1, 6, 6, 2, 2);
                        g2.fillRoundRect(x + 1, y + 9, 6, 6, 2, 2);
                        g2.fillRoundRect(x + 9, y + 9, 6, 6, 2, 2);
                        break;
                    case "PROFILE":
                        // Person silhouette (head and shoulders)
                        g2.fillOval(x + 5, y + 1, 6, 6);
                        g2.fillArc(x + 1, y + 8, 14, 10, 0, 180);
                        break;
                    case "ATTENDANCE":
                        // Calendar with header bar and dots
                        g2.drawRoundRect(x + 1, y + 3, 14, 12, 2, 2);
                        g2.fillRect(x + 1, y + 3, 14, 4);
                        g2.fillRect(x + 4, y + 1, 2, 3);
                        g2.fillRect(x + 10, y + 1, 2, 3);
                        g2.fillRect(x + 4, y + 9, 2, 2);
                        g2.fillRect(x + 7, y + 9, 2, 2);
                        g2.fillRect(x + 10, y + 9, 2, 2);
                        break;
                    case "MEDICAL":
                        // First aid medical cross
                        g2.fillRect(x + 6, y + 2, 4, 12);
                        g2.fillRect(x + 2, y + 6, 12, 4);
                        break;
                    case "COURSES":
                        // Book
                        g2.drawRoundRect(x + 2, y + 2, 12, 12, 2, 2);
                        g2.drawLine(x + 8, y + 2, x + 8, y + 14);
                        g2.drawLine(x + 4, y + 5, x + 6, y + 5);
                        g2.drawLine(x + 10, y + 5, x + 12, y + 5);
                        break;
                    case "GRADES":
                        // 3-column bar chart
                        g2.fillRect(x + 2, y + 9, 3, 5);
                        g2.fillRect(x + 6, y + 5, 3, 9);
                        g2.fillRect(x + 10, y + 2, 3, 12);
                        break;
                    case "TIMETABLE":
                        // Clock dial with hands
                        g2.drawOval(x + 1, y + 1, 14, 14);
                        g2.drawLine(x + 8, y + 8, x + 8, y + 4);
                        g2.drawLine(x + 8, y + 8, x + 12, y + 8);
                        break;
                    case "MATERIALS":
                        // Document page with lines
                        g2.drawRoundRect(x + 2, y + 1, 11, 14, 2, 2);
                        g2.drawLine(x + 5, y + 5, x + 10, y + 5);
                        g2.drawLine(x + 5, y + 8, x + 10, y + 8);
                        g2.drawLine(x + 5, y + 11, x + 9, y + 11);
                        break;
                    case "NOTICES":
                        // Bell
                        g2.fillArc(x + 3, y + 3, 10, 10, 0, 180);
                        g2.fillRect(x + 2, y + 8, 12, 3);
                        g2.fillOval(x + 6, y + 12, 4, 3);
                        break;
                    case "LOGOUT":
                        // Doorway and exit arrow
                        g2.drawRect(x + 1, y + 2, 7, 12);
                        g2.drawLine(x + 7, y + 8, x + 14, y + 8);
                        g2.drawLine(x + 12, y + 6, x + 14, y + 8);
                        g2.drawLine(x + 12, y + 10, x + 14, y + 8);
                        break;
                    default:
                        g2.fillOval(x + 5, y + 5, 6, 6);
                        break;
                }
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return 16; }

            @Override
            public int getIconHeight() { return 16; }
        };
    }
}
