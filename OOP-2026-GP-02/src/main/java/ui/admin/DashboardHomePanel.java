package main.java.ui.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import main.java.dao.admin.DashboardDAO;

public class DashboardHomePanel extends JPanel {

    public DashboardHomePanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        Map<String, Integer> stats = DashboardDAO.getDashboardStatistics();

        // 1. Metric Cards (3 rows of 3)
        JPanel metricsPanel1 = new JPanel(new GridLayout(1, 3, 20, 0));
        metricsPanel1.setOpaque(false);
        metricsPanel1.add(createMetricCard("Total Users", String.valueOf(stats.getOrDefault("totalUsers", 0)), "Updated real-time", new Color(225, 238, 255), "src/main/resources/images/admin-icons/card_users.png"));
        metricsPanel1.add(createMetricCard("Undergraduates", String.valueOf(stats.getOrDefault("totalUndergraduates", 0)), "Updated real-time", new Color(230, 248, 234), "src/main/resources/images/admin-icons/card_undergrad.png"));
        metricsPanel1.add(createMetricCard("Lecturers", String.valueOf(stats.getOrDefault("totalLecturers", 0)), "Updated real-time", new Color(254, 239, 227), "src/main/resources/images/admin-icons/card_lecturer.png"));

        JPanel metricsPanel2 = new JPanel(new GridLayout(1, 3, 20, 0));
        metricsPanel2.setOpaque(false);
        metricsPanel2.setBorder(new EmptyBorder(20, 0, 0, 0));
        metricsPanel2.add(createMetricCard("Total Courses", String.valueOf(stats.getOrDefault("totalCourses", 0)), "Updated real-time", new Color(255, 235, 238), "src/main/resources/images/admin-icons/card_courses.png"));
        metricsPanel2.add(createMetricCard("Active Notices", String.valueOf(stats.getOrDefault("totalNotices", 0)), "Updated real-time", new Color(232, 244, 253), "src/main/resources/images/admin-icons/card_notices.png"));
        metricsPanel2.add(createMetricCard("Timetables", String.valueOf(stats.getOrDefault("totalTimetables", 0)), "Updated real-time", new Color(232, 245, 254), "src/main/resources/images/admin-icons/card_timetables.png"));
        
        JPanel metricsPanel3 = new JPanel(new GridLayout(1, 3, 20, 0));
        metricsPanel3.setOpaque(false);
        metricsPanel3.setBorder(new EmptyBorder(20, 0, 20, 0));
        metricsPanel3.add(createMetricCard("Technical Officers", String.valueOf(stats.getOrDefault("totalTechnicalOfficers", 0)), "Updated real-time", new Color(246, 234, 255), "src/main/resources/images/admin-icons/card_tech.png"));
        
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

        // Add some extra right margin to the content to ensure it doesn't hug the right edge
        content.setBorder(new EmptyBorder(0, 0, 0, 25));

        add(content, BorderLayout.NORTH);
    }

    private JPanel createMetricCard(String title, String value, String trend, Color iconBgColor, String iconPath) {
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
        
        try {
            ImageIcon originalIcon = new ImageIcon(iconPath);
            if (originalIcon.getImageLoadStatus() == java.awt.MediaTracker.ERRORED) {
                System.out.println("Could not load icon: " + iconPath);
            } else {
                Image scaledImg = originalIcon.getImage().getScaledInstance(28, 28, Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(scaledImg));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        iconLabel.setOpaque(false);
        iconLabel.setPreferredSize(new Dimension(60, 60));

        // Text Info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(new Color(100, 110, 120));

        JLabel valLbl = new JLabel(value);
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

    // tables removed
}
