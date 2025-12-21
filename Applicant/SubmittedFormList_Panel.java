package Applicant;

import AdminSetup.EntryTest.EntryTestRecordManager;
import Applicant.Tests.AdvancedMathTest;
import Applicant.Tests.BioTest;
import Applicant.Tests.EnglishTest;
import Applicant.Tests.MathTest;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class SubmittedFormList_Panel extends JPanel {
    String appID;
    private Applicant userInfo;
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private ArrayList<ApplicationFormData> userApplications;

    public SubmittedFormList_Panel(Applicant userInfo) {
        this.userInfo = userInfo;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Title and Search
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
        searchField.setToolTipText("Search by Application No., Program or College");

        // Header with title and refresh
        JPanel headerBar = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Submitted Applications Record", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.BLACK);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshApplications());
        headerBar.add(title, BorderLayout.WEST);
        headerBar.add(refreshBtn, BorderLayout.EAST);

        searchPanel.add(headerBar, BorderLayout.NORTH);
        searchPanel.add(searchField, BorderLayout.CENTER);
        add(searchPanel, BorderLayout.NORTH);

        try {
            this.userApplications = new ArrayList<>(ApplicantManager.getApplicationsByUserEmail(userInfo.getEmail()));
        } catch (Exception e) {
            e.printStackTrace();
            add(new JLabel("Error loading applications.", SwingConstants.CENTER), BorderLayout.CENTER);
            return;
        }

        if (userApplications == null || userApplications.isEmpty()) {
            add(new JLabel("Ab tak koi application submit nahi hui hai.", SwingConstants.CENTER), BorderLayout.CENTER);
            return;
        }

        String[] columns = {
                "Application No.",
                "Program",
                "College",
                "Email",
                "Status",           // combined application + fee status
                "Test Schedule",
                "Test Score",
                "Give Test"
        };

        model = new DefaultTableModel(columns, 0);
        table = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Only "Give Test" button editable
            }
        };

        table.setRowHeight(40);
        table.setDefaultRenderer(Object.class, new CustomRowRenderer());
        table.getColumn("Give Test").setCellRenderer(new ButtonRenderer());
        table.getColumn("Give Test").setCellEditor(new ButtonEditor(new JCheckBox()));

        populateTable(userApplications);

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String query = searchField.getText().toLowerCase();
                model.setRowCount(0);
                for (ApplicationFormData app : userApplications) {
                    if (app.getApplicationId().toLowerCase().contains(query)
                            || app.getSelectedProgram().toLowerCase().contains(query)
                            || app.getSelectedCollege().toLowerCase().contains(query)) {
                        addRow(app);
                    }
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void refreshApplications() {
        try {
            this.userApplications = new ArrayList<>(ApplicantManager.getApplicationsByUserEmail(userInfo.getEmail()));
            model.setRowCount(0);
            if (userApplications != null) {
                populateTable(userApplications);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to refresh applications.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateTable(ArrayList<ApplicationFormData> applications) {
        for (ApplicationFormData app : applications) {
            addRow(app);
        }
    }

    private void addRow(ApplicationFormData app) {
        EntryTestRecordManager entryTestRecordManager = new EntryTestRecordManager();
        EntryTestRecordManager.EntryTestRecord recordById = entryTestRecordManager.getRecordById(app.getApplicationId());

        String schedule = "Not Scheduled";
        String score = "N/A";
        String actionText = "Unavailable";

        if (recordById != null) {
            LocalDateTime testDate = recordById.getTestDateTime();
            if (testDate != null) {
                schedule = testDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

                // Allow taking test on or after the scheduled date (and if not already attempted)
                if (!recordById.isAttempted() && !testDate.isAfter(LocalDateTime.now())) {
                    actionText = "Give Test Now";
                } else {
                    actionText = "Scheduled";
                }
            }

            if (recordById.isAttempted()) {
                score = String.valueOf(recordById.getScore());
                actionText = "Completed";
            }
        }

        String combinedStatus = formatCombinedStatus(app.getStatus(), app.getFeeStatus());
        appID=app.getApplicationId();
        model.addRow(new Object[]{
                app.getApplicationId(),
                app.getSelectedProgram() != null ? app.getSelectedProgram() : "N/A",
                app.getSelectedCollege() != null ? app.getSelectedCollege() : "N/A",
                userInfo.getEmail(),
                combinedStatus,
                schedule,
                score,
                actionText
        });
    }





    private String formatCombinedStatus(Status status, FeeStatus feeStatus) {
        String statusText = switch (status) {
            case SUBMITTED -> "Submitted";
            case APPROVED -> "Approved";
            case REJECTED -> "Rejected";
            case TEST_SCHEDULED -> "Test Scheduled";
            case TEST_TAKEN -> "Test Taken";
            case ADMISSION_OFFERED -> "Admission Offered";
            case WAIT_LISTED -> "Wait Listed";
            case ADMISSION_SECURED -> "Admission Secured";
            case ADMISSION_WITHDRAWN -> "Withdrawn";
        };

        String feeText = "";
        if (feeStatus != null) {
            feeText = switch (feeStatus) {
                case PAID -> " (Fee Paid)";
                case UNPAID -> " (Fee Unpaid)";
                default -> "";
            };
        }

        return statusText + feeText;
    }

    private class CustomRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {  // only set background if not selected
                String status = table.getValueAt(row, 4).toString();  // combined status column

                if (status.contains("Approved")) {
                    c.setBackground(new Color(198, 239, 206)); // Light green (success)
                } else if (status.contains("Rejected") || status.contains("Admission Withdrawn")) {
                    c.setBackground(new Color(255, 199, 206)); // Light red (error)
                } else if (status.contains("Submitted") || status.contains("Test Scheduled")) {
                    c.setBackground(new Color(255, 235, 156)); // Light yellow (pending)
                } else if (status.contains("Test Taken")) {
                    c.setBackground(new Color(209, 222, 255)); // Light blue (completed action)
                } else if (status.contains("Wait Listed")) {
                    c.setBackground(new Color(255, 207, 159)); // Light orange (waiting)
                } else if (status.contains("Admission Secured")|| status.contains("Admission Offered")) {
                    c.setBackground(new Color(183, 225, 205)); // Medium green (confirmed)
                } else {
                    c.setBackground(Color.WHITE); // Default
                }
            }
            return c;
        }
    }

    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            String text = (value == null) ? "" : value.toString();
            setText(text);
            boolean isGiveTest = "Give Test Now".equals(text);
            setEnabled(isGiveTest);
            setBackground(isGiveTest ? Color.GREEN : Color.LIGHT_GRAY);
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int selectedRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            selectedRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed && "Give Test Now".equals(label)) {
                int modelRow = table.convertRowIndexToModel(selectedRow);
                ApplicationFormData selectedApp = userApplications.get(modelRow);

                EntryTestRecordManager entryTestRecordManager = new EntryTestRecordManager();
                EntryTestRecordManager.EntryTestRecord record = entryTestRecordManager.getRecordById(selectedApp.getApplicationId());

                if (record == null || record.getSubjects() == null || record.getSubjects().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Test details are not available yet. Please contact admin.");
                    isPushed = false;
                    return label;
                }

                JFrame frame = new JFrame("Start Your Test");
                frame.setSize(500, 350);
                frame.setLocationRelativeTo(null);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                JPanel subjectPanel = new JPanel(new GridLayout(2, 2, 15, 15));
                subjectPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

                JButton englishBtn = new JButton("ENGLISH");
                JButton bioBtn = new JButton("BIOLOGY");
                JButton addMathBtn = new JButton("ADVANCED MATH");
                JButton mathBtn = new JButton("MATH");

                englishBtn.setEnabled(false);
                bioBtn.setEnabled(false);
                addMathBtn.setEnabled(false);
                mathBtn.setEnabled(false);

                for (String subject : record.getSubjects()) {
                    switch (subject.trim().toLowerCase()) {
                        case "english" -> {
                            englishBtn.setEnabled(!record.isEnglishTaken());
                            englishBtn.addActionListener(e -> {
                                new EnglishTest(record);
                                checkAllSubjectsCompleted(record);
                                englishBtn.setEnabled(false);
                            });
                        }
                        case "biology" -> {
                            bioBtn.setEnabled(!record.isBiologyTaken());
                            bioBtn.addActionListener(e -> {
                                new BioTest(record);
                                checkAllSubjectsCompleted(record);
                                bioBtn.setEnabled(false);
                            });
                        }
                        case "add math", "advanced math", "add maths", "advanced maths" ->
                        {
                            addMathBtn.setEnabled(!record.isAdvMathTaken());
                            addMathBtn.addActionListener(e -> {
                                new AdvancedMathTest(record);
                                checkAllSubjectsCompleted(record);
                                addMathBtn.setEnabled(false);
                            });
                        }
                        case "math", "maths" -> {
                            mathBtn.setEnabled(!record.isMathTaken());
                            mathBtn.addActionListener(e -> {
                                new MathTest(record);
                                checkAllSubjectsCompleted(record);
                                mathBtn.setEnabled(false);
                            });
                        }
                    }
                }

                subjectPanel.add(englishBtn);
                subjectPanel.add(bioBtn);
                subjectPanel.add(addMathBtn);
                subjectPanel.add(mathBtn);

//                // Create End Exam button
                JButton endExamBtn = new JButton("End Exam");
                endExamBtn.setBackground(new Color(220, 53, 69)); // Red
                endExamBtn.setForeground(Color.black);
                endExamBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
                endExamBtn.setFocusPainted(false);
                endExamBtn.setPreferredSize(new Dimension(0, 40));

                endExamBtn.addActionListener(e -> {

                    int confirm = JOptionPane.showConfirmDialog(
                            frame,
                            "Are you sure you want to end the exam?",
                            "Confirm End Exam",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (confirm == JOptionPane.YES_OPTION) {
                        record.setAttempted(true);
                        record.setStatus(Status.TEST_TAKEN);
                        int mark=calculateMarks(record);
                        record.setScore(mark);
                        new EntryTestRecordManager().saveRecord(record);

                        JOptionPane.showMessageDialog(frame, "Exam ended successfully. Status updated to TEST_TAKEN.");
                        frame.dispose();
                    }
                });

                JPanel wrapper = new JPanel(new BorderLayout());
                wrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                wrapper.add(subjectPanel, BorderLayout.CENTER);
                wrapper.add(endExamBtn, BorderLayout.SOUTH);

                frame.add(wrapper);
                frame.setVisible(true);
            }

            isPushed = false;
            return label;
        }

        private int calculateMarks(EntryTestRecordManager.EntryTestRecord testRecord){
            int total = 0;

            if (testRecord.isEnglishTaken()) {
                total += EnglishTest.getEngScore();
            }
            if (testRecord.isMathTaken()) {
                total += MathTest.getMathScore();
            }
            if (testRecord.isBiologyTaken()) {
                total += BioTest.getBioMarks();
            }
            if (testRecord.isAdvMathTaken()) {
                total += AdvancedMathTest.getAdvMathScore();
            }

            return total;
        }



        private void checkAllSubjectsCompleted(EntryTestRecordManager.EntryTestRecord record) {
            boolean allDone = true;
            for (String subject : record.getSubjects()) {
                switch (subject.trim().toLowerCase()) {
                    case "english" -> allDone &= record.isEnglishTaken();
                    case "biology" -> allDone &= record.isBiologyTaken();
                    case "math", "maths" -> allDone &= record.isMathTaken();
                    case "add math", "advanced math", "add maths", "advanced maths" ->
                            allDone &= record.isAdvMathTaken();
                }
            }

            if (allDone) {
                record.setAttempted(true);
                record.setStatus(Status.TEST_TAKEN);//// ← update enum status here
                ApplicantManager.updateApplicationStatus(appID,Status.TEST_TAKEN);
                new EntryTestRecordManager().saveRecord(record); // Save changes
                JOptionPane.showMessageDialog(null, "All tests completed. Status updated to TEST_TAKEN!");
            }
        }
    }
}
//sssssss