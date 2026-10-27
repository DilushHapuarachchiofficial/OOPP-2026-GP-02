package main.java.ui.Undergraduate;

import main.java.dao.Undergraduate.NoticeDAO;
import main.java.model.Undergraduate.Notice;
import main.java.model.Undergraduate.Undergraduate;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TecFAMS - Faculty of Technology Academic Management System
 * Undergraduate Notices & Announcements UI.
 *
 * Displays faculty announcements, official circulars, and notices for undergraduates.
 * Notices are ordered chronologically with the newest notices placed at the top.
 *
 * Designed with simple, beginner-friendly standard Java Swing code.
 */
public class UndergraduateNoticesUI extends JPanel {

    // TecFAMS Theme Palette
    private static final Color COLOR_PRIMARY_NAVY = new Color(0x12, 0x3B, 0x70); // #123B70
    private static final Color COLOR_MAIN_BLUE    = new Color(0x25, 0x63, 0xEB); // #2563EB
    private static final Color COLOR_LIGHT_BLUE   = new Color(0xEA, 0xF2, 0xFF); // #EAF2FF
    private static final Color COLOR_WHITE        = new Color(0xFF, 0xFF, 0xFF); // #FFFFFF
    private static final Color COLOR_BG           = new Color(0xF5, 0xF8, 0xFD); // #F5F8FD
    private static final Color COLOR_TEXT_PRIMARY = new Color(0x17, 0x2B, 0x4D); // #172B4D
    private static final Color COLOR_TEXT_MUTED   = new Color(0x71, 0x80, 0x96); // #718096
    private static final Color COLOR_BORDER       = new Color(0xE2, 0xE8, 0xF0); // #E2E8F0
    private static final Color COLOR_SUCCESS      = new Color(0x10, 0xB9, 0x81); // #10B981
    private static final Color COLOR_PURPLE       = new Color(0x7C, 0x3A, 0xED); // #7C3AED

    // Data references
    private final Undergraduate loggedInStudent;
    private final UndergraduateDashboard parentDashboard;
    private final NoticeDAO noticeDAO;

    // Loaded Notices (Newest first)
    private List<Notice> noticesList = new ArrayList<>();

    // Dynamic UI Components
    private JPanel noticesFeedContainer;
    private JLabel noticeCountLabel;

    /**
     * Primary constructor for embedding inside UndergraduateDashboard.
     */
    public UndergraduateNoticesUI(Undergraduate student, UndergraduateDashboard parentDashboard) {
        this.loggedInStudent = student;
        this.parentDashboard = parentDashboard;
        this.noticeDAO = new NoticeDAO();

        initializeUI();
        loadNoticesData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);

        // Wrapper to pin content to NORTH so cards and gaps never stretch vertically
        JPanel scrollContentWrapper = new JPanel(new BorderLayout());
        scrollContentWrapper.setBackground(COLOR_BG);

        // Content panel stacking header, counter, and notice cards tightly
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(COLOR_BG);
        contentPanel.setBorder(new EmptyBorder(16, 24, 20, 24));

        // 1. Top Header Bar
        contentPanel.add(createHeaderBar());
        contentPanel.add(Box.createVerticalStrut(10));

        // 2. Control Bar (Status & Count only, Refresh button removed)
        contentPanel.add(createControlBar());
        contentPanel.add(Box.createVerticalStrut(12));

        // 3. Notices Feed Container (Newest notices dynamically added to the top)
        noticesFeedContainer = new JPanel();
        noticesFeedContainer.setLayout(new BoxLayout(noticesFeedContainer, BoxLayout.Y_AXIS));
        noticesFeedContainer.setOpaque(false);
        noticesFeedContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(noticesFeedContainer);

        scrollContentWrapper.add(contentPanel, BorderLayout.NORTH);

        // Put in smooth scroll pane
        JScrollPane scrollPane = new JScrollPane(scrollContentWrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(COLOR_BG);
        add(scrollPane, BorderLayout.CENTER);
    }

    // ==========================================
    // 1. TOP HEADER SECTION
    // ==========================================
    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Left box with Title and Subtitle stacked tightly
        JPanel leftBox = new JPanel();
        leftBox.setLayout(new BoxLayout(leftBox, BoxLayout.Y_AXIS));
        leftBox.setOpaque(false);

        JLabel titleLbl = new JLabel("Faculty Notices & Official Announcements");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(COLOR_PRIMARY_NAVY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        String studentName = (loggedInStudent != null && loggedInStudent.getName() != null)
                ? loggedInStudent.getName() : "H.D.W.B Hapuarachchi";
        String regNo = (loggedInStudent != null && loggedInStudent.getRegistrationNo() != null)
                ? loggedInStudent.getRegistrationNo() : "TG/2024/2085";

        JLabel subtitleLbl = new JLabel("Student: " + studentName + "  •  Reg No: " + regNo +
                "  •  Batch: 2023/2024  •  Department of ICT  •  Level II - Semester I");
        subtitleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLbl.setForeground(COLOR_TEXT_MUTED);
        subtitleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftBox.add(titleLbl);
        leftBox.add(Box.createVerticalStrut(4));
        leftBox.add(subtitleLbl);
        header.add(leftBox, BorderLayout.WEST);

        // Right Box: Back to Dashboard button
        if (parentDashboard != null) {
            JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            rightBox.setOpaque(false);

            JButton backBtn = createActionButton("← Back to Dashboard", COLOR_LIGHT_BLUE, COLOR_MAIN_BLUE);
            backBtn.addActionListener(e -> parentDashboard.showDashboardView());
            rightBox.add(backBtn);
            header.add(rightBox, BorderLayout.EAST);
        }

        return header;
    }

    // ==========================================
    // 2. CONTROL BAR (Counter & Sort Indicator)
    // ==========================================
    private JPanel createControlBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setOpaque(false);
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sortBadge = new JLabel(" Sorted: Newest on Top ");
        sortBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sortBadge.setOpaque(true);
        sortBadge.setBackground(new Color(0xDC, 0xFC, 0xE7));
        sortBadge.setForeground(new Color(0x16, 0x65, 0x34));
        sortBadge.setBorder(new EmptyBorder(4, 10, 4, 10));
        bar.add(sortBadge);

        bar.add(Box.createHorizontalStrut(10));

        noticeCountLabel = new JLabel("Loading announcements...");
        noticeCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        noticeCountLabel.setForeground(COLOR_TEXT_MUTED);
        bar.add(noticeCountLabel);

        return bar;
    }

    // ==========================================
    // 3. DATA LOADING & POPULATION
    // ==========================================
    public void loadNoticesData() {
        try {
            // Fetch notices from database, ordered by published_date DESC (newest at top)
            noticesList = noticeDAO.getAllNotices();

            // Populate the feed
            populateNoticesFeed();

        } catch (Exception e) {
            System.err.println("Database Notice: Error loading notices: " + e.getMessage());
        }
    }

    private void populateNoticesFeed() {
        noticesFeedContainer.removeAll();

        if (noticesList == null || noticesList.isEmpty()) {
            JPanel emptyCard = createEmptyCard();
            emptyCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            noticesFeedContainer.add(emptyCard);
            noticeCountLabel.setText("No announcements available at this time.");
        } else {
            noticeCountLabel.setText("Showing " + noticesList.size() + " active announcements");

            // Build notice cards in order (first item is the newest!)
            for (int i = 0; i < noticesList.size(); i++) {
                Notice notice = noticesList.get(i);
                boolean isLatest = (i == 0); // The very first notice is the newest!
                JPanel card = createNoticeCard(notice, isLatest);
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                noticesFeedContainer.add(card);
                if (i < noticesList.size() - 1) {
                    noticesFeedContainer.add(Box.createVerticalStrut(10));
                }
            }
        }

        noticesFeedContainer.revalidate();
        noticesFeedContainer.repaint();
    }

    // ==========================================
    // 4. NOTICE CARD COMPONENT
    // ==========================================
    private JPanel createNoticeCard(Notice notice, boolean isLatest) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                // Highlight left accent bar (Purple for latest, Blue for standard)
                g2.setColor(isLatest ? COLOR_PURPLE : COLOR_MAIN_BLUE);
                g2.fillRoundRect(0, 0, 5, getHeight(), 12, 12);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(12, 18, 12, 18)
        ));

        // Top Row: Badges (LATEST / DATE / AUDIENCE)
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel leftBadges = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        leftBadges.setOpaque(false);

        if (isLatest) {
            JLabel latestBadge = new JLabel(" ✨ LATEST ANNOUNCEMENT ");
            latestBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            latestBadge.setOpaque(true);
            latestBadge.setBackground(new Color(0xED, 0xE9, 0xFE));
            latestBadge.setForeground(COLOR_PURPLE);
            latestBadge.setBorder(new EmptyBorder(3, 8, 3, 8));
            leftBadges.add(latestBadge);
        }

        JLabel dateBadge = new JLabel(" 📅 " + notice.getPublishedDate() + " ");
        dateBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        dateBadge.setOpaque(true);
        dateBadge.setBackground(new Color(0xEA, 0xF2, 0xFF));
        dateBadge.setForeground(COLOR_MAIN_BLUE);
        dateBadge.setBorder(new EmptyBorder(3, 8, 3, 8));
        leftBadges.add(dateBadge);

        String audience = (notice.getTargetAudience() != null) ? notice.getTargetAudience() : "Students";
        JLabel audBadge = new JLabel(" Target: " + audience + " ");
        audBadge.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        audBadge.setOpaque(true);
        audBadge.setBackground(new Color(0xF1, 0xF5, 0xF9));
        audBadge.setForeground(COLOR_TEXT_PRIMARY);
        audBadge.setBorder(new EmptyBorder(3, 8, 3, 8));
        leftBadges.add(audBadge);

        topRow.add(leftBadges, BorderLayout.WEST);

        // Right side: Active status
        JLabel statusLbl = new JLabel("● Active Notice");
        statusLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusLbl.setForeground(COLOR_SUCCESS);
        topRow.add(statusLbl, BorderLayout.EAST);

        card.add(topRow, BorderLayout.NORTH);

        // Center: Title + Description stacked cleanly
        JPanel centerBox = new JPanel();
        centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
        centerBox.setOpaque(false);
        centerBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLbl = new JLabel(notice.getTitle());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(COLOR_PRIMARY_NAVY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerBox.add(titleLbl);
        centerBox.add(Box.createVerticalStrut(4));

        // Description text area (Clean, word-wrapped, strictly left aligned)
        JTextArea descArea = new JTextArea(notice.getDescription());
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descArea.setForeground(COLOR_TEXT_PRIMARY);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setOpaque(false);
        descArea.setFocusable(false);
        descArea.setBorder(null);
        descArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerBox.add(descArea);

        card.add(centerBox, BorderLayout.CENTER);

        // Bottom Row: Publisher info
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        bottomRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xF1, 0xF5, 0xF9)));

        JPanel pubBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        pubBox.setOpaque(false);

        JLabel pubIcon = new JLabel("👤 Published by:");
        pubIcon.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pubIcon.setForeground(COLOR_TEXT_MUTED);
        pubBox.add(pubIcon);

        String publisher = (notice.getPublisherName() != null) ? notice.getPublisherName() : "Faculty Administration";
        JLabel pubName = new JLabel(publisher);
        pubName.setFont(new Font("Segoe UI", Font.BOLD, 11));
        pubName.setForeground(COLOR_PRIMARY_NAVY);
        pubBox.add(pubName);

        bottomRow.add(pubBox, BorderLayout.WEST);

        JLabel deptLbl = new JLabel("Faculty of Technology • University of Sri Jayewardenepura");
        deptLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        deptLbl.setForeground(COLOR_TEXT_MUTED);
        bottomRow.add(deptLbl, BorderLayout.EAST);

        card.add(bottomRow, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createEmptyCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(COLOR_WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(24, 20, 24, 20)
        ));

        JLabel lbl = new JLabel("No notices have been published yet.");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(COLOR_TEXT_MUTED);
        card.add(lbl);
        return card;
    }

    private JButton createActionButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.darker());
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}

