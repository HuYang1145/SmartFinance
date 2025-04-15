package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CsvDataManager {
    public List<Transaction> readTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("transactions.csv"))) {
            String line;
            br.readLine(); // 跳过表头
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String username = parts[0];
                String type = parts[1];
                double amount = Double.parseDouble(parts[2]);
                LocalDateTime timestamp = LocalDateTime.parse(parts[4], DateTimeFormatter.ofPattern("yyyy/M/d HH:mm"));
                transactions.add(new Transaction(username, type, amount, timestamp));
                System.out.println("解析到的时间戳: " + timestamp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transactions;
    }

    // 读取月度预算
    public static double loadMonthlyBudget(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader("budget.csv"))) {
            String line;
            br.readLine(); // 跳过表头
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(username)) {
                    return Double.parseDouble(parts[1]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}