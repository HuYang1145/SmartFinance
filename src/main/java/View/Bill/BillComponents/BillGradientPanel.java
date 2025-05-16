package View.Bill.BillComponents;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * A custom JPanel that renders a gradient background with an overlay effect.
 * The panel uses two colors to create a diagonal gradient from the top-left to the bottom-right corner,
 * with an additional semi-transparent white gradient overlay in the top half for visual enhancement.
 *
 * @version 1.0
 * @author group19
 */
public class BillGradientPanel extends JPanel {

    /** The starting color of the gradient (default: purple, #9C27B0). */
    private Color color1 = new Color(0x9C27B0);

    /** The ending color of the gradient (default: dark blue, #002FA7). */
    private Color color2 = new Color(0x002FA7);

    /**
     * Paints the panel with a gradient background and an overlay effect.
     * The gradient transitions from color1 at the top-left to color2 at the bottom-right.
     * A semi-transparent white gradient is applied to the top half of the panel for a subtle overlay effect.
     *
     * @param g the Graphics object used for painting
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        GradientPaint gp = new GradientPaint(0, 0, color1, w, h, color2);
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);

        g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 50), w, h, new Color(255, 255, 255, 0)));
        g2.fillRect(0, 0, w, h / 2);

        g2.dispose();
        super.paintComponent(g);
    }
}