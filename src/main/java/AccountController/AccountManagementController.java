package AccountController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import AccountModel.AccountModel;
import AccountModel.AccountRepository;
import AdminController.AdminController;
import PersonModel.UserSessionModel;
import TransactionController.TransactionController; // 导入新的 TransactionController
import TransactionModel.TransactionModel; // 导入新的 TransactionModel
import View.AccountManagementUI;
import View.PersonalMainPlane;

/**
 * Controller for handling login and registration logic in AccountManagementUI.
 * Interacts with model classes to validate credentials, manage sessions, load user data (including transactions),
 * and create new accounts.
 */
public class AccountManagementController {

    /**
     * Checks if a string is null or empty after trimming.
     *
     * @param value The string to check.
     * @return true if the string is null or empty after trimming, false otherwise.
     */
    private static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Handles user login attempts.
     * Validates input, checks credentials, verifies account status, loads transactions,
     * checks for abnormal activities, sets the session, and opens the appropriate AdminView or PersonalMainPlane.
     *
     * @param username The entered username.
     * @param password The entered password.
     * @param ui       The AccountManagementUI instance for displaying messages and closing the UI.
     */
    public void handleLogin(String username, String password, AccountManagementUI ui) {
        System.out.println("DEBUG: handleLogin - Attempting login for user: '" + username + "'");

        // 1. Validate input
        if (isEmpty(username)) {
            ui.showCustomMessage("用户名不能为空！", "登录错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (isEmpty(password)) {
            ui.showCustomMessage("密码不能为空！", "登录错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Read account data
        List<AccountModel> accounts = AccountRepository.readFromCSV();
        AccountModel matchedAccount = null;

        // 3. Find matching account
        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username) && account.getPassword().equals(password)) {
                matchedAccount = account;
                break;
            }
        }

        // 4. Process login result
        if (matchedAccount != null) {
            System.out.println("DEBUG: handleLogin - Account found: " + username);

            // 4a. Check account status
            if (matchedAccount.getAccountStatus() == AccountModel.AccountStatus.FROZEN) {
                System.out.println("DEBUG: handleLogin - Account is FROZEN.");
                ui.showCustomMessage("此账户当前被冻结。请联系管理员。", "账户冻结", JOptionPane.WARNING_MESSAGE);
                return;
            }
            System.out.println("DEBUG: handleLogin - Account status is ACTIVE.");

            // 4b. Load transactions
            loadTransactionsForAccount(matchedAccount);

            // 4c. Set user session
            UserSessionModel.setCurrentAccount(matchedAccount);
            System.out.println("DEBUG: handleLogin - User session set for: " + UserSessionModel.getCurrentUsername());

            // 4d. Check for abnormal transactions
            boolean abnormalDetected = TransactionController.hasAbnormalTransactions(matchedAccount);
            if (abnormalDetected) {
                System.out.println("DEBUG: handleLogin - Abnormal transaction activity detected.");
                SwingUtilities.invokeLater(() ->
                    ui.showCustomMessage(
                        "警告：近期检测到异常交易活动。\n请检查您的交易历史。",
                        "活动警告",
                        JOptionPane.WARNING_MESSAGE
                    )
                );
            } else {
                System.out.println("DEBUG: handleLogin - No abnormal transactions detected.");
            }

            try {
                if (matchedAccount.isAdmin()) {
                    System.out.println("DEBUG: handleLogin - Opening AdminUI.");
                    SwingUtilities.invokeLater(() -> new AdminController());
                } else {
                    System.out.println("DEBUG: handleLogin - Opening PersonalUI.");
                    SwingUtilities.invokeLater(() -> new PersonalMainPlane(username));
                }
                ui.closeWindow();
            } catch (Exception ex) {
                System.err.println("ERROR: handleLogin - Error opening main UI or closing login window!");
                ui.showCustomMessage("打开主应用窗口时发生错误。", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.out.println("DEBUG: handleLogin - Login failed: Incorrect username or password.");
            ui.showCustomMessage("用户名或密码错误！", "登录失败", JOptionPane.ERROR_MESSAGE);
        }
        System.out.println("DEBUG: handleLogin - Method end.");
    }

    /**
     * Handles user registration attempts.
     * Checks for existing usernames, validates input, creates an AccountModel instance,
     * and saves it to accounts.csv in append mode.
     *
     * @param username            The entered username.
     * @param password            The entered password.
     * @param phone               The entered phone number.
     * @param email               The entered email address.
     * @param gender              The selected gender.
     * @param address             The entered address.
     * @param selectedAccountType The selected account type ("personal" or "Admin").
     * @param ui                  The AccountManagementUI instance.
     */
    public void handleRegister(String username, String password, String phone, String email, String gender, String address, String selectedAccountType, AccountManagementUI ui) {
        System.out.println("DEBUG: handleRegister - Attempting registration for user: '" + username + "', type: '" + selectedAccountType + "'");

        // 1. Check if username already exists
        List<AccountModel> accounts = AccountRepository.readFromCSV();
        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username)) {
                ui.showCustomMessage("用户名已存在！请选择其他用户名。", "注册错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // 2. Validate all required fields
        if (isEmpty(username) || isEmpty(password) ||
            isEmpty(phone) || isEmpty(email) ||
            isEmpty(gender) || isEmpty(address) ||
            isEmpty(selectedAccountType)) {
            ui.showCustomMessage("所有字段均需填写！", "注册错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Prepare account details
        String creationTime = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date());
        AccountModel.AccountStatus accountStatus = AccountModel.AccountStatus.ACTIVE;
        double initialBalance = 0.0;

        // 4. Create AccountModel instance
        AccountModel newAccount = new AccountModel(
            username, password, phone, email, gender, address,
            creationTime, accountStatus, selectedAccountType, initialBalance
        );

        // 5. Save new account
        List<AccountModel> accountListToAdd = new ArrayList<>();
        accountListToAdd.add(newAccount);
        boolean saved = AccountRepository.saveToCSV(accountListToAdd, true);

        if (saved) {
            System.out.println("DEBUG: handleRegister - Account saved successfully for user: " + username);
            ui.showCustomMessage("账户创建成功！您现在可以登录。", "注册成功", JOptionPane.INFORMATION_MESSAGE);
            ui.switchToLoginPanel();
        } else {
            ui.showCustomMessage("无法保存账户信息。请重试或联系支持。", "注册错误", JOptionPane.ERROR_MESSAGE);
        }
        System.out.println("DEBUG: handleRegister - Method end for user: " + username);
    }

    /**
     * Loads transaction records from transactions.csv for the given account and populates its internal transaction list.
     *
     * @param account The AccountModel object to load transactions for.
     */
    private void loadTransactionsForAccount(AccountModel account) {
        if (account == null) {
            System.err.println("ERROR: loadTransactionsForAccount - Account object is null.");
            return;
        }
        String username = account.getUsername();
        if (isEmpty(username)) {
            System.err.println("ERROR: loadTransactionsForAccount - Account username is invalid.");
            return;
        }

        List<TransactionModel> transactionList = account.getTransactions();
        if (transactionList == null) {
            System.err.println("ERROR: loadTransactionsForAccount - Account's transaction list is null for user: " + username + ". Cannot load transactions.");
            return;
        }
        transactionList.clear();

        System.out.println("DEBUG: loadTransactionsForAccount - Loading transactions via TransactionController for user: " + username);

        List<TransactionModel> transactionDataList = TransactionController.readTransactions(username);

        int loadedCount = 0;
        if (transactionDataList != null && !transactionDataList.isEmpty()) {
            System.out.println("DEBUG: loadTransactionsForAccount - Found " + transactionDataList.size() + " records from controller.");
            for (TransactionModel data : transactionDataList) {
                if (!username.equals(data.getAccountUsername())) {
                    System.err.println("WARNING: loadTransactionsForAccount - Transaction data username mismatch. Expected '" + username + "', got '" + data.getAccountUsername() + "'. Skipping.");
                    continue;
                }
                try {
                    // TransactionModel is already in the correct format, add directly
                    transactionList.add(data);
                    loadedCount++;
                } catch (Exception e) {
                    System.err.println("ERROR: loadTransactionsForAccount - Failed to process transaction data: " + data + " | Error: " + e.getMessage());
                }
            }
        } else {
            System.out.println("DEBUG: loadTransactionsForAccount - No transactions returned by controller for user: " + username);
        }

        System.out.println("DEBUG: loadTransactionsForAccount - Successfully loaded " + loadedCount + " transactions into AccountModel for user: " + username);
    }
}