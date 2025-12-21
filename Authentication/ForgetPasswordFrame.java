package Authentication;

import javax.swing.*;
import java.awt.*;

public class ForgetPasswordFrame extends JFrame {
    UserLoginManager userLoginManager = new UserLoginManager();
    public ForgetPasswordFrame(){
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
        title2.setText("Forget Password");
        title2.setBounds(550, 100, 300, 40);  // Centered

        title2.setFont(new Font("Arial", Font.BOLD, 24));


        JLabel email;
        JLabel security;
        JLabel newPassword;

        email=new JLabel("Enter your email ");
        email.setFont(new Font("Tahoma", Font.PLAIN, 20));
        security= new JLabel("What is the name of your pet");
        security.setFont(new Font("Tahoma", Font.PLAIN, 20));
        newPassword = new JLabel("Enter new password");
        newPassword.setFont(new Font("Tahoma", Font.PLAIN, 20));
        email.setBounds(350, 160, 180, 40);
        security.setBounds(350, 230, 280, 40);
        newPassword.setBounds(350, 300, 180, 40);


        JTextField emailField;
        JTextField securityField;
        JTextField newPasswordField;

        emailField= new JTextField();
        securityField = new JTextField();
        newPasswordField = new JTextField();
        emailField.setBounds(650, 170, 180, 25);
        securityField.setBounds(650, 240, 180, 25);
        newPasswordField.setBounds(650, 310, 180, 25);

        JButton enter ;
        enter = new JButton("Enter");
        enter.setBounds(650, 360, 180, 30);

        this.setSize(1300, 900);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(null);
        this.setVisible(true);
        this.add(title2);
        this.add(email);
        this.add(security);
        this.add(emailField);
        this.add(securityField);
        this.add(newPassword);
        this.add(newPasswordField);
        this.add(enter);

        JButton backButton = new JButton("Back");
        backButton.setBounds(10, 40, 70, 25);
        this.add(backButton);

        backButton.addActionListener(e -> {
            new UserLogin();
            dispose();
        });

        enter.addActionListener(e -> {
            String emailFieldText = emailField.getText();
            String password2 = newPasswordField.getText();
            String securityAns = securityField.getText();

            String forget = userLoginManager.forgetPassword(emailFieldText,password2,securityAns);

            if(forget.equals("Password reset successful!")){
                JOptionPane.showMessageDialog(null,"Password reset successful!");
                new LoginFrame();
                dispose();

            }
            else if(forget.equals("Incorrect security answer.")){
                JOptionPane.showMessageDialog(null,"Incorrect security answer.");
            }
            else {
                JOptionPane.showMessageDialog(null,"Username not found.");
            }

        });
    }

    public static void main(String[] args) {
        new ForgetPasswordFrame();
    }
}
