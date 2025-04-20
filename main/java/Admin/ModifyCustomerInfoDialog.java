package Admin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import Model.AccountModel;
import Model.AdminAccount;
import Model.UserRegistrationCSVExporter;
import Model.UserSession;

public class ModifyCustomerInfoDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField genderField;
    private JTextField addressField;
    private JComboBox<String> accountStatusComboBox; // 新增账户状态下拉框
    private JButton confirmButton;
    private JPasswordField adminPasswordField;

    public ModifyCustomerInfoDialog() {
        setTitle("Modify Customer Information");
        setSize(400, 450); // 增加垂直高度以容纳新组件
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 创建输入框和标签
        JLabel usernameLabel = new JLabel("Username (cannot be modified):");
        usernameField = new JTextField(20);
        usernameField.setEditable(false); // 用户名不可修改

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);

        JLabel phoneLabel = new JLabel("Phone Number:");
        phoneField = new JTextField(20);

        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField(20);

        JLabel genderLabel = new JLabel("Gender:");
        genderField = new JTextField(20);

        JLabel addressLabel = new JLabel("Address:");
        addressField = new JTextField(20);

        JLabel accountStatusLabel = new JLabel("Account Status:"); // 新增账户状态标签
        accountStatusComboBox = new JComboBox<>(new String[]{"ACTIVE", "FROZEN"}); // 新增账户状态下拉框，默认选项为 ACTIVE 和 FROZEN
        accountStatusComboBox.setPreferredSize(new Dimension(200, 25)); // 设置下拉框的首选大小

        JLabel adminPasswordLabel = new JLabel("Admin Password:");
        adminPasswordField = new JPasswordField(20);

        confirmButton = new JButton("Confirm Modification");

        // 布局设置
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(9, 2, 10, 10)); // 9 行，因为新增了账户状态字段
        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(phoneLabel);
        panel.add(phoneField);
        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(genderLabel);
        panel.add(genderField);
        panel.add(addressLabel);
        panel.add(addressField);
        panel.add(accountStatusLabel); // 添加账户状态标签
        panel.add(accountStatusComboBox); // 添加账户状态下拉框
        panel.add(adminPasswordLabel);
        panel.add(adminPasswordField);
        panel.add(new JLabel("")); // 占位
        panel.add(confirmButton);

        // 按钮事件
        confirmButton.addActionListener((ActionEvent e) -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String phone = phoneField.getText();
            String email = emailField.getText();
            String gender = genderField.getText();
            String address = addressField.getText();
            String accountStatus = (String) accountStatusComboBox.getSelectedItem(); // 获取选中的账户状态
            String adminPassword = new String(adminPasswordField.getPassword());
            
            // 验证管理员密码是否正确
            if (!isAdminPasswordValid(adminPassword)) {
                JOptionPane.showMessageDialog(null, "Incorrect admin password!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            AccountModel account = AdminModifyService.getAccount(username, password);
            
            if (account != null) {
                // 调用 AdminModifyService 的方法来更新客户信息
                boolean updatedInfo = AdminModifyService.updateCustomerInfo(username, password, phone, email, gender, address);
                // 调用 AdminModifyService 的方法来更新账户状态
                AdminModifyService.modifyAccountStatus(username, accountStatus);
                
                if (updatedInfo) {
                    JOptionPane.showMessageDialog(null, "Customer information updated successfully!");
                    dispose(); // 关闭对话框
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to update customer information!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "User does not exist or password incorrect!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);

        setVisible(true);
    }

    // 验证管理员密码是否正确
    private boolean isAdminPasswordValid(String adminPassword) {
        // 获取当前登录的管理员用户名
        String currentAdminUsername = UserSession.getCurrentUsername();

        if (currentAdminUsername == null) {
            JOptionPane.showMessageDialog(null, "Admin not logged in!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // 从 CSV 中读取所有账户数据
        java.util.List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();

        // 查找当前管理员账户
        for (AccountModel account : accounts) {
            if (account.getUsername().equals(currentAdminUsername) && account instanceof AdminAccount) {
                // 如果找到了对应的管理员账户，验证密码
                if (account.getPassword().equals(adminPassword)) {
                    return true;
                } else {
                    JOptionPane.showMessageDialog(null, "Incorrect password!", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }

        // 如果没有找到对应的管理员账户
        JOptionPane.showMessageDialog(null, "Admin account not found!", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
    }


    // 用于设置要修改的客户信息 (在显示对话框之前调用)
    public void setAccountInfo(AccountModel account) {
        if (account != null) {
            usernameField.setText(account.getUsername());
            passwordField.setText(account.getPassword()); // 允许管理员修改密码，所以需要显示
            phoneField.setText(account.getPhone());
            emailField.setText(account.getEmail());
            genderField.setText(account.getGender());
            addressField.setText(account.getAddress());
            accountStatusComboBox.setSelectedItem(account.getAccountStatus()); // 设置账户状态下拉框的选中项
        }
    }

   

}