package View.Administrator;

import java.awt.Dialog;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

/**
 * A dialog view for displaying the user's account balance.
 */
public class BalanceDialogView extends JDialog {

    private final JLabel balanceLabel;
    private final JButton closeButton;

    /**
     * Constructs a balance view dialog.
     *
     * @param owner The parent dialog.
     */
    public BalanceDialogView(Dialog owner) {
        super(owner, "View Balance", true);
        setSize(300, 150);
        setLocationRelativeTo(owner);
        setLayout(new GridBagLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        balanceLabel = new JLabel();
        balanceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(balanceLabel, gbc);

        gbc.gridy = 1;
        closeButton = new JButton("Close");
        add(closeButton, gbc);
    }

    /**
     * Sets the text to display in the balance label.
     *
     * @param text The text to display (balance or error message).
     */
    public void setBalanceText(String text) {
        balanceLabel.setText(text);
    }

    /**
     * Gets the Close button for attaching event listeners.
     *
     * @return The Close button.
     */
    public JButton getCloseButton() {
        return closeButton;
    }

    /**
     * Closes the dialog.
     */
    public void close() {
        setVisible(false);
        dispose();
    }
}