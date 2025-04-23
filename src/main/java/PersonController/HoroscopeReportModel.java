// Put this class in its own file: PersonModel/HoroscopeReport.java
package PersonController;

/**
 * Data Transfer Object (DTO) to hold the generated horoscope report details.
 */
public class HoroscopeReportModel {
    private final String title;         // e.g., "This Week: Foodie Star!"
    private final String description;   // The generated horoscope text
    private final String imagePath;     // Relative path to the image resource

    /**
     * Constructor for HoroscopeReport.
     * @param title The title of the report.
     * @param description The descriptive text of the report.
     * @param imagePath The relative path to the associated image.
     */
    public HoroscopeReportModel(String title, String description, String imagePath) {
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
}