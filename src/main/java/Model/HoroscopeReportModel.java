package Model;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList; // Import ArrayList is good practice for explicit constructor use
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Data Transfer Object (DTO) that encapsulates the details of a generated horoscope (spending star) report.
 * This model includes the report's title, descriptive text, the filename of an associated image,
 * the month for which the report was generated, and a list of transactions for that month.
 * <p>
 * This class is {@link Serializable} to allow for potential persistence or transfer.
 * </p>
 *
 * @author Group 19
 * @version 1.2 (Added selectedMonth, monthlyTransactions, uses imageFileName)
 */
public class HoroscopeReportModel implements Serializable {
    /**
     * The serial version UID for serialization.
     */
    @Serial
    private static final long serialVersionUID = 2L;

    private final String title;
    private final String description;
    /**
     * Stores only the filename of the image (e.g., "food.jpg", "failed.jpg").
     * The full path is typically constructed by the controller or view.
     */
    private final String imageFileName;
    /**
     * The month for which this report is generated (1 for January, ..., 12 for December).
     */
    private final int selectedMonth;
    /**
     * An unmodifiable list of transactions relevant to the selected month.
     */
    private final List<Transaction> monthlyTransactions;

    /**
     * Constructs a {@code HoroscopeReportModel}.
     *
     * @param title             The title of the horoscope report. Must not be null or empty.
     * @param description       The descriptive text of the horoscope report. Must not be null or empty.
     * @param imageFileName     The filename of the associated image (e.g., "food.jpg").
     * Can be a fallback like "failed.jpg". May be null or empty if no specific image applies,
     * though the {@link Service.HoroscopeService} aims to provide one (e.g., "failed.jpg").
     * @param selectedMonth     The month (an integer from 1 for January to 12 for December) for which this report is generated.
     * Must be a valid month number.
     * @param monthlyTransactions A list of {@link Transaction} objects for the selected month.
     * If null, an empty list will be used. The provided list is defensively copied.
     * @throws IllegalArgumentException if {@code title} or {@code description} are null or empty,
     * or if {@code selectedMonth} is not between 1 and 12 (inclusive).
     */
    public HoroscopeReportModel(String title, String description, String imageFileName,
                                int selectedMonth, List<Transaction> monthlyTransactions) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        if (selectedMonth < 1 || selectedMonth > 12) {
            throw new IllegalArgumentException("Selected month must be between 1 and 12.");
        }

        this.title = title;
        this.description = description;
        this.imageFileName = imageFileName; // Can be null or empty
        this.selectedMonth = selectedMonth;

        // Ensure monthlyTransactions is never null and store an unmodifiable copy
        this.monthlyTransactions = (monthlyTransactions != null) ?
                Collections.unmodifiableList(new ArrayList<>(monthlyTransactions)) :
                Collections.emptyList();
    }

    /**
     * Gets the title of the horoscope report.
     * @return The report title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the description text of the horoscope report.
     * @return The report description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the filename of the image associated with this report.
     * @return The image filename (e.g., "food.jpg"), or null/empty if no specific image.
     */
    public String getImageFileName() {
        return imageFileName;
    }

    /**
     * Gets the month for which this report was generated.
     * @return The selected month as an integer (1 for January, ..., 12 for December).
     */
    public int getSelectedMonth() {
        return selectedMonth;
    }

    /**
     * Gets the list of transactions for the selected month.
     * The returned list is unmodifiable.
     * @return An unmodifiable list of {@link Transaction} objects.
     */
    public List<Transaction> getMonthlyTransactions() {
        return monthlyTransactions;
    }

    /**
     * Returns a string representation of the {@code HoroscopeReportModel}.
     * Includes title, description, image filename, selected month, and the number of transactions.
     * @return A string summary of this report model.
     */
    @Override
    public String toString() {
        return "HoroscopeReportModel{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", imageFileName='" + imageFileName + '\'' +
                ", selectedMonth=" + selectedMonth +
                ", numTransactions=" + monthlyTransactions.size() +
                '}';
    }

    /**
     * Compares this {@code HoroscopeReportModel} to another object for equality.
     * Two models are considered equal if all their fields (title, description, imageFileName,
     * selectedMonth, and monthlyTransactions) are equal.
     *
     * @param o The object to compare with.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HoroscopeReportModel that = (HoroscopeReportModel) o;
        return selectedMonth == that.selectedMonth &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(imageFileName, that.imageFileName) &&
                Objects.equals(monthlyTransactions, that.monthlyTransactions); // Compares list content
    }

    /**
     * Generates a hash code for this {@code HoroscopeReportModel}.
     * The hash code is based on all fields: title, description, imageFileName,
     * selectedMonth, and monthlyTransactions.
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(title, description, imageFileName, selectedMonth, monthlyTransactions);
    }
}