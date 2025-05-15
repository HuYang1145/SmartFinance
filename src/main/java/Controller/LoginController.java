/**
 * Manages user authentication and registration for the finance management system.
 * Handles login and registration processes, directing users to appropriate interfaces based on their role.
 *
 * @author Group 19
 * @version 1.0
 */
package Controller;

import javax.swing.JOptionPane;
import java.util.List;
import java.util.stream.Collectors; // Added import for Collectors

import Model.User;
import Model.UserSession;
import Repository.AccountRepository;
import Repository.TransactionRepository; // Keep import for dependency creation
import Service.BudgetService;
import Service.PersonChartDataService;
import Service.PersonFinancialService;
import Service.TransactionService;
import View.Administrator.AdminView;
import View.LoginAndMain.Login;
import View.LoginAndMain.MainPlane;
import View.LoginAndMain.LoginComponents;

/**
 * Manages user authentication and registration for the finance management system.
 * Handles login and registration processes, directing users to appropriate interfaces based on their role.
 * Includes abnormal transaction checks upon login.
 *
 * @author Group 19
 * @version 1.0
 */
public class LoginController {
    /** Repository for managing user account data. */
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    private final BudgetService budgetService;

    /**
     * Constructs a LoginController with the specified dependencies.
     *
     * @param accountRepository the repository for user account data
     * @param transactionService the service for transaction-related operations and checks
     * @param budgetService      the service for budget calculations, including average expense
     */
    public LoginController(AccountRepository accountRepository, TransactionService transactionService, BudgetService budgetService) {
        this.accountRepository = accountRepository;
        this.transactionService = transactionService;
        this.budgetService = budgetService;
    }

    /**
     * Processes a login attempt by verifying the provided username and password.
     * If successful, sets the user session, checks for abnormal transactions, and displays the appropriate interface;
     * otherwise, shows an error message.
     *
     * @param username    the username entered by the user
     * @param password    the password entered by the user
     * @param loginFrame  the login frame for displaying messages and closing the window
     */
    public void handleLogin(String username, String password, Login loginFrame) {
        User user = accountRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            if (user.getAccountStatus() == User.AccountStatus.FROZEN) {
                LoginComponents.showCustomMessage(loginFrame, "Account is frozen", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            UserSession.setCurrentAccount(user); // Sets both account and username
            loginFrame.closeWindow();

            // --- Added Abnormal Transaction Check on Login ---
            // Fetch all transactions and filter for the current user
            List<Model.Transaction> allTransactions = transactionService.getTransactionRepository().readAllTransactions(); // Use service's exposed repository or pass service to read
             List<Model.Transaction> currentUserTransactions = allTransactions.stream()
                 .filter(tx -> user.getUsername().equals(tx.getAccountUsername()))
                 .collect(Collectors.toList());

            List<String> warnings = transactionService.checkAbnormalTransactions(user.getUsername(), currentUserTransactions);

            if (!warnings.isEmpty()) {
                StringBuilder warningMessage = new StringBuilder("<html><center><b>Warning: Potential abnormal transactions detected in your history.</b><br>");
                warningMessage.append("Please review your transaction details.<br><br>");
                for (String warning : warnings) {
                    warningMessage.append("- ").append(warning).append("<br>");
                }
                warningMessage.append("</center></html>");
                // Use null as parent to center on screen, or loginFrame if preferred
                LoginComponents.showCustomMessage(null, warningMessage.toString(), "Transaction Risk Alert", JOptionPane.WARNING_MESSAGE);
            }
            // --- End of Added Check ---

            if (user.isAdmin()) {
                showAdminInterface(username);
            } else {
                showMainInterface(username);
            }
        } else {
            LoginComponents.showCustomMessage(loginFrame, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Processes a registration request by validating input and creating a new user account.
     * If successful, saves the user to the repository and prompts for login; otherwise,
     * shows an error message.
     *
     * @param username     the username for the new account
     * @param password     the password for the new account
     * @param phone        the phone number for the new account
     * @param email        the email address for the new account
     * @param gender       the gender for the new account
     * @param address      the address for the new account
     * @param accountType  the type of account (e.g., Admin, Personal)
     * @param loginFrame   the login frame for displaying messages and switching panels
     */
    public void handleRegister(String username, String password, String phone, String email,
                               String gender, String address, String accountType, Login loginFrame) {
        if (username.isEmpty() || password.isEmpty()) {
            LoginComponents.showCustomMessage(loginFrame, "Username and password are required", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User existingUser = accountRepository.findByUsername(username);
        if (existingUser != null) {
            LoginComponents.showCustomMessage(loginFrame, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User newUser = new User(
                username, password, phone, email, gender, address,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")),
                User.AccountStatus.ACTIVE, accountType, 0.0
        );
        accountRepository.save(newUser);
        LoginComponents.showCustomMessage(loginFrame, "Registration successful! Please log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
        loginFrame.switchToLoginPanel();
    }

    /**
     * Initializes and displays the main interface for ordinary users.
     * Creates necessary dependencies for financial and chart services and controllers.
     *
     * @param username the username of the logged-in user
     */
    void showMainInterface(String username) {
        // Create services needed for MainPlane and its controllers.
        // BudgetService and TransactionService are already fields in LoginController,
        // so we can reuse them.
        TransactionRepository transactionRepository = transactionService.getTransactionRepository(); // Get repo from service
        PersonFinancialService financialService = new PersonFinancialService(transactionRepository);
        PersonChartDataService chartDataService = new PersonChartDataService(transactionRepository);
        PersonCenterController personCenterController = new PersonCenterController(financialService, chartDataService);
        BillController billController = new BillController(accountRepository);

        // Pass shared BudgetService and TransactionService instances to MainPlane
        new MainPlane(personCenterController, billController, budgetService, transactionService).setVisible(true);
    }

    /**
     * Initializes and displays the admin interface for administrators.
     * Creates an AdminController and sets up the AdminView with necessary actions.
     *
     * @param username the username of the logged-in admin
     */
    private void showAdminInterface(String username) {
        AdminController adminController = new AdminController(accountRepository);
        adminController.showDashboard();
    }
}