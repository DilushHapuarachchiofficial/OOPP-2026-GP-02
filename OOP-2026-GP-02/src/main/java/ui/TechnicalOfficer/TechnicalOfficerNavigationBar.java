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
 * Sidebar Navigation Bar component for Technical Officer Dashboard in TecFAMS.
 * University of Ruhuna - Faculty of Technology.
 * 
 * Features:
 * - Faculty Portal university branding header
 * - Navigation buttons with active and hover states
 * - Links to Attendance, Medical Records, Timetable, Notices, and Profile
 * - Logout button with hover effect and confirmation
 */
public class TechnicalOfficerNavigationBar extends JPanel {

    private final TechnicalOfficerDashboard dashboard;
    private final Map<String, JPanel> navButtonMap = new LinkedHashMap<>();
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
        setPreferredSize(new Dimension(245, 0));

        // 1. Top University Header Label
        JPanel topLabelPanel = new JPanel(new BorderLayout());
        topLabelPanel.setBackground(Color.decode("#091B33"));
        topLabelPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("FACULTY PORTAL");
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setForeground(Color.decode("#94A3B8"));

        JLabel uni = new JLabel("University of Ruhuna");
        uni.setFont(FONT_SMALL);
        uni.setForeground(Color.decode("#64748B"));

        topLabelPanel.add(title, BorderLayout.NORTH);
        topLabelPanel.add(uni, BorderLayout.SOUTH);
        add(topLabelPanel, BorderLayout.NORTH);

        // 2. Navigation Items Menu
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(12, 0, 12, 0));

        addNavItem(menuPanel, "DASHBOARD",  "📊  Dashboard", () -> selectNav("DASHBOARD"));
        addNavItem(menuPanel, "ATTENDANCE", "📝  Attendance Management", () -> {
            if (dashboard != null) {
                dashboard.openSectionAction("Attendance Management", "Undergraduate Attendance Management section.\n(Theory and Practical Attendance, 15 Sessions Per Module)");
            }
        });
        addNavItem(menuPanel, "MEDICAL",    "🏥  Medical Records", () -> {
            if (dashboard != null) {
                dashboard.openSectionAction("Medical Records Management", "Undergraduate Medical Records section.\n(Medical Certificate Submissions and Approval Status)");
            }
        });
        addNavItem(menuPanel, "TIMETABLE",  "📅  Department Timetable", () -> {
            if (dashboard != null) {
                dashboard.openSectionAction("Department Timetable", "Department Academic Timetable section.\n(Lecture Halls, Computer Laboratories, and Scheduled Timeslots)");
            }
        });
        addNavItem(menuPanel, "NOTICES",    "📢  Notices", () -> {
            if (dashboard != null) {
                dashboard.openSectionAction("Faculty Notices", "Faculty Notices and Circulars section.\n(View-Only Access - Managed by Administration)");
            }
        });
        addNavItem(menuPanel, "PROFILE",    "👤  My Profile", () -> {
            if (dashboard != null) {
                dashboard.showProfileDialog();
            }
        });

        add(menuPanel, BorderLayout.CENTER);

        // 3. Bottom Logout Action
        JPanel logoutPanel = new JPanel(new BorderLayout());
        logoutPanel.setOpaque(false);
        logoutPanel.setBorder(new EmptyBorder(10, 12, 16, 12));

        JPanel logoutBtn = createSidebarButton("🚪  Logout", false);
        logoutBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (dashboard != null) {
                    dashboard.handleLogout();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                logoutBtn.setBackground(Color.decode("#7F1D1D"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoutBtn.setBackground(SIDEBAR_BG);
            }
        });

        logoutPanel.add(logoutBtn, BorderLayout.CENTER);
        add(logoutPanel, BorderLayout.SOUTH);
    }

    private void addNavItem(JPanel container, String key, String label, Runnable action) {
        JPanel btn = createSidebarButton(label, key.equals(activeNavKey));
        navButtonMap.put(key, btn);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectNav(key);
                action.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!key.equals(activeNavKey)) {
                    btn.setBackground(SIDEBAR_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!key.equals(activeNavKey)) {
                    btn.setBackground(SIDEBAR_BG);
                }
            }
        });

        container.add(btn);
        container.add(Box.createVerticalStrut(4));
    }

    private JPanel createSidebarButton(String text, boolean isActive) {
        JPanel p = new JPanel(new BorderLayout());
        p.setMaximumSize(new Dimension(245, 44));
        p.setPreferredSize(new Dimension(245, 44));
        p.setBackground(isActive ? SIDEBAR_ACTIVE : SIDEBAR_BG);
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));
        p.setBorder(new EmptyBorder(0, 16, 0, 16));

        JLabel label = new JLabel(text);
        label.setFont(FONT_MEDIUM);
        label.setForeground(isActive ? WHITE : Color.decode("#CBD5E1"));

        p.add(label, BorderLayout.WEST);
        return p;
    }

    public void selectNav(String key) {
        this.activeNavKey = key;
        for (Map.Entry<String, JPanel> entry : navButtonMap.entrySet()) {
            boolean active = entry.getKey().equals(key);
            JPanel btn = entry.getValue();
            btn.setBackground(active ? SIDEBAR_ACTIVE : SIDEBAR_BG);
            Component[] comps = btn.getComponents();
            if (comps.length > 0 && comps[0] instanceof JLabel) {
                ((JLabel) comps[0]).setForeground(active ? WHITE : Color.decode("#CBD5E1"));
            }
        }
    }

    public String getActiveNavKey() {
        return activeNavKey;
    }
}
