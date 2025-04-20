package src.Model;

public class AdminAccount extends AccountModel {

    public AdminAccount(String username, String password, String phone, String email, String gender, String address, String creationTime, String accountStatus, String accountType, double balance) {
        // 调用父类构造函数
        super(username, password, phone, email, gender, address, creationTime, accountStatus, accountType, balance);
        // 如果 AdminAccount 有自己特有的属性，可以在这里初始化
    }

    /**
     * 实现父类的抽象方法：管理员账户总是返回 true。
     */
    @Override
    public boolean isAdmin() {
        return true; // 管理员账户实现
    }

    // --- 重点：删除了所有其他返回 Object 的 @Override 方法 ---
    // 例如：getAccountStatus(), getCreationTime(), getAddress(), getGender(), getEmail(), getPhone(), getPassword()
    // 这些方法现在直接从父类 AccountModel 继承具体的实现。

    // 你可以在这里添加 AdminAccount 特有的方法和属性
    // 例如：public void manageUserAccounts() { ... }
}