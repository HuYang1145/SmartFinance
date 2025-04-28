package Controller;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import Model.UserSession;
import Service.PersonChartDataService;
import Service.PersonFinancialService;
import View.PersonalCenter.PersonalCenterPanel;

public class PersonCenterController {
    private final PersonFinancialService financialService;
    private final PersonChartDataService chartDataService;
    private PersonalCenterPanel view;
    private boolean isDataLoadedSuccessfully = false;

    public PersonCenterController(PersonFinancialService financialService, PersonChartDataService chartDataService) {
        this.financialService = financialService;
        this.chartDataService = chartDataService;
    }

    public void setView(PersonalCenterPanel view) {
        this.view = view;
    }

    public void loadData(int selectedYear) {
        String username = UserSession.getCurrentUsername();
        if (username == null) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, "Please log in first!", "Error", JOptionPane.ERROR_MESSAGE);
            });
            return;
        }
        System.out.println("Loading data for user: " + username + ", year: " + selectedYear);

        try {
            PersonFinancialService.FinancialSummary summary = financialService.calculateFinancialSummary(username, selectedYear);
            String paymentLocationSummary = financialService.generatePaymentLocationSummary(username, selectedYear);
            PersonChartDataService.AnnualChartData annualChartData = chartDataService.prepareAnnualChartData(username, selectedYear);
            PersonChartDataService.CategoryChartData categoryChartData = chartDataService.prepareCategoryChartData(username, selectedYear);

            System.out.println("Financial Summary - Income: " + summary.getTotalIncomeYear() + ", Expense: " + summary.getTotalExpenseYear());
            System.out.println("Annual Chart Data - Income: " + annualChartData.getAnnualIncome() + ", Expense: " + annualChartData.getAnnualExpense());
            System.out.println("Category Chart Data - Income Categories: " + categoryChartData.getIncomeCategories().size() + ", Expense Categories: " + categoryChartData.getExpenseCategories().size());

            SwingUtilities.invokeLater(() -> {
                if (view != null) {
                    view.updateFinancialSummary(summary);
                    view.updatePaymentLocationSummary(paymentLocationSummary);
                    view.updateAnnualChartData(annualChartData);
                    view.updateCategoryChartData(categoryChartData);
                    isDataLoadedSuccessfully = true;
                    System.out.println("Data updated in UI for PersonalCenterPanel");
                } else {
                    System.err.println("View is null, cannot update UI");
                }
            });
        } catch (Exception e) {
            System.err.println("Failed to load data: " + e.getMessage());
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, "Failed to load data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            });
            isDataLoadedSuccessfully = false;
        }
    }

    public boolean isDataLoadedSuccessfully() {
        return isDataLoadedSuccessfully;
    }
}