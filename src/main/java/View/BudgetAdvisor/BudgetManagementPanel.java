package View.BudgetAdvisor;

import View.Transaction.TransactionSystemComponents;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
// import java.util.Collections; // If sorting of monthlyDataListForChart is needed and not guaranteed by service

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import Controller.BudgetManagementController;
import Model.BudgetDataContainer;
import Service.BudgetService;

/**
 * Panel for managing budget, viewing financial advice, and tracking spending.
 * It includes charts for expense ratios and income amounts, along with sections
 * for budget goals, custom budget settings, saving advice, and spending status.
 */
public class BudgetManagementPanel extends TransactionSystemComponents.BlueGradientPanel {
    private BudgetManagementController controller;
    private JLabel budgetValueLabel, savingGoalValueLabel, modeValueLabel, reasonValueLabel;
    private JTextField customBudgetInputField;
    private JButton saveCustomBudgetButton, restoreIntelligentButton;
    private JTextArea largeConsumptionTextArea;
    private JLabel topSpendingCategoryLabel, expenditureValueLabel, budgetStatusLabel;
    private boolean isInitialized;

    private static final Color SECTION_BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Border SECTION_BORDER = BorderFactory.createLineBorder(new Color(220, 220, 220));
    private static final Color BUTTON_BACKGROUND_COLOR = Color.LIGHT_GRAY;
    private static final Color BUTTON_FOREGROUND_COLOR = Color.BLACK;
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);

    private JPanel expenseRatioChartPanel;
    private JPanel incomeAmountChartPanel;
    private List<BudgetService.LargeConsumptionItem> currentLargeConsumptions;
    private JComboBox<String> sortComboBox;
    private List<BudgetService.MonthlyFinancialData> monthlyDataListForChart = new ArrayList<>();

    /**
     * Constructs a BudgetManagementPanel.
     *
     * @param username The username of the current user. Can be null or empty if no user is logged in,
     * in which case a "User not logged in" message is displayed.
     * @param budgetService The service to handle budget-related data operations.
     */
    public BudgetManagementPanel(String username, BudgetService budgetService) {
        super();
        if (username == null || username.trim().isEmpty()) {
            setLayout(new BorderLayout());
            add(new JLabel("User not logged in. Please log in to view budget information.", SwingConstants.CENTER), BorderLayout.CENTER);
            this.controller = null;
            this.isInitialized = false;
            return;
        }
        this.controller = new BudgetManagementController(username, this, budgetService);
        this.isInitialized = true;
        setLayout(new GridBagLayout());
        initComponents();
        expenseRatioChartPanel = createExpenseRatioChartPanel();
        incomeAmountChartPanel = createIncomeAmountChartPanel();
        layoutComponents();
        if (isInitialized && controller != null) {
            controller.loadBudgetData();
        }
    }

    /**
     * Initializes the UI components such as labels, text fields, and buttons.
     */
    private void initComponents() {
        budgetValueLabel = new JLabel("Loading...");
        savingGoalValueLabel = new JLabel("Loading...");
        modeValueLabel = new JLabel("Loading...");
        reasonValueLabel = new JLabel("Loading...");
        customBudgetInputField = new JTextField(8);
        saveCustomBudgetButton = new JButton("Save");
        restoreIntelligentButton = new JButton("Restore Intelligent");
        largeConsumptionTextArea = new JTextArea(8, 20);
        largeConsumptionTextArea.setEditable(false);
        topSpendingCategoryLabel = new JLabel("N/A");
        expenditureValueLabel = new JLabel("Loading...");
        budgetStatusLabel = new JLabel("Loading...");

        String[] sortOptions = {"Date (Earliest First)", "Date (Latest First)", "Amount (High to Low)", "Amount (Low to High)"};
        sortComboBox = new JComboBox<>(sortOptions);
        sortComboBox.addActionListener(e -> updateLargeConsumptionDisplay());

        Font infoFont = new Font("Segoe UI", Font.PLAIN, 14);
        budgetValueLabel.setFont(infoFont);
        savingGoalValueLabel.setFont(infoFont);
        modeValueLabel.setFont(infoFont);
        reasonValueLabel.setFont(infoFont);
        customBudgetInputField.setFont(infoFont);
        largeConsumptionTextArea.setFont(infoFont);
        topSpendingCategoryLabel.setFont(infoFont);
        expenditureValueLabel.setFont(infoFont);
        budgetStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        styleCustomButton(saveCustomBudgetButton);
        saveCustomBudgetButton.addActionListener(e -> {
            if (controller != null) {
                controller.saveCustomBudget(customBudgetInputField.getText());
            }
        });
        styleCustomButton(restoreIntelligentButton);
        restoreIntelligentButton.addActionListener(e -> {
            if (controller != null) {
                controller.clearCustomBudget();
            }
        });
    }

    /**
     * Applies a common style to the provided button.
     * @param button The JButton to style.
     */
    private void styleCustomButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(BUTTON_FOREGROUND_COLOR);
        button.setBackground(BUTTON_BACKGROUND_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Creates a JPanel with a standard frame (border and background) to wrap content.
     * @param content The JComponent to be placed inside the framed panel.
     * @return A JPanel with the specified content and styling.
     */
    private JPanel createFramedPanel(JComponent content) {
        JPanel framedPanel = new JPanel();
        framedPanel.setLayout(new BoxLayout(framedPanel, BoxLayout.Y_AXIS));
        framedPanel.setBackground(SECTION_BACKGROUND_COLOR);
        framedPanel.setBorder(new CompoundBorder(SECTION_BORDER, new EmptyBorder(10, 10, 10, 10)));
        framedPanel.add(content);
        framedPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return framedPanel;
    }

    /**
     * Creates the panel for displaying the expense ratio chart.
     * The chart is custom drawn using AWT Graphics.
     * @return A JPanel configured for the expense ratio chart.
     */
    private JPanel createExpenseRatioChartPanel() {
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 0)); // Transparent background
                g2d.fillRect(0, 0, getWidth(), getHeight());
                int startX = (getWidth() - 300) / 2;
                int startY = (getHeight() - 230) / 2;
                drawExpenseRatioChart(g2d, monthlyDataListForChart, startX, startY); // 调用正确的绘制方法
                g2d.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(300, 230); // Reduced size
            }
        };
        chartPanel.setOpaque(false);
        chartPanel.setBackground(new Color(0, 0, 0, 0));
        chartPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        return chartPanel;
    }

    /**
     * Creates the panel for displaying the income amount chart.
     * The chart is custom drawn using AWT Graphics.
     * @return A JPanel configured for the income amount chart.
     */
    private JPanel createIncomeAmountChartPanel() {
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                int startX = (getWidth() - 300) / 2;
                int startY = (getHeight() - 230) / 2;
                drawIncomeAmountChart(g2d, monthlyDataListForChart, startX, startY);
                g2d.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(300, 230);
            }
        };
        chartPanel.setOpaque(false);
        chartPanel.setBackground(new Color(0, 0, 0, 0));
        chartPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        return chartPanel;
    }

    /**
     * Draws the expense ratio line chart using AWT Graphics.
     * Displays data for up to the last 3 months.
     *
     * @param g2d The Graphics2D context to draw on.
     * @param allData A list of all available monthly financial data, assumed to be sorted chronologically (oldest first).
     */
    private void drawExpenseRatioChart(Graphics2D g2d, List<BudgetService.MonthlyFinancialData> allData, int startX, int startY) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (allData == null || allData.isEmpty()) {
            g2d.setColor(Color.BLACK);
            String noDataMsg = "No data";
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(noDataMsg, startX + (300 - fm.stringWidth(noDataMsg)) / 2, startY + 230 / 2);
            return;
        }

        List<BudgetService.MonthlyFinancialData> displayData = new ArrayList<>();
        int startIndex = Math.max(0, allData.size() - 3);
        for (int i = startIndex; i < allData.size(); i++) {
            displayData.add(allData.get(i));
        }

        if (displayData.isEmpty()) {
            g2d.setColor(Color.BLACK);
            String noDataMsg = "No recent data";
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(noDataMsg, startX + (300 - fm.stringWidth(noDataMsg)) / 2, startY + 230 / 2);
            return;
        }

        int padding = 0;
        int labelPadding = 70;
        Font axisFont = new Font("Segoe UI", Font.PLAIN, 15);
        g2d.setFont(axisFont);
        FontMetrics fm = g2d.getFontMetrics();

        int chartWidth = 300 - 2 * padding;
        int chartHeight = 230 - 2 * padding;

        double maxRatio = 0.0;
        for (BudgetService.MonthlyFinancialData data : displayData) {
            if (data.getIncome() > 0) {
                maxRatio = Math.max(maxRatio, data.getExpense() / data.getIncome());
            }
        }
        if (maxRatio == 0.0) maxRatio = 1.0;
        else maxRatio = Math.min(1.0, Math.ceil(maxRatio * 10.0) / 10.0);

        g2d.setColor(Color.BLACK);
        g2d.drawLine(startX + padding, startY + padding, startX + padding, startY + padding + chartHeight);
        int numYLabels = 3;
        for (int i = 0; i <= numYLabels; i++) {
            double ratioVal = maxRatio * ((double) i / numYLabels);
            String yLabel = String.format("%.0f%%", ratioVal * 100);
            int y = startY + padding + chartHeight - (int) (chartHeight * (ratioVal / maxRatio));
            Rectangle2D labelBounds = fm.getStringBounds(yLabel, g2d);
            g2d.drawString(yLabel, startX + padding - (int)labelBounds.getWidth() - labelPadding, y + fm.getAscent()/2);
            g2d.setColor(new Color(230,230,230));
            g2d.drawLine(startX + padding + 1, y, startX + padding + chartWidth, y);
            g2d.setColor(Color.BLACK);
        }
        String yAxisTitle = "Exp/Inc";
        FontRenderContext frc = g2d.getFontRenderContext();
        Rectangle2D yTitleBounds = axisFont.getStringBounds(yAxisTitle, frc);
        Graphics2D g2dRotate = (Graphics2D) g2d.create();
        g2dRotate.rotate(-Math.PI / 2);
        g2dRotate.setFont(axisFont);
        g2dRotate.drawString(yAxisTitle, -(startY + padding + chartHeight / 2 + (int)(yTitleBounds.getWidth() / 2)), startX + padding / 4 - (int)yTitleBounds.getHeight() + fm.getAscent()-30);
        g2dRotate.dispose();

        g2d.drawLine(startX + padding, startY + padding + chartHeight, startX + padding + chartWidth, startY + padding + chartHeight);

        g2d.setColor(new Color(0x84ACC9));
        g2d.setStroke(new java.awt.BasicStroke(3.0f));

        int pointRadius = 2;
        if (displayData.size() == 1) {
            BudgetService.MonthlyFinancialData data = displayData.get(0);
            double ratio = (data.getIncome() > 0) ? data.getExpense() / data.getIncome() : 0;
            ratio = Math.min(ratio, maxRatio);
            int x = startX + padding + chartWidth / 2;
            int y = startY + padding + chartHeight - (int) ((ratio / maxRatio) * chartHeight);
            g2d.fillOval(x - pointRadius, y - pointRadius, pointRadius * 2, pointRadius * 2);
            String monthName = data.getMonth().substring(0, 3);
            int monthNameWidth = fm.stringWidth(monthName);
            g2d.setColor(Color.BLACK);
            g2d.drawString(monthName, x - monthNameWidth / 2, startY + padding + chartHeight + labelPadding + fm.getAscent());
        } else {
            double pointDeltaX = (double) chartWidth / (displayData.size() - 1);
            int prevX = -1, prevY = -1;
            for (int i = 0; i < displayData.size(); i++) {
                BudgetService.MonthlyFinancialData data = displayData.get(i);
                double ratio = (data.getIncome() > 0) ? data.getExpense() / data.getIncome() : 0;
                ratio = Math.min(ratio, maxRatio);
                int x = startX + padding + (int) (i * pointDeltaX);
                int y = startY + padding + chartHeight - (int) ((ratio / maxRatio) * chartHeight);
                g2d.setColor(new Color(0x84ACC9));
                g2d.fillOval(x - pointRadius, y - pointRadius, pointRadius * 2, pointRadius * 2);
                if (prevX != -1) {
                    g2d.draw(new Line2D.Double(prevX, prevY, x, y));
                }
                prevX = x;
                prevY = y;
            }

            FontMetrics fmAxis = g2d.getFontMetrics(axisFont);
            for (int i = 0; i < displayData.size(); i++) {
                BudgetService.MonthlyFinancialData data = displayData.get(i);
                String monthYear = data.getMonth();
                String month;
                if (monthYear.length() > 7) {
                    month = monthYear.substring(5, 7);
                } else if (monthYear.length() > 4) {
                    month = monthYear.substring(5);
                } else {
                    month = monthYear;
                }

                try {
                    int monthNumber = Integer.parseInt(month);
                    java.time.Month monthEnum = java.time.Month.of(monthNumber);
                    month = monthEnum.toString().substring(0, 3);
                } catch (NumberFormatException e) {
                    month = monthYear.substring(0, Math.min(3, monthYear.length()));
                }

                double pointDeltaXForLabel = (double) chartWidth / (displayData.size() - 1);
                int x = startX + padding + (int) (i * pointDeltaXForLabel);
                int monthNameWidth = fmAxis.stringWidth(month);
                g2d.setColor(Color.BLACK);
                g2d.drawString(month, x - monthNameWidth / 2, startY + padding + chartHeight + labelPadding + fmAxis.getAscent() - 50);
            }
        }

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 25));
        fm = g2d.getFontMetrics();
        String chartTitle = "Expense Ratio";
        g2d.setColor(Color.BLACK);
        g2d.drawString(chartTitle, startX + (300 - fm.stringWidth(chartTitle)) / 2, startY + padding / 2 + fm.getAscent() / 2 - 30);
    }

    /**
     * Draws the income amount bar chart using AWT Graphics.
     * Displays data for up to the last 3 months, with bars colored
     * Sky Blue (newest, right), Light Green (middle), and Moccasin (oldest, left).
     *
     * @param g2d The Graphics2D context to draw on.
     * @param allData A list of all available monthly financial data, assumed to be sorted chronologically (oldest first).
     */
    private void drawIncomeAmountChart(Graphics2D g2d, List<BudgetService.MonthlyFinancialData> allData, int startX, int startY) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (allData == null || allData.isEmpty()) {
            g2d.setColor(Color.BLACK);
            String noDataMsg = "No data to display";
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(noDataMsg, startX + (300 - fm.stringWidth(noDataMsg)) / 2, startY + 230 / 2);
            return;
        }

        List<BudgetService.MonthlyFinancialData> displayData = new ArrayList<>();
        int startIndex = Math.max(0, allData.size() - 3);
        for (int i = startIndex; i < allData.size(); i++) {
            displayData.add(allData.get(i));
        }

        if (displayData.isEmpty()) {
            g2d.setColor(Color.BLACK);
            String noDataMsg = "No data available for the last 3 months";
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(noDataMsg, startX + (300 - fm.stringWidth(noDataMsg)) / 2, startY + 230 / 2);
            return;
        }

        int padding = 0;
        int labelPadding = 70;
        Font axisFont = new Font("Segoe UI", Font.PLAIN, 15);
        Font valueFont = new Font("Segoe UI", Font.PLAIN, 15);
        g2d.setFont(axisFont);
        FontMetrics fm = g2d.getFontMetrics();

        int chartWidth = 300 - 2 * padding;
        int chartHeight = 230 - 2 * padding;

        double maxIncome = 0;
        for (BudgetService.MonthlyFinancialData data : displayData) {
            maxIncome = Math.max(maxIncome, data.getIncome());
        }
        if (maxIncome == 0) maxIncome = 1000;
        else maxIncome = Math.ceil(maxIncome / 500.0) * 500.0;

        g2d.setColor(Color.BLACK);
        g2d.drawLine(startX + padding, startY + padding, startX + padding, startY + padding + chartHeight);
        int numYLabels = 4;
        for (int i = 0; i <= numYLabels; i++) {
            double incomeVal = maxIncome * ((double) i / numYLabels);
            String yLabel = String.format("¥%.0f", incomeVal);
            int y = startY + padding + chartHeight - (int) (chartHeight * (incomeVal / maxIncome));
            Rectangle2D labelBounds = fm.getStringBounds(yLabel, g2d);
            g2d.drawString(yLabel, startX + padding - (int)labelBounds.getWidth() - labelPadding, y + fm.getAscent()/2);
            g2d.setColor(new Color(230,230,230));
            g2d.drawLine(startX + padding + 1, y, startX + padding + chartWidth, y);
            g2d.setColor(Color.BLACK);
        }
        String yAxisTitle = "Income";
        FontRenderContext frc = g2d.getFontRenderContext();
        Rectangle2D yTitleBounds = axisFont.getStringBounds(yAxisTitle, frc);
        Graphics2D g2dRotate = (Graphics2D) g2d.create();
        g2dRotate.rotate(-Math.PI / 2);
        g2dRotate.setFont(axisFont);
        g2dRotate.drawString(yAxisTitle, -(startY + padding + chartHeight / 2 + (int)(yTitleBounds.getWidth() / 2)), startX + padding / 4 + fm.getAscent()-30);
        g2dRotate.dispose();

        g2d.drawLine(startX + padding, startY + padding + chartHeight, startX + padding + chartWidth, startY + padding + chartHeight);

        int numBars = displayData.size();
        double barGroupWidth = (double) chartWidth / numBars;
        double barWidthRatio = 0.4;
        int actualBarWidth = (int) (barGroupWidth * barWidthRatio);
        int barSidePadding = (int) (barGroupWidth * (1 - barWidthRatio) / 2);

        Color[] barColorsPalette = {new Color(0xFFE4B5), new Color(0x90EE90), new Color(0x84ACC9)};

        for (int i = 0; i < numBars; i++) {
            BudgetService.MonthlyFinancialData data = displayData.get(i);
            double income = data.getIncome();
            int barHeight = (maxIncome == 0) ? 0 : (int) ((income / maxIncome) * chartHeight * 0.8);
            int x = startX + padding + (int)(i * barGroupWidth) + barSidePadding;
            int y = startY + padding + chartHeight - barHeight;

            Color barColor;
            if (numBars == 1) barColor = barColorsPalette[2];
            else if (numBars == 2) barColor = barColorsPalette[i + 1];
            else barColor = barColorsPalette[i];

            g2d.setColor(barColor);
            g2d.fillRect(x, y, actualBarWidth, barHeight);
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawRect(x, y, actualBarWidth, barHeight);

            String monthName = data.getMonth();
            int monthNameWidth = fm.stringWidth(monthName);
            g2d.setColor(Color.BLACK);
            g2d.drawString(monthName, x + actualBarWidth / 2 - monthNameWidth / 2, startY + padding + chartHeight + labelPadding + fm.getAscent());

            g2d.setFont(valueFont);
            FontMetrics valueFm = g2d.getFontMetrics();
            String incomeText = String.format("¥%.0f", income);
            int incomeTextWidth = valueFm.stringWidth(incomeText);
            int textX = x + actualBarWidth / 2 - incomeTextWidth / 2;
            int textY = y - 3;
            if (barHeight > valueFm.getHeight() + 2)
                g2d.drawString(incomeText, textX, textY);
            g2d.setFont(axisFont);
        }

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 30));
        fm = g2d.getFontMetrics();
        String chartTitle = "Income Trend";
        g2d.setColor(Color.BLACK);
        g2d.drawString(chartTitle, startX + (300 - fm.stringWidth(chartTitle)) / 2, startY + padding/2 + fm.getAscent()/2-70);
    }

    /**
     * Updates the data used for drawing the charts.
     * This method should be called when new historical monthly data is available.
     * The provided list is assumed to be sorted chronologically (oldest month first).
     *
     * @param monthlyDataList The list of {@link BudgetService.MonthlyFinancialData} to display.
     */
    public void updateChartData(List<BudgetService.MonthlyFinancialData> monthlyDataList) {
        this.monthlyDataListForChart.clear();
        if (monthlyDataList != null) {
            this.monthlyDataListForChart.addAll(monthlyDataList);
        }

        if (expenseRatioChartPanel != null) {
            expenseRatioChartPanel.repaint();
        }
        if (incomeAmountChartPanel != null) {
            incomeAmountChartPanel.repaint();
        }
    }

    /**
     * Updates the budget-related information displayed on the panel.
     * This includes budget goals, spending status, and large consumptions.
     *
     * @param data A {@link BudgetDataContainer} object holding the budget data.
     */
    public void updateBudgetData(BudgetDataContainer data) {
        if (!isInitialized) return;

        BudgetService.BudgetRecommendation recommendation = data.getRecommendation();
        double currentMonthExpense = data.getCurrentMonthExpense();
        double currentMonthIncome = data.getCurrentMonthIncome();
        String topCategory = data.getTopCategory();
        currentLargeConsumptions = data.getLargeConsumptions();
        Double customBudget = data.getCustomBudget();

        double budgetToUse = (customBudget != null && customBudget >= 0) ? customBudget : recommendation.getSuggestedBudget();
        budgetValueLabel.setText(String.format("¥%.2f", budgetToUse));
        savingGoalValueLabel.setText(String.format("¥%.2f", Math.max(0, currentMonthIncome - budgetToUse)));
        modeValueLabel.setText(recommendation.getMode().getDisplayName());
        reasonValueLabel.setText(recommendation.getReason());
        topSpendingCategoryLabel.setText(topCategory != null ? topCategory : "No expenses this month");
        updateLargeConsumptionDisplay();
        updateSpendingStatusLabel(currentMonthExpense, budgetToUse);

        revalidate();
        repaint();
    }

    /**
     * Sets the loading state of the panel.
     * When loading, components display "Loading..." and buttons may be disabled.
     *
     * @param isLoading true if data is currently being loaded, false otherwise.
     */
    public void setLoadingState(boolean isLoading) {
        if (!isInitialized) return;
        String loadingText = "Loading...";
        if (isLoading) {
            budgetValueLabel.setText(loadingText);
            savingGoalValueLabel.setText(loadingText);
            modeValueLabel.setText(loadingText);
            reasonValueLabel.setText(loadingText);
            topSpendingCategoryLabel.setText("N/A");
            expenditureValueLabel.setText(loadingText);
            budgetStatusLabel.setText(loadingText);
            largeConsumptionTextArea.setText(loadingText + "\n\nPlease wait...");
            if (expenseRatioChartPanel != null) expenseRatioChartPanel.repaint();
            if (incomeAmountChartPanel != null) incomeAmountChartPanel.repaint();
        }
        saveCustomBudgetButton.setEnabled(!isLoading);
        restoreIntelligentButton.setEnabled(!isLoading);
        customBudgetInputField.setEnabled(!isLoading);
        sortComboBox.setEnabled(!isLoading);
    }

    /**
     * Displays an error message dialog.
     * @param message The error message to display.
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Displays an informational message dialog.
     * @param message The message to display.
     */
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Checks if the panel has been initialized (i.e., a user is logged in).
     * @return true if initialized, false otherwise.
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Updates the labels related to spending status (current expenditure and budget surplus/deficit).
     * @param currentExpense The total expenses for the current month.
     * @param budget The allocated budget for the current month.
     */
    private void updateSpendingStatusLabel(double currentExpense, double budget) {
        String statusText;
        Color textColor;
        expenditureValueLabel.setText(String.format("¥%.2f", currentExpense));
        if (currentExpense > budget) {
            statusText = String.format("Over budget by ¥%.2f", currentExpense - budget);
            textColor = Color.RED;
        } else {
            statusText = String.format("¥%.2f remaining in budget", budget - currentExpense);
            textColor = new Color(0, 128, 0); // Green
        }
        budgetStatusLabel.setText(statusText);
        budgetStatusLabel.setForeground(textColor);
    }

    /**
     * Arranges all UI components on the panel using GridBagLayout.
     * This method defines the overall structure and layout of the panel.
     */
    private void layoutComponents() {
        removeAll();
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        int horizontalSpacing = 15;
        int verticalSpacing = 12;

        JLabel mainTitle = new JLabel("Budget Management & Advice", SwingConstants.CENTER);
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        mainTitle.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weighty = 0.05;
        gbc.insets = new Insets(verticalSpacing, horizontalSpacing, verticalSpacing * 2, horizontalSpacing);
        add(mainTitle, gbc);

        gbc.gridwidth = 1;
        gbc.weighty = 0.475;
        Insets contentInsets = new Insets(verticalSpacing, horizontalSpacing, verticalSpacing, horizontalSpacing);

        JPanel topLeftContainer = new JPanel();
        topLeftContainer.setLayout(new BoxLayout(topLeftContainer, BoxLayout.Y_AXIS));
        topLeftContainer.setOpaque(false);

        JLabel goalsTitleLabel = new JLabel("Monthly Saving Goal & Budget", SwingConstants.LEFT);
        goalsTitleLabel.setFont(TITLE_FONT);
        JPanel goalsPanelContent = createGoalsPanelContent();
        JPanel goalsSectionPanel = new JPanel();
        goalsSectionPanel.setLayout(new BoxLayout(goalsSectionPanel, BoxLayout.Y_AXIS));
        goalsSectionPanel.setOpaque(false);
        goalsSectionPanel.add(goalsTitleLabel);
        goalsSectionPanel.add(Box.createVerticalStrut(5));
        goalsSectionPanel.add(goalsPanelContent);
        topLeftContainer.add(createFramedPanel(goalsSectionPanel));
        topLeftContainer.add(Box.createVerticalStrut(verticalSpacing));

        JLabel customBudgetTitleLabel = new JLabel("Custom Budget", SwingConstants.LEFT);
        customBudgetTitleLabel.setFont(TITLE_FONT);
        JPanel customBudgetPanelContent = createCustomBudgetPanelContent();
        JPanel customBudgetSectionPanel = new JPanel();
        customBudgetSectionPanel.setLayout(new BoxLayout(customBudgetSectionPanel, BoxLayout.Y_AXIS));
        customBudgetSectionPanel.setOpaque(false);
        customBudgetSectionPanel.add(customBudgetTitleLabel);
        customBudgetSectionPanel.add(Box.createVerticalStrut(5));
        customBudgetSectionPanel.add(customBudgetPanelContent);
        topLeftContainer.add(createFramedPanel(customBudgetSectionPanel));

        gbc.gridx = 0; gbc.gridy = 1; gbc.insets = contentInsets;
        add(topLeftContainer, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.insets = contentInsets;
        add(expenseRatioChartPanel, gbc);

        JPanel bottomLeftContainer = new JPanel();
        bottomLeftContainer.setLayout(new BoxLayout(bottomLeftContainer, BoxLayout.Y_AXIS));
        bottomLeftContainer.setOpaque(false);

        JLabel savingAdviceTitleLabel = new JLabel("Saving Advice - Reducible Consumption", SwingConstants.LEFT);
        savingAdviceTitleLabel.setFont(TITLE_FONT);
        JPanel savingAdvicePanelContent = createSavingAdvicePanelContent();
        JPanel savingAdviceSectionPanel = new JPanel();
        savingAdviceSectionPanel.setLayout(new BoxLayout(savingAdviceSectionPanel, BoxLayout.Y_AXIS));
        savingAdviceSectionPanel.setOpaque(false);
        savingAdviceSectionPanel.add(savingAdviceTitleLabel);
        savingAdviceSectionPanel.add(Box.createVerticalStrut(5));
        savingAdviceSectionPanel.add(savingAdvicePanelContent);
        bottomLeftContainer.add(createFramedPanel(savingAdviceSectionPanel));
        bottomLeftContainer.add(Box.createVerticalStrut(verticalSpacing));

        JLabel spendingStatusTitleLabel = new JLabel("Spending Status", SwingConstants.LEFT);
        spendingStatusTitleLabel.setFont(TITLE_FONT);
        JPanel spendingStatusPanelContent = createSpendingStatusPanelContent();
        JPanel spendingStatusSectionPanel = new JPanel();
        spendingStatusSectionPanel.setLayout(new BoxLayout(spendingStatusSectionPanel, BoxLayout.Y_AXIS));
        spendingStatusSectionPanel.setOpaque(false);
        spendingStatusSectionPanel.add(spendingStatusTitleLabel);
        spendingStatusSectionPanel.add(Box.createVerticalStrut(5));
        spendingStatusSectionPanel.add(spendingStatusPanelContent);
        bottomLeftContainer.add(createFramedPanel(spendingStatusSectionPanel));

        gbc.gridx = 0; gbc.gridy = 2; gbc.insets = contentInsets;
        add(bottomLeftContainer, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.insets = contentInsets;
        add(incomeAmountChartPanel, gbc);

        revalidate();
        repaint();
    }

    /**
     * Creates the content panel for the "Monthly Saving Goal & Budget" section.
     * @return A JPanel containing labels for budget, saving goal, mode, and reason.
     */
    private JPanel createGoalsPanelContent() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 5, 2, 10);

        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);

        JLabel budgetLabelText = new JLabel("Budget:"); budgetLabelText.setFont(labelFont);
        JLabel savingGoalLabelText = new JLabel("Saving Goal:"); savingGoalLabelText.setFont(labelFont);
        JLabel modeLabelText = new JLabel("Mode:"); modeLabelText.setFont(labelFont);
        JLabel reasonLabelText = new JLabel("Reason:"); reasonLabelText.setFont(labelFont);

        gbc.gridx = 0; gbc.gridy = 0; content.add(budgetLabelText, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; content.add(budgetValueLabel, gbc);
        gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 0; gbc.gridy = 1; content.add(savingGoalLabelText, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; content.add(savingGoalValueLabel, gbc);
        gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 0; gbc.gridy = 2; content.add(modeLabelText, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; content.add(modeValueLabel, gbc);
        gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 0; gbc.gridy = 3; content.add(reasonLabelText, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; content.add(reasonValueLabel, gbc);
        gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        return content;
    }

    /**
     * Creates the content panel for the "Custom Budget" section.
     * @return A JPanel containing input field and buttons for setting a custom budget.
     */
    private JPanel createCustomBudgetPanelContent() {
        JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        content.setOpaque(false);
        JLabel setBudgetLabel = new JLabel("Set Custom Budget (¥):");
        setBudgetLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        content.add(setBudgetLabel);
        customBudgetInputField.setPreferredSize(new Dimension(100, 28));
        content.add(customBudgetInputField);
        content.add(saveCustomBudgetButton);
        content.add(restoreIntelligentButton);
        return content;
    }

    /**
     * Creates the content panel for the "Saving Advice" section.
     * @return A JPanel containing information about top spending and large consumptions.
     */
    private JPanel createSavingAdvicePanelContent() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.weightx = 1.0;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);

        JLabel topSpendingLabelText = new JLabel("Top spending category this month:"); topSpendingLabelText.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 0; content.add(topSpendingLabelText, gbc);
        gbc.gridy++; content.add(topSpendingCategoryLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 5, 3, 5);
        JPanel largeConsumptionHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        largeConsumptionHeader.setOpaque(false);
        JLabel largeConsLabel = new JLabel("Large Consumptions (Over 7% of Income):"); largeConsLabel.setFont(labelFont);
        largeConsumptionHeader.add(largeConsLabel);
        sortComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        largeConsumptionHeader.add(Box.createHorizontalStrut(10));
        largeConsumptionHeader.add(sortComboBox);
        content.add(largeConsumptionHeader, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(3, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        JScrollPane largeConsumptionScrollPane = new JScrollPane(largeConsumptionTextArea);
        largeConsumptionTextArea.setBorder(BorderFactory.createLineBorder(new Color(200,200,200)));
        largeConsumptionScrollPane.setMinimumSize(new Dimension(100, 80));
        content.add(largeConsumptionScrollPane, gbc);
        return content;
    }

    /**
     * Creates the content panel for the "Spending Status" section.
     * @return A JPanel displaying current month's expenditure and budget status.
     */
    private JPanel createSpendingStatusPanelContent() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5,5,5,5);

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 14);

        JLabel currentExpenditurePromptLabel = new JLabel("This month's expenditure:");
        currentExpenditurePromptLabel.setFont(labelFont);

        JPanel expenditureLinePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
        expenditureLinePanel.setOpaque(false);
        expenditureLinePanel.add(currentExpenditurePromptLabel);
        expenditureLinePanel.add(expenditureValueLabel);

        gbc.gridx = 0; gbc.gridy = 0;
        content.add(expenditureLinePanel, gbc);

        gbc.gridy = 1;
        content.add(budgetStatusLabel, gbc);
        return content;
    }

    /**
     * Updates the text area displaying large consumption items.
     * Items are sorted based on the selection in the sortComboBox.
     */
    private void updateLargeConsumptionDisplay() {
        if (currentLargeConsumptions == null || currentLargeConsumptions.isEmpty()) {
            largeConsumptionTextArea.setText("No large consumption items found this month.");
            return;
        }

        List<BudgetService.LargeConsumptionItem> sortedList = new ArrayList<>(currentLargeConsumptions);
        String selectedSort = (String) sortComboBox.getSelectedItem();

        if (selectedSort != null) {
            switch (selectedSort) {
                case "Date (Earliest First)":
                    sortedList.sort(Comparator.comparing(BudgetService.LargeConsumptionItem::getDate));
                    break;
                case "Date (Latest First)":
                    sortedList.sort(Comparator.comparing(BudgetService.LargeConsumptionItem::getDate).reversed());
                    break;
                case "Amount (High to Low)":
                    sortedList.sort(Comparator.comparingDouble(BudgetService.LargeConsumptionItem::getAmount).reversed());
                    break;
                case "Amount (Low to High)":
                    sortedList.sort(Comparator.comparingDouble(BudgetService.LargeConsumptionItem::getAmount));
                    break;
            }
        }

        StringBuilder sb = new StringBuilder();
        java.time.format.DateTimeFormatter displayFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd");
        for (BudgetService.LargeConsumptionItem item : sortedList) {
            String type = item.getType() != null ? item.getType() : "N/A"; // Assuming getType() exists
            sb.append(String.format("%s - %s: ¥%.2f",
                            item.getDate().format(displayFormatter),
                            type,
                            item.getAmount()))
                    .append("\n");
        }
        largeConsumptionTextArea.setText(sb.toString().trim());
        largeConsumptionTextArea.setCaretPosition(0);
    }

    /**
     * Overridden paintComponent to handle custom painting for the BlueGradientPanel.
     * @param g The Graphics context.
     */
    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
    }
}