package Service;

import Model.Transaction;
import Model.User;
import Repository.TransactionRepository;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    private TransactionRepository mockTxRepo;
    private BudgetService mockBudgetService;
    private TransactionService txService;
    private User user;

    @BeforeEach
    void setUp() {
        mockTxRepo = mock(TransactionRepository.class);
        mockBudgetService = mock(BudgetService.class);
        txService = new TransactionService(mockTxRepo, mockBudgetService);
        user = mock(User.class);
        when(user.getBalance()).thenReturn(888.0);
        when(user.getUsername()).thenReturn("testUser");
    }

    @Test
    void getBalance_userNull_returnsZero() {
        assertEquals(0.0, txService.getBalance(null));
    }

    @Test
    void getBalance_userNotNull_returnsBalance() {
        assertEquals(888.0, txService.getBalance(user));
    }

    @Test
    void buildTransactionSummary_userNullOrNoTx_returnsEmpty() {
        assertEquals("", txService.buildTransactionSummary(null));
        when(mockTxRepo.findTransactionsByUser(user)).thenReturn(Collections.emptyList());
        assertEquals("", txService.buildTransactionSummary(user));
    }

    @Test
    void buildTransactionSummary_validUser_returnsCSV() {
        List<Transaction> txs = List.of(
                new Transaction("u", "Expense", 22.5, "2024/05/01 12:34", "shop", "Pay", "", "Food", "", "", "", "", ""),
                new Transaction("u", "Income", 2000, "2024/05/03 10:20", "boss", "Salary", "", "Work", "", "", "", "", "")
        );
        when(mockTxRepo.findTransactionsByUser(user)).thenReturn(txs);

        String csv = txService.buildTransactionSummary(user);
        assertTrue(csv.contains("Operation,Amount,Time,Merchant/Payee,Type,Category"));
        assertTrue(csv.contains("Expense,22.50,2024/05/01 12:34,shop,Pay,Food"));
        assertTrue(csv.contains("Income,2000.00,2024/05/03 10:20,boss,Salary,Work"));
    }

    @Test
    void getCurrentMonthExpense_returnsCorrectTotal() {
        LocalDate now = LocalDate.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + " 10:00";
        List<Transaction> txs = List.of(
                new Transaction("u", "Expense", 300.0, dateStr, "", "", "", "", "", "", "", "", "")
        );
        when(mockTxRepo.findTransactionsByUser(user)).thenReturn(txs);

        double result = txService.getCurrentMonthExpense(user);
        assertEquals(300.0, result, 0.0001);

    }

    @Test
    void checkAbnormalTransactions_allPatternsDetected() {
        // Pattern 1: 一天内有 3 笔 >=5000
        // Pattern 2: 单笔大额支出 > 3*avg_daily
        // Pattern 3: 单笔支出 > 50000
        when(mockBudgetService.calculateAverageDailyExpense(anyString(), anyInt())).thenReturn(100.0);

        List<Transaction> txs = new ArrayList<>();
        txs.add(new Transaction("testUser", "Expense", 6000.0, "2024/05/01 10:00", "", "", "", "", "", "", "", "", ""));
        txs.add(new Transaction("testUser", "Expense", 7000.0, "2024/05/01 11:00", "", "", "", "", "", "", "", "", ""));
        txs.add(new Transaction("testUser", "Expense", 8000.0, "2024/05/01 12:00", "", "", "", "", "", "", "", "", ""));
        // Pattern 2
        txs.add(new Transaction("testUser", "Expense", 400.0, "2024/05/02 11:00", "", "", "", "", "", "", "", "", "")); // > 3 * 100
        // Pattern 3
        txs.add(new Transaction("testUser", "Expense", 60000.0, "2024/05/03 10:00", "", "", "", "", "", "", "", "", ""));

        List<String> warnings = txService.checkAbnormalTransactions("testUser", txs);
        assertEquals(3, warnings.size());
        assertTrue(warnings.stream().anyMatch(s -> s.contains("Frequent large transactions")));
        assertTrue(warnings.stream().anyMatch(s -> s.contains("exceeded 3.0 times")));
        assertTrue(warnings.stream().anyMatch(s -> s.contains("over ¥50000.0")));
    }

    @Test
    void checkAbnormalTransactions_emptyInput_returnsEmpty() {
        assertTrue(txService.checkAbnormalTransactions(null, null).isEmpty());
        assertTrue(txService.checkAbnormalTransactions("u", Collections.emptyList()).isEmpty());
    }

}
