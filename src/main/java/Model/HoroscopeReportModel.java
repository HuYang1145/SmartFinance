package Model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Data Transfer Object (DTO) to hold the generated horoscope report details in the Smart Finance Application.
 * This class encapsulates the title, description, and image path of a horoscope report, implementing
 * {@link Serializable} for data persistence.
 *
 * @author Group 19
 * @version 1.0
 */
public class HoroscopeReportModel implements Serializable {
    /** Serial version UID for serialization compatibility. */
    @Serial
    private static final long serialVersionUID = 1L;

    /** The title of the horoscope report (e.g., "This Week: Foodie Star!"). */
    private final String title;

    /** The descriptive text of the horoscope report. */
    private final String description;

    /** The relative path to the image resource associated with the report. */
    private final String imagePath;

    /**
     * Constructs a HoroscopeReportModel with the specified title, description, and image path.
     *
     * @param title       The title of the horoscope report.
     * @param description The descriptive text of the horoscope report.
     * @param imagePath   The relative path to the associated image resource.
     * @throws IllegalArgumentException if any parameter is null or empty after trimming.
     */
    public HoroscopeReportModel(String title, String description, String imagePath) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        if (imagePath == null || imagePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Image path cannot be null or empty");
        }
        this.title = title;
        this.description = description;
        this.imagePath = imagePath;
    }

    /**
     * Gets the title of the horoscope report.
     *
     * @return The title of the report.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the descriptive text of the horoscope report.
     *
     * @return The description of the report.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the relative path to the image resource associated with the report.
     *
     * @return The image path.
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * Returns a string representation of the horoscope report.
     *
     * @return A string containing the title, description, and image path.
     */
    @Override
    public String toString() {
        return "HoroscopeReport{" +
               "title='" + title + '\'' +
               ", description='" + description + '\'' +
               ", imagePath='" + imagePath + '\'' +
               '}';
    }

    /**
     * Compares this horoscope report to another object for equality.
     * Two reports are equal if they have the same title, description, and image path.
     *
     * @param o The object to compare with.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HoroscopeReportModel that = (HoroscopeReportModel) o;
        return Objects.equals(title, that.title) &&
               Objects.equals(description, that.description) &&
               Objects.equals(imagePath, that.imagePath);
    }

    /**
     * Generates a hash code for the horoscope report based on its title, description, and image path.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(title, description, imagePath);
    }
}