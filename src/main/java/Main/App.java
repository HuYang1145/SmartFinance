package Main;

import javax.swing.SwingUtilities;

import AccountController.AccountManagementController;
import View.AccountManagementUI;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AccountManagementController controller = new AccountManagementController();
            new AccountManagementUI(controller);
        });
    }
}