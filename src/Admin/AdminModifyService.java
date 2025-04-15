package Admin;

import java.util.List;

import model.AccountModel;
import model.UserRegistrationCSVExporter;

public class AdminModifyService {

    // 获取账户
    public static AccountModel getAccount(String username, String password) {
        System.out.println("正在查找用户名: [" + username + "]，密码: [" + password + "]"); // 添加这行
        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        for (AccountModel account : accounts) {
            System.out.println("读取到的账户 - 用户名: [" + account.getUsername() + "]，密码: [" + account.getPassword() + "]"); // 添加这行
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
                account.setAccountStatus(newStatus);  // 使用 setter 方法修改账户状态
                // 保存到CSV文件
                UserRegistrationCSVExporter.saveToCSV(accounts, false);
                return;
            }
        }
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