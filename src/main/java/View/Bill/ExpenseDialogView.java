/**
 * A dialog view for adding expense records in the Smart Finance Application.
 * Provides input fields for amount, time, merchant, type, and password, along with confirm and cancel buttons.
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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class ExpenseDialogView extends JDialog {
    private JTextField amountField;
    private JTextField timeField;
    private JTextField merchantField;
    private JComboBox<String> typeComboBox;
    private JPasswordField passwordField;
    private JButton confirmButton;
    private JButton cancelButton;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    private static final String[] EXPENSE_TYPES = {
        "(Select Type)", "Food", "Shopping", "Transport", "Entertainment",
        "Education", "Transfer", "Other"
    };

    /**
     * Constructs an ExpenseDialogView for adding expense records.
     *
     * @param owner the parent dialog that owns this dialog
     */
    public ExpenseDialogView(Dialog owner) {
        super(owner, "Add Expense", true);
        initComponents();
        layoutComponents();
        pack();
        setMinimumSize(new Dimension(450, 350));
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /**
     * Initializes the dialog's components, including input fields and buttons.
     */
    private void initComponents() {
        amountField = new JTextField(15);
        timeField = new JTextField(DATE_FORMAT.format(new Date()), 15);
        merchantField = new JTextField(15);
        typeComboBox = new JComboBox<>(EXPENSE_TYPES);
        passwordField = new JPasswordField(15);
        confirmButton = new JButton("Confirm Expense");
        cancelButton = new JButton("Cancel");

        // Set button styles
        confirmButton.setBackground(new Color(220, 53, 69)); // Red for expense
        confirmButton.setForeground(Color.WHITE);
        cancelButton.setBackground(new Color(200, 200, 200));
    }

    /**
     * Lays out the dialog's components, including title, input form, and button panel.
     */
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 245, 245));

        // Title
        JLabel titleLabel = new JLabel("Add Expense Record");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(titleLabel, BorderLayout.NORTH);

        // Form panel
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Amount
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        panel.add(new JLabel("Amount (¥):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(amountField, gbc);

        // Time
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Time (yyyy/MM/dd HH:mm):"), gbc);
        gbc.gridx = 1;
        panel.add(timeField, gbc);

        // Merchant
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Merchant/Payee:"), gbc);
        gbc.gridx = 1;
        panel.add(merchantField, gbc);

        // Type
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        panel.add(typeComboBox, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        add(panel, BorderLayout.CENTER);

        // Button panel
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
    public JTextField getAmountField() {
        return amountField;
    }

    /**
     * Gets the time input field.
     *
     * @return the time text field
     */
    public JTextField getTimeField() {
        return timeField;
    }

    /**
     * Gets the merchant input field.
     *
     * @return the merchant text field
     */
    public JTextField getMerchantField() {
        return merchantField;
    }

    /**
     * Gets the type selection combo box.
     *
     * @return the type combo box
     */
    public JComboBox<String> getTypeComboBox() {
        return typeComboBox;
    }

    /**
     * Gets the password input field.
     *
     * @return the password field
     */
    public JPasswordField getPasswordField() {
        return passwordField;
    }

    /**
     * Gets the confirm button.
     *
     * @return the confirm button
     */
    public JButton getConfirmButton() {
        return confirmButton;
    }

    /**
     * Gets the cancel button.
     *
     * @return the cancel button
     */
    public JButton getCancelButton() {
        return cancelButton;
    }

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