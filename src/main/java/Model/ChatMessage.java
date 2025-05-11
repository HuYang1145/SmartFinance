package Model;

import java.time.LocalDateTime;

public class ChatMessage {
    private final String content;
    private final boolean isUserSent;
    private final LocalDateTime timestamp;

    public ChatMessage(String content, boolean isUserSent, LocalDateTime timestamp) {
        this.content = content;
        this.isUserSent = isUserSent;
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public boolean isUserSent() {
        return isUserSent;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}