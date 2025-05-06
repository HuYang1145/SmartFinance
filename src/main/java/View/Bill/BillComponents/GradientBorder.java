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

public class GradientBorder extends AbstractBorder {
    private int thickness, radius;

    public GradientBorder(int thickness, int radius) {
        this.thickness = thickness;
        this.radius = radius;
    }

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

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(thickness, thickness, thickness, thickness);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.set(thickness, thickness, thickness, thickness);
        return insets;
    }
}