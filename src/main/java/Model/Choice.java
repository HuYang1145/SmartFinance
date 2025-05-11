package Model;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a single choice in a chat response from an AI service in the Smart Finance Application.
 * This class encapsulates the index of the choice, the message content, and the reason why the response generation finished.
 *
 * @author Group 19
 * @version 1.0
 */
public class Choice {
    /** The index of this choice in the list of response choices. */
    private int index;

    /** The message content associated with this choice. */
    private Message message;

    /** The reason why the response generation finished (e.g., "stop", "length"). */
    @SerializedName("finish_reason")
    private String finishReason;

    /**
     * Gets the index of this choice.
     *
     * @return The index of the choice.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets the message content associated with this choice.
     *
     * @return The {@link Message} object.
     */
    public Message getMessage() {
        return message;
    }

    /**
     * Gets the reason why the response generation finished.
     *
     * @return The finish reason (e.g., "stop", "length"), or null if not specified.
     */
    public String getFinishReason() {
        return finishReason;
    }

    /**
     * Sets the index of this choice.
     *
     * @param index The index to set.
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Sets the message content associated with this choice.
     *
     * @param message The {@link Message} object to set.
     */
    public void setMessage(Message message) {
        this.message = message;
    }

    /**
     * Sets the reason why the response generation finished.
     *
     * @param finishReason The finish reason to set (e.g., "stop", "length").
     */
    public void setFinishReason(String finishReason) {
        this.finishReason = finishReason;
    }
}