package Authentication;
import AdminSetup.*;


import javax.swing.*;
import java.awt.*;

public class AdminLoginFrame extends JFrame{
    AdminLogin adminLogin = new AdminLogin();
    public AdminLoginFrame(){
        setLayout(null); // use absolute positioning for manually set bounds
        getContentPane().setBackground(new Color(245, 245, 245));
        
        JLabel title = new JLabel("Colaraz");
        title.setFont(new Font("Tahoma", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setBounds(5, 0, 400, 30);

        JPanel green = new JPanel();
        green.setBackground(new Color(116, 202, 74));
        green.setBounds(0, 0, 1300, 35);
        green.setLayout(null);
        green.add(title);
        add(green);

        // Beautiful main heading with green background panel
        JPanel headingPanel = new JPanel();
        headingPanel.setBackground(new Color(116, 202, 74));
        headingPanel.setBounds(0, 35, 1300, 100);
        headingPanel.setLayout(null);
        
        JLabel mainHeading = new JLabel("University Admission System");
        mainHeading.setFont(new Font("Arial", Font.BOLD, 42));
        mainHeading.setForeground(Color.WHITE);
        mainHeading.setBounds(150, 15, 1000, 70);
        mainHeading.setHorizontalAlignment(SwingConstants.CENTER);
        headingPanel.add(mainHeading);

        JLabel title2=new JLabel();
        title2.setText("Admin Login");
        title2.setBounds(0, 180, 1300, 40);
        title2.setFont(new Font("Arial", Font.BOLD, 32));
        title2.setForeground(new Color(116, 202, 74));
        title2.setHorizontalAlignment(SwingConstants.CENTER);


        JLabel username;
        JLabel password;

        username=new JLabel("Email");
        username.setFont(new Font("Arial", Font.BOLD, 18));
        username.setForeground(new Color(80, 80, 80));
        username.setBounds(450, 270, 150, 30);
        
        password= new JLabel("Password");
        password.setFont(new Font("Arial", Font.BOLD, 18));
        password.setForeground(new Color(80, 80, 80));
        password.setBounds(450, 350, 150, 30);

        JTextField emailField;
        JPasswordField passwordField;

        emailField= new JTextField();
        emailField.setBounds(450, 305, 400, 40);
        emailField.setFont(new Font("Arial", Font.PLAIN, 16));
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        passwordField = new JPasswordField();
        passwordField.setBounds(450, 385, 400, 40);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        RoundedButton Enter;
        Enter = new RoundedButton("Login", 25, 25);
        Enter.setBounds(450, 460, 400, 45);
        Enter.setFont(new Font("Arial", Font.BOLD, 18));
        Enter.setBackground(new Color(116, 202, 74));
        Enter.setForeground(Color.WHITE);
        Enter.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Back button
        RoundedButton backButton = new RoundedButton("← Back", 20, 20);
        backButton.setBounds(15, 145, 100, 38);
        backButton.setFont(new Font("Arial", Font.BOLD, 15));
        backButton.setBackground(new Color(80, 80, 80));
        backButton.setForeground(Color.WHITE);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        this.setSize(1300, 900);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(green);
        this.add(headingPanel);
        this.add(backButton);
        this.add(title2);
        this.add(username);
        this.add(password);
        this.add(emailField);
        this.add(passwordField);
        this.add(Enter);

        backButton.addActionListener(e -> {
            new Myframe();
            dispose();
        });

        Enter.addActionListener(e -> {
            String Username = emailField.getText();
            String Password = new String(passwordField.getPassword());

            boolean success = adminLogin.login(Username,Password);


            if(success){
                dispose();
                AdminDashboard_Panel adminDashboardPanel= new AdminDashboard_Panel();
                adminDashboardPanel.setVisible(true);


            }
            else {
                JOptionPane.showMessageDialog(null, "Incorrect username or Password");
            }
            adminLogin.saveAdmin();

        });

        this.setVisible(true);

    }







}
