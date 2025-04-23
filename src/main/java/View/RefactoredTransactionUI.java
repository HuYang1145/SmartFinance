package View;

import javax.swing.*;
import java.awt.*;

public class RefactoredTransactionUI extends JPanel {
    private JSplitPane splitPane;

    public RefactoredTransactionUI(String username) {
        // 使用无 gaps 的 BorderLayout
        super(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setBackground(new Color(245, 245, 245));

        // 单例化 TransactionSystem
        TransactionSystem ts = new TransactionSystem(username);

        // ===== 左侧（2/3）: 交易记录 + 按钮 =====
        JPanel leftPanel = new JPanel(new BorderLayout(0, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(ts.createTransactionHistoryPanel(), BorderLayout.CENTER);
        leftPanel.add(ts.createTransactionButtonsPanel(),   BorderLayout.SOUTH);

        // ===== 右侧（1/3）: 汇率 / 转换 / 趋势 =====
        JPanel rightPanel = new JPanel(new GridLayout(3, 1, 0, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(ts.createExchangeRatePanel());
        rightPanel.add(ts.createCurrencyConversionPanel());
        rightPanel.add(ts.createHistoricalTrendPanel());

        // ===== 分割面板 =====
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                leftPanel,    // 左侧放交易记录
                rightPanel);  // 右侧放汇率模块
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(4);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        // 调整时按比例：左侧 2/3，右侧 1/3
        splitPane.setResizeWeight(0.67);

        add(splitPane, BorderLayout.CENTER);

        // 面板可见后再定位分隔条，百分比用 0.67
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.67));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Smart Finance Refactored UI");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 700);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new RefactoredTransactionUI("test_user"));
            frame.setVisible(true);
        });
    }
}
