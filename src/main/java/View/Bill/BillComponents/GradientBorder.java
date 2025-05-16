package View.Bill.BillComponents;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

import javax.swing.border.AbstractBorder;

/**
 * A custom border that renders a gradient outline with rounded corners around a component.
 * The gradient transitions from a starting color (purple, #9C27B0) at the top-left to an ending color
 * (dark blue, #002FA7) at the bottom-right, with configurable thickness and corner radius.
 *
 * @version 1.0
 * @author group19
 */
public class GradientBorder extends AbstractBorder {

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
     * The gradient transitions from purple (#9C27B0) at the top-left to dark blue (#002FA7) at the
     * bottom-right, with anti-aliasing enabled for smooth rendering.
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
        Shape outer = new RoundRectangle2D.Float(x + thickness / 2f, y + thickness / 2f, w - thickness, h - thickness, radius, radius);
        GradientPaint gp = new GradientPaint(x, y, new Color(0x9C27B0), x + w, y + h, new Color(0x002FA7));
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