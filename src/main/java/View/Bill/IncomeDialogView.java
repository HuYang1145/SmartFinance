/**
 * A dialog view for adding income records in the Smart Finance Application.
 * Provides input fields for amount, time, and password, along with confirm and cancel buttons.
 *
 * @author Group 19
 * @version 1.0
 */
package View.Bill;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class IncomeDialogView extends JDialog {
    private JTextField amountField;
    private JTextField timeField;
    private JPasswordField passwordField;
    private JButton confirmButton;
    private JButton cancelButton;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    /**
     * Constructs an IncomeDialogView for adding income records.
     *
     * @param owner the parent dialog that owns this dialog
     */
    public IncomeDialogView(Dialog owner) {
        super(owner, "Add Income", true);
        initComponents();
        layoutComponents();
        pack();
        setMinimumSize(new Dimension(400, 250));
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /**
     * Initializes the dialog's components, including input fields and buttons.
     */
    private void initComponents() {
        amountField = new JTextField(15);
        timeField = new JTextField(DATE_FORMAT.format(new Date()), 15);
        passwordField = new JPasswordField(15);
        confirmButton = new JButton("Confirm Income");
        cancelButton = new JButton("Cancel");

        confirmButton.setBackground(new Color(30, 60, 120));
        confirmButton.setForeground(Color.WHITE);
        cancelButton.setBackground(new Color(200, 200, 200));
    }

    /**
     * Lays out the dialog's components, including title, input form, and button panel.
     */
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 245, 245));

        JLabel titleLabel = new JLabel("Add Income Record");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(titleLabel, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        panel.add(new JLabel("Amount (¥):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(amountField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        panel.add(new JLabel("Time (yyyy/MM/dd HH:mm):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(timeField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(passwordField, gbc);

        add(panel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Gets the amount input field.
     *
     * @return the amount text field
     */
    public JTextField getAmountField() { return amountField; }

    /**
     * Gets the time input field.
     *
     * @return the time text field
     */
    public JTextField getTimeField() { return timeField; }

    /**
     * Gets the password input field.
     *
     * @return the password field
     */
    public JPasswordField getPasswordField() { return passwordField; }

    /**
     * Gets the confirm button.
     *
     * @return the confirm button
     */
    public JButton getConfirmButton() { return confirmButton; }

    /**
     * Gets the cancel button.
     *
     * @return the cancel button
     */
    public JButton getCancelButton() { return cancelButton; }

    /**
     * Displays an error message dialog.
     *
     * @param message the error message to display
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Displays a success message dialog.
     *
     * @param message the success message to display
     */
    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Clears the password field.
     */
    public void clearPassword() {
        passwordField.setText("");
    }
}