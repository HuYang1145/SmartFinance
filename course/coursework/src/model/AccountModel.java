package model;

import java.io.Serializable;

public abstract class AccountModel implements Serializable {
    private String username;
    private String password;
    private String phone;
    private String email;
    private String gender;
    private String address;
    private String creationTime;
    private String accountStatus;
    private String accountType;
    private double balance; // 余额属性

    // 构造函数，接收所有通用属性
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
        this.balance = balance; // 初始化 balance 属性
    }

    // Getter 和 Setter 方法 for username, password, and other properties
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    // Getter and Setter for balance
    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    // toCSV 方法，用于将账户信息转换为 CSV 格式
    public String toCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append(getUsername()).append(",");
        sb.append(getPassword()).append(",");
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

    // 从 CSV 格式创建 AccountModel 对象 (假设 CSV 数据的顺序是固定的)
    public static AccountModel fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length >= 10) { // 注意这里要增加判断，因为现在有 10 个字段
            String username = parts[0];
            String password = parts[1];
            String accountType = parts[2];
            double balance = Double.parseDouble(parts[9]); // 解析余额
            String phone = parts[3];
            String email = parts[4];
            String gender = parts[5];
            String address = parts[6];
            String creationTime = parts[7];
            String accountStatus = parts[8];

            if ("admin".equalsIgnoreCase(accountType)) {
                return new AdminAccount(username, password, phone, email, gender, address, creationTime, accountStatus, accountType, balance);
            } else if ("personal".equalsIgnoreCase(accountType)) {
                return new PersonalAccount(username, password, phone, email, gender, address, creationTime, accountStatus, accountType, balance);
            }
        }
        return null; // 处理 CSV 格式错误的情况
    }
}
