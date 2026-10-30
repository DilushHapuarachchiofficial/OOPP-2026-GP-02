package main.java;

import main.java.ui.TechnicalOfficer.TechnicalOfficerDashboard;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TechnicalOfficerDashboard().setVisible(true);
        });
    }
}
