package model;

public class PersonalAccount extends AccountModel {

    public PersonalAccount(String username, String password, String phone, String email, String gender, String address, String creationTime, String accountStatus, String accountType, double balance) {
        super(username, password, phone, email, gender, address, creationTime, accountStatus, accountType, balance);
    }

    // 你可以在这里添加 PersonalAccount 特有的方法和属性
}