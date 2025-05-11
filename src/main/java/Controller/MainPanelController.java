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
    private PersonalCenterPanel personalCenterPanel;
    private TransactionSystemController transactionSystemController;
    private AIController aiController;

    /**
     * Constructs a MainPanelController with the specified parameters.
     *
     * @param username              The username of the logged-in user.
     * @param contentPanel          The main JPanel that holds different sub-panels.
     * @param personCenterController Controller for the Personal Center panel.
     * @param billController        Controller for the Bill Statistics panel.
     * @param cardLayout            The CardLayout used to switch between panels.
     */
    public MainPanelController(String username, JPanel contentPanel, PersonCenterController personCenterController, BillController billController, CardLayout cardLayout) {
        this.username = username;
        this.contentPanel = contentPanel;
        this.personCenterController = personCenterController;
        this.billController = billController;
        this.cardLayout = cardLayout;
        System.out.println("MainPanelController initialized for user: " + username);
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
                        return null;
                    }

                    @Override
                    protected void done() {
                        System.out.println("Initializing data for Personal Center");
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
                    return personalCenterPanel;
                case "AI Assistant":
                    System.out.println("Loading AI Assistant panel");
                    AIPanel aiPanel = new AIPanel();
                    aiController = new AIController(aiPanel, new AIService(new TransactionService(new TransactionRepository()), new TransactionController(), new DeepSeekService(), new UserRepository(), new ExchangeRateService()));
                    return aiPanel;
                case "Transaction System":
                    System.out.println("Loading Transaction System panel");
                    TransactionSystemPlane transactionSystemPlane = new TransactionSystemPlane(username);
                    transactionSystemController = new TransactionSystemController(
                            transactionSystemPlane, new ExchangeRateService(), new TransactionController(), username
                    );
                    return transactionSystemPlane;
                case "Bill Statistics":
                    System.out.println("Loading Bill Statistics panel");
                    BillPanel billPanel = new BillPanel(username, personCenterController, billController);
                    SwingWorker<Void, Void> worker = new SwingWorker<>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            return null;
                        }

                        @Override
                        protected void done() {
                            try {
                                System.out.println("Initializing BillPanel chart");
                                billPanel.initializeChart();
                            } catch (Exception e) {
                                System.err.println("Failed to initialize BillPanel chart: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    };
                    worker.execute();
                    return billPanel;
                case "Budget Management":
                    System.out.println("Loading Budget Management panel");
                    BudgetManagementPanel budgetPanel = new BudgetManagementPanel(username);
                    if (!budgetPanel.isInitialized()) {
                        throw new IllegalStateException("Budget Management panel failed to initialize due to invalid username.");
                    }
                    return budgetPanel;
                case "Spending Star Whispers":
                    System.out.println("Loading Spending Star Whispers panel");
                    return new HoroscopePanel(username);
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

        for (Component comp : components) {
            if (name.equals(comp.getName())) {
                targetPanel = comp;
                break;
            }
        }

        if (targetPanel == null) {
            System.out.println("Panel not found, loading: " + name);
            JPanel panel = loadPanel(name);
            panel.setName(name);
            contentPanel.add(panel, name);
        } else if (targetPanel instanceof JLabel) {
            System.out.println("Replacing placeholder for: " + name);
            contentPanel.remove(targetPanel);
            JPanel panel = loadPanel(name);
            panel.setName(name);
            contentPanel.add(panel, name);
        } else {
            System.out.println("Panel already loaded: " + name);
        }

        cardLayout.show(contentPanel, name);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}