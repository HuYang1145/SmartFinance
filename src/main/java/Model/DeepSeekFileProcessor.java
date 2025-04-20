package Model;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

// 用于构建发送给API的请求体
class ChatRequest {
    private String model;
    private List<Message> messages;
    // 可以添加其他参数，如 temperature, max_tokens 等

    public ChatRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }

    // Gson 需要 getter 方法来序列化对象
    public String getModel() { return model; }
    public List<Message> getMessages() { return messages; }
}

// 用于表示消息对象 (request 和 response 都用得到)
class Message {
    private String role;
    private String content;

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    // Gson 需要 getter 方法来序列化/反序列化对象
    public String getRole() { return role; }
    public String getContent() { return content; }

    // 也需要 setter 给 Gson 反序列化用
    public void setRole(String role) { this.role = role; }
    public void setContent(String content) { this.content = content; }
}

// 用于解析API返回的响应体
class ChatResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;
    // 可以添加 usage 对象等其他字段

    // Gson 需要 getter 方法来反序列化对象
    public String getId() { return id; }
    public String getObject() { return object; }
    public long getCreated() { return created; }
    public String getModel() { return model; }
    public List<Choice> getChoices() { return choices; }

    // 也需要 setter 给 Gson 反序列化用
    public void setId(String id) { this.id = id; }
    public void setObject(String object) { this.object = object; }
    public void setCreated(long created) { this.created = created; }
    public void setModel(String model) { this.model = model; }
    public void setChoices(List<Choice> choices) { this.choices = choices; }
}

// 用于表示响应中的一个选项
class Choice {
    private int index;
    private Message message; // 嵌套的消息对象
    @SerializedName("finish_reason") // 如果字段名与JSON键不同，使用此注解
    private String finishReason;

    // Gson 需要 getter 方法来反序列化对象
    public int getIndex() { return index; }
    public Message getMessage() { return message; }
    public String getFinishReason() { return finishReason; }

    // 也需要 setter 给 Gson 反序列化用
    public void setIndex(int index) { this.index = index; }
    public void setMessage(Message message) { this.message = message; }
    public void setFinishReason(String finishReason) { this.finishReason = finishReason; }
}


public class DeepSeekFileProcessor {

    // >>> 请替换为你的 DeepSeek API Key <<<
    private static final String DEEPSEEK_API_KEY = "sk-7ae19476ebd14de3b1a93c594c267886";
    // DeepSeek API 聊天完成的 Endpoint
    // 请根据DeepSeek官方文档确认具体的Endpoint，此为示例
    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/chat/completions";

    // DeepSeek 模型名称，请根据需要选择
    private static final String DEEPSEEK_MODEL = "deepseek-reasoner"; // 或 "deepseek-coder" 等

    // >>> 请替换为你想要读取的文件路径 <<<
    private static final String FILE_PATH = "D:/桌面/java/coursework_main/coursework_main/coursework/src/main/java/src/Model/transactions.csv";

    // Gson 实例用于JSON处理
    private static final Gson GSON = new Gson();

    public static void main(String[] args) {
        // >>> 你想问AI的问题 <<<
        String userQuery = "这个文档是什么。";

        if ("YOUR_DEEPSEEK_API_KEY".equals(DEEPSEEK_API_KEY) || DEEPSEEK_API_KEY.isEmpty()) {
             System.err.println("错误：请将 YOUR_DEEPSEEK_API_KEY 替换为你的实际 API 密钥。");
             return;
        }
         if ("PATH_TO_YOUR_FILE.txt".equals(FILE_PATH) || FILE_PATH.isEmpty()) {
             System.err.println("错误：请将 PATH_TO_YOUR_FILE.txt 替换为你的实际文件路径。");
             return;
        }

        // 检查文件是否存在
        if (!Files.exists(Paths.get(FILE_PATH))) {
            System.err.println("错误：指定的文件不存在：" + FILE_PATH);
            return;
        }


        try {
            // 1. 读取文件内容
            String fileContent = readFileContent(FILE_PATH);
            System.out.println("文件内容读取成功。");
            // System.out.println("--- 文件内容开始 ---");
            // System.out.println(fileContent);
            // System.out.println("--- 文件内容结束 ---");

            if (fileContent.isEmpty()) {
                System.out.println("警告：文件内容为空。继续发送请求...");
            }

            // 2. 调用DeepSeek API并获取响应
            String aiResponse = callDeepSeekApi(fileContent, userQuery);

            // 3. 打印AI的回答
            System.out.println("\n--- DeepSeek AI 的回答 ---");
            System.out.println(aiResponse);
            System.out.println("--------------------------");

        } catch (IOException e) {
            System.err.println("文件读取错误：" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("调用 DeepSeek API 时发生错误：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 读取指定文件的所有内容为字符串。
     * @param filePath 文件路径
     * @return 文件内容字符串
     * @throws IOException 如果读取文件时发生错误
     */
    private static String readFileContent(String filePath) throws IOException {
        // 使用Files.readString (Java 11+) 简单读取文件所有内容
        // 对于大文件，建议使用BufferedReader逐行读取
        return Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
    }

    /**
     * 调用 DeepSeek API 发送消息并获取响应。
     * @param fileContent 文件内容
     * @param userQuery 用户问题
     * @return AI的回答字符串
     * @throws IOException 如果发生网络或其他I/O错误
     * @throws InterruptedException 如果线程被中断
     * @throws Exception 如果API返回错误或JSON解析失败
     */
    private static String callDeepSeekApi(String fileContent, String userQuery) throws IOException, InterruptedException, Exception {
        HttpClient client = HttpClient.newHttpClient();

        // 构建发送给API的消息内容
        // 将文件内容和用户问题组合起来作为用户消息的内容
        String combinedContent = "以下是文件的内容：\n\n---\n" + fileContent + "\n---\n\n我的问题是：" + userQuery;

        // 创建 Message 对象列表
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("user", combinedContent)); // 添加用户消息

        // 创建 ChatRequest 对象
        ChatRequest requestBody = new ChatRequest(DEEPSEEK_MODEL, messages);

        // 使用 Gson 将 Java 对象转换为 JSON 字符串
        String jsonRequest = GSON.toJson(requestBody);
        // System.out.println("发送的 JSON 请求体:\n" + jsonRequest); // 调试用

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DEEPSEEK_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + DEEPSEEK_API_KEY)
                .POST(BodyPublishers.ofString(jsonRequest, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        int statusCode = response.statusCode();
        String responseBody = response.body();

        if (statusCode != 200) {
            System.err.println("API 返回错误状态码: " + statusCode);
            System.err.println("错误响应体: " + responseBody);
            throw new Exception("API 调用失败，状态码: " + statusCode);
        }

        // 使用 Gson 将 JSON 响应体解析为 Java 对象
        ChatResponse chatResponse = GSON.fromJson(responseBody, ChatResponse.class);

        // 从解析后的对象中提取 AI 的回答
        if (chatResponse != null && chatResponse.getChoices() != null && !chatResponse.getChoices().isEmpty()) {
            // 获取第一个选项（通常只有一个）中的消息内容
            Message assistantMessage = chatResponse.getChoices().get(0).getMessage();
            if (assistantMessage != null && "assistant".equals(assistantMessage.getRole())) {
                return assistantMessage.getContent();
            }
        }

        // 如果解析失败或没有找到预期的内容
        System.err.println("警告：无法从响应中提取 AI 内容或响应格式不符合预期。完整响应体：");
        System.err.println(responseBody);
        return "无法解析 AI 回答。";
    }
}