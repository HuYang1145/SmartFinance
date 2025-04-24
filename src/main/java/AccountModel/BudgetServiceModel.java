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


public class BudgetServiceModel {
    private static final String BUDGET_FILE = "user_budget.csv";
    private static final String TRANSACTIONS_FILE = "transactions.csv";
    // 使用 DateTimeFormatterBuilder 创建可以解析两种格式的 formatter
    public static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy/M/d")
            .optionalStart()
            .appendPattern(" HH:mm")
            .optionalEnd()
            .optionalStart()
            .appendPattern(":ss")
            .optionalEnd()
            .toFormatter();
    private static final double DEFAULT_SAVING_RATIO = 0.2; // 默认储蓄比例
    private static final double ECONOMICAL_SAVING_INCREASE = 0.1; // 经济模式增加的储蓄比例
    private static final double LARGE_TRANSACTION_THRESHOLD = 1000;
    private static final int LARGE_TRANSACTION_COUNT_THRESHOLD = 3;
    private static final int LEARNING_MONTHS = 3;
    private static Map<String, Double> cachedCustomBudgets = new HashMap<>();

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

    public static BudgetRecommendation calculateRecommendation(String currentUser, LocalDate now) {
        Double customBudget = loadCustomBudget(currentUser);

        if (customBudget != null && customBudget >= 0) {
            double totalIncomeThisMonth = calculateTotalIncomeForMonth(currentUser, now);
            return new BudgetRecommendation(BudgetMode.CUSTOM, customBudget, Math.max(0, totalIncomeThisMonth - customBudget), BudgetMode.CUSTOM.getReason(), false); // hasPastData doesn't matter for custom
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

    private static BudgetMode determineBudgetMode(String currentUser, LocalDate now) {
        if (hasUnstableSpendingLastMonth(currentUser, now)) {
            return BudgetMode.ECONOMICAL_UNSTABLE;
        }
        // 判断下个月是否是购物节月份
        if (isShoppingFestivalMonth(now.plusMonths(1))) {
            return BudgetMode.ECONOMICAL_FESTIVAL;
        }
        return BudgetMode.NORMAL;
    }

    private static boolean isShoppingFestivalMonth(LocalDate date) {
        Month month = date.getMonth();
        return month == Month.MARCH || month == Month.JUNE || month == Month.NOVEMBER || month == Month.DECEMBER;
    }

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

    private static double calculateEconomicalBudget(String currentUser, LocalDate now, double totalIncomeThisMonth) {
        return totalIncomeThisMonth * (1 - (DEFAULT_SAVING_RATIO + ECONOMICAL_SAVING_INCREASE)); // Increase saving
    }

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
        return (1 - DEFAULT_SAVING_RATIO); // Default to default consumption ratio if no history
    }

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

    private static List<Double> getMonthlyWithdrawals(String username, LocalDate start, LocalDate end) {
        List<Double> withdrawals = new ArrayList<>();
        List<TransactionServiceModel.TransactionData> transactions = TransactionServiceModel.readTransactions(username);
        if (transactions == null) return withdrawals;
        for (TransactionServiceModel.TransactionData tx : transactions) {
            try {
                // 使用新的 DATE_FORMATTER 解析日期和时间
                LocalDate transactionDate = LocalDate.parse(tx.time, DATE_FORMATTER);
                if (!transactionDate.isBefore(start) && !transactionDate.isAfter(end) &&
                        tx.operation.equalsIgnoreCase("Expense")) {
                    withdrawals.add(tx.amount);
                }
            } catch (DateTimeParseException e) {
                System.err.println("日期解析错误 (getMonthlyWithdrawals): " + tx.time + " - " + e.getMessage());
            }
        }
        return withdrawals;
    }

    private static double calculateTotalIncomeForMonth(String currentUser, LocalDate date) {
        System.out.println("BudgetAdvisor.calculateTotalIncomeForMonth - date: " + date);
        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        LocalDate lastDayOfMonth = date.withDayOfMonth(date.lengthOfMonth());
        double totalIncome = 0.0;
        List<TransactionServiceModel.TransactionData> transactions = TransactionServiceModel.readTransactions(currentUser);
        if (transactions == null) {
            System.out.println("警告: 未找到用户 " + currentUser + " 的交易记录。");
            return 0.0;
        }
        System.out.println("正在计算用户 " + currentUser + " " + date.getMonth() + " 月的收入...");
        // 打印返回的交易记录的用户名
        System.out.println("---- 返回的交易记录 (用户): ----");
        for (TransactionServiceModel.TransactionData tx : transactions) {
            System.out.println("  用户: " + tx.username + ", 时间: " + tx.time + ", 操作: " + tx.operation + ", 金额: " + tx.amount);
        }
        System.out.println("---- 结束 ----");

        for (TransactionServiceModel.TransactionData tx : transactions) {
            try {
                // 使用新的 DATE_FORMATTER 解析日期和时间
                LocalDate transactionDate = LocalDate.parse(tx.time, DATE_FORMATTER);
                if (tx.operation.equalsIgnoreCase("Income") &&
                        !transactionDate.isBefore(firstDayOfMonth) && !transactionDate.isAfter(lastDayOfMonth)){
                    totalIncome += tx.amount;
                }
            } catch (DateTimeParseException e) {
                System.err.println("日期解析错误 (calculateTotalIncomeForMonth): " + tx.time + " - " + e.getMessage());
            }
        }
        System.out.println(date.getMonth() + " 月的总收入计算结果: " + totalIncome);
        return totalIncome;
    }

    private static double calculateTotalExpenseForMonth(String currentUser, LocalDate date) {
        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        LocalDate lastDayOfMonth = date.withDayOfMonth(date.lengthOfMonth());
        double totalExpense = 0.0;
        List<TransactionServiceModel.TransactionData> transactions = TransactionServiceModel.readTransactions(currentUser);
        if (transactions == null) return 0.0;
        for (TransactionServiceModel.TransactionData tx : transactions) {
            try {
                // 使用新的 DATE_FORMATTER 解析日期和时间
                LocalDate transactionDate = LocalDate.parse(tx.time, DATE_FORMATTER);
                if (tx.operation.equalsIgnoreCase("Expense") &&
                        !transactionDate.isBefore(firstDayOfMonth) && !transactionDate.isAfter(lastDayOfMonth)) {
                    totalExpense += Math.abs(tx.amount);
                }
            } catch (DateTimeParseException e) {
                // Handle parsing errors if necessary
            }
        }
        return totalExpense;
    }

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

    public static void saveCustomBudget(String username, double budget) {
        cachedCustomBudgets.put(username, budget);
        try (FileWriter fw = new FileWriter(BUDGET_FILE, false); // Overwrite for simplicity
             java.io.BufferedWriter bw = new java.io.BufferedWriter(fw)) {
            for (Map.Entry<String, Double> entry : cachedCustomBudgets.entrySet()) {
                bw.write(entry.getKey() + "," + String.format("%.2f", entry.getValue()));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to budget file: " + e.getMessage());
        }
    }

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

    public static Double getCustomBudget(String username) {
        return loadCustomBudget(username);
    }

    // --- Methods for UI Display ---

    public static String getCurrentBudget(String username, LocalDate now) {
        Double customBudget = loadCustomBudget(username);
        if (customBudget != null) {
            return String.format("¥%.2f", customBudget);
        }
        BudgetRecommendation recommendation = calculateRecommendation(username, now);
        return String.format("¥%.2f", recommendation.suggestedBudget);
    }

    public static String getSavingGoal(String username, LocalDate now) {
        BudgetRecommendation recommendation = calculateRecommendation(username, now);
        return String.format("¥%.2f", recommendation.suggestedSaving);
    }

    public static String getBudgetMode(String username, LocalDate now) {
        Double customBudget = loadCustomBudget(username);
        if (customBudget != null) {
            return BudgetMode.CUSTOM.getDisplayName();
        }
        return calculateRecommendation(username, now).mode.getDisplayName();
    }

    public static String getBudgetReason(String username, LocalDate now) {
        Double customBudget = loadCustomBudget(username);
        if (customBudget != null) {
            return BudgetMode.CUSTOM.getReason();
        }
        return calculateRecommendation(username, now).reason;
    }

    public static BudgetRecommendation getViewSuggestionDetails(String username, LocalDate now) {
        return calculateRecommendation(username, now);
    }

    public static String getTopSpendingCategory(String username, LocalDate now) {
        Map<String, Double> categoryTotals = new HashMap<>();
        List<TransactionServiceModel.TransactionData> transactions = TransactionServiceModel.readTransactions(username);
        if (transactions == null) return "N/A";

        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        LocalDate lastDayOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        for (TransactionServiceModel.TransactionData tx : transactions) {
            try {
                LocalDate transactionDate = LocalDate.parse(tx.time, DATE_FORMATTER);
                if (tx.operation.equalsIgnoreCase("Expense") &&
                        !transactionDate.isBefore(firstDayOfMonth) && !transactionDate.isAfter(lastDayOfMonth) &&
                        tx.category != null && !tx.category.trim().isEmpty()) {
                    categoryTotals.put(tx.category.trim(), categoryTotals.getOrDefault(tx.category.trim(), 0.0) + Math.abs(tx.amount));
                } else if (tx.operation.equalsIgnoreCase("Expense") &&
                        !transactionDate.isBefore(firstDayOfMonth) && !transactionDate.isAfter(lastDayOfMonth)) {
                    categoryTotals.put("Unclassified", categoryTotals.getOrDefault("Unclassified", 0.0) + Math.abs(tx.amount));
                }
            } catch (DateTimeParseException e) {
                // Handle parsing errors if necessary
            }
        }

        return categoryTotals.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }

    public static List<String> getLargeConsumptions(String username, LocalDate now) {
        List<String> largeConsumptions = new ArrayList<>();
        List<TransactionServiceModel.TransactionData> transactions = TransactionServiceModel.readTransactions(username);
        if (transactions == null) return largeConsumptions;

        double currentMonthIncome = calculateTotalIncomeForMonth(username, now);
        double largeThreshold = currentMonthIncome * 0.07; // 7% of income
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        LocalDate lastDayOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        for (TransactionServiceModel.TransactionData tx : transactions) {
            try {
                LocalDate transactionDate = LocalDate.parse(tx.time, DATE_FORMATTER);
                if (tx.operation.equalsIgnoreCase("Expense") &&
                        !transactionDate.isBefore(firstDayOfMonth) && !transactionDate.isAfter(lastDayOfMonth) &&
                        Math.abs(tx.amount) > largeThreshold) {
                    String formattedDate = tx.time;
                    largeConsumptions.add(String.format("%s - ¥%.2f - %s", formattedDate, Math.abs(tx.amount), tx.type));
                }
            } catch (DateTimeParseException e) {
                // Handle parsing errors if necessary
            }
        }
        return largeConsumptions;
    }

    public static String getCurrentMonthExpenditure(String username, LocalDate now) {
        double expenditure = calculateTotalExpenseForMonth(username, now);
        return String.format("¥%.2f", expenditure);
    }

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