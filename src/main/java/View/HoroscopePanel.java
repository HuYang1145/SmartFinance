package View;

import PersonModel.HoroscopeReportModel;
import PersonModel.UserSessionModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import PersonController.SpendingHoroscopeServiceController;

import java.awt.*;
import java.io.File; // Needed for file system access
// No longer need java.net.URL directly if only using file path loading
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * HoroscopePanel.java
 *
 * Displays the "Spending Star Whispers" feature.
 * Uses separate JLabels for welcome and report images to avoid conflicts.
 * Images are scaled to fit available space while maintaining aspect ratio.
 * Includes a Back button on the report view.
 * Welcome image loading is delayed until the panel is added to the container.
 * Image loading uses SwingWorker.
 *
 * !! WARNING !! File system path loading is fragile. Consider classpath resources.
 */
public class HoroscopePanel extends JPanel {

    private final String username;
    private final SpendingHoroscopeServiceController horoscopeService;

    // UI Components
    private JLabel titleLabel;
    // *** MODIFICATION: Separate JLabels for images ***
    private JLabel welcomeImageLabel;
    private JLabel reportImageLabel;
    private JTextArea descriptionArea;
    private JButton revealButton;
    private JButton refreshButton;
    private JButton backButton;

    // Layout Panels
    private JPanel welcomePanel;
    private JPanel reportPanel;
    private CardLayout contentCardLayout;
    private JPanel contentCardPanel;

    private static final String WELCOME_CARD = "Welcome";
    private static final String REPORT_CARD = "Report";

    // File System Paths (Relative to project root - Fragile)
    private static final String IMAGE_BASE_PATH = "src/test/horoscope/";
    private static final String WELCOME_IMAGE_FILENAME = "welcome.jpg";
    private static final String ERROR_IMAGE_FILENAME = "error_monster.png";

    // Flag to prevent multiple initial loads of welcome image
    private boolean initialWelcomeImageLoaded = false;

    public HoroscopePanel(String username) {
        // Username validation and service initialization
        if (username == null || username.trim().isEmpty()) {
            String sessionUser = UserSessionModel.getCurrentUsername();
            if (sessionUser == null || sessionUser.trim().isEmpty()) {
                setLayout(new BorderLayout());
                add(new JLabel("Error: User context not found.", SwingConstants.CENTER), BorderLayout.CENTER);
                this.username = null;
                this.horoscopeService = null;
                return;
            }
             this.username = sessionUser;
        } else {
             this.username = username;
        }
        this.horoscopeService = new SpendingHoroscopeServiceController();

        // Setup Panel Layout and Appearance
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(250, 250, 255));
        setBorder(new EmptyBorder(5, 5, 5, 5));

        // Initialize UI Components
        initComponents();

        // Arrange Components using CardLayout
        layoutComponentsWithCardLayout();

        // Show the initial welcome view
        contentCardLayout.show(contentCardPanel, WELCOME_CARD);

        // Welcome image will be loaded in addNotify now.
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (!initialWelcomeImageLoaded) {
             Component visibleComponent = null;
             for (Component comp : contentCardPanel.getComponents()) {
                 if (comp.isVisible()) {
                     visibleComponent = comp;
                     break;
                 }
             }
             // Only load if the welcome panel is the one actually showing
             if (visibleComponent == welcomePanel) {
                  System.out.println("DEBUG: addNotify() - Triggering initial welcome image load.");
                  SwingUtilities.invokeLater(this::loadWelcomeImage); // Load welcome image on EDT
                  initialWelcomeImageLoaded = true;
             } else {
                 System.out.println("DEBUG: addNotify() - Welcome panel not visible, skipping initial image load for now.");
             }
        }
    }


    private void initComponents() {
        // --- Components for the REPORT view ---
        titleLabel = new JLabel(" ", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(80, 80, 150));
        titleLabel.setBorder(new EmptyBorder(5, 0, 10, 0));

        descriptionArea = new JTextArea(" ");
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        descriptionArea.setForeground(new Color(60, 60, 60));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setEditable(false);
        descriptionArea.setOpaque(false);
        descriptionArea.setBorder(new EmptyBorder(5, 5, 5, 5));

        refreshButton = new JButton("Check Stars Again");
        styleGradientButton(refreshButton, new Color(0x007BFF), new Color(0x0056b3));
        refreshButton.addActionListener(e -> loadHoroscopeData());

        backButton = new JButton("Back");
        styleGradientButton(backButton, new Color(0x6c757d), new Color(0x5a6268));
        backButton.addActionListener(e -> {
            contentCardLayout.show(contentCardPanel, WELCOME_CARD);
            // Reload welcome image if needed (optional, could rely on initial load)
            // If initial load might have failed due to timing, reloading here is safer.
            if (!initialWelcomeImageLoaded || welcomeImageLabel.getIcon() == null) {
                System.out.println("DEBUG: Back button - Reloading welcome image.");
                SwingUtilities.invokeLater(this::loadWelcomeImage);
            }
        });

        // --- MODIFICATION: Initialize separate Image Labels ---
        welcomeImageLabel = new JLabel();
        welcomeImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeImageLabel.setVerticalAlignment(SwingConstants.CENTER);

        reportImageLabel = new JLabel();
        reportImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        reportImageLabel.setVerticalAlignment(SwingConstants.CENTER);
        // ------------------------------------------------------

        // Reveal Button
        revealButton = new JButton("Reveal My Spending Star!");
        styleGradientButton(revealButton, new Color(0x9C27B0), new Color(0x002FA7));
        revealButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        revealButton.setForeground(Color.WHITE);
        revealButton.addActionListener(e -> {
            revealButton.setEnabled(false);
            revealButton.setText("Revealing...");
            loadHoroscopeData();
        });

        // --- CardLayout Panel ---
        contentCardLayout = new CardLayout();
        contentCardPanel = new JPanel(contentCardLayout);
        contentCardPanel.setOpaque(false);
    }

    private void styleGradientButton(JButton button, Color color1, Color color2) {
        // (Code unchanged)
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 20, 8, 20));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        Color base1=color1, base2=color2, hover1=color1.brighter(), hover2=color2.brighter();
        button.setBackground(new Color(0,0,0,0));
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = c.getWidth(), h = c.getHeight();
                GradientPaint gp = new GradientPaint(0, 0, button.getModel().isRollover() ? hover1 : base1, w, h, button.getModel().isRollover() ? hover2 : base2);
                g2.setPaint(gp); g2.fillRoundRect(0, 0, w, h, 18, 18);
                g2.dispose(); super.paint(g, c);
            }
        });
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.repaint(); }
            @Override public void mouseExited(MouseEvent e) { button.repaint(); }
        });
    }

    private void layoutComponentsWithCardLayout() {
        // --- 1. Create the Welcome Card Panel ---
        welcomePanel = new JPanel(new BorderLayout(10, 10));
        welcomePanel.setOpaque(false);

        JPanel imageContainerWelcome = new JPanel(new BorderLayout());
        imageContainerWelcome.setOpaque(false);
        imageContainerWelcome.setBorder(new EmptyBorder(5, 0, 0, 0));
        // *** MODIFICATION: Add welcomeImageLabel to welcome panel ***
        imageContainerWelcome.add(welcomeImageLabel, BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel(); // (controlsPanel setup unchanged)
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setOpaque(false);
        controlsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel welcomeText = new JLabel("Ready to see your Spending Star?", SwingConstants.CENTER);
        welcomeText.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        welcomeText.setForeground(Color.DARK_GRAY);
        welcomeText.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeText.setBorder(new EmptyBorder(5, 0, 10, 0));
        revealButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlsPanel.add(welcomeText);
        controlsPanel.add(revealButton);

        welcomePanel.add(imageContainerWelcome, BorderLayout.CENTER);
        welcomePanel.add(controlsPanel, BorderLayout.SOUTH);

        // --- 2. Create the Report Card Panel ---
        reportPanel = new JPanel(new GridBagLayout());
        reportPanel.setOpaque(false);
        GridBagConstraints reportGbc = new GridBagConstraints();

        // Report Title (Unchanged)
        reportGbc.gridx = 0; reportGbc.gridy = 0; reportGbc.gridwidth = 1;
        reportGbc.weightx = 1.0; reportGbc.weighty = 0.0;
        reportGbc.fill = GridBagConstraints.HORIZONTAL;
        reportGbc.anchor = GridBagConstraints.PAGE_START;
        reportGbc.insets = new Insets(5, 10, 0, 10);
        reportPanel.add(titleLabel, reportGbc);

        // *** MODIFICATION: Add reportImageLabel to report panel ***
        reportGbc.gridy = 1; reportGbc.weighty = 1.0; // Image gets vertical weight
        reportGbc.fill = GridBagConstraints.BOTH;
        reportGbc.anchor = GridBagConstraints.CENTER;
        reportGbc.insets = new Insets(5, 5, 5, 5);
        reportPanel.add(reportImageLabel, reportGbc);

        // Report Description (Unchanged)
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        descriptionScrollPane.setBorder(BorderFactory.createCompoundBorder(
             BorderFactory.createMatteBorder(1, 0, 1, 0, Color.LIGHT_GRAY),
             new EmptyBorder(5,5,5,5)));
        descriptionScrollPane.getViewport().setOpaque(false);
        descriptionScrollPane.setOpaque(false);
        descriptionScrollPane.setPreferredSize(new Dimension(100, 80));
        reportGbc.gridy = 2; reportGbc.weighty = 0.0;
        reportGbc.fill = GridBagConstraints.HORIZONTAL;
        reportGbc.anchor = GridBagConstraints.PAGE_END;
        reportGbc.insets = new Insets(0, 10, 5, 10);
        reportPanel.add(descriptionScrollPane, reportGbc);

        // Report Buttons (Unchanged)
        JPanel reportButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        reportButtonPanel.setOpaque(false);
        reportButtonPanel.add(refreshButton);
        reportButtonPanel.add(backButton);
        reportGbc.gridy = 3; reportGbc.weighty = 0.0;
        reportGbc.fill = GridBagConstraints.HORIZONTAL;
        reportGbc.anchor = GridBagConstraints.PAGE_END;
        reportGbc.insets = new Insets(5, 10, 5, 10);
        reportPanel.add(reportButtonPanel, reportGbc);

        // --- 3. Add Cards to the CardLayout Panel ---
        contentCardPanel.add(welcomePanel, WELCOME_CARD);
        contentCardPanel.add(reportPanel, REPORT_CARD);

        // --- 4. Add the Card Panel to the Main Layout ---
        add(contentCardPanel, BorderLayout.CENTER);
    }

    /**
     * Loads the initial welcome image. Targets welcomeImageLabel.
     */
    private void loadWelcomeImage() {
        System.out.println("DEBUG: Loading welcome image file: " + WELCOME_IMAGE_FILENAME + " for welcomeImageLabel");
        // *** MODIFICATION: Pass welcomeImageLabel as the target ***
        loadAndScaleImageFromFile(WELCOME_IMAGE_FILENAME, "Welcome Image Missing", welcomeImageLabel);
        initialWelcomeImageLoaded = true; // Mark attempt even if it fails
    }

    /**
     * Helper method to load and scale an image from the FILE SYSTEM path.
     * Updates the specified target JLabel. Uses SwingWorker.
     *
     * @param imageFileName The simple filename (e.g., "welcome.jpg").
     * @param errorText     Text to display if loading fails.
     * @param targetLabel   The JLabel to update with the loaded image.  <- *** NEW PARAMETER ***
     */
    private void loadAndScaleImageFromFile(String imageFileName, String errorText, JLabel targetLabel) {
        // --- File Path Construction and Existence Check --- (Unchanged)
        if (imageFileName == null || imageFileName.trim().isEmpty()) {
            // *** MODIFICATION: Pass targetLabel to error display ***
            displayImageError(errorText + " (No filename)", targetLabel);
            return;
        }
        String fullPath = IMAGE_BASE_PATH + imageFileName;
        File imageFile = new File(fullPath);
        System.out.println("DEBUG: Attempting to load image from file system: " + imageFile.getAbsolutePath() + " for target label: " + targetLabel.hashCode());
        if (!imageFile.exists() || !imageFile.isFile()) {
            File altFile = new File("../" + fullPath);
            System.out.println("DEBUG: Trying alternative path: " + altFile.getAbsolutePath());
            if (!altFile.exists() || !altFile.isFile()) {
                 // *** MODIFICATION: Pass targetLabel to error display ***
                displayImageError(errorText + " (File Not Found at '" + fullPath + "' or alt path)", targetLabel);
                return;
            } else {
                imageFile = altFile;
                System.out.println("DEBUG: Using alternative path.");
            }
        }
        // --- End File Path Logic ---

        // Create final references for use in worker/lambda
        final File finalImageFile = imageFile;
        final String finalErrorText = errorText;
        // *** MODIFICATION: Make targetLabel effectively final for worker ***
        final JLabel finalTargetLabel = targetLabel;

        // Use SwingWorker for image loading and scaling
        SwingWorker<ImageIcon, Void> imageLoader = new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                ImageIcon originalIcon = new ImageIcon(finalImageFile.getAbsolutePath());
                Image originalImage = originalIcon.getImage();

                if (originalIcon.getImageLoadStatus() != MediaTracker.COMPLETE || originalIcon.getIconWidth() <= 0) {
                     throw new Exception("Image status not complete or width<=0. Status: " + originalIcon.getImageLoadStatus() + " for " + finalImageFile.getName());
                }
                System.out.println("DEBUG (Worker): ImageIcon status OK for " + finalImageFile.getName());

                // --- Aspect Ratio Scaling Logic ---
                // *** MODIFICATION: Use finalTargetLabel for size calculation ***
                int targetW = finalTargetLabel.getWidth();
                int targetH = finalTargetLabel.getHeight();

                if (targetW <= 0 || targetH <= 0) {
                    // *** MODIFICATION: Use finalTargetLabel's parent ***
                    Container parent = finalTargetLabel.getParent();
                    if (parent != null && parent.getWidth() > 0 && parent.getHeight() > 0) {
                        Insets insets = parent.getInsets();
                        targetW = parent.getWidth() - insets.left - insets.right - 10;
                        targetH = parent.getHeight() - insets.top - insets.bottom - 10;
                        System.out.println("DEBUG (Worker): Using parent container size for scaling target: " + targetW + "x" + targetH);
                    } else {
                        targetW = 600; targetH = 600;
                         System.out.println("WARN (Worker): Could not get valid component/parent size, using fallback target: " + targetW + "x" + targetH);
                    }
                }
                 targetW = Math.max(1, targetW);
                 targetH = Math.max(1, targetH);

                int originalW = originalIcon.getIconWidth();
                int originalH = originalIcon.getIconHeight();
                Image scaledImage;

                if (originalW <= 0 || originalH <= 0) { //(Scaling logic unchanged)
                    System.err.println("WARN (Worker): Original image dimensions invalid for " + finalImageFile.getName() + ". Cannot scale.");
                    scaledImage = originalImage;
                } else {
                    double imgAspect = (double) originalW / originalH;
                    double targetAspect = (double) targetW / targetH;
                    int newW, newH;
                    if (imgAspect > targetAspect) { newW = targetW; newH = (int) (newW / imgAspect); }
                    else { newH = targetH; newW = (int) (newH * imgAspect); }
                    newW = Math.max(1, newW); newH = Math.max(1, newH);
                    System.out.println("DEBUG (Worker): Scaling " + finalImageFile.getName() + " from " + originalW + "x" + originalH + " to " + newW + "x" + newH + " (Target area: " + targetW + "x" + targetH +")");
                    scaledImage = originalImage.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                }
                // --- End Scaling Logic ---

                ImageIcon scaledIcon = new ImageIcon(scaledImage);
                // Ensure scaled image is loaded (optional check, getScaledInstance is usually synchronous enough for this)
                while (scaledIcon.getImageLoadStatus() == MediaTracker.LOADING) { Thread.sleep(50); }
                if (scaledIcon.getImageLoadStatus() != MediaTracker.COMPLETE) {
                     throw new Exception("Scaled image failed to load. Status: " + scaledIcon.getImageLoadStatus());
                }
                return scaledIcon;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon scaledIcon = get(); // Get result from doInBackground

                    // *** MODIFICATION: Update finalTargetLabel on EDT ***
                    finalTargetLabel.setIcon(scaledIcon);
                    finalTargetLabel.setText(null); // Clear any error text

                    // Debugging and Revalidation
                    System.out.println("DEBUG (EDT): Icon set for " + finalImageFile.getName() + ". Target Label(" + finalTargetLabel.hashCode() + ") current size: " + finalTargetLabel.getSize());
                    Container parent = finalTargetLabel.getParent();
                    if (parent != null) {
                        System.out.println("DEBUG (EDT): Parent container size: " + parent.getSize());
                        parent.revalidate(); // Force parent revalidation
                        parent.repaint();
                    } else {
                        System.out.println("DEBUG (EDT): Target label has no parent.");
                        finalTargetLabel.revalidate(); // Revalidate label itself
                        finalTargetLabel.repaint();
                    }

                    System.out.println("DEBUG (EDT): Image scaled and UI updated successfully for: " + finalImageFile.getName() + " on label " + finalTargetLabel.hashCode());

                } catch (Exception e) {
                     System.err.println("ERROR (EDT): Failed to load/scale image in worker for label " + finalTargetLabel.hashCode() + ": " + e.getMessage());
                     e.printStackTrace();
                      // *** MODIFICATION: Pass finalTargetLabel to error display ***
                     displayImageError(finalErrorText + " (Load/Scale Error)", finalTargetLabel);
                }
            }
        };
        imageLoader.execute(); // Start the worker
    }


    /**
     * Displays an error state on the specified target JLabel. Ensures update is on EDT.
     * @param text Text to display.
     * @param targetLabel The JLabel to update. <- *** NEW PARAMETER ***
     */
     private void displayImageError(String text, JLabel targetLabel) {
         // *** MODIFICATION: Make targetLabel effectively final for lambda ***
         final JLabel finalTargetLabel = targetLabel;
         // Ensure UI update is on EDT
         if (!SwingUtilities.isEventDispatchThread()) {
              SwingUtilities.invokeLater(() -> displayImageError(text, finalTargetLabel));
              return;
         }
         // *** MODIFICATION: Update finalTargetLabel ***
         finalTargetLabel.setIcon(null);
         finalTargetLabel.setText("<html><center style='color:red; padding: 20px;'>" + text + "<br><br>(Check path/file: " + IMAGE_BASE_PATH + ")</center></html>");
         finalTargetLabel.setForeground(Color.RED);
         finalTargetLabel.setOpaque(true);
         finalTargetLabel.setBackground(new Color(255, 220, 220));
         finalTargetLabel.revalidate();
         finalTargetLabel.repaint();
         System.err.println("DISPLAYING IMAGE ERROR on label " + finalTargetLabel.hashCode() + ": " + text);
     }

    // showLoadingState remains unchanged as it affects buttons, not image labels directly
    private void showLoadingState(boolean isLoading) {
        if (!SwingUtilities.isEventDispatchThread()) {
             SwingUtilities.invokeLater(() -> showLoadingState(isLoading));
             return;
        }
        revealButton.setEnabled(!isLoading);
        refreshButton.setEnabled(!isLoading);
        backButton.setEnabled(!isLoading);
        if(isLoading) {
            Component visibleComponent = null;
             for (Component comp : contentCardPanel.getComponents()) {
                 if (comp.isVisible()) { visibleComponent = comp; break; }
             }
             if(visibleComponent == welcomePanel) { revealButton.setText("Consulting Stars..."); }
        } else {
             revealButton.setText("Reveal My Spending Star!");
        }
    }

    // loadHoroscopeData remains largely unchanged, but updateUI call is the key
    private void loadHoroscopeData() {
        if (horoscopeService == null) { /* ... error handling ... */ return; }
        showLoadingState(true);
        SwingWorker<HoroscopeReportModel, Void> worker = new SwingWorker<HoroscopeReportModel, Void>() {
            @Override protected HoroscopeReportModel doInBackground() throws Exception {
                System.out.println("DEBUG: HoroscopePanel (Worker) - Calling generateWeeklyReport for " + username);
                return horoscopeService.generateWeeklyReport(username);
            }
            @Override protected void done() {
                HoroscopeReportModel report = null;
                try {
                    report = get();
                    if (report == null) report = horoscopeService.getDefaultErrorReport();
                    System.out.println("DEBUG: HoroscopePanel (Worker) - Report received: " + report);
                    // *** updateUI will now handle loading into reportImageLabel ***
                    updateUI(report);
                    contentCardLayout.show(contentCardPanel, REPORT_CARD); // Switch card AFTER starting UI update
                } catch (Exception e) {
                    System.err.println("ERROR: HoroscopePanel (Worker) - Failed to get report: " + e.getMessage());
                    e.printStackTrace(); report = horoscopeService.getDefaultErrorReport();
                    updateUI(report); contentCardLayout.show(contentCardPanel, REPORT_CARD);
                } finally { showLoadingState(false); }
            }
        };
        worker.execute();
    }


    /**
     * Updates the REPORT card UI. Calls image loading targeting reportImageLabel.
     * Ensures runs on EDT.
     */
    private void updateUI(HoroscopeReportModel report) {
        final HoroscopeReportModel finalReport = (report != null) ? report :
             ((horoscopeService != null) ? horoscopeService.getDefaultErrorReport() :
              new HoroscopeReportModel("Error", "Failed to load report.", ERROR_IMAGE_FILENAME));

        if (!SwingUtilities.isEventDispatchThread()) {
             SwingUtilities.invokeLater(() -> updateUI(finalReport));
             return;
        }

        // --- Now on EDT ---
        titleLabel.setText(finalReport.getTitle());
        descriptionArea.setText(finalReport.getDescription());
        descriptionArea.setCaretPosition(0);

        // Load image for the REPORT view
        String imageFileName = finalReport.getImagePath();
        if (imageFileName == null || imageFileName.isEmpty()) {
             imageFileName = ERROR_IMAGE_FILENAME;
             System.err.println("WARN: Report image path null/empty, using error image.");
        }
        System.out.println("DEBUG: HoroscopePanel (updateUI on EDT) - Requesting update for report image: " + imageFileName + " on reportImageLabel");
        // *** MODIFICATION: Pass reportImageLabel as the target ***
        loadAndScaleImageFromFile(imageFileName, "Report Image Error", reportImageLabel);
    }
}