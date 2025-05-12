/**
 * Provides utility methods for displaying custom message dialogs in the Smart Finance Application.
 * Supports styled message dialogs with configurable titles, messages, and types (e.g., error, warning, information).
 *
 * @author Group 19
 * @version 1.0
 */
package View.LoginAndMain;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.Window;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class LoginComponents {

    /**
     * Displays a custom message dialog with the specified message, title, and type.
     * The dialog is modal, centered relative to the parent window, and styled with a consistent look.
     *
     * @param parent      the parent window for the dialog
     * @param message     the message to display, supports HTML for formatting
     * @param title       the title of the dialog
     * @param messageType the type of message (e.g., JOptionPane.ERROR_MESSAGE, WARNING_MESSAGE, INFORMATION_MESSAGE)
     */
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