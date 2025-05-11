package Model;

public class AIResponse {
    private final String reply;
    private final String errorMessage;

    public AIResponse(String reply, String errorMessage) {
        this.reply = reply;
        this.errorMessage = errorMessage;
    }

    public String getReply() {
        return reply;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isSuccess() {
        return errorMessage == null;
    }
}