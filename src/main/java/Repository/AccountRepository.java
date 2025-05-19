package Repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import Model.User;

/**
 * Repository class for managing user accounts in the Smart Finance Application.
 * This class provides methods to read, save, and query user data stored in a CSV file,
 * ensuring thread-safe operations using a read-write lock.
 *
 * @author Group 19
 * @version 1.0
 */
public class AccountRepository {
    /** The file path for the CSV file storing user account data. */
    private final String accountsFilePath;

    /** A read-write lock for thread-safe access to the CSV file. */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /** The expected header for the accounts CSV file. */
    public static final String EXPECTED_ACCOUNT_HEADER = "username,password,phone,email,gender,address,creationTime,accountStatus,accountType,balance";

    /**
     * Constructs an AccountRepository with the specified file path.
     *
     * @param accountsFilePath The path to the CSV file storing user account data.
     * @throws IllegalArgumentException if the file path is null or empty.
     */
    public AccountRepository(String accountsFilePath) {
        if (accountsFilePath == null || accountsFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Accounts file path cannot be null or empty");
        }
        this.accountsFilePath = accountsFilePath;

        // Debug: Check file existence
        File file = new File(accountsFilePath);
        System.out.println("Accounts file path: " + accountsFilePath);
        System.out.println("File exists: " + file.exists());
        System.out.println("File can read: " + file.canRead());
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
    }

    /**
     * Finds a user by their username.
     *
     * @param username The username to search for.
     * @return The {@link User} object with the specified username, or null if not found.
     */
    public User findByUsername(String username) {
        List<User> users = readFromCSV();
        return users.stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    /**
     * Saves a single user to the CSV file in append mode.
     *
     * @param user The {@link User} object to save.
     */
    public void save(User user) {
        List<User> users = new ArrayList<>();
        users.add(user);
        saveToCSV(users, true);
    }

    /**
     * Reads all user accounts from the CSV file.
     *
     * @return A list of {@link User} objects, or an empty list if an error occurs.
     */
    public List<User> readFromCSV() {
        List<User> users = new ArrayList<>();
        lock.readLock().lock();
        try (BufferedReader br = new BufferedReader(new FileReader(accountsFilePath))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length >= 10) {
                    User user = new User(
                            parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(),
                            parts[4].trim(), parts[5].trim(), parts[6].trim(),
                            User.AccountStatus.valueOf(parts[7].trim()), parts[8].trim(),
                            Double.parseDouble(parts[9].trim())
                    );
                    users.add(user);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading " + accountsFilePath + ": " + e.getMessage());
            return Collections.emptyList();
        } finally {
            lock.readLock().unlock();
        }
        return users;
    }

    /**
     * Updates the balance of a specific user in the accounts CSV.
     * If the user is found, updates their balance to newBalance.
     */
    public void updateBalance(String username, double newBalance) {
        lock.writeLock().lock();
        try {
            List<User> users = readFromCSV();
            boolean found = false;
            for (User user : users) {
                if (user.getUsername().equals(username)) {
                    user.setBalance(newBalance);
                    found = true;
                    break;
                }
            }
            if (found) {
                // 覆盖写回所有用户
                saveToCSV(users, false); // false: overwrite
            } else {
                System.out.println("User not found when updating balance: " + username);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }


    /**
     * Saves a list of users to the CSV file, with an option to append or overwrite.
     *
     * @param users  The list of {@link User} objects to save.
     * @param append {@code true} to append to the file, {@code false} to overwrite it.
     * @return {@code true} if the save operation was successful, {@code false} otherwise.
     */
    public boolean saveToCSV(List<User> users, boolean append) {
        lock.writeLock().lock();
        try (FileWriter fw = new FileWriter(accountsFilePath, append);
             BufferedWriter bw = new BufferedWriter(fw)) {
            if (!append) {
                bw.write(EXPECTED_ACCOUNT_HEADER);
                bw.newLine();
            }
            for (User user : users) {
                bw.write("%s,%s,%s,%s,%s,%s,%s,%s,%s,%.2f".formatted(
                        user.getUsername(), user.getPassword(), user.getPhone(), user.getEmail(),
                        user.getGender(), user.getAddress(), user.getCreationTime(),
                        user.getAccountStatus(), user.getAccountType(), user.getBalance()));
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error writing to " + accountsFilePath + ": " + e.getMessage());
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }
}