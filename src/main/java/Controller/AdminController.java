package Controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import Model.User;
import Model.UserSession;
import Repository.AccountRepository;
import View.Administrator.AdminView;
import View.LoginAndMain.LoginComponents;

/**
 * Manages administrator operations for the finance management system, including user account modification,
 * customer inquiries, user deletion, and data import. Interacts with the AdminView for UI updates and
 * AccountRepository for data operations.
 *
 * @author Group 19
 * @version 1.0
 */
public class AdminController {
    /** Repository for managing user account data. */
    private final AccountRepository accountRepository;
    /** Admin view for displaying UI components, initialized lazily. */
    private AdminView view;

    /**
     * Constructs an AdminController with the specified account repository.
     * Initializes required CSV files for accounts and transactions.
     *
     * @param accountRepository the repository for user account data
     * @throws IllegalStateException if file initialization fails
     */
    public AdminController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
        try {
            ensureFileExists("accounts.csv", AccountRepository.EXPECTED_ACCOUNT_HEADER);
            ensureFileExists("transactions.csv", TransactionController.CSV_HEADER);
        } catch (IOException e) {
            System.err.println("Error initializing files: " + e.getMessage());
        }
    }

    /**
     * Initializes the AdminView lazily if not already created.
     */
    private void initializeView() {
        if (view == null) {
            view = new AdminView(
                    this::showDashboard,
                    () -> view.showAdminInfoDialog(),
                    this::handleModifyCustomer,
                    this::handleCustomerInquiry,
                    this::handleDeleteUsers,
                    this::handleImportAccounts,
                    this::handleImportTransactions,
                    this::handleLogout
            );
        }
    }

    /**
     * Displays the dashboard panel in the AdminView.
     */
    void showDashboard() {
        initializeView();
        view.cardLayout.show(view.contentPanel, "dashboard");
    }

    /**
     * Handles the modification of a customer account by prompting for admin credentials
     * and customer username, then displaying a modification dialog.
     *
     * @param credentials an array containing [username, password] for admin verification
     */
    private void handleModifyCustomer(String[] credentials) {
        initializeView();
        String username = credentials[0];
        String password = credentials[1];

        User adminAccount = accountRepository.findByUsername(username);
        boolean isValidAdmin = adminAccount != null && adminAccount.getPassword().equals(password) && adminAccount.isAdmin();

        if (isValidAdmin) {
            String customerUsername = JOptionPane.showInputDialog(view, "Please enter the customer username to modify:");
            if (customerUsername != null && !customerUsername.trim().isEmpty()) {
                User targetAccount = accountRepository.findByUsername(customerUsername.trim());
                if (targetAccount != null) {
                    view.showModifyCustomerDialog(targetAccount, this::handleModifyConfirm);
                } else {
                    LoginComponents.showCustomMessage(view, "Customer username '" + customerUsername + "' not found!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else if (customerUsername != null) {
                LoginComponents.showCustomMessage(view, "Customer username cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            LoginComponents.showCustomMessage(view, "Invalid admin username or password, or not an admin account!", "Verification Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Confirms and processes the modification of a customer account based on dialog input.
     *
     * @param modifyView the dialog containing updated customer information
     * @throws IllegalStateException if admin verification fails or data saving fails
     */
    private void handleModifyConfirm(AdminView.ModifyCustomerDialog modifyView) {
        String username = modifyView.getUsername();
        String password = modifyView.getPassword();
        String phone = modifyView.getPhone();
        String email = modifyView.getEmail();
        String gender = modifyView.getGender();
        String address = modifyView.getAddress();
        String accountStatusStr = modifyView.getAccountStatus();
        String adminPassword = modifyView.getAdminPassword();
        String currentAdminUsername = UserSession.getCurrentUsername();

        try {
            User adminAccount = accountRepository.findByUsername(currentAdminUsername);
            if (currentAdminUsername == null || adminAccount == null || !adminAccount.getPassword().equals(adminPassword) || !adminAccount.isAdmin()) {
                LoginComponents.showCustomMessage(modifyView, "Admin password incorrect or not logged in as admin!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User account = accountRepository.findByUsername(username);
            if (account != null) {
                account.setPassword(password);
                account.setPhone(phone);
                account.setEmail(email);
                account.setGender(gender);
                account.setAddress(address);

                try {
                    User.AccountStatus accountStatus = User.AccountStatus.valueOf(accountStatusStr.toUpperCase());
                    account.setAccountStatus(accountStatus);
                } catch (IllegalArgumentException ex) {
                    LoginComponents.showCustomMessage(modifyView, "Invalid account status: " + accountStatusStr, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                List<User> accounts = accountRepository.readFromCSV();
                accounts.removeIf(u -> u.getUsername().equals(username));
                accounts.add(account);
                accountRepository.saveToCSV(accounts, false);

                LoginComponents.showCustomMessage(modifyView, "Customer information updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                modifyView.close();
                if (view.isUserListVisible()) {
                    handleCustomerInquiry(null);
                }
            } else {
                LoginComponents.showCustomMessage(modifyView, "User does not exist!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (RuntimeException e) {
            LoginComponents.showCustomMessage(modifyView, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Retrieves and displays the list of customer accounts in the AdminView.
     *
     * @param ignored unused parameter (reserved for future use)
     * @throws IllegalStateException if account data cannot be loaded
     */
    private void handleCustomerInquiry(List<User> ignored) {
        try {
            List<User> accounts = accountRepository.readFromCSV();
            view.updateAccountTable(accounts);
        } catch (Exception e) {
            LoginComponents.showCustomMessage(view, "Failed to load account data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Deletes the specified user accounts from the repository.
     *
     * @param usernamesToDelete a set of usernames to be deleted
     * @throws IllegalStateException if deletion fails
     */
    private void handleDeleteUsers(Set<String> usernamesToDelete) {
        if (usernamesToDelete.isEmpty()) {
            LoginComponents.showCustomMessage(view, "No users selected for deletion.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            List<User> accounts = accountRepository.readFromCSV();
            accounts.removeIf(user -> usernamesToDelete.contains(user.getUsername()));
            accountRepository.saveToCSV(accounts, false);
            LoginComponents.showCustomMessage(view, "Selected users deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            handleCustomerInquiry(null);
        } catch (Exception e) {
            LoginComponents.showCustomMessage(view, "Failed to delete users: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Imports customer account data from a specified CSV file.
     *
     * @param file the CSV file containing account data
     * @throws IllegalStateException if import fails
     */
    private void handleImportAccounts(File file) {
        try {
            TransactionController.importTransactions(file, "accounts.csv");
            LoginComponents.showCustomMessage(view, "Customer accounts imported successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            if (view.isUserListVisible()) {
                handleCustomerInquiry(null);
            }
        } catch (IOException e) {
            LoginComponents.showCustomMessage(view, "Failed to import accounts: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Imports transaction records from a specified CSV file.
     *
     * @param file the CSV file containing transaction data
     * @throws IllegalStateException if import fails
     */
    private void handleImportTransactions(File file) {
        try {
            TransactionController.importTransactions(file, "transactions.csv");
            LoginComponents.showCustomMessage(view, "Transaction records imported successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            LoginComponents.showCustomMessage(view, "Failed to import transaction records: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Logs out the current admin user and closes the AdminView.
     */
    private void handleLogout() {
        view.dispose();
    }

    /**
     * Ensures that the specified CSV file exists, creating it with the expected header if necessary.
     *
     * @param filePath        the path to the CSV file
     * @param expectedHeader  the expected header line for the CSV file
     * @throws IOException if file creation or writing fails
     */
    private void ensureFileExists(String filePath, String expectedHeader) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
                        bw.write(expectedHeader);
                        bw.newLine();
                        System.out.println("Created new file: " + filePath);
                    }
                }
            } catch (IOException e) {
                throw new IOException("Error creating file: " + e.getMessage(), e);
            }
        }
    }
}