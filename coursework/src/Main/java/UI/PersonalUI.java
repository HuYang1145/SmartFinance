package UI;

import Model.UserSession;
import Person.IncomeExpenseChart; // 确保 Person 包可以被访问
import Person.*; // Import other Person dialogs

import java.awt.*;
import javax.swing.*;

public class PersonalUI extends JDialog {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private SidebarPanel sidebarPanel;
    private ContentPanelManager contentManager; // Manager to handle panel creation and adding

    // --- Instance variables for report panels (Managed by ContentPanelManager) ---
    // We might not need direct references here if ContentPanelManager handles them
    // private ReportOptionsPanel reportOptionsPanel;
    // private ReportViewPanel reportViewPanel;
    // private ReportCycleSettingsPanel reportCycleSettingsPanel;

    private int currentReportCycleDays = 7; // Default cycle days, non-persistent (session-based)

    public PersonalUI() {
        setTitle("Personal Account Center");
        // Consider slightly larger default size if chart needs more space
        setSize(950, 650); // Increased size slightly
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- 1. Create Sidebar ---
        sidebarPanel = new SidebarPanel(this); // Pass reference to this PersonalUI instance
        add(sidebarPanel, BorderLayout.WEST);

        // --- 2. Create Content Panel area with CardLayout ---
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        // Background color will be determined by the panels inside

        // --- 3. Create ContentPanelManager and Initialize Panels ---
        // ContentPanelManager will create and add all panels to contentPanel
        contentManager = new ContentPanelManager(this, contentPanel, cardLayout);
        contentManager.initializePanels(); // This method adds all cards

        // --- 4. Add the Content Panel to the main layout ---
        add(contentPanel, BorderLayout.CENTER);

        // --- 5. Set Default View ---
        // Ensure "individualCenter" is a valid card name added by ContentPanelManager
        cardLayout.show(contentPanel, "individualCenter");

        setVisible(true);
    }

    // --- Methods to switch cards in the contentPanel ---

    /**
     * Shows the main individual center panel.
     */
    public void showIndividualCenter() {
        checkLoginAndShowCard("individualCenter");
    }

    /**
     * Shows the periodic report options panel.
     */
    public void showReportOptions() {
        checkLoginAndShowCard("reportOptions");
    }

    /**
     * Shows the detailed report view panel.
     * Important: This assumes ContentPanelManager holds the reference to ReportViewPanel
     * or we need a way to get it to call loadReportData.
     * A better approach might be to have ContentPanelManager provide access or
     * call loadReportData when switching.
     * For now, we rely on the panel's setVisible method.
     */
    public void showReportView() {
        // The ReportViewPanel's setVisible(true) should trigger data loading
        checkLoginAndShowCard("reportView");
    }

    /**
     * Shows the report cycle settings panel.
     * Relies on the panel's setVisible(true) to refresh the displayed cycle days.
     */
    public void showCycleSettings() {
        checkLoginAndShowCard("reportCycleSettings");
    }

    /**
     * Shows the Financial Suggestion panel.
     */
    public void showFinancialSuggestion() {
        checkLoginAndShowCard("financialSuggestion");
    }

    /**
     * Shows the AI Q&A panel.
     */
    public void showAiQA() {
        checkLoginAndShowCard("aiQA");
    }

    /**
     * Shows the Spending Proportion placeholder panel.
     */
    public void showSpendingProportion() {
        checkLoginAndShowCard("spendingProportion");
    }

    // --- Getter/Setter for report cycle (session only) ---

    /**
     * Gets the current reporting cycle duration in days for this session.
     * @return The number of days in the reporting cycle.
     */
    public int getReportCycleDays() {
        return currentReportCycleDays;
    }

    /**
     * Sets the reporting cycle duration in days for the current session.
     * Value is not persisted across logins.
     * @param days The new number of days for the reporting cycle (must be > 0).
     */
    public void setReportCycleDays(int days) {
        if (days > 0) {
            this.currentReportCycleDays = days;
            System.out.println("Report cycle for this session set to: " + days + " days.");
            // We don't automatically refresh the report view here.
            // The view will refresh when it becomes visible next time or manually refreshed.
        } else {
            System.err.println("Attempted to set invalid report cycle days: " + days);
        }
    }


    // --- Methods called by SidebarPanel or other components ---

    /**
     * Displays the income/expense pie chart window.
     * Checks login status first.
     */
    public void showIncomeExpenseChart() {
        System.out.println("Attempting to show IncomeExpenseChart...");
        if (UserSession.getCurrentUsername() != null) {
            // Run chart display on the EDT
            SwingUtilities.invokeLater(() -> {
                // Assuming IncomeExpenseChart expects the file path
                IncomeExpenseChart.showIncomeExpensePieChart("transactions.csv");
            });
        } else {
            UIUtils.showLoginError(this);
        }
    }

    /**
     * Checks if user is logged in before showing a specific card panel.
     * @param cardName The name of the card panel to show in the CardLayout.
     */
    public void checkLoginAndShowCard(String cardName) {
        if (UserSession.getCurrentUsername() != null) {
            System.out.println("Switching to card: " + cardName); // Debugging
            cardLayout.show(contentPanel, cardName);
        } else {
            System.out.println("Login required to show card: " + cardName); // Debugging
            UIUtils.showLoginError(this);
        }
    }

    /**
     * Checks if user is logged in before executing an action, typically showing a dialog.
     * The action is executed on the Event Dispatch Thread.
     * @param dialogCreator A Runnable that creates and shows the dialog or performs the action.
     */
    public void checkLoginAndShowDialog(Runnable dialogCreator) {
        if (UserSession.getCurrentUsername() != null) {
            // Ensure the dialog creation/display happens on the EDT
            SwingUtilities.invokeLater(dialogCreator);
        } else {
            System.out.println("Login required to perform this action."); // Debugging
            UIUtils.showLoginError(this);
        }
    }

    /**
     * Handles the logout process, including confirmation and clearing the user session.
     */
    public void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            UserSession.clearSession(); // Clear the username from session
            dispose(); // Close this PersonalUI window
            // Optionally: Bring the main login window (App) back to the front or create a new one
            // This depends on how App is managed. If App is still running but hidden:
            // findParentAppFrame().setVisible(true); // Requires a method to get the App frame
            // Or create a new login session:
            // SwingUtilities.invokeLater(() -> new Main.App().setVisible(true));
        }
    }

    // Optional helper to find the parent App frame if needed after logout
    /*
    private Frame findParentAppFrame() {
        Container parent = getParent();
        while (parent != null && !(parent instanceof Frame)) {
            parent = parent.getParent();
        }
        return (Frame) parent;
    }
    */
}