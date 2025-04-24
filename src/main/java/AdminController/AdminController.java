package AdminController;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AccountModel.AccountModel;
import AccountModel.TransactionCSVImporterModel;
import AdminModel.AdminModel;
import PersonModel.UserSessionModel;
import View.AdminView;
import View.AdminView.ModifyCustomerDialog;

public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final AdminModel model;
    private AdminView view = null;

    public AdminController() {
        this.model = new AdminModel();
        this.view = new AdminView(
            this::showDashboard,
            () -> view.showAdminInfoDialog(model),
            this::handleModifyCustomer,
            this::handleCustomerInquiry,
            this::handleDeleteUsers,
            this::handleImportAccounts,
            this::handleImportTransactions,
            this::handleLogout
        );

        try {
            model.ensureFileExists("accounts.csv", AdminModel.EXPECTED_ACCOUNT_HEADER);
            model.ensureFileExists("transactions.csv", TransactionCSVImporterModel.EXPECTED_HEADER);
        } catch (IOException e) {
            logger.error("Error initializing files", e); // Translated from "初始化文件时出错"
            view.showMessage("Error initializing files: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); // Translated from "初始化文件时出错"
        }
    }

    private void showDashboard() {
        view.cardLayout.show(view.contentPanel, "dashboard");
    }

    private void handleModifyCustomer(String[] credentials) {
        String username = credentials[0];
        String password = credentials[1];

        boolean isValidAdmin = model.isAdminPasswordValid(password, username);
        AccountModel adminAccount = model.getAccount(username, password);

        if (isValidAdmin && adminAccount != null && adminAccount.isAdmin()) {
            String customerUsername = JOptionPane.showInputDialog(view, "Please enter the customer username to modify:"); // Translated from "请输入要修改的客户用户名："
            if (customerUsername != null && !customerUsername.trim().isEmpty()) {
                AccountModel targetAccount = model.getAccountByUsername(customerUsername.trim());
                if (targetAccount != null) {
                    view.showModifyCustomerDialog(targetAccount, this::handleModifyConfirm);
                } else {
                    view.showMessage("Customer username '" + customerUsername + "' not found!", "Error", JOptionPane.ERROR_MESSAGE); // Translated from "客户用户名" + "' 未找到！"
                }
            } else if (customerUsername != null) {
                view.showMessage("Customer username cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE); // Translated from "客户用户名不能为空。"
            }
        } else {
            view.showMessage("Invalid admin username or password, or not an admin account!", "Verification Failed", JOptionPane.ERROR_MESSAGE); // Translated from "无效的管理员用户名或密码，或不是管理员账户！"
        }
    }

    private void handleModifyConfirm(ModifyCustomerDialog modifyView) {
        String username = modifyView.getUsername();
        String password = modifyView.getPassword();
        String phone = modifyView.getPhone();
        String email = modifyView.getEmail();
        String gender = modifyView.getGender();
        String address = modifyView.getAddress();
        String accountStatusStr = modifyView.getAccountStatus();
        String adminPassword = modifyView.getAdminPassword();
        String currentAdminUsername = UserSessionModel.getCurrentUsername();

        try {
            if (currentAdminUsername == null || !model.isAdminPasswordValid(adminPassword, currentAdminUsername)) {
                modifyView.showMessage("Admin password incorrect or not logged in as admin!", "Error", JOptionPane.ERROR_MESSAGE); // Translated from "管理员密码错误或未登录管理员！"
                return;
            }

            AccountModel account = model.getAccount(username, password); // This seems incorrect, should likely use getAccountByUsername
            if (account != null) {
                boolean updatedInfo = model.updateCustomerInfo(username, password, phone, email, gender, address);
                boolean updatedStatus = true;
                try {
                    AccountModel.AccountStatus accountStatus = AccountModel.AccountStatus.valueOf(accountStatusStr.toUpperCase());
                    updatedStatus = model.modifyAccountStatus(username, accountStatus);
                } catch (IllegalArgumentException ex) {
                    modifyView.showMessage("Invalid account status: " + accountStatusStr, "Error", JOptionPane.ERROR_MESSAGE); // Translated from "无效的账户状态"
                    return;
                }

                if (updatedInfo && updatedStatus) {
                    modifyView.showMessage("Customer information updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE); // Translated from "客户信息更新成功！"
                    modifyView.close();
                    if (view.isUserListVisible()) {
                        handleCustomerInquiry(null); // Refresh user list if visible
                    }
                } else {
                    modifyView.showMessage("Failed to update customer information!", "Error", JOptionPane.ERROR_MESSAGE); // Translated from "无法更新客户信息！"
                }
            } else {
                modifyView.showMessage("User does not exist or password incorrect!", "Error", JOptionPane.ERROR_MESSAGE); // Translated from "用户不存在或密码错误！"
            }
        } catch (RuntimeException e) {
            logger.error("Error processing modification", e); // Translated from "处理修改时出错"
            modifyView.showMessage("Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); // Translated from "错误:"
        }
    }

    private void handleCustomerInquiry(List<AccountModel> ignored) { // The List<AccountModel> parameter seems unused, maybe left from refactoring?
        try {
            List<AccountModel> accounts = model.readFromCSV(); // This might be inefficient if AdminModel.readFromCSV reads the whole file
            view.updateAccountTable(accounts);
        } catch (Exception e) { // Catching generic Exception is generally not recommended
            logger.error("Error reading accounts.csv", e); // Translated from "读取 accounts.csv 时出错"
            view.showMessage("Failed to load account data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); // Translated from "无法加载账户数据"
        }
    }

    private void handleDeleteUsers(Set<String> usernamesToDelete) {
        if (usernamesToDelete.isEmpty()) {
            view.showMessage("No users selected for deletion.", "Information", JOptionPane.INFORMATION_MESSAGE); // Translated from "未选择要删除的用户。"
            return;
        }

        try {
            model.deleteUsers(usernamesToDelete);
            view.showMessage("Selected users deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE); // Translated from "选中的用户已成功删除。"
            handleCustomerInquiry(null); // Refresh the list after deletion
        } catch (IOException e) { // Catching IOException from the model is good
            logger.error("Error deleting users", e); // Translated from "删除用户时出错"
            view.showMessage("Failed to delete users: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); // Translated from "无法删除用户"
        }
    }

    private void handleImportAccounts(File file) {
        try {
            model.importAccounts(file);
            view.showMessage("Customer accounts imported successfully.", "Success", JOptionPane.INFORMATION_MESSAGE); // Translated from "成功导入客户账户。"
            if (view.isUserListVisible()) { // Assuming this checks if the table view is active
                handleCustomerInquiry(null); // Refresh the list after import
            }
        } catch (IOException e) { // Catching IOException from the model is good
            logger.error("Error importing accounts", e); // Translated from "导入账户时出错"
            view.showMessage("Failed to import accounts: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); // Translated from "无法导入账户"
        }
    }

    private void handleImportTransactions(File file) {
        try {
            // Assuming model.importTransactions handles the file path logic internally or uses a better mechanism
            model.importTransactions(file, "transactions.csv"); // The "transactions.csv" string literal might belong in a constant in the Model layer
            view.showMessage("Transaction records imported successfully.", "Success", JOptionPane.INFORMATION_MESSAGE); // Translated from "成功导入交易记录。"
            // No table update needed here as this view doesn't show transaction history directly
        } catch (IOException e) { // Catching IOException from the model is good
            logger.error("Error importing transaction records", e); // Translated from "导入交易记录时出错"
            view.showMessage("Failed to import transaction records: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); // Translated from "无法导入交易记录"
        }
    }

    private void handleLogout() {
        view.dispose(); // Close the admin view window
    }
}