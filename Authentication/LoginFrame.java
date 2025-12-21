package Authentication;

//import ApplicationForm.ApplicantDashboard;
import Applicant.Applicant;
import Applicant.ApplicantDashboard_Panel;
import AdminSetup.Program.ProgramManager;
import AdminSetup.College.CollegeManager;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    UserLogin userLogin = new UserLogin();


    public LoginFrame(){
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
        title2.setText("Login");
        title2.setBounds(600, 100, 100, 40);
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

        JButton forget ;
        forget= new JButton("Forget Password");
        forget.setBounds(650, 350, 180, 30);

        JButton backButton = new JButton("Back");
        backButton.setBounds(10, 40, 70, 25);
        this.add(backButton);

        backButton.addActionListener(e -> {
            new Myframe();
            dispose();
        });



        this.setSize(1300, 900);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(null);
        this.setVisible(true);
        this.add(title2);
        this.add(username);
        this.add(password);
        this.add(emailField);
        this.add(passwordField);
        this.add(Enter);
        this.add(forget);


//        Enter.addActionListener(e -> {
//            String email = emailField.getText();
//            String password2 = passwordField.getText();
//
//
//            Users success = userLogin.login(email,password2);
//            System.out.println(success);
//            if (success.getEmail()) {
//                JOptionPane.showMessageDialog(null, "Incorrect email or Password");
//            } else {
//                ApplicantDashboard dashboard = new ApplicantDashboard(Users);
//                dispose();
//            }
//
//        });
        Enter.addActionListener(e -> {
            String email = emailField.getText();
            String password2 = new String(passwordField.getPassword());

            Users user = userLogin.login(email, password2);  // login returns User object

            if (user != null) {

                ProgramManager pm = new ProgramManager();
                CollegeManager cm = new CollegeManager();
                Applicant applicant = new Applicant(user.firstName,user.lastName,user.email,user.password,user.securityAnswer,user.cnic,user.dateOfBirth,user.gender,user.phone,user.getUserID());


                // login successful
                ApplicantDashboard_Panel dashboard = new ApplicantDashboard_Panel(applicant,pm, cm); // pass the user object
                dashboard.setVisible(true); // make the dashboard visible
                dispose(); // close the login frame//

            } else {
                // login failed
                JOptionPane.showMessageDialog(null, "Incorrect email or Password");
            }
        });

        forget.addActionListener( e -> {
            new ForgetPasswordFrame();
        });




    }

    public static void main(String[] args) {
        new LoginFrame();
    }
}
