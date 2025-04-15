package Person;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class IncomeExpenseChart {

    // Transaction 类，表示每一笔交易数据
    public static class Transaction {
        private String username;    // 用户名
        private String category;    // 分类：存款、提款、转账等
        private double amount;      // 金额
        private String target;      // 操作对象
        private String time;        // 时间

        public Transaction(String username, String category, double amount, String target, String time) {
            this.username = username;
            this.category = category;
            this.amount = amount;
            this.target = target;
            this.time = time;
        }

        public String getUsername() { return username; }
        public String getCategory() { return category; }
        public double getAmount() { return amount; }
        public String getTarget() { return target; }
        public String getTime() { return time; }
    }

    // 从 CSV 文件读取账单数据
    public static List<Transaction> readBillData(String filePath) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            reader.readLine(); // 跳过标题行
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                String username = data[0].trim();
                String category = data[1].trim();
                double amount = Double.parseDouble(data[2].trim());
                String target = data[3].trim();
                String time = data[4].trim();
                transactions.add(new Transaction(username, category, amount, target, time));
            }
        }
        return transactions;
    }

    // 根据分类汇总金额
    public static Map<String, Double> calculateCategoryTotals(List<Transaction> transactions) {
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Transaction transaction : transactions) {
            String category = transaction.getCategory();
            double amount = transaction.getAmount();
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
        }
        return categoryTotals;
    }

    // 静态方法：从 CSV 文件读取数据并显示饼状图
    public static void showIncomeExpensePieChart(String filePath) {
        try {
            List<Transaction> transactions = readBillData(filePath);
            Map<String, Double> categoryTotals = calculateCategoryTotals(transactions);

            // 创建一个饼状图（简单的绘制）
            JFrame frame = new JFrame("收入支出分类饼状图");
            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    int startAngle = 0;
double totalAmount = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();
int i = 0;
Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN};

int size = 400; // 直径
int centerX = getWidth() / 2;
int centerY = getHeight() / 2;
int radius = size / 2;
int labelRadius = radius + 20; // 文字稍微离饼状图外一些

for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
    int arcAngle = (int) Math.round((entry.getValue() / totalAmount) * 360);
    g.setColor(colors[i % colors.length]);
    g.fillArc(centerX - radius, centerY - radius, size, size, startAngle, arcAngle);

    // 计算文字位置
    int labelAngle = startAngle + arcAngle / 2;
    int labelX = (int) (centerX + Math.cos(Math.toRadians(labelAngle)) * labelRadius);
    int labelY = (int) (centerY - Math.sin(Math.toRadians(labelAngle)) * labelRadius);

    // 绘制类别和金额
    g.setColor(Color.BLACK);
    g.drawString(entry.getKey() + ": " + String.format("%.2f", entry.getValue()), labelX, labelY);

    startAngle += arcAngle;
    i++;
}

                
       
                }
            };
            // 调整窗口大小和居中
frame.setSize(600, 600); // 
frame.setLocationRelativeTo(null); // 居中窗口
frame.add(panel);
frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
frame.setVisible(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Main 方法可选：用于测试显示饼状图
    public static void main(String[] args) {
        showIncomeExpensePieChart("tractions.csv"); // 假设 CSV 文件路径是 "tractions.csv"
    }
}
