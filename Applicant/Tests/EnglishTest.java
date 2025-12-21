package Applicant.Tests;
import AdminSetup.EntryTest.EntryTestRecordManager;

import javax.swing.*;
import java.awt.*;

public class EnglishTest extends JFrame {
    public static int engScore;
    private EntryTestRecordManager.EntryTestRecord record;


    private String[] questions = {
            "1. Choose the correct synonym of 'Happy':",
            "2. Which sentence is grammatically correct?",
            "3. What is the past tense of 'Go'?",
            "4. Choose the correctly spelled word:",
            "5. What is an antonym of 'Generous'?",
            "6. Identify the noun in the sentence: 'The cat sat on the mat.'",
            "7. Choose the correct article: '___ apple a day keeps the doctor away.'",
            "8. Select the correct form: 'She has ___ the letter.'",
            "9. What is a conjunction in: 'He studied hard but failed.'?",
            "10. What part of speech is 'quickly' in: 'She ran quickly'?"
    };

    private String[][] options = {
            {"Joyful", "Angry", "Sad", "Lazy"},
            {"He don't like pizza.", "She go to school daily.", "They is playing.", "He doesn't like pizza."},
            {"Goed", "Went", "Go", "Going"},
            {"Recieve", "Receive", "Recive", "Receeve"},
            {"Kind", "Helpful", "Stingy", "Friendly"},
            {"sat", "cat", "on", "the"},
            {"An", "A", "The", "No article needed"},
            {"wrote", "write", "written", "writing"},
            {"He", "studied", "but", "failed"},
            {"Adjective", "Verb", "Noun", "Adverb"}
    };

    private String[] answers = {
            "Joyful", "He doesn't like pizza.", "Went", "Receive", "Stingy",
            "cat", "An", "written", "but", "Adverb"
    };

    private JRadioButton[][] radios = new JRadioButton[10][4];
    private ButtonGroup[] groups = new ButtonGroup[10];

    public EnglishTest(EntryTestRecordManager.EntryTestRecord entryTestRecordManager) {
        setTitle("English Entry Test");
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

            engScore=score;
            record.setEnglishTaken(true);

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

    public static int getEngScore(){
        return engScore;
    }


}
