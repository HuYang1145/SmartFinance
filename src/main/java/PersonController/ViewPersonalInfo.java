package PersonController;

import javax.swing.*;

import AccountController.UserRegistrationCSVExporter;
import AccountModel.AccountModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ViewPersonalInfo extends JDialog {

    public ViewPersonalInfo(Dialog owner, String username) {
        super(owner, "Personal Information", true);
        setSize(400, 300);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(9, 2, 10, 10));

        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        AccountModel userAccount = null;

        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username)) {
                userAccount = account;
                break;
            }
        }

        if (userAccount != null) {
            add(new JLabel("Username:"));
            add(new JLabel(userAccount.getUsername()));

            add(new JLabel("Phone Number:"));
            add(new JLabel(userAccount.getPhone()));

            add(new JLabel("Email:"));
            add(new JLabel(userAccount.getEmail()));

            add(new JLabel("Gender:"));
            add(new JLabel(userAccount.getGender()));

            add(new JLabel("Address:"));
            add(new JLabel(userAccount.getAddress()));

            add(new JLabel("Creation Time:"));
            add(new JLabel(userAccount.getCreationTime()));

            add(new JLabel("Account Status:"));
            add(new JLabel(userAccount.getAccountStatus().name())); // 修复：使用 name() 转换为 String

            add(new JLabel("Account Type:"));
            add(new JLabel(userAccount.getAccountType()));

            JButton returnButton = new JButton("Return");
            returnButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                    SwingUtilities.invokeLater(() -> {
                        dispose();
                    });
                }
            });
            add(new JLabel(""));
            add(returnButton);
        } else {
            JOptionPane.showMessageDialog(this, "User information not found", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        }

        setVisible(true);
    }
}