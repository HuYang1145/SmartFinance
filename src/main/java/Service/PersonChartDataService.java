package Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Model.Transaction;
import Repository.TransactionRepository;


public class PersonChartDataService {

    private final TransactionRepository transactionRepository;

    
    public PersonChartDataService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

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

    // Inner class to hold annual chart data
    public static class AnnualChartData {
        private final double annualIncome;
        private final double annualExpense;
        private final Map<String, Double> monthlyIncomes;
        private final Map<String, Double> monthlyExpenses;

        public AnnualChartData(double annualIncome, double annualExpense,
                               Map<String, Double> monthlyIncomes, Map<String, Double> monthlyExpenses) {
            this.annualIncome = annualIncome;
            this.annualExpense = annualExpense;
            this.monthlyIncomes = monthlyIncomes;
            this.monthlyExpenses = monthlyExpenses;
        }

        public double getAnnualIncome() {
            return annualIncome;
        }

        public double getAnnualExpense() {
            return annualExpense;
        }

        public Map<String, Double> getMonthlyIncomes() {
            return monthlyIncomes;
        }

        public Map<String, Double> getMonthlyExpenses() {
            return monthlyExpenses;
        }
    }

    // Inner class to hold category chart data
    public static class CategoryChartData {
        private final List<Map.Entry<String, Double>> incomeCategories;
        private final List<Map.Entry<String, Double>> expenseCategories;

        public CategoryChartData(List<Map.Entry<String, Double>> incomeCategories,
                                 List<Map.Entry<String, Double>> expenseCategories) {
            this.incomeCategories = incomeCategories;
            this.expenseCategories = expenseCategories;
        }

        public List<Map.Entry<String, Double>> getIncomeCategories() {
            return incomeCategories;
        }

        public List<Map.Entry<String, Double>> getExpenseCategories() {
            return expenseCategories;
        }
    }
}