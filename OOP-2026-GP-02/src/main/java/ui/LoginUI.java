package main.java.ui;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;

public class LoginUI extends JFrame {

    // =========================================================================
    // COLOR PALETTE (High-Contrast Academic & Technology Theme)
    // =========================================================================
    private static final Color COLOR_DEEP_NAVY = new Color(11, 31, 58);    // #0B1F3A
    private static final Color COLOR_DARK_BLUE = new Color(10, 37, 64);    // #0A2540
    private static final Color COLOR_TECH_BLUE = new Color(21, 101, 192);  // #1565C0
    private static final Color COLOR_BRIGHT_BLUE = new Color(33, 150, 243);  // #2196F3
    private static final Color COLOR_LIGHT_BG = new Color(244, 247, 251); // #F4F7FB
    private static final Color COLOR_DARK_TEXT = new Color(15, 23, 42);    // #0F172A
    private static final Color COLOR_SLATE_TEXT = new Color(51, 65, 85);    // #334155
    private static final Color COLOR_SECONDARY_TEXT = new Color(100, 116, 139); // #64748B
    private static final Color COLOR_BORDER = new Color(203, 213, 225); // #CBD5E1
    private static final Color COLOR_ERROR = new Color(220, 38, 38);   // #DC2626
    private static final Color COLOR_ERROR_BG = new Color(254, 242, 242); // #FEF2F2
    private static final Color COLOR_WHITE = Color.WHITE;

    // =========================================================================
    // FONTS (Windows Segoe UI Hierarchy)
    // =========================================================================
    private static final Font FONT_WELCOME = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_LABEL = new Font("Segoe UI Semibold", Font.BOLD, 13);
    private static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BUTTON = new Font("Segoe UI Semibold", Font.BOLD, 15);
    private static final Font FONT_ERROR = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_FOOTER = new Font("Segoe UI", Font.PLAIN, 11);

    // =========================================================================
    // GUI COMPONENTS DECLARATION
    // =========================================================================
    private static JFrame frame;

    private static JPanel mainPanel;
    private static JPanel leftPanel;
    private static JPanel rightPanel;
    private static JPanel errorPanel;

    private static JLabel welcomeLabel;
    private static JLabel subtitleLabel;
    private static JLabel usernameLabel;
    private static JLabel passwordLabel;
    private static JLabel errorLabel;
    private static JLabel footerLabel;

    private static JTextField usernameField;
    private static JPasswordField passwordField;

    private static JButton togglePasswordButton;
    private static JButton loginButton;

    private static boolean isPasswordVisible = false;

    // Cached Background Image for Left Panel
    private Image leftBgImage = null;

    /**
     * Custom Vector Icon for Password Eye Toggle to ensure consistent rendering across OS platform fonts.
     */
    private static class EyeIcon implements Icon {
        private final boolean showEye;
        private final int width;
        private final int height;

        public EyeIcon(boolean showEye, int width, int height) {
            this.showEye = showEye;
            this.width = width;
            this.height = height;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cx = x + width / 2;
            int cy = y + height / 2;
            int w = 18;
            int h = 11;

            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(COLOR_SECONDARY_TEXT);

            // Draw Eye Outer Outline
            java.awt.geom.Path2D eyePath = new java.awt.geom.Path2D.Double();
            eyePath.moveTo(cx - w / 2.0, cy);
            eyePath.quadTo(cx, cy - h, cx + w / 2.0, cy);
            eyePath.quadTo(cx, cy + h, cx - w / 2.0, cy);
            g2.draw(eyePath);

            // Draw Pupil
            int pSize = 6;
            if (showEye) {
                g2.fillOval(cx - pSize / 2, cy - pSize / 2, pSize, pSize);
            } else {
                g2.fillOval(cx - pSize / 2, cy - pSize / 2, pSize, pSize);
                // Draw diagonal slash for hidden state
                g2.setColor(COLOR_ERROR);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx - w / 2 + 1, cy + h / 2 + 2, cx + w / 2 - 1, cy - h / 2 - 2);
            }

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }
    }
    /**
     * Constructor - Builds and initializes the Login GUI window.
     */
    public LoginUI() {
        initUI();
    }

    /**
     * Initializes frame properties and lays out components.
     */
    private void initUI() {
        // Frame Settings
        frame = this;
        setTitle("TecFAMS - Faculty of Technology | University of Ruhuna");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null); // Center on screen

        // Main Container Panel
        mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.setBackground(COLOR_LIGHT_BG);

        // Left Panel (Branding Image with Opacity & Centered Text)
        leftPanel = createLeftBrandingPanel();

        // Right Panel (Login Form)
        rightPanel = createRightFormPanel();

        // Add Panels to Main Container
        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        add(mainPanel);
    }

    // =========================================================================
    // LEFT BRANDING PANEL CREATION
    // =========================================================================
    private JPanel createLeftBrandingPanel() {
        // Panel rendering the background image with AlphaComposite opacity
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                Image bg = loadLeftBgImage();
                if (bg != null) {
                    // Set opacity using AlphaComposite (0.25f)
                    float opacity = 0.55f;
                    g2.setComposite(
                            AlphaComposite.getInstance(
                                    AlphaComposite.SRC_OVER,
                                    opacity
                            )
                    );

                    // Draw image
                    g2.drawImage(
                            bg,
                            0,
                            0,
                            getWidth(),
                            getHeight(),
                            this
                    );
                } else {
                    // Gradient fallback if image file is not found
                    GradientPaint bgGradient = new GradientPaint(
                            0, 0, COLOR_DEEP_NAVY,
                            getWidth(), getHeight(), new Color(5, 15, 30)
                    );
                    g2.setPaint(bgGradient);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
            }
        };

        panel.setBackground(COLOR_LIGHT_BG);
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(35, 35, 35, 35));

        // Content Panel inside Left Panel for text branding
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));



        // 2. System Branding Titles
        JLabel titleLabel = new JLabel("TecFAMS");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        titleLabel.setForeground(COLOR_TECH_BLUE); // Rich Dark Blue #0A2540
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subLabel = new JLabel("Faculty of Technology");
        subLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 17));
        subLabel.setForeground(COLOR_TECH_BLUE); // Bright Tech Blue #1565C0
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel systemLabel = new JLabel("Academic Management System");
        systemLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        systemLabel.setForeground(COLOR_SLATE_TEXT); // #334155
        systemLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        systemLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Decorative Separator
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(260, 2));
        separator.setForeground(new Color(21, 101, 192, 120));
        separator.setBackground(new Color(21, 101, 192, 120));
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Description Paragraph
        JLabel descLabel = new JLabel("<html><body style='width: 300px; text-align: center; color: #334155; font-family: Segoe UI; font-size: 10pt;'>" +
                "A centralized digital academic portal designed for the Faculty of Technology, " +
                "providing seamless access for undergraduates, academics, and administration." +
                "</body></html>");
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Feature Highlights
        JLabel feat1 = createFeatureLabel("✓ Secure Role-Based Authentication");
        JLabel feat2 = createFeatureLabel("✓ Real-Time Academic Portal");
        JLabel feat3 = createFeatureLabel("✓ Integrated Faculty Resources");

        // Assemble Content Box
        contentPanel.add(Box.createRigidArea(new Dimension(0, 18)));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        contentPanel.add(subLabel);
        contentPanel.add(systemLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        contentPanel.add(separator);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        contentPanel.add(descLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 22)));
        contentPanel.add(feat1);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        contentPanel.add(feat2);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        contentPanel.add(feat3);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }
    /**
     * Custom Vector Icon for Feature Checkmarks to ensure reliable rendering on all OS platform fonts.
     */
    private static class CheckIcon implements Icon {
        private final int width = 16;
        private final int height = 16;

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COLOR_TECH_BLUE);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Draw Checkmark vector polyline
            int[] xPoints = {x + 2, x + 6, x + 13};
            int[] yPoints = {y + 8, y + 12, y + 4};
            g2.drawPolyline(xPoints, yPoints, 3);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }
    }

    private JLabel createFeatureLabel(String text) {
        String cleanText = (text != null && text.startsWith("✓ ")) ? text.substring(2) : text;
        JLabel label = new JLabel(cleanText, SwingConstants.CENTER);
        label.setIcon(new CheckIcon());
        label.setIconTextGap(6);
        label.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        label.setForeground(COLOR_DARK_TEXT); // #0F172A
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private Image loadLeftBgImage() {
        if (leftBgImage != null) return leftBgImage;
        String[] candidatePaths = {
                "bg.png", "bg.jpg", "left_bg.png",
                "src/bg.png", "src/bg.jpg", "src/left_bg.png"
        };
        for (String path : candidatePaths) {
            java.io.File f = new java.io.File(path);
            if (f.exists()) {
                leftBgImage = new ImageIcon(f.getAbsolutePath()).getImage();
                return leftBgImage;
            }
        }
        java.net.URL resource = getClass().getResource("/bg.png");
        if (resource != null) {
            leftBgImage = new ImageIcon(resource).getImage();
            return leftBgImage;
        }
        return null;
    }
    // =========================================================================
    // RIGHT LOGIN FORM PANEL CREATION
    // =========================================================================
    private JPanel createRightFormPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_LIGHT_BG);
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(40, 50, 30, 50));

        // Center Form Box Container
        JPanel formContainer = new JPanel();
        formContainer.setOpaque(false);
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));

        // Header Section
        welcomeLabel = new JLabel("Welcome");
        welcomeLabel.setFont(FONT_WELCOME);
        welcomeLabel.setForeground(COLOR_DARK_TEXT);
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        subtitleLabel = new JLabel("Sign in to access your academic portal.");
        subtitleLabel.setFont(FONT_SUBTITLE);
        subtitleLabel.setForeground(COLOR_SECONDARY_TEXT);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Error Message Panel (Hidden by default)
        errorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        errorPanel.setBackground(COLOR_ERROR_BG);
        errorPanel.setBorder(BorderFactory.createLineBorder(new Color(252, 165, 165), 1, true));
        errorPanel.setMaximumSize(new Dimension(400, 36));
        errorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorPanel.setVisible(false);

        errorLabel = new JLabel();
        errorLabel.setFont(FONT_ERROR);
        errorLabel.setForeground(COLOR_ERROR);
        errorPanel.add(errorLabel);

        // 1. Username Field
        usernameLabel = new JLabel("Username");
        usernameLabel.setFont(FONT_LABEL);
        usernameLabel.setForeground(COLOR_DARK_TEXT);
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = new JTextField();
        usernameField.setFont(FONT_INPUT);
        usernameField.setForeground(COLOR_DARK_TEXT);
        usernameField.setMaximumSize(new Dimension(400, 40));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        applyCustomInputStyling(usernameField);

        // 2. Password Field with Show/Hide Toggle
        passwordLabel = new JLabel("Password");
        passwordLabel.setFont(FONT_LABEL);
        passwordLabel.setForeground(COLOR_DARK_TEXT);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField();
        passwordField.setFont(FONT_INPUT);
        passwordField.setForeground(COLOR_DARK_TEXT);
        applyCustomInputStyling(passwordField);

        // Password Container Panel (holds Password field + Vector Eye Toggle Button)
        JPanel passwordContainerPanel = new JPanel(new BorderLayout());
        passwordContainerPanel.setOpaque(false);
        passwordContainerPanel.setMaximumSize(new Dimension(400, 40));
        passwordContainerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        togglePasswordButton = new JButton(new EyeIcon(false, 22, 22));
        togglePasswordButton.setFocusPainted(false);
        togglePasswordButton.setBorderPainted(false);
        togglePasswordButton.setContentAreaFilled(false);
        togglePasswordButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        togglePasswordButton.setToolTipText("Show / Hide Password");
        togglePasswordButton.setPreferredSize(new Dimension(45, 40));

        // Password Show/Hide Event Listener
        togglePasswordButton.addActionListener(e -> togglePasswordVisibility());

        passwordContainerPanel.add(passwordField, BorderLayout.CENTER);
        passwordContainerPanel.add(togglePasswordButton, BorderLayout.EAST);

        // 3. Sign In Button
        loginButton = new JButton("SIGN IN");
        loginButton.setFont(FONT_BUTTON);
        loginButton.setForeground(COLOR_WHITE);
        loginButton.setBackground(COLOR_TECH_BLUE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setMaximumSize(new Dimension(400, 44));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Button Hover & Click Effects
        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(COLOR_BRIGHT_BLUE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(COLOR_TECH_BLUE);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                loginButton.setBackground(COLOR_DEEP_NAVY);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                loginButton.setBackground(COLOR_BRIGHT_BLUE);
            }
        });
}
