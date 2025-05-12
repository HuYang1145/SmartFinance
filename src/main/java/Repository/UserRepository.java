package Repository;

import Model.User;
import Model.UserSession;

/**
 * Repository class for accessing user data in the Smart Finance Application.
 * This class provides methods to retrieve the currently logged-in user from the session.
 *
 * @author Group 19
 * @version 1.0
 */
public class UserRepository {
    /**
     * Retrieves the currently logged-in user from the session.
     *
     * @return The current {@link User} account, or null if no user is logged in.
     */
    public User getCurrentUser() {
        return UserSession.getCurrentAccount();
    }
}