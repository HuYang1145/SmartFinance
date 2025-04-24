package AccountController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import AccountModel.AccountModel;
import AccountModel.AccountRepository;
import AccountModel.TransactionCheckerModel;
import AccountModel.TransactionModel;
import AccountModel.TransactionServiceModel;
import AccountModel.TransactionServiceModel.TransactionData;
import AdminController.AdminController;
import PersonModel.UserSessionModel;
import View.AccountManagementUI;
import View.PersonalMainPlane;

/**
 * 控制器，处理 AccountManagementUI 的登录和注册逻辑。
 * 与模型类交互以验证凭据、管理会话、加载用户数据（包括交易记录）并创建新账户。
 */
public class AccountManagementController {

    /**
     * 检查字符串是否为空或去除空格后为空。
     *
     * @param value 要检查的字符串。
     * @return 如果字符串为空或去除空格后为空，返回 true；否则返回 false。
     */
    private static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * 处理用户登录尝试。
     * 验证输入、检查凭据、检查账户状态、加载交易记录、检测异常活动、设置会话，
     * 并打开相应的 AdminView 或 PersonalMainPlane。
     *
     * @param username 输入的用户名。
     * @param password 输入的密码。
     * @param ui AccountManagementUI 实例，用于显示消息和关闭界面。
     */
    public void handleLogin(String username, String password, AccountManagementUI ui) {
        System.out.println("DEBUG: handleLogin - Attempting login for user: '" + username + "'");

        // 1. 验证输入
        if (isEmpty(username)) {
            ui.showCustomMessage("用户名不能为空！", "登录错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (isEmpty(password)) {
            ui.showCustomMessage("密码不能为空！", "登录错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. 读取账户数据
        List<AccountModel> accounts = AccountRepository.readFromCSV();
        AccountModel matchedAccount = null;

        // 3. 查找匹配的账户
        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username) && account.getPassword().equals(password)) {
                matchedAccount = account;
                break;
            }
        }

        // 4. 处理登录结果
        if (matchedAccount != null) {
            System.out.println("DEBUG: handleLogin - Account found: " + username);

            // 4a. 检查账户状态
            if (matchedAccount.getAccountStatus() == AccountModel.AccountStatus.FROZEN) {
                System.out.println("DEBUG: handleLogin - Account is FROZEN.");
                ui.showCustomMessage("此账户当前被冻结。请联系管理员。", "账户冻结", JOptionPane.WARNING_MESSAGE);
                return;
            }
            System.out.println("DEBUG: handleLogin - Account status is ACTIVE.");

            // 4b. 加载交易记录
            loadTransactionsForAccount(matchedAccount);

            // 4c. 设置用户会话
            UserSessionModel.setCurrentAccount(matchedAccount);
            System.out.println("DEBUG: handleLogin - User session set for: " + UserSessionModel.getCurrentUsername());

            // 4d. 检查异常交易
            boolean abnormalDetected = TransactionCheckerModel.hasAbnormalTransactions(matchedAccount);
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
     * 处理用户注册尝试。
     * 检查用户名是否存在、验证输入、创建 AccountModel 实例，
     * 并以追加模式保存到 accounts.csv。
     *
     * @param username            输入的用户名。
     * @param password            输入的密码。
     * @param phone               输入的电话号码。
     * @param email               输入的邮箱地址。
     * @param gender              选择的性别。
     * @param address             输入的地址。
     * @param selectedAccountType 选择的账户类型（"personal" 或 "Admin"）。
     * @param ui                  AccountManagementUI 实例。
     */
    public void handleRegister(String username, String password, String phone, String email, String gender, String address, String selectedAccountType, AccountManagementUI ui) {
        System.out.println("DEBUG: handleRegister - Attempting registration for user: '" + username + "', type: '" + selectedAccountType + "'");

        // 1. 检查用户名是否已存在
        List<AccountModel> accounts = AccountRepository.readFromCSV();
        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username)) {
                ui.showCustomMessage("用户名已存在！请选择其他用户名。", "注册错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // 2. 验证所有必填字段非空
        if (isEmpty(username) || isEmpty(password) ||
            isEmpty(phone) || isEmpty(email) ||
            isEmpty(gender) || isEmpty(address) ||
            isEmpty(selectedAccountType)) {
            ui.showCustomMessage("所有字段均需填写！", "注册错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. 准备账户详情
        String creationTime = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date());
        AccountModel.AccountStatus accountStatus = AccountModel.AccountStatus.ACTIVE;
        double initialBalance = 0.0;

        // 4. 创建 AccountModel 实例
        AccountModel newAccount = new AccountModel(
            username, password, phone, email, gender, address,
            creationTime, accountStatus, selectedAccountType, initialBalance
        );

        // 5. 保存新账户
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
     * 为给定账户从 transactions.csv 加载交易记录，并填充账户的内部交易列表。
     * 假设 account.getTransactions() 返回一个非空的、可清除的、可修改的 List，
     * 并且 TransactionModel 有一个匹配新 6 字段结构的构造函数。
     *
     * @param account 要加载交易记录的 AccountModel 对象。
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

        System.out.println("DEBUG: loadTransactionsForAccount - Loading transactions via TransactionService for user: " + username);

        List<TransactionData> transactionDataList = TransactionServiceModel.readTransactions(username);

        int loadedCount = 0;
        if (transactionDataList != null && !transactionDataList.isEmpty()) {
            System.out.println("DEBUG: loadTransactionsForAccount - Found " + transactionDataList.size() + " records from service.");
            for (TransactionData data : transactionDataList) {
                if (!username.equals(data.getUsername())) {
                    System.err.println("WARNING: loadTransactionsForAccount - Transaction data username mismatch. Expected '" + username + "', got '" + data.getUsername() + "'. Skipping.");
                    continue;
                }
                try {
                    TransactionModel txModel = new TransactionModel(
                            data.getUsername(),
                            data.getOperation(),
                            data.getAmount(),
                            data.getTime(),
                            data.getMerchant(),
                            data.getType()
                    );
                    transactionList.add(txModel);
                    loadedCount++;
                } catch (Exception e) {
                    System.err.println("ERROR: loadTransactionsForAccount - Failed to process transaction data: " + data + " | Error: " + e.getMessage());
                }
            }
        } else {
            System.out.println("DEBUG: loadTransactionsForAccount - No transactions returned by service for user: " + username);
        }

        System.out.println("DEBUG: loadTransactionsForAccount - Successfully loaded " + loadedCount + " transactions into AccountModel for user: " + username);
    }
}