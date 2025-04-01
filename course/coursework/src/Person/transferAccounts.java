package Person;

import model.AccountModel;
import model.PersonalAccount;
import model.UserRegistrationCSVExporter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class transferAccounts extends JDialog {
    private JTextField recipientUsernameField, amountField;
    private JPasswordField passwordField;
    private JButton confirmButton, cancelButton;

    public transferAccounts(JFrame parent) {
        super(parent, "转出资金", true);
        setSize(300, 200);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 创建面板和布局
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

        JLabel recipientUsernameLabel = new JLabel("转入账户用户名:");
        recipientUsernameField = new JTextField();
        JLabel amountLabel = new JLabel("转出金额:");
        amountField = new JTextField();
        JLabel passwordLabel = new JLabel("密码:");
        passwordField = new JPasswordField();

        confirmButton = new JButton("确认");
        cancelButton = new JButton("取消");

        confirmButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String recipientUsername = recipientUsernameField.getText();
                String amountText = amountField.getText();
                char[] passwordChars = passwordField.getPassword();
                String password = new String(passwordChars);

                if (recipientUsername.isEmpty() || amountText.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(transferAccounts.this, "所有字段都不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    double amount = Double.parseDouble(amountText);
                    if (amount <= 0) {
                        JOptionPane.showMessageDialog(transferAccounts.this, "请输入有效的金额", "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // 使用 UserSession 获取当前登录用户名 (转出方)
                    String loggedInUsername = model.UserSession.getCurrentUsername();

                    // 验证用户账户余额和密码
                    AccountModel loggedInAccount = getAccount(loggedInUsername, password);
                    if (loggedInAccount == null) {
                        JOptionPane.showMessageDialog(transferAccounts.this, "密码错误", "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (loggedInAccount instanceof PersonalAccount) {
                        PersonalAccount personalAccount = (PersonalAccount) loggedInAccount;

                        if (personalAccount.getBalance() < amount) {
                            JOptionPane.showMessageDialog(transferAccounts.this, "余额不足，无法转出该金额", "错误", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        // 查找转入账户
                        AccountModel recipientAccount = findAccountByUsername(recipientUsername);
                        if (recipientAccount != null && recipientAccount instanceof PersonalAccount) {
                            PersonalAccount recipientPersonalAccount = (PersonalAccount) recipientAccount;

                            // 执行转账操作
                            personalAccount.setBalance(personalAccount.getBalance() - amount);
                            recipientPersonalAccount.setBalance(recipientPersonalAccount.getBalance() + amount);

                            // 保存账户变更
                            saveAccounts();

                            // 记录转账信息
                            recordTransfer(loggedInUsername, recipientUsername, amount);

                            JOptionPane.showMessageDialog(transferAccounts.this, "转出资金成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                            dispose(); // 关闭对话框

                        } else {
                            JOptionPane.showMessageDialog(transferAccounts.this, "转入账户不存在", "错误", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                    } else {
                        JOptionPane.showMessageDialog(transferAccounts.this, "该账户类型不支持转账", "错误", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(transferAccounts.this, "请输入有效的金额", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); // 关闭对话框
            }
        });

        // 添加组件
        panel.add(recipientUsernameLabel);
        panel.add(recipientUsernameField);
        panel.add(amountLabel);
        panel.add(amountField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(confirmButton);
        panel.add(cancelButton);

        add(panel);
    }

    // 从 CSV 中获取账户信息
    private AccountModel getAccount(String username, String password) {
        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username) && account.getPassword().equals(password)) {
                return account;
            }
        }
        return null;
    }

    // 从 CSV 中获取账户信息，只通过用户名查找
    private AccountModel findAccountByUsername(String username) {
        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username)) {
                return account;
            }
        }
        return null;
    }

    // 保存账户信息
    private void saveAccounts() {
        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        UserRegistrationCSVExporter.saveToCSV(accounts, false);
    }

    private void recordTransfer(String senderUsername, String recipientUsername, double amount) {
        String filePath = "transactions.csv";
        try (FileWriter fw = new FileWriter(filePath, true)) {
            // 获取当前时间
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = sdf.format(now);
    
            // 记录转出方的交易
            fw.write(senderUsername + ",转出," + amount + "," + recipientUsername + "," + formattedDateTime + "\n");
    
            // 记录转入方的交易
            fw.write(recipientUsername + ",转入," + amount + "," + senderUsername + "," + formattedDateTime + "\n");
    
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "记录转账交易时发生错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}