import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class IndexForm extends JDialog {

    private JTextField tfItemName;
    private JTextField tfPrice;
    private JTextField tfQuantity;
    private JButton btnSave;
    private JPanel IndexForm;
    private JButton btnClose;

    public IndexForm(JFrame parent) {
        setTitle("Index Form");
        setContentPane(IndexForm);
        setSize(400, 300);
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addItem();
                showIndexForm();
            }
        });
        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        setVisible(true);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new IndexForm(null);

        });
    }

    private void addItem() {
        String item_name = tfItemName.getText();
        int price = 0;
        int quantity = 0;

        try {
            price = Integer.parseInt(tfPrice.getText());
            quantity = Integer.parseInt(tfQuantity.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid price or quantity. Please enter valid numbers.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (item_name.isEmpty() || price == 0 || quantity == 0) {
            JOptionPane.showMessageDialog(this,
                    "Please enter all fields",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Attempt to add item to database
        Item item = addItemToDatabase(item_name, price, quantity);
        if (item != null) {
            // Show success message
            JOptionPane.showMessageDialog(this,
                    "Item added successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            // Clear input fields
            tfItemName.setText("");
            tfPrice.setText("");
            tfQuantity.setText("");

            // Dispose the dialog or hide the window
            dispose();
        } else {
            // Show error message if item couldn't be added
            JOptionPane.showMessageDialog(this,
                    "Failed to add new item",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

public void showIndexForm(){
        IndexForm indexForm = new IndexForm(null);
}
    private Item addItemToDatabase(String item_name, int price, int quantity){
        Item item = null;
        final String DB_URL = "jdbc:mysql://192.168.1.5:3306/norm_db";
        final String USERNAME = "root";
        final String PASSWORD = "pass";
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)){
            String sql = "INSERT INTO items (item_name, price, quantity) VALUES (?,?,?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, item_name);
            preparedStatement.setInt(2, price); // Use index 2 for price
            preparedStatement.setInt(3, quantity); // Use index 3 for quantity

            int addedRows = preparedStatement.executeUpdate();
            if (addedRows > 0){
                item = new Item();
                item.item_name = item_name;
                item.price = price;
                item.quantity = quantity;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return item;
    }

}
