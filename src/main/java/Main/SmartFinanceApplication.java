package Main;

import javax.swing.SwingUtilities;

import Controller.LoginController;
import Repository.AccountRepository;
import View.LoginAndMain.Login;

/**
 * Main entry point for the Smart Finance Application.
 * This class initializes the application by setting up the account repository,
 * creating the login controller, and launching the login interface.
 *
 * @author Group 19
 * @version 1.0
 */
public class SmartFinanceApplication {
    /**
     * The main method that starts the Smart Finance Application.
     * It configures the account repository, initializes the login controller,
     * and launches the login interface on the Event Dispatch Thread (EDT).
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        // Set the file path for accounts.csv (assumed to be in the project root directory)
        String accountsFilePath = "accounts.csv";

        // Manually create dependencies
        AccountRepository accountRepository = new AccountRepository(accountsFilePath);
        LoginController loginController = new LoginController(accountRepository);

        // Launch the login interface
        SwingUtilities.invokeLater(() -> {
            new Login(loginController).setVisible(true);
        });
    }
}