package Admin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import model.*;

public class ModifyCustomerInfoDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField genderField;
    private JTextField addressField;
    private JButton confirmButton;
    private JPasswordField adminPasswordField;

    public ModifyCustomerInfoDialog() {
        setTitle("修改客户信息");
        setSize(400, 400); // 增加垂直高度
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 创建输入框和标签
        JLabel usernameLabel = new JLabel("用户名 (不可修改):");
        usernameField = new JTextField(20);
        usernameField.setEditable(false); // 用户名不可修改

        JLabel passwordLabel = new JLabel("密码:");
        passwordField = new JPasswordField(20);

        JLabel phoneLabel = new JLabel("手机号:");
        phoneField = new JTextField(20);

        JLabel emailLabel = new JLabel("邮箱:");
        emailField = new JTextField(20);

        JLabel genderLabel = new JLabel("性别:");
        genderField = new JTextField(20);

        JLabel addressLabel = new JLabel("地址:");
        addressField = new JTextField(20);

        JLabel adminPasswordLabel = new JLabel("管理员密码:");
        adminPasswordField = new JPasswordField(20);  // 用来输入管理员密码

        confirmButton = new JButton("确认修改");

        // 布局设置
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(8, 2, 10, 10)); // 8 行，因为有 8 个可编辑或只读的字段
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
        panel.add(adminPasswordLabel); // 添加管理员密码字段
        panel.add(adminPasswordField);  // 管理员密码输入框
        panel.add(new JLabel("")); // 占位
        panel.add(confirmButton);

        // 按钮事件
        confirmButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String phone = phoneField.getText();
                String email = emailField.getText();
                String gender = genderField.getText();
                String address = addressField.getText();
                String adminPassword = new String(adminPasswordField.getPassword());  // 获取管理员输入的密码

                // 验证管理员密码是否正确
                if (!isAdminPasswordValid(adminPassword)) {
                    JOptionPane.showMessageDialog(null, "管理员密码错误！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                AccountModel account = AdminModifyService.getAccount(username, password);

                if (account != null) {
                    // 调用 AdminModifyService 的方法来更新客户信息
                    boolean updated = AdminModifyService.updateCustomerInfo(username, password, phone, email, gender, address);

                    if (updated) {
                        JOptionPane.showMessageDialog(null, "客户信息修改成功！");
                        dispose(); // 关闭对话框
                    } else {
                        JOptionPane.showMessageDialog(null, "修改客户信息失败！", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "用户不存在或密码错误！", "错误", JOptionPane.ERROR_MESSAGE);
                }
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
        JOptionPane.showMessageDialog(null, "管理员未登录！", "错误", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(null, "密码错误！", "错误", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
    }

    // 如果没有找到对应的管理员账户
    JOptionPane.showMessageDialog(null, "没有找到该管理员账户！", "错误", JOptionPane.ERROR_MESSAGE);
    return false;
}


    // 单独列出的修改用户状态函数 (不实现)
    public void modifyAccountStatus(String username, String newStatus) {
        // 这里将实现修改用户账户状态的逻辑
        System.out.println("修改用户 " + username + " 的状态为: " + newStatus);
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
        }
    }

}
