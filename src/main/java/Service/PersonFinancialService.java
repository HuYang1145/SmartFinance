/**
 * Provides financial analysis services for a user, including annual financial summaries and payment location summaries.
 * Processes transaction data to generate insights into income, expenses, and spending patterns.
 *
 * @author Group 19
 * @version 1.0
 */
package Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Model.Transaction;
import Repository.TransactionRepository;

public class PersonFinancialService {

    private final TransactionRepository transactionRepository;

    /**
     * Constructs a PersonFinancialService instance with the specified transaction repository.
     *
     * @param transactionRepository the repository for accessing transaction data
     */
    public PersonFinancialService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Calculates a financial summary for a user, including total income, expenses, balance, and year-over-year changes.
     *
     * @param username     the username of the user
     * @param selectedYear the year for which to calculate the summary
     * @return a FinancialSummary object containing financial metrics
     */
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

    /**
     * Generates a summary of payment methods and locations for a user's expenses in the specified year.
     *
     * @param username     the username of the user
     * @param selectedYear the year for which to generate the summary
     * @return a formatted string summarizing payment methods, locations, and transaction details
     */
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

        return String.format(
            "Summary:\n" +
        "- Most transactions were made via %s.\n" +
        "- Primary transaction locations were %s.\n" +
        "- Total transactions: %d\n" +
        "- Highest single transaction: ¥%.2f (%s)",
            primaryPayment, primaryLocation, transactionCount, maxSingleTransaction, maxTransactionCategory
        );
    }

    /**
     * Represents a financial summary, including total income, expenses, balance, and year-over-year changes.
     */
    public static class FinancialSummary {
        private final double totalIncomeYear;
        private final double totalExpenseYear;
        private final double accountBalance;
        private final double incomeChangeYear;
        private final double expenseChangeYear;

        /**
         * Constructs a FinancialSummary instance with the specified financial metrics.
         *
         * @param totalIncomeYear    the total income for the selected year
         * @param totalExpenseYear   the total expenses for the selected year
         * @param accountBalance     the overall account balance
         * @param incomeChangeYear   the percentage change in income from the previous year
         * @param expenseChangeYear  the percentage change in expenses from the previous year
         */
        public FinancialSummary(double totalIncomeYear, double totalExpenseYear, double accountBalance,
                                double incomeChangeYear, double expenseChangeYear) {
            this.totalIncomeYear = totalIncomeYear;
            this.totalExpenseYear = totalExpenseYear;
            this.accountBalance = accountBalance;
            this.incomeChangeYear = incomeChangeYear;
            this.expenseChangeYear = expenseChangeYear;
        }

        /**
         * Gets the total income for the selected year.
         *
         * @return the total income
         */
        public double getTotalIncomeYear() {
            return totalIncomeYear;
        }

        /**
         * Gets the total expenses for the selected year.
         *
         * @return the total expenses
         */
        public double getTotalExpenseYear() {
            return totalExpenseYear;
        }

        /**
         * Gets the overall account balance.
         *
         * @return the account balance
         */
        public double getAccountBalance() {
            return accountBalance;
        }

        /**
         * Gets the percentage change in income from the previous year.
         *
         * @return the income change percentage
         */
        public double getIncomeChangeYear() {
            return incomeChangeYear;
        }

        /**
         * Gets the percentage change in expenses from the previous year.
         *
         * @return the expense change percentage
         */
        public double getExpenseChangeYear() {
            return expenseChangeYear;
        }

        /**
         * Calculates the net balance for the selected year (income minus expenses).
         *
         * @return the net balance for the year
         */
        public double getTotalBalanceYear() {
            return totalIncomeYear - totalExpenseYear;
        }
    }
}