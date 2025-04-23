package View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import AccountModel.TransactionServiceModel.TransactionData;
import PersonModel.TransactionAnalyzer;

/**
 * View class for rendering income and expense charts, tables, and analysis panels.
 */
public class IncomeExpenseChartView {

    /**
     * Returns a JPanel containing the expense category donut chart with centered labels.
     *
     * @param username   The username to fetch transactions for.
     * @param yearMonth  The year and month to filter transactions (format: yyyy/MM).
     * @return A JPanel displaying the expense donut chart.
     */
    public static JPanel getExpenseChartPanel(String username, String yearMonth) {
        return createDonutChartPanel(username, true, yearMonth);
    }

    /**
     * Returns a JPanel containing the income category donut chart with centered labels.
     *
     * @param username   The username to fetch transactions for.
     * @param yearMonth  The year and month to filter transactions (format: yyyy/MM).
     * @return A JPanel displaying the income donut chart.
     */
    public static JPanel getIncomeChartPanel(String username, String yearMonth) {
        return createDonutChartPanel(username, false, yearMonth);
    }

    /**
     * Creates a donut chart panel for either expense or income categories with centered labels.
     */
    private static JPanel createDonutChartPanel(String username, boolean isExpense, String yearMonth) {
        return new JPanel() {
            private List<Map.Entry<String, Double>> categoryEntries = new ArrayList<>();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                List<TransactionData> transactions = TransactionAnalyzer.getFilteredTransactions(username, yearMonth);
                Map<String, Double> categoryTotals = isExpense
                        ? TransactionAnalyzer.calculateExpenseCategoryTotals(transactions)
                        : TransactionAnalyzer.calculateIncomeCategoryTotals(transactions);

                double totalAmount = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();
                if (totalAmount <= 0) {
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    g2d.drawString("No valid " + (isExpense ? "expense" : "income") + " data to display.",
                            getWidth() / 2 - 70, getHeight() / 2);
                    g2d.dispose();
                    return;
                }

                categoryEntries.clear();
                categoryEntries.addAll(categoryTotals.entrySet());

                int startAngle = 0;
                int i = 0;
                Color[] colors = {
                        new Color(255, 182, 193), // Soft Pink
                        new Color(144, 238, 144), // Light Green
                        new Color(135, 206, 250), // Sky Blue
                        new Color(240, 230, 140), // Pale Yellow
                        new Color(221, 160, 221), // Light Plum
                        new Color(173, 216, 230), // Light Cyan
                        new Color(255, 218, 185), // Peach
                        new Color(200, 162, 200)  // Lavender
                };

                int diameter = Math.min(getWidth(), getHeight()) - 80;
                int radius = diameter / 2;
                int innerRadius = radius / 2;
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;

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

                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                startAngle = 0;
                for (int j = 0; j < categoryEntries.size(); j++) {
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

                    String labelText = String.format("%s: ¥%.2f (%.1f%%)", entry.getKey(), entry.getValue(), percentage * 100);
                    FontMetrics fm = g2d.getFontMetrics();
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

                    g2d.drawString(labelText, labelX, labelY);
                    startAngle += arcAngle;
                }

                g2d.dispose();
            }
        };
    }

    /**
     * Returns a JPanel containing a transaction records table.
     *
     * @param username   The username to fetch transactions for.
     * @param yearMonth  The year and month to filter transactions (format: yyyy/MM).
     * @return A JPanel displaying the transaction table.
     */
    public static JPanel getTransactionTablePanel(String username, String yearMonth) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Transaction Records", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        panel.add(title, BorderLayout.NORTH);

        List<TransactionData> transactions = TransactionAnalyzer.getFilteredTransactions(username, yearMonth);
        String[] columnNames = {"User", "Operation", "Amount", "Time", "Merchant", "Type", "Remark", "Category",
                "Payment Method", "Location"};
        Object[][] data = new Object[transactions.size()][columnNames.length];
        for (int i = 0; i < transactions.size(); i++) {
            TransactionData t = transactions.get(i);
            data[i] = new Object[] {
                    t.getUsername(), t.getOperation(), String.format("%.2f", t.getAmount()), t.getTime(),
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
     * Returns a JScrollPane containing type analysis (expense count, total amount, percentage by type).
     *
     * @param username   The username to fetch transactions for.
     * @param yearMonth  The year and month to filter transactions (format: yyyy/MM).
     * @return A JScrollPane displaying the type analysis.
     */
    public static JScrollPane getTypeAnalysisPanel(String username, String yearMonth) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        List<TransactionData> transactions = TransactionAnalyzer.getFilteredTransactions(username, yearMonth);
        Map<String, Integer> typeCounts = new HashMap<>();
        Map<String, Double> typeTotals = new HashMap<>();
        double totalExpense = 0.0;

        for (TransactionData t : transactions) {
            if ("Expense".equalsIgnoreCase(t.getOperation())) {
                String type = t.getType();
                if (type == null || type.trim().isEmpty() || "u".equalsIgnoreCase(type.trim())) {
                    type = "Unclassified";
                }
                typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
                typeTotals.put(type, typeTotals.getOrDefault(type, 0.0) + t.getAmount());
                totalExpense += t.getAmount();
            }
        }

        Color[] typeColors = {
                new Color(255, 182, 193), // Soft Pink
                new Color(144, 238, 144), // Light Green
                new Color(135, 206, 250), // Sky Blue
                new Color(240, 230, 140), // Pale Yellow
                new Color(221, 160, 221), // Light Plum
                new Color(173, 216, 230), // Light Cyan
                new Color(255, 218, 185), // Peach
                new Color(200, 162, 200)  // Lavender
        };

        int i = 0;
        for (Map.Entry<String, Integer> entry : typeCounts.entrySet().stream().limit(8).toList()) {
            String type = entry.getKey();
            int count = entry.getValue();
            double amount = typeTotals.get(type);
            double percentage = totalExpense > 0 ? (amount / totalExpense) * 100 : 0;

            JPanel typePanel = new JPanel(new GridLayout(3, 1, 5, 5));
            typePanel.setBackground(Color.WHITE);
            typePanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                    BorderFactory.createLineBorder(typeColors[i % typeColors.length], 4, true)
            ));
            typePanel.add(new JLabel(type, SwingConstants.CENTER));
            typePanel.add(new JLabel("Count: " + count, SwingConstants.CENTER));
            typePanel.add(new JLabel(String.format("¥%.2f (%.1f%%)", amount, percentage), SwingConstants.CENTER));
            panel.add(typePanel);
            i++;
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        return scrollPane;
    }

    /**
     * Returns a JPanel containing bar charts for daily expenses and income.
     *
     * @param username   The username to fetch transactions for.
     * @param yearMonth  The year and month to filter transactions (format: yyyy/MM).
     * @return A JPanel displaying the daily bar chart.
     */
    public static JPanel getDailyLineChartPanel(String username, String yearMonth) {
        return new JPanel(new BorderLayout(10, 10)) {
            private double totalIncome = 0.0;
            private double totalExpense = 0.0;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                List<TransactionData> transactions = TransactionAnalyzer.getFilteredTransactions(username, yearMonth);

                double[] dailyExpenses = new double[31];
                double[] dailyIncomes = new double[31];
                double maxAmount = 0;
                totalIncome = 0.0;
                totalExpense = 0.0;

                for (TransactionData t : transactions) {
                    try {
                        String dayStr = t.getTime().substring(8, 10);
                        int day = Integer.parseInt(dayStr) - 1;
                        if (day >= 0 && day < 31) {
                            if ("Expense".equalsIgnoreCase(t.getOperation())) {
                                dailyExpenses[day] += t.getAmount();
                                totalExpense += t.getAmount();
                            } else if ("Income".equalsIgnoreCase(t.getOperation())) {
                                dailyIncomes[day] += t.getAmount();
                                totalIncome += t.getAmount();
                            }
                            maxAmount = Math.max(maxAmount, Math.max(dailyExpenses[day], dailyIncomes[day]));
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing date: " + t.getTime());
                    }
                }

                if (maxAmount == 0) {
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    g2d.drawString("No data for selected period", getWidth() / 2 - 70, getHeight() / 2);
                    g2d.dispose();
                    return;
                }

                int margin = 50;
                int width = getWidth() - 2 * margin;
                int height = getHeight() - 2 * margin;
                int barWidth = width / 62;

                g2d.setColor(Color.BLACK);
                g2d.drawLine(margin, margin, margin, margin + height);
                g2d.drawLine(margin, margin + height, margin + width, margin + height);

                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.setColor(Color.BLACK);
                int numTicks = 5;
                double amountStep = maxAmount / numTicks;
                for (int i = 0; i <= numTicks; i++) {
                    double amount = i * amountStep;
                    int y = margin + height - (int) ((amount / maxAmount) * height);
                    g2d.drawLine(margin - 5, y, margin, y);
                    String amountLabel = String.format("%.0f", amount);
                    g2d.drawString(amountLabel, margin - 40, y + 5);
                }

                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.setColor(Color.BLACK);
                FontMetrics fm = g2d.getFontMetrics();
                String amountLabel = "Amount (¥)";
                int labelWidth = fm.stringWidth(amountLabel);
                g2d.drawString(amountLabel, margin - labelWidth - 20, margin - 10);

                for (int i = 0; i < 31; i++) {
                    int x = margin + i * barWidth * 2;
                    double expense = dailyExpenses[i];
                    double income = dailyIncomes[i];

                    boolean hasBoth = expense > 0 && income > 0;
                    int adjustedBarWidth = hasBoth ? barWidth : barWidth - 2;
                    int roundness = hasBoth ? 15 : 10;

                    if (expense > 0) {
                        int expenseHeight = (int) ((expense / maxAmount) * height);
                        g2d.setColor(new Color(255, 99, 132));
                        g2d.fillRoundRect(x, margin + height - expenseHeight, adjustedBarWidth, expenseHeight, roundness, roundness);
                    }

                    if (income > 0) {
                        int incomeHeight = (int) ((income / maxAmount) * height);
                        g2d.setColor(new Color(54, 162, 235));
                        g2d.fillRoundRect(x + barWidth, margin + height - incomeHeight, adjustedBarWidth, incomeHeight, roundness, roundness);
                    }
                }

                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.setColor(Color.BLACK);
                for (int i = 0; i <= 30; i += 5) {
                    int x = margin + i * barWidth * 2;
                    g2d.drawString(String.valueOf(i + 1), x - 5, margin + height + 20);
                }
                g2d.drawString("Day", margin + width / 2 - 20, margin + height + 40);

                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.setColor(Color.BLACK);
                fm = g2d.getFontMetrics();
                int textHeight = fm.getHeight();
                g2d.drawString("Total Income: ¥" + String.format("%.2f", totalIncome), margin + width - 120, margin + textHeight);
                g2d.drawString("Total Expense: ¥" + String.format("%.2f", totalExpense), margin + width - 120, margin + textHeight * 2);

                g2d.dispose();
            }
        };
    }

    /**
     * Custom ScrollBar UI for a modern look.
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
}