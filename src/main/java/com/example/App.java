package com.example; // 这个包声明是正确的

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.net.URL; // 需要导入 java.net.URL

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import src.UI.AccountManagementUI;


public class App extends JFrame {

    public App() {
        setTitle("Smart Finance - Login");
        setSize(900, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // 背景面板
        JPanel backgroundPanel = new JPanel() {
            // >>> 修改图片加载路径 <<<
            // 从 resources/images/ 中加载 background.png
            URL bgImageUrl = getClass().getResource("/images/background.png"); // 注意路径修改为 /images/
            Image bgImage = null;
            { // 使用实例初始化块来处理可能为 null 的情况
                if (bgImageUrl != null) {
                    bgImage = new ImageIcon(bgImageUrl).getImage();
                } else {
                    System.err.println("Error: Background image not found at /images/background.png");
                    // 可以选择加载一个默认图片或者不设置背景图
                }
            }


            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) { // 只有图片成功加载才绘制
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        backgroundPanel.setLayout(null);
        backgroundPanel.setBounds(0, 0, 900, 500);
        add(backgroundPanel);


        // 插入理财图片
        // >>> 修改图片加载路径 <<<
        // 从 resources/images/ 中加载 log_img.png
        URL logImageUrl = getClass().getResource("/images/log_img.png"); // 注意路径修改为 /images/
        ImageIcon originalIcon = null;
        if (logImageUrl != null) {
             originalIcon = new ImageIcon(logImageUrl);
        } else {
             System.err.println("Error: Log image not found at /images/log_img.png");
             // 可以创建一个空白的 ImageIcon 或者处理错误
             originalIcon = new ImageIcon(); // 创建一个空的 ImageIcon 避免 NullPointerException
        }


        Image originalImage = originalIcon.getImage();

        // 缩放到 300x250
        Image scaledImage = originalImage.getScaledInstance(300, 250, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        JLabel imageLabel = new JLabel(scaledIcon);
        imageLabel.setBounds(150, 120, 300, 250);  // 尺寸和缩放后保持一致
        backgroundPanel.add(imageLabel);

        // 左上角 Smart Finance 艺术字
        JLabel titleArtLabel = new JLabel("Smart Finance");
        titleArtLabel.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, 32));
        titleArtLabel.setForeground(Color.WHITE);
        titleArtLabel.setBounds(30, 30, 400, 40); // 位置靠左上，宽度要足够显示文字
        backgroundPanel.add(titleArtLabel);



        // 自定义圆角登录面板
        JPanel loginPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.LIGHT_GRAY);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
                g2.dispose();
            }
        };
        loginPanel.setOpaque(false);
        loginPanel.setLayout(null);
        loginPanel.setBounds(450, 120, 300, 250);
        backgroundPanel.add(loginPanel);

        JLabel titleLabel = new JLabel("User Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setBounds(80, 20, 200, 30);
        loginPanel.add(titleLabel);

        JButton loginButton = new JButton("Log in");
        styleRoundedButton(loginButton);
        loginButton.setBounds(60, 80, 180, 40);
        loginPanel.add(loginButton);

        JButton registerButton = new JButton("Sign in");
        styleRoundedButton(registerButton);
        registerButton.setBounds(60, 140, 180, 40);
        loginPanel.add(registerButton);

        setVisible(true);
        // 按钮监听事件
        // >>> 修改 AccountManagementUI 引用 <<<
        loginButton.addActionListener(e -> new AccountManagementUI(App.this, "login")); // 删除 src.UI
        registerButton.addActionListener(e -> new AccountManagementUI(App.this, "register")); // 删除 src.UI

    }

    private void styleRoundedButton(JButton button) {
        button.setBackground(new Color(0, 120, 215));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setBorder(BorderFactory.createLineBorder(new Color(0, 120, 215)));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::new);
    }
}