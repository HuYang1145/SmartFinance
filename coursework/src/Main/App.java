package Main;

import javax.swing.*;
import UI.AccountManagementUI;
import java.awt.*;
import java.awt.event.*;

public class App extends JFrame {

    public App() {
        setTitle("银行管理系统");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 窗口居中

        // 创建按钮
        JButton loginButton = new JButton("登录账户");
        JButton registerButton = new JButton("注册账户");

        // 设置按钮的监听事件
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 打开账户管理界面，进行登录操作
                new AccountManagementUI(App.this, "login");
            }
        });

        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 打开账户管理界面，进行注册操作
                new AccountManagementUI(App.this, "register");
            }
        });

        // 设置布局
        setLayout(new FlowLayout());
        add(loginButton);
        add(registerButton);

        setVisible(true);
    }

    public static void main(String[] args) {
        // 在 Event Dispatch Thread 中启动界面
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new App();
            }
        });
    }
}
