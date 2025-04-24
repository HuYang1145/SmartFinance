package AccountModel;

import java.io.BufferedReader;
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

import TransactionController.TransactionController;
import TransactionModel.TransactionModel;

/**
 * Service class for managing user budgets, providing recommendations, and analyzing spending patterns.
 */
public class BudgetServiceModel {
    private static final String BUDGET_FILE = "user_budget.csv";
    private static final String TRANSACTIONS_FILE = "transactions.csv";
    // Formatter for parsing transaction timestamps in yyyy/MM/dd [HH:mm] format
    public static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy/MM/dd")
            .optionalStart()
            .appendPattern(" HH:mm")
            .optionalEnd()
            .toFormatter();
    private static final double DEFAULT_SAVING_RATIO = 0.2; // Default saving ratio
    private static final double ECONOMICAL_SAVING_INCREASE = 0.1; // Additional saving ratio for economical mode
    private static final double LARGE_TRANSACTION_THRESHOLD = 1000;
    private static final int LARGE_TRANSACTION_COUNT_THRESHOLD = 3;
    private static final int LEARNING_MONTHS = 3;
    private static Map<String, Double> cachedCustomBudgets = new HashMap<>();
    private static Map<String, List<TransactionModel>> cachedTransactions = new HashMap<>();
    private static Map<String, Long> cacheTimestamps = new HashMap<>();
    private static final long CACHE_EXPIRY_MS = 5 * 60 * 1000; // 5 minutes expiry

    public static class BudgetRecommendation {
        public final BudgetMode mode;
        public final double suggestedBudget;
        public final double suggestedSaving;
        public final String reason;
        public final boolean hasPastData; // Flag to indicate if past 3 months data exists

        public BudgetRecommendation(BudgetMode mode, double budget, double saving, String reason, boolean hasPastData) {
            this.mode = mode;
            this.suggestedBudget = budget;
            this.suggestedSaving = saving;
            this.reason = reason;
            this.hasPastData = hasPastData;
        }
    }

    public enum BudgetMode {
        NORMAL("Normal Mode", "Consumption is stable and predictable now."),
        ECONOMICAL_UNSTABLE("Economical Mode", "Users' spending is unstable."),
        ECONOMICAL_FESTIVAL("Economical Mode", "Next month is the shopping festival."),
        CUSTOM("Custom Mode", "Following your defined budget.");

        private final String displayName;
        private final String reason;

        BudgetMode(String name, String reason) {
            this.displayName = name;
            this.reason = reason;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getReason() {
            return reason;
        }
    }

    /**
     * Checks if the transaction cache for the user is valid.
     */
    private static boolean isCacheValid(String username) {
        Long timestamp = cacheTimestamps.get(username);
        if (timestamp == null) return false;
        return (System.currentTimeMillis() - timestamp) < CACHE_EXPIRY_MS;
    }

    /**
 * Retrieves transactions for the user, using cache if valid.
 */
private static List<TransactionModel> getCachedTransactions(String username) {
    if (isCacheValid(username)) {
        return cachedTransactions.getOrDefault(username, new ArrayList<>());
    }
    List<TransactionModel> transactions = TransactionController.readTransactions(username);
    cachedTransactions.put(username, transactions != null ? transactions : new ArrayList<>());
    cacheTimestamps.put(username, System.currentTimeMillis());
    return cachedTransactions.get(username);
}

    /**
     * Calculates a budget recommendation for the user based on their transaction history and preferences.
     *
     * @param currentUser The username of the current user.
     * @param now         The current date.
     * @return A BudgetRecommendation object with mode, budget, saving, and reason.
     */
    public static BudgetRecommendation calculateRecommendation(String currentUser, LocalDate now) {
        Double customBudget = loadCustomBudget(currentUser);

        if (customBudget != null && customBudget >= 0) {
            double totalIncomeThisMonth = calculateTotalIncomeForMonth(currentUser, now);
            return new BudgetRecommendation(BudgetMode.CUSTOM, customBudget, Math.max(0, totalIncomeThisMonth - customBudget), BudgetMode.CUSTOM.getReason(), false);
        }

        BudgetMode mode = determineBudgetMode(currentUser, now);
        double totalIncomeThisMonth = calculateTotalIncomeForMonth(currentUser, now);
        System.out.println("四月份的总收入: " + totalIncomeThisMonth);
        double suggestedBudget;
        double suggestedSaving;
        boolean hasPastData = hasSufficientPastData(currentUser, now);

        if (mode == BudgetMode.ECONOMICAL_UNSTABLE || mode == BudgetMode.ECONOMICAL_FESTIVAL) {
            suggestedBudget = calculateEconomicalBudget(currentUser, now, totalIncomeThisMonth);
            suggestedSaving = Math.max(0, totalIncomeThisMonth - suggestedBudget);
        } else { // Normal Mode
            suggestedBudget = calculateNormalBudget(currentUser, now, totalIncomeThisMonth, hasPastData);
            suggestedSaving = Math.max(0, totalIncomeThisMonth - suggestedBudget);
        }
        System.out.println("当前月份总收入 (calculateRecommendation): " + totalIncomeThisMonth);
        System.out.println("计算出的预算 (calculateRecommendation): " + suggestedBudget);
        System.out.println("计算出的储蓄 (calculateRecommendation): " + suggestedSaving);
        System.out.println("当前的预算模式 (calculateRecommendation): " + mode);
        System.out.println("自定义预算 (calculateRecommendation): " + customBudget);

        return new BudgetRecommendation(mode, suggestedBudget, suggestedSaving, mode.getReason(), hasPastData);
    }

    /**
     * Determines the appropriate budget mode based on spending patterns and upcoming events.
     */
    private static BudgetMode determineBudgetMode(String currentUser, LocalDate now) {
        if (hasUnstableSpendingLastMonth(currentUser, now)) {
            return BudgetMode.ECONOMICAL_UNSTABLE;
        }
        if (isShoppingFestivalMonth(now.plusMonths(1))) {
            return BudgetMode.ECONOMICAL_FESTIVAL;
        }
        return BudgetMode.NORMAL;
    }

    /**
     * Checks if the given month is a shopping festival month (March, June, November, December).
     */
    private static boolean isShoppingFestivalMonth(LocalDate date) {
        Month month = date.getMonth();
        return month == Month.MARCH || month == Month.JUNE || month == Month.NOVEMBER || month == Month.DECEMBER;
    }

    /**
     * Calculates the budget for normal mode based on past consumption ratios or default ratio.
     */
    private static double calculateNormalBudget(String currentUser, LocalDate now, double totalIncomeThisMonth, boolean hasPastData) {
        double consumptionRatio;
        if (hasPastData) {
            consumptionRatio = calculateAverageConsumptionRatio(currentUser, now);
            System.out.println("计算出的消费比例: " + consumptionRatio);
        } else {
            consumptionRatio = (1 - DEFAULT_SAVING_RATIO);
            System.out.println("使用的默认消费比例: " + consumptionRatio + " (DEFAULT_SAVING_RATIO: " + DEFAULT_SAVING_RATIO + ")");
        }
        return totalIncomeThisMonth * consumptionRatio;
    }

    /**
     * Calculates the budget for economical mode with increased savings.
     */
    private static double calculateEconomicalBudget(String currentUser, LocalDate now, double totalIncomeThisMonth) {
        return totalIncomeThisMonth * (1 - (DEFAULT_SAVING_RATIO + ECONOMICAL_SAVING_INCREASE));
    }

    /**
     * Calculates the average consumption ratio over the past LEARNING_MONTHS.
     */
    private static double calculateAverageConsumptionRatio(String currentUser, LocalDate now) {
        double totalConsumptionRatio = 0;
        int validMonths = 0;
        for (int i = 1; i <= LEARNING_MONTHS; i++) {
            LocalDate month = now.minusMonths(i);
            double totalIncome = calculateTotalIncomeForMonth(currentUser, month);
            double totalExpense = calculateTotalExpenseForMonth(currentUser, month);
            if (totalIncome > 0) {
                totalConsumptionRatio += (totalExpense / totalIncome);
                validMonths++;
            }
        }
        if (validMonths > 0) {
            return totalConsumptionRatio / validMonths;
        }
        return (1 - DEFAULT_SAVING_RATIO);
    }

    /**
     * Checks if there is sufficient transaction data for the past LEARNING_MONTHS.
     */
    private static boolean hasSufficientPastData(String currentUser, LocalDate now) {
        int validMonths = 0;
        for (int i = 1; i <= LEARNING_MONTHS; i++) {
            LocalDate month = now.minusMonths(i);
            double totalIncome = calculateTotalIncomeForMonth(currentUser, month);
            if (totalIncome > 0) {
                validMonths++;
            }
        }
        return validMonths == LEARNING_MONTHS;
    }

    /**
     * Checks if last month's spending was unstable (too many large transactions).
     */
    private static boolean hasUnstableSpendingLastMonth(String currentUser, LocalDate now) {
        LocalDate lastMonthStart = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEnd = now.minusMonths(1).withDayOfMonth(now.minusMonths(1).lengthOfMonth());
        List<Double> lastMonthWithdrawals = getMonthlyWithdrawals(currentUser, lastMonthStart, lastMonthEnd);
        if (lastMonthWithdrawals.size() >= LARGE_TRANSACTION_COUNT_THRESHOLD) {
            int largeTransactionCount = 0;
            for (double amount : lastMonthWithdrawals) {
                if (Math.abs(amount) > LARGE_TRANSACTION_THRESHOLD) {
                    largeTransactionCount++;
                }
            }
            return largeTransactionCount >= LARGE_TRANSACTION_COUNT_THRESHOLD;
        }
        return false;
    }

    /**
     * Retrieves all withdrawal transactions for a given month.
     */
    private static List<Double> getMonthlyWithdrawals(String username, LocalDate start, LocalDate end) {
        List<Double> withdrawals = new ArrayList<>();
        List<TransactionModel> transactions = getCachedTransactions(username);
        for (TransactionModel tx : transactions) {
            try {
                LocalDate transactionDate = LocalDate.parse(tx.getTimestamp(), DATE_FORMATTER);
                if (!transactionDate.isBefore(start) && !transactionDate.isAfter(end) &&
                        tx.getOperation().equalsIgnoreCase("Expense")) {
                    withdrawals.add(tx.getAmount());
                }
            } catch (DateTimeParseException e) {
                System.err.println("日期解析错误 (getMonthlyWithdrawals): " + tx.getTimestamp() + " - " + e.getMessage());
            }
        }
        return withdrawals;
    }

    /**
     * Calculates total income for a given month.
     */
    private static double calculateTotalIncomeForMonth(String currentUser, LocalDate date) {
        System.out.println("BudgetAdvisor.calculateTotalIncomeForMonth - date: " + date);
        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        LocalDate lastDayOfMonth = date.withDayOfMonth(date.lengthOfMonth());
        double totalIncome = 0.0;
        List<TransactionModel> transactions = getCachedTransactions(currentUser);
        if (transactions.isEmpty()) {
            System.out.println("警告: 未找到用户 " + currentUser + " 的交易记录。");
            return 0.0;
        }
        System.out.println("正在计算用户 " + currentUser + " " + date.getMonth() + " 月的收入...");
        System.out.println("---- 返回的交易记录 (用户): ----");
        for (TransactionModel tx : transactions) {
            System.out.println("  用户: " + tx.getAccountUsername() + ", 时间: " + tx.getTimestamp() + ", 操作: " + tx.getOperation() + ", 金额: " + tx.getAmount());
        }
        System.out.println("---- 结束 ----");

        for (TransactionModel tx : transactions) {
            try {
                LocalDate transactionDate = LocalDate.parse(tx.getTimestamp(), DATE_FORMATTER);
                if (tx.getOperation().equalsIgnoreCase("Income") &&
                        !transactionDate.isBefore(firstDayOfMonth) && !transactionDate.isAfter(lastDayOfMonth)) {
                    totalIncome += tx.getAmount();
                }
            } catch (DateTimeParseException e) {
                System.err.println("日期解析错误 (calculateTotalIncomeForMonth): " + tx.getTimestamp() + " - " + e.getMessage());
            }
        }
        System.out.println(date.getMonth() + " 月的总收入计算结果: " + totalIncome);
        return totalIncome;
    }

    /**
     * Calculates total expenses for a given month.
     */
    private static double calculateTotalExpenseForMonth(String currentUser, LocalDate date) {
        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        LocalDate lastDayOfMonth = date.withDayOfMonth(date.lengthOfMonth());
        double totalExpense = 0.0;
        List<TransactionModel> transactions = getCachedTransactions(currentUser);
        if (transactions.isEmpty()) return 0.0;
        for (TransactionModel tx : transactions) {
            try {
                LocalDate transactionDate = LocalDate.parse(tx.getTimestamp(), DATE_FORMATTER);
                if (tx.getOperation().equalsIgnoreCase("Expense") &&
                        !transactionDate.isBefore(firstDayOfMonth) && !transactionDate.isAfter(lastDayOfMonth)) {
                    totalExpense += Math.abs(tx.getAmount());
                }
            } catch (DateTimeParseException e) {
                System.err.println("日期解析错误 (calculateTotalExpenseForMonth): " + tx.getTimestamp() + " - " + e.getMessage());
            }
        }
        return totalExpense;
    }

    /**
     * Loads a custom budget for the user from user_budget.csv.
     */
    private static Double loadCustomBudget(String username) {
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
            System.err.println("Error parsing budget amount in file.");
        }
        return null;
    }

    /**
     * Saves a custom budget for the user to user_budget.csv.
     */
    public static void saveCustomBudget(String username, double budget) {
        cachedCustomBudgets.put(username, budget);
        try (FileWriter fw = new FileWriter(BUDGET_FILE, false);
             java.io.BufferedWriter bw = new java.io.BufferedWriter(fw)) {
            for (Map.Entry<String, Double> entry : cachedCustomBudgets.entrySet()) {
                bw.write(entry.getKey() + "," + String.format("%.2f", entry.getValue()));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to budget file: " + e.getMessage());
        }
    }

    /**
     * Clears a custom budget for the user from user_budget.csv.
     */
    public static void clearCustomBudget(String username) {
        cachedCustomBudgets.remove(username);
        try (FileWriter fw = new FileWriter(BUDGET_FILE, false);
             java.io.BufferedWriter bw = new java.io.BufferedWriter(fw)) {
            for (Map.Entry<String, Double> entry : cachedCustomBudgets.entrySet()) {
                bw.write(entry.getKey() + "," + String.format("%.2f", entry.getValue()));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to budget file when clearing: " + e.getMessage());
        }
    }

    /**
     * Retrieves the custom budget for the user.
     */
    public static Double getCustomBudget(String username) {
        return loadCustomBudget(username);
    }

    /**
     * Gets the current budget (custom or recommended) as a formatted string.
     */
    public static String getCurrentBudget(String username, LocalDate now) {
        Double customBudget = loadCustomBudget(username);
        if (customBudget != null) {
            return String.format("¥%.2f", customBudget);
        }
        BudgetRecommendation recommendation = calculateRecommendation(username, now);
        return String.format("¥%.2f", recommendation.suggestedBudget);
    }

    /**
     * Gets the saving goal as a formatted string.
     */
    public static String getSavingGoal(String username, LocalDate now) {
        BudgetRecommendation recommendation = calculateRecommendation(username, now);
        return String.format("¥%.2f", recommendation.suggestedSaving);
    }

    /**
     * Gets the current budget mode display name.
     */
    public static String getBudgetMode(String username, LocalDate now) {
        Double customBudget = loadCustomBudget(username);
        if (customBudget != null) {
            return BudgetMode.CUSTOM.getDisplayName();
        }
        return calculateRecommendation(username, now).mode.getDisplayName();
    }

    /**
     * Gets the reason for the current budget mode.
     */
    public static String getBudgetReason(String username, LocalDate now) {
        Double customBudget = loadCustomBudget(username);
        if (customBudget != null) {
            return BudgetMode.CUSTOM.getReason();
        }
        return calculateRecommendation(username, now).reason;
    }

    /**
     * Gets detailed budget recommendation for UI display.
     */
    public static BudgetRecommendation getViewSuggestionDetails(String username, LocalDate now) {
        return calculateRecommendation(username, now);
    }

    /**
     * Gets the top spending category for the current month.
     */
    public static String getTopSpendingCategory(String username, LocalDate now) {
        Map<String, Double> categoryTotals = new HashMap<>();
        List<TransactionModel> transactions = getCachedTransactions(username);
        if (transactions.isEmpty()) return "N/A";

        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        LocalDate lastDayOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        for (TransactionModel tx : transactions) {
            try {
                LocalDate transactionDate = LocalDate.parse(tx.getTimestamp(), DATE_FORMATTER);
                if (tx.getOperation().equalsIgnoreCase("Expense") &&
                        !transactionDate.isBefore(firstDayOfMonth) && !transactionDate.isAfter(lastDayOfMonth)) {
                    String category = tx.getCategory() != null && !tx.getCategory().trim().isEmpty() ? tx.getCategory().trim() : "Unclassified";
                    categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + Math.abs(tx.getAmount()));
                }
            } catch (DateTimeParseException e) {
                System.err.println("日期解析错误 (getTopSpendingCategory): " + tx.getTimestamp() + " - " + e.getMessage());
            }
        }

        return categoryTotals.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }

    /**
     * Gets a list of large consumption transactions for the current month.
     */
    public static List<String> getLargeConsumptions(String username, LocalDate now) {
        List<String> largeConsumptions = new ArrayList<>();
        List<TransactionModel> transactions = getCachedTransactions(username);
        if (transactions.isEmpty()) return largeConsumptions;

        double currentMonthIncome = calculateTotalIncomeForMonth(username, now);
        double largeThreshold = currentMonthIncome * 0.07; // 7% of income
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        LocalDate lastDayOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        for (TransactionModel tx : transactions) {
            try {
                LocalDate transactionDate = LocalDate.parse(tx.getTimestamp(), DATE_FORMATTER);
                if (tx.getOperation().equalsIgnoreCase("Expense") &&
                        !transactionDate.isBefore(firstDayOfMonth) && !transactionDate.isAfter(lastDayOfMonth) &&
                        Math.abs(tx.getAmount()) > largeThreshold) {
                    String formattedDate = transactionDate.format(displayFormatter);
                    largeConsumptions.add(String.format("%s - ¥%.2f - %s", formattedDate, Math.abs(tx.getAmount()), tx.getType()));
                }
            } catch (DateTimeParseException e) {
                System.err.println("日期解析错误 (getLargeConsumptions): " + tx.getTimestamp() + " - " + e.getMessage());
            }
        }
        return largeConsumptions;
    }

    /**
     * Gets the total expenditure for the current month as a formatted string.
     */
    public static String getCurrentMonthExpenditure(String username, LocalDate now) {
        double expenditure = calculateTotalExpenseForMonth(username, now);
        return String.format("¥%.2f", expenditure);
    }

    /**
     * Gets the budget status (overspent or remaining) as a formatted string.
     */
    public static String getBudgetStatus(String username, LocalDate now) {
        double budget = 0;
        Double customBudget = loadCustomBudget(username);
        if (customBudget != null) {
            budget = customBudget;
        } else {
            budget = calculateRecommendation(username, now).suggestedBudget;
        }
        double expenditure = calculateTotalExpenseForMonth(username, now);
        double difference = budget - expenditure;

        if (difference < 0) {
            return String.format("Overspent by: ¥%.2f", Math.abs(difference));
        } else {
            return String.format("Distance to the budget: ¥%.2f", difference);
        }
    }
}