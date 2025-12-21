package AdminSetup.Applicants;

import Applicant.ApplicantManager;
import Applicant.ApplicationFormData;
import Applicant.Status;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.ArrayList;

public class ViewApplicantsPanel extends JPanel {

    private DefaultTableModel model;
    private JTable table;

    public ViewApplicantsPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Submitted Applicant Applications");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        String[] columns = {
                "Application ID", "Applicant Name", "12th Year", "12th %", "12th Stream",
                "Program", "College", "Status", "Fee Status", "Action"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 9; // Only "Action" column editable
            }
        };

        table = new JTable(model);
        table.setRowHeight(40);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        loadApplicants();

        int[] columnWidths = {100, 150, 80, 70, 140, 110, 120, 125, 100, 220};
        for (int i = 0; i < columnWidths.length; i++) {
            if (i < table.getColumnModel().getColumnCount()) {
                table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
                table.getColumnModel().getColumn(i).setMinWidth(columnWidths[i] / 2);
            }
        }

        table.getColumn("Action").setCellRenderer(new ActionCellRenderer());
        table.getColumn("Action").setCellEditor(new ActionCellEditor(table, model));

        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadApplicants() {
        model.setRowCount(0);
        try {
            ArrayList<ApplicationFormData> applicants = ApplicantManager.loadAllApplications();
            for (ApplicationFormData app : applicants) {
                String feeStatus = app.getFeeStatus() != null ? app.getFeeStatus().toString() : "UNPAID";
                String applicantName = "N/A";
                if (app.getUsers() != null) {
                    applicantName = app.getUsers().getFirstName() + " " + app.getUsers().getLastName();
                }
                model.addRow(new Object[]{
                        app.getApplicationId(),
                        applicantName,
                        app.getYear12(),
                        app.getPercent12(),
                        app.getStream12(),
                        app.getSelectedProgram() != null ? app.getSelectedProgram() : "N/A",
                        app.getSelectedCollege() != null ? app.getSelectedCollege() : "N/A",
                        app.getStatus().toString(),
                        feeStatus,
                        "Action"
                });
            }
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    class ColorButton extends JButton {
        private Color bgColor;

        public ColorButton(String text, Color bgColor) {
            super(text);
            this.bgColor = bgColor;
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setOpaque(true);
            setBorderPainted(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (getModel().isPressed()) {
                g.setColor(bgColor.darker());
            }
            else if (getModel().isRollover()) {
                g.setColor(bgColor.brighter());
            }
            else {
                g.setColor(bgColor);
            }
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
        }
    }

    class ActionCellRenderer extends JPanel implements TableCellRenderer {
        private final JButton btnAccept = new ColorButton("Approve", new Color(76, 175, 80)); // Green
        private final JButton btnReject = new ColorButton("Reject", new Color(244, 67, 54));   // Red

        public ActionCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            add(btnAccept);
            add(btnReject);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            String status = (String) table.getValueAt(row, 7);
            boolean enabled = status.equalsIgnoreCase(Status.SUBMITTED.toString());
            btnAccept.setEnabled(enabled);
            btnReject.setEnabled(enabled);

            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }

    class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        private JButton btnAccept;
        private JButton btnReject;
        private DefaultTableModel model;

        public ActionCellEditor(JTable table, DefaultTableModel model) {
            this.model = model;

            btnAccept = new ColorButton("Approve", new Color(76, 175, 80)); // Green
            btnReject = new ColorButton("Reject", new Color(244, 67, 54));  // Red

            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            panel.add(btnAccept);
            panel.add(btnReject);

            btnAccept.addActionListener(e -> {
                int row = table.getEditingRow();
                if (row == -1) return;

                String appId = (String) model.getValueAt(row, 0);
                try {
                    Status status = ApplicantManager.getApplicationStatus(appId);
                    if (status == Status.SUBMITTED) {
                        ApplicantManager.updateApplicationStatus(appId, Status.APPROVED);
                        model.setValueAt(Status.APPROVED.toString(), row, 7);
                        JOptionPane.showMessageDialog(null, "Application ID " + appId + " has been APPROVED.");
                    }
                    else {
                        JOptionPane.showMessageDialog(null, "Application already processed.");
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error while approving application.");
                }
                stopCellEditing();
                table.repaint();
            });

            btnReject.addActionListener(e -> {
                int row = table.getEditingRow();
                if (row == -1) return;

                String appId = (String) model.getValueAt(row, 0);
                try {
                    Status status = ApplicantManager.getApplicationStatus(appId);
                    if (status == Status.SUBMITTED) {
                        ApplicantManager.updateApplicationStatus(appId, Status.REJECTED);
                        model.setValueAt(Status.REJECTED.toString(), row, 7);
                        JOptionPane.showMessageDialog(null, "Application ID " + appId + " has been REJECTED.");
                    }
                    else {
                        JOptionPane.showMessageDialog(null, "Application already processed.");
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error while rejecting application.");
                }
                stopCellEditing();
                table.repaint();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            String status = (String) model.getValueAt(row, 7);
            boolean submitted = status.equalsIgnoreCase(Status.SUBMITTED.toString());
            btnAccept.setEnabled(submitted);
            btnReject.setEnabled(submitted);

            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }
    }
}
