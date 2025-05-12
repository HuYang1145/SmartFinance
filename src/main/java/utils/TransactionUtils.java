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
    // 不再硬编码汇率，而是依赖注入这个服务
    private final ExchangeRateService rateService;
    private static final DateTimeFormatter OUT_FMT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    public TransactionUtils(ExchangeRateService rateService) {
        this.rateService = rateService;
        // 立即加载最新汇率
        rateService.fetchExchangeRates(rates -> {}, err -> System.err.println("Rate fetch error: " + err));
    }

    /**
     * 把 "30 dollars"/"$30"/"30 USD" → 根据实时汇率算成人民币
     * 把 "30 yuan"/"30 CNY"/没有单位 → 直接当人民币
     */
    public double normalizeAmount(String rawAmt) {
        if (rawAmt == null || rawAmt.isBlank()) {
            return 0.0; // 或 return -1 作为非法金额标记也可以
        }

        try {
            String norm = rawAmt.trim().toUpperCase().replace("CHY", "CNY"); // 修正常见错误
            Pattern p = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)(?:\\s*)([A-Z$¥]*)");
            Matcher m = p.matcher(norm);
            if (!m.matches()) {
                return Double.parseDouble(norm.replaceAll("[^0-9.]", "")); // fallback: 去除单位直接解析数值
            }

            double value = Double.parseDouble(m.group(1));
            String unit = m.group(2);

            if (unit.contains("DOLLAR") || unit.equals("USD") || unit.equals("$")) {
                Double usd = rateService.getExchangeRates().get("USD");
                if (usd != null) return value * usd;
            }
            if (unit.contains("EUR") || unit.equals("€")) {
                Double eur = rateService.getExchangeRates().get("EUR");
                if (eur != null) return value * eur;
            }
            if (!unit.isEmpty()) {
                Double rate = rateService.getExchangeRates().get(unit);
                if (rate != null) return value * rate;
            }

            // 默认：无法识别或无汇率，就视为人民币原值
            return value;

        } catch (Exception e) {
            System.err.println("[Amount Warning] Failed to normalize amount: " + rawAmt + ", reason: " + e.getMessage());
            return 0.0; // fallback：报错时返回 0，或者 return original value if preferred
        }
    }

    /**
     * 把 "today"/"yesterday"/"last month" 等相对时间转成 "yyyy/MM/dd HH:mm" 格式。
     * 如果已经是具体日期（如 "2025/05/01 14:30"），就尝试 parse 并返回格式化后的。
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
                // 尝试解析常见格式
                try {
                    dt = LocalDateTime.parse(rawTime, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
                } catch (Exception e1) {
                    try {
                        LocalDate d = LocalDate.parse(rawTime, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                        dt = d.atStartOfDay();
                    } catch (Exception e2) {
                        // 最后 fallback：用当前时间
                        dt = LocalDateTime.now();
                    }
                }
        }
        return dt.format(OUT_FMT);
    }
}

