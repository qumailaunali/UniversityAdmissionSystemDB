package Applicant.Tests;

import AdminSetup.EntryTest.EntryTestRecordManager;

import javax.swing.*;
import java.awt.*;

public class AdvancedMathTest extends JFrame {
    private static int advMathScore;

    private final String[] questions = {
            "1. What is the derivative of sin(x)?",
            "2. What is the integral of cos(x)?",
            "3. Solve: lim(x→0) (sin x)/x",
            "4. What is the determinant of a 2x2 matrix [[1,2],[3,4]]?",
            "5. What is e^0?",
            "6. What is the value of log₁₀(100)?",
            "7. What is the factorial of 5?",
            "8. If A = {1,2,3}, how many subsets does A have?",
            "9. What is the binomial coefficient C(5,2)?",
            "10. What is the value of tan(45°)?"
    };

    private final String[][] options = {
            {"cos(x)", "-cos(x)", "sin(x)", "-sin(x)"},
            {"sin(x)", "-sin(x)", "cos(x)", "x"},
            {"0", "1", "Undefined", "∞"},
            {"-2", "-1", "1", "2"},
            {"0", "1", "e", "-1"},
            {"1", "2", "10", "2"},
            {"120", "60", "20", "100"},
            {"6", "8", "4", "2"},
            {"10", "5", "20", "15"},
            {"1", "0", "√3", "tan(x)"}
    };

    private final String[] answers = {
            "cos(x)", "sin(x)", "1", "-2", "1",
            "2", "120", "8", "10", "1"
    };

    private final JRadioButton[][] radios = new JRadioButton[10][4];
    private final ButtonGroup[] groups = new ButtonGroup[10];

    public AdvancedMathTest(EntryTestRecordManager.EntryTestRecord record) {
        setTitle("Advanced Math Entry Test");

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        for (int i = 0; i < 10; i++) {
            JPanel qPanel = new JPanel();
            qPanel.setLayout(new BoxLayout(qPanel, BoxLayout.Y_AXIS));
            qPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            qPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Wrapped question text
            JTextArea questionText = new JTextArea(questions[i]);
            questionText.setLineWrap(true);
            questionText.setWrapStyleWord(true);
            questionText.setEditable(false);
            questionText.setOpaque(false);
            questionText.setFont(new Font("SansSerif", Font.BOLD, 14));
            questionText.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));
            qPanel.add(questionText);
            qPanel.add(Box.createVerticalStrut(5));

            groups[i] = new ButtonGroup();

            for (int j = 0; j < 4; j++) {
                radios[i][j] = new JRadioButton(options[i][j]);
                radios[i][j].setAlignmentX(Component.LEFT_ALIGNMENT);
                groups[i].add(radios[i][j]);
                qPanel.add(radios[i][j]);
            }

            contentPanel.add(qPanel);
            contentPanel.add(Box.createVerticalStrut(15));
        }

        JButton submitBtn = new JButton("Submit");
        submitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        submitBtn.addActionListener(e -> {
            int score = 0;
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 4; j++) {
                    if (radios[i][j].isSelected() && radios[i][j].getText().equals(answers[i])) {
                        score++;
                    }
                }
            }
            advMathScore = score;
            JOptionPane.showMessageDialog(this, "Score: " + score + "/10");
            dispose();
            JOptionPane.showMessageDialog(this, "Score: " + score + "/10");
        });

        contentPanel.add(submitBtn);
        contentPanel.add(Box.createVerticalStrut(20));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);

        add(scrollPane);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static int getAdvMathScore() {
        return advMathScore;
    }
}
//sss
