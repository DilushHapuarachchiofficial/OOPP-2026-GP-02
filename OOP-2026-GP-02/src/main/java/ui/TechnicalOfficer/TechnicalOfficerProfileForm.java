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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

import static main.java.ui.TechnicalOfficer.TechnicalOfficerDashboard.*;

/**
 * TechnicalOfficerProfileForm
 * TecFAMS - Faculty of Technology Academic Management System
 * University of Ruhuna
 *
 * Professional Java Swing Profile management form for Technical Officers:
 * 1. Profile Photo: Display current photo, choose new file (JPG/JPEG/PNG),
 *    proportional preview before saving, safe storage to uploads/profiles.
 * 2. Editable Profile Details: Full Name, Official Email, Contact Number with validation
 *    and duplicate email check against MySQL database.
 * 3. Read-Only System Details: Officer ID, Username, Department, Role (protected).
 * 4. Change Password: JPasswordField inputs, current password verification against DB,
 *    password policy rules, match confirmation, and secure PreparedStatement update.
 * 5. Save Changes and Cancel buttons with full state restoration on cancel.
 */
public class TechnicalOfficerProfileForm extends JDialog {

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
    private File pendingPhotoFile = null;

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

    public TechnicalOfficerProfileForm(Frame parent, TechnicalOfficer officer, TechnicalOfficerDAO dao) {
        super(parent, "Edit Profile - Technical Officer", true);
        this.dao = dao != null ? dao : new TechnicalOfficerDAO();
        this.currentOfficer = officer != null ? officer : this.dao.getCurrentOfficer();

        initDialogSettings();
        loadExistingPhoto();
        buildUI();
        populateFields();
    }

    private void initDialogSettings() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(760, 800);
        setMinimumSize(new Dimension(560, 580));
        setLocationRelativeTo(getOwner());
    }

    public void setOnProfileSavedCallback(Runnable callback) {
        this.onProfileSavedCallback = callback;
    }

    private void buildUI() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);

        // 1. Header Banner
        getContentPane().add(createHeaderBanner(), BorderLayout.NORTH);

        // 2. Central Scrollable Content
        ScrollablePanel contentContainer = new ScrollablePanel();
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
        contentContainer.setBackground(BG_COLOR);
        contentContainer.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Card 1: Profile Photo & Quick Identity Summary
        contentContainer.add(createPhotoCard());
        contentContainer.add(Box.createVerticalStrut(16));

        // Card 2: Editable Personal & Contact Information
        contentContainer.add(createPersonalInfoCard());
        contentContainer.add(Box.createVerticalStrut(16));

        // Card 3: Read-Only System Details
        contentContainer.add(createReadOnlySystemCard());
        contentContainer.add(Box.createVerticalStrut(16));

        // Card 4: Change Password Section
        contentContainer.add(createChangePasswordCard());
        contentContainer.add(Box.createVerticalStrut(10));

        JScrollPane scrollPane = new JScrollPane(contentContainer);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BG_COLOR);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // 3. Bottom Action Bar (Save & Cancel)
        getContentPane().add(createBottomBar(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderBanner() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_NAVY);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel textBlock = new JPanel(new GridLayout(2, 1, 0, 3));
        textBlock.setOpaque(false);

        JLabel title = new JLabel("Edit Technical Officer Profile");
        title.setFont(FONT_HEADER_LARGE);
        title.setForeground(WHITE);

        JLabel subtitle = new JLabel("University of Ruhuna • Faculty of Technology (TecFAMS)");
        subtitle.setFont(FONT_SMALL);
        subtitle.setForeground(Color.decode("#BFDBFE"));

        textBlock.add(title);
        textBlock.add(subtitle);

        header.add(textBlock, BorderLayout.WEST);
        return header;
    }

    /**
     * Card 1: Profile Photo display, proportional preview, and Change Photo button.
     */
    private JPanel createPhotoCard() {
        JPanel card = createStyledCard();
        card.setLayout(new BorderLayout(20, 0));

        // Left: Avatar Component
        avatarPanel = new AvatarPreviewPanel();
        JPanel avatarWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        avatarWrapper.setOpaque(false);
        avatarWrapper.add(avatarPanel);

        // Middle: Buttons & Photo Note
        JPanel photoControls = new JPanel();
        photoControls.setLayout(new BoxLayout(photoControls, BoxLayout.Y_AXIS));
        photoControls.setOpaque(false);

        lblNameCard = new JLabel(currentOfficer != null ? currentOfficer.getName() : "Technical Officer");
        lblNameCard.setFont(FONT_HEADER_MEDIUM);
        lblNameCard.setForeground(PRIMARY_NAVY);

        lblEmailCard = new JLabel(currentOfficer != null ? currentOfficer.getEmail() : "");
        lblEmailCard.setFont(FONT_REGULAR);
        lblEmailCard.setForeground(TEXT_SECONDARY);

        JLabel roleBadge = new JLabel("  Technical Officer • Staff  ");
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        roleBadge.setForeground(MAIN_BLUE);
        roleBadge.setBackground(LIGHT_BLUE);
        roleBadge.setOpaque(true);
        roleBadge.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#BFDBFE"), 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));

        JButton btnChangePhoto = createPrimaryButton("📷  Change Photo");
        btnChangePhoto.addActionListener(e -> handleChangePhoto());

        JLabel photoHelp = new JLabel("Supported formats: JPG, JPEG, PNG (Max 5 MB)");
        photoHelp.setFont(FONT_SMALL);
        photoHelp.setForeground(TEXT_SECONDARY);

        lblPhotoStatus = new JLabel("");
        lblPhotoStatus.setFont(FONT_SMALL);
        lblPhotoStatus.setForeground(MAIN_BLUE);

        photoControls.add(lblNameCard);
        photoControls.add(Box.createVerticalStrut(2));
        photoControls.add(lblEmailCard);
        photoControls.add(Box.createVerticalStrut(6));
        photoControls.add(roleBadge);
        photoControls.add(Box.createVerticalStrut(12));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(btnChangePhoto);

        photoControls.add(btnRow);
        photoControls.add(Box.createVerticalStrut(6));
        photoControls.add(photoHelp);
        photoControls.add(Box.createVerticalStrut(4));
        photoControls.add(lblPhotoStatus);

        card.add(avatarWrapper, BorderLayout.WEST);
        card.add(photoControls, BorderLayout.CENTER);

        return card;
    }

    /**
     * Card 2: Editable Personal & Contact Information (Name, Email, Phone).
     */
    private JPanel createPersonalInfoCard() {
        JPanel card = createStyledCard();
        card.setLayout(new BorderLayout(0, 12));

        JLabel title = createSectionTitle("1. Personal & Contact Details (Editable)");
        card.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 2, 16, 12));
        grid.setOpaque(false);

        txtName = createStyledTextField();
        txtEmail = createStyledTextField();
        txtPhone = createStyledTextField();

        grid.add(createFieldBlock("Full Name:", txtName));
        grid.add(createFieldBlock("Official Email Address:", txtEmail));
        grid.add(createFieldBlock("Contact Phone Number:", txtPhone));

        JPanel emptyCell = new JPanel();
        emptyCell.setOpaque(false);
        grid.add(emptyCell);

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    /**
     * Card 3: Read-Only System Details (Staff ID, Officer ID, Department, Role).
     */
    private JPanel createReadOnlySystemCard() {
        JPanel card = createStyledCard();
        card.setLayout(new BorderLayout(0, 12));

        JLabel title = createSectionTitle("2. University & System Identifiers (Read-Only 🔒)");
        card.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 2, 16, 12));
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
     * Card 4: Change Password Section with JPasswordField and verification.
     */
    private JPanel createChangePasswordCard() {
        JPanel card = createStyledCard();
        card.setLayout(new BorderLayout(0, 12));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = createSectionTitle("3. Security: Change Password");
        JLabel note = new JLabel("Leave blank if you do not wish to change your password");
        note.setFont(FONT_SMALL);
        note.setForeground(TEXT_SECONDARY);

        top.add(title, BorderLayout.WEST);
        top.add(note, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 1, 0, 10));
        grid.setOpaque(false);

        txtCurrentPassword = createStyledPasswordField();
        txtNewPassword = createStyledPasswordField();
        txtConfirmPassword = createStyledPasswordField();

        grid.add(createFieldBlock("Current Password:", txtCurrentPassword));
        grid.add(createFieldBlock("New Password:", txtNewPassword));
        grid.add(createFieldBlock("Confirm New Password:", txtConfirmPassword));

        card.add(grid, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 6));
        bottom.setOpaque(false);

        chkShowPasswords = new JCheckBox("Show passwords");
        chkShowPasswords.setFont(FONT_SMALL);
        chkShowPasswords.setForeground(TEXT_SECONDARY);
        chkShowPasswords.setOpaque(false);
        chkShowPasswords.addActionListener(e -> {
            char echo = chkShowPasswords.isSelected() ? (char) 0 : '•';
            txtCurrentPassword.setEchoChar(echo);
            txtNewPassword.setEchoChar(echo);
            txtConfirmPassword.setEchoChar(echo);
        });

        bottom.add(chkShowPasswords);
        card.add(bottom, BorderLayout.SOUTH);

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
                new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel tipLabel = new JLabel("All edits are securely saved using JDBC PreparedStatement transactions");
        tipLabel.setFont(FONT_SMALL);
        tipLabel.setForeground(TEXT_SECONDARY);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton btnCancel = createSecondaryButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(95, 36));
        btnCancel.addActionListener(e -> handleCancel());

        JButton btnSave = createPrimaryButton("Save Changes");
        btnSave.setPreferredSize(new Dimension(130, 36));
        btnSave.addActionListener(e -> handleSaveChanges());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        bar.add(tipLabel, BorderLayout.CENTER);
        bar.add(btnPanel, BorderLayout.EAST);
        return bar;
    }

    private void populateFields() {
        if (currentOfficer == null) return;

        txtName.setText(currentOfficer.getName() != null ? currentOfficer.getName() : "");
        txtEmail.setText(currentOfficer.getEmail() != null ? currentOfficer.getEmail() : "");
        txtPhone.setText(currentOfficer.getPhone() != null ? currentOfficer.getPhone() : "");

        txtOfficerId.setText(currentOfficer.getOfficerId() != null ? currentOfficer.getOfficerId() : "12");
        txtUsername.setText(currentOfficer.getUsername() != null ? currentOfficer.getUsername() : "to_officer");
        txtDepartment.setText(currentOfficer.getDepartment() != null ? currentOfficer.getDepartment() : "Faculty of Technology");
        txtRole.setText(currentOfficer.getRole() != null ? currentOfficer.getRole() : "Technical Officer");

        lblNameCard.setText(txtName.getText());
        lblEmailCard.setText(txtEmail.getText());
    }

    /**
     * Loads the existing profile photo if available from the file system or resources.
     */
    private void loadExistingPhoto() {
        if (currentOfficer == null) return;
        String path = currentOfficer.getProfilePicture();
        currentLoadedPhoto = resolveImageFile(path);
    }

    private BufferedImage resolveImageFile(String path) {
        if (path == null || path.trim().isEmpty() || "default_avatar.png".equalsIgnoreCase(path.trim())) {
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
     * Handles image file selection via JFileChooser, image validation,
     * and immediate proportional preview.
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

            // Extension check
            String name = selected.getName().toLowerCase();
            if (!name.endsWith(".jpg") && !name.endsWith(".jpeg") && !name.endsWith(".png")) {
                JOptionPane.showMessageDialog(this,
                        "Unsupported image format. Allowed formats: JPG, JPEG, and PNG only.",
                        "Invalid Format", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // File size validation (Max 5 MB)
            long maxBytes = 5 * 1024 * 1024;
            if (selected.length() > maxBytes) {
                JOptionPane.showMessageDialog(this,
                        "The selected file is too large (" + (selected.length() / (1024 * 1024)) + " MB). Maximum allowed size is 5 MB.",
                        "File Size Limit", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (selected.length() == 0) {
                JOptionPane.showMessageDialog(this,
                        "The selected image file is empty (0 bytes).",
                        "Invalid Image", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Verify image readability
            try {
                BufferedImage decoded = ImageIO.read(selected);
                if (decoded == null) {
                    JOptionPane.showMessageDialog(this,
                            "The selected file is not a valid or readable image.",
                            "Corrupted Image", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                this.pendingPhotoFile = selected;
                this.previewPhoto = decoded;
                avatarPanel.repaint();

                lblPhotoStatus.setText("<html>✓ Previewing: <b>" + selected.getName() + "</b> (Click 'Save Changes' to apply)</html>");
                lblPhotoStatus.setForeground(SUCCESS_GREEN);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to read image file: " + ex.getMessage(),
                        "Read Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Saves the pending photo to uploads/profiles without modifying DB schema.
     */
    private String savePendingPhoto(int officerId) throws IOException {
        if (pendingPhotoFile == null) return null;

        File uploadDir = new File("uploads/profiles");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String orig = pendingPhotoFile.getName().toLowerCase();
        String ext = orig.endsWith(".png") ? ".png" : ".jpg";
        String uniqueName = "officer_" + officerId + "_" + System.currentTimeMillis() + ext;
        File dest = new File(uploadDir, uniqueName);

        // Copy new photo
        Files.copy(pendingPhotoFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return "uploads/profiles/" + uniqueName;
    }

    /**
     * Validates personal details, duplicate email, and password changes,
     * then executes database updates using PreparedStatement.
     */
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
            // Password validation failed; do not save profile edits
            return;
        }

        // 6. Handle profile photo save if pending
        String oldPhoto = currentOfficer.getProfilePicture();
        String savedPhotoPath = null;
        if (pendingPhotoFile != null) {
            try {
                savedPhotoPath = savePendingPhoto(officerId);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to save profile photo to storage: " + ex.getMessage(),
                        "Storage Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // 7. Update profile record in database via DAO PreparedStatement
        boolean updated = dao.updateProfile(officerId, name, email, phone, savedPhotoPath);
        if (updated) {
            if (savedPhotoPath != null) {
                currentLoadedPhoto = previewPhoto;
                previewPhoto = null;
                pendingPhotoFile = null;

                // Safely delete previous custom photo only after new photo saved successfully
                if (oldPhoto != null && !oldPhoto.equalsIgnoreCase("default_avatar.png")
                        && !oldPhoto.equals(savedPhotoPath)) {
                    try {
                        File oldFile = new File(oldPhoto);
                        if (oldFile.exists() && oldFile.isFile()) {
                            oldFile.delete();
                        }
                    } catch (Exception ignored) {}
                }
            }

            lblPhotoStatus.setText("");
            avatarPanel.repaint();
            lblNameCard.setText(name);
            lblEmailCard.setText(email);

            JOptionPane.showMessageDialog(this,
                    "Technical Officer profile changes have been saved successfully!",
                    "Profile Updated - TecFAMS", JOptionPane.INFORMATION_MESSAGE);

            if (onProfileSavedCallback != null) {
                onProfileSavedCallback.run();
            }

            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to save profile changes to database. Please check connection and try again.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Validates and updates password if any password field is populated.
     */
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

        // Clear password fields on success
        txtCurrentPassword.setText("");
        txtNewPassword.setText("");
        txtConfirmPassword.setText("");

        return true;
    }

    /**
     * Discards unsaved changes, clears photo preview, restores saved profile information,
     * and closes the dialog.
     */
    private void handleCancel() {
        // Discard pending photo preview
        pendingPhotoFile = null;
        previewPhoto = null;
        lblPhotoStatus.setText("");
        avatarPanel.repaint();

        // Restore saved profile text fields
        populateFields();

        // Clear password fields
        txtCurrentPassword.setText("");
        txtNewPassword.setText("");
        txtConfirmPassword.setText("");

        dispose();
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
                new EmptyBorder(16, 18, 16, 18)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return card;
    }

    private JLabel createSectionTitle(String title) {
        JLabel l = new JLabel(title);
        l.setFont(FONT_HEADER_SMALL);
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

        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(lbl);
        p.add(Box.createVerticalStrut(6));
        p.add(field);

        return p;
    }

    private static final CompoundBorder FIELD_BORDER = new CompoundBorder(
            new LineBorder(Color.decode("#CBD5E1"), 1, true),
            new EmptyBorder(6, 10, 6, 10));

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
        tf.setForeground(Color.decode("#64748B"));
        tf.setBackground(Color.decode("#F1F5F9"));
        tf.setEditable(false);
        tf.setFocusable(false);
        tf.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(6, 10, 6, 10)));
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
     * Circular avatar rendering component with proportional scaling,
     * smooth antialiasing, and default initials fallback.
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
                // Circle clip
                Shape oldClip = g2.getClip();
                Ellipse2D.Float circle = new Ellipse2D.Float(x, y, diameter, diameter);
                g2.clip(circle);

                // Proportional scale to fill circle (center-cropped)
                int imgW = imgToDraw.getWidth();
                int imgH = imgToDraw.getHeight();
                double scale = Math.max((double) diameter / imgW, (double) diameter / imgH);
                int drawW = (int) Math.round(imgW * scale);
                int drawH = (int) Math.round(imgH * scale);
                int drawX = x + (diameter - drawW) / 2;
                int drawY = y + (diameter - drawH) / 2;

                g2.drawImage(imgToDraw, drawX, drawY, drawW, drawH, null);
                g2.setClip(oldClip);

                // Circular border around photo
                g2.setColor(Color.decode("#BFDBFE"));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(x, y, diameter - 1, diameter - 1);
            } else {
                // TecFAMS theme default avatar
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

    /**
     * Scrollable panel that tracks viewport width, eliminating horizontal scrollbars
     * while enabling natural vertical scrolling.
     */
    private static class ScrollablePanel extends JPanel implements Scrollable {
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
            return true; // Match viewport width precisely
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /**
     * Standalone main method to launch and test TechnicalOfficerProfileForm.
     */
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
