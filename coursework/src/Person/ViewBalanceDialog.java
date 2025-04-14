package Person;

import javax.swing.*;
import java.awt.*;
import model.AccountModel;
import model.UserRegistrationCSVExporter;

public class ViewBalanceDialog extends JDialog {

    public ViewBalanceDialog(Dialog owner, String username) {
        super(owner, "查看余额", true); // 模态对话框
        setSize(300, 150);
        setLocationRelativeTo(owner);
        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // 系统自动处理关闭

        // 查询账户信息
        AccountModel account = findAccount(username);

        if (account != null) {
            JLabel balanceLabel = new JLabel("您的账户余额为: ¥" + String.format("%.2f", account.getBalance()));
            add(balanceLabel);
        } else {
            JLabel errorLabel = new JLabel("找不到该用户名的账户信息。");
            add(errorLabel);
        }

    // 添加“关闭”按钮
    JButton closeButton = new JButton("关闭");
    closeButton.addActionListener(e -> {
        setVisible(false);
        SwingUtilities.invokeLater(() -> {
            dispose();
        });
    });
    add(closeButton);
    setVisible(true); // 显示对话框
    }
    private AccountModel findAccount(String username) {
        java.util.List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username)) {
                return account;
            }
        }
        return null;
    }
}