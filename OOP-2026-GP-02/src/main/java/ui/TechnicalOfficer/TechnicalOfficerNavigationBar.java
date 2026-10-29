package main.java.ui.TechnicalOfficer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import static main.java.ui.TechnicalOfficer.TechnicalOfficerDashboard.*;

/**
 * Top Horizontal Navigation Bar component for Technical Officer Dashboard in TecFAMS.
 * University of Ruhuna - Faculty of Technology.
 * 
 * Features:
 * - Full-width horizontal top navigation bar positioned directly below header
 * - Modern tab buttons with hover transitions and active indicator
 * - Horizontal links to Dashboard, Attendance, Medical Records, Timetable, Notices, and Profile
 * - Dedicated styled Logout button on the right edge with confirmation
 */
public class   TechnicalOfficerNavigationBar extends JPanel {

    private final TechnicalOfficerDashboard dashboard;
    private final Map<String, NavTabButton> navButtonMap = new LinkedHashMap<>();
    private String activeNavKey = "DASHBOARD";

    public TechnicalOfficerNavigationBar() {
        this(null);
    }

    public TechnicalOfficerNavigationBar(TechnicalOfficerDashboard dashboard) {
        this.dashboard = dashboard;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(SIDEBAR_BG);
        setPreferredSize(new Dimension(1000, 48));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#1E3A5F")));

        // 1. Navigation items (Left aligned)
        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(0, 16, 0, 0));

        addNavItem(menuPanel, "DASHBOARD",  "📊", "Dashboard", () -> selectNav("DASHBOARD"));
        addNavItem(menuPanel, "ATTENDANCE", "📝", "Attendance Management", () -> {
            if (dashboard != null) {
                dashboard.openAttendanceManagement();
            }
        });
        addNavItem(menuPanel, "MEDICAL",    "🏥", "Medical Records", () -> {
            if (dashboard != null) {
                dashboard.openMedicalManagement();
            }
        });
        addNavItem(menuPanel, "TIMETABLE",  "📅", "Department Timetable", () -> {
            if (dashboard != null) {
                dashboard.openDepartmentTimetable();
            } else {
                new DepartmentTimetableForm().setVisible(true);
            }
        });
        addNavItem(menuPanel, "NOTICES",    "📢", "Notices", () -> {
            if (dashboard != null) {
                dashboard.openFacultyNotices();
            } else {
                new FacultyNoticesForm().setVisible(true);
            }
        });
        addNavItem(menuPanel, "PROFILE",    "👤", "My Profile", () -> {
            if (dashboard != null) {
                dashboard.showProfileDialog();
            }
        });

        add(menuPanel, BorderLayout.CENTER);

        // 2. Right side Logout action
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(new EmptyBorder(0, 0, 0, 16));

        LogoutButton logoutBtn = new LogoutButton("🚪", "Logout");
        logoutBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (dashboard != null) {
                    dashboard.handleLogout();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                logoutBtn.setHovered(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoutBtn.setHovered(false);
            }
        });

        rightPanel.add(logoutBtn);
        add(rightPanel, BorderLayout.EAST);
    }

    private void addNavItem(JPanel container, String key, String icon, String label, Runnable action) {
        NavTabButton btn = new NavTabButton(key, icon, label, key.equals(activeNavKey));
        navButtonMap.put(key, btn);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectNav(key);
                action.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setHovered(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setHovered(false);
            }
        });

        container.add(btn);
    }

    public void selectNav(String key) {
        this.activeNavKey = key;
        for (Map.Entry<String, NavTabButton> entry : navButtonMap.entrySet()) {
            boolean active = entry.getKey().equals(key);
            entry.getValue().setActive(active);
        }
    }

    public String getActiveNavKey() {
        return activeNavKey;
    }

    /**
     * Modern rounded navigation tab button component for top horizontal navbar.
     */
    public static class NavTabButton extends JPanel {
        private final String key;
        private boolean active;
        private boolean hovered;
        private final JLabel iconLabel;
        private final JLabel textLabel;

        public NavTabButton(String key, String icon, String text, boolean isActive) {
            super(new FlowLayout(FlowLayout.CENTER, 6, 8));
            this.key = key;
            this.active = isActive;
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(0, 12, 0, 12));

            iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            iconLabel.setForeground(isActive ? WHITE : Color.decode("#CBD5E1"));

            textLabel = new JLabel(text);
            textLabel.setFont(FONT_MEDIUM);
            textLabel.setForeground(isActive ? WHITE : Color.decode("#CBD5E1"));

            add(iconLabel);
            add(textLabel);
        }

        public void setActive(boolean active) {
            this.active = active;
            updateColors();
            repaint();
        }

        public void setHovered(boolean hovered) {
            this.hovered = hovered;
            updateColors();
            repaint();
        }

        private void updateColors() {
            Color color = (active || hovered) ? WHITE : Color.decode("#CBD5E1");
            iconLabel.setForeground(color);
            textLabel.setForeground(color);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active) {
                g2.setColor(SIDEBAR_ACTIVE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            } else if (hovered) {
                g2.setColor(SIDEBAR_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Dedicated styled Logout button on the right edge of top navigation bar.
     */
    public static class LogoutButton extends JPanel {
        private boolean hovered;
        private final JLabel iconLabel;
        private final JLabel textLabel;

        public LogoutButton(String icon, String text) {
            super(new FlowLayout(FlowLayout.CENTER, 6, 8));
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(0, 14, 0, 14));

            iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            iconLabel.setForeground(Color.decode("#FCA5A5"));

            textLabel = new JLabel(text);
            textLabel.setFont(FONT_MEDIUM);
            textLabel.setForeground(Color.decode("#FCA5A5"));

            add(iconLabel);
            add(textLabel);
        }

        public void setHovered(boolean hovered) {
            this.hovered = hovered;
            Color c = hovered ? WHITE : Color.decode("#FCA5A5");
            iconLabel.setForeground(c);
            textLabel.setForeground(c);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (hovered) {
                g2.setColor(Color.decode("#DC2626"));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            } else {
                g2.setColor(Color.decode("#1E293B"));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
