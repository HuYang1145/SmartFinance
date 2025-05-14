package Service;

import Model.Transaction;
import Repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

// --- Mockito Imports ---
// Make sure these static imports are present and correct in your actual file
import static org.mockito.Mockito.mock; // Optional, if you mock manually
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
// --- End Mockito Imports ---


// --- JUnit 5 Imports ---
import static org.junit.jupiter.api.Assertions.*;
// --- End JUnit 5 Imports ---


class TransactionServiceTest {

    // We need to mock the dependencies of TransactionService
    @Mock // Ensure this annotation is resolved (org.mockito.Mock)
    private TransactionRepository transactionRepository;
    @Mock // Ensure this annotation is resolved (org.mockito.Mock)
    private BudgetService budgetService;

    // Inject the mocked dependencies into the TransactionService instance we are testing
    @InjectMocks // Ensure this annotation is resolved (org.mockito.InjectMocks)
    private TransactionService transactionService;

    // Formatter for creating transaction timestamps in "yyyy/MM/dd HH:mm" format
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd"); // Formatter for date part used in warning messages

    // Define constants from TransactionService for clarity and easier testing
    private static final double FREQUENT_LARGE_TRANSACTION_AMOUNT = 5000.0;
    private static final int FREQUENT_LARGE_TRANSACTION_COUNT = 3;
    private static final double LARGE_EXPENSE_MULTIPLIER = 3.0;
    private static final double LARGE_TRANSFER_OR_EXPENSE_AMOUNT = 50000.0; // Using the same constant name as in TransactionService


    @BeforeEach
    void setUp() {
        // Initialize mocks before each test method
        // Ensure MockitoAnnotations is resolved (org.mockito.MockitoAnnotations)
        MockitoAnnotations.openMocks(this);
        // Note: The TransactionService constructor injects the mocks when @InjectMocks is used.
    }

    // Helper method to create a list of transactions
    private List<Transaction> createTransactions(Transaction... transactions) {
        return new ArrayList<>(Arrays.asList(transactions));
    }

    // Helper method to create a transaction with core fields
    // Accepts LocalDateTime to make creating timestamps easier in tests
    private Transaction createTransaction(String username, String operation, double amount, LocalDateTime timestamp, String type, String category) {
         // Use the formatter to convert LocalDateTime to String for the Transaction constructor
        return new Transaction(username, operation, amount, timestamp.format(formatter), "", type, "", category, "", "", "", "", "");
    }

     // Helper method to create a transaction with core fields, accepting String timestamp
      private Transaction createTransaction(String username, String operation, double amount, String timestamp, String type, String category) {
           // Directly use the string timestamp
         return new Transaction(username, operation, amount, timestamp, "", type, "", category, "", "", "", "", "");
      }


    // --- Test Cases for checkAbnormalTransactions (Historical Check) ---

    @Test
    @DisplayName("Historical: Should warn for frequent large transactions (>=5000, >=3) on the same day")
    void checkAbnormalTransactions_FrequentLargeTransactionsSameDay_ShouldWarn() {
        // Arrange
        String username = "testUser";
        LocalDate today = LocalDate.now();
        // Create 3 large transactions (>=FREQUENT_LARGE_TRANSACTION_AMOUNT) on the same day
        LocalDateTime timestamp1 = today.atTime(10, 0);
        LocalDateTime timestamp2 = today.atTime(11, 0);
        LocalDateTime timestamp3 = today.atTime(12, 0); // Triggers >= 3 count for today
        LocalDateTime timestampTomorrow = today.plusDays(1).atTime(9, 0);

        List<Transaction> transactions = createTransactions(
                createTransaction(username, "Income", FREQUENT_LARGE_TRANSACTION_AMOUNT, timestamp1, "Salary", "Income"), // Large transaction 1 (>=5000)
                createTransaction(username, "Expense", FREQUENT_LARGE_TRANSACTION_AMOUNT + 1000, timestamp2, "Shopping", "Expense"), // Large transaction 2 (>=5000)
                createTransaction(username, "Income", FREQUENT_LARGE_TRANSACTION_AMOUNT + 2000, timestamp3, "Gift", "Income"),     // Large transaction 3 (>=5000)
                // Add one more transaction on a different day to ensure logic handles multiple days
                createTransaction(username, "Expense", 100.0, today.minusDays(1).atTime(9, 0), "Food", "Expense")
        );

        // Mock BudgetService: Set average daily expense such that individual large transactions don't trigger Pattern 2
        // FREQUENT_LARGE_TRANSACTION_AMOUNT (5000) < 3 * avg => avg > 5000 / 3 ≈ 1666.67
        when(budgetService.calculateAverageDailyExpense(username, 3)).thenReturn(2000.0); // Mock the method call

        // Act
        List<String> warnings = transactionService.checkAbnormalTransactions(username, transactions);

        // Assert
        // Check if there is a warning containing "Frequent large transactions" and today's date formatted correctly
        boolean frequentWarningFound = warnings.stream()
                .anyMatch(w -> w.contains("Frequent large transactions") && w.contains(today.format(dateFormatter))); // Use dateFormatter for checking warning message format

        assertTrue(frequentWarningFound, "Should warn about frequent large transactions on the same day.");
        // In this specific setup, only Pattern 1 should be triggered.
        // Check that only one warning related to frequent large transaction on that specific day is present.
        long frequentWarningsCount = warnings.stream()
            .filter(w -> w.contains("Frequent large transactions") && w.contains(today.format(dateFormatter)))
            .count();
        assertEquals(1, frequentWarningsCount, "Should have exactly one warning line for frequent large transactions on today.");

        // Also check that no other warnings are present for this specific setup
        long otherWarningsCount = warnings.size() - frequentWarningsCount;
         assertEquals(0, otherWarningsCount, "Should not have other warning types in this specific scenario.");
    }

    @Test
    @DisplayName("Historical: Should not warn for frequent large transactions on different days")
    void checkAbnormalTransactions_FrequentLargeTransactionsDifferentDays_ShouldNotWarn() {
         // Arrange
         String username = "testUser";
         LocalDate today = LocalDate.now();
         // Create 3 large transactions (>=5000), each on a different day
         List<Transaction> transactions = createTransactions(
                 createTransaction(username, "Income", FREQUENT_LARGE_TRANSACTION_AMOUNT, today.atTime(10, 0), "Salary", "Income"),      // Day 1: 1 large
                 createTransaction(username, "Expense", FREQUENT_LARGE_TRANSACTION_AMOUNT + 1000, today.plusDays(1).atTime(11, 0), "Shopping", "Expense"), // Day 2: 1 large
                 createTransaction(username, "Income", FREQUENT_LARGE_TRANSACTION_AMOUNT + 2000, today.plusDays(2).atTime(12, 0), "Gift", "Income"),      // Day 3: 1 large
                 createTransaction(username, "Expense", 100.0, today.atTime(9, 0), "Food", "Expense") // Other transaction on Day 1
         );

         // Mock BudgetService: Set average daily expense to avoid P2 trigger
         when(budgetService.calculateAverageDailyExpense(username, 3)).thenReturn(2000.0);

         // Act
         List<String> warnings = transactionService.checkAbnormalTransactions(username, transactions);

         // Assert
         assertTrue(warnings.isEmpty(), "Should not warn about frequent large transactions on different days.");
    }

     @Test
     @DisplayName("Historical: Should not warn for transactions below frequent large threshold")
     void checkAbnormalTransactions_BelowFrequentLargeThreshold_ShouldNotWarn() {
         // Arrange
         String username = "testUser";
         LocalDate today = LocalDate.now();
         // Create 3 transactions on the same day, all below FREQUENT_LARGE_TRANSACTION_AMOUNT (5000)
         List<Transaction> transactions = createTransactions(
                 createTransaction(username, "Income", FREQUENT_LARGE_TRANSACTION_AMOUNT - 0.01, today.atTime(10, 0), "Salary", "Income"), // Amount < 5000
                 createTransaction(username, "Expense", 4000.0, today.atTime(11, 0), "Shopping", "Expense"), // Amount < 5000
                 createTransaction(username, "Income", 3000.0, today.atTime(12, 0), "Gift", "Income") // Amount < 5000
         );

         // Mock BudgetService: Set average daily expense high enough so Pattern 2 is not triggered
         // Max amount is 4999.99. Need 4999.99 < 3 * avg => avg > 4999.99 / 3 ≈ 1666.66
         when(budgetService.calculateAverageDailyExpense(username, 3)).thenReturn(2000.0); // Example: 4999.99 < 3 * 2000

         // Act
         List<String> warnings = transactionService.checkAbnormalTransactions(username, transactions);

         // Assert
         assertTrue(warnings.isEmpty(), "Should not warn if transaction amounts are below the frequent large threshold.");
     }


    @Test
    @DisplayName("Historical: Should warn for single expense > 3 times average daily")
    void checkAbnormalTransactions_ExpenseGreaterThanThreeTimesAverage_ShouldWarn() {
        // Arrange
        String username = "testUser";
        double averageDaily = 200.0; // Mock average daily expense
        double largeExpenseAmount = averageDaily * LARGE_EXPENSE_MULTIPLIER + 0.01; // Amount > 3 * averageDaily

        List<Transaction> transactions = createTransactions(
                createTransaction(username, "Expense", largeExpenseAmount, LocalDate.now().atTime(10, 0), "Shopping", "Expense"), // Large expense
                createTransaction(username, "Income", 1000.0, LocalDate.now().minusDays(5).atTime(9, 0), "Salary", "Income") // Other transaction
        );

        when(budgetService.calculateAverageDailyExpense(username, 3)).thenReturn(averageDaily); // Mock the method call

        // Act
        List<String> warnings = transactionService.checkAbnormalTransactions(username, transactions);

        // Assert
        boolean largeExpenseWarningFound = warnings.stream()
                .anyMatch(w -> w.contains("single expense transaction") && w.contains("exceeded") && w.contains(String.format("%.1f times", LARGE_EXPENSE_MULTIPLIER)));

        assertTrue(largeExpenseWarningFound, "Should warn about single expense exceeding 3 times average daily.");
         // In this specific setup, only P2 should trigger.
         // Check that only one warning related to exceeding average is present
         long avgWarningsCount = warnings.stream()
              .filter(w -> w.contains("single expense transaction") && w.contains("exceeded"))
              .count();
         assertEquals(1, avgWarningsCount, "Should have exactly one warning for exceeding average daily.");

         long totalWarningsCount = warnings.size();
         assertEquals(1, totalWarningsCount, "Should only have one warning type in this specific scenario.");
    }

     @Test
     @DisplayName("Historical: Should not warn for single expense <= 3 times average daily")
     void checkAbnormalTransactions_ExpenseLessThanThreeTimesAverage_ShouldNotWarn() {
         // Arrange
         String username = "testUser";
         double averageDaily = 200.0; // Mock average daily expense
         double normalExpenseAmount = averageDaily * LARGE_EXPENSE_MULTIPLIER - 0.01; // Amount <= 3 * averageDaily

         List<Transaction> transactions = createTransactions(
                 createTransaction(username, "Expense", normalExpenseAmount, LocalDate.now().atTime(10, 0), "Food", "Expense"), // Normal expense
                 createTransaction(username, "Income", 1000.0, LocalDate.now().minusDays(5).atTime(9, 0), "Salary", "Income") // Other transaction
         );

         when(budgetService.calculateAverageDailyExpense(username, 3)).thenReturn(averageDaily);

         // Act
         List<String> warnings = transactionService.checkAbnormalTransactions(username, transactions);

         // Assert
         assertTrue(warnings.isEmpty(), "Should not warn if single expense is less than or equal to 3 times average daily.");
     }

      @Test
      @DisplayName("Historical: Should not warn for single expense > 3 times average daily when average is 0")
      void checkAbnormalTransactions_ExpenseGreaterThanZeroTimesZeroAverage_ShouldNotWarn() {
          // Arrange
          String username = "testUser";
          double averageDaily = 0.0; // Mock zero average daily expense
          double expenseAmount = 100.0; // Any positive expense

          List<Transaction> transactions = createTransactions(
                 createTransaction(username, "Expense", expenseAmount, LocalDate.now().atTime(10, 0), "Food", "Expense") // Expense
          );

          when(budgetService.calculateAverageDailyExpense(username, 3)).thenReturn(averageDaily); // Mock the method call as 0

          // Act
          List<String> warnings = transactionService.checkAbnormalTransactions(username, transactions);

          // Assert
          assertTrue(warnings.isEmpty(), "Should not warn if average daily expense is 0, as the condition '> 3 * avg' cannot be met by a positive amount.");
          // Verify that calculateAverageDailyExpense was called
          verify(budgetService, times(1)).calculateAverageDailyExpense(anyString(), anyInt());
      }


    @Test
    @DisplayName("Historical: Should warn for single Expense or Transfer Out >= 50000")
    void checkAbnormalTransactions_LargeTransferOrExpenseOverFiftyThousand_ShouldWarn() { // Changed name to reflect 50000 and Or Transfer Out
        // Arrange
        String username = "testUser";
        double largeAmount = LARGE_TRANSFER_OR_EXPENSE_AMOUNT; // Amount >= 50000

        List<Transaction> transactions = createTransactions(
                createTransaction(username, "Expense", largeAmount, LocalDate.now().atTime(10, 0), "Shopping", "Expense"), // Expense >= 50000
                createTransaction(username, "Transfer Out", largeAmount + 1000, LocalDate.now().minusDays(1).atTime(11, 0), "Bank", "Transfer Out"), // Transfer Out >= 50000
                createTransaction(username, "Income", 500.0, LocalDate.now().minusDays(2).atTime(9, 0), "Gift", "Income") // Other transaction
        );

        // Mock BudgetService to return a high enough average daily expense so Pattern 2 is not triggered by these amounts.
         // Need 50000 < 3 * avg => avg > 50000 / 3 ≈ 16666.67. Avg daily >= 16666.68.
        when(budgetService.calculateAverageDailyExpense(username, 3)).thenReturn(17000.00); // Example: 50000 < 3 * 17000. Mock the method call

        // Act
        List<String> warnings = transactionService.checkAbnormalTransactions(username, transactions);

        // Assert
        // Check for the warning message format for Pattern 3
        boolean largeWarningFound = warnings.stream()
                .anyMatch(w -> w.contains("single Expense or Transfer Out transaction") && w.contains(String.format("over ¥%.2f", LARGE_TRANSFER_OR_EXPENSE_AMOUNT))); // Match updated message and constant

        assertTrue(largeWarningFound, "Should warn about single Expense or Transfer Out over 50000.");
         // In this specific setup, only P3 should trigger.
         long over50kWarningsCount = warnings.stream()
              .filter(w -> w.contains("single Expense or Transfer Out transaction") && w.contains(String.format("over ¥%.2f", LARGE_TRANSFER_OR_EXPENSE_AMOUNT))) // Match updated message and constant
              .count();
         assertEquals(1, over50kWarningsCount, "Should have exactly one warning for Expense or Transfer Out over 50000.");

         long totalWarningsCount = warnings.size();
         assertEquals(1, totalWarningsCount, "Should only have one warning type in this specific scenario.");
    }

     @Test
     @DisplayName("Historical: Should not warn for single Expense or Transfer Out < 50000")
     void checkAbnormalTransactions_ExpenseLessThanFiftyThousand_ShouldNotWarn() { // Changed name
         // Arrange
         String username = "testUser";
         double normalAmount = LARGE_TRANSFER_OR_EXPENSE_AMOUNT - 0.01; // Amount < 50000

         List<Transaction> transactions = createTransactions(
                 createTransaction(username, "Expense", normalAmount, LocalDate.now().atTime(10, 0), "Shopping", "Expense"), // Expense < 50000
                 createTransaction(username, "Transfer Out", normalAmount - 1000, LocalDate.now().minusDays(1).atTime(11, 0), "Bank", "Transfer Out"), // Transfer Out < 50000
                 createTransaction(username, "Income", 500.0, LocalDate.now().minusDays(2).atTime(9, 0), "Gift", "Income") // Other transaction
         );

         // Mock average daily high enough so Pattern 2 is not triggered by these amounts
         // Need normalAmount (49999.99) is NOT > 3 * averageDaily. 49999.99 / 3 ≈ 16666.66. Avg daily >= 16666.67.
         when(budgetService.calculateAverageDailyExpense(username, 3)).thenReturn(17000.00);

         // Act
         List<String> warnings = transactionService.checkAbnormalTransactions(username, transactions);

         // Assert
         assertTrue(warnings.isEmpty(), "Should not warn if single Expense or Transfer Out is less than 50000.");
     }


    @Test
    @DisplayName("Historical: Should list all triggered warnings")
    void checkAbnormalTransactions_MultipleWarnings_ShouldListAll() {
        // Arrange
        String username = "testUser";
        LocalDate today = LocalDate.now();
        // Create 3 large transactions (>=5000) on the same day for P1
        LocalDateTime timestamp1 = today.atTime(10, 0);
        LocalDateTime timestamp2 = today.atTime(11, 0);
        LocalDateTime timestamp3 = today.atTime(12, 0); // Triggers frequent large on today

        double averageDaily = 200.0; // Avg daily expense
        // Define amounts that trigger specific patterns without overlap for clarity in this test
         // P1 triggered by amounts >= 5000 (transactions on 'today')
         // P2 triggered by p2OnlyAmount (> 3 * 200 = 600) AND < 50000
         double p2OnlyAmount = 8000.0; // Example: 8000 > 600 and < 50000
         // P3 triggered by p3OnlyAmount (>= 50000). Also > 600.
         double p3OnlyAmount = 55000.0; // Example: 55000 >= 50000. Also > 600.


        List<Transaction> transactions = createTransactions(
                // Transactions triggering P1 (Frequent Large) - Need >=3 >= 5000 on the same day
                createTransaction(username, "Income", 5500.0, today.atTime(10, 0), "Salary", "Income"), // P1 trigger 1 (>=5000)
                createTransaction(username, "Expense", 6000.0, today.atTime(11, 0), "Shopping", "Expense"), // P1 trigger 2 (>=5000)
                createTransaction(username, "Income", 7000.0, today.atTime(12, 0), "Gift", "Income"),     // P1 trigger 3 (>=5000) (triggers P1 warning)

                // Transaction triggering P2 (>3x avg) - Must be Expense, amount > 600 and < 50000
                createTransaction(username, "Expense", p2OnlyAmount, today.minusDays(1).atTime(10, 0), "Travel", "Expense"), // Triggers P2 on yesterday

                // Transaction triggering P3 (>=50000) - Must be Expense or Transfer Out
                createTransaction(username, "Expense", p3OnlyAmount, today.minusDays(2).atTime(10, 0), "Transfer Out", "Expense") // Triggers P3 on 2 days ago
        );

        when(budgetService.calculateAverageDailyExpense(username, 3)).thenReturn(averageDaily); // Mock the average daily expense

        // Act
        List<String> warnings = transactionService.checkAbnormalTransactions(username, transactions);

        // Assert
        assertEquals(3, warnings.size(), "Should have three distinct warnings.");
        // Check for Pattern 1 warning (frequent large today)
        assertTrue(warnings.stream().anyMatch(w -> w.contains("Frequent large transactions") && w.contains(today.format(dateFormatter))), "Should contain frequent large transaction warning.");
        // Check for Pattern 2 warning (>3x avg expense)
        assertTrue(warnings.stream().anyMatch(w -> w.contains("single expense transaction") && w.contains("exceeded") && w.contains(String.format("%.1f times", LARGE_EXPENSE_MULTIPLIER))), "Should contain >3x average expense warning."); // Use constant in message match
        // Check for Pattern 3 warning (>50000 expense or transfer out) - Match updated message and constant
        assertTrue(warnings.stream().anyMatch(w -> w.contains("single Expense or Transfer Out transaction") && w.contains(String.format("over ¥%.2f", LARGE_TRANSFER_OR_EXPENSE_AMOUNT))), "Should contain >50000 expense or transfer out warning."); // Use constant in message match
    }


    // --- Test Cases for checkRealtimeAbnormalTransactions (Real-time Check) ---

    @Test
    @DisplayName("Realtime: Should warn if adding triggers frequent large transactions today")
    void checkRealtimeAbnormalTransactions_AddingTriggersFrequentLarge_ShouldWarn() {
         // Arrange
         String username = "testUser";
         LocalDate today = LocalDate.now();
         // Existing large transactions (>=5000) today, count = 2
         LocalDateTime timestamp1 = today.atTime(10, 0);
         LocalDateTime timestamp2 = today.atTime(11, 0);

         // New transaction timestamp (today)
         LocalDateTime newTimestamp = today.atTime(12, 0);

         List<Transaction> existingTransactions = createTransactions(
                 createTransaction(username, "Income", 5000.0, today.atTime(10, 0), "Salary", "Income"), // Existing large 1 today (>=5000)
                 createTransaction(username, "Expense", 6000.0, today.atTime(11, 0), "Shopping", "Expense"), // Existing large 2 today (>=5000)
                 createTransaction(username, "Expense", 100.0, today.minusDays(1).atTime(9, 0), "Food", "Expense") // Other existing
         );

         // This new transaction (7000 >= 5000) makes the total count for today 3 (2+1). Triggers P1.
         Transaction newTransaction = createTransaction(username, "Income", 7000.0, today.atTime(12, 0), "Gift", "Income");

         // Mock dependencies (average daily doesn't affect P1). Set avg high enough so P2 isn't triggered by 7000.
         // 7000 < 3 * avg => avg > 7000 / 3 ≈ 2333.33
         when(budgetService.calculateAverageDailyExpense(username, 3)).thenReturn(2333.34); // Example: 7000 < 3 * 2333.34

         // Act
         List<String> warnings = transactionService.checkRealtimeAbnormalTransactions(username, existingTransactions, newTransaction);

         // Assert
         boolean frequentWarningFound = warnings.stream()
                 .anyMatch(w -> w.contains("Adding this transaction") && w.contains("will result in") && w.contains("3 large transactions") && w.contains("today") && w.contains(today.format(dateFormatter))); // Check count, date, and amount format (amount is in message)

         assertTrue(frequentWarningFound, "Should warn if adding a transaction that triggers frequent large transactions today.");
         // In this specific setup, only the frequent warning should be triggered by the new transaction.
         assertEquals(1, warnings.size(), "Should only have one warning for this scenario.");
     }

     @Test
     @DisplayName("Realtime: Should not warn if adding non-large transaction when others are large")
     void checkRealtimeAbnormalTransactions_AddingNonLargeTransaction_ShouldNotTriggerFrequent() {
         // Arrange
         String username = "testUser";
         LocalDate today = LocalDate.now();
         // Existing large transactions (>=5000) today, count = 2
         LocalDateTime timestamp1 = today.atTime(10, 0);
         LocalDateTime timestamp2 = today.atTime(11, 0);

         // New transaction timestamp (today)
         LocalDateTime newTimestamp = today.atTime(12, 0);

         List<Transaction> existingTransactions = createTransactions(
                 createTransaction(username, "Income", 5000.0, today.atTime(10, 0), "Salary", "Income"), // Existing large 1 today
                 createTransaction(username, "Expense", 6000.0, today.atTime(11, 0), "Shopping", "Expense") // Existing large 2 today
         );

         // This new transaction is NOT large (< 5000)
         Transaction newTransaction = createTransaction(username, "Expense", 4000.0, today.atTime(12, 0), "Food", "Expense");

         // Mock dependencies (average daily doesn't affect P1). Set avg high enough so P2 isn't triggered by 4000.
         when(budgetService.calculateAverageDailyExpense(username, 3)).thenReturn(1333.34); // Example: 4000 < 3 * 1333.34

         // Act
         List<String> warnings = transactionService.checkRealtimeAbnormalTransactions(username, existingTransactions, newTransaction);

         // Assert
         assertTrue(warnings.isEmpty(), "Should not warn about frequent large transactions if the added transaction itself is not large (< 5000).");
     }


    @Test
    @DisplayName("Realtime: Should warn if new expense > 3 times average daily")
    void checkRealtimeAbnormalTransactions_NewExpenseGreaterThanThreeTimesAverage_ShouldWarn() {
        // Arrange
        String username = "testUser";
        double averageDaily = 200.0; // Mock average daily expense
        double newLargeExpenseAmount = averageDaily * LARGE_EXPENSE_MULTIPLIER + 0.01; // New expense > 3 * averageDaily

        List<Transaction> existingTransactions = createTransactions(
                createTransaction(username, "Income", 1000.0, LocalDate.now().minusDays(5).atTime(9, 0), "Salary", "Income") // Other existing
        );

        Transaction newTransaction = createTransaction(username, "Expense", newLargeExpenseAmount, LocalDate.now().atTime(10, 0), "Shopping", "Expense");

        when(budgetService.calculateAverageDailyExpense(username, 3)).thenReturn(averageDaily); // Mock the method call

        // Act
        List<String> warnings = transactionService.checkRealtimeAbnormalTransactions(username, existingTransactions, newTransaction);

        // Assert
        boolean largeExpenseWarningFound = warnings.stream()
                .anyMatch(w -> w.contains("This expense") && w.contains("exceeds") && w.contains(String.format("%.1f times", LARGE_EXPENSE_MULTIPLIER)) && w.contains(String.format("¥%.2f", newLargeExpenseAmount))); // Check amount format

        assertTrue(largeExpenseWarningFound, "Should warn about new single expense exceeding 3 times average daily.");
         // In this specific setup, only P2 should trigger.
         assertEquals(1, warnings.size(), "Should only have one warning for this scenario.");
    }

     @Test
     @DisplayName("Realtime: Should not warn if new expense <= 3 times average daily")
     void checkRealtimeAbnormalTransactions_NewExpenseLessThanThreeTimesAverage_ShouldNotWarn() {
         // Arrange
         String username = "testUser";
         double averageDaily = 200.0; // Mock average daily expense
         double newNormalExpenseAmount = averageDaily * LARGE_EXPENSE_MULTIPLIER - 0.01; // New expense <= 3 * averageDaily

         List<Transaction> existingTransactions = createTransactions(
                 createTransaction(username, "Income", 1000.0, LocalDate.now().minusDays(5).atTime(9, 0), "Salary", "Income") // Other existing
         );

         Transaction newTransaction = createTransaction(username, "Expense", newNormalExpenseAmount, LocalDate.now().atTime(10, 0), "Food", "Expense");

         when(budgetService.calculateAverageDailyExpense(username, 3)).thenReturn(averageDaily); // Mock the method call

         // Act
         List<String> warnings = transactionService.checkRealtimeAbnormalTransactions(username, existingTransactions, newTransaction);

         // Assert
         assertTrue(warnings.isEmpty(), "Should not warn if new single expense is less than or equal to 3 times average daily.");
     }

      @Test
      @DisplayName("Realtime: Should not warn for new expense > 3 times average daily when average is 0")
      void checkRealtimeAbnormalTransactions_ExpenseGreaterThanZeroTimesZeroAverage_ShouldNotWarn() {
          // Arrange
          String username = "testUser";
          double averageDaily = 0.0; // Mock zero average daily expense
          double newExpenseAmount = 100.0; // Any positive expense

          List<Transaction> existingTransactions = Collections.emptyList(); // No existing transactions

          Transaction newTransaction = createTransaction(username, "Expense", newExpenseAmount, LocalDate.now().atTime(10, 0), "Food", "Expense");

          when(budgetService.calculateAverageDailyExpense(username, 3)).thenReturn(averageDaily); // Mock the method call as 0

          // Act
          List<String> warnings = transactionService.checkRealtimeAbnormalTransactions(username, existingTransactions, newTransaction);

          // Assert
          assertTrue(warnings.isEmpty(), "Should not warn if average daily expense is 0, as the condition '> 3 * avg' cannot be met by a positive amount.");
           // Verify that calculateAverageDailyExpense was called (it's called unconditionally)
          verify(budgetService, times(1)).calculateAverageDailyExpense(anyString(), anyInt());
      }


    @Test
    @DisplayName("Realtime: Should warn if new Expense or Transfer Out >= 50000")
    void checkRealtimeAbnormalTransactions_NewLargeTransferOrExpenseOverFiftyThousand_ShouldWarn() { // Changed name
        // Arrange
        String username = "testUser";
        double newLargeAmount = LARGE_TRANSFER_OR_EXPENSE_AMOUNT; // New amount >= 50000

        List<Transaction> existingTransactions = createTransactions(
                createTransaction(username, "Income", 500.0, LocalDate.now().minusDays(2).atTime(9, 0), "Gift", "Income") // Other existing
        );

        Transaction newTransaction = createTransaction(username, "Expense", newLargeAmount, LocalDate.now().atTime(10, 0), "Transfer Out", "Expense"); // New Expense >= 50000

        // Mock average daily high enough so only P3 triggers based on amount value itself.
        // Need LARGE_TRANSFER_OR_EXPENSE_AMOUNT (50000) is NOT > 3 * averageDaily. 50000 / 3 ≈ 16666.67. Avg daily >= 16666.68.
        when(budgetService.calculateAverageDailyExpense(username, 3)).thenReturn(17000.00); // Mock the method call

        // Act
        List<String> warnings = transactionService.checkRealtimeAbnormalTransactions(username, existingTransactions, newTransaction);

        // Assert
        boolean largeWarningFound = warnings.stream()
                .anyMatch(w -> w.contains("This Expense or Transfer Out transaction") && w.contains(String.format("is over ¥%.2f", LARGE_TRANSFER_OR_EXPENSE_AMOUNT))); // Match updated message and constant

        assertTrue(largeWarningFound, "Should warn about new single Expense or Transfer Out over 50000.");
         // In this specific setup, only P3 should trigger.
         assertEquals(1, warnings.size(), "Should only have one warning for this scenario.");
    }

     @Test
     @DisplayName("Realtime: Should not warn if new Expense or Transfer Out < 50000")
     void checkRealtimeAbnormalTransactions_NewExpenseLessThanFiftyThousand_ShouldNotWarn() { // Changed name
         // Arrange
         String username = "testUser";
         double newNormalAmount = LARGE_TRANSFER_OR_EXPENSE_AMOUNT - 0.01; // New amount < 50000

         List<Transaction> existingTransactions = createTransactions(
                 createTransaction(username, "Income", 500.0, LocalDate.now().minusDays(2).atTime(9, 0), "Gift", "Income") // Other existing
         );

         Transaction newTransaction = createTransaction(username, "Expense", newNormalAmount, LocalDate.now().atTime(10, 0), "Shopping", "Expense"); // New Expense < 50000

         // Mock average daily high enough so Pattern 2 is not triggered
         // Need newNormalAmount (49999.99) is NOT > 3 * averageDaily. 49999.99 / 3 ≈ 16666.66. Avg daily >= 16666.67.
         when(budgetService.calculateAverageDailyExpense(username, 3)).thenReturn(17000.00);

         // Act
         List<String> warnings = transactionService.checkRealtimeAbnormalTransactions(username, existingTransactions, newTransaction);

         // Assert
         assertTrue(warnings.isEmpty(), "Should not warn if new single Expense or Transfer Out is less than 50000.");
     }


    @Test
    @DisplayName("Realtime: Should list all triggered warnings for the new transaction")
    void checkRealtimeAbnormalTransactions_NewTransactionTriggersMultipleWarnings_ShouldListAll() {
        // Arrange
        String username = "testUser";
        LocalDate today = LocalDate.now();
        // Existing large transactions (>=5000) today, count = 2
        LocalDateTime timestamp1 = today.atTime(10, 0);
        LocalDateTime timestamp2 = today.atTime(11, 0);

        // New transaction timestamp (today)
        LocalDateTime newTimestamp = today.atTime(12, 0); // Timestamp for the new transaction

        double averageDaily = 200.0; // Avg daily expense
        // New transaction amount needs to trigger P1 (if it's >=5000 AND makes count >=3), P2 (>3*avg), and P3 (>=50000)
        // Need amount >= 5000, > 600, >= 50000. So need amount >= 50000.
        double newLargeAmount = 55000.0; // Example: 55000 >= 50000, 55000 > 3*200=600, and >= 5000.

        List<Transaction> existingTransactions = createTransactions(
                // Need 2 existing large transactions (>= 5000) on the same day as the new transaction to trigger P1
                createTransaction(username, "Income", 5500.0, today.atTime(10, 0), "Salary", "Income"), // Existing large 1 today (>=5000)
                createTransaction(username, "Expense", 6000.0, today.atTime(11, 0), "Shopping", "Expense"), // Existing large 2 today (>=5000)
                createTransaction(username, "Expense", 100.0, today.minusDays(1).atTime(9, 0), "Food", "Expense") // Other existing
        );

        // This new transaction (>= 5000 AND triggers P2 (>3*200) AND >= 50000) makes the total count for today 3 (2+1). Triggers P1, P2, P3.
        Transaction newTransaction = createTransaction(username, "Expense", newLargeAmount, today.atTime(12, 0), "Travel", "Expense"); // Use newLargeAmount

        when(budgetService.calculateAverageDailyExpense(username, 3)).thenReturn(averageDaily);

        // Act
        List<String> warnings = transactionService.checkRealtimeAbnormalTransactions(username, existingTransactions, newTransaction);

        // Assert
        assertEquals(3, warnings.size(), "Should have three warnings for frequent, >3x avg, and >50000 for the new transaction.");
        assertTrue(warnings.stream().anyMatch(w -> w.contains("Adding this transaction") && w.contains("will result in") && w.contains("3 large transactions") && w.contains("today") && w.contains(today.format(dateFormatter))), "Should contain frequent large transaction warning.");
        assertTrue(warnings.stream().anyMatch(w -> w.contains("This expense") && w.contains("exceeds") && w.contains(String.format("%.1f times", LARGE_EXPENSE_MULTIPLIER))), "Should contain >3x average expense warning.");
        assertTrue(warnings.stream().anyMatch(w -> w.contains("This Expense or Transfer Out transaction") && w.contains("is over ¥50000.00")), "Should contain >50000 expense or transfer out warning.");
    }

     @Test
     @DisplayName("Realtime: Should warn if adding income triggers frequent large transactions today")
     void checkRealtimeAbnormalTransactions_AddingIncomeTriggersFrequentLarge_ShouldWarn() {
         // Arrange
         String username = "testUser";
         LocalDate today = LocalDate.now();
         // Existing large transactions (>=5000) today, count = 2
         LocalDateTime timestamp1 = today.atTime(10, 0);
         LocalDateTime timestamp2 = today.atTime(11, 0);

         // New transaction timestamp (today)
         LocalDateTime newTimestamp = today.atTime(12, 0);

         List<Transaction> existingTransactions = createTransactions(
                 createTransaction(username, "Income", 5500.0, today.atTime(10, 0), "Salary", "Income"), // Existing large 1 today (>=5000)
                 createTransaction(username, "Expense", 6000.0, today.atTime(11, 0), "Shopping", "Expense"), // Existing large 2 today (>=5000)
                 createTransaction(username, "Expense", 100.0, today.minusDays(1).atTime(9, 0), "Food", "Expense") // Other existing
         );

         // This new transaction (7000 >= 5000) makes the total count for today 3 (2+1). Triggers P1.
         Transaction newTransaction = createTransaction(username, "Income", 7000.0, today.atTime(12, 0), "Gift", "Income");

         // Mock dependencies (average daily doesn't affect P1). Set avg high enough so P2 isn't triggered by 7000.
         // 7000 < 3 * avg => avg > 7000 / 3 ≈ 2333.33
         when(budgetService.calculateAverageDailyExpense(username, 3)).thenReturn(2333.34); // Example: 7000 < 3 * 2333.34

         // Act
         List<String> warnings = transactionService.checkRealtimeAbnormalTransactions(username, existingTransactions, newTransaction);

         // Assert
         boolean frequentWarningFound = warnings.stream()
                 .anyMatch(w -> w.contains("Adding this transaction") && w.contains("will result in") && w.contains("3 large transactions") && w.contains("today") && w.contains(today.format(dateFormatter)));

         assertTrue(frequentWarningFound, "Should warn if adding the new INCOME transaction triggers frequent large transactions for the day.");
         assertEquals(1, warnings.size(), "Should only have one warning for this scenario.");
     }

     @Test
     @DisplayName("Realtime: Should handle null new transaction")
     void checkRealtimeAbnormalTransactions_NullNewTransaction_ShouldReturnEmpty() {
         // Arrange
         String username = "testUser";
         List<Transaction> existingTransactions = createTransactions(
                 createTransaction(username, "Income", 5000.0, LocalDate.now().atTime(10, 0), "Salary", "Income")
         );
         Transaction newTransaction = null; // <-- newTransaction is null

         // Mock setting is not strictly needed here because the method should exit before calling budgetService
         // when(budgetService.calculateAverageDailyExpense(username, 3)).thenReturn(100.0);

         // Act
         List<String> warnings = transactionService.checkRealtimeAbnormalTransactions(username, existingTransactions, newTransaction);

         // Assert
         assertTrue(warnings.isEmpty(), "Should return empty list for a null new transaction.");
          // Verify budgetService was NOT called
         verify(budgetService, never()).calculateAverageDailyExpense(anyString(), anyInt()); // Corrected: Assert never called
     }

    @Test
    @DisplayName("Realtime: Should handle null existing transactions list")
    void checkRealtimeAbnormalTransactions_NullExistingTransactions_ShouldCheckNewTransactionOnly() {
        // Arrange
        String username = "testUser";
        List<Transaction> existingTransactions = null; // Null existing list

        // New transaction (>=50000) triggers P2 & P3 (if avg daily allows)
        Transaction newTransaction = createTransaction(username, "Expense", 55000.0, LocalDate.now().atTime(10, 0), "Shopping", "Expense");

        // Mock average daily to trigger P2
        double averageDaily = 100.0; // 55000 > 3 * 100
        when(budgetService.calculateAverageDailyExpense(username, 3)).thenReturn(averageDaily); // Mock the method call

        // Act
        List<String> warnings = transactionService.checkRealtimeAbnormalTransactions(username, existingTransactions, newTransaction);

        // Assert
        // Pattern 1 requires iterating existing transactions, so it won't trigger with a null list.
        // Pattern 2 and 3 only check the new transaction's amount.
        // Warning for Pattern 2 (exceeds average) and Pattern 3 (>50000) should be present.
        assertEquals(2, warnings.size(), "Should warn for P2 and P3 based on the new transaction, ignoring null existing list.");
         assertTrue(warnings.stream().anyMatch(w -> w.contains("This expense") && w.contains("exceeds") && w.contains(String.format("%.1f times", LARGE_EXPENSE_MULTIPLIER))), "Should contain >3x average expense warning.");
         assertTrue(warnings.stream().anyMatch(w -> w.contains("This Expense or Transfer Out transaction") && w.contains(String.format("is over ¥%.2f", LARGE_TRANSFER_OR_EXPENSE_AMOUNT))), "Should contain >50000 expense or transfer out warning.");
         // Verify budgetService was called
         verify(budgetService, times(1)).calculateAverageDailyExpense(anyString(), anyInt());
    }


    // --- Add more tests for edge cases and invalid inputs (e.g., bad dates in existing or new transactions) ---
    // For example:
    // @Test
    // @DisplayName("Realtime: Should handle new transaction with invalid date")
    // void checkRealtimeAbnormalTransactions_InvalidNewDate_ShouldReturnWarning() {
    //     String username = "testUser";
    //     List<Transaction> existingTransactions = Collections.emptyList(); // Keep it simple
    //     // Create a transaction with a deliberately bad date string
    //     Transaction newTransaction = new Transaction(username, "Expense", 10000.0, "invalid-date", "Test", "Other", "", "Other", "", "", "", "", "");
    //     when(budgetService.calculateAverageDailyExpense(username, 3)).thenReturn(100.0);
    //
    //     List<String> warnings = transactionService.checkRealtimeAbnormalTransactions(username, existingTransactions, newTransaction);
    //
    //     // Assert that a warning about invalid date or inability to check frequency is present
    //     assertTrue(warnings.stream().anyMatch(w -> w.contains("invalid date format") || w.contains("Cannot perform full check")), "Should warn about invalid date format.");
    //      // Should also still check P2 and P3 if operation/amount are valid
    //      assertTrue(warnings.stream().anyMatch(w -> w.contains("This Expense or Transfer Out transaction") && w.contains(String.format("is over ¥%.2f", LARGE_TRANSFER_OR_EXPENSE_AMOUNT))), "Should still check P3 if amount is large."); // Updated threshold and message match
    //     assertEquals(2, warnings.size(), "Should have warning about invalid date and P3.");
    // }


}