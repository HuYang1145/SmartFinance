package Service;

import java.text.SimpleDateFormat;
import java.util.Date;

import Model.User;
import Model.UserSession;
import Repository.AccountRepository;


public class LoginService {
    private final AccountRepository accountRepository;

    public LoginService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public boolean loginUser(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空！");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空！");
        }

        User user = accountRepository.findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            return false;
        }

        if (user.getAccountStatus() == User.AccountStatus.FROZEN) {
            throw new IllegalStateException("此账户当前被冻结。请联系管理员。");
        }

        UserSession.setCurrentAccount(user);
        return true;
    }

    public void registerUser(
            String username, String password, String phone, String email,
            String gender, String address, String accountType) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                phone == null || phone.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                gender == null || gender.trim().isEmpty() ||
                address == null || address.trim().isEmpty() ||
                accountType == null || accountType.trim().isEmpty()) {
            throw new IllegalArgumentException("所有字段均需填写！");
        }

        if (accountRepository.findByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已存在！请选择其他用户名。");
        }

        String creationTime = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date());
        User.AccountStatus status = User.AccountStatus.ACTIVE;
        double balance = 0.0;

        User user = new User(username, password, phone, email, gender, address,
                creationTime, status, accountType, balance);
        accountRepository.save(user);
    }

    public AccountRepository getAccountRepository() {
        return accountRepository;
    }
}