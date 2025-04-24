package PersonModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import AccountModel.AccountModel;
import AccountModel.TransactionService;
import AccountModel.UserRegistrationCSVExporter;
import AccountModel.UserSession;

public class IncomeDialog extends JDialog {
    private JTextField amountField;
    private JTextField timeField; // Use JTextField for now, could be replaced with JDatePicker
    private JPasswordField passwordField;
    private JButton confirmButton;
    private JButton cancelButton;
    private String currentUsername;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    public IncomeDialog(Dialog owner) {
        super(owner, "Add Income", true); // Modal dialog
        this.currentUsername = UserSession.getCurrentUsername(); // Get username from session

        if (this.currentUsername == null) {
            JOptionPane.showMessageDialog(owner, "Error: Not logged in.", "Error", JOptionPane.ERROR_MESSAGE);
            // Dispose immediately if not logged in
            SwingUtilities.invokeLater(this::dispose);
            return;
        }

        initComponents();
        layoutComponents();
        addListeners();

        pack(); // Adjust size to fit components
        setMinimumSize(new Dimension(400, 250)); // Set a minimum size
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        amountField = new JTextField(15);
        // Pre-fill time field with current time in the correct format
        timeField = new JTextField(DATE_FORMAT.format(new Date()), 15);
        passwordField = new JPasswordField(15);
        confirmButton = new JButton("Confirm Income");
        cancelButton = new JButton("Cancel");

        // Style buttons (optional)
        confirmButton.setBackground(new Color(30, 60, 120));
        confirmButton.setForeground(Color.WHITE);
        cancelButton.setBackground(new Color(200, 200, 200));
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 245, 245));

        // Title
        JLabel titleLabel = new JLabel("Add Income Record");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(titleLabel, BorderLayout.NORTH);

        // Form Panel
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Amount
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Amount (¥):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(amountField, gbc);

        // Time
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; // Reset weightx
        panel.add(new JLabel("Time (yyyy/MM/dd HH:mm):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(timeField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(passwordField, gbc);

        add(panel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addListeners() {
        confirmButton.addActionListener(e -> processIncome());
        cancelButton.addActionListener(e -> dispose());
        // Add ActionListener to passwordField to trigger on Enter key
        passwordField.addActionListener(e -> processIncome());
    }

    private void processIncome() {
        String amountText = amountField.getText();
        String timeText = timeField.getText();
        String password = new String(passwordField.getPassword());

        // Input Validation
        if (amountText.isEmpty() || timeText.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be positive.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount format.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate date format
        try {
            DATE_FORMAT.setLenient(false); // Make date parsing strict
            DATE_FORMAT.parse(timeText.trim()); // 这行会抛出 ParseException
        } catch (ParseException ex) { // 你已经捕获了 ParseException
            JOptionPane.showMessageDialog(this, "Invalid time format. Use yyyy/MM/dd HH:mm", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }


        // --- Account Verification and Update ---
        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        AccountModel currentUserAccount = null;
        boolean passwordCorrect = false;

        for (AccountModel account : accounts) {
            if (account.getUsername().equals(currentUsername)) {
                currentUserAccount = account;
                if (account.getPassword().equals(password)) {
                    passwordCorrect = true;
                }
                break; // Found the user, no need to continue loop
            }
        }

        if (currentUserAccount == null) {
            JOptionPane.showMessageDialog(this, "Current user account not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!passwordCorrect) {
            JOptionPane.showMessageDialog(this, "Incorrect password.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            passwordField.setText(""); // Clear password field
            return;
        }

        // --- Add Transaction and Update Balance ---
        // Add transaction first
        boolean transactionAdded = TransactionService.addTransaction(
                currentUsername,
                "Income",
                amount,
                timeText.trim(), // Use the validated time string
                "I", // Merchant placeholder for Income
                "I"  // Type placeholder for Income
, password, password, password, password, password, password, password
        );

        if (transactionAdded) {
            // Update balance in the AccountModel object
            currentUserAccount.setBalance(currentUserAccount.getBalance() + amount);

            // Save the updated list of accounts back to accounts.csv
            boolean saved = UserRegistrationCSVExporter.saveToCSV(accounts, false); // Overwrite mode

            if (saved) {
                JOptionPane.showMessageDialog(this, "Income of ¥" + String.format("%.2f", amount) + " added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                // Update the account in the current session as well
                UserSession.setCurrentAccount(currentUserAccount);
                dispose(); // Close dialog on success
            } else {
                // This case is less likely if addTransaction succeeded, but handle file write errors
                JOptionPane.showMessageDialog(this, "Income recorded, but failed to update account balance file.", "Warning", JOptionPane.WARNING_MESSAGE);
                 // Consider how to handle this inconsistency - maybe attempt rollback?
            }
        } else {
            // TransactionService.addTransaction would have shown an error message already
             System.err.println("Failed to add transaction via TransactionService.");
        }
         passwordField.setText(""); // Clear password after attempt
    }
}