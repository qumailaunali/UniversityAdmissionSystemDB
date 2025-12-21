package Applicant.Tests;

import AdminSetup.EntryTest.EntryTestRecordManager;

import javax.swing.*;
import java.awt.*;

public class MathTest extends JFrame {
    private static int mathScore;
    private EntryTestRecordManager.EntryTestRecord record;
    private String[] questions = {
            "1. What is the value of π (pi) approximately?",
            "2. What is the derivative of x²?",
            "3. Simplify: 5 + 3 × 2?",
            "4. What is the square root of 144?",
            "5. If f(x) = 2x + 3, what is f(2)?",
            "6. What is the area of a circle with radius 3?",
            "7. Solve for x: 2x - 4 = 10",
            "8. Which is a prime number?",
            "9. What is the integral of 1/x?",
            "10. What is the result of 3⁴?"
    };

    private String[][] options = {
            {"3.14", "2.17", "1.62", "4.00"},
            {"x", "2x", "x^3", "1"},
            {"11", "16", "6", "14"},
            {"10", "11", "12", "13"},
            {"5", "6", "7", "8"},
            {"9π", "6π", "3π", "π"},
            {"6", "7", "8", "9"},
            {"4", "6", "9", "11"},
            {"ln|x| + C", "x + C", "1 + C", "e^x + C"},
            {"81", "64", "27", "12"}
    };

    private String[] answers = {
            "3.14", "2x", "11", "12", "7",
            "9π", "7", "11", "ln|x| + C", "81"
    };

    private JRadioButton[][] radios = new JRadioButton[10][4];
    private ButtonGroup[] groups = new ButtonGroup[10];

    public MathTest(EntryTestRecordManager.EntryTestRecord entryTestRecordManager) {
        setTitle("Math Entry Test");
        this.record=entryTestRecordManager;
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < 10; i++) {
            JPanel qPanel = new JPanel();
            qPanel.setLayout(new BoxLayout(qPanel, BoxLayout.Y_AXIS));
            qPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            groups[i] = new ButtonGroup();

            JPanel innerPanel = new JPanel();
            innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
            innerPanel.setBorder(BorderFactory.createTitledBorder(questions[i]));
            innerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            innerPanel.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));

            for (int j = 0; j < 4; j++) {
                radios[i][j] = new JRadioButton(options[i][j]);
                radios[i][j].setAlignmentX(Component.LEFT_ALIGNMENT);
                groups[i].add(radios[i][j]);
                innerPanel.add(radios[i][j]);
            }

            qPanel.add(innerPanel);
            contentPanel.add(qPanel);
            contentPanel.add(Box.createVerticalStrut(10));
        }

        JButton submitBtn = new JButton("Submit");
        submitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(submitBtn);

        submitBtn.addActionListener(e -> {
            int score = 0;
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 4; j++) {
                    if (radios[i][j].isSelected() && radios[i][j].getText().equals(answers[i])) {
                        score++;
                    }
                }
            }
            mathScore=score;
            record.setMathTaken(true);
            JOptionPane.showMessageDialog(this, "Score: " + score + "/10");
            dispose();
        });

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);

        add(scrollPane);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static int getMathScore(){
        return mathScore;
    }


}
