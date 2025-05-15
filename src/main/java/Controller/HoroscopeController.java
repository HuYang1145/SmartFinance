package Controller;

import Model.HoroscopeReportModel;
// import Model.Transaction; // Not directly used in this class, consider removing if not needed by a commented-out part
import Service.HoroscopeService;
import Service.ImageLoader;
import View.HoroscopePanel.HoroscopePanel;

import javax.swing.ImageIcon;
import javax.swing.SwingWorker;
import java.io.File;
import java.time.LocalDate;
import java.util.Collections;
// import java.util.List; // Not directly used, consider removing

/**
 * Controller for the Horoscope feature.
 * Manages interactions between the {@link HoroscopePanel} (view) and the {@link HoroscopeService} (model/service).
 * It handles requests to load welcome images, generate monthly spending star reports, and navigate UI states.
 * Images are loaded asynchronously using {@link ImageLoader}.
 */
public class HoroscopeController {
    private final String username;
    private final HoroscopePanel view;
    private final HoroscopeService horoscopeService;
    private final ImageLoader imageLoader;

    /**
     * Fallback width for the welcome image if panel dimensions are not available or too small.
     * Intended for a "Super Big" display.
     */
    private static final int FALLBACK_WELCOME_IMAGE_WIDTH = 1200;
    /**
     * Fallback height for the welcome image if panel dimensions are not available or too small.
     * Intended for a "Super Big" display.
     */
    private static final int FALLBACK_WELCOME_IMAGE_HEIGHT = 900;

    /**
     * Fallback width for the report image if panel dimensions are not available or too small.
     * Intended for a "Medium-Large" display.
     */
    private static final int FALLBACK_REPORT_IMAGE_WIDTH = 550;
    /**
     * Fallback height for the report image if panel dimensions are not available or too small.
     * Intended for a "Medium-Large" display.
     */
    private static final int FALLBACK_REPORT_IMAGE_HEIGHT = 412;

    /**
     * Base path for horoscope-related images stored in the file system.
     * Constructed using {@code File.separator} for platform independence.
     */
    private static final String HOROSCOPE_IMAGE_BASE_PATH = "src" + File.separator + "test" + File.separator + "horoscope" + File.separator;

    /**
     * Constructs a HoroscopeController.
     *
     * @param username The username of the current user. If null or empty, attempts to retrieve
     * from {@link Model.UserSession}.
     * @param view     The {@link HoroscopePanel} instance this controller will manage.
     * @throws IllegalArgumentException if the username cannot be determined (is null or empty after attempting session retrieval)
     * and the view is available to show an error.
     */
    public HoroscopeController(String username, HoroscopePanel view) {
        this.view = view;
        // Prioritize provided username, then fallback to UserSession
        this.username = (username != null && !username.trim().isEmpty()) ? username : Model.UserSession.getCurrentUsername();

        if (this.username == null || this.username.trim().isEmpty()) {
            if (this.view != null) {
                this.view.showError("User context not found. Horoscope feature requires login.");
            }
            // This exception ensures the controller is not created in an invalid state.
            throw new IllegalArgumentException("Username cannot be null or empty for HoroscopeController");
        }
        this.horoscopeService = new HoroscopeService();
        this.imageLoader = new ImageLoader();
    }

    /**
     * Loads the welcome image asynchronously.
     * The image is scaled to fit the provided target container dimensions or uses fallback dimensions.
     * Updates the view with the loaded image or an error message.
     *
     * @param targetContainerWidth  The target width of the container where the image will be displayed.
     * If less than or equal to 50, fallback width is used.
     * @param targetContainerHeight The target height of the container where the image will be displayed.
     * If less than or equal to 50, fallback height is used.
     */
    public void loadWelcomeImage(int targetContainerWidth, int targetContainerHeight) {
        if (view == null) return; // Safety check
        view.setLoadingState(true);
        String welcomeImageName = "welcome.jpg";
        String welcomeImagePath = HOROSCOPE_IMAGE_BASE_PATH + welcomeImageName;
        System.out.println("HoroscopeController: Attempting to load welcome image: " + new File(welcomeImagePath).getAbsolutePath());

        // Use fallback if provided dimensions are too small or invalid
        int actualTargetWidth = (targetContainerWidth > 50) ? targetContainerWidth : FALLBACK_WELCOME_IMAGE_WIDTH;
        int actualTargetHeight = (targetContainerHeight > 50) ? targetContainerHeight : FALLBACK_WELCOME_IMAGE_HEIGHT;

        System.out.println("HoroscopeController: Welcome image target dimensions for ImageLoader: " + actualTargetWidth + "x" + actualTargetHeight);

        imageLoader.loadImageFromFileSystem(welcomeImagePath,
                actualTargetWidth,
                actualTargetHeight,
                new ImageLoader.Callback() {
                    @Override
                    public void onSuccess(ImageIcon icon) {
                        if (view != null) {
                            view.setWelcomeImage(icon);
                            view.setLoadingState(false);
                        }
                    }
                    @Override
                    public void onError(String message) {
                        if (view != null) {
                            view.setWelcomeImage(null); // Clear any previous image
                            view.showError("Failed to load welcome image (" + welcomeImageName + "): " + message);
                            view.setLoadingState(false);
                        }
                    }
                });
    }

    /**
     * Processes a request to generate and display a horoscope (spending star) report for a selected month.
     * Validates the selected month and then calls {@link #loadMonthlyReport(int)}.
     *
     * @param selectedMonth The month number (1 for January, ..., 12 for December).
     */
    public void processHoroscopeRequest(int selectedMonth) {
        if (view == null) return;
        if (selectedMonth < 1 || selectedMonth > 12) {
            view.showError("Invalid month selected. Please choose a valid month.");
            return;
        }
        loadMonthlyReport(selectedMonth);
    }

    /**
     * Handles the action when the "back" or "try another month" button is clicked.
     * Switches the view to the welcome card and reloads the welcome image if the panel is visible.
     */
    public void onBackClicked() {
        if (view == null) return;
        view.showWelcomeCard();
        // Reload welcome image if the panel is currently showing and has valid dimensions
        if (view.isShowing()) { // Check if the panel itself is visible
            loadWelcomeImage(view.getWelcomeImageLabelWidth(), view.getWelcomeImageLabelHeight());
        }
    }

    /**
     * Loads the monthly spending star report asynchronously.
     * Fetches the report data using {@link HoroscopeService} and then loads the associated image.
     * Updates the view with the report details (title, description, image, transactions) or an error message.
     *
     * @param selectedMonth The month number (1-12) for which to generate the report.
     */
    private void loadMonthlyReport(int selectedMonth) {
        if (view == null) return;
        view.setLoadingState(true);
        final int reportYear = LocalDate.now().getYear(); // Assume current year for reports

        SwingWorker<HoroscopeReportModel, Void> worker = new SwingWorker<>() {
            @Override
            protected HoroscopeReportModel doInBackground() throws Exception {
                // This runs on a background thread
                return horoscopeService.generateMonthlyHoroscopeReport(username, selectedMonth, reportYear);
            }

            @Override
            protected void done() {
                // This runs on the EDT
                if (view == null) return;
                try {
                    HoroscopeReportModel report = get(); // Retrieve result from doInBackground
                    if (report == null) { // Should not happen if service returns a valid model or error model
                        view.showError("Failed to generate horoscope report (Service returned null).");
                        view.setLoadingState(false);
                        return;
                    }

                    String reportImageFileName = report.getImageFileName();
                    String fullReportImagePath = HOROSCOPE_IMAGE_BASE_PATH + reportImageFileName;
                    System.out.println("HoroscopeController: Loading report image '" + reportImageFileName + "' from: " + new File(fullReportImagePath).getAbsolutePath());

                    if (reportImageFileName != null && !reportImageFileName.isEmpty()) {
                        int panelReportWidth = view.getReportImageLabelWidth();
                        int panelReportHeight = view.getReportImageLabelHeight();

                        // Use fallback if panel dimensions are too small or invalid
                        int finalReportTargetWidth = (panelReportWidth > 100) ? panelReportWidth : FALLBACK_REPORT_IMAGE_WIDTH;
                        int finalReportTargetHeight = (panelReportHeight > 100) ? panelReportHeight : FALLBACK_REPORT_IMAGE_HEIGHT;

                        System.out.println("HoroscopeController: Report image target dimensions for ImageLoader: " + finalReportTargetWidth + "x" + finalReportTargetHeight);

                        imageLoader.loadImageFromFileSystem(fullReportImagePath,
                                finalReportTargetWidth,
                                finalReportTargetHeight,
                                new ImageLoader.Callback() {
                                    @Override
                                    public void onSuccess(ImageIcon icon) {
                                        if (view != null) {
                                            view.setReportData(report.getTitle(), report.getDescription(), icon, report.getMonthlyTransactions(), report.getSelectedMonth());
                                            view.setLoadingState(false);
                                        }
                                    }
                                    @Override
                                    public void onError(String message) {
                                        if (view != null) {
                                            // Display report text even if image fails, but show error for image
                                            view.setReportData(report.getTitle(), report.getDescription(), null, report.getMonthlyTransactions(), report.getSelectedMonth());
                                            view.showError("Failed to load report image '" + reportImageFileName + "': " + message);
                                            view.setLoadingState(false);
                                        }
                                    }
                                });
                    } else {
                        // No image file name provided, set report data without an image
                        if (view != null) {
                            view.setReportData(report.getTitle(), report.getDescription(), null, report.getMonthlyTransactions(), report.getSelectedMonth());
                            view.setLoadingState(false);
                        }
                    }
                } catch (Exception e) { // Catch exceptions from get() or other issues in done()
                    System.err.println("HoroscopeController: Error retrieving/processing report: " + e.getMessage());
                    e.printStackTrace();
                    if (view != null) {
                        view.showError("Unexpected error generating Spending Star: " + e.getMessage());
                        // Create a fallback error report model
                        HoroscopeReportModel errorFallback = new HoroscopeReportModel(
                                "Error Generating Report",
                                "Could not retrieve your spending star at this time. Please check your transactions or try again later.",
                                HoroscopeService.NO_DATA_IMAGE_FILENAME, // Use the standard "no data" image
                                selectedMonth,
                                Collections.emptyList()
                        );
                        String errorImgPath = HOROSCOPE_IMAGE_BASE_PATH + errorFallback.getImageFileName();

                        // Attempt to load the fallback error image
                        imageLoader.loadImageFromFileSystem(errorImgPath,
                                FALLBACK_REPORT_IMAGE_WIDTH, // Use report fallback dimensions
                                FALLBACK_REPORT_IMAGE_HEIGHT,
                                new ImageLoader.Callback() {
                                    @Override public void onSuccess(ImageIcon icon) {
                                        if (view != null) view.setReportData(errorFallback.getTitle(), errorFallback.getDescription(), icon, errorFallback.getMonthlyTransactions(), errorFallback.getSelectedMonth());
                                    }
                                    @Override public void onError(String msg) { // If even fallback image fails
                                        if (view != null) view.setReportData(errorFallback.getTitle(), errorFallback.getDescription(), null, errorFallback.getMonthlyTransactions(), errorFallback.getSelectedMonth());
                                    }
                                });
                        view.setLoadingState(false);
                    }
                }
            }
        };
        worker.execute();
    }
}