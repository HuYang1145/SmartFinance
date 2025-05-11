package Controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import javax.swing.JOptionPane;

import Model.Transaction;
import Model.User;
import Service.BudgetService;

public class TransactionController {
    private static final String CSV_FILE_PATH = "transactions.csv";
    public static final String CSV_HEADER = "user,operation,amount,time,merchant,type,remark,category,payment_method,location,tag,attachment,recurrence";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    private static final Set<String> ALLOWED_OPERATIONS = new HashSet<>(Arrays.asList("Transfer Out", "Transfer In", "Withdrawal", "Deposit"));
    private static final int EXPECTED_FIELD_COUNT = 13;
    private static final int OPERATION_FIELD_INDEX = 1;

    private static void ensureFileExists() {
        File file = new File(CSV_FILE_PATH);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
                        bw.write(CSV_HEADER);
                        bw.newLine();
                        System.out.println("Created new file: " + CSV_FILE_PATH);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error creating file: " + e.getMessage());
                JOptionPane.showMessageDialog(null, "Could not create file: " + e.getMessage(), "File Creation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static boolean hasAbnormalTransactions(String username, List<Transaction> transactions) {
        if (username == null || username.trim().isEmpty()) {
            System.err.println("Received null or empty username.");
            return false;
        }

        if (transactions == null || transactions.isEmpty()) {
            return false;
        }

        System.out.println("Checking " + transactions.size() + " transactions for user " + username + "...");
        for (Transaction transaction : transactions) {
            if (transaction == null) {
                System.err.println("Found null transaction for user " + username + ".");
                continue;
            }

            String transactionType = transaction.getType();
            if (transactionType != null &&
                    ("Transfer Out".equalsIgnoreCase(transactionType) || "Transfer In".equalsIgnoreCase(transactionType)) &&
                    transaction.getAmount() > 500) {
                System.out.println("Abnormal transaction found for user " + username +
                        " (Type: " + transactionType + ", Amount: " + transaction.getAmount() + ")");
                return true;
            }
        }

        System.out.println("No abnormal transactions found for user " + username + ".");
        return false;
    }

    public static int importTransactions(File sourceFile, String destinationFilePath) throws IOException, IllegalArgumentException {
        List<String> validDataLines = new ArrayList<>();
        boolean headerProcessed = false;
        int linesRead = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(sourceFile, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                linesRead++;
                if (line.trim().isEmpty()) continue;

                if (!headerProcessed) {
                    if (!line.trim().equalsIgnoreCase(CSV_HEADER)) {
                        throw new IllegalArgumentException("Invalid header. Expected: '" + CSV_HEADER + "', Actual: '" + line + "'");
                    }
                    headerProcessed = true;
                    continue;
                }

                String[] fields = line.split(",", -1);
                if (fields.length != EXPECTED_FIELD_COUNT) {
                    System.err.println("Skipping invalid line (field count: " + fields.length + "): " + line);
                    continue;
                }

                String operation = fields[OPERATION_FIELD_INDEX].trim();
                if (!ALLOWED_OPERATIONS.contains(operation)) {
                    System.err.println("Skipping invalid line (operation: '" + operation + "'): " + line);
                    continue;
                }

                validDataLines.add(line);
            }
        }

        if (!headerProcessed && linesRead > 0) {
            throw new IllegalArgumentException("Source file lacks valid header or data.");
        } else if (linesRead == 0) {
            System.out.println("Source file is empty.");
            return 0;
        }

        int importedCount = 0;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(destinationFilePath, StandardCharsets.UTF_8, true))) {
            for (String dataLine : validDataLines) {
                bw.write(dataLine);
                bw.newLine();
                importedCount++;
            }
        }
        return importedCount;
    }

    public static boolean addTransaction(String username, String operation, double amount, String time, String merchant, String type) {
        return addTransaction(username, operation, amount, time, merchant, type, "", type, "", "", "", "", "");
    }

    public static boolean addTransaction(String username, String operation, double amount, String time, String merchant, String type,
                                         String remark, String category, String paymentMethod, String location, String tag,
                                         String attachment, String recurrence) {
        ensureFileExists();

        if (username == null || username.trim().isEmpty() ||
                operation == null || (!operation.equals("Income") && !operation.equals("Expense")) ||
                amount < 0 || time == null || time.trim().isEmpty()) {
            System.err.println("Invalid transaction data: username=" + username + ", operation=" + operation + ", amount=" + amount + ", time=" + time);
            return false;
        }

        String normalizedTime;
        try {
            LocalDateTime dateTime;
            try {
                dateTime = LocalDateTime.parse(time.trim(), BudgetService.DATE_FORMATTER);
            } catch (DateTimeParseException e1) {
                LocalDate date = LocalDate.parse(time.trim(), DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                dateTime = date.atStartOfDay();
            }
            normalizedTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
        } catch (DateTimeParseException e) {
            System.err.println("Invalid date/time format: " + time + ", error: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Invalid date/time: " + time + ". Expected: yyyy/MM/dd [HH:mm]", "Date/Time Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(CSV_FILE_PATH, true), StandardCharsets.UTF_8))) {
            String csvLine = String.join(",",
                    escapeCsvField(username),
                    escapeCsvField(operation),
                    "%.2f".formatted(amount),
                    escapeCsvField(normalizedTime),
                    escapeCsvField(Objects.toString(merchant, "")),
                    escapeCsvField(Objects.toString(type, "u")),
                    escapeCsvField(Objects.toString(remark, "")),
                    escapeCsvField(Objects.toString(category, "u")),
                    escapeCsvField(Objects.toString(paymentMethod, "")),
                    escapeCsvField(Objects.toString(location, "")),
                    escapeCsvField(Objects.toString(tag, "")),
                    escapeCsvField(Objects.toString(attachment, "")),
                    escapeCsvField(Objects.toString(recurrence, ""))
            );
            bw.write(csvLine);
            bw.newLine();
            System.out.println("Transaction added: " + csvLine);
            return true;
        } catch (IOException e) {
            System.err.println("Error writing transaction: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Failed to record transaction: " + e.getMessage(), "Transaction Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * 把识别到的实体映射成 CSV 记录并调用静态 addTransaction。
     *
     * @param user     当前用户
     * @param entities 识别到的实体 Map，至少包含 operation, amount, timestamp
     * @return true if saved successfully
     */
    public boolean addTransactionFromEntities(User user, Map<String, String> entities) {
        // 1. 提取并校验必需字段
        String username  = user.getUsername();
        String operation = entities.get("operation");
        String amtStr    = entities.get("amount");
        String time      = entities.get("timestamp");
        if (username == null || operation == null || amtStr == null || time == null) {
            throw new IllegalArgumentException("缺少必需字段");
        }
        double amount;
        try {
            amount = Double.parseDouble(amtStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("金额格式不正确：" + amtStr, e);
        }

        // 2. 其它可选字段
        String merchant      = entities.getOrDefault("merchant", "");
        String type          = entities.getOrDefault("type", "");
        String remark        = entities.getOrDefault("remark", "");
        String category      = entities.getOrDefault("category", "");
        String paymentMethod = entities.getOrDefault("paymentMethod", "");
        String location      = entities.getOrDefault("location", "");
        String tag           = entities.getOrDefault("tag", "");
        String attachment    = entities.getOrDefault("attachment", "");
        String recurrence    = entities.getOrDefault("recurrence", "");

        // 3. 调用静态方法写入 CSV
        boolean ok = addTransaction(
                username,
                operation,
                amount,
                time,
                merchant,
                type,
                remark,
                category,
                paymentMethod,
                location,
                tag,
                attachment,
                recurrence
        );

        if (!ok) {
            throw new RuntimeException("记录交易失败: " + entities);
        }
        return true;
    }
    public static boolean removeTransaction(String username, String time) {
        ensureFileExists();
        File file = new File(CSV_FILE_PATH);
        File tempFile = new File("transactions_temp.csv");
        boolean removed = false;

        if (username == null || time == null || time.trim().isEmpty()) {
            System.err.println("Invalid parameters for removeTransaction: username=" + username + ", time=" + time);
            return false;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
             BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile, StandardCharsets.UTF_8))) {
            bw.write(CSV_HEADER);
            bw.newLine();
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",", -1);
                if (data.length >= 4 && username.equals(unescapeCsvField(data[0])) && time.equals(unescapeCsvField(data[3]))) {
                    removed = true;
                    continue;
                }
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error removing transaction: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Failed to remove transaction: " + e.getMessage(), "Transaction Error", JOptionPane.ERROR_MESSAGE);
            tempFile.delete();
            return false;
        }

        if (removed) {
            if (!file.delete() || !tempFile.renameTo(file)) {
                System.err.println("Error replacing transaction file.");
                tempFile.delete();
                return false;
            }
        } else {
            tempFile.delete();
        }
        return removed;
    }

    public static List<Transaction> readTransactions(String username) {
        ensureFileExists();
        List<Transaction> transactions = new ArrayList<>();
        File file = new File(CSV_FILE_PATH);

        if (username == null || username.trim().isEmpty()) {
            System.err.println("Cannot read transactions: username is null or empty");
            return transactions;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",", -1);
                if (data.length >= 6 && username.equals(unescapeCsvField(data[0]))) {
                    try {
                        String normalizedTime;
                        try {
                            LocalDateTime dateTime = LocalDateTime.parse(data[3].trim(), BudgetService.DATE_FORMATTER);
                            normalizedTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
                        } catch (DateTimeParseException e1) {
                            LocalDate date = LocalDate.parse(data[3].trim(), DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                            normalizedTime = date.atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
                        }

                        Transaction transaction = new Transaction(
                                unescapeCsvField(data[0]),
                                unescapeCsvField(data[1]),
                                Double.parseDouble(data[2].trim()),
                                normalizedTime,
                                unescapeCsvField(data[4]),
                                unescapeCsvField(data[5]),
                                data.length > 6 ? unescapeCsvField(data[6]) : "",
                                data.length > 7 ? unescapeCsvField(data[7]) : "u",
                                data.length > 8 ? unescapeCsvField(data[8]) : "",
                                data.length > 9 ? unescapeCsvField(data[9]) : "",
                                data.length > 10 ? unescapeCsvField(data[10]) : "",
                                data.length > 11 ? unescapeCsvField(data[11]) : "",
                                data.length > 12 ? unescapeCsvField(data[12]) : ""
                        );
                        transactions.add(transaction);
                    } catch (NumberFormatException | DateTimeParseException e) {
                        System.err.println("Skipping transaction (parse error): " + line + " | Error: " + e.getMessage());
                    }
                }
            }
            System.out.println("Read " + transactions.size() + " transactions for user " + username);
        } catch (IOException e) {
            System.err.println("Error reading transactions: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Failed to read transactions: " + e.getMessage(), "Read Error", JOptionPane.ERROR_MESSAGE);
        }
        return transactions;
    }

    public static List<Transaction> readAllTransactions() {
        ensureFileExists();
        List<Transaction> transactions = new ArrayList<>();
        File file = new File(CSV_FILE_PATH);

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",", -1);
                if (data.length >= 6) {
                    try {
                        String normalizedTime;
                        try {
                            LocalDateTime dateTime = LocalDateTime.parse(data[3].trim(), BudgetService.DATE_FORMATTER);
                            normalizedTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
                        } catch (DateTimeParseException e1) {
                            LocalDate date = LocalDate.parse(data[3].trim(), DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                            normalizedTime = date.atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
                        }

                        Transaction transaction = new Transaction(
                                unescapeCsvField(data[0]),
                                unescapeCsvField(data[1]),
                                Double.parseDouble(data[2].trim()),
                                normalizedTime,
                                unescapeCsvField(data[4]),
                                unescapeCsvField(data[5]),
                                data.length > 6 ? unescapeCsvField(data[6]) : "",
                                data.length > 7 ? unescapeCsvField(data[7]) : "u",
                                data.length > 8 ? unescapeCsvField(data[8]) : "",
                                data.length > 9 ? unescapeCsvField(data[9]) : "",
                                data.length > 10 ? unescapeCsvField(data[10]) : "",
                                data.length > 11 ? unescapeCsvField(data[11]) : "",
                                data.length > 12 ? unescapeCsvField(data[12]) : ""
                        );
                        transactions.add(transaction);
                    } catch (NumberFormatException | DateTimeParseException e) {
                        System.err.println("Skipping transaction: " + line + " | Error: " + e.getMessage());
                    }
                }
            }
            System.out.println("Read " + transactions.size() + " transactions in total");
        } catch (IOException e) {
            System.err.println("Error reading all transactions: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Failed to read transactions: " + e.getMessage(), "Read Error", JOptionPane.ERROR_MESSAGE);
        }
        return transactions;
    }

    private static String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    private static String unescapeCsvField(String field) {
        if (field == null) return "";
        field = field.trim();
        if (field.startsWith("\"") && field.endsWith("\"") && field.length() >= 2) {
            return field.substring(1, field.length() - 1).replace("\"\"", "\"");
        }
        return field;
    }
}