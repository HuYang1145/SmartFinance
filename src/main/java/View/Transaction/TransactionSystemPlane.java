
package View.Transaction;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import Model.User;
import Model.UserSession;
import View.LoginAndMain.GradientComponents;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import Controller.TransactionController;
import Model.Transaction;
import View.LoginAndMain.LoginRoundedInputField;
import utils.Refreshable;

/**
 * A JPanel that serves as the main interface for the transaction system, providing modules for
 * real-time exchange rate monitoring, currency conversion, historical exchange rate trends,
 * transaction input, and transaction history display.
 * 
 * @author Group 19
 * @version 1.0
 */
public class TransactionSystemPlane extends JPanel implements Refreshable {
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
    private JLabel lbl;
    private DefaultListModel<Transaction> model;
    private JList<Transaction> list;
    /**
     * Constructs a TransactionSystemPlane for the specified user.
     * 
     * @param username the username of the current user
     */
    public TransactionSystemPlane(String username) {
        this.username = username;
        initializePanel();
    }

    /**
     * Initializes the panel with a BorderLayout, setting up the left and right panels
     * using a JSplitPane for layout.
     */
    private void initializePanel() {
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 245, 245));

        JSplitPane left = createLeftPanel();
        JPanel right = createRightPanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.67);
        split.setDividerSize(4);
        split.setContinuousLayout(true);
        split.setBorder(null);
        split.setOpaque(false);

        add(split, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> split.setDividerLocation(0.67));
    }

    /**
     * Creates the left panel, containing the transaction history and input panels,
     * arranged vertically using a JSplitPane.
     * 
     * @return the left panel as a JSplitPane
     */
    private JSplitPane createLeftPanel() {
        JPanel history = createTransactionHistoryPanel();
        JPanel input = createTransactionInputPanel();

        JSplitPane vsplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, history, input);
        vsplit.setResizeWeight(0.8);
        vsplit.setDividerSize(4);
        vsplit.setBorder(null);
        return vsplit;
    }

    /**
     * Creates the right panel, containing the exchange rate, currency conversion,
     * and historical trend panels, arranged vertically in a GridLayout.
     * 
     * @return the right panel as a JPanel
     */
    private JPanel createRightPanel() {
        JPanel p = new JPanel(new GridLayout(3, 1, 0, 0));
        p.setOpaque(false);
        p.add(createExchangeRatePanel());
        p.add(createCurrencyConversionPanel());
        p.add(createHistoricalTrendPanel());
        return p;
    }

    /**
     * Creates the panel for real-time exchange rate monitoring, displaying a table
     * of currency pairs and their rates, along with a countdown for the next refresh.
     * 
     * @return the exchange rate panel
     */
    private JPanel createExchangeRatePanel() {
        JPanel panel = new TransactionSystemComponents.BlueGradientPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Real-Time Exchange Rates", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(0x2C3C49));
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
        centerRenderer.setForeground(new Color(0x84ACC9));
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
        countdownLabel.setForeground(new Color(0x2C3C49));
        panel.add(countdownLabel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates the panel for currency conversion, allowing users to input an amount
     * in CNY and select a target currency for conversion.
     * 
     * @return the currency conversion panel
     */
    private JPanel createCurrencyConversionPanel() {
        JPanel panel = new TransactionSystemComponents.BlueGradientPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Currency Conversion", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(0x2C3C49));
        panel.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel amountLabel = new JLabel("Amount (CNY):");
        amountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        amountLabel.setForeground(new Color(0x2C3C49));
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
        currencyLabel.setForeground(new Color(0x2C3C49));
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
        conversionResultLabel.setForeground(new Color(0x2C3C49));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        formPanel.add(conversionResultLabel, gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates the panel for displaying the historical exchange rate trend of a selected currency
     * using a time series chart.
     * 
     * @return the historical trend panel
     */
    private JPanel createHistoricalTrendPanel() {
        JPanel panel = new TransactionSystemComponents.BlueGradientPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Historical Exchange Rate Trend", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.BLACK);
        panel.add(title, BorderLayout.NORTH);

        TimeSeries series = new TimeSeries("Rate");

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            Calendar cal = Calendar.getInstance();
            double baseRate = 6.90;

            for (int i = 5; i >= 0; i--) {
                Calendar temp = (Calendar) cal.clone();
                temp.add(Calendar.MONTH, -i);
                Date date = temp.getTime();
                double rate = baseRate + Math.random() * 0.2 - 0.1;
                series.add(new org.jfree.data.time.Month(date), rate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    /**
     * Customizes the scrollbar of a JScrollPane to have a transparent track and a semi-transparent thumb.
     * 
     * @param scrollPane the JScrollPane to customize
     */
    private void customizeScrollBar(JScrollPane scrollPane) {
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
    }

    /**
     * Creates the panel for inputting new transactions, including fields for transaction details
     * and buttons for confirmation or cancellation.
     * 
     * @return the transaction input panel
     */
    private JPanel createTransactionInputPanel() {
        JPanel panel = new TransactionSystemComponents.LightGradientPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LoginRoundedInputField.ShadowBorder(10, Color.LIGHT_GRAY, 20),
                BorderFactory.createEmptyBorder(20, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel("Add Transaction", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0x2C3C49));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(20, 0));
        content.setOpaque(false);

        JScrollPane formScroll = new JScrollPane(buildFormPanel(false));
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        formScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        customizeScrollBar(formScroll);
        content.add(formScroll, BorderLayout.CENTER);

        JPanel btnBox = new JPanel();
        btnBox.setOpaque(false);
        btnBox.setLayout(new BoxLayout(btnBox, BoxLayout.Y_AXIS));

        passwordField = new LoginRoundedInputField.RoundedPasswordField("Enter password");
        passwordField.setMaximumSize(new Dimension(200, 40));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton confirm = new GradientComponents.GradientButton("Confirm Transaction");
        confirm.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton cancel = new GradientComponents.GradientButton("Cancel");
        cancel.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnBox.add(Box.createVerticalGlue());
        btnBox.add(passwordField);
        btnBox.add(Box.createVerticalStrut(20));
        btnBox.add(confirm);
        btnBox.add(Box.createVerticalStrut(20));
        btnBox.add(cancel);
        btnBox.add(Box.createVerticalGlue());

        content.add(btnBox, BorderLayout.EAST);

        panel.add(content, BorderLayout.CENTER);

        confirm.addActionListener(e -> {
            if (transactionActionListener != null) {
                transactionActionListener.actionPerformed(e);
            }
        });

        cancel.addActionListener(e -> {
            transactionAmountField.setText("");
            transactionTimeField.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date()));
            merchantField.setText("Enter merchant (e.g., Amazon)");
            typeComboBox.setSelectedIndex(0);
            remarkField.setText("Enter remark (e.g., Grocery)");
            categoryField.setText("Enter category (e.g., Household)");
            paymentMethodField.setText("Enter payment method (e.g., Credit Card)");
            locationComboBox.setSelectedIndex(0);
            tagField.setText("Enter tag (e.g., Urgent)");
            attachmentField.setText("Enter attachment path (e.g., receipt.pdf)");
            recurrenceField.setText("Enter recurrence (e.g., Monthly)");
            passwordField.setText("Enter password");
        });

        return panel;
    }

    /**
     * Builds the form panel for transaction input, containing fields for transaction details.
     * 
     * @param includePassword whether to include a password field (not used in this implementation)
     * @return the form panel
     */
    private JPanel buildFormPanel(boolean includePassword) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.weightx = 1.0;

        int y = 0;

        gbc.gridx = 0; gbc.gridy = y;
        form.add(createWhiteLabel("Operation:"), gbc);
        gbc.gridx = 1;
        form.add(operationComboBox = new LoginRoundedInputField.RoundedComboBox<>(
                new String[]{"Income", "Expense"}), gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y;
        form.add(createWhiteLabel("Amount (¥):"), gbc);
        gbc.gridx = 1;
        form.add(transactionAmountField = new LoginRoundedInputField.RoundedTextField(
                "Enter amount (e.g., 100.50)"), gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y;
        form.add(createWhiteLabel("Time (yyyy/MM/dd HH:mm):"), gbc);
        gbc.gridx = 1;
        form.add(transactionTimeField = new LoginRoundedInputField.RoundedTextField(
                new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date())), gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y;
        form.add(createWhiteLabel("Merchant (English):"), gbc);
        gbc.gridx = 1;
        form.add(merchantField = new LoginRoundedInputField.RoundedTextField(
                "Enter merchant (e.g., Amazon)"), gbc);
        y++;


        gbc.gridx = 0; gbc.gridy = y;
        form.add(createWhiteLabel("Remark (Optional):"), gbc);
        gbc.gridx = 1;
        form.add(remarkField = new LoginRoundedInputField.RoundedTextField(
                "Enter remark (e.g., Grocery)"), gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y;
        form.add(createWhiteLabel("Category :"), gbc);
        gbc.gridx = 1;
        form.add(categoryField = new LoginRoundedInputField.RoundedTextField(
                "Enter category (e.g., Household)"), gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y;
        form.add(createWhiteLabel("Payment Method:"), gbc);
        gbc.gridx = 1;
        form.add(paymentMethodField = new LoginRoundedInputField.RoundedTextField(
                "Enter payment method (e.g., Credit Card)"), gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y;
        form.add(createWhiteLabel("Location:"), gbc);
        gbc.gridx = 1;
        form.add(locationComboBox = new LoginRoundedInputField.RoundedComboBox<>(
                new String[]{"Online", "Offline"}), gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y;
        form.add(createWhiteLabel("Tag (Optional):"), gbc);
        gbc.gridx = 1;
        form.add(tagField = new LoginRoundedInputField.RoundedTextField(
                "Enter tag (e.g., Urgent)"), gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y;
        form.add(createWhiteLabel("Attachment (Optional):"), gbc);
        gbc.gridx = 1;
        form.add(attachmentField = new LoginRoundedInputField.RoundedTextField(
                "Enter attachment path (e.g., receipt.pdf)"), gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y;
        form.add(createWhiteLabel("Recurrence (Optional):"), gbc);
        gbc.gridx = 1;
        form.add(recurrenceField = new LoginRoundedInputField.RoundedTextField(
                "Enter recurrence (e.g., Monthly)"), gbc);

        return form;
    }

    /**
     * Creates a JLabel with the specified text, styled with a bold font and dark color.
     * 
     * @param text the text for the label
     * @return the styled JLabel
     */
    private JLabel createWhiteLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(0x2C3C49));
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return label;
    }

    /**
     * Creates the panel for displaying the transaction history, showing the user's balance
     * and a list of transactions for the current month.
     * 
     * @return the transaction history panel
     */
    private JPanel createTransactionHistoryPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 10));
        container.setBackground(Color.WHITE);

        TransactionSystemComponents.DarkGradientPanel header = new TransactionSystemComponents.DarkGradientPanel();
        header.setPreferredSize(new Dimension(0, 120));
        header.setLayout(null);
        User acct = UserSession.getCurrentAccount();
        lbl = new JLabel(
                String.format("Your Balance: %.2f CNY", acct.getBalance()),
                SwingConstants.LEFT
        );
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lbl.setForeground(new Color(0x2C3C49));
        lbl.setBounds(20, 20, 400, 40);
        header.add(lbl);
        container.add(header, BorderLayout.NORTH);

        Calendar cal = Calendar.getInstance();
        String ym = String.format("%d/%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1
        );
        List<Transaction> all = TransactionController.readTransactions(username);
        model = new DefaultListModel<>();
        for (Transaction t : all) {
            if (t.getTimestamp().startsWith(ym)) {
                model.addElement(t);
            }
        }

        list = new JList<>(model);
        list.setCellRenderer(new ListCellRenderer<Transaction>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends Transaction> list,
                                                          Transaction t, int idx,
                                                          boolean isSel, boolean cellHasFocus) {
                JPanel row = new JPanel(new BorderLayout());
                row.setBackground(Color.WHITE);
                row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230,230,230)));
                row.setPreferredSize(new Dimension(0, 80));

                JPanel left = new JPanel();
                left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
                left.setOpaque(false);
                JLabel op = new JLabel("  " + t.getOperation());
                op.setFont(new Font("Segoe UI", Font.BOLD, 14));
                JLabel ti = new JLabel(" " + t.getTimestamp());
                ti.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                ti.setForeground(Color.GRAY);
                left.add(op);
                left.add(ti);
                row.add(left, BorderLayout.WEST);

                String opText = t.getOperation().toLowerCase();
                String amountText;
                if (opText.contains("income") || opText.contains("receive")) {
                    amountText = String.format("+%.2f", t.getAmount());
                } else {
                    amountText = String.format("-%.2f", Math.abs(t.getAmount()));
                }
                JLabel amt = new JLabel(amountText, SwingConstants.RIGHT);
                amt.setFont(new Font("Segoe UI", Font.BOLD, 14));

                if (opText.contains("income") || opText.contains("receive")) {
                    amt.setForeground(new Color(0,150,0));
                } else {
                    amt.setForeground(new Color(200,0,0));
                }
                row.add(amt, BorderLayout.EAST);

                if (isSel) {
                    row.setBackground(new Color(230,240,255));
                }
                return row;
            }
        });

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(0xFAF0D2));
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = Color.WHITE;
                trackColor = new Color(245,245,245);
            }
            @Override protected JButton createDecreaseButton(int o){ return zero(); }
            @Override protected JButton createIncreaseButton(int o){ return zero(); }
            private JButton zero(){
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0));
                b.setBorder(null); b.setContentAreaFilled(false); return b;
            }
        });

        container.add(scroll, BorderLayout.CENTER);
        return container;
    }

    /**
     * Creates a placeholder panel for future use.
     * 
     * @return the placeholder panel
     */
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

    /**
     * Gets the JTable displaying exchange rates.
     * 
     * @return the exchange rate table
     */
    public JTable getRateTable() {
        return rateTable;
    }

    /**
     * Gets the JLabel displaying the currency conversion result.
     * 
     * @return the conversion result label
     */
    public JLabel getConversionResultLabel() {
        return conversionResultLabel;
    }

    /**
     * Gets the JComboBox for selecting the target currency.
     * 
     * @return the currency combo box
     */
    public JComboBox<String> getCurrencyComboBox() {
        return currencyComboBox;
    }

    /**
     * Gets the JTable displaying transactions (not used in this implementation).
     * 
     * @return the transaction table
     */
    public JTable getTransactionTable() {
        return transactionTable;
    }

    /**
     * Gets the JLabel displaying the countdown for the next exchange rate refresh.
     * 
     * @return the countdown label
     */
    public JLabel getCountdownLabel() {
        return countdownLabel;
    }

    /**
     * Gets the text field for entering the amount to convert.
     * 
     * @return the amount field
     */
    public LoginRoundedInputField.RoundedTextField getAmountField() {
        return amountField;
    }

    /**
     * Gets the JComboBox for selecting the transaction operation (Income/Expense).
     * 
     * @return the operation combo box
     */
    public JComboBox<String> getOperationComboBox() {
        return operationComboBox;
    }

    /**
     * Gets the text field for entering the transaction amount.
     * 
     * @return the transaction amount field
     */
    public LoginRoundedInputField.RoundedTextField getTransactionAmountField() {
        return transactionAmountField;
    }

    /**
     * Gets the text field for entering the transaction timestamp.
     * 
     * @return the transaction time field
     */
    public LoginRoundedInputField.RoundedTextField getTransactionTimeField() {
        return transactionTimeField;
    }

    /**
     * Gets the text field for entering the merchant name.
     * 
     * @return the merchant field
     */
    public LoginRoundedInputField.RoundedTextField getMerchantField() {
        return merchantField;
    }

    /**
     * Gets the JComboBox for selecting the transaction type.
     * 
     * @return the type combo box
     */
    public JComboBox<String> getTypeComboBox() {
        return typeComboBox;
    }

    /**
     * Gets the text field for entering optional remarks.
     * 
     * @return the remark field
     */
    public LoginRoundedInputField.RoundedTextField getRemarkField() {
        return remarkField;
    }

    /**
     * Gets the text field for entering the transaction category.
     * 
     * @return the category field
     */
    public LoginRoundedInputField.RoundedTextField getCategoryField() {
        return categoryField;
    }

    /**
     * Gets the text field for entering the payment method.
     * 
     * @return the payment method field
     */
    public LoginRoundedInputField.RoundedTextField getPaymentMethodField() {
        return paymentMethodField;
    }

    /**
     * Gets the JComboBox for selecting the transaction location (Online/Offline).
     * 
     * @return the location combo box
     */
    public JComboBox<String> getLocationComboBox() {
        return locationComboBox;
    }

    /**
     * Gets the text field for entering optional tags.
     * 
     * @return the tag field
     */
    public LoginRoundedInputField.RoundedTextField getTagField() {
        return tagField;
    }

    /**
     * Gets the text field for entering optional attachment paths.
     * 
     * @return the attachment field
     */
    public LoginRoundedInputField.RoundedTextField getAttachmentField() {
        return attachmentField;
    }

    /**
     * Gets the text field for entering optional recurrence details.
     * 
     * @return the recurrence field
     */
    public LoginRoundedInputField.RoundedTextField getRecurrenceField() {
        return recurrenceField;
    }

    /**
     * Gets the password field for entering the user's password.
     * 
     * @return the password field
     */
    public JPasswordField getPasswordField() {
        return passwordField;
    }

    /**
     * Sets the ActionListener for transaction-related actions.
     * 
     * @param listener the ActionListener to set
     */
    public void setTransactionActionListener(ActionListener listener) {
        this.transactionActionListener = listener;
    }

    /**
     * Sets the ActionListener for currency conversion actions.
     * 
     * @param listener the ActionListener to set
     */
    public void setConversionActionListener(ActionListener listener) {
        this.conversionActionListener = listener;
    }

    /**
     * Sets the ActionListener for historical trend actions.
     * 
     * @param listener the ActionListener to set
     */
    public void setHistoricalTrendActionListener(ActionListener listener) {
        this.historicalTrendActionListener = listener;
    }

    /**
     * Displays an error message in a dialog.
     * 
     * @param message the error message to display
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Displays a success message in a dialog.
     * 
     * @param message the success message to display
     */
    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Updates the transaction list (not used in this implementation).
     * 
     * @param transactions the list of transactions to display
     */
    public void updateTransactionList(List<Transaction> transactions) {
        // Implementation removed; transaction history loaded in createTransactionHistoryPanel
    }
    @Override
    public void refreshData() {
        User acct = UserSession.getCurrentAccount();
        if (lbl != null && acct != null) {
            lbl.setText(String.format("Your Balance: %.2f CNY", acct.getBalance()));
        }
        if (model != null) {
            model.clear();
            Calendar cal = Calendar.getInstance();
            String ym = String.format("%d/%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
            List<Transaction> all = TransactionController.readTransactions(username);
            for (Transaction t : all) {
                if (t.getTimestamp().startsWith(ym)) {
                    model.addElement(t);
                }
            }
        }
    }


}
