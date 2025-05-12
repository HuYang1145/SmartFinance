package Model;

/**
 * Represents a budget in the Smart Finance Application.
 * This class encapsulates the details of a user's budget, including the username,
 * budget amount, mode (e.g., NORMAL, ECONOMICAL_UNSTABLE), and creation time.
 *
 * @author Group 19
 * @version 1.0
 */
public class Budget {
    /** The username associated with the budget. */
    private String username;

    /** The budget amount. */
    private double amount;

    /** The budget mode (e.g., NORMAL, ECONOMICAL_UNSTABLE). */
    private String mode;

    /** The creation time of the budget. */
    private String creationTime;

    /**
     * Constructs a Budget with the specified details.
     *
     * @param username     The username associated with the budget.
     * @param amount       The budget amount.
     * @param mode         The budget mode (e.g., NORMAL, ECONOMICAL_UNSTABLE).
     * @param creationTime The creation time of the budget.
     */
    public Budget(String username, double amount, String mode, String creationTime) {
        this.username = username;
        this.amount = amount;
        this.mode = mode;
        this.creationTime = creationTime;
    }

    /**
     * Gets the username associated with the budget.
     *
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username associated with the budget.
     *
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the budget amount.
     *
     * @return The budget amount.
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Sets the budget amount.
     *
     * @param amount The budget amount to set.
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * Gets the budget mode.
     *
     * @return The budget mode (e.g., NORMAL, ECONOMICAL_UNSTABLE).
     */
    public String getMode() {
        return mode;
    }

    /**
     * Sets the budget mode.
     *
     * @param mode The budget mode to set (e.g., NORMAL, ECONOMICAL_UNSTABLE).
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * Gets the creation time of the budget.
     *
     * @return The creation time.
     */
    public String getCreationTime() {
        return creationTime;
    }

    /**
     * Sets the creation time of the budget.
     *
     * @param creationTime The creation time to set.
     */
    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }
}