package AccountModel; // Note: Package name suggests Controller, but class content does not.

import java.util.List;

public class TransactionCheckerModel {

    /**
     * Checks if there are "abnormal" transactions for the given account.
     * Current definition: An abnormal transaction is of type 'Transfer Out' or 'Transfer In'
     * and has an amount greater than 500. // <-- Updated comment
     *
     * @param account The AccountModel object to check.
     * @return {@code true} if an abnormal transaction is found; otherwise returns {@code false}.
     */
    public static boolean hasAbnormalTransactions(AccountModel account) {
        // 1. Safety check: Confirm the incoming account object is not null
        if (account == null) {
            System.err.println("TransactionChecker: Received null account object.");
            return false; // Cannot check a null account
        }

        // 2. Get the transaction list from the account object
        List<TransactionModel> transactions = account.getTransactions();

        // 3. Check if the transaction list is null or empty
        if (transactions == null || transactions.isEmpty()) {
            // System.out.println("TransactionChecker: Account " + account.getUsername() + " has no transaction records."); // Can uncomment for debugging
            return false; // No transaction records, so no abnormal transactions
        }

        // --- Debug Info: Starting check ---
        System.out.println("TransactionChecker: Checking " + transactions.size() + " transaction records for account " + account.getUsername() + "...");

        // 4. Iterate through each transaction in the list
        for (TransactionModel transaction : transactions) {
            // 5. Safety check: Confirm the transaction object within the list is not null
            if (transaction == null) {
                System.err.println("TransactionChecker: Found null transaction in the transaction list for account " + account.getUsername() + ".");
                continue; // Skip this null transaction, continue checking the next one
            }

            // --- Debug Info: Print type and amount of current transaction being checked ---
            // System.out.println("   Checking transaction: Type=" + transaction.getType() + ", Amount=" + transaction.getAmount());

            // 6. Apply the "abnormal" criteria: (Modified)
            //    - Transaction type is 'Transfer Out' or 'Transfer In' (case-insensitive)
            //    - AND Transaction amount is greater than 500
            String transactionType = transaction.getType(); // Get type
            if (transactionType != null && // Added check for null transactionType
                ("Transfer Out".equalsIgnoreCase(transactionType) || "Transfer In".equalsIgnoreCase(transactionType)) &&
                 transaction.getAmount() > 500)
            {
                // Found one transaction that meets the criteria for abnormal
                // --- Debug Info: Abnormal transaction found ---
                System.out.println("TransactionChecker: Abnormal transaction found for account " + account.getUsername() +
                                   " (Type: " + transaction.getType() + ", Amount: " + transaction.getAmount() + ")");
                return true; // Found at least one, can confirm abnormal and stop checking
            }
        }

        // 7. If the loop finishes normally (checked all transactions), it means no abnormal transactions were found
        // --- Debug Info: No abnormal transactions found ---
        System.out.println("TransactionChecker: No abnormal transactions found for account " + account.getUsername() + ".");
        return false;
    }
}