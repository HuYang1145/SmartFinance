package View.LoginAndMain;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;

import Controller.LoginController;
import View.LoginAndMain.LoginComponents.GradientTextButton;
import View.LoginAndMain.LoginComponents.LoginGradientPanel;
import View.LoginAndMain.LoginComponents.RoundBorder;
import View.LoginAndMain.LoginComponents.RoundedComboBox;
import View.LoginAndMain.LoginComponents.RoundedPasswordField;
import View.LoginAndMain.LoginComponents.RoundedTextField;
import View.LoginAndMain.NavItemPanel.GradientLabel;

public class Login extends JFrame {
    private JPanel sidebar;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private LoginController controller;
    private List<NavItemPanel> navItems = new ArrayList<>();

    public Login(LoginController controller) {
        this.controller = controller;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Smart Finance - Welcome");
        setSize(750, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(230, 230, 250));
        add(mainPanel);

        // 初始化 cardLayout 和 contentPanel
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(new Color(230, 240, 250));

        // 创建侧边栏
        sidebar = createSidebar(false);
        mainPanel.add(sidebar, BorderLayout.WEST);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // 初始化内容面板
        initializeContentPanels();
        setVisible(true);
    }

    private JPanel createSidebar(boolean isLoggedIn) {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(Color.WHITE);
        sb.setPreferredSize(new Dimension(200, 0));

        GradientLabel logo = new GradientLabel("Smart Finance");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setBorder(new EmptyBorder(20, 0, 20, 0));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        sb.add(logo);

        // 根据登录状态选择侧边栏选项
        String[] options = isLoggedIn
                ? new String[]{"Personal Main", "Account Management"}
                : new String[]{"Welcome", "Login", "Register"};

        // 清空 navItems 避免重复
        navItems.clear();

        for (String opt : options) {
            NavItemPanel item = new NavItemPanel(opt, navItems, cardLayout, contentPanel);
            navItems.add(item);
            sb.add(item);
            sb.add(Box.createRigidArea(new Dimension(0, 8)));
            System.out.println("Added Login NavItem: " + opt + ", bounds: " + item.getBounds());
        }

        // 默认选中第一个选项
        if (!navItems.isEmpty()) {
            navItems.get(0).setSelected(true);
        }

        return sb;
    }

    private void initializeContentPanels() {
        // 确保卡片名称与侧边栏选项一致
        contentPanel.add(wrapInScroll(createWelcomePanel()), "Welcome");
        contentPanel.add(wrapInScroll(createLoginPanel()), "Login");
        contentPanel.add(wrapInScroll(createRegisterPanel()), "Register");
        contentPanel.add(wrapInScroll(createPersonalMain()), "Personal Main");
        contentPanel.add(wrapInScroll(createAccountManagement()), "Account Management");

        // 默认显示 Welcome 界面
        cardLayout.show(contentPanel, "Welcome");
    }

    private JPanel createWelcomePanel() {
        JPanel p = new LoginGradientPanel();
        p.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 60, 20, 60);

        JLabel title = new JLabel("User Account", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        gbc.gridy = 0;
        gbc.ipady = 10;
        p.add(title, gbc);

        GradientTextButton bLogin = new GradientTextButton("Log in");
        bLogin.setPreferredSize(new Dimension(0, 40));
        bLogin.setBackground(Color.WHITE);
        bLogin.setBorderPainted(false);
        bLogin.setFocusPainted(false);
        bLogin.setContentAreaFilled(true);
        bLogin.setOpaque(true);
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
        bLogin.addActionListener(e -> {
            cardLayout.show(contentPanel, "Login");
            // 更新选中状态
            navItems.forEach(item -> item.setSelected(item.getName().equals("Login")));
        });
        gbc.gridy = 1;
        p.add(bLogin, gbc);

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
        bReg.addActionListener(e -> {
            cardLayout.show(contentPanel, "Register");
            // 更新选中状态
            navItems.forEach(item -> item.setSelected(item.getName().equals("Register")));
        });
        gbc.gridy = 2;
        p.add(bReg, gbc);

        return p;
    }

    private JPanel createLoginPanel() {
        JPanel p = new LoginGradientPanel();
        p.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(12, 0, 12, 0);

        JLabel title = new JLabel("Finance Management System", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        gbc.gridy = 0;
        p.add(title, gbc);

        RoundedTextField userField = new RoundedTextField("Username");
        gbc.gridy = 1;
        p.add(userField, gbc);

        RoundedPasswordField passField = new RoundedPasswordField("Password");
        gbc.gridy = 2;
        p.add(passField, gbc);

        JButton btn = new JButton("Log in");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0, 47, 167));
        btn.setFocusPainted(false);
        btn.setBorder(new RoundBorder(30, new Color(0, 47, 167)));
        btn.setPreferredSize(new Dimension(240, 50));
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
                        userField.getActualText(),
                        new String(passField.getActualPassword()),
                        Login.this
                )
        );
        gbc.gridy = 3;
        p.add(btn, gbc);

        return p;
    }

    private JPanel createRegisterPanel() {
        JPanel p = new LoginGradientPanel();
        p.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(12, 0, 12, 0);

        JLabel title = new JLabel("Register New Account", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        gbc.gridy = 0;
        p.add(title, gbc);

        RoundedTextField usernameField = new RoundedTextField("Username");
        gbc.gridy = 1;
        p.add(usernameField, gbc);

        RoundedPasswordField passwordField = new RoundedPasswordField("Password");
        gbc.gridy = 2;
        p.add(passwordField, gbc);

        RoundedTextField phoneField = new RoundedTextField("Phone");
        gbc.gridy = 3;
        p.add(phoneField, gbc);

        RoundedTextField emailField = new RoundedTextField("Email");
        gbc.gridy = 4;
        p.add(emailField, gbc);

        RoundedComboBox<String> genderBox = new RoundedComboBox<>(new String[]{"Male", "Female"});
        gbc.gridy = 5;
        p.add(genderBox, gbc);

        RoundedTextField addressField = new RoundedTextField("Address");
        gbc.gridy = 6;
        p.add(addressField, gbc);

        RoundedComboBox<String> acctTypeBox = new RoundedComboBox<>(new String[]{"Personal", "Admin"});
        gbc.gridy = 7;
        p.add(acctTypeBox, gbc);

        JButton registerBtn = new JButton("Register");
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setBackground(new Color(0, 47, 167));
        registerBtn.setFocusPainted(false);
        registerBtn.setBorder(new RoundBorder(30, new Color(0, 47, 167)));
        registerBtn.setPreferredSize(new Dimension(240, 50));
        registerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                registerBtn.setBackground(new Color(30, 70, 200));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                registerBtn.setBackground(new Color(0, 47, 167));
            }
        });
        registerBtn.addActionListener(e -> {
            controller.handleRegister(
                    usernameField.getActualText(),
                    new String(passwordField.getActualPassword()),
                    phoneField.getActualText(),
                    emailField.getActualText(),
                    genderBox.getSelectedItem().toString(),
                    addressField.getActualText(),
                    acctTypeBox.getSelectedItem().toString(),
                    Login.this
            );
        });
        gbc.gridy = 8;
        p.add(registerBtn, gbc);

        return p;
    }

    private JPanel createPersonalMain() {
        JPanel p = new JPanel();
        p.setBackground(new Color(230, 240, 250));
        p.add(new JLabel("Welcome to Personal Main"));
        return p;
    }

    private JPanel createAccountManagement() {
        JPanel p = new JPanel();
        p.setBackground(new Color(230, 240, 250));
        p.add(new JLabel("Account Management - Personal Center"));
        return p;
    }

    private JScrollPane wrapInScroll(JPanel panel) {
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getViewport().setBackground(panel.getBackground());

        JScrollBar bar = scroll.getVerticalScrollBar();
        bar.setUnitIncrement(16);
        bar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(156, 39, 176);
                this.trackColor = new Color(40, 100, 250);
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(0, 0));
                btn.setMinimumSize(new Dimension(0, 0));
                btn.setMaximumSize(new Dimension(0, 0));
                return btn;
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(
                        thumbBounds.x,
                        thumbBounds.y,
                        thumbBounds.width,
                        thumbBounds.height,
                        10, 10
                );
                g2.dispose();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(255, 255, 255, 50));
                g2.fillRoundRect(
                        trackBounds.x,
                        trackBounds.y,
                        trackBounds.width,
                        trackBounds.height,
                        10, 10
                );
                g2.dispose();
            }
        });

        return scroll;
    }

    public void switchToLoginPanel() {
        if (cardLayout != null && contentPanel != null) {
            cardLayout.show(contentPanel, "Login");
            navItems.forEach(item -> item.setSelected(item.getName().equals("Login")));
        }
    }

    public void closeWindow() {
        SwingUtilities.invokeLater(this::dispose);
    }
}