package View.Bill;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * 用于添加支出记录的对话框视图，提供金额、时间、商户、类型和密码的输入字段。
 */
public class ExpenseDialogView extends JDialog {
    private JTextField amountField;
    private JTextField timeField;
    private JTextField merchantField;
    private JComboBox<String> typeComboBox;
    private JPasswordField passwordField;
    private JButton confirmButton;
    private JButton cancelButton;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    private static final String[] EXPENSE_TYPES = {
        "(选择类型)", "食品", "购物", "交通", "娱乐",
        "教育", "转账", "其他"
    };

    /**
     * 构造支出对话框视图。
     *
     * @param owner 父对话框。
     */
    public ExpenseDialogView(Dialog owner) {
        super(owner, "添加支出", true);
        initComponents();
        layoutComponents();
        pack();
        setMinimumSize(new Dimension(450, 350));
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        amountField = new JTextField(15);
        timeField = new JTextField(DATE_FORMAT.format(new Date()), 15);
        merchantField = new JTextField(15);
        typeComboBox = new JComboBox<>(EXPENSE_TYPES);
        passwordField = new JPasswordField(15);
        confirmButton = new JButton("确认支出");
        cancelButton = new JButton("取消");

        // 设置按钮样式
        confirmButton.setBackground(new Color(220, 53, 69)); // 红色用于支出
        confirmButton.setForeground(Color.WHITE);
        cancelButton.setBackground(new Color(200, 200, 200));
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 245, 245));

        // 标题
        JLabel titleLabel = new JLabel("添加支出记录");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(titleLabel, BorderLayout.NORTH);

        // 表单面板
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // 金额
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        panel.add(new JLabel("金额 (¥):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(amountField, gbc);

        // 时间
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("时间 (yyyy/MM/dd HH:mm):"), gbc);
        gbc.gridx = 1;
        panel.add(timeField, gbc);

        // 商户
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("商户/收款人:"), gbc);
        gbc.gridx = 1;
        panel.add(merchantField, gbc);

        // 类型
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("类型:"), gbc);
        gbc.gridx = 1;
        panel.add(typeComboBox, gbc);

        // 密码
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("密码:"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        add(panel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // UI 组件的 getter 方法
    public JTextField getAmountField() {
        return amountField;
    }

    public JTextField getTimeField() {
        return timeField;
    }

    public JTextField getMerchantField() {
        return merchantField;
    }

    public JComboBox<String> getTypeComboBox() {
        return typeComboBox;
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }

    public JButton getConfirmButton() {
        return confirmButton;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    /**
     * 显示错误消息对话框。
     *
     * @param message 要显示的错误消息。
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * 显示成功消息对话框。
     *
     * @param message 要显示的成功消息。
     */
    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "成功", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 清空密码字段。
     */
    public void clearPassword() {
        passwordField.setText("");
    }
}