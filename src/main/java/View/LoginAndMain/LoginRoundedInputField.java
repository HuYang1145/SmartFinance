package View.LoginAndMain;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;

public class LoginRoundedInputField {

    public static class RoundedTextField extends JTextField {
        private String placeholder;
        private boolean showingPlaceholder = true;

        public RoundedTextField(String placeholder) {
            this.placeholder = placeholder;
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setForeground(Color.GRAY);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            setPreferredSize(new Dimension(260, 40)); // 增加宽度
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

        public String getActualText() {
            return showingPlaceholder ? "" : getText();
        }
    }

    public static class RoundedPasswordField extends JPasswordField {
        private String placeholder;
        private boolean showingPlaceholder = true;

        public RoundedPasswordField(String placeholder) {
            this.placeholder = placeholder;
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setForeground(Color.GRAY);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            setPreferredSize(new Dimension(260, 40)); // 增加宽度
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

        public String getActualPassword() {
            return showingPlaceholder ? "" : new String(getPassword());
        }

        public boolean isPlaceholderShowing() {
            return showingPlaceholder;
        }
    }

    public static class RoundedComboBox<E> extends JComboBox<E> {
        private Color start = new Color(156, 39, 176);
        private Color end = new Color(0, 47, 167);

        public RoundedComboBox(E[] items) {
            super(items);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setOpaque(false);
            setPreferredSize(new Dimension(260, 40));
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        }

        @Override
        public void updateUI() {
            super.updateUI();
            setUI(new BasicComboBoxUI() {
                @Override
                protected JButton createArrowButton() {
                    JButton btn = new JButton("\u25BE");
                    btn.setOpaque(false);
                    btn.setContentAreaFilled(false);
                    btn.setBorder(null);
                    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    return btn;
                }

                @Override
                public void installUI(JComponent c) {
                    super.installUI(c);
                    comboBox.setOpaque(false);
                    arrowButton.setOpaque(false);
                    arrowButton.setBackground(new Color(0, 0, 0, 0));
                }
            });
        }

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

    public static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(156, 27, 154),
                    w, h, new Color(3, 169, 244)
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
        }
    }

    public static class GradientTextButton extends JButton {
        private Color colorStart = new Color(156, 39, 176);
        private Color colorEnd = new Color(0, 47, 167);

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
        private final Color startColor = new Color(156, 39, 176);
        private final Color endColor = new Color(33, 150, 243);

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

    public static class DeepBlueGradientPanel extends JPanel {
        private final Color startColor = new Color(156, 39, 176);
        private final Color endColor = Color.decode("#4A90E2");

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