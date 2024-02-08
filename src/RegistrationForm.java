import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RegistrationForm extends JDialog{
    private JPanel registerPanel;
    private JTextField tfName;
    private JTextField tfEmail;
    private JTextField tfPhone;
    private JTextField tfAddress;
    private JPasswordField pfPassword;
    private JPasswordField pfConfirmPassword;
    private JButton registerButton;
    private JButton btnLogin;
    private User user;

    public RegistrationForm(JFrame parent){
        setTitle("Create new account");
        setContentPane(registerPanel);
        setMinimumSize(new Dimension(450, 474));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                showLoginForm();
            }
        });
        setVisible(true);
    }

    private String encryptPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            BigInteger hashInt = new BigInteger(1, hashBytes);
            StringBuilder hashedPassword = new StringBuilder(hashInt.toString(16));
            // Ensure the hashed password has 64 characters (SHA-256 produces a 256-bit hash)
            while (hashedPassword.length() < 64) {
                hashedPassword.insert(0, "0");
            }
            return hashedPassword.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            // Handle the NoSuchAlgorithmException appropriately
            return null;
        }
    }

    private void registerUser() {
        String name = tfName.getText();
        String email = tfEmail.getText();
        String phone = tfPhone.getText();
        String address = tfAddress.getText();
        String password = String.valueOf(pfPassword.getPassword());
        String confirmPassword = String.valueOf(pfConfirmPassword.getPassword());

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || password.isEmpty()){
            JOptionPane.showMessageDialog(this,
                    "Please enter all fields",
                    "Try again",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!password.equals(confirmPassword)){
            JOptionPane.showMessageDialog(this,
                    "Confirm Password do not match",
                    "Try again",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this,
                    "Invalid email format",
                    "Try again",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String encryptedPassword = encryptPassword(password);

        if (encryptedPassword == null) {
            JOptionPane.showMessageDialog(this,
                    "Failed to encrypt password",
                    "Try again",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (isEmailExists(email)) {
            JOptionPane.showMessageDialog(this,
                    "Email already exists",
                    "Try again",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = addUserToDatabase(name, email, phone, address, encryptedPassword);
        if (user != null){
            dispose();
            showLoginForm(); // Open the login form after successful registration
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to register new user",
                    "Try again",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isValidEmail(String email) {
        // Simple email validation
        return email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    }

    private boolean isEmailExists(String email) {
        final String DB_URL = "jdbc:mysql://192.168.1.5:3306/norm_db";
        final String USERNAME = "root";
        final String PASSWORD = "pass";

        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT * FROM test_user2 WHERE email=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next(); // Returns true if email exists
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private User addUserToDatabase(String name, String email, String phone, String address, String password){
        User user = null;
        final String DB_URL = "jdbc:mysql://192.168.1.5:3306/norm_db";
        final String USERNAME = "root";
        final String PASSWORD = "pass";
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = "INSERT INTO test_user2 (name, email, phone, address, password) VALUES (?,?,?,?,?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, phone);
            preparedStatement.setString(4, address);
            preparedStatement.setString(5, password);

            int addedRows = preparedStatement.executeUpdate();
            if (addedRows > 0) {
                user = new User();
                user.name = name;
                user.email = email;
                user.phone = phone;
                user.address = address;
                user.password = password;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public void showLoginForm(){
        LoginForm loginForm = new LoginForm(null);
    }

    public static void main(String[] args) {
        RegistrationForm myForm = new RegistrationForm(null);
        User user = myForm.user;
        if (user != null){
            System.out.println("Successfully registration of: " + user.name);
        } else {
            System.out.println("Registration cancelled");
        }
    }
}
