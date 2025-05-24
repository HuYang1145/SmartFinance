/**
 * Provides integration with the DeepSeek API to generate AI-driven responses based on transaction summaries and prompts.
 * Handles HTTP requests and JSON parsing for communication with the DeepSeek chat completions endpoint.
 *
 * @author Group 19
 * @version 1.0
 */
package Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;

import Model.ChatRequest;
import Model.ChatResponse;
import Model.Message;

public class DeepSeekService {
    private static final String DEEPSEEK_API_KEY = "sk-7ae19476ebd14de3b1a93c594c267886";
    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/chat/completions";
    private static final String DEEPSEEK_MODEL = "deepseek-reasoner";
    private static final Gson GSON = new Gson();

    /**
     * Calls the DeepSeek API to generate a response based on a transaction summary and user prompt.
     *
     * @param summary the transaction summary to provide context
     * @param prompt  the specific instruction or question for the AI
     * @return the AI-generated response content
     * @throws RuntimeException if the API call fails or the response cannot be parsed
     */
    public String callDeepSeekApi(String summary, String prompt) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String combinedContent = "Transaction summary:\n\n" + summary + "\n\nPrompt: " + prompt;
            List<Message> messages = new ArrayList<>();
            messages.add(new Message("user", combinedContent));
            ChatRequest requestBody = new ChatRequest(DEEPSEEK_MODEL, messages);
            String jsonRequest = GSON.toJson(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(DEEPSEEK_API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + DEEPSEEK_API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            String responseBody = response.body();

            if (statusCode != 200) {
                System.err.println("DeepSeek API error: Status " + statusCode + ", Response: " + responseBody);
                throw new RuntimeException("API call failed with status: " + statusCode);
            }

            ChatResponse chatResponse = GSON.fromJson(responseBody, ChatResponse.class);
            if (chatResponse != null && chatResponse.getChoices() != null && !chatResponse.getChoices().isEmpty()) {
                Message assistantMessage = chatResponse.getChoices().get(0).getMessage();
                if (assistantMessage != null && "assistant".equals(assistantMessage.getRole())) {
                    return assistantMessage.getContent();
                }
            }

            System.err.println("Invalid DeepSeek response: " + responseBody);
            throw new RuntimeException("Unable to parse AI response.");
        } catch (Exception e) {
            System.err.println("DeepSeek API error: " + e.getMessage());
            throw new RuntimeException("Failed to fetch suggestions from DeepSeek API: " + e.getMessage());
        }
    }

    /**
     * Calls the DeepSeek API to specialise in trade class prediction
     * * @param merchant
     * @param merchant merchant name (optional, null-safe)
     * @param remark remark information (optional, null-safe)
     * @param transactionType Transaction type (e.g. ‘Expense’, ‘Income’), which can help the model to distinguish between income/expense
     * @return The predicted category, which is necessarily one of these 11:
     * [Food, Shopping, Housing, Transportation, Entertainment.
     * Health, Education, Travel, Investment, Gift, Other]
     */

    public String predictCategory(String merchant, String remark, String transactionType) {
        // 1. 拼 prompt
        StringBuilder promptBuilder = new StringBuilder()
                .append("Please select the most appropriate category from [")
                .append("food, salary, rent, freelance, investment,shopping, Electronics ")
                .append("Fitness, Transport, Entertainment, Travel, Gift, Other")
                .append("]. Only return the category name. If no information, return \"Other\".")
                .append("\n\nTransaction info:");
        if (transactionType != null && !transactionType.isBlank()) {
            promptBuilder.append(" type=").append(transactionType).append(";");
        }
        if (merchant != null && !merchant.isBlank()) {
            promptBuilder.append(" merchant=").append(merchant).append(";");
        }
        if (remark != null && !remark.isBlank()) {
            promptBuilder.append(" remark=").append(remark).append(";");
        }

        String prompt = promptBuilder.toString();

        String raw = callDeepSeekApi("", prompt).strip();

        List<String> allowed = Arrays.asList(
                "food", "salary", "rent", "freelance", "investment",
                "shopping", "Electronics", "Fitness", "Transport",
                "Entertainment", "Travel", "Gift","Other"
        );
        if (allowed.contains(raw)) {
            return raw;
        }
        return "Other";
    }

    /**
     * Extracts transaction categories from the text of a single sentence.
     *
     * @param text Any sentence of user input (e.g. ‘I bought a coffee at Starbucks’).
     * @return from [Food, Shopping, Housing, Transportation.
     * Entertainment, Health, Education, Travel.
     * Investment, Gift, Other] from [Food, Shopping, Housing, Transportation, * Entertainment, Health, Education, Travel, * Investment, Gift, Other].
     */


    public String extractCategory(String text) {
        String prompt = new StringBuilder()
                .append("Please select the most appropriate category from [")
                .append("food, salary, rent, freelance, investment,shopping, Electronics ")
                .append("Fitness, Transport, Entertainment, Travel, Gift, Other")
                .append("]. Only return the category name. No other words.")
                .append("\n\nInput: \"").append(text).append("\"")
                .toString();

        String raw;
        try {
            raw = callDeepSeekApi("", prompt).strip();
        } catch (Exception e) {
            System.err.println("Category extraction failed: " + e.getMessage());
            return "Other";
        }

        List<String> allowed = List.of(
                "food", "salary", "rent", "freelance", "investment",
                "shopping", "Electronics", "Fitness", "Transport",
                "Entertainment", "Travel", "Gift","Other"
        );
        return allowed.contains(raw) ? raw : "Other";
    }


}