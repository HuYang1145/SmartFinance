package UI;

import javax.swing.*;
import java.awt.*;

public class DialogManager {
    
    // 显示建议对话框
    public static void showSuggestionDialog(JDialog parent) {
        JDialog suggestionDialog = new JDialog(parent, "Suggestion", true);
        suggestionDialog.setSize(450, 250);
        suggestionDialog.setLayout(new BorderLayout(10, 10));
        suggestionDialog.setLocationRelativeTo(parent);
        
        JLabel titleLabel = new JLabel("Suggestion", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 60, 120));
        
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
        
        JButton continueButton = new JButton("Continue");
        continueButton.setForeground(new Color(30, 60, 120));
        continueButton.setBackground(Color.WHITE);
        continueButton.setBorder(BorderFactory.createLineBorder(new Color(30, 60, 120), 2));
        continueButton.setFocusPainted(false);
        continueButton.addActionListener(e -> {
            suggestionDialog.dispose();
            showDetailedAdviceDialog(parent); // 打开第二个对话框
        });
        
        suggestionDialog.add(titleLabel, BorderLayout.NORTH);
        suggestionDialog.add(contentPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(continueButton);
        suggestionDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        suggestionDialog.setVisible(true);
    }
    
    // 显示详细建议对话框
    private static void showDetailedAdviceDialog(JDialog parent) {
        JDialog adviceDialog = new JDialog(parent, "Detailed Advice", true);
        adviceDialog.setSize(500, 300);
        adviceDialog.setLayout(new BorderLayout(10, 10));
        adviceDialog.setLocationRelativeTo(parent);
        
        JLabel titleLabel = new JLabel("Detailed Spending Advice", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
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
        
        JButton backButton = new JButton("Close");
        backButton.setForeground(new Color(30, 60, 120));
        backButton.setBackground(Color.WHITE);
        backButton.setBorder(BorderFactory.createLineBorder(new Color(30, 60, 120), 2));
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> adviceDialog.dispose());
        
        adviceDialog.add(titleLabel, BorderLayout.NORTH);
        adviceDialog.add(contentPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(backButton);
        adviceDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        adviceDialog.setVisible(true);
    }
    
    // 显示预算管理对话框
    public static void showBudgetDialog(JDialog parent) {
        JDialog budgetDialog = new JDialog(parent, "Budget Management", true);
        budgetDialog.setSize(500, 300);
        budgetDialog.setLayout(new GridLayout(5, 1, 10, 10));
        budgetDialog.setLocationRelativeTo(parent);
        ((JPanel) budgetDialog.getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel inputLabel = new JLabel("Change your budget goal ($):");
        JTextField budgetInput = new JTextField(15);
        inputPanel.add(inputLabel);
        inputPanel.add(budgetInput);
        
        JLabel currentBudgetLabel = new JLabel("Now your actual budget goal: $11,000 dollars");
        JLabel modeLabel = new JLabel("Now in: economical mode");
        JLabel reasonLabel = new JLabel("Because: The shopping festival is next month");
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        
        JButton restoreButton = new JButton("Restore Intelligent Recommendation");
        restoreButton.addActionListener(e -> {
            budgetInput.setText("");
            currentBudgetLabel.setText("Now your actual budget goal: $11,000 dollars");
            modeLabel.setText("Now in: economical mode");
            reasonLabel.setText("Because: The shopping festival is next month");
            JOptionPane.showMessageDialog(budgetDialog, "Budget restored to recommendation.", "Info", JOptionPane.INFORMATION_MESSAGE);
        });
        
        JButton backButton = new JButton("Close");
        backButton.addActionListener(e -> budgetDialog.dispose());
        
        Color textBlue = new Color(30, 60, 120);
        Color buttonBg = Color.WHITE;
        Font infoFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 12);
        
        inputLabel.setFont(infoFont);
        currentBudgetLabel.setFont(infoFont);
        modeLabel.setFont(infoFont);
        reasonLabel.setFont(infoFont);
        
        JButton[] dialogButtons = {restoreButton, backButton};
        for (JButton btn : dialogButtons) {
            btn.setForeground(textBlue);
            btn.setBackground(buttonBg);
            btn.setFont(buttonFont);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createLineBorder(textBlue, 1));
        }
        
        buttonPanel.add(restoreButton);
        buttonPanel.add(backButton);
        
        budgetDialog.add(inputPanel);
        budgetDialog.add(currentBudgetLabel);
        budgetDialog.add(modeLabel);
        budgetDialog.add(reasonLabel);
        budgetDialog.add(buttonPanel);
        
        budgetDialog.setVisible(true);
    }
}
