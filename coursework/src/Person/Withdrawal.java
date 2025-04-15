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

public class Withdrawal extends JDialog {
    private JTextField amountField;
    private JPasswordField passwordField;
    private JButton confirmButton;
    private JButton cancelButton;

    private String currentUsername; // 用于存储当前用户的用户名

    public Withdrawal(Dialog owner, String username) {
        super(owner, "Withdrawal", true); // 对话框标题
        this.currentUsername = username;
        setSize(300, 180);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        JLabel amountLabel = new JLabel("Withdrawal Amount:"); // 标签
        amountField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:"); // 标签
        passwordField = new JPasswordField();

        confirmButton = new JButton("Confirm"); // 按钮文本
        cancelButton = new JButton("Cancel"); // 按钮文本

        confirmButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String amountText = amountField.getText();
                char[] passwordChars = passwordField.getPassword();
                String enteredPassword = new String(passwordChars);

                if (amountText.isEmpty() || enteredPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(Withdrawal.this, "Amount and password cannot be empty", "Error", JOptionPane.ERROR_MESSAGE); // 消息
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

                    if (!currentUserAccount.getPassword().equals(enteredPassword)) {
                        JOptionPane.showMessageDialog(Withdrawal.this, "Incorrect password", "Error", JOptionPane.ERROR_MESSAGE); // 消息
                        return;
                    }

                    if (currentUserAccount instanceof PersonalAccount) {
                        PersonalAccount personalAccount = (PersonalAccount) currentUserAccount;
                        if (personalAccount.getBalance() < amount) {
                            JOptionPane.showMessageDialog(Withdrawal.this, "Insufficient account balance", "Error", JOptionPane.ERROR_MESSAGE); // 消息
                            return;
                        }

                        personalAccount.setBalance(personalAccount.getBalance() - amount);
                        UserRegistrationCSVExporter.saveToCSV(accounts, false); // 保存更新后的账户信息

                        recordWithdrawal(currentUsername, amount); // 记录提款交易

                        JOptionPane.showMessageDialog(Withdrawal.this, "Successfully withdrew " + amount + " RMB", "Success", JOptionPane.INFORMATION_MESSAGE); // 消息和标题
                        dispose(); // 关闭对话框
                    } else {
                        JOptionPane.showMessageDialog(Withdrawal.this, "Withdrawal is not supported for this account type", "Error", JOptionPane.ERROR_MESSAGE); // 消息
                    }

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(Withdrawal.this, "Please enter a valid amount", "Error", JOptionPane.ERROR_MESSAGE); // 消息
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String formattedDateTime = sdf.format(now);

            // 记录提款交易，格式与存款相同
            fw.write(username + ",Withdrawal," + amount + "," + formattedDateTime + ",ATM\n");

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error recording withdrawal transaction: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); // 消息
            e.printStackTrace();
        }
    }
}