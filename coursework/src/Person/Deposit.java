package Person;

import javax.swing.*;

import Model.AccountModel;
import Model.UserRegistrationCSVExporter;

import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Deposit extends JDialog {
    private JTextField amountField;
    private JPasswordField passwordField;
    private JButton confirmButton;
    private JButton cancelButton;

    private String currentUsername; // 用于存储当前用户的用户名

    public Deposit(Dialog owner, String username) { // 父类改为 Dialog
        super(owner, "Deposit", true); // true 表示是模态对话框
        this.currentUsername = username; // 保存当前用户名
        setSize(300, 200);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        JLabel amountLabel = new JLabel("Deposit Amount:");
        amountField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        confirmButton = new JButton("Confirm");
        cancelButton = new JButton("Cancel");

        confirmButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String amountText = amountField.getText();
                char[] passwordChars = passwordField.getPassword();
                String enteredPassword = new String(passwordChars);

                if (amountText.isEmpty() || enteredPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(Deposit.this, "Amount and password cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
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

                                JOptionPane.showMessageDialog(Deposit.this, "Successfully deposited " + amount + " RMB, account balance updated", "Success", JOptionPane.INFORMATION_MESSAGE);
                                dispose(); // 关闭对话框
                            } else {
                                JOptionPane.showMessageDialog(Deposit.this, "Account information not found for this username", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(Deposit.this, "Incorrect password", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(Deposit.this, "Account information not found for this username", "Error", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(Deposit.this, "Please enter a valid amount", "Error", JOptionPane.ERROR_MESSAGE);
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

    private void recordDeposit(String username, double amount) {
        String filePath = "transactions.csv"; // 文件名和路径，可以根据需要修改
        try (FileWriter fw = new FileWriter(filePath, true)) { // true 表示追加写入
            // 获取当前时间
            Date now = new Date();
            // 定义日期和时间格式
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            // 将 Date 对象格式化为字符串
            String formattedDateTime = sdf.format(now);
            // CSV 格式：用户名,进行的操作,转入或转出的金额,支付时间,商户名
            fw.write(username + ",Deposit," + amount + "," + formattedDateTime + ",ATM\n");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error recording transaction: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}