package AccountModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton; // Keep existing imports
import javax.swing.JDialog; // Keep existing imports
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import AccountModel.TransactionService.TransactionData;

public class TransactionHistory extends JDialog {

    public TransactionHistory(Dialog owner) {
        super(owner, "Transaction History", true);
        setSize(720, 460);
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 245, 245));

        String loggedInUsername = UserSession.getCurrentUsername();
        if (loggedInUsername == null) {
            JOptionPane.showMessageDialog(this, "Please log in first", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        // Title
        JLabel titleLabel = new JLabel("Your Transaction History");
        // ... (Title styling remains the same) ...
        add(titleLabel, BorderLayout.NORTH);

        // Table setup with NEW columns
        DefaultTableModel tableModel = new DefaultTableModel(
                // user, operation, amount, time, merchant, type --> Display 5 columns
                new Object[]{"Operation", "Amount (¥)", "Time", "Merchant/Payee", "Type"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Still non-editable
            }
             // Optional: Define column classes for sorting if needed
             @Override
             public Class<?> getColumnClass(int columnIndex) {
                 switch (columnIndex) {
                     case 1: return Double.class; // Amount
                     // case 2: return Date.class; // If time was parsed to Date
                     default: return String.class;
                 }
             }
        };
        JTable transactionTable = new JTable(tableModel);
        // ... (Table styling remains the same) ...

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(scrollPane, BorderLayout.CENTER);

        // Load data using the NEW service method
        loadTransactionData(loggedInUsername, tableModel); // Pass model to populate

        // Button panel remains the same
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        // ... (Button panel setup remains the same) ...
        JButton closeButton = new JButton("Close");
        // ... (Close button styling and listener remain the same) ...
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    // Modified method to load data using TransactionService
    private void loadTransactionData(String username, DefaultTableModel model) {
        model.setRowCount(0); // Clear existing rows

        // Use the service to get structured data
        List<TransactionData> transactions = TransactionService.readTransactions(username);

        if (transactions.isEmpty()) {
            // Optional: Show a message if history is empty
            // JOptionPane.showMessageDialog(this, "No transaction history found.", "Info", JOptionPane.INFORMATION_MESSAGE);
             System.out.println("No transactions found for user: " + username);
        } else {
            // Populate the table model with data from TransactionData objects
            for (TransactionData tx : transactions) {
                model.addRow(new Object[]{
                        tx.getOperation(),    // Column 0: Operation (Income/Expense)
                        tx.getAmount(),       // Column 1: Amount (Double)
                        tx.getTime(),         // Column 2: Time (String)
                        tx.getMerchant(),     // Column 3: Merchant/Payee
                        tx.getType()          // Column 4: Type
                });
            }
        }
    }

    // Remove the old loadTransactions(String username) method that read CSV directly.
    // private List<String[]> loadTransactions(String username) { ... } // DELETE THIS
}