package UI;

<<<<<<< HEAD
import Person.DepositDialog;
import Person.ViewPersonalInfo;
import Person.ViewBalanceDialog;
import Person.transferAccounts; // 确保这个类名是正确的
import Person.WithdrawalDialog;
import Person.TransactionHistoryDialog;
import Person.TransactionAnalyzer;
import model.UserSession; // 确保 UserSession 类及其方法可用
import model.CsvDataManager;
import model.Transaction;

import java.util.List;
import java.util.stream.Collectors;
=======
>>>>>>> 382f4a22ceb10164f9c36fd7cadf0016088cd827
import javax.swing.*;
import java.awt.*;
import model.UserSession;
import Person.IncomeExpenseChart;

public class PersonalUI extends JDialog {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private SidebarPanel sidebarPanel;
    private ContentPanelManager contentManager;
    
    public PersonalUI() {
        setTitle("Personal Account Center");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
<<<<<<< HEAD

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
        JButton securityAlertButton = createFunctionButton("安全警报", "/UI/icons/alert.png", btnSize); // 新增按钮

        // 添加按钮到侧边栏，使用垂直间隔，居中排列
        sidebarPanel.add(Box.createVerticalGlue()); // 顶部空白
        sidebarPanel.add(homeButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        sidebarPanel.add(securityAlertButton); // 新增按钮
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
=======
        
        // 创建侧边栏
        sidebarPanel = new SidebarPanel(this);
        add(sidebarPanel, BorderLayout.WEST);
        
        // 创建内容面板管理器
>>>>>>> 382f4a22ceb10164f9c36fd7cadf0016088cd827
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(new Color(30, 60, 120));
        
        contentManager = new ContentPanelManager(this, contentPanel, cardLayout);
        contentManager.initializePanels();
        
        add(contentPanel, BorderLayout.CENTER);
<<<<<<< HEAD
        add(bottomPanel, BorderLayout.SOUTH);

        // ============== 按钮事件处理 (整合后的逻辑) ==============

        // 主页按钮：切换到主页面板
        homeButton.addActionListener(e -> cardLayout.show(contentPanel, "home"));

        // 在按钮事件处理部分添加以下代码
        securityAlertButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String loggedInUsername = UserSession.getCurrentUsername();
                if (loggedInUsername != null) {
                    try {
                        // 1. 读取当前用户的交易记录
                        CsvDataManager csvManager = new CsvDataManager();
                        List<Transaction> transactions = csvManager.readTransactions().stream()
                                .filter(t -> t.getUsername().equals(loggedInUsername))
                                .collect(Collectors.toList());

                        // 2. 检测异常交易
                        TransactionAnalyzer analyzer = new TransactionAnalyzer();
                        List<Transaction> abnormal = analyzer.detectAbnormal(transactions);

                        // 3. 显示弹窗
                        if (abnormal != null && !abnormal.isEmpty()) {
                            new AlertPanel(abnormal, null).setVisible(true);
                        } else {
                            JOptionPane.showMessageDialog(PersonalUI.this, "未检测到异常交易", "安全状态", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(PersonalUI.this, "读取交易记录失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(PersonalUI.this, "请先登录", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

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
=======
        
        // 设置默认视图
        cardLayout.show(contentPanel, "individualCenter");
        
        setVisible(true);
>>>>>>> 382f4a22ceb10164f9c36fd7cadf0016088cd827
    }

    // 新增方法：显示收入支出图表
    public void showIncomeExpenseChart() {
        System.out.println("showIncomeExpenseChart() 方法被调用了！");
        IncomeExpenseChart.showIncomeExpensePieChart("transactions.csv");
    }

    // 检查登录状态并显示卡片
    public void checkLoginAndShowCard(String cardName) {
        if (UserSession.getCurrentUsername() != null) {
            cardLayout.show(contentPanel, cardName);
        } else {
            UIUtils.showLoginError(this);
        }
    }
    
    // 检查登录状态并执行操作（通常是显示对话框）
    public void checkLoginAndShowDialog(Runnable dialogCreator) {
        if (UserSession.getCurrentUsername() != null) {
            SwingUtilities.invokeLater(dialogCreator);
        } else {
            UIUtils.showLoginError(this);
        }
    }
    
    // 处理登出操作
    public void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            UserSession.setCurrentUsername(null); // 清除会话数据
            dispose(); // 关闭此窗口
        }
    }
}