package View;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import AccountModel.AccountModel;
import AccountModel.TransactionCSVImporterModel;
import AccountModel.UserRegistrationCSVExporterModel;
import AdminController.AdminController;
import AdminController.AdminSelfInfo;
import AdminController.PersonModifyService;
import AdminModel.AccountRepositoryModel;

public class AdminPlane extends JDialog {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private final JPanel userListPanel; // 声明为 final 如果它在构造函数中初始化后不再改变
    private JTable table;
    private DefaultTableModel model;
    private JPanel sidebarPanel; // 添加 sidebarPanel 实例变量

    // --- 修改常量以匹配 10 列格式 ---
    // 期望的账户文件头部 (更新为 10 列)
    private static final String EXPECTED_ACCOUNT_HEADER = "Username,Password,Phone,Email,Gender,Address,CreationTime,AccountStatus,AccountType,Balance";
    // 期望的字段数量 (更新为 10 个)
    private static final int EXPECTED_ACCOUNT_FIELD_COUNT = 10;


    public AdminPlane() {
        setTitle("Administrator Account Center");
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(new Color(30, 60, 120)); // 背景色

        JPanel dashboardPanel = createDashboardPanel();
        userListPanel = new JPanel(new BorderLayout()); // 初始化 userListPanel
        userListPanel.setBackground(new Color(245, 245, 245)); // 设置内容面板的背景色

        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(userListPanel, "userList");

        sidebarPanel = new JPanel(); // 初始化 sidebarPanel
        sidebarPanel.setPreferredSize(new Dimension(260, 0));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(new Color(30, 60, 120)); // 侧边栏背景色

        Dimension btnSize = new Dimension(240, 36);
        sidebarPanel.add(Box.createVerticalStrut(120)); // 顶部留空

        // 添加侧边栏按钮
        sidebarPanel.add(createSidebarButton("Administrator's Home Page", btnSize, () -> cardLayout.show(contentPanel, "dashboard")));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Administrator Information", btnSize, () -> new AdminSelfInfo().setVisible(true))); // 确保显示
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Modify Customer Information", btnSize, this::displayAdminVerificationForm));
        sidebarPanel.add(Box.createVerticalStrut(10));
       sidebarPanel.add(createSidebarButton("Customer Information Inquiry", btnSize, () -> {
    AdminController controller = new AdminController(new AccountRepositoryModel());
    controller.initialize();
}));
sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Delete Customer Information", btnSize, this::showUserList));
        sidebarPanel.add(Box.createVerticalStrut(10));
        // 导入客户账户按钮
        sidebarPanel.add(createSidebarButton("Import Customer Accounts", btnSize, this::importCustomerAccounts));
        sidebarPanel.add(Box.createVerticalStrut(10));
        // 导入交易记录按钮
        sidebarPanel.add(createSidebarButton("Import Transaction Records", btnSize, this::importTransactionRecords));


        sidebarPanel.add(Box.createVerticalGlue()); // 把按钮推到顶部和底部之间
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Log out", btnSize, this::dispose)); // 关闭当前窗口
        sidebarPanel.add(Box.createVerticalStrut(20)); // 底部留空

        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // 默认显示 dashboard
        cardLayout.show(contentPanel, "dashboard");

        setVisible(true);
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(null); // 使用 null 布局以便精确控制位置
        panel.setBackground(new Color(245, 245, 245)); // 设置内容面板背景色

        JLabel title = new JLabel("Administrator Account Center");
        title.setForeground(new Color(30, 60, 120)); // 深蓝色标题
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setBounds(40, 40, 500, 40); // 设置位置和大小
        panel.add(title);

        JLabel info = new JLabel("Welcome to the back office management system. Please select the function on the left side.");
        info.setForeground(Color.DARK_GRAY); // 深灰色文字
        info.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        info.setBounds(40, 100, 600, 30); // 设置位置和大小
        panel.add(info);

        // 可以添加其他仪表盘元素，如图标、摘要信息等

        return panel;
    }

    private JButton createSidebarButton(String text, Dimension size, Runnable action) {
        JButton button = new JButton(text);
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);  // 左对齐
        button.setIconTextGap(10); // 图标和文本间距 (如果加图标)
        button.setBackground(new Color(50, 80, 140)); // 侧边栏按钮背景色
        button.setForeground(Color.WHITE); // 文字颜色
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15)); // 内边距
        button.setContentAreaFilled(true); // 确保背景色生效
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // 手形光标
        button.setAlignmentX(Component.CENTER_ALIGNMENT); // 居中对齐（在 BoxLayout 中）

        // 悬停效果
        Color originalColor = button.getBackground();
        Color hoverColor = new Color(70, 100, 160); // 悬停时的亮一点的蓝色

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor);
            }
        });

        button.addActionListener(e -> action.run());
        return button;
    }

    // 显示用户列表的方法 (用于删除用户功能)
    private void showUserList() {
        userListPanel.removeAll(); // 清空之前的组件

        model = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int column) {
                // 第一列是 Boolean (复选框)，其他是 String
                return column == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // 只有第一列（复选框）可编辑
            }
        };

        table = new JTable(model);
        table.setFillsViewportHeight(true); // 填充整个视口高度
        table.setBackground(Color.WHITE); // 表格背景
        table.setGridColor(Color.LIGHT_GRAY); // 网格线颜色
        table.getTableHeader().setBackground(new Color(230, 230, 230)); // 表头背景
        table.getTableHeader().setForeground(Color.BLACK); // 表头文字
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setRowHeight(25); // 设置行高
        table.setSelectionBackground(new Color(180, 210, 255)); // 选中行背景
        table.setSelectionForeground(Color.BLACK); // 选中行文字

        loadUserData(); // 加载数据到表格

        // --- 创建按钮 ---
        JButton deleteButton = UIUtils.createStyledButton("Delete Selected Users", Color.WHITE, new Color(220, 53, 69), new Dimension(180, 30), new Font("Segoe UI", Font.PLAIN, 12));
        deleteButton.addActionListener(e -> deleteSelectedUsers());

        JButton backButton = UIUtils.createStyledButton("Return", Color.DARK_GRAY, Color.LIGHT_GRAY, new Dimension(100, 30), new Font("Segoe UI", Font.PLAIN, 12));
        backButton.addActionListener(e -> cardLayout.show(contentPanel, "dashboard"));

        // --- 布局按钮 ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5)); // 右对齐
        buttonPanel.setBackground(userListPanel.getBackground()); // 与面板背景一致
        buttonPanel.add(deleteButton);
        buttonPanel.add(backButton);

        // --- 列表标题 ---
        JLabel listTitle = new JLabel("Select users to delete:");
        listTitle.setForeground(new Color(30, 60, 120)); // 深蓝色标题
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        listTitle.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 添加边距

        // --- 添加到 userListPanel ---
        userListPanel.add(listTitle, BorderLayout.NORTH); // 标题放顶部
        userListPanel.add(new JScrollPane(table), BorderLayout.CENTER); // 表格放中间
        userListPanel.add(buttonPanel, BorderLayout.SOUTH); // 按钮放底部

        userListPanel.revalidate(); // 重新验证布局
        userListPanel.repaint(); // 重新绘制
        cardLayout.show(contentPanel, "userList"); // 切换到用户列表面板
    }

    // 从 accounts.csv 加载用户数据到表格 (保持不变)
    private void loadUserData() {
        model.setRowCount(0); // 清空现有数据
        model.setColumnCount(0); // 清空现有列
        File file = new File("accounts.csv");
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "accounts.csv not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            List<String[]> userData = new ArrayList<>(); // 用于存储除头部外的所有行

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // 跳过空行
                String[] user = line.split(",", -1); // 使用 -1 保留尾部空字段
                if (firstLine) {
                    // 添加第一列用于复选框
                    model.addColumn("Select");
                    // 添加 CSV 文件中的列名 (假设头部与10列格式匹配)
                    if (user.length == EXPECTED_ACCOUNT_FIELD_COUNT) { // 确认头部是10列
                        for (String columnName : user) {
                            model.addColumn(columnName.trim());
                        }
                    } else {
                        // 如果头部不匹配，显示错误并停止加载
                        JOptionPane.showMessageDialog(this, "Error: accounts.csv header does not match the expected 10 columns.", "Header Error", JOptionPane.ERROR_MESSAGE);
                        model.setColumnCount(0); // 清空列防止显示错误
                        return;
                    }
                    firstLine = false;
                } else {
                    // 存储数据行，检查列数是否匹配
                    if (user.length == EXPECTED_ACCOUNT_FIELD_COUNT) {
                        userData.add(user);
                    } else {
                        System.err.println("Skipping data row due to incorrect column count ("+user.length+"): " + line);
                    }
                }
            }

            // 将数据行添加到表格模型
            for (String[] user : userData) {
                Object[] rowData = new Object[model.getColumnCount()]; // 列数由模型决定
                rowData[0] = false; // 默认不选中
                // 将 CSV 数据放入对应的列 (从第1列开始放，因为第0列是复选框)
                for (int i = 0; i < user.length; i++) {
                    if (i + 1 < model.getColumnCount()) { // 确保不越界
                        rowData[i + 1] = user[i];
                    }
                }
                model.addRow(rowData);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load user data: " + e.getMessage(), "Error Loading Data", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 删除选中的用户 (保持不变)
    private void deleteSelectedUsers() {
        List<String[]> allUsers = new ArrayList<>();
        File file = new File("accounts.csv");
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "accounts.csv not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // 读取 accounts.csv 中的所有行（包括头部）
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // 跳过空行
                allUsers.add(line.split(",", -1)); // 使用-1保留空字段
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to read data for deletion:" + e.getMessage(), "File Read Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (allUsers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "accounts.csv is empty or could not be read.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<String[]> remainingUsers = new ArrayList<>();
        // 保留头部行
        remainingUsers.add(allUsers.get(0)); // 添加头部

        Set<String> usernamesToDelete = new HashSet<>();
        int usernameColumnIndex = -1;
        // 动态查找 Username 列的索引 (因为第一列是 Select)
        for(int i=1; i < model.getColumnCount(); i++){
            if("Username".equalsIgnoreCase(model.getColumnName(i))){
                usernameColumnIndex = i;
                break;
            }
        }
        if(usernameColumnIndex == -1){
            JOptionPane.showMessageDialog(this, "Could not find 'Username' column in the table.", "Table Error", JOptionPane.ERROR_MESSAGE);
            return;
        }


        // 收集表格中被选中的用户名
        for (int i = 0; i < model.getRowCount(); i++) {
            boolean selected = (Boolean) model.getValueAt(i, 0);
            if (selected) {
                String username = (String) model.getValueAt(i, usernameColumnIndex); // 使用动态索引
                if (username != null && !username.trim().isEmpty()) {
                    usernamesToDelete.add(username.trim());
                }
            }
        }

        if (usernamesToDelete.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No users selected for deletion.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }


        // 构建剩余用户列表 (排除被选中的)
        if (allUsers.size() > 1) { // 确保文件不只包含头部
            for (int i = 1; i < allUsers.size(); i++) { // 从第二行开始（跳过头部）
                String[] user = allUsers.get(i);
                // 检查数据行是否有效且用户名不在删除列表内
                if (user.length > 0 && user[0] != null && !usernamesToDelete.contains(user[0].trim())) {
                    remainingUsers.add(user);
                }
            }
        }

        // 将剩余用户写回 accounts.csv (覆盖写)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("accounts.csv", false))) { // false for overwrite
            for (String[] user : remainingUsers) {
                bw.write(String.join(",", user));
                bw.newLine();
            }
            JOptionPane.showMessageDialog(this, "Selected users have been successfully deleted.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Write failed:" + e.getMessage(), "File Write Error", JOptionPane.ERROR_MESSAGE);
            return; // 写入失败则停止
        }

        // 删除成功后，重新加载数据以更新表格显示
        SwingUtilities.invokeLater(this::loadUserData);
    }

    // ============================================================
    // 修改：导入客户账户功能 (适配 10 列)
    // ============================================================
    private void importCustomerAccounts() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择要导入的客户账户文件 (10 列格式)"); // 提示格式
        // fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null) {
                importAccountsFromCSV(selectedFile); // 调用修改后的导入方法
            }
        }
    }

    // --- 修改此方法以处理 10 列 ---
    private void importAccountsFromCSV(File file) {
        List<String> validDataLines = new ArrayList<>();
        boolean headerProcessed = false;
        int importedCount = 0;
        Set<String> existingUsernames = loadExistingUsernames(); // 加载已存在的用户名以防重复

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue; // 跳过空行
                }

                if (!headerProcessed) {
                    // 检查头部 (现在检查 10 列的头部)
                    if (!line.trim().equalsIgnoreCase(EXPECTED_ACCOUNT_HEADER)) {
                        JOptionPane.showMessageDialog(this, "导入失败: 账户文件头部格式不正确。\n期望 (10 列): '" + EXPECTED_ACCOUNT_HEADER + "'\n实际: '" + line + "'", "导入错误", JOptionPane.ERROR_MESSAGE);
                        return; // 头部不正确，停止导入
                    }
                    headerProcessed = true;
                    continue; // 跳过头部行
                }

                // 处理数据行
                String[] fields = line.split(",", -1); // 使用-1保留空字段

                // 验证字段数量 (现在检查 10 列)
                if (fields.length != EXPECTED_ACCOUNT_FIELD_COUNT) {
                    System.err.println("跳过无效账户行 (字段数量应为 10, 第 " + lineNumber + " 行): " + line);
                    continue; // 跳过格式错误的行
                }

                // --- 进行更详细的验证 ---
                String username = fields[0].trim();
                String password = fields[1].trim();
                String phone = fields[2].trim();
                String email = fields[3].trim();
                String gender = fields[4].trim();
                String address = fields[5].trim();
                String creationTime = fields[6].trim();
                String accountStatus = fields[7].trim();
                String accountType = fields[8].trim();
                String balanceStr = fields[9].trim();

                boolean isValidLine = true;
                // 1. 检查用户名是否为空
                if (username.isEmpty()) {
                    isValidLine = false;
                    System.err.println("跳过无效账户行 (用户名为空, 第 " + lineNumber + " 行): " + line);
                }
                // 2. 检查用户名是否已存在
                else if (existingUsernames.contains(username)) {
                    isValidLine = false;
                    System.err.println("跳过无效账户行 (用户名 '" + username + "' 已存在, 第 " + lineNumber + " 行): " + line);
                }
                // 3. 检查余额是否是数字
                else {
                    try {
                        Double.valueOf(balanceStr);
                    } catch (NumberFormatException e) {
                        isValidLine = false;
                        System.err.println("跳过无效账户行 (余额格式不正确, 第 " + lineNumber + " 行): " + line);
                    }
                }
                // 4. 检查账户状态 (可以更严格)
                if (isValidLine && !("ACTIVE".equalsIgnoreCase(accountStatus) || "FROZEN".equalsIgnoreCase(accountStatus))) {
                    // 如果需要强制有效状态，设置 isValidLine = false;
                    System.err.println("警告: 账户行状态值无效 ('" + accountStatus + "'), 第 " + lineNumber + " 行，将尝试导入。");
                }
                // 5. 检查账户类型 (可以更严格)
                if (isValidLine && !("personal".equalsIgnoreCase(accountType) || "Admin".equalsIgnoreCase(accountType))) {
                    // 如果需要强制有效类型，设置 isValidLine = false;
                    System.err.println("警告: 账户行类型值无效 ('" + accountType + "'), 第 " + lineNumber + " 行，将尝试导入。");
                }
                // 6. 检查日期格式 (可选)
                if (isValidLine && !creationTime.isEmpty()) { // 只在非空时检查
                    try {
                        // 使用与你 accounts.csv 中一致的格式
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(creationTime);
                    } catch (java.text.ParseException pe) {
                        // 如果需要强制有效日期，设置 isValidLine = false;
                        System.err.println("警告: 创建时间格式无效 ('" + creationTime + "'), 第 " + lineNumber + " 行，将尝试导入。");
                    }
                }


                if (isValidLine) {
                    // 注意：如果字段本身可能包含逗号，需要更复杂的CSV处理库或手动转义
                    validDataLines.add(line);
                    existingUsernames.add(username); // 添加到集合以检查文件内重复
                }
            }
            // 检查文件是否有效
            if (!headerProcessed && lineNumber > 0) {
                JOptionPane.showMessageDialog(this, "导入失败: 账户文件为空或不包含有效的头部。", "导入错误", JOptionPane.ERROR_MESSAGE);
                return;
            } else if (lineNumber == 0 || validDataLines.isEmpty() && lineNumber == 1) { // 文件完全为空，或只有头部
                JOptionPane.showMessageDialog(this, "源账户文件为空或不包含有效数据行。", "导入提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }


        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "读取账户文件失败: " + e.getMessage(), "导入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 将验证通过的数据行追加到 accounts.csv
        if (!validDataLines.isEmpty()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter("accounts.csv", true))) {
                File accountFile = new File("accounts.csv");
                if (accountFile.exists() && accountFile.length() > 0) {
                    // 确保文件末尾有换行符
                    try (RandomAccessFile raf = new RandomAccessFile(accountFile, "r")) {
                        if (raf.length() > 0) {
                            raf.seek(raf.length() - 1);
                            if (raf.readByte() != '\n') {
                                bw.newLine();
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("检查accounts.csv文件末尾换行符失败: " + e.getMessage());
                    }
                } else {
                    // 如果文件不存在或为空，写入头部 (虽然理论上主逻辑会创建)
                    bw.write(EXPECTED_ACCOUNT_HEADER);
                    bw.newLine();
                }

                for (String dataLine : validDataLines) {
                    bw.write(dataLine);
                    bw.newLine();
                    importedCount++;
                }
                JOptionPane.showMessageDialog(this, "成功导入 " + importedCount + " 条客户账户记录。", "导入成功", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "写入 accounts.csv 文件失败: " + e.getMessage(), "导入错误", JOptionPane.ERROR_MESSAGE);
            }
            refreshUserListIfVisible(); // 导入后刷新列表
        } else {
            // 在之前的检查中已经提示过 "没有有效记录"
            System.out.println("没有有效的账户记录可导入。");
        }
    }

    // --- 辅助方法：加载已存在的用户名 ---
    private Set<String> loadExistingUsernames() {
        Set<String> usernames = new HashSet<>();
        File file = new File("accounts.csv");
        if (!file.exists()) {
            return usernames;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); // 跳过头部
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length > 0 && !parts[0].trim().isEmpty()) {
                    usernames.add(parts[0].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("加载现有用户名时出错: " + e.getMessage());
        }
        return usernames;
    }

    // --- 辅助方法：刷新用户列表 ---
    private void refreshUserListIfVisible() {
        Component currentVisiblePanel = null;
        for (Component comp : contentPanel.getComponents()) {
            if (comp.isVisible()) {
                currentVisiblePanel = comp;
                break;
            }
        }
        if (currentVisiblePanel == userListPanel) {
            loadUserData();
        }
    }


    // --- 导入交易记录功能 (保持不变) ---
    private void importTransactionRecords() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择要导入的交易记录文件 (5 列格式)");
        // fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null) {
                processTransactionRecordsImport(selectedFile);
            }
        }
    }

    // --- 使用 TransactionCSVImporter (保持不变) ---
    private void processTransactionRecordsImport(File file) {
        if (file == null) return;
        String destinationFilePath = "transactions.csv";
        try {
            int importedCount = TransactionCSVImporterModel.importTransactions(file, destinationFilePath);
            JOptionPane.showMessageDialog(this, "成功导入 " + importedCount + " 条交易记录。", "导入成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "导入失败: 文件格式错误 - " + e.getMessage(), "导入错误", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "导入失败: 文件读写错误 - " + e.getMessage(), "导入错误", JOptionPane.ERROR_MESSAGE);
        } catch (HeadlessException e) {
            JOptionPane.showMessageDialog(this, "导入过程中发生未知错误: " + e.getMessage(), "导入错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- 管理员验证和修改客户信息功能 (保持不变) ---
    public void displayAdminVerificationForm() {
        JDialog dialog = new JDialog(this, "Administrator Validation", true);
        dialog.setSize(350, 180); // 稍微调大一点高度
        dialog.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new GridBagLayout()); // 使用 GridBagLayout 更好控制
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        panel.add(new JLabel("Admin Username:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        JTextField tfUser = new JTextField();
        panel.add(tfUser, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        panel.add(new JLabel("Admin Password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        JPasswordField pfPass = new JPasswordField();
        panel.add(pfPass, gbc);

        // Validate Button
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2; // Span button across both columns
        gbc.fill = GridBagConstraints.NONE; // Don't stretch button
        gbc.anchor = GridBagConstraints.CENTER; // Center button
        JButton btn = new JButton("Validate");
        panel.add(btn, gbc);

        dialog.add(panel);

        btn.addActionListener(e -> {
            String username = tfUser.getText();
            String password = new String(pfPass.getPassword());
            AccountModel adminAccount = getAccount(username, password); // Uses UserRegistrationCSVExporter internally

            if (adminAccount != null && adminAccount.isAdmin()) { // Check if it's an admin account
                dialog.dispose();
                String customerUsername = JOptionPane.showInputDialog(this, "Please enter the customer's username to modify:");
                if (customerUsername != null && !customerUsername.trim().isEmpty()) {
                    AccountModel targetAccount = findAccountByUsername(customerUsername.trim()); // Find without password check
                    if (targetAccount != null) {
                        // Found customer, open modify dialog
                        PersonModifyService modifyDialog = new PersonModifyService(); // Assuming constructor is parameterless
                        modifyDialog.setAccountInfo(targetAccount); // Pass account data to the dialog
                        modifyDialog.setVisible(true); // Show the dialog (likely modal)

                        // After modification dialog closes, refresh user list if visible
                        refreshUserListIfVisible();

                    } else {
                        JOptionPane.showMessageDialog(this, "Customer username '" + customerUsername + "' not found!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else if (customerUsername != null) { // User clicked OK but input was empty
                    JOptionPane.showMessageDialog(this, "Customer username cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
                }
                // If user clicked Cancel (customerUsername == null), do nothing
            } else {
                JOptionPane.showMessageDialog(dialog, "Invalid administrator username or password, or not an admin account!", "Validation Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog.setVisible(true);
    }

    // --- 使用 UserRegistrationCSVExporter 读取数据 (保持不变) ---
    public static AccountModel getAccount(String username, String password) {
        // UserRegistrationCSVExporter handles reading the 10-column CSV
        for (AccountModel a : UserRegistrationCSVExporterModel.readFromCSV()) {
            if (a.getUsername().equals(username) && a.getPassword().equals(password)) {
                return a;
            }
        }
        return null;
    }

    // --- 使用 UserRegistrationCSVExporter 读取数据 (保持不变) ---
    public static AccountModel findAccountByUsername(String username) {
        // UserRegistrationCSVExporter handles reading the 10-column CSV
        for (AccountModel a : UserRegistrationCSVExporterModel.readFromCSV()) {
            if (a.getUsername().equals(username)) {
                return a;
            }
        }
        return null;
    }


    // ============================================================
    // 修改：main 方法 (确保创建文件时使用 10 列头部)
    // ============================================================
    public static void main(String[] args) {
        // 确保 accounts.csv 使用 10 列头部创建
        ensureFileExists("accounts.csv", EXPECTED_ACCOUNT_HEADER); // EXPECTED_ACCOUNT_HEADER 已改为 10 列
        // transactions.csv 使用 TransactionCSVImporter 的 5 列头部
        ensureFileExists("transactions.csv", TransactionCSVImporterModel.EXPECTED_HEADER);

        SwingUtilities.invokeLater(AdminPlane::new);
    }

    // --- 辅助方法：确保文件存在 (保持不变) ---
    private static void ensureFileExists(String filename, String header) {
        File file = new File(filename);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    System.out.println("Created new file: " + filename);
                    // 写入指定的头部
                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                        bw.write(header);
                        bw.newLine();
                        System.out.println("Written header to " + filename + ": " + header);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error creating file " + filename + ": " + e.getMessage());
            }
        } else {
            // 文件已存在，可选：检查头部是否匹配
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String firstLine = br.readLine();
                if (firstLine == null || !firstLine.trim().equalsIgnoreCase(header.trim())) {
                    System.err.println("WARNING: File " + filename + " exists but header does not match expected value.");
                    System.err.println("  Expected: " + header.trim());
                    System.err.println("  Actual: " + (firstLine != null ? firstLine.trim() : "<empty>"));
                }
            } catch (IOException e) {
                System.err.println("Error checking header for file " + filename + ": " + e.getMessage());
            }
        }
    }
}