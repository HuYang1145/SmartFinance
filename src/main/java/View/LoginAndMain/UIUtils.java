package View.LoginAndMain;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JOptionPane;

/**
 * A utility class for creating styled UI components and displaying common UI messages.
 * Provides methods for creating customized buttons and showing login error dialogs.
 *
 * @version 1.4
 * @author group19
 */
public class UIUtils {

    /**
     * Creates a styled JButton with specified text, colors, size, and font.
     *
     * @param text      the text to display on the button
     * @param textColor the color of the button's text
     * @param bgColor   the background color of the button
     * @param size      the preferred size of the button
     * @param font      the font for the button's text
     * @return a configured JButton with the specified properties
     */
    public static JButton createStyledButton(String text, Color textColor, Color bgColor, Dimension size, Font font) {
        JButton button = new JButton(text);
        button.setForeground(textColor);
        button.setBackground(bgColor);
        button.setPreferredSize(size);
        button.setFont(font);
        return button;
    }

    /**
     * Displays an error dialog indicating that login is required.
     *
     * @param parent the parent component for the dialog, used for positioning
     */
    public static void showLoginError(Component parent) {
        JOptionPane.showMessageDialog(parent, "Please log in to continue.", "Login Required", JOptionPane.ERROR_MESSAGE);
    }
}