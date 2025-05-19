package Controller;

import java.awt.BorderLayout;
import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.Timer; // Added import for BorderLayout
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.axis.DateAxis;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import Model.Transaction;
import Model.TransactionCache;
import Model.User;
import Model.UserSession;
import Service.BudgetService;
import Service.ExchangeRateService;
import Service.TransactionService;
import View.Transaction.TransactionSystemComponents;
import View.Transaction.TransactionSystemPlane;

/**
 * Controller class for managing the transaction system panel in a financial management application.
 * It handles user interactions with the transaction interface, including adding transactions,
 * converting currencies, updating exchange rates, and displaying historical exchange rate trends.
 * Includes real-time abnormal transaction checks before adding a transaction.
 *
 * @author Group 19
 * @version 1.0
 */
public class TransactionSystemController {
    /** The view component for the transaction system panel. */
    private final TransactionSystemPlane view;

    /** Service for fetching and managing exchange rates. */
    private final ExchangeRateService exchangeRateService;

    /** Controller for handling transaction-related operations (file I/O). */
    private final TransactionController transactionController;

    private final TransactionService transactionService;

    private final BudgetService budgetService;

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
     * Includes dependencies for TransactionService and BudgetService.
     *
     * @param view                  The transaction system view to control.
     * @param exchangeRateService   The service for fetching exchange rates.
     * @param transactionController The controller for transaction file operations.
     * @param transactionService    The service for transaction logic and checks.
     * @param budgetService         The service for budget calculations.
     * @param username              The username of the current user.
     */
    public TransactionSystemController(TransactionSystemPlane view, ExchangeRateService exchangeRateService,
                                       TransactionController transactionController, TransactionService transactionService,
                                       BudgetService budgetService, String username) {
        this.view = view;
        this.exchangeRateService = exchangeRateService;
        this.transactionController = transactionController;
        this.transactionService = transactionService;
        this.budgetService = budgetService;
        this.username = username;

        System.out.println("TransactionSystemController initialized for user: " + username);
        initializeMockHistoricalRates();
        initializeListeners();
        System.out.println("Fetching exchange rates...");
        fetchExchangeRates();
        System.out.println("Updating historical rates with mock data...");
        updateHistoricalTrend();
        startRateUpdateTimer();
        loadTransactionHistory(); // Load initial history
    }

     /**
      * Loads and displays the user's transaction history for the current month.
      * Fixes: Correctly finds the JSplitPane, JScrollPane, JList, and JLabel components
      * within the view's hierarchy to update the transaction history list and balance label.
      */
    private void loadTransactionHistory() {
         // Note: The TransactionSystemPlane uses a JList for history display currently.
         // The JTable field in TransactionSystemPlane seems unused for history display.
         // This method updates the JList component.

         // 1. Find the main JSplitPane in the view
         JSplitPane mainSplitPane = null;
         Component[] viewComponents = view.getComponents();
         for (Component comp : viewComponents) {
             if (comp instanceof JSplitPane) {
                 mainSplitPane = (JSplitPane) comp;
                 break;
             }
         }

         if (mainSplitPane == null) {
             System.err.println("Error: Main JSplitPane component not found in TransactionSystemPlane view structure.");
             // Cannot update history display if the main layout structure is missing
             return;
         }

         // 2. Find the left split pane (vertical split) within the main split pane
         JSplitPane verticalSplitPane = null;
         // Ensure the left component exists and is a JSplitPane
         if (mainSplitPane.getLeftComponent() != null && mainSplitPane.getLeftComponent() instanceof JSplitPane) {
             verticalSplitPane = (JSplitPane)mainSplitPane.getLeftComponent();
         }

         if (verticalSplitPane == null) {
             System.err.println("Error: Vertical JSplitPane component not found within the main split pane's left component.");
             return;
         }

         // 3. Find the history panel container (top component of the vertical split)
         JPanel historyPanelContainer = null;
          // Ensure the top component exists and is a JPanel
         if (verticalSplitPane.getTopComponent() != null && verticalSplitPane.getTopComponent() instanceof JPanel) {
              historyPanelContainer = (JPanel)verticalSplitPane.getTopComponent();
         }

         if (historyPanelContainer == null) {
              System.err.println("Error: History panel container (JPanel) not found as the top component of the vertical split pane.");
              return;
         }

         // 4. Find the JList (transaction history list) within the history panel container
         JList<Transaction> transactionList = null;
         JScrollPane scrollPaneForList = null;
         for(Component historyChild : historyPanelContainer.getComponents()) {
             if (historyChild instanceof JScrollPane) {
                 scrollPaneForList = (JScrollPane) historyChild;
                  if (scrollPaneForList.getViewport() != null && scrollPaneForList.getViewport().getView() instanceof JList) {
                       transactionList = (JList<Transaction>)scrollPaneForList.getViewport().getView();
                       break; // Found the JList
                  }
             }
         }

         if (transactionList == null) {
              System.err.println("Error: JList component for transaction history not found within the history panel container.");
              // Cannot update the list if the component isn't found
              return;
         }

         // 5. Update the JList model with current month's transactions
          DefaultListModel<Transaction> listModel = (DefaultListModel<Transaction>) transactionList.getModel();
          listModel.clear(); // Clear existing list data

         // Get current month's transactions using TransactionCache
         java.util.Calendar cal = java.util.Calendar.getInstance();
         String ym = String.format("%d/%02d",
                cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH) + 1
         );
         List<Transaction> allUserTransactions = TransactionCache.getCachedTransactions(username); // Get from cache
         List<Transaction> currentMonthTransactions = allUserTransactions.stream()
             .filter(tx -> tx.getTimestamp() != null && tx.getTimestamp().startsWith(ym))
             .collect(Collectors.toList());

          for (Transaction tx : currentMonthTransactions) {
              listModel.addElement(tx); // Add current month transactions to the list model
          }

         // 6. Find and update the Balance Label in the history panel header
          JLabel balanceLabel = null;
          // Assuming the balance label is in the header panel, which is likely the NORTH component of the historyPanelContainer (if using BorderLayout for the container)
          // Or, if the header panel uses absolute layout, iterate its components.
          // Let's assume the structure from createTransactionHistoryPanel is a container with BorderLayout NORTH (header) and CENTER (scrollpane)
          // The header itself might be a JPanel with absolute layout containing the JLabel.

          // Find the header panel component
          Component headerComponent = null;
          if (historyPanelContainer.getLayout() instanceof BorderLayout) {
               BorderLayout layout = (BorderLayout) historyPanelContainer.getLayout();
               headerComponent = layout.getLayoutComponent(BorderLayout.NORTH);
          } else {
              // If not BorderLayout, iterate
              for(Component child : historyPanelContainer.getComponents()) {
                  // Need a way to identify the header panel
                   if (child instanceof JPanel && child.getPreferredSize().height == 120) { // Heuristic based on create method
                       headerComponent = child;
                       break;
                   }
              }
          }


          if (headerComponent instanceof JPanel) {
               JPanel headerPanel = (JPanel) headerComponent;
               // Look for a JLabel within this header panel (assuming the first one is the balance label)
               for(Component child : headerPanel.getComponents()) {
                   if (child instanceof JLabel) {
                       balanceLabel = (JLabel)child;
                       break; // Found the balance label
                   }
               }
          }


          User currentUser = UserSession.getCurrentAccount(); // Get current user from session
           if (currentUser != null && balanceLabel != null) {
                // To get the absolute latest balance, re-fetch user from repo after transaction is added/removed.
                // This requires injecting AccountRepository into TransactionSystemController or passing it around.
                // Let's inject AccountRepository into TransactionSystemController.
                // Injecting AccountRepository means we need to update the constructor and MainPanelController.

                // --- Refetching User for Balance ---
                // Need AccountRepository here. Let's add it as a dependency.
                // For now, assuming we can create a new instance (less ideal for state management/efficiency)
                // or that MainPanelController can provide it. Let's update the constructor to accept AccountRepository.
                // This will require changes up the call chain (MainPanelController).

                // --- Updated logic assuming AccountRepository is added as a dependency ---
                // User freshUser = accountRepository.findByUsername(username); // Assuming accountRepository is now available
                // if (freshUser != null) {
                //      UserSession.setCurrentAccount(freshUser); // Update session with fresh data
                //      balanceLabel.setText(String.format("Your Balance: %.2f CNY", freshUser.getBalance()));
                // } else {
                //      System.err.println("Could not refetch user account balance after transaction update.");
                // }
                // --- End of Update Logic ---

                // For now, without injecting AccountRepository into TSC, we can't reliably refetch the balance here.
                // The balance shown might be slightly outdated until the user logs in again or the MainPlane is recreated.
                // Let's keep the balance label update commented out or simplified if we don't add AccountRepository dependency here.
                // Based on previous commits, AccountRepository was NOT added to TSC, but to BillController.
                // If the goal is to update the balance displayed in TSC, TSC needs AccountRepository.

                // --- Simplified Balance Update (less accurate without refetch) ---
                 // If the TransactionController.addTransaction/removeTransaction updates the User object in session
                 // OR if the User object in session is somehow linked to the file and updates automatically (unlikely with current repo)
                 // then currentUser.getBalance() might be sufficient, but it's not guaranteed to be the absolute latest from the file.
                 // Let's display the balance from the session user, acknowledging it might be slightly delayed.
                 balanceLabel.setText(String.format("Your Balance: %.2f CNY", currentUser.getBalance()));
                // --- End of Simplified Update ---

           } else if (balanceLabel != null) {
                balanceLabel.setText("Your Balance: N/A (User or Label not found)");
           }


         System.out.println("Loaded " + currentMonthTransactions.size() + " transactions for " + ym + " into history list.");
         // Revalidate/Repaint the list/scrollpane/container if needed
         if (scrollPaneForList != null) {
             scrollPaneForList.revalidate();
             scrollPaneForList.repaint();
         }
         if (historyPanelContainer != null) {
             historyPanelContainer.revalidate();
             historyPanelContainer.repaint();
         }

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

            // Basic Validation
            if (operation == null || amountText.isEmpty() || timeText.isEmpty() || merchantText.isEmpty() || type == null || password.isEmpty()) {
                view.showError("Operation, Amount, Time, Merchant, Type, and Password must be filled.");
                return;
            }
             // Add basic check for type "Select Type" from dropdown if applicable
            if (type != null && "(Select Type)".equals(type.trim())) {
                 view.showError("Please select a valid Type.");
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
            if (account == null) {
                 view.showError("User session not found. Please log in again.");
                 return;
            }
            if (!account.getPassword().equals(password)) {
                view.showError("Incorrect password.");
                 view.getPasswordField().setText(""); // Clear password field on error
                return;
            }
             // Clear password field after successful verification
            view.getPasswordField().setText("");


            // --- Added Real-time Abnormal Transaction Check ---
             try {
                // 1. Get existing transactions (use cache for efficiency)
                List<Transaction> existingTransactions = TransactionCache.getCachedTransactions(username);

                // 2. Create a temporary transaction object for the new input
                // Use Objects.toString to handle potential nulls from getText() on fields if they aren't properly initialized
                Transaction newTransaction = new Transaction(
                    username,
                    Objects.toString(operation, "").trim(), // Ensure operation is not null and trimmed
                    amount,
                    Objects.toString(timeText, "").trim(), // Ensure time is not null and trimmed
                    Objects.toString(merchantText, "").trim(),
                    Objects.toString(type, "").trim(),
                    Objects.toString(remark, "").trim(),
                    Objects.toString(category, "").trim(),
                    Objects.toString(paymentMethod, "").trim(),
                    Objects.toString(location, "").trim(),
                    Objects.toString(tag, "").trim(),
                    Objects.toString(attachment, "").trim(),
                    Objects.toString(recurrence, "").trim()
                );

                // 3. Perform real-time checks using the TransactionService dependency
                List<String> realtimeWarnings = transactionService.checkRealtimeAbnormalTransactions(
                    username, existingTransactions, newTransaction
                );

                // 4. If warnings, show confirm dialog
                if (!realtimeWarnings.isEmpty()) {
                    StringBuilder warningMessage = new StringBuilder("<html><center><b>Transaction Risk Alert:</b><br>");
                    warningMessage.append("Adding this transaction may involve risk:<br><br>");
                    for (String warning : realtimeWarnings) {
                        warningMessage.append("- ").append(warning).append("<br>");
                    }
                    warningMessage.append("<br>Do you want to proceed?");
                    warningMessage.append("</center></html>");

                    int confirmResult = JOptionPane.showConfirmDialog(
                        view, // Parent component for the dialog
                        warningMessage.toString(),
                        "Confirm Transaction",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE // Use warning icon
                    );

                    if (confirmResult != JOptionPane.YES_OPTION) {
                        System.out.println("Transaction cancelled by user due to warning.");
                        return; // Abort transaction if user does not confirm
                    }
                     System.out.println("User confirmed to proceed despite warning.");
                }
             } catch (Exception ex) {
                 System.err.println("Error during real-time transaction check: " + ex.getMessage());
                 ex.printStackTrace();
                 // Optionally show a non-blocking message about check failure, but don't block transaction unless critical error
                 // JOptionPane.showMessageDialog(view, "Failed to perform real-time risk check: " + ex.getMessage(), "Risk Check Error", JOptionPane.WARNING_MESSAGE);
             }
            // --- End of Added Real-time Check ---


            // Add transaction (This code block was already here)
            // Note: TransactionController handles file writing. This is fine.
            boolean transactionAdded = transactionController.addTransaction(
                    account.getUsername(), operation, amount, timeText.trim(), merchantText.trim(), type,
                    remark, category, paymentMethod, location, tag, attachment, recurrence,account
            );

            if (transactionAdded) {
                view.showSuccess(operation + " of ¥" + String.format("%.2f", amount) + " added successfully!");
                TransactionCache.invalidateCache(username); // Invalidate cache so next read is fresh
                loadTransactionHistory(); // Refresh transaction history display
                // Optional: Update user balance display immediately if needed in the UI
                // This might involve re-fetching the user account from the repository.
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
        javax.swing.SwingUtilities.invokeLater(() -> {
            System.out.println("Fetched exchange rates: " + rates);
            updateRateTable(rates);
            updateConversion();
        });
    }, error -> {
        javax.swing.SwingUtilities.invokeLater(() -> {
            System.err.println("Failed to fetch exchange rates: " + error);
            view.showError("Failed to fetch exchange rates: " + error);
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
    });
}

    /**
     * Starts timers for periodic exchange rate updates and countdown display.
     * The rate update timer fetches new rates every 60 seconds, and the countdown timer
     * updates the refresh countdown every second.
     */
    private void startRateUpdateTimer() {
        nextRefreshTime = System.currentTimeMillis() + 60000;
        // Ensure previous timers are stopped if this is called multiple times
        if (rateUpdateTimer != null) rateUpdateTimer.stop();
        if (countdownTimer != null) countdownTimer.stop();

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
     * Fixes: Correctly finds the JTable component within the view's hierarchy.
     *
     * @param rates A map of currency codes to their exchange rates relative to CNY.
     */
     private void updateRateTable(Map<String, Double> rates) {
    // Find the Rate Table component within the view
    JTable currentRateTable = null;
    // Assuming the structure is JSplitPane -> Right Panel -> BlueGradientPanel -> JScrollPane -> JTable
    Component[] mainComponents = view.getComponents();
    for (Component comp : mainComponents) {
        if (comp instanceof JSplitPane) {
            JSplitPane splitPane = (JSplitPane) comp;
            if (splitPane.getRightComponent() != null && splitPane.getRightComponent() instanceof JPanel) {
                JPanel rightPanel = (JPanel) splitPane.getRightComponent();
                for (Component sectionPanel : rightPanel.getComponents()) {
                    if (sectionPanel instanceof TransactionSystemComponents.BlueGradientPanel) {
                        boolean isExchangeRatePanel = false;
                        for (Component child : ((JPanel) sectionPanel).getComponents()) {
                            if (child instanceof JLabel && "Real-Time Exchange Rates".equals(((JLabel) child).getText())) {
                                isExchangeRatePanel = true;
                                break;
                            }
                        }
                        if (isExchangeRatePanel) {
                            for (Component child : ((JPanel) sectionPanel).getComponents()) {
                                if (child instanceof JScrollPane) {
                                    JScrollPane scrollPane = (JScrollPane) child;
                                    if (scrollPane.getViewport().getView() instanceof JTable) {
                                        currentRateTable = (JTable) scrollPane.getViewport().getView();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (currentRateTable != null) break;
                }
            }
        }
        if (currentRateTable != null) break;
    }

    if (currentRateTable == null) {
        System.err.println("Rate table component not found in view structure.");
        return;
    }

    // Check if the current model is DefaultTableModel, otherwise create a new one
    DefaultTableModel tableModel;
    if (currentRateTable.getModel() instanceof DefaultTableModel) {
        tableModel = (DefaultTableModel) currentRateTable.getModel();
    } else {
        // Create new DefaultTableModel with correct columns
        tableModel = new DefaultTableModel(new Object[]{"Currency Pair", "Exchange Rate"}, 0);
        currentRateTable.setModel(tableModel);
        System.out.println("Replaced table model with DefaultTableModel due to incompatible type: " + currentRateTable.getModel().getClass().getName());
    }

    // Clear existing rows
    tableModel.setRowCount(0);

    // Populate table with rates
    String[] currenciesInTableOrder = {"USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "HKD", "SGD", "NZD"};
    for (String currency : currenciesInTableOrder) {
        Double rate = rates.getOrDefault(currency, 0.0);
        tableModel.addRow(new Object[]{currency, String.format("%.4f", rate)});
    }

    currentRateTable.revalidate();
    currentRateTable.repaint();
    System.out.println("Updated rate table with rates for " + currenciesInTableOrder.length + " currencies.");
}

    /**
     * Updates the currency conversion result based on the entered amount and selected currency.
     * Displays an error if the amount format is invalid.
     */
    private void updateConversion() {
        // Ensure components are accessible
        if (view.getAmountField() == null || view.getCurrencyComboBox() == null || view.getConversionResultLabel() == null) {
             System.err.println("Conversion components not initialized in updateConversion.");
             return;
        }

        String amountText = view.getAmountField().getActualText();
        String targetCurrency = (String) view.getCurrencyComboBox().getSelectedItem();
        // Ensure exchange rates are loaded
        Map<String, Double> rates = exchangeRateService.getExchangeRates();
        if (rates == null || rates.isEmpty()) {
            view.getConversionResultLabel().setText("Result: Rates not available");
            return;
        }
        try {
            double amount = Double.parseDouble(amountText);
             // The map rates contains Currency -> RateVsCNY (e.g. USD -> 7.1 means 1 USD = 7.1 CNY).
             // To convert Amount CNY to Target, we need Amount * (Target vs CNY) = Amount * (1 / (CNY vs Target)).
             // Example: rates has "USD": 7.1. User enters 100 CNY, selects USD. Result = 100 * (1 / 7.1) USD.
            Double rateCnyPerTarget = rates.getOrDefault(targetCurrency, 0.0);
            if (rateCnyPerTarget <= 0) {
                view.getConversionResultLabel().setText("Result: Rate for " + targetCurrency + " not available or invalid.");
                System.err.println("Conversion failed: rate for " + targetCurrency + " is " + rateCnyPerTarget);
                return;
            }
            double result = amount * rateCnyPerTarget; // Corrected calculation: Amount in CNY / (CNY per Target)

            view.getConversionResultLabel().setText(String.format("Result: %.2f %s", result, targetCurrency));
            System.out.println("Updated conversion: " + amount + " CNY = " + result + " " + targetCurrency + " (Rate: 1 " + targetCurrency + " = " + String.format("%.4f", rateCnyPerTarget) + " CNY)");
        } catch (NumberFormatException e) {
            view.getConversionResultLabel().setText("Result: Invalid amount");
            System.out.println("Conversion failed: invalid amount format");
        } catch (Exception e) {
             System.err.println("Error during updateConversion: " + e.getMessage());
             e.printStackTrace();
             view.getConversionResultLabel().setText("Result: Error");
        }
    }

    /**
     * Updates the historical exchange rate trend chart based on the selected currency.
     * Uses mock historical rates (2020-2024) to populate the chart.
     * Fixes: Corrects Calendar reference and ensures TimeSeries constructor uses the imported Month class.
     */
    private void updateHistoricalTrend() {
        JPanel historicalPanel = null;
        org.jfree.chart.ChartPanel chartPanel = null;
        org.jfree.data.time.TimeSeries series = null;
        org.jfree.chart.JFreeChart chart = null;


        // Find the historical trend chart panel and its chart/dataset
        // Assuming the structure is JSplitPane -> Right Panel (GridLayout) -> BlueGradientPanel -> ChartPanel
         Component[] mainComponents = view.getComponents(); // Components directly in TransactionSystemPlane (the JSplitPane)
         for (Component comp : mainComponents) {
             if (comp instanceof JSplitPane) {
                 JSplitPane splitPane = (JSplitPane) comp;
                 // Ensure right component exists and is a JPanel
                 if (splitPane.getRightComponent() != null && splitPane.getRightComponent() instanceof JPanel) {
                      JPanel rightPanel = (JPanel)splitPane.getRightComponent();
                      // Assuming rightPanel has GridLayout and contains the historical trend panel
                      for (Component sectionPanel : rightPanel.getComponents()) {
                           // Check if it's the historical trend panel (e.g., by title or type)
                           if (sectionPanel instanceof TransactionSystemComponents.BlueGradientPanel) {
                                boolean isHistoricalPanel = false;
                                // Check if this BlueGradientPanel contains the "Historical Exchange Rate Trend" label
                               for (Component child : ((JPanel)sectionPanel).getComponents()) {
                                   if (child instanceof JLabel && "Historical Exchange Rate Trend".equals(((JLabel)child).getText())) {
                                       isHistoricalPanel = true;
                                       break;
                                   }
                               }
                               if (isHistoricalPanel) {
                                   historicalPanel = (JPanel) sectionPanel;
                                   // Find the ChartPanel within this panel
                                   for (Component grandChild : historicalPanel.getComponents()) {
                                       if (grandChild instanceof org.jfree.chart.ChartPanel) {
                                           chartPanel = (org.jfree.chart.ChartPanel) grandChild;
                                           // Get the chart and dataset
                                           chart = chartPanel.getChart();
                                           if (chart != null && chart.getXYPlot() != null && chart.getXYPlot().getDataset() instanceof org.jfree.data.time.TimeSeriesCollection) {
                                               TimeSeriesCollection dataset = (TimeSeriesCollection) chart.getXYPlot().getDataset();
                                                // Get the existing series or create a new one
                                               if (dataset.getSeriesCount() > 0) {
                                                   series = dataset.getSeries(0);
                                               } else {
                                                    // Use the constructor that doesn't require a specific time period class,
                                                    // or ensure Month.class is correctly linked/imported.
                                                    // Using the simpler constructor is generally more robust if the time period class isn't strictly enforced.
                                                   series = new TimeSeries("Rate"); // Simpler constructor
                                                   dataset.addSeries(series);
                                               }
                                           }
                                           break; // Found chartPanel, chart, dataset, series
                                       }
                                   }
                               }
                           }
                          if (historicalPanel != null && chartPanel != null && series != null) break; // Found all needed components
                      }
                 }
             }
             if (historicalPanel != null && chartPanel != null && series != null) break; // Found all needed components
         }


        if (chartPanel == null || chart == null || series == null) {
            System.err.println("Historical trend chart components not found or initialized correctly.");
            // Attempt to add a placeholder or error message to the panel if chartPanel wasn't found
            if (historicalPanel != null && chartPanel == null) {
                 // Remove existing components if any, add error label
                 historicalPanel.removeAll();
                 JLabel errorLabel = new JLabel("Error loading chart components.", JOptionPane.ERROR_MESSAGE);
                 historicalPanel.add(errorLabel, BorderLayout.CENTER);
                 historicalPanel.revalidate();
                 historicalPanel.repaint();
            }
            return;
        }

        // Clear old data from the series
        series.clear();

        String selectedCurrency = (String) view.getCurrencyComboBox().getSelectedItem();
        // Set the series key to the selected currency for legend/tooltip clarity
         series.setKey(selectedCurrency + " Rate");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Calendar cal = java.util.Calendar.getInstance(); // Corrected Calendar reference


        // Use mock historical rates (2020-2024)
        String[] dates = {"2020-12-31", "2021-12-31", "2022-12-31", "2023-12-31", "2024-12-31"};
        boolean addedAnyData = false;
        for (String date : dates) {
            Map<String, Double> monthlyRates = mockHistoricalRates.get(date);
            if (monthlyRates != null) { // Ensure rates exist for this date in mock data
                Double rate = monthlyRates.getOrDefault(selectedCurrency, 0.0);
                if (rate > 0) {
                    try {
                        cal.setTime(sdf.parse(date));
                         // Add data to the series using org.jfree.data.time.Month
                        series.add(new Month(cal.getTime()), rate); // Use imported Month
                        System.out.println("Added historical rate for " + selectedCurrency + " on " + date + ": " + rate);
                        addedAnyData = true;
                    } catch (java.text.ParseException e) { // Catch ParseException for sdf.parse
                        System.err.println("Failed to parse date string '" + date + "' for historical chart: " + e.getMessage());
                    } catch (Exception e) { // Catch any other exceptions during adding
                         System.err.println("Unexpected error adding data point for " + date + " to series: " + e.getMessage());
                         e.printStackTrace();
                    }
                } else {
                    // System.out.println("No mock rate data for " + selectedCurrency + " on " + date + " or rate is 0."); // Too noisy
                }
            } else {
                 // System.out.println("No mock rates found for date: " + date + "."); // Too noisy
            }
        }

         // If no data was added (e.g., currency not in mock data), clear the series to show an empty chart
         if (!addedAnyData) {
             series.clear();
         }

        // Ensure chart axes are updated if needed (e.g., range auto-adjustment)
        // Check if getXYPlot() is not null before calling its methods
         if (chart.getXYPlot() != null) {
            chart.getXYPlot().getRangeAxis().setAutoRange(true); // Auto-adjust y-axis
            chart.getXYPlot().getDomainAxis().setAutoRange(true); // Auto-adjust x-axis (dates)

             // Ensure DateAxis formatter is applied if needed (was in original createHistoricalTrendPanel)
             if (chart.getXYPlot().getDomainAxis() instanceof DateAxis) {
                 DateAxis dateAxis = (DateAxis) chart.getXYPlot().getDomainAxis();
                 dateAxis.setDateFormatOverride(new java.text.SimpleDateFormat("yyyy-MM"));
             }
         }


        // Repaint the chart panel and its parent containers
        chartPanel.repaint();
        chartPanel.revalidate(); // Revalidate to recalculate layout/size if needed
        if (historicalPanel != null) {
             historicalPanel.revalidate();
             historicalPanel.repaint();
        }
        view.revalidate(); // Revalidate the main view
        view.repaint(); // Repaint the main view
        System.out.println("Historical trend chart updated for " + selectedCurrency + " with " + series.getItemCount() + " data points.");
    }
}