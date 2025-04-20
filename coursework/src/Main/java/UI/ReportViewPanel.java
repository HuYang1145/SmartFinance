package UI;

import Model.UserSession;
// 移除 JFreeChart 的 imports
// import org.jfree.chart.ChartFactory;
// import org.jfree.chart.ChartPanel;
// import org.jfree.chart.JFreeChart;
// ... (其他 JFreeChart imports)

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Collections; // Import Collections for min/max

public class ReportViewPanel extends JPanel {

    private PersonalUI parentUI;
    private JLabel incomeLabel;
    private JLabel expenditureLabel;
    private JLabel savingsLabel;
    private JPanel chartContainerPanel; // Panel to hold the custom chart panel
    private CustomChartPanel customChartPanel; // Reference to the custom chart panel
    private JButton backButton;

    private final SimpleDateFormat csvDateFormat = new SimpleDateFormat("yyyy/M/d HH:mm:ss");
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("MM/dd"); // For chart axis display

    public ReportViewPanel(PersonalUI parent) {
        this.parentUI = parent;
        initComponents();
    }

    private void initComponents() {
        setBackground(new Color(245, 245, 245));
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Last cycle financial report", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 60, 120));
        add(titleLabel, BorderLayout.NORTH);

        // --- Content Panel (Summary + Chart) ---
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setOpaque(false);

        // Summary Panel (West)
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 30));

        Font summaryFont = new Font("Segoe UI", Font.PLAIN, 18);
        incomeLabel = new JLabel("Income: ¥0.00");
        incomeLabel.setFont(summaryFont);
        expenditureLabel = new JLabel("Expenditure: ¥0.00");
        expenditureLabel.setFont(summaryFont);
        savingsLabel = new JLabel("Savings: ¥0.00");
        savingsLabel.setFont(summaryFont);

        summaryPanel.add(incomeLabel);
        summaryPanel.add(Box.createVerticalStrut(15));
        summaryPanel.add(expenditureLabel);
        summaryPanel.add(Box.createVerticalStrut(15));
        summaryPanel.add(savingsLabel);
        summaryPanel.add(Box.createVerticalGlue());

        contentPanel.add(summaryPanel, BorderLayout.WEST);

        // Chart Container Panel (Center) - Will hold CustomChartPanel
        chartContainerPanel = new JPanel(new BorderLayout());
        chartContainerPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        customChartPanel = new CustomChartPanel(); // Instantiate custom panel
        chartContainerPanel.add(customChartPanel, BorderLayout.CENTER); // Add custom panel
        contentPanel.add(chartContainerPanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        // --- Back Button (South) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        backButton = UIUtils.createStyledButton("Back to Home page", new Color(100, 100, 100), Color.LIGHT_GRAY, new Dimension(180, 35), new Font("Segoe UI", Font.PLAIN, 14));
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Action Listener ---
        backButton.addActionListener(e -> parentUI.showIndividualCenter());
    }

    // Method to load data and update the panel
    public void loadReportData() {
        String username = UserSession.getCurrentUsername();
        if (username == null) {
            JOptionPane.showMessageDialog(this, "User not logged in.", "Error", JOptionPane.ERROR_MESSAGE);
            clearDisplayOnError();
            return;
        }

        int cycleDays = parentUI.getReportCycleDays();
        List<TransactionRecord> transactions = readTransactionsForCycle(username, cycleDays);

        if (transactions == null) { // Check if reading failed
            clearDisplayOnError();
            return;
        }

        double totalIncome = 0;
        double totalExpenditure = 0;
        Map<Date, Double> dailyIncome = new TreeMap<>();
        Map<Date, Double> dailyExpenditure = new TreeMap<>();

        // Populate daily maps
        for (TransactionRecord tr : transactions) {
            if (isIncome(tr.type)) {
                totalIncome += tr.amount;
                Date dayOnly = truncateTime(tr.date);
                dailyIncome.put(dayOnly, dailyIncome.getOrDefault(dayOnly, 0.0) + tr.amount);
            } else if (isExpenditure(tr.type)) {
                double expenditureAmount = Math.abs(tr.amount);
                totalExpenditure += expenditureAmount;
                Date dayOnly = truncateTime(tr.date);
                dailyExpenditure.put(dayOnly, dailyExpenditure.getOrDefault(dayOnly, 0.0) + expenditureAmount);
            }
        }

        double savings = totalIncome - totalExpenditure;

        // Update summary labels
        incomeLabel.setText(String.format("Income: ¥%.2f", totalIncome));
        expenditureLabel.setText(String.format("Expenditure: ¥%.2f", totalExpenditure));
        savingsLabel.setText(String.format("Savings: ¥%.2f", savings));

        // --- Update Custom Chart Panel ---
        // Create complete daily maps for the cycle (including days with zero transactions)
        Map<Date, Double> fullCycleIncome = createFullCycleMap(dailyIncome, cycleDays);
        Map<Date, Double> fullCycleExpenditure = createFullCycleMap(dailyExpenditure, cycleDays);
        customChartPanel.setData(fullCycleIncome, fullCycleExpenditure); // Pass data to the chart panel
    }

    // Helper to create map including zero-value days for the full cycle
    private Map<Date, Double> createFullCycleMap(Map<Date, Double> dailyData, int cycleDays) {
        Map<Date, Double> fullMap = new TreeMap<>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -cycleDays + 1); // Start from the first day of the cycle
        Date startDate = truncateTime(cal.getTime());

        cal.setTime(startDate);
        for (int i = 0; i < cycleDays; i++) {
            Date currentDay = truncateTime(cal.getTime());
            fullMap.put(currentDay, dailyData.getOrDefault(currentDay, 0.0));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return fullMap;
    }


    private boolean isIncome(String type) {
        return "Deposit".equalsIgnoreCase(type) || "Transfer In".equalsIgnoreCase(type);
    }

    private boolean isExpenditure(String type) {
        return "Withdrawal".equalsIgnoreCase(type) || "Transfer Out".equalsIgnoreCase(type) || "Pay".equalsIgnoreCase(type);
    }

    private Date truncateTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private List<TransactionRecord> readTransactionsForCycle(String username, int cycleDays) {
        List<TransactionRecord> cycleTransactions = new ArrayList<>();
        String filePath = "transactions.csv";
        File file = new File(filePath);

        if (!file.exists()){
            JOptionPane.showMessageDialog(this, "transactions.csv not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -cycleDays + 1);
        Date startDate = truncateTime(cal.getTime());
        long cycleStartMillis = startDate.getTime();


        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            String header = br.readLine();
            if (header == null) {
                System.err.println("transactions.csv is empty.");
                return cycleTransactions;
            }

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length == 5 && parts[0].trim().equalsIgnoreCase(username)) {
                    try {
                        Date transactionDate = csvDateFormat.parse(parts[3].trim());
                        if (!transactionDate.before(new Date(cycleStartMillis))) {
                            double amount = Double.parseDouble(parts[2].trim());
                            String type = parts[1].trim();
                            cycleTransactions.add(new TransactionRecord(transactionDate, type, amount));
                        }
                    } catch (ParseException | NumberFormatException e) {
                        System.err.println("Skipping transaction due to parsing error: " + line + " - " + e.getMessage());
                    }
                } else if (parts.length != 5 && parts[0].trim().equalsIgnoreCase(username)){
                    System.err.println("Skipping transaction due to incorrect column count ("+parts.length+"): "+ line);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading transactions file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return null;
        }
        return cycleTransactions;
    }

    // Helper to clear display on error
    private void clearDisplayOnError() {
        incomeLabel.setText("Income: ¥ --");
        expenditureLabel.setText("Expenditure: ¥ --");
        savingsLabel.setText("Savings: ¥ --");
        customChartPanel.clearData(); // Add a clear method to CustomChartPanel
        // Optionally display an error message on the chart panel itself
        chartContainerPanel.removeAll();
        chartContainerPanel.add(new JLabel("Error loading data.", SwingConstants.CENTER), BorderLayout.CENTER);
        chartContainerPanel.revalidate();
        chartContainerPanel.repaint();
    }


    // Call this method when the panel becomes visible to load/refresh data
    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            SwingUtilities.invokeLater(this::loadReportData);
        }
    }

    // Simple inner class to hold transaction data
    private static class TransactionRecord {
        Date date;
        String type;
        double amount;

        TransactionRecord(Date date, String type, double amount) {
            this.date = date;
            this.type = type;
            this.amount = amount;
        }
    }

    // --- Inner class for Custom Chart Drawing ---
    private class CustomChartPanel extends JPanel {
        private Map<Date, Double> incomeData;
        private Map<Date, Double> expenditureData;
        private final int PADDING = 30; // Padding around the chart
        private final int LABEL_PADDING = 25; // Padding for axis labels
        private final Color INCOME_COLOR = new Color(34, 139, 34); // Forest Green
        private final Color EXPENDITURE_COLOR = new Color(220, 20, 60); // Crimson Red
        private final Color GRID_COLOR = Color.LIGHT_GRAY;

        public CustomChartPanel() {
            setBackground(Color.WHITE);
            this.incomeData = new TreeMap<>(); // Initialize empty maps
            this.expenditureData = new TreeMap<>();
        }

        public void setData(Map<Date, Double> incomeData, Map<Date, Double> expenditureData) {
            this.incomeData = (incomeData != null) ? new TreeMap<>(incomeData) : new TreeMap<>();
            this.expenditureData = (expenditureData != null) ? new TreeMap<>(expenditureData) : new TreeMap<>();
            repaint(); // Trigger repaint when data changes
        }

        public void clearData() {
            this.incomeData.clear();
            this.expenditureData.clear();
            repaint();
        }


        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (incomeData.isEmpty() && expenditureData.isEmpty()) {
                // Optional: Display message if no data
                g.drawString("No transaction data available for the selected period.", 50, getHeight() / 2);
                return;
            }

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Calculate chart dimensions
            int width = getWidth();
            int height = getHeight();
            int chartWidth = width - 2 * PADDING - LABEL_PADDING;
            int chartHeight = height - 2 * PADDING - LABEL_PADDING;

            // Get data range
            double maxAmount = 100.0; // Default max Y value if no data or only zero values
            if (!incomeData.isEmpty() || !expenditureData.isEmpty()){
                double maxIncome = incomeData.isEmpty() ? 0 : Collections.max(incomeData.values());
                double maxExpenditure = expenditureData.isEmpty() ? 0 : Collections.max(expenditureData.values());
                maxAmount = Math.max(maxIncome, maxExpenditure);
                if (maxAmount == 0) maxAmount = 100.0; // Avoid division by zero if all values are 0
            }


            List<Date> dates = new ArrayList<>(incomeData.keySet()); // Assuming income map covers all dates
            if(dates.isEmpty() && !expenditureData.isEmpty()){
                dates = new ArrayList<>(expenditureData.keySet()); // Use expenditure dates if income is empty
            }
            if (dates.isEmpty()) return; // Still no dates? Nothing to plot.

            long firstDateMillis = dates.get(0).getTime();
            long lastDateMillis = dates.get(dates.size() - 1).getTime();
            long dateRange = (lastDateMillis - firstDateMillis);
            if (dateRange == 0 && dates.size() == 1) { // Handle single date point case for range
                dateRange = TimeUnit.DAYS.toMillis(1); // Assume a one-day range for scaling
            } else if (dateRange <= 0) {
                dateRange = TimeUnit.DAYS.toMillis(dates.size() > 0 ? dates.size() : 1); // Prevent zero or negative range
            }


            // --- Draw Axes ---
            // Y-axis
            g2.drawLine(PADDING + LABEL_PADDING, height - PADDING - LABEL_PADDING, PADDING + LABEL_PADDING, PADDING);
            // X-axis
            g2.drawLine(PADDING + LABEL_PADDING, height - PADDING - LABEL_PADDING, width - PADDING, height - PADDING - LABEL_PADDING);

            // --- Draw Ticks and Labels ---
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(Color.BLACK);

            // Y-axis ticks and labels
            int numYTicks = 5; // Number of Y-axis ticks
            for (int i = 0; i <= numYTicks; i++) {
                int y = height - PADDING - LABEL_PADDING - (i * chartHeight / numYTicks);
                double value = maxAmount * i / numYTicks;
                String label = String.format("¥%.0f", value); // Format Y labels
                int labelWidth = fm.stringWidth(label);

                g2.setColor(GRID_COLOR); // Grid lines
                g2.drawLine(PADDING + LABEL_PADDING + 1, y, width - PADDING, y);
                g2.setColor(Color.BLACK); // Tick marks and labels
                g2.drawLine(PADDING + LABEL_PADDING - 5, y, PADDING + LABEL_PADDING, y); // Tick mark
                g2.drawString(label, PADDING + LABEL_PADDING - labelWidth - 8, y + fm.getAscent() / 2);
            }

            // X-axis ticks and labels (simplified - maybe label every few days)
            int numXTicks = Math.min(dates.size() - 1, 6); // Max 6 date labels + start date
            for (int i = 0; i < dates.size(); i++) {
                // Only draw labels for a few selected dates to avoid clutter
                boolean drawLabel = (dates.size() <= 7) || (i == 0) || (i == dates.size() - 1) || (i % ((dates.size() / numXTicks) +1) == 0) ;

                long currentDateMillis = dates.get(i).getTime();
                int x = PADDING + LABEL_PADDING + (int) (((currentDateMillis - firstDateMillis) * (double)chartWidth) / dateRange) ;
                if (x < PADDING + LABEL_PADDING) x = PADDING + LABEL_PADDING; // Clamp to start
                if (x > width - PADDING) x = width - PADDING;           // Clamp to end


                g2.setColor(GRID_COLOR); // Vertical grid lines (optional)
                g2.drawLine(x, height - PADDING - LABEL_PADDING -1 , x, PADDING);
                g2.setColor(Color.BLACK); // Tick marks and labels

                g2.drawLine(x, height - PADDING - LABEL_PADDING + 5, x, height - PADDING - LABEL_PADDING); // Tick mark

                if (drawLabel) {
                    String dateLabel = displayDateFormat.format(dates.get(i));
                    int labelWidth = fm.stringWidth(dateLabel);
                    g2.drawString(dateLabel, x - labelWidth / 2, height - PADDING - LABEL_PADDING + fm.getAscent() + 5);
                }
            }


            // --- Draw Data Lines ---
            Stroke oldStroke = g2.getStroke();
            g2.setStroke(new BasicStroke(2f)); // Thicker line

            // Draw Income line
            g2.setColor(INCOME_COLOR);
            Point prevPointIncome = null;
            for (Date date : dates) {
                double value = incomeData.getOrDefault(date, 0.0);
                int x = PADDING + LABEL_PADDING + (int) (((date.getTime() - firstDateMillis) * (double)chartWidth) / dateRange);
                int y = height - PADDING - LABEL_PADDING - (int) ((value / maxAmount) * chartHeight);
                if (x < PADDING + LABEL_PADDING) x = PADDING + LABEL_PADDING;
                if (x > width - PADDING) x = width - PADDING;
                if (y < PADDING) y = PADDING;
                if (y > height - PADDING - LABEL_PADDING) y = height - PADDING - LABEL_PADDING;


                Point currentPoint = new Point(x, y);
                g2.fillOval(x - 3, y - 3, 6, 6); // Draw point marker
                if (prevPointIncome != null) {
                    g2.drawLine(prevPointIncome.x, prevPointIncome.y, currentPoint.x, currentPoint.y);
                }
                prevPointIncome = currentPoint;
            }

            // Draw Expenditure line
            g2.setColor(EXPENDITURE_COLOR);
            Point prevPointExpenditure = null;
            for (Date date : dates) {
                double value = expenditureData.getOrDefault(date, 0.0);
                int x = PADDING + LABEL_PADDING + (int) (((date.getTime() - firstDateMillis) * (double)chartWidth) / dateRange);
                int y = height - PADDING - LABEL_PADDING - (int) ((value / maxAmount) * chartHeight);
                if (x < PADDING + LABEL_PADDING) x = PADDING + LABEL_PADDING;
                if (x > width - PADDING) x = width - PADDING;
                if (y < PADDING) y = PADDING;
                if (y > height - PADDING - LABEL_PADDING) y = height - PADDING - LABEL_PADDING;

                Point currentPoint = new Point(x, y);
                g2.fillOval(x - 3, y - 3, 6, 6); // Draw point marker
                if (prevPointExpenditure != null) {
                    g2.drawLine(prevPointExpenditure.x, prevPointExpenditure.y, currentPoint.x, currentPoint.y);
                }
                prevPointExpenditure = currentPoint;
            }

            g2.setStroke(oldStroke); // Restore original stroke

            // --- Draw Legend (Simple) ---
            int legendX = width - PADDING - 100;
            int legendY = PADDING + 10;
            g2.setColor(INCOME_COLOR);
            g2.fillRect(legendX, legendY, 15, 10);
            g2.setColor(Color.BLACK);
            g2.drawString("Income", legendX + 20, legendY + fm.getAscent());

            legendY += 20;
            g2.setColor(EXPENDITURE_COLOR);
            g2.fillRect(legendX, legendY, 15, 10);
            g2.setColor(Color.BLACK);
            g2.drawString("Expenditure", legendX + 20, legendY + fm.getAscent());
        }
    } // --- End of CustomChartPanel inner class ---

} // --- End of ReportViewPanel class ---