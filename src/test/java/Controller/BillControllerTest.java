package Controller;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import Model.Transaction;
import Model.User;
import Model.UserSession;
import Repository.AccountRepository;
import View.Bill.ExpenseDialogView;
import View.Bill.IncomeDialogView;

@ExtendWith(MockitoExtension.class)
class BillControllerTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ExpenseDialogView expenseDialogView;

    @Mock
    private IncomeDialogView incomeDialogView;

    private BillController controller;

    private static MockedStatic<TransactionController> transactionControllerMock;
    private static MockedStatic<UserSession> userSessionMock;

    @BeforeEach
    void setUp() {
        transactionControllerMock = Mockito.mockStatic(TransactionController.class);
        userSessionMock = Mockito.mockStatic(UserSession.class);
        controller = new BillController(accountRepository);
    }

    @AfterEach
    void tearDown() {
        transactionControllerMock.close();
        userSessionMock.close();
    }

    @Test
    void testGetCachedTransactions_ValidCache() throws Exception {
        String username = "testUser";
        List<Transaction> transactions = Arrays.asList(
                new Transaction("testUser", "Expense", 88.50, "2024/03/01", "Ele.me", "Food",
                        "Weekend takeout", "Food-Takeout", "WeChat Pay", "Online", "Essential",
                        "[Attachment: Receipt image]", "Non-recurring")
        );

        // Set up cache via reflection
        Field cacheField = BillController.class.getDeclaredField("transactionCache");
        cacheField.setAccessible(true);
        Map<String, List<Transaction>> cache = new HashMap<>();
        cache.put(username, transactions);
        cacheField.set(null, cache);

        Field timestampField = BillController.class.getDeclaredField("cacheTimestamps");
        timestampField.setAccessible(true);
        Map<String, Long> timestamps = new HashMap<>();
        timestamps.put(username, System.currentTimeMillis());
        timestampField.set(null, timestamps);

        List<Transaction> result = controller.getCachedTransactions(username);
        assertEquals(transactions, result);
        transactionControllerMock.verify(() -> TransactionController.readTransactions(anyString()), never());
    }

    

    @Test
    void testGetFilteredTransactions_Success() throws Exception {
        String username = "testUser";
        List<Transaction> transactions = Arrays.asList(
                new Transaction("testUser", "Expense", 88.50, "2024/03/01", "Ele.me", "Food",
                        "Weekend takeout", "Food-Takeout", "WeChat Pay", "Online", "Essential",
                        "[Attachment: Receipt image]", "Non-recurring"),
                new Transaction("testUser", "Income", 1500.00, "2024/03/05", "Employer", "Salary",
                        "March salary", "Income-Salary", "Bank Transfer", "Online", "Income",
                        "[Attachment: Payslip]", "Non-recurring"),
                new Transaction("testUser", "Expense", 300.00, "2025/01/15", "Landlord", "Rent",
                        "January rent", "Housing-Rent", "WeChat Pay", "Offline", "Essential",
                        "[Attachment: Receipt]", "Recurring")
        );

        transactionControllerMock.when(() -> TransactionController.readTransactions(username))
                .thenReturn(transactions);

        List<Transaction> result = controller.getFilteredTransactions(username, "2024/03", "2024/03");
        assertEquals(2, result.size());
        assertEquals("2024/03/01", result.get(0).getTimestamp());
        assertEquals("2024/03/05", result.get(1).getTimestamp());
    }
    

    @Test
    void testCalculateExpenseCategoryTotals() {
        List<Transaction> transactions = Arrays.asList(
                new Transaction("testUser", "Expense", 88.50, "2024/03/01", "Ele.me", "Food",
                        "Weekend takeout", "Food-Takeout", "WeChat Pay", "Online", "Essential",
                        "[Attachment: Receipt image]", "Non-recurring"),
                new Transaction("testUser", "Expense", 45.20, "2024/03/07", "Taobao", "Shopping",
                        "New clothes", "Shopping-Online", "Alipay", "Online", "Non-essential",
                        "[Attachment: Invoice]", "Non-recurring"),
                new Transaction("testUser", "Income", 1500.00, "2024/03/05", "Employer", "Salary",
                        "March salary", "Income-Salary", "Bank Transfer", "Online", "Income",
                        "[Attachment: Payslip]", "Non-recurring")
        );

        Map<String, Double> totals = controller.calculateExpenseCategoryTotals(transactions, "category");
        assertEquals(88.50, totals.get("Food-Takeout"), 0.001);
        assertEquals(45.20, totals.get("Shopping-Online"), 0.001);
        assertFalse(totals.containsKey("Income-Salary"));
    }

    @Test
    void testCalculateIncomeCategoryTotals() {
        List<Transaction> transactions = Arrays.asList(
                new Transaction("testUser", "Expense", 88.50, "2024/03/01", "Ele.me", "Food",
                        "Weekend takeout", "Food-Takeout", "WeChat Pay", "Online", "Essential",
                        "[Attachment: Receipt image]", "Non-recurring"),
                new Transaction("testUser", "Income", 1500.00, "2024/03/05", "Employer", "Salary",
                        "March salary", "Income-Salary", "Bank Transfer", "Online", "Income",
                        "[Attachment: Payslip]", "Non-recurring"),
                new Transaction("testUser", "Income", 500.00, "2025/02/01", "Freelance", "Freelance",
                        "Freelance work", "Income-Freelance", "PayPal", "Online", "Income",
                        "[Attachment: Invoice]", "Non-recurring")
        );

        Map<String, Double> totals = controller.calculateIncomeCategoryTotals(transactions, "category");
        assertEquals(1500.00, totals.get("Income-Salary"), 0.001);
        assertEquals(500.00, totals.get("Income-Freelance"), 0.001);
        assertFalse(totals.containsKey("Food-Takeout"));
    }

    @Test
void testProcessIncome_IncorrectPassword() throws Exception {
    String username = "testUser";
    User user = new User(username, "pass", "phone", "email", "M", "addr", "ts", User.AccountStatus.ACTIVE, "Personal", 1000.0);
    List<User> accounts = new ArrayList<>(Arrays.asList(user));

    // 提前创建并模拟 JTextField 和 JPasswordField
    javax.swing.JTextField amountField = mock(javax.swing.JTextField.class);
    when(amountField.getText()).thenReturn("1500.00");
    javax.swing.JTextField timeField = mock(javax.swing.JTextField.class);
    when(timeField.getText()).thenReturn("2024/03/05 12:00"); // 添加时间以匹配 DATE_FORMAT
    javax.swing.JPasswordField passwordField = mock(javax.swing.JPasswordField.class);
    when(passwordField.getPassword()).thenReturn("wrong".toCharArray());

    // 模拟 incomeDialogView 返回提前创建的对象
    userSessionMock.when(UserSession::getCurrentUsername).thenReturn(username);
    when(accountRepository.readFromCSV()).thenReturn(accounts);
    when(incomeDialogView.getAmountField()).thenReturn(amountField);
    when(incomeDialogView.getTimeField()).thenReturn(timeField);
    when(incomeDialogView.getPasswordField()).thenReturn(passwordField);

    controller.processIncome(incomeDialogView);

    verify(incomeDialogView).showError("Incorrect password.");
    verify(incomeDialogView, never()).showSuccess(anyString());
}


    

    @Test
    void testGetFieldValue() throws Exception, SecurityException {
        Transaction tx = new Transaction("testUser", "Expense", 88.50, "2024/03/01", "Ele.me", "Food",
                "Weekend takeout", "Food-Takeout", "WeChat Pay", "Online", "Essential",
                "[Attachment: Receipt image]", "Non-recurring");
        Method getFieldValueMethod = BillController.class.getDeclaredMethod("getFieldValue", Transaction.class, String.class);
        getFieldValueMethod.setAccessible(true);

        assertEquals("Food-Takeout", getFieldValueMethod.invoke(controller, tx, "category"));
        assertEquals("WeChat Pay", getFieldValueMethod.invoke(controller, tx, "payment_method"));
        assertEquals("Essential", getFieldValueMethod.invoke(controller, tx, "tag"));
        assertEquals("Unclassified", getFieldValueMethod.invoke(controller, tx, "invalid_field"));
    }

    // Helper methods to mock JTextField and JComboBox
    private javax.swing.JTextField mockTextField(String text) {
        javax.swing.JTextField field = mock(javax.swing.JTextField.class);
        when(field.getText()).thenReturn(text);
        return field;
    }

    private javax.swing.JComboBox<String> mockComboBox(String selected) {
        javax.swing.JComboBox<String> comboBox = mock(javax.swing.JComboBox.class);
        when(comboBox.getSelectedItem()).thenReturn(selected);
        return comboBox;
    }

    private javax.swing.JPasswordField mockPasswordField(String password) {
        javax.swing.JPasswordField field = mock(javax.swing.JPasswordField.class);
        when(field.getPassword()).thenReturn(password.toCharArray());
        return field;
    }
}