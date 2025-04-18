package Person;

import Model.AccountModel;
import Model.PersonalAccount;
import Model.UserRegistrationCSVExporter;
import Model.UserSession;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;

public class Pay extends JDialog {
    private JTextField merchantNameField, paymentAmountField;
    private JPasswordField passwordField;
    private JButton confirmButton, cancelButton;

    public Pay(Dialog owner) {
        super(owner, "Pay", true); // Changed title to "Pay"
        setSize(300, 200);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 创建面板和布局
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

        JLabel merchantNameLabel = new JLabel("Merchant Name:"); // Changed label to "Merchant Name"
        merchantNameField = new JTextField();
        JLabel paymentAmountLabel = new JLabel("Payment Amount:"); // Changed label to "Payment Amount"
        paymentAmountField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        confirmButton = new JButton("Confirm");
        cancelButton = new JButton("Cancel");

        confirmButton.addActionListener((ActionEvent e) -> {
            String merchantName = merchantNameField.getText(); // Renamed variable
            String paymentAmountText = paymentAmountField.getText(); // Renamed variable
            char[] passwordChars = passwordField.getPassword();
            String password = new String(passwordChars);
            
            if (merchantName.isEmpty() || paymentAmountText.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(Pay.this, "All fields cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                double paymentAmount = Double.parseDouble(paymentAmountText); // Renamed variable
                if (paymentAmount <= 0) {
                    JOptionPane.showMessageDialog(Pay.this, "Please enter a valid amount", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // 使用 UserSession 获取当前登录用户名 (付款方)
                String loggedInUsername = UserSession.getCurrentUsername();
                
                // 验证用户账户余额和密码
                AccountModel loggedInAccount = getAccount(loggedInUsername, password);
                if (loggedInAccount == null) {
                    JOptionPane.showMessageDialog(Pay.this, "Incorrect password", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (loggedInAccount instanceof PersonalAccount personalAccount) {
                    
                    if (personalAccount.getBalance() < paymentAmount) { // Renamed variable
                        JOptionPane.showMessageDialog(Pay.this, "Insufficient balance to make this payment", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    // 执行付款逻辑
                    personalAccount.setBalance(personalAccount.getBalance() - paymentAmount); // Renamed variable
                    UserRegistrationCSVExporter.saveToCSV(getAccounts(), false); // 保存更新后的账户信息
                    
                    // 记录付款信息
                    recordPayment(loggedInUsername, merchantName, paymentAmount); // Renamed method and parameters
                    
                    JOptionPane.showMessageDialog(Pay.this, "Payment successful!", "Success", JOptionPane.INFORMATION_MESSAGE); // Changed message
                    dispose(); // 关闭对话框
                    
                } else {
                    JOptionPane.showMessageDialog(Pay.this, "Payment is not supported for this account type", "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(Pay.this, "Please enter a valid amount", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener((ActionEvent e) -> {
            dispose(); // 关闭对话框
        });

        // 添加组件
        panel.add(merchantNameLabel); // Renamed label
        panel.add(merchantNameField); // Renamed field
        panel.add(paymentAmountLabel); // Renamed label
        panel.add(paymentAmountField); // Renamed field
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

    // 从 CSV 中获取所有账户信息
    private List<AccountModel> getAccounts() {
        return UserRegistrationCSVExporter.readFromCSV();
    }

    // 保存账户信息
    private void saveAccounts() {
        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        UserRegistrationCSVExporter.saveToCSV(accounts, false);
    }

    private void recordPayment(String payerUsername, String merchantName, double paymentAmount) { // Renamed method and parameters
        String filePath = "transactions.csv";
        try (FileWriter fw = new FileWriter(filePath, true)) {
            // 获取当前时间
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String formattedDateTime = sdf.format(now);

            // CSV 格式：user name, operation performed, amount, payment time, merchant name
            // 记录付款方的交易 (Transfer Out)
            fw.write(payerUsername + ",Transfer Out," + "-" + paymentAmount + "," + formattedDateTime + "," + merchantName + "\n");

            // 记录收款方的交易 (Transfer In) - 这里假设收款方是商户，用户名就是商户名
            // 如果需要更复杂的商户账户管理，这部分可能需要调整
            fw.write(merchantName + ",Transfer In," + paymentAmount + "," + formattedDateTime + "," + payerUsername + "\n");

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error recording payment transaction: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}