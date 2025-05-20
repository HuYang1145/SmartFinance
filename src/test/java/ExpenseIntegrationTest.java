import Model.Transaction;
import Model.User;
import Repository.TransactionRepository;
import Service.TransactionService;
import Service.BudgetService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import Controller.TransactionController;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseIntegrationTest {

    private File txFile;
    private TransactionRepository txRepo;
    private BudgetService budgetService;
    private TransactionService txService;
    private User user;

    @BeforeEach
    void setup(@TempDir Path tempDir) throws IOException {
        txFile = tempDir.resolve("transactions.csv").toFile();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(txFile, false))) {
            bw.write("user,operation,amount,time,merchant,type,remark,category,payment_method,location,tag,attachment,recurrence");
            bw.newLine();
        }
        TransactionRepository.setCsvFilePathForTest(txFile.getAbsolutePath());
        TransactionController.setCsvFilePathForTest(txFile.getAbsolutePath());
        txRepo = new TransactionRepository();
        budgetService = new BudgetService(txRepo);
        txService = new TransactionService(txRepo, budgetService);

        String nowTime = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + " 09:00";
        user = new User("alice", "pw", "phone", "mail", "F", "ad", nowTime,
                User.AccountStatus.ACTIVE, "Personal", 0.0);
    }

    @Test
    void test_MonthlyExpense_and_ReportSummary() {
        String thisMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String day1 = thisMonth + "/05 12:00";
        String day2 = thisMonth + "/08 09:30";
        String notThisMonth = "2023/12/22 18:00";
        LocalDate now = LocalDate.now();
        assertTrue(TransactionController.addTransaction("alice", "Expense", 100.0, day1, "shopA", "Food"));
        assertTrue(TransactionController.addTransaction("alice", "Expense", 200.0, day2, "shopB", "Snacks"));
        assertTrue(TransactionController.addTransaction("alice", "Income", 300.0, day2, "company", "Salary"));
        assertTrue(TransactionController.addTransaction("alice", "Expense", 999.0, notThisMonth, "oldshop", "Other"));
        assertTrue(TransactionController.addTransaction("bob", "Expense", 888.0, day2, "mall", "Buy"));

        List<Transaction> all = TransactionRepository.readTransactions("alice");


        assertEquals(4, all.size());


        try (BufferedReader br = new BufferedReader(new FileReader(txFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        double expense = txService.getCurrentMonthExpense(user);
        assertEquals(100.0 + 200.0, expense, 0.001);

        String report = txService.buildTransactionSummary(user);

        assertTrue(report.contains("100.00"));
        assertTrue(report.contains("200.00"));
        assertTrue(report.contains("300.00"));
        assertTrue(report.contains("999.00"));
        assertTrue(report.contains("shopA"));
        assertTrue(report.contains("company"));

        assertFalse(report.contains("bob"));

        assertTrue(report.startsWith("Operation,Amount,Time,Merchant/Payee,Type,Category"));
    }

}
