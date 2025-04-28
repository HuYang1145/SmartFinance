package View.Transaction;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import Controller.TransactionController;
import Model.Transaction;
import View.LoginAndMain.LoginRoundedInputField;

public class TransactionSystemPlane extends JPanel {
    private final String username;
    private final String[] currencies = {"USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "HKD", "SGD", "NZD", "CNY"};
    private JTable rateTable;
    private JLabel conversionResultLabel;
    private JComboBox<String> currencyComboBox;
    private JTable transactionTable;
    private JLabel countdownLabel;
    private LoginRoundedInputField.RoundedTextField amountField;
    private LoginRoundedInputField.RoundedTextField transactionAmountField, transactionTimeField, merchantField, categoryField, paymentMethodField, tagField, attachmentField, recurrenceField, remarkField;
    private JComboBox<String> operationComboBox, typeComboBox, locationComboBox;
    private JPasswordField passwordField;
    private ActionListener transactionActionListener;
    private ActionListener conversionActionListener;
    private ActionListener historicalTrendActionListener;

    public TransactionSystemPlane(String username) {
        this.username = username;
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new GridLayout(2, 3, 15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 245, 245));

        // First row
        add(createExchangeRatePanel());
        add(createCurrencyConversionPanel());
        add(createHistoricalTrendPanel());

        // Second row
        add(createTransactionInputPanel());
        add(createTransactionHistoryPanel());
        add(createPlaceholderPanel());
    }

    // Module 1: Real-Time Exchange Rate Monitoring
    private JPanel createExchangeRatePanel() {
        JPanel panel = new TransactionSystemComponents.DarkGradientPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Real-Time Exchange Rates", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.NORTH);

        int nonBaseCount = 0;
        for (String currency : currencies) {
            if (!currency.equals("CNY")) {
                nonBaseCount++;
            }
        }

        String[] columnNames = {"Currency Pair", "Exchange Rate"};
        Object[][] data = new Object[nonBaseCount][2];
        int j = 0;
        for (int i = 0; i < currencies.length && j < nonBaseCount; i++) {
            if (!currencies[i].equals("CNY")) {
                data[j][0] = "CNY/" + currencies[i];
                data[j][1] = "Loading...";
                j++;
            }
        }

        rateTable = new JTable(data, columnNames) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        rateTable.setOpaque(false);
        rateTable.setBackground(new Color(0, 0, 0, 0));
        rateTable.getTableHeader().setOpaque(false);
        rateTable.getTableHeader().setBackground(new Color(0, 0, 0, 0));
        rateTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        rateTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        rateTable.setRowHeight(35);
        rateTable.setShowGrid(false);
        rateTable.setIntercellSpacing(new Dimension(0, 0));
        rateTable.setBackground(Color.WHITE);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setForeground(Color.BLACK);
        rateTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        rateTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(rateTable);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        customizeScrollBar(scrollPane);
        JScrollBar vbar = scrollPane.getVerticalScrollBar();
        vbar.setOpaque(false);
        vbar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(255, 255, 255, 120);
                this.trackColor = new Color(0, 0, 0, 0);
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
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(0, 0));
                btn.setMinimumSize(new Dimension(0, 0));
                btn.setMaximumSize(new Dimension(0, 0));
                return btn;
            }
        });
        panel.add(scrollPane, BorderLayout.CENTER);

        countdownLabel = new JLabel("Next refresh in 60 seconds", SwingConstants.CENTER);
        countdownLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        countdownLabel.setForeground(Color.WHITE);
        panel.add(countdownLabel, BorderLayout.SOUTH);

        return panel;
    }

    // Module 2: Currency Conversion
    private JPanel createCurrencyConversionPanel() {
        JPanel panel = new TransactionSystemComponents.MidGradientPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Currency Conversion", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel amountLabel = new JLabel("Amount (CNY):");
        amountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        amountLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(amountLabel, gbc);

        amountField = new LoginRoundedInputField.RoundedTextField("Enter amount");
        amountField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        amountField.setPreferredSize(new Dimension(200, 40));
        gbc.gridx = 1;
        formPanel.add(amountField, gbc);

        JLabel currencyLabel = new JLabel("Target Currency:");
        currencyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        currencyLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(currencyLabel, gbc);

        currencyComboBox = new LoginRoundedInputField.RoundedComboBox<>(currencies);
        currencyComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        currencyComboBox.setPreferredSize(new Dimension(200, 40));
        gbc.gridx = 1;
        formPanel.add(currencyComboBox, gbc);

        conversionResultLabel = new JLabel("Result: -", SwingConstants.CENTER);
        conversionResultLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        conversionResultLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        formPanel.add(conversionResultLabel, gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        return panel;
    }

    // Module 3: Selected Currency Historical Exchange Rate Trend
    private JPanel createHistoricalTrendPanel() {
        JPanel panel = new TransactionSystemComponents.MidGradientPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Historical Exchange Rate Trend", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.NORTH);

        TimeSeries series = new TimeSeries("Rate");
        TimeSeriesCollection dataset = new TimeSeriesCollection(series);
        JFreeChart chart = org.jfree.chart.ChartFactory.createTimeSeriesChart(
                null, "Month", "Rate",
                dataset, false, true, false
        );
        DateAxis dateAxis = (DateAxis) chart.getXYPlot().getDomainAxis();
        dateAxis.setDateFormatOverride(new java.text.SimpleDateFormat("yyyy-MM"));
        dateAxis.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        chart.getXYPlot().getRangeAxis().setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        chart.getXYPlot().getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        chart.getXYPlot().setBackgroundPaint(Color.WHITE);
        chart.getXYPlot().setDomainGridlinePaint(new Color(200, 200, 200));
        chart.getXYPlot().setRangeGridlinePaint(new Color(200, 200, 200));
        chart.getXYPlot().getRenderer().setSeriesPaint(0, new Color(255, 165, 0));
        chart.getXYPlot().getRenderer().setSeriesStroke(0, new java.awt.BasicStroke(2.0f));
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);
        chartPanel.setPreferredSize(new Dimension(300, 150));
        panel.add(chartPanel, BorderLayout.CENTER);

        return panel;
    }
    private void customizeScrollBar(JScrollPane scrollPane) {
        JScrollBar vbar = scrollPane.getVerticalScrollBar();
        vbar.setOpaque(false);
        vbar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(255, 255, 255, 120); // 白色半透明滑块
                this.trackColor = new Color(0, 0, 0, 0); // 透明轨道
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
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(0, 0));
                btn.setMinimumSize(new Dimension(0, 0));
                btn.setMaximumSize(new Dimension(0, 0));
                return btn;
            }
        });
    }
    // Module 4: Transaction Input (Income/Expense)
    private JPanel createTransactionInputPanel() {
        JPanel panel = new TransactionSystemComponents.LightGradientPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LoginRoundedInputField.ShadowBorder(10, Color.LIGHT_GRAY, 20),
                BorderFactory.createEmptyBorder(20, 10, 10, 10)));

        JLabel titleLabel = new JLabel("Add Transaction", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Operation
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Operation:"), gbc);
        operationComboBox = new LoginRoundedInputField.RoundedComboBox<>(new String[]{"Income", "Expense"});
        operationComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        operationComboBox.setPreferredSize(new Dimension(200, 40));
        gbc.gridx = 1;
        formPanel.add(operationComboBox, gbc);

        // Amount
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Amount (¥):"), gbc);
        transactionAmountField = new LoginRoundedInputField.RoundedTextField("Enter amount (e.g., 100.50)");
        transactionAmountField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        formPanel.add(transactionAmountField, gbc);

        // Time
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Time (yyyy/MM/dd HH:mm):"), gbc);
        transactionTimeField = new LoginRoundedInputField.RoundedTextField(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date()));
        transactionTimeField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        formPanel.add(transactionTimeField, gbc);

        // Merchant
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Merchant (English):"), gbc);
        merchantField = new LoginRoundedInputField.RoundedTextField("Enter merchant (e.g., Amazon)");
        merchantField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        formPanel.add(merchantField, gbc);

        // Type
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Type:"), gbc);
        String[] types = {"food", "salary", "rent", "freelance", "investment", "shopping", "Electronics", "Fitness", "Transport", "Entertainment", "Travel", "Gift"};
        typeComboBox = new LoginRoundedInputField.RoundedComboBox<>(types);
        typeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        typeComboBox.setPreferredSize(new Dimension(200, 40));
        gbc.gridx = 1;
        formPanel.add(typeComboBox, gbc);

        // Remark
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Remark (Optional):"), gbc);
        remarkField = new LoginRoundedInputField.RoundedTextField("Enter remark (e.g., Grocery)");
        remarkField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        formPanel.add(remarkField, gbc);

        // Category
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Category (Purpose):"), gbc);
        categoryField = new LoginRoundedInputField.RoundedTextField("Enter category (e.g., Household)");
        categoryField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        formPanel.add(categoryField, gbc);

        // Payment Method
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Payment Method:"), gbc);
        paymentMethodField = new LoginRoundedInputField.RoundedTextField("Enter payment method (e.g., Credit Card)");
        paymentMethodField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        formPanel.add(paymentMethodField, gbc);

        // Location
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Location:"), gbc);
        locationComboBox = new LoginRoundedInputField.RoundedComboBox<>(new String[]{"Online", "Offline"});
        locationComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        locationComboBox.setPreferredSize(new Dimension(200, 40));
        gbc.gridx = 1;
        formPanel.add(locationComboBox, gbc);

        // Tag
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Tag (Optional):"), gbc);
        tagField = new LoginRoundedInputField.RoundedTextField("Enter tag (e.g., Urgent)");
        tagField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        formPanel.add(tagField, gbc);

        // Attachment
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Attachment (Optional):"), gbc);
        attachmentField = new LoginRoundedInputField.RoundedTextField("Enter attachment path (e.g., receipt.pdf)");
        attachmentField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        formPanel.add(attachmentField, gbc);

        // Recurrence
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Recurrence (Optional):"), gbc);
        recurrenceField = new LoginRoundedInputField.RoundedTextField("Enter recurrence (e.g., Monthly)");
        recurrenceField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        formPanel.add(recurrenceField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Password:"), gbc);
        passwordField = new LoginRoundedInputField.RoundedPasswordField("Enter password");
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        // Add formPanel to a JScrollPane
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JScrollBar vbar = scrollPane.getVerticalScrollBar();
        vbar.setOpaque(false);
        vbar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(255, 255, 255, 120);
                this.trackColor = new Color(0, 0, 0, 0);
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
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(0, 0));
                btn.setMinimumSize(new Dimension(0, 0));
                btn.setMaximumSize(new Dimension(0, 0));
                return btn;
            }
        });

        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setOpaque(false);
        JButton confirmButton = new LoginRoundedInputField.GradientButton("Confirm Transaction");
        confirmButton.addActionListener(e -> {
            if (transactionActionListener != null) {
                transactionActionListener.actionPerformed(e);
            }
        });

        JButton cancelButton = new LoginRoundedInputField.GradientButton("Cancel");
        cancelButton.addActionListener(e -> {
            transactionAmountField.setText("");
            transactionTimeField.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date()));
            merchantField.setText("");
            typeComboBox.setSelectedIndex(0);
            remarkField.setText("");
            categoryField.setText("");
            paymentMethodField.setText("");
            locationComboBox.setSelectedIndex(0);
            tagField.setText("");
            attachmentField.setText("");
            recurrenceField.setText("");
            passwordField.setText("");
        });

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Module 5: Transaction History (Detailed)
    private JPanel createTransactionHistoryPanel() {
        JPanel panel = new TransactionSystemComponents.DarkGradientPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    
        JLabel title = new JLabel("Transaction History", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.NORTH);
    
        String[] columnNames = {"Operation", "Amount (¥)", "Time", "Merchant", "Type", "Remark"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        transactionTable = new JTable(tableModel);
        transactionTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        transactionTable.setRowHeight(30);
        transactionTable.setShowGrid(false);
        transactionTable.setOpaque(false);
        transactionTable.setBackground(new Color(0, 0, 0, 0)); // 完全透明背景
        transactionTable.setForeground(Color.BLACK);
    
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(new Color(255, 255, 255, 200)); // 半透明白色背景
                c.setForeground(Color.BLACK);
                ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        };
        for (int i = 0; i < transactionTable.getColumnCount(); i++) {
            transactionTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    
        JTableHeader header = transactionTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setOpaque(false);
        header.setBackground(new Color(0, 0, 0, 0)); // 透明表头背景
        header.setForeground(Color.WHITE);
        header.setBorder(null); // 移除表头边框
    
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(null); // 移除所有边框
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBackground(new Color(0, 0, 0, 0)); // 透明背景
        scrollPane.getViewport().setBorder(null); // 移除视口边框
        customizeScrollBar(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
    
        // Load transaction data
        List<Transaction> transactions = TransactionController.readTransactions(username);
        for (Transaction tx : transactions) {
            tableModel.addRow(new Object[]{
                    tx.getOperation(),
                    tx.getAmount(),
                    tx.getTimestamp(),
                    tx.getMerchant(),
                    tx.getType(),
                    tx.getRemark()
            });
        }
    
        return panel;
    }

    // Module 6: Placeholder Panel
    private JPanel createPlaceholderPanel() {
        JPanel panel = new TransactionSystemComponents.LightGradientPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Placeholder Panel", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.CENTER);

        return panel;
    }

    // Getters for UI components
    public JTable getRateTable() {
        return rateTable;
    }

    public JLabel getConversionResultLabel() {
        return conversionResultLabel;
    }

    public JComboBox<String> getCurrencyComboBox() {
        return currencyComboBox;
    }

    public JTable getTransactionTable() {
        return transactionTable;
    }

    public JLabel getCountdownLabel() {
        return countdownLabel;
    }

    public LoginRoundedInputField.RoundedTextField getAmountField() {
        return amountField;
    }

    public JComboBox<String> getOperationComboBox() {
        return operationComboBox;
    }

    public LoginRoundedInputField.RoundedTextField getTransactionAmountField() {
        return transactionAmountField;
    }

    public LoginRoundedInputField.RoundedTextField getTransactionTimeField() {
        return transactionTimeField;
    }

    public LoginRoundedInputField.RoundedTextField getMerchantField() {
        return merchantField;
    }

    public JComboBox<String> getTypeComboBox() {
        return typeComboBox;
    }

    public LoginRoundedInputField.RoundedTextField getRemarkField() {
        return remarkField;
    }

    public LoginRoundedInputField.RoundedTextField getCategoryField() {
        return categoryField;
    }

    public LoginRoundedInputField.RoundedTextField getPaymentMethodField() {
        return paymentMethodField;
    }

    public JComboBox<String> getLocationComboBox() {
        return locationComboBox;
    }

    public LoginRoundedInputField.RoundedTextField getTagField() {
        return tagField;
    }

    public LoginRoundedInputField.RoundedTextField getAttachmentField() {
        return attachmentField;
    }

    public LoginRoundedInputField.RoundedTextField getRecurrenceField() {
        return recurrenceField;
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }

    // Setters for action listeners
    public void setTransactionActionListener(ActionListener listener) {
        this.transactionActionListener = listener;
    }

    public void setConversionActionListener(ActionListener listener) {
        this.conversionActionListener = listener;
    }

    public void setHistoricalTrendActionListener(ActionListener listener) {
        this.historicalTrendActionListener = listener;
    }

    // Methods to update UI components (called by controller)
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public void updateTransactionList(List<Transaction> transactions) {
        // 已移除此方法，交易历史直接在 createTransactionHistoryPanel 中加载
    }
}