package AdminSetup.EntryTest;

import AdminSetup.PaymentManager;
import Applicant.ApplicationFormData;
import Applicant.Status;
import Applicant.ApplicantManager;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Panel for admin to schedule entry tests for applicants and send admission offers/rejections
// Displays applicants with fee paid status, allows setting test date/time/subjects
public class SetTestDatePanel extends JPanel {
    private JTable table;                           // Table displaying test scheduling interface
    private DefaultTableModel model;                // Table model for dynamic row management
    private EntryTestRecordManager recordManager;   // Manager for test record database operations

    // Constructor: Initialize panel with test scheduling UI and load existing test records
    public SetTestDatePanel(EntryTestRecordManager recordManager) {
        this.recordManager = recordManager;  // Store reference to entry test record manager
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header with title and refresh button for reloading test data
        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Set Entry Test Date, Time & Subjects");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setHorizontalAlignment(SwingConstants.LEFT);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadTestData());
        header.add(title, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Define table columns: ID, Name, Program, Stream, Date/Time, Attempt Status, Score, Subjects, Action Buttons, Decision Buttons
        String[] columns = {
                "Applicant ID", "Applicant Name", "Program", "12th Stream", "Test Date & Time",
                "Attempted", "Score", "Subjects", "Action", "Decision"
        };

        // Create table model with custom editable column logic (only Action and Decision columns editable)
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8 || column == 9;  // Only columns 8 (Action) and 9 (Decision) are editable
            }
        };

        table = new JTable(model);
        table.setRowHeight(40);  // Set row height to accommodate button rendering
        loadTestData();  // Load test records from database into table

        // Set custom cell renderers and editors for button columns
        table.getColumn("Action").setCellRenderer(new ActionCellRenderer());
        table.getColumn("Action").setCellEditor(new ActionCellEditor(new JCheckBox(), model, table));
        table.getColumn("Decision").setCellRenderer(new DecisionCellRenderer());
        table.getColumn("Decision").setCellEditor(new DecisionCellEditor(new JCheckBox(), model, table));

        // Configure column widths for optimal display
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(100); // Applicant ID
        columnModel.getColumn(1).setPreferredWidth(150); // Applicant Name
        columnModel.getColumn(2).setPreferredWidth(100); // Program
        columnModel.getColumn(3).setPreferredWidth(100); // 12th Stream
        columnModel.getColumn(4).setPreferredWidth(150); // Test Date & Time
        columnModel.getColumn(5).setPreferredWidth(80);  // Attempt Status
        columnModel.getColumn(6).setPreferredWidth(60);  // Test Score
        columnModel.getColumn(7).setPreferredWidth(150); // Subjects
        columnModel.getColumn(8).setPreferredWidth(180); // Action buttons (Set Date/Subjects)
        columnModel.getColumn(9).setPreferredWidth(180); // Decision buttons (Offer/Reject)

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    // Load test data from database and populate table with applicant test records
    private void loadTestData() {
        model.setRowCount(0);  // Clear existing rows from table
        List<EntryTestRecordManager.EntryTestRecord> existingRecords = recordManager.loadAllRecords();  // Fetch all test records from database
        List<String> applicantIds = ApplicantManager.getAllApplicantIds();  // Get list of all applicants
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");  // Format for displaying date/time

        // Iterate through all applicants to build table rows
        for (String id : applicantIds) {
            // Skip applicants who haven't paid the fee (check PaymentManager database)
            if (!PaymentManager.isFeePaid(id)) {
                continue;
            }

            // Search for existing test record for this applicant
            EntryTestRecordManager.EntryTestRecord record = null;
            for (EntryTestRecordManager.EntryTestRecord r : existingRecords) {
                if (r.getApplicantId().equals(id)) {
                    record = r;
                    break;
                }
            }

            // If no test record exists, create new empty record for this applicant
            if (record == null) {
                record = new EntryTestRecordManager.EntryTestRecord(id, null, false, 0);
            }

            // Fetch applicant's application data for program/stream/name information
            ApplicationFormData appData = ApplicantManager.getApplicationByAppId(id);
            String program = appData != null ? appData.getSelectedProgram() : "N/A";
            String stream = appData != null ? appData.getStream12() : "N/A";
            String applicantName = "N/A";
            // Extract first name and last name for full applicant name display
            if (appData != null && appData.getUsers() != null) {
                applicantName = appData.getUsers().getFirstName() + " " + appData.getUsers().getLastName();
            }

            // Add row to table with test record data
            model.addRow(new Object[]{
                    record.getApplicantId(),
                    applicantName,
                    program,
                    stream,
                    record.getTestDateTime() != null ? record.getTestDateTime().format(formatter) : "Not Set",
                    record.isAttempted() ? "Yes" : "No",
                    record.getScore(),
                    record.getSubjects() != null ? String.join(", ", record.getSubjects()) : "Not Set",
                    "Set Details",
                    "Make Decision"
            });
        }

        // Show message if no applicants have paid the fee
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No applicants have paid the fee yet.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Custom cell renderer for Action column displaying Set Date and Set Subjects buttons
    class ActionCellRenderer extends JPanel implements TableCellRenderer {
        public ActionCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER));
            add(new JButton("Set Date"));      // Button for setting test date/time
            add(new JButton("Set Subjects"));  // Button for selecting test subjects
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            return this;
        }
    }

    // Custom cell editor for Action column handling Set Date and Set Subjects operations
    class ActionCellEditor extends DefaultCellEditor {
        private final JPanel panel;              // Panel containing action buttons
        private final JButton dateButton;        // Button for setting test date/time
        private final JButton subjectButton;     // Button for selecting subjects
        private int editingRow;                  // Currently editing row index

        public ActionCellEditor(JCheckBox checkBox, DefaultTableModel model, JTable table) {
            super(checkBox);
            panel = new JPanel(new FlowLayout());
            dateButton = new JButton("Set Date");
            subjectButton = new JButton("Set Subjects");
            panel.add(dateButton);
            panel.add(subjectButton);

            // Handle Set Date button: prompt for date/time input and save to database
            dateButton.addActionListener(e -> {
                editingRow = table.getSelectedRow();
                if (editingRow == -1) return;
                String applicantId = (String) model.getValueAt(editingRow, 0);  // Get applicant ID from table

                // Prompt admin to enter test date and time in YYYY-MM-DD HH:MM format
                String input = JOptionPane.showInputDialog(null, "Enter Test Date and Time (YYYY-MM-DD HH:MM):");
                if (input == null || input.trim().isEmpty()) return;

                try {
                    // Parse input date/time with specified format
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    LocalDateTime dateTime = LocalDateTime.parse(input.trim(), formatter);

                    // Fetch or create test record for this applicant
                    EntryTestRecordManager.EntryTestRecord record = recordManager.getRecordById(applicantId);
                    if (record == null) {
                        record = new EntryTestRecordManager.EntryTestRecord(applicantId, dateTime, false, 0);
                    }
                    else {
                        record.setTestDateTime(dateTime);  // Update existing record with new test date/time
                    }

                    // Persist test schedule in ApplicationForm for visibility and synchronization
                    ApplicantManager.updateTestSchedule(applicantId, dateTime);

                    // Save updated record to database via EntryTestRecordManager
                    recordManager.saveRecord(record);
                    // Update applicant status to TEST_SCHEDULED
                    ApplicantManager.updateApplicationStatus(applicantId, Status.TEST_SCHEDULED);

                    JOptionPane.showMessageDialog(null, "Test date/time set for " + applicantId);
                    loadTestData();  // Refresh table display with updated data
                    fireEditingStopped();

                }
                catch (Exception ex) {
                    // Show error message for invalid date/time format
                    JOptionPane.showMessageDialog(null,
                            "Invalid date format. Use YYYY-MM-DD HH:MM",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            // Handle Set Subjects button: display checkboxes for subject selection
            subjectButton.addActionListener(e -> {
                editingRow = table.getSelectedRow();
                if (editingRow == -1) return;
                String applicantId = (String) model.getValueAt(editingRow, 0);  // Get applicant ID from table

                // Create dialog with subject checkboxes for admin to select subjects
                JPanel inputPanel = new JPanel(new GridLayout(0, 1));
                String[] subjectOptions = {"Math", "Advanced Math", "English", "Biology"};  // Available subjects
                List<JCheckBox> checkBoxes = new ArrayList<>();

                // Create checkbox for each subject option
                for (String subject : subjectOptions) {
                    JCheckBox cb = new JCheckBox(subject);
                    checkBoxes.add(cb);
                    inputPanel.add(cb);
                }

                // Show dialog and get admin's subject selections
                int result = JOptionPane.showConfirmDialog(null, inputPanel, "Select Subjects", JOptionPane.OK_CANCEL_OPTION);
                if (result != JOptionPane.OK_OPTION) return;

                // Collect selected subjects from checkboxes
                ArrayList<String> selectedSubjects = new ArrayList<>();
                for (JCheckBox cb : checkBoxes) {
                    if (cb.isSelected()) selectedSubjects.add(cb.getText());
                }

                // Validate that at least one subject is selected
                if (selectedSubjects.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please select at least one subject.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Fetch or create test record for this applicant
                EntryTestRecordManager.EntryTestRecord record = recordManager.getRecordById(applicantId);
                if (record == null) {
                    record = new EntryTestRecordManager.EntryTestRecord(applicantId, null, false, 0);
                }
                // Update record with selected subjects
                record.setSubjects(selectedSubjects);

                // Save updated record with subjects to database
                recordManager.saveRecord(record);
                JOptionPane.showMessageDialog(null, "Subjects set for " + applicantId);
                loadTestData();  // Refresh table display with updated data
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            editingRow = row;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }
    }

    // Custom cell renderer for Decision column displaying Send Offer and Reject buttons
    class DecisionCellRenderer extends JPanel implements TableCellRenderer {
        public DecisionCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER));
            add(new JButton("Send Offer"));  // Button to send admission offer to applicant
            add(new JButton("Reject"));      // Button to reject applicant
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            return this;
        }
    }

    // Custom cell editor for Decision column handling admission offer and rejection operations
    class DecisionCellEditor extends DefaultCellEditor {
        private final JPanel panel;           // Panel containing decision buttons
        private final JButton offerButton;    // Button to send admission offer
        private final JButton rejectButton;   // Button to reject application
        private int editingRow;               // Currently editing row index

        public DecisionCellEditor(JCheckBox checkBox, DefaultTableModel model, JTable table) {
            super(checkBox);
            panel = new JPanel(new FlowLayout());
            offerButton = new JButton("Send Offer");
            rejectButton = new JButton("Reject");

            panel.add(offerButton);
            panel.add(rejectButton);

            // Handle Send Offer button: update applicant status to ADMISSION_OFFERED
            offerButton.addActionListener(e -> {
                editingRow = table.getSelectedRow();
                if (editingRow == -1) return;
                String applicantId = (String) model.getValueAt(editingRow, 0);  // Get applicant ID from table
                // Update applicant's status to ADMISSION_OFFERED in database
                ApplicantManager.updateApplicationStatus(applicantId, Status.ADMISSION_OFFERED);
                JOptionPane.showMessageDialog(null, "Admission offered to " + applicantId);
                fireEditingStopped();
            });

            // Handle Reject button: update applicant status to REJECTED
            rejectButton.addActionListener(e -> {
                editingRow = table.getSelectedRow();
                if (editingRow == -1) return;
                String applicantId = (String) model.getValueAt(editingRow, 0);  // Get applicant ID from table
                // Update applicant's status to REJECTED in database
                ApplicantManager.updateApplicationStatus(applicantId, Status.REJECTED);
                JOptionPane.showMessageDialog(null, "Application rejected for " + applicantId);
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            editingRow = row;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }
    }
}
