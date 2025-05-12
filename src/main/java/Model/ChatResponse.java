package Model;

import java.util.List;

/**
 * Represents a response from an AI chat service in the Smart Finance Application.
 * This class encapsulates metadata about the response, including its identifier, object type,
 * creation timestamp, model used, and a list of response choices.
 *
 * @author Group 19
 * @version 1.0
 */
public class ChatResponse {
    /** The unique identifier of the chat response. */
    private String id;

    /** The type of object returned by the AI service (e.g., "chat.completion"). */
    private String object;

    /** The timestamp (in seconds since epoch) when the response was created. */
    private long created;

    /** The identifier of the AI model that generated the response. */
    private String model;

    /** The list of response choices provided by the AI service. */
    private List<Choice> choices;

    /**
     * Gets the unique identifier of the chat response.
     *
     * @return The response identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the type of object returned by the AI service.
     *
     * @return The object type (e.g., "chat.completion").
     */
    public String getObject() {
        return object;
    }

    /**
     * Gets the timestamp when the response was created.
     *
     * @return The creation timestamp in seconds since epoch.
     */
    public long getCreated() {
        return created;
    }

    /**
     * Gets the identifier of the AI model used.
     *
     * @return The model identifier.
     */
    public String getModel() {
        return model;
    }

    /**
     * Gets the list of response choices.
     *
     * @return The list of {@link Choice} objects.
     */
    public List<Choice> getChoices() {
        return choices;
    }

    /**
     * Sets the unique identifier of the chat response.
     *
     * @param id The response identifier to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets the type of object returned by the AI service.
     *
     * @param object The object type to set (e.g., "chat.completion").
     */
    public void setObject(String object) {
        this.object = object;
    }

    /**
     * Sets the timestamp when the response was created.
     *
     * @param created The creation timestamp in seconds since epoch to set.
     */
    public void setCreated(long created) {
        this.created = created;
    }

    /**
     * Sets the identifier of the AI model used.
     *
     * @param model The model identifier to set.
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * Sets the list of response choices.
     *
     * @param choices The list of {@link Choice} objects to set.
     */
    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }
}