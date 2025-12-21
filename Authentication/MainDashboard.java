package Authentication;

import javax.swing.*;
import java.awt.*;



public class MainDashboard extends JFrame {
    public MainDashboard(){
        JLabel title;
        title = new JLabel("Colaraz");
        title.setFont(new Font("Tahoma", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setBounds(5,0,400,30);

        JPanel green = new JPanel();
        green.setBackground(new Color(116, 202, 74) );
        green.setBounds(0,0,1300,35);
        green.setLayout(null);
        green.add(title);
        add(green);

        // Beautiful main heading with green background panel
        JPanel headingPanel = new JPanel();
        headingPanel.setBackground(new Color(116, 202, 74));
        headingPanel.setBounds(0, 35, 1300, 120);
        headingPanel.setLayout(null);
        
        JLabel mainHeading = new JLabel("University Admission System");
        mainHeading.setFont(new Font("Arial", Font.BOLD, 52));
        mainHeading.setForeground(Color.WHITE);
        mainHeading.setBounds(150, 20, 1000, 80);
        mainHeading.setHorizontalAlignment(SwingConstants.CENTER);
        headingPanel.add(mainHeading);

        // // Welcome subtitle
        // JLabel subtitle = new JLabel("Choose Your Portal");
        // subtitle.setFont(new Font("Arial", Font.ITALIC, 18));
        // subtitle.setForeground(new Color(240, 240, 240));
        // subtitle.setBounds(150, 60, 1000, 30);
        // subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        // headingPanel.add(subtitle);

        RoundedButton admin;
        RoundedButton user;

        admin = new RoundedButton("Admin", 30, 30);
        user = new RoundedButton("User", 30, 30);

        admin.setFont(new Font ("Arial",Font.BOLD,20));
        admin.setBackground(new Color(116, 202, 74));
        admin.setForeground(Color.WHITE);
        admin.setBounds(550, 280, 200, 50);
        admin.setFocusPainted(false);
        admin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        user.setBackground(new Color(116, 202, 74));
        user.setForeground(Color.WHITE);
        user.setFont(new Font ("Arial",Font.BOLD,20));
        user.setBounds(550, 360, 200, 50);
        user.setFocusPainted(false);
        user.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Footer text label - Beautiful styling at bottom
        JLabel footerText = new JLabel("© 2025 Developed By: Kiran Zehra, Manal Mustafa Qumail, AunAli");
        footerText.setFont(new Font("Arial", Font.BOLD, 16));
        footerText.setForeground(new Color(116, 202, 74));
        footerText.setBounds(100, 700, 1100, 50);
        footerText.setHorizontalAlignment(SwingConstants.CENTER);
        footerText.setVerticalAlignment(SwingConstants.CENTER);

        this.setSize(1300, 900);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(null);
        this.setVisible(true);
        this.setTitle("Welcome to College Admission Portal ");
        this.add(title);
        this.add(headingPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(user);
        this.add(admin);
        this.add(green);
        this.add(footerText);
        this.getContentPane().setBackground(new Color(245, 245, 245));


        user.addActionListener(e -> {
            new Myframe();
            dispose();
        });

        admin.addActionListener(e -> {
            new AdminLoginFrame();
            dispose();
        });





    }
}
