package UI;

import javax.swing.*;
import java.awt.*;

public class ContentPanelManager {
    private JPanel contentPanel;
    public ContentPanelManager(PersonalUI parent, JPanel contentPanel, CardLayout cardLayout) {
        this.contentPanel = contentPanel;
    }
    
    public void initializePanels() {
        // 个人中心面板
        JPanel individualCenterPanel = createIndividualCenterPanel();
        contentPanel.add(individualCenterPanel, "individualCenter");
        
    }
    
    private JPanel createIndividualCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(245, 245, 245));
        
        JLabel centerTitle = new JLabel("Welcome to Your Account Center");
        centerTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        centerTitle.setForeground(new Color(30, 60, 120));
        panel.add(centerTitle, BorderLayout.NORTH);
        
        JLabel centerPlaceholder = new JLabel("<html><i>Account overview, quick links, or charts can be displayed here.</i></html>");
        centerPlaceholder.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        centerPlaceholder.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(centerPlaceholder, BorderLayout.CENTER);
        
        return panel;
    }
}