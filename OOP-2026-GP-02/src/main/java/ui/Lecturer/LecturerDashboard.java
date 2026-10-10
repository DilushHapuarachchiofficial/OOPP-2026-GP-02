package main.java.ui.Lecturer;

import main.java.model.Lecturer.LecturerDashboardModel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;


public class LecturerDashboard extends JPanel {

    // =========================================================================
    // COLOR PALETTE (Strictly matching TecFAMS Design Standard)
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

    // =========================================================================
    // TYPOGRAPHY (Segoe UI Hierarchy)
    // =========================================================================
    public static final Font FONT_WELCOME_TITLE    = new Font("Segoe UI", Font.BOLD, 21);
    public static final Font FONT_WELCOME_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_DEPARTMENT_BADGE = new Font("Segoe UI Semibold", Font.BOLD, 11);
    public static final Font FONT_CARD_LABEL       = new Font("Segoe UI Semibold", Font.PLAIN, 13);
    public static final Font FONT_CARD_COUNT       = new Font("Segoe UI", Font.BOLD, 30);
    public static final Font FONT_CARD_SUBTITLE    = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_SECTION_TITLE    = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SECTION_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_NOTICE_TITLE     = new Font("Segoe UI Semibold", Font.PLAIN, 13);
    public static final Font FONT_NOTICE_DESC      = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_NOTICE_DATE      = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_EMPTY_STATE      = new Font("Segoe UI Semibold", Font.PLAIN, 13);
    public static final Font FONT_EMPTY_SUBTEXT    = new Font("Segoe UI", Font.PLAIN, 11);

    // =========================================================================
    // UI BINDING REFERENCES (Ready for Part 2 database injection)
    // =========================================================================
    private JLabel welcomeTitleLabel;
    private JLabel departmentBadgeLabel;
    private JLabel coursesCountLabel;
    private JLabel materialsCountLabel;
    private JPanel noticesContainer;

    // Current State Model
    private LecturerDashboardModel model;

    /**
     * Constructs the dashboard using neutral placeholder data for Part 1.
     */
    public LecturerDashboard() {
        this(new LecturerDashboardModel());
    }

    /**
     * Constructs the dashboard with a specific data model.
     */
    public LecturerDashboard(LecturerDashboardModel model) {
        this.model = (model != null) ? model : new LecturerDashboardModel();
        initializeDashboard();
        updateDashboard(this.model);
    }

    // =========================================================================
    // UI CONSTRUCTION & LAYOUT
    // =========================================================================
    private void initializeDashboard() {
        setLayout(new BorderLayout());
        setBackground(COLOR_PAGE_BG);

        // Main vertical content container
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(24, 32, 32, 32));

        // Section A: Welcome Section
        contentPanel.add(createWelcomeSection());
        contentPanel.add(Box.createVerticalStrut(20));

        // Section B: Summary Cards Row (Assigned Courses & Uploaded Materials)
        contentPanel.add(createSummaryCardsRow());
        contentPanel.add(Box.createVerticalStrut(24));

        // Section C: Recent Notices Section
        contentPanel.add(createRecentNoticesSection());

        // Overflow Handling: Smooth vertical scrolling
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(COLOR_PAGE_BG);

        add(scrollPane, BorderLayout.CENTER);
    }

    // =========================================================================
    // SECTION A: WELCOME SECTION
    // =========================================================================
    private JPanel createWelcomeSection() {
        JPanel card = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // White card background
                g2.setColor(COLOR_WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));

                // Subtle border
                g2.setColor(COLOR_BORDER);
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 14, 14));

                // Top accent strip (Professional Blue)
                g2.setColor(COLOR_PRO_BLUE);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, 4, 4, 4));

                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setBorder(new EmptyBorder(22, 28, 22, 28));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Left Text Block
        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        welcomeTitleLabel = new JLabel("Welcome back, " + model.getLecturerName());
        welcomeTitleLabel.setFont(FONT_WELCOME_TITLE);
        welcomeTitleLabel.setForeground(COLOR_DEEP_NAVY);
        welcomeTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Here's an overview of your teaching activities.");
        subtitleLabel.setFont(FONT_WELCOME_SUBTITLE);
        subtitleLabel.setForeground(COLOR_TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textCol.add(welcomeTitleLabel);
        textCol.add(Box.createVerticalStrut(4));
        textCol.add(subtitleLabel);

        // Right Department Badge
        JPanel rightCol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        rightCol.setOpaque(false);

        departmentBadgeLabel = new JLabel(model.getDepartmentName()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(235, 243, 254)); // Soft blue pill
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        departmentBadgeLabel.setFont(FONT_DEPARTMENT_BADGE);
        departmentBadgeLabel.setForeground(COLOR_PRO_BLUE);
        departmentBadgeLabel.setBorder(new EmptyBorder(6, 14, 6, 14));
        departmentBadgeLabel.setOpaque(false);

        rightCol.add(departmentBadgeLabel);

        card.add(textCol, BorderLayout.WEST);
        card.add(rightCol, BorderLayout.EAST);

        return card;
    }

    // =========================================================================
    // SECTION B: SUMMARY CARDS (Assigned Courses & Uploaded Materials)
    // =========================================================================
    private JPanel createSummaryCardsRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 20, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 135));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Card 1: Assigned Courses
        coursesCountLabel = new JLabel(model.getCoursesCountDisplay());
        JPanel coursesCard = createMetricCard(
                "Assigned Courses",
                coursesCountLabel,
                "Active courses assigned for instruction",
                new DashboardVectorIcon(DashboardVectorIcon.Type.COURSES, 22, 22),
                new Color(235, 243, 254),
                COLOR_PRO_BLUE
        );

        // Card 2: Uploaded Materials
        materialsCountLabel = new JLabel(model.getMaterialsCountDisplay());
        JPanel materialsCard = createMetricCard(
                "Uploaded Materials",
                materialsCountLabel,
                "Course materials & learning resources uploaded",
                new DashboardVectorIcon(DashboardVectorIcon.Type.MATERIALS, 22, 22),
                new Color(240, 249, 255),
                COLOR_ACCENT_BLUE
        );

        row.add(coursesCard);
        row.add(materialsCard);

        return row;
    }

    private JPanel createMetricCard(String labelText, JLabel countLabel, String subtitleText,
                                     Icon icon, Color iconBgColor, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(18, 0)) {
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
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Background
                g2.setColor(hovered ? COLOR_CARD_HOVER : COLOR_WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));

                // Border
                g2.setColor(COLOR_BORDER);
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 14, 14));

                // Left Accent Indicator Line
                g2.setColor(accentColor);
                g2.fill(new RoundRectangle2D.Float(0, 16, 4, h - 32, 4, 4));

                g2.dispose();
                super.paintComponent(g);
            }
        };

        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Circular Icon Badge
        JLabel iconBadge = new JLabel(icon, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBgColor);
                g2.fill(new Ellipse2D.Float(0, 0, getWidth(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconBadge.setPreferredSize(new Dimension(50, 50));
        iconBadge.setOpaque(false);

        // Center Content Block
        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(FONT_CARD_LABEL);
        label.setForeground(COLOR_TEXT_SECONDARY);

        countLabel.setFont(FONT_CARD_COUNT);
        countLabel.setForeground(COLOR_DEEP_NAVY);

        JLabel subtitle = new JLabel(subtitleText);
        subtitle.setFont(FONT_CARD_SUBTITLE);
        subtitle.setForeground(COLOR_TEXT_MUTED);

        textCol.add(label);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(countLabel);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(subtitle);

        card.add(iconBadge, BorderLayout.WEST);
        card.add(textCol, BorderLayout.CENTER);

        return card;
    }

    // =========================================================================
    // SECTION C: RECENT NOTICES SECTION
    // =========================================================================
    private JPanel createRecentNoticesSection() {
        JPanel card = new JPanel(new BorderLayout(0, 16)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(COLOR_WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));

                g2.setColor(COLOR_BORDER);
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 14, 14));

                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setBorder(new EmptyBorder(24, 28, 28, 28));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Section Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Recent Notices");
        titleLabel.setFont(FONT_SECTION_TITLE);
        titleLabel.setForeground(COLOR_DEEP_NAVY);

        JLabel subLabel = new JLabel("Official faculty & university announcements");
        subLabel.setFont(FONT_SECTION_SUBTITLE);
        subLabel.setForeground(COLOR_TEXT_SECONDARY);

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setOpaque(false);
        headerText.add(titleLabel);
        headerText.add(Box.createVerticalStrut(2));
        headerText.add(subLabel);

        headerPanel.add(headerText, BorderLayout.WEST);

        // Container holding notices list or empty state
        noticesContainer = new JPanel();
        noticesContainer.setLayout(new BoxLayout(noticesContainer, BoxLayout.Y_AXIS));
        noticesContainer.setOpaque(false);

        renderNoticesContent();

        // Assemble Section Card
        JPanel bodyWrapper = new JPanel(new BorderLayout(0, 14));
        bodyWrapper.setOpaque(false);

        JSeparator divider = new JSeparator();
        divider.setForeground(COLOR_BORDER);

        bodyWrapper.add(divider, BorderLayout.NORTH);
        bodyWrapper.add(noticesContainer, BorderLayout.CENTER);

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(bodyWrapper, BorderLayout.CENTER);

        return card;
    }

    /**
     * Renders either the notice items or a clean empty-state panel.
     */
    private void renderNoticesContent() {
        if (noticesContainer == null) return;
        noticesContainer.removeAll();

        List<LecturerDashboardModel.NoticeItem> notices = model.getNotices();

        if (notices == null || notices.isEmpty()) {
            // Clean Neutral Empty State (Prevents fabricated notices)
            noticesContainer.add(createEmptyNoticesPanel());
        } else {
            // Render actual notice records
            for (int i = 0; i < notices.size(); i++) {
                LecturerDashboardModel.NoticeItem notice = notices.get(i);
                noticesContainer.add(createNoticeRow(notice));

                if (i < notices.size() - 1) {
                    JSeparator rowSep = new JSeparator();
                    rowSep.setForeground(new Color(241, 245, 249));
                    noticesContainer.add(rowSep);
                }
            }
        }

        noticesContainer.revalidate();
        noticesContainer.repaint();
    }

    private JPanel createEmptyNoticesPanel() {
        JPanel emptyPanel = new JPanel();
        emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
        emptyPanel.setOpaque(false);
        emptyPanel.setBorder(new EmptyBorder(36, 16, 36, 16));

        JLabel iconLabel = new JLabel(new DashboardVectorIcon(DashboardVectorIcon.Type.NOTICE_EMPTY, 32, 32));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msgLabel = new JLabel("No recent notices available.");
        msgLabel.setFont(FONT_EMPTY_STATE);
        msgLabel.setForeground(COLOR_TEXT_SECONDARY);
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("University and faculty notices will appear here once published.");
        subLabel.setFont(FONT_EMPTY_SUBTEXT);
        subLabel.setForeground(COLOR_TEXT_MUTED);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyPanel.add(iconLabel);
        emptyPanel.add(Box.createVerticalStrut(10));
        emptyPanel.add(msgLabel);
        emptyPanel.add(Box.createVerticalStrut(4));
        emptyPanel.add(subLabel);

        return emptyPanel;
    }

    private JPanel createNoticeRow(LecturerDashboardModel.NoticeItem notice) {
        JPanel row = new JPanel(new BorderLayout(14, 4));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(12, 8, 12, 8));

        // Left Tag Icon
        JLabel tag = new JLabel("NOTICE", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(235, 243, 254));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tag.setPreferredSize(new Dimension(54, 26));
        tag.setFont(new Font("Segoe UI", Font.BOLD, 9));
        tag.setForeground(COLOR_PRO_BLUE);

        // Center Content (Title + Description)
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel title = new JLabel(notice.getTitle());
        title.setFont(FONT_NOTICE_TITLE);
        title.setForeground(COLOR_DEEP_NAVY);

        JLabel desc = new JLabel("<html><body style='width: 580px;'>" + notice.getDescription() + "</body></html>");
        desc.setFont(FONT_NOTICE_DESC);
        desc.setForeground(COLOR_TEXT_SECONDARY);

        content.add(title);
        content.add(Box.createVerticalStrut(3));
        content.add(desc);

        // Right Date Stamp
        JLabel dateLabel = new JLabel(notice.getPublishedDate());
        dateLabel.setFont(FONT_NOTICE_DATE);
        dateLabel.setForeground(COLOR_TEXT_MUTED);

        row.add(tag, BorderLayout.WEST);
        row.add(content, BorderLayout.CENTER);
        row.add(dateLabel, BorderLayout.EAST);

        return row;
    }

    // =========================================================================
    // PUBLIC DATA BINDING API (Ready for Part 2 database integration)
    // =========================================================================
    /**
     * Updates the full dashboard state using a LecturerDashboardModel.
     */
    public void updateDashboard(LecturerDashboardModel newModel) {
        if (newModel == null) return;
        this.model = newModel;

        if (welcomeTitleLabel != null) {
            welcomeTitleLabel.setText("Welcome back, " + model.getLecturerName());
        }
        if (departmentBadgeLabel != null) {
            departmentBadgeLabel.setText(model.getDepartmentName());
        }
        if (coursesCountLabel != null) {
            coursesCountLabel.setText(model.getCoursesCountDisplay());
        }
        if (materialsCountLabel != null) {
            materialsCountLabel.setText(model.getMaterialsCountDisplay());
        }

        renderNoticesContent();
        revalidate();
        repaint();
    }

    /**
     * Updates lecturer personal header details.
     */
    public void setLecturerInfo(String name, String department) {
        model.setLecturerName(name);
        model.setDepartmentName(department);
        if (welcomeTitleLabel != null) {
            welcomeTitleLabel.setText("Welcome back, " + model.getLecturerName());
        }
        if (departmentBadgeLabel != null) {
            departmentBadgeLabel.setText(model.getDepartmentName());
        }
        repaint();
    }

    /**
     * Updates assigned courses count. Pass null to display the neutral placeholder ("—").
     */
    public void setAssignedCoursesCount(Integer count) {
        model.setAssignedCoursesCount(count);
        if (coursesCountLabel != null) {
            coursesCountLabel.setText(model.getCoursesCountDisplay());
        }
        repaint();
    }

    /**
     * Updates uploaded materials count. Pass null to display the neutral placeholder ("—").
     */
    public void setUploadedMaterialsCount(Integer count) {
        model.setUploadedMaterialsCount(count);
        if (materialsCountLabel != null) {
            materialsCountLabel.setText(model.getMaterialsCountDisplay());
        }
        repaint();
    }

    /**
     * Updates notices list. Passing null or empty list displays the neutral empty state.
     */
    public void setNotices(List<LecturerDashboardModel.NoticeItem> notices) {
        model.setNotices(notices);
        renderNoticesContent();
    }

    // =========================================================================
    // VECTOR ICON RENDERER (Java2D - Crisp on all HiDPI screens)
    // =========================================================================
    public static class DashboardVectorIcon implements Icon {
        public enum Type { COURSES, MATERIALS, NOTICE_EMPTY }

        private final Type type;
        private final int width;
        private final int height;

        public DashboardVectorIcon(Type type, int width, int height) {
            this.type = type;
            this.width = width;
            this.height = height;
        }

        @Override public int getIconWidth() { return width; }
        @Override public int getIconHeight() { return height; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            switch (type) {
                case COURSES:
                    // Academic Graduation Cap Icon
                    g2.setColor(COLOR_PRO_BLUE);
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Polygon cap = new Polygon();
                    cap.addPoint(x + 11, y + 3);
                    cap.addPoint(x + 21, y + 8);
                    cap.addPoint(x + 11, y + 13);
                    cap.addPoint(x + 1, y + 8);
                    g2.draw(cap);
                    g2.drawArc(x + 6, y + 11, 10, 7, 0, -180);
                    g2.drawLine(x + 21, y + 8, x + 21, y + 16); // Tassel
                    break;

                case MATERIALS:
                    // Learning Resource Document Sheet
                    g2.setColor(COLOR_ACCENT_BLUE);
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawRoundRect(x + 3, y + 2, 16, 18, 3, 3);
                    g2.drawLine(x + 7, y + 7, x + 15, y + 7);
                    g2.drawLine(x + 7, y + 11, x + 15, y + 11);
                    g2.drawLine(x + 7, y + 15, x + 12, y + 15);
                    break;

                case NOTICE_EMPTY:
                    // Notice Board / Announcement Bulletin Icon
                    g2.setColor(COLOR_TEXT_MUTED);
                    g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawRoundRect(x + 4, y + 4, 24, 24, 4, 4);
                    g2.drawLine(x + 9, y + 12, x + 23, y + 12);
                    g2.drawLine(x + 9, y + 17, x + 20, y + 17);
                    g2.drawLine(x + 9, y + 22, x + 16, y + 22);
                    break;
            }

            g2.dispose();
        }
    }

    // =========================================================================
    // STANDALONE INTEGRATION TEST RUNNER
    // =========================================================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TecFAMS - Lecturer Portal [Dashboard Part 1]");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 760);
            frame.setMinimumSize(new Dimension(880, 600));
            frame.setLocationRelativeTo(null);

            // Integrate existing Navbar at top
            Navbar navbar = new Navbar(Navbar.PAGE_DASHBOARD);

            // Integrate LecturerDashboard in center
            LecturerDashboard dashboard = new LecturerDashboard();

            // Set up main layout
            frame.setLayout(new BorderLayout());
            frame.add(navbar, BorderLayout.NORTH);
            frame.add(dashboard, BorderLayout.CENTER);

            frame.setVisible(true);
        });
    }
}
