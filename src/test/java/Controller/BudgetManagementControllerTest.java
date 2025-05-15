package Controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import Service.BudgetService;
import Service.BudgetService.BudgetRecommendation;
import View.BudgetAdvisor.BudgetManagementPanel;

@ExtendWith(MockitoExtension.class)
class BudgetManagementControllerTest {

    @Mock
    private BudgetManagementPanel view;

    @Mock
    private BudgetService budgetService;

    @Mock
    private BudgetRecommendation recommendation;

    private BudgetManagementController controller;
    private String username = "testUser";

    @BeforeEach
    void setUp() {
        controller = new BudgetManagementController(username, view, budgetService);
    }

    @Test
    void testLoadBudgetData_ViewNotInitialized() {
        // Arrange
        when(view.isInitialized()).thenReturn(false);

        // Act
        controller.loadBudgetData();

        // Assert
        verify(view, times(2)).isInitialized(); // 构造器和 loadBudgetData 各调用一次
        verifyNoMoreInteractions(view, budgetService);
    }

    @Test
    void testSaveCustomBudget_NegativeInput() {
        // Arrange
        when(view.isInitialized()).thenReturn(true);
        String budgetInput = "-100";

        // Act
        controller.saveCustomBudget(budgetInput);

        // Assert
        verify(view, times(2)).isInitialized(); // 构造器和 saveCustomBudget
        verify(view).showError("Budget cannot be negative.");
        verifyNoInteractions(budgetService);
    }

    @Test
    void testSaveCustomBudget_InvalidInput() {
        // Arrange
        when(view.isInitialized()).thenReturn(true);
        String budgetInput = "invalid";

        // Act
        controller.saveCustomBudget(budgetInput);

        // Assert
        verify(view, times(2)).isInitialized(); // 构造器和 saveCustomBudget
        verify(view).showError("Invalid number format.");
        verifyNoInteractions(budgetService);
    }

    @Test
    void testSaveCustomBudget_ViewNotInitialized() {
        // Arrange
        when(view.isInitialized()).thenReturn(false);

        // Act
        controller.saveCustomBudget("1000.50");

        // Assert
        verify(view, times(2)).isInitialized(); // 构造器和 saveCustomBudget
        verifyNoMoreInteractions(view, budgetService);
    }

    

    @Test
    void testClearCustomBudget_ViewNotInitialized() {
        // Arrange
        when(view.isInitialized()).thenReturn(false);

        // Act
        controller.clearCustomBudget();

        // Assert
        verify(view, times(2)).isInitialized(); // 构造器和 clearCustomBudget
        verifyNoMoreInteractions(view, budgetService);
    }
}