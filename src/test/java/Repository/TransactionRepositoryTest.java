package Repository;

import Model.Transaction;
import Model.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionRepositoryTest {

    private File txFile;

    @BeforeEach
    void setup(@TempDir Path tempDir) throws IOException {
        txFile = tempDir.resolve("transactions.csv").toFile();


        try (BufferedWriter bw = new BufferedWriter(new FileWriter(txFile, StandardCharsets.UTF_8))) {
            bw.write("user,operation,amount,time,merchant,type,remark,category,payment_method,location,tag,attachment,recurrence");
            bw.newLine();
            bw.write("zhangsan,Expense,22.50,2024/05/01 12:34,market,Pay,good,Food,,SH,tag,,"); bw.newLine();
            bw.write("zhangsan,Income,888.00,2024/05/02 11:23,company,Salary,,,Salary,,,," ); bw.newLine();
            bw.write("lisi,Expense,300.00,2024/05/03 09:00,shop,Buy,bad,Snacks,,BJ,tag2,,"); bw.newLine();
        }
        TransactionRepository.setCsvFilePathForTest(txFile.getAbsolutePath());
    }

    @Test
    void findTransactionsByUsername_found() {
        TransactionRepository repo = new TransactionRepository() {
            @Override
            public List<Transaction> findTransactionsByUsername(String username) {
                return super.findTransactionsByUsername(username);
            }
            @Override
            public List<Transaction> findAllTransactions() {
                return super.findAllTransactions();
            }
        };
        List<Transaction> zhang = repo.findTransactionsByUsername("zhangsan");
        assertEquals(2, zhang.size());
        assertTrue(zhang.stream().anyMatch(tx -> tx.getOperation().equals("Expense")));
        assertTrue(zhang.stream().anyMatch(tx -> tx.getAmount() == 888.0));
    }

    @Test
    void findTransactionsByUsername_notFound() {
        TransactionRepository repo = new TransactionRepository();
        List<Transaction> empty = repo.findTransactionsByUsername("nosuchuser");
        assertTrue(empty.isEmpty());
    }

    @Test
    void findAllTransactions_readsAll() {
        TransactionRepository repo = new TransactionRepository();
        List<Transaction> all = repo.findAllTransactions();
        assertTrue(all.size() >= 3);
    }

    @Test
    void findTransactionsByUser_nullUser_returnsEmpty() {
        TransactionRepository repo = new TransactionRepository();
        assertTrue(repo.findTransactionsByUser(null).isEmpty());
    }

    @Test
    void findTransactionsByUser_normal() {
        TransactionRepository repo = new TransactionRepository();
        User user = new User("zhangsan", "", "", "", "", "", "", User.AccountStatus.ACTIVE, "", 0);
        List<Transaction> txs = repo.findTransactionsByUser(user);
        assertEquals(2, txs.size());
    }

    @Test
    void findWeeklyExpenses_inRange() {
        TransactionRepository repo = new TransactionRepository();
        LocalDateTime weekStart = LocalDateTime.of(2024, 5, 1, 0, 0);
        List<Transaction> weekly = repo.findWeeklyExpenses("zhangsan", weekStart);
        assertTrue(weekly.stream().allMatch(tx -> tx.getOperation().equalsIgnoreCase("Expense")));
    }


    @Test
    void readAllTransactions_found() {
        TransactionRepository repo = new TransactionRepository();
        List<Transaction> list = repo.readAllTransactions();
        assertTrue(list.size() >= 3);
    }
}
