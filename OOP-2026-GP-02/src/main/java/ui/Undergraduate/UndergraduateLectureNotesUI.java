package main.java.ui.Undergraduate;

import main.java.model.Undergraduate.Undergraduate;

import javax.swing.*;
import java.awt.*;

/**
 * TecFAMS - Faculty of Technology Academic Management System
 * Undergraduate Lecture Notes UI Page (Standalone Frame).
 *
 * Wraps UndergraduateLectureNotesPanel for standalone testing or
 * independent window usage.
 */
public class UndergraduateLectureNotesUI extends JFrame {

    private final UndergraduateLectureNotesPanel notesPanel;

    public UndergraduateLectureNotesUI(Undergraduate student, UndergraduateDashboard parentDashboard) {
        setTitle("TecFAMS - Faculty of Technology | My Course Lecture Notes");
        setSize(1100, 680);
        setMinimumSize(new Dimension(850, 520));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        notesPanel = new UndergraduateLectureNotesPanel(student, parentDashboard);
        setContentPane(notesPanel);

        if (parentDashboard != null) {
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    parentDashboard.showDashboardView();
                }
            });
        }
    }

    public UndergraduateLectureNotesUI(Undergraduate student) {
        this(student, null);
    }

    public UndergraduateLectureNotesUI(String studentIdOrRegNo) {
        setTitle("TecFAMS - Faculty of Technology | My Course Lecture Notes");
        setSize(1100, 680);
        setMinimumSize(new Dimension(850, 520));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        notesPanel = new UndergraduateLectureNotesPanel(studentIdOrRegNo);
        setContentPane(notesPanel);
    }

    public UndergraduateLectureNotesUI() {
        this((Undergraduate) null, null);
    }

    public UndergraduateLectureNotesPanel getNotesPanel() {
        return notesPanel;
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            Undergraduate testStudent = new Undergraduate(
                    "21", "tg2085", "", "H.D.W.B. Hapuarachchi",
                    "tg2085@fot.ruh.ac.lk", "+94 77 123 4567", "",
                    "21", "TG/2024/2085", "2023/2024",
                    "Department of Information and Communication Technology", 2024
            );

            UndergraduateLectureNotesUI window = new UndergraduateLectureNotesUI(testStudent);
            window.setVisible(true);
        });
    }
}
