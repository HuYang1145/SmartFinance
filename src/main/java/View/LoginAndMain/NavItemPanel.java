package View.LoginAndMain;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import Controller.MainPanelController;

/**
 * A navigation item panel used in the sidebar of the Smart Finance Application.
 * Displays a clickable item with an icon and text, supporting selection states and gradient visual effects.
 * Handles navigation by switching content panels in either the login or main dashboard interfaces.
 * Provides public methods for external classes (like MainPlane) to control its appearance.
 *
 * @author Group 19
 * @version 1.0
 */
public class NavItemPanel extends JPanel {
    private JLabel iconLabel;
    private JLabel textLabel;
    private boolean selected = false;
    private Color start = new Color(0x84ACC9);
    private Color end = new Color(0xA1DDA3);
    private String name;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private List<NavItemPanel> navItems;
    private MainPanelController contentPanelManager;

    /**
     * A label with gradient text for displaying the application logo or navigation titles.
     */
    static class GradientLabel extends JLabel {
        private Color c1 = new Color(0x84ACC9);
        private Color c2 = new Color(0xA1DDA3);

        /**
         * Constructs a GradientLabel with the specified text.
         *
         * @param text the text to display
         */
        public GradientLabel(String text) {
            super(text);
            setOpaque(false);
        }

        /**
         * Paints the label with a horizontal gradient text effect.
         *
         * @param g the Graphics context
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            FontMetrics fm = g2.getFontMetrics(getFont());
            int textWidth = fm.stringWidth(getText());
            int textHeight = fm.getHeight();

            // Apply horizontal gradient
            GradientPaint gp = new GradientPaint(0, 0, c1, textWidth, 0, c2);
            g2.setPaint(gp);
            g2.setFont(getFont());

            // Text baseline
            int x = 0;
            int y = fm.getAscent();

            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    /**
     * A circular icon label with gradient fill and centered text, used for navigation item icons.
     */
    public static class CircleIcon extends JLabel {
        // Make colors public so NavItemPanel can modify them directly as needed for specific items
        public Color color1 = new Color(0x84ACC9); // Made public
        public Color color2 = new Color(0xA1DDA3); // Made public

        /**
         * Constructs a CircleIcon with the specified text.
         *
         * @param text the text to display in the center of the icon
         */
        public CircleIcon(String text) {
            super(text, SwingConstants.CENTER);
            setPreferredSize(new java.awt.Dimension(30, 30));
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(Color.WHITE); // Default text color for icon
            setOpaque(false);
        }

        /**
         * Paints the icon as a gradient-filled circle with centered text.
         *
         * @param g the Graphics context
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int size = Math.min(getWidth(), getHeight());
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            // Use the potentially modified colors for the gradient
            GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
            g2.setPaint(gp);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.fillOval(x, y, size, size);
            g2.dispose();
            super.paintComponent(g); // Paint the text
        }

        // Optional: Add public setters for colors if you prefer setters over public fields
        /*
        public void setColor1(Color color1) { this.color1 = color1; }
        public void setColor2(Color color2) { this.color2 = color2; }
        */
    }

    /**
     * A panel with a gradient background for branding purposes.
     */
    public static class BrandGradientPanel extends JPanel {
        /**
         * Paints the panel with a gradient background.
         *
         * @param g the Graphics context
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, new Color(106, 27, 154), w, 0, new Color(3, 169, 244));
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w, h, 16, 16);
        }
    }

    /**
     * Constructs a NavItemPanel for the main dashboard with a content panel manager.
     *
     * @param name                the name of the navigation item
     * @param navItems            the list of all navigation items for selection management
     * @param cardLayout          the card layout for switching content panels
     * @param contentPanel        the panel containing card content
     * @param contentPanelManager the controller for managing content panel transitions
     */
    public NavItemPanel(String name, List<NavItemPanel> navItems, CardLayout cardLayout, JPanel contentPanel, MainPanelController contentPanelManager) {
        this.name = name;
        this.navItems = navItems;
        this.cardLayout = cardLayout;
        this.contentPanel = contentPanel;
        this.contentPanelManager = contentPanelManager;
        initialize();
        // System.out.println("Creating NavItemPanel (with Manager): " + name + ", cardLayout=" + cardLayout + ", contentPanel=" + contentPanel + ", contentPanelManager=" + contentPanelManager); // Debug creation

        if (!name.equals("Logout") && !name.startsWith("Welcome, ") && (cardLayout == null || contentPanel == null || contentPanelManager == null)) {
             // Warning is valid for non-special items if manager is null
             if (!(name.equals("Login") || name.equals("Register") || name.equals("Personal Main") || name.equals("Account Management"))) { // Explicitly list Login panels
                System.err.println("Warning: cardLayout, contentPanel, or contentPanelManager is null for " + name);
             }
        }
    }

    /**
     * Constructs a NavItemPanel for the login interface without a content panel manager.
     * These panels ('Welcome', 'Login', 'Register', 'Personal Main', 'Account Management')
     * only need CardLayout and ContentPanel reference.
     *
     * @param name         the name of the navigation item
     * @param navItems     the list of all navigation items for selection management
     * @param cardLayout   the card layout for switching content panels
     * @param contentPanel the panel containing card content
     */
    public NavItemPanel(String name, List<NavItemPanel> navItems, CardLayout cardLayout, JPanel contentPanel) {
        this(name, navItems, cardLayout, contentPanel, null); // Pass null for contentPanelManager
        // System.out.println("Creating NavItemPanel (without Manager): " + name + ", cardLayout=" + cardLayout + ", contentPanel=" + contentPanel); // Debug creation
    }


    /**
     * Initializes the navigation item panel with icon, text, and mouse event handlers.
     */
    private void initialize() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(false);
        setPreferredSize(new Dimension(Short.MAX_VALUE, 60));
        Dimension fix = getPreferredSize();
        setMaximumSize(fix);
        setMinimumSize(fix);

        setBorder(new EmptyBorder(0, 12, 0, 12));

        // Create icon label - Use CircleIcon for special items, JLabel with char for others
        if ("Logout".equals(name) || name.startsWith("Welcome, ")) {
            iconLabel = new CircleIcon(name.startsWith("Welcome, ") ? "U" : "L"); // Use "U" for User/Welcome, "L" for Logout
        } else {
            iconLabel = new JLabel("\u25CF"); // Dot icon
            iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            iconLabel.setForeground(new Color(150, 150, 150)); // Default color for dot
        }

        textLabel = new JLabel(name);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        textLabel.setBorder(new EmptyBorder(0, 8, 0, 0));
        textLabel.setForeground(new Color(100, 100, 100)); // Default text color

        add(iconLabel);
        add(textLabel);
        add(Box.createHorizontalGlue()); // Push icon/text to the left


        // Add MouseListener for handling clicks and hover effects
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Ensure click is processed only if the panel is enabled
                if (!isEnabled()) return;

                System.out.println("NavItemPanel clicked: " + name);
                // If it's not the special "Logout" or "Welcome, " item (which might have custom click handlers)
                if (!name.equals("Logout") && !name.startsWith("Welcome, ")) {
                    // Logic to switch panels using CardLayout
                    if (contentPanelManager != null) {
                        // For MainPlane panels, use the manager to show the panel
                        System.out.println("Switching to panel (via Manager): " + name);
                        contentPanelManager.showPanel(name);
                    } else if (cardLayout != null && contentPanel != null) {
                        // For Login panels, use the direct CardLayout reference
                        System.out.println("Switching to panel (direct CardLayout): " + name);
                        cardLayout.show(contentPanel, name);
                    } else {
                        // Should not happen if setup is correct for non-special items
                        System.err.println("Cannot switch panel: Missing required references for " + name);
                    }
                    // Update selection state for all items in the list
                    navItems.forEach(it -> it.setSelected(it == NavItemPanel.this));
                }
                // Note: Custom click logic for "Logout" and potentially "Welcome, " needs to be added
                // in the class that creates/manages these NavItemPanels (e.g., MainPlane).
                // The MouseListener here only handles selection state and panel switching for
                // the standard navigation items.
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // Apply hover effect only if not currently selected
                if (!selected) {
                    setBackground(new Color(245, 245, 245)); // Light grey background on hover
                    // Optionally change text/icon color on hover
                    // textLabel.setForeground(new Color(50, 50, 50));
                    // if (!(iconLabel instanceof CircleIcon)) iconLabel.setForeground(new Color(100, 100, 100));
                    repaint(); // Request repaint to show background change
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Remove hover effect if not selected
                if (!selected) {
                    setBackground(null); // Reset background (makes it transparent if parent is opaque)
                    // Restore default text/icon color if hover changed them
                    // textLabel.setForeground(new Color(100, 100, 100));
                    // if (!(iconLabel instanceof CircleIcon)) iconLabel.setForeground(new Color(150, 150, 150));
                    repaint(); // Request repaint to show background change
                }
            }
        });

        setEnabled(true); // By default, the panel is enabled
        setFocusable(true); // Make it focusable for accessibility (optional)
    }

    /**
     * Gets the name of the navigation item.
     *
     * @return the name of the item
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the text to display in the icon label.
     * If the icon is a CircleIcon, sets its text. If it's a standard JLabel, sets its text and potentially updates font/color.
     * This method is intended for use by external classes to customize the icon's appearance.
     *
     * @param s the text to set for the icon. For CircleIcon, usually one or two characters.
     */
    public void setIconText(String s) {
        if (iconLabel instanceof CircleIcon) {
            ((CircleIcon) iconLabel).setText(s); // Set text in the CircleIcon JLabel
        } else {
             // For standard JLabel icons, set text and potentially update style
             iconLabel.setText(s);
             iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Match CircleIcon font style
             // iconLabel.setForeground(Color.WHITE); // Default color might be inappropriate for non-circle icons
             // You might want a different default color or make this customizable
        }
         // Ensure iconLabel is repainted after changing its text/style
         iconLabel.repaint();
         iconLabel.revalidate(); // Revalidate in case preferred size changed
    }

    /**
     * Sets the color of the text label.
     * This method is intended for use by external classes to customize the text color (e.g., for Logout).
     *
     * @param color The color to set for the text label.
     */
    public void setTextLabelColor(Color color) {
        if (textLabel != null) {
            textLabel.setForeground(color);
             textLabel.repaint(); // Repaint the text label
        }
    }

    /**
     * Sets the gradient colors for the CircleIcon.
     * This method is intended for use by external classes (like MainPlane) to customize CircleIcon appearance.
     *
     * @param color1 The start color for the gradient.
     * @param color2 The end color for the gradient.
     */
    public void setIconCircleColors(Color color1, Color color2) {
        if (iconLabel instanceof CircleIcon) {
            // Assuming color1 and color2 fields in CircleIcon are public OR have public setters
            ((CircleIcon) iconLabel).color1 = color1; // Direct access assumes public fields
            ((CircleIcon) iconLabel).color2 = color2;
            // If they were private with setters:
            // ((CircleIcon) iconLabel).setColor1(color1);
            // ((CircleIcon) iconLabel).setColor2(color2);

             iconLabel.repaint(); // Repaint the icon to reflect color change
        } else {
             System.err.println("setIconCircleColors called on a non-CircleIcon NavItemPanel.");
        }
    }


    /**
     * Sets the selected state of the navigation item, updating its appearance.
     * Handles background and text/icon color changes based on selection status.
     *
     * @param sel true to select the item, false to deselect
     */
    public void setSelected(boolean sel) {
        if (selected == sel) return; // No change needed

        selected = sel;
        if (sel) {
            // When selected, apply gradient background and set text/icon colors to white
            textLabel.setForeground(Color.WHITE);
            // Note: CircleIcon's text is already white by default.
            // For non-CircleIcon (standard JLabel), you might need to set its foreground too.
            // If iconLabel is not a CircleIcon, its default color is grey. Set it to white when selected.
            if (!(iconLabel instanceof CircleIcon)) {
                 iconLabel.setForeground(Color.WHITE);
            }
            // Background will be painted by paintComponent when selected = true
            setBackground(null); // Ensure JPanel's default background painting is off when using paintComponent
        } else {
            // When deselected, remove gradient background and restore default text/icon colors
            textLabel.setForeground(new Color(100, 100, 100)); // Default text color
            // Restore default icon color for non-CircleIcon
            if (!(iconLabel instanceof CircleIcon)) {
                 iconLabel.setForeground(new Color(150, 150, 150)); // Default dot color
            }
            // CircleIcon colors and text are handled by its own paint method and setIconText/setIconCircleColors
            setBackground(null); // Reset background to null to allow paintComponent to handle it or be transparent
        }
        repaint(); // Request repaint to show the change in selection state
    }

    /**
     * Paints the panel. Overridden to paint a gradient background when the item is selected.
     *
     * @param g the Graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        // Call super.paintComponent first to ensure proper JPanel painting (e.g., background if set)
        // However, since we are setting Opaque to false and handling background with paintComponent,
        // calling super.paintComponent is not strictly necessary for painting the background,
        // but it might be for other JPanel's default behaviors. Let's keep it.
         super.paintComponent(g);


        if (selected) {
            // Paint the gradient background only when selected
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Paint the primary gradient background
            GradientPaint gp = new GradientPaint(0, 0, start, getWidth(), 0, end);
            g2.setPaint(gp);
            // Using fillRect because NavItemPanel doesn't seem to have rounded corners itself, only the CircleIcon might.
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Paint the light overlay gradient (optional visual effect)
            g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 50), getWidth(), 0, new Color(255, 255, 255, 0)));
            // Apply the light overlay gradient to the full rectangle
            g2.fillRect(0, 0, getWidth(), getHeight());
            // Or apply it to the top half as in original code (looks like a subtle highlight)
            // g2.fillRect(0, 0, getWidth(), getHeight() / 2);


            g2.dispose();
        }
        // If not selected, the JPanel background might be painted by super.paintComponent
        // or it might be transparent if setBackground(null) is effective and parent is opaque.
    }

    /**
     * Repaints the panel and its icon and text labels.
     */
    @Override
    public void repaint() {
        // Request repaint for the panel itself
        super.repaint();
        // Also explicitly repaint the child labels to ensure they are redrawn on top
        if (textLabel != null) {
            textLabel.repaint();
        }
        if (iconLabel != null) {
            iconLabel.repaint();
        }
    }
}