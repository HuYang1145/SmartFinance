package AccountController;

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

import AccountModel.AccountModel;
import AccountModel.AdminAccount;
import AccountModel.PersonalAccount;

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
        try (FileOutputStream fos = new FileOutputStream(CSV_FILE_PATH, append);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)) {

            File file = new File(CSV_FILE_PATH);
            if (!append || (append && file.length() == 0)) {
                if (!append && file.exists()) {
                    writer.write(CSV_HEADER);
                    writer.newLine();
                } else if (file.length() == 0) {
                    writer.write(CSV_HEADER);
                    writer.newLine();
                }
            }

            for (AccountModel account : accountList) {
                writer.write(account.toCSV());
                writer.newLine();
            }
            return true;

        } catch (IOException e) {
            System.err.println("Error saving accounts to CSV: " + e.getMessage());
            e.printStackTrace();
            return false;
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
                AccountModel.AccountStatus accountStatus = AccountModel.AccountStatus.valueOf(record.get("AccountStatus").trim().toUpperCase());
                String accountType = record.get("AccountType").trim();
                double balance = Double.parseDouble(record.get("Balance").trim());

                if ("Admin".equalsIgnoreCase(accountType)) {
                    accounts.add(new AdminAccount(username, password, phone, email, gender, address, creationTime, accountStatus, accountType, balance));
                } else {
                    accounts.add(new PersonalAccount(username, password, phone, email, gender, address, creationTime, accountStatus, accountType, balance));
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Error parsing balance in CSV: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing AccountStatus in CSV: " + e.getMessage());
            e.printStackTrace();
        }
        return accounts;
    }
}