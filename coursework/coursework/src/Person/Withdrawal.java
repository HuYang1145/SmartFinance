package Person;

import javax.swing.*;

import Model.AccountModel;
import Model.PersonalAccount;
import Model.UserRegistrationCSVExporter;

import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Withdrawal extends JDialog {
    private JTextField amountField;
    private JTextField merchantField; // 新增：用于输入商户名称的文本框
    private JPasswordField passwordField;
    private JButton confirmButton;
    private JButton cancelButton;

    private String currentUsername; // 用于存储当前用户的用户名

    public Withdrawal(Dialog owner, String username) {
        super(owner, "Withdrawal", true); // 对话框标题
        this.currentUsername = username;
        // 稍微增大对话框尺寸以容纳新输入框
        setSize(300, 220); // 原为 setSize(300, 180)
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 修改 GridLayout 行数以适应新行 (从 3 行改为 4 行: 金额, 商户, 密码, 按钮)
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10)); // 原为 GridLayout(3, 2, ...)

        JLabel amountLabel = new JLabel("Withdrawal Amount:"); // 标签
        amountField = new JTextField();

        // 商户名称标签和输入框
        JLabel merchantLabel = new JLabel("Merchant/Location:");
        merchantField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:"); // 标签
        passwordField = new JPasswordField();

        confirmButton = new JButton("Confirm"); // 按钮文本
        cancelButton = new JButton("Cancel"); // 按钮文本

        confirmButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String amountText = amountField.getText();
                String merchantName = merchantField.getText(); // 新增：获取商户名称输入
                char[] passwordChars = passwordField.getPassword();
                String enteredPassword = new String(passwordChars);

                // 修改非空检查，加入对商户名称的检查
                if (amountText.isEmpty() || merchantName.isEmpty() || enteredPassword.isEmpty()) {
                    // 修改提示信息
                    JOptionPane.showMessageDialog(Withdrawal.this, "Amount, Merchant/Location, and password cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    double amount = Double.parseDouble(amountText);
                    if (amount <= 0) {
                        JOptionPane.showMessageDialog(Withdrawal.this, "Please enter a valid amount", "Error", JOptionPane.ERROR_MESSAGE); // 消息
                        return;
                    }

                    List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
                    AccountModel currentUserAccount = null;

                    for (AccountModel account : accounts) {
                        if (account.getUsername().equals(currentUsername)) {
                            currentUserAccount = account;
                            break;
                        }
                    }

                    if (currentUserAccount == null) {
                        JOptionPane.showMessageDialog(Withdrawal.this, "Account information not found for this username", "Error", JOptionPane.ERROR_MESSAGE); // 消息
                        return;
                    }

                    // --- 密码验证 ---
                    if (!currentUserAccount.getPassword().equals(enteredPassword)) {
                        JOptionPane.showMessageDialog(Withdrawal.this, "Incorrect password", "Error", JOptionPane.ERROR_MESSAGE); // 消息
                        // 清空密码和商户字段，保留金额
                        passwordField.setText("");
                        merchantField.setText(""); // 如果密码错误，也清空商户名？可选
                        return;
                    }
                    // --- 密码验证结束 ---


                    if (currentUserAccount instanceof PersonalAccount) {
                        PersonalAccount personalAccount = (PersonalAccount) currentUserAccount;
                        if (personalAccount.getBalance() < amount) {
                            JOptionPane.showMessageDialog(Withdrawal.this, "Insufficient account balance", "Error", JOptionPane.ERROR_MESSAGE); // 消息
                            // 清空密码和商户字段
                            passwordField.setText("");
                            merchantField.setText("");
                            return;
                        }

                        // 执行取款逻辑
                        personalAccount.setBalance(personalAccount.getBalance() - amount);
                        UserRegistrationCSVExporter.saveToCSV(accounts, false); // 保存更新后的账户信息

                        // 修改调用 recordWithdrawal，传入获取到的 merchantName
                        recordWithdrawal(currentUsername, amount, merchantName); // 记录提款交易

                        JOptionPane.showMessageDialog(Withdrawal.this, "Successfully withdrew " + amount + " RMB from " + merchantName, "Success", JOptionPane.INFORMATION_MESSAGE); // 修改成功消息
                        dispose(); // 关闭对话框
                    } else {
                        JOptionPane.showMessageDialog(Withdrawal.this, "Withdrawal is not supported for this account type", "Error", JOptionPane.ERROR_MESSAGE); // 消息
                    }

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(Withdrawal.this, "Please enter a valid amount", "Error", JOptionPane.ERROR_MESSAGE); // 消息
                } finally {
                    passwordField.setText("");
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); // 关闭对话框
            }
        });

        // 调整添加组件的顺序以匹配 GridLayout
        panel.add(amountLabel);
        panel.add(amountField);
        panel.add(merchantLabel);     // 添加商户标签
        panel.add(merchantField);     // 添加商户输入框
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(confirmButton);
        panel.add(cancelButton);

        add(panel);
    }

    // 修改 recordWithdrawal 方法签名，接收 merchantName 参数
    private void recordWithdrawal(String username, double amount, String merchantName) {
        String filePath = "transactions.csv";
        // 使用 try-with-resources 确保 FileWriter 被正确关闭
        try (FileWriter fw = new FileWriter(filePath, true)) { // true 表示追加写入
            // 获取当前时间
            Date now = new Date();
            // 定义日期和时间格式
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            // 将 Date 对象格式化为字符串
            String formattedDateTime = sdf.format(now);

            // CSV 格式：用户名,操作类型,金额,支付时间,商户名
            fw.write(username + ",Withdrawal," + amount + "," + formattedDateTime + "," + merchantName + "\n");

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error recording withdrawal transaction: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); // 消息
            e.printStackTrace(); // 打印堆栈跟踪，便于调试
        }
    }
}
