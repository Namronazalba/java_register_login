import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class TableForm extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JScrollPane scrollPane;
    private JButton btnEdit;
    private JPanel TablePanel;
    private JButton btnDelete;
    private JButton btnIndex;
    private JScrollPane ScrollPane;

    public TableForm() {
        setTitle("Table Display");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create the table model
        model = new DefaultTableModel();

        // Set up the table with the model
        table = new JTable(model);

        // Add columns to the model
        model.addColumn("Item ID");
        model.addColumn("Item Name");
        model.addColumn("Price");
        model.addColumn("Quantity");

        // Fetch data from the database and add it to the table model
        fetchAndAddData(model);

        // Add the table to a scroll pane
        scrollPane = new JScrollPane(table);

        // Add the scroll pane to the frame
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Create buttons
        btnEdit = new JButton("Edit");
        btnDelete = new JButton("Delete");

        // Add buttons to the frame
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnIndex);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners to buttons
        btnEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editSelectedItem();
            }
        });

        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                destroySelectedItem();
            }
        });
        btnIndex.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                showIndexForm();
            }
        });
    }

    // Method to fetch data from the database and add it to the table model
    private void fetchAndAddData(DefaultTableModel model) {
        try {
            // Your database connection setup
            Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.1.5:3306/norm_db", "root", "pass");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM items");

            // Add rows to the table model
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity")
                });
            }

            // Close resources
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void showIndexForm(){
        IndexForm indexForm = new IndexForm(this);
        indexForm.setVisible(true);
    }
    // Method to edit the selected item
    private void editSelectedItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            // Retrieve data from the selected row
            int itemId = (int) table.getValueAt(selectedRow, 0);
            String itemName = (String) table.getValueAt(selectedRow, 1);
            double price = Double.parseDouble(table.getValueAt(selectedRow, 2).toString());
            int quantity = (int) table.getValueAt(selectedRow, 3);

            // Prompt user for updated data
            String updatedName = JOptionPane.showInputDialog(this, "Enter updated item name:", itemName);
            String updatedPriceStr = JOptionPane.showInputDialog(this, "Enter updated price:", price);
            String updatedQuantityStr = JOptionPane.showInputDialog(this, "Enter updated quantity:", quantity);

            try {
                // Parse updated price and quantity
                int updatedPrice;
                if (updatedPriceStr != null && !updatedPriceStr.isEmpty()) {
                    updatedPrice = Integer.parseInt(updatedPriceStr);
                } else {
                    // Set a default value if the input is null or empty
                    updatedPrice = (int) price;
                }

                int updatedQuantity;
                if (updatedQuantityStr != null && !updatedQuantityStr.isEmpty()) {
                    updatedQuantity = Integer.parseInt(updatedQuantityStr);
                } else {
                    // Set a default value if the input is null or empty
                    updatedQuantity = quantity;
                }

                // Your database connection setup
                Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.1.5:3306/norm_db", "root", "pass");
                String updateQuery = "UPDATE items SET item_name = ?, price = ?, quantity = ? WHERE item_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(updateQuery);
                pstmt.setString(1, updatedName);
                pstmt.setInt(2, updatedPrice);
                pstmt.setInt(3, updatedQuantity);
                pstmt.setInt(4, itemId);

                // Execute the update query
                int rowsUpdated = pstmt.executeUpdate();
                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(this, "Item with ID " + itemId + " has been updated.");
                    // Update the table model with the new data
                    table.setValueAt(updatedName, selectedRow, 1);
                    table.setValueAt(updatedPrice, selectedRow, 2);
                    table.setValueAt(updatedQuantity, selectedRow, 3);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update item with ID " + itemId);
                }

                // Close resources
                pstmt.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid input for price or quantity.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item to edit.");
        }
    }

    // Method to destroy (delete) the selected item
    private void destroySelectedItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            // Retrieve item ID from the selected row
            int itemId = (int) table.getValueAt(selectedRow, 0);

            // Perform destroy operation here
            try {
                // Your database connection setup
                Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.1.5:3306/norm_db", "root", "pass");
                String deleteQuery = "DELETE FROM items WHERE item_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(deleteQuery);
                pstmt.setInt(1, itemId);

                // Execute the delete query
                int rowsDeleted = pstmt.executeUpdate();
                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(this, "Item with ID " + itemId + " has been destroyed.");
                    // Remove the row from the table model if delete is successful
                    model.removeRow(selectedRow);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to destroy item with ID " + itemId);
                }

                // Close resources
                pstmt.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item to destroy.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TableForm frame = new TableForm();
            frame.setVisible(true);
        });
    }
}
