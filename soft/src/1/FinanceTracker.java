import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class FinanceTracker {
    
    public static class Transaction {
        private LocalDate date;
        private double amount;
        private String category;

        public Transaction(LocalDate date, double amount, String category) {
            this.date = date;
            this.amount = amount;
            this.category = category;
        }

        public LocalDate getDate() {
            return date;
        }

        public double getAmount() {
            return amount;
        }

        public String getCategory() {
            return category;
        }
    }

    public static void main(String[] args) {
        
        Map<Integer, Map<String, Double>> monthlyCategoryExpenses = new HashMap<>();
        monthlyCategoryExpenses.put(1, new HashMap<>() {{
            put("Food", 100.0);
            put("Transport", 50.0);
            put("Entertainment", 30.0);
        }});
        
        Map<String, Double> categoryExpenses = monthlyCategoryExpenses.get(1);

        
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue()); 
        }

        JFreeChart pieChart = ChartFactory.createPieChart(
                "month rate",  // 
                dataset,        // 
                true,           // 
                true,           // 
                false           
        );

        // 
        ChartPanel panel = new ChartPanel(pieChart);
        JFrame frame = new JFrame("消费分类占比");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}
