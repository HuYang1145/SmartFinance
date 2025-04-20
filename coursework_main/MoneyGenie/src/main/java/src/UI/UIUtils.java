package src.UI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JOptionPane;

public class UIUtils {
    public static JButton createStyledButton(String text, Color textColor, Color bgColor, Dimension size, Font font) {
        JButton button = new JButton(text);
        button.setForeground(textColor);
        button.setBackground(bgColor);
        button.setPreferredSize(size);
        button.setFont(font);
        return button;
    }
    public static void showLoginError(Component parent) {
        JOptionPane.showMessageDialog(parent, "Please log in to continue.", "Login Required", JOptionPane.ERROR_MESSAGE);
    }
}
