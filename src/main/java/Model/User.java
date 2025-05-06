package Model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;
    private String phone;
    private String email;
    private String gender;
    private String address;
    private String creationTime;
    private AccountStatus accountStatus;
    private String accountType;
    private double balance;
    private List<Transaction> transactions;

    public enum AccountStatus {
        ACTIVE, FROZEN
    }

    public User() {
        this.transactions = new ArrayList<>();
    }

    public User(String username, String password, String phone, String email, String gender, String address,
                String creationTime, AccountStatus accountStatus, String accountType, double balance) {
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.gender = gender;
        this.address = address;
        this.creationTime = creationTime;
        this.accountStatus = accountStatus;
        this.accountType = accountType;
        this.balance = balance;
        this.transactions = new ArrayList<>();
    }

    public boolean isAdmin() {
        return "Admin".equalsIgnoreCase(accountType);
    }

    public String toCSV() {
        return "%s,%s,%s,%s,%s,%s,%s,%s,%s,%.2f".formatted(
                username, password, phone, email, gender, address, creationTime,
                accountStatus, accountType, balance);
    }

    public static User fromCSV(String csvLine) {
        String[] parts = csvLine.split(",", -1);
        if (parts.length >= 10) {
            try {
                String username = parts[0].trim();
                String password = parts[1].trim();
                String phone = parts[2].trim();
                String email = parts[3].trim();
                String gender = parts[4].trim();
                String address = parts[5].trim();
                String creationTime = parts[6].trim();
                AccountStatus status = AccountStatus.valueOf(parts[7].trim());
                String accountType = parts[8].trim();
                double balance = Double.parseDouble(parts[9].trim());

                return new User(username, password, phone, email, gender, address,
                        creationTime, status, accountType, balance);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid CSV line: " + csvLine, e);
            }
        }
        throw new IllegalArgumentException("Insufficient fields in CSV line: " + csvLine);
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCreationTime() { return creationTime; }
    public void setCreationTime(String creationTime) { this.creationTime = creationTime; }
    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }

    public void addTransaction(Transaction transaction) {
        if (transaction != null && username.equals(transaction.getAccountUsername())) {
            transactions.add(transaction);
        } else {
            throw new IllegalArgumentException("Invalid or mismatched transaction for account: " + username);
        }
    }
}