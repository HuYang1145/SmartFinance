package Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import Model.Transaction;
import Model.User;
import Repository.TransactionRepository;

/**
 * A service class for managing user transactions, including balance retrieval, transaction summaries,
 * expense calculations, and abnormal transaction detection.
 * This class interacts with a TransactionRepository for data access and a BudgetService for budget-related calculations.
 *
 * @version 1.3
 * @author group19
 */
public class TransactionService {
    /** Formatter for transaction timestamps in the format "yyyy/MM/dd HH:mm". */
    private static final DateTimeFormatter TRANSACTION_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    /** Formatter for dates in the format "yyyy/MM/dd". */
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /** The repository for accessing transaction data. */
    private final TransactionRepository transactionRepository;

    /** The service for budget-related calculations. */
    private final BudgetService budgetService;

    /** Threshold for frequent large transactions (amount >= 5000) within a single day. */
    private static final double FREQUENT_LARGE_TRANSACTION_AMOUNT = 5000.0;

    /** Threshold for the number of frequent large transactions (>= 3) within a single day. */
    private static final int FREQUENT_LARGE_TRANSACTION_COUNT = 3;

    /** Multiplier for detecting large expense transactions (> 3x average daily expense). */
    private static final double LARGE_EXPENSE_MULTIPLIER = 3.0;

    /** Threshold for detecting large expense or transfer out transactions (>= 50000). */
    private static final double LARGE_TRANSFER_OUT_AMOUNT = 50000.0;

    /**
     * Constructs a TransactionService instance with the specified dependencies.
     *
     * @param transactionRepository the repository for accessing transaction data, must not be null
     * @param budgetService the service for budget-related calculations, must not be null
     * @throws NullPointerException if transactionRepository or budgetService is null
     */
    public TransactionService(TransactionRepository transactionRepository, BudgetService budgetService) {
        this.transactionRepository = Objects.requireNonNull(transactionRepository, "TransactionRepository cannot be null");
        this.budgetService = Objects.requireNonNull(budgetService, "BudgetService cannot be null");
        System.out.println("TransactionService initialized with TransactionRepository and BudgetService.");
    }

    /**
     * Retrieves the current balance of a user.
     *
     * @param user the user whose balance is to be retrieved
     * @return the user's balance, or 0.0 if the user is null
     */
    public double getBalance(User user) {
        return user != null ? user.getBalance() : 0.0;
    }

    /**
     * Builds a CSV-formatted summary of a user's transactions.
     *
     * @param user the user whose transactions are to be summarized
     * @return a CSV string containing transaction details, or an empty string if the user is null or has no transactions
     */
    public String buildTransactionSummary(User user) {
        if (user == null) return "";
        List<Transaction> transactions = transactionRepository.findTransactionsByUser(user);
        if (transactions.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("Operation,Amount,Time,Merchant/Payee,Type,Category\n");
        for (Transaction tx : transactions) {
            if (tx == null) continue;

            String merchant = tx.getMerchant() != null ? tx.getMerchant() : "";
            String type = tx.getType() != null ? tx.getType() : "";
            String category = tx.getCategory() != null ? tx.getCategory() : "";
            String operation = tx.getOperation() != null ? tx.getOperation() : "";
            String timestamp = tx.getTimestamp() != null ? tx.getTimestamp() : "";

            sb.append(escapeForSummary(operation)).append(",")
              .append(String.format("%.2f", tx.getAmount())).append(",")
              .append(escapeForSummary(timestamp)).append(",")
              .append(escapeForSummary(merchant)).append(",")
              .append(escapeForSummary(type)).append(",")
              .append(escapeForSummary(category)).append("\n");
        }
        return sb.toString();
    }

    /**
     * Calculates the total expenses for the current month for a user.
     *
     * @param user the user whose expenses are to be calculated
     * @return the total expense amount for the current month, or 0.0 if the user is null
     */
    public double getCurrentMonthExpense(User user) {
        if (user == null) return 0.0;
        List<Transaction> transactions = transactionRepository.findTransactionsByUser(user);
        double totalExpense = 0.0;
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        for (Transaction tx : transactions) {
            if (tx == null || tx.getOperation() == null || tx.getTimestamp() == null) continue;

            if ("Expense".equalsIgnoreCase(tx.getOperation())) {
                try {
                    String[] dateTimeParts = tx.getTimestamp().split(" ");
                    LocalDate date = LocalDate.parse(dateTimeParts[0], DATE_ONLY_FORMATTER);

                    if (date.getYear() == currentYear && date.getMonthValue() == currentMonth) {
                        totalExpense += tx.getAmount();
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("Date parse error in getCurrentMonthExpense for transaction: " + tx.getTimestamp() + " - " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Unexpected error processing transaction in getCurrentMonthExpense: " + tx.getTimestamp() + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return totalExpense;
    }

    /**
     * Checks for abnormal transactions in a user's transaction history based on specific patterns.
     * Patterns include:
     * <ul>
     *   <li>Frequent large transactions (>= 5000, >= 3 times) on any single day.</li>
     *   <li>Single expense exceeding 3 times the average daily expense (over past 3 months).</li>
     *   <li>Single expense or transfer out transaction >= 50000.</li>
     * </ul>
     *
     * @param username the username to check transactions for
     * @param transactions the list of all transactions for the user to analyze
     * @return a list of strings describing detected abnormal patterns, or an empty list if none found
     */
    public List<String> checkAbnormalTransactions(String username, List<Transaction> transactions) {
        List<String> warnings = new ArrayList<>();
        if (username == null || username.trim().isEmpty() || transactions == null || transactions.isEmpty()) {
            return warnings;
        }

        System.out.println("Checking historical abnormal transactions for user " + username + " (" + transactions.size() + " transactions)...");

        double averageDailyExpense = budgetService.calculateAverageDailyExpense(username, 3);

        boolean largeExpenseFound = false;
        boolean largeTransferOrExpenseFound = false;

        for (Transaction tx : transactions) {
            if (tx == null || tx.getOperation() == null) continue;

            if ("Expense".equalsIgnoreCase(tx.getOperation()) && averageDailyExpense > 0 && tx.getAmount() > averageDailyExpense * LARGE_EXPENSE_MULTIPLIER) {
                largeExpenseFound = true;
            }

            if (("Expense".equalsIgnoreCase(tx.getOperation()) || "Transfer Out".equalsIgnoreCase(tx.getOperation()))
                && tx.getAmount() >= LARGE_TRANSFER_OUT_AMOUNT) {
                largeTransferOrExpenseFound = true;
            }

            if (largeExpenseFound && largeTransferOrExpenseFound) {
                break;
            }
        }

        if (largeExpenseFound) {
            warnings.add(String.format("A single expense transaction in your history exceeded %.1f times your average daily spending (avg daily: ¥%.2f).", LARGE_EXPENSE_MULTIPLIER, averageDailyExpense));
        }
        if (largeTransferOrExpenseFound) {
            warnings.add(String.format("A single Expense or Transfer Out transaction of over ¥%.2f was recorded.", LARGE_TRANSFER_OUT_AMOUNT));
        }

        Map<LocalDate, List<Transaction>> transactionsByDay = transactions.stream()
            .filter(tx -> tx != null && tx.getTimestamp() != null)
            .collect(Collectors.groupingBy(tx -> {
                try {
                    String[] dateTimeParts = tx.getTimestamp().split(" ");
                    return LocalDate.parse(dateTimeParts[0], DATE_ONLY_FORMATTER);
                } catch (DateTimeParseException e) {
                    System.err.println("Date parse error grouping transactions by day: " + tx.getTimestamp() + " - " + e.getMessage());
                    return null;
                }
            }));

        for (Map.Entry<LocalDate, List<Transaction>> entry : transactionsByDay.entrySet()) {
            if (entry.getKey() == null) continue;

            long largeTransactionCount = entry.getValue().stream()
                .filter(tx -> {
                    boolean isRelevantOperation = "Income".equalsIgnoreCase(tx.getOperation()) || "Expense".equalsIgnoreCase(tx.getOperation());
                    return tx != null && isRelevantOperation && tx.getAmount() >= FREQUENT_LARGE_TRANSACTION_AMOUNT;
                })
                .count();

            if (largeTransactionCount >= FREQUENT_LARGE_TRANSACTION_COUNT) {
                warnings.add(String.format("Frequent large transactions (>= ¥%.2f, >= %d times) occurred on %s.", FREQUENT_LARGE_TRANSACTION_AMOUNT, FREQUENT_LARGE_TRANSACTION_COUNT, entry.getKey().format(DATE_ONLY_FORMATTER)));
            }
        }

        System.out.println("Historical abnormal transaction check completed. Warnings found: " + warnings.size());
        return warnings;
    }

    /**
     * Checks if adding a new transaction triggers real-time abnormal transaction warnings.
     * Checks are performed before the transaction is added to the history and include:
     * <ul>
     *   <li>Frequent large transactions (>= 5000, >= 3 times) on the same day as the new transaction.</li>
     *   <li>New expense exceeding 3 times the average daily expense (over past 3 months).</li>
     *   <li>New expense or transfer out transaction >= 50000.</li>
     * </ul>
     *
     * @param username the username of the user
     * @param existingTransactions the list of existing transactions for the user
     * @param newTransaction the new transaction to check
     * @return a list of strings describing detected abnormal patterns, or an empty list if none found
     */
    public List<String> checkRealtimeAbnormalTransactions(String username, List<Transaction> existingTransactions, Transaction newTransaction) {
        List<String> warnings = new ArrayList<>();
        if (newTransaction == null || username == null || username.trim().isEmpty()) {
            return warnings;
        }
        if (newTransaction.getOperation() == null || newTransaction.getTimestamp() == null) {
            System.err.println("Real-time check: New transaction is missing operation or timestamp. Cannot perform full check.");
            warnings.add("Cannot perform full risk check due to invalid new transaction data (missing operation or time).");
            if (newTransaction.getAmount() < 0 || newTransaction.getOperation() == null) {
                return warnings;
            }
        }

        double averageDailyExpense = budgetService.calculateAverageDailyExpense(username, 3);

        if ("Expense".equalsIgnoreCase(newTransaction.getOperation()) && averageDailyExpense > 0 && newTransaction.getAmount() > averageDailyExpense * LARGE_EXPENSE_MULTIPLIER) {
            warnings.add(String.format("This expense (¥%.2f) exceeds %.1f times your average daily spending (¥%.2f).", newTransaction.getAmount(), LARGE_EXPENSE_MULTIPLIER, averageDailyExpense));
        }

        if (("Expense".equalsIgnoreCase(newTransaction.getOperation()) || "Transfer Out".equalsIgnoreCase(newTransaction.getOperation()))
            && newTransaction.getAmount() >= LARGE_TRANSFER_OUT_AMOUNT) {
            warnings.add(String.format("This Expense or Transfer Out transaction (¥%.2f) is over ¥%.2f.", newTransaction.getAmount(), LARGE_TRANSFER_OUT_AMOUNT));
        }

        if (newTransaction.getTimestamp() != null && newTransaction.getOperation() != null &&
            ("Income".equalsIgnoreCase(newTransaction.getOperation()) || "Expense".equalsIgnoreCase(newTransaction.getOperation())) &&
            newTransaction.getAmount() >= FREQUENT_LARGE_TRANSACTION_AMOUNT) {
            try {
                String[] dateTimeParts = newTransaction.getTimestamp().split(" ");
                LocalDate newTransactionDay = LocalDate.parse(dateTimeParts[0], DATE_ONLY_FORMATTER);

                long existingLargeTransactionsToday = (existingTransactions == null) ? 0 : existingTransactions.stream()
                    .filter(tx -> tx != null && tx.getTimestamp() != null && tx.getOperation() != null)
                    .filter(tx -> {
                        try {
                            String[] existingDateTimeParts = tx.getTimestamp().split(" ");
                            LocalDate txDay = LocalDate.parse(existingDateTimeParts[0], DATE_ONLY_FORMATTER);
                            return txDay.equals(newTransactionDay);
                        } catch (DateTimeParseException e) {
                            System.err.println("Date parse error filtering for daily check on existing transaction: " + tx.getTimestamp() + " - " + e.getMessage());
                            return false;
                        } catch (Exception e) {
                            System.err.println("Unexpected error parsing existing timestamp for daily check: " + tx.getTimestamp() + " - " + e.getMessage());
                            e.printStackTrace();
                            return false;
                        }
                    })
                    .filter(tx -> {
                        boolean isRelevantOperation = "Income".equalsIgnoreCase(tx.getOperation()) || "Expense".equalsIgnoreCase(tx.getOperation());
                        return isRelevantOperation && tx.getAmount() >= FREQUENT_LARGE_TRANSACTION_AMOUNT;
                    })
                    .count();

                if ((existingLargeTransactionsToday + 1) >= FREQUENT_LARGE_TRANSACTION_COUNT) {
                    warnings.add(String.format("Adding this transaction (¥%.2f) will result in %d large transactions (>= ¥%.2f) today (%s).",
                        newTransaction.getAmount(), existingLargeTransactionsToday + 1, FREQUENT_LARGE_TRANSACTION_AMOUNT, newTransactionDay.format(DATE_ONLY_FORMATTER)));
                }

            } catch (DateTimeParseException e) {
                System.err.println("Date parse error for new transaction timestamp during realtime daily check: " + newTransaction.getTimestamp() + " - " + e.getMessage());
                warnings.add("Cannot perform daily transaction frequency check due to invalid date format in new transaction: " + newTransaction.getTimestamp());
            } catch (Exception e) {
                System.err.println("Unexpected error during realtime daily frequency check: " + e.getMessage());
                e.printStackTrace();
                warnings.add("An error occurred during real-time daily frequency check.");
            }
        }

        System.out.println("Real-time abnormal transaction check completed. Warnings found: " + warnings.size());
        return warnings;
    }

    /**
     * Gets the TransactionRepository associated with this service.
     *
     * @return the TransactionRepository instance
     */
    public TransactionRepository getTransactionRepository() {
        return transactionRepository;
    }

    /**
     * Escapes a field for CSV formatting by enclosing it in quotes if it contains commas,
     * quotes, newlines, carriage returns, or is empty.
     *
     * @param field the field to escape
     * @return the escaped field, or an empty string if the field is null
     */
    private String escapeForSummary(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r") || field.isEmpty()) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}