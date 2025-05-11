package Model;

import java.util.List;

/**
 * Represents a chat request in the Smart Finance Application.
 * This class encapsulates the model identifier and a list of messages to be sent
 * to an AI service for processing.
 *
 * @author Group 19
 * @version 1.0
 */
public class ChatRequest {
    /** The identifier of the AI model to process the chat request. */
    private String model;

    /** The list of messages included in the chat request. */
    private List<Message> messages;

    /**
     * Constructs a ChatRequest with the specified model and messages.
     *
     * @param model    The identifier of the AI model to process the request.
     * @param messages The list of messages to be sent to the AI service.
     */
    public ChatRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }

    /**
     * Gets the identifier of the AI model.
     *
     * @return The model identifier.
     */
    public String getModel() {
        return model;
    }

    /**
     * Gets the list of messages in the chat request.
     *
     * @return The list of {@link Message} objects.
     */
    public List<Message> getMessages() {
        return messages;
    }
}