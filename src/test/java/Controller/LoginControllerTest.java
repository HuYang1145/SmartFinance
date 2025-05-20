package Controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

import javax.swing.JOptionPane;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;

import Controller.LoginController;
import Model.User;
import Model.User.AccountStatus;
import Repository.AccountRepository;
import Repository.TransactionRepository;      // ← 一定要 import 你项目里的 TransactionRepository
import Service.TransactionService;
import Service.BudgetService;
import View.LoginAndMain.Login;
import View.LoginAndMain.LoginComponents;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock private AccountRepository accountRepo;
    @Mock private TransactionService transactionService;
    @Mock private TransactionRepository transactionRepository; // ← 新增：Mock 出 TransactionRepository
    @Mock private BudgetService budgetService;
    @Mock private Login loginFrame;

    @InjectMocks private LoginController controller;

    private MockedStatic<LoginComponents> loginComponentsMock;

    @BeforeEach
    void setup() {
        loginComponentsMock = Mockito.mockStatic(LoginComponents.class);

        lenient().when(transactionService.getTransactionRepository())
                .thenReturn(transactionRepository);
        lenient().when(transactionRepository.readAllTransactions())
                .thenReturn(Collections.emptyList());
        lenient().when(transactionService.checkAbnormalTransactions(any(), anyList()))
                .thenReturn(Collections.emptyList());
    }

    @AfterEach
    void tearDown() {
        loginComponentsMock.close();
    }

    @Test
    void testValidPersonalUserLogin() {
        User user = new User(
                "user1","pwd","phone","email","M","addr",
                "2025/05/01 10:00",AccountStatus.ACTIVE,"Personal",0.0
        );
        when(accountRepo.findByUsername("user1")).thenReturn(user);

        LoginController spyCtrl = spy(controller);
        doNothing().when(spyCtrl).showMainInterface("user1");

        spyCtrl.handleLogin("user1","pwd",loginFrame);

        verify(loginFrame).closeWindow();
        verify(spyCtrl).showMainInterface("user1");
        loginComponentsMock.verify(() ->
                        LoginComponents.showCustomMessage(any(), anyString(), anyString(), anyInt()),
                never()
        );
    }

    @Test
    void testUnknownUserShowsError() {
        when(accountRepo.findByUsername("unknown")).thenReturn(null);

        controller.handleLogin("unknown", "any", loginFrame);

        loginComponentsMock.verify(() ->
                LoginComponents.showCustomMessage(
                        eq(loginFrame),
                        eq("Invalid username or password"),
                        eq("Error"),
                        eq(JOptionPane.ERROR_MESSAGE)
                ), times(1)
        );
        verify(loginFrame, never()).closeWindow();
    }

    @Test
    void testFrozenAccountShowsFrozenMessage() {
        User frozen = new User(
                "userX", "pwd", "phone", "email", "M", "addr",
                "2025/05/01 10:00", AccountStatus.FROZEN, "Personal", 0.0
        );
        when(accountRepo.findByUsername("userX")).thenReturn(frozen);

        controller.handleLogin("userX", "pwd", loginFrame);

        loginComponentsMock.verify(() ->
                LoginComponents.showCustomMessage(
                        eq(loginFrame),
                        eq("Account is frozen"),
                        eq("Error"),
                        eq(JOptionPane.ERROR_MESSAGE)
                ), times(1)
        );
        verify(loginFrame, never()).closeWindow();
    }

}
