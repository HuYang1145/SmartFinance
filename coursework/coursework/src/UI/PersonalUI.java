package UI;

import Model.UserSession;
import Person.IncomeExpenseChart;
import java.awt.*;
import javax.swing.*;

public class PersonalUI extends JDialog {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private SidebarPanel sidebarPanel;
    private ContentPanelManager contentManager;
    
    public PersonalUI() {
        setTitle("Personal Account Center");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // 创建侧边栏
        sidebarPanel = new SidebarPanel(this);
        add(sidebarPanel, BorderLayout.WEST);
        
        // 创建内容面板管理器
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(new Color(30, 60, 120));
        
        contentManager = new ContentPanelManager(this, contentPanel, cardLayout);
        contentManager.initializePanels();
        
        add(contentPanel, BorderLayout.CENTER);
        
        // 设置默认视图
        cardLayout.show(contentPanel, "individualCenter");
        
        setVisible(true);
    }

    // 新增方法：显示收入支出图表
    public void showIncomeExpenseChart() {
        System.out.println("showIncomeExpenseChart() 方法被调用了！");
        IncomeExpenseChart.showIncomeExpensePieChart("transactions.csv");
    }

    // 检查登录状态并显示卡片
    public void checkLoginAndShowCard(String cardName) {
        if (UserSession.getCurrentUsername() != null) {
            cardLayout.show(contentPanel, cardName);
        } else {
            UIUtils.showLoginError(this);
        }
    }
    
    // 检查登录状态并执行操作（通常是显示对话框）
    public void checkLoginAndShowDialog(Runnable dialogCreator) {
        if (UserSession.getCurrentUsername() != null) {
            SwingUtilities.invokeLater(dialogCreator);
        } else {
            UIUtils.showLoginError(this);
        }
    }
    
    // 处理登出操作
    public void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            UserSession.setCurrentUsername(null); // 清除会话数据
            dispose(); // 关闭此窗口
        }
    }

}