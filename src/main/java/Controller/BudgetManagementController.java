package Controller;

import java.time.LocalDate;
import java.util.List;
import javax.swing.SwingWorker;

import Model.BudgetDataContainer;
import Service.BudgetService;
import View.BudgetAdvisor.BudgetManagementPanel;

/**
 * Manages budget-related operations for the finance management system, including loading budget data,
 * saving custom budgets, and clearing custom budgets. Interacts with the BudgetManagementPanel for UI
 * updates and the BudgetService for data processing.
 *
 * @author Group 19
 * @version 1.0
 */
public class BudgetManagementController {
    /** Username of the current user. */
    private final String username;
    /** Budget management panel for displaying UI components. */
    private final BudgetManagementPanel view;
    /** Service for handling budget data operations. */
    private final BudgetService budgetService;

    /**
     * Constructs a BudgetManagementController with the specified username, view, and budget service.
     * Loads budget data if the view is initialized.
     *
     * @param username      the username of the current user
     * @param view          the budget management panel for UI interaction
     * @param budgetService the service for budget data operations
     */
    public BudgetManagementController(String username, BudgetManagementPanel view, BudgetService budgetService) {
        this.username = username;
        this.view = view;
        this.budgetService = budgetService;
        if (view.isInitialized()) {
            loadBudgetData();
        }
    }

    /**
     * Loads budget data for the current user and updates the view asynchronously.
     * Displays a loading state during data retrieval and handles errors if data loading fails.
     */
    /**
     * Loads budget data and chart data for the current user and updates the view asynchronously.
     * Displays a loading state during data retrieval and handles errors if data loading fails.
     */
    public void loadBudgetData() {
        if (!view.isInitialized()) return;
        view.setLoadingState(true);
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private BudgetDataContainer budgetData;
            private List<BudgetService.MonthlyFinancialData> pastMonthsData;

            @Override
            protected Void doInBackground() throws Exception {
                LocalDate now = LocalDate.now();
                budgetData = budgetService.getBudgetData(username, now);
                pastMonthsData = budgetService.getPastThreeMonthsFinancialData(username, now);
                return null;
            }

            @Override
            protected void done() {
                view.setLoadingState(false);
                try {
                    get(); // Check for any exceptions during doInBackground
                    view.updateBudgetData(budgetData);
                    if (pastMonthsData != null && !pastMonthsData.isEmpty()) {
                        view.updateChartData(pastMonthsData);
                    }
                } catch (Exception e) {
                    view.showError("Failed to load budget data: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    /**
     * Saves a custom budget amount for the current user after validating the input.
     * Updates the view with the new budget and reloads budget data.
     *
     * @param budgetInput the budget amount as a string
     */
    public void saveCustomBudget(String budgetInput) {
        if (!view.isInitialized()) return;
        try {
            double budget = Double.parseDouble(budgetInput.trim());
            if (budget >= 0) {
                budgetService.saveCustomBudget(username, budget);
                view.showMessage("Custom budget set to ¥" + String.format("%.2f", budget));
                loadBudgetData();
            } else {
                view.showError("Budget cannot be negative.");
            }
        } catch (NumberFormatException e) {
            view.showError("Invalid number format.");
        }
    }

    /**
     * Clears the custom budget for the current user, reverting to the system-recommended budget.
     * Updates the view and reloads budget data.
     */
    public void clearCustomBudget() {
        if (!view.isInitialized()) return;
        budgetService.clearCustomBudget(username);
        view.showMessage("Custom budget cleared. Using system recommendation.");
        loadBudgetData();
    }
}