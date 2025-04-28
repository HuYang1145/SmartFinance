package View.LoginAndMain;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public class LoginComponents {

    public static class LoginGradientPanel extends JPanel {
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

    public static class GradientTextButton extends JButton {
        public GradientTextButton(String text) {
            super(text);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, new Color(0x9C27B0), w, h, new Color(0x002FA7));
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w, h, 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class RoundBorder implements Border {
        private int radius;
        private Color color;

        public RoundBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D round = new RoundRectangle2D.Float(x + 1, y + 1, w - 2, h - 2, radius, radius);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2));
            g2.draw(round);
            g2.dispose();
        }
    }

    public static class RoundedTextField extends JTextField {
        private String placeholder;

        public RoundedTextField(String placeholder) {
            this.placeholder = placeholder;
            setText(placeholder);
            setForeground(Color.GRAY);
            setBorder(BorderFactory.createCompoundBorder(
                    new RoundBorder(17, new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (getText().equals(placeholder)) {
                        setText("");
                        setForeground(Color.WHITE);
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (getText().isEmpty()) {
                        setText(placeholder);
                        setForeground(Color.GRAY);
                    }
                }
            });
        }

        public String getActualText() {
            return getText().equals(placeholder) ? "" : getText();
        }
    }

    public static class RoundedPasswordField extends JPasswordField {
        private String placeholder;

        public RoundedPasswordField(String placeholder) {
            this.placeholder = placeholder;
            setText(placeholder);
            setForeground(Color.GRAY);
            setBorder(BorderFactory.createCompoundBorder(
                    new RoundBorder(17, new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (new String(getPassword()).equals(placeholder)) {
                        setText("");
                        setForeground(Color.WHITE);
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (getPassword().length == 0) {
                        setText(placeholder);
                        setForeground(Color.GRAY);
                    }
                }
            });
        }

        public char[] getActualPassword() {
            String text = new String(getPassword());
            return text.equals(placeholder) ? new char[0] : getPassword();
        }
    }

    public static class RoundedComboBox<T> extends JComboBox<T> {
        public RoundedComboBox(T[] items) {
            super(items);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setForeground(Color.WHITE);
            setBackground(new Color(30, 30, 50));
            setOpaque(false);
            setBorder(BorderFactory.createCompoundBorder(
                    new RoundBorder(17, new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));

            setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    if (isSelected) {
                        c.setBackground(new Color(50, 50, 70));
                        c.setForeground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(30, 30, 50));
                        c.setForeground(new Color(180, 180, 180));
                    }
                    return c;
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(new Color(50, 50, 70));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(new Color(30, 30, 50));
                }
            });
        }
    }

    public static void showCustomMessage(Window parent, String message, String title, int messageType) {
        JDialog dialog = new JDialog(parent, title, ModalityType.APPLICATION_MODAL);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(new Color(230, 230, 250));

        JLabel msgLabel = new JLabel("<html><center>" + message + "</center></html>", SwingConstants.CENTER);
        msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        if (messageType == JOptionPane.ERROR_MESSAGE) {
            msgLabel.setForeground(Color.RED);
        } else if (messageType == JOptionPane.WARNING_MESSAGE) {
            msgLabel.setForeground(Color.ORANGE);
        } else {
            msgLabel.setForeground(new Color(50, 50, 50));
        }
        dialog.add(msgLabel, BorderLayout.CENTER);

        JButton ok = new JButton("OK");
        ok.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ok.setBackground(new Color(147, 112, 219));
        ok.setForeground(Color.WHITE);
        ok.setFocusPainted(false);
        ok.addActionListener(e -> dialog.dispose());
        JPanel btnP = new JPanel();
        btnP.setBackground(new Color(230, 230, 250));
        btnP.add(ok);
        dialog.add(btnP, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}