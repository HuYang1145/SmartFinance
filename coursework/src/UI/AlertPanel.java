package UI;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import model.Transaction;

public class AlertPanel extends JDialog {
    public AlertPanel(List<Transaction> abnormalTransactions, String budgetWarning) {
        setTitle("MoneyGenie-安全警报");
        setSize(400, 250);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null); // 居中显示

        // ===== 主内容面板 =====
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // --- 异常交易部分 ---
        if (abnormalTransactions != null && !abnormalTransactions.isEmpty()) {
            JLabel title = new JLabel("Abnormal consumption detected, please verify:");
            title.setFont(new Font("Arial", Font.BOLD, 14));
            contentPanel.add(title);

            for (Transaction t : abnormalTransactions) {
                JLabel transLabel = new JLabel("  " + t.getType() + "    $" + t.getAmount());
                transLabel.setForeground(Color.RED);
                contentPanel.add(transLabel);
            }

            JButton continueButton = new JButton("Continue");
            continueButton.addActionListener(e -> dispose());
            contentPanel.add(continueButton);
        }

        // --- 预算提醒部分 ---
        if (budgetWarning != null) {
            JSeparator separator = new JSeparator();
            contentPanel.add(separator);

            JLabel budgetLabel = new JLabel("Budget Alert: " + budgetWarning);
            budgetLabel.setFont(new Font("Arial", Font.BOLD, 14));
            contentPanel.add(budgetLabel);

            JButton homeButton = new JButton("Back to Home");
            homeButton.addActionListener(e -> dispose());
            contentPanel.add(homeButton);
        }

        add(contentPanel, BorderLayout.CENTER);
    }
}