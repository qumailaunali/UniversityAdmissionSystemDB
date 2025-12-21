package Authentication;
import AdminSetup.*;


import javax.swing.*;
import java.awt.*;

public class AdminLoginFrame extends JFrame{
    AdminLogin adminLogin = new AdminLogin();
    public AdminLoginFrame(){
        setLayout(null); // use absolute positioning for manually set bounds
        getContentPane().setBackground(Color.LIGHT_GRAY);
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


        JLabel title2=new JLabel();
        title2.setText("Admin Login");
        title2.setBounds(600, 100, 300, 40);
        title2.setFont(new Font("Arial", Font.BOLD, 24));


        JLabel username;
        JLabel password;

        username=new JLabel("Enter your Email ");
        username.setFont(new Font("Tahoma", Font.PLAIN, 20));
        username.setBounds(375, 160, 220, 40);
        password= new JLabel("Enter your password ");
        password.setFont(new Font("Tahoma", Font.PLAIN, 20));
        password.setBounds(375, 240, 220, 40);

        JTextField emailField;
        JPasswordField passwordField;

        emailField= new JTextField();
        emailField.setBounds(650, 170, 180, 25);
        passwordField = new JPasswordField();
        passwordField.setBounds(650, 250, 180, 25);

        JButton Enter ;
        Enter = new JButton("Enter");
        Enter.setBounds(650, 310, 180, 30);


        this.setSize(1300, 900);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(title2);
        this.add(username);
        this.add(password);
        this.add(emailField);
        this.add(passwordField);
        this.add(Enter);

        JButton backButton = new JButton("Back");
        backButton.setBounds(10, 40, 70, 25);
        this.add(backButton);

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
