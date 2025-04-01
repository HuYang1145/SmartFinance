package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import model.PersonalAccount;
import model.UserRegistrationCSVExporter;
import model.AccountModel;
import model.AccountValidator;
import model.AdminAccount;

public class AccountManagementUI extends JDialog {
    private JTextField usernameField, phoneField, emailField, addressField;
    private JPasswordField passwordField;
    private JComboBox<String> genderComboBox;
    private JButton loginButton, createButton;

    // 新增一个操作类型字段，用于区分是登录还是注册
    public AccountManagementUI(JFrame parent, String actionType) {
        super(parent, "欢迎登录", true);
        setSize(400, 350);  
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        if (actionType.equals("register")) {
            // 注册界面
            JLabel accountTypeLabel = new JLabel("账户类型:");
            JComboBox<String> accountTypeComboBox = new JComboBox<>(new String[]{"个人账户", "管理员账户"});
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(accountTypeLabel, gbc);
            gbc.gridx = 1;
            gbc.gridy = 0;
            add(accountTypeComboBox, gbc);

            // 用户名
            JLabel usernameLabel = new JLabel("用户名:");
            usernameField = new JTextField();
            gbc.gridx = 0;
            gbc.gridy = 1;
            add(usernameLabel, gbc);
            gbc.gridx = 1;
            gbc.gridy = 1;
            add(usernameField, gbc);

            // 密码
            JLabel passwordLabel = new JLabel("密码:");
            passwordField = new JPasswordField();
            gbc.gridx = 0;
            gbc.gridy = 2;
            add(passwordLabel, gbc);
            gbc.gridx = 1;
            gbc.gridy = 2;
            add(passwordField, gbc);

            // 手机号
            JLabel phoneLabel = new JLabel("手机号:");
            phoneField = new JTextField();
            gbc.gridx = 0;
            gbc.gridy = 3;
            add(phoneLabel, gbc);
            gbc.gridx = 1;
            gbc.gridy = 3;
            add(phoneField, gbc);

            // 邮箱
            JLabel emailLabel = new JLabel("邮箱:");
            emailField = new JTextField();
            gbc.gridx = 0;
            gbc.gridy = 4;
            add(emailLabel, gbc);
            gbc.gridx = 1;
            gbc.gridy = 4;
            add(emailField, gbc);

            // 性别
            JLabel genderLabel = new JLabel("性别:");
            genderComboBox = new JComboBox<>(new String[]{"男", "女"});
            gbc.gridx = 0;
            gbc.gridy = 5;
            add(genderLabel, gbc);
            gbc.gridx = 1;
            gbc.gridy = 5;
            add(genderComboBox, gbc);

            // 地址
            JLabel addressLabel = new JLabel("地址:");
            addressField = new JTextField();
            gbc.gridx = 0;
            gbc.gridy = 6;
            add(addressLabel, gbc);
            gbc.gridx = 1;
            gbc.gridy = 6;
            add(addressField, gbc);

            // 创建账户按钮
            createButton = new JButton("注册账户");
            createButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    createAccount(accountTypeComboBox.getSelectedItem().toString());  // 确保传递选择的账户类型
                }
            });
            gbc.gridx = 0;
            gbc.gridy = 7;
            gbc.gridwidth = 2;
            add(createButton, gbc);
        } else if (actionType.equals("login")) {
            // 登录界面
            JLabel loginLabel = new JLabel("登录账户");
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            add(loginLabel, gbc);

            // 用户名
            JLabel usernameLabel = new JLabel("用户名:");
            usernameField = new JTextField();
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1; // 设置宽度为 1
            add(usernameLabel, gbc);
            gbc.gridx = 1;
            gbc.gridy = 1;
            add(usernameField, gbc);

            // 密码
            JLabel passwordLabel = new JLabel("密码:");
            passwordField = new JPasswordField(20);
            gbc.gridx = 0;
            gbc.gridy = 2;
            add(passwordLabel, gbc);
            gbc.gridx = 1;
            gbc.gridy = 2;
            add(passwordField, gbc);

            // 登录按钮
            loginButton = new JButton("登录");
            loginButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    loginAccount(usernameField.getText(), new String((passwordField).getPassword()));
                }
            });

            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2; // 设置宽度为 2，使其居中
            add(loginButton, gbc);
        }

        setVisible(true);
    }

    private void createAccount(String selectedAccountType) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword()); // 从 JPasswordField 获取密码
        String phone = phoneField.getText();
        String email = emailField.getText();
        String gender = genderComboBox.getSelectedItem().toString();
        String address = addressField.getText();

        // 用户名验证，确保用户名不存在
        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username)) {
                JOptionPane.showMessageDialog(this, "用户名已存在！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (AccountValidator.isEmpty(username) || AccountValidator.isEmpty(password) || AccountValidator.isEmpty(phone) ||
                AccountValidator.isEmpty(email) || AccountValidator.isEmpty(address)) {
            JOptionPane.showMessageDialog(this, "所有字段都不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String creationTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String accountStatus = "正常";
        double initialBalance = 0.0; // 初始金额设置为 0

        List<AccountModel> accountList = new ArrayList<>();

        // 根据选择的账户类型创建相应的账户，并设置正确的 accountType 字符串
        if (selectedAccountType.equals("个人账户")) {
            accountList.add(new PersonalAccount(username, password, phone, email, gender, address, creationTime, accountStatus, "personal", initialBalance)); // 设置为 "personal"
        } else if (selectedAccountType.equals("管理员账户")) {
            accountList.add(new AdminAccount(username, password, phone, email, gender, address, creationTime, accountStatus, "Admin", initialBalance)); // 设置为 "Admin"
        }

        UserRegistrationCSVExporter.saveToCSV(accountList, true);
        System.out.println("账户创建成功！");
        dispose();  // 关闭注册窗口
    }


    private void loginAccount(String username, String password) {
        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();

        boolean loginSuccess = false;

        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username) && account.getPassword().equals(password)) {
                loginSuccess = true;

                // 假设有一个名为 UserSession 来记录当前用户
                model.UserSession.setCurrentUsername(username);

                // 判断账户类型，跳转到对应的界面
                if (account instanceof AdminAccount) {
                    // 如果是管理员账户，跳转到管理员界面
                    new AdminUI();  // 创建 AdminUI 窗口
                } else if (account instanceof PersonalAccount) {
                    // 如果是个人账户，跳转到个人账户界面
                    new PersonalUI();  // 创建 PersonalUI 窗口
                }
                break;
            }
        }

        if (loginSuccess) {
            JOptionPane.showMessageDialog(this, "登录成功！");
            dispose();  // 关闭当前登录界面
        } else {
            JOptionPane.showMessageDialog(this, "用户名或密码错误！");
        }
    }
}
