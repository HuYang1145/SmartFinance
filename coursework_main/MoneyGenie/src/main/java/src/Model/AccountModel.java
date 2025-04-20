package src.Model; 

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// 最终整合和修复后的 AccountModel 抽象类
public abstract class AccountModel implements Serializable {
    private static final long serialVersionUID = 1L; // 序列化版本号

    // --- 核心账户字段 ---
    private String username;
    private String password; // 注意：直接存储明文密码存在安全风险
    private String phone;
    private String email;
    private String gender;
    private String address;
    private String creationTime;
    private String accountStatus;
    private String accountType; // 用于区分账户类型，例如 "admin" 或 "personal"
    private double balance;

    // --- 交易记录字段 ---
    private List<TransactionModel> transactions; // (确保 TransactionModel 类存在)

    // --- 构造函数 ---
    public AccountModel(String username, String password, String phone, String email, String gender, String address, String creationTime, String accountStatus, String accountType, double balance) {
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
        this.transactions = new ArrayList<>(); // 初始化为空列表
    }

    // --- Getter 和 Setter 方法 (具体的实现) ---
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; } // 返回 String
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

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; } // 具体 Setter

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
            // 可在此更新余额
        } else {
            System.err.println("警告：尝试向账户 " + this.username + " 添加无效或不匹配的交易。");
        }
    }


    // --- 必须由子类实现的抽象方法 ---
    /**
     * 判断该账户是否为管理员账户。子类必须实现此方法。
     */
    public abstract boolean isAdmin();


    // --- CSV 处理方法 ---
    public String toCSV() {
        StringBuilder sb = new StringBuilder();
        // 重要：确保 CSV 列顺序与 fromCSV 和实际文件一致！
        sb.append(getUsername()).append(",");
        sb.append(getPassword()).append(","); // 明文密码警告
        sb.append(getPhone()).append(",");
        sb.append(getEmail()).append(",");
        sb.append(getGender()).append(",");
        sb.append(getAddress()).append(",");
        sb.append(getCreationTime()).append(",");
        sb.append(getAccountStatus()).append(",");
        sb.append(getAccountType()).append(",");
        sb.append(getBalance());
        return sb.toString();
    }

    public static AccountModel fromCSV(String csvLine) {
        // 重要：再次核对 CSV 列顺序和数量！
        // 假设顺序：0:user, 1:pass, 2:type, 3:phone, 4:email, 5:gender, 6:address, 7:creation, 8:status, 9:balance
        String[] parts = csvLine.split(",", -1);
        if (parts.length >= 10) {
            try {
                String username = parts[0].trim();
                String password = parts[1].trim();
                String accountType = parts[2].trim();
                String phone = parts[3].trim();
                String email = parts[4].trim();
                String gender = parts[5].trim();
                String address = parts[6].trim();
                String creationTime = parts[7].trim();
                String accountStatus = parts[8].trim();
                double balance = Double.parseDouble(parts[9].trim());

                if ("admin".equalsIgnoreCase(accountType)) {
                    // 确保 AdminAccount 构造函数匹配
                    return new AdminAccount(username, password, phone, email, gender, address, creationTime, accountStatus, accountType, balance);
                } else if ("personal".equalsIgnoreCase(accountType)) {
                    // 确保 PersonalAccount 构造函数匹配
                    return new PersonalAccount(username, password, phone, email, gender, address, creationTime, accountStatus, accountType, balance);
                } else {
                    System.err.println("从 CSV 加载账户失败：未知的账户类型 '" + accountType + "' in line: " + csvLine);
                    return null;
                }
            } catch (Exception e) {
                System.err.println("从 CSV 解析账户时发生错误 in line: " + csvLine + " Error: " + e.getMessage());
                return null;
            }
        } else {
            System.err.println("从 CSV 加载账户失败：字段数量不足 (" + parts.length + ") in line: " + csvLine);
            return null;
        }
    }

    // 确认已移除所有冗余的抽象方法
}