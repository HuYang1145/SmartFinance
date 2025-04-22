package UI;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;

public class PersonalCenterPanel extends JPanel {
    private JLabel totalIncomeYearLabel, totalExpenseYearLabel, totalBalanceYearLabel;
    private JLabel accountBalanceLabel, incomeChangeLabel, expenseChangeLabel;
    private String userId;
    private AnnualChartPanel annualChartPanel;
    private CategoryChartPanel categoryChartPanel;
    private JTextArea paymentLocationSummary;
    private JComboBox<Integer> yearComboBox;

    public PersonalCenterPanel(String userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245)); // Soft light gray
    
        // Font setup
        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Font titleFont = new Font("Segoe UI", Font.BOLD, 16);
    
        // Year selector (top)
        JPanel yearPanel = new RoundedInputField.GradientPanel();
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

// 1) 安装一个透明的箭头按钮，避免箭头区域破坏圆角／渐变
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

// 2) 让 comboBox 自己绘制白底+紫蓝渐变边框
        yearComboBox.setOpaque(false);
        yearComboBox.setBackground(Color.WHITE);
        yearComboBox.setBorder(new AccountManagementUI.GradientBorder(2, 16));

// 3) 保留你原来的列表渲染器设置（选中项高亮逻辑）：
        yearComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                                                          Object value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    c.setBackground(new Color(100,149,237));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(new Color(50,50,50));
                }
                c.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
                return c;
            }
        });

// 4) 把它添加到 yearPanel
        yearPanel.add(yearLabel);
        yearPanel.add(yearComboBox);
        add(yearPanel, BorderLayout.NORTH);
    
        // Main content panel
        JPanel mainContent = new JPanel(new GridLayout(2, 3, 10, 10));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(20, 20, 20, 20));
    
        // Plate 1: Financial Overview (Top Left)
        JPanel financialOverviewPanel = createFinancialOverviewPanel();
        financialOverviewPanel.setPreferredSize(new Dimension(0, 540));
        mainContent.add(financialOverviewPanel);
    
        // Plate 3: Category Analysis (Top Middle)
        JPanel incomeCategoriesPanel = createIncomeCategoriesPanel();
        mainContent.add(incomeCategoriesPanel);
    
        // Plate 4: Expense Categories (Top Right)
        JPanel expenseCategoriesPanel = createExpenseCategoriesPanel();
        mainContent.add(expenseCategoriesPanel);
    
        // Plate 2: Placeholder (Bottom Left)
        JPanel placeholderPanel = new JPanel(new BorderLayout());
        placeholderPanel.setOpaque(false);
        placeholderPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel placeholderLabel = new JLabel("Placeholder", SwingConstants.CENTER);
        placeholderLabel.setFont(labelFont);
        placeholderLabel.setForeground(Color.DARK_GRAY);
        placeholderPanel.add(placeholderLabel, BorderLayout.CENTER);
        mainContent.add(placeholderPanel);
    
        // Plate 5: Payment & Location Summary (Bottom Middle)
        JPanel paymentLocationPanel = createPaymentLocationPanel();
        mainContent.add(paymentLocationPanel);
    
        // Plate 6: Income & Expense Trends (Bottom Right)
        JPanel trendChartsPanel = createTrendChartsPanel();
        mainContent.add(trendChartsPanel);
    
        add(mainContent, BorderLayout.CENTER);
    
        // Load data
        loadData(currentYear);
    }
    
    private JPanel createIncomeCategoriesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    
        // Title
        JLabel title = new JLabel("Income Categories", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.DARK_GRAY);
        title.setBorder(new EmptyBorder(5, 0, 10, 0));
        panel.add(title, BorderLayout.NORTH);
    
        // Chart
        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        categoryChartPanel = new CategoryChartPanel();
        JPanel incomeChart = categoryChartPanel.createIncomeChart(userId, currentYear);
        incomeChart.setPreferredSize(new Dimension(300, 300));
        panel.add(incomeChart, BorderLayout.CENTER);
    
        // Selected Category Info
        JLabel selectedCategoryLabel = new JLabel("Click a category to view details", SwingConstants.CENTER);
        selectedCategoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        selectedCategoryLabel.setForeground(Color.DARK_GRAY);
        categoryChartPanel.setSelectedCategoryLabel(selectedCategoryLabel);
        panel.add(selectedCategoryLabel, BorderLayout.SOUTH);
    
        return panel;
    }
    
    private JPanel createExpenseCategoriesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    
        // Title
        JLabel title = new JLabel("Expense Categories", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.DARK_GRAY);
        title.setBorder(new EmptyBorder(5, 0, 10, 0));
        panel.add(title, BorderLayout.NORTH);
    
        // Chart
        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        JPanel expenseChart = categoryChartPanel.createExpenseChart(userId, currentYear);
        expenseChart.setPreferredSize(new Dimension(300, 300));
        panel.add(expenseChart, BorderLayout.CENTER);
    
        return panel;
    }

    private JPanel createFinancialOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Title
        JLabel title = new JLabel("Financial Overview", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.DARK_GRAY);
        title.setBorder(new EmptyBorder(5, 0, 10, 0));
        panel.add(title, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel(new GridLayout(3, 2, 10, 10));
        content.setOpaque(false);
        totalIncomeYearLabel = new JLabel("Yearly Income: 0.00", SwingConstants.CENTER);
        totalExpenseYearLabel = new JLabel("Yearly Expense: 0.00", SwingConstants.CENTER);
        totalBalanceYearLabel = new JLabel("Yearly Balance: 0.00", SwingConstants.CENTER);
        accountBalanceLabel = new JLabel("Account Balance: 0.00", SwingConstants.CENTER);
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
        // Add empty panel for the last grid cell
        content.add(new JPanel());
        panel.add(content, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCategoryAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    
        // Title
        JLabel title = new JLabel("Category Analysis", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.DARK_GRAY);
        title.setBorder(new EmptyBorder(5, 0, 10, 0));
        panel.add(title, BorderLayout.NORTH);
    
        // Charts (1x2 layout for two plates)
        JPanel charts = new JPanel(new GridLayout(1, 2, 10, 10));
        charts.setOpaque(false);
        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        categoryChartPanel = new CategoryChartPanel();
        JPanel incomePanel = new JPanel(new BorderLayout());
        incomePanel.setOpaque(false);
        JLabel incomeTitle = new JLabel("Income Categories", SwingConstants.CENTER);
        incomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        incomeTitle.setForeground(Color.DARK_GRAY);
        incomePanel.add(incomeTitle, BorderLayout.NORTH);
        JPanel incomeChart = categoryChartPanel.createIncomeChart(userId, currentYear);
        incomeChart.setPreferredSize(new Dimension(300, 300));
        incomePanel.add(incomeChart, BorderLayout.CENTER);
        JPanel expensePanel = new JPanel(new BorderLayout());
        expensePanel.setOpaque(false);
        JLabel expenseTitle = new JLabel("Expense Categories", SwingConstants.CENTER);
        expenseTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        expenseTitle.setForeground(Color.DARK_GRAY);
        expensePanel.add(expenseTitle, BorderLayout.NORTH);
        JPanel expenseChart = categoryChartPanel.createExpenseChart(userId, currentYear);
        expenseChart.setPreferredSize(new Dimension(300, 300));
        expensePanel.add(expenseChart, BorderLayout.CENTER);
        charts.add(incomePanel);
        charts.add(expensePanel);
        panel.add(charts, BorderLayout.CENTER);
    
        // Selected Category Info
        JLabel selectedCategoryLabel = new JLabel("Click a category to view details", SwingConstants.CENTER);
        selectedCategoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        selectedCategoryLabel.setForeground(Color.DARK_GRAY);
        categoryChartPanel.setSelectedCategoryLabel(selectedCategoryLabel);
        panel.add(selectedCategoryLabel, BorderLayout.SOUTH);
    
        return panel;
    }
    
    private JPanel createTrendChartsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    
        // Title
        JLabel title = new JLabel("Income & Expense Trends", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.DARK_GRAY);
        title.setBorder(new EmptyBorder(5, 0, 10, 0));
        panel.add(title, BorderLayout.NORTH);
    
        // Control panel
        JPanel controlPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        controlPanel.setOpaque(false);
        JButton toggleIncomeButton = new JButton("Toggle Income");
        toggleIncomeButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        toggleIncomeButton.setBackground(new Color(255, 99, 71));
        toggleIncomeButton.setForeground(Color.WHITE);
        toggleIncomeButton.setFocusPainted(false);
        toggleIncomeButton.addActionListener(e -> annualChartPanel.toggleIncome());
        JButton toggleExpenseButton = new JButton("Toggle Expense");
        toggleExpenseButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        toggleExpenseButton.setBackground(new Color(60, 179, 113));
        toggleExpenseButton.setForeground(Color.WHITE);
        toggleExpenseButton.setFocusPainted(false);
        toggleExpenseButton.addActionListener(e -> annualChartPanel.toggleExpense());
        controlPanel.add(toggleIncomeButton);
        controlPanel.add(toggleExpenseButton);
        panel.add(controlPanel, BorderLayout.SOUTH);
    
        // Chart
        annualChartPanel = new AnnualChartPanel();
        panel.add(annualChartPanel, BorderLayout.CENTER);
    
        return panel;
    }
    
    private JPanel createPaymentLocationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    
        // Title
        JLabel title = new JLabel("Payment & Location Summary", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.DARK_GRAY);
        title.setBorder(new EmptyBorder(5, 0, 10, 0));
        panel.add(title, BorderLayout.NORTH);
    
        // Summary Text
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
    
    private void loadData(int selectedYear) {
        List<Transaction> transactions = readTransactions(userId);
        double totalIncomeYear = 0, totalExpenseYear = 0;
        double totalIncomeLastYear = 0, totalExpenseLastYear = 0;
        double accountBalance = 0;
        int lastYear = selectedYear - 1;
    
        // Clear previous data
        annualChartPanel.resetData();
        categoryChartPanel.updateData(transactions, selectedYear);
    
        // Process transactions
        for (Transaction tx : transactions) {
            String txYear = tx.time.substring(0, 4);
            if ("Income".equals(tx.operation)) {
                accountBalance += tx.amount;
                if (txYear.equals(String.valueOf(selectedYear))) {
                    totalIncomeYear += tx.amount;
                    annualChartPanel.addAnnualIncome(tx.amount);
                    annualChartPanel.addMonthlyIncome(tx.time.substring(5, 7), tx.amount);
                }
                if (txYear.equals(String.valueOf(lastYear))) {
                    totalIncomeLastYear += tx.amount;
                }
            } else if ("Expense".equals(tx.operation)) {
                accountBalance -= tx.amount;
                if (txYear.equals(String.valueOf(selectedYear))) {
                    totalExpenseYear += tx.amount;
                    annualChartPanel.addAnnualExpense(tx.amount);
                    annualChartPanel.addMonthlyExpense(tx.time.substring(5, 7), tx.amount);
                }
                if (txYear.equals(String.valueOf(lastYear))) {
                    totalExpenseLastYear += tx.amount;
                }
            }
        }
    
        // Update Financial Overview
        totalIncomeYearLabel.setText("Yearly Income: " + String.format("%.2f", totalIncomeYear));
        totalExpenseYearLabel.setText("Yearly Expense: " + String.format("%.2f", totalExpenseYear));
        totalBalanceYearLabel.setText("Yearly Balance: " + String.format("%.2f", totalIncomeYear - totalExpenseYear));
        accountBalanceLabel.setText("Account Balance: " + String.format("%.2f", accountBalance));
        double incomeChangeYear = totalIncomeLastYear > 0 ? ((totalIncomeYear - totalIncomeLastYear) / totalIncomeLastYear) * 100 : 0;
        double expenseChangeYear = totalExpenseLastYear > 0 ? ((totalExpenseYear - totalExpenseLastYear) / totalExpenseLastYear) * 100 : 0;
        incomeChangeLabel.setText("Income Change: " + String.format("%.1f%%", incomeChangeYear));
        expenseChangeLabel.setText("Expense Change: " + String.format("%.1f%%", expenseChangeYear));
    
        // Update Payment & Location Summary
        Map<String, Double> paymentMethods = new HashMap<>();
        Map<String, Double> locations = new HashMap<>();
        double totalExpense = 0;
        int transactionCount = 0;
        double maxSingleTransaction = 0;
        String maxTransactionCategory = "None";
        for (Transaction tx : transactions) {
            if ("Expense".equals(tx.operation) && tx.time.startsWith(String.valueOf(selectedYear))) {
                paymentMethods.put(tx.paymentMethod, paymentMethods.getOrDefault(tx.paymentMethod, 0.0) + tx.amount);
                locations.put(tx.location, locations.getOrDefault(tx.location, 0.0) + tx.amount);
                totalExpense += tx.amount;
                transactionCount++;
                if (tx.amount > maxSingleTransaction) {
                    maxSingleTransaction = tx.amount;
                    maxTransactionCategory = tx.category;
                }
            }
        }
        final double finalTotalExpense = totalExpense > 0 ? totalExpense : 1;
        String primaryPayment = paymentMethods.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(entry -> String.format("%s (%.1f%%)", entry.getKey(), (entry.getValue() / finalTotalExpense) * 100))
            .orElse("No payment data");
        String primaryLocation = locations.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(entry -> String.format("%s (%.1f%%)", entry.getKey(), (entry.getValue() / finalTotalExpense) * 100))
            .orElse("No location data");
        paymentLocationSummary.setText(String.format("""
                                                     Summary:
                                                     - Most transactions were made via %s.
                                                     - Primary transaction locations were %s.
                                                     - Total transactions: %d
                                                     - Highest single transaction: \u00a5%.2f (%s)""",
            primaryPayment, primaryLocation, transactionCount, maxSingleTransaction, maxTransactionCategory
        ));
    
        annualChartPanel.repaint();
    }

    private List<Transaction> readTransactions(String userId) {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("transactions.csv"))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",", -1);
                if (data.length >= 13 && data[0].equals(userId)) {
                    transactions.add(new Transaction(
                        data[1], Double.parseDouble(data[2]), data[3], data[7],
                        data[8], data[9], data[12]
                    ));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    class AnnualChartPanel extends JPanel {
        private double annualIncome = 0, annualExpense = 0;
        private Map<String, Double> monthlyIncomes = new HashMap<>();
        private Map<String, Double> monthlyExpenses = new HashMap<>();
        private boolean showIncome = true;
        private boolean showExpense = true;

        public AnnualChartPanel() {
            setPreferredSize(new Dimension(450, 400));
            setOpaque(false);
        }

        public void resetData() {
            annualIncome = 0;
            annualExpense = 0;
            monthlyIncomes.clear();
            monthlyExpenses.clear();
            for (int i = 1; i <= 12; i++) {
                String month = String.format("%02d", i);
                monthlyIncomes.put(month, 0.0);
                monthlyExpenses.put(month, 0.0);
            }
        }

        public void addAnnualIncome(double amount) {
            annualIncome += amount;
        }

        public void addAnnualExpense(double amount) {
            annualExpense += amount;
        }

        public void addMonthlyIncome(String month, double amount) {
            monthlyIncomes.put(month, monthlyIncomes.getOrDefault(month, 0.0) + amount);
        }

        public void addMonthlyExpense(String month, double amount) {
            monthlyExpenses.put(month, monthlyExpenses.getOrDefault(month, 0.0) + amount);
        }

        public void toggleIncome() {
            showIncome = !showIncome;
            repaint();
        }

        public void toggleExpense() {
            showExpense = !showExpense;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Horizontal Bar Chart
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

            // Trend Chart (Smooth Curve)
            int chartX = 60;
            int chartY = 150;
            int chartWidth = lineWidth;
            int chartHeight = 200;

            // Find max amount for scaling
            double maxAmount = 0;
            for (int i = 1; i <= 12; i++) {
                String month = String.format("%02d", i);
                maxAmount = Math.max(maxAmount, monthlyIncomes.getOrDefault(month, 0.0));
                maxAmount = Math.max(maxAmount, monthlyExpenses.getOrDefault(month, 0.0));
            }
            maxAmount = Math.max(maxAmount, 1);

            // Draw axes
            g2d.setColor(Color.BLACK);
            g2d.drawLine(chartX, chartY, chartX, chartY + chartHeight);
            g2d.drawLine(chartX, chartY + chartHeight, chartX + chartWidth, chartY + chartHeight);

            // Y-axis labels
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            int numTicks = 5;
            double amountStep = maxAmount / numTicks;
            for (int i = 0; i <= numTicks; i++) {
                double amount = i * amountStep;
                int y = chartY + chartHeight - (int) ((amount / maxAmount) * chartHeight);
                g2d.drawString(String.format("%.0f ¥", amount), chartX - 50, y + 5);
            }

            // X-axis labels
            for (int i = 0; i < 12; i++) {
                int x = chartX + (i * chartWidth / 12);
                g2d.drawString(String.format("%02d", i + 1), x, chartY + chartHeight + 20);
            }

            // Draw smooth curves
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

    class CategoryChartPanel {
        private List<Map.Entry<String, Double>> incomeCategories = new ArrayList<>();
        private List<Map.Entry<String, Double>> expenseCategories = new ArrayList<>();
        private JLabel selectedCategoryLabel;
        private int incomeIndex = 0, expenseIndex = 0;
        private Timer incomeTimer, expenseTimer;

        public CategoryChartPanel() {
            selectedCategoryLabel = new JLabel("Click a category to view details", SwingConstants.CENTER);
        }

        public JPanel createIncomeChart(String userId, String year) {
            JPanel panel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Draw donut chart
                    double totalAmount = incomeCategories.stream().mapToDouble(Map.Entry::getValue).sum();
                    if (totalAmount <= 0) {
                        g2d.drawString("No income data", getWidth() / 2 - 50, getHeight() / 2);
                        return;
                    }

                    int diameter = Math.min(getWidth(), getHeight() - 40) - 20; // Larger chart
                    int radius = diameter / 2;
                    int innerRadius = radius / 2;
                    int centerX = getWidth() / 2;
                    int centerY = (getHeight() - 40) / 2 + 20;
                    int startAngle = 0;
                    int i = 0;
                    Color[] colors = {
                        new Color(255, 182, 193), new Color(144, 238, 144), new Color(135, 206, 250),
                        new Color(240, 230, 140), new Color(221, 160, 221), new Color(173, 216, 230),
                        new Color(255, 218, 185), new Color(200, 162, 200)
                    };

                    for (Map.Entry<String, Double> entry : incomeCategories) {
                        double percentage = entry.getValue() / totalAmount;
                        int arcAngle = (int) Math.round(percentage * 360);
                        if (arcAngle == 0 && entry.getValue() > 0) arcAngle = 1;
                        g2d.setColor(colors[i % colors.length]);
                        g2d.fillArc(centerX - radius, centerY - radius, diameter, diameter, startAngle, arcAngle);
                        startAngle += arcAngle;
                        i++;
                    }
                    g2d.setColor(Color.WHITE);
                    g2d.fillArc(centerX - innerRadius, centerY - innerRadius, innerRadius * 2, innerRadius * 2, 0, 360);

                    // Scrolling percentage in center
                    if (!incomeCategories.isEmpty()) {
                        Map.Entry<String, Double> entry = incomeCategories.get(incomeIndex % incomeCategories.size());
                        double percentage = totalAmount > 0 ? (entry.getValue() / totalAmount) * 100 : 0;
                        g2d.setColor(Color.BLACK);
                        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                        g2d.drawString(String.format("%s: %.1f%%", entry.getKey(), percentage), centerX - 50, centerY);
                    }
                }
            };
            panel.setOpaque(false);
            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int x = e.getX();
                    int y = e.getY();
                    int centerX = panel.getWidth() / 2;
                    int centerY = (panel.getHeight() - 40) / 2 + 20;
                    int radius = Math.min(panel.getWidth(), panel.getHeight() - 40) / 2 - 10;
                    int innerRadius = radius / 2;
                    if (Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2)) <= radius &&
                        Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2)) >= innerRadius) {
                        double angle = Math.toDegrees(Math.atan2(centerY - y, x - centerX));
                        if (angle < 0) angle += 360;
                        double total = incomeCategories.stream().mapToDouble(Map.Entry::getValue).sum();
                        double currentAngle = 0;
                        for (Map.Entry<String, Double> entry : incomeCategories) {
                            double arcAngle = total > 0 ? (entry.getValue() / total) * 360 : 0;
                            if (angle >= currentAngle && angle < currentAngle + arcAngle) {
                                double percentage = total > 0 ? (entry.getValue() / total) * 100 : 0;
                                if (selectedCategoryLabel != null) {
                                    selectedCategoryLabel.setText(String.format("Income: %s, ¥%.2f, %.1f%%",
                                        entry.getKey(), entry.getValue(), percentage));
                                }
                                break;
                            }
                            currentAngle += arcAngle;
                        }
                    }
                }
            });
            incomeTimer = new Timer(2000, e -> {
                incomeIndex++;
                panel.repaint();
            });
            incomeTimer.start();
            return panel;
        }

        public JPanel createExpenseChart(String userId, String year) {
            JPanel panel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Draw donut chart
                    double totalAmount = expenseCategories.stream().mapToDouble(Map.Entry::getValue).sum();
                    if (totalAmount <= 0) {
                        g2d.drawString("No expense data", getWidth() / 2 - 50, getHeight() / 2);
                        return;
                    }

                    int diameter = Math.min(getWidth(), getHeight() - 40) - 20; // Larger chart
                    int radius = diameter / 2;
                    int innerRadius = radius / 2;
                    int centerX = getWidth() / 2;
                    int centerY = (getHeight() - 40) / 2 + 20;
                    int startAngle = 0;
                    int i = 0;
                    Color[] colors = {
                        new Color(255, 182, 193), new Color(144, 238, 144), new Color(135, 206, 250),
                        new Color(240, 230, 140), new Color(221, 160, 221), new Color(173, 216, 230),
                        new Color(255, 218, 185), new Color(200, 162, 200)
                    };

                    for (Map.Entry<String, Double> entry : expenseCategories) {
                        double percentage = entry.getValue() / totalAmount;
                        int arcAngle = (int) Math.round(percentage * 360);
                        if (arcAngle == 0 && entry.getValue() > 0) arcAngle = 1;
                        g2d.setColor(colors[i % colors.length]);
                        g2d.fillArc(centerX - radius, centerY - radius, diameter, diameter, startAngle, arcAngle);
                        startAngle += arcAngle;
                        i++;
                    }
                    g2d.setColor(Color.WHITE);
                    g2d.fillArc(centerX - innerRadius, centerY - innerRadius, innerRadius * 2, innerRadius * 2, 0, 360);

                    // Scrolling percentage in center
                    if (!expenseCategories.isEmpty()) {
                        Map.Entry<String, Double> entry = expenseCategories.get(expenseIndex % expenseCategories.size());
                        double percentage = totalAmount > 0 ? (entry.getValue() / totalAmount) * 100 : 0;
                        g2d.setColor(Color.BLACK);
                        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                        g2d.drawString(String.format("%s: %.1f%%", entry.getKey(), percentage), centerX - 50, centerY);
                    }
                }
            };
            panel.setOpaque(false);
            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int x = e.getX();
                    int y = e.getY();
                    int centerX = panel.getWidth() / 2;
                    int centerY = (panel.getHeight() - 40) / 2 + 20;
                    int radius = Math.min(panel.getWidth(), panel.getHeight() - 40) / 2 - 10;
                    int innerRadius = radius / 2;
                    if (Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2)) <= radius &&
                        Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2)) >= innerRadius) {
                        double angle = Math.toDegrees(Math.atan2(centerY - y, x - centerX));
                        if (angle < 0) angle += 360;
                        double total = expenseCategories.stream().mapToDouble(Map.Entry::getValue).sum();
                        double currentAngle = 0;
                        for (Map.Entry<String, Double> entry : expenseCategories) {
                            double arcAngle = total > 0 ? (entry.getValue() / total) * 360 : 0;
                            if (angle >= currentAngle && angle < currentAngle + arcAngle) {
                                double percentage = total > 0 ? (entry.getValue() / total) * 100 : 0;
                                if (selectedCategoryLabel != null) {
                                    selectedCategoryLabel.setText(String.format("Expense: %s, ¥%.2f, %.1f%%",
                                        entry.getKey(), entry.getValue(), percentage));
                                }
                                break;
                            }
                            currentAngle += arcAngle;
                        }
                    }
                }
            });
            expenseTimer = new Timer(2000, e -> {
                expenseIndex++;
                panel.repaint();
            });
            expenseTimer.start();
            return panel;
        }

        public void updateData(List<Transaction> transactions, int selectedYear) {
            incomeCategories.clear();
            expenseCategories.clear();
            Map<String, Double> incomeMap = new HashMap<>();
            Map<String, Double> expenseMap = new HashMap<>();
            for (Transaction tx : transactions) {
                if (tx.time.startsWith(String.valueOf(selectedYear))) {
                    if ("Income".equals(tx.operation)) {
                        incomeMap.put(tx.category, incomeMap.getOrDefault(tx.category, 0.0) + tx.amount);
                    } else if ("Expense".equals(tx.operation)) {
                        expenseMap.put(tx.category, expenseMap.getOrDefault(tx.category, 0.0) + tx.amount);
                    }
                }
            }
            incomeCategories.addAll(incomeMap.entrySet());
            expenseCategories.addAll(expenseMap.entrySet());
        }

        public void setSelectedCategoryLabel(JLabel label) {
            this.selectedCategoryLabel = label;
        }
    }

    class Transaction {
        String operation;
        double amount;
        String time;
        String category;
        String paymentMethod;
        String location;
        String isRecurring;

        Transaction(String operation, double amount, String time, String category, String paymentMethod, String location, String isRecurring) {
            this.operation = operation;
            this.amount = amount;
            this.time = time;
            this.category = category;
            this.paymentMethod = paymentMethod;
            this.location = location;
            this.isRecurring = isRecurring;
        }
    }
}