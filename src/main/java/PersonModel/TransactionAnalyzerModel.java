package PersonModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import AccountModel.TransactionServiceModel;
import AccountModel.TransactionServiceModel.TransactionData;

/**
 * Model class for analyzing transaction data, providing category totals and filtered transactions.
 */
public class TransactionAnalyzerModel {

    /**
     * Calculates the total amount for each expense category based on transaction data.
     *
     * @param transactions The list of transactions to analyze.
     * @return A map of category names to their total amounts.
     */
    public static Map<String, Double> calculateExpenseCategoryTotals(List<TransactionData> transactions) {
        Map<String, Double> categoryTotals = new HashMap<>();
        if (transactions == null) {
            System.err.println("TransactionAnalyzer: Received null transaction list.");
            return categoryTotals;
        }

        for (TransactionData transaction : transactions) {
            if ("Expense".equalsIgnoreCase(transaction.getOperation())) {
                String category = transaction.getCategory();
                if (category == null || category.trim().isEmpty() || "u".equalsIgnoreCase(category.trim())) {
                    category = "Unclassified";
                } else {
                    category = category.trim();
                }
                double amount = transaction.getAmount();
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
            }
        }
        return categoryTotals;
    }

    /**
     * Calculates the total amount for each income category based on transaction data.
     *
     * @param transactions The list of transactions to analyze.
     * @return A map of category names to their total amounts.
     */
    public static Map<String, Double> calculateIncomeCategoryTotals(List<TransactionData> transactions) {
        Map<String, Double> categoryTotals = new HashMap<>();
        if (transactions == null) {
            System.err.println("TransactionAnalyzer: Received null transaction list.");
            return categoryTotals;
        }

        for (TransactionData transaction : transactions) {
            if ("Income".equalsIgnoreCase(transaction.getOperation())) {
                String category = transaction.getCategory();
                if (category == null || category.trim().isEmpty() || "u".equalsIgnoreCase(category.trim())) {
                    category = "Unclassified";
                } else {
                    category = category.trim();
                }
                double amount = transaction.getAmount();
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
            }
        }
        return categoryTotals;
    }

    /**
     * Filters transactions by year and month.
     *
     * @param username   The username to fetch transactions for.
     * @param yearMonth  The year and month to filter transactions (format: yyyy/MM).
     * @return A list of filtered transactions.
     */
    public static List<TransactionData> getFilteredTransactions(String username, String yearMonth) {
        List<TransactionData> transactions = TransactionServiceModel.readTransactions(username);
        List<TransactionData> filtered = new ArrayList<>();
        if (transactions == null) {
            System.err.println("No transactions found for user: " + username);
            return filtered;
        }

        String yearMonthPrefix = yearMonth + "/";
        for (TransactionData t : transactions) {
            String transactionTime = t.getTime();
            if (transactionTime != null && transactionTime.startsWith(yearMonthPrefix)) {
                filtered.add(t);
            }
        }
        return filtered;
    }
}