package main.java.ui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.imageio.ImageIO;
import java.io.InputStream;

public class WelcomeScreen extends JFrame {
    public WelcomeScreen(){
        setTitle("Tech-FAMS");
        setSize(600, 250);
        setResizable(false);
        setUndecorated(true); // Modern borderless splash screen look
        setLocationRelativeTo(null); // Center on screen
    }
}
