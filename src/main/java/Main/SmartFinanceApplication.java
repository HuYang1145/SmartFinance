package Main;

import javax.swing.SwingUtilities;

import Controller.LoginController;
import Repository.AccountRepository;
import View.LoginAndMain.Login;

public class SmartFinanceApplication {
    public static void main(String[] args) {
        // 设置 accounts.csv 文件路径（假设在项目根目录）
        String accountsFilePath = "accounts.csv";

        // 手动创建依赖
        AccountRepository accountRepository = new AccountRepository(accountsFilePath);
        LoginController loginController = new LoginController(accountRepository);

        // 启动登录界面
        SwingUtilities.invokeLater(() -> {
            new Login(loginController).setVisible(true);
        });
    }
}