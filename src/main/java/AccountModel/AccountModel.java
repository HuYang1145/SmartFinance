package AccountModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import TransactionModel.TransactionModel;

public class AccountModel implements Serializable {
    private static final long serialVersionUID = 1L;

    // 账户状态枚举
    public enum AccountStatus {
        ACTIVE,     // 激活状态
        FROZEN      // 冻结状态
    }

    // --- 核心账户字段 ---
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
    private List<TransactionModel> transactions;

    // --- 构造函数 ---
    public AccountModel() {
        this.transactions = new ArrayList<>();
    }

    public AccountModel(String username, String password, String phone, String email, String gender, String address,
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

    // --- Getter 和 Setter 方法 ---
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

    // --- 交易相关方法 ---
    public List<TransactionModel> getTransactions() {
        return this.transactions;
    }

    public void addTransaction(TransactionModel transaction) {
        if (this.transactions == null) {
            this.transactions = new ArrayList<>();
        }
        if (transaction != null && this.username.equals(transaction.getAccountUsername())) {
            this.transactions.add(transaction);
        } else {
            throw new IllegalArgumentException("Invalid or mismatched transaction for account: " + this.username);
        }
    }

    // --- 判断是否为管理员 ---
    public boolean isAdmin() {
        return "Admin".equalsIgnoreCase(accountType);
    }

    // --- CSV 处理方法 ---
    public String toCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append(getUsername()).append(",");
        sb.append(getPassword()).append(",");
        sb.append(getPhone()).append(",");
        sb.append(getEmail()).append(",");
        sb.append(getGender()).append(",");
        sb.append(getAddress()).append(",");
        sb.append(getCreationTime()).append(",");
        sb.append(getAccountStatus().name()).append(",");
        sb.append(getAccountType()).append(",");
        sb.append(getBalance());
        return sb.toString();
    }

    public static AccountModel fromCSV(String csvLine) throws IllegalArgumentException {
        String[] parts = csvLine.split(",", -1);
        if (parts.length >= 10) {
            try {
                String username = parts[0].trim().isEmpty() ? null : parts[0].trim();
                String password = parts[1].trim().isEmpty() ? null : parts[1].trim();
                String phone = parts[2].trim().isEmpty() ? null : parts[2].trim();
                String email = parts[3].trim().isEmpty() ? null : parts[3].trim();
                String gender = parts[4].trim().isEmpty() ? null : parts[4].trim();
                String address = parts[5].trim().isEmpty() ? null : parts[5].trim();
                String creationTime = parts[6].trim().isEmpty() ? null : parts[6].trim();
                String statusStr = parts[7].trim().toUpperCase();
                String accountType = parts[8].trim().isEmpty() ? null : parts[8].trim();
                String balanceStr = parts[9].trim();

                if (username == null || password == null || accountType == null) {
                    throw new IllegalArgumentException("Required fields (username, password, accountType) cannot be empty in CSV line: " + csvLine);
                }

                AccountStatus accountStatus = AccountStatus.valueOf(statusStr);
                double balance = balanceStr.isEmpty() ? 0.0 : Double.parseDouble(balanceStr);

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

                return account;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid balance format in CSV line: " + csvLine, e);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid account status or other field in CSV line: " + csvLine, e);
            }
        } else {
            throw new IllegalArgumentException("Insufficient fields (" + parts.length + ") in CSV line: " + csvLine);
        }
    }
}