package Model;

public class UserSessionRunner {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("getCurrentUsername")) {
            // 输出当前用户名到标准输出
            System.out.println(UserSession.getCurrentUsername());
        }
    }
}