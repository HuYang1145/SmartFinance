package View;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import AdminController.AdminController;
import AdminModel.AccountRepositoryModel;
import AdminModel.AccountService;
import PersonModel.UserSessionModel;

public class AdminDashboard {
    private JPanel sidebarPanel;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private Dimension btnSize = new Dimension(200, 40);
    private final AdminPlane adminPlane;

    public AdminDashboard(AccountService accountService, UserSessionModel userSessionModel) {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        adminPlane = new AdminPlane(accountService); // 修复构造器调用
        initSidebar();
    }

    private void initSidebar() {
        sidebarPanel.add(createSidebarButton("Administrator's Home Page", btnSize, () -> cardLayout.show(contentPanel, "dashboard")));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Administrator Information", btnSize, () -> new AdminSelfInfo().setVisible(true)));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Modify Customer Information", btnSize, () -> adminPlane.displayAdminVerificationForm()));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Customer Information Inquiry", btnSize, () -> {
            AdminController controller = new AdminController(new AccountRepositoryModel());
            controller.initialize();
        }));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Delete Customer Information", btnSize, this::showUserList));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Import Customer Accounts", btnSize, this::importCustomerAccounts));
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(createSidebarButton("Import Transaction Records", btnSize, this::importTransactionRecords));
    }

    private JButton createSidebarButton(String text, Dimension size, Runnable action) {
        JButton button = new JButton(text);
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.addActionListener(e -> action.run());
        return button;
    }

    private void showUserList() { /* 占位实现 */ }
    private void importCustomerAccounts() { /* 占位实现 */ }
    private void importTransactionRecords() { /* 占位实现 */ }

    private static class AdminSelfInfo extends JFrame {
        public AdminSelfInfo() {
            setTitle("Admin Information");
            setSize(400, 300);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLocationRelativeTo(null);
        }
    }
}