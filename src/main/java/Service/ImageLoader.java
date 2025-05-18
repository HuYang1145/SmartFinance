package Service;

import java.awt.Image;
import java.io.File; // Added
import javax.imageio.ImageIO; // Added for reading from File
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
// No need for java.net.URL if only loading from file system for this method

/**
 * Utility class for loading images asynchronously.
 * Supports loading from classpath resources and the file system.
 */
public class ImageLoader {

    /**
     * Callback interface for image loading operations.
     * Provides methods to handle success or failure.
     */
    public interface Callback {
        /**
         * Called when the image is successfully loaded and scaled.
         * @param icon The loaded and scaled ImageIcon.
         */
        void onSuccess(ImageIcon icon);

        /**
         * Called when an error occurs during image loading.
         * @param message A descriptive error message.
         */
        void onError(String message);
    }

    /**
     * Loads an image from the classpath resources asynchronously.
     * This method is suitable for images bundled with the application.
     *
     * @param resourcePath Path to the image resource within the classpath (e.g., "images/myimage.png").
     * The path is relative to the root of the classpath.
     * @param targetWidth  The desired width to scale the image to. Must be positive.
     * @param targetHeight The desired height to scale the image to. Must be positive.
     * @param callback     The callback to be invoked with the loaded image or an error message.
     * It will be called on the Swing Event Dispatch Thread.
     * @throws IllegalArgumentException if resourcePath is null or empty, or if targetWidth/Height are non-positive (via callback).
     */
    public void loadImage(String resourcePath, int targetWidth, int targetHeight, Callback callback) {
        if (resourcePath == null || resourcePath.trim().isEmpty()) {
            SwingUtilities.invokeLater(() -> callback.onError("Resource path cannot be null or empty."));
            return;
        }
        if (targetWidth <= 0 || targetHeight <= 0) {
            SwingUtilities.invokeLater(() -> callback.onError("Target width and height must be positive."));
            return;
        }

        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                java.net.URL url = ImageLoader.class.getClassLoader().getResource(resourcePath);
                if (url == null) {
                    throw new IllegalArgumentException("Resource not found: " + resourcePath);
                }
                ImageIcon originalIcon = new ImageIcon(url);
                if (originalIcon.getImageLoadStatus() != java.awt.MediaTracker.COMPLETE) {
                    throw new RuntimeException("Failed to load image completely from resource: " + resourcePath);
                }
                Image originalImage = originalIcon.getImage();
                int originalW = originalIcon.getIconWidth();
                int originalH = originalIcon.getIconHeight();
                if (originalW <= 0 || originalH <= 0) {
                    throw new IllegalStateException("Invalid image dimensions (non-positive) from resource: " + resourcePath);
                }
                // Scaling logic
                double imgAspect = (double) originalW / originalH;
                double targetAspect = (double) targetWidth / targetHeight;
                int newW, newH;
                if (imgAspect > targetAspect) {
                    newW = targetWidth;
                    newH = (int) (newW / imgAspect);
                } else {
                    newH = targetHeight;
                    newW = (int) (newH * imgAspect);
                }
                if (newW < 1) newW = 1; // Ensure minimum dimensions
                if (newH < 1) newH = 1; // Ensure minimum dimensions
                Image scaledImage = originalImage.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }

            @Override
            protected void done() {
                SwingUtilities.invokeLater(() -> {
                    try {
                        ImageIcon scaledIcon = get();
                        callback.onSuccess(scaledIcon);
                    } catch (Exception e) {
                        System.err.println("Error loading image from resource in SwingWorker for " + resourcePath + ": " + e.getMessage());
                        callback.onError("Failed to load image '" + resourcePath + "': " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
                    }
                });
            }
        };
        worker.execute();
    }

    /**
     * Loads an image from the file system asynchronously.
     * This method is suitable for images stored externally to the application.
     *
     * @param filePath     The absolute or relative path to the image file on the file system.
     * If relative, it's resolved against the application's working directory.
     * @param targetWidth  The target width for the scaled image. Must be positive.
     * @param targetHeight The target height for the scaled image. Must be positive.
     * @param callback     The callback to be invoked with the loaded image or an error message.
     * It will be called on the Swing Event Dispatch Thread.
     * @throws IllegalArgumentException if filePath is null or empty, or if targetWidth/Height are non-positive (via callback).
     */
    public void loadImageFromFileSystem(String filePath, int targetWidth, int targetHeight, Callback callback) {
        if (filePath == null || filePath.trim().isEmpty()) {
            SwingUtilities.invokeLater(() -> callback.onError("File path cannot be null or empty."));
            return;
        }
        if (targetWidth <= 0 || targetHeight <= 0) {
            SwingUtilities.invokeLater(() -> callback.onError("Target width and height must be positive."));
            return;
        }

        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                File imageFile = new File(filePath);
                if (!imageFile.exists() || !imageFile.isFile()) {
                    throw new IllegalArgumentException("Image file not found or is not a file: " + imageFile.getAbsolutePath());
                }

                Image originalImage = ImageIO.read(imageFile);
                if (originalImage == null) {
                    throw new RuntimeException("Failed to read image file (unsupported format or corrupted): " + imageFile.getAbsolutePath());
                }

                int originalW = originalImage.getWidth(null);
                int originalH = originalImage.getHeight(null);

                if (originalW <= 0 || originalH <= 0) {
                    throw new IllegalStateException("Invalid image dimensions (non-positive) from file: " + filePath);
                }

                // Scaling logic
                double imgAspect = (double) originalW / originalH;
                double targetAspect = (double) targetWidth / targetHeight;
                int newW, newH;

                if (imgAspect > targetAspect) {
                    newW = targetWidth;
                    newH = (int) (newW / imgAspect);
                } else {
                    newH = targetHeight;
                    newW = (int) (newH * imgAspect);
                }
                if (newW < 1) newW = 1; // Ensure minimum dimensions
                if (newH < 1) newH = 1; // Ensure minimum dimensions

                Image scaledImage = originalImage.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }

            @Override
            protected void done() {
                SwingUtilities.invokeLater(() -> {
                    try {
                        ImageIcon scaledIcon = get();
                        callback.onSuccess(scaledIcon);
                    } catch (Exception e) {
                        System.err.println("Error loading image from file system in SwingWorker for " + filePath + ": " + e.getMessage());
                        String errorMessage = "Failed to load image '" + new File(filePath).getName() + "'.";
                        // Provide more specific error cause if available
                        if (e.getCause() instanceof IllegalArgumentException || e.getCause() instanceof RuntimeException) {
                            errorMessage += " Reason: " + e.getCause().getMessage();
                        } else if (e.getCause() != null) {
                            errorMessage += " Reason: " + e.getCause().getClass().getSimpleName();
                        } else {
                            // Fallback to the main exception message if no specific cause
                            errorMessage += " Reason: " + e.getMessage();
                        }
                        callback.onError(errorMessage);
                    }
                });
            }
        };
        worker.execute();
    }
}