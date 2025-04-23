package View;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.json.JSONObject;

import com.google.gson.Gson;

import AccountModel.AccountModel;
import AccountModel.TransactionServiceModel;
import AccountModel.TransactionServiceModel.TransactionData;
import AccountModel.UserSessionModel;
import PersonModel.TransactionAnalyzerModel;
import View.AccountManagementUI.RoundBorder;
import View.RoundedInputField.BrandGradientPanel;
import View.RoundedInputField.GradientButton;

public class TransactionSystem extends JPanel {
    private final String username;
    private final String exchangeApiKey = "ea7c697a26d9704cbef405f1";
    private final String fixerApiKey = "9b67dd5bf75330aee3706632e0961743";
    private final String baseCurrency = "EUR";
    private final String targetBaseCurrency = "CNY";
    private final String[] currencies = {"USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "HKD", "SGD", "NZD", "CNY"};
    private Map<String, Double> exchangeRates = new HashMap<>();
    private JTable rateTable;
    private JLabel conversionResultLabel;
    private JComboBox<String> currencyComboBox;
    private JTable transactionTable;
    private Timer rateUpdateTimer;
    private JLabel countdownLabel;
    private Timer countdownTimer;
    private long nextRefreshTime;
    private Map<String, Map<String, Double>> historicalRatesCache = new HashMap<>();
    private long lastApiCallTimestamp = 0;
    private static final String CACHE_FILE = "history_cache.json";
    private static final long ONE_DAY_MS = 24 * 60 * 60 * 1000;
    // Colors for beautification
    private static final Color DARK_GRADIENT_START = new Color(156, 39, 176);
    private static final Color DARK_GRADIENT_END = new Color(40, 100, 250);
    private static final Color MID_GRADIENT_START = new Color(180, 60, 200);
    private static final Color MID_GRADIENT_END = new Color(60, 120, 250);
    private static final Color LIGHT_GRADIENT_START = new Color(200, 80, 220);
    private static final Color LIGHT_GRADIENT_END = new Color(80, 140, 255); // Fixed Blue from 290 to 255
    private static final Color TEXT_COLOR = Color.BLACK;
    private static final Color HIGHLIGHT_COLOR = Color.WHITE;

    public TransactionSystem(String username) {
        this.username = username;
        loadCache();
        initializePanel();
        fetchExchangeRates();
        startRateUpdateTimer();
    }

    private void initializePanel() {
        setLayout(new GridLayout(2, 3, 15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 245, 245));
    
        // First row
        add(createExchangeRatePanel()); // Module 1
        add(createCurrencyConversionPanel()); // Module 2
        add(createTransactionHistoryPanel()); // Module 6
    
        // Second row
        add(createHistoricalTrendPanel()); // Module 4
        add(createIncomePanel()); // Module 5
        add(createExpensePanel()); // Module 7
    }

   // Module 1: Real-Time Exchange Rate Monitoring
   JPanel createExchangeRatePanel() {
       JPanel panel = new BrandGradientPanel();
       panel.setLayout(new BorderLayout(10,10));
       panel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
    JLabel title = new JLabel("Real-Time Exchange Rates", SwingConstants.CENTER);
    title.setFont(new Font("Segoe UI", Font.BOLD, 20));
    title.setForeground(Color.WHITE);
    panel.add(title, BorderLayout.NORTH);

    int nonBaseCount = 0;
    for (String currency : currencies) {
        if (!currency.equals(targetBaseCurrency)) {
            nonBaseCount++;
        }
    }

    String[] columnNames = {"Currency Pair", "Exchange Rate"};
    Object[][] data = new Object[nonBaseCount][2];
    int j = 0;
    for (int i = 0; i < currencies.length && j < nonBaseCount; i++) {
        if (!currencies[i].equals(targetBaseCurrency)) {
            data[j][0] = targetBaseCurrency + "/" + currencies[i];
            data[j][1] = "Loading...";
            j++;
        }
    }

    rateTable = new JTable(data, columnNames) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
   rateTable.setOpaque(false);
   rateTable.setBackground(new Color(0,0,0,0));

   rateTable.getTableHeader().setOpaque(false);
   rateTable.getTableHeader().setBackground(new Color(0,0,0,0));

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
// 让滚动区域也透明
       scrollPane.setOpaque(false);
       scrollPane.getViewport().setOpaque(false);

       JScrollBar vbar = scrollPane.getVerticalScrollBar();
       vbar.setOpaque(false);
       vbar.setUI(new BasicScrollBarUI() {
           @Override protected void configureScrollBarColors() {
               // 半透明白色拖块
               this.thumbColor = new Color(255,255,255,120);
               // 完全透明轨道
               this.trackColor = new Color(0,0,0,0);
           }
           @Override protected JButton createDecreaseButton(int orientation) {
               return createZeroButton();
           }
           @Override protected JButton createIncreaseButton(int orientation) {
               return createZeroButton();
           }
           private JButton createZeroButton() {
               JButton btn = new JButton();
               btn.setPreferredSize(new Dimension(0,0));
               btn.setMinimumSize(new Dimension(0,0));
               btn.setMaximumSize(new Dimension(0,0));
               return btn;
           }
       });
       panel.add(scrollPane, BorderLayout.CENTER);


       countdownLabel = new JLabel("Next refresh in 60 seconds", SwingConstants.CENTER);
    countdownLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    countdownLabel.setForeground(HIGHLIGHT_COLOR);
    panel.add(countdownLabel, BorderLayout.SOUTH);

    return panel;
}

   // Module 2: Currency Conversion
   JPanel createCurrencyConversionPanel() {
       // ===== 外层容器，带阴影 + 圆角边框 =====
       JPanel panel = new BrandGradientPanel();
       panel.setLayout(new BorderLayout(10,10));
       panel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

       // ===== 顶部标题 =====
       JLabel title = new JLabel("Currency Conversion", SwingConstants.CENTER);
       title.setFont(new Font("Segoe UI", Font.BOLD, 20));
       title.setForeground(Color.WHITE);
       panel.add(title, BorderLayout.NORTH);

       // ===== 中部表单区域（GridBagLayout） =====
       JPanel formPanel = new JPanel(new GridBagLayout());
       formPanel.setOpaque(false); // 保持背景透明以显示外部渐变

       GridBagConstraints gbc = new GridBagConstraints();
       gbc.insets = new Insets(12, 12, 12, 12);
       gbc.fill = GridBagConstraints.HORIZONTAL;

       // -- Amount Label
       JLabel amountLabel = new JLabel("Amount (CNY):");
       amountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
       amountLabel.setForeground(Color.WHITE);
       gbc.gridx = 0; gbc.gridy = 0;
       formPanel.add(amountLabel, gbc);

       // -- Amount Field
       RoundedTextField amountField = new RoundedTextField("Enter amount");
       amountField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
       amountField.setPreferredSize(new Dimension(200, 40));
       gbc.gridx = 1;
       formPanel.add(amountField, gbc);

       // -- Currency Label
       JLabel currencyLabel = new JLabel("Target Currency:");
       currencyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
       currencyLabel.setForeground(Color.WHITE);
       gbc.gridx = 0; gbc.gridy = 1;
       formPanel.add(currencyLabel, gbc);

       // -- Currency ComboBox
       currencyComboBox = new RoundedComboBox<>(currencies);
       currencyComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));
       currencyComboBox.setPreferredSize(new Dimension(200, 40));
       gbc.gridx = 1;
       formPanel.add(currencyComboBox, gbc);

       // -- Conversion Result
       conversionResultLabel = new JLabel("Result: -", SwingConstants.CENTER);
       conversionResultLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
       conversionResultLabel.setForeground(Color.WHITE);
       gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
       formPanel.add(conversionResultLabel, gbc);

       // 加入表单面板到中间区域
       panel.add(formPanel, BorderLayout.CENTER);

       // 监听输入和选择
       amountField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
           public void insertUpdate(javax.swing.event.DocumentEvent e) { updateConversion(); }
           public void removeUpdate(javax.swing.event.DocumentEvent e) { updateConversion(); }
           public void changedUpdate(javax.swing.event.DocumentEvent e) { updateConversion(); }
       });

       currencyComboBox.addActionListener(e -> {
           updateConversion();
           updateHistoricalTrend(); // 如果你需要刷新趋势图
       });

       return panel;
   }

    // Module 4: Selected Currency Historical Exchange Rate Trend
   JPanel createHistoricalTrendPanel() {
       JPanel panel = new BrandGradientPanel();
       panel.setLayout(new BorderLayout(10,10));
       panel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
    JLabel title = new JLabel("Historical Exchange Rate Trend", SwingConstants.CENTER);
    title.setFont(new Font("Segoe UI", Font.BOLD, 20));
    title.setForeground(Color.WHITE);
    panel.add(title, BorderLayout.NORTH);

    TimeSeries series = new TimeSeries("Rate");
    TimeSeriesCollection dataset = new TimeSeriesCollection(series);
    JFreeChart chart = ChartFactory.createTimeSeriesChart(
            null, "Month", "Rate",
            dataset, false, true, false
    );
    DateAxis dateAxis = (DateAxis) chart.getXYPlot().getDomainAxis();
    dateAxis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM"));
    dateAxis.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
    dateAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
    chart.getXYPlot().getRangeAxis().setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
    chart.getXYPlot().getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
    chart.getXYPlot().setBackgroundPaint(Color.WHITE);
    chart.getXYPlot().setDomainGridlinePaint(new Color(200, 200, 200));
    chart.getXYPlot().setRangeGridlinePaint(new Color(200, 200, 200));
    chart.getXYPlot().getRenderer().setSeriesPaint(0, new Color(255, 165, 0));
    chart.getXYPlot().getRenderer().setSeriesStroke(0, new BasicStroke(2.0f));
    ChartPanel chartPanel = new ChartPanel(chart);
    chartPanel.setDomainZoomable(false);
    chartPanel.setRangeZoomable(false);
    chartPanel.setPreferredSize(new Dimension(300, 150));
    panel.add(chartPanel, BorderLayout.CENTER);

    // Initial fetch if cache is empty or outdated
    fetchHistoricalRates();
    updateHistoricalTrend();

    return panel;
}
    // Module 5: Income Interface
private JPanel createIncomePanel() {
    LightGradientPanel panel = new LightGradientPanel();
    panel.setLayout(new BorderLayout(10, 10));
    panel.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(10, HIGHLIGHT_COLOR),
            BorderFactory.createEmptyBorder(20, 10, 10, 10)));

    JLabel titleLabel = new JLabel("Add Income Record", SwingConstants.CENTER);
    titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
    titleLabel.setForeground(Color.WHITE);
    panel.add(titleLabel, BorderLayout.NORTH);

    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setOpaque(false);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(12, 12, 12, 12);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    JLabel amountLabel = new JLabel("Amount (¥):");
    amountLabel.setForeground(Color.WHITE);
    gbc.gridx = 0; gbc.gridy = 0;
    formPanel.add(amountLabel, gbc);

    JTextField amountField = new JTextField(15);
    amountField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    gbc.gridx = 1; gbc.weightx = 1.0;
    formPanel.add(amountField, gbc);

    JLabel timeLabel = new JLabel("Time (yyyy/MM/dd HH:mm):");
    timeLabel.setForeground(Color.WHITE);
    gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
    formPanel.add(timeLabel, gbc);

    JTextField timeField = new JTextField(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date()), 15);
    timeField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    gbc.gridx = 1; gbc.weightx = 1.0;
    formPanel.add(timeField, gbc);

    JLabel passwordLabel = new JLabel("Password:");
    passwordLabel.setForeground(Color.WHITE);
    gbc.gridx = 0; gbc.gridy = 2;
    formPanel.add(passwordLabel, gbc);

    JPasswordField passwordField = new JPasswordField(15);
    passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    gbc.gridx = 1;
    formPanel.add(passwordField, gbc);

    panel.add(formPanel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
    buttonPanel.setOpaque(false);
    JButton confirmButton = new JButton("Confirm Income");
    confirmButton.setBackground(new Color(30, 60, 120));
    confirmButton.setForeground(Color.WHITE);
    confirmButton.setBorder(new RoundBorder(10, confirmButton.getBackground()));
    confirmButton.addMouseListener(new MouseAdapter() {
        @Override public void mouseEntered(MouseEvent e) { confirmButton.setBackground(new Color(50, 80, 140)); }
        @Override public void mouseExited(MouseEvent e) { confirmButton.setBackground(new Color(30, 60, 120)); }
    });
    confirmButton.addActionListener(e -> {
        String amountText = amountField.getText();
        String timeText = timeField.getText();
        String password = new String(passwordField.getPassword());

        if (amountText.isEmpty() || timeText.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(TransactionSystem.this, "All fields must be filled.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(TransactionSystem.this, "Amount must be positive.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(TransactionSystem.this, "Invalid amount format.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            dateFormat.setLenient(false);
            dateFormat.parse(timeText.trim());
        } catch (java.text.ParseException ex) {
            JOptionPane.showMessageDialog(TransactionSystem.this, "Invalid time format. Use yyyy/MM/dd HH:mm", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean transactionAdded = TransactionServiceModel.addTransaction(
                username, "Income", amount, timeText.trim(), "I", "I", "", "u", "", "", "", "", ""
        );

        if (transactionAdded) {
            JOptionPane.showMessageDialog(TransactionSystem.this, "Income of ¥" + String.format("%.2f", amount) + " added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshTransactionTable();
            amountField.setText("");
            timeField.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date()));
            passwordField.setText("");
        } else {
            JOptionPane.showMessageDialog(TransactionSystem.this, "Failed to add income.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    });

    JButton cancelButton = new JButton("Cancel");
    cancelButton.setBackground(new Color(150, 150, 150));
    cancelButton.setForeground(Color.WHITE);
    cancelButton.setBorder(new RoundBorder(10, cancelButton.getBackground()));
    cancelButton.addMouseListener(new MouseAdapter() {
        @Override public void mouseEntered(MouseEvent e) { cancelButton.setBackground(new Color(170, 170, 170)); }
        @Override public void mouseExited(MouseEvent e) { cancelButton.setBackground(new Color(150, 150, 150)); }
    });
    cancelButton.addActionListener(e -> {
        amountField.setText("");
        timeField.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date()));
        passwordField.setText("");
    });

    buttonPanel.add(confirmButton);
    buttonPanel.add(cancelButton);
    panel.add(buttonPanel, BorderLayout.SOUTH);

    return panel;
}

   // Module 7: Expense Interface
private JPanel createExpensePanel() {
    LightGradientPanel panel = new LightGradientPanel();
    panel.setLayout(new BorderLayout(10, 10));
    panel.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(10, HIGHLIGHT_COLOR),
            BorderFactory.createEmptyBorder(20, 10, 10, 10)));

    JLabel titleLabel = new JLabel("Add Expense Record", SwingConstants.CENTER);
    titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
    titleLabel.setForeground(Color.WHITE);
    panel.add(titleLabel, BorderLayout.NORTH);

    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setOpaque(false);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(12, 12, 12, 12);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.WEST;

    JLabel amountLabel = new JLabel("Amount (¥):");
    amountLabel.setForeground(Color.WHITE);
    gbc.gridx = 0; gbc.gridy = 0;
    formPanel.add(amountLabel, gbc);

    JTextField amountField = new JTextField(15);
    amountField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    gbc.gridx = 1; gbc.weightx = 1.0;
    formPanel.add(amountField, gbc);

    JLabel timeLabel = new JLabel("Time (yyyy/MM/dd HH:mm):");
    timeLabel.setForeground(Color.WHITE);
    gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
    formPanel.add(timeLabel, gbc);

    JTextField timeField = new JTextField(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date()), 15);
    timeField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    gbc.gridx = 1; gbc.weightx = 1.0;
    formPanel.add(timeField, gbc);

    JLabel merchantLabel = new JLabel("Merchant/Payee:");
    merchantLabel.setForeground(Color.WHITE);
    gbc.gridx = 0; gbc.gridy = 2;
    formPanel.add(merchantLabel, gbc);

    JTextField merchantField = new JTextField(15);
    merchantField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    gbc.gridx = 1;
    formPanel.add(merchantField, gbc);

    JLabel typeLabel = new JLabel("Type:");
    typeLabel.setForeground(Color.WHITE);
    gbc.gridx = 0; gbc.gridy = 3;
    formPanel.add(typeLabel, gbc);

    String[] expenseTypes = {"(Select Type)", "Food", "Shopping", "Traffic", "Entertainment", "Education", "Transfer", "Others"};
    JComboBox<String> typeComboBox = new JComboBox<>(expenseTypes);
    typeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    gbc.gridx = 1;
    formPanel.add(typeComboBox, gbc);

    JLabel passwordLabel = new JLabel("Password:");
    passwordLabel.setForeground(TEXT_COLOR);
    gbc.gridx = 0; gbc.gridy = 4;
    formPanel.add(passwordLabel, gbc);

    JPasswordField passwordField = new JPasswordField(15);
    passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    gbc.gridx = 1;
    formPanel.add(passwordField, gbc);

    panel.add(formPanel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
    buttonPanel.setOpaque(false);
    JButton confirmButton = new JButton("Confirm Expense");
    confirmButton.setBackground(new Color(220, 53, 69));
    confirmButton.setForeground(Color.WHITE);
    confirmButton.setBorder(new RoundBorder(10, confirmButton.getBackground()));
    confirmButton.addMouseListener(new MouseAdapter() {
        @Override public void mouseEntered(MouseEvent e) { confirmButton.setBackground(new Color(240, 73, 89)); }
        @Override public void mouseExited(MouseEvent e) { confirmButton.setBackground(new Color(220, 53, 69)); }
    });
    confirmButton.addActionListener(e -> {
        String amountText = amountField.getText();
        String timeText = timeField.getText();
        String merchantText = merchantField.getText();
        String selectedType = (String) typeComboBox.getSelectedItem();
        String password = new String(passwordField.getPassword());

        if (amountText.isEmpty() || timeText.isEmpty() || merchantText.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(TransactionSystem.this, "Amount, Time, Merchant, and Password must be filled.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ("I".equalsIgnoreCase(merchantText.trim())) {
            JOptionPane.showMessageDialog(TransactionSystem.this, "Invalid merchant name.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(TransactionSystem.this, "Amount must be positive.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(TransactionSystem.this, "Invalid amount format.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            dateFormat.setLenient(false);
            dateFormat.parse(timeText.trim());
        } catch (java.text.ParseException ex) {
            JOptionPane.showMessageDialog(TransactionSystem.this, "Invalid time format. Use yyyy/MM/dd HH:mm", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String typeToRecord = "u";
        if (selectedType != null && !selectedType.equals("(Select Type)")) {
            typeToRecord = selectedType;
        }
        if ("I".equalsIgnoreCase(typeToRecord)) {
            JOptionPane.showMessageDialog(TransactionSystem.this, "Invalid expense type selected.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean transactionAdded = TransactionServiceModel.addTransaction(
                username, "Expense", amount, timeText.trim(), merchantText.trim(), typeToRecord, "", "u", "", "", "", "", ""
        );

        if (transactionAdded) {
            JOptionPane.showMessageDialog(TransactionSystem.this, "Expense of ¥" + String.format("%.2f", amount) + " added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshTransactionTable();
            amountField.setText("");
            timeField.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date()));
            merchantField.setText("");
            typeComboBox.setSelectedIndex(0);
            passwordField.setText("");
        } else {
            JOptionPane.showMessageDialog(TransactionSystem.this, "Failed to add expense.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    });

    JButton cancelButton = new JButton("Cancel");
    cancelButton.setBackground(new Color(150, 150, 150));
    cancelButton.setForeground(Color.WHITE);
    cancelButton.setBorder(new RoundBorder(10, cancelButton.getBackground()));
    cancelButton.addMouseListener(new MouseAdapter() {
        @Override public void mouseEntered(MouseEvent e) { cancelButton.setBackground(new Color(170, 170, 170)); }
        @Override public void mouseExited(MouseEvent e) { cancelButton.setBackground(new Color(150, 150, 150)); }
    });
    cancelButton.addActionListener(e -> {
        amountField.setText("");
        timeField.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date()));
        merchantField.setText("");
        typeComboBox.setSelectedIndex(0);
        passwordField.setText("");
    });

    buttonPanel.add(confirmButton);
    buttonPanel.add(cancelButton);
    panel.add(buttonPanel, BorderLayout.SOUTH);

    return panel;
}
    // Module 6: Transaction Buttons
    JPanel createTransactionButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));
        panel.setBackground(Color.WHITE);
        panel.setOpaque(true);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Add Income 按钮
        GradientButton btnIncome = new GradientButton("Add Income");
        btnIncome.setPreferredSize(new Dimension(160, 40));
        btnIncome.addActionListener(e -> {
            // 找到最外层的 Window 作为 owner
            Window owner = SwingUtilities.getWindowAncestor(panel);
            JDialog dialog = new JDialog(owner, "Add Income Record", Dialog.ModalityType.APPLICATION_MODAL);
            // 把我们之前写的 createIncomePanel() 面板加进去
            dialog.setContentPane(createIncomePanel());
            dialog.pack();
            dialog.setLocationRelativeTo(owner);
            dialog.setVisible(true);
        });

        // Add Expense 按钮
        GradientButton btnExpense = new GradientButton("Add Expense");
        btnExpense.setPreferredSize(new Dimension(160, 40));
        btnExpense.addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(panel);
            JDialog dialog = new JDialog(owner, "Add Expense Record", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setContentPane(createExpensePanel());
            dialog.pack();
            dialog.setLocationRelativeTo(owner);
            dialog.setVisible(true);
        });

        panel.add(btnIncome);
        panel.add(btnExpense);
        return panel;
    }


    // Module 6: Historical Transaction Records
    JPanel createTransactionHistoryPanel() {
        // Main container
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 30, 0, 10),
                BorderFactory.createEmptyBorder()
        ));
        container.setBackground(Color.WHITE);

        // Header: Gradient balance panel
        DarkGradientPanel header = new DarkGradientPanel(); // Assumes DarkGradientPanel is defined
        header.setPreferredSize(new Dimension(0, 120));
        header.setLayout(null);

        AccountModel acct = UserSessionModel.getCurrentAccount();
        JLabel lblBalance = new JLabel("Your Balance: " + String.format("%.2f", acct.getBalance()) + " CNY");
        lblBalance.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblBalance.setForeground(Color.WHITE);
        lblBalance.setBounds(20, 20, 400, 40);
        header.add(lblBalance);

        container.add(header, BorderLayout.NORTH);

        // Middle: Transaction list
        // Filter records for current month
        java.util.Calendar cal = java.util.Calendar.getInstance();
        String currentYearMonth = String.format("%d/%02d",
                cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH) + 1
        );
        List<TransactionData> txs = TransactionAnalyzerModel.getFilteredTransactions(username, currentYearMonth);

        // Populate ListModel
        DefaultListModel<TransactionData> model = new DefaultListModel<>();
        for (TransactionData t : txs) {
            model.addElement(t);
        }

        // Display in JList
        JList<TransactionData> list = new JList<>(model);
        list.setCellRenderer((JList<? extends TransactionData> l, TransactionData t, int idx, boolean sel, boolean focus) -> {
            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(Color.WHITE);
            row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
            row.setPreferredSize(new Dimension(0, 80));

            // Left: Operation and Time
            JPanel left = new JPanel();
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            left.setOpaque(false);
            JLabel opLbl = new JLabel("  " + t.getOperation());
            opLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            JLabel timeLbl = new JLabel(" " + t.getTime());
            timeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            timeLbl.setForeground(Color.GRAY);
            left.add(opLbl);
            left.add(timeLbl);
            row.add(left, BorderLayout.WEST);

            // Right: Amount
            JLabel amtLbl = new JLabel(String.format("%+.2f", t.getAmount()), SwingConstants.RIGHT);
            amtLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            String opText = t.getOperation().toLowerCase();
            if ("income".equals(opText)) {
                amtLbl.setForeground(new Color(0, 150, 0));
            } else {
                amtLbl.setForeground(new Color(200, 0, 0));
            }
            row.add(amtLbl, BorderLayout.EAST);

            if (sel) {
                row.setBackground(new Color(230, 240, 255));
            }

            return row;
        });

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = Color.WHITE;
                this.trackColor = new Color(245, 245, 245);
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
                btn.setBorder(null);
                btn.setFocusable(false);
                btn.setContentAreaFilled(false);
                return btn;
            }
        });

        container.add(scroll, BorderLayout.CENTER);

        return container;
    }

    // Placeholder for DarkGradientPanel (assumed to be defined elsewhere)
    private static class DarkGradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(new GradientPaint(0, 0, new Color(30, 60, 120), getWidth(), getHeight(), new Color(50, 50, 50)));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    

    // Fetch real-time exchange rates (ExchangeRate-API)
private void fetchExchangeRates() {
    new Thread(() -> {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://v6.exchangerate-api.com/v6/" + exchangeApiKey + "/latest/" + baseCurrency))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject json = new JSONObject(response.body());
            JSONObject rates = json.getJSONObject("conversion_rates");

            SwingUtilities.invokeLater(() -> {
                exchangeRates.clear();
                double eurToCny = rates.getDouble(targetBaseCurrency);
                for (String currency : currencies) {
                    if (rates.has(currency)) {
                        // Calculate CNY/X = (EUR/X) / (EUR/CNY)
                        exchangeRates.put(currency, rates.getDouble(currency) / eurToCny);
                    }
                }
                updateRateTable();
                updateConversion();
            });
        } catch (IOException | InterruptedException e) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "Failed to fetch exchange rates: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            });
        }
    }).start();
}

    // Fetch historical exchange rates (fixer.io)
// Fetch historical exchange rates (fixer.io)
private void fetchHistoricalRates() {
    long currentTime = System.currentTimeMillis();
    if (currentTime - lastApiCallTimestamp < ONE_DAY_MS && !historicalRatesCache.isEmpty()) {
        return; // Use cached data if less than 24 hours
    }

    historicalRatesCache.clear();
    HttpClient client = HttpClient.newHttpClient();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Calendar cal = Calendar.getInstance();

    for (int i = 11; i >= 0; i--) {
        cal.setTime(new Date());
        cal.add(Calendar.MONTH, -i);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH)); // Last day of the month
        String date = sdf.format(cal.getTime());
        try {
            String symbols = String.join(",", currencies);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://data.fixer.io/api/" + date + "?access_key=" + fixerApiKey + "&base=" + baseCurrency + "&symbols=" + symbols))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject json = new JSONObject(response.body());
            if (!json.getBoolean("success")) {
                JSONObject error = json.getJSONObject("error");
                String errorType = error.optString("type", "unknown");
                String errorInfo = error.optString("info", "no info");
                throw new IOException("API error: type=" + errorType + ", info=" + errorInfo);
            }
            JSONObject rates = json.getJSONObject("rates");
            Map<String, Double> monthlyRates = new HashMap<>();
            double eurToCny = rates.getDouble(targetBaseCurrency); // EUR/CNY rate
            for (String currency : currencies) {
                if (rates.has(currency) && !currency.equals(targetBaseCurrency)) {
                    // Calculate CNY/X = (EUR/X) / (EUR/CNY)
                    monthlyRates.put(currency, rates.getDouble(currency) / eurToCny);
                }
            }
            historicalRatesCache.put(date, monthlyRates);
        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to fetch historical rates for " + date + ": " + e.getMessage());
        }
    }

    lastApiCallTimestamp = currentTime;
    saveCache();
}

    // Save cache to file
    private void saveCache() {
        try (FileWriter writer = new FileWriter(CACHE_FILE)) {
            Gson gson = new Gson();
            Map<String, Object> cache = new HashMap<>();
            cache.put("timestamp", lastApiCallTimestamp);
            cache.put("rates", historicalRatesCache);
            gson.toJson(cache, writer);
        } catch (IOException e) {
            System.err.println("Failed to save cache: " + e.getMessage());
        }
    }

    // Load cache from file
    private void loadCache() {
        File file = new File(CACHE_FILE);
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();
            Map<String, Object> cache = gson.fromJson(reader, Map.class);
            lastApiCallTimestamp = ((Double) cache.get("timestamp")).longValue();
            Map<String, Map<String, Double>> rates = (Map<String, Map<String, Double>>) cache.get("rates");
            historicalRatesCache.putAll(rates);
        } catch (IOException e) {
            System.err.println("Failed to load cache: " + e.getMessage());
        }
    }

    // Update rate table
    private void updateRateTable() {
        for (int i = 0, j = 0; i < currencies.length; i++) {
            if (!currencies[i].equals(baseCurrency)) {
                Double rate = exchangeRates.getOrDefault(currencies[i], 0.0);
                rateTable.setValueAt(String.format("%.4f", rate), j, 1);
                j++;
            }
        }
    }

    // Update currency conversion
private void updateConversion() {
    Component[] components = currencyComboBox.getParent().getComponents();
    RoundedTextField amountField = null;
    for (Component comp : components) {
        if (comp instanceof RoundedTextField) {
            amountField = (RoundedTextField) comp;
            break;
        }
    }
    if (amountField == null) return;

    String amountText = amountField.getActualText();
    String targetCurrency = (String) currencyComboBox.getSelectedItem();
    try {
        double amount = Double.parseDouble(amountText);
        Double rate = exchangeRates.getOrDefault(targetCurrency, 0.0);
        double result = amount * rate; // EUR to target currency
        conversionResultLabel.setText(String.format("Result: %.2f %s", result, targetCurrency));
    } catch (NumberFormatException e) {
        conversionResultLabel.setText("Result: -");
    }
}

 // Update historical trend chart
private void updateHistoricalTrend() {
    // Find the historical trend panel dynamically
    JPanel historicalPanel = null;
    for (Component comp : getComponents()) {
        if (comp instanceof MidGradientPanel) {
            JPanel panel = (JPanel) comp;
            if (panel.getComponentCount() > 0 && panel.getComponent(0) instanceof JLabel &&
                ((JLabel) panel.getComponent(0)).getText().equals("Historical Exchange Rate Trend")) {
                historicalPanel = panel;
                break;
            }
        }
    }
    if (historicalPanel == null) return;

    ChartPanel chartPanel = (ChartPanel) historicalPanel.getComponent(1);
    JFreeChart chart = chartPanel.getChart();
    TimeSeriesCollection dataset = (TimeSeriesCollection) chart.getXYPlot().getDataset();
    TimeSeries series = dataset.getSeries(0);
    series.clear();

    String selectedCurrency = (String) currencyComboBox.getSelectedItem();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Calendar cal = Calendar.getInstance();
    for (int i = 11; i >= 0; i--) {
        cal.setTime(new Date());
        cal.add(Calendar.MONTH, -i);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH)); // Last day of the month
        String date = sdf.format(cal.getTime());
        Map<String, Double> monthlyRates = historicalRatesCache.getOrDefault(date, new HashMap<>());
        Double rate = monthlyRates.getOrDefault(selectedCurrency, 0.0);
        if (rate > 0) {
            series.add(new org.jfree.data.time.Month(cal.getTime()), rate);
        }
    }
}

    // Refresh transaction table
    private void refreshTransactionTable() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        String currentYearMonth = String.format("%d/%02d",
                cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH) + 1
        );

        List<TransactionData> txs = TransactionAnalyzerModel.getFilteredTransactions(username, currentYearMonth);

        String[] colNames = {"Operation", "Amount"};
        Object[][] data = new Object[txs.size()][2];
        for (int i = 0; i < txs.size(); i++) {
            TransactionData t = txs.get(i);
            data[i][0] = t.getOperation();
            data[i][1] = String.format("%+.2f", t.getAmount());
        }

        transactionTable.setModel(new javax.swing.table.DefaultTableModel(data, colNames) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        transactionTable.getColumnModel().getColumn(0).setCellRenderer(center);
        transactionTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table,
                                                           Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String op = table.getValueAt(row, 0).toString().toLowerCase();
                double amt = Double.parseDouble(value.toString());
                if ("expense".equals(op)) { // Adjusted to match TransactionData operation
                    setForeground(Color.RED);
                    setText(String.format("-%.2f", Math.abs(amt)));
                } else {
                    setForeground(new Color(0, 128, 0));
                    setText(String.format("%.2f", amt));
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }
        });
    }

    // Start timer for periodic rate updates
    // Start timer for periodic rate updates
private void startRateUpdateTimer() {
    nextRefreshTime = System.currentTimeMillis() + 60000;
    rateUpdateTimer = new Timer(60000, e -> {
        fetchExchangeRates();
        nextRefreshTime = System.currentTimeMillis() + 60000;
    });
    rateUpdateTimer.start();

    countdownTimer = new Timer(1000, e -> {
        long remaining = (nextRefreshTime - System.currentTimeMillis()) / 1000;
        if (remaining >= 0) {
            countdownLabel.setText("Next refresh in " + remaining + " seconds");
        } else {
            countdownLabel.setText("Refreshing...");
        }
    });
    countdownTimer.start();
}

    // Nested UI Classes
    public static class RoundedTextField extends JTextField {
        private String placeholder;
        private boolean showingPlaceholder = true;

        public RoundedTextField(String placeholder) {
            this.placeholder = placeholder;
            setFont(new Font("Segoe UI", Font.PLAIN, 18));
            setForeground(Color.GRAY);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            setPreferredSize(new Dimension(240, 50));
            setText(placeholder);

            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) {
                    if (showingPlaceholder) {
                        setText("");
                        setForeground(Color.DARK_GRAY);
                        showingPlaceholder = false;
                    }
                }

                @Override public void focusLost(FocusEvent e) {
                    if (getText().trim().isEmpty()) {
                        setText(placeholder);
                        setForeground(Color.GRAY);
                        showingPlaceholder = true;
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int arc = 30;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setColor(new Color(200, 200, 200));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }

        public String getActualText() {
            return showingPlaceholder ? "" : getText();
        }
    }

    public static class RoundedPasswordField extends JPasswordField {
        private String placeholder;
        private boolean showingPlaceholder = true;

        public RoundedPasswordField(String placeholder) {
            this.placeholder = placeholder;
            setFont(new Font("Segoe UI", Font.PLAIN, 18));
            setForeground(Color.GRAY);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            setPreferredSize(new Dimension(240, 50));
            setEchoChar((char) 0);
            setText(placeholder);

            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) {
                    if (showingPlaceholder) {
                        setText("");
                        setForeground(Color.DARK_GRAY);
                        setEchoChar('●');
                        showingPlaceholder = false;
                    }
                }

                @Override public void focusLost(FocusEvent e) {
                    if (getPassword().length == 0) {
                        setText(placeholder);
                        setForeground(Color.GRAY);
                        setEchoChar((char) 0);
                        showingPlaceholder = true;
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int arc = 30;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setColor(new Color(200, 200, 200));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }

        public String getActualPassword() {
            return showingPlaceholder ? "" : new String(getPassword());
        }

        public boolean isPlaceholderShowing() {
            return showingPlaceholder;
        }
    }

    public static class RoundedComboBox<E> extends JComboBox<E> {
        public RoundedComboBox(E[] items) {
            super(items);
            setFont(new Font("Segoe UI", Font.PLAIN, 18));
            setOpaque(false);
            setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value,
                                                              int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel lbl = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                    return lbl;
                }
            });
            setPreferredSize(new Dimension(240, 50));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            int arc = 30;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setColor(new Color(200, 200, 200));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    
    
    public static class MidGradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, MID_GRADIENT_START, w, h, MID_GRADIENT_END);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
        }
    }
    
    public static class LightGradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, LIGHT_GRADIENT_START, w, h, LIGHT_GRADIENT_END);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
        }
    }
    public static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(156, 39, 176),
                    w, h, new Color(40, 100, 250)
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
        }
    }

    public static class GradientTextButton extends JButton {
        private Color colorStart = new Color(156, 39, 176);
        private Color colorEnd = new Color(0, 47, 167);

        public GradientTextButton(String text) {
            super(text);
            setFocusPainted(false);
            setContentAreaFilled(true);
            setOpaque(true);
            setBorderPainted(false);
            setBackground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(new Color(245, 245, 255));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(Color.WHITE);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(getText());
            int x = (getWidth() - textWidth) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            GradientPaint gp = new GradientPaint(x, 0, colorStart, x + textWidth, 0, colorEnd);
            g2.setPaint(gp);
            g2.setFont(getFont());
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    public static class GradientLabel extends JLabel {
        private Color c1 = new Color(0x9C27B0);
        private Color c2 = new Color(0x002FA7);

        public GradientLabel(String text) {
            super(text);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            FontMetrics fm = g2.getFontMetrics(getFont());
            int textWidth = fm.stringWidth(getText());
            GradientPaint gp = new GradientPaint(
                    0, 0, c1,
                    textWidth, 0, c2
            );
            g2.setPaint(gp);
            g2.setFont(getFont());
            int x = 0;
            int y = fm.getAscent();
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    static class ColoredHeaderRenderer extends DefaultTableCellRenderer {
        private final Color bg;

        public ColoredHeaderRenderer(Color bg) {
            this.bg = bg;
            setHorizontalAlignment(SwingConstants.CENTER);
            setForeground(Color.WHITE);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBackground(bg);
            return this;
        }
    }
}