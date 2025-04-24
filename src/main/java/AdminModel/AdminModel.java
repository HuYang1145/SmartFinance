package AdminModel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AccountModel.AccountModel;
import AccountModel.TransactionCSVImporterModel;

public class AdminModel {
    private static final Logger logger = LoggerFactory.getLogger(AdminModel.class);
    public static final String EXPECTED_ACCOUNT_HEADER = "Username,Password,Phone,Email,Gender,Address,CreationTime,AccountStatus,AccountType,Balance";
    private static final int EXPECTED_ACCOUNT_FIELD_COUNT = 10;
    private static final String CSV_FILE_PATH = "accounts.csv";

    public AdminModel() {
        // No external dependencies needed, operates directly on the CSV file
    }

    // Reads account data from accounts.csv
    public List<AccountModel> readFromCSV() throws IOException {
        List<AccountModel> accounts = new ArrayList<>();
        File file = new File(CSV_FILE_PATH);
        if (!file.exists()) {
            logger.warn("CSV file not found: {}", file.getAbsolutePath());
            throw new IOException("CSV file not found: " + file.getAbsolutePath());
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            int invalidRows = 0;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] fields = line.split(",", -1);
                if (firstLine) {
                    if (fields.length != EXPECTED_ACCOUNT_FIELD_COUNT || !line.trim().equalsIgnoreCase(EXPECTED_ACCOUNT_HEADER)) {
                        throw new IOException("Invalid header format. Expected: '" + EXPECTED_ACCOUNT_HEADER + "', Actual: '" + line + "'");
                    }
                    firstLine = false;
                    continue;
                }

                try {
                    if (fields.length != EXPECTED_ACCOUNT_FIELD_COUNT) {
                        throw new IllegalArgumentException("Invalid field count: expected " + EXPECTED_ACCOUNT_FIELD_COUNT + ", got " + fields.length);
                    }

                    String username = fields[0].trim();
                    String password = fields[1].trim();
                    String phone = fields[2].trim();
                    String email = fields[3].trim();
                    String gender = fields[4].trim();
                    String address = fields[5].trim();
                    String creationTime = fields[6].trim();
                    String accountStatusStr = fields[7].trim();
                    String accountType = fields[8].trim();
                    String balanceStr = fields[9].trim();

                    // Validate data
                    if (username.isEmpty()) {
                        throw new IllegalArgumentException("Username cannot be empty");
                    }
                    Double balance;
                    try {
                        balance = Double.parseDouble(balanceStr);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid balance format: " + balanceStr);
                    }
                    AccountModel.AccountStatus accountStatus;
                    try {
                        accountStatus = AccountModel.AccountStatus.valueOf(accountStatusStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid account status: " + accountStatusStr);
                    }
                    // Validate creation time format
                    if (!creationTime.isEmpty()) {
                        try {
                            new SimpleDateFormat("yyyy/MM/dd HH:mm").parse(creationTime);
                        } catch (java.text.ParseException pe) {
                            throw new IllegalArgumentException("Invalid creation time format: " + creationTime);
                        }
                    }

                    // Create AccountModel object
                    AccountModel account = new AccountModel();
                    account.setUsername(username);
                    account.setPassword(password);
                    account.setPhone(phone);
                    account.setEmail(email);
                    account.setGender(gender);
                    account.setAddress(address);
                    account.setCreationTime(creationTime);
                    account.setAccountStatus(accountStatus);
                    account.setAccountType(accountType);
                    account.setBalance(balance);

                    accounts.add(account);
                } catch (IllegalArgumentException e) {
                    invalidRows++;
                    logger.warn("Skipping invalid CSV line: {}", e.getMessage());
                }
            }
            if (invalidRows > 0) {
                logger.info("Skipped {} invalid rows while reading CSV", invalidRows);
            }
            if (accounts.isEmpty() && !firstLine) {
                logger.info("No valid accounts found in CSV file");
            }
        }
        return accounts;
    }

    // Saves account data to CSV
    public void saveToCSV(List<AccountModel> accounts, boolean append) throws IOException {
        File file = new File(CSV_FILE_PATH);
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), StandardCharsets.UTF_8))) {
            if (!append || !file.exists() || file.length() == 0) {
                bw.write(EXPECTED_ACCOUNT_HEADER);
                bw.newLine();
            } else if (file.length() > 0) {
                try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                    if (raf.length() > 0) {
                        raf.seek(raf.length() - 1);
                        if (raf.readByte() != '\n') {
                            bw.newLine();
                        }
                    }
                }
            }

            for (AccountModel account : accounts) {
                StringBuilder line = new StringBuilder();
                line.append(account.getUsername()).append(",");
                line.append(account.getPassword()).append(",");
                line.append(account.getPhone()).append(",");
                line.append(account.getEmail()).append(",");
                line.append(account.getGender()).append(",");
                line.append(account.getAddress()).append(",");
                line.append(account.getCreationTime()).append(",");
                line.append(account.getAccountStatus().name()).append(",");
                line.append(account.getAccountType()).append(",");
                line.append(account.getBalance());
                bw.write(line.toString());
                bw.newLine();
            }
        }
    }

    // Loads user data (for table display)
    public List<String[]> loadUserData() throws IOException {
        List<String[]> userData = new ArrayList<>();
        File file = new File(CSV_FILE_PATH);
        if (!file.exists()) {
            throw new IOException("accounts.csv not found!"); // Translated from "accounts.csv 未找到！"
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] user = line.split(",", -1);
                if (firstLine) {
                    if (user.length != EXPECTED_ACCOUNT_FIELD_COUNT) {
                        throw new IOException("accounts.csv header does not match the expected 10 columns.");
                    }
                    firstLine = false;
                } else {
                    if (user.length == EXPECTED_ACCOUNT_FIELD_COUNT) {
                        userData.add(user);
                    } else {
                        System.err.println("Skipping data row due to incorrect column count (" + user.length + "): " + line); // Translated from "数据行字段数量与标题不匹配，跳过行"
                    }
                }
            }
        }
        return userData;
    }

    // Deletes users
    public void deleteUsers(Set<String> usernamesToDelete) throws IOException {
        List<String[]> allUsers = new ArrayList<>();
        File file = new File(CSV_FILE_PATH);
        if (!file.exists()) {
            throw new IOException("accounts.csv not found!"); // Translated from "accounts.csv 未找到！"
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                allUsers.add(line.split(",", -1));
            }
        }

        if (allUsers.isEmpty()) {
            throw new IOException("accounts.csv is empty or could not be read."); // Translated from "accounts.csv 文件为空或无法读取。"
        }

        List<String[]> remainingUsers = new ArrayList<>();
        remainingUsers.add(allUsers.get(0)); // Keep header
        if (allUsers.size() > 1) {
            for (int i = 1; i < allUsers.size(); i++) {
                String[] user = allUsers.get(i);
                if (user.length > 0 && user[0] != null && !usernamesToDelete.contains(user[0].trim())) {
                    remainingUsers.add(user);
                }
            }
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CSV_FILE_PATH, false))) {
            for (String[] user : remainingUsers) {
                bw.write(String.join(",", user));
                bw.newLine();
            }
        }
    }

    // Imports accounts
    public void importAccounts(File file) throws IOException {
        List<String> validDataLines = new ArrayList<>();
        boolean headerProcessed = false;
        int importedCount = 0;
        Set<String> existingUsernames = loadExistingUsernames();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                if (!headerProcessed) {
                    if (!line.trim().equalsIgnoreCase(EXPECTED_ACCOUNT_HEADER)) {
                        throw new IOException("Invalid header format. Expected: '" + EXPECTED_ACCOUNT_HEADER + "', Actual: '" + line + "'");
                    }
                    headerProcessed = true;
                    continue;
                }

                String[] fields = line.split(",", -1);
                if (fields.length != EXPECTED_ACCOUNT_FIELD_COUNT) {
                    System.err.println("Skipping invalid row (expected 10 fields, line " + lineNumber + "): " + line); // Translated from "Skipping invalid row (expected 10 fields, line "
                    continue;
                }

                String username = fields[0].trim();
                String password = fields[1].trim();
                String phone = fields[2].trim();
                String email = fields[3].trim();
                String gender = fields[4].trim();
                String address = fields[5].trim();
                String creationTime = fields[6].trim();
                String accountStatus = fields[7].trim();
                String accountType = fields[8].trim();
                String balanceStr = fields[9].trim();

                boolean isValidLine = true;
                if (username.isEmpty()) {
                    isValidLine = false;
                    System.err.println("Skipping invalid row (empty username, line " + lineNumber + "): " + line); // Translated from "Skipping invalid row (empty username, line "
                } else if (existingUsernames.contains(username)) {
                    isValidLine = false;
                    System.err.println("Skipping invalid row (username '" + username + "' exists, line " + lineNumber + "): " + line); // Translated from "Skipping invalid row (username '" + "' exists, line "
                } else {
                    try {
                        Double.valueOf(balanceStr);
                    } catch (NumberFormatException e) {
                        isValidLine = false;
                        System.err.println("Skipping invalid row (invalid balance, line " + lineNumber + "): " + line); // Translated from "Skipping invalid row (invalid balance, line "
                    }
                }

                if (isValidLine && !("ACTIVE".equalsIgnoreCase(accountStatus) || "FROZEN".equalsIgnoreCase(accountStatus))) {
                    System.err.println("Warning: Invalid account status ('" + accountStatus + "'), line " + lineNumber + ", will attempt import."); // Translated from "Warning: Invalid account status ('" + "'), line " + ", will attempt import."
                }
                if (isValidLine && !("personal".equalsIgnoreCase(accountType) || "Admin".equalsIgnoreCase(accountType))) {
                    System.err.println("Warning: Invalid account type ('" + accountType + "'), line " + lineNumber + ", will attempt import."); // Translated from "Warning: Invalid account type ('" + "'), line " + ", will attempt import."
                }
                if (isValidLine && !creationTime.isEmpty()) {
                    try {
                        new SimpleDateFormat("yyyy/MM/dd HH:mm").parse(creationTime);
                    } catch (java.text.ParseException pe) {
                        System.err.println("Warning: Invalid creation time ('" + creationTime + "'), line " + lineNumber + ", will attempt import."); // Translated from "Warning: Invalid creation time ('" + "'), line " + ", will attempt import."
                    }
                }

                if (isValidLine) {
                    validDataLines.add(line);
                    existingUsernames.add(username);
                }
            }

            if (!headerProcessed && lineNumber > 0) {
                throw new IOException("File is empty or missing valid header."); // Translated from "File is empty or missing valid header."
            } else if (lineNumber == 0 || (validDataLines.isEmpty() && lineNumber == 1)) {
                throw new IOException("File is empty or contains no valid data rows."); // Translated from "File is empty or contains no valid data rows."
            }
        }

        if (!validDataLines.isEmpty()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(CSV_FILE_PATH, true))) {
                File accountFile = new File(CSV_FILE_PATH);
                if (accountFile.exists() && accountFile.length() > 0) {
                    try (RandomAccessFile raf = new RandomAccessFile(accountFile, "r")) {
                        if (raf.length() > 0) {
                            raf.seek(raf.length() - 1);
                            if (raf.readByte() != '\n') {
                                bw.newLine();
                            }
                        }
                    }
                } else {
                    bw.write(EXPECTED_ACCOUNT_HEADER);
                    bw.newLine();
                }

                for (String dataLine : validDataLines) {
                    bw.write(dataLine);
                    bw.newLine();
                    importedCount++;
                }
            }
        }

        if (importedCount == 0) {
            throw new IOException("No valid account records to import."); // Translated from "No valid account records to import."
        }
    }

    // Gets existing usernames
    private Set<String> loadExistingUsernames() throws IOException {
        Set<String> usernames = new HashSet<>();
        File file = new File(CSV_FILE_PATH);
        if (!file.exists()) {
            return usernames;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // Skip header line
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length > 0 && !parts[0].trim().isEmpty()) {
                    usernames.add(parts[0].trim());
                }
            }
        }
        return usernames;
    }

    // Imports transaction records
    public void importTransactions(File file, String destinationFilePath) throws IOException {
        int importedCount = TransactionCSVImporterModel.importTransactions(file, destinationFilePath);
        if (importedCount == 0) {
            throw new IOException("No transactions imported."); // Translated from "No transactions imported."
        }
    }

    // Ensures file exists
    public void ensureFileExists(String filename, String header) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            if (file.createNewFile()) {
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                    bw.write(header);
                    bw.newLine();
                }
            }
        } else {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String firstLine = br.readLine();
                if (firstLine == null || !firstLine.trim().equalsIgnoreCase(header.trim())) {
                    System.err.println("Warning: File " + filename + " exists, but header does not match the expected value."); // Translated from "警告: 文件 " + " 存在，但标题与预期值不匹配。"
                    System.err.println("  Expected: " + header); // Translated from " 预期: "
                    System.err.println("  Actual: " + (firstLine != null ? firstLine : "<Empty>")); // Translated from " 实际: " + "<空>"
                }
            }
        }
    }

    // Gets account
    public AccountModel getAccount(String username, String password) {
        try {
            List<AccountModel> accounts = readFromCSV();
            for (AccountModel account : accounts) {
                if (account.getUsername().equals(username) && account.getPassword().equals(password)) {
                    return account;
                }
            }
            return null;
        } catch (IOException e) {
            logger.error("Error reading account CSV file", e); // Translated from "读取账户 CSV 文件时出错"
            throw new RuntimeException("Failed to read account: " + e.getMessage()); // Translated from "无法读取账户"
        }
    }

    // Gets account by username
    public AccountModel getAccountByUsername(String username) {
        try {
            List<AccountModel> accounts = readFromCSV();
            for (AccountModel account : accounts) {
                if (account.getUsername().equals(username)) {
                    return account;
                }
            }
            return null;
        } catch (IOException e) {
            logger.error("Error reading account CSV file", e); // Translated from "读取账户 CSV 文件时出错"
            throw new RuntimeException("Failed to read account: " + e.getMessage()); // Translated from "无法读取账户"
        }
    }

    // Updates customer info
    public boolean updateCustomerInfo(String username, String password, String phone, String email, String gender, String address) {
        try {
            List<AccountModel> accounts = readFromCSV();
            for (AccountModel account : accounts) {
                if (account.getUsername().equals(username)) {
                    account.setPassword(password != null && !password.isEmpty() ? password : account.getPassword());
                    account.setPhone(phone != null ? phone : account.getPhone());
                    account.setEmail(email != null ? email : account.getEmail());
                    account.setGender(gender != null ? gender : account.getGender());
                    account.setAddress(address != null ? address : account.getAddress());
                    saveToCSV(accounts, false);
                    return true;
                }
            }
            logger.warn("Account not found for username {}", username); // Translated from "未找到用户名 {} 的账户"
            return false;
        } catch (IOException e) {
            logger.error("Error updating account info", e); // Translated from "更新账户信息时出错"
            throw new RuntimeException("Failed to update account: " + e.getMessage()); // Translated from "无法更新账户"
        }
    }

    // Modifies account status
    public boolean modifyAccountStatus(String username, AccountModel.AccountStatus accountStatus) {
        try {
            List<AccountModel> accounts = readFromCSV();
            for (AccountModel account : accounts) {
                if (account.getUsername().equals(username)) {
                    account.setAccountStatus(accountStatus);
                    saveToCSV(accounts, false);
                    return true;
                }
            }
            logger.warn("Account not found for username {}", username); // Translated from "未找到用户名 {} 的账户"
            return false;
        } catch (IOException e) {
            logger.error("Error modifying account status", e); // Translated from "修改账户状态时出错"
            throw new RuntimeException("Failed to modify account status: " + e.getMessage()); // Translated from "无法修改账户状态"
        }
    }

    // Validates admin password
    public boolean isAdminPasswordValid(String adminPassword, String currentAdminUsername) {
        if (currentAdminUsername == null || currentAdminUsername.isEmpty()) {
            logger.warn("No admin logged in"); // Translated from "未登录管理员"
            return false;
        }

        try {
            List<AccountModel> accounts = readFromCSV();
            for (AccountModel account : accounts) {
                if (account.getUsername().equals(currentAdminUsername) && account.isAdmin()) {
                    return account.getPassword().equals(adminPassword);
                }
            }
            logger.warn("Admin account not found for username: {}", currentAdminUsername); // Translated from "未找到管理员账户，用户名: {}"
            return false;
        } catch (IOException e) {
            logger.error("Error validating admin password", e); // Translated from "验证管理员密码时出错"
            throw new RuntimeException("Failed to validate admin password: " + e.getMessage()); // Translated from "无法验证管理员密码"
        }
    }

    // Gets admin info
    public String[][] getAdminInfo(String username) throws IOException {
        File file = new File(CSV_FILE_PATH);
        if (!file.exists()) {
            throw new IOException("accounts.csv not found!"); // Translated from "accounts.csv 未找到！"
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                throw new IOException("accounts.csv file is empty or missing header"); // Translated from "accounts.csv 文件为空或缺少标题"
            }
            String[] headers = headerLine.split(",");

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length != headers.length) {
                    System.err.println("Data row field count does not match header, skipping row: " + line); // Translated from "数据行字段数量与标题不匹配，跳过行"
                    continue;
                }

                if (values[0].equals(username)) {
                    return new String[][]{headers, values};
                }
            }
            throw new IOException("Admin information not found for username " + username); // Translated from "未找到用户名 " + " 的管理员信息"
        }
    }
}