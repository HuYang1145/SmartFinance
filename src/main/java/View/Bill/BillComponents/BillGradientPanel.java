package View.Bill.BillComponents;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

public class BillGradientPanel extends JPanel {
    private Color color1 = new Color(0x9C27B0);
    private Color color2 = new Color(0x002FA7);

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