package Person;

import java.awt.Dialog;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import Model.AccountModel;
import Model.UserRegistrationCSVExporter;

public class ViewBalance extends JDialog {

    public ViewBalance(Dialog owner, String username) {
        super(owner, "View Balance", true); // 模态对话框
        setSize(300, 150);
        setLocationRelativeTo(owner);
        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // 系统自动处理关闭

        // 查询账户信息
        AccountModel account = findAccount(username);

        if (account != null) {
            JLabel balanceLabel = new JLabel("Your account balance is: ¥" + String.format("%.2f", account.getBalance()));
            add(balanceLabel);
        } else {
            JLabel errorLabel = new JLabel("Account information not found for this username.");
            add(errorLabel);
        }

    // 添加“关闭”按钮
    JButton closeButton = new JButton("Close");
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