package Applicant;

import AdminSetup.College.CollegeManager;
import AdminSetup.Program.ProgramManager;
import Authentication.LoginFrame;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ApplicantDashboard_Panel extends JFrame{
    private Applicant userInfo;
    private JPanel contentPanel;
    private ProgramManager programManager;
    private CollegeManager collegeManager;
    // Removed unused field: applicationFormData

    private static final Color COLORAZ_BLACK = Color.BLACK;
    private static final Color COLORAZ_SAGE = new Color(180, 195, 180);
    private static final Color COLORAZ_WHITE = new Color(255, 255, 255);

    public ApplicantDashboard_Panel(Applicant userInfo, ProgramManager programManager, CollegeManager collegeManager) {
        this.userInfo = userInfo;
        this.programManager = programManager;
        this.collegeManager = collegeManager;
        setupFrame();
        initUI();
    }

    private void setupFrame() {
        setTitle("Applicant Dashboard");
        setSize(1600, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLORAZ_WHITE);
        setLocationRelativeTo(null); // Center the window
    }

    private void initUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLORAZ_SAGE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel welcomeLabel = new JLabel("Welcome to Colaraz, " + userInfo.getFirstName(), SwingConstants.LEFT);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(COLORAZ_BLACK);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.X_AXIS));
        menuPanel.setBackground(COLORAZ_BLACK);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        menuPanel.setPreferredSize(new Dimension(1000, 60));

        String[] menuItems = {
                "Home",
                "Apply Now",
                "Submitted Form List",
                "View Colleges & Programs",
                "Payment Portal",
//                "Edit Profile",
                "Logout"
        };

        for (String item : menuItems) {
            JButton menuButton = new JButton(item);

            menuButton.setForeground(COLORAZ_WHITE);
            menuButton.setBackground(COLORAZ_BLACK);
            menuButton.setBorderPainted(false);
            menuButton.setFocusPainted(false);
            menuButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            menuButton.addActionListener(e -> {
                try {
                    handleMenuClick(item);
                }
                catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });

            menuPanel.add(menuButton);

            // Add spacing between buttons
            menuPanel.add(Box.createHorizontalStrut(10));
        }

        // Content Panel (Dynamic)
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(COLORAZ_WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel defaultContent = new JLabel("Select an option from the menu above", SwingConstants.CENTER);
        defaultContent.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        contentPanel.add(defaultContent, BorderLayout.CENTER);

        // Combine menu + content
        JPanel middlePanel = new JPanel(new BorderLayout());
        middlePanel.add(menuPanel, BorderLayout.NORTH);
        middlePanel.add(contentPanel, BorderLayout.CENTER);
        add(middlePanel, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(COLORAZ_SAGE);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        JLabel copyright = new JLabel("© 2025 Colaraz. All rights reserved.", SwingConstants.CENTER);
        copyright.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerPanel.add(copyright);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void handleMenuClick(String menuItem) throws IOException {
        switch (menuItem) {
            case "Home" -> showHome();
            case "Apply Now" -> showApplicationForm();
            case "Submitted Form List" -> showSubmittedFormList();
            case "View Colleges & Programs" -> showCollegeAndProgramViewer();
            case "Payment Portal" -> showPaymentPortal();
            // Scholarship removed from user menu
//            case "Documents" -> showDocumentsPanel();
//            case "Edit Profile" -> showEditProfilePanel();
            case "Logout" -> {
                dispose();
                new LoginFrame().setVisible(true);
            }
        }
    }

    private void showHome() {
        contentPanel.removeAll();

        JPanel messagePanel = new JPanel(new GridBagLayout());
        JLabel welcomeLabel = new JLabel("<html><div style='text-align: center;'>Please select an option from the menu above.</div></html>");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        messagePanel.add(welcomeLabel);

        contentPanel.add(messagePanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showApplicationForm() throws IOException {
        ApplicationForm_Panel formPanel = new ApplicationForm_Panel(userInfo, this.programManager, this.collegeManager);
        contentPanel.removeAll();
        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showSubmittedFormList() {
        SubmittedFormList_Panel submittedFormListPanel = new SubmittedFormList_Panel(userInfo);
        contentPanel.removeAll();
        contentPanel.add(submittedFormListPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showCollegeAndProgramViewer() {
        CollegeAndProgramViewer_Panel viewerPanel = new CollegeAndProgramViewer_Panel();
        contentPanel.removeAll();
        contentPanel.add(viewerPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showPaymentPortal() {
        PaymentPortal_Panel portalPanel = new PaymentPortal_Panel(userInfo);
        contentPanel.removeAll();
        contentPanel.add(portalPanel,BorderLayout.CENTER); // Use the actual panel
        contentPanel.revalidate();
        contentPanel.repaint();
    }


    // Scholarship form removed

//    private void showDocumentsPanel() {
//        DocumentsPanel documentsPanel = new DocumentsPanel(userInfo); // Replace with actual panel
//        contentPanel.removeAll();
//        contentPanel.add(documentsPanel, BorderLayout.CENTER);
//        contentPanel.revalidate();
//        contentPanel.repaint();
//    }

//    private void showEditProfilePanel() {
//        EditProfilePanel editPanel = new EditProfilePanel(userInfo); // Replace with actual panel
//        contentPanel.removeAll();
//        contentPanel.add(editPanel, BorderLayout.CENTER);
//        contentPanel.revalidate();
//        contentPanel.repaint();
//    }
}
