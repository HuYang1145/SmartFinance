package Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Model.Transaction;
import Repository.TransactionRepository;


public class PersonFinancialService {

    private final TransactionRepository transactionRepository;

    
    public PersonFinancialService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public FinancialSummary calculateFinancialSummary(String username, int selectedYear) {
        List<Transaction> transactions = transactionRepository.readTransactions(username);
        double totalIncomeYear = 0, totalExpenseYear = 0;
        double totalIncomeLastYear = 0, totalExpenseLastYear = 0;
        double accountBalance = 0;
        int lastYear = selectedYear - 1;

        // Process transactions to calculate totals
        for (Transaction tx : transactions) {
            String txYear = tx.getTimestamp().substring(0, 4);
            if ("Income".equals(tx.getOperation())) {
                accountBalance += tx.getAmount();
                if (txYear.equals(String.valueOf(selectedYear))) {
                    totalIncomeYear += tx.getAmount();
                }
                if (txYear.equals(String.valueOf(lastYear))) {
                    totalIncomeLastYear += tx.getAmount();
                }
            } else if ("Expense".equals(tx.getOperation())) {
                accountBalance -= tx.getAmount();
                if (txYear.equals(String.valueOf(selectedYear))) {
                    totalExpenseYear += tx.getAmount();
                }
                if (txYear.equals(String.valueOf(lastYear))) {
                    totalExpenseLastYear += tx.getAmount();
                }
            }
        }

        // Calculate changes
        double incomeChangeYear = totalIncomeLastYear > 0 ? ((totalIncomeYear - totalIncomeLastYear) / totalIncomeLastYear) * 100 : 0;
        double expenseChangeYear = totalExpenseLastYear > 0 ? ((totalExpenseYear - totalExpenseLastYear) / totalExpenseLastYear) * 100 : 0;

        return new FinancialSummary(totalIncomeYear, totalExpenseYear, accountBalance, incomeChangeYear, expenseChangeYear);
    }

    public String generatePaymentLocationSummary(String username, int selectedYear) {
        List<Transaction> transactions = transactionRepository.readTransactions(username);
        Map<String, Double> paymentMethods = new HashMap<>();
        Map<String, Double> locations = new HashMap<>();
        double totalExpense = 0;
        int transactionCount = 0;
        double maxSingleTransaction = 0;
        String maxTransactionCategory = "None";

        // Process transactions for payment and location summary
        for (Transaction tx : transactions) {
            if ("Expense".equals(tx.getOperation()) && tx.getTimestamp().startsWith(String.valueOf(selectedYear))) {
                paymentMethods.put(tx.getPaymentMethod(), paymentMethods.getOrDefault(tx.getPaymentMethod(), 0.0) + tx.getAmount());
                locations.put(tx.getLocation(), locations.getOrDefault(tx.getLocation(), 0.0) + tx.getAmount());
                totalExpense += tx.getAmount();
                transactionCount++;
                if (tx.getAmount() > maxSingleTransaction) {
                    maxSingleTransaction = tx.getAmount();
                    maxTransactionCategory = tx.getCategory();
                }
            }
        }

        final double finalTotalExpense = totalExpense > 0 ? totalExpense : 1;
        String primaryPayment = paymentMethods.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(entry -> String.format("%s (%.1f%%)", entry.getKey(), (entry.getValue() / finalTotalExpense) * 100))
            .orElse("No payment data");
        String primaryLocation = locations.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(entry -> String.format("%s (%.1f%%)", entry.getKey(), (entry.getValue() / finalTotalExpense) * 100))
            .orElse("No location data");

        return String.format("""
            Summary:
            - Most transactions were made via %s.
            - Primary transaction locations were %s.
            - Total transactions: %d
            - Highest single transaction: ¥%.2f (%s)""",
            primaryPayment, primaryLocation, transactionCount, maxSingleTransaction, maxTransactionCategory
        );
    }

    // Inner class to hold financial summary data
    public static class FinancialSummary {
        private final double totalIncomeYear;
        private final double totalExpenseYear;
        private final double accountBalance;
        private final double incomeChangeYear;
        private final double expenseChangeYear;

        public FinancialSummary(double totalIncomeYear, double totalExpenseYear, double accountBalance,
                                double incomeChangeYear, double expenseChangeYear) {
            this.totalIncomeYear = totalIncomeYear;
            this.totalExpenseYear = totalExpenseYear;
            this.accountBalance = accountBalance;
            this.incomeChangeYear = incomeChangeYear;
            this.expenseChangeYear = expenseChangeYear;
        }

        public double getTotalIncomeYear() {
            return totalIncomeYear;
        }

        public double getTotalExpenseYear() {
            return totalExpenseYear;
        }

        public double getAccountBalance() {
            return accountBalance;
        }

        public double getIncomeChangeYear() {
            return incomeChangeYear;
        }

        public double getExpenseChangeYear() {
            return expenseChangeYear;
        }

        public double getTotalBalanceYear() {
            return totalIncomeYear - totalExpenseYear;
        }
    }
}