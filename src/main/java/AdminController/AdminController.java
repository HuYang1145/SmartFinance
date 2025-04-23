package AdminController;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AdminModel.AccountRepositoryModel;
import View.AdminAccountView;

public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final AccountRepositoryModel accountRepository;

    public AdminController(AccountRepositoryModel accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void initialize() {
        new AdminAccountView(this::handleQuery);
    }

    private void handleQuery(AdminAccountView view) {
        try {
            view.updateTable(accountRepository.readFromCSV());
        } catch (IOException e) {
            logger.error("Error reading accounts.csv", e);
            view.showError("Error reading accounts.csv: " + e.getMessage());
        }
    }
}