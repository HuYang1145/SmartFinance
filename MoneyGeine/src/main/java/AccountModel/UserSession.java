package AccountModel;

public class UserSession {
    private static String currentUsername;
    private static AccountModel currentAccount;

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static void setCurrentUsername(String username) {
        UserSession.currentUsername = username;
    }

    public static AccountModel getCurrentAccount() {
        return currentAccount;
    }

    public static void setCurrentAccount(AccountModel account) {
        UserSession.currentAccount = account;
        if (account != null) {
            currentUsername = account.getUsername();
        }
    }

    public static void clearSession() {
        currentUsername = null;
        currentAccount = null;
    }
}
