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
     * Initializes the top horizontal navigation layout, buttons, and logout button.
     */
    private void initializeNavigation() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 56));
        setBackground(COLOR_PRIMARY_NAVY);

        // Center: Horizontal Navigation Buttons centered across the bar
        JPanel navList = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 9)) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                Container parent = getParent();
                if (parent instanceof JViewport) {
                    d.width = Math.max(d.width, parent.getWidth());
                }
                return d;
            }
        };
        navList.setOpaque(false);
        navList.setBorder(new EmptyBorder(0, 8, 0, 8));

        // 1. Dashboard (Active main view)
        dashboardBtn = createSidebarItem("Dashboard", "DASHBOARD", true);
        navList.add(dashboardBtn);

        // 2. Lecturer Notes (Connected to enrolled course notes page)
        navList.add(createSidebarItem("Lecturer Notes", "MATERIALS", false));

        // 3. Attendance
        navList.add(createSidebarItem("Attendance", "ATTENDANCE", false));

        // 4. Medical Details
        navList.add(createSidebarItem("Medical Details", "MEDICAL", false));

        // 5. Course Details
        navList.add(createSidebarItem("Course Details", "COURSES", false));

        // 6. Grades & GPA
        navList.add(createSidebarItem("Grades & GPA", "GRADES", false));

        // 7. Timetable
        navList.add(createSidebarItem("Timetable", "TIMETABLE", false));

        // 8. Notices
        navList.add(createSidebarItem("Notices", "NOTICES", false));

        // 9. My Profile (Last item)
        navList.add(createSidebarItem("My Profile", "PROFILE", false));

        JScrollPane scrollPane = new JScrollPane(navList);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 0));
        add(scrollPane, BorderLayout.CENTER);

        // Initial highlight on Dashboard
        highlightButton("DASHBOARD");
    }

    /**
     * Creates a styled navigation button with custom vector icon for horizontal bar.
     *
     * @param title       Display title text.
     * @param moduleKey   Identifier for icon and button mapping.
     * @param isDashboard True if this button controls the Dashboard overview.
     * @return Configured JButton.
     */
    private JButton createSidebarItem(String title, String moduleKey, boolean isDashboard) {
        JButton btn = new JButton(title) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(COLOR_TEXT_MUTED);
        btn.setBackground(COLOR_PRIMARY_NAVY);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setIcon(createNavIcon(moduleKey, COLOR_TEXT_MUTED));
        btn.setIconTextGap(8);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 24, 38));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.getBackground() != COLOR_MAIN_BLUE) {
                    btn.setBackground(COLOR_HOVER_NAVY);
                    btn.setForeground(COLOR_WHITE);
                    btn.setIcon(createNavIcon(moduleKey, COLOR_WHITE));
                    btn.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.getBackground() != COLOR_MAIN_BLUE) {
                    btn.setBackground(COLOR_PRIMARY_NAVY);
                    btn.setForeground(COLOR_TEXT_MUTED);
                    btn.setIcon(createNavIcon(moduleKey, COLOR_TEXT_MUTED));
                    btn.repaint();
                }
            }
        });

        btn.addActionListener(e -> {
            if (isDashboard) {
                highlightButton("DASHBOARD");
                if (parentDashboard != null) {
                    parentDashboard.showDashboardView();
                }
            } else if ("MATERIALS".equals(moduleKey)) {
                highlightButton("MATERIALS");
                if (parentDashboard != null) {
                    parentDashboard.openLectureNotes();
                }
            } else if ("ATTENDANCE".equals(moduleKey)) {
                highlightButton("ATTENDANCE");
                if (parentDashboard != null) {
                    parentDashboard.openAttendance();
                }
            } else if ("MEDICAL".equals(moduleKey)) {
                highlightButton("MEDICAL");
                if (parentDashboard != null) {
                    parentDashboard.openMedical();
                }
            } else if ("COURSES".equals(moduleKey)) {
                highlightButton("COURSES");
                if (parentDashboard != null) {
                    parentDashboard.openCourseDetails();
                }
            } else if ("GRADES".equals(moduleKey)) {
                highlightButton("GRADES");
                if (parentDashboard != null) {
                    parentDashboard.openGrades();
                }
            } else if ("TIMETABLE".equals(moduleKey)) {
                highlightButton("TIMETABLE");
                if (parentDashboard != null) {
                    parentDashboard.openTimetable();
                }
            } else {
                if (parentDashboard != null) {
                    parentDashboard.showModulePlaceholder(title);
                }
            }
        });

        navButtons.put(moduleKey, btn);
        return btn;
    }

    /**
     * Highlights the navigation button corresponding to the specified key,
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
            b.repaint();
        }

        JButton selected = navButtons.get(moduleKey);
        if (selected != null) {
            selected.setBackground(COLOR_MAIN_BLUE);
            selected.setForeground(COLOR_WHITE);
            selected.setFont(new Font("Segoe UI", Font.BOLD, 13));
            selected.setIcon(createNavIcon(moduleKey, COLOR_WHITE));
            selected.repaint();
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
