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

        JLabel subtitle = new JLabel("Student Portal");
        subtitle.setFont(new Font("Arial", Font.BOLD, 28));
        subtitle.setForeground(new Color(116, 202, 74));
        subtitle.setBounds(0, 180, 1300, 40);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);

        RoundedButton signUpButton = new RoundedButton("Sign Up", 30, 30);
        signUpButton.setBounds(450, 280, 200, 50);
        signUpButton.setBackground(new Color(116, 202, 74));
        signUpButton.setFont(new Font("Arial", Font.BOLD, 20));
        signUpButton.setForeground(Color.WHITE);
        signUpButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        RoundedButton loginButton = new RoundedButton("Login", 30, 30);
        loginButton.setBounds(650, 280, 200, 50);
        loginButton.setFont(new Font("Arial", Font.BOLD, 20));
        loginButton.setBackground(new Color(116, 202, 74));
        loginButton.setForeground(Color.WHITE);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        RoundedButton backButton = new RoundedButton("← Back", 20, 20);
        backButton.setBounds(15, 145, 100, 38);
        backButton.setFont(new Font("Arial", Font.BOLD, 15));
        backButton.setBackground(new Color(80, 80, 80));
        backButton.setForeground(Color.WHITE);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        setSize(1300, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setTitle("Sign Up");
        getContentPane().setBackground(new Color(245, 245, 245));

        add(green);
        add(headingPanel);
        add(backButton);
        add(subtitle);
        add(signUpButton);
        add(loginButton);
        add(title);

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
