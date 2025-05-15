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

    // 用于捕获静态提示框调用
    private MockedStatic<LoginComponents> loginComponentsMock;

    private AdminController controller;

    @BeforeEach
    void setUp() throws Exception {
        // 1. Mock 静态方法
        loginComponentsMock = Mockito.mockStatic(LoginComponents.class);

        // 2. 实例化控制器，并通过反射注入 mockView
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
     * 测试 handleCustomerInquiry 成功分支，
     * 应调用 view.updateAccountTable(accounts)
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
        // 传入 null 参数（方法内部并未使用）
        m.invoke(controller, (Object) null);

        verify(adminView).updateAccountTable(accounts);
    }

    /**
     * 测试 handleCustomerInquiry 异常分支，
     * 仓库抛异常时应调 showCustomMessage
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
     * 测试 handleDeleteUsers 的空集合分支，
     * 应提示“未选中用户”
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
        // 不应调用保存
        verify(accountRepository, never()).saveToCSV(anyList(), anyBoolean());
    }

    /**
     * 测试 handleDeleteUsers 的非空集合分支，
     * 应过滤指定用户并保存剩余列表，然后提示成功
     */
    @Test
    void testHandleDeleteUsersNonEmpty() throws Exception {
        // 原始列表包含 u1、u2
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

        // 保存时只剩 u2
        verify(accountRepository).saveToCSV(
                argThat(savedList ->
                        savedList.size() == 1 &&
                                savedList.get(0).getUsername().equals("u2")
                ),
                eq(false)
        );
        // 并提示成功
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
     * 测试 handleLogout，应该调用 view.dispose()
     */
    @Test
    void testHandleLogout() throws Exception {
        Method m = AdminController.class.getDeclaredMethod("handleLogout");
        m.setAccessible(true);
        m.invoke(controller);

        verify(adminView).dispose();
    }
}
