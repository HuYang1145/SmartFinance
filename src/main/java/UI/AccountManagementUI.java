package UI;

import AccountModel.AccountManagementController;
import UI.RoundedInputField.*;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class AccountManagementUI extends JFrame {
    private JPanel sidebar;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private AccountManagementController controller;

    public AccountManagementUI(AccountManagementController controller) {
        this.controller = controller;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Smart Finance - Welcome");
        setSize(750, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Main container with light blue background
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(230, 230, 250)); // #E6F0FA
        add(mainPanel);

        // Sidebar (Left)
        sidebar = createSidebar(false);
        mainPanel.add(sidebar, BorderLayout.WEST);

        // Content Panel (Right)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(new Color(230, 230, 250));
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // 初始化各面板
        initializeContentPanels();

        setVisible(true);
    }

    private List<NavItemPanel> navItems = new ArrayList<>();

    private JPanel createSidebar(boolean isLoggedIn) {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(Color.WHITE);
        sb.setPreferredSize(new Dimension(200, 0));

        // 1) 顶部 logo (这里用文字代替)
        GradientLabel logo = new GradientLabel("Smart Finance");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setBorder(new EmptyBorder(20, 0, 20, 0));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        sb.add(logo);

        // 3) 菜单项
        String[] options = isLoggedIn
                ? new String[]{"Personal Main", "Account Management"}
                : new String[]{"Welcome", "Login", "Register"};

        for (String opt : options) {
            NavItemPanel item = new NavItemPanel(opt);
            navItems.add(item);
            sb.add(item);
            sb.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        // 默认选中第一个
        if (!navItems.isEmpty()) navItems.get(0).setSelected(true);

        return sb;
    }

    /**
     * 搜索框样式
     **/
    private void styleSearchField(JTextComponent tf, String placeholder) {
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBackground(new Color(240, 240, 240));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(17, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        tf.setText(placeholder);
        tf.setForeground(Color.GRAY);
        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(Color.DARK_GRAY);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) {
                    tf.setText(placeholder);
                    tf.setForeground(Color.GRAY);
                }
            }
        });
    }

    /**
     * 左侧每一项的面板，点击选中时画渐变背景
     */
    class NavItemPanel extends JPanel {
        private String name;
        private JLabel iconLabel;
        private JLabel textLabel;
        private boolean selected = false;

        public NavItemPanel(String name) {
            this.name = name;
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setBorder(new EmptyBorder(0, 12, 0, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // TODO: 这里可以换成真正的图标
            iconLabel = new JLabel("\u25CF"); // 占位圆点
            iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            iconLabel.setForeground(new Color(150, 150, 150));

            textLabel = new JLabel(name);
            textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            textLabel.setBorder(new EmptyBorder(0, 8, 0, 0));
            textLabel.setForeground(new Color(100, 100, 100));

            add(iconLabel);
            add(textLabel);
            add(Box.createHorizontalGlue());

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // 切卡
                    cardLayout.show(contentPanel, name);

                    // 更新选中状态
                    for (NavItemPanel it : navItems) {
                        it.setSelected(it == NavItemPanel.this);
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!selected) setBackground(new Color(245, 245, 245));
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!selected) {
                        setBackground(null);
                        repaint();
                    }
                }
            });
        }

        public void setSelected(boolean sel) {
            this.selected = sel;
            if (sel) {
                // 文字变白
                textLabel.setForeground(Color.WHITE);
                iconLabel.setForeground(Color.WHITE);
            } else {
                textLabel.setForeground(new Color(100, 100, 100));
                iconLabel.setForeground(new Color(150, 150, 150));
                setBackground(null);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (selected) {
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(156, 39, 176),
                        w, 0, new Color(0, 47, 167)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 12, 12);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    private void initializeContentPanels() {
        contentPanel.add(wrapInScroll(createWelcomePanel()),       "Welcome");
        contentPanel.add(wrapInScroll(createLoginPanel()),         "Login");
        contentPanel.add(wrapInScroll(createRegisterPanel()),      "Register");
        contentPanel.add(wrapInScroll(createPersonalMain()),       "Personal Main");
        contentPanel.add(wrapInScroll(createPersonalCenter()),     "Account Management");
        cardLayout.show(contentPanel, "Welcome");
    }

    // --- 面板工厂方法 ---
    private JPanel createWelcomePanel() {
        JPanel p = new GradientPanel();
        p.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 60, 20, 60);  // 增大垂直间距

        // 1. 标题 User Account
        JLabel title = new JLabel("User Account", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32)); // 字体更大
        title.setForeground(Color.WHITE);
        gbc.gridy = 0;
        gbc.ipady = 10;
        p.add(title, gbc);


        // 2. Log in 按钮
        GradientTextButton bLogin = new GradientTextButton("Log in");
        bLogin.setPreferredSize(new Dimension(0, 40));

        bLogin.setBackground(Color.WHITE);

// ✅ 去掉边框绘制 + 不透明背景绘制
        bLogin.setBorderPainted(false);                // 不画边框
        bLogin.setFocusPainted(false);                 // 去掉焦点虚线
        bLogin.setContentAreaFilled(true);             // 使用 background
        bLogin.setOpaque(true);                        // 启用 background 不透明
        bLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                bLogin.setBackground(new Color(245, 245, 255));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                bLogin.setBackground(Color.WHITE);
            }
        });

        bLogin.addActionListener(e -> cardLayout.show(contentPanel, "Login"));
        gbc.gridy = 1;
        p.add(bLogin, gbc);

        // 3. Sign in 按钮
        GradientTextButton bReg = new GradientTextButton("Sign in");
        bReg.setPreferredSize(new Dimension(0, 40));
        bReg.setBorderPainted(false);
        bReg.setFocusPainted(false);
        bReg.setContentAreaFilled(true);
        bReg.setOpaque(true);
        bReg.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bReg.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                bReg.setBackground(new Color(245, 245, 255));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                bReg.setBackground(Color.WHITE);
            }
        });

        bReg.addActionListener(e -> cardLayout.show(contentPanel, "Register"));
        gbc.gridy = 2;
        p.add(bReg, gbc);

        return p;
    }


    private JPanel createLoginPanel() {
        JPanel p = new GradientPanel();
        p.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE; // ✅ 不拉伸
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(12, 0, 12, 0); // 垂直间距

        // 1. 标题
        JLabel title = new JLabel("Finance Management System", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        gbc.gridy = 0;
        p.add(title, gbc);

        // 2. Username
        RoundedTextField userField = new RoundedTextField("Username");
        gbc.gridy = 1;
        p.add(userField, gbc);

        // 3. Password
        RoundedPasswordField passField = new RoundedPasswordField("Password");
        gbc.gridy = 2;
        p.add(passField, gbc);

        // 4. 登录按钮
        JButton btn = new JButton("Log in");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0, 47, 167));
        btn.setFocusPainted(false);
        btn.setBorder(new RoundBorder(30, new Color(0, 47, 167)));
        btn.setPreferredSize(new Dimension(240, 50)); // ✅ 与输入框宽度一致
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(30, 70, 200));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(0, 47, 167));
            }
        });
        btn.addActionListener(e ->
                controller.handleLogin(
                        userField.getText(),
                        new String(passField.getPassword()),
                        AccountManagementUI.this
                )
        );
        gbc.gridy = 3;
        p.add(btn, gbc);

        return p;
    }



    private JPanel createRegisterPanel() {
        JPanel p = new GradientPanel();
        p.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(12, 0, 12, 0);

        // 标题
        JLabel title = new JLabel("Register New Account", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        gbc.gridy = 0;
        p.add(title, gbc);

        // Username
        RoundedTextField usernameField = new RoundedTextField("Username");
        gbc.gridy = 1;
        p.add(usernameField, gbc);

        // Password
        RoundedPasswordField passwordField = new RoundedPasswordField("Password");
        gbc.gridy = 2;
        p.add(passwordField, gbc);

        // Phone
        RoundedTextField phoneField = new RoundedTextField("Phone");
        gbc.gridy = 3;
        p.add(phoneField, gbc);

        // Email
        RoundedTextField emailField = new RoundedTextField("Email");
        gbc.gridy = 4;
        p.add(emailField, gbc);

        // Gender 下拉
        RoundedComboBox<String> genderBox = new RoundedComboBox<>(new String[]{"Male", "Female"});
        gbc.gridy = 5;
        p.add(genderBox, gbc);

        // Address
        RoundedTextField addressField = new RoundedTextField("Address");
        gbc.gridy = 6;
        p.add(addressField, gbc);

        // Account Type 下拉
        RoundedComboBox<String> acctTypeBox = new RoundedComboBox<>(new String[]{"Personal", "Admin"});
        gbc.gridy = 7;
        p.add(acctTypeBox, gbc);

        // 注册按钮
        JButton registerBtn = new JButton("Register");
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setBackground(new Color(0, 47, 167));
        registerBtn.setFocusPainted(false);
        registerBtn.setBorder(new RoundBorder(30, new Color(0, 47, 167)));
        registerBtn.setPreferredSize(new Dimension(240, 50));
        registerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { registerBtn.setBackground(new Color(30,70,200)); }
            @Override public void mouseExited(MouseEvent e) { registerBtn.setBackground(new Color(0,47,167)); }
        });
        registerBtn.addActionListener(e -> {
            String username = usernameField.getActualText();
            String password = passwordField.getActualPassword();
            String phone    = phoneField.getActualText();
            String email    = emailField.getActualText();
            String gender   = genderBox.getSelectedItem().toString();
            String address  = addressField.getActualText();
            String acctType = acctTypeBox.getSelectedItem().toString();

            controller.handleRegister(
                    username, password, phone,
                    email, gender, address,
                    acctType, AccountManagementUI.this
            );
        });
        gbc.gridy = 8;
        p.add(registerBtn, gbc);

        return p;
    }



    private JScrollPane wrapInScroll(JPanel panel) {
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        // 保持背景一致
        scroll.getViewport().setBackground(panel.getBackground());
        return scroll;
    }


    private JPanel createPersonalMain() {
        JPanel p = new JPanel();
        p.setBackground(new Color(230, 240, 250));
        p.add(new JLabel("Welcome to Personal Main"));
        return p;
    }

    private JPanel createPersonalCenter() {
        JPanel p = new JPanel();
        p.setBackground(new Color(230, 240, 250));
        p.add(new JLabel("Account Management - Personal Center"));
        return p;
    }

    // -------- 样式方法 --------

    /**
     * 给按钮设渐变边框
     **/
    private void styleButton(JButton btn) {
        btn.setBackground(Color.WHITE);
        btn.setOpaque(true);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new GradientBorder(2, 12));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(245, 245, 255));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
        });
    }

    /**
     * 渐变文本 Label
     **/

    /**
     * 渐变圆角边框
     **/
    static class GradientBorder extends AbstractBorder {
        private int thickness, radius;

        public GradientBorder(int thickness, int radius) {
            this.thickness = thickness;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            Shape outer = new RoundRectangle2D.Float(
                    x + thickness / 2f, y + thickness / 2f,
                    w - thickness, h - thickness,
                    radius, radius
            );
            GradientPaint gp = new GradientPaint(
                    x, y, new Color(0x9C27B0),
                    x + w, y + h, new Color(0x002FA7)
            );
            g2.setPaint(gp);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(outer);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(thickness, thickness, thickness, thickness);
            return insets;
        }
    }



    private void styleTextField(JTextComponent tf, String placeholder) {
        tf.setOpaque(false); // ❗不填背景，让我们自己画圆角
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        tf.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        tf.setPreferredSize(new Dimension(240, 40));

        tf.setText(placeholder);
        tf.setForeground(Color.GRAY);

        // 👇 可选：标记 placeholder 状态（避免误清空）
        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(Color.DARK_GRAY);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (tf.getText().trim().isEmpty()) {
                    tf.setText(placeholder);
                    tf.setForeground(Color.GRAY);
                }
            }
        });
    }


    /**
     * 通用圆角边框
     **/
    static class RoundBorder implements Border {
        private int radius;
        private Color color;

        public RoundBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D round = new RoundRectangle2D.Float(
                    x + 1, y + 1, w - 2, h - 2, radius, radius
            );
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2));
            g2.draw(round);
            g2.dispose();
        }
    }


    /**
     * 在 UI 中弹出自定义对话框
     * @param message 弹窗内容
     * @param title    弹窗标题
     * @param messageType JOptionPane 类型（JOptionPane.ERROR_MESSAGE / INFORMATION_MESSAGE / WARNING_MESSAGE）
     */
    public void showCustomMessage(String message, String title, int messageType) {
        // 使用 modal JDialog 而非 JOptionPane.showMessageDialog，样式更统一
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(new Color(230, 230, 250)); // 背景色

        JLabel msgLabel = new JLabel("<html><center>" + message + "</center></html>", SwingConstants.CENTER);
        msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        // 根据类型设置颜色
        if (messageType == JOptionPane.ERROR_MESSAGE) {
            msgLabel.setForeground(Color.RED);
        } else if (messageType == JOptionPane.WARNING_MESSAGE) {
            msgLabel.setForeground(Color.ORANGE);
        } else {
            msgLabel.setForeground(new Color(50, 50, 50));
        }
        dialog.add(msgLabel, BorderLayout.CENTER);

        JButton ok = new JButton("OK");
        ok.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ok.setBackground(new Color(147,112,219));
        ok.setForeground(Color.WHITE);
        ok.setFocusPainted(false);
        ok.addActionListener(e -> dialog.dispose());
        JPanel btnP = new JPanel();
        btnP.setBackground(new Color(230, 230, 250));
        btnP.add(ok);
        dialog.add(btnP, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
    public void switchToLoginPanel() {
        // 确保 cardLayout、contentPanel 已经初始化
        if (this.cardLayout != null && this.contentPanel != null) {
            cardLayout.show(contentPanel, "Login");
        }
    }
    /**
     * 由 Controller 登录/注册成功后，关闭当前窗口
     */
    public void closeWindow() {
        SwingUtilities.invokeLater(this::dispose);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new AccountManagementUI(new AccountManagementController())
        );
    }
}
