package Model; //  假设放在 model 文件夹下，你也可以放在其他合适的包下

public class UserSession {
    private static String currentUsername; // 静态变量，存储当前登录的用户名

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static void setCurrentUsername(String username) {
        UserSession.currentUsername = username;
    }

    public static void clearSession() { // 可选：用于退出登录时清除 Session
        UserSession.currentUsername = null;
    }
}
