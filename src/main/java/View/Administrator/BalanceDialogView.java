/**
 * A dialog view for displaying the user's account balance in the Smart Finance Application.
 * Provides a simple interface with a label to show the balance or an error message and a close button.
 *
 * @author Group 19
 * @version 1.0
 */
package View.Administrator;

import java.awt.Dialog;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

public class BalanceDialogView extends JDialog {

    private final JLabel balanceLabel;
    private final JButton closeButton;

    /**
     * Constructs a BalanceDialogView dialog for displaying the user's account balance.
     *
     * @param owner the parent dialog that owns this dialog
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
     * Sets the text to display in the balance label, such as the account balance or an error message.
     *
     * @param text the text to display
     */
    public void setBalanceText(String text) {
        balanceLabel.setText(text);
    }

    /**
     * Retrieves the close button for attaching event listeners.
     *
     * @return the close button
     */
    public JButton getCloseButton() {
        return closeButton;
    }

    /**
     * Closes the dialog by hiding and disposing of it.
     */
    public void close() {
        setVisible(false);
        dispose();
    }
}