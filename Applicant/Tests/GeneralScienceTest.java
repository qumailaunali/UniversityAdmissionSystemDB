package Applicant.Tests;

import AdminSetup.EntryTest.EntryTestRecordManager;

import javax.swing.*;
// import java.awt.*;

public class GeneralScienceTest extends JFrame {
    private static int scienceScore;
    // private EntryTestRecordManager.EntryTestRecord record;

    private final String[] questions = {
            "1. What is the boiling point of water?",
            "2. Which gas is most abundant in Earth's atmosphere?",
            "3. What organ pumps blood?",
            "4. Which planet is known as the Red Planet?",
            "5. Which part of the plant conducts photosynthesis?",
            "6. Which is a non-renewable source of energy?",
            "7. What is the chemical formula for water?",
            "8. What vitamin is produced in human skin in sunlight?",
            "9. What is Newton's Third Law?",
            "10. Which metal is liquid at room temperature?"
    };

    private final String[][] options = {
            {"90°C", "100°C", "110°C", "120°C"},
            {"Oxygen", "Nitrogen", "Carbon Dioxide", "Hydrogen"},
            {"Lungs", "Brain", "Heart", "Kidney"},
            {"Venus", "Mars", "Jupiter", "Saturn"},
            {"Stem", "Leaf", "Root", "Flower"},
            {"Wind", "Solar", "Coal", "Hydro"},
            {"H2O", "CO2", "NaCl", "O2"},
            {"Vitamin A", "Vitamin C", "Vitamin D", "Vitamin B12"},
            {"Every action has...", "Energy = mc²", "Force = Mass × Acceleration", "Opposite forces attract"},
            {"Mercury", "Iron", "Copper", "Lead"}
    };

    private final String[] answers = {
            "100°C", "Nitrogen", "Heart", "Mars", "Leaf",
            "Coal", "H2O", "Vitamin D", "Every action has...", "Mercury"
    };

    private final JRadioButton[][] radios = new JRadioButton[10][4];
    private final ButtonGroup[] groups = new ButtonGroup[10];

    public GeneralScienceTest(EntryTestRecordManager.EntryTestRecord record) {
        setTitle("General Science Test");
        // this.record = record;

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < 10; i++) {
            JPanel qPanel = new JPanel();
            qPanel.setLayout(new BoxLayout(qPanel, BoxLayout.Y_AXIS));
            groups[i] = new ButtonGroup();

            JPanel innerPanel = new JPanel();
            innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
            innerPanel.setBorder(BorderFactory.createTitledBorder(questions[i]));

            for (int j = 0; j < 4; j++) {
                radios[i][j] = new JRadioButton(options[i][j]);
                groups[i].add(radios[i][j]);
                innerPanel.add(radios[i][j]);
            }

            qPanel.add(innerPanel);
            contentPanel.add(qPanel);
            contentPanel.add(Box.createVerticalStrut(10));
        }

        JButton submitBtn = new JButton("Submit");
        submitBtn.addActionListener(e -> {
            int score = 0;
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 4; j++) {
                    if (radios[i][j].isSelected() && radios[i][j].getText().equals(answers[i])) {
                        score++;
                    }
                }
            }
            scienceScore = score;
            record.setBiologyTaken(true); // Since biology is being used for science
            JOptionPane.showMessageDialog(this, "Score: " + score + "/10");
        });

        contentPanel.add(submitBtn);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static int getScienceScore() {
        return scienceScore;
    }
}
