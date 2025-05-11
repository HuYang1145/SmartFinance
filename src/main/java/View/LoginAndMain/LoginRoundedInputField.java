
package View.LoginAndMain;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxUI;

/**
 * Provides custom rounded input fields and combo boxes for a login interface.
 * Includes text fields, password fields, and combo boxes with rounded borders and placeholder text.
 * 
 * @author Group 19
 * @version 1.0
 */
public class LoginRoundedInputField {

    /**
     * A custom JTextField with rounded corners and placeholder text functionality.
     */
    public static class RoundedTextField extends JTextField {
        private String placeholder;
        private boolean showingPlaceholder = true;

        /**
         * Constructs a RoundedTextField with the specified placeholder text.
         * 
         * @param placeholder the placeholder text to display when the field is empty
         */
        public RoundedTextField(String placeholder) {
            this.placeholder = placeholder;
            setFont(new Font("Segoe UI", Font.PLAIN, 18));
            setForeground(Color.GRAY);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            setPreferredSize(new Dimension(240, 50));
            setText(placeholder);

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (showingPlaceholder) {
                        setText("");
                        setForeground(Color.DARK_GRAY);
                        showingPlaceholder = false;
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (getText().trim().isEmpty()) {
                        setText(placeholder);
                        setForeground(Color.GRAY);
                        showingPlaceholder = true;
                    }
                }
            });
        }

        /**
         * Paints the component with a rounded white background and a gray border.
         * 
         * @param g the Graphics object to paint with
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int arc = 30;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setColor(new Color(200, 200, 200));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }

        /**
         * Returns the actual text entered by the user, excluding the placeholder.
         * 
         * @return the text entered, or an empty string if the placeholder is showing
         */
        public String getActualText() {
            return showingPlaceholder ? "" : getText();
        }
    }

    /**
     * A custom JPasswordField with rounded corners and placeholder text functionality.
     */
    public static class RoundedPasswordField extends JPasswordField {
        private String placeholder;
        private boolean showingPlaceholder = true;

        /**
         * Constructs a RoundedPasswordField with the specified placeholder text.
         * 
         * @param placeholder the placeholder text to display when the field is empty
         */
        public RoundedPasswordField(String placeholder) {
            this.placeholder = placeholder;
            setFont(new Font("Segoe UI", Font.PLAIN, 18));
            setForeground(Color.GRAY);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            setPreferredSize(new Dimension(240, 50));
            setEchoChar((char) 0);
            setText(placeholder);

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (showingPlaceholder) {
                        setText("");
                        setForeground(Color.DARK_GRAY);
                        setEchoChar('●');
                        showingPlaceholder = false;
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (getPassword().length == 0) {
                        setText(placeholder);
                        setForeground(Color.GRAY);
                        setEchoChar((char) 0);
                        showingPlaceholder = true;
                    }
                }
            });
        }

        /**
         * Paints the component with a rounded white background and a gray border.
         * 
         * @param g the Graphics object to paint with
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int arc = 30;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setColor(new Color(200, 200, 200));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }

        /**
         * Returns the actual password entered by the user, excluding the placeholder.
         * 
         * @return the password entered, or an empty string if the placeholder is showing
         */
        public String getActualPassword() {
            return showingPlaceholder ? "" : new String(getPassword());
        }

        /**
         * Checks if the placeholder text is currently displayed.
         * 
         * @return true if the placeholder is showing, false otherwise
         */
        public boolean isPlaceholderShowing() {
            return showingPlaceholder;
        }
    }

    /**
     * A custom border with rounded corners for Swing components.
     */
    public static class RoundedBorder implements Border {
        private int radius;
        private Color borderColor;

        /**
         * Constructs a RoundedBorder with the specified radius and color.
         * 
         * @param radius the radius of the rounded corners
         * @param borderColor the color of the border
         */
        public RoundedBorder(int radius, Color borderColor) {
            this.radius = radius;
            this.borderColor = borderColor;
        }

        /**
         * Paints the border with rounded corners.
         * 
         * @param c the component to paint the border for
         * @param g the Graphics object to paint with
         * @param x the x-coordinate of the border
         * @param y the y-coordinate of the border
         * @param width the width of the border
         * @param height the height of the border
         */
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(borderColor);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }

        /**
         * Returns the insets of the border.
         * 
         * @param c the component to get insets for
         * @return the insets of the border
         */
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        /**
         * Checks if the border is opaque.
         * 
         * @return false, as the border is not opaque
         */
        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

    /**
     * A custom border with rounded corners and customizable stroke.
     */
    public static class RoundBorder implements Border {
        private int radius;
        private Color color;

        /**
         * Constructs a RoundBorder with the specified radius and color.
         * 
         * @param radius the radius of the rounded corners
         * @param color the color of the border
         */
        public RoundBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        /**
         * Returns the insets of the border.
         * 
         * @param c the component to get insets for
         * @return the insets of the border
         */
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        /**
         * Checks if the border is opaque.
         * 
         * @return false, as the border is not opaque
         */
        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        /**
         * Paints the border with rounded corners and a specified stroke.
         * 
         * @param c the component to paint the border for
         * @param g the Graphics object to paint with
         * @param x the x-coordinate of the border
         * @param y the y-coordinate of the border
         * @param w the width of the border
         * @param h the height of the border
         */
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D round = new RoundRectangle2D.Float(x + 1, y + 1, w - 2, h - 2, radius, radius);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2));
            g2.draw(round);
            g2.dispose();
        }
    }

    /**
     * A custom JComboBox with rounded corners and a gradient border.
     */
    public static class RoundedComboBox<E> extends JComboBox<E> {
        private Color start = new Color(156, 39, 176);
        private Color end = new Color(0, 47, 167);

        /**
         * Constructs a RoundedComboBox with the specified items.
         * 
         * @param items the items to display in the combo box
         */
        public RoundedComboBox(E[] items) {
            super(items);
            setFont(new Font("Segoe UI", Font.PLAIN, 18));
            setOpaque(false);
            setPreferredSize(new Dimension(240, 50));
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        }

        /**
         * Updates the UI to include a custom arrow button with transparency.
         */
        @Override
        public void updateUI() {
            super.updateUI();
            setUI(new BasicComboBoxUI() {
                /**
                 * Creates a transparent arrow button for the combo box.
                 * 
                 * @return the custom arrow button
                 */
                @Override
                protected JButton createArrowButton() {
                    JButton btn = new JButton("\u25BE");  // ▼
                    btn.setOpaque(false);
                    btn.setContentAreaFilled(false);
                    btn.setBorder(null);
                    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    return btn;
                }

                /**
                 * Installs the UI, ensuring transparency for the combo box and arrow button.
                 * 
                 * @param c the component to install the UI for
                 */
                @Override
                public void installUI(JComponent c) {
                    super.installUI(c);
                    comboBox.setOpaque(false);
                    arrowButton.setOpaque(false);
                    arrowButton.setBackground(new Color(0, 0, 0, 0));
                }
            });
        }

        /**
         * Paints the component with a rounded white background and a gradient border.
         * 
         * @param g the Graphics object to paint with
         */
        @Override
        protected void paintComponent(Graphics g) {
            int w = getWidth(), h = getHeight(), arc = 30;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            GradientPaint gp = new GradientPaint(0, 0, start, w, 0, end);
            g2.setPaint(gp);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(1, 1, w - 2, h - 2, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * A custom border that adds a shadow effect with rounded corners.
     */
    public static class ShadowBorder extends AbstractBorder {
        private int shadowSize;
        private Color shadowColor;
        private int arc;

        /**
         * Constructs a ShadowBorder with the specified shadow size, color, and arc.
         * 
         * @param shadowSize the size of the shadow
         * @param shadowColor the color of the shadow
         * @param arc the arc of the rounded corners
         */
        public ShadowBorder(int shadowSize, Color shadowColor, int arc) {
            this.shadowSize = shadowSize;
            this.shadowColor = shadowColor;
            this.arc = arc;
        }

        /**
         * Paints the border with a layered shadow effect.
         * 
         * @param c the component to paint the border for
         * @param g the Graphics object to paint with
         * @param x the x-coordinate of the border
         * @param y the y-coordinate of the border
         * @param w the width of the border
         * @param h the height of the border
         */
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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
         * Returns the insets of the border.
         * 
         * @param c the component to get insets for
         * @return the insets of the border
         */
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(shadowSize, shadowSize, shadowSize, shadowSize);
        }

        /**
         * Returns the insets of the border, updating the provided Insets object.
         * 
         * @param c the component to get insets for
         * @param insets the Insets object to update
         * @return the updated Insets object
         */
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(shadowSize, shadowSize, shadowSize, shadowSize);
            return insets;
        }
    }
}
