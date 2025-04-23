package AccountModel; // Note: Package name suggests Controller, but class content does not.

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
 * Class for importing transaction records from a CSV file and performing validation.
 */
public class TransactionCSVImporterModel {

    // Defines the expected CSV file header
    public static final String EXPECTED_HEADER = "user name,operation performed,amount,payment time,merchant name";
    // Defines the set of allowed operation types
    private static final Set<String> ALLOWED_OPERATIONS = new HashSet<>(Arrays.asList(
            "Transfer Out", "Transfer In", "Withdrawal", "Deposit"
    ));
    // Defines the expected number of fields in each data line
    private static final int EXPECTED_FIELD_COUNT = 5;
    // Defines the index of the operation type field (0-based)
    private static final int OPERATION_FIELD_INDEX = 1;

    /**
     * Imports transaction records from a specified source CSV file to a destination CSV file.
     * Validates the file header and the operation type of each record.
     * Skips the header row of the source file and appends valid data rows to the destination file.
     *
     * @param sourceFile The source CSV file to read transaction records from.
     * @param destinationFilePath The path to the destination CSV file where records should be appended (e.g., "transactions.csv").
     * @return The number of transaction records successfully imported.
     * @throws IOException If an I/O error occurs during file reading or writing.
     * @throws IllegalArgumentException If the source file format (header, field count, or operation type) is invalid.
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
                // Skip empty lines or lines with only whitespace
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Process file header
                if (!headerProcessed) {
                    // Validate header matches
                    if (!line.trim().equalsIgnoreCase(EXPECTED_HEADER)) {
                        // Header mismatch, throw exception
                        throw new IllegalArgumentException("File header format error. Expected format: '" + EXPECTED_HEADER + "', Actual: '" + line + "'");
                    }
                    headerProcessed = true; // Mark header as processed, subsequent lines are data
                    continue; // Skip the header line, don't process it as data
                }

                // Process data line
                // Use -1 as the limit parameter to ensure empty trailing fields are included by split
                String[] fields = line.split(",", -1);

                // Validate field count
                if (fields.length != EXPECTED_FIELD_COUNT) {
                    // Incorrect field count, log error and skip the line
                    System.err.println("Skipping invalid line (incorrect field count): " + line);
                    continue;
                }

                // Validate operation type field
                String operation = fields[OPERATION_FIELD_INDEX].trim();
                if (!ALLOWED_OPERATIONS.contains(operation)) {
                    // Invalid operation type, log error and skip the line
                    System.err.println("Skipping invalid line (invalid operation type: '" + operation + "'): " + line);
                    continue;
                }

                // If all validations pass, add the original data line to the list of valid data lines
                validDataLines.add(line);
            }
        } catch (IOException e) {
            // Catch IO errors during file reading, re-throw or wrap
            throw new IOException("Failed to read source file: " + e.getMessage(), e);
        }

        // Check if at least one line (header) was processed
        if (!headerProcessed && linesRead > 0) {
             throw new IllegalArgumentException("Source file is empty or contains only whitespace lines, no data or header to import.");
        } else if (linesRead == 0) {
             // File is completely empty
             System.out.println("Source file is empty, no data to import.");
             return 0; // No lines read, return 0
        }

        // Write valid data lines to the destination file (append mode)
        int importedCount = 0;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(destinationFilePath, true))) {
            for (String dataLine : validDataLines) {
                bw.write(dataLine);
                bw.newLine(); // Add a newline character after each data line
                importedCount++;
            }
        } catch (IOException e) {
            // Catch IO errors during file writing, re-throw or wrap
            throw new IOException("Failed to write to destination file: " + e.getMessage(), e);
        }

        return importedCount; // Return the number of successfully imported records
    }
}