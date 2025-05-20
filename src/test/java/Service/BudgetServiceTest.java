package Service;

import Model.BudgetDataContainer;
import Model.Transaction;
import Repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BudgetServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    private BudgetService budgetService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        budgetService = new BudgetService(transactionRepository);
    }

    @Test
    void saveAndGetCustomBudget_PersistsCorrectly() {
        // Arrange
        String username = "testUser";
        double budget = 5000.0;

        // Act
        budgetService.saveCustomBudget(username, budget);
        Double result = budgetService.getCustomBudget(username);

        // Assert
        assertEquals(budget, result, 0.001);
    }

    @Test
    void calculateRecommendation_WithCustomBudget_ReturnsCustomMode() {
        // Arrange
        String username = "testUser";
        budgetService.saveCustomBudget(username, 6000.0);

        // Act
        BudgetService.BudgetRecommendation recommendation = budgetService.calculateRecommendation(username, LocalDate.now());

        // Assert
        assertEquals(BudgetService.BudgetMode.CUSTOM, recommendation.getMode());
        assertEquals(6000.0, recommendation.getSuggestedBudget());
    }

    @Test
    void getPastThreeMonthsFinancialData_ReturnsCorrectStructure() {
        // Arrange
        String username = "testUser";
        LocalDate now = LocalDate.of(2024, Month.MARCH, 15);
        List<Transaction> transactions = Arrays.asList(
                createTransaction(username, "Income", 9000.0, "2024/02/01 00:00", "Salary", "Income"),
                createTransaction(username, "Expense", 4000.0, "2024/02/05 00:00", "Rent", "Expense")
        );

        when(transactionRepository.findTransactionsByUsername(username)).thenReturn(transactions);

        // Act
        List<BudgetService.MonthlyFinancialData> result = budgetService.getPastThreeMonthsFinancialData(username, now);

        // Assert
        assertEquals(3, result.size());
        assertEquals("2024-02", result.get(0).getMonth()); // Most recent first
        assertEquals(9000.0, result.get(0).getIncome());
        assertEquals(4000.0, result.get(0).getExpense());
    }

    private Transaction createTransaction(String username, String operation, double amount, String timestamp, String type, String category) {
        return new Transaction(username, operation, amount, timestamp, "", type, "", category, "", "", "", "", "");
    }

    @Test
    void calculateRecommendation_WithZeroIncome_ReturnsZeroBudget() {
        when(transactionRepository.findTransactionsByUsername(anyString())).thenReturn(Collections.emptyList());

        BudgetService.BudgetRecommendation recommendation = budgetService.calculateRecommendation("emptyUser", LocalDate.now());

        assertEquals(0.0, recommendation.getSuggestedBudget());
    }
}