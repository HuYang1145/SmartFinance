package Service;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import Model.HoroscopeReportModel;
import Model.Transaction;
import Repository.TransactionRepository;

public class HoroscopeService {
    private final TransactionRepository transactionRepository;

    public HoroscopeService() {
        this.transactionRepository = new TransactionRepository();
    }

    public HoroscopeReportModel generateWeeklyReport(String username) {
        // Get the start time of this week (Monday 00:00)
        LocalDateTime startOfWeek = LocalDateTime.now()
                .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        // Get expenses for this week
        List<Transaction> expenses = transactionRepository.findWeeklyExpenses(username, startOfWeek);

        // Summarize expenses by type
        Map<String, Double> typeSums = expenses.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getType() != null && !t.getType().isEmpty() ? t.getType() : "Unknown",
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        // Find the type with the highest expense
        String topType = typeSums.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");

        // Map to horoscope
        return mapToHoroscope(topType);
    }

    public HoroscopeReportModel getDefaultErrorReport() {
        return new HoroscopeReportModel(
                "This week you are the Mysterious Constellation!",
                "Transaction records are few. Try recording your daily expenses to discover your constellation!",
                "icons/default_star.png"
        );
    }

    private HoroscopeReportModel mapToHoroscope(String type) {
        switch (type.toLowerCase()) {
            case "electronics":
                return new HoroscopeReportModel(
                        "This week you are the Tech Constellation!",
                        "Your passion for tech products lights up the starry sky! Consider prioritizing essential purchases and controlling impulsive spending.",
                        "icons/electronics_star.png"
                );
            case "food":
                return new HoroscopeReportModel(
                        "This week you are the Foodie Constellation!",
                        "Your taste buds are shining in the starry sky! Try planning a dining budget to avoid overspending.",
                        "icons/foodie_star.png"
                );
            case "shopping":
                return new HoroscopeReportModel(
                        "This week you are the Shopping Constellation!",
                        "Shopping enthusiasm is high! Make a list of essentials, rational spending saves you more worry.",
                        "icons/shopper_star.png"
                );
            case "rent":
                return new HoroscopeReportModel(
                        "This week you are the Homebody Constellation!",
                        "Rent is the foundation of life! Ensure fixed expenses are paid first and plan other spending.",
                        "icons/rent_star.png"
                );
            case "transport":
                return new HoroscopeReportModel(
                        "This week you are the Journey Constellation!",
                        "You are full of energy on the road! Try optimizing your mode of transport to save money.",
                        "icons/transport_star.png"
                );
            case "travel":
                return new HoroscopeReportModel(
                        "This week you are the Adventure Constellation!",
                        "Travel makes life more exciting! Plan your itinerary in advance to avoid unexpected overspending.",
                        "icons/travel_star.png"
                );
            case "entertainment":
                return new HoroscopeReportModel(
                        "This week you are the Entertainment Constellation!",
                        "Enjoy your entertainment time! Set an entertainment budget and balance life with expenses.",
                        "icons/entertainment_star.png"
                );
            case "fitness":
                return new HoroscopeReportModel(
                        "This week you are the Fitness Constellation!",
                        "Fitness makes you more energetic! Stick to a reasonable budget and invest in a healthy lifestyle.",
                        "icons/fitness_star.png"
                );
            case "gift":
                return new HoroscopeReportModel(
                        "This week you are the Generous Constellation!",
                        "Giving gifts conveys warmth! Plan a gift budget and express your feelings with less worry.",
                        "icons/gift_star.png"
                );
            case "health":
                return new HoroscopeReportModel(
                        "This week you are the Medical Constellation!",
                        "Health comes first! Prioritize medical expenses and plan other spending reasonably.",
                        "icons/health_star.png"
                );
            case "utilities":
                return new HoroscopeReportModel(
                        "This week you are the Practical Constellation!",
                        "Utilities support life! Ensure bills are paid on time and optimize daily expenses.",
                        "icons/utilities_star.png"
                );
            default:
                return getDefaultErrorReport();
        }
    }
}