package UI;

import AccountModel.TransactionService;
// import AccountModel.TransactionService.TransactionData; // Not used directly in this snippet of Code 1
import AccountModel.UserSession;
// import PersonModel.ExpenseDialog; // Not used directly in this snippet of Code 1
// import PersonModel.IncomeDialog; // Not used directly in this snippet of Code 1
import PersonModel.IncomeExpenseChart;
import UI.AccountManagementUI.RoundBorder;
// --- Added Import for the new panel ---
import UI.HoroscopePanel; // <<< MODIFICATION: Added import for the new panel (assuming package UI)

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer; // Not used directly in this snippet of Code 1
import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;
import java.util.List; // Explicit import for List

import UI.RoundedInputField.*; // Assuming GradientLabel is here as per Code 1 reference in createSidebar

// import static PersonModel.IncomeExpenseChart.filterByYearMonth; // Not used directly in this snippet of Code 1


public class PersonalMainPlane extends JFrame {
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private String username;
    private Component chartPanel; // Kept from Code 1
    // Using java.util.List explicitly as in Code 1
    private final java.util.List<NavItemPanel> navItems = new java.util.ArrayList<>();


    public PersonalMainPlane(String username) {
        this.username = username;
        setTitle("Smart Finance - Personal Dashboard");
        setSize(1920, 1080); // Keep original size from Code 1
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        add(mainPanel);

        JPanel sidebar = createSidebar();
        mainPanel.add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        initializeContentPanels();
        // Keep original default view behavior from Code 1
        // Code 1 *did* explicitly show "Personal Center" in the provided snippet, let's retain that
        cardLayout.show(contentPanel, "Personal Center");

        // Ensure the corresponding nav item is selected if "Personal Center" is default
        // This part might need adjustment based on how selection was originally intended in Code 1,
        // but we'll add basic selection sync based on the explicit show() call above.
        for(NavItemPanel item : navItems) {
            if(item.name.equals("Personal Center")) {
                item.setSelected(true);
            } else {
                // Ensure others aren't selected by default if logic wasn't present
                // Code 1's NavItemPanel did handle setting others to false on click,
                // but initial state needs care. Let's assume only one selected initially.
                // If the default wasn't Personal Center, the first item selection below would apply.
                // item.setSelected(false); // Potentially redundant if default selection below is active
            }
        }
        // Code 1 had default selection for the *first* item in navItems list in createSidebar
        // If Personal Center wasn't explicitly shown/selected, this would apply.
        // Let's keep it but commented out if Personal Center default is primary.
        // if (!navItems.isEmpty() && !navItems.get(0).name.equals("Personal Center")) {
        //    navItems.get(0).setSelected(true);
        // }


        setVisible(true);
    }

    // createSidebar remains EXACTLY as in Code One, EXCEPT for adding the new option
    private JPanel createSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(Color.WHITE);
        sb.setPreferredSize(new Dimension(200, 0)); // Code 1 dimension

        // Use GradientLabel from RoundedInputField as referenced in Code 1
        RoundedInputField.GradientLabel logo = new RoundedInputField.GradientLabel("Smart Finance");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20)); // Code 1 font
        logo.setBorder(new EmptyBorder(20, 0, 10, 0)); // Code 1 border
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        sb.add(logo);

        // Code 1's Welcome Panel logic
        NavItemPanel welcomePanel = new NavItemPanel("Welcome, " + username);
        welcomePanel.setIconText("U");
        // Disable click events as per Code 1
        for (MouseListener ml : welcomePanel.getMouseListeners()) {
            welcomePanel.removeMouseListener(ml);
        }
        welcomePanel.setCursor(Cursor.getDefaultCursor());
        welcomePanel.setBackground(null); // Code 1 setting

        // Add to sidebar, but NOT to navItems list used for navigation selection/filtering
        // Correction: Code 1 *did* add welcomePanel to navItems, affecting search filter. Retain this.
        navItems.add(welcomePanel);
        sb.add(welcomePanel);
        sb.add(Box.createRigidArea(new Dimension(0, 8))); // Code 1 spacing


        // 2) Search box - Code 1 style
        JTextField search = new JTextField();
        styleSearchField(search, "Search..."); // Code 1 placeholder and style method
        sb.add(search);
        sb.add(Box.createRigidArea(new Dimension(0, 16))); // Code 1 spacing

        // 3) Menu items - Code 1 options + new one
        String[] options = {
                "Transaction System",
                "Bill Statistics",
                "Personal Center",
                "Budget Management",
                "AI Assistant",
                "Spending Star Whispers" // <<< MODIFICATION: Added new option string
        };

        // Clear navItems *before* adding specific navigation items (excluding welcome/logout if needed)
        // Let's refine: Add only navigable items to navItems list used for selection logic.
        // Code 1 added *all* NavItemPanels (including welcome) to navItems. Let's stick to that.
        // No, Code 1 added Welcome separately, then looped options. Let's keep that structure.
        // However, navItems list was used in search filter and selection logic in Code 1.
        // Let's keep adding all created items to navItems as Code 1 did.
        //navItems.clear(); // Don't clear if welcome was already added

        for (String opt : options) {
            NavItemPanel item = new NavItemPanel(opt);
            navItems.add(item); // Add to the list used for selection/filtering
            sb.add(item);
            sb.add(Box.createRigidArea(new Dimension(0, 8))); // Code 1 spacing
        }
        // Default selection logic moved to constructor for clarity with explicit show()

        sb.add(Box.createVerticalGlue());

        // 4) Logout - Code 1 style
        NavItemPanel logout = new NavItemPanel("Logout");
        logout.setIconText("U"); // Code 1 method call
        logout.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                        PersonalMainPlane.this,
                        "Are you sure you want to log out?",
                        "Logout Confirmation",
                        JOptionPane.YES_NO_OPTION
                );
                if (choice == JOptionPane.YES_OPTION) {
                    UserSession.clearSession();
                    dispose();
                }
            }
        });
        navItems.add(logout); // Add logout to list as in Code 1
        sb.add(logout);
        sb.add(Box.createRigidArea(new Dimension(0, 20))); // Code 1 spacing

        // Search filter logic - Code 1 exact implementation
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                String text = search.getText().trim().toLowerCase();
                for (NavItemPanel it : navItems) {
                    // Code 1 logic included filtering Welcome and Logout items
                    it.setVisible(it.name.toLowerCase().contains(text));
                }
                sb.revalidate(); sb.repaint();
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        welcomePanel.setSelected(true);


        return sb;
    }

    // styleSearchField remains EXACTLY as in Code One
    private void styleSearchField(JTextField tf, String placeholder) {
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBackground(new Color(240, 240, 240));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(17, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        tf.setText(placeholder);
        tf.setForeground(Color.GRAY);
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(Color.DARK_GRAY);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) {
                    tf.setText(placeholder);
                    tf.setForeground(Color.GRAY);
                }
            }
        });
    }

    // NavItemPanel inner class remains EXACTLY as in Code One
    class NavItemPanel extends JPanel {
        private JLabel iconLabel, textLabel;
        private boolean selected = false;
        private Color start = new Color(156, 39, 176), end = new Color(0, 47, 167);
        private String name;

        public NavItemPanel(String name) {
            this.name = name;
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setOpaque(false);
            setPreferredSize(new Dimension(Short.MAX_VALUE, 60));  // 高度 = 60，可调整
            setBorder(new EmptyBorder(0, 12, 0, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if ("Logout".equals(name) || ("Welcome, " + username).equals(name)) {
                iconLabel = new CircleIcon("U");
            } else {
                iconLabel = new JLabel("\u25CF");
                iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                iconLabel.setForeground(new Color(150, 150, 150));
            }

            textLabel = new JLabel(name);
            textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            textLabel.setBorder(new EmptyBorder(0, 8, 0, 0));
            textLabel.setForeground(new Color(100, 100, 100));

            add(iconLabel);
            add(textLabel);
            add(Box.createHorizontalGlue());

            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    cardLayout.show(contentPanel, name);
                    navItems.forEach(it -> it.setSelected(it == NavItemPanel.this));
                }

                @Override public void mouseEntered(MouseEvent e) {
                    if (!selected) setBackground(new Color(245, 245, 245));
                }

                @Override public void mouseExited(MouseEvent e) {
                    if (!selected) setBackground(null);
                }
            });
        }

        /** Set custom round icon and text for Logout **/
        public void setIconText(String s) {
            iconLabel.setText(s);
            iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            iconLabel.setForeground(Color.WHITE);
            iconLabel.setOpaque(false);
        }

        public void setSelected(boolean sel) {
            selected = sel;
            if (sel) {
                textLabel.setForeground(Color.WHITE);
                iconLabel.setForeground(Color.WHITE);
            } else {
                textLabel.setForeground(new Color(100, 100, 100));
                iconLabel.setForeground(new Color(150, 150, 150));
                setBackground(null);
            }
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            if (selected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, start, getWidth(), 0, end));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
    // CircleIcon inner class remains EXACTLY as in Code One
    public class CircleIcon extends JLabel {
        private Color color1 = new Color(156, 39, 176); // Purple
        private Color color2 = new Color(0, 47, 167);   // Blue

        public CircleIcon(String text) {
            super(text, SwingConstants.CENTER);
            setPreferredSize(new Dimension(30, 30)); // Code 1 size
            setFont(new Font("Segoe UI", Font.BOLD, 14)); // Code 1 font
            setForeground(Color.ORANGE); // Code 1 foreground (Orange)
            // Code 1 didn't explicitly setOpaque(false), but likely needed for custom paint
             setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            // Code 1 centering logic
            int size = Math.min(getWidth(), getHeight());
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            // Code 1 gradient and drawing
            GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
            g2.setPaint(gp);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.fillOval(x, y, size, size);

            g2.dispose();
            // Call super AFTER drawing background circle
            super.paintComponent(g);
        }
    }


    // createMenuItem remains EXACTLY as in Code One (even if unused)
    private JLabel createMenuItem(String text) {
        JLabel menuItem = new JLabel(text);
        menuItem.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        menuItem.setForeground(new Color(50, 50, 50));
        menuItem.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuItem.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        menuItem.setOpaque(true);
        menuItem.setBackground(new Color(245, 245, 245));
        menuItem.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (!"Logout".equals(text)) {
            menuItem.addMouseListener(new MouseAdapter() {
                Color originalBg = menuItem.getBackground();
                Color hoverBg = new Color(230, 230, 230);
                Color selectedBg = new Color(200, 200, 200);

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!menuItem.getBackground().equals(selectedBg)) {
                        menuItem.setBackground(hoverBg);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!menuItem.getBackground().equals(selectedBg)) {
                        menuItem.setBackground(originalBg);
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    cardLayout.show(contentPanel, text);
                    // Code 1 selection update logic
                    Component[] components = menuItem.getParent().getComponents();
                    for (Component comp : components) {
                        if (comp instanceof JLabel) {
                            if (comp == menuItem) {
                                comp.setBackground(selectedBg);
                            } else {
                                if (comp.getBackground().equals(selectedBg) || comp.getBackground().equals(hoverBg)) {
                                    // Simple reset logic from Code 1
                                    // Note: This logic is likely superseded by NavItemPanel's handling
                                    // if createMenuItem wasn't actually used for main nav.
                                    comp.setBackground(originalBg);
                                }
                            }
                        }
                    }
                    // Refined selection logic block from Code 1 (keep it)
                     Component[] allComponents = menuItem.getParent().getComponents();
                     for(Component comp : allComponents) {
                           if (comp instanceof JLabel) {
                                boolean isNavigable = false;
                                if(comp.getMouseListeners().length > 0 && !((JLabel)comp).getText().startsWith("Welcome") && !((JLabel)comp).getText().equals("Smart Finance") && ((JLabel)comp).getIcon() == null ) {
                                     isNavigable = true;
                                }

                                if(isNavigable && !"Logout".equals(((JLabel)comp).getText())) {
                                     if(comp == menuItem) {
                                         comp.setBackground(selectedBg);
                                     } else {
                                         comp.setBackground(originalBg); // Reset others
                                     }
                                }
                           }
                     }
                }
            });
        } else {
            // Code 1 MouseAdapter logic for Logout (hover only)
            menuItem.addMouseListener(new MouseAdapter() {
                Color originalBg = menuItem.getBackground();
                Color hoverBg = new Color(230, 230, 230);

                @Override
                public void mouseEntered(MouseEvent e) {
                    menuItem.setBackground(hoverBg);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    menuItem.setBackground(originalBg);
                }
                // Click handled separately in Code 1
            });
        }

        return menuItem;
    }

    // initializeContentPanels MODIFIED to include HoroscopePanel
    private void initializeContentPanels() {
        // Code 1 panel names array + new one
        String[] panelNames = {
                "Transaction System",
                "Bill Statistics",
                "Personal Center",
                "Budget Management", // Keep original handling (placeholder or actual)
                "AI Assistant",
                "Spending Star Whispers" // <<< MODIFICATION: Added new panel name
        };

        for (String name : panelNames) {
            JPanel panel;
            // Code 1's logic for creating panels
            if (name.equals("AI Assistant")) {
                panel = new AIPanel(); // Code 1 used AIPanel
            } else if (name.equals("Transaction System")) {
                // Code 1 used RefactoredTransactionUI
                panel = new RefactoredTransactionUI(username);
            } else if (name.equals("Bill Statistics")) {
                // Code 1 called createBillStatisticsPanel()
                panel = createBillStatisticsPanel();
            } else if (name.equals("Personal Center")) {
                // Code 1 used PersonalCenterPanel
                panel = new PersonalCenterPanel(username);
            } else if (name.equals("Budget Management")) {
                // Code 1 integrated BudgetManagementPanel in the provided snippet
                // Keep this integration logic
                panel = new BudgetManagementPanel(username);
            } else if (name.equals("Spending Star Whispers")) { // <<< MODIFICATION: Add case for new panel
                // Instantiate the new panel, assuming it takes username
                panel = new HoroscopePanel(username);
            } else {
                // Fallback from Code 1
                System.err.println("Warning: Unhandled panel name in initializeContentPanels: " + name);
                panel = createPlaceholderPanel(name);
            }
            // Add panel using name as key (Code 1 logic)
            contentPanel.add(panel, name);
            // DO NOT add panel.setName(name) - stick to Code 1 structure
        }
    }

    // createBillStatisticsPanel remains EXACTLY as in Code One
    private JPanel createBillStatisticsPanel() {
        // Use GradientPanel as defined/used in Code 1
        JPanel panel = new GradientPanel(); // Code 1 used GradientPanel
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Bill Statistics", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24)); // Code 1 style
        title.setForeground(Color.WHITE); // Code 1 color
        panel.add(title, BorderLayout.NORTH);

        // Code 1 logic for chart creation and date selection
        java.util.Calendar cal = java.util.Calendar.getInstance();
        String currentYearMonth = String.format("%d/%02d", cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1);

        try {
            // Code 1 used IncomeExpenseChart.getIncomeExpensePlane
            // Ensure chartPanel is treated as Component
            chartPanel = IncomeExpenseChart.getIncomeExpensePlane(username, currentYearMonth);
            panel.add(chartPanel, BorderLayout.CENTER);
        } catch (Exception e) {
            e.printStackTrace();
            JLabel errorLabel = new JLabel("加载图表失败: " + e.getMessage(), SwingConstants.CENTER);
            errorLabel.setForeground(Color.RED);
            panel.add(errorLabel, BorderLayout.CENTER);
            chartPanel = errorLabel; // Store error label as the component
        }

        // Code 1 selector panel and components
        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        selectorPanel.setBackground(Color.WHITE); // Code 1 background

        JLabel yearLabel = new JLabel("Year:");
        yearLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Code 1 style
        yearLabel.setForeground(new Color(50, 50, 50)); // Code 1 color

        // Year ComboBox - Code 1 style
        JComboBox<Integer> yearComboBox = new JComboBox<>();
        int currentYear = cal.get(Calendar.YEAR);
        for (int i = currentYear - 5; i <= currentYear + 5; i++) {
            yearComboBox.addItem(i);
        }
        yearComboBox.setSelectedItem(currentYear);
        yearComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Code 1 font

        // Code 1 custom UI for ComboBox
        yearComboBox.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton btn = super.createArrowButton();
                btn.setOpaque(false);
                btn.setContentAreaFilled(false);
                btn.setBorder(null);
                return btn;
            }
            @Override public void installUI(JComponent c) {
                super.installUI(c);
                comboBox.setOpaque(false);
                arrowButton.setOpaque(false);
            }
        });
        yearComboBox.setOpaque(false);
        yearComboBox.setBackground(Color.WHITE);
        // Code 1 used AccountManagementUI.GradientBorder
        yearComboBox.setBorder(new AccountManagementUI.GradientBorder(2, 16));

        // Code 1 custom Renderer for ComboBox
        yearComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    lbl.setBackground(new Color(100,149,237)); // Code 1 selected color
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(new Color(50,50,50)); // Code 1 default color
                }
                lbl.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10)); // Code 1 padding
                return lbl;
            }
        });

        // Month ComboBox - Code 1 definition and styling
        String[] months = { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" };
        JLabel monthLabel = new JLabel("Month:");
        monthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Code 1 style
        monthLabel.setForeground(new Color(50, 50, 50)); // Code 1 color
        JComboBox<String> monthComboBox = new JComboBox<>(months);
        monthComboBox.setSelectedItem(String.format("%02d", cal.get(Calendar.MONTH)+1));
        monthComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Code 1 font
        monthComboBox.setUI(new BasicComboBoxUI() { // Code 1 custom UI
            @Override protected JButton createArrowButton() { /* As above */
                JButton btn = super.createArrowButton(); btn.setOpaque(false); btn.setContentAreaFilled(false); btn.setBorder(null); return btn;
            }
            @Override public void installUI(JComponent c) { /* As above */
                 super.installUI(c); comboBox.setOpaque(false); arrowButton.setOpaque(false);
            }
        });
        monthComboBox.setOpaque(false);
        monthComboBox.setBackground(Color.WHITE);
        monthComboBox.setBorder(new AccountManagementUI.GradientBorder(2, 16)); // Code 1 border
        monthComboBox.setRenderer(new DefaultListCellRenderer() { // Code 1 renderer
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                 JLabel lbl = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                 if (isSelected) { /* As above */
                     lbl.setBackground(new Color(100,149,237)); lbl.setForeground(Color.WHITE);
                 } else { /* As above */
                     lbl.setBackground(Color.WHITE); lbl.setForeground(new Color(50,50,50));
                 }
                 lbl.setBorder(BorderFactory.createEmptyBorder(2,10,2,10)); // Code 1 padding
                 return lbl;
            }
        });

        // Add components to selector panel - Code 1 order
        selectorPanel.add(yearLabel);
        selectorPanel.add(yearComboBox);
        selectorPanel.add(monthLabel);
        selectorPanel.add(monthComboBox);

        panel.add(selectorPanel, BorderLayout.SOUTH);

        // Update Listener - Code 1 logic
        ActionListener updateListener = e -> {
            try {
                String selectedYearMonth = yearComboBox.getSelectedItem() + "/" + monthComboBox.getSelectedItem();
                // Code 1 used IncomeExpenseChart.getIncomeExpensePlane
                JPanel newChartPanel = IncomeExpenseChart.getIncomeExpensePlane(username, selectedYearMonth);

                // Remove OLD component using the instance variable chartPanel
                panel.remove(chartPanel); // chartPanel stores the component currently in CENTER

                // Update chartPanel instance variable and add NEW component
                chartPanel = newChartPanel;
                panel.add(chartPanel, BorderLayout.CENTER);

                panel.revalidate();
                panel.repaint();
            }
            catch (Exception ex) {
                ex.printStackTrace();
                JLabel errorLabel = new JLabel("更新图表失败: " + ex.getMessage(), SwingConstants.CENTER);
                errorLabel.setForeground(Color.RED);

                // Code 1 error handling: Remove old, add error, update instance var
                if(chartPanel != null && chartPanel.getParent() == panel) {
                    panel.remove(chartPanel);
                }
                panel.add(errorLabel, BorderLayout.CENTER);
                chartPanel = errorLabel; // Update instance var to the error label

                panel.revalidate();
                panel.repaint();
            }
        };
        yearComboBox.addActionListener(updateListener);
        monthComboBox.addActionListener(updateListener);

        return panel;
    }

    // createPlaceholderPanel remains EXACTLY as in Code One
    private JPanel createPlaceholderPanel(String name) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20), // Code 1 padding
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true))); // Code 1 border

        JLabel contentLabel = new JLabel("Content for " + name, SwingConstants.CENTER);
        contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24)); // Code 1 font
        contentLabel.setForeground(new Color(50, 50, 50)); // Code 1 color
        card.add(contentLabel, BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);

        return panel;
    }


    // main method remains EXACTLY as in Code One
    public static void main(String[] args) {
        String userId = UserSession.getCurrentUsername(); // Code 1 logic
        if (userId == null) {
            userId = "defaultUser"; // Code 1 fallback
        }
        String finalUserId = userId;
        SwingUtilities.invokeLater(() -> new PersonalMainPlane(finalUserId));
    }

    // Added GradientPanel class definition if it was missing from Code 1 snippet
    // but referenced in createBillStatisticsPanel()
    // If GradientPanel was defined elsewhere in Code 1's project, this is not needed.
    // Assuming it might be a simple panel with gradient paint:
    static class GradientPanel extends JPanel {
         private Color color1 = new Color(156, 39, 176); // Purple
         private Color color2 = new Color(0, 47, 167);   // Blue
         @Override
         protected void paintComponent(Graphics g) {
             super.paintComponent(g);
             Graphics2D g2d = (Graphics2D) g;
             g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
             GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
             g2d.setPaint(gp);
             g2d.fillRect(0, 0, getWidth(), getHeight());
         }
     }

}