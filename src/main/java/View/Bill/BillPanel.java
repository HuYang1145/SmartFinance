
package View.Bill;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import Controller.BillController;
import Controller.PersonCenterController;
import Model.Transaction;
import Service.BudgetService;
import View.Bill.BillComponents.GradientBorder;
import View.LoginAndMain.GradientComponents;
import View.Transaction.TransactionSystemComponents;

/**
 * A JPanel that displays bill statistics, including income and expense donut charts,
 * transaction records, type analysis, and a monthly bar chart for a selected date range
 * and category field.
 * 
 * @author Group 19
 * @version 1.0
 */
public class BillPanel extends TransactionSystemComponents.MidGradientPanel {
    private JComboBox<Integer> startYearComboBox, endYearComboBox;
    private JComboBox<String> startMonthComboBox, endMonthComboBox;
    private JComboBox<String> categoryFieldComboBox;
    private String username;
    private final PersonCenterController personCenterController;
    private final BillController billController;
    private JPanel incomeChartPanel, expenseChartPanel, transactionTablePanel, dailyBarChartPanel;
    private JScrollPane typeAnalysisPanel;

    /**
     * Constructs a BillPanel for the specified user with controllers for personal center and bill data.
     * 
     * @param username the username of the current user
     * @param personCenterController the controller for personal center data
     * @param billController the controller for bill data
     */
    public BillPanel(String username, PersonCenterController personCenterController, BillController billController) {
        this.username = username;
        this.personCenterController = personCenterController;
        this.billController = billController;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Bill Statistics", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.DARK_GRAY);
        add(title, BorderLayout.NORTH);

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 14);
        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        selectorPanel.setBackground(Color.WHITE);

        JLabel fromLabel = new JLabel("From:");
        fromLabel.setFont(labelFont);
        fromLabel.setForeground(new Color(50, 50, 50));
        selectorPanel.add(fromLabel);

        startYearComboBox = new JComboBox<>();
        for (int i = 2020; i <= 2030; i++) {
            startYearComboBox.addItem(i);
        }
        startYearComboBox.setSelectedItem(2024);
        startYearComboBox.setFont(labelFont);
        startYearComboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton("\u25BE");
                btn.setOpaque(false);
                btn.setContentAreaFilled(false);
                btn.setBorder(null);
                return btn;
            }

            @Override
            public void installUI(JComponent c) {
                super.installUI(c);
                comboBox.setOpaque(false);
                arrowButton.setOpaque(false);
                arrowButton.setBackground(new Color(0, 0, 0, 0));
            }
        });
        startYearComboBox.setOpaque(false);
        startYearComboBox.setBackground(Color.WHITE);
        startYearComboBox.setBorder(new GradientBorder(2, 16));
        startYearComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    lbl.setBackground(new Color(100, 149, 237));
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(new Color(50, 50, 50));
                }
                lbl.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
                return lbl;
            }
        });
        selectorPanel.add(startYearComboBox);

        String[] months = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
        startMonthComboBox = new JComboBox<>(months);
        startMonthComboBox.setSelectedItem("03");
        startMonthComboBox.setFont(labelFont);
        startMonthComboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton("\u25BE");
                btn.setOpaque(false);
                btn.setContentAreaFilled(false);
                btn.setBorder(null);
                return btn;
            }

            @Override
            public void installUI(JComponent c) {
                super.installUI(c);
                comboBox.setOpaque(false);
                arrowButton.setOpaque(false);
                arrowButton.setBackground(new Color(0, 0, 0, 0));
            }
        });
        startMonthComboBox.setOpaque(false);
        startMonthComboBox.setBackground(Color.WHITE);
        startMonthComboBox.setBorder(new GradientBorder(2, 16));
        startMonthComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    lbl.setBackground(new Color(100, 149, 237));
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(Color.white);
                    lbl.setForeground(new Color(50, 50, 50));
                }
                lbl.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
                return lbl;
            }
        });
        selectorPanel.add(startMonthComboBox);

        JLabel toLabel = new JLabel("To:");
        toLabel.setFont(labelFont);
        toLabel.setForeground(new Color(50, 50, 50));
        selectorPanel.add(toLabel);

        endYearComboBox = new JComboBox<>();
        for (int i = 2020; i <= 2030; i++) {
            endYearComboBox.addItem(i);
        }
        endYearComboBox.setSelectedItem(2025);
        endYearComboBox.setFont(labelFont);
        endYearComboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton("\u25BE");
                btn.setOpaque(false);
                btn.setContentAreaFilled(false);
                btn.setBorder(null);
                return btn;
            }

            @Override
            public void installUI(JComponent c) {
                super.installUI(c);
                comboBox.setOpaque(false);
                arrowButton.setOpaque(false);
                arrowButton.setBackground(new Color(0, 0, 0, 0));
            }
        });
        endYearComboBox.setOpaque(false);
        endYearComboBox.setBackground(Color.WHITE);
        endYearComboBox.setBorder(new GradientBorder(2, 16));
        endYearComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    lbl.setBackground(new Color(100, 149, 237));
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(new Color(50, 50, 50));
                }
                lbl.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
                return lbl;
            }
        });
        selectorPanel.add(endYearComboBox);

        endMonthComboBox = new JComboBox<>(months);
        endMonthComboBox.setSelectedItem("06");
        endMonthComboBox.setFont(labelFont);
        endMonthComboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton("\u25BE");
                btn.setOpaque(false);
                btn.setContentAreaFilled(false);
                btn.setBorder(null);
                return btn;
            }

            @Override
            public void installUI(JComponent c) {
                super.installUI(c);
                comboBox.setOpaque(false);
                arrowButton.setOpaque(false);
                arrowButton.setBackground(new Color(0, 0, 0, 0));
            }
        });
        endMonthComboBox.setOpaque(false);
        endMonthComboBox.setBackground(Color.WHITE);
        endMonthComboBox.setBorder(new GradientBorder(2, 16));
        endMonthComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    lbl.setBackground(new Color(100, 149, 237));
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(new Color(50, 50, 50));
                }
                lbl.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
                return lbl;
            }
        });
        selectorPanel.add(endMonthComboBox);

        JLabel categorizeLabel = new JLabel("Categorize By:");
        categorizeLabel.setFont(labelFont);
        categorizeLabel.setForeground(new Color(50, 50, 50));
        selectorPanel.add(categorizeLabel);

        String[] categoryFields = {"category", "type", "payment_method", "location", "merchant", "tag", "recurrence"};
        categoryFieldComboBox = new JComboBox<>(categoryFields);
        categoryFieldComboBox.setSelectedItem("category");
        categoryFieldComboBox.setFont(labelFont);
        categoryFieldComboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton("\u25BE");
                btn.setOpaque(false);
                btn.setContentAreaFilled(false);
                btn.setBorder(null);
                return btn;
            }

            @Override
            public void installUI(JComponent c) {
                super.installUI(c);
                comboBox.setOpaque(false);
                arrowButton.setOpaque(false);
                arrowButton.setBackground(new Color(0, 0, 0, 0));
            }
        });
        categoryFieldComboBox.setOpaque(false);
        categoryFieldComboBox.setBackground(Color.WHITE);
        categoryFieldComboBox.setBorder(new GradientBorder(2, 16));
        categoryFieldComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    lbl.setBackground(new Color(100, 149, 237));
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(new Color(50, 50, 50));
                }
                lbl.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
                return lbl;
            }
        });
        selectorPanel.add(categoryFieldComboBox);

        add(selectorPanel, BorderLayout.SOUTH);

        initializeChart();
        startYearComboBox.addActionListener(e -> updateChart());
        startMonthComboBox.addActionListener(e -> updateChart());
        endYearComboBox.addActionListener(e -> updateChart());
        endMonthComboBox.addActionListener(e -> updateChart());
        categoryFieldComboBox.addActionListener(e -> updateChart());
    }

    /**
     * Initializes the chart by calling the updateChart method.
     */
    public void initializeChart() {
        System.out.println("Initializing chart for BillPanel");
        updateChart();
    }

    /**
     * Updates the chart based on the selected date range and category field.
     */
    public void updateChart() {
        String[] dateRange = getSelectedDateRange();
        if (dateRange == null) {
            System.err.println("Invalid date range selected");
            return;
        }
        String categoryField = (String) categoryFieldComboBox.getSelectedItem();
        updateChart(username, dateRange[0], dateRange[1], categoryField);
    }

    /**
     * Updates the chart with data for the specified user, date range, and category field.
     * 
     * @param username the username to retrieve transactions for
     * @param startYearMonth the start year and month (yyyy/MM)
     * @param endYearMonth the end year and month (yyyy/MM)
     * @param categoryField the field to categorize transactions by
     */
    public void updateChart(String username, String startYearMonth, String endYearMonth, String categoryField) {
        if (username == null) {
            System.err.println("Cannot update chart: username is null");
            return;
        }
        System.out.println("Updating chart for user: " + username + ", range: " + startYearMonth + " to " + endYearMonth + ", categoryField: " + categoryField);
        try {
            java.awt.Component centerComponent = ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (centerComponent != null) {
                System.out.println("Removing existing center component");
                remove(centerComponent);
            } else {
                System.out.println("No existing center component to remove");
            }

            JPanel mainContent = new JPanel(new GridBagLayout());
            mainContent.setOpaque(false);
            mainContent.setBorder(new EmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.BOTH;

            incomeChartPanel = createDonutChartPanel(username, false, startYearMonth, endYearMonth, categoryField);
            JPanel incomeCategoriesPanel = new JPanel(new BorderLayout());
            incomeCategoriesPanel.setOpaque(true);
            incomeCategoriesPanel.setBackground(Color.WHITE);
            incomeCategoriesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            JLabel incomeTitle = new JLabel("Income Categories", SwingConstants.CENTER);
            incomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
            incomeTitle.setForeground(Color.DARK_GRAY);
            incomeTitle.setBorder(new EmptyBorder(5, 0, 10, 0));
            incomeCategoriesPanel.add(incomeTitle, BorderLayout.NORTH);
            incomeCategoriesPanel.add(incomeChartPanel, BorderLayout.CENTER);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.33;
            gbc.weighty = 0.5;
            mainContent.add(incomeCategoriesPanel, gbc);
            System.out.println("Added Income Categories panel");

            expenseChartPanel = createDonutChartPanel(username, true, startYearMonth, endYearMonth, categoryField);
            JPanel expenseCategoriesPanel = new JPanel(new BorderLayout());
            expenseCategoriesPanel.setOpaque(true);
            expenseCategoriesPanel.setBackground(Color.WHITE);
            expenseCategoriesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            JLabel expenseTitle = new JLabel("Expense Categories", SwingConstants.CENTER);
            expenseTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
            expenseTitle.setForeground(Color.DARK_GRAY);
            expenseTitle.setBorder(new EmptyBorder(5, 0, 10, 0));
            expenseCategoriesPanel.add(expenseTitle, BorderLayout.NORTH);
            expenseCategoriesPanel.add(expenseChartPanel, BorderLayout.CENTER);
            gbc.gridx = 1;
            gbc.gridy = 0;
            mainContent.add(expenseCategoriesPanel, gbc);
            System.out.println("Added Expense Categories panel");

            typeAnalysisPanel = getTypeAnalysisPanel(username, startYearMonth, endYearMonth, categoryField);
            gbc.gridx = 2;
            gbc.gridy = 0;
            mainContent.add(typeAnalysisPanel, gbc);
            System.out.println("Added Type Analysis panel");

            transactionTablePanel = getTransactionTablePanel(username, startYearMonth, endYearMonth);
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbc.weightx = 0.66;
            gbc.weighty = 0.5;
            mainContent.add(transactionTablePanel, gbc);
            System.out.println("Added Transaction Table panel");

            dailyBarChartPanel = getDailyLineChartPanel(username, startYearMonth, endYearMonth);
            gbc.gridx = 2;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.33;
            mainContent.add(dailyBarChartPanel, gbc);
            System.out.println("Added Monthly Bar Chart panel");

            add(mainContent, BorderLayout.CENTER);
            System.out.println("Chart updated successfully for " + username);
            revalidate();
            repaint();
        } catch (Exception ex) {
            System.err.println("Failed to update chart for " + username + ": " + ex.getMessage());
            ex.printStackTrace();
            JLabel errorLabel = new JLabel("Failed to update chart: " + ex.getMessage(), SwingConstants.CENTER);
            errorLabel.setForeground(Color.RED);
            add(errorLabel, BorderLayout.CENTER);
            revalidate();
            repaint();
        }
    }

    /**
     * Retrieves the selected date range from the combo boxes.
     * 
     * @return an array containing the start and end year/month (yyyy/MM), or null if the range is invalid
     */
    public String[] getSelectedDateRange() {
        int startYear = (Integer) startYearComboBox.getSelectedItem();
        String startMonth = (String) startMonthComboBox.getSelectedItem();
        int endYear = (Integer) endYearComboBox.getSelectedItem();
        String endMonth = (String) endMonthComboBox.getSelectedItem();

        String startYearMonth = startYear + "/" + startMonth;
        String endYearMonth = endYear + "/" + endMonth;

        LocalDate startDate = LocalDate.parse(startYearMonth + "/01", BudgetService.DATE_FORMATTER);
        LocalDate endDate = LocalDate.parse(endYearMonth + "/01", BudgetService.DATE_FORMATTER);
        if (startDate.isAfter(endDate)) {
            javax.swing.JOptionPane.showMessageDialog(this, "Start date cannot be after end date.", "Invalid Date Range", javax.swing.JOptionPane.ERROR_MESSAGE);
            return null;
        }

        return new String[]{startYearMonth, endYearMonth};
    }

    /**
     * Creates a donut chart panel for income or expense categories.
     * 
     * @param username the username to retrieve transactions for
     * @param isExpense true for expense chart, false for income chart
     * @param startYearMonth the start year and month (yyyy/MM)
     * @param endYearMonth the end year and month (yyyy/MM)
     * @param categoryField the field to categorize transactions by
     * @return the donut chart panel
     */
    private JPanel createDonutChartPanel(String username, boolean isExpense, String startYearMonth, String endYearMonth, String categoryField) {
        return new JPanel() {
            private List<Map.Entry<String, Double>> categoryEntries = new ArrayList<>();
            private double totalAmount = 0.0;
            private int hoveredIndex = -1;
            private Popup tooltipPopup;

            {
                List<Transaction> transactions = billController.getFilteredTransactions(username, startYearMonth, endYearMonth);
                System.out.println("Filtered transactions for " + (isExpense ? "expense" : "income") + ": " + transactions.size());
                Map<String, Double> categoryTotals = isExpense
                        ? billController.calculateExpenseCategoryTotals(transactions, categoryField)
                        : billController.calculateIncomeCategoryTotals(transactions, categoryField);

                totalAmount = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();
                categoryEntries.clear();
                categoryEntries.addAll(categoryTotals.entrySet().stream()
                        .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                        .collect(Collectors.toList()));

                addMouseMotionListener(new MouseAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        int centerX = getWidth() / 2;
                        int centerY = getHeight() / 2;
                        int diameter = Math.min(getWidth(), getHeight()) - 80;
                        int radius = diameter / 2;
                        int innerRadius = radius / 2;

                        double dx = e.getX() - centerX;
                        double dy = e.getY() - centerY;
                        double distance = Math.sqrt(dx * dx + dy * dy);

                        int newHoveredIndex = -1;
                        if (distance >= innerRadius && distance <= radius) {
                            double angle = Math.toDegrees(Math.atan2(-dy, dx));
                            if (angle < 0) angle += 360;

                            int startAngle = 0;
                            for (int i = 0; i < categoryEntries.size(); i++) {
                                Map.Entry<String, Double> entry = categoryEntries.get(i);
                                double percentage = entry.getValue() / totalAmount;
                                int arcAngle = (int) Math.round(percentage * 360);
                                if (arcAngle == 0 && entry.getValue() > 0) {
                                    arcAngle = 1;
                                }

                                if (angle >= startAngle && angle < startAngle + arcAngle) {
                                    newHoveredIndex = i;
                                    break;
                                }
                                startAngle += arcAngle;
                            }
                        }

                        if (newHoveredIndex != hoveredIndex) {
                            hoveredIndex = newHoveredIndex;
                            repaint();

                            if (tooltipPopup != null) {
                                tooltipPopup.hide();
                                tooltipPopup = null;
                            }

                            if (hoveredIndex >= 0) {
                                Map.Entry<String, Double> entry = categoryEntries.get(hoveredIndex);
                                double percentage = entry.getValue() / totalAmount;
                                String tooltipText = String.format("%s: ¥%.2f (%.1f%%)", entry.getKey(), entry.getValue(), percentage * 100);
                                JToolTip tooltip = new JToolTip();
                                tooltip.setTipText(tooltipText);
                                tooltip.setBackground(new Color(255, 255, 225));
                                tooltip.setForeground(Color.BLACK);
                                tooltip.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                                tooltipPopup = PopupFactory.getSharedInstance().getPopup(BillPanel.this, tooltip, e.getXOnScreen(), e.getYOnScreen() + 15);
                                tooltipPopup.show();
                            }
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (hoveredIndex != -1) {
                            hoveredIndex = -1;
                            repaint();
                            if (tooltipPopup != null) {
                                tooltipPopup.hide();
                                tooltipPopup = null;
                            }
                        }
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setBackground(Color.WHITE);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (totalAmount <= 0) {
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    g2d.setColor(Color.black);
                    g2d.drawString("No valid " + (isExpense ? "expense" : "income") + " data to display.",
                            getWidth() / 2 - 70, getHeight() / 2);
                    g2d.dispose();
                    return;
                }

                int startAngle = 0;
                Color[] colors = {
                        new Color(255, 182, 193), new Color(173, 216, 230), new Color(144, 238, 144),
                        new Color(240, 230, 140), new Color(221, 160, 221), new Color(255, 218, 185),
                        new Color(135, 206, 250), new Color(200, 162, 200)
                };

                int diameter = Math.min(getWidth(), getHeight()) - 80;
                int radius = diameter / 2;
                int innerRadius = radius / 2;
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;

                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                for (int j = 0; j < categoryEntries.size(); j++) {
                    Map.Entry<String, Double> entry = categoryEntries.get(j);
                    double percentage = entry.getValue() / totalAmount;
                    int arcAngle = (int) Math.round(percentage * 360);
                    if (arcAngle == 0 && entry.getValue() > 0) {
                        arcAngle = 1;
                    }

                    Color baseColor = colors[j % colors.length];
                    if (j == hoveredIndex) {
                        float[] hsb = Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), null);
                        baseColor = Color.getHSBColor(hsb[0], hsb[1], Math.min(hsb[2] + 0.2f, 1.0f));
                    }
                    g2d.setColor(baseColor);
                    g2d.fillArc(centerX - radius, centerY - radius, diameter, diameter, startAngle, arcAngle);
                    startAngle += arcAngle;
                }

                g2d.setColor(new Color(245, 245, 245));
                g2d.fillArc(centerX - innerRadius, centerY - innerRadius, innerRadius * 2, innerRadius * 2, 0, 360);

                g2d.setColor(Color.black);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                FontMetrics fm = g2d.getFontMetrics();
                List<Rectangle> labelBounds = new ArrayList<>();
                startAngle = 0;
                int maxLabels = 10;
                int labelCount = 0;
                for (int j = 0; j < categoryEntries.size() && labelCount < maxLabels; j++) {
                    Map.Entry<String, Double> entry = categoryEntries.get(j);
                    double percentage = entry.getValue() / totalAmount;
                    int arcAngle = (int) Math.round(percentage * 360);
                    if (arcAngle == 0 && entry.getValue() > 0) {
                        arcAngle = 1;
                    }

                    int labelAngle = startAngle + arcAngle / 2;
                    double labelAngleRad = Math.toRadians(labelAngle);

                    int labelRadius = radius + 30;
                    int labelX = centerX + (int) (labelRadius * Math.cos(labelAngleRad));
                    int labelY = centerY - (int) (labelRadius * Math.sin(labelAngleRad));

                    String labelText = "%s: ¥%.2f (%.1f%%)".formatted(entry.getKey(), entry.getValue(), percentage * 100);
                    int textWidth = fm.stringWidth(labelText);
                    int textHeight = fm.getHeight();

                    if (labelX + textWidth > getWidth()) {
                        labelX = getWidth() - textWidth - 5;
                    } else if (labelX < 0) {
                        labelX = 5;
                    }
                    if (labelY - textHeight < 0) {
                        labelY = textHeight + 5;
                    } else if (labelY > getHeight()) {
                        labelY = getHeight() - 5;
                    }

                    Rectangle newBounds = new Rectangle(labelX, labelY - textHeight, textWidth, textHeight);
                    boolean overlaps = labelBounds.stream().anyMatch(b -> b.intersects(newBounds));
                    if (!overlaps) {
                        g2d.drawString(labelText, labelX, labelY);
                        labelBounds.add(newBounds);
                        labelCount++;
                    }

                    startAngle += arcAngle;
                }

                g2d.setColor(Color.blue);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                String totalText = String.format("Total: ¥%.2f", totalAmount);
                int textWidth = fm.stringWidth(totalText);
                int textHeight = fm.getHeight();
                int textX = centerX - textWidth / 2;
                int textY = centerY + textHeight / 2;
                g2d.drawString(totalText, textX, textY);

                g2d.dispose();
            }
        };
    }

    /**
     * Creates a panel displaying a table of transaction records.
     * 
     * @param username the username to retrieve transactions for
     * @param startYearMonth the start year and month (yyyy/MM)
     * @param endYearMonth the end year and month (yyyy/MM)
     * @return the transaction table panel
     */
    private JPanel getTransactionTablePanel(String username, String startYearMonth, String endYearMonth) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Transaction Records", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        panel.add(title, BorderLayout.NORTH);

        List<Transaction> transactions = billController.getFilteredTransactions(username, startYearMonth, endYearMonth);
        System.out.println("Transaction table transactions: " + transactions.size());
        String[] columnNames = {"User", "Operation", "Amount", "Time", "Merchant", "Type", "Remark", "Category",
                "Payment Method", "Location"};
        Object[][] data = new Object[transactions.size()][columnNames.length];
        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            data[i] = new Object[] {
                    t.getAccountUsername(), t.getOperation(), "%.2f".formatted(t.getAmount()), t.getTimestamp(),
                    t.getMerchant(), t.getType(), t.getRemark(), t.getCategory(), t.getPaymentMethod(),
                    t.getLocation()
            };
        }

        JTable table = new JTable(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);
        table.setIntercellSpacing(new Dimension(0, 0));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        renderer.setBackground(Color.WHITE);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Color.BLACK));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        scrollPane.setPreferredSize(new Dimension(0, 250));

        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tableWrapper.add(scrollPane, BorderLayout.CENTER);
        panel.add(tableWrapper, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates a scrollable panel displaying type analysis for expense transactions.
     * 
     * @param username the username to retrieve transactions for
     * @param startYearMonth the start year and month (yyyy/MM)
     * @param endYearMonth the end year and month (yyyy/MM)
     * @param categoryField the field to categorize transactions by
     * @return the type analysis panel as a JScrollPane
     */
    private JScrollPane getTypeAnalysisPanel(String username, String startYearMonth, String endYearMonth, String categoryField) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        List<Transaction> transactions = billController.getFilteredTransactions(username, startYearMonth, endYearMonth);
        Map<String, Integer> fieldCounts = new HashMap<>();
        Map<String, Double> fieldTotals = new HashMap<>();
        double totalExpense = 0.0;

        for (Transaction t : transactions) {
            if ("Expense".equalsIgnoreCase(t.getOperation())) {
                String fieldValue = getFieldValue(t, categoryField);
                if (fieldValue == null || fieldValue.trim().isEmpty() || "u".equalsIgnoreCase(fieldValue.trim())) {
                    fieldValue = "Unclassified";
                }
                fieldCounts.put(fieldValue, fieldCounts.getOrDefault(fieldValue, 0) + 1);
                fieldTotals.put(fieldValue, fieldTotals.getOrDefault(fieldValue, 0.0) + t.getAmount());
                totalExpense += t.getAmount();
            }
        }

        Color[] fieldColors = {
                new Color(255, 182, 193), new Color(144, 238, 144), new Color(135, 206, 250),
                new Color(240, 230, 140), new Color(221, 160, 221), new Color(173, 216, 230),
                new Color(255, 218, 185), new Color(200, 162, 200)
        };

        int i = 0;
        for (Map.Entry<String, Integer> entry : fieldCounts.entrySet().stream().limit(8).toList()) {
            String fieldValue = entry.getKey();
            int count = entry.getValue();
            double amount = fieldTotals.get(fieldValue);
            double percentage = totalExpense > 0 ? (amount / totalExpense) * 100 : 0;

            JPanel fieldPanel = new JPanel(new GridLayout(3, 1, 5, 5));
            fieldPanel.setBackground(Color.WHITE);
            fieldPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                    BorderFactory.createLineBorder(fieldColors[i % fieldColors.length], 4, true)
            ));
            fieldPanel.add(new JLabel(fieldValue, SwingConstants.CENTER));
            fieldPanel.add(new JLabel("Count: " + count, SwingConstants.CENTER));
            fieldPanel.add(new JLabel("¥%.2f (%.1f%%)".formatted(amount, percentage), SwingConstants.CENTER));
            panel.add(fieldPanel);
            i++;
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        return scrollPane;
    }

    /**
     * Creates a panel displaying a bar chart of monthly income and expense totals.
     * 
     * @param username the username to retrieve transactions for
     * @param startYearMonth the start year and month (yyyy/MM)
     * @param endYearMonth the end year and month (yyyy/MM)
     * @return the monthly bar chart panel
     */
    private JPanel getDailyLineChartPanel(String username, String startYearMonth, String endYearMonth) {
        JPanel panel = new JPanel() {
            private boolean showIncome = true;
            private boolean showExpense = true;
            private double totalIncome = 0.0;
            private double totalExpense = 0.0;

            {
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
                buttonPanel.setOpaque(false);
                JButton showIncomeButton = new JButton("Show Income");
                showIncomeButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                showIncomeButton.setBackground(new Color(54, 162, 235));
                showIncomeButton.setForeground(Color.WHITE);
                showIncomeButton.setFocusPainted(false);
                showIncomeButton.addActionListener(e -> {
                    showIncome = !showIncome;
                    showIncomeButton.setText(showIncome ? "Hide Income" : "Show Income");
                    repaint();
                });

                JButton showExpenseButton = new JButton("Show Expense");
                showExpenseButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                showExpenseButton.setBackground(new Color(255, 99, 132));
                showExpenseButton.setForeground(Color.WHITE);
                showExpenseButton.setFocusPainted(false);
                showExpenseButton.addActionListener(e -> {
                    showExpense = !showExpense;
                    showExpenseButton.setText(showExpense ? "Hide Expense" : "Show Expense");
                    repaint();
                });

                buttonPanel.add(showIncomeButton);
                buttonPanel.add(showExpenseButton);
                add(buttonPanel, BorderLayout.NORTH);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setBackground(Color.WHITE);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                List<Transaction> transactions = billController.getFilteredTransactions(username, startYearMonth, endYearMonth);

                LocalDate startDate = LocalDate.parse(startYearMonth + "/01", BudgetService.DATE_FORMATTER);
                LocalDate endDate = LocalDate.parse(endYearMonth + "/01", BudgetService.DATE_FORMATTER).withDayOfMonth(1);
                List<String> months = new ArrayList<>();
                Map<String, Double> monthlyIncomes = new HashMap<>();
                Map<String, Double> monthlyExpenses = new HashMap<>();
                double maxAmount = 0;
                totalIncome = 0.0;
                totalExpense = 0.0;

                LocalDate current = startDate;
                while (!current.isAfter(endDate)) {
                    String monthKey = current.getYear() + "/" + String.format("%02d", current.getMonthValue());
                    months.add(monthKey);
                    monthlyIncomes.put(monthKey, 0.0);
                    monthlyExpenses.put(monthKey, 0.0);
                    current = current.plusMonths(1);
                }

                for (Transaction t : transactions) {
                    try {
                        LocalDate date = LocalDate.parse(t.getTimestamp(), BudgetService.DATE_FORMATTER);
                        String monthKey = date.getYear() + "/" + String.format("%02d", date.getMonthValue());
                        if (monthlyIncomes.containsKey(monthKey)) {
                            if ("Income".equalsIgnoreCase(t.getOperation())) {
                                monthlyIncomes.merge(monthKey, t.getAmount(), Double::sum);
                                totalIncome += t.getAmount();
                            } else if ("Expense".equalsIgnoreCase(t.getOperation())) {
                                monthlyExpenses.merge(monthKey, t.getAmount(), Double::sum);
                                totalExpense += t.getAmount();
                            }
                            maxAmount = Math.max(maxAmount, Math.max(monthlyIncomes.get(monthKey), monthlyExpenses.get(monthKey)));
                        }
                    } catch (DateTimeParseException e) {
                        System.err.println("Error parsing date: " + t.getTimestamp());
                    }
                }

                if (maxAmount == 0) {
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    g2d.setColor(Color.black);
                    g2d.drawString("No data for selected period", getWidth() / 2 - 70, getHeight() / 2);
                    g2d.dispose();
                    return;
                }

                int margin = 50;
                int width = getWidth() - 2 * margin;
                int height = getHeight() - 2 * margin - 50;
                int barWidth = (int) Math.max(1, height / (2.0 * months.size()));

                g2d.setColor(Color.black);
                g2d.drawLine(margin, margin, margin + width, margin);
                g2d.drawLine(margin, margin, margin, margin + height);

                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.setColor(Color.black);
                int numTicks = 5;
                double amountStep = maxAmount / numTicks;
                for (int i = 0; i <= numTicks; i++) {
                    double amount = i * amountStep;
                    int x = margin + (int) ((amount / maxAmount) * width);
                    g2d.drawLine(x, margin, x, margin + 5);
                    String amountLabel = "%.0f".formatted(amount);
                    int labelWidth = g2d.getFontMetrics().stringWidth(amountLabel);
                    g2d.drawString(amountLabel, x - labelWidth / 2, margin + 20);
                }

                String amountLabel = "Amount (¥)";
                FontMetrics fm = g2d.getFontMetrics();
                int labelWidth = fm.stringWidth(amountLabel);
                g2d.drawString(amountLabel, margin + width - labelWidth, margin + 40);

                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.setColor(Color.black);
                for (int i = 0; i < months.size(); i++) {
                    String month = months.get(i);
                    int y = margin + i * barWidth * 2 + barWidth;
                    g2d.drawLine(margin - 5, y, margin, y);
                    int monthLabelWidth = fm.stringWidth(month);
                    g2d.drawString(month, margin - monthLabelWidth - 10, y + 5);
                }

                for (int i = 0; i < months.size(); i++) {
                    String month = months.get(i);
                    double income = monthlyIncomes.get(month);
                    double expense = monthlyExpenses.get(month);

                    boolean hasBoth = showIncome && showExpense && income > 0 && expense > 0;
                    int adjustedBarWidth = hasBoth ? barWidth : barWidth - 2;
                    int roundness = hasBoth ? 15 : 10;

                    int y = margin + i * barWidth * 2;

                    if (showIncome && income > 0) {
                        int incomeWidth = (int) ((income / maxAmount) * width);
                        g2d.setColor(new Color(54, 162, 235));
                        g2d.fillRoundRect(margin, y, incomeWidth, adjustedBarWidth, roundness, roundness);
                    }

                    if (showExpense && expense > 0) {
                        int expenseWidth = (int) ((expense / maxAmount) * width);
                        g2d.setColor(new Color(255, 99, 132));
                        g2d.fillRoundRect(margin, y + (hasBoth ? barWidth : 0), expenseWidth, adjustedBarWidth, roundness, roundness);
                    }
                }

                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.setColor(Color.blue);
                fm = g2d.getFontMetrics();
                int textHeight = fm.getHeight();
                g2d.drawString("Total Income: ¥" + "%.2f".formatted(totalIncome), margin + 10, margin + textHeight);
                g2d.drawString("Total Expense: ¥" + "%.2f".formatted(totalExpense), margin + 10, margin + textHeight * 2);

                g2d.dispose();
            }
        };
        return panel;
    }

    /**
     * Retrieves the value of the specified field from a transaction.
     * 
     * @param t the transaction to extract the field from
     * @param field the field name to retrieve
     * @return the field value, or "Unclassified" if not applicable
     */
    private String getFieldValue(Transaction t, String field) {
        switch (field) {
            case "category": return t.getCategory();
            case "type": return t.getType();
            case "payment_method": return t.getPaymentMethod();
            case "location": return t.getLocation();
            case "merchant": return t.getMerchant();
            case "tag": return t.getTag();
            case "recurrence": return t.getRecurrence();
            default: return "Unclassified";
        }
    }

    /**
     * A custom UI for scrollbars with a modern look, featuring a rounded thumb and track.
     */
    private static class ModernScrollBarUI extends BasicScrollBarUI {
        private static final Color TRACK_COLOR = new Color(240, 240, 240);
        private static final Color THUMB_COLOR = new Color(100, 149, 237);
        private static final int THUMB_WIDTH = 10;
        private static final int TRACK_MARGIN = 2;

        @Override
        protected void configureScrollBarColors() {
            trackColor = TRACK_COLOR;
            thumbColor = THUMB_COLOR;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(TRACK_COLOR);
            g2.fillRoundRect(trackBounds.x + TRACK_MARGIN, trackBounds.y + TRACK_MARGIN,
                    trackBounds.width - 2 * TRACK_MARGIN, trackBounds.height - 2 * TRACK_MARGIN, 10, 10);
            g2.dispose();
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (!thumbBounds.isEmpty() && this.scrollbar.isEnabled()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(THUMB_COLOR);
                g2.fillRoundRect(thumbBounds.x + TRACK_MARGIN, thumbBounds.y + TRACK_MARGIN,
                        thumbBounds.width - 2 * TRACK_MARGIN, thumbBounds.height - 2 * TRACK_MARGIN, 10, 10);
                g2.dispose();
            }
        }

        @Override
        protected Dimension getMinimumThumbSize() {
            return new Dimension(THUMB_WIDTH, THUMB_WIDTH);
        }
    }

    public void refreshData() {
        updateChart();
    }

}
