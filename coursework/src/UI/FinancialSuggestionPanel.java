package UI;

import javax.swing.*;
import java.awt.*;

public class FinancialSuggestionPanel extends JPanel {
    private JDialog parentDialog;
    
    public FinancialSuggestionPanel(JDialog parent) {
        this.parentDialog = parent;
        
        setLayout(null);
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        initializeComponents();
    }
    
    private void initializeComponents() {
        // 标题
        JLabel titleLabel = new JLabel("Financial Management Suggestion");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 60, 120));
        titleLabel.setBounds(30, 30, 500, 40);
        add(titleLabel);
        
        // 按钮样式配置
        Color buttonTextColor = Color.WHITE;
        Color buttonBgColor = new Color(30, 60, 120);
        Font btnFont = new Font("Segoe UI", Font.BOLD, 14);
        Dimension btnSizePanel = new Dimension(250, 40);
        
        // 查看建议按钮
        JButton viewSuggestionButton = UIUtils.createStyledButton(
            "View Suggestion", buttonTextColor, buttonBgColor, btnSizePanel, btnFont
        );
        viewSuggestionButton.setBounds(50, 100, btnSizePanel.width, btnSizePanel.height);
        viewSuggestionButton.addActionListener(e -> showSuggestionDialog());
        add(viewSuggestionButton);
        
        // 更改预算按钮
        JButton changeBudgetButton = UIUtils.createStyledButton(
            "Manage Budget Goal", buttonTextColor, buttonBgColor, btnSizePanel, btnFont
        );
        changeBudgetButton.setBounds(50, 160, btnSizePanel.width, btnSizePanel.height);
        changeBudgetButton.addActionListener(e -> showBudgetDialog());
        add(changeBudgetButton);
    }
    
    private void showSuggestionDialog() {
        // 创建建议对话框
        DialogManager.showSuggestionDialog(parentDialog);
    }
    
    private void showBudgetDialog() {
        // 创建预算对话框
        DialogManager.showBudgetDialog(parentDialog);
    }
}