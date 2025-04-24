package View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import PersonModel.UserSessionModel; // 导入 TransactionController
import TransactionController.TransactionController; // 导入 TransactionModel
import TransactionModel.TransactionModel;

/**
 * Dialog for displaying the transaction history of the logged-in user in a table.
 */
public class TransactionHistoryView extends JDialog {

    public TransactionHistoryView(Dialog owner) {
        super(owner, "Transaction History", true);
        setSize(720, 460);
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 245, 245));

        String loggedInUsername = UserSessionModel.getCurrentUsername();
        if (loggedInUsername == null) {
            JOptionPane.showMessageDialog(this, "Please log in first", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        // Title
        JLabel titleLabel = new JLabel("Your Transaction History", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(50, 50, 50));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Table setup
        DefaultTableModel tableModel = new DefaultTableModel(
                new Object[]{"Operation", "Amount (¥)", "Time", "Merchant/Payee", "Type"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 1: return Double.class; // Amount
                    default: return String.class;
                }
            }
        };
        JTable transactionTable = new JTable(tableModel);
        transactionTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        transactionTable.setRowHeight(30);
        transactionTable.setShowGrid(false);
        transactionTable.setBackground(Color.WHITE);
        transactionTable.setForeground(Color.BLACK);

        // Center align table cells
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        renderer.setBackground(Color.WHITE);
        for (int i = 0; i < transactionTable.getColumnCount(); i++) {
            transactionTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // Table header styling
        JTableHeader header = transactionTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(scrollPane, BorderLayout.CENTER);

        // Load transaction data
        loadTransactionData(loggedInUsername, tableModel);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        buttonPanel.setBackground(new Color(245, 245, 245));
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeButton.setBackground(new Color(100, 149, 237));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    /**
     * Loads transaction data for the specified user and populates the table model.
     *
     * @param username The username to fetch transactions for.
     * @param model    The DefaultTableModel to populate with transaction data.
     */
    private void loadTransactionData(String username, DefaultTableModel model) {
        model.setRowCount(0); // Clear existing rows

        // Use TransactionController to get transaction data
        List<TransactionModel> transactions = TransactionController.readTransactions(username);

        if (transactions.isEmpty()) {
            System.out.println("No transactions found for user: " + username);
        } else {
            // Populate the table model with data from TransactionModel objects
            for (TransactionModel tx : transactions) {
                model.addRow(new Object[]{
                        tx.getOperation(),    // Column 0: Operation (Income/Expense)
                        tx.getAmount(),       // Column 1: Amount (Double)
                        tx.getTimestamp(),    // Column 2: Time (String)
                        tx.getMerchant(),     // Column 3: Merchant/Payee
                        tx.getType()          // Column 4: Type
                });
            }
        }
    }
}