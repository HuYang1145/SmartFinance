package Main;

import javax.swing.SwingUtilities;

import AccountModel.AccountManagementController;
import UI.AccountManagementUI;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AccountManagementController controller = new AccountManagementController();
            new AccountManagementUI(controller);
        });
    }
}