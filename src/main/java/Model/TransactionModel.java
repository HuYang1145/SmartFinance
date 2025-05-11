package Model;

/**
 * Represents a financial transaction model in the Smart Finance Application.
 * This class serves as a data transfer object (DTO) to encapsulate transaction details,
 * including the associated user, operation type, amount, timestamp, and other attributes
 * such as merchant, category, and payment method.
 *
 * @author Group 19
 * @version 1.0
 */
public class TransactionModel {
    /** The username associated with the transaction. */
    private String accountUsername;

    /** The operation type of the transaction (e.g., "Income", "Expense"). */
    private String operation;

    /** The amount of the transaction. */
    private double amount;

    /** The timestamp of the transaction. */
    private String timestamp;

    /** The merchant involved in the transaction. */
    private String merchant;

    /** The type of the transaction (e.g., "Transfer Out", "Withdrawal"). */
    private String type;

    /** Additional remarks or notes about the transaction. */
    private String remark;

    /** The category of the transaction (e.g., "Food", "Travel"). */
    private String category;

    /** The payment method used for the transaction (e.g., "Credit Card", "Cash"). */
    private String paymentMethod;

    /** The location where the transaction occurred. */
    private String location;

    /** Tags associated with the transaction for categorization. */
    private String tag;

    /** The path or reference to any attachment related to the transaction. */
    private String attachment;

    /** The recurrence details of the transaction, if applicable (e.g., "Monthly"). */
    private String recurrence;

    /**
     * Constructs a TransactionModel with the specified details.
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
    public TransactionModel(String accountUsername, String operation, double amount, String timestamp, String merchant,
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
     * @return The username, or null if not set.
     */
    public String getAccountUsername() {
        return accountUsername;
    }

    /**
     * Gets the operation type of the transaction.
     *
     * @return The operation type (e.g., "Income", "Expense"), or null if not set.
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
     * @return The timestamp, or null if not set.
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the merchant involved in the transaction.
     *
     * @return The merchant name, or null if not set.
     */
    public String getMerchant() {
        return merchant;
    }

    /**
     * Gets the type of the transaction.
     *
     * @return The transaction type (e.g., "Transfer Out", "Withdrawal"), or null if not set.
     */
    public String getType() {
        return type;
    }

    /**
     * Gets additional remarks or notes about the transaction.
     *
     * @return The remark, or null if not set.
     */
    public String getRemark() {
        return remark;
    }

    /**
     * Gets the category of the transaction.
     *
     * @return The category (e.g., "Food", "Travel"), or null if not set.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Gets the payment method used for the transaction.
     *
     * @return The payment method (e.g., "Credit Card", "Cash"), or null if not set.
     */
    public String getPaymentMethod() {
        return paymentMethod;
    }

    /**
     * Gets the location where the transaction occurred.
     *
     * @return The location, or null if not set.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Gets the tags associated with the transaction.
     *
     * @return The tag, or null if not set.
     */
    public String getTag() {
        return tag;
    }

    /**
     * Gets the path or reference to any attachment related to the transaction.
     *
     * @return The attachment, or null if not set.
     */
    public String getAttachment() {
        return attachment;
    }

    /**
     * Gets the recurrence details of the transaction, if applicable.
     *
     * @return The recurrence details (e.g., "Monthly"), or null if not set.
     */
    public String getRecurrence() {
        return recurrence;
    }

    /**
     * Sets the username associated with the transaction.
     *
     * @param accountUsername The username to set.
     */
    public void setAccountUsername(String accountUsername) {
        this.accountUsername = accountUsername;
    }

    /**
     * Sets the operation type of the transaction.
     *
     * @param operation The operation type to set (e.g., "Income", "Expense").
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * Sets the amount of the transaction.
     *
     * @param amount The transaction amount to set.
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * Sets the timestamp of the transaction.
     *
     * @param timestamp The timestamp to set.
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Sets the merchant involved in the transaction.
     *
     * @param merchant The merchant name to set.
     */
    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    /**
     * Sets the type of the transaction.
     *
     * @param type The transaction type to set (e.g., "Transfer Out", "Withdrawal").
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Sets additional remarks or notes about the transaction.
     *
     * @param remark The remark to set.
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * Sets the category of the transaction.
     *
     * @param category The category to set (e.g., "Food", "Travel").
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Sets the payment method used for the transaction.
     *
     * @param paymentMethod The payment method to set (e.g., "Credit Card", "Cash").
     */
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    /**
     * Sets the location where the transaction occurred.
     *
     * @param location The location to set.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Sets the tags associated with the transaction.
     *
     * @param tag The tag to set.
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Sets the path or reference to any attachment related to the transaction.
     *
     * @param attachment The attachment to set.
     */
    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    /**
     * Sets the recurrence details of the transaction.
     *
     * @param recurrence The recurrence details to set (e.g., "Monthly").
     */
    public void setRecurrence(String recurrence) {
        this.recurrence = recurrence;
    }
}