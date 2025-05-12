package Model;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents a financial transaction in the Smart Finance Application.
 * This class encapsulates detailed information about a transaction, including the associated user,
 * operation type, amount, timestamp, and other attributes such as merchant, category, and payment method.
 * Implements {@link Serializable} for data persistence.
 *
 * @author Group 19
 * @version 1.0
 */
public class Transaction implements Serializable {
    /** Serial version UID for serialization compatibility. */
    @Serial
    private static final long serialVersionUID = 2L;

    /** The username associated with the transaction. */
    private final String accountUsername;

    /** The operation type of the transaction (e.g., "Income", "Expense"). */
    private final String operation;

    /** The amount of the transaction. */
    private final double amount;

    /** The timestamp of the transaction. */
    private final String timestamp;

    /** The merchant involved in the transaction. */
    private final String merchant;

    /** The type of the transaction (e.g., "Transfer Out", "Withdrawal"). */
    private final String type;

    /** Additional remarks or notes about the transaction. */
    private final String remark;

    /** The category of the transaction (e.g., "Food", "Travel"). */
    private final String category;

    /** The payment method used for the transaction (e.g., "Credit Card", "Cash"). */
    private final String paymentMethod;

    /** The location where the transaction occurred. */
    private final String location;

    /** Tags associated with the transaction for categorization. */
    private final String tag;

    /** The path or reference to any attachment related to the transaction. */
    private final String attachment;

    /** The recurrence details of the transaction, if applicable (e.g., "Monthly"). */
    private final String recurrence;

    /**
     * Constructs a Transaction with the specified details.
     *
     * @param accountUsername The username associated with the transaction.
     * @param operation      The operation type of the transaction (e.g., "Income", "Expense").
     * @param amount         The amount of the transaction.
     * @param timestamp      The timestamp of the transaction.
     * @param merchant       The merchant involved in the transaction.
     * @param type           The type of the transaction (e.g., "Transfer Out", "Withdrawal").
     * @param remark         Additional remarks or notes about the transaction.
     * @param category       The category of the transaction (e.g., "Food", "Travel").
     * @param paymentMethod  The payment method used for the transaction (e.g., "Credit Card", "Cash").
     * @param location       The location where the transaction occurred.
     * @param tag            Tags associated with the transaction for categorization.
     * @param attachment     The path or reference to any attachment related to the transaction.
     * @param recurrence     The recurrence details of the transaction, if applicable (e.g., "Monthly").
     */
    public Transaction(String accountUsername, String operation, double amount, String timestamp, String merchant,
                       String type, String remark, String category, String paymentMethod, String location,
                       String tag, String attachment, String recurrence) {
        this.accountUsername = accountUsername;
        this.operation = operation;
        this.amount = amount;
        this.timestamp = timestamp;
        this.merchant = merchant;
        this.type = type;
        this.remark = remark;
        this.category = category;
        this.paymentMethod = paymentMethod;
        this.location = location;
        this.tag = tag;
        this.attachment = attachment;
        this.recurrence = recurrence;
    }

    /**
     * Gets the username associated with the transaction.
     *
     * @return The username.
     */
    public String getAccountUsername() {
        return accountUsername;
    }

    /**
     * Gets the operation type of the transaction.
     *
     * @return The operation type (e.g., "Income", "Expense").
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Gets the amount of the transaction.
     *
     * @return The transaction amount.
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Gets the timestamp of the transaction.
     *
     * @return The timestamp.
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the merchant involved in the transaction.
     *
     * @return The merchant name.
     */
    public String getMerchant() {
        return merchant;
    }

    /**
     * Gets the type of the transaction.
     *
     * @return The transaction type (e.g., "Transfer Out", "Withdrawal").
     */
    public String getType() {
        return type;
    }

    /**
     * Gets additional remarks or notes about the transaction.
     *
     * @return The remark.
     */
    public String getRemark() {
        return remark;
    }

    /**
     * Gets the category of the transaction.
     *
     * @return The category (e.g., "Food", "Travel").
     */
    public String getCategory() {
        return category;
    }

    /**
     * Gets the payment method used for the transaction.
     *
     * @return The payment method (e.g., "Credit Card", "Cash").
     */
    public String getPaymentMethod() {
        return paymentMethod;
    }

    /**
     * Gets the location where the transaction occurred.
     *
     * @return The location.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Gets the tags associated with the transaction.
     *
     * @return The tag.
     */
    public String getTag() {
        return tag;
    }

    /**
     * Gets the path or reference to any attachment related to the transaction.
     *
     * @return The attachment.
     */
    public String getAttachment() {
        return attachment;
    }

    /**
     * Gets the recurrence details of the transaction, if applicable.
     *
     * @return The recurrence details (e.g., "Monthly").
     */
    public String getRecurrence() {
        return recurrence;
    }

    /**
     * Returns a string representation of the transaction.
     *
     * @return A string containing the transaction details, including username, operation,
     *         amount, timestamp, merchant, type, remark, and category.
     */
    @Override
    public String toString() {
        return "Transaction{" +
                "accountUsername='" + accountUsername + '\'' +
                ", operation='" + operation + '\'' +
                ", amount=" + amount +
                ", timestamp='" + timestamp + '\'' +
                ", merchant='" + merchant + '\'' +
                ", type='" + type + '\'' +
                ", remark='" + remark + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}