package Model;

/**
 * Represents a message in a chat conversation in the Smart Finance Application.
 * This class encapsulates the role of the message sender (e.g., "user", "assistant")
 * and the content of the message, used in interactions with an AI chat service.
 *
 * @author Group 19
 * @version 1.0
 */
public class Message {
    /** The role of the message sender (e.g., "user", "assistant"). */
    private String role;

    /** The content of the message. */
    private String content;

    /**
     * Constructs a Message with the specified role and content.
     *
     * @param role    The role of the message sender (e.g., "user", "assistant").
     * @param content The content of the message.
     */
    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    /**
     * Gets the role of the message sender.
     *
     * @return The role string, or null if not set.
     */
    public String getRole() {
        return role;
    }

    /**
     * Gets the content of the message.
     *
     * @return The message content, or null if not set.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the role of the message sender.
     *
     * @param role The role string to set (e.g., "user", "assistant").
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Sets the content of the message.
     *
     * @param content The message content to set.
     */
    public void setContent(String content) {
        this.content = content;
    }
}