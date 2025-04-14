package UI;

import Person.DepositDialog;
import Person.ViewPersonalInfo;
import Person.ViewBalanceDialog;
import Person.transferAccounts; // 确保这个类名是正确的
import Person.WithdrawalDialog;
import Person.TransactionHistoryDialog;
import model.UserSession; // 确保 UserSession 类及其方法可用

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PersonalUI extends JDialog {
    private CardLayout cardLayout;
    private JPanel contentPanel;

    public PersonalUI() {
        setTitle("个人账户中心"); // 修改标题为中文
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        // 整体使用 BorderLayout
        setLayout(new BorderLayout());

        // ============== 左侧侧边栏 ==============
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setPreferredSize(new Dimension(200, 0));
        // 设置侧边栏背景为深蓝色
        sidebarPanel.setBackground(new Color(30, 60, 120));

        Dimension btnSize = new Dimension(180, 40); // 稍微调整按钮大小以便容纳中文和图标

        // 创建侧边栏按钮（使用第一个代码段的中文标签，并添加图标）
        JButton homeButton = createFunctionButton("主页", "/UI/icons/home.png", btnSize); // 主页按钮
        JButton viewBalanceButton = createFunctionButton("查看余额", "/UI/icons/balance.png", btnSize);
        JButton depositButton = createFunctionButton("转入资金", "/UI/icons/transfer.png", btnSize); // 转入
        JButton transferButton = createFunctionButton("转账", "/UI/icons/transfer_out.png", btnSize); // 转账
        JButton withdrawalButton = createFunctionButton("提款", "/UI/icons/atm.png", btnSize);
        JButton transactionHistoryButton = createFunctionButton("查看交易记录", "/UI/icons/history.png", btnSize);
        JButton aiChatButton = createFunctionButton("AI 交流区", "/UI/icons/ai.png", btnSize); // AI区
        JButton logoutButton = createFunctionButton("退出登录", "/UI/icons/logout.png", btnSize);

        // 添加按钮到侧边栏，使用垂直间隔，居中排列
        sidebarPanel.add(Box.createVerticalGlue()); // 顶部空白
        sidebarPanel.add(homeButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 15))); // 增加间距
        sidebarPanel.add(viewBalanceButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        sidebarPanel.add(depositButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        sidebarPanel.add(transferButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        sidebarPanel.add(withdrawalButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        sidebarPanel.add(transactionHistoryButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        sidebarPanel.add(aiChatButton);
        sidebarPanel.add(Box.createVerticalGlue()); // 中间空白，将退出按钮推到底部
        sidebarPanel.add(logoutButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20))); // 底部间距

        // ============== 右侧内容区域（使用CardLayout） ==============
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        // 设置右侧内容区背景（可以保持或修改）
        contentPanel.setBackground(new Color(240, 240, 240)); // 使用浅灰色背景可能更常见

        // --- 主页面板 ---
        JPanel mainDashboardPanel = createContentPanel("个人账户概览"); // 使用辅助方法创建面板
        // 添加主页特有内容...
        JLabel placeholder = new JLabel("[ 在这里显示主页内容，如图表、欢迎信息等 ]");
        placeholder.setHorizontalAlignment(SwingConstants.CENTER);
        mainDashboardPanel.add(placeholder, BorderLayout.CENTER);

        // --- AI交流区面板 (保持不变) ---
        JPanel aiPanel = new JPanel(new BorderLayout());
        aiPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 添加边距
        aiPanel.setBackground(Color.WHITE); // AI区域用白色背景
        JTextArea aiChatArea = new JTextArea();
        aiChatArea.setEditable(false);
        aiChatArea.setLineWrap(true);
        aiChatArea.setWrapStyleWord(true);
        aiChatArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14)); // 中文字体
        JScrollPane aiScrollPane = new JScrollPane(aiChatArea);
        aiPanel.add(aiScrollPane, BorderLayout.CENTER);
        JPanel aiInputPanel = new JPanel(new BorderLayout(5, 0));
        aiInputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        JTextField aiInputField = new JTextField();
        aiInputField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        JButton sendButton = new JButton("发送");
        sendButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        aiInputPanel.add(aiInputField, BorderLayout.CENTER);
        aiInputPanel.add(sendButton, BorderLayout.EAST);
        aiPanel.add(aiInputPanel, BorderLayout.SOUTH);
        // AI回复模拟
        sendButton.addActionListener(e -> {
            String text = aiInputField.getText().trim();
            if (!text.isEmpty()) {
                aiChatArea.append("我: " + text + "\n\n");
                // 模拟延迟回复
                Timer timer = new Timer(500, evt -> {
                    aiChatArea.append("AI: " + "你好！这是一个模拟的AI回复。\n\n");
                    ((Timer)evt.getSource()).stop(); // 只执行一次
                });
                timer.setRepeats(false);
                timer.start();
                aiInputField.setText("");
            }
        });
        aiInputField.addActionListener(e -> sendButton.doClick()); // 回车发送

        // 将面板添加到CardLayout
        contentPanel.add(mainDashboardPanel, "home");
        // 注意：其他功能现在直接弹出对话框，不再需要独立的CardLayout面板
        // 如果仍想保留切换效果，可以在弹出对话框前切换到一个相应的占位面板
        contentPanel.add(aiPanel, "aiChat"); // AI面板保留

        // 默认显示主页
        cardLayout.show(contentPanel, "home");

        // ============== 底部区域（单独放置“查看个人信息”按钮） ==============
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10)); // 居中对齐，上下边距10
        bottomPanel.setBackground(new Color(20, 40, 80)); // 深色底部背景
        JButton viewPersonalInfoButton = createFunctionButton("查看个人信息", "/UI/icons/person.png", new Dimension(180, 35));
        bottomPanel.add(viewPersonalInfoButton);


        // ============== 添加整体布局 ==============
        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // ============== 按钮事件处理 (整合后的逻辑) ==============

        // 主页按钮：切换到主页面板
        homeButton.addActionListener(e -> cardLayout.show(contentPanel, "home"));

        // 查看余额按钮：弹出 ViewBalanceDialog
        viewBalanceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String loggedInUsername = UserSession.getCurrentUsername();
                if (loggedInUsername != null) {
                    // 将 PersonalUI.this 作为父窗口传递
                    ViewBalanceDialog balanceDialog = new ViewBalanceDialog(PersonalUI.this, loggedInUsername);
                    balanceDialog.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(PersonalUI.this, "请先登录", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 转入资金按钮：弹出 DepositDialog
        depositButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String loggedInUsername = UserSession.getCurrentUsername();
                if (loggedInUsername != null) {
                    // 将 PersonalUI.this 作为父窗口传递
                    DepositDialog depositDialog = new DepositDialog(PersonalUI.this, loggedInUsername);
                    depositDialog.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(PersonalUI.this, "请先登录", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 转账按钮：弹出 transferAccounts 对话框
        transferButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String loggedInUsername = UserSession.getCurrentUsername();
                if (loggedInUsername != null) {
                    // 假设 transferAccounts 构造函数接受父窗口
                    // 如果 transferAccounts 不需要用户名，则不需要传递
                    transferAccounts transferDialog = new transferAccounts(PersonalUI.this); // 确认构造函数签名
                    transferDialog.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(PersonalUI.this, "请先登录", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 提款按钮：弹出 WithdrawalDialog
        withdrawalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String loggedInUsername = UserSession.getCurrentUsername();
                if (loggedInUsername != null) {
                    // 将 PersonalUI.this 作为父窗口传递
                    WithdrawalDialog withdrawalDialog = new WithdrawalDialog(PersonalUI.this, loggedInUsername);
                    withdrawalDialog.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(PersonalUI.this, "请先登录", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 查看交易记录按钮：弹出 TransactionHistoryDialog
        transactionHistoryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String loggedInUsername = UserSession.getCurrentUsername();
                if (loggedInUsername != null) {
                    // 假设 TransactionHistoryDialog 构造函数接受父窗口
                    // 如果 TransactionHistoryDialog 需要用户名，也需要传递
                    TransactionHistoryDialog historyDialog = new TransactionHistoryDialog(PersonalUI.this); // 确认构造函数签名
                    historyDialog.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(PersonalUI.this, "请先登录", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // AI 交流区按钮：切换到 AI 面板
        aiChatButton.addActionListener(e -> cardLayout.show(contentPanel, "aiChat"));

        // 退出登录按钮：关闭当前窗口
        logoutButton.addActionListener(e -> dispose());

        // 查看个人信息按钮 (底部)：弹出 ViewPersonalInfo 对话框
        viewPersonalInfoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String loggedInUsername = UserSession.getCurrentUsername();
                if (loggedInUsername != null) {
                    // 将 PersonalUI.this 作为父窗口传递
                    ViewPersonalInfo infoDialog = new ViewPersonalInfo(PersonalUI.this, loggedInUsername);
                    infoDialog.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(PersonalUI.this, "请先登录", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });


        setVisible(true); // 显示主窗口
    }

    /**
     * 创建一个内容面板的辅助方法
     */
    private JPanel createContentPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // 内边距
        panel.setBackground(Color.WHITE); // 内容区使用白色背景

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 24)); // 中文字体加粗
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0)); // 标题下边距
        panel.add(titleLabel, BorderLayout.NORTH);

        return panel;
    }


    /**
     * 创建一个带图标和文本的功能按钮
     */
    private JButton createFunctionButton(String text, String iconPath, Dimension size) {
        ImageIcon icon = null;
        try {
            // 尝试加载图标资源
            java.net.URL imgURL = getClass().getResource(iconPath);
            if (imgURL != null) {
                icon = new ImageIcon(imgURL);
                // 缩放图标到合适的大小，例如 24x24
                Image img = icon.getImage();
                Image scaled = img.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaled);
            } else {
                System.err.println("无法加载图标: " + iconPath); // 错误提示
            }
        } catch (Exception e) {
            System.err.println("加载图标时出错: " + iconPath + " - " + e.getMessage());
        }

        JButton button = new JButton(text, icon);
        button.setPreferredSize(size);
        button.setMaximumSize(size); // 配合 BoxLayout，限制最大尺寸
        button.setMinimumSize(size); // 确保最小尺寸

        // 文本和图标的对齐与间距
        button.setHorizontalAlignment(SwingConstants.LEFT); // 图标在左，文本在右
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setIconTextGap(15); // 图标和文本之间的距离
        // 设置内边距，让内容不至于太靠边
        button.setMargin(new Insets(5, 15, 5, 15)); // 上、左、下、右

        // 外观设置
        button.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14)); // 使用微软雅黑字体
        button.setBackground(new Color(45, 75, 140)); // 按钮背景色稍亮一点
        button.setForeground(Color.WHITE); // 文字颜色
        button.setFocusPainted(false); // 去掉焦点框
        button.setBorderPainted(false); // 去掉边框
        button.setOpaque(true); // 必须设置为 true 背景色才会显示
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // 鼠标悬停时显示手形光标

        // 悬停效果 (可选)
        button.addMouseListener(new MouseAdapter() {
            Color originalColor = button.getBackground();
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(70, 100, 170)); // 悬停时变亮
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(originalColor); // 离开时恢复原色
            }
        });


        // 使按钮在 BoxLayout 中水平居中（如果需要的话）
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        return button;
    }

    // main 方法用于测试（如果需要）
    public static void main(String[] args) {
        // 运行前最好能模拟一个登录用户，否则功能按钮会提示未登录
         // model.UserSession.setCurrentUsername("testUser"); // 假设有这样一个设置方法

        // 使用 SwingUtilities.invokeLater 确保 UI 在事件调度线程上创建
        SwingUtilities.invokeLater(() -> {
            // 设置一个更好看的外观（可选）
            try {
                 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
             } catch (Exception e) {
                 e.printStackTrace();
            }
            new PersonalUI();
        });
    }
}