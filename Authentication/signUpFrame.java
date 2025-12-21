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
        title2.setText("Signup");
        title2.setBounds(600, 80, 100, 40);
        title2.setFont(new Font("Arial", Font.BOLD, 24));
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
        int y = 170;
        for (JLabel label : labels) {
            label.setFont(new Font("Times New Roman", Font.ITALIC, 18));
            label.setBounds(350, y, 250, 20);
            add(label);
            y += 40;
        }

        JButton backButton = new JButton("Back");
        backButton.setBounds(10, 40, 70, 25);
        this.add(backButton);

        backButton.addActionListener(e -> {
            new Myframe();
            dispose();
        });

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
        y = 170;
        for (JComponent field : fields) {
            field.setBounds(650, y, 160, 20);
            add(field);
            y += 40;
        }

        JButton enter = new JButton("Enter");
        enter.setBounds(600, 560, 140, 25);
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
