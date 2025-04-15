package model;

public class AdminAccount extends AccountModel {

    public AdminAccount(String username, String password, String phone, String email, String gender, String address, String creationTime, String accountStatus, String accountType, double balance) {
        super(username, password, phone, email, gender, address, creationTime, accountStatus, accountType, balance);
    }

    // 你可以在这里添加 AdminAccount 特有的方法和属性
}