
/**
 * Represents the result of entity recognition, containing extracted entities such as amount, time, and category.
 *
 * @author Group 19
 * @version 1.0
 */
package Model;

public class EntityResultModel {
    private String amount;
    private String time;
    private String category;

    /**
     * Gets the extracted amount entity.
     *
     * @return the amount as a string, or null if not present
     */
    public String getAmount() {
        return amount;
    }

    /**
     * Sets the amount entity.
     *
     * @param amount the amount to set
     */
    public void setAmount(String amount) {
        this.amount = amount;
    }

    /**
     * Gets the extracted time entity.
     *
     * @return the time as a string, or null if not present
     */
    public String getTime() {
        return time;
    }

    /**
     * Sets the time entity.
     *
     * @param time the time to set
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * Gets the extracted category entity.
     *
     * @return the category as a string, or null if not present
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category entity.
     *
     * @param category the category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }
}
