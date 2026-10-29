package main.java.ui.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import main.java.dao.admin.DashboardDAO;
import main.java.dao.admin.SettingsDAO;
import main.java.dao.admin.UserDAO;

public class DashboardHomePanel extends JPanel {

    private String username;
    private JLabel welcomeLabel;

    // Academic session labels (updated dynamically from Settings)
    private JLabel academicYearValLabel;
    private JLabel academicSemesterValLabel;

    // Metric cards value labels (updated dynamically)
    private JLabel totalUsersValLabel;
    private JLabel undergraduatesValLabel;
    private JLabel lecturersValLabel;
    private JLabel totalCoursesValLabel;
    private JLabel activeNoticesValLabel;
    private JLabel timetablesValLabel;
    private JLabel techOfficersValLabel;

    public DashboardHomePanel() {
        this(null);
    }

    public DashboardHomePanel(String username) {
        if (username == null || username.trim().isEmpty()) {
            main.java.model.admin.Admin a = UserDAO.getAdminProfile(1);
            this.username = (a != null && a.getUsername() != null) ? a.getUsername() : "admin";
        } else {
            this.username = username;
        }

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        ScrollablePanel content = new ScrollablePanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(25, 25, 30, 25));

        // --- Welcome Header Section (At the very top) ---
        JPanel welcomeHeaderPanel = new JPanel(new BorderLayout());
        welcomeHeaderPanel.setOpaque(false);
        welcomeHeaderPanel.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel welcomeTextBlock = new JPanel();
        welcomeTextBlock.setLayout(new BoxLayout(welcomeTextBlock, BoxLayout.Y_AXIS));
        welcomeTextBlock.setOpaque(false);

        welcomeLabel = new JLabel("Welcome, " + this.username + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        welcomeLabel.setForeground(new Color(23, 32, 48));

        String dateStr = new SimpleDateFormat("EEEE, MMMM dd, yyyy").format(new Date());
        JLabel welcomeSubtitle = new JLabel("Here is an overview of what is happening across your faculty today • " + dateStr);
        welcomeSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        welcomeSubtitle.setForeground(new Color(120, 130, 140));

        welcomeTextBlock.add(welcomeLabel);
        welcomeTextBlock.add(Box.createVerticalStrut(4));
        welcomeTextBlock.add(welcomeSubtitle);

        welcomeHeaderPanel.add(welcomeTextBlock, BorderLayout.WEST);
        content.add(welcomeHeaderPanel);

        // --- Active Academic Session & Cohort Section ---
        String initialYear = SettingsDAO.getSetting("academic.year", "2024/2025");
        String initialSem = SettingsDAO.getSetting("academic.semester", "Semester 1");

        JPanel sessionCardsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        sessionCardsPanel.setOpaque(false);
        sessionCardsPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        academicYearValLabel = new JLabel(initialYear);
        academicSemesterValLabel = new JLabel(initialSem);

        JPanel yearCard = createSessionCard(
                "CURRENT ACADEMIC YEAR",
                academicYearValLabel,
                "Active undergraduate academic cohort",
                "● Active Cohort",
                new Color(79, 70, 229),      // Indigo accent
                new Color(238, 242, 255),    // Soft Indigo icon bg
                new Color(224, 231, 255),    // Badge bg
                new Color(67, 56, 202),      // Badge text
                "src/main/resources/images/admin-icons/card_academic_year.png"
        );

        JPanel semesterCard = createSessionCard(
                "CURRENT ACTIVE SEMESTER",
                academicSemesterValLabel,
                "Determines timetable and attendance period",
                "● Operational Term",
                new Color(13, 148, 136),     // Teal accent
                new Color(204, 251, 241),    // Soft Teal icon bg
                new Color(204, 251, 241),    // Badge bg
                new Color(15, 118, 110),     // Badge text
                "src/main/resources/images/admin-icons/card_semester.png"
        );

        sessionCardsPanel.add(yearCard);
        sessionCardsPanel.add(semesterCard);
        content.add(sessionCardsPanel);

        // --- Statistics Metric Cards ---
        Map<String, Integer> stats = DashboardDAO.getDashboardStatistics();

        totalUsersValLabel = new JLabel(String.valueOf(stats.getOrDefault("totalUsers", 0)));
        undergraduatesValLabel = new JLabel(String.valueOf(stats.getOrDefault("totalUndergraduates", 0)));
        lecturersValLabel = new JLabel(String.valueOf(stats.getOrDefault("totalLecturers", 0)));
        totalCoursesValLabel = new JLabel(String.valueOf(stats.getOrDefault("totalCourses", 0)));
        activeNoticesValLabel = new JLabel(String.valueOf(stats.getOrDefault("totalNotices", 0)));
        timetablesValLabel = new JLabel(String.valueOf(stats.getOrDefault("totalTimetables", 0)));
        techOfficersValLabel = new JLabel(String.valueOf(stats.getOrDefault("totalTechnicalOfficers", 0)));

        // Row 1
        JPanel metricsPanel1 = new JPanel(new GridLayout(1, 3, 20, 0));
        metricsPanel1.setOpaque(false);
        metricsPanel1.add(createMetricCard("Total Users", totalUsersValLabel, "Updated real-time", new Color(225, 238, 255), "src/main/resources/images/admin-icons/card_users.png"));
        metricsPanel1.add(createMetricCard("Undergraduates", undergraduatesValLabel, "Updated real-time", new Color(230, 248, 234), "src/main/resources/images/admin-icons/card_undergrad.png"));
        metricsPanel1.add(createMetricCard("Lecturers", lecturersValLabel, "Updated real-time", new Color(254, 239, 227), "src/main/resources/images/admin-icons/card_lecturer.png"));

        // Row 2
        JPanel metricsPanel2 = new JPanel(new GridLayout(1, 3, 20, 0));
        metricsPanel2.setOpaque(false);
        metricsPanel2.setBorder(new EmptyBorder(20, 0, 0, 0));
        metricsPanel2.add(createMetricCard("Total Courses", totalCoursesValLabel, "Updated real-time", new Color(255, 235, 238), "src/main/resources/images/admin-icons/card_courses.png"));
        metricsPanel2.add(createMetricCard("Active Notices", activeNoticesValLabel, "Updated real-time", new Color(232, 244, 253), "src/main/resources/images/admin-icons/card_notices.png"));
        metricsPanel2.add(createMetricCard("Timetables", timetablesValLabel, "Updated real-time", new Color(232, 245, 254), "src/main/resources/images/admin-icons/card_timetables.png"));

        // Row 3
        JPanel metricsPanel3 = new JPanel(new GridLayout(1, 3, 20, 0));
        metricsPanel3.setOpaque(false);
        metricsPanel3.setBorder(new EmptyBorder(20, 0, 20, 0));
        metricsPanel3.add(createMetricCard("Technical Officers", techOfficersValLabel, "Updated real-time", new Color(246, 234, 255), "src/main/resources/images/admin-icons/card_tech.png"));

        // Add dummy panels to keep the same width for the 1 card in a 3-column grid
        JPanel dummyPanel1 = new JPanel();
        dummyPanel1.setOpaque(false);
        metricsPanel3.add(dummyPanel1);

        JPanel dummyPanel2 = new JPanel();
        dummyPanel2.setOpaque(false);
        metricsPanel3.add(dummyPanel2);

        content.add(metricsPanel1);
        content.add(metricsPanel2);
        content.add(metricsPanel3);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(new Color(245, 247, 250));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());

        add(scrollPane, BorderLayout.CENTER);

        // Auto-refresh when dashboard becomes visible
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                refreshData();
            }
        });
    }

    public void setLoggedUsername(String newUsername) {
        if (newUsername != null && !newUsername.trim().isEmpty()) {
            this.username = newUsername;
            if (welcomeLabel != null) {
                welcomeLabel.setText("Welcome, " + this.username + "!");
            }
        }
    }

    /**
     * Refreshes the active academic session (academic year & semester) from settings.
     */
    public void refreshAcademicSession() {
        try {
            String year = SettingsDAO.getSetting("academic.year", "2024/2025");
            String sem = SettingsDAO.getSetting("academic.semester", "Semester 1");
            if (academicYearValLabel != null) {
                academicYearValLabel.setText(year);
            }
            if (academicSemesterValLabel != null) {
                academicSemesterValLabel.setText(sem);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Refreshes both academic session info and real-time dashboard statistics.
     */
    public void refreshData() {
        refreshAcademicSession();
        try {
            Map<String, Integer> stats = DashboardDAO.getDashboardStatistics();
            if (totalUsersValLabel != null) totalUsersValLabel.setText(String.valueOf(stats.getOrDefault("totalUsers", 0)));
            if (undergraduatesValLabel != null) undergraduatesValLabel.setText(String.valueOf(stats.getOrDefault("totalUndergraduates", 0)));
            if (lecturersValLabel != null) lecturersValLabel.setText(String.valueOf(stats.getOrDefault("totalLecturers", 0)));
            if (totalCoursesValLabel != null) totalCoursesValLabel.setText(String.valueOf(stats.getOrDefault("totalCourses", 0)));
            if (activeNoticesValLabel != null) activeNoticesValLabel.setText(String.valueOf(stats.getOrDefault("totalNotices", 0)));
            if (timetablesValLabel != null) timetablesValLabel.setText(String.valueOf(stats.getOrDefault("totalTimetables", 0)));
            if (techOfficersValLabel != null) techOfficersValLabel.setText(String.valueOf(stats.getOrDefault("totalTechnicalOfficers", 0)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        revalidate();
        repaint();
    }

    /**
     * Creates a styled, colorful session card for Academic Year or Semester.
     */
    private JPanel createSessionCard(
            String title,
            JLabel valLbl,
            String subtext,
            String badgeText,
            Color accentColor,
            Color iconBgColor,
            Color badgeBgColor,
            Color badgeTextColor,
            String iconPath
    ) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Card background with rounded shape
                Shape roundedCard = new RoundRectangle2D.Float(0, 0, w, h, 16, 16);
                g2.setColor(Color.WHITE);
                g2.fill(roundedCard);

                // Clip to the card's rounded border to cleanly draw the left accent bar
                Shape oldClip = g2.getClip();
                g2.clip(roundedCard);
                g2.setColor(accentColor);
                g2.fillRect(0, 0, 6, h);
                g2.setClip(oldClip);

                // Subtle card border
                g2.setColor(new Color(228, 233, 242));
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 16, 16));

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(16, 0));
        card.setBorder(new EmptyBorder(16, 22, 16, 20));

        // Icon Box (West)
        JLabel iconLabel = new JLabel("", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBgColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        loadIconIntoLabel(iconLabel, iconPath, 30, 30);
        iconLabel.setOpaque(false);
        iconLabel.setPreferredSize(new Dimension(56, 56));

        // Center Content Block
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(accentColor);

        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLbl.setForeground(new Color(23, 32, 48));

        JLabel subLbl = new JLabel(subtext);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLbl.setForeground(new Color(115, 125, 138));

        textPanel.add(titleLbl);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(valLbl);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(subLbl);

        // East Status Badge Pill
        JPanel badgeWrapper = new JPanel(new GridBagLayout());
        badgeWrapper.setOpaque(false);

        JLabel badgeLbl = new JLabel(badgeText) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(badgeBgColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        badgeLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badgeLbl.setForeground(badgeTextColor);
        badgeLbl.setBorder(new EmptyBorder(5, 12, 5, 12));
        badgeLbl.setOpaque(false);

        badgeWrapper.add(badgeLbl);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        card.add(badgeWrapper, BorderLayout.EAST);

        return card;
    }

    /**
     * Creates a standard metric card for dashboard statistics.
     */
    private JPanel createMetricCard(String title, JLabel valLbl, String trend, Color iconBgColor, String iconPath) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(15, 0));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Icon Box
        JLabel iconLabel = new JLabel("", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBgColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        loadIconIntoLabel(iconLabel, iconPath, 28, 28);
        iconLabel.setOpaque(false);
        iconLabel.setPreferredSize(new Dimension(60, 60));

        // Text Info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(new Color(100, 110, 120));

        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valLbl.setForeground(new Color(30, 40, 50));

        JLabel trendLbl = new JLabel(trend);
        trendLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        if (trend.contains("↑") && !trend.contains("0%")) {
            trendLbl.setForeground(new Color(40, 167, 69));
        } else {
            trendLbl.setForeground(Color.GRAY);
        }

        infoPanel.add(titleLbl);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(valLbl);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(trendLbl);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(infoPanel, BorderLayout.CENTER);

        return card;
    }

    /**
     * Loads an icon from file or class resource safely, scaling to target size.
     */
    private void loadIconIntoLabel(JLabel label, String path, int width, int height) {
        try {
            ImageIcon icon = null;
            File file = new File(path);
            if (file.exists()) {
                icon = new ImageIcon(path);
            } else {
                java.net.URL res = getClass().getResource("/" + path.replaceFirst("^src/main/resources/", ""));
                if (res != null) {
                    icon = new ImageIcon(res);
                }
            }

            if (icon != null && icon.getImageLoadStatus() != MediaTracker.ERRORED) {
                Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(scaled));
            } else {
                System.out.println("Could not load icon: " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Scrollable panel that tracks viewport width to ensure components stretch full-width,
     * while allowing natural vertical scrolling.
     */
    private static class ScrollablePanel extends JPanel implements Scrollable {
        public ScrollablePanel() {
            super();
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 64;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /**
     * Sleek, modern scrollbar matching the application's clean design.
     */
    private static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(203, 213, 225);
            this.trackColor = new Color(245, 247, 250);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            btn.setMinimumSize(new Dimension(0, 0));
            btn.setMaximumSize(new Dimension(0, 0));
            return btn;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(185, 198, 214));
            g2.fill(new RoundRectangle2D.Float(thumbBounds.x + 2, thumbBounds.y + 1, thumbBounds.width - 4, thumbBounds.height - 2, 6, 6));
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(241, 245, 249));
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g2.dispose();
        }
    }
}

