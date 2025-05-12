/**
 * Manages user authentication and registration processes by interacting with the account repository.
 * Provides functionality for user login and registration with validation checks.
 *
 * @author Group 19
 * @version 1.0
 */
package Service;

import java.text.SimpleDateFormat;
import java.util.Date;

import Model.User;
import Model.UserSession;
import Repository.AccountRepository;

public class LoginService {
    private final AccountRepository accountRepository;

    /**
     * Constructs a LoginService instance with the specified account repository.
     *
     * @param accountRepository the repository for accessing and managing user account data
     */
    public LoginService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Authenticates a user by verifying the provided username and password.
     * Sets the current user session if authentication is successful.
     *
     * @param username the username of the user attempting to log in
     * @param password the password provided by the user
     * @return true if login is successful, false otherwise
     * @throws IllegalArgumentException if username or password is null or empty
     * @throws IllegalStateException if the account is frozen
     */
    public boolean loginUser(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty!");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty!");
        }

        User user = accountRepository.findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            return false;
        }

        if (user.getAccountStatus() == User.AccountStatus.FROZEN) {
            throw new IllegalStateException("This account is currently frozen. Please contact the administrator.");
        }

        UserSession.setCurrentAccount(user);
        return true;
    }

    /**
     * Registers a new user with the provided details and saves them to the repository.
     * Validates all input fields and checks for duplicate usernames.
     *
     * @param username    the username for the new account
     * @param password    the password for the new account
     * @param phone       the phone number of the user
     * @param email       the email address of the user
     * @param gender      the gender of the user
     * @param address     the address of the user
     * @param accountType the type of account
     * @throws IllegalArgumentException if any field is null, empty, or if the username already exists
     */
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
            throw new IllegalArgumentException("All fields must be filled!");
        }

        if (accountRepository.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists! Please choose another username.");
        }

        String creationTime = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date());
        User.AccountStatus status = User.AccountStatus.ACTIVE;
        double balance = 0.0;

        User user = new User(username, password, phone, email, gender, address,
                creationTime, status, accountType, balance);
        accountRepository.save(user);
    }

    /**
     * Retrieves the account repository used by this service.
     *
     * @return the AccountRepository instance
     */
    public AccountRepository getAccountRepository() {
        return accountRepository;
    }
}