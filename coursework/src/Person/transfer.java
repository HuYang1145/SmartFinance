<<<<<<< HEAD
package Person;

import model.AccountModel;
import model.PersonalAccount;
import model.UserRegistrationCSVExporter;
import model.Transaction;
import model.CsvDataManager;
import UI.AlertPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class transfer extends JDialog {
    private JTextField recipientUsernameField, amountField;
    private JPasswordField passwordField;
    private JButton confirmButton, cancelButton;

    public transfer(Dialog owner) {
        super(owner, "Transfer Funds", true);
        setSize(300, 200);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 创建面板和布局
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

        JLabel recipientUsernameLabel = new JLabel("Recipient Username:");
        recipientUsernameField = new JTextField();
        JLabel amountLabel = new JLabel("Transfer Amount:");
        amountField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        confirmButton = new JButton("Confirm");
        cancelButton = new JButton("Cancel");

        confirmButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String recipientUsername = recipientUsernameField.getText();
                String amountText = amountField.getText();
                char[] passwordChars = passwordField.getPassword();
                String password = new String(passwordChars);

                if (recipientUsername.isEmpty() || amountText.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(transfer.this, "All fields cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    double amount = Double.parseDouble(amountText);
                    if (amount <= 0) {
                        JOptionPane.showMessageDialog(transfer.this, "Please enter a valid amount", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // 使用 UserSession 获取当前登录用户名 (转出方)
                    String loggedInUsername = model.UserSession.getCurrentUsername();

                    // 验证用户账户余额和密码
                    AccountModel loggedInAccount = getAccount(loggedInUsername, password);
                    if (loggedInAccount == null) {
                        JOptionPane.showMessageDialog(transfer.this, "Incorrect password", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (loggedInAccount instanceof PersonalAccount) {
                        PersonalAccount personalAccount = (PersonalAccount) loggedInAccount;

                        if (personalAccount.getBalance() < amount) {
                            JOptionPane.showMessageDialog(transfer.this, "Insufficient balance to transfer this amount", "Error", JOptionPane.ERROR_MESSAGE);
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

                            JOptionPane.showMessageDialog(transfer.this, "Funds transferred successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                            dispose(); // 关闭对话框

                            // ===== 触发异常检测 =====
                            CsvDataManager csvManager = new CsvDataManager();
                            List<Transaction> transactions = csvManager.readTransactions().stream()
                                    .filter(t -> t.getUsername().equals(loggedInUsername))
                                    .collect(Collectors.toList());

                            // 调试输出：打印读取到的交易记录
                            System.out.println("读取到的交易记录: " + transactions);

                            TransactionAnalyzer analyzer = new TransactionAnalyzer();
                            List<Transaction> abnormal = analyzer.detectAbnormal(transactions);

                            // 调试输出：打印检测到的异常交易
                            System.out.println("检测到的异常交易: " + abnormal);

                            if (abnormal != null && !abnormal.isEmpty()) {
                                new AlertPanel(abnormal, "检测到交易异常").setVisible(true);
                            }
                        } else {
<<<<<<< HEAD:coursework/src/Person/transferAccounts.java
                            JOptionPane.showMessageDialog(transferAccounts.this, "转入账户不存在", "错误", JOptionPane.ERROR_MESSAGE);
=======
                            JOptionPane.showMessageDialog(transfer.this, "Recipient account does not exist", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
>>>>>>> 4f9a9c84ae715ab848f6680dc125ee58391d2736:coursework/src/Person/transfer.java
                        }
                    } else {
                        JOptionPane.showMessageDialog(transfer.this, "Transfer is not supported for this account type", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(transfer.this, "Please enter a valid amount", "Error", JOptionPane.ERROR_MESSAGE);
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
<<<<<<< HEAD:coursework/src/Person/transferAccounts.java
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d HH:mm");
            String formattedDateTime = sdf.format(now);

            // 记录转出方的交易
            fw.write(senderUsername + ",转出," + amount + "," + recipientUsername + "," + formattedDateTime + "\n");

            // 记录转入方的交易
            fw.write(recipientUsername + ",转入," + amount + "," + senderUsername + "," + formattedDateTime + "\n");
            fw.flush();
=======
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String formattedDateTime = sdf.format(now);

            // 记录转出方的交易
            fw.write(senderUsername + ",Transfer Out," + "-" + amount + "," + formattedDateTime + "," + recipientUsername + "\n");

            // 记录转入方的交易
            fw.write(recipientUsername + ",Transfer In," + amount + "," + formattedDateTime + "," + senderUsername + "\n");

>>>>>>> 4f9a9c84ae715ab848f6680dc125ee58391d2736:coursework/src/Person/transfer.java
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error recording transfer transaction: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

=======
package Person;

import model.AccountModel;
import model.PersonalAccount;
import model.UserRegistrationCSVExporter;
import model.Transaction;
import model.CsvDataManager;
import UI.AlertPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class transfer extends JDialog {
    private JTextField recipientUsernameField, amountField;
    private JPasswordField passwordField;
    private JButton confirmButton, cancelButton;

    public transfer(Dialog owner) {
        super(owner, "Transfer Funds", true);
        setSize(300, 200);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 创建面板和布局
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

        JLabel recipientUsernameLabel = new JLabel("Recipient Username:");
        recipientUsernameField = new JTextField();
        JLabel amountLabel = new JLabel("Transfer Amount:");
        amountField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        confirmButton = new JButton("Confirm");
        cancelButton = new JButton("Cancel");

        confirmButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String recipientUsername = recipientUsernameField.getText();
                String amountText = amountField.getText();
                char[] passwordChars = passwordField.getPassword();
                String password = new String(passwordChars);

                if (recipientUsername.isEmpty() || amountText.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(transfer.this, "All fields cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    double amount = Double.parseDouble(amountText);
                    if (amount <= 0) {
                        JOptionPane.showMessageDialog(transfer.this, "Please enter a valid amount", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // 使用 UserSession 获取当前登录用户名 (转出方)
                    String loggedInUsername = model.UserSession.getCurrentUsername();

                    // 验证用户账户余额和密码
                    AccountModel loggedInAccount = getAccount(loggedInUsername, password);
                    if (loggedInAccount == null) {
                        JOptionPane.showMessageDialog(transfer.this, "Incorrect password", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (loggedInAccount instanceof PersonalAccount) {
                        PersonalAccount personalAccount = (PersonalAccount) loggedInAccount;

                        if (personalAccount.getBalance() < amount) {
                            JOptionPane.showMessageDialog(transfer.this, "Insufficient balance to transfer this amount", "Error", JOptionPane.ERROR_MESSAGE);
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

                            JOptionPane.showMessageDialog(transfer.this, "Funds transferred successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                            dispose(); // 关闭对话框

                            // ===== 触发异常检测 =====
                            CsvDataManager csvManager = new CsvDataManager();
                            List<Transaction> transactions = csvManager.readTransactions().stream()
                                    .filter(t -> t.getUsername().equals(loggedInUsername))
                                    .collect(Collectors.toList());

                            // 调试输出：打印读取到的交易记录
                            System.out.println("读取到的交易记录: " + transactions);

                            TransactionAnalyzer analyzer = new TransactionAnalyzer();
                            List<Transaction> abnormal = analyzer.detectAbnormal(transactions);

                            // 调试输出：打印检测到的异常交易
                            System.out.println("检测到的异常交易: " + abnormal);

                            if (abnormal != null && !abnormal.isEmpty()) {
                                new AlertPanel(abnormal, "检测到交易异常").setVisible(true);
                            }
                        } else {
<<<<<<<< HEAD:coursework/src/Person/transferAccounts.java
                            JOptionPane.showMessageDialog(transferAccounts.this, "转入账户不存在", "错误", JOptionPane.ERROR_MESSAGE);
========
                            JOptionPane.showMessageDialog(transfer.this, "Recipient account does not exist", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
>>>>>>>> 382f4a22ceb10164f9c36fd7cadf0016088cd827:coursework/src/Person/transfer.java
                        }
                    } else {
                        JOptionPane.showMessageDialog(transfer.this, "Transfer is not supported for this account type", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(transfer.this, "Please enter a valid amount", "Error", JOptionPane.ERROR_MESSAGE);
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
<<<<<<<< HEAD:coursework/src/Person/transferAccounts.java
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d HH:mm");
            String formattedDateTime = sdf.format(now);

            // 记录转出方的交易
            fw.write(senderUsername + ",转出," + amount + "," + recipientUsername + "," + formattedDateTime + "\n");

            // 记录转入方的交易
            fw.write(recipientUsername + ",转入," + amount + "," + senderUsername + "," + formattedDateTime + "\n");
            fw.flush();
========
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String formattedDateTime = sdf.format(now);

            // 记录转出方的交易
            fw.write(senderUsername + ",Transfer Out," + "-" + amount + "," + formattedDateTime + "," + recipientUsername + "\n");

            // 记录转入方的交易
            fw.write(recipientUsername + ",Transfer In," + amount + "," + formattedDateTime + "," + senderUsername + "\n");

>>>>>>>> 382f4a22ceb10164f9c36fd7cadf0016088cd827:coursework/src/Person/transfer.java
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error recording transfer transaction: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

>>>>>>> 382f4a22ceb10164f9c36fd7cadf0016088cd827
}