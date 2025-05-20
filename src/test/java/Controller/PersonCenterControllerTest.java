package Controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import Model.UserSession;
import Service.PersonFinancialService;
import Service.PersonFinancialService.FinancialSummary;
import Service.PersonChartDataService;
import Service.PersonChartDataService.AnnualChartData;
import Service.PersonChartDataService.CategoryChartData;
import View.PersonalCenter.PersonalCenterPanel;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonCenterControllerTest {

    @Mock
    private PersonFinancialService financialService;

    @Mock
    private PersonChartDataService chartDataService;

    @Mock
    private PersonalCenterPanel view;

    private PersonCenterController controller;

    @BeforeEach
    void setUp() {
        controller = new PersonCenterController(financialService, chartDataService);
        controller.setView(view);
    }

    /**
     * Test normal load data flow
     */
    @Test
    void testLoadData_success() throws Exception {
        try (MockedStatic<UserSession> userSessionMock = mockStatic(UserSession.class);
             MockedStatic<SwingUtilities> swingMock = mockStatic(SwingUtilities.class)) {

            userSessionMock.when(UserSession::getCurrentUsername).thenReturn("testUser");

            swingMock.when(() -> SwingUtilities.invokeLater(any(Runnable.class)))
                    .thenAnswer(invocation -> {
                        ((Runnable)invocation.getArgument(0)).run();
                        return null;
                    });

            FinancialSummary summary = mock(FinancialSummary.class);
            when(financialService.calculateFinancialSummary("testUser", 2024)).thenReturn(summary);
            when(financialService.generatePaymentLocationSummary("testUser", 2024)).thenReturn("Shanghai, Beijing");
            AnnualChartData annualChart = mock(AnnualChartData.class);
            CategoryChartData categoryChart = mock(CategoryChartData.class);
            when(chartDataService.prepareAnnualChartData("testUser", 2024)).thenReturn(annualChart);
            when(chartDataService.prepareCategoryChartData("testUser", 2024)).thenReturn(categoryChart);

            controller.loadData(2024);

            verify(view).updateFinancialSummary(summary);
            verify(view).updatePaymentLocationSummary("Shanghai, Beijing");
            verify(view).updateAnnualChartData(annualChart);
            verify(view).updateCategoryChartData(categoryChart);

            assertTrue(controller.isDataLoadedSuccessfully());
        }
    }


    /**
     * Test: no view method is called when not logged in
     */
    @Test
    void testLoadData_notLoggedIn() throws Exception {
        try (MockedStatic<UserSession> userSessionMock = mockStatic(UserSession.class)) {
            userSessionMock.when(UserSession::getCurrentUsername).thenReturn(null);

            controller.loadData(2024);
            Thread.sleep(50);

            verifyNoInteractions(view);
            assertFalse(controller.isDataLoadedSuccessfully());
        }
    }

    /**
     * The test service throws an exception
     */
    @Test
    void testLoadData_serviceThrows() throws Exception {
        try (MockedStatic<UserSession> userSessionMock = mockStatic(UserSession.class)) {
            userSessionMock.when(UserSession::getCurrentUsername).thenReturn("testUser");

            when(financialService.calculateFinancialSummary(anyString(), anyInt()))
                    .thenThrow(new RuntimeException("DB Error"));

            controller.loadData(2024);
            Thread.sleep(50);

            verifyNoInteractions(view);
            assertFalse(controller.isDataLoadedSuccessfully());
        }
    }
}
