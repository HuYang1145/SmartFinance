package Controller;

import java.time.LocalDateTime;

import javax.swing.SwingWorker;

import Model.AIResponse;
import Model.ChatMessage;
import Service.AIService;
import View.AI.AIPanel;

public class AIController implements AIPanel.AIViewListener {
    private final AIPanel view;
    private final AIService aiService;

    public AIController(AIPanel view, AIService aiService) {
        this.view = view;
        this.aiService = aiService;
        this.view.setListener(this);
    }

    @Override
    public void onSendMessage(String message) {
        ChatMessage userMessage = new ChatMessage(message, true, LocalDateTime.now());
        view.addMessage(userMessage);
        view.setLoadingState(true);

        SwingWorker<AIResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected AIResponse doInBackground() {
                return aiService.predictReply(message);
            }

            @Override
            protected void done() {
                try {
                    AIResponse response = get();
                    view.setLoadingState(false);
                    if (response.isSuccess()) {
                        ChatMessage aiMessage = new ChatMessage(response.getReply(), false, LocalDateTime.now());
                        view.addMessage(aiMessage);
                    } else {
                        view.showError(response.getErrorMessage());
                    }
                } catch (Exception e) {
                    view.setLoadingState(false);
                    view.showError("Failed to process AI response: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
}