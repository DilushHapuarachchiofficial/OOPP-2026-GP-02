package main.java.ui.admin;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import main.java.dao.admin.UserDAO;
import main.java.model.admin.Admin;

public class AdminProfilePanel extends JPanel {

    private int currentAdminId = 1; // Default logged in admin id
    private Admin currentAdmin;

    // Left card labels
    private JLabel avatarLabel;
    private JLabel nameDisplayLabel;
    private JLabel roleDisplayLabel;
    private JLabel emailDisplayLabel;
    private JPanel statusBadge;
    private JLabel statusTextLabel;

    // Navigation buttons on left card
    private JPanel personalInfoNavBtn;
    private JPanel changePasswordNavBtn;
    private boolean isPersonalInfoTab = true;

    // Right card container & panels
    private CardLayout rightCardLayout;
    private JPanel rightCardContainer;
    private JPanel personalInfoPanel;
    private JPanel changePasswordPanel;

    // Personal Information form fields
    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField addressField;

    // Change Password form fields
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    public AdminProfilePanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // Load admin profile from database
        loadAdminProfile();

        buildUI();
    }

    private void loadAdminProfile() {
        currentAdmin = UserDAO.getAdminProfile(currentAdminId);
        if (currentAdmin == null) {
            // Fallback default
            currentAdmin = new Admin(
                1,
                "Admin",
                "admin1",
                "Administrator",
                "admin@tech.ac.lk",
                "Active",
                "+94 77 123 4567",
                "Faculty of Technology, University of Ruhuna"
            );
        }
    }

    private void buildUI() {
        removeAll();

        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(25, 30, 20, 30));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("Admin Profile");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(new Color(30, 41, 59));

        JLabel subtitleLabel = new JLabel("Manage your personal information");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(120, 134, 150));

        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(5));
        titleBlock.add(subtitleLabel);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Split Section (Left Card & Right Card)
        JPanel contentGrid = new JPanel(new GridBagLayout());
        contentGrid.setOpaque(false);
        contentGrid.setBorder(new EmptyBorder(0, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();

        // 1. Left Card (Profile Summary & Navigation)
        JPanel leftCard = buildLeftCard();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.38;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 15);
        contentGrid.add(leftCard, gbc);

        // 2. Right Card (Form details)
        JPanel rightCard = buildRightCard();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.62;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 15, 0, 0);
        contentGrid.add(rightCard, gbc);

        add(contentGrid, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    /**
     * Builds the left profile card matching the image:
     * - Circular blue avatar with user silhouette
     * - Name, "Administrator", email with @ icon, Active badge
     * - "Personal Information" active tab item
     * - "Change Password" inactive tab item
     */
    private JPanel buildLeftCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(230, 235, 242), 16, 1),
            new EmptyBorder(30, 26, 30, 26)
        ));

        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);

        // Header sub-panel with Avatar on left, info on right
        JPanel userHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        userHeader.setOpaque(false);

        // Circular Blue Avatar with user silhouette
        avatarLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = Math.min(getWidth(), getHeight());

                // Blue circle background (#4F83F0 / #3B82F6)
                g2.setColor(new Color(66, 133, 244));
                g2.fillOval(0, 0, size, size);

                // Silhouette in light soft blue/white
                g2.setColor(new Color(235, 243, 255));
                // Head
                int headR = (int)(size * 0.36);
                int headX = (size - headR) / 2;
                int headY = (int)(size * 0.18);
                g2.fillOval(headX, headY, headR, headR);

                // Body shoulders
                int bodyW = (int)(size * 0.72);
                int bodyH = (int)(size * 0.55);
                int bodyX = (size - bodyW) / 2;
                int bodyY = (int)(size * 0.58);
                g2.fillArc(bodyX, bodyY, bodyW, bodyH, 0, 180);

                g2.dispose();
            }
        };
        avatarLabel.setPreferredSize(new Dimension(84, 84));

        // User info texts
        JPanel infoTextPanel = new JPanel();
        infoTextPanel.setLayout(new BoxLayout(infoTextPanel, BoxLayout.Y_AXIS));
        infoTextPanel.setOpaque(false);

        nameDisplayLabel = new JLabel(currentAdmin.getName());
        nameDisplayLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameDisplayLabel.setForeground(new Color(30, 41, 59));

        roleDisplayLabel = new JLabel("Administrator");
        roleDisplayLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        roleDisplayLabel.setForeground(new Color(110, 125, 140));

        // Email line with small @ icon
        JPanel emailRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        emailRow.setOpaque(false);
        JLabel emailIcon = new JLabel("@");
        emailIcon.setFont(new Font("Segoe UI", Font.BOLD, 13));
        emailIcon.setForeground(new Color(120, 135, 150));

        emailDisplayLabel = new JLabel(currentAdmin.getEmail());
        emailDisplayLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emailDisplayLabel.setForeground(new Color(120, 135, 150));
        emailRow.add(emailIcon);
        emailRow.add(emailDisplayLabel);

        // Status Badge (Active with green dot)
        statusBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 252, 231)); // Soft green background
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        statusBadge.setOpaque(false);
        statusBadge.setBorder(new EmptyBorder(3, 10, 3, 10));

        JLabel greenDot = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(22, 163, 74));
                g2.fillOval(0, 3, 7, 7);
                g2.dispose();
            }
        };
        greenDot.setPreferredSize(new Dimension(8, 14));

        statusTextLabel = new JLabel("Active");
        statusTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusTextLabel.setForeground(new Color(22, 163, 74));
        statusBadge.add(greenDot);
        statusBadge.add(statusTextLabel);

        infoTextPanel.add(nameDisplayLabel);
        infoTextPanel.add(Box.createVerticalStrut(3));
        infoTextPanel.add(roleDisplayLabel);
        infoTextPanel.add(Box.createVerticalStrut(6));
        infoTextPanel.add(emailRow);
        infoTextPanel.add(Box.createVerticalStrut(6));
        infoTextPanel.add(statusBadge);

        userHeader.add(avatarLabel);
        userHeader.add(infoTextPanel);

        topSection.add(userHeader);
        topSection.add(Box.createVerticalStrut(32));

        // Navigation Tabs in left card
        personalInfoNavBtn = createNavTab("Personal Information", true, "src/main/resources/images/admin-icons/profile.png");
        changePasswordNavBtn = createNavTab("Change Password", false, "src/main/resources/images/admin-icons/settings.png");

        personalInfoNavBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switchToTab(true);
            }
        });

        changePasswordNavBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switchToTab(false);
            }
        });

        topSection.add(personalInfoNavBtn);
        topSection.add(Box.createVerticalStrut(8));
        topSection.add(changePasswordNavBtn);

        card.add(topSection, BorderLayout.NORTH);

        return card;
    }

    /**
     * Creates a navigation pill tab in the left card
     */
    private JPanel createNavTab(String title, boolean active, String iconPath) {
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                if ((title.equals("Personal Information") && isPersonalInfoTab) ||
                    (title.equals("Change Password") && !isPersonalInfoTab)) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(235, 242, 255)); // Soft blue highlight
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        nav.setOpaque(false);
        nav.setCursor(new Cursor(Cursor.HAND_CURSOR));
        nav.setBorder(new EmptyBorder(2, 12, 2, 12));
        nav.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel iconLabel = new JLabel();
        try {
            ImageIcon icon = new ImageIcon(iconPath);
            Image img = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            iconLabel.setIcon(new ImageIcon(img));
        } catch (Exception ex) {
            iconLabel.setText("•");
        }

        JLabel textLabel = new JLabel(title);
        textLabel.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 14));
        textLabel.setForeground(active ? new Color(24, 119, 242) : new Color(100, 115, 130));

        nav.add(iconLabel);
        nav.add(textLabel);

        return nav;
    }

    private void switchToTab(boolean personalInfo) {
        isPersonalInfoTab = personalInfo;
        personalInfoNavBtn.repaint();
        changePasswordNavBtn.repaint();

        // Update font styles
        updateNavTabStyles();

        if (personalInfo) {
            rightCardLayout.show(rightCardContainer, "PersonalInfo");
        } else {
            rightCardLayout.show(rightCardContainer, "ChangePassword");
        }
    }

    private void updateNavTabStyles() {
        for (Component c : personalInfoNavBtn.getComponents()) {
            if (c instanceof JLabel) {
                JLabel l = (JLabel) c;
                if (!l.getText().isEmpty()) {
                    l.setFont(new Font("Segoe UI", isPersonalInfoTab ? Font.BOLD : Font.PLAIN, 14));
                    l.setForeground(isPersonalInfoTab ? new Color(24, 119, 242) : new Color(100, 115, 130));
                }
            }
        }
        for (Component c : changePasswordNavBtn.getComponents()) {
            if (c instanceof JLabel) {
                JLabel l = (JLabel) c;
                if (!l.getText().isEmpty()) {
                    l.setFont(new Font("Segoe UI", !isPersonalInfoTab ? Font.BOLD : Font.PLAIN, 14));
                    l.setForeground(!isPersonalInfoTab ? new Color(24, 119, 242) : new Color(100, 115, 130));
                }
            }
        }
    }

    /**
     * Builds the right card with CardLayout switching between
     * Personal Information Form & Change Password Form
     */
    private JPanel buildRightCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(230, 235, 242), 16, 1),
            new EmptyBorder(30, 32, 30, 32)
        ));

        rightCardLayout = new CardLayout();
        rightCardContainer = new JPanel(rightCardLayout);
        rightCardContainer.setOpaque(false);

        personalInfoPanel = buildPersonalInfoPanel();
        changePasswordPanel = buildChangePasswordPanel();

        rightCardContainer.add(personalInfoPanel, "PersonalInfo");
        rightCardContainer.add(changePasswordPanel, "ChangePassword");

        card.add(rightCardContainer, BorderLayout.CENTER);
        return card;
    }

    /**
     * Personal Information Panel matching image:
     * - Section Title: "Personal Information" (Bold 18px)
     * - Fields: Full Name, Email Address, Phone Number, Address
     * - Bottom buttons: "Save Changes" (Blue) and "Cancel" (White with border)
     */
    private JPanel buildPersonalInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // Section Title
        JLabel title = new JLabel("Personal Information");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(30, 41, 59));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));

        // Form Fields
        fullNameField = createStyledTextField(currentAdmin.getName());
        emailField = createStyledTextField(currentAdmin.getEmail());
        phoneField = createStyledTextField(currentAdmin.getPhone() != null ? currentAdmin.getPhone() : "+94 77 123 4567");
        addressField = createStyledTextField(currentAdmin.getAddress() != null ? currentAdmin.getAddress() : "Faculty of Technology, University of Ruhuna");

        addFormField(panel, "Full Name", fullNameField);
        addFormField(panel, "Email Address", emailField);
        addFormField(panel, "Phone Number", phoneField);
        addFormField(panel, "Address", addressField);

        panel.add(Box.createVerticalStrut(10));

        // Action Buttons: Save Changes & Cancel
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton saveBtn = new JButton("Save Changes");
        stylePrimaryButton(saveBtn);
        saveBtn.addActionListener(e -> savePersonalInfoChanges());

        JButton cancelBtn = new JButton("Cancel");
        styleSecondaryButton(cancelBtn);
        cancelBtn.addActionListener(e -> resetPersonalInfoFields());

        btnRow.add(saveBtn);
        btnRow.add(cancelBtn);

        panel.add(btnRow);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    /**
     * Change Password Panel:
     * - Current Password
     * - New Password
     * - Confirm New Password
     * - "Update Password" & "Cancel"
     */
    private JPanel buildChangePasswordPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel title = new JLabel("Change Password");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(30, 41, 59));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));

        currentPasswordField = createStyledPasswordField();
        newPasswordField = createStyledPasswordField();
        confirmPasswordField = createStyledPasswordField();

        addFormField(panel, "Current Password", currentPasswordField);
        addFormField(panel, "New Password", newPasswordField);
        addFormField(panel, "Confirm New Password", confirmPasswordField);

        panel.add(Box.createVerticalStrut(10));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton saveBtn = new JButton("Update Password");
        stylePrimaryButton(saveBtn);
        saveBtn.addActionListener(e -> savePasswordChange());

        JButton cancelBtn = new JButton("Cancel");
        styleSecondaryButton(cancelBtn);
        cancelBtn.addActionListener(e -> {
            currentPasswordField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");
            switchToTab(true);
        });

        btnRow.add(saveBtn);
        btnRow.add(cancelBtn);

        panel.add(btnRow);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private void addFormField(JPanel parent, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(new Color(71, 85, 105));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setPreferredSize(new Dimension(480, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        parent.add(label);
        parent.add(Box.createVerticalStrut(6));
        parent.add(field);
        parent.add(Box.createVerticalStrut(14));
    }

    private JTextField createStyledTextField(String text) {
        JTextField tf = new JTextField(text);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setForeground(new Color(30, 41, 59));
        tf.setBackground(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(226, 232, 240), 10, 1),
            new EmptyBorder(8, 14, 8, 14)
        ));
        return tf;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pf.setForeground(new Color(30, 41, 59));
        pf.setBackground(Color.WHITE);
        pf.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(226, 232, 240), 10, 1),
            new EmptyBorder(8, 14, 8, 14)
        ));
        return pf;
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(24, 119, 242));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(135, 40));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(13, 98, 220));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(24, 119, 242));
            }
        });
    }

    private void styleSecondaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(71, 85, 105));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(95, 40));
        btn.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(203, 213, 225), 10, 1),
            new EmptyBorder(8, 18, 8, 18)
        ));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(248, 250, 252));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
        });
    }

    private void savePersonalInfoChanges() {
        String newName = fullNameField.getText().trim();
        String newEmail = emailField.getText().trim();
        String newPhone = phoneField.getText().trim();
        String newAddress = addressField.getText().trim();

        if (newName.isEmpty() || newEmail.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Email cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success = UserDAO.updateAdminProfile(currentAdminId, newName, newEmail, newPhone, newAddress);
        if (success) {
            currentAdmin.setPhone(newPhone);
            currentAdmin.setAddress(newAddress);
            // Refresh display labels on left card
            nameDisplayLabel.setText(newName);
            emailDisplayLabel.setText(newEmail);

            JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update profile. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetPersonalInfoFields() {
        if (currentAdmin != null) {
            fullNameField.setText(currentAdmin.getName());
            emailField.setText(currentAdmin.getEmail());
            phoneField.setText(currentAdmin.getPhone() != null ? currentAdmin.getPhone() : "");
            addressField.setText(currentAdmin.getAddress() != null ? currentAdmin.getAddress() : "");
        }
    }

    private void savePasswordChange() {
        String currentPass = new String(currentPasswordField.getPassword()).trim();
        String newPass = new String(newPasswordField.getPassword()).trim();
        String confirmPass = new String(confirmPasswordField.getPassword()).trim();

        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all password fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "New password and confirmation do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (newPass.length() < 4) {
            JOptionPane.showMessageDialog(this, "New password must be at least 4 characters long.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success = UserDAO.changeUserPassword(currentAdminId, currentPass, newPass);
        if (success) {
            JOptionPane.showMessageDialog(this, "Password changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            currentPasswordField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");
            switchToTab(true);
        } else {
            JOptionPane.showMessageDialog(this, "Current password is incorrect. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Custom Rounded Border helper
    private static class RoundedBorder extends LineBorder {
        private int radius;
        RoundedBorder(Color color, int radius, int thickness) {
            super(color, thickness, true);
            this.radius = radius;
        }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(lineColor);
            g2.draw(new RoundRectangle2D.Float(x, y, width - 1, height - 1, radius, radius));
            g2.dispose();
        }
    }
}
