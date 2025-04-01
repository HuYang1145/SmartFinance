package UI;

import model.AccountModel;
import model.UserRegistrationCSVExporter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import Admin.AdminAccountQuery;
import Admin.AdminSelfInfo;
import Admin.ModifyCustomerInfoDialog;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AdminUI extends JDialog {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel userListPanel;
    private JTable table;
    private DefaultTableModel model;
    private JButton deleteButton;

    public AdminUI() {
        setTitle("管理员账户中心");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 创建主菜单面板
        JPanel menuPanel = createMenuPanel();

        // 创建用户列表面板
        userListPanel = new JPanel(new BorderLayout());

        mainPanel.add(menuPanel, "menu");
        mainPanel.add(userListPanel, "userList");

        add(mainPanel);
        cardLayout.show(mainPanel, "menu");

        setVisible(true);
    }

    private JPanel createMenuPanel() {
        JPanel menuPanel = new JPanel(new GridLayout(6, 1, 10, 10));

        JButton viewAdminInfoButton = new JButton("查看管理员信息");
        JButton updateCustomerInfoButton = new JButton("修改客户信息");
        JButton customerInfoQueryButton = new JButton("客户信息查询");
        JButton deleteCustomerButton = new JButton("删除客户信息");
        JButton importTransactionsButton = new JButton("导入CSV交易记录");
        JButton logoutButton = new JButton("退出登录");

        viewAdminInfoButton.addActionListener(e -> new AdminSelfInfo());
        updateCustomerInfoButton.addActionListener(e -> displayAdminVerificationForm());
        customerInfoQueryButton.addActionListener(e -> new AdminAccountQuery());
        deleteCustomerButton.addActionListener(e -> showUserList());
        importTransactionsButton.addActionListener(e -> importTransactions());
        logoutButton.addActionListener(e -> dispose());

        menuPanel.add(viewAdminInfoButton);
        menuPanel.add(updateCustomerInfoButton);
        menuPanel.add(customerInfoQueryButton);
        menuPanel.add(deleteCustomerButton);
        menuPanel.add(importTransactionsButton);
        menuPanel.add(logoutButton);

        return menuPanel;
    }

    private void showUserList() {
        userListPanel.removeAll();

        model = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        table = new JTable(model);
        loadUserData();

        deleteButton = new JButton("删除选中用户");
        deleteButton.addActionListener(e -> deleteSelectedUsers());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(deleteButton);
        JButton backButton = new JButton("返回");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "menu"));
        buttonPanel.add(backButton);

        userListPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        userListPanel.add(buttonPanel, BorderLayout.SOUTH);

        cardLayout.show(mainPanel, "userList");
    }

    private void loadUserData() {
        model.setRowCount(0);
        model.setColumnCount(0);

        try (BufferedReader br = new BufferedReader(new FileReader("accounts.csv"))) {
            String line;
            boolean firstLine = true;
            List<String[]> userData = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                String[] user = line.split(",");

                if (firstLine) {
                    model.addColumn("选择");
                    for (String columnName : user) {
                        model.addColumn(columnName);
                    }
                    firstLine = false;
                } else {
                    userData.add(user);
                }
            }

            for (String[] user : userData) {
                Object[] rowData = new Object[user.length + 1];
                rowData[0] = false;
                for (int i = 0; i < user.length; i++) {
                    rowData[i + 1] = user[i];
                }
                model.addRow(rowData);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载用户数据失败：" + e.getMessage());
        }
    }

    private void deleteSelectedUsers() {
        List<String[]> usersList = new ArrayList<>();
        List<String[]> updatedUsersList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("accounts.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                usersList.add(line.split(","));
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "读取用户数据失败：" + e.getMessage());
            return;
        }

        for (int i = 0; i < model.getRowCount(); i++) {
            Boolean selected = (Boolean) model.getValueAt(i, 0);
            String username = (String) model.getValueAt(i, 1);

            if (!selected) {
                for (String[] user : usersList) {
                    if (user[0].equals(username)) {
                        updatedUsersList.add(user);
                        break;
                    }
                }
            }
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("accounts.csv"))) {
            if (!usersList.isEmpty()) {
                String[] headers = usersList.get(0);
                bw.write(String.join(",", headers));
                bw.newLine();
            }

            for (String[] user : updatedUsersList) {
                bw.write(String.join(",", user));
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "操作失败：" + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            loadUserData();
            JOptionPane.showMessageDialog(this, "选中用户已成功删除");
        });
    }

    private void importTransactions() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            importTransactionsFromCSV(selectedFile);
        }
    }

    private void importTransactionsFromCSV(File file) {
        List<String> newTransactions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 5) {
                    newTransactions.add(line);
                } else {
                    JOptionPane.showMessageDialog(this, "导入文件格式不正确，跳过行: " + line, "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "读取导入文件失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }

        if (!newTransactions.isEmpty()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter("transactions.csv", true))) {
                for (String transaction : newTransactions) {
                    bw.newLine();
                    bw.write(transaction);
                }
                JOptionPane.showMessageDialog(this, "成功导入 " + newTransactions.size() + " 条交易记录。", "导入成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "写入交易记录文件失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "导入文件中没有找到有效的交易记录。", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void displayAdminVerificationForm() {
        JDialog loginDialog = new JDialog(this, "管理员登录", true);
        loginDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        loginDialog.setSize(350, 150);
        loginDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
         panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panel.add(new JLabel("管理员用户名:"));
        JTextField tfAdminUsername = new JTextField();
        panel.add(tfAdminUsername);
        
        panel.add(new JLabel("管理员密码:"));
        JPasswordField pfAdminPassword = new JPasswordField();
        panel.add(pfAdminPassword);
        
        JButton btnLogin = new JButton("登录");
        panel.add(new JLabel());
        panel.add(btnLogin);
        
        loginDialog.add(panel, BorderLayout.CENTER);
        
        AdminUI self = this; // 存储 AdminUI 实例的引用
        
        btnLogin.addActionListener(e -> {
            String adminUsername = tfAdminUsername.getText().trim();
            String adminPassword = new String(pfAdminPassword.getPassword()).trim();
            AccountModel adminAccount = getAccount(adminUsername, adminPassword);
        
        if (adminAccount != null) {
            loginDialog.dispose();
            self.toFront(); // 将 AdminUI 带到前面
            self.requestFocusInWindow(); // 请求焦点 (可选但可能很有用)
            String customerUsernameToModify = JOptionPane.showInputDialog(self, "请输入要修改信息的客户用户名:");
            if (customerUsernameToModify != null && !customerUsernameToModify.trim().isEmpty()) {
                AccountModel customerAccount = getAccount(customerUsernameToModify.trim(), "");
                if (customerAccount != null) {
                    ModifyCustomerInfoDialog modifyDialog = new ModifyCustomerInfoDialog();
                    modifyDialog.setAccountInfo(customerAccount);
                    modifyDialog.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(self, "找不到该客户用户名的账户信息！");
                }
            }
            } else {
                JOptionPane.showMessageDialog(loginDialog, "管理员用户名或密码错误！");
            }
        });
    loginDialog.setVisible(true);
}

    public static AccountModel getAccount(String username, String password) {
        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username)) {
                return account;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminUI::new);
    }
}