package main.java.ui.TechnicalOfficer;

import main.java.dao.TechnicalOfficer.TechnicalOfficerDAO;
import main.java.model.TechnicalOfficer.TechnicalOfficer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import static main.java.ui.TechnicalOfficer.TechnicalOfficerDashboard.*;

/**
 * TechnicalOfficerProfileForm
 * TecFAMS - Faculty of Technology Academic Management System
 * University of Ruhuna
 *
 * Full standalone UI (JFrame) for Technical Officer Profile management:
 * - Top header with TecFAMS branding, Officer badge, and "← Back to Dashboard" button.
 * - Profile Photo card with circular preview, upload, and format validation.
 * - Editable personal details (Full Name, Official Email, Phone number).
 * - Read-only system identifiers (Officer ID, Username, Department, Role).
 * - Security & Password change section with password validation and database verification.
 * - Seamless integration with TechnicalOfficerDashboard and navigation bar.
 */
public class TechnicalOfficerProfileForm extends JFrame {

    private final JFrame parentDashboard;
    private final TechnicalOfficerDAO dao;
    private final TechnicalOfficer currentOfficer;
    private Runnable onProfileSavedCallback;

    // UI Fields - Editable
    private JTextField txtName;
    private JTextField txtEmail;
    private JTextField txtPhone;

    // UI Fields - Read-Only
    private JTextField txtOfficerId;
    private JTextField txtUsername;
    private JTextField txtDepartment;
    private JTextField txtRole;

    // UI Fields - Password
    private JPasswordField txtCurrentPassword;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;
    private JCheckBox chkShowPasswords;

    // UI Photo Management
    private AvatarPreviewPanel avatarPanel;
    private JLabel lblPhotoStatus;
    private JLabel lblNameCard;
    private JLabel lblEmailCard;

    private BufferedImage currentLoadedPhoto = null;
    private BufferedImage previewPhoto = null;
    private byte[] pendingPhotoBytes = null;

    // Regex Patterns for validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+0-9\\s\\-()]{7,20}$"
    );

    public TechnicalOfficerProfileForm() {
        this(null, null, null);
    }

    public TechnicalOfficerProfileForm(JFrame parentDashboard) {
        this(parentDashboard, null, null);
    }

    public TechnicalOfficerProfileForm(Frame parent, TechnicalOfficer officer, TechnicalOfficerDAO dao) {
        this(parent instanceof JFrame ? (JFrame) parent : null, officer, dao);
    }

    public TechnicalOfficerProfileForm(JFrame parentDashboard, TechnicalOfficer officer, TechnicalOfficerDAO dao) {
        this.parentDashboard = parentDashboard;
        this.dao = dao != null ? dao : new TechnicalOfficerDAO();
        this.currentOfficer = officer != null ? officer : this.dao.getCurrentOfficer();

        initFrameSettings();
        loadExistingPhoto();
        buildUI();
        populateFields();
    }

    private void initFrameSettings() {
        setTitle("TecFAMS - Technical Officer Profile");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1240, 780);
        setMinimumSize(new Dimension(1020, 660));
        setLocationRelativeTo(parentDashboard);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                backToDashboard();
            }
        });
    }

    public void setOnProfileSavedCallback(Runnable callback) {
        this.onProfileSavedCallback = callback;
    }

    private void buildUI() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);

        // 1. Top Header Bar
        getContentPane().add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Content Area (Scrollable balanced 2-column container)
        JPanel contentContainer = new JPanel(new BorderLayout());
        contentContainer.setBackground(BG_COLOR);
        contentContainer.setBorder(new EmptyBorder(18, 24, 18, 24));
        contentContainer.add(createTwoColumnSection(), BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(contentContainer);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BG_COLOR);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // 3. Bottom Action Bar
        getContentPane().add(createBottomBar(), BorderLayout.SOUTH);
    }

    /**
     * Top Header Panel with TecFAMS branding, Officer badge, and Back to Dashboard button.
     */
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(14, 24, 14, 24)
        ));

        // Left Branding & Title
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        JLabel titleLabel = new JLabel("TecFAMS  •  Technical Officer Profile");
        titleLabel.setFont(FONT_HEADER_LARGE);
        titleLabel.setForeground(PRIMARY_NAVY);

        JLabel subLabel = new JLabel("Faculty of Technology • University of Ruhuna | Personal Details & Account Security");
        subLabel.setFont(FONT_SMALL);
        subLabel.setForeground(TEXT_SECONDARY);
        left.add(titleLabel);
        left.add(subLabel);

        // Right Officer Info & Back Button
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        String offName = (currentOfficer != null && currentOfficer.getName() != null && !currentOfficer.getName().trim().isEmpty())
                ? currentOfficer.getName().trim() : "Technical Officer";
        JLabel officerBadge = new JLabel("Officer: " + offName);
        officerBadge.setFont(FONT_SMALL);
        officerBadge.setForeground(PRIMARY_NAVY);
        officerBadge.setBackground(LIGHT_BLUE);
        officerBadge.setOpaque(true);
        officerBadge.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#BFDBFE"), 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));

        JButton btnBack = createSecondaryButton("← Back to Dashboard");
        btnBack.addActionListener(e -> backToDashboard());

        right.add(officerBadge);
        right.add(btnBack);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    /**
     * Profile Photo & Quick Identity Card.
     */
    private JPanel createPhotoCard() {
        JPanel card = createStyledCard();
        card.setLayout(new BorderLayout(20, 0));

        // Left: Avatar Component
        avatarPanel = new AvatarPreviewPanel();
        JPanel avatarWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        avatarWrapper.setOpaque(false);
        avatarWrapper.add(avatarPanel);

        // Right: Information & Photo action
        JPanel photoControls = new JPanel();
        photoControls.setLayout(new BoxLayout(photoControls, BoxLayout.Y_AXIS));
        photoControls.setOpaque(false);

        lblNameCard = new JLabel(currentOfficer != null ? currentOfficer.getName() : "Technical Officer");
        lblNameCard.setFont(FONT_HEADER_LARGE);
        lblNameCard.setForeground(PRIMARY_NAVY);
        lblNameCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblEmailCard = new JLabel(currentOfficer != null ? currentOfficer.getEmail() : "");
        lblEmailCard.setFont(FONT_REGULAR);
        lblEmailCard.setForeground(TEXT_SECONDARY);
        lblEmailCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleBadge = new JLabel("Technical Officer • Faculty Staff");
        roleBadge.setFont(FONT_SMALL);
        roleBadge.setForeground(PRIMARY_BLUE);
        roleBadge.setBackground(LIGHT_BLUE);
        roleBadge.setOpaque(true);
        roleBadge.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#BFDBFE"), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        roleBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnChangePhoto = createPrimaryButton("📷  Change Photo");
        btnChangePhoto.setPreferredSize(new Dimension(160, 36));
        btnChangePhoto.setMaximumSize(new Dimension(160, 36));
        btnChangePhoto.addActionListener(e -> handleChangePhoto());
        btnChangePhoto.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel photoHelp = new JLabel("JPG, JPEG, PNG • Maximum size: 5 MB");
        photoHelp.setFont(FONT_SMALL);
        photoHelp.setForeground(TEXT_SECONDARY);
        photoHelp.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblPhotoStatus = new JLabel("");
        lblPhotoStatus.setFont(FONT_SMALL);
        lblPhotoStatus.setForeground(SUCCESS_GREEN);
        lblPhotoStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        photoControls.add(lblNameCard);
        photoControls.add(Box.createVerticalStrut(4));
        photoControls.add(lblEmailCard);
        photoControls.add(Box.createVerticalStrut(8));
        photoControls.add(roleBadge);
        photoControls.add(Box.createVerticalStrut(14));
        photoControls.add(btnChangePhoto);
        photoControls.add(Box.createVerticalStrut(8));
        photoControls.add(photoHelp);
        photoControls.add(Box.createVerticalStrut(4));
        photoControls.add(lblPhotoStatus);

        card.add(avatarWrapper, BorderLayout.WEST);
        card.add(photoControls, BorderLayout.CENTER);
        return card;
    }

    /**
     * Balanced Two-Column Section:
     * - Left Column: Profile Photo Card + System Identifiers Card (Read-Only)
     * - Right Column: Personal & Contact Details Card (Editable) + Password & Security Card
     */
    private JPanel createTwoColumnSection() {
        JPanel section = new JPanel(new GridLayout(1, 2, 20, 0));
        section.setOpaque(false);

        // Left Column: Photo Card + System Identifiers Card
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);
        leftCol.add(createPhotoCard());
        leftCol.add(Box.createVerticalStrut(18));
        leftCol.add(createReadOnlySystemCard());
        leftCol.add(Box.createVerticalGlue());

        // Right Column: Personal Details Card + Password Management Card
        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setOpaque(false);
        rightCol.add(createPersonalInfoCard());
        rightCol.add(Box.createVerticalStrut(18));
        rightCol.add(createChangePasswordCard());
        rightCol.add(Box.createVerticalGlue());

        section.add(leftCol);
        section.add(rightCol);
        return section;
    }

    /**
     * Card: Editable Personal & Contact Details.
     */
    private JPanel createPersonalInfoCard() {
        JPanel card = createStyledCard();
        card.setLayout(new BorderLayout(0, 14));

        JLabel title = createSectionTitle("1. Personal & Contact Details (Editable ✏️)");
        card.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        txtName = createStyledTextField();
        txtEmail = createStyledTextField();
        txtPhone = createStyledTextField();

        form.add(createFieldBlock("Full Name:", txtName));
        form.add(Box.createVerticalStrut(12));
        form.add(createFieldBlock("Official Email Address:", txtEmail));
        form.add(Box.createVerticalStrut(12));
        form.add(createFieldBlock("Contact Phone Number:", txtPhone));

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    /**
     * Card: Read-Only System Identifiers.
     */
    private JPanel createReadOnlySystemCard() {
        JPanel card = createStyledCard();
        card.setLayout(new BorderLayout(0, 14));

        JLabel title = createSectionTitle("2. University & System Identifiers (Read-Only 🔒)");
        card.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 14, 12));
        grid.setOpaque(false);

        txtOfficerId = createReadOnlyField();
        txtUsername = createReadOnlyField();
        txtDepartment = createReadOnlyField();
        txtRole = createReadOnlyField();

        grid.add(createFieldBlock("Officer ID:", txtOfficerId));
        grid.add(createFieldBlock("Username / Staff ID:", txtUsername));
        grid.add(createFieldBlock("Assigned Department:", txtDepartment));
        grid.add(createFieldBlock("Assigned Role:", txtRole));

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    /**
     * Card: Password & Security Management.
     */
    private JPanel createChangePasswordCard() {
        JPanel card = createStyledCard();
        card.setLayout(new BorderLayout(0, 14));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = createSectionTitle("3. Security & Password Management");
        JLabel note = new JLabel("Leave blank to keep current password");
        note.setFont(FONT_SMALL);
        note.setForeground(TEXT_SECONDARY);

        top.add(title, BorderLayout.WEST);
        top.add(note, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        txtCurrentPassword = createStyledPasswordField();
        txtNewPassword = createStyledPasswordField();
        txtConfirmPassword = createStyledPasswordField();

        form.add(createFieldBlock("Current Password:", txtCurrentPassword));
        form.add(Box.createVerticalStrut(10));
        form.add(createFieldBlock("New Password:", txtNewPassword));
        form.add(Box.createVerticalStrut(10));
        form.add(createFieldBlock("Confirm New Password:", txtConfirmPassword));
        form.add(Box.createVerticalStrut(8));

        chkShowPasswords = new JCheckBox("Show passwords");
        chkShowPasswords.setFont(FONT_SMALL);
        chkShowPasswords.setForeground(TEXT_SECONDARY);
        chkShowPasswords.setOpaque(false);
        chkShowPasswords.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkShowPasswords.addActionListener(e -> {
            char echo = chkShowPasswords.isSelected() ? (char) 0 : '•';
            txtCurrentPassword.setEchoChar(echo);
            txtNewPassword.setEchoChar(echo);
            txtConfirmPassword.setEchoChar(echo);
        });
        form.add(chkShowPasswords);
        form.add(Box.createVerticalStrut(10));

        // Password policy hint box
        JPanel policyBox = new JPanel(new GridLayout(2, 1, 0, 3));
        policyBox.setBackground(Color.decode("#F8FAFC"));
        policyBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        policyBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        policyBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel p1 = new JLabel("📌 Password Policy: Minimum 6 characters long.");
        p1.setFont(FONT_SMALL);
        p1.setForeground(TEXT_SECONDARY);
        JLabel p2 = new JLabel("📌 Must contain at least one letter and at least one number.");
        p2.setFont(FONT_SMALL);
        p2.setForeground(TEXT_SECONDARY);
        policyBox.add(p1);
        policyBox.add(p2);
        form.add(policyBox);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    /**
     * Bottom Action Bar: Cancel and Save Changes buttons.
     */
    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(12, 24, 12, 24)
        ));

        JLabel tipLabel = new JLabel("💡 Profile changes are securely saved in the TecFAMS database");
        tipLabel.setFont(FONT_SMALL);
        tipLabel.setForeground(TEXT_SECONDARY);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnPanel.setOpaque(false);

        JButton btnCancel = createSecondaryButton("🧹 Discard Changes");
        btnCancel.setPreferredSize(new Dimension(160, 38));
        btnCancel.addActionListener(e -> handleCancel());

        JButton btnSave = createPrimaryButton("💾 Save Profile Changes");
        btnSave.setPreferredSize(new Dimension(190, 38));
        btnSave.addActionListener(e -> handleSaveChanges());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        bar.add(tipLabel, BorderLayout.WEST);
        bar.add(btnPanel, BorderLayout.EAST);
        return bar;
    }

    private void populateFields() {
        if (currentOfficer == null) return;

        txtName.setText(currentOfficer.getName() != null ? currentOfficer.getName() : "");
        txtEmail.setText(currentOfficer.getEmail() != null ? currentOfficer.getEmail() : "");
        txtPhone.setText(currentOfficer.getPhone() != null ? currentOfficer.getPhone() : "");

        txtOfficerId.setText(currentOfficer.getOfficerId() != null ? currentOfficer.getOfficerId() : "");
        txtUsername.setText(currentOfficer.getUsername() != null ? currentOfficer.getUsername() : "");
        txtDepartment.setText(currentOfficer.getDepartment() != null ? currentOfficer.getDepartment() : "");
        txtRole.setText(currentOfficer.getRole() != null ? currentOfficer.getRole() : "Technical Officer");

        lblNameCard.setText(txtName.getText());
        lblEmailCard.setText(txtEmail.getText());
    }

    /**
     * Loads existing profile photo directly from database model or legacy file path.
     */
    private void loadExistingPhoto() {
        if (currentOfficer == null) return;
        currentLoadedPhoto = currentOfficer.getProfileImage();
        if (currentLoadedPhoto == null && currentOfficer.getProfilePicture() != null) {
            currentLoadedPhoto = resolveImageFile(currentOfficer.getProfilePicture());
        }
    }

    private BufferedImage resolveImageFile(String path) {
        if (path == null || path.trim().isEmpty() || "default_avatar.png".equalsIgnoreCase(path.trim()) || "database_blob".equalsIgnoreCase(path.trim())) {
            return null;
        }

        String clean = path.trim();
        // 1. Direct path check
        File direct = new File(clean);
        if (direct.exists() && direct.isFile()) {
            try {
                BufferedImage img = ImageIO.read(direct);
                if (img != null) return img;
            } catch (Exception ignored) {}
        }

        // 2. Relative to uploads/profiles
        File uploadF = new File("uploads/profiles", new File(clean).getName());
        if (uploadF.exists() && uploadF.isFile()) {
            try {
                BufferedImage img = ImageIO.read(uploadF);
                if (img != null) return img;
            } catch (Exception ignored) {}
        }

        // 3. In src/main/resources/images/profiles
        File resF = new File("src/main/resources/images/profiles", new File(clean).getName());
        if (resF.exists() && resF.isFile()) {
            try {
                BufferedImage img = ImageIO.read(resF);
                if (img != null) return img;
            } catch (Exception ignored) {}
        }

        return null;
    }

    /**
     * Handles image file selection via JFileChooser.
     * Reads image bytes directly into memory for database persistence (no file creation on disk).
     */
    private void handleChangePhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Technical Officer Profile Photo");
        chooser.setFileFilter(new FileNameExtensionFilter("Image Files (*.jpg, *.jpeg, *.png)", "jpg", "jpeg", "png"));
        chooser.setAcceptAllFileFilterUsed(false);

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            if (selected == null || !selected.exists() || !selected.isFile()) {
                JOptionPane.showMessageDialog(this,
                        "The selected file does not exist or is not a regular file.",
                        "File Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Size validation (Max 5 MB)
            long maxBytes = 5 * 1024 * 1024;
            if (selected.length() > maxBytes) {
                JOptionPane.showMessageDialog(this,
                        "Image file size exceeds the 5 MB limit. Please select a smaller photo.",
                        "File Too Large", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                byte[] bytes = java.nio.file.Files.readAllBytes(selected.toPath());
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
                if (img == null) {
                    JOptionPane.showMessageDialog(this,
                            "The selected file is not a supported image format.",
                            "Invalid Image", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                pendingPhotoBytes = bytes;
                previewPhoto = img;
                lblPhotoStatus.setText("●  New photo ready to save to database (" + selected.getName() + ")");
                avatarPanel.repaint();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to read image file: " + ex.getMessage(),
                        "Read Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSaveChanges() {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();

        // 1. Name validation
        if (name.isEmpty()) {
            showValidationError("Full Name cannot be left empty.", txtName);
            return;
        }
        if (name.length() > 100) {
            showValidationError("Full Name cannot exceed 100 characters.", txtName);
            return;
        }

        // 2. Email validation
        if (email.isEmpty()) {
            showValidationError("Official Email Address cannot be left empty.", txtEmail);
            return;
        }
        if (email.length() > 100) {
            showValidationError("Email Address cannot exceed 100 characters.", txtEmail);
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showValidationError("Please enter a valid university email address (e.g., name@fot.ruh.ac.lk).", txtEmail);
            return;
        }

        int officerId = TechnicalOfficerDAO.parseOfficerId(currentOfficer);

        // 3. Duplicate Email validation
        if (dao.isEmailTaken(email, officerId)) {
            JOptionPane.showMessageDialog(this,
                    "The email address '" + email + "' is already registered to another technical officer.",
                    "Duplicate Email Address", JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocus();
            return;
        }

        // 4. Contact phone validation
        if (!phone.isEmpty()) {
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                showValidationError("Please enter a valid phone number (digits, spaces, hyphens, or '+' allowed; 7-20 chars).", txtPhone);
                return;
            }
        }

        // 5. Change Password validation and execution (if entered)
        boolean passwordHandled = processPasswordChange(officerId);
        if (!passwordHandled) {
            return;
        }

        // 6. Update profile record and save photo directly into users table in database via DAO PreparedStatement
        boolean updated = dao.updateProfile(officerId, name, email, phone, pendingPhotoBytes);
        if (updated) {
            if (pendingPhotoBytes != null) {
                currentLoadedPhoto = previewPhoto;
                previewPhoto = null;
                pendingPhotoBytes = null;
            }

            lblPhotoStatus.setText("");
            avatarPanel.repaint();
            lblNameCard.setText(name);
            lblEmailCard.setText(email);

            JOptionPane.showMessageDialog(this,
                    "Technical Officer profile and photo changes have been saved successfully to database!",
                    "Profile Updated - TecFAMS", JOptionPane.INFORMATION_MESSAGE);

            if (onProfileSavedCallback != null) {
                onProfileSavedCallback.run();
            }

            backToDashboard();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to save profile changes to database. Please check connection and try again.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean processPasswordChange(int officerId) {
        String currentPass = new String(txtCurrentPassword.getPassword()).trim();
        String newPass = new String(txtNewPassword.getPassword()).trim();
        String confirmPass = new String(txtConfirmPassword.getPassword()).trim();

        // If all password fields are blank, no change is requested
        if (currentPass.isEmpty() && newPass.isEmpty() && confirmPass.isEmpty()) {
            return true;
        }

        if (currentPass.isEmpty()) {
            showValidationError("Please enter your current password to authorize a password change.", txtCurrentPassword);
            return false;
        }

        if (newPass.isEmpty()) {
            showValidationError("Please enter your new password.", txtNewPassword);
            return false;
        }

        if (confirmPass.isEmpty()) {
            showValidationError("Please confirm your new password.", txtConfirmPassword);
            return false;
        }

        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this,
                    "The new password and confirmation password do not match. Please verify both entries.",
                    "Password Mismatch", JOptionPane.ERROR_MESSAGE);
            txtConfirmPassword.requestFocus();
            return false;
        }

        if (newPass.equals(currentPass)) {
            JOptionPane.showMessageDialog(this,
                    "The new password cannot be the same as your current password.",
                    "Password Policy", JOptionPane.WARNING_MESSAGE);
            txtNewPassword.requestFocus();
            return false;
        }

        // Complexity rules
        if (newPass.length() < 6) {
            JOptionPane.showMessageDialog(this,
                    "New password must be at least 6 characters long.",
                    "Password Policy", JOptionPane.WARNING_MESSAGE);
            txtNewPassword.requestFocus();
            return false;
        }

        if (!newPass.matches(".*[a-zA-Z].*")) {
            JOptionPane.showMessageDialog(this,
                    "New password must contain at least one letter.",
                    "Password Policy", JOptionPane.WARNING_MESSAGE);
            txtNewPassword.requestFocus();
            return false;
        }

        if (!newPass.matches(".*[0-9].*")) {
            JOptionPane.showMessageDialog(this,
                    "New password must contain at least one number.",
                    "Password Policy", JOptionPane.WARNING_MESSAGE);
            txtNewPassword.requestFocus();
            return false;
        }

        // Verify current password against database
        boolean verified = dao.verifyPassword(officerId, currentPass);
        if (!verified) {
            JOptionPane.showMessageDialog(this,
                    "The current password you entered is incorrect. Authentication failed.",
                    "Incorrect Password", JOptionPane.ERROR_MESSAGE);
            txtCurrentPassword.requestFocus();
            return false;
        }

        // Change password in database
        boolean changed = dao.changePassword(officerId, currentPass, newPass);
        if (!changed) {
            JOptionPane.showMessageDialog(this,
                    "Failed to update password in database. Please try again.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        txtCurrentPassword.setText("");
        txtNewPassword.setText("");
        txtConfirmPassword.setText("");

        return true;
    }

    private void handleCancel() {
        int conf = JOptionPane.showConfirmDialog(
                this,
                "Discard all unsaved changes and reset the profile form?",
                "Discard Changes",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (conf != JOptionPane.YES_OPTION) return;

        pendingPhotoBytes = null;
        previewPhoto = null;
        lblPhotoStatus.setText("");
        avatarPanel.repaint();

        populateFields();

        txtCurrentPassword.setText("");
        txtNewPassword.setText("");
        txtConfirmPassword.setText("");
    }

    private void backToDashboard() {
        if (parentDashboard instanceof TechnicalOfficerDashboard) {
            ((TechnicalOfficerDashboard) parentDashboard).showDashboardView();
        } else {
            dispose();
            if (parentDashboard != null) {
                parentDashboard.setVisible(true);
                parentDashboard.toFront();
            }
        }
    }

    private void showValidationError(String msg, JComponent targetField) {
        JOptionPane.showMessageDialog(this, msg, "Validation Notice", JOptionPane.WARNING_MESSAGE);
        if (targetField != null) {
            targetField.requestFocus();
        }
    }

    // --- Styling & UI Factory Helpers ---

    private JPanel createStyledCard() {
        JPanel card = new JPanel();
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private JLabel createSectionTitle(String title) {
        JLabel l = new JLabel(title);
        l.setFont(FONT_HEADER_MEDIUM);
        l.setForeground(PRIMARY_NAVY);
        return l;
    }

    private JPanel createFieldBlock(String labelText, JComponent field) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_MEDIUM);
        lbl.setForeground(TEXT_MAIN);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 38));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(lbl);
        p.add(Box.createVerticalStrut(6));
        p.add(field);

        return p;
    }

    private static final CompoundBorder FIELD_BORDER = new CompoundBorder(
            new LineBorder(Color.decode("#CBD5E1"), 1, true),
            new EmptyBorder(8, 12, 8, 12));

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_REGULAR);
        tf.setForeground(TEXT_MAIN);
        tf.setBackground(WHITE);
        tf.setBorder(FIELD_BORDER);
        return tf;
    }

    private JTextField createReadOnlyField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_REGULAR);
        tf.setForeground(Color.decode("#475569"));
        tf.setBackground(Color.decode("#F1F5F9"));
        tf.setEditable(false);
        tf.setFocusable(false);
        tf.setCursor(Cursor.getDefaultCursor());
        tf.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(8, 12, 8, 12)));
        return tf;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(FONT_REGULAR);
        pf.setForeground(TEXT_MAIN);
        pf.setBackground(WHITE);
        pf.setBorder(FIELD_BORDER);
        return pf;
    }

    private String getOfficerInitials() {
        if (currentOfficer == null || currentOfficer.getName() == null || currentOfficer.getName().trim().isEmpty()) {
            return "TO";
        }
        String clean = currentOfficer.getName().trim();
        String[] parts = clean.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty() && !p.endsWith(".")) {
                sb.append(Character.toUpperCase(p.charAt(0)));
            }
        }
        if (sb.length() >= 2) {
            return sb.substring(0, 2);
        } else if (sb.length() == 1) {
            return sb.toString();
        }
        return "TO";
    }

    /**
     * Circular avatar rendering component.
     */
    private class AvatarPreviewPanel extends JPanel {
        private final int diameter = 100;

        public AvatarPreviewPanel() {
            setPreferredSize(new Dimension(diameter + 10, diameter + 10));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int x = (getWidth() - diameter) / 2;
            int y = (getHeight() - diameter) / 2;

            BufferedImage imgToDraw = previewPhoto != null ? previewPhoto : currentLoadedPhoto;

            if (imgToDraw != null) {
                Shape oldClip = g2.getClip();
                Ellipse2D.Float circle = new Ellipse2D.Float(x, y, diameter, diameter);
                g2.clip(circle);

                int imgW = imgToDraw.getWidth();
                int imgH = imgToDraw.getHeight();
                double scale = Math.max((double) diameter / imgW, (double) diameter / imgH);
                int drawW = (int) Math.round(imgW * scale);
                int drawH = (int) Math.round(imgH * scale);
                int drawX = x + (diameter - drawW) / 2;
                int drawY = y + (diameter - drawH) / 2;

                g2.drawImage(imgToDraw, drawX, drawY, drawW, drawH, null);
                g2.setClip(oldClip);

                g2.setColor(Color.decode("#BFDBFE"));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(x, y, diameter - 1, diameter - 1);
            } else {
                g2.setColor(LIGHT_BLUE);
                g2.fillOval(x, y, diameter, diameter);

                g2.setColor(MAIN_BLUE);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(x, y, diameter - 1, diameter - 1);

                String initials = getOfficerInitials();
                g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
                FontMetrics fm = g2.getFontMetrics();
                int textX = x + (diameter - fm.stringWidth(initials)) / 2;
                int textY = y + ((diameter - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(initials, textX, textY);
            }

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            TechnicalOfficerDAO dao = new TechnicalOfficerDAO();
            TechnicalOfficerProfileForm form = new TechnicalOfficerProfileForm(null, dao.getCurrentOfficer(), dao);
            form.setVisible(true);
        });
    }
}
