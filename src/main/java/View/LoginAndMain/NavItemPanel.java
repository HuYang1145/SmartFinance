/**
 * A navigation item panel used in the sidebar of the Smart Finance Application.
 * Displays a clickable item with an icon and text, supporting selection states and gradient visual effects.
 * Handles navigation by switching content panels in either the login or main dashboard interfaces.
 *
 * @author Group 19
 * @version 1.0
 */
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
        private Color color1 = new Color(0x84ACC9);
        private Color color2 = new Color(0xA1DDA3);

        /**
         * Constructs a CircleIcon with the specified text.
         *
         * @param text the text to display in the center of the icon
         */
        public CircleIcon(String text) {
            super(text, SwingConstants.CENTER);
            setPreferredSize(new java.awt.Dimension(30, 30));
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(Color.WHITE);
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
            GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
            g2.setPaint(gp);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.fillOval(x, y, size, size);
            g2.dispose();
            super.paintComponent(g);
        }
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
        System.out.println("Creating NavItemPanel (with Manager): " + name + ", cardLayout=" + cardLayout + ", contentPanel=" + contentPanel + ", contentPanelManager=" + contentPanelManager);

        if (!name.equals("Logout") && !name.startsWith("Welcome, ") && (cardLayout == null || contentPanel == null || contentPanelManager == null)) {
            System.err.println("Warning: cardLayout, contentPanel, or contentPanelManager is null for " + name);
        }
    }

    /**
     * Constructs a NavItemPanel for the login interface without a content panel manager.
     *
     * @param name         the name of the navigation item
     * @param navItems     the list of all navigation items for selection management
     * @param cardLayout   the card layout for switching content panels
     * @param contentPanel the panel containing card content
     */
    public NavItemPanel(String name, List<NavItemPanel> navItems, CardLayout cardLayout, JPanel contentPanel) {
        this(name, navItems, cardLayout, contentPanel, null);
        System.out.println("Creating NavItemPanel (without Manager): " + name + ", cardLayout=" + cardLayout + ", contentPanel=" + contentPanel);
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

        if ("Logout".equals(name) || name.startsWith("Welcome, ")) {
            iconLabel = new CircleIcon("U");
        } else {
            iconLabel = new JLabel("\u25CF");
            iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            iconLabel.setForeground(new Color(150, 150, 150));
        }

        textLabel = new JLabel(name);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        textLabel.setBorder(new EmptyBorder(0, 8, 0, 0));
        textLabel.setForeground(new Color(100, 100, 100));

        add(iconLabel);
        add(textLabel);
        add(Box.createHorizontalGlue());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("NavItemPanel clicked: " + name + ", isEnabled: " + isEnabled() + ", isVisible: " + isVisible() + ", bounds: " + getBounds());
                if (!name.equals("Logout") && !name.startsWith("Welcome, ")) {
                    if (contentPanelManager != null) {
                        // MainPlane uses MainPanelController
                        System.out.println("Switching to panel (via Manager): " + name);
                        contentPanelManager.showPanel(name);
                    } else if (cardLayout != null && contentPanel != null) {
                        // Login uses cardLayout
                        System.out.println("Switching to panel (direct): " + name);
                        cardLayout.show(contentPanel, name);
                    } else {
                        System.err.println("Cannot switch panel: contentPanelManager=" + contentPanelManager + ", cardLayout=" + cardLayout + ", contentPanel=" + contentPanel);
                    }
                    navItems.forEach(it -> it.setSelected(it == NavItemPanel.this));
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                System.out.println("Mouse entered: " + name);
                if (!selected) {
                    setBackground(new Color(245, 245, 245));
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                System.out.println("Mouse exited: " + name);
                if (!selected) {
                    setBackground(null);
                    repaint();
                }
            }
        });

        setEnabled(true);
        setFocusable(true);
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
     *
     * @param s the text to set
     */
    public void setIconText(String s) {
        iconLabel.setText(s);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setOpaque(false);
    }

    /**
     * Sets the selected state of the navigation item, updating its appearance.
     *
     * @param sel true to select the item, false to deselect
     */
    public void setSelected(boolean sel) {
        selected = sel;
        if (sel) {
            textLabel.setForeground(Color.WHITE);
            iconLabel.setForeground(Color.WHITE);
        } else {
            textLabel.setForeground(new Color(100, 100, 100));
            iconLabel.setForeground(new Color(150, 150, 150));
            setBackground(null);
        }
        repaint();
    }

    /**
     * Paints the panel with a gradient background when selected.
     *
     * @param g the Graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        if (selected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, start, getWidth(), 0, end));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

            g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 50), getWidth(), 0, new Color(255, 255, 255, 0)));
            g2.fillRoundRect(0, 0, getWidth(), getHeight() / 2, 12, 12);

            g2.dispose();
        }
        super.paintComponent(g);
    }

    /**
     * Repaints the panel and its icon and text labels.
     */
    @Override
    public void repaint() {
        super.repaint();
        if (textLabel != null && iconLabel != null) {
            textLabel.repaint();
            iconLabel.repaint();
        }
    }
}