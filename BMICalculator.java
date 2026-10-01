import javax.swing.*;
import java.awt.*;


public class BMICalculator extends JFrame {
    private JRadioButton metric = new JRadioButton("Metric (kg, m)", true);
    private JRadioButton english = new JRadioButton("English (lbs, in)");
    private JTextField weight = new JTextField();
    private JTextField height = new JTextField();
    private JLabel bmiResult = new JLabel(), statusResult = new JLabel();

    public BMICalculator() {
        setTitle("BMI CALCULATOR");
        setSize(400, 380);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        // UI Elements
        add(label("BMI CALCULATOR", 20, 10, 360, 30, true));
        add(label("Select Measurement System :", 30, 50, 250, 20, false));

        metric.setBounds(30, 75, 140, 20);
        english.setBounds(180, 75, 140, 20);
        ButtonGroup group = new ButtonGroup();
        group.add(metric); group.add(english);
        add(metric); add(english);

        add(label("Weight :", 30, 110, 80, 20, false));
        weight.setBounds(110, 110, 150, 25); add(weight);

        add(label("Height :", 30, 145, 80, 20, false));
        height.setBounds(110, 145, 150, 25); add(height);

        JButton calcBtn = new JButton("Calculate");
        calcBtn.setBounds(110, 185, 100, 30);
        calcBtn.addActionListener(e -> calculate());
        add(calcBtn);

        JButton clearBtn = new JButton("Clear");
        clearBtn.setBounds(220, 185, 80, 30);
        clearBtn.addActionListener(e -> clear());
        add(clearBtn);

        JSeparator sep = new JSeparator();
        sep.setBounds(20, 230, 350, 10);
        add(sep);

        add(label("Result", 30, 240, 100, 20, true));
        add(label("BMI :", 30, 270, 60, 20, false));
        bmiResult.setBounds(90, 270, 200, 20); add(bmiResult);

        add(label("Status :", 30, 295, 60, 20, false));
        statusResult.setBounds(90, 295, 200, 20); add(statusResult);
    }

    private JLabel label(String text, int x, int y, int w, int h, boolean bold) {
        JLabel l = new JLabel(text);
        l.setBounds(x, y, w, h);
        if (bold) l.setFont(new Font("Arial", Font.BOLD, 14));
        return l;
    }

    private void calculate() {
        try {
            double w = Double.parseDouble(weight.getText());
            double h = Double.parseDouble(height.getText());
            double bmi = metric.isSelected() ? (w / (h * h)) : (703 * w / (h * h));

            bmiResult.setText(String.format("%.2f", bmi));
            statusResult.setText(bmi < 18.5 ? "Underweight" : bmi < 25 ? "Normal weight" : bmi < 30 ? "Overweight" : "Obesity");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Yogya sankhya takaa!");
        }
    }

    private void clear() {
        weight.setText("");
        height.setText("");
        bmiResult.setText("");
        statusResult.setText("");
        metric.setSelected(true);
    }

    public static void main(String[] args) {
        new BMICalculator().setVisible(true);
    }
}

