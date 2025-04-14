package Person; // 确保包声明是 Person

import model.AccountModel;
import model.UserRegistrationCSVExporter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

// 类名通常与文件名一致，确保文件名也是 ViewPersonalInfo.java
public class ViewPersonalInfo extends JDialog {

    /**
     * 构造函数，用于显示指定用户的个人信息。
     * @param owner    调用此对话框的父对话框 (现在是 Dialog 类型)
     * @param username 要显示信息的用户名称
     */
    public ViewPersonalInfo(Dialog owner, String username) { // *** 修改点：JFrame 改为 Dialog ***
        super(owner, "个人信息", true); // *** 修改点：使用 owner ***
        setSize(400, 300);
        setLocationRelativeTo(owner); // *** 修改点：相对于 owner 定位 ***
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        // 使用 BorderLayout 可能更灵活，但 GridLayout 也可以
        // 如果用 GridLayout，确保行数足够
        JPanel contentPanel = new JPanel(new GridBagLayout()); // 使用 GridBagLayout 更灵活控制
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // 组件间距
        gbc.anchor = GridBagConstraints.WEST; // 左对齐

        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        AccountModel userAccount = null;

        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username)) {
                userAccount = account;
                break;
            }
        }

        if (userAccount != null) {
            int gridy = 0;

            // 添加标签和信息
            addLabelAndValue(contentPanel, gbc, "用户名:", userAccount.getUsername(), gridy++);
            addLabelAndValue(contentPanel, gbc, "手机号:", userAccount.getPhone(), gridy++);
            addLabelAndValue(contentPanel, gbc, "邮箱:", userAccount.getEmail(), gridy++);
            addLabelAndValue(contentPanel, gbc, "性别:", userAccount.getGender(), gridy++);
            addLabelAndValue(contentPanel, gbc, "地址:", userAccount.getAddress(), gridy++);
            addLabelAndValue(contentPanel, gbc, "创建时间:", userAccount.getCreationTime(), gridy++);
            addLabelAndValue(contentPanel, gbc, "账户状态:", userAccount.getAccountStatus(), gridy++);
            addLabelAndValue(contentPanel, gbc, "账户类型:", userAccount.getAccountType(), gridy++);


            // 添加返回按钮
            JButton returnButton = new JButton("返回");
            returnButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // setVisible(false); // dispose() 会处理可见性
                    // 使用 SwingUtilities.invokeLater 确保在事件调度线程中处理 Swing 组件
                    SwingUtilities.invokeLater(() -> {
                        dispose(); // 关闭并释放对话框资源
                    });
                }
            });

            gbc.gridx = 0;
            gbc.gridy = gridy;
            gbc.gridwidth = 2; // 按钮跨越两列
            gbc.anchor = GridBagConstraints.CENTER; // 按钮居中
            contentPanel.add(returnButton, gbc);

            // 将内容面板添加到 JDialog
            setContentPane(contentPanel); // 设置 JDialog 的内容面板

        } else {
            // 确保在事件调度线程中显示对话框
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "找不到用户信息", "错误", JOptionPane.ERROR_MESSAGE);
                dispose(); // 找不到信息也关闭对话框
            });
            // 因为 showMessageDialog 是模态的，后面的 setVisible(true) 可能不会立即执行
            // 如果上面直接 dispose，这里的 setVisible 就不需要了，并且应该在构造函数最后调用
            return; // 提前返回，避免执行下面的 setVisible(true)
        }

        // pack(); // 可以用 pack() 代替 setSize() 来自动调整大小，如果布局管理器设置得当
        // setVisible(true); // 将 setVisible 移到构造函数末尾，确保所有组件都添加完毕
    }

    // 辅助方法添加标签和值到面板
    private void addLabelAndValue(JPanel panel, GridBagConstraints gbc, String labelText, String valueText, int gridy) {
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST; // 标签右对齐
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.gridy = gridy;
        gbc.anchor = GridBagConstraints.WEST; // 值左对齐
        // 对于可能很长的值，可以考虑使用 JTextArea 或限制 JLabel 宽度
        JLabel valueLabel = new JLabel(valueText != null ? valueText : ""); // 处理 null 值
        panel.add(valueLabel, gbc);
    }


    public static void main(String[] args) {
        // 用于测试 ViewPersonalInfo
        // 因为构造函数现在需要 Dialog，我们不能直接创建 JFrame 传递。
        // 可以传递 null，或者创建一个临时的 JDialog 作为 owner。
        // 这里我们传递 null 进行简单测试。
        SwingUtilities.invokeLater(() -> {
            // 创建一个临时的父 Dialog (或 Frame，如果需要测试 Frame owner 的情况，需要重载构造函数)
            JDialog testOwnerDialog = new JDialog();
            testOwnerDialog.setTitle("Test Owner");
            testOwnerDialog.setSize(100, 100);
            testOwnerDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            // testOwnerDialog.setVisible(true); // 可选：显示测试父窗口

            // String testUsername = "admin"; // 使用一个实际存在的测试用户名
            String testUsername = "testuser"; // 使用一个实际存在的测试用户名
            if (testUsername == null) {
                System.err.println("Please provide a valid test username in the main method.");
                return;
            }

            System.out.println("Attempting to view info for: " + testUsername);
            ViewPersonalInfo dialog = new ViewPersonalInfo(testOwnerDialog, testUsername); // 传递 testOwnerDialog
            // ViewPersonalInfo dialog = new ViewPersonalInfo(null, testUsername); // 或者传递 null
            dialog.setVisible(true); // 在构造函数外调用 setVisible

            // 如果 ViewPersonalInfo 在构造函数中因为找不到用户而 dispose 了，
            // 父窗口可能仍然存在。可以添加逻辑在 ViewPersonalInfo 关闭后也关闭父窗口。
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    // testOwnerDialog.dispose(); // 可选：关闭测试父窗口
                    System.exit(0); // 退出测试程序
                }
            });


            // 如果 ViewPersonalInfo 正常显示，程序会等待它关闭
            // 如果 ViewPersonalInfo 内部调用 dispose(), 程序可能会继续执行然后退出
            // 如果父窗口也可见且未关闭，程序不会退出，需要用户手动关闭或添加退出逻辑
            // System.out.println("Dialog closed or user not found.");

        });

    }
}