package View.HoroscopePanel;

import Controller.HoroscopeController;
import Model.Transaction;
// import Service.HoroscopeService; // Not directly used here, but its constants might be (indirectly via controller/model)

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Month;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

/**
 * UI Panel for the "Spending Star Whispers" feature.
 * This panel displays a welcome screen with an option to select a month,
 * and a report screen showing a "spending star" (an image and description based on
 * the user's highest spending category for that month), along with a list of transactions.
 * <p>
 * Features include:
 * <ul>
 * <li>A colorful gradient background.</li>
 * <li>Vibrantly styled buttons.</li>
 * <li>Display of large images for welcome and report sections.</li>
 * <li>CardLayout to switch between welcome and report views.</li>
 * <li>A table to display transactions for the selected month.</li>
 * </ul>
 * It interacts with {@link HoroscopeController} to handle user actions and data loading.
 *
 * @author Group 19
 * @version 2.1 (Report text moved left, welcome.jpg reverted to very large, other reports medium-large)
 */
public class HoroscopePanel extends JPanel {
    private transient Controller.HoroscopeController controller; // Mark as transient if panel is serializable, though Swing components usually aren't.
    private JLabel titleLabel, welcomeImageLabel, reportImageLabel;
    private JTextArea descriptionArea;
    private JButton revealButton, backButton;
    private CardLayout contentCardLayout;
    private JPanel contentCardPanel, welcomePanel, reportPanel;

    private JComboBox<String> monthComboBox;
    private JTable transactionsTable;
    private DefaultTableModel transactionTableModel;
    private JScrollPane transactionScrollPane;

    private static final String WELCOME_CARD = "Welcome";
    private static final String REPORT_CARD = "Report";

    // Main panel background gradient (Bluish-Gray to Beige/Pale Yellow)
    private static final Color BG_GRADIENT_START = new Color(170, 185, 200);
    private static final Color BG_GRADIENT_END = new Color(230, 220, 190);

    // Unified Button Gradient Colors (Deep Blue to Deep Purple)
    private static final Color UNIFIED_BTN_START = new Color(0, 0, 139);    // Dark Blue
    private static final Color UNIFIED_BTN_END = new Color(75, 0, 130);     // Indigo/Deep Purple


    /**
     * Constructs the HoroscopePanel.
     * Initializes UI components and sets up the layout.
     * Attempts to create a {@link HoroscopeController}. If the controller creation fails
     * (e.g., due to missing username), an error message is displayed.
     *
     * @param username The username of the current user, required by the {@link HoroscopeController}.
     */
    public HoroscopePanel(String username) {
        try {
            this.controller = new Controller.HoroscopeController(username, this);
        } catch (IllegalArgumentException e) {
            System.err.println("HoroscopePanel: Error initializing HoroscopeController - " + e.getMessage());
            this.controller = null; // Ensure controller is null if initialization failed
            setLayout(new BorderLayout());
            JLabel errorLabel = new JLabel("<html><center>Horoscope feature is currently unavailable.<br>Error: " +
                    e.getMessage() + "</center></html>", SwingConstants.CENTER);
            errorLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            errorLabel.setForeground(Color.DARK_GRAY);
            add(errorLabel, BorderLayout.CENTER);
            return; // Stop further initialization if controller setup fails
        }

        setLayout(new BorderLayout(5, 5)); // Add small gaps
        setBorder(new EmptyBorder(15, 25, 15, 25)); // Add padding around the panel

        initComponents();
        layoutComponents();

        contentCardLayout.show(contentCardPanel, WELCOME_CARD); // Show welcome card initially

        // Listener to load welcome image when panel becomes visible and ready
        this.addComponentListener(new ComponentAdapter() {
            private boolean initialWelcomeLoadDone = false;
            @Override
            public void componentShown(ComponentEvent e) {
                // Ensure panel is part of a window, showing, controller exists, and initial load hasn't happened
                if (SwingUtilities.getWindowAncestor(HoroscopePanel.this) != null &&
                        HoroscopePanel.this.isShowing() &&
                        controller != null && !initialWelcomeLoadDone) {

                    SwingUtilities.invokeLater(() -> { // Ensure executed on EDT
                        // Load image if label has dimensions, otherwise use default (0,0) to trigger fallback in controller
                        if (welcomeImageLabel.getWidth() > 0 && welcomeImageLabel.getHeight() > 0) {
                            controller.loadWelcomeImage(welcomeImageLabel.getWidth(), welcomeImageLabel.getHeight());
                        } else {
                            controller.loadWelcomeImage(0,0); // Controller will use fallback
                        }
                        initialWelcomeLoadDone = true;
                    });
                }
            }
        });
    }

    /**
     * Overrides paintComponent to draw a gradient background.
     * @param g the Graphics object to protect
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(
                0, 0, BG_GRADIENT_START,
                getWidth(), 0, BG_GRADIENT_END // Horizontal gradient
        );
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }


    /**
     * Called when the panel is added to a container.
     * Attempts to load the welcome image if it hasn't been loaded yet and the panel is ready.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        // Use invokeLater to ensure components are properly sized and laid out.
        SwingUtilities.invokeLater(() -> {
            if (controller != null &&
                    (welcomeImageLabel.getIcon() == null && (welcomeImageLabel.getText() == null || welcomeImageLabel.getText().isEmpty()))) {
                // Load image if label has dimensions, otherwise use default (0,0) for controller fallback
                if (welcomeImageLabel.getWidth() > 0) {
                    controller.loadWelcomeImage(welcomeImageLabel.getWidth(), welcomeImageLabel.getHeight());
                } else {
                    controller.loadWelcomeImage(0, 0);
                }
            }
        });
    }

    /**
     * Gets the current width of the welcome image label.
     * Used by the controller to determine appropriate scaling for the welcome image.
     * @return The width of the welcome image label if it's showing and has a positive width, otherwise 0.
     */
    public int getWelcomeImageLabelWidth() {
        return (welcomeImageLabel != null && welcomeImageLabel.isShowing() && welcomeImageLabel.getWidth() > 1) ? welcomeImageLabel.getWidth() : 0;
    }

    /**
     * Gets the current height of the welcome image label.
     * Used by the controller to determine appropriate scaling for the welcome image.
     * @return The height of the welcome image label if it's showing and has a positive height, otherwise 0.
     */
    public int getWelcomeImageLabelHeight() {
        return (welcomeImageLabel != null && welcomeImageLabel.isShowing() && welcomeImageLabel.getHeight() > 1) ? welcomeImageLabel.getHeight() : 0;
    }

    /**
     * Gets the current width of the report image label.
     * Used by the controller to determine appropriate scaling for the report image.
     * @return The width of the report image label if it's showing and has a positive width, otherwise 0.
     */
    public int getReportImageLabelWidth() {
        return (reportImageLabel != null && reportImageLabel.isShowing() && reportImageLabel.getWidth() > 1) ? reportImageLabel.getWidth() : 0;
    }

    /**
     * Gets the current height of the report image label.
     * Used by the controller to determine appropriate scaling for the report image.
     * @return The height of the report image label if it's showing and has a positive height, otherwise 0.
     */
    public int getReportImageLabelHeight() {
        return (reportImageLabel != null && reportImageLabel.isShowing() && reportImageLabel.getHeight() > 1) ? reportImageLabel.getHeight() : 0;
    }

    /**
     * Initializes all UI components (labels, buttons, combo box, table, panels).
     * Sets fonts, colors, alignment, and other visual properties.
     * Action listeners for buttons are also set up here.
     */
    private void initComponents() {
        titleLabel = new JLabel(" ", SwingConstants.CENTER); // Default text, will be updated
        titleLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 28));
        titleLabel.setForeground(new Color(255, 255, 255, 230)); // Semi-transparent white
        titleLabel.setBorder(new EmptyBorder(15, 0, 20, 0));
        // Custom UI for a subtle text shadow effect
        titleLabel.setUI(new javax.swing.plaf.basic.BasicLabelUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                String text = ((JLabel) c).getText();
                FontMetrics fm = g2.getFontMetrics();
                int x = (c.getWidth() - fm.stringWidth(text)) / 2;
                int y = (c.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(0,0,0,80)); // Shadow color
                g2.drawString(text, x + 1, y + 1);
                g2.setColor(c.getForeground()); // Actual text color
                g2.drawString(text, x, y);
                g2.dispose();
            }
        });

        welcomeImageLabel = new JLabel();
        welcomeImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeImageLabel.setVerticalAlignment(SwingConstants.CENTER);

        reportImageLabel = new JLabel();
        reportImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        reportImageLabel.setVerticalAlignment(SwingConstants.TOP); // Align image to the top

        descriptionArea = new JTextArea(" "); // Default text
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        descriptionArea.setForeground(new Color(15, 15, 35)); // Dark text color
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setEditable(false);
        descriptionArea.setOpaque(false); // Transparent background
        descriptionArea.setBorder(new EmptyBorder(5, 10, 5, 10));

        // Populate month combo box
        String[] monthNames = new String[12];
        for (int i = 0; i < 12; i++) {
            monthNames[i] = Month.of(i + 1).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        }
        monthComboBox = new JComboBox<>(monthNames);
        monthComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        monthComboBox.setPreferredSize(new Dimension(180, 35));
        monthComboBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1); // Default to current month
        monthComboBox.setBackground(Color.WHITE);
        monthComboBox.setForeground(new Color(50,50,80));

        revealButton = new JButton("Reveal My Spending Star!");
        styleButton(revealButton, UNIFIED_BTN_START, UNIFIED_BTN_END);
        revealButton.addActionListener(e -> {
            if (controller != null) {
                int selectedMonthIndex = monthComboBox.getSelectedIndex() + 1; // Month is 1-indexed
                controller.processHoroscopeRequest(selectedMonthIndex);
            } else { showError("Horoscope feature unavailable. Controller not initialized."); }
        });

        backButton = new JButton("Try Another Month");
        styleButton(backButton, UNIFIED_BTN_START, UNIFIED_BTN_END);
        backButton.addActionListener(e -> {
            if (controller != null) {
                controller.onBackClicked();
            } else { showError("Horoscope feature unavailable. Controller not initialized.");}
        });

        // Setup for transactions table
        transactionTableModel = new DefaultTableModel(new Object[]{"Date", "Category/Type", "Amount (¥)"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; } // Non-editable cells
        };
        transactionsTable = new JTable(transactionTableModel);
        transactionsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        transactionsTable.setRowHeight(20);
        transactionsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        transactionsTable.setFillsViewportHeight(true); // Table uses entire height of scroll pane
        TableColumnModel columnModel = transactionsTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(70);
        columnModel.getColumn(1).setPreferredWidth(130);
        columnModel.getColumn(2).setPreferredWidth(60);
        transactionsTable.setOpaque(false); // Transparent table
        ((JComponent)transactionsTable.getDefaultRenderer(Object.class)).setOpaque(false); // Transparent cells
        transactionsTable.setShowGrid(true);
        transactionsTable.setGridColor(new Color(170, 180, 200)); // Light grid color

        transactionScrollPane = new JScrollPane(transactionsTable);
        transactionScrollPane.setOpaque(false); // Transparent scroll pane
        transactionScrollPane.getViewport().setOpaque(false); // Transparent viewport
        transactionScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(new Color(170,180,210), new Color(190,200,220)),
                "Selected Month's Expenses", // Default title
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI Semibold", Font.PLAIN, 11),
                new Color(40,50,80)
        ));

        contentCardLayout = new CardLayout();
        contentCardPanel = new JPanel(contentCardLayout);
        contentCardPanel.setOpaque(false); // Transparent card panel
    }

    /**
     * Applies a consistent style to a JButton, including gradient background,
     * font, foreground color, cursor, and hover/pressed effects.
     *
     * @param button     The JButton to style.
     * @param baseColor1 The starting color of the gradient for the button's normal state.
     * @param baseColor2 The ending color of the gradient for the button's normal state.
     */
    private void styleButton(JButton button, Color baseColor1, Color baseColor2) {
        button.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25)); // Padding
        button.setContentAreaFilled(false); // Required for custom painting
        button.setOpaque(false);

        // Define colors for hover and pressed states based on base colors
        Color hoverColor1 = baseColor1.brighter();
        Color hoverColor2 = baseColor2.brighter();
        Color pressedColor1 = baseColor1.darker();
        Color pressedColor2 = baseColor2.darker();

        // Custom UI for gradient painting
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                ButtonModel model = b.getModel();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = c.getWidth(); int h = c.getHeight();
                GradientPaint gp;
                // Choose gradient based on button state
                if (model.isPressed()) {
                    gp = new GradientPaint(0, 0, pressedColor1, w, 0, pressedColor2, false);
                } else if (model.isRollover()) {
                    gp = new GradientPaint(0, 0, hoverColor1, w, 0, hoverColor2, false);
                } else {
                    gp = new GradientPaint(0, 0, baseColor1, w, 0, baseColor2, false);
                }
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 30, 30); // Rounded rectangle
                g2.dispose();
                super.paint(g, c); // Paint text and icon
            }
        });
        // Repaint on mouse enter/exit to update hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.repaint(); }
            @Override public void mouseExited(MouseEvent e) { button.repaint(); }
        });
    }

    /**
     * Lays out the UI components within their respective panels (welcome and report)
     * using {@link GridBagLayout}. Also adds these panels to the main content card panel.
     */
    private void layoutComponents() {
        // --- Welcome Panel ---
        welcomePanel = new JPanel(new GridBagLayout());
        welcomePanel.setOpaque(false);
        welcomePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints welGbc = new GridBagConstraints();

        // Welcome Image - takes most space
        welGbc.gridx = 0; welGbc.gridy = 0;
        welGbc.weightx = 1.0; // Fill horizontal space
        welGbc.weighty = 0.85; // Takes 85% of vertical space
        welGbc.fill = GridBagConstraints.BOTH;
        welGbc.insets = new Insets(0, 0, 10, 0); // Bottom margin
        welcomePanel.add(welcomeImageLabel, welGbc);

        // Welcome Text Label
        JLabel welcomeTextLabel = new JLabel("Select a month and reveal your Spending Star!", SwingConstants.CENTER);
        welcomeTextLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 18));
        welcomeTextLabel.setForeground(new Color(250,250,250, 210)); // Semi-transparent white
        welGbc.gridy = 1; // Next row
        welGbc.weighty = 0.0; // Minimal vertical space
        welGbc.fill = GridBagConstraints.HORIZONTAL;
        welGbc.insets = new Insets(5, 0, 8, 0);
        welcomePanel.add(welcomeTextLabel, welGbc);

        // Month Selection Panel (holds label and combo box)
        JPanel monthSelectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        monthSelectionPanel.setOpaque(false);
        JLabel monthLabel = new JLabel("Month:");
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        monthLabel.setForeground(new Color(250,250,250, 200));
        monthSelectionPanel.add(monthLabel);
        monthSelectionPanel.add(monthComboBox);
        welGbc.gridy = 2;
        welGbc.weighty = 0.05;
        welGbc.fill = GridBagConstraints.HORIZONTAL;
        welGbc.anchor = GridBagConstraints.CENTER;
        welGbc.insets = new Insets(5, 0, 10, 0);
        welcomePanel.add(monthSelectionPanel, welGbc);

        // Reveal Button
        welGbc.gridy = 3;
        welGbc.weighty = 0.10;
        welGbc.fill = GridBagConstraints.NONE; // Don't stretch button
        welGbc.anchor = GridBagConstraints.CENTER;
        welGbc.insets = new Insets(5, 0, 0, 0);
        welcomePanel.add(revealButton, welGbc);


        // --- Report Panel ---
        reportPanel = new JPanel(new GridBagLayout());
        reportPanel.setOpaque(false);
        reportPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        GridBagConstraints repGbc = new GridBagConstraints();

        // Report Title
        repGbc.gridx = 0; repGbc.gridy = 0; repGbc.gridwidth = 2; // Span 2 columns
        repGbc.weightx = 1.0; repGbc.weighty = 0.0;
        repGbc.fill = GridBagConstraints.HORIZONTAL;
        repGbc.anchor = GridBagConstraints.PAGE_START;
        repGbc.insets = new Insets(0, 0, 20, 0); // Bottom margin for spacing
        reportPanel.add(titleLabel, repGbc);

        // Report Image (Left side)
        repGbc.gridx = 0; repGbc.gridy = 1; repGbc.gridwidth = 1; // Reset gridwidth
        repGbc.weightx = 0.60; // Image takes 60% of horizontal space in its row
        repGbc.weighty = 0.60; // Image/Text row takes 60% of vertical space
        repGbc.fill = GridBagConstraints.BOTH;
        repGbc.anchor = GridBagConstraints.NORTHWEST; // Align to top-left
        repGbc.insets = new Insets(0, 0, 10, 5); // Margin: bottom, right
        reportPanel.add(reportImageLabel, repGbc);

        // Description ScrollPane (Right side)
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        descriptionScrollPane.setBorder(BorderFactory.createEmptyBorder()); // No border for scroll pane itself
        descriptionScrollPane.getViewport().setOpaque(false); // Transparent viewport
        descriptionScrollPane.setOpaque(false); // Transparent scroll pane
        descriptionScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        descriptionScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        repGbc.gridx = 1; repGbc.gridy = 1; // Next column, same row
        repGbc.weightx = 0.40; // Text takes 40% of horizontal space
        repGbc.weighty = 0.60; // Match image row's vertical weight
        repGbc.fill = GridBagConstraints.BOTH;
        repGbc.anchor = GridBagConstraints.NORTHWEST;
        repGbc.insets = new Insets(25, 0, 10, 5); // Margin: top(push down), bottom, right
        reportPanel.add(descriptionScrollPane, repGbc);

        // Transaction Table
        repGbc.gridx = 0; repGbc.gridy = 2; repGbc.gridwidth = 2; // Span 2 columns
        repGbc.weightx = 1.0;
        repGbc.weighty = 0.30; // Table takes 30% of vertical space
        repGbc.fill = GridBagConstraints.BOTH;
        repGbc.insets = new Insets(10, 0, 10, 0); // Top/bottom margin
        reportPanel.add(transactionScrollPane, repGbc);

        // Report Buttons Panel (holds back button)
        JPanel reportButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        reportButtonPanel.setOpaque(false);
        reportButtonPanel.add(backButton);
        repGbc.gridy = 3; repGbc.weighty = 0.10; // Buttons take 10% of vertical space
        repGbc.fill = GridBagConstraints.NONE; // Don't stretch panel
        repGbc.anchor = GridBagConstraints.CENTER;
        repGbc.insets = new Insets(10, 0, 5, 0);
        reportPanel.add(reportButtonPanel, repGbc);

        // Add individual panels to the card layout panel
        contentCardPanel.add(welcomePanel, WELCOME_CARD);
        contentCardPanel.add(reportPanel, REPORT_CARD);
        add(contentCardPanel, BorderLayout.CENTER); // Add card panel to the main panel
    }


    /**
     * Sets the welcome image on the {@code welcomeImageLabel}.
     * If the provided icon is null, a placeholder text indicating failure to load is set.
     *
     * @param icon The {@link ImageIcon} to display, or null if loading failed.
     */
    public void setWelcomeImage(ImageIcon icon) {
        if (welcomeImageLabel == null) return; // Safety check
        welcomeImageLabel.setIcon(icon);
        // Set placeholder text if icon is null
        welcomeImageLabel.setText(icon == null ? "<html><body style='text-align: center; color:#FF3366; padding:20px; font-family: Segoe UI; font-size: 12pt;'>" +
                "Welcome image could not be loaded.<br>Please ensure images are in 'src/test/horoscope/'.</body></html>" : null);
        welcomeImageLabel.revalidate();
        welcomeImageLabel.repaint();
    }

    /**
     * Updates the report panel with the provided data.
     * Sets the title, description, report image, and populates the transactions table.
     * Switches the view to the report card.
     *
     * @param title         The title for the report.
     * @param description   The descriptive text for the report.
     * @param icon          The {@link ImageIcon} for the report, or null if not available/failed.
     * @param transactions  A list of {@link Transaction} objects for the selected month.
     * @param selectedMonth The month (1-12) for which the report is generated, used to title the transaction table.
     */
    public void setReportData(String title, String description, ImageIcon icon, List<Transaction> transactions, int selectedMonth) {
        // Safety check for critical UI components
        if (titleLabel == null || descriptionArea == null || reportImageLabel == null || transactionTableModel == null || contentCardLayout == null || contentCardPanel == null) {
            System.err.println("HoroscopePanel: UI components not ready for setReportData call.");
            showError("Failed to display report due to an internal UI issue.");
            return;
        }

        titleLabel.setText(title != null ? title : "Monthly Spending Star");
        descriptionArea.setText(description != null ? description : "No specific insights this month.");
        descriptionArea.setCaretPosition(0); // Scroll to top

        reportImageLabel.setIcon(icon);
        if (icon == null) {
            // Provide more contextual placeholder text if icon is null
            // Check if it's the "NO_DATA_TITLE" from HoroscopeService (via controller/model)
            // This requires NO_DATA_TITLE to be accessible or passed through
            if (title != null && title.equals(Service.HoroscopeService.NO_DATA_TITLE)) { // Assuming access to this constant somehow or passed via report model
                reportImageLabel.setText("<html><body style='text-align: center; color:#555555; padding:10px; font-family: Segoe UI; font-size: 10pt;'>" +
                        Service.HoroscopeService.NO_DATA_DESCRIPTION + "</body></html>");
            } else {
                reportImageLabel.setText("<html><body style='text-align: center; color:#D32F2F; padding:10px; font-family: Segoe UI; font-size: 10pt;'>" +
                        "Report Image Not Available</body></html>");
            }
        } else {
            reportImageLabel.setText(null); // Clear any placeholder text if icon is present
        }
        reportImageLabel.setVerticalAlignment(SwingConstants.TOP); // Ensure alignment

        // Clear and populate transactions table
        transactionTableModel.setRowCount(0); // Clear previous data
        if (transactions != null) {
            for (Transaction tx : transactions) {
                String formattedDate = "N/A";
                if (tx.getTimestamp() != null) {
                    try {
                        // Basic substring formatting, assumes YYYY-MM-DD... format
                        if (tx.getTimestamp().length() >= 10) {
                            formattedDate = tx.getTimestamp().substring(0, 10);
                        } else {
                            formattedDate = tx.getTimestamp(); // Use as is if too short
                        }
                    } catch (Exception e) { /* ignore formatting errors, keep "N/A" or original */ }
                }
                transactionTableModel.addRow(new Object[]{
                        formattedDate,
                        tx.getType() != null ? tx.getType() : "Unknown",
                        String.format("%.2f", tx.getAmount()) // Format amount to 2 decimal places
                });
            }
        }
        // Update transaction table title
        String monthName = Month.of(selectedMonth).getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH);
        int reportYear = LocalDate.now().getYear(); // Assume current year
        transactionScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(new Color(170,180,210), new Color(190,200,220)),
                monthName + " " + reportYear + " Expenses", // Dynamic title
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI Semibold", Font.PLAIN, 11),
                new Color(40,50,80)
        ));

        contentCardLayout.show(contentCardPanel, REPORT_CARD); // Switch to report view
        revalidate();
        repaint();
    }

    /**
     * Sets the loading state of the UI.
     * Disables/enables interactive components (buttons, combo box) and updates button text
     * to indicate whether an operation is in progress.
     * Ensures execution on the Event Dispatch Thread.
     *
     * @param isLoading {@code true} if loading is in progress, {@code false} otherwise.
     */
    public void setLoadingState(boolean isLoading) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> setLoadingState(isLoading)); // Ensure on EDT
            return;
        }
        if (revealButton == null || backButton == null || monthComboBox == null) return; // Safety check

        revealButton.setEnabled(!isLoading);
        backButton.setEnabled(!isLoading);
        monthComboBox.setEnabled(!isLoading);
        revealButton.setText(isLoading ? "Consulting Stars..." : "Reveal My Spending Star!");
    }

    /**
     * Displays an error message dialog to the user.
     * Ensures the dialog is shown on the Event Dispatch Thread.
     *
     * @param message The error message to display.
     */
    public void showError(String message) {
        String title = "Horoscope Error";
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(HoroscopePanel.this, message, title, JOptionPane.ERROR_MESSAGE));
        } else {
            JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Switches the view to the welcome card.
     * Also triggers a reload of the welcome image via the controller.
     * Ensures UI updates related to image loading happen on the EDT.
     */
    public void showWelcomeCard() {
        if (contentCardLayout == null || contentCardPanel == null) return; // Safety check
        contentCardLayout.show(contentCardPanel, WELCOME_CARD);
        // Reload welcome image when returning to welcome card
        SwingUtilities.invokeLater(() -> { // Ensure controller access is on EDT if it interacts with UI
            if (controller != null) {
                if (welcomeImageLabel.getWidth() > 0 && welcomeImageLabel.getHeight() > 0) {
                    controller.loadWelcomeImage(welcomeImageLabel.getWidth(), welcomeImageLabel.getHeight());
                } else {
                    controller.loadWelcomeImage(0,0); // Let controller use fallback
                }
            }
        });
    }
}