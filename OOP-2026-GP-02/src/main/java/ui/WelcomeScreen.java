package main.java.ui;
import main.java.ui.admin.AdminDashboard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.imageio.ImageIO;
import java.io.InputStream;

public class WelcomeScreen extends JFrame {
    public WelcomeScreen() {
        setTitle("TecFAMS");
        setSize(600, 250);
        setResizable(false);
        setUndecorated(true); // Modern borderless splash screen look
        setLocationRelativeTo(null); // Center on screen

        // Main panel with custom painted modern abstract cover graphic
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            private Image bgImage;

            {
                try {
                    InputStream is = getClass().getResourceAsStream("/images/welcome_cover.jpg");
                    if (is != null) {
                        bgImage = ImageIO.read(is);
                    } else {
                        java.io.File file = new java.io.File("src/main/resources/images/welcome_cover.jpg");
                        if (file.exists()) {
                            bgImage = ImageIO.read(file);
                        }
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };

        // Add a subtle sleek border matching the light theme
        mainPanel.setBorder(BorderFactory.createLineBorder(new Color(195, 215, 240), 1));

        // Texts
        JLabel titleLabel = new JLabel("TecFAMS");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
        titleLabel.setForeground(new Color(21, 101, 192)); // Signature #1565C0
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitleLabel = new JLabel("Faculty Academic Management System");
        subtitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        subtitleLabel.setForeground(new Color(80, 100, 125)); // Refined slate-blue for clear readability
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);
        textPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // Login Button (Prominent #1565C0 primary button)
        JButton loginButton = new JButton("Get Started / Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(new Color(21, 101, 192)); // #1565C0 theme blue
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setOpaque(true);
        loginButton.setPreferredSize(new Dimension(200, 40));

        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(13, 71, 161)); // #0D47A1 hover
            }
            @Override
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(new Color(21, 101, 192));
            }
        });

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close the welcome screen
                new LoginUI().setVisible(true); // Open the login screen
            }
        });

        // Top right close button (because undecorated frames don't have OS controls)
        JButton closeButton = new JButton("X");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeButton.setForeground(new Color(130, 145, 165));
        closeButton.setBackground(new Color(0, 0, 0, 0));
        closeButton.setOpaque(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setForeground(Color.RED);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setForeground(new Color(130, 145, 165));
            }
        });
        closeButton.addActionListener(e -> System.exit(0));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        topPanel.setOpaque(false);
        topPanel.add(closeButton);

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonWrapper.setOpaque(false);
        buttonWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        buttonWrapper.add(loginButton);

        JLabel versionLabel = new JLabel("v1.0.0 ");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setForeground(new Color(130, 150, 175));
        versionLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));
        bottomPanel.add(buttonWrapper, BorderLayout.CENTER);
        bottomPanel.add(versionLabel, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(textPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);


    }
}
