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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import AccountModel.AccountModel;
import AccountModel.PersonalAccount;
import AccountModel.TransactionService;
import AccountModel.UserRegistrationCSVExporter;
import AccountModel.UserSession;

public class ExpenseDialog extends JDialog {
    private JTextField amountField;
    private JTextField timeField;
    private JTextField merchantField;
    private JComboBox<String> typeComboBox;
    private JPasswordField passwordField;
    private JButton confirmButton;
    private JButton cancelButton;
    private String currentUsername;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    private static final String[] EXPENSE_TYPES = {
        "(Select Type)", // Default, indicates unclassified if chosen
        "Food", "Shopping", "Traffic", "Entertainment",
        "Education", "Transfer", "Others"
        // Note: 'others' category, maybe rename to 'Miscellaneous' or keep as 'Others'
    };
    private static final String UNCLASSIFIED_TYPE_CODE = "u";
    private static final String INCOME_PLACEHOLDER = "I"; // Placeholder for merchant/type in Income


    public ExpenseDialog(Dialog owner) {
        super(owner, "Add Expense", true);
        this.currentUsername = UserSession.getCurrentUsername();

        if (this.currentUsername == null) {
            JOptionPane.showMessageDialog(owner, "Error: Not logged in.", "Error", JOptionPane.ERROR_MESSAGE);
             SwingUtilities.invokeLater(this::dispose);
            return;
        }

        initComponents();
        layoutComponents();
        addListeners();

        pack();
        setMinimumSize(new Dimension(450, 350)); // Slightly larger
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

     private void initComponents() {
        amountField = new JTextField(15);
        timeField = new JTextField(DATE_FORMAT.format(new Date()), 15);
        merchantField = new JTextField(15);
        typeComboBox = new JComboBox<>(EXPENSE_TYPES);
        passwordField = new JPasswordField(15);
        confirmButton = new JButton("Confirm Expense");
        cancelButton = new JButton("Cancel");

        // Style buttons
        confirmButton.setBackground(new Color(220, 53, 69)); // Reddish for expense
        confirmButton.setForeground(Color.WHITE);
        cancelButton.setBackground(new Color(200, 200, 200));
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 245, 245));

        // Title
        JLabel titleLabel = new JLabel("Add Expense Record");
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
        gbc.anchor = GridBagConstraints.WEST;


        // Amount
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        panel.add(new JLabel("Amount (¥):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(amountField, gbc);

        // Time
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Time (yyyy/MM/dd HH:mm):"), gbc);
        gbc.gridx = 1;
        panel.add(timeField, gbc);

        // Merchant
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Merchant/Payee:"), gbc);
        gbc.gridx = 1;
        panel.add(merchantField, gbc);

        // Type
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        panel.add(typeComboBox, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
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
        confirmButton.addActionListener(e -> processExpense());
        cancelButton.addActionListener(e -> dispose());
        passwordField.addActionListener(e -> processExpense());
    }

    private void processExpense() {
        String amountText = amountField.getText();
        String timeText = timeField.getText();
        String merchantText = merchantField.getText();
        String selectedType = (String) typeComboBox.getSelectedItem();
        String password = new String(passwordField.getPassword());

        // Input Validation
        if (amountText.isEmpty() || timeText.isEmpty() || merchantText.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Amount, Time, Merchant, and Password must be filled.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Merchant should not be the income placeholder 'I'
         if (INCOME_PLACEHOLDER.equalsIgnoreCase(merchantText.trim())) {
             JOptionPane.showMessageDialog(this, "Invalid merchant name.", "Input Error", JOptionPane.ERROR_MESSAGE);
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
            DATE_FORMAT.setLenient(false);
            DATE_FORMAT.parse(timeText.trim());
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid time format. Use yyyy/MM/dd HH:mm", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Determine the type to record
        String typeToRecord = UNCLASSIFIED_TYPE_CODE; // Default to unclassified
        if (selectedType != null && !selectedType.equals(EXPENSE_TYPES[0])) { // Check if not the default "(Select Type)"
            typeToRecord = selectedType;
        }
         // Type should not be the income placeholder 'I'
         if (INCOME_PLACEHOLDER.equalsIgnoreCase(typeToRecord)) {
              JOptionPane.showMessageDialog(this, "Invalid expense type selected.", "Input Error", JOptionPane.ERROR_MESSAGE);
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
                break;
            }
        }

        if (currentUserAccount == null) {
            JOptionPane.showMessageDialog(this, "Current user account not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!passwordCorrect) {
            JOptionPane.showMessageDialog(this, "Incorrect password.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
             passwordField.setText("");
            return;
        }

        // Check balance (only for PersonalAccount, maybe redundant if Admin can't use this dialog)
        if (currentUserAccount instanceof PersonalAccount) {
            if (currentUserAccount.getBalance() < amount) {
                JOptionPane.showMessageDialog(this, "Insufficient balance for this expense.", "Balance Error", JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
                return;
            }
        } else {
             JOptionPane.showMessageDialog(this, "Expense operations are not supported for this account type.", "Type Error", JOptionPane.ERROR_MESSAGE);
             return; // Prevent non-personal accounts from proceeding
        }


        // --- Add Transaction and Update Balance ---
        boolean transactionAdded = TransactionService.addTransaction(
                currentUsername,
                "Expense",
                amount,
                timeText.trim(),
                merchantText.trim(),
                typeToRecord, typeToRecord, typeToRecord, typeToRecord, typeToRecord, typeToRecord, typeToRecord, typeToRecord
        );

        if (transactionAdded) {
            // Update balance
            currentUserAccount.setBalance(currentUserAccount.getBalance() - amount);

            // Save updated accounts list
            boolean saved = UserRegistrationCSVExporter.saveToCSV(accounts, false);

            if (saved) {
                JOptionPane.showMessageDialog(this, "Expense of ¥" + String.format("%.2f", amount) + " added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                // Update session account
                UserSession.setCurrentAccount(currentUserAccount);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Expense recorded, but failed to update account balance file.", "Warning", JOptionPane.WARNING_MESSAGE);
                 // Handle inconsistency
            }
        } else {
            // TransactionService error message already shown
             System.err.println("Failed to add expense via TransactionService.");
        }
        passwordField.setText("");
    }
}