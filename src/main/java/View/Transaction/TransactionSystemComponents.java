package View.Transaction;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.table.DefaultTableCellRenderer;

public class TransactionSystemComponents {
    // Gradient Panels
    public static class DarkGradientPanel extends JPanel {
        private static final Color DARK_GRADIENT_START = new Color(156, 39, 176);
        private static final Color DARK_GRADIENT_END = new Color(40, 100, 250);

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, DARK_GRADIENT_START, w, h, DARK_GRADIENT_END);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
        }
    }

    public static class MidGradientPanel extends JPanel {
        private static final Color MID_GRADIENT_START = new Color(180, 60, 200);
        private static final Color MID_GRADIENT_END = new Color(60, 120, 250);

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, MID_GRADIENT_START, w, h, MID_GRADIENT_END);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
        }
    }

    public static class LightGradientPanel extends JPanel {
        private static final Color LIGHT_GRADIENT_START = new Color(200, 80, 220);
        private static final Color LIGHT_GRADIENT_END = new Color(80, 140, 255);

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, LIGHT_GRADIENT_START, w, h, LIGHT_GRADIENT_END);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
        }
    }

    // Gradient Text Button
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
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

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
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            java.awt.FontMetrics fm = g2.getFontMetrics();
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

    // Colored Header Renderer for JTable
    public static class ColoredHeaderRenderer extends DefaultTableCellRenderer {
        private final Color bg;

        public ColoredHeaderRenderer(Color bg) {
            this.bg = bg;
            setHorizontalAlignment(SwingConstants.CENTER);
            setForeground(Color.WHITE);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBackground(bg);
            return this;
        }
    }

    // Rounded ComboBox
    public static class RoundedComboBox<T> extends JComboBox<T> {
        private int arcSize = 20; // 圆弧半径，需与 RoundedTextField 一致
        private Color borderColor = Color.GRAY; // 边框颜色
        private Color backgroundColor = Color.WHITE; // 背景颜色

        public RoundedComboBox(T[] items) {
            super(items);
            setOpaque(false);
            setBorder(new RoundedBorder(arcSize, borderColor));
            setBackground(backgroundColor);
            setForeground(Color.BLACK);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setUI(new RoundedComboBoxUI());
            // 自定义下拉菜单渲染
            setRenderer(new DefaultListCellRenderer() {
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

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcSize, arcSize);
            super.paintComponent(g2);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcSize, arcSize);
            g2.dispose();
        }

        // 自定义圆弧边框
        private static class RoundedBorder implements Border {
            private int radius;
            private Color borderColor;

            public RoundedBorder(int radius, Color borderColor) {
                this.radius = radius;
                this.borderColor = borderColor;
            }

            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(borderColor);
                g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
            }

            @Override
            public boolean isBorderOpaque() {
                return false;
            }
        }

        // 自定义 UI 以处理下拉箭头
        private class RoundedComboBoxUI extends BasicComboBoxUI {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                comboBox.setOpaque(false);
                arrowButton.setOpaque(false);
                arrowButton.setBorder(null);
                arrowButton.setBackground(new Color(0, 0, 0, 0));
            }

            @Override
            public void configureArrowButton() {
                super.configureArrowButton();
                arrowButton.setOpaque(false);
                arrowButton.setBorder(null);
                arrowButton.setBackground(new Color(0, 0, 0, 0));
            }

            @Override
            protected javax.swing.plaf.basic.ComboPopup createPopup() {
                BasicComboPopup popup = new BasicComboPopup(comboBox) {
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