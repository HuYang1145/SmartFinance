package UI;

// Necessary imports from Model and standard libraries
import AccountModel.BudgetAdvisor;
import AccountModel.BudgetAdvisor.BudgetRecommendation; // Import the inner class
import AccountModel.TransactionService;
import AccountModel.TransactionService.TransactionData;
import AccountModel.UserSession; // Needed to get username implicitly or explicitly if needed

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JPanel displaying budget advice, spending status, and modification options.
 * Uses BudgetAdvisor and TransactionService.
 */
public class BudgetManagementPanel extends JPanel {

    // Instance variables for labels to allow easy refreshing
    private JLabel topCategory1Label;
    private JLabel topCategory2Label;
    private JLabel budgetGoalLabel;
    private JLabel savingGoalLabel;
    private JLabel modeLabel;
    private JLabel spentLabel;
    private JLabel remainingLabel;
    private String username; // Store the username for this panel instance

    // Formatter for parsing transaction times
    private static final DateTimeFormatter TRANSACTION_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    public BudgetManagementPanel(String username) {
        this.username = username; // Store the username

        if (this.username == null || this.username.trim().isEmpty()) {
            // Handle case where panel is created without a valid user
            setLayout(new BorderLayout());
            add(new JLabel("User not logged in.", SwingConstants.CENTER), BorderLayout.CENTER);
            return;
        }

        initComponents();
        layoutComponents();
        loadBudgetData(); // Load initial data
    }

    // Initialize UI components (Labels)
    private void initComponents() {
        // Initialize labels with placeholder text
        topCategory1Label = new JLabel("Top Spending 1: N/A");
        topCategory2Label = new JLabel("Top Spending 2: N/A");
        budgetGoalLabel = new JLabel("Budget Goal: Loading...");
        savingGoalLabel = new JLabel("Saving Goal: Loading...");
        modeLabel = new JLabel("Budget Mode: Loading...");
        spentLabel = new JLabel("Spent This Month: Loading...");
        remainingLabel = new JLabel("Remaining Budget: Loading...");
        remainingLabel.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Make remaining bold

        // Apply common font
        Font infoFont = new Font("Segoe UI", Font.PLAIN, 14);
        topCategory1Label.setFont(infoFont);
        topCategory2Label.setFont(infoFont);
        budgetGoalLabel.setFont(infoFont);
        savingGoalLabel.setFont(infoFont);
        modeLabel.setFont(infoFont);
        spentLabel.setFont(infoFont);
    }

    // Layout the components on the panel
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30)); // Padding

        // Title
        JLabel title = new JLabel("Budget Management & Advice", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(50, 50, 50));
        add(title, BorderLayout.NORTH);

        // Content Area using GridBagLayout
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE; // Stack vertically
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 5, 10, 5); // Vertical spacing increased
        gbc.anchor = GridBagConstraints.NORTHWEST; // Align components top-left
        gbc.fill = GridBagConstraints.HORIZONTAL; // Fill horizontally
        gbc.weightx = 1.0; // Allow horizontal expansion

        // --- Create and add sections using the initialized labels ---
        JPanel topSpendingPanel = createSectionPanel("Top Spending This Month");
        topSpendingPanel.add(topCategory1Label);
        topSpendingPanel.add(topCategory2Label);
        content.add(topSpendingPanel, gbc);

        JPanel goalsPanel = createSectionPanel("Monthly Goals & Mode");
        goalsPanel.add(budgetGoalLabel);
        goalsPanel.add(savingGoalLabel);
        goalsPanel.add(modeLabel);
        content.add(goalsPanel, gbc);

        JPanel statusPanel = createSectionPanel("Spending Status");
        statusPanel.add(spentLabel);
        statusPanel.add(remainingLabel);
        content.add(statusPanel, gbc);

        // --- Add Modify Button ---
        JButton modifyBudgetButton = new JButton("Set/Modify Custom Budget Goal");
        // Use UIUtils if available and preferred, or style directly
        styleBudgetButton(modifyBudgetButton); // Apply styling
        modifyBudgetButton.addActionListener(e -> handleModifyBudget());

        // Add button below status panel with padding
        gbc.fill = GridBagConstraints.NONE; // Don't stretch button
        gbc.anchor = GridBagConstraints.CENTER; // Center button
        gbc.insets = new Insets(25, 5, 10, 5); // Add top margin
        gbc.weighty = 1.0; // Push previous content up
        content.add(modifyBudgetButton, gbc);

        // Add content panel to the main panel's center
        add(content, BorderLayout.CENTER);
    }

    // --- Data Loading Logic (using SwingWorker) ---
    private void loadBudgetData() {
        // Display loading state
        setLabelsLoading();

        // Fetch data in background
        SwingWorker<BudgetDataContainer, Void> worker = new SwingWorker<>() {
            @Override
            protected BudgetDataContainer doInBackground() throws Exception {
                // Ensure username is valid before proceeding
                if (username == null || username.trim().isEmpty()) {
                     throw new IllegalStateException("Username is not set for BudgetManagementPanel.");
                }
                LocalDate today = LocalDate.now();
                BudgetRecommendation recommendation = BudgetAdvisor.calculateRecommendation(username, today);
                List<TransactionData> transactions = TransactionService.readTransactions(username);
                double currentMonthExpense = calculateCurrentMonthExpense(transactions);
                Map<String, Double> topCategories = findTopExpenseCategories(transactions, 2);
                return new BudgetDataContainer(recommendation, currentMonthExpense, topCategories);
            }

            @Override
            protected void done() {
                try {
                    BudgetDataContainer data = get(); // Get result
                    updateLabels(data); // Update UI on EDT
                } catch (Exception e) {
                    System.err.println("ERROR: BudgetPanel - Failed to load or display budget data.");
                    e.printStackTrace();
                    setLabelsError(); // Show error state on labels
                }
            }
        };
        worker.execute();
    }

    // --- Update UI Labels based on loaded data ---
    private void updateLabels(BudgetDataContainer data) {
        BudgetRecommendation recommendation = data.recommendation;
        double currentMonthExpense = data.currentMonthExpense;
        Map<String, Double> topCategories = data.topCategories;

        // Update Top Spending Labels
        if (!topCategories.isEmpty()) {
            int count = 0;
            // Use iterator to ensure order if LinkedHashMap is used
            for (Map.Entry<String, Double> entry : topCategories.entrySet()) {
                count++;
                String text = String.format("%s: ¥%.2f", entry.getKey(), entry.getValue());
                if (count == 1) topCategory1Label.setText("1. " + text);
                if (count == 2) topCategory2Label.setText("2. " + text);
            }
            if (count < 2) topCategory2Label.setText("Top Spending 2: N/A");
            if (count < 1) topCategory1Label.setText("Top Spending 1: N/A"); // Should not happen if !isEmpty
        } else {
            topCategory1Label.setText("Top Spending: No expenses this month.");
            topCategory2Label.setText("");
        }

        // Update Goals & Mode Labels
        String budgetGoalText;
        double budgetToUse = recommendation.suggestedBudget; // Default to recommended

        // Check for custom budget
        Double customBudget = BudgetAdvisor.getCustomBudget(this.username); // Use stored username
        if (customBudget != null) {
            budgetToUse = customBudget; // Use custom budget for remaining calc
            budgetGoalText = String.format("¥%.2f (Custom)", customBudget);
            modeLabel.setText("Budget Mode: Custom");
            modeLabel.setToolTipText("You have set a custom monthly budget.");
        } else {
            // Use recommended budget and mode info
            budgetGoalText = String.format("¥%.2f", recommendation.suggestedBudget);
            modeLabel.setText(String.format("Mode: %s", recommendation.mode.getDisplayName()));
            modeLabel.setToolTipText(recommendation.reason);
        }
        budgetGoalLabel.setText("Budget Goal: " + budgetGoalText);
        // Saving goal is always based on recommendation, regardless of custom budget
        savingGoalLabel.setText(String.format("Suggested Saving: ¥%.2f", recommendation.suggestedSaving));

        // Update Status Labels
        spentLabel.setText(String.format("Spent This Month: ¥%.2f", currentMonthExpense));
        // Calculate remaining based on the budget being used (custom or recommended)
        double remaining = budgetToUse - currentMonthExpense;
        updateRemainingLabel(remaining);

        // Ensure panel redraws
        this.revalidate();
        this.repaint();
    }

    // --- Set labels to loading state ---
     private void setLabelsLoading() {
        String loading = "Loading...";
        topCategory1Label.setText("Top Spending 1: " + loading);
        topCategory2Label.setText("Top Spending 2: " + loading);
        budgetGoalLabel.setText("Budget Goal: " + loading);
        savingGoalLabel.setText("Saving Goal: " + loading);
        modeLabel.setText("Budget Mode: " + loading);
        spentLabel.setText("Spent This Month: " + loading);
        remainingLabel.setText("Remaining Budget: " + loading);
        remainingLabel.setForeground(Color.GRAY); // Indicate loading state visually
    }

    // --- Set labels to error state ---
    private void setLabelsError() {
         String error = "Error loading data";
         topCategory1Label.setText("Top Spending 1: " + error);
         topCategory2Label.setText("");
         budgetGoalLabel.setText("Budget Goal: " + error);
         savingGoalLabel.setText("Saving Goal: " + error);
         modeLabel.setText("Budget Mode: " + error);
         spentLabel.setText("Spent This Month: " + error);
         remainingLabel.setText("Remaining Budget: " + error);
         remainingLabel.setForeground(Color.RED);
    }


   // Inside BudgetManagementPanel.java

private void handleModifyBudget() {
    String username = this.username; // Use the stored username
    if (username == null) {
        System.err.println("ERROR: handleModifyBudget - Username is null.");
        return;
    }

    Double currentCustomDbl = BudgetAdvisor.getCustomBudget(username);
    String currentCustomStr = (currentCustomDbl != null) ? String.format("%.2f", currentCustomDbl) : "";

   // --- MODIFICATION START ---
   // 1. Receive the result as an Object first
   Object result = JOptionPane.showInputDialog(
       this,
       "Enter new monthly budget (¥).\nLeave empty or cancel to clear custom budget:",
       "Set Custom Budget",
       JOptionPane.PLAIN_MESSAGE,
       null,           // Icon
       null,           // Selection values (not used here)
       currentCustomStr // Initial selection value
   );

   // 2. Check if the user cancelled (result will be null)
   if (result != null) {
       // 3. Cast the Object to String since we expect text input here
       String newBudgetStr = (String) result;

       // 4. Process the input string (the rest of your logic)
        if (newBudgetStr.trim().isEmpty()) {
            // User confirmed OK but left it empty -> Clear custom budget
            BudgetAdvisor.clearCustomBudget(this.username);
            JOptionPane.showMessageDialog(this, "Custom budget cleared. Using system recommendation.");
        } else {
            // User entered a value, try to parse and save
            try {
                double newBudget = Double.parseDouble(newBudgetStr.trim());
                if (newBudget >= 0) {
                    BudgetAdvisor.saveCustomBudget(this.username, newBudget);
                    JOptionPane.showMessageDialog(this, "Custom budget set to ¥" + String.format("%.2f", newBudget));
                } else {
                    JOptionPane.showMessageDialog(this, "Budget cannot be negative.", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException nfe) {
                 JOptionPane.showMessageDialog(this, "Invalid number format entered.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        // Refresh the display after potentially changing the budget
        loadBudgetData(); // Reload and update labels
   }
   // If result is null (user cancelled), do nothing.
   // --- MODIFICATION END ---
}

    // --- Helper method to update the remaining/overspent label ---
    private void updateRemainingLabel(double remainingValue) {
        if (remainingValue >= 0) {
            remainingLabel.setText(String.format("Remaining Budget: ¥%.2f", remainingValue));
            remainingLabel.setForeground(new Color(0, 128, 0)); // Green
        } else {
            remainingLabel.setText(String.format("Overspent By: ¥%.2f", Math.abs(remainingValue)));
            remainingLabel.setForeground(Color.RED); // Red
        }
    }

    // --- Helper class to hold data fetched in background ---
    private static class BudgetDataContainer {
        final BudgetRecommendation recommendation;
        final double currentMonthExpense;
        final Map<String, Double> topCategories;

        BudgetDataContainer(BudgetRecommendation rec, double expense, Map<String, Double> top) {
            this.recommendation = rec;
            this.currentMonthExpense = expense;
            this.topCategories = top;
        }
    }

    // --- Helper method to create styled section panels ---
    private JPanel createSectionPanel(String title) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setBackground(new Color(248, 249, 250));
        Border lineBorder = BorderFactory.createLineBorder(new Color(210, 215, 220));
        Border titledBorder = BorderFactory.createTitledBorder(
            lineBorder,
            " " + title + " ",
            TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
            new Font("Segoe UI", Font.BOLD, 15), new Color(70, 80, 90)
        );
        Border paddingBorder = BorderFactory.createEmptyBorder(8, 12, 8, 12);
        sectionPanel.setBorder(new CompoundBorder(titledBorder, paddingBorder));
        sectionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Ensure labels added later also align left
        // For BoxLayout on Y_AXIS, components usually align based on their X alignment property.
        // Setting it on the panel helps if components don't have it set.
        return sectionPanel;
    }

    // --- Helper to calculate current month's expense (remains the same) ---
    private double calculateCurrentMonthExpense(List<TransactionData> transactions) {
         if (transactions == null) return 0.0;
         double totalExpense = 0.0;
         LocalDate now = LocalDate.now();
         for (TransactionData tx : transactions) {
             if ("Expense".equalsIgnoreCase(tx.getOperation())) {
                 try {
                     LocalDateTime dt = LocalDateTime.parse(tx.getTime(), TRANSACTION_TIME_FORMATTER);
                     if (dt.getYear() == now.getYear() && dt.getMonthValue() == now.getMonthValue()) {
                         totalExpense += tx.getAmount();
                     }
                 } catch (DateTimeParseException e) { /* Log error */ }
             }
         }
         return totalExpense;
    }

    // --- Helper to find top N expense categories (remains the same) ---
    private Map<String, Double> findTopExpenseCategories(List<TransactionData> transactions, int topN) {
         if (transactions == null) return new HashMap<>();
         Map<String, Double> categoryTotals = new HashMap<>();
         LocalDate now = LocalDate.now();
         for (TransactionData tx : transactions) {
             if ("Expense".equalsIgnoreCase(tx.getOperation())) {
                  try {
                     LocalDateTime dt = LocalDateTime.parse(tx.getTime(), TRANSACTION_TIME_FORMATTER);
                     if (dt.getYear() == now.getYear() && dt.getMonthValue() == now.getMonthValue()) {
                         String category = tx.getType();
                         if (category == null || category.trim().isEmpty() || "u".equalsIgnoreCase(category.trim())) {
                             category = "Unclassified";
                         } else { category = category.trim(); }
                         categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + tx.getAmount());
                     }
                 } catch (DateTimeParseException e) { /* Log error */ }
             }
         }
         // Sort and limit
         return categoryTotals.entrySet().stream()
                              .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                              .limit(topN)
                              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

     // --- Helper to style buttons (can use UIUtils or style locally) ---
     private void styleBudgetButton(JButton button) {
         button.setFont(new Font("Segoe UI", Font.BOLD, 14));
         button.setForeground(Color.WHITE);
         button.setBackground(new Color(0, 120, 215)); // Blue
         button.setPreferredSize(new Dimension(250, 40)); // Make button wider
         button.setFocusPainted(false);
         button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
         button.setCursor(new Cursor(Cursor.HAND_CURSOR));
     }

} // End of BudgetManagementPanel class