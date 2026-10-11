package main.java.ui.Lecturer;

import main.java.model.Lecturer.LecturerDashboardDAO;
import main.java.model.Lecturer.LecturerDashboardModel;
import main.java.model.UserSession;
import main.java.ui.LoginUI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * LecturerDashboard.java
 * TecFAMS – Faculty of Technology Academic Management System
 *
 * Professional Lecturer Dashboard Page (Part 2: Real Database Integration).
 *
 * Key Capabilities:
 * - Asynchronous JDBC data retrieval using SwingWorker (non-blocking EDT).
 * - Real data binding for authenticated lecturer profile, assigned courses count,
 *   uploaded materials count, and official faculty notices.
 * - Distinct, robust UI state handling:
 *     1. LOADING: Pulsing indicator while background worker queries MySQL.
 *     2. SUCCESS: Real database statistics, cards, and recent notices.
 *     3. ZERO RESULTS: Legitimate count of 0 or empty notice board without fake fallbacks.
 *     4. DATABASE ERROR: Styled diagnostic panel with "Retry Connection" action.
 *     5. UNAUTHENTICATED: Security prompt when no active lecturer session exists.
 * - Seamless integration with Navbar.java under a shared container.
 * - Responsive Swing layout (BorderLayout, GridLayout, BoxLayout, JScrollPane).
 */
public class LecturerDashboard extends JPanel {

    // =========================================================================
    // COLOR PALETTE (Strictly TecFAMS Design Standard)
    // =========================================================================
    public static final Color COLOR_DEEP_NAVY      = new Color(8, 47, 90);    // #082F5A - Brand & primary headings
    public static final Color COLOR_PRO_BLUE       = new Color(11, 79, 156);  // #0B4F9C - Primary accents & active indicators
    public static final Color COLOR_ACCENT_BLUE    = new Color(40, 120, 208); // #2878D0 - Highlights & secondary accents
    public static final Color COLOR_WHITE          = new Color(255, 255, 255);// #FFFFFF - Card backgrounds
    public static final Color COLOR_PAGE_BG        = new Color(244, 247, 251);// #F4F7FB - Dashboard surface background
    public static final Color COLOR_BORDER         = new Color(220, 228, 238);// #DCE4EE - Card borders & dividers
    public static final Color COLOR_TEXT_SECONDARY = new Color(100, 116, 139);// #64748B - Muted subtitles & labels
    public static final Color COLOR_TEXT_MUTED     = new Color(148, 163, 184);// #94A3B8 - Explanatory helper text
    public static final Color COLOR_NOTIF_ACCENT   = new Color(255, 107, 0);  // #FF6B00 - Notification badges
    public static final Color COLOR_CARD_HOVER     = new Color(250, 252, 255);// Subtle hover highlight
    public static final Color COLOR_DANGER         = new Color(220, 38, 38);  // #DC2626 - Error & warning alerts
    public static final Color COLOR_DANGER_BG      = new Color(254, 242, 242);// #FEF2F2 - Soft error background
    public static final Color COLOR_SUCCESS_GREEN  = new Color(16, 185, 129);// #10B981 - Success pill

    // =========================================================================
    // TYPOGRAPHY (Segoe UI Hierarchy)
    // =========================================================================
    public static final Font FONT_WELCOME_TITLE    = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_WELCOME_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_DEPARTMENT_BADGE = new Font("Segoe UI Semibold", Font.BOLD, 11);
    public static final Font FONT_CARD_LABEL       = new Font("Segoe UI Semibold", Font.PLAIN, 13);
    public static final Font FONT_CARD_COUNT       = new Font("Segoe UI", Font.BOLD, 32);
    public static final Font FONT_CARD_SUBTITLE    = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_SECTION_TITLE    = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SECTION_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_NOTICE_TITLE     = new Font("Segoe UI Semibold", Font.PLAIN, 13);
    public static final Font FONT_NOTICE_DESC      = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_NOTICE_DATE      = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_EMPTY_STATE      = new Font("Segoe UI Semibold", Font.PLAIN, 13);
    public static final Font FONT_EMPTY_SUBTEXT    = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_STATE_TITLE      = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_STATE_DESC       = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BUTTON           = new Font("Segoe UI Semibold", Font.BOLD, 13);

    // =========================================================================
    // CARD VIEW CONSTANTS (State switching)
    // =========================================================================
    private static final String VIEW_LOADING         = "VIEW_LOADING";
    private static final String VIEW_SUCCESS         = "VIEW_SUCCESS";
    private static final String VIEW_ERROR           = "VIEW_ERROR";
    private static final String VIEW_UNAUTHENTICATED = "VIEW_UNAUTHENTICATED";

    // Layout Card Manager
    private CardLayout cardLayout;
    private JPanel stateContainer;

    // Success View Components
    private JLabel welcomeTitleLabel;
    private JLabel departmentBadgeLabel;
    private JLabel coursesCountLabel;
    private JLabel materialsCountLabel;
    private JPanel noticesContainer;

    // Error View Components
    private JLabel errorTitleLabel;
    private JTextArea errorDetailsArea;

    // Background Worker Tracking (prevents overlapping tasks)
    private SwingWorker<LecturerDashboardModel, Void> currentWorker;

    // Lecturer Identity Target
    private int targetLecturerId = -1;
    private String targetUsername = null;

    // =========================================================================
    // TECHNOLOGY BACKGROUND IMAGE MANAGEMENT
    // =========================================================================
    private static BufferedImage bgImage = null;
    private static boolean bgImageChecked = false;

    private static synchronized BufferedImage getDashboardBackgroundImage() {
        if (!bgImageChecked) {
            bgImageChecked = true;
            String[] candidatePaths = {
                "src/main/resources/images/dashboard_tech_bg.jpg",
                "resources/images/dashboard_tech_bg.jpg",
                "images/dashboard_tech_bg.jpg",
                "OOP-2026-GP-02/src/main/resources/images/dashboard_tech_bg.jpg"
            };
            for (String p : candidatePaths) {
                File f = new File(p);
                if (f.exists()) {
                    try {
                        bgImage = ImageIO.read(f);
                        if (bgImage != null) break;
                    } catch (Exception ignored) {}
                }
            }
            if (bgImage == null) {
                try {
                    InputStream is = LecturerDashboard.class.getResourceAsStream("/images/dashboard_tech_bg.jpg");
                    if (is == null) {
                        is = LecturerDashboard.class.getResourceAsStream("/dashboard_tech_bg.jpg");
                    }
                    if (is != null) {
                        bgImage = ImageIO.read(is);
                        is.close();
                    }
                } catch (Exception ignored) {}
            }
        }
        return bgImage;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();

        BufferedImage bg = getDashboardBackgroundImage();
        if (bg != null) {
            // Draw technology background image scaled to cover panel
            g2.drawImage(bg, 0, 0, w, h, null);

            // Refined translucent gradient overlay keeping all cards and typography crystal clear
            GradientPaint overlay = new GradientPaint(
                0, 0, new Color(11, 31, 58, 140),
                0, h, new Color(244, 247, 251, 220)
            );
            g2.setPaint(overlay);
            g2.fillRect(0, 0, w, h);
        } else {
            // Fallback tech gradient if image is not present
            GradientPaint fallback = new GradientPaint(
                0, 0, new Color(236, 242, 250),
                w, h, COLOR_PAGE_BG
            );
            g2.setPaint(fallback);
            g2.fillRect(0, 0, w, h);
        }

        g2.dispose();
    }

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================

    /**
     * Default constructor: Resolves the currently authenticated user from UserSession.
     * If no active session exists, displays the unauthenticated state.
     */
    public LecturerDashboard() {
        UserSession session = UserSession.getCurrentSession();
        if (session != null && "Lecturer".equalsIgnoreCase(session.getRole())) {
            this.targetLecturerId = session.getUserId();
            this.targetUsername = session.getUsername();
        }
        initializeUI();
        refreshDashboard();
    }

    /**
     * Constructs the dashboard for a specific authenticated lecturer ID.
     */
    public LecturerDashboard(int lecturerId) {
        this.targetLecturerId = lecturerId;
        initializeUI();
        refreshDashboard();
    }

    /**
     * Constructs the dashboard for a specific authenticated username.
     */
    public LecturerDashboard(String username) {
        this.targetUsername = username;
        initializeUI();
        refreshDashboard();
    }

    /**
     * Constructs the dashboard and directly displays a pre-populated data model.
     */
    public LecturerDashboard(LecturerDashboardModel model) {
        initializeUI();
        if (model != null) {
            applyModelToUI(model);
            cardLayout.show(stateContainer, VIEW_SUCCESS);
        } else {
            refreshDashboard();
        }
    }

    // =========================================================================
    // UI INITIALIZATION & ROOT CARD LAYOUT
    // =========================================================================
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(COLOR_PAGE_BG);

        cardLayout = new CardLayout();
        stateContainer = new JPanel(cardLayout);
        stateContainer.setOpaque(false);

        // 1. Loading View
        stateContainer.add(createLoadingView(), VIEW_LOADING);

        // 2. Success View
        stateContainer.add(createSuccessView(), VIEW_SUCCESS);

        // 3. Database Error View
        stateContainer.add(createErrorView(), VIEW_ERROR);

        // 4. Unauthenticated View
        stateContainer.add(createUnauthenticatedView(), VIEW_UNAUTHENTICATED);

        add(stateContainer, BorderLayout.CENTER);
    }

    // =========================================================================
    // BACKGROUND DATA RETRIEVAL (SwingWorker)
    // =========================================================================

    /**
     * Initiates asynchronous database retrieval without blocking the Event Dispatch Thread.
     */
    public void refreshDashboard() {
        // Cancel any currently running background worker
        if (currentWorker != null && !currentWorker.isDone()) {
            currentWorker.cancel(true);
        }

        // Check if authenticated identity is present
        int resolvedId = resolveLecturerId();
        if (resolvedId <= 0 && targetUsername == null) {
            cardLayout.show(stateContainer, VIEW_UNAUTHENTICATED);
            return;
        }

        // Show Loading View
        cardLayout.show(stateContainer, VIEW_LOADING);

        // Launch SwingWorker
        currentWorker = new SwingWorker<LecturerDashboardModel, Void>() {
            @Override
            protected LecturerDashboardModel doInBackground() throws Exception {
                if (targetUsername != null && targetLecturerId <= 0) {
                    return LecturerDashboardDAO.loadDashboardDataByUsername(targetUsername);
                } else {
                    return LecturerDashboardDAO.loadDashboardData(targetLecturerId);
                }
            }

            @Override
            protected void done() {
                if (isCancelled()) return;
                int currentId = resolveLecturerId();
                if (currentId <= 0 && targetUsername == null) {
                    cardLayout.show(stateContainer, VIEW_UNAUTHENTICATED);
                    return;
                }
                try {
                    LecturerDashboardModel model = get();
                    if (targetLecturerId > 0 && model.getLecturerId() != targetLecturerId) {
                        return; // Discard stale background result for different lecturer
                    }
                    applyModelToUI(model);
                    cardLayout.show(stateContainer, VIEW_SUCCESS);
                } catch (Exception e) {
                    Throwable cause = (e.getCause() != null) ? e.getCause() : e;
                    showErrorState(cause.getMessage());
                }
            }
        };

        currentWorker.execute();
    }

    /**
     * Resolves the target lecturer ID from explicit parameter or active UserSession.
     */
    private int resolveLecturerId() {
        if (targetLecturerId > 0) {
            return targetLecturerId;
        }
        UserSession session = UserSession.getCurrentSession();
        if (session != null && "Lecturer".equalsIgnoreCase(session.getRole())) {
            this.targetLecturerId = session.getUserId();
            return this.targetLecturerId;
        }
        return -1;
    }

    /**
     * Switches to the Error View and displays the exact database diagnostic message.
     */
    private void showErrorState(String message) {
        String displayMsg;
        if (message == null || message.trim().isEmpty()) {
            displayMsg = "Unable to establish connection to TecFAMS MySQL database. Please verify that MySQL is running on localhost:3306.";
        } else if (message.toLowerCase().contains("password") || message.toLowerCase().contains("access denied")) {
            displayMsg = "Database access denied. Please verify MySQL configuration and user privileges.";
        } else {
            displayMsg = message;
        }
        errorDetailsArea.setText(displayMsg);
        cardLayout.show(stateContainer, VIEW_ERROR);
    }

    // =========================================================================
    // SUCCESS VIEW BUILDER
    // =========================================================================
    private JScrollPane createSuccessView() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(24, 32, 32, 32));

        // Section A: Welcome Section
        contentPanel.add(createWelcomeSection());
        contentPanel.add(Box.createRigidArea(new Dimension(0, 24)));

        // Section B: Summary Cards Section
        contentPanel.add(createSummaryCardsSection());
        contentPanel.add(Box.createRigidArea(new Dimension(0, 28)));

        // Section C: Recent Notices Section
        contentPanel.add(createRecentNoticesSection());

        // Make Scrollable
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        return scrollPane;
    }

    // =========================================================================
    // SECTION A: WELCOME SECTION
    // =========================================================================
    private JPanel createWelcomeSection() {
        JPanel welcomeCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Frosted card background
                g2.setColor(new Color(255, 255, 255, 245));
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 16, 16));

                // Left 4px technology gradient stripe (Deep Navy to Cyber Cyan)
                GradientPaint leftStripe = new GradientPaint(
                        0, 0, new Color(11, 79, 156),
                        0, h, new Color(6, 182, 212)
                );
                g2.setPaint(leftStripe);
                g2.fill(new RoundRectangle2D.Float(0, 0, 4, h - 1, 4, 4));

                // Border
                g2.setColor(new Color(218, 228, 242));
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 16, 16));

                g2.dispose();
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };

        welcomeCard.setLayout(new BorderLayout(16, 0));
        welcomeCard.setOpaque(false);
        welcomeCard.setBorder(new EmptyBorder(18, 24, 18, 24));
        welcomeCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Text Content
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel statusTag = new JLabel("● SYSTEM ACTIVE • FACULTY OF TECHNOLOGY ACADEMIC PORTAL");
        statusTag.setFont(new Font("Segoe UI Semibold", Font.BOLD, 10));
        statusTag.setForeground(new Color(2, 132, 199)); // #0284C7
        statusTag.setAlignmentX(Component.LEFT_ALIGNMENT);

        welcomeTitleLabel = new JLabel("Welcome back, —");
        welcomeTitleLabel.setFont(FONT_WELCOME_TITLE);
        welcomeTitleLabel.setForeground(COLOR_DEEP_NAVY);
        welcomeTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel welcomeSubtitleLabel = new JLabel("Overview of your active semester modules, student submissions, and resources.");
        welcomeSubtitleLabel.setFont(FONT_WELCOME_SUBTITLE);
        welcomeSubtitleLabel.setForeground(COLOR_TEXT_SECONDARY);
        welcomeSubtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(statusTag);
        textPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        textPanel.add(welcomeTitleLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        textPanel.add(welcomeSubtitleLabel);

        // Right Department / Designation Badge
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        badgePanel.setOpaque(false);

        departmentBadgeLabel = new JLabel("Faculty of Technology") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(238, 245, 255));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.setColor(new Color(186, 216, 255));
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        departmentBadgeLabel.setFont(FONT_DEPARTMENT_BADGE);
        departmentBadgeLabel.setForeground(new Color(11, 79, 156));
        departmentBadgeLabel.setBorder(new EmptyBorder(7, 16, 7, 16));

        badgePanel.add(departmentBadgeLabel);

        welcomeCard.add(textPanel, BorderLayout.CENTER);
        welcomeCard.add(badgePanel, BorderLayout.EAST);

        return welcomeCard;
    }

    // =========================================================================
    // SECTION B: SUMMARY CARDS SECTION (Technology-Themed Visuals)
    // =========================================================================
    private JPanel createSummaryCardsSection() {
        JPanel container = new JPanel(new GridLayout(1, 2, 22, 0));
        container.setOpaque(false);
        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, 145));
        container.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Card 1: Assigned Courses (Tech Blue & Cyber Cyan theme)
        coursesCountLabel = new JLabel("—");
        JPanel card1 = createSummaryCard(
                "Assigned Courses",
                coursesCountLabel,
                "Active semester teaching modules",
                DashboardVectorIcon.IconType.TECH_COURSE_CIRCUIT,
                new Color(11, 79, 156),  // #0B4F9C - Pro Blue
                new Color(6, 182, 212)    // #06B6D4 - Cyber Cyan
        );

        // Card 2: Uploaded Materials (Electric Indigo & Tech Emerald theme)
        materialsCountLabel = new JLabel("—");
        JPanel card2 = createSummaryCard(
                "Uploaded Materials",
                materialsCountLabel,
                "Cloud learning resources & guides",
                DashboardVectorIcon.IconType.TECH_CLOUD_STORAGE,
                new Color(79, 70, 229),  // #4F46E5 - Electric Indigo
                new Color(16, 185, 129)   // #10B981 - Tech Emerald
        );

        container.add(card1);
        container.add(card2);

        return container;
    }

    private JPanel createSummaryCard(String labelText, JLabel countLabel, String subtitleText,
                                     DashboardVectorIcon.IconType iconType,
                                     Color primaryColor, Color secondaryColor) {
        final boolean[] isHovered = {false};

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Frosted card background
                g2.setColor(isHovered[0] ? new Color(255, 255, 255, 255) : new Color(255, 255, 255, 245));
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 16, 16));

                // Top 3px technology gradient accent line
                GradientPaint topStripe = new GradientPaint(
                        0, 0, primaryColor,
                        w, 0, secondaryColor
                );
                g2.setPaint(topStripe);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, 3, 16, 16));

                // Border: subtle standard or illuminated on hover
                g2.setColor(isHovered[0] ? primaryColor : new Color(220, 228, 238, 200));
                g2.setStroke(new BasicStroke(isHovered[0] ? 1.8f : 1.0f));
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 16, 16));

                g2.dispose();
            }
        };

        card.setLayout(new BorderLayout(16, 0));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover Effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered[0] = true;
                card.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered[0] = false;
                card.repaint();
            }
        });

        // Left Technology Icon Squircle Container
        JPanel iconContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Technology dual-color gradient squircle fill
                GradientPaint gp = new GradientPaint(
                        0, 0, primaryColor,
                        w, h, secondaryColor
                );
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(2, 2, w - 4, h - 4, 18, 18));

                // Outer glowing halo ring
                g2.setColor(new Color(primaryColor.getRed(), primaryColor.getGreen(), primaryColor.getBlue(), isHovered[0] ? 150 : 70));
                g2.setStroke(new BasicStroke(isHovered[0] ? 2.0f : 1.2f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 18, 18));

                // Subtle circuit corner notch
                g2.setColor(new Color(255, 255, 255, 80));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawLine(7, 7, 13, 7);
                g2.drawLine(7, 7, 7, 13);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconContainer.setLayout(new GridBagLayout());
        iconContainer.setPreferredSize(new Dimension(54, 54));
        iconContainer.setMinimumSize(new Dimension(54, 54));
        iconContainer.setMaximumSize(new Dimension(54, 54));
        iconContainer.setOpaque(false);

        // Vector icon in crisp white
        JLabel iconLabel = new JLabel(new DashboardVectorIcon(iconType, 26, 26, COLOR_WHITE));
        iconContainer.add(iconLabel);

        // Center Content Panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(labelText);
        titleLabel.setFont(FONT_CARD_LABEL);
        titleLabel.setForeground(COLOR_TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        countLabel.setFont(FONT_CARD_COUNT);
        countLabel.setForeground(COLOR_DEEP_NAVY);
        countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLabel = new JLabel(subtitleText);
        subLabel.setFont(FONT_CARD_SUBTITLE);
        subLabel.setForeground(COLOR_TEXT_MUTED);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(titleLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        textPanel.add(countLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        textPanel.add(subLabel);

        card.add(iconContainer, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    // =========================================================================
    // SECTION C: RECENT NOTICES SECTION
    // =========================================================================
    private JPanel createRecentNoticesSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sectionTitle = new JLabel("Recent Notices");
        sectionTitle.setFont(FONT_SECTION_TITLE);
        sectionTitle.setForeground(COLOR_DEEP_NAVY);

        JLabel sectionSubtitle = new JLabel("Latest announcements for academic staff");
        sectionSubtitle.setFont(FONT_SECTION_SUBTITLE);
        sectionSubtitle.setForeground(COLOR_TEXT_SECONDARY);

        headerPanel.add(sectionTitle, BorderLayout.WEST);
        headerPanel.add(sectionSubtitle, BorderLayout.EAST);

        // Notices List Container
        noticesContainer = new JPanel();
        noticesContainer.setLayout(new BoxLayout(noticesContainer, BoxLayout.Y_AXIS));
        noticesContainer.setOpaque(false);
        noticesContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Default to clean empty state
        renderEmptyNoticesState();

        section.add(headerPanel);
        section.add(Box.createRigidArea(new Dimension(0, 14)));
        section.add(noticesContainer);

        return section;
    }

    private void renderEmptyNoticesState() {
        noticesContainer.removeAll();

        JPanel emptyCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));
                g2.setColor(COLOR_BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));
                g2.dispose();
            }
        };

        emptyCard.setLayout(new GridBagLayout());
        emptyCard.setOpaque(false);
        emptyCard.setBorder(new EmptyBorder(36, 24, 36, 24));
        emptyCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        emptyCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel iconLabel = new JLabel(new DashboardVectorIcon(DashboardVectorIcon.IconType.PIN, 26, 26, COLOR_TEXT_MUTED));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel mainLabel = new JLabel("No recent notices available.");
        mainLabel.setFont(FONT_EMPTY_STATE);
        mainLabel.setForeground(COLOR_TEXT_SECONDARY);
        mainLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("Notices published by the faculty administration will appear here.");
        subLabel.setFont(FONT_EMPTY_SUBTEXT);
        subLabel.setForeground(COLOR_TEXT_MUTED);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(iconLabel);
        inner.add(Box.createRigidArea(new Dimension(0, 10)));
        inner.add(mainLabel);
        inner.add(Box.createRigidArea(new Dimension(0, 4)));
        inner.add(subLabel);

        emptyCard.add(inner);
        noticesContainer.add(emptyCard);
        noticesContainer.revalidate();
        noticesContainer.repaint();
    }

    private void renderNoticeItems(List<LecturerDashboardModel.NoticeItem> items) {
        noticesContainer.removeAll();

        if (items == null || items.isEmpty()) {
            renderEmptyNoticesState();
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            LecturerDashboardModel.NoticeItem item = items.get(i);
            JPanel noticeCard = createNoticeCard(item);
            noticesContainer.add(noticeCard);
            if (i < items.size() - 1) {
                noticesContainer.add(Box.createRigidArea(new Dimension(0, 12)));
            }
        }

        noticesContainer.revalidate();
        noticesContainer.repaint();
    }

    private JPanel createNoticeCard(LecturerDashboardModel.NoticeItem item) {
        final boolean[] isHovered = {false};
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(isHovered[0] ? new Color(255, 255, 255, 255) : new Color(255, 255, 255, 245));
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 14, 14));

                // Left 4px accent stripe: Deep navy for Lecturers, Cyber Cyan for All
                String aud = (item != null && item.getTargetAudience() != null) ? item.getTargetAudience() : "All";
                Color accent = "Lecturers".equalsIgnoreCase(aud) ? new Color(11, 79, 156) : new Color(2, 132, 199);
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Float(0, 0, 4, h - 1, 4, 4));

                g2.setColor(isHovered[0] ? accent : new Color(220, 228, 238, 200));
                g2.setStroke(new BasicStroke(isHovered[0] ? 1.5f : 1.0f));
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 14, 14));
                g2.dispose();
            }

            @Override
            public Dimension getMaximumSize() {
                Dimension pref = getPreferredSize();
                return new Dimension(Integer.MAX_VALUE, Math.max(pref.height, 100));
            }
        };

        card.setLayout(new BorderLayout(14, 0));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 20));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered[0] = true;
                card.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered[0] = false;
                card.repaint();
            }
        });

        // Left Pin Indicator
        JPanel pinPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pinPanel.setOpaque(false);
        JLabel pinIcon = new JLabel(new DashboardVectorIcon(DashboardVectorIcon.IconType.PIN, 20, 20, COLOR_NOTIF_ACCENT));
        pinPanel.add(pinIcon);

        // Center Details
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        String rawTitle = (item != null && item.getTitle() != null) ? item.getTitle() : "Notice";
        JLabel titleLabel = new JLabel(rawTitle);
        titleLabel.setFont(FONT_NOTICE_TITLE);
        titleLabel.setForeground(COLOR_DEEP_NAVY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String rawDesc = (item != null && item.getDescription() != null) ? item.getDescription() : "";
        JLabel descLabel = new JLabel("<html><body style='width:650px;'>" + escapeHtml(rawDesc) + "</body></html>");
        descLabel.setFont(FONT_NOTICE_DESC);
        descLabel.setForeground(COLOR_TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Date and Audience Tags
        JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        metaPanel.setOpaque(false);
        metaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String dateStr = (item != null) ? formatNoticeDate(item.getPublishedDate()) : "Recently";
        JLabel dateLabel = new JLabel("Published: " + dateStr);
        dateLabel.setFont(FONT_NOTICE_DATE);
        dateLabel.setForeground(COLOR_TEXT_MUTED);

        String audienceText = (item != null && item.getTargetAudience() != null) ? item.getTargetAudience() : "All";
        JLabel audienceBadge = new JLabel(audienceText) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(235, 243, 254));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        audienceBadge.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        audienceBadge.setForeground(COLOR_PRO_BLUE);
        audienceBadge.setBorder(new EmptyBorder(2, 6, 2, 6));

        metaPanel.add(dateLabel);
        metaPanel.add(audienceBadge);

        centerPanel.add(titleLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        centerPanel.add(descLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        centerPanel.add(metaPanel);

        card.add(pinPanel, BorderLayout.WEST);
        card.add(centerPanel, BorderLayout.CENTER);

        return card;
    }

    /**
     * Formats database datetime strings (yyyy-MM-dd HH:mm:ss) into human-readable format (MMM dd, yyyy).
     */
    private String formatNoticeDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return "Recently";
        }
        try {
            java.text.SimpleDateFormat inFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date parsed = inFormat.parse(rawDate.trim());
            java.text.SimpleDateFormat outFormat = new java.text.SimpleDateFormat("MMM dd, yyyy");
            return outFormat.format(parsed);
        } catch (Exception e1) {
            try {
                java.text.SimpleDateFormat inFormat2 = new java.text.SimpleDateFormat("yyyy-MM-dd");
                java.util.Date parsed = inFormat2.parse(rawDate.trim());
                java.text.SimpleDateFormat outFormat = new java.text.SimpleDateFormat("MMM dd, yyyy");
                return outFormat.format(parsed);
            } catch (Exception e2) {
                return rawDate;
            }
        }
    }

    // =========================================================================
    // STATE VIEWS: LOADING, ERROR, UNAUTHENTICATED
    // =========================================================================

    /**
     * Builds the clean animated loading screen.
     */
    private JPanel createLoadingView() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_PAGE_BG);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(240, 6));
        progressBar.setForeground(COLOR_PRO_BLUE);
        progressBar.setBackground(COLOR_BORDER);
        progressBar.setBorderPainted(false);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel loadingTitle = new JLabel("Loading Academic Dashboard...");
        loadingTitle.setFont(FONT_STATE_TITLE);
        loadingTitle.setForeground(COLOR_DEEP_NAVY);
        loadingTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel loadingSubtitle = new JLabel("Connecting to TecFAMS MySQL database...");
        loadingSubtitle.setFont(FONT_STATE_DESC);
        loadingSubtitle.setForeground(COLOR_TEXT_SECONDARY);
        loadingSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(loadingTitle);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(loadingSubtitle);
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(progressBar);

        panel.add(card);
        return panel;
    }

    /**
     * Builds the Database Connection Error diagnostic panel.
     */
    private JPanel createErrorView() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_PAGE_BG);

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.setColor(COLOR_BORDER);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(32, 36, 32, 36));
        card.setPreferredSize(new Dimension(540, 290));

        JLabel iconLabel = new JLabel(new DashboardVectorIcon(DashboardVectorIcon.IconType.ERROR_ALERT, 36, 36, COLOR_DANGER));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        errorTitleLabel = new JLabel("Database Connection Error");
        errorTitleLabel.setFont(FONT_STATE_TITLE);
        errorTitleLabel.setForeground(COLOR_DEEP_NAVY);
        errorTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        errorDetailsArea = new JTextArea("Unable to connect to MySQL database.");
        errorDetailsArea.setFont(FONT_STATE_DESC);
        errorDetailsArea.setForeground(COLOR_TEXT_SECONDARY);
        errorDetailsArea.setWrapStyleWord(true);
        errorDetailsArea.setLineWrap(true);
        errorDetailsArea.setEditable(false);
        errorDetailsArea.setFocusable(false);
        errorDetailsArea.setOpaque(false);
        errorDetailsArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorDetailsArea.setMaximumSize(new Dimension(460, 60));

        JButton retryBtn = new JButton("Retry Connection");
        retryBtn.setFont(FONT_BUTTON);
        retryBtn.setForeground(COLOR_WHITE);
        retryBtn.setBackground(COLOR_PRO_BLUE);
        retryBtn.setFocusPainted(false);
        retryBtn.setBorder(new EmptyBorder(10, 24, 10, 24));
        retryBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        retryBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        retryBtn.addActionListener(e -> refreshDashboard());

        card.add(iconLabel);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(errorTitleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(errorDetailsArea);
        card.add(Box.createRigidArea(new Dimension(0, 22)));
        card.add(retryBtn);

        panel.add(card);
        return panel;
    }

    /**
     * Builds the unauthenticated / session-required prompt panel.
     */
    private JPanel createUnauthenticatedView() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_PAGE_BG);

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.setColor(COLOR_BORDER);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(32, 36, 32, 36));
        card.setPreferredSize(new Dimension(540, 270));

        JLabel iconLabel = new JLabel(new DashboardVectorIcon(DashboardVectorIcon.IconType.LOCK, 36, 36, COLOR_PRO_BLUE));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Authenticated Session Required");
        titleLabel.setFont(FONT_STATE_TITLE);
        titleLabel.setForeground(COLOR_DEEP_NAVY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel("<html><center>No active lecturer session was detected.<br>Please sign in through the TecFAMS portal to access your dashboard.</center></html>");
        descLabel.setFont(FONT_STATE_DESC);
        descLabel.setForeground(COLOR_TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginBtn = new JButton("Go to Login");
        loginBtn.setFont(FONT_BUTTON);
        loginBtn.setForeground(COLOR_WHITE);
        loginBtn.setBackground(COLOR_PRO_BLUE);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorder(new EmptyBorder(10, 24, 10, 24));
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> {
            Window top = SwingUtilities.getWindowAncestor(this);
            if (top != null) top.dispose();
            LoginUI loginUI = new LoginUI();
            loginUI.setVisible(true);
        });

        card.add(iconLabel);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(descLabel);
        card.add(Box.createRigidArea(new Dimension(0, 22)));
        card.add(loginBtn);

        panel.add(card);
        return panel;
    }

    // =========================================================================
    // MODEL TO UI BINDING
    // =========================================================================

    /**
     * Applies the database-backed model to the UI components on the Event Dispatch Thread.
     */
    public void applyModelToUI(LecturerDashboardModel model) {
        if (model == null) return;

        // Welcome Section
        String name = model.getLecturerName();
        welcomeTitleLabel.setText("Welcome back, " + ((name != null && !name.equals("—")) ? name : "Lecturer"));
        departmentBadgeLabel.setText(model.getDepartmentBadgeText());

        // Summary Cards
        coursesCountLabel.setText(model.getCoursesCountDisplay());
        materialsCountLabel.setText(model.getMaterialsCountDisplay());

        // Recent Notices
        if (model.hasNotices()) {
            renderNoticeItems(model.getNotices());
        } else {
            renderEmptyNoticesState();
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }

    // =========================================================================
    // VECTOR ICON RENDERER (Java2D - Crisp HiDPI, zero external JAR dependencies)
    // =========================================================================
    public static class DashboardVectorIcon implements Icon {

        public enum IconType {
            GRADUATION_CAP,
            DOCUMENT_STACK,
            TECH_COURSE_CIRCUIT,
            TECH_CLOUD_STORAGE,
            PIN,
            ERROR_ALERT,
            LOCK
        }

        private final IconType type;
        private final int width;
        private final int height;
        private final Color color;

        public DashboardVectorIcon(IconType type, int width, int height, Color color) {
            this.type = type;
            this.width = width;
            this.height = height;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            g2.setColor(color);

            float scaleX = width / 24.0f;
            float scaleY = height / 24.0f;
            g2.scale(scaleX, scaleY);

            switch (type) {
                case GRADUATION_CAP:
                    drawGraduationCap(g2);
                    break;
                case DOCUMENT_STACK:
                    drawDocumentStack(g2);
                    break;
                case TECH_COURSE_CIRCUIT:
                    drawTechCourseCircuit(g2);
                    break;
                case TECH_CLOUD_STORAGE:
                    drawTechCloudStorage(g2);
                    break;
                case PIN:
                    drawPin(g2);
                    break;
                case ERROR_ALERT:
                    drawErrorAlert(g2);
                    break;
                case LOCK:
                    drawLock(g2);
                    break;
            }

            g2.dispose();
        }

        private void drawTechCourseCircuit(Graphics2D g2) {
            g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Central microchip processor body
            g2.drawRoundRect(6, 6, 12, 12, 3, 3);
            g2.setColor(new Color(255, 255, 255, 60));
            g2.fillRoundRect(7, 7, 10, 10, 2, 2);
            g2.setColor(color);

            // Inner code symbol < / >
            g2.drawLine(9, 10, 8, 12);
            g2.drawLine(8, 12, 9, 14); // <
            g2.drawLine(15, 10, 16, 12);
            g2.drawLine(16, 12, 15, 14); // >
            g2.drawLine(11, 14, 13, 10); // /

            // External circuit bus pins extending to nodes
            // Top pins
            g2.drawLine(9, 6, 9, 2);
            g2.drawLine(15, 6, 15, 2);
            g2.fillOval(8, 1, 3, 3);
            g2.fillOval(14, 1, 3, 3);

            // Bottom pins
            g2.drawLine(9, 18, 9, 22);
            g2.drawLine(15, 18, 15, 22);
            g2.fillOval(8, 21, 3, 3);
            g2.fillOval(14, 21, 3, 3);

            // Left & Right pins
            g2.drawLine(6, 9, 2, 9);
            g2.drawLine(6, 15, 2, 15);
            g2.fillOval(1, 8, 3, 3);
            g2.fillOval(1, 14, 3, 3);

            g2.drawLine(18, 9, 22, 9);
            g2.drawLine(18, 15, 22, 15);
            g2.fillOval(21, 8, 3, 3);
            g2.fillOval(21, 14, 3, 3);
        }

        private void drawTechCloudStorage(Graphics2D g2) {
            g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Cloud base structure
            Path2D.Float cloud = new Path2D.Float();
            cloud.moveTo(6, 15);
            cloud.curveTo(3, 15, 2, 12, 5, 10);
            cloud.curveTo(4, 6, 10, 5, 12, 7);
            cloud.curveTo(14, 4, 20, 6, 19, 10);
            cloud.curveTo(22, 11, 22, 15, 18, 15);
            cloud.closePath();
            g2.draw(cloud);

            g2.setColor(new Color(255, 255, 255, 50));
            g2.fill(cloud);
            g2.setColor(color);

            // Upload sync arrow
            g2.drawLine(12, 17, 12, 10);
            g2.drawLine(12, 10, 9, 13);
            g2.drawLine(12, 10, 15, 13);

            // Base server data bus track with circuit nodes
            g2.drawLine(5, 20, 19, 20);
            g2.fillOval(4, 19, 3, 3);
            g2.fillOval(11, 19, 3, 3);
            g2.fillOval(18, 19, 3, 3);
        }

        private void drawGraduationCap(Graphics2D g2) {
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Polygon cap = new Polygon();
            cap.addPoint(12, 4);
            cap.addPoint(22, 9);
            cap.addPoint(12, 14);
            cap.addPoint(2, 9);
            g2.fill(cap);

            g2.drawArc(6, 11, 12, 8, 190, 160);
            g2.drawLine(20, 10, 20, 17);
            g2.fillOval(19, 17, 3, 3);
        }

        private void drawDocumentStack(Graphics2D g2) {
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawRoundRect(6, 3, 14, 18, 3, 3);
            g2.drawLine(10, 7, 16, 7);
            g2.drawLine(10, 11, 16, 11);
            g2.drawLine(10, 15, 14, 15);
            g2.drawArc(3, 7, 4, 14, 100, 160);
        }

        private void drawPin(Graphics2D g2) {
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.fillOval(8, 3, 8, 8);
            g2.drawLine(12, 11, 12, 19);
            g2.drawLine(6, 11, 18, 11);
        }

        private void drawErrorAlert(Graphics2D g2) {
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(2, 2, 20, 20);
            g2.drawLine(12, 7, 12, 13);
            g2.fillOval(11, 16, 2, 2);
        }

        private void drawLock(Graphics2D g2) {
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(7, 4, 10, 10, 0, 180);
            g2.drawRoundRect(5, 10, 14, 11, 3, 3);
            g2.fillOval(11, 14, 2, 3);
        }

        @Override public int getIconWidth() { return width; }
        @Override public int getIconHeight() { return height; }
    }

    // =========================================================================
    // STANDALONE RUNNER / DEMO (Connects to Live Database)
    // =========================================================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Establish session for Dr. Chinthaka Premachandra (Lecturer ID: 4) for direct testing
        UserSession.setCurrentSession(new UserSession(
                4,
                "lec_chinthaka",
                "Lecturer",
                "Dr. Chinthaka Premachandra",
                "Senior Lecturer Gr. I",
                "Department of Information and Communication Technology",
                "chinthaka@fot.ruh.ac.lk"
        ));

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TecFAMS - Lecturer Portal (Part 3 Integration Verification)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 720);
            frame.setLayout(new BorderLayout());

            // 1. Existing Navbar
            Navbar navbar = new Navbar(Navbar.PAGE_DASHBOARD);

            // 2. Shared Container with CardLayout
            CardLayout cardLayout = new CardLayout();
            JPanel contentPanel = new JPanel(cardLayout);

            LecturerDashboard dashboard = new LecturerDashboard();
            contentPanel.add(dashboard, Navbar.PAGE_DASHBOARD);

            // Placeholder modules for single-window navigation
            contentPanel.add(createModulePlaceholder(
                    "Course Materials",
                    "Manage course syllabus, upload lecture notes, lab sheets, and student reference guides.",
                    navbar, cardLayout, contentPanel
            ), Navbar.PAGE_MATERIALS);

            contentPanel.add(createModulePlaceholder(
                    "Student Eligibility",
                    "Review attendance thresholds (80% criteria), continuous assessment requirements, and exam admissions.",
                    navbar, cardLayout, contentPanel
            ), Navbar.PAGE_ELIGIBILITY);

            contentPanel.add(createModulePlaceholder(
                    "Marks Management",
                    "Enter, calculate, and publish continuous assessment and end-of-semester exam marks.",
                    navbar, cardLayout, contentPanel
            ), Navbar.PAGE_MARKS);

            contentPanel.add(createModulePlaceholder(
                    "My Profile",
                    "View and update your academic designations, department affiliations, and contact credentials.",
                    navbar, cardLayout, contentPanel
            ), Navbar.PAGE_PROFILE);

            // Wire Navigation Listener to switch views in the same window
            navbar.addNavigationListener(pageName -> {
                cardLayout.show(contentPanel, pageName);
                if (Navbar.PAGE_DASHBOARD.equals(pageName)) {
                    dashboard.refreshDashboard();
                }
            });

            // Wire Logout Listener
            navbar.addLogoutListener(UserSession::clearSession);

            frame.add(navbar, BorderLayout.NORTH);
            frame.add(contentPanel, BorderLayout.CENTER);

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static JPanel createModulePlaceholder(
            String title, String description,
            Navbar navbar, CardLayout layout, JPanel container) {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_PAGE_BG);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(36, 44, 36, 44)
        ));
        card.setPreferredSize(new Dimension(580, 260));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLbl.setForeground(COLOR_DEEP_NAVY);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLbl = new JLabel("<html><center style='width:450px;'>" + description + "</center></html>");
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLbl.setForeground(COLOR_TEXT_SECONDARY);
        descLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statusBadge = new JLabel("Module Under Active Development");
        statusBadge.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 11));
        statusBadge.setForeground(COLOR_PRO_BLUE);
        statusBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton returnBtn = new JButton("← Return to Dashboard");
        returnBtn.setFont(FONT_BUTTON);
        returnBtn.setForeground(COLOR_WHITE);
        returnBtn.setBackground(COLOR_PRO_BLUE);
        returnBtn.setFocusPainted(false);
        returnBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        returnBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        returnBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        returnBtn.addActionListener(e -> {
            navbar.setActivePage(Navbar.PAGE_DASHBOARD);
            layout.show(container, Navbar.PAGE_DASHBOARD);
        });

        card.add(titleLbl);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(descLbl);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(statusBadge);
        card.add(Box.createRigidArea(new Dimension(0, 22)));
        card.add(returnBtn);

        panel.add(card);
        return panel;
    }
}
