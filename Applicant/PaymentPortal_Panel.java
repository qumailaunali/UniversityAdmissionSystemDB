package Applicant;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import Database.DBConnection;

public class PaymentPortal_Panel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private Applicant userInfo;

    public PaymentPortal_Panel(Applicant userInfo) {
        this.userInfo = userInfo;
        setLayout(new BorderLayout());

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(new JLabel("Applicant ID: " + userInfo.getUserID()));
        infoPanel.add(new JLabel(" | Email: " + userInfo.getEmail()));
        add(infoPanel, BorderLayout.NORTH);

        String[] columnNames = {"Application Form", "Program", "College", "Due Date", "Fee Status", "Action"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JTextField filterField = new JTextField(20);
        filterField.setToolTipText("Filter by any column...");
        filterPanel.add(new JLabel("Search:"));
        filterPanel.add(filterField);
        add(filterPanel, BorderLayout.SOUTH);

        filterField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                search(filterField.getText());
            }

            public void removeUpdate(DocumentEvent e) {
                search(filterField.getText());
            }

            public void changedUpdate(DocumentEvent e) {
                search(filterField.getText());
            }

            private void search(String text) {
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        loadApplications();
    }

    private void loadApplications() {
        tableModel.setRowCount(0);
        
        String sql = """
            SELECT af.application_form_id, af.status, af.fee_status, af.email,
                   p.ProgramName, af.university_name
            FROM dbo.ApplicationForm af
            LEFT JOIN dbo.Program p ON af.programid = p.ProgramID
            WHERE af.email = ? AND af.status IN ('APPROVED', 'TEST_SCHEDULED', 'PAYMENT_CLEARED')
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, userInfo.getEmail());
            
            try (ResultSet rs = ps.executeQuery()) {
                boolean foundAny = false;
                
                while (rs.next()) {
                    foundAny = true;
                    
                    String appId = String.valueOf(rs.getInt("application_form_id"));
                    String program = rs.getString("ProgramName");
                    String college = rs.getString("university_name");
                    String feeStatus = rs.getString("fee_status");
                    
                    String dueDate = DATE_FORMAT.format(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000));
                    String feeStatusDisplay = (feeStatus != null && feeStatus.equalsIgnoreCase("PAID")) ? "Paid" : "Unpaid";

                    tableModel.addRow(new Object[]{
                        appId, 
                        program != null ? program : "N/A", 
                        college != null ? college : "N/A", 
                        dueDate, 
                        feeStatusDisplay, 
                        "Pay Now"
                    });
                }
                
                if (!foundAny) {
                    JOptionPane.showMessageDialog(this, "No forms found.", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading applications: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void markAsPaid(String appId) {
        String sql = "UPDATE dbo.ApplicationForm SET fee_status = ? WHERE application_form_id = ?";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, "PAID");
            ps.setInt(2, Integer.parseInt(appId));
            
            int rowsUpdated = ps.executeUpdate();
            
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Payment successful! Status updated.");
                loadApplications();
            } else {
                JOptionPane.showMessageDialog(this, "Application not found.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating payment status: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
//

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Arial", Font.BOLD, 12));
            setForeground(Color.BLACK);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String feeStatus = table.getValueAt(row, 4).toString();
            if (feeStatus.equalsIgnoreCase("Paid")) {
                setText("Paid");
                setEnabled(false);
                setBackground(Color.GRAY);
            } else {
                setText("Pay Now");
                setEnabled(true);
                setBackground(new Color(255, 165, 0));
            }
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String appId;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.setForeground(Color.BLACK);
            button.setBackground(new Color(255, 165, 0));
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            appId = table.getValueAt(row, 0).toString();
            String feeStatus = table.getValueAt(row, 4).toString();

            if (feeStatus.equalsIgnoreCase("Paid")) {
                button.setText("Paid");
                button.setEnabled(false);
                button.setBackground(Color.GRAY);
            } else {
                button.setText("Pay Now");
                button.setEnabled(true);
                button.setBackground(new Color(255, 165, 0));
            }

            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                markAsPaid(appId);
            }
            isPushed = false;
            return "Pay Now";
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}
