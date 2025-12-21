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

        JButton admin;
        JButton user;

        admin = new JButton("Admin");
        user = new JButton("User");

        admin.setFont(new Font ("Serif",Font.PLAIN,20));
        admin.setBackground(new Color(237, 235, 235));
        admin.setBounds(665, 300, 150, 40);
        user.setBackground(new Color(237, 235, 235));
        user.setFont(new Font ("Serif",Font.PLAIN,20));
        user.setBounds(485, 300, 150, 40);

        this.setSize(1300, 900);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(null);
        this.setVisible(true);
        this.setTitle("Welcome to College Admission Portal ");
        this.add(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(user);
        this.add(admin);
        this.add(green);
        this.getContentPane().setBackground(Color.LIGHT_GRAY);


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
