package View.PersonalCenter;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory; // 添加导入
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;

import Controller.PersonCenterController;
import Controller.TransactionController;
import Model.Transaction;
import Model.UserSession;
import Service.PersonChartDataService;
import Service.PersonFinancialService;
import View.PersonalCenter.PersonalCenterComponents.GradientBorder;
import View.PersonalCenter.PersonalCenterComponents.GradientPanel;

public class PersonalCenterPanel extends JPanel {
    private JLabel totalIncomeYearLabel, totalExpenseYearLabel, totalBalanceYearLabel;
    private JLabel accountBalanceLabel, incomeChangeLabel, expenseChangeLabel;
    private JTextArea paymentLocationSummary;
    private JComboBox<Integer> yearComboBox;
    private JPanel annualChartPanel, incomeChartPanel, expenseChartPanel;
    private JPanel incomeCategoriesPanel, expenseCategoriesPanel; // 保存对面板的引用
    private JLabel selectedIncomeCategoryLabel, selectedExpenseCategoryLabel;
    private List<Map.Entry<String, Double>> incomeCategories = new ArrayList<>();
    private List<Map.Entry<String, Double>> expenseCategories = new ArrayList<>();
    private double annualIncome = 0, annualExpense = 0;
    private Map<String, Double> monthlyIncomes, monthlyExpenses;
    private boolean showIncome = true, showExpense = true;
    private int incomeIndex = 0, expenseIndex = 0;
    private Timer incomeTimer, expenseTimer;
    private boolean isDataLoaded = false;
    private boolean isUILoaded = false;

    private static Map<String, List<Transaction>> transactionCache = new HashMap<>();
    private static Map<String, Long> cacheTimestamps = new HashMap<>();
    private static final long CACHE_EXPIRY_MS = 5 * 60 * 1000;

    public PersonalCenterPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        JPanel yearPanel = new GradientPanel();
        yearPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        yearPanel.setOpaque(false);
        JLabel yearLabel = new JLabel("Select Year:");
        yearLabel.setFont(labelFont);
        yearLabel.setForeground(Color.WHITE);
        yearComboBox = new JComboBox<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear - 5; i <= currentYear + 5; i++) {
            yearComboBox.addItem(i);
        }
        yearComboBox.setSelectedItem(currentYear);
        yearComboBox.setFont(labelFont);

        yearComboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = super.createArrowButton();
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
            }
        });
        yearComboBox.setOpaque(false);
        yearComboBox.setBackground(Color.WHITE);
        yearComboBox.setBorder(new GradientBorder(2, 16));
        yearComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    c.setBackground(new Color(100, 149, 237));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(new Color(50, 50, 50));
                }
                c.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
        yearPanel.add(yearLabel);
        yearPanel.add(yearComboBox);
        add(yearPanel, BorderLayout.NORTH);

        JLabel placeholder = new JLabel("Loading panel...", SwingConstants.CENTER);
        placeholder.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        placeholder.setForeground(Color.DARK_GRAY);
        add(placeholder, BorderLayout.CENTER);

        addHierarchyListener(e -> {
            if (e.getChanged().isShowing() && !isUILoaded) {
                isUILoaded = true;
                SwingUtilities.invokeLater(this::buildUI);
            }
        });
    }

    private void buildUI() {
        System.out.println("Building UI for PersonalCenterPanel");
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel && "Loading panel...".equals(((JLabel) comp).getText())) {
                remove(comp);
                break;
            }
        }

        // 使用 GridLayout 强制所有单元格大小相等
        JPanel mainContent = new JPanel(new GridLayout(2, 3, 10, 10));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 第一行，左：Financial Overview
        JPanel financialOverviewPanel = createFinancialOverviewPanel();
        mainContent.add(financialOverviewPanel);

        // 第一行，中：Income Categories
        String username = UserSession.getCurrentUsername();
        String year = String.valueOf(yearComboBox.getSelectedItem());
        incomeChartPanel = createDonutChartPanel(username, false, year);
        incomeCategoriesPanel = new JPanel(new BorderLayout());
        incomeCategoriesPanel.setOpaque(false);
        incomeCategoriesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel incomeTitle = new JLabel("Income Categories", SwingConstants.CENTER);
        incomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        incomeTitle.setForeground(Color.DARK_GRAY);
        incomeTitle.setBorder(new EmptyBorder(5, 0, 10, 0));
        incomeCategoriesPanel.add(incomeTitle, BorderLayout.NORTH);
        incomeCategoriesPanel.add(incomeChartPanel, BorderLayout.CENTER);
        selectedIncomeCategoryLabel = new JLabel("Click a category to view details", SwingConstants.CENTER);
        selectedIncomeCategoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        selectedIncomeCategoryLabel.setForeground(Color.DARK_GRAY);
        incomeCategoriesPanel.add(selectedIncomeCategoryLabel, BorderLayout.SOUTH);
        mainContent.add(incomeCategoriesPanel);

        // 第一行，右：Expense Categories
        expenseChartPanel = createDonutChartPanel(username, true, year);
        expenseCategoriesPanel = new JPanel(new BorderLayout());
        expenseCategoriesPanel.setOpaque(false);
        expenseCategoriesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel expenseTitle = new JLabel("Expense Categories", SwingConstants.CENTER);
        expenseTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        expenseTitle.setForeground(Color.DARK_GRAY);
        expenseTitle.setBorder(new EmptyBorder(5, 0, 10, 0));
        expenseCategoriesPanel.add(expenseTitle, BorderLayout.NORTH);
        expenseCategoriesPanel.add(expenseChartPanel, BorderLayout.CENTER);
        selectedExpenseCategoryLabel = new JLabel("Click a category to view details", SwingConstants.CENTER);
        selectedExpenseCategoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        selectedExpenseCategoryLabel.setForeground(Color.DARK_GRAY);
        expenseCategoriesPanel.add(selectedExpenseCategoryLabel, BorderLayout.SOUTH);
        mainContent.add(expenseCategoriesPanel);

        // 第二行，左：Payment & Location Summary
        JPanel paymentLocationPanel = createPaymentLocationPanel();
        mainContent.add(paymentLocationPanel);

        // 第二行，中：占位符
        JLabel placeholder = new JLabel("Placeholder", SwingConstants.CENTER);
        placeholder.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        placeholder.setForeground(Color.DARK_GRAY);
        JPanel placeholderPanel = new JPanel(new BorderLayout());
        placeholderPanel.setOpaque(false);
        placeholderPanel.add(placeholder, BorderLayout.CENTER);
        mainContent.add(placeholderPanel);

        // 第二行，右：Income & Expense Trends
        annualChartPanel = createTrendChartsPanel();
        mainContent.add(annualChartPanel);

        add(mainContent, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void updateCharts(String username, String year) {
        System.out.println("Updating charts for user: " + username + ", year: " + year);
        // 创建新的图表面板
        incomeChartPanel = createDonutChartPanel(username, false, year);
        expenseChartPanel = createDonutChartPanel(username, true, year);

        // 更新 incomeCategoriesPanel 和 expenseCategoriesPanel 的内容
        incomeCategoriesPanel.removeAll();
        JLabel incomeTitle = new JLabel("Income Categories", SwingConstants.CENTER);
        incomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        incomeTitle.setForeground(Color.DARK_GRAY);
        incomeTitle.setBorder(new EmptyBorder(5, 0, 10, 0));
        incomeCategoriesPanel.add(incomeTitle, BorderLayout.NORTH);
        incomeCategoriesPanel.add(incomeChartPanel, BorderLayout.CENTER);
        selectedIncomeCategoryLabel.setText("Click a category to view details");
        incomeCategoriesPanel.add(selectedIncomeCategoryLabel, BorderLayout.SOUTH);

        expenseCategoriesPanel.removeAll();
        JLabel expenseTitle = new JLabel("Expense Categories", SwingConstants.CENTER);
        expenseTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        expenseTitle.setForeground(Color.DARK_GRAY);
        expenseTitle.setBorder(new EmptyBorder(5, 0, 10, 0));
        expenseCategoriesPanel.add(expenseTitle, BorderLayout.NORTH);
        expenseCategoriesPanel.add(expenseChartPanel, BorderLayout.CENTER);
        selectedExpenseCategoryLabel.setText("Click a category to view details");
        expenseCategoriesPanel.add(selectedExpenseCategoryLabel, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    private static boolean isCacheValid(String username) {
        Long timestamp = cacheTimestamps.get(username);
        if (timestamp == null) return false;
        return (System.currentTimeMillis() - timestamp) < CACHE_EXPIRY_MS;
    }

    private static List<Transaction> getCachedTransactions(String username) {
        if (isCacheValid(username)) {
            return transactionCache.getOrDefault(username, new ArrayList<>());
        }
        List<Transaction> transactions = TransactionController.readTransactions(username);
        System.out.println("Transactions loaded for user " + username + ": " + (transactions != null ? transactions.size() : 0));
        transactionCache.put(username, transactions != null ? transactions : new ArrayList<>());
        cacheTimestamps.put(username, System.currentTimeMillis());
        return transactionCache.get(username);
    }

    private static List<Transaction> getFilteredTransactions(String username, String year) {
        List<Transaction> transactions = getCachedTransactions(username);
        List<Transaction> filtered = new ArrayList<>();
        String yearPrefix = year + "/";

        for (Transaction tx : transactions) {
            try {
                String timestamp = tx.getTimestamp();
                if (timestamp.startsWith(yearPrefix)) {
                    filtered.add(tx);
                }
            } catch (Exception e) {
                System.err.println("Error processing transaction timestamp: " + (tx != null ? tx.getTimestamp() : "null"));
            }
        }
        System.out.println("Filtered transactions for year " + year + ": " + filtered.size());
        return filtered;
    }

    private static Map<String, Double> calculateExpenseCategoryTotals(List<Transaction> transactions) {
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Transaction tx : transactions) {
            if ("Expense".equalsIgnoreCase(tx.getOperation())) {
                String category = tx.getCategory() != null && !tx.getCategory().trim().isEmpty() ? tx.getCategory().trim().toLowerCase() : "unclassified";
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + Math.abs(tx.getAmount()));
            }
        }
        return categoryTotals;
    }

    private static Map<String, Double> calculateIncomeCategoryTotals(List<Transaction> transactions) {
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Transaction tx : transactions) {
            if ("Income".equalsIgnoreCase(tx.getOperation())) {
                String category = tx.getCategory() != null && !tx.getCategory().trim().isEmpty() ? tx.getCategory().trim() : "Unclassified";
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + tx.getAmount());
            }
        }
        return categoryTotals;
    }

    private JPanel createDonutChartPanel(String username, boolean isExpense, String year) {
        JPanel panel = new JPanel() {
            private List<Map.Entry<String, Double>> categoryEntries = new ArrayList<>();
            private int currentDisplayIndex = 0;
            private Timer displayTimer;
            private double totalAmount = 0.0;
    
            {
                List<Transaction> transactions = getFilteredTransactions(username, year);
                System.out.println("Filtered transactions for " + (isExpense ? "expense" : "income") + ": " + transactions.size());
                Map<String, Double> categoryTotals = isExpense
                        ? calculateExpenseCategoryTotals(transactions)
                        : calculateIncomeCategoryTotals(transactions);
    
                totalAmount = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();
                categoryEntries.clear();
                // 按金额降序排序，合并小类别
                Map<String, Double> mergedTotals = mergeSmallCategories(categoryTotals, totalAmount);
                categoryEntries.addAll(mergedTotals.entrySet().stream()
                        .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                        .collect(Collectors.toList()));
    
                displayTimer = new Timer(2000, e -> {
                    if (!categoryEntries.isEmpty()) {
                        currentDisplayIndex = (currentDisplayIndex + 1) % categoryEntries.size();
                        repaint();
                    }
                });
                displayTimer.start();
    
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int centerX = getWidth() / 2;
                        int centerY = getHeight() / 2;
                        int diameter = Math.min(getWidth(), getHeight()) - getMargin();
                        int radius = diameter / 2;
                        int innerRadius = radius / 2;
    
                        double dx = e.getX() - centerX;
                        double dy = e.getY() - centerY;
                        double distance = Math.sqrt(dx * dx + dy * dy);
    
                        if (distance >= innerRadius && distance <= radius) {
                            double angle = Math.toDegrees(Math.atan2(-dy, dx));
                            if (angle < 0) angle += 360;
    
                            int startAngle = 0;
                            for (Map.Entry<String, Double> entry : categoryEntries) {
                                double percentage = entry.getValue() / totalAmount;
                                int arcAngle = (int) Math.round(percentage * 360);
                                if (arcAngle == 0 && entry.getValue() > 0) {
                                    arcAngle = 1;
                                }
    
                                if (angle >= startAngle && angle < startAngle + arcAngle) {
                                    String labelText = "%s: ¥%.2f (%.1f%%)".formatted(
                                            entry.getKey(), entry.getValue(), percentage * 100);
                                    if (isExpense) {
                                        selectedExpenseCategoryLabel.setText(labelText);
                                    } else {
                                        selectedIncomeCategoryLabel.setText(labelText);
                                    }
                                    break;
                                }
                                startAngle += arcAngle;
                            }
                        }
                    }
                });
            }
    
            private int getMargin() {
                // 动态调整边距：5-10 个类别用 80，20+ 个类别用 120
                int categoryCount = categoryEntries.size();
                if (categoryCount > 15) {
                    return 120;
                } else if (categoryCount > 10) {
                    return 100;
                } else {
                    return 80;
                }
            }
    
            private Map<String, Double> mergeSmallCategories(Map<String, Double> categoryTotals, double totalAmount) {
                Map<String, Double> merged = new HashMap<>();
                double otherTotal = 0.0;
                for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                    double percentage = entry.getValue() / totalAmount;
                    if (percentage < 0.01) { // 金额占比 < 1% 归为 "Other"
                        otherTotal += entry.getValue();
                    } else {
                        merged.put(entry.getKey(), entry.getValue());
                    }
                }
                if (otherTotal > 0) {
                    merged.put("Other", otherTotal);
                }
                return merged;
            }
    
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, getWidth(), getHeight());
    
                if (totalAmount <= 0) {
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    g2d.drawString("No valid " + (isExpense ? "expense" : "income") + " data to display.",
                            getWidth() / 2 - 70, getHeight() / 2);
                    g2d.dispose();
                    return;
                }
    
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                int margin = getMargin();
                int diameter = Math.min(getWidth(), getHeight()) - margin;
                int radius = diameter / 2;
                int innerRadius = radius / 2;
    
                // 绘制甜甜圈
                int startAngle = 0;
                int i = 0;
                Color[] colors = {
                        new Color(255, 182, 193), new Color(144, 238, 144), new Color(135, 206, 250),
                        new Color(240, 230, 140), new Color(221, 160, 221), new Color(173, 216, 230),
                        new Color(255, 218, 185), new Color(200, 162, 200)
                };
    
                for (Map.Entry<String, Double> entry : categoryEntries) {
                    double percentage = entry.getValue() / totalAmount;
                    int arcAngle = (int) Math.round(percentage * 360);
                    if (arcAngle == 0 && entry.getValue() > 0) {
                        arcAngle = 1;
                    }
    
                    g2d.setColor(colors[i % colors.length]);
                    g2d.fillArc(centerX - radius, centerY - radius, diameter, diameter, startAngle, arcAngle);
                    startAngle += arcAngle;
                    i++;
                }
                g2d.setColor(Color.WHITE);
                g2d.fillArc(centerX - innerRadius, centerY - innerRadius, innerRadius * 2, innerRadius * 2, 0, 360);
    
                // 绘制标签（无引导线）
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                FontMetrics fm = g2d.getFontMetrics();
                List<Rectangle> labelBounds = new ArrayList<>();
                startAngle = 0;
                int maxLabels = Math.min(categoryEntries.size(), 10); // 限制最多显示 10 个标签
    
                for (int j = 0; j < categoryEntries.size() && j < maxLabels; j++) {
                    Map.Entry<String, Double> entry = categoryEntries.get(j);
                    double percentage = entry.getValue() / totalAmount;
                    int arcAngle = (int) Math.round(percentage * 360);
                    if (arcAngle == 0 && entry.getValue() > 0) {
                        arcAngle = 1;
                    }
    
                    // 仅显示弧角度 >= 5° 的标签
                    if (arcAngle < 5) {
                        startAngle += arcAngle;
                        continue;
                    }
    
                    int labelAngle = startAngle + arcAngle / 2;
                    double labelAngleRad = Math.toRadians(labelAngle);
                    String labelText = "%s: ¥%.2f (%.1f%%)".formatted(entry.getKey(), entry.getValue(), percentage * 100);
                    int textWidth = fm.stringWidth(labelText);
                    int textHeight = fm.getHeight();
    
                    // 计算标签位置
                    int labelRadius = radius + 40;
                    int labelX = centerX + (int) (labelRadius * Math.cos(labelAngleRad));
                    int labelY = centerY - (int) (labelRadius * Math.sin(labelAngleRad));
    
                    // 调整标签位置
                    int adjustedX = labelX;
                    int adjustedY = labelY;
                    if (labelX + textWidth > getWidth()) {
                        adjustedX = getWidth() - textWidth - 5;
                    } else if (labelX < 0) {
                        adjustedX = 5;
                    }
                    if (labelY - textHeight < 0) {
                        adjustedY = textHeight + 5;
                    } else if (labelY > getHeight()) {
                        adjustedY = getHeight() - 5;
                    }
    
                    // 检测重叠
                    Rectangle labelRect = new Rectangle(adjustedX - 5, adjustedY - textHeight, textWidth + 10, textHeight + 10);
                    boolean overlaps = false;
                    for (Rectangle existing : labelBounds) {
                        if (labelRect.intersects(existing)) {
                            overlaps = true;
                            // 尝试向上偏移
                            int offsetY = adjustedY - textHeight - 10;
                            if (offsetY > textHeight + 5) {
                                labelRect = new Rectangle(adjustedX - 5, offsetY - textHeight, textWidth + 10, textHeight + 10);
                                overlaps = labelBounds.stream().anyMatch(existing::intersects);
                                if (!overlaps) {
                                    adjustedY = offsetY;
                                }
                            }
                            // 尝试向下偏移
                            if (overlaps) {
                                offsetY = adjustedY + textHeight + 10;
                                if (offsetY < getHeight() - 5) {
                                    labelRect = new Rectangle(adjustedX - 5, offsetY - textHeight, textWidth + 10, textHeight + 10);
                                    overlaps = labelBounds.stream().anyMatch(existing::intersects);
                                    if (!overlaps) {
                                        adjustedY = offsetY;
                                    }
                                }
                            }
                            break;
                        }
                    }
    
                    if (!overlaps) {
                        // 绘制标签（无引导线）
                        g2d.setColor(Color.BLACK);
                        g2d.drawString(labelText, adjustedX, adjustedY);
                        labelBounds.add(labelRect);
                    }
    
                    startAngle += arcAngle;
                }
    
                // 绘制中心显示的当前类别
                if (!categoryEntries.isEmpty()) {
                    Map.Entry<String, Double> entry = categoryEntries.get(currentDisplayIndex);
                    double percentage = entry.getValue() / totalAmount;
                    String displayText = "%s: ¥%.2f (%.1f%%)".formatted(entry.getKey(), entry.getValue(), percentage * 100);
    
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    int textWidth = fm.stringWidth(displayText);
                    int textHeight = fm.getHeight();
                    int textX = centerX - textWidth / 2;
                    int textY = centerY + textHeight / 2;
    
                    g2d.drawString(displayText, textX, textY);
                }
    
                g2d.dispose();
            }
        };
        return panel;
    }

    private JPanel createFinancialOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Financial Overview", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.DARK_GRAY);
        title.setBorder(new EmptyBorder(5, 0, 10, 0));
        panel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(3, 2, 10, 10));
        content.setOpaque(false);
        totalIncomeYearLabel = new JLabel("Yearly Income: 0.00", SwingConstants.CENTER);
        totalExpenseYearLabel = new JLabel("Yearly Expense: 0.00", SwingConstants.CENTER);
        totalBalanceYearLabel = new JLabel("Yearly Balance: 0.00", SwingConstants.CENTER);
        accountBalanceLabel = new JLabel("Amount Balance: 0.00", SwingConstants.CENTER);
        incomeChangeLabel = new JLabel("Income Change: 0.0%", SwingConstants.CENTER);
        expenseChangeLabel = new JLabel("Expense Change: 0.0%", SwingConstants.CENTER);
        Color[] borderColors = {
                new Color(100, 149, 237), new Color(255, 99, 71), new Color(60, 179, 113),
                new Color(200, 162, 200), new Color(255, 182, 193)
        };
        JLabel[] labels = {totalIncomeYearLabel, totalExpenseYearLabel, totalBalanceYearLabel, accountBalanceLabel, incomeChangeLabel};
        for (int i = 0; i < labels.length; i++) {
            JLabel label = labels[i];
            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            label.setForeground(Color.DARK_GRAY);
            label.setOpaque(true);
            label.setBackground(Color.WHITE);
            label.setBorder(new LineBorder(borderColors[i % borderColors.length], 2, true) {
                @Override
                public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(getLineColor());
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(x + 2, y + 2, width - 4, height - 4, 10, 10);
                    g2d.setColor(new Color(0, 0, 0, 50));
                    g2d.drawRoundRect(x + 3, y + 3, width - 4, height - 4, 10, 10);
                }
            });
            content.add(label);
        }
        content.add(new JPanel());
        panel.add(content, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPaymentLocationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Payment & Location Summary", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.DARK_GRAY);
        title.setBorder(new EmptyBorder(5, 0, 10, 0));
        panel.add(title, BorderLayout.NORTH);

        paymentLocationSummary = new JTextArea();
        paymentLocationSummary.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        paymentLocationSummary.setForeground(Color.DARK_GRAY);
        paymentLocationSummary.setOpaque(false);
        paymentLocationSummary.setEditable(false);
        paymentLocationSummary.setLineWrap(true);
        paymentLocationSummary.setWrapStyleWord(true);
        panel.add(paymentLocationSummary, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTrendChartsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Income & Expense Trends", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.DARK_GRAY);
        title.setBorder(new EmptyBorder(5, 0, 10, 0));
        panel.add(title, BorderLayout.NORTH);

        annualChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (!isDataLoaded) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    g2d.drawString("Loading chart...", getWidth() / 2 - 50, getHeight() / 2);
                    return;
                }

                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int lineWidth = 400;
                int lineHeight = 10;
                double total = Math.max(annualIncome + annualExpense, 1);
                int redLength = (int) ((annualIncome / total) * lineWidth);
                int greenLength = lineWidth - redLength;

                g2d.setColor(new Color(255, 99, 71));
                g2d.fillRect(25, 80, redLength, lineHeight);
                g2d.setColor(new Color(60, 179, 113));
                g2d.fillRect(25 + redLength, 80, greenLength, lineHeight);

                g2d.setColor(Color.DARK_GRAY);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g2d.drawString("Annual Income (¥)", 25, 60);
                g2d.drawString(String.format("%.2f", annualIncome), 25, 75);
                g2d.drawString("Annual Expense (¥)", 25 + lineWidth - 100, 60);
                g2d.drawString(String.format("%.2f", annualExpense), 25 + lineWidth - 50, 75);
                g2d.drawString("Annual Balance: " + String.format("%.2f", annualIncome - annualExpense) + " ¥",
                        25 + lineWidth - 100, 110);

                int chartX = 60;
                int chartY = 150;
                int chartWidth = lineWidth;
                int chartHeight = 200;

                double maxAmount = 0;
                if (monthlyIncomes != null && monthlyExpenses != null) {
                    for (int i = 1; i <= 12; i++) {
                        String month = String.format("%02d", i);
                        maxAmount = Math.max(maxAmount, monthlyIncomes.getOrDefault(month, 0.0));
                        maxAmount = Math.max(maxAmount, monthlyExpenses.getOrDefault(month, 0.0));
                    }
                }
                maxAmount = Math.max(maxAmount, 1);

                g2d.setColor(Color.BLACK);
                g2d.drawLine(chartX, chartY, chartX, chartY + chartHeight);
                g2d.drawLine(chartX, chartY + chartHeight, chartX + chartWidth, chartY + chartHeight);

                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                int numTicks = 5;
                double amountStep = maxAmount / numTicks;
                for (int i = 0; i <= numTicks; i++) {
                    double amount = i * amountStep;
                    int y = chartY + chartHeight - (int) ((amount / maxAmount) * chartHeight);
                    g2d.drawString(String.format("%.0f ¥", amount), chartX - 50, y + 5);
                }

                for (int i = 0; i < 12; i++) {
                    int x = chartX + (i * chartWidth / 12);
                    g2d.drawString(String.format("%02d", i + 1), x, chartY + chartHeight + 20);
                }

                if (monthlyIncomes != null && monthlyExpenses != null) {
                    Path2D incomePath = new Path2D.Double();
                    Path2D expensePath = new Path2D.Double();
                    int[] incomePointsX = new int[12];
                    int[] incomePointsY = new int[12];
                    int[] expensePointsX = new int[12];
                    int[] expensePointsY = new int[12];

                    for (int i = 0; i < 12; i++) {
                        String month = String.format("%02d", i + 1);
                        double income = monthlyIncomes.getOrDefault(month, 0.0);
                        double expense = monthlyExpenses.getOrDefault(month, 0.0);
                        incomePointsX[i] = chartX + (i * chartWidth / 12);
                        incomePointsY[i] = chartY + chartHeight - (int) ((income / maxAmount) * chartHeight);
                        expensePointsX[i] = chartX + (i * chartWidth / 12);
                        expensePointsY[i] = chartY + chartHeight - (int) ((expense / maxAmount) * chartHeight);
                    }

                    if (showIncome) {
                        g2d.setColor(new Color(255, 99, 71));
                        incomePath.moveTo(incomePointsX[0], incomePointsY[0]);
                        for (int i = 1; i < 12; i++) {
                            int x1 = incomePointsX[i - 1];
                            int y1 = incomePointsY[i - 1];
                            int x2 = incomePointsX[i];
                            int y2 = incomePointsY[i];
                            int cx = (x1 + x2) / 2;
                            incomePath.curveTo(cx, y1, cx, y2, x2, y2);
                        }
                        g2d.draw(incomePath);
                    }

                    if (showExpense) {
                        g2d.setColor(new Color(60, 179, 113));
                        expensePath.moveTo(expensePointsX[0], expensePointsY[0]);
                        for (int i = 1; i < 12; i++) {
                            int x1 = expensePointsX[i - 1];
                            int y1 = expensePointsY[i - 1];
                            int x2 = expensePointsX[i];
                            int y2 = expensePointsY[i];
                            int cx = (x1 + x2) / 2;
                            expensePath.curveTo(cx, y1, cx, y2, x2, y2);
                        }
                        g2d.draw(expensePath);
                    }
                }
            }
        };
        annualChartPanel.setPreferredSize(new Dimension(450, 400));
        annualChartPanel.setOpaque(false);
        panel.add(annualChartPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        controlPanel.setOpaque(false);
        JButton toggleIncomeButton = new JButton("Toggle Income");
        toggleIncomeButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        toggleIncomeButton.setBackground(new Color(255, 99, 71));
        toggleIncomeButton.setForeground(Color.WHITE);
        toggleIncomeButton.setFocusPainted(false);
        toggleIncomeButton.addActionListener(e -> {
            showIncome = !showIncome;
            annualChartPanel.repaint();
        });
        JButton toggleExpenseButton = new JButton("Toggle Expense");
        toggleExpenseButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        toggleExpenseButton.setBackground(new Color(60, 179, 113));
        toggleExpenseButton.setForeground(Color.WHITE);
        toggleExpenseButton.setFocusPainted(false);
        toggleExpenseButton.addActionListener(e -> {
            showExpense = !showExpense;
            annualChartPanel.repaint();
        });
        controlPanel.add(toggleIncomeButton);
        controlPanel.add(toggleExpenseButton);
        panel.add(controlPanel, BorderLayout.SOUTH);

        return panel;
    }

    public void initializeData(PersonCenterController controller) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Initializing data for PersonalCenterPanel, isUILoaded: " + isUILoaded);
            if (!isUILoaded) {
                isUILoaded = true;
                buildUI();
            }
            int selectedYear = (Integer) yearComboBox.getSelectedItem();
            controller.loadData(selectedYear);

            if (incomeTimer == null) {
                incomeTimer = new Timer(2000, e -> {
                    if (isShowing()) {
                        incomeIndex++;
                        incomeChartPanel.repaint();
                    }
                });
                incomeTimer.start();
            }
            if (expenseTimer == null) {
                expenseTimer = new Timer(2000, e -> {
                    if (isShowing()) {
                        expenseIndex++;
                        expenseChartPanel.repaint();
                    }
                });
                expenseTimer.start();
            }

            yearComboBox.addActionListener(e -> {
                int newYear = (Integer) yearComboBox.getSelectedItem();
                controller.loadData(newYear);
                String newYearStr = String.valueOf(newYear);
                updateCharts(UserSession.getCurrentUsername(), newYearStr);
            });
        });
    }

    public void updateFinancialSummary(PersonFinancialService.FinancialSummary summary) {
        totalIncomeYearLabel.setText("Yearly Income: " + String.format("%.2f", summary.getTotalIncomeYear()));
        totalExpenseYearLabel.setText("Yearly Expense: " + String.format("%.2f", summary.getTotalExpenseYear()));
        totalBalanceYearLabel.setText("Yearly Balance: " + String.format("%.2f", summary.getTotalBalanceYear()));
        accountBalanceLabel.setText("Amount Balance: " + String.format("%.2f", summary.getAccountBalance()));
        incomeChangeLabel.setText("Income Change: " + String.format("%.1f%%", summary.getIncomeChangeYear()));
        expenseChangeLabel.setText("Expense Change: " + String.format("%.1f%%", summary.getExpenseChangeYear()));
        isDataLoaded = true;
        repaint();
    }

    public void updatePaymentLocationSummary(String summary) {
        paymentLocationSummary.setText(summary);
        isDataLoaded = true;
        repaint();
    }

    public void updateAnnualChartData(PersonChartDataService.AnnualChartData data) {
        this.annualIncome = data.getAnnualIncome();
        this.annualExpense = data.getAnnualExpense();
        this.monthlyIncomes = data.getMonthlyIncomes();
        this.monthlyExpenses = data.getMonthlyExpenses();
        isDataLoaded = true;
        annualChartPanel.repaint();
    }

    public void updateCategoryChartData(PersonChartDataService.CategoryChartData data) {
        this.incomeCategories = data.getIncomeCategories();
        this.expenseCategories = data.getExpenseCategories();
        isDataLoaded = true;
        incomeChartPanel.repaint();
        expenseChartPanel.repaint();
    }

    public JPanel getExpenseChartPanel(String username, String yearMonth) {
        if (expenseChartPanel == null) {
            System.out.println("expenseChartPanel is null, rebuilding UI");
            buildUI();
        }
        return expenseChartPanel;
    }

    public JPanel getIncomeChartPanel(String username, String yearMonth) {
        if (incomeChartPanel == null) {
            System.out.println("incomeChartPanel is null, rebuilding UI");
            buildUI();
        }
        return incomeChartPanel;
    }

    public JComboBox<Integer> getYearComboBox() {
        return yearComboBox;
    }
}