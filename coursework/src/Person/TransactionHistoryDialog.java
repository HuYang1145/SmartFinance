package Person;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import model.UserSession;

public class TransactionHistoryDialog extends JDialog {

    public TransactionHistoryDialog(JFrame parent) {
        super(parent, "交易记录", true);
        setSize(600, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        String loggedInUsername = UserSession.getCurrentUsername();
        if (loggedInUsername == null) {
            JOptionPane.showMessageDialog(this, "请先登录", "错误", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"操作", "金额", "目标账户", "时间"}, 0);
        JTable transactionTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(transactionTable);

        List<String[]> transactions = loadTransactions(loggedInUsername);
        for (String[] transaction : transactions) {
            tableModel.addRow(transaction);
        }

        add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> {
            setVisible(false);
            SwingUtilities.invokeLater(() -> {
                dispose();
            });
        });
        add(closeButton, BorderLayout.SOUTH);

        setVisible(true);
    }

    private List<String[]> loadTransactions(String username) {
        List<String[]> userTransactions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("transactions.csv"))) {
            String line;
            // 读取表头（第一行），可以跳过或者用于设置更详细的列名
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 5 && data[0].equals(username)) {
                    // 只展示 "进行的操作", "转入或转出的金额", "转入或转出的目标账户", "时间" 这几列
                    userTransactions.add(new String[]{data[1], data[2], data[3], data[4]});
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "读取交易记录文件失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return userTransactions;
    }
}