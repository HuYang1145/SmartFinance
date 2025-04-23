package AccountModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class AccountModel implements Serializable {
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
            System.err.println("警告：尝试向账户 " + this.username + " 添加无效或不匹配的交易。");
        }
    }

    // --- 抽象方法 ---
    public abstract boolean isAdmin();

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

    public static AccountModel fromCSV(String csvLine) {
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
                AccountStatus accountStatus = AccountStatus.valueOf(parts[7].trim().toUpperCase());
                String accountType = parts[8].trim();
                double balance = Double.parseDouble(parts[9].trim());

                if ("admin".equalsIgnoreCase(accountType)) {
                    return new AdminAccount(username, password, phone, email, gender, address, 
                                          creationTime, accountStatus, accountType, balance);
                } else if ("personal".equalsIgnoreCase(accountType)) {
                    return new PersonalAccount(username, password, phone, email, gender, address, 
                                             creationTime, accountStatus, accountType, balance);
                } else {
                    System.err.println("从 CSV 加载账户失败：未知的账户类型 '" + accountType + "' in line: " + csvLine);
                    return null;
                }
            } catch (NumberFormatException e) {
                System.err.println("从 CSV 解析账户时发生错误 in line: " + csvLine + " Error: " + e.getMessage());
                return null;
            } catch (IllegalArgumentException e) {
                System.err.println("从 CSV 解析账户状态失败：无效的状态值 in line: " + csvLine + " Error: " + e.getMessage());
                return null;
            }
        } else {
            System.err.println("从 CSV 加载账户失败：字段数量不足 (" + parts.length + ") in line: " + csvLine);
            return null;
        }
    }
}