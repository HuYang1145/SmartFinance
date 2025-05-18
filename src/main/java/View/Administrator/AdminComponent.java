package View.Administrator;

import java.awt.*;
import javax.swing.*;

public class AdminComponent extends JPanel {
    public static class RoundedShadowPanel extends JPanel {
        private int radius;
        public RoundedShadowPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // 阴影
            g2.setColor(new Color(0,0,0,40));
            g2.fillRoundRect(6, 6, getWidth()-12, getHeight()-12, radius, radius);
            // 主体
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth()-6, getHeight()-6, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
    static class DialogTitleBar extends JPanel {
        public DialogTitleBar(String title) {
            setPreferredSize(new Dimension(100, 42));
            setOpaque(false);
            setLayout(new BorderLayout());
            JLabel label = new JLabel(title, SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 16));
            label.setForeground(Color.WHITE);
            add(label, BorderLayout.CENTER);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(135, 100, 200)); // 主题色
            g2.fillRoundRect(0, 0, getWidth(), getHeight()+16, 16, 16); // 上圆角
            g2.dispose();
            super.paintComponent(g);
        }
    }

}
