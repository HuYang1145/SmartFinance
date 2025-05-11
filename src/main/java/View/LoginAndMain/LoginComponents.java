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