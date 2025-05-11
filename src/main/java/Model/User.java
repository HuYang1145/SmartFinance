
package Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user in the Smart Finance Application.
 * This class encapsulates user account details, including personal information, account status,
 * balance, and a list of associated transactions. It provides methods for CSV serialization,
 * deserialization, and transaction management.
 *
 * @author Group 19
 * @version 1.0
 */
public class User {
    /**
     * Enum defining possible account statuses.
     */
    public enum AccountStatus {
        /** The account is active and fully operational. */
        ACTIVE,
        /** The account is frozen and restricted from certain operations. */
        FROZEN
    }

    /** The username of the user. */
    private String username;

    /** The password of the user. */
    private String password;

    /** The phone number of the user. */
    private String phone;

    /** The email address of the user. */
    private String email;

    /** The gender of the user. */
    private String gender;

    /** The address of the user. */
    private String address;

    /** The creation time of the user account. */
    private String creationTime;

    /** The status of the user account (e.g., ACTIVE, FROZEN). */
    private AccountStatus accountStatus;

    /** The type of the user account (e.g., "Admin", "User"). */
    private String accountType;

    /** The current balance of the user account. */
    private double balance;

    /** The list of transactions associated with the user. */
    private List<Transaction> transactions;

    /**
     * Constructs an empty User with an initialized transaction list.
     */
    public User() {
        this.transactions = new ArrayList<>();
    }

    /**
     * Constructs a User with the specified details and an initialized transaction list.
     *
     * @param username      The username of the user.
     * @param password      The password of the user.
     * @param phone         The phone number of the user.
     * @param email         The email address of the user.
     * @param gender        The gender of the user.
     * @param address       The address of the user.
     * @param creationTime  The creation time of the user account.
     * @param accountStatus The status of the user account (e.g., ACTIVE, FROZEN).
     * @param accountType   The type of the user account (e.g., "Admin", "User").
     * @param balance       The current balance of the user account.
     */
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

    /**
     * Checks if the user has an admin account type.
     *
     * @return {@code true} if the account type is "Admin" (case-insensitive), {@code false} otherwise.
     */
    public boolean isAdmin() {
        return "Admin".equalsIgnoreCase(accountType);
    }

    /**
     * Converts the user details to a CSV-formatted string.
     *
     * @return A CSV string containing the user's details.
     */
    public String toCSV() {
        return "%s,%s,%s,%s,%s,%s,%s,%s,%s,%.2f".formatted(
                username, password, phone, email, gender, address, creationTime,
                accountStatus, accountType, balance);
    }

    /**
     * Creates a User object from a CSV-formatted string.
     *
     * @param csvLine The CSV string containing user details.
     * @return A new {@link User} object populated with the CSV data.
     * @throws IllegalArgumentException if the CSV line is invalid or has insufficient fields.
     */
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

    /**
     * Gets the username of the user.
     *
     * @return The username, or null if not set.
     */
    public String getUsername() { return username; }

    /**
     * Sets the username of the user.
     *
     * @param username The username to set.
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * Gets the password of the user.
     *
     * @return The password, or null if not set.
     */
    public String getPassword() { return password; }

    /**
     * Sets the password of the user.
     *
     * @param password The password to set.
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * Gets the phone number of the user.
     *
     * @return The phone number, or null if not set.
     */
    public String getPhone() { return phone; }

    /**
     * Sets the phone number of the user.
     *
     * @param phone The phone number to set.
     */
    public void setPhone(String phone) { this.phone = phone; }

    /**
     * Gets the email address of the user.
     *
     * @return The email address, or null if not set.
     */
    public String getEmail() { return email; }

    /**
     * Sets the email address of the user.
     *
     * @param email The email address to set.
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Gets the gender of the user.
     *
     * @return The gender, or null if not set.
     */
    public String getGender() { return gender; }

    /**
     * Sets the gender of the user.
     *
     * @param gender The gender to set.
     */
    public void setGender(String gender) { this.gender = gender; }

    /**
     * Gets the address of the user.
     *
     * @return The address, or null if not set.
     */
    public String getAddress() { return address; }

    /**
     * Sets the address of the user.
     *
     * @param address The address to set.
     */
    public void setAddress(String address) { this.address = address; }

    /**
     * Gets the creation time of the user account.
     *
     * @return The creation time, or null if not set.
     */
    public String getCreationTime() { return creationTime; }

    /**
     * Sets the creation time of the user account.
     *
     * @param creationTime The creation time to set.
     */
    public void setCreationTime(String creationTime) { this.creationTime = creationTime; }

    /**
     * Gets the status of the user account.
     *
     * @return The {@link AccountStatus} of the account, or null if not set.
     */
    public AccountStatus getAccountStatus() { return accountStatus; }

    /**
     * Sets the status of the user account.
     *
     * @param accountStatus The {@link AccountStatus} to set.
     */
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }

    /**
     * Gets the type of the user account.
     *
     * @return The account type (e.g., "Admin", "User"), or null if not set.
     */
    public String getAccountType() { return accountType; }

    /**
     * Sets the type of the user account.
     *
     * @param accountType The account type to set (e.g., "Admin", "User").
     */
    public void setAccountType(String accountType) { this.accountType = accountType; }

    /**
     * Gets the current balance of the user account.
     *
     * @return The account balance.
     */
    public double getBalance() { return balance; }

    /**
     * Sets the current balance of the user account.
     *
     * @param balance The account balance to set.
     */
    public void setBalance(double balance) { this.balance = balance; }

    /**
     * Gets the list of transactions associated with the user.
     *
     * @return The list of {@link Transaction} objects.
     */
    public List<Transaction> getTransactions() { return transactions; }

    /**
     * Sets the list of transactions associated with the user.
     *
     * @param transactions The list of {@link Transaction} objects to set.
     */
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }

    /**
     * Adds a transaction to the user's transaction list.
     *
     * @param transaction The {@link Transaction} to add.
     * @throws IllegalArgumentException if the transaction is null or does not match the user's username.
     */
    public void addTransaction(Transaction transaction) {
        if (transaction != null && username.equals(transaction.getAccountUsername())) {
            transactions.add(transaction);
        } else {
            throw new IllegalArgumentException("Invalid or mismatched transaction for account: " + username);
        }
    }
}
