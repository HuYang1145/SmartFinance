package PersonController;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import AccountModel.UserSessionModel;
import View.IncomeExpenseChartView;

/**
 * Controller class for coordinating income and expense chart displays.
 */
public class ChartController {

    /**
     * Returns a JPanel containing multiple charts and tables for income and expense analysis.
     *
     * @param username   The username to fetch transactions for.
     * @param yearMonth  The year and month to filter transactions (format: yyyy/MM).
     * @return A JPanel with expense/income charts, transaction table, and type analysis.
     */
    public static JPanel getIncomeExpensePlane(String username, String yearMonth) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;

        // Plate 1: Expense Donut Chart
        JPanel expensePanel = new JPanel(new BorderLayout(10, 10));
        expensePanel.setBackground(Color.WHITE);
        JLabel expenseTitle = new JLabel("Expense Categories", SwingConstants.CENTER);
        expenseTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        expensePanel.add(expenseTitle, BorderLayout.NORTH);
        JPanel expenseChartPanel = IncomeExpenseChartView.getExpenseChartPanel(username, yearMonth);
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
        JPanel incomeChartPanel = IncomeExpenseChartView.getIncomeChartPanel(username, yearMonth);
        incomePanel.add(incomeChartPanel, BorderLayout.CENTER);
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(incomePanel, gbc);

        // Plate 3 & 4: Transaction Table
        JPanel tablePanel = IncomeExpenseChartView.getTransactionTablePanel(username, yearMonth);
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
        typeAnalysisPanel.add(IncomeExpenseChartView.getTypeAnalysisPanel(username, yearMonth), BorderLayout.CENTER);
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
        barChartPanel.add(IncomeExpenseChartView.getDailyLineChartPanel(username, yearMonth), BorderLayout.CENTER);
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
     * Creates and displays a donut chart for expense categories in a new window (legacy method).
     *
     * @param filePath_no_longer_needed Deprecated parameter, no longer used.
     * @deprecated Use getIncomeExpensePlane instead.
     */
    @Deprecated
    public static void showIncomeExpensePieChart(String filePath_no_longer_needed) {
        String currentUsername = UserSessionModel.getCurrentUsername();
        if (currentUsername == null) {
            JOptionPane.showMessageDialog(null, "Please log in first!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Calendar cal = Calendar.getInstance();
            String currentMonth = new SimpleDateFormat("yyyy/MM").format(cal.getTime());
            JPanel panel = IncomeExpenseChartView.getExpenseChartPanel(currentUsername, currentMonth);

            JFrame frame = new JFrame("Personal Expense Category Donut Chart");
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