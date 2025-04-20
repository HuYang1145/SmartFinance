package Main;

import javax.swing.SwingUtilities;

import UI.AccountManagementController;
import UI.AccountManagementUI;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AccountManagementController controller = new AccountManagementController();
            new AccountManagementUI(controller);
        });
    }
}