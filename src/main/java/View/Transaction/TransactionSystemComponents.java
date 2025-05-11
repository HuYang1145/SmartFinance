
package View.Transaction;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.table.DefaultTableCellRenderer;
import View.LoginAndMain.LoginRoundedInputField.*;

/**
 * Provides custom Swing components for the transaction system interface, including gradient panels,
 * colored table headers, and rounded combo boxes with consistent styling.
 * 
 * @author Group 19
 * @version 1.0
 */
public class TransactionSystemComponents {

    /**
     * A JPanel with a dark gradient background, transitioning from a light blue to a beige color.
     */
    public static class DarkGradientPanel extends JPanel {
        private static final Color DARK_GRADIENT_START = new Color(0x84ACC9);
        private static final Color DARK_GRADIENT_END = new Color(0xFAF0D2);

        /**
         * Paints the panel with a diagonal gradient from DARK_GRADIENT_START to DARK_GRADIENT_END.
         * 
         * @param g the Graphics object to paint with
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, DARK_GRADIENT_START, w, h, DARK_GRADIENT_END);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
        }
    }

    /**
     * A JPanel with a medium gradient background, transitioning from a light blue to a beige color.
     */
    public static class MidGradientPanel extends JPanel {
        private static final Color MID_GRADIENT_START = new Color(0x84ACC9);
        private static final Color MID_GRADIENT_END = new Color(0xFAF0D2);

        /**
         * Paints the panel with a diagonal gradient from MID_GRADIENT_START to MID_GRADIENT_END.
         * 
         * @param g the Graphics object to paint with
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, MID_GRADIENT_START, w, h, MID_GRADIENT_END);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
        }
    }

    /**
     * A JPanel with a blue gradient background, transitioning from a light blue to a beige color.
     */
    public static class BlueGradientPanel extends JPanel {
        private static final Color LIGHT_GRADIENT_START = new Color(0x84ACC9);
        private static final Color LIGHT_GRADIENT_END = new Color(0xFAF0D2);

        /**
         * Paints the panel with a diagonal gradient from LIGHT_GRADIENT_START to LIGHT_GRADIENT_END.
         * 
         * @param g the Graphics object to paint with
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, LIGHT_GRADIENT_START, w, h, LIGHT_GRADIENT_END);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
        }
    }

    /**
     * A JPanel with a light gradient background, transitioning from a light blue to a beige color.
     */
    public static class LightGradientPanel extends JPanel {
        private static final Color LIGHT_GRADIENT_START = new Color(0x84ACC9);
        private static final Color LIGHT_GRADIENT_END = new Color(0xFAF0D2);

        /**
         * Paints the panel with a diagonal gradient from LIGHT_GRADIENT_START to LIGHT_GRADIENT_END.
         * 
         * @param g the Graphics object to paint with
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, LIGHT_GRADIENT_START, w, h, LIGHT_GRADIENT_END);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
        }
    }

    /**
     * A custom table cell renderer for JTable headers with a colored background and centered text.
     */
    public static class ColoredHeaderRenderer extends DefaultTableCellRenderer {
        private final Color bg;

        /**
         * Constructs a ColoredHeaderRenderer with the specified background color.
         * 
         * @param bg the background color for the header
         */
        public ColoredHeaderRenderer(Color bg) {
            this.bg = bg;
            setHorizontalAlignment(SwingConstants.CENTER);
            setForeground(Color.WHITE);
            setOpaque(true);
        }

        /**
         * Returns the component used for rendering a table header cell with the specified background color.
         * 
         * @param table the JTable that is asking the renderer to draw
         * @param value the value of the cell
         * @param isSelected true if the cell is selected
         * @param hasFocus true if the cell has focus
         * @param row the row index of the cell
         * @param column the column index of the cell
         * @return the component used for rendering
         */
        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBackground(bg);
            return this;
        }
    }

    /**
     * A custom JComboBox with rounded corners and a consistent appearance for the transaction system.
     */
    public static class RoundedComboBox<T> extends JComboBox<T> {
        private int arcSize = 20; // Arc radius, consistent with RoundedTextField
        private Color borderColor = Color.GRAY; // Border color
        private Color backgroundColor = Color.WHITE; // Background color

        /**
         * Constructs a RoundedComboBox with the specified items.
         * 
         * @param items the items to display in the combo box
         */
        public RoundedComboBox(T[] items) {
            super(items);
            setOpaque(false);
            setBorder(new RoundedBorder(arcSize, borderColor));
            setBackground(backgroundColor);
            setForeground(Color.BLACK);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setUI(new RoundedComboBoxUI());
            // Custom renderer for dropdown menu
            setRenderer(new DefaultListCellRenderer() {
                /**
                 * Returns the component used for rendering a list cell in the combo box dropdown.
                 * 
                 * @param list the JList being rendered
                 * @param value the value to render
                 * @param index the index of the cell
                 * @param isSelected true if the cell is selected
                 * @param cellHasFocus true if the cell has focus
                 * @return the component used for rendering
                 */
                @Override
                public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                    label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                    label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    if (isSelected) {
                        label.setBackground(new Color(200, 200, 255));
                        label.setForeground(Color.BLACK);
                    } else {
                        label.setBackground(Color.WHITE);
                        label.setForeground(Color.BLACK);
                    }
                    return label;
                }
            });
        }

        /**
         * Paints the component with a rounded white background.
         * 
         * @param g the Graphics object to paint with
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcSize, arcSize);
            super.paintComponent(g2);
            g2.dispose();
        }

        /**
         * Paints the border with a rounded gray outline.
         * 
         * @param g the Graphics object to paint with
         */
        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcSize, arcSize);
            g2.dispose();
        }

        /**
         * A custom UI for the RoundedComboBox to handle the arrow button and popup appearance.
         */
        private class RoundedComboBoxUI extends BasicComboBoxUI {
            /**
             * Installs default properties, ensuring transparency for the combo box and arrow button.
             */
            @Override
            protected void installDefaults() {
                super.installDefaults();
                comboBox.setOpaque(false);
                arrowButton.setOpaque(false);
                arrowButton.setBorder(null);
                arrowButton.setBackground(new Color(0, 0, 0, 0));
            }

            /**
             * Configures the arrow button to be transparent.
             */
            @Override
            public void configureArrowButton() {
                super.configureArrowButton();
                arrowButton.setOpaque(false);
                arrowButton.setBorder(null);
                arrowButton.setBackground(new Color(0, 0, 0, 0));
            }

            /**
             * Creates a custom popup with a rounded border for the combo box dropdown.
             * 
             * @return the custom ComboPopup
             */
            @Override
            protected javax.swing.plaf.basic.ComboPopup createPopup() {
                BasicComboPopup popup = new BasicComboPopup(comboBox) {
                    /**
                     * Configures the popup with a rounded border and transparent background.
                     */
                    @Override
                    protected void configurePopup() {
                        super.configurePopup();
                        setBorder(new RoundedBorder(arcSize, borderColor));
                        setOpaque(false);
                        setBackground(new Color(0, 0, 0, 0));
                    }
                };
                return popup;
            }
        }
    }
}
