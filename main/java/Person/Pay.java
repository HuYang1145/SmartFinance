package Person;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import Model.AccountModel;
import Model.PersonalAccount;
import Model.UserRegistrationCSVExporter;
import Model.UserSession;

public class Pay extends JDialog {
    private JTextField merchantNameField, paymentAmountField;
    private JPasswordField passwordField;
    private JButton confirmButton, cancelButton;

    public Pay(Dialog owner) {
        super(owner, "Pay", true);
        setSize(420, 280);
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 245, 245));

        // 标题
        JLabel titleLabel = new JLabel("Make a Payment");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 60, 120));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(titleLabel, BorderLayout.NORTH);

        // 表单面板
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel merchantNameLabel = new JLabel("Merchant Name:");
        merchantNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        panel.add(merchantNameLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        merchantNameField = new JTextField();
        merchantNameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        merchantNameField.setPreferredSize(new Dimension(180, 28));
        panel.add(merchantNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        JLabel paymentAmountLabel = new JLabel("Payment Amount:");
        paymentAmountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        panel.add(paymentAmountLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        paymentAmountField = new JTextField();
        paymentAmountField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        paymentAmountField.setPreferredSize(new Dimension(180, 28));
        panel.add(paymentAmountField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        panel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(180, 28));
        panel.add(passwordField, gbc);

        add(panel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setBackground(new Color(245, 245, 245));

        confirmButton = new JButton("Confirm");
        confirmButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setBackground(new Color(30, 60, 120));
        confirmButton.setFocusPainted(false);
        confirmButton.setBorderPainted(false);

        cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cancelButton.setBackground(new Color(200, 200, 200));
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 事件绑定（不变）
        confirmButton.addActionListener((ActionEvent e) -> {
            String merchantName = merchantNameField.getText();
            String paymentAmountText = paymentAmountField.getText();
            char[] passwordChars = passwordField.getPassword();
            String password = new String(passwordChars);

            if (merchantName.isEmpty() || paymentAmountText.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(Pay.this, "All fields cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double paymentAmount = Double.parseDouble(paymentAmountText);
                if (paymentAmount <= 0) {
                    JOptionPane.showMessageDialog(Pay.this, "Please enter a valid amount", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String loggedInUsername = UserSession.getCurrentUsername();
                AccountModel loggedInAccount = getAccount(loggedInUsername, password);
                if (loggedInAccount == null) {
                    JOptionPane.showMessageDialog(Pay.this, "Incorrect password", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (loggedInAccount instanceof PersonalAccount personalAccount) {
                    if (personalAccount.getBalance() < paymentAmount) {
                        JOptionPane.showMessageDialog(Pay.this, "Insufficient balance to make this payment", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    personalAccount.setBalance(personalAccount.getBalance() - paymentAmount);
                    UserRegistrationCSVExporter.saveToCSV(getAccounts(), false);
                    recordPayment(loggedInUsername, merchantName, paymentAmount);

                    JOptionPane.showMessageDialog(Pay.this, "Payment successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(Pay.this, "Payment is not supported for this account type", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(Pay.this, "Please enter a valid amount", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener((ActionEvent e) -> dispose());
    }

    private AccountModel getAccount(String username, String password) {
        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username) && account.getPassword().equals(password)) {
                return account;
            }
        }
        return null;
    }

    private List<AccountModel> getAccounts() {
        return UserRegistrationCSVExporter.readFromCSV();
    }

    private void recordPayment(String payerUsername, String merchantName, double paymentAmount) {
        String filePath = "transactions.csv";
        try (FileWriter fw = new FileWriter(filePath, true)) {
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String formattedDateTime = sdf.format(now);

            fw.write(payerUsername + ",Transfer Out," + "-" + paymentAmount + "," + formattedDateTime + "," + merchantName + "\n");
            fw.write(merchantName + ",Transfer In," + paymentAmount + "," + formattedDateTime + "," + payerUsername + "\n");

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error recording payment transaction: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
