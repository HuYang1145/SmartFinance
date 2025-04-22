package PersonModel;

import AccountModel.TransactionService;
import AccountModel.TransactionService.TransactionData;
// IMPORTANT: Make sure BillStatics is in the correct package (or adjust import)
// Assuming BillStatics is accessible (e.g., public in PersonModel)

import java.time.DayOfWeek;
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
import java.util.stream.Collectors;

public class SpendingHoroscopeService {

    // --- Keep TRANSACTION_TIME_FORMATTER ---
    private static final DateTimeFormatter TRANSACTION_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    // --- Keep Horoscope Types ---
    private static final String TYPE_FOODIE = "Foodie Star";
    private static final String TYPE_ENTERTAINMENT = "Entertainment Guru";
    private static final String TYPE_SHOPPING = "Shopping Ace";
    private static final String TYPE_TRANSPORT = "Transport Titan";
    private static final String TYPE_FITNESS = "Fitness Fanatic";
    private static final String TYPE_TRAVEL = "Travel Tracker";
    private static final String TYPE_BALANCED = "Balanced Budgeter";
    private static final String TYPE_UNCATEGORIZED = "Mystery Spender";
    private static final String TYPE_NO_SPENDING = "Frugal Friend";

    // --- MODIFIED ReportDetails and reportTemplates ---
    // Changed image paths to be FILENAMES ONLY to align with previous HoroscopePanel modification.
    private static final Map<String, ReportDetails> reportTemplates = Map.ofEntries(
        Map.entry(TYPE_FOODIE, new ReportDetails("This Week: Foodie Star!", "Looks like delicious food (%.0f%%) topped your spending chart this week! Remember, homemade can be just as tasty (and cheaper!).", "food.jpg")), // Filename only
        Map.entry(TYPE_ENTERTAINMENT, new ReportDetails("This Week: Entertainment Guru!", "Fun times ruled your budget (%.0f%%)! Hope you enjoyed it. Maybe explore some free activities next week?", "entertainment.jpg")), // Filename only
        Map.entry(TYPE_SHOPPING, new ReportDetails("This Week: Shopping Ace!", "Retail therapy or grabbing essentials? Shopping took the lead (%.0f%%). Did you stick to your list?", "shopping.jpg")), // Filename only
        Map.entry(TYPE_TRANSPORT, new ReportDetails("This Week: Transport Titan!", "Getting around was your main expense (%.0f%%). Consider carpooling or public transport if possible!", "transport.jpg")), // Filename only
        Map.entry(TYPE_FITNESS, new ReportDetails("This Week: Fitness Fanatic!", "Investing in health and fitness (%.0f%%)! Keep up the great work towards your goals!", "fitness.jpg")), // Filename only
        Map.entry(TYPE_TRAVEL, new ReportDetails("This Week: Travel Tracker!", "Adventures calling? Travel expenses were significant (%.0f%%). Hope you're making memories!", "travel.jpg")), // Filename only
        Map.entry(TYPE_UNCATEGORIZED, new ReportDetails("This Week: Mystery Spender!", "A chunk of your spending (%.0f%%) is uncategorized. Knowing where money goes is the first step to managing it!", "mystery_monster.png")), // Filename only
        Map.entry(TYPE_BALANCED, new ReportDetails("This Week: Balanced Budgeter!", "Your spending seems well-distributed across categories! Maintaining balance is key.", "balanced_monster.png")), // Filename only
        Map.entry(TYPE_NO_SPENDING, new ReportDetails("This Week: Frugal Friend!", "Zero expenses logged this week! Saving like a champ!", "frugal_monster.png")), // Filename only
        // Add an entry for the error report's image filename if needed by getDefaultErrorReport
        Map.entry("ERROR_TYPE", new ReportDetails("Error", "Error", "error_monster.png")) // Filename only for error
    );

    // Helper class for report details (keep as is)
    private static class ReportDetails {
        final String title;
        final String descriptionTemplate;
        final String imagePath; // Now stores ONLY the filename
        ReportDetails(String title, String desc, String img) {
            this.title = title; this.descriptionTemplate = desc; this.imagePath = img;
        }
    }

    // --- MODIFIED generateWeeklyReport ---
    public HoroscopeReport generateWeeklyReport(String username) {
        if (username == null || username.trim().isEmpty()) {
            System.err.println("SpendingHoroscopeService: Username is null or empty.");
            return getDefaultErrorReport();
        }

        try {
            // ================================================================
            // == START: Original Logic (Now Commented Out) ==
            // ================================================================
            /*
            // 1. Get date range for the current week (Monday to Sunday)
            LocalDate today = LocalDate.now();
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            System.out.println("DEBUG: Analyzing week: " + startOfWeek + " to " + endOfWeek); // Debug

            // 2. Get all transactions for the user
            List<TransactionData> allTransactions = TransactionService.readTransactions(username);
            if (allTransactions == null) {
                 System.err.println("SpendingHoroscopeService: Failed to read transactions for user " + username);
                 return getDefaultErrorReport();
            }

            // 3. Filter transactions for the current week's expenses
            List<TransactionData> weeklyExpenses = new ArrayList<>();
            for (TransactionData tx : allTransactions) {
                if ("Expense".equalsIgnoreCase(tx.getOperation())) {
                    try {
                        LocalDateTime transactionDateTime = LocalDateTime.parse(tx.getTime(), TRANSACTION_TIME_FORMATTER);
                        LocalDate transactionDate = transactionDateTime.toLocalDate();
                        if (!transactionDate.isBefore(startOfWeek) && !transactionDate.isAfter(endOfWeek)) {
                            weeklyExpenses.add(tx);
                        }
                    } catch (DateTimeParseException e) {
                        System.err.println("SpendingHoroscopeService: Skipping transaction due to date parse error: " + tx.getTime());
                    } catch (Exception e) {
                        System.err.println("SpendingHoroscopeService: Error processing transaction: " + tx + " - " + e.getMessage());
                    }
                }
            }
            System.out.println("DEBUG: Found " + weeklyExpenses.size() + " expenses this week."); // Debug

            // 4. Check if there were any expenses this week
            if (weeklyExpenses.isEmpty()) {
                ReportDetails details = reportTemplates.get(TYPE_NO_SPENDING);
                // Ensure imagePath is just the filename if uncommenting
                return new HoroscopeReport(details.title, details.descriptionTemplate, details.imagePath);
            }

            // 5. *** USE BillStatics to calculate category totals for THIS WEEK'S expenses ***
            Map<String, Double> categoryTotals = BillStatics.calculateExpenseCategoryTotals(weeklyExpenses);

            // Calculate total weekly expense from the map
            double totalWeeklyExpense = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();

             System.out.println("DEBUG: Weekly expense total: " + totalWeeklyExpense); // Debug
             System.out.println("DEBUG: Category Totals: " + categoryTotals); // Debug


            // 6. Find dominant category
             Optional<Map.Entry<String, Double>> dominantEntry = categoryTotals.entrySet().stream()
                     .filter(entry -> entry.getValue() > 0) // Only consider categories with spending
                     .max(Map.Entry.comparingByValue());

            if (dominantEntry.isPresent()) {
                Map.Entry<String, Double> dominantCategory = dominantEntry.get();
                String categoryName = dominantCategory.getKey();
                double categoryAmount = dominantCategory.getValue();
                double percentage = (totalWeeklyExpense > 0) ? (categoryAmount / totalWeeklyExpense) * 100.0 : 0.0;

                 System.out.println("DEBUG: Dominant Category: " + categoryName + " (" + String.format("%.1f", percentage) + "%)"); // Debug

                // 7. Determine Horoscope Type based on the dominant category name
                String horoscopeType = mapCategoryToHoroscopeType(categoryName); // Use a helper method

                ReportDetails details = reportTemplates.getOrDefault(horoscopeType, reportTemplates.get(TYPE_BALANCED)); // Fallback
                String description = String.format(details.descriptionTemplate, percentage); // Format description

                // Ensure imagePath is just the filename if uncommenting
                return new HoroscopeReport(details.title, description, details.imagePath);

            } else {
                 // This case means expenses exist but perhaps all are zero amount? Fallback.
                 System.out.println("DEBUG: No dominant category found, defaulting to Balanced."); // Debug
                 ReportDetails details = reportTemplates.get(TYPE_BALANCED);
                 // Ensure imagePath is just the filename if uncommenting
                 return new HoroscopeReport(details.title, details.descriptionTemplate, details.imagePath);
            }
            */
            // ================================================================
            // == END: Original Logic (Now Commented Out) ==
            // ================================================================


            // --- START: New Forced Logic ---
            System.out.println("DEBUG: SpendingHoroscopeService - Bypassing transaction analysis. Forcing Foodie report.");

            // Directly get the "Foodie Star" template details
            ReportDetails foodieDetails = reportTemplates.get(TYPE_FOODIE);
            if (foodieDetails == null) {
                // Fallback if somehow the template is missing (shouldn't happen)
                 System.err.println("ERROR: Foodie Star template details missing!");
                 return getDefaultErrorReport();
            }

            // Format the description with a placeholder percentage (e.g., 16.1%)
            // Or you could modify the template string itself to not require a percentage
            double placeholderPercentage = 16.1;
            String description = String.format(foodieDetails.descriptionTemplate, placeholderPercentage);

            // Create and return the forced "Foodie Star" report
            // The imagePath from foodieDetails should now be just "food.jpg"
            return new HoroscopeReport(foodieDetails.title, description, foodieDetails.imagePath);
            // --- END: New Forced Logic ---


        } catch (Exception e) {
            // This catch block is now less likely to be hit by the main logic,
            // but kept for safety (e.g., if template lookup fails).
            System.err.println("SpendingHoroscopeService: Error generating weekly report for user " + username + ": " + e.getMessage());
            e.printStackTrace();
            return getDefaultErrorReport();
        }
    }

    /**
     * Maps the dominant spending category name to a specific Horoscope Type.
     * (Kept for reference if original logic is uncommented)
     */
    private String mapCategoryToHoroscopeType(String categoryName) {
        if (categoryName == null) return TYPE_UNCATEGORIZED;
        String lowerCaseCategory = categoryName.toLowerCase();
        switch (lowerCaseCategory) {
            case "food": case "dining": case "restaurant": return TYPE_FOODIE;
            case "entertainment": case "movies": case "games": return TYPE_ENTERTAINMENT;
            case "shopping": case "clothes": case "electronics": return TYPE_SHOPPING;
            case "transport": case "traffic": case "gas": case "taxi": return TYPE_TRANSPORT;
            case "fitness": case "gym": case "sports": return TYPE_FITNESS;
            case "travel": case "vacation": case "flights": case "hotels": return TYPE_TRAVEL;
            case "unclassified": return TYPE_UNCATEGORIZED;
            default:
                System.out.println("DEBUG: Unmapped dominant category '" + categoryName + "', defaulting to Balanced.");
                return TYPE_BALANCED;
        }
    }

    // getDefaultErrorReport() - Kept as is, but ensure image path is filename only
    /**
     * Provides a default report in case of errors.
     * @return A default HoroscopeReport object.
     */
    public HoroscopeReport getDefaultErrorReport() {
        // Get details using the key, ensuring the path is just the filename
        ReportDetails errorDetails = reportTemplates.get("ERROR_TYPE");
        String imagePath = (errorDetails != null) ? errorDetails.imagePath : "error_monster.png"; // Fallback filename

        return new HoroscopeReport(
                "Report Unavailable",
                "Sorry, couldn't generate your spending horoscope this week. Please check back later or ensure transactions are recorded correctly.",
                imagePath // Use filename from template or fallback
        );
    }
}