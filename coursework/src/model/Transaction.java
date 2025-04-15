package model;

import java.time.LocalDateTime;

public class Transaction {
    private String username;
    private String type;
    private double amount;
    private LocalDateTime timestamp;

    public Transaction(String username, String type, double amount, LocalDateTime timestamp) {
        this.username = username;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    // Getter 方法
    public String getUsername() { return username; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }
}