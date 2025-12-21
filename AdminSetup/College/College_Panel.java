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
        // Setup: Create panel for managing colleges (add new, view existing)
        setLayout(new BorderLayout());
        setBackground(COLORAZ_WHITE);
        addElements();
        // Load all colleges from database
        refreshCollegeList();
    }


    private void addElements (){
        // Build UI: title, input field for college name, add button, and list of existing colleges
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
        // Input field for new college name
        collegeNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        collegeNameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton addButton = new JButton("Add College");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addButton.setBackground(Color.BLACK);
        addButton.setForeground(Color.WHITE);
        addButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addButton.addActionListener(e -> handleAddCollege()); // Trigger add college validation

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
        // Display list of colleges loaded from database
        collegeList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        collegeList.setVisibleRowCount(8);
        JScrollPane scrollPane = new JScrollPane(collegeList);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(scrollPane);

        add(centerPanel, BorderLayout.CENTER);
    }


private void handleAddCollege() {
    // Validate and add new college: check if empty, verify no duplicates, insert to database
    String collegeName = collegeNameField.getText().trim();
    // Validate required field
    if (collegeName.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter a college name.", "Input Required", JOptionPane.WARNING_MESSAGE);
        return;
    }
    // Check if college name already exists to prevent duplicates
    for (College c : collegeManager.getAllColleges()) {
        if (c.getName().equalsIgnoreCase(collegeName)) {
            JOptionPane.showMessageDialog(this, "This college already exists", "Duplicate Entry", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }
    // SQL: INSERT INTO dbo.College (CollegeName) VALUES (?)
    collegeManager.addCollege(collegeName);
    // Refresh the displayed list from database
    refreshCollegeList();
    // Clear input field for next entry
    collegeNameField.setText("");
    JOptionPane.showMessageDialog(this, "College added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
}


    private void refreshCollegeList() {
        // Load all colleges from database and update the list display
        // SQL: SELECT * FROM dbo.College
        collegeListModel.clear();
        for (College c : collegeManager.getAllColleges()) {
            collegeListModel.addElement(c.getName());
        }
    }
}
