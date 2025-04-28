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

    public static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy/MM/dd")
            .optionalStart()
            .appendPattern(" HH:mm")
            .optionalEnd()
            .toFormatter();

    public BudgetService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

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

    private BudgetMode determineBudgetMode(String username, LocalDate now) {
        if (hasUnstableSpendingLastMonth(username, now)) {
            return BudgetMode.ECONOMICAL_UNSTABLE;
        }
        if (isShoppingFestivalMonth(now.plusMonths(1))) {
            return BudgetMode.ECONOMICAL_FESTIVAL;
        }
        return BudgetMode.NORMAL;
    }

    private boolean isShoppingFestivalMonth(LocalDate date) {
        Month month = date.getMonth();
        return month == Month.MARCH || month == Month.JUNE || month == Month.NOVEMBER || month == Month.DECEMBER;
    }

    private double calculateNormalBudget(String username, LocalDate now, double totalIncomeThisMonth, boolean hasPastData) {
        double consumptionRatio = hasPastData ? calculateAverageConsumptionRatio(username, now) : (1 - 0.2);
        return totalIncomeThisMonth * consumptionRatio;
    }

    private double calculateEconomicalBudget(String username, LocalDate now, double totalIncomeThisMonth) {
        return totalIncomeThisMonth * (1 - (0.2 + 0.1));
    }

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

    public static class BudgetRecommendation {
        private final BudgetMode mode;
        private final double suggestedBudget;
        private final double suggestedSaving;
        private final String reason;
        private final boolean hasPastData;

        public BudgetRecommendation(BudgetMode mode, double budget, double saving, String reason, boolean hasPastData) {
            this.mode = mode;
            this.suggestedBudget = budget;
            this.suggestedSaving = saving;
            this.reason = reason;
            this.hasPastData = hasPastData;
        }

        public BudgetMode getMode() {
            return mode;
        }

        public double getSuggestedBudget() {
            return suggestedBudget;
        }

        public double getSuggestedSaving() {
            return suggestedSaving;
        }

        public String getReason() {
            return reason;
        }

        public boolean hasPastData() {
            return hasPastData;
        }
    }
}