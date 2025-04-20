package src.Person;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import src.Model.UserSession;

public class IncomeExpenseChart {

    // Transaction 类，表示每一笔交易数据
    public static class Transaction {
        private String username;         // 用户名
        private String category;         // 操作类型：存款、提款、转账等 (对应 "operation performed")
        private double amount;           // 金额
        private String paymentTime;      // 支付时间 (对应 "payment time")
        private String merchantName;     // 商户名称 (对应 "merchant name")

        public Transaction(String username, String category, double amount, String paymentTime, String merchantName) {
            this.username = username;
            this.category = category;
            this.amount = amount;
            this.paymentTime = paymentTime;
            this.merchantName = merchantName;
        }

        public String getUsername() { return username; }
        public String getCategory() { return category; }
        public double getAmount() { return amount; }
        public String getPaymentTime() { return paymentTime; }
        public String getMerchantName() { return merchantName; }
    }

    // 从 CSV 文件读取指定用户的账单数据
    public static List<Transaction> readBillData(String filePath, String currentUsername) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            reader.readLine(); // 跳过标题行
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 5) { // 确保数据完整
                    String username = data[0].trim();
                    String operationPerformed = data[1].trim();
                    double amount = Double.parseDouble(data[2].trim());
                    String paymentTime = data[3].trim();
                    String merchantName = data[4].trim();

                    // 只添加当前用户的交易记录
                    if (username.equals(currentUsername)) {
                        transactions.add(new Transaction(username, operationPerformed, amount, paymentTime, merchantName));
                    }
                } else {
                    System.err.println("Skipping invalid line: " + line);
                }
            }
        }
        return transactions;
    }

    // 根据操作类型汇总金额 (修改为使用 "operation performed" 作为分类)
    public static Map<String, Double> calculateCategoryTotals(List<Transaction> transactions) {
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Transaction transaction : transactions) {
            String category = transaction.getCategory(); // 使用 "operation performed"
            double amount = transaction.getAmount();
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
        }
        return categoryTotals;
    }

    // 静态方法：从 CSV 文件读取指定用户数据并显示饼状图
    public static void showIncomeExpensePieChart(String filePath) {
        String currentUsername = UserSession.getCurrentUsername(); // 获取当前用户名
        if (currentUsername == null) {
            JOptionPane.showMessageDialog(null, "请先登录！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            List<Transaction> transactions = readBillData(filePath, currentUsername); // 传递当前用户名
            Map<String, Double> categoryTotals = calculateCategoryTotals(transactions);

            // 创建一个饼状图（简单的绘制）
            JFrame frame = new JFrame("个人收入支出分类饼状图");
            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    int startAngle = 0;
                    double totalAmount = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();
                    int i = 0;
                    Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.PINK};
                    FontMetrics fm = g.getFontMetrics();

                    int size = 400; // 直径
                    int centerX = getWidth() / 2;
                    int centerY = getHeight() / 2;
                    int radius = size / 2;
                    int outerRadius = radius + 30; // 标签引线的起始位置

                    // 用于记录已经使用的 y 坐标，避免标签垂直方向重叠
                    java.util.List<Rectangle> labelBounds = new ArrayList<>();

                    for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                        int arcAngle = (int) Math.round((entry.getValue() / totalAmount) * 360);
                        g.setColor(colors[i % colors.length]);
                        g.fillArc(centerX - radius, centerY - radius, size, size, startAngle, arcAngle);

                        // 计算弧度的中间角度
                        double midAngle = Math.toRadians(startAngle + arcAngle / 2.0);

                        // 计算引线起始点
                        int x1 = (int) (centerX + Math.cos(midAngle) * radius);
                        int y1 = (int) (centerY - Math.sin(midAngle) * radius);

                        // 计算引线结束点 (稍微向外延伸)
                        int x2 = (int) (centerX + Math.cos(midAngle) * outerRadius);
                        int y2 = (int) (centerY - Math.sin(midAngle) * outerRadius);

                        g.setColor(Color.BLACK);
                        g.drawLine(x1, y1, x2, y2);

                        String labelText = entry.getKey() + ": " + String.format("%.2f", entry.getValue());
                        int labelWidth = fm.stringWidth(labelText);
                        int labelHeight = fm.getHeight();

                        // 根据角度判断标签应该放在饼图的左边还是右边
                        int labelX, labelY;
                        if (Math.cos(midAngle) > 0) { // 右边
                            labelX = x2 + 5;
                            labelY = y2 - labelHeight / 2;
                        } else { // 左边
                            labelX = x2 - 5 - labelWidth;
                            labelY = y2 - labelHeight / 2;
                        }

                        Rectangle currentLabelBounds = new Rectangle(labelX, labelY, labelWidth, labelHeight);

                        // 检查是否与之前的标签重叠，如果重叠则稍微调整 y 坐标 (简单的避免重叠方法)
                        boolean overlap = false;
                        int yOffset = 0;
                        for (Rectangle existingBound : labelBounds) {
                            if (currentLabelBounds.intersects(existingBound)) {
                                overlap = true;
                                yOffset += labelHeight + 2; // 增加偏移量
                            }
                        }

                        if (overlap) {
                            labelY += yOffset;
                            currentLabelBounds.y = labelY;
                        }

                        g.drawString(labelText, labelX, labelY + fm.getAscent()); // 绘制文本

                        labelBounds.add(currentLabelBounds); // 记录当前标签的位置

                        startAngle += arcAngle;
                        i++;
                    }
                }
            };
            // 调整窗口大小和居中
            frame.setSize(600, 600);
            frame.setLocationRelativeTo(null);
            frame.add(panel);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 修改为关闭窗口而不是退出程序
            frame.setVisible(true);

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "读取账单数据失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}