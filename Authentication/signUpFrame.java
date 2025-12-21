package Authentication;



import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;

public class signUpFrame extends JFrame {
    UserSignup signup = new UserSignup();

    signUpFrame() {
        this.setSize(1300, 900);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(null);
        this.setTitle("Sign Up");
        this.setVisible(true);

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
        title2.setText("Student Registration");
        title2.setBounds(0, 160, 1300, 40);
        title2.setFont(new Font("Arial", Font.BOLD, 32));
        title2.setForeground(new Color(116, 202, 74));
        title2.setHorizontalAlignment(SwingConstants.CENTER);
        add(title2);


        JLabel firstname = new JLabel("First Name:");
        JLabel lastname = new JLabel("Last Name:");
        JLabel email = new JLabel("Email:");
        JLabel password = new JLabel("Password:");
        JLabel security = new JLabel("Name of your pet:");
        JLabel cnic = new JLabel("CNIC (XXXXX-XXXXXXX-X):");
        JLabel dob = new JLabel("Date of Birth (YYYY-MM-DD):");
        JLabel gender = new JLabel("Gender:");
        JLabel phone = new JLabel("Phone:");

        JLabel[] labels = {firstname,lastname,password, security, cnic, dob, gender, phone, email};
        int y = 230;
        for (JLabel label : labels) {
            label.setFont(new Font("Arial", Font.BOLD, 16));
            label.setForeground(new Color(80, 80, 80));
            label.setBounds(300, y, 280, 25);
            add(label);
            y += 50;
        }

        RoundedButton backButton = new RoundedButton("← Back", 20, 20);
        backButton.setBounds(15, 145, 100, 38);
        backButton.setFont(new Font("Arial", Font.BOLD, 15));
        backButton.setBackground(new Color(80, 80, 80));
        backButton.setForeground(Color.WHITE);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        this.add(backButton);

        backButton.addActionListener(e -> {
            new Myframe();
            dispose();
        });

        add(green);
        add(headingPanel);

        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField securityField = new JTextField();
        JTextField cnicField = new JTextField();
        JTextField dobField = new JTextField();
        JComboBox<Gender> genderBox = new JComboBox<>();
        genderBox.addItem(Gender.FEMALE);
        genderBox.addItem(Gender.MALE);
        genderBox.addItem(Gender.PREFER_NOT_TO_SAY);
        genderBox.addItem(Gender.OTHER);
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();


        JComponent[] fields = {firstNameField,lastNameField, passwordField, securityField, cnicField, dobField, genderBox, phoneField, emailField};
        y = 230;
        for (JComponent field : fields) {
            field.setBounds(600, y, 380, 35);
            field.setFont(new Font("Arial", Font.PLAIN, 15));
            if (field instanceof JTextField || field instanceof JPasswordField) {
                ((JTextField)field).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(180, 180, 180), 2),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
            add(field);
            y += 50;
        }

        RoundedButton enter = new RoundedButton("Sign Up", 25, 25);
        enter.setBounds(600, 700, 380, 45);
        enter.setFont(new Font("Arial", Font.BOLD, 18));
        enter.setBackground(new Color(116, 202, 74));
        enter.setForeground(Color.WHITE);
        enter.setCursor(new Cursor(Cursor.HAND_CURSOR));
        add(enter);


        /*
            below key listener method is to make sure that space is treated as character in password field
         */
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == ' ') {
                    e.consume();
                }
            }
        });



        enter.addActionListener(e -> {
            String passWord = new String(passwordField.getPassword()).trim();
            String securityAnswer = securityField.getText().trim();
            String cnicVal = cnicField.getText().trim();
            String dobText = dobField.getText().trim();
            Gender genderVal = (Gender) genderBox.getSelectedItem();
            String phoneVal = phoneField.getText().trim();
            String emailVal = emailField.getText().trim();

            if (emailVal.isEmpty() || passWord.isEmpty() || securityAnswer.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Fields cannot be empty");
                return;
            }

            LocalDate dobValue;
            try {
                dobValue = LocalDate.parse(dobText);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Invalid date format (YYYY-MM-DD)");
                return;
            }

            String result = signup.signUp (firstNameField.getText(), lastNameField.getText(), passWord, securityAnswer,
                    cnicVal, dobValue, genderVal, phoneVal, emailVal
            );

            switch (result) {
                case "Sign up successful!":
                    new LoginFrame();
                    dispose();
                    break;
                case "All fields are required.":
                    JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE);
                    break;
                case "commas":
                    JOptionPane.showMessageDialog(this, "Commas are not allowed in any of the fields.", "Error", JOptionPane.ERROR_MESSAGE);
                    break;
                case "Password must be at least 8 characters.":
                    JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE);
                    break;
                case "Password must contain at least one uppercase letter.":
                    JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE);
                    break;
                case "Password must contain at least one lowercase letter.":
                    JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE);
                    break;
                case "Password must contain at least one digit.":
                    JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE);
                    break;
                case "Password must contain at least one special character.":
                    JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE);
                    break;
                case "Email already exists.":
                    JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE);
                    break;
                case "Invalid CNIC format. Use XXXXX-XXXXXXX-X.":
                    JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE);
                    break;
                case "You must be at least 18 years old to sign up.":
                    JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE);
                    break;
                case "Invalid phone number format. Include country code.":
                    JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE);
                    break;
                case "Invalid email address.":
                    JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE);
                    break;
                case "Field can not be empty":
                    JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE);
                    break;
                case "Enter a valid pet name":
                    JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE);
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "An unexpected error occurred. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


    }

    public static void main(String[] args) {
        new signUpFrame();
    }


}
