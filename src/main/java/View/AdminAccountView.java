package View;

import java.awt.BorderLayout;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import AccountModel.AccountModel;

public class AdminAccountView extends JFrame {
    private JButton queryButton;
    private JTable accountTable;
    private NonEditableTableModel tableModel;

    // 自定义 TableModel，禁止编辑
    private static class NonEditableTableModel extends DefaultTableModel {
        public NonEditableTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    public AdminAccountView(Consumer<AdminAccountView> onQuery) {
        setTitle("Admin Account Management");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // 创建查询按钮
        queryButton = new JButton("Query Registered Users");

        // 创建表格模型
        String[] columnNames = {"Username", "Phone", "Email", "Gender", "Address", "CreationTime", "AccountStatus", "AccountType", "Balance"};
        tableModel = new NonEditableTableModel(columnNames, 0);
        accountTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(accountTable);

        // 查询按钮监听事件
        queryButton.addActionListener(e -> onQuery.accept(this));

        // 布局设置
        setLayout(new BorderLayout());
        add(queryButton, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // 设置 CreationTime 列宽度
        TableColumnModel columnModel = accountTable.getColumnModel();
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            if ("CreationTime".equals(tableModel.getColumnName(i))) {
                columnModel.getColumn(i).setPreferredWidth(150);
                break;
            }
        }

        setVisible(true);
    }

    public void updateTable(List<AccountModel> accounts) {
        tableModel.setRowCount(0);
        if (accounts == null || accounts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No accounts found!", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        for (AccountModel account : accounts) {
            tableModel.addRow(new Object[]{
                account.getUsername(),
                account.getPhone(),
                account.getEmail(),
                account.getGender(),
                account.getAddress(),
                account.getCreationTime(),
                account.getAccountStatus().name(),
                account.getAccountType(),
                String.format("%.2f", account.getBalance())
            });
        }
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}