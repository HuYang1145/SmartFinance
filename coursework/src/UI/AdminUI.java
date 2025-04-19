package UI;

import Admin.AdminAccountQuery;
import Admin.AdminSelfInfo;
import Admin.ModifyCustomerInfoDialog;
import Model.AccountModel;
import Model.TransactionCSVImporter;
import Model.UserRegistrationCSVExporter;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AdminUI extends JDialog {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private final JPanel userListPanel;
    private JTable table;
    private DefaultTableModel model;

    // 期望的账户文件头部
    private static final String EXPECTED_ACCOUNT_HEADER = "username,password,balance,isAdmin";
    private static final int EXPECTED_ACCOUNT_FIELD_COUNT = 4;


    public AdminUI() {
        setTitle("Administrator Account Center");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(new Color(30, 60, 120));

        JPanel dashboardPanel = createDashboardPanel();
        userListPanel = new JPanel(new BorderLayout());
        userListPanel.setBackground(new Color(30, 60, 120));

        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(userListPanel, "userList");

        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setPreferredSize(new Dimension(260, 0));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(new Color(30, 60, 120));

        Dimension btnSize = new Dimension(240, 36);
        sidebarPanel.add(Box.createVerticalStrut(120)); // 顶部留空

        // 添加侧边栏按钮
        sidebarPanel.add(createSidebarButton("Administrator's Home Page", btnSize, () -> cardLayout.show(contentPanel, "dashboard")));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Administrator Information", btnSize, AdminSelfInfo::new)); // 假设 AdminSelfInfo 构造函数会显示对话框或窗口
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Modify Customer Information", btnSize, this::displayAdminVerificationForm));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Customer Information Inquiry", btnSize, AdminAccountQuery::new)); // 假设 AdminAccountQuery 构造函数会显示对话框或窗口
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Delete Customer Information", btnSize, this::showUserList));
        sidebarPanel.add(Box.createVerticalStrut(10));
        // 新增的导入客户账户按钮
        sidebarPanel.add(createSidebarButton("Import Customer Accounts", btnSize, this::importCustomerAccounts));
        sidebarPanel.add(Box.createVerticalStrut(10));
        // 导入交易记录按钮（保持原名，但功能调用新类）
        sidebarPanel.add(createSidebarButton("Import Transaction Records", btnSize, this::importTransactionRecords));


        sidebarPanel.add(Box.createVerticalGlue());
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Log out", btnSize, this::dispose));
        sidebarPanel.add(Box.createVerticalStrut(20));

        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(30, 60, 120));

        JLabel title = new JLabel("Administrator Account Center");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBounds(60, 30, 400, 40);
        panel.add(title);

        JLabel info = new JLabel("Welcome to the back office management system. Please select the function on the left side.");
        info.setForeground(Color.WHITE);
        info.setBounds(30, 80, 700, 30);
        panel.add(info);

        return panel;
    }

    private JButton createSidebarButton(String text, Dimension size, Runnable action) {
        JButton button = new JButton(text);
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);  // 左对齐
        button.setIconTextGap(10);
        button.setBackground(new Color(20, 40, 80)); // 深蓝背景
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.addActionListener(e -> action.run());
        return button;
    }

    // 显示用户列表的方法 (用于删除用户功能)
    private void showUserList() {
        userListPanel.removeAll();

        model = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // 只有第一列（复选框）可编辑
            }
        };

        table = new JTable(model);
        table.setFillsViewportHeight(true); // 填充整个视口高度
        table.setBackground(new Color(230, 230, 250)); // 淡紫色背景
        table.getTableHeader().setBackground(new Color(100, 149, 237)); // 康乃馨蓝表头
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setRowHeight(25); // 设置行高
        table.setSelectionBackground(new Color(173, 216, 230)); // 浅蓝选择背景

        loadUserData(); // 加载数据到表格

        JButton deleteButton = new JButton("Delete Selected Users");
        deleteButton.addActionListener(e -> deleteSelectedUsers());

        JButton backButton = new JButton("Return");
        backButton.addActionListener(e -> cardLayout.show(contentPanel, "dashboard"));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(30, 60, 120)); // 与侧边栏背景一致
        buttonPanel.add(deleteButton);
        buttonPanel.add(backButton);

        // 添加标题或描述标签
        JLabel listTitle = new JLabel("Select users to delete:");
        listTitle.setForeground(Color.WHITE);
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        listTitle.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 添加边距

        userListPanel.add(listTitle, BorderLayout.NORTH); // 标题放顶部
        userListPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        userListPanel.add(buttonPanel, BorderLayout.SOUTH);

        userListPanel.revalidate(); // 重新验证布局
        userListPanel.repaint(); // 重新绘制
        cardLayout.show(contentPanel, "userList"); // 切换到用户列表面板
    }

    // 从 accounts.csv 加载用户数据到表格
    private void loadUserData() {
        model.setRowCount(0); // 清空现有数据
        model.setColumnCount(0); // 清空现有列
        try (BufferedReader br = new BufferedReader(new FileReader("accounts.csv"))) {
            String line;
            boolean firstLine = true;
            List<String[]> userData = new ArrayList<>(); // 用于存储除头部外的所有行

            while ((line = br.readLine()) != null) {
                String[] user = line.split(",");
                if (firstLine) {
                    // 添加第一列用于复选框
                    model.addColumn("Select");
                    // 添加 CSV 文件中的列名
                    for (String columnName : user) {
                         model.addColumn(columnName);
                    }
                    firstLine = false;
                } else {
                    // 存储数据行
                    userData.add(user);
                }
            }

            // 将数据行添加到表格模型
            for (String[] user : userData) {
                Object[] rowData = new Object[user.length + 1];
                rowData[0] = false; // 默认不选中
                System.arraycopy(user, 0, rowData, 1, user.length); // 将 CSV 数据放入对应的列
                model.addRow(rowData);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load user data: " + e.getMessage(), "Error Loading Data", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 删除选中的用户
    private void deleteSelectedUsers() {
        List<String[]> allUsers = new ArrayList<>();
        // 读取 accounts.csv 中的所有行（包括头部）
        try (BufferedReader br = new BufferedReader(new FileReader("accounts.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                allUsers.add(line.split(",", -1)); // 使用-1保留空字段
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to read data for deletion:" + e.getMessage(), "File Read Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<String[]> remainingUsers = new ArrayList<>();
        // 保留头部行
        if (!allUsers.isEmpty()) {
            remainingUsers.add(allUsers.get(0)); // 添加头部
        }

        Set<String> usernamesToDelete = new HashSet<>();
        // 收集表格中被选中的用户名
        for (int i = 0; i < model.getRowCount(); i++) {
            boolean selected = (Boolean) model.getValueAt(i, 0);
            if (selected) {
                // 假设用户名的列是第二列 (索引为 1)
                String username = (String) model.getValueAt(i, 1);
                usernamesToDelete.add(username);
            }
        }

        // 构建剩余用户列表 (排除被选中的)
        if (allUsers.size() > 1) { // 确保文件不只包含头部
            for (int i = 1; i < allUsers.size(); i++) { // 从第二行开始（跳过头部）
                String[] user = allUsers.get(i);
                if (user.length > 0 && !usernamesToDelete.contains(user[0])) { // 假设用户名字段是第一个 (索引为 0)
                    remainingUsers.add(user);
                }
            }
        }

        // 将剩余用户写回 accounts.csv
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("accounts.csv"))) {
            for (String[] user : remainingUsers) {
                bw.write(String.join(",", user));
                bw.newLine();
            }
            JOptionPane.showMessageDialog(this, "Selected users have been successfully deleted");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Write failed:" + e.getMessage(), "File Write Error", JOptionPane.ERROR_MESSAGE);
            return; // 写入失败则停止
        }

        // 删除成功后，重新加载数据以更新表格显示
        SwingUtilities.invokeLater(this::loadUserData);
    }

    // ============================================================
    // 新增：导入客户账户功能
    // ============================================================
    private void importCustomerAccounts() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择要导入的客户账户文件");
        // 可以添加文件过滤器，只显示 CSV 文件
        // fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null) {
                importAccountsFromCSV(selectedFile);
            }
        }
    }

    private void importAccountsFromCSV(File file) {
        List<String> validDataLines = new ArrayList<>();
        boolean headerProcessed = false;
        int importedCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                 if (line.trim().isEmpty()) {
                    continue; // 跳过空行
                }

                if (!headerProcessed) {
                    // 检查头部
                    if (!line.trim().equalsIgnoreCase(EXPECTED_ACCOUNT_HEADER)) {
                         JOptionPane.showMessageDialog(this, "导入失败: 账户文件头部格式不正确。\n期望: '" + EXPECTED_ACCOUNT_HEADER + "'\n实际: '" + line + "'", "导入错误", JOptionPane.ERROR_MESSAGE);
                         return; // 头部不正确，停止导入
                    }
                    headerProcessed = true;
                    continue; // 跳过头部行
                }

                // 处理数据行
                String[] fields = line.split(",", -1); // 使用-1保留空字段

                // 验证字段数量
                if (fields.length != EXPECTED_ACCOUNT_FIELD_COUNT) {
                    System.err.println("跳过无效账户行 (字段数量不正确, 第 " + lineNumber + " 行): " + line);
                    // 可以选择向用户报告哪些行被跳过
                    continue; // 跳过格式错误的行
                }

                // TODO: 可以根据需要添加更多的数据验证，例如：
                // - 检查用户名是否为空
                // - 检查密码是否为空
                // - 尝试解析余额字段为数字
                // - 检查 isAdmin 字段是否是预期的值 (例如 "true" 或 "false")

                // 简单的验证：用户名、密码、余额、isAdmin 字段都不为空
                boolean isValidLine = true;
                 if (fields[0].trim().isEmpty() || fields[1].trim().isEmpty() || fields[2].trim().isEmpty() || fields[3].trim().isEmpty()) {
                    isValidLine = false;
                     System.err.println("跳过无效账户行 (存在空字段, 第 " + lineNumber + " 行): " + line);
                 } else {
                    // 尝试验证余额是否是有效的数字
                    try {
                        Double.valueOf(fields[2].trim());
                    } catch (NumberFormatException e) {
                        isValidLine = false;
                         System.err.println("跳过无效账户行 (余额格式不正确, 第 " + lineNumber + " 行): " + line);
                    }
                     // 验证 isAdmin 字段
                     String isAdminStr = fields[3].trim().toLowerCase();
                     if (!isAdminStr.equals("true") && !isAdminStr.equals("false")) {
                         isValidLine = false;
                          System.err.println("跳过无效账户行 (isAdmin 格式不正确, 期望 true 或 false, 第 " + lineNumber + " 行): " + line);
                     }
                 }


                if (isValidLine) {
                    validDataLines.add(line);
                }
            }
             // 检查文件是否只有头部或为空
            if (!headerProcessed && lineNumber > 0) {
                 JOptionPane.showMessageDialog(this, "导入失败: 账户文件为空或不包含有效的头部。", "导入错误", JOptionPane.ERROR_MESSAGE);
                 return;
            } else if (lineNumber == 0) {
                JOptionPane.showMessageDialog(this, "源账户文件为空，没有数据可导入。", "导入提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }


        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "读取账户文件失败: " + e.getMessage(), "导入错误", JOptionPane.ERROR_MESSAGE);
            return; // 读取失败，停止导入
        }

        // 将验证通过的数据行追加到 accounts.csv
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("accounts.csv", true))) {
             // 在写入数据之前，检查 accounts.csv 文件是否为空。
             // 如果文件不为空，并且最后一行不是换行符，我们应该先写入一个换行符
             // 以免新数据与旧数据连接在一起。
             File accountFile = new File("accounts.csv");
             if (accountFile.exists() && accountFile.length() > 0) {
                 try (RandomAccessFile raf = new RandomAccessFile(accountFile, "r")) {
                      if (raf.length() > 0) {
                          raf.seek(raf.length() - 1);
                          if (raf.readByte() != '\n') {
                              bw.newLine(); // 如果最后不是换行符，则添加一个
                          }
                      }
                 } catch (IOException e) {
                      System.err.println("检查accounts.csv文件末尾换行符失败，继续写入。");
                      // 忽略此错误，继续尝试写入，可能导致格式问题
                 }
             } else {
                 // 文件不存在或为空，首次写入，确保有头部 (如果需要的话)
                 // 此处我们假设 accounts.csv 应该预先有头部，导入只追加数据。
                 // 如果需要导入时添加头部，可以在这里判断并写入
                 // if (!accountFile.exists() || accountFile.length() == 0) {
                 //     bw.write(EXPECTED_ACCOUNT_HEADER);
                 //     bw.newLine();
                 // }
             }


            for (String dataLine : validDataLines) {
                bw.write(dataLine);
                bw.newLine(); // 每行数据后添加换行符
                importedCount++;
            }
            JOptionPane.showMessageDialog(this, "成功导入 " + importedCount + " 条客户账户记录。", "导入成功", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "写入 accounts.csv 文件失败: " + e.getMessage(), "导入错误", JOptionPane.ERROR_MESSAGE);
        }

         // 导入成功后，如果当前显示的是用户列表，则需要刷新
         Component currentVisiblePanel = null;
         for (Component comp : contentPanel.getComponents()) {
         if (comp.isVisible()) {
         currentVisiblePanel = comp;
         break;
         }
         }
        if (currentVisiblePanel == userListPanel) {
        loadUserData();
    } // 刷新用户列表
        }


    // ============================================================
    // 导入交易记录功能 (使用 TransactionCSVImporter 类)
    // ============================================================
    // 将原有的 importTransactions() 方法重命名以提高清晰度
    private void importTransactionRecords() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择要导入的交易记录文件"); // 设置文件选择器标题
        // 可以添加文件过滤器，只显示 CSV 文件
        // fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null) {
                // 调用处理交易记录导入的方法
                processTransactionRecordsImport(selectedFile);
            }
        }
    }

    // 将原有的 importTransactionsFromCSV() 方法重命名并修改为使用 TransactionCSVImporter
    private void processTransactionRecordsImport(File file) {
         if (file == null) {
            // 用户取消了文件选择
            return;
        }

        // 目标文件路径硬编码为根目录下的 transactions.csv
        String destinationFilePath = "transactions.csv";

        try {
            // 调用 TransactionCSVImporter 类进行导入和验证
            int importedCount = TransactionCSVImporter.importTransactions(file, destinationFilePath);

            // 导入成功，显示导入条数
            JOptionPane.showMessageDialog(this, "成功导入 " + importedCount + " 条交易记录。", "导入成功", JOptionPane.INFORMATION_MESSAGE);

        } catch (IllegalArgumentException e) {
            // 捕获导入器抛出的格式错误异常
            JOptionPane.showMessageDialog(this, "导入失败: 文件格式错误 - " + e.getMessage(), "导入错误", JOptionPane.ERROR_MESSAGE);
             // 打印堆栈信息，帮助调试
        } catch (IOException e) {
            // 捕获导入器抛出的文件读写错误异常
            JOptionPane.showMessageDialog(this, "导入失败: 文件读写错误 - " + e.getMessage(), "导入错误", JOptionPane.ERROR_MESSAGE);
             // 打印堆栈信息，帮助调试
        } catch (HeadlessException e) {
             // 捕获其他可能的意外异常
             JOptionPane.showMessageDialog(this, "导入过程中发生未知错误: " + e.getMessage(), "导入错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ============================================================
    // 管理员验证和修改客户信息功能
    // ============================================================
    public void displayAdminVerificationForm() {
        JDialog dialog = new JDialog(this, "Administrator Validation", true);
        dialog.setSize(350, 150);
        dialog.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Administrator user name:"));
        JTextField tfUser = new JTextField();
        panel.add(tfUser);
        panel.add(new JLabel("Administrator password:")); // 修正拼写错误
        JPasswordField pfPass = new JPasswordField();
        panel.add(pfPass);
        panel.add(new JLabel());
        JButton btn = new JButton("validate");
        panel.add(btn);

        dialog.add(panel);
        btn.addActionListener(e -> {
            String username = tfUser.getText();
            String password = new String(pfPass.getPassword());
            // 假设 admin 的用户名和密码保存在 accounts.csv 中，且 isAdmin 字段为 true
            AccountModel adminAccount = getAccount(username, password); // 检查用户名和密码是否匹配
            // 还需要验证这个账户确实是管理员账户
            if (adminAccount != null && adminAccount.isAdmin()) {
                dialog.dispose();
                String customer = JOptionPane.showInputDialog(this, "Please enter the customer's username to be changed:");
                if (customer != null && !customer.trim().isEmpty()) {
                    AccountModel target = findAccountByUsername(customer.trim()); // 查找要修改的客户账户
                    if (target != null) {
                        // 找到了客户，打开修改信息对话框
                        ModifyCustomerInfoDialog modifyDialog = new ModifyCustomerInfoDialog();
                        modifyDialog.setAccountInfo(target);
                        modifyDialog.setVisible(true);
                        // 修改对话框关闭后，如果当前是用户列表页面，可能需要刷新
                         // 检查当前显示的面板是否是用户列表面板
Component currentVisiblePanel = null;
for (Component comp : contentPanel.getComponents()) {
if (comp.isVisible()) {
currentVisiblePanel = comp;
break;
}
}
if (currentVisiblePanel == userListPanel) {
loadUserData(); // 刷新用户列表
}

                    } else {
                        JOptionPane.showMessageDialog(this, "The user could not be found!");
                    }
                } else if (customer != null) { // 用户点了确定但输入为空
                    JOptionPane.showMessageDialog(this, "Username cannot be empty.");
                }
                // 如果用户点了取消 (customer == null)，则不执行任何操作
            } else {
                JOptionPane.showMessageDialog(dialog, "Invalid administrator username or password!");
            }
        });
        dialog.setVisible(true);
    }

    // 查找账户（用于验证管理员身份） - 包含密码匹配
    public static AccountModel getAccount(String username, String password) {
         for (AccountModel a : UserRegistrationCSVExporter.readFromCSV()) {
             if (a.getUsername().equals(username) && a.getPassword().equals(password)) {
                 return a;
             }
         }
         return null; // 用户名或密码不匹配，或者用户不存在
    }

    // 按用户名查找账户（用于查找要修改的客户） - 不检查密码
     public static AccountModel findAccountByUsername(String username) {
        for (AccountModel a : UserRegistrationCSVExporter.readFromCSV()) {
            if (a.getUsername().equals(username)) {
                return a;
            }
        }
        return null; // 用户不存在
     }


    public static void main(String[] args) {
        // 确保 accounts.csv 和 transactions.csv 文件存在，如果不存在则创建（带或不带头部，取决于您的设计）
        // 简单的检查和创建空文件
        ensureFileExists("accounts.csv", EXPECTED_ACCOUNT_HEADER);
        ensureFileExists("transactions.csv", TransactionCSVImporter.EXPECTED_HEADER); // 使用 Importer 中定义的头部

        SwingUtilities.invokeLater(AdminUI::new);
    }

    // 辅助方法：确保文件存在，如果不存在则创建并写入头部
    private static void ensureFileExists(String filename, String header) {
        File file = new File(filename);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    System.out.println("Created new file: " + filename);
                    // 写入头部
                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                        bw.write(header);
                        bw.newLine();
                    }
                }
            } catch (IOException e) {
                System.err.println("Error creating file " + filename + ": " + e.getMessage());
            }
        }
    }
}