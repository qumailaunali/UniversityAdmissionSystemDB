package Authentication;

import javax.swing.*;
import java.awt.*;

public class Myframe extends JFrame {
    public Myframe() {
        JLabel title = new JLabel("Colaraz");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setBounds(5, 0, 400, 30);

        JPanel green = new JPanel();
        green.setBackground(new Color(116, 202, 74));
        green.setBounds(0, 0, 1300, 35);
        green.setLayout(null);
        green.add(title);

        JButton signUpButton = new JButton("Sign up");
        signUpButton.setBounds(485, 300, 150, 40);
        signUpButton.setBackground(new Color(237, 235, 235));
        signUpButton.setFont(new Font("Serif", Font.PLAIN, 20));

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(665, 300, 150, 40);
        loginButton.setFont(new Font("Serif", Font.PLAIN, 20));
        loginButton.setBackground(new Color(237, 235, 235));

        JButton backButton = new JButton("Back");
        backButton.setBounds(10, 40, 70, 25);

        setSize(1300, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setTitle("Sign Up");
        getContentPane().setBackground(Color.LIGHT_GRAY);

        add(signUpButton);
        add(loginButton);
        add(backButton);
        add(title);
        add(green);

        backButton.addActionListener(e -> {
            new MainDashboard();
            dispose();
        });

        signUpButton.addActionListener(e -> {
            dispose();
            new signUpFrame();
        });

        loginButton.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        setVisible(true);
    }
}
