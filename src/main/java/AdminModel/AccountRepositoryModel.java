package AdminModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AccountModel.AccountModel;

public class AccountRepositoryModel {
    private static final Logger logger = LoggerFactory.getLogger(AccountRepositoryModel.class);
    private static final String CSV_FILE_PATH = "accounts.csv";

    public List<AccountModel> readFromCSV() throws IOException {
        List<AccountModel> accounts = new ArrayList<>();
        File file = new File(CSV_FILE_PATH);
        if (!file.exists()) {
            logger.warn("CSV file not found: {}", file.getAbsolutePath());
            throw new IOException("CSV file not found: " + file.getAbsolutePath());
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            int invalidRows = 0;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // 跳过标题行
                    continue;
                }
                try {
                    AccountModel account = AccountModel.fromCSV(line);
                    if (account != null) {
                        accounts.add(account);
                    }
                } catch (IllegalArgumentException e) {
                    invalidRows++;
                    logger.warn("Skipping invalid CSV line: {}", e.getMessage());
                }
            }
            if (invalidRows > 0) {
                logger.info("Skipped {} invalid rows while reading CSV", invalidRows);
            }
            if (accounts.isEmpty() && !firstLine) {
                logger.info("No valid accounts found in CSV file");
            }
        }
        return accounts;
    }
}