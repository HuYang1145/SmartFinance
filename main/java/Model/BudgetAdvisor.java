package Model;

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

public class BudgetAdvisor {
    private static final String BUDGET_FILE = "user_budget.csv";
    private static final String TRANSACTIONS_FILE = "transactions.csv";
    public static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy/M/d")
            .optionalStart()
            .appendPattern("/HH:mm")
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
        } else {
            consumptionRatio = (1 - DEFAULT_SAVING_RATIO); // Default if no history
        }

        if (consumptionRatio > 0) {
            return totalIncomeThisMonth * consumptionRatio;
        } else {
            return totalIncomeThisMonth * (1 - DEFAULT_SAVING_RATIO); // Fallback
        }
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
        return 0;
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
        try (BufferedReader br = new BufferedReader(new FileReader(TRANSACTIONS_FILE))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 4 && data[0].trim().equals(username.trim())) {
                    try {
                        LocalDate transactionDate = LocalDate.parse(data[3].trim().split(" ")[0], DATE_FORMATTER);
                        double amount = Double.parseDouble(data[2].trim());
                        String operation = data[1].trim();
                        if (!(operation.equals("Deposit") || operation.equals("Transfer In")) &&
                                !transactionDate.isBefore(start) && !transactionDate.isAfter(end)) {
                            withdrawals.add(amount);
                        }
                    } catch (DateTimeParseException | NumberFormatException e) {
                        // Log or handle parsing errors
                    }
                }
            }
        } catch (IOException e) {
            // Handle file reading error
        }
        return withdrawals;
    }

    private static double calculateTotalIncomeForMonth(String currentUser, LocalDate date) {
        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        LocalDate lastDayOfMonth = date.withDayOfMonth(date.lengthOfMonth());
        double totalIncome = 0.0;
        try (BufferedReader br = new BufferedReader(new FileReader(TRANSACTIONS_FILE))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 4 && data[0].trim().equals(currentUser.trim())) {
                    try {
                        LocalDate transactionDate = LocalDate.parse(data[3].trim().split(" ")[0], DATE_FORMATTER);
                        double amount = Double.parseDouble(data[2].trim());
                        String operation = data[1].trim();
                        if ((operation.equals("Deposit") || operation.equals("Transfer In")) &&
                                !transactionDate.isBefore(firstDayOfMonth) && !transactionDate.isAfter(lastDayOfMonth)) {
                            totalIncome += amount;
                        }
                    } catch (DateTimeParseException | NumberFormatException e) {
                        // Log or handle parsing errors
                    }
                }
            }
        } catch (IOException e) {
            // Handle file reading error
        }
        return totalIncome;
    }

    private static double calculateTotalExpenseForMonth(String currentUser, LocalDate date) {
        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        LocalDate lastDayOfMonth = date.withDayOfMonth(date.lengthOfMonth());
        double totalExpense = 0.0;
        try (BufferedReader br = new BufferedReader(new FileReader(TRANSACTIONS_FILE))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 4 && data[0].trim().equals(currentUser.trim())) {
                    try {
                        LocalDate transactionDate = LocalDate.parse(data[3].trim().split(" ")[0], DATE_FORMATTER);
                        double amount = Double.parseDouble(data[2].trim());
                        String operation = data[1].trim();
                        if (!(operation.equals("Deposit") || operation.equals("Transfer In")) &&
                                !transactionDate.isBefore(firstDayOfMonth) && !transactionDate.isAfter(lastDayOfMonth)) {
                            totalExpense += Math.abs(amount);
                        }
                    } catch (DateTimeParseException | NumberFormatException e) {
                        // Log or handle parsing errors
                    }
                }
            }
        } catch (IOException e) {
            // Handle file reading error
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

    public static class Transaction {
        public String username;
        public String operation;
        public double amount;
        public LocalDate date;
        public String details;
    }
}