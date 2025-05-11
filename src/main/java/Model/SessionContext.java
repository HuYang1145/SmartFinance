package Model;

import java.util.Map;
import java.util.List;
public class SessionContext {
    String intent;                // 当前意图
    Map<String, String> slots;    // 已填的槽位
    List<String> missingSlots;    // 剩余需要用户补全的槽位
    private boolean confirmed = false;
    public boolean isConfirmed() { return confirmed; }
    public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }


    public List<String> getMissingSlots() {
        return missingSlots;
    }

    public void setMissingSlots(List<String> missingSlots) {
        this.missingSlots = missingSlots;
    }

    public Map<String, String> getSlots() {
        return slots;
    }

    public void setSlots(Map<String, String> slots) {
        this.slots = slots;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }
}
