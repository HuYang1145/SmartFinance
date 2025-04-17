package model;

import java.io.Serializable;
import java.util.ArrayList; // <<<=== 导入 ArrayList
import java.util.List;      // <<<=== 导入 List

// 抽象类 AccountModel
public abstract class AccountModel implements Serializable {
    private static final long serialVersionUID = 1L; // 良好的序列化实践

    // --- 原有字段 ---
    private String username;
    private String password;
    private String phone;
    private String email;
    private String gender;
    private String address;
    private String creationTime;
    private String accountStatus;
    private String accountType;
    private double balance;

    // --- 新增字段 ---
    private List<TransactionModel> transactions; // <<<=== 新增：用于存储该账户交易记录的列表

    // 构造函数 - 需要初始化新增的 transactions 列表
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
        this.transactions = new ArrayList<>(); // <<<=== 新增：在构造时初始化为空的 ArrayList
    }

    // --- 原有的 Getter 和 Setter 方法 ---
    // ... (保持不变) ...
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
    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }


    // --- 新增的与交易记录相关的方法 ---

    /**
     * 获取此账户的交易记录列表。
     * 这是 TransactionChecker 所需的关键方法。
     * @return 一个包含 TransactionModel 对象的列表。
     */
    public List<TransactionModel> getTransactions() { // <<<=== 新增：必需的 getTransactions() 方法
        // 为了安全，可以考虑返回列表的副本，防止外部直接修改内部列表
        // return new ArrayList<>(this.transactions);
        // 或者直接返回内部列表，如果允许外部修改（需要谨慎管理）
         return this.transactions;
    }

    /**
     * 向此账户的交易历史中添加一笔交易。
     * 同时可以进行校验，确保交易属于当前账户。
     * @param transaction 要添加的 TransactionModel 对象。
     */
    public void addTransaction(TransactionModel transaction) { // <<<=== 新增：添加交易记录的方法
        if (transaction != null && this.username.equals(transaction.getAccountUsername())) {
             this.transactions.add(transaction);
             // 可以在这里根据交易类型和金额更新账户余额 balance
             // 示例逻辑 (需要根据实际情况调整):
             // if ("deposit".equalsIgnoreCase(transaction.getType())) {
             //     this.balance += transaction.getAmount();
             // } else if ("withdrawal".equalsIgnoreCase(transaction.getType()) || "transfer".equalsIgnoreCase(transaction.getType())) {
             //     // 需要检查余额是否足够
             //     if (this.balance >= transaction.getAmount()) {
             //          this.balance -= transaction.getAmount();
             //     } else {
             //          // 处理余额不足的情况，可能抛出异常或阻止交易
             //          System.err.println("错误：账户 " + this.username + " 余额不足以完成交易 " + transaction.getTransactionId());
             //          this.transactions.remove(transaction); // 如果添加了，需要移除
             //          return; // 或者抛出异常
             //     }
             // }
        } else {
            // 处理错误：交易为空或尝试添加不属于此账户的交易
            System.err.println("错误：无法添加空交易或属于用户 " +
                               (transaction != null ? transaction.getAccountUsername() : "null") + " 的交易到账户 " + this.username);
        }
    }

    // --- CSV 处理相关方法 ---

    /**
     * 将账户的基本信息转换为 CSV 格式。
     * **重要提示：** 这个方法 *不应该* 包含交易列表 (`transactions`) 的信息。
     * 将列表数据嵌入单行 CSV 是非常复杂且不推荐的做法。
     * 交易数据应该存储在单独的文件中。
     * @return CSV 格式的字符串，仅包含账户基本信息。
     */
    public String toCSV() {
        // 此方法保持不变，只输出账户本身的信息
        StringBuilder sb = new StringBuilder();
        sb.append(getUsername()).append(",");
        sb.append(getPassword()).append(",");
        sb.append(getPhone()).append(","); // 注意：原始 fromCSV 假设第3个是 accountType，请根据实际 CSV 调整
        sb.append(getEmail()).append(",");
        sb.append(getGender()).append(",");
        sb.append(getAddress()).append(",");
        sb.append(getCreationTime()).append(",");
        sb.append(getAccountStatus()).append(",");
        sb.append(getAccountType()).append(","); // 假设 accountType 现在是第9个
        sb.append(getBalance());
        return sb.toString();
    }

    /**
     * 从 CSV 格式的一行创建 AccountModel 对象（不包括交易记录）。
     * **重要提示：** 加载交易记录需要在此方法创建账户对象 *之后*，
     * 通过读取单独的交易文件，并将交易添加到对应的账户对象中来完成。
     *
     * **警告：** 你提供的原始 fromCSV 方法中的索引似乎与 toCSV 方法的顺序不完全匹配
     * （例如，accountType 在 fromCSV 中是第 3 个，但在 toCSV 中是第 9 个）。
     * 请务必根据你的实际 CSV 文件格式调整这里的索引！
     *
     * @param csvLine CSV 文件中的一行数据
     * @return 创建的 AccountModel 子类实例 (AdminAccount 或 PersonalAccount)，如果格式错误则返回 null
     */
    public static AccountModel fromCSV(String csvLine) {
        // 假设 CSV 顺序是：0:user, 1:pass, 2:type, 3:phone, 4:email, 5:gender, 6:address, 7:creation, 8:status, 9:balance
        // (这是根据你之前代码的 fromCSV 推断的，请务必核对！)
        String[] parts = csvLine.split(",");
        if (parts.length >= 10) {
            try {
                String username = parts[0];
                String password = parts[1];
                String accountType = parts[2]; // 假设类型在第3个位置
                String phone = parts[3];
                String email = parts[4];
                String gender = parts[5];
                String address = parts[6];
                String creationTime = parts[7];
                String accountStatus = parts[8];
                double balance = Double.parseDouble(parts[9]); // 解析余额

                AccountModel account = null;
                // 使用 equalsIgnoreCase 增加鲁棒性
                if ("Admin".equalsIgnoreCase(accountType)) {
                    account = new AdminAccount(username, password, phone, email, gender, address, creationTime, accountStatus, accountType, balance);
                } else if ("personal".equalsIgnoreCase(accountType)) {
                    account = new PersonalAccount(username, password, phone, email, gender, address, creationTime, accountStatus, accountType, balance);
                } else {
                    System.err.println("未知的账户类型: " + accountType + " 在CSV行: " + csvLine);
                    return null; // 或者抛出异常
                }

                // 重要：交易记录没有在这里加载！
                // 你需要在调用此方法创建 account 对象后，再执行类似 loadTransactionsForAccount(account) 的逻辑。
                return account;

            } catch (NumberFormatException e) {
                 System.err.println("从CSV解析余额失败: " + csvLine);
                return null;
            } catch (ArrayIndexOutOfBoundsException e) {
                 System.err.println("从CSV解析账户数据失败 - 部件不足: " + csvLine);
                return null;
            }
        }
        System.err.println("从CSV解析账户数据失败 - 部件不足: " + csvLine);
        return null; // CSV 格式错误
    }
}