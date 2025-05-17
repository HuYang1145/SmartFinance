import Model.User;
import Repository.AccountRepository;
import Repository.TransactionRepository;
import Service.LoginService;
import Model.Transaction;
import Controller.TransactionController;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserIntegrationTest {

    private File userFile;
    private File txFile;
    private AccountRepository accountRepo;
    private TransactionRepository txRepo;
    private LoginService loginService;

    @BeforeEach
    void setup(@TempDir Path tempDir) throws IOException {
        userFile = tempDir.resolve("accounts.csv").toFile();
        txFile = tempDir.resolve("transactions.csv").toFile();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(userFile, StandardCharsets.UTF_8))) {
            bw.write(AccountRepository.EXPECTED_ACCOUNT_HEADER);
            bw.newLine();
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(txFile, StandardCharsets.UTF_8))) {
            bw.write("user,operation,amount,time,merchant,type,remark,category,payment_method,location,tag,attachment,recurrence");
            bw.newLine();
        }
        accountRepo = new AccountRepository(userFile.getAbsolutePath());
        TransactionRepository.setCsvFilePathForTest(txFile.getAbsolutePath());
        TransactionController.setCsvFilePathForTest(txFile.getAbsolutePath());
        txRepo = new TransactionRepository();

        loginService = new LoginService(accountRepo);
    }

    @Test
    void register_login_addAndReadTransaction_success() {
        loginService.registerUser("testuser", "testpw", "133", "a@b.com", "F", "SH", "Personal");
        User user = accountRepo.findByUsername("testuser");
        assertNotNull(user);

        boolean loggedIn = loginService.loginUser("testuser", "testpw");
        assertTrue(loggedIn);



        boolean ok = TransactionController.addTransaction("testuser", "Expense", 100.0, "2024/05/19 10:00", "shop", "Shopping");
        assertTrue(ok);


        List<Transaction> txs = TransactionRepository.readTransactions("testuser");
        assertEquals(1, txs.size());
        Transaction t = txs.get(0);
        assertEquals("testuser", t.getAccountUsername());
        assertEquals(100.0, t.getAmount());
        assertEquals("Expense", t.getOperation());
    }
}
