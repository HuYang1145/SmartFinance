package Controller;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import Model.UserSession;
import Service.PersonChartDataService;
import Service.PersonFinancialService;
import View.PersonalCenter.PersonalCenterPanel;

/**
 * Controller class for managing the Personal Center panel in a financial management application.
 * It handles loading and updating financial data, including summaries, payment locations, and chart data,
 * for a specific user and year, and communicates with the associated view to update the UI.
 *
 * @author Group 19
 * @version 1.0
 */
public class PersonCenterController {
    private final PersonFinancialService financialService;
    private final PersonChartDataService chartDataService;
    private PersonalCenterPanel view;
    private boolean isDataLoadedSuccessfully = false;

    /**
     * Constructs a PersonCenterController with the specified financial and chart data services.
     *
     * @param financialService The service responsible for calculating financial summaries and payment location data.
     * @param chartDataService The service responsible for preparing chart data for annual and category visualizations.
     */
    public PersonCenterController(PersonFinancialService financialService, PersonChartDataService chartDataService) {
        this.financialService = financialService;
        this.chartDataService = chartDataService;
    }

    /**
     * Sets the view associated with this controller.
     *
     * @param view The PersonalCenterPanel instance to be updated with financial data.
     */
    public void setView(PersonalCenterPanel view) {
        this.view = view;
    }

    /**
     * Loads financial data for the specified year and updates the associated view.
     * The method retrieves the current user's financial summary, payment location summary,
     * and chart data, then updates the UI on the Event Dispatch Thread (EDT).
     * If the user is not logged in or an error occurs, an error message is displayed.
     *
     * @param selectedYear The year for which to load financial data.
     */
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

    /**
     * Checks whether the last data loading operation was successful.
     *
     * @return {@code true} if the data was loaded successfully, {@code false} otherwise.
     */
    public boolean isDataLoadedSuccessfully() {
        return isDataLoadedSuccessfully;
    }
}