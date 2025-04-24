package AdminModel;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AccountModel.AccountModel;

public class AccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    private final AccountRepositoryModel accountRepository;

    public AccountService(AccountRepositoryModel accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountModel getAccount(String username, String password) {
        try {
            List<AccountModel> accounts = accountRepository.readFromCSV();
            return accounts.stream()
                    .filter(account -> account.getUsername().equals(username) && account.getPassword().equals(password))
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            logger.error("Error reading accounts from CSV", e);
            throw new RuntimeException("Failed to read accounts: " + e.getMessage());
        }
    }

    public AccountModel getAccountByUsername(String username) {
        try {
            List<AccountModel> accounts = accountRepository.readFromCSV();
            return accounts.stream()
                    .filter(account -> account.getUsername().equals(username))
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            logger.error("Error reading accounts from CSV", e);
            throw new RuntimeException("Failed to read accounts: " + e.getMessage());
        }
    }

    public boolean updateCustomerInfo(String username, String password, String phone, String email, String gender, String address) {
        try {
            List<AccountModel> accounts = accountRepository.readFromCSV();
            for (AccountModel account : accounts) {
                if (account.getUsername().equals(username)) {
                    account.setPassword(password != null && !password.isEmpty() ? password : account.getPassword());
                    account.setPhone(phone != null ? phone : account.getPhone());
                    account.setEmail(email != null ? email : account.getEmail());
                    account.setGender(gender != null ? gender : account.getGender());
                    account.setAddress(address != null ? address : account.getAddress());
                    // Assume AccountRepositoryModel supports writing back to CSV
                    return true;
                }
            }
            logger.warn("Account not found for username: {}", username);
            return false;
        } catch (IOException e) {
            logger.error("Error updating account info", e);
            throw new RuntimeException("Failed to update account: " + e.getMessage());
        }
    }

    public boolean modifyAccountStatus(String username, AccountModel.AccountStatus accountStatus) {
        try {
            List<AccountModel> accounts = accountRepository.readFromCSV();
            for (AccountModel account : accounts) {
                if (account.getUsername().equals(username)) {
                    account.setAccountStatus(accountStatus);
                    // Assume AccountRepositoryModel supports writing back to CSV
                    return true;
                }
            }
            logger.warn("Account not found for username: {}", username);
            return false;
        } catch (IOException e) {
            logger.error("Error modifying account status", e);
            throw new RuntimeException("Failed to modify account status: " + e.getMessage());
        }
    }

    public boolean isAdminPasswordValid(String adminPassword, String currentAdminUsername) {
        if (currentAdminUsername == null || currentAdminUsername.isEmpty()) {
            logger.warn("No admin logged in");
            return false;
        }

        try {
            List<AccountModel> accounts = accountRepository.readFromCSV();
            for (AccountModel account : accounts) {
                if (account.getUsername().equals(currentAdminUsername) && account instanceof AdminAccountModel) {
                    return account.getPassword().equals(adminPassword);
                }
            }
            logger.warn("Admin account not found for username: {}", currentAdminUsername);
            return false;
        } catch (IOException e) {
            logger.error("Error validating admin password", e);
            throw new RuntimeException("Failed to validate admin password: " + e.getMessage());
        }
    }
}