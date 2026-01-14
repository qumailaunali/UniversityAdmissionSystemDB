package Applicant;
import javax.swing.*;
import java.awt.*;
// import java.awt.event.*;
import java.sql.*;
import java.util.*;
import Database.DBConnection;

public class CollegeAndProgramViewer_Panel extends JPanel {
    private JTextField searchField;
    private JButton searchButton, refreshButton;
    private JPanel resultPanel;
    private JScrollPane scrollPane;

    public CollegeAndProgramViewer_Panel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Top Search Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(245, 245, 255));
        topPanel.setBorder(BorderFactory.createTitledBorder("🔍 Search College / Program / Stream"));

        searchField = new JTextField(25);
        searchButton = new JButton("Search");
        refreshButton = new JButton("Refresh");

        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);

        //  Result Panel with Scroll
        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBackground(Color.WHITE);

        scrollPane = new JScrollPane(resultPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        // Load all data at startup
        loadCollegeData("");

        // Refresh
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadCollegeData("");
        });

        //  Search
        searchButton.addActionListener(e -> {
            String query = searchField.getText().trim().toLowerCase();
            loadCollegeData(query);
        });
    }

    private void loadCollegeData(String query) {
        resultPanel.removeAll();

        // Use view to fetch college, program, and stream data with aggregated streams
        String sql = """
            SELECT college_id, college_name, ProgramID, ProgramName, Seats, Eligibility, Fee, streams
            FROM dbo.vw_CollegesProgramsAggregated
            ORDER BY college_name, ProgramName
        """;

        Map<Integer, JPanel> collegePanels = new LinkedHashMap<>();

        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int collegeId = rs.getInt("college_id");
                String collegeName = rs.getString("college_name");

                JPanel collegePanel = collegePanels.computeIfAbsent(collegeId, id -> createCollegePanel(collegeName));

                String programName = rs.getString("ProgramName");
                if (programName == null) {
                    continue; // No program for this college yet
                }
                int seats = rs.getInt("Seats");
                int eligibility = rs.getInt("Eligibility");
                double fee = rs.getDouble("Fee");
                String streams = rs.getString("streams");

                // Search filter matching similar to file approach
                boolean matchesQuery = query.isEmpty()
                        || collegeName.toLowerCase().contains(query)
                        || programName.toLowerCase().contains(query)
                        || String.valueOf(seats).contains(query)
                        || String.valueOf(eligibility).contains(query)
                        || String.valueOf(fee).contains(query)
                        || (streams != null && streams.toLowerCase().contains(query));

                if (!matchesQuery) {
                    continue;
                }

                JPanel programPanel = new JPanel();
                programPanel.setLayout(new GridLayout(5, 1));
                programPanel.setBackground(new Color(255, 250, 240));
                programPanel.setBorder(BorderFactory.createTitledBorder(programName));

                programPanel.add(new JLabel("Program Name: " + programName));
                programPanel.add(new JLabel("Seats: " + seats));
                programPanel.add(new JLabel("Eligibility: " + eligibility + "%"));
                programPanel.add(new JLabel("Fee: PKR " + String.format("%,.2f", fee) + " per semester"));
                programPanel.add(new JLabel("Eligible Streams: " + (streams == null ? "-" : streams)));

                collegePanel.add(programPanel);
            }

            // Add panels to result
            for (JPanel panel : collegePanels.values()) {
                resultPanel.add(panel);
            }

            if (resultPanel.getComponentCount() == 0) {
                resultPanel.add(new JLabel(" No results found for: " + query));
            }

        } catch (SQLException e) {
            resultPanel.add(new JLabel("⚠ Error loading data from database!"));
            e.printStackTrace();
        }

        revalidate();
        repaint();
    }

    private JPanel createCollegePanel(String collegeName) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("College: " + collegeName));
        panel.setBackground(new Color(230, 255, 250));
        return panel;
    }
}
