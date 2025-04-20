package src.UI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import src.Model.AccountModel;
import src.Model.AccountValidator;
import src.Model.AdminAccount;
import src.Model.PersonalAccount;
import src.Model.TransactionChecker;
import src.Model.TransactionModel;
import src.Model.UserRegistrationCSVExporter;
import src.Model.UserSession;

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

        // --- UI Initialization Code (保持不变) ---
        // 背景面板
        JPanel backgroundPanel = new JPanel() {
            Image bgImage = new ImageIcon(getClass().getResource("/images/background.png")).getImage();
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(null);
        backgroundPanel.setBounds(0, 0, 900, 500);
        add(backgroundPanel);

        // Image Label
        ImageIcon originalIcon = new ImageIcon(getClass().getResource("/images/log_img.png"));
        Image originalImage = originalIcon.getImage();
        Image scaledImage = originalImage.getScaledInstance(300, 250, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        JLabel imageLabel = new JLabel(scaledIcon);
        imageLabel.setBounds(150, 120, 300, 250);
        backgroundPanel.add(imageLabel);

        // Title Art Label
        JLabel titleArtLabel = new JLabel("Smart Finance");
        titleArtLabel.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, 32));
        titleArtLabel.setForeground(Color.WHITE);
        titleArtLabel.setBounds(30, 30, 400, 40);
        backgroundPanel.add(titleArtLabel);

        // White Card Panel
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
                // 不要调用 super.paintComponent(g); 如果你想让它完全由自定义绘制决定
            }
            @Override
            protected void paintBorder(Graphics g) {
                // 可选：绘制边框
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.LIGHT_GRAY); // 边框颜色
                g2.setStroke(new BasicStroke(1)); // 边框粗细
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30); // 绘制圆角矩形边框
                g2.dispose();
            }
        };
        panel.setLayout(null);
        panel.setOpaque(false); // 使 JPanel 透明，以便圆角背景可见
        panel.setBounds(450, 120, 300, 250); // 登录面板位置和大小
        backgroundPanel.add(panel);


        // Common Font and Layout variables
        Font font = new Font("Segoe UI", Font.PLAIN, 16);
        int labelX = 30, fieldX = 130, width = 150, height = 30;
        int y = 30, gap = 50; // 登录界面垂直间距

        // --- Conditional UI for Login or Register (保持不变) ---
        if (actionType.equals("login")) {
            // --- Login UI Elements ---
            JLabel usernameLabel = new JLabel("Username:");
            usernameLabel.setBounds(labelX, y, 100, height);
            panel.add(usernameLabel);
            usernameField = new JTextField();
            usernameField.setBounds(fieldX, y, width, height);
            panel.add(usernameField);

            y += gap; // 下移
            JLabel passwordLabel = new JLabel("Password:");
            passwordLabel.setBounds(labelX, y, 100, height);
            panel.add(passwordLabel);
            passwordField = new JPasswordField();
            passwordField.setBounds(fieldX, y, width, height);
            panel.add(passwordField);

            y += gap; // 下移
            loginButton = new JButton("Log in");
            loginButton.setBounds(50, y, 200, 35); // 按钮居中，更宽
            loginButton.setBackground(new Color(0, 120, 215)); // 现代蓝色背景
            loginButton.setForeground(Color.WHITE); // 白色文字
            loginButton.setFocusPainted(false); // 去掉焦点边框
            loginButton.setFont(font); // 应用字体
            panel.add(loginButton);

            // --- Login Button Action Listener ---
            loginButton.addActionListener(e -> loginAccount(usernameField.getText(), new String(passwordField.getPassword())));

        } else { // register
            // --- Register UI Elements ---
            panel.setBounds(450, 30, 300, 420); // 注册面板位置和大小调整以容纳更多字段
            y = 20; // 重置 y 坐标
            gap = 40; // 调整注册界面的垂直间距

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
                y += gap; // 下移
            }

            // Account Type Selection
            JLabel accountTypeLabel = new JLabel("Account Type:");
            JComboBox<String> accountTypeComboBox = new JComboBox<>(new String[]{"Personal Account", "Admin Account"});
            accountTypeLabel.setBounds(labelX, y, 100, height);
            panel.add(accountTypeLabel);
            accountTypeComboBox.setBounds(fieldX, y, width, height);
            panel.add(accountTypeComboBox);

            y += gap + 10; // 增加间距

            createButton = new JButton("Register");
            createButton.setBounds(60, y, 180, 35); // 按钮居中
            createButton.setBackground(new Color(0, 120, 215));
            createButton.setForeground(Color.WHITE);
            createButton.setFocusPainted(false);
            createButton.setFont(font);
            panel.add(createButton);

            // --- Register Button Action Listener ---
            createButton.addActionListener(e -> {
                String type = accountTypeComboBox.getSelectedItem().toString();
                createAccount(type.equals("Admin Account") ? "Admin" : "personal");
            });
        }

        setVisible(true);
    }

    // --- createAccount Method (保持不变) ---
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
                return; // 用户名已存在，停止注册
            }
        }

        // 检查字段是否为空
        if (AccountValidator.isEmpty(username) || AccountValidator.isEmpty(password) ||
                AccountValidator.isEmpty(phone) || AccountValidator.isEmpty(email) || AccountValidator.isEmpty(address)) {
            JOptionPane.showMessageDialog(this, "All fields cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return; // 有字段为空，停止注册
        }

        // 可以添加更复杂的验证，如邮箱格式、手机号格式等

        String creationTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String accountStatus = "ACTIVE"; // 新账户默认为激活状态
        double initialBalance = 0.0; // 新账户初始余额为0

        AccountModel newAccount = null;
        if ("personal".equalsIgnoreCase(selectedAccountType)) {
            newAccount = new PersonalAccount(username, password, phone, email, gender, address, creationTime, accountStatus, "personal", initialBalance);
        } else if ("Admin".equalsIgnoreCase(selectedAccountType)) {
            newAccount = new AdminAccount(username, password, phone, email, gender, address, creationTime, accountStatus, "Admin", initialBalance);
        }

        if (newAccount != null) {
            List<AccountModel> accountListToAdd = new ArrayList<>();
            accountListToAdd.add(newAccount);
            // 使用追加模式保存新账户
            UserRegistrationCSVExporter.saveToCSV(accountListToAdd, true);
            JOptionPane.showMessageDialog(this, "Account created successfully!");
            dispose(); // 关闭注册/登录窗口
        } else {
             JOptionPane.showMessageDialog(this, "Invalid account type selected!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    // --- loginAccount Method (修正版：绝对只提醒，不冻结，继续流程) ---
    private void loginAccount(String username, String password) {
        System.out.println("DEBUG: loginAccount - 尝试登录用户: " + username);
        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        AccountModel matchedAccount = null;

        // 查找匹配的账户
        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username) && account.getPassword().equals(password)) {
                matchedAccount = account;
                break;
            }
        }

        // 处理查找结果
        if (matchedAccount != null) {
            System.out.println("DEBUG: loginAccount - 找到账户: " + username);
            // 账户找到了

            // 1. 检查是否 *已经* 被冻结 (这是防止已冻结账户登录的必要检查)
            System.out.println("DEBUG: loginAccount - 检查账户状态...");
            if ("FROZEN".equalsIgnoreCase(matchedAccount.getAccountStatus())) {
                System.out.println("DEBUG: loginAccount - 账户已冻结，停止登录。");
                JOptionPane.showMessageDialog(this,
                    "This account is currently frozen. Please contact the administrator.",
                    "Account Frozen", JOptionPane.ERROR_MESSAGE);
                return; // 如果账户已经是冻结状态，则不允许登录
            }
            System.out.println("DEBUG: loginAccount - 账户状态为 ACTIVE。");

            // --- 确认登录凭证有效且账户未冻结 ---
            // 2. *** 首先显示“登录成功”提示 ***
            System.out.println("DEBUG: loginAccount - 显示 'Login successful!' 对话框...");
            JOptionPane.showMessageDialog(this, "Login successful!");
            System.out.println("DEBUG: loginAccount - 'Login successful!' 对话框已关闭。");

            // 3. 加载交易记录
            System.out.println("DEBUG: loginAccount - 准备加载交易记录...");
            loadTransactionsForAccount(matchedAccount); // 加载交易记录到 matchedAccount 对象中
            System.out.println("DEBUG: loginAccount - 交易记录加载完成。");

            // 4. 检查异常交易（只提醒）
            System.out.println("DEBUG: loginAccount - 准备检查异常交易...");
            boolean abnormalDetected = TransactionChecker.hasAbnormalTransactions(matchedAccount);
            System.out.println("DEBUG: loginAccount - 异常交易检查结果: " + abnormalDetected);

            if (abnormalDetected) {
                // *** 如果检测到异常，只显示警告信息 ***
                System.out.println("DEBUG: loginAccount - 检测到异常交易，显示警告对话框...");
                JOptionPane.showMessageDialog(this,
                    "Warning: Recent large transfer activity detected on your account. Please review your transaction history.", // 警告信息
                    "Activity Alert", // 弹窗标题
                    JOptionPane.WARNING_MESSAGE); // 使用警告图标
                System.out.println("DEBUG: loginAccount - 'Activity Alert' 对话框已关闭。");
                // *** 删除了所有冻结账户的代码和 return 语句 ***
                // 不再调用 AdminModifyService.freezeAccount(...)
                // 不再显示账户被冻结的消息
                // 不再 return，允许登录继续
            }

            // 5. *** 最后，设置 Session，打开主界面，关闭登录窗口 ***
            // (无论 abnormalDetected 是 true 还是 false，都会执行到这里)
            System.out.println("DEBUG: loginAccount - 准备设置用户 Session...");
            UserSession.setCurrentUsername(username); // 设置当前登录的用户名
            // 可以考虑存储整个 matchedAccount 对象到 UserSession 如果需要更多用户信息
            // UserSession.setCurrentAccount(matchedAccount);
            System.out.println("DEBUG: loginAccount - 用户 Session 已设置。");

            System.out.println("DEBUG: loginAccount - 准备打开主界面...");
            try {
                 // 根据账户类型打开不同的主界面
                 if (matchedAccount instanceof AdminAccount) {
                     System.out.println("DEBUG: loginAccount - 打开 AdminUI。");
                     new AdminUI(); // 创建并显示管理员界面
                 } else if (matchedAccount instanceof PersonalAccount) {
                     System.out.println("DEBUG: loginAccount - 打开 PersonalUI。");
                     new PersonalUI(); // 创建并显示个人用户界面
                 }
                 System.out.println("DEBUG: loginAccount - 主界面已创建。");

                 System.out.println("DEBUG: loginAccount - 准备关闭登录窗口...");
                 // 使用 SwingUtilities.invokeLater 确保 dispose 在事件分发线程中安全执行
                 // 避免与新窗口的显示发生潜在的时序问题
                 SwingUtilities.invokeLater(this::dispose); // 关闭当前的登录对话框
                 System.out.println("DEBUG: loginAccount - dispose() 已调用。");

            } catch (Exception ex) {
                 // 捕获打开主界面或关闭登录窗口时可能发生的任何异常
                 System.err.println("ERROR: loginAccount - 打开主界面或关闭登录窗口时发生错误！");
                 ex.printStackTrace(); // 打印详细的错误堆栈信息
                 // 向用户显示一个通用的错误消息
                 JOptionPane.showMessageDialog(this, "An error occurred while opening the main window.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } else {
            // 没有找到匹配的账户
            System.out.println("DEBUG: loginAccount - 登录失败：用户名或密码错误。");
            JOptionPane.showMessageDialog(this, "Incorrect username or password!", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
        System.out.println("DEBUG: loginAccount - 方法执行完毕。");
    }


    // --- Helper method to load transactions for a specific account (保持不变) ---
    private void loadTransactionsForAccount(AccountModel account) {
        if (account == null) return; // 如果账户为空，则不执行任何操作
        String username = account.getUsername();
        String transactionFilePath = "transactions.csv"; // 交易记录文件名，确保路径正确

        account.getTransactions().clear(); // 清空账户对象中现有的交易列表，准备重新加载

        File file = new File(transactionFilePath);
        if (!file.exists()) {
            System.err.println("ERROR: loadTransactionsForAccount - 交易文件未找到: " + transactionFilePath);
            // 文件不存在，可能需要创建或提示用户
            return; // 无法加载，直接返回
        }
         System.out.println("DEBUG: loadTransactionsForAccount - 开始读取文件: " + transactionFilePath + " 为用户: " + username);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            // 读取并丢弃表头行
            String header = br.readLine();
            if (header == null) {
                System.err.println("ERROR: loadTransactionsForAccount - " + transactionFilePath + " 为空或只有头部。");
                return; // 文件为空
            }

            int loadedCount = 0;
            // 逐行读取交易数据
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // 跳过空行

                String[] data = line.split(","); // 使用逗号分隔数据
                // 确保至少有基本字段：用户名,类型,金额,时间戳,描述
                if (data.length >= 5) {
                    String csvUsername = data[0].trim(); // CSV中的用户名

                    // 只加载属于当前登录用户的交易记录
                    if (csvUsername.equalsIgnoreCase(username)) {
                        try {
                            // 生成一个唯一的交易ID（这里用时间戳+随机数，仅作示例）
                            String transactionId = System.currentTimeMillis() + "_" + ((int)(Math.random() * 1000));
                            String type = data[1].trim(); // 交易类型
                            double amount = Double.parseDouble(data[2].trim()); // 交易金额
                            String timestamp = data[3].trim(); // 时间戳
                            String descriptionOrMerchant = data[4].trim(); // 描述或商户

                            // 处理描述中可能包含逗号的情况 (如果CSV是用引号包围含逗号的字段)
                            if (data.length > 5) {
                                StringBuilder sb = new StringBuilder(descriptionOrMerchant);
                                for (int i = 5; i < data.length; i++) {
                                    sb.append(",").append(data[i]); // 将后续部分拼接回来
                                }
                                descriptionOrMerchant = sb.toString().trim();
                                // 如果描述是用引号包围的，去除首尾引号，并将双引号替换为单引号
                                if (descriptionOrMerchant.startsWith("\"") && descriptionOrMerchant.endsWith("\"") && descriptionOrMerchant.length() >= 2) {
                                     descriptionOrMerchant = descriptionOrMerchant.substring(1, descriptionOrMerchant.length() - 1).replace("\"\"", "\"");
                                }
                            }

                            // 如果有相关用户字段，可以在这里解析 data[index]
                            String relatedUser = null; // 假设没有这个字段或暂时不用

                            // 创建 TransactionModel 对象
                            TransactionModel tx = new TransactionModel(
                                    transactionId, // 使用生成的ID
                                    username,      // 确认是当前用户的
                                    type,
                                    amount,
                                    timestamp,
                                    descriptionOrMerchant,
                                    relatedUser    // 可能为 null
                            );
                            // 将交易添加到账户的交易列表中
                            account.addTransaction(tx);
                            loadedCount++;

                        } catch (NumberFormatException ex) {
                            System.err.println("ERROR: loadTransactionsForAccount - 跳过交易，金额解析错误: " + line + " | 错误: " + ex.getMessage());
                        } catch (ArrayIndexOutOfBoundsException ex) {
                             System.err.println("ERROR: loadTransactionsForAccount - 跳过交易，字段不足: " + line + " | 错误: " + ex.getMessage());
                        } catch (Exception ex) {
                             // 捕获其他潜在错误
                             System.err.println("ERROR: loadTransactionsForAccount - 跳过交易，发生意外错误: " + line + " | 错误: " + ex.getMessage());
                             ex.printStackTrace();
                        }
                    }
                } else {
                     // 行数据列数不足，记录警告
                     System.err.println("WARN: loadTransactionsForAccount - 跳过交易，列数不足: " + line);
                }
            }
             System.out.println("DEBUG: loadTransactionsForAccount - 为用户 " + username + " 加载了 " + loadedCount + " 条交易记录。");
        } catch (IOException e) {
            System.err.println("ERROR: loadTransactionsForAccount - 读取交易文件 '" + transactionFilePath + "' 时发生错误: " + e.getMessage());
             // 可以在这里向用户显示错误消息
             JOptionPane.showMessageDialog(this, "Failed to load transaction history.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

} // End of AccountManagementUI class