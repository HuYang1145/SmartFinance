package UI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import Model.BudgetAdvisor;
import Model.BudgetAdvisor.BudgetMode;
import Model.BudgetAdvisor.BudgetRecommendation;
import Model.TransactionCSVImporter;
import Model.UserSession;
import Person.IncomeExpenseChart;

//import UI.UIUtils;

public class PersonalUI extends JDialog {

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private SidebarPanel sidebarPanel;
    private ContentPanelManager contentManager; // 管理面板创建和添加

    // --- 财务建议面板的实例变量 ---
    private JPanel financialSuggestionPanel;
    private JLabel financialSuggestionTitle;
    private JLabel currentBudgetLabel;
    private JLabel currentSavingGoalLabel;
    private JLabel budgetModeLabel;
    private JLabel budgetReasonLabel;
    private JButton viewSuggestionButton;
    private JButton manageBudgetGoalButton;
    private boolean budgetExceeded80Percent = false; // 预算警告标志

    // --- 其他实例变量 ---
    private int currentReportCycleDays = 7; // 默认报告周期天数 (会话期间有效)
    private final String SOURCE_TRANSACTIONS_FILE = "transactions.csv"; // 导入源文件 (可与目标文件相同)
    private final String DESTINATION_TRANSACTIONS_FILE = "transactions.csv"; // 主要的交易记录文件
    // private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"); // 已移除未使用的常量


    public PersonalUI() {
        setTitle("Personal Account Center");
        setSize(950, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- 1. 创建侧边栏 ---
        sidebarPanel = new SidebarPanel(this);
        add(sidebarPanel, BorderLayout.WEST);

        // --- 2. 创建带 CardLayout 的内容面板区域 ---
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(new Color(30, 60, 120)); // 设置背景色

        // --- 3. 创建 ContentPanelManager 并初始化 *其他* 面板 ---
        // ContentPanelManager 将创建和添加除 financialSuggestion 之外的面板
        contentManager = new ContentPanelManager(this, contentPanel, cardLayout);
        contentManager.initializePanels(); // 确保此方法 *不会* 添加 "financialSuggestion"

        // --- 4. 创建并添加财务建议面板 ---
        createFinancialSuggestionPanel(); // 创建特定面板
        contentPanel.add(financialSuggestionPanel, "financialSuggestion"); // 添加到 CardLayout

        // --- 5. 将内容面板添加到主布局 ---
        add(contentPanel, BorderLayout.CENTER);

        // --- 6. 设置默认视图 ---
        cardLayout.show(contentPanel, "individualCenter"); // 显示初始面板

        // --- 7. 初始化预算检查计时器 ---
        Timer budgetCheckTimer = new Timer(60000, e -> checkBudgetThreshold()); // 每60秒检查一次
        budgetCheckTimer.start();

        // --- 8. 登录时初始数据加载/检查 ---
        String username = UserSession.getCurrentUsername();
        if (username != null) {
            // 导入交易记录 (考虑是否只应在登录/启动时执行一次)
            try {
                File sourceFile = new File(SOURCE_TRANSACTIONS_FILE);
                if (sourceFile.exists()) {
                    TransactionCSVImporter.importTransactions(sourceFile, DESTINATION_TRANSACTIONS_FILE);
                    System.out.println("交易记录导入完成 (PersonalUI 初始化)");
                } else {
                    System.out.println("源交易记录文件未找到，跳过导入: " + SOURCE_TRANSACTIONS_FILE);
                }
            } catch (IOException | IllegalArgumentException e) {
                System.err.println("导入交易记录失败 (PersonalUI 初始化): " + e.getMessage());
                // 可以选择性地显示用户友好的错误消息
                // JOptionPane.showMessageDialog(this, "导入交易记录时出错: " + e.getMessage(), "导入错误", JOptionPane.ERROR_MESSAGE);
            }
            // 立即更新显示并检查预算
            updateFinancialSuggestionDisplay();
            checkBudgetExceededOnLogin(username); // 登录时检查预算状态
        } else {
            // 即使用户未登录也更新显示 (将显示 N/A)
             updateFinancialSuggestionDisplay();
        }

        // --- 9. 使 UI 可见 ---
        setVisible(true);
    }

    // --- 切换 contentPanel 中卡片的方法 ---

    /**
     * 显示主个人中心面板。
     */
    public void showIndividualCenter() {
        checkLoginAndShowCard("individualCenter");
    }

    /**
     * 显示定期报告选项面板。
     */
    public void showReportOptions() {
        checkLoginAndShowCard("reportOptions");
    }

    /**
     * 显示详细报告视图面板。
     */
    public void showReportView() {
        checkLoginAndShowCard("reportView");
        // 考虑 ReportViewPanel 是否需要在显示时显式刷新数据
        // ReportViewPanel panel = (ReportViewPanel) contentManager.getPanel("reportView");
        // if (panel != null) { panel.loadReportData(); }
    }

    /**
     * 显示报告周期设置面板。
     */
    public void showCycleSettings() {
        checkLoginAndShowCard("reportCycleSettings");
        // 考虑面板是否需要显式刷新
        // ReportCycleSettingsPanel panel = (ReportCycleSettingsPanel) contentManager.getPanel("reportCycleSettings");
        // if (panel != null) { panel.refreshDisplay(); }
    }

    /**
     * 显示财务建议面板并更新其内容。
     */
    public void showFinancialSuggestion() {
         checkLoginAndShowCard("financialSuggestion"); // 首先切换卡片
         // 仅当用户在检查后已登录时才更新显示
        if (UserSession.getCurrentUsername() != null) {
             updateFinancialSuggestionDisplay();
         }
        // 此处不需要 else 子句，checkLoginAndShowCard 会处理登录错误弹窗
    }


    /**
     * 显示 AI 问答面板。
     */
    public void showAiQA() {
        checkLoginAndShowCard("aiQA");
    }

    /**
     * 显示支出占比占位符面板。
     */
    public void showSpendingProportion() {
        checkLoginAndShowCard("spendingProportion");
    }

    /**
     * 显示交易历史记录对话框。
     */
     public void showTransactionHistory() {
         checkLoginAndShowDialog(() -> new TransactionHistory(PersonalUI.this)); // 假设 TransactionHistory 接受 PersonalUI 作为父窗口
     }


    // --- 报告周期 Getter/Setter (仅会话期间有效) ---

    /**
     * 获取当前会话的报告周期持续时间（天）。
     * @return 报告周期的天数。
     */
    public int getReportCycleDays() {
        return currentReportCycleDays;
    }

    /**
     * 设置当前会话的报告周期持续时间（天）。
     * 该值不会跨登录持久化。
     * @param days 报告周期的新天数 (必须 > 0)。
     */
    public void setReportCycleDays(int days) {
        if (days > 0) {
            this.currentReportCycleDays = days;
            System.out.println("当前会话的报告周期设置为: " + days + " 天。");
            // 报告视图通常会在下次可见时刷新，或者有自己的刷新机制。
        } else {
            System.err.println("尝试设置无效的报告周期天数: " + days);
            JOptionPane.showMessageDialog(this, "报告周期必须是正数天数。", "无效输入", JOptionPane.WARNING_MESSAGE);
        }
    }


    // --- 财务建议和预算方法 ---

    /**
     * 创建并配置财务建议面板。
     */
    private void createFinancialSuggestionPanel() {
        financialSuggestionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 30));
        financialSuggestionPanel.setBackground(new Color(30, 60, 120)); // 深蓝色背景

        financialSuggestionTitle = new JLabel("财务管理建议"); // Financial Management Suggestion
        financialSuggestionTitle.setForeground(Color.WHITE);
        financialSuggestionTitle.setFont(new Font("Arial", Font.BOLD, 24));
        financialSuggestionTitle.setHorizontalAlignment(SwingConstants.CENTER); // 标题居中

        currentBudgetLabel = new JLabel("当前预算: 加载中..."); // Current Budget: Loading...
        currentBudgetLabel.setForeground(Color.WHITE);
        currentBudgetLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        currentSavingGoalLabel = new JLabel("储蓄目标: 加载中..."); // Saving Goal: Loading...
        currentSavingGoalLabel.setForeground(Color.WHITE);
        currentSavingGoalLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        budgetModeLabel = new JLabel("预算模式: 加载中..."); // Budget Mode: Loading...
        budgetModeLabel.setForeground(Color.WHITE);
        budgetModeLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        budgetReasonLabel = new JLabel("原因: 加载中..."); // Reason: Loading...
        budgetReasonLabel.setForeground(Color.WHITE);
        budgetReasonLabel.setFont(new Font("Arial", Font.PLAIN, 14)); // 原因使用稍小字体

        viewSuggestionButton = new JButton("查看详细建议"); // View Detailed Suggestion
        styleButton(viewSuggestionButton);
        viewSuggestionButton.setPreferredSize(new Dimension(220, 40));
        viewSuggestionButton.addActionListener(e -> showBudgetSuggestionDialog());

        manageBudgetGoalButton = new JButton("管理预算目标"); // Manage Budget Goal
        styleButton(manageBudgetGoalButton);
        manageBudgetGoalButton.setPreferredSize(new Dimension(220, 40));
        manageBudgetGoalButton.addActionListener(e -> checkLoginAndShowDialog(() -> {
            // 这个 lambda 现在正确使用了 checkLoginAndShowDialog 工具方法
            BudgetGoalDialog budgetDialog = new BudgetGoalDialog(PersonalUI.this); // 传递父窗口/对话框
            budgetDialog.setVisible(true);
             // 对话框关闭后，刷新显示
             updateFinancialSuggestionDisplay();
        }));

        // 用于显示文本信息（预算、目标、模式、原因）的面板
        JPanel suggestionInfoPanel = new JPanel(new GridLayout(4, 1, 10, 10)); // 行, 列, 水平间距, 垂直间距
        suggestionInfoPanel.setOpaque(false); // 使其透明以显示父面板背景
        // suggestionInfoPanel.setBackground(new Color(30, 60, 120)); // 或者匹配父背景色
        suggestionInfoPanel.add(currentBudgetLabel);
        suggestionInfoPanel.add(currentSavingGoalLabel);
        suggestionInfoPanel.add(budgetModeLabel);
        suggestionInfoPanel.add(budgetReasonLabel);
        suggestionInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // 添加内边距

        // 用于放置按钮的面板
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10)); // 行, 列, 水平间距, 垂直间距
        buttonPanel.setOpaque(false); // 使其透明
        // buttonPanel.setBackground(new Color(30, 60, 120)); // 或者匹配父背景色
        buttonPanel.add(viewSuggestionButton);
        buttonPanel.add(manageBudgetGoalButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // 添加上下边距

        // 主面板，垂直排列标题、信息和按钮
        JPanel mainSuggestionPanel = new JPanel(new BorderLayout(0, 25)); // 组件间的垂直间距
        mainSuggestionPanel.setOpaque(false); // 使其透明
        // mainSuggestionPanel.setBackground(new Color(30, 60, 120)); // 或者匹配父背景色
        mainSuggestionPanel.add(financialSuggestionTitle, BorderLayout.NORTH);
        mainSuggestionPanel.add(suggestionInfoPanel, BorderLayout.CENTER);
        mainSuggestionPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainSuggestionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // 添加外边距

        // 将主面板添加到 financialSuggestionPanel 的流式布局中
        financialSuggestionPanel.add(mainSuggestionPanel);
    }


    /**
     * 对 JButton 应用标准样式。
     * @param button 要设置样式的按钮。
     */
    private void styleButton(JButton button) {
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(30, 60, 120)); // 深蓝色文本
        button.setFont(new Font("Arial", Font.BOLD, 16)); // 粗体
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // 设置手型光标
        // 可选：添加悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.LIGHT_GRAY); // 悬停时背景变浅
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE); // 恢复原始背景
            }
        });
    }

    /**
     * 根据当前用户数据更新财务建议面板上的标签。
     */
    public void updateFinancialSuggestionDisplay() {
        String username = UserSession.getCurrentUsername();
        LocalDate now = LocalDate.now();
        if (username != null) {
            try {
                BudgetRecommendation recommendation = BudgetAdvisor.calculateRecommendation(username, now);
                Double customBudget = BudgetAdvisor.getCustomBudget(username);
                double displayBudget = (customBudget != null) ? customBudget : recommendation.suggestedBudget;

                currentBudgetLabel.setText(String.format("当前预算: $%.2f", displayBudget));
                currentSavingGoalLabel.setText(String.format("储蓄目标: $%.2f", recommendation.suggestedSaving));
                budgetModeLabel.setText("预算模式: " + recommendation.mode.getDisplayName());
                // 使用 HTML 换行以防原因过长
                budgetReasonLabel.setText("<html><body style='width: 200px;'>原因: " + recommendation.reason + "</body></html>");

            } catch (Exception e) {
                 System.err.println("更新财务建议显示时出错: " + e.getMessage());
                 e.printStackTrace(); // 记录完整错误
                 currentBudgetLabel.setText("当前预算: 错误");
                 currentSavingGoalLabel.setText("储蓄目标: 错误");
                 budgetModeLabel.setText("预算模式: 错误");
                 budgetReasonLabel.setText("原因: 加载数据出错");
            }
        } else {
            currentBudgetLabel.setText("当前预算: N/A (未登录)");
            currentSavingGoalLabel.setText("储蓄目标: N/A");
            budgetModeLabel.setText("预算模式: N/A");
            budgetReasonLabel.setText("原因: N/A");
        }
    }

    /**
     * 显示包含详细预算建议的对话框。
     */
    private void showBudgetSuggestionDialog() {
        String username = UserSession.getCurrentUsername();
        if (username == null) {
            UIUtils.showLoginError(this); // 使用工具函数
            return;
        }

         LocalDate now = LocalDate.now();
         try{
             BudgetRecommendation rec = BudgetAdvisor.calculateRecommendation(username, now);
             Double customBudget = BudgetAdvisor.getCustomBudget(username);
             double budget = (customBudget != null) ? customBudget : rec.suggestedBudget; // 如果设置了自定义预算则使用
             double savingGoal = rec.suggestedSaving; // 显示建议的储蓄目标
             BudgetMode mode = rec.mode;
             String reason = rec.reason;
             boolean hasPastData = rec.hasPastData; // 检查建议是否基于历史数据

             StringBuilder message = new StringBuilder("<html><body style='width: 300px;'>"); // 基本样式设置宽度
             message.append("<b><font size='+1'>本月预算建议</font></b><br><br>"); // Budget Suggestion for This Month

             // 解释建议的基础
             if (mode == BudgetMode.CUSTOM) {
                 message.append("基于您的<b>自定义设置</b>:<br>"); // Based on your custom settings
             } else if (hasPastData) {
                 message.append("基于您<b>过去三个月</b>的消费模式:<br>"); // Based on your spending patterns from the last three months
             } else {
                 message.append("基于<b>通用建议</b> (无足够历史记录):<br>"); // Based on general recommendations (no sufficient history)
             }

             // 如果不是 CUSTOM 模式，添加特定模式的上下文
             if (mode == BudgetMode.ECONOMICAL_FESTIVAL) {
                 message.append("<i>注意: 下个月有购物节，建议采取更经济的策略。</i><br>"); // Note: Next month includes a shopping festival...
             } else if (mode == BudgetMode.ECONOMICAL_UNSTABLE) {
                 message.append("<i>注意: 近期消费波动较大，建议采取更谨慎的预算。</i><br>"); // Note: Recent spending was variable...
             }

             // 显示预算和储蓄目标
             message.append("建议月度预算: <b>$").append(String.format("%.2f", budget)).append("</b><br>"); // Suggested Monthly Budget
             message.append("建议月度储蓄目标: <b>$").append(String.format("%.2f", savingGoal)).append("</b><br><br>"); // Suggested Monthly Saving Goal

             // 显示模式和原因
             message.append("预算模式: ").append(mode.getDisplayName()).append("<br>"); // Budget Mode
             message.append("原因: ").append(reason).append("<br>"); // Reason

             message.append("</body></html>");

             JOptionPane.showMessageDialog(
                     this,
                     message.toString(),
                     "预算建议详情", // Budget Suggestion Details
                     JOptionPane.INFORMATION_MESSAGE
             );
        } catch (Exception e) {
             System.err.println("生成预算建议对话框时出错: " + e.getMessage());
             JOptionPane.showMessageDialog(this, "无法检索预算建议详情。", "错误", JOptionPane.ERROR_MESSAGE); // Could not retrieve budget suggestion details.
        }
    }

   /**
    * 检查用户的支出是否已超过当月预算的 80%。
    * 如果超过阈值，则在本会话中仅显示一次警告消息。
    */
    private void checkBudgetThreshold() {
        String username = UserSession.getCurrentUsername();
        // 仅在用户已登录且本会话尚未显示警告时检查
        if (username != null && !budgetExceeded80Percent) {
             try {
                 List<BudgetAdvisor.Transaction> transactionsThisMonth = getTransactionsForCurrentMonth(username);
                 LocalDate now = LocalDate.now();
                 LocalDate startOfMonth = now.withDayOfMonth(1);
                 LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

                 // 计算总支出（转出、取款，可能还包括支付？）
                 // 需要明确哪些操作计入预算支出。
                 double totalExpenseThisMonth = transactionsThisMonth.stream()
                         .filter(t -> (t.operation.equalsIgnoreCase("Transfer Out") || t.operation.equalsIgnoreCase("Withdrawal") || t.operation.equalsIgnoreCase("Payment")) && // 添加 Payment?
                                 !t.date.isBefore(startOfMonth) && !t.date.isAfter(endOfMonth)) // 日期检查（包含边界）
                         .mapToDouble(t -> Math.abs(t.amount)) // 使用绝对值计算支出
                         .sum();

                 // 获取当前预算（自定义或推荐）
                 Double currentBudgetObj = BudgetAdvisor.getCustomBudget(username); // Use Object type first
                 double currentBudget; // Use primitive double for calculations
                 if (currentBudgetObj == null) {
                     BudgetRecommendation recommendation = BudgetAdvisor.calculateRecommendation(username, now);
                     currentBudget = recommendation.suggestedBudget; // Use recommended
                 } else {
                     currentBudget = currentBudgetObj; // Use custom
                 }


                 // 如果预算已设置且为正数，则执行检查
                 if (currentBudget > 0) {
                     double eightyPercentBudget = currentBudget * 0.8;
                     if (totalExpenseThisMonth >= eightyPercentBudget) {
                         // **创建 final 副本供 Lambda 使用**
                         final double finalTotalExpense = totalExpenseThisMonth;
                         final double finalCurrentBudget = currentBudget;
                         final double finalRemainingBudget = finalCurrentBudget - finalTotalExpense;

                         // 确保警告显示在事件分发线程上
                         SwingUtilities.invokeLater(() -> {
                             JOptionPane.showMessageDialog(
                                     this,
                                     // **在 Lambda 内部使用 final 副本**
                                     String.format("预算警告: 您本月已使用 $%.2f (%.1f%%)，预算为 $%.2f。\n" +
                                                     "剩余预算: $%.2f",
                                             finalTotalExpense, (finalTotalExpense / finalCurrentBudget) * 100, finalCurrentBudget, finalRemainingBudget),
                                     "预算警告 (达到80%)", // Budget Warning (80% Reached)
                                     JOptionPane.WARNING_MESSAGE
                             );
                         });
                         budgetExceeded80Percent = true; // 设置标志以防止本会话重复警告
                     }
                 }
            } catch (Exception e) {
                 System.err.println("检查预算阈值时出错: " + e.getMessage());
                 // 避免为后台任务显示错误弹窗，除非是关键错误
            }
        }
         // 是否需要在月份更改或登出/登录时重置标志？
         // 目前，它在创建新的 PersonalUI 实例（新登录）时重置。
    }


   /**
    * 在用户登录时执行初始检查，查看其预算在登录时是否已超支（或超过 80%）。
    * 这与 checkBudgetThreshold 类似，但在初始化时运行一次。
    * @param username 已登录用户的用户名。
    */
    private void checkBudgetExceededOnLogin(String username) {
         // 这个逻辑与 checkBudgetThreshold 非常相似。
         // 如果检查完全相同，考虑重构以避免重复。
        try {
            List<BudgetAdvisor.Transaction> transactionsThisMonth = getTransactionsForCurrentMonth(username);
            LocalDate now = LocalDate.now();
            LocalDate startOfMonth = now.withDayOfMonth(1);
            LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

            double totalExpenseThisMonth = transactionsThisMonth.stream()
                    .filter(t -> (t.operation.equalsIgnoreCase("Transfer Out") || t.operation.equalsIgnoreCase("Withdrawal") || t.operation.equalsIgnoreCase("Payment")) && // 与 checkBudgetThreshold 保持一致
                             !t.date.isBefore(startOfMonth) && !t.date.isAfter(endOfMonth))
                    .mapToDouble(t -> Math.abs(t.amount))
                    .sum();

            Double currentBudgetObj = BudgetAdvisor.getCustomBudget(username);
            double currentBudget;
             if (currentBudgetObj == null) {
                 BudgetRecommendation recommendation = BudgetAdvisor.calculateRecommendation(username, LocalDate.now());
                 currentBudget = recommendation.suggestedBudget;
             } else {
                currentBudget = currentBudgetObj;
             }


            if (currentBudget > 0) {
                double eightyPercentBudget = currentBudget * 0.8;
                if (totalExpenseThisMonth >= eightyPercentBudget) {
                    // 检查是否已完全超支
                    if (totalExpenseThisMonth >= currentBudget) {
                         // **创建 final 副本供 Lambda 使用**
                         final double finalCurrentBudgetExceeded = currentBudget;
                         final double finalOverBudgetBy = totalExpenseThisMonth - currentBudget;

                         SwingUtilities.invokeLater(() -> { // 确保 UI 更新在 EDT 上
                             JOptionPane.showMessageDialog(
                                 this,
                                 // **在 Lambda 内部使用 final 副本**
                                 String.format("预算警报: 您本月已超出预算 $%.2f，超出金额 $%.2f。", // Budget Alert: You have already exceeded your budget...
                                         finalCurrentBudgetExceeded, finalOverBudgetBy),
                                 "预算已超支", // Budget Exceeded
                                 JOptionPane.WARNING_MESSAGE
                             );
                         });
                    } else {
                         // 刚超过 80%，显示标准警告
                         // **创建 final 副本供 Lambda 使用**
                         final double finalCurrentBudgetWarning = currentBudget;
                         final double finalRemainingBudgetWarning = currentBudget - totalExpenseThisMonth;

                         SwingUtilities.invokeLater(() -> { // 确保 UI 更新在 EDT 上
                            JOptionPane.showMessageDialog(
                                    this,
                                    // **在 Lambda 内部使用 final 副本**
                                    String.format("预算警告: 您即将超出本月预算。\n" +
                                                  "您的预算 $%.2f 中还剩 $%.2f。", // Budget Warning: You are close to exceeding...
                                                  finalCurrentBudgetWarning, finalRemainingBudgetWarning), // 注意参数顺序
                                    "预算警告", // Budget Warning
                                    JOptionPane.WARNING_MESSAGE
                            );
                         });
                    }
                    budgetExceeded80Percent = true; // 设置标志，以便计时器不会立即再次警告
                }
            }
         } catch (Exception e) {
             System.err.println("登录时检查预算出错: " + e.getMessage());
         }
    }


    /**
     * 检索指定用户在当前日历月内的所有交易记录。
     * 从 DESTINATION_TRANSACTIONS_FILE 读取。
     * @param username 要筛选交易记录的用户名。
     * @return 用户在当前月份的 Transaction 对象列表。
     */
     private List<BudgetAdvisor.Transaction> getTransactionsForCurrentMonth(String username) {
         List<BudgetAdvisor.Transaction> userTransactions = new ArrayList<>();
         LocalDate now = LocalDate.now();
         LocalDate startOfMonth = now.withDayOfMonth(1);
         LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
         File transactionFile = new File(DESTINATION_TRANSACTIONS_FILE);

         if (!transactionFile.exists()) {
             System.err.println("交易记录文件未找到: " + DESTINATION_TRANSACTIONS_FILE);
             return userTransactions; // 返回空列表
         }

         // 使用 try-with-resources 确保 BufferedReader 被关闭
         try (BufferedReader br = new BufferedReader(new FileReader(transactionFile))) {
             String line;
             br.readLine(); // 跳过标题行
             while ((line = br.readLine()) != null) {
                 // 使用更安全的分割方式，限制次数，防止逗号在详情中引起问题
                 String[] data = line.split(",", 5); // Limit splits, details in last part
                 // 基本验证：检查数组长度和用户名匹配 (忽略大小写)
                 if (data.length >= 5 && data[0].trim().equalsIgnoreCase(username)) {
                     try {
                         // 使用 BudgetAdvisor 的日期格式化器保持一致性
                         // 假设日期在第 3 列 (索引 3), 格式为 yyyy/MM/dd 或 yyyy/MM/dd HH:mm
                         String dateString = data[3].trim();
                         LocalDate transactionDate;
                         // 尝试解析日期，处理可能的空格和时间部分
                         if (dateString.contains(" ")) {
                             transactionDate = LocalDate.parse(dateString.split(" ")[0], BudgetAdvisor.DATE_FORMATTER);
                         } else {
                             transactionDate = LocalDate.parse(dateString, BudgetAdvisor.DATE_FORMATTER);
                         }


                         // 检查交易日期是否在当前月份内 (包含 startOfMonth 和 endOfMonth)
                         if (!transactionDate.isBefore(startOfMonth) && !transactionDate.isAfter(endOfMonth)) {
                             String operation = data[1].trim(); // 操作在第 1 列 (索引 1)
                             double amount = Double.parseDouble(data[2].trim()); // 金额在第 2 列 (索引 2)
                             String details = data[4].trim(); // 详情在第 4 列 (索引 4)

                             // 创建 BudgetAdvisor.Transaction 对象
                             BudgetAdvisor.Transaction transaction = new BudgetAdvisor.Transaction();
                             // transaction.username = username; // BudgetAdvisor 内部可能不需要这个
                             transaction.operation = operation;
                             transaction.amount = amount;
                             transaction.date = transactionDate;
                             transaction.details = details;
                             // 如果需要，可以解析并添加交易时间
                             // try { transaction.dateTime = LocalDateTime.parse(dateString, BudgetAdvisor.DATE_TIME_FORMATTER); } catch (Exception ignore) {}

                             userTransactions.add(transaction);
                         }
                     } catch (DateTimeParseException e) {
                         System.err.println("解析交易记录日期时出错: " + line + " - " + e.getMessage());
                     } catch (NumberFormatException e) {
                         System.err.println("解析交易记录金额时出错: " + line + " - " + e.getMessage());
                     } catch (ArrayIndexOutOfBoundsException e) {
                          System.err.println("解析行时出错 (字段缺失?): " + line + " - " + e.getMessage());
                     }
                 }
             }
         } catch (IOException e) {
             System.err.println("读取交易记录文件 '" + DESTINATION_TRANSACTIONS_FILE + "' 时出错: " + e.getMessage());
             // 在关键情况下可能需要向用户显示错误
         }
         return userTransactions;
     }


    // --- 由 SidebarPanel 或其他组件调用的方法 ---

    /**
     * 显示收入/支出饼图窗口。首先检查登录状态。
     */
    public void showIncomeExpenseChart() {
        System.out.println("尝试显示 IncomeExpenseChart..."); // 调试信息
        checkLoginAndShowDialog(() -> {
             // 假设 IncomeExpenseChart.showIncomeExpensePieChart 是静态方法
             // 并且它自己处理了潜在的文件读取错误。
            IncomeExpenseChart.showIncomeExpensePieChart(DESTINATION_TRANSACTIONS_FILE);
        });
    }

    /**
     * 在显示特定卡片面板之前检查用户是否已登录。
     * 如果未登录，则显示错误消息。
     * @param cardName 要在 CardLayout 中显示的卡片面板的名称。
     */
    public void checkLoginAndShowCard(String cardName) {
        if (UserSession.getCurrentUsername() != null) {
            System.out.println("切换到卡片: " + cardName); // 调试信息
            cardLayout.show(contentPanel, cardName);
        } else {
            System.out.println("需要登录才能显示卡片: " + cardName); // 调试信息
            UIUtils.showLoginError(this); // 使用工具方法
        }
    }

    /**
     * 在执行操作（通常是显示对话框）之前检查用户是否已登录。
     * 操作在事件分发线程上执行。
     * 如果未登录，则显示错误消息。
     * @param action 包含要执行操作的 Runnable (例如，创建并显示对话框)。
     */
    public void checkLoginAndShowDialog(Runnable action) {
        if (UserSession.getCurrentUsername() != null) {
            // 确保操作（对话框创建/显示）在 EDT 上发生
            SwingUtilities.invokeLater(action);
        } else {
            System.out.println("需要登录才能执行此操作。"); // 调试信息
            UIUtils.showLoginError(this); // 使用工具方法
        }
    }

    /**
     * 处理登出过程，包括确认和清除用户会话。
     */
    public void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "您确定要登出吗?", "确认登出", // Are you sure you want to log out? Confirm Logout
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            UserSession.clearSession(); // 从会话中清除用户名
            budgetExceeded80Percent = false; // 登出时重置预算警告标志
            dispose(); // 关闭此 PersonalUI 窗口

            // 可选：返回登录屏幕 (App) 的逻辑
            // 这在很大程度上取决于你的主应用程序 (App) 是如何构建的。
            // 示例：如果主 App 框架可通过静态方法或上下文对象访问：
            // MainAppFrame.getInstance().setVisible(true);
            // 或者如果旧实例已被销毁，则创建新的登录实例：
             SwingUtilities.invokeLater(() -> new App().setVisible(true)); // 假设 App 是你的主登录类
        }
    }

     /**
      * 按名称显示特定卡片的辅助方法。主要供内部使用或简单情况。
      * 不执行登录检查。
      * @param cardName 要显示的卡片的名称。
      */
     public void showCard(String cardName) {
         cardLayout.show(contentPanel, cardName);
     }


    // 可选的辅助方法，用于在登出后查找父 App 框架（如果需要）
    /*
    private Frame findParentAppFrame() {
        Container parent = getParent();
        while (parent != null && !(parent instanceof Frame)) {
            parent = parent.getParent();
        }
        // 如果对话框未添加到 Frame 中，这可能返回 null
        return (Frame) parent;
    }
    */

    // --- 假设存在的工具类和依赖类 (用于编译和示例) ---

    // 假设 UIUtils 类存在于你的项目中
    // 你可能需要根据项目结构调整包/位置
    private static class UIUtils {
        public static void showLoginError(Component parentComponent) {
            JOptionPane.showMessageDialog(parentComponent,
                    "您必须登录才能访问此功能。", // You must be logged in to access this feature.
                    "需要登录", JOptionPane.ERROR_MESSAGE); // Login Required
        }
    }

     // 假设 App 类存在，用于登出重定向
     // 你可能需要正确导入它
     private static class App extends JFrame {
          // 用于编译的虚拟实现
          public App() { /* ... 登录屏幕设置 ... */
              // Example setup:
              setTitle("Login");
              setSize(400, 300);
              setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
              setLocationRelativeTo(null);
              // Add login components...
          }
     }

     // 假设 TransactionHistory 类存在
     // 你可能需要正确导入它
     private static class TransactionHistory extends JDialog {
          // 用于编译的虚拟实现
          public TransactionHistory(Window owner) {
               super(owner, "Transaction History", ModalityType.APPLICATION_MODAL);
               setSize(600, 400);
               setLocationRelativeTo(owner);
              // Add history components...
          }
     }

     // 假设 BudgetGoalDialog 类存在
     private static class BudgetGoalDialog extends JDialog {
         public BudgetGoalDialog(Window owner){
              super(owner, "Manage Budget Goal", ModalityType.APPLICATION_MODAL);
               setSize(400, 300);
               setLocationRelativeTo(owner);
              // Add budget goal components...
         }
     }

     // 假设 ContentPanelManager 类存在
     private static class ContentPanelManager {
         public ContentPanelManager(PersonalUI owner, JPanel contentPanel, CardLayout layout) { /* ... */ }
         public void initializePanels() { /* ... Add panels like individualCenter, reportOptions etc. EXCEPT financialSuggestion */ }
         // public Component getPanel(String name) { /* ... return panel by name ... */ return null;}
     }

     // 假设 SidebarPanel 类存在
     private static class SidebarPanel extends JPanel {
         public SidebarPanel(PersonalUI owner) { /* ... Add buttons and action listeners calling owner.show...() methods ... */ }
     }

} // End of PersonalUI class
