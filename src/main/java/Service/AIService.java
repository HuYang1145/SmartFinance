/**
 * Provides AI-driven services for processing user inputs, recognizing intents, and managing transactions.
 * This service integrates with external AI models and transaction systems to handle user requests such as
 * recording expenses/income, querying balances, and generating suggestions.
 *
 * @author Group 19
 * @version 1.0
 */
package Service;

import Model.PredictResult;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import Controller.TransactionController;
import Model.EntityResultModel;
import com.google.gson.Gson;
import java.io.IOException;

import Model.AIResponse;
import Model.IntentResultModel;
import Model.User;
import Model.SessionContext;
import Repository.UserRepository;
import utils.TransactionUtils;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AIService {
    private static final Gson GSON = new Gson();
    private final TransactionService transactionService;
    private final DeepSeekService deepSeekService;
    private final UserRepository userRepository;
    private final TransactionController transactionController;
    private final TransactionUtils txUtils;
    private final String baseDir;
    private final ConcurrentHashMap<String, SessionContext> sessions = new ConcurrentHashMap<>();

    /**
     * Constructs an AIService instance with dependencies for transaction processing and AI services.
     *
     * @param transactionService    the service for handling transaction-related operations
     * @param transactionController the controller for managing transaction data
     * @param deepSeekService       the service for interacting with the DeepSeek AI model
     * @param userRepository        the repository for user data access
     * @param rateService           the service for exchange rate conversions
     */
   public AIService(
            TransactionService transactionService,
            TransactionController transactionController,
            DeepSeekService deepSeekService,
            UserRepository userRepository,
            ExchangeRateService rateService
    ) {
        this.transactionService = transactionService;
        this.transactionController = transactionController;
        this.deepSeekService = deepSeekService;
        this.userRepository = userRepository;
        this.txUtils = new TransactionUtils(rateService);
        this.baseDir = System.getProperty("user.dir");
    }

    /**
     * Processes user input to predict and respond with appropriate actions, such as recording transactions
     * or querying balances. Manages session context for multi-turn conversations.
     *
     * @param userInput the user's input text
     * @return an AIResponse containing the response message and optional error
     * @throws Exception if an error occurs during intent recognition or transaction processing
     */
   public AIResponse predictReply(String userInput) {
        User user = userRepository.getCurrentUser();
        if (user == null) {
            return new AIResponse(null, "Please log in first.");
        }
        String userKey = user.getUsername();
        SessionContext ctx = sessions.get(userKey);

        try {
            if (ctx != null) {
                PredictResult exitPr = callPredict(userInput);
                if (!ctx.getMissingSlots().isEmpty()) {
                    PredictResult pr = callPredict(userInput);
                    String slot = ctx.getMissingSlots().get(0);
                    String val = pr.getEntities().get(slot);
                    if ("amount".equals(slot) && (val == null || val.isBlank())) {
                        Pattern p = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)(?:\\s*)([a-zA-Z$¥]*)");
                        Matcher m = p.matcher(userInput.trim());
                        if (m.matches()) {
                            val = userInput;
                        }
                    }
                    if (("time".equals(slot) || "date".equals(slot)) && (val == null || val.isBlank())) {
                        if (userInput.matches(".*(today|yesterday|tomorrow|\\b\\d{1,2}/\\d{1,2}\\b|\\b\\d{4}-\\d{1,2}-\\d{1,2}\\b|\\b(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec)[a-z]*\\.?\\s+\\d{1,2}(,\\s*\\d{4})?\\b).*")) {
                            val = userInput;
                        }
                    }

                    if ("merchant".equals(slot)|| "category".equals(slot)) {
                        val = userInput;
                    }
                    if (val != null && !val.isBlank()) {
                        if ("amount".equals(slot)) {
                            try {
                                double amt = txUtils.normalizeAmount(val);
                                ctx.getSlots().put("amount", String.format("%.2f", amt));
                            } catch (Exception e) {
                                return new AIResponse("Sorry, I couldn't understand the amount. Please enter a number like 3000 or 3000 CNY.", null);
                            }

                        } else if ("time".equals(slot)) {
                            ctx.getSlots().put("time", TransactionUtils.normalizeTime(val));
                        } else {
                            ctx.getSlots().put(slot, val);
                        }
                        List<String> temp = new ArrayList<>(ctx.getMissingSlots());
                        temp.remove(0);
                        ctx.setMissingSlots(temp);
                    } else {
                        return new AIResponse("AI didn't recognize " + slot + ", please tell me again.", null);
                    }
                    if (!ctx.getMissingSlots().isEmpty()) {
                        return new AIResponse("Please tell me " + ctx.getMissingSlots().get(0) + ".", null);
                    }
                    ctx.setConfirmed(false);
                    String preview = ctx.getSlots().entrySet().stream()
                            .map(e -> e.getKey() + ": " + e.getValue())
                            .collect(Collectors.joining("\n"));
                    return new AIResponse(
                            "Please confirm the transaction:\n" + preview + "\nReply 'yes' to confirm, 'no' to cancel"
                                    + "\nor tell me what to change like \"change category to **\"",
                            null
                    );
                }
                if (!ctx.isConfirmed()) {
                    String input = userInput.trim().toLowerCase();
                    if ("yes".equals(input) || "confirm".equals(input)) {
                        ctx.setConfirmed(true);
                    } else if ("no".equals(input) || "cancel".equals(input)) {
                        sessions.remove(userKey);
                        return new AIResponse("Transaction canceled.", null);
                    } else {
                        tryModifySlot(ctx, userInput);
                        String preview = ctx.getSlots().entrySet().stream()
                                .map(e -> e.getKey() + ": " + e.getValue())
                                .collect(Collectors.joining("\n"));
                        return new AIResponse(
                                "Updated transaction:\n" + preview + "\nReply 'yes' to confirm, 'no' to cancel"
                                        + "\nor tell me what to change like \"change category to **\".",
                                null
                        );
                    }
                }

                ctx.getSlots().put("operation",
                        ctx.getIntent().equals("RecordExpense") ? "Expense" : "Income");
                transactionController.addTransactionFromEntities(user, ctx.getSlots());
                sessions.remove(userKey);
                return new AIResponse(
                        (ctx.getIntent().equals("RecordExpense") ? "Recorded expense: ¥" : "Recorded income: ¥") +
                                ctx.getSlots().get("amount"),
                        null
                );
            }

            PredictResult pr = callPredict(userInput);
            String intent = pr.getIntent();
            Map<String, String> slots = pr.getEntities();

            switch (intent) {
                case "RecordExpense":
                case "RecordIncome": {
                    if (slots.containsKey("amount")) {
                        double amt = txUtils.normalizeAmount(slots.get("amount"));
                        slots.put("amount", String.format("%.2f", amt));
                    }
                    if (slots.containsKey("time")) {
                        slots.put("time", TransactionUtils.normalizeTime(slots.get("time")));
                    }
                    if(slots.containsKey("category")){
                        slots.put("category", slots.get("category"));
                    }
                    List<String> required = List.of("amount", "time", "category", "merchant");
                    List<String> missing = required.stream()
                            .filter(k -> !slots.containsKey(k) || slots.get(k).isBlank())
                            .toList();
                    if (missing.isEmpty()) {
                        slots.put("operation", intent.equals("RecordExpense") ? "Expense" : "Income");
                        transactionController.addTransactionFromEntities(user, slots);
                        return new AIResponse(
                                (intent.equals("RecordExpense") ? "Recorded expense: ¥" : "Recorded income: ¥") +
                                        slots.get("amount"),
                                null
                        );
                    }
                    SessionContext newCtx = new SessionContext();
                    newCtx.setIntent(intent);
                    newCtx.setSlots(slots);
                    newCtx.setMissingSlots(missing);
                    sessions.put(userKey, newCtx);
                    return new AIResponse("Please tell me " + missing.get(0) + ".", null);
                }
                case "QueryBalance": {
                    double bal = transactionService.getBalance(user);
                    return new AIResponse("Your balance is: ¥" + String.format("%.2f", bal), null);
                }
                case "QuerySuggestion": {
                    String summary = transactionService.buildTransactionSummary(user);
                    if (summary.isEmpty()) {
                        return new AIResponse(null, "No transactions loaded.");
                    }
                    String suggestions = deepSeekService.callDeepSeekApi(
                            summary,
                            "Analyze these transactions and give 2-3 brief, actionable suggestions."
                    );
                    return new AIResponse("Suggestions:\n" + suggestions, null);
                }
                case "QuerySpendTime": {
                    double spent = transactionService.getCurrentMonthExpense(user);
                    return new AIResponse("You spent ¥" + String.format("%.2f", spent) + " this month.", null);
                }
                case "Greeting":{
                    return new AIResponse("HI! I can tell you your balance, monthly spending, or record expense/income and give you some suggestion.", null);
                }
                case "Thanking":{
                    return new AIResponse("My honor.",null);
                }
                case "Farewell":{
                    return new AIResponse("Bye,have a nice day!",null);
                }
                default:
                    return new AIResponse(
                            "I can tell you your balance, monthly spending, or record expense/income.",
                            null
                    );
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new AIResponse(null, "Sorry, an unexpected error occurred.");
        }
    }

    /**
     * Recognizes the intent and entity of the user input by invoking an external prediction executable.
     * @param input The user's natural language input, e.g. "I spent 30 yuan today".
     * @return {@link PredictResult} object containing the intent type, confidence level, and entity extracted.
     * @throws IOException If startup of predict.exe fails or an I/O exception occurs while reading output.
     * @throws InterruptedException If the process is interrupted.
     * @throws RuntimeException If predict.exe returns a non-zero status code or does not produce valid JSON output.
     */
   private PredictResult callPredict(String input) throws Exception {
        String exePath = baseDir + File.separator + "dist" + File.separator +"predict"+File.separator+ "predict.exe";
        ProcessBuilder pb = new ProcessBuilder(
                exePath,
                "-t", input
        );

        pb.directory(new File(baseDir + File.separator + "dist"));
        pb.redirectErrorStream(true); 

        Process proc = pb.start();

        StringBuilder fullOutput = new StringBuilder();
        String jsonLine = null;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fullOutput.append(line).append(System.lineSeparator());
                line = line.trim();
                if (line.startsWith("{") && line.endsWith("}")) {
                    jsonLine = line;
                }
            }
        }

        int exitCode = proc.waitFor();

        if (exitCode != 0 || jsonLine == null) {
            System.err.println("⚠️ [predict.exe ]：");
            System.err.println(fullOutput.toString());
            throw new RuntimeException("predict.exe failed，exit=" + exitCode);
        }
        return GSON.fromJson(jsonLine, PredictResult.class);
    }

    /**
     * Attempts to modify a slot in the session context based on user input.
     *
     * @param ctx       the session context containing slot data
     * @param userInput the user's input specifying the slot to modify
     */
    private void tryModifySlot(SessionContext ctx, String userInput) {
        String input = userInput.trim().toLowerCase();
        Map<String, String> slots = ctx.getSlots();
        Matcher m;

        // 1.change/set amount to <number>
        m = Pattern.compile("^(?:change|set) amount to ([0-9]+(?:\\.[0-9]+)?)$").matcher(input);
        if (m.find()) {
            double amt = txUtils.normalizeAmount(m.group(1));
            slots.put("amount", String.format("%.2f", amt));
            return;
        }

        // 2. change/set time to <text>
        m = Pattern.compile("^(?:change|set) (?:time|date) to (.+)$").matcher(input);
        if (m.find()) {
            String rawTime = m.group(1).trim();
            slots.put("time", TransactionUtils.normalizeTime(rawTime));
            return;
        }

        // 3. change/set category to <text>
        m = Pattern.compile("^(?:change|set) category to (.+)$").matcher(input);
        if (m.find()) {
            slots.put("category", m.group(1).trim());
            return;
        }

        // 4.change/set merchant to <text>
        m = Pattern.compile("^(?:change|set) merchant to (.+)$").matcher(input);
        if (m.find()) {
            slots.put("merchant", m.group(1).trim());
        }

        //5.operation
        m = Pattern.compile("^(?:change|set) operation to (income|expense)$").matcher(input);
        if (m.find()) {
            String op = m.group(1).trim().equalsIgnoreCase("income") ? "Income" : "Expense";
            slots.put("operation", op);
        }
    }
}