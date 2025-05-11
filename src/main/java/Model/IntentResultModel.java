package Model;

/**
 * Represents the result of an intent recognition process in the Smart Finance Application.
 * This class encapsulates the identified intent, typically used to interpret user input
 * for actions such as transaction processing or query handling.
 *
 * @author Group 19
 * @version 1.0
 */
public class IntentResultModel {
    /** The identified intent from user input. */
    private String intent;

    /**
     * Gets the identified intent.
     *
     * @return The intent string, or null if not set.
     */
    public String getIntent() {
        return intent;
    }

    /**
     * Sets the identified intent.
     *
     * @param intent The intent string to set.
     */
    public void setIntent(String intent) {
        this.intent = intent;
    }
}