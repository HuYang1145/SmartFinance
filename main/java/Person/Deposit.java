package Person;

import javax.swing.*;

import Model.AccountModel;

import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import Model.UserRegistrationCSVExporter;

public class Deposit extends JDialog {
    private JTextField amountField;
    private JPasswordField passwordField;
    private JButton confirmButton;
    private JButton cancelButton;
    private String currentUsername;

    public Deposit(Dialog owner, String username) {
        super(owner, "Deposit", true);
        this.currentUsername = username;
        setSize(420, 280);
        setResizable(false);
        getContentPane().setBackground(new Color(245, 245, 245));
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 标题
        JLabel titleLabel = new JLabel("Deposit Funds");
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
        JLabel amountLabel = new JLabel("Deposit Amount:");
        amountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        panel.add(amountLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        amountField = new JTextField();
        amountField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        amountField.setPreferredSize(new Dimension(180, 28));
        panel.add(amountField, gbc);

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

        // 按钮事件逻辑（原样保留）
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

                    if (storedPassword != null) {
                        if (enteredPassword.equals(storedPassword)) {
                            recordDeposit(currentUsername, amount);

                            if (currentUserAccount != null) {
                                double currentBalance = currentUserAccount.getBalance();
                                currentUserAccount.setBalance(currentBalance + amount);
                                UserRegistrationCSVExporter.saveToCSV(accounts, false);

                                JOptionPane.showMessageDialog(Deposit.this, "Successfully deposited " + amount + " RMB, account balance updated", "Success", JOptionPane.INFORMATION_MESSAGE);
                                dispose();
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
                    passwordField.setText("");
                }
            }
        });

        cancelButton.addActionListener(e -> dispose());
    }

    private void recordDeposit(String username, double amount) {
        String filePath = "transactions.csv";
        try (FileWriter fw = new FileWriter(filePath, true)) {
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String formattedDateTime = sdf.format(now);
            fw.write(username + ",Deposit," + amount + "," + formattedDateTime + ",ATM\n");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error recording transaction: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
