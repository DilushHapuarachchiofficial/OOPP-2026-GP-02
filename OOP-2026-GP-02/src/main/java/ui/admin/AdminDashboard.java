package main.java.ui.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class AdminDashboard extends JFrame {
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JButton activeButton;

    public AdminDashboard() {
        setTitle("FAMS - Faculty Academic Management System");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 1. Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(23, 32, 48)); // Dark blue from image
        sidebar.setPreferredSize(new Dimension(250, getHeight()));

        // Logo section
        JPanel logoPanel = new JPanel(new BorderLayout(10, 0));
        logoPanel.setOpaque(false);
        logoPanel.setBorder(new EmptyBorder(25, 20, 30, 20));

        JLabel iconLabel = new JLabel(createScaledIcon("src/main/resources/images/admin-icons/logo.png", 40, 40));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        JLabel title1 = new JLabel("FAMS");
        title1.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title1.setForeground(Color.WHITE);
        JLabel title2 = new JLabel("Faculty Academic");
        title2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        title2.setForeground(new Color(180, 190, 210));
        JLabel title3 = new JLabel("Management System");
        title3.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        title3.setForeground(new Color(180, 190, 210));
        titlePanel.add(title1);
        titlePanel.add(title2);
        titlePanel.add(title3);

        logoPanel.add(iconLabel, BorderLayout.WEST);
        logoPanel.add(titlePanel, BorderLayout.CENTER);
        sidebar.add(logoPanel);

        // Sidebar Menus
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(new Color(245, 247, 250));

        addSidebarButton(sidebar, "Dashboard", "src/main/resources/images/admin-icons/dashboard.png", new DashboardHomePanel(), true);
        addSidebarButton(sidebar, "User Management", "src/main/resources/images/admin-icons/users.png", new UserManagementPanel(), false);
        addSidebarButton(sidebar, "Course Management", "src/main/resources/images/admin-icons/courses.png", new CourseManagementPanel(), false);
        addSidebarButton(sidebar, "Notices", "src/main/resources/images/admin-icons/notices.png", new NoticeManagementPanel(), false);
        addSidebarButton(sidebar, "Timetables", "src/main/resources/images/admin-icons/timetables.png", new TimetableManagementPanel(), false);
        addSidebarButton(sidebar, "Reports", "src/main/resources/images/admin-icons/reports.png", new ReportsPanel(), false);
        addSidebarButton(sidebar, "Profile", "src/main/resources/images/admin-icons/profile.png", new AdminProfilePanel(), false);
        addSidebarButton(sidebar, "Settings", "src/main/resources/images/admin-icons/settings.png", new SettingsPanel(), false);

        sidebar.add(Box.createVerticalGlue());

        // Logout button
        JButton logoutBtn = createSidebarButton("Logout", "src/main/resources/images/admin-icons/logout.png");
        logoutBtn.setForeground(new Color(255, 99, 71));
        logoutBtn.addActionListener(e -> {
            dispose();
            // new LoginScreen().setVisible(true);
        });
        JPanel logoutPanel = new JPanel(new BorderLayout());
        logoutPanel.setOpaque(false);
        logoutPanel.setBorder(new EmptyBorder(10, 10, 20, 10));
        logoutPanel.add(logoutBtn);

        // Divider line for logout
        JPanel divider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(255, 255, 255, 30));
                g.drawLine(20, getHeight()/2, getWidth()-20, getHeight()/2);
            }
        };
        divider.setOpaque(false);
        divider.setMaximumSize(new Dimension(250, 20));
        sidebar.add(divider);
        sidebar.add(logoutPanel);

        // 2. Main Content Area
        JPanel mainContentPanel = new JPanel(new BorderLayout());

        // Top Header Bar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                new EmptyBorder(15, 25, 15, 25)
        ));

        // Header Left: Hamburger & Title
        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        headerLeft.setOpaque(false);

        JLabel hamburger = new JLabel(createScaledIcon("src/main/resources/images/admin-icons/hamburger.png", 24, 24));
        hamburger.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        JLabel pageTitle = new JLabel("Admin Dashboard");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        pageTitle.setForeground(new Color(23, 32, 48));
        JLabel pageSubtitle = new JLabel("Welcome back! Manage your academic environment efficiently.");
        pageSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pageSubtitle.setForeground(new Color(120, 130, 140));
        titleBlock.add(pageTitle);
        titleBlock.add(pageSubtitle);

        headerLeft.add(hamburger);
        headerLeft.add(titleBlock);

        // Header Right: Profile Only
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        headerRight.setOpaque(false);

        // Profile Info
        JPanel profileInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        profileInfo.setOpaque(false);

        // Custom drawn avatar with letter "A"
        JLabel profileIcon = new JLabel("A", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 100, 220)); // Blue background
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        };
        profileIcon.setFont(new Font("Segoe UI", Font.BOLD, 18));
        profileIcon.setForeground(Color.WHITE);
        profileIcon.setPreferredSize(new Dimension(36, 36));

        JPanel profileTexts = new JPanel();
        profileTexts.setLayout(new BoxLayout(profileTexts, BoxLayout.Y_AXIS));
        profileTexts.setOpaque(false);
        JLabel pName = new JLabel("Admin User");
        pName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel pRole = new JLabel("Administrator");
        pRole.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pRole.setForeground(Color.GRAY);
        profileTexts.add(pName);
        profileTexts.add(pRole);

        JLabel dropDownIcon = new JLabel(createScaledIcon("src/main/resources/images/admin-icons/dropdown.png", 16, 16));
        dropDownIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Profile Popup Menu
        JPopupMenu profileMenu = new JPopupMenu();
        profileMenu.setBackground(Color.WHITE);
        profileMenu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230), 1, true),
                BorderFactory.createEmptyBorder(10, 5, 10, 5)
        ));

        JMenuItem settingsItem = new JMenuItem("  Profile Settings");
        JMenuItem logoutItem = new JMenuItem("  Logout");

        logoutItem.addActionListener(e -> {
            this.dispose();
            SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
        });

        Font menuFont = new Font("Segoe UI", Font.PLAIN, 14);

        for (JMenuItem item : new JMenuItem[]{settingsItem, logoutItem}) {
            item.setFont(menuFont);
            item.setBackground(Color.WHITE);
            item.setForeground(new Color(30, 40, 50));
            item.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 40));
            item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        logoutItem.setForeground(new Color(220, 53, 69)); // Red color for logout
        logoutItem.addActionListener(e -> {
            dispose();
            // new LoginScreen().setVisible(true);
        });

        profileMenu.add(settingsItem);
        profileMenu.addSeparator();
        profileMenu.add(logoutItem);

        dropDownIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                profileMenu.show(dropDownIcon, -profileMenu.getPreferredSize().width + dropDownIcon.getWidth(), dropDownIcon.getHeight() + 10);
            }
        });

        profileInfo.add(profileIcon);
        profileInfo.add(profileTexts);
        profileInfo.add(dropDownIcon);

        headerRight.add(profileInfo);

        headerPanel.add(headerLeft, BorderLayout.WEST);
        headerPanel.add(headerRight, BorderLayout.EAST);

        mainContentPanel.add(headerPanel, BorderLayout.NORTH);
        mainContentPanel.add(cardPanel, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(mainContentPanel, BorderLayout.CENTER);

        cardLayout.show(cardPanel, "Dashboard");
    }

    private void addSidebarButton(JPanel sidebar, String title, String iconStr, JPanel contentPanel, boolean isDefault) {
        JButton btn = createSidebarButton(title, iconStr);
        if (isDefault) {
            setActiveButtonStyle(btn);
            activeButton = btn;
        }

        btn.addActionListener(e -> {
            if (activeButton != null) {
                setInactiveButtonStyle(activeButton);
            }
            setActiveButtonStyle(btn);
            activeButton = btn;
            cardLayout.show(cardPanel, title);
        });

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(5, 10, 5, 10));
        wrap.add(btn);
        wrap.setMaximumSize(new Dimension(250, 55));

        sidebar.add(wrap);
        cardPanel.add(contentPanel, title);
    }

    private JButton createSidebarButton(String title, String iconPath) {
        JButton btn = new JButton("  " + title) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getBackground() != null && getBackground().getAlpha() > 0) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };

        btn.setIcon(createScaledIcon(iconPath, 20, 20));

        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(new Color(200, 210, 220));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBackground(new Color(0, 0, 0, 0)); // Transparent by default
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(12, 20, 12, 20));
        btn.setIconTextGap(15);
        return btn;
    }

    private void setActiveButtonStyle(JButton btn) {
        btn.setBackground(new Color(30, 100, 220)); // Blue background
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }

    private void setInactiveButtonStyle(JButton btn) {
        btn.setBackground(new Color(0, 0, 0, 0)); // Fully transparent
        btn.setForeground(new Color(200, 210, 220));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    private JPanel createPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        JLabel header = new JLabel(title);
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(header, BorderLayout.NORTH);
        return panel;
    }

    private ImageIcon createScaledIcon(String path, int width, int height) {
        try {
            ImageIcon originalIcon = new ImageIcon(path);
            if (originalIcon.getImageLoadStatus() == java.awt.MediaTracker.ERRORED) {
                System.out.println("Could not load icon: " + path);
                return null;
            }
            Image scaledImg = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
