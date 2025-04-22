package UI;


import AccountModel.TransactionService;
import AccountModel.TransactionService.TransactionData;
import AccountModel.UserSession;
import PersonModel.ExpenseDialog;
import PersonModel.IncomeDialog;
import PersonModel.IncomeExpenseChart;
import UI.AccountManagementUI.RoundBorder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;

import UI.RoundedInputField.*;

import static PersonModel.IncomeExpenseChart.filterByYearMonth;
// --- Added Import from Code Two for Budget Management ---


public class PersonalMainPlane extends JFrame {
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private String username;
    private Component chartPanel;
    private final java.util.List<NavItemPanel> navItems = new java.util.ArrayList<>();


    public PersonalMainPlane(String username) {
        this.username = username;
        setTitle("Smart Finance - Personal Dashboard");
        setSize(1920, 1080); // Keep original size
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
        cardLayout.show(contentPanel, "Personal Center"); // Keep original default view behavior (implicit or explicit)
        setVisible(true);
    }

    // createSidebar remains EXACTLY as in Code One
    private JPanel createSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(Color.WHITE);
        sb.setPreferredSize(new Dimension(200, 0));

        RoundedInputField.GradientLabel logo = new GradientLabel("Smart Finance");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setBorder(new EmptyBorder(20, 0, 10, 0));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        sb.add(logo);

        NavItemPanel welcomePanel = new NavItemPanel("Welcome, " + username);
        welcomePanel.setIconText("U");
// 禁用点击事件，不要响应菜单切换
        for (MouseListener ml : welcomePanel.getMouseListeners()) {
            welcomePanel.removeMouseListener(ml);
        }
        welcomePanel.setCursor(Cursor.getDefaultCursor());
        welcomePanel.setBackground(null);

// 添加到 sidebar
        navItems.add(welcomePanel);
        sb.add(welcomePanel);
        sb.add(Box.createRigidArea(new Dimension(0, 8)));


        // 2) 搜索框
        JTextField search = new JTextField();
        styleSearchField(search, "Search...");
        sb.add(search);
        sb.add(Box.createRigidArea(new Dimension(0, 16)));

        // 3) 菜单项
        String[] options = {
                "Transaction System",
                "Bill Statistics",
                "Personal Center",
                "Budget Management",
                "AI Assistant"
        };
        for (String opt : options) {
            NavItemPanel item = new NavItemPanel(opt);
            navItems.add(item);
            sb.add(item);
            sb.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        // 默认选中第一个
        if (!navItems.isEmpty()) navItems.get(0).setSelected(true);

        sb.add(Box.createVerticalGlue());

        // 4) Logout，左边圆形 U 图标
        NavItemPanel logout = new NavItemPanel("Logout");
        logout.setIconText("U");
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
        navItems.add(logout);
        sb.add(logout);
        sb.add(Box.createRigidArea(new Dimension(0, 20)));

        // 搜索联动过滤
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                String text = search.getText().trim().toLowerCase();
                for (NavItemPanel it : navItems) {
                    it.setVisible(it.name.toLowerCase().contains(text));
                }
                sb.revalidate(); sb.repaint();
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        return sb;
    }

    /** 样式化搜索框 **/
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

    /** 侧边栏项面板 **/
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

    public class CircleIcon extends JLabel {
        private Color color1 = new Color(156, 39, 176); // 紫
        private Color color2 = new Color(0, 47, 167);   // 蓝
        public CircleIcon(String text) {
            super(text, SwingConstants.CENTER);
            setPreferredSize(new Dimension(30, 30));
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(Color.ORANGE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            int size = Math.min(getWidth(), getHeight());
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
            g2.setPaint(gp);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.fillOval(x, y, size, size);

            g2.dispose();
            super.paintComponent(g);
        }

    }



    // createMenuItem remains EXACTLY as in Code One
    private JLabel createMenuItem(String text) {
        JLabel menuItem = new JLabel(text);
        menuItem.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        menuItem.setForeground(new Color(50, 50, 50));
        menuItem.setAlignmentX(Component.CENTER_ALIGNMENT); // Keep original alignment
        menuItem.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        menuItem.setOpaque(true);
        menuItem.setBackground(new Color(245, 245, 245));
        menuItem.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (!"Logout".equals(text)) {
            // Keep original MouseAdapter logic for navigation items from Code One
            menuItem.addMouseListener(new MouseAdapter() {
                Color originalBg = menuItem.getBackground();
                Color hoverBg = new Color(230, 230, 230);
                Color selectedBg = new Color(200, 200, 200); // Keep original selected color

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
                    // Keep original selection update logic from Code One
                    Component[] components = menuItem.getParent().getComponents();
                    for (Component comp : components) {
                        if (comp instanceof JLabel) {
                            if (comp == menuItem) {
                                comp.setBackground(selectedBg);
                            } else {
                                // Only reset background of other JLabels that might have been selected
                                // This simplistic approach from Code One is kept.
                                if (comp.getBackground().equals(selectedBg) || comp.getBackground().equals(hoverBg)) {
                                     comp.setBackground(originalBg);
                                }
                            }
                        }
                    }
                     // --- Modification from Code One's original selection logic ---
                     // The previous loop had a potential issue resetting hover state correctly.
                     // Let's refine slightly while staying close to original intent:
                     // Reset all *other* navigable labels, apply selected to current.
                     Component[] allComponents = menuItem.getParent().getComponents();
                     for(Component comp : allComponents) {
                         if (comp instanceof JLabel) {
                             boolean isNavigable = false;
                             // Simple check: Does it have mouse listeners and isn't the title/welcome/avatar?
                             // This is an assumption based on Code 1 structure.
                             if(comp.getMouseListeners().length > 0 && !((JLabel)comp).getText().startsWith("Welcome") && !((JLabel)comp).getText().equals("Smart Finance") && ((JLabel)comp).getIcon() == null ) {
                                 isNavigable = true; // Assume labels with listeners are navigable items or logout
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
                     // --- End of selection logic refinement ---
                }
            });
        } else {
            // Keep original MouseAdapter logic for Logout from Code One
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
                // mouseClicked is handled by the separate listener added in createSidebar
            });
        }

        return menuItem;
    }

    // initializeContentPanels is MODIFIED to include BudgetManagementPanel
    private void initializeContentPanels() {
        // Keep original panel names array
        String[] panelNames = {"Transaction System", "Bill Statistics", "Personal Center", "Budget Management", "AI Assistant"};
        for (String name : panelNames) {
            JPanel panel;
            if (name.equals("AI Assistant")) {
                panel = new AIPanel(); // Keep using AIPanel
            } else if (name.equals("Transaction System")) {
                panel = new RefactoredTransactionUI(username);
            } else if (name.equals("Bill Statistics")) {
                panel = createBillStatisticsPanel(); // << KEEP ORIGINAL BILL STATISTICS PANEL
            } else if (name.equals("Personal Center")) {
                panel = new PersonalCenterPanel(username); // Keep using PersonalCenterPanel
            } else if (name.equals("Budget Management")) { // << CHANGE HERE
                // Instead of placeholder, instantiate BudgetManagementPanel
                panel = new BudgetManagementPanel(username); // Integrate the panel
            } else {
                // Fallback for any unexpected names (though shouldn't happen)
                System.err.println("Warning: Unhandled panel name in initializeContentPanels: " + name);
                panel = createPlaceholderPanel(name); // Keep placeholder as fallback
            }
            contentPanel.add(panel, name);
        }
    }

    // createTransactionSystemPanel remains EXACTLY as in Code One



    // createBillStatisticsPanel remains EXACTLY as in Code One
    private JPanel createBillStatisticsPanel() {

        JPanel panel = new GradientPanel(); // 使用你定义过的紫蓝渐变面板
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Bill Statistics", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE); // 白字
        panel.add(title, BorderLayout.NORTH);



        // Keep original logic for chart creation and date selection
        java.util.Calendar cal = java.util.Calendar.getInstance();
        String currentYearMonth = String.format("%d/%02d", cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1);

        try {
            chartPanel = IncomeExpenseChart.getIncomeExpensePlane(username, currentYearMonth); // Keep original method call
            panel.add(chartPanel, BorderLayout.CENTER);
        } catch (Exception e) {
            e.printStackTrace();
            JLabel errorLabel = new JLabel("加载图表失败: " + e.getMessage(), SwingConstants.CENTER);
            errorLabel.setForeground(Color.RED);
            panel.add(errorLabel, BorderLayout.CENTER);
        }

        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        selectorPanel.setBackground(Color.WHITE);

        JLabel yearLabel = new JLabel("Year:");
        yearLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        yearLabel.setForeground(new Color(50, 50, 50));


    // Year ComboBox
        JComboBox<Integer> yearComboBox = new JComboBox<>();
        int currentYear = cal.get(Calendar.YEAR);
        for (int i = currentYear - 5; i <= currentYear + 5; i++) {
            yearComboBox.addItem(i);
        }
        yearComboBox.setSelectedItem(currentYear);
        yearComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));

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
        yearComboBox.setBorder(new AccountManagementUI.GradientBorder(2, 16));

        yearComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                                                          Object value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel lbl = (JLabel)super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    lbl.setBackground(new Color(100,149,237));
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(new Color(50,50,50));
                }
                lbl.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
                return lbl;
            }
        });

        String[] months = { "01", "02", "03", "04", "05", "06",
                "07", "08", "09", "10", "11", "12" };

        JLabel monthLabel = new JLabel("Month:");
        monthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        monthLabel.setForeground(new Color(50, 50, 50));
        JComboBox<String> monthComboBox = new JComboBox<>(months);
        monthComboBox.setSelectedItem(String.format("%02d", cal.get(Calendar.MONTH)+1));
        monthComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        monthComboBox.setUI(new BasicComboBoxUI() {
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
        monthComboBox.setOpaque(false);
        monthComboBox.setBackground(Color.WHITE);
        monthComboBox.setBorder(new AccountManagementUI.GradientBorder(2, 16));
        monthComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                                                          Object value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel lbl = (JLabel)super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    lbl.setBackground(new Color(100,149,237));
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(new Color(50,50,50));
                }
                lbl.setBorder(BorderFactory.createEmptyBorder(2,10,2,10));
                return lbl;
            }
        });

        selectorPanel.add(yearLabel);
        selectorPanel.add(yearComboBox);
        selectorPanel.add(monthLabel);
        selectorPanel.add(monthComboBox);

        panel.add(selectorPanel, BorderLayout.SOUTH);

        ActionListener updateListener = e -> {
            try {
                String selectedYearMonth = yearComboBox.getSelectedItem() + "/" + monthComboBox.getSelectedItem();
                JPanel newChartPanel = IncomeExpenseChart.getIncomeExpensePlane(username, selectedYearMonth); // Keep original method call
                panel.remove(chartPanel); // chartPanel instance variable is kept
                chartPanel = newChartPanel;
                panel.add(chartPanel, BorderLayout.CENTER);
                panel.revalidate();
                panel.repaint();
            } // ... inside createBillStatisticsPanel method
            catch (Exception ex) {
                ex.printStackTrace();
                JLabel errorLabel = new JLabel("更新图表失败: " + ex.getMessage(), SwingConstants.CENTER);
                errorLabel.setForeground(Color.RED);
        
                // Ensure chartPanel is handled correctly on error
                // Check if the component exists and is attached to the panel
                if(chartPanel != null && chartPanel.getParent() == panel) {
                    panel.remove(chartPanel);
                }
        
                // Add the error label
                panel.add(errorLabel, BorderLayout.CENTER);
        
                // Assign the errorLabel (which is a Component) to chartPanel (now a Component variable)
                chartPanel = errorLabel; // <--- THIS LINE IS NOW VALID
        
                panel.revalidate();
                panel.repaint();
            }
        // ... rest of createBillStatisticsPanel
        };
        yearComboBox.addActionListener(updateListener);
        monthComboBox.addActionListener(updateListener);

        return panel;
    }

    // createPlaceholderPanel remains EXACTLY as in Code One (used as fallback)
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
        contentLabel.setForeground(new Color(50, 50, 50)); // Keep original color
        card.add(contentLabel, BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);

        return panel;
    }



    // main method remains EXACTLY as in Code One
    public static void main(String[] args) {
        String userId = UserSession.getCurrentUsername(); // Keep original UserSession reference
        if (userId == null) {
            userId = "defaultUser"; // Keep original fallback
        }
        String finalUserId = userId;
        SwingUtilities.invokeLater(() -> new PersonalMainPlane(finalUserId));
    }
}