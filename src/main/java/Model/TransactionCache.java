package Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Controller.TransactionController;

/**
 * A cache for storing and managing transaction data in the Smart Finance Application.
 * This class provides methods to cache transactions for users, retrieve cached or filtered transactions,
 * and invalidate the cache when necessary. The cache has a fixed expiry time to ensure data freshness.
 *
 * @author Group 19
 * @version 1.0
 */
public class TransactionCache {
    /** Cache storing transactions for each user, mapped by username. */
    private static Map<String, List<Transaction>> transactionCache = new HashMap<>();

    /** Timestamps of when each user's transactions were cached, mapped by username. */
    private static Map<String, Long> cacheTimestamps = new HashMap<>();

    /** Cache expiry duration in milliseconds (5 minutes). */
    private static final long CACHE_EXPIRY_MS = 5 * 60 * 1000;

    /**
     * Checks if the cache for a given user is still valid based on the expiry time.
     *
     * @param username The username whose cache validity is to be checked.
     * @return {@code true} if the cache is valid, {@code false} otherwise.
     */
    private static boolean isCacheValid(String username) {
        Long timestamp = cacheTimestamps.get(username);
        if (timestamp == null) return false;
        return (System.currentTimeMillis() - timestamp) < CACHE_EXPIRY_MS;
    }

    /**
     * Retrieves cached transactions for a given user. If the cache is invalid or empty,
     * it fetches fresh transactions from the {@link TransactionController} and updates the cache.
     *
     * @param username The username whose transactions are to be retrieved.
     * @return A list of {@link Transaction} objects for the user, or an empty list if none exist.
     */
    public static List<Transaction> getCachedTransactions(String username) {
        if (isCacheValid(username)) {
            return transactionCache.getOrDefault(username, new ArrayList<>());
        }
        List<Transaction> transactions = TransactionController.readTransactions(username);
        transactionCache.put(username, transactions);
        cacheTimestamps.put(username, System.currentTimeMillis());
        return transactionCache.get(username);
    }

    /**
     * Retrieves transactions for a given user filtered by a specific year and month.
     * Uses cached transactions and filters them based on the provided year-month prefix.
     *
     * @param username   The username whose transactions are to be retrieved.
     * @param yearMonth  The year and month to filter transactions by, in the format "YYYY/MM".
     * @return A list of {@link Transaction} objects matching the year-month filter.
     */
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

    /**
     * Invalidates the cache for a given user, removing their transactions and timestamp.
     *
     * @param username The username whose cache is to be invalidated.
     */
    public static void invalidateCache(String username) {
        transactionCache.remove(username);
        cacheTimestamps.remove(username);
    }
}