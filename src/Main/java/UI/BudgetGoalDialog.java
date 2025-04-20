package UI;

import Model.BudgetAdvisor;
import Model.BudgetAdvisor.BudgetRecommendation;
import Model.BudgetAdvisor.BudgetMode;
import Model.UserSession;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class BudgetGoalDialog extends JDialog {
    private JTextField customBudgetTextField;
    private JLabel actualBudgetLabel;
    private JLabel modeLabel;
    private JLabel reasonLabel;
    private PersonalUI parentUI;
    private BudgetRecommendation currentRecommendation;

    public BudgetGoalDialog(PersonalUI parent) {
        super(parent, "Manage Budget Goal", true);
        this.parentUI = parent;
        setLayout(new GridLayout(5, 1, 10, 10));
        setPreferredSize(new Dimension(400, 500));
        initComponents();
        updateBudgetInfo();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
// 第一行：自定义预算输入
        JPanel customBudgetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel customBudgetLabel = new JLabel("Set your custom budget:");
        customBudgetTextField = new JTextField(10);
        customBudgetPanel.add(customBudgetLabel);
        customBudgetPanel.add(customBudgetTextField);
        customBudgetPanel.add(new JLabel("dollars"));
        add(customBudgetPanel);

// 第二行：当前实际预算
        JPanel actualBudgetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actualBudgetLabel = new JLabel("Current Budget: N/A dollars");
        actualBudgetPanel.add(actualBudgetLabel);
        add(actualBudgetPanel);

// 第三行：当前模式
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modeLabel = new JLabel("Budget Mode: N/A");
        modePanel.add(modeLabel);
        add(modePanel);

// 第四行：模式原因
        JPanel reasonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reasonLabel = new JLabel("Reason: N/A");
        reasonPanel.add(reasonLabel);
        add(reasonPanel);

// 第五行：按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton restoreButton = new JButton("Restore Intelligent Recommendation");
        restoreButton.setBackground(Color.WHITE);
        restoreButton.setForeground(Color.BLACK);
        restoreButton.addActionListener(e -> {
            String username = UserSession.getCurrentUsername();
            if (username != null) {
                BudgetAdvisor.clearCustomBudget(username);
                updateBudgetInfo();
                parentUI.updateFinancialSuggestionDisplay(); // 更新 PersonalUI 的建议显示
                JOptionPane.showMessageDialog(BudgetGoalDialog.this, "Intelligent recommendation restored.");
            } else {
                JOptionPane.showMessageDialog(BudgetGoalDialog.this, "Please log in to restore budget.");
            }
        });

        JButton backButton = new JButton("Back to Financial Suggestions");
        backButton.setBackground(Color.WHITE);
        backButton.setForeground(Color.BLACK);
        backButton.addActionListener(e -> {
            saveCustomBudget(); // 点击返回前保存自定义预算
            dispose();
            parentUI.showCard("financialSuggestion"); // 返回财务建议面板
            parentUI.updateFinancialSuggestionDisplay(); // 更新 PersonalUI 的建议显示
        });

        buttonPanel.add(restoreButton);
        buttonPanel.add(backButton);
        add(buttonPanel);

// 添加保存自定义预算的监听器
        customBudgetTextField.addActionListener(e -> saveCustomBudget());
    }

    private void updateBudgetInfo() {
        String username = UserSession.getCurrentUsername();
        if (username != null) {
            // 获取当前日期
            LocalDate now = LocalDate.now();
            // 调用 calculateRecommendation 方法时传递用户名和当前日期
            currentRecommendation = BudgetAdvisor.calculateRecommendation(username, now);
            actualBudgetLabel.setText("Current Budget: $" + String.format("%.2f", currentRecommendation.suggestedBudget) + " dollars");
            modeLabel.setText("Budget Mode: " + currentRecommendation.mode.getDisplayName());

            if (currentRecommendation.mode == BudgetMode.CUSTOM) {
                reasonLabel.setText("Reason: User defined budget goal.");
                Double customBudget = BudgetAdvisor.getCustomBudget(username);
                if (customBudget != null) {
                    customBudgetTextField.setText(String.format("%.2f", customBudget));
                } else {
                    customBudgetTextField.setText("");
                }
            } else {
                reasonLabel.setText("Reason: " + currentRecommendation.mode.getReason());
                customBudgetTextField.setText(""); // 清空自定义预算输入框
            }
        } else {
            actualBudgetLabel.setText("Current Budget: N/A dollars");
            modeLabel.setText("Budget Mode: N/A");
            reasonLabel.setText("Reason: N/A");
            customBudgetTextField.setText("");
        }
    }

    private void saveCustomBudget() {
        String username = UserSession.getCurrentUsername();
        if (username != null) {
            String customBudgetText = customBudgetTextField.getText();
            if (!customBudgetText.isEmpty()) {
                try {
                    double customBudget = Double.parseDouble(customBudgetText);
                    BudgetAdvisor.saveCustomBudget(username, customBudget);
                    updateBudgetInfo();
                    parentUI.updateFinancialSuggestionDisplay(); // 更新 PersonalUI 的建议显示
                    JOptionPane.showMessageDialog(this, "Custom budget goal saved.");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid budget format. Please enter a number.");
                    customBudgetTextField.requestFocusInWindow();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please log in to save budget.");
        }
    }
}