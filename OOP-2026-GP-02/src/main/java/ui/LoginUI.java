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
}
