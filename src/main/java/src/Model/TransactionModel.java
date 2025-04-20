package src.Model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

// 实现 Serializable 接口，如果需要通过网络传输或保存到文件
public class TransactionModel implements Serializable {
    // 建议为 Serializable 类添加 serialVersionUID
    private static final long serialVersionUID = 1L;

    private String transactionId; // 交易的唯一ID
    private String accountUsername; // 这笔交易所属账户的用户名 (关联到 AccountModel)
    private String type; // 交易类型，例如："deposit"（存款）, "withdrawal"（取款）, "transfer"（转账）
    private double amount; // 涉及的金额
    private String timestamp; // 交易发生的时间戳 (例如："yyyy-MM-dd HH:mm:ss" 格式)
    private String description; // 可选的交易描述 (例如："工资", "转给张三")
    private String relatedAccountUsername; // 对于转账交易，对方账户的用户名

    // 构造函数 (用于创建新的交易)
    public TransactionModel(String accountUsername, String type, double amount, String description, String relatedAccountUsername) {
        this.transactionId = generateTransactionId(); // 生成唯一ID
        this.accountUsername = accountUsername;
        this.type = type;
        this.amount = amount;
        // 自动设置当前时间为时间戳
        this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.description = description;
        // 如果不是转账，relatedAccountUsername 可以为 null
        this.relatedAccountUsername = relatedAccountUsername;
    }

    // 另一个构造函数 (用于从存储中加载已有ID和时间戳的交易)
    public TransactionModel(String transactionId, String accountUsername, String type, double amount, String timestamp, String description, String relatedAccountUsername) {
        this.transactionId = transactionId;
        this.accountUsername = accountUsername;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.description = description;
        this.relatedAccountUsername = relatedAccountUsername;
    }

    // --- Getters (获取器方法) ---
    // 提供外部访问这些私有字段的方法
    public String getTransactionId() {
        return transactionId;
    }

    public String getAccountUsername() {
        return accountUsername;
    }

    public String getType() {
        // 这是 TransactionChecker 需要的方法
        return type;
    }

    public double getAmount() {
        // 这是 TransactionChecker 需要的方法
        return amount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    public String getRelatedAccountUsername() {
        return relatedAccountUsername;
    }

    // --- 私有辅助方法 ---
    // 生成一个简单的唯一交易ID (实际应用中可能需要更复杂的ID生成策略)
    private String generateTransactionId() {
        // 简单示例：当前时间毫秒数 + 随机数
        return System.currentTimeMillis() + "_" + ((int)(Math.random() * 1000));
    }

    // --- equals, hashCode, toString (可选但推荐) ---
    // 重写 equals 和 hashCode 通常基于唯一标识符 (transactionId)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionModel that = (TransactionModel) o;
        return Objects.equals(transactionId, that.transactionId); // ID 相同则认为对象相同
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId); // 基于 ID 计算哈希值
    }

    // toString 方法方便调试时打印对象信息
    @Override
    public String toString() {
        return "TransactionModel{" +
                "transactionId='" + transactionId + '\'' +
                ", accountUsername='" + accountUsername + '\'' +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                ", timestamp='" + timestamp + '\'' +
                ", description='" + description + '\'' +
                ", relatedAccountUsername='" + relatedAccountUsername + '\'' +
                '}';
    }

    // --- CSV 转换 (示例 - 假设交易信息存储在单独的CSV文件中) ---

    /**
     * 将交易数据转换为 CSV 格式的一行。
     * 注意：如果 description 可能包含逗号，需要特殊处理（例如使用引号包裹并转义内部引号）。
     * @return CSV 格式的字符串
     */
    public String toCSV() {
        // 示例：使用逗号分隔，需要注意 description 中的逗号和引号处理
        // 更安全的方式可能是使用不易冲突的分隔符，如 | 或制表符 \t
        return String.join(",",
                getTransactionId(),
                getAccountUsername(),
                getType(),
                String.valueOf(getAmount()), // 将 double 转为 String
                getTimestamp(),
                // 对 description 进行简单处理：用双引号包裹，并将内部双引号替换为两个双引号
                "\"" + getDescription().replace("\"", "\"\"") + "\"",
                // 处理 relatedAccountUsername 可能为 null 的情况
                Objects.toString(getRelatedAccountUsername(), "")
        );
    }

    /**
     * 从 CSV 格式的一行创建 TransactionModel 对象。
     * 需要与 toCSV 方法的格式严格对应。
     * @param csvLine CSV 文件中的一行数据
     * @return 创建的 TransactionModel 对象，如果格式错误则返回 null
     */
    public static TransactionModel fromCSV(String csvLine) {
        // 注意：这个简单的 split 对包含逗号的 description 处理可能不完善
        // 实际应用中可能需要更健壮的 CSV 解析库
        String[] parts = csvLine.split(","); // 简单按逗号分割
        if (parts.length >= 7) {
            try {
                // 反处理 description 中的引号
                String description = parts[5].replace("\"\"", "\"");
                if (description.startsWith("\"") && description.endsWith("\"")) {
                    description = description.substring(1, description.length() - 1);
                }

                return new TransactionModel(
                        parts[0], // transactionId
                        parts[1], // accountUsername
                        parts[2], // type
                        Double.parseDouble(parts[3]), // amount
                        parts[4], // timestamp
                        description, // description
                        parts[6].isEmpty() ? null : parts[6] // relatedAccountUsername (空字符串视为null)
                );
            } catch (NumberFormatException e) {
                System.err.println("从CSV解析交易金额失败: " + csvLine);
                return null;
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("从CSV解析交易数据失败 - 部件不足: " + csvLine);
                return null;
            }
        }
        System.err.println("从CSV解析交易数据失败 - 部件不足: " + csvLine);
        return null; // 格式不符
    }
}