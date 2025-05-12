package Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import Controller.TransactionController;
import Model.AIResponse;
import Model.PredictResult;
import Model.SessionContext;
import Model.User;
import Repository.UserRepository;
import utils.TransactionUtils;
import Service.DeepSeekService;
import Service.ExchangeRateService;
import Service.TransactionService;

/**
 * 普通 Java 类版本的 AIService，无需 Spring Boot 框架
 */
public class AIService {
    private static final Gson GSON = new Gson();
    private final TransactionService transactionService;
    private final DeepSeekService deepSeekService;
    private final UserRepository userRepository;
    private final TransactionController transactionController;
    private final TransactionUtils txUtils;
    private final String baseDir;
    private final ConcurrentHashMap<String, SessionContext> sessions = new ConcurrentHashMap<>();
    private static final String OUTPUT_NAME = "predict";



    /**
     * 构造器中手动传入所需依赖，无需依赖注入框架
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
     * 主业务入口：识别意图并执行相应操作
     */
    public AIResponse predictReply(String userInput) {
        User user = userRepository.getCurrentUser();
        if (user == null) {
            return new AIResponse(null, "Please log in first.");
        }
        String userKey = user.getUsername();
        SessionContext ctx = sessions.get(userKey);

        try {
            // 继续未完成的对话
            if (ctx != null) {
                PredictResult exitPr = callPredict(userInput);
                if (!ctx.getMissingSlots().isEmpty()) {
                    PredictResult pr = callPredict(userInput);
                    String slot = ctx.getMissingSlots().get(0);
                    String val = pr.getEntities().get(slot);
                    if ("merchant".equals(slot)|| "category".equals(slot)) {
                        val = userInput;
                    }
                    if (val != null && !val.isBlank()) {
                        if ("amount".equals(slot)) {
                            double amt = txUtils.normalizeAmount(val);
                            ctx.getSlots().put("amount", String.format("%.2f", amt));
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
                            "Please confirm the transaction:\n" + preview + "\nReply 'yes' to confirm, or tell me what to change.",
                            null
                    );
                }
                if (!ctx.isConfirmed()) {
                    if ("yes".equalsIgnoreCase(userInput.trim()) || "confirm".equalsIgnoreCase(userInput.trim())) {
                        ctx.setConfirmed(true);
                    } else {
                        tryModifySlot(ctx, userInput);
                        String preview = ctx.getSlots().entrySet().stream()
                                .map(e -> e.getKey() + ": " + e.getValue())
                                .collect(Collectors.joining("\n"));
                        return new AIResponse(
                                "Updated transaction:\n" + preview + "\nReply 'yes' to confirm, or tell me what to change.",
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

            // 初次处理
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
                    return new AIResponse("HI!  can tell you your balance, monthly spending, or record expense/income.", null);
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
     * 统一调用 Python 脚本，返回合并后的意图+实体
     */
    private PredictResult callPredict(String input) throws Exception {
        // 可执行文件路径（你可以根据打包结构调整）
        String exePath = baseDir + File.separator + "dist" + File.separator +"predict"+File.separator+ "predict.exe";

        // 构造进程调用命令，只传文本参数
        ProcessBuilder pb = new ProcessBuilder(
                exePath,
                "-t", input
        );

        // 设置工作目录为 exe 所在目录（防止部分依赖找不到）
        pb.directory(new File(baseDir + File.separator + "dist"));
        pb.redirectErrorStream(true); // stderr 合并进 stdout

        Process proc = pb.start();

        // 捕获输出
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
            System.err.println("⚠️ [predict.exe 输出内容]：");
            System.err.println(fullOutput.toString());
            throw new RuntimeException("predict.exe 调用失败，exit=" + exitCode);
        }

        // 反序列化为 Java 对象
        return GSON.fromJson(jsonLine, PredictResult.class);
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
            slots.put("time", TransactionUtils.normalizeTime(rawTime));
            return;
        }

        // 3. 修改类别：change/set category to <text>
        m = Pattern.compile("^(?:change|set) category to (.+)$").matcher(input);
        if (m.find()) {
            slots.put("category", m.group(1).trim());
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