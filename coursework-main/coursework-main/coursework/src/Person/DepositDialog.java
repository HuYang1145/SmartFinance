package Person;

import model.AccountModel;
import model.UserRegistrationCSVExporter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DepositDialog extends JDialog {
    private JTextField amountField;
    private JPasswordField passwordField;
    private JButton confirmButton;
    private JButton cancelButton;

    private String currentUsername; // 用于存储当前用户的用户名

    public DepositDialog(Dialog owner, String username) { // 父类改为 Dialog
        super(owner, "存款", true); // true 表示是模态对话框
        this.currentUsername = username; // 保存当前用户名
        setSize(300, 200);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        JLabel amountLabel = new JLabel("存入金额:");
        amountField = new JTextField();

        JLabel passwordLabel = new JLabel("密码:");
        passwordField = new JPasswordField();

        confirmButton = new JButton("确认");
        cancelButton = new JButton("取消");

        confirmButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String amountText = amountField.getText();
                char[] passwordChars = passwordField.getPassword();
                String enteredPassword = new String(passwordChars);
            
                if (amountText.isEmpty() || enteredPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(DepositDialog.this, "金额和密码不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            
                try {
                    double amount = Double.parseDouble(amountText);
                    // 根据用户名从 CSV 文件中读取用户信息
                    List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
                    String storedPassword = null;
                    AccountModel currentUserAccount = null; // 用于存储当前用户的账户对象
            
                    System.out.println("当前用户名: " + currentUsername); // 调试打印
            
                    for (AccountModel account : accounts) {
                        if (account.getUsername().equals(currentUsername)) {
                            storedPassword = account.getPassword();
                            currentUserAccount = account; // 找到当前用户
                            System.out.println("找到用户账户: " + currentUserAccount); // 调试打印
                            System.out.println("初始余额: " + currentUserAccount.getBalance()); // 调试打印
                            break;
                        }
                    }
            
                    if (storedPassword != null) {
                        // 比较输入的密码和存储的密码
                        if (enteredPassword.equals(storedPassword)) {
                            // 将转入资金记录到 transactions.csv 文件
                            recordDeposit(currentUsername, amount);
            
                            // 更新 accounts.csv 文件中的余额
                            if (currentUserAccount != null) {
                                double currentBalance = currentUserAccount.getBalance();
                                System.out.println("存入金额: " + amount); // 调试打印
                                System.out.println("更新前余额: " + currentBalance); // 调试打印
                                currentUserAccount.setBalance(currentBalance + amount);
                                System.out.println("更新后余额 (内存中): " + currentUserAccount.getBalance()); // 调试打印
            
                                // 将更新后的账户列表写回 accounts.csv 文件
                                UserRegistrationCSVExporter.saveToCSV(accounts, false);
                                System.out.println("账户信息已保存到 CSV 文件"); // 调试打印
            
                                JOptionPane.showMessageDialog(DepositDialog.this, "成功转入资金 " + amount + " 元，账户余额已更新", "成功", JOptionPane.INFORMATION_MESSAGE);
                                dispose(); // 关闭对话框
                            } else {
                                JOptionPane.showMessageDialog(DepositDialog.this, "找不到该用户名的账户信息", "错误", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(DepositDialog.this, "密码错误", "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(DepositDialog.this, "找不到该用户名的账户信息", "错误", JOptionPane.ERROR_MESSAGE);
                    }
            
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(DepositDialog.this, "请输入有效的金额", "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    // 清空密码字段，增强安全性
                    passwordField.setText("");
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); // 关闭对话框
            }
        });

        panel.add(amountLabel);
        panel.add(amountField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(confirmButton);
        panel.add(cancelButton);

        add(panel);
    }

    private void recordDeposit(String accountNumber, double amount) {
        String filePath = "transactions.csv"; // 文件名和路径，可以根据需要修改
        try (FileWriter fw = new FileWriter(filePath, true)) { // true 表示追加写入
            // 获取当前时间
            Date now = new Date();
            // 定义日期和时间格式
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 你可以自定义格式
            // 将 Date 对象格式化为字符串
            String formattedDateTime = sdf.format(now);
            // CSV 格式：用户名,进行的操作,转入或转出的金额,转入或转出的目标账户,时间
            fw.write(accountNumber + ",存款," + amount + "," + accountNumber + "," + formattedDateTime + "\n");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "记录交易时发生错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}