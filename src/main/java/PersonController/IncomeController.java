package PersonController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.SwingUtilities;

import AccountModel.AccountModel;
import AccountModel.TransactionServiceModel;
import AccountModel.UserRegistrationCSVExporterModel;
import AccountModel.UserSessionModel;
import View.IncomeDialogView;

public class IncomeController {
    private final IncomeDialogView view;
    private final String currentUsername;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    public IncomeController(IncomeDialogView view, String currentUsername) {
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
        view.getConfirmButton().addActionListener(e -> processIncome());
        view.getCancelButton().addActionListener(e -> view.dispose());
        view.getPasswordField().addActionListener(e -> processIncome());
    }

    private void processIncome() {
        String amountText = view.getAmountField().getText();
        String timeText = view.getTimeField().getText();
        String password = new String(view.getPasswordField().getPassword());

        if (amountText.isEmpty() || timeText.isEmpty() || password.isEmpty()) {
            view.showError("All fields must be filled.");
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

        try {
            DATE_FORMAT.setLenient(false);
            DATE_FORMAT.parse(timeText.trim());
        } catch (ParseException ex) {
            view.showError("Invalid time format. Use yyyy/MM/dd HH:mm");
            return;
        }

        List<AccountModel> accounts = UserRegistrationCSVExporterModel.readFromCSV();
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

        boolean transactionAdded = TransactionServiceModel.addTransaction(
            currentUsername, "Income", amount, timeText.trim(), "I", "I"
        );

        if (transactionAdded) {
            currentUserAccount.setBalance(currentUserAccount.getBalance() + amount);
            boolean saved = UserRegistrationCSVExporterModel.saveToCSV(accounts, false);

            if (saved) {
                UserSessionModel.setCurrentAccount(currentUserAccount);
                view.showSuccess("Income of ¥" + String.format("%.2f", amount) + " added successfully!");
                view.dispose();
            } else {
                view.showError("Income recorded, but failed to update account balance file.");
            }
        } else {
            view.showError("Failed to add income.");
        }
        view.clearPassword();
    }
}