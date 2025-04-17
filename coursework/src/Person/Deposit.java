package Person; // Assuming TransactionAnalyzer is also in this package

import model.AccountModel;
import model.UserRegistrationCSVExporter;
import model.Transaction; // Needed for abnormal transaction detection
import model.CsvDataManager; // Needed for abnormal transaction detection
import UI.AlertPanel; // Needed for abnormal transaction detection

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors; // Needed for abnormal transaction detection

// Assuming TransactionAnalyzer class exists in the Person package
// If it's elsewhere, adjust the import accordingly.
// import Person.TransactionAnalyzer;

public class Deposit extends JDialog {
    private JTextField amountField;
    private JPasswordField passwordField;
    private JButton confirmButton, cancelButton;
    private String currentUsername;

    public Deposit(Dialog owner, String username) { // Use Dialog as owner type
        super(owner, "Deposit", true); // English Title, modal dialog
        this.currentUsername = username; // Store current username

        setSize(300, 200);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        JLabel amountLabel = new JLabel("Deposit Amount:"); // English label
        amountField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:"); // English label
        passwordField = new JPasswordField();

        confirmButton = new JButton("Confirm"); // English button
        cancelButton = new JButton("Cancel");   // English button

        confirmButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String amountText = amountField.getText();
                char[] passwordChars = passwordField.getPassword();
                String enteredPassword = new String(passwordChars);

                // Basic validation
                if (amountText.isEmpty() || enteredPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(Deposit.this,
                            "Amount and password cannot be empty.", // English message
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    double amount = Double.parseDouble(amountText);

                    // Ensure deposit amount is positive
                    if (amount <= 0) {
                        JOptionPane.showMessageDialog(Deposit.this,
                                "Deposit amount must be positive.", // English message
                                "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
                    AccountModel currentUserAccount = null;
                    String storedPassword = null;

                    // Find the current user's account
                    for (AccountModel account : accounts) {
                        if (account.getUsername().equals(currentUsername)) {
                            storedPassword = account.getPassword();
                            currentUserAccount = account;
                            break;
                        }
                    }

                    // Check if account was found
                    if (currentUserAccount == null || storedPassword == null) {
                        JOptionPane.showMessageDialog(Deposit.this,
                                "Account information not found for this username.", // English message
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return; // Exit if account not found
                    }

                    // Verify password
                    if (enteredPassword.equals(storedPassword)) {
                        // Record the deposit transaction
                        recordDeposit(currentUsername, amount);

                        // Update balance in the account object
                        currentUserAccount.setBalance(currentUserAccount.getBalance() + amount);

                        // Save the updated account list back to CSV
                        UserRegistrationCSVExporter.saveToCSV(accounts, false);

                        // Show success message
                        JOptionPane.showMessageDialog(Deposit.this,
                                "Successfully deposited " + amount + " RMB, account balance updated.", // English message
                                "Success", JOptionPane.INFORMATION_MESSAGE);

                        // Close the deposit dialog
                        dispose();

                        // --- Abnormal Transaction Detection ---
                        // This part is retained from Branch 1
                        try {
                            CsvDataManager csvManager = new CsvDataManager();
                            // Read all transactions and filter for the current user
                            List<Transaction> transactions = csvManager.readTransactions().stream()
                                    .filter(t -> t.getUsername().equals(currentUsername))
                                    .collect(Collectors.toList());

                            TransactionAnalyzer analyzer = new TransactionAnalyzer(); // Assumes this class exists
                            List<Transaction> abnormal = analyzer.detectAbnormal(transactions); // Assumes this method exists

                            // If abnormal transactions are detected, show the alert panel
                            if (abnormal != null && !abnormal.isEmpty()) {
                                // Assuming AlertPanel constructor takes list and optionally a parent window
                                // Passing 'null' as parent might be okay, or pass 'owner' if needed.
                                new AlertPanel(abnormal, null).setVisible(true); // Show alert
                            }
                        } catch (Exception ex) {
                            // Log error during detection but don't stop the user flow
                            System.err.println("Error during abnormal transaction detection: " + ex.getMessage());
                            ex.printStackTrace();
                            // Optionally show a less intrusive warning to the user or log to a file
                            // JOptionPane.showMessageDialog(null, "Could not perform transaction analysis.", "Warning", JOptionPane.WARNING_MESSAGE);
                        }
                        // --- End of Abnormal Transaction Detection ---

                    } else {
                        // Incorrect password
                        JOptionPane.showMessageDialog(Deposit.this,
                                "Incorrect password.", // English message
                                "Authentication Error", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(Deposit.this,
                            "Please enter a valid numerical amount.", // English message
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                } catch (IOException ioEx) {
                    // Handle errors reading/writing CSV files
                    JOptionPane.showMessageDialog(Deposit.this,
                            "Error accessing account data: " + ioEx.getMessage(), // English message
                            "File Error", JOptionPane.ERROR_MESSAGE);
                    ioEx.printStackTrace(); // Log the full error
                } finally {
                    // Clear the password field regardless of outcome
                    passwordField.setText("");
                    // Consider clearing amountField as well: amountField.setText("");
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close the dialog
            }
        });

        // Add components to the panel
        panel.add(amountLabel);
        panel.add(amountField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(confirmButton);
        panel.add(cancelButton);

        // Add panel to the dialog window
        add(panel);
    }


    /**
     * Records a deposit transaction to the transactions.csv file.
     * Uses the format: username,Deposit,amount,yyyy/MM/dd HH:mm,ATM
     *
     * @param username The username making the deposit.
     * @param amount   The amount being deposited.
     */
    private void recordDeposit(String username, double amount) {
        String filePath = "transactions.csv"; // Path to the transaction log file
        // Use try-with-resources to ensure the writer is closed automatically
        try (FileWriter fw = new FileWriter(filePath, true)) { // true for appending to the file
            Date now = new Date();
            // Define the date and time format (English locale friendly)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String formattedDateTime = sdf.format(now);

            // Write the transaction record in CSV format
            // Format: Username,OperationType,Amount,Timestamp,Merchant/Source
            fw.write(username + ",Deposit," + amount + "," + formattedDateTime + ",ATM\n");

        } catch (IOException e) {
            // Show error message if transaction logging fails
            JOptionPane.showMessageDialog(this, // 'this' refers to the JDialog
                    "Error recording transaction: " + e.getMessage(), // English message
                    "Transaction Log Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Print detailed error for debugging
        }
    }

    // --- Main method for testing (Optional) ---
    /*
    public static void main(String[] args) {
        // Example of how to launch the dialog (requires a parent Frame or Dialog)
        JFrame frame = new JFrame(); // Dummy parent frame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(100, 100);
        frame.setLocationRelativeTo(null);

        // You would typically get the username from a login session
        String loggedInUsername = "testuser";

        // Create and show the Deposit dialog
        Deposit depositDialog = new Deposit(null, loggedInUsername); // Pass null or a real parent Dialog/Frame
        depositDialog.setVisible(true);

        // The program will wait here until the dialog is closed because it's modal
        System.out.println("Deposit dialog closed.");
        System.exit(0); // Exit after testing
    }
    */
}