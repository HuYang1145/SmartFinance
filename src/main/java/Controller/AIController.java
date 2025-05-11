package Controller;

import java.time.LocalDateTime;

import javax.swing.SwingWorker;

import Model.AIResponse;
import Model.ChatMessage;
import Service.AIService;
import View.AI.AIPanel;

/**
 * Manages interactions between the AI panel and the AI service in the finance management system.
 * Handles user messages, sends them to the AI service for processing, and updates the UI with responses.
 * Implements the AIPanel.AIViewListener interface to listen for user input events.
 *
 * @author Group 19
 * @version 1.0
 */
public class AIController implements AIPanel.AIViewListener {
    /** The AI panel for displaying chat messages and UI updates. */
    private final AIPanel view;
    /** The service for processing AI responses. */
    private final AIService aiService;

    /**
     * Constructs an AIController with the specified AI panel and service.
     * Sets this controller as the listener for the AI panel's events.
     *
     * @param view      the AI panel for user interaction
     * @param aiService the service for generating AI responses
     */
    public AIController(AIPanel view, AIService aiService) {
        this.view = view;
        this.aiService = aiService;
        this.view.setListener(this);
    }

    /**
     * Handles the event when a user sends a message in the AI panel.
     * Displays the user's message, sends it to the AI service asynchronously,
     * and updates the UI with the AI's response or an error message.
     *
     * @param message the user's input message
     */
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