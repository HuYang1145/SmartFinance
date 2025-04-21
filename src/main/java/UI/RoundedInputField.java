package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RoundedInputField {

    public static class RoundedTextField extends JTextField {
        private String placeholder;
        private boolean showingPlaceholder = true;

        public RoundedTextField(String placeholder) {
            this.placeholder = placeholder;
            setFont(new Font("Segoe UI", Font.PLAIN, 18));
            setForeground(Color.GRAY);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            setPreferredSize(new Dimension(240, 50));
            setText(placeholder);

            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) {
                    if (showingPlaceholder) {
                        setText("");
                        setForeground(Color.DARK_GRAY);
                        showingPlaceholder = false;
                    }
                }

                @Override public void focusLost(FocusEvent e) {
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
            setFont(new Font("Segoe UI", Font.PLAIN, 18));
            setForeground(Color.GRAY);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            setPreferredSize(new Dimension(240, 50));
            setEchoChar((char) 0);
            setText(placeholder);

            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) {
                    if (showingPlaceholder) {
                        setText("");
                        setForeground(Color.DARK_GRAY);
                        setEchoChar('●');
                        showingPlaceholder = false;
                    }
                }

                @Override public void focusLost(FocusEvent e) {
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
        public RoundedComboBox(E[] items) {
            super(items);
            setFont(new Font("Segoe UI", Font.PLAIN, 18));
            setOpaque(false);
            setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value,
                                                              int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel lbl = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                    return lbl;
                }
            });
            setPreferredSize(new Dimension(240, 50));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            int arc = 30;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // 背景
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            // 边框
            g2.setColor(new Color(200, 200, 200));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }
    static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            int w = getWidth(), h = getHeight();
            // 变浅：紫色和蓝色都提亮一点
            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(156, 39, 176),
                    w, h, new Color(40, 100, 250)
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
        }
    }
    static class GradientTextButton extends JButton {
        private Color colorStart = new Color(156, 39, 176); // 紫
        private Color colorEnd = new Color(0, 47, 167);   // 蓝

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

            // 开启抗锯齿
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

    static class GradientLabel extends JLabel {
        private Color c1 = new Color(0x9C27B0);
        private Color c2 = new Color(0x002FA7);

        public GradientLabel(String text) {
            super(text);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            FontMetrics fm = g2.getFontMetrics(getFont());
            int textWidth = fm.stringWidth(getText());
            int textHeight = fm.getHeight();

            // 构造水平渐变
            GradientPaint gp = new GradientPaint(
                    0, 0, c1,
                    textWidth, 0, c2
            );
            g2.setPaint(gp);
            g2.setFont(getFont());

            // 文本基线
            int x = 0;
            int y = fm.getAscent();

            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }
}

