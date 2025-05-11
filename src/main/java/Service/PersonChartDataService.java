/**
 * Provides data preparation services for generating chart data based on user transactions.
 * Supports annual and category-based chart data for income and expenses.
 *
 * @author Group 19
 * @version 1.0
 */
package Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Model.Transaction;
import Repository.TransactionRepository;

public class PersonChartDataService {

    private final TransactionRepository transactionRepository;

    /**
     * Constructs a PersonChartDataService instance with the specified transaction repository.
     *
     * @param transactionRepository the repository for accessing transaction data
     */
    public PersonChartDataService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Prepares annual chart data for a user, including total income, expenses, and monthly breakdowns.
     *
     * @param username     the username of the user
     * @param selectedYear the year for which to prepare data
     * @return an AnnualChartData object containing annual and monthly income and expense data
     */
    public AnnualChartData prepareAnnualChartData(String username, int selectedYear) {
        List<Transaction> transactions = transactionRepository.readTransactions(username);
        double annualIncome = 0, annualExpense = 0;
        Map<String, Double> monthlyIncomes = new HashMap<>();
        Map<String, Double> monthlyExpenses = new HashMap<>();

        // Initialize monthly data
        for (int i = 1; i <= 12; i++) {
            String month = String.format("%02d", i);
            monthlyIncomes.put(month, 0.0);
            monthlyExpenses.put(month, 0.0);
        }

        // Process transactions to calculate annual and monthly data
        for (Transaction tx : transactions) {
            String txYear = tx.getTimestamp().substring(0, 4);
            if (txYear.equals(String.valueOf(selectedYear))) {
                String month = tx.getTimestamp().substring(5, 7);
                if ("Income".equals(tx.getOperation())) {
                    annualIncome += tx.getAmount();
                    monthlyIncomes.put(month, monthlyIncomes.getOrDefault(month, 0.0) + tx.getAmount());
                } else if ("Expense".equals(tx.getOperation())) {
                    annualExpense += tx.getAmount();
                    monthlyExpenses.put(month, monthlyExpenses.getOrDefault(month, 0.0) + tx.getAmount());
                }
            }
        }

        return new AnnualChartData(annualIncome, annualExpense, monthlyIncomes, monthlyExpenses);
    }

    /**
     * Prepares category chart data for a user, categorizing income and expenses by transaction category.
     *
     * @param username     the username of the user
     * @param selectedYear the year for which to prepare data
     * @return a CategoryChartData object containing categorized income and expense data
     */
    public CategoryChartData prepareCategoryChartData(String username, int selectedYear) {
        List<Transaction> transactions = transactionRepository.readTransactions(username);
        Map<String, Double> incomeMap = new HashMap<>();
        Map<String, Double> expenseMap = new HashMap<>();
        String yearStr = String.valueOf(selectedYear);

        // Process transactions to calculate category data
        for (Transaction tx : transactions) {
            if (tx.getTimestamp() != null && tx.getTimestamp().startsWith(yearStr)) {
                if ("Income".equals(tx.getOperation())) {
                    incomeMap.put(tx.getCategory(), incomeMap.getOrDefault(tx.getCategory(), 0.0) + tx.getAmount());
                } else if ("Expense".equals(tx.getOperation())) {
                    expenseMap.put(tx.getCategory(), expenseMap.getOrDefault(tx.getCategory(), 0.0) + tx.getAmount());
                }
            }
        }

        List<Map.Entry<String, Double>> incomeCategories = new ArrayList<>(incomeMap.entrySet());
        List<Map.Entry<String, Double>> expenseCategories = new ArrayList<>(expenseMap.entrySet());

        return new CategoryChartData(incomeCategories, expenseCategories);
    }

    /**
     * Represents annual chart data, including total income, expenses, and monthly breakdowns.
     */
    public static class AnnualChartData {
        private final double annualIncome;
        private final double annualExpense;
        private final Map<String, Double> monthlyIncomes;
        private final Map<String, Double> monthlyExpenses;

        /**
         * Constructs an AnnualChartData instance with the specified data.
         *
         * @param annualIncome     the total income for the year
         * @param annualExpense    the total expenses for the year
         * @param monthlyIncomes   a map of months to income amounts
         * @param monthlyExpenses  a map of months to expense amounts
         */
        public AnnualChartData(double annualIncome, double annualExpense,
                               Map<String, Double> monthlyIncomes, Map<String, Double> monthlyExpenses) {
            this.annualIncome = annualIncome;
            this.annualExpense = annualExpense;
            this.monthlyIncomes = monthlyIncomes;
            this.monthlyExpenses = monthlyExpenses;
        }

        /**
         * Gets the total annual income.
         *
         * @return the annual income
         */
        public double getAnnualIncome() {
            return annualIncome;
        }

        /**
         * Gets the total annual expenses.
         *
         * @return the annual expenses
         */
        public double getAnnualExpense() {
            return annualExpense;
        }

        /**
         * Gets the monthly income data.
         *
         * @return a map of months to income amounts
         */
        public Map<String, Double> getMonthlyIncomes() {
            return monthlyIncomes;
        }

        /**
         * Gets the monthly expense data.
         *
         * @return a map of months to expense amounts
         */
        public Map<String, Double> getMonthlyExpenses() {
            return monthlyExpenses;
        }
    }

    /**
     * Represents category chart data, including categorized income and expense data.
     */
    public static class CategoryChartData {
        private final List<Map.Entry<String, Double>> incomeCategories;
        private final List<Map.Entry<String, Double>> expenseCategories;

        /**
         * Constructs a CategoryChartData instance with the specified data.
         *
         * @param incomeCategories  a list of category-amount pairs for income
         * @param expenseCategories a list of category-amount pairs for expenses
         */
        public CategoryChartData(List<Map.Entry<String, Double>> incomeCategories,
                                 List<Map.Entry<String, Double>> expenseCategories) {
            this.incomeCategories = incomeCategories;
            this.expenseCategories = expenseCategories;
        }

        /**
         * Gets the income categories and their amounts.
         *
         * @return a list of category-amount pairs for income
         */
        public List<Map.Entry<String, Double>> getIncomeCategories() {
            return incomeCategories;
        }

        /**
         * Gets the expense categories and their amounts.
         *
         * @return a list of category-amount pairs for expenses
         */
        public List<Map.Entry<String, Double>> getExpenseCategories() {
            return expenseCategories;
        }
    }
}