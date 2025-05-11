package Controller;

import java.time.LocalDate;

import javax.swing.SwingWorker;

import Model.BudgetDataContainer;
import Service.BudgetService;
import View.BudgetAdvisor.BudgetManagementPanel;

public class BudgetManagementController {
    private final String username;
    private final BudgetManagementPanel view;
    private final BudgetService budgetService;

    public BudgetManagementController(String username, BudgetManagementPanel view, BudgetService budgetService) {
        this.username = username;
        this.view = view;
        this.budgetService = budgetService;
        if (view.isInitialized()) {
            loadBudgetData();
        }
    }

    public void loadBudgetData() {
        if (!view.isInitialized()) return;
        view.setLoadingState(true);
        SwingWorker<BudgetDataContainer, Void> worker = new SwingWorker<>() {
            @Override
            protected BudgetDataContainer doInBackground() {
                return budgetService.getBudgetData(username, LocalDate.now());
            }

            @Override
            protected void done() {
                try {
                    BudgetDataContainer data = get();
                    view.updateBudgetData(data);
                    view.setLoadingState(false);
                } catch (Exception e) {
                    view.showError("Failed to load budget data: " + e.getMessage());
                    view.setLoadingState(false);
                }
            }
        };
        worker.execute();
    }

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

    public void clearCustomBudget() {
        if (!view.isInitialized()) return;
        budgetService.clearCustomBudget(username);
        view.showMessage("Custom budget cleared. Using system recommendation.");
        loadBudgetData();
    }
}