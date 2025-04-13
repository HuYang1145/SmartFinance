package UI;

import Person.DepositDialog;
import Person.ViewPersonalInfo;
import Person.ViewBalanceDialog;
import Person.transferAccounts;
import Person.WithdrawalDialog;
import Person.TransactionHistoryDialog;
import model.UserSession;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;


public class PersonalUI extends JDialog {
    private CardLayout cardLayout;
    private JPanel contentPanel;

    public PersonalUI() {
        setTitle("Personal Account Center");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        // 整体使用 BorderLayout
        setLayout(new BorderLayout());

        
        // ============== 左侧侧边栏 ==============
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setPreferredSize(new Dimension(200, 0));
        // 设置侧边栏背景为深蓝色
        sidebarPanel.setBackground(new Color(30, 60, 120));

        Dimension btnSize = new Dimension(150, 30);

        // 创建侧边栏按钮（顺序：主页、查看余额、转入资金、转账、提款、交易记录、AI交流区、退出登录）
        JButton homeButton = createFunctionButton("Home", "/UI/icons/home.png", btnSize);
        JButton viewBalanceButton = createFunctionButton("Balance", "/UI/icons/balance.png", btnSize);
        JButton depositButton = createFunctionButton("Transfers in", "/UI/icons/transfer.png", btnSize);
        JButton transferButton = createFunctionButton("transfer", "/UI/icons/transfer_out.png", btnSize);
        JButton withdrawalButton = createFunctionButton("ATM", "/UI/icons/atm.png", btnSize);
        JButton transactionHistoryButton = createFunctionButton("Transaction records", "/UI/icons/history.png", btnSize);
        JButton aiChatButton = createFunctionButton("AI Communication", "/UI/icons/ai.png", btnSize);
        JButton logoutButton = createFunctionButton("Log out", "/UI/icons/logout.png", btnSize);
      
        JButton expenseCategoryButton = createFunctionButton("Expense Categories", "/UI/icons/expense.png", btnSize); // 新按钮

        // 添加按钮，使用垂直间隔，居中排列
        sidebarPanel.add(Box.createVerticalGlue());
        sidebarPanel.add(homeButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(viewBalanceButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(depositButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(transferButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(withdrawalButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(transactionHistoryButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(expenseCategoryButton); // 添加到侧边栏
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(aiChatButton);
        sidebarPanel.add(Box.createVerticalGlue());
        
        sidebarPanel.add(logoutButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        

        // ============== 右侧内容区域（使用CardLayout） ==============
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        // 设置右侧内容区背景为深蓝色
        contentPanel.setBackground(new Color(30, 60, 120));

        // 主页面板：初始页面，显示一些概览信息（你可以根据需要替换内容）
        JPanel mainDashboardPanel = new JPanel(null);
        mainDashboardPanel.setBackground(new Color(30, 60, 120));
        JLabel dashboardLabel = new JLabel("Overview of individual accounts");
        dashboardLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        dashboardLabel.setForeground(Color.WHITE);
        dashboardLabel.setBounds(30, 30, 200, 40);
        mainDashboardPanel.add(dashboardLabel);
        // 可添加图表、数据等占位元素
        JLabel placeholder = new JLabel("[Display the contents of the homepage here]");
        placeholder.setForeground(Color.WHITE);
        placeholder.setBounds(30, 100, 300, 30);
        mainDashboardPanel.add(placeholder);

 // 支出分类饼状图面板
 JPanel expenseCategoryPanel = new JPanel(new BorderLayout());
 expenseCategoryPanel.setBackground(new Color(30, 60, 120));

 DefaultPieDataset dataset = new DefaultPieDataset();
 dataset.setValue("Repast", 150.0);
 dataset.setValue("Entertainment", 80.0);
 dataset.setValue("Shopping", 120.0);
 dataset.setValue("Others", 50.0);

 JFreeChart pieChart = ChartFactory.createPieChart(
         "Expense Categories",  
         dataset,               
         true,                  
         true,                  
         false                  
 );

 ChartPanel chartPanel = new ChartPanel(pieChart);
 expenseCategoryPanel.add(chartPanel, BorderLayout.CENTER);
 contentPanel.add(expenseCategoryPanel, "expenseCategory");

 // 按钮事件监听
 expenseCategoryButton.addActionListener(e -> cardLayout.show(contentPanel, "expenseCategory"));

        // 查看余额面板
        JPanel viewBalancePanel = new JPanel(null);
        viewBalancePanel.setBackground(new Color(30, 60, 120));
        JLabel balanceLabel = new JLabel("View Balance Ribbon");
        balanceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        balanceLabel.setForeground(Color.WHITE);
        balanceLabel.setBounds(30, 30, 200, 30);
        viewBalancePanel.add(balanceLabel);

        // 转入资金面板
        JPanel depositPanel = new JPanel(null);
        depositPanel.setBackground(new Color(30, 60, 120));
        JLabel depositLabel = new JLabel("Transfer to funds functional area");
        depositLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        depositLabel.setForeground(Color.WHITE);
        depositLabel.setBounds(30, 30, 200, 30);
        depositPanel.add(depositLabel);

        // 转账面板
        JPanel transferPanel = new JPanel(null);
        transferPanel.setBackground(new Color(30, 60, 120));
        JLabel transferLabel = new JLabel("Transfer Function Area");
        transferLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        transferLabel.setForeground(Color.WHITE);
        transferLabel.setBounds(30, 30, 200, 30);
        transferPanel.add(transferLabel);

        // 提款面板
        JPanel withdrawalPanel = new JPanel(null);
        withdrawalPanel.setBackground(new Color(30, 60, 120));
        JLabel withdrawalLabel = new JLabel("cash withdrawal functional area");
        withdrawalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        withdrawalLabel.setForeground(Color.WHITE);
        withdrawalLabel.setBounds(30, 30, 200, 30);
        withdrawalPanel.add(withdrawalLabel);

        // 交易记录面板
        JPanel transactionHistoryPanel = new JPanel(null);
        transactionHistoryPanel.setBackground(new Color(30, 60, 120));
        JLabel historyLabel = new JLabel("Transaction Record Function Area");
        historyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        historyLabel.setForeground(Color.WHITE);
        historyLabel.setBounds(30, 30, 200, 30);
        transactionHistoryPanel.add(historyLabel);

        // AI交流区面板
        JPanel aiPanel = new JPanel(new BorderLayout());
        aiPanel.setBackground(new Color(30, 60, 120));
        JTextArea aiChatArea = new JTextArea();
        aiChatArea.setEditable(false);
        aiChatArea.setLineWrap(true);
        aiChatArea.setForeground(Color.WHITE);
        aiChatArea.setBackground(new Color(30, 60, 120));
        JScrollPane aiScrollPane = new JScrollPane(aiChatArea);
        aiPanel.add(aiScrollPane, BorderLayout.CENTER);
        JPanel aiInputPanel = new JPanel(new BorderLayout());
        JTextField aiInputField = new JTextField();
        JButton sendButton = new JButton("发送");
        sendButton.setPreferredSize(new Dimension(80, 30));
        aiInputPanel.add(aiInputField, BorderLayout.CENTER);
        aiInputPanel.add(sendButton, BorderLayout.EAST);
        aiPanel.add(aiInputPanel, BorderLayout.SOUTH);
        // 简单模拟AI回复
        sendButton.addActionListener(e -> {
            String text = aiInputField.getText().trim();
            if (!text.isEmpty()) {
                aiChatArea.append("我: " + text + "\n");
                aiChatArea.append("AI: " + "这是AI的回答\n");
                aiInputField.setText("");
            }
        });

        // 将各面板添加到内容区CardLayout中
        contentPanel.add(mainDashboardPanel, "home");
        contentPanel.add(viewBalancePanel, "viewBalance");
        contentPanel.add(depositPanel, "deposit");
        contentPanel.add(transferPanel, "transfer");
        contentPanel.add(withdrawalPanel, "withdrawal");
        contentPanel.add(transactionHistoryPanel, "transactionHistory");
        contentPanel.add(aiPanel, "aiChat");

        // 默认显示主页
        cardLayout.show(contentPanel, "home");

        // ============== 底部区域（单独放置“查看个人信息”按钮） ==============
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(20, 40, 80));
        JButton viewPersonalInfoButton = createFunctionButton("View Personal Information", "/UI/icons/person.png", btnSize);
        bottomPanel.add(viewPersonalInfoButton);

        // ============== 添加整体布局 ==============
        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // ============== 按钮事件处理 ==============
        homeButton.addActionListener(e -> cardLayout.show(contentPanel, "home"));
        viewBalanceButton.addActionListener(e -> {
            if (UserSession.getCurrentUsername() != null) {
                cardLayout.show(contentPanel, "viewBalance");
            } else {
                JOptionPane.showMessageDialog(this, "请先登录", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        depositButton.addActionListener(e -> {
            if (UserSession.getCurrentUsername() != null) {
                cardLayout.show(contentPanel, "deposit");
            } else {
                JOptionPane.showMessageDialog(this, "请先登录", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        transferButton.addActionListener(e -> {
            if (UserSession.getCurrentUsername() != null) {
                cardLayout.show(contentPanel, "transfer");
            } else {
                JOptionPane.showMessageDialog(this, "请先登录", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        withdrawalButton.addActionListener(e -> {
            if (UserSession.getCurrentUsername() != null) {
                cardLayout.show(contentPanel, "withdrawal");
            } else {
                JOptionPane.showMessageDialog(this, "请先登录", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        transactionHistoryButton.addActionListener(e -> {
            if (UserSession.getCurrentUsername() != null) {
                cardLayout.show(contentPanel, "transactionHistory");
            } else {
                JOptionPane.showMessageDialog(this, "请先登录", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        aiChatButton.addActionListener(e -> cardLayout.show(contentPanel, "aiChat"));
        logoutButton.addActionListener(e -> dispose());
        viewPersonalInfoButton.addActionListener(e -> {
            if (UserSession.getCurrentUsername() != null) {
                new ViewPersonalInfo(null, UserSession.getCurrentUsername()).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "请先登录", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        setVisible(true);
    }

    /**
     * 创建一个带图标的小按钮，文本居中
     */
    private JButton createFunctionButton(String text, String iconPath, Dimension size) {
        ImageIcon icon = null;
        try {
            icon = new ImageIcon(getClass().getResource(iconPath));
        } catch (Exception e) {
            // 图标加载失败时不设置图标
        }
        try {
            icon = new ImageIcon(getClass().getResource(iconPath));
            // 假设想缩放到 20x20 像素
            Image img = icon.getImage();
            Image scaled = img.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            icon = new ImageIcon(scaled);
        } catch (Exception e) {
            // 如果图标加载失败，继续
        }
        JButton button = new JButton(text, icon);
        // 让文字与图标横向排列，且图标在左边
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(10);

        // 如果你要固定大小，可以调大点
        if (size != null) {
            button.setPreferredSize(size);
            // button.setMaximumSize(size); // <-- 视情况决定是否保留
        }
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // 设置按钮背景为深蓝色，文字为白色
        button.setBackground(new Color(20, 40, 80));
        button.setForeground(Color.WHITE);
        // 设置无边框样式
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);

        return button;
    }

}
