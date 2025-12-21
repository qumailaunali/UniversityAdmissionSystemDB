package Applicant;

// import AdminSetup.College.College;
import AdminSetup.College.CollegeManager;
import AdminSetup.Program.Program;
import AdminSetup.Program.ProgramManager;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import Database.DBConnection;

// Application form class to submit forms - this class will also generate id of forms globally for every applicant
public class ApplicationForm_Panel extends JPanel {
    private ProgramManager programManager;
    private CollegeManager collegeManager;

    private static final Color COLORAZ_BLACK = Color.BLACK;
    private static final Color COLORAZ_SAGE = new Color(180, 195, 180);
    private static final Color COLORAZ_WHITE = Color.WHITE;

    private String applicationId;

    private JComboBox<String> programDropdown, collegeDropdown, stream12Dropdown;
    private JTextField year12Field, percent12Field;
    private JButton submitButton;

    private Applicant userInfo;
    // private Status status;
    // private ArrayList<College> colleges;

    public ApplicationForm_Panel(Applicant userInfo, ProgramManager programManager, CollegeManager collegeManager) throws IOException {
        this.userInfo = userInfo;
        this.collegeManager = collegeManager;
        this.programManager = programManager;
        // this.colleges = new ArrayList<>();
//        collegeManager.loadFromFile("colleges.txt");

        setLayout(new BorderLayout());
        setBackground(COLORAZ_WHITE);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(COLORAZ_WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        JLabel title = new JLabel("Admission Application Form", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(COLORAZ_BLACK);
        add(title, BorderLayout.NORTH);

        // Applicant Info Display
        row = addLabelAndValue(formPanel, "Applicant ID:", userInfo.getUserID(), row, gbc);
        row = addLabelAndValue(formPanel, "Email:", userInfo.getEmail(), row, gbc);
        row = addLabelAndValue(formPanel, "First Name:", userInfo.getFirstName(), row, gbc);
        row = addLabelAndValue(formPanel, "Last Name:", userInfo.getLastName(), row, gbc);
        row = addLabelAndValue(formPanel, "Phone Number:", userInfo.getPhone(), row, gbc);
        row = addLabelAndValue(formPanel, "Date of Birth:", String.valueOf(userInfo.getDateOfBirth()), row, gbc);
        row = addLabelAndValue(formPanel, "Gender:", String.valueOf(userInfo.getGender()), row, gbc);

        year12Field = addFieldWithPlaceholder(formPanel, "12th Year:", "Enter year of 12th", row++, gbc);
        percent12Field = addFieldWithPlaceholder(formPanel, "12th Percentage:", "Enter your 12th %", row++, gbc);

        stream12Dropdown = new JComboBox<>(new String[] {
                "Select your 12th Stream", "Pre-Medical", "Pre-Engineering",
                "Computer Science", "Commerce", "Humanities/Arts", "General Science"
        });
        stream12Dropdown.addActionListener(e -> {
            String selectedStream = (String) stream12Dropdown.getSelectedItem();
            if (selectedStream != null && !selectedStream.equals("Select your 12th Stream")) {
                loadProgramsByStream(selectedStream);
            } else {
                programDropdown.removeAllItems();
                programDropdown.addItem("Select a stream first");

                collegeDropdown.removeAllItems();
                collegeDropdown.addItem("Select a program first");
            }
            validateDropdowns();
        });
        addLabelAndComponent(formPanel, "12th Stream:", stream12Dropdown, row++, gbc);

        programDropdown = new JComboBox<>();
        addLabelAndComponent(formPanel, "Program:", programDropdown, row++, gbc);

        collegeDropdown = new JComboBox<>();
        addLabelAndComponent(formPanel, "College:", collegeDropdown, row++, gbc);

        programDropdown.addActionListener(e -> {
            String selectedProgram = (String) programDropdown.getSelectedItem();
            if (selectedProgram != null && !selectedProgram.equals("No programs available") &&
                    !selectedProgram.equals("Select a stream first") && !selectedProgram.trim().isEmpty()) {
                updateCollegeDropdown(selectedProgram);
            } else {
                collegeDropdown.removeAllItems();
                collegeDropdown.addItem("Select a program first");

            }

            validateDropdowns();
        });

        submitButton = new JButton("Submit");
        submitButton.setBackground(COLORAZ_SAGE);
        submitButton.addActionListener(e -> validateForm());

        gbc.gridx = 1;
        gbc.gridy = row;
        formPanel.add(submitButton, gbc);

        add(new JScrollPane(formPanel), BorderLayout.CENTER);
    }

    private int addLabelAndValue(JPanel panel, String label, String value, int row, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        panel.add(new JLabel(value), gbc);

        return row + 1;
    }

    private JTextField addFieldWithPlaceholder(JPanel panel, String label, String placeholder, int row, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        JTextField field = new JTextField();
        field.setToolTipText(placeholder);
        panel.add(field, gbc);

        return field;
    }

    private void addLabelAndComponent(JPanel panel, String label, JComponent component, int row, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        panel.add(component, gbc);
    }
    private void loadProgramsByStream(String stream) {
        programDropdown.removeAllItems();
        ArrayList<Program> filteredPrograms = programManager.getProgramsByStream(stream);

        if (filteredPrograms.isEmpty()) {
            programDropdown.addItem("No programs available");
        } else {
            for (Program p : filteredPrograms) {
                programDropdown.addItem(p.getName());
            }

            programDropdown.setSelectedIndex(0);
        }
    }


    private void updateCollegeDropdown(String programName) {
        collegeDropdown.removeAllItems();

        String sql = """
            SELECT DISTINCT c.college_name
            FROM dbo.College c
            JOIN dbo.Program p ON p.College_ID = c.college_id
            WHERE p.ProgramName = ?
            ORDER BY c.college_name
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, programName);
            try (ResultSet rs = ps.executeQuery()) {
                boolean any = false;
                while (rs.next()) {
                    collegeDropdown.addItem(rs.getString("college_name"));
                    any = true;
                }
                if (!any) {
                    collegeDropdown.addItem("No colleges available");
                } else {
                    collegeDropdown.setSelectedIndex(0);
                }
            }
        } catch (SQLException ex) {
            collegeDropdown.addItem("Error loading colleges");
            ex.printStackTrace();
        }

        validateDropdowns();
        collegeDropdown.addActionListener(e -> validateDropdowns());
    }



    private void validateForm() {
        if (!validateAllFields()) {
            return;
        }

        try {
            String selectedProgramName = (String) programDropdown.getSelectedItem();
            String selectedCollegeName = (String) collegeDropdown.getSelectedItem();

            int programId = findProgramId(selectedProgramName, selectedCollegeName);
            if (programId == -1) {
            JOptionPane.showMessageDialog(this, "Unable to locate program in database.");
            return;
            }

                int generatedId = saveApplicationToDatabase(
                    userInfo,
                    year12Field.getText().trim(),
                    percent12Field.getText().trim(),
                    stream12Dropdown.getSelectedItem().toString(),
                    programId,
                    selectedCollegeName
                );
            applicationId = "APP-" + generatedId;

            JOptionPane.showMessageDialog(this,
                "Application Submitted Successfully!\nApplication ID: " + applicationId,
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );

            int response = JOptionPane.showConfirmDialog(this,
                    "Do you want to submit another application?",
                    "New Application",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (response == JOptionPane.YES_OPTION) {
                clearForm();
            }

            else {
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

                if (topFrame != null) {
                    ApplicantDashboard_Panel dashboard = new ApplicantDashboard_Panel(userInfo, programManager, collegeManager);
                    dashboard.setVisible(true);
                    topFrame.dispose();
                }
                else {
                    JOptionPane.showMessageDialog(this, "Unable to locate main window. Exiting application.");
                    System.exit(0);
                }
            }

        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Unexpected error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        year12Field.setText("");
        percent12Field.setText("");
        stream12Dropdown.setSelectedIndex(0);
        programDropdown.setSelectedIndex(0);
        collegeDropdown.setSelectedIndex(0);
    }


    private boolean validateAllFields() {
        if (!validateYearField(year12Field, "12th Year") ||
                !validatePercentageField(percent12Field, "12th Percentage")) {
            return false;
        }

        if (!validateDropdowns()) {
            JOptionPane.showMessageDialog(this,
                    "Please make valid selections in all dropdown fields",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }


        return true;
    }

    private int findProgramId(String programName, String collegeName) {
        String sql = """
            SELECT p.ProgramID
            FROM dbo.Program p
            JOIN dbo.College c ON c.college_id = p.College_ID
            WHERE p.ProgramName = ? AND c.college_name = ?
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, programName);
            ps.setString(2, collegeName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ProgramID");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int saveApplicationToDatabase(Applicant applicant, String year12, String percent12, String stream12,
                                          int programId, String universityName) {
        String sql = """
            INSERT INTO dbo.ApplicationForm (
                ApplicantID, AdminID, email, first_name, last_name, date_of_birth, gender,
                twelfth_percentage, twelfth_year, twelfth_stream,
                university_name, test_schedule, test_score, status, fee_status,
                is_submitted, is_scholarship_submitted, programid
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            int applicantId = applicant != null ? applicant.getApplicantID() : 0;
            if ((applicantId <= 0 || applicantId == Integer.MAX_VALUE) && applicant != null && applicant.getEmail() != null) {
                applicantId = findApplicantIdByEmail(applicant.getEmail());
                if (applicantId > 0) {
                    applicant.setApplicantID(applicantId);
                }
            }
            if (applicantId <= 0) {
                JOptionPane.showMessageDialog(this, "Applicant not found in database. Please log in again.");
                return -1;
            }

            // Applicant and personal info
            ps.setInt(1, applicantId); // NOT NULL FK
            ps.setNull(2, java.sql.Types.INTEGER); // AdminID optional
            ps.setString(3, applicant != null ? applicant.getEmail() : null);
            ps.setString(4, applicant != null ? applicant.getFirstName() : null);
            ps.setString(5, applicant != null ? applicant.getLastName() : null);
            ps.setDate(6, applicant != null && applicant.getDateOfBirth() != null ? java.sql.Date.valueOf(applicant.getDateOfBirth()) : null);
            ps.setString(7, applicant != null && applicant.getGender() != null ? applicant.getGender().name() : null);

            // Academics
            ps.setBigDecimal(8, percent12 != null && !percent12.isEmpty() ? new BigDecimal(percent12) : null);
            ps.setObject(9, year12 != null && !year12.isEmpty() ? Integer.parseInt(year12) : null, java.sql.Types.INTEGER);
            ps.setString(10, stream12);

            // Program/university
            ps.setString(11, universityName);
            ps.setNull(12, java.sql.Types.DATE); // test_schedule
            ps.setNull(13, java.sql.Types.INTEGER); // test_score
            ps.setString(14, Status.SUBMITTED.name());
            ps.setString(15, FeeStatus.UNPAID.name());
            ps.setBoolean(16, true); // is_submitted
            ps.setBoolean(17, false); // is_scholarship_submitted
            ps.setInt(18, programId);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saving application to database: " + e.getMessage());
        }
        return -1;
    }

    private int findApplicantIdByEmail(String email) {
        String sql = "SELECT ApplicantID FROM dbo.Applicant WHERE Email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ApplicantID");
                }
            }
        } catch (SQLException e) {
            // log to console; UI message handled by caller
            e.printStackTrace();
        }
        return -1;
    }

    // private boolean validateEducationField(JTextField field, String fieldName) {
    //     String value = field.getText().trim();
    //     if (value.isEmpty()) {
    //         showError(fieldName + " cannot be empty");
    //         field.requestFocus();
    //         return false;
    //     }
    //     return true;
    // }

    private boolean validateYearField(JTextField field, String fieldName) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            showError(fieldName + " cannot be empty");
            field.requestFocus();
            return false;
        }

        try {
            int year = Integer.parseInt(value);
            int currentYear = java.time.Year.now().getValue();
            if (year < 1900 || year > currentYear) {
                showError(fieldName + " must be between 1900 and " + currentYear);
                field.requestFocus();
                return false;
            }
        }

        catch (NumberFormatException e) {
            showError(fieldName + " must be a valid year");
            field.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validatePercentageField(JTextField field, String fieldName) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            showError(fieldName + " cannot be empty");
            field.requestFocus();
            return false;
        }

        try {
            double percentage = Double.parseDouble(value);
            if (percentage < 0 || percentage > 100) {
                showError(fieldName + " must be between 0 and 100");
                field.requestFocus();
                return false;
            }
        }
        catch (NumberFormatException e) {
            showError(fieldName + " must be a valid number");
            field.requestFocus();
            return false;
        }

        return true;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private boolean validateDropdowns() {
        String selectedStream = (String) stream12Dropdown.getSelectedItem();
        String selectedProgram = (String) programDropdown.getSelectedItem();
        String selectedCollege = (String) collegeDropdown.getSelectedItem();

        boolean isStreamValid = selectedStream != null
                && !selectedStream.equals("Select your 12th Stream")
                && !selectedStream.trim().isEmpty();

        boolean isProgramValid = selectedProgram != null
                && !selectedProgram.equals("No programs available")
                && !selectedProgram.equals("Select a stream first")
                && !selectedProgram.trim().isEmpty();

        boolean isCollegeValid = selectedCollege != null
                && !selectedCollege.equals("No colleges available")
                && !selectedCollege.equals("Select a program first")
                && !selectedCollege.trim().isEmpty();

        boolean isValid = isStreamValid && isProgramValid && isCollegeValid;
        submitButton.setEnabled(isValid);

        return isValid;
    }
}