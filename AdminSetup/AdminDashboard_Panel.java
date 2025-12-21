package AdminSetup;

import AdminSetup.AddAdmin.AddAdmin_Panel;
import AdminSetup.Applicants.ViewApplicantsPanel;
import AdminSetup.College.College_Panel;
import AdminSetup.EntryTest.EntryTestRecordManager;
import AdminSetup.EntryTest.SetTestDatePanel;
import AdminSetup.Program.ProgramPanel;
import Authentication.LoginFrame;
import javax.swing.*;
import java.awt.*;

public class AdminDashboard_Panel extends JFrame {
    private JPanel contentPanel;

    private static final Color COLORAZ_BLACK = Color.BLACK;
    private static final Color COLORAZ_SAGE = new Color(180, 195, 180);
    private static final Color COLORAZ_WHITE = new Color(255, 255, 255);

    public AdminDashboard_Panel() {
        setupFrame();
        initUI();
    }

    private void setupFrame() {
        setTitle("Admin Dashboard");
        setSize(1600, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLORAZ_WHITE);
        setLocationRelativeTo(null);
    }

    private void initUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLORAZ_SAGE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel welcomeLabel = new JLabel("Welcome, Admin", SwingConstants.LEFT);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(COLORAZ_BLACK);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        menuPanel.setBackground(COLORAZ_BLACK);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String[] menuItems = {
                "View College",
                "View Applicant",
                "View Program",
                "Set Test",
                "Add Admin",
                "Logout"
        };

        for (String item : menuItems) {
            JButton menuButton = new JButton(item);
            menuButton.setForeground(COLORAZ_WHITE);
            menuButton.setBackground(COLORAZ_BLACK);
            menuButton.setBorderPainted(false);
            menuButton.setFocusPainted(false);
            menuButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            menuButton.addActionListener(e -> handleMenuClick(item));
            menuPanel.add(menuButton);
            menuPanel.add(Box.createHorizontalStrut(10));
        }

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(COLORAZ_WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel defaultContent = new JLabel("Select an option from the menu above", SwingConstants.CENTER);
        defaultContent.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        contentPanel.add(defaultContent, BorderLayout.CENTER);

        JPanel middlePanel = new JPanel(new BorderLayout());
        middlePanel.add(menuPanel, BorderLayout.NORTH);
        middlePanel.add(contentPanel, BorderLayout.CENTER);
        add(middlePanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(COLORAZ_SAGE);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        JLabel copyright =
                new JLabel("© 2025 Colaraz. All rights reserved.", SwingConstants.CENTER);
        copyright.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerPanel.add(copyright);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void handleMenuClick(String menuItem) {
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());

        switch (menuItem) {
            case "View College" -> showColleges();
            case "View Applicant" -> showApplicants();
            case "View Program" -> showPrograms();
            case "Set Test" -> showSetTestDatePanel();
            case "Add Admin" -> showAddAdmin();
            case "Logout" -> {
                dispose();
                new LoginFrame().setVisible(true);
                return;
            }
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showAddAdmin() {
        AddAdmin_Panel formPanel = new AddAdmin_Panel();
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showColleges() {
        College_Panel collegePanel = new College_Panel();
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(collegePanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    private void showPrograms() {
        ProgramPanel programPanel = new ProgramPanel();
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(programPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }


    private void showApplicants() {
        ViewApplicantsPanel applicantsPanel = new ViewApplicantsPanel() ;
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(applicantsPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showSetTestDatePanel() {
        EntryTestRecordManager recordManager = new EntryTestRecordManager();
        SetTestDatePanel panel = new SetTestDatePanel(recordManager);
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }









}
