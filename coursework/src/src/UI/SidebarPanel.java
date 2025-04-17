package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import Person.*;
import model.UserSession;

public class SidebarPanel extends JPanel {
    private PersonalUI parentUI;

    public SidebarPanel(PersonalUI parent) {
        this.parentUI = parent;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(230, 0));
        setBackground(new Color(30, 60, 120));

        initializeButtons();
    }

    private void initializeButtons() {
        // 按钮尺寸
        Dimension btnSize = new Dimension(210, 38);

        // 创建所有侧边栏按钮
        JButton individualCenterButton = createSidebarButton("Individual Center", "/UI/icons/home.png", btnSize);
        JButton viewBalanceButton = createSidebarButton("View Balance", "/UI/icons/balance.png", btnSize);
        JButton depositButton = createSidebarButton("Deposit", "/UI/icons/deposit.png", btnSize);
        JButton withdrawalButton = createSidebarButton("Withdrawal", "/UI/icons/atm.png", btnSize);
        JButton transferButton = createSidebarButton("Transfer", "/UI/icons/transfer_out.png", btnSize);
        JButton transactionHistoryButton = createSidebarButton("Transaction History", "/UI/icons/history.png", btnSize);
        JButton monthlyIncomeExpenseButton = createSidebarButton("Monthly Income Expense", "/UI/icons/chart.png", btnSize);
        JButton periodicReportButton = createSidebarButton("Periodic Reporting", "/UI/icons/report.png", btnSize);
        JButton spendingProportionButton = createSidebarButton("Spending Proportion", "/UI/icons/pie_chart.png", btnSize);
        JButton financialSuggestionButton = createSidebarButton("Financial Suggestion", "/UI/icons/suggestion.png", btnSize);
        JButton aiQAButton = createSidebarButton("AI Q&A", "/UI/icons/ai.png", btnSize);
        JButton viewPersonalInfoButton = createSidebarButton("View Personal Info", "/UI/icons/person.png", btnSize);
        JButton logoutButton = createSidebarButton("Log out", "/UI/icons/logout.png", btnSize);

        // 添加按钮到侧边栏 (逻辑顺序)
        add(Box.createVerticalGlue());
        add(individualCenterButton);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(viewBalanceButton);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(depositButton);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(withdrawalButton);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(transferButton);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(transactionHistoryButton);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(monthlyIncomeExpenseButton);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(periodicReportButton);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(spendingProportionButton);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(financialSuggestionButton);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(aiQAButton);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(viewPersonalInfoButton);
        add(Box.createVerticalGlue());
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(logoutButton);


        // 添加按钮事件
        configureButtonActions(
                individualCenterButton, viewBalanceButton, depositButton,
                withdrawalButton, transferButton, transactionHistoryButton,
                monthlyIncomeExpenseButton,
                periodicReportButton, spendingProportionButton,
                financialSuggestionButton, aiQAButton, viewPersonalInfoButton, logoutButton
        );
    }

    private void configureButtonActions(JButton individualCenterButton, JButton viewBalanceButton,
                                         JButton depositButton, JButton withdrawalButton,
                                         JButton transferButton, JButton transactionHistoryButton,
                                         JButton monthlyIncomeExpenseButton,
                                         JButton periodicReportButton, JButton spendingProportionButton,
                                         JButton financialSuggestionButton, JButton aiQAButton,
                                         JButton viewPersonalInfoButton, JButton logoutButton) {

        // 卡片布局相关按钮
        individualCenterButton.addActionListener(e -> parentUI.checkLoginAndShowCard("individualCenter"));
        periodicReportButton.addActionListener(e -> parentUI.checkLoginAndShowCard("periodicReport"));
        spendingProportionButton.addActionListener(e -> parentUI.checkLoginAndShowCard("spendingProportion"));
        financialSuggestionButton.addActionListener(e -> parentUI.checkLoginAndShowCard("financialSuggestion"));
        aiQAButton.addActionListener(e -> parentUI.checkLoginAndShowCard("aiQA"));

        // 对话框相关按钮
        viewBalanceButton.addActionListener(e -> parentUI.checkLoginAndShowDialog(() ->
                new ViewBalance(parentUI, UserSession.getCurrentUsername()).setVisible(true)
        ));
        depositButton.addActionListener(e -> parentUI.checkLoginAndShowDialog(() ->
                new Deposit(parentUI, UserSession.getCurrentUsername()).setVisible(true)
        ));
        withdrawalButton.addActionListener(e -> parentUI.checkLoginAndShowDialog(() ->
                new Withdrawal(parentUI, UserSession.getCurrentUsername()).setVisible(true)
        ));
        transferButton.addActionListener(e -> parentUI.checkLoginAndShowDialog(() ->
                new transfer(parentUI).setVisible(true)
        ));
        transactionHistoryButton.addActionListener(e -> parentUI.checkLoginAndShowDialog(() ->
                new TransactionHistory(parentUI).setVisible(true)
        ));
        viewPersonalInfoButton.addActionListener(e -> parentUI.checkLoginAndShowDialog(() ->
                new ViewPersonalInfo(parentUI, UserSession.getCurrentUsername()).setVisible(true)
        ));

        // 月度收入支出分类按钮
        monthlyIncomeExpenseButton.addActionListener(e -> parentUI.showIncomeExpenseChart()); // 添加这行

        // 登出按钮
        logoutButton.addActionListener(e -> parentUI.handleLogout());
    }

    private JButton createSidebarButton(String text, String iconPath, Dimension size) {
        ImageIcon icon = loadIcon(iconPath, 24, 24);

        JButton button = new JButton(text, icon);
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        button.setMinimumSize(size);

        // 样式设置
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBackground(new Color(20, 40, 80));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 文本/图标对齐
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(12);
        button.setMargin(new Insets(5, 15, 5, 15));

        // 悬停效果
        addHoverEffect(button);

        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        return button;
    }

    private ImageIcon loadIcon(String iconPath, int width, int height) {
        if (iconPath == null || iconPath.isEmpty()) {
            return null;
        }

        try {
            URL imgURL = getClass().getResource(iconPath);
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            } else {
                System.err.println("Icon not found: " + iconPath);
            }
        } catch (Exception e) {
            System.err.println("Error loading icon: " + iconPath + " - " + e);
            e.printStackTrace();
        }

        return null;
    }

    private void addHoverEffect(JButton button) {
        Color originalColor = button.getBackground();
        Color hoverColor = new Color(45, 75, 140); // 悬停时的亮蓝色

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(hoverColor);
            }

            public void mouseExited(MouseEvent evt) {
                button.setBackground(originalColor);
            }
        });
    }
}