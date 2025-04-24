package PersonModel;

import AccountModel.AccountModel;

public class UserSessionModel {
    private static String currentUsername;
    private static AccountModel currentAccount;

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static void setCurrentUsername(String username) {
        UserSessionModel.currentUsername = username;
    }

    public static AccountModel getCurrentAccount() {
        return currentAccount;
    }

    public static void setCurrentAccount(AccountModel account) {
        UserSessionModel.currentAccount = account;
        if (account != null) {
            currentUsername = account.getUsername();
        }
    }

    public static void clearSession() {
        currentUsername = null;
        currentAccount = null;
    }
}
