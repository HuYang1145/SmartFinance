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

public class AccountRepository {
    private final String accountsFilePath;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    public static final String EXPECTED_ACCOUNT_HEADER = "username,password,phone,email,gender,address,creationTime,accountStatus,accountType,balance";

    // 构造函数，初始化文件路径
    public AccountRepository(String accountsFilePath) {
        if (accountsFilePath == null || accountsFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Accounts file path cannot be null or empty");
        }
        this.accountsFilePath = accountsFilePath;

        // 调试：检查文件是否存在
        File file = new File(accountsFilePath);
        System.out.println("Accounts file path: " + accountsFilePath);
        System.out.println("File exists: " + file.exists());
        System.out.println("File can read: " + file.canRead());
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
    }

    public User findByUsername(String username) {
        List<User> users = readFromCSV();
        return users.stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    public void save(User user) {
        List<User> users = new ArrayList<>();
        users.add(user);
        saveToCSV(users, true);
    }

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