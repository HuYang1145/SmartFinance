package View.LoginAndMain;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class GradientComponents {
    public static class GradientBorder extends AbstractBorder {
        private int thickness, radius;

        public GradientBorder(int thickness, int radius) {
            this.thickness = thickness;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            Shape outer = new RoundRectangle2D.Float(
                    x + thickness / 2f, y + thickness / 2f,
                    w - thickness, h - thickness,
                    radius, radius
            );
            GradientPaint gp = new GradientPaint(
                    x, y, Color.decode("#84ACC9"),
                    x + w, y + h,Color.decode("#A1DDA3")
            );
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
    public static class GradientPanel extends JPanel {
        public GradientPanel() {
            setOpaque(true);  // 一定要设置为 opaque，自己负责画背景
        }

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
            g2.dispose();  // 释放掉临时 Graphics2D
        }
    }


    public static class GradientTextButton extends JButton {
        private Color colorStart = Color.decode("#84ACC9");
        private Color colorEnd = Color.decode("#A1DDA3");

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

    public static class GradientButton extends JButton {
        private final Color startColor = Color.decode("#84ACC9");
        private final Color endColor = Color.decode("#A1DDA3");

        public GradientButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

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

        @Override
        public void updateUI() {
            super.updateUI();
            setOpaque(false);
            setContentAreaFilled(false);
        }
    }


    public static void styleScrollBar(JScrollBar bar) {
        bar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                // 拖拽块颜色
                this.thumbColor = Color.white;
                // 滑道背景色，如果想保持默认可以不用这行
                this.trackColor = Color.ORANGE;
            }
            // 如果你想让滚动箭头也有颜色，可以覆盖下面两个方法
            @Override
            protected void paintDecreaseHighlight(Graphics g) { }
            @Override
            protected void paintIncreaseHighlight(Graphics g) { }
        });
    }
    public static class DeepBlueGradientPanel extends JPanel {
        private final Color startColor = Color.decode("#84ACC9");
        private final Color endColor = Color.decode("#A1DDA3");

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

    public static class ShadowBorder extends AbstractBorder {
        private int shadowSize;
        private Color shadowColor;
        private int arc;

        public ShadowBorder(int shadowSize, Color shadowColor, int arc) {
            this.shadowSize = shadowSize;
            this.shadowColor = shadowColor;
            this.arc = arc;
        }

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
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(shadowSize, shadowSize, shadowSize, shadowSize);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(shadowSize, shadowSize, shadowSize, shadowSize);
            return insets;
        }
    }
}
