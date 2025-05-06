package Service;

import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

public class ImageLoader {
    public interface Callback {
        void onSuccess(ImageIcon icon);
        void onError(String message);
    }

    public void loadImage(String resourcePath, int targetWidth, int targetHeight, Callback callback) {
        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() {
                URL url = getClass().getClassLoader().getResource(resourcePath);
                if (url == null) {
                    throw new IllegalArgumentException("Resource not found: " + resourcePath);
                }
                ImageIcon icon = new ImageIcon(url);
                Image image = icon.getImage();
                int originalW = icon.getIconWidth();
                int originalH = icon.getIconHeight();
                if (originalW <= 0 || originalH <= 0) {
                    throw new IllegalStateException("Invalid image dimensions: " + resourcePath);
                }
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
                Image scaledImage = image.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }

            @Override
            protected void done() {
                try {
                    callback.onSuccess(get());
                } catch (Exception e) {
                    callback.onError("Failed to load image: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
}