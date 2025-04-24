package AccountModel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.JOptionPane;

public class TransactionService {

    private static final String CSV_FILE_PATH = "transactions.csv";
    private static final String CSV_HEADER = "user,operation,amount,time,merchant,type,remark,category,payment_method,location,tag,attachment,recurrence";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    private static void ensureFileExists() {
        File file = new File(CSV_FILE_PATH);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    System.out.println("Created new file: " + CSV_FILE_PATH);
                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
                        bw.write(CSV_HEADER);
                        bw.newLine();
                        System.out.println("Written header to " + CSV_FILE_PATH);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error creating file " + CSV_FILE_PATH + ": " + e.getMessage());
                JOptionPane.showMessageDialog(null,
                        "Could not create transaction file: " + e.getMessage(),
                        "File Creation Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                String firstLine = br.readLine();
                if (firstLine == null || !firstLine.trim().equalsIgnoreCase(CSV_HEADER)) {
                    System.err.println("WARNING: File " + CSV_FILE_PATH + " exists but header might be missing or incorrect.");
                }
            } catch (IOException e) {
                System.err.println("Error checking header for file " + CSV_FILE_PATH + ": " + e.getMessage());
            }
        }
    }


    /**
 * Appends a new transaction record to the transactions.csv file.
 */
    public static boolean addTransaction(String username, String operation, double amount, String time, String merchant, String type,
                                         String remark, String category, String paymentMethod, String location, String tag,
                                         String attachment, String recurrence) {
        ensureFileExists();

        if (username == null || username.trim().isEmpty() ||
                operation == null || (!operation.equals("Income") && !operation.equals("Expense")) ||
                amount < 0 || time == null || time.trim().isEmpty()) {
            System.err.println("Invalid transaction data provided to addTransaction.");
            return false;
        }

        // 验证日期时间格式是否为 yyyy/MM/dd HH:mm
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        dateTimeFormat.setLenient(false); // 严格验证格式
        try {
            dateTimeFormat.parse(time.trim());
        } catch (ParseException e) {
            System.err.println("Invalid date/time format provided for transaction: " + time);
            JOptionPane.showMessageDialog(null,
                    "Invalid date/time format provided: " + time + ". Expected: yyyy/MM/dd HH:mm",
                    "Date/Time Format Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try (FileOutputStream fos = new FileOutputStream(CSV_FILE_PATH, true);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(osw)) {
            String formattedAmount = String.format("%.2f", amount);
            String safeMerchant = Objects.toString(merchant, "");
            String safeType = Objects.toString(type, "u");
            String safeRemark = Objects.toString(remark, "");
            String safeCategory = Objects.toString(category, "u");
            String safePaymentMethod = Objects.toString(paymentMethod, "");
            String safeLocation = Objects.toString(location, "");
            String safeTag = Objects.toString(tag, "");
            String safeAttachment = Objects.toString(attachment, "");
            String safeRecurrence = Objects.toString(recurrence, "");

            String csvLine = String.join(",",
                    escapeCsvField(username),
                    escapeCsvField(operation),
                    formattedAmount,
                    escapeCsvField(time.trim()),
                    escapeCsvField(safeMerchant),
                    escapeCsvField(safeType),
                    escapeCsvField(safeRemark),
                    escapeCsvField(safeCategory),
                    escapeCsvField(safePaymentMethod),
                    escapeCsvField(safeLocation),
                    escapeCsvField(safeTag),
                    escapeCsvField(safeAttachment),
                    escapeCsvField(safeRecurrence)
            );

            bw.write(csvLine);
            bw.newLine();
            return true;

        } catch (IOException e) {
            System.err.println("Error writing transaction to CSV: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Failed to record transaction: " + e.getMessage(),
                    "Transaction Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Reads all transaction records for a specific user from transactions.csv.
     */
    public static List<TransactionData> readTransactions(String username) {
        ensureFileExists();
        List<TransactionData> transactions = new ArrayList<>();
        File file = new File(CSV_FILE_PATH);

        if (!file.exists() || username == null || username.trim().isEmpty()) {
            return transactions;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",", -1);

                if (data.length >= 6) {
                    String csvUsername = unescapeCsvField(data[0]);
                    if (username.equals(csvUsername)) {
                        try {
                            String operation = unescapeCsvField(data[1]);
                            double amount = Double.parseDouble(data[2].trim());
                            String time = unescapeCsvField(data[3]);
                            String merchant = unescapeCsvField(data[4]);
                            String type = unescapeCsvField(data[5]);
                            String remark = data.length > 6 ? unescapeCsvField(data[6]) : "";
                            String category = data.length > 7 ? unescapeCsvField(data[7]) : "u";
                            String paymentMethod = data.length > 8 ? unescapeCsvField(data[8]) : "";
                            String location = data.length > 9 ? unescapeCsvField(data[9]) : "";
                            String tag = data.length > 10 ? unescapeCsvField(data[10]) : "";
                            String attachment = data.length > 11 ? unescapeCsvField(data[11]) : "";
                            String recurrence = data.length > 12 ? unescapeCsvField(data[12]) : "";

                            transactions.add(new TransactionData(csvUsername, operation, amount, time, merchant, type,
                                    remark, category, paymentMethod, location, tag, attachment, recurrence));
                        } catch (NumberFormatException e) {
                            System.err.println("Skipping transaction due to amount parse error: " + line + " | Error: " + e.getMessage());
                        } catch (Exception e) {
                            System.err.println("Skipping transaction due to unexpected error: " + line + " | Error: " + e.getMessage());
                        }
                    }
                } else {
                    System.err.println("Skipping transaction due to insufficient columns (" + data.length + "): " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading transactions from CSV for user " + username + ": " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Failed to read transaction history: " + e.getMessage(),
                    "Read Error", JOptionPane.ERROR_MESSAGE);
        }
        return transactions;
    }

    /**
     * Reads all transaction records from transactions.csv.
     */
    public static List<TransactionData> readAllTransactions() {
        ensureFileExists();
        List<TransactionData> transactions = new ArrayList<>();
        File file = new File(CSV_FILE_PATH);
        if (!file.exists()) return transactions;

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",", -1);
                if (data.length >= 6) {
                    try {
                        String username = unescapeCsvField(data[0]);
                        String operation = unescapeCsvField(data[1]);
                        double amount = Double.parseDouble(data[2].trim());
                        String time = unescapeCsvField(data[3]);
                        String merchant = unescapeCsvField(data[4]);
                        String type = unescapeCsvField(data[5]);
                        String remark = data.length > 6 ? unescapeCsvField(data[6]) : "";
                        String category = data.length > 7 ? unescapeCsvField(data[7]) : "u";
                        String paymentMethod = data.length > 8 ? unescapeCsvField(data[8]) : "";
                        String location = data.length > 9 ? unescapeCsvField(data[9]) : "";
                        String tag = data.length > 10 ? unescapeCsvField(data[10]) : "";
                        String attachment = data.length > 11 ? unescapeCsvField(data[11]) : "";
                        String recurrence = data.length > 12 ? unescapeCsvField(data[12]) : "";

                        transactions.add(new TransactionData(username, operation, amount, time, merchant, type,
                                remark, category, paymentMethod, location, tag, attachment, recurrence));
                    } catch (Exception e) {
                        System.err.println("Skipping transaction during readAll: " + line + " | Error: " + e.getMessage());
                    }
                } else {
                    System.err.println("Skipping transaction (readAll) due to incorrect column count (" + data.length + "): " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading all transactions from CSV: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Failed to read full transaction history: " + e.getMessage(),
                    "Read Error", JOptionPane.ERROR_MESSAGE);
        }
        return transactions;
    }

    private static String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            String escapedField = field.replace("\"", "\"\"");
            return "\"" + escapedField + "\"";
        }
        return field;
    }

    private static String unescapeCsvField(String field) {
        if (field == null) return "";
        field = field.trim();
        if (field.startsWith("\"") && field.endsWith("\"") && field.length() >= 2) {
            String content = field.substring(1, field.length() - 1);
            return content.replace("\"\"", "\"");
        }
        return field;
    }

    public static class TransactionData {
        public final String username;
        public final String operation;
        public final double amount;
        public final String time;
        public final String merchant;
        public final String type;
        public final String remark;
        public final String category;
        public final String paymentMethod;
        public final String location;
        public final String tag;
        public final String attachment;
        public final String recurrence;

        public TransactionData(String username, String operation, double amount, String time, String merchant, String type,
                               String remark, String category, String paymentMethod, String location, String tag,
                               String attachment, String recurrence) {
            this.username = username;
            this.operation = operation;
            this.amount = amount;
            this.time = time;
            this.merchant = merchant;
            this.type = type;
            this.remark = remark;
            this.category = category;
            this.paymentMethod = paymentMethod;
            this.location = location;
            this.tag = tag;
            this.attachment = attachment;
            this.recurrence = recurrence;
        }

        public String getUsername() { return username; }
        public String getOperation() { return operation; }
        public double getAmount() { return amount; }
        public String getTime() { return time; }
        public String getMerchant() { return merchant; }
        public String getType() { return type; }
        public String getCategory() { return category; }
        public String getRemark() { return remark; }
        public String getPaymentMethod() { return paymentMethod; }
        public String getLocation() { return location; }
        public String getTag() { return tag; }
        public String getAttachment() { return attachment; }
        public String getRecurrence() { return recurrence; }

        @Override
        public String toString() {
            return "TransactionData{" +
                   "username='" + username + '\'' +
                   ", operation='" + operation + '\'' +
                   ", amount=" + amount +
                   ", time='" + time + '\'' +
                   ", merchant='" + merchant + '\'' +
                   ", type='" + type + '\'' +
                   ", category='" + category + '\'' +
                   '}';
        }
    }
}