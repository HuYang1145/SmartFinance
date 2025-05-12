package Model;

import java.util.Map;
import com.google.gson.annotations.SerializedName;

/**
 * 映射 predict.py 输出的意图与实体结果
 */
public class PredictResult {
    private String intent;

    @SerializedName("intent_score")
    private Double intentScore;

    private Map<String, String> entities;

    public PredictResult() {
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public Double getIntentScore() {
        return intentScore;
    }

    public void setIntentScore(Double intentScore) {
        this.intentScore = intentScore;
    }

    public Map<String, String> getEntities() {
        return entities;
    }

    public void setEntities(Map<String, String> entities) {
        this.entities = entities;
    }

    @Override
    public String toString() {
        return "PredictResult{" +
                "intent='" + intent + '\'' +
                ", intentScore=" + intentScore +
                ", entities=" + entities +
                '}';
    }
}
