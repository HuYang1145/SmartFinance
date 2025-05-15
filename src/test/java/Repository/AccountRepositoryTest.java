package Repository;

import Model.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AccountRepositoryTest {

    private File accountFile;
    private AccountRepository repo;

    @BeforeEach
    void setup(@TempDir Path tempDir) throws IOException {
        accountFile = tempDir.resolve("accounts.csv").toFile();
        // 写入表头（避免初次读取抛空）
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(accountFile, StandardCharsets.UTF_8))) {
            bw.write(AccountRepository.EXPECTED_ACCOUNT_HEADER);
            bw.newLine();
        }
        repo = new AccountRepository(accountFile.getAbsolutePath());
    }

    @Test
    void constructor_invalidPath_throws() {
        assertThrows(IllegalArgumentException.class, () -> new AccountRepository(null));
        assertThrows(IllegalArgumentException.class, () -> new AccountRepository("  "));
    }

    @Test
    void saveAndFindByUsername_success() {
        User user = new User("zhangsan", "pw123", "135", "zs@test.com", "M", "address", "2024/05/19 09:00", User.AccountStatus.ACTIVE, "Personal", 200.0);
        repo.save(user);

        User loaded = repo.findByUsername("zhangsan");
        assertNotNull(loaded);
        assertEquals("zhangsan", loaded.getUsername());
        assertEquals("pw123", loaded.getPassword());
        assertEquals("135", loaded.getPhone());
        assertEquals(User.AccountStatus.ACTIVE, loaded.getAccountStatus());
        assertEquals(200.0, loaded.getBalance());
    }

    @Test
    void findByUsername_userNotExist_returnsNull() {
        assertNull(repo.findByUsername("nosuchuser"));
    }

    @Test
    void saveToCSV_and_readFromCSV_batch_success() {
        User u1 = new User("a", "1", "p", "e", "M", "ad", "2024/01/01 11:00", User.AccountStatus.ACTIVE, "P", 123.0);
        User u2 = new User("b", "2", "p2", "e2", "F", "ad2", "2024/01/02 12:00", User.AccountStatus.FROZEN, "A", 88.6);

        // 覆盖写
        boolean ok = repo.saveToCSV(List.of(u1, u2), false);
        assertTrue(ok);

        List<User> users = repo.readFromCSV();
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("a")));
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("b")));

        // 检查数据内容
        User u = users.get(1);
        assertNotNull(u.getCreationTime());
        assertTrue(u.getBalance() > 0);
    }

    @Test
    void readFromCSV_emptyFile_returnsEmptyList() throws IOException {
        // 新建一个无内容文件
        File emptyFile = File.createTempFile("empty", ".csv");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(emptyFile, StandardCharsets.UTF_8))) {
            bw.write(AccountRepository.EXPECTED_ACCOUNT_HEADER);
            bw.newLine();
        }
        AccountRepository emptyRepo = new AccountRepository(emptyFile.getAbsolutePath());
        List<User> list = emptyRepo.readFromCSV();
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void readFromCSV_invalidData_skipLines() throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(accountFile, StandardCharsets.UTF_8))) {
            bw.write(AccountRepository.EXPECTED_ACCOUNT_HEADER); bw.newLine();
            bw.write("only,2,fields"); bw.newLine();
            bw.write("test,pass,150,mail,male,add,2024/01/01 10:00,ACTIVE,Type,321.99"); bw.newLine();
        }
        AccountRepository repo2 = new AccountRepository(accountFile.getAbsolutePath());
        List<User> users = repo2.readFromCSV();
        assertEquals(1, users.size());
        assertEquals("test", users.get(0).getUsername());
        assertEquals(321.99, users.get(0).getBalance());
    }
}
