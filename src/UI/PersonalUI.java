package UI;

import Person.ViewPersonalInfo;
import model.UserSession;
import model.BudgetAdvisor;  // 根据实际包路径调整
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PersonalUI extends JDialog {
    private CardLayout cardLayout;
    private JPanel contentPanel;

    public PersonalUI() {
        setTitle("Personal Account Center");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        // 整体使用 BorderLayout
        setLayout(new BorderLayout());

        // ============== 左侧侧边栏 ==============
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setPreferredSize(new Dimension(220, 0)); // 可以根据按钮宽度调整侧边栏宽度
        // 设置侧边栏背景为深蓝色
        sidebarPanel.setBackground(new Color(30, 60, 120));

        // 调整按钮大小以适应文本
        Dimension btnSize = new Dimension(200, 35); // 增加宽度和高度

        // 创建新的侧边栏按钮 (无图标)
        JButton incomeDetailButton = new JButton("Income and expenditure detail");
        JButton periodicReportButton = new JButton("Periodic financial reporting");
        JButton aiQAButton = new JButton("AI Q&A");
        JButton financialSuggestionButton = new JButton("Financial management suggestion");
        JButton individualCenterButton = new JButton("Individual center");
        JButton depositWithdrawTransferButton = new JButton("Deposits, withdrawals and transfers");
        JButton spendingProportionButton = new JButton("Monthly, annual spending proportion display");
        JButton viewPersonalInfoButton = new JButton("View Personal Information"); // 从底部移到侧边栏
        JButton logoutButton = new JButton("Log out");

        // 将所有需要设置样式的按钮放入数组统一处理
        JButton[] sidebarButtons = {
                incomeDetailButton, periodicReportButton, aiQAButton,
                financialSuggestionButton, individualCenterButton, depositWithdrawTransferButton,
                spendingProportionButton, viewPersonalInfoButton, logoutButton
        };

        // 设置按钮样式和居中对齐
        for (JButton button : sidebarButtons) {
            button.setAlignmentX(Component.CENTER_ALIGNMENT); // 水平居中对齐
            if (btnSize != null) {
                button.setPreferredSize(btnSize);
                button.setMaximumSize(btnSize); // **重要**: 配合 BoxLayout 实现居中和固定大小
            }
            button.setFocusPainted(false);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // 字体调小一点以适应长文本
            // 设置按钮背景为深蓝色，文字为白色
            button.setBackground(new Color(20, 40, 80));
            button.setForeground(Color.WHITE);
            // 设置无边框样式
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setContentAreaFilled(true); // 确保背景色生效
            button.setOpaque(true); // 确保背景色生效
        }


        // 添加按钮到侧边栏，使用垂直间隔和 Glue 实现垂直居中效果
        sidebarPanel.add(Box.createVerticalGlue()); // 顶部空白，将按钮向下推
        sidebarPanel.add(individualCenterButton); // 将"个人中心"放在靠前的位置可能更符合习惯
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(incomeDetailButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(periodicReportButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(depositWithdrawTransferButton); // 存取转账
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(spendingProportionButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(financialSuggestionButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(aiQAButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(viewPersonalInfoButton); // 查看个人信息
        sidebarPanel.add(Box.createVerticalGlue()); // 中间空白，将下面的按钮向下推
        sidebarPanel.add(logoutButton); // 退出登录通常在最后
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20))); // 底部留一些空间


        // ============== 右侧内容区域（使用CardLayout） ==============
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        // 设置右侧内容区背景为深蓝色
        contentPanel.setBackground(new Color(30, 60, 120));

        // --- 为新按钮创建对应的面板 (占位符) ---

        // 个人中心 面板 (可以复用之前的 home 面板或新建)
        JPanel individualCenterPanel = new JPanel(null);
        individualCenterPanel.setBackground(new Color(30, 60, 120));
        JLabel centerLabel = new JLabel("Individual Center Overview");
        centerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        centerLabel.setForeground(Color.WHITE);
        centerLabel.setBounds(30, 30, 400, 40);
        individualCenterPanel.add(centerLabel);
        JLabel centerPlaceholder = new JLabel("[Display individual center content here]");
        centerPlaceholder.setForeground(Color.WHITE);
        centerPlaceholder.setBounds(30, 100, 400, 30);
        individualCenterPanel.add(centerPlaceholder);

        // 收支明细 面板
        JPanel incomeDetailPanel = createPlaceholderPanel("Income and Expenditure Detail Area");
        // 定期财报 面板
        JPanel periodicReportPanel = createPlaceholderPanel("Periodic Financial Reporting Area");
        // 存取转账 面板 (这里可以集成原来的存款、取款、转账逻辑)
        JPanel depositWithdrawTransferPanel = createPlaceholderPanel("Deposits, Withdrawals, and Transfers Area");
        // 月度/年度支出比例 面板
        JPanel spendingProportionPanel = createPlaceholderPanel("Monthly/Annual Spending Proportion Display Area");
        // 财务管理建议面板
        JPanel financialSuggestionPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(30, 60, 120)); // 深蓝色背景
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        financialSuggestionPanel.setBackground(new Color(30, 60, 120));

// 标题
        JLabel titleLabel = new JLabel("Financial Management Suggestion");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(50, 50, 400, 40);
        financialSuggestionPanel.add(titleLabel);

// 关闭按钮（左上角）
        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        closeButton.setBounds(20, 20, 40, 40);
        closeButton.setFocusPainted(false);
        closeButton.setBackground(Color.RED);
        closeButton.setForeground(Color.WHITE);
        closeButton.setBorderPainted(false);
        closeButton.addActionListener(e -> cardLayout.show(contentPanel, "individualCenter"));
        financialSuggestionPanel.add(closeButton);

// 按钮样式配置
        Color buttonTextColor = new Color(30, 60, 120); // 按钮文字颜色（与背景同款蓝色）
        Color buttonBgColor = Color.WHITE; // 按钮背景白色
        Font btnFont = new Font("Segoe UI", Font.BOLD, 14);

// 查看建议按钮
        JButton viewSuggestionButton = createStyledButton("View the suggestion", buttonTextColor, buttonBgColor, btnSize, btnFont);
        viewSuggestionButton.setBounds(50, 150, 250, 40);
        // 原代码中的 View the suggestion 按钮事件修改为：
        viewSuggestionButton.addActionListener(e -> showSuggestionDialog());
        financialSuggestionPanel.add(viewSuggestionButton);

// 修改预算按钮
        JButton changeBudgetButton = createStyledButton("Change your budget goal", buttonTextColor, buttonBgColor, btnSize, btnFont);
        changeBudgetButton.setBounds(50, 210, 250, 40);
        changeBudgetButton.addActionListener(e -> showBudgetDialog());
        financialSuggestionPanel.add(changeBudgetButton);

// 返回按钮
        JButton backButton = createStyledButton("Back to Home page", buttonTextColor, buttonBgColor, btnSize, btnFont);
        backButton.setBounds(50, 270, 250, 40);
        backButton.addActionListener(e -> cardLayout.show(contentPanel, "individualCenter"));
        financialSuggestionPanel.add(backButton);


        // AI 问答 面板 (复用之前的 AI 聊天区)
        JPanel aiPanel = new JPanel(new BorderLayout());
        aiPanel.setBackground(new Color(30, 60, 120));
        JTextArea aiChatArea = new JTextArea();
        aiChatArea.setEditable(false);
        aiChatArea.setLineWrap(true);
        aiChatArea.setForeground(Color.WHITE);
        aiChatArea.setBackground(new Color(40, 70, 130)); // 稍微不同的背景以便区分
        JScrollPane aiScrollPane = new JScrollPane(aiChatArea);
        aiPanel.add(aiScrollPane, BorderLayout.CENTER);
        JPanel aiInputPanel = new JPanel(new BorderLayout());
        JTextField aiInputField = new JTextField();
        JButton sendButton = new JButton("发送");
        sendButton.setPreferredSize(new Dimension(80, 30));
        // 可以给输入区域也加上深色主题
        aiInputField.setBackground(new Color(50, 80, 140));
        aiInputField.setForeground(Color.WHITE);
        aiInputField.setCaretColor(Color.WHITE); // 光标颜色
        aiInputField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // 添加内边距
        aiInputPanel.setBackground(new Color(30, 60, 120));
        aiInputPanel.add(aiInputField, BorderLayout.CENTER);
        aiInputPanel.add(sendButton, BorderLayout.EAST);
        aiPanel.add(aiInputPanel, BorderLayout.SOUTH);
        // 简单模拟AI回复
        sendButton.addActionListener(e -> {
            String text = aiInputField.getText().trim();
            if (!text.isEmpty()) {
                aiChatArea.append("我: " + text + "\n");
                aiChatArea.append("AI: " + "这是 AI 的回复...\n"); // 替换为实际的 AI 逻辑
                aiInputField.setText("");
            }
        });

        // 将各面板添加到内容区CardLayout中 (使用新的 key)
        contentPanel.add(individualCenterPanel, "individualCenter"); // 默认显示个人中心
        contentPanel.add(incomeDetailPanel, "incomeDetail");
        contentPanel.add(periodicReportPanel, "periodicReport");
        contentPanel.add(depositWithdrawTransferPanel, "depositWithdrawTransfer");
        contentPanel.add(spendingProportionPanel, "spendingProportion");
        contentPanel.add(financialSuggestionPanel, "financialSuggestion");
        contentPanel.add(aiPanel, "aiQA"); // AI 问答使用 aiPanel

        // 默认显示个人中心面板
        cardLayout.show(contentPanel, "individualCenter");

        // ============== 整体布局 ==============
        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        // 不再需要 bottomPanel
        // add(bottomPanel, BorderLayout.SOUTH);

        // ============== 按钮事件处理 ==============
        // 为新按钮添加事件监听器
        individualCenterButton.addActionListener(e -> cardLayout.show(contentPanel, "individualCenter"));
        incomeDetailButton.addActionListener(e -> checkLoginAndShow("incomeDetail"));
        periodicReportButton.addActionListener(e -> checkLoginAndShow("periodicReport"));
        depositWithdrawTransferButton.addActionListener(e -> checkLoginAndShow("depositWithdrawTransfer"));
        spendingProportionButton.addActionListener(e -> checkLoginAndShow("spendingProportion"));
        financialSuggestionButton.addActionListener(e -> checkLoginAndShow("financialSuggestion"));
        aiQAButton.addActionListener(e -> cardLayout.show(contentPanel, "aiQA")); // AI 区通常不需要登录检查? (按需修改)

        // 查看个人信息按钮 (现在在侧边栏)
        viewPersonalInfoButton.addActionListener(e -> {
            if (UserSession.getCurrentUsername() != null) {
                // 创建并显示 ViewPersonalInfo 对话框
                // 注意：如果 ViewPersonalInfo 依赖于 PersonalUI 作为父窗口，需要传递 this
                new ViewPersonalInfo(this, UserSession.getCurrentUsername()).setVisible(true);
            } else {
                showLoginError();
            }
        });

        setVisible(true);
    }

    // 辅助方法：创建占位符面板
    private JPanel createPlaceholderPanel(String title) {
        JPanel panel = new JPanel(null); // 使用 null 布局以便于放置标签
        panel.setBackground(new Color(30, 60, 120));
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        label.setForeground(Color.WHITE);
        // 简单的位置设置，你可以根据需要使用更复杂的布局或组件
        label.setBounds(30, 30, 500, 30); // 增加了宽度以防标题太长
        panel.add(label);

        JLabel placeholder = new JLabel("[Content for " + title + " goes here]");
        placeholder.setForeground(Color.GRAY);
        placeholder.setBounds(30, 80, 400, 30);
        panel.add(placeholder);

        return panel;
    }

    // 辅助方法：检查登录状态并切换卡片
    private void checkLoginAndShow(String cardName) {
        if (UserSession.getCurrentUsername() != null) {
            cardLayout.show(contentPanel, cardName);
        } else {
            showLoginError();
        }
    }

    // 显示建议内容的对话框
// 第一层弹窗：显示预算建议
    private void showSuggestionDialog() {
        JDialog suggestionDialog = new JDialog(this, "Suggestion", true);
        suggestionDialog.setSize(450, 250);
        suggestionDialog.setLayout(new BorderLayout(10, 10));

        // ===== 标题 =====
        JLabel titleLabel = new JLabel("Suggestion", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 60, 120)); // 深蓝色

        // ===== 正文内容 =====
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] lines = {
                "Anticipating a possible high expense next month,",
                "Your budget for this month is: $11,000",
                "Your savings goals for this month are: $2,750"
        };

        for (String line : lines) {
            JLabel label = new JLabel(line);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            contentPanel.add(label);
            contentPanel.add(Box.createVerticalStrut(10));
        }

        // ===== Continue 按钮 =====
        JButton continueButton = new JButton("Continue");
        continueButton.setForeground(new Color(30, 60, 120)); // 深蓝文字
        continueButton.setBackground(Color.WHITE);
        continueButton.setBorder(BorderFactory.createLineBorder(new Color(30, 60, 120), 2));
        continueButton.addActionListener(e -> {
            suggestionDialog.dispose();
            showDetailedAdviceDialog(); // 打开第二层弹窗
        });

        // ===== 组装界面 =====
        suggestionDialog.add(titleLabel, BorderLayout.NORTH);
        suggestionDialog.add(contentPanel, BorderLayout.CENTER);
        suggestionDialog.add(continueButton, BorderLayout.SOUTH);
        suggestionDialog.setVisible(true);
    }

    // 第二层弹窗：显示详细消费建议
    private void showDetailedAdviceDialog() {
        JDialog adviceDialog = new JDialog(this, "Detailed Advice", true);
        adviceDialog.setSize(500, 300);
        adviceDialog.setLayout(new BorderLayout(10, 10));

        // ===== 标题 =====
        JLabel titleLabel = new JLabel("Detailed Spending Advice", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));

        // ===== 正文内容（支持部分文本加粗）=====
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 使用 HTML 实现部分文本加粗
        String[] adviceLines = {
                "<html>You spend the most on <b>shopping</b> this month, and we suggest you cut back on this item.</html>",
                "<html>You have two large purchases on <b>entertainment</b> this month, and we recommend reducing single purchase amounts.</html>"
        };

        for (String line : adviceLines) {
            JLabel label = new JLabel(line);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            contentPanel.add(label);
            contentPanel.add(Box.createVerticalStrut(15));
        }

        // ===== 返回按钮 =====
        JButton backButton = new JButton("Back to Home Page");
        backButton.setForeground(new Color(30, 60, 120));
        backButton.setBackground(Color.WHITE);
        backButton.setBorder(BorderFactory.createLineBorder(new Color(30, 60, 120), 2));
        backButton.addActionListener(e -> adviceDialog.dispose());

        // ===== 组装界面 =====
        adviceDialog.add(titleLabel, BorderLayout.NORTH);
        adviceDialog.add(contentPanel, BorderLayout.CENTER);
        adviceDialog.add(backButton, BorderLayout.SOUTH);
        adviceDialog.setVisible(true);
    }

    // 显示预算调整的对话框
// 在 PersonalUI 类中添加的预算管理对话框方法
    private void showBudgetDialog() {
        JDialog budgetDialog = new JDialog(this, "Budget Management", true);
        budgetDialog.setSize(500, 250);
        budgetDialog.setLayout(new GridLayout(6, 1, 10, 10));

        // ===== 输入区域 =====
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel inputLabel = new JLabel("Change your budget goal:");
        JTextField budgetInput = new JTextField(15);
        inputPanel.add(inputLabel);
        inputPanel.add(budgetInput);

        // ===== 信息展示 =====
        JLabel currentBudgetLabel = new JLabel("Now your actual budget goal: 11,000 dollars");
        JLabel modeLabel = new JLabel("Now in: economical mode");
        JLabel reasonLabel = new JLabel("Because: The shopping festival is next month");

        // ===== 按钮区域 =====
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        // 恢复推荐按钮（深蓝文字 + 白色背景）
        JButton restoreButton = new JButton("Restore Intelligent Recommendation");
        restoreButton.addActionListener(e -> {
            budgetInput.setText("");
            currentBudgetLabel.setText("Now your actual budget goal: 11,000 dollars");
            modeLabel.setText("Now in: economical mode");
            reasonLabel.setText("Because: The shopping festival is next month");
        });

        // 返回按钮（深蓝文字 + 白色背景）
        JButton backButton = new JButton("Back to Home Page");
        backButton.addActionListener(e -> budgetDialog.dispose());

        // ===== 样式设置 =====
        Color textBlue = new Color(30, 60, 120); // 深蓝色文字
        Color buttonBg = Color.WHITE; // 按钮背景白色

        // 统一字体样式
        Font infoFont = new Font("Segoe UI", Font.PLAIN, 14);
        currentBudgetLabel.setFont(infoFont);
        modeLabel.setFont(infoFont);
        reasonLabel.setFont(infoFont);

        // 设置按钮样式
        restoreButton.setForeground(textBlue);
        restoreButton.setBackground(buttonBg);
        restoreButton.setBorder(BorderFactory.createLineBorder(textBlue, 1));

        backButton.setForeground(textBlue);
        backButton.setBackground(buttonBg);
        backButton.setBorder(BorderFactory.createLineBorder(textBlue, 1));

        // ===== 组装界面 =====
        buttonPanel.add(restoreButton);
        buttonPanel.add(backButton);

        budgetDialog.add(inputPanel);
        budgetDialog.add(currentBudgetLabel);
        budgetDialog.add(modeLabel);
        budgetDialog.add(reasonLabel);
        budgetDialog.add(buttonPanel);

        budgetDialog.setVisible(true);
    }

    // 创建统一风格的按钮
    private JButton createStyledButton(String text, Color textColor, Color bgColor, Dimension size, Font font) {
        JButton button = new JButton(text);
        button.setForeground(textColor);
        button.setBackground(bgColor);
        button.setPreferredSize(size);
        button.setFont(font);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // 添加悬停效果
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }

    // 辅助方法：显示登录错误信息
    private void showLoginError() {
        JOptionPane.showMessageDialog(this, "请先登录", "错误", JOptionPane.ERROR_MESSAGE);
    }


// 添加 main 方法用于测试
    /*
    public static void main(String[] args) {
        // 设置外观，可选
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 模拟用户登录
        UserSession.setCurrentUsername("TestUser");
        // 运行 UI
        SwingUtilities.invokeLater(() -> {
            new PersonalUI().setVisible(true);
        });
    }
    */
}