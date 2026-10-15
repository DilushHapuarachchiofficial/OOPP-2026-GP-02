package main.java.ui.Lecturer;

import main.java.model.Lecturer.LecturerProfileLogic;
import main.java.model.Lecturer.LecturerProfileLogic.LecturerProfileData;
import main.java.model.Lecturer.LecturerProfileLogic.OperationResult;
import main.java.model.Lecturer.LecturerProfileLogic.ValidationResult;
import main.java.model.UserSession;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.SQLException;

/**
 * LecturerProfile.java
 * TecFAMS – Faculty of Technology Academic Management System
 *
 * Professional, Responsive Lecturer Profile User Interface.
 *
 * Key Capabilities & Enhancements:
 * - Pure Graphics2D Vector Icons for all buttons, toggles, badges, and status indicators.
 *   Zero reliance on platform unicode symbols or emoji that fail or render as broken boxes.
 * - Fluid Responsive Architecture: Automatically switches between a 2-column side-by-side
 *   layout on desktop displays (>= 920px) and a stacked 1-column layout on narrower screens (< 920px).
 * - ResponsiveScrollPanel tracks viewport width (`getScrollableTracksViewportWidth() == true`),
 *   ensuring cards expand naturally to fill 100% of available screen real estate without squishing.
 * - Full CRUD Profile & Photo Management:
 *     1. Avatar renders circular clipped uploaded photo (or crisp initials if default).
 *     2. Interactive vector-styled "Edit Photo" and "Remove Photo" buttons.
 *     3. Personal info edit mode for Name, Email, and Phone with real-time Navbar synchronization.
 *     4. Password & Security panel with eye toggle visibility buttons and complexity checks.
 * - Modern tactile button styling with gradients, subtle drop highlights, hover animations, and hand cursors.
 */
public class LecturerProfile extends JPanel {

    // =========================================================================
    // COLOR PALETTE (TecFAMS Design Language)
    // =========================================================================
    public static final Color COLOR_DEEP_NAVY      = new Color(8, 47, 90);    // #082F5A - Brand & primary headings
    public static final Color COLOR_PRO_BLUE       = new Color(11, 79, 156);  // #0B4F9C - Primary buttons & accents
    public static final Color COLOR_PRO_BLUE_HOVER = new Color(8, 63, 126);   // Hover shade for primary blue
    public static final Color COLOR_ACCENT_BLUE    = new Color(40, 120, 208); // #2878D0 - Active highlights
    public static final Color COLOR_PAGE_BG        = new Color(244, 247, 251);// #F4F7FB - Canvas background
    public static final Color COLOR_CARD_BG        = Color.WHITE;             // #FFFFFF - Card surfaces
    public static final Color COLOR_BORDER         = new Color(220, 228, 238);// #DCE4EE - Subtle card borders
    public static final Color COLOR_INPUT_BORDER   = new Color(203, 213, 225);// #CBD5E1 - Input border
    public static final Color COLOR_INPUT_FOCUS    = new Color(40, 120, 208); // #2878D0 - Input focus border
    public static final Color COLOR_READONLY_BG    = new Color(248, 250, 252);// #F8FAFC - Read-only field background
    public static final Color COLOR_TEXT_PRIMARY   = new Color(15, 23, 42);   // #0F172A - High-contrast text
    public static final Color COLOR_TEXT_SECONDARY = new Color(100, 116, 139);// #64748B - Muted labels & subtitles
    public static final Color COLOR_TEXT_MUTED     = new Color(148, 163, 184);// #94A3B8 - Hints & locks
    public static final Color COLOR_ORANGE         = new Color(255, 107, 0);  // #FF6B00 - Highlights
    public static final Color COLOR_SUCCESS        = new Color(16, 185, 129); // #10B981 - Success green
    public static final Color COLOR_SUCCESS_HOVER  = new Color(13, 153, 106); // Hover green
    public static final Color COLOR_SUCCESS_BG     = new Color(236, 253, 245);// #ECFDF5 - Success soft background
    public static final Color COLOR_DANGER         = new Color(220, 38, 38);  // #DC2626 - Error red
    public static final Color COLOR_DANGER_BG      = new Color(254, 242, 242);// #FEF2F2 - Error soft background

    // =========================================================================
    // TYPOGRAPHY (Segoe UI Hierarchy)
    // =========================================================================
    public static final Font FONT_PAGE_TITLE       = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_PAGE_SUBTITLE    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_OVERVIEW_NAME    = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SECTION_TITLE    = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SECTION_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_FIELD_LABEL      = new Font("Segoe UI Semibold", Font.PLAIN, 12);
    public static final Font FONT_INPUT            = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BUTTON           = new Font("Segoe UI Semibold", Font.BOLD, 13);
    public static final Font FONT_BUTTON_SM        = new Font("Segoe UI Semibold", Font.PLAIN, 12);
    public static final Font FONT_BADGE            = new Font("Segoe UI Semibold", Font.BOLD, 11);
    public static final Font FONT_HELPER           = new Font("Segoe UI", Font.PLAIN, 11);

    // Responsive breakpoint constant
    private static final int RESPONSIVE_BREAKPOINT = 920;

    // =========================================================================
    // INSTANCE VARIABLES & STATE
    // =========================================================================
    private int lecturerId = -1;
    private LecturerProfileData cachedData = null;
    private boolean isEditing = false;
    private BufferedImage loadedProfileImage = null;

    // Optional navigation wiring
    private Navbar parentNavbar;
    private CardLayout parentCardLayout;
    private JPanel parentContainer;

    // Overview Components
    private JLabel avatarLabel;
    private ModernButton changePhotoBtn;
    private ModernButton removePhotoBtn;
    private JLabel overviewNameLabel;
    private JLabel overviewDesignationBadge;
    private JLabel overviewDeptBadge;
    private JLabel overviewIdLabel;
    private JLabel overviewUsernameLabel;
    private JLabel offlineNoticeBanner;

    // Personal Information Form Fields
    private JTextField lecturerIdField;
    private JTextField usernameField;
    private JTextField departmentField;
    private JTextField designationField;
    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField phoneField;

    // Personal Info Action Buttons & State Indicators
    private ModernButton editProfileBtn;
    private ModernButton saveChangesBtn;
    private ModernButton cancelBtn;
    private JLabel editModeBadge;
    private StatusBanner personalInfoBanner;

    // Password Form Fields & Buttons
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private EyeToggleButton currentPassToggle;
    private EyeToggleButton newPassToggle;
    private EyeToggleButton confirmPassToggle;
    private JCheckBox showPasswordsCheckbox;
    private ModernButton changePasswordBtn;
    private ModernButton clearPasswordBtn;
    private StatusBanner passwordBanner;

    // Responsive Cards Container
    private ResponsiveCardsContainer cardsContainer;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================

    /**
     * Default constructor: Resolves lecturer identity from the active UserSession.
     */
    public LecturerProfile() {
        resolveLecturerSession();
        initializeUI();
        loadLecturerProfile();
    }

    /**
     * Constructs the profile page for a specific lecturer ID.
     */
    public LecturerProfile(int lecturerId) {
        this.lecturerId = lecturerId;
        initializeUI();
        loadLecturerProfile();
    }

    /**
     * Constructs the profile page integrated within the shared parent container and navbar.
     */
    public LecturerProfile(Navbar navbar, CardLayout cardLayout, JPanel container) {
        this.parentNavbar = navbar;
        this.parentCardLayout = cardLayout;
        this.parentContainer = container;
        resolveLecturerSession();
        initializeUI();
        loadLecturerProfile();
    }

    /**
     * Identifies the logged-in lecturer from UserSession.
     */
    private void resolveLecturerSession() {
        UserSession session = UserSession.getCurrentSession();
        if (session != null && "Lecturer".equalsIgnoreCase(session.getRole())) {
            this.lecturerId = session.getUserId();
        } else {
            // Fallback for standalone preview / testing: default to first lecturer (ID: 4)
            this.lecturerId = 4;
        }
    }

    // =========================================================================
    // UI INITIALIZATION
    // =========================================================================
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(COLOR_PAGE_BG);

        // Responsive content panel implementing Scrollable to track viewport width
        ResponsiveContentPanel contentPanel = new ResponsiveContentPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(COLOR_PAGE_BG);
        contentPanel.setBorder(new EmptyBorder(24, 28, 36, 28));

        // 1. Page Header (Title, Subtitle, Navigation / Refresh Buttons with vector icons)
        contentPanel.add(createHeaderPanel());
        contentPanel.add(Box.createVerticalStrut(16));

        // 2. Offline Notice Banner (hidden by default)
        offlineNoticeBanner = createOfflineNoticeLabel();
        contentPanel.add(offlineNoticeBanner);
        contentPanel.add(Box.createVerticalStrut(12));

        // 3. Profile Overview Card (Avatar, Photo Actions, Badges, Name, Academic Details)
        contentPanel.add(createOverviewCard());
        contentPanel.add(Box.createVerticalStrut(20));

        // 4. Two Main Content Cards in a Responsive Container
        cardsContainer = new ResponsiveCardsContainer(
                createPersonalInfoCard(),
                createSecurityPasswordCard(),
                20,
                RESPONSIVE_BREAKPOINT
        );
        contentPanel.add(cardsContainer);

        // Wrap inside JScrollPane for responsive display
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBackground(COLOR_PAGE_BG);
        scrollPane.getViewport().setBackground(COLOR_PAGE_BG);

        add(scrollPane, BorderLayout.CENTER);
    }

    // =========================================================================
    // 1. PAGE HEADER COMPONENT (Responsive Flow)
    // =========================================================================
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 12));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        // Left: Title & Subtitle
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("My Profile");
        titleLabel.setFont(FONT_PAGE_TITLE);
        titleLabel.setForeground(COLOR_DEEP_NAVY);

        JLabel subtitleLabel = new JLabel("Manage your personal information and account security.");
        subtitleLabel.setFont(FONT_PAGE_SUBTITLE);
        subtitleLabel.setForeground(COLOR_TEXT_SECONDARY);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(subtitleLabel);

        // Right: Return to Dashboard & Refresh (Crisp Vector Icons)
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsPanel.setOpaque(false);

        ModernButton refreshBtn = ModernButton.createOutline("Refresh", new ProfileVectorIcon(ProfileVectorIcon.Type.REFRESH, 14, COLOR_TEXT_SECONDARY));
        refreshBtn.setToolTipText("Reload profile information from the database");
        refreshBtn.addActionListener(e -> refreshProfileInformation());

        actionsPanel.add(refreshBtn);

        if (parentNavbar != null) {
            ModernButton backBtn = ModernButton.createPrimary("Return to Dashboard", new ProfileVectorIcon(ProfileVectorIcon.Type.BACK, 14, Color.WHITE), COLOR_PRO_BLUE);
            backBtn.setToolTipText("Return to the Lecturer Dashboard");
            backBtn.addActionListener(e -> {
                if (parentNavbar != null && parentCardLayout != null && parentContainer != null) {
                    parentNavbar.setActivePage(Navbar.PAGE_DASHBOARD);
                    parentCardLayout.show(parentContainer, Navbar.PAGE_DASHBOARD);
                }
            });
            actionsPanel.add(backBtn);
        }

        panel.add(titlePanel, BorderLayout.WEST);
        panel.add(actionsPanel, BorderLayout.EAST);

        return panel;
    }

    private JLabel createOfflineNoticeLabel() {
        JLabel label = new JLabel("  Database offline: Viewing offline session details. Editing requires a running MySQL server.", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(254, 243, 199)); // Soft amber bg
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(new Color(245, 158, 11)); // Amber border
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        label.setFont(FONT_HELPER);
        label.setForeground(new Color(146, 64, 14)); // Deep amber text
        label.setIcon(new ProfileVectorIcon(ProfileVectorIcon.Type.SHIELD, 14, new Color(180, 83, 9)));
        label.setBorder(new EmptyBorder(8, 16, 8, 16));
        label.setVisible(false);
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return label;
    }

    // =========================================================================
    // 2. PROFILE OVERVIEW HERO CARD
    // =========================================================================
    private JPanel createOverviewCard() {
        RoundedPanel card = new RoundedPanel(14, COLOR_CARD_BG, COLOR_BORDER);
        card.setLayout(new BorderLayout(24, 0));
        card.setBorder(new EmptyBorder(18, 24, 18, 24));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 145));

        // A. Left Avatar & Photo Edit Actions Container
        JPanel avatarContainer = new JPanel();
        avatarContainer.setLayout(new BoxLayout(avatarContainer, BoxLayout.Y_AXIS));
        avatarContainer.setOpaque(false);
        avatarContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        avatarLabel = new JLabel("", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                int size = Math.min(getWidth(), getHeight());
                if (loadedProfileImage != null) {
                    Shape oldClip = g2.getClip();
                    g2.setClip(new Ellipse2D.Float(0, 0, size, size));
                    g2.drawImage(loadedProfileImage, 0, 0, size, size, null);
                    g2.setClip(oldClip);

                    // Circular ring border
                    g2.setColor(new Color(255, 255, 255, 220));
                    g2.setStroke(new BasicStroke(2.0f));
                    g2.draw(new Ellipse2D.Float(1, 1, size - 2, size - 2));

                    g2.setColor(COLOR_BORDER);
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.draw(new Ellipse2D.Float(0, 0, size - 1, size - 1));
                } else {
                    // Gradient fill from Navy to Pro Blue
                    GradientPaint gp = new GradientPaint(0, 0, COLOR_DEEP_NAVY, size, size, COLOR_PRO_BLUE);
                    g2.setPaint(gp);
                    g2.fill(new Ellipse2D.Float(0, 0, size, size));

                    // Crisp border
                    g2.setColor(new Color(255, 255, 255, 120));
                    g2.setStroke(new BasicStroke(2.0f));
                    g2.draw(new Ellipse2D.Float(1, 1, size - 2, size - 2));

                    g2.dispose();
                    super.paintComponent(g);
                    return;
                }
                g2.dispose();
            }
        };
        avatarLabel.setPreferredSize(new Dimension(68, 68));
        avatarLabel.setMaximumSize(new Dimension(68, 68));
        avatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        avatarLabel.setForeground(Color.WHITE);
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Buttons for photo actions with pure Vector Icons
        JPanel photoButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        photoButtonsPanel.setOpaque(false);
        photoButtonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        changePhotoBtn = ModernButton.createSmallPill(
                "Edit Photo",
                new ProfileVectorIcon(ProfileVectorIcon.Type.CAMERA, 12, COLOR_PRO_BLUE),
                new Color(235, 243, 254),
                COLOR_PRO_BLUE,
                new Color(191, 219, 254)
        );
        changePhotoBtn.setToolTipText("Upload a new profile picture to your account");
        changePhotoBtn.addActionListener(e -> chooseAndUploadProfilePicture());

        removePhotoBtn = ModernButton.createSmallPill(
                "Remove",
                new ProfileVectorIcon(ProfileVectorIcon.Type.CLOSE, 10, COLOR_DANGER),
                new Color(254, 242, 242),
                COLOR_DANGER,
                new Color(254, 202, 202)
        );
        removePhotoBtn.setToolTipText("Reset to default initials avatar");
        removePhotoBtn.setVisible(false);
        removePhotoBtn.addActionListener(e -> resetProfilePictureToDefault());

        photoButtonsPanel.add(changePhotoBtn);
        photoButtonsPanel.add(removePhotoBtn);

        avatarContainer.add(avatarLabel);
        avatarContainer.add(Box.createVerticalStrut(6));
        avatarContainer.add(photoButtonsPanel);

        // B. Center Details (Name, Badges, Identifiers)
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);

        overviewNameLabel = new JLabel("Dr. Chinthaka Premachandra");
        overviewNameLabel.setFont(FONT_OVERVIEW_NAME);
        overviewNameLabel.setForeground(COLOR_DEEP_NAVY);

        // Badges Row (wraps nicely without truncating)
        JPanel badgesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        badgesRow.setOpaque(false);
        badgesRow.setBorder(new EmptyBorder(2, 0, 2, 0));

        JLabel roleBadge = createPillBadge("Academic Staff • Lecturer", new Color(236, 253, 245), new Color(4, 120, 87));
        overviewDesignationBadge = createPillBadge("Senior Lecturer Gr. I", new Color(235, 243, 254), COLOR_PRO_BLUE);
        overviewDeptBadge = createPillBadge("Information and Communication Technology", new Color(241, 245, 249), new Color(71, 85, 105));

        badgesRow.add(roleBadge);
        badgesRow.add(overviewDesignationBadge);
        badgesRow.add(overviewDeptBadge);

        // Metadata Subline
        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 2));
        metaRow.setOpaque(false);

        overviewIdLabel = new JLabel("Lecturer ID: #4");
        overviewIdLabel.setFont(FONT_HELPER);
        overviewIdLabel.setForeground(COLOR_TEXT_SECONDARY);

        overviewUsernameLabel = new JLabel("Username: @lec_chinthaka");
        overviewUsernameLabel.setFont(FONT_HELPER);
        overviewUsernameLabel.setForeground(COLOR_TEXT_SECONDARY);

        JLabel statusPill = new JLabel("● Account Active");
        statusPill.setFont(FONT_HELPER);
        statusPill.setForeground(COLOR_SUCCESS);

        metaRow.add(overviewIdLabel);
        metaRow.add(overviewUsernameLabel);
        metaRow.add(statusPill);

        detailsPanel.add(Box.createVerticalGlue());
        detailsPanel.add(overviewNameLabel);
        detailsPanel.add(Box.createVerticalStrut(4));
        detailsPanel.add(badgesRow);
        detailsPanel.add(Box.createVerticalStrut(2));
        detailsPanel.add(metaRow);
        detailsPanel.add(Box.createVerticalGlue());

        card.add(avatarContainer, BorderLayout.WEST);
        card.add(detailsPanel, BorderLayout.CENTER);

        return card;
    }

    // =========================================================================
    // 3A. PERSONAL INFORMATION CARD (Responsive Form)
    // =========================================================================
    private JPanel createPersonalInfoCard() {
        RoundedPanel card = new RoundedPanel(14, COLOR_CARD_BG, COLOR_BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(22, 24, 22, 24));

        // Card Header
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        cardHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("Personal Information");
        title.setFont(FONT_SECTION_TITLE);
        title.setForeground(COLOR_DEEP_NAVY);

        JLabel subtitle = new JLabel("View and update your contact details.");
        subtitle.setFont(FONT_SECTION_SUBTITLE);
        subtitle.setForeground(COLOR_TEXT_SECONDARY);

        titleBlock.add(title);
        titleBlock.add(subtitle);

        editModeBadge = createPillBadge("Read-Only", new Color(241, 245, 249), new Color(100, 116, 139));
        editModeBadge.setIcon(new ProfileVectorIcon(ProfileVectorIcon.Type.LOCK, 11, new Color(100, 116, 139)));

        cardHeader.add(titleBlock, BorderLayout.WEST);
        cardHeader.add(editModeBadge, BorderLayout.EAST);

        card.add(cardHeader);
        card.add(Box.createVerticalStrut(14));

        // Status Banner for validation / success messages
        personalInfoBanner = new StatusBanner();
        card.add(personalInfoBanner);
        card.add(Box.createVerticalStrut(10));

        // Form Fields
        lecturerIdField = createStyledTextField(false);
        usernameField = createStyledTextField(false);
        departmentField = createStyledTextField(false);
        designationField = createStyledTextField(false);
        fullNameField = createStyledTextField(false);
        emailField = createStyledTextField(false);
        phoneField = createStyledTextField(false);

        // 2-column subpanel for system read-only identifiers
        JPanel idGrid = new JPanel(new GridLayout(1, 2, 12, 0));
        idGrid.setOpaque(false);
        idGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        idGrid.add(createFormFieldWithLock("Lecturer ID", lecturerIdField));
        idGrid.add(createFormFieldWithLock("Username", usernameField));
        card.add(idGrid);
        card.add(Box.createVerticalStrut(10));

        // Full width read-only fields so long department / designation text never truncates
        card.add(createFormFieldWithLock("Faculty Department", departmentField));
        card.add(Box.createVerticalStrut(10));

        card.add(createFormFieldWithLock("Academic Designation", designationField));
        card.add(Box.createVerticalStrut(10));

        // Editable fields
        card.add(createFormField("Full Name *", fullNameField));
        card.add(Box.createVerticalStrut(10));

        card.add(createFormField("Email Address *", emailField));
        card.add(Box.createVerticalStrut(10));

        card.add(createFormField("Contact Number (Phone)", phoneField));
        card.add(Box.createVerticalStrut(6));

        // Schema limitation explanation note
        JLabel schemaHint = new JLabel("<html><i>Note: Home Address is not stored in the current faculty database schema.</i></html>");
        schemaHint.setFont(FONT_HELPER);
        schemaHint.setForeground(COLOR_TEXT_MUTED);
        card.add(schemaHint);
        card.add(Box.createVerticalStrut(18));

        // Action Buttons Row: Edit Profile, Save Changes, Cancel (Equipped with Vector Icons)
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonRow.setOpaque(false);
        buttonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        editProfileBtn = ModernButton.createPrimary("Edit Profile", new ProfileVectorIcon(ProfileVectorIcon.Type.EDIT, 14, Color.WHITE), COLOR_PRO_BLUE);
        editProfileBtn.setToolTipText("Enable editing of your personal information");
        editProfileBtn.addActionListener(e -> enableProfileEditing(true));

        saveChangesBtn = ModernButton.createSuccess("Save Changes", new ProfileVectorIcon(ProfileVectorIcon.Type.CHECK, 14, Color.WHITE));
        saveChangesBtn.setToolTipText("Save updated personal details to database");
        saveChangesBtn.setVisible(false);
        saveChangesBtn.addActionListener(e -> saveProfileChanges());

        cancelBtn = ModernButton.createOutline("Cancel", new ProfileVectorIcon(ProfileVectorIcon.Type.CLOSE, 12, COLOR_TEXT_SECONDARY));
        cancelBtn.setToolTipText("Discard changes and restore saved values");
        cancelBtn.setVisible(false);
        cancelBtn.addActionListener(e -> cancelProfileEditing());

        buttonRow.add(editProfileBtn);
        buttonRow.add(cancelBtn);
        buttonRow.add(saveChangesBtn);

        card.add(buttonRow);

        return card;
    }

    // =========================================================================
    // 3B. SECURITY & PASSWORD CARD (Interactive Eye Toggles)
    // =========================================================================
    private JPanel createSecurityPasswordCard() {
        RoundedPanel card = new RoundedPanel(14, COLOR_CARD_BG, COLOR_BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(22, 24, 22, 24));

        // Card Header
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        cardHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("Security & Password");
        title.setFont(FONT_SECTION_TITLE);
        title.setForeground(COLOR_DEEP_NAVY);

        JLabel subtitle = new JLabel("Update your password to keep your account safe.");
        subtitle.setFont(FONT_SECTION_SUBTITLE);
        subtitle.setForeground(COLOR_TEXT_SECONDARY);

        titleBlock.add(title);
        titleBlock.add(subtitle);

        JLabel shieldBadge = createPillBadge("Security", new Color(235, 243, 254), COLOR_PRO_BLUE);
        shieldBadge.setIcon(new ProfileVectorIcon(ProfileVectorIcon.Type.SHIELD, 12, COLOR_PRO_BLUE));

        cardHeader.add(titleBlock, BorderLayout.WEST);
        cardHeader.add(shieldBadge, BorderLayout.EAST);

        card.add(cardHeader);
        card.add(Box.createVerticalStrut(14));

        // Status banner for password operation feedback
        passwordBanner = new StatusBanner();
        card.add(passwordBanner);
        card.add(Box.createVerticalStrut(10));

        // Password Inputs with embedded interactive Eye Toggle Buttons
        currentPasswordField = createStyledPasswordField();
        newPasswordField = createStyledPasswordField();
        confirmPasswordField = createStyledPasswordField();

        currentPassToggle = new EyeToggleButton(currentPasswordField);
        newPassToggle = new EyeToggleButton(newPasswordField);
        confirmPassToggle = new EyeToggleButton(confirmPasswordField);

        card.add(createPasswordFieldWrapper("Current Password *", currentPasswordField, currentPassToggle));
        card.add(Box.createVerticalStrut(10));

        card.add(createPasswordFieldWrapper("New Password *", newPasswordField, newPassToggle));
        card.add(Box.createVerticalStrut(10));

        card.add(createPasswordFieldWrapper("Confirm New Password *", confirmPasswordField, confirmPassToggle));
        card.add(Box.createVerticalStrut(8));

        // Show/Hide All Passwords Checkbox Toggle
        showPasswordsCheckbox = new JCheckBox("Show Passwords Plain Text");
        showPasswordsCheckbox.setFont(FONT_HELPER);
        showPasswordsCheckbox.setForeground(COLOR_TEXT_SECONDARY);
        showPasswordsCheckbox.setOpaque(false);
        showPasswordsCheckbox.setFocusPainted(false);
        showPasswordsCheckbox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showPasswordsCheckbox.addActionListener(e -> {
            boolean show = showPasswordsCheckbox.isSelected();
            char echoChar = show ? (char) 0 : '\u2022';
            currentPasswordField.setEchoChar(echoChar);
            newPasswordField.setEchoChar(echoChar);
            confirmPasswordField.setEchoChar(echoChar);
            currentPassToggle.setRevealed(show);
            newPassToggle.setRevealed(show);
            confirmPassToggle.setRevealed(show);
        });
        card.add(showPasswordsCheckbox);
        card.add(Box.createVerticalStrut(12));

        // Password Policy Checklist Container
        JPanel policyBox = new JPanel();
        policyBox.setLayout(new BoxLayout(policyBox, BoxLayout.Y_AXIS));
        policyBox.setBackground(COLOR_READONLY_BG);
        policyBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(10, 14, 10, 14)
        ));
        policyBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel policyHeader = new JLabel("Password Policy Requirements:");
        policyHeader.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 11));
        policyHeader.setForeground(COLOR_DEEP_NAVY);

        JLabel rule1 = new JLabel(" Minimum 8 characters in length");
        rule1.setIcon(new ProfileVectorIcon(ProfileVectorIcon.Type.CHECK, 11, COLOR_SUCCESS));
        rule1.setFont(FONT_HELPER);
        rule1.setForeground(COLOR_TEXT_SECONDARY);

        JLabel rule2 = new JLabel(" Must contain at least one letter (a-z, A-Z)");
        rule2.setIcon(new ProfileVectorIcon(ProfileVectorIcon.Type.CHECK, 11, COLOR_SUCCESS));
        rule2.setFont(FONT_HELPER);
        rule2.setForeground(COLOR_TEXT_SECONDARY);

        JLabel rule3 = new JLabel(" Must contain at least one number (0-9)");
        rule3.setIcon(new ProfileVectorIcon(ProfileVectorIcon.Type.CHECK, 11, COLOR_SUCCESS));
        rule3.setFont(FONT_HELPER);
        rule3.setForeground(COLOR_TEXT_SECONDARY);

        policyBox.add(policyHeader);
        policyBox.add(Box.createVerticalStrut(4));
        policyBox.add(rule1);
        policyBox.add(rule2);
        policyBox.add(rule3);

        card.add(policyBox);
        card.add(Box.createVerticalStrut(18));

        // Buttons Row: Change Password & Clear Fields (Equipped with Vector Icons)
        JPanel passwordBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        passwordBtnRow.setOpaque(false);
        passwordBtnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        clearPasswordBtn = ModernButton.createOutline("Clear", new ProfileVectorIcon(ProfileVectorIcon.Type.CLOSE, 12, COLOR_TEXT_SECONDARY));
        clearPasswordBtn.setToolTipText("Clear all password input fields");
        clearPasswordBtn.addActionListener(e -> clearPasswordFields());

        changePasswordBtn = ModernButton.createPrimary("Change Password", new ProfileVectorIcon(ProfileVectorIcon.Type.LOCK, 14, Color.WHITE), COLOR_DEEP_NAVY);
        changePasswordBtn.setToolTipText("Submit new password to database");
        changePasswordBtn.addActionListener(e -> changeLecturerPassword());

        passwordBtnRow.add(clearPasswordBtn);
        passwordBtnRow.add(changePasswordBtn);

        card.add(passwordBtnRow);

        return card;
    }

    // =========================================================================
    // PROFILE DATA LOADING & REFRESHING
    // =========================================================================

    /**
     * Loads the logged-in lecturer's profile details from the database.
     * If MySQL is unavailable, safely uses cached session data with a diagnostic alert.
     */
    public void loadLecturerProfile() {
        personalInfoBanner.clear();
        passwordBanner.clear();

        try {
            LecturerProfileData data = LecturerProfileLogic.loadLecturerProfile(this.lecturerId);
            this.cachedData = data;
            applyDataToForm(data);
            if (offlineNoticeBanner != null) {
                offlineNoticeBanner.setVisible(false);
            }
        } catch (SQLException | IllegalArgumentException ex) {
            // Database is offline or record not found; fallback gracefully to UserSession
            UserSession session = UserSession.getCurrentSession();
            if (session != null) {
                String name = session.getFullName();
                String username = session.getUsername();
                String email = session.getEmail();
                String designation = session.getDesignation();
                String dept = session.getDepartmentName();
                this.cachedData = new LecturerProfileData(
                        this.lecturerId, username, name, email, "",
                        designation, 1, "ICT", dept, "default_avatar.png", ""
                );
                applyDataToForm(this.cachedData);
            }

            if (offlineNoticeBanner != null) {
                offlineNoticeBanner.setVisible(true);
            }
        }
    }

    /**
     * Refreshes the profile details from the database and notifies the user.
     */
    public void refreshProfileInformation() {
        cancelProfileEditing();
        loadLecturerProfile();
        personalInfoBanner.showSuccess("Profile details refreshed from database.");
    }

    /**
     * Populates all form fields and overview labels with retrieved lecturer data.
     */
    private void applyDataToForm(LecturerProfileData data) {
        if (data == null) return;

        // Overview Header
        overviewNameLabel.setText(data.getLecturerName());
        overviewDesignationBadge.setText(data.getDesignation());
        overviewDeptBadge.setText(data.getDepartmentName());
        overviewIdLabel.setText("Lecturer ID: #" + data.getLecturerId());
        overviewUsernameLabel.setText("Username: @" + data.getUsername());

        // Resolve custom profile photo if uploaded
        this.loadedProfileImage = LecturerProfileLogic.resolveProfileImage(data.getProfilePicture());
        if (this.loadedProfileImage != null) {
            avatarLabel.setText("");
            if (removePhotoBtn != null) {
                removePhotoBtn.setVisible(true);
            }
        } else {
            avatarLabel.setText(data.getInitials());
            if (removePhotoBtn != null) {
                removePhotoBtn.setVisible(false);
            }
        }
        avatarLabel.repaint();

        // Form Fields
        lecturerIdField.setText(String.valueOf(data.getLecturerId()));
        usernameField.setText(data.getUsername());
        departmentField.setText(data.getDepartmentName());
        designationField.setText(data.getDesignation());
        fullNameField.setText(data.getLecturerName());
        emailField.setText(data.getEmail());
        phoneField.setText(data.getPhone());
    }

    /**
     * Opens a file chooser allowing the lecturer to select a new profile photo.
     * Validates and saves the photo to uploads directory, updating users.profile_picture in database.
     */
    public void chooseAndUploadProfilePicture() {
        personalInfoBanner.clear();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Profile Photo");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
                "Image Files (*.png, *.jpg, *.jpeg, *.webp, *.gif, *.bmp)",
                "png", "jpg", "jpeg", "webp", "gif", "bmp"
        ));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            OperationResult op = LecturerProfileLogic.updateProfilePicture(this.lecturerId, selectedFile);

            if (op.isSuccess()) {
                loadLecturerProfile();
                personalInfoBanner.showSuccess(op.getMessage());
            } else {
                personalInfoBanner.showError(op.getMessage());
            }
        }
    }

    /**
     * Resets the profile photo back to the default avatar and re-enables initials display.
     */
    public void resetProfilePictureToDefault() {
        personalInfoBanner.clear();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to remove your custom profile photo and revert to default initials?",
                "Reset Profile Photo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            OperationResult op = LecturerProfileLogic.resetProfilePicture(this.lecturerId);
            if (op.isSuccess()) {
                loadLecturerProfile();
                personalInfoBanner.showSuccess(op.getMessage());
            } else {
                personalInfoBanner.showError(op.getMessage());
            }
        }
    }

    // =========================================================================
    // EDIT MODE TOGGLING & FORM ACTIONS
    // =========================================================================

    /**
     * Toggles personal information editable state.
     * Only permitted fields (Name, Email, Phone) are editable.
     * Identifiers and institutional fields remain strictly read-only.
     *
     * @param enable True to enable editing, False for read-only view
     */
    public void enableProfileEditing(boolean enable) {
        this.isEditing = enable;

        // Editable fields
        fullNameField.setEditable(enable);
        emailField.setEditable(enable);
        phoneField.setEditable(enable);

        // Adjust visual background for editable fields
        Color editableBg = enable ? Color.WHITE : COLOR_READONLY_BG;
        fullNameField.setBackground(editableBg);
        emailField.setBackground(editableBg);
        phoneField.setBackground(editableBg);

        // Buttons visibility
        editProfileBtn.setVisible(!enable);
        saveChangesBtn.setVisible(enable);
        cancelBtn.setVisible(enable);

        // Badge update
        if (enable) {
            editModeBadge.setText("Editing Active");
            editModeBadge.setIcon(new ProfileVectorIcon(ProfileVectorIcon.Type.EDIT, 11, new Color(180, 83, 9)));
            editModeBadge.setBackground(new Color(254, 243, 199)); // Soft yellow/orange
            editModeBadge.setForeground(new Color(180, 83, 9));
            fullNameField.requestFocusInWindow();
        } else {
            editModeBadge.setText("Read-Only");
            editModeBadge.setIcon(new ProfileVectorIcon(ProfileVectorIcon.Type.LOCK, 11, new Color(100, 116, 139)));
            editModeBadge.setBackground(new Color(241, 245, 249));
            editModeBadge.setForeground(new Color(100, 116, 139));
        }

        revalidate();
        repaint();
    }

    /**
     * Discards any unsaved edits, restores the last saved values from cachedData,
     * and returns to read-only view.
     */
    public void cancelProfileEditing() {
        personalInfoBanner.clear();
        if (cachedData != null) {
            applyDataToForm(cachedData);
        }
        enableProfileEditing(false);
    }

    /**
     * Validates the personal information form inputs using business logic rules.
     *
     * @return True if all fields satisfy validation
     */
    public boolean validateProfileForm() {
        String name = fullNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();

        ValidationResult res = LecturerProfileLogic.validateProfileForm(this.lecturerId, name, email, phone);
        if (!res.isValid()) {
            personalInfoBanner.showError(res.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Saves updated personal details to MySQL database using PreparedStatement.
     * Updates active UserSession and synchronizes Navbar branding in real-time.
     */
    public void saveProfileChanges() {
        personalInfoBanner.clear();

        // 1. Validate Form Inputs
        if (!validateProfileForm()) {
            return;
        }

        String name = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        // 2. Execute update through business logic layer
        OperationResult res = LecturerProfileLogic.updateProfile(this.lecturerId, name, email, phone);

        if (res.isSuccess()) {
            // Refresh data from database
            try {
                this.cachedData = LecturerProfileLogic.loadLecturerProfile(this.lecturerId);
                applyDataToForm(this.cachedData);
            } catch (SQLException e) {
                // If query fails, use updated local values
                this.cachedData = new LecturerProfileData(
                        this.lecturerId, usernameField.getText(), name, email, phone,
                        designationField.getText(), 1, "ICT", departmentField.getText(),
                        "default_avatar.png", ""
                );
                applyDataToForm(this.cachedData);
            }

            // Synchronize existing Navbar with new lecturer name & initials
            if (parentNavbar != null && cachedData != null) {
                parentNavbar.setLecturer(cachedData.getLecturerName(), cachedData.getInitials());
            }

            enableProfileEditing(false);
            personalInfoBanner.showSuccess("Profile information updated successfully!");
        } else {
            // Preserve entered values and display understandable error message
            personalInfoBanner.showError(res.getMessage());
        }
    }

    // =========================================================================
    // PASSWORD MANAGEMENT ACTIONS
    // =========================================================================

    /**
     * Validates the password inputs.
     *
     * @return True if valid, False otherwise
     */
    public boolean validatePasswordForm() {
        String currentPass = new String(currentPasswordField.getPassword());
        String newPass = new String(newPasswordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());

        ValidationResult res = LecturerProfileLogic.validatePasswordForm(currentPass, newPass, confirmPass);
        if (!res.isValid()) {
            passwordBanner.showError(res.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Changes the lecturer's password after verifying the current password against
     * the database. Clears password inputs safely after completion.
     */
    public void changeLecturerPassword() {
        passwordBanner.clear();

        String currentPass = new String(currentPasswordField.getPassword());
        String newPass = new String(newPasswordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());

        // Validate
        if (!validatePasswordForm()) {
            return;
        }

        // Execute password change in logic layer
        OperationResult res = LecturerProfileLogic.changePassword(this.lecturerId, currentPass, newPass, confirmPass);

        if (res.isSuccess()) {
            clearPasswordFields();
            passwordBanner.showSuccess(res.getMessage());
        } else {
            passwordBanner.showError(res.getMessage());
        }
    }

    /**
     * Clears all password fields and unchecks show-password toggle.
     */
    public void clearPasswordFields() {
        currentPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
        if (showPasswordsCheckbox != null) {
            showPasswordsCheckbox.setSelected(false);
        }
        if (currentPassToggle != null) currentPassToggle.setRevealed(false);
        if (newPassToggle != null) newPassToggle.setRevealed(false);
        if (confirmPassToggle != null) confirmPassToggle.setRevealed(false);

        currentPasswordField.setEchoChar('\u2022');
        newPasswordField.setEchoChar('\u2022');
        confirmPasswordField.setEchoChar('\u2022');
        passwordBanner.clear();
    }

    // =========================================================================
    // UI BUILDER HELPERS
    // =========================================================================

    private JPanel createFormField(String labelText, JTextField field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JLabel label = new JLabel(labelText);
        label.setFont(FONT_FIELD_LABEL);
        label.setForeground(COLOR_TEXT_PRIMARY);

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);

        return panel;
    }

    private JPanel createFormFieldWithLock(String labelText, JTextField field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        labelPanel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(FONT_FIELD_LABEL);
        label.setForeground(COLOR_TEXT_PRIMARY);

        JLabel lockIcon = new JLabel(new ProfileVectorIcon(ProfileVectorIcon.Type.LOCK, 10, COLOR_TEXT_MUTED));
        lockIcon.setToolTipText("System managed read-only field");

        labelPanel.add(label);
        labelPanel.add(lockIcon);

        panel.add(labelPanel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);

        return panel;
    }

    private JPanel createPasswordFieldWrapper(String labelText, JPasswordField field, EyeToggleButton toggleBtn) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JLabel label = new JLabel(labelText);
        label.setFont(FONT_FIELD_LABEL);
        label.setForeground(COLOR_TEXT_PRIMARY);

        // Input container with embedded eye button
        JPanel inputContainer = new JPanel(new BorderLayout(4, 0));
        inputContainer.setOpaque(false);
        inputContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        inputContainer.add(field, BorderLayout.CENTER);
        inputContainer.add(toggleBtn, BorderLayout.EAST);

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(inputContainer);

        return panel;
    }

    private JTextField createStyledTextField(boolean editable) {
        JTextField tf = new JTextField();
        tf.setFont(FONT_INPUT);
        tf.setForeground(COLOR_TEXT_PRIMARY);
        tf.setBackground(editable ? Color.WHITE : COLOR_READONLY_BG);
        tf.setEditable(editable);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_INPUT_BORDER, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return tf;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(FONT_INPUT);
        pf.setForeground(COLOR_TEXT_PRIMARY);
        pf.setBackground(Color.WHITE);
        pf.setEchoChar('\u2022');
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_INPUT_BORDER, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return pf;
    }

    private JLabel createPillBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(FONT_BADGE);
        badge.setForeground(fg);
        badge.setIconTextGap(6);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        return badge;
    }

    // =========================================================================
    // RESPONSIVE SCROLL CONTAINER (Guarantees Full Viewport Width)
    // =========================================================================

    /**
     * Special container implementing Scrollable so the entire panel dynamically
     * tracks and matches the viewport width, eliminating horizontal scrollbars
     * and allowing cards to expand fluidly.
     */
    public static class ResponsiveContentPanel extends JPanel implements Scrollable {
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
            return true; // Forces children to span 100% of viewport width
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    // =========================================================================
    // RESPONSIVE CARDS CONTAINER (2-Column on Desktop / 1-Column on Mobile/Tablet)
    // =========================================================================

    /**
     * Container that automatically toggles between side-by-side (2 columns)
     * and stacked (1 column) based on container width.
     */
    public static class ResponsiveCardsContainer extends JPanel {
        private final JPanel leftCard;
        private final JPanel rightCard;
        private final int gap;
        private final int breakpoint;

        public ResponsiveCardsContainer(JPanel leftCard, JPanel rightCard, int gap, int breakpoint) {
            this.leftCard = leftCard;
            this.rightCard = rightCard;
            this.gap = gap;
            this.breakpoint = breakpoint;

            setOpaque(false);
            setLayout(new LayoutManager() {
                @Override
                public void addLayoutComponent(String name, Component comp) {}

                @Override
                public void removeLayoutComponent(Component comp) {}

                @Override
                public Dimension preferredLayoutSize(Container parent) {
                    Insets insets = parent.getInsets();
                    int availW = parent.getWidth() - insets.left - insets.right;
                    if (availW <= 0) availW = 1000;

                    Dimension d1 = leftCard.getPreferredSize();
                    Dimension d2 = rightCard.getPreferredSize();

                    if (availW >= breakpoint) {
                        int maxH = Math.max(d1.height, d2.height);
                        return new Dimension(availW + insets.left + insets.right, maxH + insets.top + insets.bottom);
                    } else {
                        int totalH = d1.height + d2.height + gap;
                        return new Dimension(availW + insets.left + insets.right, totalH + insets.top + insets.bottom);
                    }
                }

                @Override
                public Dimension minimumLayoutSize(Container parent) {
                    return preferredLayoutSize(parent);
                }

                @Override
                public void layoutContainer(Container parent) {
                    Insets insets = parent.getInsets();
                    int availW = parent.getWidth() - insets.left - insets.right;
                    if (availW <= 0) return;

                    int x = insets.left;
                    int y = insets.top;

                    Dimension d1 = leftCard.getPreferredSize();
                    Dimension d2 = rightCard.getPreferredSize();

                    if (availW >= breakpoint) {
                        // 2-Column Side-by-Side
                        int colW = (availW - gap) / 2;
                        int maxH = Math.max(d1.height, d2.height);

                        leftCard.setBounds(x, y, colW, maxH);
                        rightCard.setBounds(x + colW + gap, y, colW, maxH);
                    } else {
                        // 1-Column Stacked Vertically
                        leftCard.setBounds(x, y, availW, d1.height);
                        rightCard.setBounds(x, y + d1.height + gap, availW, d2.height);
                    }
                }
            });

            add(leftCard);
            add(rightCard);

            // Re-layout immediately upon resize
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    revalidate();
                    repaint();
                }
            });
        }
    }

    // =========================================================================
    // VECTOR ICON IMPLEMENTATION (Crisp, High-DPI, Never Fails)
    // =========================================================================

    /**
     * Crisp Vector Icon rendered with pure Java2D strokes and shapes.
     * Guarantees identical, high-definition appearance on all Windows systems without font bugs.
     */
    public static class ProfileVectorIcon implements Icon {
        public enum Type {
            REFRESH, BACK, CAMERA, CLOSE, EDIT, CHECK, LOCK, SHIELD, EYE_OPEN, EYE_CLOSED
        }

        private final Type type;
        private final int size;
        private Color defaultColor;

        public ProfileVectorIcon(Type type, int size, Color color) {
            this.type = type;
            this.size = size;
            this.defaultColor = color;
        }

        public ProfileVectorIcon(Type type, int size) {
            this(type, size, null);
        }

        public void setColor(Color c) {
            this.defaultColor = c;
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            Color color = defaultColor;
            if (color == null) {
                color = (c != null) ? c.getForeground() : Color.BLACK;
            }
            g2.setColor(color);

            int w = size;
            int h = size;

            switch (type) {
                case REFRESH:
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawArc(x + 2, y + 2, w - 5, h - 5, 45, 275);
                    // Arrowhead
                    Polygon arrow = new Polygon();
                    arrow.addPoint(x + w - 2, y + 1);
                    arrow.addPoint(x + w + 1, y + 6);
                    arrow.addPoint(x + w - 4, y + 5);
                    g2.fill(arrow);
                    break;

                case BACK:
                    g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int midY = y + h / 2;
                    g2.drawLine(x + 3, midY, x + w - 2, midY);
                    g2.drawLine(x + 3, midY, x + 7, midY - 4);
                    g2.drawLine(x + 3, midY, x + 7, midY + 4);
                    break;

                case CAMERA:
                    g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    // Body
                    g2.drawRoundRect(x + 1, y + 4, w - 3, h - 5, 3, 3);
                    // Lens
                    int lensD = Math.max(4, w / 2);
                    g2.drawOval(x + (w - lensD) / 2, y + 4 + (h - 5 - lensD) / 2, lensD, lensD);
                    // Top flash notch
                    g2.fillRect(x + w / 2 - 2, y + 2, 4, 2);
                    break;

                case EDIT:
                    g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Path2D pencil = new Path2D.Float();
                    pencil.moveTo(x + 2, y + h - 2);
                    pencil.lineTo(x + 5, y + h - 2);
                    pencil.lineTo(x + w - 2, y + 5);
                    pencil.lineTo(x + w - 5, y + 2);
                    pencil.lineTo(x + 2, y + h - 5);
                    pencil.closePath();
                    g2.draw(pencil);
                    g2.drawLine(x + 2, y + h - 2, x + 5, y + h - 4);
                    break;

                case CHECK:
                    g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(x + 2, y + h / 2 + 1, x + w / 2 - 1, y + h - 3);
                    g2.drawLine(x + w / 2 - 1, y + h - 3, x + w - 2, y + 3);
                    break;

                case CLOSE:
                    g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(x + 3, y + 3, x + w - 3, y + h - 3);
                    g2.drawLine(x + w - 3, y + 3, x + 3, y + h - 3);
                    break;

                case LOCK:
                    g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int shackleW = Math.max(4, w - 6);
                    g2.drawArc(x + 3, y + 1, shackleW, 8, 0, 180);
                    g2.fillRoundRect(x + 2, y + 5, w - 4, h - 6, 3, 3);
                    // White keyhole dot
                    g2.setColor(Color.WHITE);
                    g2.fillOval(x + w / 2 - 1, y + 7, 3, 3);
                    break;

                case SHIELD:
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Path2D shield = new Path2D.Float();
                    shield.moveTo(x + w / 2.0f, y + 1);
                    shield.lineTo(x + w - 2, y + 3);
                    shield.quadTo(x + w - 2, y + h * 0.65f, x + w / 2.0f, y + h - 1);
                    shield.quadTo(x + 2, y + h * 0.65f, x + 2, y + 3);
                    shield.closePath();
                    g2.draw(shield);
                    g2.drawLine(x + 4, y + h / 2, x + w / 2 - 1, y + h / 2 + 2);
                    g2.drawLine(x + w / 2 - 1, y + h / 2 + 2, x + w - 4, y + h / 2 - 2);
                    break;

                case EYE_OPEN:
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawArc(x + 1, y + 3, w - 2, h - 6, 25, 130);
                    g2.drawArc(x + 1, y + 3, w - 2, h - 6, 205, 130);
                    g2.fillOval(x + w / 2 - 2, y + h / 2 - 2, 5, 5);
                    break;

                case EYE_CLOSED:
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawArc(x + 1, y + 3, w - 2, h - 6, 205, 130);
                    g2.drawLine(x + 2, y + 2, x + w - 2, y + h - 2);
                    break;
            }

            g2.dispose();
        }
    }

    // =========================================================================
    // EYE TOGGLE BUTTON (Show / Hide Password)
    // =========================================================================

    public static class EyeToggleButton extends JButton {
        private final JPasswordField targetField;
        private boolean revealed = false;

        public EyeToggleButton(JPasswordField targetField) {
            this.targetField = targetField;
            setPreferredSize(new Dimension(36, 36));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setToolTipText("Show/hide password");
            setIcon(new ProfileVectorIcon(ProfileVectorIcon.Type.EYE_OPEN, 16, COLOR_TEXT_SECONDARY));

            addActionListener(e -> toggle());
        }

        public void toggle() {
            setRevealed(!revealed);
        }

        public void setRevealed(boolean rev) {
            this.revealed = rev;
            targetField.setEchoChar(revealed ? (char) 0 : '\u2022');
            setIcon(new ProfileVectorIcon(revealed ? ProfileVectorIcon.Type.EYE_CLOSED : ProfileVectorIcon.Type.EYE_OPEN, 16, COLOR_TEXT_SECONDARY));
            repaint();
        }
    }

    // =========================================================================
    // MODERN ATTRACTIVE BUTTONS (Gradients, Hover Animations & Vector Icons)
    // =========================================================================

    public static class ModernButton extends JButton {
        private final Color baseColor;
        private final Color hoverColor;
        private final Color textColor;
        private final Color borderColor;
        private final boolean isOutline;
        private boolean isHovered = false;
        private boolean isPressed = false;

        public ModernButton(String text, Icon icon, Color baseColor, Color hoverColor, Color textColor, Color borderColor, boolean isOutline) {
            super(text, icon);
            this.baseColor = baseColor;
            this.hoverColor = hoverColor;
            this.textColor = textColor;
            this.borderColor = borderColor;
            this.isOutline = isOutline;

            setFont(FONT_BUTTON);
            setForeground(textColor);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setIconTextGap(8);
            setBorder(new EmptyBorder(9, 18, 9, 18));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    isPressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    isPressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        public static ModernButton createPrimary(String text, Icon icon, Color base) {
            Color hover = base.darker();
            return new ModernButton(text, icon, base, hover, Color.WHITE, null, false);
        }

        public static ModernButton createSuccess(String text, Icon icon) {
            return new ModernButton(text, icon, COLOR_SUCCESS, COLOR_SUCCESS_HOVER, Color.WHITE, null, false);
        }

        public static ModernButton createOutline(String text, Icon icon) {
            return new ModernButton(text, icon, Color.WHITE, new Color(241, 245, 249), COLOR_TEXT_SECONDARY, COLOR_BORDER, true);
        }

        public static ModernButton createSmallPill(String text, Icon icon, Color bg, Color fg, Color border) {
            ModernButton btn = new ModernButton(text, icon, bg, new Color(219, 234, 254), fg, border, true);
            btn.setFont(FONT_BUTTON_SM);
            btn.setBorder(new EmptyBorder(5, 10, 5, 10));
            btn.setIconTextGap(5);
            return btn;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (isOutline) {
                Color fill = isHovered ? hoverColor : baseColor;
                g2.setColor(fill);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 10, 10));

                if (borderColor != null) {
                    g2.setColor(isHovered ? borderColor.darker() : borderColor);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 10, 10));
                }
            } else {
                Color top = isPressed ? baseColor.darker() : (isHovered ? hoverColor : baseColor);
                Color bot = isPressed ? top.darker() : (isHovered ? top.darker() : baseColor.darker());
                GradientPaint gp = new GradientPaint(0, 0, top, 0, h, bot);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 10, 10));

                // Subtle inner top highlight
                g2.setColor(new Color(255, 255, 255, 40));
                g2.drawLine(2, 1, w - 3, 1);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // =========================================================================
    // ROUNDED CARD COMPONENT
    // =========================================================================

    public static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bgColor;
        private final Color borderColor;

        public RoundedPanel(int radius, Color bgColor, Color borderColor) {
            this.radius = radius;
            this.bgColor = bgColor;
            this.borderColor = borderColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, radius, radius));

            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, radius, radius));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // =========================================================================
    // INLINE STATUS BANNER
    // =========================================================================

    public static class StatusBanner extends JPanel {
        private final JLabel msgLabel;
        private final JLabel iconLabel;
        private Color currentBg = COLOR_SUCCESS_BG;
        private Color currentBorder = COLOR_SUCCESS;

        public StatusBanner() {
            setLayout(new BorderLayout(8, 0));
            setOpaque(false);
            setVisible(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setBorder(new EmptyBorder(8, 12, 8, 12));

            iconLabel = new JLabel();
            msgLabel = new JLabel("");
            msgLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));

            add(iconLabel, BorderLayout.WEST);
            add(msgLabel, BorderLayout.CENTER);
        }

        public void showSuccess(String message) {
            currentBg = COLOR_SUCCESS_BG;
            currentBorder = COLOR_SUCCESS;
            iconLabel.setIcon(new ProfileVectorIcon(ProfileVectorIcon.Type.CHECK, 14, new Color(4, 120, 87)));
            msgLabel.setForeground(new Color(4, 120, 87));
            msgLabel.setText(message);
            setVisible(true);
            revalidate();
            repaint();
        }

        public void showError(String message) {
            currentBg = COLOR_DANGER_BG;
            currentBorder = COLOR_DANGER;
            iconLabel.setIcon(new ProfileVectorIcon(ProfileVectorIcon.Type.CLOSE, 14, COLOR_DANGER));
            msgLabel.setForeground(COLOR_DANGER);
            msgLabel.setText(message);
            setVisible(true);
            revalidate();
            repaint();
        }

        public void clear() {
            setVisible(false);
            msgLabel.setText("");
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (isVisible()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(currentBg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(currentBorder);
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
