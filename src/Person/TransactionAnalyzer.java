package Person;

import model.Transaction;
import java.util.List;

public class TransactionAnalyzer {
    // 检测游戏充值超过500的异常交易
    public List<Transaction> detectAbnormal(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getType().equals("转出") && t.getAmount() > 500)
                .toList();
    }

    // 预算提醒逻辑（返回英文文本以匹配图片）
    public String checkBudget(double totalSpent, double monthlyBudget) {
        double remaining = monthlyBudget - totalSpent;
        if (remaining <= 0) return "Budget exceeded!";
        return remaining < 1000 ?
                String.format("$%.2f left this month", remaining) : null;
    }
}