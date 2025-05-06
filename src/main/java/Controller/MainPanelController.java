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

public class MainPanelController {
    private final String username;
    private final JPanel contentPanel;
    private final PersonCenterController personCenterController;
    private final BillController billController;
    private final CardLayout cardLayout;
    private PersonalCenterPanel personalCenterPanel;
    private TransactionSystemController transactionSystemController;
    private AIController aiController;

    public MainPanelController(String username, JPanel contentPanel, PersonCenterController personCenterController, BillController billController, CardLayout cardLayout) {
        this.username = username;
        this.contentPanel = contentPanel;
        this.personCenterController = personCenterController;
        this.billController = billController;
        this.cardLayout = cardLayout;
        System.out.println("MainPanelController initialized for user: " + username);
    }

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

    private JPanel loadPanel(String name) {
        try {
            switch (name) {
                case "Personal Center":
                    System.out.println("Loading Personal Center panel from loadPanel");
                    return personalCenterPanel;
                case "AI Assistant":
                    System.out.println("Loading AI Assistant panel");
                    AIPanel aiPanel = new AIPanel();
                    aiController = new AIController(aiPanel,new AIService(new TransactionService(new TransactionRepository()),new TransactionController(),new DeepSeekService(),new UserRepository(),new ExchangeRateService()));
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