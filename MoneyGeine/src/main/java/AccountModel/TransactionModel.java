package AccountModel;

import java.io.Serializable;

// Represents a transaction record, aligned with the new 6-column CSV structure
public class TransactionModel implements Serializable {
    private static final long serialVersionUID = 2L; // Bump version ID due to structure change

    // Fields matching the new CSV structure
    private String accountUsername; // user
    private String operation;       // operation (Income/Expense)
    private double amount;          // amount (always positive)
    private String timestamp;       // time (String format: yyyy/MM/dd HH:mm)
    private String merchant;        // merchant (or "I" for Income)
    private String type;            // type (or "I" for Income, "u" for unclassified)

    // Optional: Retain transactionId if needed for internal logic,
    // but it's not directly in the new CSV format unless added back.
    // private String transactionId;


    // Constructor for loading data (preferred way now)
    public TransactionModel(String accountUsername, String operation, double amount, String timestamp, String merchant, String type) {
        // Basic validation can be added here if desired
        this.accountUsername = accountUsername;
        this.operation = operation;
        this.amount = amount; // Assume positive amount is passed
        this.timestamp = timestamp;
        this.merchant = merchant;
        this.type = type;
        // If you still need a unique ID internally:
        // this.transactionId = generateTransactionId();
    }

    // --- Getters ---
    public String getAccountUsername() { return accountUsername; }
    public String getOperation() { return operation; }
    public double getAmount() { return amount; }
    public String getTimestamp() { return timestamp; }
    public String getMerchant() { return merchant; }
    public String getType() { return type; }
    // public String getTransactionId() { return transactionId; } // If you keep it

    // --- equals, hashCode, toString (Optional, update if needed) ---
    // If transactionId is removed, equals/hashCode might need redefining
    // based on other fields if unique identification is required in collections.
    // For simplicity, we might omit them or base them on all fields.
    @Override
    public String toString() {
        return "TransactionModel{" +
               "accountUsername='" + accountUsername + '\'' +
               ", operation='" + operation + '\'' +
               ", amount=" + amount +
               ", timestamp='" + timestamp + '\'' +
               ", merchant='" + merchant + '\'' +
               ", type='" + type + '\'' +
               // ", transactionId='" + transactionId + '\'' + // If kept
               '}';
    }

    // --- CSV conversion methods (toCSV/fromCSV) are NO LONGER RECOMMENDED here ---
    // Let TransactionService handle the direct CSV read/write logic
    // Remove the old toCSV() and fromCSV(String csvLine) methods.

    // --- Optional: Helper for ID generation if kept ---
    // private String generateTransactionId() {
    //     return System.currentTimeMillis() + "_" + ((int)(Math.random() * 1000));
    // }
}