package main.java.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class AdminDashboard {
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JButton activeButton;

    public AdminDashboard() {
        setTitle("FAMS - Faculty Academic Management System");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

}
