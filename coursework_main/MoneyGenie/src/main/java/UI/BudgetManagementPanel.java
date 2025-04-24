package UI;

// Necessary imports
import AccountModel.BudgetAdvisor;
import AccountModel.BudgetAdvisor.BudgetRecommendation;
import AccountModel.TransactionService;
import AccountModel.TransactionService.TransactionData;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetManagementPanel extends JPanel {

    private JLabel budgetValueLabel;
    private JLabel savingGoalValueLabel;
    private JLabel modeValueLabel;
    private JLabel reasonValueLabel;
    private JTextField customBudgetInputField;
    private JButton saveCustomBudgetButton;
    private JButton restoreIntelligentButton;
    private JTextArea largeConsumptionTextArea;
    private JLabel topSpendingCategoryLabel;
    private JLabel expenditureValueLabel;
    private JLabel budgetStatusLabel;
    private String username;

    private static final DateTimeFormatter TRANSACTION_TIME_FORMATTER = BudgetAdvisor.DATE_FORMATTER;
    private static final Color SECTION_BACKGROUND_COLOR = new Color(245, 245, 245); // Light gray
    private static final Border SECTION_BORDER = BorderFactory.createLineBorder(new Color(220, 220, 220)); // Light gray border
    private static final Color BUTTON_BACKGROUND_COLOR = Color.LIGHT_GRAY;
    private static final Color BUTTON_FOREGROUND_COLOR = Color.BLACK;
    private static final Color TITLE_FOREGROUND_COLOR = Color.WHITE;
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);

    public BudgetManagementPanel(String username) {
        this.username = username;
        if (this.username == null || this.username.trim().isEmpty()) {
            setLayout(new BorderLayout());
            add(new JLabel("User not logged in.", SwingConstants.CENTER), BorderLayout.CENTER);
            return;
        }
        initComponents();
        layoutComponents();
        loadBudgetData();
    }

    private void initComponents() {
        budgetValueLabel = new JLabel("Loading...");
        savingGoalValueLabel = new JLabel("Loading...");
        modeValueLabel = new JLabel("Loading...");
        reasonValueLabel = new JLabel("Loading...");
        customBudgetInputField = new JTextField(10);
        saveCustomBudgetButton = new JButton("Save");
        restoreIntelligentButton = new JButton("Restore intelligent recommendation");
        largeConsumptionTextArea = new JTextArea(8, 20); // Increased rows for better visibility
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
        saveCustomBudgetButton.addActionListener(e -> handleSaveCustomBudget());
        styleCustomBudgetButton(restoreIntelligentButton);
        restoreIntelligentButton.addActionListener(e -> handleRestoreIntelligent());
    }

    private JPanel createFramedPanel(JComponent content) {
        JPanel framedPanel = new JPanel();
        framedPanel.setLayout(new BoxLayout(framedPanel, BoxLayout.Y_AXIS));
        framedPanel.setBackground(SECTION_BACKGROUND_COLOR);
        framedPanel.setBorder(SECTION_BORDER);
        framedPanel.add(content);
        framedPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        framedPanel.setBorder(new CompoundBorder(framedPanel.getBorder(), new EmptyBorder(10, 10, 10, 10))); // Add padding
        return framedPanel;
    }

    private void layoutComponents() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(204, 229, 255));

        // Create a panel for the gradient background
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();
                Color colorStart = new Color(173, 216, 230); // Light Blue
                Color colorEnd = new Color(138, 43, 226);   // Blue Violet
                GradientPaint gp = new GradientPaint(0, 0, colorStart, 0, height, colorEnd);
                g2.setPaint(gp);
                g2.fillRect(0, 0, width, height);
            }
        };
        backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.Y_AXIS));
        add(backgroundPanel);

        // Main Title
        JLabel mainTitle = new JLabel("Budget Management & Advice", SwingConstants.CENTER);
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        mainTitle.setForeground(Color.WHITE);
        mainTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        backgroundPanel.add(Box.createVerticalStrut(20));
        backgroundPanel.add(mainTitle);
        backgroundPanel.add(Box.createVerticalStrut(20));

        // 1. Monthly Saving Goal & Budget
        JLabel goalsSubtitle = new JLabel("Monthly Saving Goal & Budget", SwingConstants.CENTER);
        goalsSubtitle.setFont(TITLE_FONT);
        goalsSubtitle.setForeground(TITLE_FOREGROUND_COLOR);
        goalsSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        backgroundPanel.add(goalsSubtitle);
        JPanel goalsPanelWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        goalsPanelWrapper.setOpaque(false);
        goalsPanelWrapper.add(createFramedPanel(createGoalsPanelContent()));
        goalsPanelWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        backgroundPanel.add(goalsPanelWrapper);
        backgroundPanel.add(Box.createVerticalStrut(15));

        // 2. Saving Advice -- reducible consumption
        JLabel savingAdviceSubtitle = new JLabel("Saving Advice -- reducible consumption", SwingConstants.CENTER);
        savingAdviceSubtitle.setFont(TITLE_FONT);
        savingAdviceSubtitle.setForeground(TITLE_FOREGROUND_COLOR);
        savingAdviceSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        backgroundPanel.add(savingAdviceSubtitle);
        JPanel savingAdvicePanelWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        savingAdvicePanelWrapper.setOpaque(false);
        savingAdvicePanelWrapper.add(createFramedPanel(createSavingAdvicePanelContent()));
        savingAdvicePanelWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        backgroundPanel.add(savingAdvicePanelWrapper);
        backgroundPanel.add(Box.createVerticalStrut(15));

        // 3. Custom Budget
        JLabel customBudgetSubtitle = new JLabel("Custom Budget", SwingConstants.CENTER);
        customBudgetSubtitle.setFont(TITLE_FONT);
        customBudgetSubtitle.setForeground(TITLE_FOREGROUND_COLOR);
        customBudgetSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        backgroundPanel.add(customBudgetSubtitle);
        JPanel customBudgetPanelWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        customBudgetPanelWrapper.setOpaque(false);
        customBudgetPanelWrapper.add(createFramedPanel(createCustomBudgetPanelContent()));
        customBudgetPanelWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        backgroundPanel.add(customBudgetPanelWrapper);
        backgroundPanel.add(Box.createVerticalStrut(15));

        // 4. Spending status
        JLabel spendingStatusSubtitle = new JLabel("Spending status", SwingConstants.CENTER);
        spendingStatusSubtitle.setFont(TITLE_FONT);
        spendingStatusSubtitle.setForeground(TITLE_FOREGROUND_COLOR);
        spendingStatusSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        backgroundPanel.add(spendingStatusSubtitle);
        JPanel spendingStatusPanelWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        spendingStatusPanelWrapper.setOpaque(false);
        spendingStatusPanelWrapper.add(createFramedPanel(createSpendingStatusPanelContent()));
        spendingStatusPanelWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        backgroundPanel.add(spendingStatusPanelWrapper);
        backgroundPanel.add(Box.createVerticalStrut(20));

        // Add vertical glue to push everything to the top
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

        gbc.gridx = 0;
        gbc.gridy = 0;
        goalsPanelContent.add(new JLabel("Budget:"), gbc);
        gbc.gridx = 1;
        goalsPanelContent.add(budgetValueLabel, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        goalsPanelContent.add(new JLabel("Saving Goal:"), gbc);
        gbc.gridx = 1;
        goalsPanelContent.add(savingGoalValueLabel, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        goalsPanelContent.add(new JLabel("Mode:"), gbc);
        gbc.gridx = 1;
        goalsPanelContent.add(modeValueLabel, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        goalsPanelContent.add(new JLabel("Reason:"), gbc);
        gbc.gridx = 1;
        goalsPanelContent.add(reasonValueLabel, gbc);

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

        gbc.gridx = 0;
        gbc.gridy = 0;
        customBudgetPanelContent.add(new JLabel("Expected budget amount: ¥"), gbc);
        gbc.gridx = 1;
        customBudgetPanelContent.add(customBudgetInputField, gbc);
        gbc.gridx = 2;
        customBudgetPanelContent.add(saveCustomBudgetButton, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 3; // 让 Restore 按钮占据一行
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

        gbc.gridx = 0;
        gbc.gridy = 0;
        savingAdvicePanelContent.add(new JLabel("Top spending this month:"), gbc);
        gbc.gridy++;
        savingAdvicePanelContent.add(topSpendingCategoryLabel, gbc);
        gbc.gridy++;
        savingAdvicePanelContent.add(new JLabel("Large consumption (Over 7% of income):"), gbc);
        gbc.gridy++;
        savingAdvicePanelContent.add(new JScrollPane(largeConsumptionTextArea), gbc);
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

        gbc.gridx = 0;
        gbc.gridy = 0;
        spendingStatusPanelContent.add(new JLabel("This month's expenditure:"), gbc);
        gbc.gridx = 1;
        spendingStatusPanelContent.add(expenditureValueLabel, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        spendingStatusPanelContent.add(budgetStatusLabel, gbc);

        return spendingStatusPanelContent;
    }

    private void loadBudgetData() {
        setLabelsLoading();
        SwingWorker<BudgetDataContainer, Void> worker = new SwingWorker<>() {
            @Override
            protected BudgetDataContainer doInBackground() throws Exception {
                if (username == null || username.trim().isEmpty()) {
                    throw new IllegalStateException("Username is not set for BudgetManagementPanel.");
                }
                LocalDate today = LocalDate.now();
                BudgetRecommendation recommendation = BudgetAdvisor.calculateRecommendation(username, today);
                List<TransactionData> transactions = TransactionService.readTransactions(username);
                double currentMonthIncome = calculateCurrentMonthIncome(transactions); // 重新计算当月收入
                double currentMonthExpense = calculateCurrentMonthExpense(transactions);
                String topCategory = findTopExpenseCategory(transactions);
                List<String> largeConsumptions = findLargeConsumptions(transactions, currentMonthIncome); // 使用实际收入作为参考
                return new BudgetDataContainer(recommendation, currentMonthExpense, currentMonthIncome, topCategory, largeConsumptions);
            }

            @Override
            protected void done() {
                try {
                    BudgetDataContainer data = get();
                    updateLabels(data);
                } catch (Exception e) {
                    System.err.println("ERROR: BudgetPanel - Failed to load or display budget data.");
                    e.printStackTrace();
                    setLabelsError();
                }
            }
        };
        List<TransactionData> transactions = TransactionService.readTransactions(username);
        System.out.println("---- Expenses for April 2025: ----");
        DateTimeFormatter debugFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        for (TransactionData tx : transactions) {
            if ("Expense".equalsIgnoreCase(tx.getOperation())) {
                try {
                    LocalDate transactionDate = LocalDate.parse(tx.getTime(), BudgetAdvisor.DATE_FORMATTER);
                    if (transactionDate.getYear() == 2025 && transactionDate.getMonthValue() == 4) {
                        System.out.println("  时间: " + transactionDate.format(debugFormatter) + ", 金额: " + tx.getAmount() + ", 类型: " + tx.getType());
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("Error parsing date for debug: " + tx.getTime());
                }
            }
        }
        System.out.println("---- End of Expenses for April 2025 ----");
        worker.execute();
    }

    private void updateLabels(BudgetDataContainer data) {
        BudgetRecommendation recommendation = data.recommendation;
        double currentMonthExpense = data.currentMonthExpense;
        double currentMonthIncome = data.currentMonthIncome;
        String topCategory = data.topCategory;
        List<String> largeConsumptions = data.largeConsumptions;

        // Update Monthly Saving Goal & Budget
        Double customBudget = BudgetAdvisor.getCustomBudget(this.username);
        double budgetToUse = (customBudget != null) ? customBudget : recommendation.suggestedBudget;
        budgetValueLabel.setText(String.format("¥%.2f", budgetToUse));
        savingGoalValueLabel.setText(String.format("¥%.2f", currentMonthIncome - budgetToUse)); // 使用实际收入减去使用的预算作为储蓄目标
        modeValueLabel.setText(recommendation.mode.getDisplayName());
        reasonValueLabel.setText(recommendation.reason);

        // Update Custom Budget input field (if a custom budget exists)
        customBudgetInputField.setText(customBudget != null ? String.format("%.2f", customBudget) : "");

        // Update Saving Advice
        topSpendingCategoryLabel.setText(topCategory != null ? topCategory : "No expenses this month");
        if (largeConsumptions.isEmpty()) {
            largeConsumptionTextArea.setText("No large consumptions found this month (over 7% of income).");
        } else {
            largeConsumptionTextArea.setText(String.join("\n", largeConsumptions));
        }

        // Update Spending status
        expenditureValueLabel.setText(String.format("¥%.2f", currentMonthExpense));
        double remaining = budgetToUse - currentMonthExpense;
        updateBudgetStatusLabel(remaining);

        this.revalidate();
        this.repaint();

        System.out.println("Current Month Income (updateLabels): " + currentMonthIncome);
        System.out.println("Budget To Use (updateLabels): " + budgetToUse);
        savingGoalValueLabel.setText(String.format("¥%.2f", currentMonthIncome - budgetToUse));
    }

    private void setLabelsLoading() {
        String loading = "Loading...";
        budgetValueLabel.setText(loading);
        savingGoalValueLabel.setText(loading);
        modeValueLabel.setText(loading);
        reasonValueLabel.setText(loading);
        topSpendingCategoryLabel.setText("N/A");
        expenditureValueLabel.setText(loading);
        budgetStatusLabel.setText(loading);
        largeConsumptionTextArea.setText("Loading...");
    }

    private void setLabelsError() {
        String error = "Error loading data";
        budgetValueLabel.setText(error);
        savingGoalValueLabel.setText(error);
        modeValueLabel.setText(error);
        reasonValueLabel.setText(error);
        topSpendingCategoryLabel.setText("N/A");
        expenditureValueLabel.setText(error);
        budgetStatusLabel.setText(error);
        largeConsumptionTextArea.setText(error);
    }

    private void handleSaveCustomBudget() {
        String newBudgetStr = customBudgetInputField.getText().trim();
        if (!newBudgetStr.isEmpty()) {
            try {
                double newBudget = Double.parseDouble(newBudgetStr);
                if (newBudget >= 0) {
                    BudgetAdvisor.saveCustomBudget(this.username, newBudget);
                    JOptionPane.showMessageDialog(this, "Custom budget set to ¥" + String.format("%.2f", newBudget));
                    loadBudgetData(); // Refresh data to reflect the change
                } else {
                    JOptionPane.showMessageDialog(this, "Budget cannot be negative.", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Invalid number format.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            int choice = JOptionPane.showConfirmDialog(this, "Do you want to clear your custom budget?", "Clear Budget", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                BudgetAdvisor.clearCustomBudget(this.username);
                JOptionPane.showMessageDialog(this, "Custom budget cleared. Using system recommendation.");
                loadBudgetData(); // Refresh data
            }
        }
    }

    private void handleRestoreIntelligent() {
        BudgetAdvisor.clearCustomBudget(this.username);
        JOptionPane.showMessageDialog(this, "Intelligent budget recommendation restored.");
        loadBudgetData(); // Refresh data
    }

    private void updateBudgetStatusLabel(double remainingValue) {
        if (remainingValue >= 0) {
            budgetStatusLabel.setText(String.format("Distance to the budget: ¥%.2f", remainingValue));
            budgetStatusLabel.setForeground(new Color(0, 128, 0)); // Green
        } else {
            budgetStatusLabel.setText(String.format("Overspent by: ¥%.2f", Math.abs(remainingValue)));
            budgetStatusLabel.setForeground(Color.RED); // Red
        }
    }

    private String findTopExpenseCategory(List<TransactionData> transactions) {
        if (transactions == null) return null;
        Map<String, Double> categoryTotals = new HashMap<>();
        LocalDate now = LocalDate.now();
        for (TransactionData tx : transactions) {
            if ("Expense".equalsIgnoreCase(tx.getOperation())) {
                try {
                    LocalDate transactionDate = LocalDate.parse(tx.getTime(), TRANSACTION_TIME_FORMATTER);
                    if (transactionDate.getYear() == now.getYear() && transactionDate.getMonthValue() == now.getMonthValue()) {
                        String category = tx.getType();
                        if (category == null || category.trim().isEmpty() || "u".equalsIgnoreCase(category.trim())) {
                            category = "Unclassified";
                        } else {
                            category = category.trim();
                        }
                        categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + tx.getAmount());
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("ERROR: Failed to parse date in findTopExpenseCategory: " + tx.getTime() + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return categoryTotals.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private List<String> findLargeConsumptions(List<TransactionData> transactions, double currentMonthIncome) {
        if (transactions == null || currentMonthIncome <= 0) return List.of();
        List<String> largeConsumptions = new ArrayList<>();
        LocalDate now = LocalDate.now();
        double largeThreshold = currentMonthIncome * 0.07;
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        for (TransactionData tx : transactions) {
            if ("Expense".equalsIgnoreCase(tx.getOperation())) {
                try {
                    LocalDate transactionDate = LocalDate.parse(tx.getTime(), TRANSACTION_TIME_FORMATTER);
                    if (transactionDate.getYear() == now.getYear() && transactionDate.getMonthValue() == now.getMonthValue() && tx.getAmount() > largeThreshold) {
                        largeConsumptions.add(String.format("%s - ¥%.2f - %s", transactionDate.format(displayFormatter), tx.getAmount(), tx.getType()));
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("ERROR: Failed to parse date in findLargeConsumptions: " + tx.getTime() + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return largeConsumptions;
    }

    private double calculateCurrentMonthIncome(List<TransactionData> transactions) {
        if (transactions == null) return 0.0;
        double totalIncome = 0.0;
        LocalDate now = LocalDate.now();
        for (TransactionData tx : transactions) {
            if ("Income".equalsIgnoreCase(tx.getOperation())) {
                try {
                    LocalDate transactionDate = LocalDate.parse(tx.getTime(), TRANSACTION_TIME_FORMATTER);
                    if (transactionDate.getYear() == now.getYear() && transactionDate.getMonthValue() == now.getMonthValue()) {
                        totalIncome += tx.getAmount();
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("ERROR: Failed to parse date in calculateCurrentMonthIncome: " + tx.getTime() + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return totalIncome;
    }

    private double calculateCurrentMonthExpense(List<TransactionData> transactions) {
        if (transactions == null) {
            System.out.println("calculateCurrentMonthExpense: transactions is null");
            return 0.0;
        }
        double totalExpense = 0.0;
        LocalDate now = LocalDate.now();
        System.out.println("calculateCurrentMonthExpense: Now is " + now.toString());
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        LocalDate lastDayOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        System.out.println("calculateCurrentMonthExpense: First day of month is " + firstDayOfMonth.toString());
        System.out.println("calculateCurrentMonthExpense: Last day of month is " + lastDayOfMonth.toString());

        for (TransactionData tx : transactions) {
            System.out.println("calculateCurrentMonthExpense: Processing transaction - Operation: " + tx.getOperation() + ", Time: " + tx.getTime() + ", Amount: " + tx.getAmount());
            if ("Expense".equalsIgnoreCase(tx.getOperation())) {
                try {
                    LocalDate transactionDate = LocalDate.parse(tx.getTime(), TRANSACTION_TIME_FORMATTER);
                    System.out.println("calculateCurrentMonthExpense: Parsed transaction date is " + transactionDate.toString());
                    if (!transactionDate.isBefore(firstDayOfMonth) && !transactionDate.isAfter(lastDayOfMonth)) {
                        totalExpense += tx.getAmount();
                        System.out.println("calculateCurrentMonthExpense: Expense added - Amount: " + tx.getAmount() + ", Total Expense: " + totalExpense);
                    } else {
                        System.out.println("calculateCurrentMonthExpense: Transaction date " + transactionDate.toString() + " is not within the current month.");
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("ERROR: Failed to parse date in calculateCurrentMonthExpense: " + tx.getTime() + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        System.out.println("calculateCurrentMonthExpense: Total expense for the month is " + totalExpense);
        return totalExpense;
    }

    private void styleCustomBudgetButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(BUTTON_FOREGROUND_COLOR);
        button.setBackground(BUTTON_BACKGROUND_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // Helper class to hold data fetched in background
    private static class BudgetDataContainer {
        final BudgetRecommendation recommendation;
        final double currentMonthExpense;
        final double currentMonthIncome;
        final String topCategory;
        final List<String> largeConsumptions;

        BudgetDataContainer(BudgetRecommendation rec, double expense, double income, String top, List<String> large) {
            this.recommendation = rec;
            this.currentMonthExpense = expense;
            this.currentMonthIncome = income;
            this.topCategory = top;
            this.largeConsumptions = large;
        }
    }
}