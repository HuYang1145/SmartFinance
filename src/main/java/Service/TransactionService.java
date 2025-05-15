package Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Objects; // Added import

import Model.Transaction;
import Model.User;
import Repository.TransactionRepository;

public class TransactionService {
    private static final DateTimeFormatter TRANSACTION_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
     private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final TransactionRepository transactionRepository;
    private final BudgetService budgetService; // Marked as final, initialized in constructor

    // Define the thresholds for new abnormal checks
    private static final double FREQUENT_LARGE_TRANSACTION_AMOUNT = 5000.0; // 一天内转账或收支>=5000的阈值
    private static final int FREQUENT_LARGE_TRANSACTION_COUNT = 3; // 一天内转账或收支 >= 5000 的次数阈值
    private static final double LARGE_EXPENSE_MULTIPLIER = 3.0; // 单笔支出 > 平均支出倍数
    private static final double LARGE_TRANSFER_OUT_AMOUNT = 50000.0; // 单笔转出或消费金额大于阈值

    /**
     * Constructs a TransactionService instance with the specified dependencies.
     *
     * @param transactionRepository the repository for accessing transaction data
     * @param budgetService         the service for budget-related calculations
     */
    public TransactionService(TransactionRepository transactionRepository, BudgetService budgetService) {
        this.transactionRepository = Objects.requireNonNull(transactionRepository, "TransactionRepository cannot be null"); // Use Objects.requireNonNull for clarity and null check
        this.budgetService = Objects.requireNonNull(budgetService, "BudgetService cannot be null"); // Use Objects.requireNonNull
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
        List<Transaction> transactions = transactionRepository.findTransactionsByUser(user); // Blocking call
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
        List<Transaction> transactions = transactionRepository.findTransactionsByUser(user); // Blocking call
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
     * Checks for abnormal transactions for a given user based on specific patterns in their history.
     * This is typically used for a warning upon login.
     * Combines checks for:
     * 1. Frequent large transactions on any single day in the history (>= 3 transactions with operation "Income" or "Expense" and amount >= 5000).
     * 2. Any single expense transaction in the history exceeding 3 times the user's average daily expense (calculated over past 3 full months).
     * 3. Any single expense or transfer out transaction in the history exceeding 50000.
     *
     * @param username     The username to check transactions for.
     * @param transactions The list of all transactions for the user to analyze.
     * @return A list of strings describing the detected abnormal patterns, or an empty list if none found.
     */
    public List<String> checkAbnormalTransactions(String username, List<Transaction> transactions) {
        List<String> warnings = new ArrayList<>();
        if (username == null || username.trim().isEmpty() || transactions == null || transactions.isEmpty()) {
            return warnings;
        }

        System.out.println("Checking historical abnormal transactions for user " + username + " (" + transactions.size() + " transactions)...");

        // Calculate average daily expense (can be 0 if no past expenses or income)
        double averageDailyExpense = budgetService.calculateAverageDailyExpense(username, 3); // Blocking call

        // Use flags to ensure we add each *type* of warning at most once for historical checks.
        boolean largeExpenseFound = false; // Flag for Pattern 2 (>3x avg)
        boolean largeTransferOrExpenseFound = false; // Flag for Pattern 3 (>=50000)

        for (Transaction tx : transactions) {
            if (tx == null || tx.getOperation() == null) continue;

            // Pattern 2: Single large expense (> 3 * avg daily) - only if average daily is meaningful (>0)
            if ("Expense".equalsIgnoreCase(tx.getOperation()) && averageDailyExpense > 0 && tx.getAmount() > averageDailyExpense * LARGE_EXPENSE_MULTIPLIER) {
                 largeExpenseFound = true;
                 // No need to check further for this pattern in this loop if already found
            }

            // Pattern 3: Single large "transfer out" or "expense" (>= 50000)
            if (("Expense".equalsIgnoreCase(tx.getOperation()) || "Transfer Out".equalsIgnoreCase(tx.getOperation()))
                 && tx.getAmount() >= LARGE_TRANSFER_OUT_AMOUNT) {
                 largeTransferOrExpenseFound = true;
                 // No need to check further for this pattern in this loop if already found
            }

            // Optimization: If both P2 and P3 warnings are already flagged, we can stop iterating transactions for these patterns
             if (largeExpenseFound && largeTransferOrExpenseFound) {
                 break; // Exit the loop early as we found at least one instance of each flagged pattern
             }
        }

        // Add warnings based on flags after iterating all transactions for P2 and P3
         if (largeExpenseFound) {
             warnings.add(String.format("A single expense transaction in your history exceeded %.1f times your average daily spending (avg daily: ¥%.2f).", LARGE_EXPENSE_MULTIPLIER, averageDailyExpense));
         }
         if (largeTransferOrExpenseFound) {
             warnings.add(String.format("A single Expense or Transfer Out transaction of over ¥%.2f was recorded.", LARGE_TRANSFER_OUT_AMOUNT)); // Updated warning message
         }


        // Pattern 1 Check (frequent large transactions per day)
        // Group transactions by day
        Map<LocalDate, List<Transaction>> transactionsByDay = transactions.stream()
            .filter(tx -> tx != null && tx.getTimestamp() != null)
            .collect(Collectors.groupingBy(tx -> {
                try {
                    String[] dateTimeParts = tx.getTimestamp().split(" ");
                    return LocalDate.parse(dateTimeParts[0], DATE_ONLY_FORMATTER);
                } catch (DateTimeParseException e) {
                    System.err.println("Date parse error grouping transactions by day: " + tx.getTimestamp() + " - " + e.getMessage());
                    return null; // Group transactions with invalid dates under null key
                }
            }));

        // Process groups, skipping the null key group
        for (Map.Entry<LocalDate, List<Transaction>> entry : transactionsByDay.entrySet()) {
            if (entry.getKey() == null) continue;

            long largeTransactionCount = entry.getValue().stream()
                 .filter(tx -> {
                      boolean isRelevantOperation = "Income".equalsIgnoreCase(tx.getOperation()) || "Expense".equalsIgnoreCase(tx.getOperation());
                      return tx != null && isRelevantOperation && tx.getAmount() >= FREQUENT_LARGE_TRANSACTION_AMOUNT;
                 })
                .count();

            if (largeTransactionCount >= FREQUENT_LARGE_TRANSACTION_COUNT) {
                // Add a specific warning for this day
                warnings.add(String.format("Frequent large transactions (>= ¥%.2f, >= %d times) occurred on %s.", FREQUENT_LARGE_TRANSACTION_AMOUNT, FREQUENT_LARGE_TRANSACTION_COUNT, entry.getKey().format(DATE_ONLY_FORMATTER)));
            }
        }

        System.out.println("Historical abnormal transaction check completed. Warnings found: " + warnings.size());
        return warnings;
    }

    /**
     * Checks if adding a *new* transaction triggers real-time abnormal transaction warnings based on the combined list.
     * This check is performed BEFORE the transaction is added to the history file.
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
         // Basic validation for new transaction's essential fields for check
         if (newTransaction.getOperation() == null || newTransaction.getTimestamp() == null) {
              System.err.println("Real-time check: New transaction is missing operation or timestamp. Cannot perform full check.");
              // Add a generic warning for invalid input and proceed with other checks if possible.
               warnings.add("Cannot perform full risk check due to invalid new transaction data (missing operation or time).");
              // If amount or operation are missing, cannot check P2/P3 either.
              if (newTransaction.getAmount() < 0 || newTransaction.getOperation() == null) { // Check amount >= 0
                   return warnings; // Return with only the invalid data warning
              }
         }


         // Calculate average daily expense (use the BudgetService dependency)
         double averageDailyExpense = budgetService.calculateAverageDailyExpense(username, 3);


         // Pattern 2 Check (Single large expense) - applies to the NEW transaction
         // Only check if the new transaction is an Expense AND averageDailyExpense is meaningful (>0)
         if ("Expense".equalsIgnoreCase(newTransaction.getOperation()) && averageDailyExpense > 0 && newTransaction.getAmount() > averageDailyExpense * LARGE_EXPENSE_MULTIPLIER) {
             warnings.add(String.format("This expense (¥%.2f) exceeds %.1f times your average daily spending (¥%.2f).", newTransaction.getAmount(), LARGE_EXPENSE_MULTIPLIER, averageDailyExpense));
         }

         // Pattern 3 Check (Single large "transfer out" or "expense" (>= 50000)) - applies to the NEW transaction
         if (("Expense".equalsIgnoreCase(newTransaction.getOperation()) || "Transfer Out".equalsIgnoreCase(newTransaction.getOperation()))
             && newTransaction.getAmount() >= LARGE_TRANSFER_OUT_AMOUNT) {
             warnings.add(String.format("This Expense or Transfer Out transaction (¥%.2f) is over ¥%.2f.", newTransaction.getAmount(), LARGE_TRANSFER_OUT_AMOUNT));
         }


         // Pattern 1 Check (Frequent large transactions on the same day)
         // Check if adding THIS transaction *causes* the frequent limit to be reached for THIS day
         // This check requires a valid timestamp AND the transaction itself must be large and relevant (Income/Expense)
         if (newTransaction.getTimestamp() != null && newTransaction.getOperation() != null &&
             ( "Income".equalsIgnoreCase(newTransaction.getOperation()) || "Expense".equalsIgnoreCase(newTransaction.getOperation())) &&
             newTransaction.getAmount() >= FREQUENT_LARGE_TRANSACTION_AMOUNT) { // Check if the new transaction itself is 'large' for Pattern 1
              try {
                  String[] dateTimeParts = newTransaction.getTimestamp().split(" ");
                  LocalDate newTransactionDay = LocalDate.parse(dateTimeParts[0], DATE_ONLY_FORMATTER);

                  // Count existing large, relevant transactions for the same day as the new one
                  long existingLargeTransactionsToday = (existingTransactions == null) ? 0 : existingTransactions.stream() // Handle null existingTransactions
                      .filter(tx -> tx != null && tx.getTimestamp() != null && tx.getOperation() != null) // Filter out invalid existing transactions
                      .filter(tx -> {
                           try {
                               String[] existingDateTimeParts = tx.getTimestamp().split(" ");
                               LocalDate txDay = LocalDate.parse(existingDateTimeParts[0], DATE_ONLY_FORMATTER);
                               return txDay.equals(newTransactionDay); // Filter for the same day
                           } catch (DateTimeParseException e) {
                               System.err.println("Date parse error filtering for daily check on existing transaction: " + tx.getTimestamp() + " - " + e.getMessage());
                               return false; // Ignore existing transactions with invalid dates
                           } catch (Exception e) {
                                System.err.println("Unexpected error parsing existing timestamp for daily check: " + tx.getTimestamp() + " - " + e.getMessage());
                                e.printStackTrace();
                                return false; // Ignore existing transactions with unexpected errors
                           }
                       })
                      .filter(tx -> {
                           // Check if Operation is Income or Expense and amount is large
                           boolean isRelevantOperation = "Income".equalsIgnoreCase(tx.getOperation()) || "Expense".equalsIgnoreCase(tx.getOperation());
                           return isRelevantOperation && tx.getAmount() >= FREQUENT_LARGE_TRANSACTION_AMOUNT;
                       })
                      .count();

                  // If adding the new large transaction makes the total count for today >= FREQUENT_LARGE_TRANSACTION_COUNT
                  if ((existingLargeTransactionsToday + 1) >= FREQUENT_LARGE_TRANSACTION_COUNT) {
                      warnings.add(String.format("Adding this transaction (¥%.2f) will result in %d large transactions (>= ¥%.2f) today (%s).",
                          newTransaction.getAmount(), existingLargeTransactionsToday + 1, FREQUENT_LARGE_TRANSACTION_AMOUNT, newTransactionDay.format(DATE_ONLY_FORMATTER)));
                  }

              } catch (DateTimeParseException e) {
                  // This case is handled by the outer check if timestamp is null.
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
     * @param field The field to escape.
     * @return The escaped field, or an empty string if the field is null.
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