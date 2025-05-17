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
        // 1. 写入账单表头
        txFile = tempDir.resolve("transactions.csv").toFile();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(txFile, false))) {
            bw.write("user,operation,amount,time,merchant,type,remark,category,payment_method,location,tag,attachment,recurrence");
            bw.newLine();
        }
        // 2. 绑定数据文件
        TransactionRepository.setCsvFilePathForTest(txFile.getAbsolutePath());
        TransactionController.setCsvFilePathForTest(txFile.getAbsolutePath());
        txRepo = new TransactionRepository();
        budgetService = new BudgetService(txRepo);
        txService = new TransactionService(txRepo, budgetService);

        // 3. 创建测试用户，creationTime 用本地动态时间
        String nowTime = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + " 09:00";
        user = new User("alice", "pw", "phone", "mail", "F", "ad", nowTime,
                User.AccountStatus.ACTIVE, "Personal", 0.0);
    }

    @Test
    void test_MonthlyExpense_and_ReportSummary() {
        // 1. 构造多条账单：本月、本月不同天、非本月、不同用户
        String thisMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String day1 = thisMonth + "/05 12:00";
        String day2 = thisMonth + "/08 09:30";
        String notThisMonth = "2023/12/22 18:00";
        System.out.println("[TEST] 添加账单日期: " + day1);
        System.out.println("[TEST] 添加账单日期: " + day2);
        LocalDate now = LocalDate.now();
        System.out.println("[TEST] 当前系统年月: " + now.getYear() + "-" + now.getMonthValue());

        // 当前用户多条账单
        assertTrue(TransactionController.addTransaction("alice", "Expense", 100.0, day1, "shopA", "Food"));
        assertTrue(TransactionController.addTransaction("alice", "Expense", 200.0, day2, "shopB", "Snacks"));
        assertTrue(TransactionController.addTransaction("alice", "Income", 300.0, day2, "company", "Salary"));
        assertTrue(TransactionController.addTransaction("alice", "Expense", 999.0, notThisMonth, "oldshop", "Other"));
        // 其他用户账单
        assertTrue(TransactionController.addTransaction("bob", "Expense", 888.0, day2, "mall", "Buy"));

        // 2. 读取账单，验证 alice 总账单数量（含非本月、含收入、支出）
        List<Transaction> all = TransactionRepository.readTransactions("alice");
        System.out.println("[TEST] 读到账单条目数: " + all.size());
        for (Transaction t : all) {
            System.out.printf("[TEST] 账单: %s %s %.2f\n", t.getTimestamp(), t.getOperation(), t.getAmount());
        }

        assertEquals(4, all.size());

        System.out.println("写入后文件内容：");
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

        // 3. 计算本月支出，只统计本月"Expense"类型
        double expense = txService.getCurrentMonthExpense(user);
        assertEquals(100.0 + 200.0, expense, 0.001);

        // 4. 生成报表字符串
        String report = txService.buildTransactionSummary(user);

        // 报表应包含这几条金额
        assertTrue(report.contains("100.00"));
        assertTrue(report.contains("200.00"));
        assertTrue(report.contains("300.00")); // 本月收入
        assertTrue(report.contains("999.00")); // 去年支出
        assertTrue(report.contains("shopA"));
        assertTrue(report.contains("company"));

        // 不应包含其他用户
        assertFalse(report.contains("bob"));

        // 可选：报表内容格式检查
        assertTrue(report.startsWith("Operation,Amount,Time,Merchant/Payee,Type,Category"));
    }

}
