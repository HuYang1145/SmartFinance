package Service;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import Model.Transaction;
import Repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class PersonFinancialServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    private PersonFinancialService service;

    private static MockedStatic<TransactionRepository> transactionRepositoryMock;

    @BeforeEach
    void setUp() {
        service = new PersonFinancialService(transactionRepository);
        transactionRepositoryMock = mockStatic(TransactionRepository.class);
    }

    @AfterEach
    void tearDown() {
        transactionRepositoryMock.close();
    }

    @Test
    void testCalculateFinancialSummary_Success() {
        // Arrange
        String username = "testUser";
        int selectedYear = 2024;
        int lastYear = 2023;
        List<Transaction> transactions = Arrays.asList(
                new Transaction(username, "Income", 1500.00, "2024/03/05 12:00", "Employer", "Salary",
                        "March salary", "Income-Salary", "Bank Transfer", "Online", "Income",
                        "[Attachment: Payslip]", "Non-recurring"),
                new Transaction(username, "Expense", 88.50, "2024/03/01 12:00", "Ele.me", "Food",
                        "Weekend takeout", "Food-Takeout", "WeChat Pay", "Online", "Essential",
                        "[Attachment: Receipt image]", "Non-recurring"),
                new Transaction(username, "Expense", 300.00, "2024/01/15 12:00", "Landlord", "Rent",
                        "January rent", "Housing-Rent", "WeChat Pay", "Offline", "Essential",
                        "[Attachment: Receipt]", "Recurring"),
                new Transaction(username, "Income", 1000.00, "2023/02/01 12:00", "Freelance", "Freelance",
                        "Freelance work", "Income-Freelance", "PayPal", "Online", "Income",
                        "[Attachment: Invoice]", "Non-recurring"),
                new Transaction(username, "Expense", 200.00, "2023/01/10 12:00", "Store", "Shopping",
                        "Clothes", "Shopping-Retail", "Credit Card", "In-store", "Non-essential",
                        "[Attachment: Receipt]", "Non-recurring")
        );
        transactionRepositoryMock.when(() -> TransactionRepository.readTransactions(username))
                .thenReturn(transactions);

        // Act
        PersonFinancialService.FinancialSummary result = service.calculateFinancialSummary(username, selectedYear);

        // Assert
        assertEquals(1500.00, result.getTotalIncomeYear(), 0.001);
        assertEquals(388.50, result.getTotalExpenseYear(), 0.001);
        assertEquals(1911.50, result.getAccountBalance(), 0.001); // 1500 + 1000 - 88.5 - 300 - 200
        assertEquals(50.0, result.getIncomeChangeYear(), 0.001); // (1500 - 1000) / 1000 * 100
        assertEquals(94.25, result.getExpenseChangeYear(), 0.001); // (388.5 - 200) / 200 * 100
        assertEquals(1111.50, result.getTotalBalanceYear(), 0.001); // 1500 - 388.5

        transactionRepositoryMock.verify(() -> TransactionRepository.readTransactions(username));
    }

    @Test
    void testCalculateFinancialSummary_NoTransactions() {
        // Arrange
        String username = "testUser";
        int selectedYear = 2024;
        transactionRepositoryMock.when(() -> TransactionRepository.readTransactions(username))
                .thenReturn(Arrays.asList());

        // Act
        PersonFinancialService.FinancialSummary result = service.calculateFinancialSummary(username, selectedYear);

        // Assert
        assertEquals(0.0, result.getTotalIncomeYear(), 0.001);
        assertEquals(0.0, result.getTotalExpenseYear(), 0.001);
        assertEquals(0.0, result.getAccountBalance(), 0.001);
        assertEquals(0.0, result.getIncomeChangeYear(), 0.001);
        assertEquals(0.0, result.getExpenseChangeYear(), 0.001);
        assertEquals(0.0, result.getTotalBalanceYear(), 0.001);

        transactionRepositoryMock.verify(() -> TransactionRepository.readTransactions(username));
    }


    @Test
    void testGeneratePaymentLocationSummary_Success() {
        // Arrange
        String username = "testUser";
        int selectedYear = 2024;
        List<Transaction> transactions = Arrays.asList(
                new Transaction(username, "Expense", 88.50, "2024/03/01 12:00", "Ele.me", "Food",
                        "Weekend takeout", "Food-Takeout", "WeChat Pay", "Online", "Essential",
                        "[Attachment: Receipt image]", "Non-recurring"),
                new Transaction(username, "Expense", 300.00, "2024/01/15 12:00", "Landlord", "Rent",
                        "January rent", "Housing-Rent", "WeChat Pay", "Offline", "Essential",
                        "[Attachment: Receipt]", "Recurring"),
                new Transaction(username, "Expense", 50.00, "2024/02/10 12:00", "Store", "Shopping",
                        "Clothes", "Shopping-Retail", "Credit Card", "In-store", "Non-essential",
                        "[Attachment: Receipt]", "Non-recurring"),
                new Transaction(username, "Income", 1500.00, "2024/03/05 12:00", "Employer", "Salary",
                        "March salary", "Income-Salary", "Bank Transfer", "Online", "Income",
                        "[Attachment: Payslip]", "Non-recurring"),
                new Transaction(username, "Expense", 200.00, "2023/01/10 12:00", "Store", "Shopping",
                        "Clothes", "Shopping-Retail", "Credit Card", "In-store", "Non-essential",
                        "[Attachment: Receipt]", "Non-recurring") // 不同年份，应忽略
        );
        transactionRepositoryMock.when(() -> TransactionRepository.readTransactions(username))
                .thenReturn(transactions);

        // Act
        String result = service.generatePaymentLocationSummary(username, selectedYear);

        // Assert
        String expected = String.format(
                "Summary:\n" +
                "- Most transactions were made via WeChat Pay (%.1f%%).\n" +
                "- Primary transaction locations were Offline (%.1f%%).\n" +
                "- Total transactions: %d\n" +
                "- Highest single transaction: ¥%.2f (%s)",
                (388.50 / 438.50) * 100, // WeChat Pay: 88.5 + 300
                (300.0 / 438.50) * 100, // Offline: 300
                3, // 3 transactions
                300.00, // Max transaction
                "Housing-Rent"
        );
        assertEquals(expected, result);

        transactionRepositoryMock.verify(() -> TransactionRepository.readTransactions(username));
    }

    @Test
    void testGeneratePaymentLocationSummary_NoExpenses() {
        // Arrange
        String username = "testUser";
        int selectedYear = 2024;
        List<Transaction> transactions = Arrays.asList(
                new Transaction(username, "Income", 1500.00, "2024/03/05 12:00", "Employer", "Salary",
                        "March salary", "Income-Salary", "Bank Transfer", "Online", "Income",
                        "[Attachment: Payslip]", "Non-recurring")
        );
        transactionRepositoryMock.when(() -> TransactionRepository.readTransactions(username))
                .thenReturn(transactions);

        // Act
        String result = service.generatePaymentLocationSummary(username, selectedYear);

        // Assert
        String expected = String.format(
                "Summary:\n" +
                "- Most transactions were made via No payment data.\n" +
                "- Primary transaction locations were No location data.\n" +
                "- Total transactions: %d\n" +
                "- Highest single transaction: ¥%.2f (%s)",
                0, 0.0, "None"
        );
        assertEquals(expected, result);

        transactionRepositoryMock.verify(() -> TransactionRepository.readTransactions(username));
    }

}