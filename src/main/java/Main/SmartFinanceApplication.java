package Main;

import javax.swing.SwingUtilities;

import Controller.LoginController;
import Repository.AccountRepository;
import Repository.TransactionRepository; // Added import
import Service.BudgetService; // Added import
import Service.TransactionService; // Added import
import View.LoginAndMain.Login;
// Removed LoginComponents import as it's not directly used in main

/**
 * Main entry point for the Smart Finance Application.
 * This class initializes the application by setting up the account repository,
 * creating necessary service dependencies, creating the login controller,
 * and launching the login interface.
 *
 * @author Group 19
 * @version 1.0
 */
public class SmartFinanceApplication {
    /**
     * The main method that starts the Smart Finance Application.
     * It configures repositories and services, initializes the login controller,
     * and launches the login interface on the Event Dispatch Thread (EDT).
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        // Set the file path for accounts.csv (assumed to be in the project root directory)
        String accountsFilePath = "accounts.csv";

        // --- Manually create dependencies ---
        // AccountRepository needs the file path
        AccountRepository accountRepository = new AccountRepository(accountsFilePath);

        // TransactionRepository is needed by services
        TransactionRepository transactionRepository = new TransactionRepository();

        // BudgetService needs TransactionRepository
        BudgetService budgetService = new BudgetService(transactionRepository);

        // TransactionService needs TransactionRepository and BudgetService
        TransactionService transactionService = new TransactionService(transactionRepository, budgetService);

        // --- Create LoginController with all required dependencies ---
        // Fix: Pass all three required arguments to the LoginController constructor
        LoginController loginController = new LoginController(accountRepository, transactionService, budgetService);

        // --- Launch the login interface ---
        // Ensure the GUI is created and shown on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            System.out.println("Launching Login interface on EDT.");
            try {
                new Login(loginController).setVisible(true);
            } catch (Exception e) {
                 System.err.println("Failed to launch Login interface:");
                 e.printStackTrace();
                 // Optionally show a simple error dialog if launching fails
                 // javax.swing.JOptionPane.showMessageDialog(null, "Application startup failed: " + e.getMessage(), "Startup Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });

        System.out.println("Main method finished.");
    }
}