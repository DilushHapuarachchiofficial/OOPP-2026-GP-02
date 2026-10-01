import javax.swing.*;
import java.awt.*;

public class SimpleBMIGUI extends JFrame {
    private JTextField weightField = new JTextField(15);
    private JTextField heightField = new JTextField(15);
    private JLabel resulbmi = new JLabel("BMI:", SwingConstants.CENTER);
    private JLabel resulcategory = new JLabel("Status:", SwingConstants.CENTER);

    private JRadioButton metricRadio = new JRadioButton("Metric (kg, m)", true);
    private JRadioButton englishRadio = new JRadioButton("English (lbs, in)");

    public SimpleBMIGUI() {
        setTitle("BMI Calculator");
        setSize(350, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(10, 1, 5, 5));

        JLabel titleLabel = new JLabel("BMI Calculator", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        add(titleLabel);

        add(new JLabel("   Select Measurement System:"));

        ButtonGroup group = new ButtonGroup();
        group.add(metricRadio); group.add(englishRadio);
        JPanel radioPanel = new JPanel();
        radioPanel.add(metricRadio); radioPanel.add(englishRadio);
        add(radioPanel);

        add(createInputPanel("Weight:", weightField));
        add(createInputPanel("Height:", heightField));

        JButton calcButton = new JButton("Calculate BMI");
        calcButton.setSize(10,10);
        calcButton.addActionListener(e -> calculateBMI());
        add(calcButton);
        add(new JLabel("Result", SwingConstants.CENTER));
        add(resulbmi);
        add(resulcategory);
    }

    private JPanel createInputPanel(String labelText, JTextField field) {
        JPanel panel = new JPanel();
        panel.add(new JLabel(labelText));
        panel.add(field);
        return panel;
    }

    private void calculateBMI() {
        try {
            double weight = Double.parseDouble(weightField.getText());
            double height = Double.parseDouble(heightField.getText());
            double bmi = 0;

            if (metricRadio.isSelected()) {
                bmi = weight / (height * height);
            } else {
                bmi = (weight * 703) / (height * height);
            }

            String category = "";
            if (bmi < 18.5) category = "Underweight";
            else if (bmi <= 24.9) category = "Normal";
            else if (bmi <= 29.9) category = "Overweight";
            else category = "Obese";

            resulbmi.setText(String.format("BMI: %.2f", bmi));
            resulcategory.setText(String.format("Status: (%s)", category));

        } catch (NumberFormatException ex) {
            resulbmi.setText("Error");
            resulcategory.setText("Please enter valid numbers!");
        }
    }

    public static void main(String[] args) {
        new SimpleBMIGUI().setVisible(true);
    }
}
