package AccountModel;

// Use the new TransactionService and its DTO/inner class
import java.text.SimpleDateFormat;
import java.util.ArrayList; // Assuming TransactionData is the DTO used
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import AccountModel.TransactionService.TransactionData;
import UI.AccountManagementUI;
import UI.AdminPlane;
import UI.PersonalMainPlane;

/**
 * Controller handling login and registration logic for AccountManagementUI.
 * It interacts with model classes to validate credentials, manage sessions,
 * load user data (including transactions), and create new accounts.
 */
public class AccountManagementController {

    /**
     * Handles the user login attempt.
     * Validates input, checks credentials against accounts.csv, checks account status,
     * loads transactions, checks for abnormal activity, sets the session,
     * and opens the appropriate AdminUI or PersonalUI.
     *
     * @param username The entered username.
     * @param password The entered password.
     * @param ui The AccountManagementUI instance to display messages and close.
     */
    public void handleLogin(String username, String password, AccountManagementUI ui) {
        System.out.println("DEBUG: handleLogin - Attempting login for user: '" + username + "'");

        // 1. Validate Inputs
        if (AccountValidator.isEmpty(username)) {
            ui.showCustomMessage("Username cannot be empty!", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (AccountValidator.isEmpty(password)) {
            ui.showCustomMessage("Password cannot be empty!", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Read Account Data
        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        AccountModel matchedAccount = null;

        // 3. Find Matching Account
        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username) && account.getPassword().equals(password)) {
                matchedAccount = account;
                break;
            }
        }

        // 4. Process Login Result
        if (matchedAccount != null) {
            System.out.println("DEBUG: handleLogin - Account found: " + username);

            // 4a. Check Account Status
            if ("FROZEN".equalsIgnoreCase(matchedAccount.getAccountStatus())) {
                System.out.println("DEBUG: handleLogin - Account is FROZEN.");
                ui.showCustomMessage("This account is currently frozen. Please contact the administrator.", "Account Frozen", JOptionPane.WARNING_MESSAGE);
                return; // Stop login if frozen
            }
            System.out.println("DEBUG: handleLogin - Account status is ACTIVE.");

            // 4b. Load Transactions into AccountModel (using the updated method)
            loadTransactionsForAccount(matchedAccount);

            // 4c. Set User Session (AFTER loading data potentially needed by checker)
            UserSession.setCurrentAccount(matchedAccount);
            System.out.println("DEBUG: handleLogin - User session set for: " + UserSession.getCurrentUsername());


            // 4d. Check for Abnormal Transactions (using updated TransactionChecker)
            boolean abnormalDetected = TransactionChecker.hasAbnormalTransactions(matchedAccount);
            if (abnormalDetected) {
                System.out.println("DEBUG: handleLogin - Abnormal transaction activity detected.");
                // Show non-blocking warning after successful login
                 SwingUtilities.invokeLater(() -> // Show warning later to not block UI transition
                    ui.showCustomMessage(
                        "Warning: Abnormal transaction activity detected recently.\nPlease review your transaction history.",
                        "Activity Alert",
                        JOptionPane.WARNING_MESSAGE
                    )
                 );
            } else {
                 System.out.println("DEBUG: handleLogin - No abnormal transactions detected.");
            }

            

            try {
                if (matchedAccount instanceof AdminAccount) {
                    System.out.println("DEBUG: handleLogin - Opening AdminUI.");
                    // Use SwingUtilities.invokeLater if AdminUI constructor does heavy work
                    SwingUtilities.invokeLater(AdminPlane::new);
                } else if (matchedAccount instanceof PersonalAccount) {
                    System.out.println("DEBUG: handleLogin - Opening PersonalUI.");
                    SwingUtilities.invokeLater(() -> new PersonalMainPlane(username));
                }
                // Close the login window AFTER initiating the opening of the new window
                ui.closeWindow();
            } catch (Exception ex) {
                System.err.println("ERROR: handleLogin - Error opening main UI or closing login window!");
                ex.printStackTrace();
                // Show error in the login window as the main UI might have failed to open
                ui.showCustomMessage("An error occurred while opening the main application window.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // Login failed
            System.out.println("DEBUG: handleLogin - Login failed: Incorrect username or password.");
            ui.showCustomMessage("Incorrect username or password!", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
        System.out.println("DEBUG: handleLogin - Method end.");
    }

    /**
     * Handles the user registration attempt.
     * Checks for existing username, validates input, creates the appropriate
     * AccountModel subclass, and saves it to accounts.csv using append mode.
     *
     * @param username            Entered username.
     * @param password            Entered password.
     * @param phone               Entered phone number.
     * @param email               Entered email address.
     * @param gender              Selected gender.
     * @param address             Entered address.
     * @param selectedAccountType String representing the selected account type ("personal" or "Admin").
     * @param ui                  The AccountManagementUI instance.
     */
    public void handleRegister(String username, String password, String phone, String email, String gender, String address, String selectedAccountType, AccountManagementUI ui) {
        System.out.println("DEBUG: handleRegister - Attempting registration for user: '" + username + "', type: '" + selectedAccountType + "'");

        // 1. Check if username exists
         List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
         for (AccountModel account : accounts) {
             if (account.getUsername().equals(username)) {
                 ui.showCustomMessage("Username already exists! Please choose another.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                 return;
             }
         }

        // 2. Validate all required fields are non-empty
        if (AccountValidator.isEmpty(username) || AccountValidator.isEmpty(password) ||
            AccountValidator.isEmpty(phone) || AccountValidator.isEmpty(email) ||
            AccountValidator.isEmpty(gender) || AccountValidator.isEmpty(address) || // Added gender validation
            AccountValidator.isEmpty(selectedAccountType)) { // Added type validation
            ui.showCustomMessage("All fields must be filled out!", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Prepare account details
        String creationTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String accountStatus = "ACTIVE"; // Default to active upon registration
        double initialBalance = 0.0;

        AccountModel newAccount = null;
        String accountTypeString = "";

        // 4. Create appropriate AccountModel instance
        // Ensure selectedAccountType matches the values used ("personal", "Admin")
        if ("personal".equalsIgnoreCase(selectedAccountType)) {
             accountTypeString = "personal";
             newAccount = new PersonalAccount(username, password, phone, email, gender, address, creationTime, accountStatus, accountTypeString, initialBalance);
        } else if ("Admin".equalsIgnoreCase(selectedAccountType)) {
             accountTypeString = "Admin";
             newAccount = new AdminAccount(username, password, phone, email, gender, address, creationTime, accountStatus, accountTypeString, initialBalance);
        } else {
             // This case should ideally not be reachable if JComboBox has fixed values
             ui.showCustomMessage("Invalid account type selected: " + selectedAccountType, "Registration Error", JOptionPane.ERROR_MESSAGE);
             return;
        }

        // 5. Save the new account
        if (newAccount != null) {
             List<AccountModel> accountListToAdd = new ArrayList<>();
             accountListToAdd.add(newAccount);
             // Use append mode (true) for registration
             boolean saved = UserRegistrationCSVExporter.saveToCSV(accountListToAdd, true);

             if (saved) {
                System.out.println("DEBUG: handleRegister - Account saved successfully for user: " + username);
                ui.showCustomMessage("Account created successfully! You can now log in.", "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
                ui.switchToLoginPanel(); // Go back to login panel
             } else {
                 // saveToCSV should handle its own errors/logging
                 ui.showCustomMessage("Failed to save account information. Please try again or contact support.", "Registration Error", JOptionPane.ERROR_MESSAGE);
             }
        }
         System.out.println("DEBUG: handleRegister - Method end for user: " + username);
    }


    /**
     * Loads transactions for the given account from transactions.csv using TransactionService
     * and populates the account's internal transaction list.
     * Assumes account.getTransactions() returns a non-null, clearable, modifiable List,
     * and that TransactionModel has a constructor matching the new 6-field structure.
     *
     * @param account The AccountModel object to load transactions into.
     */
    private void loadTransactionsForAccount(AccountModel account) {
        // 1. Validate Account and Username
        if (account == null) {
            System.err.println("ERROR: loadTransactionsForAccount - Account object is null.");
            return;
        }
        String username = account.getUsername();
        if (AccountValidator.isEmpty(username)) {
             System.err.println("ERROR: loadTransactionsForAccount - Account username is invalid.");
             return;
        }

        // 2. Get and Clear Existing Transaction List
        List<TransactionModel> transactionList = account.getTransactions();
        if (transactionList == null) {
             // Log error, maybe initialize if possible, but loading cannot proceed.
             System.err.println("ERROR: loadTransactionsForAccount - Account's transaction list is null for user: " + username + ". Cannot load transactions.");
             // If AccountModel has setTransactions(List<...>) or similar:
             // account.setTransactions(new ArrayList<>());
             // transactionList = account.getTransactions();
             // If initialization isn't possible or desired here, just return.
             return;
        }
        transactionList.clear(); // Clear any stale data

        System.out.println("DEBUG: loadTransactionsForAccount - Loading transactions via TransactionService for user: " + username);

        // 3. Use TransactionService to Read Data
        List<TransactionData> transactionDataList = TransactionService.readTransactions(username);

        // 4. Populate AccountModel's List
        int loadedCount = 0;
        if (transactionDataList != null && !transactionDataList.isEmpty()) {
            System.out.println("DEBUG: loadTransactionsForAccount - Found " + transactionDataList.size() + " records from service.");
            for (TransactionData data : transactionDataList) {
                // Double-check data consistency (username should match)
                if (!username.equals(data.getUsername())) {
                    System.err.println("WARNING: loadTransactionsForAccount - Transaction data username mismatch. Expected '" + username + "', got '" + data.getUsername() + "'. Skipping.");
                    continue;
                }
                try {
                    // Create TransactionModel (ensure constructor exists and matches)
                    TransactionModel txModel = new TransactionModel(
                            data.getUsername(),    // user
                            data.getOperation(),   // operation
                            data.getAmount(),      // amount
                            data.getTime(),        // time
                            data.getMerchant(),    // merchant
                            data.getType()         // type
                    );
                    // Add to the account's list
                    transactionList.add(txModel);
                    loadedCount++;
                } catch (Exception e) {
                    // Catch errors during TransactionModel creation or adding
                     System.err.println("ERROR: loadTransactionsForAccount - Failed to process transaction data: " + data + " | Error: " + e.getMessage());
                     e.printStackTrace(); // Print stack trace for detailed debugging
                }
            }
        } else {
             System.out.println("DEBUG: loadTransactionsForAccount - No transactions returned by service for user: " + username);
        }

        System.out.println("DEBUG: loadTransactionsForAccount - Successfully loaded " + loadedCount + " transactions into AccountModel for user: " + username);
    }
    // The old direct CSV reading logic within loadTransactionsForAccount should be removed.
}