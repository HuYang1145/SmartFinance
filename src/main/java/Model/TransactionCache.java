package Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Controller.TransactionController;

public class TransactionCache {
    private static Map<String, List<Transaction>> transactionCache = new HashMap<>();
    private static Map<String, Long> cacheTimestamps = new HashMap<>();
    private static final long CACHE_EXPIRY_MS = 5 * 60 * 1000;

    private static boolean isCacheValid(String username) {
        Long timestamp = cacheTimestamps.get(username);
        if (timestamp == null) return false;
        return (System.currentTimeMillis() - timestamp) < CACHE_EXPIRY_MS;
    }

    public static List<Transaction> getCachedTransactions(String username) {
        if (isCacheValid(username)) {
            return transactionCache.getOrDefault(username, new ArrayList<>());
        }
        List<Transaction> transactions = TransactionController.readTransactions(username);
        transactionCache.put(username, transactions);
        cacheTimestamps.put(username, System.currentTimeMillis());
        return transactionCache.get(username);
    }

    public static List<Transaction> getFilteredTransactions(String username, String yearMonth) {
        List<Transaction> transactions = getCachedTransactions(username);
        List<Transaction> filtered = new ArrayList<>();
        String yearMonthPrefix = yearMonth + "/";

        for (Transaction tx : transactions) {
            try {
                String timestamp = tx.getTimestamp();
                if (timestamp.startsWith(yearMonthPrefix)) {
                    filtered.add(tx);
                }
            } catch (Exception e) {
                System.err.println("Error processing transaction timestamp: " + tx.getTimestamp());
            }
        }
        return filtered;
    }

    public static void invalidateCache(String username) {
        transactionCache.remove(username);
        cacheTimestamps.remove(username);
    }
}