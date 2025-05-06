package Model;

public class UserSession {
    private static String currentUsername;
    private static User currentAccount;

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static void setCurrentUsername(String username) {
        UserSession.currentUsername = username;
    }

    public static User getCurrentAccount() {
        return currentAccount;
    }

    public static void setCurrentAccount(User account) {
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