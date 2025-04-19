package UI;

import javax.swing.*;
import java.awt.*;

public class ContentPanelManager {
    private PersonalUI parentUI;
    private JPanel contentPanel;
    private CardLayout cardLayout; // Keep CardLayout reference if needed

    // --- Add references to the new panels ---
    private ReportOptionsPanel reportOptionsPanel;
    private ReportViewPanel reportViewPanel;
    private ReportCycleSettingsPanel reportCycleSettingsPanel;


    public ContentPanelManager(PersonalUI parent, JPanel contentPanel, CardLayout cardLayout) {
        this.parentUI = parent;
        this.contentPanel = contentPanel;
        this.cardLayout = cardLayout; // Store cardLayout

        // --- Instantiate panels here ---
        this.reportOptionsPanel = new ReportOptionsPanel(parent);
        this.reportViewPanel = new ReportViewPanel(parent);
        this.reportCycleSettingsPanel = new ReportCycleSettingsPanel(parent);
    }

    public void initializePanels() {
        // 个人中心面板
        JPanel individualCenterPanel = createIndividualCenterPanel();
        contentPanel.add(individualCenterPanel, "individualCenter");

        // --- Add the new report panels ---
        contentPanel.add(reportOptionsPanel, "reportOptions");
        contentPanel.add(reportViewPanel, "reportView");
        contentPanel.add(reportCycleSettingsPanel, "reportCycleSettings");

        // Other content panels (keep existing ones)
        // JPanel periodicReportPanel = createPlaceholderPanel("Periodic Financial Reporting Area"); // Remove this placeholder
        // contentPanel.add(periodicReportPanel, "periodicReport");

        JPanel spendingProportionPanel = createPlaceholderPanel("Monthly/Annual Spending Proportion Display Area");
        contentPanel.add(spendingProportionPanel, "spendingProportion");

        JPanel financialSuggestionPanel = new FinancialSuggestionPanel(parentUI);
        contentPanel.add(financialSuggestionPanel, "financialSuggestion");

        JPanel aiPanel = new AIPanel();
        contentPanel.add(aiPanel, "aiQA");
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

    private JPanel createPlaceholderPanel(String title) {
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(30, 60, 120));
        titleLabel.setBounds(20, 20, 600, 30);
        panel.add(titleLabel);

        JLabel placeholder = new JLabel("[ Content for " + title + " will be displayed here ]");
        placeholder.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        placeholder.setForeground(Color.GRAY);
        placeholder.setBounds(20, 70, 600, 30);
        panel.add(placeholder);

        return panel;
    }
}