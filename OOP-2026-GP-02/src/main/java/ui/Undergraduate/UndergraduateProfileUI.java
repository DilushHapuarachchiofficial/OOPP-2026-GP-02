package main.java.ui.Undergraduate;

import main.java.dao.Undergraduate.UndergraduateDAO;
import main.java.model.Undergraduate.Undergraduate;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * TecFAMS - Faculty of Technology Academic Management System
 * Undergraduate Student Profile UI.
 *
 * This UI allows students to:
 * 1. View official Academic Records and Personal & Contact Details.
 * 2. Click "Edit Profile" to open a separate edit panel where they can update:
 *    - Full Name
 *    - Address
 *    - Date of Birth (DOB)
 *    - National ID (NIC)
 *    - Email Address
 *    - Phone Number
 *    - Profile Picture
 *
 * Built with standard, clean, simple Java Swing components.
 */
public class UndergraduateProfileUI extends JPanel {

    // TecFAMS Theme Palette Colors
    private static final Color COLOR_PRIMARY_NAVY = new Color(0x12, 0x3B, 0x70);
    private static final Color COLOR_MAIN_BLUE    = new Color(0x25, 0x63, 0xEB);
    private static final Color COLOR_LIGHT_BLUE   = new Color(0xEA, 0xF2, 0xFF);
    private static final Color COLOR_WHITE        = new Color(0xFF, 0xFF, 0xFF);
    private static final Color COLOR_BG           = new Color(0xF5, 0xF8, 0xFD);
    private static final Color COLOR_TEXT_PRIMARY = new Color(0x17, 0x2B, 0x4D);
    private static final Color COLOR_TEXT_MUTED   = new Color(0x71, 0x80, 0x96);
    private static final Color COLOR_BORDER       = new Color(0xE2, 0xE8, 0xF0);
    private static final Color COLOR_SUCCESS      = new Color(0x10, 0xB9, 0x81);
    private static final Color COLOR_CARD_BG      = new Color(0xFF, 0xFF, 0xFF);

    // Data references
    private final Undergraduate loggedInStudent;
    private final UndergraduateDashboard parentDashboard;
    private final UndergraduateDAO dao;

    // CardLayout to switch between Profile View and Edit Panel
    private CardLayout rootCardLayout;
    private JPanel viewProfilePanel;
    private JPanel editProfilePanel;

    // Banner components
    private JLabel bannerAvatarLabel;
    private JLabel bannerNameLabel;
    private JLabel bannerRegNoLabel;
    private JLabel bannerDeptLabel;

    // Academic Details Labels (Left Card - Official University Info)
    private JLabel valFullName;
    private JLabel valRegNo;
    private JLabel valStudentId;
    private JLabel valDepartment;
    private JLabel valDegree;
    private JLabel valBatch;
    private JLabel valEnrollmentYear;
    private JLabel valAcademicStatus;

    // Personal & Contact Details Labels (Right Card - Student Info)
    private JLabel valPersonalName;
    private JLabel valNic;
    private JLabel valDob;
    private JLabel valAddress;
    private JLabel valEmail;
    private JLabel valPhone;
    private JLabel valUsername;
    private JLabel valUserId;

    // Separate Edit Profile Form Inputs
    private JTextField txtEditName;
    private JTextField txtEditNic;
    private JTextField txtEditDob;
    private JTextField txtEditAddress;
    private JTextField txtEditEmail;
    private JTextField txtEditPhone;
    private JTextField txtEditProfilePic;
    private JLabel editAvatarPreviewLabel;
    private JLabel editStatusNotice;

    /**
     * Primary constructor for embedding inside UndergraduateDashboard.
     */
    public UndergraduateProfileUI(Undergraduate student, UndergraduateDashboard parentDashboard) {
        this.loggedInStudent = (student != null) ? student : createPreviewStudent();
        this.parentDashboard = parentDashboard;
        this.dao = new UndergraduateDAO();

        initializeUI();
        loadProfileData();
    }

    public UndergraduateProfileUI(Undergraduate student) {
        this(student, null);
    }

    public UndergraduateProfileUI() {
        this(null, null);
    }

    /**
     * Initializes the root CardLayout and registers the View and Edit panels.
     */
    private void initializeUI() {
        rootCardLayout = new CardLayout();
        setLayout(rootCardLayout);
        setBackground(COLOR_BG);

        // 1. Profile View Panel
        viewProfilePanel = createViewProfilePanel();
        add(viewProfilePanel, "VIEW");

        // 2. Separate Edit Profile Panel
        editProfilePanel = createEditProfilePanel();
        add(editProfilePanel, "EDIT");

        // Show view panel by default
        rootCardLayout.show(this, "VIEW");
    }

    // ==========================================
    // 1. MAIN PROFILE VIEW PANEL
    // ==========================================
    private JPanel createViewProfilePanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(COLOR_BG);
        contentPanel.setBorder(new EmptyBorder(18, 26, 26, 26));

        // 1. Top Header Bar
        contentPanel.add(createHeaderBar());
        contentPanel.add(Box.createVerticalStrut(14));

        // 2. Profile Hero Banner (Avatar, Name, Reg No, Department)
        contentPanel.add(createProfileHeroCard());
        contentPanel.add(Box.createVerticalStrut(18));

        // 3. Detailed Info Section (Academic Records & Personal Details)
        contentPanel.add(createDetailedInfoSection());
        contentPanel.add(Box.createVerticalStrut(18));

        // 4. Account Security Card
        contentPanel.add(createSecurityInfoCard());

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(COLOR_BG);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(COLOR_BG);
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(3000, 50));

        JPanel leftBox = new JPanel(new GridLayout(2, 1, 0, 2));
        leftBox.setOpaque(false);

        JLabel titleLbl = new JLabel("My Profile");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(COLOR_PRIMARY_NAVY);

        JLabel subtitleLbl = new JLabel("Faculty of Technology  •  Student Academic Profile & Personal Details");
        subtitleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLbl.setForeground(COLOR_TEXT_MUTED);

        leftBox.add(titleLbl);
        leftBox.add(subtitleLbl);
        header.add(leftBox, BorderLayout.WEST);

        // Right side buttons
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBox.setOpaque(false);

        JButton btnEditHeader = createStyledButton("Edit Profile", COLOR_MAIN_BLUE, COLOR_WHITE);
        btnEditHeader.addActionListener(e -> showEditProfilePanel());
        rightBox.add(btnEditHeader);

        if (parentDashboard != null) {
            JButton backBtn = createStyledButton("Back to Dashboard", COLOR_LIGHT_BLUE, COLOR_MAIN_BLUE);
            backBtn.addActionListener(e -> parentDashboard.showDashboardView());
            rightBox.add(backBtn);
        }

        header.add(rightBox, BorderLayout.EAST);
        return header;
    }

    private JPanel createProfileHeroCard() {
        JPanel card = new JPanel(new BorderLayout(20, 0));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(18, 22, 18, 22)
        ));
        card.setMaximumSize(new Dimension(3000, 115));

        bannerAvatarLabel = new JLabel();
        bannerAvatarLabel.setPreferredSize(new Dimension(72, 72));
        card.add(bannerAvatarLabel, BorderLayout.WEST);

        // Center Box: 3 rows grid so that every element is strictly left-aligned
        JPanel centerBox = new JPanel(new GridLayout(3, 1, 0, 4));
        centerBox.setOpaque(false);

        bannerNameLabel = new JLabel("Undergraduate Student");
        bannerNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        bannerNameLabel.setForeground(COLOR_PRIMARY_NAVY);

        JPanel subInfoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        subInfoRow.setOpaque(false);

        bannerRegNoLabel = new JLabel("Reg No: TG/2024/2085");
        bannerRegNoLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        bannerRegNoLabel.setForeground(COLOR_MAIN_BLUE);

        bannerDeptLabel = new JLabel("   •   Department of Information and Communication Technology");
        bannerDeptLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bannerDeptLabel.setForeground(COLOR_TEXT_MUTED);

        subInfoRow.add(bannerRegNoLabel);
        subInfoRow.add(bannerDeptLabel);

        JPanel badgesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        badgesRow.setOpaque(false);
        badgesRow.add(createPillBadge("Active Student", new Color(0xDC, 0xFC, 0xE7), new Color(0x16, 0x65, 0x34)));
        badgesRow.add(createPillBadge("BICT (Hons)", new Color(0xEF, 0xF6, 0xFF), new Color(0x1D, 0x4E, 0xD8)));
        badgesRow.add(createPillBadge("Level II • Semester I", new Color(0xF3, 0xE8, 0xFF), new Color(0x6B, 0x21, 0xA8)));

        centerBox.add(bannerNameLabel);
        centerBox.add(subInfoRow);
        centerBox.add(badgesRow);

        card.add(centerBox, BorderLayout.CENTER);
        return card;
    }

    private JPanel createDetailedInfoSection() {
        JPanel section = new JPanel(new GridLayout(1, 2, 18, 0));
        section.setOpaque(false);
        section.setMaximumSize(new Dimension(3000, 390));

        // Left Column: Academic Records (Official - Read Only)
        section.add(createAcademicDetailsCard());

        // Right Column: Personal & Contact Details (With Edit Profile button)
        section.add(createPersonalDetailsCard());

        return section;
    }

    private JPanel createAcademicDetailsCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JPanel cardHeader = new JPanel(new GridLayout(2, 1, 0, 2));
        cardHeader.setOpaque(false);
        JLabel title = new JLabel("Academic Records");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(COLOR_PRIMARY_NAVY);
        JLabel subtitle = new JLabel("Official university records (Managed by Academic Division)");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(COLOR_TEXT_MUTED);
        cardHeader.add(title);
        cardHeader.add(subtitle);
        card.add(cardHeader, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(8, 1, 0, 6));
        body.setOpaque(false);

        valFullName = new JLabel("-");
        valRegNo = new JLabel("-");
        valStudentId = new JLabel("-");
        valDepartment = new JLabel("-");
        valDegree = new JLabel("Bachelor of Information and Communication Technology (Hons)");
        valBatch = new JLabel("-");
        valEnrollmentYear = new JLabel("-");
        valAcademicStatus = new JLabel("Active Undergraduate Student (Full-time)");

        body.add(createFieldRow("Full Name:", valFullName));
        body.add(createFieldRow("Registration No:", valRegNo));
        body.add(createFieldRow("Student ID:", valStudentId));
        body.add(createFieldRow("Department:", valDepartment));
        body.add(createFieldRow("Degree Program:", valDegree));
        body.add(createFieldRow("Academic Batch:", valBatch));
        body.add(createFieldRow("Enrollment Year:", valEnrollmentYear));
        body.add(createFieldRow("Academic Status:", valAcademicStatus));

        card.add(body, BorderLayout.CENTER);

        JPanel footerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        footerRow.setOpaque(false);
        JLabel footerNote = new JLabel("To request official academic corrections, contact the Dean's Office.");
        footerNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        footerNote.setForeground(COLOR_TEXT_MUTED);
        footerRow.add(footerNote);
        card.add(footerRow, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createPersonalDetailsCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JPanel cardHeader = new JPanel(new GridLayout(2, 1, 0, 2));
        cardHeader.setOpaque(false);
        JLabel title = new JLabel("Personal & Contact Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(COLOR_PRIMARY_NAVY);
        JLabel subtitle = new JLabel("Identity, contact information, and address records");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(COLOR_TEXT_MUTED);
        cardHeader.add(title);
        cardHeader.add(subtitle);
        card.add(cardHeader, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(8, 1, 0, 6));
        body.setOpaque(false);

        valPersonalName = new JLabel("-");
        valNic = new JLabel("-");
        valDob = new JLabel("-");
        valAddress = new JLabel("-");
        valEmail = new JLabel("-");
        valPhone = new JLabel("-");
        valUsername = new JLabel("-");
        valUserId = new JLabel("-");

        body.add(createFieldRow("Full Name:", valPersonalName));
        body.add(createFieldRow("National ID (NIC):", valNic));
        body.add(createFieldRow("Date of Birth:", valDob));
        body.add(createFieldRow("Home Address:", valAddress));
        body.add(createFieldRow("Email Address:", valEmail));
        body.add(createFieldRow("Phone Number:", valPhone));
        body.add(createFieldRow("Portal Username:", valUsername));
        body.add(createFieldRow("User Account ID:", valUserId));

        card.add(body, BorderLayout.CENTER);

        // Edit Profile Button (clean text without any broken icon)
        JPanel footerRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        footerRow.setOpaque(false);

        JButton btnChangePass = createStyledButton("Change Password", COLOR_LIGHT_BLUE, COLOR_MAIN_BLUE);
        btnChangePass.addActionListener(e -> showChangePasswordDialog());
        footerRow.add(btnChangePass);

        JButton btnEditProfile = createStyledButton("Edit Profile", COLOR_MAIN_BLUE, COLOR_WHITE);
        btnEditProfile.addActionListener(e -> showEditProfilePanel());
        footerRow.add(btnEditProfile);

        card.add(footerRow, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createFieldRow(String labelText, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(COLOR_TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(135, 22));

        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        valueLabel.setForeground(COLOR_TEXT_PRIMARY);

        row.add(lbl, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.CENTER);
        return row;
    }

    private JPanel createSecurityInfoCard() {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));
        card.setMaximumSize(new Dimension(3000, 75));

        JPanel leftBox = new JPanel(new GridLayout(2, 1, 0, 2));
        leftBox.setOpaque(false);

        JLabel title = new JLabel("System Privileges & Account Security");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(COLOR_PRIMARY_NAVY);

        JLabel desc = new JLabel("Role: Undergraduate Student  •  Session: Authenticated via TecFAMS Local Database  •  Privacy Protected");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        desc.setForeground(COLOR_TEXT_MUTED);

        leftBox.add(title);
        leftBox.add(desc);
        card.add(leftBox, BorderLayout.CENTER);

        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBox.setOpaque(false);

        JLabel badge = createPillBadge("Security Level: Student", new Color(0xEA, 0xF2, 0xFF), COLOR_MAIN_BLUE);
        rightBox.add(badge);

        JButton btnChangePassSec = createStyledButton("Change Password", COLOR_MAIN_BLUE, COLOR_WHITE);
        btnChangePassSec.addActionListener(e -> showChangePasswordDialog());
        rightBox.add(btnChangePassSec);

        card.add(rightBox, BorderLayout.EAST);

        return card;
    }

    // ==========================================
    // 2. SEPARATE EDIT PROFILE PANEL
    // ==========================================
    private JPanel createEditProfilePanel() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(COLOR_BG);
        scrollContent.setBorder(new EmptyBorder(18, 30, 30, 30));

        // 1. Header Bar for Edit Panel
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setOpaque(false);
        headerBar.setMaximumSize(new Dimension(3000, 50));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Edit Student Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(COLOR_PRIMARY_NAVY);
        JLabel subtitle = new JLabel("Update your personal details, home address, contact numbers, and profile photo");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(COLOR_TEXT_MUTED);
        titleBox.add(title);
        titleBox.add(subtitle);
        headerBar.add(titleBox, BorderLayout.WEST);

        JButton btnBack = createStyledButton("Back to Profile", COLOR_LIGHT_BLUE, COLOR_MAIN_BLUE);
        btnBack.addActionListener(e -> showViewProfilePanel());
        headerBar.add(btnBack, BorderLayout.EAST);

        scrollContent.add(headerBar);
        scrollContent.add(Box.createVerticalStrut(18));

        // 2. Form Card
        JPanel formCard = new JPanel(new BorderLayout(0, 16));
        formCard.setBackground(COLOR_CARD_BG);
        formCard.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(22, 26, 22, 26)
        ));
        formCard.setMaximumSize(new Dimension(860, 680));
        formCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setOpaque(false);

        txtEditName = new JTextField();
        styleInputField(txtEditName);

        txtEditNic = new JTextField();
        styleInputField(txtEditNic);

        txtEditDob = new JTextField();
        styleInputField(txtEditDob);

        txtEditAddress = new JTextField();
        styleInputField(txtEditAddress);

        txtEditEmail = new JTextField();
        styleInputField(txtEditEmail);

        txtEditPhone = new JTextField();
        styleInputField(txtEditPhone);

        txtEditProfilePic = new JTextField();
        styleInputField(txtEditProfilePic);

        fieldsPanel.add(createFormRow("Full Name *", txtEditName, "Your full name as registered in the university"));
        fieldsPanel.add(Box.createVerticalStrut(10));

        fieldsPanel.add(createFormRow("National ID (NIC)", txtEditNic, "National Identity Card number (e.g. 200113401237)"));
        fieldsPanel.add(Box.createVerticalStrut(10));

        fieldsPanel.add(createFormRow("Date of Birth (DOB)", txtEditDob, "Format: YYYY-MM-DD (e.g. 2001-11-25)"));
        fieldsPanel.add(Box.createVerticalStrut(10));

        fieldsPanel.add(createFormRow("Home Address", txtEditAddress, "Current residential or permanent address"));
        fieldsPanel.add(Box.createVerticalStrut(10));

        fieldsPanel.add(createFormRow("Email Address *", txtEditEmail, "Active contact email (e.g. student@fot.ruh.ac.lk)"));
        fieldsPanel.add(Box.createVerticalStrut(10));

        fieldsPanel.add(createFormRow("Phone Number *", txtEditPhone, "Contact mobile number (e.g. 0710001004)"));
        fieldsPanel.add(Box.createVerticalStrut(10));

        // Profile Picture with File Chooser Row
        fieldsPanel.add(createProfilePicChooserRow());
        fieldsPanel.add(Box.createVerticalStrut(14));

        editStatusNotice = new JLabel(" ");
        editStatusNotice.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        editStatusNotice.setForeground(COLOR_TEXT_MUTED);
        fieldsPanel.add(editStatusNotice);

        formCard.add(fieldsPanel, BorderLayout.CENTER);

        // Action Buttons Row (clean text)
        JPanel actionsRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsRow.setOpaque(false);

        JButton btnCancel = createStyledButton("Cancel", new Color(0xF3, 0xF4, 0xF6), COLOR_TEXT_PRIMARY);
        btnCancel.addActionListener(e -> showViewProfilePanel());

        JButton btnSave = createStyledButton("Save Changes", COLOR_SUCCESS, COLOR_WHITE);
        btnSave.addActionListener(e -> saveProfileChanges());

        JButton btnChangePassInEdit = createStyledButton("Change Password", COLOR_LIGHT_BLUE, COLOR_MAIN_BLUE);
        btnChangePassInEdit.addActionListener(e -> showChangePasswordDialog());
        actionsRow.add(btnChangePassInEdit);
        actionsRow.add(btnCancel);
        actionsRow.add(btnSave);
        formCard.add(actionsRow, BorderLayout.SOUTH);

        scrollContent.add(formCard);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(COLOR_BG);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(COLOR_BG);
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createFormRow(String labelText, JTextField inputField, String hintText) {
        JPanel row = new JPanel(new BorderLayout(12, 4));
        row.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(COLOR_TEXT_PRIMARY);
        lbl.setPreferredSize(new Dimension(150, 28));

        row.add(lbl, BorderLayout.WEST);
        row.add(inputField, BorderLayout.CENTER);

        if (hintText != null && !hintText.isEmpty()) {
            JLabel hint = new JLabel(hintText);
            hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            hint.setForeground(COLOR_TEXT_MUTED);
            hint.setBorder(new EmptyBorder(0, 162, 0, 0));
            row.add(hint, BorderLayout.SOUTH);
        }

        return row;
    }

    private JPanel createProfilePicChooserRow() {
        JPanel panel = new JPanel(new BorderLayout(12, 4));
        panel.setOpaque(false);

        JLabel lbl = new JLabel("Profile Picture");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(COLOR_TEXT_PRIMARY);
        lbl.setPreferredSize(new Dimension(150, 24));

        JPanel rightBox = new JPanel(new BorderLayout(8, 0));
        rightBox.setOpaque(false);

        editAvatarPreviewLabel = new JLabel();
        editAvatarPreviewLabel.setPreferredSize(new Dimension(48, 48));
        rightBox.add(editAvatarPreviewLabel, BorderLayout.WEST);

        JPanel inputWithBtn = new JPanel(new BorderLayout(6, 0));
        inputWithBtn.setOpaque(false);
        inputWithBtn.add(txtEditProfilePic, BorderLayout.CENTER);

        JButton btnBrowse = createStyledButton("Browse Image...", COLOR_LIGHT_BLUE, COLOR_MAIN_BLUE);
        btnBrowse.addActionListener(e -> chooseImageFile());
        inputWithBtn.add(btnBrowse, BorderLayout.EAST);

        rightBox.add(inputWithBtn, BorderLayout.CENTER);

        panel.add(lbl, BorderLayout.WEST);
        panel.add(rightBox, BorderLayout.CENTER);

        JLabel hint = new JLabel("Select an image file (PNG, JPG) from your computer to update avatar");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(COLOR_TEXT_MUTED);
        hint.setBorder(new EmptyBorder(0, 162, 0, 0));
        panel.add(hint, BorderLayout.SOUTH);

        return panel;
    }

    private void chooseImageFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Profile Picture");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files (*.png, *.jpg, *.jpeg)", "png", "jpg", "jpeg");
        chooser.setFileFilter(filter);

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            txtEditProfilePic.setText(selectedFile.getAbsolutePath());
            updateAvatarPreview(selectedFile.getAbsolutePath(), txtEditName.getText());
        }
    }

    private void updateAvatarPreview(String path, String name) {
        if (editAvatarPreviewLabel != null) {
            editAvatarPreviewLabel.setIcon(renderAvatar(48, path, name));
        }
    }

    private void styleInputField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(COLOR_TEXT_PRIMARY);
        field.setBackground(COLOR_WHITE);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 32));
        field.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(5, 8, 5, 8)
        ));
    }

    // ==========================================
    // 3. SWITCHING & DATA LOGIC
    // ==========================================
    public void showEditProfilePanel() {
        // Populate inputs with current student details
        txtEditName.setText(loggedInStudent.getName() != null ? loggedInStudent.getName() : "");
        txtEditNic.setText(loggedInStudent.getNic() != null ? loggedInStudent.getNic() : "");
        txtEditDob.setText(loggedInStudent.getDob() != null ? loggedInStudent.getDob() : "");
        txtEditAddress.setText(loggedInStudent.getAddress() != null ? loggedInStudent.getAddress() : "");
        txtEditEmail.setText(loggedInStudent.getEmail() != null ? loggedInStudent.getEmail() : "");
        txtEditPhone.setText(loggedInStudent.getPhone() != null ? loggedInStudent.getPhone() : "");
        txtEditProfilePic.setText(loggedInStudent.getProfilePicture() != null ? loggedInStudent.getProfilePicture() : "");

        updateAvatarPreview(txtEditProfilePic.getText(), txtEditName.getText());
        editStatusNotice.setText("Editing student profile. Click 'Save Changes' to commit to database.");
        editStatusNotice.setForeground(COLOR_MAIN_BLUE);

        rootCardLayout.show(this, "EDIT");
    }

    public void showViewProfilePanel() {
        rootCardLayout.show(this, "VIEW");
    }

    private void saveProfileChanges() {
        String name = txtEditName.getText().trim();
        String nic = txtEditNic.getText().trim();
        String dob = txtEditDob.getText().trim();
        String address = txtEditAddress.getText().trim();
        String email = txtEditEmail.getText().trim();
        String phone = txtEditPhone.getText().trim();
        String pic = txtEditProfilePic.getText().trim();

        // Validation
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Full Name cannot be empty.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            txtEditName.requestFocus();
            return;
        }

        if (email.isEmpty() || !email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid email address (e.g. student@fot.ruh.ac.lk).",
                    "Invalid Email",
                    JOptionPane.WARNING_MESSAGE);
            txtEditEmail.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Phone number cannot be empty.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            txtEditPhone.requestFocus();
            return;
        }

        if (!dob.isEmpty() && !dob.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this,
                    "Please enter Date of Birth in YYYY-MM-DD format (e.g. 2001-11-25).",
                    "Invalid Date of Birth",
                    JOptionPane.WARNING_MESSAGE);
            txtEditDob.requestFocus();
            return;
        }

        String studentKey = (loggedInStudent.getRegistrationNo() != null && !loggedInStudent.getRegistrationNo().isEmpty())
                ? loggedInStudent.getRegistrationNo() : loggedInStudent.getStudentId();

        // 1. Update in Database via DAO
        boolean dbSuccess = dao.updateFullProfile(studentKey, name, email, phone, address, nic, dob, pic);

        // 2. Update model in memory
        loggedInStudent.updateFullProfile(name, email, phone, address, nic, dob, pic);

        if (dbSuccess) {
            JOptionPane.showMessageDialog(this,
                    "Profile details successfully updated in the system database!",
                    "Profile Updated",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Profile details updated for current session.\n(Note: Database update could not be committed)",
                    "Profile Updated (Session)",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        // Refresh View labels and avatars
        loadProfileData();

        // Notify parent dashboard to update header, overview, and other pages
        if (parentDashboard != null) {
            parentDashboard.notifyProfileUpdated();
        }

        // Return to View panel
        showViewProfilePanel();
    }

    /**
     * Loads student profile data from the database or the loggedInStudent model.
     */
    public void loadProfileData() {
        if (loggedInStudent == null) return;

        String studentKey = (loggedInStudent.getRegistrationNo() != null && !loggedInStudent.getRegistrationNo().isEmpty())
                ? loggedInStudent.getRegistrationNo() : loggedInStudent.getStudentId();
        if (studentKey == null || studentKey.trim().isEmpty()) {
            studentKey = "TG/2024/2085";
        }

        // Fetch latest live details from database
        try {
            Undergraduate dbStudent = dao.getUndergraduateByStudentId(studentKey);
            if (dbStudent != null) {
                if (dbStudent.getName() != null) loggedInStudent.setName(dbStudent.getName());
                if (dbStudent.getEmail() != null) loggedInStudent.setEmail(dbStudent.getEmail());
                if (dbStudent.getPhone() != null) loggedInStudent.setPhone(dbStudent.getPhone());
                if (dbStudent.getDepartment() != null) loggedInStudent.setDepartment(dbStudent.getDepartment());
                if (dbStudent.getBatch() != null) loggedInStudent.setBatch(dbStudent.getBatch());
                if (dbStudent.getRegistrationNo() != null) loggedInStudent.setRegistrationNo(dbStudent.getRegistrationNo());
                if (dbStudent.getEnrollmentYear() > 0) loggedInStudent.setEnrollmentYear(dbStudent.getEnrollmentYear());
                if (dbStudent.getNic() != null) loggedInStudent.setNic(dbStudent.getNic());
                if (dbStudent.getDob() != null) loggedInStudent.setDob(dbStudent.getDob());
                if (dbStudent.getAddress() != null) loggedInStudent.setAddress(dbStudent.getAddress());
                if (dbStudent.getProfilePicture() != null) loggedInStudent.setProfilePicture(dbStudent.getProfilePicture());
            }
        } catch (Exception e) {
            System.err.println("Notice: Using memory student profile: " + e.getMessage());
        }

        // Populate Banner
        String name = loggedInStudent.getName() != null ? loggedInStudent.getName() : "Student";
        String regNo = loggedInStudent.getRegistrationNo() != null ? loggedInStudent.getRegistrationNo() : "TG/2024/2085";
        String dept = loggedInStudent.getDepartment() != null ? loggedInStudent.getDepartment() : "Department of Information and Communication Technology";

        bannerNameLabel.setText(name);
        bannerRegNoLabel.setText("Reg No: " + regNo);
        bannerDeptLabel.setText("   •   " + dept);
        bannerAvatarLabel.setIcon(renderAvatar(72, loggedInStudent.getProfilePicture(), name));

        // Populate Academic Details (Left Card)
        valFullName.setText(name);
        valRegNo.setText(regNo);
        valStudentId.setText(loggedInStudent.getStudentId() != null ? loggedInStudent.getStudentId() : "1085");
        valDepartment.setText(dept);
        valBatch.setText(loggedInStudent.getBatch() != null ? loggedInStudent.getBatch() : "Batch 2023/2024");
        valEnrollmentYear.setText(String.valueOf(loggedInStudent.getEnrollmentYear() > 0 ? loggedInStudent.getEnrollmentYear() : 2024));

        // Populate Personal & Contact Details (Right Card)
        valPersonalName.setText(name);
        valNic.setText(loggedInStudent.getNic() != null && !loggedInStudent.getNic().isEmpty() ? loggedInStudent.getNic() : "-");
        valDob.setText(loggedInStudent.getDob() != null && !loggedInStudent.getDob().isEmpty() ? loggedInStudent.getDob() : "-");
        valAddress.setText(loggedInStudent.getAddress() != null && !loggedInStudent.getAddress().isEmpty() ? loggedInStudent.getAddress() : "-");
        valEmail.setText(loggedInStudent.getEmail() != null ? loggedInStudent.getEmail() : "-");
        valPhone.setText(loggedInStudent.getPhone() != null ? loggedInStudent.getPhone() : "-");
        valUsername.setText(loggedInStudent.getUsername() != null ? loggedInStudent.getUsername() : "tg2085");
        valUserId.setText(loggedInStudent.getUserId() != null ? loggedInStudent.getUserId() : "USR-1085");

        revalidate();
        repaint();
    }

    // ==========================================
    // 4. UI HELPER METHODS & AVATAR
    // ==========================================
    private Icon renderAvatar(int size, String imagePath, String name) {
        if (imagePath != null && !imagePath.trim().isEmpty() && !imagePath.equalsIgnoreCase("default_avatar.png")) {
            File f = new File(imagePath.trim());
            if (f.exists() && f.isFile()) {
                try {
                    ImageIcon icon = new ImageIcon(f.getAbsolutePath());
                    Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                    return new ImageIcon(img);
                } catch (Exception ignored) {}
            }
        }
        return createCircularInitialsAvatar(size, name);
    }

    private JLabel createPillBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(fg);
        badge.setBackground(bg);
        badge.setOpaque(true);
        badge.setBorder(new CompoundBorder(
                new LineBorder(bg, 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));
        return badge;
    }

    private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        return btn;
    }

    private Icon createCircularInitialsAvatar(int size, String name) {
        String initials = "ST";
        if (name != null && !name.trim().isEmpty()) {
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) {
                initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
            } else if (parts[0].length() >= 2) {
                initials = parts[0].substring(0, 2).toUpperCase();
            }
        }
        final String text = initials;

        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));

                // Navy circle
                g2.setColor(COLOR_PRIMARY_NAVY);
                g2.fillOval(x, y, size, size);

                // Outer border accent ring
                g2.setColor(new Color(0x3B, 0x82, 0xF6));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(x + 1, y + 1, size - 2, size - 2);

                // Initials Text
                g2.setColor(COLOR_WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, size / 2 - 2));
                FontMetrics fm = g2.getFontMetrics();
                int tx = x + (size - fm.stringWidth(text)) / 2;
                int ty = y + ((size - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(text, tx, ty);

                g2.dispose();
            }

            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    
    // ==========================================
    // 5. CHANGE PASSWORD DIALOG
    // ==========================================
    private void showChangePasswordDialog() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow != null ? (Frame) parentWindow : null, "Change Account Password", true);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(COLOR_WHITE);
        content.setBorder(new EmptyBorder(22, 26, 22, 26));

        JLabel headerLbl = new JLabel("Change Account Password");
        headerLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLbl.setForeground(COLOR_PRIMARY_NAVY);
        headerLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(headerLbl);

        JLabel subLbl = new JLabel("Enter your current password and choose a secure new password.");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(COLOR_TEXT_MUTED);
        subLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(subLbl);
        content.add(Box.createVerticalStrut(18));

        JPasswordField txtCurrent = new JPasswordField();
        styleInputField(txtCurrent);
        txtCurrent.setPreferredSize(new Dimension(380, 36));

        JPasswordField txtNew = new JPasswordField();
        styleInputField(txtNew);
        txtNew.setPreferredSize(new Dimension(380, 36));

        JPasswordField txtConfirm = new JPasswordField();
        styleInputField(txtConfirm);
        txtConfirm.setPreferredSize(new Dimension(380, 36));

        content.add(createPasswordRow("Current Password *", txtCurrent));
        content.add(Box.createVerticalStrut(12));
        content.add(createPasswordRow("New Password *", txtNew));
        content.add(Box.createVerticalStrut(12));
        content.add(createPasswordRow("Confirm New Password *", txtConfirm));
        content.add(Box.createVerticalStrut(12));

        JCheckBox chkShow = new JCheckBox("Show Passwords");
        chkShow.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShow.setForeground(COLOR_TEXT_PRIMARY);
        chkShow.setOpaque(false);
        chkShow.setAlignmentX(Component.LEFT_ALIGNMENT);
        char defaultEcho = txtCurrent.getEchoChar();
        chkShow.addActionListener(e -> {
            char echo = chkShow.isSelected() ? (char) 0 : defaultEcho;
            txtCurrent.setEchoChar(echo);
            txtNew.setEchoChar(echo);
            txtConfirm.setEchoChar(echo);
        });
        content.add(chkShow);
        content.add(Box.createVerticalStrut(18));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnCancel = createStyledButton("Cancel", new Color(0xF3, 0xF4, 0xF6), COLOR_TEXT_PRIMARY);
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnUpdate = createStyledButton("Update Password", COLOR_SUCCESS, COLOR_WHITE);
        btnUpdate.addActionListener(e -> {
            String currentPass = new String(txtCurrent.getPassword()).trim();
            String newPass = new String(txtNew.getPassword()).trim();
            String confirmPass = new String(txtConfirm.getPassword()).trim();

            if (currentPass.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter your current password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                txtCurrent.requestFocus();
                return;
            }

            if (newPass.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a new password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                txtNew.requestFocus();
                return;
            }

            if (newPass.length() < 4) {
                JOptionPane.showMessageDialog(dialog, "New password must be at least 4 characters long.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                txtNew.requestFocus();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(dialog, "New password and Confirm password do not match!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                txtConfirm.requestFocus();
                return;
            }

            if (currentPass.equals(newPass)) {
                JOptionPane.showMessageDialog(dialog, "New password cannot be the same as your current password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                txtNew.requestFocus();
                return;
            }

            String studentKey = (loggedInStudent.getRegistrationNo() != null && !loggedInStudent.getRegistrationNo().isEmpty())
                    ? loggedInStudent.getRegistrationNo() : loggedInStudent.getStudentId();
            if (studentKey == null || studentKey.trim().isEmpty()) {
                studentKey = "TG/2024/2085";
            }

            boolean validCurrent = dao.verifyCurrentPassword(studentKey, currentPass);
            if (!validCurrent) {
                JOptionPane.showMessageDialog(dialog, "Current password is incorrect! Please verify and try again.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                txtCurrent.requestFocus();
                return;
            }

            boolean updated = dao.updatePassword(studentKey, newPass);
            if (updated) {
                loggedInStudent.setPassword(newPass);
                JOptionPane.showMessageDialog(dialog, "Your password has been changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Could not update password in database. Please check connection.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRow.add(btnCancel);
        btnRow.add(btnUpdate);
        content.add(btnRow);

        dialog.setContentPane(content);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel createPasswordRow(String labelText, JPasswordField passField) {
        JPanel row = new JPanel(new BorderLayout(8, 2));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(COLOR_TEXT_PRIMARY);

        row.add(lbl, BorderLayout.NORTH);
        row.add(passField, BorderLayout.CENTER);
        return row;
    }

    private static Undergraduate createPreviewStudent() {
        UndergraduateDAO dao = new UndergraduateDAO();
        Undergraduate student = dao.getUndergraduateByStudentId("TG/2024/2085");
        if (student != null) return student;
        return new Undergraduate("21", "tg2085", "", "Dilush Bandara Hapuarachchi",
                "wb.hapu@student.ruh.ac.lk", "0710001004", "",
                "21", "TG/2024/2085", "Batch 2023/2024",
                "Department of Information and Communication Technology", 2024);
    }

    /**
     * Standalone main method to preview and test the Profile UI directly.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TecFAMS - My Profile Preview");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 750);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new UndergraduateProfileUI(createPreviewStudent(), null));
            frame.setVisible(true);
        });
    }
}
