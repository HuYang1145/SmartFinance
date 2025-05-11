package Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import Model.Transaction;
import Model.User;
import Repository.TransactionRepository;

public class TransactionService {
    private static final DateTimeFormatter TRANSACTION_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public double getBalance(User user) {
        return user != null ? user.getBalance() : 0.0;
    }

    public String buildTransactionSummary(User user) {
        if (user == null) return "";
        List<Transaction> transactions = transactionRepository.findTransactionsByUser(user);
        if (transactions.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("Operation,Amount,Time,Merchant/Payee,Type\n");
        for (Transaction tx : transactions) {
            String merchant = tx.getMerchant() != null ? tx.getMerchant() : "";
            String type = tx.getType() != null ? tx.getType() : "";
            sb.append(tx.getOperation()).append(",")
              .append(String.format("%.2f", tx.getAmount())).append(",")
              .append(tx.getTimestamp()).append(",")
              .append(escapeForSummary(merchant)).append(",")
              .append(escapeForSummary(type)).append("\n");
        }
        return sb.toString();
    }

    public double getCurrentMonthExpense(User user) {
        if (user == null) return 0.0;
        List<Transaction> transactions = transactionRepository.findTransactionsByUser(user);
        double totalExpense = 0.0;
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        for (Transaction tx : transactions) {
            if ("Expense".equalsIgnoreCase(tx.getOperation())) {
                try {
                    LocalDateTime dt = LocalDateTime.parse(tx.getTimestamp(), TRANSACTION_TIME_FORMATTER);
                    if (dt.getYear() == currentYear && dt.getMonthValue() == currentMonth) {
                        totalExpense += tx.getAmount();
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("Date parse error: " + tx.getTimestamp());
                }
            }
        }
        return totalExpense;
    }

    private String escapeForSummary(String field) {
        if (field.contains(",")) return "\"" + field.replace("\"", "\"\"") + "\"";
        return field;
    }
}