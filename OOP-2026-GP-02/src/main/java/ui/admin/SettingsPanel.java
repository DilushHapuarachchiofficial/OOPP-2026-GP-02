package main.java.ui.admin;

import main.java.dao.admin.DatabaseConnection;
import main.java.dao.admin.SettingsDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;

public class SettingsPanel extends JPanel {

    private CardLayout cardLayout;
    private JPanel cardContainer;

    // Navigation buttons
    private JButton tabAcademicBtn;
    private JButton tabInstitutionBtn;
    private JButton tabSecurityBtn;
    private JButton tabMaintenanceBtn;
    private String currentTab = "ACADEMIC";

    // --- Academic Settings Controls ---
    private JComboBox<String> academicYearCombo;
    private JComboBox<String> semesterCombo;
    private JSpinner attendanceThresholdSpinner;
    private JComboBox<String> regStatusCombo;
    private JSpinner medicalGraceDaysSpinner;

    // --- Institution Settings Controls ---
    private JTextField instNameField;
    private JTextField facultyNameField;
    private JTextField systemNameField;
    private JTextField contactEmailField;
    private JTextField contactPhoneField;
    private JTextField campusAddressField;

    // --- Security Settings Controls ---
    private JTextField defaultPasswordField;
    private JSpinner minPasswordLengthSpinner;
    private JSpinner maxLoginAttemptsSpinner;
    private JSpinner sessionTimeoutSpinner;
    private JCheckBox forcePasswordChangeCheck;

    // --- System & Maintenance Controls ---
    private JCheckBox maintenanceModeCheck;
    private JSpinner noticeExpirySpinner;
    private JCheckBox emailNotificationsCheck;
    private JCheckBox autoBackupCheck;
    private JLabel dbStatusLabel;

    public SettingsPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        initUI();
        loadSettingsFromDatabase();
    }

    private void initUI() {
        // --- Top Header Area ---
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.setBorder(new EmptyBorder(25, 30, 15, 30));

        // 1. Header Title & Main Action Buttons
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("System & Governance Settings");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(23, 32, 48));

        JLabel subtitleLabel = new JLabel("Configure academic regulations, faculty identity, security controls, and maintenance operations");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(110, 120, 135));

        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitleLabel);

        // Header Action Buttons
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsPanel.setOpaque(false);

        JButton resetDefaultsBtn = createStyledButton("Reset Category", new Color(240, 243, 248), new Color(60, 70, 85), true);
        resetDefaultsBtn.setPreferredSize(new Dimension(140, 36));
        resetDefaultsBtn.addActionListener(e -> resetCurrentCategoryDefaults());

        JButton saveBtn = createStyledButton("💾 Save Changes", new Color(21, 101, 192), Color.WHITE, false);
        saveBtn.setPreferredSize(new Dimension(150, 36));
        saveBtn.addActionListener(e -> saveSettingsToDatabase());

        actionsPanel.add(resetDefaultsBtn);
        actionsPanel.add(saveBtn);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(actionsPanel, BorderLayout.EAST);

        // 2. Tab Navigation Bar
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        navBar.setOpaque(false);
        navBar.setBorder(new EmptyBorder(18, 0, 0, 0));

        tabAcademicBtn = createTabButton("Academic Policies", true);
        tabInstitutionBtn = createTabButton("Institutional Profile", false);
        tabSecurityBtn = createTabButton("Security & Access", false);
        tabMaintenanceBtn = createTabButton("System & Maintenance", false);

        tabAcademicBtn.addActionListener(e -> switchSettingsTab("ACADEMIC"));
        tabInstitutionBtn.addActionListener(e -> switchSettingsTab("INSTITUTION"));
        tabSecurityBtn.addActionListener(e -> switchSettingsTab("SECURITY"));
        tabMaintenanceBtn.addActionListener(e -> switchSettingsTab("MAINTENANCE"));

        navBar.add(tabAcademicBtn);
        navBar.add(tabInstitutionBtn);
        navBar.add(tabSecurityBtn);
        navBar.add(tabMaintenanceBtn);

        topContainer.add(headerPanel);
        topContainer.add(navBar);
        add(topContainer, BorderLayout.NORTH);

        // --- Card Container for the 4 Categories ---
        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setOpaque(false);

        cardContainer.add(createAcademicSettingsView(), "ACADEMIC");
        cardContainer.add(createInstitutionSettingsView(), "INSTITUTION");
        cardContainer.add(createSecuritySettingsView(), "SECURITY");
        cardContainer.add(createMaintenanceSettingsView(), "MAINTENANCE");

        JScrollPane scrollPane = new JScrollPane(cardContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(245, 247, 250));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void switchSettingsTab(String tabKey) {
        currentTab = tabKey;
        cardLayout.show(cardContainer, tabKey);

        updateTabButtonStyles(tabAcademicBtn, "ACADEMIC".equals(tabKey));
        updateTabButtonStyles(tabInstitutionBtn, "INSTITUTION".equals(tabKey));
        updateTabButtonStyles(tabSecurityBtn, "SECURITY".equals(tabKey));
        updateTabButtonStyles(tabMaintenanceBtn, "MAINTENANCE".equals(tabKey));
    }

    private JButton createTabButton(String text, boolean active) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getBackground() != null) {
                    g2.setColor(getBackground());
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                }
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        updateTabButtonStyles(btn, active);
        return btn;
    }

    private void updateTabButtonStyles(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(new Color(21, 101, 192)); // Project primary theme color
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(90, 100, 115));
        }
        btn.repaint();
    }

    // =========================================================================
    // CATEGORY 1: ACADEMIC POLICIES VIEW
    // =========================================================================
    private JPanel createAcademicSettingsView() {
        JPanel view = new JPanel();
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
        view.setOpaque(false);
        view.setBorder(new EmptyBorder(10, 30, 30, 30));

        // Card 1: Active Session
        JPanel card1 = createContentCard("Active Academic Session & Cohort", "Configure the current operational academic calendar and active semester");
        JPanel form1 = new JPanel(new GridLayout(2, 2, 25, 16));
        form1.setOpaque(false);

        academicYearCombo = new JComboBox<>(new String[]{"2023/2024", "2024/2025", "2025/2026", "2026/2027"});
        academicYearCombo.setPreferredSize(new Dimension(220, 36));

        semesterCombo = new JComboBox<>(new String[]{"Semester 1", "Semester 2"});
        semesterCombo.setPreferredSize(new Dimension(220, 36));

        form1.add(createFieldGroup("Current Academic Year", academicYearCombo, "Active undergraduate academic cohort"));
        form1.add(createFieldGroup("Current Active Semester", semesterCombo, "Determines timetable and attendance period"));
        card1.add(form1, BorderLayout.CENTER);

        // Card 2: Student Regulations
        JPanel card2 = createContentCard("Examination Eligibility & Course Regulations", "Set institutional benchmarks for student participation and course enrollment");
        JPanel form2 = new JPanel(new GridLayout(3, 1, 0, 18));
        form2.setOpaque(false);

        attendanceThresholdSpinner = new JSpinner(new SpinnerNumberModel(80, 50, 100, 5));
        attendanceThresholdSpinner.setPreferredSize(new Dimension(140, 36));

        regStatusCombo = new JComboBox<>(new String[]{"Open", "Closed"});
        regStatusCombo.setPreferredSize(new Dimension(180, 36));

        medicalGraceDaysSpinner = new JSpinner(new SpinnerNumberModel(14, 1, 60, 1));
        medicalGraceDaysSpinner.setPreferredSize(new Dimension(140, 36));

        form2.add(createFieldGroup("Minimum Attendance Requirement (%)", attendanceThresholdSpinner, "UGC standard: 80% attendance (inclusive of medicals) required for exam eligibility"));
        form2.add(createFieldGroup("Student Course Enrollment Status", regStatusCombo, "Allows or restricts undergraduates from adding/dropping course registrations"));
        form2.add(createFieldGroup("Medical Certificate Submission Grace Period (Days)", medicalGraceDaysSpinner, "Maximum permitted days after absence to submit certified medical forms"));
        card2.add(form2, BorderLayout.CENTER);

        view.add(card1);
        view.add(Box.createVerticalStrut(20));
        view.add(card2);
        return view;
    }

    // =========================================================================
    // CATEGORY 2: INSTITUTIONAL PROFILE VIEW
    // =========================================================================
    private JPanel createInstitutionSettingsView() {
        JPanel view = new JPanel();
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
        view.setOpaque(false);
        view.setBorder(new EmptyBorder(10, 30, 30, 30));

        // Card 1: Official Names
        JPanel card1 = createContentCard("Institution & Faculty Identity", "Branding details displayed across official headers, reports, and notices");
        JPanel form1 = new JPanel(new GridLayout(3, 1, 0, 16));
        form1.setOpaque(false);

        instNameField = new JTextField(30);
        instNameField.setPreferredSize(new Dimension(300, 36));

        facultyNameField = new JTextField(30);
        facultyNameField.setPreferredSize(new Dimension(300, 36));

        systemNameField = new JTextField(30);
        systemNameField.setPreferredSize(new Dimension(300, 36));

        form1.add(createFieldGroup("Higher Education Institute / University Name", instNameField, "e.g. University of Ruhuna"));
        form1.add(createFieldGroup("Faculty / Campus Name", facultyNameField, "e.g. Faculty of Technology"));
        form1.add(createFieldGroup("Portal Title / System Acronym", systemNameField, "e.g. FAMS - Faculty Academic Management System"));
        card1.add(form1, BorderLayout.CENTER);

        // Card 2: Contact Information
        JPanel card2 = createContentCard("Official Contact & Campus Location", "Administrative helpline and physical address information");
        JPanel form2 = new JPanel(new GridLayout(3, 1, 0, 16));
        form2.setOpaque(false);

        contactEmailField = new JTextField(30);
        contactEmailField.setPreferredSize(new Dimension(300, 36));

        contactPhoneField = new JTextField(30);
        contactPhoneField.setPreferredSize(new Dimension(300, 36));

        campusAddressField = new JTextField(30);
        campusAddressField.setPreferredSize(new Dimension(300, 36));

        form2.add(createFieldGroup("Official Administrator Contact Email", contactEmailField, "Receives official queries and system inquiries"));
        form2.add(createFieldGroup("Faculty Hotline / Telephone", contactPhoneField, "Primary telephone number for faculty dean's office"));
        form2.add(createFieldGroup("Campus Physical Address", campusAddressField, "Postal address of the faculty premises"));
        card2.add(form2, BorderLayout.CENTER);

        view.add(card1);
        view.add(Box.createVerticalStrut(20));
        view.add(card2);
        return view;
    }

    // =========================================================================
    // CATEGORY 3: SECURITY & ACCESS VIEW
    // =========================================================================
    private JPanel createSecuritySettingsView() {
        JPanel view = new JPanel();
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
        view.setOpaque(false);
        view.setBorder(new EmptyBorder(10, 30, 30, 30));

        // Card 1: Password Policies
        JPanel card1 = createContentCard("User Account Provisioning & Password Governance", "Security rules enforced during user creation and authentication");
        JPanel form1 = new JPanel(new GridLayout(3, 1, 0, 16));
        form1.setOpaque(false);

        defaultPasswordField = new JTextField(20);
        defaultPasswordField.setPreferredSize(new Dimension(200, 36));

        minPasswordLengthSpinner = new JSpinner(new SpinnerNumberModel(8, 6, 20, 1));
        minPasswordLengthSpinner.setPreferredSize(new Dimension(140, 36));

        forcePasswordChangeCheck = new JCheckBox("Require password change upon first user login");
        forcePasswordChangeCheck.setFont(new Font("Segoe UI", Font.BOLD, 13));
        forcePasswordChangeCheck.setOpaque(false);
        forcePasswordChangeCheck.setForeground(new Color(30, 40, 50));

        form1.add(createFieldGroup("Default Temporary Password for New Accounts", defaultPasswordField, "Initial password assigned when admin creates students or lecturers"));
        form1.add(createFieldGroup("Minimum Required Password Length (Characters)", minPasswordLengthSpinner, "Enforced when users update their profile passwords"));
        form1.add(createFieldGroup("First-Time Sign In Policy", forcePasswordChangeCheck, "Prompts users to reset default password immediately after logging in"));
        card1.add(form1, BorderLayout.CENTER);

        // Card 2: Session & Throttling
        JPanel card2 = createContentCard("Session Lifetime & Security Throttling", "Prevent unauthorized session hijacking and brute-force attacks");
        JPanel form2 = new JPanel(new GridLayout(2, 2, 25, 16));
        form2.setOpaque(false);

        maxLoginAttemptsSpinner = new JSpinner(new SpinnerNumberModel(5, 3, 10, 1));
        maxLoginAttemptsSpinner.setPreferredSize(new Dimension(140, 36));

        sessionTimeoutSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 240, 5));
        sessionTimeoutSpinner.setPreferredSize(new Dimension(140, 36));

        form2.add(createFieldGroup("Maximum Failed Login Attempts", maxLoginAttemptsSpinner, "Account temporarily locked after consecutive incorrect attempts"));
        form2.add(createFieldGroup("Session Inactivity Timeout (Minutes)", sessionTimeoutSpinner, "Automatic logout when no mouse or keyboard activity is detected"));
        card2.add(form2, BorderLayout.CENTER);

        view.add(card1);
        view.add(Box.createVerticalStrut(20));
        view.add(card2);
        return view;
    }

    // =========================================================================
    // CATEGORY 4: SYSTEM & MAINTENANCE VIEW
    // =========================================================================
    private JPanel createMaintenanceSettingsView() {
        JPanel view = new JPanel();
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
        view.setOpaque(false);
        view.setBorder(new EmptyBorder(10, 30, 30, 30));

        // Card 1: Maintenance Controls
        JPanel card1 = createContentCard("System Operational Controls", "Emergency controls and automated background tasks");
        JPanel form1 = new JPanel(new GridLayout(3, 1, 0, 16));
        form1.setOpaque(false);

        maintenanceModeCheck = new JCheckBox("Enable Maintenance Mode (Restricts access to administrators only)");
        maintenanceModeCheck.setFont(new Font("Segoe UI", Font.BOLD, 13));
        maintenanceModeCheck.setOpaque(false);
        maintenanceModeCheck.setForeground(new Color(220, 38, 38));
        maintenanceModeCheck.addActionListener(e -> {
            boolean enabled = maintenanceModeCheck.isSelected();
            SettingsDAO.updateSetting("system.maintenance_mode", String.valueOf(enabled));
        });

        noticeExpirySpinner = new JSpinner(new SpinnerNumberModel(30, 7, 180, 1));
        noticeExpirySpinner.setPreferredSize(new Dimension(140, 36));

        emailNotificationsCheck = new JCheckBox("Enable system automated email notification alerts");
        emailNotificationsCheck.setFont(new Font("Segoe UI", Font.BOLD, 13));
        emailNotificationsCheck.setOpaque(false);
        emailNotificationsCheck.setForeground(new Color(30, 40, 50));

        form1.add(createFieldGroup("Maintenance Lockout", maintenanceModeCheck, "When enabled, undergraduates and lecturers will see a maintenance message"));
        form1.add(createFieldGroup("Notice Auto-Expiry Threshold (Days)", noticeExpirySpinner, "Notices older than this period are automatically archived"));
        form1.add(createFieldGroup("Automated Notifications", emailNotificationsCheck, "Dispatches alerts for timetable revisions and priority faculty notices"));
        card1.add(form1, BorderLayout.CENTER);

        // Card 2: Database Operations & Backups
        JPanel card2 = createContentCard("Database Connectivity & Data Protection", "Manage MySQL server connection and on-demand database backups");
        JPanel content2 = new JPanel(new BorderLayout(0, 16));
        content2.setOpaque(false);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        statusPanel.setBackground(new Color(245, 248, 255));
        statusPanel.setBorder(BorderFactory.createLineBorder(new Color(215, 225, 245), 1, true));

        dbStatusLabel = new JLabel("● Database Status: Checking connection...");
        dbStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        dbStatusLabel.setForeground(new Color(22, 163, 74));

        JButton testConnBtn = createStyledButton("⚡ Test Connection", new Color(230, 238, 255), new Color(21, 101, 192), true);
        testConnBtn.setPreferredSize(new Dimension(150, 30));
        testConnBtn.addActionListener(e -> testDatabaseConnection());

        statusPanel.add(dbStatusLabel);
        statusPanel.add(Box.createHorizontalStrut(15));
        statusPanel.add(testConnBtn);

        JPanel backupActionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        backupActionPanel.setOpaque(false);

        JButton backupBtn = createStyledButton("📥 Backup Database Now (.sql)", new Color(21, 101, 192), Color.WHITE, false);
        backupBtn.setPreferredSize(new Dimension(240, 38));
        backupBtn.addActionListener(e -> performDatabaseBackup());

        JLabel backupNote = new JLabel("Exports all 17 tables, records, curriculum, and users into a standalone SQL script.");
        backupNote.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        backupNote.setForeground(new Color(110, 120, 135));

        backupActionPanel.add(backupBtn);
        backupActionPanel.add(backupNote);

        content2.add(statusPanel, BorderLayout.NORTH);
        content2.add(backupActionPanel, BorderLayout.CENTER);
        card2.add(content2, BorderLayout.CENTER);

        view.add(card1);
        view.add(Box.createVerticalStrut(20));
        view.add(card2);

        testDatabaseConnection();
        return view;
    }

    // =========================================================================
    // DATA PERSISTENCE (LOAD & SAVE)
    // =========================================================================
    private void loadSettingsFromDatabase() {
        Map<String, String> settings = SettingsDAO.getAllSettings();

        // Academic
        academicYearCombo.setSelectedItem(settings.getOrDefault("academic.year", "2024/2025"));
        semesterCombo.setSelectedItem(settings.getOrDefault("academic.semester", "Semester 1"));
        attendanceThresholdSpinner.setValue(parseIntSafe(settings.get("academic.attendance_threshold"), 80));
        regStatusCombo.setSelectedItem(settings.getOrDefault("academic.registration_status", "Open"));
        medicalGraceDaysSpinner.setValue(parseIntSafe(settings.get("academic.medical_grace_days"), 14));

        // Institution
        instNameField.setText(settings.getOrDefault("faculty.institution_name", "University of Ruhuna"));
        facultyNameField.setText(settings.getOrDefault("faculty.name", "Faculty of Technology"));
        systemNameField.setText(settings.getOrDefault("faculty.system_name", "FAMS - Faculty Academic Management System"));
        contactEmailField.setText(settings.getOrDefault("faculty.email", "admin@fot.ruh.ac.lk"));
        contactPhoneField.setText(settings.getOrDefault("faculty.phone", "+94 41 2245765"));
        campusAddressField.setText(settings.getOrDefault("faculty.address", "Karagoda Uyangoda, Kamburupitiya, Sri Lanka"));

        // Security
        defaultPasswordField.setText(settings.getOrDefault("security.default_password", "default123"));
        minPasswordLengthSpinner.setValue(parseIntSafe(settings.get("security.min_password_length"), 8));
        maxLoginAttemptsSpinner.setValue(parseIntSafe(settings.get("security.max_login_attempts"), 5));
        sessionTimeoutSpinner.setValue(parseIntSafe(settings.get("security.session_timeout"), 30));
        forcePasswordChangeCheck.setSelected("true".equalsIgnoreCase(settings.get("security.force_password_change")));

        // Maintenance
        maintenanceModeCheck.setSelected("true".equalsIgnoreCase(settings.get("system.maintenance_mode")));
        noticeExpirySpinner.setValue(parseIntSafe(settings.get("system.notice_expiry_days"), 30));
        emailNotificationsCheck.setSelected("true".equalsIgnoreCase(settings.get("system.email_notifications")));
    }

    private void saveSettingsToDatabase() {
        Map<String, String> updated = new HashMap<>();

        // Academic
        updated.put("academic.year", (String) academicYearCombo.getSelectedItem());
        updated.put("academic.semester", (String) semesterCombo.getSelectedItem());
        updated.put("academic.attendance_threshold", attendanceThresholdSpinner.getValue().toString());
        updated.put("academic.registration_status", (String) regStatusCombo.getSelectedItem());
        updated.put("academic.medical_grace_days", medicalGraceDaysSpinner.getValue().toString());

        // Institution
        updated.put("faculty.institution_name", instNameField.getText().trim());
        updated.put("faculty.name", facultyNameField.getText().trim());
        updated.put("faculty.system_name", systemNameField.getText().trim());
        updated.put("faculty.email", contactEmailField.getText().trim());
        updated.put("faculty.phone", contactPhoneField.getText().trim());
        updated.put("faculty.address", campusAddressField.getText().trim());

        // Security
        updated.put("security.default_password", defaultPasswordField.getText().trim());
        updated.put("security.min_password_length", minPasswordLengthSpinner.getValue().toString());
        updated.put("security.max_login_attempts", maxLoginAttemptsSpinner.getValue().toString());
        updated.put("security.session_timeout", sessionTimeoutSpinner.getValue().toString());
        updated.put("security.force_password_change", String.valueOf(forcePasswordChangeCheck.isSelected()));

        // Maintenance
        updated.put("system.maintenance_mode", String.valueOf(maintenanceModeCheck.isSelected()));
        updated.put("system.notice_expiry_days", noticeExpirySpinner.getValue().toString());
        updated.put("system.email_notifications", String.valueOf(emailNotificationsCheck.isSelected()));

        boolean success = SettingsDAO.updateSettings(updated);
        if (success) {
            ModernDialog.showMessageDialog(
                this,
                "All system settings have been saved and applied successfully!",
                "Settings Saved",
                ModernDialog.INFORMATION_MESSAGE
            );
        } else {
            ModernDialog.showMessageDialog(
                this,
                "Failed to update settings in the database. Please check connection.",
                "Save Failed",
                ModernDialog.ERROR_MESSAGE
            );
        }
    }

    private void resetCurrentCategoryDefaults() {
        int confirm = ModernDialog.showConfirmDialog(
            this,
            "Are you sure you want to reset all settings in '" + currentTab + "' to institutional defaults?",
            "Confirm Reset",
            ModernDialog.YES_NO_OPTION,
            ModernDialog.WARNING_MESSAGE
        );

        if (confirm == ModernDialog.YES_OPTION) {
            boolean success = SettingsDAO.resetCategoryToDefaults(currentTab);
            if (success) {
                loadSettingsFromDatabase();
                ModernDialog.showMessageDialog(this, "Settings for " + currentTab + " restored to defaults.", "Reset Successful", ModernDialog.INFORMATION_MESSAGE);
            }
        }
    }

    private void testDatabaseConnection() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                dbStatusLabel.setText("● Connected to MySQL (127.0.0.1:3306 / tecfams_db) - Operational");
                dbStatusLabel.setForeground(new Color(22, 163, 74));
            } else {
                dbStatusLabel.setText("✕ Disconnected from Database");
                dbStatusLabel.setForeground(new Color(220, 38, 38));
            }
        } catch (Exception e) {
            dbStatusLabel.setText("✕ Connection Error: " + e.getMessage());
            dbStatusLabel.setForeground(new Color(220, 38, 38));
        }
    }

    private void performDatabaseBackup() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String defaultName = "tecfams_backup_" + timeStamp + ".sql";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Database SQL Backup");
        fileChooser.setSelectedFile(new File(defaultName));

        int res = fileChooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File targetFile = fileChooser.getSelectedFile();
            if (!targetFile.getName().toLowerCase().endsWith(".sql")) {
                targetFile = new File(targetFile.getAbsolutePath() + ".sql");
            }

            boolean success = SettingsDAO.exportDatabaseBackup(targetFile);
            if (success) {
                ModernDialog.showMessageDialog(
                    this,
                    "Database backup generated successfully!\nFile: " + targetFile.getAbsolutePath() + "\nSize: " + (targetFile.length() / 1024) + " KB",
                    "Backup Complete",
                    ModernDialog.INFORMATION_MESSAGE
                );
            } else {
                ModernDialog.showMessageDialog(this, "Failed to export database backup.", "Backup Error", ModernDialog.ERROR_MESSAGE);
            }
        }
    }

    private int parseIntSafe(String val, int defaultVal) {
        if (val == null) return defaultVal;
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            return defaultVal;
        }
    }

    // =========================================================================
    // UI HELPERS & CARDS
    // =========================================================================
    private JPanel createContentCard(String title, String subtitle) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(new Color(230, 232, 238));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(18, 20, 20, 20));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(new Color(23, 32, 48));

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(new Color(120, 130, 145));

        header.add(titleLbl);
        header.add(Box.createVerticalStrut(3));
        header.add(subLbl);
        header.add(Box.createVerticalStrut(8));

        card.add(header, BorderLayout.NORTH);
        return card;
    }

    private JPanel createFieldGroup(String labelText, JComponent component, String helperText) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(40, 50, 65));

        component.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel helper = new JLabel(helperText);
        helper.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        helper.setForeground(new Color(130, 140, 155));

        group.add(label);
        group.add(Box.createVerticalStrut(5));
        group.add(component);
        group.add(Box.createVerticalStrut(3));
        group.add(helper);

        return group;
    }

    private JButton createStyledButton(String text, Color bg, Color fg, boolean border) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                if (border) {
                    g2.setColor(new Color(215, 220, 230));
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                }
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
