package Service;

import Model.HoroscopeReportModel;
import Model.Transaction;
import Repository.TransactionRepository; // Assuming this is correctly in your Repository package

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class responsible for generating monthly "Spending Star" reports.
 * These reports are based on a user's expenses, identifying the category
 * with the highest spending among a predefined set of types for a given month and year.
 * It interacts with the {@link TransactionRepository} to fetch transaction data.
 *
 * @author Group 19
 * @version 1.2 (Updated for monthly reports, specific categories, and "failed.jpg" logic)
 */
public class HoroscopeService {
    private final TransactionRepository transactionRepository;
    private static final List<String> TARGET_SPENDING_TYPES = Arrays.asList(
            "entertainment", "fitness", "food", "shopping", "transport", "travel"
    );
    // Ensure BudgetService.DATE_FORMATTER is accessible and correct
    // e.g., public static final DateTimeFormatter DATE_FORMATTER in BudgetService
    /**
     * Formatter for parsing transaction timestamps. It is expected to be compatible
     * with the format used in {@link Service.BudgetService#DATE_FORMATTER}.
     */
    private static final DateTimeFormatter TRANSACTION_TIMESTAMP_FORMATTER = BudgetService.DATE_FORMATTER;

    /**
     * Filename for the image used when there is no data or insufficient data for a spending star.
     * Publicly accessible for use in UI components like {@link View.HoroscopePanel.HoroscopePanel}.
     */
    public static final String NO_DATA_IMAGE_FILENAME = "failed.jpg";
    /**
     * Title used for reports when there is no data or insufficient data.
     * Publicly accessible for UI components.
     */
    public static final String NO_DATA_TITLE = "More Data Needed";
    /**
     * Description used for reports when there is no data or insufficient data.
     * Publicly accessible for UI components.
     */
    public static final String NO_DATA_DESCRIPTION = "Please add more expense records for the selected month to see your Spending Star.";

    /**
     * Constructs a new HoroscopeService.
     * Initializes the {@link TransactionRepository} to fetch transaction data.
     */
    public HoroscopeService() {
        this.transactionRepository = new TransactionRepository();
    }

    /**
     * Generates a monthly horoscope (spending star) report for the specified user, month, and year.
     * It calculates the top spending category among predefined types and creates a report model.
     * If insufficient data is found, a default "no data" report is returned.
     *
     * @param username      The username of the user for whom the report is generated.
     * Cannot be null or empty.
     * @param selectedMonth An integer representing the month (1 for January, 12 for December).
     * Must be between 1 and 12.
     * @param selectedYear  The year for which the report is to be generated.
     * @return A {@link HoroscopeReportModel} containing the report details.
     * This includes a title, description, image filename, the selected month,
     * and a list of transactions for that month. Returns a "no data" model
     * if username is invalid, month is invalid, or no relevant expenses are found.
     */
    public HoroscopeReportModel generateMonthlyHoroscopeReport(String username, int selectedMonth, int selectedYear) {
        String monthNameForError = (selectedMonth >= 1 && selectedMonth <= 12) ?
                Month.of(selectedMonth).getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH) : "the selected period";

        if (username == null || username.trim().isEmpty()) {
            // Handle invalid username
            return new HoroscopeReportModel(
                    "User Error", "Username not specified. Cannot generate report for " + monthNameForError + " " + selectedYear + ".",
                    NO_DATA_IMAGE_FILENAME,
                    selectedMonth > 0 && selectedMonth <= 12 ? selectedMonth : LocalDate.now().getMonthValue(), // Use valid month or current
                    Collections.emptyList()
            );
        }
        if (selectedMonth < 1 || selectedMonth > 12) {
            // Handle invalid month
            return new HoroscopeReportModel(
                    "Input Error", "Invalid month selected. Please choose a month from January to December.",
                    NO_DATA_IMAGE_FILENAME,
                    LocalDate.now().getMonthValue(), // Default to current month if input is bad
                    Collections.emptyList()
            );
        }

        YearMonth yearMonth = YearMonth.of(selectedYear, selectedMonth);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59, 999999999); // Inclusive end

        System.out.println("HoroscopeService: Generating report for user=" + username +
                ", month=" + selectedMonth + ", year=" + selectedYear);
        System.out.println("HoroscopeService: Period: " + startOfMonth.format(TRANSACTION_TIMESTAMP_FORMATTER) +
                " to " + endOfMonth.format(TRANSACTION_TIMESTAMP_FORMATTER));

        List<Transaction> allUserTransactions = transactionRepository.findTransactionsByUsername(username);

        if (allUserTransactions == null) { // Should ideally not happen if repository returns empty list on error
            allUserTransactions = Collections.emptyList();
        }
        System.out.println("HoroscopeService: Fetched " + allUserTransactions.size() + " total transactions for user " + username);

        List<Transaction> monthlyExpenses = allUserTransactions.stream()
                .filter(tx -> "Expense".equalsIgnoreCase(tx.getOperation())) // Only consider expenses
                .filter(tx -> {
                    if (tx.getTimestamp() == null) return false;
                    try {
                        LocalDateTime txTimestamp = LocalDateTime.parse(tx.getTimestamp(), TRANSACTION_TIMESTAMP_FORMATTER);
                        // Check if transaction is within the month
                        return !txTimestamp.isBefore(startOfMonth) && !txTimestamp.isAfter(endOfMonth);
                    } catch (DateTimeParseException e) {
                        System.err.println("HoroscopeService: Error parsing transaction timestamp '" + tx.getTimestamp() + "' for " + username + ". Error: " + e.getMessage());
                        return false; // Skip transactions with unparseable timestamps
                    }
                })
                .collect(Collectors.toList());

        System.out.println("HoroscopeService: Filtered to " + monthlyExpenses.size() + " expense transactions for " + Month.of(selectedMonth).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + selectedYear + ".");

        if (monthlyExpenses.isEmpty()) {
            System.out.println("HoroscopeService: No expenses found for " + Month.of(selectedMonth) + " " + selectedYear + ". Using 'failed.jpg'.");
            return new HoroscopeReportModel(
                    NO_DATA_TITLE,
                    NO_DATA_DESCRIPTION,
                    NO_DATA_IMAGE_FILENAME,
                    selectedMonth,
                    monthlyExpenses // Pass the (empty) list
            );
        }

        Map<String, Double> expensesByTargetType = new HashMap<>();
        for (String targetType : TARGET_SPENDING_TYPES) {
            double totalForType = monthlyExpenses.stream()
                    .filter(tx -> tx.getType() != null && targetType.equalsIgnoreCase(tx.getType()))
                    .mapToDouble(Transaction::getAmount)
                    .sum();
            if (totalForType > 0) { // Only add if there's spending in this category
                expensesByTargetType.put(targetType, totalForType);
            }
        }

        if (expensesByTargetType.isEmpty()) {
            // No expenses in any of the TARGET_SPENDING_TYPES
            System.out.println("HoroscopeService: No spending in target categories for " + Month.of(selectedMonth) + " " + selectedYear + ". Using 'failed.jpg'.");
            return new HoroscopeReportModel(
                    NO_DATA_TITLE,
                    NO_DATA_DESCRIPTION,
                    NO_DATA_IMAGE_FILENAME,
                    selectedMonth,
                    monthlyExpenses // Pass all monthly expenses, even if not in target types
            );
        }

        // Find the type with the maximum spending
        Optional<Map.Entry<String, Double>> topSpendingEntry = expensesByTargetType.entrySet().stream()
                .max(Map.Entry.comparingByValue());

        // Since expensesByTargetType is not empty, topSpendingEntry should be present
        String topType = topSpendingEntry.get().getKey().toLowerCase();
        System.out.println("HoroscopeService: Top spending type for " + Month.of(selectedMonth) + " " + selectedYear + " is '" + topType + "'.");
        return getReportForTopSpendingType(topType, selectedMonth, selectedYear, monthlyExpenses);
    }

    /**
     * Creates a {@link HoroscopeReportModel} based on the identified top spending type.
     * This method provides specific titles, descriptions, and image filenames for each spending category.
     *
     * @param topType             The category with the highest spending (e.g., "food", "transport").
     * Expected to be one of the {@code TARGET_SPENDING_TYPES}.
     * @param selectedMonth       The month (1-12) for the report.
     * @param selectedYear        The year for the report.
     * @param monthlyTransactions The list of all transactions for the selected month and year.
     * @return A {@link HoroscopeReportModel} tailored to the top spending type.
     * If the {@code topType} is not recognized (which indicates a logic error),
     * a default "no data" report is returned.
     */
    private HoroscopeReportModel getReportForTopSpendingType(String topType, int selectedMonth, int selectedYear, List<Transaction> monthlyTransactions) {
        String monthName = Month.of(selectedMonth).getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH);
        String title, description, imageName;

        switch (topType) {
            case "food":
                title = "Your " + monthName + " " + selectedYear + ": The Foodie Star!";
                description = "You truly savored the flavors this " + monthName + "! Delicious meals and culinary experiences were your highlight. Keep enjoying, and perhaps plan your next food adventure!";
                imageName = "food.jpg";
                break;
            case "transport":
                title = "Your " + monthName + " " + selectedYear + ": The Commuter Star!";
                description = "Always on the move! Your transport expenses indicate a busy " + monthName + ". Whether it's daily commutes or necessary trips, you're keeping things rolling.";
                imageName = "transport.jpg";
                break;
            case "shopping":
                title = "Your " + monthName + " " + selectedYear + ": The Shopping Star!";
                description = "Retail adventures called your name this " + monthName + "! From essentials to treats, your shopping game was strong. Hope you found some great deals!";
                imageName = "shopping.jpg";
                break;
            case "entertainment":
                title = "Your " + monthName + " " + selectedYear + ": The Entertainment Star!";
                description = "Fun and leisure were a priority! Movies, concerts, or hobbies – you made sure " + monthName + " was an enjoyable one. What's next on the fun-list?";
                imageName = "entertainment.jpg";
                break;
            case "fitness":
                title = "Your " + monthName + " " + selectedYear + ": The Fitness Star!";
                description = "Dedicated to your well-being! Your fitness spending shows commitment. Keep up the great work towards your health goals this " + monthName + " and beyond!";
                imageName = "fitness.jpg";
                break;
            case "travel":
                title = "Your " + monthName + " " + selectedYear + ": The Explorer Star!";
                description = "Wanderlust took over! Your travel expenses suggest exciting journeys or well-deserved getaways. " + monthName + " was a month of discovery!";
                imageName = "travel.jpg";
                break;
            default:
                // This case implies a bug if TARGET_SPENDING_TYPES was the source of topType
                System.err.println("HoroscopeService: Logic error - Unhandled topType '" + topType + "' that was derived from TARGET_SPENDING_TYPES. Using 'failed.jpg'.");
                return new HoroscopeReportModel(
                        NO_DATA_TITLE,
                        NO_DATA_DESCRIPTION,
                        NO_DATA_IMAGE_FILENAME,
                        selectedMonth,
                        monthlyTransactions
                );
        }
        return new HoroscopeReportModel(title, description, imageName, selectedMonth, monthlyTransactions);
    }
}