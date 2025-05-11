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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

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
        // 1. 整个面板改用 BorderLayout
        setLayout(new BorderLayout(0,0));
        setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        setBackground(new Color(245,245,245));

        // 2. 左右两侧面板
        JSplitPane left = createLeftPanel();
        JPanel right = createRightPanel();

        // 3. 用 SplitPane 分割，左 2/3、右 1/3
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.67);      // 左侧占 67%
        split.setDividerSize(4);
        split.setContinuousLayout(true);
        split.setBorder(null);
        split.setOpaque(false);

        add(split, BorderLayout.CENTER);

        // 界面可见后再定位分隔条比例
        SwingUtilities.invokeLater(() -> split.setDividerLocation(0.67));
    }

    // 左侧：交易历史 + 操作按钮
    private JSplitPane createLeftPanel() {
        JPanel history = createTransactionHistoryPanel();
        JPanel input   = createTransactionInputPanel();

        JSplitPane vsplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                history,
                input
        );
        vsplit.setResizeWeight(0.8);     // 上半部分（历史）占 50%
        vsplit.setDividerSize(4);
        vsplit.setBorder(null);
        return vsplit;
    }


    // 右侧：汇率、转换、趋势，垂直排列
    private JPanel createRightPanel() {
        JPanel p = new JPanel(new GridLayout(3,1,0,0));
        p.setOpaque(false);
        p.add(createExchangeRatePanel());
        p.add(createCurrencyConversionPanel());
        p.add(createHistoricalTrendPanel());
        return p;
    }

    // Module 1: Real-Time Exchange Rate Monitoring
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

    // Module 2: Currency Conversion
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

    // Module 3: Selected Currency Historical Exchange Rate Trend
    private JPanel createHistoricalTrendPanel() {
        JPanel panel = new TransactionSystemComponents.BlueGradientPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Historical Exchange Rate Trend", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.BLACK);
        panel.add(title, BorderLayout.NORTH);

        // 1. 创建时间序列
        TimeSeries series = new TimeSeries("Rate");

        // 2. 自动生成最近6个月的模拟数据
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            Calendar cal = Calendar.getInstance();
            double baseRate = 6.90; // 设定基准汇率

            for (int i = 5; i >= 0; i--) {
                Calendar temp = (Calendar) cal.clone();
                temp.add(Calendar.MONTH, -i);  // 往前推i个月
                Date date = temp.getTime();
                double rate = baseRate + Math.random() * 0.2 - 0.1; // 小幅随机波动（±0.1）
                series.add(new org.jfree.data.time.Month(date), rate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection(series);

        // 3. 创建 Chart
        JFreeChart chart = org.jfree.chart.ChartFactory.createTimeSeriesChart(
                null, "Month", "Rate",
                dataset, false, true, false
        );

        // 4. 美化 Chart 样式
        DateAxis dateAxis = (DateAxis) chart.getXYPlot().getDomainAxis();
        dateAxis.setDateFormatOverride(new java.text.SimpleDateFormat("yyyy-MM"));
        dateAxis.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));

        chart.getXYPlot().getRangeAxis().setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        chart.getXYPlot().getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 12));

        chart.getXYPlot().setBackgroundPaint(Color.WHITE);
        chart.getXYPlot().setDomainGridlinePaint(new Color(200, 200, 200));
        chart.getXYPlot().setRangeGridlinePaint(new Color(200, 200, 200));

        chart.getXYPlot().getRenderer().setSeriesPaint(0, new Color(255, 165, 0)); // 橙色曲线
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
                BorderFactory.createEmptyBorder(20, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel("Add Transaction", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0x2C3C49));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(20, 0));
        content.setOpaque(false);

        // 左侧：表单（不含 Password）
        JScrollPane formScroll = new JScrollPane(buildFormPanel(false));
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        formScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        customizeScrollBar(formScroll);
        content.add(formScroll, BorderLayout.CENTER);

        // 右侧：Password输入框 + 确认/取消按钮
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

        // Confirm按钮
        confirm.addActionListener(e -> {
            if (transactionActionListener != null) {
                transactionActionListener.actionPerformed(e);
            }
        });

// Cancel按钮
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


    // 把你原来那一大块 GridBagLayout 代码提炼到这里
    private JPanel buildFormPanel(boolean includePassword) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_START; // 左对齐
        gbc.weightx = 1.0;  // 让输入框撑满

        int y = 0;

        // Operation
        gbc.gridx = 0; gbc.gridy = y;
        form.add(createWhiteLabel("Operation:"), gbc);
        gbc.gridx = 1;
        form.add(operationComboBox = new LoginRoundedInputField.RoundedComboBox<>(
                new String[]{"Income", "Expense"}), gbc);
        y++;

        // Amount
        gbc.gridx = 0; gbc.gridy = y;
        form.add(createWhiteLabel("Amount (¥):"), gbc);
        gbc.gridx = 1;
        form.add(transactionAmountField = new LoginRoundedInputField.RoundedTextField(
                "Enter amount (e.g., 100.50)"), gbc);
        y++;

        // Time
        gbc.gridx = 0; gbc.gridy = y;
        form.add(createWhiteLabel("Time (yyyy/MM/dd HH:mm):"), gbc);
        gbc.gridx = 1;
        form.add(transactionTimeField = new LoginRoundedInputField.RoundedTextField(
                new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date())), gbc);
        y++;

        // Merchant
        gbc.gridx = 0; gbc.gridy = y;
        form.add(createWhiteLabel("Merchant (English):"), gbc);
        gbc.gridx = 1;
        form.add(merchantField = new LoginRoundedInputField.RoundedTextField(
                "Enter merchant (e.g., Amazon)"), gbc);
        y++;

        // Type
        gbc.gridx = 0; gbc.gridy = y;
        form.add(createWhiteLabel("Type:"), gbc);
        gbc.gridx = 1;
        form.add(typeComboBox = new LoginRoundedInputField.RoundedComboBox<>(
                new String[]{"food", "salary", "rent", "freelance", "investment",
                        "shopping", "Electronics", "Fitness", "Transport",
                        "Entertainment", "Travel", "Gift"}), gbc);
        y++;

        // Remark
        gbc.gridx = 0; gbc.gridy = y;
        form.add(createWhiteLabel("Remark (Optional):"), gbc);
        gbc.gridx = 1;
        form.add(remarkField = new LoginRoundedInputField.RoundedTextField(
                "Enter remark (e.g., Grocery)"), gbc);
        y++;

        // Category
        gbc.gridx = 0; gbc.gridy = y;
        form.add(createWhiteLabel("Category (Purpose):"), gbc);
        gbc.gridx = 1;
        form.add(categoryField = new LoginRoundedInputField.RoundedTextField(
                "Enter category (e.g., Household)"), gbc);
        y++;

        // Payment Method
        gbc.gridx = 0; gbc.gridy = y;
        form.add(createWhiteLabel("Payment Method:"), gbc);
        gbc.gridx = 1;
        form.add(paymentMethodField = new LoginRoundedInputField.RoundedTextField(
                "Enter payment method (e.g., Credit Card)"), gbc);
        y++;

        // Location
        gbc.gridx = 0; gbc.gridy = y;
        form.add(createWhiteLabel("Location:"), gbc);
        gbc.gridx = 1;
        form.add(locationComboBox = new LoginRoundedInputField.RoundedComboBox<>(
                new String[]{"Online", "Offline"}), gbc);
        y++;

        // Tag
        gbc.gridx = 0; gbc.gridy = y;
        form.add(createWhiteLabel("Tag (Optional):"), gbc);
        gbc.gridx = 1;
        form.add(tagField = new LoginRoundedInputField.RoundedTextField(
                "Enter tag (e.g., Urgent)"), gbc);
        y++;

        // Attachment
        gbc.gridx = 0; gbc.gridy = y;
        form.add(createWhiteLabel("Attachment (Optional):"), gbc);
        gbc.gridx = 1;
        form.add(attachmentField = new LoginRoundedInputField.RoundedTextField(
                "Enter attachment path (e.g., receipt.pdf)"), gbc);
        y++;

        // Recurrence
        gbc.gridx = 0; gbc.gridy = y;
        form.add(createWhiteLabel("Recurrence (Optional):"), gbc);
        gbc.gridx = 1;
        form.add(recurrenceField = new LoginRoundedInputField.RoundedTextField(
                "Enter recurrence (e.g., Monthly)"), gbc);

        // ➡️ 不加 Password（因为 Password 移到右边了）
        return form;
    }

    private JLabel createWhiteLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(0x2C3C49));
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return label;
    }


    // Module 5: Transaction History (Detailed)
    private JPanel createTransactionHistoryPanel() {
        // 整体容器：白底
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 10));
        container.setBackground(Color.WHITE);

        // 1. 渐变色头部：显示余额
        TransactionSystemComponents.DarkGradientPanel header = new TransactionSystemComponents.DarkGradientPanel();
        header.setPreferredSize(new Dimension(0, 120));
        header.setLayout(null);
        User acct = UserSession.getCurrentAccount();
        JLabel lbl = new JLabel(
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
        DefaultListModel<Transaction> model = new DefaultListModel<>();
        for (Transaction t : all) {
            if (t.getTimestamp().startsWith(ym)) {
                model.addElement(t);
            }
        }

        // 3. 用 JList + 自定义渲染器
        JList<Transaction> list = new JList<>(model);
        list.setCellRenderer(new ListCellRenderer<Transaction>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends Transaction> list,
                                                          Transaction t, int idx,
                                                          boolean isSel, boolean cellHasFocus) {
                // 每一行都是一个 BorderLayout 容器
                JPanel row = new JPanel(new BorderLayout());
                row.setBackground(Color.WHITE);
                row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230,230,230)));
                row.setPreferredSize(new Dimension(0, 80));

                // 左：操作 + 时间（垂直排）
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

                // 右：金额，红/绿色
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
                    amt.setForeground(new Color(0,150,0));  // 绿色
                } else {
                    amt.setForeground(new Color(200,0,0));  // 红色
                }
                row.add(amt, BorderLayout.EAST);


                if (isSel) {
                    row.setBackground(new Color(230,240,255));
                }
                return row;
            }
        });

        // 4. 装到 JScrollPane，白底滚动区 + 自定义滚动条
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