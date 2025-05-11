package Model;

import java.util.List;

import Service.BudgetService.BudgetRecommendation;

public class BudgetDataContainer {
    private final BudgetRecommendation recommendation;
    private final double currentMonthExpense;
    private final double currentMonthIncome;
    private final String topCategory;
    private final List<String> largeConsumptions;
    private final Double customBudget;

    public BudgetDataContainer(BudgetRecommendation recommendation, double currentMonthExpense,
                              double currentMonthIncome, String topCategory, List<String> largeConsumptions,
                              Double customBudget) {
        this.recommendation = recommendation;
        this.currentMonthExpense = currentMonthExpense;
        this.currentMonthIncome = currentMonthIncome;
        this.topCategory = topCategory;
        this.largeConsumptions = largeConsumptions;
        this.customBudget = customBudget;
    }

    public BudgetRecommendation getRecommendation() {
        return recommendation;
    }

    public double getCurrentMonthExpense() {
        return currentMonthExpense;
    }

    public double getCurrentMonthIncome() {
        return currentMonthIncome;
    }

    public String getTopCategory() {
        return topCategory;
    }

    public List<String> getLargeConsumptions() {
        return largeConsumptions;
    }

    public Double getCustomBudget() {
        return customBudget;
    }
}