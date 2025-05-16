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
     * Initializes required CSV files for accounts.
     *
     * @param accountRepository the repository for user account data
     * @throws IllegalStateException if file initialization fails
     */
    public AdminController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
        try {
            ensureFileExists("accounts.csv", AccountRepository.EXPECTED_ACCOUNT_HEADER);
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
                    this::handleModifyCustomer,
                    this::handleCustomerInquiry,
                    this::handleDeleteUsers,
                    this::handleImportAccounts,
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
     * Handles querying a customer for modification by username.
     *
     * @param username the username of the customer to query
     */
    private void handleModifyCustomer(String username) {
        initializeView();
        User targetAccount = accountRepository.findByUsername(username.trim());
        if (targetAccount == null) {
            LoginComponents.showCustomMessage(view, "Customer username '" + username + "' not found!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        view.updateModifyPanel(targetAccount, this::handleSaveModifiedUser);
    }

    /**
     * Handles saving the modified user account.
     *
     * @param updatedAccount the updated user account
     */
    private void handleSaveModifiedUser(User updatedAccount) {
        try {
            List<User> accounts = accountRepository.readFromCSV();
            accounts.removeIf(u -> u.getUsername().equals(updatedAccount.getUsername()));
            accounts.add(updatedAccount);
            accountRepository.saveToCSV(accounts, false);
            LoginComponents.showCustomMessage(view, "Customer information updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            view.updateModifyPanel(null, this::handleSaveModifiedUser); // 刷新界面
        } catch (Exception e) {
            LoginComponents.showCustomMessage(view, "Failed to save changes: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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