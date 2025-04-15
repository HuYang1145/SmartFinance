package Person;

import model.AccountModel;
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

public class DepositDialog extends JDialog {
    private JTextField amountField;
    private JPasswordField passwordField;
    private JButton confirmButton, cancelButton;
    private String currentUsername;

    public DepositDialog(Dialog owner, String username) {
        super(owner, "存款", true);
        this.currentUsername = username;
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
                    List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
                    String storedPassword = null;
                    AccountModel currentUserAccount = null;

                    for (AccountModel account : accounts) {
                        if (account.getUsername().equals(currentUsername)) {
                            storedPassword = account.getPassword();
                            currentUserAccount = account;
                            break;
                        }
                    }

                    if (storedPassword != null && enteredPassword.equals(storedPassword)) {
                        recordDeposit(currentUsername, amount);

                        if (currentUserAccount != null) {
                            currentUserAccount.setBalance(currentUserAccount.getBalance() + amount);
                            UserRegistrationCSVExporter.saveToCSV(accounts, false);
                            JOptionPane.showMessageDialog(DepositDialog.this, "成功转入资金 " + amount + " 元", "成功", JOptionPane.INFORMATION_MESSAGE);
                            dispose();

                            // ===== 触发异常检测 =====
                            try {
                                CsvDataManager csvManager = new CsvDataManager();
                                List<Transaction> transactions = csvManager.readTransactions().stream()
                                        .filter(t -> t.getUsername().equals(currentUsername))
                                        .collect(Collectors.toList());

                                TransactionAnalyzer analyzer = new TransactionAnalyzer();
                                List<Transaction> abnormal = analyzer.detectAbnormal(transactions);

                                if (abnormal != null && !abnormal.isEmpty()) {
                                    new AlertPanel(abnormal, null).setVisible(true);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(DepositDialog.this, "密码错误", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(DepositDialog.this, "请输入有效的金额", "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    passwordField.setText("");
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
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
        try (FileWriter fw = new FileWriter("transactions.csv", true)) {
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            fw.write(accountNumber + ",存款," + amount + "," + accountNumber + "," + sdf.format(now) + "\n");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "记录交易失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}