package model;

public class AccountValidator {

    // 检查字段是否为空
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
    
}
