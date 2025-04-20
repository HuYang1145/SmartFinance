package Model;

public class PersonalAccount extends AccountModel {

    public PersonalAccount(String username, String password, String phone, String email, String gender, String address, String creationTime, String accountStatus, String accountType, double balance) {
        // 调用父类构造函数
        super(username, password, phone, email, gender, address, creationTime, accountStatus, accountType, balance);
        // 如果 PersonalAccount 有自己特有的属性，可以在这里初始化
    }

    /**
     * 实现父类的抽象方法：个人账户不是管理员，返回 false。
     */
    @Override
    public boolean isAdmin() {
        return false; // 个人账户实现
    }

    
}