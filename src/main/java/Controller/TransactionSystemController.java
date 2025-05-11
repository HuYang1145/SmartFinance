package Controller;

import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import Model.Transaction;
import Model.TransactionCache;
import Model.User;
import Model.UserSession;
import Service.ExchangeRateService;
import View.Transaction.TransactionSystemComponents;
import View.Transaction.TransactionSystemPlane;

/**
 * Controller class for managing the transaction system panel in a financial management application.
 * It handles user interactions with the transaction interface, including adding transactions,
 * converting currencies, updating exchange rates, and displaying historical exchange rate trends.
 * The controller integrates with the exchange rate service and transaction controller to perform
 * these operations and updates the view accordingly.
 *
 * @author Group 19
 * @version 1.0
 */
public class TransactionSystemController {
    /** The view component for the transaction system panel. */
    private final TransactionSystemPlane view;

    /** Service for fetching and managing exchange rates. */
    private final ExchangeRateService exchangeRateService;

    /** Controller for handling transaction-related operations. */
    private final TransactionController transactionController;

    /** The username of the current user. */
    private final String username;

    /** Timer for periodically updating exchange rates. */
    private Timer rateUpdateTimer;

    /** Timer for updating the countdown display for the next rate refresh. */
    private Timer countdownTimer;

    /** Timestamp for the next scheduled exchange rate refresh. */
    private long nextRefreshTime;

    /** Mock historical exchange rates for CNY to other currencies (2020-2024). */
    private final Map<String, Map<String, Double>> mockHistoricalRates = new HashMap<>();

    /**
     * Constructs a TransactionSystemController with the specified view, services, and username.
     * Initializes mock historical rates, listeners, exchange rates, and timers for rate updates.
     *
     * @param view                  The transaction system view to control.
     * @param exchangeRateService   The service for fetching exchange rates.
     * @param transactionController The controller for transaction operations.
     * @param username              The username of the current user.
     */
    public TransactionSystemController(TransactionSystemPlane view, ExchangeRateService exchangeRateService,
                                       TransactionController transactionController, String username) {
        this.view = view;
        this.exchangeRateService = exchangeRateService;
        this.transactionController = transactionController;
        this.username = username;

        System.out.println("TransactionSystemController initialized for user: " + username);
        initializeMockHistoricalRates();
        initializeListeners();
        System.out.println("Fetching exchange rates...");
        fetchExchangeRates();
        System.out.println("Updating historical rates with mock data...");
        updateHistoricalTrend();
        startRateUpdateTimer();
    }

    /**
     * Initializes mock historical exchange rates for CNY to various currencies (2020-2024).
     * Populates the mockHistoricalRates map with approximate yearly rates for testing purposes.
     */
    private void initializeMockHistoricalRates() {
        // Mock historical exchange rates for CNY to USD, EUR, GBP, JPY, AUD, CAD, CHF, HKD, SGD, NZD (2020-2024)
        // Data sourced from historical trends (approximated for simplicity)
        String[] dates = {
                "2020-12-31", "2021-12-31", "2022-12-31", "2023-12-31", "2024-12-31"
        };
        double[] usdRates = {6.52, 6.37, 6.96, 7.10, 7.28};
        double[] eurRates = {7.98, 7.22, 7.42, 7.85, 7.65};
        double[] gbpRates = {8.89, 8.60, 8.37, 9.02, 9.20};
        double[] jpyRates = {0.063, 0.055, 0.053, 0.050, 0.049};
        double[] audRates = {5.02, 4.62, 4.73, 4.82, 4.90};
        double[] cadRates = {5.11, 5.03, 5.14, 5.35, 5.40};
        double[] chfRates = {7.37, 6.98, 7.32, 8.40, 8.30};
        double[] hkdRates = {0.84, 0.82, 0.89, 0.91, 0.93};
        double[] sgdRates = {4.92, 4.72, 5.18, 5.36, 5.45};
        double[] nzdRates = {4.68, 4.35, 4.41, 4.48, 4.55};

        for (int i = 0; i < dates.length; i++) {
            Map<String, Double> rates = new HashMap<>();
            rates.put("USD", usdRates[i]);
            rates.put("EUR", eurRates[i]);
            rates.put("GBP", gbpRates[i]);
            rates.put("JPY", jpyRates[i]);
            rates.put("AUD", audRates[i]);
            rates.put("CAD", cadRates[i]);
            rates.put("CHF", chfRates[i]);
            rates.put("HKD", hkdRates[i]);
            rates.put("SGD", sgdRates[i]);
            rates.put("NZD", nzdRates[i]);
            rates.put("CNY", 1.0);
            mockHistoricalRates.put(dates[i], rates);
        }
    }

    /**
     * Initializes listeners for user interactions, including transaction submission and currency conversion.
     * Sets up action listeners for adding transactions and document listeners for real-time currency conversion updates.
     */
    private void initializeListeners() {
        // Transaction Action Listener (Income/Expense)
        view.setTransactionActionListener(e -> {
            String operation = (String) view.getOperationComboBox().getSelectedItem();
            String amountText = view.getTransactionAmountField().getActualText();
            String timeText = view.getTransactionTimeField().getActualText();
            String merchantText = view.getMerchantField().getActualText();
            String type = (String) view.getTypeComboBox().getSelectedItem();
            String remark = view.getRemarkField().getActualText();
            String category = view.getCategoryField().getActualText();
            String paymentMethod = view.getPaymentMethodField().getActualText();
            String location = (String) view.getLocationComboBox().getSelectedItem();
            String tag = view.getTagField().getActualText();
            String attachment = view.getAttachmentField().getActualText();
            String recurrence = view.getRecurrenceField().getActualText();
            String password = new String(view.getPasswordField().getPassword());

            // Validation
            if (operation == null || amountText.isEmpty() || timeText.isEmpty() || merchantText.isEmpty() || type == null || password.isEmpty()) {
                view.showError("Operation, Amount, Time, Merchant, Type, and Password must be filled.");
                return;
            }
            if ("I".equalsIgnoreCase(merchantText.trim()) || "I".equalsIgnoreCase(type.trim())) {
                view.showError("Invalid merchant name or type.");
                return;
            }
            if (!merchantText.matches("[a-zA-Z0-9\\s]+")) {
                view.showError("Merchant name must be in English.");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    view.showError("Amount must be positive.");
                    return;
                }
            } catch (NumberFormatException ex) {
                view.showError("Invalid amount format.");
                return;
            }

            // Verify password
            User account = UserSession.getCurrentAccount();
            if (account == null || !account.getPassword().equals(password)) {
                view.showError("Incorrect password.");
                return;
            }

            // Add transaction
            boolean transactionAdded = transactionController.addTransaction(
                    account.getUsername(), operation, amount, timeText.trim(), merchantText.trim(), type,
                    remark, category, paymentMethod, location, tag, attachment, recurrence
            );

            if (transactionAdded) {
                view.showSuccess(operation + " of ¥" + String.format("%.2f", amount) + " added successfully!");
                TransactionCache.invalidateCache(username);
                // Refresh transaction history table
                DefaultTableModel tableModel = (DefaultTableModel) view.getTransactionTable().getModel();
                tableModel.setRowCount(0);
                List<Transaction> transactions = TransactionController.readTransactions(username);
                for (Transaction tx : transactions) {
                    tableModel.addRow(new Object[]{
                            tx.getOperation(),
                            tx.getAmount(),
                            tx.getTimestamp(),
                            tx.getMerchant(),
                            tx.getType(),
                            tx.getRemark()
                    });
                }
            } else {
                view.showError("Failed to add " + operation.toLowerCase() + ".");
            }
        });

        // Currency Conversion Listener
        view.getAmountField().getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateConversion(); }
            public void removeUpdate(DocumentEvent e) { updateConversion(); }
            public void changedUpdate(DocumentEvent e) { updateConversion(); }
        });

        view.getCurrencyComboBox().addActionListener(e -> {
            updateConversion();
            updateHistoricalTrend();
        });
    }

    /**
     * Fetches exchange rates from the exchange rate service and updates the rate table and conversion display.
     * Falls back to mock data if the fetch fails.
     */
    private void fetchExchangeRates() {
        System.out.println("Calling fetchExchangeRates...");
        exchangeRateService.fetchExchangeRates(rates -> {
            System.out.println("Fetched exchange rates: " + rates);
            updateRateTable(rates);
            updateConversion();
        }, error -> {
            System.err.println("Failed to fetch exchange rates: " + error);
            view.showError("Failed to fetch exchange rates: " + error);
            // Use mock data for testing
            Map<String, Double> mockRates = new HashMap<>();
            mockRates.put("USD", 7.1);
            mockRates.put("EUR", 7.8);
            mockRates.put("GBP", 8.9);
            mockRates.put("JPY", 0.05);
            mockRates.put("AUD", 4.8);
            mockRates.put("CAD", 5.2);
            mockRates.put("CHF", 8.1);
            mockRates.put("HKD", 0.91);
            mockRates.put("SGD", 5.4);
            mockRates.put("NZD", 4.3);
            mockRates.put("CNY", 1.0);
            updateRateTable(mockRates);
            updateConversion();
        });
    }

    /**
     * Starts timers for periodic exchange rate updates and countdown display.
     * The rate update timer fetches new rates every 60 seconds, and the countdown timer
     * updates the refresh countdown every second.
     */
    private void startRateUpdateTimer() {
        nextRefreshTime = System.currentTimeMillis() + 60000;
        rateUpdateTimer = new Timer(60000, e -> {
            System.out.println("Timer triggered: fetching exchange rates...");
            fetchExchangeRates();
            nextRefreshTime = System.currentTimeMillis() + 60000;
        });
        rateUpdateTimer.start();

        countdownTimer = new Timer(1000, e -> {
            long remaining = (nextRefreshTime - System.currentTimeMillis()) / 1000;
            if (remaining >= 0) {
                view.getCountdownLabel().setText("Next refresh in " + remaining + " seconds");
            } else {
                view.getCountdownLabel().setText("Refreshing...");
            }
        });
        countdownTimer.start();
    }

    /**
     * Updates the exchange rate table with the provided rates.
     *
     * @param rates A map of currency codes to their exchange rates relative to CNY.
     */
    private void updateRateTable(Map<String, Double> rates) {
        String[] currencies = view.getCurrencyComboBox().getModel().getSize() > 0 ?
                new String[view.getCurrencyComboBox().getModel().getSize()] : new String[0];
        for (int i = 0; i < currencies.length; i++) {
            currencies[i] = view.getCurrencyComboBox().getItemAt(i);
        }

        for (int i = 0, j = 0; i < currencies.length; i++) {
            if (!currencies[i].equals("CNY")) {
                Double rate = rates.getOrDefault(currencies[i], 0.0);
                view.getRateTable().setValueAt(String.format("%.4f", rate), j, 1);
                j++;
            }
        }
        view.revalidate();
        view.repaint();
        System.out.println("Updated rate table with rates: " + rates);
    }

    /**
     * Updates the currency conversion result based on the entered amount and selected currency.
     * Displays an error if the amount format is invalid.
     */
    private void updateConversion() {
        String amountText = view.getAmountField().getActualText();
        String targetCurrency = (String) view.getCurrencyComboBox().getSelectedItem();
        try {
            double amount = Double.parseDouble(amountText);
            Double rate = exchangeRateService.getExchangeRates().getOrDefault(targetCurrency, 0.0);
            double result = amount * rate;
            view.getConversionResultLabel().setText(String.format("Result: %.2f %s", result, targetCurrency));
            System.out.println("Updated conversion: " + amount + " CNY = " + result + " " + targetCurrency);
        } catch (NumberFormatException e) {
            view.getConversionResultLabel().setText("Result: -");
            System.out.println("Conversion failed: invalid amount format");
        }
    }

    /**
     * Updates the historical exchange rate trend chart based on the selected currency.
     * Uses mock historical rates (2020-2024) to populate the chart.
     */
    private void updateHistoricalTrend() {
        JPanel historicalPanel = null;
        for (Component comp : view.getComponents()) {
            if (comp instanceof TransactionSystemComponents.MidGradientPanel) {
                JPanel panel = (JPanel) comp;
                if (panel.getComponentCount() > 0 && panel.getComponent(0) instanceof javax.swing.JLabel &&
                        ((javax.swing.JLabel) panel.getComponent(0)).getText().equals("Historical Exchange Rate Trend")) {
                    historicalPanel = panel;
                    break;
                }
            }
        }
        if (historicalPanel == null) {
            System.err.println("Historical trend panel not found.");
            return;
        }

        org.jfree.chart.ChartPanel chartPanel = (org.jfree.chart.ChartPanel) historicalPanel.getComponent(1);
        org.jfree.chart.JFreeChart chart = chartPanel.getChart();
        org.jfree.data.time.TimeSeriesCollection dataset = (org.jfree.data.time.TimeSeriesCollection) chart.getXYPlot().getDataset();
        org.jfree.data.time.TimeSeries series = dataset.getSeries(0);
        series.clear();

        String selectedCurrency = (String) view.getCurrencyComboBox().getSelectedItem();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Calendar cal = java.util.Calendar.getInstance();

        // Use mock historical rates (2020-2024)
        String[] dates = {"2020-12-31", "2021-12-31", "2022-12-31", "2023-12-31", "2024-12-31"};
        for (String date : dates) {
            Map<String, Double> monthlyRates = mockHistoricalRates.get(date);
            Double rate = monthlyRates.getOrDefault(selectedCurrency, 0.0);
            if (rate > 0) {
                try {
                    cal.setTime(sdf.parse(date));
                    series.add(new org.jfree.data.time.Month(cal.getTime()), rate);
                    System.out.println("Added historical rate for " + selectedCurrency + " on " + date + ": " + rate);
                } catch (Exception e) {
                    System.err.println("Failed to parse date " + date + ": " + e.getMessage());
                }
            } else {
                System.out.println("No rate data for " + selectedCurrency + " on " + date);
            }
        }

        dataset.removeAllSeries();
        dataset.addSeries(series);
        chartPanel.repaint();
        chartPanel.revalidate();
        historicalPanel.repaint();
        view.revalidate();
        view.repaint();
        System.out.println("Historical trend chart updated for " + selectedCurrency);
    }
}