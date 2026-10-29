package main.java.ui.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern, sleek, aesthetic replacement for Swing's JOptionPane.
 * Features rounded card layout, soft drop-shadow, custom vector icons,
 * modern typography, and hover-animated pill buttons.
 */
public class ModernDialog extends JDialog {

    // JOptionPane compatibility constants
    public static final int DEFAULT_OPTION = -1;
    public static final int YES_NO_OPTION = 0;
    public static final int YES_NO_CANCEL_OPTION = 1;
    public static final int OK_CANCEL_OPTION = 2;

    public static final int YES_OPTION = 0;
    public static final int NO_OPTION = 1;
    public static final int CANCEL_OPTION = 2;
    public static final int OK_OPTION = 0;
    public static final int CLOSED_OPTION = -1;

    public static final int ERROR_MESSAGE = 0;
    public static final int INFORMATION_MESSAGE = 1;
    public static final int WARNING_MESSAGE = 2;
    public static final int QUESTION_MESSAGE = 3;
    public static final int PLAIN_MESSAGE = -1;

    public enum DialogType {
        SUCCESS, INFO, WARNING, ERROR, CONFIRM
    }

    private int userChoice = CLOSED_OPTION;
    private Point initialClick;

    public ModernDialog(Window owner, String title, String message, DialogType type, int optionType, boolean isDestructive) {
        super(owner, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        try {
            setBackground(new Color(0, 0, 0, 0));
        } catch (Exception ignored) {}

        buildUI(title, message, type, optionType, isDestructive);
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI(String title, String message, DialogType type, int optionType, boolean isDestructive) {
        // Shadow margin
        final int SHADOW_SIZE = 8;
        final int CORNER_RADIUS = 16;

        JPanel rootPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();

                // Draw multi-layered soft drop shadow
                for (int i = 0; i < SHADOW_SIZE; i++) {
                    int alpha = (int) (16 * (1.0f - (float) i / SHADOW_SIZE));
                    g2.setColor(new Color(15, 23, 42, alpha));
                    g2.fill(new RoundRectangle2D.Float(
                        i, i + 2,
                        width - (i * 2), height - (i * 2),
                        CORNER_RADIUS + (SHADOW_SIZE - i), CORNER_RADIUS + (SHADOW_SIZE - i)
                    ));
                }

                // Draw main card background
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(
                    SHADOW_SIZE, SHADOW_SIZE,
                    width - (SHADOW_SIZE * 2), height - (SHADOW_SIZE * 2),
                    CORNER_RADIUS, CORNER_RADIUS
                ));

                // Draw subtle card outline border
                g2.setColor(new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(
                    SHADOW_SIZE, SHADOW_SIZE,
                    width - (SHADOW_SIZE * 2) - 1, height - (SHADOW_SIZE * 2) - 1,
                    CORNER_RADIUS, CORNER_RADIUS
                ));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        rootPanel.setOpaque(false);
        rootPanel.setBorder(new EmptyBorder(SHADOW_SIZE, SHADOW_SIZE, SHADOW_SIZE, SHADOW_SIZE));

        // Enable dragging the dialog
        rootPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });
        rootPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (initialClick != null) {
                    int thisX = getLocation().x;
                    int thisY = getLocation().y;
                    int xMoved = e.getX() - initialClick.x;
                    int yMoved = e.getY() - initialClick.y;
                    setLocation(thisX + xMoved, thisY + yMoved);
                }
            }
        });

        JPanel contentCard = new JPanel(new BorderLayout());
        contentCard.setOpaque(false);
        contentCard.setBorder(new EmptyBorder(16, 20, 18, 20));

        // Top bar with close '✕' button
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JButton closeBtn = new JButton() {
            private boolean hovered = false;
            {
                setFocusPainted(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                setPreferredSize(new Dimension(28, 28));

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
                if (hovered) {
                    g2.setColor(new Color(241, 245, 249));
                    g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
                }
                g2.setColor(hovered ? new Color(15, 23, 42) : new Color(148, 163, 184));
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                int r = 4;
                g2.drawLine(cx - r, cy - r, cx + r, cy + r);
                g2.drawLine(cx + r, cy - r, cx - r, cy + r);
                g2.dispose();
            }
        };
        closeBtn.addActionListener(e -> {
            userChoice = CLOSED_OPTION;
            dispose();
        });
        topBar.add(closeBtn, BorderLayout.EAST);
        contentCard.add(topBar, BorderLayout.NORTH);

        // Center Area: Icon Badge (Left) + Title & Message (Right)
        JPanel centerPanel = new JPanel(new BorderLayout(16, 0));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(4, 4, 16, 4));

        // Modern Icon Badge
        IconBadge iconBadge = new IconBadge(type, isDestructive);
        centerPanel.add(iconBadge, BorderLayout.WEST);

        // Text block
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setForeground(new Color(15, 23, 42)); // Slate-900
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(titleLbl);
        textPanel.add(Box.createVerticalStrut(6));

        // Format message with line breaks and wrapping
        String formattedMsg = "<html><body style='width: 310px; font-family: Segoe UI, sans-serif; font-size: 13px; color: #475569; line-height: 1.45;'>"
            + message.replace("\n", "<br>") + "</body></html>";
        JLabel messageLbl = new JLabel(formattedMsg);
        messageLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(messageLbl);

        centerPanel.add(textPanel, BorderLayout.CENTER);
        contentCard.add(centerPanel, BorderLayout.CENTER);

        // Bottom Actions Bar
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonBar.setOpaque(false);

        if (optionType == YES_NO_OPTION || optionType == OK_CANCEL_OPTION) {
            String cancelText = (optionType == YES_NO_OPTION) ? "No" : "Cancel";
            String confirmText = (optionType == YES_NO_OPTION) ? "Yes" : "OK";

            JButton cancelBtn = createSecondaryButton(cancelText);
            cancelBtn.addActionListener(e -> {
                userChoice = (optionType == YES_NO_OPTION) ? NO_OPTION : CANCEL_OPTION;
                dispose();
            });

            JButton confirmBtn = createPrimaryButton(confirmText, type, isDestructive);
            confirmBtn.addActionListener(e -> {
                userChoice = (optionType == YES_NO_OPTION) ? YES_OPTION : OK_OPTION;
                dispose();
            });

            buttonBar.add(cancelBtn);
            buttonBar.add(confirmBtn);
            getRootPane().setDefaultButton(confirmBtn);
        } else {
            // Standard single OK button
            JButton okBtn = createPrimaryButton("OK", type, false);
            okBtn.addActionListener(e -> {
                userChoice = OK_OPTION;
                dispose();
            });
            buttonBar.add(okBtn);
            getRootPane().setDefaultButton(okBtn);
        }

        contentCard.add(buttonBar, BorderLayout.SOUTH);
        rootPanel.add(contentCard, BorderLayout.CENTER);
        setContentPane(rootPanel);

        // Escape key listener to close
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().registerKeyboardAction(e -> {
            userChoice = CLOSED_OPTION;
            dispose();
        }, escapeKeyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private JButton createPrimaryButton(String text, DialogType type, boolean isDestructive) {
        Color baseBg;
        Color hoverBg;

        if (isDestructive) {
            baseBg = new Color(220, 38, 38);   // Crimson red
            hoverBg = new Color(185, 28, 28);
        } else if (type == DialogType.SUCCESS) {
            baseBg = new Color(16, 149, 79);   // Vibrant Emerald
            hoverBg = new Color(12, 120, 63);
        } else if (type == DialogType.WARNING) {
            baseBg = new Color(217, 119, 6);   // Amber
            hoverBg = new Color(180, 83, 9);
        } else {
            baseBg = new Color(21, 101, 192);  // Royal Blue (#1565C0)
            hoverBg = new Color(13, 71, 161);  // Darker Blue (#0D47A1)
        }

        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setForeground(Color.WHITE);
                setFocusPainted(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                setPreferredSize(new Dimension(Math.max(90, text.length() * 12 + 36), 36));

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
                g2.setColor(hovered ? hoverBg : baseBg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        return btn;
    }

    private JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setForeground(new Color(71, 85, 105)); // Slate-600
                setFocusPainted(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                setPreferredSize(new Dimension(Math.max(85, text.length() * 12 + 32), 36));

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        setForeground(new Color(15, 23, 42));
                        repaint();
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        setForeground(new Color(71, 85, 105));
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                g2.setColor(hovered ? new Color(241, 245, 249) : Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));

                // Border
                g2.setColor(hovered ? new Color(148, 163, 184) : new Color(203, 213, 225));
                g2.setStroke(new BasicStroke(1.1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 8, 8));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        return btn;
    }

    /**
     * Custom-painted vector icon badge with pastel tinted circle & crisp symbol
     */
    private static class IconBadge extends JComponent {
        private final DialogType type;
        private final boolean isDestructive;

        public IconBadge(DialogType type, boolean isDestructive) {
            this.type = type;
            this.isDestructive = isDestructive;
            setPreferredSize(new Dimension(50, 50));
            setMinimumSize(new Dimension(50, 50));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            int size = 48;
            int x = (getWidth() - size) / 2;
            int y = 0;

            Color bgCircle;
            Color ringBorder;
            Color iconColor;

            if (isDestructive) {
                bgCircle = new Color(254, 242, 242);     // Rose-50
                ringBorder = new Color(254, 202, 202);   // Rose-200
                iconColor = new Color(220, 38, 38);      // Red-600
            } else if (type == DialogType.SUCCESS) {
                bgCircle = new Color(236, 253, 245);     // Emerald-50
                ringBorder = new Color(167, 243, 208);   // Emerald-200
                iconColor = new Color(16, 163, 74);      // Emerald-600
            } else if (type == DialogType.WARNING) {
                bgCircle = new Color(254, 243, 199);     // Amber-100
                ringBorder = new Color(253, 230, 138);   // Amber-200
                iconColor = new Color(217, 119, 6);      // Amber-600
            } else if (type == DialogType.ERROR) {
                bgCircle = new Color(254, 242, 242);     // Rose-50
                ringBorder = new Color(254, 202, 202);   // Rose-200
                iconColor = new Color(239, 68, 68);      // Rose-500
            } else {
                bgCircle = new Color(239, 246, 255);     // Blue-50
                ringBorder = new Color(191, 219, 254);   // Blue-200
                iconColor = new Color(37, 99, 235);      // Blue-600
            }

            // Fill Background Circle
            g2.setColor(bgCircle);
            g2.fillOval(x, y, size, size);

            // Ring Border
            g2.setColor(ringBorder);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(x, y, size, size);

            // Draw Icon Symbol
            g2.setColor(iconColor);

            if (type == DialogType.SUCCESS) {
                // Vector Checkmark
                Path2D check = new Path2D.Float();
                check.moveTo(x + size * 0.28f, y + size * 0.52f);
                check.lineTo(x + size * 0.44f, y + size * 0.68f);
                check.lineTo(x + size * 0.73f, y + size * 0.35f);
                g2.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(check);
            } else if (type == DialogType.ERROR || isDestructive) {
                // Vector 'X'
                float pad = size * 0.33f;
                g2.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine((int)(x + pad), (int)(y + pad), (int)(x + size - pad), (int)(y + size - pad));
                g2.drawLine((int)(x + size - pad), (int)(y + pad), (int)(x + pad), (int)(y + size - pad));
            } else if (type == DialogType.WARNING) {
                // Vector '!'
                g2.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine((int)(x + size * 0.5f), (int)(y + size * 0.28f), (int)(x + size * 0.5f), (int)(y + size * 0.58f));
                g2.fillOval((int)(x + size * 0.5f - 2f), (int)(y + size * 0.69f), 4, 4);
            } else if (type == DialogType.CONFIRM) {
                // Vector '?'
                Font orig = g2.getFont();
                g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
                FontMetrics fm = g2.getFontMetrics();
                int textX = x + (size - fm.stringWidth("?")) / 2;
                int textY = y + ((size - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString("?", textX, textY);
                g2.setFont(orig);
            } else {
                // Vector Info 'i'
                g2.fillOval((int)(x + size * 0.5f - 2.5f), (int)(y + size * 0.26f), 5, 5);
                g2.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine((int)(x + size * 0.5f), (int)(y + size * 0.44f), (int)(x + size * 0.5f), (int)(y + size * 0.72f));
            }

            g2.dispose();
        }
    }

    // =========================================================================
    // SMART STATIC HELPERS & JOPTIONPANE COMPATIBILITY METHODS
    // =========================================================================

    private static Window getWindowForComponent(Component parent) {
        if (parent == null) return null;
        if (parent instanceof Window) return (Window) parent;
        return SwingUtilities.getWindowAncestor(parent);
    }

    private static DialogType inferDialogType(String title, String message, int messageType) {
        if (messageType == ERROR_MESSAGE) return DialogType.ERROR;
        if (messageType == WARNING_MESSAGE) return DialogType.WARNING;
        if (messageType == QUESTION_MESSAGE) return DialogType.CONFIRM;

        String combined = ((title != null ? title : "") + " " + (message != null ? message : "")).toLowerCase();
        if (combined.contains("success") || combined.contains("saved") || combined.contains("created")
            || combined.contains("updated") || combined.contains("deleted") || combined.contains("restored")
            || combined.contains("exported") || combined.contains("complete")) {
            return DialogType.SUCCESS;
        }
        if (combined.contains("failed") || combined.contains("error") || combined.contains("incorrect")
            || combined.contains("invalid")) {
            return DialogType.ERROR;
        }
        if (combined.contains("warning") || combined.contains("please fill") || combined.contains("missing")
            || combined.contains("match")) {
            return DialogType.WARNING;
        }
        return DialogType.INFO;
    }

    /**
     * Drop-in replacement for JOptionPane.showMessageDialog(parent, message)
     */
    public static void showMessageDialog(Component parent, Object message) {
        showMessageDialog(parent, message, "Notice", INFORMATION_MESSAGE);
    }

    /**
     * Drop-in replacement for JOptionPane.showMessageDialog(parent, message, title, messageType)
     */
    public static void showMessageDialog(Component parent, Object message, String title, int messageType) {
        String msgStr = String.valueOf(message);
        DialogType type = inferDialogType(title, msgStr, messageType);

        String displayTitle = title;
        if (displayTitle == null || displayTitle.trim().isEmpty() || "Message".equalsIgnoreCase(displayTitle)) {
            switch (type) {
                case SUCCESS: displayTitle = "Success"; break;
                case ERROR: displayTitle = "Error"; break;
                case WARNING: displayTitle = "Warning"; break;
                default: displayTitle = "Notice"; break;
            }
        }

        Window owner = getWindowForComponent(parent);
        ModernDialog dialog = new ModernDialog(owner, displayTitle, msgStr, type, DEFAULT_OPTION, false);
        dialog.setVisible(true);
    }

    /**
     * Drop-in replacement for JOptionPane.showConfirmDialog(parent, message, title, optionType)
     */
    public static int showConfirmDialog(Component parent, Object message, String title, int optionType) {
        return showConfirmDialog(parent, message, title, optionType, QUESTION_MESSAGE);
    }

    /**
     * Drop-in replacement for JOptionPane.showConfirmDialog(parent, message, title, optionType, messageType)
     */
    public static int showConfirmDialog(Component parent, Object message, String title, int optionType, int messageType) {
        String msgStr = String.valueOf(message);
        String displayTitle = (title != null && !title.trim().isEmpty()) ? title : "Confirm Action";

        boolean isDestructive = displayTitle.toLowerCase().contains("delete") || msgStr.toLowerCase().contains("delete")
            || displayTitle.toLowerCase().contains("reset") || msgStr.toLowerCase().contains("reset");

        DialogType type = isDestructive ? DialogType.ERROR : DialogType.CONFIRM;

        Window owner = getWindowForComponent(parent);
        ModernDialog dialog = new ModernDialog(owner, displayTitle, msgStr, type, optionType, isDestructive);
        dialog.setVisible(true);
        return dialog.userChoice;
    }

    // Direct convenience methods
    public static void showSuccess(Component parent, String message) {
        showMessageDialog(parent, message, "Success", INFORMATION_MESSAGE);
    }

    public static void showSuccess(Component parent, String title, String message) {
        showMessageDialog(parent, message, title, INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        showMessageDialog(parent, message, "Error", ERROR_MESSAGE);
    }

    public static void showError(Component parent, String title, String message) {
        showMessageDialog(parent, message, title, ERROR_MESSAGE);
    }

    public static void showWarning(Component parent, String message) {
        showMessageDialog(parent, message, "Warning", WARNING_MESSAGE);
    }

    public static void showWarning(Component parent, String title, String message) {
        showMessageDialog(parent, message, title, WARNING_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        showMessageDialog(parent, message, "Information", INFORMATION_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String title, String message) {
        return showConfirmDialog(parent, message, title, YES_NO_OPTION) == YES_OPTION;
    }
}
