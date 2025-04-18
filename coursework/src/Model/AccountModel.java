package Model;

import java.io.Serializable;

public abstract class AccountModel implements Serializable {
    // ... 现有属性 ...
    private String username;
    private String password;
    private String phone;
    private String email;
    private String gender;
    private String address;
    private String creationTime;
    private String accountStatus;
    private String accountType; // 这个字段用于区分账户类型，如 "admin" 或 "personal"
    private double balance;

    // ... 现有构造函数 ...
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
    }

    // ... 现有 Getter 和 Setter 方法 ...
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    // ... 其他 getter/setter ...
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }


    // *** 添加这个抽象方法声明 ***
    /**
     * 判断该账户是否为管理员账户。
     * 子类需要实现此方法。
     * @return 如果是管理员账户返回 true，否则返回 false。
     */
    public abstract boolean isAdmin(); // 添加此行


    // ... 现有 toCSV 方法 ...
    public String toCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append(getUsername()).append(",");
        sb.append(getPassword()).append(",");
        sb.append(getPhone()).append(","); // 注意：这里 toCSV 的字段顺序需要与 fromCSV 匹配
        sb.append(getEmail()).append(",");
        sb.append(getGender()).append(",");
        sb.append(getAddress()).append(",");
        sb.append(getCreationTime()).append(",");
        sb.append(getAccountStatus()).append(",");
        sb.append(getAccountType()).append(","); // accountType
        sb.append(getBalance());
        return sb.toString();
    }


    public abstract Object getAccountStatus();

    protected abstract Object getCreationTime();

    protected abstract Object getAddress();

    protected abstract Object getGender();

    protected abstract Object getEmail();

    protected abstract Object getPhone();

    public abstract Object getPassword();

    // ... 现有 fromCSV 方法 ...
    public static AccountModel fromCSV(String csvLine) {
        // 注意：这里需要与 toCSV 的顺序和 accounts.csv 的实际顺序匹配
        // 根据你 AdminUI 中 EXPECTED_ACCOUNT_HEADER = "username,password,balance,isAdmin" 的定义，
        // 以及 loadUserData 中读取 accounts.csv 的逻辑，accounts.csv 文件的字段顺序可能与 AccountModel 的属性顺序不一致。
        // 如果 accounts.csv 实际只有 username,password,balance,isAdmin 四列：
        // String[] parts = csvLine.split(",", -1);
        // if (parts.length == 4) {
        //     String username = parts[0];
        //     String password = parts[1];
        //     double balance = 0.0; // 账户导入不包含余额，或者需要从其他地方获取
        //     try { balance = Double.parseDouble(parts[2].trim()); } catch (NumberFormatException e) { /* handle error */ }
        //     boolean isAdmin = Boolean.parseBoolean(parts[3].trim());
        //
        //     // 这里需要根据 isAdmin 创建 AdminAccount 或 PersonalAccount
        //     if (isAdmin) {
        //          // 需要 AdminAccount 的构造函数接受这些参数
        //          return new AdminAccount(username, password, /* other admin specific fields? */, true);
        //     } else {
        //          // 需要 PersonalAccount 的构造函数接受这些参数
        //          return new PersonalAccount(username, password, balance, /* other personal fields? */, false);
        //     }
        // }
        // return null; // 处理格式错误

        // 如果 accounts.csv 实际包含所有 AccountModel 的字段，并且 isAdmin 状态是通过 accountType ("admin"/"personal") 表示：
        String[] parts = csvLine.split(",", -1);
        // 检查字段数量是否正确 (假设 accounts.csv 包含了所有 AccountModel 的属性，共 10 个字段)
        if (parts.length >= 10) {
             // 请根据 accounts.csv 实际的列顺序来解析 parts 数组
             // 根据你之前的 fromCSV 代码，顺序似乎是：username, password, accountType, phone, email, gender, address, creationTime, accountStatus, balance
             // 确认一下这个顺序是否与 accounts.csv 文件以及 AdminUI 的导入逻辑（尤其是 importAccountsFromCSV 中的验证）一致。

            String username = parts[0].trim();
            String password = parts[1].trim();
            String accountType = parts[2].trim(); // 获取 accountType
            String phone = parts[3].trim();
            String email = parts[4].trim();
            String gender = parts[5].trim();
            String address = parts[6].trim();
            String creationTime = parts[7].trim();
            String accountStatus = parts[8].trim();
            double balance = 0.0;
            try {
                 balance = Double.parseDouble(parts[9].trim()); // 解析余额
            } catch (NumberFormatException e) {
                 System.err.println("Error parsing balance for user: " + username + ". Value: " + parts[9]);
                 // 可以选择返回 null 或给一个默认余额
            }


            if ("admin".equalsIgnoreCase(accountType)) {
                // 创建 AdminAccount 实例
                // 假设 AdminAccount 构造函数接收 AccountModel 的所有字段
                 return new AdminAccount(username, password, phone, email, gender, address, creationTime, accountStatus, accountType, balance);
            } else if ("personal".equalsIgnoreCase(accountType)) {
                // 创建 PersonalAccount 实例
                // 假设 PersonalAccount 构造函数接收 AccountModel 的所有字段
                 return new PersonalAccount(username, password, phone, email, gender, address, creationTime, accountStatus, accountType, balance);
            }
             // 如果 accountType 既不是 "admin" 也不是 "personal"
             System.err.println("Unknown account type for user: " + username + ". Type: " + accountType);
             return null;

        } else {
             // 字段数量不正确
             System.err.println("Skipping CSV line due to incorrect field count (" + parts.length + "): " + csvLine);
        }
        return null; // 处理 CSV 格式错误的情况
    }

    protected abstract void setAccountStatus(String newStatus);
}