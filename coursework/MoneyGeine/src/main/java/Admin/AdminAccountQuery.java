package Admin;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class AdminAccountQuery extends JFrame {
    private JButton queryButton;
    private JTable accountTable;
    private NonEditableTableModel tableModel; // 使用自定义的 TableModel

    // 自定义 TableModel，禁止编辑
    private static class NonEditableTableModel extends DefaultTableModel {
        public NonEditableTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // 禁用所有单元格的编辑
        }
    }

    public AdminAccountQuery() {
        setTitle("Admin Account Management");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // 创建查询按钮
        queryButton = new JButton("Query Registered Users");

        // 创建表格模型
        String[] columnNames = {"Username", "Password", "Phone", "Email", "Gender", "Address", "CreationTime", "AccountStatus", "AccountType", "Balance"};
        tableModel = new NonEditableTableModel(columnNames, 0); // 使用自定义的 TableModel
        accountTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(accountTable);

        // 查询按钮监听事件
        queryButton.addActionListener((ActionEvent e) -> {
            showRegisteredUsers();
        });

        // 布局设置
        setLayout(new BorderLayout());
        add(queryButton, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private void showRegisteredUsers() {
        tableModel.setRowCount(0);

        try (BufferedReader reader = new BufferedReader(new FileReader("accounts.csv"))) {
            String line = reader.readLine(); // 读取并跳过表头
            if (line == null) {
                JOptionPane.showMessageDialog(this, "accounts.csv file is empty!", "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 10) {
                    tableModel.addRow(parts);
                } else {
                    System.out.println("Invalid data row: " + line + ". Expected 10 fields but found " + parts.length + ".");
                }
            }

            // 设置 "创建时间" 列的宽度
            TableColumnModel columnModel = accountTable.getColumnModel();
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                if (tableModel.getColumnName(i).equals("CreationTime")) {
                    TableColumn creationTimeColumn = columnModel.getColumn(i);
                    creationTimeColumn.setPreferredWidth(150); // 设置一个较大的宽度，你可以根据实际情况调整
                    break;
                }
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading accounts.csv file!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
}