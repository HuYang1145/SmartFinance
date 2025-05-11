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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Model.BudgetDataContainer;
import Model.Transaction;
import Repository.TransactionRepository;

public class BudgetService {
    private final TransactionRepository transactionRepository;
    private final Map<String, Double> cachedCustomBudgets = new HashMap<>();
    private final Map<String, Long> cacheTimestamps = new HashMap<>();
    private static final String BUDGET_FILE = "user_budget.csv";
    private static final long CACHE_EXPIRY_MS = 5 * 60 * 1000;
    private static final double DEFAULT_SAVING_RATIO = 0.2;
    private static final double ECONOMICAL_SAVING_INCREASE = 0.1;
    private static final double LARGE_CONSUMPTION_THRESHOLD = 0.07;
    private static final double LARGE_TRANSACTION_THRESHOLD = 1000;
    private static final int LARGE_TRANSACTION_COUNT_THRESHOLD = 3;
    private static final int LEARNING_MONTHS = 3;

    /**
     * Formatter for parsing and formatting transaction dates in the format "yyyy/MM/dd" or "yyyy/MM/dd HH:mm".
     */
    public static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy/MM/dd")
            .optionalStart()
            .appendPattern(" HH:mm")
            .optionalEnd()
            .toFormatter();

    /**
     * Constructs a BudgetService instance with a transaction repository dependency.
     *
     * @param transactionRepository the repository for accessing transaction data
     */
    public BudgetService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Retrieves budget-related data for a user, including income, expenses, and recommendations.
     *
     * @param username the username of the user
     * @param now      the current date for temporal context
     * @return a BudgetDataContainer with budget data and recommendations
     */
    public BudgetDataContainer getBudgetData(String username, LocalDate now) {
        List<Transaction> transactions = transactionRepository.findTransactionsByUsername(username);
        double currentMonthIncome = calculateCurrentMonthIncome(transactions, now);
        double currentMonthExpense = calculateCurrentMonthExpense(transactions, now);
        String topType = findTopExpenseType(transactions, now);
        List<String> largeConsumptions = findLargeConsumptions(transactions, currentMonthIncome, now);
        BudgetRecommendation recommendation = calculateRecommendation(username, now);
        Double customBudget = getCustomBudget(username);
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
            for (Map.Entry<String, Double> entry : cachedCustomBudgets.entrySet()) {
                bw.write(entry.getKey() + "," + String.format("%.2f", entry.getValue()));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to budget file: " + e.getMessage());
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
            for (Map.Entry<String, Double> entry : cachedCustomBudgets.entrySet()) {
                bw.write(entry.getKey() + "," + String.format("%.2f", entry.getValue()));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to budget file when clearing: " + e.getMessage());
        }
    }

    /**
     * Retrieves a custom budget for a user from cache or file.
     *
     * @param username the username of the user
     * @return the custom budget amount, or null if not set
     */
    public Double getCustomBudget(String username) {
        if (cachedCustomBudgets.containsKey(username)) {
            return cachedCustomBudgets.get(username);
        }
        try (BufferedReader br = new BufferedReader(new FileReader(BUDGET_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2 && parts[0].trim().equals(username.trim())) {
                    double budget = Double.parseDouble(parts[1].trim());
                    cachedCustomBudgets.put(username, budget);
                    return budget;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading budget file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing budget amount in file: " + e.getMessage());
        }
        return null;
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
            if ("Income".equalsIgnoreCase(tx.getOperation())) {
                try {
                    LocalDate transactionDate = LocalDate.parse(tx.getTimestamp(), DATE_FORMATTER);
                    if (transactionDate.getYear() == now.getYear() && transactionDate.getMonthValue() == now.getMonthValue()) {
                        totalIncome += tx.getAmount();
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("Failed to parse date in calculateCurrentMonthIncome: " + tx.getTimestamp());
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
            if ("Expense".equalsIgnoreCase(tx.getOperation())) {
                try {
                    LocalDate transactionDate = LocalDate.parse(tx.getTimestamp(), DATE_FORMATTER);
                    if (!transactionDate.isBefore(firstDayOfMonth) && !transactionDate.isAfter(lastDayOfMonth)) {
                        totalExpense += tx.getAmount();
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("Failed to parse date in calculateCurrentMonthExpense: " + tx.getTimestamp());
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
            if ("Expense".equalsIgnoreCase(tx.getOperation())) {
                try {
                    LocalDate transactionDate = LocalDate.parse(tx.getTimestamp(), DATE_FORMATTER);
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
                    System.err.println("Failed to parse date in findTopExpenseType: " + tx.getTimestamp());
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
     *
     * @param transactions       the list of transactions
     * @param currentMonthIncome the total income for the current month
     * @param now                the current date for temporal context
     * @return a list of formatted large consumption details
     */
    private List<String> findLargeConsumptions(List<Transaction> transactions, double currentMonthIncome, LocalDate now) {
        if (transactions == null || currentMonthIncome <= 0) return new ArrayList<>();
        List<String> largeConsumptions = new ArrayList<>();
        double largeThreshold = currentMonthIncome * 0.07;
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        for (Transaction tx : transactions) {
            if ("Expense".equalsIgnoreCase(tx.getOperation())) {
                try {
                    LocalDate transactionDate = LocalDate.parse(tx.getTimestamp(), DATE_FORMATTER);
                    if (transactionDate.getYear() == now.getYear() && transactionDate.getMonthValue() == now.getMonthValue() && tx.getAmount() > largeThreshold) {
                        largeConsumptions.add(String.format("%s - ¥%.2f - %s", transactionDate.format(displayFormatter), tx.getAmount(), tx.getType()));
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("Failed to parse date in findLargeConsumptions: " + tx.getTimestamp());
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
     * @param hasPastData        whether sufficient past data is available
     * @return the suggested normal budget
     */
    private double calculateNormalBudget(String username, LocalDate now, double totalIncomeThisMonth, boolean hasPastData) {
        double consumptionRatio = hasPastData ? calculateAverageConsumptionRatio(username, now) : (1 - 0.2);
        return totalIncomeThisMonth * consumptionRatio;
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
        return totalIncomeThisMonth * (1 - (0.2 + 0.1));
    }

    /**
     * Calculates the average consumption ratio over the past three months.
     *
     * @param username the username of the user
     * @param now      the current date for temporal context
     * @return the average consumption ratio, or default if no valid data
     */
    private double calculateAverageConsumptionRatio(String username, LocalDate now) {
        double totalConsumptionRatio = 0;
        int validMonths = 0;
        for (int i = 1; i <= 3; i++) {
            LocalDate month = now.minusMonths(i);
            double totalIncome = calculateCurrentMonthIncome(transactionRepository.findTransactionsByUsername(username), month);
            double totalExpense = calculateCurrentMonthExpense(transactionRepository.findTransactionsByUsername(username), month);
            if (totalIncome > 0) {
                totalConsumptionRatio += (totalExpense / totalIncome);
                validMonths++;
            }
        }
        return validMonths > 0 ? totalConsumptionRatio / validMonths : (1 - 0.2);
    }

    /**
     * Checks if sufficient past data (three months of income) is available for budgeting.
     *
     * @param username the username of the user
     * @param now      the current date for temporal context
     * @return true if sufficient data is available, false otherwise
     */
    private boolean hasSufficientPastData(String username, LocalDate now) {
        int validMonths = 0;
        for (int i = 1; i <= 3; i++) {
            LocalDate month = now.minusMonths(i);
            double totalIncome = calculateCurrentMonthIncome(transactionRepository.findTransactionsByUsername(username), month);
            if (totalIncome > 0) {
                validMonths++;
            }
        }
        return validMonths == 3;
    }

    /**
     * Checks if the user's spending was unstable last month based on large transactions.
     *
     * @param username the username of the user
     * @param now      the current date for temporal context
     * @return true if spending was unstable, false otherwise
     */
    private boolean hasUnstableSpendingLastMonth(String username, LocalDate now) {
        LocalDate lastMonthStart = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEnd = now.minusMonths(1).withDayOfMonth(now.minusMonths(1).lengthOfMonth());
        List<Double> lastMonthWithdrawals = getMonthlyWithdrawals(username, lastMonthStart, lastMonthEnd);
        if (lastMonthWithdrawals.size() >= 3) {
            int largeTransactionCount = 0;
            for (double amount : lastMonthWithdrawals) {
                if (Math.abs(amount) > 1000) {
                    largeTransactionCount++;
                }
            }
            return largeTransactionCount >= 3;
        }
        return false;
    }

    /**
     * Retrieves a list of withdrawal amounts for a given month.
     *
     * @param username the username of the user
     * @param start    the start date of the period
     * @param end      the end date of the period
     * @return a list of withdrawal amounts
     */
    private List<Double> getMonthlyWithdrawals(String username, LocalDate start, LocalDate end) {
        List<Double> withdrawals = new ArrayList<>();
        List<Transaction> transactions = transactionRepository.findTransactionsByUsername(username);
        for (Transaction tx : transactions) {
            if ("Expense".equalsIgnoreCase(tx.getOperation())) {
                try {
                    LocalDate transactionDate = LocalDate.parse(tx.getTimestamp(), DATE_FORMATTER);
                    if (!transactionDate.isBefore(start) && !transactionDate.isAfter(end)) {
                        withdrawals.add(tx.getAmount());
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("Failed to parse date in getMonthlyWithdrawals: " + tx.getTimestamp());
                }
            }
        }
        return withdrawals;
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
         * @  
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
         * @param hasPastData    whether sufficient past data is available
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
         * Checks if sufficient past data is available.
         *
         * @return true if past data is available, false otherwise
         */
        public boolean hasPastData() {
            return hasPastData;
        }
    }
}