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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.swing.JOptionPane;

import Model.Transaction;
import Model.User;
import Service.BudgetService;

/**
 * Controller class for managing transactions in a financial management application.
 * It handles operations such as adding, removing, importing, and reading transactions
 * stored in a CSV file. The class also provides functionality to detect abnormal transactions
 * and supports parsing entities into transaction records.
 *
 * @author Group 19
 * @version 1.0
 */
public class TransactionController {
    /** The file path for storing transactions in CSV format. */
    private static final String CSV_FILE_PATH = "transactions.csv";

    /** The CSV header defining the structure of transaction records. */
    public static final String CSV_HEADER = "user,operation,amount,time,merchant,type,remark,category,payment_method,location,tag,attachment,recurrence";

    /** Date format used for parsing and formatting transaction timestamps. */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    /** Set of allowed transaction operations. */
    private static final Set<String> ALLOWED_OPERATIONS = new HashSet<>(Arrays.asList("Transfer Out", "Transfer In", "Withdrawal", "Deposit"));

    /** Expected number of fields in a valid CSV transaction record. */
    private static final int EXPECTED_FIELD_COUNT = 13;

    /** Index of the operation field in a CSV record. */
    private static final int OPERATION_FIELD_INDEX = 1;

    /**
     * Ensures the transaction CSV file exists, creating it with the header if it does not.
     * Displays an error message if file creation fails.
     */
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

    /**
     * Checks for abnormal transactions for a given user based on the provided transaction list.
     * An abnormal transaction is defined as a "Transfer Out" or "Transfer In" with an amount exceeding 500.
     *
     * @param username     The username to check transactions for.
     * @param transactions The list of transactions to analyze.
     * @return {@code true} if an abnormal transaction is found, {@code false} otherwise.
     */
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

    /**
     * Imports transactions from a source CSV file to the destination file, validating the header and data.
     *
     * @param sourceFile        The source CSV file containing transactions.
     * @param destinationFilePath The destination file path to append valid transactions.
     * @return The number of transactions successfully imported.
     * @throws IOException           If an I/O error occurs during file reading or writing.
     * @throws IllegalArgumentException If the source file has an invalid header or lacks valid data.
     */
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

    /**
     * Adds a transaction with minimal required fields to the CSV file.
     *
     * @param username  The username associated with the transaction.
     * @param operation The operation type (e.g., "Income", "Expense").
     * @param amount    The transaction amount.
     * @param time      The timestamp of the transaction.
     * @param merchant  The merchant involved in the transaction.
     * @param type      The transaction type.
     * @return {@code true} if the transaction was added successfully, {@code false} otherwise.
     */
    public static boolean addTransaction(String username, String operation, double amount, String time, String merchant, String type) {
        return addTransaction(username, operation, amount, time, merchant, type, "", type, "", "", "", "", "");
    }

    /**
     * Adds a transaction with all possible fields to the CSV file, validating and normalizing the data.
     *
     * @param username      The username associated with the transaction.
     * @param operation     The operation type (e.g., "Income", "Expense").
     * @param amount        The transaction amount.
     * @param time          The timestamp of the transaction.
     * @param merchant      The merchant involved in the transaction.
     * @param type          The transaction type.
     * @param remark        Additional remarks for the transaction.
     * @param category      The category of the transaction.
     * @param paymentMethod The payment method used.
     * @param location      The location of the transaction.
     * @param tag           Tags associated with the transaction.
     * @param attachment    Any attachment details for the transaction.
     * @param recurrence    Recurrence details for the transaction.
     * @return {@code true} if the transaction was added successfully, {@code false} otherwise.
     */
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
     * Adds a transaction to the CSV file by mapping entities extracted from external input.
     *
     * @param user     The current user.
     * @param entities A map of entity names to their values, containing at least operation, amount, and timestamp.
     * @return {@code true} if the transaction was added successfully.
     * @throws IllegalArgumentException If required fields are missing or invalid.
     * @throws RuntimeException        If the transaction cannot be recorded.
     */
    public boolean addTransactionFromEntities(User user, Map<String, String> entities) {
        // Extract and validate required fields
        String username = user.getUsername();
        String operation = entities.get("operation");
        String amtStr = entities.get("amount");
        String time = entities.get("time");
        if (username == null || operation == null || amtStr == null || time == null) {
            throw new IllegalArgumentException("Missing required fields");
        }
        double amount;
        try {
            amount = Double.parseDouble(amtStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format: " + amtStr, e);
        }

        // Extract optional fields
        String merchant = entities.getOrDefault("merchant", "");
        String type = entities.getOrDefault("type", "");
        String remark = entities.getOrDefault("remark", "");
        String category = entities.getOrDefault("category", "");
        String paymentMethod = entities.getOrDefault("paymentMethod", "");
        String location = entities.getOrDefault("location", "");
        String tag = entities.getOrDefault("tag", "");
        String attachment = entities.getOrDefault("attachment", "");
        String recurrence = entities.getOrDefault("recurrence", "");

        // Call static method to write to CSV
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
            throw new RuntimeException("Failed to record transaction: " + entities);
        }
        return true;
    }

    /**
     * Removes a transaction from the CSV file based on the username and timestamp.
     *
     * @param username The username associated with the transaction.
     * @param time     The timestamp of the transaction to remove.
     * @return {@code true} if the transaction was removed successfully, {@code false} otherwise.
     */
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

    /**
     * Reads all transactions for a specific user from the CSV file.
     *
     * @param username The username whose transactions are to be retrieved.
     * @return A list of {@link Transaction} objects for the specified user.
     */
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

    /**
     * Reads all transactions from the CSV file, regardless of the user.
     *
     * @return A list of all {@link Transaction} objects in the CSV file.
     */
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

    /**
     * Escapes a CSV field to handle commas, quotes, and newlines.
     *
     * @param field The field to escape.
     * @return The escaped field, or an empty string if the field is null.
     */
    private static String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    /**
     * Unescapes a CSV field to remove surrounding quotes and handle escaped quotes.
     *
     * @param field The field to unescape.
     * @return The unescaped field, or an empty string if the field is null.
     */
    private static String unescapeCsvField(String field) {
        if (field == null) return "";
        field = field.trim();
        if (field.startsWith("\"") && field.endsWith("\"") && field.length() >= 2) {
            return field.substring(1, field.length() - 1).replace("\"\"", "\"");
        }
        return field;
    }
}
