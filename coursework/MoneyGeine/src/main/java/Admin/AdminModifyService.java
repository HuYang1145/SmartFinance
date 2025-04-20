package Admin;

import java.util.List;

import Model.AccountModel;
import Model.UserRegistrationCSVExporter;


public class AdminModifyService {

    // 获取账户
    public static AccountModel getAccount(String username, String password) {
        System.out.println("Searching for username: [" + username + "]，password: [" + password + "]"); // 添加这行
        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        for (AccountModel account : accounts) {
            System.out.println("Read account - Username: [" + account.getUsername() + "]，Password: [" + account.getPassword() + "]"); // 添加这行
            if (account.getUsername().equals(username) && account.getPassword().equals(password)) {
                return account;
            }
        }
        return null;
    }

    // 修改账户状态
    public static void modifyAccountStatus(String username, String newStatus) {
        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username)) {
                // 检查新的状态是否是允许的值
                if (newStatus.equals("ACTIVE") || newStatus.equals("FROZEN")) {
                    account.setAccountStatus(newStatus);
                    // 保存到CSV文件
                    UserRegistrationCSVExporter.saveToCSV(accounts, false);
                    return;
                } else {
                    System.out.println("Invalid account status: [" + newStatus + "]. Allowed values are ACTIVE and FROZEN.");
                    return; // 可以根据需要抛出异常
                }
            }
        }
        System.out.println("Account with username [" + username + "] not found.");
    }

    // 更新客户信息（密码，手机号，邮箱，性别，地址）
    public static boolean updateCustomerInfo(String username, String password, String phone, String email, String gender, String address) {
        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username)) {
                account.setPassword(password);
                account.setPhone(phone);
                account.setEmail(email);
                account.setGender(gender);
                account.setAddress(address);
                // 保存到CSV文件
                UserRegistrationCSVExporter.saveToCSV(accounts, false);
                return true; // 找到并更新了账户
            }
        }
        return false; // 没有找到匹配的账户
    }

}