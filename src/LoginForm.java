import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.sql.*;

public class LoginForm extends JDialog {
    private JPanel loginPanel;
    private JTextField tfEmail;
    private JPasswordField pfPassword;
    private JButton btnLogin;
    private JButton btnRegister;
    private User user;

    public LoginForm(JFrame parent) {
        super(parent);
        setTitle("Login");
        setContentPane(loginPanel);
        setMinimumSize(new Dimension(450, 474));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = tfEmail.getText();
                String password = String.valueOf(pfPassword.getPassword());

                user = getAuthenticatedUser(email, password);
                if (user != null) {
                    dispose();
                    showIndexForm();
                } else {
                    JOptionPane.showMessageDialog(LoginForm.this,
                            "Email or password invalid!",
                            "Try again.",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                showRegisterForm();
            }
        });

        setVisible(true);
    }

    private User getAuthenticatedUser(String email, String password) {
        User user = null;

        final String DB_URL = "jdbc:mysql://192.168.1.5:3306/norm_db";
        final String USERNAME = "root";
        final String PASSWORD = "pass";

        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);

            String sql = "SELECT * FROM test_user2 WHERE email=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, email);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String storedPasswordHash = resultSet.getString("password");
                if (verifyPassword(password, storedPasswordHash)) {
                    user = new User();
                    user.name = resultSet.getString("name");
                    user.email = resultSet.getString("email");
                    user.phone = resultSet.getString("phone");
                    user.address = resultSet.getString("address");
                    user.password = storedPasswordHash;
                }
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    private boolean verifyPassword(String password, String storedPasswordHash) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString().equals(storedPasswordHash);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showIndexForm() {
        IndexForm indexForm = new IndexForm(null);
    }

    private void showRegisterForm() {
        RegistrationForm registerForm = new RegistrationForm(null);
    }

    public static void main(String[] args) {
        LoginForm loginForm = new LoginForm(null);
        User user = loginForm.user;
        if (user != null) {
            System.out.println("Successful Authentication of: " + user.name);
            System.out.println("           Email: " + user.email);
            System.out.println("           Phone: " + user.phone);
            System.out.println("           Address: " + user.address);
        } else {
            System.out.println("Authentication canceled");
        }
    }
}
