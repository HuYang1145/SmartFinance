package Controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Model.Transaction;
import Model.User;
import Model.UserSession;
import Repository.AccountRepository;
import Service.BudgetService;
import View.Bill.ExpenseDialogView;
import View.Bill.IncomeDialogView;

/**
 * Manages bill-related operations for the finance management system, including transaction filtering,
 * category calculations, and processing income/expense transactions. Uses caching to optimize
 * transaction retrieval and integrates with the account repository for user data.
 *
 * @author Group 19
 * @version 1.0
 */
public class BillController {
    /** Repository for managing user account data. */
    private final AccountRepository accountRepository;
    /** Cache for storing user transactions. */
    private static Map<String, List<Transaction>> transactionCache = new HashMap<>();
    /** Timestamps for cache validity tracking. */
    private static Map<String, Long> cacheTimestamps = new HashMap<>();
    /** Cache expiry duration in milliseconds (5 minutes). */
    private static final long CACHE_EXPIRY_MS = 5 * 60 * 1000;
    /** Date format for parsing transaction timestamps. */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    /** Code for unclassified transaction types. */
    private static final String UNCLASSIFIED_TYPE_CODE = "u";
    /** Placeholder for income transactions. */
    private static final String INCOME_PLACEHOLDER = "I";

    /**
     * Constructs a BillController with the specified account repository.
     *
     * @param accountRepository the repository for user account data
     */
    public BillController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Checks if the transaction cache for a user is still valid.
     *
     * @param username the username of the user
     * @return true if the cache is valid, false otherwise
     */
    private boolean isCacheValid(String username) {
        Long timestamp = cacheTimestamps.get(username);
        if (timestamp == null) return false;
        return (System.currentTimeMillis() - timestamp) < CACHE_EXPIRY_MS;
    }

    /**
     * Retrieves cached transactions for a user, refreshing the cache if expired.
     *
     * @param username the username of the user
     * @return a list of transactions for the user
     */
    public List<Transaction> getCachedTransactions(String username) {
        if (isCacheValid(username)) {
            return transactionCache.getOrDefault(username, new ArrayList<>());
        }
        List<Transaction> transactions = TransactionController.readTransactions(username);
        System.out.println("Transactions loaded for user " + username + ": " + (transactions != null ? transactions.size() : 0));
        transactionCache.put(username, transactions != null ? transactions : new ArrayList<>());
        cacheTimestamps.put(username, System.currentTimeMillis());
        return transactionCache.get(username);
    }

    /**
     * Filters transactions for a user within the specified date range.
     *
     * @param username       the username of the user
     * @param startYearMonth the start date in format "YYYY/MM"
     * @param endYearMonth   the end date in format "YYYY/MM"
     * @return a list of transactions within the date range
     */
    public List<Transaction> getFilteredTransactions(String username, String startYearMonth, String endYearMonth) {
        List<Transaction> transactions = getCachedTransactions(username);
        List<Transaction> filtered = new ArrayList<>();

        try {
            LocalDate startDate = LocalDate.parse(startYearMonth + "/01", BudgetService.DATE_FORMATTER);
            LocalDate tempEndDate = LocalDate.parse(endYearMonth + "/01", BudgetService.DATE_FORMATTER);
            LocalDate endDate = tempEndDate.withDayOfMonth(tempEndDate.lengthOfMonth());
            for (Transaction tx : transactions) {
                try {
                    LocalDate txDate = LocalDate.parse(tx.getTimestamp(), BudgetService.DATE_FORMATTER);
                    if (!txDate.isBefore(startDate) && !txDate.isAfter(endDate)) {
                        filtered.add(tx);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing transaction timestamp: " + tx.getTimestamp());
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing date range: " + startYearMonth + " to " + endYearMonth);
        }

        System.out.println("Filtered transactions for range " + startYearMonth + " to " + endYearMonth + ": " + filtered.size());
        return filtered;
    }

    /**
     * Calculates total expense amounts by category field for the given transactions.
     *
     * @param transactions  the list of transactions to process
     * @param categoryField the field to categorize expenses (e.g., "category", "type")
     * @return a map of category names to total expense amounts
     */
    public Map<String, Double> calculateExpenseCategoryTotals(List<Transaction> transactions, String categoryField) {
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Transaction tx : transactions) {
            if ("Expense".equalsIgnoreCase(tx.getOperation())) {
                String category = getFieldValue(tx, categoryField);
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + Math.abs(tx.getAmount()));
            }
        }
        return categoryTotals;
    }

    /**
     * Calculates total income amounts by category field for the given transactions.
     *
     * @param transactions  the list of transactions to process
     * @param categoryField the field to categorize incomes (e.g., "category", "type")
     * @return a map of category names to total income amounts
     */
    public Map<String, Double> calculateIncomeCategoryTotals(List<Transaction> transactions, String categoryField) {
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Transaction tx : transactions) {
            if ("Income".equalsIgnoreCase(tx.getOperation())) {
                String category = getFieldValue(tx, categoryField);
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + tx.getAmount());
            }
        }
        return categoryTotals;
    }

    /**
     * Retrieves the value of a specified field from a transaction, returning "Unclassified" if empty.
     *
     * @param t     the transaction to process
     * @param field the field to retrieve (e.g., "category", "type")
     * @return the field value or "Unclassified" if not set
     */
    private String getFieldValue(Transaction t, String field) {
        switch (field) {
            case "category": return t.getCategory() != null && !t.getCategory().trim().isEmpty() ? t.getCategory().trim() : "Unclassified";
            case "type": return t.getType() != null && !t.getType().trim().isEmpty() ? t.getType().trim() : "Unclassified";
            case "payment_method": return t.getPaymentMethod() != null && !t.getPaymentMethod().trim().isEmpty() ? t.getPaymentMethod().trim() : "Unclassified";
            case "location": return t.getLocation() != null && !t.getLocation().trim().isEmpty() ? t.getLocation().trim() : "Unclassified";
            case "merchant": return t.getMerchant() != null && !t.getMerchant().trim().isEmpty() ? t.getMerchant().trim() : "Unclassified";
            case "tag": return t.getTag() != null && !t.getTag().trim().isEmpty() ? t.getTag().trim() : "Unclassified";
            case "recurrence": return t.getRecurrence() != null && !t.getRecurrence().trim().isEmpty() ? t.getRecurrence().trim() : "Unclassified";
            default: return "Unclassified";
        }
    }

    /**
     * Processes an expense transaction from the expense dialog view, validating input and updating user balance.
     *
     * @param view the expense dialog view containing input fields
     */
    public void processExpense(ExpenseDialogView view) {
        String currentUsername = UserSession.getCurrentUsername();
        if (currentUsername == null) {
            view.showError("Error: Not logged in.");
            view.dispose();
            return;
        }

        String amountText = view.getAmountField().getText();
        String timeText = view.getTimeField().getText();
        String merchantText = view.getMerchantField().getText();
        String selectedType = (String) view.getTypeComboBox().getSelectedItem();
        String password = new String(view.getPasswordField().getPassword());

        if (amountText.isEmpty() || timeText.isEmpty() || merchantText.isEmpty() || password.isEmpty()) {
            view.showError("Amount, Time, Merchant, and Password must be filled.");
            return;
        }

        if (INCOME_PLACEHOLDER.equalsIgnoreCase(merchantText.trim())) {
            view.showError("Invalid merchant name.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                view.showError("Amount must be positive.");
                return;
            }
        } catch (NumberFormatException ex) {
            view.showError("Invalid amount format.");
            return;
        }

        try {
            DATE_FORMAT.setLenient(false);
            DATE_FORMAT.parse(timeText.trim());
        } catch (ParseException ex) {
            view.showError("Invalid time format. Use yyyy/MM/dd HH:mm");
            return;
        }

        String typeToRecord = UNCLASSIFIED_TYPE_CODE;
        if (selectedType != null && !selectedType.equals("(Select Type)")) {
            typeToRecord = selectedType;
        }
        if (INCOME_PLACEHOLDER.equalsIgnoreCase(typeToRecord)) {
            view.showError("Invalid expense type selected.");
            return;
        }

        List<User> accounts = accountRepository.readFromCSV();
        User currentUserAccount = null;
        boolean passwordCorrect = false;

        for (User account : accounts) {
            if (account.getUsername().equals(currentUsername)) {
                currentUserAccount = account;
                if (account.getPassword().equals(password)) {
                    passwordCorrect = true;
                }
                break;
            }
        }

        if (currentUserAccount == null) {
            view.showError("Current user account not found.");
            return;
        }

        if (!passwordCorrect) {
            view.showError("Incorrect password.");
            view.clearPassword();
            return;
        }

        if (!currentUserAccount.getAccountType().equalsIgnoreCase("Personal")) {
            view.showError("This account type does not support expense operations.");
            return;
        }

        if (currentUserAccount.getBalance() < amount) {
            view.showError("Insufficient balance for this expense.");
            view.clearPassword();
            return;
        }

        boolean transactionAdded = TransactionController.addTransaction(
                currentUsername, "Expense", amount, timeText.trim(), merchantText.trim(), typeToRecord
        );

        if (transactionAdded) {
            double originalBalance = currentUserAccount.getBalance();
            currentUserAccount.setBalance(originalBalance - amount);
            boolean saved = accountRepository.saveToCSV(accounts, false);

            if (saved) {
                UserSession.setCurrentAccount(currentUserAccount);
                view.showSuccess("Successfully added expense of ¥" + "%.2f".formatted(amount) + "!");
                view.dispose();
                transactionCache.remove(currentUsername);
                cacheTimestamps.remove(currentUsername);
            } else {
                TransactionController.removeTransaction(currentUsername, timeText.trim());
                currentUserAccount.setBalance(originalBalance);
                view.showError("Expense recorded successfully, but failed to update account balance file.");
            }
        } else {
            view.showError("Failed to add expense.");
        }
        view.clearPassword();
    }

    /**
     * Processes an income transaction from the income dialog view, validating input and updating user balance.
     *
     * @param view the income dialog view containing input fields
     */
    public void processIncome(IncomeDialogView view) {
        String currentUsername = UserSession.getCurrentUsername();
        if (currentUsername == null) {
            view.showError("Error: Not logged in.");
            view.dispose();
            return;
        }

        String amountText = view.getAmountField().getText();
        String timeText = view.getTimeField().getText();
        String password = new String(view.getPasswordField().getPassword());

        if (amountText.isEmpty() || timeText.isEmpty() || password.isEmpty()) {
            view.showError("All fields must be filled.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                view.showError("Amount must be positive.");
                return;
            }
        } catch (NumberFormatException ex) {
            view.showError("Invalid amount format.");
            return;
        }

        try {
            DATE_FORMAT.setLenient(false);
            DATE_FORMAT.parse(timeText.trim());
        } catch (ParseException ex) {
            view.showError("Invalid time format. Use yyyy/MM/dd HH:mm");
            return;
        }

        List<User> accounts = accountRepository.readFromCSV();
        User currentUserAccount = null;
        boolean passwordCorrect = false;

        for (User account : accounts) {
            if (account.getUsername().equals(currentUsername)) {
                currentUserAccount = account;
                if (account.getPassword().equals(password)) {
                    passwordCorrect = true;
                }
                break;
            }
        }

        if (currentUserAccount == null) {
            view.showError("Current user account not found.");
            return;
        }

        if (!passwordCorrect) {
            view.showError("Incorrect password.");
            view.clearPassword();
            return;
        }

        boolean transactionAdded = TransactionController.addTransaction(
                currentUsername, "Income", amount, timeText.trim(), "I", "I"
        );

        if (transactionAdded) {
            double originalBalance = currentUserAccount.getBalance();
            currentUserAccount.setBalance(originalBalance + amount);
            boolean saved = accountRepository.saveToCSV(accounts, false);

            if (saved) {
                UserSession.setCurrentAccount(currentUserAccount);
                view.showSuccess("Income of ¥" + "%.2f".formatted(amount) + " added successfully!");
                view.dispose();
                transactionCache.remove(currentUsername);
                cacheTimestamps.remove(currentUsername);
            } else {
                TransactionController.removeTransaction(currentUsername, timeText.trim());
                currentUserAccount.setBalance(originalBalance);
                view.showError("Income recorded, but failed to update account balance file.");
            }
        } else {
            view.showError("Failed to add income.");
        }
        view.clearPassword();
    }
}