/**
 * Provides services for managing and analyzing user transactions, including balance queries,
 * transaction summaries, and monthly expense calculations.
 *
 * @author Group 19
 * @version 1.0
 */
package Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

import Model.Transaction;
import Model.User;
import Repository.TransactionRepository;
import Service.BudgetService;

public class TransactionService {
    private static final DateTimeFormatter TRANSACTION_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
     private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd"); // Added for date parsing

    private final TransactionRepository transactionRepository;
    private final BudgetService budgetService; // Added dependency

    // Define the thresholds for new abnormal checks
    private static final double FREQUENT_LARGE_TRANSACTION_AMOUNT = 5000.0;
    private static final int FREQUENT_LARGE_TRANSACTION_COUNT = 3;
    private static final double LARGE_EXPENSE_MULTIPLIER = 3.0; // Expense > 3 * avg_daily_expense
    private static final double LARGE_TRANSFER_OUT_AMOUNT = 10000.0; // Transfer Out > 5000

    /**
     * Constructs a TransactionService instance with the specified dependencies.
     *
     * @param transactionRepository the repository for accessing transaction data
     * @param budgetService         the service for budget-related calculations
     */
    public TransactionService(TransactionRepository transactionRepository, BudgetService budgetService) {
        this.transactionRepository = transactionRepository;
        this.budgetService = budgetService;
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
            String merchant = tx.getMerchant() != null ? tx.getMerchant() : "";
            String type = tx.getType() != null ? tx.getType() : "";
             String category = tx.getCategory() != null ? tx.getCategory() : "";
            sb.append(tx.getOperation()).append(",")
              .append(String.format("%.2f", tx.getAmount())).append(",")
              .append(tx.getTimestamp()).append(",")
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
            if ("Expense".equalsIgnoreCase(tx.getOperation())) {
                try {
                    // Assuming timestamp is yyyy/MM/dd HH:mm or yyyy/MM/dd
                    String[] dateTimeParts = tx.getTimestamp().split(" ");
                    LocalDate date = LocalDate.parse(dateTimeParts[0], DATE_ONLY_FORMATTER); // Use DATE_ONLY_FORMATTER

                    if (date.getYear() == currentYear && date.getMonthValue() == currentMonth) {
                        totalExpense += tx.getAmount();
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("Date parse error in getCurrentMonthExpense: " + tx.getTimestamp());
                }
            }
        }
        return totalExpense;
    }

    /**
     * Checks for abnormal transactions for a given user based on specific patterns in their history.
     * Combines checks for:
     * 1. Frequent large transactions on any single day (>= 3 transactions with amount >= 2000).
     * 2. Any single expense transaction exceeding 3 times the user's average daily expense.
     * 3. Any single expense transaction (interpreting "转出" as Expense) exceeding 5000.
     *
     * @param username     The username to check transactions for.
     * @param transactions The list of transactions to analyze.
     * @return A list of strings describing the detected abnormal patterns, or an empty list if none found.
     */
    public List<String> checkAbnormalTransactions(String username, List<Transaction> transactions) {
        List<String> warnings = new ArrayList<>();
        if (username == null || username.trim().isEmpty() || transactions == null || transactions.isEmpty()) {
            return warnings; // Return empty list if no data
        }

        System.out.println("Checking historical abnormal transactions for user " + username + "...");

        // Pattern 2 & 3 Checks (per transaction, across all history)
        double averageDailyExpense = budgetService.calculateAverageDailyExpense(username, 3); // Use last 3 full months for average
        boolean largeExpenseFound = false;
        boolean largeTransferOutFound = false; // Assuming '转出' corresponds to Expense operation > 5000

        for (Transaction tx : transactions) {
            if (tx == null) continue;

            // Pattern 2: Single large expense (> 3 * avg daily)
            if ("Expense".equalsIgnoreCase(tx.getOperation()) && tx.getAmount() > averageDailyExpense * LARGE_EXPENSE_MULTIPLIER && averageDailyExpense > 0) {
                 largeExpenseFound = true;
            }

            // Pattern 3: Single large "transfer out" (> 5000) - interpreting "转出" as Expense operation here
            // This might be ambiguous with the UI's "Operation" dropdown (Income/Expense).
            // If 'Transfer Out' is a 'Type' within 'Expense', the old logic from TC was checking Type.
            // If 'Transfer Out' is a distinct 'Operation', this check should filter by Operation.
            // Based on the prompt "转出金额大于5000", it's likely an Operation.
            // Let's check Operation == "Transfer Out" (if that operation exists) OR Operation == "Expense" with amount > 5000.
            // Given the available operations (Income/Expense in UI, or the old list in TC),
            // let's assume "转出金额大于5000" means an 'Expense' transaction with amount > 5000 for simplicity based on current UI.
            if ("Expense".equalsIgnoreCase(tx.getOperation()) && tx.getAmount() > LARGE_TRANSFER_OUT_AMOUNT) {
                 largeTransferOutFound = true;
            }
        }
         if (largeExpenseFound) {
             warnings.add(String.format("A single expense transaction exceeded %.1f times your average daily spending (avg daily: ¥%.2f).", LARGE_EXPENSE_MULTIPLIER, averageDailyExpense));
         }
         if (largeTransferOutFound) {
             warnings.add(String.format("A single expense transaction of over ¥%.2f was recorded.", LARGE_TRANSFER_OUT_AMOUNT));
         }


        // Pattern 1 Check (frequent large transactions per day)
        // Group transactions by day
        Map<LocalDate, List<Transaction>> transactionsByDay = transactions.stream()
            .filter(tx -> tx != null && tx.getTimestamp() != null)
            .collect(Collectors.groupingBy(tx -> {
                try {
                    // Parse only date part for grouping
                    return LocalDate.parse(tx.getTimestamp().split(" ")[0], DATE_ONLY_FORMATTER);
                } catch (DateTimeParseException e) {
                    System.err.println("Date parse error grouping transactions: " + tx.getTimestamp());
                    return null; // Ignore transactions with invalid dates
                }
            }));

        boolean frequentLargeTransactionsFound = false;
        for (Map.Entry<LocalDate, List<Transaction>> entry : transactionsByDay.entrySet()) {
            if (entry.getKey() == null) continue; // Skip invalid dates
            long largeTransactionCount = entry.getValue().stream()
                 .filter(tx -> {
                     // Check if Operation is Income or Expense and amount is large
                     boolean isRelevantOperation = "Income".equalsIgnoreCase(tx.getOperation()) || "Expense".equalsIgnoreCase(tx.getOperation());
                     return tx != null && isRelevantOperation && tx.getAmount() >= FREQUENT_LARGE_TRANSACTION_AMOUNT;
                 })
                .count();

            if (largeTransactionCount >= FREQUENT_LARGE_TRANSACTION_COUNT) {
                frequentLargeTransactionsFound = true;
                // We just need to know *if* it happened, not list every day, for the login warning.
                // Add the date to the warning for more specific feedback
                warnings.add(String.format("Frequent large transactions (>= ¥%.2f, >= %d times) occurred on %s.", FREQUENT_LARGE_TRANSACTION_AMOUNT, FREQUENT_LARGE_TRANSACTION_COUNT, entry.getKey().format(DATE_ONLY_FORMATTER)));
            }
        }

        System.out.println("Historical abnormal transaction check completed. Warnings: " + warnings.size());
        return warnings;
    }

    /**
     * Checks if adding a new transaction triggers real-time abnormal transaction warnings.
     * This check is performed BEFORE the transaction is added to the history.
     *
     * @param username          The username of the user.
     * @param existingTransactions The list of existing transactions for the user.
     * @param newTransaction    The transaction being added (temporary object).
     * @return A list of strings describing the detected abnormal patterns for this transaction, or an empty list if none found.
     */
    public List<String> checkRealtimeAbnormalTransactions(String username, List<Transaction> existingTransactions, Transaction newTransaction) {
        List<String> warnings = new ArrayList<>();
         if (newTransaction == null || username == null || username.trim().isEmpty()) {
             return warnings;
         }

         double averageDailyExpense = budgetService.calculateAverageDailyExpense(username, 3); // Calculate average daily expense

         // Pattern 2 Check (Single large expense)
         if ("Expense".equalsIgnoreCase(newTransaction.getOperation()) && newTransaction.getAmount() > averageDailyExpense * LARGE_EXPENSE_MULTIPLIER && averageDailyExpense > 0) {
             warnings.add(String.format("This expense (¥%.2f) exceeds %.1f times your average daily spending (¥%.2f).", newTransaction.getAmount(), LARGE_EXPENSE_MULTIPLIER, averageDailyExpense));
         }

         // Pattern 3 Check (Single large "transfer out" - interpreting "转出" as Expense operation here)
         if ("Expense".equalsIgnoreCase(newTransaction.getOperation()) && newTransaction.getAmount() > LARGE_TRANSFER_OUT_AMOUNT) {
             warnings.add(String.format("This expense transaction (¥%.2f) is a large amount.", newTransaction.getAmount()));
         }

         // Pattern 1 Check (Frequent large transactions on the same day)
         // Check if adding THIS transaction *causes* the frequent limit to be reached for THIS day
         try {
             String[] dateTimeParts = newTransaction.getTimestamp().split(" ");
             LocalDate newTransactionDay = LocalDate.parse(dateTimeParts[0], DATE_ONLY_FORMATTER);

              // Count existing large transactions for the same day as the new one
             long existingLargeTransactionsToday = existingTransactions.stream()
                 .filter(tx -> tx != null && tx.getTimestamp() != null)
                 .filter(tx -> {
                      try {
                          LocalDate txDay = LocalDate.parse(tx.getTimestamp().split(" ")[0], DATE_ONLY_FORMATTER);
                          return txDay.equals(newTransactionDay); // Filter for the same day
                      } catch (DateTimeParseException e) {
                          System.err.println("Date parse error filtering for daily check: " + tx.getTimestamp());
                          return false; // Ignore transactions with invalid dates
                      }
                  })
                 .filter(tx -> {
                      // Check if Operation is Income or Expense and amount is large
                      boolean isRelevantOperation = "Income".equalsIgnoreCase(tx.getOperation()) || "Expense".equalsIgnoreCase(tx.getOperation());
                      return tx != null && isRelevantOperation && tx.getAmount() >= FREQUENT_LARGE_TRANSACTION_AMOUNT;
                  })
                 .count();

             // Check if the new transaction itself is large
             boolean isNewTransactionLarge = ("Income".equalsIgnoreCase(newTransaction.getOperation()) || "Expense".equalsIgnoreCase(newTransaction.getOperation())) && newTransaction.getAmount() >= FREQUENT_LARGE_TRANSACTION_AMOUNT;

             // If adding the new transaction makes the count >= FREQUENT_LARGE_TRANSACTION_COUNT for today
             if (isNewTransactionLarge && (existingLargeTransactionsToday + 1) >= FREQUENT_LARGE_TRANSACTION_COUNT) {
                 warnings.add(String.format("Adding this transaction (¥%.2f) will result in >= %d large transactions (>= ¥%.2f) today (%s).", newTransaction.getAmount(), FREQUENT_LARGE_TRANSACTION_COUNT, FREQUENT_LARGE_TRANSACTION_AMOUNT, newTransactionDay.format(DATE_ONLY_FORMATTER)));
             }

         } catch (DateTimeParseException e) {
             System.err.println("Date parse error for new transaction timestamp during realtime check: " + newTransaction.getTimestamp());
              // Warning might not be possible due to invalid date, but proceed with other checks
         }

        return warnings;
    }

    /**
     * Gets the TransactionRepository associated with this service.
     * This is a controlled way to expose the repository if needed by trusted controllers.
     *
     * @return the TransactionRepository instance
     */
    public TransactionRepository getTransactionRepository() {
        return transactionRepository;
    }


    /**
     * Escapes a field for CSV formatting by enclosing it in quotes if it contains commas,
     * quotes, newline, carriage return, or is empty (to preserve structure with split(",", -1)).
     *
     * @param field the field to escape
     * @return the escaped field
     */
    private String escapeForSummary(String field) {
         if (field == null) return ""; // Treat null as empty string
        // Enclose in quotes if it contains comma, quotes, newline, carriage return, or is empty
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r") || field.isEmpty()) {
            return "\"" + field.replace("\"", "\"\"") + "\""; // Escape quotes by doubling them
        }
        return field;
    }
}