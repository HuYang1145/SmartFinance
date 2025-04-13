package Person; // 确保包声明是 Person

import model.AccountModel;
import model.UserRegistrationCSVExporter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ViewPersonalInfo extends JDialog { // 类名改为 ViewPersonalInfo

    public ViewPersonalInfo(JFrame frame, String username) { // 父类改为 JDialog
        super(frame, "个人信息", true);
        setSize(400, 300);
        setLocationRelativeTo(frame);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(9, 2, 10, 10)); // 调整行数以适应所有信息和按钮

        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        AccountModel userAccount = null;

        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username)) {
                userAccount = account;
                break;
            }
        }

        if (userAccount != null) {
            add(new JLabel("用户名:"));
            add(new JLabel(userAccount.getUsername()));  
        
            add(new JLabel("手机号:"));
            add(new JLabel(userAccount.getPhone()));  

            add(new JLabel("邮箱:"));
            add(new JLabel(userAccount.getEmail())); 
        
            add(new JLabel("性别:"));
            add(new JLabel(userAccount.getGender()));  
        
            add(new JLabel("地址:"));
            add(new JLabel(userAccount.getAddress()));  
        
            add(new JLabel("创建时间:"));
            add(new JLabel(userAccount.getCreationTime()));  

            add(new JLabel("账户状态:"));
            add(new JLabel(userAccount.getAccountStatus())); 

            add(new JLabel("账户类型:"));
            add(new JLabel(userAccount.getAccountType())); 

            JButton returnButton = new JButton("返回");
        returnButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                SwingUtilities.invokeLater(() -> {
                    dispose();
                });
            }
        });
        add(new JLabel("")); // 用于占据一个网格位置，使按钮靠右
        add(returnButton);
        } else {
            JOptionPane.showMessageDialog(this, "找不到用户信息", "错误", JOptionPane.ERROR_MESSAGE);
            dispose();
        }

        setVisible(true);
    }

    public static void main(String[] args) {
        // 用于测试 ViewPersonalInfo
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(100, 100);
        frame.setVisible(true);
        ViewPersonalInfo dialog = new ViewPersonalInfo(frame, "testuser"); // 将 "testuser" 替换为你的测试用户名
        dialog.setVisible(true);
    }
}