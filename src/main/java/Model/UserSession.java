/**
 * Manages the current user session in the Smart Finance Application.
 * Provides static methods to store, retrieve, and clear the current user's
 * username and account details, maintaining session state across the application.
 *
 * @author Group 19
 * @version 1.0
 */
package Model;

public class UserSession {
    /** The username of the currently logged-in user. */
    private static String currentUsername;

    /** The account details of the currently logged-in user. */
    private static User currentAccount;

    /**
     * Gets the username of the currently logged-in user.
     *
     * @return The current username, or null if no user is logged in.
     */
    public static String getCurrentUsername() {
        return currentUsername;
    }

    /**
     * Sets the username of the currently logged-in user.
     *
     * @param username The username to set.
     */
    public static void setCurrentUsername(String username) {
        UserSession.currentUsername = username;
    }

    /**
     * Gets the account details of the currently logged-in user.
     *
     * @return The current {@link User} account, or null if no user is logged in.
     */
    public static User getCurrentAccount() {
        return currentAccount;
    }

    /**
     * Sets the account details of the currently logged-in user and updates the username.
     *
     * @param account The {@link User} account to set, or null to clear the session.
     */
    public static void setCurrentAccount(User account) {
        UserSession.currentAccount = account;
        if (account != null) {
            currentUsername = account.getUsername();
        }
    }

    /**
     * Checks if the current user is an administrator.
     *
     * @return true if the current user has an admin account type, false otherwise
     */
    public static boolean isAdmin() {
        return currentAccount != null && currentAccount.isAdmin();
    }

    /**
     * Clears the current user session, resetting the username and account to null.
     */
    public static void clearSession() {
        currentUsername = null;
        currentAccount = null;
    }
}