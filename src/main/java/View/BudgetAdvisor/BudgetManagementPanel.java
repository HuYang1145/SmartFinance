package View.BudgetAdvisor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import Model.BudgetDataContainer;
import Service.BudgetService.BudgetRecommendation;

public class BudgetManagementPanel extends JPanel {
    private final Controller.BudgetManagementController controller;
    private JLabel budgetValueLabel, savingGoalValueLabel, modeValueLabel, reasonValueLabel;
    private JTextField customBudgetInputField;
    private JButton saveCustomBudgetButton, restoreIntelligentButton;
    private JTextArea largeConsumptionTextArea;
    private JLabel topSpendingCategoryLabel, expenditureValueLabel, budgetStatusLabel;
    private boolean isInitialized;
    private static final Color SECTION_BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Border SECTION_BORDER = BorderFactory.createLineBorder(new Color(220, 220, 220));
    private static final Color BUTTON_BACKGROUND_COLOR = Color.LIGHT_GRAY;
    private static final Color BUTTON_FOREGROUND_COLOR = Color.BLACK;
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);

    public BudgetManagementPanel(String username) {
        if (username == null || username.trim().isEmpty()) {
            setLayout(new BorderLayout());
            add(new JLabel("User not logged in.", SwingConstants.CENTER), BorderLayout.CENTER);
            this.controller = null;
            this.isInitialized = false;
            return;
        }
        this.controller = new Controller.BudgetManagementController(username, this, new Service.BudgetService(new Repository.TransactionRepository()));
        this.isInitialized = true;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(204, 229, 255));
        initComponents();
        layoutComponents();
        if (controller != null) {
            controller.loadBudgetData();
        }
    }

    private void initComponents() {
        budgetValueLabel = new JLabel("Loading...");
        savingGoalValueLabel = new JLabel("Loading...");
        modeValueLabel = new JLabel("Loading...");
        reasonValueLabel = new JLabel("Loading...");
        customBudgetInputField = new JTextField(10);
        saveCustomBudgetButton = new JButton("Save");
        restoreIntelligentButton = new JButton("Restore intelligent recommendation");
        largeConsumptionTextArea = new JTextArea(8, 20);
        largeConsumptionTextArea.setEditable(false);
        topSpendingCategoryLabel = new JLabel("N/A");
        expenditureValueLabel = new JLabel("Loading...");
        budgetStatusLabel = new JLabel("Loading...");

        Font infoFont = new Font("Segoe UI", Font.PLAIN, 14);
        budgetValueLabel.setFont(infoFont);
        savingGoalValueLabel.setFont(infoFont);
        modeValueLabel.setFont(infoFont);
        reasonValueLabel.setFont(infoFont);
        customBudgetInputField.setFont(infoFont);
        largeConsumptionTextArea.setFont(infoFont);
        topSpendingCategoryLabel.setFont(infoFont);
        expenditureValueLabel.setFont(infoFont);
        budgetStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        styleCustomBudgetButton(saveCustomBudgetButton);
        saveCustomBudgetButton.addActionListener(e -> {
            if (controller != null) {
                controller.saveCustomBudget(customBudgetInputField.getText());
            }
        });
        styleCustomBudgetButton(restoreIntelligentButton);
        restoreIntelligentButton.addActionListener(e -> {
            if (controller != null) {
                controller.clearCustomBudget();
            }
        });
    }

    private void styleCustomBudgetButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(BUTTON_FOREGROUND_COLOR);
        button.setBackground(BUTTON_BACKGROUND_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JPanel createFramedPanel(JComponent content) {
        JPanel framedPanel = new JPanel();
        framedPanel.setLayout(new BoxLayout(framedPanel, BoxLayout.Y_AXIS));
        framedPanel.setBackground(SECTION_BACKGROUND_COLOR);
        framedPanel.setBorder(new CompoundBorder(SECTION_BORDER, new EmptyBorder(10, 10, 10, 10)));
        framedPanel.add(content);
        framedPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return framedPanel;
    }

    private void layoutComponents() {
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                int width = getWidth(), height = getHeight();
                Color colorStart = new Color(173, 216, 230);
                Color colorEnd = new Color(138, 43, 226);
                GradientPaint gp = new GradientPaint(0, 0, colorStart, 0, height, colorEnd);
                g2.setPaint(gp);
                g2.fillRect(0, 0, width, height);
            }
        };
        backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.Y_AXIS));
        add(backgroundPanel);

        JLabel mainTitle = new JLabel("Budget Management & Advice", SwingConstants.CENTER);
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        mainTitle.setForeground(Color.WHITE);
        mainTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        backgroundPanel.add(Box.createVerticalStrut(20));
        backgroundPanel.add(mainTitle);
        backgroundPanel.add(Box.createVerticalStrut(20));

        JLabel goalsSubtitle = new JLabel("Monthly Saving Goal & Budget", SwingConstants.CENTER);
        goalsSubtitle.setFont(TITLE_FONT);
        goalsSubtitle.setForeground(Color.WHITE);
        backgroundPanel.add(goalsSubtitle);
        JPanel goalsPanelWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        goalsPanelWrapper.setOpaque(false);
        goalsPanelWrapper.add(createFramedPanel(createGoalsPanelContent()));
        backgroundPanel.add(goalsPanelWrapper);
        backgroundPanel.add(Box.createVerticalStrut(15));

        JLabel savingAdviceSubtitle = new JLabel("Saving Advice -- reducible consumption", SwingConstants.CENTER);
        savingAdviceSubtitle.setFont(TITLE_FONT);
        savingAdviceSubtitle.setForeground(Color.WHITE);
        backgroundPanel.add(savingAdviceSubtitle);
        JPanel savingAdvicePanelWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        savingAdvicePanelWrapper.setOpaque(false);
        savingAdvicePanelWrapper.add(createFramedPanel(createSavingAdvicePanelContent()));
        backgroundPanel.add(savingAdvicePanelWrapper);
        backgroundPanel.add(Box.createVerticalStrut(15));

        JLabel customBudgetSubtitle = new JLabel("Custom Budget", SwingConstants.CENTER);
        customBudgetSubtitle.setFont(TITLE_FONT);
        customBudgetSubtitle.setForeground(Color.WHITE);
        backgroundPanel.add(customBudgetSubtitle);
        JPanel customBudgetPanelWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        customBudgetPanelWrapper.setOpaque(false);
        customBudgetPanelWrapper.add(createFramedPanel(createCustomBudgetPanelContent()));
        backgroundPanel.add(customBudgetPanelWrapper);
        backgroundPanel.add(Box.createVerticalStrut(15));

        JLabel spendingStatusSubtitle = new JLabel("Spending status", SwingConstants.CENTER);
        spendingStatusSubtitle.setFont(TITLE_FONT);
        spendingStatusSubtitle.setForeground(Color.WHITE);
        backgroundPanel.add(spendingStatusSubtitle);
        JPanel spendingStatusPanelWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        spendingStatusPanelWrapper.setOpaque(false);
        spendingStatusPanelWrapper.add(createFramedPanel(createSpendingStatusPanelContent()));
        backgroundPanel.add(spendingStatusPanelWrapper);
        backgroundPanel.add(Box.createVerticalStrut(20));

        backgroundPanel.add(Box.createVerticalGlue());
    }

    private JPanel createGoalsPanelContent() {
        JPanel goalsPanelContent = new JPanel(new GridBagLayout());
        goalsPanelContent.setBackground(SECTION_BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0; goalsPanelContent.add(new JLabel("Budget:"), gbc);
        gbc.gridx = 1; goalsPanelContent.add(budgetValueLabel, gbc);
        gbc.gridx = 0; gbc.gridy++; goalsPanelContent.add(new JLabel("Saving Goal:"), gbc);
        gbc.gridx = 1; goalsPanelContent.add(savingGoalValueLabel, gbc);
        gbc.gridx = 0; gbc.gridy++; goalsPanelContent.add(new JLabel("Mode:"), gbc);
        gbc.gridx = 1; goalsPanelContent.add(modeValueLabel, gbc);
        gbc.gridx = 0; gbc.gridy++; goalsPanelContent.add(new JLabel("Reason:"), gbc);
        gbc.gridx = 1; goalsPanelContent.add(reasonValueLabel, gbc);

        return goalsPanelContent;
    }

    private JPanel createCustomBudgetPanelContent() {
        JPanel customBudgetPanelContent = new JPanel(new GridBagLayout());
        customBudgetPanelContent.setBackground(SECTION_BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0; customBudgetPanelContent.add(new JLabel("Expected budget amount: ¥"), gbc);
        gbc.gridx = 1; customBudgetPanelContent.add(customBudgetInputField, gbc);
        gbc.gridx = 2; customBudgetPanelContent.add(saveCustomBudgetButton, gbc);
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 3;
        customBudgetPanelContent.add(restoreIntelligentButton, gbc);

        return customBudgetPanelContent;
    }

    private JPanel createSavingAdvicePanelContent() {
        JPanel savingAdvicePanelContent = new JPanel(new GridBagLayout());
        savingAdvicePanelContent.setBackground(SECTION_BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0; savingAdvicePanelContent.add(new JLabel("Top spending this month:"), gbc);
        gbc.gridy++; savingAdvicePanelContent.add(topSpendingCategoryLabel, gbc);
        gbc.gridy++; savingAdvicePanelContent.add(new JLabel("Large consumption (Over 7% of income):"), gbc);
        gbc.gridy++; savingAdvicePanelContent.add(new JScrollPane(largeConsumptionTextArea), gbc);
        largeConsumptionTextArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        return savingAdvicePanelContent;
    }

    private JPanel createSpendingStatusPanelContent() {
        JPanel spendingStatusPanelContent = new JPanel(new GridBagLayout());
        spendingStatusPanelContent.setBackground(SECTION_BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0; spendingStatusPanelContent.add(new JLabel("This month's expenditure:"), gbc);
        gbc.gridx = 1; spendingStatusPanelContent.add(expenditureValueLabel, gbc);
        gbc.gridx = 0; gbc.gridy++; spendingStatusPanelContent.add(budgetStatusLabel, gbc);

        return spendingStatusPanelContent;
    }

    public void updateBudgetData(BudgetDataContainer data) {
        if (!isInitialized) return;

        BudgetRecommendation recommendation = data.getRecommendation();
        double currentMonthExpense = data.getCurrentMonthExpense();
        double currentMonthIncome = data.getCurrentMonthIncome();
        String topCategory = data.getTopCategory();
        List<String> largeConsumptions = data.getLargeConsumptions();
        Double customBudget = data.getCustomBudget();

        double budgetToUse = (customBudget != null) ? customBudget : recommendation.getSuggestedBudget();
        budgetValueLabel.setText(String.format("¥%.2f", budgetToUse));
        savingGoalValueLabel.setText(String.format("¥%.2f", currentMonthIncome - budgetToUse));
        modeValueLabel.setText(recommendation.getMode().getDisplayName());
        reasonValueLabel.setText(recommendation.getReason());
        customBudgetInputField.setText(customBudget != null ? String.format("%.2f", customBudget) : "");
        topSpendingCategoryLabel.setText(topCategory != null ? topCategory : "No expenses this month");
        largeConsumptionTextArea.setText(largeConsumptions.isEmpty() ? "No large consumptions found this month." : String.join("\n", largeConsumptions));
        expenditureValueLabel.setText(String.format("¥%.2f", currentMonthExpense));
        updateBudgetStatusLabel(budgetToUse - currentMonthExpense);

        revalidate();
        repaint();
    }

    public void setLoadingState(boolean isLoading) {
        if (!isInitialized) return;

        String text = isLoading ? "Loading..." : "";
        budgetValueLabel.setText(text);
        savingGoalValueLabel.setText(text);
        modeValueLabel.setText(text);
        reasonValueLabel.setText(text);
        topSpendingCategoryLabel.setText(isLoading ? "N/A" : topSpendingCategoryLabel.getText());
        expenditureValueLabel.setText(text);
        budgetStatusLabel.setText(text);
        largeConsumptionTextArea.setText(isLoading ? "Loading..." : largeConsumptionTextArea.getText());
        saveCustomBudgetButton.setEnabled(!isLoading);
        restoreIntelligentButton.setEnabled(!isLoading);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    private void updateBudgetStatusLabel(double remainingValue) {
        if (!isInitialized) return;

        if (remainingValue >= 0) {
            budgetStatusLabel.setText(String.format("Distance to the budget: ¥%.2f", remainingValue));
            budgetStatusLabel.setForeground(new Color(0, 128, 0)); // Green
        } else {
            budgetStatusLabel.setText(String.format("Overspent by: ¥%.2f", Math.abs(remainingValue)));
            budgetStatusLabel.setForeground(Color.RED); // Red
        }
    }
}