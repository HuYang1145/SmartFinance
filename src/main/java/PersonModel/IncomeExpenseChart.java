package PersonModel;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import AccountModel.TransactionService;
import AccountModel.TransactionService.TransactionData;
import AccountModel.UserSession;

public class IncomeExpenseChart {

    /**
     * Calculates the total amount for each expense category based on transaction data.
     */
    public static Map<String, Double> calculateExpenseCategoryTotals(List<TransactionData> transactions) {
        Map<String, Double> categoryTotals = new HashMap<>();
        if (transactions == null) {
            System.err.println("IncomeExpenseChart: Received null transaction list.");
            return categoryTotals;
        }

        for (TransactionData transaction : transactions) {
            if ("Expense".equalsIgnoreCase(transaction.getOperation())) {
                String category = transaction.getCategory();
                if (category == null || category.trim().isEmpty() || "u".equalsIgnoreCase(category.trim())) {
                    category = "Unclassified";
                } else {
                    category = category.trim();
                }
                double amount = transaction.getAmount();
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
            }
        }
        return categoryTotals;
    }

    /**
     * Calculates the total amount for each income category based on transaction data.
     */
    public static Map<String, Double> calculateIncomeCategoryTotals(List<TransactionData> transactions) {
        Map<String, Double> categoryTotals = new HashMap<>();
        if (transactions == null) {
            System.err.println("IncomeExpenseChart: Received null transaction list.");
            return categoryTotals;
        }

        for (TransactionData transaction : transactions) {
            if ("Income".equalsIgnoreCase(transaction.getOperation())) {
                String category = transaction.getCategory();
                if (category == null || category.trim().isEmpty() || "u".equalsIgnoreCase(category.trim())) {
                    category = "Unclassified";
                } else {
                    category = category.trim();
                }
                double amount = transaction.getAmount();
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
            }
        }
        return categoryTotals;
    }

    /**
     * Returns a JPanel containing the expense category donut chart with centered labels.
     */
    public static JPanel getExpenseChartPanel(String username, String yearMonth) {
        return new JPanel() {
            private List<Map.Entry<String, Double>> categoryEntries = new ArrayList<>();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Set background to white explicitly
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                List<TransactionData> transactions = TransactionService.readTransactions(username);
                transactions = filterByYearMonth(transactions, yearMonth);
                Map<String, Double> categoryTotals = calculateExpenseCategoryTotals(transactions);

                double totalAmount = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();
                if (totalAmount <= 0) {
                    g2d.drawString("No valid expense data to display.", getWidth() / 2 - 70, getHeight() / 2);
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

                // Draw donut chart
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

                // Draw labels around the donut chart without lines
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

                    // Calculate the angle for the label (middle of the arc)
                    int labelAngle = startAngle + arcAngle / 2;
                    double labelAngleRad = Math.toRadians(labelAngle);

                    // Calculate the position for the label (outside the donut chart)
                    int labelRadius = radius + 30; // Distance from center for labels
                    int labelX = centerX + (int) (labelRadius * Math.cos(labelAngleRad));
                    int labelY = centerY - (int) (labelRadius * Math.sin(labelAngleRad));

                    // Prepare label text
                    String labelText = String.format("%s: ¥%.2f (%.1f%%)", entry.getKey(), entry.getValue(), percentage * 100);
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(labelText);
                    int textHeight = fm.getHeight();

                    // Adjust label position to avoid going out of bounds
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

                    // Draw the label
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(labelText, labelX, labelY);

                    startAngle += arcAngle;
                }

                g2d.dispose();
            }
        };
    }

    /**
     * Returns a JPanel containing the income category donut chart with centered labels.
     */
    public static JPanel getIncomeChartPanel(String username, String yearMonth) {
        return createDonutChartPanel(username, false, yearMonth);
    }

    /**
     * Creates a donut chart panel for either expense or income categories with centered labels.
     */
    private static JPanel createDonutChartPanel(String username, boolean isExpense, String yearMonth) {
        JPanel panel = new JPanel() {
            private List<Map.Entry<String, Double>> categoryEntries = new ArrayList<>();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Ensure background is white
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                List<TransactionData> transactions = TransactionService.readTransactions(username);
                transactions = filterByYearMonth(transactions, yearMonth);
                Map<String, Double> categoryTotals = isExpense ? calculateExpenseCategoryTotals(transactions)
                        : calculateIncomeCategoryTotals(transactions);

                double totalAmount = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();
                if (totalAmount <= 0) {
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    g2d.drawString("No valid " + (isExpense ? "expense" : "income") + " data to display.",
                            getWidth() / 2 - 70, getHeight() / 2);
                    g2d.dispose();
                    return;
                }

                // Limit to top 5 categories, combine the rest into "Other"
                List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(categoryTotals.entrySet());
                sortedEntries.sort((a, b) -> Double.compare(b.getValue(), a.getValue())); // Sort descending by amount
                categoryEntries.clear();
                double otherAmount = 0.0;
                for (int i = 0; i < sortedEntries.size(); i++) {
                    if (i < 5) {
                        categoryEntries.add(sortedEntries.get(i));
                    } else {
                        otherAmount += sortedEntries.get(i).getValue();
                    }
                }
                if (otherAmount > 0) {
                    categoryEntries.add(new AbstractMap.SimpleEntry<>("Other", otherAmount));
                }

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

                // Draw donut chart
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

                // Draw labels around the donut chart without lines
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                startAngle = 0;
                List<Rectangle> labelBounds = new ArrayList<>(); // Track label bounds to avoid overlap
                for (int j = 0; j < categoryEntries.size(); j++) {
                    Map.Entry<String, Double> entry = categoryEntries.get(j);
                    double percentage = entry.getValue() / totalAmount;
                    int arcAngle = (int) Math.round(percentage * 360);
                    if (arcAngle == 0 && entry.getValue() > 0) {
                        arcAngle = 1;
                    }

                    // Calculate the angle for the label (middle of the arc)
                    int labelAngle = startAngle + arcAngle / 2;
                    double labelAngleRad = Math.toRadians(labelAngle);

                    // Calculate the position for the label (outside the donut chart)
                    int labelRadius = radius + 40; // Increase distance for better spacing
                    int labelX = centerX + (int) (labelRadius * Math.cos(labelAngleRad));
                    int labelY = centerY - (int) (labelRadius * Math.sin(labelAngleRad));

                    // Prepare label text
                    String labelText = String.format("%s: ¥%.2f (%.1f%%)", entry.getKey(), entry.getValue(), percentage * 100);
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(labelText);
                    int textHeight = fm.getHeight();

                    // Adjust label position to avoid going out of bounds
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

                    // Check for overlap and adjust position
                    Rectangle newLabelBounds = new Rectangle(labelX, labelY - textHeight, textWidth, textHeight);
                    boolean overlap;
                    do {
                        overlap = false;
                        for (Rectangle bounds : labelBounds) {
                            if (newLabelBounds.intersects(bounds)) {
                                overlap = true;
                                // Shift label downward to avoid overlap with larger spacing
                                labelY += textHeight + 5; // Increased spacing
                                newLabelBounds.setLocation(labelX, labelY - textHeight);
                                // Re-check bounds
                                if (labelY > getHeight()) {
                                    labelY = getHeight() - 5;
                                    labelX -= textWidth + 5; // Shift left if we hit the bottom
                                    newLabelBounds.setLocation(labelX, labelY - textHeight);
                                }
                            }
                        }
                    } while (overlap);

                    labelBounds.add(newLabelBounds);

                    // Draw the label
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(labelText, labelX, labelY);

                    startAngle += arcAngle;
                }

                g2d.dispose();
            }
        };
        panel.setOpaque(true); // Ensure panel is opaque for background
        panel.setBackground(Color.WHITE); // Set background to white
        panel.setPreferredSize(new Dimension(300, 300));
        return panel;
    }

    /**
     * Returns a JPanel containing a transaction records table.
     */
    public static JPanel getTransactionTablePanel(String username, String yearMonth) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Title
        JLabel title = new JLabel("Transaction Records", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // Reduced bottom padding
        panel.add(title, BorderLayout.NORTH);

        // Purple line below the title
        JPanel linePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(128, 0, 128));
                g2d.setStroke(new BasicStroke(4));
                g2d.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2d.dispose();
            }
        };
        linePanel.setPreferredSize(new Dimension(0, 10));
        linePanel.setBackground(Color.WHITE);
        panel.add(linePanel, BorderLayout.CENTER);

        // Table
        List<TransactionData> transactions = TransactionService.readTransactions(username);
        transactions = filterByYearMonth(transactions, yearMonth);
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
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new Dimension(0, 250)); // Height matches Daily Income & Expense

        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());

        // Add table in a wrapper panel to reduce spacing
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0)); // Minimal top padding
        tableWrapper.add(scrollPane, BorderLayout.CENTER);
        panel.add(tableWrapper, BorderLayout.SOUTH);

        return panel;
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

    /**
     * Returns a JPanel containing type analysis (expense count, total amount, percentage by type).
     */
    public static JScrollPane getTypeAnalysisPanel(String username, String yearMonth) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        List<TransactionData> transactions = TransactionService.readTransactions(username);
        transactions = filterByYearMonth(transactions, yearMonth);
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
     * Returns a JPanel containing bar charts for daily expenses and income with interaction.
     */
    public static JPanel getDailyLineChartPanel(String username, String yearMonth) {
        JPanel panel = new JPanel(new BorderLayout(10, 10)) {
            private double totalIncome = 0.0;
            private double totalExpense = 0.0;
    
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
                List<TransactionData> transactions = TransactionService.readTransactions(username);
                transactions = filterByYearMonth(transactions, yearMonth);
    
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
                int barWidth = width / 62; // 31 days, 2 bars per day

                // Draw axes
                g2d.setColor(Color.BLACK);
                g2d.drawLine(margin, margin, margin, margin + height);
                g2d.drawLine(margin, margin + height, margin + width, margin + height);

                // Draw vertical axis labels (amounts)
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.setColor(Color.BLACK);
                int numTicks = 5; // Number of ticks on the vertical axis
                double amountStep = maxAmount / numTicks;
                for (int i = 0; i <= numTicks; i++) {
                    double amount = i * amountStep;
                    int y = margin + height - (int) ((amount / maxAmount) * height);
                    g2d.drawLine(margin - 5, y, margin, y); // Small tick mark
                    String amountLabel = String.format("%.0f", amount);
                    g2d.drawString(amountLabel, margin - 40, y + 5);
                }

                // Draw "Amount (¥)" label above Y-axis, moved further right
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.setColor(Color.BLACK);
                FontMetrics fm = g2d.getFontMetrics();
                String amountLabel = "Amount (¥)";
                int labelWidth = fm.stringWidth(amountLabel);
                g2d.drawString(amountLabel, margin - labelWidth - 20, margin - 10); // Moved 10 pixels further left

                // Draw bars (always show both income and expense)
                for (int i = 0; i < 31; i++) {
                    int x = margin + i * barWidth * 2;
                    double expense = dailyExpenses[i];
                    double income = dailyIncomes[i];

                    // Determine if both income and expense exist
                    boolean hasBoth = expense > 0 && income > 0;
                    int adjustedBarWidth = hasBoth ? barWidth : barWidth - 2; // Thicker bars if both exist
                    int roundness = hasBoth ? 15 : 10; // More rounded ends if both exist

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

                // Draw labels
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.setColor(Color.BLACK);
                for (int i = 0; i <= 30; i += 5) {
                    int x = margin + i * barWidth * 2;
                    g2d.drawString(String.valueOf(i + 1), x - 5, margin + height + 20);
                }
                g2d.drawString("Day", margin + width / 2 - 20, margin + height + 40);

                // Draw totals in top-right with consistent font
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Changed to PLAIN to match other text
                g2d.setColor(Color.BLACK);
                fm = g2d.getFontMetrics();
                int textHeight = fm.getHeight();
                g2d.drawString("Total Income: ¥" + String.format("%.2f", totalIncome), margin + width - 120, margin + textHeight);
                g2d.drawString("Total Expense: ¥" + String.format("%.2f", totalExpense), margin + width - 120, margin + textHeight * 2);

                g2d.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(400, 300));
        panel.setBackground(Color.WHITE);
        return panel;
    }

    /**
     * Returns a JPanel containing 6 plates: 2 donut charts, transaction table, type analysis, and daily bar chart.
     */
    public static JPanel getIncomeExpensePlane(String username, String yearMonth) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        // Plate 1: Expense Donut Chart
        JPanel expensePanel = new JPanel(new BorderLayout(10, 10));
        expensePanel.setBackground(Color.WHITE);
        JLabel expenseTitle = new JLabel("Expense Categories", SwingConstants.CENTER);
        expenseTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        expensePanel.add(expenseTitle, BorderLayout.NORTH);
        JPanel expenseChartPanel = getExpenseChartPanel(username, yearMonth);
        expensePanel.add(expenseChartPanel, BorderLayout.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.33;
        gbc.weighty = 0.4;
        panel.add(expensePanel, gbc);

        // Plate 2: Income Donut Chart
        JPanel incomePanel = new JPanel(new BorderLayout(10, 10));
        incomePanel.setBackground(Color.WHITE);
        JLabel incomeTitle = new JLabel("Income Categories", SwingConstants.CENTER);
        incomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        incomePanel.add(incomeTitle, BorderLayout.NORTH);
        JPanel incomeChartPanel = getIncomeChartPanel(username, yearMonth); // Corrected line
        incomePanel.add(incomeChartPanel, BorderLayout.CENTER);
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(incomePanel, gbc);

        // Plate 3 & 4: Transaction Table
        JPanel tablePanel = getTransactionTablePanel(username, yearMonth);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.weightx = 0.66;
        gbc.weighty = 0.6;
        panel.add(tablePanel, gbc);

        // Plate 5: Type Analysis
        JPanel typeAnalysisPanel = new JPanel(new BorderLayout(10, 10));
        typeAnalysisPanel.setBackground(Color.WHITE);
        JLabel typeTitle = new JLabel("Type Analysis", SwingConstants.CENTER);
        typeTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        typeAnalysisPanel.add(typeTitle, BorderLayout.NORTH);
        typeAnalysisPanel.add(getTypeAnalysisPanel(username, yearMonth), BorderLayout.CENTER);
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.33;
        gbc.weighty = 0.4;
        panel.add(typeAnalysisPanel, gbc);

        // Plate 6: Daily Bar Chart
        JPanel barChartPanel = new JPanel(new BorderLayout(10, 10));
        barChartPanel.setBackground(Color.WHITE);
        JLabel barTitle = new JLabel("Daily Income & Expense", SwingConstants.CENTER);
        barTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        barChartPanel.add(barTitle, BorderLayout.NORTH);
        barChartPanel.add(getDailyLineChartPanel(username, yearMonth), BorderLayout.CENTER);
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.33;
        gbc.weighty = 0.6;
        panel.add(barChartPanel, gbc);

        return panel;
    }

    /**
     * Filters transactions by year and month.
     */
    public static List<TransactionData> filterByYearMonth(List<TransactionData> transactions, String yearMonth) {
        List<TransactionData> filtered = new ArrayList<>();
        // yearMonth is in format "yyyy/MM" (e.g., "2025/04")
        System.out.println("Filtering for yearMonth: " + yearMonth);
        for (TransactionData t : transactions) {
            String transactionTime = t.getTime(); // e.g., "2025/04/01"
            System.out.println("Transaction time: " + transactionTime);
            if (transactionTime != null && transactionTime.startsWith(yearMonth)) {
                filtered.add(t);
                System.out.println("Matched transaction: " + t.toString());
            }
        }
        System.out.println("Filtered transactions count: " + filtered.size());
        return filtered;
    }

    /**
     * Creates and displays a donut chart for expense categories in a new window (legacy method).
     */
    public static void showIncomeExpensePieChart(String filePath_no_longer_needed) {
        String currentUsername = UserSession.getCurrentUsername();
        if (currentUsername == null) {
            JOptionPane.showMessageDialog(null, "Please log in first!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        System.out.println("Generating expense chart for user: " + currentUsername);

        try {
            List<TransactionData> transactions = TransactionService.readTransactions(currentUsername);
            Map<String, Double> categoryTotals = calculateExpenseCategoryTotals(transactions);

            if (categoryTotals.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No expense data found to display in the chart.", "Info", JOptionPane.INFORMATION_MESSAGE);
                System.out.println("No expense data found for chart.");
                return;
            }

            JFrame frame = new JFrame("Personal Expense Category Donut Chart");
            Calendar cal = Calendar.getInstance();
            String currentMonth = new SimpleDateFormat("yyyy/MM").format(cal.getTime());
            JPanel panel = getExpenseChartPanel(currentUsername, currentMonth);
            frame.add(panel);
            frame.pack();
            frame.setMinimumSize(new Dimension(550, 550));
            frame.setSize(650, 650);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to generate chart: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}