/**
 * Provides utility methods for normalizing transaction amounts and timestamps.
 * Integrates with an exchange rate service to convert foreign currencies to CNY and standardizes time formats.
 *
 * @author Group 19
 * @version 1.0
 */
package utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import Service.ExchangeRateService;

public class TransactionUtils {
    // No longer hardcodes exchange rates, relies on injected service
    private final ExchangeRateService rateService;
    private static final DateTimeFormatter OUT_FMT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    /**
     * Constructs a TransactionUtils instance with the specified exchange rate service.
     * Immediately fetches the latest exchange rates.
     *
     * @param rateService the service for accessing exchange rate data
     */
    public TransactionUtils(ExchangeRateService rateService) {
        this.rateService = rateService;
        // Immediately load the latest exchange rates
        rateService.fetchExchangeRates(rates -> {}, err -> System.err.println("Rate fetch error: " + err));
    }

    /**
     * Normalizes a raw amount string to CNY. Converts amounts like "30 dollars", "$30", or "30 USD"
     * to CNY using real-time exchange rates. Treats "30 yuan", "30 CNY", or amounts without units as CNY.
     *
     * @param rawAmt the raw amount string (e.g., "30 USD", "$30", "30 yuan")
     * @return the normalized amount in CNY
     * @throws IllegalArgumentException if the amount is empty or the format is unrecognized
     * @throws RuntimeException if the required exchange rate is unavailable
     */
    public double normalizeAmount(String rawAmt) {
        if (rawAmt == null || rawAmt.isBlank()) {
            throw new IllegalArgumentException("Amount is empty");
        }
        Pattern p = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)(?:\\s*)([a-zA-Z$¥]*)");
        Matcher m = p.matcher(rawAmt.trim());
        if (!m.matches()) {
            throw new IllegalArgumentException("Amount format not recognised: " + rawAmt);
        }
        double value = Double.parseDouble(m.group(1));
        String unit = m.group(2).toUpperCase();

        // 1. US Dollar
        if (unit.contains("DOLLAR") || unit.equals("USD") || unit.equals("$")) {
            Double usd2cny = rateService.getExchangeRates().get("USD");
            if (usd2cny == null) throw new RuntimeException("USD exchange rate not available");
            return value / usd2cny;
        }
        // 2. Euro
        if (unit.contains("EUR") || unit.equals("€")) {
            Double eur2cny = rateService.getExchangeRates().get("EUR");
            if (eur2cny == null) throw new RuntimeException("EUR exchange rate not available");
            return value / eur2cny;
        }
        // 3. Other supported currencies
        if (!unit.isEmpty() && rateService.getExchangeRates().containsKey(unit)) {
            return value * rateService.getExchangeRates().get(unit);
        }
        // 4. Default to CNY
        return value;
    }

    /**
     * Converts relative time expressions like "today", "yesterday", or "last month" to a standardized
     * "yyyy/MM/dd HH:mm" format. Also parses specific dates like "2025/05/01 14:30" and reformats them.
     * Falls back to the current time if parsing fails.
     *
     * @param rawTime the raw time string (e.g., "today", "2025/05/01 14:30")
     * @return the normalized time in "yyyy/MM/dd HH:mm" format
     */
    public static String normalizeTime(String rawTime) {
        rawTime = rawTime.trim().toLowerCase();
        LocalDateTime dt;
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        switch (rawTime) {
            case "today":
                dt = LocalDateTime.of(today, LocalTime.now());
                break;
            case "yesterday":
                dt = LocalDateTime.of(today.minusDays(1), LocalTime.now());
                break;
            case "this week":
                dt = today.with(DayOfWeek.MONDAY).atTime(now);
                break;
            case "last week":
                dt = today.minusWeeks(1).with(DayOfWeek.MONDAY).atTime(now);
                break;
            case "next week":
                dt = today.plusWeeks(1).with(DayOfWeek.MONDAY).atTime(now);
                break;
            case "this month":
                dt = today.withDayOfMonth(1).atTime(now);
                break;
            case "last month":
                dt = today.minusMonths(1).withDayOfMonth(1).atTime(now);
                break;
            case "next month":
                dt = today.plusMonths(1).withDayOfMonth(1).atTime(now);
                break;
            case "this year":
                dt = today.withDayOfYear(1).atTime(now);
                break;
            case "last year":
                dt = today.minusYears(1).withDayOfYear(1).atTime(now);
                break;
            case "next year":
                dt = today.plusYears(1).withDayOfYear(1).atTime(now);
                break;
            default:
                // Attempt to parse common formats
                try {
                    dt = LocalDateTime.parse(rawTime, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
                } catch (Exception e1) {
                    try {
                        LocalDate d = LocalDate.parse(rawTime, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                        dt = d.atStartOfDay();
                    } catch (Exception e2) {
                        // Final fallback: use current time
                        dt = LocalDateTime.now();
                    }
                }
        }
        return dt.format(OUT_FMT);
    }
}