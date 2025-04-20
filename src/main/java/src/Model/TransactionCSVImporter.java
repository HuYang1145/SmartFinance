package src.Model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用于从 CSV 文件导入交易记录并进行验证的类。
 */
public class TransactionCSVImporter {

    // 定义期望的 CSV 文件头部
    public static final String EXPECTED_HEADER = "user name,operation performed,amount,payment time,merchant name";
    // 定义允许的操作类型集合
    private static final Set<String> ALLOWED_OPERATIONS = new HashSet<>(Arrays.asList(
            "Transfer Out", "Transfer In", "Withdrawal", "Deposit"
    ));
    // 定义每行数据的期望字段数量
    private static final int EXPECTED_FIELD_COUNT = 5;
    // 定义操作类型字段的索引（从0开始）
    private static final int OPERATION_FIELD_INDEX = 1;

    /**
     * 从指定的源 CSV 文件导入交易记录到目标 CSV 文件。
     * 会验证文件头部和每条记录的操作类型。
     * 跳过源文件的头部行，并将有效数据行追加到目标文件。
     *
     * @param sourceFile        要读取交易记录的源 CSV 文件。
     * @param destinationFilePath 交易记录应追加到的目标 CSV 文件路径（例如 "transactions.csv"）。
     * @return 成功导入的交易记录条数。
     * @throws IOException             如果发生文件读写错误。
     * @throws IllegalArgumentException 如果源文件格式（头部或字段数量、操作类型）无效。
     */
    public static int importTransactions(File sourceFile, String destinationFilePath)
            throws IOException, IllegalArgumentException {

        List<String> validDataLines = new ArrayList<>();
        boolean headerProcessed = false;
        int linesRead = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(sourceFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                linesRead++;
                // 跳过空行或只有空白的行
                if (line.trim().isEmpty()) {
                    continue;
                }

                // 处理文件头部
                if (!headerProcessed) {
                    // 验证头部是否匹配
                    if (!line.trim().equalsIgnoreCase(EXPECTED_HEADER)) {
                        // 头部不匹配，抛出异常
                        throw new IllegalArgumentException("文件头部格式错误。期望格式: '" + EXPECTED_HEADER + "', 实际是: '" + line + "'");
                    }
                    headerProcessed = true; // 标记头部已处理，后续行视为数据行
                    continue; // 跳过头部行，不将其作为数据处理
                }

                // 处理数据行
                // 使用-1作为limit参数，确保即使最后一个字段为空白也会被split分割出来
                String[] fields = line.split(",", -1);

                // 验证字段数量
                if (fields.length != EXPECTED_FIELD_COUNT) {
                    // 字段数量不正确，记录错误并跳过该行
                    System.err.println("跳过无效行 (字段数量不正确): " + line);
                    continue;
                }

                // 验证操作类型字段
                String operation = fields[OPERATION_FIELD_INDEX].trim();
                if (!ALLOWED_OPERATIONS.contains(operation)) {
                    // 操作类型无效，记录错误并跳过该行
                    System.err.println("跳过无效行 (操作类型无效: '" + operation + "'): " + line);
                    continue;
                }

                // 如果所有验证通过，将原始数据行添加到有效数据列表中
                validDataLines.add(line);
            }
        } catch (IOException e) {
             // 捕获读取文件时的IO错误，重新抛出或包装
             throw new IOException("读取源文件失败: " + e.getMessage(), e);
        }

        // 检查是否处理了至少一行（头部）
        if (!headerProcessed && linesRead > 0) {
             throw new IllegalArgumentException("源文件为空或只包含空白行，没有可导入的数据和头部。");
        } else if (linesRead == 0) {
             // 文件完全为空
            System.out.println("源文件为空，没有数据可导入。");
             return 0; // 没有读取到任何行，返回0
        }


        // 将有效数据行写入目标文件（追加模式）
        int importedCount = 0;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(destinationFilePath, true))) {
            for (String dataLine : validDataLines) {
                bw.write(dataLine);
                bw.newLine(); // 在每行数据后添加换行符
                importedCount++;
            }
        } catch (IOException e) {
            // 捕获写入文件时的IO错误，重新抛出或包装
            throw new IOException("写入目标文件失败: " + e.getMessage(), e);
        }

        return importedCount; // 返回成功导入的条数
    }
}