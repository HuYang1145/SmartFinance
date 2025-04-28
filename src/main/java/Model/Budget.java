package Model;


public class Budget {
    private String username;
    private double amount;
    private String mode; // NORMAL, ECONOMICAL_UNSTABLE, etc.
    private String creationTime;

    public Budget(String username, double amount, String mode, String creationTime) {
        this.username = username;
        this.amount = amount;
        this.mode = mode;
        this.creationTime = creationTime;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getCreationTime() { return creationTime; }
    public void setCreationTime(String creationTime) { this.creationTime = creationTime; }
}
