package AdminController;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AccountModel.AccountModel;
import AdminModel.AccountService;
import View.AdminModifyPersonalInfoView;

public class AdminModifyPersonalInfoController {
    private static final Logger logger = LoggerFactory.getLogger(AdminModifyPersonalInfoController.class);
    private final AccountService accountService;
    private final AccountModel currentAccount; // 改为 AccountModel
    private AdminModifyPersonalInfoView view;

    public AdminModifyPersonalInfoController(AccountService accountService, AccountModel currentAccount) {
        this.accountService = accountService;
        this.currentAccount = currentAccount;
    }

    public void initialize(AccountModel account) {
        view = new AdminModifyPersonalInfoView(this::handleConfirm);
        view.setAccountInfo(account);
    }

    private void handleConfirm(AdminModifyPersonalInfoView view) {
        String username = view.getUsername();
        String password = view.getPassword();
        String phone = view.getPhone();
        String email = view.getEmail();
        String gender = view.getGender();
        String address = view.getAddress();
        String accountStatusStr = view.getAccountStatus();
        String adminPassword = view.getAdminPassword();
        String currentAdminUsername = currentAccount != null ? currentAccount.getUsername() : null;

        try {
            if (currentAdminUsername == null || !accountService.isAdminPasswordValid(adminPassword, currentAdminUsername)) {
                view.showMessage("Incorrect admin password or no admin logged in!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            AccountModel account = accountService.getAccount(username, password);
            if (account != null) {
                boolean updatedInfo = accountService.updateCustomerInfo(username, password, phone, email, gender, address);
                boolean updatedStatus = true;
                try {
                    AccountModel.AccountStatus accountStatus = AccountModel.AccountStatus.valueOf(accountStatusStr.toUpperCase());
                    updatedStatus = accountService.modifyAccountStatus(username, accountStatus);
                } catch (IllegalArgumentException ex) {
                    view.showMessage("Invalid account status: " + accountStatusStr, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (updatedInfo && updatedStatus) {
                    view.showMessage("Customer information updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    view.close();
                } else {
                    view.showMessage("Failed to update customer information!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                view.showMessage("User does not exist or password incorrect!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (RuntimeException e) {
            logger.error("Error processing modification", e);
            view.showMessage("Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}