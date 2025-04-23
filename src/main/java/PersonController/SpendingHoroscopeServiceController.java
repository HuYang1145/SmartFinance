package PersonController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AccountModel.TransactionServiceModel.TransactionData;
import PersonModel.HoroscopeReportModel;
import PersonModel.TransactionAnalyzerModel;

/**
 * Controller service for generating personalized spending horoscope reports based on user transactions.
 */
public class SpendingHoroscopeServiceController {

    private static final Logger logger = LoggerFactory.getLogger(SpendingHoroscopeServiceController.class);

    private static final DateTimeFormatter TRANSACTION_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd[ HH:mm]");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private static final String TYPE_FOODIE = "Foodie Star";
    private static final String TYPE_ENTERTAINMENT = "Entertainment Guru";
    private static final String TYPE_SHOPPING = "Shopping Ace";
    private static final String TYPE_TRANSPORT = "Transport Titan";
    private static final String TYPE_FITNESS = "Fitness Fanatic";
    private static final String TYPE_TRAVEL = "Travel Tracker";
    private static final String TYPE_BALANCED = "Balanced Budgeter";
    private static final String TYPE_UNCATEGORIZED = "Mystery Spender";
    private static final String TYPE_NO_SPENDING = "Frugal Friend";

    private static final Map<String, ReportDetails> reportTemplates = Map.ofEntries(
            Map.entry(TYPE_FOODIE, new ReportDetails("This Week: Foodie Star!", "Looks like delicious food (%.0f%%) topped your spending chart this week! Remember, homemade can be just as tasty (and cheaper!).", "food.jpg")),
            Map.entry(TYPE_ENTERTAINMENT, new ReportDetails("This Week: Entertainment Guru!", "Fun times ruled your budget (%.0f%%)! Hope you enjoyed it. Maybe explore some free activities next week?", "entertainment.jpg")),
            Map.entry(TYPE_SHOPPING, new ReportDetails("This Week: Shopping Ace!", "Retail therapy or grabbing essentials? Shopping took the lead (%.0f%%). Did you stick to your list?", "shopping.jpg")),
            Map.entry(TYPE_TRANSPORT, new ReportDetails("This Week: Transport Titan!", "Getting around was your main expense (%.0f%%). Consider carpooling or public transport if possible!", "transport.jpg")),
            Map.entry(TYPE_FITNESS, new ReportDetails("This Week: Fitness Fanatic!", "Investing in health and fitness (%.0f%%)! Keep up the great work towards your goals!", "fitness.jpg")),
            Map.entry(TYPE_TRAVEL, new ReportDetails("This Week: Travel Tracker!", "Adventures calling? Travel expenses were significant (%.0f%%). Hope you're making memories!", "travel.jpg")),
            Map.entry(TYPE_UNCATEGORIZED, new ReportDetails("This Week: Mystery Spender!", "A chunk of your spending (%.0f%%) is uncategorized. Knowing where money goes is the first step to managing it!", "mystery_monster.png")),
            Map.entry(TYPE_BALANCED, new ReportDetails("This Week: Balanced Budgeter!", "Your spending seems well-distributed across categories! Maintaining balance is key.", "balanced_monster.png")),
            Map.entry(TYPE_NO_SPENDING, new ReportDetails("This Week: Frugal Friend!", "Zero expenses logged this week! Saving like a champ!", "frugal_monster.png")),
            Map.entry("ERROR_TYPE", new ReportDetails("Error", "Error", "error_monster.png"))
    );

    private static class ReportDetails {
        final String title;
        final String descriptionTemplate;
        final String imagePath;
        ReportDetails(String title, String desc, String img) {
            this.title = title;
            this.descriptionTemplate = desc;
            this.imagePath = img;
        }
    }

    /**
     * Generates a weekly spending horoscope report based on user transactions.
     *
     * @param username The username to fetch transactions for.
     * @return A HoroscopeReportModel containing the report details.
     */
    public HoroscopeReportModel generateWeeklyReport(String username) {
        if (username == null || username.trim().isEmpty()) {
            logger.error("Username is null or empty.");
            return getDefaultErrorReport();
        }

        try {
            // Get date range for the current week (Monday to Sunday)
            LocalDate today = LocalDate.now();
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
            logger.debug("Analyzing week: {} to {}", startOfWeek, endOfWeek);

            // Get all transactions for the user
            List<TransactionData> allTransactions = TransactionAnalyzerModel.getFilteredTransactions(username, startOfWeek.toString().substring(0, 7)); // yyyy/MM
            if (allTransactions == null) {
                logger.error("Failed to read transactions for user {}", username);
                return getDefaultErrorReport();
            }

            // Filter transactions for the current week's expenses
            List<TransactionData> weeklyExpenses = new ArrayList<>();
            for (TransactionData tx : allTransactions) {
                if ("Expense".equalsIgnoreCase(tx.getOperation())) {
                    try {
                        String time = tx.getTime();
                        LocalDateTime transactionDateTime;
                        if (time.matches("\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}")) {
                            transactionDateTime = LocalDateTime.parse(time, TRANSACTION_TIME_FORMATTER);
                        } else {
                            transactionDateTime = LocalDate.parse(time, DATE_ONLY_FORMATTER).atStartOfDay();
                        }
                        LocalDate transactionDate = transactionDateTime.toLocalDate();
                        if (!transactionDate.isBefore(startOfWeek) && !transactionDate.isAfter(endOfWeek)) {
                            weeklyExpenses.add(tx);
                        }
                    } catch (DateTimeParseException e) {
                        logger.error("Skipping transaction due to date parse error: {}", tx.getTime(), e);
                    } catch (Exception e) {
                        logger.error("Error processing transaction: {} - {}", tx, e.getMessage());
                    }
                }
            }
            logger.debug("Found {} expenses this week.", weeklyExpenses.size());

            // Check if there were any expenses this week
            if (weeklyExpenses.isEmpty()) {
                ReportDetails details = reportTemplates.get(TYPE_NO_SPENDING);
                if (details == null) {
                    logger.error("No spending template not found.");
                    return getDefaultErrorReport();
                }
                return new HoroscopeReportModel(details.title, details.descriptionTemplate, details.imagePath);
            }

            // Calculate category totals for this week's expenses
            Map<String, Double> categoryTotals = TransactionAnalyzerModel.calculateExpenseCategoryTotals(weeklyExpenses);

            // Calculate total weekly expense
            double totalWeeklyExpense = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();
            logger.debug("Weekly expense total: {}", totalWeeklyExpense);
            logger.debug("Category Totals: {}", categoryTotals);

            // Find dominant category
            Optional<Map.Entry<String, Double>> dominantEntry = categoryTotals.entrySet().stream()
                    .filter(entry -> entry.getValue() > 0)
                    .max(Map.Entry.comparingByValue());

            if (dominantEntry.isPresent()) {
                Map.Entry<String, Double> dominantCategory = dominantEntry.get();
                String categoryName = dominantCategory.getKey();
                double categoryAmount = dominantCategory.getValue();
                double percentage = (totalWeeklyExpense > 0) ? (categoryAmount / totalWeeklyExpense) * 100.0 : 0.0;

                logger.debug("Dominant Category: {} ({}%)", categoryName, String.format("%.1f", percentage));

                // Determine Horoscope Type
                String horoscopeType = mapCategoryToHoroscopeType(categoryName);
                ReportDetails details = reportTemplates.getOrDefault(horoscopeType, reportTemplates.get(TYPE_BALANCED));
                String description = String.format(details.descriptionTemplate, percentage);

                // Check image resource
                String imagePath = details.imagePath;
                if (!new java.io.File("src/main/resources/images/" + imagePath).exists()) {
                    logger.warn("Image file not found: {}", imagePath);
                    imagePath = reportTemplates.get("ERROR_TYPE").imagePath;
                }

                return new HoroscopeReportModel(details.title, description, imagePath);
            } else {
                logger.debug("No dominant category found, defaulting to Balanced.");
                ReportDetails details = reportTemplates.get(TYPE_BALANCED);
                if (details == null) {
                    logger.error("Balanced template not found.");
                    return getDefaultErrorReport();
                }
                String imagePath = details.imagePath;
                if (!new java.io.File("src/main/resources/images/" + imagePath).exists()) {
                    logger.warn("Image file not found: {}", imagePath);
                    imagePath = reportTemplates.get("ERROR_TYPE").imagePath;
                }
                return new HoroscopeReportModel(details.title, details.descriptionTemplate, imagePath);
            }
        } catch (Exception e) {
            logger.error("Error generating weekly report for user {}: {}", username, e.getMessage());
            return getDefaultErrorReport();
        }
    }

    /**
     * Maps the dominant spending category name to a specific Horoscope Type.
     *
     * @param categoryName The name of the dominant spending category.
     * @return The corresponding horoscope type.
     */
    private String mapCategoryToHoroscopeType(String categoryName) {
        if (categoryName == null) return TYPE_UNCATEGORIZED;
        String lowerCaseCategory = categoryName.toLowerCase();
        switch (lowerCaseCategory) {
            case "food":
            case "dining":
            case "restaurant":
                return TYPE_FOODIE;
            case "entertainment":
            case "movies":
            case "games":
                return TYPE_ENTERTAINMENT;
            case "shopping":
            case "clothes":
            case "electronics":
                return TYPE_SHOPPING;
            case "transport":
            case "traffic":
            case "gas":
            case "taxi":
                return TYPE_TRANSPORT;
            case "fitness":
            case "gym":
            case "sports":
                return TYPE_FITNESS;
            case "travel":
            case "vacation":
            case "flights":
            case "hotels":
                return TYPE_TRAVEL;
            case "unclassified":
                return TYPE_UNCATEGORIZED;
            default:
                logger.debug("Unmapped dominant category '{}', defaulting to Balanced.", categoryName);
                return TYPE_BALANCED;
        }
    }

    /**
     * Provides a default report in case of errors.
     *
     * @return A default HoroscopeReportModel object.
     */
    public HoroscopeReportModel getDefaultErrorReport() {
        ReportDetails errorDetails = reportTemplates.get("ERROR_TYPE");
        String imagePath = (errorDetails != null) ? errorDetails.imagePath : "error_monster.png";
        return new HoroscopeReportModel(
                "Report Unavailable",
                "Sorry, couldn't generate your spending horoscope this week. Please check back later or ensure transactions are recorded correctly.",
                imagePath
        );
    }
}