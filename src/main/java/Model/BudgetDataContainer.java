package Model;

import java.util.List;

import Service.BudgetService;
import Service.BudgetService.BudgetRecommendation;
import Service.BudgetService.LargeConsumptionItem; // 导入 LargeConsumptionItem

/**
 * Represents a container for budget-related data in the Smart Finance Application.
 * This class encapsulates budget recommendations, monthly financial summaries, top spending categories,
 * significant consumption details, and custom budget settings for a user.
 *
 * @author Group 19
 * @version 1.0
 */
public class BudgetDataContainer {
    /** The budget recommendation for the user. */
    private final BudgetRecommendation recommendation;

    /** The total expenses for the current month. */
    private final double currentMonthExpense;

    /** The total income for the current month. */
    private final double currentMonthIncome;

    /** The category with the highest spending for the current month. */
    private final String topCategory;

    /** A list of significant consumption transactions or items for the current month. */
    private List<LargeConsumptionItem> largeConsumptions; // 使用 LargeConsumptionItem

    /** The custom budget amount set by the user, or null if not specified. */
    private final Double customBudget;

    /**
     * Constructs a BudgetDataContainer with the specified budget data.
     *
     * @param recommendation       The budget recommendation for the user.
     * @param currentMonthExpense  The total expenses for the current month.
     * @param currentMonthIncome   The total income for the current month.
     * @param topCategory         The category with the highest spending for the current month.
     * @param largeConsumptions    A list of significant consumption transactions or items.
     * @param customBudget        The custom budget amount set by the user, or null if not specified.
     */
    public BudgetDataContainer(BudgetRecommendation recommendation, double currentMonthExpense,
                               double currentMonthIncome, String topCategory, List<LargeConsumptionItem> largeConsumptions, // 使用 LargeConsumptionItem
                               Double customBudget) {
        this.recommendation = recommendation;
        this.currentMonthExpense = currentMonthExpense;
        this.currentMonthIncome = currentMonthIncome;
        this.topCategory = topCategory;
        this.largeConsumptions = largeConsumptions;
        this.customBudget = customBudget;
    }

    /**
     * Gets the budget recommendation for the user.
     *
     * @return The {@link BudgetRecommendation} object.
     */
    public BudgetRecommendation getRecommendation() {
        return recommendation;
    }

    /**
     * Gets the total expenses for the current month.
     *
     * @return The current month's expenses.
     */
    public double getCurrentMonthExpense() {
        return currentMonthExpense;
    }

    /**
     * Gets the total income for the current month.
     *
     * @return The current month's income.
     */
    public double getCurrentMonthIncome() {
        return currentMonthIncome;
    }

    /**
     * Gets the category with the highest spending for the current month.
     *
     * @return The top spending category, or null if not available.
     */
    public String getTopCategory() {
        return topCategory;
    }

    /**
     * Gets the list of significant consumption transactions or items for the current month.
     *
     * @return A list of significant consumption details.
     */
    public List<LargeConsumptionItem> getLargeConsumptions() { // 返回 LargeConsumptionItem
        return largeConsumptions;
    }

    /**
     * Gets the custom budget amount set by the user.
     *
     * @return The custom budget amount, or null if not specified.
     */
    public Double getCustomBudget() {
        return customBudget;
    }
}