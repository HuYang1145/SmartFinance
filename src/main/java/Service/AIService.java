package Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;

import Model.AIResponse;
import Model.IntentResultModel;
import Model.User;
import Repository.UserRepository;

public class AIService {
    private static final Gson GSON = new Gson();
    private final TransactionService transactionService;
    private final DeepSeekService deepSeekService;
    private final UserRepository userRepository;
    private final String predictExePath;

    public AIService(TransactionService transactionService, DeepSeekService deepSeekService, UserRepository userRepository) {
        this.transactionService = transactionService;
        this.deepSeekService = deepSeekService;
        this.userRepository = userRepository;
        this.predictExePath = System.getProperty("user.dir") + File.separator + "dist" + File.separator + "predict" + File.separator + "predict.exe";
    }

    public AIResponse predictReply(String userInput) {
        if (userInput == null || userInput.trim().isEmpty()) {
            return new AIResponse(null, "Please say something first.");
        }

        try {
            IntentResultModel intentResult = recognizeIntent(userInput);
            String intent = intentResult.getIntent() != null ? intentResult.getIntent() : "Unknown";
            User user = userRepository.getCurrentUser();
            if (user == null) {
                return new AIResponse(null, "Please log in first.");
            }

            switch (intent) {
                case "QueryBalance":
                    double balance = transactionService.getBalance(user);
                    return new AIResponse("Your balance is: ¥" + String.format("%.2f", balance), null);
                case "QuerySuggestion":
                    String summary = transactionService.buildTransactionSummary(user);
                    if (summary.isEmpty()) {
                        return new AIResponse(null, "No transactions loaded.");
                    }
                    String suggestions = deepSeekService.callDeepSeekApi(summary, "Analyze these transactions and give 2-3 brief, actionable suggestions.");
                    return new AIResponse("Suggestions:\n" + suggestions, null);
                case "QuerySpendTime":
                    double spent = transactionService.getCurrentMonthExpense(user);
                    return new AIResponse("You spent ¥" + String.format("%.2f", spent) + " this month.", null);
                default:
                    return new AIResponse("I can tell you your balance, spending this month, or give suggestions.", null);
            }
        } catch (Exception e) {
            System.err.println("AI error: " + e.getMessage());
            return new AIResponse(null, "Sorry, an unexpected error occurred.");
        }
    }

    private IntentResultModel recognizeIntent(String input) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(predictExePath, input);
        pb.redirectErrorStream(true);
        Process proc = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        }
        int exitCode = proc.waitFor();
        if (exitCode != 0 || output.length() == 0) {
            throw new RuntimeException("predict.exe failed with exit code: " + exitCode);
        }

        try {
            return GSON.fromJson(output.toString(), IntentResultModel.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse predict.exe output: " + output);
        }
    }
}