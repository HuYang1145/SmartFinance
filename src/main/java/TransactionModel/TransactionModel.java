package TransactionModel;

import java.io.Serializable;

/**
 * Represents a transaction record with fields aligned to a 13-column CSV structure.
 */
public class TransactionModel implements Serializable {
    private static final long serialVersionUID = 2L;

    private final String accountUsername;
    private final String operation;
    private final double amount;
    private final String timestamp;
    private final String merchant;
    private final String type;
    private final String remark;
    private final String category;
    private final String paymentMethod;
    private final String location;
    private final String tag;
    private final String attachment;
    private final String recurrence;

    /**
     * Constructor for TransactionModel.
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

    // Getters
    public String getAccountUsername() { return accountUsername; }
    public String getOperation() { return operation; }
    public double getAmount() { return amount; }
    public String getTimestamp() { return timestamp; }
    public String getMerchant() { return merchant; }
    public String getType() { return type; }
    public String getRemark() { return remark; }
    public String getCategory() { return category; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getLocation() { return location; }
    public String getTag() { return tag; }
    public String getAttachment() { return attachment; }
    public String getRecurrence() { return recurrence; }

    @Override
    public String toString() {
        return "TransactionModel{" +
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