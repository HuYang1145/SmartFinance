package Controller;

import javax.swing.JOptionPane;

import Model.User;
import Model.UserSession;
import Repository.AccountRepository;
import Repository.TransactionRepository;
import Service.PersonChartDataService;
import Service.PersonFinancialService;
import View.LoginAndMain.Login;
import View.LoginAndMain.MainPlane;
import Controller.AIController;

public class LoginController {
    private final AccountRepository accountRepository;

    public LoginController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void handleLogin(String username, String password, Login loginFrame) {
        User user = accountRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            UserSession.setCurrentUsername(username);
            UserSession.setCurrentAccount(user);
            loginFrame.closeWindow();
            showMainInterface(username);
        } else {
            JOptionPane.showMessageDialog(loginFrame, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void handleRegister(String username, String password, String phone, String email,
                               String gender, String address, String accountType, Login loginFrame) {
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(loginFrame, "Username and password are required", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User existingUser = accountRepository.findByUsername(username);
        if (existingUser != null) {
            JOptionPane.showMessageDialog(loginFrame, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User newUser = new User(
                username, password, phone, email, gender, address,
                java.time.LocalDateTime.now().toString(),
                User.AccountStatus.ACTIVE, accountType, 0.0
        );
        accountRepository.save(newUser);
        JOptionPane.showMessageDialog(loginFrame, "Registration successful! Please log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
        loginFrame.switchToLoginPanel();
    }

    private void showMainInterface(String username) {
        // 创建依赖
        TransactionRepository transactionRepository = new TransactionRepository();
        PersonFinancialService financialService = new PersonFinancialService(transactionRepository);
        PersonChartDataService chartDataService = new PersonChartDataService(transactionRepository);
        PersonCenterController personCenterController = new PersonCenterController(financialService, chartDataService);
        BillController billController = new BillController(accountRepository);

        // 显示 MainPlane
        new MainPlane(personCenterController, billController).setVisible(true);
    }
}