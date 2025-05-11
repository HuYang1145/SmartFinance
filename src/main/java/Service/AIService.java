package Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import Controller.TransactionController;
import Model.EntityResultModel;
import com.google.gson.Gson;

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
    private final String predictExePath;
    private final Map<String, SessionContext> sessions = new ConcurrentHashMap<>();
    private final TransactionController transactionController;
    private final TransactionUtils txUtils;

    public AIService(TransactionService transactionService,TransactionController transactionController, DeepSeekService deepSeekService, UserRepository userRepository,ExchangeRateService rateService) {
        this.transactionService = transactionService;
        this.transactionController = transactionController;
        this.deepSeekService = deepSeekService;
        this.userRepository = userRepository;
        this.txUtils = new TransactionUtils(rateService);
        this.predictExePath = System.getProperty("user.dir") + File.separator + "dist" + File.separator + "predict" + File.separator + "predict.exe";
    }

    public AIResponse predictReply(String userInput) {
        User user = userRepository.getCurrentUser();
        if (user == null) {
            return new AIResponse(null, "Please log in first.");
        }
        String userKey = user.getUsername();            // 或者 sessionId
        SessionContext ctx = sessions.get(userKey);      // 看看有没有正在补全的会话

        try {
            // —— 如果有未完成的对话，就继续补全槽位 ——
            if (ctx != null) {
                // —— 如果正在确认 ——
                if (!ctx.getMissingSlots().isEmpty()) {
                    // 正在补全槽位
                    EntityResultModel er = recognizeEntitiesWithPy(userInput);
                    String slot = ctx.getMissingSlots().get(0);
                    String val ;
                    if (slot.equals("merchant")) {
                        val = userInput;  // 仅 merchant 使用原始文本
                    }else if (slot.equals("type")) {
                        val = er.getCategory();
                        if (val == null || val.isBlank()) {
                            val = userInput.trim();  // fallback
                        }
                    }else {
                        val = switch (slot) {
                            case "amount" -> er.getAmount();
                            case "timestamp" -> er.getTime();
                            default -> null;
                        };
                    }
                    if (val != null && !val.isBlank()) {
                        if (slot.equals("amount")) {
                            double amt = txUtils.normalizeAmount(val);
                            ctx.getSlots().put("amount", String.format("%.2f", amt));
                        } else if (slot.equals("timestamp")) {
                            ctx.getSlots().put("timestamp", TransactionUtils.normalizeTime(val));
                        } else {
                            ctx.getSlots().put(slot, val);
                        }
                        ctx.getMissingSlots().remove(0);
                    } else {
                        return new AIResponse("AI didn't recognize " + slot + ", please tell me again.",null );
                    }

                    if (!ctx.getMissingSlots().isEmpty()) {
                        return new AIResponse("Please tell me " + ctx.getMissingSlots().get(0) + ".",null);
                    }

                    // 槽位已补全，进入确认流程
                    ctx.setConfirmed(false);
                    String preview = ctx.getSlots().entrySet().stream()
                            .map(e -> e.getKey() + ": " + e.getValue())
                            .collect(Collectors.joining("\n"));
                    return new AIResponse("Please confirm the transaction:\n" + preview + "\nReply 'yes' to confirm, or tell me what to change in the format 'change ** to **.", null);
                }

                // —— 等待用户确认 ——
                if (!ctx.isConfirmed()) {
                    if (userInput.trim().equalsIgnoreCase("yes") || userInput.trim().equalsIgnoreCase("confirm")) {
                        ctx.setConfirmed(true);
                    } else {
                        tryModifySlot(ctx, userInput);
                        String preview = ctx.getSlots().entrySet().stream()
                                .map(e -> e.getKey() + ": " + e.getValue())
                                .collect(Collectors.joining("\n"));
                        return new AIResponse("Updated transaction:\n" + preview + "\nReply 'yes' to confirm, or tell me what to change.", null);
                    }
                }

                // —— 用户确认后，写入交易记录 ——
                ctx.getSlots().put("operation", ctx.getIntent().equals("RecordExpense") ? "Expense" : "Income");
                transactionController.addTransactionFromEntities(user, ctx.getSlots());
                sessions.remove(userKey);
                return new AIResponse(
                        (ctx.getIntent().equals("RecordExpense") ? "Recorded expense: ¥" : "Recorded income: ¥") + ctx.getSlots().get("amount"),
                        null
                );
            }


            // —— 初次识别意图 ——
            IntentResultModel intentResult = recognizeIntent(userInput);
            String intent = intentResult.getIntent();

            if ("RecordExpense".equals(intent) || "RecordIncome".equals(intent)) {
                EntityResultModel er = recognizeEntitiesWithPy(userInput);
                Map<String, String> slots = new HashMap<>();

                if (er.getAmount() != null) {
                    double amt = txUtils.normalizeAmount(er.getAmount());
                    slots.put("amount", String.format("%.2f", amt));
                }
                if (er.getTime() != null) {
                    slots.put("timestamp", TransactionUtils.normalizeTime(er.getTime()));
                }
                if (er.getCategory() != null) {
                    slots.put("type", er.getCategory());
                }

                List<String> required = List.of("amount", "timestamp", "type", "merchant");
                List<String> missing = required.stream()
                        .filter(k -> slots.get(k) == null || slots.get(k).isBlank())
                        .collect(Collectors.toList());

                if (missing.isEmpty()) {
                    slots.put("operation", intent.equals("RecordExpense") ? "Expense" : "Income");
                    transactionController.addTransactionFromEntities(user, slots);
                    return new AIResponse(
                            (intent.equals("RecordExpense") ? "Recorded expense: ¥" : "Recorded income: ¥") + slots.get("amount"),
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
            if("QueryBalance".equals(intent)) {
                double balance = transactionService.getBalance(user);
                return new AIResponse("Your balance is: ¥" + String.format("%.2f", balance), null);
            }
            if("QuerySuggestion".equals(intent)) {
                String summary = transactionService.buildTransactionSummary(user);
                if (summary.isEmpty()) {
                    return new AIResponse(null, "No transactions loaded.");
                }
                String suggestions = deepSeekService.callDeepSeekApi(summary, "Analyze these transactions and give 2-3 brief, actionable suggestions.");
                return new AIResponse("Suggestions:\n" + suggestions, null);
            }
            if("QuerySpendTime".equals(intent)) {
                double spent = transactionService.getCurrentMonthExpense(user);
                return new AIResponse("You spent ¥" + String.format("%.2f", spent) + " this month.", null);
            }
            return new AIResponse(
                "I can tell you your balance, monthly spending, or record expense/income.",
                null
        );

        } catch (Exception e) {
            e.printStackTrace();
            return new AIResponse(null, "Sorry, an unexpected error occurred.");
        }
    }


    private IntentResultModel recognizeIntent(String input) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(predictExePath, input);
        pb.redirectErrorStream(true);
        Process proc = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("{") && line.trim().endsWith("}")) {
                    output.setLength(0);  // 清除前面的 debug 输出
                    output.append(line.trim()); // 只保留 JSON
                }
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
    private EntityResultModel recognizeEntitiesWithPy(String input) throws Exception {
        String baseDir = System.getProperty("user.dir");  // 项目根路径

        String python = "D:/python/envs/nlp/python.exe";
        String script = baseDir + "/ner/predict_ner.py";
        String modelDir = baseDir + "/ner/ner_model";

        // 构造命令：python predict.py --model_dir ner_model --text "..."
        ProcessBuilder pb = new ProcessBuilder(
                python,
                script,
                "--model_dir", modelDir,
                "--text", input
        );
        pb.redirectErrorStream(true);
        Process proc = pb.start();

        // 读取输出 JSON
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        }

        int exitCode = proc.waitFor();
        if (exitCode != 0 || output.length() == 0) {
            throw new RuntimeException("python predict.py 返回错误，exit=" + exitCode + " out=" + output);
        }

        // 使用 GSON 解析成你的实体模型
        return GSON.fromJson(output.toString(), EntityResultModel.class);
    }
    private void tryModifySlot(SessionContext ctx, String userInput) {
        String input = userInput.trim().toLowerCase();
        Map<String, String> slots = ctx.getSlots();
        Matcher m;

        // 1. 修改金额：change/set amount to <number>
        m = Pattern.compile("^(?:change|set) amount to ([0-9]+(?:\\.[0-9]+)?)$").matcher(input);
        if (m.find()) {
            double amt = txUtils.normalizeAmount(m.group(1));
            slots.put("amount", String.format("%.2f", amt));
            return;
        }

        // 2. 修改时间：change/set time to <text>
        m = Pattern.compile("^(?:change|set) (?:time|date) to (.+)$").matcher(input);
        if (m.find()) {
            String rawTime = m.group(1).trim();
            slots.put("timestamp", TransactionUtils.normalizeTime(rawTime));
            return;
        }

        // 3. 修改类别：change/set type to <text>
        m = Pattern.compile("^(?:change|set) type to (.+)$").matcher(input);
        if (m.find()) {
            slots.put("type", m.group(1).trim());
            return;
        }

        // 4. 修改商户：change/set merchant to <text>
        m = Pattern.compile("^(?:change|set) merchant to (.+)$").matcher(input);
        if (m.find()) {
            slots.put("merchant", m.group(1).trim());
        }

        //5.修改operation
        m = Pattern.compile("^(?:change|set) operation to (income|expense)$").matcher(input);
        if (m.find()) {
            String op = m.group(1).trim().equalsIgnoreCase("income") ? "Income" : "Expense";
            slots.put("operation", op);
        }
    }
}