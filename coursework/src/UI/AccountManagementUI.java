package UI;

import javax.swing.*;
import java.awt.*;
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
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
                super.paintComponent(g);
            }

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
        int labelX = 30, fieldX = 100, width = 150, height = 30;
        int y = 30, gap = 50;

        if (actionType.equals("login")) {
            JLabel usernameLabel = new JLabel("Username:");
            usernameLabel.setBounds(labelX, y, 200, height);
            panel.add(usernameLabel);
            usernameField = new JTextField();
            usernameField.setBounds(fieldX, y, width, height);
            panel.add(usernameField);

            y += gap;
            JLabel passwordLabel = new JLabel("Password:");
            passwordLabel.setBounds(labelX, y, 200, height);
            panel.add(passwordLabel);
            passwordField = new JPasswordField();
            passwordField.setBounds(fieldX, y, width, height);
            panel.add(passwordField);

            y += gap;
            loginButton = new JButton("Log in");
            loginButton.setBounds(50, y, 210, 35);
            loginButton.setBackground(new Color(0, 120, 215));
            loginButton.setForeground(Color.WHITE);
            loginButton.setFocusPainted(false);
            loginButton.setFont(font);
            panel.add(loginButton);

            loginButton.addActionListener(e -> loginAccount(usernameField.getText(), new String(passwordField.getPassword())));
        } else {
            panel.setBounds(450, 30, 300, 420);
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

            //账户类型选择

            JLabel accountTypeLabel = new JLabel("Account Type:");
            JComboBox<String> accountTypeComboBox = new JComboBox<>(new String[]{"Personal Account", "Admin Account"});
            accountTypeLabel.setBounds(labelX, y, 100, height);
            panel.add(accountTypeLabel);
            accountTypeComboBox.setBounds(fieldX, y, width, height);
            panel.add(accountTypeComboBox);
            y += gap;


            createButton = new JButton("Register");
            createButton.setBounds(60, y, 180, 35);
            createButton.setBackground(new Color(0, 120, 215));
            createButton.setForeground(Color.WHITE);
            createButton.setFocusPainted(false);
            createButton.setFont(font);
            panel.add(createButton);


            createButton.addActionListener(e -> {
                String type = accountTypeComboBox.getSelectedItem().toString();
                createAccount(type.equals("Admin Account") ? "Admin" : "personal");
            });
        }

        setVisible(true);
    }

    private void createAccount(String selectedAccountType) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String phone = phoneField.getText();
        String email = emailField.getText();
        String gender = genderComboBox.getSelectedItem().toString();
        String address = addressField.getText();

        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username)) {
                JOptionPane.showMessageDialog(this, "Username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (AccountValidator.isEmpty(username) || AccountValidator.isEmpty(password) ||
                AccountValidator.isEmpty(phone) || AccountValidator.isEmpty(email) || AccountValidator.isEmpty(address)) {
            JOptionPane.showMessageDialog(this, "All fields cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String creationTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String accountStatus = "Normal";
        double initialBalance = 0.0;

        List<AccountModel> accountList = new ArrayList<>();
        if (selectedAccountType.equals("personal")) {
            accountList.add(new PersonalAccount(username, password, phone, email, gender, address, creationTime, accountStatus, "personal", initialBalance));
        } else if (selectedAccountType.equals("Admin")) {
            accountList.add(new AdminAccount(username, password, phone, email, gender, address, creationTime, accountStatus, "Admin", initialBalance));
        }

        UserRegistrationCSVExporter.saveToCSV(accountList, true);
        JOptionPane.showMessageDialog(this, "Account created successfully!");
        dispose();
    }

    private void loginAccount(String username, String password) {
        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        boolean loginSuccess = false;
        AccountModel currentAccount = null; // 保存当前登录的账户对象
        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username) && account.getPassword().equals(password)) {
                loginSuccess = true;
                currentAccount = account; // 保存当前登录的账户对象
                model.UserSession.setCurrentUsername(username);

                if (account instanceof AdminAccount) new AdminUI();
                else if (account instanceof PersonalAccount) new PersonalUI();
                break;
            }
        }

        if (loginSuccess) {
        // 登录成功后，检测是否为个人账户并检查异常交易
        if (currentAccount instanceof PersonalAccount) {
            if (TransactionChecker.hasAbnormalTransactions(currentAccount)) { // 调用model包中的检测方法
                JOptionPane.showMessageDialog(this, 
                    "Abnormal consumption is detected, please verify as soon as possible to avoid property damage.");
            }
        }

        // 显示登录成功的提示
        JOptionPane.showMessageDialog(this, "Login successful!");
        dispose();
    } else {
        JOptionPane.showMessageDialog(this, "Incorrect username or password!");
    }
}