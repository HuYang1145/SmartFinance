package Repository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import Model.Transaction;
import Model.User;


public class TransactionRepository {

private static final DateTimeFormatter DATE_FORMATTER = Service.BudgetService.DATE_FORMATTER;
private static final String CSV_FILE = "transactions.csv";

    public List<Transaction> findTransactionsByUsername(String username) {
        //return TransactionCache.getCachedTransactions(username);
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",", -1);
                if (data.length >= 13 && data[0].equals(username)) {
                    transactions.add(new Transaction(
                            data[0], data[1], Double.parseDouble(data[2]), data[3], data[4], data[5],
                            data[6], data[7], data[8], data[9], data[10], data[11], data[12]
                    ));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading transactions: " + e.getMessage());
        }
        return transactions;
    }

    public List<Transaction> findAllTransactions() {
        //return TransactionCache.getCachedTransactions(null); 
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",", -1);
                if (data.length >= 13) {
                    transactions.add(new Transaction(
                            data[0], data[1], Double.parseDouble(data[2]), data[3], data[4], data[5],
                            data[6], data[7], data[8], data[9], data[10], data[11], data[12]
                    ));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading transactions: " + e.getMessage());
        }
        return transactions;
    }
    
    public List<Transaction> findTransactionsByUser(User user) {
        if (user == null) return new ArrayList<>();
        return findTransactionsByUsername(user.getUsername());
    }
    public List<Transaction> findWeeklyExpenses(String username, LocalDateTime startOfWeek) {
        List<Transaction> transactions = findTransactionsByUsername(username);
        LocalDateTime endOfWeek = startOfWeek.plusDays(7);
        return transactions.stream()
                .filter(t -> "Expense".equalsIgnoreCase(t.getOperation()))
                .filter(t -> {
                    try {
                        LocalDateTime transactionTime = LocalDateTime.parse(t.getTimestamp(), DATE_FORMATTER);
                        return !transactionTime.isBefore(startOfWeek) && transactionTime.isBefore(endOfWeek);
                    } catch (Exception e) {
                        System.err.println("Invalid timestamp: " + t.getTimestamp() + " - " + e.getMessage());
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }
    public List<Transaction> readTransactions(String username) {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("transactions.csv"))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",", -1); // Use -1 to keep empty trailing fields
                if (data.length >= 13 && data[0].equals(username)) {
                    transactions.add(new Transaction(
                        data[0], // accountUsername
                        data[1], // operation
                        Double.parseDouble(data[2]), // amount
                        data[3], // timestamp
                        data[4], // merchant
                        data[5], // type
                        data[6], // remark
                        data[7], // category
                        data[8], // paymentMethod
                        data[9], // location
                        data[10], // tag
                        data[11], // attachment
                        data[12] // recurrence
                    ));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public List<Transaction> readAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("transactions.csv"))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",", -1); // Use -1 to keep empty trailing fields
                if (data.length >= 13) {
                    transactions.add(new Transaction(
                        data[0], // accountUsername
                        data[1], // operation
                        Double.parseDouble(data[2]), // amount
                        data[3], // timestamp
                        data[4], // merchant
                        data[5], // type
                        data[6], // remark
                        data[7], // category
                        data[8], // paymentMethod
                        data[9], // location
                        data[10], // tag
                        data[11], // attachment
                        data[12] // recurrence
                    ));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return transactions;
    }

}