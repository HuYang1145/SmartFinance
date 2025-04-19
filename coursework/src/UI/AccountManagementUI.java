package UI;

import Model.AccountModel;
import Model.AccountValidator;
import Model.AdminAccount;
import Model.PersonalAccount;
import Model.UserRegistrationCSVExporter;
import Model.UserSession; // 确保导入 UserSession
import Admin.AdminModifyService; // 确保导入 AdminModifyService
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;

public class AccountManagementUI extends JDialog {
    private JTextField usernameField, phoneField, emailField, addressField;
    private JPasswordField passwordField;
    private JComboBox<String> genderComboBox;
    private JButton loginButton, createButton;

    public AccountManagementUI(JFrame parent, String actionType) {
        super(parent, "Smart Finance", true);
        setSize(900, 500);
        setLocationRelativeTo(parent);
        setLayout(null);

        // 背景面板
        JPanel backgroundPanel = new JPanel() {
            Image bgImage = new ImageIcon(getClass().getResource("/Main/background.png")).getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(null);
        backgroundPanel.setBounds(0, 0, 900, 500);
        add(backgroundPanel);

        ImageIcon originalIcon = new ImageIcon(getClass().getResource("/Main/log_img.png"));
        Image originalImage = originalIcon.getImage();

        // 缩放到 300x250
        Image scaledImage = originalImage.getScaledInstance(300, 250, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        JLabel imageLabel = new JLabel(scaledIcon);
        imageLabel.setBounds(150, 120, 300, 250);  // 尺寸和缩放后保持一致
        backgroundPanel.add(imageLabel);

        // 左上角艺术字
        JLabel titleArtLabel = new JLabel("Smart Finance");
        titleArtLabel.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, 32));
        titleArtLabel.setForeground(Color.WHITE);
        titleArtLabel.setBounds(30, 30, 400, 40);
        backgroundPanel.add(titleArtLabel);

        // 白色登录或注册卡片区域
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.LIGHT_GRAY);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
                g2.dispose();
            }
        };
        panel.setLayout(null);
        panel.setOpaque(false);
        panel.setBounds(450, 120, 300, 250);
        backgroundPanel.add(panel);

        Font font = new Font("Segoe UI", Font.PLAIN, 16);
        int labelX = 30, fieldX = 130, width = 150, height = 30;
        int y = 30, gap = 50;

        if (actionType.equals("login")) {
            JLabel usernameLabel = new JLabel("Username:");
            usernameLabel.setBounds(labelX, y, 100, height); // 调整标签宽度
            panel.add(usernameLabel);
            usernameField = new JTextField();
            usernameField.setBounds(fieldX, y, width, height);
            panel.add(usernameField);

            y += gap;
            JLabel passwordLabel = new JLabel("Password:");
            passwordLabel.setBounds(labelX, y, 100, height); // 调整标签宽度
            panel.add(passwordLabel);
            passwordField = new JPasswordField();
            passwordField.setBounds(fieldX, y, width, height);
            panel.add(passwordField);

            y += gap;
            loginButton = new JButton("Log in");
            loginButton.setBounds(50, y, 200, 35); // 稍微调整按钮大小和位置
            loginButton.setBackground(new Color(0, 120, 215));
            loginButton.setForeground(Color.WHITE);
            loginButton.setFocusPainted(false);
            loginButton.setFont(font);
            panel.add(loginButton);

            // --- 添加登录按钮的事件监听器 ---
            loginButton.addActionListener(e -> loginAccount(usernameField.getText(), new String(passwordField.getPassword())));

        } else { // register
            panel.setBounds(450, 30, 300, 420); // 调整注册面板的位置和大小
            JLabel[] labels = {
                    new JLabel("Username:"), new JLabel("Password:"), new JLabel("Phone Number:"),
                    new JLabel("Email:"), new JLabel("Gender:"), new JLabel("Address:")
            };
            Component[] fields = {
                    usernameField = new JTextField(), passwordField = new JPasswordField(),
                    phoneField = new JTextField(), emailField = new JTextField(),
                    genderComboBox = new JComboBox<>(new String[]{"Male", "Female"}),
                    addressField = new JTextField()
            };

            for (int i = 0; i < labels.length; i++) {
                labels[i].setBounds(labelX, y, 100, height);
                panel.add(labels[i]);
                fields[i].setBounds(fieldX, y, width, height);
                panel.add(fields[i]);
                y += gap;
            }

            // 账户类型选择
            JLabel accountTypeLabel = new JLabel("Account Type:");
            JComboBox<String> accountTypeComboBox = new JComboBox<>(new String[]{"Personal Account", "Admin Account"});
            accountTypeLabel.setBounds(labelX, y, 100, height);
            panel.add(accountTypeLabel);
            accountTypeComboBox.setBounds(fieldX, y, width, height);
            panel.add(accountTypeComboBox);

            y += gap + 10; // 增加间距

            createButton = new JButton("Register");
            createButton.setBounds(60, y, 180, 35);
            createButton.setBackground(new Color(0, 120, 215));
            createButton.setForeground(Color.WHITE);
            createButton.setFocusPainted(false);
            createButton.setFont(font);
            panel.add(createButton);

            // --- 添加注册按钮的事件监听器 ---
            createButton.addActionListener(e -> {
                String type = accountTypeComboBox.getSelectedItem().toString();
                createAccount(type.equals("Admin Account") ? "Admin" : "personal");
            });
        }

        setVisible(true);
    }

    // --- 注册账户方法 ---
    private void createAccount(String selectedAccountType) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String phone = phoneField.getText();
        String email = emailField.getText();
        String gender = genderComboBox.getSelectedItem().toString();
        String address = addressField.getText();

        // 检查用户名是否已存在
        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username)) {
                JOptionPane.showMessageDialog(this, "Username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // 检查字段是否为空
        if (AccountValidator.isEmpty(username) || AccountValidator.isEmpty(password) ||
                AccountValidator.isEmpty(phone) || AccountValidator.isEmpty(email) || AccountValidator.isEmpty(address)) {
            JOptionPane.showMessageDialog(this, "All fields cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 创建账户信息
        String creationTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String accountStatus = "ACTIVE"; // 默认激活状态
        double initialBalance = 0.0; // 初始余额为0

        AccountModel newAccount = null;
        if ("personal".equalsIgnoreCase(selectedAccountType)) {
            newAccount = new PersonalAccount(username, password, phone, email, gender, address, creationTime, accountStatus, "personal", initialBalance);
        } else if ("Admin".equalsIgnoreCase(selectedAccountType)) {
            newAccount = new AdminAccount(username, password, phone, email, gender, address, creationTime, accountStatus, "Admin", initialBalance);
        }

        // 保存账户信息
        if (newAccount != null) {
            List<AccountModel> accountListToAdd = new ArrayList<>();
            accountListToAdd.add(newAccount);
            UserRegistrationCSVExporter.saveToCSV(accountListToAdd, true); // true表示追加写入
            JOptionPane.showMessageDialog(this, "Account created successfully!");
            dispose(); // 关闭注册/登录窗口
        } else {
             JOptionPane.showMessageDialog(this, "Invalid account type selected!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- 登录账户方法 (已修改) ---
    private void loginAccount(String username, String password) {
        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        AccountModel matchedAccount = null; // 用于存储匹配到的账户

        // 查找匹配的账户
        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username) && account.getPassword().equals(password)) {
                matchedAccount = account; // 找到匹配项，赋值给外部变量
                break; // 找到后退出循环
            }
        }

        // 处理查找结果
        if (matchedAccount != null) {
            // 账户找到了，进行后续检查

            // 1. 检查账户状态是否为 FROZEN
            if ("FROZEN".equalsIgnoreCase(matchedAccount.getAccountStatus())) {
                JOptionPane.showMessageDialog(this,
                    "This account has been frozen due to suspicious activity. Please contact the administrator to resolve this.",
                    "Account Frozen", JOptionPane.ERROR_MESSAGE);
                return; // 账户已冻结，直接返回，停止登录
            }

            // 2. 检查是否存在异常交易 (如果账户状态正常)
            if (Model.TransactionChecker.hasAbnormalTransactions(matchedAccount)) {
                // 检测到异常交易，冻结账户并提示用户
                AdminModifyService.modifyAccountStatus(username, "FROZEN"); // 调用服务冻结账户
                JOptionPane.showMessageDialog(this,
                    "Suspicious activity detected (large transfers). Your account has been temporarily frozen for security reasons. Please contact support.",
                    "Security Alert",
                    JOptionPane.WARNING_MESSAGE);
                return; // 发现异常并冻结后，停止登录
            }

            // 3. 如果账户状态正常且无异常交易，则登录成功
            UserSession.setCurrentUsername(username); // 设置用户会话

            // 根据账户类型打开不同的UI
            if (matchedAccount instanceof AdminAccount) {
                new AdminUI(); // 打开管理员界面
            } else if (matchedAccount instanceof PersonalAccount) {
                new PersonalUI(); // 打开个人用户界面
            }

            dispose(); // 关闭当前的登录/注册窗口
            // 可以在新打开的UI中显示欢迎信息，这里不再显示 "Login successful!" 弹窗

        } else {
            // 没有找到匹配的账户 (用户名或密码错误)
            JOptionPane.showMessageDialog(this, "Incorrect username or password!", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}