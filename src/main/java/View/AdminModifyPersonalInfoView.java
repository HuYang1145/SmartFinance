package View;



import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import AccountModel.AccountModel;

public class AdminModifyPersonalInfoView extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField genderField;
    private JTextField addressField;
    private JComboBox<String> accountStatusComboBox;
    private JPasswordField adminPasswordField;
    private JButton confirmButton;

    public AdminModifyPersonalInfoView(Consumer<AdminModifyPersonalInfoView> onConfirm) {
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
        confirmButton.addActionListener(e -> onConfirm.accept(this));

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

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);

        setVisible(true);
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

    public String getUsername() {
        return usernameField.getText();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public String getPhone() {
        return phoneField.getText();
    }

    public String getEmail() {
        return emailField.getText();
    }

    public String getGender() {
        return genderField.getText();
    }

    public String getAddress() {
        return addressField.getText();
    }

    public String getAccountStatus() {
        return (String) accountStatusComboBox.getSelectedItem();
    }

    public String getAdminPassword() {
        return new String(adminPasswordField.getPassword());
    }

    public void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    public void close() {
        dispose();
    }
}