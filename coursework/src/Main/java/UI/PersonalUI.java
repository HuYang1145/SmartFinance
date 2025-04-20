package UI; // Assuming the final package is UI, adjust if necessary

import Model.BudgetAdvisor;
import Model.BudgetAdvisor.BudgetMode;
import Model.BudgetAdvisor.BudgetRecommendation;
// Removed duplicate Transaction import if BudgetAdvisor.Transaction is used
import Model.TransactionCSVImporter;
import Model.UserSession;
import Person.IncomeExpenseChart;
import Person.*; // Import other Person dialogs like TransactionHistory if needed
import UI.BudgetGoalDialog; // Import BudgetGoalDialog

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer; // Explicit import for Timer

// It's assumed UIUtils class exists in the UI package or is correctly imported
// import UI.UIUtils;

public class PersonalUI extends JDialog {

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private SidebarPanel sidebarPanel;
    private ContentPanelManager contentManager; // Manager to handle panel creation and adding

    // --- Instance variables for Financial Suggestion Panel (from second snippet) ---
    private JPanel financialSuggestionPanel;
    private JLabel financialSuggestionTitle;
    private JLabel currentBudgetLabel;
    private JLabel currentSavingGoalLabel;
    private JLabel budgetModeLabel;
    private JLabel budgetReasonLabel;
    private JButton viewSuggestionButton;
    private JButton manageBudgetGoalButton;
    private boolean budgetExceeded80Percent = false; // Flag for budget warning

    // --- Other instance variables ---
    private int currentReportCycleDays = 7; // Default cycle days, non-persistent (session-based)
    private final String SOURCE_TRANSACTIONS_FILE = "transactions.csv"; // Source file for import (can be same as dest)
    private final String DESTINATION_TRANSACTIONS_FILE = "transactions.csv"; // Main transaction file
    // Date Formatter for parsing transactions within this UI if needed elsewhere
    // BudgetAdvisor might have its own, ensure consistency
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");


    public PersonalUI() {
        setTitle("Personal Account Center");
        // Consider slightly larger default size if chart needs more space
        setSize(950, 650); // Using size from both snippets
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- 1. Create Sidebar ---
        sidebarPanel = new SidebarPanel(this); // Pass reference to this PersonalUI instance
        add(sidebarPanel, BorderLayout.WEST);

        // --- 2. Create Content Panel area with CardLayout ---
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(new Color(30, 60, 120)); // Background from second snippet

        // --- 3. Create ContentPanelManager and Initialize *other* Panels ---
        // ContentPanelManager will create and add panels EXCEPT financialSuggestion (handled below)
        contentManager = new ContentPanelManager(this, contentPanel, cardLayout);
        contentManager.initializePanels(); // Make sure this DOES NOT add "financialSuggestion"

        // --- 4. Create and Add Financial Suggestion Panel (from second snippet) ---
        createFinancialSuggestionPanel(); // Create the specific panel
        contentPanel.add(financialSuggestionPanel, "financialSuggestion"); // Add it to the CardLayout

        // --- 5. Add the Content Panel to the main layout ---
        add(contentPanel, BorderLayout.CENTER);

        // --- 6. Set Default View ---
        cardLayout.show(contentPanel, "individualCenter"); // Show initial panel

        // --- 7. Initialize Budget Check Timer (from second snippet) ---
        Timer budgetCheckTimer = new Timer(60000, e -> checkBudgetThreshold()); // Check every 60 seconds
        budgetCheckTimer.start();

        // --- 8. Initial Data Load / Checks on Login (from second snippet) ---
        String username = UserSession.getCurrentUsername();
        if (username != null) {
            // Import transactions (consider if this should happen only once at login/startup elsewhere)
            try {
                File sourceFile = new File(SOURCE_TRANSACTIONS_FILE);
                // Check if source exists before importing, or handle appropriately
                 if (sourceFile.exists()){
                   TransactionCSVImporter.importTransactions(sourceFile, DESTINATION_TRANSACTIONS_FILE);
                   System.out.println("交易记录导入完成 (PersonalUI 初始化)");
                 } else {
                    System.out.println("Source transaction file not found, skipping import: " + SOURCE_TRANSACTIONS_FILE);
                 }
            } catch (IOException | IllegalArgumentException e) {
                System.err.println("导入交易记录失败 (PersonalUI 初始化): " + e.getMessage());
                // Optionally show a user-friendly error message
                // JOptionPane.showMessageDialog(this, "Error importing transactions: " + e.getMessage(), "Import Error", JOptionPane.ERROR_MESSAGE);
            }
            // Update display and check budget immediately
            updateFinancialSuggestionDisplay();
            checkBudgetExceededOnLogin(username); // Check budget status on login
        } else {
            // Update display even if not logged in (will show N/A)
             updateFinancialSuggestionDisplay();
        }


        // --- 9. Make the UI Visible ---
        setVisible(true);
    }

    // --- Methods to switch cards in the contentPanel ---

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
     */
    public void showReportView() {
        checkLoginAndShowCard("reportView");
        // Consider if ReportViewPanel needs explicit data refresh on show
        // ReportViewPanel panel = (ReportViewPanel) contentManager.getPanel("reportView");
        // if (panel != null) { panel.loadReportData(); }
    }

    /**
     * Shows the report cycle settings panel.
     */
    public void showCycleSettings() {
        checkLoginAndShowCard("reportCycleSettings");
        // Consider if panel needs explicit refresh
        // ReportCycleSettingsPanel panel = (ReportCycleSettingsPanel) contentManager.getPanel("reportCycleSettings");
        // if (panel != null) { panel.refreshDisplay(); }
    }

    /**
     * Shows the Financial Suggestion panel and updates its content.
     */
    public void showFinancialSuggestion() {
         checkLoginAndShowCard("financialSuggestion"); // Switch card first
         // Update the display only if the user is logged in after the check
        if (UserSession.getCurrentUsername() != null) {
             updateFinancialSuggestionDisplay();
         }
        // No need for the else clause here, checkLoginAndShowCard handles the login error popup
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
     * Shows the Transaction History dialog.
     */
     public void showTransactionHistory() {
         checkLoginAndShowDialog(() -> new TransactionHistory(PersonalUI.this)); // Assumes TransactionHistory takes PersonalUI frame
     }


    // --- Getter/Setter for report cycle (session only) ---

    /**
     * Gets the current reporting cycle duration in days for this session.
     * @return The number of days in the reporting cycle.
     */
    public int getReportCycleDays() {
        return currentReportCycleDays;
    }

    /**
     * Sets the reporting cycle duration in days for the current session.
     * Value is not persisted across logins.
     * @param days The new number of days for the reporting cycle (must be > 0).
     */
    public void setReportCycleDays(int days) {
        if (days > 0) {
            this.currentReportCycleDays = days;
            System.out.println("Report cycle for this session set to: " + days + " days.");
            // The report view will typically refresh when it becomes visible next time
            // or if it has its own refresh mechanism.
        } else {
            System.err.println("Attempted to set invalid report cycle days: " + days);
            JOptionPane.showMessageDialog(this, "Report cycle must be a positive number of days.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }


    // --- Financial Suggestion and Budget Methods (from second snippet) ---

    /**
     * Creates and configures the Financial Suggestion panel.
     */
    private void createFinancialSuggestionPanel() {
        financialSuggestionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 30));
        financialSuggestionPanel.setBackground(new Color(30, 60, 120)); // Dark blue background

        financialSuggestionTitle = new JLabel("Financial Management Suggestion");
        financialSuggestionTitle.setForeground(Color.WHITE);
        financialSuggestionTitle.setFont(new Font("Arial", Font.BOLD, 24));
        // Center the title text
        financialSuggestionTitle.setHorizontalAlignment(SwingConstants.CENTER);


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
        budgetReasonLabel.setFont(new Font("Arial", Font.PLAIN, 14)); // Slightly smaller for reason

        viewSuggestionButton = new JButton("View Detailed Suggestion");
        styleButton(viewSuggestionButton);
        viewSuggestionButton.setPreferredSize(new Dimension(220, 40));
        viewSuggestionButton.addActionListener(e -> showBudgetSuggestionDialog());

        manageBudgetGoalButton = new JButton("Manage Budget Goal");
        styleButton(manageBudgetGoalButton);
        manageBudgetGoalButton.setPreferredSize(new Dimension(220, 40));
        manageBudgetGoalButton.addActionListener(e -> checkLoginAndShowDialog(() -> {
            // This lambda now correctly uses the checkLoginAndShowDialog utility
            BudgetGoalDialog budgetDialog = new BudgetGoalDialog(PersonalUI.this); // Pass parent frame/dialog
            budgetDialog.setVisible(true);
             // After dialog closes, refresh the display
             updateFinancialSuggestionDisplay();
        }));

        // Panel for the text info (Budget, Goal, Mode, Reason)
        JPanel suggestionInfoPanel = new JPanel(new GridLayout(4, 1, 10, 10)); // Rows, Cols, Hgap, Vgap
        suggestionInfoPanel.setBackground(new Color(30, 60, 120)); // Match parent background
        suggestionInfoPanel.add(currentBudgetLabel);
        suggestionInfoPanel.add(currentSavingGoalLabel);
        suggestionInfoPanel.add(budgetModeLabel);
        suggestionInfoPanel.add(budgetReasonLabel);
        // Add some padding around the text
        suggestionInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));


        // Panel for the buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10)); // Rows, Cols, Hgap, Vgap
        buttonPanel.setBackground(new Color(30, 60, 120)); // Match parent background
        buttonPanel.add(viewSuggestionButton);
        buttonPanel.add(manageBudgetGoalButton);
         // Add some padding below buttons
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));


        // Main panel to hold title, info, and buttons vertically
        JPanel mainSuggestionPanel = new JPanel(new BorderLayout(0, 25)); // Vgap between components
        mainSuggestionPanel.setBackground(new Color(30, 60, 120)); // Match parent background
        mainSuggestionPanel.add(financialSuggestionTitle, BorderLayout.NORTH);
        mainSuggestionPanel.add(suggestionInfoPanel, BorderLayout.CENTER);
        mainSuggestionPanel.add(buttonPanel, BorderLayout.SOUTH);
         // Add padding around the main content block
        mainSuggestionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));


        // Add the main panel to the flow layout of financialSuggestionPanel
        financialSuggestionPanel.add(mainSuggestionPanel);
    }


    /**
     * Applies standard styling to a JButton.
     * @param button The button to style.
     */
    private void styleButton(JButton button) {
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(30, 60, 120)); // Dark blue text
        button.setFont(new Font("Arial", Font.BOLD, 16)); // Bold text
        button.setFocusPainted(false);
        // Optional: Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.LIGHT_GRAY); // Lighter background on hover
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE); // Restore original background
            }
        });
    }

    /**
     * Updates the labels on the Financial Suggestion panel based on current user data.
     */
    public void updateFinancialSuggestionDisplay() {
        String username = UserSession.getCurrentUsername();
        LocalDate now = LocalDate.now();
        // System.out.println("Updating Financial Suggestion Display for: " + username); // Debugging
        if (username != null) {
            try {
                BudgetRecommendation recommendation = BudgetAdvisor.calculateRecommendation(username, now);
                // Use retrieved or calculated custom budget if available
                Double customBudget = BudgetAdvisor.getCustomBudget(username);
                double displayBudget = (customBudget != null) ? customBudget : recommendation.suggestedBudget;

                currentBudgetLabel.setText(String.format("Current Budget: $%.2f", displayBudget));
                currentSavingGoalLabel.setText(String.format("Saving Goal: $%.2f", recommendation.suggestedSaving)); // Saving goal might still be suggestion
                budgetModeLabel.setText("Budget Mode: " + recommendation.mode.getDisplayName());
                // Wrap reason text if it's too long
                budgetReasonLabel.setText("<html>Reason: " + recommendation.reason + "</html>");

            } catch (Exception e) {
                 System.err.println("Error updating financial suggestion display: " + e.getMessage());
                 e.printStackTrace(); // Log the full error
                 currentBudgetLabel.setText("Current Budget: Error");
                 currentSavingGoalLabel.setText("Saving Goal: Error");
                 budgetModeLabel.setText("Budget Mode: Error");
                 budgetReasonLabel.setText("Reason: Error loading data");
            }
        } else {
            currentBudgetLabel.setText("Current Budget: N/A (Not logged in)");
            currentSavingGoalLabel.setText("Saving Goal: N/A");
            budgetModeLabel.setText("Budget Mode: N/A");
            budgetReasonLabel.setText("Reason: N/A");
        }
    }

    /**
     * Shows a dialog with detailed budget suggestions.
     */
    private void showBudgetSuggestionDialog() {
        String username = UserSession.getCurrentUsername();
        if (username == null) {
            UIUtils.showLoginError(this); // Use the utility function
            return;
        }

         LocalDate now = LocalDate.now();
         try{
             BudgetRecommendation rec = BudgetAdvisor.calculateRecommendation(username, now);
             Double customBudget = BudgetAdvisor.getCustomBudget(username);
             double budget = (customBudget != null) ? customBudget : rec.suggestedBudget; // Use custom if set
             double savingGoal = rec.suggestedSaving; // Use suggestion for saving goal display
             BudgetMode mode = rec.mode;
             String reason = rec.reason;
             boolean hasPastData = rec.hasPastData; // Check if recommendation was based on past data

             StringBuilder message = new StringBuilder("<html><body style='width: 300px;'>"); // Basic styling for width
             message.append("<b><font size='+1'>Budget Suggestion for This Month</font></b><br><br>");

             // Explain the basis of the suggestion
             if (mode == BudgetMode.CUSTOM) {
                 message.append("Based on your <b>custom settings</b>:<br>");
             } else if (hasPastData) {
                 message.append("Based on your spending patterns from the <b>last three months</b>:<br>");
             } else {
                 message.append("Based on <b>general recommendations</b> (no sufficient history):<br>");
             }

             // Add mode-specific context if not CUSTOM
             if (mode == BudgetMode.ECONOMICAL_FESTIVAL) {
                 message.append("<i>Note: Next month includes a shopping festival, suggesting a more economical approach.</i><br>");
             } else if (mode == BudgetMode.ECONOMICAL_UNSTABLE) {
                 message.append("<i>Note: Recent spending was variable, suggesting a more cautious budget.</i><br>");
             }

             // Display Budget and Saving Goal
             message.append("Suggested Monthly Budget: <b>$").append(String.format("%.2f", budget)).append("</b><br>");
             message.append("Suggested Monthly Saving Goal: <b>$").append(String.format("%.2f", savingGoal)).append("</b><br><br>");

             // Display Mode and Reason
             message.append("Budget Mode: ").append(mode.getDisplayName()).append("<br>");
             message.append("Reason: ").append(reason).append("<br>");

             message.append("</body></html>");

             JOptionPane.showMessageDialog(
                     this,
                     message.toString(),
                     "Budget Suggestion Details",
                     JOptionPane.INFORMATION_MESSAGE
             );
        } catch (Exception e) {
             System.err.println("Error generating budget suggestion dialog: " + e.getMessage());
             JOptionPane.showMessageDialog(this, "Could not retrieve budget suggestion details.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
    * Checks if the user's spending has exceeded 80% of their budget for the current month.
    * Shows a warning message only once per session if the threshold is crossed.
    */
    private void checkBudgetThreshold() {
        String username = UserSession.getCurrentUsername();
        // Only check if logged in AND the warning hasn't been shown this session
        if (username != null && !budgetExceeded80Percent) {
             try {
                 List<BudgetAdvisor.Transaction> transactionsThisMonth = getTransactionsForCurrentMonth(username);
                 LocalDate now = LocalDate.now();
                 LocalDate startOfMonth = now.withDayOfMonth(1);
                 LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

                 // Calculate total expenses (Transfer Out, Withdrawal, potentially Payment?)
                 // Be specific about what counts as an expense against the budget.
                 double totalExpenseThisMonth = transactionsThisMonth.stream()
                         .filter(t -> (t.operation.equalsIgnoreCase("Transfer Out") || t.operation.equalsIgnoreCase("Withdrawal")) &&
                                 !t.date.isBefore(startOfMonth) && !t.date.isAfter(endOfMonth)) // Inclusive date check
                         .mapToDouble(t -> Math.abs(t.amount)) // Use absolute amount for expenses
                         .sum();

                 // Get the current budget (custom or recommended)
                 Double currentBudget = BudgetAdvisor.getCustomBudget(username);
                 if (currentBudget == null) {
                     BudgetRecommendation recommendation = BudgetAdvisor.calculateRecommendation(username, now);
                     currentBudget = recommendation.suggestedBudget;
                 }

                 // Perform the check if budget is set and positive
                 if (currentBudget != null && currentBudget > 0) {
                     double eightyPercentBudget = currentBudget * 0.8;
                     if (totalExpenseThisMonth >= eightyPercentBudget) {
                         double remainingBudget = currentBudget - totalExpenseThisMonth;
                         // Ensure the warning shows on the Event Dispatch Thread
                         SwingUtilities.invokeLater(() -> {
                             JOptionPane.showMessageDialog(
                                     this,
                                     String.format("Budget Warning: You have used $%.2f (%.1f%%) of your $%.2f budget this month.\n" +
                                                     "Remaining budget: $%.2f",
                                             totalExpenseThisMonth, (totalExpenseThisMonth/currentBudget)*100, currentBudget, remainingBudget),
                                     "Budget Warning (80% Reached)",
                                     JOptionPane.WARNING_MESSAGE
                             );
                         });
                         budgetExceeded80Percent = true; // Set flag to prevent repeated warnings this session
                     }
                 }
            } catch (Exception e) {
                 System.err.println("Error checking budget threshold: " + e.getMessage());
                 // Avoid showing error popups for background tasks unless critical
            }
        }
         // Reset the flag if the month changes? Or maybe on logout/login?
         // For now, it resets when a new PersonalUI instance is created (new login).
    }


    /**
    * Performs an initial check when the user logs in to see if their budget
    * was already exceeded (or past 80%) at the time of login.
    * This is similar to checkBudgetThreshold but runs once on init.
    * @param username The username of the logged-in user.
    */
    private void checkBudgetExceededOnLogin(String username) {
         // This logic is very similar to checkBudgetThreshold.
         // Consider refactoring to avoid duplication if the checks are identical.
        try {
            List<BudgetAdvisor.Transaction> transactionsThisMonth = getTransactionsForCurrentMonth(username);
            LocalDate now = LocalDate.now();
            LocalDate startOfMonth = now.withDayOfMonth(1);
            LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

            double totalExpenseThisMonth = transactionsThisMonth.stream()
                    .filter(t -> (t.operation.equalsIgnoreCase("Transfer Out") || t.operation.equalsIgnoreCase("Withdrawal")) &&
                             !t.date.isBefore(startOfMonth) && !t.date.isAfter(endOfMonth))
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
                    // Check if already fully exceeded
                    if (totalExpenseThisMonth >= currentBudget) {
                        double overBudgetBy = totalExpenseThisMonth - currentBudget;
                         SwingUtilities.invokeLater(() -> { // Ensure UI update is on EDT
                             JOptionPane.showMessageDialog(
                                 this,
                                 String.format("Budget Alert: You have already exceeded your budget of $%.2f for this month by $%.2f.",
                                         currentBudget, overBudgetBy),
                                 "Budget Exceeded",
                                 JOptionPane.WARNING_MESSAGE
                             );
                         });
                    } else {
                         // Just past 80%, show the standard warning
                        double remainingBudget = currentBudget - totalExpenseThisMonth;
                         SwingUtilities.invokeLater(() -> { // Ensure UI update is on EDT
                            JOptionPane.showMessageDialog(
                                    this,
                                    String.format("Budget Warning: You are close to exceeding your budget this month.\n" +
                                                  "You have $%.2f left in your $%.2f budget.", remainingBudget, currentBudget),
                                    "Budget Warning",
                                    JOptionPane.WARNING_MESSAGE
                            );
                         });
                    }
                    budgetExceeded80Percent = true; // Set flag so timer doesn't immediately warn again
                }
            }
         } catch (Exception e) {
             System.err.println("Error checking budget on login: " + e.getMessage());
         }
    }


    /**
     * Retrieves all transactions for the specified user within the current calendar month.
     * Reads from the DESTINATION_TRANSACTIONS_FILE.
     * @param username The username to filter transactions for.
     * @return A list of Transaction objects for the user in the current month.
     */
     private List<BudgetAdvisor.Transaction> getTransactionsForCurrentMonth(String username) {
         List<BudgetAdvisor.Transaction> userTransactions = new ArrayList<>();
         LocalDate now = LocalDate.now();
         LocalDate startOfMonth = now.withDayOfMonth(1);
         LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
         File transactionFile = new File(DESTINATION_TRANSACTIONS_FILE);

         if (!transactionFile.exists()) {
             System.err.println("Transaction file not found: " + DESTINATION_TRANSACTIONS_FILE);
             return userTransactions; // Return empty list
         }

         try (BufferedReader br = new BufferedReader(new FileReader(transactionFile))) {
             String line;
             br.readLine(); // Skip header row
             while ((line = br.readLine()) != null) {
                 String[] data = line.split(",");
                 // Basic validation: check array length and username match
                 if (data.length >= 5 && data[0].trim().equalsIgnoreCase(username)) { // Case-insensitive username match?
                     try {
                         // Use BudgetAdvisor's date formatter for consistency
                         LocalDate transactionDate = LocalDate.parse(data[3].trim().split(" ")[0], BudgetAdvisor.DATE_FORMATTER);

                         // Check if the transaction date falls within the current month
                         if (!transactionDate.isBefore(startOfMonth) && !transactionDate.isAfter(endOfMonth)) {
                             String operation = data[1].trim();
                             double amount = Double.parseDouble(data[2].trim());
                             String details = data[4].trim(); // Assuming details are at index 4

                             // Create BudgetAdvisor.Transaction object
                             BudgetAdvisor.Transaction transaction = new BudgetAdvisor.Transaction();
                             transaction.username = username; // Store username? BudgetAdvisor likely handles this internally
                             transaction.operation = operation;
                             transaction.amount = amount;
                             transaction.date = transactionDate;
                             transaction.details = details;
                             // Add transaction time if available and needed
                             // transaction.dateTime = LocalDateTime.parse(data[3].trim(), DATE_TIME_FORMATTER);

                             userTransactions.add(transaction);
                         }
                     } catch (DateTimeParseException e) {
                         System.err.println("Error parsing date in transaction line: " + line + " - " + e.getMessage());
                     } catch (NumberFormatException e) {
                         System.err.println("Error parsing amount in transaction line: " + line + " - " + e.getMessage());
                     } catch (ArrayIndexOutOfBoundsException e) {
                          System.err.println("Error parsing line (missing fields?): " + line + " - " + e.getMessage());
                     }
                 }
             }
         } catch (IOException e) {
             System.err.println("Error reading transaction file '" + DESTINATION_TRANSACTIONS_FILE + "': " + e.getMessage());
             // Maybe show an error to the user in critical cases
         }
         return userTransactions;
     }


    // --- Methods called by SidebarPanel or other components ---

    /**
     * Displays the income/expense pie chart window. Checks login status first.
     */
    public void showIncomeExpenseChart() {
        System.out.println("Attempting to show IncomeExpenseChart..."); // Debug
        checkLoginAndShowDialog(() -> {
             // Assuming IncomeExpenseChart.showIncomeExpensePieChart is static
             // and handles potential file reading errors itself.
            IncomeExpenseChart.showIncomeExpensePieChart(DESTINATION_TRANSACTIONS_FILE);
        });
    }

    /**
     * Checks if user is logged in before showing a specific card panel.
     * If not logged in, shows an error message.
     * @param cardName The name of the card panel to show in the CardLayout.
     */
    public void checkLoginAndShowCard(String cardName) {
        if (UserSession.getCurrentUsername() != null) {
            System.out.println("Switching to card: " + cardName); // Debugging
            cardLayout.show(contentPanel, cardName);
        } else {
            System.out.println("Login required to show card: " + cardName); // Debugging
            UIUtils.showLoginError(this); // Use the utility method
        }
    }

    /**
     * Checks if user is logged in before executing an action (typically showing a dialog).
     * The action is executed on the Event Dispatch Thread.
     * If not logged in, shows an error message.
     * @param action A Runnable containing the action to perform (e.g., creating and showing a dialog).
     */
    public void checkLoginAndShowDialog(Runnable action) {
        if (UserSession.getCurrentUsername() != null) {
            // Ensure the action (dialog creation/display) happens on the EDT
            SwingUtilities.invokeLater(action);
        } else {
            System.out.println("Login required to perform this action."); // Debugging
            UIUtils.showLoginError(this); // Use the utility method
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
            budgetExceeded80Percent = false; // Reset budget warning flag on logout
            dispose(); // Close this PersonalUI window

            // Optional: Logic to return to the login screen (App)
            // This depends heavily on how your main application (App) is structured.
            // Example: If the main App frame is accessible statically or via a context object:
            // MainAppFrame.getInstance().setVisible(true);
            // Or create a new login instance if the old one was disposed:
             SwingUtilities.invokeLater(() -> new App().setVisible(true)); // Assuming App is your main login class
        }
    }

     /**
      * Helper method to show a specific card by name. Primarily for internal use or simple cases.
      * Does not perform login check.
      * @param cardName The name of the card to show.
      */
     public void showCard(String cardName) {
         cardLayout.show(contentPanel, cardName);
     }


    // Optional helper to find the parent App frame if needed after logout
    /*
    private Frame findParentAppFrame() {
        Container parent = getParent();
        while (parent != null && !(parent instanceof Frame)) {
            parent = parent.getParent();
        }
        // This might return null if the dialog wasn't added to a Frame
        return (Frame) parent;
    }
    */

    // Assumed Utility Class (Must exist elsewhere in your project)
    // You might need to adjust the package/location based on your project structure
    private static class UIUtils {
        public static void showLoginError(Component parentComponent) {
            JOptionPane.showMessageDialog(parentComponent,
                    "You must be logged in to access this feature.",
                    "Login Required", JOptionPane.ERROR_MESSAGE);
        }
    }
     // Assuming App class exists for the logout redirection
     // You might need to import it properly
     private static class App extends JFrame {
          // Dummy implementation for compilation
          public App() { /* ... login screen setup ... */ }
     }

     // Assuming TransactionHistory class exists
     // You might need to import it properly
     private static class TransactionHistory extends JDialog {
          // Dummy implementation for compilation
          public TransactionHistory(Window owner) { super(owner); /* ... history setup ... */ }
     }


} // End of PersonalUI class
