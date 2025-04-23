package AdminController;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import AccountController.UserRegistrationCSVExporter;
import AccountModel.AccountModel;
import AccountModel.AdminAccount;
import AccountModel.UserSession;

public class PersonModifyService extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField genderField;
    private JTextField addressField;
    private JComboBox<String> accountStatusComboBox;
    private JButton confirmButton;
    private JPasswordField adminPasswordField;

    public PersonModifyService() {
        setTitle("Modify Customer Information");
        setSize(400, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JLabel usernameLabel = new JLabel("Username (cannot be modified):");
        usernameField = new JTextField(20);
        usernameField.setEditable(false);

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);

        JLabel phoneLabel = new JLabel("Phone Number:");
        phoneField = new JTextField(20);

        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField(20);

        JLabel genderLabel = new JLabel("Gender:");
        genderField = new JTextField(20);

        JLabel addressLabel = new JLabel("Address:");
        addressField = new JTextField(20);

        JLabel accountStatusLabel = new JLabel("Account Status:");
        accountStatusComboBox = new JComboBox<>(new String[]{"ACTIVE", "FROZEN"});
        accountStatusComboBox.setPreferredSize(new Dimension(200, 25));

        JLabel adminPasswordLabel = new JLabel("Admin Password:");
        adminPasswordField = new JPasswordField(20);

        confirmButton = new JButton("Confirm Modification");

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(9, 2, 10, 10));
        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(phoneLabel);
        panel.add(phoneField);
        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(genderLabel);
        panel.add(genderField);
        panel.add(addressLabel);
        panel.add(addressField);
        panel.add(accountStatusLabel);
        panel.add(accountStatusComboBox);
        panel.add(adminPasswordLabel);
        panel.add(adminPasswordField);
        panel.add(new JLabel(""));
        panel.add(confirmButton);

        confirmButton.addActionListener((ActionEvent e) -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String phone = phoneField.getText();
            String email = emailField.getText();
            String gender = genderField.getText();
            String address = addressField.getText();
            String accountStatusStr = (String) accountStatusComboBox.getSelectedItem();
            String adminPassword = new String(adminPasswordField.getPassword());
            
            if (!isAdminPasswordValid(adminPassword)) {
                JOptionPane.showMessageDialog(null, "Incorrect admin password!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            AccountModel account = AdminModifyService.getAccount(username, password);
            
            if (account != null) {
                boolean updatedInfo = AdminModifyService.updateCustomerInfo(username, password, phone, email, gender, address);
                try {
                    AccountModel.AccountStatus accountStatus = AccountModel.AccountStatus.valueOf(accountStatusStr.toUpperCase());
                    AdminModifyService.modifyAccountStatus(username, accountStatus);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid account status: " + accountStatusStr, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (updatedInfo) {
                    JOptionPane.showMessageDialog(null, "Customer information updated successfully!");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to update customer information!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "User does not exist or password incorrect!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);

        setVisible(true);
    }

    private boolean isAdminPasswordValid(String adminPassword) {
        String currentAdminUsername = UserSession.getCurrentUsername();

        if (currentAdminUsername == null) {
            JOptionPane.showMessageDialog(null, "Admin not logged in!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        java.util.List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();

        for (AccountModel account : accounts) {
            if (account.getUsername().equals(currentAdminUsername) && account instanceof AdminAccount) {
                if (account.getPassword().equals(adminPassword)) {
                    return true;
                } else {
                    JOptionPane.showMessageDialog(null, "Incorrect password!", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }

        JOptionPane.showMessageDialog(null, "Admin account not found!", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    public void setAccountInfo(AccountModel account) {
        if (account != null) {
            usernameField.setText(account.getUsername());
            passwordField.setText(account.getPassword());
            phoneField.setText(account.getPhone());
            emailField.setText(account.getEmail());
            genderField.setText(account.getGender());
            addressField.setText(account.getAddress());
            accountStatusComboBox.setSelectedItem(account.getAccountStatus().name());
        }
    }
}