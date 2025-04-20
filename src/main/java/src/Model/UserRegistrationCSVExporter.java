package src.Model;

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
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(CSV_FILE_PATH), "UTF-8"))) {
            String line;
            // Read and skip the header line
            reader.readLine();

            // Read account data
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 10) { // Ensure there are 10 fields (including balance)
                    String username = parts[0];
                    String password = parts[1];
                    String phone = parts[2];
                    String email = parts[3];
                    String gender = parts[4];
                    String address = parts[5];
                    String creationTime = parts[6];
                    String accountStatus = parts[7];
                    String accountType = parts[8];
                    double balance = Double.parseDouble(parts[9]); // Parse balance

                    if (accountType.equals("Admin")) {
                        accounts.add(new AdminAccount(username, password, phone, email, gender, address, creationTime, accountStatus, accountType, balance));
                    } else {
                        accounts.add(new PersonalAccount(username, password, phone, email, gender, address, creationTime, accountStatus, accountType, balance));
                    }
                } else {
                    System.out.println("Invalid data row: " + line + ". Expected 10 fields but found " + parts.length + ".");
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