package View;

import PersonController.ChartController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;

/**
 * A control panel for selecting year and month to display income and expense charts.
 */
public class ChartControlPanel extends JPanel {
    private JComboBox<Integer> yearComboBox;
    private JComboBox<String> monthComboBox;
    private String username;

    /**
     * Constructs a control panel with year and month selection for chart display.
     *
     * @param username The username to fetch transactions for.
     */
    public ChartControlPanel(String username) {
        this.username = username;
        setBackground(Color.WHITE);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Initialize year combo box
        JLabel yearLabel = new JLabel("Year:", SwingConstants.RIGHT);
        yearLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        yearComboBox = new JComboBox<>();
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        for (int i = currentYear - 5; i <= currentYear + 5; i++) {
            yearComboBox.addItem(i);
        }
        yearComboBox.setSelectedItem(2025); // Default to 2025 where data exists
        yearComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Initialize month combo box
        JLabel monthLabel = new JLabel("Month:", SwingConstants.RIGHT);
        monthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        String[] months = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
        monthComboBox = new JComboBox<>(months);
        monthComboBox.setSelectedItem("01"); // Default to January where data exists
        monthComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Add components to panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        add(yearLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.8;
        add(yearComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.2;
        add(monthLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.8;
        add(monthComboBox, gbc);

        // Add listeners
        ActionListener updateListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateChart();
            }
        };
        yearComboBox.addActionListener(updateListener);
        monthComboBox.addActionListener(updateListener);
    }

    /**
     * Sets the chart panel (not used directly, triggers initial update).
     *
     * @param chartPanel The chart panel to set (ignored, as update is handled directly).
     */
    public void setChartPanel(JPanel chartPanel) {
        updateChart();
    }

    /**
     * Updates the chart display based on selected year and month.
     */
    private void updateChart() {
        if (username != null) {
            String yearMonth = yearComboBox.getSelectedItem() + "/" + monthComboBox.getSelectedItem();
            JPanel newChartPanel = ChartController.getIncomeExpensePlane(username, yearMonth);
            JFrame frame = (JFrame) getTopLevelAncestor();
            if (frame != null) {
                frame.getContentPane().removeAll();
                frame.add(this, BorderLayout.NORTH);
                frame.add(newChartPanel, BorderLayout.CENTER);
                frame.revalidate();
                frame.repaint();
            }
        }
    }

    /**
     * Displays the chart with control panel in a new window.
     *
     * @param username The username to fetch transactions for.
     */
    public static void showChartWithControls(String username) {
        JFrame frame = new JFrame("Income and Expense Dashboard");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        ChartControlPanel controlPanel = new ChartControlPanel(username);
        frame.add(controlPanel, BorderLayout.NORTH);

        String initialYearMonth = "2025/01"; // Default to a period with data
        JPanel chartPanel = ChartController.getIncomeExpensePlane(username, initialYearMonth);
        frame.add(chartPanel, BorderLayout.CENTER);

        controlPanel.setChartPanel(chartPanel);

        frame.setMinimumSize(new Dimension(1200, 800));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}