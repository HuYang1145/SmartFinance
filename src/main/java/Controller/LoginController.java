/**
 * Manages user authentication and registration for the finance management system.
 * Handles login and registration processes, directing users to appropriate interfaces based on their role.
 *
 * @author Group 19
 * @version 1.0
 */
package Controller;

import javax.swing.JOptionPane;

import Model.User;
import Model.UserSession;
import Repository.AccountRepository;
import Repository.TransactionRepository;
import Service.PersonChartDataService;
import Service.PersonFinancialService;
import View.Administrator.AdminView;
import View.LoginAndMain.Login;
import View.LoginAndMain.MainPlane;

public class LoginController {
    /** Repository for managing user account data. */
    private final AccountRepository accountRepository;

    /**
     * Constructs a LoginController with the specified account repository.
     *
     * @param accountRepository the repository for user account data
     */
    public LoginController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Processes a login attempt by verifying the provided username and password.
     * If successful, sets the user session and displays the appropriate interface based on user type; otherwise,
     * shows an error message.
     *
     * @param username    the username entered by the user
     * @param password    the password entered by the user
     * @param loginFrame  the login frame for displaying messages and closing the window
     */
    public void handleLogin(String username, String password, Login loginFrame) {
        User user = accountRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            if (user.getAccountStatus() == User.AccountStatus.FROZEN) {
                JOptionPane.showMessageDialog(loginFrame, "Account is frozen", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            UserSession.setCurrentAccount(user); // Sets both account and username
            loginFrame.closeWindow();
            if (user.isAdmin()) {
                showAdminInterface(username);
            } else {
                showMainInterface(username);
            }
        } else {
            JOptionPane.showMessageDialog(loginFrame, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(loginFrame, "Username and password are required", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User existingUser = accountRepository.findByUsername(username);
        if (existingUser != null) {
            JOptionPane.showMessageDialog(loginFrame, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User newUser = new User(
                username, password, phone, email, gender, address,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")),
                User.AccountStatus.ACTIVE, accountType, 0.0
        );
        accountRepository.save(newUser);
        JOptionPane.showMessageDialog(loginFrame, "Registration successful! Please log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
        loginFrame.switchToLoginPanel();
    }

    /**
     * Initializes and displays the main interface for ordinary users.
     * Creates necessary dependencies for financial and chart services and controllers.
     *
     * @param username the username of the logged-in user
     */
    private void showMainInterface(String username) {
        TransactionRepository transactionRepository = new TransactionRepository();
        PersonFinancialService financialService = new PersonFinancialService(transactionRepository);
        PersonChartDataService chartDataService = new PersonChartDataService(transactionRepository);
        PersonCenterController personCenterController = new PersonCenterController(financialService, chartDataService);
        BillController billController = new BillController(accountRepository);

        new MainPlane(personCenterController, billController).setVisible(true);
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