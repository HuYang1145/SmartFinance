package Model;

public class TransactionModel {
    private String accountUsername;
    private String operation;
    private double amount;
    private String timestamp;
    private String merchant;
    private String type;
    private String remark;
    private String category;
    private String paymentMethod;
    private String location;
    private String tag;
    private String attachment;
    private String recurrence;

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
    public String getAccountUsername() {
        return accountUsername;
    }

    public String getOperation() {
        return operation;
    }

    public double getAmount() {
        return amount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getMerchant() {
        return merchant;
    }

    public String getType() {
        return type;
    }

    public String getRemark() {
        return remark;
    }

    public String getCategory() {
        return category;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getLocation() {
        return location;
    }

    public String getTag() {
        return tag;
    }

    public String getAttachment() {
        return attachment;
    }

    public String getRecurrence() {
        return recurrence;
    }

    // Setters (if needed)
    public void setAccountUsername(String accountUsername) {
        this.accountUsername = accountUsername;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public void setRecurrence(String recurrence) {
        this.recurrence = recurrence;
    }
}