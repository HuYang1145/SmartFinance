package AccountModel;

// Import TransactionService and its DTO
import AccountModel.TransactionService;
import AccountModel.TransactionService.TransactionData;

import java.io.BufferedReader; // Keep for reading budget file
import java.io.File;
import java.io.FileReader;   // Keep for reading budget file
import java.io.FileWriter;   // Keep for writing budget file
import java.io.IOException;  // Keep for budget file IO
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
// Import needed formatters and exception types
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.LocalDateTime; // Use LocalDateTime for parsing time part
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // Keep if used elsewhere, or remove

import javax.swing.JOptionPane;

/**
 * Provides budget recommendations and calculations based on transaction history
 * obtained via TransactionService. Manages custom user budgets stored in user_budget.csv.
 */
public class BudgetAdvisor {
    private static final String BUDGET_FILE = "user_budget.csv"; // File for custom budgets
    // TRANSACTIONS_FILE constant is no longer needed for direct reading

    // Define the formatter consistent with TransactionService/TransactionModel timestamps
    public static final DateTimeFormatter TRANSACTION_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    // Constants for budget logic
    private static final double DEFAULT_SAVING_RATIO = 0.2;
    private static final double ECONOMICAL_SAVING_INCREASE = 0.1;
    private static final double LARGE_TRANSACTION_THRESHOLD = 1000;
    private static final int LARGE_TRANSACTION_COUNT_THRESHOLD = 3; // Threshold for number of large expenses
    private static final int LEARNING_MONTHS = 3; // Number of past months to analyze for patterns

    // Cache for custom budgets read from BUDGET_FILE
    private static Map<String, Double> cachedCustomBudgets = new HashMap<>();

    /**
     * Represents the result of a budget calculation.
     */
    public static class BudgetRecommendation {
        public final BudgetMode mode;
        public final double suggestedBudget;
        public final double suggestedSaving;
        public final String reason;
        public final boolean hasPastData; // Flag indicating if enough historical data was available

        public BudgetRecommendation(BudgetMode mode, double budget, double saving, String reason, boolean hasPastData) {
            this.mode = mode;
            this.suggestedBudget = budget;
            this.suggestedSaving = saving;
            this.reason = reason;
            this.hasPastData = hasPastData;
        }

        @Override
        public String toString() { // Helpful for debugging
             return String.format("BudgetRecommendation[Mode=%s, Budget=%.2f, Saving=%.2f, HasPastData=%b, Reason=%s]",
                                  mode, suggestedBudget, suggestedSaving, hasPastData, reason);
        }
    }

    /**
     * Represents the different budget modes determined by the advisor.
     */
    public enum BudgetMode {
        NORMAL("Normal Mode", "Consumption is stable and predictable."),
        ECONOMICAL_UNSTABLE("Economical Mode", "Recent spending patterns were unstable."),
        ECONOMICAL_FESTIVAL("Economical Mode", "Prepare for potential increased spending next month (shopping festival)."),
        CUSTOM("Custom Mode", "Following your defined budget goal.");
        // Add more modes if needed

        private final String displayName;
        private final String reason;

        BudgetMode(String name, String reason) { this.displayName = name; this.reason = reason; }
        public String getDisplayName() { return displayName; }
        public String getReason() { return reason; }
    }

    /**
     * Calculates a budget recommendation for the current user and date.
     * Considers custom budgets first, then determines a mode and calculates
     * suggested budget and savings based on transaction history via TransactionService.
     *
     * @param currentUser The username of the user.
     * @param now The current date (used for monthly calculations).
     * @return A BudgetRecommendation object. Returns default/fallback if data is unavailable.
     */
    public static BudgetRecommendation calculateRecommendation(String currentUser, LocalDate now) {
        if (currentUser == null || currentUser.trim().isEmpty()) {
             System.err.println("ERROR: BudgetAdvisor - Cannot calculate recommendation for null or empty user.");
             // Return a default/error state recommendation
             return new BudgetRecommendation(BudgetMode.NORMAL, 0, 0, "User not specified.", false);
        }

        // 1. Check for Custom Budget
        Double customBudget = loadCustomBudget(currentUser);
        if (customBudget != null && customBudget >= 0) {
            // If custom budget exists, calculate income to estimate potential saving against it
            double totalIncomeThisMonth = calculateTotalIncomeForMonth(currentUser, now); // Uses TransactionService
            System.out.println("DEBUG: BudgetAdvisor - Using custom budget: " + customBudget + " for user: " + currentUser);
            return new BudgetRecommendation(BudgetMode.CUSTOM, customBudget, Math.max(0, totalIncomeThisMonth - customBudget), BudgetMode.CUSTOM.getReason(), false);
        }

        System.out.println("DEBUG: BudgetAdvisor - Calculating recommended budget for user: " + currentUser);
        // 2. Determine Budget Mode (based on spending stability and upcoming events)
        BudgetMode mode = determineBudgetMode(currentUser, now); // Uses TransactionService via helper

        // 3. Calculate Income and check historical data
        double totalIncomeThisMonth = calculateTotalIncomeForMonth(currentUser, now); // Uses TransactionService
        boolean hasPastData = hasSufficientPastData(currentUser, now); // Uses TransactionService via helper

        // 4. Calculate Budget and Saving based on mode
        double suggestedBudget;
        double suggestedSaving;

        if (mode == BudgetMode.ECONOMICAL_UNSTABLE || mode == BudgetMode.ECONOMICAL_FESTIVAL) {
            suggestedBudget = calculateEconomicalBudget(totalIncomeThisMonth);
        } else { // Normal Mode
            suggestedBudget = calculateNormalBudget(currentUser, now, totalIncomeThisMonth, hasPastData);
        }
        // Ensure budget isn't negative (e.g., if income is zero)
        suggestedBudget = Math.max(0, suggestedBudget);
        // Calculate suggested saving based on calculated income and budget
        suggestedSaving = Math.max(0, totalIncomeThisMonth - suggestedBudget);

        System.out.println("DEBUG: BudgetAdvisor - Recommendation: Mode=" + mode + ", Budget=" + suggestedBudget + ", Saving=" + suggestedSaving + ", Income=" + totalIncomeThisMonth + ", HasPast=" + hasPastData);
        return new BudgetRecommendation(mode, suggestedBudget, suggestedSaving, mode.getReason(), hasPastData);
    }

    // --- Helper Methods using TransactionService ---

    private static BudgetMode determineBudgetMode(String currentUser, LocalDate now) {
        // Check spending stability first
        if (hasUnstableSpendingLastMonth(currentUser, now)) { // Uses TransactionService
            return BudgetMode.ECONOMICAL_UNSTABLE;
        }
        // Check for upcoming shopping festivals
        if (isShoppingFestivalMonth(now.plusMonths(1))) {
            return BudgetMode.ECONOMICAL_FESTIVAL;
        }
        // Default to Normal mode
        return BudgetMode.NORMAL;
    }

    // Checks if next month is a designated shopping month
    private static boolean isShoppingFestivalMonth(LocalDate date) {
        Month month = date.getMonth();
        // Example shopping months
        return month == Month.MARCH || month == Month.JUNE || month == Month.NOVEMBER || month == Month.DECEMBER;
    }

    // Calculates budget for Normal mode, using past consumption ratio if available
    private static double calculateNormalBudget(String currentUser, LocalDate now, double totalIncomeThisMonth, boolean hasPastData) {
        double consumptionRatio;
        if (hasPastData) {
            consumptionRatio = calculateAverageConsumptionRatio(currentUser, now); // Uses TransactionService
            System.out.println("DEBUG: BudgetAdvisor - Using average consumption ratio: " + consumptionRatio);
        } else {
            consumptionRatio = (1 - DEFAULT_SAVING_RATIO); // Default if no history
             System.out.println("DEBUG: BudgetAdvisor - Using default consumption ratio: " + consumptionRatio);
        }

        // Ensure ratio is reasonable (e.g., between 0 and 1, or apply other logic)
        if (consumptionRatio <= 0 || consumptionRatio > 1.5) { // Example bounds check
             System.out.println("DEBUG: BudgetAdvisor - Consumption ratio out of bounds (" + consumptionRatio + "), using fallback.");
             consumptionRatio = (1 - DEFAULT_SAVING_RATIO); // Use default if calculated ratio is strange
        }

        return totalIncomeThisMonth * consumptionRatio;
    }

    // Calculates budget for Economical modes (simply increases saving ratio)
    private static double calculateEconomicalBudget(double totalIncomeThisMonth) {
        double savingRatio = DEFAULT_SAVING_RATIO + ECONOMICAL_SAVING_INCREASE;
         System.out.println("DEBUG: BudgetAdvisor - Using economical saving ratio: " + savingRatio);
        return totalIncomeThisMonth * (1 - savingRatio);
    }

    // Calculates average expense-to-income ratio over the last LEARNING_MONTHS
    private static double calculateAverageConsumptionRatio(String currentUser, LocalDate now) {
        double totalRatioSum = 0;
        int monthsCounted = 0;

        for (int i = 1; i <= LEARNING_MONTHS; i++) {
            LocalDate monthDate = now.minusMonths(i);
            // Get income and expense for that specific month using helpers
            double monthlyIncome = calculateTotalIncomeForMonth(currentUser, monthDate); // Uses TransactionService
            double monthlyExpense = calculateTotalExpenseForMonth(currentUser, monthDate); // Uses TransactionService

            // Only include month if there was income to calculate a ratio
            if (monthlyIncome > 0) {
                totalRatioSum += (monthlyExpense / monthlyIncome);
                monthsCounted++;
                 System.out.println("DEBUG: BudgetAdvisor - Ratio for " + monthDate.getMonth() + ": " + (monthlyExpense / monthlyIncome));
            } else {
                 System.out.println("DEBUG: BudgetAdvisor - Skipping ratio calc for " + monthDate.getMonth() + " (Income <= 0)");
            }
        }

        if (monthsCounted > 0) {
            return totalRatioSum / monthsCounted;
        } else {
             System.out.println("DEBUG: BudgetAdvisor - No valid past months found for ratio calculation.");
            return 0; // Or return default ratio (1 - DEFAULT_SAVING_RATIO)
        }
    }

    // Checks if there is income data for the required number of past months
    private static boolean hasSufficientPastData(String currentUser, LocalDate now) {
        int validMonths = 0;
        for (int i = 1; i <= LEARNING_MONTHS; i++) {
            LocalDate monthDate = now.minusMonths(i);
            // Check income for the month using the helper
            if (calculateTotalIncomeForMonth(currentUser, monthDate) > 0) { // Uses TransactionService
                validMonths++;
            }
        }
        return validMonths >= LEARNING_MONTHS; // Check if we have data for ALL required months
    }

    // Checks for unstable spending (multiple large expenses) in the last month
    private static boolean hasUnstableSpendingLastMonth(String currentUser, LocalDate now) {
        LocalDate lastMonthStart = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEnd = now.withDayOfMonth(1).minusDays(1); // End of last month

        List<TransactionData> allUserTransactions = TransactionService.readTransactions(currentUser);
        if (allUserTransactions == null) return false; // Service failed or no transactions

        int largeExpenseCount = 0;
        for (TransactionData tx : allUserTransactions) {
            if ("Expense".equalsIgnoreCase(tx.getOperation())) { // Check for Expense type
                try {
                    LocalDateTime transactionDateTime = LocalDateTime.parse(tx.getTime().trim(), TRANSACTION_TIME_FORMATTER);
                    LocalDate transactionDate = transactionDateTime.toLocalDate();

                    // Check if date is within the last month
                    if (!transactionDate.isBefore(lastMonthStart) && !transactionDate.isAfter(lastMonthEnd)) {
                        // Check if amount exceeds threshold
                        if (tx.getAmount() > LARGE_TRANSACTION_THRESHOLD) {
                            largeExpenseCount++;
                        }
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("BudgetAdvisor (Unstable Check): Skipping transaction due to date parse error: " + tx.getTime());
                } catch (Exception e) {
                     System.err.println("BudgetAdvisor (Unstable Check): Error processing transaction: " + tx + " | Error: " + e.getMessage());
                }
            }
        }
        System.out.println("DEBUG: BudgetAdvisor - Large expenses last month: " + largeExpenseCount);
        // Check if the count meets the threshold
        return largeExpenseCount >= LARGE_TRANSACTION_COUNT_THRESHOLD;
    }

    // --- Calculates total income for a given month using TransactionService ---
    private static double calculateTotalIncomeForMonth(String currentUser, LocalDate date) {
        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        LocalDate lastDayOfMonth = date.withDayOfMonth(date.lengthOfMonth());
        double totalIncome = 0.0;

        List<TransactionData> allUserTransactions = TransactionService.readTransactions(currentUser);
         if (allUserTransactions == null) return 0.0; // Handle service error

        for (TransactionData tx : allUserTransactions) {
            if ("Income".equalsIgnoreCase(tx.getOperation())) { // Check for Income type
                try {
                    LocalDateTime transactionDateTime = LocalDateTime.parse(tx.getTime().trim(), TRANSACTION_TIME_FORMATTER);
                    LocalDate transactionDate = transactionDateTime.toLocalDate();
                    if (!transactionDate.isBefore(firstDayOfMonth) && !transactionDate.isAfter(lastDayOfMonth)) {
                        totalIncome += tx.getAmount();
                    }
                } catch (DateTimeParseException e) {
                     System.err.println("BudgetAdvisor (Income Calc): Skipping transaction due to date parse error: " + tx.getTime());
                } catch (Exception e) {
                     System.err.println("BudgetAdvisor (Income Calc): Error processing transaction: " + tx + " | Error: " + e.getMessage());
                }
            }
        }
        return totalIncome;
    }

    // --- Calculates total expense for a given month using TransactionService ---
    private static double calculateTotalExpenseForMonth(String currentUser, LocalDate date) {
        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        LocalDate lastDayOfMonth = date.withDayOfMonth(date.lengthOfMonth());
        double totalExpense = 0.0;

        List<TransactionData> allUserTransactions = TransactionService.readTransactions(currentUser);
         if (allUserTransactions == null) return 0.0; // Handle service error

        for (TransactionData tx : allUserTransactions) {
            if ("Expense".equalsIgnoreCase(tx.getOperation())) { // Check for Expense type
                try {
                    LocalDateTime transactionDateTime = LocalDateTime.parse(tx.getTime().trim(), TRANSACTION_TIME_FORMATTER);
                    LocalDate transactionDate = transactionDateTime.toLocalDate();
                    if (!transactionDate.isBefore(firstDayOfMonth) && !transactionDate.isAfter(lastDayOfMonth)) {
                        totalExpense += tx.getAmount(); // Amount is positive expense value
                    }
                } catch (DateTimeParseException e) {
                     System.err.println("BudgetAdvisor (Expense Calc): Skipping transaction due to date parse error: " + tx.getTime());
                } catch (Exception e) {
                     System.err.println("BudgetAdvisor (Expense Calc): Error processing transaction: " + tx + " | Error: " + e.getMessage());
                }
            }
        }
        return totalExpense;
    }

    // --- Custom Budget File Handling (Remains the same, operates on user_budget.csv) ---

    private static Double loadCustomBudget(String username) {
        if (cachedCustomBudgets.containsKey(username)) {
            return cachedCustomBudgets.get(username);
        }
        // Ensure file exists and potentially has headers if needed
        ensureBudgetFileExists(); // Add helper to ensure file exists

        try (BufferedReader br = new BufferedReader(new FileReader(BUDGET_FILE))) {
            String line;
            // Optional: Skip header if your budget file has one
            // br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1); // Allow empty budget value if needed
                if (parts.length == 2 && parts[0].trim().equals(username)) {
                    try {
                        // Handle potential empty string for budget value if allowed
                        if (parts[1].trim().isEmpty()) {
                             System.out.println("DEBUG: BudgetAdvisor - Empty budget value found for user: " + username);
                             return null; // Or handle as 0? Define behavior.
                        }
                        double budget = Double.parseDouble(parts[1].trim());
                        cachedCustomBudgets.put(username, budget);
                        return budget;
                    } catch (NumberFormatException e) {
                         System.err.println("Error parsing budget amount for user " + username + " in file: " + line);
                         // Decide whether to return null or skip the line
                         return null;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading budget file: " + e.getMessage());
        }
        return null; // Return null if user not found or error occurred
    }

    public static void saveCustomBudget(String username, double budget) {
        ensureBudgetFileExists(); // Ensure file exists before writing
        // Update cache first
        cachedCustomBudgets.put(username, budget);
        // Read existing budgets to rewrite the file (safer than assuming cache is complete)
        Map<String, Double> allBudgets = loadAllBudgets();
        allBudgets.put(username, budget); // Ensure the new/updated budget is in the map

        // Rewrite the entire file
        try (FileWriter fw = new FileWriter(BUDGET_FILE, false); // false = Overwrite
             java.io.BufferedWriter bw = new java.io.BufferedWriter(fw)) {
            // Optional: Write header if needed
            // bw.write("username,budget"); bw.newLine();
            for (Map.Entry<String, Double> entry : allBudgets.entrySet()) {
                bw.write(entry.getKey() + "," + String.format("%.2f", entry.getValue()));
                bw.newLine();
            }
             System.out.println("DEBUG: BudgetAdvisor - Saved custom budget for " + username);
        } catch (IOException e) {
            System.err.println("Error writing to budget file: " + e.getMessage());
            // Optionally inform user
            JOptionPane.showMessageDialog(null, "Error saving custom budget.", "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void clearCustomBudget(String username) {
        ensureBudgetFileExists();
        // Update cache
        cachedCustomBudgets.remove(username);
         // Read existing budgets, remove the user, and rewrite
         Map<String, Double> allBudgets = loadAllBudgets();
         allBudgets.remove(username); // Remove the user's entry

        // Rewrite the file without the cleared user
        try (FileWriter fw = new FileWriter(BUDGET_FILE, false);
             java.io.BufferedWriter bw = new java.io.BufferedWriter(fw)) {
             // Optional: Write header
             // bw.write("username,budget"); bw.newLine();
            for (Map.Entry<String, Double> entry : allBudgets.entrySet()) {
                bw.write(entry.getKey() + "," + String.format("%.2f", entry.getValue()));
                bw.newLine();
            }
             System.out.println("DEBUG: BudgetAdvisor - Cleared custom budget for " + username);
        } catch (IOException e) {
            System.err.println("Error writing to budget file when clearing: " + e.getMessage());
             JOptionPane.showMessageDialog(null, "Error clearing custom budget.", "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Public getter remains the same
    public static Double getCustomBudget(String username) {
        return loadCustomBudget(username);
    }

    // Helper to load all budgets from file (used for save/clear)
    private static Map<String, Double> loadAllBudgets() {
         Map<String, Double> allBudgets = new HashMap<>();
         ensureBudgetFileExists();
         try (BufferedReader br = new BufferedReader(new FileReader(BUDGET_FILE))) {
            String line;
            // Optional: Skip header
            // br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length == 2 && !parts[0].trim().isEmpty()) {
                     try {
                         if (!parts[1].trim().isEmpty()) {
                             allBudgets.put(parts[0].trim(), Double.parseDouble(parts[1].trim()));
                         }
                    } catch (NumberFormatException e) {
                         System.err.println("Skipping budget line due to parse error: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading all budgets: " + e.getMessage());
        }
        return allBudgets;
    }

     // Helper to ensure budget file exists (similar to ensureFileExists in AdminUI/TransactionService)
    private static void ensureBudgetFileExists() {
        File file = new File(BUDGET_FILE);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    System.out.println("Created new file: " + BUDGET_FILE);
                    // Optional: Write header if your budget file needs one
                    // try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                    //     bw.write("username,budget");
                    //     bw.newLine();
                    // }
                }
            } catch (IOException e) {
                System.err.println("Error creating budget file " + BUDGET_FILE + ": " + e.getMessage());
            }
        }
    }

    // Remove the old inner Transaction class - use TransactionData from TransactionService instead
    // public static class Transaction { ... }

} // End of BudgetAdvisor class