package TransactionController;

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
import java.util.Objects;
import java.util.Set;

import javax.swing.JOptionPane;

import AccountModel.AccountModel;
import AccountModel.BudgetServiceModel;
import TransactionModel.TransactionModel;

/**
 * Handles transaction-related operations including checking, importing, adding, removing, and reading transactions.
 */
public class TransactionController {
    private static final String CSV_FILE_PATH = "transactions.csv";
    public static final String CSV_HEADER = "user,operation,amount,time,merchant,type,remark,category,payment_method,location,tag,attachment,recurrence";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    private static final Set<String> ALLOWED_OPERATIONS = new HashSet<>(Arrays.asList("Transfer Out", "Transfer In", "Withdrawal", "Deposit"));
    private static final int EXPECTED_FIELD_COUNT = 13;
    private static final int OPERATION_FIELD_INDEX = 1;

    /**
     * Ensures the transactions CSV file exists and has the correct header.
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
     * Checks if an account has abnormal transactions (Transfer In/Out with amount > 500).
     *
     * @param account The AccountModel to check.
     * @return true if an abnormal transaction is found, false otherwise.
     */
    public static boolean hasAbnormalTransactions(AccountModel account) {
        if (account == null) {
            System.err.println("Received null account object.");
            return false;
        }

        List<TransactionModel> transactions = account.getTransactions();
        if (transactions == null || transactions.isEmpty()) {
            return false;
        }

        System.out.println("Checking " + transactions.size() + " transactions for account " + account.getUsername() + "...");
        for (TransactionModel transaction : transactions) {
            if (transaction == null) {
                System.err.println("Found null transaction for account " + account.getUsername() + ".");
                continue;
            }

            String transactionType = transaction.getType();
            if (transactionType != null &&
                ("Transfer Out".equalsIgnoreCase(transactionType) || "Transfer In".equalsIgnoreCase(transactionType)) &&
                transaction.getAmount() > 500) {
                System.out.println("Abnormal transaction found for account " + account.getUsername() +
                                   " (Type: " + transactionType + ", Amount: " + transaction.getAmount() + ")");
                return true;
            }
        }

        System.out.println("No abnormal transactions found for account " + account.getUsername() + ".");
        return false;
    }

    /**
     * Imports transactions from a source CSV to the destination CSV.
     *
     * @param sourceFile         The source CSV file.
     * @param destinationFilePath The destination CSV file path.
     * @return The number of records imported.
     * @throws IOException             If an I/O error occurs.
     * @throws IllegalArgumentException If the CSV format is invalid.
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
     * Adds a transaction with minimal fields.
     *
     * @param account    The AccountModel to associate with the transaction.
     * @param operation  The operation type ("Income" or "Expense").
     * @param amount     The transaction amount.
     * @param time       The transaction time (yyyy/MM/dd [HH:mm]).
     * @param merchant   The merchant or payee.
     * @param type       The transaction type.
     * @return true if the transaction was added successfully, false otherwise.
     */
    public static boolean addTransaction(AccountModel account, String operation, double amount, String time, String merchant, String type) {
        return addTransaction(account, operation, amount, time, merchant, type, "", type, "", "", "", "", "");
    }

    /**
     * Adds a transaction with all fields.
     */
    public static boolean addTransaction(AccountModel account, String operation, double amount, String time, String merchant, String type,
                                        String remark, String category, String paymentMethod, String location, String tag,
                                        String attachment, String recurrence) {
        ensureFileExists();

        if (account == null || account.getUsername() == null || account.getUsername().trim().isEmpty() ||
            operation == null || (!operation.equals("Income") && !operation.equals("Expense")) ||
            amount < 0 || time == null || time.trim().isEmpty()) {
            System.err.println("Invalid transaction data.");
            return false;
        }

        // Normalize timestamp to yyyy/MM/dd HH:mm
        String normalizedTime;
        try {
            LocalDateTime dateTime;
            try {
                // Try parsing with full format (yyyy/MM/dd HH:mm)
                dateTime = LocalDateTime.parse(time.trim(), BudgetServiceModel.DATE_FORMATTER);
            } catch (DateTimeParseException e1) {
                // Try parsing as date only (yyyy/MM/dd)
                LocalDate date = LocalDate.parse(time.trim(), DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                dateTime = date.atStartOfDay(); // Default to 00:00
            }
            normalizedTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
        } catch (DateTimeParseException e) {
            System.err.println("Invalid date/time format: " + time);
            JOptionPane.showMessageDialog(null, "Invalid date/time: " + time + ". Expected: yyyy/MM/dd [HH:mm]", "Date/Time Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(CSV_FILE_PATH, true), StandardCharsets.UTF_8))) {
            String csvLine = String.join(",",
                    escapeCsvField(account.getUsername()),
                    escapeCsvField(operation),
                    String.format("%.2f", amount),
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

            // Create TransactionModel with normalized timestamp
            TransactionModel transaction = new TransactionModel(
                    account.getUsername(), operation, amount, normalizedTime, merchant, type,
                    remark, category, paymentMethod, location, tag, attachment, recurrence
            );

            // Add transaction to AccountModel
            account.addTransaction(transaction);
            return true;
        } catch (IOException e) {
            System.err.println("Error writing transaction: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Failed to record transaction: " + e.getMessage(), "Transaction Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("Error adding transaction to account: " + e.getMessage());
            return false;
        }
    }

    /**
     * Removes a transaction by username and time.
     *
     * @param account The AccountModel to remove the transaction from.
     * @param time    The transaction time (yyyy/MM/dd HH:mm).
     * @return true if the transaction was removed successfully, false otherwise.
     */
    public static boolean removeTransaction(AccountModel account, String time) {
        ensureFileExists();
        File file = new File(CSV_FILE_PATH);
        File tempFile = new File("transactions_temp.csv");
        boolean removed = false;

        if (account == null || time == null || time.trim().isEmpty()) {
            System.err.println("Invalid parameters for removeTransaction.");
            return false;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
             BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile, StandardCharsets.UTF_8))) {
            bw.write(CSV_HEADER);
            bw.newLine();
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",", -1);
                if (data.length >= 4 && account.getUsername().equals(unescapeCsvField(data[0])) && time.equals(unescapeCsvField(data[3]))) {
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
            // Update AccountModel transactions
            account.getTransactions().removeIf(t -> t.getTimestamp().equals(time));
        } else {
            tempFile.delete();
        }
        return removed;
    }

    /**
     * Reads transactions for a specific user.
     *
     * @param username The username to filter transactions.
     * @return List of TransactionModel objects.
     */
    public static List<TransactionModel> readTransactions(String username) {
        ensureFileExists();
        List<TransactionModel> transactions = new ArrayList<>();
        File file = new File(CSV_FILE_PATH);

        if (username == null || username.trim().isEmpty()) {
            return transactions;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",", -1);
                if (data.length >= 6 && username.equals(unescapeCsvField(data[0]))) {
                    try {
                        // Normalize timestamp
                        String normalizedTime;
                        try {
                            LocalDateTime dateTime = LocalDateTime.parse(data[3].trim(), BudgetServiceModel.DATE_FORMATTER);
                            normalizedTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
                        } catch (DateTimeParseException e1) {
                            LocalDate date = LocalDate.parse(data[3].trim(), DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                            normalizedTime = date.atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
                        }

                        transactions.add(new TransactionModel(
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
                        ));
                    } catch (NumberFormatException | DateTimeParseException e) {
                        System.err.println("Skipping transaction (parse error): " + line + " | Error: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading transactions: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Failed to read transactions: " + e.getMessage(), "Read Error", JOptionPane.ERROR_MESSAGE);
        }
        return transactions;
    }

    /**
     * Reads all transactions.
     *
     * @return List of all TransactionModel objects.
     */
    public static List<TransactionModel> readAllTransactions() {
        ensureFileExists();
        List<TransactionModel> transactions = new ArrayList<>();
        File file = new File(CSV_FILE_PATH);

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",", -1);
                if (data.length >= 6) {
                    try {
                        // Normalize timestamp
                        String normalizedTime;
                        try {
                            LocalDateTime dateTime = LocalDateTime.parse(data[3].trim(), BudgetServiceModel.DATE_FORMATTER);
                            normalizedTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
                        } catch (DateTimeParseException e1) {
                            LocalDate date = LocalDate.parse(data[3].trim(), DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                            normalizedTime = date.atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
                        }

                        transactions.add(new TransactionModel(
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
                        ));
                    } catch (NumberFormatException | DateTimeParseException e) {
                        System.err.println("Skipping transaction: " + line + " | Error: " + e.getMessage());
                    }
                }
            }
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