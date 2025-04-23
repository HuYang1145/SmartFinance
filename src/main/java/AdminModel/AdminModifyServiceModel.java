package AdminModel;

import java.util.List;

import AccountModel.AccountModel;
import AccountModel.UserRegistrationCSVExporterModel;

public class AdminModifyServiceModel {

    // 获取账户
    public static AccountModel getAccount(String username, String password) {
        System.out.println("Searching for username: [" + username + "]，password: [" + password + "]"); // 添加这行
        List<AccountModel> accounts = UserRegistrationCSVExporterModel.readFromCSV();
        for (AccountModel account : accounts) {
            System.out.println("Read account - Username: [" + account.getUsername() + "]，Password: [" + account.getPassword() + "]"); // 添加这行
            if (account.getUsername().equals(username) && account.getPassword().equals(password)) {
                return account;
            }
        }
        return null;
    }

    public static void modifyAccountStatus(String username, AccountModel.AccountStatus newStatus) {
        List<AccountModel> accounts = UserRegistrationCSVExporterModel.readFromCSV();
        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username)) {
                // 直接设置新状态（枚举保证了有效性）
                account.setAccountStatus(newStatus);
                // 保存到 CSV 文件
                UserRegistrationCSVExporterModel.saveToCSV(accounts, false);
                return;
            }
        }
        System.out.println("Account not found: " + username);
    }

    // 更新客户信息（密码，手机号，邮箱，性别，地址）
    public static boolean updateCustomerInfo(String username, String password, String phone, String email, String gender, String address) {
        List<AccountModel> accounts = UserRegistrationCSVExporterModel.readFromCSV();
        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username)) {
                account.setPassword(password);
                account.setPhone(phone);
                account.setEmail(email);
                account.setGender(gender);
                account.setAddress(address);
                // 保存到CSV文件
                UserRegistrationCSVExporterModel.saveToCSV(accounts, false);
                return true; // 找到并更新了账户
            }
        }
        return false; // 没有找到匹配的账户
    }

}