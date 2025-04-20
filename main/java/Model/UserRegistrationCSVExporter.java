package Model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
    public static void saveToCSV(List<AccountModel> accountList, boolean append) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(CSV_FILE_PATH, append), "UTF-8"))) {
            if (!append) {
                // If overwriting, write the header
                writer.append(CSV_HEADER);
            } else {
                // If appending, check if the file is empty and write the header if it is (optional, depending on your needs)
                File file = new File(CSV_FILE_PATH);
                if (file.length() == 0) {
                    writer.append(CSV_HEADER);
                }
            }

            // Write account data
            for (AccountModel account : accountList) {
                writer.append(account.toCSV());
                writer.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
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