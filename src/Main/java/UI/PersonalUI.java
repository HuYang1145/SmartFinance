package UI;

import Model.UserSession;
import Person.IncomeExpenseChart;
import Person.*; // Import other Person dialogs
import UI.BudgetGoalDialog; // Import BudgetGoalDialog

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import Model.BudgetAdvisor;
import Model.BudgetAdvisor.BudgetMode;
import Model.BudgetAdvisor.BudgetRecommendation;
import Model.BudgetAdvisor.Transaction;
import java.time.LocalDate;
import java.util.List;
import javax.swing.Timer;
import Model.TransactionCSVImporter;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.io.BufferedReader; // Import BufferedReader
import java.io.FileReader; // Import FileReader

public class PersonalUI extends JDialog {

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private SidebarPanel sidebarPanel;
    private ContentPanelManager contentManager;
    private JPanel financialSuggestionPanel; // 显示财务建议的面板
    private JLabel financialSuggestionTitle;
    private JLabel currentBudgetLabel;
    private JLabel currentSavingGoalLabel;
    private JLabel budgetModeLabel; // 显示预算模式
    private JLabel budgetReasonLabel; // 显示预算原因
    private JButton viewSuggestionButton;
    private JButton manageBudgetGoalButton;
    private boolean budgetExceeded80Percent = false;
    private int currentReportCycleDays = 7;
    private final String SOURCE_TRANSACTIONS_FILE = "transactions.csv";
    private final String DESTINATION_TRANSACTIONS_FILE = "transactions.csv";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    public PersonalUI() {
        setTitle("Personal Account Center");
        setSize(950, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        sidebarPanel = new SidebarPanel(this);
        add(sidebarPanel, BorderLayout.WEST);

        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(new Color(30, 60, 120));

        contentManager = new ContentPanelManager(this, contentPanel, cardLayout);
        contentManager.initializePanels();

        createFinancialSuggestionPanel();
        contentPanel.add(financialSuggestionPanel, "financialSuggestion");

        add(contentPanel, BorderLayout.CENTER);

        cardLayout.show(contentPanel, "individualCenter");

        Timer budgetCheckTimer = new Timer(60000, e -> checkBudgetThreshold());
        budgetCheckTimer.start();

        String username = UserSession.getCurrentUsername();
        LocalDate now = LocalDate.now();
        if (username != null) {
            try {
                File sourceFile = new File(SOURCE_TRANSACTIONS_FILE);
                String destinationFile = DESTINATION_TRANSACTIONS_FILE;
                TransactionCSVImporter.importTransactions(sourceFile, destinationFile);
                System.out.println("交易记录导入完成 (PersonalUI 初始化)");
            } catch (IOException | IllegalArgumentException e) {
                System.err.println("导入交易记录失败 (PersonalUI 初始化): " + e.getMessage());
                e.printStackTrace();
            }
            updateFinancialSuggestionDisplay();
            checkBudgetExceededOnLogin(username); // 在登录后检查预算是否超支
        } else {
            updateFinancialSuggestionDisplay();
        }

        setVisible(true);
    }

    private void createFinancialSuggestionPanel() {
        financialSuggestionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 30));
        financialSuggestionPanel.setBackground(new Color(30, 60, 120));

        financialSuggestionTitle = new JLabel("Financial Management Suggestion");
        financialSuggestionTitle.setForeground(Color.WHITE);
        financialSuggestionTitle.setFont(new Font("Arial", Font.BOLD, 24));

        currentBudgetLabel = new JLabel("Current Budget: Loading...");
        currentBudgetLabel.setForeground(Color.WHITE);
        currentBudgetLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        currentSavingGoalLabel = new JLabel("Saving Goal: Loading...");
        currentSavingGoalLabel.setForeground(Color.WHITE);
        currentSavingGoalLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        budgetModeLabel = new JLabel("Budget Mode: Loading...");
        budgetModeLabel.setForeground(Color.WHITE);
        budgetModeLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        budgetReasonLabel = new JLabel("Reason: Loading...");
        budgetReasonLabel.setForeground(Color.WHITE);
        budgetReasonLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        viewSuggestionButton = new JButton("View Detailed Suggestion");
        styleButton(viewSuggestionButton);
        viewSuggestionButton.setPreferredSize(new Dimension(220, 40));
        viewSuggestionButton.addActionListener(e -> showBudgetSuggestionDialog());

        manageBudgetGoalButton = new JButton("Manage Budget Goal");
        styleButton(manageBudgetGoalButton);
        manageBudgetGoalButton.setPreferredSize(new Dimension(220, 40));
        manageBudgetGoalButton.addActionListener(e -> checkLoginAndShowDialog(() -> {
            BudgetGoalDialog budgetDialog = new BudgetGoalDialog(PersonalUI.this);
            budgetDialog.setVisible(true);
        }));

        JPanel suggestionInfoPanel = new JPanel(new GridLayout(4, 1, 10, 5)); // 增加行数以显示模式和原因
        suggestionInfoPanel.setBackground(new Color(30, 60, 120));
        suggestionInfoPanel.add(currentBudgetLabel);
        suggestionInfoPanel.add(currentSavingGoalLabel);
        suggestionInfoPanel.add(budgetModeLabel);
        suggestionInfoPanel.add(budgetReasonLabel);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        buttonPanel.setBackground(new Color(30, 60, 120));
        buttonPanel.add(viewSuggestionButton);
        buttonPanel.add(manageBudgetGoalButton);

        JPanel mainSuggestionPanel = new JPanel(new BorderLayout(0, 30));
        mainSuggestionPanel.setBackground(new Color(30, 60, 120));
        mainSuggestionPanel.add(financialSuggestionTitle, BorderLayout.NORTH);
        mainSuggestionPanel.add(suggestionInfoPanel, BorderLayout.CENTER);
        mainSuggestionPanel.add(buttonPanel, BorderLayout.SOUTH);

        financialSuggestionPanel.add(mainSuggestionPanel);
    }

    private void checkBudgetExceededOnLogin(String username) {
        List<BudgetAdvisor.Transaction> transactionsThisMonth = getTransactionsForCurrentMonth(username);
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        double totalExpenseThisMonth = transactionsThisMonth.stream()
                .filter(t -> (t.operation.equals("Transfer Out") || t.operation.equals("Withdrawal")) &&
                        t.date.isAfter(startOfMonth.minusDays(1)) && t.date.isBefore(endOfMonth.plusDays(1)))
                .mapToDouble(t -> Math.abs(t.amount))
                .sum();

        Double currentBudget = BudgetAdvisor.getCustomBudget(username);
        if (currentBudget == null) {
            BudgetRecommendation recommendation = BudgetAdvisor.calculateRecommendation(username, LocalDate.now());
            currentBudget = recommendation.suggestedBudget;
        }

        if (currentBudget != null && currentBudget > 0) {
            double eightyPercentBudget = currentBudget * 0.8;
            if (totalExpenseThisMonth >= eightyPercentBudget) {
                double remainingBudget = currentBudget - totalExpenseThisMonth;
                JOptionPane.showMessageDialog(
                        this,
                        "You are about to exceed your budget this month.\n" +
                                "You have $" + String.format("%.2f", remainingBudget) + " left in your budget this month.",
                        "Budget Warning",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        }
    }

    /**
     * Shows the main individual center panel.
     */
    public void showIndividualCenter() {
        checkLoginAndShowCard("individualCenter");
    }

    /**
     * Shows the periodic report options panel.
     */
    public void showReportOptions() {
        checkLoginAndShowCard("reportOptions");
    }

    /**
     * Shows the detailed report view panel.
     * Important: This assumes ContentPanelManager holds the reference to ReportViewPanel
     * or we need a way to get it to call loadReportData.
     * A better approach might be to have ContentPanelManager provide access or
     * call loadReportData when switching.
     * For now, we rely on the panel's setVisible method.
     */
    public void showReportView() {
        // The ReportViewPanel's setVisible(true) should trigger data loading
        checkLoginAndShowCard("reportView");
    }

    /**
     * Shows the report cycle settings panel.
     * Relies on the panel's setVisible(true) to refresh the displayed cycle days.
     */
    public void showCycleSettings() {
        checkLoginAndShowCard("reportCycleSettings");
    }

    /**
     * Shows the AI Q&A panel.
     */
    public void showAiQA() {
        checkLoginAndShowCard("aiQA");
    }

    /**
     * Shows the Spending Proportion placeholder panel.
     */
    public void showSpendingProportion() {
        checkLoginAndShowCard("spendingProportion");
    }

    /**
     * Gets the current reporting cycle duration in days for this session.
     *
     * @return The number of days in the reporting cycle.
     */
    public int getReportCycleDays() {
        return currentReportCycleDays;
    }

    /**
     * Sets the reporting cycle duration in days for the current session.
     * Value is not persisted across logins.
     *
     * @param days The new number of days for the reporting cycle (must be > 0).
     */
    public void setReportCycleDays(int days) {
        if (days > 0) {
            this.currentReportCycleDays = days;
            System.out.println("Report cycle for this session set to: " + days + " days.");
            // We don't automatically refresh the report view here.
            // The view will refresh when it becomes visible next time or manually refreshed.
        } else {
            System.err.println("Attempted to set invalid report cycle days: " + days);
        }
    }

    public void updateFinancialSuggestionDisplay() {
        String username = UserSession.getCurrentUsername();
        LocalDate now = LocalDate.now();
        System.err.println("Current Username in PersonalUI: " + username);
        if (username != null) {
            BudgetRecommendation recommendation = BudgetAdvisor.calculateRecommendation(username, now);
            currentBudgetLabel.setText("Current Budget: $" + String.format("%.2f", recommendation.suggestedBudget));
            currentSavingGoalLabel.setText("Saving Goal: $" + String.format("%.2f", recommendation.suggestedSaving));
            budgetModeLabel.setText("Budget Mode: " + recommendation.mode.getDisplayName());
            budgetReasonLabel.setText("Reason: " + recommendation.reason);
        } else {
            currentBudgetLabel.setText("Current Budget: N/A");
            currentSavingGoalLabel.setText("Saving Goal: N/A");
            budgetModeLabel.setText("Budget Mode: N/A");
            budgetReasonLabel.setText("Reason: N/A");
        }
    }

    private void styleButton(JButton button) {
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setFocusPainted(false);
    }

    public void showFinancialSuggestion() {
        checkLoginAndShowCard("financialSuggestion");
        if (UserSession.getCurrentUsername() != null) {
            updateFinancialSuggestionDisplay();
        } else {
            UIUtils.showLoginError(this);
        }
    }

    private void showBudgetSuggestionDialog() {
        String username = UserSession.getCurrentUsername();
        LocalDate now = LocalDate.now();
        if (username == null) {
            JOptionPane.showMessageDialog(this, "Please log in to see budget information.", "Login Required", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BudgetRecommendation rec = BudgetAdvisor.calculateRecommendation(username, now);
        double budget = rec.suggestedBudget;
        double savingGoal = rec.suggestedSaving;
        BudgetMode mode = rec.mode;
        String reason = rec.reason;
        boolean hasPastData = rec.hasPastData;
        StringBuilder message = new StringBuilder("<html><b><font size=\"+1\">Budget Suggestion</font></b><br>");

        if (mode == BudgetMode.NORMAL) {
            if (hasPastData) {
                message.append("Based on your consumption habits in the past three months,<br>");
            } else {
                message.append("Based on system suggestions,<br>");
            }
        } else if (mode == BudgetMode.ECONOMICAL_FESTIVAL) {
            message.append("Next month is the shopping festival. It is recommended to save more money,<br>");
        } else if (mode == BudgetMode.ECONOMICAL_UNSTABLE) {
            message.append("Last month's consumption was difficult to predict. It is recommended to save more money,<br>");
        } else if (mode == BudgetMode.CUSTOM) {
            message.append("Based on your own defined budget,<br>");
        }

        message.append("Your budget for this month is: $").append(String.format("%.2f", budget)).append("<br>");
        message.append("Your savings goals for this month are: $").append(String.format("%.2f", savingGoal)).append("<br>");
        message.append("Budget Mode: ").append(mode.getDisplayName()).append("<br>");
        message.append("Reason: ").append(reason).append("</html>");

        JOptionPane.showMessageDialog(
                this,
                message.toString(),
                "Budget Suggestion",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void checkBudgetThreshold() {
        String username = UserSession.getCurrentUsername();
        if (username != null && !budgetExceeded80Percent) {
            List<BudgetAdvisor.Transaction> transactionsThisMonth = getTransactionsForCurrentMonth(username);
            LocalDate now = LocalDate.now();
            LocalDate startOfMonth = now.withDayOfMonth(1);
            LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

            double totalTransferOutThisMonth = transactionsThisMonth.stream()
                    .filter(t -> !(t.operation.equals("Deposit") || t.operation.equals("Transfer In")) &&
                            t.date.isAfter(startOfMonth.minusDays(1)) && t.date.isBefore(endOfMonth.plusDays(1)))
                    .mapToDouble(t -> Math.abs(t.amount))
                    .sum();

            Double currentBudget = BudgetAdvisor.getCustomBudget(username);
            if (currentBudget == null) {
                BudgetRecommendation recommendation = BudgetAdvisor.calculateRecommendation(username, LocalDate.now());
                currentBudget = recommendation.suggestedBudget;
            }

            if (currentBudget != null && currentBudget > 0) {
                double eightyPercentBudget = currentBudget * 0.8;
                if (totalTransferOutThisMonth >= eightyPercentBudget) {
                    double remainingBudget = currentBudget - totalTransferOutThisMonth;
                    JOptionPane.showMessageDialog(
                            this,
                            "Budget Warning: You have used 80% of your budget this month.\n" +
                                    "Remaining budget: $" + String.format("%.2f", remainingBudget),
                            "Budget Warning",
                            JOptionPane.WARNING_MESSAGE
                    );
                    budgetExceeded80Percent = true;
                }
            }
        }
    }

    private List<BudgetAdvisor.Transaction> getTransactionsForCurrentMonth(String username) {
        List<BudgetAdvisor.Transaction> userTransactions = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        try (BufferedReader br = new BufferedReader(new FileReader(DESTINATION_TRANSACTIONS_FILE))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 5 && data[0].trim().equals(username)) {
                    try {
                        LocalDate transactionDate = LocalDate.parse(data[3].trim().split(" ")[0], BudgetAdvisor.DATE_FORMATTER);
                        String operation = data[1].trim();
                        double amount = Double.parseDouble(data[2].trim());
                        String details = data[4].trim();
                        if (transactionDate.isAfter(startOfMonth.minusDays(1)) &&
                                transactionDate.isBefore(endOfMonth.plusDays(1))) {
                            BudgetAdvisor.Transaction transaction = new BudgetAdvisor.Transaction();
                            transaction.username = username;
                            transaction.operation = operation;
                            transaction.amount = amount;
                            transaction.date = transactionDate;
                            transaction.details = details;
                            userTransactions.add(transaction);
                        }
                    } catch (DateTimeParseException | NumberFormatException e) {
                        System.err.println("Error parsing transaction data: " + line + " - " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading transaction file: " + e.getMessage());
        }
        return userTransactions;
    }

    public void showIncomeExpenseChart() {
        System.out.println("Attempting to show IncomeExpenseChart...");
        if (UserSession.getCurrentUsername() != null) {
            // Run chart display on the EDT
            SwingUtilities.invokeLater(() -> {
                // Assuming IncomeExpenseChart expects the file path
                IncomeExpenseChart.showIncomeExpensePieChart("transactions.csv");
            });
        } else {
            UIUtils.showLoginError(this);
        }
    }

    public void showTransactionHistory() {
        checkLoginAndShowDialog(() -> new TransactionHistory(PersonalUI.this));
    }

    public void checkLoginAndShowCard(String cardName) {
        if (UserSession.getCurrentUsername() != null) {
            System.out.println("Switching to card: " + cardName); // Debugging
            cardLayout.show(contentPanel, cardName);
        } else {
            System.out.println("Login required to show card: " + cardName); // Debugging
            UIUtils.showLoginError(this);
        }
    }

    public void checkLoginAndShowDialog(Runnable dialogCreator) {
        if (UserSession.
                getCurrentUsername() != null) {
            // Ensure the dialog creation/display happens on the EDT
            SwingUtilities.invokeLater(dialogCreator);
        } else {
            System.out.println("Login required to perform this action."); // Debugging
            UIUtils.showLoginError(this);
        }
    }

    /**
     * Handles the logout process, including confirmation and clearing the user session.
     */
    public void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            UserSession.clearSession(); // Clear the username from session
            dispose(); // Close this PersonalUI window
            // Optionally: Bring the main login window (App) back to the front or create a new one
            // This depends on how App is managed. If App is still running but hidden:
            // findParentAppFrame().setVisible(true); // Requires a method to get the App frame
            // Or create a new login session:
            // SwingUtilities.invokeLater(() -> new Main.App().setVisible(true));
        }
    }

    /**
     * Helper method to show a specific card by name.
     *
     * @param cardName The name of the card to show.
     */
    public void showCard(String cardName) {
        cardLayout.show(contentPanel, cardName);
    }
}

// Optional helper to find the parent App frame if needed after logout
/*
private Frame findParentAppFrame() {
    Container parent = getParent();
    while (parent != null && !(parent instanceof Frame)) {
        parent = parent.getParent();
    }
    return (Frame) parent;
}
*/