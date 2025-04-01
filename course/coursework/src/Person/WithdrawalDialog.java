package Person;

import model.AccountModel;
import model.UserRegistrationCSVExporter;
import model.PersonalAccount;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class WithdrawalDialog extends JDialog {
    private JTextField amountField;
    private JPasswordField passwordField;
    private JButton confirmButton;
    private JButton cancelButton;

    private String currentUsername; // 用于存储当前用户的用户名

    public WithdrawalDialog(JFrame frame, String username) {
        super(frame, "提款", true);
        this.currentUsername = username;
        setSize(300, 180);
        setLocationRelativeTo(frame);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        JLabel amountLabel = new JLabel("提款金额:");
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
                    JOptionPane.showMessageDialog(WithdrawalDialog.this, "金额和密码不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    double amount = Double.parseDouble(amountText);
                    if (amount <= 0) {
                        JOptionPane.showMessageDialog(WithdrawalDialog.this, "请输入有效的金额", "错误", JOptionPane.ERROR_MESSAGE);
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
                        JOptionPane.showMessageDialog(WithdrawalDialog.this, "找不到该用户名的账户信息", "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (!currentUserAccount.getPassword().equals(enteredPassword)) {
                        JOptionPane.showMessageDialog(WithdrawalDialog.this, "密码错误", "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (currentUserAccount instanceof PersonalAccount) {
                        PersonalAccount personalAccount = (PersonalAccount) currentUserAccount;
                        if (personalAccount.getBalance() < amount) {
                            JOptionPane.showMessageDialog(WithdrawalDialog.this, "账户余额不足", "错误", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        personalAccount.setBalance(personalAccount.getBalance() - amount);
                        UserRegistrationCSVExporter.saveToCSV(accounts, false); // 保存更新后的账户信息

                        recordWithdrawal(currentUsername, amount); // 记录提款交易

                        JOptionPane.showMessageDialog(WithdrawalDialog.this, "成功提取 " + amount + " 元", "成功", JOptionPane.INFORMATION_MESSAGE);
                        dispose(); // 关闭对话框
                    } else {
                        JOptionPane.showMessageDialog(WithdrawalDialog.this, "该账户类型不支持提款", "错误", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(WithdrawalDialog.this, "请输入有效的金额", "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    passwordField.setText(""); // 清空密码字段
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

    private void recordWithdrawal(String username, double amount) {
        String filePath = "transactions.csv";
        try (FileWriter fw = new FileWriter(filePath, true)) {
            // 获取当前时间
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = sdf.format(now);

            // 记录提款交易
            fw.write(username + ",提款," + amount + "," + username + "," + formattedDateTime + "\n");

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "记录提款交易时发生错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}