package Repository;

import Model.User;
import Model.UserSession;

public class UserRepository {
    public User getCurrentUser() {
        return UserSession.getCurrentAccount();
    }
}