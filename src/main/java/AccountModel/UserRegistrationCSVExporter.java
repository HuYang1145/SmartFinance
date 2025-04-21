package AccountModel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


public class UserRegistrationCSVExporter {
    private static final String CSV_FILE_PATH = "accounts.csv";
    private static final String CSV_HEADER = "Username,Password,Phone,Email,Gender,Address,CreationTime,AccountStatus,AccountType,Balance\n";

    /**
     * Saves the account list to a CSV file.
     *
     * @param accountList The list of accounts to save.
     * @param append      If true, appends to the file; if false, overwrites the file.
     */
    public static boolean saveToCSV(List<AccountModel> accountList, boolean append) {
        // Use the compatible way to create BufferedWriter with charset
        try (FileOutputStream fos = new FileOutputStream(CSV_FILE_PATH, append);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8); // Use UTF-8
             BufferedWriter writer = new BufferedWriter(osw)) {

            File file = new File(CSV_FILE_PATH);
            // Write header only if overwriting OR appending to an empty/new file
            if (!append || (append && file.length() == 0)) {
                 // Check if the file actually exists *before* writing header when appending
                 // If appending to a non-empty file, we don't write the header.
                 if (!append && file.exists()) { // Overwriting existing file
                      writer.write(CSV_HEADER);
                      writer.newLine();
                 } else if (file.length() == 0) { // Appending to empty or new file
                      writer.write(CSV_HEADER);
                      writer.newLine();
                 }
                 // Note: This logic ensures header is written correctly in most cases.
                 // Consider edge cases like appending to a file with only a header.
            }

            // Write account data
            for (AccountModel account : accountList) {
                // Assuming account.toCSV() provides the correct 10-column comma-separated string
                writer.write(account.toCSV());
                writer.newLine();
            }
            return true; // --- MODIFIED: Return true on success ---

        } catch (IOException e) {
            System.err.println("Error saving accounts to CSV: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
             // Optionally show a user-friendly error message here if needed
            // JOptionPane.showMessageDialog(null, "Error saving account data.", "Save Error", JOptionPane.ERROR_MESSAGE);
            return false; // --- MODIFIED: Return false on failure ---
        }
    }

    public static List<AccountModel> readFromCSV() {
        List<AccountModel> accounts = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(CSV_FILE_PATH), "UTF-8"));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader().withSkipHeaderRecord())) {
    
            for (CSVRecord record : csvParser) {
                String username = record.get("Username").trim();
                String password = record.get("Password").trim();
                String phone = record.get("Phone").trim();
                String email = record.get("Email").trim();
                String gender = record.get("Gender").trim();
                String address = record.get("Address").trim();
                String creationTime = record.get("CreationTime").trim();
                String accountStatus = record.get("AccountStatus").trim();
                String accountType = record.get("AccountType").trim();
                double balance = Double.parseDouble(record.get("Balance").trim()); // Parse balance
    
                if (accountType.equals("Admin")) {
                    accounts.add(new AdminAccount(username, password, phone, email, gender, address, creationTime, accountStatus, accountType, balance));
                } else {
                    accounts.add(new PersonalAccount(username, password, phone, email, gender, address, creationTime, accountStatus, accountType, balance));
                }
            }
    
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.out.println("Error reading balance, please check the CSV file format.");
            e.printStackTrace();
        }
        return accounts;
    }
}