package Main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class App extends JFrame {

    public App() {
        setTitle("Smart Finance - Login");
        setSize(900, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // 背景面板
        JPanel backgroundPanel = new JPanel() {
            Image bgImage = new ImageIcon(getClass().getResource("/Main/background.png")).getImage(); // 替换为你的图片路径

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(null);
        backgroundPanel.setBounds(0, 0, 900, 500);
        add(backgroundPanel);


        // 插入理财图片
        ImageIcon originalIcon = new ImageIcon(getClass().getResource("/Main/log_img.png"));
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
        loginButton.addActionListener(e -> new UI.AccountManagementUI(App.this, "login"));
        registerButton.addActionListener(e -> new UI.AccountManagementUI(App.this, "register"));

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
