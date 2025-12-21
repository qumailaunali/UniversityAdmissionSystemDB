package AdminSetup.Applicants;

import Applicant.ApplicantManager;
import Applicant.ApplicationFormData;
import Applicant.Status;
import Authentication.AdminLogin;
import Authentication.Admins;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.ArrayList;

// Panel to display and manage submitted applicant applications
// Allows admins to approve or reject applications and automatically records the admin ID
public class ViewApplicantsPanel extends JPanel {

    private DefaultTableModel model;
    private JTable table;

    public ViewApplicantsPanel() {
        // Setup: Create table with application data and action buttons for approval/rejection
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
        // Load all submitted applications from database
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
        // Add custom renderers/editors for approve/reject buttons
        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadApplicants() {
        // Load all applications from database and populate table rows
        // SQL: SELECT * FROM ApplicationForm (via ApplicantManager.loadAllApplications())
        model.setRowCount(0);
        try {
            // Fetch all submitted applications from database
            ArrayList<ApplicationFormData> applicants = ApplicantManager.loadAllApplications();
            for (ApplicationFormData app : applicants) {
                // Extract fee status (default UNPAID if null)
                String feeStatus = app.getFeeStatus() != null ? app.getFeeStatus().toString() : "UNPAID";
                // Extract applicant full name
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
        // Custom button with background color that changes on hover/press
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
            // Change color based on button state: pressed (darker), hover (brighter), normal
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
        // Render approve/reject buttons in Action column
        // Green button for approve, red for reject
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
            // Get current application status from row
            String status = (String) table.getValueAt(row, 7);
            // Only enable buttons if application is in SUBMITTED status
            boolean enabled = status.equalsIgnoreCase(Status.SUBMITTED.toString());
            btnAccept.setEnabled(enabled);
            btnReject.setEnabled(enabled);

            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }

    class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        // Handle approve/reject button clicks and update database
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
                // Approve application: get ID, verify status, update database with admin ID, refresh table
                int row = table.getEditingRow();
                if (row == -1) return;

                String appId = (String) model.getValueAt(row, 0);
                try {
                    // Check current status before update (prevent double-processing)
                    Status status = ApplicantManager.getApplicationStatus(appId);
                    if (status == Status.SUBMITTED) {
                        // Get current admin who is approving the application
                        Admins currentAdmin = AdminLogin.getCurrentAdmin();
                        if (currentAdmin != null) {
                            // SQL: UPDATE ApplicationForm SET Status = 'APPROVED', AdminID = ? WHERE ApplicationID = ?
                            ApplicantManager.updateApplicationStatusWithAdmin(appId, Status.APPROVED, currentAdmin.getAdminID());
                            // Update table display
                            model.setValueAt(Status.APPROVED.toString(), row, 7);
                            JOptionPane.showMessageDialog(null, "Application ID " + appId + " has been APPROVED by Admin ID " + currentAdmin.getAdminID());
                        } else {
                            JOptionPane.showMessageDialog(null, "Error: No admin session found.");
                        }
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
                // Reject application: get ID, verify status, update database with admin ID, refresh table
                int row = table.getEditingRow();
                if (row == -1) return;

                String appId = (String) model.getValueAt(row, 0);
                try {
                    // Check current status before update
                    Status status = ApplicantManager.getApplicationStatus(appId);
                    if (status == Status.SUBMITTED) {
                        // Get current admin who is rejecting the application
                        Admins currentAdmin = AdminLogin.getCurrentAdmin();
                        if (currentAdmin != null) {
                            // SQL: UPDATE ApplicationForm SET Status = 'REJECTED', AdminID = ? WHERE ApplicationID = ?
                            ApplicantManager.updateApplicationStatusWithAdmin(appId, Status.REJECTED, currentAdmin.getAdminID());
                            // Update table display
                            model.setValueAt(Status.REJECTED.toString(), row, 7);
                            JOptionPane.showMessageDialog(null, "Application ID " + appId + " has been REJECTED by Admin ID " + currentAdmin.getAdminID());
                        } else {
                            JOptionPane.showMessageDialog(null, "Error: No admin session found.");
                        }
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
