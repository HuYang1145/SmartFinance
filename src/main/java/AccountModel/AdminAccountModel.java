package AccountModel;

public class AdminAccountModel extends AccountModel {
    public AdminAccountModel(String username, String password, String phone, String email, String gender, String address, 
                       String creationTime, AccountStatus accountStatus, String accountType, double balance) {
        super(username, password, phone, email, gender, address, creationTime, accountStatus, accountType, balance);
    }

    @Override
    public boolean isAdmin() {
        return true;
    }
}