package Person;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

import model.UserSession; // 导入 UserSession 类

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

    /**
     * 从 CSV 文件读取指定用户的账单数据
     * @param filePath CSV 文件路径
     * @param currentUsername 要筛选的当前用户名
     * @return 包含指定用户交易记录的列表
     * @throws IOException 读取文件发生错误时抛出
     */
    public static List<Transaction> readBillData(String filePath, String currentUsername) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        // 使用 try-with-resources 确保 reader 被正确关闭
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            reader.readLine(); // 跳过标题行 (假设第一行是标题)
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(","); // 使用逗号分割
                // 检查数据列数是否符合预期 (5列)
                if (data.length == 5) {
                    try {
                        String username = data[0].trim();
                        String operationPerformed = data[1].trim();
                        // 尝试解析金额，如果失败则跳过该行
                        double amount = Double.parseDouble(data[2].trim());
                        String paymentTime = data[3].trim();
                        String merchantName = data[4].trim();

                        // 只添加属于当前登录用户的交易记录
                        if (username.equals(currentUsername)) {
                            transactions.add(new Transaction(username, operationPerformed, amount, paymentTime, merchantName));
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping line due to invalid amount format: " + line);
                    } catch (Exception e) {
                        // 捕获其他潜在的解析错误
                        System.err.println("Skipping line due to unexpected error: " + line + " - Error: " + e.getMessage());
                    }
                } else {
                    // 如果列数不为5，则打印错误信息并跳过该行
                    System.err.println("Skipping invalid line (incorrect number of columns): " + line);
                }
            }
        }
        return transactions;
    }

    /**
     * 根据操作类型汇总金额
     * @param transactions 交易列表
     * @return 一个Map，键是操作类型 (category)，值是该类型的总金额
     */
    public static Map<String, Double> calculateCategoryTotals(List<Transaction> transactions) {
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Transaction transaction : transactions) {
            String category = transaction.getCategory(); // 使用 "operation performed" 作为分类依据
            double amount = transaction.getAmount();
            // 将金额累加到对应的分类中
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
        }
        return categoryTotals;
    }

    /**
     * 显示指定用户收支数据的饼状图
     * @param csvFilePath transactions.csv 文件的路径
     */
    public static void showIncomeExpensePieChart(String csvFilePath) {
        String currentUsername = UserSession.getCurrentUsername(); // 获取当前登录的用户名
        // 检查用户是否已登录
        if (currentUsername == null) {
            JOptionPane.showMessageDialog(null, "请先登录！", "错误", JOptionPane.ERROR_MESSAGE);
            return; // 未登录则不继续执行
        }

        try {
            // 读取指定用户的交易数据
            List<Transaction> transactions = readBillData(csvFilePath, currentUsername);
            // 如果没有交易数据，提示用户并退出
            if (transactions.isEmpty()) {
                JOptionPane.showMessageDialog(null, "未找到用户 '" + currentUsername + "' 的交易记录。", "信息", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // 计算各分类的总金额
            Map<String, Double> categoryTotals = calculateCategoryTotals(transactions);
            // 如果计算结果为空(理论上不会，除非 transactions 为空)，也退出
            if (categoryTotals.isEmpty()) {
                System.err.println("No category totals calculated for user: " + currentUsername);
                return;
            }


            // --- 创建并显示饼状图 ---
            JFrame frame = new JFrame(currentUsername + " 的收支分类饼状图"); // 窗口标题包含用户名
            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g; // 使用 Graphics2D 以获得更好的绘图控制（如抗锯齿）
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // 开启抗锯齿

                    int startAngle = 0;
                    // 计算所有分类的总金额
                    double totalAmount = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();
                    // 如果总金额为0或负数（异常情况），则不绘制
                    if (totalAmount <= 0) {
                        g2d.drawString("无有效数据或总金额为零", getWidth()/2 - 50, getHeight()/2);
                        return;
                    }

                    int i = 0;
                    // 定义一组颜色用于区分不同的扇区
                    Color[] colors = {
                            new Color(255, 99, 132), new Color(54, 162, 235), new Color(255, 206, 86),
                            new Color(75, 192, 192), new Color(153, 102, 255), new Color(255, 159, 64),
                            new Color(201, 203, 207), new Color(100, 181, 246), new Color(255, 182, 193)
                    };
                    FontMetrics fm = g2d.getFontMetrics();

                    // 饼图绘制区域参数
                    int padding = 50; // 图表距离面板边缘的距离
                    int availableWidth = getWidth() - 2 * padding;
                    int availableHeight = getHeight() - 2 * padding;
                    int diameter = Math.min(availableWidth, availableHeight); // 饼图直径取可用空间的较小值
                    int centerX = getWidth() / 2;
                    int centerY = getHeight() / 2;
                    int radius = diameter / 2;
                    int labelLineStartRadius = radius + 5; // 引线起始点稍微离开饼图
                    int labelLineEndRadius = radius + 20; // 引线结束点

                    // 用于存储标签及其位置，以便后续调整避免重叠
                    java.util.List<LabelInfo> labels = new ArrayList<>();

                    // 绘制每个扇区和标签引线
                    for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                        // 跳过金额为0或负数的分类
                        if (entry.getValue() <= 0) {
                            continue;
                        }
                        // 计算扇形角度
                        int arcAngle = (int) Math.round((entry.getValue() / totalAmount) * 360.0);
                        // 如果角度小于1度，至少画1度，避免看不见
                        if (arcAngle == 0 && entry.getValue() > 0) {
                            arcAngle = 1;
                        }

                        g2d.setColor(colors[i % colors.length]); // 设置颜色
                        // 绘制扇形
                        g2d.fillArc(centerX - radius, centerY - radius, diameter, diameter, startAngle, arcAngle);

                        // --- 计算标签和引线位置 ---
                        double midAngleRad = Math.toRadians(-(startAngle + arcAngle / 2.0)); // 角度转弧度，Y轴向下为正，所以取负

                        // 引线起始点 (饼图边缘外侧一点)
                        int x1 = (int) (centerX + Math.cos(midAngleRad) * labelLineStartRadius);
                        int y1 = (int) (centerY + Math.sin(midAngleRad) * labelLineStartRadius);

                        // 引线结束点 (更外侧一点)
                        int x2 = (int) (centerX + Math.cos(midAngleRad) * labelLineEndRadius);
                        int y2 = (int) (centerY + Math.sin(midAngleRad) * labelLineEndRadius);

                        // 绘制引线
                        g2d.setColor(Color.DARK_GRAY);
                        g2d.drawLine(x1, y1, x2, y2);

                        // 准备标签文本
                        String labelText = entry.getKey() + ": " + String.format("%.2f", entry.getValue());
                        int labelWidth = fm.stringWidth(labelText);
                        int labelHeight = fm.getAscent(); // 使用 ascent 作为基线高度

                        // 根据引线结束点和角度确定标签的初始位置
                        int labelX, labelY;
                        int horizontalOffset = 5; // 标签与引线末端的水平距离
                        if (Math.cos(midAngleRad) >= 0) { // 标签在右侧
                            labelX = x2 + horizontalOffset;
                        } else { // 标签在左侧
                            labelX = x2 - horizontalOffset - labelWidth;
                        }
                        labelY = y2 + labelHeight / 2; // 初始垂直位置对准引线末端

                        // 将标签信息存起来，稍后统一绘制并处理重叠
                        labels.add(new LabelInfo(labelText, labelX, labelY, labelWidth, labelHeight, midAngleRad));

                        startAngle += arcAngle; // 更新下一个扇区的起始角度
                        i++; // 更新颜色索引
                    }

                    // --- 绘制标签并处理重叠 ---
                    adjustLabelPositions(labels, centerY, getHeight()); // 调用方法调整标签位置
                    g2d.setColor(Color.BLACK);
                    for(LabelInfo label : labels) {
                        g2d.drawString(label.text, label.x, label.y); // 绘制调整后的标签
                    }
                }
            };
            // 设置窗口大小和居中
            frame.setSize(700, 700); // 可以适当调整窗口大小以容纳标签
            frame.setLocationRelativeTo(null); // 窗口居中显示
            frame.add(panel); // 将包含饼图的面板添加到窗口
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 设置关闭操作为销毁窗口，不退出整个程序
            frame.setVisible(true); // 显示窗口

        } catch (IOException e) {
            // 处理文件读取错误
            e.printStackTrace(); // 打印错误信息到控制台
            JOptionPane.showMessageDialog(null, "读取账单数据失败: " + e.getMessage(), "文件读取错误", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            // 处理其他潜在错误
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "显示图表时发生未知错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 内部类，用于存储标签信息
    private static class LabelInfo {
        String text;
        int x, y, width, height;
        double angleRad; // 标签对应的角度

        LabelInfo(String text, int x, int y, int width, int height, double angleRad) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.angleRad = angleRad;
        }
    }

    // 简单的标签重叠避免逻辑 (可能需要更复杂的算法来完美处理)
    private static void adjustLabelPositions(List<LabelInfo> labels, int centerY, int panelHeight) {
        if (labels.isEmpty()) return;

        // 按垂直位置排序，便于检查相邻重叠
        labels.sort(Comparator.comparingInt(l -> l.y));

        int minGap = 2; // 标签之间的最小垂直间距
        boolean changed;
        int maxIterations = labels.size() * 2; // 防止无限循环
        int iterations = 0;

        do {
            changed = false;
            iterations++;
            for (int i = 0; i < labels.size() - 1; i++) {
                LabelInfo current = labels.get(i);
                LabelInfo next = labels.get(i + 1);

                // 检查垂直重叠
                if (current.y + current.height + minGap > next.y) {
                    // 如果重叠，将下面的标签向下推
                    int adjustment = (current.y + current.height + minGap) - next.y;
                    next.y += adjustment;
                    changed = true;

                    // 确保标签不会移出面板底部 (简单处理)
                    if (next.y + next.height > panelHeight - 10) {
                        next.y = panelHeight - 10 - next.height;
                        // 如果调整后第一个标签也需要移动
                        if (current.y + current.height + minGap > next.y){
                            current.y -= adjustment/2; // 尝试向上移动一点
                        }
                    }
                }
            }
            // 可以再从下往上检查一遍，帮助分布更均匀
            for (int i = labels.size() - 1; i > 0; i--) {
                LabelInfo current = labels.get(i);
                LabelInfo previous = labels.get(i - 1);
                if (previous.y + previous.height + minGap > current.y) {
                    int adjustment = (previous.y + previous.height + minGap) - current.y;
                    previous.y -= adjustment; // 将上面的标签向上推
                    changed = true;
                    // 确保标签不会移出顶部
                    if (previous.y < 10) {
                        previous.y = 10;
                        if (previous.y + previous.height + minGap > current.y){
                            current.y += adjustment/2; // 尝试向下移动一点
                        }
                    }
                }
            }

        } while (changed && iterations < maxIterations); // 如果发生改变则重复，但设置最大迭代次数
    }

    // --- 可选的 main 方法用于独立测试 ---
    /*
    public static void main(String[] args) {
        // !!! 仅用于测试，实际使用时应通过主程序调用 showIncomeExpensePieChart !!!

        // 模拟用户登录
        UserSession.login("testuser"); // 假设存在一个名为 "testuser" 的用户

        // 指定 CSV 文件路径 (请根据你的实际文件位置修改)
        String csvPath = "transactions.csv";

        // 确保测试用的 transactions.csv 文件存在且包含 "testuser" 的数据
        File testCsv = new File(csvPath);
        if (!testCsv.exists()) {
             System.err.println("测试错误: " + csvPath + " 文件不存在!");
             // 可以选择创建一个简单的测试文件
             try (FileWriter fw = new FileWriter(csvPath)) {
                 fw.write("Username,OperationPerformed,Amount,PaymentTime,MerchantName\n");
                 fw.write("testuser,Deposit,1000.0,2025/04/17 10:00,ATM\n");
                 fw.write("otheruser,Withdrawal,200.0,2025/04/17 11:00,ATM\n");
                 fw.write("testuser,Payment,50.5,2025/04/17 12:30,CoffeeShop\n");
                 fw.write("testuser,TransferOut,150.0,2025/04/17 14:00,FriendA\n");
                 fw.write("testuser,Deposit,300.0,2025/04/18 09:00,Salary\n");
                 System.out.println("创建了简单的测试文件: " + csvPath);
             } catch (IOException e) {
                 System.err.println("创建测试文件失败: " + e.getMessage());
                 return;
             }
        }


        // 使用 SwingUtilities.invokeLater 确保 GUI 操作在事件调度线程中执行
        SwingUtilities.invokeLater(() -> {
            showIncomeExpensePieChart(csvPath);
        });

        // 注意：这个main方法执行后程序可能不会立即退出，因为 Swing 窗口是活动的。
        // 需要手动关闭弹出的饼状图窗口。
    }
    */

    // --- 确保 UserSession 类存在 (或者提供一个简单的模拟版本用于测试) ---
    /*
    // 这是一个非常简单的 UserSession 模拟类，仅用于让 IncomeExpenseChart 能编译通过
    // 你应该使用你项目中实际的 UserSession 类
    static class UserSession {
        private static String currentUsername = null;

        public static void login(String username) {
            currentUsername = username;
        }

        public static void logout() {
            currentUsername = null;
        }

        public static String getCurrentUsername() {
            return currentUsername;
        }
    }
    */
}