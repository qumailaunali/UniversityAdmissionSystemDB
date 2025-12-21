package Applicant;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class ScholarshipForm_Panel extends JPanel {
    // Applicant info loaded from file
    private String applicantId;
    private String name;
    private String email;
    private String gender;
    private String dob;
    private ScholarshipFormStatus scholarshipFormStatus;

    // Form fields
    private JTextField schoolField, percentageField, incomeField, otherAidField,
            clubsField, volunteerField, sportsField, leadershipField,
            signatureField;
    private JTextArea achievementsArea, explanationArea;
    private JCheckBox proofIncomeCheck, portfolioCheck;
    private JButton submitButton;
    private JLabel statusLabel;

    public ScholarshipForm_Panel(Applicant userInfo) {
        setLayout(new BorderLayout());
        // Load applicant info from file
        boolean found = loadApplicantInfoByEmail(userInfo.getEmail());
        if (!found) {
            add(new JLabel("You must submit at least one admission application before applying for a scholarship."), BorderLayout.CENTER);
            return;
        }

        // Build form panel
        JPanel formPanel = new JPanel(new GridLayout(0, 1, 10, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Show loaded applicant info
        formPanel.add(new JLabel("Applicant ID: " + userInfo.getUserID()));
        formPanel.add(new JLabel("Name: " + userInfo.getFirstName()+" " + userInfo.getLastName()));
        formPanel.add(new JLabel("Email: " + email));


        // Academic Info
        formPanel.add(new JLabel("Current School/College:"));
        schoolField = new JTextField();
        formPanel.add(schoolField);

        formPanel.add(new JLabel("Percentage (attach transcripts):"));
        percentageField = new JTextField();
        formPanel.add(percentageField);

        formPanel.add(new JLabel("Academic Achievements (awards, honors, publications):"));
        achievementsArea = new JTextArea(3, 20);
        formPanel.add(new JScrollPane(achievementsArea));

        // Financial Info
        formPanel.add(new JLabel("Annual Family Income:"));
        incomeField = new JTextField();
        formPanel.add(incomeField);

        formPanel.add(new JLabel("Other Financial Aid Received:"));
        otherAidField = new JTextField();
        formPanel.add(otherAidField);

        formPanel.add(new JLabel("Brief Explanation of Need (100-200 words):"));
        explanationArea = new JTextArea(4, 20);
        formPanel.add(new JScrollPane(explanationArea));

        // Extracurricular Info
        formPanel.add(new JLabel("Clubs/Societies (with roles):"));
        clubsField = new JTextField();
        formPanel.add(clubsField);

        formPanel.add(new JLabel("Volunteer Work / Community Service:"));
        volunteerField = new JTextField();
        formPanel.add(volunteerField);

        formPanel.add(new JLabel("Sports / Arts / Other Talents:"));
        sportsField = new JTextField();
        formPanel.add(sportsField);

        formPanel.add(new JLabel("Leadership Positions Held:"));
        leadershipField = new JTextField();
        formPanel.add(leadershipField);

        // Additional Requirements
        formPanel.add(new JLabel("Additional Requirements:"));
        proofIncomeCheck = new JCheckBox("Proof of Income attached (tax documents/pay stubs)");
        portfolioCheck = new JCheckBox("Portfolio attached (for arts/design scholarships)");
        formPanel.add(proofIncomeCheck);
        formPanel.add(portfolioCheck);

        // Declaration
        formPanel.add(new JLabel("Declaration:"));
        formPanel.add(new JLabel("\"I certify that all information provided is accurate. I understand that false statements may disqualify my application.\""));

        formPanel.add(new JLabel("Signature:"));
        signatureField = new JTextField();
        formPanel.add(signatureField);

        // Submit button
        submitButton = new JButton("Submit Scholarship Form");
        formPanel.add(submitButton);

        // Status message label
        statusLabel = new JLabel();
        formPanel.add(statusLabel);

        // Add scrollable form panel
        add(new JScrollPane(formPanel), BorderLayout.CENTER);

        // Submit button action
        submitButton.addActionListener(e -> validateAndSave());
    }

    private boolean loadApplicantInfoByEmail(String emailToFind) {
        try (BufferedReader reader = new BufferedReader(new FileReader("all_applications.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 14) {
                    String existingEmail = parts[13].trim();
                    if (existingEmail.equalsIgnoreCase(emailToFind.trim())) {
                        name = parts[1].trim();
                        // Your example data does not have gender and dob fields,
                        // so set these as "N/A" or read if you add those fields.

                        email = existingEmail;
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading applications: " + e.getMessage());
        }
        return false;
    }

    private void validateAndSave() {
        // Trim all relevant fields for easier reuse
        String school = schoolField.getText().trim();
        String percentageText = percentageField.getText().trim();
        String incomeText = incomeField.getText().trim();
        String achievements = achievementsArea.getText().trim();
        String explanation = explanationArea.getText().trim();
        String signature = signatureField.getText().trim();

        if (school.isEmpty() || percentageText.isEmpty() || incomeText.isEmpty() ||
                achievements.isEmpty() || explanation.isEmpty() || signature.isEmpty()) {
            statusLabel.setText("Please complete all required fields.");
            return;
        }

        if (explanation.split("\\s+").length < 100) {
            statusLabel.setText("Explanation must be at least 100 words.");
            return;
        }

        double percentage;
        try {
            percentage = Double.parseDouble(percentageText);
            if (percentage < 0 || percentage > 100.0) {
                statusLabel.setText("Percentage must be between 0.0 and 100.0");
                return;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Please enter a valid numeric GPA.");
            return;
        }

        // Validate Income: numeric and non-negative
        double income;
        try {
            income = Double.parseDouble(incomeText);
            if (income < 0) {
                statusLabel.setText("Income cannot be negative.");
                return;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Please enter a valid numeric Income.");
            return;
        }

        if (achievements.length() > 500) {
            statusLabel.setText("Achievements text is too long (max 500 characters).");
            return;
        }
        if (signature.length() < 3) {
            statusLabel.setText("Signature seems too short.");
            return;
        }
        if (signature.length() > 100) {
            statusLabel.setText("Signature is too long (max 100 characters).");
            return;
        }

        // Optional: Check for dangerous special characters in text fields (basic check)
        if (school.matches(".*[<>\"'%;)(&+].*") ||
                achievements.matches(".*[<>\"'%;)(&+].*") ||
                explanation.matches(".*[<>\"'%;)(&+].*") ||
                signature.matches(".*[<>\"'%;)(&+].*")) {
            statusLabel.setText("Input contains invalid special characters.");
            return;
        }

        if (income > 0 && !proofIncomeCheck.isSelected()) {
            statusLabel.setText("Proof of Income must be attached if annual income is declared.");
            return;
        }
        scholarshipFormStatus = ScholarshipFormStatus.SUBMITTED;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("allscholarships.txt", true))) {
            writer.write(String.join("|",
                    applicantId,
                    name,
                    email,
                    gender,
                    dob,
                    schoolField.getText().trim(),
                    percentageField.getText().trim(),
                    achievementsArea.getText().trim(),
                    incomeField.getText().trim(),
                    otherAidField.getText().trim(),
                    explanationArea.getText().trim(),
                    clubsField.getText().trim(),
                    volunteerField.getText().trim(),
                    sportsField.getText().trim(),
                    leadershipField.getText().trim(),
                    proofIncomeCheck.isSelected() ? "Yes" : "No",
                    portfolioCheck.isSelected() ? "Yes" : "No",
                    signatureField.getText().trim(),
                    scholarshipFormStatus.name(),
                    java.time.LocalDate.now().toString()
            ));
            writer.newLine();
            statusLabel.setText("Scholarship form submitted successfully!");
            submitButton.setEnabled(false);
        }
        catch (IOException ex) {
            statusLabel.setText("Error saving scholarship form.");
        }
    }
}
