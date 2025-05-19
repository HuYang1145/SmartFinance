package Controller;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import Repository.TransactionRepository;
import Repository.UserRepository;
import Service.AIService;
import Service.DeepSeekService;
import Service.ExchangeRateService;
import Service.TransactionService;
import Service.BudgetService;
import View.AI.AIPanel;
import View.Bill.BillPanel;
import View.BudgetAdvisor.BudgetManagementPanel;
import View.HoroscopePanel.HoroscopePanel;
import View.PersonalCenter.PersonalCenterPanel;
import View.Transaction.TransactionSystemPlane;

/**
 * Controller class responsible for managing the main content panel and its associated sub-panels
 * in a financial management application. It handles the initialization, loading, and display
 * of various panels such as Personal Center, Transaction System, Bill Statistics, Budget Management,
 * AI Assistant, and Spending Star Whispers using a CardLayout.
 * Manages dependencies for sub-controllers and services.
 *
 * @author 19
 * @version 1.0
 */
public class MainPanelController {
    private final String username;
    private final JPanel contentPanel;
    private final PersonCenterController personCenterController;
    private final BillController billController;
    private final CardLayout cardLayout;
    private final BudgetService budgetService; // Added dependency
    private final TransactionService transactionService; // Added dependency

    private PersonalCenterPanel personalCenterPanel;
    private TransactionSystemController transactionSystemController;
    private AIController aiController;
    private TransactionSystemPlane transactionSystemPlane;
    private BillPanel billPanel;

    /**
     * Constructs a MainPanelController with the specified parameters and dependencies.
     *
     * @param username              The username of the logged-in user.
     * @param contentPanel          The main JPanel that holds different sub-panels.
     * @param personCenterController Controller for the Personal Center panel.
     * @param billController        Controller for the Bill Statistics panel.
     * @param budgetService         Service for budget calculations.
     * @param transactionService    Service for transaction logic and checks.
     * @param cardLayout            The CardLayout used to switch between panels.
     */
    public MainPanelController(String username, JPanel contentPanel, PersonCenterController personCenterController,
                               BillController billController, BudgetService budgetService, TransactionService transactionService,
                               CardLayout cardLayout) {
        this.username = username;
        this.contentPanel = contentPanel;
        this.personCenterController = personCenterController;
        this.billController = billController;
        this.budgetService = budgetService;
        this.transactionService = transactionService;
        this.cardLayout = cardLayout;
        System.out.println("MainPanelController initialized for user: " + username);
        TransactionController.setMainPanelController(this);
    }

    /**
     * Initializes the content panels by adding placeholders or the Personal Center panel
     * to the main content panel. The Personal Center panel is fully initialized, while
     * others are represented by placeholders until loaded.
     */
    public void initializeContentPanels() {
        String[] panelNames = {
                "Personal Center",
                "Transaction System",
                "Bill Statistics",
                "Budget Management",
                "AI Assistant",
                "Spending Star Whispers"
        };

        for (String name : panelNames) {
            if (name.equals("Personal Center")) {
                personalCenterPanel = new PersonalCenterPanel();
                contentPanel.add(personalCenterPanel, name);
                System.out.println("Added Personal Center panel to contentPanel");
                personCenterController.setView(personalCenterPanel);
                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        // Data loading logic is now within PersonCenterController.initializeData
                        return null;
                    }

                    @Override
                    protected void done() {
                        System.out.println("Initializing data for Personal Center");
                        // Pass the controller itself to the view for data loading actions
                        personalCenterPanel.initializeData(personCenterController);
                    }
                };
                worker.execute();
            } else {
                JLabel placeholder = new JLabel("Loading " + name + "...", SwingConstants.CENTER);
                placeholder.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                placeholder.setForeground(Color.DARK_GRAY);
                placeholder.setName(name);
                contentPanel.add(placeholder, name);
                System.out.println("Added placeholder for panel: " + name);
            }
        }
    }

    /**
     * Loads the specified panel dynamically based on its name. If the panel fails to load,
     * a placeholder panel is returned.
     *
     * @param name The name of the panel to load.
     * @return The loaded JPanel or a placeholder if loading fails.
     */
    private JPanel loadPanel(String name) {
        try {
            switch (name) {
                case "Personal Center":
                    System.out.println("Loading Personal Center panel from loadPanel");
                    return personalCenterPanel; // Return the already created instance
                case "AI Assistant":
                    System.out.println("Loading AI Assistant panel");
                    AIPanel aiPanel = new AIPanel();
                    // AIService needs TransactionService and its dependencies (TransactionRepository, BudgetService)
                    // And other services (DeepSeekService, UserRepository, ExchangeRateService)
                    // We can pass the shared transactionService and budgetService here
                    AIService aiService = new AIService(transactionService, new TransactionController(), new DeepSeekService(), new UserRepository(), new ExchangeRateService());
                    aiController = new AIController(aiPanel, aiService);
                    return aiPanel;
                case "Transaction System":
                    System.out.println("Loading Transaction System panel");
                    transactionSystemPlane = new TransactionSystemPlane(username);
                    // TransactionSystemController needs BudgetService and TransactionService
                    transactionSystemController = new TransactionSystemController(
                            transactionSystemPlane, new ExchangeRateService(), new TransactionController(),
                            transactionService, budgetService, username // Pass dependencies
                    );
                    return transactionSystemPlane;
                case "Bill Statistics":
                    System.out.println("Loading Bill Statistics panel");
                    // BillController needs AccountRepository (already a field in MainPanelController)
                    billPanel = new BillPanel(username, personCenterController, billController);
                    // Initialize chart on a background thread
                    SwingWorker<Void, Void> worker = new SwingWorker<>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            // Chart initialization might involve reading files/calculations, do off EDT
                            return null; // Initialization is called in done() on EDT
                        }

                        @Override
                        protected void done() {
                            try {
                                System.out.println("Initializing BillPanel chart");
                                billPanel.initializeChart(); // This method calls updateChart() on EDT
                            } catch (Exception e) {
                                System.err.println("Failed to initialize BillPanel chart: " + e.getMessage());
                                e.printStackTrace();
                                JOptionPane.showMessageDialog(billPanel, "Failed to load Bill Chart: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    };
                    worker.execute();
                    return billPanel;
                case "Budget Management":
                    System.out.println("Loading Budget Management panel");
                    // BudgetManagementPanel now accepts BudgetService
                    BudgetManagementPanel budgetPanel = new BudgetManagementPanel(username, budgetService); // Pass shared BudgetService
                    if (!budgetPanel.isInitialized()) {
                        throw new IllegalStateException("Budget Management panel failed to initialize due to invalid username or dependency.");
                    }
                    // Loading data is handled internally by BudgetManagementPanel's controller
                    return budgetPanel;
                case "Spending Star Whispers":
                    System.out.println("Loading Spending Star Whispers panel");
                    // HoroscopePanel needs its own controller and service
                    return new HoroscopePanel(username); // HoroscopePanel internally creates controller/service
                default:
                    System.err.println("Warning: Unhandled panel name in loadPanel: " + name);
                    return createPlaceholderPanel(name);
            }
        } catch (Exception e) {
            System.err.println("Failed to load panel " + name + ": " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(contentPanel, "Failed to load " + name + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return createPlaceholderPanel(name);
        }
    }

    /**
     * Creates a placeholder panel with a centered label for panels that fail to load
     * or are not yet implemented.
     *
     * @param name The name of the panel to display on the placeholder.
     * @return A JPanel with a placeholder label.
     */
    private JPanel createPlaceholderPanel(String name) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20),
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true)));

        JLabel contentLabel = new JLabel("Content for " + name, SwingConstants.CENTER);
        contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        contentLabel.setForeground(new Color(50, 50, 50));
        card.add(contentLabel, BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Displays the specified panel in the content panel using the CardLayout.
     * If the panel is not loaded, it will be dynamically loaded. If it is a placeholder,
     * it will be replaced with the actual panel.
     *
     * @param name The name of the panel to display.
     */
    public void showPanel(String name) {
        System.out.println("Showing panel: " + name);
        Component[] components = contentPanel.getComponents();
        Component targetPanel = null;

        // Find the existing component with the target name
        for (Component comp : components) {
            if (name.equals(comp.getName())) {
                targetPanel = comp;
                break;
            }
        }

        // If the target panel is a placeholder (JLabel), remove it and load the actual panel
        if (targetPanel instanceof JLabel) {
            System.out.println("Replacing placeholder for: " + name);
            contentPanel.remove(targetPanel);
            JPanel panel = loadPanel(name); // loadPanel ensures the panel is created or returns existing
            if (panel != null) {
                 panel.setName(name); // Set name on the loaded panel
                 contentPanel.add(panel, name);
            } else {
                 // Handle case where loadPanel might return null or fail, although current loadPanel creates placeholders on failure
                 System.err.println("Failed to load panel " + name + " after removing placeholder.");
                 // Optionally re-add a placeholder or show an error panel
                 contentPanel.add(createPlaceholderPanel(name), name);
            }
        } else if (targetPanel == null) {
             // If the panel wasn't found at all, load it
             System.out.println("Panel not found, loading: " + name);
             JPanel panel = loadPanel(name);
             if (panel != null) {
                panel.setName(name);
                contentPanel.add(panel, name);
             } else {
                System.err.println("Failed to load panel " + name + ".");
                contentPanel.add(createPlaceholderPanel(name), name);
             }
        } else {
            System.out.println("Panel already loaded and is not a placeholder: " + name);
            // The panel already exists and is not a placeholder, just show it.
            // No action needed here as cardLayout.show handles it.
        }

        // Ensure the panel is added to the layout manager before showing
        // This should already be handled by contentPanel.add()

        cardLayout.show(contentPanel, name);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    public void reloadAllPanels() {
            if (personalCenterPanel != null) {
                personalCenterPanel.refreshData();
            }
            if (transactionSystemPlane != null) {
                transactionSystemPlane.refreshData();
            }
            if(billPanel !=null){
                billPanel.refreshData();
            }
        }
}