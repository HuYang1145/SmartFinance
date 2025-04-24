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

public class AccountRepository {
    private static final String CSV_FILE_PATH = "accounts.csv";
    private static final String CSV_HEADER = "Username,Password,Phone,Email,Gender,Address,CreationTime,AccountStatus,AccountType,Balance\n";

    /**
     * 将账户列表保存到 CSV 文件。
     *
     * @param accountList 要保存的账户列表。
     * @param append      如果为 true，追加到文件；如果为 false，覆盖文件。
     */
    public static boolean saveToCSV(List<AccountModel> accountList, boolean append) {
        try (FileOutputStream fos = new FileOutputStream(CSV_FILE_PATH, append);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)) {

            File file = new File(CSV_FILE_PATH);
            if (!append || (append && file.length() == 0)) {
                if (!append && file.exists()) {
                    writer.write(CSV_HEADER);
                } else if (file.length() == 0) {
                    writer.write(CSV_HEADER);
                }
            }

            for (AccountModel account : accountList) {
                writer.write(account.toCSV());
                writer.newLine();
            }
            return true;

        } catch (IOException e) {
            System.err.println("保存账户到 CSV 时出错: " + e.getMessage());
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

                AccountModel account = new AccountModel();
                account.setUsername(username);
                account.setPassword(password);
                account.setPhone(phone);
                account.setEmail(email);
                account.setGender(gender);
                account.setAddress(address);
                account.setCreationTime(creationTime);
                account.setAccountStatus(accountStatus);
                account.setAccountType(accountType);
                account.setBalance(balance);

                accounts.add(account);
            }

        } catch (IOException e) {
            System.err.println("读取 CSV 文件时出错: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("解析 CSV 中的余额时出错: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("解析 CSV 中的账户状态时出错: " + e.getMessage());
            e.printStackTrace();
        }
        return accounts;
    }
}