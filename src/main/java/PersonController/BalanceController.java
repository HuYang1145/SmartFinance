package PersonController;

import java.util.List;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AccountModel.AccountModel;
import AccountModel.AccountRepository;
import View.BalanceDialogView;

/**
 * Controller for handling the logic of displaying the user's account balance.
 */
public class BalanceController {

    private static final Logger logger = LoggerFactory.getLogger(BalanceController.class);

    private static final String ERROR_ACCOUNT_NOT_FOUND = "Account information not found for this username.";
    private static final String ERROR_INVALID_USERNAME = "Invalid username.";
    private static final String BALANCE_FORMAT = "Your account balance is: ¥%.2f";

    private final BalanceDialogView view;
    private final String username;

    /**
     * Constructs a balance controller to manage the balance view dialog.
     *
     * @param view     The balance view dialog.
     * @param username The username to query the balance for.
     */
    public BalanceController(BalanceDialogView view, String username) {
        this.view = view;
        this.username = username;
        initialize();
    }

    /**
     * Initializes the dialog by querying the account and setting up event listeners.
     */
    private void initialize() {
        if (username == null || username.trim().isEmpty()) {
            logger.error("Invalid username provided.");
            view.setBalanceText(ERROR_INVALID_USERNAME);
        } else {
            AccountModel account = findAccount(username);
            if (account != null) {
                view.setBalanceText(String.format(BALANCE_FORMAT, account.getBalance()));
                logger.debug("Balance retrieved for user {}: ¥{}", username, account.getBalance());
            } else {
                view.setBalanceText(ERROR_ACCOUNT_NOT_FOUND);
                logger.warn("No account found for user {}", username);
            }
        }

        view.getCloseButton().addActionListener(e -> {
            SwingUtilities.invokeLater(view::close);
        });

        view.setVisible(true);
    }

    /**
     * Finds the account for the given username.
     *
     * @param username The username to search for.
     * @return The AccountModel if found, null otherwise.
     */
    private AccountModel findAccount(String username) {
        try {
            List<AccountModel> accounts = AccountRepository.readFromCSV();
            for (AccountModel account : accounts) {
                if (account.getUsername().equals(username)) {
                    return account;
                }
            }
        } catch (Exception e) {
            logger.error("Error reading accounts for user {}: {}", username, e.getMessage());
        }
        return null;
    }
}