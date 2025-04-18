package Person;

import Model.UserSession;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TransactionHistory extends JDialog {

    public TransactionHistory(Dialog owner) {
        super(owner, "Transaction History", true);
        setSize(600, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        String loggedInUsername = UserSession.getCurrentUsername();
        if (loggedInUsername == null) {
            JOptionPane.showMessageDialog(this, "Please log in first", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        // Create a DefaultTableModel that makes the table non-editable
        DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Operation Performed", "Amount", "Payment Time", "Merchant Name"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        JTable transactionTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(transactionTable);

        List<String[]> transactions = loadTransactions(loggedInUsername);
        for (String[] transaction : transactions) {
            tableModel.addRow(transaction);
        }

        add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> {
            setVisible(false);
            SwingUtilities.invokeLater(() -> {
                dispose();
            });
        });
        add(closeButton, BorderLayout.SOUTH);

        setVisible(true);
    }

    private List<String[]> loadTransactions(String username) {
        List<String[]> userTransactions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("transactions.csv"))) {
            String line;
            br.readLine(); // Skip the header line
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 5 && data[0].trim().equals(username.trim())) {
                    userTransactions.add(new String[]{data[1].trim(), data[2].trim(), data[3].trim(), data[4].trim()});
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to read transaction history file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return userTransactions;
    }
}