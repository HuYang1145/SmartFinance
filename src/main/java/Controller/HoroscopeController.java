package Controller;

import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

import Model.HoroscopeReportModel;
import Service.HoroscopeService;
import Service.ImageLoader;
import View.HoroscopePanel.HoroscopePanel;

/**
 * Manages interactions between the horoscope panel and the horoscope service in the finance management
 * system. Handles loading welcome images, generating weekly horoscope reports, and navigating between
 * welcome and report views.
 *
 * @author Group 19
 * @version 1.0
 */
public class HoroscopeController {
    /** Username of the current user. */
    private final String username;
    /** Horoscope panel for displaying UI components. */
    private final HoroscopePanel view;
    /** Service for generating horoscope reports. */
    private final HoroscopeService horoscopeService;
    /** Utility for loading images. */
    private final ImageLoader imageLoader;
    /** Target width for loaded images. */
    private static final int IMAGE_TARGET_WIDTH = 600;
    /** Target height for loaded images. */
    private static final int IMAGE_TARGET_HEIGHT = 600;

    /**
     * Constructs a HoroscopeController with the specified username and view.
     * Initializes the horoscope service and image loader, and validates the username.
     *
     * @param username the username of the current user
     * @param view     the horoscope panel for UI interaction
     * @throws IllegalArgumentException if the username is null or empty
     */
    public HoroscopeController(String username, HoroscopePanel view) {
        this.username = username != null && !username.trim().isEmpty() ? username : Model.UserSession.getCurrentUsername();
        if (this.username == null || this.username.trim().isEmpty()) {
            view.showError("User context not found.");
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        this.view = view;
        this.horoscopeService = new HoroscopeService();
        this.imageLoader = new ImageLoader();
    }

    /**
     * Loads the welcome image asynchronously and updates the view.
     * Displays a loading state during image retrieval and handles errors if loading fails.
     */
    public void loadWelcomeImage() {
        view.setLoadingState(true);
        imageLoader.loadImage("icons/welcome.jpg", IMAGE_TARGET_WIDTH, IMAGE_TARGET_HEIGHT, new ImageLoader.Callback() {
            @Override
            public void onSuccess(ImageIcon icon) {
                view.setWelcomeImage(icon);
                view.setLoadingState(false);
            }

            @Override
            public void onError(String message) {
                view.setWelcomeImage(null);
                view.showError("Failed to load welcome image: " + message);
                view.setLoadingState(false);
            }
        });
    }

    /**
     * Initiates the loading of a weekly horoscope report when the reveal button is clicked.
     */
    public void onRevealClicked() {
        loadReport();
    }

    /**
     * Refreshes the weekly horoscope report when the refresh button is clicked.
     */
    public void onRefreshClicked() {
        loadReport();
    }

    /**
     * Navigates back to the welcome card and reloads the welcome image.
     */
    public void onBackClicked() {
        view.showWelcomeCard();
        loadWelcomeImage();
    }

    /**
     * Loads a weekly horoscope report asynchronously, including its image, and updates the view.
     * Displays a loading state during data retrieval and handles errors by showing a default error report.
     */
    private void loadReport() {
        view.setLoadingState(true);
        SwingWorker<HoroscopeReportModel, Void> worker = new SwingWorker<>() {
            @Override
            protected HoroscopeReportModel doInBackground() {
                return horoscopeService.generateWeeklyReport(username);
            }

            @Override
            protected void done() {
                try {
                    HoroscopeReportModel report = get();
                    // Ensure report is not null by using default error report
                    final HoroscopeReportModel finalReport = (report != null) ? report : horoscopeService.getDefaultErrorReport();
                    imageLoader.loadImage(finalReport.getImagePath(), IMAGE_TARGET_WIDTH, IMAGE_TARGET_HEIGHT, new ImageLoader.Callback() {
                        @Override
                        public void onSuccess(ImageIcon icon) {
                            view.setReportData(finalReport.getTitle(), finalReport.getDescription(), icon);
                            view.setLoadingState(false);
                        }

                        @Override
                        public void onError(String message) {
                            view.setReportData(finalReport.getTitle(), finalReport.getDescription(), null);
                            view.showError("Failed to load report image: " + message);
                            view.setLoadingState(false);
                        }
                    });
                } catch (Exception e) {
                    // Handle exception by using default error report
                    final HoroscopeReportModel errorReport = horoscopeService.getDefaultErrorReport();
                    imageLoader.loadImage(errorReport.getImagePath(), IMAGE_TARGET_WIDTH, IMAGE_TARGET_HEIGHT, new ImageLoader.Callback() {
                        @Override
                        public void onSuccess(ImageIcon icon) {
                            view.setReportData(errorReport.getTitle(), errorReport.getDescription(), icon);
                            view.setLoadingState(false);
                        }

                        @Override
                        public void onError(String message) {
                            view.setReportData(errorReport.getTitle(), errorReport.getDescription(), null);
                            view.showError("Failed to load report: " + e.getMessage());
                            view.setLoadingState(false);
                        }
                    });
                }
            }
        };
        worker.execute();
    }
}