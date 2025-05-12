package Model;

/**
 * Represents a response from an AI service in the Smart Finance Application.
 * This class encapsulates the AI's reply and any associated error message,
 * providing a way to check if the response was successful.
 *
 * @author Group 19
 * @version 1.0
 */
public class AIResponse {
    /** The reply from the AI service, or null if an error occurred. */
    private final String reply;

    /** The error message if the AI request failed, or null if successful. */
    private final String errorMessage;

    /**
     * Constructs an AIResponse with the specified reply and error message.
     *
     * @param reply        The reply from the AI service, or null if an error occurred.
     * @param errorMessage The error message if the request failed, or null if successful.
     */
    public AIResponse(String reply, String errorMessage) {
        this.reply = reply;
        this.errorMessage = errorMessage;
    }

    /**
     * Gets the reply from the AI service.
     *
     * @return The reply string, or null if no reply was received.
     */
    public String getReply() {
        return reply;
    }

    /**
     * Gets the error message associated with the AI request.
     *
     * @return The error message, or null if the request was successful.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Checks if the AI response was successful.
     *
     * @return {@code true} if the response has no error message, {@code false} otherwise.
     */
    public boolean isSuccess() {
        return errorMessage == null;
    }
}