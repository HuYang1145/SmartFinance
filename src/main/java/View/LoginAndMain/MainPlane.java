package View.LoginAndMain;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
// Removed unnecessary imports related to Graphics/Shape/RoundRectangle2D/BufferedImage/File/IOException/ImageIO
// import java.awt.Graphics;
// import java.awt.Graphics2D;
// import java.awt.RenderingHints;
// import java.awt.Shape;
// import java.awt.geom.RoundRectangle2D;
// import java.awt.image.BufferedImage;
// import java.io.File;
// import java.io.IOException;
// import javax.imageio.ImageIO;

import java.util.ArrayList;
import java.util.List;
import javax.swing.*; // Consolidated Swing imports

import Controller.BillController; // Keep import
import Controller.MainPanelController; // Keep import
import Controller.PersonCenterController; // Keep import
import Model.UserSession; // Keep import
// Keep imports for custom components and services
import View.LoginAndMain.LoginRoundedInputField.*;
import View.LoginAndMain.NavItemPanel.*;
import Service.BudgetService;
import Service.TransactionService;

import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.SwingUtilities;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentListener;


/**
 * Represents the main user dashboard interface for the Smart Finance Application.
 * Provides a sidebar with navigation items and a card-based content area for various user functionalities,
 * such as personal center, transactions, bill statistics, budget management, AI assistant, and horoscope reports.
 *
 * @author Group 19
 * @version 1.0
 */
public class MainPlane extends JFrame {
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private String username;
    private final List<NavItemPanel> navItems = new ArrayList<>();
    private final PersonCenterController personCenterController;
    private final BillController billController;
    private final BudgetService budgetService;
    private final TransactionService transactionService;

    private MainPanelController contentPanelManager;
    private JPanel sidebar;

    /**
     * Constructs a MainPlane frame for the logged-in user's dashboard with specified dependencies.
     *
     * @param personCenterController the controller for managing personal center functionalities
     * @param billController         the controller for managing bill-related operations
     * @param budgetService          the service for budget calculations
     * @param transactionService     the service for transaction logic and checks
     */
    public MainPlane(PersonCenterController personCenterController, BillController billController,
                     BudgetService budgetService, TransactionService transactionService) {
        this.personCenterController = personCenterController;
        this.billController = billController;
        this.budgetService = budgetService;
        this.transactionService = transactionService;

        this.username = UserSession.getCurrentUsername();
        if (username == null || username.trim().isEmpty()) {
            // Ensure UI interaction (JOptionPane) is on EDT if this constructor is called off EDT
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "No user logged in. Please log in first.", "Error", JOptionPane.ERROR_MESSAGE));
            dispose(); // Close the frame
            return;
        }
        System.out.println("MainPlane initialized with username: " + username);
        initializeUI();
    }

    /**
     * Initializes the user interface, setting up the sidebar, content panel, and card layout.
     * This method should be called on the Event Dispatch Thread (EDT).
     */
    private void initializeUI() {
         // Ensure initialization happens on EDT if called from elsewhere
         if (!SwingUtilities.isEventDispatchThread()) {
             SwingUtilities.invokeLater(this::initializeUI);
             return;
         }

        setTitle("Smart Finance - Personal Dashboard");
        setSize(1920, 1080);
        setLocationRelativeTo(null); // Center the frame on the screen
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close only this window on exit

        // Setup the main layout
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE); // Set background color
        add(mainPanel); // Add main panel to the frame's content pane

        // Initialize CardLayout and content panel
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE); // Set background color
        System.out.println("Initialized cardLayout=" + cardLayout + ", contentPanel=" + contentPanel);

        // Initialize the MainPanelController with dependencies
        contentPanelManager = new MainPanelController(username, contentPanel, personCenterController, billController, budgetService, transactionService, cardLayout);
        contentPanelManager.initializeContentPanels(); // Initialize the sub-panels/placeholders

        // Create and add the sidebar
        sidebar = createSidebar();
        mainPanel.add(sidebar, BorderLayout.WEST);

        // Add the content panel to the main panel
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Initially show the "Personal Center" panel
        // This should also ensure the corresponding NavItem is selected.
        contentPanelManager.showPanel("Personal Center");

        // Select the initial item in the sidebar *after* creating the sidebar and navItems
        SwingUtilities.invokeLater(() -> { // Ensure this runs on EDT
            for (NavItemPanel item : navItems) {
                if ("Personal Center".equals(item.getName())) {
                    item.setSelected(true); // Select "Personal Center"
                } else {
                    item.setSelected(false); // Deselect others
                }
            }
            // Repaint the sidebar to update the visual selection
             if (sidebar != null) {
                sidebar.revalidate();
                sidebar.repaint();
             }
        });

        // Make the frame visible
        setVisible(true);
    }

    /**
     * Creates the sidebar with navigation items and a search field for filtering options.
     *
     * @return the configured sidebar panel
     */
    private JPanel createSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS)); // Use BoxLayout for vertical arrangement
        sb.setBackground(Color.WHITE); // Set background color
        sb.setPreferredSize(new Dimension(200, 0)); // Set preferred width

        System.out.println("Creating sidebar with " + navItems.size() + " items (before adding)");

        // Create and add the logo
        NavItemPanel.GradientLabel logo = new NavItemPanel.GradientLabel("Smart Finance");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setBorder(new EmptyBorder(20, 0, 10, 0)); // Add padding
        logo.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT); // Center horizontally
        sb.add(logo);

        // Clear the navItems list to ensure it's fresh if this method is called multiple times
        navItems.clear();

        // Create and add the "Welcome" panel item
        NavItemPanel welcomePanel = new NavItemPanel("Welcome, " + username, navItems, cardLayout, contentPanel, contentPanelManager);
        // Use public methods to customize the icon/text
        welcomePanel.setIconText("U"); // Set icon text for the circle
        welcomePanel.setCursor(Cursor.getDefaultCursor()); // Set cursor
        welcomePanel.setBackground(null); // Ensure background is handled by paintComponent or parent
        navItems.add(welcomePanel); // Add to the list
        sb.add(welcomePanel); // Add to the sidebar panel
        sb.add(Box.createRigidArea(new Dimension(0, 8))); // Add vertical spacing

        // Create and add the search field
        JTextField search = new JTextField();
        styleSearchField(search, "Search..."); // Apply styling
        sb.add(search); // Add to sidebar
        sb.add(Box.createRigidArea(new Dimension(0, 16))); // Add vertical spacing

        // Define standard navigation options
        String[] options = {
                "Personal Center",
                "Transaction System",
                "Bill Statistics",
                "Budget Management",
                "AI Assistant",
                "Spending Star Whispers"
        };

        // Create and add NavItemPanels for standard options
        for (String opt : options) {
            NavItemPanel item = new NavItemPanel(opt, navItems, cardLayout, contentPanel, contentPanelManager);
            navItems.add(item); // Add to the list
            sb.add(item); // Add to the sidebar panel
            sb.add(Box.createRigidArea(new Dimension(0, 8))); // Add vertical spacing
            // System.out.println("Added NavItem: " + opt); // Debug adding
        }

        sb.add(Box.createVerticalGlue()); // Push items to the top

        // Create and add the "Logout" panel item
        NavItemPanel logout = new NavItemPanel("Logout", navItems, cardLayout, contentPanel, contentPanelManager);
        // Use public methods to customize icon/text appearance
        logout.setIconText("L"); // Set icon text
        logout.setTextLabelColor(Color.RED.darker()); // Set text color
        // Assuming NavItemPanel.CircleIcon fields color1/color2 are public
        logout.setIconCircleColors(Color.RED.brighter(), Color.RED.darker()); // Set icon gradient colors

        // Add MouseListener for the core logout action
        logout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                 // Ensure click action runs on EDT if the listener might be triggered off-EDT (less common for Swing event listeners)
                 SwingUtilities.invokeLater(() -> {
                     // Ensure the item is interactable before proceeding
                     if (logout.isEnabled() && logout.isVisible()) {
                         System.out.println("Logout button clicked for user: " + username);
                         int choice = JOptionPane.showConfirmDialog(
                                 MainPlane.this, // Parent frame for the dialog
                                 "Are you sure you want to log out?",
                                 "Logout Confirmation",
                                 JOptionPane.YES_NO_OPTION,
                                 JOptionPane.QUESTION_MESSAGE // Use question icon
                         );
                         if (choice == JOptionPane.YES_OPTION) {
                             UserSession.clearSession(); // Clear the user session
                             MainPlane.this.dispose(); // Close the main application window
                             System.out.println("User " + username + " logged out. MainPlane disposed.");
                             // Optional: Code to show the login window again could go here,
                             // or be handled by the main application entry point.
                         }
                     }
                 });
            }
            // Hover effects (background change) are handled by the MouseListener within NavItemPanel itself,
            // triggered by mouseEntered/mouseExited events on the NavItemPanel.
        });

        navItems.add(logout); // Add to the list
        sb.add(logout); // Add to the sidebar panel
        sb.add(Box.createRigidArea(new Dimension(0, 20))); // Add vertical spacing

        // Add DocumentListener to the search field for filtering
        search.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                String text = search.getText().trim().toLowerCase();
                // If the text is the placeholder, treat it as an empty search
                if (text.equals("search...") || text.isEmpty()) {
                    text = "";
                }
                final String searchText = text; // Use final variable in lambda

                // Iterate through all navigation items to determine visibility based on search text
                for (NavItemPanel it : navItems) {
                    // Define items that should always be visible regardless of search
                    boolean isAlwaysVisibleItem = it.getName().toLowerCase().equals("welcome, " + username.toLowerCase()) || "logout".equals(it.getName().toLowerCase());

                    if (isAlwaysVisibleItem) {
                        it.setVisible(true); // Always show welcome and logout
                    } else {
                        // For other items, check if their name contains the search text (case-insensitive)
                        boolean isVisibleBySearch = it.getName().toLowerCase().contains(searchText);
                        // System.out.println("NavItem: " + it.getName() + ", Visible: " + isVisibleBySearch + ", SearchText: '" + searchText + "'"); // Debug search filtering
                        it.setVisible(isVisibleBySearch);
                    }
                }
                // Revalidate and repaint the sidebar to update layout after changing component visibility
                sb.revalidate();
                sb.repaint();
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                update(); // Call update on insert
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                update(); // Call update on remove
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                update(); // Call update on changed
            }
        });

        // Initial selection is handled outside this method after the sidebar is created.
        // welcomePanel.setSelected(true); // This line might be redundant here if handled elsewhere

        System.out.println("Sidebar creation finished with " + navItems.size() + " items.");

        return sb; // Return the configured sidebar panel
    }

    /**
     * Styles the search field with a placeholder and rounded border.
     *
     * @param tf          the text field to style
     * @param placeholder the placeholder text to display
     */
    private void styleSearchField(JTextField tf, String placeholder) {
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34)); // Set maximum height, full width
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Set font
        tf.setBackground(new Color(240, 240, 240)); // Set background color
        tf.setBorder(BorderFactory.createCompoundBorder( // Apply compound border for rounded corners and padding
                new RoundBorder(17, new Color(200, 200, 200)), // Outer rounded border
                BorderFactory.createEmptyBorder(4, 10, 4, 10) // Inner padding
        ));
        tf.setText(placeholder); // Set initial text (placeholder)
        tf.setForeground(Color.GRAY); // Set placeholder text color

        // Add focus listener for placeholder behavior
        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText(""); // Clear placeholder on focus gain
                    tf.setForeground(Color.DARK_GRAY); // Change text color
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) {
                    tf.setText(placeholder); // Restore placeholder if empty on focus loss
                    tf.setForeground(Color.GRAY); // Restore placeholder text color
                }
            }
        });
    }
}