package Model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Data Transfer Object (DTO) to hold the generated horoscope report details.
 */
public class HoroscopeReportModel implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String title;         // e.g., "This Week: Foodie Star!"
    private final String description;   // The generated horoscope text
    private final String imagePath;     // Relative path to the image resource

    /**
     * Constructor for HoroscopeReport.
     *
     * @param title The title of the report.
     * @param description The descriptive text of the report.
     * @param imagePath The relative path to the associated image.
     * @throws IllegalArgumentException if any parameter is null or empty.
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

    // --- Getters ---

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }

    @Override
    public String toString() {
        return "HoroscopeReport{" +
               "title='" + title + '\'' +
               ", description='" + description + '\'' +
               ", imagePath='" + imagePath + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HoroscopeReportModel that = (HoroscopeReportModel) o;
        return Objects.equals(title, that.title) &&
               Objects.equals(description, that.description) &&
               Objects.equals(imagePath, that.imagePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, imagePath);
    }
}