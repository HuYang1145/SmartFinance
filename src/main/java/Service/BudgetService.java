/**
 * Manages budget-related operations, including calculating budgets, tracking expenses, and providing recommendations.
 * Integrates with transaction data to analyze user spending patterns and generate budget suggestions.
 *
 * @author Group 19
 * @version 1.0
 */
package Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import Model.BudgetDataContainer;
import Model.Transaction;
import Repository.TransactionRepository;

public class BudgetService {
    private final TransactionRepository transactionRepository;
    private final Map<String, Double> cachedCustomBudgets = new HashMap<>();
    private final Map<String, Long> cacheTimestamps = new HashMap<>(); // Although not actively used for cache expiry in this class methods, kept from original structure
    private static final String BUDGET_FILE = "user_budget.csv";
    private static final long CACHE_EXPIRY_MS = 5 * 60 * 1000; // Not actively used in this class methods
    private static final double DEFAULT_SAVING_RATIO = 0.2;
    private static final double ECONOMICAL_SAVING_INCREASE = 0.1;
    private static final double LARGE_CONSUMPTION_THRESHOLD_RATIO = 0.07; // Renamed for clarity
    // private static final double LARGE_TRANSACTION_THRESHOLD = 1000; // Not directly used in new checks
    // private static final int LARGE_TRANSACTION_COUNT_THRESHOLD = 3; // Not directly used in new checks
    private static final int LEARNING_MONTHS = 3; // Number of past months for calculating average ratio/expense

    /**
     * Formatter for parsing and formatting transaction dates in the format "yyyy/MM/dd HH:mm".
     */
    public static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy/MM/dd")
            .optionalStart()
            .appendPattern(" HH:mm")
            .optionalEnd()
            .toFormatter();

    /**
     * Formatter for parsing only the date part "yyyy/MM/dd".
     */
     private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");


    /**
     * Constructs a BudgetService instance with a transaction repository dependency.
     *
     * @param transactionRepository the repository for accessing transaction data
     */
    public BudgetService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
         System.out.println("BudgetService initialized with TransactionRepository.");
    }

    /**
     * Calculates the average daily expense for a user over a specified number of past full months.
     * Considers only 'Expense' operations. A "full month" excludes the current partial month.
     *
     * @param username    The username of the user.
     * @param pastMonths  The number of past *full* months to include in the calculation (e.g., 3 for the last 3 completed months).
     * @return The average daily expense over the specified period, or 0.0 if no expenses or no full months included.
     */
    public double calculateAverageDailyExpense(String username, int pastMonths) {
        List<Transaction> transactions = transactionRepository.findTransactionsByUsername(username);
        if (transactions == null || transactions.isEmpty() || pastMonths <= 0) {
            return 0.0;
        }

        double totalExpense = 0.0;
        int totalDays = 0;
        LocalDate today = LocalDate.now();

        for (int i = 1; i <= pastMonths; i++) {
            // Get the date for the *start* of the i-th month before the current month
            LocalDate monthDate = today.minusMonths(i);
            LocalDate monthStartDate = monthDate.withDayOfMonth(1);
            LocalDate monthEndDate = monthDate.withDayOfMonth(monthDate.lengthOfMonth());

            double monthExpense = 0.0;
            for (Transaction tx : transactions) {
                if (tx == null || tx.getOperation() == null || tx.getTimestamp() == null) continue;

                if ("Expense".equalsIgnoreCase(tx.getOperation())) {
                    try {
                        // Parse only date part for comparison
                        String[] dateTimeParts = tx.getTimestamp().split(" ");
                        LocalDate transactionDate = LocalDate.parse(dateTimeParts[0], DATE_ONLY_FORMATTER);
                        if (!transactionDate.isBefore(monthStartDate) && !transactionDate.isAfter(monthEndDate)) {
                            monthExpense += tx.getAmount();
                        }
                    } catch (DateTimeParseException e) {
                        System.err.println("Failed to parse date in calculateAverageDailyExpense for transaction: " + tx.getTimestamp() + " - " + e.getMessage());
                         // Ignore transaction with bad date format
                    } catch (Exception e) {
                         System.err.println("Unexpected error processing transaction in calculateAverageDailyExpense: " + tx.getTimestamp() + " - " + e.getMessage());
                         e.printStackTrace();
                    }
                }
            }
            totalExpense += monthExpense;
            totalDays += (int) ChronoUnit.DAYS.between(monthStartDate, monthEndDate) + 1;
        }

        if (totalDays <= 0 || totalExpense <= 0) {
             return 0.0;
        }

        return totalExpense / totalDays;
    }


    /**
     * Retrieves budget-related data for a user, including income, expenses, and recommendations.
     *
     * @param username the username of the user
     * @param now      the current date for temporal context
     * @return a BudgetDataContainer with budget data and recommendations
     */
    public BudgetDataContainer getBudgetData(String username, LocalDate now) {
        List<Transaction> transactions = transactionRepository.findTransactionsByUsername(username); // Blocking call
        double currentMonthIncome = calculateCurrentMonthIncome(transactions, now);
        double currentMonthExpense = calculateCurrentMonthExpense(transactions, now);
        String topType = findTopExpenseType(transactions, now);
        List<String> largeConsumptions = findLargeConsumptions(transactions, currentMonthIncome, now);
        BudgetRecommendation recommendation = calculateRecommendation(username, now);
        Double customBudget = getCustomBudget(username); // Blocking call
        return new BudgetDataContainer(recommendation, currentMonthExpense, currentMonthIncome, topType, largeConsumptions, customBudget);
    }

    /**
     * Calculates a budget recommendation based on user transaction history and budget mode.
     *
     * @param username the username of the user
     * @param now      the current date for temporal context
     * @return a BudgetRecommendation with suggested budget and savings
     */
    public BudgetRecommendation calculateRecommendation(String username, LocalDate now) {
        Double customBudget = getCustomBudget(username);
        if (customBudget != null && customBudget >= 0) {
            double totalIncomeThisMonth = calculateCurrentMonthIncome(transactionRepository.findTransactionsByUsername(username), now);
            return new BudgetRecommendation(BudgetMode.CUSTOM, customBudget, Math.max(0, totalIncomeThisMonth - customBudget), BudgetMode.CUSTOM.getReason(), false);
        }

        BudgetMode mode = determineBudgetMode(username, now);
        double totalIncomeThisMonth = calculateCurrentMonthIncome(transactionRepository.findTransactionsByUsername(username), now);
        boolean hasPastData = hasSufficientPastData(username, now);
        double suggestedBudget;
        double suggestedSaving;

        if (mode == BudgetMode.ECONOMICAL_UNSTABLE || mode == BudgetMode.ECONOMICAL_FESTIVAL) {
            suggestedBudget = calculateEconomicalBudget(username, now, totalIncomeThisMonth);
            suggestedSaving = Math.max(0, totalIncomeThisMonth - suggestedBudget);
        } else {
            suggestedBudget = calculateNormalBudget(username, now, totalIncomeThisMonth, hasPastData);
            suggestedSaving = Math.max(0, totalIncomeThisMonth - suggestedBudget);
        }

        return new BudgetRecommendation(mode, suggestedBudget, suggestedSaving, mode.getReason(), hasPastData);
    }

    /**
     * Saves a custom budget for a user to the cache and persistent storage.
     *
     * @param username the username of the user
     * @param budget   the custom budget amount
     */
    public void saveCustomBudget(String username, double budget) {
        cachedCustomBudgets.put(username, budget);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(BUDGET_FILE, false))) {
            // Overwrite the file with the current state of the cache
            for (Map.Entry<String, Double> entry : cachedCustomBudgets.entrySet()) {
                bw.write(entry.getKey() + "," + String.format("%.2f", entry.getValue()));
                bw.newLine();
            }
             System.out.println("Saved custom budget for user " + username + ": ¥" + budget);
        } catch (IOException e) {
            System.err.println("Error writing to budget file: " + e.getMessage());
             e.printStackTrace();
        }
    }

    /**
     * Clears a custom budget for a user from the cache and persistent storage.
     *
     * @param username the username of the user
     */
    public void clearCustomBudget(String username) {
        cachedCustomBudgets.remove(username);
         try (BufferedWriter bw = new BufferedWriter(new FileWriter(BUDGET_FILE, false))) {
             // Overwrite the file with the reduced cache state
            for (Map.Entry<String, Double> entry : cachedCustomBudgets.entrySet()) {
                bw.write(entry.getKey() + "," + String.format("%.2f", entry.getValue()));
                bw.newLine();
            }
             System.out.println("Cleared custom budget for user " + username);
        } catch (IOException e) {
            System.err.println("Error writing to budget file when clearing: " + e.getMessage());
             e.printStackTrace();
        }
    }

    /**
     * Retrieves a custom budget for a user from cache or file.
     * Loads all budgets from file into cache if not already in cache.
     *
     * @param username the username of the user
     * @return the custom budget amount, or null if not set
     */
    public Double getCustomBudget(String username) {
        if (cachedCustomBudgets.containsKey(username)) {
            return cachedCustomBudgets.get(username);
        }
         // If not in cache, load all budgets from file into cache
         loadBudgetsFromFile();
         // Then check cache again
         return cachedCustomBudgets.get(username);
    }

     /**
      * Loads all custom budgets from the budget file into the cache.
      * Handles potential file read errors and format errors.
      */
     private void loadBudgetsFromFile() {
         System.out.println("Loading custom budgets from file: " + BUDGET_FILE);
         File budgetFile = new File(BUDGET_FILE);
         if (!budgetFile.exists()) {
             System.out.println("Budget file not found. No custom budgets loaded.");
             return;
         }

         try (BufferedReader br = new BufferedReader(new FileReader(budgetFile))) {
             String line;
             while ((line = br.readLine()) != null) {
                 String[] parts = line.split(",");
                 if (parts.length == 2) {
                     String user = parts[0].trim();
                     try {
                         double budget = Double.parseDouble(parts[1].trim());
                         cachedCustomBudgets.put(user, budget);
                     } catch (NumberFormatException e) {
                         System.err.println("Error parsing budget amount for user " + user + " in file: " + parts[1] + " - " + e.getMessage());
                          // Continue reading other lines
                     }
                 } else if (!line.trim().isEmpty()) {
                      System.err.println("Skipping invalid line in budget file (incorrect field count): " + line);
                 }
             }
             System.out.println("Finished loading custom budgets from file. Loaded " + cachedCustomBudgets.size() + " entries.");
         } catch (IOException e) {
             System.err.println("Error reading budget file: " + e.getMessage());
             e.printStackTrace();
         } catch (Exception e) {
              System.err.println("Unexpected error during budget file loading: " + e.getMessage());
              e.printStackTrace();
         }
     }


    /**
     * Calculates the total income for the current month based on transaction data.
     *
     * @param transactions the list of transactions
     * @param now          the current date for temporal context
     * @return the total income for the current month
     */
    private double calculateCurrentMonthIncome(List<Transaction> transactions, LocalDate now) {
        if (transactions == null) return 0.0;
        double totalIncome = 0.0;
        for (Transaction tx : transactions) {
             if (tx == null || tx.getOperation() == null || tx.getTimestamp() == null) continue;

            if ("Income".equalsIgnoreCase(tx.getOperation())) {
                try {
                    // Parse only date part
                    String[] dateTimeParts = tx.getTimestamp().split(" ");
                    LocalDate transactionDate = LocalDate.parse(dateTimeParts[0], DATE_ONLY_FORMATTER);
                    if (transactionDate.getYear() == now.getYear() && transactionDate.getMonthValue() == now.getMonthValue()) {
                        totalIncome += tx.getAmount();
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("Failed to parse date in calculateCurrentMonthIncome for transaction: " + tx.getTimestamp() + " - " + e.getMessage());
                } catch (Exception e) {
                     System.err.println("Unexpected error processing transaction in calculateCurrentMonthIncome: " + tx.getTimestamp() + " - " + e.getMessage());
                     e.printStackTrace();
                }
            }
        }
        return totalIncome;
    }

    /**
     * Calculates the total expenses for the current month based on transaction data.
     *
     * @param transactions the list of transactions
     * @param now          the current date for temporal context
     * @return the total expenses for the current month
     */
    private double calculateCurrentMonthExpense(List<Transaction> transactions, LocalDate now) {
        if (transactions == null) return 0.0;
        double totalExpense = 0.0;
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        LocalDate lastDayOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        for (Transaction tx : transactions) {
             if (tx == null || tx.getOperation() == null || tx.getTimestamp() == null) continue;

            if ("Expense".equalsIgnoreCase(tx.getOperation())) {
                try {
                    // Parse only date part
                    String[] dateTimeParts = tx.getTimestamp().split(" ");
                    LocalDate transactionDate = LocalDate.parse(dateTimeParts[0], DATE_ONLY_FORMATTER);
                    if (!transactionDate.isBefore(firstDayOfMonth) && !transactionDate.isAfter(lastDayOfMonth)) {
                        totalExpense += tx.getAmount();
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("Failed to parse date in calculateCurrentMonthExpense for transaction: " + tx.getTimestamp() + " - " + e.getMessage());
                } catch (Exception e) {
                     System.err.println("Unexpected error processing transaction in calculateCurrentMonthExpense: " + tx.getTimestamp() + " - " + e.getMessage());
                     e.printStackTrace();
                }
            }
        }
        return totalExpense;
    }

    /**
     * Identifies the top expense category for the current month.
     *
     * @param transactions the list of transactions
     * @param now          the current date for temporal context
     * @return the top expense category, or null if none found
     */
    private String findTopExpenseType(List<Transaction> transactions, LocalDate now) {
        if (transactions == null) return null;
        Map<String, Double> typeTotals = new HashMap<>();
        for (Transaction tx : transactions) {
             if (tx == null || tx.getOperation() == null || tx.getTimestamp() == null) continue;

            if ("Expense".equalsIgnoreCase(tx.getOperation())) {
                try {
                    // Parse only date part
                    String[] dateTimeParts = tx.getTimestamp().split(" ");
                    LocalDate transactionDate = LocalDate.parse(dateTimeParts[0], DATE_ONLY_FORMATTER);
                    if (transactionDate.getYear() == now.getYear() && transactionDate.getMonthValue() == now.getMonthValue()) {
                        String type = tx.getType();
                        if (type == null || type.trim().isEmpty() || "u".equalsIgnoreCase(type.trim())) {
                            type = "Unclassified";
                        } else {
                            type = type.trim();
                        }
                        typeTotals.put(type, typeTotals.getOrDefault(type, 0.0) + tx.getAmount());
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("Failed to parse date in findTopExpenseType for transaction: " + tx.getTimestamp() + " - " + e.getMessage());
                } catch (Exception e) {
                     System.err.println("Unexpected error processing transaction in findTopExpenseType: " + tx.getTimestamp() + " - " + e.getMessage());
                     e.printStackTrace();
                }
            }
        }
        return typeTotals.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Identifies large consumption transactions for the current month based on income threshold.
     * Transactions with an amount greater than 7% of the current month's income are considered large.
     * A minimum threshold of 100 is used if income is zero or results in a very low 7%.
     *
     * @param transactions       the list of transactions
     * @param currentMonthIncome the total income for the current month
     * @param now                the current date for temporal context
     * @return a list of formatted large consumption details
     */
    private List<String> findLargeConsumptions(List<Transaction> transactions, double currentMonthIncome, LocalDate now) {
        if (transactions == null) return new ArrayList<>();
        List<String> largeConsumptions = new ArrayList<>();
        // Calculate the large threshold: 7% of current month's income, with a minimum floor of 100.0 if income is zero/low.
        double largeThreshold = (currentMonthIncome > 0) ? currentMonthIncome * LARGE_CONSUMPTION_THRESHOLD_RATIO : 100.0;
        largeThreshold = Math.max(largeThreshold, 100.0); // Ensure threshold is at least 100


        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        for (Transaction tx : transactions) {
             if (tx == null || tx.getOperation() == null || tx.getTimestamp() == null) continue;

            if ("Expense".equalsIgnoreCase(tx.getOperation())) {
                try {
                    // Parse only date part
                    String[] dateTimeParts = tx.getTimestamp().split(" ");
                    LocalDate transactionDate = LocalDate.parse(dateTimeParts[0], DATE_ONLY_FORMATTER);
                    if (transactionDate.getYear() == now.getYear() && transactionDate.getMonthValue() == now.getMonthValue() && tx.getAmount() > largeThreshold) {
                         // Include time in the description for clarity if available
                         String timePart = dateTimeParts.length > 1 ? " " + dateTimeParts[1] : "";
                        largeConsumptions.add(String.format("%s%s - ¥%.2f - %s", transactionDate.format(displayFormatter), timePart, tx.getAmount(), tx.getType() != null && !tx.getType().trim().isEmpty() ? tx.getType().trim() : "Unspecified Type")); // Include type
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("Failed to parse date in findLargeConsumptions for transaction: " + tx.getTimestamp() + " - " + e.getMessage());
                } catch (Exception e) {
                     System.err.println("Unexpected error processing transaction in findLargeConsumptions: " + tx.getTimestamp() + " - " + e.getMessage());
                     e.printStackTrace();
                }
            }
        }
        return largeConsumptions;
    }

    /**
     * Determines the appropriate budget mode based on spending stability and upcoming events.
     *
     * @param username the username of the user
     * @param now      the current date for temporal context
     * @return the determined BudgetMode
     */
    private BudgetMode determineBudgetMode(String username, LocalDate now) {
        // This logic uses hasUnstableSpendingLastMonth which is defined below and checks withdrawals > 1000
        if (hasUnstableSpendingLastMonth(username, now)) {
            return BudgetMode.ECONOMICAL_UNSTABLE;
        }
        if (isShoppingFestivalMonth(now.plusMonths(1))) {
            return BudgetMode.ECONOMICAL_FESTIVAL;
        }
        return BudgetMode.NORMAL;
    }

    /**
     * Checks if the next month is a shopping festival month (March, June, November, December).
     *
     * @param date the date to check
     * @return true if the month is a shopping festival month, false otherwise
     */
    private boolean isShoppingFestivalMonth(LocalDate date) {
        Month month = date.getMonth();
        return month == Month.MARCH || month == Month.JUNE || month == Month.NOVEMBER || month == Month.DECEMBER;
    }

    /**
     * Calculates a normal budget based on past consumption ratios or default savings.
     *
     * @param username           the username of the user
     * @param now                the current date for temporal context
     * @param totalIncomeThisMonth the total income for the current month
     * @param hasPastData        whether sufficient past data is available to calculate average ratio
     * @return the suggested normal budget
     */
    private double calculateNormalBudget(String username, LocalDate now, double totalIncomeThisMonth, boolean hasPastData) {
        double consumptionRatio = hasPastData ? calculateAverageConsumptionRatio(username, now) : (1 - DEFAULT_SAVING_RATIO); // Use DEFAULT_SAVING_RATIO
        // Ensure suggested budget is not negative
        return Math.max(0, totalIncomeThisMonth * consumptionRatio);
    }

    /**
     * Calculates an economical budget with increased savings for unstable or festival periods.
     *
     * @param username           the username of the user
     * @param now                the current date for temporal context
     * @param totalIncomeThisMonth the total income for the current month
     * @return the suggested economical budget
     */
    private double calculateEconomicalBudget(String username, LocalDate now, double totalIncomeThisMonth) {
        double consumptionRatio = 1 - (DEFAULT_SAVING_RATIO + ECONOMICAL_SAVING_INCREASE); // Increased saving percentage
        // Ensure suggested budget is not negative
        return Math.max(0, totalIncomeThisMonth * consumptionRatio);
    }

    /**
     * Calculates the average consumption ratio (Total Expense / Total Income) over the past three full months with non-zero income.
     * If fewer than LEARNING_MONTHS (3) past full months have non-zero income, the default saving ratio is used.
     *
     * @param username the username of the user
     * @param now      the current date for temporal context
     * @return the average consumption ratio, or default saving ratio if no valid data
     */
    private double calculateAverageConsumptionRatio(String username, LocalDate now) {
        double totalConsumptionRatioSum = 0;
        int validMonthsCount = 0; // Count months with non-zero income and non-zero expense for a valid ratio

        for (int i = 1; i <= LEARNING_MONTHS; i++) { // Check the last LEARNING_MONTHS (3) full months
            LocalDate monthDate = now.minusMonths(i);
            double totalIncome = calculateCurrentMonthIncome(transactionRepository.findTransactionsByUsername(username), monthDate); // Blocking calls
            double totalExpense = calculateCurrentMonthExpense(transactionRepository.findTransactionsByUsername(username), monthDate); // Blocking calls

            // Only consider months with income > 0 for ratio calculation
            if (totalIncome > 0) {
                 // If income > 0, calculate ratio. If expense is also > 0, it's a meaningful ratio month.
                 // If income > 0 but expense is 0, ratio is 0. This is a valid data point.
                 totalConsumptionRatioSum += (totalExpense / totalIncome);
                 validMonthsCount++; // Count this month as valid for the average
            } else if (totalExpense > 0) {
                 // If income is 0 but expense > 0, this month has an undefined/infinite ratio. Skip for averaging.
                 System.out.println("Skipping ratio calculation for month " + monthDate.getYear() + "/" + monthDate.getMonthValue() + " due to zero income but non-zero expense.");
            }
             // If both are zero, it doesn't affect the average ratio or valid count.
        }

        // If we found at least LEARNING_MONTHS (3) months with income > 0, return the average ratio.
        // Otherwise, return the consumption ratio derived from the default saving ratio.
        return (validMonthsCount >= LEARNING_MONTHS) ? totalConsumptionRatioSum / validMonthsCount : (1 - DEFAULT_SAVING_RATIO);
    }

    /**
     * Checks if sufficient past data (specifically LEARNING_MONTHS (3) full months with non-zero income)
     * is available for calculating a meaningful average consumption ratio.
     *
     * @param username the username of the user
     * @param now      the current date for temporal context
     * @return true if sufficient past data is available, false otherwise
     */
    private boolean hasSufficientPastData(String username, LocalDate now) {
        int monthsWithIncome = 0;
        for (int i = 1; i <= LEARNING_MONTHS; i++) { // Check the last LEARNING_MONTHS (3) full months
            LocalDate month = now.minusMonths(i);
            // Check income for that full month (blocking call)
            double totalIncome = calculateCurrentMonthIncome(transactionRepository.findTransactionsByUsername(username), month);
            if (totalIncome > 0) {
                monthsWithIncome++;
            }
        }
        return monthsWithIncome >= LEARNING_MONTHS; // Require income in at least LEARNING_MONTHS (3) months
    }


    /**
     * Checks if the user's spending was unstable last month based on large expense transactions.
     * "Unstable" is defined as having at least 3 expense transactions with an amount greater than 1000 in the last full month.
     * (This is the original logic from BudgetService, kept for budget mode determination)
     *
     * @param username the username of the user
     * @param now      the current date for temporal context
     * @return true if spending was unstable, false otherwise
     */
    private boolean hasUnstableSpendingLastMonth(String username, LocalDate now) {
        // Get the date range for the last *full* month
        LocalDate lastMonthDate = now.minusMonths(1);
        LocalDate lastMonthStart = lastMonthDate.withDayOfMonth(1);
        LocalDate lastMonthEnd = lastMonthDate.withDayOfMonth(lastMonthDate.lengthOfMonth());

        // Get expense amounts for the last full month (blocking call)
        List<Double> lastMonthExpenses = getMonthlyExpensesAmounts(username, lastMonthStart, lastMonthEnd);

        if (lastMonthExpenses.size() >= 3) { // Check if last month has at least 3 expense transactions
            int largeTransactionCount = 0;
            for (double amount : lastMonthExpenses) {
                // Original logic checked against 1000
                if (Math.abs(amount) > 1000) { // Count expenses greater than 1000
                    largeTransactionCount++;
                }
            }
            return largeTransactionCount >= 3; // Original count threshold: at least 3 such large expenses
        }
        return false; // Not unstable if fewer than 3 total expenses or fewer than 3 large expenses
    }

     /**
      * Retrieves a list of expense amounts for a given date range.
      * (Helper for hasUnstableSpendingLastMonth - kept for BudgetMode)
      *
      * @param username the username of the user
      * @param start    the start date of the period (inclusive)
      * @param end      the end date of the period (inclusive)
      * @return a list of expense amounts within the specified date range. Returns empty list if no transactions or transactions list is null.
      */
    private List<Double> getMonthlyExpensesAmounts(String username, LocalDate start, LocalDate end) {
        List<Double> expenses = new ArrayList<>();
        List<Transaction> transactions = transactionRepository.findTransactionsByUsername(username); // Blocking call

        if (transactions == null) return expenses; // Return empty list if no transactions found

        for (Transaction tx : transactions) {
            if (tx == null || tx.getOperation() == null || tx.getTimestamp() == null) continue;

            if ("Expense".equalsIgnoreCase(tx.getOperation())) {
                try {
                    // Parse only date part for comparison
                    String[] dateTimeParts = tx.getTimestamp().split(" ");
                    LocalDate transactionDate = LocalDate.parse(dateTimeParts[0], DATE_ONLY_FORMATTER);

                    if (!transactionDate.isBefore(start) && !transactionDate.isAfter(end)) {
                        expenses.add(tx.getAmount());
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("Failed to parse date in getMonthlyExpensesAmounts for transaction: " + tx.getTimestamp() + " - " + e.getMessage());
                } catch (Exception e) {
                     System.err.println("Unexpected error processing transaction in getMonthlyExpensesAmounts: " + tx.getTimestamp() + " - " + e.getMessage());
                     e.printStackTrace();
                }
            }
        }
        return expenses;
    }


    /**
     * Enum representing different budget modes with associated display names and reasons.
     */
    public enum BudgetMode {
        NORMAL("Normal Mode", "Consumption is stable and predictable now."),
        ECONOMICAL_UNSTABLE("Economical Mode", "Users' spending is unstable."),
        ECONOMICAL_FESTIVAL("Economical Mode", "Next month is the shopping festival."),
        CUSTOM("Custom Mode", "Following your defined budget.");

        private final String displayName;
        private final String reason;

        /**
         * Constructs a BudgetMode with a display name and reason.
         *
         * @param name   the display name of the mode
         * @param reason the reason for selecting this mode
         */
        BudgetMode(String name, String reason) {
            this.displayName = name;
            this.reason = reason;
        }

        /**
         * Gets the display name of the budget mode.
         *
         * @return the display name
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Gets the reason for selecting this budget mode.
         *
         * @return the reason
         */
        public String getReason() {
            return reason;
        }
    }

    /**
     * Represents a budget recommendation with mode, budget, savings, and reason.
     */
    public static class BudgetRecommendation {
        private final BudgetMode mode;
        private final double suggestedBudget;
        private final double suggestedSaving;
        private final String reason;
        private final boolean hasPastData;

        /**
         * Constructs a BudgetRecommendation with the specified parameters.
         *
         * @param mode           the budget mode
         * @param budget         the suggested budget amount
         * @param saving         the suggested savings amount
         * @param reason         the reason for the recommendation
         * @param hasPastData    whether sufficient past data is available for meaningful ratio calculation
         */
        public BudgetRecommendation(BudgetMode mode, double budget, double saving, String reason, boolean hasPastData) {
            this.mode = mode;
            this.suggestedBudget = budget;
            this.suggestedSaving = saving;
            this.reason = reason;
            this.hasPastData = hasPastData;
        }

        /**
         * Gets the budget mode.
         *
         * @return the budget mode
         */
        public BudgetMode getMode() {
            return mode;
        }

        /**
         * Gets the suggested budget amount.
         *
         * @return the suggested budget
         */
        public double getSuggestedBudget() {
            return suggestedBudget;
        }

        /**
         * Gets the suggested savings amount.
         *
         * @return the suggested savings
         */
        public double getSuggestedSaving() {
            return suggestedSaving;
        }

        /**
         * Gets the reason for the recommendation.
         *
         * @return the reason
         */
        public String getReason() {
            return reason;
        }

        /**
         * Checks if sufficient past data is available to calculate a meaningful average consumption ratio.
         *
         * @return true if past data is available, false otherwise
         */
        public boolean hasPastData() {
            return hasPastData;
        }
    }
}