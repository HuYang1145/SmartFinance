package AccountModel;

import java.util.List;

public class TransactionChecker {

    /**
     * 检查给定账户是否存在“异常”交易。
     * 当前定义：异常交易是指类型为 'Transfer Out' 或 'Transfer In' 且金额大于 500 的交易。 // <-- 更新了注释
     *
     * @param account 需要检查的 AccountModel 对象。
     * @return 如果找到异常交易，返回 {@code true}；否则返回 {@code false}。
     */
    public static boolean hasAbnormalTransactions(AccountModel account) {
        // 1. 安全性检查：确认传入的 account 对象不是 null
        if (account == null) {
            System.err.println("TransactionChecker：收到的账户对象为 null。");
            return false; // null 账户无法检查
        }

        // 2. 从账户对象获取交易列表
        List<TransactionModel> transactions = account.getTransactions();

        // 3. 检查交易列表是否为 null 或空
        if (transactions == null || transactions.isEmpty()) {
             // System.out.println("TransactionChecker: 账户 " + account.getUsername() + " 没有交易记录。"); // 可以取消注释以进行调试
            return false; // 没有交易记录，自然没有异常交易
        }

        // --- 调试信息：开始检查 ---
        System.out.println("TransactionChecker: 正在检查账户 " + account.getUsername() + " 的 " + transactions.size() + " 条交易记录...");

        // 4. 遍历列表中的每一笔交易
        for (TransactionModel transaction : transactions) {
            // 5. 安全性检查：确认列表中的 transaction 对象不是 null
            if (transaction == null) {
                System.err.println("TransactionChecker：在账户 " + account.getUsername() + " 的交易列表中发现 null 交易。");
                continue; // 跳过这个 null 交易，继续检查下一个
            }

            // --- 调试信息：打印当前检查的交易类型和金额 ---
            // System.out.println("  检查交易: Type=" + transaction.getType() + ", Amount=" + transaction.getAmount());

            // 6. 应用“异常”判断标准： (已修改)
            //    - 交易类型是 'Transfer Out' 或 'Transfer In' (忽略大小写)
            //    - 并且 交易金额大于 500
            String transactionType = transaction.getType(); // 获取类型
            if (transactionType != null && // 增加对 transactionType 为 null 的检查
                ("Transfer Out".equalsIgnoreCase(transactionType) || "Transfer In".equalsIgnoreCase(transactionType)) &&
                 transaction.getAmount() > 500)
            {
                // 找到一笔符合条件的异常交易
                // --- 调试信息：找到异常交易 ---
                System.out.println("TransactionChecker：为账户 " + account.getUsername()  + " (类型: " + transaction.getType() + ", 金额: " + transaction.getAmount() + ")");
                return true; // 只要找到一笔，就可以确定存在异常，无需继续检查
            }
        }

        // 7. 如果循环正常结束（遍历完所有交易），说明没有找到异常交易
        // --- 调试信息：未找到异常 ---
        System.out.println("TransactionChecker: 账户 " + account.getUsername() + " 未发现异常交易。");
        return false;
    }
}