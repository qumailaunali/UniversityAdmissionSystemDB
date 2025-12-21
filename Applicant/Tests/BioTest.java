package Applicant.Tests;

import AdminSetup.EntryTest.EntryTestRecordManager;
import javax.swing.*;
import java.awt.*;

public class BioTest extends JFrame {
    private static int bioMarks;
    private String[] questions = {
            "1. Powerhouse of cell?",
            "2. Vitamin made in skin?",
            "3. Unit of kidney?",
            "4. Clotting cells?",
            "5. Insulin produced by?",
            "6. Genetic disorder?",
            "7. Gas in red blood cells?",
            "8. Hormone for metabolism?",
            "9. Food making process?",
            "10. Brain balance part?"
    };

    private String[][] options = {
            {"Nucleus", "Mitochondria", "Ribosome", "Golgi"},
            {"A", "B", "C", "D"},
            {"Neuron", "Nephron", "Alveoli", "Axon"},
            {"Red", "White", "Platelets", "Plasma"},
            {"Liver", "Pancreas", "Stomach", "Kidney"},
            {"Flu", "Down syndrome", "Malaria", "Pneumonia"},
            {"Oxygen", "CO2", "Nitrogen", "Hydrogen"},
            {"Insulin", "Adrenaline", "Thyroxine", "Estrogen"},
            {"Transpiration", "Photosynthesis", "Respiration", "Germination"},
            {"Cerebrum", "Cerebellum", "Medulla", "Pons"}
    };

    private String[] answers = {
            "Mitochondria", "D", "Nephron", "Platelets", "Pancreas",
            "Down syndrome", "Oxygen", "Thyroxine", "Photosynthesis", "Cerebellum"
    };

    private JRadioButton[][] radios = new JRadioButton[10][4];
    private ButtonGroup[] groups = new ButtonGroup[10];

    public BioTest(EntryTestRecordManager.EntryTestRecord record) {
        setTitle("Bio Entry Test");
        setSize(800, 700); // initial size, can keep or remove


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
            innerPanel.setMaximumSize(new Dimension(800, Integer.MAX_VALUE)); // allow width

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
            bioMarks=score;
            JOptionPane.showMessageDialog(this, "Score: " + score + "/10");
            dispose();
        });

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);

        add(scrollPane);

        // Here is the minimal addition to open fullscreen:
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }
    public static int getBioMarks(){
        return bioMarks;
    }

    // Test main removed - use from SubmittedFormList_Panel
}