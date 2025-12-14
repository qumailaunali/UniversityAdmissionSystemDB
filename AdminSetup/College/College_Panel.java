package AdminSetup.College;

import javax.swing.*;
import java.awt.*;

public class College_Panel extends JPanel {
    private static final Color COLORAZ_BLACK = Color.BLACK;
    private static final Color COLORAZ_WHITE = Color.WHITE;

    private final CollegeManager collegeManager = new CollegeManager();
    private final DefaultListModel<String> collegeListModel = new DefaultListModel<>();
    private final JList<String> collegeList = new JList<>(collegeListModel);
    private final JTextField collegeNameField = new JTextField(20);


    public College_Panel() {
        setLayout(new BorderLayout());
        setBackground(COLORAZ_WHITE);
         addElements();
         refreshCollegeList();
    }


    private void addElements (){
        JLabel titleLabel = new JLabel("College Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(COLORAZ_BLACK);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(COLORAZ_WHITE);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(40, 300, 40, 300));

        JLabel nameLabel = new JLabel("Enter College Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        collegeNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        collegeNameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton addButton = new JButton("Add College");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addButton.setBackground(Color.BLACK);
        addButton.setForeground(Color.WHITE);
        addButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addButton.addActionListener(e -> handleAddCollege());

        centerPanel.add(nameLabel);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(collegeNameField);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(addButton);

        centerPanel.add(Box.createVerticalStrut(30));

        JLabel listLabel = new JLabel("Existing Colleges:");
        listLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        listLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(listLabel);
        centerPanel.add(Box.createVerticalStrut(5));

        collegeList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        collegeList.setVisibleRowCount(8);
        JScrollPane scrollPane = new JScrollPane(collegeList);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(scrollPane);

        add(centerPanel, BorderLayout.CENTER);
    }


private void handleAddCollege() {
    String collegeName = collegeNameField.getText().trim();

    if (collegeName.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter a college name.", "Input Required", JOptionPane.WARNING_MESSAGE);
        return;
    }

    for (College c : collegeManager.getAllColleges()) {
        if (c.getName().equalsIgnoreCase(collegeName)) {
            JOptionPane.showMessageDialog(this, "This college already exists", "Duplicate Entry", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }
    collegeManager.addCollege(collegeName);
    refreshCollegeList();
    collegeNameField.setText("");
    JOptionPane.showMessageDialog(this, "College added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
}


    private void refreshCollegeList() {
        collegeListModel.clear();
        for (College c : collegeManager.getAllColleges()) {
            collegeListModel.addElement(c.getName());
        }
    }
}
