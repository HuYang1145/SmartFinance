package Model;

import java.time.LocalDateTime;

/**
 * Represents a chat message in the Smart Finance Application.
 * This class encapsulates the content of a message, whether it was sent by the user,
 * and the timestamp of when the message was created.
 *
 * @author Group 19
 * @version 1.0
 */
public class ChatMessage {
    /** The content of the chat message. */
    private final String content;

    /** Indicates whether the message was sent by the user. */
    private final boolean isUserSent;

    /** The timestamp of when the message was created. */
    private final LocalDateTime timestamp;

    /**
     * Constructs a ChatMessage with the specified content, sender information, and timestamp.
     *
     * @param content     The content of the chat message.
     * @param isUserSent  {@code true} if the message was sent by the user, {@code false} otherwise.
     * @param timestamp   The timestamp of when the message was created.
     */
    public ChatMessage(String content, boolean isUserSent, LocalDateTime timestamp) {
        this.content = content;
        this.isUserSent = isUserSent;
        this.timestamp = timestamp;
    }

    /**
     * Gets the content of the chat message.
     *
     * @return The message content.
     */
    public String getContent() {
        return content;
    }

    /**
     * Checks if the message was sent by the user.
     *
     * @return {@code true} if the message was sent by the user, {@code false} otherwise.
     */
    public boolean isUserSent() {
        return isUserSent;
    }

    /**
     * Gets the timestamp of when the message was created.
     *
     * @return The message timestamp.
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}