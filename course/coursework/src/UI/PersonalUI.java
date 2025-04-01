package UI;

import javax.swing.*;
import Person.DepositDialog;
import Person.ViewPersonalInfo; 
import Person.ViewBalanceDialog; 
import Person.transferAccounts; 
import Person.WithdrawalDialog; 
import Person.TransactionHistoryDialog;

import java.awt.*;
import java.awt.event.*;

public class PersonalUI extends JDialog {
    public PersonalUI() {
        setTitle("个人账户中心");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // 点击关闭按钮时关闭窗口

        // 创建按钮
        JButton viewBalanceButton = new JButton("查看余额");
        JButton viewPersonalInfoButton = new JButton("查看个人信息"); // 修改按钮文本
        JButton depositButton = new JButton("转入资金");
        JButton transferAccountsButton = new JButton("转账");
        JButton withdrawalButton = new JButton("提款"); 
        JButton viewTransactionHistoryButton = new JButton("查看交易记录"); 
        JButton logoutButton = new JButton("退出登录");

        // 添加按钮监听器
        viewBalanceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("PersonalUI: 查看余额按钮被点击");
                String loggedInUsername = model.UserSession.getCurrentUsername();
                if (loggedInUsername != null) {
                    ViewBalanceDialog balanceDialog = new ViewBalanceDialog(PersonalUI.this, loggedInUsername);
                    balanceDialog.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(PersonalUI.this, "请先登录", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        viewPersonalInfoButton.addActionListener(new ActionListener() { // 修改按钮监听器
            public void actionPerformed(ActionEvent e) {
                String loggedInUsername = model.UserSession.getCurrentUsername();
                if (loggedInUsername != null) {
                    // 使用 null 作为所有者参数，如果需要父窗口，请传入正确的 Frame 对象
                    ViewPersonalInfo infoDialog = new ViewPersonalInfo(null, loggedInUsername); // 使用 Person 包下的 ViewPersonalInfo
                    infoDialog.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(PersonalUI.this, "请先登录", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        depositButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String loggedInUsername = model.UserSession.getCurrentUsername();
                if (loggedInUsername != null) {
                    DepositDialog depositDialog = new DepositDialog(null, loggedInUsername);
                    depositDialog.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(PersonalUI.this, "请先登录", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        transferAccountsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String loggedInUsername = model.UserSession.getCurrentUsername();
                if (loggedInUsername != null) {
                    transferAccounts transferAccounts = new transferAccounts(null); // 注意这里的类名，如果你的转账功能在 WithdrawDialog 中，这里需要修改
                    transferAccounts.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(PersonalUI.this, "请先登录", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 新增提款按钮的监听器
        withdrawalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String loggedInUsername = model.UserSession.getCurrentUsername();
                if (loggedInUsername != null) {
                    WithdrawalDialog withdrawalDialog = new WithdrawalDialog(null, loggedInUsername);
                    withdrawalDialog.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(PersonalUI.this, "请先登录", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        viewTransactionHistoryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String loggedInUsername = model.UserSession.getCurrentUsername();
                if (loggedInUsername != null) {
                    TransactionHistoryDialog transactionHistoryDialog = new TransactionHistoryDialog(null);
                    transactionHistoryDialog.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(PersonalUI.this, "请先登录", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 退出登录操作
                dispose(); // 关闭当前窗口
            }
        });

        // 设置布局
        setLayout(new GridLayout(7, 1, 10, 10)); 
        add(viewBalanceButton);
        add(viewPersonalInfoButton); 
        add(depositButton);
        add(transferAccountsButton);
        add(withdrawalButton); 
        add(viewTransactionHistoryButton);
        add(logoutButton);

        setVisible(true);
    }

    public static void main(String[] args) {
        new PersonalUI();
    }
}