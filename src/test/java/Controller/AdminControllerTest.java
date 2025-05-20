package Controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import javax.swing.JOptionPane;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;

import Model.User;
import Repository.AccountRepository;
import View.Administrator.AdminView;
import View.LoginAndMain.LoginComponents;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AdminView adminView;

    private MockedStatic<LoginComponents> loginComponentsMock;

    private AdminController controller;

    @BeforeEach
    void setUp() throws Exception {
        loginComponentsMock = Mockito.mockStatic(LoginComponents.class);

        controller = new AdminController(accountRepository);
        Field viewField = AdminController.class.getDeclaredField("view");
        viewField.setAccessible(true);
        viewField.set(controller, adminView);
    }

    @AfterEach
    void tearDown() {
        loginComponentsMock.close();
    }

    /**
     * To test the handleCustomerInquiry success branch, * the view.updateAccountTable(accounts) should be called.
     * view.updateAccountTable(accounts) should be called.
     */
    @Test
    void testHandleCustomerInquirySuccess() throws Exception {
        List<User> accounts = Arrays.asList(
                new User("u1","p","ph","e","M","addr","ts",User.AccountStatus.ACTIVE,"Personal",0.0),
                new User("u2","p","ph","e","F","addr","ts",User.AccountStatus.ACTIVE,"Personal",0.0)
        );
        when(accountRepository.readFromCSV()).thenReturn(accounts);

        Method m = AdminController.class.getDeclaredMethod("handleCustomerInquiry", List.class);
        m.setAccessible(true);
        m.invoke(controller, (Object) null);

        verify(adminView).updateAccountTable(accounts);
    }

    /**
     * Testing the handleCustomerInquiry exception branch.
     * ShowCustomMessage should be called when the warehouse throws an exception.
     */
    @Test
    void testHandleCustomerInquiryFailure() throws Exception {
        when(accountRepository.readFromCSV()).thenThrow(new RuntimeException("fail"));

        Method m = AdminController.class.getDeclaredMethod("handleCustomerInquiry", List.class);
        m.setAccessible(true);
        m.invoke(controller, (Object) null);

        loginComponentsMock.verify(() ->
                LoginComponents.showCustomMessage(
                        eq(adminView),
                        eq("Failed to load account data: fail"),
                        eq("Error"),
                        eq(JOptionPane.ERROR_MESSAGE)
                ), times(1)
        );
    }

    /**
     * Tests for an empty collection branch of handleDeleteUsers, * which should prompt for ‘Unchecked Users’.
     * should prompt ‘unchecked user’.
     */
    @Test
    void testHandleDeleteUsersEmpty() throws Exception {
        Method m = AdminController.class.getDeclaredMethod("handleDeleteUsers", Set.class);
        m.setAccessible(true);
        m.invoke(controller, Collections.emptySet());

        loginComponentsMock.verify(() ->
                LoginComponents.showCustomMessage(
                        eq(adminView),
                        eq("No users selected for deletion."),
                        eq("Information"),
                        eq(JOptionPane.INFORMATION_MESSAGE)
                ), times(1)
        );
        verify(accountRepository, never()).saveToCSV(anyList(), anyBoolean());
    }

    /**
     * Tests handleDeleteUsers for a non-empty set branch that
     * should filter the specified users and save the remaining list, then prompt for success
     */
    @Test
    void testHandleDeleteUsersNonEmpty() throws Exception {
        List<User> accounts = new ArrayList<>(Arrays.asList(
                new User("u1","p","ph","e","M","addr","ts",User.AccountStatus.ACTIVE,"Personal",0.0),
                new User("u2","p","ph","e","F","addr","ts",User.AccountStatus.ACTIVE,"Personal",0.0)
        ));
        when(accountRepository.readFromCSV()).thenReturn(accounts);

        Set<String> toDelete = new HashSet<>();
        toDelete.add("u1");

        Method m = AdminController.class.getDeclaredMethod("handleDeleteUsers", Set.class);
        m.setAccessible(true);
        m.invoke(controller, toDelete);

        verify(accountRepository).saveToCSV(
                argThat(savedList ->
                        savedList.size() == 1 &&
                                savedList.get(0).getUsername().equals("u2")
                ),
                eq(false)
        );
        loginComponentsMock.verify(() ->
                LoginComponents.showCustomMessage(
                        eq(adminView),
                        eq("Selected users deleted successfully."),
                        eq("Success"),
                        eq(JOptionPane.INFORMATION_MESSAGE)
                ), times(1)
        );
    }

    /**
     * Test handleLogout, should call view.dispose()
     */
    @Test
    void testHandleLogout() throws Exception {
        Method m = AdminController.class.getDeclaredMethod("handleLogout");
        m.setAccessible(true);
        m.invoke(controller);

        verify(adminView).dispose();
    }
}
