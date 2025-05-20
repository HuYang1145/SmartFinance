package Service;

import Model.User;
import Model.UserSession;
import Repository.AccountRepository;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginServiceTest {

    private AccountRepository mockAccountRepo;
    private LoginService loginService;

    @BeforeEach
    void setUp() {
        mockAccountRepo = mock(AccountRepository.class);
        loginService = new LoginService(mockAccountRepo);
    }

    @Test
    void loginUser_usernameEmpty_throwsException() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> loginService.loginUser(" ", "123"));
        assertEquals("Username cannot be empty!", e.getMessage());
    }

    @Test
    void loginUser_passwordEmpty_throwsException() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> loginService.loginUser("user1", "  "));
        assertEquals("Password cannot be empty!", e.getMessage());
    }

    @Test
    void loginUser_userNotExistOrPasswordWrong_returnsFalse() {
        when(mockAccountRepo.findByUsername("user1")).thenReturn(null);
        assertFalse(loginService.loginUser("user1", "123"));

        User mockUser = mock(User.class);
        when(mockUser.getPassword()).thenReturn("abc");
        when(mockAccountRepo.findByUsername("user1")).thenReturn(mockUser);
        assertFalse(loginService.loginUser("user1", "wrong"));
    }

    @Test
    void loginUser_frozenAccount_throwsException() {
        User frozenUser = mock(User.class);
        when(frozenUser.getPassword()).thenReturn("123");
        when(frozenUser.getAccountStatus()).thenReturn(User.AccountStatus.FROZEN);
        when(mockAccountRepo.findByUsername("user1")).thenReturn(frozenUser);

        Exception e = assertThrows(IllegalStateException.class, () ->
                loginService.loginUser("user1", "123"));
        assertTrue(e.getMessage().toLowerCase().contains("frozen"));
    }

    @Test
    void loginUser_success_setsUserSessionAndReturnsTrue() {
        User user = mock(User.class);
        when(user.getPassword()).thenReturn("123");
        when(user.getAccountStatus()).thenReturn(User.AccountStatus.ACTIVE);
        when(mockAccountRepo.findByUsername("user1")).thenReturn(user);

        try (MockedStatic<UserSession> userSessionMock = mockStatic(UserSession.class)) {
            boolean ok = loginService.loginUser("user1", "123");
            assertTrue(ok);
            userSessionMock.verify(() -> UserSession.setCurrentAccount(user), times(1));
        }
    }

    @Test
    void registerUser_anyFieldEmpty_throwsException() {
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                loginService.registerUser("user1", "pass", "", "a@b.com", "F", "addr", "Personal"));
        assertEquals("All fields must be filled!", e.getMessage());
    }

    @Test
    void registerUser_usernameExists_throwsException() {
        when(mockAccountRepo.findByUsername("user1")).thenReturn(new User());
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                loginService.registerUser("user1", "pass", "123", "a@b.com", "F", "addr", "Personal"));
        assertTrue(e.getMessage().toLowerCase().contains("exists"));
    }

    @Test
    void registerUser_success_savesUser() {
        when(mockAccountRepo.findByUsername("newuser")).thenReturn(null);
        doNothing().when(mockAccountRepo).save(any(User.class));
        loginService.registerUser("newuser", "pass", "123", "a@b.com", "F", "addr", "Personal");
        verify(mockAccountRepo, times(1)).save(any(User.class));
    }
}
