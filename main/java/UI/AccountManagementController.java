package UI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import Model.AccountModel;
import Model.AccountValidator;
import Model.AdminAccount;
import Model.PersonalAccount;
import Model.TransactionChecker;
import Model.TransactionModel;
import Model.UserRegistrationCSVExporter;
import Model.UserSession;

public class AccountManagementController {
    public void handleLogin(String username, String password, AccountManagementUI ui) {
        System.out.println("DEBUG: handleLogin - 用户名: '" + username + "', 密码: '" + password + "'");
    
        // 校验用户名和密码是否为空
        if (AccountValidator.isEmpty(username)) {
            ui.showMessage("Username cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (AccountValidator.isEmpty(password)) {
            ui.showMessage("Password cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        // 读取所有账户信息
        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        AccountModel matchedAccount = null;
    
        // 遍历账户列表，查找匹配的账户
        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username) && account.getPassword().equals(password)) {
                matchedAccount = account;
                break;
            }
        }
    
        if (matchedAccount != null) {
            System.out.println("DEBUG: handleLogin - 找到账户: " + username);
    
            // 检查账户是否被冻结
            if ("FROZEN".equalsIgnoreCase(matchedAccount.getAccountStatus())) {
                System.out.println("DEBUG: handleLogin - 账户已冻结，停止登录。");
                ui.showMessage("This account is currently frozen. Please contact the administrator.", "Account Frozen", JOptionPane.ERROR_MESSAGE);
                return;
            }
            System.out.println("DEBUG: handleLogin - 账户状态为 ACTIVE。");
    
            // 登录成功
            ui.showMessage("Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadTransactionsForAccount(matchedAccount);
            UserSession.setCurrentAccount(matchedAccount);
    
            // 检测是否有异常交易
            boolean abnormalDetected = TransactionChecker.hasAbnormalTransactions(matchedAccount);
            if (abnormalDetected) {
                System.out.println("DEBUG: handleLogin - 检测到异常交易，显示警告对话框...");
                ui.showMessage(
                    "Warning: Recent large transfer activity detected on your account. Please review your transaction history.",
                    "Activity Alert",
                    JOptionPane.WARNING_MESSAGE
                );
            }
    
            // 设置当前用户会话
            UserSession.setCurrentUsername(username);
    
            // 打开相应的用户界面
            try {
                if (matchedAccount instanceof AdminAccount) {
                    System.out.println("DEBUG: handleLogin - 打开 AdminUI。");
                    new AdminUI();
                } else if (matchedAccount instanceof PersonalAccount) {
                    System.out.println("DEBUG: handleLogin - 打开 PersonalUI。");
                    new PersonalUI();
                }
                ui.closeWindow();  // 关闭当前窗口
            } catch (Exception ex) {
                System.err.println("ERROR: handleLogin - 打开主界面或关闭登录窗口时发生错误！");
                ex.printStackTrace();
                ui.showMessage("An error occurred while opening the main window.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.out.println("DEBUG: handleLogin - 登录失败：用户名或密码错误。");
            ui.showMessage("Incorrect username or password!", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    
        System.out.println("DEBUG: handleLogin - 方法执行完毕。");
    }
    

    public void handleRegister(String username, String password, String phone, String email, String gender, String address, String selectedAccountType, AccountManagementUI ui) {
        List<AccountModel> accounts = UserRegistrationCSVExporter.readFromCSV();
        for (AccountModel account : accounts) {
            if (account.getUsername().equals(username)) {
                ui.showMessage("Username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (AccountValidator.isEmpty(username) || AccountValidator.isEmpty(password) ||
                AccountValidator.isEmpty(phone) || AccountValidator.isEmpty(email) || AccountValidator.isEmpty(address)) {
            ui.showMessage("All fields cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String creationTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String accountStatus = "ACTIVE";
        double initialBalance = 0.0;

        AccountModel newAccount = null;
        if ("personal".equalsIgnoreCase(selectedAccountType)) {
            newAccount = new PersonalAccount(username, password, phone, email, gender, address, creationTime, accountStatus, "personal", initialBalance);
        } else if ("Admin".equalsIgnoreCase(selectedAccountType)) {
            newAccount = new AdminAccount(username, password, phone, email, gender, address, creationTime, accountStatus, "Admin", initialBalance);
        }

        if (newAccount != null) {
            List<AccountModel> accountListToAdd = new ArrayList<>();
            accountListToAdd.add(newAccount);
            UserRegistrationCSVExporter.saveToCSV(accountListToAdd, true);
            ui.showMessage("Account created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            ui.switchToLoginPanel();
        } else {
            ui.showMessage("Invalid account type selected!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTransactionsForAccount(AccountModel account) {
        if (account == null) return;
        String username = account.getUsername();
        String transactionFilePath = "transactions.csv";

        account.getTransactions().clear();

        File file = new File(transactionFilePath);
        if (!file.exists()) {
            System.err.println("ERROR: loadTransactionsForAccount - 交易文件未找到: " + transactionFilePath);
            return;
        }
        System.out.println("DEBUG: loadTransactionsForAccount - 开始读取文件: " + transactionFilePath + " 为用户: " + username);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            String header = br.readLine();
            if (header == null) {
                System.err.println("ERROR: loadTransactionsForAccount - " + transactionFilePath + " 为空或只有头部。");
                return;
            }

            int loadedCount = 0;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] data = line.split(",");
                if (data.length >= 5) {
                    String csvUsername = data[0].trim();
                    if (csvUsername.equalsIgnoreCase(username)) {
                        try {
                            String transactionId = System.currentTimeMillis() + "_" + ((int)(Math.random() * 1000));
                            String type = data[1].trim();
                            double amount = Double.parseDouble(data[2].trim());
                            String timestamp = data[3].trim();
                            String descriptionOrMerchant = data[4].trim();

                            if (data.length > 5) {
                                StringBuilder sb = new StringBuilder(descriptionOrMerchant);
                                for (int i = 5; i < data.length; i++) {
                                    sb.append(",").append(data[i]);
                                }
                                descriptionOrMerchant = sb.toString().trim();
                                if (descriptionOrMerchant.startsWith("\"") && descriptionOrMerchant.endsWith("\"") && descriptionOrMerchant.length() >= 2) {
                                    descriptionOrMerchant = descriptionOrMerchant.substring(1, descriptionOrMerchant.length() - 1).replace("\"\"", "\"");
                                }
                            }

                            String relatedUser = null;
                            TransactionModel tx = new TransactionModel(
                                    transactionId, username, type, amount, timestamp, descriptionOrMerchant, relatedUser
                            );
                            account.addTransaction(tx);
                            loadedCount++;
                        } catch (NumberFormatException ex) {
                            System.err.println("ERROR: loadTransactionsForAccount - 跳过交易，金额解析错误: " + line + " | 错误: " + ex.getMessage());
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            System.err.println("ERROR: loadTransactionsForAccount - 跳过交易，字段不足: " + line + " | 错误: " + ex.getMessage());
                        } catch (Exception ex) {
                            System.err.println("ERROR: loadTransactionsForAccount - 跳过交易，发生意外错误: " + line + " | 错误: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }
                } else {
                    System.err.println("WARN: loadTransactionsForAccount - 跳过交易，列数不足: " + line);
                }
            }
            System.out.println("DEBUG: loadTransactionsForAccount - 为用户 " + username + " 加载了 " + loadedCount + " 条交易记录。");
        } catch (IOException e) {
            System.err.println("ERROR: loadTransactionsForAccount - 读取交易文件 '" + transactionFilePath + "' 时发生错误: " + e.getMessage());
        }
    }
}