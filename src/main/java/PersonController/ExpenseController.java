package PersonController;

import AccountController.TransactionService;
import AccountController.UserRegistrationCSVExporter;
import AccountModel.AccountModel;
import AccountModel.PersonalAccount;
import AccountModel.UserSession;
import UI.ExpenseDialogView;

import javax.swing.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Controller for handling expense addition logic, coordinating ExpenseDialogView and Model interactions.
 */
public class ExpenseController {
    private final ExpenseDialogView view;
    private final String currentUsername;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    private static final String UNCLASSIFIED_TYPE_CODE = "u";
    private static final String INCOME_PLACEHOLDER = "I";

    /**
     * Constructs the expense controller and initializes event listeners.
     *
     * @param view            The expense dialog view.
     * @param currentUsername The username of the current user.
     */
    public ExpenseController(ExpenseDialogView view, String currentUsername) {
        this.view = view;
        this.currentUsername = currentUsername;

        if (currentUsername == null) {
            view.showError("Error: Not logged in.");
            SwingUtilities.invokeLater(view::dispose);
            return;
        }

        addListeners();
    }

    private void addListeners() {
        view.getConfirmButton().addActionListener(e -> processExpense());
        view.getCancelButton().addActionListener(e -> view.dispose());
        view.getPasswordField().addActionListener(e -> processExpense());
    }

    private void processExpense() {
        String amountText = view.getAmountField().getText();
        String timeText = view.getTimeField().getText();
        String merchantText = view.getMerchantField().getText();
        String selectedType = (String) view.getTypeComboBox().getSelectedItem();
        String password = new String(view.getPasswordField().getPassword());

        // Input validation
        if (amountText.isEmpty() || timeText.isEmpty() || merchantText.isEmpty() || password.isEmpty()) {
            view.showError("Amount, Time, Merchant, and Password must be filled.");
            return;
        }

        if (INCOME_PLACEHOLDER.equalsIgnoreCase(merchantText.trim())) {
            view.showError("Invalid merchant name.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                view.showError("Amount must be positive.");
                return;
            }
        } catch (NumberFormatException ex) {
            view.showError("Invalid amount format.");
            return;
        }

        // Validate date format
        try {
            DATE_FORMAT.setLenient(false);
            DATE_FORMAT.parse(timeText.trim());
        } catch (ParseException ex) {
            view.showError("Invalid time format. Use yyyy/MM/dd HH:mm");
            return;
        }

        // Determine the type to record
        String typeToRecord = UNCLASSIFIED_TYPE_CODE;
        if (selectedType != null && !selectedType.equals("(Select Type)")) {
            typeToRecord = selectedType;
        }
        if (INCOME_PLACEHOLDER.equalsIgnoreCase(typeToRecord)) {
            view.showError("Invalid expense type selected.");
            return;
        }

        // Account verification and update
        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        AccountModel currentUserAccount = null;
        boolean passwordCorrect = false;

        for (AccountModel account : accounts) {
            if (account.getUsername().equals(currentUsername)) {
                currentUserAccount = account;
                if (account.getPassword().equals(password)) {
                    passwordCorrect = true;
                }
                break;
            }
        }

        if (currentUserAccount == null) {
            view.showError("Current user account not found.");
            return;
        }

        if (!passwordCorrect) {
            view.showError("Incorrect password.");
            view.clearPassword();
            return;
        }

        if (!(currentUserAccount instanceof PersonalAccount)) {
            view.showError("This account type does not support expense operations.");
            return;
        }

        if (currentUserAccount.getBalance() < amount) {
            view.showError("Insufficient balance for this expense.");
            view.clearPassword();
            return;
        }

        // Add transaction and update balance
        boolean transactionAdded = TransactionService.addTransaction(
            currentUsername, "Expense", amount, timeText.trim(), merchantText.trim(), typeToRecord
        );

        if (transactionAdded) {
            double originalBalance = currentUserAccount.getBalance();
            currentUserAccount.setBalance(originalBalance - amount);
            boolean saved = UserRegistrationCSVExporter.saveToCSV(accounts, false);

            if (saved) {
                UserSession.setCurrentAccount(currentUserAccount);
                view.showSuccess("Successfully added expense of ¥" + String.format("%.2f", amount) + "!");
                view.dispose();
            } else {
                // Rollback transaction
                TransactionService.removeTransaction(currentUsername, timeText.trim());
                currentUserAccount.setBalance(originalBalance);
                view.showError("Expense recorded successfully, but failed to update account balance file.");
            }
        } else {
            view.showError("Failed to add expense.");
        }
        view.clearPassword();
    }
}