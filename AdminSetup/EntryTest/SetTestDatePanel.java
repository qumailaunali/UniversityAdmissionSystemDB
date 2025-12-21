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

public class SetTestDatePanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private EntryTestRecordManager recordManager;

    public SetTestDatePanel(EntryTestRecordManager recordManager) {
        this.recordManager = recordManager;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header with title and refresh button
        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Set Entry Test Date, Time & Subjects");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setHorizontalAlignment(SwingConstants.LEFT);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadTestData());
        header.add(title, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] columns = {
                "Applicant ID", "Applicant Name", "Program", "12th Stream", "Test Date & Time",
                "Attempted", "Score", "Subjects", "Action", "Decision"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8 || column == 9;
            }
        };

        table = new JTable(model);
        table.setRowHeight(40);
        loadTestData();

        table.getColumn("Action").setCellRenderer(new ActionCellRenderer());
        table.getColumn("Action").setCellEditor(new ActionCellEditor(new JCheckBox(), model, table));
        table.getColumn("Decision").setCellRenderer(new DecisionCellRenderer());
        table.getColumn("Decision").setCellEditor(new DecisionCellEditor(new JCheckBox(), model, table));

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(100); // ID
        columnModel.getColumn(1).setPreferredWidth(150); // Applicant Name
        columnModel.getColumn(2).setPreferredWidth(100); // Program
        columnModel.getColumn(3).setPreferredWidth(100); // Stream
        columnModel.getColumn(4).setPreferredWidth(150); // Date & Time
        columnModel.getColumn(5).setPreferredWidth(80);  // Attempted
        columnModel.getColumn(6).setPreferredWidth(60);  // Score
        columnModel.getColumn(7).setPreferredWidth(150); // Subjects
        columnModel.getColumn(8).setPreferredWidth(180); // Action buttons
        columnModel.getColumn(9).setPreferredWidth(180); // Decision buttons

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadTestData() {
        model.setRowCount(0);
        List<EntryTestRecordManager.EntryTestRecord> existingRecords = recordManager.loadAllRecords();
        List<String> applicantIds = ApplicantManager.getAllApplicantIds();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (String id : applicantIds) {
            if (!PaymentManager.isFeePaid(id)) {
                continue;
            }

            EntryTestRecordManager.EntryTestRecord record = null;
            for (EntryTestRecordManager.EntryTestRecord r : existingRecords) {
                if (r.getApplicantId().equals(id)) {
                    record = r;
                    break;
                }
            }

            if (record == null) {
                record = new EntryTestRecordManager.EntryTestRecord(id, null, false, 0);
            }

            ApplicationFormData appData = ApplicantManager.getApplicationByAppId(id);
            String program = appData != null ? appData.getSelectedProgram() : "N/A";
            String stream = appData != null ? appData.getStream12() : "N/A";
            String applicantName = "N/A";
            if (appData != null && appData.getUsers() != null) {
                applicantName = appData.getUsers().getFirstName() + " " + appData.getUsers().getLastName();
            }

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

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No applicants have paid the fee yet.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    class ActionCellRenderer extends JPanel implements TableCellRenderer {
        public ActionCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER));
            add(new JButton("Set Date"));
            add(new JButton("Set Subjects"));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            return this;
        }
    }

    class ActionCellEditor extends DefaultCellEditor {
        private final JPanel panel;
        private final JButton dateButton;
        private final JButton subjectButton;
        private final JTable table;
        private int editingRow;

        public ActionCellEditor(JCheckBox checkBox, DefaultTableModel model, JTable table) {
            super(checkBox);
            this.table = table;
            panel = new JPanel(new FlowLayout());
            dateButton = new JButton("Set Date");
            subjectButton = new JButton("Set Subjects");
            panel.add(dateButton);
            panel.add(subjectButton);

            dateButton.addActionListener(e -> {
                editingRow = table.getSelectedRow();
                if (editingRow == -1) return;
                String applicantId = (String) model.getValueAt(editingRow, 0);

                String input = JOptionPane.showInputDialog(null, "Enter Test Date and Time (YYYY-MM-DD HH:MM):");
                if (input == null || input.trim().isEmpty()) return;

                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    LocalDateTime dateTime = LocalDateTime.parse(input.trim(), formatter);

                    EntryTestRecordManager.EntryTestRecord record = recordManager.getRecordById(applicantId);
                    if (record == null) {
                        record = new EntryTestRecordManager.EntryTestRecord(applicantId, dateTime, false, 0);
                    }

                    else {
                        record.setTestDateTime(dateTime);
                    }

                    // Persist schedule on ApplicationForm for visibility
                    ApplicantManager.updateTestSchedule(applicantId, dateTime);

                    recordManager.saveRecord(record);
                    ApplicantManager.updateApplicationStatus(applicantId, Status.TEST_SCHEDULED);

                    JOptionPane.showMessageDialog(null, "Test date/time set for " + applicantId);
                    loadTestData();
                    fireEditingStopped();

                }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,
                            "Invalid date format. Use YYYY-MM-DD HH:MM",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            subjectButton.addActionListener(e -> {
                editingRow = table.getSelectedRow();
                if (editingRow == -1) return;
                String applicantId = (String) model.getValueAt(editingRow, 0);

                JPanel inputPanel = new JPanel(new GridLayout(0, 1));
                // Align subject labels with applicant-side checks
                String[] subjectOptions = {"Math", "Advanced Math", "English", "Biology"};
                List<JCheckBox> checkBoxes = new ArrayList<>();

                for (String subject : subjectOptions) {
                    JCheckBox cb = new JCheckBox(subject);
                    checkBoxes.add(cb);
                    inputPanel.add(cb);
                }

                int result = JOptionPane.showConfirmDialog(null, inputPanel, "Select Subjects", JOptionPane.OK_CANCEL_OPTION);
                if (result != JOptionPane.OK_OPTION) return;

                ArrayList<String> selectedSubjects = new ArrayList<>();
                for (JCheckBox cb : checkBoxes) {
                    if (cb.isSelected()) selectedSubjects.add(cb.getText());
                }

                if (selectedSubjects.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please select at least one subject.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                EntryTestRecordManager.EntryTestRecord record = recordManager.getRecordById(applicantId);
                if (record == null) {
                    record = new EntryTestRecordManager.EntryTestRecord(applicantId, null, false, 0);
                }
                record.setSubjects(selectedSubjects);

                recordManager.saveRecord(record);
                JOptionPane.showMessageDialog(null, "Subjects set for " + applicantId);
                loadTestData();
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

    class DecisionCellRenderer extends JPanel implements TableCellRenderer {
        public DecisionCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER));
            add(new JButton("Send Offer"));
            add(new JButton("Reject"));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            return this;
        }
    }

    class DecisionCellEditor extends DefaultCellEditor {
        private final JPanel panel;
        private final JButton offerButton;
        private final JButton rejectButton;
        private final JTable table;
        private int editingRow;

        public DecisionCellEditor(JCheckBox checkBox, DefaultTableModel model, JTable table) {
            super(checkBox);
            this.table = table;
            panel = new JPanel(new FlowLayout());
            offerButton = new JButton("Send Offer");
            rejectButton = new JButton("Reject");

            panel.add(offerButton);
            panel.add(rejectButton);

            offerButton.addActionListener(e -> {
                editingRow = table.getSelectedRow();
                if (editingRow == -1) return;
                String applicantId = (String) model.getValueAt(editingRow, 0);
                ApplicantManager.updateApplicationStatus(applicantId, Status.ADMISSION_OFFERED);
                JOptionPane.showMessageDialog(null, "Admission offered to " + applicantId);
                fireEditingStopped();
            });

            rejectButton.addActionListener(e -> {
                editingRow = table.getSelectedRow();
                if (editingRow == -1) return;
                String applicantId = (String) model.getValueAt(editingRow, 0);
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
