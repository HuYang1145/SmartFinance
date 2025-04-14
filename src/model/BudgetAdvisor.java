package model;

import java.time.Month;
import java.util.List;

public class BudgetAdvisor {
    // 用户交易记录数据结构
    public static class Transaction {
        public String type; // "转入"/"转出"/"提款"
        public double amount;
        public String date; // 格式 yyyy-MM-dd
    }

    // 预算模式枚举
    public enum BudgetMode {
        NORMAL("Normal Mode", "Stable spending pattern"),
        ECONOMICAL("Economical Mode", "Unstable spending pattern"),
        SHOPPING_SEASON("Economical Mode", "Upcoming shopping season"),
        CUSTOM("Custom Mode", "User-defined budget");

        private final String displayName;
        private final String reason;

        BudgetMode(String name, String reason) {
            this.displayName = name;
            this.reason = reason;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getReason() {
            return reason;
        }
    }

    // 核心预算建议算法
    public static BudgetRecommendation calculateRecommendation(List<Transaction> transactions, Month currentMonth) {
        BudgetMode mode = BudgetMode.NORMAL;
        double suggestedBudget = 0;
        double suggestedSaving = 0;

        // 基础计算
        double totalIncome = transactions.stream()
                .filter(t -> t.type.equals("转入"))
                .mapToDouble(t -> t.amount)
                .sum();

        double avgExpense = transactions.stream()
                .filter(t -> t.type.equals("转出") || t.type.equals("提款"))
                .mapToDouble(t -> t.amount)
                .average()
                .orElse(0);

        // 模式判断逻辑
        boolean hasBigWithdrawals = transactions.stream()
                .filter(t -> t.type.equals("转出") || t.type.equals("提款"))
                .filter(t -> t.amount > avgExpense * 1.5)
                .count() >= 3;

        boolean isShoppingMonth = List.of(Month.FEBRUARY, Month.MAY, Month.OCTOBER, Month.NOVEMBER)
                .contains(currentMonth);

        // 模式优先级: 自定义 > 购物季 > 大额消费 > 正常
        if (isShoppingMonth) {
            mode = BudgetMode.SHOPPING_SEASON;
            suggestedBudget = avgExpense * 0.7;
            suggestedSaving = totalIncome - suggestedBudget;
        } else if (hasBigWithdrawals) {
            mode = BudgetMode.ECONOMICAL;
            suggestedBudget = avgExpense * 0.8;
            suggestedSaving = totalIncome - suggestedBudget;
        } else {
            suggestedBudget = avgExpense * 1.1;
            suggestedSaving = totalIncome - suggestedBudget;
        }

        return new BudgetRecommendation(mode, suggestedBudget, suggestedSaving);
    }

    // 推荐结果封装类
    public static class BudgetRecommendation {
        public final BudgetMode mode;
        public final double suggestedBudget;
        public final double suggestedSaving;

        public BudgetRecommendation(BudgetMode mode, double budget, double saving) {
            this.mode = mode;
            this.suggestedBudget = budget;
            this.suggestedSaving = saving;
        }
    }
}