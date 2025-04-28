package Model;

import com.google.gson.annotations.SerializedName;

public class Choice {
    private int index;
    private Message message;
    @SerializedName("finish_reason")
    private String finishReason;

    public int getIndex() {
        return index;
    }

    public Message getMessage() {
        return message;
    }

    public String getFinishReason() {
        return finishReason;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public void setFinishReason(String finishReason) {
        this.finishReason = finishReason;
    }
}