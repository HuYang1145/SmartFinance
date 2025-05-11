package Model;

import java.util.List;
import java.util.Map;

/**
 * Represents the context of a user session in the Smart Finance Application.
 * This class encapsulates the current intent, filled slots, missing slots, and confirmation status
 * for managing conversational interactions, such as intent recognition and slot-filling processes.
 *
 * @author Group 19
 * @version 1.0
 */
public class SessionContext {
    /** The current intent identified from user input. */
    String intent;

    /** A map of filled slots, where keys are slot names and values are slot values. */
    Map<String, String> slots;

    /** A list of slot names that still need to be filled by the user. */
    List<String> missingSlots;

    /** Indicates whether the session context has been confirmed by the user. */
    private boolean confirmed = false;

    /**
     * Checks if the session context has been confirmed.
     *
     * @return {@code true} if the session context is confirmed, {@code false} otherwise.
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Sets the confirmation status of the session context.
     *
     * @param confirmed {@code true} to mark the session context as confirmed, {@code false} otherwise.
     */
    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    /**
     * Gets the list of slot names that still need to be filled.
     *
     * @return The list of missing slot names, or null if not set.
     */
    public List<String> getMissingSlots() {
        return missingSlots;
    }

    /**
     * Sets the list of slot names that still need to be filled.
     *
     * @param missingSlots The list of missing slot names to set.
     */
    public void setMissingSlots(List<String> missingSlots) {
        this.missingSlots = missingSlots;
    }

    /**
     * Gets the map of filled slots.
     *
     * @return The map of slot names to their values, or null if not set.
     */
    public Map<String, String> getSlots() {
        return slots;
    }

    /**
     * Sets the map of filled slots.
     *
     * @param slots The map of slot names to their values to set.
     */
    public void setSlots(Map<String, String> slots) {
        this.slots = slots;
    }

    /**
     * Gets the current intent.
     *
     * @return The intent string, or null if not set.
     */
    public String getIntent() {
        return intent;
    }

    /**
     * Sets the current intent.
     *
     * @param intent The intent string to set.
     */
    public void setIntent(String intent) {
        this.intent = intent;
    }
}