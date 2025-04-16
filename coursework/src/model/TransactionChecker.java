package model;

import java.util.List;

public class TransactionChecker {

    /**
     * 检测账户是否存在异常交易（转账金额 > 500）
     * 
     * @param account 需要检测的账户对象
     * @return 是否存在异常交易
     */
    public static boolean hasAbnormalTransactions(AccountModel account) {
        List<TransactionModel> transactions = account.getTransactions(); // 假设账户包含交易记录
        
        if (transactions == null) {
            return false; // 如果没有交易记录，直接返回正常
        }

        for (TransactionModel transaction : transactions) {
            if ("transfer".equals(transaction.getType()) && transaction.getAmount() > 500) {
                return true; // 存在异常交易
            }
        }
        return false; // 没有异常交易
    }
}