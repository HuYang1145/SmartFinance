package View.LoginAndMain;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * A utility class providing custom Swing components with gradient effects and stylized borders.
 * Includes components such as gradient panels, buttons, borders, and a custom scrollbar style.
 *
 * @version 1.4
 * @author group19
 */
public class GradientComponents {

    /**
     * A custom border that renders a gradient outline with rounded corners around a component.
     * The gradient transitions from a light blue (#84ACC9) at the top-left to a light green (#A1DDA3) at the bottom-right.
     */
    public static class GradientBorder extends AbstractBorder {
        /** The thickness of the border in pixels. */
        private int thickness;

        /** The radius of the rounded corners in pixels. */
        private int radius;

        /**
         * Constructs a GradientBorder with the specified thickness and corner radius.
         *
         * @param thickness the thickness of the border in pixels
         * @param radius the radius of the rounded corners in pixels
         */
        public GradientBorder(int thickness, int radius) {
            this.thickness = thickness;
            this.radius = radius;
        }

        /**
         * Paints the border around the specified component using a gradient outline with rounded corners.
         * The gradient transitions from light blue (#84ACC9) to light green (#A1DDA3) with anti-aliasing enabled.
         *
         * @param c the component for which the border is being painted
         * @param g the Graphics object used for painting
         * @param x the x-coordinate of the border's top-left corner
         * @param y the y-coordinate of the border's top-left corner
         * @param w the width of the border
         * @param h the height of the border
         */
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Shape outer = new RoundRectangle2D.Float(
                    x + thickness / 2f, y + thickness / 2f,
                    w - thickness, h - thickness,
                    radius, radius
            );
            GradientPaint gp = new GradientPaint(
                    x, y, Color.decode("#84ACC9"),
                    x + w, y + h, Color.decode("#A1DDA3")
            );
            g2.setPaint(gp);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(outer);
        }

        /**
         * Returns the insets of the border, defining the space required for the border around the component.
         *
         * @param c the component for which the border is applied
         * @return an Insets object with equal insets on all sides, based on the border thickness
         */
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }

        /**
         * Stores the insets of the border in the specified Insets object.
         *
         * @param c the component for which the border is applied
         * @param insets the Insets object to be modified
         * @return the modified Insets object with equal insets on all sides, based on the border thickness
         */
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(thickness, thickness, thickness, thickness);
            return insets;
        }
    }

    /**
     * A custom JPanel that renders a gradient background.
     * The gradient transitions from light blue (#84ACC9) at the top-left to light green (#A1DDA3) at the bottom-right.
     */
    public static class GradientPanel extends JPanel {
        /**
         * Constructs a GradientPanel with an opaque background.
         */
        public GradientPanel() {
            setOpaque(true);
        }

        /**
         * Paints the panel with a gradient background.
         * The gradient transitions from light blue (#84ACC9) to light green (#A1DDA3).
         *
         * @param g the Graphics object used for painting
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth();
            int h = getHeight();
            GradientPaint gp = new GradientPaint(
                    0, 0, Color.decode("#84ACC9"),
                    w, h, Color.decode("#A1DDA3")
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
            g2.dispose();
        }
    }

    /**
     * A custom JButton that renders its text with a gradient effect.
     * The gradient text transitions from light blue (#84ACC9) to light green (#A1DDA3) horizontally,
     * with hover effects changing the background color.
     */
    public static class GradientTextButton extends JButton {
        /** The starting color of the text gradient (light blue, #84ACC9). */
        private Color colorStart = Color.decode("#84ACC9");

        /** The ending color of the text gradient (light green, #A1DDA3). */
        private Color colorEnd = Color.decode("#A1DDA3");

        /**
         * Constructs a GradientTextButton with the specified text.
         * The button has a white background, bold Segoe UI font, and hover effects.
         *
         * @param text the text to display on the button
         */
        public GradientTextButton(String text) {
            super(text);
            setFocusPainted(false);
            setContentAreaFilled(true);
            setOpaque(true);
            setBorderPainted(false);
            setBackground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(new Color(245, 245, 255));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(Color.WHITE);
                }
            });
        }

        /**
         * Paints the button with gradient text.
         * The text gradient transitions from light blue (#84ACC9) to light green (#A1DDA3) horizontally,
         * with anti-aliasing enabled for smooth rendering.
         *
         * @param g the Graphics object used for painting
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(getText());
            int x = (getWidth() - textWidth) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

            GradientPaint gp = new GradientPaint(x, 0, colorStart, x + textWidth, 0, colorEnd);
            g2.setPaint(gp);
            g2.setFont(getFont());
            g2.drawString(getText(), x, y);

            g2.dispose();
        }
    }

    /**
     * A custom JButton with a gradient background and rounded corners.
     * The gradient transitions from light blue (#84ACC9) to light green (#A1DDA3).
     */
    public static class GradientButton extends JButton {
        /** The starting color of the gradient (light blue, #84ACC9). */
        private final Color startColor = Color.decode("#84ACC9");

        /** The ending color of the gradient (light green, #A1DDA3). */
        private final Color endColor = Color.decode("#A1DDA3");

        /**
         * Constructs a GradientButton with the specified text.
         * The button has a gradient background, white text, and rounded corners.
         *
         * @param text the text to display on the button
         */
        public GradientButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        /**
         * Paints the button with a gradient background and rounded corners.
         * The gradient transitions from light blue (#84ACC9) to light green (#A1DDA3).
         *
         * @param g the Graphics object used for painting
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(
                    0, 0, startColor,
                    w, h, endColor
            );
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w, h, 20, 20);
            g2.dispose();

            super.paintComponent(g);
        }

        /**
         * Updates the UI, ensuring the button remains non-opaque and does not use default content filling.
         */
        @Override
        public void updateUI() {
            super.updateUI();
            setOpaque(false);
            setContentAreaFilled(false);
        }
    }

    /**
     * Styles a JScrollBar with a custom appearance.
     * Configures the scrollbar thumb to white and the track to orange, with no highlight painting for arrows.
     *
     * @param bar the JScrollBar to style
     */
    public static void styleScrollBar(JScrollBar bar) {
        bar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = Color.white;
                this.trackColor = Color.ORANGE;
            }

            @Override
            protected void paintDecreaseHighlight(Graphics g) { }

            @Override
            protected void paintIncreaseHighlight(Graphics g) { }
        });
    }

    /**
     * A custom JPanel with a deep gradient background.
     * The gradient transitions from light blue (#84ACC9) to light green (#A1DDA3).
     */
    public static class DeepBlueGradientPanel extends JPanel {
        /** The starting color of the gradient (light blue, #84ACC9). */
        private final Color startColor = Color.decode("#84ACC9");

        /** The ending color of the gradient (light green, #A1DDA3). */
        private final Color endColor = Color.decode("#A1DDA3");

        /**
         * Paints the panel with a gradient background.
         * The gradient transitions from light blue (#84ACC9) to light green (#A1DDA3) with anti-aliasing enabled.
         *
         * @param g the Graphics object used for painting
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, startColor, w, h, endColor);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
            g2.dispose();
        }
    }

    /**
     * A custom border that renders a shadow effect with rounded corners around a component.
     * The shadow fades outward with decreasing opacity, using a specified color and corner radius.
     */
    public static class ShadowBorder extends AbstractBorder {
        /** The size of the shadow in pixels. */
        private int shadowSize;

        /** The color of the shadow. */
        private Color shadowColor;

        /** The radius of the rounded corners in pixels. */
        private int arc;

        /**
         * Constructs a ShadowBorder with the specified shadow size, color, and corner radius.
         *
         * @param shadowSize the size of the shadow in pixels
         * @param shadowColor the color of the shadow
         * @param arc the radius of the rounded corners in pixels
         */
        public ShadowBorder(int shadowSize, Color shadowColor, int arc) {
            this.shadowSize = shadowSize;
            this.shadowColor = shadowColor;
            this.arc = arc;
        }

        /**
         * Paints the border with a shadow effect around the component.
         * The shadow is rendered with decreasing opacity outward, using rounded rectangles and anti-aliasing.
         *
         * @param c the component for which the border is being painted
         * @param g the Graphics object used for painting
         * @param x the x-coordinate of the border's top-left corner
         * @param y the y-coordinate of the border's top-left corner
         * @param w the width of the border
         * @param h the height of the border
         */
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            for (int i = 0; i < shadowSize; i++) {
                float alpha = (float) (shadowSize - i) / (shadowSize * 1.0f) * 0.5f;
                g2.setColor(new Color(shadowColor.getRed(),
                        shadowColor.getGreen(),
                        shadowColor.getBlue(),
                        (int) (alpha * 255)));
                RoundRectangle2D rect = new RoundRectangle2D.Double(
                        x + i, y + i,
                        w - 1 - i * 2, h - 1 - i * 2,
                        arc, arc
                );
                g2.draw(rect);
            }

            g2.dispose();
        }

        /**
         * Returns the insets of the border, defining the space required for the shadow around the component.
         *
         * @param c the component for which the border is applied
         * @return an Insets object with equal insets on all sides, based on the shadow size
         */
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(shadowSize, shadowSize, shadowSize, shadowSize);
        }

        /**
         * Stores the insets of the border in the specified Insets object.
         *
         * @param c the component for which the border is applied
         * @param insets the Insets object to be modified
         * @return the modified Insets object with equal insets on all sides, based on the shadow size
         */
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(shadowSize, shadowSize, shadowSize, shadowSize);
            return insets;
        }
    }
}