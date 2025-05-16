package Model;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * A class that maps the intent and entity results output by predict.py.
 * This class is used to store the predicted intent, its confidence score, and associated entities
 * extracted from the input data.
 *
 * @version 1.2
 * @author group19
 */
public class PredictResult {

    /** The predicted intent from the input data. */
    private String intent;

    /** The confidence score of the predicted intent, serialized as "intent_score" in JSON. */
    @SerializedName("intent_score")
    private Double intentScore;

    /** A map containing the extracted entities, where the key is the entity type and the value is the entity value. */
    private Map<String, String> entities;

    /**
     * Default constructor for creating an empty PredictResult object.
     */
    public PredictResult() {
    }

    /**
     * Gets the predicted intent.
     *
     * @return the predicted intent as a String
     */
    public String getIntent() {
        return intent;
    }

    /**
     * Sets the predicted intent.
     *
     * @param intent the intent to set
     */
    public void setIntent(String intent) {
        this.intent = intent;
    }

    /**
     * Gets the confidence score of the predicted intent.
     *
     * @return the confidence score as a Double
     */
    public Double getIntentScore() {
        return intentScore;
    }

    /**
     * Sets the confidence score of the predicted intent.
     *
     * @param intentScore the confidence score to set
     */
    public void setIntentScore(Double intentScore) {
        this.intentScore = intentScore;
    }

    /**
     * Gets the map of extracted entities.
     *
     * @return a Map containing entity types as keys and entity values as values
     */
    public Map<String, String> getEntities() {
        return entities;
    }

    /**
     * Sets the map of extracted entities.
     *
     * @param entities the Map of entities to set
     */
    public void setEntities(Map<String, String> entities) {
        this.entities = entities;
    }

    /**
     * Returns a string representation of the PredictResult object.
     *
     * @return a string containing the intent, intent score, and entities
     */
    @Override
    public String toString() {
        return "PredictResult{" +
                "intent='" + intent + '\'' +
                ", intentScore=" + intentScore +
                ", entities=" + entities +
                '}';
    }
}