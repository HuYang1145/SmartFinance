package Model;

public class AdminAccount extends AccountModel {

    public AdminAccount(String username, String password, String phone, String email, String gender, String address, String creationTime, String accountStatus, String accountType, double balance) {
        super(username, password, phone, email, gender, address, creationTime, accountStatus, accountType, balance);
    }

    @Override
    public boolean isAdmin() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isAdmin'");
    }

    @Override
    protected Object getAccountStatus() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAccountStatus'");
    }

    @Override
    protected Object getCreationTime() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCreationTime'");
    }

    @Override
    protected Object getAddress() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAddress'");
    }

    @Override
    protected Object getGender() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getGender'");
    }

    @Override
    protected Object getEmail() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEmail'");
    }

    @Override
    protected Object getPhone() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPhone'");
    }

    @Override
    protected Object getPassword() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPassword'");
    }

    // 你可以在这里添加 AdminAccount 特有的方法和属性
}