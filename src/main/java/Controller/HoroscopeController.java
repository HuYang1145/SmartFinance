package Controller;

import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

import Model.HoroscopeReportModel;
import Service.HoroscopeService;
import Service.ImageLoader;
import View.HoroscopePanel.HoroscopePanel;

public class HoroscopeController {
    private final String username;
    private final HoroscopePanel view;
    private final HoroscopeService horoscopeService;
    private final ImageLoader imageLoader;
    private static final int IMAGE_TARGET_WIDTH = 600;
    private static final int IMAGE_TARGET_HEIGHT = 600;

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

    public void onRevealClicked() {
        loadReport();
    }

    public void onRefreshClicked() {
        loadReport();
    }

    public void onBackClicked() {
        view.showWelcomeCard();
        loadWelcomeImage();
    }

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